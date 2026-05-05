-- 添加更多示例数据，丰富小程序展示
-- 包括：更多告警 + 更多工单 + 设备离线告警 + 湿度超标告警
-- 最终修正：alert_config_id 对于系统自发告警使用 NULL

SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- 添加更多告警数据
-- ----------------------------
INSERT INTO `alerts` (`device_id`, `warehouse_id`, `alert_config_id`, `alert_type`, `alert_level`, `message`, `temperature`, `humidity`, `threshold_value`, `status`, `work_order_id`, `created_time`, `trigger_count`) VALUES
-- device 1 (id=1) 在库区 3 (冷藏区 2~8℃) - 温度高
(1, 3, 2, 'TEMP_HIGH', 'HIGH', '冷藏区温度持续超过8℃，当前值 9.8℃，已超过上限8.0℃', 9.8, 48.0, 8.00, 'UNHANDLED', NULL, DATE_SUB(NOW(), INTERVAL 45 MINUTE), 2),
-- device 1 冷藏区，湿度高
(1, 3, NULL, 'HIGH_HUMI', 'MEDIUM', '冷藏区湿度超过65%，当前值 72%，需要检查除湿机运行', NULL, 72.0, 65.00, 'UNHANDLED', NULL, DATE_SUB(NOW(), INTERVAL 2 HOUR), 1),
-- device 3 冷冻区大门传感器离线
(3, 4, NULL, 'DEVICE_OFFLINE', 'HIGH', '冷冻区大门传感器离线已超过30分钟，可能影响保温', NULL, NULL, NULL, 'UNHANDLED', NULL, DATE_SUB(NOW(), INTERVAL 3 HOUR), 1),
-- device 2 冷冻区温度高
(2, 4, 1, 'TEMP_HIGH', 'CRITICAL', '冷冻区温度异常升高，当前 -11.2℃，已超过阈值 -18℃', -11.2, 48.5, -18.00, 'PROCESSING', 11, DATE_SUB(NOW(), INTERVAL 2 HOUR), 5),
-- device 5 冷冻区传感器低电量
(5, 4, NULL, 'DEVICE_LOW_BATTERY', 'NORMAL', '温度传感器电池电量低，请准备更换电池', NULL, NULL, NULL, 'HANDLED', NULL, DATE_SUB(NOW(), INTERVAL 2 DAY), 1);

-- ----------------------------
-- 添加更多工单数据（更多不同状态）
-- ----------------------------
INSERT INTO work_orders (id, order_no, title, description, priority, order_type, warehouse_id, device_id, status, creator_id, assigned_to, due_time, created_time, updated_time) VALUES
(16, 'WO-20260402-006', '处理冷藏区湿度超标', '冷藏区B区当前湿度72%超过阈值65%，需要检查除湿机是否工作正常，清理过滤网', 'MEDIUM', 'MAINTENANCE', 3, 1, 'PENDING', 1, 19, DATE_ADD(NOW(), INTERVAL 4 HOUR), NOW(), NOW()),
(17, 'WO-20260402-007', '排查大门传感器离线', '冷冻区大门传感器离线，检查线路连接是否松动，设备是否断电', 'HIGH', 'ALERT_FIX', 4, 3, 'PENDING', 1, 19, DATE_ADD(NOW(), INTERVAL 1 HOUR), NOW(), NOW()),
(18, 'WO-20260402-008', '月度设备校准 - A栋所有探头', '按照月度维护计划，校准A栋所有温湿度传感器', 'MEDIUM', 'INSPECTION', 2, NULL, 'PENDING', 1, 19, DATE_ADD(NOW(), INTERVAL 3 DAY), NOW(), NOW()),
(19, 'WO-20260402-009', '处理低温报警 - 冷藏区A-01', '冷藏区温度突然降到0℃，检查风门是否故障', 'URGENT', 'ALERT_FIX', 3, 2, 'PROCESSING', 1, 19, DATE_ADD(NOW(), INTERVAL 30 MINUTE), NOW(), NOW()),
(20, 'WO-20260402-010', '更换传感器电池 - 冷冻区A-05', '传感器上报低电量，到达现场更换电池', 'LOW', 'MAINTENANCE', 4, 5, 'PROCESSING', 1, 19, DATE_ADD(NOW(), INTERVAL 1 DAY), NOW(), NOW()),
(21, 'WO-20260402-011', '保温门密封条检查更换', '冷藏区B-02区保温门密封条老化漏冷，需要检查并计划更换', 'MEDIUM', 'MAINTENANCE', 3, NULL, 'COMPLETED', 1, 19, DATE_SUB(NOW(), INTERVAL 2 DAY), DATE_SUB(NOW(), INTERVAL 1 DAY), DATE_SUB(NOW(), INTERVAL 1 DAY));

SET FOREIGN_KEY_CHECKS = 1;
