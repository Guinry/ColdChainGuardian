<template>
  <div class="kpi-card" :class="{ 'highlight': highlight }">
    <div class="kpi-header">
      <div class="kpi-icon" :class="iconClass">
        <el-icon><component :is="iconComponent" /></el-icon>
      </div>
      <div class="kpi-info">
        <div class="kpi-title">{{ title }}</div>
        <div class="kpi-trend" :class="trendClass">
          <el-icon><component :is="trendIconComponent" /></el-icon>{{ trendText }}
        </div>
      </div>
    </div>
    <div class="kpi-value">{{ value }}</div>
    <div v-if="subtitle" class="kpi-subtitle">{{ subtitle }}</div>

    <!-- 微缩图 -->
    <div v-if="showSparkline && sparklineData && sparklineData.length > 0" class="sparkline-container">
      <canvas
        ref="sparklineCanvas"
        class="sparkline-canvas"
        :width="sparklineWidth"
        :height="sparklineHeight">
      </canvas>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick, watch, computed } from 'vue'
import {
  Monitor,
  Warning,
  DocumentChecked,
  StarFilled,
  ArrowUp,
  ArrowDown
} from '@element-plus/icons-vue'

// Props
const props = defineProps({
  title: {
    type: String,
    required: true
  },
  value: {
    type: [String, Number],
    required: true
  },
  subtitle: {
    type: String,
    default: ''
  },
  icon: {
    type: String,
    default: 'StarFilled'
  },
  iconClass: {
    type: String,
    default: 'default-icon'
  },
  trendText: {
    type: String,
    default: ''
  },
  trendIcon: {
    type: String,
    default: 'ArrowUp'
  },
  trendClass: {
    type: String,
    default: ''
  },
  sparklineData: {
    type: Array,
    default: () => []
  },
  showSparkline: {
    type: Boolean,
    default: true
  },
  sparklineWidth: {
    type: Number,
    default: 100
  },
  sparklineHeight: {
    type: Number,
    default: 30
  },
  highlight: {
    type: Boolean,
    default: false
  }
})

// 将图标类名转换为组件
const iconComponent = computed(() => {
  // 移除 'el-icon-' 前缀，提取图标名称并转换为首字母大写格式
  let iconName = props.icon.replace('el-icon-', '')
  if (iconName.startsWith('el-')) {
    iconName = iconName.substring(3) // 移除 'el-' 前缀
  }

  // 特殊处理一些常见图标名称映射
  const iconMap = {
    'monitor': Monitor,
    'warning': Warning,
    'document-checked': DocumentChecked,
    'star-filled': StarFilled,
    'arrow-up': ArrowUp,
    'arrow-down': ArrowDown,
    // 处理驼峰命名
    'Monitor': Monitor,
    'Warning': Warning,
    'DocumentChecked': DocumentChecked,
    'StarFilled': StarFilled,
    'ArrowUp': ArrowUp,
    'ArrowDown': ArrowDown
  }

  return iconMap[iconName] || StarFilled
})

const trendIconComponent = computed(() => {
  let iconName = props.trendIcon.replace('el-icon-', '')
  if (iconName.startsWith('el-')) {
    iconName = iconName.substring(3) // 移除 'el-' 前缀
  }

  // 特殊处理一些常见图标名称映射
  const iconMap = {
    'monitor': Monitor,
    'warning': Warning,
    'document-checked': DocumentChecked,
    'star-filled': StarFilled,
    'arrow-up': ArrowUp,
    'arrow-down': ArrowDown,
    'ArrowUp': ArrowUp,
    'ArrowDown': ArrowDown
  }

  return iconMap[iconName] || ArrowUp
})

// Refs
const sparklineCanvas = ref(null)

// Draw sparkline
const drawSparkline = () => {
  if (!sparklineCanvas.value || !props.sparklineData || props.sparklineData.length === 0) {
    return
  }

  const canvas = sparklineCanvas.value
  const ctx = canvas.getContext('2d')
  const data = props.sparklineData

  // Clear canvas
  ctx.clearRect(0, 0, canvas.width, canvas.height)

  // Calculate dimensions
  const padding = 3
  const chartWidth = canvas.width - 2 * padding
  const chartHeight = canvas.height - 2 * padding
  const xStep = chartWidth / (data.length - 1)

  // Find min/max values for scaling
  const maxValue = Math.max(...data)
  const minValue = Math.min(...data)
  const valueRange = maxValue - minValue || 1

  // Determine color based on trend
  const color = props.trendClass.includes('up') ? '#67C23A' : '#F56C6C'

  // Draw the line
  ctx.beginPath()
  ctx.strokeStyle = color
  ctx.lineWidth = 1.5

  data.forEach((value, i) => {
    const x = padding + i * xStep
    const y = padding + chartHeight - ((value - minValue) / valueRange) * chartHeight

    if (i === 0) {
      ctx.moveTo(x, y)
    } else {
      ctx.lineTo(x, y)
    }
  })

  ctx.stroke()

  // Draw fill area (gradient)
  const gradient = ctx.createLinearGradient(0, 0, 0, canvas.height)
  gradient.addColorStop(0, color + '40') // Add alpha channel
  gradient.addColorStop(1, color + '10')

  // Complete the path to create filled area
  ctx.lineTo(canvas.width - padding, canvas.height - padding)
  ctx.lineTo(padding, canvas.height - padding)
  ctx.closePath()
  ctx.fillStyle = gradient
  ctx.fill()
}

// Redraw when data changes
watch(() => props.sparklineData, () => {
  nextTick(() => {
    drawSparkline()
  })
}, { immediate: true })

// Initialize on mount
onMounted(() => {
  nextTick(() => {
    drawSparkline()
  })
})
</script>

<style scoped>
.kpi-card {
  height: 120px;
  background: white;
  border-radius: 8px;
  padding: 15px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  transition: all 0.3s ease;
  position: relative;
  overflow: hidden;
}

.kpi-card.highlight {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.12);
}

.kpi-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.12);
}

.kpi-header {
  display: flex;
  align-items: center;
  margin-bottom: 8px;
}

.kpi-icon {
  width: 40px;
  height: 40px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 12px;
  font-size: 20px;
  color: white;
}

.default-icon {
  background: linear-gradient(135deg, #409EFF, #79bbff);
}

.temp-icon {
  background: linear-gradient(135deg, #409EFF, #79bbff);
}

.device-icon {
  background: linear-gradient(135deg, #67C23A, #85ce61);
}

.alert-icon {
  background: linear-gradient(135deg, #F56C6C, #f78989);
}

.work-icon {
  background: linear-gradient(135deg, #909399, #b1b3b8);
}

.kpi-info {
  flex: 1;
}

.kpi-title {
  font-size: 14px;
  color: #606266;
  margin-bottom: 4px;
}

.kpi-trend {
  font-size: 12px;
  display: flex;
  align-items: center;
  gap: 4px;
}

.kpi-trend.trend-up {
  color: #67C23A;
}

.kpi-trend.trend-down {
  color: #F56C6C;
}

.kpi-trend.danger {
  color: #F56C6C;
}

.kpi-value {
  font-size: 24px;
  font-weight: bold;
  color: #303133;
  margin-bottom: 4px;
}

.kpi-subtitle {
  font-size: 12px;
  color: #909399;
}

.sparkline-container {
  position: absolute;
  bottom: 10px;
  right: 15px;
  width: 100px;
  height: 30px;
}

.sparkline-canvas {
  width: 100%;
  height: 100%;
}
</style>