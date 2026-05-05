<template>
  <Layout>
    <div class="system-page">
      <div class="page-head">
        <div>
          <h1>阈值规则</h1>
          <p>集中查看设备阈值配置，支持直接修改单台设备的温湿度告警边界。</p>
        </div>
        <el-button @click="loadDevices">
          <el-icon><Refresh /></el-icon>
          刷新
        </el-button>
      </div>

      <el-card shadow="never" class="rule-summary">
        <el-row :gutter="16">
          <el-col :span="6">
            <div class="metric">
              <span>设备总数</span>
              <strong>{{ devices.length }}</strong>
            </div>
          </el-col>
          <el-col :span="6">
            <div class="metric">
              <span>自定义阈值</span>
              <strong>{{ customCount }}</strong>
            </div>
          </el-col>
          <el-col :span="6">
            <div class="metric">
              <span>继承库区</span>
              <strong>{{ inheritCount }}</strong>
            </div>
          </el-col>
          <el-col :span="6">
            <div class="metric">
              <span>告警启用</span>
              <strong>{{ alarmEnabledCount }}</strong>
            </div>
          </el-col>
        </el-row>
      </el-card>

      <el-card shadow="never" class="table-card">
        <el-table :data="devices" v-loading="loading" stripe>
          <el-table-column prop="deviceCode" label="编码" width="130" />
          <el-table-column prop="deviceName" label="设备名称" min-width="170" />
          <el-table-column prop="areaName" label="库区" width="150" />
          <el-table-column prop="thresholdMode" label="模式" width="120">
            <template #default="{ row }">
              <el-tag :type="row.thresholdMode === 'CUSTOM' || row.thresholdMode === 'OVERRIDE' ? 'warning' : 'info'">
                {{ modeLabel(row.thresholdMode) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="温度阈值" width="160">
            <template #default="{ row }">
              {{ formatRange(row.temperatureThresholdMin, row.temperatureThresholdMax, '°C') }}
            </template>
          </el-table-column>
          <el-table-column label="湿度阈值" width="160">
            <template #default="{ row }">
              {{ formatRange(row.humidityThresholdMin, row.humidityThresholdMax, '%') }}
            </template>
          </el-table-column>
          <el-table-column prop="alarmEnabled" label="告警" width="100">
            <template #default="{ row }">
              <el-tag :type="row.alarmEnabled ? 'success' : 'info'">
                {{ row.alarmEnabled ? '启用' : '关闭' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" fixed="right" width="120">
            <template #default="{ row }">
              <el-button size="small" type="primary" @click="openEditor(row)">编辑</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-card>

      <el-drawer v-model="drawerVisible" title="编辑阈值规则" size="420px">
        <el-form label-width="110px" :model="form">
          <el-form-item label="设备">
            <el-input :model-value="form.deviceName" disabled />
          </el-form-item>
          <el-form-item label="阈值模式">
            <el-radio-group v-model="form.thresholdMode">
              <el-radio label="INHERIT">继承库区</el-radio>
              <el-radio label="CUSTOM">自定义</el-radio>
            </el-radio-group>
          </el-form-item>
          <template v-if="form.thresholdMode === 'CUSTOM'">
            <el-form-item label="温度下限">
              <el-input-number v-model="form.temperatureThresholdMin" :precision="2" :step="0.5" />
            </el-form-item>
            <el-form-item label="温度上限">
              <el-input-number v-model="form.temperatureThresholdMax" :precision="2" :step="0.5" />
            </el-form-item>
            <el-form-item label="湿度下限">
              <el-input-number v-model="form.humidityThresholdMin" :precision="2" :step="1" />
            </el-form-item>
            <el-form-item label="湿度上限">
              <el-input-number v-model="form.humidityThresholdMax" :precision="2" :step="1" />
            </el-form-item>
          </template>
          <el-form-item label="告警开关">
            <el-switch v-model="form.alarmEnabled" active-text="启用" inactive-text="关闭" />
          </el-form-item>
        </el-form>

        <template #footer>
          <div class="drawer-footer">
            <el-button @click="drawerVisible = false">取消</el-button>
            <el-button type="primary" :loading="submitting" @click="submitThreshold">保存</el-button>
          </div>
        </template>
      </el-drawer>
    </div>
  </Layout>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { Refresh } from '@element-plus/icons-vue'
import Layout from '@/components/Layout.vue'
import { deviceApi } from '@/api/device'

const loading = ref(false)
const submitting = ref(false)
const devices = ref([])
const drawerVisible = ref(false)
const form = reactive({})

const unwrapPageList = (response) => {
  const payload = response?.data?.data || response?.data || {}
  if (Array.isArray(payload)) return payload
  return payload.data || payload.records || payload.list || []
}

const customCount = computed(() => devices.value.filter(item => ['CUSTOM', 'OVERRIDE'].includes(item.thresholdMode)).length)
const inheritCount = computed(() => devices.value.filter(item => !['CUSTOM', 'OVERRIDE'].includes(item.thresholdMode)).length)
const alarmEnabledCount = computed(() => devices.value.filter(item => item.alarmEnabled).length)

const loadDevices = async () => {
  loading.value = true
  try {
    const response = await deviceApi.getList({ page: 1, size: 200 })
    devices.value = unwrapPageList(response)
  } catch (error) {
    console.error(error)
    ElMessage.error('获取设备阈值失败')
  } finally {
    loading.value = false
  }
}

const openEditor = (row) => {
  Object.assign(form, {
    ...row,
    thresholdMode: row.thresholdMode === 'OVERRIDE' ? 'CUSTOM' : (row.thresholdMode || 'INHERIT')
  })
  drawerVisible.value = true
}

const submitThreshold = async () => {
  if (!form.id) return
  submitting.value = true
  try {
    await deviceApi.updateThreshold(form.id, {
      thresholdMode: form.thresholdMode,
      temperatureThresholdMin: form.thresholdMode === 'CUSTOM' ? form.temperatureThresholdMin : null,
      temperatureThresholdMax: form.thresholdMode === 'CUSTOM' ? form.temperatureThresholdMax : null,
      humidityThresholdMin: form.thresholdMode === 'CUSTOM' ? form.humidityThresholdMin : null,
      humidityThresholdMax: form.thresholdMode === 'CUSTOM' ? form.humidityThresholdMax : null
    })
    await deviceApi.updateAlarmStatus(form.id, { alarmEnabled: form.alarmEnabled })
    ElMessage.success('阈值规则已保存')
    drawerVisible.value = false
    await loadDevices()
  } catch (error) {
    console.error(error)
    ElMessage.error(error.response?.data?.message || '保存失败')
  } finally {
    submitting.value = false
  }
}

const modeLabel = (mode) => {
  if (mode === 'CUSTOM' || mode === 'OVERRIDE') return '自定义'
  if (mode === 'SYSTEM') return '系统默认'
  return '继承库区'
}

const formatRange = (min, max, unit) => {
  if (min == null || max == null) return '继承'
  return `${min} ~ ${max}${unit}`
}

onMounted(loadDevices)
</script>

<style scoped>
.system-page {
  padding: 20px 24px 28px;
  min-height: 100%;
  background: var(--ccg-bg);
}

.page-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 16px;
}

.page-head h1 {
  margin: 0;
  font-size: 22px;
  line-height: 1.2;
  font-weight: 750;
  color: var(--ccg-text);
}

.page-head p {
  margin: 6px 0 0;
  color: var(--ccg-muted);
  font-size: 13px;
}

.rule-summary {
  margin-bottom: 14px;
  border-radius: 8px;
}

.metric {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.metric span {
  color: #606266;
}

.metric strong {
  font-size: 28px;
  color: #303133;
}

.table-card {
  border-radius: 8px;
}

.drawer-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}
</style>
