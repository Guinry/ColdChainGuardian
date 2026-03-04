package com.coldchain.guardian.infra.persistence.repository;

import com.coldchain.guardian.infra.persistence.entity.TelemetryEntity;
import com.coldchain.guardian.infra.persistence.mapper.TelemetryMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public class TelemetryRepository {

    @Autowired
    private TelemetryMapper telemetryMapper;

    public List<TelemetryEntity> findByDeviceIdAndTimeRange(Long deviceId, LocalDateTime startTime, LocalDateTime endTime) {
        // This would require a custom method in the mapper or a query wrapper
        // For now, returning all records and filtering in memory
        List<TelemetryEntity> allRecords = findAll();
        return allRecords.stream()
                .filter(telemetry -> telemetry.getDataTime().isAfter(startTime) &&
                                   telemetry.getDataTime().isBefore(endTime))
                .filter(telemetry -> deviceId == null || telemetry.getDeviceId().equals(deviceId))
                .toList();
    }

    public List<TelemetryEntity> findAll() {
        return telemetryMapper.selectList(null);
    }

    public List<TelemetryEntity> findLatestByDevice(Long deviceId, int limit) {
        // This would be a custom query to get the latest records for a device
        List<TelemetryEntity> allRecords = findAll();
        return allRecords.stream()
                .filter(telemetry -> telemetry.getDeviceId().equals(deviceId))
                .sorted((t1, t2) -> t2.getDataTime().compareTo(t1.getDataTime()))
                .limit(limit)
                .toList();
    }
}