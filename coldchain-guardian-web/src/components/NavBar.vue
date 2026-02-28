<template>
  <div class="top-bar">
    <div class="logo-section">
      <div class="logo">
        <svg viewBox="0 0 24 24" width="32" height="32" fill="none" xmlns="http://www.w3.org/2000/svg">
          <path d="M12 2L13.09 8.26L22 9L13.09 9.74L12 16L10.91 9.74L2 9L10.91 8.26L12 2Z" fill="#409EFF"/>
          <circle cx="12" cy="12" r="10" stroke="#409EFF" stroke-width="2"/>
        </svg>
      </div>
      <span class="app-title">ColdChain Guardian</span>
    </div>

    <div class="search-section">
      <el-input
        v-model="globalSearch"
        placeholder="全局搜索..."
        :prefix-icon="Search"
        class="global-search"
      />
    </div>

    <div class="action-section">
      <el-badge :value="unreadNotifications" class="notification-badge">
        <el-button circle class="notification-btn">
          <el-icon><Bell /></el-icon>
        </el-button>
      </el-badge>

      <el-dropdown>
        <div class="user-avatar">
          <el-avatar :size="32" :src="userAvatar">{{ userInitial }}</el-avatar>
          <span class="user-name">{{ userInfo.realName }}</span>
          <el-icon><ArrowDown /></el-icon>
        </div>
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item @click="viewProfile">个人资料</el-dropdown-item>
            <el-dropdown-item @click="settings">系统设置</el-dropdown-item>
            <el-dropdown-item divided @click="logout">退出登录</el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue';
import {
  Search, Bell, ArrowDown
} from '@element-plus/icons-vue';

// Props
defineProps({
  userInfo: {
    type: Object,
    default: () => ({
      realName: '管理员'
    })
  }
});

// Emits
const emit = defineEmits(['view-profile', 'settings', 'logout']);

// Reactive variables
const globalSearch = ref('');
const unreadNotifications = ref(3);

// Computed properties
const userInitial = computed(() => {
  return props.userInfo.realName ? props.userInfo.realName.charAt(0) : 'U';
});

const userAvatar = computed(() => {
  return 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png';
});

// Methods
const viewProfile = () => {
  emit('view-profile');
};

const settings = () => {
  emit('settings');
};

const logout = () => {
  emit('logout');
};
</script>

<style scoped>
.top-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  height: 60px;
  background-color: white;
  box-shadow: 0 1px 4px rgba(0, 21, 41, 0.08);
  z-index: 10;
  flex-shrink: 0;
}

.logo-section {
  display: flex;
  align-items: center;
}

.logo {
  margin-right: 12px;
}

.app-title {
  font-size: 18px;
  font-weight: bold;
  color: #303133;
}

.search-section {
  flex: 1;
  max-width: 400px;
  margin: 0 40px;
}

.global-search {
  width: 100%;
}

.action-section {
  display: flex;
  align-items: center;
  gap: 20px;
}

.notification-badge {
  margin-right: 20px;
}

.notification-btn {
  border: none;
}

.user-avatar {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
}

.user-name {
  font-size: 14px;
  color: #606266;
}
</style>