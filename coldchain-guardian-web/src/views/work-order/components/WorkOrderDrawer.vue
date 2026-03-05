<template>
  <el-drawer
    v-model="drawerVisible"
    :title="`工单详情 #${workOrder?.id || '加载中...'}`"
    size="600px"
    @close="handleClose"
  >
    <div v-if="workOrder" class="work-order-detail">
      <!-- 工单基本信息 -->
      <el-card class="info-card">
        <template #header>
          <div class="card-header">
            <span>工单信息</span>
          </div>
        </template>

        <el-descriptions :column="1" border>
          <el-descriptions-item label="工单标题">{{ workOrder.title }}</el-descriptions-item>
          <el-descriptions-item label="工单类型">
            <el-tag :type="getWorkTypeTagType(workOrder.workType)">
              {{ getWorkTypeName(workOrder.workType) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="优先级">
            <el-tag :type="getPriorityTagType(workOrder.priority)">
              {{ getPriorityName(workOrder.priority) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="getStatusTagType(workOrder.status)">
              {{ getStatusName(workOrder.status) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="责任人">{{ workOrder.assigneeName || '未分配' }}</el-descriptions-item>
          <el-descriptions-item label="报告人">{{ workOrder.reporterName || '系统' }}</el-descriptions-item>
          <el-descriptions-item label="发生位置">
            <div v-if="workOrder.warehouseName || workOrder.deviceName">
              <el-tag type="info" size="small">{{ workOrder.warehouseName }}</el-tag>
              <br />{{ workOrder.deviceName }}
            </div>
            <span v-else>未指定</span>
          </el-descriptions-item>
          <el-descriptions-item label="创建时间">
            {{ formatDate(workOrder.createdAt) }}
          </el-descriptions-item>
          <el-descriptions-item label="截止时间" v-if="workOrder.dueDate">
            {{ formatDate(workOrder.dueDate) }}
          </el-descriptions-item>
          <el-descriptions-item label="完成时间" v-if="workOrder.completedAt">
            {{ formatDate(workOrder.completedAt) }}
          </el-descriptions-item>
        </el-descriptions>
      </el-card>

      <!-- 工单描述 -->
      <el-card class="info-card">
        <template #header>
          <div class="card-header">
            <span>工单描述</span>
          </div>
        </template>
        <div class="description-content">
          {{ workOrder.description || '无描述信息' }}
        </div>
      </el-card>

      <!-- 溯源卡片（如果有关联告警） -->
      <el-card class="info-card" v-if="workOrder.alertId">
        <template #header>
          <div class="card-header">
            <span>溯源信息</span>
          </div>
        </template>
        <div class="source-info">
          <p><strong>关联告警:</strong> 告警 #{{ workOrder.alertId }}</p>
          <p><strong>告警类型:</strong> {{ workOrder.alertType || '未知' }}</p>
          <p><strong>告警时间:</strong> {{ formatDate(workOrder.alertTime) }}</p>
          <p><strong>告警详情:</strong> {{ workOrder.alertDescription || '无' }}</p>
        </div>
      </el-card>

      <!-- 工单流转时间轴 -->
      <el-card class="info-card">
        <template #header>
          <div class="card-header">
            <span>处理流程</span>
          </div>
        </template>
        <el-timeline>
          <el-timeline-item
            v-for="(log, index) in workOrderLogs"
            :key="index"
            :timestamp="formatDate(log.createdAt)"
            :color="getLogColor(log.action)"
          >
            <div class="timeline-content">
              <p><strong>{{ log.operatorName || '系统' }}</strong> {{ log.actionText }}</p>
              <p v-if="log.remark" class="remark">{{ log.remark }}</p>
              <p v-if="log.previousStatus || log.currentStatus" class="status-change">
                状态: {{ getStatusName(log.previousStatus) }} → {{ getStatusName(log.currentStatus) }}
              </p>
            </div>
          </el-timeline-item>

          <!-- 添加初始状态 -->
          <el-timeline-item
            timestamp="创建工单"
            color="#0bbd87"
          >
            <div class="timeline-content">
              <p><strong>{{ workOrder.reporterName || '系统' }}</strong> 创建了工单</p>
              <p class="status-change">状态: 未开始 → 待处理</p>
            </div>
          </el-timeline-item>
        </el-timeline>
      </el-card>

      <!-- 操作按钮 -->
      <div class="action-buttons" v-if="currentUserCanOperate">
        <el-button
          type="primary"
          @click="handleOperation('accept')"
          v-if="workOrder.status === 'PENDING'"
        >
          接受工单
        </el-button>
        <el-button
          type="warning"
          @click="handleOperation('start')"
          v-if="workOrder.status === 'PENDING'"
        >
          直接开始处理
        </el-button>
        <el-button
          type="success"
          @click="handleOperation('complete')"
          v-if="workOrder.status === 'PROCESSING'"
        >
          完成工单
        </el-button>
        <el-button
          type="primary"
          @click="handleOperation('verify')"
          v-if="workOrder.status === 'VERIFYING'"
        >
          验收通过
        </el-button>
        <el-button
          type="info"
          @click="handleOperation('close')"
          v-if="workOrder.status === 'COMPLETED'"
        >
          关闭工单
        </el-button>
        <el-button
          @click="handleOperation('reject')"
          v-if="['PROCESSING', 'VERIFYING'].includes(workOrder.status)"
        >
          驳回处理
        </el-button>
      </div>
    </div>

    <div v-else class="loading-placeholder">
      <el-empty description="正在加载工单详情..." />
    </div>
  </el-drawer>
</template>

<script setup>
import { ref, computed, watchEffect } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { workOrderApi } from '@/api/work-order.js';

// Props
const props = defineProps({
  visible: {
    type: Boolean,
    default: false
  },
  workOrderId: {
    type: Number,
    default: null
  }
});

// Emits
const emit = defineEmits(['close', 'updated']);

// Data
const workOrder = ref(null);
const workOrderLogs = ref([]);
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

// Check if current user can operate on this work order
const currentUserCanOperate = computed(() => {
  // Simplified check - in real app you'd check user roles and permissions
  return true;
});

// Watch for changes in work order ID
watchEffect(async () => {
  if (props.visible && props.workOrderId) {
    await loadWorkOrderDetails();
  }
});

// Load work order details
const loadWorkOrderDetails = async () => {
  loading.value = true;

  try {
    // Get work order details
    const response = await workOrderApi.getDetail(props.workOrderId);
    workOrder.value = response.data.data;

    // Get work order logs
    const logsResponse = await workOrderApi.getLogs(props.workOrderId);
    let logs = logsResponse.data.data || [];

    // Add actionText property to logs for display and normalize timestamps
    workOrderLogs.value = logs.map(log => ({
      ...log,
      actionText: getActionText(log.action),
      createdAt: log.createdAt || log.createTime
    }));
  } catch (error) {
    console.error('获取工单详情失败:', error);
    ElMessage.error('获取工单详情失败');
  } finally {
    loading.value = false;
  }
};

// Helper to get readable action text
const getActionText = (action) => {
  switch(action) {
    case 'CREATED': return '创建了工单';
    case 'ACCEPTED': return '接受了工单';
    case 'STARTED': return '开始处理工单';
    case 'COMPLETED': return '完成了工单';
    case 'VERIFIED': return '验收了工单';
    case 'REJECTED': return '驳回了工单';
    case 'CLOSED': return '关闭了工单';
    case 'STATUS_CHANGED': return '变更了状态';
    default: return action;
  }
};

// Close drawer
const handleClose = () => {
  emit('close');
  // Reset data
  workOrder.value = null;
  workOrderLogs.value = [];
};

// Handle operations
const handleOperation = async (operation) => {
  try {
    let status, title, message, action;

    switch(operation) {
      case 'accept':
        status = 'PROCESSING';
        action = 'ACCEPTED';
        title = '接受工单';
        message = '确认接受此工单吗？';
        break;
      case 'start':
        status = 'PROCESSING';
        action = 'STARTED';
        title = '开始处理';
        message = '确认开始处理此工单吗？';
        break;
      case 'complete':
        status = 'VERIFYING';
        action = 'COMPLETED';
        title = '完成工单';
        message = '确认已完成工单处理，等待验收吗？';
        break;
      case 'verify':
        status = 'COMPLETED';
        action = 'VERIFIED';
        title = '验收通过';
        message = '确认验收通过此工单吗？';
        break;
      case 'close':
        status = 'CLOSED';
        action = 'CLOSED';
        title = '关闭工单';
        message = '确认关闭此工单吗？关闭后将无法修改。';
        break;
      case 'reject':
        status = 'PENDING';
        action = 'REJECTED';
        title = '驳回处理';
        message = '确认驳回此工单处理吗？';
        break;
      default:
        return;
    }

    await ElMessageBox.confirm(message, title, {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    });

    // Update work order status
    const response = await workOrderApi.updateStatus(props.workOrderId, {
      status: status,
      remark: `通过工单详情页操作: ${title}`,
      operatorId: 1, // Current user ID
      operatorName: '当前用户' // Current user name
    });

    if (response.data.success) {
      ElMessage.success(`${title}成功`);

      // Refresh the work order details
      await loadWorkOrderDetails();

      // Notify parent component
      emit('updated');
    } else {
      ElMessage.error(`${title}失败: ${response.data.message}`);
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error(`工单${operation}操作失败:`, error);
      ElMessage.error(`操作失败: ${error.message}`);
    }
  }
};

// Utility methods
const getWorkTypeTagType = (type) => {
  switch (type) {
    case 'ALERT_DEFECT': return 'warning';
    case 'ROUTINE_INSPECTION': return 'info';
    case 'EQUIPMENT_MAINTENANCE': return 'success';
    default: return 'info';
  }
};

const getWorkTypeName = (type) => {
  switch (type) {
    case 'ALERT_DEFECT': return '告警消缺';
    case 'ROUTINE_INSPECTION': return '日常巡检';
    case 'EQUIPMENT_MAINTENANCE': return '设备维保';
    default: return '未知';
  }
};

const getPriorityTagType = (priority) => {
  switch (priority) {
    case 'URGENT': return 'danger';
    case 'HIGH': return 'warning';
    case 'MEDIUM': return 'info';
    case 'LOW': return 'success';
    default: return 'info';
  }
};

const getPriorityName = (priority) => {
  switch (priority) {
    case 'URGENT': return '紧急';
    case 'HIGH': return '高';
    case 'MEDIUM': return '中';
    case 'LOW': return '低';
    default: return '普通';
  }
};

const getStatusTagType = (status) => {
  switch (status) {
    case 'PENDING': return 'info';
    case 'PROCESSING': return 'warning';
    case 'VERIFYING': return 'primary';
    case 'COMPLETED': return 'success';
    case 'CLOSED': return 'info';
    default: return 'info';
  }
};

const getStatusName = (status) => {
  switch (status) {
    case 'PENDING': return '待处理';
    case 'PROCESSING': return '处理中';
    case 'VERIFYING': return '待验收';
    case 'COMPLETED': return '已完成';
    case 'CLOSED': return '已关闭';
    default: return '未知';
  }
};

const formatDate = (timestamp) => {
  if (!timestamp) return '';
  const date = new Date(timestamp);
  return date.toLocaleString('zh-CN');
};

const getLogColor = (action) => {
  if (action.includes('CREATED')) return '#0bbd87';
  if (action.includes('COMPLETED')) return '#67c23a';
  if (action.includes('VERIFIED')) return '#409eff';
  return '#909399';
};
</script>

<style scoped>
.work-order-detail {
  padding: 0 20px 20px;
}

.info-card {
  margin-bottom: 20px;
}

.card-header {
  font-weight: bold;
  color: #303133;
}

.description-content {
  white-space: pre-wrap;
  line-height: 1.6;
}

.source-info p {
  margin: 8px 0;
  color: #606266;
}

.timeline-content {
  margin-bottom: 8px;
}

.timeline-content .remark {
  color: #909399;
  font-size: 14px;
  margin: 4px 0;
}

.timeline-content .status-change {
  color: #409eff;
  font-size: 13px;
  margin: 4px 0;
}

.action-buttons {
  margin-top: 20px;
  padding-top: 20px;
  border-top: 1px solid #ebeef5;
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

.loading-placeholder {
  height: 400px;
  display: flex;
  align-items: center;
  justify-content: center;
}
</style>