// composables/useAiAssistant.js
import { ref, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import { aiAssistantApi } from '@/api/ai-assistant'
import { SSEClient } from '@/utils/sse'

export function useAiAssistant() {
  const loading = ref(false)
  const chatSessions = ref([])
  const currentSession = ref(null)

  // API实例
  const api = aiAssistantApi()

  // 发送消息
  const sendMessage = async (message, attachmentType = null, attachmentId = null, sessionId = null) => {
    try {
      loading.value = true

      const response = await api.sendMessage({
        message,
        attachmentType,
        attachmentId,
        sessionId
      })

      return response.data
    } catch (error) {
      console.error('发送消息失败:', error)
      ElMessage.error('发送消息失败，请重试')
      throw error
    } finally {
      loading.value = false
    }
  }

  // 流式发送消息（用于SSE）
  const streamMessage = (message, attachmentType = null, attachmentId = null, sessionId = null, onTokenReceived, onError) => {
    const data = {
      message,
      attachmentType,
      attachmentId,
      sessionId
    }

    // Create SSE client
    const sseClient = new SSEClient('/api/ai-assistant/chat/stream')

    if (onTokenReceived) {
      sseClient.setMessageHandler(onTokenReceived)
    }

    if (onError) {
      sseClient.setErrorHandler(onError)
    }

    // Connect and send data
    const controller = sseClient.connect(data)

    return controller // Return controller to allow cancellation
  }

  // 获取聊天历史
  const getChatHistory = async (userId = 1) => {
    try {
      const response = await api.getChatHistory(userId)
      chatSessions.value = response.data
      return response.data
    } catch (error) {
      console.error('获取聊天历史失败:', error)
      ElMessage.error('获取聊天历史失败')
      throw error
    }
  }

  // 根据会话ID获取消息
  const getChatMessages = async (sessionId) => {
    try {
      const response = await api.getChatMessages(sessionId)
      return response.data
    } catch (error) {
      console.error('获取消息失败:', error)
      ElMessage.error('获取消息失败')
      throw error
    }
  }

  // 创建新会话
  const createChatSession = async (title) => {
    try {
      const response = await api.createChatSession({ title })
      chatSessions.value.unshift(response.data)
      currentSession.value = response.data
      return response.data
    } catch (error) {
      console.error('创建会话失败:', error)
      ElMessage.error('创建会话失败')
      throw error
    }
  }

  // 删除会话
  const deleteChatSession = async (sessionId) => {
    try {
      await api.deleteChatSession(sessionId)
      const index = chatSessions.value.findIndex(s => s.id === sessionId)
      if (index !== -1) {
        chatSessions.value.splice(index, 1)
      }
      if (currentSession.value?.id === sessionId) {
        currentSession.value = null
      }
      ElMessage.success('会话已删除')
    } catch (error) {
      console.error('删除会话失败:', error)
      ElMessage.error('删除会话失败')
      throw error
    }
  }

  return {
    loading,
    chatSessions,
    currentSession,
    sendMessage,
    streamMessage,
    getChatHistory,
    getChatMessages,
    createChatSession,
    deleteChatSession
  }
}