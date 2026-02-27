import { apiClient } from '@/utils/api'

// 设备管理API接口
export const deviceApi = {
  // 获取设备列表（分页）
  getList(params) {
    return apiClient.get('/devices', { params })
  },

  // 获取设备详情
  getDetail(id) {
    return apiClient.get(`/devices/${id}`)
  },

  // 获取设备最新数据
  getLatestData(id) {
    return apiClient.get(`/devices/${id}/latest`)
  },

  // 获取设备历史数据
  getHistoricalData(id, params) {
    return apiClient.get(`/devices/${id}/data`, { params })
  },

  // 新增设备
  create(data) {
    return apiClient.post('/devices', data)
  },

  // 更新设备
  update(id, data) {
    return apiClient.put(`/devices/${id}`, data)
  },

  // 删除设备
  delete(id) {
    return apiClient.delete(`/devices/${id}`)
  },

  // 启用/禁用设备
  updateStatus(id, enabled) {
    return apiClient.put(`/devices/${id}/status`, { enabled })
  },

  // 更新告警开关状态
  updateAlarmStatus(id, alarmEnabled) {
    return apiClient.put(`/devices/${id}/alarm-status`, { alarmEnabled })
  },

  // 更新设备阈值
  updateThreshold(id, thresholdData) {
    return apiClient.put(`/devices/${id}/threshold`, thresholdData)
  },

  // 批量启用/禁用设备
  batchUpdateStatus(ids, enabled) {
    return apiClient.put('/devices/batch-status', { ids, enabled })
  },

  // 解绑设备与库区的关联
  unbindArea(id) {
    return apiClient.put(`/devices/${id}/unbind-area`)
  },

  // 导出设备
  exportDevices(params) {
    return apiClient.get('/devices/export', { params, responseType: 'blob' })
  },

  // 批量导入设备
  importDevices(formData) {
    return apiClient.post('/devices/import', formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })
  }
}