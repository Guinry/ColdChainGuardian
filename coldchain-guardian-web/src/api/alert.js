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

  // 导出告警
  exportAlerts(params) {
    return apiClient.get('/alerts/export', { params, responseType: 'blob' })
  }
}