package com.coldchain.guardian.app.service;

import com.coldchain.guardian.contract.dto.alert.AlertDto;
import com.coldchain.guardian.contract.dto.alert.CreateAlertRequestDto;
import com.coldchain.guardian.contract.dto.workorder.CreateWorkOrderRequestDto;
import com.coldchain.guardian.contract.dto.workorder.WorkOrderDto;
import com.coldchain.guardian.infra.persistence.entity.AlertEntity;
import com.coldchain.guardian.infra.persistence.repository.AlertRepository;
import com.coldchain.guardian.infra.persistence.repository.DeviceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AlertService {

    @Autowired
    private AlertRepository alertRepository;

    @Autowired
    private DeviceRepository deviceRepository;

    @Autowired
    private WorkOrderService workOrderService;

    /**
     * 根据设备ID获取告警列表（分页）
     */
    public List<AlertDto> getAlertsByDeviceId(Long deviceId, Integer page, Integer size, String alertType, String alertLevel, String status, String startTime, String endTime) {
        LocalDateTime startDateTime = null;
        LocalDateTime endDateTime = null;

        if (startTime != null && !startTime.isEmpty()) {
            startDateTime = LocalDateTime.parse(startTime.replace("Z", ""), DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        }
        if (endTime != null && !endTime.isEmpty()) {
            endDateTime = LocalDateTime.parse(endTime.replace("Z", ""), DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        }

        List<AlertEntity> entities = alertRepository.findByDeviceId(deviceId, page, size, alertType, alertLevel, status, startDateTime, endDateTime);

        return entities.stream()
                .map(this::convertToAlertDto)
                .collect(Collectors.toList());
    }

    /**
     * 根据设备ID获取告警总数
     */
    public long countAlertsByDeviceId(Long deviceId, String alertType, String alertLevel, String status, String startTime, String endTime) {
        LocalDateTime startDateTime = null;
        LocalDateTime endDateTime = null;

        if (startTime != null && !startTime.isEmpty()) {
            startDateTime = LocalDateTime.parse(startTime.replace("Z", ""), DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        }
        if (endTime != null && !endTime.isEmpty()) {
            endDateTime = LocalDateTime.parse(endTime.replace("Z", ""), DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        }

        return alertRepository.countByDeviceId(deviceId, alertType, alertLevel, status, startDateTime, endDateTime);
    }

    /**
     * 根据ID获取告警详情
     */
    public AlertDto getAlertById(Long id) {
        AlertEntity entity = alertRepository.findById(id);
        return entity != null ? convertToAlertDto(entity) : null;
    }

    /**
     * 创建告警
     */
    public AlertDto createAlert(CreateAlertRequestDto requestDto) {
        AlertEntity entity = new AlertEntity();
        entity.setDeviceId(requestDto.getDeviceId() != null ? Long.valueOf(requestDto.getDeviceId()) : null);
        entity.setDeviceName(requestDto.getDeviceId()); // 使用设备ID作为设备名称，或者可以从设备表查询真正的名称
        entity.setAlertType(requestDto.getAlertType());
        entity.setMessage(requestDto.getDescription()); // 映射description到message

        // 根据severityLevel设置alertLevel
        if (requestDto.getSeverityLevel() != null) {
            switch (requestDto.getSeverityLevel()) {
                case 1:
                    entity.setAlertLevel("LOW");
                    break;
                case 2:
                    entity.setAlertLevel("MEDIUM");
                    break;
                case 3:
                    entity.setAlertLevel("HIGH");
                    break;
                case 4:
                    entity.setAlertLevel("CRITICAL");
                    break;
                default:
                    entity.setAlertLevel("MEDIUM");
            }
        }

        entity.setFirstTime(LocalDateTime.now());  // 设置首次触发时间
        entity.setLastTime(LocalDateTime.now());   // 设置最后触发时间
        entity.setTriggerCount(1);                 // 初始化触发次数
        entity.setStatus("UNHANDLED");             // 默认未处理状态

        alertRepository.save(entity);

        return convertToAlertDto(entity);
    }

    /**
     * 更新告警状态
     */
    public AlertDto updateAlertStatus(Long id, String status, String handleRemark) {
        AlertEntity entity = alertRepository.findById(id);
        if (entity == null) {
            return null;
        }

        entity.setStatus(status);
        entity.setHandleTime(LocalDateTime.now());
        entity.setHandleRemark(handleRemark);

        if ("RESOLVED".equals(status)) {
            entity.setUpdateTime(LocalDateTime.now());
        }

        alertRepository.save(entity);

        return convertToAlertDto(entity);
    }

    /**
     * 批量更新告警状态
     */
    public void batchUpdateAlertStatus(List<Long> ids, String status, String handleRemark) {
        for (Long id : ids) {
            updateAlertStatus(id, status, handleRemark);
        }
    }

    /**
     * 删除告警
     */
    public void deleteAlert(Long id) {
        AlertEntity entity = alertRepository.findById(id);
        if (entity != null) {
            alertRepository.deleteById(id);
        }
    }

    /**
     * 获取告警统计数据
     */
    public Object getAlertStatistics(String startTime, String endTime, Long deviceId) {
        LocalDateTime startDateTime = null;
        LocalDateTime endDateTime = null;

        if (startTime != null && !startTime.isEmpty()) {
            startDateTime = LocalDateTime.parse(startTime.replace("Z", ""), DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        }
        if (endTime != null && !endTime.isEmpty()) {
            endDateTime = LocalDateTime.parse(endTime.replace("Z", ""), DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        }

        // 获取总数统计
        long totalCount = alertRepository.countAllByConditions(deviceId, null, null, startDateTime, endDateTime);

        // 获取按级别分类的统计
        long criticalCount = alertRepository.countByConditions(deviceId, null, "CRITICAL", startDateTime, endDateTime);
        long highCount = alertRepository.countByConditions(deviceId, null, "HIGH", startDateTime, endDateTime);
        long mediumCount = alertRepository.countByConditions(deviceId, null, "MEDIUM", startDateTime, endDateTime);
        long lowCount = alertRepository.countByConditions(deviceId, null, "LOW", startDateTime, endDateTime);

        // 获取按状态分类的统计
        long unhandledCount = alertRepository.countByConditions(deviceId, "UNHANDLED", null, startDateTime, endDateTime);
        long handlingCount = alertRepository.countByConditions(deviceId, "HANDLING", null, startDateTime, endDateTime);
        long resolvedCount = alertRepository.countByConditions(deviceId, "RESOLVED", null, startDateTime, endDateTime);
        long ignoredCount = alertRepository.countByConditions(deviceId, "IGNORED", null, startDateTime, endDateTime);

        // 构建统计对象
        AlertStats stats = new AlertStats();
        stats.setTotalCount(totalCount);
        stats.setCriticalCount(criticalCount);
        stats.setHighCount(highCount);
        stats.setMediumCount(mediumCount);
        stats.setLowCount(lowCount);
        stats.setUnhandledCount(unhandledCount);
        stats.setHandlingCount(handlingCount);
        stats.setResolvedCount(resolvedCount);
        stats.setIgnoredCount(ignoredCount);

        return stats;
    }

    /**
     * 搜索告警（多维度）
     */
    public List<AlertDto> searchAlerts(String keyword, String location, String level, String status,
                                      String startTime, String endTime, Integer page, Integer size) {
        LocalDateTime startDateTime = null;
        LocalDateTime endDateTime = null;

        if (startTime != null && !startTime.isEmpty()) {
            startDateTime = LocalDateTime.parse(startTime.replace("Z", ""), DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        }
        if (endTime != null && !endTime.isEmpty()) {
            endDateTime = LocalDateTime.parse(endTime.replace("Z", ""), DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        }

        // 使用现有的repository方法，传入所有过滤条件
        // 我们不能直接使用null的deviceId，而是需要根据location查找
        List<AlertEntity> entities = alertRepository.findByDeviceId(
            null, page, size, null, level, status, startDateTime, endDateTime);

        // 对keyword和location进行内存中的过滤
        List<AlertEntity> filteredEntities = entities.stream()
            .filter(alert -> keyword == null || keyword.isEmpty() ||
                           (alert.getMessage() != null && alert.getMessage().contains(keyword)) ||
                           (alert.getDeviceName() != null && alert.getDeviceName().contains(keyword)))
            .filter(alert -> location == null || location.isEmpty() ||
                           (alert.getAreaName() != null && alert.getAreaName().contains(location)) ||
                           (alert.getDeviceName() != null && alert.getDeviceName().contains(location)))
            .collect(Collectors.toList());

        return filteredEntities.stream()
                .map(this::convertToAlertDto)
                .collect(Collectors.toList());
    }

    /**
     * 获取告警总数量（用于分页计算）
     */
    public long getTotalAlertCount(String keyword, String location, String level, String status,
                                  String startTime, String endTime) {
        LocalDateTime startDateTime = null;
        LocalDateTime endDateTime = null;

        if (startTime != null && !startTime.isEmpty()) {
            startDateTime = LocalDateTime.parse(startTime.replace("Z", ""), DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        }
        if (endTime != null && !endTime.isEmpty()) {
            endDateTime = LocalDateTime.parse(endTime.replace("Z", ""), DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        }

        // 如果提供了具体的位置（库区/设备），我们可能需要先从其他地方获取对应的设备ID
        Long deviceId = null;
        if (location != null && !location.isEmpty()) {
            // 在实际实现中，这里需要从库区/设备名称查找ID
            // 目前暂时设为null，这将导致忽略位置筛选
            // 未来可以扩展设备服务来提供此功能
        }

        // 使用repository的计数方法
        long totalCount = alertRepository.countByDeviceId(
            deviceId, null, level, status, startDateTime, endDateTime);

        // 对keyword进行内存中的计数
        if (keyword != null && !keyword.isEmpty()) {
            // 由于我们无法直接在数据库中进行关键词搜索，需要先获取所有匹配的记录
            List<AlertEntity> entities = alertRepository.findByDeviceId(
                deviceId, 1, (int)totalCount, null, level, status, startDateTime, endDateTime);

            return entities.stream()
                .filter(alert -> keyword == null || keyword.isEmpty() ||
                               (alert.getMessage() != null && alert.getMessage().contains(keyword)) ||
                               (alert.getDeviceName() != null && alert.getDeviceName().contains(keyword)))
                .filter(alert -> location == null || location.isEmpty() ||
                               (alert.getAreaName() != null && alert.getAreaName().contains(location)) ||
                               (alert.getDeviceName() != null && alert.getDeviceName().contains(location)))
                .count();
        } else if (location != null && !location.isEmpty()) {
            // 如果只有位置筛选，也需要在内存中过滤
            List<AlertEntity> entities = alertRepository.findByDeviceId(
                deviceId, 1, (int)totalCount, null, level, status, startDateTime, endDateTime);

            return entities.stream()
                .filter(alert -> location == null || location.isEmpty() ||
                               (alert.getAreaName() != null && alert.getAreaName().contains(location)) ||
                               (alert.getDeviceName() != null && alert.getDeviceName().contains(location)))
                .count();
        }

        return totalCount;
    }

    /**
     * 将告警转为工单
     */
    public WorkOrderDto convertAlertToWorkOrder(Long alertId, Long assigneeId, String assigneeName, String description) {
        AlertEntity alert = alertRepository.findById(alertId);
        if (alert == null) {
            return null;
        }

        // Create work order from alert
        CreateWorkOrderRequestDto workOrderRequest = new CreateWorkOrderRequestDto();
        workOrderRequest.setTitle("告警处理工单 - " + alert.getAlertType());
        workOrderRequest.setDescription(description != null ? description : "处理来自告警的工单: " + alert.getMessage());
        workOrderRequest.setPriority(convertAlertLevelToPriority(alert.getAlertLevel()));
        workOrderRequest.setAssigneeId(assigneeId);
        workOrderRequest.setReporterId(1L); // System or admin user
        workOrderRequest.setAlertId(alertId);

        // Update alert status to HANDLING
        alert.setStatus("HANDLING");
        alert.setHandleTime(LocalDateTime.now());
        alertRepository.save(alert);

        return workOrderService.createWorkOrder(workOrderRequest);
    }

    /**
     * 批量将告警转为工单
     */
    public List<WorkOrderDto> batchConvertAlertsToWorkOrders(List<Long> alertIds, Long assigneeId, String assigneeName, String description) {
        return alertIds.stream()
                .map(alertId -> convertAlertToWorkOrder(alertId, assigneeId, assigneeName,
                      description != null ? description + " (批量创建)" : "批量处理告警工单"))
                .collect(Collectors.toList());
    }

    /**
     * 获取紧急告警列表（未处理的紧急和高危告警）
     */
    public List<AlertDto> getUrgentAlerts() {
        List<AlertEntity> urgentAlerts = alertRepository.findUrgentAlerts();
        return urgentAlerts.stream()
                .map(this::convertToAlertDto)
                .collect(Collectors.toList());
    }

    /**
     * Helper method to convert alert level to priority
     */
    private String convertAlertLevelToPriority(String alertLevel) {
        switch (alertLevel) {
            case "CRITICAL":
                return "URGENT";
            case "HIGH":
                return "HIGH";
            case "MEDIUM":
                return "MEDIUM";
            case "LOW":
                return "LOW";
            default:
                return "MEDIUM";
        }
    }

    private AlertDto convertToAlertDto(AlertEntity entity) {
        AlertDto dto = new AlertDto();
        dto.setId(entity.getId());
        // 将Long类型的deviceId转换为String
        dto.setDeviceId(entity.getDeviceId() != null ? entity.getDeviceId().toString() : null);
        dto.setDeviceName(entity.getDeviceName());
        dto.setAlertType(entity.getAlertType());
        dto.setDescription(entity.getMessage()); // message映射到description

        // 将AlertEntity中的alertLevel映射到AlertDto中的severityLevel
        if (entity.getAlertLevel() != null) {
            switch (entity.getAlertLevel().toUpperCase()) {
                case "LOW":
                    dto.setSeverityLevel(1);
                    break;
                case "MEDIUM":
                    dto.setSeverityLevel(2);
                    break;
                case "HIGH":
                    dto.setSeverityLevel(3);
                    break;
                case "CRITICAL":
                    dto.setSeverityLevel(4); // critical比high更重要
                    break;
                default:
                    dto.setSeverityLevel(1);
            }
        }

        // 将AlertEntity中的status映射到AlertDto中的isResolved
        if (entity.getStatus() != null) {
            dto.setResolved("RESOLVED".equalsIgnoreCase(entity.getStatus()) ||
                           "IGNORED".equalsIgnoreCase(entity.getStatus()));
        }

        // 将LocalDateTime类型的firstTime转换为Long类型的createdAt（毫秒时间戳）
        if (entity.getFirstTime() != null) {
            dto.setCreatedAt(entity.getFirstTime().atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli());
        }

        // 将LocalDateTime类型的updateTime转换为Long类型的resolvedAt（毫秒时间戳）
        if (entity.getUpdateTime() != null) {
            dto.setResolvedAt(entity.getUpdateTime().atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli());
        }

        return dto;
    }

    // 内部统计类
    public static class AlertStats {
        private long totalCount;
        private long criticalCount;
        private long highCount;
        private long mediumCount;
        private long lowCount;
        private long unhandledCount;
        private long handlingCount;
        private long resolvedCount;
        private long ignoredCount;

        // getters and setters
        public long getTotalCount() { return totalCount; }
        public void setTotalCount(long totalCount) { this.totalCount = totalCount; }

        public long getCriticalCount() { return criticalCount; }
        public void setCriticalCount(long criticalCount) { this.criticalCount = criticalCount; }

        public long getHighCount() { return highCount; }
        public void setHighCount(long highCount) { this.highCount = highCount; }

        public long getMediumCount() { return mediumCount; }
        public void setMediumCount(long mediumCount) { this.mediumCount = mediumCount; }

        public long getLowCount() { return lowCount; }
        public void setLowCount(long lowCount) { this.lowCount = lowCount; }

        public long getUnhandledCount() { return unhandledCount; }
        public void setUnhandledCount(long unhandledCount) { this.unhandledCount = unhandledCount; }

        public long getHandlingCount() { return handlingCount; }
        public void setHandlingCount(long handlingCount) { this.handlingCount = handlingCount; }

        public long getResolvedCount() { return resolvedCount; }
        public void setResolvedCount(long resolvedCount) { this.resolvedCount = resolvedCount; }

        public long getIgnoredCount() { return ignoredCount; }
        public void setIgnoredCount(long ignoredCount) { this.ignoredCount = ignoredCount; }
    }
}