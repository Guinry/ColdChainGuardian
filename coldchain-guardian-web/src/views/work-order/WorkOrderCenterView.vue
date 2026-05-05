<template>
  <Layout>
    <div class="work-order-center">
      <header class="page-head">
        <div>
          <h1>工单管理</h1>
          <p>跟踪告警消缺、巡检和维保工单，按优先级推进派工、处理与闭环。</p>
        </div>
        <div class="head-actions">
          <el-button :icon="Refresh" @click="fetchWorkOrders">刷新</el-button>
          <el-button type="primary" :icon="Plus" @click="showCreateModal = true">新建工单</el-button>
        </div>
      </header>

      <!-- 顶部状态看板 -->
      <el-row :gutter="20" class="kpi-cards">
        <el-col :span="6">
          <el-card class="kpi-card overdue">
            <div class="kpi-content">
              <div class="kpi-value">{{ kpiData.overdueCount }}</div>
              <div class="kpi-label">逾期工单</div>
            </div>
            <i class="el-icon-warning icon"></i>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="kpi-card pending">
            <div class="kpi-content">
              <div class="kpi-value">{{ kpiData.pendingCount }}</div>
              <div class="kpi-label">待处理</div>
            </div>
            <i class="el-icon-clock icon"></i>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="kpi-card processing">
            <div class="kpi-content">
              <div class="kpi-value">{{ kpiData.processingCount }}</div>
              <div class="kpi-label">处理中</div>
            </div>
            <i class="el-icon-refresh icon"></i>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="kpi-card completed">
            <div class="kpi-content">
              <div class="kpi-value">{{ kpiData.completedThisWeek }}</div>
              <div class="kpi-label">本周已闭环</div>
            </div>
            <i class="el-icon-check icon"></i>
          </el-card>
        </el-col>
      </el-row>

      <!-- 筛选栏 -->
      <el-card class="filter-section">
        <el-form :model="filterForm" inline>
          <el-form-item label="关键字">
            <el-input v-model="filterForm.keyword" placeholder="工单编号/标题" />
          </el-form-item>
          <el-form-item label="工单类型">
            <el-select v-model="filterForm.workType" placeholder="请选择">
              <el-option label="全部" value=""></el-option>
              <el-option label="告警消缺" value="ALERT_DEFECT"></el-option>
              <el-option label="日常巡检" value="ROUTINE_INSPECTION"></el-option>
              <el-option label="设备维保" value="EQUIPMENT_MAINTENANCE"></el-option>
            </el-select>
          </el-form-item>
          <el-form-item label="优先级">
            <el-select v-model="filterForm.priority" placeholder="请选择">
              <el-option label="全部" value=""></el-option>
              <el-option label="紧急" value="URGENT"></el-option>
              <el-option label="高" value="HIGH"></el-option>
              <el-option label="中" value="MEDIUM"></el-option>
              <el-option label="低" value="LOW"></el-option>
            </el-select>
          </el-form-item>
          <el-form-item label="状态">
            <el-select v-model="filterForm.status" placeholder="请选择">
              <el-option label="全部" value=""></el-option>
              <el-option label="待处理" value="PENDING"></el-option>
              <el-option label="处理中" value="PROCESSING"></el-option>
              <el-option label="待验收" value="VERIFYING"></el-option>
              <el-option label="已完成" value="COMPLETED"></el-option>
              <el-option label="已关闭" value="CLOSED"></el-option>
            </el-select>
          </el-form-item>
          <el-form-item label="责任人">
            <el-select v-model="filterForm.assigneeId" placeholder="请选择">
              <el-option label="全部" value=""></el-option>
              <el-option label="张三" value="1"></el-option>
              <el-option label="李四" value="2"></el-option>
              <el-option label="王五" value="3"></el-option>
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="fetchWorkOrders">查询</el-button>
            <el-button @click="resetFilter">重置</el-button>
          </el-form-item>
        </el-form>
      </el-card>

      <!-- 工单表格 -->
      <el-table
        :data="workOrders"
        v-loading="loading"
        class="work-order-table"
        stripe
        style="width: 100%"
      >
        <el-table-column prop="title" label="工单信息" min-width="220" show-overflow-tooltip>
          <template #default="{ row }">
            <div class="work-order-info">
              <div class="title">{{ row.title }}</div>
              <div class="id">#{{ row.id }}</div>
            </div>
          </template>
        </el-table-column>

        <el-table-column prop="workType" label="类型" width="96">
          <template #default="{ row }">
            <el-tag
              :type="getWorkTypeTagType(row.workType)"
              size="small"
            >
              {{ getWorkTypeName(row.workType) }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column prop="priority" label="优先级" width="82" align="center">
          <template #default="{ row }">
            <el-tag
              :type="getPriorityTagType(row.priority)"
              size="small"
            >
              {{ getPriorityName(row.priority) }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column prop="locationDetail" label="发生位置" min-width="148" show-overflow-tooltip>
          <template #default="{ row }">
            <div class="location-info">
              <el-tag type="info" size="small">{{ row.warehouseName }}</el-tag>
              <br />
              <span>{{ row.deviceName }}</span>
            </div>
          </template>
        </el-table-column>

        <el-table-column prop="assigneeName" label="责任人" width="116">
          <template #default="{ row }">
            <div class="assignee-info">
              <el-avatar size="small" :src="getUserAvatar(row.assigneeId)" />
              <span>{{ row.assigneeName }}</span>
            </div>
          </template>
        </el-table-column>

        <el-table-column prop="createTime" label="时间节点" width="132">
          <template #default="{ row }">
            <div class="timeline-info">
              <div>创建: {{ formatDate(row.createdAt) }}</div>
              <div v-if="row.completedAt">完成: {{ formatDate(row.completedAt) }}</div>
            </div>
          </template>
        </el-table-column>

        <el-table-column prop="status" label="状态" width="92" align="center">
          <template #default="{ row }">
            <el-tag
              :type="getStatusTagType(row.status)"
              size="small"
            >
              {{ getStatusName(row.status) }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column label="操作" width="148" align="center">
          <template #default="{ row }">
            <el-button size="small" @click="viewWorkOrder(row)">查看</el-button>
            <el-dropdown split-button size="small" @click="handleProcess(row)">
              处理
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item @click="handleAccept(row)" v-if="row.status === 'PENDING'">接受工单</el-dropdown-item>
                  <el-dropdown-item @click="handleComplete(row)" v-if="row.status === 'PROCESSING'">完成工单</el-dropdown-item>
                  <el-dropdown-item @click="handleVerify(row)" v-if="row.status === 'VERIFYING'">验收通过</el-dropdown-item>
                  <el-dropdown-item @click="handleClose(row)" v-if="row.status === 'COMPLETED'">关闭工单</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination">
        <el-pagination
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
          :current-page="pagination.currentPage"
          :page-sizes="[10, 20, 50, 100]"
          :page-size="pagination.pageSize"
          layout="total, sizes, prev, pager, next, jumper"
          :total="pagination.total"
        />
      </div>

      <!-- 新建工单模态框 -->
      <CreateWorkOrderModal
        :visible="showCreateModal"
        @close="showCreateModal = false"
        @submit="onWorkOrderCreated"
      />

      <!-- 工单详情抽屉 -->
      <WorkOrderDrawer
        :work-order-id="selectedWorkOrderId"
        :visible="showWorkOrderDrawer"
        @close="showWorkOrderDrawer = false"
        @updated="fetchWorkOrders"
      />
    </div>
  </Layout>
</template>

<script setup>
import { ref, reactive, onMounted, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';
import { Plus, Refresh } from '@element-plus/icons-vue';
import CreateWorkOrderModal from '@/views/work-order/components/CreateWorkOrderModal.vue';
import WorkOrderDrawer from '@/views/work-order/components/WorkOrderDrawer.vue';
import Layout from '@/components/Layout.vue';
import { workOrderApi } from '@/api/work-order';

const route = useRoute();
const router = useRouter();

// KPI 数据
const kpiData = ref({
  overdueCount: 0,
  pendingCount: 0,
  processingCount: 0,
  completedThisWeek: 0
});

// 表格数据
const workOrders = ref([]);
const loading = ref(false);

// 筛选表单
const filterForm = reactive({
  keyword: '',
  workType: '',
  priority: '',
  status: '',
  assigneeId: ''
});

const getQueryValue = (value) => Array.isArray(value) ? value[0] : value;

const applyRouteQuery = () => {
  filterForm.status = getQueryValue(route.query.status) ? String(getQueryValue(route.query.status)) : '';
  filterForm.keyword = getQueryValue(route.query.keyword) ? String(getQueryValue(route.query.keyword)) : '';
  filterForm.workType = getQueryValue(route.query.workType) ? String(getQueryValue(route.query.workType)) : '';
  filterForm.priority = getQueryValue(route.query.priority) ? String(getQueryValue(route.query.priority)) : '';
  filterForm.assigneeId = getQueryValue(route.query.assigneeId) ? String(getQueryValue(route.query.assigneeId)) : '';
};

// 分页
const pagination = reactive({
  currentPage: 1,
  pageSize: 10,
  total: 0
});

// 模态框和抽屉状态
const showCreateModal = ref(false);
const showWorkOrderDrawer = ref(false);
const selectedWorkOrderId = ref(null);

// 🌟 核心修复：定义缺失的数据适配器函数
const adaptWorkOrderData = (rawOrder) => {
  return {
    ...rawOrder,
    // 处理时间字段差异
    createdAt: rawOrder.createTime || rawOrder.createdAt,
    completedAt: rawOrder.completedTime || rawOrder.updateTime || rawOrder.completedAt,

    // 如果没有名字，暂时用 ID 兜底显示，防止页面空白
    assigneeName: rawOrder.assigneeName || (rawOrder.assigneeId ? `用户 ${rawOrder.assigneeId}` : '未分配'),
    warehouseName: rawOrder.warehouseName || (rawOrder.warehouseId ? `库区 ${rawOrder.warehouseId}` : '未知库区'),
    deviceName: rawOrder.deviceName || (rawOrder.deviceId ? `设备 ${rawOrder.deviceId}` : '未知设备'),

    // 确保状态值匹配 (防脏数据)
    status: rawOrder.status || 'PENDING'
  };
};

// 获取工单数据
const fetchWorkOrders = async () => {
  loading.value = true;

  try {
    // 🌟 核心修复：手动构建参数，只把有值的条件放进去，剔除空字符串
    const params = {
      page: pagination.currentPage,
      size: pagination.pageSize
    };

    if (filterForm.keyword) params.keyword = filterForm.keyword;
    if (filterForm.workType) params.workType = filterForm.workType;
    if (filterForm.priority) params.priority = filterForm.priority;
    if (filterForm.status) params.status = filterForm.status;
    if (filterForm.assigneeId) params.assigneeId = filterForm.assigneeId;

    // 发送请求，此时 URL 就会非常干净，比如: /api/work-orders?page=1&size=10
    const response = await workOrderApi.getList(params);

    if (response && response.data) {
      const paginationData = response.data.data || {};
      const rawOrders = paginationData.records || paginationData.data || [];

      // 现在这里调用 adaptWorkOrderData 就不会报错了
      workOrders.value = rawOrders.map(adaptWorkOrderData);
      pagination.total = paginationData.total || 0;
    } else {
      workOrders.value = [];
      pagination.total = 0;
    }
  } catch (error) {
    console.error('获取工单失败:', error);
    ElMessage.error('获取工单失败');
    workOrders.value = [];
    pagination.total = 0;
  } finally {
    loading.value = false;
  }
};

// 获取工单统计数据
const fetchWorkOrderStats = async () => {
  try {
    const response = await workOrderApi.getStats();
    kpiData.value = response.data.data || {
      overdueCount: 0,
      pendingCount: 0,
      processingCount: 0,
      completedThisWeek: 0
    };
  } catch (error) {
    console.error('获取工单统计数据失败:', error);
    ElMessage.error('获取工单统计数据失败');
  }
};

// 重置筛选条件
const resetFilter = () => {
  Object.keys(filterForm).forEach(key => {
    filterForm[key] = '';
  });
  pagination.currentPage = 1;
  fetchWorkOrders();
};

// 页码变化
const handleCurrentChange = (val) => {
  pagination.currentPage = val;
  fetchWorkOrders();
};

// 页大小变化
const handleSizeChange = (val) => {
  pagination.pageSize = val;
  pagination.currentPage = 1;
  fetchWorkOrders();
};

// 查看工单详情
const viewWorkOrder = (row) => {
  selectedWorkOrderId.value = row.id;
  showWorkOrderDrawer.value = true;
};

// 工单类型标签类型
const getWorkTypeTagType = (type) => {
  switch (type) {
    case 'ALERT_DEFECT': return 'warning';
    case 'ROUTINE_INSPECTION': return 'info';
    case 'EQUIPMENT_MAINTENANCE': return 'success';
    default: return 'info';
  }
};

// 工单类型名称
const getWorkTypeName = (type) => {
  switch (type) {
    case 'ALERT_DEFECT': return '告警消缺';
    case 'ROUTINE_INSPECTION': return '日常巡检';
    case 'EQUIPMENT_MAINTENANCE': return '设备维保';
    case 'ALERT_FIX': return '告警修复'; // Added based on your DB log
    case 'INSPECTION': return '例行巡检'; // Added based on your DB log
    default: return '未知';
  }
};

// 优先级标签类型
const getPriorityTagType = (priority) => {
  switch (priority) {
    case 'URGENT': return 'danger';
    case 'HIGH': return 'warning';
    case 'MEDIUM': return 'info';
    case 'LOW': return 'success';
    default: return 'info';
  }
};

// 优先级名称
const getPriorityName = (priority) => {
  switch (priority) {
    case 'URGENT': return '紧急';
    case 'HIGH': return '高';
    case 'MEDIUM': return '中';
    case 'LOW': return '低';
    default: return '普通';
  }
};

// 状态标签类型
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

// 状态名称
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

// 格式化日期
const formatDate = (timestamp) => {
  if (!timestamp) return '';
  const date = new Date(timestamp);
  return date.toLocaleDateString('zh-CN');
};

// 获取用户头像
const getUserAvatar = (userId) => {
  // 这里应该从用户服务获取真实头像
  return '';
};

// 统一处理工单状态变更操作
const updateWorkOrderStatus = async (row, action, targetStatus, title, message) => {
  try {
    await ElMessageBox.confirm(message, title, {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    });

    // 发送真实的后端请求
    const response = await workOrderApi.updateStatus(row.id, {
      status: targetStatus,
      remark: `通过工单列表快捷操作: ${title}`,
      operatorId: 1, // 当前登录用户ID，实际项目中从 authStore 获取
      operatorName: '当前用户' // 当前登录用户名称
    });

    if (response.data.success) {
      ElMessage.success(`${title}成功`);
      // 操作成功后，刷新表格数据和顶部的 KPI 统计数据
      fetchWorkOrders();
      fetchWorkOrderStats();
    } else {
      ElMessage.error(response.data.message || `${title}失败`);
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error(`工单${title}操作失败:`, error);
      ElMessage.error(`操作失败，请检查网络或联系管理员`);
    }
  }
};

// 按钮点击入口
const handleProcess = (row) => {
  // 默认点击主按钮时的行为
  if (row.status === 'PENDING') handleAccept(row);
  else if (row.status === 'PROCESSING') handleComplete(row);
  else if (row.status === 'VERIFYING') handleVerify(row);
  else if (row.status === 'COMPLETED') handleClose(row);
};

// 各个具体操作的封装
const handleAccept = (row) => {
  updateWorkOrderStatus(row, 'ACCEPTED', 'PROCESSING', '接受工单', '确认开始处理此工单吗？');
};

const handleComplete = (row) => {
  updateWorkOrderStatus(row, 'COMPLETED', 'VERIFYING', '完成工单', '确认已完成工单处理，提交验收吗？');
};

const handleVerify = (row) => {
  updateWorkOrderStatus(row, 'VERIFIED', 'COMPLETED', '验收通过', '确认此工单已处理合格并验收通过吗？');
};

const handleClose = (row) => {
  updateWorkOrderStatus(row, 'CLOSED', 'CLOSED', '关闭工单', '确认归档关闭此工单吗？关闭后将不可更改。');
};

// 工单创建成功回调
const onWorkOrderCreated = () => {
  showCreateModal.value = false;
  fetchWorkOrders();
};

onMounted(() => {
  applyRouteQuery();
  if (route.query.create === '1' || route.query.alertId) {
    showCreateModal.value = true;
  }
  if (route.params.id) {
    selectedWorkOrderId.value = Number(route.params.id);
    showWorkOrderDrawer.value = true;
  }
  fetchWorkOrderStats();
  fetchWorkOrders();
});

watch(() => route.query, () => {
  applyRouteQuery();
  pagination.currentPage = 1;
  fetchWorkOrderStats();
  fetchWorkOrders();
});

watch(() => route.params.id, (id) => {
  if (id) {
    selectedWorkOrderId.value = Number(id);
    showWorkOrderDrawer.value = true;
  }
});

watch(showWorkOrderDrawer, (visible) => {
  if (!visible && route.params.id) {
    router.replace({ path: '/work-orders', query: route.query });
  }
});
</script>

<style scoped>
.work-order-center {
  padding: 20px 24px 28px;
  background-color: var(--ccg-bg);
  min-height: 100%;
  color: var(--ccg-text);
}

.page-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 16px;
}

.page-head h1 {
  margin: 0;
  font-size: 22px;
  line-height: 1.2;
  font-weight: 750;
}

.page-head p {
  margin: 6px 0 0;
  color: var(--ccg-muted);
  font-size: 13px;
}

.head-actions {
  display: flex;
  gap: 10px;
  flex-shrink: 0;
}

.kpi-cards {
  margin-bottom: 16px;
}

.kpi-card {
  text-align: center;
  position: relative;
  min-height: 104px;
}

.kpi-card.overdue {
  border-left: 4px solid #f56c6c;
}

.kpi-card.pending {
  border-left: 4px solid #e6a23c;
}

.kpi-card.processing {
  border-left: 4px solid #409eff;
}

.kpi-card.completed {
  border-left: 4px solid #67c23a;
}

.kpi-content {
  text-align: left;
  display: inline-block;
}

.kpi-value {
  font-size: 24px;
  font-weight: bold;
  color: #303133;
  margin-bottom: 5px;
}

.kpi-label {
  font-size: 14px;
  color: #909399;
}

.icon {
  position: absolute;
  top: 20px;
  right: 20px;
  font-size: 36px;
  opacity: 0.3;
}

.filter-section {
  margin-bottom: 16px;
}

.work-order-table {
  background: #fff;
  border: 1px solid #e5e7eb;
  border-radius: 4px;
}

.work-order-table :deep(.el-table__cell) {
  padding: 9px 0;
}

.work-order-info .title {
  font-weight: bold;
  color: #303133;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.work-order-info .id {
  color: #909399;
  font-size: 12px;
}

.location-info {
  color: #606266;
  line-height: 1.6;
}

.location-info span,
.assignee-info span {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.assignee-info {
  display: flex;
  align-items: center;
  gap: 8px;
  min-width: 0;
}

.timeline-info {
  font-size: 12px;
  color: #909399;
}

.pagination {
  margin-top: 14px;
  display: flex;
  justify-content: flex-end;
  padding: 12px 16px;
  border: 1px solid var(--ccg-border);
  border-radius: 8px;
  background: #fff;
}

@media (max-width: 1080px) {
  .page-head {
    flex-direction: column;
  }

  .kpi-cards :deep(.el-col) {
    margin-bottom: 12px;
  }
}
</style>
