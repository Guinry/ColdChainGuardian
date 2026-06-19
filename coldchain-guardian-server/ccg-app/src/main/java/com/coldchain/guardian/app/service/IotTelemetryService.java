package com.coldchain.guardian.app.service;

import com.coldchain.guardian.contract.dto.iot.IotTelemetryRequest;
import com.coldchain.guardian.contract.dto.iot.IotTelemetryResponse;
import com.coldchain.guardian.infra.persistence.entity.DeviceEntity;
import com.coldchain.guardian.infra.persistence.entity.TelemetryEntity;
import com.coldchain.guardian.infra.persistence.mapper.TelemetryMapper;
import com.coldchain.guardian.infra.persistence.repository.DeviceRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;

@Service
public class IotTelemetryService {

    @Autowired
    private DeviceRepository deviceRepository;

    @Autowired
    private TelemetryMapper telemetryMapper;

    @Autowired
    private ObjectMapper objectMapper;

    @Transactional
    public IotTelemetryResponse receiveTelemetry(IotTelemetryRequest request) {
        DeviceEntity device = deviceRepository.findByDeviceCode(request.getDeviceCode());
        if (device == null) {
            throw new IllegalArgumentException("Device not found: " + request.getDeviceCode());
        }

        LocalDateTime dataTime = parseReportedAt(request.getReportedAt());
        LocalDateTime now = LocalDateTime.now();

        TelemetryEntity telemetry = new TelemetryEntity();
        telemetry.setDeviceId(device.getId());
        telemetry.setTemperature(request.getTemperature());
        telemetry.setHumidity(request.getHumidity());
        telemetry.setBatteryLevel(request.getBatteryLevel());
        telemetry.setSignalStrength(request.getRssi());
        telemetry.setRawData(toRawJson(request));
        telemetry.setDataTime(dataTime);
        telemetry.setUpdateTime(now);
        telemetryMapper.insert(telemetry);

        device.setOnlineStatus(1);
        device.setLastSeenTime(now);
        device.setLatestTemp(BigDecimal.valueOf(request.getTemperature()));
        device.setLatestHumi(BigDecimal.valueOf(request.getHumidity()));
        device.setLatestDataTime(dataTime);
        deviceRepository.save(device);

        IotTelemetryResponse response = new IotTelemetryResponse();
        response.setTelemetryId(telemetry.getId());
        response.setDeviceId(device.getId());
        response.setDeviceCode(device.getDeviceCode());
        response.setTemperature(request.getTemperature());
        response.setHumidity(request.getHumidity());
        response.setDataTime(dataTime);
        return response;
    }

    private LocalDateTime parseReportedAt(String reportedAt) {
        if (reportedAt == null || reportedAt.isBlank()) {
            return LocalDateTime.now();
        }

        try {
            return LocalDateTime.parse(reportedAt.replace("Z", ""));
        } catch (DateTimeParseException ex) {
            return LocalDateTime.now();
        }
    }

    private String toRawJson(IotTelemetryRequest request) {
        try {
            return objectMapper.writeValueAsString(request);
        } catch (JsonProcessingException ex) {
            return "{}";
        }
    }
}
