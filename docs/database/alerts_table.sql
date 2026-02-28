CREATE TABLE IF NOT EXISTS alerts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    alert_config_id BIGINT NOT NULL COMMENT '告警配置ID',
    device_id BIGINT NOT NULL COMMENT '设备ID',
    warehouse_id BIGINT NOT NULL COMMENT '库区ID',

    alert_type VARCHAR(50) NOT NULL COMMENT '告警类型',
    alert_level ENUM('LOW', 'MEDIUM', 'HIGH', 'CRITICAL') NOT NULL COMMENT '告警级别',
    temperature DECIMAL(5,2) COMMENT '告警时温度',
    humidity DECIMAL(5,2) COMMENT '告警时湿度',
    threshold_value DECIMAL(10,2) COMMENT '触发阈值',
    message TEXT COMMENT '告警消息',

    status ENUM('UNHANDLED', 'HANDLING', 'RESOLVED', 'IGNORED') DEFAULT 'UNHANDLED' COMMENT '处理状态',
    handler_user_id BIGINT COMMENT '处理人ID',
    handle_time TIMESTAMP NULL COMMENT '处理时间',
    handle_remark TEXT COMMENT '处理备注',
    acknowledged_time TIMESTAMP NULL COMMENT '确认时间',
    resolved_time TIMESTAMP NULL COMMENT '解决时间',

    created_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    -- 可选增强：持续告警统计（不影响现有逻辑）
    first_time TIMESTAMP NULL COMMENT '首次触发时间',
    last_time TIMESTAMP NULL COMMENT '最后一次触发时间',
    trigger_count INT DEFAULT 1 COMMENT '触发次数',

    INDEX idx_status_time (status, created_time),
    INDEX idx_device_time (device_id, created_time),
    INDEX idx_wh_time (warehouse_id, created_time),
    INDEX idx_type_time (alert_type, created_time),

    CONSTRAINT fk_alert_cfg FOREIGN KEY (alert_config_id) REFERENCES alert_configs(id),
    CONSTRAINT fk_alert_device FOREIGN KEY (device_id) REFERENCES devices(id),

    -- 关键修复：指向 warehouse_areas（字段名 warehouse_id 不变）
    CONSTRAINT fk_alert_warehouse_area FOREIGN KEY (warehouse_id) REFERENCES warehouse_areas(id),

    CONSTRAINT fk_alert_handler FOREIGN KEY (handler_user_id) REFERENCES users(id)
) COMMENT='告警记录';