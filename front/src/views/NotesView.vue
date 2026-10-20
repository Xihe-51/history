<template>
  <div class="notes-page">
    <top-component />

    <div class="notes-container">
      <!-- 顶部 hero 标题区 -->
      <div class="hero-section">
        <div class="hero-content">
          <h1 class="hero-title">📒 我的笔记</h1>
          <p class="hero-subtitle">记录历史 · 关联人物 / 事件 / 典故</p>
          <div class="hero-stats">
            <div class="stat-item">
              <span class="stat-number">{{ statistics.totalCount ?? notes.length }}</span>
              <span class="stat-label">总笔记</span>
            </div>
            <div class="stat-item" v-for="t in statistics.typeStats" :key="t.type">
              <span class="stat-number">{{ t.count }}</span>
              <span class="stat-label">{{ t.typeName }}</span>
            </div>
          </div>
        </div>
        <div class="hero-wave"></div>
      </div>

      <!-- 玻璃拟态筛选 / 工具面板 -->
      <div class="filter-glass">
        <div class="filter-header">
          <span class="filter-icon">🔍</span>
          <span class="filter-title">笔记检索</span>
        </div>

        <div class="filter-row">
          <el-input
            v-model="keyword"
            placeholder="搜索标题或内容"
            clearable
            :prefix-icon="Search"
            @keyup.enter="reload"
            @clear="reload"
            class="search-input"
          />
          <el-select v-model="type" placeholder="全部类型" clearable @change="reload" class="type-select">
            <el-option label="学习笔记" value="study" />
            <el-option label="心得笔记" value="insight" />
            <el-option label="收藏笔记" value="collect" />
            <el-option label="自定义" value="custom" />
          </el-select>
          <el-button type="primary" class="query-btn" @click="reload">查询</el-button>
        </div>

        <div class="filter-actions">
          <div class="actions-left">
            <el-button :icon="Download" :disabled="notes.length === 0" @click="exportNotes('markdown')">导出 Markdown</el-button>
            <el-button :icon="Download" :disabled="notes.length === 0" @click="exportNotes('html')">导出 PDF/HTML</el-button>
          </div>
          <el-button type="success" :icon="Plus" class="add-btn" @click="openAdd">新建笔记</el-button>
        </div>
      </div>

      <div class="grid" v-loading="loading">
        <div v-for="note in notes" :key="note.id" class="card">
          <div class="card-top">
            <div class="card-title" :title="note.title">{{ note.title }}</div>
            <el-tag size="small" :type="typeTagType(note.type)">{{ typeName(note.type) }}</el-tag>
          </div>
          <div class="card-tags">
            <el-tag v-for="t in getTags(note.tags)" :key="t" size="small" effect="plain">{{ t }}</el-tag>
          </div>
          <!-- 富文本摘要：已剥离标签，避免 v-html 注入风险 -->
          <div class="card-content">{{ getShortContent(note.content) }}</div>
          <div class="card-meta">更新于 {{ formatTime(note.updateTime || note.createTime) }}</div>
          <div class="card-footer">
            <el-button size="small" @click="openEdit(note)">编辑</el-button>
            <el-button size="small" @click="viewNote(note)">查看</el-button>
            <el-button size="small" type="danger" @click="del(note)">删除</el-button>
          </div>
        </div>
      </div>

      <div v-if="notes.length === 0 && !loading" class="empty-state">
        <div class="empty-icon">📝</div>
        <p>{{ keyword || type ? '没有符合条件的笔记' : '暂无笔记，点击「新建笔记」开始记录' }}</p>
      </div>

      <el-pagination
        v-if="total > pageSize"
        v-model:current-page="currentPage"
        :page-size="pageSize"
        :total="total"
        layout="prev, pager, next, total"
        @current-change="loadNotes"
        class="notes-pagination"
      />

      <!-- 新建 / 编辑 -->
      <el-dialog v-model="showEditor" width="min(900px, 92vw)" top="5vh" :title="editId ? '编辑笔记' : '新建笔记'" destroy-on-close>
        <NoteEditor
          :key="editId ?? 'new'"
          :note-id="editId"
          @saved="onSaved"
          @cancel="showEditor = false"
        />
      </el-dialog>

      <!-- 查看 -->
      <el-dialog v-model="showViewer" width="min(820px, 92vw)" :title="viewing?.title || '笔记详情'" destroy-on-close>
        <div v-if="viewing" class="note-view">
          <div class="view-meta">
            <el-tag size="small">{{ typeName(viewing.type) }}</el-tag>
            <span>{{ formatTime(viewing.createTime) }}</span>
          </div>
          <!-- eslint-disable-next-line vue/no-v-html -->
          <div class="view-content" v-html="viewing.content || '（无内容）'"></div>
        </div>
      </el-dialog>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Plus, Download } from '@element-plus/icons-vue'
import topComponent from '@/components/topComponent.vue'
import NoteEditor from '@/components/NoteEditor.vue'
import { getNotePage, deleteNote, getNoteStatistics, downloadNoteExport } from '@/api/note'
import { errorMessage } from '@/utils/request'
import type { NotePageItemVO, NoteStatisticsVO } from '@/type/note'

// 列表状态（服务端分页 + 服务端筛选；用户维度由后端按 token 决定，前端不再自行过滤）
const notes = ref<NotePageItemVO[]>([])
const total = ref(0)
const loading = ref(false)
const keyword = ref('')
const type = ref('')
const currentPage = ref(1)
const pageSize = ref(9)

const statistics = ref<Partial<NoteStatisticsVO>>({ totalCount: 0, typeStats: [] })

const showEditor = ref(false)
const editId = ref<number | null>(null)
const showViewer = ref(false)
const viewing = ref<NotePageItemVO | null>(null)

const typeNameMap: Record<string, string> = {
  study: '学习笔记',
  insight: '心得笔记',
  collect: '收藏笔记',
  custom: '自定义'
}

const typeName = (t?: string) => typeNameMap[t || ''] || '未分类'

function typeTagType(t?: string): 'success' | 'warning' | 'info' | 'primary' {
  switch (t) {
    case 'study': return 'success'
    case 'insight': return 'warning'
    case 'collect': return 'info'
    default: return 'primary'
  }
}

const getTags = (tags: any): string[] => {
  if (!tags) return []
  if (Array.isArray(tags)) return tags
  if (typeof tags === 'string') {
    try { return JSON.parse(tags) } catch { return tags.split(',').filter(Boolean) }
  }
  return []
}

const getShortContent = (content?: string) => {
  if (!content) return '（暂无内容）'
  const text = content.replace(/<[^>]+>/g, ' ').replace(/&nbsp;/g, ' ').replace(/\s+/g, ' ').trim()
  return text.length > 120 ? text.substring(0, 120) + '…' : (text || '（暂无内容）')
}

const formatTime = (t?: string) => (t ? String(t).replace('T', ' ').substring(0, 16) : '')

// ==================== 数据加载 ====================
async function loadNotes() {
  loading.value = true
  try {
    const res = await getNotePage({
      pageNum: currentPage.value,
      pageSize: pageSize.value,
      type: type.value || undefined,
      keyword: keyword.value.trim() || undefined
    })
    if (res.data.code === 200) {
      notes.value = res.data.data?.records || []
      total.value = res.data.data?.total || 0
    } else {
      ElMessage.error(res.data.msg || '加载失败')
    }
  } catch (e: any) {
    ElMessage.error(errorMessage(e, '加载笔记失败'))
  } finally {
    loading.value = false
  }
}

async function loadStatistics() {
  try {
    const res = await getNoteStatistics()
    if (res.data.code === 200 && res.data.data) {
      statistics.value = res.data.data
    }
  } catch {
    /* 统计失败不影响主流程 */
  }
}

function reload() {
  currentPage.value = 1
  loadNotes()
}

// ==================== 操作 ====================
function openAdd() {
  editId.value = null
  showEditor.value = true
}

function openEdit(note: NotePageItemVO) {
  editId.value = note.id
  showEditor.value = true
}

function onSaved() {
  showEditor.value = false
  loadNotes()
  loadStatistics()
}

function viewNote(note: NotePageItemVO) {
  viewing.value = note
  showViewer.value = true
}

async function del(note: NotePageItemVO) {
  try {
    await ElMessageBox.confirm(`确定删除笔记「${note.title}」吗？删除后不可恢复。`, '删除确认', {
      type: 'warning',
      confirmButtonText: '删除',
      cancelButtonText: '取消'
    })
  } catch {
    return
  }
  try {
    const res = await deleteNote(note.id)
    if (res.data.code === 200) {
      ElMessage.success('删除成功')
      // 删除当前页最后一条时回退一页，避免停在空白页
      if (notes.value.length === 1 && currentPage.value > 1) {
        currentPage.value--
      }
      loadNotes()
      loadStatistics()
    } else {
      ElMessage.error(res.data.msg || '删除失败')
    }
  } catch (e: any) {
    ElMessage.error(errorMessage(e, '删除失败'))
  }
}

async function exportNotes(format: 'markdown' | 'html') {
  try {
    const ids = notes.value.map(n => n.id)
    const filename = await downloadNoteExport(ids, format)
    ElMessage.success(`已导出：${filename}`)
    if (format === 'html') {
      ElMessage.info('如需 PDF，请在打开的文件中使用“打印 → 另存为 PDF”')
    }
  } catch (e: any) {
    ElMessage.error(errorMessage(e, '导出失败'))
  }
}

onMounted(() => {
  loadNotes()
  loadStatistics()
})
</script>

<style scoped>
/* ==================== 页面基底 ==================== */
.notes-page {
  min-height: 100vh;
  background: linear-gradient(160deg, #f9f5f0 0%, #f7f2ea 42%, #efe6d5 100%);
  overflow-x: hidden;
}

.notes-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px 64px;
}

/* ==================== 顶部 hero ==================== */
.hero-section {
  position: relative;
  padding: 60px 0 68px;
  text-align: center;
  background: radial-gradient(ellipse at 50% 0%, rgba(216, 180, 103, 0.24) 0%, rgba(216, 180, 103, 0.07) 45%, rgba(247, 242, 234, 0) 78%);
}

.hero-title {
  font-size: 50px;
  font-weight: 700;
  letter-spacing: 2px;
  margin-bottom: 14px;
  background: linear-gradient(135deg, #8B4513 0%, #a8792f 58%, #c9a227 100%);
  -webkit-background-clip: text;
  background-clip: text;
  color: transparent;
}

.hero-subtitle {
  font-size: 17px;
  color: #8a7a63;
  letter-spacing: 2px;
  margin-bottom: 30px;
}

.hero-stats {
  display: flex;
  justify-content: center;
  flex-wrap: wrap;
  gap: 44px;
}

.stat-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  min-width: 84px;
}

.stat-number {
  font-size: 30px;
  font-weight: 700;
  line-height: 1.2;
  color: #b8912f;
}

.stat-label {
  margin-top: 5px;
  font-size: 13px;
  color: #8a7a63;
  letter-spacing: 1px;
}

.hero-wave {
  position: absolute;
  bottom: -2px;
  left: 0;
  width: 100%;
  height: 56px;
  background: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 1440 320'%3E%3Cpath fill='%23d8b467' fill-opacity='0.28' d='M0,96L48,112C96,128,192,160,288,160C384,160,480,128,576,122.7C672,117,768,139,864,154.7C960,171,1056,181,1152,165.3C1248,149,1344,107,1392,85.3L1440,64L1440,320L1392,320C1344,320,1248,320,1152,320C1056,320,960,320,864,320C768,320,672,320,576,320C480,320,384,320,288,320C192,320,96,320,48,320L0,320Z'%3E%3C/path%3E%3C/svg%3E") no-repeat bottom;
  background-size: cover;
  pointer-events: none;
}

/* ==================== 玻璃拟态筛选 / 工具面板 ==================== */
.filter-glass {
  position: relative;
  margin: -30px auto 30px;
  padding: 22px 28px;
  background: rgba(255, 255, 255, 0.72);
  backdrop-filter: blur(14px);
  -webkit-backdrop-filter: blur(14px);
  border-radius: 24px;
  border: 1px solid rgba(216, 180, 103, 0.38);
  box-shadow: 0 12px 32px rgba(139, 69, 19, 0.08), 0 2px 8px rgba(139, 69, 19, 0.05);
}

.filter-header {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 18px;
  padding-bottom: 14px;
  border-bottom: 1px solid rgba(216, 180, 103, 0.28);
}

.filter-icon {
  font-size: 20px;
}

.filter-title {
  flex: 1;
  font-size: 17px;
  font-weight: 600;
  letter-spacing: 1px;
  color: #8B4513;
}

.filter-row {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
}

.search-input {
  width: 320px;
}

.type-select {
  width: 150px;
}

.search-input :deep(.el-input__wrapper),
.type-select :deep(.el-select__wrapper) {
  border-radius: 12px;
  background: rgba(255, 255, 255, 0.88);
  box-shadow: 0 0 0 1px rgba(216, 180, 103, 0.4) inset;
  transition: box-shadow 0.3s;
}

.search-input :deep(.el-input__wrapper.is-focus),
.type-select :deep(.el-select__wrapper.is-focused) {
  box-shadow: 0 0 0 1px #c9a227 inset, 0 0 0 4px rgba(201, 162, 39, 0.13);
}

.search-input :deep(.el-input__inner) {
  color: #5a4634;
}

.filter-glass :deep(.el-button) {
  border-radius: 12px;
  transition: all 0.3s;
}

.query-btn {
  min-width: 88px;
  font-weight: 500;
  color: #fff;
  border: none;
  background: linear-gradient(135deg, #d8b467, #c9a227);
  box-shadow: 0 6px 16px rgba(201, 162, 39, 0.28);
}

.query-btn:hover {
  color: #fff;
  border: none;
  background: linear-gradient(135deg, #e0bd76, #d0a92e);
  transform: translateY(-2px);
  box-shadow: 0 10px 22px rgba(201, 162, 39, 0.38);
}

.filter-actions {
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex-wrap: wrap;
  gap: 12px;
  padding-top: 16px;
  border-top: 1px dashed rgba(216, 180, 103, 0.32);
}

.actions-left {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.actions-left :deep(.el-button) {
  color: #8B4513;
  background: rgba(255, 255, 255, 0.7);
  border: 1px solid rgba(216, 180, 103, 0.5);
}

.actions-left :deep(.el-button:hover) {
  color: #8B4513;
  background: rgba(216, 180, 103, 0.18);
  border-color: #c9a227;
  transform: translateY(-2px);
}

.actions-left :deep(.el-button.is-disabled) {
  color: #bdb0a0;
  background: rgba(255, 255, 255, 0.5);
  border-color: rgba(216, 180, 103, 0.25);
}

.add-btn {
  font-weight: 500;
  color: #fff;
  border: none;
  background: linear-gradient(135deg, #c9a227, #a8792f);
  box-shadow: 0 6px 16px rgba(168, 121, 47, 0.26);
}

.add-btn:hover {
  color: #fff;
  border: none;
  background: linear-gradient(135deg, #d0a92e, #b8852f);
  transform: translateY(-2px);
  box-shadow: 0 10px 22px rgba(168, 121, 47, 0.34);
}

/* ==================== 笔记卡片网格 ==================== */
.grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 20px;
  min-height: 120px;
}

.card {
  position: relative;
  display: flex;
  flex-direction: column;
  padding: 20px;
  background: linear-gradient(180deg, #ffffff 0%, #fdfaf4 100%);
  border-radius: 18px;
  border: 1px solid rgba(216, 180, 103, 0.22);
  box-shadow: 0 4px 14px rgba(139, 69, 19, 0.06), 0 1px 3px rgba(139, 69, 19, 0.04);
  transition: all 0.35s cubic-bezier(0.22, 0.61, 0.36, 1);
  overflow: hidden;
}

.card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 3px;
  background: linear-gradient(90deg, #d8b467, #c9a227, #d8b467);
  opacity: 0;
  transition: opacity 0.35s;
}

.card:hover {
  transform: translateY(-6px);
  border-color: rgba(201, 162, 39, 0.5);
  box-shadow: 0 16px 34px rgba(139, 69, 19, 0.12), 0 4px 10px rgba(201, 162, 39, 0.1);
}

.card:hover::before {
  opacity: 1;
}

.card-top {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
  margin-bottom: 10px;
}

.card-title {
  font-size: 17px;
  font-weight: 700;
  color: #8B4513;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.card :deep(.el-tag) {
  border-radius: 8px;
}

.card-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  min-height: 24px;
  margin-bottom: 12px;
}

.card-content {
  flex: 1;
  margin-bottom: 12px;
  font-size: 13px;
  line-height: 1.7;
  color: #6b5a48;
  word-break: break-word;
}

.card-meta {
  margin-bottom: 12px;
  font-size: 12px;
  color: #a89a86;
}

.card-footer {
  display: flex;
  gap: 8px;
  justify-content: flex-end;
  padding-top: 14px;
  margin-top: auto;
  border-top: 1px solid rgba(216, 180, 103, 0.25);
}

.card-footer :deep(.el-button) {
  border-radius: 10px;
  transition: all 0.25s;
}

.card-footer :deep(.el-button:not(.el-button--danger)) {
  color: #8B4513;
  background: rgba(216, 180, 103, 0.12);
  border-color: rgba(216, 180, 103, 0.45);
}

.card-footer :deep(.el-button:not(.el-button--danger):hover) {
  color: #8B4513;
  background: rgba(216, 180, 103, 0.26);
  border-color: #c9a227;
  transform: translateY(-1px);
}

/* ==================== 空状态 ==================== */
.empty-state {
  padding: 70px 20px;
  text-align: center;
  color: #a89a86;
}

.empty-icon {
  margin-bottom: 14px;
  font-size: 64px;
  animation: float 3s ease-in-out infinite;
}

.empty-state p {
  font-size: 15px;
  color: #8a7a63;
}

@keyframes float {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-10px); }
}

/* ==================== 分页 ==================== */
.notes-pagination {
  justify-content: center;
  margin-top: 32px;
}

.notes-pagination :deep(.el-pagination__total) {
  color: #8a7a63;
}

.notes-pagination :deep(.el-pager li) {
  margin: 0 3px;
  border-radius: 10px;
  color: #8B4513;
  background: rgba(255, 255, 255, 0.65);
  transition: all 0.25s;
}

.notes-pagination :deep(.el-pager li.is-active) {
  color: #fff;
  background: linear-gradient(135deg, #d8b467, #c9a227);
  box-shadow: 0 4px 12px rgba(201, 162, 39, 0.3);
}

.notes-pagination :deep(.btn-prev),
.notes-pagination :deep(.btn-next) {
  border-radius: 10px;
  color: #8B4513;
  background: rgba(255, 255, 255, 0.65);
}

/* ==================== 弹窗（新建 / 编辑 / 查看） ==================== */
.notes-page :deep(.el-dialog) {
  border-radius: 20px;
  background: linear-gradient(180deg, #fffdf9, #fbf6ec);
  border: 1px solid rgba(216, 180, 103, 0.32);
  box-shadow: 0 24px 60px rgba(139, 69, 19, 0.18);
  overflow: hidden;
}

.notes-page :deep(.el-dialog__header) {
  margin: 0;
  padding: 20px 24px;
  border-bottom: 1px solid rgba(216, 180, 103, 0.28);
  background: linear-gradient(135deg, rgba(216, 180, 103, 0.16), rgba(216, 180, 103, 0.02));
}

.notes-page :deep(.el-dialog__title) {
  font-size: 19px;
  font-weight: 600;
  letter-spacing: 1px;
  color: #8B4513;
}

.notes-page :deep(.el-dialog__headerbtn .el-dialog__close) {
  color: #a8792f;
}

.notes-page :deep(.el-dialog__body) {
  padding: 24px;
  max-height: 70vh;
  overflow-y: auto;
  color: #4a3728;
}

/* 查看笔记内容 */
.note-view .view-meta {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
  font-size: 13px;
  color: #a89a86;
}

.note-view .view-content {
  line-height: 1.9;
  color: #4a3728;
  max-height: 60vh;
  overflow-y: auto;
  word-break: break-word;
}

.note-view :deep(img) {
  max-width: 100%;
  border-radius: 8px;
}

/* ==================== 响应式 ==================== */
@media (max-width: 768px) {
  .hero-section {
    padding: 44px 0 52px;
  }

  .hero-title {
    font-size: 34px;
  }

  .hero-stats {
    gap: 22px;
  }

  .stat-number {
    font-size: 23px;
  }

  .filter-glass {
    margin: -18px 0 24px;
    padding: 20px;
  }

  .filter-row {
    flex-direction: column;
    align-items: stretch;
  }

  .search-input,
  .type-select {
    width: 100%;
  }

  .query-btn {
    width: 100%;
  }

  .filter-actions {
    flex-direction: column;
    align-items: stretch;
  }

  .actions-left {
    flex-direction: column;
  }

  .actions-left :deep(.el-button) {
    margin-left: 0;
  }

  .grid {
    grid-template-columns: 1fr;
  }
}
</style>
