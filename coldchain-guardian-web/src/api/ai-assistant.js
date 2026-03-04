import { apiClient } from '@/utils/api'

// AI助手相关API
export function aiAssistantApi() {
  return {
    // 发送消息到AI（流式）
    streamMessage: (data) => {
      return apiClient({
        url: '/ai-assistant/chat/stream',
        method: 'post',
        data,
        headers: {
          'Accept': 'text/event-stream',
          'Content-Type': 'application/json'
        }
      })
    },

    // 发送消息到AI（普通）
    sendMessage: (data) => {
      return apiClient({
        url: '/ai-assistant/chat',
        method: 'post',
        data
      })
    },

    // 获取用户的所有会话历史
    getChatHistory: (userId) => {
      return apiClient({
        url: `/ai-assistant/sessions/${userId}`,
        method: 'get'
      })
    },

    // 根据会话ID获取消息历史
    getChatMessages: (sessionId) => {
      return apiClient({
        url: `/ai-assistant/messages/${sessionId}`,
        method: 'get'
      })
    },

    // 创建新的聊天会话
    createChatSession: (data) => {
      return apiClient({
        url: '/ai-assistant/sessions',
        method: 'post',
        data
      })
    },

    // 更新聊天会话
    updateChatSession: (id, data) => {
      return apiClient({
        url: `/ai-assistant/sessions/${id}`,
        method: 'put',
        data
      })
    },

    // 删除聊天会话
    deleteChatSession: (id) => {
      return apiClient({
        url: `/ai-assistant/sessions/${id}`,
        method: 'delete'
      })
    }
  }
}