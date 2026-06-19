<template>
  <el-dialog
    v-model="dialogVisible"
    :width="dialogWidth"
    class="device-insight-dialog"
    destroy-on-close
    append-to-body
    @opened="handleOpened"
    @closed="handleClosed"
  >
    <template #header>
      <div class="insight-header">
        <div class="device-title-block">
          <div class="device-title-line">
            <span class="device-name">{{ normalizedDevice.deviceName || '设备详情' }}</span>
            <el-tag size="small" :type="isOnline ? 'success' : 'info'">
              {{ isOnline ? '在线' : '离线' }}
            </el-tag>
            <el-tag v-if="hasUnresolvedAlert" size="small" type="danger">有告警</el-tag>
          </div>
          <div class="device-subtitle">
            {{ normalizedDevice.deviceCode || '-' }} · {{ getDeviceTypeLabel(normalizedDevice.deviceType) }}
            <span v-if="normalizedDevice.areaPath || normalizedDevice.areaName">
              · {{ normalizedDevice.areaPath || normalizedDevice.areaName }}
            </span>
          </div>
        </div>
        <div class="device-metrics">
          <div class="metric-chip">
            <span class="metric-label">温度</span>
            <strong>{{ formatNumber(latestTemperature, 1) }}</strong>
            <span>℃</span>
          </div>
          <div class="metric-chip humidity">
            <span class="metric-label">湿度</span>
            <strong>{{ formatNumber(latestHumidity, 1) }}</strong>
            <span>%</span>
          </div>
        </div>
      </div>
    </template>

    <div v-if="device" class="insight-body">
      <el-tabs v-model="activeTab" class="insight-tabs">
        <el-tab-pane label="数据趋势" name="data">
          <div class="tab-toolbar">
            <el-segmented
              v-model="rangeKey"
              :options="rangeOptions"
              @change="handleRangeChange"
            />
            <div class="toolbar-actions">
              <span class="range-tip">{{ rangeTip }}</span>
              <el-button :icon="Refresh" :loading="dataLoading" @click="loadData">刷新</el-button>
            </div>
          </div>

          <div class="chart-shell" v-loading="dataLoading">
            <div v-show="historyRows.length" ref="chartRef" class="trend-chart"></div>
            <el-empty
              v-if="!dataLoading && !historyRows.length"
              description="当前时间范围暂无设备数据"
              class="empty-state"
            />
          </div>

          <div class="history-table-wrap">
            <div class="section-title">历史上报明细</div>
            <el-table
              :data="historyRows"
              height="220"
              size="small"
              class="compact-table"
            >
              <el-table-column prop="dataTime" label="上报时间" min-width="170">
                <template #default="{ row }">{{ formatDate(row.dataTime) }}</template>
              </el-table-column>
              <el-table-column prop="temperature" label="温度(℃)" width="104" align="right">
                <template #default="{ row }">{{ formatNumber(row.temperature, 2) }}</template>
              </el-table-column>
              <el-table-column prop="humidity" label="湿度(%)" width="104" align="right">
                <template #default="{ row }">{{ formatNumber(row.humidity, 2) }}</template>
              </el-table-column>
              <el-table-column prop="batteryLevel" label="电量(%)" width="96" align="right">
                <template #default="{ row }">{{ formatNumber(row.batteryLevel, 0) }}</template>
              </el-table-column>
              <el-table-column prop="signalStrength" label="信号" width="88" align="right">
                <template #default="{ row }">{{ row.signalStrength ?? '-' }}</template>
              </el-table-column>
            </el-table>
          </div>
        </el-tab-pane>

        <el-tab-pane label="告警记录" name="alerts">
          <div class="tab-toolbar alert-toolbar">
            <div class="alert-filters">
              <el-select v-model="alertFilters.status" placeholder="处理状态" clearable @change="searchAlerts">
                <el-option label="未处理" value="UNHANDLED" />
                <el-option label="处理中" value="HANDLING" />
                <el-option label="已解决" value="RESOLVED" />
                <el-option label="已忽略" value="IGNORED" />
              </el-select>
              <el-select v-model="alertFilters.alertLevel" placeholder="告警级别" clearable @change="searchAlerts">
                <el-option label="低" value="LOW" />
                <el-option label="中" value="MEDIUM" />
                <el-option label="高" value="HIGH" />
                <el-option label="紧急" value="CRITICAL" />
              </el-select>
            </div>
            <el-button :icon="Refresh" :loading="alertLoading" @click="loadAlerts">刷新</el-button>
          </div>

          <el-table
            v-loading="alertLoading"
            :data="alertRows"
            height="430"
            class="compact-table"
          >
            <el-table-column prop="alertType" label="类型" width="112">
              <template #default="{ row }">
                <el-tag size="small" :type="getAlertTypeTag(row.alertType)">
                  {{ getAlertTypeLabel(row.alertType) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="alertLevel" label="级别" width="82" align="center">
              <template #default="{ row }">
                <el-tag size="small" :type="getAlertLevelType(row.alertLevel)">
                  {{ getAlertLevelText(row.alertLevel) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="description" label="告警信息" min-width="220" show-overflow-tooltip />
            <el-table-column prop="temperature" label="温度" width="82" align="right">
              <template #default="{ row }">{{ formatNumber(row.temperature, 1) }}</template>
            </el-table-column>
            <el-table-column prop="humidity" label="湿度" width="82" align="right">
              <template #default="{ row }">{{ formatNumber(row.humidity, 1) }}</template>
            </el-table-column>
            <el-table-column prop="status" label="状态" width="96" align="center">
              <template #default="{ row }">
                <el-tag size="small" :type="getAlertStatusType(row.status)">
                  {{ getAlertStatusText(row.status) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="createdTime" label="发生时间" min-width="160">
              <template #default="{ row }">{{ formatDate(row.createdTime) }}</template>
            </el-table-column>
            <el-table-column label="操作" width="130" fixed="right" align="center">
              <template #default="{ row }">
                <div class="alert-actions">
                  <el-button
                    type="primary"
                    link
                    size="small"
                    :disabled="row.status === 'RESOLVED' || row.resolved"
                    @click="resolveAlert(row)"
                  >
                    处理
                  </el-button>
                  <el-button link size="small" @click="openAlertDetail(row)">详情</el-button>
                </div>
              </template>
            </el-table-column>
          </el-table>

          <div class="dialog-pagination">
            <el-pagination
              v-model:current-page="alertPagination.currentPage"
              v-model:page-size="alertPagination.pageSize"
              :page-sizes="[10, 20, 50]"
              layout="total, sizes, prev, pager, next"
              :total="alertPagination.total"
              @size-change="handleAlertSizeChange"
              @current-change="handleAlertPageChange"
            />
          </div>
        </el-tab-pane>

        <el-tab-pane label="设备信息" name="detail">
          <div class="detail-grid">
            <el-descriptions :column="2" border>
              <el-descriptions-item label="设备编码">{{ normalizedDevice.deviceCode || '-' }}</el-descriptions-item>
              <el-descriptions-item label="设备名称">{{ normalizedDevice.deviceName || '-' }}</el-descriptions-item>
              <el-descriptions-item label="设备类型">{{ getDeviceTypeLabel(normalizedDevice.deviceType) }}</el-descriptions-item>
              <el-descriptions-item label="所属库区">{{ normalizedDevice.areaPath || normalizedDevice.areaName || '未分配' }}</el-descriptions-item>
              <el-descriptions-item label="型号">{{ normalizedDevice.model || '-' }}</el-descriptions-item>
              <el-descriptions-item label="固件版本">{{ normalizedDevice.firmwareVersion || '-' }}</el-descriptions-item>
              <el-descriptions-item label="最后在线">{{ formatDate(normalizedDevice.lastSeenTime) }}</el-descriptions-item>
              <el-descriptions-item label="告警开关">
                <el-tag :type="normalizedDevice.alarmEnabled === false ? 'info' : 'success'">
                  {{ normalizedDevice.alarmEnabled === false ? '关闭' : '启用' }}
                </el-tag>
              </el-descriptions-item>
              <el-descriptions-item label="温度阈值">
                {{ thresholdText(normalizedDevice.temperatureThresholdMin, normalizedDevice.temperatureThresholdMax, '℃') }}
              </el-descriptions-item>
              <el-descriptions-item label="湿度阈值">
                {{ thresholdText(normalizedDevice.humidityThresholdMin, normalizedDevice.humidityThresholdMax, '%') }}
              </el-descriptions-item>
              <el-descriptions-item label="位置说明" :span="2">
                {{ normalizedDevice.locationDesc || '暂无位置说明' }}
              </el-descriptions-item>
            </el-descriptions>
          </div>
        </el-tab-pane>
      </el-tabs>
    </div>

    <el-empty v-else description="请选择设备" />

    <el-dialog v-model="alertDetailVisible" title="告警详情" width="560px" append-to-body>
      <el-descriptions v-if="currentAlert" :column="1" border>
        <el-descriptions-item label="告警类型">{{ getAlertTypeLabel(currentAlert.alertType) }}</el-descriptions-item>
        <el-descriptions-item label="告警级别">{{ getAlertLevelText(currentAlert.alertLevel) }}</el-descriptions-item>
        <el-descriptions-item label="告警信息">{{ currentAlert.description || '-' }}</el-descriptions-item>
        <el-descriptions-item label="温度">{{ formatNumber(currentAlert.temperature, 2) }} ℃</el-descriptions-item>
        <el-descriptions-item label="湿度">{{ formatNumber(currentAlert.humidity, 2) }} %</el-descriptions-item>
        <el-descriptions-item label="发生时间">{{ formatDate(currentAlert.createdTime) }}</el-descriptions-item>
        <el-descriptions-item label="处理状态">{{ getAlertStatusText(currentAlert.status) }}</el-descriptions-item>
        <el-descriptions-item label="处理备注">{{ currentAlert.handleRemark || '-' }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </el-dialog>
</template>

<script setup>
import { computed, nextTick, onBeforeUnmount, reactive, ref, watch } from 'vue'
import * as echarts from 'echarts'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Refresh } from '@element-plus/icons-vue'
import { deviceApi } from '@/api/device'
import { alertApi } from '@/api/alert'

const props = defineProps({
  modelValue: {
    type: Boolean,
    default: false
  },
  device: {
    type: Object,
    default: null
  },
  initialTab: {
    type: String,
    default: 'data'
  }
})

const emit = defineEmits(['update:modelValue', 'alert-updated'])

const dialogVisible = computed({
  get: () => props.modelValue,
  set: (value) => emit('update:modelValue', value)
})

const dialogWidth = computed(() => (window.innerWidth < 900 ? '96vw' : '1080px'))
const activeTab = ref('data')
const rangeKey = ref('24h')
const chartRef = ref(null)
const dataLoading = ref(false)
const alertLoading = ref(false)
const historyRows = ref([])
const alertRows = ref([])
const currentAlert = ref(null)
const alertDetailVisible = ref(false)
let chartInstance = null

const rangeOptions = [
  { label: '1小时', value: '1h' },
  { label: '12小时', value: '12h' },
  { label: '24小时', value: '24h' },
  { label: '7天', value: '7d' },
  { label: '30天', value: '30d' }
]

const alertFilters = reactive({
  status: '',
  alertLevel: ''
})

const alertPagination = reactive({
  currentPage: 1,
  pageSize: 10,
  total: 0
})

const normalizedDevice = computed(() => props.device || {})
const isOnline = computed(() => normalizedDevice.value.online ?? normalizedDevice.value.onlineStatus ?? false)
const hasUnresolvedAlert = computed(() => normalizedDevice.value.hasUnresolvedAlert || false)
const latestTemperature = computed(() => normalizedDevice.value.latestTemp ?? historyRows.value[0]?.temperature)
const latestHumidity = computed(() => normalizedDevice.value.latestHumi ?? historyRows.value[0]?.humidity)

const rangeTip = computed(() => {
  const option = rangeOptions.find(item => item.value === rangeKey.value)
  return option ? `最近${option.label}` : ''
})

const resetDialogState = () => {
  activeTab.value = props.initialTab || 'data'
  historyRows.value = []
  alertRows.value = []
  alertPagination.currentPage = 1
  alertPagination.total = 0
  alertFilters.status = ''
  alertFilters.alertLevel = ''
}

const disposeChart = () => {
  if (chartInstance) {
    chartInstance.dispose()
    chartInstance = null
  }
}

const toNumberOrNull = (value) => {
  const numberValue = Number(value)
  return Number.isFinite(numberValue) ? numberValue : null
}

const formatLocalDateTime = (date) => {
  const pad = (value) => String(value).padStart(2, '0')
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())}T${pad(date.getHours())}:${pad(date.getMinutes())}:${pad(date.getSeconds())}`
}

const getRange = () => {
  const end = new Date()
  const start = new Date(end)
  switch (rangeKey.value) {
    case '1h':
      start.setHours(start.getHours() - 1)
      break
    case '12h':
      start.setHours(start.getHours() - 12)
      break
    case '7d':
      start.setDate(start.getDate() - 7)
      break
    case '30d':
      start.setDate(start.getDate() - 30)
      break
    case '24h':
    default:
      start.setDate(start.getDate() - 1)
      break
  }
  return { start, end }
}

const sortByTimeAsc = (rows) => [...rows].sort((a, b) => new Date(a.dataTime || 0) - new Date(b.dataTime || 0))

const loadData = async () => {
  if (!props.device?.id) return

  dataLoading.value = true
  try {
    const { start, end } = getRange()
    const response = await deviceApi.getHistoricalData(props.device.id, {
      page: 1,
      size: rangeKey.value === '30d' ? 1000 : 500,
      startTime: formatLocalDateTime(start),
      endTime: formatLocalDateTime(end)
    })
    historyRows.value = response.data?.data?.data || []
    await nextTick()
    renderChart()
  } catch (error) {
    console.error('加载设备趋势失败:', error)
    historyRows.value = []
    disposeChart()
    ElMessage.error(error.response?.data?.message || '加载设备趋势失败')
  } finally {
    dataLoading.value = false
  }
}

const renderChart = () => {
  if (!chartRef.value || !historyRows.value.length) {
    disposeChart()
    return
  }

  if (!chartInstance) {
    chartInstance = echarts.init(chartRef.value)
  }

  const rows = sortByTimeAsc(historyRows.value)
  const xData = rows.map(row => formatChartTime(row.dataTime))
  const temperatures = rows.map(row => toNumberOrNull(row.temperature))
  const humidities = rows.map(row => toNumberOrNull(row.humidity))

  chartInstance.setOption({
    color: ['#2563eb', '#16a34a'],
    tooltip: {
      trigger: 'axis',
      valueFormatter: value => (value === null || value === undefined ? '-' : value)
    },
    legend: {
      top: 0,
      right: 8,
      data: ['温度(℃)', '湿度(%)']
    },
    grid: {
      top: 42,
      left: 44,
      right: 46,
      bottom: 34,
      containLabel: true
    },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: xData,
      axisLabel: {
        color: '#6b7280',
        hideOverlap: true
      },
      axisLine: {
        lineStyle: { color: '#d8dee9' }
      }
    },
    yAxis: [
      {
        type: 'value',
        name: '温度(℃)',
        min: value => Number.isFinite(value.min) ? Math.floor(Math.min(value.min, -5)) : -30,
        axisLabel: { formatter: '{value}' },
        splitLine: { lineStyle: { color: '#edf0f5' } }
      },
      {
        type: 'value',
        name: '湿度(%)',
        min: 0,
        max: 100,
        axisLabel: { formatter: '{value}' },
        splitLine: { show: false }
      }
    ],
    series: [
      {
        name: '温度(℃)',
        type: 'line',
        yAxisIndex: 0,
        smooth: true,
        symbol: 'circle',
        symbolSize: 5,
        connectNulls: false,
        data: temperatures,
        lineStyle: { width: 2.5 }
      },
      {
        name: '湿度(%)',
        type: 'line',
        yAxisIndex: 1,
        smooth: true,
        symbol: 'circle',
        symbolSize: 5,
        connectNulls: false,
        data: humidities,
        lineStyle: { width: 2.5 }
      }
    ]
  })

  requestAnimationFrame(() => chartInstance?.resize())
}

const loadAlerts = async () => {
  if (!props.device?.id) return

  alertLoading.value = true
  try {
    const response = await alertApi.getByDeviceId(props.device.id, {
      page: alertPagination.currentPage,
      size: alertPagination.pageSize,
      status: alertFilters.status || undefined,
      alertLevel: alertFilters.alertLevel || undefined
    })
    alertRows.value = response.data?.data?.data || []
    alertPagination.total = response.data?.data?.total || 0
  } catch (error) {
    console.error('加载设备告警失败:', error)
    alertRows.value = []
    alertPagination.total = 0
    ElMessage.error(error.response?.data?.message || '加载设备告警失败')
  } finally {
    alertLoading.value = false
  }
}

const loadCurrentTab = () => {
  if (activeTab.value === 'data') {
    loadData()
  } else if (activeTab.value === 'alerts') {
    loadAlerts()
  } else {
    nextTick(() => chartInstance?.resize())
  }
}

const handleOpened = async () => {
  resetDialogState()
  await nextTick()
  loadCurrentTab()
}

const handleClosed = () => {
  disposeChart()
  resetDialogState()
}

const handleRangeChange = () => {
  loadData()
}

const searchAlerts = () => {
  alertPagination.currentPage = 1
  loadAlerts()
}

const handleAlertSizeChange = (size) => {
  alertPagination.pageSize = size
  alertPagination.currentPage = 1
  loadAlerts()
}

const handleAlertPageChange = (page) => {
  alertPagination.currentPage = page
  loadAlerts()
}

const openAlertDetail = (alert) => {
  currentAlert.value = alert
  alertDetailVisible.value = true
}

const resolveAlert = async (alert) => {
  try {
    const { value } = await ElMessageBox.prompt('请输入处理备注', '处理告警', {
      confirmButtonText: '标记已解决',
      cancelButtonText: '取消',
      inputType: 'textarea',
      inputPlaceholder: '例如：现场复核后设备恢复正常'
    })

    await alertApi.updateStatus(alert.id, {
      status: 'RESOLVED',
      handleRemark: value || '设备弹窗快速处理'
    })
    ElMessage.success('告警已处理')
    emit('alert-updated')
    loadAlerts()
  } catch (error) {
    if (error !== 'cancel' && error !== 'close') {
      console.error('处理告警失败:', error)
      ElMessage.error(error.response?.data?.message || '处理告警失败')
    }
  }
}

const getDeviceTypeLabel = (type) => {
  const labels = {
    TEMP_HUM: '温湿度传感器',
    FREEZER: '冷柜',
    VEHICLE: '车载设备',
    DOOR: '门磁'
  }
  return labels[type] || type || '-'
}

const getAlertTypeLabel = (type) => {
  const labels = {
    TEMP_HIGH: '温度过高',
    TEMP_LOW: '温度过低',
    HUMI_HIGH: '湿度过高',
    HUMI_LOW: '湿度过低',
    DEVICE_OFFLINE: '设备离线'
  }
  return labels[type] || type || '-'
}

const getAlertTypeTag = (type) => {
  const tags = {
    TEMP_HIGH: 'danger',
    TEMP_LOW: 'warning',
    HUMI_HIGH: 'danger',
    HUMI_LOW: 'warning',
    DEVICE_OFFLINE: 'info'
  }
  return tags[type] || 'info'
}

const getAlertLevelType = (level) => {
  const tags = {
    LOW: 'info',
    MEDIUM: 'warning',
    HIGH: 'danger',
    CRITICAL: 'danger'
  }
  return tags[level] || 'info'
}

const getAlertLevelText = (level) => {
  const labels = {
    LOW: '低',
    MEDIUM: '中',
    HIGH: '高',
    CRITICAL: '紧急'
  }
  return labels[level] || level || '-'
}

const getAlertStatusType = (status) => {
  const tags = {
    UNHANDLED: 'danger',
    HANDLING: 'warning',
    RESOLVED: 'success',
    IGNORED: 'info'
  }
  return tags[status] || 'info'
}

const getAlertStatusText = (status) => {
  const labels = {
    UNHANDLED: '未处理',
    HANDLING: '处理中',
    RESOLVED: '已解决',
    IGNORED: '已忽略'
  }
  return labels[status] || status || '-'
}

const formatDate = (value) => {
  if (!value) return '-'
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return String(value)
  return date.toLocaleString('zh-CN')
}

const formatChartTime = (value) => {
  if (!value) return '-'
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return String(value)
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  const hour = String(date.getHours()).padStart(2, '0')
  const minute = String(date.getMinutes()).padStart(2, '0')
  return rangeKey.value.endsWith('d') ? `${month}-${day} ${hour}:${minute}` : `${hour}:${minute}`
}

const formatNumber = (value, precision = 1) => {
  const numberValue = Number(value)
  if (!Number.isFinite(numberValue)) return '-'
  return numberValue.toFixed(precision)
}

const thresholdText = (min, max, unit) => {
  const left = Number.isFinite(Number(min)) ? `${min}${unit}` : '未设'
  const right = Number.isFinite(Number(max)) ? `${max}${unit}` : '未设'
  return `${left} ~ ${right}`
}

watch(activeTab, async () => {
  if (!dialogVisible.value) return
  await nextTick()
  loadCurrentTab()
})

watch(() => props.device?.id, () => {
  if (dialogVisible.value) {
    handleOpened()
  }
})

const resizeHandler = () => chartInstance?.resize()
window.addEventListener('resize', resizeHandler)

onBeforeUnmount(() => {
  window.removeEventListener('resize', resizeHandler)
  disposeChart()
})
</script>

<style scoped>
:global(.device-insight-dialog) {
  border-radius: 12px;
  overflow: hidden;
}

:global(.device-insight-dialog .el-dialog__header) {
  padding: 18px 22px 14px;
  margin: 0;
  border-bottom: 1px solid #e5e7eb;
}

:global(.device-insight-dialog .el-dialog__body) {
  padding: 0 22px 22px;
}

.insight-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 18px;
  padding-right: 30px;
}

.device-title-block {
  min-width: 0;
}

.device-title-line {
  display: flex;
  align-items: center;
  gap: 8px;
  min-width: 0;
}

.device-name {
  overflow: hidden;
  color: #111827;
  font-size: 18px;
  font-weight: 700;
  line-height: 1.35;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.device-subtitle {
  margin-top: 4px;
  color: #6b7280;
  font-size: 13px;
}

.device-metrics {
  display: flex;
  gap: 10px;
  flex-shrink: 0;
}

.metric-chip {
  display: grid;
  grid-template-columns: auto auto auto;
  align-items: baseline;
  gap: 4px;
  min-width: 106px;
  padding: 8px 10px;
  border: 1px solid #dbeafe;
  border-radius: 8px;
  background: #eff6ff;
  color: #1d4ed8;
}

.metric-chip.humidity {
  border-color: #dcfce7;
  background: #f0fdf4;
  color: #15803d;
}

.metric-label {
  grid-column: 1 / -1;
  color: #6b7280;
  font-size: 12px;
}

.metric-chip strong {
  font-size: 18px;
}

.insight-body {
  min-height: 590px;
}

.insight-tabs :deep(.el-tabs__header) {
  margin-bottom: 14px;
}

.tab-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 14px;
  margin-bottom: 14px;
}

.toolbar-actions,
.alert-filters {
  display: flex;
  align-items: center;
  gap: 10px;
}

.alert-filters .el-select {
  width: 132px;
}

.range-tip {
  color: #6b7280;
  font-size: 13px;
}

.chart-shell {
  position: relative;
  min-height: 334px;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  background: #ffffff;
}

.trend-chart {
  width: 100%;
  height: 334px;
}

.empty-state {
  height: 334px;
}

.history-table-wrap {
  margin-top: 14px;
}

.section-title {
  margin-bottom: 8px;
  color: #111827;
  font-size: 14px;
  font-weight: 650;
}

.compact-table {
  border: 1px solid #e5e7eb;
  border-radius: 8px;
}

.compact-table :deep(.el-table__cell) {
  padding: 8px 0;
}

.alert-toolbar {
  align-items: flex-start;
}

.alert-actions {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 4px;
  white-space: nowrap;
}

.dialog-pagination {
  display: flex;
  justify-content: flex-end;
  margin-top: 12px;
}

.detail-grid {
  padding-top: 4px;
}

@media (max-width: 900px) {
  .insight-header,
  .tab-toolbar {
    flex-direction: column;
    align-items: stretch;
  }

  .device-metrics,
  .toolbar-actions,
  .alert-filters {
    flex-wrap: wrap;
  }
}
</style>
