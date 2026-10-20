<template>
    <!-- 悬浮外层：固定在屏幕左侧，不影响其他布局 -->
    <div class="float-card">
        <div class="content">
            <p class="title">尊敬的评委老师好！</p>
            <p style="color: black;">为方便您审阅系统功能，特提供以下账号密码：</p>
            <div class="account-group">
                <p class="label">管理端账号密码：</p>
                <p class="value">账号：admin</p>
                <p class="value">密码：123456</p>
            </div>

            <div class="account-group">
                <p class="label">用户端测试账号密码：</p>
                <p class="value">账号：test</p>
                <p class="value">密码：123456</p>
            </div>
        </div>
    </div>
</template>

<script setup lang="ts">
import { reactive, onMounted } from 'vue';

const data = reactive({});

onMounted(() => {
    console.log('账号密码卡片已挂载');
});
</script>

<style scoped>
/* 核心：悬浮定位 + 不占页面空间 + 贴左侧 */
.float-card {
    position: fixed;
    /* 固定悬浮，脱离文档流 → 不影响其他布局 */
    left: 20px;
    /* 距离左侧 20px，可自己改 */
    top: 50%;
    /* 垂直居中 */
    transform: translateY(-50%);
    z-index: 999;
    /* 悬浮在最上层 */

    /* 尺寸 */
    width: 400px;
    height: 400px;

    /* 样式 */
    background: #ffffff;
    border-radius: 16px;
    box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
    padding: 32px;
    box-sizing: border-box;
}

/* 内容样式 */
.content {
    width: 100%;
    height: 100%;
    display: flex;
    flex-direction: column;
    justify-content: center;
}

.title {
    font-size: 18px;
    font-weight: 600;
    text-align: center;
    margin: 0 0 28px 0;
    color: #222;
}

.account-group {
    margin-bottom: 24px;
}

.label {
    font-size: 15px;
    color: #555;
    margin: 0 0 10px 0;
}

.value {
    font-size: 15px;
    font-weight: 500;
    color: #000;
    margin: 6px 0 6px 12px;
}
</style>