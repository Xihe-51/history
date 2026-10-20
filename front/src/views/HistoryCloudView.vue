<template>
  <div class="hc-page">
    <topComponent />

    <div class="hc-stage" ref="stageRef">
      <canvas ref="canvasRef" class="hc-canvas"></canvas>
      <div class="hc-vignette"></div>
      <div class="hc-scrim hc-scrim-top"></div>
      <div class="hc-scrim hc-scrim-bottom"></div>

      <!-- 星点名称层（事件 / 人物 / 典故，自动避让） -->
      <div class="hc-labels hc-labels-star" ref="starLabelRef"></div>

      <!-- 朝代标签层（每帧投影定位） -->
      <div class="hc-labels">
        <div
          v-for="(d, i) in dynasties"
          :key="'dl' + d.id"
          class="hc-label"
          :class="{ 'is-active': activeDynastyId === d.id }"
          :title="`定位到${d.name}`"
          @click="focusDynasty(d, i)"
        >
          <span class="hc-label-dot" :style="{ background: dynastyColor(i) }"></span>
          <span class="hc-label-txt">{{ d.name }}</span>
        </div>
      </div>

      <!-- 左上：标题与统计 -->
      <div class="hc-hud hc-hud-title">
        <div class="hc-brand">史 云 · HISTORY CLOUD</div>
        <div class="hc-sub">
          {{ dynasties.length }} 个朝代 · {{ eventNodes.length }} 件史事 · {{ personNodes.length }} 位人物 · {{ allusionNodes.length }} 条典故
        </div>
      </div>

      <!-- 右上：工具条 -->
      <div class="hc-hud hc-hud-tools">
        <button class="hc-tool" :class="{ on: autoRotate }" @click="toggleAutoRotate">自动旋转</button>
        <button class="hc-tool" @click="resetView">复位视角</button>
        <button class="hc-tool" :class="{ on: showMore }" @click="showMore = !showMore">更多</button>
      </div>

      <!-- 更多面板 -->
      <div v-if="showMore" class="hc-hud hc-more">
        <div class="hc-more-row">
          <span>显示事件星</span>
          <el-switch v-model="showEvents" size="small" @change="applyLayers" />
        </div>
        <div class="hc-more-row">
          <span>显示人物星</span>
          <el-switch v-model="showPersons" size="small" @change="applyLayers" />
        </div>
        <div class="hc-more-row">
          <span>显示典故星</span>
          <el-switch v-model="showAllusions" size="small" @change="applyLayers" />
        </div>
        <div class="hc-more-row">
          <span>星团轨迹环</span>
          <el-switch v-model="showShells" size="small" @change="applyLayers" />
        </div>
        <div class="hc-more-row">
          <span>朝代传承弧</span>
          <el-switch v-model="showSuccession" size="small" @change="applyLayers" />
        </div>
        <div class="hc-more-row">
          <span>归属连线</span>
          <el-switch v-model="showAffiliation" size="small" @change="applyLayers" />
        </div>
        <div class="hc-more-row hc-more-slider">
          <span>星点密度</span>
          <el-slider v-model="density" :min="0.6" :max="2.6" :step="0.1" size="small" />
        </div>
        <div class="hc-more-legend">
          <div class="hc-lg-title">事件类型</div>
          <div class="hc-lg-items">
            <span v-for="(v, k) in TYPE_META" :key="'lt' + k" class="hc-lg-item">
              <i :style="{ background: v.color }"></i>{{ v.label }}
            </span>
          </div>
          <div class="hc-lg-title">人物身份</div>
          <div class="hc-lg-items">
            <span v-for="(v, k) in IDENTITY_META" :key="'li' + k" class="hc-lg-item">
              <i :style="{ background: v.color }"></i>{{ v.label }}
            </span>
          </div>
        </div>
      </div>

      <!-- 左侧：朝代图例 / 定位 -->
      <div class="hc-hud hc-legend">
        <div class="hc-legend-head">朝代 · 点击定位</div>
        <div class="hc-legend-body">
          <div
            v-for="(d, i) in dynasties"
            :key="'lg' + d.id"
            class="hc-legend-item"
            :class="{ active: activeDynastyId === d.id }"
            @click="focusDynasty(d, i)"
          >
            <span class="hc-legend-dot" :style="{ background: dynastyColor(i) }"></span>
            <span class="hc-legend-name">{{ d.name }}</span>
            <span class="hc-legend-years">{{ dynastyYears(d) }}</span>
          </div>
        </div>
      </div>

      <!-- 底部：操作提示 -->
      <div class="hc-hud hc-hint">
        电脑版：WASD 控制方向 · Q/E 升降 · Shift 加速 · 鼠标拖拽转向 · 滚轮前后推进 &nbsp;|&nbsp; 手机版：单指拖拽转向 · 双指前后推动 · 点击星点查看详情
      </div>

      <!-- 悬浮提示 -->
      <div
        v-if="hoverTip"
        class="hc-tooltip"
        :style="{ left: hoverTip.x + 'px', top: hoverTip.y + 'px' }"
      >
        <span class="hc-tt-kind" :style="{ color: hoverTip.color }">{{ hoverTip.kindLabel }}</span>
        <span class="hc-tt-name">{{ hoverTip.name }}</span>
      </div>

      <!-- 详情面板 -->
      <div v-if="selected" class="hc-hud hc-detail">
        <button class="hc-detail-close" @click="closeDetail">×</button>
        <div v-if="selected.image" class="hc-detail-imgwrap">
          <img :src="selected.image" alt="" @error="onImgError" />
        </div>
        <div class="hc-detail-head">
          <span class="hc-detail-kind" :style="{ background: selected.color + '22', color: selected.color, borderColor: selected.color + '55' }">
            {{ selected.kindLabel }}
          </span>
          <h3 class="hc-detail-name">{{ selected.name }}</h3>
        </div>
        <div class="hc-detail-meta">
          <span v-for="(m, i) in selected.meta" :key="'mt' + i">{{ m }}</span>
        </div>
        <div class="hc-detail-fields">
          <p v-for="(f, i) in selected.fields" :key="'fd' + i">
            <b>{{ f.label }}</b>
            <span>{{ f.value }}</span>
          </p>
        </div>
        <button class="hc-detail-link" @click="goDetailPage">
          前往{{ selected.pageLabel }}
        </button>
      </div>

      <!-- 加载中 -->
      <div v-if="loading" class="hc-loading">
        <div class="hc-loading-ring"></div>
        <p>正在点亮史云…</p>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted, onBeforeUnmount, ref, watch } from 'vue';
import { useRouter } from 'vue-router';
import * as THREE from 'three';
import { EffectComposer } from 'three/examples/jsm/postprocessing/EffectComposer.js';
import { RenderPass } from 'three/examples/jsm/postprocessing/RenderPass.js';
import { UnrealBloomPass } from 'three/examples/jsm/postprocessing/UnrealBloomPass.js';
import { OutputPass } from 'three/examples/jsm/postprocessing/OutputPass.js';
import { ElMessage } from 'element-plus';
import topComponent from '@/components/topComponent.vue';
import request, { resolveAssetUrl, errorMessage } from '@/utils/request';

/* ========================= 常量与配色 ========================= */

/**
 * 朝代配色：统一在「墨蓝 → 靛青 → 石青 → 青玉 → 鎏金」的国风色阶内，
 * 保证明度接近、饱和度收敛，远看是同一片星云，而不是杂色噪点。
 * 顺序按历史时序推进（远古偏冷蓝，越往后越偏暖金）。
 */
const DYNASTY_COLORS = [
  '#5B7DB1', // 夏 · 玄青
  '#4A86A8', // 商 · 靛蓝
  '#3E8E9E', // 周 · 青碧
  '#4FA3A8', // 秦 · 石青
  '#5AA8B8', // 汉 · 苍青
  '#6E9EC4', // 三国 · 雾蓝
  '#7F9FC9', // 晋 · 天青
  '#8FA8CE', // 南北朝 · 月白
  '#7BA8B0', // 隋 · 青瓷
  '#C9A86A', // 唐 · 鎏金（最盛，最暖）
  '#B8B084', // 宋 · 秋香
  '#9FB0B8', // 元 · 灰青
  '#C4A878', // 明 · 赤金
  '#8FA3B8'  // 清 · 黛蓝
];


/** 事件：统一暖色系（珊瑚 / 琥珀 / 金），与人物冷色系明确区分 */
const TYPE_META: Record<string, { color: string; label: string }> = {
  political: { color: '#FF8A5B', label: '政治' },
  military: { color: '#FF5E7A', label: '军事' },
  cultural: { color: '#FFC857', label: '文化' },
  economic: { color: '#FFA24B', label: '经济' },
  social: { color: '#F47FB8', label: '社会' },
  other: { color: '#D8B094', label: '其他' }
};

/** 人物：统一冷色系（青 / 蓝 / 薄荷） */
const IDENTITY_META: Record<string, { color: string; label: string }> = {
  帝王: { color: '#8FC7FF', label: '帝王' },
  政治家: { color: '#5BD3F0', label: '政治家' },
  军事家: { color: '#7E97FF', label: '军事家' },
  外交家: { color: '#54E0C8', label: '外交家' },
  文人: { color: '#9BB0FF', label: '文人' },
  科学家: { color: '#68E2A6', label: '科学家' },
  其他: { color: '#A9BCD8', label: '其他' }
};

/** 典故：紫色系，与事件 / 人物都能区分 */
const ALLUSION_COLOR = '#C08BFF';
const CORE_BASE_SIZE = 20;

/* ========================= 类型 ========================= */

type Kind = 'event' | 'person' | 'allusion' | 'core';

interface CloudNode {
  kind: Kind;
  id: number;
  name: string;
  dynastyId: number | null;
  dynastyName: string;
  year: number | null;
  color: string;
  baseSize: number;
  pos: THREE.Vector3;
  raw: any;
}

interface PointsPack {
  object: THREE.Points;
  material: THREE.ShaderMaterial;
  nodes: CloudNode[];
  sizes: Float32Array;
  sizeAttr: THREE.BufferAttribute;
  baseOpacity: number;
}

interface DetailState {
  kind: Kind;
  name: string;
  kindLabel: string;
  color: string;
  image: string;
  meta: string[];
  fields: { label: string; value: string }[];
  pagePath: string;
  pageLabel: string;
}

/* ========================= 响应式状态 ========================= */

const router = useRouter();
const stageRef = ref<HTMLDivElement | null>(null);
const canvasRef = ref<HTMLCanvasElement | null>(null);

const loading = ref(true);
const showMore = ref(false);
const showEvents = ref(true);
const showPersons = ref(true);
const showAllusions = ref(true);
const showShells = ref(true);
const showSuccession = ref(true);
const showAffiliation = ref(true);
const autoRotate = ref(true);
const density = ref(1.2);
const activeDynastyId = ref<number | null>(null);

const starLabelRef = ref<HTMLDivElement | null>(null);

const hoverTip = ref<null | { x: number; y: number; name: string; kindLabel: string; color: string }>(null);
const selected = ref<DetailState | null>(null);

const dynasties = ref<any[]>([]);
const eventNodes = ref<CloudNode[]>([]);
const personNodes = ref<CloudNode[]>([]);
const allusionNodes = ref<CloudNode[]>([]);

/* ========================= 三维运行时 ========================= */

let renderer: THREE.WebGLRenderer | null = null;
let scene: THREE.Scene | null = null;
let camera: THREE.PerspectiveCamera | null = null;
let galaxyGroup: THREE.Group;
let dustPoints: THREE.Points | null = null;
let successionLines: THREE.LineSegments | null = null;
let affiliationLines: THREE.LineSegments | null = null;
const packs: PointsPack[] = [];
let coresPack: PointsPack | null = null;
let cores: CloudNode[] = [];
const coreLocal: THREE.Vector3[] = [];
let rafId = 0;
let starTexture: THREE.Texture | null = null;
let coreTexture: THREE.Texture | null = null;
let glowTexture: THREE.Texture | null = null;
let nebulaTexture: THREE.Texture | null = null;
let resizeObserver: ResizeObserver | null = null;
const clock = new THREE.Clock();

/* ---- 后处理泛光 ---- */
let composer: EffectComposer | null = null;
let bloomPass: UnrealBloomPass | null = null;

/* ---- 需要逐帧驱动时间的材质 ---- */
const timeMaterials: THREE.ShaderMaterial[] = [];
/* ---- 朝代核星光晕 ---- */
const coreHalos: THREE.Sprite[] = [];
/* ---- 星团轨迹环 ---- */
let structureLines: THREE.LineSegments | null = null;
/* ---- 背景天球 ---- */
let bgMesh: THREE.Mesh | null = null;

/* ---- 星系布局：紧凑的立体双臂旋涡 ----
 * 坐标空间与星系实际尺寸对齐：相机活动范围收敛到星系尺度的两倍以内，
 * 半径方向按「面积 × 指数盘」采样，盘面有厚度与翘曲，
 * 外缘用整数谐波叠加出不规则轮廓。
 */
/* ---- 世界坐标缩放倍率 ----
 * 星系的所有世界尺寸（坐标、半径、盘厚、翘曲、相机距离、背景层）统一按此倍率换算。
 * 注意：星点自身的 size 属性不跟着放大——它是「屏幕像素」的基准量，
 * 保持原值可以让星点在放大后的世界里变成更细小、更锐利的实心点。
 */
const WS = 5;

const CLUSTER_RADIUS = 340 * WS;
const GALAXY_R0 = 520 * WS;         // 银心外缘半径
const GALAXY_R1 = 3300 * WS;        // 星系外缘半径
const ARM_COUNT = 2;                // 两条主旋臂
const ARM_TWIST = 3.15;             // 旋臂缠绕弧度
const ARM_BASE_ANGLE = -1.35;       // 旋臂起始角
const ALLUSION_RADIUS = 3450 * WS;
const TAU = Math.PI * 2;

/* 相机活动范围（随世界尺度一起放大，保证仍能看全整个星系） */
const CAM_MIN_DIST = 170 * WS;
const CAM_MAX_DIST = 6400 * WS;

/** 星系外缘的角向起伏：整数谐波 → 首尾无缝；再加一个 m=1 项让盘面轻微偏心、不对称 */
function edgeShape(th: number): number {
  return (
    1 +
    0.075 * Math.sin(th + 0.6) +
    0.085 * Math.sin(th * 3 + 1.1) +
    0.055 * Math.sin(th * 7 + 2.3) +
    0.03 * Math.sin(th * 11 + 0.7) +
    0.018 * Math.sin(th * 19 + 1.9)
  );
}

/** 盘面翘曲：外盘整体上下起伏，侧视时不再是一张纸 */
function diskWarp(r: number, th: number): number {
  const k = Math.pow(THREE.MathUtils.clamp(r / GALAXY_R1, 0, 1.4), 2);
  return (Math.sin(th * 2 + r / (1500 * WS)) * 300 * WS + Math.sin(th * 5 - r / (900 * WS)) * 90 * WS) * k;
}

/** 旋臂上的基准点：t∈[0,1] 为半径进度，arm 为旋臂序号 */
function spiralPoint(t: number, arm: number): THREE.Vector3 {
  const r = GALAXY_R0 + t * (GALAXY_R1 - GALAXY_R0);
  const th = ARM_BASE_ANGLE + arm * ((Math.PI * 2) / ARM_COUNT) + t * ARM_TWIST;
  return new THREE.Vector3(Math.cos(th) * r, 0, Math.sin(th) * r);
}

/** 旋臂半宽：越靠外越宽，形成真正的「宽臂」而不是一根线 */
function armSigma(t: number): number {
  return (170 + t * 480) * WS;
}

/** 朝代核星位置：按时间沿半径外扩，轮流落在四条旋臂上（偏移固定，避免每次刷新都变形） */
function clusterCenter(i: number): THREE.Vector3 {
  const n = Math.max(1, dynasties.value.length);
  const t = n > 1 ? i / (n - 1) : 0.5;
  const arm = i % ARM_COUNT;
  const p = spiralPoint(t, arm);
  const len = Math.hypot(p.x, p.z) || 1;
  const nx = -p.z / len;
  const nz = p.x / len;
  const off = Math.sin(i * 1.9) * armSigma(t) * 0.5;
  p.x += nx * off;
  p.z += nz * off;
  p.y = diskWarp(Math.hypot(p.x, p.z), Math.atan2(p.z, p.x)) + Math.sin(i * 1.3) * 14 * WS;
  return p;
}

/* ---- 星点名称标签池 ---- */
const STAR_LABEL_MAX = 46;
let starLabels: HTMLElement[] = [];
let labelTick = 0;
const coreLabelPos: { x: number; y: number; len?: number }[] = [];

let yaw = 0;
let pitch = 0;
let lastInteraction = 0;
let selectedNode: CloudNode | null = null;

/* ---- 相机飞行 ---- */
let fly: {
  from: THREE.Vector3; to: THREE.Vector3;
  lookFrom: THREE.Vector3; lookTo: THREE.Vector3;
  t: number; dur: number;
} | null = null;

/* ---- 交互 ---- */
const pointers = new Map<number, { x: number; y: number }>();
let downInfo: { x: number; y: number; t: number } | null = null;
let pinchPrev = 0;
let dragDist = 0;
let dragEndTime = 0;
const keys = new Set<string>();

/* ---- 标签 DOM ---- */
let labelEls: HTMLElement[] = [];

/* ---- 自适应画质 ---- */
let fpsFrames = 0;
let fpsTime = 0;
let qualityDropped = false;

/* ========================= 工具函数 ========================= */

function dynastyColor(i: number) {
  return DYNASTY_COLORS[i % DYNASTY_COLORS.length];
}

function fmtYear(y: number | null | undefined): string {
  if (y === null || y === undefined || Number.isNaN(y)) return '—';
  return y < 0 ? `公元前 ${-y} 年` : `公元 ${y} 年`;
}

function dynastyYears(d: any): string {
  const s = fmtYear(d.startYear).replace(/公元 ?/, '').replace(/ 年/, '');
  const e = fmtYear(d.endYear).replace(/公元 ?/, '').replace(/ 年/, '');
  return `${s}-${e}`;
}

function normEventType(t: string | undefined): string {
  if (!t) return 'other';
  if (TYPE_META[t]) return t;
  const cn: Record<string, string> = { 政治: 'political', 军事: 'military', 文化: 'cultural', 经济: 'economic', 社会: 'social' };
  return cn[t] || 'other';
}

function pickColor(meta: Record<string, { color: string }>, key: string | undefined, fallback: string) {
  if (key && meta[key]) return meta[key].color;
  return fallback;
}

/** Fibonacci 球面均匀分布，返回单位向量 */
function fibSphere(index: number, total: number, phase: number): THREE.Vector3 {
  const ga = Math.PI * (3 - Math.sqrt(5));
  const y = 1 - ((index + 0.5) * 2) / Math.max(total, 1);
  const r = Math.sqrt(Math.max(0, 1 - y * y));
  const theta = ga * index + phase;
  return new THREE.Vector3(Math.cos(theta) * r, y, Math.sin(theta) * r);
}

/** 近似高斯采样（三次均匀分布相加），标准差约 0.33，用于打散规则网格感 */
function gaussRand(): number {
  return (Math.random() + Math.random() + Math.random() - 1.5) / 1.5;
}

/**
 * 星点贴图：实心硬边圆点。
 * 参考效果里星星是「有实体的锐利小点」，所以不透明区做到半径的 80%，
 * 只在最外 20% 收一圈很窄的抗锯齿，几乎没有外扩的柔晕。
 */
function makeStarTexture(): THREE.Texture {
  const S = 64;
  const R = S / 2;
  const c = document.createElement('canvas');
  c.width = c.height = S;
  const ctx = c.getContext('2d')!;
  const g = ctx.createRadialGradient(R, R, 0, R, R, R);
  g.addColorStop(0.0, 'rgba(255,255,255,1)');
  g.addColorStop(0.80, 'rgba(255,255,255,1)');
  g.addColorStop(0.88, 'rgba(255,255,255,0.72)');
  g.addColorStop(0.95, 'rgba(255,255,255,0.16)');
  g.addColorStop(1.0, 'rgba(255,255,255,0)');
  ctx.fillStyle = g;
  ctx.fillRect(0, 0, S, S);
  const tex = new THREE.CanvasTexture(c);
  tex.colorSpace = THREE.SRGBColorSpace;
  // 星点常被压到 1~3 像素，若使用 mipmap 会被平均成灰雾，必须关掉
  tex.generateMipmaps = false;
  tex.minFilter = THREE.LinearFilter;
  tex.magFilter = THREE.LinearFilter;
  return tex;
}

/** 朝代核星贴图：明亮核心 + 十字衍射星芒，做出「恒星」质感 */
function makeCoreTexture(): THREE.Texture {
  const S = 256;
  const R = S / 2;
  const c = document.createElement('canvas');
  c.width = c.height = S;
  const ctx = c.getContext('2d')!;

  // 核心 + 柔晕
  const g = ctx.createRadialGradient(R, R, 0, R, R, R);
  g.addColorStop(0.0, 'rgba(255,255,255,1)');
  g.addColorStop(0.1, 'rgba(255,255,255,0.92)');
  g.addColorStop(0.22, 'rgba(255,255,255,0.4)');
  g.addColorStop(0.45, 'rgba(255,255,255,0.11)');
  g.addColorStop(1.0, 'rgba(255,255,255,0)');
  ctx.fillStyle = g;
  ctx.fillRect(0, 0, S, S);

  // 衍射星芒
  ctx.globalCompositeOperation = 'lighter';
  const spike = (angle: number, len: number, width: number, alpha: number) => {
    ctx.save();
    ctx.translate(R, R);
    ctx.rotate(angle);
    const lg = ctx.createLinearGradient(-len, 0, len, 0);
    lg.addColorStop(0, 'rgba(255,255,255,0)');
    lg.addColorStop(0.5, `rgba(255,255,255,${alpha})`);
    lg.addColorStop(1, 'rgba(255,255,255,0)');
    ctx.fillStyle = lg;
    ctx.fillRect(-len, -width / 2, len * 2, width);
    ctx.restore();
  };
  spike(0, R * 0.96, 5, 0.75);
  spike(Math.PI / 2, R * 0.96, 5, 0.75);
  spike(Math.PI / 4, R * 0.62, 3.4, 0.3);
  spike(-Math.PI / 4, R * 0.62, 3.4, 0.3);

  const tex = new THREE.CanvasTexture(c);
  tex.colorSpace = THREE.SRGBColorSpace;
  return tex;
}

/** 大范围柔光，用于核星光晕与银心 */
function makeSoftGlowTexture(): THREE.Texture {
  const S = 128;
  const c = document.createElement('canvas');
  c.width = c.height = S;
  const ctx = c.getContext('2d')!;
  const g = ctx.createRadialGradient(S / 2, S / 2, 0, S / 2, S / 2, S / 2);
  g.addColorStop(0.0, 'rgba(255,255,255,0.9)');
  g.addColorStop(0.25, 'rgba(255,255,255,0.4)');
  g.addColorStop(0.55, 'rgba(255,255,255,0.12)');
  g.addColorStop(1.0, 'rgba(255,255,255,0)');
  ctx.fillStyle = g;
  ctx.fillRect(0, 0, S, S);
  const tex = new THREE.CanvasTexture(c);
  tex.colorSpace = THREE.SRGBColorSpace;
  return tex;
}

/** 不规则星云贴图（叠加随机柔斑，边缘羽化） */
function makeNebulaTexture(): THREE.Texture {
  const S = 256;
  const c = document.createElement('canvas');
  c.width = c.height = S;
  const ctx = c.getContext('2d')!;
  ctx.globalCompositeOperation = 'lighter';
  for (let i = 0; i < 26; i++) {
    const x = Math.random() * S;
    const y = Math.random() * S;
    const r = 26 + Math.random() * 74;
    const g = ctx.createRadialGradient(x, y, 0, x, y, r);
    g.addColorStop(0, `rgba(255,255,255,${0.08 + Math.random() * 0.16})`);
    g.addColorStop(1, 'rgba(255,255,255,0)');
    ctx.fillStyle = g;
    ctx.fillRect(0, 0, S, S);
  }
  // 径向遮罩，去掉方形硬边
  ctx.globalCompositeOperation = 'destination-in';
  const m = ctx.createRadialGradient(S / 2, S / 2, 0, S / 2, S / 2, S / 2);
  m.addColorStop(0, 'rgba(255,255,255,1)');
  m.addColorStop(0.62, 'rgba(255,255,255,0.75)');
  m.addColorStop(1, 'rgba(255,255,255,0)');
  ctx.fillStyle = m;
  ctx.fillRect(0, 0, S, S);
  const tex = new THREE.CanvasTexture(c);
  tex.colorSpace = THREE.SRGBColorSpace;
  return tex;
}

function makeStarMaterial(tex: THREE.Texture, opacity = 1, sharpen = 1): THREE.ShaderMaterial {
  const pr = renderer ? renderer.getPixelRatio() : 1;
  const material = new THREE.ShaderMaterial({
    uniforms: {
      pointTexture: { value: tex },
      uSizeScale: { value: density.value },
      uOpacity: { value: opacity },
      uSharpen: { value: sharpen },
      uPixelRatio: { value: pr },
      uProjScale: { value: 1150 },
      uTime: { value: 0 }
    },
    vertexShader: `
      attribute float size;
      attribute vec3 customColor;
      uniform float uSizeScale;
      uniform float uPixelRatio;
      uniform float uProjScale;
      uniform float uTime;
      varying vec3 vColor;
      varying float vFade;
      void main() {
        vColor = customColor;
        vec4 mv = modelViewMatrix * vec4(position, 1.0);
        float dist = max(-mv.z, 1.0);
        float tw = 0.88 + 0.12 * sin(uTime * 1.6 + position.x * 0.11 + position.y * 0.09 + position.z * 0.13);
        // 透视缩放用 0.8 次幂：远近差距被压平，近距离推进时星点不会膨胀成巨大光斑
        float ps = size * uSizeScale * uPixelRatio * tw * pow(uProjScale / dist, 0.8);
        // 上限 9 像素：保证放大观测时仍是「一颗锐利的小星」而不是一团糊
        gl_PointSize = clamp(ps, 1.0 * uPixelRatio, 9.0 * uPixelRatio);
        // 远离相机的点略微淡出，增强纵深（阈值随世界尺度换算）
        vFade = clamp(1.0 - (dist - 21000.0) / 70000.0, 0.4, 1.0);
        gl_Position = projectionMatrix * mv;
      }
    `,
    fragmentShader: `
      uniform sampler2D pointTexture;
      uniform float uOpacity;
      uniform float uSharpen;
      varying vec3 vColor;
      varying float vFade;
      void main() {
        vec4 t = texture2D(pointTexture, gl_PointCoord);
        if (t.a < 0.02) discard;
        // 锐化：把贴图边缘的软过渡压成硬边，星点变成轮廓分明的实心小圆点，
        // 成百上千颗叠在一起时也不会糊成一片灰雾（uSharpen=0 时保留核星的柔晕）
        float a = mix(t.a, smoothstep(0.30, 0.94, t.a), uSharpen);
        gl_FragColor = vec4(vColor, a * uOpacity * vFade);
      }
    `,
    blending: THREE.AdditiveBlending,
    depthTest: true,
    depthWrite: false,
    transparent: true
  });
  timeMaterials.push(material);
  return material;
}

/** 由裸数组直接构建点云（用于背景星场 / 银河带） */
function makePointObject(pos: Float32Array, col: Float32Array, siz: Float32Array, tex: THREE.Texture, opacity: number, sharpen = 1) {
  const geo = new THREE.BufferGeometry();
  geo.setAttribute('position', new THREE.BufferAttribute(pos, 3));
  geo.setAttribute('customColor', new THREE.BufferAttribute(col, 3));
  geo.setAttribute('size', new THREE.BufferAttribute(siz, 1));
  const material = makeStarMaterial(tex, opacity, sharpen);
  const object = new THREE.Points(geo, material);
  object.frustumCulled = false;
  return { object, material };
}

function buildPoints(nodes: CloudNode[], tex: THREE.Texture, opacity: number): PointsPack {
  const n = nodes.length;
  const positions = new Float32Array(n * 3);
  const colors = new Float32Array(n * 3);
  const sizes = new Float32Array(n);
  const col = new THREE.Color();
  nodes.forEach((nd, i) => {
    positions[i * 3] = nd.pos.x;
    positions[i * 3 + 1] = nd.pos.y;
    positions[i * 3 + 2] = nd.pos.z;
    col.set(nd.color);
    colors[i * 3] = col.r;
    colors[i * 3 + 1] = col.g;
    colors[i * 3 + 2] = col.b;
    sizes[i] = nd.baseSize;
  });

  const built = makePointObject(positions, colors, sizes, tex, opacity, tex === coreTexture ? 0 : 1);
  const sizeAttr = built.object.geometry.getAttribute('size') as THREE.BufferAttribute;

  return { object: built.object, material: built.material, nodes, sizes, sizeAttr, baseOpacity: opacity };
}

/* ========================= 数据加载 ========================= */

async function loadData() {
  try {
    const dynRes = await request.get('/dynasty/list/all');
    const list: any[] = dynRes.data?.data || [];
    const unique = new Map<number, any>();
    list.forEach(d => { if (!unique.has(d.id)) unique.set(d.id, d); });
    dynasties.value = Array.from(unique.values()).sort((a, b) => (a.startYear ?? 0) - (b.startYear ?? 0));

    // 逐朝代拉取事件与人物（接口 dynastyId 必填）
    const tasks: Promise<any>[] = [];
    dynasties.value.forEach((d, i) => {
      tasks.push(
        request.get('/historical-event/page', { params: { pageNum: 1, pageSize: 1000, dynastyId: d.id } })
          .then(r => ({ kind: 'event', i, d, records: r.data?.data?.records || [] }))
          .catch(() => ({ kind: 'event', i, d, records: [] }))
      );
      tasks.push(
        request.get('/historical-person/page', { params: { pageNum: 1, pageSize: 1000, dynastyId: d.id } })
          .then(r => ({ kind: 'person', i, d, records: r.data?.data?.records || [] }))
          .catch(() => ({ kind: 'person', i, d, records: [] }))
      );
    });

    const results = await Promise.all(tasks);

    const evNodes: CloudNode[] = [];
    const peNodes: CloudNode[] = [];

    dynasties.value.forEach((d, i) => {
      const center = clusterCenter(i);
      const phase = i * 0.9;
      const evs = results.find(r => r.kind === 'event' && r.i === i)?.records || [];
      const pes = results.find(r => r.kind === 'person' && r.i === i)?.records || [];
      const total = evs.length + pes.length;

      // 沿旋臂方向拉长分布，让星团融进臂带，而不是孤立成一个球
      const radial = new THREE.Vector3(center.x, 0, center.z);
      if (radial.lengthSq() < 1e-4) radial.set(1, 0, 0);
      radial.normalize();
      const tangent = new THREE.Vector3(-radial.z, 0, radial.x); // 切向 = 沿臂方向
      // 该朝代在时间轴上的归一化进度，用于匹配所在位置的旋臂宽度
      const tNorm = dynasties.value.length > 1 ? i / (dynasties.value.length - 1) : 0.5;

      let k = 0;
      const place = () => {
        const idx = k++;
        const golden = idx * 2.39996 + phase;
        // 高斯椭圆形分布：切向跨度更大，贴住旋臂走向，且没有矩形的硬边
        const alongArm = gaussRand() * CLUSTER_RADIUS * 1.55;
        const acrossArm = gaussRand() * armSigma(tNorm) * 0.62;
        const lift = gaussRand() * 95 * WS;
        return center.clone()
          .add(tangent.clone().multiplyScalar(alongArm))
          .add(radial.clone().multiplyScalar(acrossArm))
          .add(new THREE.Vector3(0, lift, 0))
          .add(new THREE.Vector3(
            Math.cos(golden) * 22 * WS,
            Math.sin(golden) * 16 * WS,
            Math.sin(golden) * 22 * WS
          ));
      };

      evs.forEach((e: any) => {
        const t = normEventType(e.eventType);
        evNodes.push({
          kind: 'event', id: e.id, name: e.eventName,
          dynastyId: d.id, dynastyName: d.name,
          year: typeof e.eventYear === 'number' ? e.eventYear : null,
          color: TYPE_META[t].color, baseSize: 8,
          pos: place(),
          raw: e
        });
      });
      pes.forEach((p: any) => {
        peNodes.push({
          kind: 'person', id: p.id, name: p.personName,
          dynastyId: d.id, dynastyName: d.name,
          year: typeof p.birthYear === 'number' ? p.birthYear : null,
          color: pickColor(IDENTITY_META, p.identityType, '#A9BCD8'), baseSize: 26,
          pos: place(),
          raw: p
        });
      });
    });

    eventNodes.value = evNodes;
    personNodes.value = peNodes;

    // 典故（无朝代外键，环绕星系外围形成一圈紫色星带）
    try {
      const alRes = await request.get('/allusion/page', { params: { pageNum: 1, pageSize: 200 } });
      const recs: any[] = alRes.data?.data?.records || [];
      const total = recs.length;
      allusionNodes.value = recs.map((a, i) => {
        const dir = fibSphere(i, total, 0.6);
        dir.y *= 0.55;
        dir.normalize();
        // 半径随角度起伏，避免一圈完美的紫色圆环
        const ang = Math.atan2(dir.z, dir.x);
        const r = ALLUSION_RADIUS * edgeShape(ang) * (0.94 + Math.random() * 0.2);
        const pos = dir.multiplyScalar(r);
        pos.y += diskWarp(r, ang) * 0.7;
        return {
          kind: 'allusion' as const, id: a.id, name: a.allusionName,
          dynastyId: null, dynastyName: '典故',
          year: null,
          color: ALLUSION_COLOR, baseSize: 30,
          pos,
          raw: a
        };
      });
    } catch {
      allusionNodes.value = [];
    }
  } catch (err) {
    ElMessage.error(errorMessage(err, '加载史云数据失败'));
  }
}

/* ========================= 场景构建 ========================= */

function buildGalaxy() {
  if (!scene || !starTexture || !coreTexture || !glowTexture) return;
  coreLocal.length = 0;

  // ---- 朝代核星：沿螺旋主轴分布，是全图的「星系骨架」----
  cores = dynasties.value.map((d, i) => {
    const pos = clusterCenter(i);
    coreLocal.push(pos.clone());
    return {
      kind: 'core' as const, id: d.id, name: d.name,
      dynastyId: d.id, dynastyName: d.name, year: d.startYear,
      color: dynastyColor(i), baseSize: CORE_BASE_SIZE,
      pos, raw: d
    };
  });
  coresPack = buildPoints(cores, coreTexture, 1);
  galaxyGroup.add(coresPack.object);

  // 核星只留一圈很小的柔光，避免大光晕糊住星点
  cores.forEach((nd, i) => {
    const halo = new THREE.Sprite(new THREE.SpriteMaterial({
      map: glowTexture,
      color: new THREE.Color(dynastyColor(i)),
      transparent: true, opacity: 0.12,
      blending: THREE.AdditiveBlending, depthWrite: false, depthTest: false
    }));
    halo.position.copy(nd.pos);
    halo.scale.setScalar(64 * WS);
    halo.renderOrder = 2;
    galaxyGroup.add(halo);
    coreHalos.push(halo);
  });

  // ---- 星团轨迹环：每个朝代一圈倾斜细环 ----
  const structVerts: number[] = [];
  const structCols: number[] = [];
  const sc = new THREE.Color();
  dynasties.value.forEach((_d, i) => {
    const c = coreLocal[i];
    sc.set(dynastyColor(i));
    const q = new THREE.Quaternion().setFromEuler(new THREE.Euler(0.55 + i * 0.13, i * 0.5, i * 0.21));
    const seg = 64;
    const rr = CLUSTER_RADIUS * 1.2;
    for (let s = 0; s < seg; s++) {
      const a1 = (s / seg) * Math.PI * 2;
      const a2 = ((s + 1) / seg) * Math.PI * 2;
      const p1 = new THREE.Vector3(Math.cos(a1) * rr, 0, Math.sin(a1) * rr).applyQuaternion(q).add(c);
      const p2 = new THREE.Vector3(Math.cos(a2) * rr, 0, Math.sin(a2) * rr).applyQuaternion(q).add(c);
      structVerts.push(p1.x, p1.y, p1.z, p2.x, p2.y, p2.z);
      structCols.push(sc.r, sc.g, sc.b, sc.r, sc.g, sc.b);
    }
  });
  const structGeo = new THREE.BufferGeometry();
  structGeo.setAttribute('position', new THREE.Float32BufferAttribute(structVerts, 3));
  structGeo.setAttribute('color', new THREE.Float32BufferAttribute(structCols, 3));
  structureLines = new THREE.LineSegments(
    structGeo,
    new THREE.LineBasicMaterial({ vertexColors: true, transparent: true, opacity: 0.07, blending: THREE.AdditiveBlending, depthWrite: false })
  );
  structureLines.visible = showShells.value;
  galaxyGroup.add(structureLines);

  // ---- 事件 / 人物 / 典故星点 ----
  const evPack = buildPoints(eventNodes.value, starTexture, 0.95);
  evPack.object.visible = showEvents.value;
  packs.push(evPack);
  galaxyGroup.add(evPack.object);

  const pePack = buildPoints(personNodes.value, starTexture, 0.92);
  pePack.object.visible = showPersons.value;
  packs.push(pePack);
  galaxyGroup.add(pePack.object);

  const alPack = buildPoints(allusionNodes.value, starTexture, 0.9);
  alPack.object.visible = showAllusions.value;
  packs.push(alPack);
  galaxyGroup.add(alPack.object);

  // ---- 星团尘埃（每个朝代一小团彩色微尘）----
  const dustN = 0;
  const totalDust = dynasties.value.length * dustN;
  const dpos = new Float32Array(totalDust * 3);
  const dcol = new Float32Array(totalDust * 3);
  const dsize = new Float32Array(totalDust);
  const dc = new THREE.Color();
  let di = 0;
  dynasties.value.forEach((_d, i) => {
    const c = coreLocal[i];
    dc.set(dynastyColor(i));
    for (let k = 0; k < dustN; k++) {
      const dir = fibSphere(k, dustN, i * 0.7 + 5.1);
      const rr = CLUSTER_RADIUS * (0.9 + Math.random() * 1.5);
      dpos[di * 3] = c.x + dir.x * rr;
      dpos[di * 3 + 1] = c.y + dir.y * rr;
      dpos[di * 3 + 2] = c.z + dir.z * rr;
      dcol[di * 3] = dc.r * 0.6; dcol[di * 3 + 1] = dc.g * 0.6; dcol[di * 3 + 2] = dc.b * 0.6;
      dsize[di] = 1 + Math.random() * 1.6;
      di++;
    }
  });
  const dustGeo = new THREE.BufferGeometry();
  dustGeo.setAttribute('position', new THREE.BufferAttribute(dpos, 3));
  dustGeo.setAttribute('customColor', new THREE.BufferAttribute(dcol, 3));
  dustGeo.setAttribute('size', new THREE.BufferAttribute(dsize, 1));
  const dustMat = makeStarMaterial(starTexture, 0.34);
  (dustMat.uniforms.uSizeScale as any).value = density.value * 0.85;
  dustPoints = new THREE.Points(dustGeo, dustMat);
  dustPoints.frustumCulled = false;
  dustPoints.visible = showShells.value;
  galaxyGroup.add(dustPoints);

  // ---- 连续星系盘 ----
  // 之前把盘面拆成「内盘 / 旋臂 / 外盘」三层，每层各有一个硬截断半径，
  // 叠起来后就在中心外侧形成一个亮度均匀、边界规整的白圈，完全不像星系。
  // 现在改成一套连续的密度场：
  //   · 半径按面积均匀采样（幂次 0.62 让中心略密），铺满整个盘面，不留空白也没有硬边；
  //   · 亮度只随半径平滑衰减，中途没有台阶，因此不会再出现环带；
  //   · 旋臂是叠加在同一场上的密度/相色调制，臂间仍保留星点，不出现空白带；
  //   · 多尺度伪噪声把旋臂打碎成纤维与结块，避免「画一圈绿环」的规则感；
  //   · 颜色沿半径连续过渡：金白核 → 蓝白内盘 → 翠绿臂 → 橙红外缘。
  const gauss = gaussRand;
  const tmpC = new THREE.Color();
  const cCoreHot = new THREE.Color('#FFF7E4');
  const cCoreWarm = new THREE.Color('#FFE7B0');
  const cWhite = new THREE.Color('#DCE7F7');
  const cArmGreen = new THREE.Color('#2FE195');
  const cArmDeep = new THREE.Color('#189A63');
  const cOuterRed = new THREE.Color('#E06A3A');
  const cPurple = new THREE.Color('#9B7BE4');
  const cBlue = new THREE.Color('#79A2E8');

  const gs = (s: number) => gauss() * 3 * s;
  const armTheta = (u: number, arm: number) => ARM_BASE_ANGLE + arm * Math.PI + u * ARM_TWIST;
  const RIM = GALAXY_R1 * 1.06;
  // 亮度增益：星点被压成 1~2 像素的实心小点后，单颗的总光通量只有原来的一半左右，
  // 这里统一回补，保证「更细更锐」的同时盘面结构不会变暗到读不出来。
  const GB = 2.1;

  // 盘厚：中心厚、外盘薄
  const thickness = (r: number) => (60 + 250 * Math.exp(-r / (720 * WS))) * WS;
  // 多尺度伪噪声（-1~1）：让盘面出现纤维与暗隙，而不是均匀的一大片
  // （径向频率要随世界尺度一起缩小，否则特性长度被放大会退化成纯色带）
  const floc = (th: number, r: number) =>
    0.42 * Math.sin(th * 6.5 + r * 0.0041 / WS) +
    0.3 * Math.sin(th * 11.5 - r * 0.0073 / WS + 1.7) +
    0.28 * Math.sin(th * 18 + r * 0.0119 / WS + 3.1);

  // 旋臂接近度：臂心 1、臂间 0；臂宽随半径向外张开
  const armProx = (r: number, th: number): number => {
    const u = THREE.MathUtils.clamp((r - 300 * WS) / (GALAXY_R1 * 1.02 - 300 * WS), 0, 1.2);
    let best = 0;
    for (let a = 0; a < ARM_COUNT; a++) {
      let d = (armTheta(u, a) - th) % TAU;
      if (d > Math.PI) d -= TAU;
      if (d < -Math.PI) d += TAU;
      const sig = 0.16 + 0.15 * u;
      const p = Math.exp(-(d * d) / (2 * sig * sig));
      if (p > best) best = p;
    }
    return best;
  };

  // 盘面采样：面积均匀（铺满、无空洞），幂次 0.62 让中心略密于外围
  const sampleField = () => {
    const th = Math.random() * TAU;
    const r = RIM * edgeShape(th) * Math.pow(Math.random(), 0.62);
    return { r, th };
  };

  // 连续配色：核 → 内盘 → 臂 → 外缘，全部平滑过渡，没有任何分段
  const diskColor = (rn: number, prox: number, out: THREE.Color) => {
    out.copy(cWhite);
    out.lerp(cCoreHot, Math.exp(-rn / 0.1));                 // 银心：暖白
    out.lerp(cCoreWarm, 0.4 * Math.exp(-rn / 0.22));
    const armBand = Math.exp(-Math.pow((rn - 0.5) / 0.5, 2)) * THREE.MathUtils.smoothstep(rn, 0.04, 0.26);
    const g = THREE.MathUtils.clamp(prox * armBand, 0, 1);
    out.lerp(cArmGreen, 0.9 * g);                            // 旋臂：翠绿
    out.lerp(cArmDeep, 0.35 * g * g);
    out.lerp(cOuterRed, 0.8 * THREE.MathUtils.smoothstep(rn, 0.55, 1.06));  // 外缘：橙红
    return out;
  };

  // 亮度：只随半径平滑衰减；旋臂处抬亮，噪声做细微调制
  const diskBright = (rn: number, prox: number, f: number) =>
    (0.5 * Math.exp(-rn / 0.15) + 0.22 * Math.exp(-rn / 0.68) + 0.055) *
    (0.62 + 0.9 * prox * THREE.MathUtils.smoothstep(rn, 0.04, 0.3)) *
    (0.78 + 0.34 * f);

  // (0) 核球：紧凑三维椭球，给银心真正的体积（同样平滑衰减，不留硬壳）
  const BULGE_N = 12000;
  const buPos = new Float32Array(BULGE_N * 3);
  const buCol = new Float32Array(BULGE_N * 3);
  const buSiz = new Float32Array(BULGE_N);
  for (let i = 0; i < BULGE_N; i++) {
    const dir = fibSphere(i, BULGE_N, 1.7);
    const rad = 260 * WS * Math.pow(Math.random(), 0.62);
    buPos[i * 3] = dir.x * rad;
    buPos[i * 3 + 1] = dir.y * rad * 0.68;
    buPos[i * 3 + 2] = dir.z * rad * 0.94;
    tmpC.copy(cCoreWarm).lerp(cCoreHot, 0.3 + Math.random() * 0.55);
    const b = (0.3 + 0.34 * Math.exp(-rad / (150 * WS)) + Math.random() * 0.1) * GB;
    buCol[i * 3] = tmpC.r * b; buCol[i * 3 + 1] = tmpC.g * b; buCol[i * 3 + 2] = tmpC.b * b;
    buSiz[i] = 2 + Math.random() * 2;
  }
  galaxyGroup.add(makePointObject(buPos, buCol, buSiz, starTexture, 0.95).object);

  // (1) 主星场：单场覆盖整个盘面，亮度与配色连续，是星系的底色
  const FIELD_N = 170000;
  const fdPos = new Float32Array(FIELD_N * 3);
  const fdCol = new Float32Array(FIELD_N * 3);
  const fdSiz = new Float32Array(FIELD_N);
  for (let i = 0; i < FIELD_N; i++) {
    const { r, th } = sampleField();
    const rn = r / GALAXY_R1;
    const prox = armProx(r, th);
    const f = floc(th, r);
    const jt = th + gs(0.02) + 0.02 * Math.sin(r * 0.02 / WS);   // 轻微角向扰动，打断规则弧线
    const rr = r * (1 + gauss() * 0.035);
    fdPos[i * 3] = Math.cos(jt) * rr;
    fdPos[i * 3 + 1] = gs(thickness(rr) * 0.6) + diskWarp(rr, jt);
    fdPos[i * 3 + 2] = Math.sin(jt) * rr;
    diskColor(rn, prox, tmpC);
    // 少量冷色/紫色星，增加真实星场的色温层次
    const rc = Math.random();
    if (rc < 0.05) tmpC.lerp(cBlue, 0.5);
    else if (rc < 0.085) tmpC.lerp(cPurple, 0.5);
    const b = diskBright(rn, prox, f) * (0.82 + Math.random() * 0.36) * GB;
    fdCol[i * 3] = tmpC.r * b; fdCol[i * 3 + 1] = tmpC.g * b; fdCol[i * 3 + 2] = tmpC.b * b;
    fdSiz[i] = 2.2 + Math.random() * 2;
    if (Math.random() < 0.012) fdSiz[i] = 5.5 + Math.random() * 3.5;
  }
  galaxyGroup.add(makePointObject(fdPos, fdCol, fdSiz, starTexture, 0.92).object);

  // (2) 旋臂加亮：额外把星点铺在两条旋臂上，让臂形从底色里「浮」出来（仍带噪声断口）
  const ARM_N = 70000;
  const amPos = new Float32Array(ARM_N * 3);
  const amCol = new Float32Array(ARM_N * 3);
  const amSiz = new Float32Array(ARM_N);
  for (let i = 0; i < ARM_N; i++) {
    const arm = i % ARM_COUNT;
    const u = Math.pow(Math.random(), 0.62);
    const r = 260 * WS + u * (RIM - 260 * WS);
    const wid = 0.15 + 0.14 * u;
    const dth = gs(wid);
    const th = armTheta(u, arm) + dth;
    const prox = Math.exp(-(dth * dth) / (2 * wid * wid));
    const f = floc(th, r);
    const rr = r * (1 + gauss() * 0.045);
    const rn = rr / GALAXY_R1;
    amPos[i * 3] = Math.cos(th) * rr;
    amPos[i * 3 + 1] = gs(thickness(rr) * 0.5) + diskWarp(rr, th);
    amPos[i * 3 + 2] = Math.sin(th) * rr;
    diskColor(rn, Math.max(prox, 0.15), tmpC);
    if (Math.random() < 0.035) tmpC.lerp(cPurple, 0.6);
    const b = (0.34 + 0.2 * Math.exp(-u * 2.2)) * (0.7 + 0.7 * prox) * (0.72 + 0.4 * f) * (0.85 + Math.random() * 0.3) * GB;
    amCol[i * 3] = tmpC.r * b; amCol[i * 3 + 1] = tmpC.g * b; amCol[i * 3 + 2] = tmpC.b * b;
    amSiz[i] = 2.3 + Math.random() * 2.2;
    if (Math.random() < 0.012) amSiz[i] = 5.5 + Math.random() * 3.5;
  }
  galaxyGroup.add(makePointObject(amPos, amCol, amSiz, starTexture, 0.9).object);

  // (3) 星团结块：挂在盘面上的小星团，制造密度起伏，避免「均匀撒盐」
  const CLOUDLETS = 210;
  const CLOUD_PER = 130;
  const CLOUD_N = CLOUDLETS * CLOUD_PER;
  const clPos = new Float32Array(CLOUD_N * 3);
  const clCol = new Float32Array(CLOUD_N * 3);
  const clSiz = new Float32Array(CLOUD_N);
  let ci = 0;
  for (let c = 0; c < CLOUDLETS; c++) {
    const { r, th } = sampleField();
    const rr = Math.max(140 * WS, r);
    const cx = Math.cos(th) * rr;
    const cz = Math.sin(th) * rr;
    const cy = gauss() * thickness(rr) * 0.5 + diskWarp(rr, th);
    const prox = armProx(rr, th);
    const sig = (24 + Math.random() * 76) * WS;
    diskColor(rr / GALAXY_R1, Math.max(prox, 0.2), tmpC);
    for (let k = 0; k < CLOUD_PER; k++) {
      clPos[ci * 3] = cx + gauss() * sig;
      clPos[ci * 3 + 1] = cy + gauss() * sig * 0.6;
      clPos[ci * 3 + 2] = cz + gauss() * sig;
      const b = (0.22 + 0.2 * Math.exp(-rr / (2600 * WS)) + Math.random() * 0.12) * (0.7 + 0.6 * prox) * GB;
      clCol[ci * 3] = tmpC.r * b; clCol[ci * 3 + 1] = tmpC.g * b; clCol[ci * 3 + 2] = tmpC.b * b;
      clSiz[ci] = 2.2 + Math.random() * 2.2;
      ci++;
    }
  }
  galaxyGroup.add(makePointObject(clPos, clCol, clSiz, starTexture, 0.72).object);

  // (4) 外围晕星：从盘缘向外稀疏拖尾，把边缘打散成绒毛状，没有「被刀切」的边界
  const HALO_N = 13000;
  const haPos = new Float32Array(HALO_N * 3);
  const haCol = new Float32Array(HALO_N * 3);
  const haSiz = new Float32Array(HALO_N);
  for (let i = 0; i < HALO_N; i++) {
    const th = Math.random() * TAU;
    const rEdge = RIM * edgeShape(th);
    const rr = rEdge * (0.78 + Math.pow(Math.random(), 2.1) * 1.4);
    haPos[i * 3] = Math.cos(th) * rr;
    haPos[i * 3 + 1] = gauss() * (140 + Math.random() * 380) * WS * (0.6 + 0.7 * rr / GALAXY_R1)
      + diskWarp(rr, th) * 0.6;
    haPos[i * 3 + 2] = Math.sin(th) * rr;
    tmpC.copy(cOuterRed).lerp(cBlue, Math.random() * 0.45);
    const b = (0.1 + Math.random() * 0.14) * GB;
    haCol[i * 3] = tmpC.r * b; haCol[i * 3 + 1] = tmpC.g * b; haCol[i * 3 + 2] = tmpC.b * b;
    haSiz[i] = 2.2 + Math.random() * 2.2;
  }
  galaxyGroup.add(makePointObject(haPos, haCol, haSiz, starTexture, 0.6).object);

  // (5) 潮汐尾：两条从盘缘甩出去的稀疏星流，进一步打散规则轮廓
  const TAIL_N = 4200;
  const taPos = new Float32Array(TAIL_N * 3);
  const taCol = new Float32Array(TAIL_N * 3);
  const taSiz = new Float32Array(TAIL_N);
  for (let i = 0; i < TAIL_N; i++) {
    const side = i % 2;
    const u = Math.pow(Math.random(), 0.85);
    const ang = (side === 0 ? 0.35 : 0.35 + Math.PI) + u * (side === 0 ? 0.95 : 0.75);
    const rr = GALAXY_R1 * (0.98 + u * 1.2) * (1 + gauss() * 0.07);
    taPos[i * 3] = Math.cos(ang) * rr;
    taPos[i * 3 + 1] = gauss() * (90 + u * 340) * WS + diskWarp(rr, ang) * 0.7;
    taPos[i * 3 + 2] = Math.sin(ang) * rr;
    tmpC.copy(cBlue).lerp(cOuterRed, Math.random() * 0.5);
    const b = (0.1 + Math.random() * 0.12) * GB;
    taCol[i * 3] = tmpC.r * b; taCol[i * 3 + 1] = tmpC.g * b; taCol[i * 3 + 2] = tmpC.b * b;
    taSiz[i] = 2.2 + Math.random() * 2.2;
  }
  galaxyGroup.add(makePointObject(taPos, taCol, taSiz, starTexture, 0.5).object);

  // (6) 亮星：盘面撒少量偏亮的白星，撑起「点点繁星」的颗粒感
  const SPARK_N = 420;
  const spPos = new Float32Array(SPARK_N * 3);
  const spCol = new Float32Array(SPARK_N * 3);
  const spSiz = new Float32Array(SPARK_N);
  for (let i = 0; i < SPARK_N; i++) {
    const { r, th } = sampleField();
    const rr = Math.max(200 * WS, r);
    spPos[i * 3] = Math.cos(th) * rr;
    spPos[i * 3 + 1] = gauss() * 120 * WS + diskWarp(rr, th);
    spPos[i * 3 + 2] = Math.sin(th) * rr;
    tmpC.set(Math.random() > 0.4 ? 0xffffff : 0xffeccb);
    const b = 0.8 + Math.random() * 0.2;
    spCol[i * 3] = tmpC.r * b; spCol[i * 3 + 1] = tmpC.g * b; spCol[i * 3 + 2] = tmpC.b * b;
    spSiz[i] = 5 + Math.random() * 4;
  }
  galaxyGroup.add(makePointObject(spPos, spCol, spSiz, starTexture, 0.9).object);

  if (glowTexture) {
    // 银心亮核：紧凑的一小团暖光，只做核心的点睛，不用大面积柔晕
    const coreGlow = new THREE.Sprite(new THREE.SpriteMaterial({
      map: glowTexture,
      color: 0xffe9c4,
      transparent: true,
      opacity: 0.42,
      blending: THREE.AdditiveBlending,
      depthWrite: false,
      depthTest: false
    }));
    coreGlow.scale.setScalar(130 * WS);
    coreGlow.renderOrder = 0;
    galaxyGroup.add(coreGlow);

    // ---- 全星系柔光晕：极淡的一层，只用来弥合盘缘的颗粒感 ----
    const haloInner = new THREE.Sprite(new THREE.SpriteMaterial({
      map: glowTexture,
      color: 0xffd9a0,
      transparent: true,
      opacity: 0.016,
      blending: THREE.AdditiveBlending,
      depthWrite: false,
      depthTest: false
    }));
    haloInner.scale.setScalar(GALAXY_R1 * 1.35);
    haloInner.renderOrder = -2;
    galaxyGroup.add(haloInner);
  }

  // ---- 传承弧：只连「同一条旋臂上」的相邻朝代 ----
  // 朝代已分散到四条臂，若按索引 i→i+1 相连会横跨星系拉出锯齿，
  // 因此改为 i → i+ARM_COUNT（同臂的下一个朝代），弧线顺着旋臂走。
  const arcVerts: number[] = [];
  for (let i = 0; i + ARM_COUNT < coreLocal.length; i++) {
    const a = coreLocal[i];
    const b = coreLocal[i + ARM_COUNT];
    const mid = a.clone().add(b).multiplyScalar(0.5);
    mid.y += 150 * WS;
    // 轻微外凸，且凸起幅度与距离成正比，避免大跨度时弧度失真
    mid.multiplyScalar(1 + 48 * WS / Math.max(mid.length(), 1));
    const curve = new THREE.QuadraticBezierCurve3(a.clone(), mid, b.clone());
    const pts = curve.getPoints(32);
    for (let s = 0; s < pts.length - 1; s++) {
      arcVerts.push(pts[s].x, pts[s].y, pts[s].z, pts[s + 1].x, pts[s + 1].y, pts[s + 1].z);
    }
  }
  const arcGeo = new THREE.BufferGeometry();
  arcGeo.setAttribute('position', new THREE.Float32BufferAttribute(arcVerts, 3));
  successionLines = new THREE.LineSegments(
    arcGeo,
    new THREE.LineBasicMaterial({ color: 0xffdca8, transparent: true, opacity: 0.1, blending: THREE.AdditiveBlending, depthWrite: false })
  );
  successionLines.visible = showSuccession.value;
  galaxyGroup.add(successionLines);

  // ---- 归属连线：星点 → 朝代核星，外凸弧线 + 尾亮头暗 ----
  // 美术收敛：连线只是"气息"，不是主体。数量多（事件+人物可达上百条），
  // 因此整体压到极低透明度，只在星点被聚焦/悬浮时才提亮（见 highlightAffiliation）。
  const affVerts: number[] = [];
  const affCols: number[] = [];
  const ac = new THREE.Color();
  const upAxis = new THREE.Vector3(0, 1, 0);
  const pushArc = (a: THREE.Vector3, b: THREE.Vector3, color: string, segs: number, head: number) => {
    const dir = new THREE.Vector3().subVectors(b, a);
    const mid = a.clone().add(b).multiplyScalar(0.5);
    const perp = new THREE.Vector3().crossVectors(dir, upAxis);
    if (perp.lengthSq() < 1e-4) perp.set(1, 0, 0);
    perp.normalize().multiplyScalar(Math.min(320 * WS, dir.length() * 0.6));
    mid.add(perp);
    const curve = new THREE.QuadraticBezierCurve3(a.clone(), mid, b.clone());
    const pts = curve.getPoints(segs);
    ac.set(color);
    for (let s = 0; s < pts.length - 1; s++) {
      // 头部（靠星点端）最亮，向核星端衰减到几乎不可见
      const f0 = Math.pow(s / segs, 2.6) * head;
      const f1 = Math.pow((s + 1) / segs, 2.6) * head;
      affVerts.push(pts[s].x, pts[s].y, pts[s].z, pts[s + 1].x, pts[s + 1].y, pts[s + 1].z);
      affCols.push(ac.r * f0, ac.g * f0, ac.b * f0, ac.r * f1, ac.g * f1, ac.b * f1);
    }
  };
  const pushAff = (nd: CloudNode) => {
    const ci = dynasties.value.findIndex(d => d.id === nd.dynastyId);
    if (ci < 0 || !coreLocal[ci]) return;
    pushArc(coreLocal[ci], nd.pos, nd.color, 10, 0.85);
  };
  eventNodes.value.forEach(pushAff);
  personNodes.value.forEach(pushAff);
  const affGeo = new THREE.BufferGeometry();
  affGeo.setAttribute('position', new THREE.Float32BufferAttribute(affVerts, 3));
  affGeo.setAttribute('color', new THREE.Float32BufferAttribute(affCols, 3));
  affiliationLines = new THREE.LineSegments(
    affGeo,
    new THREE.LineBasicMaterial({
      vertexColors: true,
      transparent: true,
      opacity: 0.10,
      blending: THREE.AdditiveBlending,
      depthWrite: false
    })
  );
  affiliationLines.visible = showAffiliation.value;
  galaxyGroup.add(affiliationLines);

  // ---- 流星雨：贯穿星云的长弧，沿弧线有流星头在飞行 ----
  const metVerts: number[] = [];
  const metCols: number[] = [];
  const metT: number[] = [];
  const metSeed: number[] = [];
  const mcol = new THREE.Color();
  for (let i = 0; i < 55; i++) {
    const a0 = Math.random() * Math.PI * 2;
    const a1 = a0 + (0.55 + Math.random() * 1.1) * (Math.random() > 0.5 ? 1 : -1);
    const r0 = (420 + Math.random() * 2600) * WS;
    const r1 = (420 + Math.random() * 2600) * WS;
    const p0 = new THREE.Vector3(Math.cos(a0) * r0, (Math.random() * 2 - 1) * 950 * WS, Math.sin(a0) * r0);
    const p1 = new THREE.Vector3(Math.cos(a1) * r1, (Math.random() * 2 - 1) * 950 * WS, Math.sin(a1) * r1);
    if (p0.distanceTo(p1) < 850 * WS) continue;
    const mid = p0.clone().add(p1).multiplyScalar(0.5);
    mid.y += (420 + Math.random() * 950) * WS;
    const curve = new THREE.QuadraticBezierCurve3(p0, mid, p1);
    const segs = 30;
    const pts = curve.getPoints(segs);
    mcol.set(Math.random() > 0.72 ? 0xffd2a0 : 0xbcd6ff);
    const seed = Math.random();
    for (let s = 0; s < pts.length - 1; s++) {
      metVerts.push(pts[s].x, pts[s].y, pts[s].z, pts[s + 1].x, pts[s + 1].y, pts[s + 1].z);
      metCols.push(mcol.r, mcol.g, mcol.b, mcol.r, mcol.g, mcol.b);
      metT.push(s / segs, (s + 1) / segs);
      metSeed.push(seed, seed);
    }
  }
  const metGeo = new THREE.BufferGeometry();
  metGeo.setAttribute('position', new THREE.Float32BufferAttribute(metVerts, 3));
  metGeo.setAttribute('color', new THREE.Float32BufferAttribute(metCols, 3));
  metGeo.setAttribute('aT', new THREE.Float32BufferAttribute(metT, 1));
  metGeo.setAttribute('aSeed', new THREE.Float32BufferAttribute(metSeed, 1));
  const metMat = new THREE.ShaderMaterial({
    uniforms: {
      uTime: { value: 0 },
      uSpeed: { value: 0.22 }
    },
    vertexShader: `
      attribute vec3 color;
      attribute float aT;
      attribute float aSeed;
      varying vec3 vColor;
      varying float vT;
      varying float vSeed;
      void main() {
        vColor = color;
        vT = aT;
        vSeed = aSeed;
        gl_Position = projectionMatrix * modelViewMatrix * vec4(position, 1.0);
      }
    `,
    fragmentShader: `
      uniform float uTime;
      uniform float uSpeed;
      varying vec3 vColor;
      varying float vT;
      varying float vSeed;
      void main() {
        float h = fract(uTime * uSpeed + vSeed);
        float d = h - vT;
        float tail = d >= 0.0 ? pow(1.0 - clamp(d / 0.42, 0.0, 1.0), 2.6) : 0.0;
        float head = 1.0 - clamp(abs(d) / 0.015, 0.0, 1.0);
        gl_FragColor = vec4(vColor * (tail * 0.2 + head * 0.36), 1.0);
      }
    `,
    transparent: true,
    blending: THREE.AdditiveBlending,
    depthWrite: false
  });
  timeMaterials.push(metMat);
  galaxyGroup.add(new THREE.LineSegments(metGeo, metMat));

  applyFilters();
  loading.value = false;
}

function buildBackdrop() {
  if (!scene || !starTexture || !glowTexture) return;
  const sc: THREE.Scene = scene;

  // ---- 天球渐变（深夜蓝 → 靛青 → 暖金地平辉光，呼应登录页的水墨星云）----
  const bgMat = new THREE.ShaderMaterial({
    uniforms: {
      uDeep: { value: new THREE.Color(0x02040c) },
      uMid: { value: new THREE.Color(0x071026) },
      uHigh: { value: new THREE.Color(0x0d1130) },
      uWarm: { value: new THREE.Color(0x2a1f14) }
    },
    vertexShader: `
      varying vec3 vDir;
      void main() {
        vDir = normalize(position);
        gl_Position = projectionMatrix * modelViewMatrix * vec4(position, 1.0);
      }
    `,
    fragmentShader: `
      uniform vec3 uDeep;
      uniform vec3 uMid;
      uniform vec3 uHigh;
      uniform vec3 uWarm;
      varying vec3 vDir;
      void main() {
        vec3 d = normalize(vDir);
        float h = d.y * 0.5 + 0.5;
        // 垂直分层：底部最深，向上过渡到靛蓝
        vec3 col = mix(uDeep, uMid, smoothstep(0.0, 0.72, h));
        col = mix(col, uHigh, smoothstep(0.55, 1.0, h) * 0.55);
        // 地平线附近的暖色辉光（金色，与 HUD 呼应），只在低角度出现，避免整体发黄
        float warm = smoothstep(0.34, 0.0, h) * 0.55;
        col = mix(col, uWarm, warm);
        // 极轻微的抖动，消除大面积渐变的色带
        float dither = fract(sin(dot(d.xy, vec2(12.9898, 78.233))) * 43758.5453) * 0.004;
        gl_FragColor = vec4(col + dither, 1.0);
      }
    `,
    side: THREE.BackSide,
    depthWrite: false,
    depthTest: false
  });
  bgMesh = new THREE.Mesh(new THREE.SphereGeometry(16000 * WS, 32, 16), bgMat);
  bgMesh.frustumCulled = false;
  bgMesh.renderOrder = -20;
  scene.add(bgMesh);

  // ---- 远景星场：颗粒小、亮度低，只做底噪 ----
  const N = 5200;
  const pos = new Float32Array(N * 3);
  const col = new Float32Array(N * 3);
  const siz = new Float32Array(N);
  const palette = [0x9fb8ff, 0xffffff, 0xdce9ff, 0xffe6c0, 0xc9d6ff, 0xffd0a0];
  const pc = new THREE.Color();
  for (let i = 0; i < N; i++) {
    const dir = fibSphere(i, N, Math.random() * 6.28);
    const r = (9500 + Math.random() * 3600) * WS;
    pos[i * 3] = dir.x * r;
    pos[i * 3 + 1] = dir.y * r;
    pos[i * 3 + 2] = dir.z * r;
    pc.set(palette[(Math.random() * palette.length) | 0]);
    const b = 0.4 + Math.random() * 0.8;
    col[i * 3] = pc.r * b; col[i * 3 + 1] = pc.g * b; col[i * 3 + 2] = pc.b * b;
    siz[i] = 14 + Math.random() * 26;
  }
  scene.add(makePointObject(pos, col, siz, starTexture, 0.45).object);

  // ---- 银河带：沿倾斜平面的密集星点 ----
  const BN = 1000;
  const bpos = new Float32Array(BN * 3);
  const bcol = new Float32Array(BN * 3);
  const bsiz = new Float32Array(BN);
  const normal = new THREE.Vector3(0.32, 0.86, -0.4).normalize();
  const bc = new THREE.Color();
  for (let i = 0; i < BN; i++) {
    const dir = fibSphere(i, BN, 2.7);
    dir.sub(normal.clone().multiplyScalar(dir.dot(normal) * 0.74)).normalize();
    const r = (9800 + Math.random() * 3400) * WS;
    bpos[i * 3] = dir.x * r;
    bpos[i * 3 + 1] = dir.y * r;
    bpos[i * 3 + 2] = dir.z * r;
    bc.set(Math.random() > 0.5 ? 0xcfe0ff : 0xfff0d8);
    const b = 0.25 + Math.random() * 0.5;
    bcol[i * 3] = bc.r * b; bcol[i * 3 + 1] = bc.g * b; bcol[i * 3 + 2] = bc.b * b;
    bsiz[i] = 14 + Math.random() * 24;
  }
  scene.add(makePointObject(bpos, bcol, bsiz, starTexture, 0.35).object);

  // ---- 星云：只留三团极淡的冷色雾，不再压过前景 ----
  nebulaTexture = makeNebulaTexture();
  const nebCfg = [
    { c: 0x3f5bd0, s: 10000, o: 0.035 },
    { c: 0x246a8c, s: 7800, o: 0.03 },
    { c: 0x5b3f9c, s: 8800, o: 0.035 }
  ];
  nebCfg.forEach((cfg, i) => {
    const spr = new THREE.Sprite(new THREE.SpriteMaterial({
      map: nebulaTexture, color: cfg.c, transparent: true, opacity: cfg.o,
      blending: THREE.AdditiveBlending, depthWrite: false, depthTest: false
    }));
    const dir = fibSphere(i, nebCfg.length, 0.9);
    spr.position.copy(dir.multiplyScalar((11200 + i * 600) * WS));
    spr.scale.setScalar(cfg.s * WS);
    spr.renderOrder = -10;
    sc.add(spr);
  });

  // ---- 星系中心极淡的柔光 ----
  const glows: { c: number; s: number; o: number }[] = [
    { c: 0x5f7fff, s: 1500, o: 0.032 },
    { c: 0xffd9a0, s: 1000, o: 0.03 }
  ];
  glows.forEach(g => {
    const spr = new THREE.Sprite(new THREE.SpriteMaterial({
      map: glowTexture, color: g.c, transparent: true, opacity: g.o,
      blending: THREE.AdditiveBlending, depthWrite: false, depthTest: false
    }));
    spr.scale.setScalar(g.s * WS);
    sc.add(spr);
  });
}

/* ========================= 过滤 / 图层 ========================= */

function applyFilters() {
  for (const pack of packs) {
    const isAllusion = pack.nodes[0]?.kind === 'allusion';
    pack.nodes.forEach((nd, i) => {
      let s = nd.baseSize;
      if (activeDynastyId.value !== null) {
        if (nd.dynastyId === activeDynastyId.value) s *= 1.35;
        else if (isAllusion) s *= 0.22;
        else s *= 0.16;
      }
      if (selectedNode && selectedNode === nd) s *= 1.9;
      pack.sizes[i] = s;
    });
    pack.sizeAttr.needsUpdate = true;
  }
  const cp = coresPack;
  if (cp) {
    cp.nodes.forEach((nd, i) => {
      let s = nd.baseSize;
      if (activeDynastyId.value !== null && nd.dynastyId !== activeDynastyId.value) s *= 0.35;
      cp.sizes[i] = s;
    });
    cp.sizeAttr.needsUpdate = true;
  }
  // 核星光晕：聚焦时高亮当前朝代
  coreHalos.forEach((halo, i) => {
    if (activeDynastyId.value === null) halo.material.opacity = 0.12;
    else if (cores[i]?.dynastyId === activeDynastyId.value) halo.material.opacity = 0.3;
    else halo.material.opacity = 0.03;
  });
}

function applyLayers() {
  packs.forEach(p => {
    const k = p.nodes[0]?.kind;
    if (k === 'event') p.object.visible = showEvents.value;
    if (k === 'person') p.object.visible = showPersons.value;
    if (k === 'allusion') p.object.visible = showAllusions.value;
  });
  if (dustPoints) dustPoints.visible = showShells.value;
  if (structureLines) structureLines.visible = showShells.value;
  if (successionLines) successionLines.visible = showSuccession.value;
  if (affiliationLines) affiliationLines.visible = showAffiliation.value;
  applyFilters();
}

function setDensity(v: number) {
  density.value = v;
  packs.forEach(p => { (p.material.uniforms.uSizeScale as any).value = v; });
  if (coresPack) (coresPack.material.uniforms.uSizeScale as any).value = v;
  if (dustPoints) ((dustPoints.material as THREE.ShaderMaterial).uniforms.uSizeScale as any).value = v * 0.85;
}

/* ========================= 相机与交互 ========================= */

function applyCamRotation() {
  if (!camera) return;
  camera.quaternion.setFromEuler(new THREE.Euler(pitch, yaw, 0, 'YXZ'));
}

function setCamFromLook(pos: THREE.Vector3, look: THREE.Vector3) {
  if (!camera) return;
  camera.position.copy(pos);
  const d = look.clone().sub(pos).normalize();
  pitch = Math.asin(THREE.MathUtils.clamp(d.y, -1, 1));
  yaw = Math.atan2(-d.x, -d.z);
  applyCamRotation();
}

function currentLook(): THREE.Vector3 {
  if (!camera) return new THREE.Vector3();
  return camera.position.clone().add(new THREE.Vector3(0, 0, -1).applyQuaternion(camera.quaternion).multiplyScalar(2500 * WS));
}

const DEFAULT_POS = new THREE.Vector3(0, 2600 * WS, 2050 * WS);

function startFly(to: THREE.Vector3, lookAt: THREE.Vector3, dur = 1.1) {
  if (!camera) return;
  fly = { from: camera.position.clone(), to: to.clone(), lookFrom: currentLook(), lookTo: lookAt.clone(), t: 0, dur };
  lastInteraction = performance.now();
}

function resetView() {
  activeDynastyId.value = null;
  selectedNode = null;
  selected.value = null;
  applyFilters();
  fly = null;
  setCamFromLook(DEFAULT_POS.clone(), new THREE.Vector3(0, 0, 0));
}

function toggleAutoRotate() {
  autoRotate.value = !autoRotate.value;
  if (!autoRotate.value && galaxyGroup) galaxyGroup.rotation.y = galaxyGroup.rotation.y % (Math.PI * 2);
}

function focusDynasty(d: any, i: number) {
  if (activeDynastyId.value === d.id) {
    activeDynastyId.value = null;
    applyFilters();
    return;
  }
  activeDynastyId.value = d.id;
  applyFilters();

  const core = coreLocal[i]?.clone();
  if (!core) return;
  if (galaxyGroup) core.applyMatrix4(galaxyGroup.matrixWorld);
  const dir = core.clone().normalize();
  const camPos = core.clone().add(dir.multiplyScalar(920 * WS)).add(new THREE.Vector3(0, 340 * WS, 0));
  startFly(camPos, core);
}

/* ---- 拾取 ---- */

function nearestOnScreen(cx: number, cy: number): { node: CloudNode | null; dist: number; total: number } {
  if (!camera || !stageRef.value) return { node: null, dist: Infinity, total: 0 };
  const rect = stageRef.value.getBoundingClientRect();
  const px = cx - rect.left;
  const py = cy - rect.top;
  const w = rect.width;
  const h = rect.height;

  const all: { node: CloudNode; obj: THREE.Points }[] = [];
  if (coresPack && coresPack.object.visible) coresPack.nodes.forEach(n => all.push({ node: n, obj: coresPack!.object }));
  packs.forEach(p => { if (p.object.visible) p.nodes.forEach(n => all.push({ node: n, obj: p.object })); });

  let best: CloudNode | null = null;
  let bestD = Infinity;
  const camSpace = new THREE.Vector3();
  const proj = new THREE.Vector3();
  for (const item of all) {
    camSpace.copy(item.node.pos).applyMatrix4(item.obj.matrixWorld).applyMatrix4(camera.matrixWorldInverse);
    if (camSpace.z > -1) continue; // 位于相机后方
    proj.copy(camSpace).applyMatrix4(camera.projectionMatrix);
    const sx = (proj.x * 0.5 + 0.5) * w;
    const sy = (-proj.y * 0.5 + 0.5) * h;
    const dd = Math.hypot(sx - px, sy - py);
    if (dd < bestD) { bestD = dd; best = item.node; }
  }
  return { node: best, dist: bestD, total: all.length };
}

function pickNode(cx: number, cy: number, maxPx: number): CloudNode | null {
  const r = nearestOnScreen(cx, cy);
  return r.node && r.dist <= maxPx ? r.node : null;
}

function kindLabel(k: Kind): string {
  if (k === 'event') return '历史事件';
  if (k === 'person') return '历史人物';
  if (k === 'allusion') return '典故';
  return '朝代';
}

function updateHover(cx: number, cy: number) {
  const hit = pickNode(cx, cy, 20);
  if (!hit) {
    if (hoverTip.value) hoverTip.value = null;
    return;
  }
  if (hoverTip.value && hoverTip.value.name === hit.name) return;
  const rect = stageRef.value!.getBoundingClientRect();
  hoverTip.value = {
    x: cx - rect.left + 14,
    y: cy - rect.top + 14,
    name: hit.name,
    kindLabel: kindLabel(hit.kind),
    color: hit.color
  };
}

/* ---- 选中与详情 ---- */

function buildDetail(nd: CloudNode): DetailState {
  const base = { kind: nd.kind, kindLabel: kindLabel(nd.kind), color: nd.color };
  if (nd.kind === 'event') {
    const e = nd.raw;
    const meta = [fmtYear(nd.year), `${nd.dynastyName}朝`, TYPE_META[normEventType(e.eventType)].label];
    const fields: { label: string; value: string }[] = [];
    if (e.eventBg) fields.push({ label: '背景', value: e.eventBg });
    if (e.eventProcess) fields.push({ label: '过程', value: e.eventProcess });
    if (e.eventImpact) fields.push({ label: '影响', value: e.eventImpact });
    return { ...base, name: nd.name, image: resolveAssetUrl(e.eventPicPath), meta, fields, pagePath: '/event', pageLabel: '事件科普' };
  }
  if (nd.kind === 'person') {
    const p = nd.raw;
    const life = `${fmtYear(p.birthYear).replace('公元 ', '').replace('公元前 ', '前')} - ${fmtYear(p.deathYear).replace('公元 ', '').replace('公元前 ', '前')}`;
    const meta = [life, `${nd.dynastyName}朝`, p.identityType || '其他'];
    const fields: { label: string; value: string }[] = [];
    if (p.personBio) fields.push({ label: '生平', value: p.personBio });
    if (Array.isArray(p.tags) && p.tags.length) fields.push({ label: '标签', value: p.tags.join(' · ') });
    return { ...base, name: nd.name, image: resolveAssetUrl(p.avatarPath), meta, fields, pagePath: '/person', pageLabel: '人物科普' };
  }
  if (nd.kind === 'allusion') {
    const a = nd.raw;
    const meta = [a.source || '出处不详', a.themeType || '其他'];
    const fields: { label: string; value: string }[] = [];
    if (a.originalText) fields.push({ label: '原文', value: a.originalText });
    if (a.paraphrase) fields.push({ label: '释义', value: a.paraphrase });
    if (a.moral) fields.push({ label: '寓意', value: a.moral });
    return { ...base, name: nd.name, image: resolveAssetUrl(a.multimediaPath), meta, fields, pagePath: '/allusion', pageLabel: '典故科普' };
  }
  // core
  const d = nd.raw;
  const meta = [`${fmtYear(d.startYear)} ~ ${fmtYear(d.endYear)}`, `都城：${d.capital || '—'}`, `建立者：${d.founder || '—'}`];
  const fields: { label: string; value: string }[] = [];
  if (d.dynastyDesc) fields.push({ label: '简介', value: d.dynastyDesc });
  return { ...base, name: nd.name, image: resolveAssetUrl(d.mapPath), meta, fields, pagePath: '/timeline', pageLabel: '时间轴' };
}

function selectNode(nd: CloudNode | null) {
  selectedNode = nd;
  selected.value = nd ? buildDetail(nd) : null;
  hoverTip.value = null;
  applyFilters();
}

function closeDetail() {
  selectNode(null);
}

function onImgError(e: Event) {
  const el = e.target as HTMLImageElement;
  el.style.display = 'none';
}

function goDetailPage() {
  if (selected.value) router.push(selected.value.pagePath);
}

/* ---- 指针事件 ---- */

function pinchDist(): number {
  const arr = Array.from(pointers.values());
  if (arr.length < 2) return 0;
  return Math.hypot(arr[0].x - arr[1].x, arr[0].y - arr[1].y);
}

function onPointerDown(e: PointerEvent) {
  try {
    (e.target as HTMLElement).setPointerCapture?.(e.pointerId);
  } catch {
    /* 合成事件或指针未激活时忽略 */
  }
  pointers.set(e.pointerId, { x: e.clientX, y: e.clientY });
  if (pointers.size === 1) {
    downInfo = { x: e.clientX, y: e.clientY, t: performance.now() };
    dragDist = 0;
  } else if (pointers.size === 2) {
    pinchPrev = pinchDist();
    downInfo = null;
  }
  lastInteraction = performance.now();
}

function onPointerMove(e: PointerEvent) {
  const rec = pointers.get(e.pointerId);
  if (!rec) {
    if (e.pointerType !== 'touch') updateHover(e.clientX, e.clientY);
    return;
  }
  const dx = e.clientX - rec.x;
  const dy = e.clientY - rec.y;
  rec.x = e.clientX;
  rec.y = e.clientY;

  if (pointers.size === 1 && downInfo) {
    dragDist += Math.abs(dx) + Math.abs(dy);
    yaw -= dx * 0.0026;
    pitch = THREE.MathUtils.clamp(pitch - dy * 0.0026, -1.45, 1.45);
    applyCamRotation();
    lastInteraction = performance.now();
  } else if (pointers.size === 2) {
    const d = pinchDist();
    if (pinchPrev > 0 && camera) {
      const delta = d - pinchPrev;
      const f = new THREE.Vector3(0, 0, -1).applyQuaternion(camera.quaternion);
      camera.position.addScaledVector(f, delta * 8 * WS);
      lastInteraction = performance.now();
      if (fly) fly = null;
    }
    pinchPrev = d;
  }
}

function onPointerUp(e: PointerEvent) {
  pointers.delete(e.pointerId);
  if (pointers.size === 0) {
    if (dragDist > 8) dragEndTime = performance.now();
    downInfo = null;
  } else if (pointers.size === 1) {
    downInfo = { x: e.clientX, y: e.clientY, t: performance.now() };
    dragDist = 0;
  }
  pinchPrev = pointers.size === 2 ? pinchDist() : 0;
}

/** 点击拾取：用原生 click 保证鼠标点击与自动化点击都能命中 */
function onCanvasClick(e: MouseEvent) {
  if (pointers.size > 0) return;
  if (dragDist > 8 && performance.now() - dragEndTime < 350) return;
  const hit = pickNode(e.clientX, e.clientY, 26);
  selectNode(hit);
  if (!hit) hoverTip.value = null;
  dragDist = 0;
}

function onWheel(e: WheelEvent) {
  e.preventDefault();
  if (!camera) return;
  const f = new THREE.Vector3(0, 0, -1).applyQuaternion(camera.quaternion);
  camera.position.addScaledVector(f, -e.deltaY * 3.0 * WS);
  lastInteraction = performance.now();
  if (fly) fly = null;
}

function onKeyDown(e: KeyboardEvent) {
  const tag = (e.target as HTMLElement)?.tagName;
  if (tag === 'INPUT' || tag === 'TEXTAREA') return;
  const k = e.key.toLowerCase();
  if (['w', 'a', 's', 'd', 'q', 'e', ' ', 'shift'].includes(k)) {
    keys.add(k === ' ' ? 'space' : k);
    e.preventDefault();
    lastInteraction = performance.now();
  }
}

function onKeyUp(e: KeyboardEvent) {
  const k = e.key.toLowerCase();
  keys.delete(k === ' ' ? 'space' : k);
}

function onBlur() {
  keys.clear();
}

function onPointerLeave() {
  hoverTip.value = null;
}

/* ========================= 渲染循环 ========================= */

function updateLabels() {
  if (!coresPack || !camera || !stageRef.value) return;
  if (!labelEls.length) {
    const found = stageRef.value.querySelectorAll('.hc-label');
    if (!found.length) return;
    labelEls = Array.from(found) as HTMLElement[];
  }
  const rect = stageRef.value.getBoundingClientRect();
  const w = rect.width;
  const h = rect.height;
  const camSpace = new THREE.Vector3();
  const proj = new THREE.Vector3();

  coreLabelPos.length = 0;
  labelEls.forEach((el, i) => {
    const nd = cores[i];
    if (!nd) return;
    camSpace.copy(nd.pos).applyMatrix4(coresPack!.object.matrixWorld).applyMatrix4(camera!.matrixWorldInverse);
    if (camSpace.z > -1) { el.style.opacity = '0'; el.style.pointerEvents = 'none'; return; }
    proj.copy(camSpace).applyMatrix4(camera!.projectionMatrix);
    const sx = (proj.x * 0.5 + 0.5) * w;
    const sy = (-proj.y * 0.5 + 0.5) * h;
    if (sx < -80 || sx > w + 80 || sy < -40 || sy > h + 40) {
      el.style.opacity = '0';
      el.style.pointerEvents = 'none';
      return;
    }
    const dim = activeDynastyId.value !== null && activeDynastyId.value !== nd.dynastyId;
    el.style.opacity = dim ? '0.28' : '1';
    el.style.pointerEvents = 'auto';
    el.style.transform = `translate(-50%, -50%) translate(${sx}px, ${sy - 30}px)`;
    coreLabelPos.push({ x: sx, y: sy - 30, len: (nd.name || '').length });
  });
}

/** 星点名称标签：按重要度挑选、网格避让、随距离淡出 */
function updateStarLabels() {
  if (!camera || !stageRef.value || !starLabels.length) return;
  const rect = stageRef.value.getBoundingClientRect();
  const w = rect.width;
  const h = rect.height;
  if (w < 2 || h < 2) return;

  const cands: { nd: CloudNode; sx: number; sy: number; score: number }[] = [];
  const camSpace = new THREE.Vector3();
  const proj = new THREE.Vector3();
  const active = activeDynastyId.value;

  for (const pack of packs) {
    if (!pack.object.visible) continue;
    const mw = pack.object.matrixWorld;
    pack.nodes.forEach((nd, idx) => {
      if (pack.sizes[idx] < 1) return;
      camSpace.copy(nd.pos).applyMatrix4(mw).applyMatrix4(camera!.matrixWorldInverse);
      const z = -camSpace.z;
      if (z <= 1) return;
      proj.copy(camSpace).applyMatrix4(camera!.projectionMatrix);
      const sx = (proj.x * 0.5 + 0.5) * w;
      const sy = (-proj.y * 0.5 + 0.5) * h;
      if (sx < 40 || sx > w - 40 || sy < 40 || sy > h - 64) return;
      if (sx < 196 && sy > 60) return;
      if (nd.kind !== 'allusion' && active !== null && nd.dynastyId !== active) return;
      const cd = Math.hypot(sx - w / 2, sy - h / 2) / Math.max(w, h);
      let score = 1.3 - cd * 2 + (700 * WS / Math.max(z, 700 * WS)) * 0.6;
      if (active !== null && nd.dynastyId === active) score += 1.2;
      if (selectedNode === nd) score += 2;
      cands.push({ nd, sx, sy, score });
    });
  }
  cands.sort((a, b) => b.score - a.score);

  // 避让栅格：单元格尺寸需容纳一个 4 字中文标签（12px × 4 ≈ 52px 宽，18px 高），
  // 原来只登记锚点所在的单格，长名字会横向溢出压到邻居，故按标签实际宽度占多格。
  const CELL_W = 58;
  const CELL_H = 20;
  const cells = new Set<string>();
  const occupy = (x: number, y: number, halfWidthPx: number) => {
    const span = Math.max(1, Math.ceil(halfWidthPx / CELL_W));
    const cx = Math.round(x / CELL_W);
    const cy = Math.round(y / CELL_H);
    for (let dx = -span; dx <= span; dx++) {
      for (let dy = 0; dy <= 1; dy++) {
        cells.add(`${cx + dx}:${cy + dy}`);
      }
    }
  };
  // 朝代核星标签优先级最高，先占位（宽度按名称长度估算）
  coreLabelPos.forEach(p => {
    occupy(p.x, p.y, Math.max(26, (p.len || 2) * 7));
  });

  let used = 0;
  for (const c of cands) {
    if (used >= starLabels.length) break;
    const halfW = Math.max(26, c.nd.name.length * 7);
    const cx = Math.round(c.sx / CELL_W);
    const cy = Math.round(c.sy / CELL_H);
    let blocked = false;
    const span = Math.max(1, Math.ceil(halfW / CELL_W));
    for (let dx = -span; dx <= span && !blocked; dx++) {
      for (let dy = 0; dy <= 1 && !blocked; dy++) {
        if (cells.has(`${cx + dx}:${cy + dy}`)) blocked = true;
      }
    }
    if (blocked) continue;

    occupy(c.sx, c.sy, halfW);
    const el = starLabels[used];
    const tag = `${c.nd.kind}${c.nd.id}`;
    if (el.dataset.tag !== tag) {
      el.textContent = c.nd.name;
      el.dataset.tag = tag;
    }
    el.style.color = c.nd.color;
    el.style.transform = `translate(-50%, 0) translate(${c.sx}px, ${c.sy + 9}px)`;
    // 权重分层更明显：非重点标签压到很淡，让主体（核星/选中）凸显
    el.style.opacity = String(c.score > 2.2 ? 0.95 : c.score > 1.5 ? 0.72 : c.score > 1 ? 0.46 : 0.26);
    used++;
  }
  for (let i = used; i < starLabels.length; i++) starLabels[i].style.opacity = '0';
}

function initStarLabels() {
  if (!starLabelRef.value) return;
  starLabelRef.value.replaceChildren();
  starLabels = [];
  for (let i = 0; i < STAR_LABEL_MAX; i++) {
    const el = document.createElement('div');
    el.className = 'hc-star-label';
    el.style.cssText =
      'position:absolute;top:0;left:0;font-size:12px;font-weight:500;letter-spacing:0.5px;' +
      'white-space:nowrap;opacity:0;will-change:transform,opacity;pointer-events:none;' +
      'text-shadow:0 0 10px rgba(0,0,0,1),0 1px 3px rgba(0,0,0,1);transition:opacity 0.3s ease;';
    starLabelRef.value.appendChild(el);
    starLabels.push(el);
  }
}

function tick() {
  rafId = requestAnimationFrame(tick);
  if (!renderer || !scene || !camera) return;
  const dt = Math.min(clock.getDelta(), 0.05);
  const now = performance.now();

  // 移动
  const f = new THREE.Vector3(0, 0, -1).applyQuaternion(camera.quaternion);
  const r = new THREE.Vector3(1, 0, 0).applyQuaternion(camera.quaternion);
  const mv = new THREE.Vector3();
  if (keys.has('w')) mv.add(f);
  if (keys.has('s')) mv.sub(f);
  if (keys.has('d')) mv.add(r);
  if (keys.has('a')) mv.sub(r);
  if (keys.has('q') || keys.has('space')) mv.y += 1;
  if (keys.has('e')) mv.y -= 1;
  if (mv.lengthSq() > 0) {
    // 巡航基础速度放慢；按住 Shift 加速到 3.4 倍，长距离移动依然干脆
    const speed = 600 * WS * (keys.has('shift') ? 3.4 : 1);
    mv.normalize().multiplyScalar(speed * dt);
    camera.position.add(mv);
    lastInteraction = now;
  }

  // 边界：坐标空间收敛到星系尺度，避免推到远处只剩一个小点
  const dist = camera.position.length();
  if (dist > CAM_MAX_DIST) camera.position.setLength(CAM_MAX_DIST);
  if (dist < CAM_MIN_DIST) camera.position.setLength(CAM_MIN_DIST);

  // 相机飞行
  if (fly) {
    fly.t += dt / fly.dur;
    const k = fly.t >= 1 ? 1 : easeInOut(fly.t);
    const pos = fly.from.clone().lerp(fly.to, k);
    const look = fly.lookFrom.clone().lerp(fly.lookTo, k);
    setCamFromLook(pos, look);
    if (fly.t >= 1) fly = null;
  }

  // 自动旋转（空闲时）
  if (galaxyGroup && autoRotate.value && !fly && now - lastInteraction > 3500) {
    galaxyGroup.rotation.y += 0.032 * dt;
  }

  // 天球跟随相机，保持夜空稳定
  if (bgMesh && camera) bgMesh.position.copy(camera.position);

  // 星点闪烁时间
  const t = clock.elapsedTime;
  for (let i = 0; i < timeMaterials.length; i++) {
    (timeMaterials[i].uniforms.uTime as any).value = t;
  }

  updateLabels();
  if (++labelTick % 3 === 0) updateStarLabels();
  if (composer) composer.render();
  else renderer.render(scene, camera);

  // 自适应画质
  fpsFrames++;
  fpsTime += dt;
  if (fpsTime >= 1.5) {
    const fps = fpsFrames / fpsTime;
    if (fps < 45 && !qualityDropped && renderer.getPixelRatio() > 1) {
      renderer.setPixelRatio(1);
      qualityDropped = true;
      onResize();
    }
    fpsFrames = 0;
    fpsTime = 0;
  }
}

function easeInOut(t: number): number {
  return t < 0.5 ? 2 * t * t : 1 - Math.pow(-2 * t + 2, 2) / 2;
}

/* ========================= 初始化 ========================= */

function onResize() {
  if (!renderer || !camera || !stageRef.value) return;
  const rect = stageRef.value.getBoundingClientRect();
  const w = Math.max(1, rect.width);
  const h = Math.max(1, rect.height);
  camera.aspect = w / h;
  camera.updateProjectionMatrix();
  renderer.setSize(w, h, false);
  if (composer) {
    composer.setPixelRatio(renderer.getPixelRatio());
    composer.setSize(w, h);
  }
  const pr = renderer.getPixelRatio();
  packs.forEach(p => { (p.material.uniforms.uPixelRatio as any).value = pr; });
  if (coresPack) (coresPack.material.uniforms.uPixelRatio as any).value = pr;
  if (dustPoints) ((dustPoints.material as THREE.ShaderMaterial).uniforms.uPixelRatio as any).value = pr;
}

// 星点密度滑杆联动
watch(density, v => setDensity(v));

onMounted(async () => {
  const canvas = canvasRef.value!;
  renderer = new THREE.WebGLRenderer({ canvas, antialias: true, alpha: false, powerPreference: 'high-performance' });
  renderer.setPixelRatio(Math.min(window.devicePixelRatio || 1, 2));
  renderer.setClearColor(0x010207, 1);
  renderer.toneMapping = THREE.ACESFilmicToneMapping;
  renderer.toneMappingExposure = 1.0;

  scene = new THREE.Scene();
  camera = new THREE.PerspectiveCamera(58, 1, 1 * WS, 24000 * WS);
  galaxyGroup = new THREE.Group();
  scene.add(galaxyGroup);

  // 后处理：泛光让星点、光晕、传承弧发光，整体质感提升
  composer = new EffectComposer(renderer);
  composer.setPixelRatio(renderer.getPixelRatio());
  composer.addPass(new RenderPass(scene, camera));
  // 泛光只作用在很亮的星点上：阈值抬高、半径收窄，避免整片星空被泡成模糊的光雾
  bloomPass = new UnrealBloomPass(new THREE.Vector2(1, 1), 0.16, 0.18, 0.78);
  composer.addPass(bloomPass);
  composer.addPass(new OutputPass());

  starTexture = makeStarTexture();
  coreTexture = makeCoreTexture();
  glowTexture = makeSoftGlowTexture();
  buildBackdrop();
  onResize();
  setCamFromLook(DEFAULT_POS.clone(), new THREE.Vector3(0, 0, 0));

  canvas.addEventListener('pointerdown', onPointerDown);
  canvas.addEventListener('pointermove', onPointerMove);
  canvas.addEventListener('pointerup', onPointerUp);
  canvas.addEventListener('pointercancel', onPointerUp);
  canvas.addEventListener('wheel', onWheel, { passive: false });
  canvas.addEventListener('click', onCanvasClick);
  canvas.addEventListener('pointerleave', onPointerLeave);
  window.addEventListener('resize', onResize);
  window.addEventListener('keydown', onKeyDown);
  window.addEventListener('keyup', onKeyUp);
  window.addEventListener('blur', onBlur);
  if (typeof ResizeObserver !== 'undefined') {
    resizeObserver = new ResizeObserver(onResize);
    if (stageRef.value) resizeObserver.observe(stageRef.value);
  }

  await loadData();
  buildGalaxy();
  initStarLabels();
  lastInteraction = performance.now();
  clock.getDelta();
  tick();
});

onBeforeUnmount(() => {
  cancelAnimationFrame(rafId);
  const canvas = canvasRef.value;
  if (canvas) {
    canvas.removeEventListener('pointerdown', onPointerDown);
    canvas.removeEventListener('pointermove', onPointerMove);
    canvas.removeEventListener('pointerup', onPointerUp);
    canvas.removeEventListener('pointercancel', onPointerUp);
    canvas.removeEventListener('wheel', onWheel);
    canvas.removeEventListener('click', onCanvasClick);
    canvas.removeEventListener('pointerleave', onPointerLeave);
  }
  window.removeEventListener('resize', onResize);
  window.removeEventListener('keydown', onKeyDown);
  window.removeEventListener('keyup', onKeyUp);
  window.removeEventListener('blur', onBlur);
  if (resizeObserver) { resizeObserver.disconnect(); resizeObserver = null; }

  scene?.traverse(obj => {
    const anyObj = obj as any;
    if (anyObj.geometry) anyObj.geometry.dispose?.();
    if (anyObj.material) {
      const mats = Array.isArray(anyObj.material) ? anyObj.material : [anyObj.material];
      mats.forEach((m: any) => m.dispose?.());
    }
  });
  starTexture?.dispose();
  coreTexture?.dispose();
  glowTexture?.dispose();
  nebulaTexture?.dispose();
  composer?.dispose();
  composer = null;
  bloomPass = null;
  renderer?.dispose();
  renderer = null;
  scene = null;
  camera = null;
  packs.length = 0;
  timeMaterials.length = 0;
  coreHalos.length = 0;
  starLabels = [];
  structureLines = null;
  bgMesh = null;
  labelEls = [];
});
</script>

<style scoped>
.hc-page {
  background: #04050c;
}

.hc-stage {
  position: relative;
  width: 100%;
  height: calc(100vh - 70px);
  overflow: hidden;
  /* 与 3D 天球渐变同色系，避免 WebGL 初始化瞬间闪黑边 */
  background: radial-gradient(ellipse at 50% 62%, #0b1330 0%, #060a18 42%, #02040c 78%);
  user-select: none;
}

.hc-canvas {
  position: absolute;
  inset: 0;
  width: 100%;
  height: 100%;
  display: block;
  cursor: grab;
  touch-action: none;
}

.hc-canvas:active {
  cursor: grabbing;
}

/* ---- 氛围叠层：暗角 + 上下渐变遮罩，提升 HUD 可读性与质感 ---- */
.hc-vignette {
  position: absolute;
  inset: 0;
  z-index: 3;
  pointer-events: none;
  background: radial-gradient(ellipse at 50% 50%, rgba(0, 0, 0, 0) 52%, rgba(0, 0, 0, 0.42) 100%);
}

.hc-scrim {
  position: absolute;
  left: 0;
  right: 0;
  height: 132px;
  z-index: 3;
  pointer-events: none;
}

.hc-scrim-top {
  top: 0;
  background: linear-gradient(to bottom, rgba(4, 6, 16, 0.72), rgba(4, 6, 16, 0));
}

.hc-scrim-bottom {
  bottom: 0;
  background: linear-gradient(to top, rgba(4, 6, 16, 0.6), rgba(4, 6, 16, 0));
}

/* ---- HUD 通用 ---- */
.hc-hud {
  position: absolute;
  z-index: 5;
  pointer-events: none;
  color: #dfe6f5;
}

.hc-hud button,
.hc-hud .el-switch,
.hc-hud .el-slider {
  pointer-events: auto;
}

/* ---- 标题 ---- */
.hc-hud-title {
  top: 18px;
  left: 20px;
}

.hc-brand {
  position: relative;
  display: inline-block;
  padding-bottom: 8px;
  font-size: 24px;
  font-weight: 700;
  letter-spacing: 6px;
  color: #ffd166;
  text-shadow: 0 0 22px rgba(255, 209, 102, 0.5), 0 0 4px rgba(255, 209, 102, 0.6);
}

.hc-brand::after {
  content: '';
  position: absolute;
  left: 0;
  bottom: 0;
  width: 100%;
  height: 1px;
  background: linear-gradient(90deg, rgba(255, 209, 102, 0.85), rgba(255, 209, 102, 0.05));
}

.hc-sub {
  margin-top: 10px;
  font-size: 12px;
  color: #93a0c2;
  letter-spacing: 1px;
}

/* ---- 工具条 ---- */
.hc-hud-tools {
  top: 18px;
  right: 20px;
  display: flex;
  gap: 8px;
}

.hc-tool {
  padding: 7px 16px;
  font-size: 12px;
  letter-spacing: 0.5px;
  color: #cfd8ee;
  background: linear-gradient(180deg, rgba(28, 38, 68, 0.78), rgba(14, 20, 40, 0.72));
  border: 1px solid rgba(120, 150, 220, 0.28);
  border-radius: 18px;
  cursor: pointer;
  backdrop-filter: blur(8px);
  box-shadow: 0 4px 14px rgba(0, 0, 0, 0.35), inset 0 1px 0 rgba(255, 255, 255, 0.06);
  transition: all 0.22s ease;
}

.hc-tool:hover {
  border-color: rgba(255, 209, 102, 0.75);
  color: #ffd166;
  transform: translateY(-1px);
  box-shadow: 0 6px 18px rgba(0, 0, 0, 0.4), 0 0 14px rgba(255, 209, 102, 0.18);
}

.hc-tool.on {
  background: linear-gradient(180deg, rgba(255, 209, 102, 0.24), rgba(255, 179, 71, 0.12));
  border-color: #ffd166;
  color: #ffd166;
  box-shadow: 0 0 16px rgba(255, 209, 102, 0.28), inset 0 1px 0 rgba(255, 255, 255, 0.12);
}

/* ---- 更多面板 ---- */
.hc-more {
  top: 62px;
  right: 20px;
  width: 244px;
  padding: 16px;
  background: linear-gradient(180deg, rgba(18, 26, 50, 0.92), rgba(10, 15, 32, 0.9));
  border: 1px solid rgba(120, 150, 220, 0.26);
  border-radius: 14px;
  backdrop-filter: blur(14px);
  box-shadow: 0 16px 44px rgba(0, 0, 0, 0.5), inset 0 1px 0 rgba(255, 255, 255, 0.05);
  animation: hc-pop 0.22s ease both;
}

@keyframes hc-pop {
  from {
    opacity: 0;
    transform: translateY(-8px) scale(0.98);
  }
  to {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}

.hc-more-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-size: 13px;
  margin-bottom: 10px;
  color: #b8c4de;
}

.hc-more-slider {
  flex-direction: column;
  align-items: stretch;
  gap: 2px;
}

.hc-more-legend {
  margin-top: 4px;
  padding-top: 10px;
  border-top: 1px solid rgba(120, 150, 220, 0.16);
}

.hc-lg-title {
  font-size: 11px;
  color: #6f7c9c;
  margin: 6px 0 6px;
}

.hc-lg-items {
  display: flex;
  flex-wrap: wrap;
  gap: 6px 10px;
}

.hc-lg-item {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  font-size: 11px;
  color: #aab6d2;
}

.hc-lg-item i {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  display: inline-block;
}

/* ---- 图例 ---- */
.hc-legend {
  top: 84px;
  left: 20px;
  width: 176px;
  max-height: calc(100% - 200px);
  padding: 12px 12px 10px;
  background: linear-gradient(180deg, rgba(18, 26, 50, 0.68), rgba(10, 15, 32, 0.62));
  border: 1px solid rgba(120, 150, 220, 0.2);
  border-radius: 14px;
  backdrop-filter: blur(12px);
  box-shadow: 0 14px 36px rgba(0, 0, 0, 0.4);
  pointer-events: auto;
  display: flex;
  flex-direction: column;
}

.hc-legend-head {
  font-size: 11px;
  color: #8a97b8;
  letter-spacing: 2px;
  margin-bottom: 10px;
  padding-bottom: 8px;
  border-bottom: 1px solid rgba(120, 150, 220, 0.14);
}

.hc-legend-body {
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.hc-legend-body::-webkit-scrollbar {
  width: 4px;
}

.hc-legend-body::-webkit-scrollbar-thumb {
  background: rgba(120, 150, 220, 0.3);
  border-radius: 2px;
}

.hc-legend-item {
  position: relative;
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 5px 8px;
  border-radius: 8px;
  font-size: 12px;
  color: #b8c4de;
  cursor: pointer;
  border-left: 2px solid transparent;
  transition: background 0.18s ease, border-color 0.18s ease, transform 0.18s ease;
}

.hc-legend-item:hover {
  background: rgba(120, 150, 220, 0.14);
  transform: translateX(2px);
}

.hc-legend-item.active {
  background: rgba(255, 209, 102, 0.14);
  border-left-color: #ffd166;
  color: #ffd166;
}

.hc-legend-dot {
  width: 9px;
  height: 9px;
  border-radius: 50%;
  flex: 0 0 auto;
  box-shadow: 0 0 8px currentColor;
}

.hc-legend-item.active .hc-legend-dot {
  box-shadow: 0 0 12px currentColor, 0 0 4px #ffd166;
}

.hc-legend-name {
  flex: 1 1 auto;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.hc-legend-years {
  font-size: 10px;
  color: #6f7c9c;
}

/* ---- 底部提示 ---- */
.hc-hint {
  left: 50%;
  bottom: 18px;
  transform: translateX(-50%);
  font-size: 12px;
  color: #8a97b8;
  background: rgba(10, 15, 32, 0.66);
  padding: 7px 18px;
  border: 1px solid rgba(120, 150, 220, 0.16);
  border-radius: 16px;
  backdrop-filter: blur(8px);
  white-space: nowrap;
  max-width: 92%;
  overflow: hidden;
  text-overflow: ellipsis;
}

/* ---- 朝代标签 ---- */
.hc-labels {
  position: absolute;
  inset: 0;
  z-index: 4;
  pointer-events: none;
  overflow: hidden;
}

.hc-label {
  position: absolute;
  top: 0;
  left: 0;
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 3px 11px;
  font-size: 12px;
  font-weight: 500;
  color: #eaf1ff;
  background: linear-gradient(180deg, rgba(18, 26, 50, 0.72), rgba(10, 16, 34, 0.6));
  border: 1px solid rgba(140, 170, 230, 0.26);
  border-radius: 14px;
  white-space: nowrap;
  opacity: 0;
  cursor: pointer;
  will-change: transform, opacity;
  backdrop-filter: blur(4px);
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.35);
  transition: opacity 0.2s ease, border-color 0.2s ease, box-shadow 0.2s ease, transform 0.2s ease;
}

.hc-label:hover {
  border-color: rgba(255, 209, 102, 0.6);
  box-shadow: 0 2px 14px rgba(0, 0, 0, 0.4), 0 0 12px rgba(255, 209, 102, 0.22);
}

.hc-label.is-active {
  border-color: #ffd166;
  color: #ffd166;
  box-shadow: 0 0 18px rgba(255, 209, 102, 0.4), 0 2px 10px rgba(0, 0, 0, 0.4);
}

.hc-label-dot {
  width: 7px;
  height: 7px;
  border-radius: 50%;
  box-shadow: 0 0 8px currentColor;
}

/* ---- 星点名称层（事件 / 人物 / 典故），单条样式见 initStarLabels 内联样式 ---- */
.hc-labels-star {
  z-index: 2;
}

/* ---- 悬浮提示 ---- */
.hc-tooltip {
  position: absolute;
  z-index: 6;
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 4px 10px;
  font-size: 12px;
  background: rgba(10, 16, 34, 0.9);
  border: 1px solid rgba(140, 170, 230, 0.3);
  border-radius: 8px;
  pointer-events: none;
  white-space: nowrap;
}

.hc-tt-kind {
  font-size: 10px;
  opacity: 0.85;
}

.hc-tt-name {
  color: #f2f6ff;
}

/* ---- 详情面板 ---- */
.hc-detail {
  top: 84px;
  right: 20px;
  width: 348px;
  max-height: calc(100% - 176px);
  overflow-y: auto;
  padding: 16px;
  background: linear-gradient(180deg, rgba(18, 26, 50, 0.94), rgba(10, 15, 32, 0.94));
  border: 1px solid rgba(120, 150, 220, 0.3);
  border-radius: 16px;
  backdrop-filter: blur(16px);
  pointer-events: auto;
  box-shadow: 0 20px 56px rgba(0, 0, 0, 0.58), inset 0 1px 0 rgba(255, 255, 255, 0.06);
  animation: hc-pop 0.24s ease both;
}

.hc-detail::-webkit-scrollbar {
  width: 5px;
}

.hc-detail::-webkit-scrollbar-thumb {
  background: rgba(120, 150, 220, 0.3);
  border-radius: 3px;
}

.hc-detail-close {
  position: absolute;
  top: 8px;
  right: 10px;
  z-index: 2;
  width: 26px;
  height: 26px;
  line-height: 1;
  font-size: 18px;
  color: #b8c4de;
  background: rgba(10, 15, 32, 0.6);
  border: 1px solid rgba(140, 170, 230, 0.24);
  border-radius: 50%;
  cursor: pointer;
  transition: all 0.18s ease;
}

.hc-detail-close:hover {
  color: #ffd166;
  border-color: rgba(255, 209, 102, 0.6);
  transform: rotate(90deg);
}

.hc-detail-imgwrap {
  position: relative;
  margin: -4px -4px 14px;
  border-radius: 12px;
  overflow: hidden;
  background: rgba(0, 0, 0, 0.35);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.4);
}

.hc-detail-imgwrap::after {
  content: '';
  position: absolute;
  inset: 0;
  pointer-events: none;
  background: linear-gradient(to bottom, rgba(10, 15, 32, 0) 45%, rgba(12, 18, 38, 0.85) 100%);
}

.hc-detail-imgwrap img {
  display: block;
  width: 100%;
  max-height: 200px;
  object-fit: cover;
}

.hc-detail-head {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.hc-detail-kind {
  font-size: 11px;
  padding: 2px 8px;
  border-radius: 10px;
  border: 1px solid transparent;
}

.hc-detail-name {
  margin: 0;
  font-size: 20px;
  color: #f4f7ff;
  letter-spacing: 1.5px;
  text-shadow: 0 0 16px rgba(140, 175, 255, 0.35);
}

.hc-detail-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  margin: 12px 0 14px;
  padding-bottom: 12px;
  border-bottom: 1px solid rgba(120, 150, 220, 0.16);
}

.hc-detail-meta span {
  font-size: 11px;
  color: #a8b8da;
  background: rgba(120, 150, 220, 0.13);
  border: 1px solid rgba(120, 150, 220, 0.16);
  padding: 3px 9px;
  border-radius: 9px;
}

.hc-detail-fields p {
  margin: 0 0 10px;
  font-size: 13px;
  line-height: 1.7;
  color: #c6d1ea;
}

.hc-detail-fields b {
  display: inline-block;
  min-width: 34px;
  margin-right: 8px;
  color: #ffd166;
  font-weight: 600;
}

.hc-detail-link {
  width: 100%;
  margin-top: 6px;
  padding: 9px;
  font-size: 13px;
  color: #0b1024;
  background: linear-gradient(135deg, #ffd166, #ffb347);
  border: none;
  border-radius: 10px;
  cursor: pointer;
  font-weight: 600;
  transition: filter 0.2s ease;
}

.hc-detail-link:hover {
  filter: brightness(1.08);
}

/* ---- 加载 ---- */
.hc-loading {
  position: absolute;
  inset: 0;
  z-index: 20;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 22px;
  background: radial-gradient(ellipse at 50% 50%, rgba(10, 15, 34, 0.9), rgba(3, 4, 10, 0.94));
}

.hc-loading p {
  font-size: 15px;
  color: #ffd166;
  letter-spacing: 4px;
  text-shadow: 0 0 18px rgba(255, 209, 102, 0.5);
}

.hc-loading-ring {
  position: relative;
  width: 62px;
  height: 62px;
  border-radius: 50%;
  border: 2px solid rgba(255, 209, 102, 0.16);
  box-shadow: 0 0 26px rgba(255, 209, 102, 0.18), inset 0 0 18px rgba(255, 209, 102, 0.1);
  animation: hc-spin 1.1s linear infinite;
}

.hc-loading-ring::before {
  content: '';
  position: absolute;
  inset: -2px;
  border-radius: 50%;
  border: 2px solid transparent;
  border-top-color: #ffd166;
  border-right-color: rgba(255, 209, 102, 0.4);
}

.hc-loading-ring::after {
  content: '';
  position: absolute;
  top: 50%;
  left: 50%;
  width: 8px;
  height: 8px;
  margin: -4px 0 0 -4px;
  border-radius: 50%;
  background: #ffd166;
  box-shadow: 0 0 14px #ffd166;
}

@keyframes hc-spin {
  to {
    transform: rotate(360deg);
  }
}

/* ---- 深色下 Element Plus 微调 ---- */
.hc-more :deep(.el-slider__runway) {
  background-color: rgba(120, 150, 220, 0.2);
}

@media (max-width: 900px) {
  .hc-legend,
  .hc-hud-title .hc-sub {
    display: none;
  }

  .hc-detail {
    width: 84%;
    right: 8%;
    top: auto;
    bottom: 62px;
    max-height: 52%;
  }

  .hc-hint {
    font-size: 10px;
    white-space: normal;
    text-align: center;
    line-height: 1.5;
  }
}
</style>
