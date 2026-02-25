-- 创建库区表
CREATE TABLE IF NOT EXISTS t_area (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    area_code VARCHAR(50) NOT NULL UNIQUE COMMENT '库区编码',
    area_name VARCHAR(100) NOT NULL COMMENT '库区名称',
    type VARCHAR(50) COMMENT '库区类型（冷冻/冷藏/恒温等）',
    location VARCHAR(200) COMMENT '位置信息',
    status VARCHAR(20) DEFAULT 'ENABLED' COMMENT '状态（ENABLED/DISABLED）',
    remark TEXT COMMENT '备注',
    deleted TINYINT DEFAULT 0 COMMENT '软删除标记（0-未删除，1-已删除）',
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_area_code (area_code),
    INDEX idx_status (status),
    INDEX idx_deleted (deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='库区表';