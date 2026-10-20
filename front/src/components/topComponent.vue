<template>
    <header class="app-header">
        <div class="header-container">
            <div class="header-left">
                <div class="logo-section">
                    <img src="@/assets/logo.jpg" width="40" height="40">
                    <h1 class="app-title">寰宇古今长廊 · 历史科普系统</h1>
                </div>

                <nav class="main-nav">
                    <router-link to="/timeline" class="nav-item" active-class="active">
                        <span>时间轴</span>
                    </router-link>
                    <router-link to="/history-cloud" class="nav-item" active-class="active">
                        <span>史云</span>
                    </router-link>
                    <router-link to="/event" class="nav-item" active-class="active">
                        <span>事件科普</span>
                    </router-link>
                    <router-link to="/person" class="nav-item" active-class="active">
                        <span>人物科普</span>
                    </router-link>
                    <router-link to="/allusion" class="nav-item" active-class="active">
                        <span>典故科普</span>
                    </router-link>
                    <router-link to="/ai-assistant" class="nav-item" active-class="active">
                        <span>AI科普助手</span>
                    </router-link>
                    <router-link to="/interactive" class="nav-item" active-class="active">
                        <span>趣味小游戏</span>
                    </router-link>
                    <!-- ✅ 新增：管理后台入口，仅 admin 可见 -->
                    <router-link v-if="userRole === 'admin'" to="/admin" class="nav-item admin-nav" active-class="active">
                        <span>管理后台</span>
                    </router-link>
                </nav>
            </div>

            <div class="user-center">
                <el-dropdown v-if="isLogin">
                    <span class="el-dropdown-link">
                        <el-avatar :size="40" :src="userAvatar" />
                    </span>
                    <template #dropdown>
                        <el-dropdown-menu>
                            <el-dropdown-item @click="goToProfile">
                                <el-icon><User /></el-icon>
                                个人中心
                            </el-dropdown-item>
                            <el-dropdown-item @click="goToMyNotes">
                                <el-icon><Notebook /></el-icon>
                                我的笔记
                            </el-dropdown-item>
                            <el-dropdown-item v-if="userRole === 'admin'" @click="goToAdmin" divided>
                                <el-icon><Setting /></el-icon>
                                管理后台
                            </el-dropdown-item>
                            <el-dropdown-item divided @click="logout">
                                <el-icon><SwitchButton /></el-icon>
                                退出登录
                            </el-dropdown-item>
                        </el-dropdown-menu>
                    </template>
                </el-dropdown>
                <div v-else>
                    <router-link to="/login" class="tip-link">你好，请登录</router-link>
                </div>
            </div>
        </div>
    </header>
</template>

<script setup lang="ts">
import { onMounted, ref, computed } from 'vue';
import { useRouter } from 'vue-router';
import { User, Notebook, SwitchButton, Setting } from '@element-plus/icons-vue';
import { resolveAssetUrl } from '@/utils/request';

const router = useRouter();

const isLogin = ref(false);
const defaultAvatar = 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png';
const userAvatar = ref(defaultAvatar);

// 计算用户角色
const userRole = ref('user');

onMounted(() => {
    const token = localStorage.getItem("token");
    console.log('topComponent token:', token);
    
    // 简化逻辑：只要有token就认为已登录
    if (token && token !== 'undefined' && token !== 'null' && token.length > 5) {
        isLogin.value = true;
        
        // 获取用户信息
        try {
            const userInfo = localStorage.getItem('userInfo');
            if (userInfo) {
                const info = JSON.parse(userInfo);
                // 读取角色
                userRole.value = info.role || 'user';
                
                if (info.avatarPath) {
                    userAvatar.value = resolveAssetUrl(info.avatarPath);
                }
            }
        } catch (e) {
            console.error('解析用户信息失败:', e);
        }
    }
});

const logout = () => {
    localStorage.removeItem("token");
    localStorage.removeItem("userInfo");
    router.push('/login');
};

const goToProfile = () => {
    router.push('/profile');
};

const goToMyNotes = () => {
    router.push('/notes');
};

// 跳转到管理后台
const goToAdmin = () => {
    router.push('/admin');
};
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
    height: 70px;
}

.header-left {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 80px;
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
    gap: 8px;
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
    font-size: 16px;
}

.nav-item:hover {
    background-color: #f0e6d6;
    color: #8B4513;
}

/* 添加激活状态样式 */
.nav-item.active {
    background-color: #f0e6d6;
    color: #8B4513;
    font-weight: 600;
    border-bottom: 3px solid #8B4513;
}

/* ✅ 新增：管理后台特殊样式（可选） */
.admin-nav {
    color: #d4a373;
}

.admin-nav:hover {
    background-color: #f5e6d3;
    color: #b8860b;
}

.admin-nav.active {
    background-color: #f5e6d3;
    color: #b8860b;
    border-bottom-color: #b8860b;
}

.user-center {
    position: relative;
    cursor: pointer;
}

.el-dropdown-link {
    cursor: pointer;
    display: flex;
    align-items: center;
}

.el-dropdown-menu .el-dropdown-item {
    display: flex;
    align-items: center;
    gap: 8px;
}

.tip-link {
    font-size: 15px;
    text-decoration: none;
    color: #8B4513;
}

.tip-link:hover {
    color: #ff0000;
}
</style>