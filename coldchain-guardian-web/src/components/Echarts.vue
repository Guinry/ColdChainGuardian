<template>
  <div class="chart-container" ref="chartContainerRef" :style="{ height: height, width: width }"></div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, nextTick, watch } from 'vue'
import * as echarts from 'echarts'
import { debounce } from 'lodash-es'

// Props
const props = defineProps({
  option: {
    type: Object,
    required: true
  },
  width: {
    type: String,
    default: '100%'
  },
  height: {
    type: String,
    default: '400px'
  },
  theme: {
    type: String,
    default: 'light'
  }
})

// Chart instance
let chartInstance = null
const chartContainerRef = ref(null)

// Initialize chart
const initChart = () => {
  if (!chartContainerRef.value) return

  // Dispose existing chart instance if exists
  if (chartInstance) {
    chartInstance.dispose()
  }

  // Initialize new chart instance
  chartInstance = echarts.init(chartContainerRef.value, props.theme)
  chartInstance.setOption(props.option)

  // Add resize listener
  window.addEventListener('resize', debounce(() => {
    if (chartInstance) {
      chartInstance.resize()
    }
  }, 200))
}

// Watch for option changes
watch(() => props.option, () => {
  nextTick(() => {
    if (chartInstance) {
      chartInstance.setOption(props.option, { notMerge: false })
    }
  })
}, { deep: true })

// Lifecycle hooks
onMounted(() => {
  nextTick(() => {
    initChart()
  })
})

onUnmounted(() => {
  if (chartInstance) {
    chartInstance.dispose()
    chartInstance = null
  }
})
</script>

<style scoped>
.chart-container {
  width: 100%;
  height: 100%;
}
</style>