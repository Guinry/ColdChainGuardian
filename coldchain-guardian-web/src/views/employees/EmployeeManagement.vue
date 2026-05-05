<template>
  <Layout>
    <div class="employee-management-container">
      <header class="page-head">
        <div>
          <h1>员工管理</h1>
          <p>维护小程序端员工账号、角色、微信绑定和启停状态。</p>
        </div>
        <el-button type="primary" :icon="Plus" @click="handleAdd">新增员工</el-button>
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

            <el-form-item label="角色">
              <el-select v-model="queryParams.role" placeholder="请选择角色" clearable style="width: 160px">
                <el-option label="库管员" value="STOCK_MANAGER" />
                <el-option label="机修工" value="TECHNICIAN" />
                <el-option label="普通员工" value="EMPLOYEE" />
              </el-select>
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
            :data="employeeList"
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
              <el-tag
                  :type="getRoleTagType(row.role)"
                  size="small"
              >
                {{ getRoleText(row.role) }}
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
            :model="employeeForm"
            :rules="formRules"
            ref="formRef"
            label-width="100px"
        >
          <el-form-item label="姓名" prop="realName">
            <el-input v-model="employeeForm.realName" placeholder="请输入员工姓名" />
          </el-form-item>

          <el-form-item label="手机号" prop="phone">
            <el-input
                v-model="employeeForm.phone"
                placeholder="请输入11位手机号"
                maxlength="11"
            />
          </el-form-item>

          <el-form-item label="角色" prop="role">
            <el-select v-model="employeeForm.role" placeholder="请选择角色" style="width: 100%;">
              <el-option label="库管员" value="STOCK_MANAGER" />
              <el-option label="机修工" value="TECHNICIAN" />
              <el-option label="普通员工" value="EMPLOYEE" />
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
import Layout from '@/components/Layout.vue' // 引入Layout组件
import { employeeApi } from '@/utils/api'

// 类型定义
interface Employee {
  id: number
  realName: string
  phone: string
  role: string
  status: number // 1 正常, 0 停用
  openId?: string
  wxNickname?: string
}

// 响应式数据
const loading = ref(false)
const drawerVisible = ref(false)
const drawerTitle = ref('')
const formRef = ref<FormInstance>()

// 查询参数
const queryParams = reactive({
  keyword: '',
  role: '',
  status: null as number | null,
  isWechatBound: null as boolean | null
})

// 分页参数
const pagination = reactive({
  currentPage: 1,
  pageSize: 10,
  total: 0
})

// 表格数据
const employeeList = ref<Employee[]>([])

// 表单数据
const employeeForm = reactive({
  id: undefined as number | undefined,
  realName: '',
  phone: '',
  role: ''
})

// 当前用户（用于防止自己停用自己的情况）
const currentUser = reactive({ id: 1 }) // 假设当前用户ID为1

// 表单验证规则
const formRules: FormRules = {
  realName: [
    { required: true, message: '请输入员工姓名', trigger: 'blur' }
  ],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的11位手机号', trigger: 'blur' }
  ],
  role: [
    { required: true, message: '请选择角色', trigger: 'change' }
  ]
}

// 角色标签类型映射
const getRoleTagType = (role: string) => {
  switch (role) {
    case 'ADMIN': return 'danger'
    case 'STOCK_MANAGER': return 'success'
    case 'TECHNICIAN': return 'warning'
    case 'EMPLOYEE': return 'info'
    default: return 'info'
  }
}

// 角色文本映射
const getRoleText = (role: string) => {
  switch (role) {
    case 'STOCK_MANAGER': return '库管员'
    case 'TECHNICIAN': return '机修工'
    case 'EMPLOYEE': return '普通员工'
    default: return role
  }
}

// 获取员工列表
const getList = async () => {
  loading.value = true
  try {
    const response = await employeeApi.getEmployeeList(
        queryParams,
        pagination.currentPage,
        pagination.pageSize
    )

    // 🌟 修复：精准解析后端的 ApiResponse 和 PageResponse 结构
    // 1. 如果你的 Axios 拦截器没有脱壳，response.data 才是后端的 JSON 对象
    const result = response.data.code ? response.data : response;

    // 2. result.data 就是后端的 PageResponse 对象
    const pageData = result.data;

    // 3. PageResponse 里面通常包含 total 和 真正的数据数组 (字段名可能是 data、records 或 list)
    if (pageData) {
      employeeList.value = pageData.data || pageData.records || pageData.list || [];
      pagination.total = pageData.total || 0;
    } else {
      employeeList.value = [];
      pagination.total = 0;
    }

  } catch (error) {
    console.error(error)
    ElMessage.error('获取员工列表失败')
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
  queryParams.role = ''
  queryParams.status = null
  queryParams.isWechatBound = null
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
  drawerTitle.value = '新增员工'
  Object.assign(employeeForm, {
    id: undefined,
    realName: '',
    phone: '',
    role: ''
  })
  drawerVisible.value = true
}

// 处理编辑
const handleEdit = (row: Employee) => {
  drawerTitle.value = '编辑员工'
  Object.assign(employeeForm, {
    id: row.id,
    realName: row.realName,
    phone: row.phone,
    role: row.role
  })
  drawerVisible.value = true
}

// 处理状态改变（启停用）
const handleStatusChange = async (row: Employee) => {
  try {
    await ElMessageBox.confirm(
        `确认${row.status === 1 ? '启用' : '停用'}该员工账号吗？${row.status === 1 ? '停用后该员工将无法进入小程序工作台' : '启用后该员工可正常登录'}`,
        '提示',
        {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: row.status === 1 ? 'warning' : 'info'
        }
    )

    // 调用实际的API更新状态
    await employeeApi.updateEmployeeStatus(row.id, row.status)
    ElMessage.success(`${row.status === 1 ? '启用' : '停用'}成功`)
  } catch {
    // 取消操作，恢复原状态
    row.status = row.status === 1 ? 0 : 1
  }
}

// 处理解绑微信
const handleUnbindWechat = async (row: Employee) => {
  try {
    await ElMessageBox.confirm(
        `确认解除 ${row.realName} 与当前微信的绑定关系吗？解除后，该员工需在小程序重新输入手机号进行首次绑定。`,
        '解绑微信',
        {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        }
    )

    // 调用实际的API解绑微信
    await employeeApi.unbindEmployeeWechat(row.id)
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
    if (employeeForm.id) {
      // 编辑
      await employeeApi.updateEmployee(employeeForm)
      ElMessage.success('编辑成功')
    } else {
      // 新增
      await employeeApi.createEmployee(employeeForm)
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
.employee-management-container {
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

/* 给新增按钮加一点左边距，和重置按钮拉开一点距离 */
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
