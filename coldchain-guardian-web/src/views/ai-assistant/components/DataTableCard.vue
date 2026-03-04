<template>
  <div class="data-table-card">
    <el-card shadow="hover" class="table-card">
      <template #header>
        <div class="card-header">
          <span class="card-title">{{ data.title || '数据表格' }}</span>
          <div class="card-actions">
            <el-button size="small" icon="Download">导出</el-button>
          </div>
        </div>
      </template>

      <el-table
        :data="data.rows || []"
        style="width: 100%"
        stripe
        border
        @row-click="handleRowClick"
      >
        <el-table-column
          v-for="column in data.columns || []"
          :key="column.prop"
          :prop="column.prop"
          :label="column.label"
          :width="column.width"
        >
          <template #default="{ row }">
            <!-- 如果是设备ID或工单号，渲染为链接 -->
            <router-link
              v-if="['deviceId', 'orderId', 'deviceCode'].includes(column.prop)"
              :to="getColumnLink(row, column.prop)"
              class="table-link"
              @click.stop
            >
              {{ row[column.prop] }}
            </router-link>
            <span v-else>{{ row[column.prop] }}</span>
          </template>
        </el-table-column>
      </el-table>

      <div class="table-footer" v-if="data.pagination">
        <el-pagination
          :current-page="data.pagination.currentPage"
          :page-size="data.pagination.pageSize"
          :total="data.pagination.total"
          layout="prev, pager, next"
          @current-change="handlePageChange"
        />
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

// 处理行点击事件
const handleRowClick = (row) => {
  console.log('表格行点击', row)
  // 可以在这里打开详细信息抽屉
}

// 处理分页变化
const handlePageChange = (page) => {
  console.log('分页变化', page)
  // 触发父组件的分页处理
}

// 生成列链接
const getColumnLink = (row, prop) => {
  if (prop === 'deviceId' || prop === 'deviceCode') {
    return `/devices/${row[prop]}`
  } else if (prop === 'orderId') {
    return `/work-orders/${row[prop]}`
  }
  return '#'
}
</script>

<style scoped>
.data-table-card {
  margin-top: 16px;
}

.table-card {
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

.card-actions {
  display: flex;
  gap: 8px;
}

.table-link {
  color: #409eff;
  text-decoration: none;
}

.table-link:hover {
  text-decoration: underline;
}

.table-footer {
  margin-top: 16px;
  text-align: right;
}
</style>