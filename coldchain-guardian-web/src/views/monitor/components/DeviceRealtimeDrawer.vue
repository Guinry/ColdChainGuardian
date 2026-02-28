<template>
  <el-drawer
    v-model="localVisible"
    :title="getTitle"
    size="50%"
    :before-close="handleClose"
    destroy-on-close
  >
    <div v-if="device" class="drawer-content">
      <el-tabs v-model="activeTab" class="drawer-tabs">
        <!-- 实时数据标签 -->
        <el-tab-pane label="实时数据" name="detail">
          <div class="detail-section">
            <el-descriptions :column="2" border>
              <el-descriptions-item label="设备编码">{{ device.deviceCode }}</el-descriptions-item>
              <el-descriptions-item label="设备名称">{{ device.deviceName }}</el-descriptions-item>
              <el-descriptions-item label="设备类型">{{ getDeviceTypeLabel(device.deviceType) }}</el-descriptions-item>
              <el-descriptions-item label="所属库区">{{ device.areaPath || '未分配' }}</el-descriptions-item>
              <el-descriptions-item label="最新温度">
                <span :class="getTempClass(device.latestTemp, device.temperatureThresholdMin, device.temperatureThresholdMax)">
                  {{ device.latestTemp !== null ? device.latestTemp + '℃' : '未上报' }}
                </span>
              </el-descriptions-item>
              <el-descriptions-item label="最新湿度">
                <span :class="getHumiClass(device.latestHumi, device.humidityThresholdMin, device.humidityThresholdMax)">
                  {{ device.latestHumi !== null ? device.latestHumi + '%' : '未上报' }}
                </span>
              </el-descriptions-item>
              <el-descriptions-item label="数据时间">
                {{ device.latestDataTime ? formatDate(device.latestDataTime) : '未上报' }}
              </el-descriptions-item>
              <el-descriptions-item label="最后上报时间">
                {{ device.lastSeenTime ? formatDate(device.lastSeenTime) : '从未上报' }}
              </el-descriptions-item>
              <el-descriptions-item label="在线状态">
                <el-tag :type="device.online ? 'success' : 'danger'">
                  {{ device.online ? '在线' : '离线' }}
                </el-tag>
              </el-descriptions-item>
              <el-descriptions-item label="告警状态">
                <el-tag v-if="device.hasUnresolvedAlert" :type="getAlertLevelType(device.highestAlertLevel)">
                  {{ getAlertLevelText(device.highestAlertLevel) }}
                </el-tag>
                <span v-else>正常</span>
              </el-descriptions-item>
            </el-descriptions>

            <div class="threshold-section">
              <h4>阈值设置</h4>
              <div class="threshold-info">
                <p>温度阈值: {{ device.temperatureThresholdMin || '无' }}°C ~ {{ device.temperatureThresholdMax || '无' }}°C</p>
                <p>湿度阈值: {{ device.humidityThresholdMin || '无' }}% ~ {{ device.humidityThresholdMax || '无' }}%</p>
                <p>阈值模式: {{ getThresholdModeLabel(device.thresholdMode) }}</p>
              </div>
            </div>
          </div>
        </el-tab-pane>

        <!-- 趋势曲线标签 -->
        <el-tab-pane label="趋势曲线" name="trend">
          <div class="trend-section">
            <div class="trend-controls">
              <el-radio-group v-model="trendRange" @change="loadTrendData">
                <el-radio-button label="1h">1小时</el-radio-button>
                <el-radio-button label="6h">6小时</el-radio-button>
                <el-radio-button label="24h">24小时</el-radio-button>
                <el-radio-button label="7d">7天</el-radio-button>
              </el-radio-group>
            </div>

            <div ref="chartRef" class="chart-container" v-loading="trendLoading"></div>
          </div>
        </el-tab-pane>

        <!-- 告警标签 -->
        <el-tab-pane label="告警记录" name="alert">
          <div class="alert-section">
            <el-table :data="alertList" :loading="alertLoading" style="width: 100%" border>
              <el-table-column prop="alertType" label="告警类型" width="120" />
              <el-table-column prop="alertLevel" label="级别" width="80">
                <template #default="{ row }">
                  <el-tag :type="getAlertLevelType(row.alertLevel)">
                    {{ getAlertLevelText(row.alertLevel) }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="temperature" label="温度" width="100">
                <template #default="{ row }">
                  {{ row.temperature !== null ? row.temperature + '℃' : '-' }}
                </template>
              </el-table-column>
              <el-table-column prop="humidity" label="湿度" width="100">
                <template #default="{ row }">
                  {{ row.humidity !== null ? row.humidity + '%' : '-' }}
                </template>
              </el-table-column>
              <el-table-column prop="status" label="状态" width="100">
                <template #default="{ row }">
                  <el-tag :type="getStatusType(row.status)">
                    {{ getStatusText(row.status) }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="createdTime" label="创建时间" width="160" />
              <el-table-column label="操作" width="150">
                <template #default="{ row }">
                  <el-button size="small" @click="handleAcknowledge(row)">确认</el-button>
                  <el-button size="small" type="primary" @click="handleHandle(row)">处理</el-button>
                </template>
              </el-table-column>
            </el-table>

            <el-pagination
              class="pagination-container"
              v-model:currentPage="alertPagination.currentPage"
              v-model:pageSize="alertPagination.pageSize"
              :page-sizes="[5, 10, 20]"
              layout="total, sizes, prev, pager, next"
              :total="alertPagination.total"
              @size-change="handleAlertSizeChange"
              @current-change="handleAlertPageChange"
            />
          </div>
        </el-tab-pane>
      </el-tabs>
    </div>
  </el-drawer>
</template>

<script setup>
import { ref, computed, watch, nextTick } from 'vue'
import { useEcharts } from '@/composables/useEcharts'
import { monitorApi } from '@/api/monitor'
import { alertApi } from '@/api/alert' // 假设存在告警API

const props = defineProps({
  visible: {
    type: Boolean,
    default: false
  },
  device: {
    type: Object,
    default: null
  },
  type: {
    type: String,
    default: 'detail' // 'detail', 'trend', 'alert'
  }
})

const emit = defineEmits(['update:visible', 'close'])

// 响应式数据
const localVisible = ref(props.visible)
const activeTab = ref(props.type)
const trendRange = ref('1h')
const trendLoading = ref(false)
const alertList = ref([])
const alertLoading = ref(false)
const chartRef = ref(null)

// 图表实例
let chartInstance = null

// 告警分页
const alertPagination = ref({
  currentPage: 1,
  pageSize: 10,
  total: 0
})

// 监听属性变化
watch(() => props.visible, (val) => {
  localVisible.value = val
})

watch(localVisible, (val) => {
  if (val) {
    activeTab.value = props.type
    if (activeTab.value === 'trend') {
      nextTick(() => loadTrendData())
    } else if (activeTab.value === 'alert') {
      loadAlertData()
    }
  } else {
    emit('update:visible', false)
    emit('close')
  }
})

watch(activeTab, (tab) => {
  if (tab === 'trend' && props.device) {
    nextTick(() => loadTrendData())
  } else if (tab === 'alert' && props.device) {
    loadAlertData()
  }
})

// 计算属性
const getTitle = computed(() => {
  if (!props.device) return '设备详情'
  return `${props.device.deviceName} (${props.device.deviceCode})`
})

// 方法
const handleClose = () => {
  emit('update:visible', false)
  emit('close')
}

const formatDate = (dateStr) => {
  if (!dateStr) return '-'
  const date = new Date(dateStr)
  return date.toLocaleString()
}

const getDeviceTypeLabel = (type) => {
  const typeMap = {
    'TEMP_HUM': '温湿度传感器',
    'FREEZER': '冷柜',
    'VEHICLE': '车载设备',
    'DOOR': '门磁'
  }
  return typeMap[type] || type
}

const getThresholdModeLabel = (mode) => {
  const modeMap = {
    'INHERIT': '继承库区',
    'OVERRIDE': '设备独立'
  }
  return modeMap[mode] || mode
}

const isOverThreshold = (value, min, max, type) => {
  if (value === null || value === undefined) return false
  if (type === 'temp') {
    return (min !== null && value < min) || (max !== null && value > max)
  } else {
    return (min !== null && value < min) || (max !== null && value > max)
  }
}

const isNearThreshold = (value, min, max, type) => {
  if (value === null || value === undefined) return false
  if (type === 'temp') {
    if (min !== null && Math.abs(value - min) < 0.5) return true
    if (max !== null && Math.abs(value - max) < 0.5) return true
  } else {
    if (min !== null && Math.abs(value - min) < 0.5) return true
    if (max !== null && Math.abs(value - max) < 0.5) return true
  }
  return false
}

const getTempClass = (temp, min, max) => {
  if (isOverThreshold(temp, min, max, 'temp')) return 'over-threshold'
  if (isNearThreshold(temp, min, max, 'temp')) return 'near-threshold'
  return temp !== null ? 'normal-temp' : 'no-data'
}

const getHumiClass = (humi, min, max) => {
  if (isOverThreshold(humi, min, max, 'humi')) return 'over-threshold'
  if (isNearThreshold(humi, min, max, 'humi')) return 'near-threshold'
  return humi !== null ? 'normal-humi' : 'no-data'
}

const getAlertLevelType = (level) => {
  const typeMap = {
    'LOW': 'info',
    'MEDIUM': 'warning',
    'HIGH': 'danger',
    'CRITICAL': 'error'
  }
  return typeMap[level] || 'info'
}

const getAlertLevelText = (level) => {
  const textMap = {
    'LOW': '低',
    'MEDIUM': '中',
    'HIGH': '高',
    'CRITICAL': '危'
  }
  return textMap[level] || level
}

const getStatusType = (status) => {
  const typeMap = {
    'UNHANDLED': 'warning',
    'HANDLING': 'primary',
    'RESOLVED': 'success',
    'IGNORED': 'info'
  }
  return typeMap[status] || 'info'
}

const getStatusText = (status) => {
  const textMap = {
    'UNHANDLED': '未处理',
    'HANDLING': '处理中',
    'RESOLVED': '已解决',
    'IGNORED': '已忽略'
  }
  return textMap[status] || status
}

// 加载趋势数据
const loadTrendData = async () => {
  if (!props.device) return

  trendLoading.value = true
  try {
    // 根据时间范围计算开始和结束时间
    const now = new Date()
    let startTime = new Date(now)

    switch (trendRange.value) {
      case '1h':
        startTime.setHours(startTime.getHours() - 1)
        break
      case '6h':
        startTime.setHours(startTime.getHours() - 6)
        break
      case '24h':
        startTime.setDate(startTime.getDate() - 1)
        break
      case '7d':
        startTime.setDate(startTime.getDate() - 7)
        break
      default:
        startTime.setHours(startTime.getHours() - 1)
    }

    const from = startTime.toISOString().slice(0, 19).replace('T', ' ')
    const to = now.toISOString().slice(0, 19).replace('T', ' ')

    const response = await monitorApi.getDeviceTrend(props.device.id, from, to, 60)
    const data = response.data?.data || []

    // 使用 ECharts 渲染图表
    await nextTick()
    if (!chartInstance) {
      chartInstance = useEcharts(chartRef.value)
    }

    const option = {
      tooltip: {
        trigger: 'axis',
        formatter: (params) => {
          let result = params[0].axisValue + '<br/>'
          params.forEach(param => {
            result += param.marker + param.seriesName + ': ' + param.data + '<br/>'
          })
          return result
        }
      },
      legend: {
        data: ['温度(℃)', '湿度(%)']
      },
      grid: {
        left: '3%',
        right: '4%',
        bottom: '3%',
        containLabel: true
      },
      xAxis: {
        type: 'category',
        boundaryGap: false,
        data: data.map(item => item.time)
      },
      yAxis: [
        {
          type: 'value',
          name: '温度(℃)',
          min: 0,
          max: 50,
          position: 'left',
          axisLine: {
            show: true,
            lineStyle: {
              color: '#5470c6'
            }
          }
        },
        {
          type: 'value',
          name: '湿度(%)',
          min: 0,
          max: 100,
          position: 'right',
          axisLine: {
            show: true,
            lineStyle: {
              color: '#91cc75'
            }
          }
        }
      ],
      series: [
        {
          name: '温度(℃)',
          type: 'line',
          yAxisIndex: 0,
          data: data.map(item => item.temperature ? parseFloat(item.temperature) : '-'),
          itemStyle: {
            color: '#5470c6'
          },
          areaStyle: {
            opacity: 0.1
          }
        },
        {
          name: '湿度(%)',
          type: 'line',
          yAxisIndex: 1,
          data: data.map(item => item.humidity ? parseFloat(item.humidity) : '-'),
          itemStyle: {
            color: '#91cc75'
          },
          areaStyle: {
            opacity: 0.1
          }
        }
      ]
    }

    chartInstance.setOption(option)
  } catch (error) {
    console.error('加载趋势数据失败:', error)
  } finally {
    trendLoading.value = false
  }
}

// 加载告警数据
const loadAlertData = async () => {
  if (!props.device) return

  alertLoading.value = true
  try {
    const params = {
      page: alertPagination.value.currentPage,
      size: alertPagination.value.pageSize,
      deviceId: props.device.id
    }
    // 注意：这里需要实际的告警API来获取设备的告警记录
    // const response = await alertApi.getAlertsByDeviceId(params)
    // alertList.value = response.data.list || []
    // alertPagination.value.total = response.data.total || 0

    // 实际的告警数据加载
    const response = await alertApi.getByDeviceId(props.device.id, {
      page: alertPagination.value.currentPage,
      size: alertPagination.value.pageSize
    })
    alertList.value = response.data?.data?.data || []
    alertPagination.value.total = response.data?.data?.total || 0
  } catch (error) {
    console.error('加载告警数据失败:', error)
  } finally {
    alertLoading.value = false
  }
}

// 告警处理方法
const handleAcknowledge = async (alert) => {
  try {
    // 调用告警确认API
    // await alertApi.acknowledgeAlert(alert.id)
    console.log('确认告警:', alert.id)
    // 刷新告警列表
    loadAlertData()
  } catch (error) {
    console.error('确认告警失败:', error)
  }
}

const handleHandle = async (alert) => {
  try {
    // 调用告警处理API
    // await alertApi.handleAlert(alert.id)
    console.log('处理告警:', alert.id)
    // 刷新告警列表
    loadAlertData()
  } catch (error) {
    console.error('处理告警失败:', error)
  }
}

// 分页方法
const handleAlertSizeChange = (size) => {
  alertPagination.value.pageSize = size
  alertPagination.value.currentPage = 1
  loadAlertData()
}

const handleAlertPageChange = (page) => {
  alertPagination.value.currentPage = page
  loadAlertData()
}

// 组件卸载时销毁图表
// 注意：在实际应用中，需要在组件卸载时调用 chartInstance.dispose()
</script>

<style scoped>
.drawer-content {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.drawer-tabs {
  height: 100%;
}

.detail-section {
  height: 100%;
  overflow-y: auto;
}

.threshold-section {
  margin-top: 20px;
  padding: 15px;
  background: #f8f9fa;
  border-radius: 4px;
}

.threshold-info p {
  margin: 5px 0;
  font-size: 14px;
}

.trend-section {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.trend-controls {
  margin-bottom: 15px;
  text-align: center;
}

.chart-container {
  flex: 1;
  min-height: 400px;
}

.alert-section {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.pagination-container {
  margin-top: 15px;
}

.over-threshold {
  color: #F56C6C;
  font-weight: bold;
}

.near-threshold {
  color: #E6A23C;
}

.normal-temp, .normal-humi {
  color: #67C23A;
}

.no-data {
  color: #c0c4cc;
}
</style>