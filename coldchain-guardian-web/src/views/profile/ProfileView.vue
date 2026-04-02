<template>
  <Layout>
    <div class="profile-container">
      <el-row :gutter="20">
        <el-col :span="8">
          <el-card class="user-card" shadow="hover">
            <div class="avatar-wrapper">
              <el-avatar :size="100" class="user-avatar">{{ avatarText }}</el-avatar>
            </div>
            <div class="user-info-brief" v-loading="pageLoading">
              <h2 class="user-name">{{ userInfo.realName || '未命名' }}</h2>
              <p class="user-role">
                <el-tag :type="getRoleTagType(userInfo.role)" effect="dark" round>
                  {{ getRoleText(userInfo.role) }}
                </el-tag>
              </p>
            </div>
            <el-divider />
            <div class="user-details" v-loading="pageLoading">
              <div class="detail-item">
                <span class="label"><el-icon><User /></el-icon> 账号名</span>
                <span class="value">{{ userInfo.username }}</span>
              </div>
              <div class="detail-item">
                <span class="label"><el-icon><Iphone /></el-icon> 手机号码</span>
                <span class="value">{{ userInfo.phone || '未绑定' }}</span>
              </div>
              <div class="detail-item">
                <span class="label"><el-icon><CircleCheck /></el-icon> 账号状态</span>
                <span class="value">
                  <el-tag :type="userInfo.status === 1 ? 'success' : 'danger'" size="small">
                    {{ userInfo.status === 1 ? '正常' : '已停用' }}
                  </el-tag>
                </span>
              </div>
            </div>
          </el-card>
        </el-col>

        <el-col :span="16">
          <el-card shadow="hover" class="settings-card">
            <el-tabs v-model="activeTab">
              <el-tab-pane label="基本资料" name="basic">
                <el-form
                  :model="basicForm"
                  :rules="basicRules"
                  ref="basicFormRef"
                  label-width="100px"
                  class="profile-form"
                >
                  <el-form-item label="真实姓名" prop="realName">
                    <el-input v-model="basicForm.realName" placeholder="请输入真实姓名" />
                  </el-form-item>
                  <el-form-item label="手机号码" prop="phone">
                    <el-input v-model="basicForm.phone" placeholder="请输入手机号" maxlength="11" />
                  </el-form-item>
                  <el-form-item>
                    <el-button type="primary" @click="submitBasicForm" :loading="savingBasic">保存修改</el-button>
                  </el-form-item>
                </el-form>
              </el-tab-pane>

              <el-tab-pane label="修改密码" name="password">
                <el-form
                  :model="pwdForm"
                  :rules="pwdRules"
                  ref="pwdFormRef"
                  label-width="100px"
                  class="profile-form"
                >
                  <el-form-item label="原密码" prop="oldPassword">
                    <el-input type="password" v-model="pwdForm.oldPassword" placeholder="请输入当前密码" show-password />
                  </el-form-item>
                  <el-form-item label="新密码" prop="newPassword">
                    <el-input type="password" v-model="pwdForm.newPassword" placeholder="请输入新密码" show-password />
                  </el-form-item>
                  <el-form-item label="确认新密码" prop="confirmPassword">
                    <el-input type="password" v-model="pwdForm.confirmPassword" placeholder="请再次输入新密码" show-password />
                  </el-form-item>
                  <el-form-item>
                    <el-button type="primary" @click="submitPwdForm" :loading="savingPwd">确认修改</el-button>
                  </el-form-item>
                </el-form>
              </el-tab-pane>
            </el-tabs>
          </el-card>
        </el-col>
      </el-row>
    </div>
  </Layout>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { User, Iphone, CircleCheck } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import Layout from '@/components/Layout.vue'
import { useAuthStore } from '@/store/auth'
import { userApi } from '@/utils/api'

const authStore = useAuthStore()

// 🌟 1. 改为使用一个响应式对象来存储从后端拉取的用户最新数据
const userInfo = ref({
  id: '',
  username: '',
  realName: '',
  phone: '',
  role: '',
  status: 1
})

// 提取姓名的第一个字作为头像 (依赖最新获取的 userInfo)
const avatarText = computed(() => {
  return userInfo.value.realName ? userInfo.value.realName.charAt(0).toUpperCase() : 'U'
})

// UI 状态
const activeTab = ref('basic')
const savingBasic = ref(false)
const savingPwd = ref(false)
const pageLoading = ref(false) // 🌟 添加页面加载状态

// Refs
const basicFormRef = ref(null)
const pwdFormRef = ref(null)

// --- 基本资料表单 ---
const basicForm = reactive({
  realName: '',
  phone: ''
})

const basicRules = {
  realName: [{ required: true, message: '姓名不能为空', trigger: 'blur' }],
  phone: [
    { required: true, message: '手机号不能为空', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的11位手机号', trigger: 'blur' }
  ]
}

// --- 密码修改表单 ---
const pwdForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const pwdRules = {
  oldPassword: [{ required: true, message: '请输入原密码', trigger: 'blur' }],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能小于 6 位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认新密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value !== pwdForm.newPassword) {
          callback(new Error('两次输入的密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
}

// 字典翻译映射
const getRoleTagType = (role) => {
  const map = { ADMIN: 'danger', STOCK_MANAGER: 'success', TECHNICIAN: 'warning', EMPLOYEE: 'info' }
  return map[role] || 'info'
}
const getRoleText = (role) => {
  const map = { ADMIN: '管理员', STOCK_MANAGER: '库管员', TECHNICIAN: '机修工', EMPLOYEE: '员工' }
  return map[role] || role || '未知'
}

// 🌟 2. 新增：从后端获取最新用户数据的方法
const fetchCurrentUserInfo = async () => {
  pageLoading.value = true
  try {
    const response = await userApi.getCurrentUser()
    const data = response.data.code ? response.data.data : response.data

    // 更新页面展示数据
    userInfo.value = data

    // 同步到表单
    basicForm.realName = data.realName || ''
    basicForm.phone = data.phone || ''

    // 顺便更新一下本地 Store，防止顶部导航栏名字没变
    authStore.updateUser({
      realName: data.realName,
      phone: data.phone,
      role: data.role
    })
  } catch (error) {
    console.error(error)
    ElMessage.error('获取个人信息失败')
  } finally {
    pageLoading.value = false
  }
}

// 提交基本资料
const submitBasicForm = async () => {
  if (!basicFormRef.value) return
  await basicFormRef.value.validate()

  savingBasic.value = true
  try {
    await userApi.updateProfile(basicForm)
    ElMessage.success('资料修改成功')

    // 🌟 修改成功后，重新从后端拉取一次最新数据刷新页面
    await fetchCurrentUserInfo()
  } catch (error) {
    ElMessage.error(error.message || '修改失败')
  } finally {
    savingBasic.value = false
  }
}

// 提交密码修改
const submitPwdForm = async () => {
  if (!pwdFormRef.value) return
  await pwdFormRef.value.validate()

  savingPwd.value = true
  try {
    await userApi.updatePassword({
      oldPassword: pwdForm.oldPassword,
      newPassword: pwdForm.newPassword
    })
    ElMessage.success('密码修改成功，请妥善保管')
    pwdFormRef.value.resetFields()
  } catch (error) {
    ElMessage.error(error.message || '密码修改失败，请检查原密码')
  } finally {
    savingPwd.value = false
  }
}

// 🌟 3. 在页面挂载时调用后端接口
onMounted(() => {
  fetchCurrentUserInfo()
})
</script>

<style scoped>
.profile-container {
  padding: 20px;
}

/* 左侧卡片样式 */
.user-card {
  border-radius: 8px;
  text-align: center;
}
.avatar-wrapper {
  margin: 20px 0;
}
.user-avatar {
  background-color: #409eff;
  font-size: 36px;
}
.user-name {
  font-size: 22px;
  font-weight: 600;
  color: #303133;
  margin: 10px 0;
}
.user-role {
  margin-bottom: 20px;
}
.user-details {
  text-align: left;
  padding: 0 10px;
}
.detail-item {
  display: flex;
  justify-content: space-between;
  padding: 12px 0;
  border-bottom: 1px solid #f0f2f5;
  font-size: 14px;
}
.detail-item:last-child {
  border-bottom: none;
}
.detail-item .label {
  color: #606266;
  display: flex;
  align-items: center;
  gap: 8px;
}
.detail-item .value {
  color: #909399;
}

/* 右侧卡片样式 */
.settings-card {
  border-radius: 8px;
  min-height: 400px;
}
.profile-form {
  margin-top: 20px;
  max-width: 500px;
}
</style>