CREATE TABLE IF NOT EXISTS alerts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    device_id BIGINT NOT NULL COMMENT '设备ID',
    warehouse_id BIGINT NOT NULL COMMENT '库区ID',
    alert_config_id BIGINT COMMENT '触发的告警配置ID',

    -- 告警详情
    alert_type VARCHAR(50) NOT NULL COMMENT '告警类型(TEMP_HIGH, DEVICE_OFFLINE等)',
    alert_level ENUM('LOW', 'MEDIUM', 'HIGH', 'CRITICAL') NOT NULL COMMENT '告警级别',
    message TEXT NOT NULL COMMENT '告警消息',
    temperature DECIMAL(5,2) COMMENT '告警时温度',
    humidity DECIMAL(5,2) COMMENT '告警时湿度',
    threshold_value DECIMAL(10,2) COMMENT '触发阈值',

    -- 处理状态与关联
    status ENUM('UNHANDLED', 'HANDLING', 'RESOLVED', 'IGNORED') DEFAULT 'UNHANDLED' COMMENT '处理状态',
    work_order_id BIGINT NULL COMMENT '关联的工单ID（若已转工单）',
    handler_user_id BIGINT COMMENT '处理人ID(快速处理时使用)',

    -- 时间线
    handle_time TIMESTAMP NULL COMMENT '处理/转工单时间',
    handle_remark TEXT COMMENT '处理备注',
    created_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '告警发生时间',
    updated_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    INDEX idx_status (status),
    INDEX idx_level (alert_level),
    CONSTRAINT fk_alerts_device FOREIGN KEY (device_id) REFERENCES devices(id),
    CONSTRAINT fk_alerts_work_order FOREIGN KEY (work_order_id) REFERENCES work_orders(id)
) COMMENT='告警记录表';