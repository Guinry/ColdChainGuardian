<template>
  <el-drawer
    v-model="drawerVisible"
    :title="isEdit ? '编辑库区' : '新增库区'"
    size="60%"
    :before-close="handleClose"
  >
    <el-form
      ref="formRef"
      :model="form"
      :rules="rules"
      label-width="120px"
      style="padding-right: 20px;"
    >
      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="上级库区">
            <el-cascader
              v-model="form.parentId"
              :options="treeOptions"
              :props="cascaderProps"
              placeholder="请选择上级库区"
              clearable
              filterable
              :disabled="isEdit"
              style="width: 100%;"
            />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="库区层级" prop="areaLevel">
            <el-select
              v-model="form.areaLevel"
              placeholder="请选择层级"
              :disabled="isEdit"
              style="width: 100%;"
            >
              <el-option label="SITE (站点)" value="SITE" />
              <el-option label="WAREHOUSE (仓库)" value="WAREHOUSE" />
              <el-option label="FLOOR (楼层)" value="FLOOR" />
              <el-option label="AREA (区域)" value="AREA" />
              <el-option label="BIN (库位)" value="BIN" />
            </el-select>
          </el-form-item>
        </el-col>
      </el-row>

      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="库区编码" prop="areaCode">
            <el-input v-model="form.areaCode" placeholder="请输入库区编码" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="库区名称" prop="areaName">
            <el-input v-model="form.areaName" placeholder="请输入库区名称" />
          </el-form-item>
        </el-col>
      </el-row>

      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="地址">
            <el-input
              v-model="form.address"
              placeholder="请输入地址"
              :disabled="!isSiteOrWarehouse"
            />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="位置描述">
            <el-input v-model="form.locationDesc" placeholder="请输入位置描述" />
          </el-form-item>
        </el-col>
      </el-row>

      <el-divider>阈值设置</el-divider>

      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="温度范围">
            <el-input-number
              v-model="form.temperatureThresholdMin"
              :min="-50"
              :max="form.temperatureThresholdMax"
              :precision="2"
              :step="0.5"
              placeholder="最小温度"
              style="width: 45%;"
            />
            <span style="margin: 0 10px;">~</span>
            <el-input-number
              v-model="form.temperatureThresholdMax"
              :min="form.temperatureThresholdMin"
              :max="50"
              :precision="2"
              :step="0.5"
              placeholder="最大温度"
              style="width: 45%;"
            />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="湿度范围">
            <el-input-number
              v-model="form.humidityThresholdMin"
              :min="0"
              :max="form.humidityThresholdMax"
              :precision="2"
              :step="0.5"
              placeholder="最小湿度"
              style="width: 45%;"
            />
            <span style="margin: 0 10px;">~</span>
            <el-input-number
              v-model="form.humidityThresholdMax"
              :min="form.humidityThresholdMin"
              :max="100"
              :precision="2"
              :step="0.5"
              placeholder="最大湿度"
              style="width: 45%;"
            />
          </el-form-item>
        </el-col>
      </el-row>

      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="告警状态">
            <el-switch
              v-model="form.alarmEnabled"
              :active-value="1"
              :inactive-value="0"
              inline-prompt
              active-text="开启"
              inactive-text="关闭"
            />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="启用状态">
            <el-switch
              v-model="form.status"
              :active-value="1"
              :inactive-value="0"
              inline-prompt
              active-text="启用"
              inactive-text="禁用"
            />
          </el-form-item>
        </el-col>
      </el-row>

      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="排序号">
            <el-input-number
              v-model="form.sortNo"
              :min="0"
              :max="9999"
              style="width: 100%;"
            />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="阈值策略">
            <el-radio-group v-model="thresholdStrategy">
              <el-radio label="inherit">继承父级</el-radio>
              <el-radio label="override">覆盖</el-radio>
            </el-radio-group>
          </el-form-item>
        </el-col>
      </el-row>

      <el-form-item label="备注">
        <el-input
          v-model="form.remark"
          type="textarea"
          :rows="4"
          placeholder="请输入备注信息"
        />
      </el-form-item>
    </el-form>

    <template #footer>
      <div class="drawer-footer">
        <el-button @click="handleClose">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </div>
    </template>
  </el-drawer>
</template>

<script setup>
import { ref, reactive, computed, watch, nextTick } from 'vue';
import { ElMessage } from 'element-plus';

// Props
const props = defineProps({
  visible: {
    type: Boolean,
    default: false
  },
  area: {
    type: Object,
    default: () => ({})
  },
  isEdit: {
    type: Boolean,
    default: false
  }
});

// Emits
const emit = defineEmits(['save', 'close']);

// 响应式数据
const drawerVisible = computed({
  get: () => props.visible,
  set: (val) => emit('close', val)
});

const formRef = ref();
const thresholdStrategy = ref('inherit'); // 默认继承策略

// 表单数据
const form = reactive({
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

// 是否为SITE或WAREHOUSE层级
const isSiteOrWarehouse = computed(() => {
  return form.areaLevel === 'SITE' || form.areaLevel === 'WAREHOUSE';
});

// 表单验证规则
const rules = {
  areaCode: [
    { required: true, message: '请输入库区编码', trigger: 'blur' },
    { min: 2, max: 50, message: '编码长度应在2-50之间', trigger: 'blur' },
    { pattern: /^[A-Z0-9_-]+$/, message: '编码只能包含大写字母、数字、下划线和横线', trigger: 'blur' }
  ],
  areaName: [
    { required: true, message: '请输入库区名称', trigger: 'blur' },
    { min: 2, max: 100, message: '名称长度应在2-100之间', trigger: 'blur' }
  ],
  areaLevel: [
    { required: true, message: '请选择层级', trigger: 'change' }
  ]
};

// 监听props变化更新表单
watch(
  () => props.area,
  (newVal) => {
    if (newVal) {
      Object.assign(form, {
        id: newVal.id || null,
        parentId: newVal.parentId || null,
        areaCode: newVal.areaCode || '',
        areaName: newVal.areaName || '',
        areaLevel: newVal.areaLevel || 'AREA',
        address: newVal.address || '',
        locationDesc: newVal.locationDesc || '',
        temperatureThresholdMin: newVal.temperatureThresholdMin ?? -20.00,
        temperatureThresholdMax: newVal.temperatureThresholdMax ?? 8.00,
        humidityThresholdMin: newVal.humidityThresholdMin ?? 30.00,
        humidityThresholdMax: newVal.humidityThresholdMax ?? 70.00,
        alarmEnabled: newVal.alarmEnabled ?? 1,
        status: newVal.status ?? 1,
        sortNo: newVal.sortNo ?? 0,
        remark: newVal.remark || ''
      });
    }
  },
  { deep: true, immediate: true }
);

// 监听表单中的areaLevel变化，调整阈值策略
watch(
  () => form.areaLevel,
  (newLevel) => {
    // 某些层级可能有默认阈值策略
    if (newLevel === 'BIN') {
      thresholdStrategy.value = 'override';
    }
  }
);

// 处理提交
const handleSubmit = async () => {
  try {
    await formRef.value.validate();

    // 如果是覆盖阈值策略，需要设置阈值来源
    const areaData = { ...form };

    // 发送数据到父组件
    emit('save', areaData);

    // 关闭抽屉
    drawerVisible.value = false;
  } catch (error) {
    ElMessage.error('请检查表单信息');
  }
};

// 处理关闭
const handleClose = () => {
  // 验证是否有未保存的更改
  emit('close');
};
</script>

<style scoped>
.drawer-footer {
  text-align: right;
  padding-top: 20px;
  border-top: 1px solid #e4e7ed;
}
</style>