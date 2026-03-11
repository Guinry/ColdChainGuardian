import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/',
    redirect: '/login'
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/LoginPage.vue')
  },
  {
    path: '/dashboard',
    name: 'Dashboard',
    component: () => import('../views/Dashboard.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/warehouse-area',
    name: 'WarehouseArea',
    component: () => import('../views/warehouse-area/WarehouseAreaView.vue'),
    meta: { requiresAuth: true, roles: ['ADMIN', 'MANAGER'] }
  },
  {
    path: '/devices',
    name: 'DeviceManagement',
    component: () => import('../views/device/DeviceManagementView.vue'),
    meta: { requiresAuth: true, roles: ['ADMIN', 'MANAGER'] }
  },
  {
    path: '/devices/:deviceId/data',
    name: 'DeviceData',
    component: () => import('../views/device/components/DeviceDataView.vue'),
    meta: { requiresAuth: true, roles: ['ADMIN', 'MANAGER'] },
    props: true
  },
  {
    path: '/devices/:deviceId/alerts',
    name: 'DeviceAlerts',
    component: () => import('../views/device/components/DeviceAlertView.vue'),
    meta: { requiresAuth: true, roles: ['ADMIN', 'MANAGER'] },
    props: true
  },
  {
    path: '/monitor',
    name: 'Monitor',
    component: () => import('../views/monitor/MonitorView.vue'),
    meta: { requiresAuth: true, roles: ['ADMIN', 'MANAGER'] }
  },
  {
    path: '/work-orders',
    name: 'WorkOrderCenter',
    component: () => import('../views/work-order/WorkOrderCenterView.vue'),
    meta: { requiresAuth: true, roles: ['ADMIN', 'MANAGER'] }
  },
  {
    path: '/alerts',
    name: 'AlertCenter',
    component: () => import('../views/alert/AlertCenterView.vue'),
    meta: { requiresAuth: true, roles: ['ADMIN', 'MANAGER'] }
  },
  {
    path: '/trend-analysis',
    name: 'TrendAnalysis',
    component: () => import('../views/trend-analysis/TrendAnalysisView.vue'),
    meta: { requiresAuth: true, roles: ['ADMIN', 'MANAGER'] }
  },
  {
    path: '/ai-assistant',
    name: 'AIAssistant',
    component: () => import('../views/ai-assistant/AIAssistantView.vue'),
    meta: { requiresAuth: true, roles: ['ADMIN', 'MANAGER'] }
  },
  {
    path: '/employees',
    name: 'EmployeeManagement',
    component: () => import('@/views/employees/EmployeeManagement.vue'),
    meta: { requiresAuth: true, roles: ['ADMIN'] }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router