package com.coldchain.guardian.app.controller;

import com.coldchain.guardian.app.service.AlertService;
import com.coldchain.guardian.common.api.ApiResponse;
import com.coldchain.guardian.common.api.PageResponse;
import com.coldchain.guardian.contract.dto.alert.AlertDto;
import com.coldchain.guardian.contract.dto.alert.CreateAlertRequestDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/alerts")
public class AlertController {

    @Autowired
    private AlertService alertService;

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