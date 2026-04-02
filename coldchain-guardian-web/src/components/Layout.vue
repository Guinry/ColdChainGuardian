<template>
  <div class="layout-container">
    <NavBar :user-info="userInfo" @view-profile="viewProfile" @settings="settings" @logout="logout" />
    <div class="layout-content">
      <SideBar :active-menu="activeMenu" :is-admin="isAdmin" />
      <div class="main-content">
        <slot></slot>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue';
import { useRouter } from 'vue-router';
import { useAuthStore } from '@/store/auth';
import NavBar from './NavBar.vue';
import SideBar from './SideBar.vue';

const router = useRouter();
const authStore = useAuthStore();

const activeMenu = computed(() => router.currentRoute.value.path);

const userInfo = computed(() => ({
  realName: authStore.user?.realName || authStore.user?.username || '访客'
}));

// 🌟 简化为直接判断 role 是否为 ADMIN
const isAdmin = computed(() => {
  return authStore.user?.role === 'ADMIN';
});

const logout = () => {
  authStore.clearAuthData();
  router.push('/login');
};

const viewProfile = () => {
  router.push('/profile'); // 🌟 跳向刚写的个人资料页
};
const settings = () => console.log('Settings clicked');
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