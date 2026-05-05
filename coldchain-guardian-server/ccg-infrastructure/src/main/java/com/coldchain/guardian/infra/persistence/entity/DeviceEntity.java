package com.coldchain.guardian.infra.persistence.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("devices")
public class DeviceEntity extends BaseEntity {

    @TableField("device_code")
    private String deviceCode;

    @TableField("device_name")
    private String deviceName;

    @TableField("device_type")
    private String deviceType;

    @TableField("model")
    private String model;

    @TableField("manufacturer")
    private String manufacturer;

    @TableField("sn")
    private String sn;

    @TableField("firmware_version")
    private String firmwareVersion;

    @TableField("area_id")
    private Long areaId;

    @TableField("location_desc")
    private String locationDesc;

    @TableField("threshold_mode")
    private String thresholdMode;

    @TableField("temperature_threshold_min")
    private BigDecimal temperatureThresholdMin;

    @TableField("temperature_threshold_max")
    private BigDecimal temperatureThresholdMax;

    @TableField("humidity_threshold_min")
    private BigDecimal humidityThresholdMin;

    @TableField("humidity_threshold_max")
    private BigDecimal humidityThresholdMax;

    @TableField("alarm_enabled")
    private Integer alarmEnabled;

    @TableField("enabled")
    private Integer enabled;

    @TableField("online_status")
    private Integer onlineStatus;

    @TableField("last_seen_time")
    private LocalDateTime lastSeenTime;

    @TableField("extra")
    private String extra;

    @TableField("latest_temp")
    private BigDecimal latestTemp;

    @TableField("latest_humi")
    private BigDecimal latestHumi;

    @TableField("latest_data_time")
    private LocalDateTime latestDataTime;

    @TableField("has_unresolved_alert")
    private Integer hasUnresolvedAlert;
}
