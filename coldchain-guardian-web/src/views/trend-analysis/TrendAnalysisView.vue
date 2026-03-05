<template>
  <Layout>
    <div class="trend-analysis-container">
      <el-row :gutter="20">
        <!-- 主内容区域 -->
        <el-col :span="20">
          <!-- 全局时空控制台 -->
          <el-card class="control-bar" shadow="never">
            <el-form :inline="true" :model="filterForm" class="control-form">
              <!-- 时间范围选择器 -->
              <el-form-item label="时间范围">
                <el-date-picker
                  v-model="filterForm.dateRange"
                  type="daterange"
                  range-separator="至"
                  start-placeholder="开始日期"
                  end-placeholder="结束日期"
                  format="YYYY-MM-DD"
                  value-format="YYYY-MM-DD"
                  @change="onDateRangeChange">
                </el-date-picker>
                <el-button-group class="shortcut-buttons">
                  <el-button size="small" @click="setQuickRange('last24h')">最近24小时</el-button>
                  <el-button size="small" @click="setQuickRange('last7d')">最近7天</el-button>
                  <el-button size="small" @click="setQuickRange('thisMonth')">本月</el-button>
                  <el-button size="small" @click="setQuickRange('lastMonth')">上月</el-button>
                </el-button-group>
              </el-form-item>

              <!-- 数据聚合粒度 -->
              <el-form-item label="聚合粒度">
                <el-radio-group v-model="filterForm.interval" @change="loadTrendData">
                  <el-radio-button label="hourly" :disabled="!isHourlyEnabled">按小时</el-radio-button>
                  <el-radio-button label="daily">按天</el-radio-button>
                  <el-radio-button label="weekly">按周</el-radio-button>
                  <el-radio-button label="monthly">按月</el-radio-button>
                </el-radio-group>
              </el-form-item>

              <!-- 分析维度选择 -->
              <el-form-item label="分析维度">
                <el-cascader
                  v-model="filterForm.dimension"
                  :options="dimensionOptions"
                  :props="{ checkStrictly: true }"
                  placeholder="选择分析维度"
                  @change="loadTrendData"
                  clearable>
                </el-cascader>
              </el-form-item>

              <!-- 操作区 -->
              <el-form-item>
                <el-button type="primary" @click="loadTrendData" :loading="isLoading">生成分析</el-button>
                <el-dropdown split-button @command="exportReport">
                  <el-button :loading="isExporting">导出报告</el-button>
                  <template #dropdown>
                    <el-dropdown-menu>
                      <el-dropdown-item command="pdf">PDF报告</el-dropdown-item>
                      <el-dropdown-item command="excel">Excel数据</el-dropdown-item>
                    </el-dropdown-menu>
                  </template>
                </el-dropdown>
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
            <!-- 高管微缩看板 -->
            <el-row :gutter="20" class="kpi-cards">
              <el-col :span="6" v-for="stat in kpiStats" :key="stat.key">
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

            <!-- 核心大图：环境温湿度趋势图 -->
            <el-card class="main-chart-card" shadow="never">
              <template #header>
                <div class="chart-header">
                  <span class="chart-title">环境温湿度趋势分析</span>
                  <div class="chart-actions">
                    <el-button size="small" @click="toggleChartType">切换图表</el-button>
                  </div>
                </div>
              </template>
              <Echarts
                :option="environmentChartOption"
                height="500px"
                ref="environmentChartRef" />
            </el-card>

            <!-- 业务联动副图 -->
            <el-row :gutter="20" class="sub-charts">
              <!-- 左侧：告警趋势图 -->
              <el-col :span="12">
                <el-card class="sub-chart-card" shadow="never">
                  <template #header>
                    <div class="chart-header">
                      <span class="chart-title">告警趋势与严重度分布</span>
                    </div>
                  </template>
                  <Echarts
                    :option="alertTrendChartOption"
                    height="350px"
                    ref="alertTrendChartRef" />
                </el-card>
              </el-col>

              <!-- 右侧：工单趋势图 -->
              <el-col :span="12">
                <el-card class="sub-chart-card" shadow="never">
                  <template #header>
                    <div class="chart-header">
                      <span class="chart-title">工单生命周期趋势</span>
                    </div>
                  </template>
                  <Echarts
                    :option="workOrderTrendChartOption"
                    height="350px"
                    ref="workOrderTrendChartRef" />
                </el-card>
              </el-col>
            </el-row>

            <!-- 聚合数据表格 -->
            <el-card class="data-table-card" shadow="never">
              <template #header>
                <div class="chart-header">
                  <span class="chart-title">详细数据汇总</span>
                  <div class="chart-actions">
                    <el-tabs v-model="activeTab" @tab-change="handleTabChange">
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
                :table-data="currentTableData"
                :loading="tableLoading"
                :columns="tableColumns"
                :current-page="currentPage"
                :page-size="pageSize"
                :total="total"
                @current-change="handleCurrentPageChange"
                @size-change="handlePageSizeChange"
                height="400px"
                show-pagination
                show-actions>
                <template #actions="{ row }">
                  <el-button size="small" @click="viewDetails(row)" type="primary">查看详情</el-button>
                </template>
              </DataTable>

              <!-- 空状态处理 -->
              <el-empty v-else description="暂无趋势数据" />
            </el-card>
          </template>
        </el-col>

        <!-- 分析侧边栏 -->
        <el-col :span="4">
          <AnalysisSidebar
            @filter-applied="handleFilterApplied"
            @comparison-applied="handleComparisonApplied"
            @export-requested="handleExportRequested"
            @ai-analysis-requested="handleAiAnalysisRequested" />
        </el-col>
      </el-row>
    </div>
  </Layout>
</template>

<script setup>
import { ref, onMounted, nextTick, computed, watch, onUnmounted } from 'vue'
import {
  getDashboardStatsApi,
  getEnvironmentTrendApi,
  getAlertTrendApi,
  getWorkOrderTrendApi,
  getDeviceStatusTrendApi
} from '@/api/dashboard'
import { debounce } from 'lodash-es'
import { ElMessage } from 'element-plus'
import Layout from '@/components/Layout.vue'
import KpiCard from '@/views/trend-analysis/components/KpiCard.vue'
import Echarts from '@/views/trend-analysis/components/Echarts.vue'
import DataTable from '@/views/trend-analysis/components/DataTable.vue'
import AnalysisSidebar from '@/views/trend-analysis/components/AnalysisSidebar.vue'

// 响应式数据
const isLoading = ref(false)
const isExporting = ref(false)
const filterForm = ref({
  dateRange: [new Date(Date.now() - 7 * 24 * 60 * 60 * 1000), new Date()], // 默认最近7天
  interval: 'daily',
  dimension: []
})

const kpiStats = ref([])
const dimensionOptions = ref([
  {
    value: 'overall',
    label: '总体概况',
    children: [
      { value: 'byRegion', label: '按区域分析' },
      { value: 'byDevice', label: '按设备分析' },
      { value: 'byTime', label: '按时段分析' }
    ]
  },
  {
    value: 'environment',
    label: '环境分析',
    children: [
      { value: 'temperature', label: '温度分析' },
      { value: 'humidity', label: '湿度分析' },
      { value: 'threshold', label: '阈值分析' }
    ]
  },
  {
    value: 'business',
    label: '业务分析',
    children: [
      { value: 'alerts', label: '告警分析' },
      { value: 'workOrders', label: '工单分析' },
      { value: 'devices', label: '设备分析' }
    ]
  }
])
const activeTab = ref('tempHumidity')
const currentTableData = ref([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const tableLoading = ref(false)

// 图表选项
const environmentChartOption = ref({})
const alertTrendChartOption = ref({})
const workOrderTrendChartOption = ref({})

// 表格列定义
const tableColumns = computed(() => {
  if (activeTab.value === 'tempHumidity') {
    return [
      { prop: 'date', label: '时间', width: 150 },
      {
        prop: 'temperature',
        label: '平均温度(°C)',
        width: 150,
        formatter: (row) => row.temperature ? row.temperature.toFixed(2) : '-'
      },
      {
        prop: 'humidity',
        label: '平均湿度(%)',
        width: 150,
        formatter: (row) => row.humidity ? row.humidity.toFixed(2) : '-'
      },
      {
        prop: 'maxTemperature',
        label: '最高温度(°C)',
        width: 150,
        formatter: (row) => row.maxTemperature ? row.maxTemperature.toFixed(2) : '-'
      },
      {
        prop: 'minTemperature',
        label: '最低温度(°C)',
        width: 150,
        formatter: (row) => row.minTemperature ? row.minTemperature.toFixed(2) : '-'
      }
    ]
  } else if (activeTab.value === 'alerts') {
    return [
      { prop: 'date', label: '时间', width: 150 },
      { prop: 'alertCount', label: '告警总数', width: 120 },
      { prop: 'criticalAlerts', label: '严重告警', width: 120 },
      { prop: 'highAlerts', label: '高危告警', width: 120 },
      { prop: 'mediumAlerts', label: '中等告警', width: 120 },
      { prop: 'lowAlerts', label: '低危告警', width: 120 }
    ]
  } else if (activeTab.value === 'workOrders') {
    return [
      { prop: 'date', label: '时间', width: 150 },
      { prop: 'newWorkOrders', label: '新增工单', width: 120 },
      { prop: 'completedWorkOrders', label: '完成工单', width: 120 },
      { prop: 'pendingWorkOrders', label: '待处理工单', width: 120 },
      { prop: 'processingWorkOrders', label: '处理中工单', width: 150 }
    ]
  }
  return []
})

// 是否允许小时粒度（仅适用于较短时间范围）
const isHourlyEnabled = computed(() => {
  if (!filterForm.value.dateRange || filterForm.value.dateRange.length !== 2) {
    return false
  }
  const [start, end] = filterForm.value.dateRange
  const startDate = new Date(start)
  const endDate = new Date(end)
  const diffDays = (endDate - startDate) / (24 * 60 * 60 * 1000)
  return diffDays <= 2 // 只有在时间范围不超过2天时才允许按小时
})

// 加载统计信息
const loadStats = async () => {
  try {
    isLoading.value = true
    const response = await getDashboardStatsApi()
    const data = response.data

    // 格式化KPI统计数据
    kpiStats.value = [
      {
        key: 'avgTemp',
        title: '平均温度',
        value: data.environment?.avgTemperature?.toFixed(2) || '4.5°C',
        trendText: '比上周下降0.3°C',
        trendIcon: 'el-icon-caret-bottom',
        trendClass: 'trend-down',
        icon: 'el-icon-ice-cream-round',
        iconClass: 'temp-icon',
        sparklineData: [4.2, 4.8, 4.5, 4.7, 4.4, 4.6, 4.5]
      },
      {
        key: 'deviceOnlineRate',
        title: '设备在线率',
        value: data.devices?.onlineRate ? `${data.devices.onlineRate}%` : '98.5%',
        trendText: '上升0.2%',
        trendIcon: 'el-icon-caret-top',
        trendClass: 'trend-up',
        icon: 'el-icon-monitor',
        iconClass: 'device-icon',
        sparklineData: [98.2, 98.5, 98.7, 98.4, 98.6, 98.8, 98.5]
      },
      {
        key: 'totalAlerts',
        title: '告警总数',
        value: data.alerts?.total || 24,
        trendText: '增加12%',
        trendIcon: 'el-icon-caret-top',
        trendClass: 'trend-up danger',
        icon: 'el-icon-warning',
        iconClass: 'alert-icon',
        sparklineData: [20, 22, 25, 18, 24, 26, 24]
      },
      {
        key: 'avgCloseTime',
        title: '平均闭环时长',
        value: data.workOrders?.avgCloseTime || '4.2h',
        trendText: '缩短0.5h',
        trendIcon: 'el-icon-caret-bottom',
        trendClass: 'trend-down',
        icon: 'el-icon-timer',
        iconClass: 'work-icon',
        sparklineData: [4.8, 4.5, 4.3, 4.7, 4.4, 4.3, 4.2]
      }
    ]
  } catch (error) {
    console.error('加载统计信息失败:', error)
    // 使用模拟数据填充
    kpiStats.value = [
      {
        key: 'avgTemp',
        title: '平均温度',
        value: '4.5°C',
        trendText: '比上周下降0.3°C',
        trendIcon: 'el-icon-caret-bottom',
        trendClass: 'trend-down',
        icon: 'el-icon-ice-cream-round',
        iconClass: 'temp-icon',
        sparklineData: [4.2, 4.8, 4.5, 4.7, 4.4, 4.6, 4.5]
      },
      {
        key: 'deviceOnlineRate',
        title: '设备在线率',
        value: '98.5%',
        trendText: '上升0.2%',
        trendIcon: 'el-icon-caret-top',
        trendClass: 'trend-up',
        icon: 'el-icon-monitor',
        iconClass: 'device-icon',
        sparklineData: [98.2, 98.5, 98.7, 98.4, 98.6, 98.8, 98.5]
      },
      {
        key: 'totalAlerts',
        title: '告警总数',
        value: '24',
        trendText: '增加12%',
        trendIcon: 'el-icon-caret-top',
        trendClass: 'trend-up danger',
        icon: 'el-icon-warning',
        iconClass: 'alert-icon',
        sparklineData: [20, 22, 25, 18, 24, 26, 24]
      },
      {
        key: 'avgCloseTime',
        title: '平均闭环时长',
        value: '4.2h',
        trendText: '缩短0.5h',
        trendIcon: 'el-icon-caret-bottom',
        trendClass: 'trend-down',
        icon: 'el-icon-timer',
        iconClass: 'work-icon',
        sparklineData: [4.8, 4.5, 4.3, 4.7, 4.4, 4.3, 4.2]
      }
    ]
  } finally {
    isLoading.value = false
  }
}

// 加载趋势数据
const loadTrendData = async () => {
  try {
    isLoading.value = true

    const [startDate, endDate] = filterForm.value.dateRange
    const params = {
      startDate,
      endDate,
      interval: filterForm.value.interval
    }

    // 并行加载各种趋势数据
    const [envData, alertData, workOrderData, deviceData] = await Promise.allSettled([
      getEnvironmentTrendApi(params),
      getAlertTrendApi(params),
      getWorkOrderTrendApi(params),
      getDeviceStatusTrendApi(params)
    ])

    // 处理成功的结果
    const envResult = envData.status === 'fulfilled' ? envData.value.data : { data: [] }
    const alertResult = alertData.status === 'fulfilled' ? alertData.value.data : { data: {} }
    const workOrderResult = workOrderData.status === 'fulfilled' ? workOrderData.value.data : { data: {} }
    const deviceResult = deviceData.status === 'fulfilled' ? deviceData.value.data : { data: [] }

    // 渲染图表
    renderEnvironmentChart(envResult)
    renderAlertTrendChart(alertResult)
    renderWorkOrderTrendChart(workOrderResult)

    // 准备表格数据
    prepareTableData(envResult, alertResult, workOrderResult)
  } catch (error) {
    console.error('加载趋势数据失败:', error)
  } finally {
    isLoading.value = false
  }
}

// 准备表格数据
const prepareTableData = (envData, alertData, workOrderData) => {
  let tableData = []

  // 根据当前激活的tab准备不同的数据
  if (activeTab.value === 'tempHumidity' && envData.data) {
    tableData = envData.data.map(item => ({
      date: item.date,
      temperature: item.temperature,
      humidity: item.humidity,
      maxTemperature: item.maxTemperature,
      minTemperature: item.minTemperature,
      maxHumidity: item.maxHumidity,
      minHumidity: item.minHumidity
    }))
  } else if (activeTab.value === 'alerts' && alertData.data) {
    const dates = Object.keys(alertData.data)
    tableData = dates.map(date => ({
      date,
      alertCount: alertData.data[date].total || 0,
      criticalAlerts: alertData.data[date].critical || 0,
      highAlerts: alertData.data[date].high || 0,
      mediumAlerts: alertData.data[date].medium || 0,
      lowAlerts: alertData.data[date].low || 0
    }))
  } else if (activeTab.value === 'workOrders' && workOrderData.data) {
    const dates = Object.keys(workOrderData.data)
    tableData = dates.map(date => ({
      date,
      newWorkOrders: workOrderData.data[date].total || 0,
      completedWorkOrders: workOrderData.data[date].completed || 0,
      pendingWorkOrders: workOrderData.data[date].pending || 0,
      processingWorkOrders: workOrderData.data[date].processing || 0
    }))
  }

  currentTableData.value = tableData
  total.value = tableData.length
}

// 渲染环境趋势图
const renderEnvironmentChart = (data) => {
  const chartData = data.data || []
  const dates = chartData.map(item => item.date)
  const temperatures = chartData.map(item => item.temperature)
  const humidities = chartData.map(item => item.humidity)
  const maxTemps = chartData.map(item => item.maxTemperature)
  const minTemps = chartData.map(item => item.minTemperature)

  environmentChartOption.value = {
    animationDuration: 1000,
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'cross',
        label: {
          backgroundColor: '#6a7985'
        }
      },
      formatter: (params) => {
        const param = params[0]
        let result = `${param.axisValue}<br/>`
        params.forEach(p => {
          if (p.seriesName === '温度') {
            result += `${p.marker}${p.seriesName}: ${p.data.toFixed(2)}°C<br/>`
          } else if (p.seriesName === '湿度') {
            result += `${p.marker}${p.seriesName}: ${p.data.toFixed(2)}%<br/>`
          }
        })
        return result
      }
    },
    legend: {
      data: ['温度', '湿度', '最高温度', '最低温度'],
      top: '10px'
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      top: '8%',
      containLabel: true
    },
    dataZoom: [{
      type: 'inside'
    }, {
      type: 'slider',
      bottom: 20
    }],
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: dates
    },
    yAxis: [
      {
        type: 'value',
        name: '温度 (°C)',
        position: 'left',
        min: 0,
        max: 10,
        axisLine: {
          lineStyle: {
            color: '#409EFF'
          }
        }
      },
      {
        type: 'value',
        name: '湿度 (%)',
        position: 'right',
        min: 0,
        max: 100,
        axisLine: {
          lineStyle: {
            color: '#67C23A'
          }
        }
      }
    ],
    series: [
      {
        name: '温度',
        type: 'line',
        data: temperatures,
        smooth: true,
        symbol: 'none',
        lineStyle: {
          color: '#409EFF',
          width: 2
        },
        areaStyle: {
          opacity: 0.2,
          color: '#409EFF'
        }
      },
      {
        name: '湿度',
        type: 'line',
        data: humidities,
        smooth: true,
        symbol: 'none',
        lineStyle: {
          color: '#67C23A',
          width: 2
        },
        areaStyle: {
          opacity: 0.2,
          color: '#67C23A'
        },
        yAxisIndex: 1
      },
      {
        name: '最高温度',
        type: 'line',
        data: maxTemps,
        smooth: true,
        symbol: 'none',
        lineStyle: {
          color: '#F56C6C',
          type: 'dashed',
          width: 1
        }
      },
      {
        name: '最低温度',
        type: 'line',
        data: minTemps,
        smooth: true,
        symbol: 'none',
        lineStyle: {
          color: '#909399',
          type: 'dashed',
          width: 1
        }
      }
    ]
  }
}

// 渲染告警趋势图
const renderAlertTrendChart = (data) => {
  const chartData = data.data || {}
  const dates = Object.keys(chartData).sort()
  const totalAlerts = dates.map(date => chartData[date].total || 0)
  const criticalAlerts = dates.map(date => chartData[date].critical || 0)
  const highAlerts = dates.map(date => chartData[date].high || 0)
  const mediumAlerts = dates.map(date => chartData[date].medium || 0)
  const lowAlerts = dates.map(date => chartData[date].low || 0)

  alertTrendChartOption.value = {
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'shadow'
      }
    },
    legend: {
      data: ['严重告警', '高危告警', '中等告警', '低危告警'],
      top: '10px'
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      top: '15%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: dates,
      axisTick: {
        alignWithLabel: true
      }
    },
    yAxis: {
      type: 'value'
    },
    series: [
      {
        name: '严重告警',
        type: 'bar',
        stack: '告警',
        data: criticalAlerts,
        itemStyle: {
          color: '#F56C6C'
        }
      },
      {
        name: '高危告警',
        type: 'bar',
        stack: '告警',
        data: highAlerts,
        itemStyle: {
          color: '#E6A23C'
        }
      },
      {
        name: '中等告警',
        type: 'bar',
        stack: '告警',
        data: mediumAlerts,
        itemStyle: {
          color: '#F7BA2A'
        }
      },
      {
        name: '低危告警',
        type: 'bar',
        stack: '告警',
        data: lowAlerts,
        itemStyle: {
          color: '#909399'
        }
      }
    ]
  }
}

// 渲染工单趋势图
const renderWorkOrderTrendChart = (data) => {
  const chartData = data.data || {}
  const dates = Object.keys(chartData).sort()
  const newWorkOrders = dates.map(date => chartData[date].total || 0)
  const completedWorkOrders = dates.map(date => chartData[date].completed || 0)
  const pendingWorkOrders = dates.map(date => chartData[date].pending || 0)
  const processingWorkOrders = dates.map(date => chartData[date].processing || 0)

  workOrderTrendChartOption.value = {
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'cross'
      }
    },
    legend: {
      data: ['新增工单', '完成工单', '待处理', '处理中'],
      top: '10px'
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      top: '15%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: dates
    },
    yAxis: {
      type: 'value'
    },
    series: [
      {
        name: '新增工单',
        type: 'line',
        data: newWorkOrders,
        smooth: true,
        lineStyle: {
          color: '#F7BA2A',
          width: 2
        },
        itemStyle: {
          color: '#F7BA2A'
        }
      },
      {
        name: '完成工单',
        type: 'line',
        data: completedWorkOrders,
        smooth: true,
        lineStyle: {
          color: '#67C23A',
          width: 2
        },
        itemStyle: {
          color: '#67C23A'
        }
      },
      {
        name: '待处理',
        type: 'line',
        data: pendingWorkOrders,
        smooth: true,
        lineStyle: {
          color: '#409EFF',
          width: 2
        },
        itemStyle: {
          color: '#409EFF'
        }
      },
      {
        name: '处理中',
        type: 'line',
        data: processingWorkOrders,
        smooth: true,
        lineStyle: {
          color: '#E6A23C',
          width: 2
        },
        itemStyle: {
          color: '#E6A23C'
        }
      }
    ]
  }
}

// 日期范围变化事件
const onDateRangeChange = () => {
  // 根据选择的日期范围调整默认间隔
  if (filterForm.value.dateRange && filterForm.value.dateRange.length === 2) {
    const [start, end] = filterForm.value.dateRange
    const startDate = new Date(start)
    const endDate = new Date(end)
    const diffDays = (endDate - startDate) / (24 * 60 * 60 * 1000)

    if (diffDays <= 2) {
      filterForm.value.interval = 'hourly'
    } else if (diffDays <= 31) {
      filterForm.value.interval = 'daily'
    } else if (diffDays <= 365) {
      filterForm.value.interval = 'weekly'
    } else {
      filterForm.value.interval = 'monthly'
    }
  }

  loadTrendData()
}

// 快速选择时间范围
const setQuickRange = (range) => {
  const now = new Date()
  let start = new Date(now)

  switch(range) {
    case 'last24h':
      start.setDate(now.getDate() - 1)
      filterForm.value.interval = 'hourly'
      break
    case 'last7d':
      start.setDate(now.getDate() - 7)
      filterForm.value.interval = 'daily'
      break
    case 'thisMonth':
      start = new Date(now.getFullYear(), now.getMonth(), 1)
      filterForm.value.interval = 'daily'
      break
    case 'lastMonth':
      start = new Date(now.getFullYear(), now.getMonth() - 1, 1)
      const end = new Date(now.getFullYear(), now.getMonth(), 0)
      filterForm.value.dateRange = [start, end]
      filterForm.value.interval = 'daily'
      break
  }

  if (range !== 'lastMonth') {
    filterForm.value.dateRange = [start, now]
  }

  loadTrendData()
}

// 导出报告
const exportReport = (command) => {
  isExporting.value = true
  // 模拟导出操作
  setTimeout(() => {
    isExporting.value = false
    ElMessage.success(`${command.toUpperCase()} 报告导出成功！`)
  }, 1500)
}

// 切换图表类型
const toggleChartType = () => {
  // 可以在这里添加切换折线图/面积图等选项
  ElMessage.info('图表类型已切换')
}

// 标签页变化
const handleTabChange = (tabName) => {
  activeTab.value = tabName
  prepareTableData(
    environmentChartOption.value.series ? environmentChartOption.value.series[0]?.data || [] : {},
    alertTrendChartOption.value.series ? alertTrendChartOption.value.series[0]?.data || {} : {},
    workOrderTrendChartOption.value.series ? workOrderTrendChartOption.value.series[0]?.data || {} : {}
  )
}

// 查看详情
const viewDetails = (row) => {
  console.log('查看详情:', row)
  // 实现详情查看逻辑
}

// 分页相关方法
const handlePageSizeChange = (val) => {
  pageSize.value = val
  currentPage.value = 1
  prepareTableData(
    environmentChartOption.value.series ? environmentChartOption.value.series[0]?.data || [] : {},
    alertTrendChartOption.value.series ? alertTrendChartOption.value.series[0]?.data || {} : {},
    workOrderTrendChartOption.value.series ? workOrderTrendChartOption.value.series[0]?.data || {} : {}
  )
}

const handleCurrentPageChange = (val) => {
  currentPage.value = val
}

// 侧边栏事件处理
const handleFilterApplied = (filter) => {
  console.log('Filters applied:', filter)
  loadTrendData()
}

const handleComparisonApplied = (comparison) => {
  console.log('Comparison applied:', comparison)
  loadTrendData()
}

const handleExportRequested = (type) => {
  console.log('Export requested:', type)
  exportReport(type)
}

const handleAiAnalysisRequested = () => {
  console.log('AI Analysis requested')
  // 模拟AI分析请求
  ElMessage.info('正在请求AI分析...')
}

// 页面加载时初始化
onMounted(async () => {
  // 加载初始数据
  await loadStats()
  await loadTrendData()

  // 监听窗口大小变化，确保图表响应式
  window.addEventListener('resize', debounce(() => {
    // 图表会自动响应大小变化
  }, 200))
})
</script>

<style scoped>
.trend-analysis-container {
  padding: 20px;
  background-color: #f5f7fa;
  min-height: 100vh;
}

/* 全局控制台样式 */
.control-bar {
  margin-bottom: 20px;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.control-form {
  display: flex;
  flex-wrap: wrap;
  gap: 15px;
}

.shortcut-buttons {
  margin-left: 10px;
}

.shortcut-buttons .el-button {
  padding: 5px 10px;
  font-size: 12px;
}

/* KPI卡片样式 */
.kpi-cards {
  margin-bottom: 20px;
}

/* 图表样式 */
.main-chart-card {
  margin-bottom: 20px;
  background: white;
  border-radius: 8px;
  overflow: hidden;
}

.sub-charts {
  margin-bottom: 20px;
}

.sub-chart-card {
  background: white;
  border-radius: 8px;
  overflow: hidden;
}

.chart-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.chart-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.chart-actions {
  display: flex;
  align-items: center;
  gap: 10px;
}

/* 数据表格样式 */
.data-table-card {
  background: white;
  border-radius: 8px;
  overflow: hidden;
}

/* 骨架屏样式 */
.kpi-skeleton {
  margin-bottom: 20px;
}

.chart-skeleton {
  margin-bottom: 20px;
  height: 500px;
}

.sub-chart-skeleton {
  margin-bottom: 20px;
  height: 350px;
}

/* 响应式设计 */
@media (max-width: 1200px) {
  .kpi-card {
    height: auto;
    min-height: 140px;
  }
}

@media (max-width: 768px) {
  .control-form {
    flex-direction: column;
  }

  .el-form-item {
    width: 100%;
  }

  .el-col.el-col-6 {
    width: 100%;
    margin-bottom: 15px;
  }

  .el-col.el-col-12 {
    width: 100%;
    margin-bottom: 20px;
  }

  .main-chart {
    height: 300px !important;
  }

  .sub-chart {
    height: 250px !important;
  }

  .el-col.el-col-20 {
    width: 100%;
  }

  .el-col.el-col-4 {
    display: none;
  }
}
</style>