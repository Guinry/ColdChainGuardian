<template>
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
      <el-autocomplete
        v-model="globalSearch"
        :fetch-suggestions="fetchSearchSuggestions"
        placeholder="搜索设备、告警、工单或页面"
        :prefix-icon="Search"
        class="global-search"
        clearable
        :trigger-on-focus="false"
        :debounce="260"
        @select="handleSearchSelect"
        @keyup.enter="handleSearchEnter"
      >
        <template #default="{ item }">
          <div class="search-result">
            <div class="result-main">
              <el-icon class="result-icon"><component :is="item.icon" /></el-icon>
              <span class="result-title">{{ item.title }}</span>
            </div>
            <span class="result-meta">{{ item.meta }}</span>
          </div>
        </template>
      </el-autocomplete>
    </div>

    <div class="action-section">
      <el-popover placement="bottom-end" trigger="click" width="380" @show="refreshNotifications">
        <template #reference>
          <el-badge :value="unreadNotifications" :hidden="!unreadNotifications" class="notification-badge">
            <el-button circle class="notification-btn">
              <el-icon><Bell /></el-icon>
            </el-button>
          </el-badge>
        </template>
        <div class="notification-panel">
          <div class="panel-header">
            <div>
              <div class="panel-title">待处理事项</div>
              <div class="panel-subtitle">{{ unreadNotifications }} 条需要关注</div>
            </div>
            <el-button size="small" link :icon="Refresh" @click="refreshNotifications">刷新</el-button>
          </div>

          <div class="notification-group">
            <div class="group-title">
              <span>未处理告警</span>
              <el-button link type="primary" @click="goToAlerts">全部</el-button>
            </div>
            <button
              v-for="alert in notificationAlerts"
              :key="`alert-${alert.id}`"
              class="notification-item"
              type="button"
              @click="goToAlert(alert)"
            >
              <span class="dot danger"></span>
              <span class="item-content">
                <span class="item-title">{{ alert.description || alert.alertType || '未处理告警' }}</span>
                <span class="item-meta">{{ alert.deviceName || alert.areaName || '未知位置' }}</span>
              </span>
            </button>
            <el-empty v-if="!notificationAlerts.length" description="暂无未处理告警" :image-size="56" />
          </div>

          <div class="notification-group">
            <div class="group-title">
              <span>待处理工单</span>
              <el-button link type="primary" @click="goToOrders">全部</el-button>
            </div>
            <button
              v-for="order in notificationOrders"
              :key="`order-${order.id || order.orderNo}`"
              class="notification-item"
              type="button"
              @click="goToOrder(order)"
            >
              <span class="dot warning"></span>
              <span class="item-content">
                <span class="item-title">{{ order.title || order.description || order.orderNo || '待处理工单' }}</span>
                <span class="item-meta">{{ order.assigneeName || '未分配' }} · {{ getStatusText(order.status) }}</span>
              </span>
            </button>
            <el-empty v-if="!notificationOrders.length" description="暂无待处理工单" :image-size="56" />
          </div>
        </div>
      </el-popover>

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
</template>

<script setup>
import { ref, computed, onMounted, markRaw } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import {
  Search, Bell, ArrowDown, Refresh, Monitor, WarningFilled, Tickets, Grid, Setting, TrendCharts, User
} from '@element-plus/icons-vue';
import { deviceApi } from '@/api/device';
import { alertApi } from '@/api/alert';
import { workOrderApi } from '@/api/work-order';

const router = useRouter();

// Props
const props = defineProps({
  userInfo: {
    type: Object,
    default: () => ({
      realName: '管理员'
    })
  }
});

// Emits
const emit = defineEmits(['view-profile', 'settings', 'logout']);

// Reactive variables
const globalSearch = ref('');
const notificationAlerts = ref([]);
const notificationOrders = ref([]);
const notificationTotals = ref({ alerts: 0, orders: 0 });
const lastSuggestions = ref([]);

const icons = {
  Monitor: markRaw(Monitor),
  WarningFilled: markRaw(WarningFilled),
  Tickets: markRaw(Tickets),
  Grid: markRaw(Grid),
  Setting: markRaw(Setting),
  TrendCharts: markRaw(TrendCharts),
  User: markRaw(User)
};

const quickEntries = [
  { type: 'page', title: 'Dashboard', meta: '页面', value: 'Dashboard', path: '/dashboard', icon: icons.Grid },
  { type: 'page', title: '实时监测', meta: '页面', value: '实时监测', path: '/monitor', icon: icons.Monitor },
  { type: 'page', title: '设备管理', meta: '页面', value: '设备管理', path: '/devices', icon: icons.Monitor },
  { type: 'page', title: '库区管理', meta: '页面', value: '库区管理', path: '/warehouse-area', icon: icons.Grid },
  { type: 'page', title: '告警中心', meta: '页面', value: '告警中心', path: '/alerts', icon: icons.WarningFilled },
  { type: 'page', title: '工单管理', meta: '页面', value: '工单管理', path: '/work-orders', icon: icons.Tickets },
  { type: 'page', title: '趋势分析', meta: '页面', value: '趋势分析', path: '/trend-analysis', icon: icons.TrendCharts },
  { type: 'page', title: '阈值规则', meta: '页面', value: '阈值规则', path: '/settings/thresholds', icon: icons.Setting },
  { type: 'page', title: '个人资料', meta: '页面', value: '个人资料', path: '/profile', icon: icons.User }
];

// Computed properties
const unreadNotifications = computed(() => notificationTotals.value.alerts + notificationTotals.value.orders);

const userInitial = computed(() => {
  return props.userInfo.realName ? props.userInfo.realName.charAt(0) : 'U';
});

const userAvatar = computed(() => {
  return 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png';
});

const unwrapPage = (response) => {
  const payload = response?.data?.data || response?.data || {};
  const records = payload.records || payload.data || payload.list || [];
  return {
    records: Array.isArray(records) ? records : [],
    total: payload.total || 0
  };
};

const fetchSearchSuggestions = async (queryString, callback) => {
  const keyword = queryString.trim();
  if (!keyword) {
    lastSuggestions.value = [];
    callback([]);
    return;
  }

  const lowerKeyword = keyword.toLowerCase();
  const pageResults = quickEntries
    .filter((item) => item.title.toLowerCase().includes(lowerKeyword) || item.path.includes(lowerKeyword))
    .slice(0, 4);

  const [deviceRes, alertRes, orderRes] = await Promise.allSettled([
    deviceApi.getList({ keyword, page: 1, size: 5 }),
    alertApi.search({ keyword, page: 1, size: 5 }),
    workOrderApi.getList({ keyword, page: 1, size: 5 })
  ]);

  const deviceResults = deviceRes.status === 'fulfilled'
    ? unwrapPage(deviceRes.value).records.map((device) => ({
      type: 'device',
      title: device.deviceName || device.deviceCode,
      meta: `设备 · ${device.deviceCode || '-'} · ${device.areaName || '未分配库区'}`,
      value: device.deviceName || device.deviceCode,
      path: `/devices/${device.id}/data`,
      icon: icons.Monitor
    }))
    : [];

  const alertResults = alertRes.status === 'fulfilled'
    ? unwrapPage(alertRes.value).records.map((alert) => ({
      type: 'alert',
      title: alert.description || alert.alertType || `告警 #${alert.id}`,
      meta: `告警 · ${alert.deviceName || alert.areaName || '未知位置'}`,
      value: alert.description || alert.alertType || `告警 #${alert.id}`,
      path: `/alerts/${alert.id}`,
      icon: icons.WarningFilled
    }))
    : [];

  const orderResults = orderRes.status === 'fulfilled'
    ? unwrapPage(orderRes.value).records.map((order) => ({
      type: 'order',
      title: order.title || order.description || order.orderNo || `工单 #${order.id}`,
      meta: `工单 · ${getStatusText(order.status)} · ${order.assigneeName || '未分配'}`,
      value: order.title || order.description || order.orderNo || `工单 #${order.id}`,
      path: order.id ? `/work-orders/${order.id}` : `/work-orders?keyword=${encodeURIComponent(keyword)}`,
      icon: icons.Tickets
    }))
    : [];

  lastSuggestions.value = [...pageResults, ...deviceResults, ...alertResults, ...orderResults].slice(0, 10);
  callback(lastSuggestions.value);
};

const handleSearchSelect = (item) => {
  if (!item?.path) return;
  router.push(item.path);
  globalSearch.value = '';
};

const handleSearchEnter = () => {
  const keyword = globalSearch.value.trim();
  if (!keyword) return;

  const exact = lastSuggestions.value[0] || quickEntries.find((item) => item.title.includes(keyword));
  if (exact) {
    handleSearchSelect(exact);
    return;
  }

  router.push({ path: '/devices', query: { keyword } });
  ElMessage.info(`已在设备管理中搜索“${keyword}”`);
};

const refreshNotifications = async () => {
  const [alertsRes, ordersRes] = await Promise.allSettled([
    alertApi.search({ status: 'UNHANDLED', page: 1, size: 5 }),
    workOrderApi.getList({ status: 'PENDING', page: 1, size: 5 })
  ]);

  if (alertsRes.status === 'fulfilled') {
    const page = unwrapPage(alertsRes.value);
    notificationAlerts.value = page.records;
    notificationTotals.value.alerts = page.total;
  }

  if (ordersRes.status === 'fulfilled') {
    const page = unwrapPage(ordersRes.value);
    notificationOrders.value = page.records;
    notificationTotals.value.orders = page.total;
  }
};

const getStatusText = (status) => {
  const map = {
    UNHANDLED: '未处理',
    HANDLING: '处理中',
    PENDING: '待处理',
    PROCESSING: '处理中',
    VERIFYING: '待验收',
    COMPLETED: '已完成',
    CLOSED: '已关闭'
  };
  return map[status] || status || '待处理';
};

const goToAlert = (alert) => router.push(`/alerts/${alert.id}`);
const goToAlerts = () => router.push('/alerts?status=unhandled');
const goToOrder = (order) => {
  if (order.id) router.push(`/work-orders/${order.id}`);
  else router.push({ path: '/work-orders', query: { keyword: order.orderNo || order.title || '' } });
};
const goToOrders = () => router.push('/work-orders?status=PENDING');

// Methods
const viewProfile = () => {
  emit('view-profile');
};

const settings = () => {
  emit('settings');
};

const logout = () => {
  emit('logout');
};

onMounted(() => {
  refreshNotifications();
});
</script>

<style scoped>
.top-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 22px;
  height: var(--ccg-header-height);
  background-color: #fff;
  border-bottom: 1px solid var(--ccg-border);
  box-shadow: 0 1px 3px rgba(15, 23, 42, 0.04);
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
  font-size: 17px;
  font-weight: 700;
  color: #1f2937;
}

.search-section {
  flex: 1;
  max-width: 560px;
  margin: 0 32px;
}

.global-search {
  width: 100%;
}

.search-result {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  min-width: 0;
}

.result-main {
  display: flex;
  align-items: center;
  gap: 8px;
  min-width: 0;
}

.result-icon {
  color: #409eff;
}

.result-title {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  color: #1f2937;
}

.result-meta {
  flex-shrink: 0;
  color: #909399;
  font-size: 12px;
}

.action-section {
  display: flex;
  align-items: center;
  gap: 14px;
}

.notification-badge {
  margin-right: 4px;
}

.notification-btn {
  border-color: var(--ccg-border);
  color: #4b5563;
  background: #fff;
}

.notification-panel {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.panel-header,
.group-title {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.panel-title {
  font-size: 15px;
  font-weight: 700;
  color: #1f2937;
}

.panel-subtitle {
  margin-top: 2px;
  font-size: 12px;
  color: #909399;
}

.notification-group {
  padding-top: 10px;
  border-top: 1px solid #ebeef5;
}

.group-title {
  margin-bottom: 8px;
  font-size: 13px;
  font-weight: 600;
  color: #606266;
}

.notification-item {
  width: 100%;
  display: flex;
  align-items: flex-start;
  gap: 10px;
  padding: 8px 6px;
  border: 0;
  border-radius: 6px;
  background: transparent;
  text-align: left;
  cursor: pointer;
}

.notification-item:hover {
  background: #f8fafc;
}

.dot {
  width: 8px;
  height: 8px;
  margin-top: 6px;
  border-radius: 50%;
  flex-shrink: 0;
}

.dot.danger {
  background: #f56c6c;
}

.dot.warning {
  background: #e6a23c;
}

.item-content {
  display: flex;
  flex-direction: column;
  min-width: 0;
}

.item-title {
  color: #1f2937;
  font-size: 13px;
  line-height: 1.35;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.item-meta {
  margin-top: 2px;
  color: #909399;
  font-size: 12px;
}

.user-avatar {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  min-height: 36px;
  padding: 2px 6px 2px 2px;
  border-radius: 999px;
  transition: background 0.18s;
}

.user-avatar:hover {
  background: #f8fafc;
}

.user-name {
  font-size: 14px;
  color: #606266;
}

@media (max-width: 1080px) {
  .search-section {
    max-width: 420px;
    margin: 0 18px;
  }
}
</style>
