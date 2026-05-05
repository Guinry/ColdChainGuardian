/*
 Navicat Premium Dump SQL

 Source Server         : localhost
 Source Server Type    : MySQL
 Source Server Version : 80042 (8.0.42)
 Source Host           : localhost:3306
 Source Schema         : coldchain_guardian

 Target Server Type    : MySQL
 Target Server Version : 80042 (8.0.42)
 File Encoding         : 65001

 Date: 03/04/2026 16:21:30
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for ai_chat_messages
-- ----------------------------
DROP TABLE IF EXISTS `ai_chat_messages`;
CREATE TABLE `ai_chat_messages`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `session_id` bigint NOT NULL COMMENT '所属会话ID (关联 ai_chat_sessions 表)',
  `role` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '角色类型：USER(用户提问), ASSISTANT(AI回答), SYSTEM(系统预设)',
  `content` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '消息内容(支持Markdown格式)',
  `attachment_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '关联附件类型(DEVICE/ALERT/WORK_ORDER/AREA)',
  `attachment_id` bigint NULL DEFAULT NULL COMMENT '关联附件的业务ID',
  `tokens_used` int NULL DEFAULT 0 COMMENT '消耗的Token数量(可选，用于后续统计大模型API成本)',
  `created_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '消息发送时间',
  `updated_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_session_time`(`session_id` ASC, `created_time` ASC) USING BTREE COMMENT '用于按时间顺序拉取某个会话的所有聊天记录',
  CONSTRAINT `fk_msg_session` FOREIGN KEY (`session_id`) REFERENCES `ai_chat_sessions` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 49 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = 'AI助手-消息明细表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of ai_chat_messages
-- ----------------------------
INSERT INTO `ai_chat_messages` VALUES (47, 8, 'USER', '你好', NULL, NULL, 0, NULL, NULL);
INSERT INTO `ai_chat_messages` VALUES (48, 8, 'ASSISTANT', '您好！我是您的专业冷链仓储安全AI参谋，若您有关于**冷链仓储温控管理、消杀防疫、货品存储安全、作业规范、风险防控**等相关问题，欢迎随时提问，我会为您提供专业简明的解答。', NULL, NULL, 0, NULL, NULL);

-- ----------------------------
-- Table structure for ai_chat_sessions
-- ----------------------------
DROP TABLE IF EXISTS `ai_chat_sessions`;
CREATE TABLE `ai_chat_sessions`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL COMMENT '所属用户ID (关联 users 表)',
  `title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '新对话' COMMENT '会话标题(由AI根据首轮对话自动生成总结)',
  `is_deleted` tinyint NOT NULL DEFAULT 0 COMMENT '是否已删除(1是 0否，用于前端历史列表的软删除)',
  `created_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '会话创建时间',
  `updated_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后对话时间(用于列表排序)',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_time`(`user_id` ASC, `updated_time` DESC) USING BTREE COMMENT '用于快速拉取某用户的历史会话列表'
) ENGINE = InnoDB AUTO_INCREMENT = 9 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = 'AI助手-会话表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of ai_chat_sessions
-- ----------------------------
INSERT INTO `ai_chat_sessions` VALUES (5, 1, 'B1区温度异常分析', 0, '2026-04-01 22:07:09', '2026-04-02 22:07:09');
INSERT INTO `ai_chat_sessions` VALUES (6, 1, '本周告警趋势分析', 0, '2026-04-02 16:07:09', '2026-04-02 22:07:09');
INSERT INTO `ai_chat_sessions` VALUES (8, 1, '你好', 0, '2026-04-02 22:18:59', '2026-04-02 22:19:00');

-- ----------------------------
-- Table structure for alert_configs
-- ----------------------------
DROP TABLE IF EXISTS `alert_configs`;
CREATE TABLE `alert_configs`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `device_id` bigint NULL DEFAULT NULL COMMENT '设备ID(为空表示全局配置)',
  `warehouse_id` bigint NULL DEFAULT NULL COMMENT '库区ID(为空表示全局配置)',
  `alert_type` enum('TEMP_HIGH','TEMP_LOW','HUMI_HIGH','HUMI_LOW','DEVICE_OFFLINE') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '告警类型',
  `threshold_value` decimal(10, 2) NULL DEFAULT NULL COMMENT '阈值',
  `alert_level` enum('LOW','MEDIUM','HIGH','CRITICAL') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'MEDIUM' COMMENT '告警级别',
  `enabled` tinyint NULL DEFAULT 1 COMMENT '是否启用',
  `notification_methods` json NULL COMMENT '通知方式(邮件、短信、APP推送)',
  `cool_down_minutes` int NULL DEFAULT 5 COMMENT '冷却时间(分钟)',
  `created_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_cfg`(`device_id` ASC, `warehouse_id` ASC, `alert_type` ASC) USING BTREE,
  INDEX `idx_scope_type`(`device_id` ASC, `warehouse_id` ASC, `alert_type` ASC) USING BTREE,
  INDEX `idx_enabled_type`(`enabled` ASC, `alert_type` ASC) USING BTREE,
  INDEX `fk_cfg_warehouse_area`(`warehouse_id` ASC) USING BTREE,
  CONSTRAINT `fk_cfg_device` FOREIGN KEY (`device_id`) REFERENCES `devices` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_cfg_warehouse_area` FOREIGN KEY (`warehouse_id`) REFERENCES `warehouse_areas` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '告警配置' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of alert_configs
-- ----------------------------

-- ----------------------------
-- Table structure for alerts
-- ----------------------------
DROP TABLE IF EXISTS `alerts`;
CREATE TABLE `alerts`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `device_id` bigint NOT NULL COMMENT '设备ID',
  `warehouse_id` bigint NOT NULL COMMENT '库区ID',
  `alert_config_id` bigint NULL DEFAULT NULL COMMENT '触发的告警配置ID',
  `alert_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '告警类型(TEMP_HIGH, DEVICE_OFFLINE等)',
  `alert_level` enum('LOW','MEDIUM','HIGH','CRITICAL') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '告警级别',
  `message` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '告警消息',
  `temperature` decimal(5, 2) NULL DEFAULT NULL COMMENT '告警时温度',
  `humidity` decimal(5, 2) NULL DEFAULT NULL COMMENT '告警时湿度',
  `threshold_value` decimal(10, 2) NULL DEFAULT NULL COMMENT '触发阈值',
  `status` enum('UNHANDLED','HANDLING','RESOLVED','IGNORED') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'UNHANDLED' COMMENT '处理状态',
  `work_order_id` bigint NULL DEFAULT NULL COMMENT '关联的工单ID（若已转工单）',
  `handler_user_id` bigint NULL DEFAULT NULL COMMENT '处理人ID(快速处理时使用)',
  `handle_time` timestamp NULL DEFAULT NULL COMMENT '处理/转工单时间',
  `handle_remark` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '处理备注',
  `created_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '告警发生时间',
  `updated_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `first_time` timestamp NULL DEFAULT NULL COMMENT '首次触发时间',
  `last_time` timestamp NULL DEFAULT NULL COMMENT '最后一次触发时间',
  `trigger_count` int NULL DEFAULT 1 COMMENT '触发次数',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE,
  INDEX `idx_level`(`alert_level` ASC) USING BTREE,
  INDEX `fk_alerts_work_order`(`work_order_id` ASC) USING BTREE,
  INDEX `idx_status_level_time`(`status` ASC, `alert_level` ASC, `created_time` DESC) USING BTREE,
  INDEX `idx_device_id_status`(`device_id` ASC, `status` ASC) USING BTREE,
  CONSTRAINT `fk_alerts_device` FOREIGN KEY (`device_id`) REFERENCES `devices` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_alerts_work_order` FOREIGN KEY (`work_order_id`) REFERENCES `work_orders` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 623 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '告警记录表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of alerts
-- ----------------------------
INSERT INTO `alerts` VALUES (573, 1, 8, NULL, 'TEMP_HIGH', 'HIGH', 'A1区温度探头检测到温度过高', 2.80, 45.00, 0.00, 'RESOLVED', NULL, NULL, NULL, NULL, '2026-04-02 22:07:09', '2026-04-02 22:07:09', NULL, NULL, 1);
INSERT INTO `alerts` VALUES (574, 1, 8, NULL, 'TEMP_HIGH', 'MEDIUM', 'A1区温度探头检测到温度偏高', 1.20, 42.00, 0.00, 'RESOLVED', NULL, NULL, NULL, NULL, '2026-04-02 22:07:09', '2026-04-02 22:07:09', NULL, NULL, 1);
INSERT INTO `alerts` VALUES (575, 4, 10, NULL, 'TEMP_HIGH', 'CRITICAL', 'B1区温度探头检测到温度严重过高', 9.50, 52.00, 8.00, 'HANDLING', NULL, NULL, NULL, NULL, '2026-04-02 22:07:09', '2026-04-02 22:07:09', NULL, NULL, 1);
INSERT INTO `alerts` VALUES (576, 5, 10, NULL, 'TEMP_HIGH', 'MEDIUM', 'B1区温度探头检测到温度偏高', 8.20, 55.00, 8.00, 'UNHANDLED', NULL, NULL, NULL, NULL, '2026-04-02 22:07:09', '2026-04-02 22:07:09', NULL, NULL, 1);
INSERT INTO `alerts` VALUES (577, 5, 10, NULL, 'TEMP_HIGH', 'LOW', 'B1区温度探头检测到温度轻微偏高', 8.50, 56.00, 8.00, 'UNHANDLED', NULL, NULL, NULL, NULL, '2026-04-02 22:07:09', '2026-04-02 22:07:09', NULL, NULL, 1);
INSERT INTO `alerts` VALUES (578, 2, 9, NULL, 'TEMP_LOW', 'HIGH', 'A2区温度探头检测到温度过低', -28.00, 40.00, -25.00, 'RESOLVED', NULL, NULL, NULL, NULL, '2026-04-02 22:07:09', '2026-04-02 22:07:09', NULL, NULL, 1);
INSERT INTO `alerts` VALUES (579, 10, 1, NULL, 'TEMP_LOW', 'MEDIUM', '冷链车检测到温度过低', 2.00, 50.00, 2.00, 'UNHANDLED', NULL, NULL, NULL, NULL, '2026-04-02 22:07:09', '2026-04-02 22:07:09', NULL, NULL, 1);
INSERT INTO `alerts` VALUES (580, 6, 11, NULL, 'TEMP_LOW', 'LOW', 'B1区温度探头检测到温度轻微偏低', -1.00, 42.00, 2.00, 'IGNORED', NULL, NULL, NULL, NULL, '2026-04-02 22:07:09', '2026-04-02 22:07:09', NULL, NULL, 1);
INSERT INTO `alerts` VALUES (581, 1, 8, NULL, 'HUMI_HIGH', 'MEDIUM', 'A1区温度探头检测到湿度过高', -1.00, 72.00, 60.00, 'RESOLVED', NULL, NULL, NULL, NULL, '2026-04-02 22:07:09', '2026-04-02 22:07:09', NULL, NULL, 1);
INSERT INTO `alerts` VALUES (582, 3, 10, NULL, 'HUMI_HIGH', 'LOW', 'B1区门磁传感器检测到湿度偏高', NULL, 71.00, 65.00, 'UNHANDLED', NULL, NULL, NULL, NULL, '2026-04-02 22:07:09', '2026-04-02 22:07:09', NULL, NULL, 1);
INSERT INTO `alerts` VALUES (583, 5, 10, NULL, 'HUMI_HIGH', 'MEDIUM', 'B1区温度探头检测到湿度过高', 6.00, 78.00, 65.00, 'HANDLING', NULL, NULL, NULL, NULL, '2026-04-02 22:07:09', '2026-04-02 22:07:09', NULL, NULL, 1);
INSERT INTO `alerts` VALUES (584, 7, 10, NULL, 'HUMI_HIGH', 'LOW', 'B1区门磁传感器检测到湿度轻微偏高', NULL, 71.00, 65.00, 'UNHANDLED', NULL, NULL, NULL, NULL, '2026-04-02 22:07:09', '2026-04-02 22:07:09', NULL, NULL, 1);
INSERT INTO `alerts` VALUES (585, 2, 9, NULL, 'HUMI_LOW', 'MEDIUM', 'A2区温度探头检测到湿度过低', -18.00, 25.00, 30.00, 'RESOLVED', NULL, NULL, NULL, NULL, '2026-04-02 22:07:09', '2026-04-02 22:07:09', NULL, NULL, 1);
INSERT INTO `alerts` VALUES (586, 5, 10, NULL, 'HUMI_LOW', 'LOW', 'B1区温度探头检测到湿度偏低', 5.50, 28.00, 40.00, 'UNHANDLED', NULL, NULL, NULL, NULL, '2026-04-02 22:07:09', '2026-04-02 22:07:09', NULL, NULL, 1);
INSERT INTO `alerts` VALUES (587, 8, 11, NULL, 'HUMI_LOW', 'LOW', 'B1区门磁传感器检测到湿度偏低', NULL, 29.00, 40.00, 'IGNORED', NULL, NULL, NULL, NULL, '2026-04-02 22:07:09', '2026-04-02 22:07:09', NULL, NULL, 1);
INSERT INTO `alerts` VALUES (588, 3, 10, NULL, 'DEVICE_OFFLINE', 'HIGH', 'B1区三号传感器已离线', NULL, NULL, NULL, 'UNHANDLED', NULL, NULL, NULL, NULL, '2026-04-02 22:07:09', '2026-04-02 22:07:09', NULL, NULL, 1);
INSERT INTO `alerts` VALUES (589, 4, 10, NULL, 'DEVICE_OFFLINE', 'MEDIUM', 'B1区二号传感器已离线', NULL, NULL, NULL, 'HANDLING', NULL, NULL, NULL, NULL, '2026-04-02 22:07:09', '2026-04-02 22:07:09', NULL, NULL, 1);
INSERT INTO `alerts` VALUES (590, 10, 1, NULL, 'DEVICE_OFFLINE', 'MEDIUM', '冷链一号车已离线', NULL, NULL, NULL, 'UNHANDLED', NULL, NULL, NULL, NULL, '2026-04-02 22:07:09', '2026-04-02 22:07:09', NULL, NULL, 1);
INSERT INTO `alerts` VALUES (591, 9, 4, NULL, 'DEVICE_OFFLINE', 'LOW', '超低温冰柜通讯异常', NULL, NULL, NULL, 'RESOLVED', NULL, NULL, NULL, NULL, '2026-04-02 22:07:09', '2026-04-02 22:07:09', NULL, NULL, 1);
INSERT INTO `alerts` VALUES (592, 8, 11, NULL, 'DEVICE_OFFLINE', 'LOW', 'B1区一号门磁短暂离线', NULL, NULL, NULL, 'RESOLVED', NULL, NULL, NULL, NULL, '2026-04-02 22:07:09', '2026-04-02 22:07:09', NULL, NULL, 1);

-- ----------------------------
-- Table structure for devices
-- ----------------------------
DROP TABLE IF EXISTS `devices`;
CREATE TABLE `devices`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `device_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '设备编码（唯一）',
  `device_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '设备名称',
  `device_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '设备类型（TEMP_HUM / FREEZER / VEHICLE / DOOR ...）',
  `model` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '型号',
  `manufacturer` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '厂商',
  `sn` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '序列号',
  `firmware_version` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '固件版本',
  `area_id` bigint NOT NULL COMMENT '所属库区ID(warehouse_areas.id)',
  `location_desc` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '设备位置描述（如A栋2层东区/货架3）',
  `threshold_mode` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'INHERIT' COMMENT '阈值模式：INHERIT/OVERRIDE',
  `temperature_threshold_min` decimal(5, 2) NULL DEFAULT NULL COMMENT '设备温度下限(覆盖时生效)',
  `temperature_threshold_max` decimal(5, 2) NULL DEFAULT NULL COMMENT '设备温度上限(覆盖时生效)',
  `humidity_threshold_min` decimal(5, 2) NULL DEFAULT NULL COMMENT '设备湿度下限(覆盖时生效)',
  `humidity_threshold_max` decimal(5, 2) NULL DEFAULT NULL COMMENT '设备湿度上限(覆盖时生效)',
  `alarm_enabled` tinyint NOT NULL DEFAULT 1 COMMENT '是否启用告警(1是0否)',
  `enabled` tinyint NOT NULL DEFAULT 1 COMMENT '启用状态(1启用0禁用)',
  `online_status` tinyint NOT NULL DEFAULT 0 COMMENT '在线状态(1在线0离线)',
  `last_seen_time` timestamp NULL DEFAULT NULL COMMENT '最后上报/心跳时间',
  `extra` json NULL COMMENT '扩展信息(JSON)：如安装参数/通讯方式/IMEI等',
  `created_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `creator_id` bigint NULL DEFAULT NULL,
  `updater_id` bigint NULL DEFAULT NULL,
  `latest_temp` decimal(5, 2) NULL DEFAULT NULL COMMENT '最新温度',
  `latest_humi` decimal(5, 2) NULL DEFAULT NULL COMMENT '最新湿度',
  `latest_data_time` timestamp NULL DEFAULT NULL COMMENT '最新数据上报时间',
  `has_unresolved_alert` tinyint NULL DEFAULT 0 COMMENT '是否有未处理告警(1是0否)',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `device_code`(`device_code` ASC) USING BTREE,
  INDEX `idx_area`(`area_id` ASC) USING BTREE,
  INDEX `idx_type`(`device_type` ASC) USING BTREE,
  INDEX `idx_enabled`(`enabled` ASC) USING BTREE,
  INDEX `idx_online`(`online_status` ASC) USING BTREE,
  INDEX `idx_last_seen`(`last_seen_time` ASC) USING BTREE,
  INDEX `idx_area_id`(`area_id` ASC) USING BTREE,
  CONSTRAINT `fk_device_area` FOREIGN KEY (`area_id`) REFERENCES `warehouse_areas` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 24 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '设备表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of devices
-- ----------------------------
INSERT INTO `devices` VALUES (14, 'TH-A1-001', 'A1区温度湿度传感器 1号', 'TEMP_HUM', 'TH-200', '海尔生物医疗', 'SN20240001', NULL, 16, 'A1区西北角', 'INHERIT', NULL, NULL, NULL, NULL, 1, 1, 1, '2026-04-02 22:07:09', NULL, '2026-04-02 22:07:09', '2026-04-03 16:19:23', NULL, NULL, -2.50, 45.50, '2026-04-02 22:07:09', 0);
INSERT INTO `devices` VALUES (15, 'TH-A1-002', 'A1区温度湿度传感器 2号', 'TEMP_HUM', 'TH-200', '海尔生物医疗', 'SN20240002', NULL, 16, 'A1区东南角', 'INHERIT', NULL, NULL, NULL, NULL, 1, 1, 1, '2026-04-02 22:07:09', NULL, '2026-04-02 22:07:09', '2026-04-03 16:19:18', NULL, NULL, -1.80, 42.30, '2026-04-02 22:07:09', 0);
INSERT INTO `devices` VALUES (16, 'TH-A2-001', 'A2区温度湿度传感器 1号', 'TEMP_HUM', 'TH-200', '海尔生物医疗', 'SN20240003', NULL, 17, 'A2区东北角', 'INHERIT', NULL, NULL, NULL, NULL, 1, 1, 1, '2026-04-02 22:07:09', NULL, '2026-04-02 22:07:09', '2026-04-03 16:19:14', NULL, NULL, -3.20, 48.10, '2026-04-02 22:07:09', 0);
INSERT INTO `devices` VALUES (17, 'TH-B1-001', 'B1区温度湿度传感器 1号', 'TEMP_HUM', 'TH-200', '海尔生物医疗', 'SN20240004', NULL, 18, 'B1区中央', 'INHERIT', NULL, NULL, NULL, NULL, 1, 1, 1, '2026-04-02 22:07:09', NULL, '2026-04-02 22:07:09', '2026-04-03 16:19:11', NULL, NULL, 5.60, 52.80, '2026-04-02 22:07:09', 0);
INSERT INTO `devices` VALUES (18, 'TH-B1-002', 'B1区温度湿度传感器 2号', 'TEMP_HUM', 'TH-200', '海尔生物医疗', 'SN20240005', NULL, 18, 'B1区门口', 'INHERIT', NULL, NULL, NULL, NULL, 1, 1, 1, '2026-04-02 22:07:09', NULL, '2026-04-02 22:07:09', '2026-04-03 16:19:07', NULL, NULL, 6.20, 55.30, '2026-04-02 22:07:09', 0);
INSERT INTO `devices` VALUES (19, 'TH-B1-003', 'B1区温度湿度传感器 3号', 'TEMP_HUM', 'TH-200', '海尔生物医疗', 'SN20240006', NULL, 18, 'B1区西侧', 'INHERIT', NULL, NULL, NULL, NULL, 1, 1, 0, '2026-04-02 22:07:09', NULL, '2026-04-02 22:07:09', '2026-04-03 16:19:03', NULL, NULL, NULL, NULL, NULL, 0);
INSERT INTO `devices` VALUES (20, 'DOOR-001', 'A1区入口门磁传感器', 'DOOR_SENSOR', 'DS-100', '海康威视', 'SN20240007', NULL, 16, 'A1区主入口', 'INHERIT', NULL, NULL, NULL, NULL, 1, 1, 1, '2026-04-02 22:07:09', NULL, '2026-04-02 22:07:09', '2026-04-03 16:19:00', NULL, NULL, NULL, NULL, '2026-04-02 22:07:09', 0);
INSERT INTO `devices` VALUES (21, 'DOOR-002', 'B1区入口门磁传感器', 'DOOR_SENSOR', 'DS-100', '海康威视', 'SN20240008', NULL, 18, 'B1区主入口', 'INHERIT', NULL, NULL, NULL, NULL, 1, 1, 1, '2026-04-02 22:07:09', NULL, '2026-04-02 22:07:09', '2026-04-03 16:18:56', NULL, NULL, NULL, NULL, '2026-04-02 22:07:09', 0);
INSERT INTO `devices` VALUES (22, 'FREEZER-001', '超低温保存冰柜', 'FREEZER', 'UF-500', '赛默飞世尔', 'SN20240009', NULL, 12, 'A栋实验区', 'OVERRIDE', -80.00, -60.00, 20.00, 40.00, 1, 1, 1, '2026-04-02 22:07:09', NULL, '2026-04-02 22:07:09', '2026-04-03 16:18:53', NULL, NULL, -70.50, 35.00, '2026-04-02 22:07:09', 0);
INSERT INTO `devices` VALUES (23, 'VEHICLE-001', '冷链配送车 001', 'VEHICLE', 'VT-300', '中集集团', 'SN20240010', NULL, 9, '停车场A区', 'INHERIT', NULL, NULL, NULL, NULL, 1, 1, 0, '2026-04-02 22:07:09', NULL, '2026-04-02 22:07:09', '2026-04-03 16:18:47', NULL, NULL, NULL, NULL, NULL, 0);

-- ----------------------------
-- Table structure for sensor_data
-- ----------------------------
DROP TABLE IF EXISTS `sensor_data`;
CREATE TABLE `sensor_data`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `device_id` bigint NOT NULL COMMENT '设备ID',
  `temperature` decimal(5, 2) NULL DEFAULT NULL COMMENT '温度(℃)',
  `humidity` decimal(5, 2) NULL DEFAULT NULL COMMENT '湿度(%)',
  `data_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '数据时间戳',
  `battery_level` decimal(5, 2) NULL DEFAULT NULL COMMENT '电池电量(%)',
  `signal_strength` int NULL DEFAULT NULL COMMENT '信号强度',
  `raw_data` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '原始数据',
  `created_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_device_time`(`device_id` ASC, `data_time` ASC) USING BTREE,
  INDEX `idx_time`(`data_time` ASC) USING BTREE,
  INDEX `idx_device_time_desc`(`device_id` ASC, `data_time` DESC) USING BTREE,
  CONSTRAINT `fk_sensor_device` FOREIGN KEY (`device_id`) REFERENCES `devices` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 74993 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '温湿度原始上报数据' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sensor_data
-- ----------------------------
INSERT INTO `sensor_data` VALUES (74893, 1, -2.30, 44.20, '2026-03-26 22:07:09', NULL, NULL, NULL, '2026-04-02 22:07:09', '2026-04-02 22:07:09');
INSERT INTO `sensor_data` VALUES (74894, 1, -3.10, 42.80, '2026-03-27 04:07:09', NULL, NULL, NULL, '2026-04-02 22:07:09', '2026-04-02 22:07:09');
INSERT INTO `sensor_data` VALUES (74895, 1, -1.80, 46.50, '2026-03-27 10:07:09', NULL, NULL, NULL, '2026-04-02 22:07:09', '2026-04-02 22:07:09');
INSERT INTO `sensor_data` VALUES (74896, 1, -2.70, 43.90, '2026-03-27 16:07:09', NULL, NULL, NULL, '2026-04-02 22:07:09', '2026-04-02 22:07:09');
INSERT INTO `sensor_data` VALUES (74897, 1, -2.00, 45.10, '2026-03-27 22:07:09', NULL, NULL, NULL, '2026-04-02 22:07:09', '2026-04-02 22:07:09');
INSERT INTO `sensor_data` VALUES (74898, 1, -3.20, 41.70, '2026-03-28 04:07:09', NULL, NULL, NULL, '2026-04-02 22:07:09', '2026-04-02 22:07:09');
INSERT INTO `sensor_data` VALUES (74899, 1, -1.50, 47.20, '2026-03-28 10:07:09', NULL, NULL, NULL, '2026-04-02 22:07:09', '2026-04-02 22:07:09');
INSERT INTO `sensor_data` VALUES (74900, 1, -2.80, 43.30, '2026-03-28 16:07:09', NULL, NULL, NULL, '2026-04-02 22:07:09', '2026-04-02 22:07:09');
INSERT INTO `sensor_data` VALUES (74901, 1, -2.20, 45.60, '2026-03-28 22:07:09', NULL, NULL, NULL, '2026-04-02 22:07:09', '2026-04-02 22:07:09');
INSERT INTO `sensor_data` VALUES (74902, 1, -3.00, 42.10, '2026-03-29 04:07:09', NULL, NULL, NULL, '2026-04-02 22:07:09', '2026-04-02 22:07:09');
INSERT INTO `sensor_data` VALUES (74903, 1, -1.90, 46.80, '2026-03-29 10:07:09', NULL, NULL, NULL, '2026-04-02 22:07:09', '2026-04-02 22:07:09');
INSERT INTO `sensor_data` VALUES (74904, 1, -2.60, 44.50, '2026-03-29 16:07:09', NULL, NULL, NULL, '2026-04-02 22:07:09', '2026-04-02 22:07:09');
INSERT INTO `sensor_data` VALUES (74905, 1, -2.40, 43.80, '2026-03-29 22:07:09', NULL, NULL, NULL, '2026-04-02 22:07:09', '2026-04-02 22:07:09');
INSERT INTO `sensor_data` VALUES (74906, 1, -2.90, 41.90, '2026-03-30 04:07:09', NULL, NULL, NULL, '2026-04-02 22:07:09', '2026-04-02 22:07:09');
INSERT INTO `sensor_data` VALUES (74907, 1, -1.70, 46.30, '2026-03-30 10:07:09', NULL, NULL, NULL, '2026-04-02 22:07:09', '2026-04-02 22:07:09');
INSERT INTO `sensor_data` VALUES (74908, 1, -2.50, 44.10, '2026-03-30 16:07:09', NULL, NULL, NULL, '2026-04-02 22:07:09', '2026-04-02 22:07:09');
INSERT INTO `sensor_data` VALUES (74909, 1, -2.10, 45.90, '2026-03-30 22:07:09', NULL, NULL, NULL, '2026-04-02 22:07:09', '2026-04-02 22:07:09');
INSERT INTO `sensor_data` VALUES (74910, 1, -3.30, 42.50, '2026-03-31 04:07:09', NULL, NULL, NULL, '2026-04-02 22:07:09', '2026-04-02 22:07:09');
INSERT INTO `sensor_data` VALUES (74911, 1, -1.60, 47.00, '2026-03-31 10:07:09', NULL, NULL, NULL, '2026-04-02 22:07:09', '2026-04-02 22:07:09');
INSERT INTO `sensor_data` VALUES (74912, 1, -2.70, 43.70, '2026-03-31 16:07:09', NULL, NULL, NULL, '2026-04-02 22:07:09', '2026-04-02 22:07:09');
INSERT INTO `sensor_data` VALUES (74913, 1, -2.30, 45.30, '2026-03-31 22:07:09', NULL, NULL, NULL, '2026-04-02 22:07:09', '2026-04-02 22:07:09');
INSERT INTO `sensor_data` VALUES (74914, 1, -2.80, 42.60, '2026-04-01 04:07:09', NULL, NULL, NULL, '2026-04-02 22:07:09', '2026-04-02 22:07:09');
INSERT INTO `sensor_data` VALUES (74915, 1, -1.90, 46.10, '2026-04-01 10:07:09', NULL, NULL, NULL, '2026-04-02 22:07:09', '2026-04-02 22:07:09');
INSERT INTO `sensor_data` VALUES (74916, 1, -2.40, 44.80, '2026-04-01 16:07:09', NULL, NULL, NULL, '2026-04-02 22:07:09', '2026-04-02 22:07:09');
INSERT INTO `sensor_data` VALUES (74917, 1, -2.50, 45.50, '2026-04-02 22:07:09', NULL, NULL, NULL, '2026-04-02 22:07:09', '2026-04-02 22:07:09');
INSERT INTO `sensor_data` VALUES (74918, 2, -1.50, 41.20, '2026-03-26 22:07:09', NULL, NULL, NULL, '2026-04-02 22:07:09', '2026-04-02 22:07:09');
INSERT INTO `sensor_data` VALUES (74919, 2, -2.20, 39.80, '2026-03-27 04:07:09', NULL, NULL, NULL, '2026-04-02 22:07:09', '2026-04-02 22:07:09');
INSERT INTO `sensor_data` VALUES (74920, 2, -0.90, 43.50, '2026-03-27 10:07:09', NULL, NULL, NULL, '2026-04-02 22:07:09', '2026-04-02 22:07:09');
INSERT INTO `sensor_data` VALUES (74921, 2, -1.90, 40.90, '2026-03-27 16:07:09', NULL, NULL, NULL, '2026-04-02 22:07:09', '2026-04-02 22:07:09');
INSERT INTO `sensor_data` VALUES (74922, 2, -1.60, 42.10, '2026-03-27 22:07:09', NULL, NULL, NULL, '2026-04-02 22:07:09', '2026-04-02 22:07:09');
INSERT INTO `sensor_data` VALUES (74923, 2, -2.40, 38.70, '2026-03-28 04:07:09', NULL, NULL, NULL, '2026-04-02 22:07:09', '2026-04-02 22:07:09');
INSERT INTO `sensor_data` VALUES (74924, 2, -1.20, 44.20, '2026-03-28 10:07:09', NULL, NULL, NULL, '2026-04-02 22:07:09', '2026-04-02 22:07:09');
INSERT INTO `sensor_data` VALUES (74925, 2, -2.00, 40.30, '2026-03-28 16:07:09', NULL, NULL, NULL, '2026-04-02 22:07:09', '2026-04-02 22:07:09');
INSERT INTO `sensor_data` VALUES (74926, 2, -1.70, 42.60, '2026-03-28 22:07:09', NULL, NULL, NULL, '2026-04-02 22:07:09', '2026-04-02 22:07:09');
INSERT INTO `sensor_data` VALUES (74927, 2, -2.30, 39.10, '2026-03-29 04:07:09', NULL, NULL, NULL, '2026-04-02 22:07:09', '2026-04-02 22:07:09');
INSERT INTO `sensor_data` VALUES (74928, 2, -1.10, 43.80, '2026-03-29 10:07:09', NULL, NULL, NULL, '2026-04-02 22:07:09', '2026-04-02 22:07:09');
INSERT INTO `sensor_data` VALUES (74929, 2, -1.80, 41.50, '2026-03-29 16:07:09', NULL, NULL, NULL, '2026-04-02 22:07:09', '2026-04-02 22:07:09');
INSERT INTO `sensor_data` VALUES (74930, 2, -1.50, 40.80, '2026-03-29 22:07:09', NULL, NULL, NULL, '2026-04-02 22:07:09', '2026-04-02 22:07:09');
INSERT INTO `sensor_data` VALUES (74931, 2, -2.10, 38.90, '2026-03-30 04:07:09', NULL, NULL, NULL, '2026-04-02 22:07:09', '2026-04-02 22:07:09');
INSERT INTO `sensor_data` VALUES (74932, 2, -0.80, 43.30, '2026-03-30 10:07:09', NULL, NULL, NULL, '2026-04-02 22:07:09', '2026-04-02 22:07:09');
INSERT INTO `sensor_data` VALUES (74933, 2, -1.80, 41.10, '2026-03-30 16:07:09', NULL, NULL, NULL, '2026-04-02 22:07:09', '2026-04-02 22:07:09');
INSERT INTO `sensor_data` VALUES (74934, 2, -1.40, 42.80, '2026-03-30 22:07:09', NULL, NULL, NULL, '2026-04-02 22:07:09', '2026-04-02 22:07:09');
INSERT INTO `sensor_data` VALUES (74935, 2, -2.50, 39.50, '2026-03-31 04:07:09', NULL, NULL, NULL, '2026-04-02 22:07:09', '2026-04-02 22:07:09');
INSERT INTO `sensor_data` VALUES (74936, 2, -1.00, 44.00, '2026-03-31 10:07:09', NULL, NULL, NULL, '2026-04-02 22:07:09', '2026-04-02 22:07:09');
INSERT INTO `sensor_data` VALUES (74937, 2, -1.90, 40.70, '2026-03-31 16:07:09', NULL, NULL, NULL, '2026-04-02 22:07:09', '2026-04-02 22:07:09');
INSERT INTO `sensor_data` VALUES (74938, 2, -1.60, 42.30, '2026-03-31 22:07:09', NULL, NULL, NULL, '2026-04-02 22:07:09', '2026-04-02 22:07:09');
INSERT INTO `sensor_data` VALUES (74939, 2, -2.10, 39.60, '2026-04-01 04:07:09', NULL, NULL, NULL, '2026-04-02 22:07:09', '2026-04-02 22:07:09');
INSERT INTO `sensor_data` VALUES (74940, 2, -1.20, 43.10, '2026-04-01 10:07:09', NULL, NULL, NULL, '2026-04-02 22:07:09', '2026-04-02 22:07:09');
INSERT INTO `sensor_data` VALUES (74941, 2, -1.80, 41.80, '2026-04-01 16:07:09', NULL, NULL, NULL, '2026-04-02 22:07:09', '2026-04-02 22:07:09');
INSERT INTO `sensor_data` VALUES (74942, 2, -1.80, 42.30, '2026-04-02 22:07:09', NULL, NULL, NULL, '2026-04-02 22:07:09', '2026-04-02 22:07:09');
INSERT INTO `sensor_data` VALUES (74943, 4, 5.20, 51.30, '2026-03-26 22:07:09', NULL, NULL, NULL, '2026-04-02 22:07:09', '2026-04-02 22:07:09');
INSERT INTO `sensor_data` VALUES (74944, 4, 6.80, 48.70, '2026-03-27 04:07:09', NULL, NULL, NULL, '2026-04-02 22:07:09', '2026-04-02 22:07:09');
INSERT INTO `sensor_data` VALUES (74945, 4, 4.10, 55.20, '2026-03-27 10:07:09', NULL, NULL, NULL, '2026-04-02 22:07:09', '2026-04-02 22:07:09');
INSERT INTO `sensor_data` VALUES (74946, 4, 6.30, 47.50, '2026-03-27 16:07:09', NULL, NULL, NULL, '2026-04-02 22:07:09', '2026-04-02 22:07:09');
INSERT INTO `sensor_data` VALUES (74947, 4, 5.50, 50.80, '2026-03-27 22:07:09', NULL, NULL, NULL, '2026-04-02 22:07:09', '2026-04-02 22:07:09');
INSERT INTO `sensor_data` VALUES (74948, 4, 7.20, 45.90, '2026-03-28 04:07:09', NULL, NULL, NULL, '2026-04-02 22:07:09', '2026-04-02 22:07:09');
INSERT INTO `sensor_data` VALUES (74949, 4, 3.80, 56.80, '2026-03-28 10:07:09', NULL, NULL, NULL, '2026-04-02 22:07:09', '2026-04-02 22:07:09');
INSERT INTO `sensor_data` VALUES (74950, 4, 6.10, 49.20, '2026-03-28 16:07:09', NULL, NULL, NULL, '2026-04-02 22:07:09', '2026-04-02 22:07:09');
INSERT INTO `sensor_data` VALUES (74951, 4, 5.30, 52.10, '2026-03-28 22:07:09', NULL, NULL, NULL, '2026-04-02 22:07:09', '2026-04-02 22:07:09');
INSERT INTO `sensor_data` VALUES (74952, 4, 7.50, 46.30, '2026-03-29 04:07:09', NULL, NULL, NULL, '2026-04-02 22:07:09', '2026-04-02 22:07:09');
INSERT INTO `sensor_data` VALUES (74953, 4, 4.20, 57.50, '2026-03-29 10:07:09', NULL, NULL, NULL, '2026-04-02 22:07:09', '2026-04-02 22:07:09');
INSERT INTO `sensor_data` VALUES (74954, 4, 5.90, 50.10, '2026-03-29 16:07:09', NULL, NULL, NULL, '2026-04-02 22:07:09', '2026-04-02 22:07:09');
INSERT INTO `sensor_data` VALUES (74955, 4, 5.10, 51.60, '2026-03-29 22:07:09', NULL, NULL, NULL, '2026-04-02 22:07:09', '2026-04-02 22:07:09');
INSERT INTO `sensor_data` VALUES (74956, 4, 6.80, 47.80, '2026-03-30 04:07:09', NULL, NULL, NULL, '2026-04-02 22:07:09', '2026-04-02 22:07:09');
INSERT INTO `sensor_data` VALUES (74957, 4, 3.50, 58.20, '2026-03-30 10:07:09', NULL, NULL, NULL, '2026-04-02 22:07:09', '2026-04-02 22:07:09');
INSERT INTO `sensor_data` VALUES (74958, 4, 5.70, 49.50, '2026-03-30 16:07:09', NULL, NULL, NULL, '2026-04-02 22:07:09', '2026-04-02 22:07:09');
INSERT INTO `sensor_data` VALUES (74959, 4, 5.40, 52.90, '2026-03-30 22:07:09', NULL, NULL, NULL, '2026-04-02 22:07:09', '2026-04-02 22:07:09');
INSERT INTO `sensor_data` VALUES (74960, 4, 7.80, 44.60, '2026-03-31 04:07:09', NULL, NULL, NULL, '2026-04-02 22:07:09', '2026-04-02 22:07:09');
INSERT INTO `sensor_data` VALUES (74961, 4, 3.90, 55.70, '2026-03-31 10:07:09', NULL, NULL, NULL, '2026-04-02 22:07:09', '2026-04-02 22:07:09');
INSERT INTO `sensor_data` VALUES (74962, 4, 6.20, 48.30, '2026-03-31 16:07:09', NULL, NULL, NULL, '2026-04-02 22:07:09', '2026-04-02 22:07:09');
INSERT INTO `sensor_data` VALUES (74963, 4, 5.60, 51.20, '2026-03-31 22:07:09', NULL, NULL, NULL, '2026-04-02 22:07:09', '2026-04-02 22:07:09');
INSERT INTO `sensor_data` VALUES (74964, 4, 7.10, 46.80, '2026-04-01 04:07:09', NULL, NULL, NULL, '2026-04-02 22:07:09', '2026-04-02 22:07:09');
INSERT INTO `sensor_data` VALUES (74965, 4, 4.50, 54.50, '2026-04-01 10:07:09', NULL, NULL, NULL, '2026-04-02 22:07:09', '2026-04-02 22:07:09');
INSERT INTO `sensor_data` VALUES (74966, 4, 5.80, 49.90, '2026-04-01 16:07:09', NULL, NULL, NULL, '2026-04-02 22:07:09', '2026-04-02 22:07:09');
INSERT INTO `sensor_data` VALUES (74967, 4, 5.60, 52.80, '2026-04-02 22:07:09', NULL, NULL, NULL, '2026-04-02 22:07:09', '2026-04-02 22:07:09');
INSERT INTO `sensor_data` VALUES (74968, 5, 5.80, 53.50, '2026-03-26 22:07:09', NULL, NULL, NULL, '2026-04-02 22:07:09', '2026-04-02 22:07:09');
INSERT INTO `sensor_data` VALUES (74969, 5, 7.50, 56.80, '2026-03-27 04:07:09', NULL, NULL, NULL, '2026-04-02 22:07:09', '2026-04-02 22:07:09');
INSERT INTO `sensor_data` VALUES (74970, 5, 4.20, 47.20, '2026-03-27 10:07:09', NULL, NULL, NULL, '2026-04-02 22:07:09', '2026-04-02 22:07:09');
INSERT INTO `sensor_data` VALUES (74971, 5, 6.90, 54.10, '2026-03-27 16:07:09', NULL, NULL, NULL, '2026-04-02 22:07:09', '2026-04-02 22:07:09');
INSERT INTO `sensor_data` VALUES (74972, 5, 6.10, 52.90, '2026-03-27 22:07:09', NULL, NULL, NULL, '2026-04-02 22:07:09', '2026-04-02 22:07:09');
INSERT INTO `sensor_data` VALUES (74973, 5, 8.10, 58.30, '2026-03-28 04:07:09', NULL, NULL, NULL, '2026-04-02 22:07:09', '2026-04-02 22:07:09');
INSERT INTO `sensor_data` VALUES (74974, 5, 4.80, 49.60, '2026-03-28 10:07:09', NULL, NULL, NULL, '2026-04-02 22:07:09', '2026-04-02 22:07:09');
INSERT INTO `sensor_data` VALUES (74975, 5, 6.50, 55.20, '2026-03-28 16:07:09', NULL, NULL, NULL, '2026-04-02 22:07:09', '2026-04-02 22:07:09');
INSERT INTO `sensor_data` VALUES (74976, 5, 5.90, 51.70, '2026-03-28 22:07:09', NULL, NULL, NULL, '2026-04-02 22:07:09', '2026-04-02 22:07:09');
INSERT INTO `sensor_data` VALUES (74977, 5, 7.80, 57.60, '2026-03-29 04:07:09', NULL, NULL, NULL, '2026-04-02 22:07:09', '2026-04-02 22:07:09');
INSERT INTO `sensor_data` VALUES (74978, 5, 4.50, 48.50, '2026-03-29 10:07:09', NULL, NULL, NULL, '2026-04-02 22:07:09', '2026-04-02 22:07:09');
INSERT INTO `sensor_data` VALUES (74979, 5, 6.30, 53.80, '2026-03-29 16:07:09', NULL, NULL, NULL, '2026-04-02 22:07:09', '2026-04-02 22:07:09');
INSERT INTO `sensor_data` VALUES (74980, 5, 6.00, 54.20, '2026-03-29 22:07:09', NULL, NULL, NULL, '2026-04-02 22:07:09', '2026-04-02 22:07:09');
INSERT INTO `sensor_data` VALUES (74981, 5, 7.20, 56.90, '2026-03-30 04:07:09', NULL, NULL, NULL, '2026-04-02 22:07:09', '2026-04-02 22:07:09');
INSERT INTO `sensor_data` VALUES (74982, 5, 4.90, 49.80, '2026-03-30 10:07:09', NULL, NULL, NULL, '2026-04-02 22:07:09', '2026-04-02 22:07:09');
INSERT INTO `sensor_data` VALUES (74983, 5, 6.40, 52.60, '2026-03-30 16:07:09', NULL, NULL, NULL, '2026-04-02 22:07:09', '2026-04-02 22:07:09');
INSERT INTO `sensor_data` VALUES (74984, 5, 6.20, 55.50, '2026-03-30 22:07:09', NULL, NULL, NULL, '2026-04-02 22:07:09', '2026-04-02 22:07:09');
INSERT INTO `sensor_data` VALUES (74985, 5, 8.30, 59.10, '2026-03-31 04:07:09', NULL, NULL, NULL, '2026-04-02 22:07:09', '2026-04-02 22:07:09');
INSERT INTO `sensor_data` VALUES (74986, 5, 5.10, 48.90, '2026-03-31 10:07:09', NULL, NULL, NULL, '2026-04-02 22:07:09', '2026-04-02 22:07:09');
INSERT INTO `sensor_data` VALUES (74987, 5, 6.70, 53.20, '2026-03-31 16:07:09', NULL, NULL, NULL, '2026-04-02 22:07:09', '2026-04-02 22:07:09');
INSERT INTO `sensor_data` VALUES (74988, 5, 5.90, 54.80, '2026-03-31 22:07:09', NULL, NULL, NULL, '2026-04-02 22:07:09', '2026-04-02 22:07:09');
INSERT INTO `sensor_data` VALUES (74989, 5, 7.40, 57.30, '2026-04-01 04:07:09', NULL, NULL, NULL, '2026-04-02 22:07:09', '2026-04-02 22:07:09');
INSERT INTO `sensor_data` VALUES (74990, 5, 4.70, 50.20, '2026-04-01 10:07:09', NULL, NULL, NULL, '2026-04-02 22:07:09', '2026-04-02 22:07:09');
INSERT INTO `sensor_data` VALUES (74991, 5, 6.50, 52.80, '2026-04-01 16:07:09', NULL, NULL, NULL, '2026-04-02 22:07:09', '2026-04-02 22:07:09');
INSERT INTO `sensor_data` VALUES (74992, 5, 6.20, 55.30, '2026-04-02 22:07:09', NULL, NULL, NULL, '2026-04-02 22:07:09', '2026-04-02 22:07:09');

-- ----------------------------
-- Table structure for users
-- ----------------------------
DROP TABLE IF EXISTS `users`;
CREATE TABLE `users`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `password` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `email` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `real_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `role` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'USER',
  `status` tinyint NULL DEFAULT 1 COMMENT '1-启用，0-禁用',
  `open_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '微信小程序唯一ID',
  `wx_nickname` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '微信昵称',
  `wx_avatar` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '微信头像',
  `created_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `creator_id` bigint NULL DEFAULT NULL,
  `updater_id` bigint NULL DEFAULT NULL,
  `deleted` tinyint NULL DEFAULT 0 COMMENT '逻辑删除 0-未删除 1-已删除',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `username`(`username` ASC) USING BTREE,
  UNIQUE INDEX `uk_phone`(`phone` ASC) USING BTREE,
  INDEX `idx_username`(`username` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 21 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of users
-- ----------------------------
INSERT INTO `users` VALUES (15, 'admin', '$2b$10$dkStpko5WPMbqxq2KrlHJeQPx0Q9jC35lOikGi2EZnHhK7YWiso/O', 'admin@coldchain-guardian.com', '13800000001', '系统管理员', 'ADMIN', 1, NULL, NULL, NULL, '2026-04-02 22:07:09', '2026-04-02 22:07:54', NULL, NULL, 0);
INSERT INTO `users` VALUES (16, 'manager', '$2b$10$dkStpko5WPMbqxq2KrlHJeQPx0Q9jC35lOikGi2EZnHhK7YWiso/O', 'manager@coldchain.com', '13800000002', '张经理', 'MANAGER', 1, NULL, NULL, NULL, '2026-04-02 22:07:09', '2026-04-02 22:07:56', NULL, NULL, 0);
INSERT INTO `users` VALUES (17, 'zhangsan', '$2b$10$dkStpko5WPMbqxq2KrlHJeQPx0Q9jC35lOikGi2EZnHhK7YWiso/O', 'zhangsan@coldchain.com', '13800000003', '张三', 'EMPLOYEE', 1, NULL, NULL, NULL, '2026-04-02 22:07:09', NULL, NULL, NULL, 0);
INSERT INTO `users` VALUES (18, 'lisi', '$2b$10$dkStpko5WPMbqxq2KrlHJeQPx0Q9jC35lOikGi2EZnHhK7YWiso/O', 'lisi@coldchain.com', '13800000004', '李四', 'TECHNICIAN', 1, NULL, NULL, NULL, '2026-04-02 22:07:09', NULL, NULL, NULL, 0);
INSERT INTO `users` VALUES (19, '19511687612', '$2a$10$default_password_for_employee', NULL, '19511687612', '郭鑫瑞', 'STOCK_MANAGER', 1, NULL, NULL, NULL, '2026-04-02 22:19:37', '2026-04-03 16:01:04', NULL, NULL, 0);
INSERT INTO `users` VALUES (20, '13188751661', '$2a$10$default_password_for_employee', NULL, '13188751661', '测试01', 'TECHNICIAN', 1, 'odIJh3fh4D_06QAVrPAOvBWbko6k', NULL, NULL, '2026-04-03 16:01:01', '2026-04-03 16:01:01', NULL, NULL, 0);

-- ----------------------------
-- Table structure for warehouse_areas
-- ----------------------------
DROP TABLE IF EXISTS `warehouse_areas`;
CREATE TABLE `warehouse_areas`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `parent_id` bigint NULL DEFAULT NULL COMMENT '上级库区ID，NULL表示顶级',
  `area_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '库区编码',
  `area_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '库区名称',
  `area_level` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'AREA' COMMENT '层级：SITE/WAREHOUSE/FLOOR/AREA/BIN',
  `address` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '地址（顶级/仓库级可用）',
  `location_desc` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '位置描述（如A栋2层东区）',
  `temperature_threshold_min` decimal(5, 2) NULL DEFAULT -20.00,
  `temperature_threshold_max` decimal(5, 2) NULL DEFAULT 8.00,
  `humidity_threshold_min` decimal(5, 2) NULL DEFAULT 30.00,
  `humidity_threshold_max` decimal(5, 2) NULL DEFAULT 70.00,
  `alarm_enabled` tinyint NULL DEFAULT 1,
  `status` tinyint NULL DEFAULT 1 COMMENT '1-启用，0-禁用',
  `sort_no` int NULL DEFAULT 0 COMMENT '排序',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `created_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `creator_id` bigint NULL DEFAULT NULL,
  `updater_id` bigint NULL DEFAULT NULL,
  `area_path` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '树状路径(如 /1/3/12/)',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `area_code`(`area_code` ASC) USING BTREE,
  INDEX `idx_parent`(`parent_id` ASC) USING BTREE,
  INDEX `idx_level`(`area_level` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE,
  CONSTRAINT `fk_area_parent` FOREIGN KEY (`parent_id`) REFERENCES `warehouse_areas` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 19 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of warehouse_areas
-- ----------------------------
INSERT INTO `warehouse_areas` VALUES (9, NULL, 'SITE-MAIN', '聊城冷链中心', 'SITE', '山东省聊城市经济开发区', '总部主库区', -25.00, 10.00, 30.00, 70.00, 1, 1, 1, NULL, '2026-04-02 22:07:09', '2026-04-02 22:07:09', NULL, NULL, '/1/2/5/');
INSERT INTO `warehouse_areas` VALUES (10, 9, 'WH-A', 'A 栋冷冻库', 'WAREHOUSE', '聊城市经开区A栋', 'A栋冷库', -25.00, 0.00, 30.00, 60.00, 1, 1, 1, NULL, '2026-04-02 22:07:09', '2026-04-03 16:14:15', NULL, NULL, '/1/3/6/');
INSERT INTO `warehouse_areas` VALUES (11, 9, 'WH-B', 'B 栋冷藏库', 'WAREHOUSE', '聊城市经开区B栋', 'B栋冷藏库', 2.00, 8.00, 40.00, 65.00, 1, 1, 2, NULL, '2026-04-02 22:07:09', '2026-04-03 16:14:17', NULL, NULL, '/1/3/6/');
INSERT INTO `warehouse_areas` VALUES (12, 10, 'FL-A1', 'A区一层', 'FLOOR', NULL, 'A栋一层A区', -25.00, 0.00, 30.00, 60.00, 1, 1, 1, NULL, '2026-04-02 22:07:09', '2026-04-03 16:14:20', NULL, NULL, NULL);
INSERT INTO `warehouse_areas` VALUES (13, 10, 'FL-A2', 'A区二层', 'FLOOR', NULL, 'A栋二层B区', -25.00, 0.00, 30.00, 60.00, 1, 1, 2, NULL, '2026-04-02 22:07:09', '2026-04-03 16:14:24', NULL, NULL, NULL);
INSERT INTO `warehouse_areas` VALUES (14, 11, 'FL-B1', 'B区一层', 'FLOOR', NULL, 'B栋一层', 2.00, 8.00, 40.00, 65.00, 1, 1, 1, NULL, '2026-04-02 22:07:09', '2026-04-03 16:14:28', NULL, NULL, NULL);
INSERT INTO `warehouse_areas` VALUES (15, 13, 'BIN-A01', 'A01 货架', 'BIN', NULL, 'A栋一层A区A01货架', -25.00, 0.00, 30.00, 60.00, 1, 1, 1, NULL, '2026-04-02 22:07:09', '2026-04-03 16:14:43', NULL, NULL, NULL);
INSERT INTO `warehouse_areas` VALUES (16, 13, 'BIN-A02', 'A02 货架', 'BIN', NULL, 'A栋一层A区A02货架', -25.00, 0.00, 30.00, 60.00, 1, 1, 2, NULL, '2026-04-02 22:07:09', '2026-04-03 16:14:46', NULL, NULL, NULL);
INSERT INTO `warehouse_areas` VALUES (17, 14, 'BIN-B01', 'B01 货架', 'BIN', NULL, 'B栋一层B区B01货架', 2.00, 8.00, 40.00, 65.00, 1, 1, 1, NULL, '2026-04-02 22:07:09', '2026-04-03 16:14:49', NULL, NULL, NULL);
INSERT INTO `warehouse_areas` VALUES (18, 14, 'BIN-B02', 'B02 货架', 'BIN', NULL, 'B栋一层B区B02货架', 2.00, 8.00, 40.00, 65.00, 1, 1, 2, NULL, '2026-04-02 22:07:09', '2026-04-03 16:14:55', NULL, NULL, NULL);

-- ----------------------------
-- Table structure for work_order_logs
-- ----------------------------
DROP TABLE IF EXISTS `work_order_logs`;
CREATE TABLE `work_order_logs`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `work_order_id` bigint NOT NULL COMMENT '工单ID',
  `operator_id` bigint NOT NULL COMMENT '操作人ID',
  `action` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '动作(如 CREATE, ASSIGN, START, UPLOAD_PHOTO, RESOLVE)',
  `remark` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '操作备注(如\"已紧固冷机阀门\")',
  `attachment_urls` json NULL COMMENT '现场照片URL集合',
  `created_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_wo_id`(`work_order_id` ASC) USING BTREE,
  CONSTRAINT `fk_log_wo` FOREIGN KEY (`work_order_id`) REFERENCES `work_orders` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 170 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '工单流转日志表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of work_order_logs
-- ----------------------------

-- ----------------------------
-- Table structure for work_orders
-- ----------------------------
DROP TABLE IF EXISTS `work_orders`;
CREATE TABLE `work_orders`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `order_no` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '工单编号(如 WO-20260303-001)',
  `ref_alert_id` bigint NULL DEFAULT NULL COMMENT '触发该工单的源告警ID(手动建单可为空)',
  `title` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '工单标题',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '工单详细描述',
  `priority` enum('LOW','MEDIUM','HIGH','URGENT') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'MEDIUM' COMMENT '优先级',
  `order_type` enum('ALERT_FIX','INSPECTION','MAINTENANCE') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'ALERT_FIX' COMMENT '工单类型',
  `warehouse_id` bigint NOT NULL COMMENT '发生库区ID',
  `device_id` bigint NULL DEFAULT NULL COMMENT '关联设备ID(如有)',
  `status` enum('PENDING','PROCESSING','VERIFYING','COMPLETED','CLOSED') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'PENDING' COMMENT '状态: 待处理/处理中/待验收/已完成/已关闭',
  `creator_id` bigint NOT NULL COMMENT '创建人ID',
  `assigned_to` bigint NULL DEFAULT NULL COMMENT '当前指派给(处理人ID)',
  `due_time` timestamp NULL DEFAULT NULL COMMENT '要求完成时间',
  `completed_time` timestamp NULL DEFAULT NULL COMMENT '实际完成时间',
  `created_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `verified_time` timestamp NULL DEFAULT NULL COMMENT '审核/验收时间',
  `verification_result` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '审核/验收结果描述',
  `location_detail` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '发生位置详细说明',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `order_no`(`order_no` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE,
  INDEX `idx_assigned`(`assigned_to` ASC) USING BTREE,
  INDEX `fk_wo_warehouse`(`warehouse_id` ASC) USING BTREE,
  INDEX `idx_device_id`(`device_id` ASC) USING BTREE,
  CONSTRAINT `fk_wo_warehouse` FOREIGN KEY (`warehouse_id`) REFERENCES `warehouse_areas` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 213 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '设备运维工单表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of work_orders
-- ----------------------------
INSERT INTO `work_orders` VALUES (203, 'WO-20260315-001', NULL, 'A1区温度异常处理', '检查制冷设备，调整温度设置', 'HIGH', 'ALERT_FIX', 1, 1, 'COMPLETED', 1, 3, NULL, NULL, '2026-04-02 22:07:09', '2026-04-02 22:07:09', NULL, NULL, NULL);
INSERT INTO `work_orders` VALUES (204, 'WO-20260314-001', NULL, '例行设备巡检', '月度设备巡检维护', 'LOW', 'INSPECTION', 1, NULL, 'COMPLETED', 2, 3, NULL, NULL, '2026-04-02 22:07:09', '2026-04-02 22:07:09', NULL, NULL, NULL);
INSERT INTO `work_orders` VALUES (205, 'WO-20260313-001', NULL, 'A栋冷库设备保养', '季度保养维护', 'MEDIUM', 'MAINTENANCE', 2, NULL, 'COMPLETED', 1, 2, NULL, NULL, '2026-04-02 22:07:09', '2026-04-02 22:07:09', NULL, NULL, NULL);
INSERT INTO `work_orders` VALUES (206, 'WO-20260312-001', NULL, '门磁传感器更换', '更换故障门磁传感器', 'MEDIUM', 'MAINTENANCE', 10, 3, 'COMPLETED', 2, 4, NULL, NULL, '2026-04-02 22:07:09', '2026-04-02 22:07:09', NULL, NULL, NULL);
INSERT INTO `work_orders` VALUES (207, 'WO-20260318-001', NULL, 'B1区温度严重异常紧急处理', '立即检查制冷系统，可能需要紧急维修', 'URGENT', 'ALERT_FIX', 10, 4, 'PROCESSING', 1, 2, NULL, NULL, '2026-04-02 22:07:09', '2026-04-02 22:07:09', NULL, NULL, NULL);
INSERT INTO `work_orders` VALUES (208, 'WO-20260317-001', NULL, 'B1区湿度过高处理', '检查除湿设备，排查漏水可能', 'MEDIUM', 'ALERT_FIX', 10, 5, 'PROCESSING', 2, 3, NULL, NULL, '2026-04-02 22:07:09', '2026-04-02 22:07:09', NULL, NULL, NULL);
INSERT INTO `work_orders` VALUES (209, 'WO-20260316-001', NULL, '冷链车设备检修', '冷链车制冷系统检修', 'HIGH', 'MAINTENANCE', 1, 10, 'PROCESSING', 1, 2, NULL, NULL, '2026-04-02 22:07:09', '2026-04-02 22:07:09', NULL, NULL, NULL);
INSERT INTO `work_orders` VALUES (210, 'WO-20260319-001', NULL, 'B1区温度偏高处理', '检查温度传感器和制冷设备', 'MEDIUM', 'ALERT_FIX', 10, 5, 'PENDING', 1, NULL, NULL, NULL, '2026-04-02 22:07:09', '2026-04-02 22:07:09', NULL, NULL, NULL);
INSERT INTO `work_orders` VALUES (211, 'WO-20260319-002', NULL, 'B1区温度轻微异常', '持续监控温度变化', 'LOW', 'ALERT_FIX', 10, 6, 'PENDING', 2, NULL, NULL, NULL, '2026-04-02 22:07:09', '2026-04-02 22:07:09', NULL, NULL, NULL);
INSERT INTO `work_orders` VALUES (212, 'WO-20260319-003', NULL, '设备离线排查', '检查网络和设备电源', 'LOW', 'MAINTENANCE', 11, 8, 'PENDING', 1, NULL, NULL, NULL, '2026-04-02 22:07:09', '2026-04-02 22:07:09', NULL, NULL, NULL);

SET FOREIGN_KEY_CHECKS = 1;
