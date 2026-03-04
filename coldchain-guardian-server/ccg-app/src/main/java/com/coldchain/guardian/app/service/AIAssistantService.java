package com.coldchain.guardian.app.service;

import com.coldchain.guardian.contract.dto.ai.ChatRequestDto;
import com.coldchain.guardian.infra.persistence.entity.AiChatMessageEntity;
import com.coldchain.guardian.infra.persistence.entity.AiChatSessionEntity;
import com.coldchain.guardian.infra.persistence.entity.DeviceEntity;
import com.coldchain.guardian.infra.persistence.entity.AlertEntity;
import com.coldchain.guardian.infra.persistence.repository.*;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class AIAssistantService {

    @Autowired
    private ChatClient.Builder chatClientBuilder;

    @Autowired
    private AiChatSessionRepository aiChatSessionRepository;

    @Autowired
    private AiChatMessageRepository aiChatMessageRepository;

    @Autowired
    private DeviceRepository deviceRepository;

    @Autowired
    private AlertRepository alertRepository;

    private ChatClient chatClient;

    public AIAssistantService(ChatClient.Builder builder) {
        this.chatClient = builder
            .defaultSystem("你是一个专业的冷链仓储安全AI参谋。你的回答需要专业、简明扼要，支持使用 Markdown 格式。")
            .build();
    }

    public Flux<String> streamChat(ChatRequestDto request) {
        // 如果是新会话，创建会话记录
        Long sessionId = request.getSessionId();
        if (sessionId == null) {
            AiChatSessionEntity session = new AiChatSessionEntity();
            session.setUserId(1L); // 这前硬编码，实际应该从当前用户获取
            session.setTitle(request.getMessage().length() > 50 ? request.getMessage().substring(0, 50) + "..." : request.getMessage());
            session.setIsDeleted(0);
            aiChatSessionRepository.insert(session);
            sessionId = session.getId();
        }

        // 获取历史消息记录
        List<Message> historyMessages = getChatMessageHistory(sessionId);

        // 构建最终用户消息，包括附件上下文
        String finalUserMessage = buildFinalUserMessage(request);

        // 保存用户消息到数据库
        saveUserMessage(sessionId, request.getMessage(), request.getAttachmentType(), request.getAttachmentId());

        // 构建提示词
        StringBuilder promptBuilder = new StringBuilder();
        for (Message msg : historyMessages) {
            promptBuilder.append(msg.getContent()).append("\n");
        }
        promptBuilder.append("用户问题: ").append(finalUserMessage);

        // 使用流式方式调用AI
        Flux<String> responseFlux = chatClient.prompt()
                .user(promptBuilder.toString())
                .stream()
                .content();

        // 异步保存AI回复
        Long finalSessionId = sessionId; // Make sessionId effectively final for lambda access
        StringBuilder responseBuilder = new StringBuilder();
        return responseFlux.doOnNext(token -> responseBuilder.append(token))
                .doOnComplete(() -> {
                    // 保存AI的完整回复
                    saveAssistantMessage(finalSessionId, responseBuilder.toString());
                });
    }

    private List<Message> getChatMessageHistory(Long sessionId) {
        List<Message> history = new ArrayList<>();
        List<AiChatMessageEntity> messages = aiChatMessageRepository.findBySessionId(sessionId);

        for (AiChatMessageEntity message : messages) {
            if ("USER".equals(message.getRole())) {
                history.add(new UserMessage(message.getContent()));
            } else if ("ASSISTANT".equals(message.getRole())) {
                history.add(new AssistantMessage(message.getContent()));
            }
        }

        return history;
    }

    private String buildFinalUserMessage(ChatRequestDto request) {
        String userMessage = request.getMessage();

        // 如果用户带了"附件"，后端主动去数据库查数据，喂给大模型
        if (request.getAttachmentType() != null && request.getAttachmentId() != null) {
            String context = "";

            if ("DEVICE".equals(request.getAttachmentType())) {
                DeviceEntity device = deviceRepository.findById(request.getAttachmentId());
                if (device != null) {
                    context = String.format(
                        "【系统注入上下文】：用户正在询问关于设备 [%s] (%s) 的问题。该设备当前状态：%s，安装位置：%s。",
                        device.getDeviceName(), device.getDeviceCode(),
                        device.getOnlineStatus() == 1 ? "在线" : "离线", device.getLocationDesc()
                    );
                }
            } else if ("ALERT".equals(request.getAttachmentType())) {
                AlertEntity alert = alertRepository.findById(request.getAttachmentId());
                if (alert != null) {
                    context = String.format(
                        "【系统注入上下文】：用户正在询问关于告警 [%s] 的问题。告警级别：%s，告警类型：%s，发生时间：%s。",
                        alert.getMessage(), alert.getAlertLevel(), alert.getAlertType(), alert.getCreateTime()
                    );
                }
            }

            userMessage = context + "\n\n用户的实际问题是：" + userMessage;
        }

        return userMessage;
    }

    private void saveUserMessage(Long sessionId, String content, String attachmentType, Long attachmentId) {
        AiChatMessageEntity message = new AiChatMessageEntity();
        message.setSessionId(sessionId);
        message.setRole("USER");
        message.setContent(content);
        message.setAttachmentType(attachmentType);
        message.setAttachmentId(attachmentId);
        aiChatMessageRepository.insert(message);
    }

    private void saveAssistantMessage(Long sessionId, String content) {
        AiChatMessageEntity message = new AiChatMessageEntity();
        message.setSessionId(sessionId);
        message.setRole("ASSISTANT");
        message.setContent(content);
        aiChatMessageRepository.insert(message);
    }

    // Public method for getting session history
    public List<AiChatSessionEntity> getSessionHistory(Long userId) {
        return aiChatSessionRepository.findByUserId(userId);
    }

    public List<AiChatMessageEntity> getChatMessagesBySessionId(Long sessionId) {
        return aiChatMessageRepository.findBySessionId(sessionId);
    }

    // Method to create a new session
    public AiChatSessionEntity createSession(AiChatSessionEntity session) {
        // 设置默认用户ID，实际应用中应该从当前认证用户获取
        if (session.getUserId() == null) {
            session.setUserId(1L); // 默认用户ID，实际应用中应从SecurityContext获取
        }
        // 确保创建时间和更新时间被设置
        if (session.getCreateTime() == null) {
            session.setCreateTime(LocalDateTime.now());
        }
        if (session.getUpdateTime() == null) {
            session.setUpdateTime(LocalDateTime.now());
        }
        aiChatSessionRepository.insert(session);
        return session; // 返回创建后的会话对象，包含新生成的ID
    }

    // Method to update a session
    public void updateSession(AiChatSessionEntity session) {
        aiChatSessionRepository.updateById(session);
    }

    // Method to delete a session
    public void deleteSession(Long id) {
        aiChatSessionRepository.removeById(id);
    }
}