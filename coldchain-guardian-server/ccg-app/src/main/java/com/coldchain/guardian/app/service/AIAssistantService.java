package com.coldchain.guardian.app.service;

import com.coldchain.guardian.contract.dto.ai.ChatRequestDto;
import com.coldchain.guardian.infra.persistence.entity.AiChatMessageEntity;
import com.coldchain.guardian.infra.persistence.entity.AiChatSessionEntity;
import com.coldchain.guardian.infra.persistence.entity.DeviceEntity;
import com.coldchain.guardian.infra.persistence.entity.AlertEntity;
import com.coldchain.guardian.infra.persistence.repository.*;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

@Service
public class AIAssistantService {

    // 1. 解决“不建议使用字段注入”：全部改为 private final 并通过构造函数注入
    private final AiChatSessionRepository aiChatSessionRepository;
    private final AiChatMessageRepository aiChatMessageRepository;
    private final DeviceRepository deviceRepository;
    private final AlertRepository alertRepository;
    private final ChatClient chatClient;

    // 2. 移除未使用的 chatModel，使用标准的构造器注入
    public AIAssistantService(AiChatSessionRepository aiChatSessionRepository,
                              AiChatMessageRepository aiChatMessageRepository,
                              DeviceRepository deviceRepository,
                              AlertRepository alertRepository,
                              ChatClient.Builder chatClientBuilder) {
        this.aiChatSessionRepository = aiChatSessionRepository;
        this.aiChatMessageRepository = aiChatMessageRepository;
        this.deviceRepository = deviceRepository;
        this.alertRepository = alertRepository;
        this.chatClient = chatClientBuilder.build();
    }

    public Flux<String> streamChat(ChatRequestDto request) {
        // 3. 解决 Lambda 表达式变量 effectively final 的问题
        Long tempSessionId = request.getSessionId();
        if (tempSessionId == null) {
            AiChatSessionEntity session = new AiChatSessionEntity();
            session.setUserId(1L); // 此前硬编码，实际应该从当前用户获取
            session.setTitle(request.getMessage().length() > 50 ? request.getMessage().substring(0, 50) + "..." : request.getMessage());
            session.setIsDeleted(0);
            session.setCreateTime(LocalDateTime.now());
            session.setUpdateTime(LocalDateTime.now());
            aiChatSessionRepository.insert(session);
            tempSessionId = session.getId();
        }
        // 声明为 final，以便在 doOnComplete 闭包中安全使用
        final Long sessionId = tempSessionId;

        // 获取历史消息记录
        List<Message> historyMessages = getChatMessageHistory(sessionId);

        // 构建最终用户消息，包括附件上下文
        String finalUserMessage = buildFinalUserMessage(request);

        // 保存用户消息到数据库
        saveUserMessage(sessionId, request.getMessage(), request.getAttachmentType(), request.getAttachmentId());

        // Prepare the full prompt with context
        StringBuilder fullPromptBuilder = new StringBuilder();
        fullPromptBuilder.append("你是一个专业的冷链仓储安全AI参谋。你的回答需要专业、简明扼要，支持使用 Markdown 格式。\n\n");

        // Add historical messages
        for (Message msg : historyMessages) {
            fullPromptBuilder.append("Message: ").append(msg.toString()).append("\n");
        }

        // 4. 解决“finalUserMessage 从未使用”的警告
        fullPromptBuilder.append("用户问题: ").append(finalUserMessage);

        String fullPrompt = fullPromptBuilder.toString();

        AtomicReference<StringBuilder> aiResponseBuilder = new AtomicReference<>(new StringBuilder());

        return chatClient.prompt()
                .user(fullPrompt)
                .stream()
                .content()
                .doOnNext(chunk -> {
                    if (chunk != null) {
                        aiResponseBuilder.get().append(chunk);
                    }
                })
                // 5. 致命错误修复：替换掉了原本断掉的文本，补全完整的执行链
                .doOnComplete(() -> {
                    // 当流式传输完成时，将拼接好的完整内容存入数据库
                    saveAssistantMessage(sessionId, aiResponseBuilder.get().toString());
                })
                .doOnError(error -> {
                    System.err.println("流式输出发生异常: " + error.getMessage());
                    error.printStackTrace();
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
        updateSessionLastUpdated(sessionId);
    }

    // 6. 解决 "saveAssistantMessage 从未使用" 警告（已经在上方的 doOnComplete 中被调用）
    private void saveAssistantMessage(Long sessionId, String content) {
        AiChatMessageEntity message = new AiChatMessageEntity();
        message.setSessionId(sessionId);
        message.setRole("ASSISTANT");
        message.setContent(content);
        aiChatMessageRepository.insert(message);
        updateSessionLastUpdated(sessionId);
    }

    public List<AiChatSessionEntity> getSessionHistory(Long userId) {
        return aiChatSessionRepository.findByUserId(userId);
    }

    public List<AiChatMessageEntity> getChatMessagesBySessionId(Long sessionId) {
        return aiChatMessageRepository.findBySessionId(sessionId);
    }

    public AiChatSessionEntity createSession(AiChatSessionEntity session) {
        if (session.getUserId() == null) {
            session.setUserId(1L);
        }
        if (session.getCreateTime() == null) {
            session.setCreateTime(LocalDateTime.now());
        }
        if (session.getUpdateTime() == null) {
            session.setUpdateTime(LocalDateTime.now());
        }
        aiChatSessionRepository.insert(session);
        return session;
    }

    private void updateSessionLastUpdated(Long sessionId) {
        AiChatSessionEntity session = new AiChatSessionEntity();
        session.setId(sessionId);
        session.setUpdateTime(LocalDateTime.now());
        aiChatSessionRepository.updateById(session);
    }

    public void updateSession(AiChatSessionEntity session) {
        if (session.getUpdateTime() == null) {
            session.setUpdateTime(LocalDateTime.now());
        }
        aiChatSessionRepository.updateById(session);
    }

    public void deleteSession(Long id) {
        aiChatSessionRepository.removeById(id);
    }
}