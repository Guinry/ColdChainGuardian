<template>
  <div class="kpi-cards-container">
    <button class="kpi-card neutral" type="button" @click="$emit('cardClick', 'enabledDevices')">
      <span class="kpi-value">
              {{ summary.enabledDevices || '--' }}
      </span>
      <span class="kpi-label">启用设备</span>
    </button>
    <button class="kpi-card success" type="button" @click="$emit('cardClick', 'onlineDevices')">
      <span class="kpi-value online">
              {{ summary.onlineDevices || '--' }}
      </span>
      <span class="kpi-label">在线设备</span>
    </button>
    <button class="kpi-card muted" type="button" @click="$emit('cardClick', 'offlineDevices')">
      <span class="kpi-value offline">
              {{ summary.offlineDevices || '--' }}
      </span>
      <span class="kpi-label">离线设备</span>
    </button>
    <button class="kpi-card warning" type="button" @click="$emit('cardClick', 'alarmingDevices')">
      <span class="kpi-value alarming">
              {{ summary.alarmingDevices || '--' }}
      </span>
      <span class="kpi-label">告警设备</span>
    </button>
    <button class="kpi-card danger" type="button" @click="$emit('cardClick', 'unhandledAlerts')">
      <span class="kpi-value unhandled">
              {{ summary.unhandledAlerts || '--' }}
      </span>
      <span class="kpi-label">未处理告警</span>
    </button>
    <button class="kpi-card neutral" type="button" @click="$emit('cardClick', 'todayAlerts')">
      <span class="kpi-value">
              {{ summary.todayAlerts || '--' }}
      </span>
      <span class="kpi-label">今日告警</span>
    </button>
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
  display: grid;
  grid-template-columns: repeat(6, minmax(0, 1fr));
  gap: 14px;
  margin-bottom: 16px;
}

.kpi-card {
  min-width: 0;
  min-height: 96px;
  padding: 16px 14px;
  border: 1px solid var(--ccg-border);
  border-left: 4px solid #94a3b8;
  border-radius: 8px;
  background: #fff;
  text-align: center;
  cursor: pointer;
  box-shadow: var(--ccg-shadow-sm);
  transition: transform 0.18s, box-shadow 0.18s, border-color 0.18s;
}

.kpi-card:hover {
  transform: translateY(-1px);
  border-color: #cbd5e1;
  box-shadow: var(--ccg-shadow-md);
}

.kpi-card.success { border-left-color: var(--ccg-success); }
.kpi-card.warning { border-left-color: var(--ccg-warning); }
.kpi-card.danger { border-left-color: var(--ccg-danger); }
.kpi-card.muted { border-left-color: #94a3b8; }

.kpi-value {
  display: block;
  font-size: 28px;
  line-height: 1;
  font-weight: 750;
  color: var(--ccg-text);
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
  display: block;
  font-size: 14px;
  color: var(--ccg-muted);
}

@media (max-width: 1280px) {
  .kpi-cards-container {
    grid-template-columns: repeat(3, minmax(0, 1fr));
  }
}
</style>
