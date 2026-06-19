SET NAMES utf8mb4 COLLATE utf8mb4_unicode_ci;
SET collation_connection = 'utf8mb4_unicode_ci';

-- Demo data extension for ColdChain Guardian.
-- Scope: 2026-05-04 15:00:00 through the current database time.
-- Safe to run repeatedly: this script removes only rows tagged with this demo source
-- or order/session prefixes before inserting the refreshed dataset.

SET @demo_source := 'demo_extend_20260511';
START TRANSACTION;

UPDATE alerts a
JOIN work_orders w ON w.id = a.work_order_id
SET a.work_order_id = NULL
WHERE w.order_no LIKE 'WO-DEMO-EXT-%';

DELETE l
FROM work_order_logs l
JOIN work_orders w ON w.id = l.work_order_id
WHERE w.order_no LIKE 'WO-DEMO-EXT-%';

DELETE FROM work_orders
WHERE order_no LIKE 'WO-DEMO-EXT-%';

DELETE FROM alerts
WHERE message COLLATE utf8mb4_unicode_ci LIKE _utf8mb4'[演示数据]%' COLLATE utf8mb4_unicode_ci
  AND created_time >= CAST('2026-05-04 15:00:00' AS DATETIME);

DELETE m
FROM ai_chat_messages m
JOIN ai_chat_sessions s ON s.id = m.session_id
WHERE s.title COLLATE utf8mb4_unicode_ci LIKE _utf8mb4'[演示数据]%' COLLATE utf8mb4_unicode_ci;

DELETE FROM ai_chat_sessions
WHERE title COLLATE utf8mb4_unicode_ci LIKE _utf8mb4'[演示数据]%' COLLATE utf8mb4_unicode_ci;

DELETE FROM sensor_data
WHERE raw_data LIKE CONCAT('%', @demo_source, '%');

UPDATE devices d
JOIN (
  SELECT 'TH-A1-001' device_code, 'BIN-A01' area_code
  UNION ALL SELECT 'TH-A1-002', 'BIN-A02'
  UNION ALL SELECT 'TH-A1-003', 'BIN-A02'
  UNION ALL SELECT 'TH-A2-001', 'FL-A2'
  UNION ALL SELECT 'TH-A2-002', 'FL-A2'
  UNION ALL SELECT 'TH-B1-001', 'BIN-B01'
  UNION ALL SELECT 'TH-B1-002', 'BIN-B02'
  UNION ALL SELECT 'TH-B1-003', 'BIN-B02'
  UNION ALL SELECT 'TH-B1-004', 'BIN-B02'
  UNION ALL SELECT 'TH-B1-005', 'BIN-B02'
  UNION ALL SELECT 'DOOR-001', 'FL-A1'
  UNION ALL SELECT 'DOOR-002', 'FL-B1'
  UNION ALL SELECT 'DOOR-003', 'FL-A2'
  UNION ALL SELECT 'FREEZER-001', 'FL-A1'
  UNION ALL SELECT 'FREEZER-002', 'FL-A1'
  UNION ALL SELECT 'VEHICLE-001', 'SITE-MAIN'
  UNION ALL SELECT 'VEHICLE-002', 'SITE-MAIN'
) m ON m.device_code = d.device_code
JOIN warehouse_areas w ON w.area_code = m.area_code
SET d.area_id = w.id,
    d.updated_time = NOW();

INSERT INTO sensor_data (
  device_id, temperature, humidity, data_time, battery_level, signal_strength,
  raw_data, created_time, update_time
)
SELECT
  d.id,
  ROUND(
    CASE d.device_code
      WHEN 'TH-A1-001' THEN -2.80 + (seq.n % 9) * 0.10 + IF(HOUR(seq.tick_time) BETWEEN 11 AND 15, 0.15, 0)
      WHEN 'TH-A1-002' THEN -2.30 + (seq.n % 8) * 0.09 + IF(DATE(seq.tick_time) = '2026-05-04' AND HOUR(seq.tick_time) BETWEEN 16 AND 18, 2.60, 0)
      WHEN 'TH-A1-003' THEN -2.55 + (seq.n % 7) * 0.11
      WHEN 'TH-A2-001' THEN -5.20 + (seq.n % 10) * 0.12 + IF(DATE(seq.tick_time) = '2026-05-06' AND HOUR(seq.tick_time) BETWEEN 9 AND 11, -1.10, 0)
      WHEN 'TH-A2-002' THEN -4.90 + (seq.n % 8) * 0.13 + IF(DATE(seq.tick_time) = '2026-05-11' AND HOUR(seq.tick_time) BETWEEN 11 AND 13, -0.80, 0)
      WHEN 'TH-B1-001' THEN 5.20 + (seq.n % 8) * 0.18
      WHEN 'TH-B1-002' THEN 5.90 + (seq.n % 8) * 0.19 + IF(DATE(seq.tick_time) = '2026-05-11' AND HOUR(seq.tick_time) BETWEEN 14 AND 16, 0.85, 0)
      WHEN 'TH-B1-003' THEN 5.70 + (seq.n % 7) * 0.17
      WHEN 'TH-B1-004' THEN
        CASE
          WHEN seq.tick_time >= '2026-05-10 08:00:00' THEN 8.60 + (seq.n % 7) * 0.24
          WHEN DATE(seq.tick_time) = '2026-05-07' AND HOUR(seq.tick_time) BETWEEN 10 AND 12 THEN 8.20 + (seq.n % 5) * 0.18
          ELSE 5.80 + (seq.n % 8) * 0.16
        END
      WHEN 'TH-B1-005' THEN 5.60 + (seq.n % 8) * 0.18
      WHEN 'FREEZER-001' THEN
        CASE
          WHEN DATE(seq.tick_time) = '2026-05-10' AND HOUR(seq.tick_time) BETWEEN 18 AND 20 THEN -80.80 - (seq.n % 4) * 0.25
          WHEN DATE(seq.tick_time) = '2026-05-05' AND HOUR(seq.tick_time) BETWEEN 8 AND 10 THEN -59.20 + (seq.n % 3) * 0.18
          ELSE -70.90 + (seq.n % 9) * 0.16
        END
      WHEN 'FREEZER-002' THEN
        CASE
          WHEN seq.tick_time >= '2026-05-11 07:30:00' THEN -58.90 + (seq.n % 5) * 0.22
          WHEN DATE(seq.tick_time) = '2026-05-09' AND HOUR(seq.tick_time) BETWEEN 6 AND 8 THEN -59.10 + (seq.n % 4) * 0.16
          ELSE -66.80 + (seq.n % 8) * 0.20
        END
      WHEN 'VEHICLE-001' THEN 3.80 + (seq.n % 9) * 0.20
      WHEN 'VEHICLE-002' THEN 3.40 + (seq.n % 9) * 0.18
    END,
    2
  ) AS temperature,
  ROUND(
    CASE d.device_code
      WHEN 'TH-A1-001' THEN 43.00 + (seq.n % 12) * 0.55
      WHEN 'TH-A1-002' THEN 42.30 + (seq.n % 10) * 0.50
      WHEN 'TH-A1-003' THEN IF(DATE(seq.tick_time) = '2026-05-08' AND HOUR(seq.tick_time) BETWEEN 13 AND 15, 28.20 + (seq.n % 4) * 0.30, 44.10 + (seq.n % 11) * 0.52)
      WHEN 'TH-A2-001' THEN 45.50 + (seq.n % 10) * 0.62
      WHEN 'TH-A2-002' THEN 46.20 + (seq.n % 12) * 0.55
      WHEN 'TH-B1-001' THEN IF(DATE(seq.tick_time) = '2026-05-05' AND HOUR(seq.tick_time) BETWEEN 14 AND 17, 66.80 + (seq.n % 5) * 0.62, 52.00 + (seq.n % 12) * 0.58)
      WHEN 'TH-B1-002' THEN IF(DATE(seq.tick_time) = '2026-05-11' AND HOUR(seq.tick_time) BETWEEN 14 AND 16, 66.30 + (seq.n % 5) * 0.58, 53.20 + (seq.n % 10) * 0.60)
      WHEN 'TH-B1-003' THEN 53.80 + (seq.n % 8) * 0.55
      WHEN 'TH-B1-004' THEN IF(seq.tick_time >= '2026-05-10 08:00:00', 66.20 + (seq.n % 8) * 0.55, 54.00 + (seq.n % 11) * 0.55)
      WHEN 'TH-B1-005' THEN 54.60 + (seq.n % 10) * 0.50
      WHEN 'FREEZER-001' THEN 34.80 + (seq.n % 6) * 0.35
      WHEN 'FREEZER-002' THEN 36.10 + (seq.n % 7) * 0.38
      WHEN 'VEHICLE-001' THEN 53.00 + (seq.n % 8) * 0.72
      WHEN 'VEHICLE-002' THEN IF(DATE(seq.tick_time) = '2026-05-08' AND HOUR(seq.tick_time) BETWEEN 8 AND 10, 72.00 + (seq.n % 5) * 0.45, 52.50 + (seq.n % 9) * 0.62)
    END,
    2
  ) AS humidity,
  seq.tick_time,
  GREATEST(52, 96 - MOD(seq.n + d.id, 42)) AS battery_level,
  -44 - MOD(seq.n + d.id, 20) AS signal_strength,
  JSON_OBJECT(
    'source', @demo_source,
    'tick', seq.n,
    'scenario',
    CASE
      WHEN d.device_code = 'TH-B1-004' AND seq.tick_time >= '2026-05-10 08:00:00' THEN 'b1_continuous_over_temperature'
      WHEN d.device_code = 'FREEZER-002' AND seq.tick_time >= '2026-05-11 07:30:00' THEN 'freezer_temperature_recovery_required'
      WHEN d.device_code IN ('TH-B1-005', 'TH-B1-003', 'VEHICLE-001') THEN 'offline_before_today'
      ELSE 'normal_operation'
    END
  ),
  NOW(),
  NOW()
FROM devices d
JOIN (
  SELECT base.n, DATE_ADD(CAST('2026-05-04 15:00:00' AS DATETIME), INTERVAL (base.n * 30) MINUTE) AS tick_time
  FROM (
    SELECT ones.n + tens.n * 10 + hundreds.n * 100 AS n
    FROM
      (SELECT 0 n UNION ALL SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4 UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9) ones
      CROSS JOIN
      (SELECT 0 n UNION ALL SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4 UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9) tens
      CROSS JOIN
      (SELECT 0 n UNION ALL SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4 UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9) hundreds
  ) base
) seq
WHERE seq.tick_time <= NOW()
  AND d.device_code IN (
    'TH-A1-001','TH-A1-002','TH-A1-003','TH-A2-001','TH-A2-002',
    'TH-B1-001','TH-B1-002','TH-B1-003','TH-B1-004','TH-B1-005',
    'FREEZER-001','FREEZER-002','VEHICLE-001','VEHICLE-002'
  )
  AND NOT (d.device_code = 'TH-B1-003' AND seq.tick_time > '2026-05-06 18:30:00')
  AND NOT (d.device_code = 'VEHICLE-001' AND seq.tick_time > '2026-05-07 23:00:00')
  AND NOT (d.device_code = 'TH-B1-005' AND seq.tick_time > '2026-05-10 22:30:00');

UPDATE devices d
LEFT JOIN (
  SELECT sd.device_id, sd.temperature, sd.humidity, sd.data_time
  FROM sensor_data sd
  JOIN (
    SELECT device_id, MAX(data_time) AS max_data_time
    FROM sensor_data
    GROUP BY device_id
  ) mx ON mx.device_id = sd.device_id AND mx.max_data_time = sd.data_time
) latest ON latest.device_id = d.id
SET
  d.online_status = CASE
    WHEN d.device_code IN ('TH-B1-003','TH-B1-005','VEHICLE-001') THEN 0
    ELSE 1
  END,
  d.latest_temp = latest.temperature,
  d.latest_humi = latest.humidity,
  d.latest_data_time = latest.data_time,
  d.last_seen_time = CASE
    WHEN d.device_code IN ('TH-B1-003','TH-B1-005','VEHICLE-001') THEN latest.data_time
    ELSE DATE_SUB(NOW(), INTERVAL (MOD(d.id, 5) + 1) MINUTE)
  END,
  d.updated_time = NOW()
WHERE d.device_code IN (
  'TH-A1-001','TH-A1-002','TH-A1-003','TH-A2-001','TH-A2-002',
  'TH-B1-001','TH-B1-002','TH-B1-003','TH-B1-004','TH-B1-005',
  'FREEZER-001','FREEZER-002','VEHICLE-001','VEHICLE-002'
);

UPDATE devices
SET
  online_status = 1,
  last_seen_time = DATE_SUB(NOW(), INTERVAL (MOD(id, 4) + 1) MINUTE),
  latest_data_time = DATE_SUB(NOW(), INTERVAL (MOD(id, 4) + 1) MINUTE),
  updated_time = NOW()
WHERE device_code IN ('DOOR-001','DOOR-002','DOOR-003');

INSERT INTO alerts (
  device_id, warehouse_id, alert_type, alert_level, message, temperature, humidity,
  threshold_value, status, handler_user_id, handle_time, handle_remark,
  created_time, updated_time, first_time, last_time, trigger_count
)
SELECT
  d.id,
  d.area_id,
  x.alert_type,
  x.alert_level,
  x.message,
  x.temperature,
  x.humidity,
  x.threshold_value,
  x.status,
  x.handler_user_id,
  x.handle_time,
  x.handle_remark,
  x.created_time,
  x.updated_time,
  x.first_time,
  x.last_time,
  x.trigger_count
FROM (
  SELECT 'TH-A1-002' device_code, 'TEMP_HIGH' alert_type, 'MEDIUM' alert_level, '[演示数据] A1区2号温湿度传感器短时温度接近上限，已完成复核' message, 0.42 temperature, 45.80 humidity, 0.00 threshold_value, 'RESOLVED' status, 18 handler_user_id, '2026-05-04 18:05:00' handle_time, '核查为装卸开门造成的短时波动，复测后恢复正常' handle_remark, '2026-05-04 16:20:00' created_time, '2026-05-04 18:05:00' updated_time, '2026-05-04 16:20:00' first_time, '2026-05-04 17:40:00' last_time, 3 trigger_count
  UNION ALL SELECT 'FREEZER-001','TEMP_HIGH','HIGH','[演示数据] 超低温保存冰柜温度高于-60℃阈值，疑似门封未闭合',-58.80,35.60,-60.00,'RESOLVED',20,'2026-05-05 11:20:00','已重新压紧门封并复测，温度回落至-66℃以下','2026-05-05 09:10:00','2026-05-05 11:20:00','2026-05-05 09:10:00','2026-05-05 10:40:00',4
  UNION ALL SELECT 'TH-B1-001','HUMI_HIGH','MEDIUM','[演示数据] B1区1号点位湿度偏高，需检查回风口和货物外包装',6.40,68.20,65.00,'RESOLVED',17,'2026-05-05 17:35:00','完成除湿巡检，回风口无遮挡，湿度恢复至安全范围','2026-05-05 15:40:00','2026-05-05 17:35:00','2026-05-05 15:40:00','2026-05-05 16:50:00',3
  UNION ALL SELECT 'TH-A2-001','TEMP_LOW','LOW','[演示数据] A2区1号传感器温度短时低于策略观察线',-6.10,48.70,-5.80,'IGNORED',16,'2026-05-06 12:05:00','制冷策略切换导致的允许波动，保留观察记录','2026-05-06 10:25:00','2026-05-06 12:05:00','2026-05-06 10:25:00','2026-05-06 11:10:00',2
  UNION ALL SELECT 'TH-B1-003','DEVICE_OFFLINE','HIGH','[演示数据] B1区3号温湿度传感器离线，需恢复供电和网关绑定',NULL,NULL,NULL,'RESOLVED',18,'2026-05-06 20:30:00','更换电池并重新绑定网关，离线告警关闭','2026-05-06 17:15:00','2026-05-06 20:30:00','2026-05-06 17:15:00','2026-05-06 19:50:00',5
  UNION ALL SELECT 'TH-B1-002','TEMP_HIGH','MEDIUM','[演示数据] B1区2号传感器午间温度高于8℃，已安排复查',8.55,56.70,8.00,'RESOLVED',18,'2026-05-07 13:40:00','现场清理货架间隙后温度恢复','2026-05-07 11:30:00','2026-05-07 13:40:00','2026-05-07 11:30:00','2026-05-07 12:50:00',3
  UNION ALL SELECT 'VEHICLE-001','DEVICE_OFFLINE','MEDIUM','[演示数据] 冷链配送车001长时间未上报，需检查车载终端电源',NULL,NULL,NULL,'HANDLING',20,'2026-05-08 08:20:00','已联系车辆负责人，等待车辆返场复测','2026-05-07 23:20:00',NOW(),'2026-05-07 23:20:00','2026-05-08 08:00:00',6
  UNION ALL SELECT 'VEHICLE-002','HUMI_HIGH','MEDIUM','[演示数据] 冷链配送车002装卸后湿度偏高，已完成排湿复核',4.80,73.40,70.00,'RESOLVED',17,'2026-05-08 11:10:00','完成车厢除湿并复核记录，湿度回落','2026-05-08 08:50:00','2026-05-08 11:10:00','2026-05-08 08:50:00','2026-05-08 10:10:00',4
  UNION ALL SELECT 'TH-A1-003','HUMI_LOW','LOW','[演示数据] A1区3号点位湿度低于下限，已观察并恢复',-2.20,28.60,30.00,'RESOLVED',17,'2026-05-08 16:15:00','除霜后湿度恢复，暂不追加工单','2026-05-08 14:30:00','2026-05-08 16:15:00','2026-05-08 14:30:00','2026-05-08 15:20:00',2
  UNION ALL SELECT 'FREEZER-002','TEMP_HIGH','HIGH','[演示数据] 超低温保存冰柜2号清晨温度回升，已完成压缩机巡检',-58.90,37.10,-60.00,'RESOLVED',20,'2026-05-09 09:20:00','清理冷凝器并复测30分钟，温度下降至-63℃','2026-05-09 06:40:00','2026-05-09 09:20:00','2026-05-09 06:40:00','2026-05-09 08:10:00',4
  UNION ALL SELECT 'TH-B1-004','HUMI_HIGH','MEDIUM','[演示数据] B1区4号点位湿度偏高，回风通道需持续观察',6.90,67.80,65.00,'RESOLVED',18,'2026-05-09 23:10:00','已完成回风通道清理，湿度暂时恢复','2026-05-09 21:10:00','2026-05-09 23:10:00','2026-05-09 21:10:00','2026-05-09 22:40:00',3
  UNION ALL SELECT 'TH-B1-004','TEMP_HIGH','CRITICAL','[演示数据] B1区4号温湿度传感器连续超温，疑似蒸发器结霜或回风受阻',9.08,68.40,8.00,'UNHANDLED',NULL,NULL,NULL,'2026-05-10 08:25:00',NOW(),'2026-05-10 08:25:00',DATE_SUB(NOW(), INTERVAL 15 MINUTE),18
  UNION ALL SELECT 'FREEZER-001','TEMP_LOW','LOW','[演示数据] 超低温保存冰柜温度低于-80℃观察线，已记录策略波动',-81.20,35.10,-80.00,'RESOLVED',20,'2026-05-10 21:15:00','制冷策略切换导致短时低温，已恢复正常','2026-05-10 19:40:00','2026-05-10 21:15:00','2026-05-10 19:40:00','2026-05-10 20:30:00',2
  UNION ALL SELECT 'FREEZER-002','TEMP_HIGH','HIGH','[演示数据] 超低温保存冰柜2号当前温度高于-60℃阈值，需继续处理并复测',-58.24,37.60,-60.00,'HANDLING',20,'2026-05-11 08:10:00','已派发维修工单，正在检查压缩机负载和门封状态','2026-05-11 07:45:00',NOW(),'2026-05-11 07:45:00',DATE_SUB(NOW(), INTERVAL 10 MINUTE),7
  UNION ALL SELECT 'TH-B1-005','DEVICE_OFFLINE','HIGH','[演示数据] B1区5号温湿度传感器离线超过8小时，存在监测盲区',NULL,NULL,NULL,'UNHANDLED',NULL,NULL,NULL,'2026-05-11 08:35:00',NOW(),'2026-05-11 08:35:00',DATE_SUB(NOW(), INTERVAL 20 MINUTE),8
  UNION ALL SELECT 'TH-A2-002','TEMP_LOW','LOW','[演示数据] A2区2号传感器温度接近下限，已标记为观察项',-5.55,50.40,-5.00,'IGNORED',16,'2026-05-11 13:35:00','波动幅度较小，未影响库区控制策略','2026-05-11 12:20:00','2026-05-11 13:35:00','2026-05-11 12:20:00','2026-05-11 13:05:00',2
  UNION ALL SELECT 'TH-B1-002','HUMI_HIGH','MEDIUM','[演示数据] B1区2号温湿度传感器湿度再次偏高，需现场核查货物外包装',6.85,67.90,65.00,'UNHANDLED',NULL,NULL,NULL,'2026-05-11 15:10:00',NOW(),'2026-05-11 15:10:00',DATE_SUB(NOW(), INTERVAL 5 MINUTE),3
) x
JOIN devices d ON d.device_code = x.device_code;

INSERT INTO work_orders (
  order_no, ref_alert_id, title, description, priority, order_type, warehouse_id, device_id,
  status, creator_id, assigned_to, due_time, completed_time, verified_time,
  verification_result, location_detail, created_time, updated_time
)
SELECT
  x.order_no,
  a.id,
  x.title,
  x.description,
  x.priority,
  x.order_type,
  COALESCE(dw.id, fallback.id, x.warehouse_id),
  d.id,
  x.status,
  x.creator_id,
  x.assigned_to,
  x.due_time,
  x.completed_time,
  x.verified_time,
  x.verification_result,
  x.location_detail,
  x.created_time,
  x.updated_time
FROM (
  SELECT 'WO-DEMO-EXT-20260504-001' order_no, '[演示数据] A1区2号温湿度传感器短时温度接近上限，已完成复核' alert_message, 'TH-A1-002' device_code, 16 warehouse_id, 'A1区短时超温复核' title, '复核A1区2号点位短时升温原因，检查装卸开门记录和温度回落曲线。' description, 'MEDIUM' priority, 'ALERT_FIX' order_type, 'COMPLETED' status, 15 creator_id, 18 assigned_to, '2026-05-04 19:00:00' due_time, '2026-05-04 17:55:00' completed_time, '2026-05-04 18:05:00' verified_time, '复测曲线稳定，确认已闭环。' verification_result, 'A1区A02货架中部' location_detail, '2026-05-04 16:30:00' created_time, '2026-05-04 18:05:00' updated_time
  UNION ALL SELECT 'WO-DEMO-EXT-20260505-001','[演示数据] 超低温保存冰柜温度高于-60℃阈值，疑似门封未闭合','FREEZER-001',12,'超低温冰柜门封检查','检查冰柜门封、压缩机运行声音和开门记录，恢复后连续复测30分钟。','HIGH','ALERT_FIX','CLOSED',15,20,'2026-05-05 12:00:00','2026-05-05 10:55:00','2026-05-05 11:20:00','验收通过，温度回落至安全区间。','A区一层超低温保存区','2026-05-05 09:25:00','2026-05-05 11:35:00'
  UNION ALL SELECT 'WO-DEMO-EXT-20260505-002','[演示数据] B1区1号点位湿度偏高，需检查回风口和货物外包装','TH-B1-001',18,'B1区湿度偏高排查','检查B01货架外包装含水、回风口遮挡和除湿设备状态。','MEDIUM','ALERT_FIX','COMPLETED',15,17,'2026-05-05 18:30:00','2026-05-05 17:20:00','2026-05-05 17:35:00','除湿处理完成，湿度恢复正常。','B1区B01货架南侧','2026-05-05 15:55:00','2026-05-05 17:35:00'
  UNION ALL SELECT 'WO-DEMO-EXT-20260506-001','[演示数据] B1区3号温湿度传感器离线，需恢复供电和网关绑定','TH-B1-003',18,'B1区3号传感器离线恢复','检查传感器电池、网关信号和设备绑定状态，恢复后观察上报频率。','HIGH','MAINTENANCE','COMPLETED',15,18,'2026-05-06 21:00:00','2026-05-06 20:10:00','2026-05-06 20:30:00','设备恢复上报，现场确认无监测盲区。','B1区B02货架东侧','2026-05-06 17:30:00','2026-05-06 20:30:00'
  UNION ALL SELECT 'WO-DEMO-EXT-20260507-001','[演示数据] 冷链配送车001长时间未上报，需检查车载终端电源','VEHICLE-001',9,'配送车001车载终端返场检查','车辆返场后检查车载终端供电、4G卡状态和定位上报链路。','MEDIUM','MAINTENANCE','PROCESSING',15,20,'2026-05-12 10:00:00',NULL,NULL,NULL,'装车月台1号位','2026-05-08 08:30:00',NOW()
  UNION ALL SELECT 'WO-DEMO-EXT-20260508-001','[演示数据] 冷链配送车002装卸后湿度偏高，已完成排湿复核','VEHICLE-002',9,'配送车002排湿复核','完成车厢排湿、门封检查和装卸时长复盘。','MEDIUM','ALERT_FIX','CLOSED',15,17,'2026-05-08 12:00:00','2026-05-08 10:55:00','2026-05-08 11:10:00','排湿后复测合格，工单归档。','装车月台2号位','2026-05-08 09:05:00','2026-05-08 11:20:00'
  UNION ALL SELECT 'WO-DEMO-EXT-20260508-002',NULL,NULL,10,'A区传感器月度校准抽检','抽检A区温湿度传感器漂移情况，补充校准记录和照片。','MEDIUM','INSPECTION','COMPLETED',15,17,'2026-05-08 18:00:00','2026-05-08 16:40:00','2026-05-08 17:00:00','抽检点位误差在允许范围内。','A栋冷冻库A1/A2区','2026-05-08 13:20:00','2026-05-08 17:00:00'
  UNION ALL SELECT 'WO-DEMO-EXT-20260509-001','[演示数据] 超低温保存冰柜2号清晨温度回升，已完成压缩机巡检','FREEZER-002',12,'冰柜2号压缩机巡检复测','检查压缩机负载、门封和冷凝器，完成后提交连续复测记录。','HIGH','ALERT_FIX','VERIFYING',15,20,'2026-05-09 10:30:00','2026-05-09 09:05:00',NULL,NULL,'A区一层超低温保存区北侧','2026-05-09 06:55:00','2026-05-09 09:10:00'
  UNION ALL SELECT 'WO-DEMO-EXT-20260510-001','[演示数据] B1区4号温湿度传感器连续超温，疑似蒸发器结霜或回风受阻','TH-B1-004',18,'B1区4号点位连续超温紧急处理','优先检查蒸发器结霜、回风通道、风机运行和货物堆放间距。','URGENT','ALERT_FIX','PENDING',15,18,'2026-05-11 18:00:00',NULL,NULL,NULL,'B1区B02货架西侧蒸发器附近','2026-05-10 08:40:00',NOW()
  UNION ALL SELECT 'WO-DEMO-EXT-20260510-002','[演示数据] B1区4号点位湿度偏高，回风通道需持续观察','TH-B1-004',18,'B1区回风通道清理','清理B02货架回风通道并记录湿度变化曲线。','MEDIUM','MAINTENANCE','COMPLETED',15,18,'2026-05-10 23:30:00','2026-05-09 22:50:00','2026-05-09 23:10:00','通道清理完成，但后续温度风险仍需持续跟踪。','B1区B02货架西侧','2026-05-09 21:20:00','2026-05-09 23:10:00'
  UNION ALL SELECT 'WO-DEMO-EXT-20260511-001','[演示数据] 超低温保存冰柜2号当前温度高于-60℃阈值，需继续处理并复测','FREEZER-002',12,'冰柜2号当前高温处理','检查压缩机、门封和货物摆放，处理后连续复测并提交验收。','HIGH','ALERT_FIX','PROCESSING',15,20,'2026-05-11 19:00:00',NULL,NULL,NULL,'A区一层超低温保存区北侧','2026-05-11 08:00:00',NOW()
  UNION ALL SELECT 'WO-DEMO-EXT-20260511-002','[演示数据] B1区5号温湿度传感器离线超过8小时，存在监测盲区','TH-B1-005',18,'B1区5号传感器离线恢复','核查传感器电池、网关连接和设备绑定，恢复后观察15分钟。','HIGH','MAINTENANCE','PENDING',15,18,'2026-05-11 20:00:00',NULL,NULL,NULL,'B1区B02货架东侧门口','2026-05-11 08:45:00',NOW()
  UNION ALL SELECT 'WO-DEMO-EXT-20260511-003',NULL,NULL,9,'今日全库巡检与告警复盘','汇总今日告警、工单和设备在线情况，形成晚班交接记录。','MEDIUM','INSPECTION','PENDING',15,19,'2026-05-11 22:00:00',NULL,NULL,NULL,'聊城冷链中心全库区','2026-05-11 14:00:00',NOW()
  UNION ALL SELECT 'WO-DEMO-EXT-20260511-004','[演示数据] B1区2号温湿度传感器湿度再次偏高，需现场核查货物外包装','TH-B1-002',18,'B1区2号湿度偏高现场核查','核查B1区2号点位货物外包装、除湿设备和人员通道开门频次。','MEDIUM','ALERT_FIX','PENDING',15,17,'2026-05-11 19:30:00',NULL,NULL,NULL,'B1区B02货架中部','2026-05-11 15:20:00',NOW()
) x
LEFT JOIN devices d ON d.device_code = x.device_code
LEFT JOIN warehouse_areas dw ON dw.id = d.area_id
CROSS JOIN (SELECT MIN(id) AS id FROM warehouse_areas) fallback
LEFT JOIN alerts a ON a.message = x.alert_message;

UPDATE alerts a
JOIN work_orders w ON w.ref_alert_id = a.id
SET a.work_order_id = w.id,
    a.updated_time = NOW()
WHERE w.order_no LIKE 'WO-DEMO-EXT-%';

INSERT INTO work_order_logs (work_order_id, operator_id, action, remark, attachment_urls, created_time)
SELECT id, creator_id, 'CREATED', '演示数据：系统根据告警或巡检计划创建工单', NULL, created_time
FROM work_orders
WHERE order_no LIKE 'WO-DEMO-EXT-%';

INSERT INTO work_order_logs (work_order_id, operator_id, action, remark, attachment_urls, created_time)
SELECT id, assigned_to, 'STARTED', '演示数据：责任人接收工单并开始现场处理', NULL, DATE_ADD(created_time, INTERVAL 20 MINUTE)
FROM work_orders
WHERE order_no LIKE 'WO-DEMO-EXT-%'
  AND status IN ('PROCESSING','VERIFYING','COMPLETED','CLOSED');

INSERT INTO work_order_logs (work_order_id, operator_id, action, remark, attachment_urls, created_time)
SELECT id, assigned_to, 'COMPLETED', '演示数据：现场处理完成，提交验收', NULL, completed_time
FROM work_orders
WHERE order_no LIKE 'WO-DEMO-EXT-%'
  AND status IN ('VERIFYING','COMPLETED','CLOSED')
  AND completed_time IS NOT NULL;

INSERT INTO work_order_logs (work_order_id, operator_id, action, remark, attachment_urls, created_time)
SELECT id, creator_id, 'VERIFIED', COALESCE(verification_result, '演示数据：验收通过'), NULL, verified_time
FROM work_orders
WHERE order_no LIKE 'WO-DEMO-EXT-%'
  AND status IN ('COMPLETED','CLOSED')
  AND verified_time IS NOT NULL;

INSERT INTO work_order_logs (work_order_id, operator_id, action, remark, attachment_urls, created_time)
SELECT id, creator_id, 'CLOSED', '演示数据：工单归档关闭', NULL, updated_time
FROM work_orders
WHERE order_no LIKE 'WO-DEMO-EXT-%'
  AND status = 'CLOSED';

UPDATE devices d
LEFT JOIN (
  SELECT device_id, COUNT(*) AS unresolved_count
  FROM alerts
  WHERE status IN ('UNHANDLED','HANDLING')
  GROUP BY device_id
) ua ON ua.device_id = d.id
SET d.has_unresolved_alert = IF(COALESCE(ua.unresolved_count, 0) > 0, 1, 0),
    d.updated_time = NOW();

INSERT INTO ai_chat_sessions (user_id, title, is_deleted, created_time, updated_time)
VALUES
(1, '[演示数据] 5月5日至今运行总览', 0, '2026-05-09 17:30:00', '2026-05-09 17:33:00'),
(1, '[演示数据] B1区连续超温复盘', 0, '2026-05-10 18:30:00', '2026-05-10 18:35:00'),
(1, '[演示数据] 今日冷链风险研判', 0, DATE_SUB(NOW(), INTERVAL 35 MINUTE), DATE_SUB(NOW(), INTERVAL 32 MINUTE));

INSERT INTO ai_chat_messages (
  session_id, role, content, attachment_type, attachment_id, tokens_used, created_time, updated_time
)
SELECT s.id, 'USER', '请基于5月5日至今的数据库数据，概括冷链仓储运行情况。', NULL, NULL, 0, '2026-05-09 17:30:00', '2026-05-09 17:30:00'
FROM ai_chat_sessions s WHERE s.title = '[演示数据] 5月5日至今运行总览'
UNION ALL
SELECT s.id, 'ASSISTANT',
'### 结论
5月5日至今，系统已形成连续的传感器曲线、告警记录和工单闭环数据。A区整体稳定，B区存在阶段性温湿度波动，超低温冰柜2号需要重点跟踪。

### 数据依据
- 传感器数据覆盖 2026-05-04 15:00 至当前时间，包含A区、B区、超低温冰柜和冷链车辆。
- 已记录温度、湿度、设备离线、工单处理和验收归档等多类业务数据。
- 工单状态包含待处理、处理中、待验收、已完成和已关闭，适合用于页面筛选和统计测试。

### 建议
1. 优先关注B1区4号点位连续超温和B1区5号设备离线。
2. 对超低温冰柜2号执行连续复测，防止温度回升影响库存安全。
3. 使用数据分析页面查看5月5日至5月11日的告警趋势和工单闭环效率。', NULL, NULL, 538, '2026-05-09 17:33:00', '2026-05-09 17:33:00'
FROM ai_chat_sessions s WHERE s.title = '[演示数据] 5月5日至今运行总览'
UNION ALL
SELECT s.id, 'USER', '分析B1区4号连续超温的可能原因和处理顺序。', NULL, NULL, 0, '2026-05-10 18:30:00', '2026-05-10 18:30:00'
FROM ai_chat_sessions s WHERE s.title = '[演示数据] B1区连续超温复盘'
UNION ALL
SELECT s.id, 'ASSISTANT',
'### 结论
B1区4号点位从5月10日开始出现连续超温，且伴随湿度偏高，风险优先级应判定为紧急。

### 可能原因
- 蒸发器结霜或风机效率下降，导致局部回风不足。
- 货物堆放距离过近，影响冷风循环。
- 门口装卸频次偏高，使局部热湿负荷增加。

### 处理顺序
1. 先检查蒸发器、风机和回风通道。
2. 同步调整货架堆放间距，保持通风间隔。
3. 处理后连续观察30分钟温度曲线，确认恢复到2℃至8℃范围。', NULL, NULL, 486, '2026-05-10 18:35:00', '2026-05-10 18:35:00'
FROM ai_chat_sessions s WHERE s.title = '[演示数据] B1区连续超温复盘'
UNION ALL
SELECT s.id, 'USER', '请基于当前数据库快照列出今日Top风险和处置建议。', NULL, NULL, 0, DATE_SUB(NOW(), INTERVAL 35 MINUTE), DATE_SUB(NOW(), INTERVAL 35 MINUTE)
FROM ai_chat_sessions s WHERE s.title = '[演示数据] 今日冷链风险研判'
UNION ALL
SELECT s.id, 'ASSISTANT',
'### 结论
今日主要风险集中在B1区连续超温、B1区传感器离线和超低温冰柜2号温度回升，建议按“温度失控优先、监测盲区同步恢复、工单复测闭环”的顺序处理。

### Top风险
1. **B1区4号连续超温**：当前仍未处理，可能影响B02货架冷藏稳定性。
2. **超低温冰柜2号温度回升**：已进入处理中，需提交连续复测记录。
3. **B1区5号传感器离线**：存在监测盲区，应优先恢复上报。
4. **B1区2号湿度偏高**：需核查外包装和除湿设备状态。
5. **配送车001离线**：车辆返场后检查车载终端。

### 处置建议
- 将 B1区4号超温工单提升为紧急处理，安排技术人员现场排查。
- 对冰柜2号完成压缩机、门封和冷凝器检查后，保留30分钟复测曲线。
- 今日全库巡检应覆盖告警设备、离线设备和待验收工单。', NULL, NULL, 621, DATE_SUB(NOW(), INTERVAL 32 MINUTE), DATE_SUB(NOW(), INTERVAL 32 MINUTE)
FROM ai_chat_sessions s WHERE s.title = '[演示数据] 今日冷链风险研判';

COMMIT;
