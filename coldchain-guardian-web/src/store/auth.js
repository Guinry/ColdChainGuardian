import { defineStore } from 'pinia'

export const useAuthStore = defineStore('auth', {
  state: () => ({
    token: localStorage.getItem('token') || sessionStorage.getItem('token') || null,
    user: (() => {
      try {
        const userData = localStorage.getItem('user') || sessionStorage.getItem('user');
        return userData ? JSON.parse(userData) : null;
      } catch (e) {
        console.error('Failed to parse user data:', e);
        return null;
      }
    })(),
    permissions: (() => {
      try {
        const permissionsData = localStorage.getItem('permissions') || sessionStorage.getItem('permissions');
        return permissionsData ? JSON.parse(permissionsData) : [];
      } catch (e) {
        console.error('Failed to parse permissions data:', e);
        return [];
      }
    })()
  }),

  getters: {
    isAuthenticated: (state) => !!state.token,
    getUserRole: (state) => state.user?.role || null,
    hasPermission: (state) => (permission) => {
      return state.permissions.includes(permission)
    }
  },

  actions: {
    setAuthData(data, rememberMe = false) {
      const storage = rememberMe ? localStorage : sessionStorage

      this.token = data.token
      this.user = data.user
      this.permissions = data.permissions || []

      storage.setItem('token', data.token)
      storage.setItem('user', JSON.stringify(data.user))
      storage.setItem('permissions', JSON.stringify(data.permissions || []))
    },

    clearAuthData() {
      localStorage.removeItem('token')
      localStorage.removeItem('user')
      localStorage.removeItem('permissions')
      sessionStorage.removeItem('token')
      sessionStorage.removeItem('user')
      sessionStorage.removeItem('permissions')

      this.token = null
      this.user = null
      this.permissions = []
    },

    updateUser(userData) {
      this.user = { ...this.user, ...userData }

      // Update storage
      if (localStorage.getItem('user')) {
        localStorage.setItem('user', JSON.stringify(this.user))
      } else {
        sessionStorage.setItem('user', JSON.stringify(this.user))
      }
    }
  }
})