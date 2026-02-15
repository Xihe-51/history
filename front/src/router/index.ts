import { createRouter, createWebHistory } from 'vue-router';
import LoginView from '@/views/LoginView.vue';

const router = createRouter({
	history: createWebHistory(import.meta.env.BASE_URL),
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
		},
		{
			path: '/admin',
			name: 'admin',
			redirect: '/admin/user-management', // 默认子路由
			component: () => import("@/views/admin/AdminView.vue"),
			children: [
				{
					path: 'user-management',
					name: 'user-management',
					component: () => import("@/views/admin/UserManagementView.vue"),
				},
				{
					path: 'science-data-maintenance',
					name: 'science-data-maintenance',
					component: () => import("@/views/admin/ScienceDataMaintenanceView.vue"),
				},
				{
					path: 'data-management',
					name: 'data-management',
					component: () => import("@/views/admin/DataManagementView.vue"),
				}
			]
		}
	]
})

export default router
