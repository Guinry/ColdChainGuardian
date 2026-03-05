<template>
  <div class="analysis-sidebar">
    <el-collapse v-model="activeNames" accordion>
      <el-collapse-item title="高级筛选" name="1">
        <el-form :model="filterForm" label-position="top">
          <el-form-item label="区域">
            <el-select v-model="filterForm.region" placeholder="请选择区域" multiple clearable>
              <el-option label="华北区" value="north"></el-option>
              <el-option label="华东区" value="east"></el-option>
              <el-option label="华南区" value="south"></el-option>
              <el-option label="西南区" value="southwest"></el-option>
              <el-option label="西北区" value="northwest"></el-option>
            </el-select>
          </el-form-item>

          <el-form-item label="设备类型">
            <el-select v-model="filterForm.deviceType" placeholder="请选择设备类型" multiple clearable>
              <el-option label="温湿度传感器" value="temp_humidity"></el-option>
              <el-option label="压力传感器" value="pressure"></el-option>
              <el-option label="流量计" value="flow"></el-option>
              <el-option label="摄像头" value="camera"></el-option>
            </el-select>
          </el-form-item>

          <el-form-item label="告警等级">
            <el-checkbox-group v-model="filterForm.alertLevels">
              <el-checkbox value="critical">严重</el-checkbox>
              <el-checkbox value="high">高危</el-checkbox>
              <el-checkbox value="medium">中等</el-checkbox>
              <el-checkbox value="low">低危</el-checkbox>
            </el-checkbox-group>
          </el-form-item>

          <el-form-item>
            <el-button type="primary" @click="applyFilters">应用筛选</el-button>
            <el-button @click="resetFilters">重置</el-button>
          </el-form-item>
        </el-form>
      </el-collapse-item>

      <el-collapse-item title="对比分析" name="2">
        <el-form :model="comparisonForm" label-position="top">
          <el-form-item label="对比维度">
            <el-radio-group v-model="comparisonForm.dimension">
              <el-radio label="time">时间对比</el-radio>
              <el-radio label="region">区域对比</el-radio>
              <el-radio label="device">设备对比</el-radio>
            </el-radio-group>
          </el-form-item>

          <el-form-item label="对比对象" v-if="comparisonForm.dimension === 'time'">
            <el-date-picker
              v-model="comparisonForm.compareDates"
              type="daterange"
              range-separator="至"
              start-placeholder="开始日期"
              end-placeholder="结束日期">
            </el-date-picker>
          </el-form-item>

          <el-form-item v-if="comparisonForm.dimension === 'region'">
            <el-select v-model="comparisonForm.regions" multiple placeholder="选择要对比的区域">
              <el-option label="华北区" value="north"></el-option>
              <el-option label="华东区" value="east"></el-option>
              <el-option label="华南区" value="south"></el-option>
              <el-option label="西南区" value="southwest"></el-option>
              <el-option label="西北区" value="northwest"></el-option>
            </el-select>
          </el-form-item>

          <el-form-item>
            <el-button type="primary" @click="applyComparison">应用对比</el-button>
          </el-form-item>
        </el-form>
      </el-collapse-item>

      <el-collapse-item title="数据导出" name="3">
        <div class="export-section">
          <el-button type="success" icon="Download" @click="exportData('pdf')">
            导出PDF报告
          </el-button>
          <el-button type="primary" icon="Document" @click="exportData('excel')">
            导出Excel数据
          </el-button>
          <el-button type="info" icon="Printer" @click="printReport">
            打印报表
          </el-button>
        </div>
      </el-collapse-item>

      <el-collapse-item title="智能分析" name="4">
        <div class="ai-analysis">
          <p>AI助手将为您分析数据趋势和模式</p>
          <el-button type="warning" icon="ChatLineRound" @click="requestAiAnalysis">
            请求AI分析
          </el-button>
        </div>
      </el-collapse-item>

      <el-collapse-item title="统计概览" name="5">
        <div class="stats-overview">
          <el-row :gutter="10">
            <el-col :span="12">
              <el-statistic title="总告警数" :value="totalAlerts" />
            </el-col>
            <el-col :span="12">
              <el-statistic title="总工单数" :value="totalWorkOrders" />
            </el-col>
          </el-row>
          <el-divider />
          <el-row :gutter="10">
            <el-col :span="12">
              <el-statistic title="设备总数" :value="totalDevices" />
            </el-col>
            <el-col :span="12">
              <el-statistic title="平均温度" :value="avgTemperature" suffix="°C" />
            </el-col>
          </el-row>
          <el-divider />
          <el-progress :percentage="deviceOnlineRate" :stroke-width="12" status="success">
            <span>{{ deviceOnlineRate }}% 在线率</span>
          </el-progress>
        </div>
      </el-collapse-item>

      <el-collapse-item title="实时监控" name="6">
        <div class="realtime-monitor">
          <el-descriptions :column="1" size="small" border>
            <el-descriptions-item label="当前时间">
              {{ currentTime }}
            </el-descriptions-item>
            <el-descriptions-item label="数据更新">
              {{ lastUpdate }}
            </el-descriptions-item>
            <el-descriptions-item label="系统状态">
              <el-tag type="success" size="small">正常运行</el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="内存使用">
              <el-progress :percentage="45" :stroke-width="8" />
            </el-descriptions-item>
          </el-descriptions>
        </div>
      </el-collapse-item>
    </el-collapse>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted } from 'vue'
import { Download, Document, Printer, ChatLineRound } from '@element-plus/icons-vue'

// Active collapse panel
const activeNames = ref(['1'])

// Stats data
const totalAlerts = ref(124)
const totalWorkOrders = ref(87)
const totalDevices = ref(142)
const avgTemperature = ref(4.2)
const deviceOnlineRate = ref(98.5)

// Time data
const currentTime = ref(new Date().toLocaleString())
const lastUpdate = ref(new Date().toLocaleTimeString())

// Filter form
const filterForm = reactive({
  region: [],
  deviceType: [],
  alertLevels: []
})

// Comparison form
const comparisonForm = reactive({
  dimension: 'time',
  compareDates: [],
  regions: []
})

// Timer for updating time
let timeTimer = null

// Apply filters
const applyFilters = () => {
  console.log('Applying filters:', filterForm)
  // 发送过滤事件给父组件
  emit('filter-applied', filterForm)
}

// Reset filters
const resetFilters = () => {
  filterForm.region = []
  filterForm.deviceType = []
  filterForm.alertLevels = []
  emit('filter-reset')
}

// Apply comparison
const applyComparison = () => {
  console.log('Applying comparison:', comparisonForm)
  emit('comparison-applied', comparisonForm)
}

// Export data
const exportData = (type) => {
  console.log(`Exporting ${type} data`)
  emit('export-requested', type)
}

// Print report
const printReport = () => {
  console.log('Printing report')
  emit('print-requested')
}

// Request AI analysis
const requestAiAnalysis = () => {
  console.log('Requesting AI analysis')
  emit('ai-analysis-requested')
}

// Update time
const updateTime = () => {
  currentTime.value = new Date().toLocaleString()
  lastUpdate.value = new Date().toLocaleTimeString()
}

// Emits
const emit = defineEmits([
  'filter-applied',
  'filter-reset',
  'comparison-applied',
  'export-requested',
  'print-requested',
  'ai-analysis-requested'
])

// Lifecycle hooks
onMounted(() => {
  // Start timer to update time
  timeTimer = setInterval(updateTime, 1000)
})

onUnmounted(() => {
  // Clear timer
  if (timeTimer) {
    clearInterval(timeTimer)
  }
})
</script>

<style scoped>
.analysis-sidebar {
  padding: 20px;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  height: calc(100vh - 100px);
  overflow-y: auto;
}

.export-section {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.ai-analysis {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.ai-analysis p {
  color: #909399;
  font-size: 14px;
  margin: 0;
}

.stats-overview {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.realtime-monitor {
  display: flex;
  flex-direction: column;
  gap: 10px;
}
</style>