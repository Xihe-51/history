<template>
  <div class="interactive-page">
    <!-- 顶部导航栏 -->
    <top-component ref="topNavRef" class="top-nav" />

    <div class="interactive-container">
      <!-- 页面标题区 -->
      <div class="hero-section">
        <div class="hero-content">
          <h1 class="hero-title">🎮 趣味小游戏</h1>
          <p class="hero-subtitle">在游戏中学习历史 · 寓教于乐</p>
          <div class="hero-stats">
            <div class="stat-item">
              <span class="stat-number">{{ games.length }}</span>
              <span class="stat-label">游戏总数</span>
            </div>
            <div class="stat-item">
              <span class="stat-number">{{ questionTotal }}</span>
              <span class="stat-label">题库题量</span>
            </div>
            <div class="stat-item">
              <span class="stat-number">{{ bestScore }}</span>
              <span class="stat-label">历史最高分</span>
            </div>
          </div>
        </div>
        <div class="hero-wave"></div>
      </div>

      <!-- 游戏分类筛选 -->
      <div class="filter-tabs">
        <button
          v-for="category in categories"
          :key="category.value"
          class="filter-tab"
          :class="{ active: currentCategory === category.value }"
          @click="currentCategory = category.value"
        >
          {{ category.icon }} {{ category.label }}
        </button>
      </div>

      <!-- 游戏列表 - 网格视图 -->
      <div class="game-grid" v-if="filteredGames.length > 0">
        <div
          v-for="game in filteredGames"
          :key="game.id"
          class="game-card"
          @click="openGame(game)"
        >
          <div class="game-cover">
            <div class="cover-placeholder">
              <span class="placeholder-icon">{{ game.icon }}</span>
              <span class="placeholder-text">{{ game.title }}</span>
            </div>
            <div class="play-overlay">
              <span class="play-icon">▶</span>
              <span class="play-text">开始游戏</span>
            </div>
            <div class="game-badge" :class="game.difficulty">
              {{ getDifficultyText(game.difficulty) }}
            </div>
          </div>
          <div class="game-info">
            <h3 class="game-title">{{ game.title }}</h3>
            <p class="game-desc">{{ game.description }}</p>
            <div class="game-meta">
              <span class="game-category">{{ game.icon }} {{ game.categoryLabel }}</span>
              <span class="game-play-count">🎮 {{ game.playCount }} 次</span>
            </div>
          </div>
        </div>
      </div>

      <!-- 空状态 -->
      <div v-else class="empty-state">
        <div class="empty-icon">🎮</div>
        <h3>暂无游戏</h3>
        <p>该分类下暂无游戏，敬请期待</p>
      </div>
    </div>

    <!-- 游戏面板 -->
    <el-dialog
      v-model="showGame"
      :title="currentGame?.title || '游戏'"
      width="860px"
      top="6vh"
      class="game-dialog"
      :close-on-click-modal="false"
      @closed="resetGame"
    >
      <div class="game-panel" v-loading="gameLoading">
        <!-- 问答 / 典故 选择型游戏 -->
        <template v-if="currentGame && (currentGame.id === 'quiz' || currentGame.id === 'allusion')">
          <div v-if="quizQuestions.length === 0 && !gameLoading" class="game-empty">
            暂无可作答的题目，请稍后重试或联系管理员补充题库。
          </div>
          <div v-else class="quiz-list">
            <div
              v-for="(q, qi) in quizQuestions"
              :key="qi"
              class="q-item"
              :class="{ 'q-correct': q.submitted && q.isCorrect, 'q-wrong': q.submitted && !q.isCorrect }"
            >
              <div class="q-content">
                <span class="q-index">{{ qi + 1 }}</span>
                <span class="q-type-tag">{{ typeLabel(q.type) }}</span>
                <span>{{ q.content }}</span>
              </div>
              <div class="q-options">
                <button
                  v-for="(opt, oi) in q.options"
                  :key="oi"
                  class="opt"
                  :class="{
                    selected: isSelected(q, oi),
                    correct: q.submitted && isCorrectOpt(q, oi),
                    wrong: q.submitted && isSelected(q, oi) && !isCorrectOpt(q, oi)
                  }"
                  @click="toggleOption(q, oi)"
                >
                  <span class="opt-letter">{{ letter(oi) }}</span>
                  <span class="opt-text">{{ opt }}</span>
                </button>
              </div>
              <div v-if="q.submitted" class="q-analysis">
                <span class="analysis-label">解析</span>{{ q.analysis || '暂无解析' }}
              </div>
            </div>
          </div>

          <div class="game-bar" v-if="quizQuestions.length > 0">
            <div class="bar-info">
              <span v-if="!quizSubmitted">共 {{ quizQuestions.length }} 题，作答后点击「提交」查看得分</span>
              <span v-else class="score-text">
                得分：<b :class="scoreClass">{{ quizScore }}</b> / 100（答对 {{ quizCorrectCount }} 题）
              </span>
            </div>
            <div class="bar-actions">
              <el-button @click="restartQuiz">重新作答</el-button>
              <el-button type="primary" :disabled="quizSubmitted" @click="submitQuiz">提交答案</el-button>
            </div>
          </div>
        </template>

        <!-- 人物-朝代 配对游戏 -->
        <template v-else-if="currentGame && currentGame.id === 'matching'">
          <div v-if="matchingLeft.length === 0 && !gameLoading" class="game-empty">
            暂无可用于配对的人物数据。
          </div>
          <div v-else class="match-board">
            <div class="match-col">
              <div class="match-col-title">历史人物</div>
              <div
                v-for="item in matchingLeft"
                :key="item.key"
                class="match-cell"
                :class="{ active: matchSelectedLeft === item.key, matched: item.matched, err: item.err }"
                @click="selectLeft(item)"
              >{{ item.label }}</div>
            </div>
            <div class="match-mid">⇄</div>
            <div class="match-col">
              <div class="match-col-title">所属朝代</div>
              <div
                v-for="item in matchingRight"
                :key="item.key"
                class="match-cell"
                :class="{ active: matchSelectedRight === item.key, matched: item.matched, err: item.err }"
                @click="selectRight(item)"
              >{{ item.label }}</div>
            </div>
          </div>
          <div class="game-bar" v-if="matchingLeft.length > 0">
            <div class="bar-info">
              <span v-if="!matchingDone">已配对 {{ matchedCount }} / {{ matchingLeft.length }} 对，失误 {{ matchErrors }} 次</span>
              <span v-else class="score-text">
                全部配对完成！得分：<b class="good">{{ matchScore }}</b> / 100
              </span>
            </div>
            <div class="bar-actions">
              <el-button @click="restartMatching">换一批</el-button>
            </div>
          </div>
        </template>

        <!-- 朝代排序游戏 -->
        <template v-else-if="currentGame && currentGame.id === 'ordering'">
          <div v-if="orderingItems.length === 0 && !gameLoading" class="game-empty">
            暂无朝代数据。
          </div>
          <div v-else>
            <p class="order-tip">请将下列朝代按时间先后（由早到晚）排序：</p>
            <div class="order-list">
              <div
                v-for="(item, index) in orderingItems"
                :key="item.id"
                class="order-item"
                :class="{ 'order-correct': orderSubmitted && orderCorrectFlags[index] === true, 'order-wrong': orderSubmitted && orderCorrectFlags[index] === false }"
              >
                <span class="order-pos">{{ index + 1 }}</span>
                <span class="order-name">{{ item.name }}</span>
                <span class="order-year" v-if="orderSubmitted">{{ formatYear(item.startYear) }}</span>
                <span class="order-actions">
                  <button class="order-btn" :disabled="index === 0" @click="moveOrder(index, -1)">↑</button>
                  <button class="order-btn" :disabled="index === orderingItems.length - 1" @click="moveOrder(index, 1)">↓</button>
                </span>
              </div>
            </div>
          </div>
          <div class="game-bar" v-if="orderingItems.length > 0">
            <div class="bar-info">
              <span v-if="!orderSubmitted">按时间先后排列，完成后点击「检查答案」</span>
              <span v-else class="score-text">
                正确 {{ orderCorrectCount }} / {{ orderingItems.length }} 个，得分：<b :class="orderScore >= 80 ? 'good' : 'bad'">{{ orderScore }}</b> / 100
              </span>
            </div>
            <div class="bar-actions">
              <el-button @click="restartOrdering">换一批</el-button>
              <el-button type="primary" :disabled="orderSubmitted" @click="checkOrdering">检查答案</el-button>
            </div>
          </div>
        </template>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue';
import { ElMessage } from 'element-plus';
import topComponent from '@/components/topComponent.vue';
import request from '@/utils/request';

/* ============ 游戏元数据 ============ */
const games = ref([
  {
    id: 'quiz', title: '历史问答挑战',
    description: '从题库随机抽取题目作答，涵盖事件、人物、典故与朝代知识点。',
    category: 'quiz', categoryLabel: '问答', icon: '❓', difficulty: 'medium', playCount: 0
  },
  {
    id: 'allusion', title: '典故答题挑战',
    description: '根据释义选择对应的历史典故，考察你的典故储备与理解。',
    category: 'quiz', categoryLabel: '问答', icon: '📚', difficulty: 'easy', playCount: 0
  },
  {
    id: 'matching', title: '人物事件匹配',
    description: '将历史人物与其所属朝代正确配对，挑战你的记忆力。',
    category: 'matching', categoryLabel: '配对', icon: '🃏', difficulty: 'easy', playCount: 0
  },
  {
    id: 'ordering', title: '历史朝代排序',
    description: '将历史朝代按时间先后正确排列，考验你的通史知识！',
    category: 'puzzle', categoryLabel: '益智', icon: '🧩', difficulty: 'easy', playCount: 0
  }
]);

const categories = [
  { value: 'all', label: '全部', icon: '🎮' },
  { value: 'quiz', label: '问答', icon: '❓' },
  { value: 'matching', label: '配对', icon: '🃏' },
  { value: 'puzzle', label: '益智', icon: '🧩' }
];

/* ============ 页面级状态 ============ */
const topNavRef = ref(null);
const currentCategory = ref('all');
const showGame = ref(false);
const currentGame = ref(null);
const gameLoading = ref(false);
const questionTotal = ref(0);
const bestScore = ref(Number(localStorage.getItem('interactive_best_score') || 0));

const filteredGames = computed(() =>
  currentCategory.value === 'all'
    ? games.value
    : games.value.filter(g => g.category === currentCategory.value)
);

function getDifficultyText(difficulty) {
  const map = { easy: '入门', medium: '进阶', hard: '挑战' };
  return map[difficulty] || difficulty;
}

function formatYear(year) {
  if (year === null || year === undefined || Number.isNaN(year)) return '';
  return year < 0 ? `公元前${Math.abs(year)}年` : `公元${year}年`;
}

/* ============ 朝代/人物公共数据 ============ */
const dynastyList = ref([]);

async function loadDynastyList() {
  try {
    const res = await request.get('/dynasty/list/all');
    if (res.data && res.data.code === 200 && Array.isArray(res.data.data)) {
      dynastyList.value = res.data.data;
    }
  } catch (e) {
    console.error('加载朝代数据失败', e);
  }
}

async function loadQuestionTotal() {
  try {
    const res = await request.get('/question/page', { params: { pageNum: 1, pageSize: 1 } });
    if (res.data && res.data.code === 200) {
      questionTotal.value = res.data.data?.total || 0;
    }
  } catch (e) {
    console.error('加载题库统计失败', e);
  }
}

onMounted(() => {
  loadDynastyList();
  loadQuestionTotal();
});

/* ============ 打开/关闭游戏 ============ */
async function openGame(game) {
  currentGame.value = game;
  game.playCount++;
  showGame.value = true;
  gameLoading.value = true;
  try {
    if (game.id === 'quiz') await loadQuizFromBank();
    else if (game.id === 'allusion') await loadAllusionQuiz();
    else if (game.id === 'matching') await loadMatchingData();
    else if (game.id === 'ordering') loadOrderingData();
  } catch (e) {
    ElMessage.error('游戏数据加载失败，请稍后重试');
  } finally {
    gameLoading.value = false;
  }
}

function resetGame() {
  quizQuestions.value = [];
  quizSubmitted.value = false;
  matchingLeft.value = [];
  matchingRight.value = [];
  matchingDone.value = false;
  orderingItems.value = [];
  orderSubmitted.value = false;
  orderCorrectFlags.value = [];
}

/* ============ 游戏一/二：问答 & 典故作答 ============ */
const quizQuestions = ref([]);
const quizSubmitted = ref(false);

function letter(i) {
  return String.fromCharCode(65 + i);
}
function answerLetters(answer) {
  return String(answer || '').split(',').map(s => s.trim()).filter(Boolean);
}
function typeLabel(type) {
  return { single: '单选', multiple: '多选', judge: '判断' }[type] || '单选';
}
function normalizeQuestion(raw) {
  let options = [];
  try {
    options = Array.isArray(raw.options) ? raw.options : JSON.parse(raw.options || '[]');
  } catch {
    options = [];
  }
  return {
    type: raw.questionType || 'single',
    content: raw.content,
    options,
    answer: raw.answer,
    analysis: raw.analysis,
    userAnswer: [],
    submitted: false,
    isCorrect: false
  };
}

async function loadQuizFromBank() {
  const res = await request.get('/question/list', { params: { count: 8 } });
  if (res.data && res.data.code === 200 && Array.isArray(res.data.data)) {
    quizQuestions.value = res.data.data.map(normalizeQuestion);
  } else {
    quizQuestions.value = [];
  }
}

async function loadAllusionQuiz() {
  const res = await request.get('/allusion/page', { params: { pageNum: 1, pageSize: 60 } });
  const records = (res.data && res.data.code === 200 && res.data.data?.records) || [];
  const pool = records.filter(a => a.paraphrase && a.allusionName);
  if (pool.length < 4) {
    quizQuestions.value = [];
    return;
  }
  const shuffled = [...pool].sort(() => Math.random() - 0.5).slice(0, 8);
  quizQuestions.value = shuffled.map(item => {
    const others = pool.filter(a => a.allusionName !== item.allusionName)
      .sort(() => Math.random() - 0.5).slice(0, 3);
    const opts = [item.allusionName, ...others.map(o => o.allusionName)]
      .sort(() => Math.random() - 0.5);
    const ansIndex = opts.indexOf(item.allusionName);
    return {
      type: 'single',
      content: `下列典故中，释义为“${item.paraphrase}”的是？`,
      options: opts,
      answer: letter(ansIndex),
      analysis: `${item.allusionName}：${item.paraphrase}${item.source ? `（出自《${item.source}》）` : ''}`,
      userAnswer: [],
      submitted: false,
      isCorrect: false
    };
  });
}

function isSelected(q, oi) {
  const l = letter(oi);
  return q.type === 'multiple' ? q.userAnswer.includes(l) : q.userAnswer[0] === l;
}
function isCorrectOpt(q, oi) {
  return answerLetters(q.answer).includes(letter(oi));
}
function toggleOption(q, oi) {
  if (q.submitted) return;
  const l = letter(oi);
  if (q.type === 'multiple') {
    const idx = q.userAnswer.indexOf(l);
    if (idx === -1) q.userAnswer.push(l);
    else q.userAnswer.splice(idx, 1);
  } else {
    q.userAnswer = [l];
  }
}
function submitQuiz() {
  const unanswered = quizQuestions.value.filter(q => q.userAnswer.length === 0).length;
  if (unanswered > 0) {
    ElMessage.warning(`还有 ${unanswered} 题未作答，已按答错计入`);
  }
  quizQuestions.value.forEach(q => {
    q.submitted = true;
    const correct = answerLetters(q.answer).sort().join(',');
    const user = [...q.userAnswer].sort().join(',');
    q.isCorrect = correct !== '' && correct === user;
  });
  quizSubmitted.value = true;
  saveBestScore(quizScore.value);
}
function restartQuiz() {
  quizSubmitted.value = false;
  quizQuestions.value.forEach(q => {
    q.userAnswer = [];
    q.submitted = false;
    q.isCorrect = false;
  });
}

const quizCorrectCount = computed(() => quizQuestions.value.filter(q => q.isCorrect).length);
const quizScore = computed(() =>
  quizQuestions.value.length === 0 ? 0 : Math.round((quizCorrectCount.value / quizQuestions.value.length) * 100)
);
const scoreClass = computed(() => (quizScore.value >= 80 ? 'good' : 'bad'));

/* ============ 游戏三：人物-朝代配对 ============ */
const matchingLeft = ref([]);
const matchingRight = ref([]);
const matchSelectedLeft = ref('');
const matchSelectedRight = ref('');
const matchingDone = ref(false);
const matchErrors = ref(0);

async function loadMatchingData() {
  matchingLeft.value = [];
  matchingRight.value = [];
  matchingDone.value = false;
  matchErrors.value = 0;
  matchSelectedLeft.value = '';
  matchSelectedRight.value = '';

  if (dynastyList.value.length === 0) await loadDynastyList();
  const dynasties = dynastyList.value;
  if (dynasties.length === 0) return;

  const results = await Promise.all(
    dynasties.map(d => request.get('/historical-person/page', {
      params: { pageNum: 1, pageSize: 50, dynastyId: d.id }
    }).catch(() => null))
  );
  const persons = [];
  results.forEach((res, i) => {
    if (res && res.data && res.data.code === 200) {
      (res.data.data?.records || []).forEach(p => {
        persons.push({ personName: p.personName, dynastyName: dynasties[i].name, dynastyId: dynasties[i].id });
      });
    }
  });
  if (persons.length < 4) return;

  const pick = [...persons].sort(() => Math.random() - 0.5).slice(0, 8);
  const usedDynasties = [...new Set(pick.map(p => p.dynastyName))];
  matchingLeft.value = pick
    .map((p, i) => ({ key: p.dynastyId + '-' + i, label: p.personName, match: p.dynastyName, matched: false, err: false }))
    .sort(() => Math.random() - 0.5);
  matchingRight.value = usedDynasties
    .map((name, i) => ({ key: 'd-' + i, label: name, matched: false, err: false }))
    .sort(() => Math.random() - 0.5);
}

function selectLeft(item) {
  if (item.matched || matchingDone.value) return;
  matchSelectedLeft.value = item.key;
  tryMatch();
}
function selectRight(item) {
  if (item.matched || matchingDone.value) return;
  matchSelectedRight.value = item.key;
  tryMatch();
}
function tryMatch() {
  if (!matchSelectedLeft.value || !matchSelectedRight.value) return;
  const left = matchingLeft.value.find(i => i.key === matchSelectedLeft.value);
  const right = matchingRight.value.find(i => i.key === matchSelectedRight.value);
  if (!left || !right) return;
  if (left.match === right.label) {
    left.matched = true;
    right.matched = true;
  } else {
    matchErrors.value++;
    left.err = true;
    right.err = true;
    setTimeout(() => {
      left.err = false;
      right.err = false;
    }, 600);
  }
  matchSelectedLeft.value = '';
  matchSelectedRight.value = '';
  if (matchingLeft.value.every(i => i.matched)) {
    matchingDone.value = true;
    saveBestScore(matchScore.value);
  }
}
function restartMatching() {
  loadMatchingData();
}

const matchedCount = computed(() => matchingLeft.value.filter(i => i.matched).length);
const matchScore = computed(() => {
  const total = matchingLeft.value.length || 1;
  return Math.max(0, Math.round((total / (total + matchErrors.value)) * 100));
});

/* ============ 游戏四：朝代排序 ============ */
const orderingItems = ref([]);
const orderSubmitted = ref(false);
const orderCorrectFlags = ref([]);

function loadOrderingData() {
  orderSubmitted.value = false;
  orderCorrectFlags.value = [];
  if (dynastyList.value.length === 0) {
    // 朝代数据未就绪时，稍后重试
    loadDynastyList().then(() => buildOrderingItems());
    return;
  }
  buildOrderingItems();
}
function buildOrderingItems() {
  const valid = dynastyList.value.filter(d => typeof d.startYear === 'number');
  if (valid.length === 0) {
    orderingItems.value = [];
    return;
  }
  const pick = [...valid].sort(() => Math.random() - 0.5).slice(0, 6);
  orderingItems.value = pick.map(d => ({ id: d.id, name: d.name, startYear: d.startYear }))
    .sort(() => Math.random() - 0.5);
}
function moveOrder(index, delta) {
  if (orderSubmitted.value) return;
  const target = index + delta;
  if (target < 0 || target >= orderingItems.value.length) return;
  const arr = orderingItems.value;
  [arr[index], arr[target]] = [arr[target], arr[index]];
  orderingItems.value = [...arr];
}
function checkOrdering() {
  const sorted = [...orderingItems.value].sort((a, b) => a.startYear - b.startYear);
  orderCorrectFlags.value = orderingItems.value.map(item =>
    item.id === sorted[orderingItems.value.indexOf(item)]?.id
  );
  // 逐个位置判断是否与正确顺序一致
  orderCorrectFlags.value = orderingItems.value.map((item, i) => item.id === sorted[i].id);
  orderSubmitted.value = true;
  saveBestScore(orderScore.value);
}
function restartOrdering() {
  buildOrderingItems();
}

const orderCorrectCount = computed(() => orderCorrectFlags.value.filter(Boolean).length);
const orderScore = computed(() =>
  orderingItems.value.length === 0 ? 0 : Math.round((orderCorrectCount.value / orderingItems.value.length) * 100)
);

/* ============ 最高分记录 ============ */
function saveBestScore(score) {
  if (!Number.isFinite(score)) return;
  if (score > bestScore.value) {
    bestScore.value = score;
    localStorage.setItem('interactive_best_score', String(score));
  }
}
</script>

<style scoped>
.interactive-page {
  min-height: 100vh;
  background: linear-gradient(135deg, #0a0a1a 0%, #1a1a2e 100%);
}

.interactive-container {
  max-width: 1400px;
  margin: 0 auto;
  padding: 20px;
}

/* 标题区 */
.hero-section {
  text-align: center;
  padding: 60px 0 50px;
}

.hero-title {
  font-size: 48px;
  font-weight: 700;
  background: linear-gradient(135deg, #FFD700, #FFA500);
  -webkit-background-clip: text;
  background-clip: text;
  color: transparent;
  margin-bottom: 16px;
}

.hero-subtitle {
  font-size: 16px;
  color: rgba(255, 255, 255, 0.7);
  margin-bottom: 30px;
}

.hero-stats {
  display: flex;
  justify-content: center;
  gap: 48px;
}

.stat-number {
  display: block;
  font-size: 28px;
  font-weight: bold;
  color: #FFD700;
}

.stat-label {
  font-size: 12px;
  color: rgba(255, 255, 255, 0.6);
}

.hero-wave {
  height: 60px;
  background: linear-gradient(180deg, transparent, rgba(26, 26, 46, 0.6));
}

/* 分类筛选 */
.filter-tabs {
  display: flex;
  justify-content: center;
  gap: 12px;
  margin: 20px 0 32px;
  flex-wrap: wrap;
}

.filter-tab {
  padding: 8px 20px;
  border-radius: 20px;
  border: 1px solid rgba(255, 215, 0, 0.3);
  background: rgba(255, 255, 255, 0.05);
  color: rgba(255, 255, 255, 0.8);
  cursor: pointer;
  transition: all 0.2s;
}

.filter-tab:hover,
.filter-tab.active {
  background: rgba(255, 215, 0, 0.18);
  color: #FFD700;
  border-color: #FFD700;
}

/* 游戏网格 */
.game-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 24px;
  padding-bottom: 60px;
}

.game-card {
  border-radius: 16px;
  overflow: hidden;
  background: rgba(255, 255, 255, 0.04);
  border: 1px solid rgba(255, 255, 255, 0.08);
  cursor: pointer;
  transition: transform 0.25s, box-shadow 0.25s, border-color 0.25s;
}

.game-card:hover {
  transform: translateY(-6px);
  border-color: rgba(255, 215, 0, 0.5);
  box-shadow: 0 16px 40px rgba(0, 0, 0, 0.5);
}

.game-cover {
  position: relative;
  height: 150px;
  background: linear-gradient(135deg, #23233f, #35355c);
}

.cover-placeholder {
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 10px;
}

.placeholder-icon {
  font-size: 42px;
}

.placeholder-text {
  font-size: 16px;
  color: rgba(255, 255, 255, 0.85);
}

.play-overlay {
  position: absolute;
  inset: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 6px;
  background: rgba(0, 0, 0, 0.55);
  opacity: 0;
  transition: opacity 0.25s;
}

.game-card:hover .play-overlay {
  opacity: 1;
}

.play-icon {
  font-size: 30px;
  color: #FFD700;
}

.play-text {
  color: #FFD700;
  font-size: 14px;
}

.game-badge {
  position: absolute;
  top: 12px;
  right: 12px;
  padding: 3px 10px;
  border-radius: 10px;
  font-size: 12px;
  color: #1a1a2e;
  font-weight: 600;
  background: #FFD700;
}

.game-badge.easy {
  background: #4CAF50;
  color: #fff;
}

.game-badge.medium {
  background: #FFA500;
}

.game-badge.hard {
  background: #FF4D4D;
  color: #fff;
}

.game-info {
  padding: 16px;
}

.game-title {
  font-size: 18px;
  color: #fff;
  margin-bottom: 8px;
}

.game-desc {
  font-size: 13px;
  color: rgba(255, 255, 255, 0.6);
  line-height: 1.6;
  margin-bottom: 12px;
  min-height: 42px;
}

.game-meta {
  display: flex;
  justify-content: space-between;
  font-size: 12px;
  color: rgba(255, 255, 255, 0.55);
}

.empty-state {
  text-align: center;
  padding: 80px 0;
  color: rgba(255, 255, 255, 0.6);
}

.empty-icon {
  font-size: 48px;
  margin-bottom: 12px;
}

/* 游戏面板内容 */
.game-panel {
  max-height: 66vh;
  overflow-y: auto;
  padding: 4px 6px;
}

.game-empty {
  padding: 40px 0;
  text-align: center;
  color: #888;
}

.quiz-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.q-item {
  border: 1px solid #e6e6e6;
  border-radius: 10px;
  padding: 14px 16px;
  background: #fafafa;
}

.q-item.q-correct {
  border-color: #67c23a;
  background: #f0f9eb;
}

.q-item.q-wrong {
  border-color: #f56c6c;
  background: #fef0f0;
}

.q-content {
  font-size: 15px;
  color: #303133;
  line-height: 1.7;
  margin-bottom: 10px;
}

.q-index {
  display: inline-block;
  width: 22px;
  height: 22px;
  line-height: 22px;
  text-align: center;
  border-radius: 50%;
  background: #ffd700;
  color: #1a1a2e;
  font-size: 12px;
  margin-right: 8px;
}

.q-type-tag {
  display: inline-block;
  font-size: 11px;
  padding: 1px 8px;
  border-radius: 6px;
  background: #ecf5ff;
  color: #409eff;
  margin-right: 8px;
}

.q-options {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.opt {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 9px 14px;
  border: 1px solid #dcdfe6;
  border-radius: 8px;
  background: #fff;
  cursor: pointer;
  text-align: left;
  font-size: 14px;
  color: #303133;
  transition: all 0.15s;
}

.opt:hover {
  border-color: #ffd700;
}

.opt.selected {
  border-color: #ffd700;
  background: #fffbe6;
}

.opt.correct {
  border-color: #67c23a;
  background: #f0f9eb;
}

.opt.wrong {
  border-color: #f56c6c;
  background: #fef0f0;
}

.opt-letter {
  width: 22px;
  height: 22px;
  line-height: 22px;
  text-align: center;
  border-radius: 50%;
  background: #f2f6fc;
  font-size: 12px;
  flex-shrink: 0;
}

.q-analysis {
  margin-top: 10px;
  font-size: 13px;
  color: #606266;
  background: #f4f6f9;
  border-radius: 8px;
  padding: 8px 12px;
  line-height: 1.6;
}

.analysis-label {
  display: inline-block;
  margin-right: 8px;
  padding: 0 6px;
  border-radius: 4px;
  background: #ffd700;
  color: #1a1a2e;
  font-size: 11px;
}

/* 底部操作条 */
.game-bar {
  position: sticky;
  bottom: 0;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  margin-top: 18px;
  padding: 12px 4px;
  background: #fff;
  border-top: 1px solid #eee;
}

.bar-info {
  font-size: 14px;
  color: #606266;
}

.score-text b {
  font-size: 20px;
}

.score-text .good {
  color: #67c23a;
}

.score-text .bad {
  color: #f56c6c;
}

/* 配对游戏 */
.match-board {
  display: grid;
  grid-template-columns: 1fr 40px 1fr;
  align-items: start;
  gap: 12px;
}

.match-col-title {
  font-size: 13px;
  color: #909399;
  margin-bottom: 8px;
  text-align: center;
}

.match-col {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.match-mid {
  text-align: center;
  font-size: 22px;
  color: #dcdfe6;
  padding-top: 40px;
}

.match-cell {
  padding: 12px 14px;
  border: 1px solid #dcdfe6;
  border-radius: 8px;
  background: #fff;
  text-align: center;
  cursor: pointer;
  font-size: 14px;
  color: #303133;
  transition: all 0.15s;
}

.match-cell:hover {
  border-color: #ffd700;
}

.match-cell.active {
  border-color: #409eff;
  background: #ecf5ff;
}

.match-cell.matched {
  border-color: #67c23a;
  background: #f0f9eb;
  color: #67c23a;
  cursor: default;
}

.match-cell.err {
  border-color: #f56c6c;
  background: #fef0f0;
}

/* 排序游戏 */
.order-tip {
  font-size: 14px;
  color: #606266;
  margin-bottom: 12px;
}

.order-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.order-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 14px;
  border: 1px solid #dcdfe6;
  border-radius: 8px;
  background: #fff;
}

.order-item.order-correct {
  border-color: #67c23a;
  background: #f0f9eb;
}

.order-item.order-wrong {
  border-color: #f56c6c;
  background: #fef0f0;
}

.order-pos {
  width: 24px;
  height: 24px;
  line-height: 24px;
  text-align: center;
  border-radius: 50%;
  background: #ffd700;
  color: #1a1a2e;
  font-size: 12px;
}

.order-name {
  flex: 1;
  font-size: 15px;
  color: #303133;
}

.order-year {
  font-size: 12px;
  color: #909399;
}

.order-actions {
  display: flex;
  gap: 6px;
}

.order-btn {
  width: 30px;
  height: 28px;
  border: 1px solid #dcdfe6;
  border-radius: 6px;
  background: #fff;
  cursor: pointer;
}

.order-btn:disabled {
  opacity: 0.4;
  cursor: not-allowed;
}
</style>
