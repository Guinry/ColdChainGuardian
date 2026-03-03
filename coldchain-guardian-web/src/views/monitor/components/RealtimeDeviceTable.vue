<template>
  <div class="device-table-container">
    <!-- 筛选栏 -->
    <el-form :model="localFilters" inline class="filter-form">
      <el-row :gutter="12">
        <el-col :span="6">
          <el-form-item label="关键字">
            <el-input
              v-model="localFilters.keyword"
              placeholder="设备编码/名称"
              clearable
              @keyup.enter="handleFilterChange"
            />
          </el-form-item>
        </el-col>
        <el-col :span="4.5">
          <el-form-item label="在线状态">
            <el-select
              v-model="localFilters.online"
              placeholder="全部"
              clearable
              @change="handleFilterChange"
            >
              <el-option label="在线" :value="true" />
              <el-option label="离线" :value="false" />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="4.5">
          <el-form-item label="告警状态">
            <el-select
              v-model="localFilters.alarming"
              placeholder="全部"
              clearable
              @change="handleFilterChange"
            >
              <el-option label="有未处理" :value="true" />
              <el-option label="无" :value="false" />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="4.5">
          <el-form-item label="设备类型">
            <el-select
              v-model="localFilters.deviceType"
              placeholder="全部"
              clearable
              @change="handleFilterChange"
            >
              <el-option label="温湿度传感器" value="TEMP_HUM" />
              <el-option label="冷柜" value="FREEZER" />
              <el-option label="车载设备" value="VEHICLE" />
              <el-option label="门磁" value="DOOR" />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="4.5">
          <el-form-item>
            <el-button type="primary" @click="handleFilterChange" :icon="Search">查询</el-button>
            <el-button @click="resetFilters">重置</el-button>
          </el-form-item>
        </el-col>
      </el-row>
    </el-form>

    <!-- 表格 -->
    <el-table
      :data="data"
      :loading="loading"
      style="width: 100%"
      row-key="id"
      border
      stripe
      :header-cell-style="{ background: '#f8f9ff', color: '#606266' }"
    >
      <el-table-column prop="deviceName" label="设备信息" min-width="150">
        <template #default="{ row }">
          <div class="device-info">
            <div class="device-name">{{ row.deviceName }}</div>
            <div class="device-code">{{ row.deviceCode }}</div>
          </div>
        </template>
      </el-table-column>

      <el-table-column prop="areaPath" label="所属库区" min-width="180" show-overflow-tooltip>
        <template #default="{ row }">
          <el-link type="primary" @click="goToArea(row.areaId)" :underline="'never'">
            <el-icon><Location /></el-icon>
            {{ row.areaPath }}
          </el-link>
        </template>
      </el-table-column>

      <el-table-column prop="latestTemp" label="最新温度(℃)" min-width="120" sortable>
        <template #default="{ row }">
          <div :class="['temp-value', getTempClass(row.latestTemp, row.temperatureThresholdMin, row.temperatureThresholdMax)]">
            <span v-if="row.latestTemp !== null">{{ row.latestTemp }}℃</span>
            <span v-else class="no-data">-</span>
            <el-icon v-if="isNearThreshold(row.latestTemp, row.temperatureThresholdMin, row.temperatureThresholdMax, 'temp')" class="warning-icon"><Warning /></el-icon>
            <el-icon v-else-if="isOverThreshold(row.latestTemp, row.temperatureThresholdMin, row.temperatureThresholdMax, 'temp')" class="danger-icon"><CircleCloseFilled /></el-icon>
          </div>
        </template>
      </el-table-column>

      <el-table-column prop="latestHumi" label="最新湿度(%)" min-width="120" sortable>
        <template #default="{ row }">
          <div :class="['humi-value', getHumiClass(row.latestHumi, row.humidityThresholdMin, row.humidityThresholdMax)]">
            <span v-if="row.latestHumi !== null">{{ row.latestHumi }}%</span>
            <span v-else class="no-data">-</span>
            <el-icon v-if="isNearThreshold(row.latestHumi, row.humidityThresholdMin, row.humidityThresholdMax, 'humi')" class="warning-icon"><Warning /></el-icon>
            <el-icon v-else-if="isOverThreshold(row.latestHumi, row.humidityThresholdMin, row.humidityThresholdMax, 'humi')" class="danger-icon"><CircleCloseFilled /></el-icon>
          </div>
        </template>
      </el-table-column>

      <el-table-column prop="latestDataTime" label="数据时间" min-width="140" sortable>
        <template #default="{ row }">
          <div class="time-cell">
            <span v-if="row.latestDataTime">{{ formatDate(row.latestDataTime) }}</span>
            <span v-else class="no-data">未上报</span>
          </div>
        </template>
      </el-table-column>

      <el-table-column prop="online" label="在线状态" min-width="90" align="center">
        <template #default="{ row }">
          <el-tag :type="row.online ? 'success' : 'danger'" size="small">
            {{ row.online ? '在线' : '离线' }}
          </el-tag>
        </template>
      </el-table-column>

      <el-table-column prop="hasUnresolvedAlert" label="告警状态" min-width="120" align="center">
        <template #default="{ row }">
          <el-tag
            v-if="row.hasUnresolvedAlert"
            :type="getAlertLevelType(row.highestAlertLevel)"
            size="small"
            @click="openAlertDrawer(row)"
            class="alert-status"
          >
            {{ getAlertLevelText(row.highestAlertLevel) }}
          </el-tag>
          <span v-else class="normal-status">正常</span>
        </template>
      </el-table-column>

      <el-table-column label="操作" min-width="180" fixed="right" align="center">
        <template #default="{ row }">
          <el-button size="small" @click="$emit('viewDetail', row)">详情</el-button>
          <el-button size="small" type="primary" @click="$emit('viewTrend', row)">曲线</el-button>
          <el-popconfirm
            title="确定要查看告警吗？"
            @confirm="$emit('viewAlert', row)"
          >
            <template #reference>
              <el-button size="small" type="warning">告警</el-button>
            </template>
          </el-popconfirm>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页 -->
    <el-pagination
      class="pagination-container"
      v-model:currentPage="localPagination.currentPage"
      v-model:pageSize="localPagination.pageSize"
      :page-sizes="[10, 20, 50, 100]"
      :page-size="localPagination.pageSize"
      layout="total, sizes, prev, pager, next, jumper"
      :total="localPagination.total"
      @size-change="handleSizeChange"
      @current-change="handlePageChange"
    />
  </div>
</template>

<script setup>
import { ref, reactive, defineProps, defineEmits, watch } from 'vue'
import { Search, Location, Warning, CircleCloseFilled } from '@element-plus/icons-vue'

const props = defineProps({
  data: {
    type: Array,
    default: () => []
  },
  loading: {
    type: Boolean,
    default: false
  },
  pagination: {
    type: Object,
    default: () => ({ currentPage: 1, pageSize: 20, total: 0 })
  },
  filters: {
    type: Object,
    default: () => ({})
  }
})

const emit = defineEmits([
  'pageChange',
  'sizeChange',
  'filterChange',
  'refresh',
  'viewDetail',
  'viewTrend',
  'viewAlert'
])

// 使用响应式变量保存本地副本
const localPagination = reactive({ ...props.pagination })
const localFilters = reactive({ ...props.filters })

// 监听外部pagination的变化
watch(() => props.pagination, (newVal) => {
  Object.assign(localPagination, newVal)
}, { deep: true })

// 监听外部filters的变化
watch(() => props.filters, (newVal) => {
  Object.assign(localFilters, newVal)
}, { deep: true })

// 处理分页变化
const handlePageChange = (page) => {
  emit('pageChange', page)
}

const handleSizeChange = (size) => {
  emit('sizeChange', size)
}

// 处理筛选变化
const handleFilterChange = () => {
  emit('filterChange')
}

// 重置筛选
const resetFilters = () => {
  // 重置本地filters
  localFilters.keyword = ''
  localFilters.online = null
  localFilters.alarming = null
  localFilters.deviceType = ''

  // 重置父级组件的filters
  const resetFiltersObj = {
    keyword: '',
    online: null,
    alarming: null,
    deviceType: ''
  }
  Object.assign(props.filters, resetFiltersObj)

  emit('filterChange')
}

// 格式化时间
const formatDate = (dateStr) => {
  if (!dateStr) return '-'
  const date = new Date(dateStr)
  const now = new Date()
  const diffSeconds = Math.floor((now - date) / 1000)

  if (diffSeconds < 60) {
    return `${diffSeconds}秒前`
  } else if (diffSeconds < 3600) {
    const diffMinutes = Math.floor(diffSeconds / 60)
    return `${diffMinutes}分钟前`
  } else if (diffSeconds < 86400) {
    const diffHours = Math.floor(diffSeconds / 3600)
    return `${diffHours}小时前`
  } else {
    return date.toLocaleString()
  }
}

// 温度阈值判断相关函数
const isOverThreshold = (value, min, max) => {
  if (value === null || value === undefined) return false
  return (min !== null && value < min) || (max !== null && value > max)
}

const isNearThreshold = (value, min, max) => {
  if (value === null || value === undefined) return false
  // 接近阈值：差值小于 0.5 (也可以根据温湿度分别设置不同的容差，比如湿度容差2%)
  return (min !== null && Math.abs(value - min) < 0.5) ||
         (max !== null && Math.abs(value - max) < 0.5)
}

const getTempClass = (temp, min, max) => {
  if (isOverThreshold(temp, min, max)) return 'over-threshold'
  if (isNearThreshold(temp, min, max)) return 'near-threshold'
  return temp !== null ? 'normal-temp' : 'no-data'
}

const getHumiClass = (humi, min, max) => {
  if (isOverThreshold(humi, min, max)) return 'over-threshold'
  if (isNearThreshold(humi, min, max)) return 'near-threshold'
  return humi !== null ? 'normal-humi' : 'no-data'
}

// 获取告警等级类型
const getAlertLevelType = (level) => {
  const typeMap = {
    'LOW': 'info',
    'MEDIUM': 'warning',
    'HIGH': 'danger',
    'CRITICAL': 'error'
  }
  return typeMap[level] || 'info'
}

// 获取告警等级文本
const getAlertLevelText = (level) => {
  const textMap = {
    'LOW': '低风险',
    'MEDIUM': '中风险',
    'HIGH': '高风险',
    'CRITICAL': '严重'
  }
  return textMap[level] || '告警'
}

// 跳转到库区
const goToArea = (areaId) => {
  console.log('Go to area:', areaId)
  // 可以跳转到库区管理页面或在地图上高亮显示
}

// 打开告警抽屉
const openAlertDrawer = (device) => {
  emit('viewAlert', device)
}
</script>

<style scoped>
.device-table-container {
  display: flex;
  flex-direction: column;
  height: 100%;
}

.filter-form {
  margin-bottom: 16px;
  padding: 16px;
  background: #fafafa;
  border-radius: 4px;
}

.filter-form :deep(.el-form-item) {
  margin-bottom: 12px;
  margin-right: 0;
}

.device-info {
  display: flex;
  flex-direction: column;
}

.device-name {
  font-weight: 500;
  color: #303133;
}

.device-code {
  font-size: 12px;
  color: #909399;
}

.temp-value, .humi-value {
  display: flex;
  align-items: center;
  gap: 4px;
}

.no-data {
  color: #c0c4cc;
  font-style: italic;
}

.normal-temp, .normal-humi {
  color: #67C23A;
}

.near-threshold {
  color: #E6A23C;
}

.over-threshold {
  color: #F56C6C;
  font-weight: bold;
}

.warning-icon {
  color: #E6A23C;
}

.danger-icon {
  color: #F56C6C;
}

.time-cell {
  font-family: monospace;
}

.alert-status {
  cursor: pointer;
}

.normal-status {
  color: #909399;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}
</style>