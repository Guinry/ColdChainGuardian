<template>
  <div class="layout-container">
    <NavBar :user-info="userInfo" @view-profile="viewProfile" @settings="settings" @logout="logout" />
    <div class="layout-content">
      <SideBar :active-menu="activeMenu" :is-super-admin="isSuperAdmin" />
      <div class="main-content">
        <slot></slot>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'; // 引入 computed
import { useRouter } from 'vue-router';
import { useAuthStore } from '@/store/auth';
import NavBar from './NavBar.vue';
import SideBar from './SideBar.vue';

const router = useRouter();
const authStore = useAuthStore();

// 检查权限的方法
const hasPermission = (permission) => {
  return authStore.hasPermission(permission);
};

// 🌟 修复 1：使用 computed 监听路由变化，解决刷新或跳转后菜单不高亮的问题
const activeMenu = computed(() => {
  return router.currentRoute.value.path;
});

// 🌟 修复 2：使用 computed 监听用户信息，防止登录后没刷新拿不到名字
const userInfo = computed(() => {
  return {
    realName: authStore.user?.realName || authStore.user?.username || '访客'
  };
});

// 🌟 修复 3：使用 computed 实时监听角色变化，替代 onMounted
const isSuperAdmin = computed(() => {
  const role = authStore.user?.role;
  // 注意：如果你的后端角色前缀带 ROLE_，请在这里加上，例如 'ROLE_ADMIN'
  return role ? ['ADMIN', 'SUPER_ADMIN'].includes(role.toUpperCase()) : false;
});

// 退出登录
const logout = () => {
  authStore.clearAuthData();
  router.push('/login');
};

// 个人资料
const viewProfile = () => {
  console.log('View profile clicked');
};

// 系统设置
const settings = () => {
  console.log('Settings clicked');
};
</script>

<style scoped>
.layout-container {
  position: fixed;
  top: 0;
  left: 0;
  width: 100vw;
  height: 100vh;
  display: flex;
  flex-direction: column;
  background-color: #f5f7fa;
  box-sizing: border-box;
  overflow: hidden;
}

.layout-content {
  display: flex;
  flex: 1;
  overflow: hidden;
  min-height: 0;
}

.main-content {
  flex: 1;
  overflow-y: auto;
  min-height: 0;
  height: calc(100vh - 60px);
  background-color: #f5f7fa;
}
</style>