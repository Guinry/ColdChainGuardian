CREATE TABLE IF NOT EXISTS devices (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,

    -- 设备基础信息
    device_code VARCHAR(50) NOT NULL UNIQUE COMMENT '设备编码（唯一）',
    device_name VARCHAR(100) NOT NULL COMMENT '设备名称',
    device_type VARCHAR(50) NOT NULL COMMENT '设备类型（TEMP_HUM / FREEZER / VEHICLE / DOOR ...）',
    model VARCHAR(50) NULL COMMENT '型号',
    manufacturer VARCHAR(100) NULL COMMENT '厂商',
    sn VARCHAR(100) NULL COMMENT '序列号',
    firmware_version VARCHAR(50) NULL COMMENT '固件版本',

    -- 绑定库区（建议绑定到 AREA/BIN）
    area_id BIGINT NOT NULL COMMENT '所属库区ID(warehouse_areas.id)',
    location_desc VARCHAR(200) NULL COMMENT '设备位置描述（如A栋2层东区/货架3）',

    -- 阈值策略：继承库区 or 设备覆盖
    threshold_mode VARCHAR(20) NOT NULL DEFAULT 'INHERIT' COMMENT '阈值模式：INHERIT/OVERRIDE',
    temperature_threshold_min DECIMAL(5,2) NULL COMMENT '设备温度下限(覆盖时生效)',
    temperature_threshold_max DECIMAL(5,2) NULL COMMENT '设备温度上限(覆盖时生效)',
    humidity_threshold_min DECIMAL(5,2) NULL COMMENT '设备湿度下限(覆盖时生效)',
    humidity_threshold_max DECIMAL(5,2) NULL COMMENT '设备湿度上限(覆盖时生效)',
    alarm_enabled TINYINT NOT NULL DEFAULT 1 COMMENT '是否启用告警(1是0否)',

    -- 状态
    enabled TINYINT NOT NULL DEFAULT 1 COMMENT '启用状态(1启用0禁用)',
    online_status TINYINT NOT NULL DEFAULT 0 COMMENT '在线状态(1在线0离线)',
    last_seen_time TIMESTAMP NULL COMMENT '最后上报/心跳时间',

    -- 扩展字段：通用信息放 JSON（可选，但很实用）
    extra JSON NULL COMMENT '扩展信息(JSON)：如安装参数/通讯方式/IMEI等',

    created_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    creator_id BIGINT NULL,
    updater_id BIGINT NULL,

    INDEX idx_area (area_id),
    INDEX idx_type (device_type),
    INDEX idx_enabled (enabled),
    INDEX idx_online (online_status),
    INDEX idx_last_seen (last_seen_time),
    CONSTRAINT fk_device_area FOREIGN KEY (area_id) REFERENCES warehouse_areas(id)
) COMMENT='设备表';