<template>
  <Layout>
    <div class="ai-assistant-container">
      <div class="history-sidebar">
        <div class="sidebar-header">
          <el-button type="primary" icon="Plus" @click="createNewChat" class="new-chat-btn">
            <el-icon><EditPen /></el-icon> 新建洞察
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

      <div class="chat-main-area">
        <div class="messages-container" ref="messagesContainer">
          <div v-if="currentMessages.length === 0" class="empty-state">
            <div class="welcome-message">
              <h2>您好，我是冷链守护 AI</h2>
              <p>我可以帮您分析告警、排查设备故障，或生成运维报告。</p>
            </div>

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

          <div
              v-for="(message, index) in currentMessages"
              :key="index"
              class="message-wrapper"
              :class="{ 'user-message': message.role === 'user', 'assistant-message': message.role === 'assistant' }"
          >
            <div v-if="message.role === 'assistant'" class="assistant-content">
              <div class="avatar">
                <el-avatar :size="32" icon="Monitor" style="background-color: #f2f6fc; color: #409eff;" />
              </div>
              <div class="message-bubble assistant-bubble">

                <div v-if="isThinking && !message.content && index === currentMessages.length - 1" class="typing-indicator">
                  <span class="dot"></span>
                  <span class="dot"></span>
                  <span class="dot"></span>
                </div>

                <div v-else class="message-text">
                  <div class="markdown-body" v-html="renderMarkdown(message.content)"></div>
                  <span v-if="isThinking && index === currentMessages.length - 1" class="blinking-cursor">▍</span>
                </div>

                <div v-if="message.cards && message.cards.length > 0" class="structured-cards">
                  <component
                      v-for="(card, cardIndex) in message.cards"
                      :key="cardIndex"
                      :is="getCardComponent(card.type)"
                      :data="card.data"
                  />
                </div>

                <div class="message-actions" v-if="!isThinking || index !== currentMessages.length - 1">
                  <el-tooltip content="复制内容" placement="top">
                    <el-button link size="small" icon="CopyDocument" @click="copyMessage(message.content)" />
                  </el-tooltip>
                  <el-tooltip content="有帮助" placement="top">
                    <el-button link size="small" icon="Select" @click="likeMessage(message)" />
                  </el-tooltip>
                  <el-tooltip content="无帮助" placement="top">
                    <el-button link size="small" icon="CloseBold" @click="dislikeMessage(message)" />
                  </el-tooltip>
                </div>
              </div>
            </div>

            <div v-else class="user-content">
              <div class="message-bubble user-bubble">{{ message.content }}</div>
            </div>
          </div>
        </div>

        <div class="input-area">
          <div class="context-tools">
            <el-popover placement="top-start" trigger="click" :width="200">
              <template #reference>
                <el-button icon="Paperclip" circle title="附加系统上下文" />
              </template>
              <div class="context-menu">
                <div style="margin-bottom: 8px; font-size: 12px; color: #909399;">选择要发给AI的系统数据</div>
                <el-checkbox-group v-model="attachedContext" class="context-checkboxes">
                  <el-checkbox value="device-info">当前设备状态</el-checkbox>
                  <el-checkbox value="alert-record">最新告警记录</el-checkbox>
                  <el-checkbox value="workorder-detail">我的待办工单</el-checkbox>
                </el-checkbox-group>
              </div>
            </el-popover>
          </div>

          <div class="input-wrapper">
            <el-input
                v-model="inputMessage"
                type="textarea"
                :autosize="{ minRows: 1, maxRows: 6 }"
                placeholder="输入您的问题，按 Enter 发送，Shift + Enter 换行..."
                @keydown.enter.exact.prevent="sendMessage"
                @keydown.shift.enter.native="insertNewline"
                :disabled="isThinking"
            />
          </div>

          <div class="send-tools">
            <el-button
                v-if="!isThinking"
                type="primary"
                icon="Top"
                circle
                @click="sendMessage"
                :disabled="!inputMessage.trim()"
            />
            <el-button
                v-else
                type="danger"
                icon="VideoPause"
                circle
                @click="stopGeneration"
                title="停止生成"
            />
          </div>
        </div>
      </div>
    </div>
  </Layout>
</template>

<script setup>
import { ref, onMounted, onUnmounted, nextTick } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useAiAssistant } from '@/composables/useAiAssistant'
import MarkdownIt from 'markdown-it'
import Layout from '@/components/Layout.vue'
import {
  Edit,
  Delete,
  Lightning,
  EditPen,
  Paperclip,
  Top,
  VideoPause,
  CopyDocument,
  Select,
  CloseBold,
  Monitor
} from '@element-plus/icons-vue'

import AlertAnalysisCard from './components/AlertAnalysisCard.vue'
import MiniChartCard from './components/MiniChartCard.vue'
import DataTableCard from './components/DataTableCard.vue'

const md = new MarkdownIt({ html: true, linkify: true, typographer: true })

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
const attachedContext = ref([])
const messagesContainer = ref(null)

let currentSSEController = null
let scrollObserver = null

const quickPrompts = [
  { id: 1, text: '分析最近 24 小时未处理的紧急告警' },
  { id: 2, text: '生成本周冷库温湿度波动总结' },
  { id: 3, text: '设备离线通常有哪些原因？如何排查？' },
  { id: 4, text: '帮我催办所有已逾期的运维工单' }
]

// 核心优化：利用 MutationObserver 监听 DOM 变化实现丝滑自动滚动
const setupScrollObserver = () => {
  if (!messagesContainer.value) return

  scrollObserver = new MutationObserver(() => {
    const container = messagesContainer.value
    // 如果用户往上翻看历史记录（距离底部超过 150px），则不强制滚动到底部，尊重用户操作
    const isNearBottom = container.scrollHeight - container.scrollTop - container.clientHeight < 150
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

const sendMessage = async () => {
  if (!inputMessage.value.trim() || isThinking.value) return

  const userText = inputMessage.value.trim()
  currentMessages.value.push({ role: 'user', content: userText })
  inputMessage.value = ''

  isThinking.value = true

  try {
    // 1. 🌟 先直接把空数据推入响应式数组中
    currentMessages.value.push({
      role: 'assistant',
      content: '',
      cards: []
    })

    // 如果还没有会话，自动创建一个隐式会话
    if (!currentSessionId.value) {
      const newSession = await createChatSession({ title: userText.substring(0, 15) })
      currentSessionId.value = newSession.id
    }

    let fullResponse = ''

    // 2. 🌟🌟🌟 极其关键：从数组的最后一位，把刚刚推入的那个对象"捞"出来！
    // 这个被捞出来的 targetMessage 是被 Vue 包装过的 Proxy 响应式对象！
    const targetMessage = currentMessages.value[currentMessages.value.length - 1]

    currentSSEController = streamMessage(
        userText,
        null,
        null,
        currentSessionId.value,
        (token) => {
          // 防错兼容：如果 sse.js 传过来的是对象，就提取 content，否则直接用字符串
          const chunkText = typeof token === 'string' ? token : (token.content || '')

          // 过滤掉后端的流结束标识符(如果有的话)
          if (chunkText === '[DONE]') return

          fullResponse += chunkText

          // 3. 🌟 修改这个 Proxy 对象的 content，就能瞬间触发 Vue 的视图刷新！
          targetMessage.content = fullResponse
        },
        (error) => {
          console.error('SSE error:', error)
          if (fullResponse === '') {
            targetMessage.content = '抱歉，连接服务器失败，请稍后重试。'
          }
          isThinking.value = false
        },
        () => {
          // 流完成回调：设置 isThinking 为 false 并清理状态
          isThinking.value = false
          currentSSEController = null
        }
    )

  } catch (error) {
    console.error('发送失败:', error)
    isThinking.value = false
  }
}

// 供 sse.js 中 `if (data === '[DONE]')` 触发时调用的结束方法 (如已实现可接入)
const onStreamComplete = () => {
  isThinking.value = false
  currentSSEController = null
}

const stopGeneration = () => {
  if (currentSSEController) {
    currentSSEController.abort()
    currentSSEController = null
  }
  isThinking.value = false
}

const formatDate = (date) => {
  if (!date) return '刚刚'
  const dateObj = typeof date === 'string' ? new Date(date) : date
  if (isNaN(dateObj.getTime())) return '刚刚'

  const now = new Date()
  const diff = now - dateObj
  const dayDiff = Math.floor(diff / (1000 * 60 * 60 * 24))

  if (dayDiff === 0) return dateObj.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })
  if (dayDiff === 1) return '昨天'
  if (dayDiff <= 7) return `${dayDiff}天前`
  return dateObj.toLocaleDateString()
}

const renderMarkdown = (text) => {
  if (!text) return ''
  return md.render(text)
}

const getCardComponent = (type) => {
  switch (type) {
    case 'alert-analysis': return AlertAnalysisCard
    case 'mini-chart': return MiniChartCard
    case 'data-table': return DataTableCard
    default: return null
  }
}

const copyMessage = (content) => {
  navigator.clipboard.writeText(content).then(() => ElMessage.success('已复制'))
}
const likeMessage = () => ElMessage.success('感谢反馈')
const dislikeMessage = () => ElMessage.info('已记录您的反馈，我们会继续改进')

const insertNewline = (event) => {
  event.target.value += '\n'
  inputMessage.value = event.target.value
}

const createNewChat = async () => {
  if (isThinking.value) return
  currentSessionId.value = null
  currentMessages.value = []
}

const switchSession = async (sessionId) => {
  if (isThinking.value) return
  currentSessionId.value = sessionId
  try {
    if (!sessionId) {
      currentMessages.value = []
      return
    }
    const messages = await getChatMessages(sessionId)
    currentMessages.value = messages.map(msg => ({
      role: msg.role.toLowerCase(),
      content: msg.content
    }))
    nextTick(() => {
      if (messagesContainer.value) {
        messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
      }
    })
  } catch (error) {
    currentMessages.value = []
  }
}

const renameSession = (sessionId) => {
  const session = chatSessions.value.find(s => s.id === sessionId)
  ElMessageBox.prompt('请输入新的会话标题', '重命名', {
    inputValue: session?.title
  }).then(({ value }) => {
    if (session) session.title = value
  }).catch(() => {})
}

const deleteSession = async (sessionId) => {
  try {
    if (!sessionId) {
      return
    }
    await deleteChatSession(sessionId)
    if (currentSessionId.value === sessionId) {
      currentSessionId.value = null
      currentMessages.value = []
    }
  } catch (error) {}
}

const sendPrompt = (prompt) => {
  inputMessage.value = prompt
  sendMessage()
}

onMounted(async () => {
  await getChatHistory()
  setupScrollObserver()
})

onUnmounted(() => {
  if (scrollObserver) scrollObserver.disconnect()
})
</script>

<style scoped>
.ai-assistant-container {
  display: flex;
  height: calc(100vh - 60px);
  background-color: #ffffff;
}

/* 左侧栏样式优化 */
.history-sidebar {
  width: 260px;
  background: #f9f9f9;
  border-right: 1px solid #e4e7ed;
  display: flex;
  flex-direction: column;
}

.sidebar-header {
  padding: 16px;
  border-bottom: 1px solid transparent; /* 保持高度占位 */
}

.new-chat-btn {
  width: 100%;
  border-radius: 8px;
  font-weight: 500;
}

.chat-history-list {
  flex: 1;
  overflow-y: auto;
  padding: 8px 12px;
}

.chat-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 12px;
  margin-bottom: 4px;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s ease;
  color: #303133;
}

.chat-item:hover {
  background-color: #eef2f9;
}

.chat-item.active {
  background-color: #e1edfd;
  color: #409eff;
}

.chat-title {
  font-size: 14px;
  font-weight: 500;
  margin-bottom: 4px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  max-width: 160px;
}

.chat-actions {
  display: flex;
  gap: 6px;
}

.action-icon {
  padding: 4px;
  border-radius: 4px;
}

.action-icon:hover {
  background-color: #dcdfe6;
  color: #f56c6c;
}

/* 主对话区优化 */
.chat-main-area {
  flex: 1;
  display: flex;
  flex-direction: column;
  background: #ffffff;
}

.messages-container {
  flex: 1;
  overflow-y: auto;
  padding: 24px 15%;
  display: flex;
  flex-direction: column;
  gap: 24px;
  scroll-behavior: smooth;
}

.message-wrapper {
  display: flex;
  width: 100%;
}

.user-message {
  justify-content: flex-end;
  /* 确保父容器占满整行，但子元素靠右对齐 */
  width: 100%;
}

.user-bubble {
  background-color: #f4f6f8;
  color: #303133;
  border-radius: 16px;
  padding: 10px 16px; /* 稍微调小一点内边距，看起来更紧凑 */

  /* 🌟 核心修复 1：不要设为块级元素，设为 inline-block 可以让气泡紧贴文字宽度 */
  display: inline-block;

  /* 🌟 核心修复 2：取消固定的 max-width 80%，改用 vw 或 fit-content */
  max-width: fit-content;

  font-size: 15px;
  line-height: 1.6;

  /* 保留 pre-wrap 以支持用户输入的真实换行 */
  white-space: pre-wrap;
  word-break: break-word;

  /* 确保文本靠左对齐，但气泡整体靠右 */
  text-align: left;
}

.assistant-content {
  display: flex;
  gap: 16px;
  max-width: 90%;
}

.assistant-bubble {
  flex: 1;
  color: #303133;
  font-size: 15px;
  line-height: 1.7;
}

/* Markdown 排版细节优化 */
.markdown-body :deep(p) { margin-top: 0; margin-bottom: 1em; }
.markdown-body :deep(p:last-child) { margin-bottom: 0; }
.markdown-body :deep(code) {
  background-color: #f0f2f5;
  padding: 2px 6px;
  border-radius: 4px;
  font-family: Consolas, Monaco, monospace;
  font-size: 13px;
  color: #f56c6c;
}
.markdown-body :deep(pre) {
  background-color: #282c34;
  color: #abb2bf;
  padding: 16px;
  border-radius: 8px;
  overflow-x: auto;
  margin: 16px 0;
}
.markdown-body :deep(pre code) {
  background-color: transparent;
  color: inherit;
  padding: 0;
}
.markdown-body :deep(ul), .markdown-body :deep(ol) {
  padding-left: 24px;
  margin-bottom: 16px;
}

/* 光标与动画 */
.blinking-cursor {
  display: inline-block;
  width: 8px;
  height: 16px;
  background-color: #409eff;
  vertical-align: middle;
  animation: blink 1s step-end infinite;
  margin-left: 4px;
}

@keyframes blink {
  0%, 100% { opacity: 1; }
  50% { opacity: 0; }
}

.typing-indicator {
  display: flex;
  align-items: center;
  height: 24px;
  gap: 4px;
  padding: 4px 8px;
}

.dot {
  width: 6px;
  height: 6px;
  background-color: #a8abb2;
  border-radius: 50%;
  animation: bounce 1.4s infinite ease-in-out both;
}

.dot:nth-child(1) { animation-delay: -0.32s; }
.dot:nth-child(2) { animation-delay: -0.16s; }

@keyframes bounce {
  0%, 80%, 100% { transform: scale(0); opacity: 0.5; }
  40% { transform: scale(1); opacity: 1; }
}

.message-actions {
  margin-top: 8px;
  display: flex;
  gap: 4px;
  opacity: 0;
  transition: opacity 0.2s;
}

.assistant-content:hover .message-actions {
  opacity: 1;
}

/* 输入区优化 */
.input-area {
  padding: 16px 15%;
  background: #ffffff;
  display: flex;
  align-items: flex-end;
  gap: 12px;
  position: relative;
}

.input-area::before {
  content: '';
  position: absolute;
  top: -40px;
  left: 0;
  right: 0;
  height: 40px;
  background: linear-gradient(to top, rgba(255,255,255,1), rgba(255,255,255,0));
  pointer-events: none;
}

.input-wrapper {
  flex: 1;
  background: #f4f6f8;
  border-radius: 16px;
  padding: 4px;
  border: 1px solid transparent;
  transition: border-color 0.2s;
}
.input-wrapper:focus-within {
  border-color: #c0c4cc;
  background: #ffffff;
}

.input-wrapper :deep(.el-textarea__inner) {
  background: transparent;
  border: none;
  box-shadow: none;
  padding: 8px 12px;
  font-size: 15px;
  resize: none;
}

.context-checkboxes {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

/* 空状态卡片优化 */
.empty-state {
  margin-top: 10vh;
}
.welcome-message h2 { font-size: 28px; margin-bottom: 12px; font-weight: 600; }
.prompt-card {
  border-radius: 12px;
  border: 1px solid #ebeef5;
  color: #606266;
  font-size: 14px;
}
.prompt-card:hover {
  border-color: #409eff;
  color: #409eff;
}
</style>