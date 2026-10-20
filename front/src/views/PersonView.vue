<template>
  <div class="person-page">
    <!-- 顶部导航栏 -->
    <top-component 
      ref="topNavRef" 
      class="top-nav"
      :class="{ 'nav-active': activeDynastyIndex !== -1, 'nav-bounce': isNavBouncing }"
    />
    
    <div class="person-container">
      <!-- 页面标题区 -->
      <div class="hero-section">
        <div class="hero-content">
          <h1 class="hero-title">👥 人物科普</h1>
          <p class="hero-subtitle">探寻历史人物 · 感受先贤风采</p>
          <div class="hero-stats">
            <div class="stat-item">
              <span class="stat-number">{{ allPersons.length }}</span>
              <span class="stat-label">历史人物</span>
            </div>
            <div class="stat-item">
              <span class="stat-number">{{ dynastyList.length }}</span>
              <span class="stat-label">涵盖朝代</span>
            </div>
            <div class="stat-item">
              <span class="stat-number">{{ totalEvents }}</span>
              <span class="stat-label">关联事件</span>
            </div>
          </div>
        </div>
        <div class="hero-wave"></div>
      </div>

      <!-- 筛选区域 -->
      <div class="filter-glass">
        <div class="filter-header">
          <span class="filter-icon">🔍</span>
          <span class="filter-title">智能筛选</span>
          <button class="reset-btn" @click="resetFilters">重置筛选</button>
        </div>
        
        <div class="filter-grid">
          <div class="filter-card">
            <div class="filter-label">📅 朝代</div>
            <div class="tag-group">
              <span v-for="dynasty in dynastyList" :key="dynasty.id"
                class="filter-tag"
                :class="{ active: filters.dynastyId === dynasty.id }"
                @click="toggleDynastyFilter(dynasty.id)">
                {{ dynasty.name }}
              </span>
            </div>
          </div>

          <div class="filter-card">
            <div class="filter-label">📆 生卒年区间</div>
            <div class="range-slider-container">
              <div class="range-values">
                <span>公元前</span>
                <input type="range" v-model.number="filters.birthMin" :min="-500" :max="0" step="50" class="range-slider">
                <span>{{ Math.abs(filters.birthMin) }}年</span>
              </div>
              <div class="range-values">
                <span>公元后</span>
                <input type="range" v-model.number="filters.birthMax" :min="0" :max="1900" step="50" class="range-slider">
                <span>{{ filters.birthMax }}年</span>
              </div>
            </div>
          </div>
        </div>

        <div class="filter-actions">
          <div class="search-wrapper">
            <input type="text" v-model="filters.keyword" placeholder="搜索人物姓名..." @input="handleSearch" class="search-input-modern">
            <span class="search-icon-modern">🔍</span>
            <button v-if="filters.keyword" class="clear-search" @click="clearSearch">✕</button>
          </div>

          <div class="sort-view-group">
            <div class="sort-selector">
              <span class="sort-label">排序</span>
              <div class="sort-buttons">
                <button class="sort-btn" :class="{ active: filters.sortBy === 'year' }" @click="filters.sortBy = 'year'">📅 生卒年</button>
                <button class="sort-btn" :class="{ active: filters.sortBy === 'name' }" @click="filters.sortBy = 'name'">🔤 姓名</button>
              </div>
            </div>
            <div class="view-toggle">
              <button class="view-btn" :class="{ active: viewMode === 'grid' }" @click="viewMode = 'grid'">🔲 网格</button>
              <button class="view-btn" :class="{ active: viewMode === 'list' }" @click="viewMode = 'list'">📋 列表</button>
            </div>
          </div>
        </div>
      </div>

      <div class="result-stats">
        <span>共找到 <strong>{{ filteredPersonList.length }}</strong> 位历史人物</span>
        <span v-if="filters.keyword" class="keyword-badge">关键词：{{ filters.keyword }}</span>
      </div>

      <!-- 网格视图 -->
      <div v-if="viewMode === 'grid'" class="person-grid">
        <div v-for="person in paginatedPersonList" :key="person.id" class="person-card-modern" @click="openPersonDetail(person.id)">
          <div class="card-glow"></div>
          <div class="card-header">
            <div class="person-avatar-modern">
              <img v-if="person.avatarPath" :src="getAvatarUrl(person.avatarPath)" :alt="person.personName" class="avatar-image" @error="handleImageError(person)">
              <div v-else class="avatar-inner">{{ getPersonInitial(person.personName) }}</div>
              <div class="avatar-ring"></div>
            </div>
            <div class="person-badge" :class="getTagClass(getFirstTag(person.tags))">{{ getFirstTag(person.tags) || '历史人物' }}</div>
          </div>
          <div class="card-body">
            <h3 class="person-name-modern" v-html="highlightText(person.personName, filters.keyword)"></h3>
            <div class="person-meta">
              <span class="meta-dynasty">{{ getDynastyName(person.dynastyId) }}</span>
              <span class="meta-year">{{ formatYear(person.birthYear) }} - {{ formatYear(person.deathYear) }}</span>
            </div>
            <div class="person-tags-modern">
              <span v-for="tag in getTagArray(person.tags).slice(0, 3)" :key="tag" class="tag-modern" :class="getTagClass(tag)">{{ tag }}</span>
            </div>
            <div class="person-stats-modern">
              <div class="stat-chip"><span class="stat-icon">📖</span><span>{{ person.eventCount || 0 }} 事件</span></div>
            </div>
          </div>
          <div class="card-footer"><span class="view-detail">查看详情 →</span></div>
        </div>
      </div>

      <!-- 列表视图 -->
      <div v-else class="person-list-modern">
        <div class="list-header">
          <div class="list-col col-avatar">头像</div>
          <div class="list-col col-name">姓名</div>
          <div class="list-col col-dynasty">朝代</div>
          <div class="list-col col-year">生卒年</div>
          <div class="list-col col-tags">标签</div>
          <div class="list-col col-events">事件</div>
          <div class="list-col col-action">操作</div>
        </div>
        <div v-for="person in paginatedPersonList" :key="person.id" class="list-row" @click="openPersonDetail(person.id)">
          <div class="list-col col-avatar">
            <div class="list-avatar">
              <img v-if="person.avatarPath" :src="getAvatarUrl(person.avatarPath)" :alt="person.personName" class="list-avatar-img" @error="handleImageError(person)">
              <div v-else class="list-avatar-inner">{{ getPersonInitial(person.personName) }}</div>
            </div>
          </div>
          <div class="list-col col-name">
            <span class="name-highlight-modern" v-html="highlightText(person.personName, filters.keyword)"></span>
          </div>
          <div class="list-col col-dynasty">{{ getDynastyName(person.dynastyId) }}</div>
          <div class="list-col col-year">{{ formatYear(person.birthYear) }} - {{ formatYear(person.deathYear) }}</div>
          <div class="list-col col-tags">
            <div class="tags-inline-modern">
              <span v-for="tag in getTagArray(person.tags).slice(0, 2)" :key="tag" class="tag-small-modern" :class="getTagClass(tag)">{{ tag }}</span>
              <span v-if="getTagArray(person.tags).length > 2" class="tag-more">+{{ getTagArray(person.tags).length - 2 }}</span>
            </div>
          </div>
          <div class="list-col col-events">{{ person.eventCount || 0 }}</div>
          <div class="list-col col-action"><button class="detail-btn-modern">详情</button></div>
        </div>
      </div>

      <!-- 分页 -->
      <div class="pagination-modern" v-if="filteredPersonList.length > pageSize">
        <button class="page-btn" :disabled="currentPage === 1" @click="goToPage(currentPage - 1)">‹</button>
        <span class="page-info">{{ currentPage }} / {{ totalPages }}</span>
        <button class="page-btn" :disabled="currentPage === totalPages" @click="goToPage(currentPage + 1)">›</button>
      </div>

      <!-- 空状态 -->
      <div v-if="filteredPersonList.length === 0 && !loading" class="empty-state-modern">
        <div class="empty-animation">📜</div>
        <h3>暂无相关人物</h3>
        <p>试试调整筛选条件或搜索其他关键词</p>
        <button class="empty-reset-btn" @click="resetFilters">重置筛选</button>
      </div>
    </div>

    <!-- 人物详情弹窗 -->
    <el-dialog 
      v-model="showDetailDialog" 
      :title="currentPerson?.personName || ''" 
      width="900px" 
      style="background: linear-gradient(135deg, #1a1a1a 0%, #2d2d2d 100%);"
      class="person-detail-dialog-modern" 
      :close-on-click-modal="false"
      :show-close="true"
    >
      <div class="person-detail-modern" v-if="currentPerson && currentPerson.id">
        <div class="detail-hero-card">
          <div class="detail-avatar-large">
            <img 
              v-if="currentPerson.avatarPath" 
              :src="getAvatarUrl(currentPerson.avatarPath)" 
              :alt="currentPerson.personName"
              class="avatar-large-image"
              @error="handleImageError(currentPerson)"
            >
            <div v-else class="avatar-large-inner">{{ getPersonInitial(currentPerson.personName) }}</div>
            <div class="avatar-glow"></div>
          </div>
          <div class="detail-info-large">
            <h2 class="detail-name">{{ currentPerson.personName }}</h2>
            <div class="detail-basic">
              <span class="detail-badge dynasty-badge">🏛️ {{ getDynastyName(currentPerson.dynastyId) }}</span>
              <span class="detail-badge year-badge">📅 {{ formatYear(currentPerson.birthYear) }} - {{ formatYear(currentPerson.deathYear) }}</span>
              <span class="detail-badge role-badge" v-if="currentPerson.identityType">👤 {{ currentPerson.identityType }}</span>
            </div>
            <div class="detail-tags-modern">
              <span v-for="tag in getTagArray(currentPerson.tags)" :key="tag" class="tag-detail-modern" :class="getTagClass(tag)">{{ tag }}</span>
            </div>
          </div>
        </div>

        <div class="bio-section">
          <div class="section-title">
            <span class="title-icon">📜</span>
            <h3>人物生平</h3>
          </div>
          <div class="bio-content" v-html="currentPerson.personBio || '暂无生平介绍'"></div>
        </div>

        <!--AI助手按钮 -->
        <div class="ai-assistant-section">
          <button class="ai-assistant-btn" @click="openAIAssistant">
            <span class="ai-icon">🤖</span>
            <span>AI历史助手</span>
            <span class="ai-hint">关于{{ currentPerson.personName }}的问题，随时问我</span>
          </button>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { ElMessage } from 'element-plus';
import topComponent from '@/components/topComponent.vue';
import request, { resolveAssetUrl } from '@/utils/request';

const router = useRouter();
const route = useRoute();

// 导航栏状态
const activeDynastyIndex = ref(-1);
const isNavBouncing = ref(false);

// 筛选条件
const filters = ref({
  dynastyId: '',
  tag: '',
  birthMin: -2100,
  birthMax: 1900,
  keyword: '',
  sortBy: 'year'
});

const viewMode = ref('grid');
const currentPage = ref(1);
const pageSize = ref(9);

const allPersons = ref([]);
const loading = ref(false);
const showDetailDialog = ref(false);
const currentPerson = ref(null);

// 朝代列表
const dynastyList = ref([
  { id: 1, name: "夏朝" }, { id: 2, name: "商朝" }, { id: 3, name: "周朝" },
  { id: 4, name: "秦朝" }, { id: 5, name: "汉朝" }, { id: 6, name: "三国" },
  { id: 7, name: "晋朝" }, { id: 8, name: "南北朝" }, { id: 9, name: "隋朝" },
  { id: 10, name: "唐朝" }, { id: 11, name: "宋朝" }, { id: 12, name: "元朝" },
  { id: 13, name: "明朝" }, { id: 14, name: "清朝" }
]);
const dynastyMap = ref({});

// 统计数据
const totalEvents = computed(() =>
  allPersons.value.reduce((sum, p) => sum + (p.eventCount || 0), 0)
);

// 标签列表
const tagList = ref(['政治家', '军事家', '文学家', '思想家', '科学家', '艺术家', '改革家', '诗人', '书法家', '帝王', '民族英雄', '发明家']);

function getTagArray(tags) {
  if (!tags) return [];
  if (Array.isArray(tags)) return tags;
  if (typeof tags === 'string') {
    try {
      // 能解析JSON数组就解析
      return JSON.parse(tags.replace(/'/g, '"'));
    } catch (e) {}
    // 否则按逗号分割
    return tags.split(',').map(t => t.trim()).filter(Boolean);
  }
  return [];
}

function getFirstTag(tagsStr) {
  const tags = getTagArray(tagsStr);
  return tags.length > 0 ? tags[0] : '';
}

const filteredPersonList = computed(() => {
  const f = filters.value;
  let list = [...allPersons.value];

  // 1.朝代
  if (f.dynastyId) {
    list = list.filter(p => p.dynastyId == f.dynastyId);
  }

  // 2.标签筛选
  if (f.tag) {
    list = list.filter(p => {
      const t = getTagArray(p.tags);
      return t.includes(f.tag);
    });
  }

  // 3.年份
  list = list.filter(p => {
    const y = p.birthYear || 0;
    return y >= f.birthMin && y <= f.birthMax;
  });

  // 4.关键词
  if (f.keyword) {
    const kw = f.keyword.toLowerCase();
    list = list.filter(p => {
      const name = (p.personName || '').toLowerCase();
      const tags = getTagArray(p.tags).map(t => t.toLowerCase());
      return name.includes(kw) || tags.some(t => t.includes(kw));
    });
  }

  // 排序
  if (f.sortBy === 'year') {
    list.sort((a, b) => (a.birthYear || 0) - (b.birthYear || 0));
  } else {
    list.sort((a, b) => (a.personName || '').localeCompare(b.personName || ''));
  }

  return list;
});

// 分页
const totalPages = computed(() => {
  const len = filteredPersonList.value.length;
  return len === 0 ? 1 : Math.ceil(len / pageSize.value);
});

const paginatedPersonList = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value;
  return filteredPersonList.value.slice(start, start + pageSize.value);
});

function getPersonInitial(name) {
  return name ? name.charAt(0) : '?';
}

function formatYear(year) {
  if (!year && year !== 0) return '不详';
  return year < 0 ? `公元前${Math.abs(year)}年` : `公元${year}年`;
}

function highlightText(text, keyword) {
  if (!keyword) return text;
  const reg = new RegExp(`(${keyword})`, 'gi');
  return text.replace(reg, '<mark class="highlight">$1</mark>');
}

function getTagClass(tag) {
  const map = {
    '政治家':'tag-political','军事家':'tag-military','文学家':'tag-literary',
    '思想家':'tag-thinker','科学家':'tag-science','艺术家':'tag-art',
    '改革家':'tag-reform','诗人':'tag-poet','书法家':'tag-calligraphy',
    '发明家':'tag-inventor','帝王':'tag-emperor','民族英雄':'tag-hero'
  };
  return map[tag] || 'tag-default';
}

function getDynastyName(id) {
  if (!id) return '';
  return dynastyMap.value[id]?.name || '';
}

function getAvatarUrl(path) {
  if (!path) return '';
  return resolveAssetUrl(path);
}

function handleImageError(p) {
  p.avatarPath = null;
}

// 筛选操作 
function toggleDynastyFilter(id) {
  filters.value.dynastyId = filters.value.dynastyId == id ? '' : id;
  currentPage.value = 1;
}

function toggleTagFilter(tag) {
  filters.value.tag = filters.value.tag === tag ? '' : tag;
  currentPage.value = 1;
}

function clearSearch() {
  filters.value.keyword = '';
}

function resetFilters() {
  filters.value = {
    dynastyId: '',
    tag: '',
    birthMin: -2100,
    birthMax: 1900,
    keyword: '',
    sortBy: 'year'
  };
  currentPage.value = 1;
}

function handleSearch() {
  currentPage.value = 1;
}

function goToPage(page) {
  if (page >= 1 && page <= totalPages.value) {
    currentPage.value = page;
  }
}

// 数据加载 
let isLoadingData = false;

/** 拉取全部事件的正文合并文本，用于统计人物关联事件数 */
async function loadEventTexts() {
  const dynastyIds = dynastyList.value.map(d => d.id);
  const results = await Promise.all(dynastyIds.map(id =>
    request.get('/historical-event/page', {
      params: { pageNum: 1, pageSize: 1000, dynastyId: id }
    }).catch(() => null)
  ));
  const texts = [];
  results.forEach(res => {
    if (res?.data?.code === 200) {
      (res.data.data?.records || []).forEach(e => {
        texts.push(`${e.eventName || ''}${e.eventBg || ''}${e.eventProcess || ''}${e.eventImpact || ''}`);
      });
    }
  });
  return texts;
}

/** 真实“关联事件”口径：事件正文中出现该人物姓名即算一条（库中无人物-事件关联表） */
function countEventsMentioning(texts, name) {
  // 单字姓名易被普通词语命中（如“启”命中“开启”），只统计两字及以上姓名
  if (!name || name.length < 2) return 0;
  return texts.filter(t => t.includes(name)).length;
}

async function loadPersonList() {
  if (isLoadingData) return;
  isLoadingData = true;
  loading.value = true;

  try {
    const dynastyIds = dynastyList.value.map(d => d.id);
    const promises = dynastyIds.map(id =>
      request.get('/historical-person/page', {
        params: { pageNum: 1, pageSize: 1000, dynastyId: id }
      }).catch(() => null)
    );

    const results = await Promise.all(promises);
    let personsData = [];

    // 先取全部事件正文，再按“正文是否提到该人物”统计真实关联事件数
    const eventTexts = await loadEventTexts();

    results.forEach(res => {
      if (res?.data?.code === 200) {
        const records = res.data.data?.records || [];
        const withEventCount = records.map(p => ({
          ...p,
          eventCount: p.eventCount ?? countEventsMentioning(eventTexts, p.personName)
        }));
        personsData = personsData.concat(withEventCount);
      }
    });

    allPersons.value = personsData;
    console.log('✅加载完成，总人数：', allPersons.value.length);
  } catch (err) {
    ElMessage.error('加载人物失败');
    console.error(err);
  } finally {
    loading.value = false;
    isLoadingData = false;
  }
}

// 人物详情
async function loadPersonDetail(id) {
  try {
    const res = await request.get(`/historical-person/${id}`);
    if (res.data.code === 200) {
      currentPerson.value = res.data.data;
      return true;
    }
  } catch (e) {
    ElMessage.error('详情加载失败');
  }
  return false;
}

async function openPersonDetail(id) {
  const ok = await loadPersonDetail(id);
  if (ok) {
    showDetailDialog.value = true;
  }
}

function jumpToEvent(id) {
  showDetailDialog.value = false;
  router.push({ path: '/event', query: { id } });
}

// 打开AI助手
function openAIAssistant() {
  if (!currentPerson.value) return;
  router.push({
    path: '/ai-assistant',
    query: {
      context: 'person',
      name: currentPerson.value.personName,
      id: currentPerson.value.id
    }
  });
  ElMessage.success('正在打开 AI 历史助手');
}

// 监听筛选变化
watch(filters, () => {
  currentPage.value = 1;
}, { deep: true });

onMounted(async () => {
  dynastyList.value.forEach(d => {
    dynastyMap.value[d.id] = d;
  });
  // 支持从典故页“同朝人物 → 查看详情”带 keyword 跳转过来，直接定位该人物
  if (route.query.keyword) {
    filters.value.keyword = String(route.query.keyword);
  }
  await loadPersonList();
});
</script>


<style scoped>
.person-page {
  min-height: 100vh;
  background: linear-gradient(135deg, #0a0a1a 0%, #1a1a2e 100%);
  overflow-x: hidden;
}

.ai-assistant-section {
  margin-top: 24px;
  padding-top: 20px;
  border-top: 1px solid rgba(255, 215, 0, 0.2);
  text-align: center;
}

.ai-assistant-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  width: 100%;
  padding: 14px 24px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
  border-radius: 40px;
  color: white;
  font-size: 16px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 4px 15px rgba(102, 126, 234, 0.3);
}

.ai-assistant-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(102, 126, 234, 0.4);
}

.ai-icon {
  font-size: 24px;
}

.ai-hint {
  font-size: 12px;
  opacity: 0.8;
  margin-left: 8px;
}


.person-container {
  
  max-width: 1400px;
  margin: 0 auto;
  padding: 20px;
}

.hero-section {
  height: 323px;
  position: relative;
  padding: 100px 0 60px;
  text-align: center;
  background: linear-gradient(135deg, rgba(255,215,0,0.1) 0%, rgba(0,0,0,0) 100%);
}

.hero-title {
  font-size: 56px;
  font-weight: 700;
  background: linear-gradient(135deg, #FFD700, #FFA500);
  -webkit-background-clip: text;
  background-clip: text;
  color: transparent;
  margin-bottom: 16px;
}

.hero-subtitle {
  font-size: 18px;
  color: rgba(255,255,255,0.7);
  letter-spacing: 2px;
  margin-bottom: 30px;
}

.hero-stats {
  display: flex;
  justify-content: center;
  gap: 48px;
}

.stat-number {
  display: block;
  font-size: 32px;
  font-weight: bold;
  color: #FFD700;
}

.stat-label {
  font-size: 14px;
  color: rgba(255,255,255,0.6);
}

.hero-wave {
  position: absolute;
  bottom: -2px;
  left: 0;
  width: 100%;
  height: 60px;
  background: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 1440 320'%3E%3Cpath fill='%231a1a2e' fill-opacity='1' d='M0,96L48,112C96,128,192,160,288,160C384,160,480,128,576,122.7C672,117,768,139,864,154.7C960,171,1056,181,1152,165.3C1248,149,1344,107,1392,85.3L1440,64L1440,320L1392,320C1344,320,1248,320,1152,320C1056,320,960,320,864,320C768,320,672,320,576,320C480,320,384,320,288,320C192,320,96,320,48,320L0,320Z'%3E%3C/path%3E%3C/svg%3E") no-repeat bottom;
  background-size: cover;
}

.filter-glass {
  max-width: 1200px;
  margin: -30px auto 30px;
  background: rgba(255,255,255,0.05);
  backdrop-filter: blur(12px);
  border-radius: 28px;
  padding: 24px 30px;
  border: 1px solid rgba(255,215,0,0.2);
}

.filter-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 24px;
  padding-bottom: 16px;
  border-bottom: 1px solid rgba(255,215,0,0.2);
}

.filter-icon {
  font-size: 24px;
}

.filter-title {
  font-size: 18px;
  font-weight: 500;
  color: #FFD700;
  flex: 1;
}

.reset-btn {
  background: rgba(255,215,0,0.15);
  border: 1px solid rgba(255,215,0,0.3);
  border-radius: 20px;
  padding: 6px 16px;
  color: #FFD700;
  cursor: pointer;
  transition: all 0.3s;
}

.reset-btn:hover {
  background: rgba(255,215,0,0.3);
  transform: translateY(-2px);
}

.filter-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: 24px;
  margin-bottom: 24px;
}

.filter-card {
  background: rgba(0,0,0,0.2);
  border-radius: 20px;
  padding: 16px;
}

.filter-label {
  font-size: 14px;
  color: #FFD700;
  margin-bottom: 12px;
  font-weight: 500;
}

.tag-group {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.filter-tag {
  padding: 6px 14px;
  background: rgba(255,255,255,0.08);
  border-radius: 20px;
  font-size: 13px;
  color: #ccc;
  cursor: pointer;
  transition: all 0.3s;
}

.filter-tag:hover {
  background: rgba(255,215,0,0.2);
  transform: translateY(-2px);
}

.filter-tag.active {
  background: #FFD700;
  color: #1a1a1a;
}

.range-slider-container {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.range-values {
  display: flex;
  align-items: center;
  gap: 12px;
  color: #ccc;
  font-size: 13px;
}

.range-slider {
  flex: 1;
  height: 4px;
  -webkit-appearance: none;
  background: rgba(255,255,255,0.2);
  border-radius: 2px;
}

.range-slider::-webkit-slider-thumb {
  -webkit-appearance: none;
  width: 16px;
  height: 16px;
  background: #FFD700;
  border-radius: 50%;
  cursor: pointer;
  box-shadow: 0 0 8px #FFD700;
}

.filter-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 20px;
  align-items: center;
  justify-content: space-between;
}

.search-wrapper {
  flex: 2;
  position: relative;
  min-width: 250px;
}

.search-input-modern {
  width: 650px;
  padding: 12px 48px 12px 48px;
  background: rgba(0,0,0,0.4);
  border: 1px solid rgba(255,215,0,0.3);
  border-radius: 40px;
  color: #fff;
  font-size: 14px;
}

.search-input-modern:focus {
  outline: none;
  border-color: #FFD700;
  box-shadow: 0 0 15px rgba(255,215,0,0.2);
}

.search-icon-modern {
  position: absolute;
  left: 18px;
  top: 50%;
  transform: translateY(-50%);
  color: #FFD700;
}

.clear-search {
  position: absolute;
  right: 18px;
  top: 50%;
  transform: translateY(-50%);
  background: none;
  border: none;
  color: #aaa;
  cursor: pointer;
}

.sort-view-group {
  display: flex;
  gap: 20px;
  align-items: center;
}

.sort-selector {
  display: flex;
  align-items: center;
  gap: 10px;
}

.sort-label {
  color: #FFD700;
  font-size: 13px;
}

.sort-buttons {
  display: flex;
  gap: 6px;
}

.sort-btn {
  padding: 6px 12px;
  background: rgba(255,255,255,0.08);
  border: none;
  border-radius: 20px;
  color: #ccc;
  cursor: pointer;
  transition: all 0.3s;
  font-size: 12px;
}

.sort-btn:hover,
.sort-btn.active {
  background: #FFD700;
  color: #1a1a1a;
}

.view-toggle {
  display: flex;
  gap: 6px;
}

.view-btn {
  padding: 6px 12px;
  background: rgba(255,255,255,0.08);
  border: none;
  border-radius: 20px;
  color: #ccc;
  cursor: pointer;
  transition: all 0.3s;
  font-size: 12px;
}

.view-btn:hover,
.view-btn.active {
  background: #FFD700;
  color: #1a1a1a;
}

.result-stats {
  max-width: 1200px;
  margin: 20px auto;
  padding: 0 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  color: #aaa;
  font-size: 14px;
}

.keyword-badge {
  background: rgba(255,215,0,0.2);
  padding: 4px 12px;
  border-radius: 20px;
  color: #FFD700;
}


.person-grid {
  max-width: 1200px;
  margin: 0 auto;
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 24px;
  padding: 0 20px;
}

.person-card-modern {
  position: relative;
  background: rgba(25, 25, 35, 0.85);
  backdrop-filter: blur(8px);
  border-radius: 24px;
  padding: 24px;
  cursor: pointer;
  transition: all 0.4s;
  border: 1px solid rgba(255,215,0,0.2);
  overflow: hidden;
}

.person-card-modern:hover {
  transform: translateY(-8px);
  border-color: #FFD700;
  box-shadow: 0 20px 40px rgba(0,0,0,0.4);
  background: rgba(35, 35, 45, 0.9);
}

.card-glow {
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255,215,0,0.1), transparent);
  transition: left 0.6s;
}

.person-card-modern:hover .card-glow {
  left: 100%;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 16px;
}

.person-avatar-modern {
  position: relative;
  width: 70px;
  height: 70px;
}

.avatar-image {
  width: 100%;
  height: 100%;
  border-radius: 50%;
  object-fit: cover;
  background: linear-gradient(135deg, #FFD700, #FFA500);
  position: relative;
  z-index: 1;
}

.avatar-inner {
  width: 70px;
  height: 70px;
  background: linear-gradient(135deg, #FFD700, #FFA500);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 32px;
  font-weight: bold;
  color: #1a1a1a;
  position: relative;
  z-index: 1;
}

.avatar-ring {
  position: absolute;
  top: -3px;
  left: -3px;
  right: -3px;
  bottom: -3px;
  border-radius: 50%;
  border: 2px solid rgba(255,215,0,0.5);
  animation: pulse 2s infinite;
}

@keyframes pulse {
  0%, 100% {
    opacity: 0.5;
    transform: scale(1);
  }
  50% {
    opacity: 1;
    transform: scale(1.05);
  }
}

.person-badge {
  padding: 4px 12px;
  border-radius: 20px;
  font-size: 11px;
  font-weight: 500;
}

.card-body {
  margin-bottom: 20px;
}

.person-name-modern {
  font-size: 22px;
  font-weight: bold;
  color: #fff;
  margin-bottom: 8px;
}

.person-meta {
  display: flex;
  gap: 12px;
  font-size: 13px;
  color: #aaa;
  margin-bottom: 12px;
}

.meta-dynasty {
  color: #FFD700;
}

.person-tags-modern {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  margin-bottom: 12px;
}

.tag-modern {
  padding: 3px 10px;
  border-radius: 20px;
  font-size: 10px;
}

.person-stats-modern {
  margin-top: 8px;
}

.stat-chip {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 4px 10px;
  background: rgba(0,0,0,0.3);
  border-radius: 20px;
  font-size: 12px;
  color: #FFD700;
}

.card-footer {
  text-align: right;
  border-top: 1px solid rgba(255,255,255,0.1);
  padding-top: 16px;
}

.view-detail {
  color: #FFD700;
  font-size: 13px;
  transition: all 0.3s;
}

.person-card-modern:hover .view-detail {
  transform: translateX(5px);
  display: inline-block;
}

/* 标签颜色 */
.tag-political,
.tag-hero {
  background: rgba(255,77,77,0.3);
  color: #FF4D4D;
}

.tag-military {
  background: rgba(255,165,0,0.3);
  color: #FFA500;
}

.tag-literary,
.tag-poet {
  background: rgba(76,175,80,0.3);
  color: #4CAF50;
}

.tag-thinker {
  background: rgba(33,150,243,0.3);
  color: #2196F3;
}

.tag-science,
.tag-inventor {
  background: rgba(156,39,176,0.3);
  color: #9C27B0;
}

.tag-art,
.tag-calligraphy {
  background: rgba(233,30,99,0.3);
  color: #E91E63;
}

.tag-reform {
  background: rgba(0,188,212,0.3);
  color: #00BCD4;
}

.tag-education {
  background: rgba(255,152,0,0.3);
  color: #FF9800;
}

.tag-default {
  background: rgba(158,158,158,0.3);
  color: #9E9E9E;
}

.person-list-modern {
  color: white;
  max-width: 1200px;
  margin: 0 auto;
  background: rgba(0,0,0,0.3);
  border-radius: 24px;
  overflow: hidden;
  padding: 0 20px;
}

.list-header {
  display: grid;
  grid-template-columns: 0.6fr 1.8fr 1fr 1.5fr 1.8fr 0.8fr 0.8fr;
  padding: 16px;
  background: rgba(0,0,0,0.4);
  border-bottom: 1px solid rgba(255,215,0,0.2);
  color: #FFD700;
  font-size: 13px;
  font-weight: 500;
}

.list-row {
  display: grid;
  grid-template-columns: 0.6fr 1.8fr 1fr 1.5fr 1.8fr 0.8fr 0.8fr;
  padding: 14px 16px;
  border-bottom: 1px solid rgba(255,255,255,0.05);
  cursor: pointer;
  transition: all 0.3s;
  align-items: center;
}

.list-row:hover {
  background: rgba(255,215,0,0.1);
  transform: translateX(5px);
}

.list-avatar {
  width: 40px;
  height: 40px;
}

.list-avatar-img {
  width: 100%;
  height: 100%;
  border-radius: 50%;
  object-fit: cover;
}

.list-avatar-inner {
  width: 40px;
  height: 40px;
  background: linear-gradient(135deg, #FFD700, #FFA500);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
  font-weight: bold;
  color: #1a1a1a;
}

.name-highlight-modern {
  color: #fff;
  font-weight: 500;
}

.alias-modern {
  font-size: 12px;
  color: #aaa;
  margin-left: 5px;
}

.tags-inline-modern {
  display: flex;
  gap: 6px;
  flex-wrap: wrap;
}

.tag-small-modern {
  padding: 2px 8px;
  border-radius: 12px;
  font-size: 10px;
}

.tag-more {
  font-size: 10px;
  color: #aaa;
  padding: 2px 6px;
}

.detail-btn-modern {
  padding: 4px 12px;
  background: rgba(255,215,0,0.2);
  border: 1px solid rgba(255,215,0,0.5);
  border-radius: 20px;
  color: #FFD700;
  cursor: pointer;
  transition: all 0.3s;
}

.detail-btn-modern:hover {
  background: #FFD700;
  color: #1a1a1a;
}

.pagination-modern {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 16px;
  margin: 40px auto;
}

.page-btn {
  width: 40px;
  height: 40px;
  background: rgba(255,255,255,0.1);
  border: 1px solid rgba(255,215,0,0.3);
  border-radius: 50%;
  color: #FFD700;
  font-size: 20px;
  cursor: pointer;
  transition: all 0.3s;
}

.page-btn:hover:not(:disabled) {
  background: #FFD700;
  color: #1a1a1a;
  transform: scale(1.05);
}

.page-btn:disabled {
  opacity: 0.3;
  cursor: not-allowed;
}

.page-info {
  color: #fff;
  font-size: 14px;
}

.empty-state-modern {
  text-align: center;
  padding: 80px;
  color: #666;
}

.empty-animation {
  font-size: 80px;
  margin-bottom: 20px;
  animation: float 3s ease-in-out infinite;
}

@keyframes float {
  0%, 100% {
    transform: translateY(0);
  }
  50% {
    transform: translateY(-10px);
  }
}

.empty-state-modern h3 {
  color: #FFD700;
  margin-bottom: 10px;
}

.empty-reset-btn {
  margin-top: 20px;
  padding: 10px 24px;
  background: rgba(255,215,0,0.2);
  border: 1px solid #FFD700;
  border-radius: 30px;
  color: #FFD700;
  cursor: pointer;
  transition: all 0.3s;
}

.empty-reset-btn:hover {
  background: #FFD700;
  color: #1a1a1a;
}

.person-detail-dialog-modern :deep(.el-dialog) {
  background: linear-gradient(135deg, rgba(15, 15, 25, 0.98), rgba(8, 8, 20, 0.98));
  backdrop-filter: blur(20px);
  border: 1px solid rgba(255, 215, 0, 0.3);
  border-radius: 32px;
  box-shadow: 0 25px 50px rgba(0, 0, 0, 0.5);
  overflow: hidden;
}

.person-detail-dialog-modern :deep(.el-dialog__header) {
  background: linear-gradient(135deg, rgba(255, 215, 0, 0.1), transparent);
  border-bottom: 1px solid rgba(255, 215, 0, 0.2);
  padding: 20px 28px;
  margin: 0;
}

.person-detail-dialog-modern :deep(.el-dialog__title) {
  color: #FFD700;
  font-size: 26px;
  font-weight: 600;
  letter-spacing: 1px;
}

.person-detail-dialog-modern :deep(.el-dialog__headerbtn) {
  top: 24px;
  right: 24px;
}

.person-detail-dialog-modern :deep(.el-dialog__headerbtn .el-dialog__close) {
  color: #FFD700;
  font-size: 20px;
}

.person-detail-dialog-modern :deep(.el-dialog__body) {
  padding: 28px;
  max-height: 70vh;
  overflow-y: auto;
}

.person-detail-dialog-modern :deep(.el-dialog__body::-webkit-scrollbar) {
  width: 6px;
}

.person-detail-dialog-modern :deep(.el-dialog__body::-webkit-scrollbar-track) {
  background: rgba(255,255,255,0.1);
  border-radius: 3px;
}

.person-detail-dialog-modern :deep(.el-dialog__body::-webkit-scrollbar-thumb) {
  background: #FFD700;
  border-radius: 3px;
}

.detail-hero-card {
  display: flex;
  gap: 32px;
  margin-bottom: 32px;
  padding: 24px;
  background: linear-gradient(135deg, rgba(255,215,0,0.08), rgba(0,0,0,0.3));
  border-radius: 28px;
  border: 1px solid rgba(255,215,0,0.2);
  backdrop-filter: blur(8px);
}

.detail-avatar-large {
  position: relative;
  flex-shrink: 0;
  width: 120px;
  height: 120px;
}

.avatar-large-image {
  width: 100%;
  height: 100%;
  border-radius: 50%;
  object-fit: cover;
  background: linear-gradient(135deg, #FFD700, #FFA500);
  border: 3px solid rgba(255,215,0,0.5);
  box-shadow: 0 8px 25px rgba(0,0,0,0.3);
}

.avatar-large-inner {
  width: 120px;
  height: 120px;
  background: linear-gradient(135deg, #FFD700, #FFA500);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 56px;
  font-weight: bold;
  color: #1a1a1a;
  border: 3px solid rgba(255,215,0,0.5);
  box-shadow: 0 8px 25px rgba(0,0,0,0.3);
}

.avatar-glow {
  position: absolute;
  top: -8px;
  left: -8px;
  right: -8px;
  bottom: -8px;
  border-radius: 50%;
  background: radial-gradient(circle, rgba(255,215,0,0.3), transparent);
  animation: glowPulse 2s infinite;
  pointer-events: none;
}

@keyframes glowPulse {
  0%, 100% {
    opacity: 0.5;
    transform: scale(1);
  }
  50% {
    opacity: 1;
    transform: scale(1.05);
  }
}

.detail-info-large {
  flex: 1;
}

.detail-name {
  font-size: 34px;
  color: #FFD700;
  margin-bottom: 8px;
  font-weight: 600;
  text-shadow: 0 2px 10px rgba(0,0,0,0.3);
}

.detail-alias {
  color: #aaa;
  font-size: 15px;
  margin-bottom: 16px;
  font-style: italic;
}

.detail-basic {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  margin-bottom: 16px;
}

.detail-badge {
  padding: 5px 14px;
  background: rgba(0,0,0,0.5);
  border-radius: 25px;
  font-size: 13px;
  color: #e0e0e0;
  backdrop-filter: blur(4px);
}

.detail-tags-modern {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.tag-detail-modern {
  padding: 5px 14px;
  border-radius: 25px;
  font-size: 12px;
  font-weight: 500;
}

.life-timeline-section {
  position: relative;
  margin-bottom: 32px;
  padding: 16px 0;
}

.section-title {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 20px;
}

.title-icon {
  font-size: 28px;
}

.section-title h3 {
  font-size: 22px;
  color: #FFD700;
  margin: 0;
  font-weight: 500;
}

.title-line {
  flex: 1;
  height: 2px;
  background: linear-gradient(90deg, #FFD700, transparent);
}

.horizontal-timeline-wrapper {
  position: relative;
  overflow-x: auto;
  overflow-y: visible;
  scroll-behavior: smooth;
  padding: 20px 0;
}

.horizontal-timeline-wrapper::-webkit-scrollbar {
  height: 6px;
}

.horizontal-timeline-wrapper::-webkit-scrollbar-track {
  background: rgba(255,255,255,0.1);
  border-radius: 3px;
}

.horizontal-timeline-wrapper::-webkit-scrollbar-thumb {
  background: #FFD700;
  border-radius: 3px;
}

.horizontal-timeline-track {
  display: flex;
  gap: 20px;
  align-items: flex-start;
  min-width: max-content;
}

.timeline-spacer {
  width: 200px;
  flex-shrink: 0;
}

.timeline-node-card {
  position: relative;
  width: 240px;
  flex-shrink: 0;
  background: rgba(0,0,0,0.45);
  border-radius: 20px;
  padding: 18px;
  transition: all 0.3s;
  border: 1px solid rgba(255,215,0,0.25);
  backdrop-filter: blur(4px);
}

.timeline-node-card.active-node {
  border-color: #FFD700;
  background: rgba(255,215,0,0.12);
  transform: translateY(-5px);
  box-shadow: 0 8px 25px rgba(255,215,0,0.15);
}

.node-year {
  font-size: 18px;
  font-weight: bold;
  color: #FFD700;
  margin-bottom: 12px;
  letter-spacing: 1px;
}

.node-dot {
  position: absolute;
  top: -12px;
  left: 50%;
  transform: translateX(-50%);
  width: 14px;
  height: 14px;
  background: #FFD700;
  border-radius: 50%;
  box-shadow: 0 0 12px #FFD700;
}

.node-title {
  font-size: 17px;
  font-weight: bold;
  color: #fff;
  margin-bottom: 10px;
}

.node-desc {
  font-size: 13px;
  color: #ccc;
  line-height: 1.5;
  margin-bottom: 12px;
}

.node-links {
  display: flex;
  gap: 12px;
  margin-top: 8px;
}

.node-link-btn {
  background: none;
  border: none;
  color: #FFD700;
  font-size: 11px;
  cursor: pointer;
  padding: 0;
  transition: all 0.3s;
}

.node-link-btn:hover {
  text-decoration: underline;
  transform: translateX(2px);
}

.node-connector {
  position: absolute;
  top: 40px;
  right: -20px;
  width: 20px;
  height: 2px;
  background: linear-gradient(90deg, #FFD700, #666);
}

.timeline-nav-btn {
  position: absolute;
  top: 50%;
  transform: translateY(-50%);
  width: 38px;
  height: 38px;
  background: rgba(0,0,0,0.7);
  border: 1px solid rgba(255,215,0,0.5);
  border-radius: 50%;
  color: #FFD700;
  font-size: 22px;
  cursor: pointer;
  transition: all 0.3s;
  z-index: 10;
  backdrop-filter: blur(4px);
}

.timeline-nav-btn:hover:not(:disabled) {
  background: #FFD700;
  color: #1a1a1a;
  transform: translateY(-50%) scale(1.05);
}

.timeline-nav-btn:disabled {
  opacity: 0.3;
  cursor: not-allowed;
}

.timeline-prev {
  left: -10px;
}

.timeline-next {
  right: -10px;
}

.achievements-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: 20px;
  margin-bottom: 32px;
}

.achievement-card {
  background: rgba(0,0,0,0.4);
  border-radius: 20px;
  padding: 20px;
  transition: all 0.3s;
  border-left: 4px solid;
  backdrop-filter: blur(4px);
}

.achievement-card:hover {
  transform: translateY(-4px);
  background: rgba(0,0,0,0.5);
  box-shadow: 0 8px 25px rgba(0,0,0,0.3);
}

.political-card {
  border-left-color: #FF4D4D;
}

.military-card {
  border-left-color: #FFA500;
}

.cultural-card {
  border-left-color: #4CAF50;
}

.achievement-icon-large {
  font-size: 36px;
  margin-bottom: 12px;
}

.achievement-card h4 {
  font-size: 18px;
  color: #FFD700;
  margin-bottom: 12px;
  font-weight: 500;
}

.achievement-text {
  font-size: 13px;
  color: #ddd;
  line-height: 1.6;
}

.related-events-modern {
  margin-top: 24px;
}

.section-title.small {
  margin-bottom: 16px;
}

.events-scroll {
  display: flex;
  gap: 14px;
  overflow-x: auto;
  padding-bottom: 8px;
}

.events-scroll::-webkit-scrollbar {
  height: 4px;
}

.event-card-modern {
  flex-shrink: 0;
  width: 210px;
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 16px;
  background: rgba(255,255,255,0.06);
  border-radius: 16px;
  cursor: pointer;
  transition: all 0.3s;
  border: 1px solid rgba(255,215,0,0.2);
}

.event-card-modern:hover {
  background: rgba(255,215,0,0.15);
  transform: translateX(3px);
  border-color: #FFD700;
}

.event-icon {
  width: 36px;
  height: 36px;
  background: rgba(0,0,0,0.4);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 16px;
}

.event-icon.political {
  background: rgba(255,77,77,0.3);
}

.event-icon.military {
  background: rgba(255,165,0,0.3);
}

.event-icon.cultural {
  background: rgba(76,175,80,0.3);
}

.event-name-modern {
  font-size: 14px;
  font-weight: 500;
  color: #fff;
}

.event-year-modern {
  font-size: 11px;
  color: #aaa;
}

.event-arrow {
  margin-left: auto;
  color: #FFD700;
  font-size: 14px;
}

.empty-small-modern {
  text-align: center;
  padding: 32px;
  color: #666;
  font-size: 14px;
}

:deep(.highlight) {
  background: #FFD700;
  color: #1a1a1a;
  padding: 0 2px;
  border-radius: 3px;
}

@media (max-width: 768px) {
  .hero-title {
    font-size: 36px;
  }
  .hero-stats {
    gap: 24px;
  }
  .stat-number {
    font-size: 24px;
  }
  .filter-glass {
    margin: 0 16px 20px;
    padding: 20px;
  }
  .filter-grid {
    grid-template-columns: 1fr;
  }
  .person-grid {
    grid-template-columns: 1fr;
    padding: 0 16px;
  }
  .search-input-modern {
    width: 100%;
  }
  .detail-hero-card {
    flex-direction: column;
    text-align: center;
    align-items: center;
  }
  .detail-basic {
    justify-content: center;
  }
  .detail-tags-modern {
    justify-content: center;
  }
  .achievements-grid {
    grid-template-columns: 1fr;
  }
  .timeline-node-card {
    width: 260px;
  }
  .timeline-nav-btn {
    width: 32px;
    height: 32px;
    font-size: 18px;
  }
}
.person-detail-modern{
  background: linear-gradient(135deg, #1a1a1a 0%, #2d2d2d 100%);
}
</style>