<template>
    <header class="app-header">
        <div class="header-container">
            <div class="header-left">
<<<<<<< HEAD
=======
                <!-- 系统logo与名称 -->
>>>>>>> 822a5594a4ae73b06db09ca8f2e5db1ca1651bf5
                <div class="logo-section">
                    <img src="@/assets/logo.jpg" width="40" height="40">
                    <h1 class="app-title">华夏天澜 · 历史科普系统</h1>
                </div>

<<<<<<< HEAD
                <nav class="main-nav">
                    <router-link to="/timeline" class="nav-item" active-class="active">
                        <span>时间轴</span>
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
=======
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
>>>>>>> 822a5594a4ae73b06db09ca8f2e5db1ca1651bf5
                        <span>趣味小游戏</span>
                    </router-link>
                </nav>
            </div>

<<<<<<< HEAD
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
                            <el-dropdown-item divided @click="logout">
                                <el-icon><SwitchButton /></el-icon>
                                退出登录
                            </el-dropdown-item>
=======
            <!-- 用户中心 -->
            <div class="user-center">
                <el-dropdown v-if="isLogin">
                    <span class="el-dropdown-link">
                        <el-avatar :size="50" :src="BASE_URL+avatar_url || 'https://cdn.pixabay.com/photo/2015/10/05/22/37/blank-profile-picture-973460_960_720.png'" />
                    </span>
                    <template #dropdown>
                        <el-dropdown-menu>
                            <el-dropdown-item @click="goToProfile">个人中心</el-dropdown-item>
                            <el-dropdown-item>我的笔记</el-dropdown-item>
                            <el-dropdown-item @click="logout">退出登录</el-dropdown-item>
>>>>>>> 822a5594a4ae73b06db09ca8f2e5db1ca1651bf5
                        </el-dropdown-menu>
                    </template>
                </el-dropdown>
                <div v-else>
<<<<<<< HEAD
                    <router-link to="/login" class="tip-link">你好，请登录</router-link>
=======
                    <a class="tip-link" href="/login">你好，请登录</a>
>>>>>>> 822a5594a4ae73b06db09ca8f2e5db1ca1651bf5
                </div>
            </div>
        </div>
    </header>
</template>

<script setup lang="ts">
<<<<<<< HEAD
import { onMounted, ref, computed } from 'vue';
import { useRouter } from 'vue-router';
import { User, Notebook, SwitchButton } from '@element-plus/icons-vue';

const router = useRouter();

const isLogin = ref(false);
const defaultAvatar = 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png';
const userAvatar = ref(defaultAvatar);

onMounted(() => {
    const token = localStorage.getItem("token");
    console.log('topComponent token:', token);
    
    // 简化逻辑：只要有token就认为已登录
    if (token && token !== 'undefined' && token !== 'null' && token.length > 5) {
        isLogin.value = true;
        
        // 获取用户信息中的头像
        try {
            const userInfo = localStorage.getItem('userInfo');
            if (userInfo) {
                const info = JSON.parse(userInfo);
                if (info.avatarPath) {
                    userAvatar.value = `http://localhost:8080/api${info.avatarPath}`;
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
=======
import { onMounted, ref } from 'vue';
import { useRouter } from 'vue-router'
import { getUserById } from '@/api/user';
import { BASE_URL } from '@/utils/request';

const isLogin = ref(false);
const userId = ref(0);
const avatar_url = ref('');
const router = useRouter();

onMounted(() => {
    const token = localStorage.getItem("token");
    if (token) {
        isLogin.value = true;
        const payload = JSON.parse(atob(token.split('.')[1]));
        userId.value = payload.userId;
        const res = getUserById(userId.value);
        res.then((response) => {
            if (response.data.code === 200) {
                const user = response.data.data;
                avatar_url.value = user.avatarPath;
            }
        }).catch((error) => {
            console.error("获取用户信息失败:", error);
        });
    }
})


const logout = () => {
    localStorage.removeItem("token");
    router.push('/login')
}
const goToProfile = () => {
    router.push('/profile')
}
>>>>>>> 822a5594a4ae73b06db09ca8f2e5db1ca1651bf5
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
<<<<<<< HEAD
    height: 70px;
=======
    height: 80px;
>>>>>>> 822a5594a4ae73b06db09ca8f2e5db1ca1651bf5
}

.header-left {
    display: flex;
    align-items: center;
    justify-content: space-between;
<<<<<<< HEAD
    gap: 80px;
=======
    gap: 120px;
>>>>>>> 822a5594a4ae73b06db09ca8f2e5db1ca1651bf5
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
<<<<<<< HEAD
    gap: 8px;
=======
>>>>>>> 822a5594a4ae73b06db09ca8f2e5db1ca1651bf5
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
<<<<<<< HEAD
    font-size: 16px;
=======
    font-size: 18px;
>>>>>>> 822a5594a4ae73b06db09ca8f2e5db1ca1651bf5
}

.nav-item:hover {
    background-color: #f0e6d6;
    color: #8B4513;
}

<<<<<<< HEAD
/* 添加激活状态样式 */
.nav-item.active {
    background-color: #f0e6d6;
    color: #8B4513;
    font-weight: 600;
    border-bottom: 3px solid #8B4513;
}

=======
>>>>>>> 822a5594a4ae73b06db09ca8f2e5db1ca1651bf5
.user-center {
    position: relative;
    cursor: pointer;
}

<<<<<<< HEAD
.el-dropdown-link {
    cursor: pointer;
    display: flex;
    align-items: center;
}

.el-dropdown-menu .el-dropdown-item {
    display: flex;
    align-items: center;
    gap: 8px;
=======
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
>>>>>>> 822a5594a4ae73b06db09ca8f2e5db1ca1651bf5
}

.tip-link {
    font-size: 15px;
    text-decoration: none;
<<<<<<< HEAD
    color: #8B4513;
=======
    color: #000;
>>>>>>> 822a5594a4ae73b06db09ca8f2e5db1ca1651bf5
}

.tip-link:hover {
    color: #ff0000;
}
</style>