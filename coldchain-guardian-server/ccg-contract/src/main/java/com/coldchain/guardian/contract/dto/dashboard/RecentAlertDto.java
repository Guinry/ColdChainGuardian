package com.coldchain.guardian.contract.dto.dashboard;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 最近告警数据传输对象
 */
@Data
public class RecentAlertDto {
    private Long id;
    private String timestamp;
    private String area;
    private String device;
    private String type;
    private String level;  // 高、中、低
    private String status; // 未处理、处理中、已处理

    public RecentAlertDto(Long id, String timestamp, String area, String device,
                         String type, String level, String status) {
        this.id = id;
        this.timestamp = timestamp;
        this.area = area;
        this.device = device;
        this.type = type;
        this.level = level;
        this.status = status;
    }
}