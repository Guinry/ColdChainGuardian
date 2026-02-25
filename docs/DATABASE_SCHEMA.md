# 数据库设计文档

## 1. 概述

本系统涉及冷链物流安全监控的核心数据，包括温湿度传感器数据、告警信息、设备信息、用户管理等。数据库设计遵循第三范式，确保数据一致性和完整性。

## 2. 核心实体设计

### 2.1 用户表 (users)
```sql
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL COMMENT '用户名',
    password VARCHAR(255) NOT NULL COMMENT '密码',
    real_name VARCHAR(50) NOT NULL COMMENT '真实姓名',
    phone VARCHAR(20) COMMENT '联系电话',
    email VARCHAR(100) COMMENT '邮箱',
    role ENUM('ADMIN', 'STAFF', 'MANAGER') DEFAULT 'STAFF' COMMENT '用户角色',
    status TINYINT DEFAULT 1 COMMENT '用户状态(0:禁用,1:启用)',
    created_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
```

### 2.2 库区表 (warehouses)
```sql
CREATE TABLE warehouses (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL COMMENT '库区名称',
    location VARCHAR(255) COMMENT '库区位置',
    description TEXT COMMENT '库区描述',
    capacity DECIMAL(10,2) COMMENT '库容(立方米)',
    status TINYINT DEFAULT 1 COMMENT '库区状态(0:停用,1:启用)',
    created_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
```

### 2.3 传感器设备表 (devices)
```sql
CREATE TABLE devices (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    device_code VARCHAR(50) UNIQUE NOT NULL COMMENT '设备编号',
    name VARCHAR(100) NOT NULL COMMENT '设备名称',
    warehouse_id BIGINT NOT NULL COMMENT '所属库区ID',
    device_type ENUM('TEMPERATURE', 'HUMIDITY', 'TEMP_HUMI') DEFAULT 'TEMP_HUMI' COMMENT '设备类型',
    location_desc VARCHAR(255) COMMENT '设备位置描述',
    status TINYINT DEFAULT 1 COMMENT '设备状态(0:故障,1:正常)',
    manufacturer VARCHAR(100) COMMENT '制造商',
    installation_date DATE COMMENT '安装日期',
    last_calibration_date DATE COMMENT '最后校准日期',
    calibration_cycle INT DEFAULT 30 COMMENT '校准周期(天)',
    created_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (warehouse_id) REFERENCES warehouses(id)
);
```

### 2.4 温湿度数据表 (sensor_data)
```sql
CREATE TABLE sensor_data (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    device_id BIGINT NOT NULL COMMENT '设备ID',
    temperature DECIMAL(5,2) COMMENT '温度(℃)',
    humidity DECIMAL(5,2) COMMENT '湿度(%)',
    data_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '数据时间戳',
    battery_level DECIMAL(5,2) COMMENT '电池电量(%)',
    signal_strength INT COMMENT '信号强度',
    raw_data TEXT COMMENT '原始数据',
    created_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_device_time (device_id, data_time),
    FOREIGN KEY (device_id) REFERENCES devices(id)
);
```

### 2.5 告警配置表 (alert_configs)
```sql
CREATE TABLE alert_configs (
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
    FOREIGN KEY (device_id) REFERENCES devices(id),
    FOREIGN KEY (warehouse_id) REFERENCES warehouses(id)
);
```

### 2.6 告警记录表 (alerts)
```sql
CREATE TABLE alerts (
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
    INDEX idx_status_time (status, created_time),
    INDEX idx_device_time (device_id, created_time),
    FOREIGN KEY (alert_config_id) REFERENCES alert_configs(id),
    FOREIGN KEY (device_id) REFERENCES devices(id),
    FOREIGN KEY (warehouse_id) REFERENCES warehouses(id),
    FOREIGN KEY (handler_user_id) REFERENCES users(id)
);
```

### 2.7 巡检计划表 (inspection_plans)
```sql
CREATE TABLE inspection_plans (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL COMMENT '巡检计划名称',
    warehouse_id BIGINT NOT NULL COMMENT '库区ID',
    frequency_type ENUM('HOURLY', 'DAILY', 'WEEKLY', 'MONTHLY') DEFAULT 'DAILY' COMMENT '频率类型',
    frequency_value INT DEFAULT 1 COMMENT '频率值',
    schedule_time TIME COMMENT '定时执行时间',
    assign_users JSON COMMENT '分配给的用户',
    description TEXT COMMENT '计划描述',
    status TINYINT DEFAULT 1 COMMENT '计划状态(0:暂停,1:启用)',
    created_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (warehouse_id) REFERENCES warehouses(id)
);
```

### 2.8 巡检记录表 (inspection_records)
```sql
CREATE TABLE inspection_records (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    plan_id BIGINT NOT NULL COMMENT '巡检计划ID',
    inspector_id BIGINT NOT NULL COMMENT '巡检员ID',
    warehouse_id BIGINT NOT NULL COMMENT '库区ID',
    inspection_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '巡检时间',
    temperature_reading DECIMAL(5,2) COMMENT '温度读数',
    humidity_reading DECIMAL(5,2) COMMENT '湿度读数',
    status ENUM('NORMAL', 'WARNING', 'ABNORMAL') DEFAULT 'NORMAL' COMMENT '巡检状态',
    photos JSON COMMENT '巡检照片',
    remarks TEXT COMMENT '巡检备注',
    created_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (plan_id) REFERENCES inspection_plans(id),
    FOREIGN KEY (inspector_id) REFERENCES users(id),
    FOREIGN KEY (warehouse_id) REFERENCES warehouses(id)
);
```

### 2.9 隐患上报表 (hazard_reports)
```sql
CREATE TABLE hazard_reports (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    reporter_id BIGINT NOT NULL COMMENT '上报人ID',
    warehouse_id BIGINT NOT NULL COMMENT '库区ID',
    device_id BIGINT COMMENT '关联设备ID',
    hazard_type VARCHAR(50) COMMENT '隐患类型',
    severity ENUM('LOW', 'MEDIUM', 'HIGH', 'CRITICAL') DEFAULT 'MEDIUM' COMMENT '严重程度',
    description TEXT NOT NULL COMMENT '隐患描述',
    photos JSON COMMENT '隐患照片',
    status ENUM('REPORTED', 'CONFIRMED', 'PROCESSING', 'RESOLVED') DEFAULT 'REPORTED' COMMENT '处理状态',
    assign_to_user_id BIGINT COMMENT '指派给用户ID',
    resolved_time TIMESTAMP NULL COMMENT '解决时间',
    remark TEXT COMMENT '处理备注',
    created_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (reporter_id) REFERENCES users(id),
    FOREIGN KEY (assign_to_user_id) REFERENCES users(id),
    FOREIGN KEY (warehouse_id) REFERENCES warehouses(id),
    FOREIGN KEY (device_id) REFERENCES devices(id)
);
```

## 3. 关键设计考虑

### 3.1 性能优化
- 在频繁查询的字段上建立索引
- 传感器数据表按时间分区存储
- 合理设置数据库连接池大小

### 3.2 数据安全
- 密码字段使用加密存储
- 敏感操作记录日志
- 数据备份策略

### 3.3 扩展性
- 使用枚举类型限制字段取值
- 预留扩展字段
- 采用合理的命名规范

## 4. 约定和规范

- 所有时间字段使用TIMESTAMP类型
- 使用小写加下划线命名表和字段
- 所有表都包含created_time和updated_time字段
- 外键约束使用CASCADE删除策略