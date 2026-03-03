package com.coldchain.guardian.app.controller;

import com.coldchain.guardian.app.service.AlertService;
import com.coldchain.guardian.app.service.WorkOrderService;
import com.coldchain.guardian.app.service.AlertAnalysisService;
import com.coldchain.guardian.common.api.ApiResponse;
import com.coldchain.guardian.common.api.PageResponse;
import com.coldchain.guardian.contract.dto.alert.AlertDto;
import com.coldchain.guardian.contract.dto.alert.CreateAlertRequestDto;
import com.coldchain.guardian.contract.dto.workorder.WorkOrderDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;

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
     * 根据设备ID获取告警列表（分页）
     */
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

        List<AlertDto> alerts = alertService.getAlertsByDeviceId(deviceId, page, size, alertType, alertLevel, status, startTime, endTime);

        // 获取总数
        long total = alertService.countAlertsByDeviceId(deviceId, alertType, alertLevel, status, startTime, endTime);

        PageResponse<AlertDto> pageResponse = new PageResponse<>(alerts, total, page, size);
        return ApiResponse.success(pageResponse);
    }

    /**
     * 根据ID获取告警详情
     */
    @GetMapping("/{id}")
    public ApiResponse<AlertDto> getAlertById(@PathVariable Long id) {
        AlertDto alert = alertService.getAlertById(id);
        if (alert == null) {
            return ApiResponse.error(404, "告警不存在");
        }
        return ApiResponse.success(alert);
    }

    /**
     * 创建告警
     */
    @PostMapping
    public ApiResponse<AlertDto> createAlert(@Valid @RequestBody CreateAlertRequestDto requestDto) {
        AlertDto alert = alertService.createAlert(requestDto);
        return ApiResponse.success(alert);
    }

    /**
     * 更新告警状态
     */
    @PutMapping("/{id}/status")
    public ApiResponse<AlertDto> updateAlertStatus(@PathVariable Long id, @RequestBody UpdateAlertStatusRequest request) {
        AlertDto alert = alertService.updateAlertStatus(id, request.getStatus(), request.getHandleRemark());
        if (alert == null) {
            return ApiResponse.error(404, "告警不存在");
        }
        return ApiResponse.success(alert);
    }

    /**
     * 批量更新告警状态
     */
    @PutMapping("/batch-status")
    public ApiResponse<Void> batchUpdateStatus(@RequestBody BatchUpdateAlertStatusRequest request) {
        alertService.batchUpdateAlertStatus(request.getIds(), request.getStatus(), request.getHandleRemark());
        return ApiResponse.success(null);
    }

    /**
     * 删除告警
     */
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteAlert(@PathVariable Long id) {
        alertService.deleteAlert(id);
        return ApiResponse.success(null);
    }

    /**
     * 获取告警统计数据
     */
    @GetMapping("/stats")
    public ApiResponse<Object> getAlertStats(
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime,
            @RequestParam(required = false) Long deviceId) {

        Object stats = alertService.getAlertStatistics(startTime, endTime, deviceId);
        return ApiResponse.success(stats);
    }

    /**
     * 搜索告警（多维度）
     */
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

        List<AlertDto> alerts = alertService.searchAlerts(keyword, location, level, status, startTime, endTime, page, size);

        // Count total alerts for pagination
        long total = alertService.getTotalAlertCount(keyword, location, level, status, startTime, endTime);

        PageResponse<AlertDto> pageResponse = new PageResponse<>(alerts, total, page, size);
        return ApiResponse.success(pageResponse);
    }

    /**
     * 将告警转为工单
     */
    @PutMapping("/{id}/convert-to-work-order")
    public ApiResponse<WorkOrderDto> convertAlertToWorkOrder(@PathVariable Long id,
                                                            @RequestBody ConvertAlertToWorkOrderRequest request) {
        WorkOrderDto workOrder = alertService.convertAlertToWorkOrder(id, request.getAssigneeId(),
                                                                     request.getAssigneeName(), request.getDescription());
        if (workOrder == null) {
            return ApiResponse.error(404, "告警不存在");
        }
        return ApiResponse.success(workOrder);
    }

    /**
     * 获取紧急告警列表（未处理的紧急和高危告警）
     */
    @GetMapping("/urgent")
    public ApiResponse<List<AlertDto>> getUrgentAlerts() {
        List<AlertDto> alerts = alertService.getUrgentAlerts();
        return ApiResponse.success(alerts);
    }

    /**
     * 批量处理告警（转为工单）
     */
    @PutMapping("/batch-convert-to-work-order")
    public ApiResponse<List<WorkOrderDto>> batchConvertAlertsToWorkOrders(@RequestBody BatchConvertAlertsRequest request) {
        List<WorkOrderDto> workOrders = alertService.batchConvertAlertsToWorkOrders(request.getAlertIds(),
                                                                                 request.getAssigneeId(),
                                                                                 request.getAssigneeName(),
                                                                                 request.getDescription());
        return ApiResponse.success(workOrders);
    }

    /**
     * 获取告警趋势分析
     */
    @GetMapping("/analysis/trend")
    public ApiResponse<Map<String, Object>> getAlertTrendAnalysis(
            @RequestParam(defaultValue = "daily") String period) {
        Map<String, Object> analysis = alertAnalysisService.getAlertTrendAnalysis(period);
        return ApiResponse.success(analysis);
    }

    /**
     * 获取重复告警分析
     */
    @GetMapping("/analysis/recurring")
    public ApiResponse<Map<String, Object>> getRecurringAlertAnalysis() {
        Map<String, Object> analysis = alertAnalysisService.getRecurringAlertAnalysis();
        return ApiResponse.success(analysis);
    }

    /**
     * 获取设备健康度评分
     */
    @GetMapping("/analysis/device-health")
    public ApiResponse<Map<String, Object>> getDeviceHealthScore() {
        Map<String, Object> healthScores = alertAnalysisService.getDeviceHealthScore();
        return ApiResponse.success(healthScores);
    }

    /**
     * 获取告警根因分析
     */
    @GetMapping("/analysis/root-cause")
    public ApiResponse<Map<String, Object>> getRootCauseAnalysis() {
        Map<String, Object> analysis = alertAnalysisService.getRootCauseAnalysis();
        return ApiResponse.success(analysis);
    }

    // Request DTO for converting alert to work order
    public static class ConvertAlertToWorkOrderRequest {
        private Long assigneeId;
        private String assigneeName;
        private String description;

        public Long getAssigneeId() {
            return assigneeId;
        }

        public void setAssigneeId(Long assigneeId) {
            this.assigneeId = assigneeId;
        }

        public String getAssigneeName() {
            return assigneeName;
        }

        public void setAssigneeName(String assigneeName) {
            this.assigneeName = assigneeName;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }
    }

    // Request DTO for batch converting alerts to work orders
    public static class BatchConvertAlertsRequest {
        private List<Long> alertIds;
        private Long assigneeId;
        private String assigneeName;
        private String description;

        public List<Long> getAlertIds() {
            return alertIds;
        }

        public void setAlertIds(List<Long> alertIds) {
            this.alertIds = alertIds;
        }

        public Long getAssigneeId() {
            return assigneeId;
        }

        public void setAssigneeId(Long assigneeId) {
            this.assigneeId = assigneeId;
        }

        public String getAssigneeName() {
            return assigneeName;
        }

        public void setAssigneeName(String assigneeName) {
            this.assigneeName = assigneeName;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }
    }

    // 用于接收告警状态更新请求的内部类
    public static class UpdateAlertStatusRequest {
        private String status;
        private String handleRemark;

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public String getHandleRemark() {
            return handleRemark;
        }

        public void setHandleRemark(String handleRemark) {
            this.handleRemark = handleRemark;
        }
    }

    // 用于接收批量告警状态更新请求的内部类
    public static class BatchUpdateAlertStatusRequest {
        private List<Long> ids;
        private String status;
        private String handleRemark;

        public List<Long> getIds() {
            return ids;
        }

        public void setIds(List<Long> ids) {
            this.ids = ids;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public String getHandleRemark() {
            return handleRemark;
        }

        public void setHandleRemark(String handleRemark) {
            this.handleRemark = handleRemark;
        }
    }
}