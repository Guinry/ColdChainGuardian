<template>
  <div class="data-table-container">
    <el-table
      :data="tableData"
      :loading="loading"
      stripe
      style="width: 100%"
      :height="height"
      @selection-change="handleSelectionChange">

      <!-- 动态列 -->
      <el-table-column
        v-for="column in columns"
        :key="column.prop"
        :prop="column.prop"
        :label="column.label"
        :width="column.width"
        :formatter="column.formatter">
        <template #default="scope">
          <span v-if="!column.customRender">{{ scope.row[column.prop] }}</span>
          <component
            v-else
            :is="column.customRender.component"
            v-bind="column.customRender.props(scope.row)"
          />
        </template>
      </el-table-column>

      <!-- 操作列 -->
      <el-table-column
        v-if="showActions"
        label="操作"
        :width="actionColumnWidth"
        fixed="right">
        <template #default="scope">
          <slot name="actions" :row="scope.row" :index="scope.$index">
            <el-button
              v-if="hasAction('view')"
              size="small"
              @click="handleAction('view', scope.row)"
              type="primary">
              查看
            </el-button>
            <el-button
              v-if="hasAction('edit')"
              size="small"
              @click="handleAction('edit', scope.row)">
              编辑
            </el-button>
            <el-button
              v-if="hasAction('delete')"
              size="small"
              @click="handleAction('delete', scope.row)"
              type="danger">
              删除
            </el-button>
          </slot>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页 -->
    <el-pagination
      v-if="showPagination"
      @size-change="handleSizeChange"
      @current-change="handleCurrentChange"
      :current-page="currentPage"
      :page-sizes="pageSizes"
      :page-size="pageSize"
      layout="total, sizes, prev, pager, next, jumper"
      :total="total">
    </el-pagination>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'

// Props
const props = defineProps({
  tableData: {
    type: Array,
    default: () => []
  },
  loading: {
    type: Boolean,
    default: false
  },
  height: {
    type: [String, Number],
    default: 400
  },
  columns: {
    type: Array,
    default: () => []
  },
  showActions: {
    type: Boolean,
    default: true
  },
  actionColumnWidth: {
    type: Number,
    default: 150
  },
  showPagination: {
    type: Boolean,
    default: true
  },
  currentPage: {
    type: Number,
    default: 1
  },
  pageSize: {
    type: Number,
    default: 10
  },
  total: {
    type: Number,
    default: 0
  },
  pageSizes: {
    type: Array,
    default: () => [10, 20, 50, 100]
  },
  availableActions: {
    type: Array,
    default: () => ['view', 'edit', 'delete']
  }
})

// Emits
const emit = defineEmits([
  'selection-change',
  'size-change',
  'current-change',
  'action'
])

// 选中项
const selectedRows = ref([])

// 处理选择变更
const handleSelectionChange = (val) => {
  selectedRows.value = val
  emit('selection-change', val)
}

// 处理每页大小变更
const handleSizeChange = (val) => {
  emit('size-change', val)
}

// 处理当前页变更
const handleCurrentChange = (val) => {
  emit('current-change', val)
}

// 检查是否具有某个操作权限
const hasAction = (action) => {
  return props.availableActions.includes(action)
}

// 处理操作
const handleAction = (action, row) => {
  emit('action', { action, row })
}
</script>

<style scoped>
.data-table-container {
  width: 100%;
}
</style>