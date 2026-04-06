<template>
    <div class="user-center-container">
        <!-- 顶部导航栏 -->
        <div class="header">
            <el-button class="back-btn" @click="router.back()">
                <el-icon>
                    <Back />
                </el-icon>
                <span>返回</span>
            </el-button>
            <h2 class="page-title">个人中心</h2>
        </div>

        <!-- 主体内容区 -->
        <div class="main-content">
            <!-- 用户信息卡片 -->
            <div class="user-card">
                <!-- 头像区域 -->
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

                <!-- 用户信息区域 -->
                <div class="info-section">
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
                        <span class="value">{{ userInfo.username || '加载中...' }}</span>
                    </div>
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
                    <el-button @click="dialogVisible=!dialogVisible">
                        <el-icon><Unlock /></el-icon>
                        <span>修改密码</span>
                    </el-button>
                    <el-button class="ai-assistant-btn" @click="openAIAssistant">
                        <el-icon><ChatDotRound /></el-icon>
                        <span>AI 历史助手</span>
                    </el-button>
                </div>
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
        <el-dialog v-model="dialogVisible" title="修改密码" width="30%">
            <div>
                <el-input v-model="newPassword" placeholder="请输入新密码" show-password style="margin-bottom: 20px;" />
                <el-input v-model="confirmPassword" placeholder="请再次输入新密码" show-password style="margin-bottom: 20px;" />
                <el-button type="primary" @click="savePassword" style="float: right;">确认修改</el-button>
            </div>
        </el-dialog>
    </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { getUserById, updateUserById } from '@/api/user';
import { useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import { Loading, Back, Camera, Edit, Check, Unlock, ChatDotRound } from '@element-plus/icons-vue';
import { uploadFile } from '@/api/file';
import { BASE_URL } from '@/utils/request';

const newPassword = ref('');
const confirmPassword = ref('');
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
                avatarUrl.value = `${BASE_URL}${avatarPath}`;
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

// 保存密码
const savePassword = async () => {
    if (newPassword.value !== confirmPassword.value) {
        ElMessage.error('两次输入的密码不一致');
        return;
    }
    if (newPassword.value.length < 6) {
        ElMessage.error('密码长度不能少于6位');
        return;
    }
    try {
        const res = await updateUserById({
            id: userId.value,
            passwordHash: newPassword.value
        });
        if (res.data.code === 200) {
            ElMessage.success('密码修改成功，请重新登录');
            dialogVisible.value = false;
            localStorage.removeItem('token');
            localStorage.removeItem('userInfo');
            setTimeout(() => {
                router.push('/login');
            }, 1500);
        } else {
            ElMessage.error('密码修改失败：' + res.data.msg);
        }
    } catch (error) {
        console.error('修改密码失败:', error);
        ElMessage.error('修改密码失败，请稍后重试');
    }
    newPassword.value = '';
    confirmPassword.value = '';
};

// 保存用户信息
const saveUserInfo = async () => {
    try {
        const response = await updateUserById({
            id: userId.value,
            email: userInfo.value.email
        });
        if (response.data.code === 200) {
            ElMessage.success('用户信息更新成功');
            isEditing.value = false;
            const userInfoStr = localStorage.getItem('userInfo');
            if (userInfoStr) {
                const userData = JSON.parse(userInfoStr);
                userData.email = userInfo.value.email;
                localStorage.setItem('userInfo', JSON.stringify(userData));
            }
        } else {
            ElMessage.error('更新失败：' + response.data.msg);
        }
    } catch (error) {
        console.error("更新用户信息失败:", error);
        ElMessage.error('更新用户信息失败，请稍后重试');
    }
};

// 获取用户信息
const fetchUserInfo = async () => {
    try {
        loading.value = true;
        
        const userInfoStr = localStorage.getItem('userInfo');
        if (userInfoStr) {
            const savedUserInfo = JSON.parse(userInfoStr);
            userId.value = savedUserInfo.userId || savedUserInfo.id;
        }

        if (!userId.value) {
            const token = localStorage.getItem("token");
            if (token) {
                try {
                    const payload = JSON.parse(atob(token.split('.')[1]));
                    userId.value = payload.userId || payload.user_id;
                } catch (e) {
                    console.error('解析 token 失败:', e);
                }
            }
        }

        if (!userId.value) {
            ElMessage.error('无法获取用户ID，请重新登录');
            router.push('/login');
            loading.value = false;
            return;
        }

        const response = await getUserById(userId.value);
        console.log('用户详情接口返回:', response);

        if (response.data.code === 200) {
            const user = response.data.data;
            console.log('用户数据:', user);
            
            if (user.avatarPath) {
                avatarUrl.value = `${BASE_URL}${user.avatarPath}`;
            } else {
                avatarUrl.value = 'https://cdn.pixabay.com/photo/2015/10/05/22/37/blank-profile-picture-973460_960_720.png';
            }
            
            userInfo.value = {
                username: user.username || '未知',
                email: user.email || '未设置',
                createdTime: formatDate(user.createdTime),
                lastLoginTime: formatDate(user.lastLoginTime),
                updateTime: formatDate(user.updatedTime),
                status: user.status ?? 1,
                role: user.role || 'user'
            };
            
            console.log('赋值后的 userInfo:', userInfo.value);
            ElMessage.success('用户信息加载成功');
        } else {
            ElMessage.error('获取用户信息失败：' + response.data.msg);
        }
    } catch (error) {
        console.error("获取用户信息失败:", error);
        ElMessage.error('获取用户信息失败，请稍后重试');
    } finally {
        loading.value = false;
    }
};

onMounted(() => {
    fetchUserInfo();
});
</script>

<style scoped>
/* 全局容器样式 */
.user-center-container {
    width: 100%;
    min-height: 100vh;
    background-color: #FFF9E8;
    padding: 0;
    margin: 0;
    box-sizing: border-box;
}

/* 顶部导航栏 */
.header {
    width: 100%;
    height: 70px;
    background-color: #F7BA1E;
    display: flex;
    align-items: center;
    padding: 0 40px;
    box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
    box-sizing: border-box;
}

.back-btn {
    background-color: #FFFFFF;
    color: #F7BA1E;
    border: 1px solid #F7BA1E;
    margin-right: 30px;
    transition: all 0.3s ease;
}

.back-btn:hover {
    background-color: #FFF3CD;
    color: #D69E00;
    border-color: #D69E00;
}

.page-title {
    color: #FFFFFF;
    font-size: 20px;
    font-weight: 600;
    margin: 0;
}

/* 主体内容区 */
.main-content {
    width: 100%;
    padding: 50px 100px;
    display: flex;
    justify-content: center;
    box-sizing: border-box;
}

/* 用户信息卡片 */
.user-card {
    width: 400px;
    background-color: #FFFFFF;
    border-radius: 12px;
    padding: 40px;
    box-shadow: 0 4px 20px rgba(247, 186, 30, 0.15);
    border: 1px solid #FFE485;
    display: flex;
    flex-direction: column;
    align-items: center;
}

/* 头像区域 */
.avatar-section {
    margin-bottom: 30px;
    display: flex;
    flex-direction: column;
    align-items: center;
}

.user-avatar {
    border: 4px solid #F7BA1E;
    margin-bottom: 15px;
    transition: transform 0.3s ease;
}

.user-avatar:hover {
    transform: scale(1.05);
}

.change-avatar-btn {
    background-color: #FFF3CD;
    color: #F7BA1E;
    border: 1px solid #F7BA1E;
}

.change-avatar-btn:hover {
    background-color: #F7BA1E;
    color: #FFFFFF;
}

/* 信息展示区域 */
.info-section {
    width: 100%;
    margin-bottom: 30px;
    border-top: 1px solid #FFE485;
    border-bottom: 1px solid #FFE485;
    padding: 20px 0;
}

.info-item {
    display: flex;
    align-items: center;
    margin-bottom: 15px;
    font-size: 14px;
}

.info-item:last-child {
    margin-bottom: 0;
}

.label {
    width: 80px;
    color: #666666;
    font-weight: 500;
}

.value {
    color: #333333;
    flex: 1;
}

/* 操作按钮区域 */
.action-section {
    width: 100%;
    display: flex;
    justify-content: center;
    gap: 15px;
    flex-wrap: wrap;
}

.edit-btn {
    background-color: #F7BA1E;
    border-color: #F7BA1E;
}

.edit-btn:hover {
    background-color: #D69E00;
    border-color: #D69E00;
}

.ai-assistant-btn {
    background-color: #8B4513;
    border-color: #8B4513;
    color: white;
}

.ai-assistant-btn:hover {
    background-color: #6B3410;
    border-color: #6B3410;
}

/* 加载弹窗样式 */
.loading-content {
    display: flex;
    flex-direction: column;
    align-items: center;
    padding: 20px 0;
}

.loading-content p {
    margin-top: 15px;
    color: #666666;
}
</style>