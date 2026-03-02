# 数据库设计文档

## 1. 概述

本系统涉及冷链物流安全监控的核心数据，包括温湿度传感器数据、告警信息、设备信息、用户管理等。数据库设计遵循第三范式，确保数据一致性和完整性。

## 2. 核心实体设计

### 2.1 用户表 (users)
```sql
CREATE TABLE IF NOT EXISTS users (
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

### 2.2 库区表 (warehouse_areas)
```sql
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
```

### 2.3 传感器设备表 (devices)
```sql
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
```

### 2.4 温湿度数据表 (sensor_data)
```sql
CREATE TABLE IF NOT EXISTS sensor_data (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    device_id BIGINT NOT NULL COMMENT '设备ID',
    temperature DECIMAL(5,2) COMMENT '温度(℃)',
    humidity DECIMAL(5,2) COMMENT '湿度(%)',
    data_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '数据时间戳',
    battery_level DECIMAL(5,2) COMMENT '电池电量(%)',
    signal_strength INT COMMENT '信号强度',
    raw_data TEXT COMMENT '原始数据',
    created_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    -- 可选增强：服务端接收时间（不影响现有逻辑）
    recv_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '服务端接收时间',

    INDEX idx_device_time (device_id, data_time),
    INDEX idx_time (data_time),

    CONSTRAINT fk_sensor_device FOREIGN KEY (device_id) REFERENCES devices(id)
) COMMENT='温湿度原始上报数据';
```

### 2.5 告警配置表 (alert_configs)
```sql
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
```

### 2.6 告警记录表 (alerts)
```sql
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

### 2.10 维修巡检工单表 (work_orders)
```sql
CREATE TABLE IF NOT EXISTS work_orders (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_no VARCHAR(50) NOT NULL UNIQUE COMMENT '工单编号(如 WO-20260303-001)',
    ref_alert_id BIGINT NULL COMMENT '触发该工单的源告警ID(手动建单可为空)',

    -- 工单基础信息
    title VARCHAR(200) NOT NULL COMMENT '工单标题',
    description TEXT COMMENT '工单详细描述',
    priority ENUM('LOW', 'MEDIUM', 'HIGH', 'URGENT') DEFAULT 'MEDIUM' COMMENT '优先级',
    order_type ENUM('ALERT_FIX', 'INSPECTION', 'MAINTENANCE') DEFAULT 'ALERT_FIX' COMMENT '工单类型',

    -- 关联位置(冗余字段提速)
    warehouse_id BIGINT NOT NULL COMMENT '发生库区ID',
    device_id BIGINT NULL COMMENT '关联设备ID(如有)',

    -- 状态与人员
    status ENUM('PENDING', 'PROCESSING', 'VERIFYING', 'COMPLETED', 'CLOSED') DEFAULT 'PENDING' COMMENT '状态: 待处理/处理中/待验收/已完成/已关闭',
    creator_id BIGINT NOT NULL COMMENT '创建人ID',
    assigned_to BIGINT NULL COMMENT '当前指派给(处理人ID)',

    -- 时间线
    due_time TIMESTAMP NULL COMMENT '要求完成时间',
    completed_time TIMESTAMP NULL COMMENT '实际完成时间',
    created_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    INDEX idx_status (status),
    INDEX idx_assigned (assigned_to),
    CONSTRAINT fk_wo_warehouse FOREIGN KEY (warehouse_id) REFERENCES warehouse_areas(id)
) COMMENT='设备运维工单表';
```

### 2.11 工单流转日志表 (work_order_logs)
```sql
CREATE TABLE IF NOT EXISTS work_order_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    work_order_id BIGINT NOT NULL COMMENT '工单ID',
    operator_id BIGINT NOT NULL COMMENT '操作人ID',

    action VARCHAR(50) NOT NULL COMMENT '动作(如 CREATE, ASSIGN, START, UPLOAD_PHOTO, RESOLVE)',
    remark TEXT COMMENT '操作备注(如"已紧固冷机阀门")',
    attachment_urls JSON NULL COMMENT '现场照片URL集合',

    created_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',

    INDEX idx_wo_id (work_order_id),
    CONSTRAINT fk_log_wo FOREIGN KEY (work_order_id) REFERENCES work_orders(id)
) COMMENT='工单流转日志表';
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