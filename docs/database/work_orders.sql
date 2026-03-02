CREATE TABLE IF NOT EXISTS work_orders (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_no VARCHAR(50) NOT NULL UNIQUE COMMENT '工单编号(如 WO-20260303-001)',
    ref_alert_id BIGINT NULL COMMENT '触发该工单的源告警ID(手动建单可为空)',

    -- 工单基础信息
    title VARCHAR(200) NOT NULL COMMENT '工单标题',
    description TEXT COMMENT '工单详细描述',
    priority ENUM('LOW', 'MEDIUM', 'HIGH', 'URGENT') DEFAULT 'MEDIUM' COMMENT '优先级',
    order_type ENUM('ALERT_FIX', 'INSPECTION', 'MAINTENANCE') DEFAULT 'ALERT_FIX' COMMENT '工单类型',

    -- 关联位置(冗余字段提速)
    warehouse_id BIGINT NOT NULL COMMENT '发生库区ID',
    device_id BIGINT NULL COMMENT '关联设备ID(如有)',

    -- 状态与人员
    status ENUM('PENDING', 'PROCESSING', 'VERIFYING', 'COMPLETED', 'CLOSED') DEFAULT 'PENDING' COMMENT '状态: 待处理/处理中/待验收/已完成/已关闭',
    creator_id BIGINT NOT NULL COMMENT '创建人ID',
    assigned_to BIGINT NULL COMMENT '当前指派给(处理人ID)',

    -- 时间线
    due_time TIMESTAMP NULL COMMENT '要求完成时间',
    completed_time TIMESTAMP NULL COMMENT '实际完成时间',
    created_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    INDEX idx_status (status),
    INDEX idx_assigned (assigned_to),
    CONSTRAINT fk_wo_warehouse FOREIGN KEY (warehouse_id) REFERENCES warehouse_areas(id)
) COMMENT='设备运维工单表';