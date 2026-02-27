<template>
  <div class="basic-info-form">
    <el-form
      ref="formRef"
      :model="form"
      :rules="rules"
      label-width="120px"
      style="max-width: 800px;"
    >
      <el-collapse v-model="activeNames">
        <el-collapse-item title="基础信息" name="basic">
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="上级库区" prop="parentId">
                <el-cascader
                  v-model="form.parentId"
                  :options="treeData"
                  :props="cascaderProps"
                  placeholder="请选择上级库区"
                  clearable
                  filterable
                  style="width: 100%;"
                />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="库区层级" prop="areaLevel">
                <el-select
                  v-model="form.areaLevel"
                  placeholder="请选择层级"
                  :disabled="!!form.id"
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
        </el-collapse-item>

        <el-collapse-item title="阈值与告警" name="threshold">
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

          <el-divider>温度阈值</el-divider>
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="最小温度">
                <el-input-number
                  v-model="form.temperatureThresholdMin"
                  :min="-50"
                  :max="form.temperatureThresholdMax"
                  :precision="2"
                  :step="0.5"
                  placeholder="最小温度"
                  style="width: 100%;"
                />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="最大温度">
                <el-input-number
                  v-model="form.temperatureThresholdMax"
                  :min="form.temperatureThresholdMin"
                  :max="50"
                  :precision="2"
                  :step="0.5"
                  placeholder="最大温度"
                  style="width: 100%;"
                />
              </el-form-item>
            </el-col>
          </el-row>

          <el-divider>湿度阈值</el-divider>
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="最小湿度">
                <el-input-number
                  v-model="form.humidityThresholdMin"
                  :min="0"
                  :max="form.humidityThresholdMax"
                  :precision="2"
                  :step="0.5"
                  placeholder="最小湿度"
                  style="width: 100%;"
                />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="最大湿度">
                <el-input-number
                  v-model="form.humidityThresholdMax"
                  :min="form.humidityThresholdMin"
                  :max="100"
                  :precision="2"
                  :step="0.5"
                  placeholder="最大湿度"
                  style="width: 100%;"
                />
              </el-form-item>
            </el-col>
          </el-row>

          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="阈值策略">
                <el-radio-group v-model="thresholdStrategy">
                  <el-radio label="inherit">继承父级</el-radio>
                  <el-radio label="override">覆盖</el-radio>
                  <el-radio label="default">使用系统默认</el-radio>
                </el-radio-group>
              </el-form-item>
            </el-col>
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
          </el-row>
        </el-collapse-item>

        <el-collapse-item title="其他信息" name="other">
          <el-row :gutter="20">
            <el-col :span="24">
              <el-form-item label="备注">
                <el-input
                  v-model="form.remark"
                  type="textarea"
                  :rows="4"
                  placeholder="请输入备注信息"
                />
              </el-form-item>
            </el-col>
          </el-row>
        </el-collapse-item>
      </el-collapse>

      <div class="form-footer">
        <el-button type="primary" @click="submitForm">保存</el-button>
        <el-button @click="cancel">取消</el-button>
      </div>
    </el-form>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue';
import { ElMessage } from 'element-plus';

const props = defineProps({
  area: {
    type: Object,
    required: true
  }
});

const emit = defineEmits(['save', 'cancel']);

// 响应式数据
const formRef = ref();
const activeNames = ref(['basic', 'threshold', 'other']);
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

// 树形数据（模拟）
const treeData = ref([
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
  checkStrictly: true, // 可选择任意级别的节点
  emitPath: false // 只返回叶子节点的值
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

// 初始化表单数据
onMounted(() => {
  Object.assign(form, props.area);
});

// 提交表单
const submitForm = async () => {
  try {
    await formRef.value.validate();
    // 触发保存事件
    emit('save', { ...form });
  } catch (error) {
    ElMessage.error('请检查表单信息');
  }
};

// 取消编辑
const cancel = () => {
  emit('cancel');
};
</script>

<style scoped>
.basic-info-form {
  padding: 20px 0;
}

.form-footer {
  text-align: center;
  margin-top: 30px;
}

.el-divider {
  margin: 20px 0;
}
</style>