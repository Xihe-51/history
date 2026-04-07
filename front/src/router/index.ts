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
<<<<<<< HEAD
			path: '/person',
			name: 'person',
			component: () => import("@/views/PersonView.vue"),
		},
		{
			path: '/event',
			name: 'event',
			component: () => import("@/views/EventView.vue"),
		},
		{
			path: '/allusion',
			name: 'allusion',
			component: () => import("@/views/AllusionView.vue"),
		},
		{
			path: '/ai-assistant',
			name: 'ai-assistant',
			component: () => import("@/views/Ai-AssistantView.vue"),
		},
		{
			path: '/interactive',
			name: 'interactive',
			component: () => import("@/views/InteractiveView.vue"),
		},
		{
			path: '/profile',
			name: 'profile',
			component: () => import("@/views/ProfileView.vue"),
			meta: { requiresAuth: true },
		},
		{
			path: '/notes',
			name: 'MyNotes',
			component: () => import('@/views/NotesView.vue')
=======
			path: '/profile',
			name: 'profile',
			component: () => import("@/views/ProfileView.vue"),
			meta: { requiresAuth: true }, // 需要认证才能访问
>>>>>>> 822a5594a4ae73b06db09ca8f2e5db1ca1651bf5
		},
		{
			path: '/admin',
			name: 'admin',
<<<<<<< HEAD
			redirect: '/admin/user-management',
=======
			redirect: '/admin/user-management', // 默认子路由
>>>>>>> 822a5594a4ae73b06db09ca8f2e5db1ca1651bf5
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

<<<<<<< HEAD
export default router
=======
export default router
>>>>>>> 822a5594a4ae73b06db09ca8f2e5db1ca1651bf5
