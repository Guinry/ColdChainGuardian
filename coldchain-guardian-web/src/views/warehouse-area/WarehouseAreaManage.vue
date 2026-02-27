<template>
  <div class="warehouse-area-page">
    <div class="page-header">
      <h2>库区管理</h2>
      <div class="header-actions">
        <el-button type="primary" @click="openCreateDialog(null)">
          <el-icon><Plus /></el-icon>
          新增顶级节点
        </el-button>
        <el-button @click="handleImport">
          <el-icon><Upload /></el-icon>
          批量导入
        </el-button>
        <el-button @click="handleExport">
          <el-icon><Download /></el-icon>
          导出
        </el-button>
      </div>
    </div>

    <div class="content-wrapper">
      <!-- 左侧树形结构 -->
      <div class="tree-panel">
        <div class="panel-header">
          <h3>库区结构</h3>
          <el-input
            v-model="searchText"
            placeholder="搜索库区..."
            :prefix-icon="Search"
            clearable
            @input="filterTree"
          />
        </div>

        <el-tree
          ref="treeRef"
          :data="treeData"
          :props="treeProps"
          :filter-method="filterMethod"
          :expand-on-click-node="false"
          highlight-current
          node-key="id"
          @node-click="onTreeNodeClick"
          @node-contextmenu="onRightClick"
          class="custom-tree"
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
              <div class="node-status-icons">
                <el-tooltip v-if="data.status === 0" content="已禁用" placement="top">
                  <el-icon class="status-icon disabled"><CircleCloseFilled /></el-icon>
                </el-tooltip>
                <el-tooltip v-if="data.alarmEnabled === 0" content="告警已关闭" placement="top">
                  <el-icon class="status-icon alarm-disabled"><Mute /></el-icon>
                </el-tooltip>
              </div>
              <div class="node-actions" @click.stop>
                <el-button
                  size="small"
                  text
                  @click="openCreateDialog(data)"
                  :icon="FolderAdd"
                  class="action-btn"
                />
                <el-button
                  size="small"
                  text
                  @click="openEditDialog(data)"
                  :icon="Edit"
                  class="action-btn"
                />
                <el-button
                  size="small"
                  text
                  @click="handleDelete(data)"
                  :icon="Delete"
                  class="action-btn"
                />
              </div>
            </div>
          </template>
        </el-tree>
      </div>

      <!-- 右侧详情面板 -->
      <div class="detail-panel">
        <div v-if="selectedNode" class="detail-content">
          <div class="basic-info-card">
            <div class="card-header">
              <div class="header-left">
                <h3>{{ selectedNode.areaName }}</h3>
                <el-tag :type="getLevelTagType(selectedNode.areaLevel)">
                  {{ getLevelLabel(selectedNode.areaLevel) }}
                </el-tag>
              </div>
              <div class="header-actions">
                <el-button @click="openEditDialog(selectedNode)">编辑</el-button>
                <el-button
                  :type="selectedNode.status === 1 ? 'danger' : 'success'"
                  @click="toggleStatus(selectedNode)">
                  {{ selectedNode.status === 1 ? '禁用' : '启用' }}
                </el-button>
                <el-button
                  :type="selectedNode.alarmEnabled === 1 ? 'warning' : 'info'"
                  @click="toggleAlarm(selectedNode)">
                  {{ selectedNode.alarmEnabled === 1 ? '关闭告警' : '启用告警' }}
                </el-button>
                <el-button type="danger" @click="handleDelete(selectedNode)">删除</el-button>
              </div>
            </div>
            <div class="card-body">
              <div class="info-grid">
                <div class="info-row">
                  <div class="info-item">
                    <label>库区编码:</label>
                    <span>{{ selectedNode.areaCode }}</span>
                  </div>
                  <div class="info-item">
                    <label>状态:</label>
                    <el-tag :type="selectedNode.status === 1 ? 'success' : 'danger'">
                      {{ selectedNode.status === 1 ? '启用' : '禁用' }}
                    </el-tag>
                  </div>
                </div>
                <div class="info-row">
                  <div class="info-item">
                    <label>告警:</label>
                    <el-tag :type="selectedNode.alarmEnabled === 1 ? 'success' : 'info'">
                      {{ selectedNode.alarmEnabled === 1 ? '启用' : '关闭' }}
                    </el-tag>
                  </div>
                  <div class="info-item">
                    <label>排序号:</label>
                    <span>{{ selectedNode.sortNo }}</span>
                  </div>
                </div>
                <div class="info-row full-width">
                  <div class="info-item">
                    <label>地址:</label>
                    <span>{{ selectedNode.address || '-' }}</span>
                  </div>
                </div>
                <div class="info-row full-width">
                  <div class="info-item">
                    <label>位置描述:</label>
                    <span>{{ selectedNode.locationDesc || '-' }}</span>
                  </div>
                </div>
                <div class="threshold-section">
                  <h4>阈值设置</h4>
                  <div class="threshold-grid">
                    <div class="threshold-item">
                      <label>温度范围:</label>
                      <span>{{ selectedNode.temperatureThresholdMin }}°C ~ {{ selectedNode.temperatureThresholdMax }}°C</span>
                    </div>
                    <div class="threshold-item">
                      <label>湿度范围:</label>
                      <span>{{ selectedNode.humidityThresholdMin }}% ~ {{ selectedNode.humidityThresholdMax }}%</span>
                    </div>
                  </div>
                </div>
                <div class="info-row full-width">
                  <div class="info-item">
                    <label>备注:</label>
                    <span>{{ selectedNode.remark || '-' }}</span>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- 子库区列表 -->
          <div class="child-areas-section">
            <div class="section-header">
              <h3>子库区列表</h3>
              <el-button @click="openCreateDialog(selectedNode)">新增子库区</el-button>
            </div>

            <el-table
              :data="childAreas"
              style="width: 100%"
              row-key="id"
              border
            >
              <el-table-column prop="areaName" label="库区名称" width="200">
                <template #default="{ row }">
                  <el-button type="text" @click="selectNodeInTree(row)">{{ row.areaName }}</el-button>
                </template>
              </el-table-column>
              <el-table-column prop="areaCode" label="编码" width="150" />
              <el-table-column prop="areaLevel" label="层级" width="100">
                <template #default="{ row }">
                  <el-tag size="small">{{ getLevelLabel(row.areaLevel) }}</el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="status" label="状态" width="100">
                <template #default="{ row }">
                  <el-tag :type="row.status === 1 ? 'success' : 'danger'">
                    {{ row.status === 1 ? '启用' : '禁用' }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="alarmEnabled" label="告警" width="100">
                <template #default="{ row }">
                  <el-tag :type="row.alarmEnabled === 1 ? 'success' : 'warning'">
                    {{ row.alarmEnabled === 1 ? '开启' : '关闭' }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="sortNo" label="排序" width="80" />
              <el-table-column label="操作" width="200">
                <template #default="{ row }">
                  <el-button-group>
                    <el-button size="small" @click="openEditDialog(row)">编辑</el-button>
                    <el-button
                      size="small"
                      :type="row.status === 1 ? 'danger' : 'success'"
                      @click="toggleStatus(row)">
                      {{ row.status === 1 ? '禁用' : '启用' }}
                    </el-button>
                    <el-popconfirm
                      title="确定要删除这个库区吗？"
                      @confirm="handleDelete(row)"
                    >
                      <template #reference>
                        <el-button size="small" type="danger">删除</el-button>
                      </template>
                    </el-popconfirm>
                  </el-button-group>
                </template>
              </el-table-column>
            </el-table>
          </div>
        </div>
        <div v-else class="empty-state">
          <el-empty description="请选择左侧库区查看详细信息" />
        </div>
      </div>
    </div>

    <!-- 新增/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="600px"
      :before-close="handleDialogClose"
    >
      <el-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-width="120px"
      >
        <el-form-item label="上级库区" prop="parentId">
          <el-cascader
            v-model="formData.parentId"
            :options="parentOptions"
            :props="cascaderProps"
            placeholder="选择上级库区（顶级节点为空）"
            clearable
            filterable
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="库区层级" prop="areaLevel">
          <el-radio-group v-model="formData.areaLevel" @change="onLevelChange">
            <el-radio label="SITE">站点</el-radio>
            <el-radio label="WAREHOUSE">仓库</el-radio>
            <el-radio label="FLOOR">楼层</el-radio>
            <el-radio label="AREA">库区</el-radio>
            <el-radio label="BIN">库位</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="库区编码" prop="areaCode">
          <el-input
            v-model="formData.areaCode"
            placeholder="请输入库区编码（如：SITE_001, WH_A）"
            maxlength="50"
          />
        </el-form-item>
        <el-form-item label="库区名称" prop="areaName">
          <el-input
            v-model="formData.areaName"
            placeholder="请输入库区名称"
            maxlength="100"
          />
        </el-form-item>
        <el-form-item
          v-if="formData.areaLevel === 'SITE' || formData.areaLevel === 'WAREHOUSE'"
          label="地址"
          prop="address"
        >
          <el-input
            v-model="formData.address"
            placeholder="请输入地址"
            maxlength="200"
          />
        </el-form-item>
        <el-form-item label="位置描述" prop="locationDesc">
          <el-input
            v-model="formData.locationDesc"
            placeholder="请输入位置描述"
            maxlength="200"
          />
        </el-form-item>
        <el-divider>阈值设置</el-divider>
        <el-form-item label="温度范围">
          <div class="range-input">
            <el-input-number
              v-model="formData.temperatureThresholdMin"
              :min="-50"
              :max="50"
              :step="0.1"
              :precision="2"
            />
            <span class="range-separator">~</span>
            <el-input-number
              v-model="formData.temperatureThresholdMax"
              :min="-50"
              :max="50"
              :step="0.1"
              :precision="2"
            />
          </div>
        </el-form-item>
        <el-form-item label="湿度范围">
          <div class="range-input">
            <el-input-number
              v-model="formData.humidityThresholdMin"
              :min="0"
              :max="100"
              :step="0.1"
              :precision="2"
            />
            <span class="range-separator">~</span>
            <el-input-number
              v-model="formData.humidityThresholdMax"
              :min="0"
              :max="100"
              :step="0.1"
              :precision="2"
            />
          </div>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-switch
            v-model="formData.status"
            :active-value="1"
            :inactive-value="0"
            active-text="启用"
            inactive-text="禁用"
          />
        </el-form-item>
        <el-form-item label="告警" prop="alarmEnabled">
          <el-switch
            v-model="formData.alarmEnabled"
            :active-value="1"
            :inactive-value="0"
            active-text="启用告警"
            inactive-text="关闭告警"
          />
        </el-form-item>
        <el-form-item label="排序号" prop="sortNo">
          <el-input-number v-model="formData.sortNo" :min="0" :max="999999" />
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input
            v-model="formData.remark"
            type="textarea"
            :rows="3"
            placeholder="请输入备注信息"
            maxlength="500"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSubmit" :loading="submitLoading">确定</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 右键菜单 -->
    <el-popover
      ref="contextMenuRef"
      placement="bottom-start"
      trigger="manual"
      :visible="contextMenuVisible"
      width="200"
    >
      <div class="context-menu">
        <el-button size="small" text @click="openCreateDialog(contextNodeData)">
          <el-icon><FolderAdd /></el-icon>
          <span>新增子节点</span>
        </el-button>
        <el-button size="small" text @click="openEditDialog(contextNodeData)">
          <el-icon><Edit /></el-icon>
          <span>编辑节点</span>
        </el-button>
        <el-button size="small" text @click="toggleStatus(contextNodeData)">
          <el-icon><SwitchButton /></el-icon>
          <span>{{ contextNodeData?.status === 1 ? '禁用' : '启用' }}</span>
        </el-button>
        <el-button size="small" text @click="toggleAlarm(contextNodeData)">
          <el-icon><Bell /></el-icon>
          <span>{{ contextNodeData?.alarmEnabled === 1 ? '关闭告警' : '启用告警' }}</span>
        </el-button>
        <el-button size="small" text @click="handleMove(contextNodeData)" type="warning">
          <el-icon><Position /></el-icon>
          <span>移动节点</span>
        </el-button>
        <el-button size="small" text @click="handleDelete(contextNodeData)" type="danger">
          <el-icon><Delete /></el-icon>
          <span>删除节点</span>
        </el-button>
      </div>
    </el-popover>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, nextTick } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Search,
  CircleCloseFilled,
  Bell,
  Mute,
  FolderAdd,
  Edit,
  Delete,
  SwitchButton,
  Position,
  Plus,
  Upload,
  Download
} from '@element-plus/icons-vue'
import { areaApi } from '@/api/area'

// 响应式数据
const treeData = ref([])
const selectedNode = ref(null)
const childAreas = ref([])
const searchText = ref('')
const dialogVisible = ref(false)
const dialogTitle = ref('')
const submitLoading = ref(false)
const contextMenuVisible = ref(false)
const contextNodeData = ref(null)
const treeRef = ref()
const formRef = ref()
const contextMenuRef = ref()

// 表单数据
const formData = reactive({
  id: null,
  parentId: null,
  areaCode: '',
  areaName: '',
  areaLevel: 'AREA',
  address: '',
  locationDesc: '',
  temperatureThresholdMin: -20.00,
  temperatureThresholdMax: 8.00,
  humidityThresholdMin: 30.00,
  humidityThresholdMax: 70.00,
  alarmEnabled: 1,
  status: 1,
  sortNo: 0,
  remark: ''
})

// 父级选项
const parentOptions = ref([])

// 树形结构配置
const treeProps = {
  children: 'children',
  label: 'areaName'
}

// 级联选择器配置
const cascaderProps = {
  value: 'id',
  label: 'areaName',
  children: 'children',
  checkStrictly: true,
  emitPath: false,
  expandTrigger: 'hover'
}

// 表单验证规则
const formRules = {
  areaCode: [
    { required: true, message: '请输入库区编码', trigger: 'blur' },
    { min: 2, max: 50, message: '库区编码长度应在2-50个字符之间', trigger: 'blur' },
    { pattern: /^[A-Z0-9_-]+$/, message: '库区编码只能包含大写字母、数字、下划线和横线', trigger: 'blur' }
  ],
  areaName: [
    { required: true, message: '请输入库区名称', trigger: 'blur' },
    { min: 2, max: 100, message: '库区名称长度应在2-100个字符之间', trigger: 'blur' }
  ],
  areaLevel: [
    { required: true, message: '请选择库区层级', trigger: 'change' }
  ]
}

// 获取层级标签文本
const getLevelLabel = (level) => {
  const labels = {
    SITE: '站点',
    WAREHOUSE: '仓库',
    FLOOR: '楼层',
    AREA: '库区',
    BIN: '库位'
  }
  return labels[level] || level
}

// 获取层级标签类型
const getLevelTagType = (level) => {
  const types = {
    SITE: 'primary',
    WAREHOUSE: 'success',
    FLOOR: 'warning',
    AREA: 'info',
    BIN: 'danger'
  }
  return types[level] || 'info'
}

// 树形数据过滤
const filterMethod = (value, data) => {
  if (!value) return true
  return data.areaName.toLowerCase().includes(value.toLowerCase()) ||
         data.areaCode.toLowerCase().includes(value.toLowerCase())
}

// 过滤树
const filterTree = (value) => {
  treeRef.value.filter(value)
}

// 加载树形数据
const loadTreeData = async () => {
  try {
    const response = await areaApi.getAreaTree()
    treeData.value = response.data || []
    buildParentOptions()
  } catch (error) {
    ElMessage.error('获取库区数据失败')
    console.error(error)
  }
}

// 加载选中节点的子节点
const loadChildren = async (nodeId) => {
  try {
    const response = await areaApi.getChildAreasByParentId(nodeId)
    childAreas.value = response.data || []
  } catch (error) {
    ElMessage.error('获取子库区数据失败')
    console.error(error)
  }
}

// 构建父级选项
const buildParentOptions = () => {
  const buildOptions = (nodes, path = []) => {
    return nodes.map(node => {
      const newPath = [...path, node]
      return {
        id: node.id,
        areaName: node.areaName,
        children: node.children ? buildOptions(node.children, newPath) : []
      }
    })
  }
  parentOptions.value = buildOptions(treeData.value)
}

// 树节点点击事件
const onTreeNodeClick = async (data) => {
  selectedNode.value = data
  await loadChildren(data.id)
}

// 选中节点在树中定位
const selectNodeInTree = (node) => {
  selectedNode.value = node
  treeRef.value.setCurrentKey(node.id)
  loadChildren(node.id)
}

// 右键菜单事件
const onRightClick = (event, data, node, component) => {
  event.preventDefault()
  contextNodeData.value = data
  contextMenuVisible.value = true

  // 定位右键菜单
  const menu = contextMenuRef.value.$refs.reference
  if (menu) {
    menu.style.left = event.clientX + 'px'
    menu.style.top = event.clientY + 'px'
  }
}

// 隐藏右键菜单
const hideContextMenu = () => {
  contextMenuVisible.value = false
}

// 点击文档隐藏右键菜单
document.addEventListener('click', () => {
  if (contextMenuVisible.value) {
    contextMenuVisible.value = false
  }
})

// 打开新增对话框
const openCreateDialog = (parentNode) => {
  dialogTitle.value = '新增库区'
  Object.assign(formData, {
    id: null,
    parentId: parentNode ? parentNode.id : null,
    areaCode: '',
    areaName: '',
    areaLevel: 'AREA',
    address: '',
    locationDesc: '',
    temperatureThresholdMin: -20.00,
    temperatureThresholdMax: 8.00,
    humidityThresholdMin: 30.00,
    humidityThresholdMax: 70.00,
    alarmEnabled: 1,
    status: 1,
    sortNo: 0,
    remark: ''
  })
  dialogVisible.value = true
}

// 打开编辑对话框
const openEditDialog = (nodeData) => {
  dialogTitle.value = '编辑库区'
  Object.assign(formData, { ...nodeData })
  dialogVisible.value = true
}

// 处理提交
const handleSubmit = async () => {
  await formRef.value.validate()

  submitLoading.value = true
  try {
    if (formData.id) {
      // 更新
      await areaApi.updateArea(formData.id, formData)
      ElMessage.success('更新成功')
    } else {
      // 创建
      await areaApi.createArea(formData)
      ElMessage.success('创建成功')
    }

    dialogVisible.value = false
    await loadTreeData()

    // 如果是编辑操作，刷新选中节点
    if (selectedNode.value && selectedNode.value.id === formData.id) {
      selectedNode.value = treeData.value.find(node => node.id === formData.id)
      await loadChildren(formData.id)
    }
  } catch (error) {
    console.error(error)
    ElMessage.error(error.response?.data?.message || '操作失败')
  } finally {
    submitLoading.value = false
  }
}

// 处理对话框关闭
const handleDialogClose = (done) => {
  if (submitLoading.value) return
  done()
}

// 切换状态
const toggleStatus = async (node) => {
  try {
    await ElMessageBox.confirm(
      `确认${node.status === 1 ? '禁用' : '启用'}该库区吗？`,
      '提示',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    const newStatus = node.status === 1 ? 0 : 1
    await areaApi.updateArea(node.id, { ...node, status: newStatus })
    ElMessage.success(`${node.status === 1 ? '禁用' : '启用'}成功`)

    // 更新本地数据
    node.status = newStatus
    if (selectedNode.value && selectedNode.value.id === node.id) {
      selectedNode.value.status = newStatus
    }
    if (childAreas.value) {
      const child = childAreas.value.find(c => c.id === node.id)
      if (child) child.status = newStatus
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('操作失败')
    }
  }
}

// 切换告警状态
const toggleAlarm = async (node) => {
  try {
    await ElMessageBox.confirm(
      `确认${node.alarmEnabled === 1 ? '关闭' : '启用'}该库区告警吗？`,
      '提示',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    const newAlarmEnabled = node.alarmEnabled === 1 ? 0 : 1
    await areaApi.updateArea(node.id, { ...node, alarmEnabled: newAlarmEnabled })
    ElMessage.success(`${node.alarmEnabled === 1 ? '关闭' : '启用'}告警成功`)

    // 更新本地数据
    node.alarmEnabled = newAlarmEnabled
    if (selectedNode.value && selectedNode.value.id === node.id) {
      selectedNode.value.alarmEnabled = newAlarmEnabled
    }
    if (childAreas.value) {
      const child = childAreas.value.find(c => c.id === node.id)
      if (child) child.alarmEnabled = newAlarmEnabled
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('操作失败')
    }
  }
}

// 处理删除
const handleDelete = async (node) => {
  try {
    await ElMessageBox.confirm(
      `确认删除库区 "${node.areaName}" 吗？`,
      '提示',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    await areaApi.deleteArea(node.id)
    ElMessage.success('删除成功')
    await loadTreeData()

    if (selectedNode.value && selectedNode.value.id === node.id) {
      selectedNode.value = null
      childAreas.value = []
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

// 处理移动
const handleMove = async (node) => {
  try {
    const result = await ElMessageBox.prompt(
      '请选择目标父库区ID（顶级节点请输入-1）：',
      '移动库区',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        inputPlaceholder: '请输入目标父库区ID',
        inputValue: node.parentId ? node.parentId.toString() : '-1',
        inputPattern: /^-?\d+$/,
        inputErrorMessage: '请输入有效的库区ID'
      }
    )

    const targetParentId = parseInt(result.value)

    // 确认移动操作
    await ElMessageBox.confirm(
      `确认将 "${node.areaName}" 移动到ID为 ${targetParentId === -1 ? '顶级节点' : targetParentId} 的库区下吗？`,
      '移动确认',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    await areaApi.moveArea(node.id, targetParentId)
    ElMessage.success('移动成功')
    await loadTreeData()

    // 更新选中节点
    if (selectedNode.value && selectedNode.value.id === node.id) {
      selectedNode.value = treeData.value.find(n => n.id === node.id)
    }
  } catch (error) {
    if (error !== 'cancel' && error !== 'closed') {
      ElMessage.error('移动失败: ' + (error.response?.data?.message || error.message))
    }
  }
}

// 处理导入
const handleImport = () => {
  ElMessage.info('批量导入功能正在开发中...')
}

// 处理导出
const handleExport = () => {
  ElMessage.info('导出功能正在开发中...')
}

// 层级变化事件
const onLevelChange = (level) => {
  // 根据层级设置默认值
  if (level === 'SITE' || level === 'WAREHOUSE') {
    if (!formData.address) {
      formData.address = ''
    }
  }
}

onMounted(async () => {
  await loadTreeData()
})
</script>

<style scoped>
.warehouse-area-page {
  padding: 20px;
  height: 100%;
  display: flex;
  flex-direction: column;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding-bottom: 10px;
  border-bottom: 1px solid #ebeef5;
}

.page-header h2 {
  margin: 0;
  font-size: 24px;
  font-weight: 600;
  color: #303133;
}

.content-wrapper {
  flex: 1;
  display: flex;
  gap: 20px;
  overflow: hidden;
}

.tree-panel {
  width: 350px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  display: flex;
  flex-direction: column;
  background: #fff;
  height: calc(100vh - 160px); /* Adjust height based on header */
}

.panel-header {
  padding: 12px 16px;
  border-bottom: 1px solid #ebeef5;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.panel-header h3 {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.detail-panel {
  flex: 1;
  display: flex;
  flex-direction: column;
  background: #fff;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  overflow: auto;
  padding: 20px;
  height: calc(100vh - 160px); /* Adjust height based on header */
}

.detail-content {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.basic-info-card {
  border: 1px solid #ebeef5;
  border-radius: 4px;
  overflow: hidden;
}

.card-header {
  padding: 16px;
  background: #fafafa;
  border-bottom: 1px solid #ebeef5;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.header-left h3 {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  color: #303133;
}

.card-body {
  padding: 16px;
}

.info-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
}

.info-row {
  display: flex;
  margin-bottom: 12px;
  min-width: 0; /* 允许flex item收缩 */
}

.info-row.full-width {
  grid-column: 1 / -1; /* 跨越所有列 */
  flex: 100%;
}

.info-item {
  flex: 1;
  min-width: 0; /* 允许收缩 */
  margin-bottom: 8px;
}

.info-item label {
  font-weight: 600;
  color: #606266;
  margin-right: 8px;
  min-width: 80px;
  display: inline-block;
}

.info-item span {
  color: #303133;
  word-break: break-word;
}

.threshold-section {
  margin-top: 16px;
  padding-top: 16px;
  border-top: 1px solid #ebeef5;
  grid-column: 1 / -1; /* 跨越所有列 */
}

.threshold-section h4 {
  margin: 0 0 12px 0;
  font-size: 14px;
  font-weight: 600;
  color: #303133;
}

.threshold-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
}

.threshold-item {
  display: flex;
  flex-direction: column;
}

.threshold-item label {
  font-weight: 600;
  color: #606266;
  margin-bottom: 4px;
  font-size: 12px;
}

.threshold-item span {
  color: #303133;
}

.child-areas-section {
  border: 1px solid #ebeef5;
  border-radius: 4px;
  padding: 16px;
  margin-top: 20px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.section-header h3 {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.custom-tree {
  flex: 1;
  overflow: auto;
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

.node-status-icons {
  display: flex;
  align-items: center;
  margin-right: 8px;
  flex-shrink: 0;
}

.status-icon {
  font-size: 12px;
  margin-right: 4px;
}

.status-icon.disabled {
  color: #f56c6c;
}

.status-icon.alarm-disabled {
  color: #e6a23c;
}

.node-actions {
  opacity: 0;
  transition: opacity 0.2s;
  flex-shrink: 0;
}

.custom-tree .el-tree-node:hover .node-actions {
  opacity: 1;
}

.action-btn {
  padding: 2px !important;
  margin-left: 4px;
}

.action-btn:focus {
  color: var(--el-color-primary);
}

.context-menu {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.range-input {
  display: flex;
  align-items: center;
  gap: 8px;
}

.range-separator {
  color: #909399;
}

.empty-state {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100%;
  color: #909399;
}

.dialog-footer {
  text-align: right;
}
</style>