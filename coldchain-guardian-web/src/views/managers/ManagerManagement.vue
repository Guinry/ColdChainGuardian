<template>
  <Layout>
    <div class="manager-management-container">
      <header class="page-head">
        <div>
          <h1>管理员管理</h1>
          <p>维护 Web 管理端账号，控制管理员微信绑定和账号启停状态。</p>
        </div>
        <el-button type="primary" :icon="Plus" @click="handleAdd">新增管理员</el-button>
      </header>

      <el-card class="search-card">
        <div class="search-and-action-wrapper">
          <el-form :model="queryParams" inline class="search-form">
            <el-form-item label="关键词">
              <el-input
                  v-model="queryParams.keyword"
                  placeholder="姓名或手机号"
                  clearable
                  @keyup.enter="handleSearch"
                  style="width: 200px"
              />
            </el-form-item>

            <el-form-item label="微信绑定状态">
              <el-select v-model="queryParams.isWechatBound" placeholder="全部状态" clearable style="width: 160px">
                <el-option label="已绑定" :value="true" />
                <el-option label="未绑定" :value="false" />
              </el-select>
            </el-form-item>

            <el-form-item label="账号状态">
              <el-select v-model="queryParams.status" placeholder="全部状态" clearable style="width: 160px">
                <el-option label="正常" :value="1" />
                <el-option label="停用" :value="0" />
              </el-select>
            </el-form-item>

            <el-form-item>
              <el-button type="primary" @click="handleSearch">查询</el-button>
              <el-button @click="resetQuery">重置</el-button>
            </el-form-item>
          </el-form>
        </div>
      </el-card>

      <el-card class="table-card">
        <el-table
            :data="managerList"
            v-loading="loading"
            stripe
            style="width: 100%"
        >
          <el-table-column prop="realName" label="姓名" align="center" />

          <el-table-column prop="phone" label="手机号码" min-width="120" align="center">
            <template #default="{ row }">
              <span class="font-bold">{{ row.phone }}</span>
            </template>
          </el-table-column>

          <el-table-column label="所属角色" align="center">
            <template #default="{ row }">
              <el-tag type="danger" size="small">
                管理员
              </el-tag>
            </template>
          </el-table-column>

          <el-table-column label="微信绑定" align="center">
            <template #default="{ row }">
              <div class="flex items-center justify-center">
                <span
                    class="inline-block w-2 h-2 rounded-full mr-2"
                    :style="{ backgroundColor: row.openId ? '#10b981' : '#ef4444' }"
                ></span>
                {{ row.openId ? '已绑定' : '未绑定' }}
                <el-popover
                    v-if="row.openId && row.wxNickname"
                    trigger="hover"
                    :content="`微信昵称: ${row.wxNickname}`"
                >
                  <template #reference>
                    <el-icon class="ml-2 text-gray-400 cursor-pointer"><InfoFilled /></el-icon>
                  </template>
                </el-popover>
              </div>
            </template>
          </el-table-column>

          <el-table-column label="账号状态" align="center">
            <template #default="{ row }">
              <el-switch
                  v-model="row.status"
                  :active-value="1"
                  :inactive-value="0"
                  @change="handleStatusChange(row)"
                  :disabled="row.id === currentUser.id"
              />
            </template>
          </el-table-column>

          <el-table-column label="操作" min-width="180" align="center">
            <template #default="{ row }">
              <el-button size="small" @click="handleEdit(row)">编辑</el-button>
              <el-button
                  size="small"
                  type="danger"
                  :disabled="!row.openId"
                  @click="handleUnbindWechat(row)"
              >
                解绑微信
              </el-button>
            </template>
          </el-table-column>
        </el-table>

        <el-pagination
            v-model:current-page="pagination.currentPage"
            v-model:page-size="pagination.pageSize"
            :total="pagination.total"
            :page-sizes="[10, 20, 50, 100]"
            layout="total, sizes, prev, pager, next, jumper"
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
            class="mt-4 flex justify-center"
        />
      </el-card>

      <el-drawer
          v-model="drawerVisible"
          :title="drawerTitle"
          size="40%"
          :before-close="handleDrawerClose"
      >
        <el-form
            :model="managerForm"
            :rules="formRules"
            ref="formRef"
            label-width="100px"
        >
          <el-form-item label="姓名" prop="realName">
            <el-input v-model="managerForm.realName" placeholder="请输入管理员姓名" />
          </el-form-item>

          <el-form-item label="手机号" prop="phone">
            <el-input
                v-model="managerForm.phone"
                placeholder="请输入11位手机号"
                maxlength="11"
            />
          </el-form-item>

          <el-form-item label="角色" prop="role">
            <el-select v-model="managerForm.role" placeholder="请选择角色" style="width: 100%;" disabled>
              <el-option label="系统管理员" value="ADMIN" />
            </el-select>
          </el-form-item>
        </el-form>

        <template #footer>
          <div class="drawer-footer">
            <el-button @click="handleDrawerClose">取消</el-button>
            <el-button type="primary" @click="handleSubmit">确定</el-button>
          </div>
        </template>
      </el-drawer>
    </div>
  </Layout>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox, FormInstance, FormRules } from 'element-plus'
import { Plus, InfoFilled } from '@element-plus/icons-vue'
import Layout from '@/components/Layout.vue'
import { useAuthStore } from '@/store/auth'
// ⚠️ 注意：这里我们复用 employeeApi，但强制传参 role='ADMIN'
// 如果你有专门的 managerApi，请替换掉它
import { managerApi } from '@/utils/api'

// 获取当前登录用户，用于防止停用自己
const authStore = useAuthStore()
const currentUser = reactive({ id: authStore.user?.id || -1 })

// 类型定义
interface Manager {
  id: number
  realName: string
  phone: string
  role: string
  status: number
  openId?: string
  wxNickname?: string
}

// 响应式数据
const loading = ref(false)
const drawerVisible = ref(false)
const drawerTitle = ref('')
const formRef = ref<FormInstance>()

// 查询参数：强制 role 默认为 ADMIN，且不开放修改
const queryParams = reactive({
  keyword: '',
  role: 'ADMIN', // 🌟 核心：永远只查 ADMIN
  status: null as number | null,
  isWechatBound: null as boolean | null
})

// 分页参数
const pagination = reactive({
  currentPage: 1,
  pageSize: 10,
  total: 0
})

const managerList = ref<Manager[]>([])

// 表单数据：新建时默认赋予 ADMIN 角色
const managerForm = reactive({
  id: undefined as number | undefined,
  realName: '',
  phone: '',
  role: 'ADMIN' // 🌟 核心：默认且固定为 ADMIN
})

// 表单验证规则
const formRules: FormRules = {
  realName: [
    { required: true, message: '请输入管理员姓名', trigger: 'blur' }
  ],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的11位手机号', trigger: 'blur' }
  ],
  role: [
    { required: true, message: '请选择角色', trigger: 'change' }
  ]
}

// 获取管理员列表
const getList = async () => {
  loading.value = true
  try {
    const response = await managerApi.getManagerList(
        queryParams,
        pagination.currentPage,
        pagination.pageSize
    )

    const result = response.data.code ? response.data : response;
    const pageData = result.data;

    if (pageData) {
      managerList.value = pageData.data || pageData.records || pageData.list || [];
      pagination.total = pageData.total || 0;
    } else {
      managerList.value = [];
      pagination.total = 0;
    }
  } catch (error) {
    console.error(error)
    ElMessage.error('获取管理员列表失败')
  } finally {
    loading.value = false
  }
}

// 处理搜索
const handleSearch = () => {
  pagination.currentPage = 1
  getList()
}

// 重置查询
const resetQuery = () => {
  queryParams.keyword = ''
  queryParams.status = null
  queryParams.isWechatBound = null
  // 注意：重置时不能把 role 置空，必须保持 ADMIN
  queryParams.role = 'ADMIN'
  pagination.currentPage = 1
  getList()
}

// 分页大小改变
const handleSizeChange = (size: number) => {
  pagination.pageSize = size
  pagination.currentPage = 1
  getList()
}

// 当前页改变
const handleCurrentChange = (page: number) => {
  pagination.currentPage = page
  getList()
}

// 处理添加
const handleAdd = () => {
  drawerTitle.value = '新增管理员'
  Object.assign(managerForm, {
    id: undefined,
    realName: '',
    phone: '',
    role: 'ADMIN' // 始终固定
  })
  drawerVisible.value = true
}

// 处理编辑
const handleEdit = (row: Manager) => {
  drawerTitle.value = '编辑管理员'
  Object.assign(managerForm, {
    id: row.id,
    realName: row.realName,
    phone: row.phone,
    role: row.role || 'ADMIN'
  })
  drawerVisible.value = true
}

// 处理状态改变（启停用）
const handleStatusChange = async (row: Manager) => {
  if (row.id === currentUser.id) {
    ElMessage.warning('不能停用当前登录账号！');
    row.status = 1;
    return;
  }

  try {
    await ElMessageBox.confirm(
        `确认${row.status === 1 ? '启用' : '停用'}该管理员账号吗？${row.status === 1 ? '停用后该管理员将失去系统权限' : '启用后该管理员可正常登录'}`,
        '提示',
        {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: row.status === 1 ? 'warning' : 'info'
        }
    )

    await managerApi.updateManagerStatus(row.id, row.status)
    ElMessage.success(`${row.status === 1 ? '启用' : '停用'}成功`)
  } catch {
    row.status = row.status === 1 ? 0 : 1
  }
}

// 处理解绑微信
const handleUnbindWechat = async (row: Manager) => {
  try {
    await ElMessageBox.confirm(
        `确认解除管理员 ${row.realName} 与当前微信的绑定关系吗？`,
        '解绑微信',
        {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        }
    )

    await managerApi.unbindManagerWechat(row.id)
    ElMessage.success('解绑微信成功')
    // 刷新列表确保数据同步
    await getList()
  } catch (error) {
    // 如果不是用户取消，显示错误
    if (error !== 'cancel' && error !== 'Escape') {
      console.error('解绑失败:', error)
      ElMessage.error('解绑微信失败，请重试')
    }
  }
}

// 处理抽屉关闭
const handleDrawerClose = () => {
  drawerVisible.value = false
  formRef.value?.resetFields()
}

// 提交表单
const handleSubmit = async () => {
  await formRef.value?.validate()

  try {
    if (managerForm.id) {
      await managerApi.updateManager(managerForm)
      ElMessage.success('编辑成功')
    } else {
      await managerApi.createManager(managerForm)
      ElMessage.success('新增成功')
    }

    drawerVisible.value = false
    getList()
  } catch (error) {
    console.error(error)
    ElMessage.error('操作失败')
  }
}

// 初始化数据
onMounted(() => {
  getList()
})
</script>

<style scoped>
.manager-management-container {
  min-height: 100%;
  padding: 20px 24px 28px;
  background: var(--ccg-bg);
}

.page-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 16px;
}

.page-head h1 {
  margin: 0;
  font-size: 22px;
  line-height: 1.2;
  font-weight: 750;
  color: var(--ccg-text);
}

.page-head p {
  margin: 6px 0 0;
  color: var(--ccg-muted);
  font-size: 13px;
}

.search-card, .table-card {
  margin-bottom: 16px;
  border: 1px solid var(--ccg-border);
  border-radius: 8px;
  box-shadow: var(--ccg-shadow-sm);
}

.search-form {
  flex: 1;
}

.font-bold {
  font-weight: 600;
}

.flex {
  display: flex;
}

.items-center {
  align-items: center;
}

.inline-block {
  display: inline-block;
}

.w-2 {
  width: 0.5rem;
}

.h-2 {
  height: 0.5rem;
}

.rounded-full {
  border-radius: 50%;
}

.mr-2 {
  margin-right: 0.5rem;
}

.ml-2 {
  margin-left: 0.5rem;
}

.text-gray-400 {
  color: #9ca3af;
}

.cursor-pointer {
  cursor: pointer;
}

.mt-4 {
  margin-top: 1rem;
}

.justify-center {
  justify-content: center;
}

.drawer-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  padding-top: 20px;
  border-top: 1px solid #eee;
}

@media (max-width: 900px) {
  .page-head {
    flex-direction: column;
  }
}
</style>
