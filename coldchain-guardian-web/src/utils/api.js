import axios from 'axios'

// Create an Axios instance
const apiClient = axios.create({
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

export default apiClient