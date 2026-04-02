package com.coldchain.guardian.app.controller;

import com.coldchain.guardian.app.service.AIAssistantService;
import com.coldchain.guardian.contract.dto.ai.ChatRequestDto;
import com.coldchain.guardian.infra.persistence.entity.AiChatMessageEntity;
import com.coldchain.guardian.infra.persistence.entity.AiChatSessionEntity;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.List;

@Tag(name = "AI 助手管理", description = "提供 AI 智能助手聊天、会话管理等接口")
@RestController
@RequestMapping("/api/ai-assistant")
public class AIAssistantController {

    @Autowired
    private AIAssistantService aiAssistantService;

    /**
     * 流式聊天
     */
    @Operation(summary = "流式聊天", description = "与 AI 助手进行流式对话，实时返回响应")
    @PostMapping(value = "/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> streamChat(@RequestBody ChatRequestDto request) {
        return aiAssistantService.streamChat(request);
    }

    /**
     * 普通聊天
     */
    @Operation(summary = "普通聊天", description = "与 AI 助手进行普通对话")
    @PostMapping("/chat")
    public Flux<String> chat(@RequestBody ChatRequestDto request) {
        return aiAssistantService.streamChat(request);
    }

    /**
     * 获取用户会话历史
     */
    @Operation(summary = "获取会话历史", description = "获取指定用户的所有聊天会话列表")
    @GetMapping("/sessions/{userId}")
    public ApiResponse<List<AiChatSessionEntity>> getChatSessions(@PathVariable Long userId) {
        try {
            List<AiChatSessionEntity> sessions = aiAssistantService.getSessionHistory(userId);
            return ApiResponse.success(sessions);
        } catch (Exception e) {
            return ApiResponse.error("获取会话历史失败：" + e.getMessage());
        }
    }

    /**
     * 获取会话消息
     */
    @Operation(summary = "获取会话消息", description = "获取指定会话的所有消息记录")
    @GetMapping("/messages/{sessionId}")
    public ApiResponse<List<AiChatMessageEntity>> getChatMessages(@PathVariable Long sessionId) {
        try {
            List<AiChatMessageEntity> messages = aiAssistantService.getChatMessagesBySessionId(sessionId);
            return ApiResponse.success(messages);
        } catch (Exception e) {
            return ApiResponse.error("获取会话消息失败：" + e.getMessage());
        }
    }

    /**
     * 创建会话
     */
    @Operation(summary = "创建会话", description = "创建新的聊天会话")
    @PostMapping("/sessions")
    public ApiResponse<AiChatSessionEntity> createChatSession(@RequestBody AiChatSessionEntity session) {
        try {
            AiChatSessionEntity created = aiAssistantService.createSession(session);
            return ApiResponse.success(created);
        } catch (Exception e) {
            return ApiResponse.error("创建会话失败：" + e.getMessage());
        }
    }

    /**
     * 更新会话
     */
    @Operation(summary = "更新会话", description = "更新聊天会话信息")
    @PutMapping("/sessions/{id}")
    public ApiResponse<AiChatSessionEntity> updateChatSession(@PathVariable Long id, @RequestBody AiChatSessionEntity session) {
        try {
            session.setId(id);
            aiAssistantService.updateSession(session);
            return ApiResponse.success(session);
        } catch (Exception e) {
            return ApiResponse.error("更新会话失败：" + e.getMessage());
        }
    }

    /**
     * 删除会话
     */
    @Operation(summary = "删除会话", description = "删除指定的聊天会话")
    @DeleteMapping("/sessions/{id}")
    public ApiResponse<Void> deleteChatSession(@PathVariable Long id) {
        try {
            aiAssistantService.deleteSession(id);
            return ApiResponse.success(null);
        } catch (Exception e) {
            return ApiResponse.error("删除会话失败：" + e.getMessage());
        }
    }

    /**
     * 统一响应包装类
     */
    public static class ApiResponse<T> {
        private int code;
        private String message;
        private T data;

        public static <T> ApiResponse<T> success(T data) {
            ApiResponse<T> response = new ApiResponse<>();
            response.code = 200;
            response.message = "success";
            response.data = data;
            return response;
        }

        public static <T> ApiResponse<T> error(String message) {
            ApiResponse<T> response = new ApiResponse<>();
            response.code = 500;
            response.message = message;
            return response;
        }

        public int getCode() { return code; }
        public void setCode(int code) { this.code = code; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        public T getData() { return data; }
        public void setData(T data) { this.data = data; }
    }
}
