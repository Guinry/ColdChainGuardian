<template>
  <div class="basic-info-view">
    <el-descriptions :column="2" border>
      <el-descriptions-item label="上级库区">
        {{ area.parentId ? `${getParentAreaName(area.parentId)}` : '顶级库区' }}
      </el-descriptions-item>
      <el-descriptions-item label="库区编码">{{ area.areaCode }}</el-descriptions-item>
      <el-descriptions-item label="库区名称">{{ area.areaName }}</el-descriptions-item>
      <el-descriptions-item label="层级">
        <el-tag size="small">{{ area.areaLevel }}</el-tag>
      </el-descriptions-item>
      <el-descriptions-item label="地址">{{ area.address || '-' }}</el-descriptions-item>
      <el-descriptions-item label="位置描述">{{ area.locationDesc || '-' }}</el-descriptions-item>
      <el-descriptions-item label="启用状态">
        <el-tag :type="area.status === 1 ? 'success' : 'danger'">
          {{ area.status === 1 ? '启用' : '禁用' }}
        </el-tag>
      </el-descriptions-item>
      <el-descriptions-item label="告警状态">
        <el-tag :type="area.alarmEnabled === 1 ? 'success' : 'warning'">
          {{ area.alarmEnabled === 1 ? '开启' : '关闭' }}
        </el-tag>
      </el-descriptions-item>
      <el-descriptions-item label="温度阈值">
        {{ area.temperatureThresholdMin }}°C ~ {{ area.temperatureThresholdMax }}°C
      </el-descriptions-item>
      <el-descriptions-item label="湿度阈值">
        {{ area.humidityThresholdMin }}% ~ {{ area.humidityThresholdMax }}%
      </el-descriptions-item>
      <el-descriptions-item label="排序号">{{ area.sortNo }}</el-descriptions-item>
      <el-descriptions-item label="备注">{{ area.remark || '-' }}</el-descriptions-item>
      <el-descriptions-item label="创建时间">{{ formatDate(area.createTime) }}</el-descriptions-item>
      <el-descriptions-item label="更新时间">{{ formatDate(area.updateTime) }}</el-descriptions-item>
    </el-descriptions>
  </div>
</template>

<script setup>
import { defineProps } from 'vue';

const props = defineProps({
  area: {
    type: Object,
    required: true
  }
});

// 获取父级库区名称
const getParentAreaName = (parentId) => {
  // 实际项目中可以从store中获取库区信息
  return `库区ID: ${parentId}`;
};

// 格式化日期
const formatDate = (dateStr) => {
  if (!dateStr) return '-';
  const date = new Date(dateStr);
  return date.toLocaleString('zh-CN');
};
</script>

<style scoped>
.basic-info-view {
  padding: 20px 0;
}
</style>