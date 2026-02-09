<template>
  <div class="auth-container">
    <!-- 登录注册卡片 -->
    <div class="auth-card">
      <!-- Logo区域 -->
      <div class="logo-wrapper">
        <img src="@/assets/logo.jpg" alt="华夏天澜" class="logo" v-if="logoExists" @error="logoExists = false">
        <div class="logo-placeholder" v-else>华夏天澜</div>
        <h2 class="title">{{ isLogin ? '用户登录' : '用户注册' }}</h2>
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
          <el-input v-model="form.password" placeholder="请输入密码" prefix-icon="Lock" clearable show-password />
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
import { ref, reactive } from 'vue';
import { ElForm, ElFormItem, ElInput, ElButton, ElMessage } from 'element-plus';
// 只导入对外暴露的公开类型
import type { FormInstance, FormRules } from 'element-plus';
import { register, login } from '@/api/user';
import { useRouter } from 'vue-router';

const router = useRouter()

// 表单引用
const authFormRef = ref<FormInstance>();
// 提交加载状态
const submitLoading = ref(false);
// 切换登录/注册
const isLogin = ref(true);
// Logo是否存在
const logoExists = ref(true);

// 定义表单数据接口
interface AuthForm {
  username: string;
  email: string;
  password: string;
  confirmPassword: string;
}

// 表单数据
const form = reactive<AuthForm>({
  username: '',
  email: '',
  password: '',
  confirmPassword: ''
});

// 表单验证规则
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

// 切换登录/注册类型
const toggleAuthType = () => {
  isLogin.value = !isLogin.value;
  // 切换时重置表单
  authFormRef.value?.resetFields();
};

// 提交表单
const handleSubmit = async () => {
  if (!authFormRef.value) return;

  try {
    // 表单验证
    await authFormRef.value.validate();
    submitLoading.value = true;

    let response;
    if (isLogin.value) {
      // 登录逻辑
      response = await login({
        username: form.username,
        passwordHash: form.password
      });
      console.log('登录接口返回数据：', response.data);
      if (response.data.code === 200) {
        localStorage.setItem("token", response.data.data.token)
        router.push('/home')
        ElMessage.success('登录成功');
      }
      else {
        ElMessage.error("登录失败，" + response.data.msg)
      }
    } else {
      // 注册逻辑
      response = await register({
        username: form.username,
        email: form.email,
        passwordHash: form.password
      });
      console.log('注册接口返回数据：', response.data);
      ElMessage.success('注册成功');
      toggleAuthType();
    }
  } catch (error) {
    console.error('请求失败：', error);
    ElMessage.error('操作失败，请稍后重试');
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
}

/* Logo区域 */
.logo-wrapper {
  text-align: center;
  margin-bottom: 30px;
}

.logo {
  width: 80px;
  height: 80px;
  object-fit: contain;
  margin-bottom: 15px;
}

.logo-placeholder {
  width: 80px;
  height: 80px;
  line-height: 80px;
  margin: 0 auto 15px;
  background: linear-gradient(90deg, #4361ee, #3a0ca3);
  color: white;
  border-radius: 50%;
  font-size: 16px;
  font-weight: bold;
}

.title {
  font-size: 24px;
  color: #1e293b;
  font-weight: 600;
  margin: 0;
}

/* 表单样式 */
.auth-form {
  width: 100%;
}

.el-form-item {
  margin-bottom: 20px;
}

/* 按钮样式 */
.btn-item {
  margin-bottom: 15px;
}

.submit-btn {
  width: 100%;
  height: 48px;
  font-size: 16px;
  background: linear-gradient(90deg, #4361ee, #3a0ca3);
  border: none;
  border-radius: 8px;
  transition: all 0.3s ease;
}

.submit-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 16px rgba(67, 97, 238, 0.3);
}

/* 切换按钮 */
.switch-btn {
  text-align: center;
  color: #64748b;
  font-size: 14px;
}

.link-btn {
  color: #4361ee;
  font-weight: 500;
  padding: 0;
}

.link-btn:hover {
  color: #3a0ca3;
}

/* 响应式适配 */
@media (max-width: 480px) {
  .auth-card {
    width: 90%;
    padding: 30px 20px;
  }
}
</style>