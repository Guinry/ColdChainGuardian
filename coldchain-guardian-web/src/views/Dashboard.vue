<template>
  <Layout>
    <div class="dashboard-content" v-loading="loading" element-loading-text="正在加载大盘数据...">
      <!-- KPI Cards Section -->
      <div class="kpi-section">
        <div class="kpi-card" @click="goToOnlineDevices">
          <div class="kpi-icon">
            <el-icon><Link /></el-icon>
          </div>
          <div class="kpi-content">
            <div class="kpi-value">{{ kpi.onlineDevices }}/{{ kpi.totalDevices }}</div>
            <div class="kpi-label">在线设备数</div>
          </div>
        </div>

        <div class="kpi-card" @click="goToTodayAlerts">
          <div class="kpi-icon">
            <el-icon><WarningFilled /></el-icon>
          </div>
          <div class="kpi-content">
            <div class="kpi-value">{{ kpi.todayAlerts }}</div>
            <div class="kpi-label">今日告警数</div>
          </div>
        </div>

        <div class="kpi-card" @click="goToUnhandledAlerts">
          <div class="kpi-icon">
            <el-icon><CircleCloseFilled /></el-icon>
          </div>
          <div class="kpi-content">
            <div class="kpi-value">{{ kpi.unhandledAlerts }}</div>
            <div class="kpi-label">未处理告警</div>
          </div>
        </div>

        <div class="kpi-card" @click="goToTodayClosedOrders">
          <div class="kpi-icon">
            <el-icon><Finished /></el-icon>
          </div>
          <div class="kpi-content">
            <div class="kpi-value">{{ kpi.todayClosedWorkOrders }}</div>
            <div class="kpi-label">今日闭环工单</div>
          </div>
        </div>
      </div>

      <div class="overview-section">
        <div class="section-header">
          <h2>实时监测概览</h2>
          <div class="filters">
            <el-select v-model="selectedArea" placeholder="全部库区" class="area-filter" clearable>
              <el-option label="全部库区" value="" />
              <el-option v-for="area in areas" :key="area.id" :label="area.name" :value="area.id" />
            </el-select>
            <el-select v-model="timeWindow" placeholder="实时" class="time-filter" @change="fetchAreaOverview">
              <el-option label="实时" value="realtime" />
              <el-option label="近1小时" value="1h" />
              <el-option label="近12小时" value="12h" />
              <el-option label="近24小时" value="24h" />
            </el-select>
          </div>
        </div>

        <div class="overview-grid">
          <el-empty v-if="!filteredAreas.length" description="暂无库区数据" />
          <div
            v-else
            v-for="area in filteredAreas"
            :key="area.id"
            class="area-card"
            :class="{ 'area-error': area.status === 'error' }"
            @click="goToAreaDetail(area.id)"
          >
            <div class="area-header">
              <h3>{{ area.name }}</h3>
              <div class="area-status" :class="area.status">{{ area.statusText }}</div>
            </div>
            <div class="area-stats">
              <div class="stat">
                <span class="label">温度</span>
                <span class="value">{{ area.temperature }}°C</span>
              </div>
              <div class="stat">
                <span class="label">湿度</span>
                <span class="value">{{ area.humidity }}%</span>
              </div>
            </div>
            <div class="area-devices">
              <span class="device-count">设备: {{ area.onlineDevices }}/{{ area.totalDevices }}</span>
            </div>
          </div>
        </div>
      </div>

      <div class="chart-section" v-loading="chartLoading">
        <div class="section-header">
          <h2>趋势图</h2>
          <div class="chart-filters">
            <el-select v-model="selectedChartArea" placeholder="选择库区" class="chart-filter" clearable>
              <el-option v-for="area in areas" :key="area.id" :label="area.name" :value="area.id" />
            </el-select>
            <el-select v-model="selectedDevice" placeholder="选择设备" class="chart-filter" clearable>
              <el-option v-for="device in availableDevicesForChart" :key="device.id" :label="device.name" :value="device.id" />
            </el-select>
            <el-select v-model="chartMetric" placeholder="指标" class="chart-filter">
              <el-option label="温度" value="temperature" />
              <el-option label="湿度" value="humidity" />
            </el-select>
          </div>
        </div>
        <div class="chart-container">
          <el-empty v-if="!chartData.length" description="暂无数据" />
          <div v-else class="chart-placeholder">
            <div class="chart-title">温湿度趋势图</div>
            <div class="chart-subtitle">{{ selectedAreaName }} - {{ chartMetricText }}</div>
            <div class="chart-description">时间 x {{ chartMetricText }}，阈值线已标记 (共 {{ chartData.length }} 条数据)</div>
          </div>
        </div>
      </div>

      <div class="alerts-section">
        <div class="section-header">
          <h2>最近告警</h2>
          <el-button type="primary" link @click="goToAllAlerts" class="view-all-btn">全部告警 →</el-button>
        </div>
        <div class="table-container">
          <el-table :data="recentAlerts" stripe style="width: 100%" height="300" empty-text="当前无告警">
            <el-table-column prop="timestamp" label="时间" min-width="140" align="center" header-align="center" />
            <el-table-column prop="area" label="库区" min-width="100" align="center" header-align="center" />
            <el-table-column prop="device" label="设备" min-width="130" align="center" header-align="center" />
            <el-table-column prop="type" label="类型" min-width="100" align="center" header-align="center">
              <template #default="{ row }">
                <el-tag :type="getAlertTypeTag(row.type)">{{ row.type }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="level" label="级别" min-width="90" align="center" header-align="center">
              <template #default="{ row }">
                <el-tag :type="getAlertLevelTag(row.level)">{{ row.level }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="status" label="状态" min-width="90" align="center" header-align="center">
              <template #default="{ row }">
                <el-tag :type="getAlertStatusTag(row.status)">{{ row.status }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" min-width="150" align="center" header-align="center">
              <template #default="{ row }">
                <el-button size="small" @click="viewAlert(row)">查看</el-button>
                <el-button size="small" type="primary" @click="createOrder(row)" :disabled="row.status !== '未处理'">派单</el-button>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </div>

      <div class="orders-section">
        <div class="section-header">
          <h2>待处理工单</h2>
          <el-button type="primary" link @click="goToAllOrders" class="view-all-btn">全部工单 →</el-button>
        </div>
        <div class="table-container">
          <el-table :data="pendingOrders" stripe style="width: 100%" height="300" empty-text="当前无待办工单">
            <el-table-column prop="orderId" label="编号" min-width="150" align="center" header-align="center" />
            <el-table-column prop="alert" label="告警内容" min-width="130" align="center" header-align="center" />
            <el-table-column prop="assignee" label="指派人" min-width="100" align="center" header-align="center" />
            <el-table-column prop="status" label="状态" min-width="90" align="center" header-align="center">
              <template #default="{ row }">
                <el-tag :type="getOrderStatusTag(row.status)">{{ row.status }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="updatedAt" label="更新时间" min-width="140" align="center" header-align="center" />
            <el-table-column label="操作" min-width="150" align="center" header-align="center">
              <template #default="{ row }">
                <el-button size="small" @click="viewOrder(row)">查看</el-button>
                <el-button size="small" type="success" @click="completeOrder(row)" :disabled="row.status === '已完成'">验收</el-button>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </div>

      <!-- Quick Actions Section -->
      <div class="quick-actions-section">
        <div class="section-header">
          <h2>快捷入口</h2>
        </div>
        <div class="quick-actions-grid">
          <div class="quick-action-card" @click="goToDeviceManagement">
            <div class="action-icon">
              <el-icon><Grid /></el-icon>
            </div>
            <div class="action-text">设备管理</div>
          </div>

          <div class="quick-action-card" @click="goToThresholdSettings">
            <div class="action-icon">
              <el-icon><Operation /></el-icon>
            </div>
            <div class="action-text">阈值规则</div>
          </div>

          <div class="quick-action-card" @click="goToAlertCenter">
            <div class="action-icon">
              <el-icon><Warning /></el-icon>
            </div>
            <div class="action-text">告警中心</div>
          </div>

          <div class="quick-action-card" @click="generateDailyReport">
            <div class="action-icon">
              <el-icon><Document /></el-icon>
            </div>
            <div class="action-text">AI日报生成</div>
          </div>

          <!-- Super Admin only actions -->
          <div v-if="isSuperAdmin" class="quick-action-card" @click="goToUserManagement">
            <div class="action-icon">
              <el-icon><User /></el-icon>
            </div>
            <div class="action-text">管理员管理</div>
          </div>

          <div v-if="isSuperAdmin" class="quick-action-card" @click="goToPermissionManagement">
            <div class="action-icon">
              <el-icon><Tickets /></el-icon>
            </div>
            <div class="action-text">权限分配</div>
          </div>

          <div v-if="isSuperAdmin" class="quick-action-card" @click="goToAuditLogs">
            <div class="action-icon">
              <el-icon><Memo /></el-icon>
            </div>
            <div class="action-text">审计日志</div>
          </div>
        </div>
      </div>
    </div>
  </Layout>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/store/auth'
import Layout from '@/components/Layout.vue'
import {
  Link, WarningFilled, CircleCloseFilled, Finished,
  Grid, Operation, Warning, Document,
  User, Tickets, Memo
} from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { dashboardApi } from '@/api/dashboard' // 导入我们刚才建好的 API

const router = useRouter()
const authStore = useAuthStore()

// 状态控制
const loading = ref(false)
const chartLoading = ref(false)
const isSuperAdmin = computed(() => authStore.getUserRole === 'SUPER_ADMIN')

// 过滤参数
const selectedArea = ref('')
const timeWindow = ref('realtime')
const selectedChartArea = ref('')
const selectedDevice = ref('')
const chartMetric = ref('temperature')

// 核心数据模型 (清空原本的 mock 数据，赋初值)
const kpi = ref({
  onlineDevices: 0,
  totalDevices: 0,
  todayAlerts: 0,
  unhandledAlerts: 0,
  todayClosedWorkOrders: 0
})
const areas = ref([])
const devices = ref([])
const recentAlerts = ref([])
const pendingOrders = ref([])
const chartData = ref([])

// 计算属性
const filteredAreas = computed(() => {
  if (!selectedArea.value) return areas.value
  return areas.value.filter(area => area.id === selectedArea.value)
})

// 根据选择的库区动态过滤图表设备下拉框
const availableDevicesForChart = computed(() => {
  if (!selectedChartArea.value) return devices.value
  return devices.value.filter(d => d.areaId === selectedChartArea.value)
})

const selectedAreaName = computed(() => {
  if (!selectedChartArea.value) return '全部库区'
  const area = areas.value.find(a => a.id === selectedChartArea.value)
  return area ? area.name : '未知库区'
})

const chartMetricText = computed(() => {
  return chartMetric.value === 'temperature' ? '温度' : '湿度'
})

// === API 数据获取逻辑 ===

// 1. 初始化所有基础数据 (并行加载提升速度)
const fetchAllData = async () => {
  loading.value = true
  try {
    // 使用 Promise.all 并发请求
    const [kpiRes, areasRes, devicesRes, alertsRes, ordersRes] = await Promise.all([
      dashboardApi.getStats(),
      dashboardApi.getAreaOverview(timeWindow.value),
      dashboardApi.getDevices(),
      dashboardApi.getRecentAlerts(5),
      dashboardApi.getPendingOrders(5)
    ])

    // 数据映射赋值 (处理标准响应格式: {code, message, data})
    if (kpiRes.data && kpiRes.data.code === 200) {
      kpi.value = kpiRes.data.data || kpiRes.data
    } else {
      // 如果没有标准格式，直接使用返回的数据
      kpi.value = kpiRes.data
    }

    if (areasRes.data && areasRes.data.code === 200) {
      areas.value = areasRes.data.data || []
    } else {
      areas.value = areasRes.data || []
    }

    if (devicesRes.data && devicesRes.data.code === 200) {
      devices.value = devicesRes.data.data || []
    } else {
      devices.value = devicesRes.data || []
    }

    // 映射告警数据 (适配前端 table prop)
    if (alertsRes.data && alertsRes.data.code === 200) {
      const rawAlerts = alertsRes.data.data || []
      recentAlerts.value = rawAlerts.map(a => ({
        id: a.id,
        timestamp: a.timestamp,
        area: a.area,
        device: a.device,
        type: a.type,
        level: a.level,
        status: a.status
      }))
    } else {
      recentAlerts.value = []
    }

    // 映射工单数据
    if (ordersRes.data && ordersRes.data.code === 200) {
      const rawOrders = ordersRes.data.data || []
      pendingOrders.value = rawOrders.map(o => ({
        orderId: o.orderId,
        alert: o.alert,
        assignee: o.assignee,
        status: o.status,
        updatedAt: o.updatedAt
      }))
    } else {
      pendingOrders.value = []
    }

    // 初始加载一次图表数据
    await fetchChartData()
  } catch (error) {
    console.error('获取大盘数据失败:', error)
    ElMessage.error('无法加载仪表盘数据，请检查网络或联系管理员')
  } finally {
    loading.value = false
  }
}

// 2. 单独刷新库区概览
const fetchAreaOverview = async () => {
  try {
    const res = await dashboardApi.getAreaOverview(timeWindow.value)
    if (res.data && res.data.code === 200) {
      areas.value = res.data.data || []
    } else {
      areas.value = res.data || []
    }
  } catch (error) {
    ElMessage.error('刷新库区状态失败')
  }
}

// 3. 单独刷新图表数据
const fetchChartData = async () => {
  chartLoading.value = true
  try {
    // 实际的图表API还未实现，这里暂时使用mock数据
    // 在实际环境中，这里会调用dashboardApi.getEnvironmentTrend
    chartData.value = [10, 20, 15, 30, 25, 40, 35]
  } catch (error) {
    console.warn('暂无图表趋势数据', error)
    chartData.value = []
  } finally {
    chartLoading.value = false
  }
}

// 监听图表条件变化，自动拉取新数据
watch([selectedChartArea, selectedDevice, chartMetric], () => {
  fetchChartData()
})

// === 路由与交互操作 ===
const goToOnlineDevices = () => router.push('/devices?status=online')
const goToTodayAlerts = () => router.push('/alerts?date=today')
const goToUnhandledAlerts = () => router.push('/alerts?status=unhandled')
const goToTodayClosedOrders = () => router.push('/orders?date=today&status=closed')
const goToAllAlerts = () => router.push('/alerts')
const viewAlert = (alert) => router.push(`/alerts/${alert.id}`)
const createOrder = (alert) => router.push(`/orders/create?alertId=${alert.id}`)
const goToAllOrders = () => router.push('/orders')
const viewOrder = (order) => router.push(`/orders/${order.orderId}`)
const goToAreaDetail = (areaId) => router.push(`/monitoring/${areaId}`)

const goToDeviceManagement = () => router.push('/devices')
const goToThresholdSettings = () => router.push('/settings/thresholds')
const goToAlertCenter = () => router.push('/alerts')
const generateDailyReport = () => {
  ElMessage.info('正在请求AI生成日报...')
  router.push('/ai-assistant?prompt=生成今日冷链设备巡检与告警总结')
}

const goToUserManagement = () => router.push('/admin/users')
const goToPermissionManagement = () => router.push('/admin/permissions')
const goToAuditLogs = () => router.push('/admin/logs')

const completeOrder = (order) => {
  ElMessage.success(`正在跳转验收: 工单 ${order.orderId}`)
  router.push(`/orders/${order.orderId}?action=complete`)
}

// === 样式映射 Helper ===
const getAlertTypeTag = (type) => {
  if (!type) return 'info'
  if (type.includes('温度') || type.includes('超限')) return 'danger'
  if (type.includes('湿度')) return 'warning'
  if (type.includes('离线') || type.includes('断电')) return 'info'
  return 'info'
}

const getAlertLevelTag = (level) => {
  if (level === '高' || level === 'CRITICAL' || level === 'HIGH') return 'danger'
  if (level === '中' || level === 'MEDIUM') return 'warning'
  return 'info'
}

const getAlertStatusTag = (status) => {
  if (status === '未处理' || status === 'UNHANDLED') return 'danger'
  if (status === '处理中' || status === 'HANDLING') return 'warning'
  return 'success'
}

const getOrderStatusTag = (status) => {
  if (status === '待处理' || status === 'PENDING') return 'warning'
  if (status === '处理中' || status === 'PROCESSING') return 'primary'
  return 'success'
}

// 生命周期挂载
onMounted(() => {
  fetchAllData()
})
</script>

<style scoped>
.dashboard-content {
  padding: 20px;
  height: 100%;
  min-height: 0;
}

.kpi-section {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
  margin-bottom: 24px;
}

.kpi-card {
  display: flex;
  align-items: center;
  padding: 20px;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  cursor: pointer;
  transition: transform 0.2s, box-shadow 0.2s;
}

.kpi-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.kpi-icon {
  width: 48px;
  height: 48px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #e6f7ff;
  border-radius: 50%;
  margin-right: 16px;
  color: #409eff;
}

.kpi-content {
  flex: 1;
}

.kpi-value {
  font-size: 24px;
  font-weight: bold;
  color: #303133;
}

.kpi-label {
  font-size: 14px;
  color: #909399;
  margin-top: 4px;
}

.overview-section {
  background: white;
  border-radius: 8px;
  padding: 20px;
  margin-bottom: 24px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.section-header h2 {
  margin: 0;
  font-size: 18px;
  color: #303133;
}

.filters {
  display: flex;
  gap: 12px;
}

.area-filter, .time-filter {
  width: 150px;
}

.overview-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(250px, 1fr));
  gap: 16px;
}

.area-card {
  border: 1px solid #dcdfe6;
  border-radius: 8px;
  padding: 16px;
  cursor: pointer;
  transition: all 0.2s;
}

.area-card:hover {
  border-color: #409eff;
  box-shadow: 0 2px 8px rgba(64, 158, 255, 0.2);
}

.area-card.area-error {
  border-color: #f56c6c;
}

.area-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.area-header h3 {
  margin: 0;
  font-size: 16px;
  color: #303133;
}

.area-status {
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: bold;
}

.area-status.normal {
  background-color: #e6f7ff;
  color: #13c2c2;
}

.area-status.warning {
  background-color: #fff7e6;
  color: #fa8c16;
}

.area-status.error {
  background-color: #fff1f0;
  color: #f5222d;
}

.area-stats {
  display: flex;
  justify-content: space-between;
  margin-bottom: 8px;
}

.stat {
  display: flex;
  flex-direction: column;
}

.label {
  font-size: 12px;
  color: #909399;
}

.value {
  font-size: 16px;
  font-weight: bold;
  color: #303133;
}

.area-devices {
  font-size: 12px;
  color: #909399;
}

.chart-section {
  background: white;
  border-radius: 8px;
  padding: 20px;
  margin-bottom: 24px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.chart-filters {
  display: flex;
  gap: 12px;
}

.chart-filter {
  width: 150px;
}

.chart-container {
  margin-top: 16px;
  min-height: 300px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.chart-placeholder {
  text-align: center;
  color: #909399;
}

.chart-title {
  font-size: 18px;
  margin-bottom: 8px;
}

.chart-subtitle {
  font-size: 16px;
  margin-bottom: 4px;
}

.chart-description {
  font-size: 14px;
}

.alerts-section, .orders-section {
  background: white;
  border-radius: 8px;
  padding: 20px;
  margin-bottom: 24px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.view-all-btn {
  border: none;
}

.table-container {
  margin-top: 16px;
  overflow-x: auto;
  max-height: 300px;
}

/* Table styles for better alignment and font sizing */
:deep(.el-table th),
:deep(.el-table td) {
  text-align: center !important;
  font-size: 15px;
}

:deep(.el-table th) {
  font-size: 16px;
  font-weight: 600;
}

.quick-actions-section {
  background: white;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.quick-actions-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(140px, 1fr)); /* Distribute evenly based on content */
  gap: 24px; /* Increase gap for better spacing */
  margin-top: 16px;
}

.quick-action-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 24px 16px; /* Increase padding for better visual appearance */
  border: 1px solid #dcdfe6;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s;
  min-height: 120px; /* Ensure consistent height */
  text-align: center; /* Center the text */
}

.quick-action-card:hover {
  border-color: #409eff;
  box-shadow: 0 2px 8px rgba(64, 158, 255, 0.2);
}

.action-icon {
  width: 48px;
  height: 48px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #e6f7ff;
  border-radius: 50%;
  margin-bottom: 8px;
  color: #409eff;
}

.action-text {
  font-size: 14px;
  color: #606266;
}

/* Responsive design */
@media (max-width: 1200px) {
  .kpi-section {
    grid-template-columns: repeat(2, 1fr);
  }

  .overview-grid {
    grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  }
}

@media (max-width: 768px) {
  .kpi-section {
    grid-template-columns: 1fr;
  }

  .quick-actions-grid {
    grid-template-columns: repeat(auto-fill, minmax(120px, 1fr));
  }
}
</style>