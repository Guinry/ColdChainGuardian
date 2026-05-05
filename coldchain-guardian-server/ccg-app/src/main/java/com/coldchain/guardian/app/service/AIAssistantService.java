package com.coldchain.guardian.app.service;

import com.coldchain.guardian.contract.dto.ai.ChatRequestDto;
import com.coldchain.guardian.infra.persistence.entity.AiChatMessageEntity;
import com.coldchain.guardian.infra.persistence.entity.AiChatSessionEntity;
import com.coldchain.guardian.infra.persistence.entity.AreaEntity;
import com.coldchain.guardian.infra.persistence.entity.AlertEntity;
import com.coldchain.guardian.infra.persistence.entity.DeviceEntity;
import com.coldchain.guardian.infra.persistence.entity.TelemetryEntity;
import com.coldchain.guardian.infra.persistence.entity.WorkOrderEntity;
import com.coldchain.guardian.infra.persistence.repository.*;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

@Service
public class AIAssistantService {

    // 1. 解决“不建议使用字段注入”：全部改为 private final 并通过构造函数注入
    private final AiChatSessionRepository aiChatSessionRepository;
    private final AiChatMessageRepository aiChatMessageRepository;
    private final DeviceRepository deviceRepository;
    private final AlertRepository alertRepository;
    private final WorkOrderRepository workOrderRepository;
    private final TelemetryRepository telemetryRepository;
    private final AreaRepository areaRepository;
    private final AiModelClient aiModelClient;

    // 2. 使用显式模型适配层，避免第三方兼容协议被 Spring AI 默认端点误判
    public AIAssistantService(AiChatSessionRepository aiChatSessionRepository,
                              AiChatMessageRepository aiChatMessageRepository,
                              DeviceRepository deviceRepository,
                              AlertRepository alertRepository,
                              WorkOrderRepository workOrderRepository,
                              TelemetryRepository telemetryRepository,
                              AreaRepository areaRepository,
                              AiModelClient aiModelClient) {
        this.aiChatSessionRepository = aiChatSessionRepository;
        this.aiChatMessageRepository = aiChatMessageRepository;
        this.deviceRepository = deviceRepository;
        this.alertRepository = alertRepository;
        this.workOrderRepository = workOrderRepository;
        this.telemetryRepository = telemetryRepository;
        this.areaRepository = areaRepository;
        this.aiModelClient = aiModelClient;
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
        List<AiChatMessageEntity> historyMessages = getChatMessageHistory(sessionId);

        // 构建最终用户消息，包括附件上下文
        String finalUserMessage = buildFinalUserMessage(request);

        // 保存用户消息到数据库
        saveUserMessage(sessionId, request.getMessage(), request.getAttachmentType(), request.getAttachmentId());

        // Prepare the full prompt with context
        StringBuilder fullPromptBuilder = new StringBuilder();
        fullPromptBuilder.append("""
                你是 ColdChain Guardian 的冷链运维 AI 助手。
                你必须基于系统提供的数据库上下文回答，不要编造不存在的设备、告警或工单。
                回答使用清晰 Markdown，优先按以下格式输出：
                ### 结论
                ### 数据依据
                ### 风险判断
                ### 处置建议
                ### 后续跟踪
                数字、设备名、库区、工单号需要尽量引用上下文中的真实值。

                """);

        // Add historical messages
        for (AiChatMessageEntity msg : historyMessages) {
            fullPromptBuilder
                    .append("历史消息(")
                    .append(msg.getRole())
                    .append("): ")
                    .append(safe(msg.getContent()))
                    .append("\n");
        }

        // 4. 解决“finalUserMessage 从未使用”的警告
        fullPromptBuilder.append("用户问题: ").append(finalUserMessage);

        String fullPrompt = fullPromptBuilder.toString();

        AtomicReference<StringBuilder> aiResponseBuilder = new AtomicReference<>(new StringBuilder());

        return Flux.defer(() -> Flux.fromIterable(splitForStreaming(aiModelClient.generate(fullPrompt))))
                .subscribeOn(Schedulers.boundedElastic())
                .delayElements(Duration.ofMillis(18))
                .onErrorResume(error -> Flux.just(buildFallbackAnalysis(request, error)))
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

    private List<AiChatMessageEntity> getChatMessageHistory(Long sessionId) {
        return aiChatMessageRepository.findBySessionId(sessionId);
    }

    private String buildFinalUserMessage(ChatRequestDto request) {
        String userMessage = request.getMessage();
        StringBuilder contextBuilder = new StringBuilder();
        contextBuilder.append(buildOperationalContext(request));

        if (request.getAttachmentType() != null && request.getAttachmentId() != null) {
            String context = "";

            if ("DEVICE".equals(request.getAttachmentType())) {
                DeviceEntity device = deviceRepository.findById(request.getAttachmentId());
                if (device != null) {
                    context = String.format("""
                                    【关联设备】
                                    - 设备：%s（%s）
                                    - 状态：%s
                                    - 位置：%s
                                    - 最新温度：%s
                                    - 最新湿度：%s
                                    - 最后上报：%s
                                    """,
                            device.getDeviceName(), device.getDeviceCode(),
                            device.getOnlineStatus() != null && device.getOnlineStatus() == 1 ? "在线" : "离线",
                            device.getLocationDesc(),
                            formatNullable(device.getLatestTemp()), formatNullable(device.getLatestHumi()),
                            formatTime(device.getLatestDataTime())
                    );
                }
            } else if ("ALERT".equals(request.getAttachmentType())) {
                AlertEntity alert = alertRepository.findById(request.getAttachmentId());
                if (alert != null) {
                    context = formatAlert(alert);
                }
            } else if ("WORK_ORDER".equals(request.getAttachmentType())) {
                WorkOrderEntity order = workOrderRepository.findById(request.getAttachmentId());
                if (order != null) {
                    context = formatWorkOrder(order);
                }
            }
            contextBuilder.append("\n").append(context).append("\n");
        }

        return contextBuilder + "\n用户的实际问题是：" + userMessage;
    }

    private String buildOperationalContext(ChatRequestDto request) {
        List<DeviceEntity> devices = deviceRepository.findAll();
        List<AlertEntity> alerts = alertRepository.findAll();
        List<WorkOrderEntity> workOrders = workOrderRepository.findAll();
        List<TelemetryEntity> telemetry = telemetryRepository.findAll();
        List<AreaEntity> areas = areaRepository.findAll();

        Map<Long, String> areaNames = areas.stream()
                .collect(Collectors.toMap(AreaEntity::getId, AreaEntity::getAreaName, (a, b) -> a));

        long online = devices.stream().filter(device -> Integer.valueOf(1).equals(device.getOnlineStatus())).count();
        long offline = devices.size() - online;
        long unresolvedAlerts = alerts.stream().filter(alert -> "UNHANDLED".equals(alert.getStatus())).count();
        long criticalAlerts = alerts.stream().filter(alert -> "CRITICAL".equals(alert.getAlertLevel())).count();
        long handlingAlerts = alerts.stream().filter(alert -> "HANDLING".equals(alert.getStatus())).count();
        long pendingOrders = workOrders.stream().filter(order -> "PENDING".equals(order.getStatus())).count();
        long processingOrders = workOrders.stream().filter(order -> "PROCESSING".equals(order.getStatus())).count();
        long overdueOrders = workOrders.stream()
                .filter(order -> order.getDueDate() != null)
                .filter(order -> order.getDueDate().isBefore(LocalDateTime.now()))
                .filter(order -> !List.of("COMPLETED", "CLOSED").contains(order.getStatus()))
                .count();

        Double avgTemp = telemetry.stream()
                .filter(item -> item.getTemperature() != null)
                .max(Comparator.comparing(TelemetryEntity::getDataTime, Comparator.nullsLast(Comparator.naturalOrder())))
                .map(TelemetryEntity::getTemperature)
                .orElse(null);

        List<AlertEntity> topAlerts = alerts.stream()
                .sorted(Comparator.comparing(this::getAlertTime, Comparator.nullsLast(Comparator.naturalOrder())).reversed())
                .limit(8)
                .toList();

        List<WorkOrderEntity> topOrders = workOrders.stream()
                .filter(order -> List.of("PENDING", "PROCESSING", "VERIFYING").contains(order.getStatus()))
                .sorted(Comparator.comparing(WorkOrderEntity::getDueDate, Comparator.nullsLast(Comparator.naturalOrder())))
                .limit(8)
                .toList();

        Map<String, Long> alertsByLevel = alerts.stream()
                .collect(Collectors.groupingBy(alert -> Optional.ofNullable(alert.getAlertLevel()).orElse("UNKNOWN"), LinkedHashMap::new, Collectors.counting()));

        Map<String, Long> alertsByType = alerts.stream()
                .collect(Collectors.groupingBy(alert -> Optional.ofNullable(alert.getAlertType()).orElse("UNKNOWN"), LinkedHashMap::new, Collectors.counting()));

        StringBuilder builder = new StringBuilder();
        builder.append("【数据库运行快照】\n");
        builder.append("- 库区数量：").append(areas.size()).append("\n");
        builder.append("- 设备总数：").append(devices.size()).append("，在线：").append(online).append("，离线：").append(offline).append("\n");
        builder.append("- 告警总数：").append(alerts.size()).append("，未处理：").append(unresolvedAlerts)
                .append("，处理中：").append(handlingAlerts).append("，紧急：").append(criticalAlerts).append("\n");
        builder.append("- 工单总数：").append(workOrders.size()).append("，待处理：").append(pendingOrders)
                .append("，处理中：").append(processingOrders).append("，逾期：").append(overdueOrders).append("\n");
        builder.append("- 遥测记录数：").append(telemetry.size()).append("，最新样本温度：").append(formatNullable(avgTemp)).append("\n");
        builder.append("- 告警级别分布：").append(alertsByLevel).append("\n");
        builder.append("- 告警类型分布：").append(alertsByType).append("\n\n");

        builder.append("【重点离线/异常设备】\n");
        devices.stream()
                .filter(device -> Integer.valueOf(0).equals(device.getOnlineStatus()) || Integer.valueOf(1).equals(device.getHasUnresolvedAlert()))
                .limit(8)
                .forEach(device -> builder.append("- ")
                        .append(device.getDeviceName()).append("（").append(device.getDeviceCode()).append("，")
                        .append(areaNames.getOrDefault(device.getAreaId(), "未知库区")).append("）：")
                        .append(Integer.valueOf(1).equals(device.getOnlineStatus()) ? "在线" : "离线")
                        .append("，未处理告警：").append(Integer.valueOf(1).equals(device.getHasUnresolvedAlert()) ? "有" : "无")
                        .append("，最新温度：").append(formatNullable(device.getLatestTemp()))
                        .append("，最新湿度：").append(formatNullable(device.getLatestHumi()))
                        .append("\n"));

        builder.append("\n【最近告警】\n");
        topAlerts.forEach(alert -> builder.append(formatAlert(alert)).append("\n"));

        builder.append("\n【待办工单】\n");
        topOrders.forEach(order -> builder.append(formatWorkOrder(order)).append("\n"));

        if (request.getContextTypes() != null && !request.getContextTypes().isEmpty()) {
            builder.append("\n【用户勾选上下文】").append(request.getContextTypes()).append("\n");
        }

        return builder.toString();
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

    private String buildFallbackAnalysis(ChatRequestDto request, Throwable error) {
        String context = buildOperationalContext(request);
        String reason = error == null ? "模型服务暂不可用" : error.getMessage();
        return """
                ### 结论
                当前已从数据库读取运行数据，但外部模型服务暂时不可用，以下为系统规则分析结果。

                ### 数据依据
                %s

                ### 风险判断
                - 优先关注未处理告警、紧急告警、离线设备和逾期工单。
                - 如果同一库区同时出现温度异常和设备离线，建议按网关、供电、制冷机组和传感器安装位置顺序排查。

                ### 处置建议
                - 先处理 CRITICAL / HIGH 告警，再处理 DEVICE_OFFLINE 类告警。
                - 将未处理告警转成工单并指定责任人，逾期工单需要值班管理员跟进。
                - 对最新遥测缺失的设备做现场核查，确认电源、网络和探头位置。

                ### 后续跟踪
                - 模型调用失败原因：%s
                - 建议检查服务端 `SPRING_AI_OPENAI_API_KEY`、`SPRING_AI_OPENAI_BASE_URL`、`SPRING_AI_OPENAI_CHAT_MODEL`，以及第三方服务分组、模型名和 User-Agent 配置。
                """.formatted(context, reason);
    }

    private List<String> splitForStreaming(String content) {
        if (content == null || content.isBlank()) {
            return List.of("模型服务未返回有效内容。");
        }

        List<String> chunks = new ArrayList<>();
        int chunkSize = 32;
        for (int i = 0; i < content.length(); i += chunkSize) {
            chunks.add(content.substring(i, Math.min(content.length(), i + chunkSize)));
        }
        return chunks;
    }

    private LocalDateTime getAlertTime(AlertEntity alert) {
        if (alert.getFirstTime() != null) return alert.getFirstTime();
        if (alert.getCreateTime() != null) return alert.getCreateTime();
        return alert.getLastTime();
    }

    private String formatAlert(AlertEntity alert) {
        return "- 告警 #" + alert.getId()
                + "：" + safe(alert.getMessage())
                + "，级别：" + safe(alert.getAlertLevel())
                + "，类型：" + safe(alert.getAlertType())
                + "，状态：" + safe(alert.getStatus())
                + "，设备ID：" + alert.getDeviceId()
                + "，库区ID：" + alert.getWarehouseId()
                + "，发生时间：" + formatTime(getAlertTime(alert));
    }

    private String formatWorkOrder(WorkOrderEntity order) {
        return "- 工单 " + safe(order.getOrderNo())
                + "：" + safe(order.getTitle())
                + "，优先级：" + safe(order.getPriority())
                + "，状态：" + safe(order.getStatus())
                + "，类型：" + safe(order.getWorkType())
                + "，设备ID：" + order.getDeviceId()
                + "，库区ID：" + order.getWarehouseId()
                + "，责任人ID：" + order.getAssigneeId()
                + "，截止：" + formatTime(order.getDueDate());
    }

    private String formatTime(LocalDateTime time) {
        if (time == null) return "-";
        return time.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
    }

    private String formatNullable(BigDecimal value) {
        return value == null ? "-" : value.stripTrailingZeros().toPlainString();
    }

    private String formatNullable(Double value) {
        if (value == null || !Double.isFinite(value)) return "-";
        return String.format("%.2f", value);
    }

    private String safe(String value) {
        return Objects.toString(value, "-");
    }
}
