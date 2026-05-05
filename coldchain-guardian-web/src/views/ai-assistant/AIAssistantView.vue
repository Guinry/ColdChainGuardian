<template>
  <Layout>
    <div class="ai-workspace" v-loading="contextLoading" element-loading-text="正在同步数据库上下文...">
      <aside class="session-rail">
        <div class="rail-head">
          <span class="eyebrow">AI 运维助手</span>
          <el-button type="primary" :icon="EditPen" @click="createNewChat">新建研判</el-button>
        </div>

        <div class="session-list">
          <button
            v-for="session in chatSessions"
            :key="session.id"
            type="button"
            class="session-item"
            :class="{ active: session.id === currentSessionId }"
            @mouseenter="hoveredSessionId = session.id"
            @mouseleave="hoveredSessionId = null"
            @click="switchSession(session.id)"
          >
            <span class="session-copy">
              <strong>{{ session.title || '未命名研判' }}</strong>
              <small>{{ formatDate(session.lastUpdated || session.updateTime || session.createTime) }}</small>
            </span>
            <span v-show="hoveredSessionId === session.id" class="session-actions">
              <el-icon @click.stop="renameSession(session.id)"><Edit /></el-icon>
              <el-icon @click.stop="deleteSession(session.id)"><Delete /></el-icon>
            </span>
          </button>
          <el-empty v-if="!chatSessions.length" description="暂无历史会话" :image-size="64" />
        </div>
      </aside>

      <main class="assistant-main">
        <header class="assistant-toolbar">
          <div>
            <h1>数据库智能研判</h1>
            <p>基于设备、告警、工单和遥测数据输出冷链运行分析</p>
          </div>
          <div class="toolbar-actions">
            <el-tag effect="plain" :type="riskTagType">{{ riskText }}</el-tag>
            <el-button :icon="Refresh" @click="loadDatabaseContext">刷新上下文</el-button>
          </div>
        </header>

        <section class="context-strip">
          <button
            v-for="item in contextOptions"
            :key="item.value"
            type="button"
            class="context-chip"
            :class="{ selected: attachedContext.includes(item.value) }"
            @click="toggleContext(item.value)"
          >
            <el-icon><component :is="item.icon" /></el-icon>
            <span>{{ item.label }}</span>
          </button>
        </section>

        <div ref="messagesContainer" class="messages-container">
          <div v-if="currentMessages.length === 0" class="empty-state">
            <div class="empty-copy">
              <span class="status-dot"></span>
              <h2>从数据库开始分析，而不是只聊天</h2>
              <p>右侧快照会同步当前设备、告警和工单；发送问题时会一并传给后端 AI 服务。</p>
            </div>
            <div class="prompt-grid">
              <button
                v-for="prompt in quickPrompts"
                :key="prompt.id"
                type="button"
                class="prompt-button"
                @click="sendPrompt(prompt.text, prompt.contextTypes)"
              >
                <span>{{ prompt.title }}</span>
                <small>{{ prompt.text }}</small>
              </button>
            </div>
          </div>

          <article
            v-for="(message, index) in currentMessages"
            :key="`${message.role}-${index}`"
            class="message-row"
            :class="message.role === 'user' ? 'from-user' : 'from-assistant'"
          >
            <div v-if="message.role === 'assistant'" class="assistant-avatar">
              <el-icon><Monitor /></el-icon>
            </div>
            <div class="message-panel">
              <div class="message-meta">
                <strong>{{ message.role === 'assistant' ? 'ColdChain AI' : '我' }}</strong>
                <span v-if="message.contextTypes?.length">已纳入 {{ message.contextTypes.length }} 类上下文</span>
              </div>

              <div v-if="message.role === 'assistant'">
                <div v-if="isThinking && !message.content && index === currentMessages.length - 1" class="typing-indicator">
                  <span></span>
                  <span></span>
                  <span></span>
                </div>
                <div v-else class="markdown-report" v-html="renderMarkdown(message.content)"></div>
                <span v-if="isThinking && index === currentMessages.length - 1" class="stream-cursor"></span>
              </div>
              <p v-else class="user-text">{{ message.content }}</p>

              <div v-if="message.role === 'assistant' && (!isThinking || index !== currentMessages.length - 1)" class="message-actions">
                <el-button link size="small" :icon="CopyDocument" @click="copyMessage(message.content)">复制</el-button>
                <el-button link size="small" :icon="Select" @click="likeMessage">有帮助</el-button>
                <el-button link size="small" :icon="CloseBold" @click="dislikeMessage">需改进</el-button>
              </div>
            </div>
          </article>
        </div>

        <footer class="composer">
          <div class="composer-context">
            <span>数据库上下文</span>
            <el-tag v-for="item in selectedContextLabels" :key="item" size="small" effect="plain">{{ item }}</el-tag>
          </div>
          <div class="composer-box">
            <el-input
              v-model="inputMessage"
              type="textarea"
              :autosize="{ minRows: 1, maxRows: 5 }"
              placeholder="例如：请基于数据库分析当前冷链运行风险，并给出优先处置顺序"
              :disabled="isThinking"
              @keydown.enter.exact.prevent="sendMessage"
            />
            <el-button
              v-if="!isThinking"
              type="primary"
              :icon="Top"
              circle
              :disabled="!inputMessage.trim()"
              @click="sendMessage"
            />
            <el-button v-else type="danger" :icon="VideoPause" circle @click="stopGeneration" />
          </div>
        </footer>
      </main>

      <aside class="data-inspector">
        <div class="inspector-head">
          <div>
            <span class="eyebrow">数据库快照</span>
            <h2>{{ lastUpdated || '待同步' }}</h2>
          </div>
          <el-button link type="primary" @click="loadDatabaseContext">同步</el-button>
        </div>

        <div class="metric-grid">
          <div v-for="metric in snapshotMetrics" :key="metric.label" class="metric-cell" :class="metric.tone">
            <span>{{ metric.label }}</span>
            <strong>{{ metric.value }}</strong>
            <small>{{ metric.note }}</small>
          </div>
        </div>

        <section class="inspector-section">
          <div class="section-title">
            <span>重点风险</span>
            <el-button link type="primary" @click="sendPrompt('请基于数据库快照列出当前 Top 5 冷链风险和处置顺序', ['device-info', 'alert-record', 'workorder-detail'])">让 AI 研判</el-button>
          </div>
          <div class="risk-list">
            <button v-for="risk in riskItems" :key="risk.key" type="button" @click="sendPrompt(risk.prompt, risk.contextTypes)">
              <span :class="`risk-dot ${risk.tone}`"></span>
              <span>
                <strong>{{ risk.title }}</strong>
                <small>{{ risk.detail }}</small>
              </span>
            </button>
            <el-empty v-if="!riskItems.length" description="暂无重点风险" :image-size="56" />
          </div>
        </section>

        <section class="inspector-section">
          <div class="section-title">
            <span>未处理告警</span>
            <small>{{ unhandledAlerts.length }} 条</small>
          </div>
          <ul class="compact-list">
            <li v-for="alert in unhandledAlerts.slice(0, 5)" :key="alert.id">
              <strong>{{ alert.title }}</strong>
              <span>{{ alert.location }} · {{ alert.levelText }}</span>
            </li>
          </ul>
        </section>

        <section class="inspector-section">
          <div class="section-title">
            <span>待办工单</span>
            <small>{{ pendingOrders.length }} 条</small>
          </div>
          <ul class="compact-list">
            <li v-for="order in pendingOrders.slice(0, 5)" :key="order.id || order.orderNo">
              <strong>{{ order.title }}</strong>
              <span>{{ order.assignee }} · {{ order.statusText }}</span>
            </li>
          </ul>
        </section>
      </aside>
    </div>
  </Layout>
</template>

<script setup>
import { computed, nextTick, onMounted, onUnmounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import MarkdownIt from 'markdown-it'
import Layout from '@/components/Layout.vue'
import { useAiAssistant } from '@/composables/useAiAssistant'
import { dashboardApi } from '@/api/dashboard'
import { deviceApi } from '@/api/device'
import { alertApi } from '@/api/alert'
import { workOrderApi } from '@/api/work-order'
import {
  Bell,
  CloseBold,
  CopyDocument,
  Delete,
  Edit,
  EditPen,
  Finished,
  Monitor,
  Refresh,
  Select,
  Tickets,
  Top,
  TrendCharts,
  VideoPause,
  WarningFilled
} from '@element-plus/icons-vue'

const md = new MarkdownIt({
  html: false,
  linkify: true,
  typographer: true,
  breaks: true
})

const {
  chatSessions,
  getChatHistory,
  createChatSession,
  deleteChatSession,
  streamMessage,
  getChatMessages
} = useAiAssistant()

const inputMessage = ref('')
const currentSessionId = ref(null)
const currentMessages = ref([])
const isThinking = ref(false)
const hoveredSessionId = ref(null)
const attachedContext = ref(['device-info', 'alert-record', 'workorder-detail'])
const messagesContainer = ref(null)
const contextLoading = ref(false)
const lastUpdated = ref('')

const kpi = ref({
  onlineDevices: 0,
  totalDevices: 0,
  todayAlerts: 0,
  unhandledAlerts: 0,
  pendingWorkOrders: 0,
  todayClosedWorkOrders: 0
})
const devices = ref([])
const recentAlerts = ref([])
const unhandledAlerts = ref([])
const pendingOrders = ref([])

let currentSSEController = null
let scrollObserver = null

const contextOptions = [
  { value: 'device-info', label: '设备状态', icon: Monitor },
  { value: 'alert-record', label: '告警记录', icon: Bell },
  { value: 'workorder-detail', label: '工单进度', icon: Tickets },
  { value: 'trend-data', label: '趋势遥测', icon: TrendCharts }
]

const quickPrompts = [
  {
    id: 1,
    title: '运行风险研判',
    text: '请基于数据库分析当前冷链运行风险，并按优先级给出处置顺序',
    contextTypes: ['device-info', 'alert-record', 'workorder-detail', 'trend-data']
  },
  {
    id: 2,
    title: '告警处置建议',
    text: '分析最近未处理告警，说明可能原因、影响范围和处理步骤',
    contextTypes: ['alert-record', 'device-info']
  },
  {
    id: 3,
    title: '工单催办清单',
    text: '整理当前待办工单，标出紧急项、责任人和建议完成时间',
    contextTypes: ['workorder-detail', 'alert-record']
  },
  {
    id: 4,
    title: '毕业答辩汇报',
    text: '生成一段适合毕业设计答辩展示的系统运行数据说明',
    contextTypes: ['device-info', 'alert-record', 'workorder-detail']
  }
]

const selectedContextLabels = computed(() => {
  const map = new Map(contextOptions.map(item => [item.value, item.label]))
  return attachedContext.value.map(value => map.get(value)).filter(Boolean)
})

const snapshotMetrics = computed(() => {
  const total = Number(kpi.value.totalDevices || devices.value.length || 0)
  const online = Number(kpi.value.onlineDevices || devices.value.filter(device => isOnline(device)).length || 0)
  const offline = Math.max(0, total - online)
  return [
    {
      label: '设备在线',
      value: `${online}/${total}`,
      note: `离线 ${offline} 台`,
      tone: offline ? 'warning' : 'success'
    },
    {
      label: '未处理告警',
      value: kpi.value.unhandledAlerts || unhandledAlerts.value.length,
      note: '待确认/派工',
      tone: (kpi.value.unhandledAlerts || unhandledAlerts.value.length) ? 'danger' : 'success'
    },
    {
      label: '待办工单',
      value: kpi.value.pendingWorkOrders || pendingOrders.value.length,
      note: '待处理/处理中',
      tone: (kpi.value.pendingWorkOrders || pendingOrders.value.length) ? 'warning' : 'success'
    },
    {
      label: '今日闭环',
      value: kpi.value.todayClosedWorkOrders || 0,
      note: `今日告警 ${kpi.value.todayAlerts || 0}`,
      tone: 'primary'
    }
  ]
})

const riskItems = computed(() => {
  const items = []
  const offlineDevices = devices.value.filter(device => !isOnline(device))
  const alarmingDevices = devices.value.filter(device => device.hasUnresolvedAlert || device.alarming)
  const criticalAlerts = unhandledAlerts.value.filter(alert => ['CRITICAL', 'HIGH', '紧急', '高'].includes(String(alert.level || alert.alertLevel || alert.levelText || '').toUpperCase()))
  const urgentOrders = pendingOrders.value.filter(order => ['URGENT', 'HIGH'].includes(order.priority))

  if (criticalAlerts.length) {
    items.push({
      key: 'critical-alerts',
      title: `${criticalAlerts.length} 条高优先级告警`,
      detail: criticalAlerts[0]?.title || '需要优先处置',
      tone: 'danger',
      prompt: '请分析当前高优先级未处理告警，给出处置顺序和责任建议',
      contextTypes: ['alert-record', 'device-info', 'workorder-detail']
    })
  }
  if (offlineDevices.length) {
    items.push({
      key: 'offline-devices',
      title: `${offlineDevices.length} 台设备离线`,
      detail: offlineDevices.slice(0, 2).map(device => device.deviceName || device.name || device.deviceCode).join('、'),
      tone: 'warning',
      prompt: '请分析离线设备风险，按供电、网络、网关和设备故障给出排查清单',
      contextTypes: ['device-info', 'alert-record']
    })
  }
  if (alarmingDevices.length) {
    items.push({
      key: 'alarming-devices',
      title: `${alarmingDevices.length} 台设备存在未解除风险`,
      detail: alarmingDevices.slice(0, 2).map(device => device.deviceName || device.name || device.deviceCode).join('、'),
      tone: 'danger',
      prompt: '请列出存在未解除风险的设备，说明温湿度与库区影响',
      contextTypes: ['device-info', 'trend-data', 'alert-record']
    })
  }
  if (urgentOrders.length) {
    items.push({
      key: 'urgent-orders',
      title: `${urgentOrders.length} 个高优先级工单`,
      detail: urgentOrders[0]?.title || '需要跟进闭环',
      tone: 'warning',
      prompt: '请梳理高优先级工单，给出催办话术和闭环检查项',
      contextTypes: ['workorder-detail', 'alert-record']
    })
  }
  return items.slice(0, 4)
})

const riskText = computed(() => {
  const unhandled = Number(kpi.value.unhandledAlerts || unhandledAlerts.value.length || 0)
  const pending = Number(kpi.value.pendingWorkOrders || pendingOrders.value.length || 0)
  const offline = devices.value.filter(device => !isOnline(device)).length
  if (unhandled || offline) return '存在待处置风险'
  if (pending) return '工单待跟进'
  return '运行平稳'
})

const riskTagType = computed(() => riskText.value === '运行平稳' ? 'success' : riskText.value === '工单待跟进' ? 'warning' : 'danger')

const unwrapData = (response, fallback = null) => {
  const payload = response?.data
  if (!payload) return fallback
  if (payload.code === 200 || payload.success) return payload.data
  return payload.data ?? payload ?? fallback
}

const unwrapPage = (response) => {
  const payload = unwrapData(response, {})
  const records = payload.records || payload.data || payload.list || []
  return {
    records: Array.isArray(records) ? records : [],
    total: Number(payload.total || records.length || 0)
  }
}

const setupScrollObserver = () => {
  if (!messagesContainer.value) return

  scrollObserver = new MutationObserver(() => {
    const container = messagesContainer.value
    const isNearBottom = container.scrollHeight - container.scrollTop - container.clientHeight < 180
    if (isNearBottom || isThinking.value) {
      container.scrollTop = container.scrollHeight
    }
  })

  scrollObserver.observe(messagesContainer.value, {
    childList: true,
    subtree: true,
    characterData: true
  })
}

const loadDatabaseContext = async () => {
  contextLoading.value = true
  try {
    const [statsRes, devicesRes, alertsRes, unhandledRes, ordersRes] = await Promise.allSettled([
      dashboardApi.getStats(),
      deviceApi.getList({ page: 1, size: 200, pageNum: 1, pageSize: 200 }),
      alertApi.search({ page: 1, size: 8, pageNum: 1, pageSize: 8 }),
      alertApi.search({ status: 'UNHANDLED', page: 1, size: 8, pageNum: 1, pageSize: 8 }),
      workOrderApi.getList({ status: 'PENDING', page: 1, size: 8, pageNum: 1, pageSize: 8 })
    ])

    if (statsRes.status === 'fulfilled') {
      kpi.value = { ...kpi.value, ...(unwrapData(statsRes.value, {}) || {}) }
    }
    if (devicesRes.status === 'fulfilled') {
      devices.value = unwrapPage(devicesRes.value).records
    }
    if (alertsRes.status === 'fulfilled') {
      recentAlerts.value = unwrapPage(alertsRes.value).records.map(adaptAlert)
    }
    if (unhandledRes.status === 'fulfilled') {
      const page = unwrapPage(unhandledRes.value)
      unhandledAlerts.value = page.records.map(adaptAlert)
      if (!kpi.value.unhandledAlerts) kpi.value.unhandledAlerts = page.total
    }
    if (ordersRes.status === 'fulfilled') {
      const page = unwrapPage(ordersRes.value)
      pendingOrders.value = page.records.map(adaptOrder)
      if (!kpi.value.pendingWorkOrders) kpi.value.pendingWorkOrders = page.total
    }

    lastUpdated.value = new Date().toLocaleString('zh-CN', { hour12: false })
  } catch (error) {
    console.error('数据库上下文加载失败:', error)
    ElMessage.error('数据库上下文加载失败，请检查后端服务')
  } finally {
    contextLoading.value = false
  }
}

const sendMessage = async () => {
  if (!inputMessage.value.trim() || isThinking.value) return

  const userText = inputMessage.value.trim()
  const contextTypes = [...attachedContext.value]
  currentMessages.value.push({ role: 'user', content: userText, contextTypes })
  inputMessage.value = ''
  isThinking.value = true

  try {
    currentMessages.value.push({ role: 'assistant', content: '', contextTypes })

    if (!currentSessionId.value) {
      const newSession = await createChatSession({
        title: userText.length > 18 ? `${userText.substring(0, 18)}...` : userText,
        userId: 1,
        isDeleted: 0
      })
      currentSessionId.value = newSession.id
    }

    let fullResponse = ''
    const targetMessage = currentMessages.value[currentMessages.value.length - 1]

    currentSSEController = streamMessage(
      userText,
      null,
      null,
      currentSessionId.value,
      contextTypes,
      (token) => {
        const chunkText = typeof token === 'string' ? token : (token.content || '')
        if (chunkText === '[DONE]') return
        fullResponse += chunkText
        targetMessage.content = fullResponse
      },
      (error) => {
        console.error('SSE error:', error)
        if (fullResponse === '') {
          targetMessage.content = '### 结论\nAI 服务连接失败，请检查后端服务与模型环境变量。\n\n### 后续跟踪\n可先刷新数据库上下文，再重新发起研判。'
        }
        isThinking.value = false
      },
      () => {
        isThinking.value = false
        currentSSEController = null
        getChatHistory().catch(() => {})
      }
    )
  } catch (error) {
    console.error('发送失败:', error)
    isThinking.value = false
    ElMessage.error('发送失败，请检查服务连接')
  }
}

const stopGeneration = () => {
  if (currentSSEController) {
    currentSSEController.abort()
    currentSSEController = null
  }
  isThinking.value = false
}

const createNewChat = () => {
  if (isThinking.value) return
  currentSessionId.value = null
  currentMessages.value = []
}

const switchSession = async (sessionId) => {
  if (isThinking.value) return
  currentSessionId.value = sessionId
  try {
    const messages = await getChatMessages(sessionId)
    currentMessages.value = messages.map(msg => ({
      role: String(msg.role || '').toLowerCase(),
      content: msg.content || '',
      contextTypes: msg.contextTypes || []
    }))
    await nextTick()
    if (messagesContainer.value) {
      messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
    }
  } catch (error) {
    console.error('切换会话失败:', error)
    currentMessages.value = []
  }
}

const renameSession = (sessionId) => {
  const session = chatSessions.value.find(item => item.id === sessionId)
  ElMessageBox.prompt('请输入新的会话标题', '重命名', {
    inputValue: session?.title || ''
  }).then(({ value }) => {
    if (session && value?.trim()) session.title = value.trim()
  }).catch(() => {})
}

const deleteSession = async (sessionId) => {
  try {
    await deleteChatSession(sessionId)
    if (currentSessionId.value === sessionId) {
      currentSessionId.value = null
      currentMessages.value = []
    }
  } catch (error) {
    console.error('删除会话失败:', error)
  }
}

const sendPrompt = (prompt, contextTypes = attachedContext.value) => {
  attachedContext.value = Array.from(new Set(contextTypes))
  inputMessage.value = prompt
  sendMessage()
}

const toggleContext = (value) => {
  if (attachedContext.value.includes(value)) {
    attachedContext.value = attachedContext.value.filter(item => item !== value)
    return
  }
  attachedContext.value = [...attachedContext.value, value]
}

const renderMarkdown = (text) => {
  if (!text) return ''
  return md.render(text)
}

const copyMessage = (content) => {
  navigator.clipboard.writeText(content || '').then(() => ElMessage.success('已复制'))
}
const likeMessage = () => ElMessage.success('感谢反馈')
const dislikeMessage = () => ElMessage.info('已记录反馈')

const adaptAlert = (alert) => {
  const levelValue = alert.alertLevel || alert.level || alert.severityLevel
  const status = alert.status || (alert.resolved ? 'RESOLVED' : 'UNHANDLED')
  return {
    ...alert,
    id: alert.id,
    title: alert.message || alert.description || alert.alertType || `告警 #${alert.id}`,
    location: alert.deviceName || alert.areaName || alert.location || alert.sourceCode || '未知位置',
    level: levelValue,
    levelText: getAlertLevelText(levelValue),
    statusText: getAlertStatusText(status)
  }
}

const adaptOrder = (order) => {
  const status = order.status || 'PENDING'
  return {
    ...order,
    id: order.id || order.orderId || order.orderNo,
    title: order.title || order.description || order.orderNo || `工单 #${order.id || order.orderId}`,
    assignee: order.assigneeName || order.assignee || (order.assigneeId ? `用户 ${order.assigneeId}` : '未分配'),
    status,
    statusText: getOrderStatusText(status),
    priority: order.priority || 'MEDIUM'
  }
}

const isOnline = (device) => {
  if (typeof device.online === 'boolean') return device.online
  return Number(device.onlineStatus ?? device.online_status ?? 0) === 1
}

const formatDate = (value) => {
  if (!value) return '刚刚'
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return '刚刚'
  const diff = Date.now() - date.getTime()
  if (diff >= 0 && diff < 60 * 1000) return '刚刚'
  if (diff >= 0 && diff < 60 * 60 * 1000) return `${Math.floor(diff / 60000)}分钟前`
  if (diff >= 0 && diff < 24 * 60 * 60 * 1000) return `${Math.floor(diff / 3600000)}小时前`
  return date.toLocaleDateString('zh-CN')
}

const getAlertLevelText = (level) => {
  const normalized = normalizeLevel(level)
  const map = { CRITICAL: '紧急', HIGH: '高', MEDIUM: '中', LOW: '低' }
  return map[normalized] || '未知'
}

const normalizeLevel = (level) => {
  if (typeof level === 'number') {
    if (level >= 4) return 'CRITICAL'
    if (level === 3) return 'HIGH'
    if (level === 2) return 'MEDIUM'
    return 'LOW'
  }
  return String(level || '').toUpperCase()
}

const getAlertStatusText = (status) => {
  const map = {
    UNHANDLED: '未处理',
    HANDLING: '处理中',
    RESOLVED: '已解决',
    IGNORED: '已忽略'
  }
  return map[status] || status || '未知'
}

const getOrderStatusText = (status) => {
  const map = {
    PENDING: '待处理',
    PROCESSING: '处理中',
    VERIFYING: '待验收',
    COMPLETED: '已完成',
    CLOSED: '已关闭'
  }
  return map[status] || status || '未知'
}

onMounted(async () => {
  await Promise.allSettled([getChatHistory(), loadDatabaseContext()])
  setupScrollObserver()
})

onUnmounted(() => {
  if (scrollObserver) scrollObserver.disconnect()
  if (currentSSEController) currentSSEController.abort()
})
</script>

<style scoped>
.ai-workspace {
  display: grid;
  grid-template-columns: 280px minmax(0, 1fr) 340px;
  height: calc(100vh - var(--ccg-header-height));
  min-height: 0;
  background: #f6f8fb;
  color: #1f2937;
}

.session-rail,
.data-inspector {
  min-height: 0;
  background: #fff;
  border-color: #e5e7eb;
}

.session-rail {
  display: flex;
  flex-direction: column;
  border-right: 1px solid #e5e7eb;
}

.rail-head,
.inspector-head {
  padding: 18px;
  border-bottom: 1px solid #eef1f5;
}

.rail-head {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.eyebrow {
  display: block;
  color: #64748b;
  font-size: 12px;
  font-weight: 700;
}

.session-list {
  flex: 1;
  min-height: 0;
  overflow: auto;
  padding: 10px;
}

.session-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
  width: 100%;
  min-height: 58px;
  margin-bottom: 6px;
  padding: 10px;
  border: 1px solid transparent;
  border-radius: 8px;
  background: transparent;
  color: #1f2937;
  text-align: left;
  cursor: pointer;
}

.session-item:hover,
.session-item.active {
  border-color: #bfdbfe;
  background: #eff6ff;
}

.session-copy {
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 5px;
}

.session-copy strong,
.compact-list strong,
.risk-list strong {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.session-copy strong {
  max-width: 190px;
  font-size: 14px;
}

.session-copy small,
.compact-list span,
.risk-list small,
.metric-cell small {
  color: #64748b;
  font-size: 12px;
}

.session-actions {
  display: flex;
  gap: 6px;
  color: #64748b;
}

.assistant-main {
  display: flex;
  flex-direction: column;
  min-width: 0;
  min-height: 0;
}

.assistant-toolbar {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
  padding: 20px 24px 14px;
  background: #f6f8fb;
}

.assistant-toolbar h1,
.inspector-head h2 {
  margin: 0;
  font-size: 22px;
  line-height: 1.2;
}

.assistant-toolbar p {
  margin: 5px 0 0;
  color: #64748b;
  font-size: 13px;
}

.toolbar-actions {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-shrink: 0;
}

.context-strip {
  display: flex;
  gap: 10px;
  padding: 0 24px 14px;
  overflow-x: auto;
}

.context-chip {
  display: inline-flex;
  align-items: center;
  gap: 7px;
  height: 34px;
  padding: 0 12px;
  border: 1px solid #dbe3ef;
  border-radius: 8px;
  background: #fff;
  color: #475569;
  cursor: pointer;
  white-space: nowrap;
}

.context-chip.selected {
  border-color: #2563eb;
  background: #eff6ff;
  color: #1d4ed8;
}

.messages-container {
  flex: 1;
  min-height: 0;
  overflow-y: auto;
  padding: 6px 24px 22px;
}

.empty-state {
  max-width: 820px;
  margin: 52px auto 0;
}

.empty-copy {
  padding-bottom: 22px;
  border-bottom: 1px solid #e5e7eb;
}

.empty-copy h2 {
  margin: 10px 0 8px;
  font-size: 26px;
}

.empty-copy p {
  margin: 0;
  color: #64748b;
}

.status-dot {
  display: inline-block;
  width: 10px;
  height: 10px;
  border-radius: 50%;
  background: #10b981;
  box-shadow: 0 0 0 6px rgba(16, 185, 129, 0.12);
}

.prompt-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
  margin-top: 18px;
}

.prompt-button {
  min-height: 92px;
  padding: 14px;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  background: #fff;
  text-align: left;
  cursor: pointer;
}

.prompt-button:hover {
  border-color: #93c5fd;
  box-shadow: 0 10px 22px rgba(15, 23, 42, 0.07);
}

.prompt-button span,
.section-title span {
  display: block;
  font-weight: 700;
}

.prompt-button small {
  display: block;
  margin-top: 8px;
  color: #64748b;
  line-height: 1.5;
}

.message-row {
  display: flex;
  gap: 12px;
  margin: 0 auto 18px;
  max-width: 980px;
}

.message-row.from-user {
  justify-content: flex-end;
}

.assistant-avatar {
  width: 36px;
  height: 36px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border: 1px solid #dbeafe;
  border-radius: 8px;
  background: #eff6ff;
  color: #2563eb;
  flex-shrink: 0;
}

.message-panel {
  min-width: 0;
  max-width: 880px;
  padding: 14px 16px;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  background: #fff;
  box-shadow: 0 1px 2px rgba(15, 23, 42, 0.03);
}

.from-user .message-panel {
  max-width: min(680px, 78%);
  border-color: #bfdbfe;
  background: #eff6ff;
}

.message-meta {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
  color: #64748b;
  font-size: 12px;
}

.message-meta strong {
  color: #334155;
  font-size: 13px;
}

.user-text {
  margin: 0;
  white-space: pre-wrap;
  word-break: break-word;
  line-height: 1.7;
}

.markdown-report {
  color: #1f2937;
  line-height: 1.75;
  word-break: break-word;
}

.markdown-report :deep(h3) {
  margin: 16px 0 8px;
  padding-left: 10px;
  border-left: 3px solid #2563eb;
  font-size: 16px;
  line-height: 1.35;
}

.markdown-report :deep(h3:first-child) {
  margin-top: 0;
}

.markdown-report :deep(p) {
  margin: 0 0 10px;
}

.markdown-report :deep(ul),
.markdown-report :deep(ol) {
  margin: 8px 0 12px;
  padding-left: 22px;
}

.markdown-report :deep(li) {
  margin: 5px 0;
}

.markdown-report :deep(code) {
  padding: 2px 5px;
  border-radius: 5px;
  background: #f1f5f9;
  color: #be123c;
  font-family: Consolas, Monaco, monospace;
  font-size: 13px;
}

.markdown-report :deep(pre) {
  overflow-x: auto;
  margin: 12px 0;
  padding: 12px;
  border-radius: 8px;
  background: #0f172a;
  color: #e5e7eb;
}

.markdown-report :deep(pre code) {
  padding: 0;
  background: transparent;
  color: inherit;
}

.markdown-report :deep(table) {
  width: 100%;
  margin: 12px 0;
  border-collapse: collapse;
  font-size: 14px;
}

.markdown-report :deep(th),
.markdown-report :deep(td) {
  padding: 8px 10px;
  border: 1px solid #e2e8f0;
  text-align: left;
}

.markdown-report :deep(th) {
  background: #f8fafc;
}

.message-actions {
  display: flex;
  gap: 4px;
  margin-top: 8px;
}

.typing-indicator {
  display: flex;
  gap: 5px;
  padding: 6px 0;
}

.typing-indicator span {
  width: 7px;
  height: 7px;
  border-radius: 50%;
  background: #94a3b8;
  animation: pulse 1.2s infinite ease-in-out;
}

.typing-indicator span:nth-child(2) {
  animation-delay: 0.14s;
}

.typing-indicator span:nth-child(3) {
  animation-delay: 0.28s;
}

@keyframes pulse {
  0%, 80%, 100% { opacity: 0.35; transform: translateY(0); }
  40% { opacity: 1; transform: translateY(-3px); }
}

.stream-cursor {
  display: inline-block;
  width: 8px;
  height: 18px;
  margin-left: 4px;
  background: #2563eb;
  vertical-align: middle;
  animation: blink 1s step-end infinite;
}

@keyframes blink {
  50% { opacity: 0; }
}

.composer {
  padding: 12px 24px 18px;
  background: linear-gradient(to top, #f6f8fb 82%, rgba(246, 248, 251, 0));
}

.composer-context {
  display: flex;
  align-items: center;
  gap: 8px;
  margin: 0 auto 8px;
  max-width: 980px;
  color: #64748b;
  font-size: 12px;
}

.composer-box {
  display: flex;
  align-items: flex-end;
  gap: 10px;
  max-width: 980px;
  margin: 0 auto;
  padding: 8px;
  border: 1px solid #dbe3ef;
  border-radius: 8px;
  background: #fff;
}

.composer-box :deep(.el-textarea__inner) {
  min-height: 42px !important;
  border: 0;
  box-shadow: none;
  resize: none;
}

.data-inspector {
  min-width: 0;
  overflow-y: auto;
  border-left: 1px solid #e5e7eb;
}

.inspector-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 10px;
}

.inspector-head h2 {
  margin-top: 5px;
  font-size: 15px;
}

.metric-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 10px;
  padding: 14px;
}

.metric-cell {
  min-height: 92px;
  padding: 12px;
  border: 1px solid #e5e7eb;
  border-left-width: 4px;
  border-radius: 8px;
  background: #fff;
}

.metric-cell span,
.metric-cell small {
  display: block;
}

.metric-cell strong {
  display: block;
  margin: 8px 0 4px;
  font-size: 24px;
}

.metric-cell.danger { border-left-color: #ef4444; }
.metric-cell.warning { border-left-color: #f59e0b; }
.metric-cell.success { border-left-color: #10b981; }
.metric-cell.primary { border-left-color: #2563eb; }

.inspector-section {
  padding: 14px;
  border-top: 1px solid #eef1f5;
}

.section-title {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
  margin-bottom: 10px;
}

.section-title small {
  color: #64748b;
}

.risk-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.risk-list button {
  display: grid;
  grid-template-columns: auto minmax(0, 1fr);
  gap: 9px;
  width: 100%;
  padding: 10px;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  background: #fbfdff;
  text-align: left;
  cursor: pointer;
}

.risk-list button:hover {
  border-color: #93c5fd;
  background: #f8fbff;
}

.risk-list span:last-child {
  min-width: 0;
}

.risk-list small,
.compact-list span {
  display: block;
  margin-top: 4px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.risk-dot {
  width: 9px;
  height: 9px;
  margin-top: 5px;
  border-radius: 50%;
  background: #2563eb;
}

.risk-dot.danger { background: #ef4444; }
.risk-dot.warning { background: #f59e0b; }

.compact-list {
  display: flex;
  flex-direction: column;
  gap: 9px;
  margin: 0;
  padding: 0;
  list-style: none;
}

.compact-list li {
  min-width: 0;
  padding-bottom: 9px;
  border-bottom: 1px solid #f1f5f9;
}

.compact-list li:last-child {
  border-bottom: 0;
}

.compact-list strong {
  display: block;
  font-size: 13px;
}

@media (max-width: 1280px) {
  .ai-workspace {
    grid-template-columns: 240px minmax(0, 1fr);
  }

  .data-inspector {
    display: none;
  }
}

@media (max-width: 900px) {
  .ai-workspace {
    grid-template-columns: 1fr;
  }

  .session-rail {
    display: none;
  }

  .assistant-toolbar,
  .toolbar-actions {
    flex-direction: column;
    align-items: stretch;
  }

  .prompt-grid {
    grid-template-columns: 1fr;
  }

  .from-user .message-panel {
    max-width: 92%;
  }
}
</style>
