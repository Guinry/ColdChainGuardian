<template>
  <el-dialog
    v-model="dialogVisible"
    title="移动库区"
    width="50%"
    :before-close="handleClose"
  >
    <el-form :model="form" label-width="100px">
      <el-form-item label="当前库区">
        <span>{{ area?.areaName }} ({{ area?.areaCode }})</span>
      </el-form-item>

      <el-form-item label="目标位置">
        <el-cascader
          v-model="form.targetParentId"
          :options="treeOptions"
          :props="cascaderProps"
          placeholder="请选择目标父库区"
          style="width: 100%;"
        />
      </el-form-item>

      <el-alert
        title="注意事项"
        type="warning"
        description="移动库区将影响其所有子库区和关联设备，请谨慎操作。"
        show-icon
        :closable="false"
      />
    </el-form>

    <template #footer>
      <span class="dialog-footer">
        <el-button @click="handleClose">取消</el-button>
        <el-button type="primary" @click="handleConfirm">确定</el-button>
      </span>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, reactive, watch } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';

// Props
const props = defineProps({
  visible: {
    type: Boolean,
    default: false
  },
  area: {
    type: Object,
    default: () => ({})
  }
});

// Emits
const emit = defineEmits(['move', 'close']);

// 响应式数据
const dialogVisible = ref(false);
const form = reactive({
  targetParentId: null
});

// 树形选项数据（模拟）
const treeOptions = ref([
  {
    value: 1,
    label: '总部园区 (SITE)',
    children: [
      {
        value: 2,
        label: 'A仓库 (WAREHOUSE)',
        children: [
          { value: 3, label: '一楼 (FLOOR)' },
          { value: 4, label: '二楼 (FLOOR)' }
        ]
      },
      {
        value: 5,
        label: 'B仓库 (WAREHOUSE)'
      }
    ]
  }
]);

// 级联选择器配置
const cascaderProps = {
  value: 'value',
  label: 'label',
  children: 'children',
  checkStrictly: true,
  emitPath: false
};

// 监听visible变化
watch(
  () => props.visible,
  (newVal) => {
    dialogVisible.value = newVal;
    if (newVal) {
      // 重置表单
      form.targetParentId = null;
    }
  }
);

// 监听dialogVisible变化
watch(
  () => dialogVisible.value,
  (newVal) => {
    if (!newVal) {
      emit('close');
    }
  }
);

// 处理确认移动
const handleConfirm = async () => {
  if (!form.targetParentId) {
    ElMessage.warning('请选择目标父库区');
    return;
  }

  // 确认移动
  try {
    await ElMessageBox.confirm(
      `确定将 "${props.area.areaName}" 移动到所选库区下吗？`,
      '确认移动',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    );

    // 触发移动事件
    emit('move', props.area.id, form.targetParentId);

    // 关闭对话框
    dialogVisible.value = false;
  } catch (error) {
    // 用户取消操作
    console.log('用户取消移动操作');
  }
};

// 处理关闭
const handleClose = () => {
  dialogVisible.value = false;
};
</script>

<style scoped>
.dialog-footer {
  text-align: right;
}
</style>