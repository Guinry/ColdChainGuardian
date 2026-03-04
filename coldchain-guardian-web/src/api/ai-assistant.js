import request from '@/utils/request'

// AI助手相关API
export function aiAssistantApi() {
  return {
    // 发送消息到AI
    sendMessage: (data) => {
      return request({
        url: '/api/ai/chat',
        method: 'post',
        data
      })
    },

    // 流式发送消息（用于SSE）
    streamMessage: (data, onMessage, onError) => {
      const eventSource = new EventSourcePolyfill(
        `${import.meta.env.VITE_API_BASE_URL}/api/ai/stream-chat`,
        {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${localStorage.getItem('token')}`
          },
          body: JSON.stringify(data)
        }
      )

      eventSource.addEventListener('message', onMessage)
      eventSource.addEventListener('error', onError)

      return eventSource
    },

    // 获取聊天历史
    getChatHistory: (params) => {
      return request({
        url: '/api/ai/chat-history',
        method: 'get',
        params
      })
    },

    // 创建新的聊天会话
    createChatSession: (data) => {
      return request({
        url: '/api/ai/chat-session',
        method: 'post',
        data
      })
    },

    // 更新聊天会话标题
    updateChatSession: (id, data) => {
      return request({
        url: `/api/ai/chat-session/${id}`,
        method: 'put',
        data
      })
    },

    // 删除聊天会话
    deleteChatSession: (id) => {
      return request({
        url: `/api/ai/chat-session/${id}`,
        method: 'delete'
      })
    },

    // AI分析告警
    analyzeAlert: (alertId) => {
      return request({
        url: `/api/ai/analyze-alert/${alertId}`,
        method: 'get'
      })
    },

    // AI生成趋势分析
    generateTrendAnalysis: (params) => {
      return request({
        url: '/api/ai/trend-analysis',
        method: 'get',
        params
      })
    }
  }
}