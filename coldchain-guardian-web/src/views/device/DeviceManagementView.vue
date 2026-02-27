<template>
  <div class="device-management-container">
    <!-- Top Navigation Bar -->
    <div class="top-bar">
      <div class="logo-section">
        <div class="logo">
          <svg viewBox="0 0 24 24" width="32" height="32" fill="none" xmlns="http://www.w3.org/2000/svg">
            <path d="M12 2L13.09 8.26L22 9L13.09 9.74L12 16L10.91 9.74L2 9L10.91 8.26L12 2Z" fill="#409EFF"/>
            <circle cx="12" cy="12" r="10" stroke="#409EFF" stroke-width="2"/>
          </svg>
        </div>
        <span class="app-title">ColdChain Guardian</span>
      </div>

      <div class="search-section">
        <el-input
          v-model="globalSearch"
          placeholder="全局搜索..."
          :prefix-icon="Search"
          class="global-search"
        />
      </div>

      <div class="action-section">
        <el-badge :value="unreadNotifications" class="notification-badge">
          <el-button circle class="notification-btn">
            <el-icon><Bell /></el-icon>
          </el-button>
        </el-badge>

        <el-dropdown>
          <div class="user-avatar">
            <el-avatar :size="32" :src="userAvatar">{{ userInitial }}</el-avatar>
            <span class="user-name">{{ userInfo.realName }}</span>
            <el-icon><ArrowDown /></el-icon>
          </div>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item @click="viewProfile">个人资料</el-dropdown-item>
              <el-dropdown-item @click="settings">系统设置</el-dropdown-item>
              <el-dropdown-item divided @click="logout">退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </div>

    <div class="device-management-layout">
      <!-- Side Menu -->
      <div class="side-menu">
        <el-menu
          :default-active="activeMenu"
          class="menu"
          :collapse="false"
          :unique-opened="true"
          :router="true"
        >
          <el-menu-item index="/dashboard">
            <el-icon><House /></el-icon>
            <span>Dashboard</span>
          </el-menu-item>

          <el-sub-menu index="monitoring">
            <template #title>
              <el-icon><Monitor /></el-icon>
              <span>监测管理</span>
            </template>
            <el-menu-item index="/warehouse-area">库区管理</el-menu-item>
            <el-menu-item index="/devices">设备管理</el-menu-item>
            <el-menu-item index="/monitoring/realtime">实时监测</el-menu-item>
          </el-sub-menu>

          <el-sub-menu index="alerts-orders">
            <template #title>
              <el-icon><Warning /></el-icon>
              <span>告警与工单</span>
            </template>
            <el-menu-item index="/alerts">告警中心</el-menu-item>
            <el-menu-item index="/orders">工单管理</el-menu-item>
          </el-sub-menu>

          <el-sub-menu index="analysis">
            <template #title>
              <el-icon><DataAnalysis /></el-icon>
              <span>数据分析</span>
            </template>
            <el-menu-item index="/analysis/trends">趋势分析</el-menu-item>
            <el-menu-item index="/analysis/ai">AI 智能助手</el-menu-item>
          </el-sub-menu>

          <!-- System Management menu only visible for SUPER_ADMIN -->
          <el-sub-menu v-if="isSuperAdmin" index="system">
            <template #title>
              <el-icon><Setting /></el-icon>
              <span>系统管理（超管）</span>
            </template>
            <el-menu-item index="/admin/users">管理员管理</el-menu-item>
            <el-menu-item index="/admin/permissions">权限分配</el-menu-item>
          </el-sub-menu>
        </el-menu>
      </div>

      <!-- Main Content -->
      <div class="main-content">
        <div class="page-header">
          <h2>设备管理</h2>
          <div class="header-actions">
            <el-button type="primary" @click="showAddDeviceDialog">
              <el-icon><Plus /></el-icon>
              新增设备
            </el-button>
            <el-button @click="batchImport">
              <el-icon><Upload /></el-icon>
              批量导入
            </el-button>
            <el-button @click="exportDevices">
              <el-icon><Download /></el-icon>
              导出
            </el-button>
          </div>
        </div>

        <div class="content-wrapper">
          <!-- 左侧树形结构 -->
          <div class="tree-panel">
            <div class="panel-header">
              <h3>库区结构</h3>
              <el-input
                v-model="areaSearchTree"
                placeholder="搜索库区..."
                :prefix-icon="Search"
                clearable
                @input="filterAreaTree"
              />
            </div>

            <el-tree
              ref="areaTreeRef"
              :data="areaTreeData"
              :props="treeProps"
              :filter-method="filterAreaMethod"
              :expand-on-click-node="false"
              highlight-current
              node-key="id"
              @node-click="onAreaNodeClick"
              class="custom-tree"
            >
              <template #default="{ node, data }">
                <div class="tree-node-content">
                  <span class="node-label">{{ data.areaName }}</span>
                  <span class="node-code">[{{ data.areaCode }}]</span>
                  <el-tag
                    size="small"
                    :type="getLevelTagType(data.areaLevel)"
                    class="level-tag"
                  >
                    {{ getLevelLabel(data.areaLevel) }}
                  </el-tag>
                </div>
              </template>
            </el-tree>
          </div>

          <!-- 右侧设备列表 -->
          <div class="device-list-panel">
            <div class="list-header">
              <h3>设备列表</h3>
              <div class="list-toolbar">
                <el-button-group>
                  <el-button @click="refreshData" :icon="Refresh">刷新</el-button>
                  <el-button @click="resetFilters">重置</el-button>
                </el-button-group>
              </div>
            </div>

            <!-- 筛选区域 -->
            <el-form :model="filterForm" inline class="filter-form">
              <el-form-item label="设备编码" prop="keyword">
                <el-input
                  v-model="filterForm.keyword"
                  placeholder="请输入设备编码或名称"
                  clearable
                  style="width: 200px;"
                />
              </el-form-item>
              <el-form-item label="设备类型" prop="deviceType">
                <el-select
                  v-model="filterForm.deviceType"
                  placeholder="请选择"
                  clearable
                  style="width: 150px;"
                >
                  <el-option label="温湿度传感器" value="TEMP_HUM" />
                  <el-option label="冷柜" value="FREEZER" />
                  <el-option label="车载设备" value="VEHICLE" />
                  <el-option label="门磁" value="DOOR" />
                </el-select>
              </el-form-item>
              <el-form-item label="在线状态" prop="onlineStatus">
                <el-select
                  v-model="filterForm.onlineStatus"
                  placeholder="请选择"
                  clearable
                  style="width: 120px;"
                >
                  <el-option label="在线" value="true" />
                  <el-option label="离线" value="false" />
                </el-select>
              </el-form-item>
              <el-form-item label="启用状态" prop="enabled">
                <el-select
                  v-model="filterForm.enabled"
                  placeholder="请选择"
                  clearable
                  style="width: 120px;"
                >
                  <el-option label="启用" value="true" />
                  <el-option label="禁用" value="false" />
                </el-select>
              </el-form-item>
              <el-form-item label="告警状态" prop="alarmEnabled">
                <el-select
                  v-model="filterForm.alarmEnabled"
                  placeholder="请选择"
                  clearable
                  style="width: 120px;"
                >
                  <el-option label="启用" value="true" />
                  <el-option label="禁用" value="false" />
                </el-select>
              </el-form-item>
              <el-form-item label="绑定库区" prop="areaId">
                <el-cascader
                  v-model="filterForm.areaId"
                  :options="areaTreeData"
                  :props="cascaderProps"
                  placeholder="选择库区"
                  clearable
                  style="width: 200px;"
                />
              </el-form-item>
              <el-form-item label="最后上报时间" prop="lastSeenRange">
                <el-date-picker
                  v-model="filterForm.lastSeenRange"
                  type="datetimerange"
                  range-separator="至"
                  start-placeholder="开始时间"
                  end-placeholder="结束时间"
                  style="width: 280px;"
                />
              </el-form-item>
              <el-form-item>
                <el-button type="primary" @click="searchDevices" :icon="Search">查询</el-button>
              </el-form-item>
            </el-form>

            <!-- 设备表格 -->
            <el-table
              :data="deviceList"
              style="width: 100%"
              row-key="id"
              border
              stripe
              table-layout="fixed"
              :header-cell-style="{ background: '#f8f9ff', color: '#606266' }"
              @selection-change="handleSelectionChange"
            >
              <el-table-column type="selection" width="55" align="center" />
              <el-table-column prop="deviceCode" label="设备编码" min-width="120" fixed="left" show-overflow-tooltip>
                <template #default="{ row }">
                  <div class="table-device-code">
                    <el-icon><VideoCamera /></el-icon>
                    <span>{{ row.deviceCode }}</span>
                  </div>
                </template>
              </el-table-column>
              <el-table-column prop="deviceName" label="设备名称" min-width="140" show-overflow-tooltip />
              <el-table-column prop="deviceType" label="设备类型" min-width="100" align="center">
                <template #default="{ row }">
                  <el-tag :type="getDeviceTypeTag(row.deviceType)" size="small">
                    {{ getDeviceTypeLabel(row.deviceType) }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="areaPath" label="所属库区" min-width="150" show-overflow-tooltip>
                <template #default="{ row }">
                  <el-link type="primary" @click="goToArea(row.areaId)" v-if="row.areaId" :underline="false">
                    <el-icon><Location /></el-icon>
                    {{ row.areaPath }}
                  </el-link>
                  <span v-else class="unassigned">未分配</span>
                </template>
              </el-table-column>
              <el-table-column prop="onlineStatus" label="在线状态" min-width="90" align="center">
                <template #default="{ row }">
                  <el-tag
                    :type="row.onlineStatus === 'ONLINE' ? 'success' : 'danger'"
                    size="small"
                    :effect="row.onlineStatus === 'ONLINE' ? 'dark' : 'light'"
                  >
                    <el-icon v-if="row.onlineStatus === 'ONLINE'"><CircleCheckFilled /></el-icon>
                    <el-icon v-else><CircleCloseFilled /></el-icon>
                    {{ row.onlineStatus === 'ONLINE' ? '在线' : '离线' }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="currentTemp" label="当前温度" min-width="100" align="center">
                <template #default="{ row }">
                  <div class="temp-display">
                    <el-icon><Temperature /></el-icon>
                    <span v-if="row.currentTemp" :class="{
                      'temp-normal': isWithinThreshold(row, 'temp'),
                      'temp-alert': !isWithinThreshold(row, 'temp')
                    }">
                      {{ row.currentTemp.toFixed(2) }}°C
                    </span>
                    <span v-else class="temp-na">-</span>
                  </div>
                </template>
              </el-table-column>
              <el-table-column prop="currentHumidity" label="当前湿度" min-width="100" align="center">
                <template #default="{ row }">
                  <div class="humidity-display">
                    <span class="humidity-icon">💧</span>
                    <span v-if="row.currentHumidity" :class="{
                      'humidity-normal': isWithinThreshold(row, 'humidity'),
                      'humidity-alert': !isWithinThreshold(row, 'humidity')
                    }">
                      {{ row.currentHumidity.toFixed(2) }}%
                    </span>
                    <span v-else class="humidity-na">-</span>
                  </div>
                </template>
              </el-table-column>
              <el-table-column prop="lastSeenTime" label="最后上报" min-width="140" align="center">
                <template #default="{ row }">
                  <div class="last-seen-display">
                    <el-icon><Timer /></el-icon>
                    <span>{{ formatDate(row.lastSeenTime) }}</span>
                  </div>
                </template>
              </el-table-column>
              <el-table-column prop="alarmEnabled" label="告警状态" min-width="90" align="center">
                <template #default="{ row }">
                  <el-switch
                    v-model="row.alarmEnabled"
                    :active-value="true"
                    :inactive-value="false"
                    active-color="#13ce66"
                    inactive-color="#dcdfe6"
                    size="small"
                    @change="toggleAlarmStatus(row)"
                  />
                </template>
              </el-table-column>
              <el-table-column label="操作" min-width="300" fixed="right" align="center">
                <template #default="{ row }">
                  <el-button-group class="action-buttons">
                    <el-button
                      size="small"
                      @click="showDeviceInfo(row)"
                      type="info"
                      :icon="InfoFilled"
                      plain
                    >
                      查看
                    </el-button>
                    <el-button
                      size="small"
                      @click="showEditDeviceDialog(row)"
                      type="primary"
                      :icon="EditPen"
                      plain
                    >
                      编辑
                    </el-button>
                    <el-popconfirm
                      title="确定要复制此设备吗？"
                      @confirm="duplicateDevice(row)"
                    >
                      <template #reference>
                        <el-button
                          size="small"
                          type="warning"
                          :icon="CopyDocument"
                          plain
                        >
                          复制
                        </el-button>
                      </template>
                    </el-popconfirm>
                    <el-popconfirm
                      :title="`确定要${row.enabled ? '禁用' : '启用'}设备 ${row.deviceName} 吗？`"
                      @confirm="toggleDeviceStatus(row)"
                    >
                      <template #reference>
                        <el-button
                          size="small"
                          :type="row.enabled ? 'danger' : 'success'"
                          :icon="row.enabled ? 'CircleClose' : 'CircleCheck'"
                          plain
                        >
                          {{ row.enabled ? '禁用' : '启用' }}
                        </el-button>
                      </template>
                    </el-popconfirm>
                    <el-popconfirm
                      title="确定要删除此设备吗？此操作不可恢复！"
                      @confirm="deleteDevice(row)"
                    >
                      <template #reference>
                        <el-button
                          size="small"
                          type="danger"
                          :icon="Delete"
                        >
                          删除
                        </el-button>
                      </template>
                    </el-popconfirm>
                  </el-button-group>
                </template>
              </el-table-column>
            </el-table>

            <!-- 分页 -->
            <div class="pagination-container">
              <el-pagination
                v-model:current-page="pagination.currentPage"
                v-model:page-size="pagination.pageSize"
                :page-sizes="[10, 20, 50, 100]"
                :total="pagination.total"
                layout="total, sizes, prev, pager, next, jumper"
                @size-change="handleSizeChange"
                @current-change="handleCurrentChange"
              />
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 新增/编辑设备对话框 -->
    <el-dialog
      v-model="deviceDialogVisible"
      :title="editingDevice ? '编辑设备' : '新增设备'"
      width="600px"
      :before-close="closeDeviceDialog"
    >
      <el-form
        ref="deviceFormRef"
        :model="deviceForm"
        :rules="deviceFormRules"
        label-width="120px"
      >
        <el-form-item label="所属库区" prop="areaId">
          <el-tree-select
            v-model="deviceForm.areaId"
            :data="areaTreeData"
            :props="treeSelectProps"
            placeholder="请选择所属库区"
            filterable
            check-strictly
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="设备编码" prop="deviceCode">
          <el-input
            v-model="deviceForm.deviceCode"
            placeholder="请输入设备编码"
            :disabled="!!editingDevice"
          />
        </el-form-item>
        <el-form-item label="设备名称" prop="deviceName">
          <el-input
            v-model="deviceForm.deviceName"
            placeholder="请输入设备名称"
          />
        </el-form-item>
        <el-form-item label="设备类型" prop="deviceType">
          <el-select
            v-model="deviceForm.deviceType"
            placeholder="请选择设备类型"
            style="width: 100%"
          >
            <el-option label="温湿度传感器" value="TEMP_HUM" />
            <el-option label="冷柜" value="FREEZER" />
            <el-option label="车载设备" value="VEHICLE" />
            <el-option label="门磁" value="DOOR" />
          </el-select>
        </el-form-item>
        <el-form-item label="型号">
          <el-input
            v-model="deviceForm.model"
            placeholder="请输入设备型号"
          />
        </el-form-item>
        <el-form-item label="位置描述">
          <el-input
            v-model="deviceForm.locationDesc"
            type="textarea"
            :rows="2"
            placeholder="请输入位置描述"
          />
        </el-form-item>
        <el-form-item label="启用状态" prop="enabled">
          <el-switch
            v-model="deviceForm.enabled"
            :active-value="true"
            :inactive-value="false"
            active-text="启用"
            inactive-text="禁用"
          />
        </el-form-item>
        <el-form-item label="告警状态" prop="alarmEnabled">
          <el-switch
            v-model="deviceForm.alarmEnabled"
            :active-value="true"
            :inactive-value="false"
            active-text="启用告警"
            inactive-text="关闭告警"
          />
        </el-form-item>
        <el-form-item label="阈值模式" prop="thresholdMode">
          <el-radio-group v-model="deviceForm.thresholdMode" @change="onThresholdModeChange">
            <el-radio label="INHERIT">继承库区阈值</el-radio>
            <el-radio label="OVERRIDE">自定义阈值</el-radio>
          </el-radio-group>
        </el-form-item>
        <div v-if="deviceForm.thresholdMode === 'OVERRIDE'">
          <el-form-item label="温度下限">
            <el-input-number
              v-model="deviceForm.temperatureThresholdMin"
              :min="-50"
              :max="deviceForm.temperatureThresholdMax - 0.1"
              :step="0.1"
              :precision="2"
              style="width: 100%"
            />
          </el-form-item>
          <el-form-item label="温度上限">
            <el-input-number
              v-model="deviceForm.temperatureThresholdMax"
              :min="deviceForm.temperatureThresholdMin + 0.1"
              :max="50"
              :step="0.1"
              :precision="2"
              style="width: 100%"
            />
          </el-form-item>
          <el-form-item label="湿度下限">
            <el-input-number
              v-model="deviceForm.humidityThresholdMin"
              :min="0"
              :max="deviceForm.humidityThresholdMax - 0.1"
              :step="0.1"
              :precision="2"
              style="width: 100%"
            />
          </el-form-item>
          <el-form-item label="湿度上限">
            <el-input-number
              v-model="deviceForm.humidityThresholdMax"
              :min="deviceForm.humidityThresholdMin + 0.1"
              :max="100"
              :step="0.1"
              :precision="2"
              style="width: 100%"
            />
          </el-form-item>
        </div>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="closeDeviceDialog">取消</el-button>
          <el-button type="primary" @click="saveDevice" :loading="saving">确定</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 设备详情抽屉 -->
    <el-drawer
      v-model="deviceDetailVisible"
      title="设备详情"
      size="600px"
    >
      <div v-if="currentDevice" class="device-detail-content">
        <el-descriptions :column="1" border>
          <el-descriptions-item label="设备编码">{{ currentDevice.deviceCode }}</el-descriptions-item>
          <el-descriptions-item label="设备名称">{{ currentDevice.deviceName }}</el-descriptions-item>
          <el-descriptions-item label="设备类型">
            <el-tag :type="getDeviceTypeTag(currentDevice.deviceType)">
              {{ getDeviceTypeLabel(currentDevice.deviceType) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="所属库区">{{ currentDevice.areaPath || '未分配' }}</el-descriptions-item>
          <el-descriptions-item label="型号">{{ currentDevice.model || '-' }}</el-descriptions-item>
          <el-descriptions-item label="位置描述">{{ currentDevice.locationDesc || '-' }}</el-descriptions-item>
          <el-descriptions-item label="在线状态">
            <el-tag :type="currentDevice.onlineStatus === 'ONLINE' ? 'success' : 'danger'">
              {{ currentDevice.onlineStatus === 'ONLINE' ? '在线' : '离线' }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="当前温度">
            {{ currentDevice.currentTemp ? currentDevice.currentTemp.toFixed(2) + '°C' : '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="当前湿度">
            {{ currentDevice.currentHumidity ? currentDevice.currentHumidity.toFixed(2) + '%' : '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="最后上报时间">
            {{ formatDate(currentDevice.lastSeenTime) }}
          </el-descriptions-item>
          <el-descriptions-item label="启用状态">
            <el-tag :type="currentDevice.enabled ? 'success' : 'danger'">
              {{ currentDevice.enabled ? '启用' : '禁用' }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="告警状态">
            <el-tag :type="currentDevice.alarmEnabled ? 'success' : 'info'">
              {{ currentDevice.alarmEnabled ? '启用' : '关闭' }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="固件版本">{{ currentDevice.firmwareVersion || '-' }}</el-descriptions-item>
          <el-descriptions-item label="信号强度">{{ currentDevice.signalStrength || '-' }}</el-descriptions-item>
          <el-descriptions-item label="创建时间">{{ formatDate(currentDevice.createdTime) }}</el-descriptions-item>
          <el-descriptions-item label="更新时间">{{ formatDate(currentDevice.updatedTime) }}</el-descriptions-item>
        </el-descriptions>

        <div class="drawer-footer">
          <el-button @click="closeDeviceDetail">关闭</el-button>
          <el-button type="primary" @click="viewDeviceData(currentDevice)">查看数据</el-button>
        </div>
      </div>
    </el-drawer>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/store/auth'
import { ElMessage, ElMessageBox, ElNotification } from 'element-plus'
import {
  Search,
  Bell,
  ArrowDown,
  Plus,
  Upload,
  Download,
  Refresh,
  House,
  Monitor,
  Warning,
  DataAnalysis,
  Setting,
  Link,
  WarningFilled,
  CircleCloseFilled,
  Finished,
  User,
  Document,
  Grid,
  Operation,
  Tickets,
  Memo,
  House as HouseIcon,
  User as UserIcon,
  Location,
  WindPower as Temperature,
  Timer,
  CircleCheckFilled,
  InfoFilled,
  EditPen,
  CopyDocument,
  Delete,
  VideoCamera
} from '@element-plus/icons-vue'
import { deviceApi } from '@/api/device'
import { areaApi } from '@/api/area'

const router = useRouter()
const authStore = useAuthStore()

// User info from auth store
const userInfo = computed(() => authStore.user || {})
const isSuperAdmin = computed(() => authStore.getUserRole === 'SUPER_ADMIN')

// 响应式数据
const deviceList = ref([])
const selectedDevices = ref([])

// 筛选条件
const filterForm = reactive({
  keyword: '',
  deviceType: '',
  onlineStatus: '',
  enabled: '',
  alarmEnabled: '',
  areaId: null,
  lastSeenRange: []
})

// 分页信息
const pagination = reactive({
  currentPage: 1,
  pageSize: 10,
  total: 0
})

// 设备详情抽屉
const deviceDetailVisible = ref(false)
const currentDevice = ref(null)

// 设备对话框
const deviceDialogVisible = ref(false)
const editingDevice = ref(null)
const deviceForm = reactive({
  id: null,
  areaId: null,
  deviceCode: '',
  deviceName: '',
  deviceType: 'TEMP_HUM',
  model: '',
  locationDesc: '',
  enabled: true,
  thresholdMode: 'INHERIT',
  temperatureThresholdMin: null,
  temperatureThresholdMax: null,
  humidityThresholdMin: null,
  humidityThresholdMax: null,
  alarmEnabled: true
})

// 表单验证规则
const deviceFormRules = {
  areaId: [{ required: true, message: '请选择所属库区', trigger: 'change' }],
  deviceCode: [
    { required: true, message: '请输入设备编码', trigger: 'blur' },
    { min: 2, max: 100, message: '设备编码长度应在2-100个字符之间', trigger: 'blur' }
  ],
  deviceName: [
    { required: true, message: '请输入设备名称', trigger: 'blur' },
    { min: 2, max: 100, message: '设备名称长度应在2-100个字符之间', trigger: 'blur' }
  ],
  deviceType: [
    { required: true, message: '请选择设备类型', trigger: 'change' }
  ]
}

// 左侧库区树
const areaTreeRef = ref()
const areaTreeData = ref([])
const areaSearchTree = ref('')

// 库区树形结构配置
const treeProps = {
  children: 'children',
  label: 'areaName'
}

// 级联选择器配置
const cascaderProps = {
  value: 'id',
  label: 'areaName',
  children: 'children',
  checkStrictly: true,
  emitPath: false,
  expandTrigger: 'hover'
}

// 树选择器配置
const treeSelectProps = {
  value: 'id',
  label: 'areaName',
  children: 'children',
  checkStrictly: true
}

// Mock data for dashboard components
const globalSearch = ref('')
const unreadNotifications = ref(3)
const activeMenu = ref('/devices')
const saving = ref(false)

// Computed properties for user info
const userInitial = computed(() => {
  return userInfo.value.realName ? userInfo.value.realName.charAt(0) : 'U'
})

const userAvatar = computed(() => {
  // Return a default avatar if no real avatar exists
  return 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png'
})

// 获取层级标签文本
const getLevelLabel = (level) => {
  const labels = {
    SITE: '站点',
    WAREHOUSE: '仓库',
    FLOOR: '楼层',
    AREA: '库区',
    BIN: '库位'
  }
  return labels[level] || level
}

// 获取层级标签类型
const getLevelTagType = (level) => {
  const types = {
    SITE: 'primary',
    WAREHOUSE: 'success',
    FLOOR: 'warning',
    AREA: 'info',
    BIN: 'danger'
  }
  return types[level] || 'info'
}

// 获取设备类型标签
const getDeviceTypeTag = (type) => {
  const types = {
    TEMP_HUM: 'success',
    FREEZER: 'warning',
    VEHICLE: 'info',
    DOOR: 'danger'
  }
  return types[type] || 'default'
}

// 获取设备类型标签文本
const getDeviceTypeLabel = (type) => {
  const labels = {
    TEMP_HUM: '温湿度传感器',
    FREEZER: '冷柜',
    VEHICLE: '车载设备',
    DOOR: '门磁'
  }
  return labels[type] || type
}

// 检查数值是否在阈值范围内
const isWithinThreshold = (device, type) => {
  if (!device) return true

  if (type === 'temp') {
    const temp = device.currentTemp
    const min = device.temperatureThresholdMin
    const max = device.temperatureThresholdMax

    if (temp === null || temp === undefined) return true
    if (min !== null && temp < min) return false
    if (max !== null && temp > max) return false
    return true
  }
  else if (type === 'humidity') {
    const humidity = device.currentHumidity
    const min = device.humidityThresholdMin
    const max = device.humidityThresholdMax

    if (humidity === null || humidity === undefined) return true
    if (min !== null && humidity < min) return false
    if (max !== null && humidity > max) return false
    return true
  }

  return true
}

// 切换告警状态
const toggleAlarmStatus = async (device) => {
  try {
    const newStatus = !device.alarmEnabled
    await deviceApi.update(device.id, { ...device, alarmEnabled: newStatus })
    ElMessage.success(`告警${newStatus ? '已启用' : '已关闭'}`)
    // 更新本地数据
    device.alarmEnabled = newStatus
  } catch (error) {
    // 恢复原状态
    device.alarmEnabled = !device.alarmEnabled
    ElMessage.error('更新告警状态失败')
  }
}

// 树形数据过滤
const filterAreaMethod = (value, data) => {
  if (!value) return true
  return data.areaName.toLowerCase().includes(value.toLowerCase()) ||
         data.areaCode.toLowerCase().includes(value.toLowerCase())
}

// 过滤库区树
const filterAreaTree = (value) => {
  areaTreeRef.value?.filter(value)
}

// 加载库区树形数据
const loadAreaTree = async () => {
  try {
    const response = await areaApi.getAreaTree()
    areaTreeData.value = response.data?.data || []
  } catch (error) {
    ElMessage.error('获取库区数据失败')
    console.error(error)
    areaTreeData.value = []
  }
}

// 加载设备列表
const getDeviceList = async () => {
  try {
    const params = {
      page: pagination.currentPage,
      size: pagination.pageSize,
      ...filterForm
    }

    // 格式化日期范围
    if (filterForm.lastSeenRange && Array.isArray(filterForm.lastSeenRange) && filterForm.lastSeenRange.length === 2) {
      params.lastSeenStart = filterForm.lastSeenRange[0]
      params.lastSeenEnd = filterForm.lastSeenRange[1]
    }

    // 移除无效的空值参数
    Object.keys(params).forEach(key => {
      if (params[key] === '' || params[key] === null || params[key] === undefined) {
        delete params[key]
      }
    })

    const response = await deviceApi.getList(params)
    deviceList.value = response.data?.data?.records || []
    pagination.total = response.data?.data?.total || 0
  } catch (error) {
    ElMessage.error('获取设备列表失败')
    console.error(error)
    deviceList.value = []
    pagination.total = 0
  }
}

// 搜索设备
const searchDevices = () => {
  pagination.currentPage = 1
  getDeviceList()
}

// 重置筛选
const resetFilters = () => {
  Object.keys(filterForm).forEach(key => {
    if (Array.isArray(filterForm[key])) {
      filterForm[key] = []
    } else {
      filterForm[key] = null
    }
  })
  filterForm.lastSeenRange = []
  searchDevices()
}

// 库区树节点点击事件
const onAreaNodeClick = async (data) => {
  filterForm.areaId = data.id
  searchDevices()
}

// 刷新数据
const refreshData = () => {
  loadAreaTree()
  getDeviceList()
}

// 处理表格选择变化
const handleSelectionChange = (val) => {
  selectedDevices.value = val
}

// 分页大小变化
const handleSizeChange = (size) => {
  pagination.pageSize = size
  pagination.currentPage = 1
  getDeviceList()
}

// 当前页变化
const handleCurrentChange = (page) => {
  pagination.currentPage = page
  getDeviceList()
}

// 时间格式化
const formatDate = (dateString) => {
  if (!dateString) return '-'
  const date = new Date(dateString)
  return date.toLocaleString('zh-CN')
}

// 显示新增设备对话框
const showAddDeviceDialog = () => {
  editingDevice.value = null
  Object.assign(deviceForm, {
    id: null,
    areaId: null,
    deviceCode: '',
    deviceName: '',
    deviceType: 'TEMP_HUM',
    model: '',
    locationDesc: '',
    enabled: true,
    thresholdMode: 'INHERIT',
    temperatureThresholdMin: null,
    temperatureThresholdMax: null,
    humidityThresholdMin: null,
    humidityThresholdMax: null,
    alarmEnabled: true
  })
  deviceDialogVisible.value = true
}

// 显示编辑设备对话框
const showEditDeviceDialog = (device) => {
  editingDevice.value = device
  Object.assign(deviceForm, { ...device })
  deviceDialogVisible.value = true
}

// 关闭设备对话框
const closeDeviceDialog = () => {
  deviceDialogVisible.value = false
}

// 保存设备
const saveDevice = async () => {
  saving.value = true
  try {
    if (editingDevice.value) {
      // 更新设备
      await deviceApi.update(deviceForm.id, deviceForm)
      ElMessage.success('设备更新成功')
    } else {
      // 新增设备
      await deviceApi.create(deviceForm)
      ElMessage.success('设备创建成功')
    }

    deviceDialogVisible.value = false
    getDeviceList()
  } catch (error) {
    ElMessage.error('操作失败: ' + (error.message || '未知错误'))
    console.error(error)
  } finally {
    saving.value = false
  }
}

// 显示设备详情
const showDeviceInfo = (device) => {
  currentDevice.value = device
  deviceDetailVisible.value = true
}

// 关闭设备详情
const closeDeviceDetail = () => {
  deviceDetailVisible.value = false
  currentDevice.value = null
}

// 查看设备数据
const viewDeviceData = (device) => {
  // 这里可以跳转到设备数据页面
  ElMessage.info('跳转到设备数据页面')
}

// 切换设备状态
const toggleDeviceStatus = async (device) => {
  try {
    await ElMessageBox.confirm(
      `确认${device.enabled ? '禁用' : '启用'}设备 "${device.deviceName}" 吗？`,
      '提示',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    const newStatus = !device.enabled
    await deviceApi.update(device.id, { ...device, enabled: newStatus })
    ElMessage.success(`${device.enabled ? '禁用' : '启用'}成功`)

    // 更新本地数据
    device.enabled = newStatus
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('操作失败')
    }
  }
}

// 删除设备
const deleteDevice = async (device) => {
  try {
    await ElMessageBox.confirm(
      `确认删除设备 "${device.deviceName}" 吗？`,
      '提示',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    await deviceApi.delete(device.id)
    ElMessage.success('删除成功')
    getDeviceList()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

// 复制设备
const duplicateDevice = (device) => {
  // 创建新设备，复制除ID和编码外的其他信息
  const newDevice = { ...device }
  newDevice.id = null
  newDevice.deviceCode = newDevice.deviceCode + '_copy'
  newDevice.deviceName = newDevice.deviceName + ' (副本)'

  editingDevice.value = null
  Object.assign(deviceForm, newDevice)
  deviceDialogVisible.value = true
}

// 跳转到库区
const goToArea = (areaId) => {
  // 这里可以跳转到库区详情页
  ElMessage.info('跳转到库区详情页')
}

// 批量导入
const batchImport = () => {
  ElMessage.info('批量导入功能正在开发中...')
}

// 导出设备
const exportDevices = () => {
  ElMessage.info('导出功能正在开发中...')
}

// Dashboard methods for top bar and menu
const viewProfile = () => {
  router.push('/profile')
}

const settings = () => {
  router.push('/settings')
}

const logout = () => {
  authStore.clearAuthData()
  router.push('/login')
}

onMounted(() => {
  loadAreaTree()
  getDeviceList()
})
</script>

<style scoped>
.device-management-container {
  position: fixed;
  top: 0;
  left: 0;
  width: 100vw;
  height: 100vh;
  display: flex;
  flex-direction: column;
  background-color: #f5f7fa;
  box-sizing: border-box;
  overflow: hidden; /* Hide all scrollbars except browser native */
}

.top-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  height: 60px;
  background-color: white;
  box-shadow: 0 1px 4px rgba(0, 21, 41, 0.08);
  z-index: 10;
  flex-shrink: 0;
}

.logo-section {
  display: flex;
  align-items: center;
}

.logo {
  margin-right: 12px;
}

.app-title {
  font-size: 18px;
  font-weight: bold;
  color: #303133;
}

.search-section {
  flex: 1;
  max-width: 400px;
  margin: 0 40px;
}

.global-search {
  width: 100%;
}

.action-section {
  display: flex;
  align-items: center;
  gap: 20px;
}

.notification-badge {
  margin-right: 20px;
}

.notification-btn {
  border: none;
}

.user-avatar {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
}

.user-name {
  font-size: 14px;
  color: #606266;
}

.device-management-layout {
  display: flex;
  flex: 1;
  overflow: hidden; /* Hide scrollbars in the layout */
  min-height: 0;
}

.side-menu {
  width: 200px;
  background-color: white;
  box-shadow: 2px 0 6px rgba(0, 21, 41, 0.35);
  overflow-y: auto; /* Allow sidebar to scroll independently if needed */
  flex-shrink: 0;
  height: calc(100vh - 60px); /* Account for the top bar height */
}

.menu {
  border-right: none;
}

.main-content {
  flex: 1;
  overflow-y: auto; /* Main content scrolls with browser native scrollbar */
  padding: 20px;
  min-height: 0;
  height: calc(100vh - 60px); /* Account for the top bar height */
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding-bottom: 10px;
  border-bottom: 1px solid #ebeef5;
}

.page-header h2 {
  margin: 0;
  font-size: 24px;
  font-weight: 600;
  color: #303133;
}

.content-wrapper {
  display: flex;
  gap: 20px;
  overflow: hidden;
  height: calc(100% - 80px); /* Adjust for header height */
}

.tree-panel {
  width: 350px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  display: flex;
  flex-direction: column;
  background: #fff;
  height: 100%;
}

.panel-header {
  padding: 12px 16px;
  border-bottom: 1px solid #ebeef5;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.panel-header h3 {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.device-list-panel {
  flex: 1;
  display: flex;
  flex-direction: column;
  background: #fff;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  overflow: hidden;
  padding: 20px;
}

.list-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  padding-bottom: 10px;
  border-bottom: 1px solid #ebeef5;
}

.list-header h3 {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.filter-form {
  margin-bottom: 16px;
  padding-bottom: 10px;
  border-bottom: 1px solid #ebeef5;
}

.filter-form :deep(.el-form-item) {
  margin-bottom: 12px;
  margin-right: 16px;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}

.custom-tree {
  flex: 1;
  overflow: auto;
}

.tree-node-content {
  flex: 1;
  display: flex;
  align-items: center;
  font-size: 14px;
  padding-right: 8px;
}

.node-label {
  margin-right: 8px;
  font-weight: 500;
  flex-shrink: 0;
}

.node-code {
  color: #909399;
  font-size: 12px;
  margin-right: 8px;
  flex-shrink: 0;
}

.level-tag {
  margin-right: 8px;
  font-size: 10px;
  height: 18px;
  padding: 0 6px;
  flex-shrink: 0;
}

.drawer-footer {
  margin-top: 20px;
  padding-top: 16px;
  border-top: 1px solid #ebeef5;
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

/* Table styles */
.table-device-code {
  display: flex;
  align-items: center;
  gap: 6px;
  font-weight: 500;
}

.table-device-code .el-icon {
  color: #409eff;
  font-size: 14px;
}

.temp-display, .humidity-display, .last-seen-display {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 4px;
}

.temp-display .el-icon, .humidity-display .el-icon, .last-seen-display .el-icon {
  font-size: 14px;
}

.temp-normal, .humidity-normal {
  color: #67c23a;
  font-weight: 500;
}

.temp-alert, .humidity-alert {
  color: #f56c6c;
  font-weight: 600;
  animation: pulse 1.5s infinite;
}

@keyframes pulse {
  0% { opacity: 1; }
  50% { opacity: 0.7; }
  100% { opacity: 1; }
}

.temp-na, .humidity-na {
  color: #909399;
  font-style: italic;
}

.unassigned {
  color: #e6a23c;
  font-style: italic;
}

.action-buttons {
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
  justify-content: center;
}

.action-buttons .el-button {
  min-width: unset;
  font-size: 12px;
  padding: 6px 8px;
  margin: 2px;
}

/* Responsive design */
@media (max-width: 1400px) {
  .content-wrapper {
    flex-direction: column;
  }

  .tree-panel {
    width: 100%;
    height: 400px;
  }

  .device-list-panel {
    width: 100%;
  }
}

@media (max-width: 1200px) {
  .table-device-code {
    flex-direction: column;
    align-items: flex-start;
    gap: 2px;
  }

  .action-buttons {
    flex-direction: column;
    align-items: center;
  }

  .action-buttons .el-button {
    width: 100%;
    margin: 2px 0;
  }
}

@media (max-width: 768px) {
  .top-bar {
    flex-direction: column;
    height: auto;
    padding: 12px;
  }

  .logo-section {
    margin-bottom: 12px;
  }

  .search-section {
    max-width: 100%;
    margin: 0 0 12px 0;
  }

  .action-section {
    justify-content: center;
  }

  .side-menu {
    width: 60px;
    height: calc(100vh - 60px);
  }

  .main-content {
    height: calc(100vh - 60px);
  }

  .content-wrapper {
    flex-direction: column;
  }

  .tree-panel {
    width: 100%;
    height: 300px;
  }

  .action-buttons {
    flex-direction: column;
    align-items: stretch;
  }

  .action-buttons .el-button {
    width: 100%;
    margin: 2px 0;
  }
}
</style>