<template>
  <Layout>
    <div class="device-management-content">
      <div class="page-header">
        <h2>设备管理</h2>
        <div class="header-actions">
          <el-button type="primary" @click="openCreateDialog()">
            <el-icon><Plus /></el-icon>
            新增设备
          </el-button>
          <el-button @click="handleImport">
            <el-icon><Upload /></el-icon>
            批量导入
          </el-button>
          <el-button @click="handleExport">
            <el-icon><Download /></el-icon>
            导出
          </el-button>
        </div>
      </div>

      <div class="search-filters">
        <el-form :model="searchForm" inline class="search-form">
          <el-form-item label="设备编码">
            <el-input
              v-model="searchForm.keyword"
              placeholder="输入设备编码或名称"
              clearable
              @keyup.enter="handleSearch"
            />
          </el-form-item>
          <el-form-item label="设备类型">
            <el-select
              v-model="searchForm.deviceType"
              placeholder="请选择设备类型"
              clearable
              @change="handleSearch"
            >
              <el-option label="温湿度传感器" value="TEMP_HUM" />
              <el-option label="冷冻设备" value="FREEZER" />
              <el-option label="车辆" value="VEHICLE" />
              <el-option label="门磁" value="DOOR" />
            </el-select>
          </el-form-item>
          <el-form-item label="在线状态">
            <el-select
              v-model="searchForm.onlineStatus"
              placeholder="请选择在线状态"
              clearable
              @change="handleSearch"
            >
              <el-option label="在线" :value="true" />
              <el-option label="离线" :value="false" />
            </el-select>
          </el-form-item>
          <el-form-item label="启用状态">
            <el-select
              v-model="searchForm.enabled"
              placeholder="请选择启用状态"
              clearable
              @change="handleSearch"
            >
              <el-option label="启用" :value="true" />
              <el-option label="禁用" :value="false" />
            </el-select>
          </el-form-item>
          <el-form-item label="告警状态">
            <el-select
              v-model="searchForm.alarmEnabled"
              placeholder="请选择告警状态"
              clearable
              @change="handleSearch"
            >
              <el-option label="启用告警" :value="true" />
              <el-option label="关闭告警" :value="false" />
            </el-select>
          </el-form-item>
          <el-form-item label="所属库区">
            <el-cascader
              v-model="searchForm.areaId"
              :options="areaOptions"
              :props="cascaderProps"
              placeholder="请选择库区"
              clearable
              filterable
              @change="handleSearch"
            />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleSearch">
              <el-icon><Search /></el-icon>
              搜索
            </el-button>
            <el-button @click="resetFilters">
              <el-icon><Refresh /></el-icon>
              重置
            </el-button>
          </el-form-item>
        </el-form>
      </div>

      <div class="table-actions">
        <el-button
          :disabled="!selectedRows.length"
          @click="batchUpdateStatus(true)"
        >
          批量启用
        </el-button>
        <el-button
          :disabled="!selectedRows.length"
          @click="batchUpdateStatus(false)"
        >
          批量禁用
        </el-button>
        <el-button
          :disabled="!selectedRows.length"
          @click="batchUpdateAlarmStatus(true)"
        >
          批量启用告警
        </el-button>
        <el-button
          :disabled="!selectedRows.length"
          @click="batchUpdateAlarmStatus(false)"
        >
          批量关闭告警
        </el-button>
        <el-button
          :disabled="!selectedRows.length"
          type="danger"
          @click="batchDelete"
        >
          批量删除
        </el-button>
        <span class="selected-count" v-if="selectedRows.length">
          已选择 {{ selectedRows.length }} 项
        </span>
      </div>

      <el-table
        v-loading="tableLoading"
        :data="tableData"
        @selection-change="handleSelectionChange"
        height="calc(100vh - 400px)"
        class="device-table"
      >
        <el-table-column type="selection" width="55" />
        <el-table-column prop="deviceCode" label="设备编码" width="150">
          <template #default="{ row }">
            <router-link :to="`/devices/${row.id}/data`" class="device-link">
              {{ row.deviceCode }}
            </router-link>
          </template>
        </el-table-column>
        <el-table-column prop="deviceName" label="设备名称" width="150" />
        <el-table-column prop="deviceType" label="设备类型" width="120">
          <template #default="{ row }">
            <el-tag :type="getDeviceTypeTag(row.deviceType)">
              {{ getDeviceTypeLabel(row.deviceType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="areaName" label="所属库区" width="150" />
        <el-table-column prop="onlineStatus" label="在线状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.onlineStatus ? 'success' : 'info'">
              {{ row.onlineStatus ? '在线' : '离线' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="enabled" label="启用状态" width="100">
          <template #default="{ row }">
            <el-switch
              v-model="row.enabled"
              @change="toggleStatus(row)"
              :active-value="true"
              :inactive-value="false"
              :disabled="!hasPermission('device:update')"
            />
          </template>
        </el-table-column>
        <el-table-column prop="alarmEnabled" label="告警状态" width="100">
          <template #default="{ row }">
            <el-switch
              v-model="row.alarmEnabled"
              @change="toggleAlarmStatus(row)"
              :active-value="true"
              :inactive-value="false"
              :disabled="!hasPermission('device:update')"
            />
          </template>
        </el-table-column>
        <el-table-column prop="lastSeenTime" label="最后在线" width="180">
          <template #default="{ row }">
            {{ row.lastSeenTime ? formatDate(row.lastSeenTime) : '-' }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="300">
          <template #default="{ row }">
            <el-button size="small" @click="viewData(row)">查看数据</el-button>
            <el-button size="small" @click="viewAlerts(row)">查看告警</el-button>
            <el-button size="small" @click="openEditDialog(row)">编辑</el-button>
            <el-button
              size="small"
              :type="row.enabled ? 'danger' : 'success'"
              @click="toggleStatus(row)"
            >
              {{ row.enabled ? '禁用' : '启用' }}
            </el-button>
            <el-button
              size="small"
              :type="row.alarmEnabled ? 'warning' : 'info'"
              @click="toggleAlarmStatus(row)"
            >
              {{ row.alarmEnabled ? '关闭告警' : '启用告警' }}
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination">
        <el-pagination
          v-model:current-page="pagination.currentPage"
          v-model:page-size="pagination.pageSize"
          :total="pagination.total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </div>

    <!-- 新增/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="600px"
      :before-close="handleDialogClose"
    >
      <el-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-width="120px"
      >
        <el-form-item label="设备编码" prop="deviceCode">
          <el-input
            v-model="formData.deviceCode"
            placeholder="请输入设备编码"
            maxlength="50"
            :disabled="!!formData.id"
          />
        </el-form-item>
        <el-form-item label="设备名称" prop="deviceName">
          <el-input
            v-model="formData.deviceName"
            placeholder="请输入设备名称"
            maxlength="100"
          />
        </el-form-item>
        <el-form-item label="设备类型" prop="deviceType">
          <el-select
            v-model="formData.deviceType"
            placeholder="请选择设备类型"
            style="width: 100%"
          >
            <el-option label="温湿度传感器" value="TEMP_HUM" />
            <el-option label="冷冻设备" value="FREEZER" />
            <el-option label="车辆" value="VEHICLE" />
            <el-option label="门磁" value="DOOR" />
          </el-select>
        </el-form-item>
        <el-form-item label="所属库区" prop="areaId">
          <el-cascader
            v-model="formData.areaId"
            :options="areaOptions"
            :props="cascaderProps"
            placeholder="请选择库区"
            clearable
            filterable
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="型号" prop="model">
          <el-input
            v-model="formData.model"
            placeholder="请输入设备型号"
            maxlength="50"
          />
        </el-form-item>
        <el-form-item label="制造商" prop="manufacturer">
          <el-input
            v-model="formData.manufacturer"
            placeholder="请输入制造商"
            maxlength="100"
          />
        </el-form-item>
        <el-form-item label="序列号" prop="sn">
          <el-input
            v-model="formData.sn"
            placeholder="请输入序列号"
            maxlength="100"
          />
        </el-form-item>
        <el-form-item label="固件版本" prop="firmwareVersion">
          <el-input
            v-model="formData.firmwareVersion"
            placeholder="请输入固件版本"
            maxlength="50"
          />
        </el-form-item>
        <el-form-item label="位置描述" prop="locationDesc">
          <el-input
            v-model="formData.locationDesc"
            type="textarea"
            :rows="3"
            placeholder="请输入设备位置描述"
            maxlength="200"
          />
        </el-form-item>
        <el-divider>阈值设置</el-divider>
        <el-form-item label="阈值模式">
          <el-radio-group v-model="formData.thresholdMode">
            <el-radio label="SYSTEM">使用系统默认</el-radio>
            <el-radio label="CUSTOM">自定义阈值</el-radio>
          </el-radio-group>
        </el-form-item>
        <template v-if="formData.thresholdMode === 'CUSTOM'">
          <el-form-item label="温度下限" prop="temperatureThresholdMin">
            <el-input-number
              v-model="formData.temperatureThresholdMin"
              :min="-50"
              :max="formData.temperatureThresholdMax - 0.1"
              :step="0.1"
              :precision="2"
              :controls="false"
              style="width: 100%"
            />
            <span class="unit">°C</span>
          </el-form-item>
          <el-form-item label="温度上限" prop="temperatureThresholdMax">
            <el-input-number
              v-model="formData.temperatureThresholdMax"
              :min="formData.temperatureThresholdMin + 0.1"
              :max="50"
              :step="0.1"
              :precision="2"
              :controls="false"
              style="width: 100%"
            />
            <span class="unit">°C</span>
          </el-form-item>
          <el-form-item label="湿度下限" prop="humidityThresholdMin">
            <el-input-number
              v-model="formData.humidityThresholdMin"
              :min="0"
              :max="formData.humidityThresholdMax - 0.1"
              :step="0.1"
              :precision="2"
              :controls="false"
              style="width: 100%"
            />
            <span class="unit">%</span>
          </el-form-item>
          <el-form-item label="湿度上限" prop="humidityThresholdMax">
            <el-input-number
              v-model="formData.humidityThresholdMax"
              :min="formData.humidityThresholdMin + 0.1"
              :max="100"
              :step="0.1"
              :precision="2"
              :controls="false"
              style="width: 100%"
            />
            <span class="unit">%</span>
          </el-form-item>
        </template>
        <el-form-item label="启用状态" prop="enabled">
          <el-switch
            v-model="formData.enabled"
            :active-value="true"
            :inactive-value="false"
            active-text="启用"
            inactive-text="禁用"
          />
        </el-form-item>
        <el-form-item label="告警状态" prop="alarmEnabled">
          <el-switch
            v-model="formData.alarmEnabled"
            :active-value="true"
            :inactive-value="false"
            active-text="启用告警"
            inactive-text="关闭告警"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSubmit" :loading="submitLoading">确定</el-button>
        </span>
      </template>
    </el-dialog>
  </Layout>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/store/auth'
import Layout from '@/components/Layout.vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Search,
  Plus,
  Upload,
  Download,
  Refresh,
  House,
  Monitor,
  Warning,
  DataAnalysis,
  Setting,
  Bell,
  ArrowDown,
  User,
  Document,
  Grid,
  Operation,
  Tickets,
  Memo
} from '@element-plus/icons-vue'
import { deviceApi } from '@/api/device'
import { areaApi } from '@/api/area'

const router = useRouter()
const authStore = useAuthStore()

// 权限检查
const hasPermission = (permission) => {
  return authStore.hasPermission(permission)
}

// 响应式数据
const tableData = ref([])
const tableLoading = ref(false)
const selectedRows = ref([])
const dialogVisible = ref(false)
const dialogTitle = ref('')
const submitLoading = ref(false)
const formRef = ref()

// 分页
const pagination = reactive({
  currentPage: 1,
  pageSize: 10,
  total: 0
})

// 搜索表单
const searchForm = reactive({
  keyword: '',
  deviceType: '',
  onlineStatus: null,
  enabled: null,
  alarmEnabled: null,
  areaId: null
})

// 表单数据
const formData = reactive({
  id: null,
  deviceCode: '',
  deviceName: '',
  deviceType: 'TEMP_HUM',
  areaId: null,
  model: '',
  manufacturer: '',
  sn: '',
  firmwareVersion: '',
  locationDesc: '',
  thresholdMode: 'SYSTEM',
  temperatureThresholdMin: null,
  temperatureThresholdMax: null,
  humidityThresholdMin: null,
  humidityThresholdMax: null,
  enabled: true,
  alarmEnabled: true
})

// 库区选项
const areaOptions = ref([])

// 级联选择器配置
const cascaderProps = {
  value: 'id',
  label: 'areaName',
  children: 'children',
  checkStrictly: true,
  emitPath: false,
  expandTrigger: 'hover'
}

// 表单验证规则
const formRules = {
  deviceCode: [
    { required: true, message: '请输入设备编码', trigger: 'blur' },
    { min: 2, max: 50, message: '设备编码长度应在2-50个字符之间', trigger: 'blur' },
    { pattern: /^[A-Za-z0-9_-]+$/, message: '设备编码只能包含字母、数字、下划线和横线', trigger: 'blur' }
  ],
  deviceName: [
    { required: true, message: '请输入设备名称', trigger: 'blur' },
    { min: 2, max: 100, message: '设备名称长度应在2-100个字符之间', trigger: 'blur' }
  ],
  deviceType: [
    { required: true, message: '请选择设备类型', trigger: 'change' }
  ],
  temperatureThresholdMin: [
    { required: true, message: '请输入温度下限', trigger: 'blur' }
  ],
  temperatureThresholdMax: [
    { required: true, message: '请输入温度上限', trigger: 'blur' }
  ],
  humidityThresholdMin: [
    { required: true, message: '请输入湿度下限', trigger: 'blur' }
  ],
  humidityThresholdMax: [
    { required: true, message: '请输入湿度上限', trigger: 'blur' }
  ]
}

// 加载表格数据
const loadTableData = async () => {
  tableLoading.value = true
  try {
    const params = {
      page: pagination.currentPage,
      size: pagination.pageSize,
      keyword: searchForm.keyword || undefined,
      deviceType: searchForm.deviceType || undefined,
      onlineStatus: searchForm.onlineStatus,
      enabled: searchForm.enabled,
      alarmEnabled: searchForm.alarmEnabled,
      areaId: searchForm.areaId
    }

    const response = await deviceApi.getList(params)
    // 根据实际返回结构，设备列表在 response.data.data
    tableData.value = response.data?.data?.data || []
    pagination.total = response.data?.data?.total || 0
  } catch (error) {
    console.error('Failed to load device data:', error)
    ElMessage.error('获取设备列表失败')
  } finally {
    tableLoading.value = false
  }
}

// 加载库区选项
const loadAreaOptions = async () => {
  try {
    const response = await areaApi.getAreaTree()
    areaOptions.value = response.data?.data || []
  } catch (error) {
    console.error('Failed to load area options:', error)
    ElMessage.error('获取库区列表失败')
  }
}

// 处理搜索
const handleSearch = () => {
  pagination.currentPage = 1
  loadTableData()
}

// 重置过滤器
const resetFilters = () => {
  Object.keys(searchForm).forEach(key => {
    searchForm[key] = null
  })
  handleSearch()
}

// 处理每页大小变化
const handleSizeChange = (size) => {
  pagination.pageSize = size
  pagination.currentPage = 1
  loadTableData()
}

// 处理当前页变化
const handleCurrentChange = (page) => {
  pagination.currentPage = page
  loadTableData()
}

// 处理表格选择变化
const handleSelectionChange = (selection) => {
  selectedRows.value = selection
}

// 打开新增对话框
const openCreateDialog = () => {
  dialogTitle.value = '新增设备'
  Object.assign(formData, {
    id: null,
    deviceCode: '',
    deviceName: '',
    deviceType: 'TEMP_HUM',
    areaId: null,
    model: '',
    manufacturer: '',
    sn: '',
    firmwareVersion: '',
    locationDesc: '',
    thresholdMode: 'SYSTEM',
    temperatureThresholdMin: null,
    temperatureThresholdMax: null,
    humidityThresholdMin: null,
    humidityThresholdMax: null,
    enabled: true,
    alarmEnabled: true
  })
  dialogVisible.value = true
}

// 打开编辑对话框
const openEditDialog = (row) => {
  dialogTitle.value = '编辑设备'
  Object.assign(formData, { ...row })
  dialogVisible.value = true
}

// 处理提交
const handleSubmit = async () => {
  await formRef.value.validate()

  submitLoading.value = true
  try {
    if (formData.id) {
      // 更新设备
      await deviceApi.update(formData.id, formData)
      ElMessage.success('更新设备成功')
    } else {
      // 创建设备
      await deviceApi.create(formData)
      ElMessage.success('创建设备成功')
    }

    dialogVisible.value = false
    loadTableData()
  } catch (error) {
    console.error('Failed to submit device data:', error)
    ElMessage.error(error.response?.data?.message || '操作失败')
  } finally {
    submitLoading.value = false
  }
}

// 处理对话框关闭
const handleDialogClose = (done) => {
  if (submitLoading.value) return
  done()
}

// 切换设备状态
const toggleStatus = async (row) => {
  try {
    await deviceApi.updateStatus(row.id, { enabled: !row.enabled })
    row.enabled = !row.enabled
    ElMessage.success(`${row.enabled ? '启用' : '禁用'}设备成功`)
  } catch (error) {
    console.error('Failed to toggle device status:', error)
    // 恢复状态
    row.enabled = !row.enabled
    ElMessage.error(error.response?.data?.message || '操作失败')
  }
}

// 切换告警状态
const toggleAlarmStatus = async (row) => {
  try {
    await deviceApi.updateAlarmStatus(row.id, { alarmEnabled: !row.alarmEnabled })
    row.alarmEnabled = !row.alarmEnabled
    ElMessage.success(`${row.alarmEnabled ? '启用' : '关闭'}告警成功`)
  } catch (error) {
    console.error('Failed to toggle alarm status:', error)
    // 恢复状态
    row.alarmEnabled = !row.alarmEnabled
    ElMessage.error(error.response?.data?.message || '操作失败')
  }
}

// 批量更新状态
const batchUpdateStatus = async (enabled) => {
  try {
    const ids = selectedRows.value.map(row => row.id)
    await deviceApi.batchUpdateStatus({ ids, enabled })
    ElMessage.success(`${enabled ? '启用' : '禁用'}设备成功`)
    loadTableData()
  } catch (error) {
    console.error('Failed to batch update device status:', error)
    ElMessage.error(error.response?.data?.message || '批量操作失败')
  }
}

// 批量更新告警状态
const batchUpdateAlarmStatus = async (alarmEnabled) => {
  try {
    const ids = selectedRows.value.map(row => row.id)
    await deviceApi.batchUpdateAlarmStatus({ ids, alarmEnabled })
    ElMessage.success(`${alarmEnabled ? '启用' : '关闭'}告警成功`)
    loadTableData()
  } catch (error) {
    console.error('Failed to batch update alarm status:', error)
    ElMessage.error(error.response?.data?.message || '批量操作失败')
  }
}

// 批量删除
const batchDelete = async () => {
  try {
    await ElMessageBox.confirm(
      `确认删除选中的 ${selectedRows.value.length} 个设备吗？`,
      '提示',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    const ids = selectedRows.value.map(row => row.id)
    await deviceApi.batchDelete(ids)
    ElMessage.success('删除设备成功')
    loadTableData()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('Failed to batch delete devices:', error)
      ElMessage.error(error.response?.data?.message || '批量删除失败')
    }
  }
}

// 查看数据
const viewData = (row) => {
  router.push(`/devices/${row.id}/data`)
}

// 查看告警
const viewAlerts = (row) => {
  router.push(`/devices/${row.id}/alerts`)
}

// 获取设备类型标签
const getDeviceTypeTag = (type) => {
  const tags = {
    'TEMP_HUM': 'primary',
    'FREEZER': 'success',
    'VEHICLE': 'warning',
    'DOOR': 'info'
  }
  return tags[type] || 'default'
}

// 获取设备类型标签
const getDeviceTypeLabel = (type) => {
  const labels = {
    'TEMP_HUM': '温湿度传感器',
    'FREEZER': '冷冻设备',
    'VEHICLE': '车辆',
    'DOOR': '门磁'
  }
  return labels[type] || type
}

// 格式化日期
const formatDate = (dateStr) => {
  if (!dateStr) return '-'
  const date = new Date(dateStr)
  return date.toLocaleString('zh-CN')
}

// 处理导入
const handleImport = () => {
  ElMessage.info('批量导入功能正在开发中...')
}

// 处理导出
const handleExport = () => {
  ElMessage.info('导出功能正在开发中...')
}

onMounted(async () => {
  await loadAreaOptions()
  await loadTableData()
})
</script>

<style scoped>
.device-management-content {
  padding: 20px;
  height: 100%;
  min-height: 0;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding-bottom: 10px;
  border-bottom: 1px solid #ebeef5;
}

.page-header h2 {
  margin: 0;
  font-size: 24px;
  font-weight: 600;
  color: #303133;
}

.search-filters {
  background: #fff;
  padding: 20px;
  border-radius: 4px;
  margin-bottom: 20px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.search-form :deep(.el-form-item) {
  margin-bottom: 12px;
  margin-right: 20px;
}

.search-form :deep(.el-form-item__label) {
  font-weight: 600;
  color: #606266;
}

.table-actions {
  margin-bottom: 15px;
  display: flex;
  align-items: center;
  gap: 10px;
}

.selected-count {
  color: #409eff;
  font-weight: 600;
}

.device-table {
  background: #fff;
  border-radius: 4px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.device-link {
  color: #409eff;
  text-decoration: none;
  font-weight: 500;
}

.device-link:hover {
  text-decoration: underline;
}

.unit {
  margin-left: 10px;
  color: #909399;
}

.pagination {
  margin-top: 20px;
  text-align: right;
}

.dialog-footer {
  text-align: right;
}

:deep(.el-input-number) {
  width: 100%;
}

:deep(.el-input-number .el-input__wrapper) {
  padding: 0 11px;
}
</style>