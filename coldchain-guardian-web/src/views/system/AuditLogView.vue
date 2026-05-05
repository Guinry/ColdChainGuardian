<template>
  <Layout>
    <div class="system-page">
      <div class="page-head">
        <div>
          <h1>审计日志</h1>
          <p>记录当前浏览器中新增、修改、删除、状态变更等关键操作。</p>
        </div>
        <div class="head-actions">
          <el-button @click="refreshLogs">
            <el-icon><Refresh /></el-icon>
            刷新
          </el-button>
          <el-button @click="exportLogs">
            <el-icon><Download /></el-icon>
            导出
          </el-button>
          <el-button type="danger" plain @click="clearLogs">
            <el-icon><Delete /></el-icon>
            清空
          </el-button>
        </div>
      </div>

      <el-card shadow="never" class="filter-card">
        <el-form inline>
          <el-form-item label="操作人">
            <el-input v-model="filters.operator" clearable placeholder="输入姓名或账号" />
          </el-form-item>
          <el-form-item label="结果">
            <el-select v-model="filters.result" clearable placeholder="全部" style="width: 140px">
              <el-option label="成功" value="SUCCESS" />
              <el-option label="失败" value="FAILED" />
            </el-select>
          </el-form-item>
          <el-form-item label="方法">
            <el-select v-model="filters.method" clearable placeholder="全部" style="width: 140px">
              <el-option label="POST" value="POST" />
              <el-option label="PUT" value="PUT" />
              <el-option label="PATCH" value="PATCH" />
              <el-option label="DELETE" value="DELETE" />
            </el-select>
          </el-form-item>
        </el-form>
      </el-card>

      <el-card shadow="never" class="table-card">
        <el-table :data="filteredLogs" stripe empty-text="暂无审计记录">
          <el-table-column prop="time" label="时间" width="180">
            <template #default="{ row }">{{ formatTime(row.time) }}</template>
          </el-table-column>
          <el-table-column prop="operator" label="操作人" width="140" />
          <el-table-column prop="role" label="角色" width="110" />
          <el-table-column prop="method" label="方法" width="90">
            <template #default="{ row }">
              <el-tag size="small" :type="getMethodType(row.method)">{{ row.method }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="url" label="接口" min-width="220" />
          <el-table-column prop="result" label="结果" width="100">
            <template #default="{ row }">
              <el-tag size="small" :type="row.result === 'SUCCESS' ? 'success' : 'danger'">
                {{ row.result === 'SUCCESS' ? '成功' : '失败' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="message" label="说明" min-width="180" show-overflow-tooltip />
        </el-table>
      </el-card>
    </div>
  </Layout>
</template>

<script setup>
import { computed, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Delete, Download, Refresh } from '@element-plus/icons-vue'
import Layout from '@/components/Layout.vue'
import { clearAuditEvents, getAuditEvents } from '@/utils/audit'

const logs = ref(getAuditEvents())
const filters = reactive({
  operator: '',
  result: '',
  method: ''
})

const filteredLogs = computed(() => logs.value.filter(log => {
  const matchOperator = !filters.operator || log.operator.includes(filters.operator)
  const matchResult = !filters.result || log.result === filters.result
  const matchMethod = !filters.method || log.method === filters.method
  return matchOperator && matchResult && matchMethod
}))

const refreshLogs = () => {
  logs.value = getAuditEvents()
}

const clearLogs = async () => {
  await ElMessageBox.confirm('确定清空当前浏览器中的审计记录吗？', '清空审计日志', {
    confirmButtonText: '清空',
    cancelButtonText: '取消',
    type: 'warning'
  })
  clearAuditEvents()
  refreshLogs()
  ElMessage.success('审计日志已清空')
}

const exportLogs = () => {
  const content = JSON.stringify(filteredLogs.value, null, 2)
  const blob = new Blob([content], { type: 'application/json;charset=utf-8' })
  const link = document.createElement('a')
  link.href = URL.createObjectURL(blob)
  link.download = `ccg-audit-${Date.now()}.json`
  link.click()
  URL.revokeObjectURL(link.href)
}

const formatTime = (value) => value ? new Date(value).toLocaleString('zh-CN') : '-'

const getMethodType = (method) => {
  if (method === 'DELETE') return 'danger'
  if (method === 'POST') return 'success'
  if (method === 'PUT' || method === 'PATCH') return 'warning'
  return 'info'
}
</script>

<style scoped>
.system-page {
  padding: 20px 24px 28px;
  min-height: 100%;
  background: var(--ccg-bg);
}

.page-head {
  display: flex;
  align-items: center;
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

.head-actions {
  display: flex;
  gap: 10px;
}

.filter-card {
  margin-bottom: 14px;
  border-radius: 8px;
}

.table-card {
  border-radius: 8px;
}
</style>
