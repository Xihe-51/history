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
              <span class="stat-number">{{ gameList.length }}</span>
              <span class="stat-label">游戏总数</span>
            </div>
            <div class="stat-item">
              <span class="stat-number">{{ totalPlayCount }}</span>
              <span class="stat-label">总游玩次数</span>
            </div>
            <div class="stat-item">
              <span class="stat-number">{{ avgRating }}</span>
              <span class="stat-label">平均评分</span>
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
              <span class="placeholder-icon">{{ game.categoryIcon }}</span>
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
              <span class="game-category">{{ game.categoryIcon }} {{ game.category }}</span>
              <span class="game-play-count">🎮 {{ game.playCount }} 次</span>
            </div>
            <div class="game-rating">
              <el-rate v-model="game.rating" disabled show-score :colors="['#FFD700', '#FFD700', '#FFD700']" />
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

    <!-- 游戏开发中弹窗 -->
    <el-dialog 
      v-model="showGameDialog" 
      :title="currentGame?.title || '游戏'" 
      width="500px"
      class="game-dialog"
      :close-on-click-modal="false"
    >
      <div class="developing-container">
        <div class="developing-icon">🎮</div>
        <h2 class="developing-title">该模块开发中</h2>
        <p class="developing-desc">游戏功能正在紧锣密鼓地开发中，敬请期待！</p>
        <div class="developing-progress">
          <div class="progress-bar">
            <div class="progress-fill" :style="{ width: gameProgress + '%' }"></div>
          </div>
          <span class="progress-text">开发进度 {{ gameProgress }}%</span>
        </div>
        <div class="game-preview">
          <h4>📖 游戏简介</h4>
          <p>{{ currentGame?.description || '暂无简介' }}</p>
          <div class="preview-tags">
            <span class="preview-tag">🎯预计上线：2026年Q2</span>
            <span class="preview-tag">⭐难度：{{ getDifficultyText(currentGame?.difficulty) }}</span>
          </div>
        </div>
      </div>
      <template #footer>
        <div class="game-footer">
          <el-button @click="closeGame">关闭</el-button>
          <el-button type="primary" @click="notifyMe">上线通知我</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue';
import { ElMessage } from 'element-plus';
import topComponent from '@/components/topComponent.vue';

// 游戏数据
const gameList = ref([
  {
    id: 1,
    title: '历史朝代排序',
    description: '将历史朝代按照时间顺序正确排列，考验你的历史知识！',
    category: 'puzzle',
    categoryIcon: '🧩',
    difficulty: 'easy',
    playCount: 128,
    rating: 4.5,
    progress: 85
  },
  {
    id: 2,
    title: '人物配对',
    description: '将历史人物与其主要成就正确配对，挑战你的记忆力！',
    category: 'matching',
    categoryIcon: '🃏',
    difficulty: 'easy',
    playCount: 95,
    rating: 4.3,
    progress: 70
  },
  {
    id: 3,
    title: '历史问答',
    description: '回答历史知识问题，每答对一题获得积分，冲击排行榜！',
    category: 'quiz',
    categoryIcon: '❓',
    difficulty: 'medium',
    playCount: 256,
    rating: 4.7,
    progress: 90
  },
  {
    id: 4,
    title: '丝绸之路冒险',
    description: '扮演古代商人，沿着丝绸之路进行贸易冒险！',
    category: 'adventure',
    categoryIcon: '🐫',
    difficulty: 'hard',
    playCount: 67,
    rating: 4.8,
    progress: 45
  },
  {
    id: 5,
    title: '古代建筑大师',
    description: '了解并建造中国古代著名建筑，学习建筑知识！',
    category: 'building',
    categoryIcon: '🏛️',
    difficulty: 'medium',
    playCount: 43,
    rating: 4.6,
    progress: 60
  },
  {
    id: 6,
    title: '成语接龙',
    description: '传统成语接龙游戏，考验你的成语储备量！',
    category: 'word',
    categoryIcon: '📝',
    difficulty: 'easy',
    playCount: 189,
    rating: 4.4,
    progress: 75
  }
]);

// 分类列表
const categories = [
  { value: 'all', label: '全部', icon: '🎮' },
  { value: 'puzzle', label: '益智', icon: '🧩' },
  { value: 'quiz', label: '问答', icon: '❓' },
  { value: 'matching', label: '配对', icon: '🃏' },
  { value: 'adventure', label: '冒险', icon: '🐫' },
  { value: 'building', label: '建造', icon: '🏛️' },
  { value: 'word', label: '文字', icon: '📝' }
];

// 状态
const currentCategory = ref('all');
const showGameDialog = ref(false);
const currentGame = ref(null);

// 计算属性
const filteredGames = computed(() => {
  if (currentCategory.value === 'all') {
    return gameList.value;
  }
  return gameList.value.filter(g => g.category === currentCategory.value);
});

const totalPlayCount = computed(() => {
  return gameList.value.reduce((sum, g) => sum + g.playCount, 0);
});

const avgRating = computed(() => {
  const total = gameList.value.reduce((sum, g) => sum + g.rating, 0);
  return (total / gameList.value.length).toFixed(1);
});

// 当前游戏的开发进度
const gameProgress = computed(() => {
  return currentGame.value?.progress || 50;
});

// 获取难度文本
function getDifficultyText(difficulty) {
  const map = { easy: '入门', medium: '进阶', hard: '挑战' };
  return map[difficulty] || difficulty;
}

// 打开游戏
function openGame(game) {
  currentGame.value = game;
  showGameDialog.value = true;
  
  // 增加游玩次数
  game.playCount++;
  
  ElMessage.info(`《${game.title}》游戏开发中，敬请期待！`);
}

// 关闭弹窗
function closeGame() {
  showGameDialog.value = false;
  currentGame.value = null;
}

// 上线通知我
function notifyMe() {
  ElMessage.success(`已登记，游戏《${currentGame.value?.title}》上线后将通知您！`);
}

// 获取游戏封面图标
function getGameIcon(game) {
  const icons = {
    puzzle: '🧩',
    matching: '🃏',
    quiz: '❓',
    adventure: '🐫',
    building: '🏛️',
    word: '📝'
  };
  return icons[game.category] || '🎮';
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
  color: rgba(255,255,255,0.7);
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
  color: rgba(255,255,255,0.6);
}

.hero-wave {
  height: 60px;
  background: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 1440 320'%3E%3Cpath fill='%231a1a2e' fill-opacity='1' d='M0,96L48,112C96,128,192,160,288,160C384,160,480,128,576,122.7C672,117,768,139,864,154.7C960,171,1056,181,1152,165.3C1248,149,1344,107,1392,85.3L1440,64L1440,320L1392,320C1344,320,1248,320,1152,320C1056,320,960,320,864,320C768,320,672,320,576,320C480,320,384,320,288,320C192,320,96,320,48,320L0,320Z'%3E%3C/path%3E%3C/svg%3E") no-repeat bottom;
  background-size: cover;
}

/* 筛选标签 */
.filter-tabs {
  display: flex;
  justify-content: center;
  gap: 12px;
  margin: 20px 0 30px;
  flex-wrap: wrap;
}

.filter-tab {
  padding: 8px 20px;
  background: rgba(255,255,255,0.08);
  border: 1px solid rgba(255,215,0,0.3);
  border-radius: 30px;
  color: #ccc;
  cursor: pointer;
  transition: all 0.3s;
  font-size: 14px;
}

.filter-tab:hover, .filter-tab.active {
  background: #FFD700;
  color: #1a1a1a;
  border-color: #FFD700;
}

/* 游戏网格 */
.game-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 30px;
  padding: 20px;
}

.game-card {
  background: linear-gradient(135deg, rgba(30,30,40,0.9), rgba(20,20,30,0.95));
  backdrop-filter: blur(10px);
  border-radius: 20px;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.4s cubic-bezier(0.2, 0.9, 0.4, 1.1);
  border: 1px solid rgba(255,215,0,0.2);
}

.game-card:hover {
  transform: translateY(-8px);
  border-color: #FFD700;
  box-shadow: 0 20px 40px rgba(0,0,0,0.3);
}

.game-cover {
  position: relative;
  height: 200px;
  overflow: hidden;
  background: linear-gradient(135deg, #2a2a3a, #1a1a2a);
  display: flex;
  align-items: center;
  justify-content: center;
}

.cover-placeholder {
  text-align: center;
}

.placeholder-icon {
  font-size: 64px;
  display: block;
  margin-bottom: 10px;
}

.placeholder-text {
  font-size: 16px;
  color: #FFD700;
  font-weight: 500;
}

.play-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0,0,0,0.7);
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  opacity: 0;
  transition: opacity 0.3s;
}

.game-card:hover .play-overlay {
  opacity: 1;
}

.play-icon {
  font-size: 48px;
  color: #FFD700;
  margin-bottom: 8px;
}

.play-text {
  color: #FFD700;
  font-size: 16px;
  font-weight: bold;
}

.game-badge {
  position: absolute;
  top: 12px;
  right: 12px;
  padding: 4px 12px;
  border-radius: 20px;
  font-size: 11px;
  font-weight: 500;
}

.game-badge.easy {
  background: rgba(76,175,80,0.9);
  color: white;
}

.game-badge.medium {
  background: rgba(255,152,0,0.9);
  color: white;
}

.game-badge.hard {
  background: rgba(244,67,54,0.9);
  color: white;
}

.game-info {
  padding: 18px;
}

.game-title {
  font-size: 18px;
  font-weight: bold;
  color: #FFD700;
  margin-bottom: 8px;
}

.game-desc {
  font-size: 13px;
  color: #ccc;
  line-height: 1.5;
  margin-bottom: 12px;
}

.game-meta {
  display: flex;
  justify-content: space-between;
  font-size: 12px;
  color: #aaa;
  margin-bottom: 10px;
}

.game-category {
  display: flex;
  align-items: center;
  gap: 4px;
}

.game-rating :deep(.el-rate) {
  height: 20px;
}

.game-rating :deep(.el-rate__item) {
  font-size: 14px;
}

/* 开发中弹窗样式 */
.game-dialog :deep(.el-dialog) {
  background: linear-gradient(135deg, #1a1a2e, #0f0f1a);
  border: 1px solid rgba(255,215,0,0.3);
  border-radius: 20px;
}

.game-dialog :deep(.el-dialog__header) {
  border-bottom: 1px solid rgba(255,215,0,0.2);
  padding: 16px 24px;
}

.game-dialog :deep(.el-dialog__title) {
  color: #FFD700;
  font-size: 20px;
}

.game-dialog :deep(.el-dialog__body) {
  padding: 30px;
}

.developing-container {
  text-align: center;
}

.developing-icon {
  font-size: 80px;
  margin-bottom: 20px;
  animation: bounce 2s ease-in-out infinite;
}

@keyframes bounce {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-10px); }
}

.developing-title {
  font-size: 24px;
  color: #FFD700;
  margin-bottom: 12px;
}

.developing-desc {
  color: #ccc;
  margin-bottom: 24px;
}

.developing-progress {
  margin: 20px 0;
}

.progress-bar {
  width: 80%;
  margin: 0 auto 10px;
  height: 8px;
  background: rgba(255,255,255,0.2);
  border-radius: 10px;
  overflow: hidden;
}

.progress-fill {
  height: 100%;
  background: linear-gradient(90deg, #FFD700, #FFA500);
  border-radius: 10px;
  transition: width 0.5s ease;
}

.progress-text {
  font-size: 12px;
  color: #aaa;
}

.game-preview {
  margin-top: 24px;
  padding: 20px;
  background: rgba(255,255,255,0.05);
  border-radius: 16px;
  text-align: left;
}

.game-preview h4 {
  color: #FFD700;
  margin-bottom: 10px;
  font-size: 14px;
}

.game-preview p {
  color: #ccc;
  font-size: 13px;
  line-height: 1.6;
  margin-bottom: 15px;
}

.preview-tags {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

.preview-tag {
  padding: 4px 12px;
  background: rgba(255,215,0,0.15);
  border-radius: 20px;
  font-size: 11px;
  color: #FFD700;
}

.game-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

/* 空状态 */
.empty-state {
  text-align: center;
  padding: 80px;
  color: #666;
}

.empty-icon {
  font-size: 80px;
  margin-bottom: 20px;
}

.empty-state h3 {
  color: #FFD700;
  margin-bottom: 10px;
}

/* 响应式 */
@media (max-width: 768px) {
  .hero-title {
    font-size: 32px;
  }
  
  .hero-stats {
    gap: 24px;
  }
  
  .stat-number {
    font-size: 20px;
  }
  
  .game-grid {
    grid-template-columns: 1fr;
    gap: 20px;
  }
  
  .filter-tabs {
    gap: 8px;
  }
  
  .filter-tab {
    padding: 6px 14px;
    font-size: 12px;
  }
  
  .developing-title {
    font-size: 20px;
  }
  
  .developing-icon {
    font-size: 60px;
  }
}
</style>