<template>
  <div class="login-container">
    <!-- Background section -->
    <div class="background-section">
      <div class="bg-gradient"></div>
      <div class="grid-pattern"></div>
      <div class="illustration">
        <svg viewBox="0 0 200 200" xmlns="http://www.w3.org/2000/svg">
          <path fill="#409EFF" fill-opacity="0.1" d="M44.3,-63.9C56.5,-50.7,64.2,-30.7,66.4,-8.9C68.6,12.9,65.3,36.5,54.2,50.3C43.1,64.1,24.2,68.1,3.4,68.3C-17.4,68.5,-34.8,64.9,-47.4,54.5C-60,44.1,-67.8,26.9,-68.1,8.2C-68.4,-10.5,-61.2,-27.9,-49.2,-40.6C-37.2,-53.3,-20.4,-61.3,-2.1,-61.8C16.2,-62.3,32.4,-55.3,44.3,-63.9Z" transform="translate(100 100)" />
        </svg>
      </div>
      <div class="copyright">© 2026 ColdChain Guardian. All rights reserved.</div>
    </div>

    <!-- Login card floating above the background -->
    <div class="login-card-overlay">
      <div class="login-card">
        <!-- Brand section -->
        <div class="brand-section">
          <div class="logo">
            <svg viewBox="0 0 24 24" width="48" height="48" fill="none" xmlns="http://www.w3.org/2000/svg">
              <path d="M12 2L13.09 8.26L22 9L13.09 9.74L12 16L10.91 9.74L2 9L10.91 8.26L12 2Z" fill="#409EFF"/>
              <circle cx="12" cy="12" r="10" stroke="#409EFF" stroke-width="2"/>
            </svg>
          </div>
          <h1 class="title">ColdChain Guardian</h1>
          <h2 class="subtitle">冷链仓储安全管理系统</h2>
          <p class="tagline">实时监测 · 异常预警 · 工单闭环 · AI 辅助</p>
        </div>

        <!-- Form section -->
        <div class="form-section">
          <el-form
            ref="loginFormRef"
            :model="loginForm"
            :rules="loginRules"
            @submit.native.prevent
            size="large"
            label-position="top"
          >
            <!-- Username field -->
            <el-form-item prop="username" label="用户名">
              <el-input
                v-model="loginForm.username"
                placeholder="请输入管理员账号"
                prefix-icon="User"
                @keypress.enter.prevent="handleLogin"
              />
            </el-form-item>

            <!-- Password field -->
            <el-form-item prop="password" label="密码">
              <el-input
                v-model="loginForm.password"
                type="password"
                placeholder="请输入密码"
                prefix-icon="Lock"
                show-password
                @keypress.enter.prevent="handleLogin"
              />
            </el-form-item>

            <!-- Remember me checkbox -->
            <el-form-item class="remember-me">
              <el-checkbox v-model="loginForm.rememberMe">7天内免登录</el-checkbox>
            </el-form-item>

            <!-- Submit button -->
            <el-form-item>
              <el-button
                type="primary"
                :loading="loading"
                @click="handleLogin"
                class="login-button"
                :disabled="loading"
              >
                <span v-if="!loading">登录</span>
                <span v-else>登录中...</span>
              </el-button>
            </el-form-item>
          </el-form>

          <!-- Footer links -->
          <div class="footer-links">
            <span class="left-link" @click="handleForgotPassword">忘记密码？</span>
            <span class="right-link" @click="handleContactAdmin">联系超级管理员</span>
          </div>

          <!-- Error message -->
          <el-alert
            v-if="errorMessage"
            :title="errorMessage"
            type="error"
            show-icon
            :closable="true"
            @close="errorMessage = ''"
            class="error-alert"
          />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useAuthStore } from '@/store/auth'
import { login as loginApi } from '@/utils/auth'

const router = useRouter()
const authStore = useAuthStore()

// Form data
const loginForm = reactive({
  username: '',
  password: '',
  rememberMe: false
})

// Validation rules
const loginRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 30, message: '长度在 3 到 30 个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 32, message: '长度在 6 到 32 个字符', trigger: 'blur' }
  ]
}

// Component references
const loginFormRef = ref(null)

// State
const loading = ref(false)
const errorMessage = ref('')

// Handle login
const handleLogin = async (event) => {
  // Prevent any kind of form submission that might cause refresh
  if (event) {
    event.preventDefault();
    event.stopPropagation();
  }

  // Validate form
  if (!loginFormRef.value) return

  try {
    await loginFormRef.value.validate()
  } catch {
    return
  }

  // Disable button and show loading
  loading.value = true
  errorMessage.value = ''

  try {
    // Call login API
    const response = await loginApi({
      username: loginForm.username,
      password: loginForm.password,
      rememberMe: loginForm.rememberMe
    })

    const { data } = response.data

    // Store authentication data
    authStore.setAuthData(data, loginForm.rememberMe)

    // Show success message
    ElMessage.success('登录成功')

    // Redirect based on role
    const userRole = authStore.getUserRole
    if (userRole === 'SUPER_ADMIN') {
      await router.push('/dashboard')
    } else {
      await router.push('/dashboard')
    }
  } catch (error) {
    // Handle different error types
    let errorMsg = '登录失败，请重试'

    if (error.response) {
      const { code, msg } = error.response.data

      switch (code) {
        case 401:
          errorMsg = '用户名或密码错误，请重试'
          break
        case 403:
          errorMsg = '账号已被禁用，请联系超级管理员'
          break
        case 400:
          errorMsg = msg || '请求参数错误'
          break
        case 500:
          errorMsg = '服务器内部错误，请稍后再试'
          break
        default:
          errorMsg = msg || '登录失败，请重试'
      }
    } else if (error.code === 'ECONNABORTED' || error.message.includes('timeout')) {
      errorMsg = '网络超时，请检查网络连接'
    } else {
      errorMsg = '网络异常，无法连接服务器'
    }

    errorMessage.value = errorMsg
    ElMessage.error(errorMsg)
  } finally {
    loading.value = false
  }
}

// Handle footer link clicks
const handleForgotPassword = () => {
  ElMessage.info('请联系超级管理员重置密码')
}

const handleContactAdmin = () => {
  ElMessage.info('请联系超级管理员获取帮助')
}
</script>

<style scoped>
.login-container {
  position: fixed;
  top: 0;
  left: 0;
  width: 100vw;
  height: 100vh;
  overflow: hidden;
}

.background-section {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  padding: 24px;
  z-index: 1;
}

.bg-gradient {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: linear-gradient(135deg, #e6f7ff 0%, #ffffff 100%);
  z-index: 1;
}

.grid-pattern {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-image:
    linear-gradient(rgba(64, 158, 255, 0.05) 1px, transparent 1px),
    linear-gradient(90deg, rgba(64, 158, 255, 0.05) 1px, transparent 1px);
  background-size: 20px 20px;
  z-index: 2;
}

.illustration {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  opacity: 0.15;
  z-index: 3;
}

.copyright {
  position: absolute;
  bottom: 24px;
  left: 50%;
  transform: translateX(-50%);
  color: #909399;
  font-size: 12px;
  z-index: 4;
  text-align: center;
}

.login-card-overlay {
  position: relative;
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 10;
}

.login-card {
  width: 420px;
  min-height: 500px;
  background: white;
  border-radius: 16px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
  display: flex;
  flex-direction: column;
  padding: 40px;
  z-index: 5;
  position: relative;
}

.brand-section {
  text-align: center;
  margin-bottom: 40px;
}

.logo {
  margin: 0 auto 16px;
}

.title {
  font-size: 24px;
  font-weight: bold;
  color: #303133;
  margin: 0 0 8px 0;
}

.subtitle {
  font-size: 14px;
  color: #909399;
  margin: 0 0 16px 0;
}

.tagline {
  font-size: 12px;
  color: #c0c4cc;
  margin: 0;
}

.form-section {
  flex: 1;
}

.remember-me {
  margin-bottom: 24px !important;
}

.login-button {
  width: 100%;
  height: 44px;
  font-size: 16px;
  letter-spacing: 1px;
}

.footer-links {
  display: flex;
  justify-content: space-between;
  margin-top: 20px;
  font-size: 14px;
}

.left-link, .right-link {
  color: #409EFF;
  cursor: pointer;
  transition: color 0.3s;
}

.left-link:hover, .right-link:hover {
  color: #3a7bc8;
}

.error-alert {
  margin-top: 20px;
}

/* Responsive design */
@media (max-width: 768px) {
  .login-container {
    position: fixed;
    top: 0;
    left: 0;
    width: 100vw;
    height: 100vh;
  }

  .background-section {
    display: none;
  }

  .login-card-overlay {
    display: flex;
    align-items: center;
    justify-content: center;
  }

  .login-card {
    width: 90%;
    max-width: 420px;
    margin: 0 20px;
    padding: 30px 20px;
  }
}
</style>