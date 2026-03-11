import axios from 'axios'

// Create an Axios instance
export const apiClient = axios.create({
  baseURL: 'http://localhost:8080/api', // Backend API base URL
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json'
  }
})

// Request interceptor to add token to requests
apiClient.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token') || sessionStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

// Response interceptor to handle token expiration
apiClient.interceptors.response.use(
  (response) => response,
  (error) => {
    // Only redirect on 401 if not on login route
    if (error.response?.status === 401) {
      const currentPath = window.location.pathname

      // Don't redirect if we're on login page
      if (currentPath !== '/login' && !currentPath.includes('/auth/login')) {
        // Token expired or invalid, redirect to login
        localStorage.removeItem('token')
        localStorage.removeItem('user')
        localStorage.removeItem('permissions')
        sessionStorage.removeItem('token')
        sessionStorage.removeItem('user')
        sessionStorage.removeItem('permissions')

        // Redirect to login page
        window.location.href = '/login'
      }
    }
    return Promise.reject(error)
  }
)

// 库区管理API接口
export const warehouseAreaApi = {
  // 获取整棵树
  getTree: () => apiClient.get('/areas'),

  // 获取指定节点详情
  getById: (id) => apiClient.get(`/areas/${id}`),

  // 获取子节点列表
  getChildren: (parentId) => apiClient.get(`/areas/parent/${parentId}`),

  // 新增库区
  create: (data) => apiClient.post('/areas', data),

  // 更新库区
  update: (id, data) => apiClient.put(`/areas/${id}`, data),

  // 删除库区
  delete: (id) => apiClient.delete(`/areas/${id}`),

  // 移动库区
  move: (id, targetParentId) => apiClient.post(`/areas/${id}/move`, { targetParentId }),

  // 批量操作
  batch: (data) => apiClient.post('/areas/batch', data),

  // 导出
  export: (params) => apiClient.get('/areas/export', { params, responseType: 'blob' }),

  // 导入
  import: (formData) => apiClient.post('/areas/import', formData, {
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
};

// 员工管理API接口
export const employeeApi = {
  // 分页查询员工列表
  getEmployeeList: (params, pageNum = 1, pageSize = 10) =>
    apiClient.get('/admin/employees', {
      params: {
        ...params,
        pageNum,
        pageSize
      }
    }),

  // 创建员工
  createEmployee: (data) => apiClient.post('/admin/employees', data),

  // 更新员工信息
  updateEmployee: (data) => apiClient.put('/admin/employees', data),

  // 更新员工状态
  updateEmployeeStatus: (userId, status) =>
    apiClient.patch(`/admin/employees/${userId}/status`, null, {
      params: { status }
    }),

  // 解绑员工微信账号
  unbindEmployeeWechat: (userId) =>
    apiClient.delete(`/admin/employees/${userId}/wechat-binding`)
};

export default apiClient