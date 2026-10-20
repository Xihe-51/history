// src/router/index.ts
import { createRouter, createWebHashHistory } from 'vue-router';
import LoginView from '@/views/LoginView.vue';

const router = createRouter({
	history: createWebHashHistory(),
	routes: [
		{
			path: '/',
			redirect: '/timeline'
		},
		{
			path: '/login',
			name: 'login',
			component: LoginView,
		},
		{
			path: '/timeline',
			name: 'timeline',
			component: () => import("@/views/TimeLineView.vue"),
			meta: { requiresAuth: true }
		},
		{
			path: '/history-cloud',
			name: 'history-cloud',
			component: () => import("@/views/HistoryCloudView.vue"),
			meta: { requiresAuth: true }
		},
		{
			path: '/person',
			name: 'person',
			component: () => import("@/views/PersonView.vue"),
			meta: { requiresAuth: true }
		},
		{
			path: '/event',
			name: 'event',
			component: () => import("@/views/EventView.vue"),
			meta: { requiresAuth: true }
		},
		{
			path: '/allusion',
			name: 'allusion',
			component: () => import("@/views/AllusionView.vue"),
			meta: { requiresAuth: true }
		},
		{
			path: '/ai-assistant',
			name: 'ai-assistant',
			component: () => import("@/views/Ai-AssistantView.vue"),
			meta: { requiresAuth: true }
		},
		{
			path: '/interactive',
			name: 'interactive',
			component: () => import("@/views/InteractiveView.vue"),
			meta: { requiresAuth: true }
		},
		{
			path: '/profile',
			name: 'profile',
			component: () => import("@/views/ProfileView.vue"),
			meta: { requiresAuth: true }
		},
		{
			path: '/notes',
			name: 'MyNotes',
			component: () => import('@/views/NotesView.vue'),
			meta: { requiresAuth: true }
		},
		{
			path: '/admin',
			name: 'admin',
			redirect: '/admin/user-management',
			component: () => import("@/views/admin/AdminView.vue"),
			meta: {
				requiresAuth: true,
				role: 'admin'
			},
			children: [
				{
					path: 'user-management',
					name: 'user-management',
					component: () => import("@/views/admin/UserManagementView.vue"),
					meta: { requiresAuth: true, role: 'admin' }
				},
				{
					path: 'science-data-maintenance',
					name: 'science-data-maintenance',
					component: () => import("@/views/admin/ScienceDataMaintenanceView.vue"),
					meta: { requiresAuth: true, role: 'admin' }
				},
				{
					path: 'data-management',
					name: 'data-management',
					component: () => import("@/views/admin/DataManagementView.vue"),
					meta: { requiresAuth: true, role: 'admin' }
				}
			]
		},
		// 未匹配的地址回到时间轴，避免白屏
		{
			path: '/:pathMatch(.*)*',
			redirect: '/timeline'
		}
	]
});

/** 安全解析 localStorage 中的 userInfo（损坏时自动清理，避免整个应用因 JSON 报错白屏） */
function safeGetUserInfo(): any {
	try {
		const raw = localStorage.getItem('userInfo');
		return raw ? JSON.parse(raw) : null;
	} catch {
		localStorage.removeItem('userInfo');
		return null;
	}
}

/** 从 JWT 载荷中读取角色，作为 localStorage 缺失时的兜底 */
function roleFromToken(token: string | null): string | null {
	if (!token) return null;
	try {
		const payload = token.split('.')[1];
		if (!payload) return null;
		const base64 = payload.replace(/-/g, '+').replace(/_/g, '/');
		const padded = base64.padEnd(Math.ceil(base64.length / 4) * 4, '=');
		const json = decodeURIComponent(
			atob(padded).split('').map(c => '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2)).join('')
		);
		return JSON.parse(json).role || null;
	} catch {
		return null;
	}
}

router.beforeEach((to, from, next) => {
	// 根路径统一进入时间轴（登录后由管理员自行选择是否进入后台）
	const token = localStorage.getItem('token');
	const isLoggedIn = !!token;
	const userInfo = safeGetUserInfo();
	const userRole = userInfo?.role || roleFromToken(token) || 'user';

	if (to.meta.requiresAuth) {
		if (!isLoggedIn) {
			next({ name: 'login', query: { redirect: to.fullPath } });
			return;
		}
		if (to.meta.role && userRole !== to.meta.role) {
			next({ name: 'timeline' });
			return;
		}
	}

	// 已登录访问登录页：直接进入对应首页
	if (isLoggedIn && to.name === 'login') {
		next({ name: userRole === 'admin' ? 'user-management' : 'timeline' });
		return;
	}

	next();
});

export default router;
