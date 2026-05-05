<template>
  <div class="side-menu">
    <el-menu
      :default-active="activeMenu"
      class="menu"
      :collapse="false"
      :unique-opened="true"
      :router="true"
    >
      <el-menu-item index="/dashboard">
        <el-icon><House /></el-icon>
        <span>Dashboard</span>
      </el-menu-item>

      <el-sub-menu index="monitoring">
        <template #title>
          <el-icon><Monitor /></el-icon>
          <span>监测管理</span>
        </template>
        <el-menu-item index="/warehouse-area">库区管理</el-menu-item>
        <el-menu-item index="/devices">设备管理</el-menu-item>
        <el-menu-item index="/monitor">实时监测</el-menu-item>
        <el-menu-item index="/settings/thresholds">阈值规则</el-menu-item>
      </el-sub-menu>

      <el-sub-menu index="alerts-orders">
        <template #title>
          <el-icon><Warning /></el-icon>
          <span>告警与工单</span>
        </template>
        <el-menu-item index="/alerts">告警中心</el-menu-item>
        <el-menu-item index="/work-orders">工单管理</el-menu-item>
      </el-sub-menu>

      <el-sub-menu index="analysis">
        <template #title>
          <el-icon><DataAnalysis /></el-icon>
          <span>数据分析</span>
        </template>
        <el-menu-item index="/trend-analysis">趋势分析</el-menu-item>
        <el-menu-item index="/ai-assistant">AI 智能助手</el-menu-item>
      </el-sub-menu>

      <!-- System Management menu visible for users with admin permissions -->
      <el-sub-menu v-if="showSystemManagement" index="system">
        <template #title>
          <el-icon><Setting /></el-icon>
          <span>系统管理</span>
        </template>
        <el-menu-item index="/employees">员工管理</el-menu-item>
        <el-menu-item index="/managers">管理员管理</el-menu-item>
        <el-menu-item index="/permissions">权限分配</el-menu-item>
        <el-menu-item index="/audit-logs">审计日志</el-menu-item>
      </el-sub-menu>
    </el-menu>
  </div>
</template>

<script setup>
import {
  House, Monitor, Warning, DataAnalysis, Setting
} from '@element-plus/icons-vue';
import { computed } from 'vue';

// Props
const props = defineProps({
  activeMenu: {
    type: String,
    default: 'dashboard'
  },
  isAdmin: {
    type: Boolean,
    default: false
  }
});

// 简化为直接使用传入的 isAdmin 属性
const showSystemManagement = computed(() => {
  return props.isAdmin;
});
</script>

<style scoped>
.side-menu {
  width: var(--ccg-sidebar-width);
  background-color: #fff;
  border-right: 1px solid var(--ccg-border);
  box-shadow: 4px 0 18px rgba(15, 23, 42, 0.04);
  overflow-y: auto;
  flex-shrink: 0;
  height: calc(100vh - var(--ccg-header-height));
  position: sticky;
  top: var(--ccg-header-height);
}

.menu {
  border-right: none;
  padding: 12px 10px;
}

.menu :deep(.el-menu-item),
.menu :deep(.el-sub-menu__title) {
  height: 42px;
  margin: 3px 0;
  border-radius: 8px;
  color: #334155;
}

.menu :deep(.el-menu-item.is-active) {
  background: var(--ccg-primary-soft);
  color: var(--ccg-primary);
  font-weight: 700;
}

.menu :deep(.el-sub-menu .el-menu-item) {
  height: 38px;
  min-width: 0;
  margin-left: 8px;
  padding-left: 32px !important;
}

.menu :deep(.el-sub-menu__title:hover),
.menu :deep(.el-menu-item:hover) {
  background: #f8fafc;
}

@keyframes glow {
  from {
    box-shadow: 0 0 5px rgba(64, 158, 255, 0.3);
  }
  to {
    box-shadow: 0 0 15px rgba(64, 158, 255, 0.6);
  }
}
</style>
