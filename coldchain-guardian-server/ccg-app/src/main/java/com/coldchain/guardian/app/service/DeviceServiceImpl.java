package com.coldchain.guardian.app.service;

import com.coldchain.guardian.contract.dto.device.DeviceDto;
import com.coldchain.guardian.contract.dto.device.CreateDeviceRequestDto;
import com.coldchain.guardian.infra.persistence.entity.DeviceEntity;
import com.coldchain.guardian.infra.persistence.repository.DeviceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class DeviceServiceImpl implements DeviceService {

    @Autowired
    private DeviceRepository deviceRepository;

    @Override
    public List<DeviceDto> getAllDevices() {
        List<DeviceEntity> entities = deviceRepository.findAll();
        return entities.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public DeviceDto getDeviceById(Long id) {
        DeviceEntity entity = deviceRepository.findById(id);
        return entity != null ? convertToDto(entity) : null;
    }

    @Override
    public DeviceDto getDeviceByDeviceCode(String deviceCode) {
        DeviceEntity entity = deviceRepository.findByDeviceCode(deviceCode);
        return entity != null ? convertToDto(entity) : null;
    }

    @Override
    public List<DeviceDto> getDevicesByAreaId(Long areaId) {
        List<DeviceEntity> entities = deviceRepository.findByAreaId(areaId);
        return entities.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<DeviceDto> getDevicesByDeviceType(String deviceType) {
        List<DeviceEntity> entities = deviceRepository.findByDeviceType(deviceType);
        return entities.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<DeviceDto> getDevicesByEnabled(Boolean enabled) {
        Integer enabledInt = enabled != null ? (enabled ? 1 : 0) : null;
        List<DeviceEntity> entities = deviceRepository.findByEnabled(enabledInt);
        return entities.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public DeviceDto createDevice(CreateDeviceRequestDto requestDto) {
        // 检查设备编码是否已存在
        if (deviceRepository.existsByDeviceCodeExcludingId(requestDto.getDeviceCode(), null)) {
            throw new RuntimeException("设备编码已存在: " + requestDto.getDeviceCode());
        }

        DeviceEntity entity = new DeviceEntity();
        entity.setDeviceCode(requestDto.getDeviceCode());
        entity.setDeviceName(requestDto.getDeviceName());
        entity.setDeviceType(requestDto.getDeviceType());
        entity.setModel(requestDto.getModel());
        entity.setManufacturer(requestDto.getManufacturer());
        entity.setSn(requestDto.getSn());
        entity.setFirmwareVersion(requestDto.getFirmwareVersion());
        entity.setAreaId(requestDto.getAreaId());
        entity.setLocationDesc(requestDto.getLocationDesc());
        entity.setThresholdMode(requestDto.getThresholdMode());
        entity.setTemperatureThresholdMin(requestDto.getTemperatureThresholdMin());
        entity.setTemperatureThresholdMax(requestDto.getTemperatureThresholdMax());
        entity.setHumidityThresholdMin(requestDto.getHumidityThresholdMin());
        entity.setHumidityThresholdMax(requestDto.getHumidityThresholdMax());
        entity.setAlarmEnabled(requestDto.getAlarmEnabled() != null ? (requestDto.getAlarmEnabled() ? 1 : 0) : 1);
        entity.setEnabled(requestDto.getEnabled() != null ? (requestDto.getEnabled() ? 1 : 0) : 1);
        entity.setOnlineStatus(requestDto.getOnlineStatus() != null ? (requestDto.getOnlineStatus() ? 1 : 0) : 0);

        deviceRepository.save(entity);
        return convertToDto(entity);
    }

    @Override
    public DeviceDto updateDevice(Long id, CreateDeviceRequestDto requestDto) {
        DeviceEntity existing = deviceRepository.findById(id);
        if (existing == null) {
            throw new RuntimeException("设备不存在，ID: " + id);
        }

        // 检查设备编码是否与其他设备冲突
        if (deviceRepository.existsByDeviceCodeExcludingId(requestDto.getDeviceCode(), id)) {
            throw new RuntimeException("设备编码已存在: " + requestDto.getDeviceCode());
        }

        existing.setDeviceCode(requestDto.getDeviceCode());
        existing.setDeviceName(requestDto.getDeviceName());
        existing.setDeviceType(requestDto.getDeviceType());
        existing.setModel(requestDto.getModel());
        existing.setManufacturer(requestDto.getManufacturer());
        existing.setSn(requestDto.getSn());
        existing.setFirmwareVersion(requestDto.getFirmwareVersion());
        existing.setAreaId(requestDto.getAreaId());
        existing.setLocationDesc(requestDto.getLocationDesc());
        existing.setThresholdMode(requestDto.getThresholdMode());
        existing.setTemperatureThresholdMin(requestDto.getTemperatureThresholdMin());
        existing.setTemperatureThresholdMax(requestDto.getTemperatureThresholdMax());
        existing.setHumidityThresholdMin(requestDto.getHumidityThresholdMin());
        existing.setHumidityThresholdMax(requestDto.getHumidityThresholdMax());
        if (requestDto.getAlarmEnabled() != null) {
            existing.setAlarmEnabled(requestDto.getAlarmEnabled() ? 1 : 0);
        }
        if (requestDto.getEnabled() != null) {
            existing.setEnabled(requestDto.getEnabled() ? 1 : 0);
        }

        deviceRepository.save(existing);
        return convertToDto(existing);
    }

    @Override
    public void deleteDevice(Long id) {
        DeviceEntity existing = deviceRepository.findById(id);
        if (existing == null) {
            throw new RuntimeException("设备不存在，ID: " + id);
        }
        deviceRepository.deleteById(id);
    }

    @Override
    public DeviceDto toggleDeviceStatus(Long id, Boolean enabled) {
        DeviceEntity existing = deviceRepository.findById(id);
        if (existing == null) {
            throw new RuntimeException("设备不存在，ID: " + id);
        }

        existing.setEnabled(enabled ? 1 : 0);
        deviceRepository.save(existing);
        return convertToDto(existing);
    }

    @Override
    public void updateDeviceOnlineStatus(Long id, Boolean online) {
        DeviceEntity existing = deviceRepository.findById(id);
        if (existing == null) {
            throw new RuntimeException("设备不存在，ID: " + id);
        }

        existing.setOnlineStatus(online ? 1 : 0);
        existing.setLastSeenTime(LocalDateTime.now()); // 更新最后上报时间
        deviceRepository.save(existing);
    }

    @Override
    public DeviceDto updateDeviceAlarmStatus(Long id, Boolean alarmEnabled) {
        DeviceEntity existing = deviceRepository.findById(id);
        if (existing == null) {
            throw new RuntimeException("设备不存在，ID: " + id);
        }

        existing.setAlarmEnabled(alarmEnabled ? 1 : 0);
        deviceRepository.save(existing);
        return convertToDto(existing);
    }

    private DeviceDto convertToDto(DeviceEntity entity) {
        DeviceDto dto = new DeviceDto();
        dto.setId(entity.getId());
        dto.setDeviceCode(entity.getDeviceCode());
        dto.setDeviceName(entity.getDeviceName());
        dto.setDeviceType(entity.getDeviceType());
        dto.setModel(entity.getModel());
        dto.setManufacturer(entity.getManufacturer());
        dto.setSn(entity.getSn());
        dto.setFirmwareVersion(entity.getFirmwareVersion());
        dto.setAreaId(entity.getAreaId());
        dto.setLocationDesc(entity.getLocationDesc());
        dto.setThresholdMode(entity.getThresholdMode());
        dto.setTemperatureThresholdMin(entity.getTemperatureThresholdMin());
        dto.setTemperatureThresholdMax(entity.getTemperatureThresholdMax());
        dto.setHumidityThresholdMin(entity.getHumidityThresholdMin());
        dto.setHumidityThresholdMax(entity.getHumidityThresholdMax());
        dto.setAlarmEnabled(entity.getAlarmEnabled() != null && entity.getAlarmEnabled() == 1);
        dto.setEnabled(entity.getEnabled() != null && entity.getEnabled() == 1);
        dto.setOnlineStatus(entity.getOnlineStatus() != null && entity.getOnlineStatus() == 1);
        dto.setLastSeenTime(entity.getLastSeenTime());
        return dto;
    }
}