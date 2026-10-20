<template>
  <div class="allusion-page">
    <!-- 顶部导航栏 -->
    <top-component 
      ref="topNavRef" 
      class="top-nav"
      :class="{ 'nav-active': activeDynastyIndex !== -1, 'nav-bounce': isNavBouncing }"
    />
    
    <div class="allusion-container">
      <!-- 页面标题区 -->
      <div class="hero-section">
        <div class="hero-content">
          <h1 class="hero-title">📚 典故科普</h1>
          <p class="hero-subtitle">探寻成语典故 · 品味中华智慧</p>
          <div class="hero-stats">
            <div class="stat-item">
              <span class="stat-number">{{ allAllusions.length }}</span>
              <span class="stat-label">典故总数</span>
            </div>
            <div class="stat-item">
              <span class="stat-number">{{ dynastyList.length }}</span>
              <span class="stat-label">涵盖朝代</span>
            </div>
            <div class="stat-item">
              <span class="stat-number">{{ totalPersons }}</span>
              <span class="stat-label">关联人物</span>
            </div>
          </div>
        </div>
        <div class="hero-wave"></div>
      </div>

      <!-- 筛选区域 -->
      <div class="filter-glass">
        <div class="filter-header">
          <span class="filter-icon">🔍</span>
          <span class="filter-title">典故筛选</span>
          <button class="reset-btn" @click="resetFilters">重置筛选</button>
        </div>
        
        <div class="filter-grid">
          <!-- 朝代筛选 -->
          <div class="filter-card">
            <div class="filter-label">📅 朝代</div>
            <div class="tag-group">
              <span 
                v-for="dynasty in dynastyList" 
                :key="dynasty.id"
                class="filter-tag"
                :class="{ active: filters.dynastyId === dynasty.id }"
                @click="toggleDynastyFilter(dynasty.id)"
              >
                {{ dynasty.name }}
              </span>
            </div>
          </div>

          <!-- 主题类型筛选 -->
          <div class="filter-card">
            <div class="filter-label">🎭 主题类型</div>
            <div class="tag-group">
              <span 
                v-for="theme in themeList" 
                :key="theme"
                class="filter-tag"
                :class="{ active: filters.theme === theme }"
                @click="toggleThemeFilter(theme)"
              >
                {{ theme }}
              </span>
            </div>
          </div>

          <!-- 难度筛选 -->
          <div class="filter-card">
            <div class="filter-label">⭐ 难度等级</div>
            <div class="tag-group">
              <span 
                v-for="level in difficultyList" 
                :key="level.value"
                class="filter-tag"
                :class="['difficulty-' + level.value, { active: filters.difficulty === level.value }]"
                @click="filters.difficulty = filters.difficulty === level.value ? '' : level.value"
              >
                {{ level.label }}
              </span>
            </div>
          </div>
        </div>

        <!-- 第二行筛选 -->
        <div class="filter-grid second-row">
          <!-- 搜索框 -->
          <div class="filter-card search-card">
            <div class="filter-label">🔎 关键词搜索</div>
            <div class="search-wrapper">
              <input 
                type="text" 
                v-model="filters.keyword" 
                placeholder="搜索典故名称、出处原文或释义..."
                @input="handleSearch"
                class="search-input-modern"
              >
              <span class="search-icon-modern">🔍</span>
              <button v-if="filters.keyword" class="clear-search" @click="clearSearch">✕</button>
            </div>
          </div>
        </div>

        <div class="filter-actions">
          <div class="sort-selector">
            <span class="sort-label">排序：</span>
            <div class="sort-buttons">
              <button 
                class="sort-btn" 
                :class="{ active: filters.sortBy === 'name' }"
                @click="filters.sortBy = 'name'"
              >🔤 名称</button>
              <button 
                class="sort-btn" 
                :class="{ active: filters.sortBy === 'difficulty' }"
                @click="filters.sortBy = 'difficulty'"
              >⭐ 难度</button>
              <button 
                class="sort-btn" 
                :class="{ active: filters.sortBy === 'dynasty' }"
                @click="filters.sortBy = 'dynasty'"
              >📅 朝代</button>
            </div>
          </div>
          
          <div class="view-toggle">
            <button 
              class="view-btn" 
              :class="{ active: viewMode === 'grid' }"
              @click="viewMode = 'grid'"
            >🔲 网格</button>
            <button 
              class="view-btn" 
              :class="{ active: viewMode === 'list' }"
              @click="viewMode = 'list'"
            >📋 列表</button>
          </div>
        </div>
      </div>

      <!-- 结果统计 -->
      <div class="result-stats">
        <span>共找到 <strong>{{ filteredAllusions.length }}</strong> 个典故</span>
        <span v-if="filters.keyword" class="keyword-badge">关键词：{{ filters.keyword }}</span>
        <span v-if="filters.dynastyId" class="keyword-badge">朝代：{{ getDynastyName(filters.dynastyId) }}</span>
        <span v-if="filters.theme" class="keyword-badge">主题：{{ filters.theme }}</span>
      </div>

      <!-- 网格视图 -->
      <div v-if="viewMode === 'grid'" class="allusion-grid">
        <div 
          v-for="allusion in paginatedAllusions" 
          :key="allusion.id" 
          class="allusion-card"
          @click="openAllusionDetail(allusion.id)"
        >
          <div class="card-header">
            <h3 class="allusion-name">{{ allusion.allusionName }}</h3>
            <span class="difficulty-badge" :class="'difficulty-' + getDifficultyLevel(allusion)">
              {{ getDifficultyLabel(allusion) }}
            </span>
          </div>
          <div class="card-body">
            <p class="allusion-meaning">{{ allusion.paraphrase }}</p>
            <div class="allusion-meta">
              <span class="meta-dynasty" v-if="getDynastyName(allusion.dynastyId)">📅 {{ getDynastyName(allusion.dynastyId) }}</span>
            </div>
            <div class="allusion-tags">
              <span v-for="theme in getThemes(allusion.themeType)" :key="theme" class="tag" :class="getThemeClass(theme)">
                {{ theme }}
              </span>
            </div>
          </div>
          <div class="card-footer">
            <button class="quiz-btn" @click.stop="generateQuiz(allusion)">📝 生成题目</button>
            <button class="source-btn" @click.stop="scrollToSource(allusion)">📜 查看溯源</button>
          </div>
        </div>
      </div>

      <!-- 列表视图 -->
      <div v-else class="allusion-list-view">
        <div class="list-header">
          <div class="list-col col-name">典故名称</div>
          <div class="list-col col-meaning">核心释义</div>
          <div class="list-col col-dynasty">朝代</div>
          <div class="list-col col-difficulty">难度</div>
          <div class="list-col col-themes">主题</div>
          <div class="list-col col-action">操作</div>
        </div>
        <div 
          v-for="allusion in paginatedAllusions" 
          :key="allusion.id" 
          class="list-row"
          @click="openAllusionDetail(allusion.id)"
        >
          <div class="list-col col-name">
            <span class="allusion-name-link">{{ allusion.allusionName }}</span>
          </div>
          <div class="list-col col-meaning">{{ allusion.paraphrase }}</div>
          <div class="list-col col-dynasty">{{ getDynastyName(allusion.dynastyId) }}</div>
          <div class="list-col col-difficulty">
            <span class="difficulty-badge-small" :class="'difficulty-' + getDifficultyLevel(allusion)">
              {{ getDifficultyLabel(allusion) }}
            </span>
          </div>
          <div class="list-col col-themes">
            <div class="tags-inline">
              <span v-for="theme in getThemes(allusion.themeType).slice(0, 2)" :key="theme" class="tag-small" :class="getThemeClass(theme)">
                {{ theme }}
              </span>
            </div>
          </div>
          <div class="list-col col-action">
            <button class="quiz-btn-small" @click.stop="generateQuiz(allusion)">生成题目</button>
          </div>
        </div>
      </div>

      <!-- 分页 -->
      <div class="pagination-modern" v-if="filteredAllusions.length > pageSize">
        <button class="page-btn" :disabled="currentPage === 1" @click="goToPage(currentPage - 1)">‹</button>
        <span class="page-info">{{ currentPage }} / {{ totalPages }}</span>
        <button class="page-btn" :disabled="currentPage === totalPages" @click="goToPage(currentPage + 1)">›</button>
      </div>

      <!-- 空状态 -->
      <div v-if="filteredAllusions.length === 0 && !loading" class="empty-state-modern">
        <div class="empty-animation">📜</div>
        <h3>暂无相关典故</h3>
        <p>试试调整筛选条件或搜索其他关键词</p>
        <button class="empty-reset-btn" @click="resetFilters">重置筛选</button>
      </div>
    </div>

    <!-- 典故详情弹窗 -->
    <el-dialog v-model="showDetailDialog" :title="currentAllusion.allusionName" width="1000px" style="background: linear-gradient(135deg, #1a1a1a 0%, #2d2d2d 100%);" class="allusion-detail-dialog">
      <div class="allusion-detail" v-if="currentAllusion.id">
        <!-- 基础信息区 -->
        <div class="detail-header">
          <div class="detail-title-area">
            <h2 class="detail-name">{{ currentAllusion.allusionName }}</h2>
            <div class="detail-badges">
              <span class="difficulty-badge-large" :class="'difficulty-' + getDifficultyLevel(currentAllusion)">
                {{ getDifficultyLabel(currentAllusion) }}
              </span>
              <span class="dynasty-badge" v-if="getDynastyName(currentAllusion.dynastyId)">📅 {{ getDynastyName(currentAllusion.dynastyId) }}</span>
            </div>
          </div>
          <div class="detail-source" v-if="currentAllusion.source">
            <div class="source-label">📖 出处</div>
            <div class="source-name">{{ currentAllusion.source }}</div>
            <div class="source-original" v-if="currentAllusion.originalText">
              <pre>{{ currentAllusion.originalText }}</pre>
            </div>
          </div>
        </div>

        <!-- 释义区 -->
        <div class="meaning-section">
          <div class="section-title">
            <span class="title-icon">📖</span>
            <h3>释义</h3>
          </div>
          <div class="meaning-content">{{ currentAllusion.paraphrase }}</div>
          <div class="moral-content" v-if="currentAllusion.moral">
            <span class="moral-label">寓意：</span>
            {{ currentAllusion.moral }}
          </div>
        </div>

        <!-- 典故解读区 -->
        <div class="interpretation-grid">
          <div class="interpret-card" v-if="getDynastyDesc(currentAllusion.dynastyId)">
            <div class="interpret-icon">🏛️</div>
            <h4>历史背景</h4>
            <p>{{ getDynastyDesc(currentAllusion.dynastyId) }}</p>
          </div>
          <div class="interpret-card" v-if="currentAllusion.moral">
            <div class="interpret-icon">💡</div>
            <h4>寓意启示</h4>
            <p>{{ currentAllusion.moral }}</p>
          </div>
        </div>

        <!-- 溯源展示层 -->
        <div class="trace-section">
          <div class="section-title">
            <span class="title-icon">🔗</span>
            <h3>典故溯源</h3>
          </div>
          
          <div class="trace-grid">
            <!-- 关联人物：与典故同朝代的历史人物（库中无直接关联表，按朝代推导） -->
            <div class="trace-card" v-if="relatedPersons.length">
              <div class="trace-icon">👤</div>
              <div class="trace-content">
                <h4>同朝人物</h4>
                <div
                  v-for="person in relatedPersons"
                  :key="person.id"
                  class="person-trace"
                  @click="jumpToPerson(person.personName)"
                >
                  <div class="person-name">{{ person.personName }}</div>
                  <div class="person-role">{{ person.identityType || '历史人物' }}</div>
                  <span class="trace-link">查看详情 →</span>
                </div>
              </div>
            </div>

            <!-- 关联事件：与典故同朝代的历史事件 -->
            <div class="trace-card" v-if="relatedEvents.length">
              <div class="trace-icon">⚡</div>
              <div class="trace-content">
                <h4>同朝事件</h4>
                <div
                  v-for="event in relatedEvents"
                  :key="event.id"
                  class="event-trace"
                  @click="jumpToEvent(event.id)"
                >
                  <div class="event-name">{{ event.eventName }}</div>
                  <div class="event-year">{{ formatYear(event.eventYear) }}</div>
                  <span class="trace-link">查看详情 →</span>
                </div>
              </div>
            </div>

            <!-- 朝代背景 -->
            <div class="trace-card" v-if="getDynastyName(currentAllusion.dynastyId)">
              <div class="trace-icon">🏛️</div>
              <div class="trace-content">
                <h4>朝代背景</h4>
                <div class="dynasty-trace" @click="jumpToDynasty(getDynastyName(currentAllusion.dynastyId))">
                  <div class="dynasty-name">{{ getDynastyName(currentAllusion.dynastyId) }}</div>
                  <div class="dynasty-desc">{{ getDynastyDesc(currentAllusion.dynastyId) }}</div>
                  <span class="trace-link">查看详情 →</span>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 助手按钮 -->
        <div class="ai-assistant-section">
          <button class="ai-assistant-btn" @click="openAIAssistant">
            <span class="ai-icon">🤖</span>
            <span>AI历史助手</span>
            <span class="ai-hint">关于 "{{ currentAllusion.allusionName }}" 的问题，随时问我</span>
          </button>
        </div>

        <!-- 多媒体拓展：按资源类型渲染真实播放器 -->
        <div class="media-section" v-if="mediaInfo.src">
          <div class="section-title">
            <span class="title-icon">🎵</span>
            <h3>多媒体拓展</h3>
          </div>

          <!-- 视频：动画解读 -->
          <div class="media-player-box" v-if="mediaInfo.type === 'video'">
            <video
              class="media-video"
              :src="mediaInfo.src"
              controls
              preload="metadata"
              @error="onMediaError('视频')"
            ></video>
            <p class="media-caption">📺 动画解读</p>
          </div>

          <!-- 音频：典故诵读 -->
          <div class="media-player-box" v-else-if="mediaInfo.type === 'audio'">
            <div class="audio-wrap">
              <div class="audio-icon">🎧</div>
              <div class="audio-main">
                <p class="audio-title">{{ currentAllusion.allusionName }} · 典故诵读</p>
                <audio
                  class="media-audio"
                  :src="mediaInfo.src"
                  controls
                  preload="metadata"
                  @error="onMediaError('音频')"
                ></audio>
              </div>
            </div>
          </div>

          <!-- 图片：典故配图 -->
          <div class="media-player-box" v-else-if="mediaInfo.type === 'image'">
            <img class="media-image" :src="mediaInfo.src" :alt="currentAllusion.allusionName" />
            <p class="media-caption">🖼️ 典故配图</p>
          </div>

          <!-- 其他类型：提供下载/打开入口 -->
          <div class="media-player-box" v-else>
            <a class="media-link" :href="mediaInfo.src" target="_blank" rel="noopener">
              🔗 打开多媒体资源
            </a>
          </div>
        </div>
      </div>
    </el-dialog>

    <!-- 独立的题目生成弹窗 -->
    <el-dialog 
      v-model="showQuizDialog" 
      title="📝 典故小测验" 
      width="500px" 
      style="background: linear-gradient(135deg, #1a1a1a 0%, #2d2d2d 100%);"
      class="quiz-dialog" 
      :close-on-click-modal="false"
      :append-to-body="true"
    >
      <div class="quiz-content">
        <p class="quiz-question">{{ quizQuestion }}</p>
        <div class="quiz-options">
          <button 
            v-for="(option, idx) in quizOptions" 
            :key="idx"
            class="quiz-option"
            :class="{ correct: quizAnswered && option === quizCorrect, wrong: quizAnswered && option === quizUserAnswer && option !== quizCorrect }"
            @click="answerQuiz(option)"
            :disabled="quizAnswered"
          >
            {{ option }}
          </button>
        </div>
        <div v-if="quizAnswered" class="quiz-feedback">
          <p v-if="quizUserAnswer === quizCorrect">✅回答正确！{{ quizExplanation }}</p>
          <p v-else>❌回答错误！正确答案是：{{ quizCorrect }}<br>{{ quizExplanation }}</p>
        </div>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="closeQuizDialog">关闭</el-button>
          <el-button type="primary" @click="resetQuiz" v-if="quizAnswered">再来一题</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import topComponent from '@/components/topComponent.vue';
import request, { errorMessage, resolveAssetUrl } from '@/utils/request';

const router = useRouter();

// 导航栏状态
const activeDynastyIndex = ref(-1);
const isNavBouncing = ref(false);

// 筛选条件
const filters = ref({
  dynastyId: '',
  theme: '',
  difficulty: '',
  keyword: '',
  sortBy: 'name'
});

// 视图模式
const viewMode = ref('grid');

// 分页
const currentPage = ref(1);
const pageSize = ref(12);
const total = ref(0);

// 数据
const allAllusions = ref([]);
const allPersons = ref([]);
const allEvents = ref([]);
const loading = ref(false);

// 详情弹窗
const showDetailDialog = ref(false);
const currentAllusion = ref({});

// 题目弹窗
const showQuizDialog = ref(false);
const quizQuestion = ref('');
const quizOptions = ref([]);
const quizCorrect = ref('');
const quizUserAnswer = ref('');
const quizAnswered = ref(false);
const quizExplanation = ref('');
const currentQuizAllusion = ref(null);

// 朝代列表（先用内置数据占位，页面加载后由 /dynasty/list/all 覆盖）
const dynastyList = ref([
  { id: 1, name: "夏朝" }, { id: 2, name: "商朝" }, { id: 3, name: "周朝" },
  { id: 4, name: "秦朝" }, { id: 5, name: "汉朝" }, { id: 6, name: "三国" },
  { id: 7, name: "晋朝" }, { id: 8, name: "南北朝" }, { id: 9, name: "隋朝" },
  { id: 10, name: "唐朝" }, { id: 11, name: "宋朝" }, { id: 12, name: "元朝" },
  { id: 13, name: "明朝" }, { id: 14, name: "清朝" }
]);
const dynastyMap = ref({});

// 构建朝代映射
dynastyList.value.forEach(d => {
  dynastyMap.value[d.id] = d;
});

/** 加载真实朝代数据（管理端新增/修改朝代后前台同步） */
async function loadDynastyList() {
  try {
    const res = await request.get('/dynasty/list/all');
    const list = res.data?.code === 200 ? (res.data.data || []) : [];
    if (list.length === 0) return;
    dynastyList.value = list.map(d => {
      const shortName = d.name || '';
      return {
        id: d.id,
        // 数据库里多为简称（“唐”），展示时补成“唐朝”
        name: /(朝|三国|南北朝|时期|代)$/.test(shortName) ? shortName : `${shortName}朝`,
        description: d.dynastyDesc || ""
      };
    });
    dynastyMap.value = {};
    dynastyList.value.forEach(d => {
      dynastyMap.value[d.id] = d;
    });
  } catch (e) {
    console.warn('加载朝代数据失败，使用内置朝代列表', e);
  }
}

// 主题类型列表
const themeList = ref(['励志', '智慧', '警示', '寓言', '哲理', '战争', '权谋']);

// 难度列表
const difficultyList = ref([
  { value: 'easy', label: '入门' },
  { value: 'medium', label: '进阶' },
  { value: 'hard', label: '精通' }
]);

// 统计：典故所覆盖朝代内的历史人物数量（真实统计，非随机）
const coveredDynastyIds = computed(() =>
  new Set(allAllusions.value.map(a => String(a.dynastyId)).filter(Boolean))
);
const totalPersons = computed(() =>
  allPersons.value.filter(p => coveredDynastyIds.value.has(String(p.dynastyId))).length
);

// 典故 → 朝代：优先取数据库 dynasty_id；库中无值时用内置映射兜底，避免出现“未知朝代”
const ALLUSION_DYNASTY_FALLBACK = {
  卧薪尝胆: 3, 纸上谈兵: 4, 破釜沉舟: 4, 草木皆兵: 7, 四面楚歌: 5,
  完璧归赵: 3, 指鹿为马: 4, 负荆请罪: 3, 悬梁刺股: 3, 凿壁偷光: 5,
  闻鸡起舞: 7, 程门立雪: 11, 三顾茅庐: 6, 草船借箭: 6, 空城计: 6,
  乐不思蜀: 6, 望梅止渴: 6, 画蛇添足: 3, 守株待兔: 3, 掩耳盗铃: 3,
  邯郸学步: 3, 买椟还珠: 3, 狐假虎威: 3, 杯弓蛇影: 7, 对牛弹琴: 8,
  朝三暮四: 3, 井底之蛙: 3, 叶公好龙: 5, 愚公移山: 3, 精卫填海: 3
};

function autoMatchDynastyId(allusion) {
  if (allusion.dynastyId) return Number(allusion.dynastyId);
  return ALLUSION_DYNASTY_FALLBACK[allusion.allusionName] || null;
}

// 筛选后的典故列表
const filteredAllusions = computed(() => {
  let list = [...allAllusions.value];
  
  // 朝代筛选
  if (filters.value.dynastyId) {
    list = list.filter(a => a.dynastyId === filters.value.dynastyId);
  }
  
  // 主题筛选
  if (filters.value.theme) {
    list = list.filter(a => (a.themeType || '').includes(filters.value.theme));
  }
  
  // 难度筛选
  if (filters.value.difficulty) {
    list = list.filter(a => getDifficultyLevel(a) === filters.value.difficulty);
  }
  
  // 关键词搜索
  if (filters.value.keyword) {
    const kw = filters.value.keyword.toLowerCase();
    list = list.filter(a => 
      a.allusionName?.toLowerCase().includes(kw) ||
      a.paraphrase?.toLowerCase().includes(kw) ||
      a.source?.toLowerCase().includes(kw) ||
      a.originalText?.toLowerCase().includes(kw)
    );
  }
  
  // 排序
  if (filters.value.sortBy === 'name') {
    list.sort((a, b) => (a.allusionName || '').localeCompare(b.allusionName || ''));
  } else if (filters.value.sortBy === 'difficulty') {
    const order = { easy: 1, medium: 2, hard: 3 };
    list.sort((a, b) => order[getDifficultyLevel(a)] - order[getDifficultyLevel(b)]);
  } else if (filters.value.sortBy === 'dynasty') {
    list.sort((a, b) => getDynastyName(a.dynastyId).localeCompare(getDynastyName(b.dynastyId)));
  }
  
  total.value = list.length;
  return list;
});

// 分页
const totalPages = computed(() => {
  const len = filteredAllusions.value.length;
  if (len === 0) return 1;
  return Math.ceil(len / pageSize.value);
});

const paginatedAllusions = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value;
  return filteredAllusions.value.slice(start, start + pageSize.value);
});

// 获取朝代名称
function getDynastyName(dynastyId) {
  if (!dynastyId) return '';
  const dynasty = dynastyMap.value[dynastyId];
  return dynasty?.name || '';
}

// 获取朝代描述
function getDynastyDesc(dynastyId) {
  return dynastyMap.value[dynastyId]?.description || '该朝代是中国历史上的重要时期';
}

// 关联人物：与典故同朝代的历史人物；出处/名称里点到的人物优先展示（库中无典故-人物关联表）
const relatedPersons = computed(() => {
  const a = currentAllusion.value;
  if (!a.dynastyId) return [];
  const key = `${a.allusionName || ''}${a.source || ''}`;
  return allPersons.value
    .filter(p => String(p.dynastyId) === String(a.dynastyId))
    .slice()
    .sort((x, y) => (key.includes(y.personName) ? 1 : 0) - (key.includes(x.personName) ? 1 : 0))
    .slice(0, 6);
});

// 关联事件：与典故同朝代的历史事件，按年份先后排列
const relatedEvents = computed(() => {
  const a = currentAllusion.value;
  if (!a.dynastyId) return [];
  return allEvents.value
    .filter(e => String(e.dynastyId) === String(a.dynastyId))
    .slice()
    .sort((x, y) => (x.eventYear || 0) - (y.eventYear || 0))
    .slice(0, 6);
});

// 解析主题类型
function getThemes(themeType) {
  if (!themeType) return [];
  return themeType.split(',').map(t => t.trim());
}

// 获取难度等级
function getDifficultyLevel(allusion) {
  const length = (allusion.originalText || '').length;
  if (length < 50) return 'easy';
  if (length < 150) return 'medium';
  return 'hard';
}

// 获取难度标签
function getDifficultyLabel(allusion) {
  const level = getDifficultyLevel(allusion);
  const labels = { easy: '入门', medium: '进阶', hard: '精通' };
  return labels[level] || '入门';
}

// 获取主题样式类
function getThemeClass(theme) {
  const themeMap = {
    '励志': 'theme-励志', '智慧': 'theme-智慧', '警示': 'theme-警示',
    '寓言': 'theme-寓言', '哲理': 'theme-哲理', '战争': 'theme-战争',
    '权谋': 'theme-权谋'
  };
  return themeMap[theme] || 'theme-default';
}

// 格式化年份
function formatYear(year) {
  if (!year) return '';
  if (year < 0) return `公元前${Math.abs(year)}年`;
  return `公元${year}年`;
}

// 切换朝代筛选
function toggleDynastyFilter(dynastyId) {
  if (filters.value.dynastyId === dynastyId) {
    filters.value.dynastyId = '';
  } else {
    filters.value.dynastyId = dynastyId;
  }
  currentPage.value = 1;
}

// 切换主题筛选
function toggleThemeFilter(theme) {
  if (filters.value.theme === theme) {
    filters.value.theme = '';
  } else {
    filters.value.theme = theme;
  }
  currentPage.value = 1;
}

// 重置筛选
function resetFilters() {
  filters.value = {
    dynastyId: '',
    theme: '',
    difficulty: '',
    keyword: '',
    sortBy: 'name'
  };
  currentPage.value = 1;
}

// 搜索处理
function handleSearch() {
  currentPage.value = 1;
}

// 清除搜索
function clearSearch() {
  filters.value.keyword = '';
  currentPage.value = 1;
}

// 跳转到指定页
function goToPage(page) {
  if (page < 1) return;
  if (page > totalPages.value) return;
  currentPage.value = page;
}

// 加载典故列表
async function loadAllusions() {
  loading.value = true;
  try {
    // 后端支持 allusionName/source/themeType 服务端筛选，这里一次性取回后在本地做多条件组合与排序
    const res = await request.get('/allusion/page', {
      params: { pageNum: 1, pageSize: 1000 }
    });
    
    if (res.data && res.data.code === 200 && res.data.data) {
      let records = res.data.data.records || [];
  
      records = records.map(item => ({
        ...item,
        dynastyId: autoMatchDynastyId(item)
      }));
      
      allAllusions.value = records;
      total.value = res.data.data.total || 0;
    } else {
      ElMessage.error(res.data?.msg || '加载失败');
    }
  } catch (error) {
    ElMessage.error(errorMessage(error, '加载典故列表失败'));
  } finally {
    loading.value = false;
  }
}

/** 加载全部人物与事件，供“同朝人物 / 同朝事件”溯源展示使用（库中无典故关联表，按朝代推导） */
async function loadRelationSources() {
  const dynastyIds = dynastyList.value.map(d => d.id);
  const [personResults, eventResults] = await Promise.all([
    Promise.all(dynastyIds.map(id =>
      request.get('/historical-person/page', { params: { pageNum: 1, pageSize: 1000, dynastyId: id } }).catch(() => null)
    )),
    Promise.all(dynastyIds.map(id =>
      request.get('/historical-event/page', { params: { pageNum: 1, pageSize: 1000, dynastyId: id } }).catch(() => null)
    ))
  ]);
  let personRecords = [];
  personResults.forEach(res => {
    if (res?.data?.code === 200) personRecords = personRecords.concat(res.data.data?.records || []);
  });
  allPersons.value = personRecords;

  let eventRecords = [];
  eventResults.forEach(res => {
    if (res?.data?.code === 200) eventRecords = eventRecords.concat(res.data.data?.records || []);
  });
  allEvents.value = eventRecords;
}

// 加载典故详情
async function loadAllusionDetail(allusionId) {
  try {
    const res = await request.get(`/allusion/${allusionId}`);
    if (res.data && res.data.code === 200 && res.data.data) {
      currentAllusion.value = {
        ...res.data.data,
        dynastyId: autoMatchDynastyId(res.data.data)
      };
      return true;
    }
    ElMessage.error(res.data?.msg || '加载详情失败');
    return false;
  } catch (error) {
    ElMessage.error(errorMessage(error, '加载典故详情失败'));
    return false;
  }
}

// 打开典故详情
async function openAllusionDetail(allusionId) {
  const success = await loadAllusionDetail(allusionId);
  if (success) {
    showDetailDialog.value = true;
  }
}

// 滚动到溯源区
function scrollToSource(allusion) {
  openAllusionDetail(allusion.id);
  setTimeout(() => {
    const traceSection = document.querySelector('.trace-section');
    if (traceSection) {
      traceSection.scrollIntoView({ behavior: 'smooth' });
    }
  }, 300);
}

// 生成题目
function generateQuiz(allusion) {
  currentQuizAllusion.value = allusion;
  
  quizQuestion.value = `关于典故"${allusion.allusionName}"，以下哪个描述是正确的？`;
  
  const correctOption = `出自《${allusion.source || '史记'}》，意思是：${allusion.paraphrase}`;
  const wrongOptions = [
    `出自《战国策》，意思是：${allusion.paraphrase ? allusion.paraphrase.substring(0, 10) : '未知'}...`,
    `出自《左传》，意思是：与此无关的描述`,
    `出自《论语》，意思是：与此无关的描述`
  ];
  
  quizOptions.value = [correctOption, ...wrongOptions];
  for (let i = quizOptions.value.length - 1; i > 0; i--) {
    const j = Math.floor(Math.random() * (i + 1));
    [quizOptions.value[i], quizOptions.value[j]] = [quizOptions.value[j], quizOptions.value[i]];
  }
  
  quizCorrect.value = correctOption;
  quizExplanation.value = `${allusion.allusionName}出自${allusion.source || '史记'}，意思是"${allusion.paraphrase}"。`;
  quizUserAnswer.value = '';
  quizAnswered.value = false;
  
  showQuizDialog.value = true;
}

// 关闭题目弹窗
function closeQuizDialog() {
  showQuizDialog.value = false;
  currentQuizAllusion.value = null;
  quizAnswered.value = false;
}

// 重置题目
function resetQuiz() {
  if (currentQuizAllusion.value) {
    generateQuiz(currentQuizAllusion.value);
  }
}

function answerQuiz(option) {
  quizUserAnswer.value = option;
  quizAnswered.value = true;
}

// 多媒体拓展：按资源后缀判断类型，渲染真实播放器
const MEDIA_EXT = {
  audio: ['mp3', 'wav', 'm4a', 'aac', 'ogg', 'oga', 'flac'],
  video: ['mp4', 'webm', 'ogv', 'mov', 'mkv', 'm4v', 'avi'],
  image: ['jpg', 'jpeg', 'png', 'gif', 'webp', 'bmp', 'svg']
};

const mediaInfo = computed(() => {
  const path = currentAllusion.value.multimediaPath;
  if (!path) return { type: '', src: '' };
  const src = resolveAssetUrl(path);
  const ext = (String(path).split('?')[0].split('#')[0].split('.').pop() || '').toLowerCase();
  let type = 'other';
  if (MEDIA_EXT.audio.includes(ext)) type = 'audio';
  else if (MEDIA_EXT.video.includes(ext)) type = 'video';
  else if (MEDIA_EXT.image.includes(ext)) type = 'image';
  return { type, src };
});

function onMediaError(kind) {
  ElMessage.error(`${kind}资源加载失败，请确认文件已上传且路径正确`);
}

// 跳转函数
function jumpToPerson(personName) {
  showDetailDialog.value = false;
  router.push({ path: '/person', query: { keyword: personName } });
  ElMessage.success(`正在跳转至人物详情：${personName}`);
}

function jumpToEvent(eventId) {
  showDetailDialog.value = false;
  router.push({ path: '/event', query: { id: eventId } });
  ElMessage.success(`正在跳转至事件详情`);
}

function jumpToDynasty(dynastyName) {
  showDetailDialog.value = false;
  router.push({ path: '/timeline', query: { dynasty: dynastyName } });
  ElMessage.success(`正在跳转至朝代详情：${dynastyName}`);
}

// 打开AI助手（关联当前典故）
function openAIAssistant() {
  if (!currentAllusion.value.allusionName) return;
  router.push({
    path: '/ai-assistant',
    query: {
      context: 'allusion',
      name: currentAllusion.value.allusionName,
      id: currentAllusion.value.id
    }
  });
  ElMessage.success('正在打开 AI 历史助手');
}

// 监听筛选条件变化
watch([() => filters.value.dynastyId, () => filters.value.theme, () => filters.value.difficulty, 
        () => filters.value.keyword], () => {
  currentPage.value = 1;
}, { deep: true });

// 初始化
onMounted(async () => {
  // 先加载真实朝代，再加载典故（朝代筛选依赖 ID 映射），最后拉取人物/事件用于溯源展示
  await loadDynastyList();
  await loadAllusions();
  await loadRelationSources();
});
</script>

<style scoped>
.allusion-page {
  min-height: 100vh;
  background: linear-gradient(135deg, #0a0a1a 0%, #1a1a2e 100%);
  position: relative;
  overflow-x: hidden;
}

.allusion-container {
  max-width: 1400px;
  margin: 0 auto;
  padding: 20px;
}

.hero-section {
  width: 100%;
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

/* 筛选区 */
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

.filter-icon { font-size: 24px; }
.filter-title { font-size: 18px; font-weight: 500; color: #FFD700; flex: 1; }
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
  grid-template-columns: repeat(3, 1fr);
  gap: 24px;
  margin-bottom: 24px;
}

.filter-grid.second-row {
  grid-template-columns: 1fr;
  margin-bottom: 20px;
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

.filter-tag.difficulty-easy.active { background: #4CAF50; color: white; }
.filter-tag.difficulty-medium.active { background: #FF9800; color: white; }
.filter-tag.difficulty-hard.active { background: #f44336; color: white; }

.search-wrapper {
  position: relative;
  width: 100%;
}

.search-input-modern {
  width: 1068px;
  padding: 10px 40px 10px 40px;
  background: rgba(0,0,0,0.4);
  border: 1px solid rgba(255,215,0,0.3);
  border-radius: 40px;
  color: #fff;
  font-size: 14px;
  transition: all 0.3s;
}

.search-input-modern:focus {
  outline: none;
  border-color: #FFD700;
  box-shadow: 0 0 15px rgba(255,215,0,0.2);
}

.search-icon-modern {
  position: absolute;
  left: 16px;
  top: 50%;
  transform: translateY(-50%);
  color: #FFD700;
}

.clear-search {
  position: absolute;
  right: 16px;
  top: 50%;
  transform: translateY(-50%);
  background: none;
  border: none;
  color: #aaa;
  cursor: pointer;
  font-size: 16px;
}

.filter-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 16px;
  padding-top: 16px;
  border-top: 1px solid rgba(255,215,0,0.2);
}

.sort-selector {
  display: flex;
  align-items: center;
  gap: 12px;
}

.sort-label {
  color: #FFD700;
  font-size: 13px;
}

.sort-buttons {
  display: flex;
  gap: 8px;
}

.sort-btn {
  padding: 6px 14px;
  background: rgba(255,255,255,0.08);
  border: none;
  border-radius: 20px;
  color: #ccc;
  cursor: pointer;
  transition: all 0.3s;
  font-size: 12px;
}

.sort-btn:hover, .sort-btn.active {
  background: #FFD700;
  color: #1a1a1a;
}

.view-toggle {
  display: flex;
  gap: 8px;
}

.view-btn {
  padding: 6px 14px;
  background: rgba(255,255,255,0.08);
  border: none;
  border-radius: 20px;
  color: #ccc;
  cursor: pointer;
  transition: all 0.3s;
  font-size: 12px;
}

.view-btn:hover, .view-btn.active {
  background: #FFD700;
  color: #1a1a1a;
}

/* 结果统计 */
.result-stats {
  max-width: 1200px;
  margin: 20px auto;
  padding: 0 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  color: #aaa;
  font-size: 14px;
  flex-wrap: wrap;
  gap: 10px;
}

.keyword-badge {
  background: rgba(255,215,0,0.2);
  padding: 4px 12px;
  border-radius: 20px;
  color: #FFD700;
}

/* 网格视图卡片 */
.allusion-grid {
  max-width: 1200px;
  margin: 0 auto;
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(350px, 1fr));
  gap: 24px;
  padding: 0 20px;
}

.allusion-card {
  background: linear-gradient(135deg, rgba(30,30,40,0.9), rgba(20,20,30,0.95));
  backdrop-filter: blur(10px);
  border-radius: 24px;
  padding: 20px;
  cursor: pointer;
  transition: all 0.4s cubic-bezier(0.2, 0.9, 0.4, 1.1);
  border: 1px solid rgba(255,215,0,0.2);
}

.allusion-card:hover {
  transform: translateY(-5px);
  border-color: #FFD700;
  box-shadow: 0 20px 40px rgba(0,0,0,0.3);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.allusion-name {
  font-size: 20px;
  font-weight: bold;
  color: #FFD700;
  margin: 0;
}

.difficulty-badge {
  padding: 4px 10px;
  border-radius: 20px;
  font-size: 11px;
  font-weight: 500;
}

.difficulty-easy { background: rgba(76,175,80,0.3); color: #4CAF50; }
.difficulty-medium { background: rgba(255,152,0,0.3); color: #FF9800; }
.difficulty-hard { background: rgba(244,67,54,0.3); color: #f44336; }

.card-body {
  margin-bottom: 16px;
}

.allusion-meaning {
  font-size: 14px;
  color: #e0e0e0;
  line-height: 1.5;
  margin-bottom: 12px;
}

.allusion-meta {
  display: flex;
  gap: 12px;
  font-size: 12px;
  color: #aaa;
  margin-bottom: 10px;
}

.meta-dynasty { color: #FFD700; }

.allusion-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.tag {
  padding: 3px 10px;
  border-radius: 20px;
  font-size: 10px;
}

.theme-励志 { background: rgba(76,175,80,0.3); color: #4CAF50; }
.theme-权谋 { background: rgba(255,152,0,0.3); color: #FF9800; }
.theme-友情 { background: rgba(33,150,243,0.3); color: #2196F3; }
.theme-智慧 { background: rgba(156,39,176,0.3); color: #9C27B0; }
.theme-战争 { background: rgba(244,67,54,0.3); color: #f44336; }
.theme-哲理 { background: rgba(0,188,212,0.3); color: #00BCD4; }
.theme-default { background: rgba(158,158,158,0.3); color: #9E9E9E; }
.theme-警示{background: rgba(244,67,54,0.3); color: #f44336; } 

.card-footer {
  display: flex;
  gap: 12px;
  border-top: 1px solid rgba(255,255,255,0.1);
  padding-top: 16px;
}

.quiz-btn, .source-btn {
  flex: 1;
  padding: 8px;
  background: rgba(255,215,0,0.15);
  border: 1px solid rgba(255,215,0,0.3);
  border-radius: 12px;
  color: #FFD700;
  font-size: 12px;
  cursor: pointer;
  transition: all 0.3s;
}

.quiz-btn:hover, .source-btn:hover {
  background: rgba(255,215,0,0.3);
  transform: translateY(-2px);
}

/* 列表视图 */
.allusion-list-view {
  max-width: 1200px;
  margin: 0 auto;
  background: rgba(0,0,0,0.3);
  border-radius: 24px;
  overflow: hidden;
  padding: 0 20px;
}

.list-header {
  display: grid;
  grid-template-columns: 1.5fr 2.5fr 0.8fr 0.8fr 1.2fr 0.8fr;
  padding: 16px;
  background: rgba(0,0,0,0.4);
  border-bottom: 1px solid rgba(255,215,0,0.2);
  color: #FFD700;
  font-size: 13px;
  font-weight: 500;
}

.list-row {
  display: grid;
  grid-template-columns: 1.5fr 2.5fr 0.8fr 0.8fr 1.2fr 0.8fr;
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

.allusion-name-link {
  color: #FFD700;
  font-weight: 500;
}

.difficulty-badge-small {
  padding: 3px 8px;
  border-radius: 12px;
  font-size: 10px;
}

.tags-inline {
  display: flex;
  gap: 5px;
  flex-wrap: wrap;
}

.tag-small {
  padding: 2px 8px;
  border-radius: 12px;
  font-size: 10px;
}

.quiz-btn-small {
  padding: 4px 10px;
  background: rgba(255,215,0,0.2);
  border: 1px solid rgba(255,215,0,0.5);
  border-radius: 16px;
  color: #FFD700;
  font-size: 11px;
  cursor: pointer;
  transition: all 0.3s;
}

.quiz-btn-small:hover {
  background: #FFD700;
  color: #1a1a1a;
}

/* 分页 */
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

/* 空状态 */
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
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-10px); }
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

/* 详情弹窗 */
.allusion-detail-dialog :deep(.el-dialog) {
  background: linear-gradient(135deg, #1a1a1a 0%, #2d2d2d 100%) !important;
  backdrop-filter: blur(10px); 
  border: 1px solid rgba(255,215,0,0.3); 
  border-radius: 32px;
}

.allusion-detail-dialog :deep(.el-dialog__header) {
  background: transparent !important;  
  border-bottom: 1px solid rgba(255,215,0,0.2);
  padding: 20px 24px;
}

.allusion-detail-dialog :deep(.el-dialog__title) {
  color: #FFD700;
  font-size: 24px;
  font-weight: 500;
}

.allusion-detail-dialog :deep(.el-dialog__body) {
  padding: 24px;
  max-height: 70vh;
  overflow-y: auto;
}

/* 详情头部 */
.detail-header {
  margin-bottom: 24px;
  padding-bottom: 20px;
  border-bottom: 1px solid rgba(255,215,0,0.2);
}

.detail-name {
  font-size: 28px;
  color: #FFD700;
  margin-bottom: 12px;
}

.detail-badges {
  display: flex;
  gap: 12px;
  margin-bottom: 16px;
}

.difficulty-badge-large {
  padding: 4px 14px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 500;
}

.dynasty-badge {
  padding: 4px 14px;
  background: rgba(0,0,0,0.4);
  border-radius: 20px;
  font-size: 12px;
  color: #ccc;
}

.detail-source {
  background: rgba(0,0,0,0.3);
  border-radius: 16px;
  padding: 16px;
  margin-top: 16px;
}

.source-label {
  color: #FFD700;
  font-size: 12px;
  margin-bottom: 8px;
}

.source-name {
  font-size: 14px;
  font-weight: 500;
  color: #fff;
  margin-bottom: 8px;
}

.source-original pre {
  font-size: 13px;
  color: #ccc;
  line-height: 1.6;
  white-space: pre-wrap;
  font-family: inherit;
  margin: 0;
}

/* 释义区 */
.meaning-section {
  background: rgba(0,0,0,0.3);
  border-radius: 20px;
  padding: 20px;
  margin-bottom: 24px;
}

.section-title {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 16px;
}

.title-icon { font-size: 24px; }
.section-title h3 {
  font-size: 18px;
  color: #FFD700;
  margin: 0;
}

.meaning-content {
  font-size: 16px;
  color: #e0e0e0;
  line-height: 1.6;
  margin-bottom: 12px;
}

.moral-content {
  padding-top: 12px;
  border-top: 1px solid rgba(255,215,0,0.2);
  font-size: 14px;
  color: #ccc;
}

.moral-label {
  color: #FFD700;
  font-weight: 500;
}

/* 典故解读三栏 */
.interpretation-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;
  margin-bottom: 24px;
}

.interpret-card {
  background: rgba(0,0,0,0.3);
  border-radius: 16px;
  padding: 20px;
  text-align: center;
  transition: all 0.3s;
}

.interpret-card:hover {
  transform: translateY(-3px);
  background: rgba(0,0,0,0.4);
}

.interpret-icon {
  font-size: 32px;
  margin-bottom: 12px;
}

.interpret-card h4 {
  font-size: 16px;
  color: #FFD700;
  margin-bottom: 10px;
}

.interpret-card p {
  font-size: 13px;
  color: #ccc;
  line-height: 1.5;
}

/* 溯源区 */
.trace-section {
  margin-bottom: 24px;
}

.trace-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;
}

.trace-card {
  background: rgba(0,0,0,0.3);
  border-radius: 16px;
  padding: 16px;
  transition: all 0.3s;
}

.trace-card:hover {
  background: rgba(255,215,0,0.1);
}

.trace-icon {
  font-size: 28px;
  margin-bottom: 12px;
}

.trace-content h4 {
  font-size: 14px;
  color: #FFD700;
  margin-bottom: 12px;
}

.person-trace, .event-trace, .dynasty-trace {
  cursor: pointer;
}

.person-name, .event-name, .dynasty-name {
  font-size: 14px;
  font-weight: bold;
  color: #fff;
  margin-bottom: 4px;
}

.person-role, .event-year {
  font-size: 11px;
  color: #aaa;
  margin-bottom: 8px;
}

.trace-link {
  font-size: 11px;
  color: #FFD700;
  transition: all 0.3s;
}

.person-trace:hover .trace-link,
.event-trace:hover .trace-link,
.dynasty-trace:hover .trace-link {
  transform: translateX(3px);
  display: inline-block;
}

/* AI助手按钮样式 */
.ai-assistant-section {
  margin: 20px 0 10px;
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
  border-radius: 50px;
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

/* 多媒体区 */
.media-section {
  margin-top: 20px;
}

.media-player-box {
  padding: 16px;
  background: rgba(255,215,0,0.06);
  border: 1px solid rgba(255,215,0,0.25);
  border-radius: 16px;
}

.media-video {
  width: 100%;
  max-height: 420px;
  border-radius: 10px;
  background: #000;
  display: block;
}

.media-image {
  width: 100%;
  max-height: 420px;
  object-fit: contain;
  border-radius: 10px;
  background: #000;
  display: block;
}

.audio-wrap {
  display: flex;
  align-items: center;
  gap: 16px;
}

.audio-icon {
  font-size: 32px;
  flex-shrink: 0;
}

.audio-main {
  flex: 1;
  min-width: 0;
}

.audio-title {
  margin: 0 0 8px;
  color: #FFD700;
  font-size: 15px;
  font-weight: 600;
}

.media-audio {
  width: 100%;
}

.media-caption {
  margin: 10px 0 0;
  color: #FFD700;
  font-size: 14px;
  text-align: center;
}

.media-link {
  display: inline-block;
  padding: 10px 20px;
  background: rgba(255,215,0,0.12);
  border: 1px solid rgba(255,215,0,0.35);
  border-radius: 30px;
  color: #FFD700;
  font-size: 14px;
  text-decoration: none;
  transition: all 0.3s;
}

.media-link:hover {
  background: rgba(255,215,0,0.24);
  transform: translateY(-2px);
}

/* 弹窗样式 */
.quiz-dialog :deep(.el-dialog) {
  background: linear-gradient(135deg, #2a2a3a, #1a1a2a);
  border: 1px solid #FFD700;
  border-radius: 24px;
}

.quiz-dialog :deep(.el-dialog__header) {
  border-bottom: 1px solid rgba(255,215,0,0.3);
  padding: 16px 20px;
}

.quiz-dialog :deep(.el-dialog__title) {
  color: #FFD700;
  font-size: 18px;
  font-weight: 500;
}

.quiz-dialog :deep(.el-dialog__body) {
  padding: 24px;
}

.quiz-dialog :deep(.el-dialog__footer) {
  border-top: 1px solid rgba(255,215,0,0.3);
  padding: 16px 20px;
}

.quiz-content {
  color: #fff;
}

.quiz-question {
  font-size: 16px;
  font-weight: 500;
  margin-bottom: 20px;
  line-height: 1.5;
  color: #FFD700;
}

.quiz-options {
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin-bottom: 20px;
}

.quiz-option {
  padding: 12px 16px;
  background: rgba(255,255,255,0.1);
  border: 1px solid rgba(255,215,0,0.3);
  border-radius: 12px;
  color: #fff;
  cursor: pointer;
  transition: all 0.3s;
  text-align: left;
  font-size: 14px;
}

.quiz-option:hover:not(:disabled) {
  background: rgba(255,215,0,0.2);
  transform: translateX(5px);
}

.quiz-option.correct {
  background: rgba(76,175,80,0.3);
  border-color: #4CAF50;
  color: #4CAF50;
}

.quiz-option.wrong {
  background: rgba(244,67,54,0.3);
  border-color: #f44336;
  color: #f44336;
}

.quiz-feedback {
  margin-top: 20px;
  padding: 12px 16px;
  background: rgba(0,0,0,0.3);
  border-radius: 12px;
  font-size: 13px;
  line-height: 1.5;
  color: #ccc;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

.dialog-footer .el-button {
  background: rgba(255,215,0,0.2);
  border: 1px solid rgba(255,215,0,0.5);
  color: #FFD700;
}

.dialog-footer .el-button:hover {
  background: #FFD700;
  color: #1a1a1a;
}

/* 响应式 */
@media (max-width: 1024px) {
  .filter-grid {
    grid-template-columns: repeat(2, 1fr);
  }
  .filter-grid.second-row {
    grid-template-columns: 1fr;
  }
  .interpretation-grid,
  .trace-grid {
    grid-template-columns: 1fr;
  }
  .allusion-grid {
    grid-template-columns: 1fr;
  }
  .list-header, .list-row {
    grid-template-columns: 1.2fr 2fr 0.8fr 0.6fr 1fr 0.8fr;
    font-size: 11px;
  }
}

@media (max-width: 768px) {
  .hero-title { font-size: 36px; }
  .hero-stats { gap: 24px; }
  .stat-number { font-size: 24px; }
  
  .filter-glass { margin: 0 16px 20px; padding: 20px; }
  .filter-grid { grid-template-columns: 1fr; }
  
  .filter-actions {
    flex-direction: column;
    align-items: stretch;
  }
  
  .list-header, .list-row {
    grid-template-columns: 1fr 1.5fr 0.6fr 0.5fr 0.8fr 0.6fr;
    font-size: 10px;
  }
  
  .quiz-dialog :deep(.el-dialog) {
    width: 90% !important;
  }
  
  .ai-assistant-btn {
    padding: 12px 20px;
    font-size: 14px;
  }
  .ai-hint {
    display: none;
  }
}
</style>