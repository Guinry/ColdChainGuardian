// composables/useAiAssistant.js
import { ref, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import { aiAssistantApi } from '@/api/ai-assistant'

export function useAiAssistant() {
  const loading = ref(false)
  const chatSessions = ref([])
  const currentSession = ref(null)

  // API实例
  const api = aiAssistantApi()

  // 发送消息
  const sendMessage = async (message, context = {}) => {
    try {
      loading.value = true

      const response = await api.sendMessage({
        message,
        context,
        sessionId: currentSession.value?.id
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
  const streamMessage = (message, context = {}, onMessage, onError) => {
    const data = {
      message,
      context,
      sessionId: currentSession.value?.id
    }

    return api.streamMessage(data, onMessage, onError)
  }

  // 获取聊天历史
  const getChatHistory = async (params = {}) => {
    try {
      const response = await api.getChatHistory(params)
      chatSessions.value = response.data
      return response.data
    } catch (error) {
      console.error('获取聊天历史失败:', error)
      ElMessage.error('获取聊天历史失败')
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
    createChatSession,
    deleteChatSession
  }
}