<template>
  <Layout>
    <div class="trend-analysis-container">
      <!-- 筛选控制栏 -->
      <el-card class="control-card" shadow="hover">
        <el-form :inline="true" :model="filterForm" class="control-form">
          <!-- 时间范围 -->
          <el-form-item label="时间范围">
            <el-date-picker
              v-model="filterForm.dateRange"
              type="daterange"
              range-separator="至"
              start-placeholder="开始日期"
              end-placeholder="结束日期"
              format="YYYY-MM-DD"
              value-format="YYYY-MM-DD"
              @change="onDateRangeChange"
              style="width: 280px"
            ></el-date-picker>
          </el-form-item>

          <!-- 快捷选择 -->
          <el-form-item label="快捷选择">
            <el-button-group>
              <el-button size="small" @click="setQuickRange('last24h')">24小时</el-button>
              <el-button size="small" @click="setQuickRange('last7d')">7天</el-button>
              <el-button size="small" @click="setQuickRange('thisMonth')">本月</el-button>
              <el-button size="small" @click="setQuickRange('lastMonth')">上月</el-button>
            </el-button-group>
          </el-form-item>

          <!-- 聚合粒度 -->
          <el-form-item label="聚合粒度">
            <el-radio-group v-model="filterForm.interval" @change="loadTrendData">
              <el-radio-button value="hourly" :disabled="!isHourlyEnabled">小时</el-radio-button>
              <el-radio-button value="daily">天</el-radio-button>
              <el-radio-button value="weekly">周</el-radio-button>
              <el-radio-button value="monthly">月</el-radio-button>
            </el-radio-group>
          </el-form-item>

          <!-- 操作按钮 -->
          <el-form-item class="action-buttons">
            <el-button type="primary" @click="loadTrendData" :loading="isLoading">
              <el-icon><Search /></el-icon>
              生成分析
            </el-button>
          </el-form-item>
        </el-form>
      </el-card>

      <!-- 骨架屏加载状态 -->
      <template v-if="isLoading">
        <el-skeleton class="kpi-skeleton" :rows="1" animated />
        <el-skeleton class="chart-skeleton" :rows="1" animated />
        <el-skeleton class="sub-chart-skeleton" :rows="1" animated />
      </template>

      <!-- 主内容区域 -->
      <template v-else>
        <!-- KPI统计卡片 -->
        <el-row :gutter="16" class="kpi-row">
          <el-col :xs="24" :sm="12" :md="6" v-for="stat in kpiStats" :key="stat.key">
            <KpiCard
              :title="stat.title"
              :value="stat.value"
              :icon="stat.icon"
              :icon-class="stat.iconClass"
              :trend-text="stat.trendText"
              :trend-icon="stat.trendIcon"
              :trend-class="stat.trendClass"
              :sparkline-data="stat.sparklineData"
              :show-sparkline="true"
              :highlight="true"
            />
          </el-col>
        </el-row>

        <!-- 主图表：环境温湿度趋势 -->
        <el-card class="main-chart-card" shadow="hover">
          <template #header>
            <div class="chart-header">
              <span class="chart-title">
                <el-icon><TrendCharts /></el-icon>
                环境温湿度趋势分析
              </span>
              <div class="chart-actions">
                <el-tag size="small" type="info">{{ currentChartTypeLabel }}</el-tag>
                <el-button size="small" @click="toggleChartType">
                  <el-icon><RefreshLeft /></el-icon>
                  切换图表类型
                </el-button>
              </div>
            </div>
          </template>
          <div class="chart-container">
            <Echarts
              :option="environmentChartOption"
              height="420"
              ref="environmentChartRef" />
          </div>
        </el-card>

        <!-- 副图表区域 -->
        <el-row :gutter="16" class="sub-charts-row">
          <!-- 左侧：告警趋势 -->
          <el-col :span="24" :md="12">
            <el-card class="sub-chart-card" shadow="hover">
              <template #header>
                <div class="chart-header">
                  <span class="chart-title">
                    <el-icon><WarningFilled /></el-icon>
                    告警趋势统计
                  </span>
                </div>
              </template>
              <div class="chart-container">
                <Echarts
                  :option="alertTrendChartOption"
                  height="320"
                  ref="alertTrendChartRef" />
              </div>
            </el-card>
          </el-col>

          <!-- 右侧：工单趋势 -->
          <el-col :span="24" :md="12">
            <el-card class="sub-chart-card" shadow="hover">
              <template #header>
                <div class="chart-header">
                  <span class="chart-title">
                    <el-icon><Document /></el-icon>
                    工单生命周期趋势
                  </span>
                </div>
              </template>
              <div class="chart-container">
                <Echarts
                  :option="workOrderTrendChartOption"
                  height="320"
                  ref="workOrderTrendChartRef" />
              </div>
            </el-card>
          </el-col>
        </el-row>

        <!-- 详细数据表格 -->
        <el-card class="data-table-card" shadow="hover">
          <template #header>
            <div class="chart-header">
              <span class="chart-title">
                <el-icon><Grid /></el-icon>
                详细数据汇总
              </span>
              <div class="chart-actions">
                <el-tabs v-model="activeTab" @tab-change="handleTabChange" class="header-tabs">
                  <el-tab-pane label="温湿度汇总" name="tempHumidity"></el-tab-pane>
                  <el-tab-pane label="告警汇总" name="alerts"></el-tab-pane>
                  <el-tab-pane label="工单汇总" name="workOrders"></el-tab-pane>
                </el-tabs>
              </div>
            </div>
          </template>

          <!-- 表格 -->
          <DataTable
            v-if="currentTableData.length > 0"
            :table-data="paginatedTableData"
            :loading="tableLoading"
            :columns="tableColumns"
            :current-page="currentPage"
            :page-size="pageSize"
            :total="total"
            @current-change="handleCurrentPageChange"
            @size-change="handlePageSizeChange"
            height="380"
            show-pagination
            show-actions
          >
            <template #actions="{ row }">
              <el-button size="small" @click="viewDetails(row)" type="primary" link>
                查看详情
              </el-button>
            </template>
          </DataTable>

          <!-- 空状态 -->
          <el-empty v-else description="暂无趋势数据" :image-size="80" />
        </el-card>
      </template>
    </div>
  </Layout>
</template>

<script setup>
import { ref, onMounted, computed, watch } from 'vue'
import {
  getDashboardStatsApi,
  getEnvironmentTrendApi,
  getAlertTrendApi,
  getWorkOrderTrendApi
} from '@/api/dashboard'
import { debounce } from 'lodash-es'
import { ElMessage } from 'element-plus'
import {
  Search, TrendCharts, WarningFilled, Document, Grid, RefreshLeft
} from '@element-plus/icons-vue'
import * as echarts from 'echarts'
import KpiCard from './components/KpiCard.vue'
import Echarts from './components/Echarts.vue'
import DataTable from './components/DataTable.vue'
import Layout from '@/components/Layout.vue'

// 高级筛选条件（由侧边栏传入）
const advancedFilters = ref({})

// 统计信息
const isLoading = ref(false)
const isExporting = ref(false)
const filterForm = ref({
  dateRange: [
    new Date(Date.now() - 7 * 24 * 60 * 60 * 1000).toISOString().split('T')[0],
    new Date().toISOString().split('T')[0]
  ],
  interval: 'daily',
  dimension: []
})

// 图表类型切换
const currentChartType = ref('line') // line / bar
const currentChartTypeLabel = computed(() => {
  return currentChartType.value === 'line' ? '折线图' : '柱状图'
})

const kpiStats = ref([])
const dimensionOptions = ref([])
const activeTab = ref('tempHumidity')
const currentTableData = ref([])
const tableLoading = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const tableColumns = ref([])
const environmentChartOption = ref({})
const alertTrendChartOption = ref({})
const workOrderTrendChartOption = ref({})

// 计算：是否启用小时粒度
const isHourlyEnabled = computed(() => {
  if (!filterForm.value.dateRange || filterForm.value.dateRange.length < 2) return false
  const [startDateStr, endDateStr] = filterForm.value.dateRange
  const startDate = new Date(startDateStr)
  const endDate = new Date(endDateStr)
  const diffDays = (endDate - startDate) / (1000 * 60 * 60 * 24)
  return diffDays <= 1 // 时间跨度≤1天才启用小时粒度
})

// 分页后的数据
const paginatedTableData = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  const end = start + pageSize.value
  return currentTableData.value.slice(start, end)
})

// 加载KPI统计信息
const loadStats = async () => {
  try {
    const response = await getDashboardStatsApi()
    const data = response.data.data || response.data

    const totalDevices = data.totalDevices || 0
    const onlineDevices = data.onlineDevices || 0
    const onlineRate = totalDevices > 0 ? ((onlineDevices / totalDevices) * 100).toFixed(1) : '0.0'

    kpiStats.value = [
      {
        key: 'devices',
        title: '设备在线',
        value: `${onlineDevices} / ${totalDevices}`,
        trendText: `在线率 ${onlineRate}%`,
        trendIcon: 'Monitor',
        trendClass: Number(onlineRate) > 90 ? 'trend-up' : 'trend-down',
        icon: 'Monitor',
        iconClass: 'device-icon',
        sparklineData: []
      },
      {
        key: 'alerts',
        title: '今日告警',
        value: `${data.todayAlerts || 0} / ${data.unhandledAlerts || 0}`,
        trendText: '未处理告警',
        trendIcon: 'Warning',
        trendClass: (data.unhandledAlerts || 0) > 0 ? 'trend-down danger' : 'trend-up',
        icon: 'Warning',
        iconClass: 'alert-icon',
        sparklineData: []
      },
      {
        key: 'workOrders',
        title: '本周闭环',
        value: data.weekClosedWorkOrders || 0,
        trendText: '工单处理效率',
        trendIcon: 'Check',
        trendClass: 'trend-up',
        icon: 'DocumentChecked',
        iconClass: 'work-icon',
        sparklineData: []
      },
      {
        key: 'avgTemp',
        title: '平均温度',
        value: `${data.avgTemperature || '--'}°C`,
        trendText: '整体环境',
        trendIcon: 'Temperature',
        trendClass: 'trend-neutral',
        icon: 'Temperature',
        iconClass: 'temp-icon',
        sparklineData: []
      }
    ]
  } catch (error) {
    console.error('加载统计信息失败:', error)
    ElMessage.error('加载统计信息失败')
  }
}

// 加载所有趋势数据
const loadTrendData = async () => {
  try {
    isLoading.value = true
    currentPage.value = 1

    const [startDate, endDate] = filterForm.value.dateRange

    const params = {
      startDate,
      endDate,
      interval: filterForm.value.interval,
      ...advancedFilters.value
    }

    const [envResult, alertResult, workOrderResult] = await Promise.allSettled([
      getEnvironmentTrendApi(params),
      getAlertTrendApi(params),
      getWorkOrderTrendApi(params)
    ])

    // 正确处理后端返回的数据结构：response -> data -> data
    const getResponseData = (result) => {
      if (result.status !== 'fulfilled') return null
      const response = result.value
      // 标准后端返回结构: { code: xxx, data: xxx }
      return response.data
    }

    const envResponse = getResponseData(envResult)
    const envData = Array.isArray(envResponse) ? envResponse :
                 Array.isArray(envResponse?.data) ? envResponse.data : []

    const alertResponse = getResponseData(alertResult)
    const alertData = alertResponse?.data && Array.isArray(alertResponse.data) ? alertResponse :
                 Array.isArray(alertResponse) ? { data: alertResponse } :
                 alertResponse || { data: [] }

    const workOrderResponse = getResponseData(workOrderResult)
    const workOrderData = workOrderResponse?.data && Array.isArray(workOrderResponse.data) ? workOrderResponse :
                   Array.isArray(workOrderResponse) ? { data: workOrderResponse } :
                   workOrderResponse || { data: [] }

    renderEnvironmentChart(envData)
    renderAlertTrendChart(alertData)
    renderWorkOrderTrendChart(workOrderData)
    prepareTableData(envData, alertData.data || [], workOrderData.data || [])
  } catch (error) {
    console.error('加载趋势数据失败:', error)
    ElMessage.error('加载图表数据失败')
  } finally {
    isLoading.value = false
  }
}

// 切换图表类型
const toggleChartType = () => {
  currentChartType.value = currentChartType.value === 'line' ? 'bar' : 'line'
  renderEnvironmentChart(currentEnvData.value)
}

// 保存当前环境数据供切换使用
const currentEnvData = ref([])

// 日期范围改变
const onDateRangeChange = () => {
  loadTrendData()
}

// 快捷选择时间范围
const setQuickRange = (range) => {
  const now = new Date()
  let startDate

  switch (range) {
    case 'last24h':
      startDate = new Date(now.getTime() - 24 * 60 * 60 * 1000)
      break
    case 'last7d':
      startDate = new Date(now.getTime() - 7 * 24 * 60 * 60 * 1000)
      break
    case 'thisMonth':
      startDate = new Date(now.getFullYear(), now.getMonth(), 1)
      break
    case 'lastMonth':
      startDate = new Date(now.getFullYear(), now.getMonth() - 1, 1)
      const endDate = new Date(now.getFullYear(), now.getMonth(), 0)
      filterForm.value.dateRange = [
        startDate.toISOString().split('T')[0],
        endDate.toISOString().split('T')[0]
      ]
      loadTrendData()
      return
  }

  filterForm.value.dateRange = [
    startDate.toISOString().split('T')[0],
    now.toISOString().split('T')[0]
  ]

  loadTrendData()
}

// 渲染环境温湿度图表
const renderEnvironmentChart = (data) => {
  currentEnvData.value = data || []

  if (!data || data.length === 0) {
    environmentChartOption.value = {
      title: { text: '暂无数据', left: 'center', top: 'center', textStyle: { color: '#999' } },
      tooltip: { trigger: 'axis' }
    }
    return
  }

  const categories = data.map(item => item.time || item.date || '')
  const tempData = data.map(item => item.temperature ?? item.avgTemp ?? null)
  const humidityData = data.map(item => item.humidity ?? item.avgHumidity ?? null)

  const series = [
    {
      name: '温度 (°C)',
      type: currentChartType.value,
      smooth: currentChartType.value === 'line',
      data: tempData,
      itemStyle: { color: '#FF6B6B' },
      ...(currentChartType.value === 'line' && {
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(255, 107, 107, 0.3)' },
            { offset: 1, color: 'rgba(255, 107, 107, 0.05)' }
          ])
        }
      })
    },
    {
      name: '湿度 (%)',
      type: currentChartType.value,
      yAxisIndex: 1,
      smooth: currentChartType.value === 'line',
      data: humidityData,
      itemStyle: { color: '#4ECDC4' },
      ...(currentChartType.value === 'line' && {
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(78, 205, 196, 0.3)' },
            { offset: 1, color: 'rgba(78, 205, 196, 0.05)' }
          ])
        }
      })
    }
  ]

  environmentChartOption.value = {
    tooltip: {
      trigger: 'axis',
      axisPointer: { type: 'cross' }
    },
    legend: {
      data: ['温度 (°C)', '湿度 (%)'],
      top: 10,
      textStyle: { fontSize: 13 }
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '12%',
      top: '15%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: categories,
      axisLabel: {
        rotate: categories.length > 15 ? 45 : 0,
        interval: 'auto',
        fontSize: 11
      }
    },
    yAxis: [
      {
        type: 'value',
        name: '温度 (°C)',
        position: 'left',
        nameTextStyle: { fontSize: 12 },
        axisLabel: { formatter: '{value} °C', fontSize: 11 }
      },
      {
        type: 'value',
        name: '湿度 (%)',
        position: 'right',
        nameTextStyle: { fontSize: 12 },
        axisLabel: { formatter: '{value} %', fontSize: 11 }
      }
    ],
    series
  }
}

// 渲染告警趋势图表
const renderAlertTrendChart = (data) => {
  let chartData = []
  if (Array.isArray(data)) {
    chartData = data
  } else if (data && Array.isArray(data.data)) {
    chartData = data.data
  } else if (data && Array.isArray(data.result)) {
    chartData = data.result
  }

  if (!chartData || chartData.length === 0) {
    alertTrendChartOption.value = {
      title: { text: '暂无数据', left: 'center', top: 'center', textStyle: { color: '#999' } },
      tooltip: { trigger: 'axis' }
    }
    return
  }

  const categories = chartData.map(item => item.time || item.date || '')
  const alertCounts = chartData.map(item => item.count ?? item.alertCount ?? 0)

  alertTrendChartOption.value = {
    tooltip: { trigger: 'axis' },
    legend: {
      data: ['告警数量'],
      top: 10,
      textStyle: { fontSize: 13 }
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '10%',
      top: '18%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: categories,
      axisLabel: {
        rotate: categories.length > 10 ? 45 : 0,
        fontSize: 11
      }
    },
    yAxis: {
      type: 'value',
      name: '告警数',
      nameTextStyle: { fontSize: 12 },
      axisLabel: { fontSize: 11 }
    },
    series: [
      {
        name: '告警数量',
        type: 'bar',
        data: alertCounts,
        itemStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: '#FF6B6B' },
            { offset: 1, color: '#FF8E8E' }
          ])
        },
        showBackground: true,
        backgroundStyle: { color: 'rgba(255, 107, 107, 0.1)' },
        barWidth: '60%'
      }
    ]
  }
}

// 渲染工单趋势图表
const renderWorkOrderTrendChart = (data) => {
  let chartData = []
  if (Array.isArray(data)) {
    chartData = data
  } else if (data && Array.isArray(data.data)) {
    chartData = data.data
  } else if (data && Array.isArray(data.result)) {
    chartData = data.result
  }

  if (!chartData || chartData.length === 0) {
    workOrderTrendChartOption.value = {
      title: { text: '暂无数据', left: 'center', top: 'center', textStyle: { color: '#999' } },
      tooltip: { trigger: 'axis' }
    }
    return
  }

  const categories = chartData.map(item => item.time || item.date || '')
  const createdData = chartData.map(item => item.created ?? item.createdCount ?? 0)
  const completedData = chartData.map(item => item.completed ?? item.completedCount ?? 0)
  const pendingData = chartData.map(item => item.pending ?? item.pendingCount ?? 0)

  workOrderTrendChartOption.value = {
    tooltip: { trigger: 'axis' },
    legend: {
      data: ['新建', '已完成', '待处理'],
      top: 10,
      textStyle: { fontSize: 13 }
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '10%',
      top: '18%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: categories,
      axisLabel: {
        rotate: categories.length > 10 ? 45 : 0,
        fontSize: 11
      }
    },
    yAxis: {
      type: 'value',
      name: '工单数',
      nameTextStyle: { fontSize: 12 },
      axisLabel: { fontSize: 11 }
    },
    series: [
      {
        name: '新建',
        type: 'line',
        smooth: true,
        data: createdData,
        itemStyle: { color: '#4ECDC4' }
      },
      {
        name: '已完成',
        type: 'line',
        smooth: true,
        data: completedData,
        itemStyle: { color: '#95E1A3' }
      },
      {
        name: '待处理',
        type: 'line',
        smooth: true,
        data: pendingData,
        itemStyle: { color: '#F38181' }
      }
    ]
  }
}

// 准备表格数据
const prepareTableData = (envData, alertData, workOrderData) => {
  const envChartData = envData || []
  const alertChartData = alertData || []
  const workOrderChartData = workOrderData || []

  if (activeTab.value === 'tempHumidity') {
    currentTableData.value = envChartData.map(item => ({
      time: item.time || item.date || '',
      temperature: item.temperature ?? item.avgTemp ?? '-',
      humidity: item.humidity ?? item.avgHumidity ?? '-',
      deviceCount: item.deviceCount ?? '-'
    }))
    tableColumns.value = [
      { prop: 'time', label: '时间', width: 180 },
      { prop: 'temperature', label: '温度 (°C)', width: 120 },
      { prop: 'humidity', label: '湿度 (%)', width: 120 },
      { prop: 'deviceCount', label: '设备数', width: 100 }
    ]
  } else if (activeTab.value === 'alerts') {
    currentTableData.value = alertChartData.map(item => ({
      time: item.time || item.date || '',
      count: item.count ?? item.alertCount ?? 0,
      level: item.level ?? '未知',
      handled: item.handled ?? '未处理'
    }))
    tableColumns.value = [
      { prop: 'time', label: '时间', width: 180 },
      { prop: 'count', label: '告警数', width: 100 },
      { prop: 'level', label: '等级', width: 100 },
      { prop: 'handled', label: '处理状态', width: 120 }
    ]
  } else if (activeTab.value === 'workOrders') {
    currentTableData.value = workOrderChartData.map(item => ({
      time: item.time || item.date || '',
      created: item.created ?? item.createdCount ?? 0,
      completed: item.completed ?? item.completedCount ?? 0,
      pending: item.pending ?? item.pendingCount ?? 0
    }))
    tableColumns.value = [
      { prop: 'time', label: '时间', width: 180 },
      { prop: 'created', label: '新建', width: 100 },
      { prop: 'completed', label: '已完成', width: 100 },
      { prop: 'pending', label: '待处理', width: 100 }
    ]
  }

  total.value = currentTableData.value.length
  tableLoading.value = false
}

// 标签页切换
const handleTabChange = (tabName) => {
  activeTab.value = tabName
  currentPage.value = 1
}

// 分页改变
const handleCurrentPageChange = (page) => {
  currentPage.value = page
}

// 每页条数改变
const handlePageSizeChange = (size) => {
  pageSize.value = size
  currentPage.value = 1
}

// 查看详情
const viewDetails = (row) => {
  console.log('查看详情:', row)
  ElMessage.info(`查看 ${row.time} 详情`)
}

// 页面加载初始化
onMounted(async () => {
  await loadStats()
  await loadTrendData()

  // 监听窗口大小变化，确保图表响应式
  window.addEventListener('resize', debounce(() => {
    // 图表会自动响应
  }, 200))
})
</script>

<style scoped>
.trend-analysis-container {
  padding: 20px;
  background-color: #f5f7fa;
  min-height: calc(100vh - 84px);
}

/* 控制卡片样式 */
.control-card {
  margin-bottom: 20px;
  border-radius: 8px;
  transition: all 0.3s ease;
}

.control-form {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 12px;
}

.control-form .el-form-item {
  margin-bottom: 0;
}

.action-buttons {
  margin-left: auto;
}

/* KPI行样式 */
.kpi-row {
  margin-bottom: 20px;
}

/* 主图表样式 */
.main-chart-card {
  margin-bottom: 20px;
  border-radius: 8px;
  overflow: hidden;
  transition: all 0.3s ease;
}

.chart-container {
  width: 100%;
}

/* 副图表区域 */
.sub-charts-row {
  margin-bottom: 20px;
}

.sub-chart-card {
  margin-bottom: 0;
  border-radius: 8px;
  overflow: hidden;
  transition: all 0.3s ease;
}

/* 数据表格卡片 */
.data-table-card {
  border-radius: 8px;
  overflow: hidden;
  transition: all 0.3s ease;
}

/* 图表头部 */
.chart-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.chart-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  display: flex;
  align-items: center;
  gap: 8px;
}

.chart-title .el-icon {
  color: #409eff;
}

.chart-actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

.header-tabs {
  margin: 0;
}

.header-tabs :deep(.el-tabs__header) {
  margin: 0;
}

/* 骨架屏 */
.kpi-skeleton {
  margin-bottom: 20px;
  border-radius: 8px;
  overflow: hidden;
}

.chart-skeleton {
  margin-bottom: 20px;
  height: 420px;
  border-radius: 8px;
  overflow: hidden;
}

.sub-chart-skeleton {
  margin-bottom: 20px;
  height: 320px;
  border-radius: 8px;
  overflow: hidden;
}

/* 响应式设计 */
@media (max-width: 1440px) {
  .control-form {
    gap: 8px;
  }
}

@media (max-width: 1200px) {
  .control-form {
    flex-direction: column;
    align-items: flex-start;
  }

  .action-buttons {
    margin-left: 0;
    width: 100%;
  }

  .el-form-item {
    width: 100%;
  }
}

@media (max-width: 768px) {
  .trend-analysis-container {
    padding: 12px;
  }

  .el-col {
    margin-bottom: 16px;
  }

  .main-chart-card {
    height: auto;
  }

  .chart-container {
    height: 300px !important;
  }

  .sub-chart-card {
    height: 280px !important;
  }
}
</style>
