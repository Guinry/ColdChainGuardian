<template>
  <Layout>
    <div class="device-data-content">
      <div class="page-header">
        <div class="header-left">
          <el-page-header @back="goBack" :content="`设备数据 - ${deviceName}`" />
        </div>
        <div class="header-right">
          <el-button @click="refreshData" :loading="loading">
            <el-icon><Refresh /></el-icon>
            刷新
          </el-button>
        </div>
      </div>

      <div class="data-content">
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
                <label>设备类型:</label>
                <el-tag :type="getDeviceTypeTag(deviceType)">
                  {{ getDeviceTypeLabel(deviceType) }}
                </el-tag>
              </div>
              <div class="info-item">
                <label>所属库区:</label>
                <span>{{ areaName }}</span>
              </div>
            </div>
            <div class="info-row">
              <div class="info-item">
                <label>在线状态:</label>
                <el-tag :type="onlineStatus ? 'success' : 'info'">
                  {{ onlineStatus ? '在线' : '离线' }}
                </el-tag>
              </div>
              <div class="info-item">
                <label>最后上报时间:</label>
                <span>{{ formatDate(lastSeenTime) }}</span>
              </div>
            </div>
          </div>
        </el-card>

        <!-- 图表区域 -->
        <el-card class="chart-card">
          <template #header>
            <div class="card-header">
              <span>温湿度曲线图</span>
              <div class="chart-controls">
                <el-date-picker
                  v-model="timeRange"
                  type="datetimerange"
                  range-separator="至"
                  start-placeholder="开始时间"
                  end-placeholder="结束时间"
                  :default-time="[new Date(2026, 1, 1, 0, 0, 0), new Date(2026, 1, 1, 23, 59, 59)]"
                />
                <el-button type="primary" @click="loadChartData">查询</el-button>
              </div>
            </div>
          </template>
          <div class="chart-container">
            <div ref="chartRef" class="chart"></div>
          </div>
        </el-card>

        <!-- 数据表格 -->
        <el-card class="data-table-card">
          <template #header>
            <div class="card-header">
              <span>历史数据</span>
              <div class="table-controls">
                <el-button @click="exportData">导出数据</el-button>
              </div>
            </div>
          </template>
          <el-table
            v-loading="tableLoading"
            :data="dataTable"
            style="width: 100%"
            height="400"
          >
            <el-table-column prop="dataTime" label="时间" width="180">
              <template #default="{ row }">
                {{ formatDate(row.dataTime) }}
              </template>
            </el-table-column>
            <el-table-column prop="temperature" label="温度(℃)" width="120" />
            <el-table-column prop="humidity" label="湿度(%)" width="120" />
            <el-table-column prop="batteryLevel" label="电量(%)" width="120" />
            <el-table-column prop="signalStrength" label="信号强度" width="120" />
            <el-table-column prop="rawData" label="原始数据" width="200" show-overflow-tooltip />
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
    </div>
  </Layout>
</template>

<script setup>
import { ref, reactive, onMounted, onBeforeUnmount, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Refresh } from '@element-plus/icons-vue'
import Layout from '@/components/Layout.vue'
import { deviceApi } from '@/api/device.js'
import * as echarts from 'echarts'

// 路由
const route = useRoute()
const router = useRouter()

// 响应式数据
const loading = ref(false)
const tableLoading = ref(false)
const chartRef = ref(null)
let chartInstance = null
let resizeHandler = null

// 设备信息
const deviceName = ref('')
const deviceCode = ref('')
const deviceType = ref('')
const areaName = ref('')
const onlineStatus = ref(false)
const lastSeenTime = ref(null)

// 图表相关
const timeRange = ref([])
const chartData = ref({
  times: [],
  temperatures: [],
  humidities: []
})

// 表格数据
const dataTable = ref([])
const pagination = reactive({
  currentPage: 1,
  pageSize: 10,
  total: 0
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
  loadChartData(deviceId)
  loadDataTable(deviceId)
})

// 加载设备信息
const loadDeviceInfo = async (deviceId) => {
  try {
    const response = await deviceApi.getDetail(deviceId)
    const device = response.data?.data
    if (!device) {
      ElMessage.error(response.data?.message || '设备不存在')
      router.push('/devices')
      return
    }

    deviceName.value = device.deviceName
    deviceCode.value = device.deviceCode
    deviceType.value = device.deviceType
    areaName.value = device.areaName
    onlineStatus.value = device.onlineStatus
    lastSeenTime.value = device.lastSeenTime
  } catch (error) {
    ElMessage.error('获取设备信息失败')
    console.error(error)
  }
}

// 加载图表数据
const loadChartData = async (deviceId) => {
  loading.value = true
  try {
    const params = {
      startTime: timeRange.value[0] ? new Date(timeRange.value[0]).toISOString() : undefined,
      endTime: timeRange.value[1] ? new Date(timeRange.value[1]).toISOString() : undefined
    }

    const response = await deviceApi.getHistoricalData(deviceId || route.params.deviceId, {
      ...params,
      page: 1,
      size: 100
    })
    const data = response.data?.data?.data || []

    // 处理图表数据
    chartData.value.times = data.map(item => new Date(item.dataTime).toLocaleString())
    chartData.value.temperatures = data.map(item => item.temperature)
    chartData.value.humidities = data.map(item => item.humidity)

    // 渲染图表
    await nextTick()
    renderChart()
  } catch (error) {
    ElMessage.error('获取图表数据失败')
    console.error(error)
  } finally {
    loading.value = false
  }
}

// 加载表格数据
const loadDataTable = async (deviceId) => {
  tableLoading.value = true
  try {
    const params = {
      page: pagination.currentPage,
      size: pagination.pageSize,
      startTime: timeRange.value[0] ? new Date(timeRange.value[0]).toISOString() : undefined,
      endTime: timeRange.value[1] ? new Date(timeRange.value[1]).toISOString() : undefined
    }

    const response = await deviceApi.getHistoricalData(deviceId, params)
    dataTable.value = response.data?.data?.data || []
    pagination.total = response.data?.data?.total || 0
  } catch (error) {
    ElMessage.error('获取表格数据失败')
    console.error(error)
  } finally {
    tableLoading.value = false
  }
}

// 渲染图表
const renderChart = () => {
  if (!chartRef.value) return

  // 销毁之前的实例
  if (chartInstance) {
    chartInstance.dispose()
  }

  // 创建新实例
  chartInstance = echarts.init(chartRef.value)

  const option = {
    tooltip: {
      trigger: 'axis'
    },
    legend: {
      data: ['温度', '湿度']
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
      data: chartData.value.times
    },
    yAxis: [
      {
        type: 'value',
        name: '温度(℃)',
        min: -30,
        max: 50,
        axisLabel: {
          formatter: '{value}°C'
        }
      },
      {
        type: 'value',
        name: '湿度(%)',
        min: 0,
        max: 100,
        axisLabel: {
          formatter: '{value}%'
        }
      }
    ],
    series: [
      {
        name: '温度',
        type: 'line',
        yAxisIndex: 0,
        data: chartData.value.temperatures,
        smooth: true,
        itemStyle: {
          color: '#5470c6'
        }
      },
      {
        name: '湿度',
        type: 'line',
        yAxisIndex: 1,
        data: chartData.value.humidities,
        smooth: true,
        itemStyle: {
          color: '#91cc75'
        }
      }
    ]
  }

  chartInstance.setOption(option)

  if (!resizeHandler) {
    resizeHandler = () => {
      if (chartInstance) {
        chartInstance.resize()
      }
    }
    window.addEventListener('resize', resizeHandler)
  }
}

onBeforeUnmount(() => {
  if (resizeHandler) {
    window.removeEventListener('resize', resizeHandler)
    resizeHandler = null
  }
  if (chartInstance) {
    chartInstance.dispose()
    chartInstance = null
  }
})

const downloadCsv = (filename, headers, rows) => {
  const escapeCell = (value) => `"${String(value ?? '').replaceAll('"', '""')}"`
  const csv = [
    headers.map(([, label]) => escapeCell(label)).join(','),
    ...rows.map(row => headers.map(([key]) => escapeCell(row[key])).join(','))
  ].join('\n')

  const blob = new Blob(['\uFEFF' + csv], { type: 'text/csv;charset=utf-8;' })
  const url = URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = filename
  link.click()
  URL.revokeObjectURL(url)
}

// 刷新数据
const refreshData = () => {
  const deviceId = route.params.deviceId
  loadDeviceInfo(deviceId)
  loadChartData(deviceId)
  loadDataTable(deviceId)
}

// 分页相关
const handleSizeChange = (size) => {
  pagination.pageSize = size
  loadDataTable(route.params.deviceId)
}

const handleCurrentChange = (page) => {
  pagination.currentPage = page
  loadDataTable(route.params.deviceId)
}

// 导出数据
const exportData = () => {
  if (!dataTable.value.length) {
    ElMessage.info('暂无可导出的历史数据')
    return
  }

  const headers = [
    ['dataTimeText', '时间'],
    ['temperature', '温度(℃)'],
    ['humidity', '湿度(%)'],
    ['batteryLevel', '电量(%)'],
    ['signalStrength', '信号强度'],
    ['rawData', '原始数据']
  ]
  const rows = dataTable.value.map(row => ({
    ...row,
    dataTimeText: formatDate(row.dataTime)
  }))

  downloadCsv(
    `${deviceCode.value || route.params.deviceId}_历史数据_${new Date().toISOString().slice(0, 10)}.csv`,
    headers,
    rows
  )
  ElMessage.success('历史数据已导出')
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
  return types[type] || 'info'
}
</script>

<style scoped>
.device-data-content {
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

.data-content {
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

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.chart-controls {
  display: flex;
  gap: 10px;
  align-items: center;
}

.chart-card {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.chart-container {
  flex: 1;
  display: flex;
}

.chart {
  flex: 1;
  width: 100%;
  height: 400px;
}

.data-table-card {
  height: 600px;
  display: flex;
  flex-direction: column;
}

.table-controls {
  display: flex;
  gap: 10px;
}

.pagination {
  margin-top: 15px;
  text-align: right;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .info-item {
    flex: 1 0 100%;
  }

  .chart-controls {
    flex-direction: column;
  }

  .card-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;
  }
}
</style>
