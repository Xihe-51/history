<template>
    <div>
    </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue';

// 数据部分
const data = reactive({});

// 生命周期
onMounted(() => {
    console.log('组件已挂载');
});
</script>

<style scoped></style>