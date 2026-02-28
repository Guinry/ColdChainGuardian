<template>
  <Layout>
    <div class="device-alerts-content">
      <div class="page-header">
        <div class="header-left">
          <el-page-header @back="goBack" :content="`设备告警 - ${deviceName}`" />
        </div>
        <div class="header-right">
          <el-button @click="refreshData" :loading="loading">
            <el-icon><Refresh /></el-icon>
            刷新
          </el-button>
        </div>
      </div>

      <div class="alerts-content">
        <!-- 设备信息卡片 -->
        <el-card class="device-info-card">
          <template #header>
            <div class="card-header">
              <span>设备信息</span>
            </div>
          </template>
          <div class="device-info-content">
            <div class="info-row">
              <div class="info-item">
                <label>设备编码:</label>
                <span>{{ deviceCode }}</span>
              </div>
              <div class="info-item">
                <label>设备名称:</label>
                <span>{{ deviceName }}</span>
              </div>
              <div class="info-item">
                <label>设备类型:</label>
                <el-tag :type="getDeviceTypeTag(deviceType)">
                  {{ getDeviceTypeLabel(deviceType) }}
                </el-tag>
              </div>
            </div>
            <div class="info-row">
              <div class="info-item">
                <label>所属库区:</label>
                <span>{{ areaName }}</span>
              </div>
              <div class="info-item">
                <label>在线状态:</label>
                <el-tag :type="onlineStatus ? 'success' : 'info'">
                  {{ onlineStatus ? '在线' : '离线' }}
                </el-tag>
              </div>
              <div class="info-item">
                <label>告警状态:</label>
                <el-switch
                  v-model="alarmEnabled"
                  @change="toggleAlarmStatus"
                  :active-value="true"
                  :inactive-value="false"
                  active-text="开启"
                  inactive-text="关闭"
                />
              </div>
            </div>
          </div>
        </el-card>

        <!-- 筛选区 -->
        <el-card class="filter-card">
          <el-form :model="filterForm" inline class="filter-form">
            <el-form-item label="告警类型">
              <el-select v-model="filterForm.alertType" placeholder="请选择" clearable>
                <el-option label="温度过高" value="TEMP_HIGH" />
                <el-option label="温度过低" value="TEMP_LOW" />
                <el-option label="湿度过高" value="HUMI_HIGH" />
                <el-option label="湿度过低" value="HUMI_LOW" />
                <el-option label="设备离线" value="DEVICE_OFFLINE" />
              </el-select>
            </el-form-item>
            <el-form-item label="告警级别">
              <el-select v-model="filterForm.alertLevel" placeholder="请选择" clearable>
                <el-option label="低" value="LOW" />
                <el-option label="中" value="MEDIUM" />
                <el-option label="高" value="HIGH" />
                <el-option label="紧急" value="CRITICAL" />
              </el-select>
            </el-form-item>
            <el-form-item label="处理状态">
              <el-select v-model="filterForm.status" placeholder="请选择" clearable>
                <el-option label="未处理" value="UNHANDLED" />
                <el-option label="处理中" value="HANDLING" />
                <el-option label="已解决" value="RESOLVED" />
                <el-option label="已忽略" value="IGNORED" />
              </el-select>
            </el-form-item>
            <el-form-item label="时间范围">
              <el-date-picker
                v-model="filterForm.timeRange"
                type="datetimerange"
                range-separator="至"
                start-placeholder="开始时间"
                end-placeholder="结束时间"
              />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="searchAlerts">查询</el-button>
              <el-button @click="resetFilters">重置</el-button>
            </el-form-item>
          </el-form>
        </el-card>

        <!-- 告警列表 -->
        <el-card class="alerts-list-card">
          <template #header>
            <div class="card-header">
              <span>告警列表</span>
              <div class="list-controls">
                <el-button @click="batchHandleAlerts" :disabled="!selectedAlerts.length">批量处理</el-button>
                <el-button @click="exportAlerts">导出</el-button>
              </div>
            </div>
          </template>
          <el-table
            v-loading="loading"
            :data="alertsList"
            @selection-change="handleSelectionChange"
            style="width: 100%"
            height="calc(100vh - 400px)"
          >
            <el-table-column type="selection" width="55" />
            <el-table-column prop="alertType" label="告警类型" width="120">
              <template #default="{ row }">
                <el-tag :type="getAlertTypeTag(row.alertType)">
                  {{ getAlertTypeLabel(row.alertType) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="alertLevel" label="告警级别" width="100">
              <template #default="{ row }">
                <el-tag :type="getAlertLevelTag(row.alertLevel)">
                  {{ getAlertLevelLabel(row.alertLevel) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="message" label="告警信息" min-width="200" />
            <el-table-column prop="temperature" label="温度(℃)" width="120" />
            <el-table-column prop="humidity" label="湿度(%)" width="120" />
            <el-table-column prop="thresholdValue" label="阈值" width="120" />
            <el-table-column prop="status" label="处理状态" width="120">
              <template #default="{ row }">
                <el-tag :type="getAlertStatusTag(row.status)">
                  {{ getAlertStatusLabel(row.status) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="createdTime" label="发生时间" width="180">
              <template #default="{ row }">
                {{ formatDate(row.createdTime) }}
              </template>
            </el-table-column>
            <el-table-column prop="handleTime" label="处理时间" width="180">
              <template #default="{ row }">
                {{ formatDate(row.handleTime) }}
              </template>
            </el-table-column>
            <el-table-column label="操作" width="200">
              <template #default="{ row }">
                <el-button-group>
                  <el-button size="small" @click="viewAlertDetail(row)">详情</el-button>
                  <el-button size="small" type="primary" @click="handleAlert(row)" :disabled="row.status !== 'UNHANDLED'">
                    {{ row.status === 'UNHANDLED' ? '处理' : '已处理' }}
                  </el-button>
                  <el-button size="small" @click="ignoreAlert(row)" :disabled="row.status !== 'UNHANDLED'">
                    忽略
                  </el-button>
                </el-button-group>
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
        </el-card>
      </div>

      <!-- 告警详情对话框 -->
      <el-dialog
        v-model="alertDetailVisible"
        title="告警详情"
        width="600px"
      >
        <div v-if="currentAlert" class="alert-detail-content">
          <el-descriptions :column="1" border>
            <el-descriptions-item label="告警类型">
              <el-tag :type="getAlertTypeTag(currentAlert.alertType)">
                {{ getAlertTypeLabel(currentAlert.alertType) }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="告警级别">
              <el-tag :type="getAlertLevelTag(currentAlert.alertLevel)">
                {{ getAlertLevelLabel(currentAlert.alertLevel) }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="告警信息">
              {{ currentAlert.message }}
            </el-descriptions-item>
            <el-descriptions-item label="温度">
              {{ currentAlert.temperature || '-' }}℃
            </el-descriptions-item>
            <el-descriptions-item label="湿度">
              {{ currentAlert.humidity || '-' }}%
            </el-descriptions-item>
            <el-descriptions-item label="阈值">
              {{ currentAlert.thresholdValue || '-' }}
            </el-descriptions-item>
            <el-descriptions-item label="发生时间">
              {{ formatDate(currentAlert.createdTime) }}
            </el-descriptions-item>
            <el-descriptions-item label="处理状态">
              <el-tag :type="getAlertStatusTag(currentAlert.status)">
                {{ getAlertStatusLabel(currentAlert.status) }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="处理人">
              {{ currentAlert.handlerName || '-' }}
            </el-descriptions-item>
            <el-descriptions-item label="处理时间">
              {{ formatDate(currentAlert.handleTime) }}
            </el-descriptions-item>
            <el-descriptions-item label="处理备注">
              {{ currentAlert.handleRemark || '-' }}
            </el-descriptions-item>
          </el-descriptions>
        </div>
      </el-dialog>

      <!-- 处理告警对话框 -->
      <el-dialog
        v-model="handleAlertVisible"
        title="处理告警"
        width="500px"
      >
        <el-form :model="handleForm" label-width="100px">
          <el-form-item label="处理结果">
            <el-radio-group v-model="handleForm.result">
              <el-radio label="RESOLVED">已解决</el-radio>
              <el-radio label="IGNORED">已忽略</el-radio>
            </el-radio-group>
          </el-form-item>
          <el-form-item label="处理备注">
            <el-input
              v-model="handleForm.remark"
              type="textarea"
              :rows="4"
              placeholder="请输入处理备注"
            />
          </el-form-item>
        </el-form>
        <template #footer>
          <span class="dialog-footer">
            <el-button @click="handleAlertVisible = false">取消</el-button>
            <el-button type="primary" @click="confirmHandleAlert" :loading="handling">确定</el-button>
          </span>
        </template>
      </el-dialog>
    </div>
  </Layout>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Refresh } from '@element-plus/icons-vue'
import Layout from '@/components/Layout.vue'
import { deviceApi } from '@/api/device'
import { alertApi } from '@/api/alert'

// 路由
const route = useRoute()
const router = useRouter()

// 响应式数据
const loading = ref(false)
const handling = ref(false)

// 设备信息
const deviceName = ref('')
const deviceCode = ref('')
const deviceType = ref('')
const areaName = ref('')
const onlineStatus = ref(false)
const alarmEnabled = ref(true)

// 筛选条件
const filterForm = reactive({
  alertType: '',
  alertLevel: '',
  status: '',
  timeRange: []
})

// 告警列表
const alertsList = ref([])
const selectedAlerts = ref([])

// 分页信息
const pagination = reactive({
  currentPage: 1,
  pageSize: 10,
  total: 0
})

// 告警详情对话框
const alertDetailVisible = ref(false)
const currentAlert = ref(null)

// 处理告警对话框
const handleAlertVisible = ref(false)
const handleForm = reactive({
  result: 'RESOLVED',
  remark: ''
})

// 初始化
onMounted(() => {
  const deviceId = route.params.deviceId
  if (!deviceId) {
    ElMessage.error('设备ID不能为空')
    router.push('/devices')
    return
  }

  loadDeviceInfo(deviceId)
  loadAlertsList(deviceId)
})

// 加载设备信息
const loadDeviceInfo = async (deviceId) => {
  try {
    const response = await deviceApi.getDetail(deviceId)
    const device = response.data?.data?.data

    deviceName.value = device.deviceName
    deviceCode.value = device.deviceCode
    deviceType.value = device.deviceType
    areaName.value = device.areaName
    onlineStatus.value = device.onlineStatus
    alarmEnabled.value = device.alarmEnabled
  } catch (error) {
    ElMessage.error('获取设备信息失败')
    console.error(error)
  }
}

// 加载告警列表
const loadAlertsList = async (deviceId) => {
  loading.value = true
  try {
    const params = {
      deviceId,
      page: pagination.currentPage,
      size: pagination.pageSize,
      ...filterForm
    }

    // 转换时间范围
    if (filterForm.timeRange && filterForm.timeRange.length === 2) {
      params.startTime = filterForm.timeRange[0].toISOString()
      params.endTime = filterForm.timeRange[1].toISOString()
    }

    const response = await alertApi.getByDeviceId(deviceId, params)
    alertsList.value = response.data?.data?.data?.list || []
    pagination.total = response.data?.data?.data?.total || 0
  } catch (error) {
    ElMessage.error('获取告警列表失败')
    console.error(error)
  } finally {
    loading.value = false
  }
}

// 刷新数据
const refreshData = () => {
  const deviceId = route.params.deviceId
  loadDeviceInfo(deviceId)
  loadAlertsList(deviceId)
}

// 筛选和重置
const searchAlerts = () => {
  pagination.currentPage = 1
  loadAlertsList(route.params.deviceId)
}

const resetFilters = () => {
  Object.keys(filterForm).forEach(key => {
    if (Array.isArray(filterForm[key])) {
      filterForm[key] = []
    } else {
      filterForm[key] = null
    }
  })
  searchAlerts()
}

// 表格选择
const handleSelectionChange = (selection) => {
  selectedAlerts.value = selection
}

// 分页相关
const handleSizeChange = (size) => {
  pagination.pageSize = size
  loadAlertsList(route.params.deviceId)
}

const handleCurrentChange = (page) => {
  pagination.currentPage = page
  loadAlertsList(route.params.deviceId)
}

// 告警状态切换
const toggleAlarmStatus = async (enabled) => {
  try {
    await deviceApi.updateAlarmStatus(route.params.deviceId, enabled)
    ElMessage.success(enabled ? '告警开启成功' : '告警关闭成功')

    // 更新本地状态
    alarmEnabled.value = enabled
  } catch (error) {
    ElMessage.error('操作失败: ' + (error.message || '未知错误'))
    console.error(error)
    // 恢复开关状态
    alarmEnabled.value = !enabled
  }
}

// 查看告警详情
const viewAlertDetail = (alert) => {
  currentAlert.value = alert
  alertDetailVisible.value = true
}

// 处理告警
const handleAlert = async (alert) => {
  if (alert.status !== 'UNHANDLED') {
    ElMessage.info('该告警已被处理')
    return
  }

  handleForm.result = 'RESOLVED'
  handleForm.remark = ''
  currentAlert.value = alert
  handleAlertVisible.value = true
}

// 确认处理告警
const confirmHandleAlert = async () => {
  if (!currentAlert.value) return

  handling.value = true
  try {
    const params = {
      status: handleForm.result,
      handleRemark: handleForm.remark
    }

    await alertApi.updateStatus(currentAlert.value.id, params)
    ElMessage.success('告警处理成功')

    handleAlertVisible.value = false
    loadAlertsList(route.params.deviceId)
  } catch (error) {
    ElMessage.error('处理失败: ' + (error.message || '未知错误'))
    console.error(error)
  } finally {
    handling.value = false
  }
}

// 忽略告警
const ignoreAlert = async (alert) => {
  try {
    await ElMessageBox.confirm(
      '确定要忽略此告警吗？',
      '提示',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    await alertApi.updateStatus(alert.id, { status: 'IGNORED', handleRemark: '手动忽略' })
    ElMessage.success('告警已忽略')
    loadAlertsList(route.params.deviceId)
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('操作失败: ' + (error.message || '未知错误'))
      console.error(error)
    }
  }
}

// 批量处理告警
const batchHandleAlerts = async () => {
  if (!selectedAlerts.value.length) {
    ElMessage.warning('请先选择要处理的告警')
    return
  }

  try {
    const unhandledAlerts = selectedAlerts.value.filter(alert => alert.status === 'UNHANDLED')
    if (!unhandledAlerts.length) {
      ElMessage.warning('所选告警均已处理')
      return
    }

    await ElMessageBox.confirm(
      `确定要处理 ${unhandledAlerts.length} 个告警吗？`,
      '提示',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    const alertIds = unhandledAlerts.map(alert => alert.id)
    await alertApi.batchUpdateStatus(alertIds, { status: 'RESOLVED', handleRemark: '批量处理' })
    ElMessage.success('批量处理成功')
    loadAlertsList(route.params.deviceId)
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('批量处理失败: ' + (error.message || '未知错误'))
      console.error(error)
    }
  }
}

// 导出告警
const exportAlerts = () => {
  ElMessage.info('导出功能正在开发中...')
}

// 返回上一页
const goBack = () => {
  router.go(-1)
}

// 工具函数
const formatDate = (dateStr) => {
  if (!dateStr) return '-'
  const date = new Date(dateStr)
  return date.toLocaleString('zh-CN')
}

const getDeviceTypeLabel = (type) => {
  const labels = {
    'TEMP_HUM': '温湿度传感器',
    'FREEZER': '冷柜',
    'VEHICLE': '车载设备',
    'DOOR': '门磁'
  }
  return labels[type] || type
}

const getDeviceTypeTag = (type) => {
  const types = {
    'TEMP_HUM': 'primary',
    'FREEZER': 'success',
    'VEHICLE': 'warning',
    'DOOR': 'info'
  }
  return types[type] || 'default'
}

const getAlertTypeLabel = (type) => {
  const labels = {
    'TEMP_HIGH': '温度过高',
    'TEMP_LOW': '温度过低',
    'HUMI_HIGH': '湿度过高',
    'HUMI_LOW': '湿度过低',
    'DEVICE_OFFLINE': '设备离线'
  }
  return labels[type] || type
}

const getAlertTypeTag = (type) => {
  const types = {
    'TEMP_HIGH': 'danger',
    'TEMP_LOW': 'warning',
    'HUMI_HIGH': 'danger',
    'HUMI_LOW': 'warning',
    'DEVICE_OFFLINE': 'info'
  }
  return types[type] || 'default'
}

const getAlertLevelLabel = (level) => {
  const labels = {
    'LOW': '低',
    'MEDIUM': '中',
    'HIGH': '高',
    'CRITICAL': '紧急'
  }
  return labels[level] || level
}

const getAlertLevelTag = (level) => {
  const types = {
    'LOW': 'info',
    'MEDIUM': 'warning',
    'HIGH': 'danger',
    'CRITICAL': 'danger'
  }
  return types[level] || 'default'
}

const getAlertStatusLabel = (status) => {
  const labels = {
    'UNHANDLED': '未处理',
    'HANDLING': '处理中',
    'RESOLVED': '已解决',
    'IGNORED': '已忽略'
  }
  return labels[status] || status
}

const getAlertStatusTag = (status) => {
  const types = {
    'UNHANDLED': 'danger',
    'HANDLING': 'warning',
    'RESOLVED': 'success',
    'IGNORED': 'info'
  }
  return types[status] || 'default'
}
</script>

<style scoped>
.device-alerts-content {
  padding: 20px;
  height: 100%;
  display: flex;
  flex-direction: column;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.header-left {
  flex: 1;
}

.header-right {
  flex-shrink: 0;
}

.alerts-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 20px;
  min-height: 0;
}

.device-info-card {
  margin-bottom: 20px;
}

.info-row {
  display: flex;
  flex-wrap: wrap;
  margin-bottom: 10px;
}

.info-item {
  flex: 1 0 30%;
  min-width: 200px;
  margin-bottom: 10px;
}

.info-item label {
  font-weight: 600;
  color: #606266;
  margin-right: 8px;
  min-width: 80px;
  display: inline-block;
}

.filter-card {
  margin-bottom: 20px;
}

.filter-form {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.filter-form .el-form-item {
  margin-bottom: 12px;
}

.alerts-list-card {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.list-controls {
  display: flex;
  gap: 10px;
}

.pagination {
  margin-top: 15px;
  text-align: right;
}

.alert-detail-content {
  max-height: 600px;
  overflow-y: auto;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .info-item {
    flex: 1 0 100%;
  }

  .filter-form {
    flex-direction: column;
  }

  .filter-form .el-form-item {
    width: 100%;
  }

  .card-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;
  }

  .list-controls {
    width: 100%;
    margin-top: 10px;
  }
}
</style>