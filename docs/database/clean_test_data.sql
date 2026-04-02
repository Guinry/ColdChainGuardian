-- ============================================
-- ColdChain Guardian - 干净的测试数据
-- 确保: UTF-8 编码，无BOM，中文正常显示
-- ============================================

USE coldchain_guardian;

-- ============================================
-- 1. 用户数据
-- ============================================
-- 密码: 123456 -> BCrypt哈希: $2a$10$SEtSONkJ5WcF94FeQBYqHe0C2d6GzL2.tZvQ.z.Za5q6O7hKjq
INSERT IGNORE INTO users (username, password, email, phone, real_name, role, status)
VALUES
('admin', '$2a$10$SEtSONkJ5WcF94FeQBYqHe0C2d6GzL2.tZvQ.z.Za5q6O7hKjq', 'admin@coldchain-guardian.com', '13800000001', '系统管理员', 'ADMIN', 1),
('manager', '$2a$10$SEtSONkJ5WcF94FeQBYqHe0C2d6GzL2.tZvQ.z.Za5q6O7hKjq', 'manager@coldchain.com', '13800000002', '张经理', 'MANAGER', 1),
('zhangsan', '$2a$10$SEtSONkJ5WcF94FeQBYqHe0C2d6GzL2.tZvQ.z.Za5q6O7hKjq', 'zhangsan@coldchain.com', '13800000003', '张三', 'STAFF', 1),
('lisi', '$2a$10$SEtSONkJ5WcF94FeQBYqHe0C2d6GzL2.tZvQ.z.Za5q6O7hKjq', 'lisi@coldchain.com', '13800000004', '李四', 'STAFF', 1);

-- ============================================
-- 2. 库区数据 - 树形结构
-- ============================================
INSERT IGNORE INTO warehouse_areas
(parent_id, area_code, area_name, area_level, address, location_desc,
 temperature_threshold_min, temperature_threshold_max,
 humidity_threshold_min, humidity_threshold_max,
 alarm_enabled, status, sort_no)
VALUES
-- 一级: 整个站点
(NULL, 'SITE-MAIN', '聊城冷链中心', 'SITE', '山东省聊城市经济开发区', '总部主库区',
 -25.00, 10.00, 30.00, 70.00, 1, 1, 1),
-- 二级: 仓库
(1, 'WH-A', 'A 栋冷冻库', 'WAREHOUSE', '聊城市经开区A栋', 'A栋冷库',
 -25.00, 0.00, 30.00, 60.00, 1, 1, 1),
(1, 'WH-B', 'B 栋冷藏库', 'WAREHOUSE', '聊城市经开区B栋', 'B栋冷藏库',
 2.00, 8.00, 40.00, 65.00, 1, 1, 2),
-- 三级: 楼层/区域
(2, 'FL-A1', 'A区一层', 'FLOOR', NULL, 'A栋一层A区',
 -25.00, 0.00, 30.00, 60.00, 1, 1, 1),
(2, 'FL-A2', 'A区二层', 'FLOOR', NULL, 'A栋二层B区',
 -25.00, 0.00, 30.00, 60.00, 1, 1, 2),
(3, 'FL-B1', 'B区一层', 'FLOOR', NULL, 'B栋一层',
 2.00, 8.00, 40.00, 65.00, 1, 1, 1),
-- 四级: 储位/货架
(5, 'BIN-A01', 'A01 货架', 'BIN', NULL, 'A栋一层A区A01货架',
 -25.00, 0.00, 30.00, 60.00, 1, 1, 1),
(5, 'BIN-A02', 'A02 货架', 'BIN', NULL, 'A栋一层A区A02货架',
 -25.00, 0.00, 30.00, 60.00, 1, 1, 2),
(6, 'BIN-B01', 'B01 货架', 'BIN', NULL, 'B栋一层B区B01货架',
 2.00, 8.00, 40.00, 65.00, 1, 1, 1),
(6, 'BIN-B02', 'B02 货架', 'BIN', NULL, 'B栋一层B区B02货架',
 2.00, 8.00, 40.00, 65.00, 1, 1, 2);

-- 更新树形路径
UPDATE warehouse_areas SET area_path = CONCAT('/', id, '/') WHERE parent_id IS NULL;
UPDATE warehouse_areas SET area_path = '/1/' WHERE id IN (2, 3);
UPDATE warehouse_areas SET area_path = '/1/2/' WHERE id IN (4, 5);
UPDATE warehouse_areas SET area_path = '/1/3/' WHERE id IN (6, 7);
UPDATE warehouse_areas SET area_path = '/1/2/5/' WHERE id IN (8, 9);
UPDATE warehouse_areas SET area_path = '/1/3/6/' WHERE id IN (10, 11);

-- ============================================
-- 3. 设备数据
-- ============================================
INSERT IGNORE INTO devices
(device_code, device_name, device_type, model, manufacturer, sn, area_id, location_desc,
 threshold_mode, temperature_threshold_min, temperature_threshold_max,
 humidity_threshold_min, humidity_threshold_max,
 alarm_enabled, enabled, online_status, last_seen_time,
 latest_temp, latest_humi, latest_data_time)
VALUES
('TH-A1-001', 'A1区温度湿度传感器 1号', 'TEMP_HUM', 'TH-200', '海尔生物医疗', 'SN20240001', 8, 'A1区西北角',
 'INHERIT', NULL, NULL, NULL, NULL, 1, 1, 1, NOW(),  -2.50, 45.50, NOW()),
('TH-A1-002', 'A1区温度湿度传感器 2号', 'TEMP_HUM', 'TH-200', '海尔生物医疗', 'SN20240002', 8, 'A1区东南角',
 'INHERIT', NULL, NULL, NULL, NULL, 1, 1, 1, NOW(),  -1.80, 42.30, NOW()),
('TH-A2-001', 'A2区温度湿度传感器 1号', 'TEMP_HUM', 'TH-200', '海尔生物医疗', 'SN20240003', 9, 'A2区东北角',
 'INHERIT', NULL, NULL, NULL, NULL, 1, 1, 1, NOW(),  -3.20, 48.10, NOW()),
('TH-B1-001', 'B1区温度湿度传感器 1号', 'TEMP_HUM', 'TH-200', '海尔生物医疗', 'SN20240004', 10, 'B1区中央',
 'INHERIT', NULL, NULL, NULL, NULL, 1, 1, 1, NOW(),  5.60, 52.80, NOW()),
('TH-B1-002', 'B1区温度湿度传感器 2号', 'TEMP_HUM', 'TH-200', '海尔生物医疗', 'SN20240005', 10, 'B1区门口',
 'INHERIT', NULL, NULL, NULL, NULL, 1, 1, 1, NOW(),  6.20, 55.30, NOW()),
('TH-B1-003', 'B1区温度湿度传感器 3号', 'TEMP_HUM', 'TH-200', '海尔生物医疗', 'SN20240006', 11, 'B1区西侧',
 'INHERIT', NULL, NULL, NULL, NULL, 1, 1, 0, NOW(),  NULL, NULL, NULL),
('DOOR-001', 'A1区入口门磁传感器', 'DOOR_SENSOR', 'DS-100', '海康威视', 'SN20240007', 8, 'A1区主入口',
 'INHERIT', NULL, NULL, NULL, NULL, 1, 1, 1, NOW(), NULL, NULL, NOW()),
('DOOR-002', 'B1区入口门磁传感器', 'DOOR_SENSOR', 'DS-100', '海康威视', 'SN20240008', 10, 'B1区主入口',
 'INHERIT', NULL, NULL, NULL, NULL, 1, 1, 1, NOW(), NULL, NULL, NOW()),
('FREEZER-001', '超低温保存冰柜', 'FREEZER', 'UF-500', '赛默飞世尔', 'SN20240009', 4, 'A栋实验区',
 'OVERRIDE', -80.00, -60.00, 20.00, 40.00, 1, 1, 1, NOW(), -70.50, 35.00, NOW()),
('VEHICLE-001', '冷链配送车 001', 'VEHICLE', 'VT-300', '中集集团', 'SN20240010', 1, '停车场A区',
 'INHERIT', NULL, NULL, NULL, NULL, 1, 1, 0, NOW(), NULL, NULL, NULL);

-- ============================================
-- 4. 历史温湿度数据 - 最近7天
-- ============================================
-- 为设备1生成数据
SET @device_id = 1;
SET @start_date = DATE_SUB(NOW(), INTERVAL 7 DAY);
WHILE @start_date < NOW() DO
  INSERT INTO sensor_data (device_id, temperature, humidity, data_time)
  VALUES (@device_id,
          ROUND(-2.5 + RAND() * 3, 2),
          ROUND(40 + RAND() * 10, 2),
          @start_date);
  SET @start_date = DATE_ADD(@start_date, INTERVAL 2 HOUR);
END WHILE;

-- 为设备2生成数据
SET @device_id = 2;
SET @start_date = DATE_SUB(NOW(), INTERVAL 7 DAY);
WHILE @start_date < NOW() DO
  INSERT INTO sensor_data (device_id, temperature, humidity, data_time)
  VALUES (@device_id,
          ROUND(-2.0 + RAND() * 2.5, 2),
          ROUND(38 + RAND() * 9, 2),
          @start_date);
  SET @start_date = DATE_ADD(@start_date, INTERVAL 2 HOUR);
END WHILE;

-- 为设备4生成数据
SET @device_id = 4;
SET @start_date = DATE_SUB(NOW(), INTERVAL 7 DAY);
WHILE @start_date < NOW() DO
  INSERT INTO sensor_data (device_id, temperature, humidity, data_time)
  VALUES (@device_id,
          ROUND(4.0 + RAND() * 4, 2),
          ROUND(45 + RAND() * 15, 2),
          @start_date);
  SET @start_date = DATE_ADD(@start_date, INTERVAL 2 HOUR);
END WHILE;

-- 为设备5生成数据
SET @device_id = 5;
SET @start_date = DATE_SUB(NOW(), INTERVAL 7 DAY);
WHILE @start_date < NOW() DO
  INSERT INTO sensor_data (device_id, temperature, humidity, data_time)
  VALUES (@device_id,
          ROUND(5.0 + RAND() * 3.5, 2),
          ROUND(50 + RAND() * 12, 2),
          @start_date);
  SET @start_date = DATE_ADD(@start_date, INTERVAL 2 HOUR);
END WHILE;

-- ============================================
-- 5. 告警数据
-- ============================================
INSERT INTO alerts
(device_id, warehouse_id, alert_type, alert_level, message, temperature, humidity, threshold_value, status)
VALUES
(1, 8, 'TEMP_HIGH', 'HIGH', 'A1区温度探头检测到温度过高', 2.80, 45.00, 0.00, 'RESOLVED'),
(1, 8, 'TEMP_HIGH', 'MEDIUM', 'A1区温度探头检测到温度偏高', 1.20, 42.00, 0.00, 'RESOLVED'),
(4, 10, 'TEMP_HIGH', 'CRITICAL', 'B1区温度探头检测到温度严重过高', 9.50, 52.00, 8.00, 'HANDLING'),
(5, 10, 'TEMP_HIGH', 'MEDIUM', 'B1区温度探头检测到温度偏高', 8.20, 55.00, 8.00, 'UNHANDLED'),
(5, 10, 'TEMP_HIGH', 'LOW', 'B1区温度探头检测到温度轻微偏高', 8.50, 56.00, 8.00, 'UNHANDLED'),
(2, 9, 'TEMP_LOW', 'HIGH', 'A2区温度探头检测到温度过低', -28.00, 40.00, -25.00, 'RESOLVED'),
(10, 1, 'TEMP_LOW', 'MEDIUM', '冷链车检测到温度过低', 2.00, 50.00, 2.00, 'UNHANDLED'),
(6, 11, 'TEMP_LOW', 'LOW', 'B1区温度探头检测到温度轻微偏低', -1.00, 42.00, 2.00, 'IGNORED'),
(1, 8, 'HUMI_HIGH', 'MEDIUM', 'A1区温度探头检测到湿度过高', -1.00, 72.00, 60.00, 'RESOLVED'),
(3, 10, 'HUMI_HIGH', 'LOW', 'B1区门磁传感器检测到湿度偏高', NULL, 71.00, 65.00, 'UNHANDLED'),
(5, 10, 'HUMI_HIGH', 'MEDIUM', 'B1区温度探头检测到湿度过高', 6.00, 78.00, 65.00, 'HANDLING'),
(7, 10, 'HUMI_HIGH', 'LOW', 'B1区门磁传感器检测到湿度轻微偏高', NULL, 71.00, 65.00, 'UNHANDLED'),
(2, 9, 'HUMI_LOW', 'MEDIUM', 'A2区温度探头检测到湿度过低', -18.00, 25.00, 30.00, 'RESOLVED'),
(5, 10, 'HUMI_LOW', 'LOW', 'B1区温度探头检测到湿度偏低', 5.50, 28.00, 40.00, 'UNHANDLED'),
(8, 11, 'HUMI_LOW', 'LOW', 'B1区门磁传感器检测到湿度偏低', NULL, 29.00, 40.00, 'IGNORED'),
(3, 10, 'DEVICE_OFFLINE', 'HIGH', 'B1区三号传感器已离线', NULL, NULL, NULL, 'UNHANDLED'),
(4, 10, 'DEVICE_OFFLINE', 'MEDIUM', 'B1区二号传感器已离线', NULL, NULL, NULL, 'HANDLING'),
(10, 1, 'DEVICE_OFFLINE', 'MEDIUM', '冷链一号车已离线', NULL, NULL, NULL, 'UNHANDLED'),
(9, 4, 'DEVICE_OFFLINE', 'LOW', '超低温冰柜通讯异常', NULL, NULL, NULL, 'RESOLVED'),
(8, 11, 'DEVICE_OFFLINE', 'LOW', 'B1区一号门磁短暂离线', NULL, NULL, NULL, 'RESOLVED');

-- ============================================
-- 6. 工单数据
-- ============================================
INSERT INTO work_orders
(order_no, title, description, priority, order_type, warehouse_id, device_id, status, creator_id, assigned_to)
VALUES
('WO-20260315-001', 'A1区温度异常处理', '检查制冷设备，调整温度设置', 'HIGH', 'ALERT_FIX', 1, 1, 'COMPLETED', 1, 3),
('WO-20260314-001', '例行设备巡检', '月度设备巡检维护', 'LOW', 'INSPECTION', 1, NULL, 'COMPLETED', 2, 3),
('WO-20260313-001', 'A栋冷库设备保养', '季度保养维护', 'MEDIUM', 'MAINTENANCE', 2, NULL, 'COMPLETED', 1, 2),
('WO-20260312-001', '门磁传感器更换', '更换故障门磁传感器', 'MEDIUM', 'MAINTENANCE', 10, 3, 'COMPLETED', 2, 4),
('WO-20260318-001', 'B1区温度严重异常紧急处理', '立即检查制冷系统，可能需要紧急维修', 'URGENT', 'ALERT_FIX', 10, 4, 'PROCESSING', 1, 2),
('WO-20260317-001', 'B1区湿度过高处理', '检查除湿设备，排查漏水可能', 'MEDIUM', 'ALERT_FIX', 10, 5, 'PROCESSING', 2, 3),
('WO-20260316-001', '冷链车设备检修', '冷链车制冷系统检修', 'HIGH', 'MAINTENANCE', 1, 10, 'PROCESSING', 1, 2),
('WO-20260319-001', 'B1区温度偏高处理', '检查温度传感器和制冷设备', 'MEDIUM', 'ALERT_FIX', 10, 5, 'PENDING', 1, NULL),
('WO-20260319-002', 'B1区温度轻微异常', '持续监控温度变化', 'LOW', 'ALERT_FIX', 10, 6, 'PENDING', 2, NULL),
('WO-20260319-003', '设备离线排查', '检查网络和设备电源', 'LOW', 'MAINTENANCE', 11, 8, 'PENDING', 1, NULL);

-- ============================================
-- 7. AI聊天会话示例数据
-- ============================================
INSERT IGNORE INTO ai_chat_sessions (user_id, title, is_deleted, create_time, update_time)
VALUES
(1, 'B1区温度异常分析', 0, DATE_SUB(NOW(), INTERVAL 1 DAY), NOW()),
(1, '本周告警趋势分析', 0, DATE_SUB(NOW(), INTERVAL 6 HOUR), NOW());

-- ============================================
-- 8. 统计查询 - 验证数据插入成功
-- ============================================
SELECT '用户' AS table_name, COUNT(*) AS count FROM users
UNION ALL SELECT '库区', COUNT(*) FROM warehouse_areas
UNION ALL SELECT '设备', COUNT(*) FROM devices
UNION ALL SELECT '传感器历史', COUNT(*) FROM sensor_data
UNION ALL SELECT '告警', COUNT(*) FROM alerts
UNION ALL SELECT '工单', COUNT(*) FROM work_orders
UNION ALL SELECT 'AI会话', COUNT(*) FROM ai_chat_sessions;
