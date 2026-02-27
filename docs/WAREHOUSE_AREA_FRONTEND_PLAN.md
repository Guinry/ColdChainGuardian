# 库区管理前端页面技术实现计划

## 1. 页面概述

根据设计说明，库区管理页面采用"左树右详情"的经典后台布局，实现对冷链仓储层级结构的可视化管理。

## 2. 技术栈与组件规划

### 2.1 前端技术栈
- Vue 3 (Composition API)
- Element Plus (UI组件库)
- Pinia (状态管理)
- Axios (HTTP客户端)
- vue-router (路由管理)

### 2.2 主要组件规划
- `WarehouseAreaManage.vue`: 主页面组件
- `AreaTreePanel.vue`: 左侧库区树组件
- `AreaDetailPanel.vue`: 右侧详情面板组件
- `AreaEditDrawer.vue`: 新增/编辑抽屉组件
- `MoveAreaDialog.vue`: 移动库区对话框
- `AreaImportModal.vue`: 批量导入模态框

## 3. 页面结构实现

### 3.1 主页面布局
```
┌─────────────────────────────────────────────────────────┐
│ 面包屑：系统管理 / 基础数据 / 库区管理                    │
├─────────────────────────────────────────────────────────┤
│ 全局操作按钮区：新增顶级节点、批量导入、导出、阈值说明  │
├────────────────────┬────────────────────────────────────┤
│ 左侧树形导航区         │ 右侧详情与列表区                     │
│ (280-340px)       │ (剩余空间)                          │
│ - 库区树             │ - 信息卡片/编辑表单                  │
│ - 搜索过滤           │ - 子库区列表/表格                    │
│ - 右键菜单           │ - 操作按钮组                         │
└────────────────────┴────────────────────────────────────┘
```

### 3.2 AreaTreePanel 组件设计
```vue
<template>
  <div class="area-tree-panel">
    <!-- 搜索栏 -->
    <el-input
      v-model="searchKeyword"
      placeholder="搜索编码/名称"
      prefix-icon="Search"
    />

    <!-- 树结构 -->
    <el-tree
      :data="treeData"
      :props="treeProps"
      :filter-node-method="filterNode"
      :expand-on-click-node="false"
      @node-click="onNodeClick"
      @node-contextmenu="showContextMenu"
      ref="treeRef"
    >
      <template #default="{ node, data }">
        <div class="tree-node-content">
          <span class="node-name">{{ data.areaName }}</span>
          <span class="node-code">{{ data.areaCode }}</span>
          <el-tag size="small" type="info">{{ data.areaLevel }}</el-tag>
          <div class="node-status-icons">
            <el-icon v-if="!data.status" color="gray">CircleClose</el-icon>
            <el-icon v-if="!data.alarmEnabled" color="orange">BellSlash</el-icon>
          </div>
          <div class="node-actions">
            <el-button-group>
              <el-button icon="Plus" @click.stop="addChild(data)" />
              <el-button icon="Edit" @click.stop="editNode(data)" />
              <el-button icon="SwitchButton" @click.stop="toggleStatus(data)" />
              <el-dropdown trigger="click" @command="handleMoreAction($event, data)">
                <el-button icon="MoreFilled" />
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item command="viewDevices">查看设备</el-dropdown-item>
                    <el-dropdown-item command="copyCode">复制编码</el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
            </el-button-group>
          </div>
        </div>
      </template>
    </el-tree>
  </div>
</template>
```

### 3.3 AreaDetailPanel 组件设计
```vue
<template>
  <div class="area-detail-panel">
    <!-- 顶部信息卡片 -->
    <el-card class="info-card">
      <template #header>
        <div class="card-header">
          <span>{{ currentArea.areaName }} <el-tag size="small">{{ currentArea.areaLevel }}</el-tag></span>
          <el-button type="primary" @click="enterEditMode">编辑</el-button>
        </div>
      </template>

      <div class="info-section">
        <div class="stat-item">
          <span>编码:</span>
          <span>{{ currentArea.areaCode }}</span>
        </div>
        <div class="stat-item">
          <span>子库区数:</span>
          <span>{{ childCount }}</span>
        </div>
        <div class="stat-item">
          <span>设备数:</span>
          <span>{{ deviceCount }}</span>
        </div>
      </div>
    </el-card>

    <!-- Tab切换 -->
    <el-tabs v-model="activeTab">
      <el-tab-pane label="基本信息" name="basic">
        <BasicInfoForm
          v-if="!isEditing"
          :area="currentArea"
        />
        <BasicInfoEditor
          v-else
          :area="currentArea"
          @save="saveBasicInfo"
          @cancel="exitEditMode"
        />
      </el-tab-pane>
      <el-tab-pane label="子库区列表" name="children">
        <ChildAreaTable :parentId="currentArea.id" />
      </el-tab-pane>
    </el-tabs>
  </div>
</template>
```

## 4. 接口对接规划

### 4.1 API接口映射
根据设计说明中的接口建议，对应到实际的后端接口：

```javascript
// 库区管理API
const warehouseAreaApi = {
  // 获取整棵树
  getTree: () => axios.get('/api/areas'),

  // 获取指定节点详情
  getById: (id) => axios.get(`/api/areas/${id}`),

  // 获取子节点列表
  getChildren: (parentId, params) =>
    axios.get(`/api/areas/parent/${parentId}`, { params }),

  // 新增库区
  create: (data) => axios.post('/api/areas', data),

  // 更新库区
  update: (id, data) => axios.put(`/api/areas/${id}`, data),

  // 删除库区
  delete: (id) => axios.delete(`/api/areas/${id}`),

  // 移动库区
  move: (id, targetParentId) =>
    axios.post(`/api/areas/${id}/move`, { targetParentId }),

  // 批量操作
  batch: (action, ids) =>
    axios.post('/api/areas/batch', { action, ids }),

  // 导出
  export: (params) =>
    axios.get('/api/areas/export', { params, responseType: 'blob' }),

  // 导入
  import: (formData) =>
    axios.post('/api/areas/import', formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
};
```

### 4.2 响应数据结构定义
```typescript
// 库区数据类型定义
interface Area {
  id: number;
  parentId: number | null;
  areaCode: string;
  areaName: string;
  areaLevel: 'SITE' | 'WAREHOUSE' | 'FLOOR' | 'AREA' | 'BIN';
  address?: string;
  locationDesc?: string;
  temperatureThresholdMin: number;
  temperatureThresholdMax: number;
  humidityThresholdMin: number;
  humidityThresholdMax: number;
  alarmEnabled: number; // 1:启用, 0:禁用
  status: number; // 1:启用, 0:禁用
  sortNo: number;
  remark?: string;
  creatorId?: number;
  updaterId?: number;
  createTime: string;
  updateTime: string;
  children?: Area[]; // 用于树形结构
}

// 分页响应类型
interface PageResponse<T> {
  success: boolean;
  message: string;
  data: T[];
  total: number;
  pageSize: number;
  pageNum: number;
}
```

## 5. 状态管理规划

### 5.1 Pinia Store 结构
```typescript
// stores/warehouseArea.ts
import { defineStore } from 'pinia';

export const useWarehouseAreaStore = defineStore('warehouseArea', {
  state: () => ({
    // 树状态
    treeData: [] as Area[],
    selectedNodeId: null as number | null,
    expandedNodeIds: [] as number[],

    // 详情状态
    currentArea: null as Area | null,
    isEditing: false,
    formDraft: {} as Partial<Area>,
    isDirty: false,

    // 子节点表格状态
    childrenList: [] as Area[],
    childrenLoading: false,
    pagination: {
      currentPage: 1,
      pageSize: 20,
      total: 0
    },

    // 全局搜索状态
    searchKeyword: '',
    filterParams: {
      areaLevel: '',
      status: null as number | null,
      alarmEnabled: null as number | null
    },

    // 权限
    permissions: [] as string[]
  }),

  actions: {
    // 获取树数据
    async fetchTree() {
      try {
        const response = await warehouseAreaApi.getTree();
        this.treeData = response.data;
      } catch (error) {
        console.error('Failed to fetch tree:', error);
      }
    },

    // 选中节点
    selectNode(nodeId: number) {
      this.selectedNodeId = nodeId;
      // 获取当前节点详情
      this.fetchCurrentArea(nodeId);
      // 获取子节点列表
      this.fetchChildren(nodeId);
    },

    // 其他操作...
  }
});
```

## 6. 路由配置
```javascript
// router/modules/warehouse-area.js
export default {
  path: '/warehouse-area',
  name: 'WarehouseArea',
  component: () => import('@/views/warehouse-area/WarehouseAreaManage.vue'),
  meta: {
    title: '库区管理',
    requiresAuth: true,
    permissions: ['area:view']
  }
};
```

## 7. 交互与异常处理

### 7.1 确认对话框处理
```javascript
// 在删除、移动等危险操作前显示确认对话框
const handleDelete = async (id) => {
  try {
    await ElMessageBox.confirm(
      '此操作将删除该库区，是否继续？',
      '提示',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
        distinguishCancelAndClose: true
      }
    );

    await warehouseAreaApi.delete(id);
    ElMessage.success('删除成功');
    // 更新视图
    refreshData();
  } catch (action) {
    if (action === 'cancel') {
      ElMessage.info('已取消删除');
    }
  }
};
```

### 7.2 表单校验规则
```javascript
// 校验规则
const validationRules = {
  areaCode: [
    { required: true, message: '请输入库区编码', trigger: 'blur' },
    { min: 2, max: 50, message: '编码长度应在2-50之间', trigger: 'blur' },
    { pattern: /^[A-Z0-9_-]+$/, message: '编码只能包含大写字母、数字、下划线和横线', trigger: 'blur' }
  ],
  areaName: [
    { required: true, message: '请输入库区名称', trigger: 'blur' },
    { min: 2, max: 100, message: '名称长度应在2-100之间', trigger: 'blur' }
  ],
  temperatureThresholdMin: [
    { required: true, message: '请输入最小温度阈值', trigger: 'blur' },
    { type: 'number', min: -50, max: 50, message: '温度应在-50到50度之间', trigger: 'blur' }
  ],
  temperatureThresholdMax: [
    { validator: validateTempRange, trigger: 'blur' } // 自定义校验：min < max
  ]
};
```

## 8. 权限控制

### 8.1 按角色控制UI元素
```vue
<template>
  <!-- ADMIN和MANAGER可以新增 -->
  <el-button
    v-if="hasPermission('area:create')"
    type="primary"
    @click="openCreateDrawer"
  >
    新增顶级节点
  </el-button>

  <!-- STAFF只能查看 -->
  <el-button
    v-if="hasPermission('area:view')"
    @click="viewDetails"
  >
    查看详情
  </el-button>
</template>

<script setup>
const { hasPermission } = usePermission();

// 权限检查方法
const usePermission = () => {
  const store = useWarehouseAreaStore();

  const hasPermission = (permission: string) => {
    return store.permissions.includes(permission);
  };

  return { hasPermission };
};
</script>
```

## 9. 实现优先级

### Phase 1: 核心功能
1. 左侧树形结构展示
2. 基本CRUD操作
3. 右侧详情面板

### Phase 2: 增强功能
1. 批量操作
2. 导入导出
3. 拖拽排序

### Phase 3: 优化功能
1. 高级搜索过滤
2. 权限精细化控制
3. 性能优化

## 10. 测试要点

1. 树形结构正确展示层级关系
2. CRUD操作数据一致性
3. 表单校验准确性
4. 权限控制有效性
5. 异常情况处理
6. 性能：大数据量下的渲染优化

---

这份实现计划可以根据设计说明提供完整、可落地的库区管理前端页面。页面将具备完整的层级管理、可视化展示、快速检索和高效操作功能。