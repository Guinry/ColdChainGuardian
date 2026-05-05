SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ColdChain Guardian enhanced demo data.
-- Safe to run repeatedly: business keys use ON DUPLICATE KEY UPDATE.

DELETE FROM work_orders
WHERE order_no IN (
  'WO-DEMO-20260504-001',
  'WO-DEMO-20260504-002',
  'WO-DEMO-20260504-003',
  'WO-DEMO-20260504-004'
);

DELETE a
FROM alerts a
JOIN devices d ON d.id = a.device_id
WHERE d.device_code IN ('TH-B1-004', 'TH-B1-005', 'FREEZER-002', 'VEHICLE-002', 'TH-A2-002')
  AND a.created_time >= '2026-05-04 00:00:00';

DELETE m
FROM ai_chat_messages m
JOIN ai_chat_sessions s ON s.id = m.session_id
WHERE s.user_id = 1
  AND s.title IN ('数据库运行态综合研判', 'B1区连续超温复盘', '??????????', 'B1???????');

DELETE FROM ai_chat_sessions
WHERE user_id = 1
  AND title IN ('数据库运行态综合研判', 'B1区连续超温复盘', '??????????', 'B1???????');

UPDATE devices SET
  device_name = CASE device_code
    WHEN 'TH-A1-001' THEN 'A1区温湿度传感器 1号'
    WHEN 'TH-A1-002' THEN 'A1区温湿度传感器 2号'
    WHEN 'TH-A2-001' THEN 'A2区温湿度传感器 1号'
    WHEN 'TH-B1-001' THEN 'B1区温湿度传感器 1号'
    WHEN 'TH-B1-002' THEN 'B1区温湿度传感器 2号'
    WHEN 'TH-B1-003' THEN 'B1区温湿度传感器 3号'
    WHEN 'DOOR-001' THEN 'A1区入口门磁传感器'
    WHEN 'DOOR-002' THEN 'B1区入口门磁传感器'
    WHEN 'FREEZER-001' THEN '超低温保存冰柜'
    WHEN 'VEHICLE-001' THEN '冷链配送车 001'
    ELSE device_name
  END,
  updated_time = NOW()
WHERE device_code IN ('TH-A1-001','TH-A1-002','TH-A2-001','TH-B1-001','TH-B1-002','TH-B1-003','DOOR-001','DOOR-002','FREEZER-001','VEHICLE-001');

INSERT INTO devices (
  device_code, device_name, device_type, model, manufacturer, sn, firmware_version,
  area_id, location_desc, threshold_mode,
  temperature_threshold_min, temperature_threshold_max, humidity_threshold_min, humidity_threshold_max,
  alarm_enabled, enabled, online_status, last_seen_time, extra, created_time, updated_time,
  latest_temp, latest_humi, latest_data_time, has_unresolved_alert
) VALUES
('TH-A1-003', 'A1区温湿度传感器 3号', 'TEMP_HUM', 'TH-220', '海尔生物医疗', 'SN-DEMO-A1003', '2.1.3', 16, 'A1区中部货架', 'INHERIT', NULL, NULL, NULL, NULL, 1, 1, 1, NOW() - INTERVAL 4 MINUTE, JSON_OBJECT('gateway','GW-A1','installHeight','1.8m'), NOW() - INTERVAL 25 DAY, NOW(), -2.10, 44.80, NOW() - INTERVAL 4 MINUTE, 0),
('TH-A2-002', 'A2区温湿度传感器 2号', 'TEMP_HUM', 'TH-220', '海尔生物医疗', 'SN-DEMO-A2002', '2.1.3', 17, 'A2区南侧回风口', 'INHERIT', NULL, NULL, NULL, NULL, 1, 1, 1, NOW() - INTERVAL 3 MINUTE, JSON_OBJECT('gateway','GW-A2','installHeight','1.7m'), NOW() - INTERVAL 24 DAY, NOW(), -4.60, 51.20, NOW() - INTERVAL 3 MINUTE, 0),
('TH-B1-004', 'B1区温湿度传感器 4号', 'TEMP_HUM', 'TH-220', '海尔生物医疗', 'SN-DEMO-B1004', '2.0.9', 18, 'B1区西侧蒸发器附近', 'INHERIT', NULL, NULL, NULL, NULL, 1, 1, 1, NOW() - INTERVAL 2 MINUTE, JSON_OBJECT('gateway','GW-B1','installHeight','1.6m'), NOW() - INTERVAL 23 DAY, NOW(), 9.60, 68.50, NOW() - INTERVAL 2 MINUTE, 1),
('TH-B1-005', 'B1区温湿度传感器 5号', 'TEMP_HUM', 'TH-220', '海尔生物医疗', 'SN-DEMO-B1005', '2.0.9', 18, 'B1区东侧门口', 'INHERIT', NULL, NULL, NULL, NULL, 1, 1, 0, NOW() - INTERVAL 3 HOUR, JSON_OBJECT('gateway','GW-B1','installHeight','1.6m'), NOW() - INTERVAL 22 DAY, NOW(), NULL, NULL, NULL, 1),
('FREEZER-002', '超低温保存冰柜 2号', 'FREEZER', 'UF-700', '赛默飞世尔', 'SN-DEMO-F002', '1.4.1', 12, 'A栋实验区北侧', 'OVERRIDE', -80.00, -60.00, 20.00, 40.00, 1, 1, 1, NOW() - INTERVAL 5 MINUTE, JSON_OBJECT('gateway','GW-A1','volume','700L'), NOW() - INTERVAL 21 DAY, NOW(), -58.30, 37.60, NOW() - INTERVAL 5 MINUTE, 1),
('VEHICLE-002', '冷链配送车 002', 'VEHICLE', 'VT-500', '中集集团', 'SN-DEMO-V002', '3.0.2', 9, '装车月台 2 号位', 'OVERRIDE', -5.00, 8.00, 30.00, 70.00, 1, 1, 1, NOW() - INTERVAL 6 MINUTE, JSON_OBJECT('plateNo','鲁P-Demo02','gateway','4G-V2'), NOW() - INTERVAL 18 DAY, NOW(), 4.20, 54.30, NOW() - INTERVAL 6 MINUTE, 0),
('DOOR-003', 'A2区入口门磁传感器', 'DOOR_SENSOR', 'DS-120', '海康威视', 'SN-DEMO-D003', '1.2.0', 17, 'A2区人员通道', 'INHERIT', NULL, NULL, NULL, NULL, 1, 1, 1, NOW() - INTERVAL 8 MINUTE, JSON_OBJECT('gateway','GW-A2','doorType','personnel'), NOW() - INTERVAL 20 DAY, NOW(), NULL, NULL, NOW() - INTERVAL 8 MINUTE, 0)
ON DUPLICATE KEY UPDATE
  device_name = VALUES(device_name),
  device_type = VALUES(device_type),
  model = VALUES(model),
  manufacturer = VALUES(manufacturer),
  sn = VALUES(sn),
  firmware_version = VALUES(firmware_version),
  area_id = VALUES(area_id),
  location_desc = VALUES(location_desc),
  threshold_mode = VALUES(threshold_mode),
  temperature_threshold_min = VALUES(temperature_threshold_min),
  temperature_threshold_max = VALUES(temperature_threshold_max),
  humidity_threshold_min = VALUES(humidity_threshold_min),
  humidity_threshold_max = VALUES(humidity_threshold_max),
  alarm_enabled = VALUES(alarm_enabled),
  enabled = VALUES(enabled),
  online_status = VALUES(online_status),
  last_seen_time = VALUES(last_seen_time),
  extra = VALUES(extra),
  latest_temp = VALUES(latest_temp),
  latest_humi = VALUES(latest_humi),
  latest_data_time = VALUES(latest_data_time),
  has_unresolved_alert = VALUES(has_unresolved_alert),
  updated_time = NOW();

DELETE sd
FROM sensor_data sd
JOIN devices d ON d.id = sd.device_id
WHERE d.device_code IN ('TH-A1-001','TH-A1-002','TH-A1-003','TH-A2-001','TH-A2-002','TH-B1-001','TH-B1-002','TH-B1-004','FREEZER-001','FREEZER-002','VEHICLE-002')
  AND sd.data_time >= NOW() - INTERVAL 14 DAY;

INSERT INTO sensor_data (device_id, temperature, humidity, data_time, battery_level, signal_strength, raw_data, created_time, update_time)
SELECT d.id,
       CASE d.device_code
         WHEN 'TH-A1-001' THEN -2.7 + (seq.n % 5) * 0.22
         WHEN 'TH-A1-002' THEN -2.1 + (seq.n % 4) * 0.18
         WHEN 'TH-A1-003' THEN -2.4 + (seq.n % 6) * 0.16
         WHEN 'TH-A2-001' THEN -4.4 + (seq.n % 5) * 0.20
         WHEN 'TH-A2-002' THEN -4.9 + (seq.n % 6) * 0.18
         WHEN 'TH-B1-001' THEN 5.2 + (seq.n % 7) * 0.28
         WHEN 'TH-B1-002' THEN 6.1 + (seq.n % 6) * 0.35
         WHEN 'TH-B1-004' THEN CASE WHEN seq.n >= 48 THEN 8.6 + (seq.n % 5) * 0.35 ELSE 6.4 + (seq.n % 5) * 0.22 END
         WHEN 'FREEZER-001' THEN -70.8 + (seq.n % 4) * 0.30
         WHEN 'FREEZER-002' THEN CASE WHEN seq.n >= 45 THEN -59.2 + (seq.n % 4) * 0.35 ELSE -66.5 + (seq.n % 5) * 0.22 END
         WHEN 'VEHICLE-002' THEN 3.4 + (seq.n % 6) * 0.25
       END AS temperature,
       CASE d.device_code
         WHEN 'TH-B1-004' THEN CASE WHEN seq.n >= 48 THEN 68 + (seq.n % 4) * 1.4 ELSE 55 + (seq.n % 5) * 0.8 END
         WHEN 'FREEZER-002' THEN 36 + (seq.n % 4) * 0.5
         WHEN 'VEHICLE-002' THEN 52 + (seq.n % 6) * 0.7
         ELSE 42 + (seq.n % 8) * 1.1
       END AS humidity,
       NOW() - INTERVAL (72 - seq.n) * 4 HOUR AS data_time,
       92 - (seq.n % 18) AS battery_level,
       -48 - (seq.n % 12) AS signal_strength,
       JSON_OBJECT('source','enhanced_demo_seed','seq',seq.n),
       NOW(),
       NOW()
FROM devices d
JOIN (
  SELECT ones.n + tens.n * 10 AS n
  FROM
    (SELECT 0 n UNION ALL SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4 UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9) ones
    CROSS JOIN
    (SELECT 0 n UNION ALL SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4 UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7) tens
) seq ON seq.n BETWEEN 0 AND 72
WHERE d.device_code IN ('TH-A1-001','TH-A1-002','TH-A1-003','TH-A2-001','TH-A2-002','TH-B1-001','TH-B1-002','TH-B1-004','FREEZER-001','FREEZER-002','VEHICLE-002');

INSERT INTO alerts (
  device_id, warehouse_id, alert_type, alert_level, message, temperature, humidity, threshold_value,
  status, handler_user_id, handle_time, handle_remark, created_time, updated_time, first_time, last_time, trigger_count
) VALUES
((SELECT id FROM devices WHERE device_code='TH-B1-004'), 18, 'TEMP_HIGH', 'CRITICAL', 'B1区4号温湿度传感器连续超温，疑似蒸发器结霜或回风受阻', 9.80, 70.20, 8.00, 'UNHANDLED', NULL, NULL, NULL, NOW() - INTERVAL 2 HOUR, NOW(), NOW() - INTERVAL 2 HOUR, NOW() - INTERVAL 10 MINUTE, 6),
((SELECT id FROM devices WHERE device_code='TH-B1-005'), 18, 'DEVICE_OFFLINE', 'HIGH', 'B1区5号温湿度传感器离线超过3小时', NULL, NULL, NULL, 'UNHANDLED', NULL, NULL, NULL, NOW() - INTERVAL 3 HOUR, NOW(), NOW() - INTERVAL 3 HOUR, NOW() - INTERVAL 20 MINUTE, 4),
((SELECT id FROM devices WHERE device_code='FREEZER-002'), 12, 'TEMP_HIGH', 'HIGH', '超低温保存冰柜2号温度高于-60℃阈值', -58.30, 37.60, -60.00, 'HANDLING', 20, NOW() - INTERVAL 35 MINUTE, '已通知值班机修工检查压缩机和门封', NOW() - INTERVAL 1 HOUR, NOW(), NOW() - INTERVAL 1 HOUR, NOW() - INTERVAL 8 MINUTE, 3),
((SELECT id FROM devices WHERE device_code='VEHICLE-002'), 9, 'HUMI_HIGH', 'MEDIUM', '冷链配送车002湿度偏高，建议检查货厢门封和装卸时长', 4.20, 74.80, 70.00, 'RESOLVED', 16, NOW() - INTERVAL 25 MINUTE, '完成车厢排湿并复核温湿度', NOW() - INTERVAL 5 HOUR, NOW(), NOW() - INTERVAL 5 HOUR, NOW() - INTERVAL 1 HOUR, 2),
((SELECT id FROM devices WHERE device_code='TH-A2-002'), 17, 'TEMP_LOW', 'LOW', 'A2区2号传感器温度接近下限，需观察制冷策略', -24.30, 48.00, -25.00, 'IGNORED', 16, NOW() - INTERVAL 2 HOUR, '波动在允许范围内，继续观察', NOW() - INTERVAL 10 HOUR, NOW(), NOW() - INTERVAL 10 HOUR, NOW() - INTERVAL 9 HOUR, 1);

UPDATE devices SET has_unresolved_alert = 1 WHERE device_code IN ('TH-B1-004','TH-B1-005','FREEZER-002');
UPDATE devices SET has_unresolved_alert = 0 WHERE device_code IN ('TH-A1-001','TH-A1-002','TH-A1-003','TH-A2-001','TH-A2-002','VEHICLE-002','DOOR-003');

INSERT INTO work_orders (
  order_no, ref_alert_id, title, description, priority, order_type, warehouse_id, device_id, status,
  creator_id, assigned_to, due_time, completed_time, verified_time, verification_result,
  location_detail, created_time, updated_time
) VALUES
('WO-DEMO-20260504-001',
 (SELECT id FROM alerts WHERE message='B1区4号温湿度传感器连续超温，疑似蒸发器结霜或回风受阻' ORDER BY id DESC LIMIT 1),
 'B1区蒸发器结霜排查',
 'B1区4号点位连续超温，检查蒸发器结霜、风机运行和回风通道。',
 'URGENT', 'ALERT_FIX', 18, (SELECT id FROM devices WHERE device_code='TH-B1-004'), 'PENDING',
 15, 20, NOW() + INTERVAL 2 HOUR, NULL, NULL, NULL, 'B栋冷藏库 B02 货架西侧蒸发器', NOW() - INTERVAL 1 HOUR, NOW()),
('WO-DEMO-20260504-002',
 (SELECT id FROM alerts WHERE message='B1区5号温湿度传感器离线超过3小时' ORDER BY id DESC LIMIT 1),
 'B1区5号传感器离线恢复',
 '核查B1区5号传感器供电、网关信号和设备绑定状态。',
 'HIGH', 'MAINTENANCE', 18, (SELECT id FROM devices WHERE device_code='TH-B1-005'), 'PROCESSING',
 15, 18, NOW() + INTERVAL 4 HOUR, NULL, NULL, NULL, 'B栋冷藏库 B02 货架东侧门口', NOW() - INTERVAL 2 HOUR, NOW()),
('WO-DEMO-20260504-003',
 (SELECT id FROM alerts WHERE message='超低温保存冰柜2号温度高于-60℃阈值' ORDER BY id DESC LIMIT 1),
 '超低温冰柜2号温度回升处理',
 '检查压缩机、门封和近期频繁开门记录，恢复后复测30分钟。',
 'HIGH', 'ALERT_FIX', 12, (SELECT id FROM devices WHERE device_code='FREEZER-002'), 'VERIFYING',
 15, 20, NOW() + INTERVAL 1 HOUR, NOW() - INTERVAL 20 MINUTE, NULL, NULL, 'A栋实验区北侧', NOW() - INTERVAL 50 MINUTE, NOW()),
('WO-DEMO-20260504-004',
 NULL,
 'A区温湿度传感器月度校准',
 '抽检A1/A2区温湿度传感器读数漂移，补充校准记录。',
 'MEDIUM', 'INSPECTION', 10, NULL, 'PENDING',
 15, 17, NOW() + INTERVAL 2 DAY, NULL, NULL, NULL, 'A栋冷冻库', NOW() - INTERVAL 3 HOUR, NOW())
ON DUPLICATE KEY UPDATE
  status = VALUES(status),
  priority = VALUES(priority),
  assigned_to = VALUES(assigned_to),
  due_time = VALUES(due_time),
  completed_time = VALUES(completed_time),
  verified_time = VALUES(verified_time),
  updated_time = NOW();

INSERT INTO ai_chat_sessions (user_id, title, is_deleted, created_time, updated_time)
VALUES
(1, '数据库运行态综合研判', 0, NOW() - INTERVAL 2 DAY, NOW() - INTERVAL 2 DAY),
(1, 'B1区连续超温复盘', 0, NOW() - INTERVAL 1 DAY, NOW() - INTERVAL 1 DAY);

INSERT INTO ai_chat_messages (session_id, role, content, attachment_type, attachment_id, tokens_used, created_time, updated_time)
SELECT s.id, 'USER', '请基于数据库分析当前冷链运行风险', NULL, NULL, 0, s.created_time, s.updated_time
FROM ai_chat_sessions s
WHERE s.title = '数据库运行态综合研判'
  AND NOT EXISTS (SELECT 1 FROM ai_chat_messages m WHERE m.session_id = s.id AND m.content = '请基于数据库分析当前冷链运行风险');

INSERT INTO ai_chat_messages (session_id, role, content, attachment_type, attachment_id, tokens_used, created_time, updated_time)
SELECT s.id, 'ASSISTANT',
'### 结论
当前主要风险集中在 B1 区连续超温、传感器离线和超低温冰柜温度回升。

### 数据依据
- B1区4号温湿度传感器存在未处理紧急超温告警。
- B1区5号温湿度传感器离线超过3小时。
- 超低温保存冰柜2号已进入处理中/待验收链路。

### 处置建议
1. 优先处理 B1 区蒸发器结霜和回风通道。
2. 同步恢复离线传感器，避免监测盲区。
3. 对冰柜2号复测30分钟并记录闭环结果。',
NULL, NULL, 0, s.created_time + INTERVAL 1 MINUTE, s.updated_time
FROM ai_chat_sessions s
WHERE s.title = '数据库运行态综合研判'
  AND NOT EXISTS (SELECT 1 FROM ai_chat_messages m WHERE m.session_id = s.id AND m.role = 'ASSISTANT' AND m.content LIKE '### 结论%当前主要风险%');

SET FOREIGN_KEY_CHECKS = 1;
