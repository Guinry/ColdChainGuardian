<template>
  <Layout>
    <div class="system-page">
      <div class="page-head">
        <div>
          <h1>权限分配</h1>
          <p>按角色查看 Web 端菜单与核心业务权限，权限规则与路由守卫保持一致。</p>
        </div>
        <el-button type="primary" @click="savePermissions">
          <el-icon><Check /></el-icon>
          保存配置
        </el-button>
      </div>

      <el-row :gutter="16" class="summary-row">
        <el-col :span="8" v-for="item in roleSummary" :key="item.role">
          <el-card shadow="never" class="summary-card">
            <div class="summary-title">{{ item.name }}</div>
            <div class="summary-value">{{ item.enabled }}/{{ permissionRows.length }}</div>
            <div class="summary-note">已开放模块</div>
          </el-card>
        </el-col>
      </el-row>

      <el-card shadow="never" class="matrix-card">
        <el-table :data="permissionRows" border>
          <el-table-column prop="module" label="模块" width="150" fixed />
          <el-table-column prop="description" label="权限说明" min-width="240" />
          <el-table-column v-for="role in roles" :key="role.key" :label="role.name" align="center" width="150">
            <template #default="{ row }">
              <el-switch
                v-model="row.roles[role.key]"
                :disabled="row.locked || role.key === 'ADMIN'"
                active-text="允许"
                inactive-text="禁止"
                inline-prompt
              />
            </template>
          </el-table-column>
        </el-table>
      </el-card>
    </div>
  </Layout>
</template>

<script setup>
import { computed, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import { Check } from '@element-plus/icons-vue'
import Layout from '@/components/Layout.vue'

const roles = [
  { key: 'ADMIN', name: '管理员' },
  { key: 'MANAGER', name: '管理层' },
  { key: 'EMPLOYEE', name: '员工端' }
]

const permissionRows = reactive([
  {
    module: '仪表盘',
    description: '查看设备、告警、工单和趋势总览',
    locked: true,
    roles: { ADMIN: true, MANAGER: true, EMPLOYEE: true }
  },
  {
    module: '库区管理',
    description: '维护仓库、库区层级、阈值继承关系',
    roles: { ADMIN: true, MANAGER: true, EMPLOYEE: false }
  },
  {
    module: '设备管理',
    description: '注册设备、修改阈值、启停告警和查看遥测数据',
    roles: { ADMIN: true, MANAGER: true, EMPLOYEE: false }
  },
  {
    module: '实时监测',
    description: '查看库区设备在线状态、温湿度和异常趋势',
    roles: { ADMIN: true, MANAGER: true, EMPLOYEE: true }
  },
  {
    module: '告警中心',
    description: '查询、研判、忽略、解决告警并转派工单',
    roles: { ADMIN: true, MANAGER: true, EMPLOYEE: true }
  },
  {
    module: '工单管理',
    description: '创建、分派、处理、验收和关闭工单',
    roles: { ADMIN: true, MANAGER: true, EMPLOYEE: true }
  },
  {
    module: '趋势分析',
    description: '查看环境、告警、设备和工单趋势报表',
    roles: { ADMIN: true, MANAGER: true, EMPLOYEE: false }
  },
  {
    module: 'AI 助手',
    description: '使用自然语言查询和生成冷链运维分析',
    roles: { ADMIN: true, MANAGER: true, EMPLOYEE: false }
  },
  {
    module: '系统管理',
    description: '管理员、员工、权限和审计配置',
    roles: { ADMIN: true, MANAGER: false, EMPLOYEE: false }
  }
])

const roleSummary = computed(() => roles.map(role => ({
  ...role,
  role: role.key,
  enabled: permissionRows.filter(row => row.roles[role.key]).length
})))

const savePermissions = () => {
  localStorage.setItem('ccg_permission_matrix', JSON.stringify(permissionRows))
  ElMessage.success('权限配置已保存到本地演示配置')
}
</script>

<style scoped>
.system-page {
  padding: 20px 24px 28px;
  min-height: 100%;
  background: var(--ccg-bg);
}

.page-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 16px;
}

.page-head h1 {
  margin: 0;
  font-size: 22px;
  line-height: 1.2;
  font-weight: 750;
  color: var(--ccg-text);
}

.page-head p {
  margin: 6px 0 0;
  color: var(--ccg-muted);
  font-size: 13px;
}

.summary-row {
  margin-bottom: 16px;
}

.summary-card,
.matrix-card {
  border-radius: 8px;
  box-shadow: var(--ccg-shadow-sm) !important;
}

.summary-title {
  color: #606266;
  font-size: 14px;
}

.summary-value {
  margin-top: 8px;
  font-size: 28px;
  font-weight: 700;
  color: #303133;
}

.summary-note {
  margin-top: 4px;
  color: #909399;
  font-size: 13px;
}
</style>
