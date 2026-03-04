<template>
  <Layout>
    <div class="ai-assistant-container">
      <!-- 左侧历史会话栏 -->
      <div class="history-sidebar">
        <div class="sidebar-header">
          <el-button type="primary" icon="Plus" @click="createNewChat">
            + 新建洞察
          </el-button>
        </div>

        <div class="chat-history-list">
          <div
            v-for="session in chatSessions"
            :key="session.id"
            class="chat-item"
            :class="{ active: session.id === currentSessionId }"
            @mouseenter="hoveredSessionId = session.id"
            @mouseleave="hoveredSessionId = null"
            @click="switchSession(session.id)"
          >
            <div class="chat-info">
              <div class="chat-title">{{ session.title }}</div>
              <div class="chat-time">{{ formatDate(session.lastUpdated) }}</div>
            </div>
            <div class="chat-actions" v-show="hoveredSessionId === session.id">
              <el-icon @click.stop="renameSession(session.id)" class="action-icon"><Edit /></el-icon>
              <el-icon @click.stop="deleteSession(session.id)" class="action-icon"><Delete /></el-icon>
            </div>
          </div>
        </div>
      </div>

      <!-- 右侧核心对话区 -->
      <div class="chat-main-area">
        <!-- 消息列表 -->
        <div class="messages-container" ref="messagesContainer">
          <!-- 空状态 -->
          <div v-if="currentMessages.length === 0" class="empty-state">
            <div class="welcome-message">
              <h2>您好，我是冷链守护 AI</h2>
              <p>我可以帮您分析告警、排查设备故障，或生成运维报告。</p>
            </div>

            <!-- 快捷指令卡片 -->
            <div class="prompt-suggestions">
              <el-card
                v-for="prompt in quickPrompts"
                :key="prompt.id"
                class="prompt-card"
                @click="sendPrompt(prompt.text)"
              >
                <el-icon><Lightning /></el-icon>
                {{ prompt.text }}
              </el-card>
            </div>
          </div>

          <!-- 消息列表 -->
          <div
            v-for="(message, index) in currentMessages"
            :key="index"
            class="message-wrapper"
            :class="{ 'user-message': message.role === 'user', 'assistant-message': message.role === 'assistant' }"
          >
            <!-- AI消息带头像 -->
            <div v-if="message.role === 'assistant'" class="assistant-content">
              <div class="avatar">
                <el-avatar :size="32" icon="ChatLineRound" />
              </div>
              <div class="message-bubble assistant-bubble">
                <!-- AI回复内容 -->
                <div class="message-text" v-html="renderMarkdown(message.content)" />

                <!-- 结构化卡片渲染 -->
                <div v-if="message.cards && message.cards.length > 0" class="structured-cards">
                  <component
                    v-for="(card, cardIndex) in message.cards"
                    :key="cardIndex"
                    :is="getCardComponent(card.type)"
                    :data="card.data"
                  />
                </div>

                <!-- 思考状态 -->
                <div v-if="message.thinking" class="thinking-state">
                  <el-icon><Loading /></el-icon>
                  正在{{ message.thinkingText }}...
                </div>

                <!-- 操作按钮 -->
                <div class="message-actions">
                  <el-button size="small" icon="DocumentCopy" @click="copyMessage(message.content)">复制</el-button>
                  <el-button size="small" icon="ThumbUp" @click="likeMessage(message)">赞</el-button>
                  <el-button size="small" icon="ThumbDown" @click="dislikeMessage(message)">踩</el-button>
                </div>
              </div>
            </div>

            <!-- 用户消息 -->
            <div v-else class="user-content">
              <div class="message-bubble user-bubble">
                {{ message.content }}
              </div>
            </div>
          </div>

          <!-- AI正在思考状态 -->
          <div v-if="isThinking" class="assistant-content">
            <div class="avatar">
              <el-avatar :size="32" icon="ChatLineRound" />
            </div>
            <div class="message-bubble assistant-bubble thinking-bubble">
              <el-icon><Loading /></el-icon>
              {{ thinkingText }}
            </div>
          </div>
        </div>

        <!-- 底部输入区 -->
        <div class="input-area">
          <div class="context-tools">
            <el-popover placement="top" trigger="click">
              <template #reference>
                <el-button icon="Plus" circle />
              </template>
              <div class="context-menu">
                <el-checkbox-group v-model="attachedContext">
                  <el-checkbox value="device-info">设备信息</el-checkbox>
                  <el-checkbox value="alert-record">告警记录</el-checkbox>
                  <el-checkbox value="workorder-detail">工单详情</el-checkbox>
                  <el-checkbox value="trend-data">趋势数据</el-checkbox>
                </el-checkbox-group>
              </div>
            </el-popover>
          </div>

          <div class="input-wrapper">
            <el-input
              v-model="inputMessage"
              type="textarea"
              :autosize="{ minRows: 1, maxRows: 5 }"
              placeholder="输入您的问题，按 Enter 发送，Shift + Enter 换行..."
              @keydown.enter.exact.prevent="sendMessage"
              @keydown.shift.enter.native="insertNewline"
            />
          </div>

          <div class="send-tools">
            <el-button
              :icon="isThinking ? 'Close' : 'Right'"
              :loading="isThinking"
              @click="isThinking ? stopGeneration() : sendMessage()"
            />
          </div>
        </div>
      </div>
    </div>
  </Layout>
</template>

<script setup>
import { ref, onMounted, nextTick } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useAiAssistant } from '@/composables/useAiAssistant'
import MarkdownIt from 'markdown-it'
import Layout from '@/components/Layout.vue'
import {
  Edit,
  Delete,
  Lightning,
  Loading
} from '@element-plus/icons-vue'

// 引入卡片组件
import AlertAnalysisCard from './components/AlertAnalysisCard.vue'
import MiniChartCard from './components/MiniChartCard.vue'
import DataTableCard from './components/DataTableCard.vue'

// 初始化Markdown解析器
const md = new MarkdownIt({
  html: true,
  linkify: true,
  typographer: true
})

// 使用AI助手组合式函数
const {
  chatSessions,
  sendMessage: sendAiMessage,
  getChatHistory,
  createChatSession,
  deleteChatSession
} = useAiAssistant()

// 响应式数据
const inputMessage = ref('')
const currentSessionId = ref(null)
const currentMessages = ref([])
const isThinking = ref(false)
const thinkingText = ref('正在连接冷链知识库...')
const hoveredSessionId = ref(null)
const attachedContext = ref([])
const messagesContainer = ref(null)

// 快捷提示
const quickPrompts = [
  { id: 1, text: '分析最近 24 小时未处理的紧急告警' },
  { id: 2, text: '生成本周冷库温湿度波动总结' },
  { id: 3, text: '设备离线通常有哪些原因？如何排查？' },
  { id: 4, text: '帮我催办所有已逾期的运维工单' }
]

// 滚动到底部
const scrollToBottom = () => {
  nextTick(() => {
    if (messagesContainer.value) {
      messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
    }
  })
}

// 发送消息
const sendMessage = async () => {
  if (!inputMessage.value.trim()) return

  const userMessage = {
    role: 'user',
    content: inputMessage.value.trim()
  }

  // 添加用户消息到当前会话
  currentMessages.value.push(userMessage)

  // 清空输入框
  const tempMessage = inputMessage.value
  inputMessage.value = ''

  // 显示AI正在思考
  isThinking.value = true
  thinkingText.value = '正在分析您提供的信息...'

  try {
    // 使用普通API调用（暂时不用流式API，因为我们正在模拟后端）
    await new Promise(resolve => setTimeout(resolve, 1500)); // 模拟网络延迟

    // 根据用户输入生成模拟响应
    let responseContent = '';
    let cards = [];

    if (tempMessage.includes('告警')) {
      responseContent = `根据您的要求，我分析了最近的告警数据。\n\n**主要发现：**\n1. 发现3条紧急告警未处理\n2. 主要问题是温湿度超标\n3. 建议立即安排人员检查\n\n**处理建议：**\n- 检查相关传感器\n- 确认设备运行状态\n- 如需要，创建工单处理`;

      cards.push({
        type: 'alert-analysis',
        data: {
          rootCause: '温度传感器异常',
          severity: 'HIGH',
          suggestion: '更换传感器并检查线路'
        }
      });
    } else if (tempMessage.includes('温度') || tempMessage.includes('趋势')) {
      responseContent = `根据温湿度监测数据显示：\n\n- 平均温度：2°C\n- 波动范围：1-4°C\n- 最高温度：4°C（14:30）\n- 最低温度：1°C（02:15）\n\n**结论：**温度控制良好，符合冷链要求。`;

      cards.push({
        type: 'mini-chart',
        data: {
          title: '温湿度趋势',
          data: [2, 3, 1, 4, 2, 3, 2]
        }
      });
    } else if (tempMessage.includes('工单') || tempMessage.includes('催办')) {
      responseContent = `根据系统数据，共有3个逾期工单：\n\n1. 工单#WO-20260304-001（逾期2天）\n2. 工单#WO-20260303-005（逾期1天）\n3. 工单#WO-20260302-012（逾期3天）\n\n建议优先处理最逾期的工单。`;

      cards.push({
        type: 'data-table',
        data: {
          title: '逾期工单列表',
          columns: [
            { prop: 'id', label: '工单号' },
            { prop: 'title', label: '标题' },
            { prop: 'daysOverdue', label: '逾期天数' },
            { prop: 'assignee', label: '负责人' }
          ],
          rows: [
            { id: 'WO-20260302-012', title: '1号冷库制冷故障', daysOverdue: 3, assignee: '张三' },
            { id: 'WO-20260304-001', title: '温度传感器校准', daysOverdue: 2, assignee: '李四' },
            { id: 'WO-20260303-005', title: '门封条更换', daysOverdue: 1, assignee: '王五' }
          ]
        }
      });
    } else {
      responseContent = `我已经收到您的问题："${tempMessage}"\n\n根据我的分析，这里是相关信息：\n\n1. 首先，我检查了相关数据\n2. 然后进行了分析\n3. 最后给出建议\n\n您可以根据分析结果采取相应的行动。`;
    }

    // 添加AI回复
    currentMessages.value.push({
      role: 'assistant',
      content: responseContent,
      cards: cards
    });

  } catch (error) {
    isThinking.value = false
    ElMessage.error('AI助手暂时无法响应，请稍后重试')

    // 添加错误信息
    currentMessages.value.push({
      role: 'assistant',
      content: '抱歉，我在分析过程中遇到了一些问题。请稍后重试，或联系技术支持。',
      cards: []
    })
  } finally {
    isThinking.value = false;
    scrollToBottom();
  }
}

// 格式化日期
const formatDate = (date) => {
  const now = new Date()
  const diff = now - date
  const dayDiff = Math.floor(diff / (1000 * 60 * 60 * 24))

  if (dayDiff === 0) {
    return '今天'
  } else if (dayDiff === 1) {
    return '昨天'
  } else if (dayDiff <= 7) {
    return `${dayDiff}天前`
  } else {
    return date.toLocaleDateString()
  }
}

// 渲染Markdown
const renderMarkdown = (text) => {
  return md.render(text)
}

// 获取卡片组件
const getCardComponent = (type) => {
  switch (type) {
    case 'alert-analysis':
      return AlertAnalysisCard
    case 'mini-chart':
      return MiniChartCard
    case 'data-table':
      return DataTableCard
    default:
      return null
  }
}

// 复制消息
const copyMessage = (content) => {
  navigator.clipboard.writeText(content).then(() => {
    ElMessage.success('已复制到剪贴板')
  })
}

// 赞/踩消息
const likeMessage = (message) => {
  ElMessage.success('已点赞这条回复')
}
const dislikeMessage = (message) => {
  ElMessage.warning('已反馈这条回复')
}

// 其他辅助方法
const insertNewline = (event) => {
  if (event.shiftKey) {
    event.target.value += '\n'
    event.preventDefault()
  }
}

const stopGeneration = () => {
  isThinking.value = false
  ElMessage.info('AI回复已停止')
}

const createNewChat = async () => {
  try {
    const newSession = await createChatSession(`新会话 ${Date.now()}`)
    currentSessionId.value = newSession.id
    currentMessages.value = []
  } catch (error) {
    ElMessage.error('创建会话失败')
  }
}

const switchSession = async (sessionId) => {
  currentSessionId.value = sessionId
  // 实际应用中需要从后端获取该会话的消息历史
  currentMessages.value = []

  // 模拟获取历史消息
  if (sessionId === 1) {
    currentMessages.value = [
      {
        role: 'user',
        content: '帮我分析1号冷库的温度异常问题'
      },
      {
        role: 'assistant',
        content: '根据我的分析，1号冷库在过去24小时内温度波动较大，主要原因是制冷设备运行不稳定。\n\n**建议措施：**\n1. 检查制冷剂压力\n2. 清洁冷凝器\n3. 检查电气连接',
        cards: [
          {
            type: 'mini-chart',
            data: {
              title: '1号冷库温度趋势',
              data: [2, 3, 1, 4, 2, 5, 3]
            }
          }
        ]
      }
    ]
  }
}

const renameSession = (sessionId) => {
  const session = chatSessions.value.find(s => s.id === sessionId)
  ElMessageBox.prompt('请输入新的会话标题', '重命名', {
    inputValue: session?.title
  }).then(({ value }) => {
    // 这里应该调用API更新标题
    if (session) {
      session.title = value
    }
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
    ElMessage.error('删除会话失败')
  }
}

const sendPrompt = (prompt) => {
  inputMessage.value = prompt
  sendMessage()
}

onMounted(async () => {
  // 加载聊天历史
  try {
    await getChatHistory()
  } catch (error) {
    console.error('加载聊天历史失败:', error)
  }

  scrollToBottom()
})
</script>

<style scoped>
.ai-assistant-container {
  display: flex;
  height: calc(100vh - 60px); /* 减去顶部导航高度 */
  background-color: #f5f7fa;
}

.history-sidebar {
  width: 25%;
  min-width: 250px;
  background: white;
  border-right: 1px solid #e4e7ed;
  display: flex;
  flex-direction: column;
}

.sidebar-header {
  padding: 16px;
  border-bottom: 1px solid #e4e7ed;
}

.chat-history-list {
  flex: 1;
  overflow-y: auto;
  padding: 8px 0;
}

.chat-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  cursor: pointer;
  transition: background-color 0.2s;
}

.chat-item:hover {
  background-color: #f5f7fa;
}

.chat-item.active {
  background-color: #ecf5ff;
  border-left: 3px solid #409eff;
}

.chat-info {
  flex: 1;
}

.chat-title {
  font-weight: 500;
  margin-bottom: 4px;
}

.chat-time {
  font-size: 12px;
  color: #909399;
}

.chat-actions {
  display: flex;
  gap: 8px;
}

.action-icon {
  cursor: pointer;
  opacity: 0.6;
  transition: opacity 0.2s;
}

.action-icon:hover {
  opacity: 1;
}

.chat-main-area {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.messages-container {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  text-align: center;
}

.welcome-message h2 {
  color: #303133;
  margin-bottom: 12px;
}

.welcome-message p {
  color: #606266;
  margin-bottom: 30px;
}

.prompt-suggestions {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 16px;
  max-width: 800px;
  margin: 0 auto;
}

.prompt-card {
  cursor: pointer;
  transition: transform 0.2s, box-shadow 0.2s;
}

.prompt-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0,0,0,0.1);
}

.message-wrapper {
  display: flex;
  max-width: 85%;
}

.user-message {
  justify-content: flex-end;
}

.assistant-message {
  justify-content: flex-start;
}

.user-content {
  display: flex;
  justify-content: flex-end;
}

.assistant-content {
  display: flex;
  align-items: flex-start;
  gap: 12px;
}

.avatar {
  margin-top: 4px;
}

.message-bubble {
  padding: 12px 16px;
  border-radius: 18px;
  max-width: 100%;
  word-wrap: break-word;
  position: relative;
}

.user-bubble {
  background-color: #409eff;
  color: white;
  border-bottom-right-radius: 4px;
}

.assistant-bubble {
  background-color: white;
  color: #303133;
  border: 1px solid #e4e7ed;
  border-bottom-left-radius: 4px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.1);
}

.thinking-bubble {
  display: flex;
  align-items: center;
  gap: 8px;
}

.message-text {
  line-height: 1.6;
}

.message-text :deep(p) {
  margin: 8px 0;
}

.message-text :deep(ul),
.message-text :deep(ol) {
  margin: 8px 0;
  padding-left: 20px;
}

.message-text :deep(li) {
  margin: 4px 0;
}

.message-text :deep(strong) {
  font-weight: bold;
}

.message-text :deep(code) {
  background-color: #f5f7fa;
  padding: 2px 6px;
  border-radius: 3px;
  font-family: monospace;
}

.message-text :deep(pre) {
  background-color: #2d2d2d;
  color: #f8f8f2;
  padding: 12px;
  border-radius: 4px;
  overflow-x: auto;
  margin: 12px 0;
}

.structured-cards {
  margin-top: 12px;
}

.message-actions {
  margin-top: 12px;
  display: flex;
  gap: 8px;
}

.thinking-state {
  margin-top: 12px;
  display: flex;
  align-items: center;
  gap: 8px;
  color: #909399;
  font-style: italic;
}

.input-area {
  padding: 16px;
  background: white;
  border-top: 1px solid #e4e7ed;
  display: flex;
  align-items: flex-end;
  gap: 12px;
}

.context-tools {
  margin-right: 8px;
}

.input-wrapper {
  flex: 1;
}

.send-tools {
  margin-left: 8px;
}

/* 滚动条样式 */
.messages-container::-webkit-scrollbar {
  width: 6px;
}

.messages-container::-webkit-scrollbar-track {
  background: #f1f1f1;
}

.messages-container::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 3px;
}

.messages-container::-webkit-scrollbar-thumb:hover {
  background: #a8a8a8;
}
</style>