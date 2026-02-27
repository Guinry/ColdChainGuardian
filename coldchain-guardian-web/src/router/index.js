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
    component: () => import('../components/Layout.vue'),
    meta: { requiresAuth: true },
    children: [
      {
        path: '',
        component: () => import('../views/Dashboard.vue')
      }
    ]
  },
  {
    path: '/warehouse-area',
    name: 'WarehouseArea',
    component: () => import('../views/warehouse-area/WarehouseAreaView.vue'),
    meta: { requiresAuth: true, permissions: ['area:view'] }
  },
  {
    path: '/devices',
    name: 'DeviceManagement',
    component: () => import('../views/device/DeviceManagementView.vue'),
    meta: { requiresAuth: true, permissions: ['device:view'] }
  },
  {
    path: '/devices/:deviceId/data',
    name: 'DeviceData',
    component: () => import('../views/device/DeviceDataView.vue'),
    meta: { requiresAuth: true, permissions: ['device:view'] },
    props: true
  },
  {
    path: '/devices/:deviceId/alerts',
    name: 'DeviceAlerts',
    component: () => import('../views/device/DeviceAlertView.vue'),
    meta: { requiresAuth: true, permissions: ['device:view', 'alert:view'] },
    props: true
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router