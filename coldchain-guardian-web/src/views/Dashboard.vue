<template>
  <div class="dashboard-container">
    <!-- Top Navigation Bar -->
    <div class="top-bar">
      <div class="logo-section">
        <div class="logo">
          <svg viewBox="0 0 24 24" width="32" height="32" fill="none" xmlns="http://www.w3.org/2000/svg">
            <path d="M12 2L13.09 8.26L22 9L13.09 9.74L12 16L10.91 9.74L2 9L10.91 8.26L12 2Z" fill="#409EFF"/>
            <circle cx="12" cy="12" r="10" stroke="#409EFF" stroke-width="2"/>
          </svg>
        </div>
        <span class="app-title">ColdChain Guardian</span>
      </div>

      <div class="search-section">
        <el-input
          v-model="globalSearch"
          placeholder="全局搜索..."
          prefix-icon="Search"
          class="global-search"
        />
      </div>

      <div class="action-section">
        <el-badge :value="unreadNotifications" class="notification-badge">
          <el-button circle class="notification-btn">
            <el-icon><Bell /></el-icon>
          </el-button>
        </el-badge>

        <el-dropdown>
          <div class="user-avatar">
            <el-avatar :size="32" :src="userAvatar">{{ userInitial }}</el-avatar>
            <span class="user-name">{{ userInfo.realName }}</span>
            <el-icon><ArrowDown /></el-icon>
          </div>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item @click="viewProfile">个人资料</el-dropdown-item>
              <el-dropdown-item @click="settings">系统设置</el-dropdown-item>
              <el-dropdown-item divided @click="logout">退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </div>

    <div class="dashboard-layout">
      <!-- Side Menu -->
      <div class="side-menu">
        <el-menu
          :default-active="activeMenu"
          class="menu"
          :collapse="false"
          :unique-opened="true"
          :router="true"
        >
          <el-menu-item index="/dashboard">
            <el-icon><House /></el-icon>
            <span>Dashboard</span>
          </el-menu-item>

          <el-sub-menu index="monitoring">
            <template #title>
              <el-icon><Monitor /></el-icon>
              <span>监测管理</span>
            </template>
            <el-menu-item index="/warehouse-area">库区管理</el-menu-item>
            <el-menu-item index="/devices">设备管理</el-menu-item>
            <el-menu-item index="/monitoring/realtime">实时监测</el-menu-item>
          </el-sub-menu>

          <el-sub-menu index="alerts-orders">
            <template #title>
              <el-icon><Warning /></el-icon>
              <span>告警与工单</span>
            </template>
            <el-menu-item index="/alerts">告警中心</el-menu-item>
            <el-menu-item index="/orders">工单管理</el-menu-item>
          </el-sub-menu>

          <el-sub-menu index="analysis">
            <template #title>
              <el-icon><DataAnalysis /></el-icon>
              <span>数据分析</span>
            </template>
            <el-menu-item index="/analysis/trends">趋势分析</el-menu-item>
            <el-menu-item index="/analysis/ai">AI 智能助手</el-menu-item>
          </el-sub-menu>

          <!-- System Management menu only visible for SUPER_ADMIN -->
          <el-sub-menu v-if="isSuperAdmin" index="system">
            <template #title>
              <el-icon><Setting /></el-icon>
              <span>系统管理（超管）</span>
            </template>
            <el-menu-item index="/admin/users">管理员管理</el-menu-item>
            <el-menu-item index="/admin/permissions">权限分配</el-menu-item>
          </el-sub-menu>
        </el-menu>
      </div>

      <!-- Main Content -->
      <div class="main-content">
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

        <!-- Realtime Overview Section -->
        <div class="overview-section">
          <div class="section-header">
            <h2>实时监测概览</h2>
            <div class="filters">
              <el-select v-model="selectedArea" placeholder="全部库区" class="area-filter">
                <el-option label="全部库区" value="" />
                <el-option v-for="area in areas" :key="area.id" :label="area.name" :value="area.id" />
              </el-select>
              <el-select v-model="timeWindow" placeholder="实时" class="time-filter">
                <el-option label="实时" value="realtime" />
                <el-option label="近1小时" value="1h" />
                <el-option label="近12小时" value="12h" />
                <el-option label="近24小时" value="24h" />
              </el-select>
            </div>
          </div>

          <div class="overview-grid">
            <div
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

        <!-- Trend Chart Section -->
        <div class="chart-section">
          <div class="section-header">
            <h2>趋势图</h2>
            <div class="chart-filters">
              <el-select v-model="selectedChartArea" placeholder="选择库区" class="chart-filter">
                <el-option v-for="area in areas" :key="area.id" :label="area.name" :value="area.id" />
              </el-select>
              <el-select v-model="selectedDevice" placeholder="选择设备" class="chart-filter">
                <el-option v-for="device in devices" :key="device.id" :label="device.name" :value="device.id" />
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
              <!-- Placeholder for chart - would integrate with ECharts or similar -->
              <div class="chart-title">温湿度趋势图</div>
              <div class="chart-subtitle">{{ selectedAreaName }} - {{ chartMetricText }}</div>
              <div class="chart-description">时间 x {{ chartMetricText }}，阈值线已标记</div>
            </div>
          </div>
        </div>

        <!-- Recent Alerts Section -->
        <div class="alerts-section">
          <div class="section-header">
            <h2>最近告警</h2>
            <el-button type="primary" @click="goToAllAlerts" class="view-all-btn">全部告警 →</el-button>
          </div>
          <div class="table-container">
            <el-table :data="recentAlerts" stripe style="width: 100%" height="300">
              <el-table-column prop="timestamp" label="时间" min-width="140" align="center" header-align="center" />
              <el-table-column prop="area" label="库区" min-width="100" align="center" header-align="center" />
              <el-table-column prop="device" label="设备" min-width="130" align="center" header-align="center" />
              <el-table-column prop="type" label="类型" min-width="100" align="center" header-align="center">
                <template #default="{ row }">
                  <el-tag :type="getAlertTypeTag(row.type)">
                    {{ row.type }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="level" label="级别" min-width="90" align="center" header-align="center">
                <template #default="{ row }">
                  <el-tag :type="getAlertLevelTag(row.level)">
                    {{ row.level }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="status" label="状态" min-width="90" align="center" header-align="center">
                <template #default="{ row }">
                  <el-tag :type="getAlertStatusTag(row.status)">
                    {{ row.status }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column label="操作" min-width="150" align="center" header-align="center">
                <template #default="{ row }">
                  <el-button size="small" @click="viewAlert(row)">查看</el-button>
                  <el-button size="small" type="primary" @click="createOrder(row)">派单</el-button>
                </template>
              </el-table-column>
            </el-table>
          </div>
        </div>

        <!-- Work Orders Section -->
        <div class="orders-section">
          <div class="section-header">
            <h2>待处理工单</h2>
            <el-button type="primary" @click="goToAllOrders" class="view-all-btn">全部工单 →</el-button>
          </div>
          <div class="table-container">
            <el-table :data="pendingOrders" stripe style="width: 100%" height="300">
              <el-table-column prop="orderId" label="编号" min-width="150" align="center" header-align="center" />
              <el-table-column prop="alert" label="告警" min-width="130" align="center" header-align="center" />
              <el-table-column prop="assignee" label="指派人" min-width="100" align="center" header-align="center" />
              <el-table-column prop="status" label="状态" min-width="90" align="center" header-align="center">
                <template #default="{ row }">
                  <el-tag :type="getOrderStatusTag(row.status)">
                    {{ row.status }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="updatedAt" label="更新时间" min-width="140" align="center" header-align="center" />
              <el-table-column label="操作" min-width="150" align="center" header-align="center">
                <template #default="{ row }">
                  <el-button size="small" @click="viewOrder(row)">查看</el-button>
                  <el-button size="small" type="success" @click="completeOrder(row)">验收</el-button>
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
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/store/auth'
import {
  House, Monitor, Warning, DataAnalysis, Setting,
  Link, WarningFilled, CircleCloseFilled, Finished,
  Search, Bell, ArrowDown, User, Document,
  Grid, Operation, Tickets, Memo, House as HouseIcon,
  User as UserIcon
} from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

// Register components
import { ElSubMenu } from 'element-plus'

const router = useRouter()
const authStore = useAuthStore()

// User info from auth store
const userInfo = computed(() => authStore.user || {})
const isSuperAdmin = computed(() => authStore.getUserRole === 'SUPER_ADMIN')

// Mock data for demonstration
const globalSearch = ref('')
const unreadNotifications = ref(3)
const activeMenu = ref('dashboard')
const selectedArea = ref('')
const timeWindow = ref('realtime')
const selectedChartArea = ref('')
const selectedDevice = ref('')
const chartMetric = ref('temperature')

// Mock KPI data
const kpi = ref({
  onlineDevices: 42,
  totalDevices: 50,
  todayAlerts: 8,
  unhandledAlerts: 3,
  todayClosedWorkOrders: 5
})

// Mock areas data
const areas = ref([
  { id: 1, name: '库区A', temperature: 4.2, humidity: 65.5, status: 'normal', statusText: '正常', onlineDevices: 12, totalDevices: 12 },
  { id: 2, name: '库区B', temperature: -2.1, humidity: 45.3, status: 'warning', statusText: '警告', onlineDevices: 8, totalDevices: 10 },
  { id: 3, name: '库区C', temperature: 6.8, humidity: 72.1, status: 'error', statusText: '异常', onlineDevices: 5, totalDevices: 8 },
  { id: 4, name: '库区D', temperature: 3.5, humidity: 60.0, status: 'normal', statusText: '正常', onlineDevices: 10, totalDevices: 10 }
])

// Mock devices data
const devices = ref([
  { id: 1, name: '设备1', areaId: 1 },
  { id: 2, name: '设备2', areaId: 1 },
  { id: 3, name: '设备3', areaId: 2 },
  { id: 4, name: '设备4', areaId: 3 }
])

// Mock recent alerts
const recentAlerts = ref([
  { id: 1, timestamp: '2026-02-23 10:30', area: '库区C', device: '温度传感器', type: '温度高', level: '高', status: '未处理' },
  { id: 2, timestamp: '2026-02-23 09:45', area: '库区B', device: '湿度传感器', type: '湿度低', level: '中', status: '处理中' },
  { id: 3, timestamp: '2026-02-23 08:20', area: '库区A', device: '温度传感器', type: '温度低', level: '低', status: '已处理' },
  { id: 4, timestamp: '2026-02-23 07:15', area: '库区C', device: '温度传感器', type: '温度高', level: '高', status: '未处理' },
  { id: 5, timestamp: '2026-02-23 06:30', area: '库区D', device: '湿度传感器', type: '湿度高', level: '中', status: '已处理' }
])

// Mock pending orders
const pendingOrders = ref([
  { orderId: 'WO20260223001', alert: '库区C温度高', assignee: '张三', status: '待处理', updatedAt: '2026-02-23 10:30' },
  { orderId: 'WO20260223002', alert: '库区B湿度低', assignee: '李四', status: '处理中', updatedAt: '2026-02-23 09:45' },
  { orderId: 'WO20260223003', alert: '库区A温度低', assignee: '王五', status: '待处理', updatedAt: '2026-02-23 08:20' },
  { orderId: 'WO20260223004', alert: '库区C温度高', assignee: '赵六', status: '待处理', updatedAt: '2026-02-23 07:15' },
  { orderId: 'WO20260223005', alert: '库区D湿度高', assignee: '钱七', status: '处理中', updatedAt: '2026-02-23 06:30' }
])

// Mock chart data
const chartData = ref([10, 20, 15, 30, 25, 40, 35])

// Computed properties
const filteredAreas = computed(() => {
  if (!selectedArea.value) return areas.value
  return areas.value.filter(area => area.id === selectedArea.value)
})

const selectedAreaName = computed(() => {
  if (!selectedChartArea.value) return '请选择库区'
  const area = areas.value.find(a => a.id === selectedChartArea.value)
  return area ? area.name : '未知库区'
})

const chartMetricText = computed(() => {
  return chartMetric.value === 'temperature' ? '温度' : '湿度'
})

const userInitial = computed(() => {
  return userInfo.value.realName ? userInfo.value.realName.charAt(0) : 'U'
})

const userAvatar = computed(() => {
  // Return a default avatar if no real avatar exists
  return 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png'
})

// Methods for KPI cards navigation
const goToOnlineDevices = () => {
  router.push('/devices?status=online')
}

const goToTodayAlerts = () => {
  router.push('/alerts?date=today')
}

const goToUnhandledAlerts = () => {
  router.push('/alerts?status=unhandled')
}

const goToTodayClosedOrders = () => {
  router.push('/orders?date=today&status=closed')
}

// Methods for alerts navigation
const goToAllAlerts = () => {
  router.push('/alerts')
}

const viewAlert = (alert) => {
  router.push(`/alerts/${alert.id}`)
}

const createOrder = (alert) => {
  router.push(`/orders/create?alertId=${alert.id}`)
}

// Methods for orders navigation
const goToAllOrders = () => {
  router.push('/orders')
}

const viewOrder = (order) => {
  router.push(`/orders/${order.orderId}`)
}

const completeOrder = (order) => {
  // Simulate order completion
  ElMessage.success(`工单 ${order.orderId} 已完成验收`)
  // In a real app, you would make an API call here
}

// Methods for area navigation
const goToAreaDetail = (areaId) => {
  router.push(`/monitoring/${areaId}`)
}

// Methods for quick actions
const goToDeviceManagement = () => {
  router.push('/devices')
}

const goToThresholdSettings = () => {
  router.push('/settings/thresholds')
}

const goToAlertCenter = () => {
  router.push('/alerts')
}

const generateDailyReport = () => {
  ElMessage.info('正在生成AI日报...')
  // In a real app, you would make an API call here
}

// Super admin only methods
const goToUserManagement = () => {
  router.push('/admin/users')
}

const goToPermissionManagement = () => {
  router.push('/admin/permissions')
}

const goToAuditLogs = () => {
  router.push('/admin/logs')
}

// Other actions
const viewProfile = () => {
  router.push('/profile')
}

const settings = () => {
  router.push('/settings')
}

const logout = () => {
  authStore.clearAuthData()
  router.push('/login')
}

// Helper methods for tag colors
const getAlertTypeTag = (type) => {
  if (type.includes('温度')) return 'danger'
  if (type.includes('湿度')) return 'warning'
  return 'info'
}

const getAlertLevelTag = (level) => {
  if (level === '高') return 'danger'
  if (level === '中') return 'warning'
  return 'info'
}

const getAlertStatusTag = (status) => {
  if (status === '未处理') return 'danger'
  if (status === '处理中') return 'warning'
  return 'success'
}

const getOrderStatusTag = (status) => {
  if (status === '待处理') return 'warning'
  if (status === '处理中') return 'primary'
  return 'success'
}

// Simulate data refresh
const refreshData = () => {
  // In a real app, you would fetch data from API
  console.log('Refreshing dashboard data...')
}

// Initialize the page
onMounted(() => {
  refreshData()
})
</script>

<style scoped>
.dashboard-container {
  position: fixed;
  top: 0;
  left: 0;
  width: 100vw;
  height: 100vh;
  display: flex;
  flex-direction: column;
  background-color: #f5f7fa;
  box-sizing: border-box;
  overflow: hidden; /* Hide all scrollbars except browser native */
}

.top-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  height: 60px;
  background-color: white;
  box-shadow: 0 1px 4px rgba(0, 21, 41, 0.08);
  z-index: 10;
  flex-shrink: 0;
}

.logo-section {
  display: flex;
  align-items: center;
}

.logo {
  margin-right: 12px;
}

.app-title {
  font-size: 18px;
  font-weight: bold;
  color: #303133;
}

.search-section {
  flex: 1;
  max-width: 400px;
  margin: 0 40px;
}

.global-search {
  width: 100%;
}

.action-section {
  display: flex;
  align-items: center;
  gap: 20px;
}

.notification-badge {
  margin-right: 20px;
}

.notification-btn {
  border: none;
}

.user-avatar {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
}

.user-name {
  font-size: 14px;
  color: #606266;
}

.dashboard-layout {
  display: flex;
  flex: 1;
  overflow: hidden; /* Hide scrollbars in the layout */
  min-height: 0;
}

.side-menu {
  width: 200px;
  background-color: white;
  box-shadow: 2px 0 6px rgba(0, 21, 41, 0.35);
  overflow-y: auto; /* Allow sidebar to scroll independently if needed */
  flex-shrink: 0;
  height: calc(100vh - 60px); /* Account for the top bar height */
}

.menu {
  border-right: none;
}

.main-content {
  flex: 1;
  overflow-y: auto; /* Main content scrolls with browser native scrollbar */
  padding: 20px;
  min-height: 0;
  height: calc(100vh - 60px); /* Account for the top bar height */
}

/* Remove scrollbars from individual sections to prevent duplicates */
.kpi-section,
.overview-section,
.chart-section,
.alerts-section,
.orders-section,
.quick-actions-section {
  overflow: visible; /* Let content overflow naturally */
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
  overflow-x: auto; /* Only allow table horizontal scroll when needed */
  max-height: 300px; /* Limit height to prevent vertical overflow */
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
  .top-bar {
    flex-direction: column;
    height: auto;
    padding: 12px;
  }

  .logo-section {
    margin-bottom: 12px;
  }

  .search-section {
    max-width: 100%;
    margin: 0 0 12px 0;
  }

  .action-section {
    justify-content: center;
  }

  .side-menu {
    width: 60px;
    height: calc(100vh - 60px);
  }

  .main-content {
    height: calc(100vh - 60px);
  }

  .kpi-section {
    grid-template-columns: 1fr;
  }

  .quick-actions-grid {
    grid-template-columns: repeat(auto-fill, minmax(120px, 1fr));
  }
}
</style>