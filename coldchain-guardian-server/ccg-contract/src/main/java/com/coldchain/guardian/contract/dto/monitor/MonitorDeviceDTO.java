package com.coldchain.guardian.contract.dto.monitor;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class MonitorDeviceDTO {
    private Long id;
    private String deviceCode;
    private String deviceName;
    private String deviceType;
    private Long areaId;
    private String areaName;
    private String areaPath;
    private Boolean online;
    private LocalDateTime lastSeenTime;
    private BigDecimal latestTemp;
    private BigDecimal latestHumi;
    private LocalDateTime latestDataTime;
    private Boolean hasUnresolvedAlert;
    private String highestAlertLevel;
}