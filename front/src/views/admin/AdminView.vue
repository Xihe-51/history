<template>
    <div class="layout">
        <el-header>
            <div class="header-left">
                <!-- 网站Logo -->
                <div class="logo">
                    <img src="@/assets/logo.jpg" alt="历史科普" width="40" height="40" />
                    <span class="logo-text">华夏天澜 · 历史科普系统管理端</span>
                </div>
            </div>
            <div class="header-right">
                <div class="user-info">
                    <span>您好，管理员</span>
                </div>
                <el-button type="danger" @click="logout">退出系统</el-button>
            </div>
        </el-header>
        <el-container>
            <el-aside width="150px">
                <el-menu active-text-color="#8B4513" background-color="#f9f5f0" default-active="/admin/user-management"
                    text-color="#333" router>
                    <el-menu-item index="/admin/user-management">
                        <el-icon>
                            <user />
                        </el-icon>
                        <span>用户管理</span>
                    </el-menu-item>
                    <el-menu-item index="/admin/science-data-maintenance">
                        <el-icon>
                            <notebook />
                        </el-icon>
                        <span>科普数据维护</span>
                    </el-menu-item>
                    <el-menu-item index="/admin/data-management">
                        <el-icon>
                            <coin />
                        </el-icon>
                        <span>数据管理</span>
                    </el-menu-item>
                </el-menu>
            </el-aside>
            <el-main>
                <router-view />
            </el-main>
        </el-container>
    </div>
</template>


<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue';
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus';
const router = useRouter();

const logout = () => {
    localStorage.removeItem("token");
    router.push('/login')
}
</script>

<style scoped>
.layout {
    height: 100%;
    width: 100%;
    margin: 0;
    display: flex;
    flex-direction: column;
}

.el-container {
    flex: 1;
}

.el-header {
    height: 60px;
    display: flex;
    align-items: center;
    justify-content: space-between;
    color: #8B4513;
    background-color: #f9f5f0;
}

.header-left {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 0 20px;
}

.logo {
    display: flex;
    align-items: center;
    gap: 10px;
}

.header-right {
    display: flex;
    align-items: center;
    gap: 20px;
}

.user-info {
    display: flex;
    align-items: center;
    gap: 10px;
    color: #8B4513;
}

.el-menu {
    height: 100%;
    border-right: none;
}

.el-main {
    height: 100%;
    width: 100%;
}
</style>