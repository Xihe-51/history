<template>
  <div class="notes-page">
    <top-component />

    <div class="container">
      <div class="page-header">
        <h1>📒 我的笔记</h1>
        <p>记录历史 · 关联人物 / 事件 / 典故</p>
      </div>

      <div class="stats">
        <div class="stat-card">
          <div class="num">{{ myNotes.length || 0 }}</div>
          <div class="label">总笔记</div>
        </div>
      </div>

      <div class="search-bar">
        <el-input v-model="searchKey" placeholder="搜索" @input="handleSearch" style="width:240px" />
        <el-select v-model="searchType" @change="handleSearch">
          <el-option label="全部" value="" />
          <el-option label="学习" value="study" />
          <el-option label="心得" value="insight" />
        </el-select>
        <el-button type="primary" @click="openAdd">➕新建笔记</el-button>
      </div>

      <div class="grid" v-loading="loading">
        <div 
          v-for="note in paginatedNotes" 
          :key="note.id" 
          class="card"
          @click="jumpToSource(note)"
        >
          <div class="card-title">{{ note.title }}</div>
          <div class="card-tags">
            <el-tag v-for="t in getTags(note.tags)" :key="t" size="small">{{ t }}</el-tag>
          </div>
          <div class="card-content" v-html="getShortContent(note.content)" />
          <div class="card-footer">
            <el-button size="small" @click.stop="openEdit(note.id)">编辑</el-button>
            <el-button size="small" type="danger" @click.stop="del(note.id)">删除</el-button>
          </div>
        </div>
      </div>

      <div v-if="myNotes.length === 0 && !loading" class="empty-state">
        <div class="empty-icon">📝</div>
        <p>暂无笔记，点击「新建笔记」开始记录</p>
      </div>

      <el-pagination
        v-if="myNotes.length > pageSize"
        v-model:current-page="currentPage"
        :page-size="pageSize"
        :total="myNotes.length"
        layout="prev, pager, next"
        @current-change="handlePageChange"
        style="text-align:center; margin-top:20px"
      />

      <el-dialog v-model="showEditor" width="90%" top="20px" title="笔记编辑">
        <NoteEditor :note-id="editId" @saved="reload" />
      </el-dialog>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'
import topComponent from '@/components/topComponent.vue'
import NoteEditor from '@/components/NoteEditor.vue'
import { getNotePage, deleteNote } from '@/api/note'
import type { NotePageItemVO } from '@/type/note'

const router = useRouter()

// 获取当前用户ID=
const getCurrentUserId = () => {
  const token = localStorage.getItem('token')
  if (!token) {
    console.log('未找到 token')
    return null
  }
  try {
    const base64Url = token.split('.')[1]
    const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/')
    const payload = JSON.parse(decodeURIComponent(atob(base64).split('').map(c => {
      return '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2)
    }).join('')))
    
    console.log('解析 token 内容:', payload)
    const userId = payload.userId || payload.user_id || payload.id
    console.log('获取到的用户ID:', userId)
    return userId
  } catch (e) {
    console.error('解析 token 失败:', e)
    return null
  }
}

const currentUserId = ref(getCurrentUserId())
console.log('当前用户ID:', currentUserId.value)

// 数据状态
const allNotes = ref<NotePageItemVO[]>([])
const loading = ref(false)
const searchKey = ref('')
const searchType = ref('')
const showEditor = ref(false)
const editId = ref<number | null>(null)

// 分页相关
const currentPage = ref(1)
const pageSize = ref(9)

// 辅助函数
const getTags = (tags: any) => {
  if (!tags) return []
  if (Array.isArray(tags)) return tags
  if (typeof tags === 'string') {
    try {
      return JSON.parse(tags)
    } catch {
      return tags.split(',').filter(t => t.trim())
    }
  }
  return []
}

const getShortContent = (content: string) => {
  if (!content) return '暂无内容'
  if (content.length <= 120) return content
  return content.substring(0, 120) + '...'
}

// 过滤当前用户的笔记
const currentUserNotes = computed(() => {
  const uid = currentUserId.value
  if (!uid) return []
  
  return allNotes.value.filter(note => {
    const noteUserId = note.userId || note.userId
    return noteUserId === uid
  })
})

const filteredNotes = computed(() => {
  let notes = [...currentUserNotes.value]
  
  if (searchKey.value.trim()) {
    const kw = searchKey.value.toLowerCase()
    notes = notes.filter(note => 
      note.title?.toLowerCase().includes(kw) || 
      note.content?.toLowerCase().includes(kw)
    )
  }
  
  if (searchType.value) {
    notes = notes.filter(note => note.type === searchType.value)
  }
  
  return notes
})

const myNotes = computed(() => filteredNotes.value)

const paginatedNotes = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  return myNotes.value.slice(start, start + pageSize.value)
})

// 数据加载 
const loadAllNotes = async () => {
  if (!currentUserId.value) {
    console.log('用户未登录，无法加载笔记')
    ElMessage.warning('请先登录')
    return
  }

  loading.value = true
  try {
    const res = await getNotePage({
      pageNum: 1,
      pageSize: 1000
    })
    
    console.log('笔记接口返回:', res)
    
    if (res.data.code === 200) {
      allNotes.value = res.data.data.records || []
      console.log('加载笔记成功，共', allNotes.value.length, '条')
      console.log('当前用户ID:', currentUserId.value)
      console.log('当前用户笔记数:', currentUserNotes.value.length)
      
      // 打印 userId 分布
      const userIdCount: Record<number, number> = {}
      allNotes.value.forEach(note => {
        const uid = note.userId || note.userId
        userIdCount[uid] = (userIdCount[uid] || 0) + 1
      })
      console.log('笔记 userId 分布:', userIdCount)
      
      if (currentUserNotes.value.length === 0 && allNotes.value.length > 0) {
        const allUserIds = [...new Set(allNotes.value.map(n => n.userId || n.userId))]
        console.log('当前用户没有笔记，所有笔记的 userId:', allUserIds)
      }
    } else {
      ElMessage.error(res.data.msg || '加载失败')
    }
  } catch (err) {
    console.error('加载笔记失败:', err)
    ElMessage.error('加载失败')
  } finally {
    loading.value = false
  }
}

const refresh = () => {
  loadAllNotes()
}

const reload = () => {
  showEditor.value = false
  loadAllNotes()
}

// 搜索和筛选
const handleSearch = () => {
  currentPage.value = 1
}

const handlePageChange = (page: number) => {
  currentPage.value = page
}

// 增删改操作
const openAdd = () => {
  if (!currentUserId.value) {
    ElMessage.warning('请先登录')
    return
  }
  editId.value = null
  showEditor.value = true
}

const openEdit = (id: number) => {
  editId.value = id
  showEditor.value = true
}

const del = async (id: number) => {
  try {
    await deleteNote(id)
    ElMessage.success('删除成功')
    loadAllNotes()
  } catch (err) {
    console.error('删除失败:', err)
    ElMessage.error('删除失败')
  }
}

// 跳转函数
const jumpToSource = (note: NotePageItemVO) => {
  // 使用可选链，兼容多种字段名
  const relateType = note.relateType || note.relatedType
  const relateId = note.relateId || note.relatedId
  
  if (relateType === 'person' && relateId) {
    router.push({ path: '/person', query: { id: String(relateId) } })
  } else if (relateType === 'event' && relateId) {
    router.push({ path: '/event', query: { id: String(relateId) } })
  } else if (relateType === 'allusion' && relateId) {
    router.push({ path: '/allusion', query: { id: String(relateId) } })
  } else {
    ElMessage.info('该笔记未关联内容')
  }
}
//生命周期 
onMounted(() => {
  const userId = getCurrentUserId()
  if (userId !== currentUserId.value) {
    currentUserId.value = userId
  }
  loadAllNotes()
})
</script>

<style scoped>
.notes-page { 
  min-height: 100vh; 
  background: linear-gradient(135deg, #f5f0e8 0%, #e8e0d5 100%);
}

.container { 
  max-width: 1200px; 
  margin: 0 auto; 
  padding: 30px 20px; 
}

.page-header { 
  text-align: center; 
  margin-bottom: 30px;
}

.page-header h1 { 
  color: #8B4513; 
  font-size: 32px;
  margin-bottom: 8px;
}

.page-header p { 
  color: #666; 
  font-size: 14px;
}

.stats { 
  display: flex; 
  gap: 16px; 
  margin-bottom: 24px; 
}

.stat-card { 
  background: #fff; 
  border-radius: 16px; 
  padding: 20px; 
  text-align: center; 
  flex: 1;
  box-shadow: 0 2px 8px rgba(0,0,0,0.05);
}

.num { 
  font-size: 32px; 
  font-weight: bold; 
  color: #8B4513; 
}

.label { 
  color: #666; 
  margin-top: 6px; 
  font-size: 13px;
}

.search-bar { 
  display: flex; 
  gap: 12px; 
  margin-bottom: 24px; 
  flex-wrap: wrap;
  align-items: center;
}

.grid { 
  display: grid; 
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr)); 
  gap: 20px; 
}

.card { 
  background: white; 
  border-radius: 16px; 
  padding: 20px; 
  cursor: pointer; 
  transition: all 0.3s;
  box-shadow: 0 2px 8px rgba(0,0,0,0.05);
}

.card:hover { 
  transform: translateY(-4px); 
  box-shadow: 0 12px 24px rgba(0,0,0,0.1); 
}

.card-title { 
  font-size: 18px; 
  font-weight: bold; 
  color: #8B4513; 
  margin-bottom: 10px; 
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.card-tags { 
  margin-bottom: 12px; 
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.card-content { 
  font-size: 13px; 
  color: #666; 
  line-height: 1.6; 
  margin-bottom: 16px;
  word-break: break-word;
}

.card-footer { 
  display: flex; 
  gap: 12px; 
  justify-content: flex-end;
  padding-top: 12px;
  border-top: 1px solid #eee;
}

.empty-state {
  text-align: center;
  padding: 60px 20px;
  color: #999;
}

.empty-icon {
  font-size: 64px;
  margin-bottom: 16px;
}

/* 响应式 */
@media (max-width: 768px) {
  .grid {
    grid-template-columns: 1fr;
  }
  
  .search-bar {
    flex-direction: column;
    align-items: stretch;
  }
  
  .search-bar .el-input,
  .search-bar .el-select {
    width: 100% !important;
  }
}
</style>