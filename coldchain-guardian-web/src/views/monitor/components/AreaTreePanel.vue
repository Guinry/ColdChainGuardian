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
  height: 100%;
  display: flex;
  flex-direction: column;
}

.tree-header {
  margin-bottom: 16px;
}

.tree-content {
  flex: 1;
  overflow: auto;
}

.area-tree {
  height: 100%;
}

.tree-node-content {
  flex: 1;
  display: flex;
  align-items: center;
  font-size: 14px;
  padding-right: 8px;
}

.node-label {
  margin-right: 8px;
  font-weight: 500;
  flex-shrink: 0;
  max-width: 120px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.node-code {
  color: #909399;
  font-size: 12px;
  margin-right: 8px;
  flex-shrink: 0;
}

.level-tag {
  margin-right: 8px;
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
  border-top: 1px solid #ebeef5;
}
</style>