<template>
    <header class="app-header">
        <div class="header-container">
            <div class="header-left">
                <!-- 系统logo与名称 -->
                <div class="logo-section">
                    <img src="@/assets/logo.jpg" width="40" height="40">
                    <h1 class="app-title">华夏天澜 · 历史科普系统</h1>
                </div>

                <!-- 主导航菜单 -->
                <nav class="main-nav">
                    <router-link to="/timeline" class="nav-item">
                        <span>时间轴</span>
                    </router-link>
                    <router-link to="/event" class="nav-item">
                        <span>事件科普</span>
                    </router-link>
                    <router-link to="/person" class="nav-item">
                        <span>人物科普</span>
                    </router-link>
                    <router-link to="/allusion" class="nav-item">
                        <span>典故科普</span>
                    </router-link>
                    <router-link to="/ai-assistant" class="nav-item">
                        <span>AI科普助手</span>
                    </router-link>
                    <router-link to="/interactive" class="nav-item">
                        <span>趣味小游戏</span>
                    </router-link>
                </nav>
            </div>

            <!-- 用户中心 -->
            <div class="user-center">
                <el-dropdown v-if="isLogin">
                    <span class="el-dropdown-link">
                        <el-avatar :size="50" :src="avatar_url" />
                    </span>
                    <template #dropdown>
                        <el-dropdown-menu>
                            <el-dropdown-item>个人中心</el-dropdown-item>
                            <el-dropdown-item>我的笔记</el-dropdown-item>
                            <el-dropdown-item>答题记录</el-dropdown-item>
                            <el-dropdown-item @click="logout">退出登录</el-dropdown-item>
                        </el-dropdown-menu>
                    </template>
                </el-dropdown>
                <div v-else>
                    <a class="tip-link" href="/login">你好，请登录</a>
                </div>
            </div>
        </div>
    </header>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue';
import { useRouter } from 'vue-router'

const isLogin = ref(false)

onMounted(() => {
    const token = localStorage.getItem("token");
    if (token) {
        isLogin.value = true;
    }
})
const avatar_url = "https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png"
const router = useRouter();

const logout = () => {
    localStorage.removeItem("token");
    router.push('/login')
}
</script>

<style scoped>
/* 顶部导航栏 */
.app-header {
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
    position: sticky;
    top: 0;
    z-index: 100;
    background: #f9f5f0;
}

.header-container {
    width: 95%;
    margin: 0 auto;
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 0 auto;
    height: 80px;
}

.header-left {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 120px;
}

.logo-section {
    display: flex;
    align-items: center;
    gap: 10px;
}

.logo-icon {
    width: 50px;
    height: 50px;
}

.app-title {
    font-size: 20px;
    color: #8B4513;
    font-weight: 600;
    margin: 0;
}

.main-nav {
    display: flex;
    align-items: center;
    justify-content: space-around;
}

.nav-item {
    display: flex;
    align-items: center;
    gap: 6px;
    padding: 8px 16px;
    color: #555;
    text-decoration: none;
    border-radius: 4px;
    transition: all 0.3s ease;
    font-size: 18px;
}

.nav-item:hover {
    background-color: #f0e6d6;
    color: #8B4513;
}

.user-center {
    position: relative;
    cursor: pointer;
}

.user-avatar {
    width: 40px;
    height: 40px;
    border-radius: 50%;
    border: 2px solid #f0e6d6;
}

.user-menu {
    position: absolute;
    top: 50px;
    right: 0;
    background-color: #fff;
    border-radius: 4px;
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
    padding: 10px 0;
    width: 120px;
    display: none;
}

.user-center:hover .user-menu {
    display: block;
}

.user-menu span {
    display: block;
    padding: 8px 16px;
    font-size: 14px;
    color: #555;
    transition: background-color 0.2s;
}

.user-menu span:hover {
    background-color: #f0e6d6;
    color: #8B4513;
}

.tip-link {
    font-size: 15px;
    text-decoration: none;
    color: #000;
}

.tip-link:hover {
    color: #ff0000;
}
</style>