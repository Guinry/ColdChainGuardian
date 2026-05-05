<template>
  <Layout>
    <div class="dashboard-page" v-loading="loading" element-loading-text="正在加载运行数据...">
      <div class="dashboard-header">
        <div>
          <h1>运行总览</h1>
          <p>最后更新 {{ lastUpdated || '-' }}</p>
        </div>
        <div class="header-actions">
          <el-button :icon="Refresh" @click="fetchAllData">刷新</el-button>
          <el-button type="primary" :icon="Warning" @click="goToUnhandledAlerts">处理告警</el-button>
        </div>
      </div>

      <div class="kpi-strip">
        <button
          v-for="card in kpiCards"
          :key="card.label"
          type="button"
          class="kpi-card"
          :class="`tone-${card.tone}`"
          @click="go(card.path, card.query)"
        >
          <span class="kpi-icon">
            <el-icon><component :is="card.icon" /></el-icon>
          </span>
          <span class="kpi-copy">
            <span class="kpi-label">{{ card.label }}</span>
            <span class="kpi-value">{{ card.value }}</span>
            <span class="kpi-note">{{ card.note }}</span>
          </span>
        </button>
      </div>

      <div class="operations-grid">
        <section class="panel area-panel">
          <div class="panel-header">
            <div>
              <h2>库区运行状态</h2>
              <p>按实时设备数据汇总在线、告警和环境指标</p>
            </div>
            <el-select v-model="selectedArea" class="compact-select" placeholder="全部库区" clearable>
              <el-option label="全部库区" value="" />
              <el-option v-for="area in areas" :key="area.id" :label="area.name" :value="area.id" />
            </el-select>
          </div>

          <div class="area-list">
            <el-empty v-if="!filteredAreas.length" description="暂无库区数据" :image-size="72" />
            <button
              v-for="area in filteredAreas"
              :key="area.id"
              type="button"
              class="area-row"
              @click="goToAreaDetail(area)"
            >
              <span class="area-main">
                <span class="area-title">
                  <strong>{{ area.name }}</strong>
                  <el-tag size="small" :type="area.tagType">{{ area.statusText }}</el-tag>
                </span>
                <span class="area-subtitle">{{ area.total }} 台设备 · 在线 {{ area.online }} · 告警 {{ area.alarming }}</span>
              </span>
              <span class="area-metrics">
                <span>{{ formatMetric(area.avgTemp, '°C') }}</span>
                <span>{{ formatMetric(area.avgHumi, '%') }}</span>
              </span>
              <span class="online-bar">
                <span :style="{ width: `${area.onlineRate}%` }"></span>
              </span>
            </button>
          </div>
        </section>

        <section class="panel trend-panel" v-loading="chartLoading">
          <div class="panel-header">
            <div>
              <h2>环境趋势</h2>
              <p>{{ chartMetricText }} · 近 30 天</p>
            </div>
            <el-select v-model="chartMetric" class="compact-select" placeholder="指标">
              <el-option label="温度" value="temperature" />
              <el-option label="湿度" value="humidity" />
            </el-select>
          </div>
          <div class="chart-wrap">
            <div v-show="chartData.length" ref="chartRef" class="trend-chart"></div>
            <el-empty v-if="!chartData.length" description="暂无趋势数据" :image-size="80" />
          </div>
        </section>

        <section class="panel queue-panel">
          <div class="panel-header">
            <div>
              <h2>待处理队列</h2>
              <p>优先查看未处理告警和待处理工单</p>
            </div>
          </div>

          <div class="queue-group">
            <div class="queue-title">
              <span>未处理告警</span>
              <el-button link type="primary" @click="goToUnhandledAlerts">全部</el-button>
            </div>
            <button
              v-for="alert in visibleUnhandledAlerts"
              :key="`alert-${alert.id}`"
              type="button"
              class="queue-item"
              @click="viewAlert(alert)"
            >
              <span class="queue-dot danger"></span>
              <span class="queue-copy">
                <strong>{{ alert.title }}</strong>
                <em>{{ alert.location }} · {{ alert.timeText }}</em>
              </span>
              <el-tag size="small" :type="alert.levelTag">{{ alert.levelText }}</el-tag>
            </button>
            <el-empty v-if="!unhandledAlerts.length" description="暂无未处理告警" :image-size="56" />
          </div>

          <div class="queue-group">
            <div class="queue-title">
              <span>待处理工单</span>
              <el-button link type="primary" @click="goToPendingOrders">全部</el-button>
            </div>
            <button
              v-for="order in visiblePendingOrders"
              :key="`order-${order.id}`"
              type="button"
              class="queue-item"
              @click="viewOrder(order)"
            >
              <span class="queue-dot warning"></span>
              <span class="queue-copy">
                <strong>{{ order.title }}</strong>
                <em>{{ order.assignee }} · {{ order.timeText }}</em>
              </span>
              <el-tag size="small" :type="getOrderStatusTag(order.status)">{{ getOrderStatusText(order.status) }}</el-tag>
            </button>
            <el-empty v-if="!pendingOrders.length" description="暂无待处理工单" :image-size="56" />
          </div>
        </section>
      </div>

      <div class="detail-grid">
        <section class="panel">
          <div class="panel-header">
            <div>
              <h2>最近告警</h2>
              <p>用于快速追踪最新异常</p>
            </div>
            <el-button link type="primary" @click="goToAllAlerts">全部告警</el-button>
          </div>
          <el-table :data="recentAlerts" height="286" stripe empty-text="当前无告警">
            <el-table-column prop="timeText" label="时间" min-width="130" />
            <el-table-column prop="location" label="位置" min-width="150" show-overflow-tooltip />
            <el-table-column prop="title" label="内容" min-width="190" show-overflow-tooltip />
            <el-table-column prop="levelText" label="级别" width="82" align="center">
              <template #default="{ row }">
                <el-tag size="small" :type="row.levelTag">{{ row.levelText }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="statusText" label="状态" width="92" align="center">
              <template #default="{ row }">
                <el-tag size="small" :type="getAlertStatusTag(row.status)">{{ row.statusText }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="84" align="center">
              <template #default="{ row }">
                <el-button size="small" link type="primary" @click="viewAlert(row)">查看</el-button>
              </template>
            </el-table-column>
          </el-table>
        </section>

        <section class="panel">
          <div class="panel-header">
            <div>
              <h2>工单跟进</h2>
              <p>待处理和流转中的工作项</p>
            </div>
            <el-button link type="primary" @click="goToAllOrders">全部工单</el-button>
          </div>
          <el-table :data="pendingOrders" height="286" stripe empty-text="当前无待办工单">
            <el-table-column prop="title" label="工单" min-width="210" show-overflow-tooltip />
            <el-table-column prop="assignee" label="负责人" width="110" />
            <el-table-column prop="priorityText" label="优先级" width="86" align="center">
              <template #default="{ row }">
                <el-tag size="small" :type="getPriorityTag(row.priority)">{{ row.priorityText }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="status" label="状态" width="92" align="center">
              <template #default="{ row }">
                <el-tag size="small" :type="getOrderStatusTag(row.status)">{{ getOrderStatusText(row.status) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="timeText" label="更新时间" width="130" />
            <el-table-column label="操作" width="84" align="center">
              <template #default="{ row }">
                <el-button size="small" link type="primary" @click="viewOrder(row)">查看</el-button>
              </template>
            </el-table-column>
          </el-table>
        </section>
      </div>

      <section class="quick-strip">
        <button v-for="action in quickActions" :key="action.label" type="button" @click="action.onClick">
          <el-icon><component :is="action.icon" /></el-icon>
          <span>{{ action.label }}</span>
        </button>
      </section>
    </div>
  </Layout>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, nextTick, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/store/auth'
import Layout from '@/components/Layout.vue'
import {
  Monitor,
  WarningFilled,
  Tickets,
  Finished,
  Refresh,
  Warning,
  Grid,
  Operation,
  Document,
  User,
  Memo,
  TrendCharts
} from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { dashboardApi } from '@/api/dashboard'
import { deviceApi } from '@/api/device'
import { alertApi } from '@/api/alert'
import { workOrderApi } from '@/api/work-order'
import * as echarts from 'echarts'

const router = useRouter()
const authStore = useAuthStore()

const loading = ref(false)
const chartLoading = ref(false)
const lastUpdated = ref('')
const selectedArea = ref('')
const chartMetric = ref('temperature')

const kpi = ref({
  onlineDevices: 0,
  totalDevices: 0,
  todayAlerts: 0,
  unhandledAlerts: 0,
  pendingWorkOrders: 0,
  todayClosedWorkOrders: 0
})
const areas = ref([])
const monitorDevices = ref([])
const recentAlerts = ref([])
const unhandledAlerts = ref([])
const pendingOrders = ref([])
const chartData = ref([])
const chartRef = ref(null)
let chartInstance = null

const isSuperAdmin = computed(() => ['ADMIN', 'SUPER_ADMIN'].includes(authStore.getUserRole))

const chartMetricText = computed(() => chartMetric.value === 'temperature' ? '温度' : '湿度')

const filteredAreas = computed(() => {
  if (!selectedArea.value) return areas.value
  return areas.value.filter(area => area.id === selectedArea.value)
})

const visibleUnhandledAlerts = computed(() => unhandledAlerts.value.slice(0, 4))
const visiblePendingOrders = computed(() => pendingOrders.value.slice(0, 4))

const kpiCards = computed(() => {
  const total = Number(kpi.value.totalDevices || 0)
  const online = Number(kpi.value.onlineDevices || 0)
  const offline = Math.max(0, total - online)
  return [
    {
      label: '在线设备',
      value: `${online}/${total}`,
      note: `离线 ${offline} 台`,
      icon: Monitor,
      tone: offline ? 'warning' : 'success',
      path: '/monitor',
      query: { online: 'true' }
    },
    {
      label: '未处理告警',
      value: kpi.value.unhandledAlerts || 0,
      note: '需要研判处理',
      icon: WarningFilled,
      tone: kpi.value.unhandledAlerts ? 'danger' : 'success',
      path: '/alerts',
      query: { status: 'unhandled' }
    },
    {
      label: '待处理工单',
      value: kpi.value.pendingWorkOrders || pendingOrders.value.length || 0,
      note: '等待派工/处理',
      icon: Tickets,
      tone: kpi.value.pendingWorkOrders ? 'warning' : 'primary',
      path: '/work-orders',
      query: { status: 'PENDING' }
    },
    {
      label: '今日闭环',
      value: kpi.value.todayClosedWorkOrders || 0,
      note: `今日告警 ${kpi.value.todayAlerts || 0}`,
      icon: Finished,
      tone: 'primary',
      path: '/work-orders',
      query: { status: 'COMPLETED', date: 'today' }
    }
  ]
})

const quickActions = computed(() => {
  const actions = [
    { label: '实时监测', icon: Monitor, onClick: () => router.push('/monitor') },
    { label: '设备管理', icon: Grid, onClick: () => router.push('/devices') },
    { label: '阈值规则', icon: Operation, onClick: () => router.push('/settings/thresholds') },
    { label: '趋势分析', icon: TrendCharts, onClick: () => router.push('/trend-analysis') },
    {
      label: 'AI 日报',
      icon: Document,
      onClick: () => router.push('/ai-assistant?prompt=生成今日冷链设备巡检与告警总结')
    }
  ]

  if (isSuperAdmin.value) {
    actions.push(
      { label: '管理员', icon: User, onClick: () => router.push('/managers') },
      { label: '审计日志', icon: Memo, onClick: () => router.push('/audit-logs') }
    )
  }
  return actions
})

const unwrapData = (response, fallback = null) => {
  const payload = response?.data
  if (!payload) return fallback
  if (payload.code === 200 || payload.success) return payload.data
  return payload.data ?? payload ?? fallback
}

const unwrapPage = (response) => {
  const payload = unwrapData(response, {})
  const records = payload.records || payload.data || payload.list || []
  return {
    records: Array.isArray(records) ? records : [],
    total: Number(payload.total || records.length || 0)
  }
}

const toNumber = (value) => {
  if (value === null || value === undefined || value === '') return null
  const next = Number(value)
  return Number.isFinite(next) ? next : null
}

const average = (values) => {
  const valid = values.map(toNumber).filter(value => value !== null)
  if (!valid.length) return null
  return valid.reduce((sum, value) => sum + value, 0) / valid.length
}

const buildAreaOverview = (devices) => {
  const groups = new Map()

  devices.forEach((device) => {
    const rawId = device.areaId ?? 'unassigned'
    const id = String(rawId)
    if (!groups.has(id)) {
      groups.set(id, {
        id,
        rawId,
        name: device.areaPath || device.areaName || '未分配库区',
        total: 0,
        online: 0,
        alarming: 0,
        temps: [],
        humis: []
      })
    }

    const area = groups.get(id)
    area.total += 1
    if (device.online || device.onlineStatus) area.online += 1
    if (device.hasUnresolvedAlert || device.alarming) area.alarming += 1
    area.temps.push(device.latestTemp ?? device.temperature)
    area.humis.push(device.latestHumi ?? device.humidity)
  })

  return Array.from(groups.values()).map((area) => {
    const offline = Math.max(0, area.total - area.online)
    const avgTemp = average(area.temps)
    const avgHumi = average(area.humis)
    const onlineRate = area.total ? Math.round((area.online / area.total) * 100) : 0
    const statusText = area.alarming ? '有告警' : offline ? '有离线' : '正常'
    const tagType = area.alarming ? 'danger' : offline ? 'warning' : 'success'

    return {
      ...area,
      offline,
      avgTemp,
      avgHumi,
      onlineRate,
      statusText,
      tagType,
      sortWeight: area.alarming ? 0 : offline ? 1 : 2
    }
  }).sort((a, b) => a.sortWeight - b.sortWeight || b.total - a.total)
}

const adaptAlert = (alert) => {
  const status = alert.status || (alert.resolved ? 'RESOLVED' : 'UNHANDLED')
  const levelValue = alert.alertLevel || alert.level || alert.severityLevel
  return {
    id: alert.id,
    title: alert.description || alert.alertType || `告警 #${alert.id}`,
    location: alert.deviceName || alert.areaName || alert.location || '未知位置',
    status,
    statusText: getAlertStatusText(status),
    levelText: getAlertLevelText(levelValue),
    levelTag: getAlertLevelTag(levelValue),
    timeText: formatDate(alert.createdAt || alert.timestamp || alert.updateTime)
  }
}

const adaptOrder = (order) => ({
  id: order.id || order.orderId,
  title: order.title || order.description || order.orderNo || `工单 #${order.id || order.orderId}`,
  assignee: order.assigneeName || order.assignee || (order.assigneeId ? `用户 ${order.assigneeId}` : '未分配'),
  status: order.status || 'PENDING',
  priority: order.priority || 'MEDIUM',
  priorityText: getPriorityText(order.priority),
  timeText: formatDate(order.updateTime || order.updatedAt || order.createdAt || order.createTime)
})

const fetchAllData = async () => {
  loading.value = true
  try {
    const [statsRes, devicesRes, alertsRes, unhandledRes, ordersRes] = await Promise.allSettled([
      dashboardApi.getStats(),
      deviceApi.getList({ page: 1, size: 200 }),
      alertApi.search({ page: 1, size: 6 }),
      alertApi.search({ status: 'UNHANDLED', page: 1, size: 5 }),
      workOrderApi.getList({ status: 'PENDING', page: 1, size: 6 })
    ])

    if (statsRes.status === 'fulfilled') {
      kpi.value = { ...kpi.value, ...(unwrapData(statsRes.value, {}) || {}) }
    }

    if (devicesRes.status === 'fulfilled') {
      const page = unwrapPage(devicesRes.value)
      monitorDevices.value = page.records
      areas.value = buildAreaOverview(page.records)
    }

    if (alertsRes.status === 'fulfilled') {
      recentAlerts.value = unwrapPage(alertsRes.value).records.map(adaptAlert)
    }

    if (unhandledRes.status === 'fulfilled') {
      const page = unwrapPage(unhandledRes.value)
      unhandledAlerts.value = page.records.map(adaptAlert)
      kpi.value.unhandledAlerts = kpi.value.unhandledAlerts || page.total
    }

    if (ordersRes.status === 'fulfilled') {
      const page = unwrapPage(ordersRes.value)
      pendingOrders.value = page.records.map(adaptOrder)
      kpi.value.pendingWorkOrders = kpi.value.pendingWorkOrders || page.total
    }

    await fetchChartData()
    lastUpdated.value = new Date().toLocaleString('zh-CN')
  } catch (error) {
    console.error('获取仪表盘数据失败:', error)
    ElMessage.error('无法加载仪表盘数据，请检查后端服务')
  } finally {
    loading.value = false
  }
}

const fetchChartData = async () => {
  chartLoading.value = true
  try {
    const response = await dashboardApi.getEnvironmentTrend({ interval: 'daily' })
    const trend = unwrapData(response, {})
    chartData.value = Array.isArray(trend?.data) ? trend.data : []
    await renderTrendChart()
  } catch (error) {
    console.warn('暂无环境趋势数据:', error)
    chartData.value = []
    await renderTrendChart()
  } finally {
    chartLoading.value = false
  }
}

const renderTrendChart = async () => {
  await nextTick()
  if (!chartRef.value) return

  if (!chartData.value.length) {
    chartInstance?.dispose()
    chartInstance = null
    return
  }

  if (!chartInstance) {
    chartInstance = echarts.init(chartRef.value)
    window.addEventListener('resize', resizeChart)
  }

  const valueKeys = chartMetric.value === 'temperature'
    ? ['temperature', 'avgTemperature', 'avgTemp', 'temp']
    : ['humidity', 'avgHumidity', 'avgHumi', 'humi']

  const xAxis = chartData.value.map(item => item.date || item.time || item.period || item.label)
  const values = chartData.value.map((item) => {
    const key = valueKeys.find(name => item[name] !== undefined && item[name] !== null)
    return key ? Number(item[key]) : 0
  })

  chartInstance.setOption({
    tooltip: { trigger: 'axis' },
    color: [chartMetric.value === 'temperature' ? '#ef4444' : '#2563eb'],
    grid: { top: 28, right: 16, bottom: 32, left: 46 },
    xAxis: {
      type: 'category',
      data: xAxis,
      boundaryGap: false,
      axisTick: { show: false },
      axisLine: { lineStyle: { color: '#d1d5db' } }
    },
    yAxis: {
      type: 'value',
      name: chartMetric.value === 'temperature' ? '°C' : '%',
      splitLine: { lineStyle: { color: '#eef2f7' } }
    },
    series: [
      {
        name: chartMetricText.value,
        type: 'line',
        smooth: true,
        showSymbol: false,
        data: values,
        areaStyle: { opacity: 0.12 },
        lineStyle: { width: 3 }
      }
    ]
  })
}

const resizeChart = () => chartInstance?.resize()

watch(chartMetric, fetchChartData)

const go = (path, query = {}) => router.push({ path, query })
const goToUnhandledAlerts = () => go('/alerts', { status: 'unhandled' })
const goToPendingOrders = () => go('/work-orders', { status: 'PENDING' })
const goToAllAlerts = () => router.push('/alerts')
const goToAllOrders = () => router.push('/work-orders')
const viewAlert = (alert) => alert.id && router.push(`/alerts/${alert.id}`)
const viewOrder = (order) => order.id && router.push(`/work-orders/${order.id}`)
const goToAreaDetail = (area) => {
  if (area.rawId === 'unassigned') return
  router.push({ path: '/monitor', query: { areaId: area.rawId } })
}

const formatMetric = (value, unit) => {
  if (value === null || value === undefined) return '-'
  return `${Number(value).toFixed(1)}${unit}`
}

const formatDate = (value) => {
  if (!value) return '-'
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return '-'
  const now = new Date()
  const diff = Math.floor((now - date) / 1000)
  if (diff >= 0 && diff < 60) return '刚刚'
  if (diff >= 0 && diff < 3600) return `${Math.floor(diff / 60)}分钟前`
  if (diff >= 0 && diff < 86400) return `${Math.floor(diff / 3600)}小时前`
  return date.toLocaleDateString('zh-CN')
}

const getAlertLevelText = (level) => {
  const normalized = normalizeLevel(level)
  const map = { CRITICAL: '紧急', HIGH: '高', MEDIUM: '中', LOW: '低' }
  return map[normalized] || '未知'
}

const getAlertLevelTag = (level) => {
  const normalized = normalizeLevel(level)
  if (normalized === 'CRITICAL' || normalized === 'HIGH') return 'danger'
  if (normalized === 'MEDIUM') return 'warning'
  return 'info'
}

const normalizeLevel = (level) => {
  if (typeof level === 'number') {
    if (level >= 4) return 'CRITICAL'
    if (level === 3) return 'HIGH'
    if (level === 2) return 'MEDIUM'
    return 'LOW'
  }
  return String(level || '').toUpperCase()
}

const getAlertStatusText = (status) => {
  const map = {
    UNHANDLED: '未处理',
    HANDLING: '处理中',
    RESOLVED: '已解决',
    IGNORED: '已忽略'
  }
  return map[status] || status || '未知'
}

const getAlertStatusTag = (status) => {
  if (status === 'UNHANDLED') return 'danger'
  if (status === 'HANDLING') return 'warning'
  if (status === 'RESOLVED') return 'success'
  return 'info'
}

const getOrderStatusText = (status) => {
  const map = {
    PENDING: '待处理',
    PROCESSING: '处理中',
    VERIFYING: '待验收',
    COMPLETED: '已完成',
    CLOSED: '已关闭'
  }
  return map[status] || status || '未知'
}

const getOrderStatusTag = (status) => {
  if (status === 'PENDING') return 'warning'
  if (status === 'PROCESSING' || status === 'VERIFYING') return 'primary'
  if (status === 'COMPLETED') return 'success'
  return 'info'
}

const getPriorityText = (priority) => {
  const map = { URGENT: '紧急', HIGH: '高', MEDIUM: '中', LOW: '低' }
  return map[priority] || '普通'
}

const getPriorityTag = (priority) => {
  if (priority === 'URGENT') return 'danger'
  if (priority === 'HIGH') return 'warning'
  if (priority === 'LOW') return 'success'
  return 'info'
}

onMounted(fetchAllData)

onUnmounted(() => {
  window.removeEventListener('resize', resizeChart)
  chartInstance?.dispose()
  chartInstance = null
})
</script>

<style scoped>
.dashboard-page {
  min-height: 100%;
  padding: 20px 24px 28px;
  background: #f6f8fb;
  color: #1f2937;
  box-sizing: border-box;
}

.dashboard-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 18px;
}

.dashboard-header h1 {
  margin: 0;
  font-size: 22px;
  font-weight: 700;
}

.dashboard-header p,
.panel-header p {
  margin: 5px 0 0;
  color: #6b7280;
  font-size: 13px;
}

.header-actions {
  display: flex;
  gap: 10px;
}

.kpi-strip {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 14px;
  margin-bottom: 16px;
}

.kpi-card {
  display: flex;
  align-items: center;
  gap: 13px;
  min-height: 104px;
  padding: 16px;
  border: 1px solid #e5e7eb;
  border-left-width: 4px;
  border-radius: 8px;
  background: #fff;
  text-align: left;
  cursor: pointer;
  transition: border-color 0.18s, box-shadow 0.18s, transform 0.18s;
}

.kpi-card:hover {
  transform: translateY(-1px);
  box-shadow: 0 10px 24px rgba(15, 23, 42, 0.08);
}

.kpi-icon {
  width: 42px;
  height: 42px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border-radius: 8px;
  background: #f3f6fb;
  color: #2563eb;
  font-size: 22px;
  flex-shrink: 0;
}

.kpi-copy {
  display: flex;
  flex-direction: column;
  min-width: 0;
}

.kpi-label,
.kpi-note {
  color: #6b7280;
  font-size: 13px;
}

.kpi-value {
  margin: 5px 0 2px;
  font-size: 26px;
  line-height: 1;
  font-weight: 750;
  color: #111827;
}

.tone-danger {
  border-left-color: #ef4444;
}

.tone-warning {
  border-left-color: #f59e0b;
}

.tone-success {
  border-left-color: #10b981;
}

.tone-primary {
  border-left-color: #2563eb;
}

.operations-grid {
  display: grid;
  grid-template-columns: minmax(360px, 1.15fr) minmax(320px, 0.95fr) minmax(330px, 0.9fr);
  gap: 16px;
  margin-bottom: 16px;
}

.detail-grid {
  display: grid;
  grid-template-columns: minmax(0, 1fr) minmax(0, 1fr);
  gap: 16px;
  margin-bottom: 16px;
}

.panel {
  min-width: 0;
  padding: 16px;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  background: #fff;
  box-shadow: 0 1px 2px rgba(15, 23, 42, 0.03);
}

.panel-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 12px;
  margin-bottom: 14px;
}

.panel-header h2 {
  margin: 0;
  font-size: 16px;
  font-weight: 700;
}

.compact-select {
  width: 132px;
  flex-shrink: 0;
}

.area-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
  max-height: 310px;
  overflow: auto;
}

.area-row {
  display: grid;
  grid-template-columns: minmax(0, 1fr) auto;
  gap: 10px 12px;
  width: 100%;
  padding: 12px;
  border: 1px solid #edf0f5;
  border-radius: 8px;
  background: #fbfcfe;
  text-align: left;
  cursor: pointer;
}

.area-row:hover {
  border-color: #93c5fd;
  background: #f8fbff;
}

.area-main,
.area-title,
.queue-copy {
  min-width: 0;
}

.area-main,
.queue-copy {
  display: flex;
  flex-direction: column;
}

.area-title {
  display: flex;
  align-items: center;
  gap: 8px;
}

.area-title strong,
.queue-copy strong {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.area-subtitle,
.queue-copy em {
  margin-top: 5px;
  color: #6b7280;
  font-size: 12px;
  font-style: normal;
}

.area-metrics {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #111827;
  font-weight: 650;
  white-space: nowrap;
}

.online-bar {
  grid-column: 1 / -1;
  height: 5px;
  overflow: hidden;
  border-radius: 999px;
  background: #e5e7eb;
}

.online-bar span {
  display: block;
  height: 100%;
  border-radius: inherit;
  background: #10b981;
}

.chart-wrap {
  height: 260px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.trend-chart {
  width: 100%;
  height: 100%;
}

.queue-panel {
  display: flex;
  flex-direction: column;
}

.queue-group + .queue-group {
  margin-top: 14px;
  padding-top: 14px;
  border-top: 1px solid #edf0f5;
}

.queue-title {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 8px;
  color: #374151;
  font-size: 13px;
  font-weight: 700;
}

.queue-item {
  width: 100%;
  display: grid;
  grid-template-columns: 8px minmax(0, 1fr) auto;
  align-items: center;
  gap: 10px;
  padding: 8px 4px;
  border: 0;
  border-radius: 6px;
  background: transparent;
  text-align: left;
  cursor: pointer;
}

.queue-item:hover {
  background: #f6f8fb;
}

.queue-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
}

.queue-dot.danger {
  background: #ef4444;
}

.queue-dot.warning {
  background: #f59e0b;
}

.quick-strip {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  padding: 12px;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  background: #fff;
}

.quick-strip button {
  display: inline-flex;
  align-items: center;
  gap: 7px;
  height: 34px;
  padding: 0 12px;
  border: 1px solid #e5e7eb;
  border-radius: 6px;
  background: #fff;
  color: #374151;
  cursor: pointer;
}

.quick-strip button:hover {
  border-color: #93c5fd;
  color: #2563eb;
}

:deep(.el-table th.el-table__cell) {
  background: #f8fafc;
  color: #4b5563;
  font-weight: 650;
}

:deep(.el-table .cell) {
  line-height: 1.35;
}

@media (max-width: 1280px) {
  .operations-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 1500px) {
  .detail-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 900px) {
  .dashboard-header {
    flex-direction: column;
  }

  .kpi-strip {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 620px) {
  .dashboard-page {
    padding: 16px;
  }

  .kpi-strip {
    grid-template-columns: 1fr;
  }

  .panel-header,
  .header-actions {
    width: 100%;
    flex-direction: column;
  }

  .compact-select {
    width: 100%;
  }
}
</style>
