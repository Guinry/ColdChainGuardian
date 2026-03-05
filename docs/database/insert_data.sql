SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- 清空现有数据（确保每次执行都是干净的基准数据）
TRUNCATE TABLE `ai_chat_messages`;
TRUNCATE TABLE `ai_chat_sessions`;
TRUNCATE TABLE `work_order_logs`;
TRUNCATE TABLE `alerts`;
TRUNCATE TABLE `work_orders`;
TRUNCATE TABLE `sensor_data`;
TRUNCATE TABLE `alert_configs`;
TRUNCATE TABLE `devices`;
TRUNCATE TABLE `warehouse_areas`;

-- ----------------------------
-- 2. 插入库区空间数据 (树状结构)
-- ----------------------------
INSERT INTO `warehouse_areas` (`id`, `parent_id`, `area_code`, `area_name`, `area_level`, `address`, `location_desc`, `temperature_threshold_min`, `temperature_threshold_max`, `humidity_threshold_min`, `humidity_threshold_max`, `area_path`) VALUES
(1, NULL, 'SITE-HD', '华东冷链总中心', 'SITE', '上海市浦东新区冷链路1号', '总部园区', -25.00, 25.00, 20.00, 80.00, '/1/'),
(2, 1, 'WH-A', 'A栋医药冷库', 'WAREHOUSE', '园区A栋', '主楼A座', -25.00, 8.00, 30.00, 70.00, '/1/2/'),
(3, 2, 'AREA-COOL', '2~8度冷藏区', 'AREA', NULL, 'A栋1层西侧', 2.00, 8.00, 35.00, 65.00, '/1/2/3/'),
(4, 2, 'AREA-FREEZE', '-20度冷冻区', 'AREA', NULL, 'A栋1层东侧', -22.00, -18.00, 30.00, 60.00, '/1/2/4/');

-- ----------------------------
-- 3. 插入设备数据
-- ----------------------------
INSERT INTO `devices` (`id`, `device_code`, `device_name`, `device_type`, `model`, `manufacturer`, `sn`, `area_id`, `location_desc`, `threshold_mode`, `online_status`, `latest_temp`, `latest_humi`, `latest_data_time`, `has_unresolved_alert`) VALUES
(1, 'TH-C-001', '冷藏区主探头', 'TEMP_HUM', 'Senso-Pro', '冷链智造', 'SN20241001', 3, '冷藏区A货架顶部', 'INHERIT', 1, 4.50, 45.00, NOW(), 0),
(2, 'TH-F-001', '冷冻区主探头', 'TEMP_HUM', 'Senso-Pro-Ultra', '冷链智造', 'SN20241002', 4, '冷冻区入口冷风机侧', 'INHERIT', 1, -12.50, 50.00, NOW(), 1),
(3, 'DOOR-001', '冷冻区主大门', 'DOOR_SENSOR', 'Door-Guard', '安防科技', 'SN20241003', 4, '冷冻区东大门', 'INHERIT', 0, NULL, NULL, DATE_SUB(NOW(), INTERVAL 5 HOUR), 0);

-- ----------------------------
-- 4. 插入告警配置
-- ----------------------------
INSERT INTO `alert_configs` (`device_id`, `warehouse_id`, `alert_type`, `threshold_value`, `alert_level`, `enabled`, `cool_down_minutes`) VALUES
(NULL, 4, 'TEMP_HIGH', -18.00, 'CRITICAL', 1, 5),
(NULL, 3, 'TEMP_HIGH', 8.00, 'HIGH', 1, 10);

-- ----------------------------
-- 5. 插入工单数据 (先插工单，告警才能关联它)
-- ----------------------------
INSERT INTO `work_orders` (`id`, `order_no`, `title`, `description`, `priority`, `order_type`, `warehouse_id`, `device_id`, `status`, `creator_id`, `assigned_to`, `created_time`) VALUES
(1, 'WO-20260304-001', '冷冻区温度严重超标检查', '探头 TH-F-001 监测到温度飙升至 -12.5℃，远超 -18℃ 阈值，请立即前往排查冷风机是否结霜或大门未关。', 'URGENT', 'ALERT_FIX', 4, 2, 'PROCESSING', 1, 3, DATE_SUB(NOW(), INTERVAL 2 HOUR)),
(2, 'WO-20260301-088', '三月第一周冷库例行巡检', '对A栋所有冷库的压缩机、保温门进行基础外观和噪音巡检。', 'LOW', 'INSPECTION', 2, NULL, 'COMPLETED', 2, 3, DATE_SUB(NOW(), INTERVAL 3 DAY));

-- ----------------------------
-- 6. 插入告警数据
-- ----------------------------
INSERT INTO `alerts` (`id`, `device_id`, `warehouse_id`, `alert_config_id`, `alert_type`, `alert_level`, `message`, `temperature`, `humidity`, `threshold_value`, `status`, `work_order_id`, `created_time`, `trigger_count`) VALUES
(1, 2, 4, 1, 'TEMP_HIGH', 'CRITICAL', '【紧急】冷冻区温度异常升高，当前值：-12.5℃，已超过设定上限 -18.0℃', -12.50, 50.00, -18.00, 'HANDLING', 1, DATE_SUB(NOW(), INTERVAL 2 HOUR), 3),
(2, 3, 4, NULL, 'DEVICE_OFFLINE', 'MEDIUM', '冷冻区大门传感器已离线超过 30 分钟', NULL, NULL, NULL, 'UNHANDLED', NULL, DATE_SUB(NOW(), INTERVAL 4 HOUR), 1);

-- ----------------------------
-- 7. 插入工单流转日志
-- ----------------------------
INSERT INTO `work_order_logs` (`work_order_id`, `operator_id`, `action`, `remark`, `created_time`) VALUES
(1, 1, 'CREATE', '系统基于超温告警自动创建紧急工单', DATE_SUB(NOW(), INTERVAL 120 MINUTE)),
(1, 2, 'ASSIGN', '站长已将工单指派给王师傅', DATE_SUB(NOW(), INTERVAL 110 MINUTE)),
(1, 3, 'START', '已到达现场，正在排查化霜定时器', DATE_SUB(NOW(), INTERVAL 30 MINUTE)),
(2, 3, 'RESOLVE', '巡检完毕，所有设备运转正常，无异响。', DATE_SUB(NOW(), INTERVAL 2 DAY));

-- ----------------------------
-- 8. 插入 AI 助手历史会话
-- ----------------------------
INSERT INTO `ai_chat_sessions` (`id`, `user_id`, `title`, `created_time`, `updated_time`) VALUES
(1, 1, '冷冻区异常升温诊断', DATE_SUB(NOW(), INTERVAL 1 HOUR), DATE_SUB(NOW(), INTERVAL 50 MINUTE)),
(2, 1, '二月能耗与报警统计', DATE_SUB(NOW(), INTERVAL 2 DAY), DATE_SUB(NOW(), INTERVAL 2 DAY));

-- ----------------------------
-- 9. 插入 AI 助手消息明细
-- ----------------------------
INSERT INTO `ai_chat_messages` (`session_id`, `role`, `content`, `attachment_type`, `attachment_id`, `created_time`) VALUES
(1, 'USER', '帮我看看 TH-F-001 这个设备现在为什么报严重超温？', 'DEVICE', 2, DATE_SUB(NOW(), INTERVAL 60 MINUTE)),
(1, 'ASSISTANT', '根据最新遥测数据，设备 **TH-F-001 (冷冻区主探头)** 当前温度为 **-12.5℃**，已严重突破 -18℃ 的安全红线。\n\n🔍 **可能原因推测：**\n1. **冷风机结霜严重**：化霜周期设置不合理导致风量急剧下降。\n2. **库门未关严**：我注意到同区域的门磁传感器(DOOR-001)目前处于离线状态，可能存在物理损坏或长时间敞开。\n\n建议您立刻联系现场运维人员核实库门状态！', NULL, NULL, DATE_SUB(NOW(), INTERVAL 59 MINUTE)),
(1, 'USER', '已经生成工单让王师傅去看了。', NULL, NULL, DATE_SUB(NOW(), INTERVAL 50 MINUTE)),
(1, 'ASSISTANT', '好的，已查询到关联的加急工单 `WO-20260304-001`，王师傅已于 30 分钟前到达现场并开始排查化霜定时器。我会持续为您监控该冷库的温度变化趋势。', NULL, NULL, DATE_SUB(NOW(), INTERVAL 50 MINUTE));

-- ----------------------------
-- 10. 插入温湿度历史曲线数据 (为趋势分析生成模拟点)
-- 冷藏区：平稳波动 (3℃ ~ 5℃)
-- 冷冻区：原本在 -19℃，在两小时前突然飙升到 -12℃
-- ----------------------------
-- 冷藏区 (正常)
INSERT INTO `sensor_data` (`device_id`, `temperature`, `humidity`, `data_time`) VALUES
(1, 4.2, 45.1, DATE_SUB(NOW(), INTERVAL 5 HOUR)),
(1, 4.3, 44.8, DATE_SUB(NOW(), INTERVAL 4 HOUR)),
(1, 4.6, 45.5, DATE_SUB(NOW(), INTERVAL 3 HOUR)),
(1, 4.4, 46.0, DATE_SUB(NOW(), INTERVAL 2 HOUR)),
(1, 4.5, 45.0, DATE_SUB(NOW(), INTERVAL 1 HOUR)),
(1, 4.5, 45.0, NOW());

-- 冷冻区 (发生异常)
INSERT INTO `sensor_data` (`device_id`, `temperature`, `humidity`, `data_time`) VALUES
(2, -19.5, 52.0, DATE_SUB(NOW(), INTERVAL 5 HOUR)),
(2, -19.2, 51.5, DATE_SUB(NOW(), INTERVAL 4 HOUR)),
(2, -18.8, 51.0, DATE_SUB(NOW(), INTERVAL 3 HOUR)),
(2, -14.5, 48.0, DATE_SUB(NOW(), INTERVAL 2 HOUR)), -- 开始飙升，触发告警
(2, -13.0, 49.5, DATE_SUB(NOW(), INTERVAL 1 HOUR)),
(2, -12.5, 50.0, NOW());

SET FOREIGN_KEY_CHECKS = 1;