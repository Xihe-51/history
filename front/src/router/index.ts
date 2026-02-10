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
		}
	]
})

export default router
