# AI智能助手后端实现

基于您前端的优秀设计，我们完成了后端的完整实现，涵盖流式输出、上下文注入、会话管理等功能。

## 核心功能实现

### 1. 流式输出 (Server-Sent Events)
- 通过 `Flux<String>` 实现SSE流式响应
- 支持前端的打字机效果

### 2. 上下文动态注入 (RAG)
- 前端可发送附件（设备、告警等）
- 后端根据附件类型查询相关数据并注入提示词
- 实现轻量级检索增强生成(RAG)

### 3. 会话管理
- 支持多会话管理
- 历史对话持久化存储
- 实现AI记忆功能

### 4. 结构化组件支持
- 为未来返回图表、工单等结构化数据预留接口

## 数据库设计

### 新增表结构
1. `ai_chat_sessions` - 会话表
2. `ai_chat_messages` - 消息明细表

### 表设计特点
- 分离Session与Message，实现标准大模型应用架构
- `role`字段对齐OpenAI规范
- 附件字段支持业务上下文注入
- LONGTEXT支持Markdown长文本

## 核心组件

### 控制器
- `AIAssistantController` - 提供SSE流式接口

### 服务类
- `AIAssistantService` - 核心业务逻辑
  - 会话管理
  - 上下文组装
  - AI调用
  - 消息持久化

### 实体类
- `AiChatSessionEntity` - 会话实体
- `AiChatMessageEntity` - 消息实体

### 数据访问层
- `AiChatSessionRepository` - 会话数据访问
- `AiChatMessageRepository` - 消息数据访问

### DTO
- `ChatRequestDto` - 请求数据传输对象

## API接口

### 流式聊天接口
```
POST /api/ai-assistant/chat/stream
Content-Type: application/json
Produces: text/event-stream

Request:
{
  "message": "用户输入的消息",
  "attachmentType": "DEVICE|ALERT|WORK_ORDER|null",
  "attachmentId": 123,
  "sessionId": 456 // null表示新建会话
}

Response: Stream of tokens
```

### 获取会话历史
```
GET /api/ai-assistant/sessions/{userId}
```

### 获取消息历史
```
GET /api/ai-assistant/messages/{sessionId}
```

## 依赖配置

需要在项目中添加Spring AI依赖：
```xml
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-qwen-spring-boot-starter</artifactId>
    <version>0.8.0-SNAPSHOT</version>
</dependency>
```

## 答辩亮点

1. **检索增强生成(RAG)**：不只是简单的问答，而是结合实时业务数据
2. **流式响应**：提供流畅的用户体验
3. **上下文记忆**：通过数据库实现AI记忆功能
4. **结构化输出**：为前端组件渲染做好准备

## 部署注意事项

1. 需要配置Qwen API密钥
2. 确保数据库表已创建
3. 调整内存配置以支持AI模型加载