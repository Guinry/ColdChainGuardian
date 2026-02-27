CREATE TABLE IF NOT EXISTS alert_rules (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,

    rule_code VARCHAR(50) NOT NULL UNIQUE COMMENT '规则编码',
    rule_name VARCHAR(100) NOT NULL COMMENT '规则名称',

    -- 作用范围：三选一或多选一（建议至少填一个）
    scope_type VARCHAR(20) NOT NULL DEFAULT 'AREA'
    COMMENT '规则范围：AREA/DEVICE/DEVICE_TYPE/GLOBAL',
    area_id BIGINT NULL COMMENT '绑定库区(warehouse_areas.id)',
    device_id BIGINT NULL COMMENT '绑定设备(devices.id)',
    device_type VARCHAR(50) NULL COMMENT '绑定设备类型',

    -- 指标/类型
    metric VARCHAR(30) NOT NULL COMMENT '指标：TEMPERATURE/HUMIDITY/ONLINE/BATTERY/DOOR...',
    operator VARCHAR(10) NOT NULL COMMENT '操作符：GT/GE/LT/LE/EQ/NE/BETWEEN',
    threshold_min DECIMAL(10,2) NULL COMMENT '阈值下限（BETWEEN/下限）',
    threshold_max DECIMAL(10,2) NULL COMMENT '阈值上限（BETWEEN/上限）',

    -- 触发与恢复策略
    duration_seconds INT NOT NULL DEFAULT 0 COMMENT '持续多久触发（0表示立即触发）',
    recovery_mode VARCHAR(20) NOT NULL DEFAULT 'AUTO'
    COMMENT '恢复方式：AUTO/MANUAL（AUTO=恢复条件满足自动恢复）',
    recovery_duration_seconds INT NOT NULL DEFAULT 0 COMMENT '恢复持续时间（防抖）',

    -- 告警级别与去重
    alert_level VARCHAR(20) NOT NULL DEFAULT 'MEDIUM' COMMENT '级别：LOW/MEDIUM/HIGH/CRITICAL',
    dedup_key VARCHAR(100) NULL COMMENT '去重Key（为空则系统按scope+metric生成）',
    repeat_interval_seconds INT NOT NULL DEFAULT 300 COMMENT '重复通知间隔(秒)',

    -- 是否启用
    enabled TINYINT NOT NULL DEFAULT 1 COMMENT '启用状态(1启用0禁用)',
    remark VARCHAR(500) NULL,

    created_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    creator_id BIGINT NULL,
    updater_id BIGINT NULL,

    INDEX idx_scope (scope_type, area_id, device_id, device_type),
    INDEX idx_metric (metric),
    INDEX idx_enabled (enabled),

    CONSTRAINT fk_rule_area FOREIGN KEY (area_id) REFERENCES warehouse_areas(id),
    CONSTRAINT fk_rule_device FOREIGN KEY (device_id) REFERENCES devices(id)
) COMMENT='告警规则';