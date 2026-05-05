<template>
  <div class="device-table-container">
    <!-- 筛选栏 -->
    <el-form :model="localFilters" class="filter-form">
      <div class="filter-grid">
        <div class="filter-item keyword">
          <el-form-item label="关键字">
            <el-input
              v-model="localFilters.keyword"
              placeholder="设备编码/名称"
              clearable
              @keyup.enter="handleFilterChange"
            />
          </el-form-item>
        </div>
        <div class="filter-item">
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
        </div>
        <div class="filter-item">
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
        </div>
        <div class="filter-item">
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
        </div>
        <div class="filter-actions">
          <el-form-item>
            <el-button type="primary" @click="handleFilterChange" :icon="Search">查询</el-button>
            <el-button @click="resetFilters">重置</el-button>
          </el-form-item>
        </div>
      </div>
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
      <el-table-column prop="deviceName" label="设备信息" min-width="140">
        <template #default="{ row }">
          <div class="device-info">
            <div class="device-name">{{ row.deviceName }}</div>
            <div class="device-code">{{ row.deviceCode }}</div>
          </div>
        </template>
      </el-table-column>

      <el-table-column prop="areaPath" label="所属库区" min-width="132" show-overflow-tooltip>
        <template #default="{ row }">
          <el-link class="area-link" type="primary" @click="goToArea(row.areaId)" :underline="'never'">
            <el-icon><Location /></el-icon>
            {{ row.areaPath }}
          </el-link>
        </template>
      </el-table-column>

      <el-table-column prop="latestTemp" label="温度(℃)" min-width="98" sortable>
        <template #default="{ row }">
          <div :class="['temp-value', getTempClass(row.latestTemp, row.temperatureThresholdMin, row.temperatureThresholdMax)]">
            <span v-if="row.latestTemp !== null">{{ row.latestTemp }}℃</span>
            <span v-else class="no-data">-</span>
            <el-icon v-if="isNearThreshold(row.latestTemp, row.temperatureThresholdMin, row.temperatureThresholdMax, 'temp')" class="warning-icon"><Warning /></el-icon>
            <el-icon v-else-if="isOverThreshold(row.latestTemp, row.temperatureThresholdMin, row.temperatureThresholdMax, 'temp')" class="danger-icon"><CircleCloseFilled /></el-icon>
          </div>
        </template>
      </el-table-column>

      <el-table-column prop="latestHumi" label="湿度(%)" min-width="98" sortable>
        <template #default="{ row }">
          <div :class="['humi-value', getHumiClass(row.latestHumi, row.humidityThresholdMin, row.humidityThresholdMax)]">
            <span v-if="row.latestHumi !== null">{{ row.latestHumi }}%</span>
            <span v-else class="no-data">-</span>
            <el-icon v-if="isNearThreshold(row.latestHumi, row.humidityThresholdMin, row.humidityThresholdMax, 'humi')" class="warning-icon"><Warning /></el-icon>
            <el-icon v-else-if="isOverThreshold(row.latestHumi, row.humidityThresholdMin, row.humidityThresholdMax, 'humi')" class="danger-icon"><CircleCloseFilled /></el-icon>
          </div>
        </template>
      </el-table-column>

      <el-table-column prop="latestDataTime" label="数据时间" min-width="112" sortable>
        <template #default="{ row }">
          <div class="time-cell">
            <span v-if="row.latestDataTime">{{ formatDate(row.latestDataTime) }}</span>
            <span v-else class="no-data">未上报</span>
          </div>
        </template>
      </el-table-column>

      <el-table-column prop="online" label="在线" min-width="78" align="center">
        <template #default="{ row }">
          <el-tag :type="row.online ? 'success' : 'danger'" size="small">
            {{ row.online ? '在线' : '离线' }}
          </el-tag>
        </template>
      </el-table-column>

      <el-table-column prop="hasUnresolvedAlert" label="告警" min-width="88" align="center">
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

      <el-table-column label="操作" width="108" align="center">
        <template #default="{ row }">
          <div class="table-actions">
            <el-button size="small" type="primary" link @click="$emit('viewDetail', row)">详情</el-button>
            <el-dropdown trigger="click" @command="(command) => handleRowAction(command, row)">
              <el-button size="small" link :icon="MoreFilled" />
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="trend">查看曲线</el-dropdown-item>
                  <el-dropdown-item command="alert">查看告警</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
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
import { reactive, defineProps, defineEmits, watch } from 'vue'
import { useRouter } from 'vue-router'
import { Search, Location, Warning, CircleCloseFilled, MoreFilled } from '@element-plus/icons-vue'

const router = useRouter()

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
  emit('filterChange', { ...localFilters })
}

// 重置筛选
const resetFilters = () => {
  const resetFiltersObj = {
    keyword: '',
    online: null,
    alarming: null,
    deviceType: ''
  }

  Object.assign(localFilters, resetFiltersObj)
  emit('filterChange', resetFiltersObj)
}

const handleRowAction = (command, row) => {
  if (command === 'trend') {
    emit('viewTrend', row)
  } else if (command === 'alert') {
    emit('viewAlert', row)
  }
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
    'CRITICAL': 'danger'
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
  if (!areaId) return
  router.push({ path: '/warehouse-area', query: { areaId } })
}

// 打开告警抽屉
const openAlertDrawer = (device) => {
  emit('viewAlert', device)
}
</script>

<style scoped>
.device-table-container {
  width: 100%;
}

.filter-form {
  margin-bottom: 14px;
  padding: 16px;
  background: #f8fafc;
  border: 1px solid var(--ccg-border);
  border-radius: 8px;
}

.filter-grid {
  display: grid;
  grid-template-columns: minmax(220px, 1.35fr) repeat(3, minmax(150px, 0.9fr)) auto;
  gap: 12px;
  align-items: end;
}

.filter-item,
.filter-actions {
  min-width: 0;
}

.filter-form :deep(.el-form-item) {
  width: 100%;
  margin-bottom: 0;
  margin-right: 0;
}

.filter-form :deep(.el-form-item__content) {
  width: 100%;
}

.filter-form :deep(.el-select),
.filter-form :deep(.el-input) {
  width: 100%;
}

.device-table-container :deep(.el-table__cell) {
  padding: 9px 0;
}

.device-table-container :deep(.caret-wrapper) {
  width: 32px;
  height: 28px;
  justify-content: center;
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

.area-link {
  max-width: 100%;
}

.area-link :deep(.el-link__inner) {
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
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

.table-actions {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  white-space: nowrap;
}

.pagination-container {
  margin-top: 14px;
  display: flex;
  justify-content: flex-end;
  padding-top: 12px;
  border-top: 1px solid var(--ccg-border);
}

@media (max-width: 1380px) {
  .filter-grid {
    grid-template-columns: repeat(2, minmax(220px, 1fr));
  }

  .filter-actions {
    grid-column: 1 / -1;
  }
}

@media (max-width: 768px) {
  .filter-grid {
    grid-template-columns: 1fr;
  }

  .pagination-container {
    justify-content: flex-start;
    overflow-x: auto;
  }
}
</style>
