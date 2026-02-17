<template>
    <div class="user-center-container">
        <!-- 顶部导航栏 -->
        <div class="header">
            <el-button class="back-btn" @click="router.back()">
                <el-icon>
                    <back />
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
                    <el-avatar :size="120" :src="BASE_URL+avatar_url || 'https://cdn.pixabay.com/photo/2015/10/05/22/37/blank-profile-picture-973460_960_720.png'" class="user-avatar"/>
                    <input ref="avatarFileInput" type="file" accept="image/*" @change="handleAvatarChange" style="display: none;" />
                    <el-button class="change-avatar-btn" size="small" @click="triggerFileInput">
                        <el-icon>
                            <camera />
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
                        <el-icon><edit /></el-icon>
                        <span>修改邮箱</span>
                    </el-button>
                    <el-button v-else class="save-btn" type="success" @click="saveUserInfo">
                        <el-icon><check /></el-icon>
                        <span>保存修改</span>
                    </el-button>
                    <el-button @click="dialogVisible=!dialogVisible">
                        <el-icon><unlock /></el-icon>
                        <span>修改密码</span>
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
        <el-dialog v-model="dialogVisible" title="修改密码" width="30%" style="height: 210px;">
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
import { ElMessage, ElMessageBox } from 'element-plus';
import { formatIsoDate } from '@/utils/date';
import { Loading } from '@element-plus/icons-vue';
import { uploadFile } from '@/api/file';
import { BASE_URL } from '@/utils/request';
const newPassword = ref('');
const confirmPassword = ref('');
const router = useRouter();
// 文件选择框的引用
const avatarFileInput = ref<HTMLInputElement>();
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

    // 校验文件类型（只允许图片）
    if (!file.type.startsWith('image/')) {
        ElMessage.error('请选择图片格式的文件');
        target.value = ''; // 清空选择
        return;
    }

    try {
        // 1. 上传头像文件
        const uploadRes = await uploadFile({
            file: file,
            resourceType: '图片' // 对应接口的resourceType参数
        });

        if (uploadRes.data.code === 200) {
            const avatarPath = uploadRes.data.data;
            // 2. 更新用户头像到数据库
            const updateRes = await updateUserById({
                id: userId.value,
                avatarPath: avatarPath
            });

            if (updateRes.data.code === 200) {
                ElMessage.success('头像更换成功');
                avatar_url.value = avatarPath; // 更新页面显示的头像
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
        target.value = ''; // 清空文件选择框，方便再次选择同一文件
    }
};
// 头像地址
const avatar_url = ref('');
// 用户ID
const userId = ref(0);
// 加载状态
const loading = ref(true);
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
const savePassword = () => {
    if (newPassword.value !== confirmPassword.value) {
        ElMessage.error('两次输入的密码不一致');
        return;
    }
    const res = updateUserById({
        id: userId.value,
        passwordHash: newPassword.value
    });
    res.then((res)=>{
        if(res.data.code === 200){
            ElMessage.success('密码修改成功');
        }else{
            ElMessage.error('密码修改失败：' + res.data.msg);
        }
    })
    dialogVisible.value = false;
}
const saveUserInfo = async () => {
    try {
        const response = await updateUserById({
            id: userId.value,
            email: userInfo.value.email
        });
        if (response.data.code === 200) {
            ElMessage.success('用户信息更新成功');
            isEditing.value = false;
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
        const token = localStorage.getItem("token");

        // 无token跳转到登录页
        if (!token) {
            router.push('/login');
            ElMessage.warning('请先登录');
            return;
        }

        // 解析token获取用户ID
        const payload = JSON.parse(atob(token.split('.')[1]));
        userId.value = payload.userId;

        // 请求用户详情
        const response = await getUserById(userId.value);
        if (response.data.code === 200) {
            const user = response.data.data;
            // 更新头像
            avatar_url.value = user.avatarPath;
            // 更新最后登录时间（如果接口返回）
            userInfo.value = {
                username: user.username,
                email: user.email,
                createdTime: user.createdTime ? formatIsoDate(user.createdTime) : '暂无数据',
                lastLoginTime: user.lastLoginTime ? formatIsoDate(user.lastLoginTime) : '暂无数据',
                updateTime: user.updatedTime ? formatIsoDate(user.updatedTime) : '暂无数据',
                status: user.status,
                role: user.role
            };
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

// 组件挂载时加载用户信息
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
    /* 浅黄色背景，提升整体柔和度 */
    padding: 0;
    margin: 0;
    box-sizing: border-box;
}

/* 顶部导航栏 */
.header {
    width: 100%;
    height: 70px;
    background-color: #F7BA1E;
    /* 主黄色 */
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
    width: 300px;
    background-color: #FFFFFF;
    border-radius: 12px;
    padding: 40px;
    box-shadow: 0 4px 20px rgba(247, 186, 30, 0.15);
    /* 黄色系阴影 */
    border: 1px solid #FFE485;
    /* 浅黄边框 */
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
    /* 黄色头像边框 */
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
    font-size: 16px;
}

.info-item:last-child {
    margin-bottom: 0;
}

.label {
    width: 100px;
    color: #666666;
    font-weight: 500;
}

.value {
    color: #333333;
}

/* 操作按钮区域 */
.action-section {
    width: 100%;
    display: flex;
    justify-content: center;
    gap: 20px;
}

.edit-btn {
    background-color: #F7BA1E;
    border-color: #F7BA1E;
}

.edit-btn:hover {
    background-color: #D69E00;
    border-color: #D69E00;
}

.security-btn {
    background-color: #FFFFFF;
    color: #F7BA1E;
    border: 1px solid #F7BA1E;
}

.security-btn:hover {
    background-color: #FFF3CD;
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