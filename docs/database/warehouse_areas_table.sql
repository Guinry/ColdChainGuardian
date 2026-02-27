CREATE TABLE IF NOT EXISTS warehouse_areas (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    parent_id BIGINT NULL COMMENT '上级库区ID，NULL表示顶级',
    area_code VARCHAR(50) NOT NULL UNIQUE COMMENT '库区编码',
    area_name VARCHAR(100) NOT NULL COMMENT '库区名称',
    area_level VARCHAR(20) NOT NULL DEFAULT 'AREA' COMMENT '层级：SITE/WAREHOUSE/FLOOR/AREA/BIN',
    address VARCHAR(200) NULL COMMENT '地址（顶级/仓库级可用）',
    location_desc VARCHAR(200) NULL COMMENT '位置描述（如A栋2层东区）',

    -- 库区默认阈值（设备可覆盖）
    temperature_threshold_min DECIMAL(5,2) DEFAULT -20.00,
    temperature_threshold_max DECIMAL(5,2) DEFAULT 8.00,
    humidity_threshold_min DECIMAL(5,2) DEFAULT 30.00,
    humidity_threshold_max DECIMAL(5,2) DEFAULT 70.00,
    alarm_enabled TINYINT DEFAULT 1,

    status TINYINT DEFAULT 1 COMMENT '1-启用，0-禁用',
    sort_no INT DEFAULT 0 COMMENT '排序',
    remark VARCHAR(500) NULL,

    created_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    creator_id BIGINT,
    updater_id BIGINT,

    INDEX idx_parent (parent_id),
    INDEX idx_level (area_level),
    INDEX idx_status (status),
    CONSTRAINT fk_area_parent FOREIGN KEY (parent_id) REFERENCES warehouse_areas(id)
);


INSERT INTO warehouse_areas (
    parent_id,
    area_code,
    area_name,
    area_level,
    address,
    location_desc,
    temperature_threshold_min,
    temperature_threshold_max,
    humidity_threshold_min,
    humidity_threshold_max,
    alarm_enabled,
    status,
    sort_no,
    remark,
    creator_id
) VALUES (
    NULL,
    'SITE-SH-001',
    '华东冷链园区',
    'SITE',
    '上海市浦东新区冷链路100号',
    '总部园区',
    -20.00,
    8.00,
    30.00,
    70.00,
    1,
    1,
    1,
    '华东区域总园区',
    1
);

INSERT INTO warehouse_areas (
    parent_id,
    area_code,
    area_name,
    area_level,
    address,
    location_desc,
    sort_no,
    creator_id
) VALUES (
    1,
    'WH-SH-01',
    '上海一号仓',
    'WAREHOUSE',
    '园区内A栋',
    '主仓库',
    1,
    1
);

INSERT INTO warehouse_areas (
    parent_id,
    area_code,
    area_name,
    area_level,
    location_desc,
    sort_no,
    creator_id
) VALUES (
    2,
    'F1-SH-01',
    '一层',
    'FLOOR',
    'A栋一层',
    1,
    1
);


INSERT INTO warehouse_areas (
    parent_id,
    area_code,
    area_name,
    area_level,
    location_desc,
    sort_no,
    creator_id
) VALUES (
     2,
     'F2-SH-01',
     '二层',
     'FLOOR',
     'A栋二层',
     2,
     1
);

INSERT INTO warehouse_areas (
    parent_id,
    area_code,
    area_name,
    area_level,
    temperature_threshold_min,
    temperature_threshold_max,
    sort_no,
    creator_id
) VALUES (
    3,
    'AREA-A',
    '冷藏区A',
    'AREA',
    2.00,
    8.00,
    1,
    1
);

INSERT INTO warehouse_areas (
    parent_id,
    area_code,
    area_name,
    area_level,
    temperature_threshold_min,
    temperature_threshold_max,
    sort_no,
    creator_id
) VALUES (
    3,
    'AREA-B',
    '冷冻区B',
    'AREA',
    -25.00,
    -18.00,
    2,
    1
);

INSERT INTO warehouse_areas (
    parent_id,
    area_code,
    area_name,
    area_level,
    sort_no,
    creator_id
) VALUES (
    4,
    'AREA-C',
    '分拣区C',
    'AREA',
    1,
    1
);

INSERT INTO warehouse_areas (
    parent_id,
    area_code,
    area_name,
    area_level,
    sort_no,
    creator_id
) VALUES
(5, 'BIN-A-01', 'A-01库位', 'BIN', 1, 1),
(5, 'BIN-A-02', 'A-02库位', 'BIN', 2, 1);

INSERT INTO warehouse_areas (
    parent_id,
    area_code,
    area_name,
    area_level,
    sort_no,
    creator_id
) VALUES
(6, 'BIN-B-01', 'B-01库位', 'BIN', 1, 1),
(6, 'BIN-B-02', 'B-02库位', 'BIN', 2, 1);
