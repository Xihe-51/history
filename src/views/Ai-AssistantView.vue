<template>
  <div class="ai-assistant-page">
    <!-- 顶部导航栏 -->
    <top-component 
      ref="topNavRef" 
      class="top-nav"
      :class="{ 'nav-active': activeDynastyIndex !== -1, 'nav-bounce': isNavBouncing }"
    />
    
    <div class="ai-container">
      <!-- 页面标题区 -->
      <div class="hero-section">
        <div class="hero-content">
          <h1 class="hero-title">🤖AI科普助手</h1>
          <p class="hero-subtitle">智能问答 · 延伸解读 · 趣味科普</p>
          <div class="hero-stats">
            <div class="stat-item">
              <span class="stat-number">{{ totalQuestions }}</span>
              <span class="stat-label">今日问答</span>
            </div>
            <div class="stat-item">
              <span class="stat-number">{{ totalExtensions }}</span>
              <span class="stat-label">延伸内容</span>
            </div>
            <div class="stat-item">
              <span class="stat-number">{{ totalRecommendations }}</span>
              <span class="stat-label">推荐内容</span>
            </div>
          </div>
        </div>
        <div class="hero-wave"></div>
      </div>

      <!-- 上下文提示栏 -->
      <div v-if="contextInfo" class="context-banner">
        <span class="context-icon">🎯</span>
        <span class="context-text">
          当前正在讨论：<strong>{{ contextInfo.name }}</strong>
          <span class="context-type">{{ getContextTypeLabel(contextInfo.type) }}</span>
        </span>
        <button class="clear-context" @click="clearContext">切换话题</button>
      </div>

      <!-- 双栏布局 -->
      <div class="ai-layout">
        <!-- 左侧：智能问答区 -->
        <div class="chat-section">
          <div class="section-header">
            <h2>💬 智能问答</h2>
            <button class="clear-history-btn" @click="clearHistory" v-if="chatHistory.length">
              清空记录
            </button>
          </div>
          
          <!-- 问答记录区 -->
          <div class="chat-history" ref="chatHistoryRef">
            <div v-if="chatHistory.length === 0" class="empty-chat">
              <div class="empty-icon">🤖</div>
              <p>{{ contextInfo ? `问我关于"${contextInfo.name}"的任何问题！` : '问我任何历史问题！' }}</p>
              <div class="example-questions">
                <span>例如：</span>
                <button v-for="suggestion in suggestions" :key="suggestion" class="example-btn" @click="askQuestion(suggestion)">
                  {{ suggestion }}
                </button>
              </div>
            </div>
            
            <div v-for="(item, idx) in chatHistory" :key="idx" class="chat-item">
              <div class="chat-question">
                <span class="avatar user-avatar">👤</span>
                <div class="question-content">{{ item.question }}</div>
                <button class="delete-question" @click="deleteQuestion(idx)">✕</button>
              </div>
              <div class="chat-answer">
                <span class="avatar ai-avatar">🤖</span>
                <div class="answer-content" v-html="item.answer"></div>
              </div>
            </div>
            
            <div v-if="isTyping" class="chat-item typing">
              <div class="chat-answer">
                <span class="avatar ai-avatar">🤖</span>
                <div class="answer-content">
                  <span class="typing-dots">思考中<span>.</span><span>.</span><span>.</span></span>
                </div>
              </div>
            </div>
          </div>
          
          <!-- 输入区 -->
          <div class="chat-input-area">
            <input 
              type="text" 
              v-model="currentQuestion" 
              :placeholder="contextInfo ? `关于「${contextInfo.name}」的问题...` : '输入您的问题...'"
              @keyup.enter="askQuestion"
              class="chat-input"
            >
            <button class="send-btn" @click="askQuestion" :disabled="!currentQuestion.trim() || isTyping">
              <span>发送</span>
            </button>
          </div>
        </div>

        <!-- 右侧：内容拓展 + 个性化推荐 -->
        <div class="right-sidebar">
          <!-- 延伸解读区 -->
          <div class="extension-section">
            <div class="section-header">
              <h2>📖延伸解读</h2>
              <button class="refresh-btn" @click="generateExtension" :disabled="isLoadingExtension">
                {{ isLoadingExtension ? '生成中...' : '换一换' }}
              </button>
            </div>
            
            <div v-if="currentExtension" class="extension-content">
              <div class="extension-title">{{ currentExtension.title }}</div>
              <div class="extension-text" v-html="currentExtension.content"></div>
              <div class="extension-tags">
                <span class="tag" v-for="tag in currentExtension.tags" :key="tag">{{ tag }}</span>
              </div>
            </div>
            <div v-else class="loading-extension">
              <div class="loading-spinner"></div>
              <p>正在生成延伸解读...</p>
            </div>
          </div>

          <!-- 趣味冷知识区 -->
          <div class="fun-fact-section">
            <div class="section-header">
              <h2>🎉 趣味冷知识</h2>
              <button class="refresh-btn" @click="generateFunFact" :disabled="isLoadingFunFact">
                {{ isLoadingFunFact ? '生成中...' : '换一个' }}
              </button>
            </div>
            
            <div v-if="currentFunFact" class="fun-fact-content">
              <div class="fact-icon">💡</div>
              <div class="fact-text" v-html="currentFunFact.text"></div>
              <div class="fact-image" v-if="currentFunFact.image">
                <img :src="currentFunFact.image" :alt="currentFunFact.title">
              </div>
            </div>
            <div v-else class="loading-fact">
              <div class="loading-spinner"></div>
              <p>加载冷知识中...</p>
            </div>
          </div>

          <!-- 个性化推荐区 -->
          <div class="recommendation-section">
            <div class="section-header">
              <h2>🔍猜你喜欢</h2>
              <button class="refresh-btn" @click="refreshRecommendations">换一批</button>
            </div>
            
            <div class="recommendation-list">
              <div v-for="item in recommendations" :key="item.id" class="recommend-card" @click="goToDetail(item)">
                <div class="recommend-icon" :class="item.type">📌</div>
                <div class="recommend-info">
                  <div class="recommend-title">{{ item.title }}</div>
                  <div class="recommend-desc">{{ item.desc }}</div>
                </div>
                <button class="dislike-btn" @click.stop="dislikeRecommendation(item.id)" title="不感兴趣">✕</button>
              </div>
              <div v-if="recommendations.length === 0" class="empty-recommend">
                暂无推荐，浏览更多内容获取个性化推荐
              </div>
            </div>
            
            <div class="interest-blocked" v-if="blockedInterests.length > 0">
              <div class="blocked-header">
                <span>已屏蔽兴趣</span>
                <button class="clear-blocked" @click="clearBlocked">清空</button>
              </div>
              <div class="blocked-tags">
                <span v-for="tag in blockedInterests" :key="tag" class="blocked-tag">
                  {{ tag }}
                  <button @click="unblockInterest(tag)">✕</button>
                </span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, nextTick, watch } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { ElMessage } from 'element-plus';
import topComponent from '@/components/topComponent.vue';
import request from '@/utils/request';

const router = useRouter();
const route = useRoute();

// 导航栏状态
const activeDynastyIndex = ref(-1);
const isNavBouncing = ref(false);

// 上下文信息
const contextInfo = ref(null);

// 聊天相关
const currentQuestion = ref('');
const chatHistory = ref([]);
const isTyping = ref(false);
const chatHistoryRef = ref(null);

// 延伸解读相关
const currentExtension = ref(null);
const isLoadingExtension = ref(false);

// 趣味冷知识相关
const currentFunFact = ref(null);
const isLoadingFunFact = ref(false);

// 推荐相关
const recommendations = ref([]);
const blockedInterests = ref([]);
const browsingHistory = ref([]);

// 建议问题列表
const suggestions = ref([]);

// 统计数据
const totalQuestions = computed(() => chatHistory.value.length);
const totalExtensions = ref(0);
const totalRecommendations = ref(0);

// 获取上下文类型标签
function getContextTypeLabel(type) {
  const labels = { event: '事件', person: '人物', allusion: '典故' };
  return labels[type] || '';
}

// 根据上下文生成建议问题
function generateSuggestionsByContext() {
  if (!contextInfo.value) {
    suggestions.value = [
      '安史之乱的影响是什么？',
      '李白的主要成就有哪些？',
      '卧薪尝胆的寓意是什么？'
    ];
    return;
  }
  
  const name = contextInfo.value.name;
  const type = contextInfo.value.type;
  
  if (type === 'event') {
    suggestions.value = [
      `${name}发生的具体背景是什么？`,
      `${name}的主要经过是怎样的？`,
      `${name}有什么历史影响？`,
      `与${name}相关的人物有哪些？`
    ];
  } else if (type === 'person') {
    suggestions.value = [
      `${name}的生平事迹有哪些？`,
      `${name}的主要成就是什么？`,
      `${name}生活在什么朝代？`,
      `与${name}相关的典故有哪些？`
    ];
  } else if (type === 'allusion') {
    suggestions.value = [
      `"${name}"这个典故的出处是哪里？`,
      `"${name}"的具体含义是什么？`,
      `"${name}"有什么现代应用？`,
      `与"${name}"相关的历史人物是谁？`
    ];
  }
}

// 构建带上下文的问题
function buildContextQuestion(question) {
  if (!contextInfo.value) return question;
  
  const name = contextInfo.value.name;
  const type = contextInfo.value.type;
  
  // 检查问题是否已经包含上下文关键词
  const keywords = ['这个事件', '该事件', '这个人', '该人物', '这个典故', '该典故', name];
  const hasContextRef = keywords.some(kw => question.includes(kw));
  
  if (hasContextRef) {
    return question;
  }
  
  // 根据类型添加上下文前缀
  const prefixes = {
    event: `关于历史事件"${name}"，`,
    person: `关于历史人物"${name}"，`,
    allusion: `关于典故"${name}"，`
  };
  
  return prefixes[type] + question;
}

// 滚动到底部
function scrollToBottom() {
  nextTick(() => {
    if (chatHistoryRef.value) {
      chatHistoryRef.value.scrollTop = chatHistoryRef.value.scrollHeight;
    }
  });
}

async function askQuestion(question) {
  // 处理不同的参数类型
  let q = '';
  
  if (question === undefined || question === null) {
    q = currentQuestion.value.trim();
  } else if (typeof question === 'string') {
    q = question.trim();
  } else if (typeof question === 'object' && question.target) {
    q = question.target.innerText.trim();
    if (q === '发送' || q === 'send') {
      q = currentQuestion.value.trim();
    }
  } else {
    q = String(question).trim();
  }
  
  if (!q) {
    ElMessage.warning('请输入问题');
    return;
  }
  
  // 清空输入框
  currentQuestion.value = '';
  
  // 构建带上下文的问题
  const finalQuestion = buildContextQuestion(q);
  console.log('发送问题（带上下文）:', finalQuestion);
  
  // 添加问题到历史
  chatHistory.value.push({
    question: q,
    answer: '',
    timestamp: Date.now()
  });
  
  isTyping.value = true;
  
  await nextTick();
  scrollToBottom();
  
  try {
    const res = await request.post('/ai/chat', { question: finalQuestion });
    console.log('AI 回答原始响应:', res);
    
    if (res.data && res.data.code === 200 && res.data.data) {
      chatHistory.value[chatHistory.value.length - 1].answer = res.data.data.answer;
    } else {
      chatHistory.value[chatHistory.value.length - 1].answer = `<p>抱歉，我暂时无法回答这个问题。${res.data?.msg || '请稍后再试。'}</p>`;
    }
  } catch (error) {
    console.error('AI 问答失败:', error);
    chatHistory.value[chatHistory.value.length - 1].answer = `<p>网络错误，请检查后端服务是否正常运行。</p>`;
    ElMessage.error('AI 问答失败，请检查网络');
  } finally {
    isTyping.value = false;
    
    localStorage.setItem('ai_chat_history', JSON.stringify(chatHistory.value));
    recordInterest(finalQuestion);
    
    nextTick(() => {
      scrollToBottom();
    });
  }
}

function recordInterest(question) {
  request.post('/ai/record-interest', { question }).catch(err => console.error('记录兴趣失败:', err));
  
  browsingHistory.value.unshift({ keyword: question.substring(0, 20), timestamp: Date.now() });
  if (browsingHistory.value.length > 20) {
    browsingHistory.value.pop();
  }
  localStorage.setItem('user_browsing_history', JSON.stringify(browsingHistory.value));
  generateRecommendations();
}

function deleteQuestion(index) {
  chatHistory.value.splice(index, 1);
  localStorage.setItem('ai_chat_history', JSON.stringify(chatHistory.value));
  ElMessage.success('已删除');
}

function clearHistory() {
  chatHistory.value = [];
  localStorage.setItem('ai_chat_history', JSON.stringify(chatHistory.value));
  ElMessage.success('历史记录已清空');
}

// 清除上下文
function clearContext() {
  contextInfo.value = null;
  generateSuggestionsByContext();
  ElMessage.info('已切换为通用话题模式');
}

// 初始化上下文（从路由参数获取）
function initContext() {
  const context = route.query.context;
  const name = route.query.name;
  const id = route.query.id;
  
  if (context && name) {
    contextInfo.value = {
      type: context,
      name: decodeURIComponent(name),
      id: id
    };
    
    // 生成建议问题
    generateSuggestionsByContext();
    
    // 如果有历史记录且没有欢迎消息，添加欢迎消息
    if (chatHistory.value.length === 0) {
      const welcomeMessages = {
        event: `你好！我是 AI 历史助手，很高兴为你解答关于历史事件"${name}"的问题。`,
        person: `你好！我是 AI 历史助手，很高兴为你解答关于历史人物"${name}"的问题。`,
        allusion: `你好！我是 AI 历史助手，很高兴为你解答关于典故"${name}"的问题。`
      };
      
      const welcomeMsg = welcomeMessages[context] || `你好！我是 AI 历史助手，很高兴为你解答关于"${name}"的问题。`;
      
      chatHistory.value.push({
        question: '',
        answer: welcomeMsg,
        timestamp: Date.now(),
        isWelcome: true
      });
    }
  } else {
    contextInfo.value = null;
    generateSuggestionsByContext();
  }
  
  scrollToBottom();
}

// 监听路由变化
watch(() => route.query, () => {
  initContext();
}, { deep: true });

async function generateExtension() {
  isLoadingExtension.value = true;
  try {
    const res = await request.get('/ai/extension');
    console.log('延伸解读原始响应:', res);
    
    if (res.data && res.data.code === 200 && res.data.data) {
      currentExtension.value = res.data.data;
      totalExtensions.value++;
    } else {
      currentExtension.value = {
        title: '中国历史的文化传承',
        content: '<p>中国历史源远流长，五千年的文明积淀形成了独特的文化传统。</p>',
        tags: ['文化', '历史']
      };
    }
  } catch (error) {
    console.error('生成延伸解读失败:', error);
    currentExtension.value = {
      title: '中国历史的文化传承',
      content: '<p>中国历史源远流长，五千年的文明积淀形成了独特的文化传统。</p>',
      tags: ['文化', '历史']
    };
  } finally {
    isLoadingExtension.value = false;
  }
}

async function generateFunFact() {
  isLoadingFunFact.value = true;
  try {
    const res = await request.get('/ai/fun-fact');
    console.log('趣味冷知识原始响应:', res);
    
    if (res.data && res.data.code === 200 && res.data.data) {
      currentFunFact.value = res.data.data;
    } else {
      currentFunFact.value = {
        text: '秦始皇统一六国后，曾五次巡游天下，最后一次在途中病逝。为掩盖死讯，车队每天照常送饭，还载着一车臭鲍鱼混淆气味。',
        title: '秦始皇的趣味冷知识',
        image: null
      };
    }
  } catch (error) {
    console.error('生成冷知识失败:', error);
    currentFunFact.value = {
      text: '秦始皇统一六国后，曾五次巡游天下，最后一次在途中病逝。',
      title: '秦始皇的趣味冷知识',
      image: null
    };
  } finally {
    isLoadingFunFact.value = false;
  }
}

async function generateRecommendations() {
  try {
    const res = await request.get('/ai/recommendations');
    console.log('推荐内容原始响应:', res);
    
    if (res.data && res.data.code === 200 && res.data.data) {
      recommendations.value = res.data.data;
      totalRecommendations.value = recommendations.value.length;
    } else {
      generateLocalRecommendations();
    }
  } catch (error) {
    console.error('获取推荐失败:', error);
    generateLocalRecommendations();
  }
}

function generateLocalRecommendations() {
  const localRecommendations = [
    { id: 1, type: 'event', title: '贞观之治', desc: '唐太宗开创的盛世', category: '政治' },
    { id: 2, type: 'event', title: '赤壁之战', desc: '奠定三国鼎立格局', category: '军事' },
    { id: 3, type: 'person', title: '苏轼', desc: '宋代文学巨匠', category: '文学' },
    { id: 4, type: 'person', title: '王昭君', desc: '出塞和亲的传奇女子', category: '历史' },
    { id: 5, type: 'allusion', title: '指鹿为马', desc: '权臣赵高的故事', category: '权谋' }
  ];
  
  let filtered = localRecommendations.filter(rec => {
    return !blockedInterests.value.some(blocked => rec.category === blocked || rec.title === blocked);
  });
  
  recommendations.value = filtered.slice(0, 5);
  totalRecommendations.value = recommendations.value.length;
}

function refreshRecommendations() {
  generateRecommendations();
  ElMessage.success('推荐已刷新');
}

function dislikeRecommendation(id) {
  const item = recommendations.value.find(r => r.id === id);
  if (item) {
    blockedInterests.value.push(item.category || item.title);
    localStorage.setItem('ai_blocked_interests', JSON.stringify(blockedInterests.value));
    generateRecommendations();
    ElMessage.success('已屏蔽此类内容');
  }
}

function unblockInterest(tag) {
  const index = blockedInterests.value.indexOf(tag);
  if (index !== -1) {
    blockedInterests.value.splice(index, 1);
    localStorage.setItem('ai_blocked_interests', JSON.stringify(blockedInterests.value));
    generateRecommendations();
    ElMessage.success('已取消屏蔽');
  }
}

function clearBlocked() {
  blockedInterests.value = [];
  localStorage.setItem('ai_blocked_interests', JSON.stringify(blockedInterests.value));
  generateRecommendations();
  ElMessage.success('已清空屏蔽列表');
}

function goToDetail(item) {
  if (item.type === 'event') {
    router.push({ path: '/event', query: { id: item.id } });
  } else if (item.type === 'person') {
    router.push({ path: '/person', query: { keyword: item.title } });
  } else if (item.type === 'allusion') {
    router.push({ path: '/allusion', query: { keyword: item.title } });
  }
}

onMounted(() => {
  // 加载本地存储
  const savedHistory = localStorage.getItem('ai_chat_history');
  if (savedHistory) {
    chatHistory.value = JSON.parse(savedHistory);
  }
  
  const savedBlocked = localStorage.getItem('ai_blocked_interests');
  if (savedBlocked) {
    blockedInterests.value = JSON.parse(savedBlocked);
  }
  
  const savedBrowsing = localStorage.getItem('user_browsing_history');
  if (savedBrowsing) {
    browsingHistory.value = JSON.parse(savedBrowsing);
  }
  
  // 初始化上下文
  initContext();
  
  // 生成初始内容
  generateExtension();
  generateFunFact();
  generateRecommendations();
  
  nextTick(() => {
    scrollToBottom();
  });
});
</script>

<style scoped>
.ai-assistant-page {
  min-height: 100vh;
  background: linear-gradient(135deg, #0a0a1a 0%, #1a1a2e 100%);
  overflow-x: hidden;
}

.ai-container {
  max-width: 1400px;
  margin: 0 auto;
  padding: 20px;
}

.hero-section {
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

.ai-layout {
  display: grid;
  grid-template-columns: 1fr 380px;
  gap: 30px;
  margin-top: 20px;
}

.chat-section {
  background: rgba(0,0,0,0.3);
  backdrop-filter: blur(10px);
  border-radius: 28px;
  border: 1px solid rgba(255,215,0,0.2);
  overflow: hidden;
  display: flex;
  flex-direction: column;
  height: calc(100vh - 280px);
  min-height: 600px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 24px;
  border-bottom: 1px solid rgba(255,215,0,0.2);
}

.section-header h2 {
  font-size: 20px;
  color: #FFD700;
  margin: 0;
}

.clear-history-btn {
  background: rgba(255,215,0,0.15);
  border: 1px solid rgba(255,215,0,0.3);
  border-radius: 20px;
  padding: 5px 12px;
  color: #FFD700;
  font-size: 12px;
  cursor: pointer;
  transition: all 0.3s;
}

.clear-history-btn:hover {
  background: rgba(255,215,0,0.3);
}

/* 聊天历史区域 */
.chat-history {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
}

.chat-history::-webkit-scrollbar {
  width: 6px;
}

.chat-history::-webkit-scrollbar-track {
  background: rgba(255,255,255,0.1);
  border-radius: 3px;
}

.chat-history::-webkit-scrollbar-thumb {
  background: #FFD700;
  border-radius: 3px;
}

.empty-chat {
  text-align: center;
  padding: 60px 20px;
  color: #aaa;
}

.empty-icon {
  font-size: 64px;
  margin-bottom: 20px;
}

.example-questions {
  margin-top: 20px;
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  justify-content: center;
}

.example-btn {
  background: rgba(255,215,0,0.15);
  border: 1px solid rgba(255,215,0,0.3);
  border-radius: 20px;
  padding: 6px 14px;
  color: #FFD700;
  font-size: 12px;
  cursor: pointer;
  transition: all 0.3s;
}

.example-btn:hover {
  background: rgba(255,215,0,0.3);
}

.chat-item {
  margin-bottom: 20px;
}

.chat-question {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  margin-bottom: 12px;
  justify-content: flex-end;
}

.chat-question .user-avatar {
  order: 2;
}

.question-content {
  background: rgba(255,215,0,0.15);
  padding: 12px 16px;
  border-radius: 20px 20px 4px 20px;
  max-width: 80%;
  color: #fff;
  line-height: 1.5;
}

.delete-question {
  background: none;
  border: none;
  color: #666;
  cursor: pointer;
  font-size: 12px;
  opacity: 0;
  transition: opacity 0.3s;
}

.chat-question:hover .delete-question {
  opacity: 1;
}

.delete-question:hover {
  color: #FFD700;
}

.chat-answer {
  display: flex;
  align-items: flex-start;
  gap: 12px;
}

.avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 16px;
  flex-shrink: 0;
}

.user-avatar {
  background: linear-gradient(135deg, #FFD700, #FFA500);
}

.ai-avatar {
  background: rgba(255,255,255,0.2);
}

.answer-content {
  background: rgba(255,255,255,0.08);
  padding: 12px 16px;
  border-radius: 20px 20px 20px 4px;
  max-width: 85%;
  color: #e0e0e0;
  line-height: 1.6;
  font-size: 14px;
}

.answer-content :deep(p) {
  margin-bottom: 10px;
}

.answer-content :deep(ul) {
  margin: 8px 0;
  padding-left: 20px;
}

.answer-content :deep(li) {
  margin-bottom: 4px;
}

.typing-dots span {
  animation: blink 1.4s infinite;
  animation-fill-mode: both;
}

.typing-dots span:nth-child(2) { animation-delay: 0.2s; }
.typing-dots span:nth-child(3) { animation-delay: 0.4s; }

@keyframes blink {
  0%, 80%, 100% { opacity: 0; }
  40% { opacity: 1; }
}

/* 输入区 */
.chat-input-area {
  display: flex;
  gap: 12px;
  padding: 20px;
  border-top: 1px solid rgba(255,215,0,0.2);
  background: rgba(0,0,0,0.3);
}

.chat-input {
  flex: 1;
  padding: 12px 18px;
  background: rgba(255,255,255,0.08);
  border: 1px solid rgba(255,215,0,0.3);
  border-radius: 30px;
  color: #fff;
  font-size: 14px;
  outline: none;
  transition: all 0.3s;
}

.chat-input:focus {
  border-color: #FFD700;
  box-shadow: 0 0 10px rgba(255,215,0,0.2);
}

.send-btn {
  padding: 12px 24px;
  background: linear-gradient(135deg, #FFD700, #FFA500);
  border: none;
  border-radius: 30px;
  color: #1a1a1a;
  font-weight: bold;
  cursor: pointer;
  transition: all 0.3s;
}

.send-btn:hover:not(:disabled) {
  transform: scale(1.02);
  box-shadow: 0 4px 15px rgba(255,215,0,0.3);
}

.send-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.right-sidebar {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

/* 公共卡片样式 */
.extension-section,
.fun-fact-section,
.recommendation-section {
  background: rgba(0,0,0,0.3);
  backdrop-filter: blur(10px);
  border-radius: 24px;
  border: 1px solid rgba(255,215,0,0.2);
  overflow: hidden;
}

.refresh-btn {
  background: rgba(255,215,0,0.15);
  border: 1px solid rgba(255,215,0,0.3);
  border-radius: 20px;
  padding: 5px 12px;
  color: #FFD700;
  font-size: 12px;
  cursor: pointer;
  transition: all 0.3s;
}

.refresh-btn:hover:not(:disabled) {
  background: rgba(255,215,0,0.3);
}

.refresh-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

/* 延伸解读 */
.extension-content,
.fun-fact-content {
  padding: 20px;
}

.extension-title {
  font-size: 18px;
  font-weight: bold;
  color: #FFD700;
  margin-bottom: 12px;
}

.extension-text {
  font-size: 14px;
  color: #e0e0e0;
  line-height: 1.6;
  margin-bottom: 12px;
}

.extension-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.extension-tags .tag {
  padding: 4px 10px;
  background: rgba(255,215,0,0.15);
  border-radius: 20px;
  font-size: 11px;
  color: #FFD700;
}

/* 趣味冷知识 */
.fact-icon {
  font-size: 32px;
  margin-bottom: 12px;
}

.fact-text {
  font-size: 14px;
  color: #e0e0e0;
  line-height: 1.6;
  margin-bottom: 12px;
}

.fact-image {
  margin-top: 12px;
}

.fact-image img {
  max-width: 100%;
  border-radius: 12px;
}

/* 推荐列表 */
.recommendation-list {
  padding: 16px;
  max-height: 300px;
  overflow-y: auto;
}

.recommend-card {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  background: rgba(255,255,255,0.05);
  border-radius: 16px;
  margin-bottom: 10px;
  cursor: pointer;
  transition: all 0.3s;
  position: relative;
}

.recommend-card:hover {
  background: rgba(255,215,0,0.1);
  transform: translateX(5px);
}

.recommend-icon {
  width: 40px;
  height: 40px;
  background: rgba(255,215,0,0.2);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
}

.recommend-info {
  flex: 1;
}

.recommend-title {
  font-size: 14px;
  font-weight: bold;
  color: #fff;
  margin-bottom: 4px;
}

.recommend-desc {
  font-size: 11px;
  color: #aaa;
}

.dislike-btn {
  background: none;
  border: none;
  color: #666;
  cursor: pointer;
  font-size: 12px;
  opacity: 0;
  transition: opacity 0.3s;
}

.recommend-card:hover .dislike-btn {
  opacity: 1;
}

.dislike-btn:hover {
  color: #FFD700;
}

.empty-recommend {
  text-align: center;
  padding: 40px;
  color: #666;
  font-size: 13px;
}

/* 屏蔽列表 */
.interest-blocked {
  padding: 16px;
  border-top: 1px solid rgba(255,215,0,0.2);
}

.blocked-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
  font-size: 13px;
  color: #aaa;
}

.clear-blocked {
  background: none;
  border: none;
  color: #FFD700;
  cursor: pointer;
  font-size: 11px;
}

.blocked-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.blocked-tag {
  background: rgba(255,77,77,0.2);
  border-radius: 20px;
  padding: 4px 10px;
  font-size: 11px;
  color: #FF4D4D;
  display: inline-flex;
  align-items: center;
  gap: 6px;
}

.blocked-tag button {
  background: none;
  border: none;
  color: #FF4D4D;
  cursor: pointer;
  font-size: 10px;
}

/* 加载动画 */
.loading-extension,
.loading-fact {
  text-align: center;
  padding: 40px;
  color: #aaa;
}

.loading-spinner {
  width: 40px;
  height: 40px;
  border: 3px solid rgba(255,215,0,0.2);
  border-top-color: #FFD700;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin: 0 auto 16px;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

/* 响应式 */
@media (max-width: 1024px) {
  .ai-layout {
    grid-template-columns: 1fr;
  }
  
  .chat-section {
    height: 500px;
  }
  
  .right-sidebar {
    display: grid;
    grid-template-columns: repeat(3, 1fr);
    gap: 20px;
  }
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
  
  .right-sidebar {
    grid-template-columns: 1fr;
  }
  
  .chat-section {
    height: 450px;
  }
  
  .question-content,
  .answer-content {
    max-width: 90%;
  }
}
.hero-content{
  margin-bottom:100px;
}
.context-banner {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 20px;
  background: rgba(255, 215, 0, 0.15);
  border: 1px solid rgba(255, 215, 0, 0.3);
  border-radius: 12px;
  margin-bottom: 20px;
}

.context-icon {
  font-size: 20px;
}

.context-text {
  flex: 1;
  color: #e0e0e0;
  font-size: 14px;
}

.context-text strong {
  color: #FFD700;
}

.context-type {
  display: inline-block;
  margin-left: 8px;
  padding: 2px 8px;
  background: rgba(255, 215, 0, 0.2);
  border-radius: 12px;
  font-size: 11px;
  color: #FFD700;
}

.clear-context {
  padding: 4px 12px;
  background: rgba(255, 255, 255, 0.1);
  border: 1px solid rgba(255, 215, 0, 0.3);
  border-radius: 20px;
  color: #ccc;
  font-size: 12px;
  cursor: pointer;
  transition: all 0.3s;
}

.clear-context:hover {
  background: rgba(255, 215, 0, 0.2);
  color: #FFD700;
}
.hero-content {
  margin-bottom: 100px;
}
</style>