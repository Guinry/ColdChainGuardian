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
@TableName("work_order_logs")
public class WorkOrderLogEntity extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("work_order_id")
    private Long workOrderId; // 工单ID

    @TableField("action")
    private String action; // 操作动作

    @TableField("previous_status")
    private String previousStatus; // 之前状态

    @TableField("current_status")
    private String currentStatus; // 当前状态

    @TableField("operator_id")
    private Long operatorId; // 操作人ID

    @TableField("operator_name")
    private String operatorName; // 操作人姓名

    @TableField("remark")
    private String remark; // 备注

    // 构造函数
    public WorkOrderLogEntity() {}
}