import { apiClient } from '@/utils/api'

// 仪表盘和趋势分析API接口
export const dashboardApi = {
  // 获取综合仪表盘统计
  getStats() {
    return apiClient.get('/dashboard/stats')
  },

  // 获取综合趋势分析
  getTrends(params) {
    return apiClient.get('/dashboard/trends', { params })
  },

  // 获取温湿度趋势
  getEnvironmentTrend(params) {
    return apiClient.get('/dashboard/environment-trend', { params })
  },

  // 获取告警趋势
  getAlertTrend(params) {
    return apiClient.get('/dashboard/alert-trend', { params })
  },

  // 获取工单趋势
  getWorkOrderTrend(params) {
    return apiClient.get('/dashboard/workorder-trend', { params })
  },

  // 获取设备状态趋势
  getDeviceStatusTrend(params) {
    return apiClient.get('/dashboard/device-status-trend', { params })
  },

  // AI助手分析仪表盘数据
  analyzeWithAI(params) {
    return apiClient.post('/dashboard/ai-analyze', params)
  }
}

// 导出各个函数以便单独导入使用
export const getDashboardStatsApi = dashboardApi.getStats
export const getDashboardTrendsApi = dashboardApi.getTrends
export const getEnvironmentTrendApi = dashboardApi.getEnvironmentTrend
export const getAlertTrendApi = dashboardApi.getAlertTrend
export const getWorkOrderTrendApi = dashboardApi.getWorkOrderTrend
export const getDeviceStatusTrendApi = dashboardApi.getDeviceStatusTrend
export const analyzeDashboardWithAIApi = dashboardApi.analyzeWithAI