<template>
	<div class="tl-page">
		<topComponent />

		<!-- 历史背景层：随滑块切换为当前朝代代表人物 / 疆域图 -->
		<div class="tl-bg-wrap">
			<transition name="tl-bgfade">
				<div
					v-if="bgUrl"
					:key="bgUrl"
					class="tl-bg"
					:style="{ backgroundImage: `url(${bgUrl})` }"
				></div>
			</transition>
			<div class="tl-bg-mask"></div>
		</div>

		<main class="tl-main">
			<!-- 顶部信息条 -->
			<div class="tl-hud">
				<div class="tl-hud-left">
					<div class="tl-brand">华 夏 长 河</div>
					<div class="tl-brand-sub">按朝代与时间纵览兴衰 · 拖动或滚轮滑动底部时间轴切换历史画面</div>
				</div>
				<div class="tl-hud-right">
					<span class="tl-chip"><b>{{ dynasties.length }}</b> 朝代</span>
					<span class="tl-chip"><b>{{ events.length }}</b> 事件</span>
					<span class="tl-chip"><b>{{ persons.length }}</b> 人物</span>
				</div>
			</div>

			<!-- 当前时代标题 -->
			<div class="tl-era">
				<div class="tl-era-rail"></div>
				<div class="tl-era-text">
					<div class="tl-era-year">{{ currentYearText }}</div>
					<h1 class="tl-era-name">{{ currentDynastyLabel || '——' }}</h1>
					<p class="tl-era-sub">
						<span v-if="currentDynasty">{{ dynastyPeriod(currentDynasty) }}</span>
						<span v-if="currentDynasty && currentDynasty.capital" class="tl-era-dot">·</span>
						<span v-if="currentDynasty && currentDynasty.capital">都城 {{ currentDynasty.capital }}</span>
						<span v-if="currentDynasty && currentDynasty.founder" class="tl-era-dot">·</span>
						<span v-if="currentDynasty && currentDynasty.founder">开国 {{ currentDynasty.founder }}</span>
					</p>
					<p v-if="currentDynasty && currentDynasty.dynastyDesc" class="tl-era-desc">{{ currentDynasty.dynastyDesc }}</p>
				</div>
				<div class="tl-era-count">
					<span>本朝收录</span>
					<strong>{{ currentEvents.length }}</strong>
					<span>事件 · {{ currentPersons.length }} 人物</span>
				</div>
			</div>

			<!-- 本朝大事一览：点击直接查看详情 -->
			<div v-if="currentEvents.length" class="tl-events">
				<span class="tl-events-label">本朝大事</span>
				<button
					v-for="e in currentEvents"
					:key="'ec' + e.id"
					class="tl-event-chip"
					:class="{ active: e.id === currentEvent?.id }"
					@click="openEvent(e)"
				>
					<span class="tl-chip-dot" :style="{ background: typeColor(e.type) }"></span>
					<b>{{ e.name }}</b>
					<em>{{ formatYear(e.year) }}</em>
				</button>
			</div>

			<!-- 底部：朝代时间轴 + 可拖拽滑块 -->
			<div class="tl-dock" @wheel="onDockWheel">
				<button class="tl-nav-btn" title="上一个朝代" @click="step(-1)">‹</button>
				<div class="tl-bar-area">
					<div class="tl-labels">
						<div
							v-for="s in labels"
							:key="'lb' + s.id"
							class="tl-label"
							:class="{ active: s.index === currentIndex }"
							:style="{ left: s.labelX + '%' }"
							@click="jumpTo(s.index)"
						>
							<span class="tl-label-name">{{ s.name }}</span>
							<span class="tl-label-year">{{ s.years }}</span>
						</div>
					</div>
					<div
						class="tl-bar"
						ref="barRef"
						:class="{ dragging, 'no-anim': wheeling }"
						@pointerdown="onBarDown"
					>
						<div class="tl-bar-inner"></div>
						<div
							v-for="s in segments"
							:key="'sg' + s.id"
							class="tl-seg"
							:class="{ active: s.index === currentIndex }"
							:style="{ left: s.left + '%', width: s.w + '%', background: s.color }"
							:title="s.name + ' ' + s.years"
							@click.stop="jumpTo(s.index)"
						></div>
						<div class="tl-handle" :style="{ left: handlePct + '%' }">
							<span class="tl-handle-bubble">
								<b>{{ currentDynastyLabel }}</b>
								<i>{{ currentYearText }}</i>
							</span>
							<span class="tl-handle-grip"></span>
						</div>
					</div>
				</div>
				<button class="tl-nav-btn" title="下一个朝代" @click="step(1)">›</button>
			</div>
		</main>

		<!-- 加载遮罩 -->
		<div v-if="loading" class="tl-loading">
			<div class="tl-loading-ring"></div>
			<span>正在展开历史长河…</span>
		</div>

		<!-- 详情抽屉 -->
		<transition name="tl-drawer">
			<div v-if="drawerItem" class="tl-drawer">
				<button class="tl-drawer-close" @click="drawerItem = null">×</button>
				<div class="tl-drawer-cover">
					<img :src="drawerKind === 'person' ? personImg(drawerItem) : eventImg(drawerItem)" :alt="drawerTitle" @error="onEventImgError">
					<span v-if="drawerKind === 'event'" class="tl-event-type" :style="{ background: typeColor(drawerItem.type) }">{{ typeLabel(drawerItem.type) }}</span>
				</div>
				<div class="tl-drawer-body">
					<div class="tl-drawer-kicker">{{ drawerKind === 'person' ? '历史人物' : formatYear(drawerItem.year) }}</div>
					<h2 class="tl-drawer-title">{{ drawerTitle }}</h2>
					<div class="tl-drawer-meta">
						<span class="tl-drawer-dynasty">{{ drawerDynastyLabel }}</span>
						<span v-if="drawerKind === 'person'" class="tl-drawer-life">{{ lifeSpan(drawerItem) }}</span>
					</div>
					<p class="tl-drawer-text">{{ drawerText }}</p>
					<button class="tl-drawer-btn" @click="goFullDetail">
						{{ drawerKind === 'person' ? '进入人物科普 →' : '查看完整事件科普 →' }}
					</button>
				</div>
			</div>
		</transition>
	</div>
</template>

<script setup>
import { computed, nextTick, onBeforeUnmount, onMounted, ref } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import topComponent from '@/components/topComponent.vue';
import request, { resolveAssetUrl, errorMessage } from '@/utils/request';

const router = useRouter();

/* ============ 状态 ============ */
const dynasties = ref([]);   // 后端朝代（含地图）
const events = ref([]);      // 全部历史事件
const persons = ref([]);     // 全部历史人物
const loading = ref(true);

const currentIndex = ref(0); // 当前所处朝代下标
const handlePct = ref(0);    // 滑块位置（%）—— 拖动即“滑动时间轴”
const dragging = ref(false);
const wheeling = ref(false);

const barRef = ref(null);

const drawerItem = ref(null);
const drawerKind = ref('event');

/* ============ 常量 ============ */
const DYNASTY_COLORS = [
	'#b8453b', '#c76a2e', '#c9a227', '#6f8f46', '#3f7d84', '#4a6fa5',
	'#7a5ba6', '#a05075', '#4f8a63', '#c47b3a', '#3a7ca5', '#8a6a3b',
	'#5b7d99', '#9c4b4b'
];

const TYPE_META = {
	political: { color: '#e0524d', label: '政治' },
	military: { color: '#e08a2b', label: '军事' },
	cultural: { color: '#4caf7d', label: '文化' },
	economic: { color: '#3d8fd1', label: '经济' },
	social: { color: '#9b5fd0', label: '社会' },
	other: { color: '#7a8290', label: '其他' }
};

// 代表人物挑选优先级
const IDENTITY_PRIORITY = ['帝王', '皇帝', '君主', '政治家', '军事家', '思想家', '文学家', '科学家'];

/* ============ 工具函数 ============ */
/** 朝代简称补全：夏 → 夏朝（三国/南北朝本身已是完整称呼） */
function toDisplayName(raw) {
	if (!raw) return '';
	if (raw.includes('三国') || raw.includes('南北朝')) return raw;
	return /朝$/.test(raw) ? raw : `${raw}朝`;
}

function formatYear(year) {
	if (year === null || year === undefined || Number.isNaN(year)) return '未知年代';
	return year < 0 ? `公元前${Math.abs(year)}年` : `公元${year}年`;
}

function shortYear(year) {
	if (year === null || year === undefined || Number.isNaN(year)) return '—';
	return year < 0 ? `前${Math.abs(year)}` : `${year}`;
}

function dynastyPeriod(d) {
	if (!d) return '';
	return `${shortYear(d.startYear)} — ${shortYear(d.endYear)}`;
}

function lifeSpan(p) {
	if (!p) return '';
	return `${formatYear(p.birthYear)} — ${formatYear(p.deathYear)}`;
}

function normalizeType(type) {
	const map = { '政治': 'political', '军事': 'military', '文化': 'cultural', '经济': 'economic', '社会': 'social' };
	if (!type) return 'other';
	if (map[type]) return map[type];
	if (Object.prototype.hasOwnProperty.call(TYPE_META, type)) return type;
	return 'other';
}

function typeColor(type) { return (TYPE_META[type] || TYPE_META.other).color; }
function typeLabel(type) { return (TYPE_META[type] || TYPE_META.other).label; }

/* ============ 数据加载 ============ */
async function fetchByDynasty(url, ids) {
	const tasks = ids.map(id =>
		request.get(url, { params: { pageNum: 1, pageSize: 1000, dynastyId: id } }).catch(() => null)
	);
	const results = await Promise.all(tasks);
	let records = [];
	results.forEach(res => {
		if (res && res.data && res.data.code === 200) {
			records = records.concat(res.data.data?.records || []);
		}
	});
	return records;
}

async function loadAll() {
	loading.value = true;
	try {
		const res = await request.get('/dynasty/list/all');
		const list = (res.data?.data || [])
			.filter(d => d && typeof d.startYear === 'number' && typeof d.endYear === 'number')
			.sort((a, b) => a.startYear - b.startYear);
		dynasties.value = list;

		const ids = list.map(d => d.id);
		const [rawEvents, rawPersons] = await Promise.all([
			fetchByDynasty('/historical-event/page', ids),
			fetchByDynasty('/historical-person/page', ids)
		]);

		events.value = rawEvents.map(e => ({
			id: e.id,
			name: e.eventName,
			year: e.eventYear,
			dynastyId: e.dynastyId,
			type: normalizeType(e.eventType),
			description: e.eventBg || e.eventProcess || '暂无描述',
			picPath: e.eventPicPath
		}));

		persons.value = rawPersons.map(p => ({
			id: p.id,
			dynastyId: p.dynastyId,
			personName: p.personName,
			identityType: p.identityType || '',
			birthYear: p.birthYear,
			deathYear: p.deathYear,
			avatarPath: p.avatarPath,
			personBio: p.personBio || ''
		}));
	} catch (error) {
		ElMessage.error(errorMessage(error, '加载时间轴数据失败'));
	} finally {
		loading.value = false;
		nextTick(() => jumpTo(0));
	}
}

/* ============ 时间轴分段 ============ */
const segments = computed(() => {
	const list = dynasties.value;
	if (!list.length) return [];
	// 等宽排布：每个朝代占据完全相同的宽度，位置固定，不受真实年份跨度影响
	const w = 100 / list.length;

	return list.map((d, i) => {
		const left = i * w;
		const right = left + w;
		return {
			id: d.id,
			index: i,
			raw: d,
			name: toDisplayName(d.name),
			years: dynastyPeriod(d),
			left,
			right,
			w,
			color: DYNASTY_COLORS[i % DYNASTY_COLORS.length]
		};
	});
});

/** 标签行：等宽色段，标签直接落在各自色段正中 */
const labels = computed(() => {
	return segments.value.map(s => ({ ...s, labelX: s.left + s.w / 2 }));
});

/* ============ 当前朝代 / 内容 ============ */
const currentSegment = computed(() => segments.value[currentIndex.value] || null);
const currentDynasty = computed(() => currentSegment.value?.raw || null);
const currentDynastyLabel = computed(() => currentSegment.value?.name || '');
const currentEvents = computed(() => {
	const id = currentDynasty.value?.id;
	if (id === undefined) return [];
	return events.value
		.filter(e => e.dynastyId === id)
		.sort((a, b) => (a.year ?? 0) - (b.year ?? 0));
});

/** 当前事件：把本朝色段按其事件数量等分，滑块所在的那一格即当前事件（每件大事占比完全相同） */
const currentEvent = computed(() => {
	const list = currentEvents.value;
	const s = currentSegment.value;
	if (!list.length || !s || s.w <= 0) return null;
	const ratio = Math.max(0, Math.min(1, (handlePct.value - s.left) / s.w));
	const idx = Math.min(list.length - 1, Math.floor(ratio * list.length));
	return list[idx];
});

const currentPersons = computed(() => {
	const id = currentDynasty.value?.id;
	if (id === undefined) return [];
	return persons.value.filter(p => p.dynastyId === id);
});

/** 代表人物：优先帝王，其次名臣名将等 */
const featuredPerson = computed(() => {
	const pool = currentPersons.value;
	if (!pool.length) return null;
	const withImg = pool.filter(p => p.avatarPath);
	const candidates = withImg.length ? withImg : pool;
	for (const key of IDENTITY_PRIORITY) {
		const hit = candidates.find(p => (p.identityType || '').includes(key));
		if (hit) return hit;
	}
	return candidates[0];
});

/** 当前年份：在所处朝代区间内按滑块位置线性估算 */
const currentYear = computed(() => {
	const s = currentSegment.value;
	if (!s) return null;
	const w = s.right - s.left;
	if (w <= 0) return s.raw.startYear;
	const ratio = Math.max(0, Math.min(1, (handlePct.value - s.left) / w));
	return Math.round(s.raw.startYear + ratio * (s.raw.endYear - s.raw.startYear));
});
const currentYearText = computed(() => formatYear(currentYear.value));

/** 背景图：随滑块落在的当前事件切换为其配图，其次代表人物画像，最后朝代疆域图 */
const bgUrl = computed(() => {
	if (currentEvent.value?.picPath) return resolveAssetUrl(currentEvent.value.picPath);
	if (featuredPerson.value?.avatarPath) return resolveAssetUrl(featuredPerson.value.avatarPath);
	if (currentDynasty.value?.mapPath) return resolveAssetUrl(currentDynasty.value.mapPath);
	return '';
});

/** 事件图片兜底：加载失败时退回落级到朝代疆域图 */
const dynastyMapUrl = computed(() => currentDynasty.value?.mapPath ? resolveAssetUrl(currentDynasty.value.mapPath) : '');

function personImg(p) {
	if (!p) return '';
	return resolveAssetUrl(p.avatarPath) || dynastyMapUrl.value;
}
function eventImg(e) {
	if (!e) return '';
	return resolveAssetUrl(e.picPath) || dynastyMapUrl.value;
}
function onEventImgError(ev) {
	const el = ev.target;
	if (!el || el.dataset.fallback || !dynastyMapUrl.value) return;
	el.dataset.fallback = '1';
	el.src = dynastyMapUrl.value;
}

/* ============ 滑块交互 ============ */
function pctFromClientX(clientX) {
	const el = barRef.value;
	if (!el) return handlePct.value;
	const rect = el.getBoundingClientRect();
	if (!rect.width) return handlePct.value;
	return Math.max(0, Math.min(100, ((clientX - rect.left) / rect.width) * 100));
}

function nearestIndex(p) {
	let best = 0;
	let bestDist = Infinity;
	segments.value.forEach((s, i) => {
		const dist = p < s.left ? s.left - p : (p > s.right ? p - s.right : 0);
		if (dist < bestDist) { bestDist = dist; best = i; }
	});
	return best;
}

function updateFromPct(p) {
	handlePct.value = Math.max(0, Math.min(100, p));
	const idx = nearestIndex(handlePct.value);
	if (idx !== currentIndex.value) currentIndex.value = idx;
}

function onBarDown(ev) {
	dragging.value = true;
	updateFromPct(pctFromClientX(ev.clientX));
	window.addEventListener('pointermove', onWindowMove);
	window.addEventListener('pointerup', onWindowUp);
}

function onWindowMove(ev) {
	if (!dragging.value) return;
	updateFromPct(pctFromClientX(ev.clientX));
}

function onWindowUp() {
	dragging.value = false;
	window.removeEventListener('pointermove', onWindowMove);
	window.removeEventListener('pointerup', onWindowUp);
}

// 滚轮：在整条时间轴上滚动即可连续滑动浏览各朝代与事件
let wheelTimer = null;
function onDockWheel(ev) {
	ev.preventDefault();
	wheeling.value = true;
	const delta = ev.deltaY > 0 ? 1.4 : -1.4;
	updateFromPct(handlePct.value + delta);
	if (wheelTimer) clearTimeout(wheelTimer);
	wheelTimer = setTimeout(() => { wheeling.value = false; }, 200);
}

function jumpTo(index) {
	const s = segments.value[index];
	if (!s) return;
	dragging.value = false;
	currentIndex.value = index;
	handlePct.value = (s.left + s.right) / 2;
}

function step(delta) {
	const n = segments.value.length;
	if (!n) return;
	jumpTo((currentIndex.value + delta + n) % n);
}

function onKeydown(ev) {
	if (ev.key === 'ArrowRight') { step(1); }
	else if (ev.key === 'ArrowLeft') { step(-1); }
	else if (ev.key === 'Escape') { drawerItem.value = null; }
}

/* ============ 详情抽屉 ============ */
function openEvent(e) { drawerKind.value = 'event'; drawerItem.value = e; }
function openPerson(p) { drawerKind.value = 'person'; drawerItem.value = p; }

const drawerTitle = computed(() => {
	if (!drawerItem.value) return '';
	return drawerKind.value === 'person' ? drawerItem.value.personName : drawerItem.value.name;
});

const drawerDynastyLabel = computed(() => {
	if (!drawerItem.value) return '';
	const d = dynasties.value.find(x => x.id === drawerItem.value.dynastyId);
	return d ? toDisplayName(d.name) : '';
});

const drawerText = computed(() => {
	if (!drawerItem.value) return '';
	return drawerKind.value === 'person' ? drawerItem.value.personBio : drawerItem.value.description;
});

function goFullDetail() {
	if (!drawerItem.value) return;
	if (drawerKind.value === 'person') {
		router.push({ path: '/person', query: { keyword: drawerItem.value.personName } });
	} else {
		router.push({ path: '/event', query: { id: drawerItem.value.id, name: drawerItem.value.name } });
	}
	drawerItem.value = null;
}

/* ============ 生命周期 ============ */
onMounted(() => {
	loadAll();
	window.addEventListener('keydown', onKeydown);
});

onBeforeUnmount(() => {
	window.removeEventListener('keydown', onKeydown);
	window.removeEventListener('pointermove', onWindowMove);
	window.removeEventListener('pointerup', onWindowUp);
});
</script>

<style scoped>
.tl-page {
	--gold: #d8b467;
	--ink: #0d0b12;
	position: relative;
	min-height: 100vh;
	background: var(--ink);
	overflow: hidden;
	color: #f4efe6;
}

/* ===== 背景 ===== */
.tl-bg-wrap {
	position: fixed;
	inset: 0;
	z-index: 0;
	overflow: hidden;
	pointer-events: none;
}
.tl-bg {
	position: absolute;
	inset: 0;
	background-size: cover;
	background-position: center 20%;
	transform: scale(1.04);
	filter: saturate(0.9);
}
.tl-bgfade-enter-active,
.tl-bgfade-leave-active { transition: opacity 0.9s ease; }
.tl-bgfade-enter-from,
.tl-bgfade-leave-to { opacity: 0; }
.tl-bg-mask {
	position: absolute;
	inset: 0;
	background:
		linear-gradient(180deg, rgba(13, 11, 18, 0.72) 0%, rgba(13, 11, 18, 0.42) 42%, rgba(13, 11, 18, 0.92) 100%),
		radial-gradient(120% 80% at 50% 0%, rgba(13, 11, 18, 0.1) 0%, rgba(13, 11, 18, 0.85) 100%);
}

/* ===== 主体 ===== */
.tl-main {
	position: relative;
	z-index: 1;
	display: flex;
	flex-direction: column;
	height: calc(100vh - 70px);
	padding: 22px clamp(16px, 4vw, 54px) 26px;
	box-sizing: border-box;
}

.tl-hud {
	display: flex;
	align-items: flex-start;
	justify-content: space-between;
	gap: 18px;
	flex-wrap: wrap;
}
.tl-brand {
	font-size: 22px;
	font-weight: 800;
	letter-spacing: 0.32em;
	color: var(--gold);
	text-shadow: 0 2px 18px rgba(0, 0, 0, 0.6);
}
.tl-brand-sub {
	margin-top: 6px;
	font-size: 12.5px;
	color: rgba(244, 239, 230, 0.6);
	letter-spacing: 0.04em;
}
.tl-hud-right { display: flex; gap: 10px; }
.tl-chip {
	padding: 7px 14px;
	border-radius: 999px;
	font-size: 12.5px;
	color: rgba(244, 239, 230, 0.78);
	background: rgba(255, 255, 255, 0.07);
	border: 1px solid rgba(216, 180, 103, 0.22);
	backdrop-filter: blur(6px);
}
.tl-chip b { color: var(--gold); font-size: 15px; margin-right: 3px; }

/* ===== 时代标题 ===== */
.tl-era {
	display: flex;
	align-items: center;
	gap: 22px;
	margin-top: clamp(10px, 3vh, 34px);
}
.tl-era-rail {
	width: 4px;
	align-self: stretch;
	border-radius: 4px;
	background: linear-gradient(180deg, var(--gold), rgba(216, 180, 103, 0.15));
}
.tl-era-year {
	font-size: 13px;
	letter-spacing: 0.24em;
	color: rgba(216, 180, 103, 0.9);
}
.tl-era-name {
	margin: 4px 0 8px;
	font-size: clamp(30px, 4.6vw, 52px);
	line-height: 1.05;
	font-weight: 800;
	letter-spacing: 0.14em;
	color: #fff;
	text-shadow: 0 6px 30px rgba(0, 0, 0, 0.75);
}
.tl-era-sub {
	margin: 0;
	font-size: 13.5px;
	color: rgba(244, 239, 230, 0.72);
}
.tl-era-dot { margin: 0 8px; color: rgba(216, 180, 103, 0.55); }
.tl-era-desc {
	margin: 8px 0 0;
	max-width: 640px;
	font-size: 13px;
	line-height: 1.7;
	color: rgba(244, 239, 230, 0.58);
}
.tl-era-count {
	margin-left: auto;
	text-align: right;
	font-size: 12.5px;
	color: rgba(244, 239, 230, 0.55);
	white-space: nowrap;
}
.tl-era-count strong {
	display: block;
	font-size: 34px;
	line-height: 1.1;
	color: var(--gold);
	font-weight: 800;
}

/* ===== 本朝大事 ===== */
.tl-events {
	display: flex;
	align-items: center;
	flex-wrap: wrap;
	gap: 8px;
	margin-top: clamp(10px, 2.2vh, 20px);
	max-height: 96px;
	overflow-y: auto;
}
.tl-events-label {
	flex: 0 0 auto;
	padding-right: 4px;
	font-size: 12px;
	letter-spacing: 0.16em;
	color: rgba(216, 180, 103, 0.85);
}
.tl-event-chip {
	display: inline-flex;
	align-items: center;
	gap: 7px;
	padding: 6px 12px;
	border-radius: 999px;
	font-size: 12.5px;
	color: rgba(244, 239, 230, 0.88);
	background: rgba(255, 255, 255, 0.06);
	border: 1px solid rgba(216, 180, 103, 0.22);
	cursor: pointer;
	transition: background 0.2s ease, border-color 0.2s ease, transform 0.2s ease;
}
.tl-event-chip:hover {
	background: rgba(216, 180, 103, 0.16);
	border-color: rgba(216, 180, 103, 0.5);
	transform: translateY(-1px);
}
.tl-event-chip.active {
	background: rgba(216, 180, 103, 0.24);
	border-color: rgba(216, 180, 103, 0.75);
	color: #fff;
	box-shadow: 0 0 0 1px rgba(216, 180, 103, 0.35);
}
.tl-chip-dot { flex: 0 0 auto; width: 7px; height: 7px; border-radius: 50%; }
.tl-event-chip b { font-weight: 600; }
.tl-event-chip em { font-style: normal; font-size: 11px; color: rgba(216, 180, 103, 0.8); }

/* 事件类型标签（抽屉封面复用） */
.tl-event-type {
	position: absolute;
	top: 10px;
	left: 10px;
	padding: 3px 10px;
	border-radius: 6px;
	font-size: 11px;
	color: #fff;
	letter-spacing: 0.06em;
	box-shadow: 0 4px 12px rgba(0, 0, 0, 0.35);
}

/* ===== 底部时间轴 ===== */
.tl-dock {
	display: flex;
	align-items: center;
	gap: 12px;
	margin-top: auto;
	padding: 14px 16px 10px;
	border-radius: 18px;
	background: linear-gradient(180deg, rgba(20, 17, 26, 0.72), rgba(12, 10, 16, 0.88));
	border: 1px solid rgba(216, 180, 103, 0.16);
	backdrop-filter: blur(10px);
}
.tl-nav-btn {
	flex: 0 0 auto;
	width: 40px;
	height: 40px;
	border-radius: 50%;
	font-size: 20px;
	line-height: 1;
	color: var(--gold);
	background: rgba(216, 180, 103, 0.1);
	border: 1px solid rgba(216, 180, 103, 0.3);
	cursor: pointer;
	transition: background 0.2s ease, transform 0.2s ease;
}
.tl-nav-btn:hover { background: rgba(216, 180, 103, 0.24); transform: scale(1.06); }

.tl-bar-area { position: relative; flex: 1; padding-top: 46px; }

/* 标签行 */
.tl-labels {
	position: absolute;
	top: 0;
	left: 0;
	right: 0;
	height: 40px;
}
.tl-label {
	position: absolute;
	transform: translateX(-50%);
	text-align: center;
	white-space: nowrap;
	cursor: pointer;
	transition: color 0.2s ease, transform 0.2s ease;
}
.tl-label-name {
	display: block;
	font-size: 13px;
	font-weight: 700;
	letter-spacing: 0.08em;
	color: rgba(244, 239, 230, 0.62);
}
.tl-label-year {
	display: block;
	margin-top: 2px;
	font-size: 10.5px;
	color: rgba(244, 239, 230, 0.34);
}
.tl-label.active .tl-label-name { color: var(--gold); text-shadow: 0 0 16px rgba(216, 180, 103, 0.5); }
.tl-label.active .tl-label-year { color: rgba(216, 180, 103, 0.7); }

/* 色段栏 */
.tl-bar {
	position: relative;
	height: 26px;
	border-radius: 8px;
	background: rgba(255, 255, 255, 0.05);
	cursor: pointer;
	touch-action: none;
}
.tl-bar-inner {
	position: absolute;
	inset: 0;
	border-radius: 8px;
	overflow: hidden;
	opacity: 0.28;
}
.tl-seg {
	position: absolute;
	top: 0;
	height: 100%;
	opacity: 0.55;
	transition: opacity 0.25s ease, filter 0.25s ease;
	box-shadow: inset -1px 0 0 rgba(0, 0, 0, 0.32);
}
.tl-seg:first-child { border-top-left-radius: 8px; border-bottom-left-radius: 8px; }
.tl-seg:last-child { border-top-right-radius: 8px; border-bottom-right-radius: 8px; }
.tl-seg.active { opacity: 1; filter: brightness(1.12); box-shadow: 0 0 0 1px rgba(216, 180, 103, 0.9), inset 0 0 22px rgba(255, 255, 255, 0.28); }

/* 滑块 */
.tl-handle {
	position: absolute;
	top: -14px;
	width: 3px;
	height: 54px;
	transform: translateX(-50%);
	background: linear-gradient(180deg, rgba(255, 255, 255, 0.15), #fff 22%, #fff 78%, rgba(255, 255, 255, 0.15));
	border-radius: 3px;
	box-shadow: 0 0 20px rgba(216, 180, 103, 0.85), 0 0 0 1px rgba(216, 180, 103, 0.7);
	cursor: grab;
	transition: left 0.32s cubic-bezier(0.4, 0, 0.2, 1);
	z-index: 3;
}
.tl-bar.dragging .tl-handle { transition: none; cursor: grabbing; }
.tl-bar.no-anim .tl-handle { transition: none; }
.tl-handle-grip {
	position: absolute;
	top: 50%;
	left: 50%;
	width: 15px;
	height: 15px;
	transform: translate(-50%, -50%);
	border-radius: 50%;
	background: var(--gold);
	border: 2px solid #fff;
	box-shadow: 0 2px 10px rgba(0, 0, 0, 0.6);
}
.tl-handle-bubble {
	position: absolute;
	bottom: calc(100% + 8px);
	left: 50%;
	transform: translateX(-50%);
	padding: 6px 12px;
	border-radius: 10px;
	white-space: nowrap;
	text-align: center;
	background: rgba(216, 180, 103, 0.95);
	color: #1a1408;
	box-shadow: 0 8px 22px rgba(0, 0, 0, 0.5);
}
.tl-handle-bubble b { display: block; font-size: 14px; letter-spacing: 0.08em; }
.tl-handle-bubble i { display: block; font-style: normal; font-size: 10.5px; opacity: 0.75; }
.tl-handle-bubble::after {
	content: '';
	position: absolute;
	top: 100%;
	left: 50%;
	transform: translateX(-50%);
	border: 6px solid transparent;
	border-top-color: rgba(216, 180, 103, 0.95);
}

/* ===== 加载 ===== */
.tl-loading {
	position: fixed;
	inset: 70px 0 0 0;
	z-index: 20;
	display: flex;
	flex-direction: column;
	align-items: center;
	justify-content: center;
	gap: 18px;
	background: rgba(13, 11, 18, 0.88);
	color: rgba(244, 239, 230, 0.7);
	font-size: 14px;
	letter-spacing: 0.14em;
}
.tl-loading-ring {
	width: 46px;
	height: 46px;
	border-radius: 50%;
	border: 3px solid rgba(216, 180, 103, 0.2);
	border-top-color: var(--gold);
	animation: tl-spin 0.9s linear infinite;
}
@keyframes tl-spin { to { transform: rotate(360deg); } }

/* ===== 详情抽屉 ===== */
.tl-drawer {
	position: fixed;
	top: 0;
	right: 0;
	bottom: 0;
	z-index: 30;
	width: min(430px, 92vw);
	background: linear-gradient(180deg, #1b1620 0%, #100d15 100%);
	border-left: 1px solid rgba(216, 180, 103, 0.22);
	box-shadow: -24px 0 60px rgba(0, 0, 0, 0.6);
	overflow-y: auto;
}
.tl-drawer-close {
	position: absolute;
	top: 14px;
	right: 16px;
	z-index: 2;
	width: 34px;
	height: 34px;
	border-radius: 50%;
	font-size: 20px;
	color: #fff;
	background: rgba(0, 0, 0, 0.45);
	border: 1px solid rgba(255, 255, 255, 0.18);
	cursor: pointer;
}
.tl-drawer-cover { position: relative; height: 250px; overflow: hidden; }
.tl-drawer-cover img { width: 100%; height: 100%; object-fit: cover; object-position: center 20%; }
.tl-drawer-cover .tl-event-type { top: auto; bottom: 14px; left: 18px; }
.tl-drawer-body { padding: 22px 26px 34px; }
.tl-drawer-kicker { font-size: 12px; letter-spacing: 0.2em; color: rgba(216, 180, 103, 0.85); }
.tl-drawer-title { margin: 8px 0 10px; font-size: 26px; color: #fff; letter-spacing: 0.05em; }
.tl-drawer-meta { display: flex; gap: 12px; font-size: 12.5px; color: rgba(244, 239, 230, 0.55); }
.tl-drawer-dynasty {
	padding: 2px 10px;
	border-radius: 999px;
	color: var(--gold);
	background: rgba(216, 180, 103, 0.14);
	border: 1px solid rgba(216, 180, 103, 0.3);
}
.tl-drawer-text { margin: 16px 0 24px; font-size: 14px; line-height: 1.85; color: rgba(244, 239, 230, 0.78); }
.tl-drawer-btn {
	width: 100%;
	padding: 13px 0;
	border-radius: 12px;
	font-size: 14.5px;
	letter-spacing: 0.06em;
	color: #1a1408;
	background: linear-gradient(135deg, #e2c078, var(--gold));
	border: none;
	cursor: pointer;
	transition: filter 0.2s ease, transform 0.2s ease;
}
.tl-drawer-btn:hover { filter: brightness(1.08); transform: translateY(-1px); }

.tl-drawer-enter-active,
.tl-drawer-leave-active { transition: transform 0.32s cubic-bezier(0.4, 0, 0.2, 1), opacity 0.32s ease; }
.tl-drawer-enter-from,
.tl-drawer-leave-to { transform: translateX(100%); opacity: 0.4; }

/* ===== 响应式 ===== */
@media (max-width: 900px) {
	.tl-main { height: auto; min-height: calc(100vh - 70px); }
	.tl-era-count { display: none; }
	.tl-events { max-height: 76px; }
	.tl-dock { flex-wrap: nowrap; padding: 12px 10px 8px; }
	.tl-label-year { display: none; }
}
</style>
