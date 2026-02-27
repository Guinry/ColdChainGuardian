<template>
  <el-container class="layout-container">
    <!-- 侧边栏 -->
    <el-aside width="200px" class="sidebar">
      <div class="logo">
        <h3>冷链卫士</h3>
      </div>
      <el-menu
        :default-active="$route.path"
        :unique-opened="true"
        :collapse="isCollapse"
        router
        class="menu"
      >
        <el-menu-item index="/dashboard">
          <el-icon><House /></el-icon>
          <span>仪表盘</span>
        </el-menu-item>

        <el-sub-menu index="/manage">
          <template #title>
            <el-icon><Menu /></el-icon>
            <span>系统管理</span>
          </template>

          <el-menu-item v-if="hasPermission('area:view')" index="/warehouse-area">
            <el-icon><OfficeBuilding /></el-icon>
            <span>库区管理</span>
          </el-menu-item>

          <el-menu-item v-if="hasPermission('device:view')" index="/device">
            <el-icon><Monitor /></el-icon>
            <span>设备管理</span>
          </el-menu-item>
        </el-sub-menu>
      </el-menu>
    </el-aside>

    <!-- 主内容区域 -->
    <el-container>
      <el-header class="header">
        <el-button class="collapse-btn" @click="toggleCollapse" text>
          <el-icon><Fold v-if="!isCollapse" /><Expand v-else /></el-icon>
        </el-button>

        <div class="header-right">
          <el-dropdown>
            <span class="user-info">
              <el-avatar size="small" />
              <span>{{ userInfo.username }}</span>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item>个人资料</el-dropdown-item>
                <el-dropdown-item @click="logout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>

      <el-main class="main-content">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { ref, reactive, computed } from 'vue';
import { useRouter } from 'vue-router';
import { useAuthStore } from '@/store/auth';
import {
  House,
  Menu,
  Monitor,
  OfficeBuilding,
  Fold,
  Expand
} from '@element-plus/icons-vue';

const router = useRouter();
const authStore = useAuthStore();

// 检查权限的方法
const hasPermission = (permission) => {
  return authStore.hasPermission(permission);
};

// 侧边栏折叠状态
const isCollapse = ref(false);

// 模拟用户信息
const userInfo = reactive({
  username: 'admin'
});

// 切换侧边栏折叠状态
const toggleCollapse = () => {
  isCollapse.value = !isCollapse.value;
};

// 退出登录
const logout = () => {
  localStorage.removeItem('token');
  router.push('/login');
};
</script>

<style scoped>
.layout-container {
  height: 100vh;
}

.sidebar {
  background-color: #545c64;
  color: white;
}

.logo {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-bottom: 1px solid #444;
}

.logo h3 {
  margin: 0;
  color: white;
}

.menu:not(.el-menu--collapse) {
  width: 200px;
}

.header {
  background-color: white;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
  box-shadow: 0 1px 4px rgba(0, 21, 41, 0.08);
}

.collapse-btn {
  font-size: 18px;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
}

.main-content {
  background-color: #f0f2f5;
  padding: 20px;
}
</style>