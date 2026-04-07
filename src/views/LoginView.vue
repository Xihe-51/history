<template>
  <div class="auth-container">
    <!-- 登录注册卡片 -->
    <div class="auth-card">
      <!-- Logo区域 -->
      <div class="logo-wrapper">
        <h2 class="title">华夏天澜</h2>
      </div>

      <!-- 表单区域 -->
      <el-form ref="authFormRef" :model="form" :rules="rules" class="auth-form" size="large">
        <!-- 用户名 -->
        <el-form-item prop="username">
          <el-input v-model="form.username" placeholder="请输入用户名" prefix-icon="User" clearable />
        </el-form-item>

        <!-- 邮箱 (仅注册显示) -->
        <el-form-item v-if="!isLogin" prop="email">
          <el-input v-model="form.email" placeholder="请输入邮箱" prefix-icon="Message" clearable type="email" />
        </el-form-item>

        <!-- 密码 -->
        <el-form-item prop="password">
          <el-input 
            v-model="form.password" 
            placeholder="请输入密码" 
            @keyup.enter="handleSubmit" 
            prefix-icon="Lock" 
            clearable 
            show-password 
          />
        </el-form-item>

        <!-- 确认密码 (仅注册显示) -->
        <el-form-item v-if="!isLogin" prop="confirmPassword">
          <el-input v-model="form.confirmPassword" placeholder="请确认密码" prefix-icon="Lock" clearable show-password />
        </el-form-item>

        <!-- 提交按钮 -->
        <el-form-item class="btn-item">
          <el-button type="primary" class="submit-btn" @click="handleSubmit" :loading="submitLoading">
            {{ isLogin ? '登录' : '注册' }}
          </el-button>
        </el-form-item>

        <!-- 切换登录/注册 -->
        <div class="switch-btn">
          <span>
            {{ isLogin ? '还没有账号？' : '已有账号？' }}
            <el-button type="text" @click="toggleAuthType" class="link-btn">
              {{ isLogin ? '立即注册' : '立即登录' }}
            </el-button>
          </span>
        </div>
      </el-form>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue';
import { ElForm, ElFormItem, ElInput, ElButton, ElMessage } from 'element-plus';
import type { FormInstance, FormRules } from 'element-plus';
import { register, login } from '@/api/user';
import { useRouter } from 'vue-router';

const router = useRouter();

onMounted(() => {
  let token = localStorage.getItem("token");
  if (token) {
    window.location.href = '/timeline';
  }
});

const authFormRef = ref<FormInstance>();
const submitLoading = ref(false);
const isLogin = ref(true);

interface AuthForm {
  username: string;
  email: string;
  password: string;
  confirmPassword: string;
}

const form = reactive<AuthForm>({
  username: '',
  email: '',
  password: '',
  confirmPassword: ''
});

const rules = reactive<FormRules>({
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度在 3 到 20 个字符', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email' as const, message: '请输入正确的邮箱格式', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    {
      validator: (rule: any, value: string, callback: (error?: string | Error | undefined) => void) => {
        if (value && value !== form.password) {
          callback(new Error('两次输入密码不一致!'));
        } else {
          callback();
        }
      },
      trigger: 'blur'
    }
  ]
});

const toggleAuthType = () => {
  isLogin.value = !isLogin.value;
  authFormRef.value?.resetFields();
};

const handleSubmit = async () => {
  if (!authFormRef.value) return;

  try {
    await authFormRef.value.validate();
    submitLoading.value = true;

    let response;
    if (isLogin.value) {
      response = await login({
        username: form.username,
        passwordHash: form.password
      });
      console.log('登录接口返回数据：', response);

      if (response.data.code === 200) {
        if (response.data.data && response.data.data.token) {
          localStorage.setItem("token", response.data.data.token);
        }
        if (response.data.data) {
          localStorage.setItem("userInfo", JSON.stringify(response.data.data));
        }
        
        const userRole = response.data.data?.role;
        ElMessage.success(response.data.msg || '登录成功');
        
        setTimeout(() => {
          if (userRole === "admin") {
            window.location.href = '/admin';
          } else {
            window.location.href = '/timeline';
          }
        }, 100);
      } else {
        ElMessage.error(response.data.msg || '登录失败，请检查用户名或密码');
      }
    } else {
      response = await register({
        username: form.username,
        email: form.email,
        passwordHash: form.password
      });
      console.log('注册接口返回数据：', response);
      
      if (response.data.code === 200) {
        ElMessage.success(response.data.msg || '注册成功');
        toggleAuthType();
      } else {
        ElMessage.error(response.data.msg || '注册失败，请稍后重试');
      }
    }
  } catch (error: any) {
    console.error('请求失败：', error);
    
    if (error.response && error.response.data) {
      const errorMsg = error.response.data.msg || `请求失败: ${error.response.status}`;
      ElMessage.error(errorMsg);
    } else if (error.request) {
      ElMessage.error('网络错误');
    } else {
      ElMessage.error(error.message || '操作失败，请稍后重试');
    }
  } finally {
    submitLoading.value = false;
  }
};
</script>

<style scoped>
/* 整体容器 */
.auth-container {
  width: 100%;
  height: 100%;
  position: relative;
  overflow: hidden;
  background: url("@/assets/background.png") no-repeat;
  background-size: cover;
}

/* 登录注册卡片 */
.auth-card {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 320px;
  background: rgba(255, 255, 255, 0.5);
  border-radius: 16px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.15);
  padding: 40px;
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.1);
  z-index: 10;
  animation: fadeIn 0.5s ease;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translate(-50%, -48%);
  }
  to {
    opacity: 1;
    transform: translate(-50%, -50%);
  }
}

/* Logo区域 */
.logo-wrapper {
  text-align: center;
  margin-bottom: 30px;
}

.title {
  font-size: 28px;
  color: #3b3a1e;
  font-weight: 1000;
  margin: 0;
  letter-spacing: 2px;
}

/* 表单样式 */
.auth-form {
  width: 100%;
}

:deep(.el-form-item) {
  margin-bottom: 22px;
}

:deep(.el-input__wrapper) {
  background: rgba(255, 255, 255, 0.9);
  border-radius: 8px;
  transition: all 0.3s ease;
}

:deep(.el-input__wrapper:hover) {
  transform: translateY(-1px);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

:deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 2px rgba(67, 97, 238, 0.2);
}

/* 按钮样式 */
.btn-item {
  margin-bottom: 20px;
  margin-top: 30px;
}

.submit-btn {
  width: 100%;
  height: 48px;
  font-size: 16px;
  font-weight: 600;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
  border-radius: 8px;
  transition: all 0.3s ease;
}

.submit-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 20px rgba(102, 126, 234, 0.4);
}

.submit-btn:active {
  transform: translateY(0);
}

/* 切换按钮 */
.switch-btn {
  text-align: center;
  color: #64748b;
  font-size: 14px;
}

.link-btn {
  color: #667eea;
  font-weight: 600;
  padding: 0;
  font-size: 14px;
}

.link-btn:hover {
  color: #764ba2;
}

/* 响应式适配 */
@media (max-width: 480px) {
  .auth-card {
    width: 90%;
    padding: 30px 20px;
  }
  
  .title {
    font-size: 24px;
  }
}
</style>