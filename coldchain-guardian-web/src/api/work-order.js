import { apiClient } from '@/utils/api'

// 工单管理API接口
export const workOrderApi = {
  // 创建工单
  create(data) {
    return apiClient.post('/work-orders', data)
  },

  // 获取工单详情
  getDetail(id) {
    return apiClient.get(`/work-orders/${id}`)
  },

  // 获取工单列表
  getList(params) {
    return apiClient.get('/work-orders', { params })
  },

  // 更新工单状态
  updateStatus(id, data) {
    return apiClient.put(`/work-orders/${id}/status`, data)
  },

  // 更新工单信息
  update(id, data) {
    return apiClient.put(`/work-orders/${id}`, data)
  },

  // 获取工单日志
  getLogs(workOrderId) {
    return apiClient.get(`/work-orders/${workOrderId}/logs`)
  },

  // 获取工单统计
  getStats() {
    return apiClient.get('/work-orders/stats')
  }
}

// 导出各个函数以便单独导入使用
export const createWorkOrderApi = workOrderApi.create
export const getWorkOrderByIdApi = workOrderApi.getDetail
export const getWorkOrdersApi = workOrderApi.getList
export const updateWorkOrderStatusApi = workOrderApi.updateStatus
export const updateWorkOrderApi = workOrderApi.update
export const getWorkOrderLogsApi = workOrderApi.getLogs