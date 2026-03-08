package com.coldchain.guardian.infra.persistence.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sensor_data")
public class TelemetryEntity extends BaseEntity {

    @TableField("device_id")
    private Long deviceId;

    @TableField("temperature")
    private Double temperature;

    @TableField("humidity")
    private Double humidity;

    @TableField("battery_level")
    private Double batteryLevel;

    @TableField("signal_strength")
    private Integer signalStrength;

    @TableField("raw_data")
    private String rawData;

    @TableField("data_time")
    private LocalDateTime dataTime;

    @TableField("created_time")
    private LocalDateTime createdTime;

    @TableField("update_time")
    private LocalDateTime updateTime;
}