package com.coldchain.guardian.infra.persistence.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("work_orders")
public class WorkOrderEntity extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("order_no")
    private String orderNo; // 工单编号

    @TableField("ref_alert_id")
    private Long alertId; // 关联的告警ID

    @TableField("title")
    private String title; // 工单标题

    @TableField("description")
    private String description; // 工单描述

    @TableField("status")
    private String status; // 状态 ENUM('PENDING', 'PROCESSING', 'VERIFYING', 'COMPLETED', 'CLOSED')

    @TableField("priority")
    private String priority; // 优先级 ENUM('LOW', 'MEDIUM', 'HIGH', 'URGENT')

    @TableField("assigned_to")
    private Long assigneeId; // 责任人ID

    @TableField(value = "assignee_name", exist = false)
    private String assigneeName; // 责任人姓名 (关联查询字段)

    @TableField("creator_id")
    private Long reporterId; // 报告人ID (理论上应该是创建者ID)

    @TableField(value = "reporter_name", exist = false)
    private String reporterName; // 报告人姓名 (关联查询字段)

    @TableField("due_time")
    private LocalDateTime dueDate; // 截止日期

    @TableField("completed_time")
    private LocalDateTime completedTime; // 完成时间

    @TableField("verified_time")
    private LocalDateTime verifiedTime; // 验收时间

    @TableField("verification_result")
    private String verificationResult; // 验收结果

    @TableField("order_type")
    private String workType; // 工单类型 ENUM('ALERT_FIX', 'INSPECTION', 'MAINTENANCE')

    @TableField("warehouse_id")
    private Long warehouseId; // 库区ID

    @TableField(value = "warehouse_name", exist = false)
    private String warehouseName; // 库区名称 (关联查询字段)

    @TableField("device_id")
    private Long deviceId; // 设备ID

    @TableField(value = "device_name", exist = false)
    private String deviceName; // 设备名称 (关联查询字段)

    @TableField("location_detail")
    private String locationDetail; // 详细位置

    // 构造函数
    public WorkOrderEntity() {}
}