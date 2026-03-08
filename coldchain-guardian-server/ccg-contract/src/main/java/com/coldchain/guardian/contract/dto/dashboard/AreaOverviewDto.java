package com.coldchain.guardian.contract.dto.dashboard;

import lombok.Data;

/**
 * 库区概览数据传输对象
 */
@Data
public class AreaOverviewDto {
    private Long id;
    private String name;
    private Double temperature;
    private Double humidity;
    private String status;  // normal, warning, error
    private String statusText;  // 正常, 警告, 异常
    private Integer onlineDevices;
    private Integer totalDevices;

    public AreaOverviewDto(Long id, String name, Double temperature, Double humidity,
                          String status, String statusText, Integer onlineDevices, Integer totalDevices) {
        this.id = id;
        this.name = name;
        this.temperature = temperature;
        this.humidity = humidity;
        this.status = status;
        this.statusText = statusText;
        this.onlineDevices = onlineDevices;
        this.totalDevices = totalDevices;
    }
}