-- 测试数据插入脚本 - 简化版
USE coldchain_guardian;

-- 1. 用户数据 (使用简单密码哈希)
INSERT IGNORE INTO users (username, password, email, phone, real_name, role, status)
VALUES 
('manager01', 'pwd123456', 'manager01@coldchain.com', '13800138001', '张经理', 'MANAGER', 1),
('staff01', 'pwd123456', 'staff01@coldchain.com', '13800138002', '李员工', 'STAFF', 1),
('staff02', 'pwd123456', 'staff02@coldchain.com', '13800138003', '王员工', 'STAFF', 1);

-- 2. 库区数据
INSERT IGNORE INTO warehouse_areas (parent_id, area_code, area_name, area_level, address, location_desc, temperature_threshold_min, temperature_threshold_max, humidity_threshold_min, humidity_threshold_max, alarm_enabled, status, sort_no)
VALUES 
(1, 'WH-B', 'B 栋医药库', 'WAREHOUSE', '聊城市经济开发区 B 栋', 'B 栋整体', 2.00, 8.00, 40.00, 65.00, 1, 1, 2),
(2, 'AREA-TEMP', '常温库区', 'AREA', NULL, 'A 栋 1 层西区', 10.00, 25.00, 35.00, 60.00, 1, 1, 3),
(5, 'BIN-A01', 'A01 货架', 'BIN', NULL, 'A 栋 1 层 A01 货架', 2.00, 8.00, 40.00, 65.00, 1, 1, 1),
(5, 'BIN-A02', 'A02 货架', 'BIN', NULL, 'A 栋 1 层 A02 货架', 2.00, 8.00, 40.00, 65.00, 1, 1, 2);

-- 更新 area_path
UPDATE warehouse_areas SET area_path = CONCAT('/', id, '/') WHERE parent_id IS NULL;
UPDATE warehouse_areas SET area_path = '/1/2/' WHERE id = 2;
UPDATE warehouse_areas SET area_path = '/1/5/' WHERE id = 5;
UPDATE warehouse_areas SET area_path = '/1/2/3/' WHERE id = 3;
UPDATE warehouse_areas SET area_path = '/1/2/4/' WHERE id = 4;
UPDATE warehouse_areas SET area_path = '/1/2/6/' WHERE id = 6;
UPDATE warehouse_areas SET area_path = '/1/5/7/' WHERE id = 7;
UPDATE warehouse_areas SET area_path = '/1/5/8/' WHERE id = 8;

-- 3. 设备数据
INSERT IGNORE INTO devices (device_code, device_name, device_type, model, manufacturer, sn, area_id, location_desc, threshold_mode, alarm_enabled, enabled, online_status, last_seen_time)
VALUES 
('TH-C-002', '冷藏库温度探头 2 号', 'TEMP_HUM', 'TH-200', '海尔生物', 'SN20240002', 3, '冷藏库中部', 'INHERIT', 1, 1, 1, NOW()),
('TH-C-003', '冷藏库温度探头 3 号', 'TEMP_HUM', 'TH-200', '海尔生物', 'SN20240003', 3, '冷藏库门口', 'INHERIT', 1, 1, 1, NOW()),
('TH-F-002', '冷冻库温度探头 2 号', 'TEMP_HUM', 'TH-200', '海尔生物', 'SN20240004', 4, '冷冻库北侧', 'INHERIT', 1, 1, 1, NOW()),
('TH-F-003', '冷冻库温度探头 3 号', 'TEMP_HUM', 'TH-200', '海尔生物', 'SN20240005', 4, '冷冻库南侧', 'INHERIT', 1, 1, 0, DATE_SUB(NOW(), INTERVAL 2 HOUR)),
('TH-B-001', 'B 栋库温度探头 1 号', 'TEMP_HUM', 'TH-200', '海尔生物', 'SN20240006', 5, 'B 栋库中央', 'INHERIT', 1, 1, 1, NOW()),
('TH-B-002', 'B 栋库温度探头 2 号', 'TEMP_HUM', 'TH-200', '海尔生物', 'SN20240007', 5, 'B 栋库门口', 'INHERIT', 1, 1, 1, NOW()),
('DOOR-002', '冷藏库门磁传感器', 'DOOR_SENSOR', 'DS-100', '海康威视', 'SN20240008', 3, '冷藏库主门', 'INHERIT', 1, 1, 1, NOW()),
('DOOR-003', 'B 栋库门磁传感器', 'DOOR_SENSOR', 'DS-100', '海康威视', 'SN20240009', 5, 'B 栋库主门', 'INHERIT', 1, 1, 1, NOW()),
('FREEZER-001', '超低温冰柜', 'FREEZER', 'UF-500', '赛默飞', 'SN20240010', 4, '冷冻库东侧', 'OVERRIDE', 1, 1, 1, NOW()),
('VEHICLE-001', '冷藏车 1 号', 'VEHICLE', 'VT-300', '中集车辆', 'SN20240011', 3, '停车场 A 区', 'INHERIT', 1, 1, 0, DATE_SUB(NOW(), INTERVAL 5 HOUR));

-- 4. 告警数据
INSERT INTO alerts (device_id, warehouse_id, alert_type, alert_level, message, temperature, humidity, threshold_value, status, created_time)
VALUES 
(1, 3, 'TEMP_HIGH', 'HIGH', '冷藏库温度探头 1 号检测到温度过高', 12.50, 55.00, 8.00, 'RESOLVED', DATE_SUB(NOW(), INTERVAL 5 DAY)),
(1, 3, 'TEMP_HIGH', 'MEDIUM', '冷藏库温度探头 1 号检测到温度偏高', 9.80, 52.00, 8.00, 'RESOLVED', DATE_SUB(NOW(), INTERVAL 3 DAY)),
(2, 4, 'TEMP_HIGH', 'CRITICAL', '冷冻库温度探头 1 号检测到温度严重过高', -15.00, 45.00, -20.00, 'HANDLING', DATE_SUB(NOW(), INTERVAL 1 DAY)),
(5, 5, 'TEMP_HIGH', 'MEDIUM', 'B 栋库温度探头 1 号检测到温度偏高', 9.20, 58.00, 8.00, 'UNHANDLED', DATE_SUB(NOW(), INTERVAL 2 HOUR)),
(6, 5, 'TEMP_HIGH', 'LOW', 'B 栋库温度探头 2 号检测到温度轻微偏高', 8.50, 56.00, 8.00, 'UNHANDLED', DATE_SUB(NOW(), INTERVAL 1 HOUR)),
(2, 4, 'TEMP_LOW', 'HIGH', '冷冻库温度探头 1 号检测到温度过低', -25.00, 40.00, -20.00, 'RESOLVED', DATE_SUB(NOW(), INTERVAL 4 DAY)),
(10, 3, 'TEMP_LOW', 'MEDIUM', '冷藏车 1 号检测到温度过低', -2.00, 60.00, 2.00, 'UNHANDLED', DATE_SUB(NOW(), INTERVAL 30 MINUTE)),
(4, 4, 'TEMP_LOW', 'LOW', '冷冻库温度探头 3 号检测到温度轻微偏低', -21.00, 42.00, -20.00, 'IGNORED', DATE_SUB(NOW(), INTERVAL 6 HOUR)),
(1, 3, 'HUMI_HIGH', 'MEDIUM', '冷藏库温度探头 1 号检测到湿度过高', 5.00, 75.00, 70.00, 'RESOLVED', DATE_SUB(NOW(), INTERVAL 2 DAY)),
(3, 4, 'HUMI_HIGH', 'LOW', '冷冻库门磁传感器检测到湿度偏高', NULL, 72.00, 70.00, 'UNHANDLED', DATE_SUB(NOW(), INTERVAL 4 HOUR)),
(5, 5, 'HUMI_HIGH', 'MEDIUM', 'B 栋库温度探头 1 号检测到湿度过高', 6.00, 78.00, 70.00, 'HANDLING', DATE_SUB(NOW(), INTERVAL 3 HOUR)),
(7, 3, 'HUMI_HIGH', 'LOW', '冷藏库门磁传感器 2 号检测到湿度轻微偏高', NULL, 71.00, 70.00, 'UNHANDLED', DATE_SUB(NOW(), INTERVAL 1 HOUR)),
(2, 4, 'HUMI_LOW', 'MEDIUM', '冷冻库温度探头 1 号检测到湿度过低', -18.00, 25.00, 30.00, 'RESOLVED', DATE_SUB(NOW(), INTERVAL 3 DAY)),
(6, 5, 'HUMI_LOW', 'LOW', 'B 栋库温度探头 2 号检测到湿度偏低', 5.50, 28.00, 30.00, 'UNHANDLED', DATE_SUB(NOW(), INTERVAL 5 HOUR)),
(8, 5, 'HUMI_LOW', 'LOW', 'B 栋库门磁传感器检测到湿度偏低', NULL, 29.00, 30.00, 'IGNORED', DATE_SUB(NOW(), INTERVAL 8 HOUR)),
(3, 4, 'DEVICE_OFFLINE', 'HIGH', '冷冻库门磁传感器已离线', NULL, NULL, NULL, 'UNHANDLED', DATE_SUB(NOW(), INTERVAL 2 HOUR)),
(4, 4, 'DEVICE_OFFLINE', 'MEDIUM', '冷冻库温度探头 3 号已离线', NULL, NULL, NULL, 'HANDLING', DATE_SUB(NOW(), INTERVAL 3 HOUR)),
(10, 3, 'DEVICE_OFFLINE', 'MEDIUM', '冷藏车 1 号已离线', NULL, NULL, NULL, 'UNHANDLED', DATE_SUB(NOW(), INTERVAL 5 HOUR)),
(9, 4, 'DEVICE_OFFLINE', 'LOW', '超低温冰柜通讯异常', NULL, NULL, NULL, 'RESOLVED', DATE_SUB(NOW(), INTERVAL 1 DAY)),
(8, 5, 'DEVICE_OFFLINE', 'LOW', 'B 栋库门磁传感器短暂离线', NULL, NULL, NULL, 'RESOLVED', DATE_SUB(NOW(), INTERVAL 6 HOUR));

-- 5. 工单数据
INSERT INTO work_orders (order_no, title, description, priority, order_type, warehouse_id, device_id, status, creator_id, assigned_to, due_time, location_detail)
VALUES 
('WO-20260315-001', '冷藏库温度异常处理', '检查制冷设备，调整温度设置', 'HIGH', 'ALERT_FIX', 3, 1, 'COMPLETED', 1, 5, DATE_SUB(NOW(), INTERVAL 3 DAY), '冷藏库 A 区'),
('WO-20260314-001', '例行设备巡检', '月度设备巡检维护', 'LOW', 'INSPECTION', 3, NULL, 'COMPLETED', 2, 5, DATE_SUB(NOW(), INTERVAL 2 DAY), '冷藏库全区域'),
('WO-20260313-001', '冷冻库设备保养', '季度保养维护', 'MEDIUM', 'MAINTENANCE', 4, 2, 'COMPLETED', 1, 3, DATE_SUB(NOW(), INTERVAL 1 DAY), '冷冻库北侧'),
('WO-20260312-001', '门磁传感器更换', '更换故障门磁传感器', 'MEDIUM', 'MAINTENANCE', 4, 3, 'COMPLETED', 2, 5, NOW(), '冷冻库主门'),
('WO-20260318-001', '冷冻库温度严重异常紧急处理', '立即检查制冷系统，可能需要紧急维修', 'URGENT', 'ALERT_FIX', 4, 2, 'PROCESSING', 1, 3, DATE_ADD(NOW(), INTERVAL 4 HOUR), '冷冻库中央区域'),
('WO-20260317-001', 'B 栋库湿度过高处理', '检查除湿设备，排查漏水可能', 'MEDIUM', 'ALERT_FIX', 5, 5, 'PROCESSING', 2, 5, DATE_ADD(NOW(), INTERVAL 1 DAY), 'B 栋库中央'),
('WO-20260316-001', '冷藏车设备检修', '冷藏车制冷系统检修', 'HIGH', 'MAINTENANCE', 3, 10, 'PROCESSING', 1, 3, DATE_ADD(NOW(), INTERVAL 2 DAY), '停车场 A 区'),
('WO-20260319-001', 'B 栋库温度偏高处理', '检查温度传感器和制冷设备', 'MEDIUM', 'ALERT_FIX', 5, 5, 'PENDING', 1, NULL, DATE_ADD(NOW(), INTERVAL 1 DAY), 'B 栋库中央'),
('WO-20260319-002', 'B 栋库温度轻微异常', '持续监控温度变化', 'LOW', 'ALERT_FIX', 5, 6, 'PENDING', 2, NULL, DATE_ADD(NOW(), INTERVAL 2 DAY), 'B 栋库门口'),
('WO-20260319-003', '设备离线排查', '检查网络和设备电源', 'LOW', 'MAINTENANCE', 5, 8, 'PENDING', 1, NULL, DATE_ADD(NOW(), INTERVAL 3 DAY), 'B 栋库主门');

-- 6. 工单日志
INSERT INTO work_order_logs (work_order_id, operator_id, action, remark)
VALUES 
(1, 1, 'CREATE', '创建工单：冷藏库温度异常处理'),
(1, 1, 'ASSIGN', '指派给李员工处理'),
(1, 5, 'START', '开始处理工单'),
(1, 5, 'RESOLVE', '已完成温度调整，系统恢复正常'),
(2, 2, 'CREATE', '创建工单：例行设备巡检'),
(2, 2, 'ASSIGN', '指派给李员工执行巡检'),
(2, 5, 'START', '开始巡检工作'),
(2, 5, 'RESOLVE', '巡检完成，设备运行正常'),
(5, 1, 'CREATE', '创建紧急工单：冷冻库温度严重异常'),
(5, 1, 'ASSIGN', '紧急指派给王员工处理'),
(5, 3, 'START', '已开始检查制冷系统'),
(6, 2, 'CREATE', '创建工单：B 栋库湿度过高处理'),
(6, 2, 'ASSIGN', '指派给李员工处理'),
(6, 5, 'START', '开始排查湿度问题');

-- 验证数据
SELECT '用户数据' as category, COUNT(*) as count FROM users
UNION ALL SELECT '库区数据', COUNT(*) FROM warehouse_areas
UNION ALL SELECT '设备数据', COUNT(*) FROM devices
UNION ALL SELECT '告警数据', COUNT(*) FROM alerts
UNION ALL SELECT '工单数据', COUNT(*) FROM work_orders
UNION ALL SELECT '工单日志', COUNT(*) FROM work_order_logs;
