import { apiClient } from '@/utils/api'

// 告警管理API接口
export const alertApi = {
  // 根据设备ID获取告警列表
  getByDeviceId(deviceId, params) {
    return apiClient.get(`/alerts/device/${deviceId}`, { params })
  },

  // 获取告警详情
  getDetail(id) {
    return apiClient.get(`/alerts/${id}`)
  },

  // 更新告警状态
  updateStatus(id, data) {
    return apiClient.put(`/alerts/${id}/status`, data)
  },

  // 批量更新告警状态
  batchUpdateStatus(ids, data) {
    return apiClient.put('/alerts/batch-status', { ids, ...data })
  },

  // 创建告警
  create(data) {
    return apiClient.post('/alerts', data)
  },

  // 删除告警
  delete(id) {
    return apiClient.delete(`/alerts/${id}`)
  },

  // 获取告警统计
  getStats(params) {
    return apiClient.get('/alerts/stats', { params })
  },

  // 搜索告警
  search(params) {
    return apiClient.get('/alerts/search', { params })
  },

  // 将告警转为工单
  convertToWorkOrder(id, data) {
    return apiClient.put(`/alerts/${id}/convert-to-work-order`, data)
  },

  // 获取紧急告警
  getUrgent() {
    return apiClient.get('/alerts/urgent')
  },

  // 批量将告警转为工单
  batchConvertToWorkOrders(data) {
    return apiClient.put('/alerts/batch-convert-to-work-order', data)
  },

  // 获取告警趋势分析
  getAlertTrendAnalysis(params) {
    return apiClient.get('/alerts/analysis/trend', { params })
  },

  // 获取重复告警分析
  getRecurringAlertAnalysis() {
    return apiClient.get('/alerts/analysis/recurring')
  },

  // 获取设备健康度评分
  getDeviceHealthScore() {
    return apiClient.get('/alerts/analysis/device-health')
  },

  // 获取告警根因分析
  getRootCauseAnalysis() {
    return apiClient.get('/alerts/analysis/root-cause')
  },

  // 导出告警
  exportAlerts(params) {
    return apiClient.get('/alerts/export', { params, responseType: 'blob' })
  }
}

// 导出各个函数以便单独导入使用
export const getAlertByIdApi = alertApi.getDetail
export const getAlertsApi = alertApi.search
export const getAlertStatsApi = alertApi.getStats
export const updateAlertStatusApi = alertApi.updateStatus
export const convertAlertToWorkOrderApi = alertApi.convertToWorkOrder
export const getUrgentAlertsApi = alertApi.getUrgent
export const batchConvertAlertsToWorkOrdersApi = alertApi.batchConvertToWorkOrders
export const getAlertTrendAnalysisApi = alertApi.getAlertTrendAnalysis
export const getRecurringAlertAnalysisApi = alertApi.getRecurringAlertAnalysis
export const getDeviceHealthScoreApi = alertApi.getDeviceHealthScore
export const getRootCauseAnalysisApi = alertApi.getRootCauseAnalysis