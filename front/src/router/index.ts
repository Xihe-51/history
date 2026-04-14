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
		},
		{
			path: '/admin',
			name: 'admin',
			redirect: '/admin/user-management',
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
