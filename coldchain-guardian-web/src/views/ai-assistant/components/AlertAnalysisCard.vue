<template>
  <div class="alert-analysis-card">
    <el-card shadow="hover" class="analysis-card">
      <template #header>
        <div class="card-header">
          <span class="card-title">AI 告警诊断分析</span>
          <el-button type="primary" size="small" @click="createWorkOrder">一键转交工单</el-button>
        </div>
      </template>

      <div class="analysis-grid">
        <div class="analysis-section">
          <h4>根因推测</h4>
          <p>{{ data.rootCause || '暂无分析结果' }}</p>
        </div>

        <div class="analysis-section">
          <h4>严重度评估</h4>
          <el-tag
            :type="getSeverityType(data.severity)"
            size="small"
          >
            {{ data.severity || '未知' }}
          </el-tag>
        </div>

        <div class="analysis-section">
          <h4>建议处置动作</h4>
          <p>{{ data.suggestion || '暂无建议' }}</p>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { defineProps } from 'vue'

const props = defineProps({
  data: {
    type: Object,
    required: true
  }
})

// 根据严重度返回标签类型
const getSeverityType = (severity) => {
  switch (severity?.toLowerCase()) {
    case 'critical':
    case 'high':
      return 'danger'
    case 'medium':
      return 'warning'
    case 'low':
      return 'success'
    default:
      return 'info'
  }
}

// 创建工单
const createWorkOrder = () => {
  // 这里应该调用创建工单的API
  console.log('创建工单', props.data)
  // 弹出创建工单的对话框
  // 这可能需要通过 emit 或全局事件来触发父组件中的对话框
}
</script>

<style scoped>
.alert-analysis-card {
  margin-top: 16px;
}

.analysis-card {
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

.analysis-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 16px;
}

.analysis-section h4 {
  margin: 0 0 8px 0;
  color: #606266;
  font-size: 14px;
  font-weight: 500;
}

.analysis-section p {
  margin: 0;
  color: #303133;
  line-height: 1.5;
}

.analysis-section {
  padding: 12px;
  background-color: #f8f9fa;
  border-radius: 6px;
}
</style>