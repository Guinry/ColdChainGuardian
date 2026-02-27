package com.coldchain.guardian.app.controller;

import com.coldchain.guardian.app.service.DeviceService;
import com.coldchain.guardian.common.api.ApiResponse;
import com.coldchain.guardian.contract.dto.device.DeviceDto;
import com.coldchain.guardian.contract.dto.device.CreateDeviceRequestDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/devices")
public class DeviceController {

    @Autowired
    private DeviceService deviceService;

    /**
     * 获取设备列表（分页和筛选）
     */
    @GetMapping
    public ApiResponse<List<DeviceDto>> getDevices(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String deviceType,
            @RequestParam(required = false) Boolean onlineStatus,
            @RequestParam(required = false) Boolean enabled,
            @RequestParam(required = false) Boolean alarmEnabled,
            @RequestParam(required = false) Long areaId) {
        // 这里需要实现完整的分页和筛选逻辑
        // 临时实现：先返回所有设备并做简单筛选
        List<DeviceDto> allDevices = deviceService.getAllDevices();

        // 实际应用中应将筛选逻辑传递给服务层进行数据库层面的过滤和分页
        // 这里简化处理，直接返回所有设备
        return ApiResponse.success(allDevices);
    }

    /**
     * 根据ID获取设备
     */
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
    @GetMapping("/area/{areaId}")
    public ApiResponse<List<DeviceDto>> getDevicesByAreaId(@PathVariable Long areaId) {
        List<DeviceDto> devices = deviceService.getDevicesByAreaId(areaId);
        return ApiResponse.success(devices);
    }

    /**
     * 根据设备类型获取设备列表
     */
    @GetMapping("/type/{deviceType}")
    public ApiResponse<List<DeviceDto>> getDevicesByDeviceType(@PathVariable String deviceType) {
        List<DeviceDto> devices = deviceService.getDevicesByDeviceType(deviceType);
        return ApiResponse.success(devices);
    }

    /**
     * 根据启用状态获取设备列表
     */
    @GetMapping("/status/{enabled}")
    public ApiResponse<List<DeviceDto>> getDevicesByEnabled(@PathVariable Boolean enabled) {
        List<DeviceDto> devices = deviceService.getDevicesByEnabled(enabled);
        return ApiResponse.success(devices);
    }

    /**
     * 创建新设备
     */
    @PostMapping
    public ApiResponse<DeviceDto> createDevice(@Valid @RequestBody CreateDeviceRequestDto requestDto) {
        DeviceDto device = deviceService.createDevice(requestDto);
        return ApiResponse.success(device);
    }

    /**
     * 更新设备信息
     */
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
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteDevice(@PathVariable Long id) {
        deviceService.deleteDevice(id);
        return ApiResponse.success(null);
    }

    /**
     * 启用/禁用设备 - 匹配前端期望的API路径
     */
    @PutMapping("/{id}/status")
    public ApiResponse<DeviceDto> updateDeviceStatus(@PathVariable Long id, @RequestBody UpdateStatusRequest request) {
        DeviceDto device = deviceService.toggleDeviceStatus(id, request.getEnabled());
        if (device == null) {
            return ApiResponse.error(404, "设备不存在");
        }
        return ApiResponse.success(device);
    }

    /**
     * 更新设备告警开关状态 - 匹配前端期望的API路径
     */
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
    @PutMapping("/{id}/threshold")
    public ApiResponse<DeviceDto> updateThreshold(@PathVariable Long id, @RequestBody CreateDeviceRequestDto thresholdData) {
        DeviceDto device = deviceService.getDeviceById(id);
        if (device == null) {
            return ApiResponse.error(404, "设备不存在");
        }

        // 更新阈值 - 需要创建一个请求对象
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
    @PutMapping("/{id}/unbind-area")
    public ApiResponse<DeviceDto> unbindArea(@PathVariable Long id) {
        DeviceDto device = deviceService.getDeviceById(id);
        if (device == null) {
            return ApiResponse.error(404, "设备不存在");
        }

        // 解绑设备与库区的关联
        CreateDeviceRequestDto requestDto = new CreateDeviceRequestDto();
        requestDto.setDeviceCode(device.getDeviceCode());
        requestDto.setDeviceName(device.getDeviceName());
        requestDto.setDeviceType(device.getDeviceType());
        requestDto.setAreaId(null); // 解除关联
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

    // 用于接收状态更新请求的内部类
    public static class UpdateStatusRequest {
        private Boolean enabled;

        public Boolean getEnabled() {
            return enabled;
        }

        public void setEnabled(Boolean enabled) {
            this.enabled = enabled;
        }
    }

    // 用于接收告警状态更新请求的内部类
    public static class UpdateAlarmStatusRequest {
        private Boolean alarmEnabled;

        public Boolean getAlarmEnabled() {
            return alarmEnabled;
        }

        public void setAlarmEnabled(Boolean alarmEnabled) {
            this.alarmEnabled = alarmEnabled;
        }
    }

    // 用于接收批量更新状态请求的内部类
    public static class BatchUpdateStatusRequest {
        private List<Long> ids;
        private Boolean enabled;

        public List<Long> getIds() {
            return ids;
        }

        public void setIds(List<Long> ids) {
            this.ids = ids;
        }

        public Boolean getEnabled() {
            return enabled;
        }

        public void setEnabled(Boolean enabled) {
            this.enabled = enabled;
        }
    }
}