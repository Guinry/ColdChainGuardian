package com.coldchain.guardian.app.service;

import com.coldchain.guardian.app.service.DeviceService;
import com.coldchain.guardian.contract.dto.telemetry.TelemetryDto;
import com.coldchain.guardian.infra.persistence.entity.TelemetryEntity;
import com.coldchain.guardian.infra.persistence.mapper.TelemetryMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TelemetryService {

    @Autowired
    private TelemetryMapper telemetryMapper;

    @Autowired
    private DeviceService deviceService;

    /**
     * 根据设备ID获取遥测数据列表（分页）
     */
    public List<TelemetryDto> getTelemetryByDeviceId(Long deviceId, Integer page, Integer size, String startTime, String endTime) {
        // 将字符串转换为LocalDateTime
        LocalDateTime startDateTime = null;
        LocalDateTime endDateTime = null;

        if (startTime != null && !startTime.isEmpty()) {
            startDateTime = LocalDateTime.parse(startTime.replace("Z", ""), DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        }
        if (endTime != null && !endTime.isEmpty()) {
            endDateTime = LocalDateTime.parse(endTime.replace("Z", ""), DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        }

        // 构建查询条件
        LambdaQueryWrapper<TelemetryEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TelemetryEntity::getDeviceId, deviceId);

        if (startDateTime != null) {
            queryWrapper.ge(TelemetryEntity::getDataTime, startDateTime);
        }
        if (endDateTime != null) {
            queryWrapper.le(TelemetryEntity::getDataTime, endDateTime);
        }

        queryWrapper.orderByDesc(TelemetryEntity::getDataTime);

        int offset = (page - 1) * size;
        List<TelemetryEntity> entities = telemetryMapper.selectList(queryWrapper.last("LIMIT " + size + " OFFSET " + offset));

        return entities.stream()
                .map(this::convertToTelemetryDto)
                .collect(Collectors.toList());
    }

    /**
     * 根据设备ID获取遥测数据总数
     */
    public long countTelemetryByDeviceId(Long deviceId, String startTime, String endTime) {
        LocalDateTime startDateTime = null;
        LocalDateTime endDateTime = null;

        if (startTime != null && !startTime.isEmpty()) {
            startDateTime = LocalDateTime.parse(startTime.replace("Z", ""), DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        }
        if (endTime != null && !endTime.isEmpty()) {
            endDateTime = LocalDateTime.parse(endTime.replace("Z", ""), DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        }

        // 构建查询条件
        LambdaQueryWrapper<TelemetryEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TelemetryEntity::getDeviceId, deviceId);

        if (startDateTime != null) {
            queryWrapper.ge(TelemetryEntity::getDataTime, startDateTime);
        }
        if (endDateTime != null) {
            queryWrapper.le(TelemetryEntity::getDataTime, endDateTime);
        }

        return telemetryMapper.selectCount(queryWrapper);
    }

    /**
     * 根据设备ID获取最新的遥测数据
     */
    public TelemetryDto getLatestTelemetryByDeviceId(Long deviceId) {
        LambdaQueryWrapper<TelemetryEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TelemetryEntity::getDeviceId, deviceId);
        queryWrapper.orderByDesc(TelemetryEntity::getDataTime);
        queryWrapper.last("LIMIT 1");

        List<TelemetryEntity> entities = telemetryMapper.selectList(queryWrapper);
        TelemetryEntity entity = entities.isEmpty() ? null : entities.get(0);

        return entity != null ? convertToTelemetryDto(entity) : null;
    }

    /**
     * 获取设备历史数据用于图表显示
     */
    public List<TelemetryDto> getHistoricalTelemetryForChart(Long deviceId, String startTime, String endTime, Integer limit) {
        LocalDateTime startDateTime = null;
        LocalDateTime endDateTime = null;

        if (startTime != null && !startTime.isEmpty()) {
            startDateTime = LocalDateTime.parse(startTime.replace("Z", ""), DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        }
        if (endTime != null && !endTime.isEmpty()) {
            endDateTime = LocalDateTime.parse(endTime.replace("Z", ""), DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        }

        // 构建查询条件
        LambdaQueryWrapper<TelemetryEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TelemetryEntity::getDeviceId, deviceId);

        if (startDateTime != null) {
            queryWrapper.ge(TelemetryEntity::getDataTime, startDateTime);
        }
        if (endDateTime != null) {
            queryWrapper.le(TelemetryEntity::getDataTime, endDateTime);
        }

        queryWrapper.orderByAsc(TelemetryEntity::getDataTime);

        if (limit != null && limit > 0) {
            queryWrapper.last("LIMIT " + limit);
        }

        List<TelemetryEntity> entities = telemetryMapper.selectList(queryWrapper);

        return entities.stream()
                .map(this::convertToTelemetryDto)
                .collect(Collectors.toList());
    }

    private TelemetryDto convertToTelemetryDto(TelemetryEntity entity) {
        TelemetryDto dto = new TelemetryDto();
        dto.setId(entity.getId());
        dto.setDeviceId(entity.getDeviceId());
        dto.setDeviceCode(entity.getDeviceCode());
        dto.setDeviceName(entity.getDeviceName());
        dto.setTemperature(entity.getTemperature());
        dto.setHumidity(entity.getHumidity());
        dto.setBatteryLevel(entity.getBatteryLevel());
        dto.setSignalStrength(entity.getSignalStrength());
        dto.setRawData(entity.getRawData());
        dto.setDataTime(entity.getDataTime());

        // 如果需要timestamp，也可以设置
        if (entity.getDataTime() != null) {
            dto.setTimestamp(entity.getDataTime().atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli());
        }

        return dto;
    }
}