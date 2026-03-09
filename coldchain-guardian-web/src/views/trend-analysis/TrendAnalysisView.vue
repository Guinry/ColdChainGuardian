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
            :real-stats="realStatsForSidebar"
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
// 正确的组件导入路径
import KpiCard from './components/KpiCard.vue'
import AnalysisSidebar from './components/AnalysisSidebar.vue'
import Echarts from './components/Echarts.vue'
import DataTable from './components/DataTable.vue'
import Layout from '@/components/Layout.vue'

// 🌟 1. 增加一个 ref 来存储侧边栏传过来的高级筛选条件
const advancedFilters = ref({})

// 🌟 2. 增加一个 ref 用于向侧边栏传递真实统计信息
const realStatsForSidebar = ref({})

const isLoading = ref(false)
const isExporting = ref(false)
const filterForm = ref({
  dateRange: [new Date(Date.now() - 7 * 24 * 60 * 60 * 1000).toISOString().split('T')[0], new Date().toISOString().split('T')[0]], // 格式化为 YYYY-MM-DD
  interval: 'daily',
  dimension: []
})

const kpiStats = ref([])
// ...保留你的 dimensionOptions, activeTab, currentTableData 等变量...

// 示例变量，实际项目中应根据实际需求定义
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

// 根据你的具体需要，定义一些辅助变量
const isHourlyEnabled = computed(() => {
  if (!filterForm.value.dateRange || filterForm.value.dateRange.length < 2) return false
  const [startDateStr, endDateStr] = filterForm.value.dateRange
  const startDate = new Date(startDateStr)
  const endDate = new Date(endDateStr)
  const diffDays = (endDate - startDate) / (1000 * 60 * 60 * 24)
  return diffDays <= 1 // 如果时间跨度小于等于1天，则启用小时粒度
})

// 加载统计信息 (修复数据路径映射)
const loadStats = async () => {
  try {
    const response = await getDashboardStatsApi()
    const data = response.data.data || response.data // 兼容后端结构

    // 修复数据映射，对接后端扁平结构
    const totalDevices = data.totalDevices || 1;
    const onlineDevices = data.onlineDevices || 0;
    const onlineRate = ((onlineDevices / totalDevices) * 100).toFixed(1);

    kpiStats.value = [
      {
        key: 'avgTemp',
        title: '设备总数/在线',
        value: `${onlineDevices} / ${totalDevices}`,
        trendText: `在线率 ${onlineRate}%`,
        trendIcon: 'Monitor',
        trendClass: onlineRate > 90 ? 'trend-up' : 'trend-down',
        icon: 'Monitor',
        iconClass: 'device-icon',
        sparklineData: [] // 如果后端没有微缩图数据，传空数组即可
      },
      {
        key: 'totalAlerts',
        title: '今日告警/未处理',
        value: `${data.todayAlerts || 0} / ${data.unhandledAlerts || 0}`,
        trendText: '实时监控中',
        trendIcon: 'Warning',
        trendClass: data.unhandledAlerts > 0 ? 'trend-down danger' : 'trend-up',
        icon: 'Warning',
        iconClass: 'alert-icon',
        sparklineData: []
      },
      {
        key: 'workOrders',
        title: '本周已闭环工单',
        value: data.todayClosedWorkOrders || 0,
        trendText: '工单处理效率',
        trendIcon: 'Check',
        trendClass: 'trend-up',
        icon: 'DocumentChecked',
        iconClass: 'work-icon',
        sparklineData: []
      }
    ]

    // 同步更新侧边栏所需的真实统计信息
    realStatsForSidebar.value = {
      totalAlerts: data.totalAlerts || data.todayAlerts || 0,
      totalWorkOrders: data.totalWorkOrders || data.todayClosedWorkOrders || 0,
      totalDevices: data.totalDevices || 0,
      avgTemperature: data.avgTemperature || 0,
      deviceOnlineRate: onlineRate
    }
  } catch (error) {
    console.error('加载统计信息失败:', error)
  }
}

// 加载趋势数据 (🌟 修复：合并侧边栏参数)
const loadTrendData = async () => {
  try {
    isLoading.value = true

    const [startDate, endDate] = filterForm.value.dateRange

    // 🌟 将顶部时间组件与侧边栏的高级筛选合并
    const params = {
      startDate,
      endDate,
      interval: filterForm.value.interval,
      ...advancedFilters.value // 注入侧边栏的过滤参数（region, deviceType等）
    }

    const [envData, alertData, workOrderData, deviceData] = await Promise.allSettled([
      getEnvironmentTrendApi(params),
      getAlertTrendApi(params),
      getWorkOrderTrendApi(params),
      getDeviceStatusTrendApi(params)
    ])

    const envResult = envData.status === 'fulfilled' ? envData.value.data : { data: [] }
    const alertResult = alertData.status === 'fulfilled' ? alertData.value.data : { data: {} }
    const workOrderResult = workOrderData.status === 'fulfilled' ? workOrderData.value.data : { data: {} }

    renderEnvironmentChart(envResult)
    renderAlertTrendChart(alertResult)
    renderWorkOrderTrendChart(workOrderResult)
    prepareTableData(envResult, alertResult, workOrderResult)

  } catch (error) {
    console.error('加载趋势数据失败:', error)
    ElMessage.error('加载图表数据失败')
  } finally {
    isLoading.value = false
  }
}

// 侧边栏事件处理 (🌟 修复：保存参数并重新发请求)
const handleFilterApplied = (filter) => {
  advancedFilters.value = filter // 保存侧边栏筛选条件
  loadTrendData() // 重新请求
}

const handleComparisonApplied = (comparison) => {
  // 对比分析逻辑...
  ElMessage.info('对比分析功能请求已发送')
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

// 辅助函数（这些函数需要根据实际情况实现）
const onDateRangeChange = () => {
  loadTrendData()
}

const setQuickRange = (range) => {
  const now = new Date()
  let startDate

  switch(range) {
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

const toggleChartType = () => {
  // 切换图表类型的逻辑
  console.log('Toggle chart type')
}

const exportReport = (type) => {
  // 导出报告的逻辑
  console.log(`Exporting ${type} report`)
}

const renderEnvironmentChart = (data) => {
  // 实现环境图表渲染
  console.log('Rendering environment chart:', data)
}

const renderAlertTrendChart = (data) => {
  // 实现告警趋势图表渲染
  console.log('Rendering alert chart:', data)
}

const renderWorkOrderTrendChart = (data) => {
  // 实现工单趋势图表渲染
  console.log('Rendering work order chart:', data)
}

const prepareTableData = (envData, alertData, workOrderData) => {
  // 准备表格数据
  console.log('Preparing table data:', envData, alertData, workOrderData)
}

const handleTabChange = (tabName) => {
  // 处理标签页切换
  console.log('Tab changed to:', tabName)
}

const handleCurrentPageChange = (page) => {
  currentPage.value = page
  // 重新加载数据
}

const handlePageSizeChange = (size) => {
  pageSize.value = size
  currentPage.value = 1
  // 重新加载数据
}

const viewDetails = (row) => {
  // 查看详情
  console.log('Viewing details:', row)
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