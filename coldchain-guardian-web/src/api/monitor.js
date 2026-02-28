// 实时监测API接口
import { apiClient } from '@/utils/api'

export const monitorApi = {
  // 获取实时监测总览指标
  getSummary() {
    return apiClient.get('/monitor/summary')
  },

  // 获取实时设备列表（分页 + 筛选）
  getMonitorDevices(params) {
    return apiClient.get('/monitor/devices', { params })
  },

  // 获取设备实时曲线数据
  getDeviceTrend(deviceId, from, to, interval = 60) {
    return apiClient.get(`/monitor/devices/${deviceId}/trend`, {
      params: { from, to, interval }
    })
  }
}