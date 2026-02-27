<template>
  <el-dialog
    v-model="dialogVisible"
    title="批量导入库区"
    width="50%"
    :before-close="handleClose"
  >
    <el-upload
      ref="uploadRef"
      drag
      :action="uploadUrl"
      :headers="headers"
      :on-success="handleUploadSuccess"
      :on-error="handleUploadError"
      :on-change="handleFileChange"
      :auto-upload="false"
      :limit="1"
      accept=".xlsx,.xls,.csv"
      name="file"
    >
      <el-icon class="el-icon--upload"><upload-filled /></el-icon>
      <div class="el-upload__text">
        将文件拖到此处，或<em>点击上传</em>
      </div>
      <template #tip>
        <div class="el-upload__tip">
          只能上传 Excel 或 CSV 文件，大小不超过 10MB
          <el-button type="text" @click="downloadTemplate">下载模板</el-button>
        </div>
      </template>
    </el-upload>

    <div v-if="uploadResult" class="upload-result">
      <el-card>
        <template #header>
          <span>上传结果</span>
        </template>
        <p>成功导入: <strong>{{ uploadResult.successCount }}</strong> 条</p>
        <p>失败条目: <strong>{{ uploadResult.failCount }}</strong> 条</p>
        <el-collapse v-if="uploadResult.failedItems && uploadResult.failedItems.length > 0">
          <el-collapse-item title="查看失败详情">
            <el-table :data="uploadResult.failedItems" stripe>
              <el-table-column prop="index" label="行号" width="80" />
              <el-table-column prop="reason" label="失败原因" />
            </el-table>
          </el-collapse-item>
        </el-collapse>
      </el-card>
    </div>

    <template #footer>
      <span class="dialog-footer">
        <el-button @click="handleClose">取消</el-button>
        <el-button type="primary" @click="submitUpload" :loading="uploading">
          {{ uploading ? '上传中...' : '开始上传' }}
        </el-button>
      </span>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, reactive, watch } from 'vue';
import { ElMessage, ElNotification } from 'element-plus';
import { UploadFilled } from '@element-plus/icons-vue';
import { warehouseAreaApi } from '@/utils/api';

// Props
const props = defineProps({
  visible: {
    type: Boolean,
    default: false
  }
});

// Emits
const emit = defineEmits(['import', 'close']);

// 响应式数据
const dialogVisible = ref(false);
const uploadRef = ref();
const uploading = ref(false);
const uploadResult = ref(null);
const selectedFile = ref(null);

// 请求配置
const headers = {
  'Content-Type': 'multipart/form-data'
};

// 上传URL - 这里应该根据实际情况调整
const uploadUrl = '/api/warehouse-areas/import';

// 监听visible变化
watch(
  () => props.visible,
  (newVal) => {
    dialogVisible.value = newVal;
    if (!newVal) {
      // 关闭时重置状态
      resetState();
    }
  }
);

// 处理文件选择
const handleFileChange = (file, fileList) => {
  if (fileList.length > 0) {
    selectedFile.value = file.raw;
  } else {
    selectedFile.value = null;
  }
};

// 下载模板
const downloadTemplate = async () => {
  try {
    // 模拟下载模板
    // 实际项目中应该从后端获取模板文件
    const templateUrl = '/api/warehouse-areas/template';
    window.open(templateUrl, '_blank');
    ElMessage.success('正在下载模板...');
  } catch (error) {
    ElMessage.error('下载模板失败');
  }
};

// 提交上传
const submitUpload = async () => {
  if (!selectedFile.value) {
    ElMessage.warning('请选择要上传的文件');
    return;
  }

  uploading.value = true;
  uploadResult.value = null;

  try {
    const formData = new FormData();
    formData.append('file', selectedFile.value);

    // 调用导入API
    const response = await warehouseAreaApi.import(formData);

    // 模拟上传结果
    uploadResult.value = {
      successCount: response.data?.successCount || 0,
      failCount: response.data?.failCount || 0,
      failedItems: response.data?.failedItems || []
    };

    if (response.data?.failCount === 0) {
      ElNotification({
        title: '导入成功',
        message: `成功导入 ${response.data?.successCount} 条库区数据`,
        type: 'success'
      });

      // 触发导入成功事件
      emit('import', selectedFile.value);
    } else {
      ElNotification({
        title: '部分导入',
        message: `成功导入 ${response.data?.successCount} 条，${response.data?.failCount} 条失败`,
        type: 'warning'
      });
    }
  } catch (error) {
    console.error('上传失败', error);
    ElMessage.error('导入失败，请检查文件格式和内容');
  } finally {
    uploading.value = false;
  }
};

// 上传成功回调
const handleUploadSuccess = (response) => {
  console.log('上传成功', response);
};

// 上传失败回调
const handleUploadError = (error) => {
  console.error('上传失败', error);
  ElMessage.error('上传失败，请稍后再试');
};

// 重置状态
const resetState = () => {
  uploadResult.value = null;
  selectedFile.value = null;
  uploading.value = false;
  uploadRef.value?.clearFiles();
};

// 处理关闭
const handleClose = () => {
  dialogVisible.value = false;
};
</script>

<style scoped>
.upload-result {
  margin-top: 20px;
}

.dialog-footer {
  text-align: right;
}

.el-upload__tip {
  font-size: 12px;
  color: #909399;
  margin-top: 10px;
}
</style>