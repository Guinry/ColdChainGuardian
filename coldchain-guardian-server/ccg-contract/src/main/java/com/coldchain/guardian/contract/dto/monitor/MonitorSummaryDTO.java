package com.coldchain.guardian.contract.dto.monitor;

import lombok.Data;

@Data
public class MonitorSummaryDTO {
    private Integer enabledDevices;      // 启用设备数
    private Integer onlineDevices;       // 在线设备数
    private Integer offlineDevices;      // 离线设备数
    private Integer alarmingDevices;     // 告警中设备数
    private Integer unhandledAlerts;     // 未处理告警数
    private Integer todayAlerts;         // 今日告警数
    private Integer last5MinReportDevices; // 最近5分钟上报设备数
}