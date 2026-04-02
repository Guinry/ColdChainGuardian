package com.coldchain.guardian.app.controller;

import com.coldchain.guardian.app.service.AlertService;
import com.coldchain.guardian.app.service.WorkOrderService;
import com.coldchain.guardian.app.service.AlertAnalysisService;
import com.coldchain.guardian.common.api.ApiResponse;
import com.coldchain.guardian.common.api.PageResponse;
import com.coldchain.guardian.contract.dto.alert.AlertDto;
import com.coldchain.guardian.contract.dto.alert.CreateAlertRequestDto;
import com.coldchain.guardian.contract.dto.workorder.WorkOrderDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;

@Tag(name = "告警管理", description = "处理告警查询、状态更新、转工单等操作")
@RestController
@RequestMapping("/api/alerts")
public class AlertController {

    @Autowired
    private AlertService alertService;

    @Autowired
    private WorkOrderService workOrderService;

    @Autowired
    private AlertAnalysisService alertAnalysisService;

    /**
     * 根据设备 ID 获取告警列表（分页）
     */
    @Operation(summary = "获取设备告警列表", description = "根据设备 ID 分页获取告警列表，支持多维度筛选")
    @GetMapping("/device/{deviceId}")
    public ApiResponse<PageResponse<AlertDto>> getAlertsByDeviceId(
            @PathVariable Long deviceId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String alertType,
            @RequestParam(required = false) String alertLevel,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime) {
        try {
            List<AlertDto> alerts = alertService.getAlertsByDeviceId(deviceId, page, size, alertType, alertLevel, status, startTime, endTime);
            long total = alertService.countAlertsByDeviceId(deviceId, alertType, alertLevel, status, startTime, endTime);
            PageResponse<AlertDto> pageResponse = new PageResponse<>(alerts, total, page, size);
            return ApiResponse.success(pageResponse);
        } catch (Exception e) {
            return ApiResponse.error("获取告警列表失败：" + e.getMessage());
        }
    }

    /**
     * 根据 ID 获取告警详情
     */
    @Operation(summary = "获取告警详情", description = "根据告警 ID 获取详细信息")
    @GetMapping("/{id}")
    public ApiResponse<AlertDto> getAlertById(@PathVariable Long id) {
        try {
            AlertDto alert = alertService.getAlertById(id);
            if (alert == null) {
                return ApiResponse.error(404, "告警不存在");
            }
            return ApiResponse.success(alert);
        } catch (Exception e) {
            return ApiResponse.error("获取告警详情失败：" + e.getMessage());
        }
    }

    /**
     * 创建告警
     */
    @Operation(summary = "创建告警", description = "创建新的告警记录")
    @PostMapping
    public ApiResponse<AlertDto> createAlert(@Valid @RequestBody CreateAlertRequestDto requestDto) {
        try {
            AlertDto alert = alertService.createAlert(requestDto);
            return ApiResponse.success(alert);
        } catch (Exception e) {
            return ApiResponse.error("创建告警失败：" + e.getMessage());
        }
    }

    /**
     * 更新告警状态
     */
    @Operation(summary = "更新告警状态", description = "更新告警的处理状态和备注")
    @PutMapping("/{id}/status")
    public ApiResponse<AlertDto> updateAlertStatus(@PathVariable Long id, @RequestBody UpdateAlertStatusRequest request) {
        try {
            AlertDto alert = alertService.updateAlertStatus(id, request.getStatus(), request.getHandleRemark());
            if (alert == null) {
                return ApiResponse.error(404, "告警不存在");
            }
            return ApiResponse.success(alert);
        } catch (Exception e) {
            return ApiResponse.error("更新告警状态失败：" + e.getMessage());
        }
    }

    /**
     * 批量更新告警状态
     */
    @Operation(summary = "批量更新告警状态", description = "批量更新多个告警的状态")
    @PutMapping("/batch-status")
    public ApiResponse<Void> batchUpdateStatus(@RequestBody BatchUpdateAlertStatusRequest request) {
        try {
            alertService.batchUpdateAlertStatus(request.getIds(), request.getStatus(), request.getHandleRemark());
            return ApiResponse.success(null);
        } catch (Exception e) {
            return ApiResponse.error("批量更新告警状态失败：" + e.getMessage());
        }
    }

    /**
     * 删除告警
     */
    @Operation(summary = "删除告警", description = "删除指定的告警记录")
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteAlert(@PathVariable Long id) {
        try {
            alertService.deleteAlert(id);
            return ApiResponse.success(null);
        } catch (Exception e) {
            return ApiResponse.error("删除告警失败：" + e.getMessage());
        }
    }

    /**
     * 获取告警统计数据
     */
    @Operation(summary = "获取告警统计", description = "获取告警统计数据，支持时间范围和设备筛选")
    @GetMapping("/stats")
    public ApiResponse<Object> getAlertStats(
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime,
            @RequestParam(required = false) Long deviceId) {
        try {
            Object stats = alertService.getAlertStatistics(startTime, endTime, deviceId);
            return ApiResponse.success(stats);
        } catch (Exception e) {
            return ApiResponse.error("获取告警统计失败：" + e.getMessage());
        }
    }

    /**
     * 搜索告警（多维度）
     */
    @Operation(summary = "搜索告警", description = "多维度搜索告警，支持关键词、位置、级别、状态等筛选")
    @GetMapping("/search")
    public ApiResponse<PageResponse<AlertDto>> searchAlerts(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) String level,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        try {
            List<AlertDto> alerts = alertService.searchAlerts(keyword, location, level, status, startTime, endTime, page, size);
            long total = alertService.getTotalAlertCount(keyword, location, level, status, startTime, endTime);
            PageResponse<AlertDto> pageResponse = new PageResponse<>(alerts, total, page, size);
            return ApiResponse.success(pageResponse);
        } catch (Exception e) {
            return ApiResponse.error("搜索告警失败：" + e.getMessage());
        }
    }

    /**
     * 将告警转为工单
     */
    @Operation(summary = "告警转工单", description = "将指定告警转换为工单")
    @PutMapping("/{id}/convert-to-work-order")
    public ApiResponse<WorkOrderDto> convertAlertToWorkOrder(@PathVariable Long id,
                                                            @RequestBody ConvertAlertToWorkOrderRequest request) {
        try {
            WorkOrderDto workOrder = alertService.convertAlertToWorkOrder(id, request.getAssigneeId(),
                                                                         request.getAssigneeName(), request.getDescription());
            if (workOrder == null) {
                return ApiResponse.error(404, "告警不存在");
            }
            return ApiResponse.success(workOrder);
        } catch (Exception e) {
            return ApiResponse.error("告警转工单失败：" + e.getMessage());
        }
    }

    /**
     * 获取紧急告警列表
     */
    @Operation(summary = "获取紧急告警", description = "获取未处理的紧急和高危告警列表")
    @GetMapping("/urgent")
    public ApiResponse<List<AlertDto>> getUrgentAlerts() {
        try {
            List<AlertDto> alerts = alertService.getUrgentAlerts();
            return ApiResponse.success(alerts);
        } catch (Exception e) {
            return ApiResponse.error("获取紧急告警失败：" + e.getMessage());
        }
    }

    /**
     * 批量处理告警（转为工单）
     */
    @Operation(summary = "批量告警转工单", description = "批量将多个告警转换为工单")
    @PutMapping("/batch-convert-to-work-order")
    public ApiResponse<List<WorkOrderDto>> batchConvertAlertsToWorkOrders(@RequestBody BatchConvertAlertsRequest request) {
        try {
            List<WorkOrderDto> workOrders = alertService.batchConvertAlertsToWorkOrders(request.getAlertIds(),
                                                                                     request.getAssigneeId(),
                                                                                     request.getAssigneeName(),
                                                                                     request.getDescription());
            return ApiResponse.success(workOrders);
        } catch (Exception e) {
            return ApiResponse.error("批量告警转工单失败：" + e.getMessage());
        }
    }

    /**
     * 获取告警趋势分析
     */
    @Operation(summary = "告警趋势分析", description = "获取告警数量趋势分析数据")
    @GetMapping("/analysis/trend")
    public ApiResponse<Map<String, Object>> getAlertTrendAnalysis(
            @RequestParam(defaultValue = "daily") String period) {
        try {
            Map<String, Object> analysis = alertAnalysisService.getAlertTrendAnalysis(period);
            return ApiResponse.success(analysis);
        } catch (Exception e) {
            return ApiResponse.error("获取告警趋势分析失败：" + e.getMessage());
        }
    }

    /**
     * 获取重复告警分析
     */
    @Operation(summary = "重复告警分析", description = "获取重复发生的告警分析数据")
    @GetMapping("/analysis/recurring")
    public ApiResponse<Map<String, Object>> getRecurringAlertAnalysis() {
        try {
            Map<String, Object> analysis = alertAnalysisService.getRecurringAlertAnalysis();
            return ApiResponse.success(analysis);
        } catch (Exception e) {
            return ApiResponse.error("获取重复告警分析失败：" + e.getMessage());
        }
    }

    /**
     * 获取设备健康度评分
     */
    @Operation(summary = "设备健康度评分", description = "获取各设备的健康度评分")
    @GetMapping("/analysis/device-health")
    public ApiResponse<Map<String, Object>> getDeviceHealthScore() {
        try {
            Map<String, Object> healthScores = alertAnalysisService.getDeviceHealthScore();
            return ApiResponse.success(healthScores);
        } catch (Exception e) {
            return ApiResponse.error("获取设备健康度评分失败：" + e.getMessage());
        }
    }

    /**
     * 获取告警根因分析
     */
    @Operation(summary = "告警根因分析", description = "获取告警的根本原因分析数据")
    @GetMapping("/analysis/root-cause")
    public ApiResponse<Map<String, Object>> getRootCauseAnalysis() {
        try {
            Map<String, Object> analysis = alertAnalysisService.getRootCauseAnalysis();
            return ApiResponse.success(analysis);
        } catch (Exception e) {
            return ApiResponse.error("获取告警根因分析失败：" + e.getMessage());
        }
    }

    // ==================== 内部请求 DTO 类 ====================

    public static class ConvertAlertToWorkOrderRequest {
        private Long assigneeId;
        private String assigneeName;
        private String description;

        public Long getAssigneeId() { return assigneeId; }
        public void setAssigneeId(Long assigneeId) { this.assigneeId = assigneeId; }
        public String getAssigneeName() { return assigneeName; }
        public void setAssigneeName(String assigneeName) { this.assigneeName = assigneeName; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
    }

    public static class BatchConvertAlertsRequest {
        private List<Long> alertIds;
        private Long assigneeId;
        private String assigneeName;
        private String description;

        public List<Long> getAlertIds() { return alertIds; }
        public void setAlertIds(List<Long> alertIds) { this.alertIds = alertIds; }
        public Long getAssigneeId() { return assigneeId; }
        public void setAssigneeId(Long assigneeId) { this.assigneeId = assigneeId; }
        public String getAssigneeName() { return assigneeName; }
        public void setAssigneeName(String assigneeName) { this.assigneeName = assigneeName; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
    }

    public static class UpdateAlertStatusRequest {
        private String status;
        private String handleRemark;

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public String getHandleRemark() { return handleRemark; }
        public void setHandleRemark(String handleRemark) { this.handleRemark = handleRemark; }
    }

    public static class BatchUpdateAlertStatusRequest {
        private List<Long> ids;
        private String status;
        private String handleRemark;

        public List<Long> getIds() { return ids; }
        public void setIds(List<Long> ids) { this.ids = ids; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public String getHandleRemark() { return handleRemark; }
        public void setHandleRemark(String handleRemark) { this.handleRemark = handleRemark; }
    }
}
