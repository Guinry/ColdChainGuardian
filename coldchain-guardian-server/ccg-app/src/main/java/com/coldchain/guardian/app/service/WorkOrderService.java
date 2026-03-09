package com.coldchain.guardian.app.service;

import com.coldchain.guardian.contract.dto.workorder.WorkOrderDto;
import com.coldchain.guardian.contract.dto.workorder.CreateWorkOrderRequestDto;
import com.coldchain.guardian.contract.enums.WorkOrderStatus;
import com.coldchain.guardian.infra.persistence.entity.WorkOrderEntity;
import com.coldchain.guardian.infra.persistence.entity.WorkOrderLogEntity;
import com.coldchain.guardian.infra.persistence.mapper.WorkOrderMapper;
import com.coldchain.guardian.infra.persistence.repository.WorkOrderLogRepository;
import com.coldchain.guardian.infra.persistence.repository.AlertRepository;
import com.coldchain.guardian.infra.persistence.entity.AlertEntity;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class WorkOrderService {

    @Autowired
    private WorkOrderMapper workOrderMapper;

    @Autowired
    private WorkOrderLogRepository workOrderLogRepository;

    @Autowired
    private AlertRepository alertRepository;

    /**
     * 创建工单
     */
    @Transactional
    public WorkOrderDto createWorkOrder(CreateWorkOrderRequestDto requestDto) {
        WorkOrderEntity entity = new WorkOrderEntity();
        BeanUtils.copyProperties(requestDto, entity);

        // Set initial status to PENDING
        entity.setStatus(WorkOrderStatus.PENDING.getCode());

        // If associated with an alert, get the alert details
        if (requestDto.getAlertId() != null) {
            AlertEntity alert = alertRepository.findById(requestDto.getAlertId());
            if (alert != null) {
                entity.setAlertId(alert.getId());
                // Set location and device info from alert
                entity.setWarehouseId(alert.getWarehouseId());
                entity.setDeviceId(alert.getDeviceId());
                entity.setLocationDetail(alert.getAreaName() + " - " + alert.getDeviceName());

                // Update alert status to HANDLING
                alert.setStatus("HANDLING");
                alert.setHandleTime(LocalDateTime.now());
                alertRepository.save(alert);
            }
        }

        workOrderMapper.insert(entity);

        // Log the creation
        logWorkOrderAction(entity.getId(), "CREATED", null, WorkOrderStatus.PENDING.getCode(),
                          requestDto.getReporterId(), "System", "工单创建");

        return convertToWorkOrderDto(entity);
    }

    /**
     * 获取工单详情
     */
    public WorkOrderDto getWorkOrderById(Long id) {
        WorkOrderEntity entity = workOrderMapper.selectById(id);
        return entity != null ? convertToWorkOrderDto(entity) : null;
    }

    /**
     * 获取工单列表
     */
    public List<WorkOrderDto> getWorkOrders(String status, String priority, String workType,
                                           Long assigneeId, Long reporterId, String keyword) {
        // Basic query wrapper - in a real implementation, you'd add filtering logic
        List<WorkOrderEntity> entities = workOrderMapper.selectList(null);

        return entities.stream()
                .filter(entity -> status == null || status.equals(entity.getStatus()))
                .filter(entity -> priority == null || priority.equals(entity.getPriority()))
                .filter(entity -> workType == null || workType.equals(entity.getWorkType()))
                .filter(entity -> assigneeId == null || (entity.getAssigneeId() != null && entity.getAssigneeId().equals(assigneeId)))
                .filter(entity -> reporterId == null || (entity.getReporterId() != null && entity.getReporterId().equals(reporterId)))
                .filter(entity -> keyword == null || entity.getTitle().contains(keyword) || entity.getDescription().contains(keyword))
                .map(this::convertToWorkOrderDto)
                .collect(Collectors.toList());
    }

    /**
     * 获取工单总数（用于分页）
     */
    public long countWorkOrders(String status, String priority, String workType,
                               Long assigneeId, Long reporterId, String keyword) {
        List<WorkOrderEntity> entities = workOrderMapper.selectList(null);

        return entities.stream()
                .filter(entity -> status == null || status.equals(entity.getStatus()))
                .filter(entity -> priority == null || priority.equals(entity.getPriority()))
                .filter(entity -> workType == null || workType.equals(entity.getWorkType()))
                .filter(entity -> assigneeId == null || (entity.getAssigneeId() != null && entity.getAssigneeId().equals(assigneeId)))
                .filter(entity -> reporterId == null || (entity.getReporterId() != null && entity.getReporterId().equals(reporterId)))
                .filter(entity -> keyword == null || entity.getTitle().contains(keyword) || entity.getDescription().contains(keyword))
                .count();
    }

    /**
     * 更新工单状态
     */
    @Transactional
    public WorkOrderDto updateWorkOrderStatus(Long id, String status, String remark, Long operatorId, String operatorName) {
        WorkOrderEntity entity = workOrderMapper.selectById(id);
        if (entity == null) {
            return null;
        }

        String previousStatus = entity.getStatus();
        entity.setStatus(status);

        // Set completion time if status is COMPLETED
        if (WorkOrderStatus.COMPLETED.getCode().equals(status)) {
            entity.setCompletedTime(LocalDateTime.now());
        }

        // Set verification time if status is VERIFYING
        if (WorkOrderStatus.VERIFYING.getCode().equals(status)) {
            entity.setVerifiedTime(LocalDateTime.now());
        }

        workOrderMapper.updateById(entity);

        // Determine the action type based on status transition
        String action = determineActionFromStatusTransition(previousStatus, status, remark);

        // Log the status change
        logWorkOrderAction(id, action, previousStatus, status, operatorId, operatorName, remark);

        return convertToWorkOrderDto(entity);
    }

    /**
     * Determine the action type based on status transition
     */
    private String determineActionFromStatusTransition(String previousStatus, String currentStatus, String remark) {
        // Check the remark to distinguish between accept and start operations
        if ("PENDING".equals(previousStatus) && "PROCESSING".equals(currentStatus)) {
            if (remark != null && remark.contains("接受")) {
                return "ACCEPTED"; // 工单被接受
            } else {
                return "STARTED"; // 工单开始处理
            }
        } else if ("PROCESSING".equals(previousStatus) && "VERIFYING".equals(currentStatus)) {
            return "COMPLETED"; // 工单已完成处理，等待验收
        } else if ("VERIFYING".equals(previousStatus) && "COMPLETED".equals(currentStatus)) {
            return "VERIFIED"; // 工单已通过验收
        } else if ("COMPLETED".equals(previousStatus) && "CLOSED".equals(currentStatus)) {
            return "CLOSED"; // 工单已关闭
        } else if (("PROCESSING".equals(previousStatus) || "VERIFYING".equals(previousStatus))
                   && "PENDING".equals(currentStatus)) {
            return "REJECTED"; // 工单被驳回
        } else {
            return "STATUS_CHANGED"; // 一般状态变更
        }
    }

    /**
     * 更新工单信息
     */
    @Transactional
    public WorkOrderDto updateWorkOrder(Long id, CreateWorkOrderRequestDto requestDto) {
        WorkOrderEntity entity = workOrderMapper.selectById(id);
        if (entity == null) {
            return null;
        }

        BeanUtils.copyProperties(requestDto, entity, "id", "status", "createdAt", "updatedAt");

        workOrderMapper.updateById(entity);

        return convertToWorkOrderDto(entity);
    }

    /**
     * 获取工单统计信息
     */
    public Map<String, Object> getWorkOrderStats() {
        // 直接通过Mapper获取各种状态的工单数量，提高效率和准确性
        long pendingCount = workOrderMapper.countWorkOrdersByStatus(WorkOrderStatus.PENDING.getCode());
        long processingCount = workOrderMapper.countWorkOrdersByStatus(WorkOrderStatus.PROCESSING.getCode());
        long completedCount = workOrderMapper.countWorkOrdersByStatus(WorkOrderStatus.COMPLETED.getCode());
        long closedCount = workOrderMapper.countWorkOrdersByStatus(WorkOrderStatus.CLOSED.getCode());

        // 计算逾期工单数（截止日期已过且状态不是已完成或已关闭）
        long overdueCount = workOrderMapper.countOverdueWorkOrders();

        // 修复：计算本周完成的工单数 - 使用精确的周开始时间
        LocalDate startOfWeek = LocalDate.now().with(java.time.DayOfWeek.MONDAY);
        LocalDate endOfWeek = LocalDate.now();

        long completedThisWeek = workOrderMapper.countCompletedThisWeek(startOfWeek.atStartOfDay(), endOfWeek.atTime(LocalTime.MAX));

        return Map.of(
            "overdueCount", overdueCount,
            "pendingCount", pendingCount,
            "processingCount", processingCount,
            "completedThisWeek", completedThisWeek
        );
    }

    /**
     * 获取工单日志
     */
    public List<WorkOrderLogEntity> getWorkOrderLogs(Long workOrderId) {
        return workOrderLogRepository.findByWorkOrderId(workOrderId);
    }

    /**
     * 记录工单操作日志
     */
    private void logWorkOrderAction(Long workOrderId, String action, String previousStatus,
                                   String currentStatus, Long operatorId, String operatorName, String remark) {
        WorkOrderLogEntity log = new WorkOrderLogEntity();
        log.setWorkOrderId(workOrderId);
        log.setAction(action);
        log.setPreviousStatus(previousStatus);
        log.setCurrentStatus(currentStatus);
        log.setOperatorId(operatorId);
        log.setOperatorName(operatorName);
        log.setRemark(remark);

        workOrderLogRepository.save(log);
    }

    /**
     * 转换实体到DTO
     */
    private WorkOrderDto convertToWorkOrderDto(WorkOrderEntity entity) {
        WorkOrderDto dto = new WorkOrderDto();
        BeanUtils.copyProperties(entity, dto);

        // Convert LocalDateTime to timestamp
        if (entity.getCreateTime() != null) {
            dto.setCreatedAt(entity.getCreateTime().atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli());
        }
        if (entity.getUpdateTime() != null) {
            dto.setUpdatedAt(entity.getUpdateTime().atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli());
        }
        if (entity.getCompletedTime() != null) {
            dto.setCompletedAt(entity.getCompletedTime().atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli());
        }
        if (entity.getDueDate() != null) {
            dto.setDueDate(entity.getDueDate().atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli());
        }

        return dto;
    }
}