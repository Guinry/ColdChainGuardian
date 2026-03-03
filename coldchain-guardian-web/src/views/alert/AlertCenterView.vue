<template>
  <Layout>
    <div class="alert-center">
      <!-- 顶部告警雷达 -->
      <el-row :gutter="20" class="kpi-cards">
        <el-col :span="6">
          <el-card class="kpi-card unhandled">
            <div class="kpi-content">
              <div class="kpi-value">{{ alertStats.unhandledCount }}</div>
              <div class="kpi-label">未处理告警</div>
            </div>
            <i class="el-icon-warning icon"></i>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="kpi-card critical">
            <div class="kpi-content">
              <div class="kpi-value">{{ alertStats.criticalCount + alertStats.highCount }}</div>
              <div class="kpi-label">紧急/高危</div>
            </div>
            <i class="el-icon-error icon"></i>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="kpi-card handling">
            <div class="kpi-content">
              <div class="kpi-value">{{ alertStats.handlingCount }}</div>
              <div class="kpi-label">处理中</div>
            </div>
            <i class="el-icon-setting icon"></i>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="kpi-card resolved">
            <div class="kpi-content">
              <div class="kpi-value">{{ alertStats.resolvedCount }}</div>
              <div class="kpi-label">今日已消除</div>
            </div>
            <i class="el-icon-success icon"></i>
          </el-card>
        </el-col>
      </el-row>

      <!-- 筛选栏 -->
      <el-card class="filter-section">
        <el-form :model="filterForm" inline>
          <el-form-item label="全局检索">
            <el-input v-model="filterForm.keyword" placeholder="告警内容/设备名称" />
          </el-form-item>
          <el-form-item label="发生位置">
            <el-select v-model="filterForm.location" placeholder="请选择库区">
              <el-option label="全部" value=""></el-option>
              <el-option label="冷库A区" value="冷库A区"></el-option>
              <el-option label="冷库B区" value="冷库B区"></el-option>
              <el-option label="恒温库" value="恒温库"></el-option>
            </el-select>
          </el-form-item>
          <el-form-item label="告警级别">
            <el-select v-model="filterForm.level" placeholder="请选择">
              <el-option label="全部" value=""></el-option>
              <el-option label="紧急" value="CRITICAL"></el-option>
              <el-option label="高" value="HIGH"></el-option>
              <el-option label="中" value="MEDIUM"></el-option>
              <el-option label="低" value="LOW"></el-option>
            </el-select>
          </el-form-item>
          <el-form-item label="处理状态">
            <el-select v-model="filterForm.status" placeholder="请选择">
              <el-option label="全部" value=""></el-option>
              <el-option label="未处理" value="UNHANDLED"></el-option>
              <el-option label="处理中" value="HANDLING"></el-option>
              <el-option label="已解决" value="RESOLVED"></el-option>
              <el-option label="已忽略" value="IGNORED"></el-option>
            </el-select>
          </el-form-item>
          <el-form-item label="时间范围">
            <el-date-picker
              v-model="timeRange"
              type="datetimerange"
              range-separator="至"
              start-placeholder="开始时间"
              end-placeholder="结束时间"
              format="YYYY-MM-DD HH:mm"
              value-format="YYYY-MM-DDTHH:mm:ss"
            />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="fetchAlerts">查询</el-button>
            <el-button @click="resetFilter">重置</el-button>
            <el-button @click="showAnalysisPanel = !showAnalysisPanel">
              {{ showAnalysisPanel ? '隐藏' : '显示' }}分析面板
            </el-button>
          </el-form-item>
        </el-form>
      </el-card>

      <!-- 分析面板 -->
      <el-collapse-transition>
        <el-card class="analysis-panel" v-show="showAnalysisPanel">
          <el-tabs v-model="activeTab">
            <el-tab-pane label="趋势分析" name="trend">
              <div class="chart-container">
                <h4>告警趋势分析</h4>
                <div v-if="trendAnalysis.trendData" class="trend-chart">
                  <div v-for="(count, period) in trendAnalysis.trendData" :key="period" class="trend-item">
                    <span class="period">{{ period }}</span>
                    <el-progress :percentage="Math.min(100, count / maxTrendCount * 100)" :format="() => count" />
                  </div>
                </div>
                <div v-else class="no-data">暂无趋势数据</div>
              </div>
            </el-tab-pane>

            <el-tab-pane label="重复告警" name="recurring">
              <div class="recurring-analysis">
                <h4>高频告警分析</h4>
                <el-table :data="recurringAlerts" style="width: 100%" v-if="recurringAlerts.length > 0">
                  <el-table-column prop="deviceAndType" label="设备及告警类型" />
                  <el-table-column prop="count" label="次数" />
                </el-table>
                <div v-else class="no-data">暂无重复告警数据</div>
              </div>
            </el-tab-pane>

            <el-tab-pane label="设备健康" name="health">
              <div class="health-analysis">
                <h4>设备健康度评分</h4>
                <el-table :data="deviceHealthScores" style="width: 100%" v-if="deviceHealthScores.length > 0">
                  <el-table-column prop="deviceName" label="设备名称" />
                  <el-table-column prop="score" label="健康分" />
                  <el-table-column label="状态">
                    <template #default="{ row }">
                      <el-tag :type="getHealthStatus(row.score)">
                        {{ getHealthStatusLabel(row.score) }}
                      </el-tag>
                    </template>
                  </el-table-column>
                </el-table>
                <div v-else class="no-data">暂无设备健康数据</div>
              </div>
            </el-tab-pane>

            <el-tab-pane label="根因分析" name="rootCause">
              <div class="root-cause-analysis">
                <h4>告警根因分析</h4>
                <el-table :data="rootCauseAnalysis.correlatedAlerts" style="width: 100%" v-if="rootCauseAnalysis.correlatedAlerts">
                  <el-table-column prop="time" label="时间窗口" />
                  <el-table-column prop="alerts" label="相关告警类型">
                    <template #default="{ row }">
                      <div v-for="alert in row.alerts" :key="alert" class="alert-type-tag">
                        <el-tag size="small">{{ alert }}</el-tag>
                      </div>
                    </template>
                  </el-table-column>
                </el-table>
                <div v-else class="no-data">暂无根因分析数据</div>
              </div>
            </el-tab-pane>
          </el-tabs>
        </el-card>
      </el-collapse-transition>

      <!-- 告警列表 -->
      <el-table
        :data="alerts"
        v-loading="loading"
        stripe
        height="calc(100vh - 550px)"
        style="width: 100%"
        @row-click="showTriageDrawer"
      >
        <el-table-column prop="severityLevel" label="级别" width="80">
          <template #default="{ row }">
            <el-tag
              :type="getSeverityTagType(row.severityLevel)"
              size="small"
            >
              {{ getSeverityLabel(row.severityLevel) }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column prop="description" label="告警内容" min-width="250">
          <template #default="{ row }">
            <div class="alert-content">
              <div class="alert-type">{{ row.alertType }}</div>
              <div class="alert-message">{{ row.description }}</div>
            </div>
          </template>
        </el-table-column>

        <el-table-column prop="deviceName" label="发生位置" width="200">
          <template #default="{ row }">
            <div class="location-info">
              <span>{{ row.deviceName || '未知设备' }}</span>
            </div>
          </template>
        </el-table-column>

        <el-table-column prop="createdAt" label="发生时间" width="150">
          <template #default="{ row }">
            {{ formatDate(row.createdAt) }}
          </template>
        </el-table-column>

        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag
              :type="getStatusTagType(row.status)"
              size="small"
            >
              {{ getStatusName(row.status) }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column label="操作" fixed="right" width="150">
          <template #default="{ row }">
            <el-button size="small" @click.stop="showTriageDrawer(row)">研判</el-button>
            <el-dropdown split-button size="small" @click.stop="handleQuickResolve(row)">
              快速处理
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item @click.stop="convertToWorkOrder(row)">转工单</el-dropdown-item>
                  <el-dropdown-item @click.stop="ignoreAlert(row)">标记忽略</el-dropdown-item>
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

      <!-- 告警研判抽屉 -->
      <AlertTriageDrawer
        :alert-id="selectedAlertId"
        :visible="showTriageDrawerFlag"
        @close="showTriageDrawerFlag = false"
        @updated="fetchAlerts"
      />
    </div>
  </Layout>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import AlertTriageDrawer from '@/components/alert/AlertTriageDrawer.vue';
import Layout from '@/components/Layout.vue';
import { alertApi } from '@/api/alert';

// 告警统计数据
const alertStats = ref({
  unhandledCount: 0,
  criticalCount: 0,
  highCount: 0,
  handlingCount: 0,
  resolvedCount: 0
});

// 分析数据
const trendAnalysis = ref({});
const recurringAlerts = ref([]);
const deviceHealthScores = ref([]);
const rootCauseAnalysis = ref({});

// 告警数据
const alerts = ref([]);
const loading = ref(false);

// 筛选表单
const filterForm = reactive({
  keyword: '',
  location: undefined,  // 保持为undefined而不是空字符串
  level: undefined,
  status: undefined
});

// 时间范围
const timeRange = ref([]);

// 分页
const pagination = reactive({
  currentPage: 1,
  pageSize: 10,
  total: 0
});

// 分析面板
const showAnalysisPanel = ref(false);
const activeTab = ref('trend');

// 抽屉状态
const showTriageDrawerFlag = ref(false);
const selectedAlertId = ref(null);

// 获取告警统计数据
const fetchAlertStats = async () => {
  try {
    const params = {};
    if (timeRange.value && timeRange.value.length === 2) {
      params.startTime = timeRange.value[0];
      params.endTime = timeRange.value[1];
    }

    const response = await alertApi.getStats(params);
    // 检查响应结构并设置默认值
    if (response && response.data) {
      const data = response.data.data || {};
      alertStats.value = {
        unhandledCount: data.unhandledCount || 0,
        criticalCount: data.criticalCount || 0,
        highCount: data.highCount || 0,
        handlingCount: data.handlingCount || 0,
        resolvedCount: data.resolvedCount || 0
      };
    } else {
      // 如果响应结构不符合预期，使用默认值
      alertStats.value = {
        unhandledCount: 0,
        criticalCount: 0,
        highCount: 0,
        handlingCount: 0,
        resolvedCount: 0
      };
    }
  } catch (error) {
    console.error('获取告警统计数据失败:', error);
    // 即使API调用失败，也要确保有默认值防止页面空白
    alertStats.value = {
      unhandledCount: 0,
      criticalCount: 0,
      highCount: 0,
      handlingCount: 0,
      resolvedCount: 0
    };
  }
};

// 获取趋势分析
const fetchTrendAnalysis = async () => {
  try {
    const response = await alertApi.getAlertTrendAnalysis({ period: 'daily' });
    if (response && response.data) {
      trendAnalysis.value = response.data.data || {};
    } else {
      trendAnalysis.value = {};
    }
  } catch (error) {
    console.error('获取趋势分析失败:', error);
    trendAnalysis.value = {};
  }
};

// 获取重复告警分析
const fetchRecurringAnalysis = async () => {
  try {
    const response = await alertApi.getRecurringAlertAnalysis();
    if (response && response.data) {
      const data = response.data.data || {};

      // 格式化重复告警数据
      recurringAlerts.value = Object.entries(data.recurringAlerts || {}).map(([key, count]) => ({
        deviceAndType: key,
        count: count
      }));
    } else {
      recurringAlerts.value = [];
    }
  } catch (error) {
    console.error('获取重复告警分析失败:', error);
    recurringAlerts.value = [];
  }
};

// 获取设备健康评分
const fetchDeviceHealthScores = async () => {
  try {
    const response = await alertApi.getDeviceHealthScore();
    if (response && response.data) {
      const data = response.data.data || {};

      // 格式化设备健康评分数据
      deviceHealthScores.value = Object.entries(data.healthScores || {}).map(([deviceName, score]) => ({
        deviceName: deviceName,
        score: Math.round(score * 100) / 100 // 保留两位小数
      }));
    } else {
      deviceHealthScores.value = [];
    }
  } catch (error) {
    console.error('获取设备健康评分失败:', error);
    deviceHealthScores.value = [];
  }
};

// 获取根因分析
const fetchRootCauseAnalysis = async () => {
  try {
    const response = await alertApi.getRootCauseAnalysis();
    if (response && response.data) {
      const data = response.data.data || {};

      // 格式化根因分析数据
      const correlatedAlerts = Object.entries(data.correlatedAlerts || {}).map(([time, alerts]) => ({
        time: time,
        alerts: alerts
      }));

      rootCauseAnalysis.value = {
        correlatedAlerts: correlatedAlerts,
        clusterCount: data.clusterCount || 0,
        totalAlerts: data.totalAlerts || 0
      };
    } else {
      rootCauseAnalysis.value = {
        correlatedAlerts: [],
        clusterCount: 0,
        totalAlerts: 0
      };
    }
  } catch (error) {
    console.error('获取根因分析失败:', error);
    rootCauseAnalysis.value = {
      correlatedAlerts: [],
      clusterCount: 0,
      totalAlerts: 0
    };
  }
};

// 获取告警列表
const fetchAlerts = async () => {
  loading.value = true;

  try {
    const params = {
      keyword: filterForm.keyword || undefined,
      location: filterForm.location || undefined,
      level: filterForm.level || undefined,
      status: filterForm.status || undefined,
      page: pagination.currentPage,
      size: pagination.pageSize
    };

    // 添加时间范围参数
    if (timeRange.value && timeRange.value.length === 2) {
      params.startTime = timeRange.value[0];
      params.endTime = timeRange.value[1];
    }

    const response = await alertApi.search(params);
    if (response && response.data) {
      // 数据适配器：处理后端返回的不同格式
      const rawAlerts = response.data.data?.records || [];
      alerts.value = rawAlerts.map(adaptAlertData);
      pagination.total = response.data.data?.total || 0;
    } else {
      alerts.value = [];
      pagination.total = 0;
    }
  } catch (error) {
    console.error('获取告警列表失败:', error);
    ElMessage.error('获取告警列表失败');
    alerts.value = [];
    pagination.total = 0;
  } finally {
    loading.value = false;
  }
};

// 数据适配器：确保返回的数据结构一致
const adaptAlertData = (rawAlert) => {
  // 确保status字段存在
  if (!rawAlert.status) {
    // 如果原始数据有isResolved字段，则根据它来推断状态
    if (rawAlert.isResolved !== undefined) {
      rawAlert.status = rawAlert.isResolved ? 'RESOLVED' : 'UNHANDLED';
    } else {
      // 默认为未处理
      rawAlert.status = 'UNHANDLED';
    }
  }

  // 确保severityLevel字段存在
  if (rawAlert.severityLevel === undefined && rawAlert.alertLevel) {
    // 如果只有alertLevel字符串，将其转换为severityLevel数字
    switch (rawAlert.alertLevel.toUpperCase()) {
      case 'CRITICAL':
        rawAlert.severityLevel = 4;
        break;
      case 'HIGH':
        rawAlert.severityLevel = 3;
        break;
      case 'MEDIUM':
        rawAlert.severityLevel = 2;
        break;
      case 'LOW':
        rawAlert.severityLevel = 1;
        break;
      default:
        rawAlert.severityLevel = 2; // 默认为中等
    }
  }

  // 确保alertType字段存在
  if (!rawAlert.alertType && rawAlert.description) {
    // 如果没有alertType，尝试从description提取
    rawAlert.alertType = '未知类型';
  }

  return rawAlert;
};

// 获取分析数据
const fetchAnalysisData = async () => {
  await Promise.all([
    fetchTrendAnalysis(),
    fetchRecurringAnalysis(),
    fetchDeviceHealthScores(),
    fetchRootCauseAnalysis()
  ]);
};

// 重置筛选条件
const resetFilter = () => {
  Object.keys(filterForm).forEach(key => {
    filterForm[key] = '';
  });
  timeRange.value = [];
  pagination.currentPage = 1;
  fetchAlerts();
};

// 页码变化
const handleCurrentChange = (val) => {
  pagination.currentPage = val;
  fetchAlerts();
};

// 页大小变化
const handleSizeChange = (val) => {
  pagination.pageSize = val;
  pagination.currentPage = 1;
  fetchAlerts();
};

// 显示研判抽屉
const showTriageDrawer = (row) => {
  selectedAlertId.value = row.id;
  showTriageDrawerFlag.value = true;
};

// 严重程度标签类型
const getSeverityTagType = (level) => {
  // 如果是数字（severityLevel格式），转换为字符串（alertLevel格式）
  let levelStr;
  if (typeof level === 'number') {
    switch (level) {
      case 4: levelStr = 'CRITICAL'; break;
      case 3: levelStr = 'HIGH'; break;
      case 2: levelStr = 'MEDIUM'; break;
      case 1: levelStr = 'LOW'; break;
      default: levelStr = 'MEDIUM'; break;
    }
  } else {
    levelStr = level;
  }

  switch (levelStr) {
    case 'CRITICAL': return 'danger';
    case 'HIGH': return 'warning';
    case 'MEDIUM': return 'info';
    case 'LOW': return 'success';
    default: return 'info';
  }
};

// 严重程度标签文本
const getSeverityLabel = (level) => {
  // 如果是数字（severityLevel格式），转换为字符串（alertLevel格式）
  let levelStr;
  if (typeof level === 'number') {
    switch (level) {
      case 4: levelStr = 'CRITICAL'; break;
      case 3: levelStr = 'HIGH'; break;
      case 2: levelStr = 'MEDIUM'; break;
      case 1: levelStr = 'LOW'; break;
      default: levelStr = 'MEDIUM'; break;
    }
  } else {
    levelStr = level;
  }

  switch (levelStr) {
    case 'CRITICAL': return '紧急';
    case 'HIGH': return '高';
    case 'MEDIUM': return '中';
    case 'LOW': return '低';
    default: return '未知';
  }
};

// 状态标签类型
const getStatusTagType = (status) => {
  switch (status) {
    case 'UNHANDLED': return 'info';
    case 'HANDLING': return 'warning';
    case 'RESOLVED': return 'success';
    case 'IGNORED': return 'info';
    default: return 'info';
  }
};

// 状态名称
const getStatusName = (status) => {
  switch (status) {
    case 'UNHANDLED': return '未处理';
    case 'HANDLING': return '处理中';
    case 'RESOLVED': return '已解决';
    case 'IGNORED': return '已忽略';
    default: return '未知';
  }
};

// 状态标签类型（根据isResolved字段）
const getStatusTagTypeFromResolved = (isResolved, severityLevel) => {
  if (isResolved) {
    return 'success'; // 已解决
  } else {
    // 未解决状态下，根据严重级别显示不同颜色
    if (typeof severityLevel === 'number') {
      switch (severityLevel) {
        case 4: return 'danger';  // CRITICAL
        case 3: return 'warning'; // HIGH
        case 2: return 'info';    // MEDIUM
        case 1: return 'info';    // LOW
        default: return 'info';
      }
    } else {
      // 如果是字符串格式
      switch (severityLevel) {
        case 'CRITICAL': return 'danger';
        case 'HIGH': return 'warning';
        case 'MEDIUM': return 'info';
        case 'LOW': return 'info';
        default: return 'info';
      }
    }
  }
};

// 状态名称（根据isResolved字段）
const getStatusNameFromResolved = (isResolved, severityLevel) => {
  if (isResolved) {
    return '已解决';
  } else {
    // 对于未解决的告警，我们可以添加前缀表明其状态
    let levelLabel = '未处理';
    if (typeof severityLevel === 'number') {
      switch (severityLevel) {
        case 4: levelLabel = '紧急'; break;
        case 3: levelLabel = '高'; break;
        case 2: levelLabel = '中'; break;
        case 1: levelLabel = '低'; break;
        default: levelLabel = '未处理'; break;
      }
    } else {
      // 如果是字符串格式
      switch (severityLevel) {
        case 'CRITICAL': levelLabel = '紧急'; break;
        case 'HIGH': levelLabel = '高'; break;
        case 'MEDIUM': levelLabel = '中'; break;
        case 'LOW': levelLabel = '低'; break;
        default: levelLabel = '未处理'; break;
      }
    }
    return `${levelLabel}待处理`;
  }
};

// 获取健康状态标签类型
const getHealthStatus = (score) => {
  if (score >= 80) return 'success';
  if (score >= 60) return 'warning';
  return 'danger';
};

// 获取健康状态标签文本
const getHealthStatusLabel = (score) => {
  if (score >= 80) return '健康';
  if (score >= 60) return '一般';
  return '不健康';
};

// 计算最大趋势数
const maxTrendCount = computed(() => {
  const values = Object.values(trendAnalysis.value.trendData || {});
  return values.length > 0 ? Math.max(...values) : 1;
});

// 格式化日期
const formatDate = (timestamp) => {
  if (!timestamp) return '';
  // 如果是数字，它是毫秒时间戳；如果是字符串，直接解析
  const date = typeof timestamp === 'number' ? new Date(timestamp) : new Date(timestamp);
  return date.toLocaleString('zh-CN');
};

// 快速处理告警（标记为已解决）
const handleQuickResolve = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定要快速解决此告警吗？`,
      '快速解决',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'success'
      }
    );

    const response = await alertApi.updateStatus(row.id, {
      status: 'RESOLVED',
      handleRemark: '快速解决'
    });

    if (response.data.success) {
      ElMessage.success('告警已解决');
      fetchAlerts();
      fetchAlertStats(); // 刷新统计数据
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('快速解决告警失败:', error);
      ElMessage.error('快速解决告警失败');
    }
  }
};

// 将告警转为工单
const convertToWorkOrder = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定要将此告警转为工单吗？`,
      '转为工单',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'primary'
      }
    );

    const response = await alertApi.convertToWorkOrder(row.id, {
      assigneeId: 1, // 默认负责人ID
      assigneeName: '系统管理员',
      description: `由告警 "${row.description}" 转换而来的工单`
    });

    if (response.data.success) {
      ElMessage.success('告警已转为工单');
      fetchAlerts();
      fetchAlertStats(); // 刷新统计数据
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('转换为工单失败:', error);
      ElMessage.error('转换为工单失败');
    }
  }
};

// 忽略告警
const ignoreAlert = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定要忽略此告警吗？`,
      '忽略告警',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'info'
      }
    );

    const response = await alertApi.updateStatus(row.id, {
      status: 'IGNORED',
      handleRemark: '被标记为误报'
    });

    if (response.data.success) {
      ElMessage.success('告警已忽略');
      fetchAlerts();
      fetchAlertStats(); // 刷新统计数据
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('忽略告警失败:', error);
      ElMessage.error('忽略告警失败');
    }
  }
};

onMounted(async () => {
  // 并行执行API调用以提高性能
  await Promise.all([
    fetchAlertStats().catch(err => console.error('获取告警统计数据错误:', err)),
    fetchAlerts().catch(err => console.error('获取告警列表错误:', err)),
    fetchAnalysisData().catch(err => console.error('获取分析数据错误:', err))
  ]);
});
</script>

<style scoped>
.alert-center {
  padding: 20px;
  background-color: #f5f7fa;
  min-height: 100vh;
}

.kpi-cards {
  margin-bottom: 20px;
}

.kpi-card {
  text-align: center;
  position: relative;
}

.kpi-card.unhandled {
  border-left: 4px solid #f56c6c;
}

.kpi-card.critical {
  border-left: 4px solid #e74c3c;
}

.kpi-card.handling {
  border-left: 4px solid #f39c12;
}

.kpi-card.resolved {
  border-left: 4px solid #2ecc71;
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
  margin-bottom: 20px;
}

.analysis-panel {
  margin-bottom: 20px;
}

.chart-container, .recurring-analysis, .health-analysis, .root-cause-analysis {
  padding: 20px 0;
}

.trend-chart {
  max-height: 300px;
  overflow-y: auto;
}

.trend-item {
  margin-bottom: 10px;
}

.period {
  display: inline-block;
  width: 120px;
  font-size: 12px;
  color: #606266;
}

.no-data {
  text-align: center;
  color: #909399;
  padding: 40px 0;
}

.alert-type-tag {
  margin-bottom: 4px;
}

.alert-content .alert-type {
  font-weight: bold;
  color: #303133;
}

.alert-content .alert-message {
  color: #909399;
  font-size: 13px;
  margin-top: 4px;
}

.location-info {
  color: #606266;
}

.pagination {
  margin-top: 20px;
  text-align: right;
}
</style>