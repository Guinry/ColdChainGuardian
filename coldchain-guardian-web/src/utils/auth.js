import apiClient from '@/utils/api'

export const login = (credentials) => {
  return apiClient.post('/auth/login', credentials)
}

export const getUserInfo = () => {
  return apiClient.get('/auth/me')
}