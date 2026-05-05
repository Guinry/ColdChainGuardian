<template>
  <Layout>
    <div class="analysis-page" v-loading="isLoading" element-loading-text="正在生成分析...">
      <header class="analysis-header">
        <div>
          <h1>趋势分析</h1>
          <p>围绕设备在线、告警变化和工单闭环查看运行趋势</p>
        </div>
        <div class="header-actions">
          <el-button :icon="Refresh" @click="loadAllData">刷新</el-button>
          <el-button type="primary" :icon="Search" @click="loadAllData">生成分析</el-button>
        </div>
      </header>

      <section class="filter-band">
        <el-date-picker
          v-model="filterForm.dateRange"
          type="daterange"
          range-separator="至"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
          format="YYYY-MM-DD"
          value-format="YYYY-MM-DD"
          class="date-picker"
          @change="handleDateRangeChange"
        />

        <el-segmented v-model="quickRange" :options="quickRangeOptions" @change="setQuickRange" />

        <el-radio-group v-model="filterForm.interval" @change="loadAllData">
          <el-radio-button value="hourly" :disabled="!isHourlyEnabled">小时</el-radio-button>
          <el-radio-button value="daily">天</el-radio-button>
          <el-radio-button value="weekly">周</el-radio-button>
          <el-radio-button value="monthly">月</el-radio-button>
        </el-radio-group>
      </section>

      <section class="metric-strip">
        <div v-for="metric in metrics" :key="metric.label" class="metric-tile" :class="metric.tone">
          <span class="metric-icon">
            <el-icon><component :is="metric.icon" /></el-icon>
          </span>
          <span class="metric-copy">
            <span class="metric-label">{{ metric.label }}</span>
            <strong>{{ metric.value }}</strong>
            <em>{{ metric.note }}</em>
          </span>
        </div>
      </section>

      <section class="analysis-grid">
        <div class="panel primary-panel">
          <div class="panel-header">
            <div>
              <h2>设备在线趋势</h2>
              <p>展示所选时间范围内在线与离线设备数量变化</p>
            </div>
            <el-tag type="info" size="small">{{ intervalLabel }}</el-tag>
          </div>
          <Echarts :option="deviceStatusChartOption" height="380" />
        </div>

        <aside class="panel insight-panel">
          <div class="panel-header">
            <div>
              <h2>运行判断</h2>
              <p>根据当前统计给出值班侧重点</p>
            </div>
          </div>
          <div class="insight-list">
            <div v-for="item in insights" :key="item.title" class="insight-item" :class="item.tone">
              <span class="insight-dot"></span>
              <div>
                <strong>{{ item.title }}</strong>
                <p>{{ item.text }}</p>
              </div>
            </div>
          </div>
        </aside>
      </section>

      <section class="sub-grid">
        <div class="panel">
          <div class="panel-header">
            <div>
              <h2>告警趋势</h2>
              <p>按等级拆分告警数量，当前范围内无新增时保持零线</p>
            </div>
            <el-button link type="primary" @click="goToAlerts">查看告警</el-button>
          </div>
          <Echarts :option="alertTrendChartOption" height="270" />
        </div>

        <div class="panel">
          <div class="panel-header">
            <div>
              <h2>工单趋势</h2>
              <p>观察待处理、处理中、已完成和已关闭工单变化</p>
            </div>
            <el-button link type="primary" @click="goToOrders">查看工单</el-button>
          </div>
          <Echarts :option="workOrderTrendChartOption" height="270" />
        </div>
      </section>

      <section class="panel detail-panel">
        <div class="panel-header">
          <div>
            <h2>趋势明细</h2>
            <p>按时间粒度汇总设备、告警和工单指标</p>
          </div>
          <el-tabs v-model="activeTab" class="summary-tabs">
            <el-tab-pane label="设备" name="devices" />
            <el-tab-pane label="告警" name="alerts" />
            <el-tab-pane label="工单" name="workOrders" />
          </el-tabs>
        </div>

        <el-table :data="activeTableData" stripe empty-text="暂无趋势明细">
          <el-table-column
            v-for="column in activeColumns"
            :key="column.prop"
            :prop="column.prop"
            :label="column.label"
            :min-width="column.minWidth"
            :align="column.align || 'left'"
          />
          <el-table-column label="操作" width="100" align="center">
            <template #default="{ row }">
              <el-button link type="primary" size="small" @click="viewDetails(row)">查看</el-button>
            </template>
          </el-table-column>
        </el-table>
      </section>
    </div>
  </Layout>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  Search,
  Refresh,
  Monitor,
  WarningFilled,
  Tickets,
  Finished,
  TrendCharts
} from '@element-plus/icons-vue'
import {
  getDashboardStatsApi,
  getAlertTrendApi,
  getWorkOrderTrendApi,
  getDeviceStatusTrendApi
} from '@/api/dashboard'
import Layout from '@/components/Layout.vue'
import Echarts from './components/Echarts.vue'

const router = useRouter()

const today = new Date()
const toDateString = (date) => date.toISOString().split('T')[0]

const filterForm = ref({
  dateRange: [
    toDateString(new Date(Date.now() - 7 * 24 * 60 * 60 * 1000)),
    toDateString(today)
  ],
  interval: 'daily'
})
const quickRange = ref('last7d')
const isLoading = ref(false)
const stats = ref({})
const deviceStatusData = ref([])
const alertTrendData = ref([])
const workOrderTrendData = ref([])
const activeTab = ref('devices')

const quickRangeOptions = [
  { label: '24小时', value: 'last24h' },
  { label: '7天', value: 'last7d' },
  { label: '本月', value: 'thisMonth' },
  { label: '上月', value: 'lastMonth' }
]

const isHourlyEnabled = computed(() => {
  const [start, end] = filterForm.value.dateRange || []
  if (!start || !end) return false
  const diffDays = (new Date(end) - new Date(start)) / (1000 * 60 * 60 * 24)
  return diffDays <= 1
})

const intervalLabel = computed(() => {
  const map = { hourly: '小时粒度', daily: '日粒度', weekly: '周粒度', monthly: '月粒度' }
  return map[filterForm.value.interval] || '日粒度'
})

const metrics = computed(() => {
  const totalDevices = Number(stats.value.totalDevices || 0)
  const onlineDevices = Number(stats.value.onlineDevices || 0)
  const offlineDevices = Math.max(0, totalDevices - onlineDevices)
  const onlineRate = totalDevices ? Math.round((onlineDevices / totalDevices) * 100) : 0
  return [
    {
      label: '设备在线',
      value: `${onlineDevices}/${totalDevices}`,
      note: `在线率 ${onlineRate}%`,
      icon: Monitor,
      tone: offlineDevices ? 'warning' : 'success'
    },
    {
      label: '未处理告警',
      value: stats.value.unhandledAlerts || 0,
      note: `今日新增 ${stats.value.todayAlerts || 0}`,
      icon: WarningFilled,
      tone: stats.value.unhandledAlerts ? 'danger' : 'success'
    },
    {
      label: '待处理工单',
      value: stats.value.pendingWorkOrders || 0,
      note: '等待派工或处理',
      icon: Tickets,
      tone: stats.value.pendingWorkOrders ? 'warning' : 'neutral'
    },
    {
      label: '今日闭环',
      value: stats.value.todayClosedWorkOrders || 0,
      note: '已完成工单',
      icon: Finished,
      tone: 'primary'
    }
  ]
})

const insights = computed(() => {
  const totalDevices = Number(stats.value.totalDevices || 0)
  const onlineDevices = Number(stats.value.onlineDevices || 0)
  const offlineDevices = Math.max(0, totalDevices - onlineDevices)
  const unhandledAlerts = Number(stats.value.unhandledAlerts || 0)
  const pendingWorkOrders = Number(stats.value.pendingWorkOrders || 0)
  return [
    {
      title: offlineDevices ? '设备离线需要排查' : '设备在线状态稳定',
      text: offlineDevices ? `当前有 ${offlineDevices} 台设备离线，建议优先核查网关、供电和设备安装位置。` : '当前设备在线状态良好，可继续观察趋势变化。',
      tone: offlineDevices ? 'warning' : 'success'
    },
    {
      title: unhandledAlerts ? '告警仍有积压' : '告警压力较低',
      text: unhandledAlerts ? `还有 ${unhandledAlerts} 条未处理告警，应结合告警中心逐条研判。` : '当前范围内未处理告警压力较低。',
      tone: unhandledAlerts ? 'danger' : 'success'
    },
    {
      title: pendingWorkOrders ? '工单需跟进' : '工单待办清爽',
      text: pendingWorkOrders ? `仍有 ${pendingWorkOrders} 张待处理工单，建议按优先级推进闭环。` : '暂无待处理工单，运维队列处于可控状态。',
      tone: pendingWorkOrders ? 'warning' : 'success'
    }
  ]
})

const deviceStatusChartOption = computed(() => ({
  tooltip: { trigger: 'axis' },
  legend: { data: ['在线', '离线'], top: 4 },
  grid: { left: 36, right: 20, top: 44, bottom: 34, containLabel: true },
  xAxis: {
    type: 'category',
    data: deviceStatusData.value.map(item => item.date || item.time || item.period),
    axisTick: { show: false }
  },
  yAxis: { type: 'value', minInterval: 1, splitLine: { lineStyle: { color: '#edf2f7' } } },
  series: [
    {
      name: '在线',
      type: 'line',
      smooth: true,
      showSymbol: false,
      data: deviceStatusData.value.map(item => Number(item.online || 0)),
      lineStyle: { width: 3, color: '#16a34a' },
      areaStyle: { color: 'rgba(22, 163, 74, 0.12)' }
    },
    {
      name: '离线',
      type: 'line',
      smooth: true,
      showSymbol: false,
      data: deviceStatusData.value.map(item => Number(item.offline || 0)),
      lineStyle: { width: 3, color: '#f59e0b' },
      areaStyle: { color: 'rgba(245, 158, 11, 0.12)' }
    }
  ]
}))

const alertTrendChartOption = computed(() => ({
  tooltip: { trigger: 'axis' },
  legend: { data: ['紧急', '高', '中', '低'], top: 2 },
  grid: { left: 28, right: 16, top: 42, bottom: 30, containLabel: true },
  xAxis: { type: 'category', data: alertTrendData.value.map(item => item.date), axisTick: { show: false } },
  yAxis: { type: 'value', minInterval: 1, splitLine: { lineStyle: { color: '#edf2f7' } } },
  series: [
    { name: '紧急', type: 'bar', stack: 'alert', data: alertTrendData.value.map(item => item.critical), itemStyle: { color: '#dc2626' } },
    { name: '高', type: 'bar', stack: 'alert', data: alertTrendData.value.map(item => item.high), itemStyle: { color: '#f97316' } },
    { name: '中', type: 'bar', stack: 'alert', data: alertTrendData.value.map(item => item.medium), itemStyle: { color: '#f59e0b' } },
    { name: '低', type: 'bar', stack: 'alert', data: alertTrendData.value.map(item => item.low), itemStyle: { color: '#60a5fa' } }
  ]
}))

const workOrderTrendChartOption = computed(() => ({
  tooltip: { trigger: 'axis' },
  legend: { data: ['待处理', '处理中', '已完成', '已关闭'], top: 2 },
  grid: { left: 28, right: 16, top: 42, bottom: 30, containLabel: true },
  xAxis: { type: 'category', data: workOrderTrendData.value.map(item => item.date), axisTick: { show: false } },
  yAxis: { type: 'value', minInterval: 1, splitLine: { lineStyle: { color: '#edf2f7' } } },
  series: [
    { name: '待处理', type: 'line', smooth: true, showSymbol: false, data: workOrderTrendData.value.map(item => item.pending), lineStyle: { color: '#f59e0b', width: 3 } },
    { name: '处理中', type: 'line', smooth: true, showSymbol: false, data: workOrderTrendData.value.map(item => item.processing), lineStyle: { color: '#2563eb', width: 3 } },
    { name: '已完成', type: 'line', smooth: true, showSymbol: false, data: workOrderTrendData.value.map(item => item.completed), lineStyle: { color: '#16a34a', width: 3 } },
    { name: '已关闭', type: 'line', smooth: true, showSymbol: false, data: workOrderTrendData.value.map(item => item.closed), lineStyle: { color: '#64748b', width: 3 } }
  ]
}))

const activeColumns = computed(() => {
  if (activeTab.value === 'alerts') {
    return [
      { prop: 'date', label: '日期', minWidth: 130 },
      { prop: 'total', label: '告警总数', minWidth: 100, align: 'center' },
      { prop: 'critical', label: '紧急', minWidth: 90, align: 'center' },
      { prop: 'high', label: '高', minWidth: 90, align: 'center' },
      { prop: 'medium', label: '中', minWidth: 90, align: 'center' },
      { prop: 'low', label: '低', minWidth: 90, align: 'center' }
    ]
  }

  if (activeTab.value === 'workOrders') {
    return [
      { prop: 'date', label: '日期', minWidth: 130 },
      { prop: 'total', label: '工单总数', minWidth: 100, align: 'center' },
      { prop: 'pending', label: '待处理', minWidth: 100, align: 'center' },
      { prop: 'processing', label: '处理中', minWidth: 100, align: 'center' },
      { prop: 'completed', label: '已完成', minWidth: 100, align: 'center' },
      { prop: 'closed', label: '已关闭', minWidth: 100, align: 'center' }
    ]
  }

  return [
    { prop: 'date', label: '日期', minWidth: 130 },
    { prop: 'online', label: '在线设备', minWidth: 120, align: 'center' },
    { prop: 'offline', label: '离线设备', minWidth: 120, align: 'center' },
    { prop: 'onlineRate', label: '在线率', minWidth: 120, align: 'center' }
  ]
})

const activeTableData = computed(() => {
  if (activeTab.value === 'alerts') return alertTrendData.value
  if (activeTab.value === 'workOrders') return workOrderTrendData.value
  return deviceStatusData.value.map(item => ({
    ...item,
    onlineRate: `${getOnlineRate(item.online, item.offline)}%`
  }))
})

const getOnlineRate = (online, offline) => {
  const total = Number(online || 0) + Number(offline || 0)
  return total ? Math.round((Number(online || 0) / total) * 100) : 0
}

const unwrapData = (response, fallback = {}) => {
  const payload = response?.data
  if (!payload) return fallback
  if (payload.code === 200 || payload.success) return payload.data ?? fallback
  return payload.data ?? payload ?? fallback
}

const normalizeMapTrend = (payload, defaults) => {
  const source = payload?.data
  if (Array.isArray(source)) {
    return source.map(item => ({ ...defaults, ...item, date: item.date || item.time || item.period }))
  }
  if (source && typeof source === 'object') {
    return Object.entries(source).map(([date, value]) => ({ ...defaults, ...(value || {}), date }))
  }
  return []
}

const loadAllData = async () => {
  isLoading.value = true
  try {
    const [startDate, endDate] = filterForm.value.dateRange || []
    const params = { startDate, endDate, interval: filterForm.value.interval }

    const [statsResult, deviceResult, alertResult, workOrderResult] = await Promise.allSettled([
      getDashboardStatsApi(),
      getDeviceStatusTrendApi(params),
      getAlertTrendApi(params),
      getWorkOrderTrendApi(params)
    ])

    if (statsResult.status === 'fulfilled') {
      stats.value = unwrapData(statsResult.value, {})
    }

    if (deviceResult.status === 'fulfilled') {
      const payload = unwrapData(deviceResult.value, {})
      deviceStatusData.value = Array.isArray(payload.data)
        ? payload.data.map(item => ({
          date: item.date || item.time || item.period,
          online: Number(item.online || 0),
          offline: Number(item.offline || 0)
        }))
        : []
    }

    if (alertResult.status === 'fulfilled') {
      const payload = unwrapData(alertResult.value, {})
      alertTrendData.value = normalizeMapTrend(payload, {
        total: 0,
        critical: 0,
        high: 0,
        medium: 0,
        low: 0
      })
    }

    if (workOrderResult.status === 'fulfilled') {
      const payload = unwrapData(workOrderResult.value, {})
      workOrderTrendData.value = normalizeMapTrend(payload, {
        total: 0,
        pending: 0,
        processing: 0,
        completed: 0,
        closed: 0
      })
    }
  } catch (error) {
    console.error('加载趋势数据失败:', error)
    ElMessage.error('加载趋势数据失败')
  } finally {
    isLoading.value = false
  }
}

const handleDateRangeChange = () => {
  quickRange.value = ''
  if (!isHourlyEnabled.value && filterForm.value.interval === 'hourly') {
    filterForm.value.interval = 'daily'
  }
  loadAllData()
}

const setQuickRange = (range) => {
  const now = new Date()
  let startDate = new Date(now)
  let endDate = new Date(now)

  if (range === 'last24h') {
    startDate = new Date(now.getTime() - 24 * 60 * 60 * 1000)
    filterForm.value.interval = 'hourly'
  } else if (range === 'last7d') {
    startDate = new Date(now.getTime() - 7 * 24 * 60 * 60 * 1000)
    filterForm.value.interval = 'daily'
  } else if (range === 'thisMonth') {
    startDate = new Date(now.getFullYear(), now.getMonth(), 1)
    filterForm.value.interval = 'daily'
  } else if (range === 'lastMonth') {
    startDate = new Date(now.getFullYear(), now.getMonth() - 1, 1)
    endDate = new Date(now.getFullYear(), now.getMonth(), 0)
    filterForm.value.interval = 'daily'
  }

  filterForm.value.dateRange = [toDateString(startDate), toDateString(endDate)]
  loadAllData()
}

const viewDetails = (row) => {
  const query = {
    startDate: filterForm.value.dateRange?.[0],
    endDate: filterForm.value.dateRange?.[1],
    focusTime: row.date
  }
  if (activeTab.value === 'alerts') router.push({ path: '/alerts', query })
  else if (activeTab.value === 'workOrders') router.push({ path: '/work-orders', query })
  else router.push({ path: '/monitor', query })
}

const goToAlerts = () => router.push('/alerts')
const goToOrders = () => router.push('/work-orders')

onMounted(loadAllData)
</script>

<style scoped>
.analysis-page {
  min-height: 100%;
  padding: 20px 24px 28px;
  background: var(--ccg-bg);
  box-sizing: border-box;
  color: #111827;
}

.analysis-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 16px;
  margin-bottom: 16px;
}

.analysis-header h1 {
  margin: 0;
  font-size: 22px;
  font-weight: 750;
}

.analysis-header p,
.panel-header p {
  margin: 5px 0 0;
  color: #6b7280;
  font-size: 13px;
}

.header-actions {
  display: flex;
  gap: 10px;
}

.filter-band {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 14px;
  padding: 14px 16px;
  margin-bottom: 16px;
  border: 1px solid var(--ccg-border);
  border-radius: 8px;
  background: #fff;
  box-shadow: var(--ccg-shadow-sm);
}

.date-picker {
  width: min(100%, 720px);
  max-width: 720px;
  flex-shrink: 0;
}

.metric-strip {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 14px;
  margin-bottom: 16px;
}

.metric-tile {
  display: flex;
  align-items: center;
  gap: 12px;
  min-height: 92px;
  padding: 14px;
  border: 1px solid var(--ccg-border);
  border-left-width: 4px;
  border-radius: 8px;
  background: #fff;
}

.metric-icon {
  width: 40px;
  height: 40px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border-radius: 8px;
  background: #eef4ff;
  color: #2563eb;
  font-size: 21px;
  flex-shrink: 0;
}

.metric-copy {
  min-width: 0;
  display: flex;
  flex-direction: column;
}

.metric-label,
.metric-copy em {
  color: #6b7280;
  font-size: 13px;
  font-style: normal;
}

.metric-copy strong {
  margin: 4px 0 2px;
  font-size: 24px;
  line-height: 1;
}

.metric-tile.danger {
  border-left-color: #ef4444;
}

.metric-tile.warning {
  border-left-color: #f59e0b;
}

.metric-tile.success {
  border-left-color: #16a34a;
}

.metric-tile.primary {
  border-left-color: #2563eb;
}

.metric-tile.neutral {
  border-left-color: #94a3b8;
}

.analysis-grid {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 350px;
  gap: 16px;
  margin-bottom: 16px;
}

.sub-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16px;
  margin-bottom: 16px;
}

.panel {
  min-width: 0;
  padding: 16px;
  border: 1px solid var(--ccg-border);
  border-radius: 8px;
  background: #fff;
  box-shadow: var(--ccg-shadow-sm);
}

.primary-panel {
  min-height: 456px;
}

.insight-panel {
  min-height: 456px;
}

.panel-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 12px;
  margin-bottom: 12px;
}

.panel-header h2 {
  margin: 0;
  font-size: 16px;
  font-weight: 720;
}

.insight-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.insight-item {
  display: grid;
  grid-template-columns: 10px minmax(0, 1fr);
  gap: 10px;
  padding: 11px 0;
  border-bottom: 1px solid #edf2f7;
}

.insight-item:last-child {
  border-bottom: 0;
}

.insight-dot {
  width: 8px;
  height: 8px;
  margin-top: 6px;
  border-radius: 50%;
  background: #2563eb;
}

.insight-item.warning .insight-dot {
  background: #f59e0b;
}

.insight-item.danger .insight-dot {
  background: #ef4444;
}

.insight-item.success .insight-dot {
  background: #16a34a;
}

.insight-item strong {
  font-size: 14px;
}

.insight-item p {
  margin: 4px 0 0;
  color: #6b7280;
  font-size: 13px;
  line-height: 1.5;
}

.summary-tabs {
  margin-top: -8px;
}

.summary-tabs :deep(.el-tabs__header) {
  margin: 0;
}

:deep(.el-table th.el-table__cell) {
  background: #f8fafc;
  color: #4b5563;
  font-weight: 650;
}

@media (max-width: 1280px) {
  .analysis-grid,
  .sub-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 980px) {
  .filter-band,
  .analysis-header {
    flex-direction: column;
    align-items: stretch;
  }

  .date-picker {
    width: 100%;
  }

  .metric-strip {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 620px) {
  .analysis-page {
    padding: 16px;
  }

  .metric-strip {
    grid-template-columns: 1fr;
  }

  .panel-header {
    flex-direction: column;
  }
}
</style>
