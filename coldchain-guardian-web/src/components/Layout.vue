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
import { ref, reactive } from 'vue';
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

// 当前活跃菜单项，从路由中动态获取
const activeMenu = ref(router.currentRoute.value.path.split('/')[1] || 'dashboard');

// 模拟用户信息
const userInfo = reactive({
  realName: '管理员'
});

// 检查是否为超级管理员
const isSuperAdmin = ref(false); // 可以从store或API获取实际值

// 退出登录
const logout = () => {
  localStorage.removeItem('token');
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