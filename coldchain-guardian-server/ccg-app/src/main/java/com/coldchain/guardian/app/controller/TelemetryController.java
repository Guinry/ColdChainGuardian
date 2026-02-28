package com.coldchain.guardian.app.controller;

import com.coldchain.guardian.app.service.TelemetryService;
import com.coldchain.guardian.common.api.ApiResponse;
import com.coldchain.guardian.common.api.PageResponse;
import com.coldchain.guardian.contract.dto.telemetry.TelemetryDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/telemetry")
public class TelemetryController {

    @Autowired
    private TelemetryService telemetryService;

    /**
     * 根据设备ID获取遥测数据列表（分页）
     */
    @GetMapping("/device/{deviceId}")
    public ApiResponse<PageResponse<TelemetryDto>> getTelemetryByDeviceId(
            @PathVariable Long deviceId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime) {

        List<TelemetryDto> telemetryList = telemetryService.getTelemetryByDeviceId(deviceId, page, size, startTime, endTime);

        // 获取总数
        long total = telemetryService.countTelemetryByDeviceId(deviceId, startTime, endTime);

        PageResponse<TelemetryDto> pageResponse = new PageResponse<>(telemetryList, total, page, size);
        return ApiResponse.success(pageResponse);
    }

    /**
     * 根据设备ID获取最新的遥测数据
     */
    @GetMapping("/device/{deviceId}/latest")
    public ApiResponse<TelemetryDto> getLatestTelemetryByDeviceId(@PathVariable Long deviceId) {
        TelemetryDto latestTelemetry = telemetryService.getLatestTelemetryByDeviceId(deviceId);
        if (latestTelemetry == null) {
            return ApiResponse.error(404, "未找到设备的遥测数据");
        }
        return ApiResponse.success(latestTelemetry);
    }

    /**
     * 获取设备历史数据用于图表显示
     */
    @GetMapping("/device/{deviceId}/history")
    public ApiResponse<List<TelemetryDto>> getHistoricalTelemetryForChart(
            @PathVariable Long deviceId,
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime,
            @RequestParam(defaultValue = "200") Integer limit) {

        List<TelemetryDto> historicalData = telemetryService.getHistoricalTelemetryForChart(deviceId, startTime, endTime, limit);
        return ApiResponse.success(historicalData);
    }
}