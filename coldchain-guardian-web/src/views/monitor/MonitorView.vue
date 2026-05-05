<template>
  <Layout>
    <div class="monitor-page">
      <!-- 顶部：标题 + 操作区 -->
      <div class="page-header">
        <div class="header-left">
          <h1>实时监测</h1>
        </div>
        <div class="header-right">
          <el-button @click="refreshAllData" :icon="Refresh" size="default">
            刷新
          </el-button>
          <el-switch
            v-model="autoRefreshEnabled"
            active-text="自动刷新"
            inactive-text="停止刷新"
            @change="toggleAutoRefresh"
          />
          <el-select
            v-model="refreshInterval"
            placeholder="刷新间隔"
            size="default"
            style="width: 120px; margin-left: 12px;"
            @change="handleRefreshIntervalChange"
          >
            <el-option label="5秒" :value="5000" />
            <el-option label="10秒" :value="10000" />
            <el-option label="30秒" :value="30000" />
            <el-option label="60秒" :value="60000" />
          </el-select>
          <span class="last-update-time">最后更新: {{ lastUpdateTime }}</span>
        </div>
      </div>

      <!-- 中部：KPI 统计卡片 -->
      <MonitorKpiCards :summary="summary" @card-click="handleCardClick" />

      <!-- 主体：左侧库区树 + 右侧实时列表 -->
      <div class="main-content">
        <AreaTreePanel
          :tree-data="areaTreeData"
          @node-click="handleAreaNodeClick"
          @search-change="handleAreaSearch"
        />

        <div class="device-list-panel">
          <RealtimeDeviceTable
            :data="deviceList"
            :loading="tableLoading"
            :pagination="pagination"
            :filters="filters"
            @page-change="handlePageChange"
            @size-change="handleSizeChange"
            @filter-change="handleFilterChange"
            @refresh="refreshDeviceList"
            @view-detail="handleViewDetail"
            @view-trend="handleViewTrend"
            @view-alert="handleViewAlert"
          />
        </div>
      </div>

      <!-- 设备详情抽屉 -->
      <DeviceRealtimeDrawer
        :visible="drawerVisible"
        :device="currentDevice"
        :type="drawerType"
        @close="handleCloseDrawer"
      />
    </div>
  </Layout>
</template>

<script setup>
import { ref, reactive, onMounted, onBeforeUnmount, watch } from 'vue'
import { useRoute } from 'vue-router'
import { Refresh } from '@element-plus/icons-vue'
import { monitorApi } from '@/api/monitor'
import { areaApi } from '@/api/area'
import Layout from '@/components/Layout.vue'

// 引入子组件
import MonitorKpiCards from './components/MonitorKpiCards.vue'
import AreaTreePanel from './components/AreaTreePanel.vue'
import RealtimeDeviceTable from './components/RealtimeDeviceTable.vue'
import DeviceRealtimeDrawer from './components/DeviceRealtimeDrawer.vue'

const route = useRoute()

// 页面状态
const autoRefreshEnabled = ref(true)
const refreshInterval = ref(10000) // 10秒
const timer = ref(null)
const lastUpdateTime = ref('')
const alive = ref(true) // 组件存活标志

// KPI 数据
const summary = ref({})

// 库区树数据
const areaTreeData = ref([])

// 表格数据
const deviceList = ref([])
const tableLoading = ref(false)

// 分页信息
const pagination = reactive({
  currentPage: 1,
  pageSize: 20,
  total: 0
})

// 筛选条件
const filters = reactive({
  areaId: null,
  online: null,
  alarming: null,
  keyword: '',
  temperatureMin: null,
  temperatureMax: null,
  humidityMin: null,
  humidityMax: null,
  lastSeenRange: [],
  deviceType: ''
})

const getQueryValue = (value) => Array.isArray(value) ? value[0] : value

const parseBooleanQuery = (value) => {
  const normalized = String(getQueryValue(value) ?? '').toLowerCase()
  if (['true', '1', 'online', 'yes'].includes(normalized)) return true
  if (['false', '0', 'offline', 'no'].includes(normalized)) return false
  return null
}

const applyRouteQuery = () => {
  const query = route.query
  filters.areaId = getQueryValue(query.areaId) ? Number(getQueryValue(query.areaId)) : null
  filters.online = query.online !== undefined ? parseBooleanQuery(query.online) : filters.online
  filters.alarming = query.alarming !== undefined ? parseBooleanQuery(query.alarming) : filters.alarming
  filters.keyword = getQueryValue(query.keyword) ? String(getQueryValue(query.keyword)) : ''
  filters.deviceType = getQueryValue(query.deviceType) ? String(getQueryValue(query.deviceType)) : ''
}

// 抽屉状态
const drawerVisible = ref(false)
const currentDevice = ref(null)
const drawerType = ref('detail') // 'detail', 'trend', 'alert'

// 刷新所有数据
const refreshAllData = async () => {
  await Promise.all([
    refreshSummary(),
    refreshAreaTree(),
    refreshDeviceList()
  ])
  lastUpdateTime.value = new Date().toLocaleTimeString()
}

// 刷新KPI汇总数据
const refreshSummary = async () => {
  try {
    const response = await monitorApi.getSummary()
    if (alive.value) {
      summary.value = response.data?.data
    }
  } catch (error) {
    console.error('获取汇总数据失败:', error)
    if (alive.value) {
      summary.value = {}
    }
  }
}

// 刷新库区树数据
const refreshAreaTree = async () => {
  try {
    const response = await areaApi.getAreaTree()
    if (alive.value) {
      areaTreeData.value = response.data?.data
    }
  } catch (error) {
    console.error('获取库区树失败:', error)
    if (alive.value) {
      areaTreeData.value = []
    }
  }
}

// 刷新设备列表
const refreshDeviceList = async () => {
  tableLoading.value = true
  try {
    const params = {
      page: pagination.currentPage,
      size: pagination.pageSize,
      areaId: filters.areaId,
      online: filters.online,
      alarming: filters.alarming,
      keyword: filters.keyword,
      deviceType: filters.deviceType  // 添加设备类型筛选参数
    }
    const response = await monitorApi.getMonitorDevices(params)
    if (alive.value) {
      deviceList.value = response.data?.data?.data || []
      pagination.total = response.data?.data?.total || 0
    }
  } catch (error) {
    console.error('获取设备列表失败:', error)
    if (alive.value) {
      deviceList.value = []
      pagination.total = 0
    }
  } finally {
    if (alive.value) {
      tableLoading.value = false
    }
  }
}

// 自动刷新控制
const startAutoRefresh = () => {
  const loop = async () => {
    if (alive.value && autoRefreshEnabled.value) {
      // 只刷新摘要数据和设备列表，不刷新库区树（库区结构很少变化）
      await Promise.all([refreshSummary(), refreshDeviceList()])
    }
    // 请求完成后，再开启下一个定时器
    if (alive.value && autoRefreshEnabled.value) {
      timer.value = setTimeout(loop, refreshInterval.value)
    }
  }
  loop()
}

const stopAutoRefresh = () => {
  if (timer.value) {
    clearTimeout(timer.value)
    timer.value = null
  }
}

const toggleAutoRefresh = () => {
  if (autoRefreshEnabled.value) {
    startAutoRefresh()
  } else {
    stopAutoRefresh()
  }
}

const handleRefreshIntervalChange = () => {
  if (autoRefreshEnabled.value) {
    stopAutoRefresh()
    startAutoRefresh()
  }
}

// KPI 卡片点击事件
const handleCardClick = (type) => {
  if (type === 'offlineDevices') {
    filters.online = false
    filters.areaId = null
    pagination.currentPage = 1
    refreshDeviceList()
  } else if (type === 'unhandledAlerts') {
    filters.alarming = true
    filters.areaId = null
    pagination.currentPage = 1
    refreshDeviceList()
  }
}

// 库区树节点点击事件
const handleAreaNodeClick = (node) => {
  filters.areaId = node.id
  pagination.currentPage = 1
  refreshDeviceList()
}

// 库区搜索
const handleAreaSearch = () => {}

// 分页和筛选事件
const handlePageChange = (page) => {
  pagination.currentPage = page
  refreshDeviceList()
}

const handleSizeChange = (size) => {
  pagination.pageSize = size
  pagination.currentPage = 1
  refreshDeviceList()
}

const handleFilterChange = (nextFilters = {}) => {
  Object.assign(filters, nextFilters)
  pagination.currentPage = 1
  refreshDeviceList()
}

// 设备操作事件
const handleViewDetail = (device) => {
  currentDevice.value = device
  drawerType.value = 'detail'
  drawerVisible.value = true
}

const handleViewTrend = (device) => {
  currentDevice.value = device
  drawerType.value = 'trend'
  drawerVisible.value = true
}

const handleViewAlert = (device) => {
  currentDevice.value = device
  drawerType.value = 'alert'
  drawerVisible.value = true
}

// 关闭抽屉
const handleCloseDrawer = () => {
  drawerVisible.value = false
  currentDevice.value = null
}

// 组件挂载
onMounted(async () => {
  applyRouteQuery()
  await refreshAllData()
  startAutoRefresh()
})

watch(() => route.query, () => {
  applyRouteQuery()
  pagination.currentPage = 1
  refreshDeviceList()
})

// 组件卸载前清理
onBeforeUnmount(() => {
  alive.value = false
  stopAutoRefresh()
})
</script>

<style scoped>
.monitor-page {
  padding: 20px 24px 28px;
  min-height: 100%;
  display: flex;
  flex-direction: column;
  background: var(--ccg-bg);
  color: var(--ccg-text);
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 16px;
  margin-bottom: 16px;
}

.header-left h1 {
  margin: 0;
  font-size: 22px;
  line-height: 1.2;
  font-weight: 750;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
  justify-content: flex-end;
}

.last-update-time {
  font-size: 13px;
  color: var(--ccg-muted);
}

.main-content {
  display: grid;
  grid-template-columns: 260px minmax(0, 1fr);
  gap: 16px;
  align-items: start;
}

.device-list-panel {
  min-width: 0;
  background: #fff;
  border: 1px solid var(--ccg-border);
  border-radius: 8px;
  padding: 16px;
  box-shadow: var(--ccg-shadow-sm);
}

@media (max-width: 1160px) {
  .page-header {
    flex-direction: column;
  }

  .header-right {
    justify-content: flex-start;
  }

  .main-content {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 768px) {
  .monitor-page {
    padding: 16px;
  }
}
</style>
