<template>
  <el-dialog
    v-model="dialogVisible"
    title="新建工单"
    width="600px"
    @close="handleClose"
  >
    <el-form
      :model="form"
      :rules="rules"
      ref="formRef"
      label-width="100px"
    >
      <el-form-item label="工单标题" prop="title">
        <el-input
          v-model="form.title"
          placeholder="请输入工单标题"
          maxlength="100"
        />
      </el-form-item>

      <el-form-item label="工单类型" prop="workType">
        <el-select v-model="form.workType" placeholder="请选择工单类型" style="width: 100%">
          <el-option label="告警消缺" value="ALERT_DEFECT" />
          <el-option label="日常巡检" value="ROUTINE_INSPECTION" />
          <el-option label="设备维保" value="EQUIPMENT_MAINTENANCE" />
        </el-select>
      </el-form-item>

      <el-form-item label="优先级" prop="priority">
        <el-radio-group v-model="form.priority">
          <el-radio label="LOW">低</el-radio>
          <el-radio label="MEDIUM">中</el-radio>
          <el-radio label="HIGH">高</el-radio>
          <el-radio label="URGENT">紧急</el-radio>
        </el-radio-group>
      </el-form-item>

      <el-form-item label="截止时间" prop="dueDate">
        <el-date-picker
          v-model="form.dueDate"
          type="datetime"
          placeholder="选择截止时间"
          format="YYYY-MM-DD HH:mm"
          value-format="YYYY-MM-DDTHH:mm:ss"
          style="width: 100%"
        />
      </el-form-item>

      <el-form-item label="库区" prop="warehouseId">
        <el-select v-model="form.warehouseId" placeholder="请选择库区" style="width: 100%">
          <el-option label="冷库A区" value="1" />
          <el-option label="冷库B区" value="2" />
          <el-option label="恒温库" value="3" />
        </el-select>
      </el-form-item>

      <el-form-item label="设备" prop="deviceId">
        <el-select v-model="form.deviceId" placeholder="请选择设备" style="width: 100%">
          <el-option label="温度传感器001" value="1" />
          <el-option label="湿度传感器002" value="2" />
          <el-option label="温湿度一体机003" value="3" />
        </el-select>
      </el-form-item>

      <el-form-item label="责任人" prop="assigneeId">
        <el-select v-model="form.assigneeId" placeholder="请选择责任人" style="width: 100%">
          <el-option label="张三" value="1" />
          <el-option label="李四" value="2" />
          <el-option label="王五" value="3" />
        </el-select>
      </el-form-item>

      <el-form-item label="工单描述" prop="description">
        <el-input
          v-model="form.description"
          type="textarea"
          :rows="4"
          placeholder="请输入工单详细描述..."
          maxlength="500"
          show-word-limit
        />
      </el-form-item>

      <el-form-item label="关联告警">
        <el-select v-model="form.alertId" placeholder="可选择关联的告警" style="width: 100%" clearable>
          <el-option label="告警001: 温度过高" value="1" />
          <el-option label="告警002: 湿度过低" value="2" />
        </el-select>
      </el-form-item>
    </el-form>

    <template #footer>
      <span class="dialog-footer">
        <el-button @click="handleClose">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitting">提交</el-button>
      </span>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, computed, watch } from 'vue';
import { ElMessage } from 'element-plus';
import { workOrderApi } from '@/api/work-order.js';

// Props
const props = defineProps({
  visible: {
    type: Boolean,
    default: false
  }
});

// Emits
const emit = defineEmits(['close', 'submit']);

// Form data
const form = ref({
  title: '',
  workType: 'ALERT_DEFECT',
  priority: 'MEDIUM',
  dueDate: null,
  warehouseId: '',
  deviceId: '',
  assigneeId: '',
  description: '',
  alertId: null
});

const formRef = ref();
const submitting = ref(false);

// Validation rules
const rules = {
  title: [
    { required: true, message: '请输入工单标题', trigger: 'blur' },
    { min: 2, max: 100, message: '标题长度应在2-100个字符之间', trigger: 'blur' }
  ],
  workType: [
    { required: true, message: '请选择工单类型', trigger: 'change' }
  ],
  priority: [
    { required: true, message: '请选择优先级', trigger: 'change' }
  ],
  assigneeId: [
    { required: true, message: '请选择责任人', trigger: 'change' }
  ],
  description: [
    { required: true, message: '请输入工单描述', trigger: 'blur' },
    { min: 10, message: '描述至少10个字符', trigger: 'blur' }
  ]
};

// Computed properties
const dialogVisible = computed({
  get() {
    return props.visible;
  },
  set(value) {
    if (!value) {
      emit('close');
    }
  }
});

// Handle close
const handleClose = () => {
  emit('close');
  // Reset form
  setTimeout(() => {
    form.value = {
      title: '',
      workType: 'ALERT_DEFECT',
      priority: 'MEDIUM',
      dueDate: null,
      warehouseId: '',
      deviceId: '',
      assigneeId: '',
      description: '',
      alertId: null
    };
    if (formRef.value) {
      formRef.value.clearValidate();
    }
  }, 300);
};

// Handle submit
const handleSubmit = async () => {
  if (!formRef.value) return;

  try {
    await formRef.value.validate();

    submitting.value = true;

    const requestData = {
      ...form.value,
      reporterId: 1 // 默认报告人为当前用户
    };

    const response = await workOrderApi.create(requestData);

    if (response.data.success) {
      ElMessage.success('工单创建成功');
      emit('submit');
      handleClose();
    } else {
      ElMessage.error(response.data.message || '工单创建失败');
    }
  } catch (error) {
    console.error('创建工单失败:', error);
    if (error.response?.data?.message) {
      ElMessage.error(error.response.data.message);
    } else {
      ElMessage.error('创建工单失败，请稍后重试');
    }
  } finally {
    submitting.value = false;
  }
};
</script>

<style scoped>
.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}
</style>