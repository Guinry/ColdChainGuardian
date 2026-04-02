package com.coldchain.guardian.app.controller;

import com.coldchain.guardian.app.service.DeviceService;
import com.coldchain.guardian.app.service.TelemetryService;
import com.coldchain.guardian.common.api.ApiResponse;
import com.coldchain.guardian.common.api.PageResponse;
import com.coldchain.guardian.contract.dto.device.DeviceDto;
import com.coldchain.guardian.contract.dto.device.CreateDeviceRequestDto;
import com.coldchain.guardian.contract.dto.telemetry.TelemetryDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@Tag(name = "设备管理", description = "处理设备注册、配置、状态管理等操作")
@RestController
@RequestMapping("/api/devices")
public class DeviceController {

    @Autowired
    private DeviceService deviceService;

    @Autowired
    private TelemetryService telemetryService;

    /**
     * 获取设备列表（分页和筛选）
     */
    @Operation(summary = "获取设备列表", description = "分页查询设备列表，支持多条件筛选")
    @GetMapping
    public ApiResponse<PageResponse<DeviceDto>> getDevices(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String deviceType,
            @RequestParam(required = false) Boolean onlineStatus,
            @RequestParam(required = false) Boolean enabled,
            @RequestParam(required = false) Boolean alarmEnabled,
            @RequestParam(required = false) Long areaId) {

        List<DeviceDto> filteredDevices = deviceService.getDevicesWithFilters(page, size, keyword, deviceType,
                onlineStatus, enabled, alarmEnabled, areaId);

        int total = deviceService.getDeviceCountWithFilters(keyword, deviceType,
                onlineStatus, enabled, alarmEnabled, areaId);

        PageResponse<DeviceDto> pageResponse = new PageResponse<>(filteredDevices, total, page, size);

        return ApiResponse.success(pageResponse);
    }

    /**
     * 根据ID获取设备
     */
    @Operation(summary = "根据ID获取设备详情")
    @GetMapping("/{id}")
    public ApiResponse<DeviceDto> getDeviceById(@PathVariable Long id) {
        DeviceDto device = deviceService.getDeviceById(id);
        if (device == null) {
            return ApiResponse.error(404, "设备不存在");
        }
        return ApiResponse.success(device);
    }

    /**
     * 根据设备编码获取设备
     */
    @Operation(summary = "根据设备编码获取设备详情")
    @GetMapping("/code/{deviceCode}")
    public ApiResponse<DeviceDto> getDeviceByDeviceCode(@PathVariable String deviceCode) {
        DeviceDto device = deviceService.getDeviceByDeviceCode(deviceCode);
        if (device == null) {
            return ApiResponse.error(404, "设备不存在");
        }
        return ApiResponse.success(device);
    }

    /**
     * 根据库区ID获取设备列表
     */
    @Operation(summary = "根据库区ID获取设备列表")
    @GetMapping("/area/{areaId}")
    public ApiResponse<List<DeviceDto>> getDevicesByAreaId(@PathVariable Long areaId) {
        List<DeviceDto> devices = deviceService.getDevicesByAreaId(areaId);
        return ApiResponse.success(devices);
    }

    /**
     * 根据设备类型获取设备列表
     */
    @Operation(summary = "根据设备类型获取设备列表")
    @GetMapping("/type/{deviceType}")
    public ApiResponse<List<DeviceDto>> getDevicesByDeviceType(@PathVariable String deviceType) {
        List<DeviceDto> devices = deviceService.getDevicesByDeviceType(deviceType);
        return ApiResponse.success(devices);
    }

    /**
     * 根据启用状态获取设备列表
     */
    @Operation(summary = "根据启用状态获取设备列表")
    @GetMapping("/status/{enabled}")
    public ApiResponse<List<DeviceDto>> getDevicesByEnabled(@PathVariable Boolean enabled) {
        List<DeviceDto> devices = deviceService.getDevicesByEnabled(enabled);
        return ApiResponse.success(devices);
    }

    /**
     * 创建新设备
     */
    @Operation(summary = "创建新设备")
    @PostMapping
    public ApiResponse<DeviceDto> createDevice(@Valid @RequestBody CreateDeviceRequestDto requestDto) {
        DeviceDto device = deviceService.createDevice(requestDto);
        return ApiResponse.success(device);
    }

    /**
     * 更新设备信息
     */
    @Operation(summary = "更新设备信息")
    @PutMapping("/{id}")
    public ApiResponse<DeviceDto> updateDevice(@PathVariable Long id,
                                            @Valid @RequestBody CreateDeviceRequestDto requestDto) {
        DeviceDto device = deviceService.updateDevice(id, requestDto);
        if (device == null) {
            return ApiResponse.error(404, "设备不存在");
        }
        return ApiResponse.success(device);
    }

    /**
     * 删除设备
     */
    @Operation(summary = "删除设备")
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteDevice(@PathVariable Long id) {
        deviceService.deleteDevice(id);
        return ApiResponse.success(null);
    }

    /**
     * 启用/禁用设备
     */
    @Operation(summary = "启用/禁用设备")
    @PutMapping("/{id}/status")
    public ApiResponse<DeviceDto> updateDeviceStatus(@PathVariable Long id, @RequestBody UpdateStatusRequest request) {
        DeviceDto device = deviceService.toggleDeviceStatus(id, request.getEnabled());
        if (device == null) {
            return ApiResponse.error(404, "设备不存在");
        }
        return ApiResponse.success(device);
    }

    /**
     * 更新设备告警开关状态
     */
    @Operation(summary = "更新设备告警开关状态")
    @PutMapping("/{id}/alarm-status")
    public ApiResponse<DeviceDto> updateAlarmStatus(@PathVariable Long id, @RequestBody UpdateAlarmStatusRequest request) {
        DeviceDto device = deviceService.updateDeviceAlarmStatus(id, request.getAlarmEnabled());
        if (device == null) {
            return ApiResponse.error(404, "设备不存在");
        }
        return ApiResponse.success(device);
    }

    /**
     * 更新设备阈值
     */
    @Operation(summary = "更新设备阈值配置")
    @PutMapping("/{id}/threshold")
    public ApiResponse<DeviceDto> updateThreshold(@PathVariable Long id, @RequestBody CreateDeviceRequestDto thresholdData) {
        DeviceDto device = deviceService.getDeviceById(id);
        if (device == null) {
            return ApiResponse.error(404, "设备不存在");
        }

        CreateDeviceRequestDto requestDto = new CreateDeviceRequestDto();
        requestDto.setDeviceCode(device.getDeviceCode());
        requestDto.setDeviceName(device.getDeviceName());
        requestDto.setDeviceType(device.getDeviceType());
        requestDto.setAreaId(device.getAreaId());
        requestDto.setModel(device.getModel());
        requestDto.setManufacturer(device.getManufacturer());
        requestDto.setSn(device.getSn());
        requestDto.setFirmwareVersion(device.getFirmwareVersion());
        requestDto.setLocationDesc(device.getLocationDesc());
        requestDto.setThresholdMode(thresholdData.getThresholdMode());
        requestDto.setTemperatureThresholdMin(thresholdData.getTemperatureThresholdMin());
        requestDto.setTemperatureThresholdMax(thresholdData.getTemperatureThresholdMax());
        requestDto.setHumidityThresholdMin(thresholdData.getHumidityThresholdMin());
        requestDto.setHumidityThresholdMax(thresholdData.getHumidityThresholdMax());
        requestDto.setAlarmEnabled(device.getAlarmEnabled());
        requestDto.setEnabled(device.getEnabled());

        DeviceDto updatedDevice = deviceService.updateDevice(id, requestDto);
        return ApiResponse.success(updatedDevice);
    }

    /**
     * 批量更新设备状态
     */
    @Operation(summary = "批量更新设备启用状态")
    @PutMapping("/batch-status")
    public ApiResponse<Void> batchUpdateStatus(@RequestBody BatchUpdateStatusRequest request) {
        for (Long id : request.getIds()) {
            deviceService.toggleDeviceStatus(id, request.getEnabled());
        }
        return ApiResponse.success(null);
    }

    /**
     * 解绑设备与库区关联
     */
    @Operation(summary = "解绑设备与库区的关联")
    @PutMapping("/{id}/unbind-area")
    public ApiResponse<DeviceDto> unbindArea(@PathVariable Long id) {
        DeviceDto device = deviceService.getDeviceById(id);
        if (device == null) {
            return ApiResponse.error(404, "设备不存在");
        }

        CreateDeviceRequestDto requestDto = new CreateDeviceRequestDto();
        requestDto.setDeviceCode(device.getDeviceCode());
        requestDto.setDeviceName(device.getDeviceName());
        requestDto.setDeviceType(device.getDeviceType());
        requestDto.setAreaId(null);
        requestDto.setModel(device.getModel());
        requestDto.setManufacturer(device.getManufacturer());
        requestDto.setSn(device.getSn());
        requestDto.setFirmwareVersion(device.getFirmwareVersion());
        requestDto.setLocationDesc(device.getLocationDesc());
        requestDto.setThresholdMode(device.getThresholdMode());
        requestDto.setTemperatureThresholdMin(device.getTemperatureThresholdMin());
        requestDto.setTemperatureThresholdMax(device.getTemperatureThresholdMax());
        requestDto.setHumidityThresholdMin(device.getHumidityThresholdMin());
        requestDto.setHumidityThresholdMax(device.getHumidityThresholdMax());
        requestDto.setAlarmEnabled(device.getAlarmEnabled());
        requestDto.setEnabled(device.getEnabled());

        DeviceDto updatedDevice = deviceService.updateDevice(id, requestDto);
        return ApiResponse.success(updatedDevice);
    }

    /**
     * 获取设备最新数据
     */
    @Operation(summary = "获取设备最新遥测数据")
    @GetMapping("/{id}/latest")
    public ApiResponse<TelemetryDto> getLatestTelemetry(@PathVariable Long id) {
        TelemetryDto latestTelemetry = telemetryService.getLatestTelemetryByDeviceId(id);
        if (latestTelemetry == null) {
            return ApiResponse.error(404, "未找到设备的遥测数据");
        }
        return ApiResponse.success(latestTelemetry);
    }

    /**
     * 获取设备历史数据
     */
    @Operation(summary = "获取设备历史遥测数据（分页）")
    @GetMapping("/{id}/data")
    public ApiResponse<PageResponse<TelemetryDto>> getHistoricalTelemetry(
            @PathVariable Long id,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime) {

        List<TelemetryDto> telemetryList = telemetryService.getTelemetryByDeviceId(id, page, size, startTime, endTime);
        long total = telemetryService.countTelemetryByDeviceId(id, startTime, endTime);

        PageResponse<TelemetryDto> pageResponse = new PageResponse<>(telemetryList, total, page, size);
        return ApiResponse.success(pageResponse);
    }

    /**
     * 用于接收状态更新请求的内部类
     */
    @Data
    public static class UpdateStatusRequest {
        private Boolean enabled;
    }

    /**
     * 用于接收告警状态更新请求的内部类
     */
    @Data
    public static class UpdateAlarmStatusRequest {
        private Boolean alarmEnabled;
    }

    /**
     * 用于接收批量更新状态请求的内部类
     */
    @Data
    public static class BatchUpdateStatusRequest {
        private List<Long> ids;
        private Boolean enabled;
    }
}
