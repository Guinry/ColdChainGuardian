<template>
  <el-drawer
    v-model="drawerVisible"
    :title="`告警详情 #${alert ? alert.id : '加载中...'}`"
    size="700px"
    @close="handleClose"
  >
    <div v-if="alert" class="alert-detail">
      <!-- 告警基本信息 -->
      <el-card class="info-card">
        <template #header>
          <div class="card-header">
            <span>告警信息</span>
          </div>
        </template>

        <el-descriptions :column="1" border>
          <el-descriptions-item label="告警类型">
            <el-tag :type="getSeverityTagType(alert?.severityLevel)">
              {{ alert?.alertType }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="严重等级">
            <el-tag :type="getSeverityTagType(alert?.severityLevel)">
              {{ getSeverityLabel(alert?.severityLevel) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="处理状态">
            <el-tag :type="getStatusTagType(alert?.status)">
              {{ getStatusName(alert?.status) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="发生位置">
            <div v-if="alert?.areaName || alert?.deviceName">
              <el-tag type="info" size="small">{{ alert?.areaName || alert?.warehouseName }}</el-tag>
              <br />{{ alert?.deviceName }}
            </div>
            <span v-else>未知位置</span>
          </el-descriptions-item>
          <el-descriptions-item label="发生时间">
            {{ formatDate(alert?.createdAt) }}
          </el-descriptions-item>
          <el-descriptions-item label="处理人" v-if="alert?.handlerName">
            {{ alert?.handlerName }}
          </el-descriptions-item>
          <el-descriptions-item label="处理时间" v-if="alert?.handleTime">
            {{ formatDate(alert?.handleTime) }}
          </el-descriptions-item>
        </el-descriptions>
      </el-card>

      <!-- 告警内容 -->
      <el-card class="info-card">
        <template #header>
          <div class="card-header">
            <span>告警内容</span>
          </div>
        </template>
        <div class="alert-content">
          {{ alert?.description || '无详细描述' }}
        </div>
      </el-card>

      <!-- 告警趋势图 -->
      <el-card class="info-card">
        <template #header>
          <div class="card-header">
            <span>告警趋势</span>
          </div>
        </template>
        <div class="chart-container">
          <!-- 简化的趋势图，实际项目中可以使用 echarts 等图表库 -->
          <div class="trend-placeholder">
            <p>最近24小时告警趋势图</p>
            <p>(此处应显示图表)</p>
          </div>
        </div>
      </el-card>

      <!-- 处理选项 -->
      <el-card class="info-card">
        <template #header>
          <div class="card-header">
            <span>处理选项</span>
          </div>
        </template>

        <div class="triage-options">
          <div class="option-card">
            <h4>转为工单</h4>
            <p>将此告警转化为正式工单进行跟踪处理</p>
            <el-button
              type="primary"
              size="small"
              @click="convertToWorkOrder"
              :disabled="['HANDLING', 'RESOLVED', 'IGNORED'].includes(alert.status)"
            >
              转为工单
            </el-button>
          </div>

          <div class="option-card">
            <h4>快速消除</h4>
            <p>直接将告警状态改为已解决</p>
            <el-button
              type="success"
              size="small"
              @click="resolveAlert"
              :disabled="['HANDLING', 'RESOLVED', 'IGNORED'].includes(alert.status)"
            >
              快速解决
            </el-button>
          </div>

          <div class="option-card">
            <h4>标记忽略</h4>
            <p>标记为误报或无需处理</p>
            <el-button
              type="info"
              size="small"
              @click="ignoreAlert"
              :disabled="['HANDLING', 'RESOLVED', 'IGNORED'].includes(alert.status)"
            >
              标记忽略
            </el-button>
          </div>
        </div>
      </el-card>

      <!-- 处理历史 -->
      <el-card class="info-card" v-if="alert.handleRemark">
        <template #header>
          <div class="card-header">
            <span>处理记录</span>
          </div>
        </template>
        <div class="handle-record">
          <p><strong>处理备注:</strong></p>
          <p>{{ alert.handleRemark }}</p>
        </div>
      </el-card>
    </div>

    <div v-else class="loading-placeholder">
      <el-empty description="正在加载告警详情..." />
    </div>
  </el-drawer>
</template>

<script setup>
import { ref, computed, watchEffect } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { alertApi } from '@/api/alert';

// Props
const props = defineProps({
  visible: {
    type: Boolean,
    default: false
  },
  alertId: {
    type: Number,
    default: null
  }
});

// Emits
const emit = defineEmits(['close', 'updated']);

// Data
const alert = ref(null);
const loading = ref(false);

// Computed
const drawerVisible = computed({
  get() {
    return props.visible;
  },
  set(value) {
    if (!value) {
      emit('close');
    }
  }
});

// Watch for changes in alert ID
watchEffect(async () => {
  if (props.visible && props.alertId) {
    await loadAlertDetails();
  } else {
    // 如果抽屉关闭或没有ID，则清空告警数据
    alert.value = null;
  }
});

// Load alert details
const loadAlertDetails = async () => {
  loading.value = true;

  try {
    const response = await alertApi.getDetail(props.alertId);
    // 添加响应验证
    if (response && response.data) {
      alert.value = response.data.data || null;
    } else {
      alert.value = null;
    }
  } catch (error) {
    console.error('获取告警详情失败:', error);
    ElMessage.error('获取告警详情失败');
    alert.value = null; // 确保错误情况下重置数据
  } finally {
    loading.value = false;
  }
};

// Close drawer
const handleClose = () => {
  emit('close');
  // Reset data
  alert.value = null;
};

// Convert alert to work order
const convertToWorkOrder = async () => {
  try {
    await ElMessageBox.prompt('请填写转工单原因（可选）', '转为工单', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      inputPlaceholder: '请输入转工单的原因...'
    }).then(async ({ value }) => {
      const response = await alertApi.convertToWorkOrder(props.alertId, {
        assigneeId: 1, // Default assignee
        assigneeName: '系统管理员',
        description: value ? `${alert.value.description} (转工单原因: ${value})` : alert.value.description
      });

      if (response.data.success) {
        ElMessage.success('告警已转为工单');
        emit('updated');
        handleClose();
      } else {
        ElMessage.error('转工单失败: ' + response.data.message);
      }
    });
  } catch (error) {
    if (error !== 'cancel' && error !== 'close') {
      console.error('转工单失败:', error);
      ElMessage.error('转工单失败');
    }
  }
};

// Resolve alert
const resolveAlert = async () => {
  try {
    await ElMessageBox.prompt('请填写解决备注', '快速解决告警', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      inputPlaceholder: '请输入解决备注...'
    }).then(async ({ value }) => {
      const response = await alertApi.updateStatus(props.alertId, {
        status: 'RESOLVED',
        handleRemark: value || '快速解决'
      });

      if (response.data.success) {
        ElMessage.success('告警已解决');
        emit('updated');
        handleClose();
      } else {
        ElMessage.error('解决告警失败: ' + response.data.message);
      }
    });
  } catch (error) {
    if (error !== 'cancel' && error !== 'close') {
      console.error('解决告警失败:', error);
      ElMessage.error('解决告警失败');
    }
  }
};

// Ignore alert
const ignoreAlert = async () => {
  try {
    await ElMessageBox.prompt('请填写忽略原因', '标记忽略', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      inputPlaceholder: '请输入忽略原因...'
    }).then(async ({ value }) => {
      const response = await alertApi.updateStatus(props.alertId, {
        status: 'IGNORED',
        handleRemark: value || '标记为误报'
      });

      if (response.data.success) {
        ElMessage.success('告警已忽略');
        emit('updated');
        handleClose();
      } else {
        ElMessage.error('忽略告警失败: ' + response.data.message);
      }
    });
  } catch (error) {
    if (error !== 'cancel' && error !== 'close') {
      console.error('忽略告警失败:', error);
      ElMessage.error('忽略告警失败');
    }
  }
};

// Utility methods
const getSeverityTagType = (level) => {
  switch (level) {
    case 4: return 'danger';  // CRITICAL
    case 3: return 'warning'; // HIGH
    case 2: return 'info';    // MEDIUM
    case 1: return 'success'; // LOW
    default: return 'info';
  }
};

const getSeverityLabel = (level) => {
  switch (level) {
    case 4: return '紧急';
    case 3: return '高';
    case 2: return '中';
    case 1: return '低';
    default: return '未知';
  }
};

const getStatusTagType = (status) => {
  switch (status) {
    case 'UNHANDLED': return 'info';
    case 'HANDLING': return 'warning';
    case 'RESOLVED': return 'success';
    case 'IGNORED': return 'info';
    default: return 'info';
  }
};

const getStatusName = (status) => {
  switch (status) {
    case 'UNHANDLED': return '未处理';
    case 'HANDLING': return '处理中';
    case 'RESOLVED': return '已解决';
    case 'IGNORED': return '已忽略';
    default: return '未知';
  }
};

const formatDate = (timestamp) => {
  if (!timestamp) return '';
  const date = new Date(timestamp);
  return date.toLocaleString('zh-CN');
};
</script>

<style scoped>
.alert-detail {
  padding: 0 20px 20px;
}

.info-card {
  margin-bottom: 20px;
}

.card-header {
  font-weight: bold;
  color: #303133;
}

.alert-content {
  white-space: pre-wrap;
  line-height: 1.6;
  color: #606266;
}

.chart-container {
  padding: 20px 0;
}

.trend-placeholder {
  text-align: center;
  color: #909399;
  padding: 40px 0;
}

.triage-options {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 20px;
}

.option-card {
  border: 1px solid #ebeef5;
  border-radius: 4px;
  padding: 16px;
  text-align: center;
}

.option-card h4 {
  margin: 0 0 8px 0;
  color: #303133;
}

.option-card p {
  margin: 0 0 12px 0;
  color: #909399;
  font-size: 14px;
  line-height: 1.4;
}

.handle-record {
  color: #606266;
  line-height: 1.6;
}

.loading-placeholder {
  height: 400px;
  display: flex;
  align-items: center;
  justify-content: center;
}
</style>