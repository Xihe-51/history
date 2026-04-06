<template>
	<div class="history-app">
		<!-- 顶部导航栏 -->
		<top-component 
			ref="topNavRef" 
			class="top-nav"
			:class="{ 'nav-active': activeDynastyIndex !== -1, 'nav-bounce': isNavBouncing }"
		/>
		<!-- 主内容区域 -->
		<main class="app-main" :class="{ 'panel-open': showEventPanel || showComparePanel }">
			<div class="shell" id="shell">
				<div class="header">
					<h2 class="title">华夏天澜</h2>
					<h3 class="subtitle">历史科普系统</h3>
				</div>
				
				<!-- 筛选和搜索区域 -->
				<div class="filter-section">
					<!-- 关键词搜索 -->
					<div class="search-box">
						<input 
							type="text" 
							v-model="searchKeyword" 
							placeholder="搜索事件名称或关键词..."
							@input="handleSearch"
							class="search-input"
						>
						<span class="search-icon">🔍</span>
					</div>
					
					<!-- 筛选条件展开/收起按钮 -->
					<div class="filter-toggle" @click="showFilters = !showFilters">
						<span>高级筛选</span>
						<span class="toggle-icon">{{ showFilters ? '▼' : '▶' }}</span>
					</div>
					
					<!-- 筛选条件面板 -->
					<transition name="slide-down">
						<div v-if="showFilters" class="filter-panel">
							<div class="filter-row">
								<!-- 朝代筛选 -->
								<div class="filter-item">
									<label>朝代：</label>
									<select v-model="selectedDynastyFilter" class="filter-select">
										<option value="">全部朝代</option>
										<option v-for="item in timelineItems" :key="item.dynasty" :value="item.dynasty">
											{{ item.dynasty }}
										</option>
									</select>
								</div>
								
								<!-- 事件类型筛选 -->
								<div class="filter-item">
									<label>事件类型：</label>
									<div class="checkbox-group">
										<label v-for="type in eventTypes" :key="type.value" class="checkbox-label">
											<input 
												type="checkbox" 
												v-model="selectedEventTypes" 
												:value="type.value"
											>
											<span class="checkbox-custom" :style="{ backgroundColor: type.color }"></span>
											{{ type.label }}
										</label>
									</div>
								</div>
							</div>
							
							<div class="filter-row">
								<!-- 时间区间筛选 -->
								<div class="filter-item time-range">
									<label>时间区间：</label>
									<div class="range-inputs">
										<input 
											type="number" 
											v-model.number="timeRange.min" 
											placeholder="公元前用负数"
											class="range-input"
										>
										<span class="range-sep">—</span>
										<input 
											type="number" 
											v-model.number="timeRange.max" 
											placeholder="公元用正数"
											class="range-input"
										>
									</div>
									<div class="range-slider">
										<input 
											type="range" 
											:min="minYear" 
											:max="maxYear" 
											v-model.number="timeRange.min"
											class="slider slider-min"
										>
										<input 
											type="range" 
											:min="minYear" 
											:max="maxYear" 
											v-model.number="timeRange.max"
											class="slider slider-max"
										>
									</div>
								</div>
							</div>
							
							<div class="filter-row">
								<!-- 关联人物/典故筛选 -->
								<div class="filter-item">
									<label>关联人物/典故：</label>
									<input 
										type="text" 
										v-model="relatedKeyword" 
										placeholder="输入人物或典故名称..."
										class="related-input"
									>
								</div>
							</div>
							
							<div class="filter-actions">
								<button class="filter-btn apply-btn" @click="applyFilters">应用筛选</button>
								<button class="filter-btn reset-btn" @click="resetFilters">重置</button>
							</div>
						</div>
					</transition>
					
					<!-- 搜索结果列表 -->
					<transition name="slide-down">
						<div v-if="searchResults.length > 0" class="search-results">
							<div class="results-header">
								<h4>搜索结果 ({{ searchResults.length }})</h4>
								<button class="close-results" @click="clearSearch">×</button>
							</div>
							<ul class="results-list">
								<li 
									v-for="result in searchResults" 
									:key="result.id"
									class="result-item"
									@click="jumpToEvent(result)"
								>
									<span class="result-type" :class="result.type">{{ getEventTypeLabel(result.type) }}</span>
									<span class="result-name">{{ result.name }}</span>
									<span class="result-year">{{ formatYear(result.year) }}</span>
									<span class="result-dynasty">{{ result.dynasty }}</span>
								</li>
							</ul>
						</div>
					</transition>
					
					<!-- 当前激活的筛选条件标签 -->
					<div v-if="hasActiveFilters" class="active-filters">
						<span class="filter-tag" v-if="selectedDynastyFilter">
							朝代: {{ selectedDynastyFilter }}
							<span class="remove-tag" @click="selectedDynastyFilter = ''">×</span>
						</span>
						<span class="filter-tag" v-if="selectedEventTypes.length > 0">
							类型: {{ selectedEventTypes.map(t => getEventTypeLabel(t)).join(', ') }}
							<span class="remove-tag" @click="selectedEventTypes = []">×</span>
						</span>
						<span class="filter-tag" v-if="timeRange.min > minYear || timeRange.max < maxYear">
							时间: {{ formatYear(timeRange.min) }} - {{ formatYear(timeRange.max) }}
							<span class="remove-tag" @click="resetTimeRange">×</span>
						</span>
						<span class="filter-tag" v-if="relatedKeyword">
							关联: {{ relatedKeyword }}
							<span class="remove-tag" @click="relatedKeyword = ''">×</span>
						</span>
					</div>
				</div>
				
				<!-- 多节点对比工具栏 -->
				<div v-if="selectedEvents.length > 0" class="compare-toolbar">
					<div class="compare-info">
						已选中 {{ selectedEvents.length }} 个事件
						<button class="compare-btn" @click="showComparePanel = true">对比分析</button>
						<button class="clear-btn" @click="clearSelectedEvents">清空</button>
					</div>
				</div>
				
				<div class="time-line">
					<div 
						v-for="(item, index) in filteredTimelineItems" 
						:key="index"
						class="item" 
						:data-text="item.dataText"
						:class="{ 'has-highlight': hasHighlightInDynasty(item.dynasty) }"
					>
						<div class="content">
							<img :src="item.image" alt="" class="img">
							<h2 class="content-title">{{ item.dynasty }}</h2>
							<p class="content-desc">{{ item.description }}</p>
							
							<!-- 添加该朝代下的事件列表，增加复选框 -->
							<div class="dynasty-events" v-if="getFilteredEventsByDynasty(item.dynasty).length > 0">
								<div 
									v-for="event in getFilteredEventsByDynasty(item.dynasty)" 
									:key="event.id"
									class="event-item"
									:class="[
										event.type, 
										{ 'highlight': searchKeyword && event.name.toLowerCase().includes(searchKeyword.toLowerCase()) },
										{ 'related': relatedKeyword && isEventRelated(event, relatedKeyword) },
										{ 'selected': isEventSelected(event) }
									]"
								>
									<!-- 添加复选框 -->
									<input 
										type="checkbox" 
										:checked="isEventSelected(event)"
										@change="toggleEventSelection(event)"
										class="event-checkbox"
									>
									<span class="event-type-icon">{{ getEventTypeIcon(event.type) }}</span>
									<span class="event-name" @click="showEventPanel = true; currentEvent = event">{{ event.name }}</span>
									<span class="event-year">{{ formatYear(event.year) }}</span>
									<span v-if="relatedKeyword && isEventRelated(event, relatedKeyword)" class="related-badge">
										关联: {{ getRelatedInfo(event, relatedKeyword) }}
									</span>
									<!-- 添加查看详情按钮 -->
									<button class="view-detail-btn" @click="showEventPanel = true; currentEvent = event">详情</button>
								</div>
							</div>
						</div>
					</div>
				</div>
				
				<!-- 时间轴模式按钮 -->
				<div class="timeline-mode-section">
					<div class="mode-buttons">
						<button 
							v-for="mode in timelineModes" 
							:key="mode.value"
							class="mode-btn"
							:class="{ 'active': currentTimelineMode === mode.value }"
							@click="switchTimelineMode(mode.value)"
						>
							<span class="mode-icon">{{ mode.icon }}</span>
							<span class="mode-text">{{ mode.label }}</span>
						</button>
					</div>
					
					<!-- 根据当前模式显示不同的时间轴视图 -->
					<transition name="slide-up">
						<div v-if="showTimelineView" class="timeline-view-panel">
							<div class="view-header">
								<h3>{{ currentModeTitle }}</h3>
								<button class="close-view-btn" @click="showTimelineView = false">×</button>
							</div>
							<div class="view-content">
								<!-- 线性时间轴视图 -->
								<div v-if="currentTimelineMode === 'linear'" class="linear-timeline">
									<canvas ref="linearCanvas" class="timeline-canvas"></canvas>
								</div>
								
								<!-- 朝代分段时间轴视图 -->
								<div v-if="currentTimelineMode === 'dynasty'" class="dynasty-timeline">
									<div class="dynasty-grid">
										<div 
											v-for="(dynasty, index) in filteredTimelineItems" 
											:key="index"
											class="dynasty-card"
											:style="{ backgroundColor: getDynastyColor(index) }"
											@click="scrollToDynasty(index)"
										>
											<div class="dynasty-name">{{ dynasty.dynasty }}</div>
											<div class="dynasty-period">{{ getDynastyPeriod(dynasty) }}</div>
											<div class="dynasty-event-count">{{ getFilteredEventsByDynasty(dynasty.dynasty).length }}个事件</div>
										</div>
									</div>
								</div>
								
								<!-- 聚焦式时间轴视图 -->
								<div v-if="currentTimelineMode === 'focus'" class="focus-timeline">
									<div class="focus-controls">
										<select v-model="selectedDynasty" class="focus-select">
											<option value="">选择朝代聚焦</option>
											<option 
												v-for="(item, index) in filteredTimelineItems" 
												:key="index"
												:value="index"
											>
												{{ item.dynasty }}
											</option>
										</select>
										<div class="focus-info" v-if="selectedDynasty !== ''">
											<div class="focus-period">
												{{ getDynastyPeriod(timelineItems[selectedDynasty]) }}
											</div>
											<div class="focus-events">
												<h4>重大事件：</h4>
												<ul>
													<li v-for="event in getFilteredEventsByDynasty(timelineItems[selectedDynasty].dynasty)" :key="event.id">
														{{ event.name }} ({{ formatYear(event.year) }})
														<span v-if="relatedKeyword && isEventRelated(event, relatedKeyword)" class="related-tag">
															{{ getRelatedInfo(event, relatedKeyword) }}
														</span>
													</li>
												</ul>
											</div>
										</div>
									</div>
									<canvas ref="focusCanvas" class="timeline-canvas"></canvas>
								</div>
							</div>
						</div>
					</transition>
				</div>
			</div>
		</main>
		
		<!-- 右侧联动面板 -->
		<transition name="slide-left">
			<div v-if="showEventPanel && currentEvent" class="event-panel">
				<div class="panel-header">
					<h3>{{ currentEvent.name }}</h3>
					<button class="close-panel" @click="showEventPanel = false">×</button>
				</div>
				
				<div class="panel-tabs">
					<button 
						class="tab-btn" 
						:class="{ active: activePanelTab === '人物' }"
						@click="activePanelTab = '人物'"
					>关联人物</button>
					<button 
						class="tab-btn" 
						:class="{ active: activePanelTab === '典故' }"
						@click="activePanelTab = '典故'"
					>关联典故</button>
					<button 
						class="tab-btn" 
						:class="{ active: activePanelTab === '背景' }"
						@click="activePanelTab = '背景'"
					>事件科普</button>
				</div>
				
				<div class="panel-content">
					<!-- 关联人物标签 -->
					<div v-if="activePanelTab === '人物'" class="tab-content">
						<div v-if="getRelatedByType(currentEvent, '人物').length > 0" class="related-list">
							<div 
								v-for="item in getRelatedByType(currentEvent, '人物')" 
								:key="item.name"
								class="related-item"
							>
								<span class="related-name">{{ item.name }}</span>
								<button class="jump-btn" @click="jumpToModule('人物', item.name)">查看详情</button>
							</div>
						</div>
						<div v-else class="empty-state">暂无关联人物</div>
					</div>
					
					<!-- 关联典故标签 -->
					<div v-if="activePanelTab === '典故'" class="tab-content">
						<div v-if="getRelatedByType(currentEvent, '典故').length > 0" class="related-list">
							<div 
								v-for="item in getRelatedByType(currentEvent, '典故')" 
								:key="item.name"
								class="related-item"
							>
								<span class="related-name">{{ item.name }}</span>
								<button class="jump-btn" @click="jumpToModule('典故', item.name)">查看详情</button>
							</div>
						</div>
						<div v-else class="empty-state">暂无关联典故</div>
					</div>
					
					<!-- 事件科普标签 -->
					<div v-if="activePanelTab === '背景'" class="tab-content">
						<div class="dynasty-bg">
							<h4>{{ currentEvent.name }}</h4>
							<p class="dynasty-period">{{ formatYear(currentEvent.year) }}</p>
							<p class="event-type-badge" :class="currentEvent.type">{{ getEventTypeLabel(currentEvent.type) }}</p>
							<p class="dynasty-desc">{{ currentEvent.description }}</p>
							<button class="jump-btn" @click="jumpToEventPage(currentEvent)">查看事件科普</button>
						</div>
					</div>
				</div>
			</div>
		</transition>
		
		<!-- 多节点对比面板 -->
		<transition name="slide-left">
			<div v-if="showComparePanel && selectedEvents.length >= 2" class="compare-panel">
				<div class="panel-header">
					<h3>事件对比分析</h3>
					<button class="close-panel" @click="showComparePanel = false">×</button>
				</div>
				
				<div class="compare-content">
					<!-- 时间线对比 -->
					<div class="compare-section">
						<h4>时间线对比</h4>
						<div class="timeline-compare">
							<div 
								v-for="event in selectedEvents" 
								:key="event.id"
								class="timeline-item"
								:style="{ left: getTimelinePosition(event) + '%' }"
							>
								<div class="timeline-dot" :class="event.type"></div>
								<div class="timeline-label">
									<div class="event-name">{{ event.name }}</div>
									<div class="event-year">{{ formatYear(event.year) }}</div>
								</div>
							</div>
						</div>
					</div>
					
					<!-- 事件类型分布 -->
					<div class="compare-section">
						<h4>事件类型分布</h4>
						<div class="type-chart">
							<div 
								v-for="type in getTypeDistribution()" 
								:key="type.value"
								class="type-bar"
							>
								<div class="type-label">{{ type.label }}: {{ type.count }}</div>
								<div class="bar-container">
									<div 
										class="bar" 
										:class="type.value"
										:style="{ width: (type.count / selectedEvents.length * 100) + '%' }"
									></div>
								</div>
							</div>
						</div>
					</div>
					
					<!-- 关联人物交集/差异 -->
					<div class="compare-section">
						<h4>关联人物分析</h4>
						<div class="person-analysis">
							<div class="common-persons">
								<h5>共同关联人物</h5>
								<div v-if="getCommonPersons().length > 0" class="person-tags">
									<span 
										v-for="person in getCommonPersons()" 
										:key="person"
										class="person-tag"
									>{{ person }}</span>
								</div>
								<div v-else class="empty-state">无共同关联人物</div>
							</div>
							
							<div class="unique-persons">
								<h5>各自特有人物</h5>
								<div v-for="event in selectedEvents" :key="event.id" class="event-unique">
									<div class="event-name-small">{{ event.name }}</div>
									<div class="person-tags">
										<span 
											v-for="person in getUniquePersons(event)" 
											:key="person"
											class="person-tag unique"
										>{{ person }}</span>
									</div>
								</div>
							</div>
						</div>
					</div>
				</div>
			</div>
		</transition>
		
		<!-- 事件详情弹窗 -->
		<transition name="fade">
			<div v-if="selectedEvent && !showEventPanel" class="event-modal" @click.self="selectedEvent = null">
				<div class="modal-content">
					<button class="modal-close" @click="selectedEvent = null">×</button>
					<h3 class="modal-title">{{ selectedEvent.name }}</h3>
					<div class="modal-info">
						<p><span class="info-label">年份：</span>{{ formatYear(selectedEvent.year) }}</p>
						<p><span class="info-label">朝代：</span>{{ selectedEvent.dynasty }}</p>
						<p><span class="info-label">类型：</span>
							<span class="event-type-badge" :class="selectedEvent.type">
								{{ getEventTypeLabel(selectedEvent.type) }}
							</span>
						</p>
						<p class="event-description">{{ selectedEvent.description }}</p>
						
						<!-- 关联信息 -->
						<div v-if="selectedEvent.related" class="related-info">
							<h4>关联信息：</h4>
							<ul>
								<li v-for="(rel, index) in selectedEvent.related" :key="index">
									<strong>{{ rel.type }}：</strong>{{ rel.name }}
								</li>
							</ul>
						</div>
					</div>
				</div>
			</div>
		</transition>
	</div>
</template>

<script setup>
import { ElMessage } from 'element-plus';
import { onMounted, ref, watch, nextTick, computed } from 'vue'; 
import { useRouter } from 'vue-router';  
import topComponent from '@/components/topComponent.vue';
import request from '@/utils/request';

// 默认图片路径 
const defaultImage = 'https://cdn.pixabay.com/photo/2014/04/02/10/47/map-304774_640.png';

// 时间轴模式数据
const timelineModes = [
    { value: 'linear', label: '线性时间轴', icon: '📅' },
    { value: 'dynasty', label: '朝代分段', icon: '🏛️' },
    { value: 'focus', label: '聚焦模式', icon: '🔍' }
];

const router = useRouter();

// 当前选中的时间轴模式
const currentTimelineMode = ref('dynasty');
const showTimelineView = ref(false);
const selectedDynasty = ref('');

// Canvas引用
const linearCanvas = ref(null);
const focusCanvas = ref(null);

// 筛选相关状态
const showFilters = ref(false);
const searchKeyword = ref('');
const selectedDynastyFilter = ref('');
const selectedEventTypes = ref([]);
const timeRange = ref({
    min: -2070,
    max: 1912
});
const relatedKeyword = ref('');
const searchResults = ref([]);
const selectedEvent = ref(null);

const showEventPanel = ref(false);
const showComparePanel = ref(false);
const currentEvent = ref(null);
const activePanelTab = ref('人物');
const selectedEvents = ref([]);

// 加载状态
const loading = ref(false);

// 从后端获取的事件数据
const allEvents = ref([]);

// 事件类型配置
const eventTypes = [
    { value: 'political', label: '政治', color: '#FF4D4D' },
    { value: 'military', label: '军事', color: '#FFA500' },
    { value: 'cultural', label: '文化', color: '#4CAF50' }
];

// 年份范围
const minYear = -2070;
const maxYear = 1912;

// 朝代名称映射
const dynastyNameMap = {
    '夏': '夏朝',
    '商': '商朝',
    '周': '周朝',
    '秦': '秦朝',
    '汉': '汉朝',
    '三国': '三国',
    '三国·魏': '三国',
    '三国·蜀': '三国',
    '三国·吴': '三国',
    '晋': '晋朝',
    '南北朝': '南北朝',
    '隋': '隋朝',
    '唐': '唐朝',
    '宋': '宋朝',
    '元': '元朝',
    '明': '明朝',
    '清': '清朝'
};

// 定义时间轴数据
const timelineItems = ref([
    {   
        id:1,
        dataText: "《夏》",
        image: "",
        dynasty: "夏朝",
        startYear: -2070,
        endYear: -1600,
        description: "中国历史上第一个世袭制王朝，约公元前2070年—约公元前1600年"
    },
    {   
        id:2,
        dataText: "《商》",
        image: "",
        dynasty: "商朝",
        startYear: -1600,
        endYear: -1046,
        description: "中国第一个有直接文字记载的王朝，约公元前1600年—公元前1046年"
    },
    {
        id:3,
        dataText: "《周》",
        image: "",
        dynasty: "周朝",
        startYear: -1046,
        endYear: -256,
        description: "中国历史上最长的王朝，分西周和东周，公元前1046年—公元前256年"
    },
    {
        id:4,
        dataText: "《秦》",
        image: "",
        dynasty: "秦朝",
        startYear: -221,
        endYear: -207,
        description: "中国历史上第一个大一统王朝，公元前221年—公元前207年"
    },
    {
        id:5,
        dataText: "《汉》",
        image: "",
        dynasty: "汉朝",
        startYear: -202,
        endYear: 220,
        description: "中国历史上强盛的王朝之一，分西汉和东汉，公元前202年—公元220年"
    },
    {
        id:6,
        dataText: "《三国》",
        image: "",
        dynasty: "三国",
        startYear: 220,
        endYear: 280,
        description: "魏、蜀、吴三国鼎立时期，公元220年—公元280年"
    },
    {
        id:7,
        dataText: "《晋》",
        image: "",
        dynasty: "晋朝",
        startYear: 265,
        endYear: 420,
        description: "分为西晋和东晋，公元265年—公元420年"
    },
    {
        id:8,
        dataText: "《南北朝》",
        image: "",
        dynasty: "南北朝",
        startYear: 420,
        endYear: 589,
        description: "南朝与北朝并立的时期，公元420年—公元589年"
    },
    {
        id:9,
        dataText: "《隋》",
        image: "",
        dynasty: "隋朝",
        startYear: 581,
        endYear: 618,
        description: "开创科举制度和大运河，公元581年—公元618年"
    },
    {
        id:10,
        dataText: "《唐》",
        image: "",
        dynasty: "唐朝",
        startYear: 618,
        endYear: 907,
        description: "中国历史上最强盛的朝代之一，公元618年—公元907年"
    },
    {
        id:11,
        dataText: "《宋》",
        image: "",
        dynasty: "宋朝",
        startYear: 960,
        endYear: 1279,
        description: "经济文化繁荣的时期，分北宋和南宋，公元960年—公元1279年"
    },
    {
        id:12,
        dataText: "《元》",
        image: "",
        dynasty: "元朝",
        startYear: 1271,
        endYear: 1368,
        description: "中国历史上首个由少数民族建立的大一统王朝，公元1271年—公元1368年"
    },
    {
        id:13,
        dataText: "《明》",
        image: "",
        dynasty: "明朝",
        startYear: 1368,
        endYear: 1644,
        description: "中国历史上最后一个汉族建立的王朝，公元1368年—公元1644年"
    },
    {
        id:14,
        dataText: "《清》",
        image: "",
        dynasty: "清朝",
        startYear: 1636,
        endYear: 1912,
        description: "中国历史上最后一个封建王朝，公元1636年—公元1912年"
    }
]);

// 从后端获取的朝代数据（包含地图路径）
const dynastyData = ref([]);

// 加载朝代数据（地图路径）
async function loadDynastyData() {
    try {
        const res = await request.get('/dynasty/list/all');
        if (res.data && res.data.code === 200 && res.data.data) {
            // 去重：根据 id 去重
            const uniqueMap = new Map();
            res.data.data.forEach(dynasty => {
                if (!uniqueMap.has(dynasty.id)) {
                    uniqueMap.set(dynasty.id, dynasty);
                }
            });
            dynastyData.value = Array.from(uniqueMap.values());
            console.log('朝代数据加载成功（去重后）:', dynastyData.value.length);
            
            // 更新时间轴图片为朝代地图
            updateTimelineImages();
        }
    } catch (error) {
        console.error('加载朝代数据失败:', error);
        setDefaultImages();
    }
}

// 更新时间轴图片为朝代地图
function updateTimelineImages() {
    let updateCount = 0;
    dynastyData.value.forEach(dynasty => {
        // 处理三国特殊情况：统一映射到"三国"
        let frontendName = dynasty.name;
        if (frontendName.includes('三国')) {
            frontendName = '三国';
        }
        frontendName = dynastyNameMap[frontendName] || frontendName;
        
        const timelineItem = timelineItems.value.find(item => item.dynasty === frontendName);
        
        if (timelineItem) {
            if (dynasty.mapPath && dynasty.mapPath !== '') {
                // 使用后端返回的地图路径
                timelineItem.image = `http://localhost:8080/api${dynasty.mapPath}`;
                console.log(`✅ 更新 ${frontendName} 地图:`, timelineItem.image);
                updateCount++;
            } else {
                // 没有地图路径，使用默认图片
                timelineItem.image = defaultImage;
                console.log(`⚠️ ${frontendName} 无地图，使用默认图片`);
            }
        } else {
            console.warn(`未找到匹配的朝代: ${dynasty.name} -> ${frontendName}`);
        }
    });
    
    // 检查是否有未更新的朝代，使用默认图片
    timelineItems.value.forEach(item => {
        if (!item.image || item.image === '') {
            item.image = defaultImage;
            console.log(`❌ ${item.dynasty} 未更新，使用默认图片`);
        }
    });
    
    console.log(`📊 统计: 已更新 ${updateCount} 个朝代的地图，其余使用默认图片`);
}

// 设置默认图片
function setDefaultImages() {
    timelineItems.value.forEach(item => {
        item.image = defaultImage;
    });
    console.log('使用默认图片');
}

// 朝代名称到ID的映射
const dynastyNameToId = {
    '夏朝': 1, '商朝': 2, '周朝': 3, '秦朝': 4,
    '汉朝': 5, '三国': 6, '晋朝': 7, '南北朝': 8,
    '隋朝': 9, '唐朝': 10, '宋朝': 11, '元朝': 12,
    '明朝': 13, '清朝': 14
};

// 朝代ID到名称的映射
const dynastyIdToName = {
    1: '夏朝', 2: '商朝', 3: '周朝', 4: '秦朝',
    5: '汉朝', 6: '三国', 7: '晋朝', 8: '南北朝',
    9: '隋朝', 10: '唐朝', 11: '宋朝', 12: '元朝',
    13: '明朝', 14: '清朝'
};

// 加载所有事件数据
async function loadAllEvents() {
    try {
        loading.value = true;
        const dynastyIds = [1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14];
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
        
        results.forEach((res) => {
            if (res && res.data && res.data.code === 200) {
                const events = res.data.data?.records || [];
                allEventsData = [...allEventsData, ...events];
            }
        });
        
        // 转换为前端需要的事件格式
        allEvents.value = allEventsData.map(event => ({
            id: event.id,
            name: event.eventName,
            year: event.eventYear,
            dynasty: dynastyIdToName[event.dynastyId] || '未知朝代',
            dynastyId: event.dynastyId,
            type: event.eventType || 'political',
            description: event.eventBg || '暂无描述',
            eventProcess: event.eventProcess,
            eventImpact: event.eventImpact,
            related: []
        }));
        
        console.log('事件数据加载成功，共', allEvents.value.length, '条');
        
    } catch (error) {
        console.error('加载事件数据失败:', error);
        ElMessage.error('加载事件数据失败');
    } finally {
        loading.value = false;
    }
}

// 获取朝代名称
function getDynastyName(dynastyId) {
    return dynastyIdToName[dynastyId] || '未知朝代';
}

// 计算当前模式标题
const currentModeTitle = computed(() => {
    const mode = timelineModes.find(m => m.value === currentTimelineMode.value);
    return mode ? mode.label : '时间轴视图';
});

// 计算是否有激活的筛选条件
const hasActiveFilters = computed(() => {
    return selectedDynastyFilter.value !== '' || 
           selectedEventTypes.value.length > 0 || 
           timeRange.value.min > minYear || 
           timeRange.value.max < maxYear ||
           relatedKeyword.value !== '';
});

// 计算筛选后的事件
const filteredEvents = computed(() => {
    let events = [...allEvents.value];
    
    if (selectedDynastyFilter.value) {
        events = events.filter(event => event.dynasty === selectedDynastyFilter.value);
    }
    
    if (selectedEventTypes.value.length > 0) {
        events = events.filter(event => selectedEventTypes.value.includes(event.type));
    }
    
    events = events.filter(event => {
        const year = event.year;
        return year >= timeRange.value.min && year <= timeRange.value.max;
    });
    
    if (searchKeyword.value) {
        const keyword = searchKeyword.value.toLowerCase();
        events = events.filter(event => 
            event.name.toLowerCase().includes(keyword) || 
            event.description.toLowerCase().includes(keyword)
        );
    }
    
    return events;
});

// 计算筛选后的朝代
const filteredTimelineItems = computed(() => {
    if (!hasActiveFilters.value) return timelineItems.value;
    
    return timelineItems.value.filter(dynasty => {
        const eventsInDynasty = filteredEvents.value.filter(e => e.dynasty === dynasty.dynasty);
        return eventsInDynasty.length > 0;
    });
});

// 获取事件类型标签
function getEventTypeLabel(type) {
    const typeMap = {
        political: '政治',
        military: '军事',
        cultural: '文化'
    };
    return typeMap[type] || type;
}

// 获取事件类型图标
function getEventTypeIcon(type) {
    const iconMap = {
        political: '🏛️',
        military: '⚔️',
        cultural: '📚'
    };
    return iconMap[type] || '📌';
}

// 检查事件是否有关联信息
function isEventRelated(event, keyword) {
    if (!event.related || !keyword) return false;
    const lowerKeyword = keyword.toLowerCase();
    return event.related.some(rel => 
        rel.name.toLowerCase().includes(lowerKeyword)
    );
}

// 获取关联信息
function getRelatedInfo(event, keyword) {
    if (!event.related || !keyword) return '';
    const lowerKeyword = keyword.toLowerCase();
    const matches = event.related.filter(rel => 
        rel.name.toLowerCase().includes(lowerKeyword)
    );
    if (matches.length === 0) return '';
    return matches.map(m => `${m.type}: ${m.name}`).join('、');
}

// 检查朝代中是否有高亮事件
function hasHighlightInDynasty(dynastyName) {
    if (!searchKeyword.value) return false;
    const events = allEvents.value.filter(e => e.dynasty === dynastyName);
    return events.some(e => e.name.toLowerCase().includes(searchKeyword.value.toLowerCase()));
}

// 获取筛选后指定朝代的事件
function getFilteredEventsByDynasty(dynastyName) {
    return filteredEvents.value.filter(e => e.dynasty === dynastyName);
}

// 处理搜索
function handleSearch() {
    if (!searchKeyword.value.trim()) {
        searchResults.value = [];
        nextTick(() => {
            reinitTimeline();
        });
        return;
    }
    
    const keyword = searchKeyword.value.toLowerCase();
    const results = allEvents.value.filter(event => 
        event.name.toLowerCase().includes(keyword) || 
        event.description.toLowerCase().includes(keyword)
    );
    
    searchResults.value = results.slice(0, 10);
    
    nextTick(() => {
        reinitTimeline();
    });
}

// 清除搜索
function clearSearch() {
    searchKeyword.value = '';
    searchResults.value = [];
    nextTick(() => {
        reinitTimeline();
    });
}

// 跳转到事件
function jumpToEvent(event) {
    const dynastyIndex = timelineItems.value.findIndex(d => d.dynasty === event.dynasty);
    if (dynastyIndex !== -1) {
        scrollToDynasty(dynastyIndex);
        setTimeout(() => {
            showEventPanel = true;
            currentEvent.value = event;
        }, 500);
    }
}

// 应用筛选
function applyFilters() {
    showFilters.value = false;
    ElMessage.success('筛选已应用');
    nextTick(() => {
        reinitTimeline();
    });
}

// 重置筛选
function resetFilters() {
    selectedDynastyFilter.value = '';
    selectedEventTypes.value = [];
    timeRange.value = { min: minYear, max: maxYear };
    relatedKeyword.value = '';
    searchKeyword.value = '';
    searchResults.value = [];
    ElMessage.info('筛选已重置');
    nextTick(() => {
        reinitTimeline();
    });
}

// 重置时间范围
function resetTimeRange() {
    timeRange.value = { min: minYear, max: maxYear };
}

// 获取朝代颜色
function getDynastyColor(index) {
    const colors = [
        '#FF6B6B', '#4ECDC4', '#45B7D1', '#96CEB4', '#FFE194',
        '#B19CD9', '#FF9F7C', '#8D9F87', '#F5A97F', '#A3C4F3',
        '#C9B6E4', '#FAC05E', '#F79F79', '#9AD0C2'
    ];
    return colors[index % colors.length];
}

// 获取朝代时期
function getDynastyPeriod(dynasty) {
    const startYear = dynasty.startYear < 0 ? `公元前${Math.abs(dynasty.startYear)}` : `公元${dynasty.startYear}`;
    const endYear = dynasty.endYear < 0 ? `公元前${Math.abs(dynasty.endYear)}` : `公元${dynasty.endYear}`;
    return `${startYear} - ${endYear}`;
}

// 格式化年份
function formatYear(year) {
    if (year < 0) {
        return `公元前${Math.abs(year)}年`;
    }
    return `公元${year}年`;
}

// 切换到指定时间轴模式
function switchTimelineMode(mode) {
    currentTimelineMode.value = mode;
    showTimelineView.value = true;
    selectedDynasty.value = '';
    
    nextTick(() => {
        if (mode === 'linear') {
            initLinearTimeline();
        } else if (mode === 'focus') {
            initFocusTimeline();
        }
    });
}

// 初始化线性时间轴Canvas
function initLinearTimeline() {
    if (!linearCanvas.value) return;
    
    const canvas = linearCanvas.value;
    const ctx = canvas.getContext('2d');
    const container = canvas.parentElement;
    
    const width = container.clientWidth;
    const height = 300;
    canvas.width = width;
    canvas.height = height;
    
    ctx.clearRect(0, 0, width, height);
    
    const padding = 50;
    const axisY = height - 50;
    
    ctx.beginPath();
    ctx.strokeStyle = '#888';
    ctx.lineWidth = 2;
    ctx.moveTo(padding, axisY);
    ctx.lineTo(width - padding, axisY);
    ctx.stroke();
    
    const minYearVal = -2070;
    const maxYearVal = 1912;
    const yearRange = maxYearVal - minYearVal;
    
    filteredEvents.value.forEach(event => {
        const x = padding + ((event.year - minYearVal) / yearRange) * (width - padding * 2);
        
        let color;
        switch(event.type) {
            case 'political': color = '#FF4D4D'; break;
            case 'military': color = '#FFA500'; break;
            case 'cultural': color = '#4CAF50'; break;
            default: color = '#888';
        }
        
        ctx.beginPath();
        ctx.fillStyle = color;
        ctx.arc(x, axisY, 5, 0, Math.PI * 2);
        ctx.fill();
        
        ctx.strokeStyle = '#fff';
        ctx.lineWidth = 2;
        ctx.beginPath();
        ctx.arc(x, axisY, 5, 0, Math.PI * 2);
        ctx.stroke();
    });
}

// 初始化聚焦时间轴Canvas
function initFocusTimeline() {
    if (!focusCanvas.value || selectedDynasty.value === '') return;
    
    const canvas = focusCanvas.value;
    const ctx = canvas.getContext('2d');
    const container = canvas.parentElement;
    
    const width = container.clientWidth;
    const height = 200;
    canvas.width = width;
    canvas.height = height;
    
    ctx.clearRect(0, 0, width, height);
    
    const dynasty = timelineItems.value[selectedDynasty.value];
    const padding = 50;
    const axisY = height - 50;
    
    ctx.beginPath();
    ctx.strokeStyle = '#FFD700';
    ctx.lineWidth = 3;
    ctx.moveTo(padding, axisY);
    ctx.lineTo(width - padding, axisY);
    ctx.stroke();
    
    const focusRange = dynasty.endYear - dynasty.startYear;
    const yearRange = focusRange * 1.2;
    const startFocus = dynasty.startYear - focusRange * 0.1;
    const endFocus = dynasty.endYear + focusRange * 0.1;
    
    ctx.fillStyle = 'rgba(255, 215, 0, 0.1)';
    ctx.fillRect(padding, axisY - 30, width - padding * 2, 40);
    
    const startX = padding + ((dynasty.startYear - startFocus) / (endFocus - startFocus)) * (width - padding * 2);
    const endX = padding + ((dynasty.endYear - startFocus) / (endFocus - startFocus)) * (width - padding * 2);
    
    ctx.fillStyle = getDynastyColor(selectedDynasty.value);
    ctx.globalAlpha = 0.3;
    ctx.fillRect(startX, axisY - 25, endX - startX, 25);
    ctx.globalAlpha = 1;
    
    ctx.font = 'bold 16px "Microsoft YaHei"';
    ctx.fillStyle = '#FFD700';
    ctx.textAlign = 'center';
    ctx.fillText(dynasty.dynasty, (startX + endX) / 2, axisY - 35);
    
    const dynastyEvents = getFilteredEventsByDynasty(dynasty.dynasty);
    dynastyEvents.forEach(event => {
        const x = padding + ((event.year - startFocus) / (endFocus - startFocus)) * (width - padding * 2);
        
        let color;
        switch(event.type) {
            case 'political': color = '#FF4D4D'; break;
            case 'military': color = '#FFA500'; break;
            case 'cultural': color = '#4CAF50'; break;
            default: color = '#888';
        }
        
        ctx.beginPath();
        ctx.fillStyle = color;
        ctx.arc(x, axisY, 6, 0, Math.PI * 2);
        ctx.fill();
        
        ctx.font = '10px "Microsoft YaHei"';
        ctx.fillStyle = '#fff';
        ctx.textAlign = 'center';
        ctx.fillText(event.name, x, axisY - 15);
    });
}

// 滚动到指定朝代
function scrollToDynasty(index) {
    showTimelineView.value = false;
    const items = document.querySelectorAll('.item');
    if (items && items[index]) {
        items[index].scrollIntoView({ behavior: 'smooth', block: 'center' });
    }
}

// 监听朝代选择变化
watch(selectedDynasty, (newVal) => {
    if (newVal !== '' && currentTimelineMode.value === 'focus') {
        nextTick(() => {
            initFocusTimeline();
        });
    }
});

// 监听筛选条件变化，自动更新
watch([selectedDynastyFilter, selectedEventTypes, timeRange, relatedKeyword], () => {
    applyFilters();
}, { deep: true });

// 检查事件是否被选中
function isEventSelected(event) {
    return selectedEvents.value.some(e => e.id === event.id);
}

// 切换事件选择
function toggleEventSelection(event) {
    const index = selectedEvents.value.findIndex(e => e.id === event.id);
    if (index === -1) {
        if (selectedEvents.value.length < 5) {
            selectedEvents.value.push(event);
        } else {
            ElMessage.warning('最多只能对比5个事件');
        }
    } else {
        selectedEvents.value.splice(index, 1);
    }
}

// 清空选中事件
function clearSelectedEvents() {
    selectedEvents.value = [];
    showComparePanel.value = false;
}

// 根据类型获取关联项
function getRelatedByType(event, type) {
    if (!event.related) return [];
    return event.related.filter(item => item.type === type);
}

// 获取事件所属朝代
function getEventDynasty(event) {
    return timelineItems.value.find(d => d.dynasty === event.dynasty) || timelineItems.value[0];
}

// 跳转到其他模块
function jumpToModule(module, keyword) {
    switch(module) {
        case '人物':
            router.push({ 
                path: '/person', 
                query: { keyword: keyword } 
            });
            break;
        case '典故':
            router.push({ 
                path: '/allusion', 
                query: { keyword: keyword } 
            });
            break;
        case '朝代':
            router.push({ 
                path: '/event', 
                query: { dynasty: keyword } 
            });
            break;
        default:
            router.push({ 
                path: '/timeline', 
                query: { keyword: keyword } 
            });
    }
    
    showEventPanel.value = false;
    ElMessage.success(`正在跳转至${module}模块，查看相关内容：${keyword}`);
}

// 跳转到事件科普页面
function jumpToEventPage(event) {
    router.push({ 
        path: '/event', 
        query: { 
            id: event.id,
            name: event.name 
        } 
    });
    
    showEventPanel.value = false;
    ElMessage.success(`正在跳转至事件科普页面，查看：${event.name}`);
}

// 获取时间轴位置百分比
function getTimelinePosition(event) {
    const totalRange = maxYear - minYear;
    const position = ((event.year - minYear) / totalRange) * 100;
    return Math.max(0, Math.min(100, position));
}

// 获取事件类型分布
function getTypeDistribution() {
    const counts = {
        political: 0,
        military: 0,
        cultural: 0
    };
    
    selectedEvents.value.forEach(event => {
        counts[event.type]++;
    });
    
    return eventTypes.map(type => ({
        ...type,
        count: counts[type.value]
    }));
}

// 获取共同关联人物
function getCommonPersons() {
    if (selectedEvents.value.length === 0) return [];
    
    const personSets = selectedEvents.value.map(event => {
        const persons = getRelatedByType(event, '人物').map(p => p.name);
        return new Set(persons);
    });
    
    const commonPersons = [];
    const firstSet = personSets[0];
    
    firstSet.forEach(person => {
        if (personSets.every(set => set.has(person))) {
            commonPersons.push(person);
        }
    });
    
    return commonPersons;
}

// 获取事件特有关联人物
function getUniquePersons(event) {
    const eventPersons = new Set(getRelatedByType(event, '人物').map(p => p.name));
    const otherPersons = new Set();
    
    selectedEvents.value.forEach(e => {
        if (e.id !== event.id) {
            getRelatedByType(e, '人物').forEach(p => otherPersons.add(p.name));
        }
    });
    
    return Array.from(eventPersons).filter(p => !otherPersons.has(p));
}

const topNavRef = ref(null); 
const activeDynastyIndex = ref(-1);
const isNavBouncing = ref(false);

// 重新初始化时间轴的函数
function reinitTimeline() {
    if (window.jQuery) {
        const $ = window.jQuery;
        $(window).off('scroll.timeline');
        initTimeline($);
    }
}

// jQuery时间轴初始化函数
function initTimeline($) {
    $.fn.timeline = function () {
        var selectors = {
            id: $(this),
            item: $(this).find(".item"),
            activeClass: "item--active",
            img: ".img"
        };

        selectors.item.eq(0).addClass(selectors.activeClass);
        selectors.id.css(
            "background-image",
            "url(" +
            selectors.item.first()
                .find(selectors.img)
                .attr("src") +
            ")"
        );
        activeDynastyIndex.value = 0;
        triggerNavBounce();

        var itemLength = selectors.item.length;

        $(window).on('scroll.timeline', function () {
            var scrollTop = $(this).scrollTop();
            var windowHeight = $(this).height();
            var documentHeight = $(document).height();
            
            if (scrollTop + windowHeight >= documentHeight - 5) {
                var lastIndex = itemLength - 1;
                selectors.item.removeClass(selectors.activeClass);
                selectors.id.css(
                    "background-image",
                    "url(" + selectors.item.eq(lastIndex).find(selectors.img).attr("src") + ")"
                );
                selectors.item.eq(lastIndex).addClass(selectors.activeClass);
                
                if (activeDynastyIndex.value !== lastIndex) {
                    activeDynastyIndex.value = lastIndex;
                    triggerNavBounce();
                }
                return;
            }
            
            var maxVisibleRatio = 0;
            var bestMatchIndex = -1;
            
            selectors.item.each(function (i) {
                var $this = $(this);
                var itemTop = $this.offset().top;
                var itemBottom = itemTop + $this.outerHeight();
                var itemHeight = $this.outerHeight();
                
                var visibleTop = Math.max(itemTop, scrollTop);
                var visibleBottom = Math.min(itemBottom, scrollTop + windowHeight);
                var visibleHeight = Math.max(0, visibleBottom - visibleTop);
                
                var visibleRatio = visibleHeight / itemHeight;
                
                if (visibleRatio > maxVisibleRatio) {
                    maxVisibleRatio = visibleRatio;
                    bestMatchIndex = i;
                }
            });
            
            if (bestMatchIndex !== -1 && bestMatchIndex !== activeDynastyIndex.value) {
                selectors.item.removeClass(selectors.activeClass);
                selectors.id.css(
                    "background-image",
                    "url(" + selectors.item.eq(bestMatchIndex).find(selectors.img).attr("src") + ")"
                );
                selectors.item.eq(bestMatchIndex).addClass(selectors.activeClass);
                activeDynastyIndex.value = bestMatchIndex;
                triggerNavBounce();
            }
            
            if (bestMatchIndex === -1) {
                var viewportCenter = scrollTop + windowHeight / 2;
                var closestIndex = 0;
                var minDistance = Infinity;
                
                selectors.item.each(function (i) {
                    var $this = $(this);
                    var itemCenter = $this.offset().top + $this.outerHeight() / 2;
                    var distance = Math.abs(itemCenter - viewportCenter);
                    
                    if (distance < minDistance) {
                        minDistance = distance;
                        closestIndex = i;
                    }
                });
                
                if (closestIndex !== activeDynastyIndex.value) {
                    selectors.item.removeClass(selectors.activeClass);
                    selectors.id.css(
                        "background-image",
                        "url(" + selectors.item.eq(closestIndex).find(selectors.img).attr("src") + ")"
                    );
                    selectors.item.eq(closestIndex).addClass(selectors.activeClass);
                    activeDynastyIndex.value = closestIndex;
                    triggerNavBounce();
                }
            }
        });

        setTimeout(function() {
            $(window).trigger('scroll');
        }, 100);
    };

    $("#shell").timeline();
}

function triggerNavBounce() {
    isNavBouncing.value = true;
    setTimeout(() => {
        isNavBouncing.value = false;
    }, 600);
}

// 监听筛选后的朝代变化，重新初始化时间轴
watch(filteredTimelineItems, () => {
    nextTick(() => {
        reinitTimeline();
    });
}, { deep: true });

// 页面加载时获取事件数据和朝代地图数据
onMounted(async () => {
    // 先加载朝代地图数据
    await loadDynastyData();
    // 再加载事件数据
    await loadAllEvents();
    
    if (window.jQuery) {
        if (document.readyState === 'complete') {
            initTimeline(window.jQuery);
        } else {
            window.addEventListener('load', function() {
                initTimeline(window.jQuery);
            });
        }
        return;
    }
    
    const script = document.createElement('script');
    script.src = 'https://cdnjs.cloudflare.com/ajax/libs/jquery/3.1.0/jquery.min.js';
    script.onload = () => {
        if (window.jQuery) {
            if (document.readyState === 'complete') {
                initTimeline(window.jQuery);
            } else {
                window.addEventListener('load', function() {
                    initTimeline(window.jQuery);
                });
            }
        }
    };
    script.onerror = (error) => {
        console.error('Failed to load jQuery:', error);
    };
    document.head.appendChild(script);
});
</script>

<style scoped>
.history-app {
	min-height: 100vh; 
	width: 100%;
	display: flex;
	flex-direction: column;
	font-family: "Microsoft YaHei", "SimHei", "PingFang SC", sans-serif;
	color: #333;
	background-color: #1a1a1a; 
}

/* 主内容区域 */
.app-main {
	padding: 0;
	margin: 0;
	flex: 1;
	transition: margin-right 0.3s ease;
}

.app-main.panel-open {
	margin-right: 350px;
}

.shell {
	width: 100%;
	position: relative;
	padding: 100px 0; 
	transition: all 0.5s ease;
	background-attachment: fixed;
	background-size: cover;
	background-position: center; 
	min-height: 100vh; 
}

.shell:before {
	position: absolute;
	left: 0;
	top: 0;
	width: 100%;
	height: 100%;
	background: rgba(18, 18, 18, 0.85);
	backdrop-filter: blur(2px); 
	content: "";
}

.header {
	width: 100%;
	text-align: center;
	margin-bottom: 50px;
	position: relative;
	padding: 0 20px;
}

.title {
	color: #fff;
	font-size: 52px;
	font-weight: 300;
	margin: 0;
	letter-spacing: 8px; 
	text-shadow: 0 2px 10px rgba(0, 0, 0, 0.3);
}

.subtitle {
	color: #e0e0e0;
	font-size: 18px;
	letter-spacing: 10px; 
	margin: 20px 0 0 0;
	font-weight: normal;
	opacity: 0.8;
}

/* 筛选区域样式 */
.filter-section {
	margin: 20px auto 40px;
	max-width: 800px;
	position: relative;
	z-index: 30;
}

.search-box {
	position: relative;
	margin-bottom: 15px;
}

.search-input {
	width: 100%;
	padding: 15px 20px 15px 50px;
	background: rgba(255, 255, 255, 0.1);
	border: 2px solid rgba(255, 215, 0, 0.3);
	border-radius: 40px;
	color: #fff;
	font-size: 16px;
	transition: all 0.3s ease;
}

.search-input:focus {
	outline: none;
	border-color: #FFD700;
	background: rgba(255, 255, 255, 0.15);
}

.search-icon {
	position: absolute;
	left: 20px;
	top: 50%;
	transform: translateY(-50%);
	font-size: 20px;
	color: #FFD700;
}

.filter-toggle {
	display: flex;
	align-items: center;
	justify-content: space-between;
	padding: 12px 20px;
	background: rgba(255, 255, 255, 0.05);
	border: 1px solid rgba(255, 215, 0, 0.2);
	border-radius: 8px;
	color: #fff;
	cursor: pointer;
	transition: all 0.3s ease;
	margin-bottom: 10px;
}

.filter-toggle:hover {
	background: rgba(255, 215, 0, 0.1);
}

.toggle-icon {
	font-size: 14px;
	transition: transform 0.3s ease;
}

.filter-panel {
	background: rgba(30, 30, 30, 0.95);
	backdrop-filter: blur(10px);
	border: 1px solid rgba(255, 215, 0, 0.3);
	border-radius: 12px;
	padding: 20px;
	margin-bottom: 20px;
}

.filter-row {
	display: flex;
	gap: 20px;
	margin-bottom: 20px;
	flex-wrap: wrap;
}

.filter-item {
	flex: 1;
	min-width: 250px;
}

.filter-item label {
	display: block;
	color: #FFD700;
	margin-bottom: 8px;
	font-size: 14px;
}

.filter-select {
	width: 100%;
	padding: 10px;
	background: rgba(255, 255, 255, 0.1);
	border: 1px solid rgba(255, 255, 255, 0.2);
	border-radius: 6px;
	color: #fff;
	font-size: 14px;
}

.filter-select option {
	background: #333;
}

.checkbox-group {
	display: flex;
	flex-wrap: wrap;
	gap: 15px;
}

.checkbox-label {
	display: flex;
	align-items: center;
	gap: 8px;
	color: #fff;
	cursor: pointer;
	font-size: 14px;
}

.checkbox-label input[type="checkbox"] {
	display: none;
}

.checkbox-custom {
	width: 16px;
	height: 16px;
	border-radius: 4px;
	background: rgba(255, 255, 255, 0.2);
	transition: all 0.3s ease;
}

.checkbox-label input[type="checkbox"]:checked + .checkbox-custom {
	background: #FFD700;
	box-shadow: 0 0 10px rgba(255, 215, 0, 0.5);
}

.time-range {
	width: 100%;
}

.range-inputs {
	display: flex;
	align-items: center;
	gap: 10px;
	margin-bottom: 10px;
}

.range-input {
	flex: 1;
	padding: 8px;
	background: rgba(255, 255, 255, 0.1);
	border: 1px solid rgba(255, 255, 255, 0.2);
	border-radius: 4px;
	color: #fff;
	font-size: 14px;
}

.range-sep {
	color: #FFD700;
	font-size: 18px;
}

.range-slider {
	position: relative;
	height: 30px;
}

.slider {
	position: absolute;
	width: 100%;
	height: 5px;
	background: rgba(255, 255, 255, 0.2);
	border-radius: 5px;
	outline: none;
	-webkit-appearance: none;
}

.slider::-webkit-slider-thumb {
	-webkit-appearance: none;
	width: 20px;
	height: 20px;
	background: #FFD700;
	border-radius: 50%;
	cursor: pointer;
	box-shadow: 0 0 10px rgba(255, 215, 0, 0.5);
}

.slider-min {
	z-index: 2;
}

.slider-max {
	z-index: 1;
	background: transparent;
}

.related-input {
	width: 100%;
	padding: 10px;
	background: rgba(255, 255, 255, 0.1);
	border: 1px solid rgba(255, 255, 255, 0.2);
	border-radius: 6px;
	color: #fff;
	font-size: 14px;
}

.filter-actions {
	display: flex;
	justify-content: flex-end;
	gap: 15px;
	margin-top: 20px;
}

.filter-btn {
	padding: 10px 25px;
	border: none;
	border-radius: 6px;
	font-size: 14px;
	cursor: pointer;
	transition: all 0.3s ease;
}

.apply-btn {
	background: #FFD700;
	color: #1a1a1a;
}

.apply-btn:hover {
	background: #ffed4a;
	transform: translateY(-2px);
}

.reset-btn {
	background: rgba(255, 255, 255, 0.1);
	color: #fff;
}

.reset-btn:hover {
	background: rgba(255, 255, 255, 0.2);
}

/* 搜索结果样式 */
.search-results {
	background: rgba(30, 30, 30, 0.95);
	backdrop-filter: blur(10px);
	border: 1px solid rgba(255, 215, 0, 0.3);
	border-radius: 12px;
	margin-top: 10px;
	overflow: hidden;
}

.results-header {
	display: flex;
	justify-content: space-between;
	align-items: center;
	padding: 12px 15px;
	background: rgba(255, 215, 0, 0.1);
	border-bottom: 1px solid rgba(255, 215, 0, 0.3);
}

.results-header h4 {
	margin: 0;
	color: #FFD700;
	font-size: 14px;
}

.close-results {
	width: 24px;
	height: 24px;
	border: none;
	background: rgba(255, 255, 255, 0.1);
	color: #fff;
	border-radius: 50%;
	cursor: pointer;
	display: flex;
	align-items: center;
	justify-content: center;
	transition: all 0.3s ease;
}

.close-results:hover {
	background: #FFD700;
	color: #1a1a1a;
}

.results-list {
	list-style: none;
	margin: 0;
	padding: 0;
	max-height: 300px;
	overflow-y: auto;
}

.result-item {
	padding: 12px 15px;
	border-bottom: 1px solid rgba(255, 255, 255, 0.1);
	cursor: pointer;
	display: flex;
	align-items: center;
	gap: 10px;
	transition: all 0.3s ease;
}

.result-item:hover {
	background: rgba(255, 215, 0, 0.1);
}

.result-type {
	padding: 2px 8px;
	border-radius: 12px;
	font-size: 12px;
	font-weight: bold;
}

.result-type.political {
	background: rgba(255, 77, 77, 0.3);
	color: #FF4D4D;
}

.result-type.military {
	background: rgba(255, 165, 0, 0.3);
	color: #FFA500;
}

.result-type.cultural {
	background: rgba(76, 175, 80, 0.3);
	color: #4CAF50;
}

.result-name {
	flex: 1;
	color: #fff;
	font-size: 14px;
}

.result-year {
	color: #FFD700;
	font-size: 12px;
}

.result-dynasty {
	color: #aaa;
	font-size: 12px;
}

/* 激活的筛选标签样式 */
.active-filters {
	display: flex;
	flex-wrap: wrap;
	gap: 10px;
	margin-top: 15px;
}

.filter-tag {
	display: inline-flex;
	align-items: center;
	gap: 8px;
	padding: 5px 12px;
	background: rgba(255, 215, 0, 0.2);
	border: 1px solid rgba(255, 215, 0, 0.3);
	border-radius: 20px;
	color: #FFD700;
	font-size: 13px;
}

.remove-tag {
	width: 18px;
	height: 18px;
	background: rgba(255, 255, 255, 0.1);
	border-radius: 50%;
	display: inline-flex;
	align-items: center;
	justify-content: center;
	cursor: pointer;
	font-size: 14px;
	transition: all 0.3s ease;
}

.remove-tag:hover {
	background: #FFD700;
	color: #1a1a1a;
}

/* 对比工具栏样式 */
.compare-toolbar {
	position: sticky;
	top: 20px;
	z-index: 40;
	max-width: 800px;
	margin: 0 auto 20px;
	padding: 15px 20px;
	background: rgba(255, 215, 0, 0.2);
	backdrop-filter: blur(10px);
	border: 1px solid #FFD700;
	border-radius: 40px;
	display: flex;
	justify-content: center;
}

.compare-info {
	color: #FFD700;
	font-size: 14px;
	display: flex;
	align-items: center;
	gap: 15px;
}

.compare-btn, .clear-btn {
	padding: 5px 15px;
	border: none;
	border-radius: 20px;
	font-size: 13px;
	cursor: pointer;
	transition: all 0.3s ease;
}

.compare-btn {
	background: #FFD700;
	color: #1a1a1a;
}

.compare-btn:hover {
	background: #ffed4a;
	transform: translateY(-2px);
}

.clear-btn {
	background: rgba(255, 255, 255, 0.1);
	color: #fff;
}

.clear-btn:hover {
	background: rgba(255, 255, 255, 0.2);
}

/* 朝代内事件列表样式 */
.dynasty-events {
	margin-top: 20px;
	display: flex;
	flex-direction: column;
	gap: 8px;
}

.event-item {
	display: flex;
	align-items: center;
	gap: 10px;
	padding: 8px 12px;
	background: rgba(0, 0, 0, 0.3);
	border-radius: 6px;
	cursor: pointer;
	transition: all 0.3s ease;
	border-left: 3px solid transparent;
}

.event-item.political {
	border-left-color: #FF4D4D;
}

.event-item.military {
	border-left-color: #FFA500;
}

.event-item.cultural {
	border-left-color: #4CAF50;
}

.event-item:hover {
	background: rgba(255, 215, 0, 0.1);
	transform: translateX(5px);
}

.event-item.highlight {
	background: rgba(255, 215, 0, 0.2);
	animation: pulse 1s ease;
}

.event-item.related {
	background: rgba(76, 175, 80, 0.2);
	border: 1px solid #4CAF50;
}

.event-item.selected {
	background: rgba(255, 215, 0, 0.3);
	border: 1px solid #FFD700;
}

@keyframes pulse {
	0% { background-color: rgba(255, 215, 0, 0.2); }
	50% { background-color: rgba(255, 215, 0, 0.5); }
	100% { background-color: rgba(255, 215, 0, 0.2); }
}

.event-checkbox {
	width: 16px;
	height: 16px;
	cursor: pointer;
	accent-color: #FFD700;
}

.event-type-icon {
	font-size: 16px;
}

.event-name {
	flex: 1;
	color: #fff;
	font-size: 14px;
}

.event-year {
	color: #FFD700;
	font-size: 12px;
}

.related-badge {
	padding: 2px 8px;
	background: #4CAF50;
	border-radius: 12px;
	color: #fff;
	font-size: 10px;
}

.view-detail-btn {
	padding: 2px 8px;
	background: rgba(255, 215, 0, 0.3);
	border: 1px solid #FFD700;
	border-radius: 12px;
	color: #FFD700;
	font-size: 10px;
	cursor: pointer;
	transition: all 0.3s ease;
}

.view-detail-btn:hover {
	background: #FFD700;
	color: #1a1a1a;
}

/* 右侧联动面板样式 */
.event-panel, .compare-panel {
	position: fixed;
	top: 0;
	right: 0;
	width: 350px;
	height: 100vh;
	background: rgba(30, 30, 30, 0.95);
	backdrop-filter: blur(10px);
	border-left: 2px solid #FFD700;
	box-shadow: -5px 0 20px rgba(0, 0, 0, 0.5);
	z-index: 1000;
	display: flex;
	flex-direction: column;
	overflow: hidden;
}

.panel-header {
	display: flex;
	justify-content: space-between;
	align-items: center;
	padding: 20px;
	border-bottom: 1px solid rgba(255, 215, 0, 0.3);
}

.panel-header h3 {
	margin: 0;
	color: #FFD700;
	font-size: 18px;
}

.close-panel {
	width: 30px;
	height: 30px;
	border: none;
	background: rgba(255, 255, 255, 0.1);
	color: #fff;
	font-size: 20px;
	border-radius: 50%;
	cursor: pointer;
	display: flex;
	align-items: center;
	justify-content: center;
	transition: all 0.3s ease;
}

.close-panel:hover {
	background: #FFD700;
	color: #1a1a1a;
}

.panel-tabs {
	display: flex;
	padding: 15px 20px 0;
	gap: 10px;
}

.tab-btn {
	padding: 8px 15px;
	background: rgba(255, 255, 255, 0.1);
	border: none;
	border-radius: 20px;
	color: #fff;
	font-size: 13px;
	cursor: pointer;
	transition: all 0.3s ease;
}

.tab-btn:hover {
	background: rgba(255, 215, 0, 0.2);
}

.tab-btn.active {
	background: #FFD700;
	color: #1a1a1a;
}

.panel-content {
	flex: 1;
	padding: 20px;
	overflow-y: auto;
}

.tab-content {
	height: 100%;
}

.related-list {
	display: flex;
	flex-direction: column;
	gap: 10px;
}

.related-item {
	display: flex;
	justify-content: space-between;
	align-items: center;
	padding: 12px;
	background: rgba(0, 0, 0, 0.3);
	border-radius: 8px;
	transition: all 0.3s ease;
}

.related-item:hover {
	background: rgba(255, 215, 0, 0.1);
}

.related-name {
	color: #fff;
	font-size: 14px;
}

.jump-btn {
	padding: 4px 12px;
	background: rgba(255, 215, 0, 0.3);
	border: 1px solid #FFD700;
	border-radius: 16px;
	color: #FFD700;
	font-size: 12px;
	cursor: pointer;
	transition: all 0.3s ease;
}

.jump-btn:hover {
	background: #FFD700;
	color: #1a1a1a;
}

.dynasty-bg {
	padding: 15px;
	background: rgba(0, 0, 0, 0.3);
	border-radius: 8px;
}

.dynasty-bg h4 {
	color: #FFD700;
	margin: 0 0 10px 0;
	font-size: 16px;
}

.dynasty-bg .dynasty-period {
	color: #aaa;
	font-size: 13px;
	margin-bottom: 10px;
}

.dynasty-bg .dynasty-desc {
	color: #fff;
	font-size: 14px;
	line-height: 1.6;
	margin-bottom: 15px;
}

/* 事件类型徽章样式 */
.event-type-badge {
	display: inline-block;
	padding: 3px 12px;
	border-radius: 20px;
	font-size: 12px;
	margin-bottom: 10px;
}

.event-type-badge.political {
	background: rgba(255, 77, 77, 0.3);
	color: #FF4D4D;
}

.event-type-badge.military {
	background: rgba(255, 165, 0, 0.3);
	color: #FFA500;
}

.event-type-badge.cultural {
	background: rgba(76, 175, 80, 0.3);
	color: #4CAF50;
}

.empty-state {
	padding: 30px;
	text-align: center;
	color: #666;
	font-size: 14px;
}

/* 对比面板样式 */
.compare-content {
	padding: 20px;
	overflow-y: auto;
}

.compare-section {
	margin-bottom: 30px;
}

.compare-section h4 {
	color: #FFD700;
	margin: 0 0 15px 0;
	font-size: 16px;
}

.compare-section h5 {
	color: #fff;
	margin: 10px 0;
	font-size: 14px;
}

.timeline-compare {
	position: relative;
	height: 100px;
	background: rgba(0, 0, 0, 0.3);
	border-radius: 8px;
	padding: 20px 0;
}

.timeline-item {
	position: absolute;
	bottom: 20px;
	transform: translateX(-50%);
}

.timeline-dot {
	width: 12px;
	height: 12px;
	border-radius: 50%;
	margin-bottom: 5px;
}

.timeline-dot.political {
	background: #FF4D4D;
	box-shadow: 0 0 10px #FF4D4D;
}

.timeline-dot.military {
	background: #FFA500;
	box-shadow: 0 0 10px #FFA500;
}

.timeline-dot.cultural {
	background: #4CAF50;
	box-shadow: 0 0 10px #4CAF50;
}

.timeline-label {
	text-align: center;
}

.timeline-label .event-name {
	font-size: 11px;
	color: #fff;
	white-space: nowrap;
}

.timeline-label .event-year {
	font-size: 10px;
	color: #FFD700;
}

.type-chart {
	display: flex;
	flex-direction: column;
	gap: 10px;
}

.type-bar {
	display: flex;
	align-items: center;
	gap: 10px;
}

.type-label {
	width: 60px;
	color: #fff;
	font-size: 13px;
}

.bar-container {
	flex: 1;
	height: 20px;
	background: rgba(255, 255, 255, 0.1);
	border-radius: 10px;
	overflow: hidden;
}

.bar {
	height: 100%;
	transition: width 0.3s ease;
}

.bar.political {
	background: #FF4D4D;
}

.bar.military {
	background: #FFA500;
}

.bar.cultural {
	background: #4CAF50;
}

.person-analysis {
	display: flex;
	flex-direction: column;
	gap: 20px;
}

.person-tags {
	display: flex;
	flex-wrap: wrap;
	gap: 8px;
	margin-top: 5px;
}

.person-tag {
	padding: 4px 10px;
	background: rgba(255, 215, 0, 0.2);
	border: 1px solid #FFD700;
	border-radius: 16px;
	color: #FFD700;
	font-size: 12px;
}

.person-tag.unique {
	background: rgba(76, 175, 80, 0.2);
	border-color: #4CAF50;
	color: #4CAF50;
}

.event-name-small {
	color: #fff;
	font-size: 13px;
	margin-bottom: 5px;
}

.event-unique {
	margin-bottom: 15px;
	padding: 10px;
	background: rgba(0, 0, 0, 0.2);
	border-radius: 8px;
}

/* 事件详情弹窗样式 */
.event-modal {
	position: fixed;
	top: 0;
	left: 0;
	width: 100%;
	height: 100%;
	background: rgba(0, 0, 0, 0.8);
	backdrop-filter: blur(5px);
	display: flex;
	align-items: center;
	justify-content: center;
	z-index: 1000;
}

.modal-content {
	background: rgba(30, 30, 30, 0.95);
	border: 2px solid #FFD700;
	border-radius: 16px;
	padding: 30px;
	max-width: 500px;
	width: 90%;
	position: relative;
	animation: modalAppear 0.3s ease;
}

@keyframes modalAppear {
	from {
		opacity: 0;
		transform: scale(0.9);
	}
	to {
		opacity: 1;
		transform: scale(1);
	}
}

.modal-close {
	position: absolute;
	top: 15px;
	right: 15px;
	width: 30px;
	height: 30px;
	border: none;
	background: rgba(255, 255, 255, 0.1);
	color: #fff;
	font-size: 20px;
	border-radius: 50%;
	cursor: pointer;
	display: flex;
	align-items: center;
	justify-content: center;
	transition: all 0.3s ease;
}

.modal-close:hover {
	background: #FFD700;
	color: #1a1a1a;
}

.modal-title {
	color: #FFD700;
	font-size: 24px;
	margin: 0 0 20px 0;
	padding-right: 30px;
}

.modal-info p {
	margin: 10px 0;
	color: #fff;
	line-height: 1.6;
}

.info-label {
	color: #FFD700;
	font-weight: bold;
	margin-right: 10px;
}

.event-description {
	margin: 20px 0 !important;
	padding: 15px;
	background: rgba(0, 0, 0, 0.3);
	border-radius: 8px;
	font-style: italic;
}

.related-info {
	margin-top: 20px;
	padding-top: 20px;
	border-top: 1px solid rgba(255, 215, 0, 0.3);
}

.related-info h4 {
	color: #FFD700;
	margin: 0 0 10px 0;
	font-size: 16px;
}

.related-info ul {
	margin: 0;
	padding-left: 20px;
	color: #fff;
}

.related-info li {
	margin-bottom: 5px;
}

/* 朝代卡片事件计数 */
.dynasty-event-count {
	margin-top: 5px;
	font-size: 11px;
	color: rgba(255, 255, 255, 0.8);
}

.related-tag {
	margin-left: 8px;
	padding: 1px 6px;
	background: #4CAF50;
	border-radius: 10px;
	color: #fff;
	font-size: 10px;
}

.time-line {
	display: flex;
	margin: 0 auto;
	flex-wrap: wrap;
	flex-direction: column;
	max-width: 800px; 
	position: relative;
	padding: 0 20px;
}

.time-line:before {
	position: absolute;
	left: 50%;
	top: 0;
	content: '';
	width: 3px; 
	height: 100%;
	background: linear-gradient(to bottom, #888, #fff, #888); 
	margin-left: -1.5px;
	border-radius: 3px; 
	box-shadow: 0 0 10px rgba(255, 255, 255, 0.2); 
}

.item {
	padding: 50px 0;
	opacity: 0.4; 
	transition: all 0.6s cubic-bezier(0.22, 1, 0.36, 1);
	box-sizing: border-box;
	width: calc(50% - 50px);
	display: flex;
	position: relative;
	transform: translateY(-80px);
	justify-content: center;
	flex-direction: column;
}

.content {
	position: relative;
}

.item:before {
	content: attr(data-text);
	letter-spacing: 5px;
	width: 100%;
	position: absolute;
	color: #fff;
	top: 50%;
	margin-top: -20px;
	padding-left: 20px;
	opacity: 0;
	right: calc(-100% - 60px);
	font-size: 22px;
	font-weight: 500;
	text-shadow: 0 1px 5px rgba(0, 0, 0, 0.2);
	transition: all 0.4s ease;
}

.item:nth-child(even) {
	align-self: flex-end;
	text-align: right;
	left: auto;
	right: 0;
	border-right: 3px solid rgba(255, 255, 255, 0.6); 
	padding-right: 20px;
}

.item:nth-child(odd) {
	border-left: 3px solid rgba(255, 255, 255, 0.6);
	padding-left: 20px;
}

.item:nth-child(even):before {
	right: auto;
	left: calc(-100% - 60px);
	padding-left: 0;
	text-align: left;
}

.item--active {
	opacity: 1;
	transform: translateY(0);
	filter: blur(0px);
}

.item--active:before {
	top: 50%;
	opacity: 1;
	transform: translateY(-50%); 
}

.item--active .content-title {
	margin: 0 0 25px 0;
}

.item.has-highlight {
	opacity: 1;
}

.content-title {
	font-weight: 300;
	font-size: 48px; 
	margin: 10px 0 0 0;
	transition: all 0.4s ease;
	padding: 0 15px;
	box-sizing: border-box;
	color: #fff;
	line-height: 1.2;
	text-shadow: 0 3px 15px rgba(0, 0, 0, 0.4);
	position: absolute;
	top: 20px; 
	left: 50%;
	transform: translateX(-50%);
	z-index: 10;
	width: 100%;
	text-align: center;
}

.content-desc {
	margin: 0;
	font-size: 16px; 
	box-sizing: border-box;
	color: #f0f0f0;
	line-height: 1.8; 
	padding: 0 15px;
	background: rgba(0, 0, 0, 0.2);
	padding: 15px;
	border-radius: 8px; 
	backdrop-filter: blur(5px);
	position: relative;
	z-index: 5;
}

.img {
	width: 100%;
	max-width: 500px;
	height: auto; 
	box-shadow: 0 15px 30px rgba(0, 0, 0, 0.5); 
	border-radius: 8px;
	margin: 60px 0 20px 0; 
	transition: all 0.5s ease;
	z-index: 1; 
	position: relative; 
}

.item--active .img {
	transform: scale(1.02);
}

/* 时间轴模式区域样式 */
.timeline-mode-section {
	margin-top: 60px;
	padding: 20px;
	position: relative;
	z-index: 20;
}

.mode-buttons {
	display: flex;
	justify-content: center;
	gap: 30px;
	margin-bottom: 30px;
}

.mode-btn {
	display: flex;
	flex-direction: column;
	align-items: center;
	gap: 10px;
	padding: 15px 30px;
	background: rgba(255, 255, 255, 0.1);
	border: 2px solid rgba(255, 255, 255, 0.2);
	border-radius: 12px;
	color: #fff;
	cursor: pointer;
	transition: all 0.3s ease;
	min-width: 120px;
}

.mode-btn:hover {
	background: rgba(255, 215, 0, 0.2);
	border-color: #FFD700;
	transform: translateY(-5px);
}

.mode-btn.active {
	background: #FFD700;
	border-color: #FFD700;
	color: #1a1a1a;
}

.mode-icon {
	font-size: 32px;
}

.mode-text {
	font-size: 16px;
	font-weight: 500;
}

/* 时间轴视图面板样式 */
.timeline-view-panel {
	background: rgba(30, 30, 30, 0.95);
	backdrop-filter: blur(10px);
	border-radius: 16px;
	border: 1px solid rgba(255, 215, 0, 0.3);
	margin-top: 20px;
	overflow: hidden;
	animation: slideUp 0.3s ease;
}

@keyframes slideUp {
	from {
		opacity: 0;
		transform: translateY(30px);
	}
	to {
		opacity: 1;
		transform: translateY(0);
	}
}

.view-header {
	display: flex;
	justify-content: space-between;
	align-items: center;
	padding: 15px 20px;
	border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.view-header h3 {
	margin: 0;
	color: #FFD700;
	font-size: 18px;
}

.close-view-btn {
	width: 30px;
	height: 30px;
	border: none;
	background: rgba(255, 255, 255, 0.1);
	color: #fff;
	font-size: 20px;
	border-radius: 50%;
	cursor: pointer;
	display: flex;
	align-items: center;
	justify-content: center;
	transition: all 0.3s ease;
}

.close-view-btn:hover {
	background: #FFD700;
	color: #1a1a1a;
}

.view-content {
	padding: 20px;
	min-height: 300px;
}

/* 朝代分段网格样式 */
.dynasty-grid {
	display: grid;
	grid-template-columns: repeat(auto-fill, minmax(150px, 1fr));
	gap: 15px;
}

.dynasty-card {
	padding: 15px;
	border-radius: 10px;
	text-align: center;
	cursor: pointer;
	transition: all 0.3s ease;
	opacity: 0.8;
}

.dynasty-card:hover {
	transform: scale(1.05);
	opacity: 1;
	box-shadow: 0 5px 20px rgba(255, 215, 0, 0.3);
}

.dynasty-name {
	font-size: 18px;
	font-weight: bold;
	color: #fff;
	margin-bottom: 5px;
}

.dynasty-period {
	font-size: 12px;
	color: rgba(255, 255, 255, 0.8);
}

/* 聚焦模式样式 */
.focus-controls {
	margin-bottom: 20px;
}

.focus-select {
	width: 100%;
	padding: 12px;
	background: rgba(255, 255, 255, 0.1);
	border: 1px solid rgba(255, 215, 0, 0.3);
	border-radius: 8px;
	color: #fff;
	font-size: 16px;
	cursor: pointer;
}

.focus-select option {
	background: #333;
}

.focus-info {
	margin-top: 15px;
	padding: 15px;
	background: rgba(0, 0, 0, 0.3);
	border-radius: 8px;
}

.focus-period {
	color: #FFD700;
	font-size: 16px;
	margin-bottom: 10px;
}

.focus-events h4 {
	color: #fff;
	margin: 0 0 10px 0;
	font-size: 14px;
}

.focus-events ul {
	margin: 0;
	padding-left: 20px;
	color: #ccc;
}

.focus-events li {
	margin-bottom: 5px;
	font-size: 13px;
}

/* Canvas样式 */
.timeline-canvas {
	width: 100%;
	height: 300px;
	background: rgba(0, 0, 0, 0.2);
	border-radius: 8px;
}

.slide-up-enter-active,
.slide-up-leave-active {
	transition: all 0.3s ease;
}

.slide-up-enter-from,
.slide-up-leave-to {
	opacity: 0;
	transform: translateY(30px);
}

.slide-down-enter-active,
.slide-down-leave-active {
	transition: all 0.3s ease;
}

.slide-down-enter-from,
.slide-down-leave-to {
	opacity: 0;
	transform: translateY(-20px);
}

.slide-left-enter-active,
.slide-left-leave-active {
	transition: all 0.3s ease;
}

.slide-left-enter-from,
.slide-left-leave-to {
	opacity: 0;
	transform: translateX(350px);
}

.fade-enter-active,
.fade-leave-active {
	transition: opacity 0.3s ease;
}

.fade-enter-from,
.fade-leave-to {
	opacity: 0;
}

@media only screen and (max-width: 767px) {
	.time-line:before {
		left: 60px; 
	}
	
	.item {
		align-self: baseline !important;
		width: 100%;
		padding: 0 30px 180px 90px; 
	}

	.item:nth-child(even) {
		border-right: none;
		border-left: 3px solid rgba(255, 255, 255, 0.6);
	}

	.item:before {
		left: -80px !important;
		padding: 0 !important;
		top: 30px;
		text-align: center !important;
		width: 60px;
		font-size: 18px;
	}

	.item:last-child {
		padding-bottom: 80px;
	}

	.title {
		font-size: 36px;
		letter-spacing: 5px;
	}

	.subtitle {
		font-size: 16px;
		letter-spacing: 5px;
	}

	.content-title {
		font-size: 36px;
		z-index: 10; 
		top: 15px; 
	}

	.content-desc {
		font-size: 15px;
	}

	.img {
		max-width: 100%;
		margin: 50px 0 20px 0;
	}
	
	/* 移动端适配 */
	.mode-buttons {
		flex-direction: column;
		align-items: center;
		gap: 15px;
	}
	
	.mode-btn {
		width: 100%;
		max-width: 200px;
		flex-direction: row;
		justify-content: center;
	}
	
	.dynasty-grid {
		grid-template-columns: repeat(2, 1fr);
	}
	
	.filter-row {
		flex-direction: column;
		gap: 15px;
	}
	
	.filter-item {
		min-width: 100%;
	}
	
	.checkbox-group {
		flex-direction: column;
		gap: 8px;
	}
	
	.app-main.panel-open {
		margin-right: 0;
	}
	
	.event-panel, .compare-panel {
		width: 100%;
	}
}

</style>