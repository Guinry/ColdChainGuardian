import { apiClient } from '@/utils/api'

// AI助手相关API
export function aiAssistantApi() {
  return {
    // 发送消息到AI
    sendMessage: (data) => {
      return apiClient({
        url: '/ai/chat',
        method: 'post',
        data
      })
    },

    // 流式发送消息（用于SSE）
    streamMessage: (data, onMessage, onError) => {
      // 这里将使用原生EventSource或fetch API实现流式响应
      // 由于EventSource不直接支持POST和发送数据，
      // 实际使用中可能需要用fetch API包装
      console.log('Streaming message to AI:', data)

      // 返回一个模拟的对象，实际实现需要后端支持SSE
      const mockEventSource = {
        addEventListener: (type, handler) => {
          // 模拟事件处理
          setTimeout(() => {
            if (type === 'message') {
              handler({ data: JSON.stringify({ type: 'chunk', content: '正在分析数据...' }) })
              setTimeout(() => {
                handler({ data: JSON.stringify({ type: 'complete', content: '', cards: [] }) })
              }, 1000)
            }
          }, 500)
        },
        close: () => {
          console.log('EventSource closed')
        }
      }

      return mockEventSource
    },

    // 获取聊天历史
    getChatHistory: (params) => {
      return apiClient({
        url: '/ai/chat-history',
        method: 'get',
        params
      })
    },

    // 创建新的聊天会话
    createChatSession: (data) => {
      return apiClient({
        url: '/ai/chat-session',
        method: 'post',
        data
      })
    },

    // 更新聊天会话标题
    updateChatSession: (id, data) => {
      return apiClient({
        url: `/ai/chat-session/${id}`,
        method: 'put',
        data
      })
    },

    // 删除聊天会话
    deleteChatSession: (id) => {
      return apiClient({
        url: `/ai/chat-session/${id}`,
        method: 'delete'
      })
    },

    // AI分析告警
    analyzeAlert: (alertId) => {
      return apiClient({
        url: `/ai/analyze-alert/${alertId}`,
        method: 'get'
      })
    },

    // AI生成趋势分析
    generateTrendAnalysis: (params) => {
      return apiClient({
        url: '/ai/trend-analysis',
        method: 'get',
        params
      })
    }
  }
}