<template>
    <div class="profile-page">
        <!-- 顶部返回栏 -->
        <div class="profile-topbar">
            <el-button class="back-btn" @click="router.back()">
                <el-icon>
                    <Back />
                </el-icon>
                <span>返回</span>
            </el-button>
        </div>

        <!-- 顶部 hero 标题区 -->
        <section class="profile-hero">
            <div class="hero-content">
                <h1 class="hero-title">个人中心</h1>
                <p class="hero-subtitle">管理身份信息 · 记录你的历史足迹</p>
                <div class="hero-user">
                    <el-avatar :size="64" :src="avatarUrl" class="hero-avatar" />
                    <div class="hero-user-meta">
                        <span class="hero-username">{{ displayName }}</span>
                        <span class="hero-role">{{ roleLabel }}</span>
                    </div>
                </div>
            </div>
            <div class="hero-wave"></div>
        </section>

        <!-- 主体内容区：两栏卡片 -->
        <div class="main-content">
            <div class="profile-grid">
                <!-- 左栏：头像与身份 -->
                <section class="user-card identity-card">
                    <div class="avatar-section">
                        <el-avatar :size="120" :src="avatarUrl" class="user-avatar"/>
                        <input ref="avatarFileInput" type="file" accept="image/*" @change="handleAvatarChange" style="display: none;" />
                        <el-button class="change-avatar-btn" size="small" @click="triggerFileInput">
                            <el-icon>
                                <Camera />
                            </el-icon>
                            <span>更换头像</span>
                        </el-button>
                    </div>

                    <h3 class="identity-name">{{ displayName }}</h3>

                    <div class="info-section identity-meta">
                        <div class="info-item">
                            <span class="label">UID：</span>
                            <span class="value">{{ userId || '加载中...' }}</span>
                        </div>
                        <div class="info-item">
                            <span class="label">账号状态：</span>
                            <el-tag type="success" v-if="userInfo.status === 1">正常</el-tag>
                            <el-tag type="danger" v-else>禁用</el-tag>
                        </div>
                        <div class="info-item">
                            <span class="label">用户名：</span>
                            <span class="value">{{ displayName }}</span>
                        </div>
                    </div>
                </section>

                <!-- 右栏：账户信息与操作 -->
                <section class="user-card info-card">
                    <div class="info-card-header">
                        <span class="info-card-icon">🗂️</span>
                        <h3 class="info-card-title">账户信息</h3>
                    </div>

                    <div class="info-section info-list">
                        <div class="info-item">
                            <span class="label">邮箱：</span>
                            <el-input v-model="userInfo.email" :disabled="!isEditing" />
                        </div>
                        <div class="info-item">
                            <span class="label">更新时间：</span>
                            <span class="value">{{ userInfo.updateTime || '暂无数据' }}</span>
                        </div>
                        <div class="info-item">
                            <span class="label">最后登录：</span>
                            <span class="value">{{ userInfo.lastLoginTime || '暂无数据' }}</span>
                        </div>
                        <div class="info-item">
                            <span class="label">注册时间：</span>
                            <span class="value">{{ userInfo.createdTime || '暂无数据' }}</span>
                        </div>
                    </div>

                    <!-- 操作按钮区域 -->
                    <div class="action-section">
                        <el-button v-if="!isEditing" class="edit-btn" type="primary" @click="isEditing = !isEditing">
                            <el-icon><Edit /></el-icon>
                            <span>修改邮箱</span>
                        </el-button>
                        <el-button v-else class="save-btn" type="success" @click="saveUserInfo">
                            <el-icon><Check /></el-icon>
                            <span>保存修改</span>
                        </el-button>
                        <el-button class="password-btn" @click="dialogVisible=!dialogVisible">
                            <el-icon><Unlock /></el-icon>
                            <span>修改密码</span>
                        </el-button>
                        <el-button class="ai-assistant-btn" @click="openAIAssistant">
                            <el-icon><ChatDotRound /></el-icon>
                            <span>AI 历史助手</span>
                        </el-button>
                    </div>
                </section>
            </div>
        </div>

        <!-- 加载中提示 -->
        <el-dialog v-model="loading" title="提示" width="30%" :show-close="false">
            <div class="loading-content">
                <el-icon size="30">
                    <Loading />
                </el-icon>
                <p>正在加载用户信息...</p>
            </div>
        </el-dialog>

        <!-- 修改密码弹窗 -->
        <el-dialog v-model="dialogVisible" title="修改密码" width="min(420px, 90vw)">
            <div>
                <el-input v-model="oldPassword" type="password" show-password placeholder="请输入原密码" style="margin-bottom: 16px;" />
                <el-input v-model="newPassword" type="password" show-password placeholder="请输入新密码（至少6位）" style="margin-bottom: 16px;" />
                <el-input v-model="confirmPassword" type="password" show-password placeholder="请再次输入新密码" style="margin-bottom: 16px;" />
                <div style="text-align: right;">
                    <el-button @click="dialogVisible = false">取消</el-button>
                    <el-button type="primary" :loading="savingPassword" @click="savePassword">确认修改</el-button>
                </div>
            </div>
        </el-dialog>
    </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue';
import { getCurrentUser, updateUserById, changePassword } from '@/api/user';
import { useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import { Loading, Back, Camera, Edit, Check, Unlock, ChatDotRound } from '@element-plus/icons-vue';
import { uploadFile } from '@/api/file';
import { resolveAssetUrl, errorMessage } from '@/utils/request';

const oldPassword = ref('');
const newPassword = ref('');
const confirmPassword = ref('');
const savingPassword = ref(false);
const router = useRouter();

// 文件选择框的引用
const avatarFileInput = ref<HTMLInputElement>();

// 头像地址（完整URL）
const avatarUrl = ref('');

// 用户ID
const userId = ref(0);

// 加载状态
const loading = ref(true);

// 用户信息
const userInfo = ref({
    username: '',
    email: '',
    createdTime: '',
    lastLoginTime: '',
    updateTime: '',
    status: 1,
    role: 'user'
});

const dialogVisible = ref(false);
const isEditing = ref(false);

// 纯展示：用户名回退显示
const displayName = computed(() => userInfo.value.username || '加载中...');

// 纯展示：角色文案
const roleLabel = computed(() => (userInfo.value.role === 'admin' ? '管理员' : '历史探索者'));

// 打开 AI 助手（关联当前用户）
const openAIAssistant = () => {
    router.push({ 
        path: '/ai-assistant', 
        query: { 
            context: 'user',
            name: userInfo.value.username,
            userId: userId.value
        } 
    });
    ElMessage.success('正在打开 AI 历史助手');
};

// 格式化日期
const formatDate = (dateStr: string) => {
    if (!dateStr) return '暂无数据';
    try {
        return dateStr.replace('T', ' ').substring(0, 19);
    } catch (e) {
        return dateStr;
    }
};

// 触发文件选择框点击
const triggerFileInput = () => {
    if (avatarFileInput.value) {
        avatarFileInput.value.click();
    }
};
// 处理头像文件选择
const handleAvatarChange = async (e: Event) => {
    const target = e.target as HTMLInputElement;
    const file = target.files?.[0];
    if (!file) return;

    if (!file.type.startsWith('image/')) {
        ElMessage.error('请选择图片格式的文件');
        target.value = '';
        return;
    }

    try {
        const uploadRes = await uploadFile({
            file: file,
            resourceType: '图片'
        });

        if (uploadRes.data.code === 200) {
            const avatarPath = uploadRes.data.data;
            const updateRes = await updateUserById({
                id: userId.value,
                avatarPath: avatarPath
            });

            if (updateRes.data.code === 200) {
                ElMessage.success('头像更换成功');
                avatarUrl.value = resolveAssetUrl(avatarPath);
                const userInfoStr = localStorage.getItem('userInfo');
                if (userInfoStr) {
                    const userData = JSON.parse(userInfoStr);
                    userData.avatarPath = avatarPath;
                    localStorage.setItem('userInfo', JSON.stringify(userData));
                }
            } else {
                ElMessage.error('更新头像失败：' + updateRes.data.msg);
            }
        } else {
            ElMessage.error('上传头像失败：' + uploadRes.data.msg);
        }
    } catch (error) {
        console.error('更换头像失败:', error);
        ElMessage.error('更换头像失败，请稍后重试');
    } finally {
        target.value = '';
    }
};

// 保存密码（需要原密码校验）
const savePassword = async () => {
    if (!oldPassword.value) {
        ElMessage.error('请输入原密码');
        return;
    }
    if (newPassword.value.length < 6) {
        ElMessage.error('密码长度不能少于6位');
        return;
    }
    if (newPassword.value !== confirmPassword.value) {
        ElMessage.error('两次输入的密码不一致');
        return;
    }
    savingPassword.value = true;
    try {
        const res = await changePassword(oldPassword.value, newPassword.value);
        if (res.data.code === 200) {
            ElMessage.success('密码修改成功，请重新登录');
            dialogVisible.value = false;
            localStorage.removeItem('token');
            localStorage.removeItem('userInfo');
            setTimeout(() => {
                router.replace('/login');
            }, 1200);
        } else {
            ElMessage.error(res.data.msg || '密码修改失败');
        }
    } catch (error: any) {
        ElMessage.error(errorMessage(error, '修改密码失败，请稍后重试'));
    } finally {
        savingPassword.value = false;
        oldPassword.value = '';
        newPassword.value = '';
        confirmPassword.value = '';
    }
};

// 保存用户信息
const saveUserInfo = async () => {
    const email = (userInfo.value.email || '').trim();
    if (email && !/^[^\s@]+@[^\s@]+\.[^\s@]{2,}$/.test(email)) {
        ElMessage.error('邮箱格式不正确');
        return;
    }
    try {
        const response = await updateUserById({
            id: userId.value,
            email
        });
        if (response.data.code === 200) {
            ElMessage.success('用户信息更新成功');
            isEditing.value = false;
            const userInfoStr = localStorage.getItem('userInfo');
            if (userInfoStr) {
                const userData = JSON.parse(userInfoStr);
                userData.email = email;
                localStorage.setItem('userInfo', JSON.stringify(userData));
            }
        } else {
            ElMessage.error('更新失败：' + response.data.msg);
        }
    } catch (error: any) {
        ElMessage.error(errorMessage(error, '更新用户信息失败，请稍后重试'));
    }
};
// 获取用户信息：直接调用 /user/me，由后端根据 token 判定身份，
// 不再前端解析 JWT / 依赖 localStorage 里可能过期的 userId。
const fetchUserInfo = async () => {
    try {
        loading.value = true;

        const response = await getCurrentUser();
        if (response.data.code !== 200 || !response.data.data) {
            ElMessage.error(response.data.msg || '获取用户信息失败，请重新登录');
            return;
        }

        const user = response.data.data;
        userId.value = user.id;

        avatarUrl.value = user.avatarPath
            ? resolveAssetUrl(user.avatarPath)
            : 'https://cdn.pixabay.com/photo/2015/10/05/22/37/blank-profile-picture-973460_960_720.png';

        userInfo.value = {
            username: user.username || '未知',
            email: user.email || '',
            createdTime: formatDate(user.createdTime),
            lastLoginTime: formatDate(user.lastLoginTime),
            updateTime: formatDate(user.updatedTime),
            status: user.status ?? 1,
            role: user.role || 'user'
        };

        // 同步本地缓存，供顶部导航等使用
        const cached = { userId: user.id, username: user.username, role: user.role, avatarPath: user.avatarPath };
        localStorage.setItem('userInfo', JSON.stringify(cached));
    } catch (error: any) {
        ElMessage.error(errorMessage(error, '获取用户信息失败，请稍后重试'));
    } finally {
        loading.value = false;
    }
};

onMounted(() => {
    fetchUserInfo();
});
</script>

<style scoped>
/* ========== 页面容器：暖米色底 ========== */
.profile-page {
    width: 100%;
    min-height: 100vh;
    padding-bottom: 60px;
    box-sizing: border-box;
    background: linear-gradient(160deg, #f9f5f0 0%, #f7f2ea 100%);
}

/* 顶部返回栏 */
.profile-topbar {
    max-width: 1080px;
    margin: 0 auto;
    padding: 18px 24px 0;
    box-sizing: border-box;
}

.back-btn {
    background: rgba(255, 255, 255, 0.85);
    color: #8B4513;
    border: 1px solid rgba(201, 162, 39, 0.35);
    border-radius: 12px;
    transition: all 0.3s ease;
    box-shadow: 0 4px 12px rgba(139, 69, 19, 0.06);
}

.back-btn:hover {
    background: #fffdf7;
    color: #a67c1a;
    border-color: #d8b467;
    transform: translateX(-2px);
    box-shadow: 0 6px 16px rgba(201, 162, 39, 0.18);
}

/* ========== hero 标题区 ========== */
.profile-hero {
    position: relative;
    padding: 34px 24px 104px;
    text-align: center;
    background:
        radial-gradient(circle at 50% -20%, rgba(216, 180, 103, 0.35), rgba(216, 180, 103, 0) 65%),
        linear-gradient(135deg, #f4e9d6 0%, #efe0c6 55%, #e8d5b6 100%);
    border-bottom: 1px solid rgba(201, 162, 39, 0.18);
    overflow: hidden;
}

.hero-content {
    position: relative;
    z-index: 2;
}

.hero-title {
    font-size: 46px;
    font-weight: 700;
    letter-spacing: 8px;
    margin: 0 0 14px;
    background: linear-gradient(135deg, #8B4513 0%, #c9a227 100%);
    -webkit-background-clip: text;
    background-clip: text;
    color: transparent;
    text-shadow: 0 2px 12px rgba(201, 162, 39, 0.15);
}

.hero-subtitle {
    font-size: 16px;
    color: #8a7a63;
    letter-spacing: 3px;
    margin: 0 0 26px;
}

.hero-user {
    display: inline-flex;
    align-items: center;
    gap: 14px;
    padding: 8px 26px 8px 8px;
    background: rgba(255, 255, 255, 0.7);
    border: 1px solid rgba(201, 162, 39, 0.28);
    border-radius: 999px;
    box-shadow: 0 8px 22px rgba(139, 69, 19, 0.08);
    backdrop-filter: blur(4px);
}

.hero-avatar {
    border: 2px solid #d8b467;
    box-shadow: 0 4px 12px rgba(201, 162, 39, 0.25);
}

.hero-user-meta {
    display: flex;
    flex-direction: column;
    align-items: flex-start;
    line-height: 1.35;
}

.hero-username {
    font-size: 17px;
    font-weight: 600;
    color: #4a3728;
}

.hero-role {
    font-size: 12px;
    color: #b08d3e;
    letter-spacing: 1px;
}

/* 底部装饰波纹 */
.hero-wave {
    position: absolute;
    bottom: -2px;
    left: 0;
    width: 100%;
    height: 60px;
    background: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 1440 320'%3E%3Cpath fill='%23f9f5f0' fill-opacity='1' d='M0,96L48,112C96,128,192,160,288,160C384,160,480,128,576,122.7C672,117,768,139,864,154.7C960,171,1056,181,1152,165.3C1248,149,1344,107,1392,85.3L1440,64L1440,320L1392,320C1344,320,1248,320,1152,320C1056,320,960,320,864,320C768,320,672,320,576,320C480,320,384,320,288,320C192,320,96,320,48,320L0,320Z'%3E%3C/path%3E%3C/svg%3E") no-repeat bottom;
    background-size: cover;
}

/* ========== 主体内容：两栏卡片 ========== */
.main-content {
    width: 100%;
    max-width: 1080px;
    margin: -56px auto 0;
    padding: 0 24px;
    box-sizing: border-box;
    position: relative;
    z-index: 3;
}

.profile-grid {
    display: grid;
    grid-template-columns: 320px 1fr;
    gap: 28px;
    align-items: stretch;
}

/* 卡片通用样式 */
.user-card {
    background: linear-gradient(180deg, #fffdf9 0%, #fdf9f2 100%);
    border: 1px solid rgba(201, 162, 39, 0.2);
    border-radius: 20px;
    box-shadow: 0 12px 30px rgba(139, 69, 19, 0.08), 0 2px 8px rgba(139, 69, 19, 0.05);
}

/* 左栏：头像与身份卡 */
.identity-card {
    padding: 34px 24px;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    text-align: center;
}

.avatar-section {
    display: flex;
    flex-direction: column;
    align-items: center;
    margin-bottom: 18px;
}

.user-avatar {
    border: 4px solid #e3c88a;
    box-shadow: 0 10px 26px rgba(201, 162, 39, 0.25);
    margin-bottom: 16px;
    transition: transform 0.3s ease;
}

.user-avatar:hover {
    transform: scale(1.04);
}

.change-avatar-btn {
    background: linear-gradient(135deg, #fdf3dd, #f6e6c4);
    color: #a67c1a;
    border: 1px solid rgba(201, 162, 39, 0.45);
    border-radius: 10px;
    transition: all 0.3s ease;
}

.change-avatar-btn:hover {
    background: linear-gradient(135deg, #d8b467, #c9a227);
    color: #ffffff;
    border-color: #c9a227;
    box-shadow: 0 6px 16px rgba(201, 162, 39, 0.35);
}

.identity-name {
    font-size: 20px;
    font-weight: 600;
    color: #4a3728;
    margin: 6px 0 20px;
    letter-spacing: 1px;
}

.identity-meta {
    width: 100%;
    border-top: 1px dashed rgba(201, 162, 39, 0.32);
    padding-top: 18px;
}

/* ========== 信息行通用 ========== */
.info-section {
    width: 100%;
}

.info-item {
    display: flex;
    align-items: center;
    gap: 10px;
    font-size: 14px;
    padding: 11px 0;
    border-bottom: 1px solid rgba(201, 162, 39, 0.12);
}

.info-item:last-child {
    border-bottom: none;
}

.label {
    flex-shrink: 0;
    min-width: 78px;
    color: #9a8b72;
    font-weight: 500;
}

.value {
    color: #4a3728;
    flex: 1;
    text-align: right;
    word-break: break-all;
}

/* 右栏：账户信息卡 */
.info-card {
    padding: 30px 34px;
    display: flex;
    flex-direction: column;
}

.info-card-header {
    display: flex;
    align-items: center;
    gap: 12px;
    padding-bottom: 18px;
    border-bottom: 2px solid rgba(201, 162, 39, 0.2);
}

.info-card-icon {
    font-size: 22px;
}

.info-card-title {
    font-size: 19px;
    font-weight: 600;
    color: #4a3728;
    margin: 0;
    letter-spacing: 1px;
}

.info-list {
    flex: 1;
    margin-top: 8px;
}

/* 右栏 el-input 暖色调 */
.info-item :deep(.el-input) {
    flex: 1;
    min-width: 0;
}

.info-item :deep(.el-input__wrapper) {
    background: #fffaf1;
    border-radius: 10px;
    box-shadow: 0 0 0 1px rgba(201, 162, 39, 0.3) inset;
    transition: all 0.3s ease;
}

.info-item :deep(.el-input__wrapper.is-focus) {
    box-shadow: 0 0 0 1px #c9a227 inset, 0 0 0 4px rgba(201, 162, 39, 0.12);
}

.info-item :deep(.el-input__wrapper.is-disabled) {
    background: #f7f2ea;
    box-shadow: 0 0 0 1px rgba(201, 162, 39, 0.15) inset;
}

.info-item :deep(.el-input__inner) {
    color: #4a3728;
}

.info-item :deep(.el-input.is-disabled .el-input__inner) {
    color: #8a7a63;
    -webkit-text-fill-color: #8a7a63;
}

/* el-tag 圆角更精致 */
.info-item :deep(.el-tag) {
    border: none;
    border-radius: 999px;
    font-weight: 500;
}

/* ========== 操作按钮区 ========== */
.action-section {
    display: flex;
    flex-wrap: wrap;
    gap: 12px;
    margin-top: 20px;
    padding-top: 22px;
    border-top: 1px dashed rgba(201, 162, 39, 0.28);
}

.action-section :deep(.el-button) {
    height: 40px;
    padding: 0 20px;
    border-radius: 12px;
    font-weight: 500;
    transition: all 0.3s ease;
}

.edit-btn {
    background: linear-gradient(135deg, #d8b467, #c9a227);
    border-color: #c9a227;
    color: #ffffff;
    box-shadow: 0 6px 16px rgba(201, 162, 39, 0.28);
}

.edit-btn:hover {
    background: linear-gradient(135deg, #c9a227, #a67c1a);
    border-color: #a67c1a;
    color: #ffffff;
    transform: translateY(-2px);
}

.save-btn {
    background: linear-gradient(135deg, #7fae6a, #5d9450);
    border-color: #5d9450;
    color: #ffffff;
    box-shadow: 0 6px 16px rgba(93, 148, 80, 0.24);
}

.save-btn:hover {
    background: linear-gradient(135deg, #5d9450, #4a7c3f);
    border-color: #4a7c3f;
    color: #ffffff;
    transform: translateY(-2px);
}

.password-btn {
    background: #fffdf7;
    color: #8B4513;
    border: 1px solid rgba(201, 162, 39, 0.4);
}

.password-btn:hover {
    background: #f7eeda;
    color: #a67c1a;
    border-color: #d8b467;
    transform: translateY(-2px);
}

.ai-assistant-btn {
    background: linear-gradient(135deg, #8B4513, #6b3410);
    border-color: #6b3410;
    color: #ffffff;
    box-shadow: 0 6px 16px rgba(139, 69, 19, 0.22);
}

.ai-assistant-btn:hover {
    background: linear-gradient(135deg, #6b3410, #53280c);
    border-color: #53280c;
    color: #ffffff;
    transform: translateY(-2px);
}

/* ========== 弹窗（暖色统一） ========== */
:deep(.el-dialog) {
    background: linear-gradient(180deg, #fffdf9, #faf5ec);
    border: 1px solid rgba(201, 162, 39, 0.25);
    border-radius: 18px;
    box-shadow: 0 20px 50px rgba(139, 69, 19, 0.15);
}

:deep(.el-dialog__header) {
    margin: 0;
    padding: 18px 24px;
    border-bottom: 1px solid rgba(201, 162, 39, 0.2);
}

:deep(.el-dialog__title) {
    color: #4a3728;
    font-weight: 600;
    letter-spacing: 1px;
}

:deep(.el-dialog__body) {
    padding: 22px 24px;
}

:deep(.el-dialog .el-input__wrapper) {
    background: #fffaf1;
    border-radius: 10px;
    box-shadow: 0 0 0 1px rgba(201, 162, 39, 0.3) inset;
}

:deep(.el-dialog .el-button) {
    border-radius: 10px;
}

:deep(.el-dialog .el-button--primary) {
    background: linear-gradient(135deg, #d8b467, #c9a227);
    border-color: #c9a227;
}

/* 加载弹窗样式 */
.loading-content {
    display: flex;
    flex-direction: column;
    align-items: center;
    padding: 20px 0;
    color: #8a7a63;
}

.loading-content p {
    margin-top: 15px;
    color: #9a8b72;
}

/* ========== 响应式 ========== */
@media (max-width: 900px) {
    .profile-grid {
        grid-template-columns: 1fr;
    }
    .main-content {
        margin-top: -42px;
    }
    .hero-title {
        font-size: 36px;
        letter-spacing: 5px;
    }
    .info-card {
        padding: 26px 22px;
    }
}

@media (max-width: 560px) {
    .hero-title {
        font-size: 30px;
        letter-spacing: 3px;
    }
    .hero-subtitle {
        font-size: 13px;
        letter-spacing: 1px;
    }
    .info-item {
        flex-direction: column;
        align-items: flex-start;
        gap: 6px;
    }
    .value {
        text-align: left;
    }
    .action-section :deep(.el-button) {
        flex: 1;
    }
}
</style>