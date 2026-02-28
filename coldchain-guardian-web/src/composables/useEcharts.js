import { onMounted, onUnmounted, shallowRef } from 'vue'
import * as echarts from 'echarts/core'
import {
  LineChart
} from 'echarts/charts'
import {
  TitleComponent,
  TooltipComponent,
  GridComponent,
  LegendComponent,
  DataZoomComponent
} from 'echarts/components'
import { CanvasRenderer } from 'echarts/renderers'

// 注册ECharts模块
echarts.use([
  LineChart,
  TitleComponent,
  TooltipComponent,
  GridComponent,
  LegendComponent,
  DataZoomComponent,
  CanvasRenderer
])

export function useEcharts(containerRef) {
  const chartInstance = shallowRef(null)

  const initChart = () => {
    if (containerRef.value) {
      chartInstance.value = echarts.init(containerRef.value, null, {
        renderer: 'canvas',
        useDirtyRect: false
      })
    }
  }

  const setOption = (option) => {
    if (chartInstance.value) {
      chartInstance.value.setOption(option)
    }
  }

  const resize = () => {
    if (chartInstance.value) {
      chartInstance.value.resize()
    }
  }

  const dispose = () => {
    if (chartInstance.value) {
      chartInstance.value.dispose()
      chartInstance.value = null
    }
  }

  onMounted(() => {
    initChart()

    // 监听窗口大小变化，自动调整图表大小
    window.addEventListener('resize', resize)
  })

  onUnmounted(() => {
    dispose()
    window.removeEventListener('resize', resize)
  })

  return {
    chartInstance,
    setOption,
    resize,
    dispose
  }
}