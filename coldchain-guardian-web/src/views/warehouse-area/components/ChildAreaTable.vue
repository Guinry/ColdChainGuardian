<template>
  <div class="child-area-table">
    <div class="table-header">
      <el-button type="primary" @click="$emit('add')">新增子库区</el-button>

      <div class="table-actions">
        <el-button @click="batchEnable">批量启用</el-button>
        <el-button @click="batchDisable">批量禁用</el-button>
        <el-button @click="batchEnableAlarm">批量开启告警</el-button>
        <el-button @click="batchDisableAlarm">批量关闭告警</el-button>
        <el-dropdown @command="batchSetThreshold">
          <el-button>
            批量设置阈值 <el-icon><ArrowDown /></el-icon>
          </el-button>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="inherit">继承父级</el-dropdown-item>
              <el-dropdown-item command="custom">自定义</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </div>

    <el-table
      ref="tableRef"
      :data="tableData"
      row-key="id"
      @selection-change="handleSelectionChange"
      @row-dblclick="handleRowDblclick"
      height="500"
      border
      stripe
    >
      <el-table-column type="selection" width="55" />
      <el-table-column prop="areaName" label="名称" width="150">
        <template #default="{ row }">
          <el-button type="text" @click="selectNode(row)">{{ row.areaName }}</el-button>
        </template>
      </el-table-column>
      <el-table-column prop="areaCode" label="编码" width="120" />
      <el-table-column prop="areaLevel" label="层级" width="100">
        <template #default="{ row }">
          <el-tag size="small">{{ row.areaLevel }}</el-tag>
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
      <el-table-column prop="deviceCount" label="设备数" width="100">
        <template #default="{ row }">
          <el-link type="primary" @click="viewDevices(row)">{{ row.deviceCount || 0 }}</el-link>
        </template>
      </el-table-column>
      <el-table-column label="阈值策略" width="120">
        <template #default="{ row }">
          <el-tag size="small" :type="row.thresholdSource === 'inherit' ? 'info' : 'primary'">
            {{ row.thresholdSource === 'inherit' ? '继承' : '覆盖' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="sortNo" label="排序" width="80" sortable />
      <el-table-column prop="updateTime" label="更新时间" width="150" />
      <el-table-column label="操作" fixed="right" width="220">
        <template #default="{ row }">
          <el-button-group>
            <el-button size="small" @click="editRow(row)">编辑</el-button>
            <el-button size="small" :type="row.status === 1 ? 'danger' : 'success'" @click="toggleStatus(row)">
              {{ row.status === 1 ? '禁用' : '启用' }}
            </el-button>
            <el-button size="small" type="primary" @click="moveRow(row)">移动</el-button>
            <el-popconfirm
              title="确定要删除这个库区吗？"
              @confirm="deleteRow(row)"
            >
              <template #reference>
                <el-button size="small" type="danger">删除</el-button>
              </template>
            </el-popconfirm>
          </el-button-group>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页 -->
    <el-pagination
      v-model:current-page="pagination.currentPage"
      v-model:page-size="pagination.pageSize"
      :page-sizes="[10, 20, 50, 100]"
      :total="pagination.total"
      layout="total, sizes, prev, pager, next, jumper"
      @size-change="handleSizeChange"
      @current-change="handleCurrentChange"
      style="margin-top: 20px; text-align: right;"
    />

    <!-- 阈值编辑对话框 -->
    <el-dialog
      v-model="thresholdDialogVisible"
      title="批量设置阈值"
      width="600px"
    >
      <el-form :model="thresholdForm" label-width="120px">
        <el-form-item label="温度阈值">
          <el-input-number v-model="thresholdForm.tempMin" :min="-50" :max="50" :precision="2" />
          ~
          <el-input-number v-model="thresholdForm.tempMax" :min="-50" :max="50" :precision="2" />
        </el-form-item>
        <el-form-item label="湿度阈值">
          <el-input-number v-model="thresholdForm.humMin" :min="0" :max="100" :precision="2" />
          ~
          <el-input-number v-model="thresholdForm.humMax" :min="0" :max="100" :precision="2" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="thresholdDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="confirmThresholdSet">确定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { ArrowDown } from '@element-plus/icons-vue';
import { warehouseAreaApi } from '@/utils/api';

// Props
const props = defineProps({
  parentId: {
    type: Number,
    required: true
  }
});

// Emits
const emit = defineEmits(['refresh', 'selectNode']);

// 响应式数据
const tableData = ref([]);
const multipleSelection = ref([]);
const tableRef = ref();

// 分页数据
const pagination = reactive({
  currentPage: 1,
  pageSize: 20,
  total: 0
});

// 阈值对话框相关
const thresholdDialogVisible = ref(false);
const thresholdForm = reactive({
  tempMin: -20.00,
  tempMax: 8.00,
  humMin: 30.00,
  humMax: 70.00
});

// 当前行操作的阈值设置
const currentThresholdOperation = ref('');

// 加载数据
const loadData = async () => {
  try {
    // 这里应该根据父ID获取子库区列表
    // 模拟数据
    tableData.value = [
      {
        id: 101,
        areaName: '一楼东区',
        areaCode: 'F1_EAST',
        areaLevel: 'AREA',
        status: 1,
        alarmEnabled: 1,
        deviceCount: 5,
        thresholdSource: 'override',
        sortNo: 1,
        updateTime: '2024-01-15 10:30:00'
      },
      {
        id: 102,
        areaName: '一楼西区',
        areaCode: 'F1_WEST',
        areaLevel: 'AREA',
        status: 1,
        alarmEnabled: 0,
        deviceCount: 3,
        thresholdSource: 'inherit',
        sortNo: 2,
        updateTime: '2024-01-14 15:20:00'
      },
      {
        id: 103,
        areaName: '冷库A区',
        areaCode: 'COLD_A',
        areaLevel: 'BIN',
        status: 0,
        alarmEnabled: 1,
        deviceCount: 8,
        thresholdSource: 'override',
        sortNo: 3,
        updateTime: '2024-01-13 09:15:00'
      }
    ];

    // 设置总数（实际项目中从接口获取）
    pagination.total = tableData.value.length;
  } catch (error) {
    console.error('加载数据失败', error);
    ElMessage.error('加载子库区列表失败');
  }
};

// 选择行变化
const handleSelectionChange = (val) => {
  multipleSelection.value = val;
};

// 双击行
const handleRowDblclick = (row) => {
  selectNode(row);
};

// 选择节点（在树中定位）
const selectNode = (row) => {
  emit('selectNode', row);
};

// 查看设备
const viewDevices = (row) => {
  console.log('查看设备', row);
  // 实际项目中跳转到设备列表页面
};

// 编辑行
const editRow = (row) => {
  console.log('编辑行', row);
  // 这里应该打开编辑抽屉
};

// 切换状态
const toggleStatus = async (row) => {
  try {
    const newStatus = row.status === 1 ? 0 : 1;
    await warehouseAreaApi.update(row.id, { ...row, status: newStatus });
    row.status = newStatus;
    ElMessage.success(`已${newStatus === 1 ? '启用' : '禁用'}`);
    emit('refresh');
  } catch (error) {
    ElMessage.error('操作失败');
  }
};

// 移动行
const moveRow = (row) => {
  console.log('移动行', row);
  // 这里应该打开移动对话框
};

// 删除行
const deleteRow = async (row) => {
  try {
    await warehouseAreaApi.delete(row.id);
    ElMessage.success('删除成功');
    emit('refresh'); // 刷新数据
  } catch (error) {
    ElMessage.error('删除失败');
  }
};

// 批量启用
const batchEnable = async () => {
  if (multipleSelection.value.length === 0) {
    ElMessage.warning('请先选择要操作的库区');
    return;
  }

  try {
    const ids = multipleSelection.value.map(item => item.id);
    // 实际项目中应调用批量更新API
    await Promise.all(
      multipleSelection.value.map(item =>
        warehouseAreaApi.update(item.id, { ...item, status: 1 })
      )
    );

    ElMessage.success('批量启用成功');
    emit('refresh');
  } catch (error) {
    ElMessage.error('批量启用失败');
  }
};

// 批量禁用
const batchDisable = async () => {
  if (multipleSelection.value.length === 0) {
    ElMessage.warning('请先选择要操作的库区');
    return;
  }

  try {
    const ids = multipleSelection.value.map(item => item.id);
    await Promise.all(
      multipleSelection.value.map(item =>
        warehouseAreaApi.update(item.id, { ...item, status: 0 })
      )
    );

    ElMessage.success('批量禁用成功');
    emit('refresh');
  } catch (error) {
    ElMessage.error('批量禁用失败');
  }
};

// 批量开启告警
const batchEnableAlarm = async () => {
  if (multipleSelection.value.length === 0) {
    ElMessage.warning('请先选择要操作的库区');
    return;
  }

  try {
    await Promise.all(
      multipleSelection.value.map(item =>
        warehouseAreaApi.update(item.id, { ...item, alarmEnabled: 1 })
      )
    );

    ElMessage.success('批量开启告警成功');
    emit('refresh');
  } catch (error) {
    ElMessage.error('批量开启告警失败');
  }
};

// 批量关闭告警
const batchDisableAlarm = async () => {
  if (multipleSelection.value.length === 0) {
    ElMessage.warning('请先选择要操作的库区');
    return;
  }

  try {
    await Promise.all(
      multipleSelection.value.map(item =>
        warehouseAreaApi.update(item.id, { ...item, alarmEnabled: 0 })
      )
    );

    ElMessage.success('批量关闭告警成功');
    emit('refresh');
  } catch (error) {
    ElMessage.error('批量关闭告警失败');
  }
};

// 批量设置阈值
const batchSetThreshold = (command) => {
  if (multipleSelection.value.length === 0) {
    ElMessage.warning('请先选择要操作的库区');
    return;
  }

  if (command === 'custom') {
    currentThresholdOperation.value = 'custom';
    thresholdDialogVisible.value = true;
  } else {
    // 继承父级阈值
    handleBatchSetThreshold({ inherit: true });
  }
};

// 确认阈值设置
const confirmThresholdSet = () => {
  if (currentThresholdOperation.value === 'custom') {
    handleBatchSetThreshold({
      inherit: false,
      temperatureThresholdMin: thresholdForm.tempMin,
      temperatureThresholdMax: thresholdForm.tempMax,
      humidityThresholdMin: thresholdForm.humMin,
      humidityThresholdMax: thresholdForm.humMax
    });
  }
  thresholdDialogVisible.value = false;
};

// 处理批量设置阈值
const handleBatchSetThreshold = async (settings) => {
  try {
    await Promise.all(
      multipleSelection.value.map(item => {
        const updateData = { ...item };
        if (settings.inherit) {
          // 这里需要获取父级的阈值
          updateData.thresholdSource = 'inherit';
        } else {
          updateData.thresholdSource = 'override';
          updateData.temperatureThresholdMin = settings.temperatureThresholdMin;
          updateData.temperatureThresholdMax = settings.temperatureThresholdMax;
          updateData.humidityThresholdMin = settings.humidityThresholdMin;
          updateData.humidityThresholdMax = settings.humidityThresholdMax;
        }
        return warehouseAreaApi.update(item.id, updateData);
      })
    );

    ElMessage.success('批量设置阈值成功');
    emit('refresh');
  } catch (error) {
    ElMessage.error('批量设置阈值失败');
  }
};

// 分页相关
const handleSizeChange = (size) => {
  pagination.pageSize = size;
  loadData();
};

const handleCurrentChange = (page) => {
  pagination.currentPage = page;
  loadData();
};

// 页面加载
onMounted(() => {
  loadData();
});
</script>

<style scoped>
.child-area-table {
  padding: 20px 0;
}

.table-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 20px;
}

.table-actions .el-button {
  margin-left: 10px;
}

.dialog-footer {
  text-align: right;
}
</style>