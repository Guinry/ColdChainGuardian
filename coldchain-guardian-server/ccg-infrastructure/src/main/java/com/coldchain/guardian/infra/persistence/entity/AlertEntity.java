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

    @TableField("work_order_id")
    private Long workOrderId; // 关联的工单ID

    // 告警收敛相关字段
    @TableField("first_time")
    private LocalDateTime firstTime; // 首次触发时间

    @TableField("last_time")
    private LocalDateTime lastTime; // 最后一次触发时间

    @TableField("trigger_count")
    private Integer triggerCount; // 触发次数

    @TableField(value = "device_code", exist = false)
    private String deviceCode; // 设备编码

    @TableField(value = "device_name", exist = false)
    private String deviceName; // 设备名称

    @TableField(value = "area_id", exist = false)
    private Long areaId; // 库区ID

    @TableField(value = "area_name", exist = false)
    private String areaName; // 库区名称

    @TableField(value = "handler_name", exist = false)
    private String handlerName; // 处理人姓名

    @TableField(value = "temperature_threshold_min", exist = false)
    private BigDecimal temperatureThresholdMin; // 温度阈值下限

    @TableField(value = "temperature_threshold_max", exist = false)
    private BigDecimal temperatureThresholdMax; // 温度阈值上限

    @TableField(value = "humidity_threshold_min", exist = false)
    private BigDecimal humidityThresholdMin; // 湿度阈值下限

    @TableField(value = "humidity_threshold_max", exist = false)
    private BigDecimal humidityThresholdMax; // 湿度阈值上限

    // 构造函数
    public AlertEntity() {}
}