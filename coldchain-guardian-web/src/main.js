import { createApp } from 'vue'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import './assets/main.css'

import App from './App.vue'
import router from './router'
import { useAuthStore } from './store/auth'

const app = createApp(App)
const pinia = createPinia()

// Register all icons
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

app.use(pinia)
app.use(router)
app.use(ElementPlus)

// 全局路由守卫
router.beforeEach((to, from, next) => {
  const authStore = useAuthStore()

  // 检查是否需要认证
  if (to.meta.requiresAuth) {
    if (authStore.isAuthenticated) {
      // 检查角色权限
      if (to.meta.roles) {
        const userRole = authStore.user?.role
        const hasRoleAccess = to.meta.roles.includes(userRole)

        if (hasRoleAccess) {
          next()
        } else {
          // 如果没有权限，重定向到首页或显示无权限页面
          console.warn(`用户角色 ${userRole} 没有访问 ${to.path} 的权限`)
          next('/dashboard') // 重定向到仪表板而不是首页，提供更好的用户体验
        }
      } else {
        next()
      }
    } else {
      // 未认证用户重定向到登录页
      next('/login')
    }
  } else {
    // 不需要认证的页面直接通过
    next()
  }
})

app.mount('#app')
