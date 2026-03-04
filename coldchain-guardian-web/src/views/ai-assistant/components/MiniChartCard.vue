<template>
  <div class="mini-chart-card">
    <el-card shadow="hover" class="chart-card">
      <template #header>
        <div class="card-header">
          <span class="card-title">{{ data.title || '趋势分析图' }}</span>
        </div>
      </template>

      <div class="chart-container">
        <div ref="chartRef" class="chart"></div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, nextTick } from 'vue'
import * as echarts from 'echarts'

const props = defineProps({
  data: {
    type: Object,
    required: true
  }
})

const chartRef = ref(null)
let chartInstance = null

// 初始化图表
const initChart = async () => {
  await nextTick()
  if (chartRef.value) {
    chartInstance = echarts.init(chartRef.value)

    const option = {
      tooltip: {
        trigger: 'axis'
      },
      xAxis: {
        type: 'category',
        data: ['周一', '周二', '周三', '周四', '周五', '周六', '周日']
      },
      yAxis: {
        type: 'value',
        name: '温度 (°C)'
      },
      series: [{
        data: props.data.data || [],
        type: 'line',
        smooth: true,
        itemStyle: {
          color: '#409eff'
        }
      }]
    }

    chartInstance.setOption(option)
  }
}

// 更新图表
const updateChart = () => {
  if (chartInstance) {
    const option = {
      series: [{
        data: props.data.data || [],
        type: 'line',
        smooth: true,
        itemStyle: {
          color: '#409eff'
        }
      }]
    }
    chartInstance.setOption(option)
  }
}

onMounted(() => {
  initChart()

  // 监听窗口大小变化
  window.addEventListener('resize', () => {
    if (chartInstance) {
      chartInstance.resize()
    }
  })
})

onUnmounted(() => {
  if (chartInstance) {
    chartInstance.dispose()
  }
  window.removeEventListener('resize', () => {})
})

// 当props变化时更新图表
defineExpose({
  updateChart
})
</script>

<style scoped>
.mini-chart-card {
  margin-top: 16px;
}

.chart-card {
  border: 1px solid #ebeef5;
  border-radius: 8px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-title {
  font-weight: 600;
  color: #303133;
}

.chart-container {
  height: 200px;
}

.chart {
  width: 100%;
  height: 100%;
}
</style>