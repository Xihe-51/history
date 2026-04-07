<template>
  <div class="event-detail-page">
    <!-- 顶部导航栏 -->
    <top-component ref="topNavRef" class="top-nav" />
    
    <div class="detail-container">
      <!-- 横向时间轴 - 放在面包屑导航上方 -->
      <div class="timeline-wrapper">
        <div class="timeline-container">
          <div class="timeline-header">
            <h2 class="timeline-title">📅 历史事件时间轴</h2>
            <h3 class="timeline-subtitle">从远古到近代 · 重要历史事件</h3>
          </div>
          
          <!-- 横向滚动区域 -->
          <div class="horizontal-scroll-wrapper" ref="scrollWrapper" @wheel="handleWheel">
            <div class="timeline-track" ref="track">
              <!-- 左侧隐形占位盒子 -->
              <div class="spacer-box left-spacer"></div>
              
              <!-- 实际事件卡片 - 按时间从早到晚排序 -->
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
          <div class="custom-scrollbar">
            <div class="scrollbar-track" ref="scrollbarTrack" @click="handleTrackClick">
              <div 
                class="scrollbar-thumb" 
                ref="scrollbarThumb"
                :style="{ left: thumbPosition + '%', width: thumbWidth + '%' }"
                @mousedown="startDrag"
              ></div>
            </div>
            <div class="scrollbar-steps">
              <div 
                v-for="(event, idx) in sortedEvents" 
                :key="idx"
                class="scrollbar-step"
                :class="{ 'active-step': activeEventIndex === idx }"
                @click="scrollToCard(idx, true)"
              >
                <span class="step-label">{{ event.eventName }}</span>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 面包屑导航 -->
      <div class="breadcrumb">
        <router-link to="/timeline">时间轴</router-link>
        <span class="separator">></span>
        <router-link to="/event">事件科普</router-link>
        <span class="separator">></span>
        <span class="current">{{ currentEvent.eventName }}</span>
      </div>

      <!-- 事件标题区域 -->
      <div class="event-header">
        <h1 class="event-title">{{ currentEvent.eventName }}</h1>
        <div class="event-meta">
          <span class="event-type" :class="currentEvent.eventType || 'political'">{{ getEventTypeLabel(currentEvent.eventType) }}</span>
          <span class="event-year">{{ formatYear(currentEvent.eventYear) }}</span>
          <span class="event-dynasty">{{ currentEvent.dynastyName || getDynastyName(currentEvent.dynastyId) }}</span>
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
          <!-- 事件配图轮播（暂用占位图） -->
          <div class="image-carousel">
            <el-carousel :interval="5000" arrow="always" height="400px">
              <el-carousel-item>
                <div class="image-placeholder">
                  <div class="placeholder-icon">🏛️</div>
                  <div class="placeholder-text">{{ currentEvent.eventName }}</div>
                </div>
                <div class="image-caption">{{ currentEvent.eventName }} 历史场景</div>
              </el-carousel-item>
            </el-carousel>
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
              <div class="stat-number">{{ currentEvent.relatedPersons?.length || 0 }}</div>
              <div class="stat-label">关联人物</div>
            </div>
            <div class="stat-card">
              <div class="stat-icon">📖</div>
              <div class="stat-number">{{ currentEvent.relatedAllusions?.length || 0 }}</div>
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
            <div v-if="!currentEvent.relatedPersons?.length" class="empty-state">暂无关联人物</div>
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
            <div class="recommend-steps">
              <div 
                v-for="(event, idx) in allRecommendedEvents" 
                :key="idx"
                class="recommend-step"
                :class="{ 'active-recommend-step': activeRecommendIndex === idx }"
                @click="scrollToRecommendCard(idx)"
              >
                <span class="recommend-step-label">{{ event.eventName.slice(0, 4) }}</span>
              </div>
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
import request from '@/utils/request';

const router = useRouter();
const route = useRoute();

// 朝代信息映射表
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

// 根据 dynastyId 获取朝代名称
function getDynastyName(dynastyId) {
  return dynastyMap[dynastyId]?.name || "未知朝代";
}

// 获取朝代时期
function getDynastyPeriod(dynastyName) {
  const dynasty = Object.values(dynastyMap).find(d => d.name === dynastyName);
  return dynasty?.period || "未知时期";
}

// 获取朝代都城
function getDynastyCapital(dynastyName) {
  const dynasty = Object.values(dynastyMap).find(d => d.name === dynastyName);
  return dynasty?.capital || "未知";
}

// 获取朝代创始人
function getDynastyFounder(dynastyName) {
  const dynasty = Object.values(dynastyMap).find(d => d.name === dynastyName);
  return dynasty?.founder || "未知";
}

// 从后端获取的事件数据
const allEvents = ref([]);
const isLoading = ref(false);

// 当前事件详情
const currentEvent = ref({});

// 按时间从早到晚排序
const sortedEvents = computed(() => {
  return [...allEvents.value].sort((a, b) => (a.eventYear || 0) - (b.eventYear || 0));
});

// 所有推荐事件（排除当前事件）
const allRecommendedEvents = computed(() => {
  return allEvents.value.filter(e => e.id !== currentEvent.value.id);
});

// 时间轴状态
const activeEventIndex = ref(0);
const isNavBouncing = ref(false);
const scrollWrapper = ref(null);
const track = ref(null);
const scrollbarThumb = ref(null);
const scrollbarTrack = ref(null);

// 推荐事件滚动状态
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

// 计算滑块位置和宽度
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

// 推荐事件滚动条位置
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

// 获取推荐原因
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

// 获取事件类型图标
function getEventTypeIcon(type) {
  const icons = { political: '🏛️', military: '⚔️', cultural: '📚' };
  return icons[type] || '📜';
}

// 触发导航栏跳动动画
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

// 查找当前激活的事件卡片
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

// 更新激活状态
function updateActiveState() {
  if (!scrollWrapper.value) return;
  
  const newIndex = findActiveCardByPosition();
  
  if (newIndex !== activeEventIndex.value) {
    activeEventIndex.value = newIndex;
    triggerNavBounce();
  }
}

// 滚动到指定卡片
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

// 跳转到事件详情
async function jumpToEvent(eventId) {
  try {
    isLoading.value = true;
    const res = await request.get(`/historical-event/${eventId}`);
    currentEvent.value = res.data.data;
    
    const eventIndex = sortedEvents.value.findIndex(e => e.id === eventId);
    
    if (eventIndex !== -1) {
      activeEventIndex.value = eventIndex;
      triggerNavBounce();
      scrollToCard(eventIndex, true);
    }
    
    window.scrollTo({ top: 0, behavior: 'smooth' });
    ElMessage.success(`切换到事件：${currentEvent.value.eventName}`);
  } catch (error) {
    console.error('加载事件详情失败:', error);
    ElMessage.error('加载事件详情失败');
  } finally {
    isLoading.value = false;
  }
}

// 加载所有事件列表
async function loadAllEvents() {
  try {
    isLoading.value = true;
    console.log('=== loadAllEvents 开始 ===');
    
    const dynastyIds = [1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14];
    console.log('要查询的朝代ID:', dynastyIds);
    
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
    console.log('所有请求完成，结果数量:', results.length);
    
    results.forEach((res, index) => {
      console.log(`--- 处理朝代 ${dynastyIds[index]} 的响应 ---`);
      console.log('响应对象:', res);
      
      if (res && res.data && res.data.code === 200) {
        console.log('res.data.code 是 200');
        const events = res.data.data?.records || [];
        console.log(`找到 ${events.length} 条事件`);
        
        if (events.length > 0) {
          console.log('事件列表:', events);
          events.forEach(event => {
            event.dynastyName = event.dynastyName || getDynastyName(dynastyIds[index]);
          });
          allEventsData = [...allEventsData, ...events];
        }
      } else {
        console.log(`朝代 ${dynastyIds[index]} 响应不是200或响应为空`);
      }
    });
    
    console.log('合并后的事件总数:', allEventsData.length);
    
    allEvents.value = allEventsData.sort((a, b) => (a.eventYear || 0) - (b.eventYear || 0));
    console.log('事件列表加载成功，共', allEvents.value.length, '条');
    
    if (allEvents.value.length === 0) {
      ElMessage.warning('暂无事件数据');
    }
    
  } catch (error) {
    console.error('加载事件列表失败:', error);
    ElMessage.error('加载事件列表失败');
  } finally {
    isLoading.value = false;
    console.log('=== loadAllEvents 结束 ===');
  }
}

// 加载事件详情
async function loadEventDetail(eventId) {
  try {
    isLoading.value = true;
    const res = await request.get(`/historical-event/${eventId}`);
    currentEvent.value = res.data.data;
    console.log('事件详情加载成功:', currentEvent.value.eventName);
  } catch (error) {
    console.error('加载事件详情失败:', error);
    ElMessage.error('加载事件详情失败');
  } finally {
    isLoading.value = false;
  }
}

// 推荐事件滚动相关函数
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

// 拖拽滑块
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

// 点击滚动条轨道
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

// 滚轮处理
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

// 滚动事件监听
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

// 推荐事件滚动监听
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

const eventDuration = computed(() => "1年");

const sortedPersons = computed(() => {
  const persons = [...(currentEvent.value.relatedPersons || [])];
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
  let allusions = [...(currentEvent.value.relatedAllusions || [])];
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
  const labels = { origin: '源于该事件', background: '反映背景', reflection: '历史映照' };
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

// 跳转到朝代详情
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

//onMounted生命周期
onMounted(async () => {
  console.log('=== onMounted 开始 ===');
  console.log('路由参数 route.query:', route.query);
  console.log('当前 token:', localStorage.getItem('token'));
  
  await loadAllEvents();
  
  console.log('loadAllEvents 执行完毕');
  console.log('allEvents.value.length:', allEvents.value.length);
  console.log('allEvents.value:', allEvents.value);
  
  const eventId = route.query.id;
  console.log('URL中的 eventId:', eventId);
  
  if (eventId) {
    console.log(`有指定事件ID: ${eventId}，加载详情`);
    await loadEventDetail(parseInt(eventId));
    
    const eventIndex = sortedEvents.value.findIndex(e => e.id === parseInt(eventId));
    console.log('找到的事件索引:', eventIndex);
    if (eventIndex !== -1) {
      activeEventIndex.value = eventIndex;
      setTimeout(() => {
        scrollToCard(eventIndex, false);
        triggerNavBounce();
      }, 300);
    }
  } else if (allEvents.value.length > 0) {
    console.log('没有指定事件ID，默认显示第一个事件:', allEvents.value[0]);
    await loadEventDetail(allEvents.value[0].id);
    activeEventIndex.value = 0;
    setTimeout(() => {
      scrollToCard(0, false);
      triggerNavBounce();
    }, 300);
  } else {
    console.log('没有事件数据，无法显示默认事件');
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
    
    window.addEventListener('resize', () => {
      updateActiveState();
      updateRecommendScrollState();
    });
  });
});

onUnmounted(() => {
  if (scrollWrapper.value) {
    scrollWrapper.value.removeEventListener('scroll', onScroll);
  }
  if (recommendScrollWrapper.value) {
    recommendScrollWrapper.value.removeEventListener('scroll', onRecommendScroll);
  }
  window.removeEventListener('resize', updateActiveState);
  if (bounceTimer) clearTimeout(bounceTimer);
});
</script>

<style scoped>
.event-detail-page {
  min-height: 100vh;
  background: linear-gradient(135deg, #1a1a1a 0%, #2d2d2d 100%);
  padding: 0;
}

.detail-container {
  max-width: 1400px;
  margin: 0 auto;
  padding: 20px 20px 60px;
}

/* 时间轴包装器 */
.timeline-wrapper {
  margin-bottom: 30px;
}

.timeline-container {
  position: relative;
  width: 100%;
  background: rgba(0, 0, 0, 0.5);
  backdrop-filter: blur(8px);
  border-radius: 24px;
  padding: 20px 20px 30px;
  border: 1px solid rgba(255, 215, 150, 0.3);
}

.timeline-header {
  text-align: center;
  margin-bottom: 20px;
}

.timeline-title {
  font-size: 28px;
  font-weight: 300;
  letter-spacing: 4px;
  margin: 0;
  color: #FFD700;
}

.timeline-subtitle {
  font-size: 12px;
  letter-spacing: 2px;
  opacity: 0.7;
  margin-top: 5px;
  color: #ccc;
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
  background: rgba(30, 30, 35, 0.9);
  backdrop-filter: blur(8px);
  border-radius: 20px;
  overflow: hidden;
  transition: all 0.4s cubic-bezier(0.2, 0.9, 0.4, 1.1);
  border: 1px solid rgba(255, 215, 150, 0.3);
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.3);
  opacity: 0.5;
  transform: scale(0.92);
  cursor: pointer;
}

.timeline-card.active {
  opacity: 1;
  transform: scale(1);
  border-color: #FFD700;
  box-shadow: 0 15px 30px rgba(0, 0, 0, 0.5), 0 0 0 2px rgba(255, 215, 0, 0.5);
}

.card-image-wrapper {
  position: relative;
  overflow: hidden;
  height: 140px;
}

.event-badge {
  position: absolute;
  top: 12px;
  left: 12px;
  background: rgba(0, 0, 0, 0.7);
  backdrop-filter: blur(4px);
  padding: 4px 10px;
  border-radius: 20px;
  font-size: 16px;
}

.card-content {
  padding: 15px;
  text-align: center;
}

.event-name {
  font-size: 18px;
  font-weight: 600;
  margin: 0 0 5px 0;
  color: #FFD700;
}

.event-year {
  font-size: 12px;
  color: #aaa;
  margin-bottom: 8px;
}

.event-desc {
  font-size: 12px;
  line-height: 1.5;
  color: #ddd;
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
  height: 3px;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 3px;
  cursor: pointer;
  margin-bottom: 12px;
}

.scrollbar-thumb {
  position: absolute;
  height: 100%;
  background: linear-gradient(90deg, #FFD700, #cfa668);
  border-radius: 3px;
  cursor: grab;
  transition: left 0.05s linear;
  min-width: 30px;
}

.scrollbar-thumb:active {
  cursor: grabbing;
}

.scrollbar-steps {
  display: flex;
  justify-content: space-between;
  gap: 3px;
  flex-wrap: wrap;
}

.scrollbar-step {
  flex: 1;
  text-align: center;
  padding: 5px 3px;
  cursor: pointer;
  transition: all 0.3s ease;
  border-radius: 4px;
  background: rgba(255, 255, 255, 0.05);
  min-width: 35px;
}

.scrollbar-step:hover {
  background: rgba(255, 215, 0, 0.2);
  transform: translateY(-1px);
}

.scrollbar-step.active-step {
  background: rgba(255, 215, 0, 0.3);
  box-shadow: 0 0 8px rgba(255, 215, 0, 0.3);
}

.step-label {
  font-size: 10px;
  font-weight: 500;
  color: #FFD700;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  display: block;
}

/* 面包屑导航 */
.breadcrumb {
  margin-bottom: 25px;
  color: #aaa;
  font-size: 13px;
}

.breadcrumb a {
  color: #FFD700;
  text-decoration: none;
}

.breadcrumb a:hover {
  color: #ffed4a;
}

.breadcrumb .separator {
  margin: 0 8px;
  color: #666;
}

.breadcrumb .current {
  color: #fff;
}

/* 事件标题区域 */
.event-header {
  margin-bottom: 25px;
  text-align: center;
}

.event-title {
  font-size: 42px;
  font-weight: 300;
  color: #FFD700;
  margin: 0 0 15px 0;
}

.event-meta {
  display: flex;
  justify-content: center;
  gap: 15px;
  flex-wrap: wrap;
}

.event-type {
  padding: 4px 12px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: bold;
}

.event-type.political {
  background: rgba(255, 77, 77, 0.3);
  color: #FF4D4D;
}

.event-type.military {
  background: rgba(255, 165, 0, 0.3);
  color: #FFA500;
}

.event-type.cultural {
  background: rgba(76, 175, 80, 0.3);
  color: #4CAF50;
}

.event-year, .event-dynasty {
  color: #ccc;
  font-size: 14px;
}

/* 朝代卡片 */
.dynasty-card {
  background: linear-gradient(135deg, rgba(255, 215, 0, 0.1), rgba(255, 215, 0, 0.05));
  border: 2px solid rgba(255, 215, 0, 0.3);
  border-radius: 16px;
  margin-bottom: 40px;
  cursor: pointer;
  transition: all 0.3s ease;
  overflow: hidden;
}

.dynasty-card:hover {
  transform: translateY(-5px);
  border-color: #FFD700;
  box-shadow: 0 10px 30px rgba(255, 215, 0, 0.2);
}

.dynasty-card-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 30px;
}

.dynasty-info h3 {
  color: #FFD700;
  font-size: 24px;
  margin: 0 0 10px 0;
}

.dynasty-period {
  color: #aaa;
  font-size: 14px;
  margin-bottom: 10px;
}

.dynasty-details {
  display: flex;
  gap: 20px;
  color: #ccc;
  font-size: 14px;
}

.card-arrow {
  font-size: 32px;
  color: #FFD700;
  transition: transform 0.3s;
}

.dynasty-card:hover .card-arrow {
  transform: translateX(10px);
}

/* 内容布局 */
.content-wrapper {
  display: flex;
  gap: 25px;
}

.main-content {
  flex: 2;
}

.sidebar {
  flex: 1;
  min-width: 280px;
}

/* 图片轮播 */
.image-carousel {
  margin-bottom: 30px;
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.3);
}

.image-caption {
  height: 35px;
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  background: rgba(0, 0, 0, 0.7);
  color: #fff;
  padding: 10px;
  text-align: center;
  font-size: 14px;
}

/* 富文本内容 */
.rich-content {
  background: rgba(0, 0, 0, 0.3);
  border-radius: 16px;
  padding: 25px;
  margin-bottom: 25px;
}

.content-section {
  margin-bottom: 25px;
}

.content-section h2 {
  color: #FFD700;
  font-size: 20px;
  margin-bottom: 15px;
  padding-bottom: 8px;
  border-bottom: 1px solid rgba(255, 215, 0, 0.3);
}

.content-text {
  color: #e0e0e0;
  line-height: 1.7;
  font-size: 14px;
}

/* 数据统计卡片 */
.stats-cards {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 12px;
  margin-bottom: 25px;
}

.stat-card {
  background: rgba(0, 0, 0, 0.4);
  border-radius: 12px;
  padding: 15px;
  text-align: center;
}

.stat-icon {
  font-size: 24px;
  margin-bottom: 8px;
}

.stat-number {
  font-size: 22px;
  font-weight: bold;
  color: #FFD700;
  margin-bottom: 4px;
}

.stat-label {
  color: #aaa;
  font-size: 11px;
}

/* 侧边栏 */
.sidebar-section {
  background: rgba(0, 0, 0, 0.3);
  border-radius: 16px;
  padding: 18px;
  margin-bottom: 25px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
  flex-wrap: wrap;
  gap: 8px;
}

.section-header h3 {
  color: #FFD700;
  font-size: 16px;
  margin: 0;
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
  padding: 10px;
  background: rgba(255, 255, 255, 0.05);
  border-radius: 10px;
  cursor: pointer;
  transition: all 0.3s;
}

.person-card:hover {
  background: rgba(255, 215, 0, 0.1);
  transform: translateX(3px);
}

.person-info {
  flex: 1;
}

.person-name {
  color: #fff;
  font-size: 14px;
  font-weight: bold;
  margin-bottom: 3px;
}

.person-role {
  color: #aaa;
  font-size: 11px;
  margin-bottom: 4px;
}

.relation-tag {
  display: inline-block;
  padding: 2px 6px;
  border-radius: 10px;
  font-size: 9px;
}

.relation-tag.leader {
  background: rgba(255, 77, 77, 0.3);
  color: #FF4D4D;
}

.relation-tag.participant {
  background: rgba(255, 165, 0, 0.3);
  color: #FFA500;
}

.relation-tag.influenced {
  background: rgba(76, 175, 80, 0.3);
  color: #4CAF50;
}

.allusion-card {
  padding: 12px;
  background: rgba(255, 255, 255, 0.05);
  border-radius: 10px;
}

.allusion-name {
  color: #FFD700;
  font-size: 14px;
  font-weight: bold;
  margin-bottom: 6px;
}

.allusion-meaning {
  color: #ccc;
  font-size: 11px;
  line-height: 1.4;
  margin-bottom: 8px;
}

.allusion-relation {
  display: flex;
  gap: 8px;
  margin-bottom: 10px;
}

.relation-badge, .difficulty-badge {
  padding: 2px 6px;
  border-radius: 10px;
  font-size: 9px;
}

.relation-badge.origin {
  background: rgba(76, 175, 80, 0.3);
  color: #4CAF50;
}

.relation-badge.background {
  background: rgba(255, 165, 0, 0.3);
  color: #FFA500;
}

.relation-badge.reflection {
  background: rgba(255, 77, 77, 0.3);
  color: #FF4D4D;
}

.difficulty-badge.easy {
  background: rgba(76, 175, 80, 0.3);
  color: #4CAF50;
}

.difficulty-badge.medium {
  background: rgba(255, 165, 0, 0.3);
  color: #FFA500;
}

.difficulty-badge.hard {
  background: rgba(255, 77, 77, 0.3);
  color: #FF4D4D;
}

.detail-btn {
  padding: 4px 10px;
  background: rgba(255, 215, 0, 0.2);
  border: 1px solid #FFD700;
  border-radius: 14px;
  color: #FFD700;
  font-size: 10px;
  cursor: pointer;
  transition: all 0.3s;
}

.detail-btn:hover {
  background: #FFD700;
  color: #1a1a1a;
}

.empty-state {
  text-align: center;
  color: #666;
  padding: 20px;
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
  background: rgba(255, 215, 0, 0.2);
  border: 1px solid #FFD700;
  color: #FFD700;
  font-size: 16px;
  cursor: pointer;
  transition: all 0.3s;
  display: flex;
  align-items: center;
  justify-content: center;
}

.recommend-nav-btn:hover:not(:disabled) {
  background: #FFD700;
  color: #1a1a1a;
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
  background: rgba(255, 215, 0, 0.1);
  border: 1px solid rgba(255, 215, 0, 0.3);
  border-radius: 12px;
  padding: 12px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.recommend-card-horizontal:hover {
  background: rgba(255, 215, 0, 0.2);
  transform: translateY(-3px);
  border-color: #FFD700;
}

.recommend-card-inner {
  text-align: center;
}

.recommend-year-badge {
  font-size: 11px;
  color: #FFD700;
  margin-bottom: 8px;
  font-weight: bold;
}

.recommend-name {
  font-size: 14px;
  font-weight: bold;
  color: #fff;
  margin-bottom: 4px;
}

.recommend-dynasty {
  font-size: 11px;
  color: #aaa;
  margin-bottom: 6px;
}

.recommend-relation-reason {
  font-size: 10px;
  color: #FFD700;
  background: rgba(255, 215, 0, 0.2);
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
  height: 3px;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 3px;
  cursor: pointer;
}

.recommend-scrollbar-thumb {
  position: absolute;
  height: 100%;
  background: linear-gradient(90deg, #FFD700, #cfa668);
  border-radius: 3px;
  cursor: grab;
  transition: left 0.05s linear;
  min-width: 30px;
}

.recommend-scrollbar-thumb:active {
  cursor: grabbing;
}

.recommend-steps {
  display: flex;
  justify-content: space-between;
  gap: 5px;
  margin-top: 10px;
  flex-wrap: wrap;
}

.recommend-step {
  flex: 1;
  text-align: center;
  padding: 4px 2px;
  cursor: pointer;
  transition: all 0.3s ease;
  border-radius: 4px;
  background: rgba(255, 255, 255, 0.05);
  min-width: 30px;
}

.recommend-step:hover {
  background: rgba(255, 215, 0, 0.2);
}

.recommend-step.active-recommend-step {
  background: rgba(255, 215, 0, 0.3);
  box-shadow: 0 0 6px rgba(255, 215, 0, 0.3);
}

.recommend-step-label {
  font-size: 9px;
  font-weight: 500;
  color: #FFD700;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  display: block;
}

@media (max-width: 768px) {
  .content-wrapper {
    flex-direction: column;
  }
  
  .stats-cards {
    grid-template-columns: repeat(2, 1fr);
  }
  
  .event-title {
    font-size: 28px;
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
  
  .step-label {
    font-size: 8px;
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
    gap: 5px;
  }
  
  .dynasty-card-content {
    flex-direction: column;
    text-align: center;
  }
  
  .card-arrow {
    display: none;
  }
}
</style>