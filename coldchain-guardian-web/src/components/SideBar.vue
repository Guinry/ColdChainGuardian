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
        <el-menu-item index="/analysis/trends">趋势分析</el-menu-item>
        <el-menu-item index="/analysis/ai">AI 智能助手</el-menu-item>
      </el-sub-menu>

      <!-- System Management menu only visible for SUPER_ADMIN -->
      <el-sub-menu v-if="isSuperAdmin" index="system">
        <template #title>
          <el-icon><Setting /></el-icon>
          <span>系统管理（超管）</span>
        </template>
        <el-menu-item index="/admin/users">管理员管理</el-menu-item>
        <el-menu-item index="/admin/permissions">权限分配</el-menu-item>
      </el-sub-menu>
    </el-menu>
  </div>
</template>

<script setup>
import {
  House, Monitor, Warning, DataAnalysis, Setting,
  Grid, Operation, Tickets, Memo
} from '@element-plus/icons-vue';

// Props
const props = defineProps({
  activeMenu: {
    type: String,
    default: 'dashboard'
  },
  isSuperAdmin: {
    type: Boolean,
    default: false
  }
});
</script>

<style scoped>
.side-menu {
  width: 200px;
  background-color: white;
  box-shadow: 2px 0 6px rgba(0, 21, 41, 0.35);
  overflow-y: auto;
  flex-shrink: 0;
  height: calc(100vh - 60px);
}

.menu {
  border-right: none;
}
</style>