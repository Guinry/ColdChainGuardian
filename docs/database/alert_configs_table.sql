CREATE TABLE IF NOT EXISTS alert_configs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    device_id BIGINT COMMENT '设备ID(为空表示全局配置)',
    warehouse_id BIGINT COMMENT '库区ID(为空表示全局配置)',

    alert_type ENUM('TEMP_HIGH', 'TEMP_LOW', 'HUMI_HIGH', 'HUMI_LOW', 'DEVICE_OFFLINE') NOT NULL COMMENT '告警类型',
    threshold_value DECIMAL(10,2) COMMENT '阈值',
    alert_level ENUM('LOW', 'MEDIUM', 'HIGH', 'CRITICAL') DEFAULT 'MEDIUM' COMMENT '告警级别',
    enabled TINYINT DEFAULT 1 COMMENT '是否启用',
    notification_methods JSON COMMENT '通知方式(邮件、短信、APP推送)',
    cool_down_minutes INT DEFAULT 5 COMMENT '冷却时间(分钟)',
    created_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    INDEX idx_scope_type (device_id, warehouse_id, alert_type),
    INDEX idx_enabled_type (enabled, alert_type),

    CONSTRAINT fk_cfg_device FOREIGN KEY (device_id) REFERENCES devices(id),

    -- 关键修复：指向 warehouse_areas（字段名 warehouse_id 不变）
    CONSTRAINT fk_cfg_warehouse_area FOREIGN KEY (warehouse_id) REFERENCES warehouse_areas(id)

    -- 可选：如果你想避免同一作用域同类型出现重复配置，可以加（注意 NULL 行为）
    , UNIQUE KEY uk_cfg (device_id, warehouse_id, alert_type)
) COMMENT='告警配置';