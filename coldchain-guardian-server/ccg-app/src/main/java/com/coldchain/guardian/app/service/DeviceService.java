package com.coldchain.guardian.app.service;

import com.coldchain.guardian.contract.dto.device.DeviceDto;
import com.coldchain.guardian.contract.dto.device.CreateDeviceRequestDto;
import java.util.List;

public interface DeviceService {

    /**
     * 获取所有设备
     */
    List<DeviceDto> getAllDevices();

    /**
     * 根据ID获取设备
     */
    DeviceDto getDeviceById(Long id);

    /**
     * 根据设备编码获取设备
     */
    DeviceDto getDeviceByDeviceCode(String deviceCode);

    /**
     * 根据库区ID获取设备列表
     */
    List<DeviceDto> getDevicesByAreaId(Long areaId);

    /**
     * 根据设备类型获取设备列表
     */
    List<DeviceDto> getDevicesByDeviceType(String deviceType);

    /**
     * 根据启用状态获取设备列表
     */
    List<DeviceDto> getDevicesByEnabled(Boolean enabled);

    /**
     * 根据条件获取分页设备列表
     */
    List<DeviceDto> getDevicesWithFilters(Integer page, Integer size, String keyword, String deviceType,
                                        Boolean onlineStatus, Boolean enabled, Boolean alarmEnabled, Long areaId);

    /**
     * 获取符合条件的设备总数
     */
    int getDeviceCountWithFilters(String keyword, String deviceType,
                                Boolean onlineStatus, Boolean enabled, Boolean alarmEnabled, Long areaId);

    /**
     * 创建新设备
     */
    DeviceDto createDevice(CreateDeviceRequestDto requestDto);

    /**
     * 更新设备信息
     */
    DeviceDto updateDevice(Long id, CreateDeviceRequestDto requestDto);

    /**
     * 删除设备
     */
    void deleteDevice(Long id);

    /**
     * 启用/禁用设备
     */
    DeviceDto toggleDeviceStatus(Long id, Boolean enabled);

    /**
     * 更新设备在线状态
     */
    void updateDeviceOnlineStatus(Long id, Boolean online);

    /**
     * 更新设备告警开关状态
     */
    DeviceDto updateDeviceAlarmStatus(Long id, Boolean alarmEnabled);
}