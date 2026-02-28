package com.coldchain.guardian.infra.persistence.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("alerts")
public class AlertEntity extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("alert_config_id")
    private Long alertConfigId; // 告警配置ID

    @TableField("device_id")
    private Long deviceId; // 设备ID

    @TableField("warehouse_id")
    private Long warehouseId; // 库区ID

    @TableField("alert_type")
    private String alertType; // 告警类型

    @TableField("alert_level")
    private String alertLevel; // 告警级别 ENUM('LOW', 'MEDIUM', 'HIGH', 'CRITICAL')

    @TableField("temperature")
    private BigDecimal temperature; // 告警时温度

    @TableField("humidity")
    private BigDecimal humidity; // 告警时湿度

    @TableField("threshold_value")
    private BigDecimal thresholdValue; // 触发阈值

    @TableField("message")
    private String message; // 告警消息

    @TableField("status")
    private String status; // 处理状态 ENUM('UNHANDLED', 'HANDLING', 'RESOLVED', 'IGNORED')

    @TableField("handler_user_id")
    private Long handlerUserId; // 处理人ID

    @TableField("handle_time")
    private LocalDateTime handleTime; // 处理时间

    @TableField("handle_remark")
    private String handleRemark; // 处理备注

    @TableField("acknowledged_time")
    private LocalDateTime acknowledgedTime; // 确认时间

    @TableField("resolved_time")
    private LocalDateTime resolvedTime; // 解决时间

    @TableField("first_time")
    private LocalDateTime firstTime; // 首次触发时间

    @TableField("last_time")
    private LocalDateTime lastTime; // 最后一次触发时间

    @TableField("trigger_count")
    private Integer triggerCount; // 触发次数

    @TableField("device_code")
    private String deviceCode; // 设备编码

    @TableField("device_name")
    private String deviceName; // 设备名称

    @TableField("area_id")
    private Long areaId; // 库区ID

    @TableField("area_name")
    private String areaName; // 库区名称

    @TableField("handler_name")
    private String handlerName; // 处理人姓名

    @TableField("temperature_threshold_min")
    private BigDecimal temperatureThresholdMin; // 温度阈值下限

    @TableField("temperature_threshold_max")
    private BigDecimal temperatureThresholdMax; // 温度阈值上限

    @TableField("humidity_threshold_min")
    private BigDecimal humidityThresholdMin; // 湿度阈值下限

    @TableField("humidity_threshold_max")
    private BigDecimal humidityThresholdMax; // 湿度阈值上限

    // 构造函数
    public AlertEntity() {}
}