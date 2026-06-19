package com.coldchain.guardian.app.service;

import com.coldchain.guardian.contract.dto.ai.ChatRequestDto;
import com.coldchain.guardian.infra.persistence.entity.AiChatMessageEntity;
import com.coldchain.guardian.infra.persistence.entity.AiChatSessionEntity;
import com.coldchain.guardian.infra.persistence.entity.AlertEntity;
import com.coldchain.guardian.infra.persistence.entity.AreaEntity;
import com.coldchain.guardian.infra.persistence.entity.DeviceEntity;
import com.coldchain.guardian.infra.persistence.entity.TelemetryEntity;
import com.coldchain.guardian.infra.persistence.entity.WorkOrderEntity;
import com.coldchain.guardian.infra.persistence.repository.AiChatMessageRepository;
import com.coldchain.guardian.infra.persistence.repository.AiChatSessionRepository;
import com.coldchain.guardian.infra.persistence.repository.AlertRepository;
import com.coldchain.guardian.infra.persistence.repository.AreaRepository;
import com.coldchain.guardian.infra.persistence.repository.DeviceRepository;
import com.coldchain.guardian.infra.persistence.repository.TelemetryRepository;
import com.coldchain.guardian.infra.persistence.repository.WorkOrderRepository;
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

    private static final int MAX_HISTORY_MESSAGES = 6;
    private static final int MAX_CONTEXT_ITEMS = 8;
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final AiChatSessionRepository aiChatSessionRepository;
    private final AiChatMessageRepository aiChatMessageRepository;
    private final DeviceRepository deviceRepository;
    private final AlertRepository alertRepository;
    private final WorkOrderRepository workOrderRepository;
    private final TelemetryRepository telemetryRepository;
    private final AreaRepository areaRepository;
    private final AiModelClient aiModelClient;

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
        ChatRequestDto safeRequest = request == null ? new ChatRequestDto() : request;
        String userQuestion = normalizeQuestion(safeRequest.getMessage());
        Long sessionId = ensureSession(safeRequest, userQuestion);

        String prompt = buildPrompt(safeRequest, sessionId, userQuestion);
        saveUserMessage(sessionId, userQuestion, safeRequest.getAttachmentType(), safeRequest.getAttachmentId());

        AtomicReference<StringBuilder> aiResponseBuilder = new AtomicReference<>(new StringBuilder());

        return Flux.defer(() -> Flux.fromIterable(splitForStreaming(aiModelClient.generate(prompt))))
                .subscribeOn(Schedulers.boundedElastic())
                .delayElements(Duration.ofMillis(18))
                .onErrorResume(error -> Flux.just(buildFallbackAnalysis(safeRequest, error)))
                .doOnNext(chunk -> {
                    if (chunk != null) {
                        aiResponseBuilder.get().append(chunk);
                    }
                })
                .doOnComplete(() -> saveAssistantMessage(sessionId, aiResponseBuilder.get().toString()))
                .doOnError(error -> {
                    System.err.println("AI assistant stream failed: " + error.getMessage());
                    error.printStackTrace();
                });
    }

    private Long ensureSession(ChatRequestDto request, String userQuestion) {
        if (request.getSessionId() != null) {
            return request.getSessionId();
        }

        AiChatSessionEntity session = new AiChatSessionEntity();
        session.setUserId(1L);
        session.setTitle(buildSessionTitle(userQuestion));
        session.setIsDeleted(0);
        session.setCreateTime(LocalDateTime.now());
        session.setUpdateTime(LocalDateTime.now());
        aiChatSessionRepository.insert(session);
        return session.getId();
    }

    private String buildPrompt(ChatRequestDto request, Long sessionId, String userQuestion) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("""
                你是 ColdChain Guardian 冷链仓储安全管理系统的 AI 智能助手。
                你的任务是基于系统数据库上下文，帮助管理员判断冷链风险、告警优先级和工单处置策略。

                回答要求：
                1. 必须使用中文，语气专业、简洁，适合毕业答辩现场演示。
                2. 只能基于下方系统数据回答，不要编造不存在的设备、库区、告警或工单。
                3. 优先输出以下 Markdown 结构：### 结论、### 数据依据、### 风险判断、### 处置建议、### 后续跟踪。
                4. 涉及设备、库区、告警、工单时，尽量引用真实编号、名称、状态和时间。
                5. 如果用户问题很短，也要结合系统快照给出可执行的运维建议。

                """);

        appendHistory(prompt, sessionId);
        prompt.append(buildFinalUserMessage(request, userQuestion));
        return prompt.toString();
    }

    private void appendHistory(StringBuilder prompt, Long sessionId) {
        List<AiChatMessageEntity> allMessages = getChatMessageHistory(sessionId);
        List<AiChatMessageEntity> cleanMessages = allMessages.stream()
                .filter(message -> !isLikelyMojibake(message.getContent()))
                .toList();
        List<AiChatMessageEntity> historyMessages = cleanMessages.stream()
                .skip(Math.max(0, cleanMessages.size() - MAX_HISTORY_MESSAGES))
                .toList();

        if (historyMessages.isEmpty()) {
            return;
        }

        prompt.append("【最近对话历史】\n");
        for (AiChatMessageEntity message : historyMessages) {
            prompt.append("- ")
                    .append(safe(message.getRole()))
                    .append(": ")
                    .append(trimText(safe(message.getContent()), 500))
                    .append("\n");
        }
        prompt.append("\n");
    }

    private List<AiChatMessageEntity> getChatMessageHistory(Long sessionId) {
        return aiChatMessageRepository.findBySessionId(sessionId);
    }

    private String buildFinalUserMessage(ChatRequestDto request, String userQuestion) {
        StringBuilder contextBuilder = new StringBuilder();
        contextBuilder.append(buildOperationalContext(request));

        if (request.getAttachmentType() != null && request.getAttachmentId() != null) {
            String attachmentContext = buildAttachmentContext(request);
            if (!attachmentContext.isBlank()) {
                contextBuilder.append("\n").append(attachmentContext).append("\n");
            }
        }

        contextBuilder.append("\n【用户问题】\n").append(userQuestion).append("\n");
        return contextBuilder.toString();
    }

    private String buildAttachmentContext(ChatRequestDto request) {
        if ("DEVICE".equals(request.getAttachmentType())) {
            DeviceEntity device = deviceRepository.findById(request.getAttachmentId());
            return device == null ? "" : formatDevice(device, buildAreaNameMap());
        }
        if ("ALERT".equals(request.getAttachmentType())) {
            AlertEntity alert = alertRepository.findById(request.getAttachmentId());
            return alert == null ? "" : "【关联告警】\n" + formatAlert(alert);
        }
        if ("WORK_ORDER".equals(request.getAttachmentType())) {
            WorkOrderEntity order = workOrderRepository.findById(request.getAttachmentId());
            return order == null ? "" : "【关联工单】\n" + formatWorkOrder(order);
        }
        return "";
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
        long highAlerts = alerts.stream().filter(alert -> "HIGH".equals(alert.getAlertLevel())).count();
        long handlingAlerts = alerts.stream().filter(alert -> "HANDLING".equals(alert.getStatus())).count();
        long pendingOrders = workOrders.stream().filter(order -> "PENDING".equals(order.getStatus())).count();
        long processingOrders = workOrders.stream().filter(order -> "PROCESSING".equals(order.getStatus())).count();
        long overdueOrders = workOrders.stream()
                .filter(order -> order.getDueDate() != null)
                .filter(order -> order.getDueDate().isBefore(LocalDateTime.now()))
                .filter(order -> !List.of("COMPLETED", "CLOSED").contains(order.getStatus()))
                .count();

        TelemetryEntity latestTelemetry = telemetry.stream()
                .filter(item -> item.getDataTime() != null)
                .max(Comparator.comparing(TelemetryEntity::getDataTime))
                .orElse(null);

        Map<String, Long> alertsByLevel = alerts.stream()
                .collect(Collectors.groupingBy(alert -> Optional.ofNullable(alert.getAlertLevel()).orElse("UNKNOWN"),
                        LinkedHashMap::new, Collectors.counting()));
        Map<String, Long> alertsByType = alerts.stream()
                .collect(Collectors.groupingBy(alert -> Optional.ofNullable(alert.getAlertType()).orElse("UNKNOWN"),
                        LinkedHashMap::new, Collectors.counting()));

        StringBuilder builder = new StringBuilder();
        builder.append("【系统运行快照】\n");
        builder.append("- 库区数量：").append(areas.size()).append("\n");
        builder.append("- 设备总数：").append(devices.size())
                .append("，在线：").append(online)
                .append("，离线：").append(offline).append("\n");
        builder.append("- 告警总数：").append(alerts.size())
                .append("，未处理：").append(unresolvedAlerts)
                .append("，处理中：").append(handlingAlerts)
                .append("，高危：").append(highAlerts)
                .append("，紧急：").append(criticalAlerts).append("\n");
        builder.append("- 工单总数：").append(workOrders.size())
                .append("，待处理：").append(pendingOrders)
                .append("，处理中：").append(processingOrders)
                .append("，逾期：").append(overdueOrders).append("\n");
        builder.append("- 遥测记录数：").append(telemetry.size());
        if (latestTelemetry != null) {
            builder.append("，最新样本：设备ID ").append(latestTelemetry.getDeviceId())
                    .append("，温度 ").append(formatNullable(latestTelemetry.getTemperature()))
                    .append(" C，湿度 ").append(formatNullable(latestTelemetry.getHumidity()))
                    .append(" %RH，时间 ").append(formatTime(latestTelemetry.getDataTime()));
        }
        builder.append("\n");
        builder.append("- 告警级别分布：").append(alertsByLevel).append("\n");
        builder.append("- 告警类型分布：").append(alertsByType).append("\n");

        builder.append("\n【重点离线或异常设备】\n");
        devices.stream()
                .filter(device -> Integer.valueOf(0).equals(device.getOnlineStatus())
                        || Integer.valueOf(1).equals(device.getHasUnresolvedAlert()))
                .limit(MAX_CONTEXT_ITEMS)
                .forEach(device -> builder.append(formatDevice(device, areaNames)).append("\n"));

        builder.append("\n【最近告警】\n");
        alerts.stream()
                .sorted(Comparator.comparing(this::getAlertTime, Comparator.nullsLast(Comparator.naturalOrder())).reversed())
                .limit(MAX_CONTEXT_ITEMS)
                .forEach(alert -> builder.append(formatAlert(alert)).append("\n"));

        builder.append("\n【待办工单】\n");
        workOrders.stream()
                .filter(order -> List.of("PENDING", "PROCESSING", "VERIFYING").contains(order.getStatus()))
                .sorted(Comparator.comparing(WorkOrderEntity::getDueDate, Comparator.nullsLast(Comparator.naturalOrder())))
                .limit(MAX_CONTEXT_ITEMS)
                .forEach(order -> builder.append(formatWorkOrder(order)).append("\n"));

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

    public void updateSession(AiChatSessionEntity session) {
        if (session.getUpdateTime() == null) {
            session.setUpdateTime(LocalDateTime.now());
        }
        aiChatSessionRepository.updateById(session);
    }

    public void deleteSession(Long id) {
        aiChatSessionRepository.removeById(id);
    }

    private void updateSessionLastUpdated(Long sessionId) {
        AiChatSessionEntity session = new AiChatSessionEntity();
        session.setId(sessionId);
        session.setUpdateTime(LocalDateTime.now());
        aiChatSessionRepository.updateById(session);
    }

    private String buildFallbackAnalysis(ChatRequestDto request, Throwable error) {
        String context = buildOperationalContext(request);
        String reason = error == null ? "模型服务暂时不可用" : safe(error.getMessage());
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
                - 对最新遥测缺失或长时间离线的设备做现场核查，确认电源、网络和探头位置。

                ### 后续跟踪
                - 模型调用失败原因：%s
                - 请检查服务器模型地址、API Key、模型名、账号余额和第三方服务通道状态。
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

    private Map<Long, String> buildAreaNameMap() {
        return areaRepository.findAll().stream()
                .collect(Collectors.toMap(AreaEntity::getId, AreaEntity::getAreaName, (a, b) -> a));
    }

    private LocalDateTime getAlertTime(AlertEntity alert) {
        if (alert.getFirstTime() != null) {
            return alert.getFirstTime();
        }
        if (alert.getCreateTime() != null) {
            return alert.getCreateTime();
        }
        return alert.getLastTime();
    }

    private String formatDevice(DeviceEntity device, Map<Long, String> areaNames) {
        return "- 设备 " + safe(device.getDeviceName())
                + "（" + safe(device.getDeviceCode()) + "，" + safe(device.getDeviceType()) + "）"
                + "，库区：" + areaNames.getOrDefault(device.getAreaId(), "未知库区")
                + "，状态：" + (Integer.valueOf(1).equals(device.getOnlineStatus()) ? "在线" : "离线")
                + "，未处理告警：" + (Integer.valueOf(1).equals(device.getHasUnresolvedAlert()) ? "有" : "无")
                + "，最新温度：" + formatNullable(device.getLatestTemp())
                + "，最新湿度：" + formatNullable(device.getLatestHumi())
                + "，最后上报：" + formatTime(device.getLatestDataTime());
    }

    private String formatAlert(AlertEntity alert) {
        return "- 告警 #" + alert.getId()
                + "，消息：" + safe(alert.getMessage())
                + "，级别：" + safe(alert.getAlertLevel())
                + "，类型：" + safe(alert.getAlertType())
                + "，状态：" + safe(alert.getStatus())
                + "，设备ID：" + alert.getDeviceId()
                + "，库区ID：" + alert.getWarehouseId()
                + "，温度：" + formatNullable(alert.getTemperature())
                + "，湿度：" + formatNullable(alert.getHumidity())
                + "，发生时间：" + formatTime(getAlertTime(alert));
    }

    private String formatWorkOrder(WorkOrderEntity order) {
        return "- 工单 " + safe(order.getOrderNo())
                + "，标题：" + safe(order.getTitle())
                + "，优先级：" + safe(order.getPriority())
                + "，状态：" + safe(order.getStatus())
                + "，类型：" + safe(order.getWorkType())
                + "，设备ID：" + order.getDeviceId()
                + "，库区ID：" + order.getWarehouseId()
                + "，责任人ID：" + order.getAssigneeId()
                + "，截止时间：" + formatTime(order.getDueDate());
    }

    private String normalizeQuestion(String question) {
        String normalized = safe(question).trim();
        if (normalized.isBlank() || "-".equals(normalized)) {
            return "请分析当前冷链系统的主要风险，并给出处置建议。";
        }
        return normalized;
    }

    private String buildSessionTitle(String question) {
        String title = normalizeQuestion(question);
        return title.length() > 50 ? title.substring(0, 50) + "..." : title;
    }

    private boolean isLikelyMojibake(String value) {
        if (value == null || value.isBlank()) {
            return false;
        }
        String markers = "锛鐨鍗涓妯瀷绌搴撳尯鍛婅璀浣犳槸";
        long count = value.chars()
                .filter(ch -> markers.indexOf(ch) >= 0)
                .count();
        return count >= 8;
    }

    private String trimText(String value, int maxLength) {
        if (value == null || value.length() <= maxLength) {
            return safe(value);
        }
        return value.substring(0, maxLength) + "...";
    }

    private String formatTime(LocalDateTime time) {
        return time == null ? "-" : time.format(TIME_FORMATTER);
    }

    private String formatNullable(BigDecimal value) {
        return value == null ? "-" : value.stripTrailingZeros().toPlainString();
    }

    private String formatNullable(Double value) {
        if (value == null || !Double.isFinite(value)) {
            return "-";
        }
        return String.format("%.2f", value);
    }

    private String safe(String value) {
        return Objects.toString(value, "-");
    }
}
