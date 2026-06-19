<template>
  <Layout>
    <div class="warehouse-area-content">
      <div class="page-header">
        <h1>库区管理</h1>
        <div class="header-actions">
          <el-button type="primary" @click="openCreateDialog(null)">
            <el-icon><Plus /></el-icon>
            新增顶级节点
          </el-button>
          <el-button @click="handleImport">
            <el-icon><Upload /></el-icon>
            批量导入
          </el-button>
          <input
            ref="importInputRef"
            type="file"
            accept=".csv"
            class="hidden-file-input"
            @change="handleImportFile"
          />
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
            <div class="panel-title-row">
              <div>
                <h3>库区结构</h3>
                <p>{{ areaStats.total }} 个节点 · {{ areaStats.enabled }} 个启用</p>
              </div>
              <el-button link type="primary" :icon="Refresh" @click="loadTreeData" />
            </div>
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
                <div class="node-main">
                  <span class="node-label">{{ data.areaName }}</span>
                  <div class="node-meta">
                    <span class="node-code">[{{ data.areaCode }}]</span>
                    <el-tag
                      size="small"
                      :type="getLevelTagType(data.areaLevel)"
                      class="level-tag"
                    >
                      {{ getLevelLabel(data.areaLevel) }}
                    </el-tag>
                    <div class="node-status-icons">
                      <template v-if="data.status === 0">
                        <el-tooltip content="已禁用" placement="top">
                          <el-icon class="status-icon disabled"><CircleCloseFilled /></el-icon>
                        </el-tooltip>
                      </template>
                      <template v-if="data.alarmEnabled === 0">
                        <el-tooltip content="告警已关闭" placement="top">
                          <el-icon class="status-icon alarm-disabled"><Mute /></el-icon>
                        </el-tooltip>
                      </template>
                    </div>
                  </div>
                </div>
                <div class="node-actions" @click.stop>
                  <el-button
                    size="small"
                    link
                    @click="openCreateDialog(data)"
                    :icon="FolderAdd"
                    class="action-btn"
                  />
                  <el-button
                    size="small"
                    link
                    @click="openEditDialog(data)"
                    :icon="Edit"
                    class="action-btn"
                  />
                  <el-button
                    size="small"
                    link
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
            <div class="area-overview">
              <div class="overview-main">
                <div class="breadcrumb-line">{{ selectedAreaPath }}</div>
                <div class="area-title-row">
                  <h2>{{ selectedNode.areaName }}</h2>
                  <el-tag :type="getLevelTagType(selectedNode.areaLevel)">
                    {{ getLevelLabel(selectedNode.areaLevel) }}
                  </el-tag>
                  <el-tag :type="selectedNode.status === 1 ? 'success' : 'danger'">
                    {{ selectedNode.status === 1 ? '启用' : '禁用' }}
                  </el-tag>
                </div>
                <div class="area-subtitle">
                  {{ selectedNode.areaCode }} · {{ selectedNode.address || selectedNode.locationDesc || '暂无位置描述' }}
                </div>
              </div>
              <div class="overview-actions">
                <el-button :icon="Edit" @click="openEditDialog(selectedNode)">编辑</el-button>
                <el-dropdown trigger="click" @command="command => handleSelectedCommand(command, selectedNode)">
                  <el-button type="primary">
                    管理操作
                    <el-icon class="el-icon--right"><ArrowDown /></el-icon>
                  </el-button>
                  <template #dropdown>
                    <el-dropdown-menu>
                      <el-dropdown-item command="create">新增子库区</el-dropdown-item>
                      <el-dropdown-item command="move">移动库区</el-dropdown-item>
                      <el-dropdown-item command="toggle-status">
                        {{ selectedNode.status === 1 ? '禁用库区' : '启用库区' }}
                      </el-dropdown-item>
                      <el-dropdown-item command="toggle-alarm">
                        {{ selectedNode.alarmEnabled === 1 ? '关闭告警' : '启用告警' }}
                      </el-dropdown-item>
                      <el-dropdown-item command="delete" divided>删除库区</el-dropdown-item>
                    </el-dropdown-menu>
                  </template>
                </el-dropdown>
              </div>
            </div>

            <div class="area-metric-grid">
              <div class="area-metric">
                <span>直属子库区</span>
                <strong>{{ childAreas.length }}</strong>
              </div>
              <div class="area-metric">
                <span>告警开关</span>
                <strong :class="{ muted: selectedNode.alarmEnabled !== 1 }">
                  {{ selectedNode.alarmEnabled === 1 ? '启用' : '关闭' }}
                </strong>
              </div>
              <div class="area-metric">
                <span>温度阈值</span>
                <strong>{{ thresholdText(selectedNode.temperatureThresholdMin, selectedNode.temperatureThresholdMax, '℃') }}</strong>
              </div>
              <div class="area-metric">
                <span>湿度阈值</span>
                <strong>{{ thresholdText(selectedNode.humidityThresholdMin, selectedNode.humidityThresholdMax, '%') }}</strong>
              </div>
            </div>

            <div class="info-and-threshold">
              <section class="detail-section">
                <div class="section-header compact">
                  <h3>基础信息</h3>
                </div>
                <div class="description-grid">
                  <div>
                    <label>库区编码</label>
                    <span>{{ selectedNode.areaCode || '-' }}</span>
                  </div>
                  <div>
                    <label>层级类型</label>
                    <span>{{ getLevelLabel(selectedNode.areaLevel) }}</span>
                  </div>
                  <div>
                    <label>排序号</label>
                    <span>{{ selectedNode.sortNo ?? '-' }}</span>
                  </div>
                  <div>
                    <label>状态</label>
                    <span>{{ selectedNode.status === 1 ? '启用' : '禁用' }}</span>
                  </div>
                  <div class="span-2">
                    <label>地址</label>
                    <span>{{ selectedNode.address || '-' }}</span>
                  </div>
                  <div class="span-2">
                    <label>位置描述</label>
                    <span>{{ selectedNode.locationDesc || '-' }}</span>
                  </div>
                  <div class="span-2">
                    <label>备注</label>
                    <span>{{ selectedNode.remark || '-' }}</span>
                  </div>
                </div>
              </section>

              <section class="detail-section">
                <div class="section-header compact">
                  <h3>运行边界</h3>
                </div>
                <div class="threshold-cards">
                  <div class="threshold-card">
                    <span>温度下限</span>
                    <strong>{{ formatThreshold(selectedNode.temperatureThresholdMin, '℃') }}</strong>
                  </div>
                  <div class="threshold-card">
                    <span>温度上限</span>
                    <strong>{{ formatThreshold(selectedNode.temperatureThresholdMax, '℃') }}</strong>
                  </div>
                  <div class="threshold-card">
                    <span>湿度下限</span>
                    <strong>{{ formatThreshold(selectedNode.humidityThresholdMin, '%') }}</strong>
                  </div>
                  <div class="threshold-card">
                    <span>湿度上限</span>
                    <strong>{{ formatThreshold(selectedNode.humidityThresholdMax, '%') }}</strong>
                  </div>
                </div>
              </section>
            </div>

            <!-- 子库区列表 -->
            <div class="child-areas-section ccg-table-panel">
              <div class="section-header">
                <div>
                  <h3>直属子库区</h3>
                  <p>{{ childAreas.length ? '点击名称可切换查看对应库区' : '当前库区暂无下级节点' }}</p>
                </div>
                <el-button type="primary" plain :icon="Plus" @click="openCreateDialog(selectedNode)">新增子库区</el-button>
              </div>

              <el-table
                :data="childAreas"
                style="width: 100%"
                row-key="id"
                stripe
              >
                <el-table-column prop="areaName" label="库区名称" min-width="210" show-overflow-tooltip>
                  <template #default="{ row }">
                    <div class="child-area-main">
                      <el-button link type="primary" @click="selectNodeInTree(row)">{{ row.areaName }}</el-button>
                      <span>{{ row.areaCode }}</span>
                    </div>
                  </template>
                </el-table-column>
                <el-table-column prop="areaLevel" label="层级" width="96" align="center">
                  <template #default="{ row }">
                    <el-tag size="small" :type="getLevelTagType(row.areaLevel)">{{ getLevelLabel(row.areaLevel) }}</el-tag>
                  </template>
                </el-table-column>
                <el-table-column prop="status" label="状态" width="88" align="center">
                  <template #default="{ row }">
                    <el-tag :type="row.status === 1 ? 'success' : 'danger'">
                      {{ row.status === 1 ? '启用' : '禁用' }}
                    </el-tag>
                  </template>
                </el-table-column>
                <el-table-column prop="alarmEnabled" label="告警" width="88" align="center">
                  <template #default="{ row }">
                    <el-tag :type="row.alarmEnabled === 1 ? 'success' : 'warning'">
                      {{ row.alarmEnabled === 1 ? '开启' : '关闭' }}
                    </el-tag>
                  </template>
                </el-table-column>
                <el-table-column prop="locationDesc" label="位置描述" min-width="180" show-overflow-tooltip>
                  <template #default="{ row }">{{ row.locationDesc || row.address || '-' }}</template>
                </el-table-column>
                <el-table-column label="阈值" min-width="180" show-overflow-tooltip>
                  <template #default="{ row }">
                    <span>{{ thresholdText(row.temperatureThresholdMin, row.temperatureThresholdMax, '℃') }}</span>
                    <span class="muted-text"> / {{ thresholdText(row.humidityThresholdMin, row.humidityThresholdMax, '%') }}</span>
                  </template>
                </el-table-column>
                <el-table-column prop="sortNo" label="排序" width="76" align="center" />
                <el-table-column label="操作" width="162" fixed="right" align="center">
                  <template #default="{ row }">
                    <div class="row-actions">
                      <el-button link type="primary" @click="openEditDialog(row)">编辑</el-button>
                      <el-dropdown trigger="click" @command="command => handleSelectedCommand(command, row)">
                        <el-button link :icon="MoreFilled" />
                        <template #dropdown>
                          <el-dropdown-menu>
                            <el-dropdown-item command="create">新增子库区</el-dropdown-item>
                            <el-dropdown-item command="move">移动</el-dropdown-item>
                            <el-dropdown-item command="toggle-status">
                              {{ row.status === 1 ? '禁用' : '启用' }}
                            </el-dropdown-item>
                            <el-dropdown-item command="toggle-alarm">
                              {{ row.alarmEnabled === 1 ? '关闭告警' : '启用告警' }}
                            </el-dropdown-item>
                            <el-dropdown-item command="delete" divided>删除</el-dropdown-item>
                          </el-dropdown-menu>
                        </template>
                      </el-dropdown>
                    </div>
                  </template>
                </el-table-column>
              </el-table>
            </div>
          </div>
          <div v-else class="empty-state">
            <el-empty description="请选择左侧库区查看详细信息">
              <template #default>
                <div class="empty-actions">
                  <el-button type="primary" :icon="Plus" @click="openCreateDialog(null)">
                    新增顶级节点
                  </el-button>
                  <el-button :icon="Upload" @click="handleImport">
                    批量导入
                  </el-button>
                </div>
              </template>
            </el-empty>
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
              <el-radio value="SITE">站点</el-radio>
              <el-radio value="WAREHOUSE">仓库</el-radio>
              <el-radio value="FLOOR">楼层</el-radio>
              <el-radio value="AREA">库区</el-radio>
              <el-radio value="BIN">库位</el-radio>
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
      <div
        v-show="contextMenuVisible"
        ref="contextMenuRef"
        class="context-menu-popover"
        :style="{ left: `${contextMenuPosition.x}px`, top: `${contextMenuPosition.y}px` }"
        @click.stop
      >
        <div class="context-menu">
          <el-button size="small" link @click="openCreateDialog(contextNodeData)">
            <el-icon><FolderAdd /></el-icon>
            <span>新增子节点</span>
          </el-button>
          <el-button size="small" link @click="openEditDialog(contextNodeData)">
            <el-icon><Edit /></el-icon>
            <span>编辑节点</span>
          </el-button>
          <el-button size="small" link @click="toggleStatus(contextNodeData)">
            <el-icon><SwitchButton /></el-icon>
            <span>{{ contextNodeData?.status === 1 ? '禁用' : '启用' }}</span>
          </el-button>
          <el-button size="small" link @click="toggleAlarm(contextNodeData)">
            <el-icon><Bell /></el-icon>
            <span>{{ contextNodeData?.alarmEnabled === 1 ? '关闭告警' : '启用告警' }}</span>
          </el-button>
          <el-button size="small" link @click="handleMove(contextNodeData)" type="warning">
            <el-icon><Position /></el-icon>
            <span>移动节点</span>
          </el-button>
          <el-button size="small" link @click="handleDelete(contextNodeData)" type="danger">
            <el-icon><Delete /></el-icon>
            <span>删除节点</span>
          </el-button>
        </div>
      </div>
    </div>
  </Layout>
</template>

<script setup>
import { ref, reactive, onMounted, nextTick, computed } from 'vue'
import { useRouter } from 'vue-router'
import Layout from '@/components/Layout.vue'
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
  Download,
  Refresh,
  ArrowDown,
  MoreFilled
} from '@element-plus/icons-vue'
import { areaApi } from '@/api/area'

const router = useRouter()

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
const contextMenuPosition = ref({ x: 0, y: 0 })
const treeRef = ref()
const formRef = ref()
const contextMenuRef = ref()
const importInputRef = ref()

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

const flattenAreas = (nodes = []) => nodes.flatMap(node => [
  node,
  ...flattenAreas(node.children || [])
])

const areaStats = computed(() => {
  const nodes = flattenAreas(treeData.value)
  return {
    total: nodes.length,
    enabled: nodes.filter(node => node.status === 1).length,
    disabled: nodes.filter(node => node.status === 0).length,
    alarmOff: nodes.filter(node => node.alarmEnabled === 0).length
  }
})

const findPathById = (nodes, id, path = []) => {
  for (const node of nodes) {
    const nextPath = [...path, node]
    if (node.id === id) return nextPath
    const childPath = findPathById(node.children || [], id, nextPath)
    if (childPath.length) return childPath
  }
  return []
}

const selectedAreaPath = computed(() => {
  if (!selectedNode.value) return ''
  const path = findPathById(treeData.value, selectedNode.value.id)
  return path.length ? path.map(node => node.areaName).join(' / ') : selectedNode.value.areaName
})

const formatThreshold = (value, unit) => {
  const numberValue = Number(value)
  if (!Number.isFinite(numberValue)) return '-'
  return `${numberValue.toFixed(1)}${unit}`
}

const thresholdText = (min, max, unit) => `${formatThreshold(min, unit)} ~ ${formatThreshold(max, unit)}`

const handleSelectedCommand = (command, node) => {
  if (!node) return
  if (command === 'create') {
    openCreateDialog(node)
  } else if (command === 'move') {
    handleMove(node)
  } else if (command === 'toggle-status') {
    toggleStatus(node)
  } else if (command === 'toggle-alarm') {
    toggleAlarm(node)
  } else if (command === 'delete') {
    handleDelete(node)
  }
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
    // 修复：取实际的数据部分
    treeData.value = response.data?.data || []
    buildParentOptions()
  } catch (error) {
    ElMessage.error('获取库区数据失败')
    console.error(error)
    // 确保即使出错也设置一个空数组
    treeData.value = []
  }
}

// 加载选中节点的子节点
const loadChildren = async (nodeId) => {
  try {
    const response = await areaApi.getChildAreasByParentId(nodeId)
    childAreas.value = response.data?.data || []
  } catch (error) {
    ElMessage.error('获取子库区数据失败')
    console.error(error)
    // 确保即使出错也设置一个空数组
    childAreas.value = []
  }
}

// 构建父级选项
const buildParentOptions = () => {
  const buildOptions = (nodes, path = []) => {
    // 修复：确保 nodes 是数组
    if (!Array.isArray(nodes)) {
      return []
    }

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
  contextMenuPosition.value = { x: event.clientX, y: event.clientY }
  contextMenuVisible.value = true
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

const findNodeById = (nodes, id) => {
  for (const node of nodes) {
    if (node.id === id) return node
    const found = findNodeById(node.children || [], id)
    if (found) return found
  }
  return null
}

const exportRows = (nodes, path = []) => {
  return nodes.flatMap(node => {
    const currentPath = [...path, node.areaName]
    const row = {
      areaCode: node.areaCode,
      areaName: node.areaName,
      areaLevel: getLevelLabel(node.areaLevel),
      parentId: node.parentId || '',
      path: currentPath.join('/'),
      status: node.status === 1 ? '启用' : '禁用',
      alarmEnabled: node.alarmEnabled === 1 ? '启用' : '关闭',
      temperatureRange: `${node.temperatureThresholdMin ?? ''}~${node.temperatureThresholdMax ?? ''}`,
      humidityRange: `${node.humidityThresholdMin ?? ''}~${node.humidityThresholdMax ?? ''}`,
      address: node.address || '',
      locationDesc: node.locationDesc || '',
      remark: node.remark || ''
    }

    return [row, ...exportRows(node.children || [], currentPath)]
  })
}

// 处理导入
const handleImport = () => {
  importInputRef.value?.click()
}

const parseCsvLine = (line) => {
  const cells = []
  let current = ''
  let inQuotes = false

  for (let i = 0; i < line.length; i += 1) {
    const char = line[i]
    const nextChar = line[i + 1]

    if (char === '"' && inQuotes && nextChar === '"') {
      current += '"'
      i += 1
    } else if (char === '"') {
      inQuotes = !inQuotes
    } else if (char === ',' && !inQuotes) {
      cells.push(current.trim())
      current = ''
    } else {
      current += char
    }
  }

  cells.push(current.trim())
  return cells
}

const buildImportPayload = (row) => ({
  parentId: row.parentId ? Number(row.parentId) : null,
  areaCode: row.areaCode,
  areaName: row.areaName,
  areaLevel: normalizeAreaLevel(row.areaLevelCode || row.areaLevel || 'AREA'),
  address: row.address || '',
  locationDesc: row.locationDesc || '',
  temperatureThresholdMin: getRangeValue(row.temperatureThresholdMin, row.temperatureRange, 0, -20),
  temperatureThresholdMax: getRangeValue(row.temperatureThresholdMax, row.temperatureRange, 1, 8),
  humidityThresholdMin: getRangeValue(row.humidityThresholdMin, row.humidityRange, 0, 30),
  humidityThresholdMax: getRangeValue(row.humidityThresholdMax, row.humidityRange, 1, 70),
  alarmEnabled: row.alarmEnabled === '关闭' || row.alarmEnabled === '0' ? 0 : 1,
  status: row.status === '禁用' || row.status === '0' ? 0 : 1,
  sortNo: row.sortNo ? Number(row.sortNo) : 0,
  remark: row.remark || ''
})

const importHeaderMap = {
  库区编码: 'areaCode',
  库区名称: 'areaName',
  层级: 'areaLevel',
  上级ID: 'parentId',
  状态: 'status',
  告警: 'alarmEnabled',
  温度范围: 'temperatureRange',
  湿度范围: 'humidityRange',
  地址: 'address',
  位置描述: 'locationDesc',
  备注: 'remark'
}

const normalizeAreaLevel = (level) => {
  const levelMap = {
    站点: 'SITE',
    仓库: 'WAREHOUSE',
    楼层: 'FLOOR',
    库区: 'AREA',
    库位: 'BIN'
  }
  return levelMap[level] || level
}

const getRangeValue = (value, range, index, fallback) => {
  if (value !== undefined && value !== '') return Number(value)
  if (!range) return fallback
  const parts = String(range).split('~').map(item => item.trim())
  return parts[index] !== undefined && parts[index] !== '' ? Number(parts[index]) : fallback
}

const handleImportFile = async (event) => {
  const file = event.target.files?.[0]
  if (!file) return

  try {
    const text = await file.text()
    const lines = text.replace(/^\uFEFF/, '').split(/\r?\n/).filter(Boolean)
    if (lines.length < 2) {
      ElMessage.warning('导入文件为空')
      return
    }

    const headers = parseCsvLine(lines[0]).map(header => importHeaderMap[header] || header)
    const rows = lines.slice(1).map(line => {
      const values = parseCsvLine(line)
      return headers.reduce((record, header, index) => {
        record[header] = values[index] || ''
        return record
      }, {})
    }).filter(row => row.areaCode && row.areaName)

    if (!rows.length) {
      ElMessage.warning('未识别到有效库区数据')
      return
    }

    let successCount = 0
    const failedRows = []
    for (const row of rows) {
      try {
        await areaApi.createArea(buildImportPayload(row))
        successCount += 1
      } catch (error) {
        failedRows.push(`${row.areaCode}: ${error.response?.data?.message || error.message}`)
      }
    }

    await loadTreeData()
    ElMessage.success(`导入完成，成功 ${successCount} 条，失败 ${failedRows.length} 条`)
    if (failedRows.length) {
      console.warn('库区导入失败明细:', failedRows)
    }
  } catch (error) {
    ElMessage.error('导入失败，请检查 CSV 文件格式')
  } finally {
    event.target.value = ''
  }
}

// 处理导出
const handleExport = () => {
  const rows = exportRows(treeData.value)
  if (!rows.length) {
    ElMessage.info('暂无可导出的库区数据')
    return
  }

  const headers = [
    ['areaCode', '库区编码'],
    ['areaName', '库区名称'],
    ['areaLevel', '层级'],
    ['parentId', '上级ID'],
    ['path', '完整路径'],
    ['status', '状态'],
    ['alarmEnabled', '告警'],
    ['temperatureRange', '温度范围'],
    ['humidityRange', '湿度范围'],
    ['address', '地址'],
    ['locationDesc', '位置描述'],
    ['remark', '备注']
  ]
  const escapeCell = (value) => `"${String(value ?? '').replaceAll('"', '""')}"`
  const csv = [
    headers.map(([, label]) => escapeCell(label)).join(','),
    ...rows.map(row => headers.map(([key]) => escapeCell(row[key])).join(','))
  ].join('\n')

  const blob = new Blob(['\uFEFF' + csv], { type: 'text/csv;charset=utf-8;' })
  const url = URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = `库区数据_${new Date().toISOString().slice(0, 10)}.csv`
  link.click()
  URL.revokeObjectURL(url)
  ElMessage.success('库区数据已导出')
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
  const areaId = Number(router.currentRoute.value.query.areaId)
  if (areaId) {
    await nextTick()
    const target = findNodeById(treeData.value, areaId)
    if (target) {
      treeRef.value?.setCurrentKey(areaId)
      await onTreeNodeClick(target)
    }
  } else if (treeData.value.length) {
    await nextTick()
    const firstNode = treeData.value[0]
    treeRef.value?.setCurrentKey(firstNode.id)
    await onTreeNodeClick(firstNode)
  }
})
</script>

<style scoped>
.warehouse-area-content {
  padding: 20px 24px 28px;
  min-height: 100%;
  background: var(--ccg-bg);
  color: var(--ccg-text);
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 16px;
  margin-bottom: 16px;
}

.page-header h1 {
  margin: 0;
  font-size: 22px;
  line-height: 1.2;
  font-weight: 750;
  color: var(--ccg-text);
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}

.content-wrapper {
  display: grid;
  grid-template-columns: minmax(340px, 380px) minmax(0, 1fr);
  gap: 16px;
  align-items: start;
}

.tree-panel {
  border: 1px solid var(--ccg-border);
  border-radius: 8px;
  display: flex;
  flex-direction: column;
  background: #fff;
  min-height: 520px;
  max-height: calc(100vh - var(--ccg-header-height) - 96px);
  position: sticky;
  top: 16px;
  overflow: hidden;
  box-shadow: var(--ccg-shadow-sm);
}

.panel-header {
  padding: 14px 16px;
  border-bottom: 1px solid var(--ccg-border);
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.panel-title-row {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
}

.panel-header h3 {
  margin: 0;
  font-size: 16px;
  font-weight: 700;
  color: var(--ccg-text);
}

.panel-header p {
  margin: 4px 0 0;
  color: var(--ccg-muted);
  font-size: 12px;
}

.detail-panel {
  display: flex;
  flex-direction: column;
  min-width: 0;
  min-height: 520px;
}

.detail-content {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.area-overview,
.detail-section {
  background: #fff;
  border: 1px solid var(--ccg-border);
  border-radius: 8px;
  box-shadow: var(--ccg-shadow-sm);
}

.area-overview {
  display: flex;
  justify-content: space-between;
  gap: 18px;
  padding: 18px;
}

.overview-main {
  min-width: 0;
}

.breadcrumb-line {
  color: var(--ccg-muted);
  font-size: 12px;
  line-height: 1.4;
  margin-bottom: 6px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.area-title-row {
  display: flex;
  align-items: center;
  gap: 10px;
  min-width: 0;
}

.area-title-row h2 {
  margin: 0;
  color: var(--ccg-text);
  font-size: 22px;
  line-height: 1.25;
  font-weight: 760;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.area-subtitle {
  margin-top: 6px;
  color: var(--ccg-muted);
  font-size: 13px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.overview-actions {
  display: flex;
  align-items: flex-start;
  gap: 10px;
  flex-shrink: 0;
}

.area-metric-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 12px;
}

.area-metric {
  min-width: 0;
  padding: 14px 16px;
  border: 1px solid var(--ccg-border);
  border-radius: 8px;
  background: #fff;
  box-shadow: var(--ccg-shadow-sm);
}

.area-metric span,
.threshold-card span,
.description-grid label {
  display: block;
  color: var(--ccg-muted);
  font-size: 12px;
  line-height: 1.4;
}

.area-metric strong {
  display: block;
  margin-top: 6px;
  color: var(--ccg-text);
  font-size: 16px;
  line-height: 1.25;
  font-weight: 750;
  overflow-wrap: anywhere;
}

.area-metric strong.muted {
  color: var(--ccg-muted);
}

.info-and-threshold {
  display: grid;
  grid-template-columns: minmax(0, 1.4fr) minmax(320px, 0.8fr);
  gap: 16px;
}

.detail-section {
  padding: 16px;
}

.section-header.compact {
  margin-bottom: 12px;
}

.description-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 14px 18px;
}

.description-grid > div {
  min-width: 0;
}

.description-grid .span-2 {
  grid-column: 1 / -1;
}

.description-grid span {
  display: block;
  margin-top: 5px;
  color: var(--ccg-text);
  font-size: 14px;
  line-height: 1.55;
  overflow-wrap: anywhere;
}

.threshold-cards {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
}

.threshold-card {
  min-width: 0;
  padding: 12px;
  border: 1px solid var(--ccg-border);
  border-radius: 8px;
  background: var(--ccg-surface-soft);
}

.threshold-card strong {
  display: block;
  margin-top: 7px;
  color: var(--ccg-text);
  font-size: 18px;
  line-height: 1.2;
  font-weight: 760;
}

.child-areas-section {
  padding: 0;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 12px;
  padding: 16px;
  border-bottom: 1px solid var(--ccg-border);
}

.section-header h3 {
  margin: 0;
  font-size: 16px;
  font-weight: 700;
  color: var(--ccg-text);
}

.section-header p {
  margin: 4px 0 0;
  color: var(--ccg-muted);
  font-size: 12px;
}

.child-area-main {
  display: flex;
  flex-direction: column;
  gap: 2px;
  align-items: flex-start;
}

.child-area-main :deep(.el-button) {
  padding-left: 0;
  font-weight: 700;
}

.child-area-main span,
.muted-text {
  color: var(--ccg-muted);
  font-size: 12px;
}

.row-actions {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 4px;
  white-space: nowrap;
}

.custom-tree {
  flex: 1;
  overflow: auto;
  padding: 8px 6px 12px;
}

.custom-tree :deep(.el-tree-node__content) {
  height: auto;
  min-height: 52px;
  align-items: stretch;
  padding-top: 6px;
  padding-bottom: 6px;
}

.custom-tree :deep(.el-tree-node__expand-icon) {
  align-self: flex-start;
  margin-top: 10px;
}

.tree-node-content {
  flex: 1;
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 8px;
  font-size: 14px;
  padding-right: 8px;
  min-width: 0;
  width: 100%;
}

.node-main {
  min-width: 0;
  display: flex;
  flex: 1;
  flex-direction: column;
  gap: 5px;
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

.node-status-icons {
  display: flex;
  align-items: center;
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
  display: flex;
  align-items: center;
  margin-top: -2px;
}

.custom-tree .el-tree-node:hover .node-actions {
  opacity: 1;
}

.action-btn {
  width: 32px;
  min-width: 32px;
  min-height: 32px;
  padding: 4px !important;
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

.context-menu-popover {
  position: fixed;
  z-index: 2200;
  width: 200px;
  padding: 8px;
  background: #fff;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  box-shadow: 0 6px 18px rgba(0, 0, 0, 0.12);
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
  min-height: 460px;
  color: #909399;
  border-radius: 8px;
  background:
    linear-gradient(135deg, rgba(37, 99, 235, 0.04), rgba(16, 185, 129, 0.04)),
    #fff;
}

.empty-actions {
  display: flex;
  justify-content: center;
  gap: 10px;
  margin-top: 8px;
}

.dialog-footer {
  text-align: right;
}

.hidden-file-input {
  display: none;
}

/* Responsive design */
@media (max-width: 1200px) {
  .content-wrapper {
    grid-template-columns: 1fr;
  }

  .info-and-threshold,
  .area-metric-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .tree-panel {
    width: 100%;
    min-height: 360px;
    max-height: none;
    position: static;
  }
}

@media (max-width: 768px) {
  .warehouse-area-content {
    padding: 16px;
  }

  .page-header,
  .area-overview,
  .section-header {
    flex-direction: column;
  }

  .content-wrapper {
    grid-template-columns: 1fr;
  }

  .area-metric-grid,
  .info-and-threshold,
  .description-grid,
  .threshold-cards {
    grid-template-columns: 1fr;
  }

  .tree-panel {
    width: 100%;
    min-height: 340px;
  }

  .detail-panel {
    min-height: 420px;
  }
}
</style>
