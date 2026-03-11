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
    open_id VARCHAR(100) COMMENT '微信用户openId',
    union_id VARCHAR(100) COMMENT '微信用户unionId',
    wx_nickname VARCHAR(100) COMMENT '微信昵称',
    wx_avatar VARCHAR(255) COMMENT '微信头像URL',
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
    completed_time TIMESTAMP NULL,
    location_detail VARCHAR(255) NULL COMMENT '发生位置详细说明',
    due_time TIMESTAMP NULL COMMENT '预计完成时间',
    verified_time TIMESTAMP NULL COMMENT '审核/验收时间',
    verification_result TEXT COMMENT '审核/验收结果描述',
    order_type VARCHAR(50) DEFAULT 'MAINTENANCE' COMMENT '工单类型',
    warehouse_id BIGINT NULL COMMENT '库区ID',
    device_id BIGINT NULL COMMENT '设备ID',
    ref_alert_id BIGINT NULL COMMENT '关联的告警ID'
);

-- 添加微信会话记录表
CREATE TABLE IF NOT EXISTS wx_login_sessions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    open_id VARCHAR(100) NOT NULL COMMENT '微信用户openId',
    session_key VARCHAR(255) NOT NULL COMMENT '微信会话密钥',
    expires_at TIMESTAMP NOT NULL COMMENT '会话过期时间',
    created_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_openid (open_id),
    INDEX idx_expires (expires_at)
);

-- 添加微信用户授权记录表
CREATE TABLE IF NOT EXISTS wx_user_authorizations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    open_id VARCHAR(100) NOT NULL UNIQUE COMMENT '微信用户openId',
    union_id VARCHAR(100) COMMENT '微信用户unionId',
    nickname VARCHAR(100) COMMENT '微信昵称',
    avatar_url VARCHAR(255) COMMENT '微信头像URL',
    gender TINYINT DEFAULT 0 COMMENT '性别：0-未知，1-男，2-女',
    country VARCHAR(50) COMMENT '国家',
    province VARCHAR(50) COMMENT '省份',
    city VARCHAR(50) COMMENT '城市',
    language VARCHAR(20) DEFAULT 'zh_CN' COMMENT '语言',
    phone_info_encrypted TEXT COMMENT '加密的手机号信息',
    phone_info_decrypted JSON COMMENT '解密后的手机号信息',
    last_login_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后登录时间',
    created_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_openid (open_id)
);

-- 添加微信小程序登录日志表
CREATE TABLE IF NOT EXISTS wx_login_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    open_id VARCHAR(100) COMMENT '微信用户openId',
    ip_address VARCHAR(45) COMMENT '登录IP地址',
    user_agent TEXT COMMENT '用户代理信息',
    login_type VARCHAR(20) DEFAULT 'MINIPROGRAM' COMMENT '登录类型：MINIPROGRAM-小程序，JSAPI-网页',
    login_result TINYINT DEFAULT 1 COMMENT '登录结果：1-成功，0-失败',
    error_message VARCHAR(500) COMMENT '错误信息',
    created_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_openid_time (open_id, created_time),
    INDEX idx_ip_time (ip_address, created_time)
);

-- 添加用户与微信授权关联关系表
CREATE TABLE IF NOT EXISTS user_wx_bindings (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    open_id VARCHAR(100) NOT NULL UNIQUE COMMENT '微信用户openId',
    union_id VARCHAR(100) COMMENT '微信用户unionId',
    binding_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '绑定时间',
    unbinding_time TIMESTAMP NULL COMMENT '解绑时间',
    status TINYINT DEFAULT 1 COMMENT '状态：1-已绑定，0-已解绑',
    created_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_userid (user_id),
    INDEX idx_openid (open_id)
);

-- 添加微信消息推送记录表（用于推送工单、告警等通知）
CREATE TABLE IF NOT EXISTS wx_message_templates (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    template_id VARCHAR(100) NOT NULL COMMENT '微信模板消息ID',
    title VARCHAR(200) COMMENT '模板标题',
    content TEXT COMMENT '模板内容',
    example TEXT COMMENT '示例',
    category VARCHAR(50) COMMENT '分类',
    status TINYINT DEFAULT 1 COMMENT '状态：1-启用，0-禁用',
    created_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 添加微信消息推送记录表
CREATE TABLE IF NOT EXISTS wx_push_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_open_id VARCHAR(100) NOT NULL COMMENT '用户微信openId',
    template_id VARCHAR(100) NOT NULL COMMENT '模板ID',
    data JSON COMMENT '推送数据',
    form_id VARCHAR(100) COMMENT '表单ID（用于消息推送）',
    page VARCHAR(200) COMMENT '跳转页面',
    push_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '推送时间',
    result_code INT COMMENT '推送结果码',
    result_msg VARCHAR(255) COMMENT '推送结果信息',
    INDEX idx_openid_time (user_open_id, push_time)
);

-- 添加外键约束
ALTER TABLE sensor_data ADD CONSTRAINT fk_sensor_device FOREIGN KEY (device_id) REFERENCES devices(id);
ALTER TABLE alerts ADD CONSTRAINT fk_alert_device FOREIGN KEY (device_id) REFERENCES devices(id);
ALTER TABLE work_orders
ADD COLUMN IF NOT EXISTS verified_time TIMESTAMP NULL COMMENT '审核/验收时间',
ADD COLUMN IF NOT EXISTS verification_result TEXT COMMENT '审核/验收结果描述',
ADD COLUMN IF NOT EXISTS location_detail VARCHAR(255) NULL COMMENT '发生位置详细说明',
ADD COLUMN IF NOT EXISTS due_time TIMESTAMP NULL COMMENT '预计完成时间',
ADD COLUMN IF NOT EXISTS order_type VARCHAR(50) DEFAULT 'MAINTENANCE' COMMENT '工单类型',
ADD COLUMN IF NOT EXISTS warehouse_id BIGINT NULL COMMENT '库区ID',
ADD COLUMN IF NOT EXISTS device_id BIGINT NULL COMMENT '设备ID',
ADD COLUMN IF NOT EXISTS ref_alert_id BIGINT NULL COMMENT '关联的告警ID';

