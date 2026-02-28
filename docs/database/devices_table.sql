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

INSERT INTO devices (
    device_code,
    device_name,
    device_type,
    model,
    manufacturer,
    sn,
    firmware_version,
    area_id,
    location_desc,
    threshold_mode,
    temperature_threshold_min,
    temperature_threshold_max,
    humidity_threshold_min,
    humidity_threshold_max,
    alarm_enabled,
    enabled,
    online_status,
    last_seen_time,
    extra,
    creator_id,
    updater_id
) VALUES

-- 冷藏区A 温湿度传感器（继承阈值）
(
    'TH-A-001',
    '冷藏区A温湿度1号',
    'TEMP_HUM',
    'TH-1000',
    'ColdTech',
    'SN-TH-A001',
    'v1.2.3',
    5,
    '冷藏区A中央货架',
    'INHERIT',
    NULL,
    NULL,
    NULL,
    NULL,
    1,
    1,
    1,
    NOW(),
    JSON_OBJECT('commType','LoRa','batteryLevel',88),
    1,
    1
),

-- 冷藏区A BIN-A-01 精确传感器（设备覆盖阈值）
(
    'TH-A-002',
    'BIN-A-01精确温湿度',
    'TEMP_HUM',
    'TH-2000',
    'ColdTech',
    'SN-TH-A002',
    'v1.3.0',
    8,
    'A-01库位顶部',
    'OVERRIDE',
    3.00,
    7.50,
    35.00,
    65.00,
    1,
    1,
    1,
    DATE_SUB(NOW(), INTERVAL 3 MINUTE),
    JSON_OBJECT('commType','NB-IoT','batteryLevel',92),
    1,
    1
),

-- 冷冻区B 冷柜
(
    'FZ-B-001',
    '冷冻区B冷柜1号',
    'FREEZER',
    'FZ-500',
    'FreezePro',
    'SN-FZ-B001',
    'v2.0.1',
    6,
    '冷冻区B西侧',
    'OVERRIDE',
    -25.00,
    -18.00,
    NULL,
    NULL,
    1,
    1,
    1,
    DATE_SUB(NOW(), INTERVAL 5 MINUTE),
    JSON_OBJECT('power','220V','compressorStatus','running'),
    1,
    1
),

-- BIN-B-01 温湿度传感器（离线）
(
    'TH-B-003',
    'BIN-B-01温湿度',
    'TEMP_HUM',
    'TH-1000',
    'ColdTech',
    'SN-TH-B003',
    'v1.1.0',
    10,
    'B-01库位',
    'INHERIT',
    NULL,
    NULL,
    NULL,
    NULL,
    1,
    1,
    0,
    DATE_SUB(NOW(), INTERVAL 1 HOUR),
    JSON_OBJECT('batteryLevel',20,'warning','low_signal'),
    1,
    1
),

-- 分拣区C 门磁
(
    'DR-C-001',
    '分拣区C门磁',
    'DOOR',
    'DR-100',
    'SecureDoor',
    'SN-DR-C001',
    'v1.0.5',
    7,
    '分拣区入口',
    'INHERIT',
    NULL,
    NULL,
    NULL,
    NULL,
    1,
    1,
    1,
    DATE_SUB(NOW(), INTERVAL 10 MINUTE),
    JSON_OBJECT('doorStatus','closed','signalStrength',76),
    1,
    1
),

-- 冷藏区A 备用设备（禁用）
(
    'TH-A-004',
    '冷藏区A备用设备',
    'TEMP_HUM',
    'TH-1000',
    'ColdTech',
    'SN-TH-A004',
    'v1.0.0',
    5,
    '备用未启用',
    'INHERIT',
    NULL,
    NULL,
    NULL,
    NULL,
    0,
    0,
    0,
    NULL,
    JSON_OBJECT('remark','备用设备'),
    1,
    1
),

-- 车载设备（如果以后扩展到运输区域）
(
    'VH-001',
    '冷链运输车1号',
    'VEHICLE',
    'VH-Tracker',
    'TransIoT',
    'SN-VH-0001',
    'v3.0.0',
    5,
    '临时绑定冷藏区A',
    'INHERIT',
    NULL,
    NULL,
    NULL,
    NULL,
    1,
    1,
    1,
    DATE_SUB(NOW(), INTERVAL 15 MINUTE),
    JSON_OBJECT('imei','864532010001234','gpsEnabled',true),
    1,
    1
);
