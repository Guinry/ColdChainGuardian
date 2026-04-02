-- 测试数据插入脚本
-- 用于 coldchain_guardian 数据库

USE coldchain_guardian;

-- ==================== 1. 用户数据 ====================
-- 已有：admin (ADMIN), root (MANAGER), 19511687612 (STOCK_MANAGER)
-- 需要补充：确保至少 3 个不同角色的用户

INSERT INTO users (username, password, email, phone, real_name, role, status, open_id, wx_nickname, wx_avatar, created_time)
VALUES 
('manager01', '$2a$10$M59yH0GBnnKKqeZZpVhsS.bzfTdaePeQijfWM9Y7SxXHFB.lT9ySq', 'manager01@coldchain.com', '13800138001', '张经理', 'MANAGER', 1, NULL, NULL, NULL, NOW()),
('staff01', '$2a$10$M59yH0GBnnKKqeZZpVhsS.bzfTdaePeQijfWM9Y7SxXHFB.lT9ySq', 'staff01@coldchain.com', '13800138002', '李员工', 'STAFF', 1, NULL, NULL, NULL, NOW()),
('staff02', '$2a$10$M59yH0GBnnKKqeZZpVhsS.bzfTdaePeQijfWM9Y7SxXHFB.lT9ySq', 'staff02@coldchain.com', '13800138003', '王员工', 'STAFF', 1, NULL, NULL, NULL, NOW())
ON DUPLICATE KEY UPDATE username=username;

-- ==================== 2. 库区数据 ====================
-- 已有：SITE-HD, WH-A, AREA-COOL, AREA-FREEZE (4 个)
-- 需要：至少 5 个

INSERT INTO warehouse_areas (parent_id, area_code, area_name, area_level, address, location_desc, temperature_threshold_min, temperature_threshold_max, humidity_threshold_min, humidity_threshold_max, alarm_enabled, status, sort_no, created_time)
VALUES 
(1, 'WH-B', 'B 栋医药库', 'WAREHOUSE', '聊城市经济开发区 B 栋', 'B 栋整体', 2.00, 8.00, 40.00, 65.00, 1, 1, 2, NOW()),
(2, 'AREA-TEMP', '常温库区', 'AREA', NULL, 'A 栋 1 层西区', 10.00, 25.00, 35.00, 60.00, 1, 1, 3, NOW()),
(5, 'BIN-A01', 'A01 货架', 'BIN', NULL, 'A 栋 1 层 A01 货架', 2.00, 8.00, 40.00, 65.00, 1, 1, 1, NOW()),
(5, 'BIN-A02', 'A02 货架', 'BIN', NULL, 'A 栋 1 层 A02 货架', 2.00, 8.00, 40.00, 65.00, 1, 1, 2, NOW())
ON DUPLICATE KEY UPDATE area_code=area_code;

-- 更新 area_path 字段
UPDATE warehouse_areas SET area_path = CONCAT('/', id, '/') WHERE parent_id IS NULL;
UPDATE warehouse_areas SET area_path = '/1/2/' WHERE id = 2;
UPDATE warehouse_areas SET area_path = '/1/5/' WHERE id = 5;
UPDATE warehouse_areas SET area_path = '/1/2/3/' WHERE id = 3;
UPDATE warehouse_areas SET area_path = '/1/2/4/' WHERE id = 4;
UPDATE warehouse_areas SET area_path = '/1/2/6/' WHERE id = 6;
UPDATE warehouse_areas SET area_path = '/1/5/7/' WHERE id = 7;
UPDATE warehouse_areas SET area_path = '/1/5/8/' WHERE id = 8;

-- ==================== 3. 设备数据 ====================
-- 已有：3 个设备
-- 需要：至少 10 个

INSERT INTO devices (device_code, device_name, device_type, model, manufacturer, sn, area_id, location_desc, threshold_mode, temperature_threshold_min, temperature_threshold_max, humidity_threshold_min, humidity_threshold_max, alarm_enabled, enabled, online_status, last_seen_time, created_time)
VALUES 
('TH-C-002', '冷藏库温度探头 2 号', 'TEMP_HUM', 'TH-200', '海尔生物', 'SN20240002', 3, '冷藏库中部', 'INHERIT', NULL, NULL, NULL, NULL, 1, 1, 1, NOW(), NOW()),
('TH-C-003', '冷藏库温度探头 3 号', 'TEMP_HUM', 'TH-200', '海尔生物', 'SN20240003', 3, '冷藏库门口', 'INHERIT', NULL, NULL, NULL, NULL, 1, 1, 1, NOW(), NOW()),
('TH-F-002', '冷冻库温度探头 2 号', 'TEMP_HUM', 'TH-200', '海尔生物', 'SN20240004', 4, '冷冻库北侧', 'INHERIT', NULL, NULL, NULL, NULL, 1, 1, 1, NOW(), NOW()),
('TH-F-003', '冷冻库温度探头 3 号', 'TEMP_HUM', 'TH-200', '海尔生物', 'SN20240005', 4, '冷冻库南侧', 'INHERIT', NULL, NULL, NULL, NULL, 1, 1, 0, DATE_SUB(NOW(), INTERVAL 2 HOUR), NOW()),
('TH-B-001', 'B 栋库温度探头 1 号', 'TEMP_HUM', 'TH-200', '海尔生物', 'SN20240006', 5, 'B 栋库中央', 'INHERIT', NULL, NULL, NULL, NULL, 1, 1, 1, NOW(), NOW()),
('TH-B-002', 'B 栋库温度探头 2 号', 'TEMP_HUM', 'TH-200', '海尔生物', 'SN20240007', 5, 'B 栋库门口', 'INHERIT', NULL, NULL, NULL, NULL, 1, 1, 1, NOW(), NOW()),
('DOOR-002', '冷藏库门磁传感器', 'DOOR_SENSOR', 'DS-100', '海康威视', 'SN20240008', 3, '冷藏库主门', 'INHERIT', NULL, NULL, NULL, NULL, 1, 1, 1, NOW(), NOW()),
('DOOR-003', 'B 栋库门磁传感器', 'DOOR_SENSOR', 'DS-100', '海康威视', 'SN20240009', 5, 'B 栋库主门', 'INHERIT', NULL, NULL, NULL, NULL, 1, 1, 1, NOW(), NOW()),
('FREEZER-001', '超低温冰柜', 'FREEZER', 'UF-500', '赛默飞', 'SN20240010', 4, '冷冻库东侧', 'OVERRIDE', -80.00, -20.00, NULL, NULL, 1, 1, 1, NOW(), NOW()),
('VEHICLE-001', '冷藏车 1 号', 'VEHICLE', 'VT-300', '中集车辆', 'SN20240011', 3, '停车场 A 区', 'INHERIT', NULL, NULL, NULL, NULL, 1, 1, 0, DATE_SUB(NOW(), INTERVAL 5 HOUR), NOW())
ON DUPLICATE KEY UPDATE device_code=device_code;

-- ==================== 4. 告警数据 ====================
-- 已有：2 条告警
-- 需要：至少 20 条

INSERT INTO alerts (device_id, warehouse_id, alert_config_id, alert_type, alert_level, message, temperature, humidity, threshold_value, status, created_time, first_time, last_time, trigger_count)
VALUES 
-- 温度过高告警 (5 条)
(1, 3, NULL, 'TEMP_HIGH', 'HIGH', '冷藏库温度探头 1 号检测到温度过高', 12.50, 55.00, 8.00, 'RESOLVED', DATE_SUB(NOW(), INTERVAL 5 DAY), DATE_SUB(NOW(), INTERVAL 5 DAY), DATE_SUB(NOW(), INTERVAL 5 DAY), 1),
(1, 3, NULL, 'TEMP_HIGH', 'MEDIUM', '冷藏库温度探头 1 号检测到温度偏高', 9.80, 52.00, 8.00, 'RESOLVED', DATE_SUB(NOW(), INTERVAL 3 DAY), DATE_SUB(NOW(), INTERVAL 3 DAY), DATE_SUB(NOW(), INTERVAL 3 DAY), 1),
(2, 4, NULL, 'TEMP_HIGH', 'CRITICAL', '冷冻库温度探头 1 号检测到温度严重过高', -15.00, 45.00, -20.00, 'HANDLING', DATE_SUB(NOW(), INTERVAL 1 DAY), DATE_SUB(NOW(), INTERVAL 1 DAY), NOW(), 3),
(5, 5, NULL, 'TEMP_HIGH', 'MEDIUM', 'B 栋库温度探头 1 号检测到温度偏高', 9.20, 58.00, 8.00, 'UNHANDLED', DATE_SUB(NOW(), INTERVAL 2 HOUR), DATE_SUB(NOW(), INTERVAL 2 HOUR), DATE_SUB(NOW(), INTERVAL 2 HOUR), 1),
(6, 5, NULL, 'TEMP_HIGH', 'LOW', 'B 栋库温度探头 2 号检测到温度轻微偏高', 8.50, 56.00, 8.00, 'UNHANDLED', DATE_SUB(NOW(), INTERVAL 1 HOUR), DATE_SUB(NOW(), INTERVAL 1 HOUR), DATE_SUB(NOW(), INTERVAL 1 HOUR), 1),

-- 温度过低告警 (3 条)
(2, 4, NULL, 'TEMP_LOW', 'HIGH', '冷冻库温度探头 1 号检测到温度过低', -25.00, 40.00, -20.00, 'RESOLVED', DATE_SUB(NOW(), INTERVAL 4 DAY), DATE_SUB(NOW(), INTERVAL 4 DAY), DATE_SUB(NOW(), INTERVAL 4 DAY), 1),
(10, 3, NULL, 'TEMP_LOW', 'MEDIUM', '冷藏车 1 号检测到温度过低', -2.00, 60.00, 2.00, 'UNHANDLED', DATE_SUB(NOW(), INTERVAL 30 MINUTE), DATE_SUB(NOW(), INTERVAL 30 MINUTE), DATE_SUB(NOW(), INTERVAL 30 MINUTE), 1),
(4, 4, NULL, 'TEMP_LOW', 'LOW', '冷冻库温度探头 3 号检测到温度轻微偏低', -21.00, 42.00, -20.00, 'IGNORED', DATE_SUB(NOW(), INTERVAL 6 HOUR), DATE_SUB(NOW(), INTERVAL 6 HOUR), DATE_SUB(NOW(), INTERVAL 6 HOUR), 1),

-- 湿度过高告警 (4 条)
(1, 3, NULL, 'HUMI_HIGH', 'MEDIUM', '冷藏库温度探头 1 号检测到湿度过高', 5.00, 75.00, 70.00, 'RESOLVED', DATE_SUB(NOW(), INTERVAL 2 DAY), DATE_SUB(NOW(), INTERVAL 2 DAY), DATE_SUB(NOW(), INTERVAL 2 DAY), 1),
(3, 4, NULL, 'HUMI_HIGH', 'LOW', '冷冻库门磁传感器检测到湿度偏高', NULL, 72.00, 70.00, 'UNHANDLED', DATE_SUB(NOW(), INTERVAL 4 HOUR), DATE_SUB(NOW(), INTERVAL 4 HOUR), DATE_SUB(NOW(), INTERVAL 4 HOUR), 1),
(5, 5, NULL, 'HUMI_HIGH', 'MEDIUM', 'B 栋库温度探头 1 号检测到湿度过高', 6.00, 78.00, 70.00, 'HANDLING', DATE_SUB(NOW(), INTERVAL 3 HOUR), DATE_SUB(NOW(), INTERVAL 3 HOUR), DATE_SUB(NOW(), INTERVAL 3 HOUR), 1),
(7, 3, NULL, 'HUMI_HIGH', 'LOW', '冷藏库门磁传感器 2 号检测到湿度轻微偏高', NULL, 71.00, 70.00, 'UNHANDLED', DATE_SUB(NOW(), INTERVAL 1 HOUR), DATE_SUB(NOW(), INTERVAL 1 HOUR), DATE_SUB(NOW(), INTERVAL 1 HOUR), 1),

-- 湿度过低告警 (3 条)
(2, 4, NULL, 'HUMI_LOW', 'MEDIUM', '冷冻库温度探头 1 号检测到湿度过低', -18.00, 25.00, 30.00, 'RESOLVED', DATE_SUB(NOW(), INTERVAL 3 DAY), DATE_SUB(NOW(), INTERVAL 3 DAY), DATE_SUB(NOW(), INTERVAL 3 DAY), 1),
(6, 5, NULL, 'HUMI_LOW', 'LOW', 'B 栋库温度探头 2 号检测到湿度偏低', 5.50, 28.00, 30.00, 'UNHANDLED', DATE_SUB(NOW(), INTERVAL 5 HOUR), DATE_SUB(NOW(), INTERVAL 5 HOUR), DATE_SUB(NOW(), INTERVAL 5 HOUR), 1),
(8, 5, NULL, 'HUMI_LOW', 'LOW', 'B 栋库门磁传感器检测到湿度偏低', NULL, 29.00, 30.00, 'IGNORED', DATE_SUB(NOW(), INTERVAL 8 HOUR), DATE_SUB(NOW(), INTERVAL 8 HOUR), DATE_SUB(NOW(), INTERVAL 8 HOUR), 1),

-- 设备离线告警 (5 条)
(3, 4, NULL, 'DEVICE_OFFLINE', 'HIGH', '冷冻库门磁传感器已离线', NULL, NULL, NULL, 'UNHANDLED', DATE_SUB(NOW(), INTERVAL 2 HOUR), DATE_SUB(NOW(), INTERVAL 2 HOUR), NOW(), 5),
(4, 4, NULL, 'DEVICE_OFFLINE', 'MEDIUM', '冷冻库温度探头 3 号已离线', NULL, NULL, NULL, 'HANDLING', DATE_SUB(NOW(), INTERVAL 3 HOUR), DATE_SUB(NOW(), INTERVAL 3 HOUR), NOW(), 3),
(10, 3, NULL, 'DEVICE_OFFLINE', 'MEDIUM', '冷藏车 1 号已离线', NULL, NULL, NULL, 'UNHANDLED', DATE_SUB(NOW(), INTERVAL 5 HOUR), DATE_SUB(NOW(), INTERVAL 5 HOUR), NOW(), 2),
(9, 4, NULL, 'DEVICE_OFFLINE', 'LOW', '超低温冰柜通讯异常', NULL, NULL, NULL, 'RESOLVED', DATE_SUB(NOW(), INTERVAL 1 DAY), DATE_SUB(NOW(), INTERVAL 1 DAY), DATE_SUB(NOW(), INTERVAL 1 DAY), 1),
(8, 5, NULL, 'DEVICE_OFFLINE', 'LOW', 'B 栋库门磁传感器短暂离线', NULL, NULL, NULL, 'RESOLVED', DATE_SUB(NOW(), INTERVAL 6 HOUR), DATE_SUB(NOW(), INTERVAL 6 HOUR), DATE_SUB(NOW(), INTERVAL 6 HOUR), 1);

-- ==================== 5. 工单数据 ====================
-- 已有：2 个工单
-- 需要：至少 10 个

INSERT INTO work_orders (order_no, ref_alert_id, title, description, priority, order_type, warehouse_id, device_id, status, creator_id, assigned_to, due_time, created_time, location_detail)
VALUES 
-- 已完成工单 (4 个)
('WO-20260315-001', 1, '冷藏库温度异常处理', '检查制冷设备，调整温度设置', 'HIGH', 'ALERT_FIX', 3, 1, 'COMPLETED', 1, 5, DATE_SUB(NOW(), INTERVAL 3 DAY), DATE_SUB(NOW(), INTERVAL 5 DAY), '冷藏库 A 区'),
('WO-20260314-001', NULL, '例行设备巡检', '月度设备巡检维护', 'LOW', 'INSPECTION', 3, NULL, 'COMPLETED', 2, 5, DATE_SUB(NOW(), INTERVAL 2 DAY), DATE_SUB(NOW(), INTERVAL 4 DAY), '冷藏库全区域'),
('WO-20260313-001', NULL, '冷冻库设备保养', '季度保养维护', 'MEDIUM', 'MAINTENANCE', 4, 2, 'COMPLETED', 1, 3, DATE_SUB(NOW(), INTERVAL 1 DAY), DATE_SUB(NOW(), INTERVAL 3 DAY), '冷冻库北侧'),
('WO-20260312-001', NULL, '门磁传感器更换', '更换故障门磁传感器', 'MEDIUM', 'MAINTENANCE', 4, 3, 'COMPLETED', 2, 5, NOW(), DATE_SUB(NOW(), INTERVAL 2 DAY), '冷冻库主门'),

-- 处理中工单 (3 个)
('WO-20260318-001', 3, '冷冻库温度严重异常紧急处理', '立即检查制冷系统，可能需要紧急维修', 'URGENT', 'ALERT_FIX', 4, 2, 'PROCESSING', 1, 3, DATE_ADD(NOW(), INTERVAL 4 HOUR), DATE_SUB(NOW(), INTERVAL 1 DAY), '冷冻库中央区域'),
('WO-20260317-001', 11, 'B 栋库湿度过高处理', '检查除湿设备，排查漏水可能', 'MEDIUM', 'ALERT_FIX', 5, 5, 'PROCESSING', 2, 5, DATE_ADD(NOW(), INTERVAL 1 DAY), DATE_SUB(NOW(), INTERVAL 3 HOUR), 'B 栋库中央'),
('WO-20260316-001', NULL, '冷藏车设备检修', '冷藏车制冷系统检修', 'HIGH', 'MAINTENANCE', 3, 10, 'PROCESSING', 1, 3, DATE_ADD(NOW(), INTERVAL 2 DAY), DATE_SUB(NOW(), INTERVAL 1 DAY), '停车场 A 区'),

-- 待处理工单 (3 个)
('WO-20260319-001', 4, 'B 栋库温度偏高处理', '检查温度传感器和制冷设备', 'MEDIUM', 'ALERT_FIX', 5, 5, 'PENDING', 1, NULL, DATE_ADD(NOW(), INTERVAL 1 DAY), NOW(), 'B 栋库中央'),
('WO-20260319-002', 5, 'B 栋库温度轻微异常', '持续监控温度变化', 'LOW', 'ALERT_FIX', 5, 6, 'PENDING', 2, NULL, DATE_ADD(NOW(), INTERVAL 2 DAY), NOW(), 'B 栋库门口'),
('WO-20260319-003', 14, '设备离线排查', '检查网络和设备电源', 'LOW', 'MAINTENANCE', 5, 8, 'PENDING', 1, NULL, DATE_ADD(NOW(), INTERVAL 3 DAY), NOW(), 'B 栋库主门');

-- ==================== 6. 工单日志数据 ====================
-- 为工单添加操作日志

INSERT INTO work_order_logs (work_order_id, operator_id, action, remark, created_time)
VALUES 
(1, 1, 'CREATE', '创建工单：冷藏库温度异常处理', DATE_SUB(NOW(), INTERVAL 5 DAY)),
(1, 1, 'ASSIGN', '指派给李员工处理', DATE_SUB(NOW(), INTERVAL 5 DAY)),
(1, 5, 'START', '开始处理工单', DATE_SUB(NOW(), INTERVAL 4 DAY)),
(1, 5, 'RESOLVE', '已完成温度调整，系统恢复正常', DATE_SUB(NOW(), INTERVAL 3 DAY)),

(2, 2, 'CREATE', '创建工单：例行设备巡检', DATE_SUB(NOW(), INTERVAL 4 DAY)),
(2, 2, 'ASSIGN', '指派给李员工执行巡检', DATE_SUB(NOW(), INTERVAL 4 DAY)),
(2, 5, 'START', '开始巡检工作', DATE_SUB(NOW(), INTERVAL 3 DAY)),
(2, 5, 'RESOLVE', '巡检完成，设备运行正常', DATE_SUB(NOW(), INTERVAL 2 DAY)),

(5, 1, 'CREATE', '创建紧急工单：冷冻库温度严重异常', DATE_SUB(NOW(), INTERVAL 1 DAY)),
(5, 1, 'ASSIGN', '紧急指派给王员工处理', DATE_SUB(NOW(), INTERVAL 1 DAY)),
(5, 3, 'START', '已开始检查制冷系统', DATE_SUB(NOW(), INTERVAL 20 HOUR)),

(6, 2, 'CREATE', '创建工单：B 栋库湿度过高处理', DATE_SUB(NOW(), INTERVAL 3 HOUR)),
(6, 2, 'ASSIGN', '指派给李员工处理', DATE_SUB(NOW(), INTERVAL 3 HOUR)),
(6, 5, 'START', '开始排查湿度问题', DATE_SUB(NOW(), INTERVAL 2 HOUR));

-- ==================== 验证数据 ====================
SELECT '用户数据' as category, COUNT(*) as count FROM users
UNION ALL SELECT '库区数据', COUNT(*) FROM warehouse_areas
UNION ALL SELECT '设备数据', COUNT(*) FROM devices
UNION ALL SELECT '告警数据', COUNT(*) FROM alerts
UNION ALL SELECT '工单数据', COUNT(*) FROM work_orders
UNION ALL SELECT '工单日志', COUNT(*) FROM work_order_logs;
