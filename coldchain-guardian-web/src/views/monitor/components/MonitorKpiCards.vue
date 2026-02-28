<template>
  <div class="kpi-cards-container">
    <el-row :gutter="20">
      <el-col :span="4">
        <el-card class="kpi-card">
          <div class="kpi-item" @click="$emit('cardClick', 'enabledDevices')">
            <div class="kpi-value">
              {{ summary.enabledDevices || '--' }}
            </div>
            <div class="kpi-label">启用设备数</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="4">
        <el-card class="kpi-card">
          <div class="kpi-item" @click="$emit('cardClick', 'onlineDevices')">
            <div class="kpi-value online">
              {{ summary.onlineDevices || '--' }}
            </div>
            <div class="kpi-label">在线设备数</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="4">
        <el-card class="kpi-card">
          <div class="kpi-item" @click="$emit('cardClick', 'offlineDevices')">
            <div class="kpi-value offline">
              {{ summary.offlineDevices || '--' }}
            </div>
            <div class="kpi-label">离线设备数</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="4">
        <el-card class="kpi-card">
          <div class="kpi-item" @click="$emit('cardClick', 'alarmingDevices')">
            <div class="kpi-value alarming">
              {{ summary.alarmingDevices || '--' }}
            </div>
            <div class="kpi-label">告警设备数</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="4">
        <el-card class="kpi-card">
          <div class="kpi-item" @click="$emit('cardClick', 'unhandledAlerts')">
            <div class="kpi-value unhandled">
              {{ summary.unhandledAlerts || '--' }}
            </div>
            <div class="kpi-label">未处理告警</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="4">
        <el-card class="kpi-card">
          <div class="kpi-item" @click="$emit('cardClick', 'todayAlerts')">
            <div class="kpi-value">
              {{ summary.todayAlerts || '--' }}
            </div>
            <div class="kpi-label">今日告警数</div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { defineProps, defineEmits } from 'vue'

defineProps({
  summary: {
    type: Object,
    default: () => ({})
  }
})

defineEmits(['cardClick'])
</script>

<style scoped>
.kpi-cards-container {
  margin-bottom: 20px;
}

.kpi-card {
  cursor: pointer;
  transition: transform 0.2s, box-shadow 0.2s;
}

.kpi-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.kpi-item {
  text-align: center;
  padding: 10px;
}

.kpi-value {
  font-size: 28px;
  font-weight: bold;
  color: #303133;
  margin-bottom: 8px;
}

.kpi-value.online {
  color: #67C23A;
}

.kpi-value.offline {
  color: #909399;
}

.kpi-value.alarming {
  color: #E6A23C;
}

.kpi-value.unhandled {
  color: #F56C6C;
}

.kpi-label {
  font-size: 14px;
  color: #909399;
}
</style>