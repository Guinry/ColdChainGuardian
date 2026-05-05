<template>
  <div class="area-tree-panel">
    <div class="tree-header">
      <el-input
        v-model="searchKeyword"
        placeholder="搜索库区..."
        :prefix-icon="Search"
        clearable
        @input="filterTree"
      />
    </div>
    <div class="tree-content">
      <el-tree
        ref="treeRef"
        :data="treeData"
        :props="treeProps"
        :filter-method="filterMethod"
        :expand-on-click-node="false"
        highlight-current
        node-key="id"
        @node-click="handleNodeClick"
        class="area-tree"
      >
        <template #default="{ node, data }">
          <div class="tree-node-content">
            <span class="node-label">{{ data.areaName }}</span>
            <span class="node-meta">
              <span class="node-code">[{{ data.areaCode }}]</span>
              <el-tag
                size="small"
                :type="getLevelTagType(data.areaLevel)"
                class="level-tag"
              >
                {{ getLevelLabel(data.areaLevel) }}
              </el-tag>
              <span class="device-count" v-if="data.deviceCount !== undefined">
                设备:{{ data.deviceCount }}/告警:{{ data.alarmingCount || 0 }}
              </span>
            </span>
          </div>
        </template>
      </el-tree>
    </div>
    <div class="tree-footer">
      <el-checkbox v-model="includeChildren" @change="handleIncludeChildrenChange">
        包含子节点
      </el-checkbox>
    </div>
  </div>
</template>

<script setup>
import { ref, defineProps, defineEmits, watch } from 'vue'
import { Search } from '@element-plus/icons-vue'

const props = defineProps({
  treeData: {
    type: Array,
    default: () => []
  }
})

const emit = defineEmits(['nodeClick', 'searchChange'])

const searchKeyword = ref('')
const includeChildren = ref(false)
const treeRef = ref(null)

const treeProps = {
  children: 'children',
  label: 'areaName'
}

// 监听搜索关键词变化
watch(searchKeyword, (val) => {
  emit('searchChange', val)
})

// 节点点击事件
const handleNodeClick = (data) => {
  const nodeInfo = {
    ...data,
    includeChildren: includeChildren.value
  }
  emit('nodeClick', nodeInfo)
}

// 搜索过滤
const filterTree = () => {
  treeRef.value?.filter(searchKeyword.value)
}

const filterMethod = (value, data) => {
  if (!value) return true
  return data.areaName.includes(value) || data.areaCode.includes(value)
}

// 获取层级标签类型
const getLevelTagType = (level) => {
  const typeMap = {
    'SITE': 'primary',
    'WAREHOUSE': 'success',
    'FLOOR': 'warning',
    'AREA': 'info',
    'BIN': 'danger'
  }
  return typeMap[level] || 'info'
}

// 获取层级标签文本
const getLevelLabel = (level) => {
  const labelMap = {
    'SITE': '园区',
    'WAREHOUSE': '仓库',
    'FLOOR': '楼层',
    'AREA': '区域',
    'BIN': '库位'
  }
  return labelMap[level] || level
}

// 包含子节点选项变更
const handleIncludeChildrenChange = () => {
  // 可能需要重新触发当前节点点击事件来应用包含子节点的过滤
  const currentNode = treeRef.value?.getCurrentNode()
  if (currentNode) {
    const nodeInfo = {
      ...currentNode,
      includeChildren: includeChildren.value
    }
    emit('nodeClick', nodeInfo)
  }
}
</script>

<style scoped>
.area-tree-panel {
  display: flex;
  flex-direction: column;
  width: 300px;
  min-width: 300px;
  min-height: 440px;
  max-height: calc(100vh - var(--ccg-header-height) - 176px);
  position: sticky;
  top: 16px;
  padding: 14px;
  border: 1px solid var(--ccg-border);
  border-radius: 8px;
  background: #fff;
  overflow: hidden;
  box-shadow: var(--ccg-shadow-sm);
}

.tree-header {
  margin-bottom: 12px;
}

.tree-content {
  flex: 1;
  min-height: 0;
  overflow: auto;
}

.area-tree {
  padding: 4px 0;
}

.area-tree :deep(.el-tree-node__content) {
  height: auto;
  min-height: 52px;
  align-items: stretch;
  padding-top: 6px;
  padding-bottom: 6px;
}

.area-tree :deep(.el-tree-node__expand-icon) {
  align-self: flex-start;
  margin-top: 10px;
}

.tree-node-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
  gap: 5px;
  font-size: 14px;
  padding-right: 8px;
  min-width: 0;
}

.node-label {
  display: block;
  min-width: 0;
  color: #1f2937;
  font-weight: 650;
  line-height: 1.35;
  white-space: normal;
  overflow-wrap: anywhere;
}

.node-meta {
  display: flex;
  align-items: center;
  gap: 6px;
  min-width: 0;
  flex-wrap: wrap;
}

.node-code {
  color: var(--ccg-muted);
  font-size: 12px;
  line-height: 1.2;
}

.level-tag {
  font-size: 10px;
  height: 18px;
  padding: 0 6px;
  flex-shrink: 0;
}

.device-count {
  font-size: 12px;
  color: #909399;
  background-color: #f4f4f5;
  padding: 2px 6px;
  border-radius: 10px;
  flex-shrink: 0;
}

.tree-footer {
  padding-top: 10px;
  border-top: 1px solid var(--ccg-border);
}

@media (max-width: 1160px) {
  .area-tree-panel {
    width: 100%;
    min-width: 0;
    min-height: 320px;
    max-height: none;
    position: static;
  }
}
</style>
