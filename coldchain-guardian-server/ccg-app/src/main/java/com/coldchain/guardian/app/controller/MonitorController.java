package com.coldchain.guardian.app.controller;

import com.coldchain.guardian.app.service.MonitorService;
import com.coldchain.guardian.common.api.ApiResponse;
import com.coldchain.guardian.common.api.PageResponse;
import com.coldchain.guardian.contract.dto.monitor.MonitorDeviceDTO;
import com.coldchain.guardian.contract.dto.monitor.MonitorSummaryDTO;
import com.coldchain.guardian.contract.dto.monitor.TrendPointDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/monitor")
public class MonitorController {

    @Autowired
    private MonitorService monitorService;

    /**
     * 获取实时监测总览指标
     */
    @GetMapping("/summary")
    public ApiResponse<MonitorSummaryDTO> getSummary() {
        MonitorSummaryDTO summary = monitorService.getSummary();
        return ApiResponse.success(summary);
    }

    /**
     * 获取实时设备列表（分页 + 筛选）
     */
    @GetMapping("/devices")
    public ApiResponse<PageResponse<MonitorDeviceDTO>> getMonitorDevices(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size,
            @RequestParam(required = false) Long areaId,
            @RequestParam(required = false) Boolean online,
            @RequestParam(required = false) Boolean alarming,
            @RequestParam(required = false) String keyword) {

        PageResponse<MonitorDeviceDTO> result = monitorService.getMonitorDevices(page, size, areaId, online, alarming, keyword);
        return ApiResponse.success(result);
    }

    /**
     * 获取设备实时曲线数据
     */
    @GetMapping("/devices/{deviceId}/trend")
    public ApiResponse<List<TrendPointDTO>> getDeviceTrend(
            @PathVariable Long deviceId,
            @RequestParam String from,
            @RequestParam String to,
            @RequestParam(defaultValue = "60") Integer interval) {

        List<TrendPointDTO> trend = monitorService.getDeviceTrend(deviceId, from, to, interval);
        return ApiResponse.success(trend);
    }
}