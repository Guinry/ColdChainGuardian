-- 创建冷链接守护数据库
CREATE DATABASE IF NOT EXISTS coldchain_guardian CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 使用数据库
USE coldchain_guardian;

-- 创建用户表
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(100),
    phone VARCHAR(20),
    real_name VARCHAR(50),
    role VARCHAR(20) DEFAULT 'USER',
    status TINYINT DEFAULT 1 COMMENT '1-启用，0-禁用',
    created_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    creator_id BIGINT,
    updater_id BIGINT
);

-- 插入默认管理员用户
INSERT IGNORE INTO users (username, password, email, real_name, role, status) VALUES
('admin', '$2a$10$SEtSONkJ5WcF94FeQBYqHe0C2d6GzL2.tZ.zVtQ.z.Za5q6O7hKjq', 'admin@coldchain-guardian.com', 'Administrator', 'ADMIN', 1);

-- 创建设备表
CREATE TABLE IF NOT EXISTS devices (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    device_code VARCHAR(50) NOT NULL UNIQUE,
    device_name VARCHAR(100) NOT NULL,
    device_type VARCHAR(50),
    location VARCHAR(200),
    temperature_threshold_min DECIMAL(5,2) DEFAULT -20.00,
    temperature_threshold_max DECIMAL(5,2) DEFAULT 8.00,
    humidity_threshold_min DECIMAL(5,2) DEFAULT 30.00,
    humidity_threshold_max DECIMAL(5,2) DEFAULT 70.00,
    alarm_enabled TINYINT DEFAULT 1,
    status TINYINT DEFAULT 1 COMMENT '1-在线，0-离线',
    created_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 创建传感器数据表
CREATE TABLE IF NOT EXISTS sensor_data (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    device_id BIGINT NOT NULL,
    temperature DECIMAL(5,2),
    humidity DECIMAL(5,2),
    location VARCHAR(200),
    created_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_device_created (device_id, created_time)
);

-- 创建告警记录表
CREATE TABLE IF NOT EXISTS alerts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    device_id BIGINT NOT NULL,
    alert_type VARCHAR(50) NOT NULL,
    alert_level VARCHAR(20) NOT NULL,
    alert_message TEXT,
    resolved_status TINYINT DEFAULT 0 COMMENT '0-未解决，1-已解决',
    resolved_by BIGINT,
    resolved_time TIMESTAMP NULL,
    created_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_device_resolved (device_id, resolved_status),
    INDEX idx_created (created_time)
);

-- 创建工单表
CREATE TABLE IF NOT EXISTS work_orders (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_no VARCHAR(50) NOT NULL UNIQUE,
    title VARCHAR(200) NOT NULL,
    description TEXT,
    priority VARCHAR(20) DEFAULT 'MEDIUM',
    status VARCHAR(20) DEFAULT 'OPEN',
    assigned_to BIGINT,
    created_by BIGINT NOT NULL,
    created_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    completed_time TIMESTAMP NULL
);

-- 添加外键约束
ALTER TABLE sensor_data ADD CONSTRAINT fk_sensor_device FOREIGN KEY (device_id) REFERENCES devices(id);
ALTER TABLE alerts ADD CONSTRAINT fk_alert_device FOREIGN KEY (device_id) REFERENCES devices(id);