<template>
  <div class="event-detail-page">
    <!-- 顶部导航栏 -->
    <top-component ref="topNavRef" class="top-nav" />
    
    <div class="detail-container">
      <!-- Hero 标题区：标题 + 副标题 + 统计数字 + 底部装饰波纹 -->
      <div class="hero-section">
        <div class="hero-content">
          <div class="breadcrumb">
            <router-link to="/timeline">时间轴</router-link>
            <span class="separator">></span>
            <router-link to="/event">事件科普</router-link>
            <span class="separator">></span>
            <span class="current">{{ currentEvent.eventName }}</span>
          </div>
          <h1 class="hero-title">{{ currentEvent.eventName }}</h1>
          <p class="hero-subtitle">回望历史瞬间 · 读懂事件背后的兴衰转折</p>
          <div class="event-meta">
            <span class="event-type" :class="currentEvent.eventType || 'political'">{{ getEventTypeLabel(currentEvent.eventType) }}</span>
            <span class="event-year">{{ formatYear(currentEvent.eventYear) }}</span>
            <span class="event-dynasty">{{ currentEvent.dynastyName || getDynastyName(currentEvent.dynastyId) }}</span>
          </div>
          <div class="hero-stats">
            <div class="stat-item">
              <span class="stat-number">{{ relatedPersons.length }}</span>
              <span class="stat-label">关联人物</span>
            </div>
            <div class="stat-item">
              <span class="stat-number">{{ relatedAllusions.length }}</span>
              <span class="stat-label">关联典故</span>
            </div>
            <div class="stat-item">
              <span class="stat-number">{{ dynastyEventsCount }}</span>
              <span class="stat-label">同朝事件</span>
            </div>
          </div>
        </div>
        <div class="hero-wave"></div>
      </div>

      <!-- 顶部横向事件时间轴 -->
      <div class="timeline-wrapper">
        <div class="timeline-container">
          <div class="timeline-header">
            <h2 class="timeline-title">📅 历史事件时间轴</h2>
            <h3 class="timeline-subtitle">从远古到近代 · 重要历史事件</h3>
          </div>

          <!-- 视图切换：时间轴 / 全部事件列表 -->
          <div class="timeline-toolbar">
            <button
              class="mode-btn"
              :class="{ active: viewMode === 'list' }"
              @click="toggleViewMode"
            >
              {{ viewMode === 'timeline' ? '📋 全部事件列表' : '📅 返回时间轴' }}
            </button>
          </div>

          <!-- 全部事件列表：搜索 + 列表，点条目直接切换到该事件 -->
          <div v-if="viewMode === 'list'" class="event-list-panel">
            <div class="event-list-search">
              <span class="search-icon">🔍</span>
              <input v-model="searchKeyword" type="text" placeholder="搜索事件名称 / 朝代 / 年份…" />
              <button v-if="searchKeyword" class="search-clear" @click="searchKeyword = ''">×</button>
            </div>
            <div class="event-list-count">
              共 <b>{{ filteredEvents.length }}</b> 条{{ searchKeyword ? '匹配结果' : '事件' }}
            </div>
            <div v-if="filteredEvents.length" class="event-list-grid">
              <button
                v-for="event in filteredEvents"
                :key="'le' + event.id"
                class="event-list-item"
                :class="{ active: String(event.id) === String(currentEvent.id) }"
                @click="jumpToEvent(event.id)"
              >
                <span class="eli-year">{{ formatYear(event.eventYear) }}</span>
                <span class="eli-name">{{ event.eventName }}</span>
                <span class="eli-dynasty">{{ event.dynastyName || getDynastyName(event.dynastyId) }}</span>
              </button>
            </div>
            <div v-else class="empty-state">没有匹配的事件，换个关键词试试</div>
          </div>
          
          <!-- 横向滚动区域 -->
          <div v-show="viewMode === 'timeline'" class="horizontal-scroll-wrapper" ref="scrollWrapper" @wheel="handleWheel">
            <div class="timeline-track" ref="track">
              <!-- 左侧隐形占位盒子 -->
              <div class="spacer-box left-spacer"></div>
              
              <!-- 实际事件卡片 -->
              <div 
                v-for="(event, idx) in sortedEvents" 
                :key="event.id"
                class="timeline-card"
                :class="{ 'active': activeEventIndex === idx }"
                :data-index="idx"
                @click="jumpToEvent(event.id)"
              >
                <div class="card-inner">
                  <div class="card-image-wrapper">
                    <img 
                      :src="getCardImageUrl(event.eventPicPath)" 
                      class="card-event-image"
                      @error="handleCardImageError"
                      alt="事件配图"
                    >
                    <div class="event-badge">{{ getEventTypeIcon(event.eventType) }}</div>
                  </div>
                  <div class="card-content">
                    <h3 class="event-name">{{ event.eventName }}</h3>
                    <p class="event-year">{{ formatYear(event.eventYear) }}</p>
                    <p class="event-desc">{{ event.eventBg ? event.eventBg.substring(0, 80) + '...' : '暂无描述' }}</p>
                  </div>
                </div>
              </div>
              
              <!-- 右侧隐形占位盒子 -->
              <div class="spacer-box right-spacer"></div>
            </div>
          </div>
          
          <!-- 自定义滚动条 -->
          <div v-show="viewMode === 'timeline'" class="custom-scrollbar">
            <div class="scrollbar-track" ref="scrollbarTrack" @click="handleTrackClick">
              <div 
                class="scrollbar-thumb" 
                ref="scrollbarThumb"
                :style="{ left: thumbPosition + '%', width: thumbWidth + '%' }"
                @mousedown="startDrag"
              ></div>
            </div>
            <!-- 事件刻度条：每个事件一格，点击直达，悬停显示年份与名称 -->
            <div class="scrollbar-steps">
              <button
                v-for="(event, idx) in sortedEvents"
                :key="idx"
                class="scrollbar-step"
                :class="{ 'active-step': activeEventIndex === idx }"
                :title="formatYear(event.eventYear) + ' · ' + event.eventName"
                @click="scrollToCard(idx, true)"
              ></button>
            </div>
          </div>
        </div>
      </div>

      <!-- 朝代关联卡片 -->
      <div class="dynasty-card" @click="jumpToDynastyDetail(currentEvent.dynastyName || getDynastyName(currentEvent.dynastyId))">
        <div class="dynasty-card-content">
          <div class="dynasty-info">
            <h3>{{ currentEvent.dynastyName || getDynastyName(currentEvent.dynastyId) }}</h3>
            <p class="dynasty-period">{{ getDynastyPeriod(currentEvent.dynastyName || getDynastyName(currentEvent.dynastyId)) }}</p>
            <div class="dynasty-details">
              <span>🏛️ 都城：{{ getDynastyCapital(currentEvent.dynastyName || getDynastyName(currentEvent.dynastyId)) }}</span>
              <span>👑 创始人：{{ getDynastyFounder(currentEvent.dynastyName || getDynastyName(currentEvent.dynastyId)) }}</span>
            </div>
          </div>
          <div class="card-arrow">→</div>
        </div>
      </div>

      <!-- 主内容区域 -->
      <div class="content-wrapper">
        <!-- 左侧主要内容 -->
        <div class="main-content">
          <div class="image-container" v-if="currentEvent.eventName">
            <img 
              :src="resolveAssetUrl(currentEvent.eventPicPath)" 
              class="event-image-simple"
              @error="handleImageError"
              alt="事件配图"
            >
            <div class="image-caption">{{ currentEvent.eventName }} 历史场景</div>
          </div>

          <!-- 富文本内容区域 -->
          <div class="rich-content">
            <div class="content-section">
              <h2>📖 事件背景</h2>
              <div class="content-text" v-html="currentEvent.eventBg || '暂无背景信息'"></div>
            </div>

            <div class="content-section">
              <h2>⚡ 事件过程</h2>
              <div class="content-text" v-html="currentEvent.eventProcess || '暂无过程信息'"></div>
            </div>

            <div class="content-section">
              <h2>🎯 历史影响</h2>
              <div class="content-text" v-html="currentEvent.eventImpact || '暂无影响信息'"></div>
            </div>
          </div>

          <!-- 数据统计卡片 -->
          <div class="stats-cards">
            <div class="stat-card">
              <div class="stat-icon">👥</div>
              <div class="stat-number">{{ relatedPersons.length }}</div>
              <div class="stat-label">关联人物</div>
            </div>
            <div class="stat-card">
              <div class="stat-icon">📖</div>
              <div class="stat-number">{{ relatedAllusions.length }}</div>
              <div class="stat-label">关联典故</div>
            </div>
            <div class="stat-card">
              <div class="stat-icon">📊</div>
              <div class="stat-number">{{ dynastyEventsPercentage }}%</div>
              <div class="stat-label">占朝代事件比例</div>
            </div>
            <div class="stat-card">
              <div class="stat-icon">⏱️</div>
              <div class="stat-number">{{ eventDuration }}</div>
              <div class="stat-label">事件持续时间</div>
            </div>
          </div>
        </div>

        <!-- 右侧侧边栏 -->
        <div class="sidebar">
          <!-- 关联人物列表 -->
          <div class="sidebar-section">
            <div class="section-header">
              <h3>👥 关联人物</h3>
              <el-select v-model="personSortBy" placeholder="排序方式" size="small" @change="sortPersons">
                <el-option label="按关联度排序" value="relevance"></el-option>
                <el-option label="按姓名排序" value="name"></el-option>
                <el-option label="按身份排序" value="role"></el-option>
              </el-select>
            </div>
            <div class="person-list">
              <div v-for="person in sortedPersons" :key="person.id" class="person-card" @click="jumpToPersonDetail(person.name)">
                <div class="person-info">
                  <div class="person-name">{{ person.name }}</div>
                  <div class="person-role">{{ person.role }}</div>
                  <div class="person-relation">
                    <span class="relation-tag" :class="person.relationType">
                      {{ getRelationTypeLabel(person.relationType) }}
                    </span>
                  </div>
                </div>
                <div class="relevance-score" v-if="person.relevance">
                  <el-progress type="circle" :percentage="person.relevance" :width="40" :stroke-width="4" />
                </div>
              </div>
            </div>
            <div v-if="!relatedPersons.length" class="empty-state">暂无关联人物</div>
          </div>

          <!-- 关联典故列表 -->
          <div class="sidebar-section">
            <div class="section-header">
              <h3>📚 关联典故</h3>
              <el-select v-model="allusionDifficulty" placeholder="难度筛选" size="small" clearable @change="filterAllusions">
                <el-option label="入门级" value="easy"></el-option>
                <el-option label="进阶级" value="medium"></el-option>
                <el-option label="精通级" value="hard"></el-option>
              </el-select>
            </div>
            <div class="allusion-list">
              <div v-for="allusion in filteredAllusions" :key="allusion.id" class="allusion-card">
                <div class="allusion-name">{{ allusion.name }}</div>
                <div class="allusion-meaning">{{ allusion.meaning }}</div>
                <div class="allusion-relation">
                  <span class="relation-badge" :class="allusion.relationType">
                    {{ getAllusionRelationLabel(allusion.relationType) }}
                  </span>
                  <span class="difficulty-badge" :class="allusion.difficulty">
                    {{ getDifficultyLabel(allusion.difficulty) }}
                  </span>
                </div>
                <button class="detail-btn" @click="jumpToAllusionDetail(allusion.name)">查看详情</button>
              </div>
            </div>
            <div v-if="filteredAllusions.length === 0" class="empty-state">暂无相关典故</div>
          </div>

          <!-- 相关事件推荐 -->
          <div class="sidebar-section recommend-events-section">
            <div class="section-header">
              <h3>🔗 相关事件推荐</h3>
              <div class="recommend-nav-buttons">
                <button class="recommend-nav-btn prev" @click="scrollRecommendPrev" :disabled="recommendScrollLeft <= 0">‹</button>
                <button class="recommend-nav-btn next" @click="scrollRecommendNext" :disabled="recommendScrollLeft >= recommendMaxScroll">›</button>
              </div>
            </div>
            <div class="recommend-scroll-wrapper" ref="recommendScrollWrapper" @wheel="handleRecommendWheel">
              <div class="recommend-track" ref="recommendTrack">
                <!-- 左侧隐形占位盒子 -->
                <div class="recommend-spacer-box left-recommend-spacer"></div>
                
                <!-- 实际推荐卡片 -->
                <div 
                  v-for="event in allRecommendedEvents" 
                  :key="event.id" 
                  class="recommend-card-horizontal"
                  @click="jumpToEvent(event.id)"
                >
                  <div class="recommend-card-inner">
                    <div class="recommend-year-badge">{{ formatYear(event.eventYear) }}</div>
                    <div class="recommend-name">{{ event.eventName }}</div>
                    <div class="recommend-dynasty">{{ event.dynastyName || getDynastyName(event.dynastyId) }}</div>
                    <div class="recommend-relation-reason">{{ getRecommendReason(event) }}</div>
                  </div>
                </div>
                
                <!-- 右侧隐形占位盒子 -->
                <div class="recommend-spacer-box right-recommend-spacer"></div>
              </div>
            </div>
            <div class="recommend-scrollbar">
              <div class="recommend-scrollbar-track" ref="recommendScrollbarTrack" @click="handleRecommendTrackClick">
                <div 
                  class="recommend-scrollbar-thumb" 
                  ref="recommendScrollbarThumb"
                  :style="{ left: recommendThumbPosition + '%', width: recommendThumbWidth + '%' }"
                  @mousedown="startRecommendDrag"
                ></div>
              </div>
            </div>
            <!-- 推荐事件刻度条：细刻度，悬停显示年份与名称 -->
            <div class="recommend-steps">
              <button
                v-for="(event, idx) in allRecommendedEvents"
                :key="idx"
                class="recommend-step"
                :class="{ 'active-recommend-step': activeRecommendIndex === idx }"
                :title="formatYear(event.eventYear) + ' · ' + event.eventName"
                @click="scrollToRecommendCard(idx)"
              ></button>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { onMounted, ref, onUnmounted, nextTick, computed } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { ElMessage } from 'element-plus';
import topComponent from '@/components/topComponent.vue';
import request, { resolveAssetUrl, errorMessage } from '@/utils/request';

const router = useRouter();
const route = useRoute();

// 默认占位图片
const defaultImage = 'https://picsum.photos/id/104/800/400';

// 获取卡片图片完整URL（统一走 resolveAssetUrl，避免硬编码后端地址）
function getCardImageUrl(picPath) {
  return resolveAssetUrl(picPath);
}

// 卡片图片加载失败处理
function handleCardImageError(e) {
  e.target.style.display = 'none'; // 隐藏失败的图片，显示图标
}

// 图片加载失败时使用默认图片
function handleImageError(e) {
  console.log('图片加载失败，尝试使用默认图片');
  e.target.src = defaultImage;
}

// 朝代信息映射表（兜底数据，实际以接口 /dynasty/list/all 返回为准，见 loadDynastyList）
const dynastyMap = {
  1: { name: "夏朝", period: "约前2070年—约前1600年", capital: "阳城", founder: "禹" },
  2: { name: "商朝", period: "约前1600年—前1046年", capital: "殷", founder: "汤" },
  3: { name: "周朝", period: "前1046年—前256年", capital: "镐京 → 洛邑", founder: "周武王" },
  4: { name: "秦朝", period: "前221年—前207年", capital: "咸阳", founder: "秦始皇" },
  5: { name: "汉朝", period: "前202年—220年", capital: "长安 → 洛阳", founder: "汉高祖" },
  6: { name: "三国", period: "220年—280年", capital: "洛阳/成都/建业", founder: "曹丕/刘备/孙权" },
  7: { name: "晋朝", period: "265年—420年", capital: "洛阳 → 建康", founder: "司马炎" },
  8: { name: "南北朝", period: "420年—589年", capital: "多个都城", founder: "多个政权" },
  9: { name: "隋朝", period: "581年—618年", capital: "大兴 → 洛阳", founder: "杨坚" },
  10: { name: "唐朝", period: "618年—907年", capital: "长安", founder: "李渊" },
  11: { name: "宋朝", period: "960年—1279年", capital: "东京", founder: "赵匡胤" },
  12: { name: "元朝", period: "1271年—1368年", capital: "大都", founder: "忽必烈" },
  13: { name: "明朝", period: "1368年—1644年", capital: "南京 → 北京", founder: "朱元璋" },
  14: { name: "清朝", period: "1636年—1912年", capital: "北京", founder: "皇太极" }
};

function getDynastyName(dynastyId) {
  return dynastyMap[dynastyId]?.name || "";
}

// 来自后端的朝代数据，管理端修改后前台可实时看到
const dynastyList = ref([]);

function formatDynastyYear(y) {
  if (y === null || y === undefined || y === '') return '';
  return Number(y) < 0 ? `前${Math.abs(Number(y))}年` : `${y}年`;
}

/** 加载真实朝代数据，并用它覆盖上面的兜底映射 */
async function loadDynastyList() {
  try {
    const res = await request.get('/dynasty/list/all');
    const list = res.data?.code === 200 ? (res.data.data || []) : [];
    if (list.length === 0) return;
    dynastyList.value = list;

    const merged = {};
    list.forEach(d => {
      const fallback = dynastyMap[d.id] || {};
      // 数据库中的朝代名多为简称（如“唐”），展示时补成“唐朝”更自然
      const shortName = d.name || fallback.name || '';
      const displayName = /(朝|三国|南北朝|时期|代)$/.test(shortName) ? shortName : `${shortName}朝`;
      merged[d.id] = {
        name: displayName,
        period: `${formatDynastyYear(d.startYear)}—${formatDynastyYear(d.endYear)}`,
        capital: d.capital || fallback.capital || '未知',
        founder: d.founder || fallback.founder || '未知',
        desc: d.dynastyDesc || '',
        mapPath: d.mapPath || ''
      };
    });
    Object.assign(dynastyMap, merged);
  } catch (e) {
    console.warn('加载朝代数据失败，使用内置朝代信息', e);
  }
}

function getDynastyPeriod(dynastyName) {
  const dynasty = Object.values(dynastyMap).find(d => d.name === dynastyName);
  return dynasty?.period || "未知时期";
}

function getDynastyCapital(dynastyName) {
  const dynasty = Object.values(dynastyMap).find(d => d.name === dynastyName);
  return dynasty?.capital || "未知";
}

function getDynastyFounder(dynastyName) {
  const dynasty = Object.values(dynastyMap).find(d => d.name === dynastyName);
  return dynasty?.founder || "未知";
}

const allEvents = ref([]);
const isLoading = ref(false);
const currentEvent = ref({});

const sortedEvents = computed(() => {
  return [...allEvents.value].sort((a, b) => (a.eventYear || 0) - (b.eventYear || 0));
});

const allRecommendedEvents = computed(() => {
  return allEvents.value.filter(e => e.id !== currentEvent.value.id);
});

const activeEventIndex = ref(0);
const isNavBouncing = ref(false);
const scrollWrapper = ref(null);
const track = ref(null);
const scrollbarThumb = ref(null);
const scrollbarTrack = ref(null);

const recommendScrollWrapper = ref(null);
const recommendTrack = ref(null);
const recommendScrollbarThumb = ref(null);
const recommendScrollbarTrack = ref(null);
const recommendScrollLeft = ref(0);
const recommendMaxScroll = ref(0);
const activeRecommendIndex = ref(0);
let isRecommendDragging = false;

let bounceTimer = null;
let isDragging = false;

const thumbPosition = computed(() => {
  if (!scrollWrapper.value) return 0;
  const container = scrollWrapper.value;
  const maxScroll = container.scrollWidth - container.clientWidth;
  const currentScroll = container.scrollLeft;
  if (maxScroll <= 0) return 0;
  return (currentScroll / maxScroll) * 100;
});

const thumbWidth = computed(() => {
  if (!scrollWrapper.value) return 100;
  const container = scrollWrapper.value;
  const visibleRatio = container.clientWidth / container.scrollWidth;
  return Math.max(visibleRatio * 100, 5);
});

const recommendThumbPosition = computed(() => {
  if (!recommendScrollWrapper.value) return 0;
  const container = recommendScrollWrapper.value;
  const maxScroll = container.scrollWidth - container.clientWidth;
  const currentScroll = container.scrollLeft;
  if (maxScroll <= 0) return 0;
  return (currentScroll / maxScroll) * 100;
});

const recommendThumbWidth = computed(() => {
  if (!recommendScrollWrapper.value) return 100;
  const container = recommendScrollWrapper.value;
  const visibleRatio = container.clientWidth / container.scrollWidth;
  return Math.max(visibleRatio * 100, 5);
});

function getRecommendReason(event) {
  const currentDynasty = currentEvent.value.dynastyName || getDynastyName(currentEvent.value.dynastyId);
  const eventDynasty = event.dynastyName || getDynastyName(event.dynastyId);
  
  if (eventDynasty === currentDynasty) {
    return `同${eventDynasty}事件`;
  }
  if (event.eventType === currentEvent.value.eventType) {
    return `同类${getEventTypeLabel(event.eventType)}事件`;
  }
  return "相关推荐";
}

function getEventTypeIcon(type) {
  const icons = { political: '🏛️', military: '⚔️', cultural: '📚' };
  return icons[type] || '📜';
}

function triggerNavBounce() {
  if (bounceTimer) clearTimeout(bounceTimer);
  
  if (isNavBouncing.value) {
    isNavBouncing.value = false;
    nextTick(() => {
      isNavBouncing.value = true;
      bounceTimer = setTimeout(() => {
        isNavBouncing.value = false;
        bounceTimer = null;
      }, 600);
    });
  } else {
    isNavBouncing.value = true;
    bounceTimer = setTimeout(() => {
      isNavBouncing.value = false;
      bounceTimer = null;
    }, 600);
  }
}

function findActiveCardByPosition() {
  if (!scrollWrapper.value || !track.value) return 0;
  
  const container = scrollWrapper.value;
  const containerRect = container.getBoundingClientRect();
  const containerCenter = containerRect.left + containerRect.width / 2;
  const cards = track.value.querySelectorAll('.timeline-card');
  
  let closestIndex = 0;
  let minDistance = Infinity;
  
  cards.forEach((card, idx) => {
    const cardRect = card.getBoundingClientRect();
    const cardCenter = cardRect.left + cardRect.width / 2;
    const distance = Math.abs(cardCenter - containerCenter);
    
    if (distance < minDistance) {
      minDistance = distance;
      closestIndex = idx;
    }
  });
  
  return closestIndex;
}

function updateActiveState() {
  if (!scrollWrapper.value) return;
  
  const newIndex = findActiveCardByPosition();
  
  if (newIndex !== activeEventIndex.value) {
    activeEventIndex.value = newIndex;
    triggerNavBounce();
  }
}

/** 窗口尺寸变化：重新计算当前激活卡片与推荐区滚动状态（具名函数，卸载时可移除监听） */
function onWindowResize() {
  updateActiveState();
  updateRecommendScrollState();
}

function scrollToCard(index, smooth = true) {
  if (!scrollWrapper.value || !track.value) return;
  if (index < 0 || index >= sortedEvents.value.length) return;
  
  const container = scrollWrapper.value;
  const cards = track.value.querySelectorAll('.timeline-card');
  const card = cards[index];
  if (!card) return;
  
  const cardOffsetLeft = card.offsetLeft;
  const cardWidth = card.offsetWidth;
  const containerWidth = container.clientWidth;
  const targetScrollLeft = cardOffsetLeft - (containerWidth / 2) + (cardWidth / 2);
  
  const maxScroll = container.scrollWidth - containerWidth;
  const finalScrollLeft = Math.max(0, Math.min(targetScrollLeft, maxScroll));
  
  container.scrollTo({
    left: finalScrollLeft,
    behavior: smooth ? 'smooth' : 'auto'
  });
  
  if (activeEventIndex.value !== index) {
    activeEventIndex.value = index;
    triggerNavBounce();
  }
}

async function jumpToEvent(eventId) {
  try {
    isLoading.value = true;
    const res = await request.get(`/historical-event/${eventId}`);
    if (res.data.code !== 200 || !res.data.data) {
      ElMessage.error(res.data.msg || '事件不存在');
      return;
    }
    currentEvent.value = res.data.data;
    
    const eventIndex = sortedEvents.value.findIndex(e => String(e.id) === String(eventId));
    
    if (eventIndex !== -1) {
      activeEventIndex.value = eventIndex;
      triggerNavBounce();
      scrollToCard(eventIndex, true);
    }
    
    window.scrollTo({ top: 0, behavior: 'smooth' });
    ElMessage.success(`切换到事件：${currentEvent.value.eventName}`);
  } catch (error) {
    ElMessage.error(errorMessage(error, '加载事件详情失败'));
  } finally {
    isLoading.value = false;
  }
}

async function loadAllEvents() {
  try {
    isLoading.value = true;

    // 修复：原来写死 1~14 号朝代，管理端新增的朝代永远不会被加载。
    // 现在按 /dynasty/list/all 返回的真实ID并发拉取。
    const dynastyIds = dynastyList.value.length > 0
      ? dynastyList.value.map(d => d.id)
      : [1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14];
    
    let allEventsData = [];
    
    const promises = dynastyIds.map(dynastyId => 
      request.get('/historical-event/page', {
        params: {
          pageNum: 1,
          pageSize: 1000,
          dynastyId: dynastyId
        }
      }).catch(err => {
        console.warn(`获取朝代ID ${dynastyId} 的事件失败:`, err);
        return null;
      })
    );
    
    const results = await Promise.all(promises);
    
    results.forEach((res, index) => {
      if (res && res.data && res.data.code === 200) {
        const events = res.data.data?.records || [];
        if (events.length > 0) {
          events.forEach(event => {
            event.dynastyName = event.dynastyName || getDynastyName(event.dynastyId != null ? event.dynastyId : dynastyIds[index]);
          });
          allEventsData = [...allEventsData, ...events];
        }
      }
    });
    
    allEvents.value = allEventsData.sort((a, b) => (a.eventYear || 0) - (b.eventYear || 0));
    
    if (allEvents.value.length === 0) {
      ElMessage.warning('暂无事件数据');
    }
    
  } catch (error) {
    ElMessage.error(errorMessage(error, '加载事件列表失败'));
  } finally {
    isLoading.value = false;
  }
}

async function loadEventDetail(eventId) {
  try {
    isLoading.value = true;
    const res = await request.get(`/historical-event/${eventId}`);
    // 修复：未判断业务码，事件不存在时会把 null 当成事件对象使用而报错
    if (res.data.code !== 200 || !res.data.data) {
      ElMessage.error(res.data.msg || '事件不存在');
      return;
    }
    currentEvent.value = res.data.data;
  } catch (error) {
    ElMessage.error(errorMessage(error, '加载事件详情失败'));
  } finally {
    isLoading.value = false;
  }
}

function updateRecommendScrollState() {
  if (!recommendScrollWrapper.value) return;
  recommendScrollLeft.value = recommendScrollWrapper.value.scrollLeft;
  recommendMaxScroll.value = recommendScrollWrapper.value.scrollWidth - recommendScrollWrapper.value.clientWidth;
  
  const container = recommendScrollWrapper.value;
  const containerRect = container.getBoundingClientRect();
  const containerCenter = containerRect.left + containerRect.width / 2;
  const cards = recommendTrack.value.querySelectorAll('.recommend-card-horizontal');
  
  let closestIndex = 0;
  let minDistance = Infinity;
  cards.forEach((card, idx) => {
    const cardRect = card.getBoundingClientRect();
    const cardCenter = cardRect.left + cardRect.width / 2;
    const distance = Math.abs(cardCenter - containerCenter);
    if (distance < minDistance) {
      minDistance = distance;
      closestIndex = idx;
    }
  });
  activeRecommendIndex.value = closestIndex;
}

function scrollRecommendPrev() {
  if (!recommendScrollWrapper.value) return;
  const container = recommendScrollWrapper.value;
  const scrollAmount = container.clientWidth * 0.8;
  container.scrollBy({ left: -scrollAmount, behavior: 'smooth' });
  setTimeout(updateRecommendScrollState, 300);
}

function scrollRecommendNext() {
  if (!recommendScrollWrapper.value) return;
  const container = recommendScrollWrapper.value;
  const scrollAmount = container.clientWidth * 0.8;
  container.scrollBy({ left: scrollAmount, behavior: 'smooth' });
  setTimeout(updateRecommendScrollState, 300);
}

function handleRecommendWheel(e) {
  if (!recommendScrollWrapper.value) return;
  e.preventDefault();
  const delta = e.deltaY || e.deltaX;
  recommendScrollWrapper.value.scrollLeft += delta;
  updateRecommendScrollState();
}

function scrollToRecommendCard(index) {
  if (!recommendScrollWrapper.value || !recommendTrack.value) return;
  const cards = recommendTrack.value.querySelectorAll('.recommend-card-horizontal');
  const card = cards[index];
  if (!card) return;
  
  const container = recommendScrollWrapper.value;
  const cardOffsetLeft = card.offsetLeft;
  const cardWidth = card.offsetWidth;
  const containerWidth = container.clientWidth;
  const targetScrollLeft = cardOffsetLeft - (containerWidth / 2) + (cardWidth / 2);
  const maxScroll = container.scrollWidth - containerWidth;
  const finalScrollLeft = Math.max(0, Math.min(targetScrollLeft, maxScroll));
  
  container.scrollTo({ left: finalScrollLeft, behavior: 'smooth' });
  setTimeout(updateRecommendScrollState, 300);
}

function handleRecommendTrackClick(e) {
  if (e.target === recommendScrollbarThumb.value) return;
  const trackRect = recommendScrollbarTrack.value.getBoundingClientRect();
  const clickX = e.clientX - trackRect.left;
  const clickPercent = clickX / trackRect.width;
  const maxScroll = recommendScrollWrapper.value.scrollWidth - recommendScrollWrapper.value.clientWidth;
  const newScrollLeft = clickPercent * maxScroll;
  recommendScrollWrapper.value.scrollLeft = Math.max(0, Math.min(newScrollLeft, maxScroll));
  updateRecommendScrollState();
}

function startRecommendDrag(e) {
  e.preventDefault();
  isRecommendDragging = true;
  const startX = e.clientX;
  const startScrollLeft = recommendScrollWrapper.value.scrollLeft;
  const maxScroll = recommendScrollWrapper.value.scrollWidth - recommendScrollWrapper.value.clientWidth;
  
  function onDragMove(moveEvent) {
    if (!isRecommendDragging) return;
    const deltaX = moveEvent.clientX - startX;
    const trackRect = recommendScrollbarTrack.value.getBoundingClientRect();
    const deltaPercent = deltaX / trackRect.width;
    const newScrollLeft = startScrollLeft + deltaPercent * maxScroll;
    recommendScrollWrapper.value.scrollLeft = Math.max(0, Math.min(newScrollLeft, maxScroll));
    updateRecommendScrollState();
  }
  
  function onDragEnd() {
    isRecommendDragging = false;
    document.removeEventListener('mousemove', onDragMove);
    document.removeEventListener('mouseup', onDragEnd);
  }
  
  document.addEventListener('mousemove', onDragMove);
  document.addEventListener('mouseup', onDragEnd);
}

function startDrag(e) {
  e.preventDefault();
  isDragging = true;
  
  const startX = e.clientX;
  const startScrollLeft = scrollWrapper.value.scrollLeft;
  const maxScroll = scrollWrapper.value.scrollWidth - scrollWrapper.value.clientWidth;
  
  function onDragMove(moveEvent) {
    if (!isDragging) return;
    const deltaX = moveEvent.clientX - startX;
    const trackRect = scrollbarTrack.value.getBoundingClientRect();
    const deltaPercent = deltaX / trackRect.width;
    const newScrollLeft = startScrollLeft + deltaPercent * maxScroll;
    scrollWrapper.value.scrollLeft = Math.max(0, Math.min(newScrollLeft, maxScroll));
    updateActiveState();
  }
  
  function onDragEnd() {
    isDragging = false;
    document.removeEventListener('mousemove', onDragMove);
    document.removeEventListener('mouseup', onDragEnd);
  }
  
  document.addEventListener('mousemove', onDragMove);
  document.addEventListener('mouseup', onDragEnd);
}

function handleTrackClick(e) {
  if (e.target === scrollbarThumb.value) return;
  
  const trackRect = scrollbarTrack.value.getBoundingClientRect();
  const clickX = e.clientX - trackRect.left;
  const clickPercent = clickX / trackRect.width;
  const maxScroll = scrollWrapper.value.scrollWidth - scrollWrapper.value.clientWidth;
  const newScrollLeft = clickPercent * maxScroll;
  
  scrollWrapper.value.scrollLeft = Math.max(0, Math.min(newScrollLeft, maxScroll));
  updateActiveState();
}

function handleWheel(e) {
  if (!scrollWrapper.value) return;
  
  e.preventDefault();
  const delta = e.deltaY || e.deltaX;
  const container = scrollWrapper.value;
  const newScrollLeft = container.scrollLeft + delta;
  const maxScroll = container.scrollWidth - container.clientWidth;
  
  container.scrollLeft = Math.max(0, Math.min(newScrollLeft, maxScroll));
  
  requestAnimationFrame(() => {
    updateActiveState();
  });
}

let ticking = false;
function onScroll() {
  if (!ticking) {
    requestAnimationFrame(() => {
      updateActiveState();
      ticking = false;
    });
    ticking = true;
  }
}

let recommendTicking = false;
function onRecommendScroll() {
  if (!recommendTicking) {
    requestAnimationFrame(() => {
      updateRecommendScrollState();
      recommendTicking = false;
    });
    recommendTicking = true;
  }
}

const personSortBy = ref('relevance');
const allusionDifficulty = ref('');

/* ===== 视图切换与事件搜索 ===== */
const viewMode = ref('timeline');   // 'timeline' 横滑卡带 | 'list' 全部事件列表
const searchKeyword = ref('');

function toggleViewMode() {
  viewMode.value = viewMode.value === 'timeline' ? 'list' : 'timeline';
}

/** 列表视图用：按事件名 / 朝代 / 年份模糊筛选 */
const filteredEvents = computed(() => {
  const kw = searchKeyword.value.trim().toLowerCase();
  const list = sortedEvents.value;
  if (!kw) return list;
  return list.filter(event => {
    const dynasty = (event.dynastyName || getDynastyName(event.dynastyId) || '').toLowerCase();
    return (
      (event.eventName || '').toLowerCase().includes(kw) ||
      dynasty.includes(kw) ||
      formatYear(event.eventYear).toLowerCase().includes(kw)
    );
  });
});

/* ===== 关联人物 / 关联典故：库中没有关联表，按事件正文关键词推导 ===== */
const persons = ref([]);
const allusions = ref([]);

/** 事件正文合并文本：字段间加分隔符并去空白，避免跨字段拼接出假人名 */
function eventFullText(ev) {
  return [ev.eventName, ev.eventBg, ev.eventProcess, ev.eventImpact]
    .map(t => (t || '').replace(/\s/g, ''))
    .join('｜');
}

async function loadRelationSources() {
  const dynastyIds = dynastyList.value.length > 0
    ? dynastyList.value.map(d => d.id)
    : [1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14];

  const [personResults, allusionRes] = await Promise.all([
    Promise.all(dynastyIds.map(id =>
      request.get('/historical-person/page', { params: { pageNum: 1, pageSize: 1000, dynastyId: id } }).catch(() => null)
    )),
    request.get('/allusion/page', { params: { pageNum: 1, pageSize: 1000 } }).catch(() => null)
  ]);

  let personRecords = [];
  personResults.forEach(res => {
    if (res?.data?.code === 200) personRecords = personRecords.concat(res.data.data?.records || []);
  });
  persons.value = personRecords;

  if (allusionRes?.data?.code === 200) allusions.value = allusionRes.data.data?.records || [];
}

/** 关联人物：姓名出现在当前事件正文里的人物（只匹配两字及以上姓名，避免“启”命中“开启”） */
const relatedPersons = computed(() => {
  const ev = currentEvent.value;
  if (!ev || !ev.eventName) return [];
  const text = eventFullText(ev);
  return persons.value
    .filter(p => p.personName && p.personName.length >= 2 && text.includes(p.personName))
    .map(p => ({
      id: p.id,
      name: p.personName,
      role: p.identityType || '历史人物',
      relationType: /帝王|皇帝|君主|王$/.test(p.identityType || '') ? 'leader' : 'participant',
      relevance: 0
    }));
});

/** 关联典故：与当前事件同朝代的典故；正文直接提到的典故标注为直接相关 */
const ALLUSION_DIFFICULTY = { 励志: 'easy', 智慧: 'medium', 警示: 'hard' };
const relatedAllusions = computed(() => {
  const ev = currentEvent.value;
  if (!ev || !ev.eventName) return [];
  const text = eventFullText(ev);
  const isDirect = a => text.includes(a.allusionName);
  return allusions.value
    .filter(a => isDirect(a) || String(a.dynastyId) === String(ev.dynastyId))
    .slice()
    .sort((a, b) => (isDirect(b) ? 1 : 0) - (isDirect(a) ? 1 : 0))
    .map(a => ({
      id: a.id,
      name: a.allusionName,
      meaning: a.paraphrase || a.moral || '',
      relationType: isDirect(a) ? 'origin' : 'era',
      difficulty: ALLUSION_DIFFICULTY[a.themeType] || 'medium'
    }));
});

const dynastyEventsPercentage = computed(() => {
  const currentDynastyName = currentEvent.value.dynastyName || getDynastyName(currentEvent.value.dynastyId);
  const dynastyEvents = allEvents.value.filter(e => {
    const eDynasty = e.dynastyName || getDynastyName(e.dynastyId);
    return eDynasty === currentDynastyName;
  });
  if (dynastyEvents.length === 0) return 0;
  const percentage = (1 / dynastyEvents.length * 100).toFixed(1);
  return percentage;
});

/** 展示用：当前事件所属朝代收录的事件总数（口径与 dynastyEventsPercentage 一致，不改变既有逻辑） */
const dynastyEventsCount = computed(() => {
  const currentDynastyName = currentEvent.value.dynastyName || getDynastyName(currentEvent.value.dynastyId);
  return allEvents.value.filter(e => (e.dynastyName || getDynastyName(e.dynastyId)) === currentDynastyName).length;
});

const eventDuration = computed(() => "1年");

const sortedPersons = computed(() => {
  const persons = [...relatedPersons.value];
  if (personSortBy.value === 'relevance') {
    return persons.sort((a, b) => b.relevance - a.relevance);
  } else if (personSortBy.value === 'name') {
    return persons.sort((a, b) => a.name.localeCompare(b.name));
  } else if (personSortBy.value === 'role') {
    return persons.sort((a, b) => a.role.localeCompare(b.role));
  }
  return persons;
});

const filteredAllusions = computed(() => {
  let allusions = [...relatedAllusions.value];
  if (allusionDifficulty.value) {
    allusions = allusions.filter(a => a.difficulty === allusionDifficulty.value);
  }
  return allusions;
});

function getEventTypeLabel(type) {
  const typeMap = { political: '政治', military: '军事', cultural: '文化' };
  return typeMap[type] || '历史';
}

function formatYear(year) {
  if (!year) return '未知年份';
  if (year < 0) return `公元前${Math.abs(year)}年`;
  return `公元${year}年`;
}

function getRelationTypeLabel(type) {
  const labels = { leader: '主导', participant: '参与', influenced: '受影响' };
  return labels[type] || type;
}

function getAllusionRelationLabel(type) {
  const labels = { origin: '源于该事件', era: '同朝典故', background: '反映背景', reflection: '历史映照' };
  return labels[type] || type;
}

function getDifficultyLabel(level) {
  const labels = { easy: '入门', medium: '进阶', hard: '精通' };
  return labels[level] || level;
}

function sortPersons() {}
function filterAllusions() {}

function jumpToPersonDetail(personName) {
  router.push({ path: '/person', query: { keyword: personName } });
  ElMessage.success(`正在跳转至人物详情：${personName}`);
}

function jumpToAllusionDetail(allusionName) {
  router.push({ path: '/allusion', query: { keyword: allusionName } });
  ElMessage.success(`正在跳转至典故详情：${allusionName}`);
}

function jumpToDynastyDetail(dynastyName) {
  const dynasty = Object.values(dynastyMap).find(item => item.name === dynastyName);
  
  if (dynasty) {
    router.push({ 
      path: '/timeline', 
      query: { 
        name: dynasty.name
      } 
    });
    ElMessage.success(`正在跳转至朝代详情：${dynastyName}`);
  } else {
    ElMessage.error(`未找到朝代：${dynastyName}`);
  }
}

onMounted(async () => {
  // 先取朝代基础数据，再按真实朝代ID加载事件与关联数据源
  await loadDynastyList();
  await loadAllEvents();
  await loadRelationSources();
  
  const eventId = route.query.id;
  
  if (eventId) {
    await loadEventDetail(Number(eventId));
    const eventIndex = sortedEvents.value.findIndex(e => String(e.id) === String(eventId));
    if (eventIndex !== -1) {
      activeEventIndex.value = eventIndex;
      setTimeout(() => {
        scrollToCard(eventIndex, false);
        triggerNavBounce();
      }, 300);
    }
  } else if (allEvents.value.length > 0) {
    await loadEventDetail(allEvents.value[0].id);
    activeEventIndex.value = 0;
    setTimeout(() => {
      scrollToCard(0, false);
      triggerNavBounce();
    }, 300);
  }
  
  nextTick(() => {
    if (scrollWrapper.value && track.value) {
      if (activeEventIndex.value === 0) {
        scrollToCard(0, false);
      }
      setTimeout(() => {
        if (activeEventIndex.value === 0) triggerNavBounce();
        updateActiveState();
      }, 200);
      scrollWrapper.value.addEventListener('scroll', onScroll);
    }
    
    if (recommendScrollWrapper.value && recommendTrack.value) {
      recommendScrollWrapper.value.addEventListener('scroll', onRecommendScroll);
      setTimeout(updateRecommendScrollState, 100);
    }
    
    // 修复：原代码此处添加的是匿名函数，卸载时无法移除，导致监听泄漏
    window.addEventListener('resize', onWindowResize);
  });
});

onUnmounted(() => {
  if (scrollWrapper.value) {
    scrollWrapper.value.removeEventListener('scroll', onScroll);
  }
  if (recommendScrollWrapper.value) {
    recommendScrollWrapper.value.removeEventListener('scroll', onRecommendScroll);
  }
  window.removeEventListener('resize', onWindowResize);
  if (bounceTimer) clearTimeout(bounceTimer);
});
</script>

<style scoped>
/* ===== 主题变量：暖米色 + 金色（全历史高级质感） ===== */
.event-detail-page {
  --paper: #f7f2ea;
  --paper-soft: #f9f5f0;
  --card: #fffdf9;
  --ink: #3b2f24;
  --ink-soft: #6b5a45;
  --muted: #9c8a74;
  --gold: #c9a227;
  --gold-soft: #d8b467;
  --line: rgba(201, 162, 39, 0.22);
  min-height: 100vh;
  background:
    radial-gradient(1200px 520px at 50% -240px, rgba(216, 180, 103, 0.20), transparent 70%),
    linear-gradient(180deg, var(--paper-soft) 0%, var(--paper) 100%);
  color: var(--ink);
}

.detail-container {
  max-width: 1400px;
  margin: 0 auto;
  padding: 20px 20px 60px;
}

/* ===== Hero 标题区 ===== */
.hero-section {
  position: relative;
  padding: 74px 24px 64px;
  text-align: center;
  border-radius: 26px;
  background: linear-gradient(160deg, rgba(216, 180, 103, 0.22) 0%, rgba(255, 253, 249, 0.66) 55%, rgba(247, 242, 234, 0) 100%);
  border: 1px solid var(--line);
  box-shadow: 0 20px 44px rgba(122, 92, 44, 0.10);
  overflow: hidden;
}

.hero-content {
  position: relative;
  z-index: 2;
}

.hero-title {
  font-size: 46px;
  font-weight: 600;
  letter-spacing: 2px;
  margin: 16px 0 14px;
  background: linear-gradient(135deg, #8B4513 0%, var(--gold) 55%, var(--gold-soft) 100%);
  -webkit-background-clip: text;
  background-clip: text;
  color: transparent;
}

.hero-subtitle {
  font-size: 15px;
  color: var(--ink-soft);
  letter-spacing: 3px;
  margin: 0 0 22px;
}

.hero-stats {
  display: flex;
  justify-content: center;
  gap: 54px;
  margin-top: 26px;
}

.hero-stats .stat-item {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.hero-stats .stat-number {
  font-size: 30px;
  font-weight: 700;
  color: var(--gold);
  line-height: 1.15;
}

.hero-stats .stat-label {
  font-size: 13px;
  color: var(--muted);
  margin-top: 4px;
  letter-spacing: 1px;
}

.hero-wave {
  position: absolute;
  left: 0;
  bottom: -1px;
  width: 100%;
  height: 56px;
  background: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 1440 320'%3E%3Cpath fill='%23f7f2ea' fill-opacity='1' d='M0,96L48,112C96,128,192,160,288,160C384,160,480,128,576,122.7C672,117,768,139,864,154.7C960,171,1056,181,1152,165.3C1248,149,1344,107,1392,85.3L1440,64L1440,320L1392,320C1344,320,1248,320,1152,320C1056,320,960,320,864,320C768,320,672,320,576,320C480,320,384,320,288,320C192,320,96,320,48,320L0,320Z'%3E%3C/path%3E%3C/svg%3E") no-repeat bottom;
  background-size: cover;
  z-index: 1;
  opacity: 0.9;
}

/* 时间轴包装器 */
.timeline-wrapper {
  position: relative;
  z-index: 3;
  margin: -30px 0 34px;
}

.timeline-container {
  position: relative;
  width: 100%;
  background: rgba(255, 253, 249, 0.88);
  backdrop-filter: blur(10px);
  border-radius: 22px;
  padding: 26px 24px 30px;
  border: 1px solid var(--line);
  box-shadow: 0 18px 40px rgba(122, 92, 44, 0.12), 0 2px 6px rgba(122, 92, 44, 0.05);
}

.timeline-header {
  text-align: center;
  margin-bottom: 18px;
}

.timeline-title {
  font-size: 26px;
  font-weight: 600;
  letter-spacing: 3px;
  margin: 0;
  color: var(--ink);
}

.timeline-subtitle {
  font-size: 12px;
  letter-spacing: 2px;
  margin-top: 6px;
  color: var(--muted);
}

.horizontal-scroll-wrapper {
  overflow-x: auto;
  overflow-y: hidden;
  scroll-behavior: smooth;
  padding-bottom: 15px;
  margin: 0 -8px;
  scrollbar-width: none;
}

.horizontal-scroll-wrapper::-webkit-scrollbar {
  display: none;
}

.timeline-track {
  display: flex;
  gap: 30px;
  padding: 15px 0;
  align-items: stretch;
}

.spacer-box {
  flex: 0 0 calc(50% - 180px);
  min-width: 80px;
  pointer-events: none;
  opacity: 0;
}

.timeline-card {
  flex: 0 0 280px;
  background: var(--card);
  border-radius: 18px;
  overflow: hidden;
  transition: all 0.4s cubic-bezier(0.2, 0.9, 0.4, 1.1);
  border: 1px solid var(--line);
  box-shadow: 0 8px 20px rgba(122, 92, 44, 0.10);
  opacity: 0.55;
  transform: scale(0.92);
  cursor: pointer;
}

.timeline-card.active {
  opacity: 1;
  transform: scale(1);
  border-color: var(--gold);
  box-shadow: 0 18px 36px rgba(122, 92, 44, 0.20), 0 0 0 2px rgba(201, 162, 39, 0.35);
}

.card-image-wrapper {
  position: relative;
  overflow: hidden;
  height: 140px;
  background: linear-gradient(135deg, #efe3cf, #e6d6bd);
}

.card-event-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}

.event-badge {
  position: absolute;
  top: 12px;
  left: 12px;
  background: rgba(255, 253, 249, 0.9);
  backdrop-filter: blur(4px);
  padding: 4px 10px;
  border-radius: 20px;
  font-size: 15px;
  box-shadow: 0 4px 10px rgba(122, 92, 44, 0.18);
}

.card-content {
  padding: 15px;
  text-align: center;
}

.event-name {
  font-size: 18px;
  font-weight: 600;
  margin: 0 0 5px 0;
  color: #8B4513;
}

.event-year {
  font-size: 12px;
  color: var(--muted);
  margin-bottom: 8px;
}

.event-desc {
  font-size: 12px;
  line-height: 1.5;
  color: var(--ink-soft);
  margin: 0;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.custom-scrollbar {
  margin-top: 20px;
  padding: 0 15px;
}

.scrollbar-track {
  position: relative;
  height: 4px;
  background: rgba(201, 162, 39, 0.18);
  border-radius: 4px;
  cursor: pointer;
  margin-bottom: 12px;
}

.scrollbar-thumb {
  position: absolute;
  height: 100%;
  background: linear-gradient(90deg, var(--gold-soft), var(--gold));
  border-radius: 4px;
  cursor: grab;
  transition: left 0.05s linear;
  min-width: 30px;
}

.scrollbar-thumb:active {
  cursor: grabbing;
}

.scrollbar-steps {
  display: flex;
  align-items: flex-end;
  gap: 2px;
  height: 22px;
  margin-top: 4px;
}

.scrollbar-step {
  flex: 1 1 0;
  height: 6px;
  padding: 0;
  border: none;
  cursor: pointer;
  border-radius: 3px;
  background: rgba(201, 162, 39, 0.26);
  transition: height 0.22s ease, background 0.22s ease;
}

.scrollbar-step:hover {
  background: rgba(201, 162, 39, 0.55);
  height: 12px;
}

.scrollbar-step.active-step {
  height: 18px;
  background: linear-gradient(180deg, var(--gold-soft), var(--gold));
  box-shadow: 0 0 8px rgba(201, 162, 39, 0.55);
}

/* ===== 视图切换按钮 ===== */
.timeline-toolbar {
  display: flex;
  justify-content: flex-end;
  margin-bottom: 12px;
}

.mode-btn {
  padding: 7px 16px;
  border-radius: 20px;
  border: 1px solid var(--gold);
  background: rgba(201, 162, 39, 0.12);
  color: #8B4513;
  font-size: 13px;
  letter-spacing: 1px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.mode-btn:hover {
  background: rgba(201, 162, 39, 0.24);
}

.mode-btn.active {
  background: linear-gradient(135deg, var(--gold-soft), var(--gold));
  color: #fff;
  border-color: transparent;
  box-shadow: 0 8px 18px rgba(201, 162, 39, 0.30);
}

/* ===== 全部事件列表 ===== */
.event-list-panel {
  padding: 2px 0 6px;
}

.event-list-search {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 16px;
  border-radius: 16px;
  border: 1px solid var(--line);
  background: rgba(255, 253, 249, 0.9);
  box-shadow: inset 0 1px 3px rgba(122, 92, 44, 0.06);
}

.event-list-search .search-icon {
  font-size: 15px;
  opacity: 0.7;
}

.event-list-search input {
  flex: 1;
  border: none;
  outline: none;
  background: transparent;
  font-size: 14px;
  color: var(--ink);
}

.event-list-search input::placeholder {
  color: var(--muted);
}

.search-clear {
  border: none;
  background: transparent;
  color: var(--muted);
  font-size: 18px;
  line-height: 1;
  cursor: pointer;
  padding: 0 2px;
}

.search-clear:hover {
  color: var(--gold);
}

.event-list-count {
  margin: 10px 2px 12px;
  font-size: 12px;
  color: var(--muted);
  letter-spacing: 1px;
}

.event-list-count b {
  color: var(--gold);
  font-size: 14px;
}

.event-list-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 10px;
  max-height: 360px;
  overflow-y: auto;
  padding: 2px;
}

.event-list-item {
  display: flex;
  flex-direction: column;
  gap: 3px;
  text-align: left;
  padding: 12px 14px;
  border-radius: 14px;
  border: 1px solid var(--line);
  background: rgba(255, 253, 249, 0.9);
  cursor: pointer;
  transition: all 0.25s ease;
}

.event-list-item:hover {
  transform: translateY(-2px);
  border-color: var(--gold);
  box-shadow: 0 10px 22px rgba(122, 92, 44, 0.14);
}

.event-list-item.active {
  border-color: var(--gold);
  background: rgba(216, 180, 103, 0.16);
  box-shadow: 0 0 0 1px rgba(201, 162, 39, 0.35);
}

.event-list-item .eli-year {
  font-size: 11px;
  font-weight: 700;
  color: #a9791a;
}

.event-list-item .eli-name {
  font-size: 15px;
  font-weight: 600;
  color: var(--ink);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.event-list-item .eli-dynasty {
  font-size: 11px;
  color: var(--muted);
}

/* 面包屑导航（玻璃信息条） */
.breadcrumb {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 4px;
  margin-bottom: 18px;
  padding: 6px 16px;
  border-radius: 20px;
  background: rgba(255, 253, 249, 0.7);
  border: 1px solid var(--line);
  color: var(--muted);
  font-size: 13px;
}

.breadcrumb a {
  color: #8B4513;
  text-decoration: none;
  transition: color 0.25s ease;
}

.breadcrumb a:hover {
  color: var(--gold);
}

.breadcrumb .separator {
  margin: 0 8px;
  color: #c3b39a;
}

.breadcrumb .current {
  color: var(--ink);
  font-weight: 600;
}

/* Hero 元信息标签 */
.event-meta {
  display: flex;
  justify-content: center;
  gap: 12px;
  flex-wrap: wrap;
}

.event-meta .event-type,
.event-meta .event-year,
.event-meta .event-dynasty {
  display: inline-flex;
  align-items: center;
  padding: 5px 14px;
  border-radius: 20px;
  margin-bottom: 0;
  font-size: 12px;
  font-weight: 600;
  letter-spacing: 1px;
  background: rgba(216, 180, 103, 0.16);
  color: var(--ink-soft);
}

.event-meta .event-type.political {
  background: rgba(201, 90, 74, 0.16);
  color: #b3543f;
}

.event-meta .event-type.military {
  background: rgba(201, 150, 39, 0.18);
  color: #a9791a;
}

.event-meta .event-type.cultural {
  background: rgba(96, 133, 76, 0.16);
  color: #5b7a45;
}

/* 朝代卡片 */
.dynasty-card {
  background: linear-gradient(135deg, rgba(216, 180, 103, 0.16), rgba(255, 253, 249, 0.9));
  border: 1px solid var(--line);
  border-radius: 18px;
  margin-bottom: 34px;
  cursor: pointer;
  transition: all 0.3s ease;
  overflow: hidden;
  box-shadow: 0 10px 26px rgba(122, 92, 44, 0.10);
}

.dynasty-card:hover {
  transform: translateY(-4px);
  border-color: var(--gold);
  box-shadow: 0 18px 36px rgba(122, 92, 44, 0.18);
}

.dynasty-card-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 22px 30px;
}

.dynasty-info h3 {
  color: #8B4513;
  font-size: 24px;
  margin: 0 0 10px 0;
}

.dynasty-period {
  color: var(--muted);
  font-size: 14px;
  margin-bottom: 10px;
}

.dynasty-details {
  display: flex;
  gap: 20px;
  color: var(--ink-soft);
  font-size: 14px;
}

.card-arrow {
  font-size: 30px;
  color: var(--gold);
  transition: transform 0.3s;
}

.dynasty-card:hover .card-arrow {
  transform: translateX(10px);
}

/* 内容布局 */
.content-wrapper {
  display: flex;
  gap: 26px;
  align-items: flex-start;
}

.main-content {
  flex: 2;
  min-width: 0;
}

.sidebar {
  flex: 1;
  min-width: 290px;
}

/* 简单图片容器 */
.image-container {
  margin-bottom: 28px;
  border-radius: 18px;
  overflow: hidden;
  box-shadow: 0 14px 32px rgba(122, 92, 44, 0.16);
  border: 1px solid var(--line);
  position: relative;
  background: #efe3cf;
}

.event-image-simple {
  width: 100%;
  height: 400px;
  object-fit: cover;
  display: block;
}

.image-caption {
  height: 38px;
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  background: linear-gradient(180deg, rgba(59, 47, 36, 0) 0%, rgba(59, 47, 36, 0.72) 100%);
  color: #fff;
  padding: 10px;
  text-align: center;
  font-size: 14px;
  letter-spacing: 1px;
}

/* 富文本内容 */
.rich-content {
  background: var(--card);
  border-radius: 18px;
  padding: 30px;
  margin-bottom: 28px;
  border: 1px solid var(--line);
  box-shadow: 0 10px 26px rgba(122, 92, 44, 0.10);
}

.content-section {
  margin-bottom: 26px;
}

.content-section:last-child {
  margin-bottom: 0;
}

.content-section h2 {
  color: #8B4513;
  font-size: 20px;
  margin: 0 0 15px;
  padding-bottom: 10px;
  border-bottom: 1px solid var(--line);
  position: relative;
}

.content-section h2::after {
  content: '';
  position: absolute;
  left: 0;
  bottom: -1px;
  width: 56px;
  height: 2px;
  background: linear-gradient(90deg, var(--gold), var(--gold-soft));
  border-radius: 2px;
}

.content-text {
  color: var(--ink);
  line-height: 1.9;
  font-size: 15px;
}

.content-text :deep(p) {
  margin: 0 0 12px;
}

.content-text :deep(h1),
.content-text :deep(h2),
.content-text :deep(h3),
.content-text :deep(h4) {
  color: #8B4513;
  margin: 16px 0 10px;
  line-height: 1.4;
}

.content-text :deep(ul),
.content-text :deep(ol) {
  padding-left: 22px;
  margin: 0 0 12px;
}

.content-text :deep(li) {
  margin-bottom: 6px;
}

.content-text :deep(a) {
  color: var(--gold);
}

.content-text :deep(img) {
  max-width: 100%;
  border-radius: 12px;
}

.content-text :deep(strong),
.content-text :deep(b) {
  color: #8B4513;
}

.content-text :deep(blockquote) {
  margin: 0 0 12px;
  padding: 8px 14px;
  border-left: 3px solid var(--gold-soft);
  background: rgba(216, 180, 103, 0.12);
  border-radius: 8px;
  color: var(--ink-soft);
}

/* 数据统计卡片 */
.stats-cards {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 14px;
  margin-bottom: 28px;
}

.stat-card {
  background: var(--card);
  border-radius: 16px;
  padding: 18px 14px;
  text-align: center;
  border: 1px solid var(--line);
  box-shadow: 0 8px 20px rgba(122, 92, 44, 0.08);
  transition: all 0.3s ease;
}

.stat-card:hover {
  transform: translateY(-4px);
  border-color: var(--gold);
  box-shadow: 0 16px 30px rgba(122, 92, 44, 0.16);
}

.stat-card .stat-icon {
  font-size: 26px;
  margin-bottom: 8px;
}

.stat-card .stat-number {
  font-size: 22px;
  font-weight: 700;
  color: var(--gold);
  margin-bottom: 4px;
}

.stat-card .stat-label {
  color: var(--muted);
  font-size: 11px;
  letter-spacing: 1px;
}

/* 侧边栏 */
.sidebar-section {
  background: var(--card);
  border-radius: 18px;
  padding: 20px;
  margin-bottom: 26px;
  border: 1px solid var(--line);
  box-shadow: 0 10px 26px rgba(122, 92, 44, 0.10);
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 1px solid var(--line);
  flex-wrap: wrap;
  gap: 8px;
}

.section-header h3 {
  color: #8B4513;
  font-size: 16px;
  margin: 0;
  letter-spacing: 1px;
}

.person-list, .allusion-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.person-card {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  background: rgba(216, 180, 103, 0.10);
  border: 1px solid rgba(201, 162, 39, 0.16);
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.3s;
}

.person-card:hover {
  background: rgba(216, 180, 103, 0.20);
  border-color: var(--gold);
  transform: translateX(4px);
  box-shadow: 0 8px 18px rgba(122, 92, 44, 0.12);
}

.person-info {
  flex: 1;
  min-width: 0;
}

.person-name {
  color: var(--ink);
  font-size: 14px;
  font-weight: 600;
  margin-bottom: 3px;
}

.person-role {
  color: var(--muted);
  font-size: 11px;
  margin-bottom: 5px;
}

.relation-tag {
  display: inline-block;
  padding: 2px 8px;
  border-radius: 10px;
  font-size: 10px;
  font-weight: 500;
}

.relation-tag.leader {
  background: rgba(201, 90, 74, 0.16);
  color: #b3543f;
}

.relation-tag.participant {
  background: rgba(201, 150, 39, 0.18);
  color: #a9791a;
}

.relation-tag.influenced {
  background: rgba(96, 133, 76, 0.16);
  color: #5b7a45;
}

.allusion-card {
  padding: 14px;
  background: rgba(216, 180, 103, 0.08);
  border: 1px solid rgba(201, 162, 39, 0.16);
  border-radius: 12px;
  transition: all 0.3s;
}

.allusion-card:hover {
  border-color: var(--gold);
  box-shadow: 0 10px 22px rgba(122, 92, 44, 0.12);
}

.allusion-name {
  color: #8B4513;
  font-size: 14px;
  font-weight: 600;
  margin-bottom: 6px;
}

.allusion-meaning {
  color: var(--ink-soft);
  font-size: 12px;
  line-height: 1.5;
  margin-bottom: 8px;
}

.allusion-relation {
  display: flex;
  gap: 8px;
  margin-bottom: 10px;
}

.relation-badge, .difficulty-badge {
  padding: 2px 8px;
  border-radius: 10px;
  font-size: 10px;
  font-weight: 500;
}

.relation-badge.origin {
  background: rgba(96, 133, 76, 0.16);
  color: #5b7a45;
}

.relation-badge.background {
  background: rgba(201, 150, 39, 0.18);
  color: #a9791a;
}

.relation-badge.reflection {
  background: rgba(201, 90, 74, 0.16);
  color: #b3543f;
}

.relation-badge.era {
  background: rgba(108, 122, 201, 0.18);
  color: #5a68b0;
}

.difficulty-badge.easy {
  background: rgba(96, 133, 76, 0.16);
  color: #5b7a45;
}

.difficulty-badge.medium {
  background: rgba(201, 150, 39, 0.18);
  color: #a9791a;
}

.difficulty-badge.hard {
  background: rgba(201, 90, 74, 0.16);
  color: #b3543f;
}

.detail-btn {
  padding: 5px 12px;
  background: rgba(201, 162, 39, 0.14);
  border: 1px solid var(--gold);
  border-radius: 14px;
  color: #8B4513;
  font-size: 11px;
  cursor: pointer;
  transition: all 0.3s;
}

.detail-btn:hover {
  background: var(--gold);
  color: #fff;
}

.empty-state {
  text-align: center;
  color: var(--muted);
  padding: 22px;
  font-size: 12px;
}

/* 推荐事件横向滚动区域 */
.recommend-events-section {
  overflow: hidden;
}

.recommend-nav-buttons {
  display: flex;
  gap: 8px;
}

.recommend-nav-btn {
  width: 28px;
  height: 28px;
  border-radius: 50%;
  background: rgba(201, 162, 39, 0.14);
  border: 1px solid var(--gold);
  color: #8B4513;
  font-size: 16px;
  cursor: pointer;
  transition: all 0.3s;
  display: flex;
  align-items: center;
  justify-content: center;
}

.recommend-nav-btn:hover:not(:disabled) {
  background: var(--gold);
  color: #fff;
}

.recommend-nav-btn:disabled {
  opacity: 0.3;
  cursor: not-allowed;
}

.recommend-scroll-wrapper {
  overflow-x: auto;
  overflow-y: hidden;
  scroll-behavior: smooth;
  margin: 0 -8px;
  padding: 5px 0 10px;
  scrollbar-width: none;
}

.recommend-scroll-wrapper::-webkit-scrollbar {
  display: none;
}

.recommend-track {
  display: flex;
  gap: 15px;
  padding: 0;
  align-items: stretch;
}

/* 推荐区域占位盒子 */
.recommend-spacer-box {
  flex: 0 0 calc(50% - 100px);
  min-width: 60px;
  pointer-events: none;
  opacity: 0;
}

.left-recommend-spacer {
  order: 0;
}

.right-recommend-spacer {
  order: 2;
}

/* 推荐卡片 */
.recommend-card-horizontal {
  order: 1;
  flex: 0 0 160px;
  background: linear-gradient(135deg, rgba(216, 180, 103, 0.16), rgba(255, 253, 249, 0.9));
  border: 1px solid rgba(201, 162, 39, 0.22);
  border-radius: 14px;
  padding: 12px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.recommend-card-horizontal:hover {
  transform: translateY(-4px);
  border-color: var(--gold);
  box-shadow: 0 12px 24px rgba(122, 92, 44, 0.16);
}

.recommend-card-inner {
  text-align: center;
}

.recommend-year-badge {
  font-size: 11px;
  color: #a9791a;
  margin-bottom: 8px;
  font-weight: 700;
}

.recommend-name {
  font-size: 14px;
  font-weight: 600;
  color: var(--ink);
  margin-bottom: 4px;
}

.recommend-dynasty {
  font-size: 11px;
  color: var(--muted);
  margin-bottom: 6px;
}

.recommend-relation-reason {
  font-size: 10px;
  color: #8B4513;
  background: rgba(201, 162, 39, 0.16);
  display: inline-block;
  padding: 2px 8px;
  border-radius: 12px;
}

.recommend-scrollbar {
  margin-top: 12px;
  padding: 0 8px;
}

.recommend-scrollbar-track {
  position: relative;
  height: 4px;
  background: rgba(201, 162, 39, 0.18);
  border-radius: 4px;
  cursor: pointer;
}

.recommend-scrollbar-thumb {
  position: absolute;
  height: 100%;
  background: linear-gradient(90deg, var(--gold-soft), var(--gold));
  border-radius: 4px;
  cursor: grab;
  transition: left 0.05s linear;
  min-width: 30px;
}

.recommend-scrollbar-thumb:active {
  cursor: grabbing;
}

.recommend-steps {
  display: flex;
  align-items: flex-end;
  gap: 1.5px;
  height: 18px;
  margin-top: 10px;
}

.recommend-step {
  flex: 1 1 0;
  height: 5px;
  padding: 0;
  border: none;
  cursor: pointer;
  border-radius: 2px;
  background: rgba(201, 162, 39, 0.24);
  transition: height 0.22s ease, background 0.22s ease;
}

.recommend-step:hover {
  background: rgba(201, 162, 39, 0.55);
  height: 10px;
}

.recommend-step.active-recommend-step {
  height: 15px;
  background: linear-gradient(180deg, var(--gold-soft), var(--gold));
  box-shadow: 0 0 6px rgba(201, 162, 39, 0.5);
}

@media (max-width: 1024px) {
  .stats-cards {
    grid-template-columns: repeat(2, 1fr);
  }

  .content-wrapper {
    gap: 20px;
  }
}

@media (max-width: 768px) {
  .content-wrapper {
    flex-direction: column;
  }

  .stats-cards {
    grid-template-columns: repeat(2, 1fr);
  }

  .hero-section {
    padding: 58px 16px 56px;
  }

  .hero-title {
    font-size: 30px;
  }

  .hero-stats {
    gap: 26px;
  }

  .timeline-title {
    font-size: 20px;
  }

  .timeline-card {
    flex: 0 0 240px;
  }

  .spacer-box {
    flex: 0 0 calc(50% - 120px);
    min-width: 50px;
  }

  .recommend-card-horizontal {
    flex: 0 0 140px;
  }

  .recommend-spacer-box {
    flex: 0 0 calc(50% - 80px);
    min-width: 40px;
  }

  .dynasty-details {
    flex-direction: column;
    gap: 6px;
  }

  .dynasty-card-content {
    flex-direction: column;
    text-align: center;
    gap: 10px;
  }

  .card-arrow {
    display: none;
  }

  .rich-content {
    padding: 22px;
  }
}
</style>