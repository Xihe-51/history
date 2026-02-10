<template>
	<div class="history-app">
		<!-- 顶部导航栏 -->
		<top-component />
		<!-- 主内容区域 -->
		<main class="app-main">

		</main>
	</div>
</template>

<script setup>
import { ElMessage } from 'element-plus';
import { onMounted, ref } from 'vue'
import topComponent from '@/components/topComponent.vue';
</script>

<style scoped>
.history-app {
	min-height: 100%;
	width: 100%;
	display: flex;
	flex-direction: column;
	background-color: #f9f5f0;
	font-family: "Microsoft YaHei", "SimHei", sans-serif;
	color: #333;
}

/* 主内容区域 */
.app-main {
	flex: 1;
	max-width: 1440px;
	margin: 0 auto;
	padding: 20px;
}
</style>