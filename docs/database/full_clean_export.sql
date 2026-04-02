/*
 ============================================
 ColdChain Guardian - 完整干净数据库导出
 结构: 从 coldchain_guardian.sql (Navicat 导出)
 数据: 使用 UTF-8 无BOM 编码，确保中文正常显示
 解决: 数据库中文乱码 (???) 问题
 ============================================
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
) ENGINE = InnoDB AUTO_INCREMENT = 46 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = 'AI助手-消息明细表' ROW_FORMAT = Dynamic;

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
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = 'AI助手-会话表' ROW_FORMAT = Dynamic;

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
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '告警配置' ROW_FORMAT = Dynamic;

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
) ENGINE = InnoDB AUTO_INCREMENT = 573 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '告警记录表' ROW_FORMAT = Dynamic;

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
) ENGINE = InnoDB AUTO_INCREMENT = 14 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '设备表' ROW_FORMAT = Dynamic;

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
) ENGINE = InnoDB AUTO_INCREMENT = 74893 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '温湿度原始上报数据' ROW_FORMAT = Dynamic;

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
) ENGINE = InnoDB AUTO_INCREMENT = 15 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

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
) ENGINE = InnoDB AUTO_INCREMENT = 9 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for work_order_logs
-- ----------------------------
DROP TABLE IF EXISTS `work_order_logs`;
CREATE TABLE `work_order_logs`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `work_order_id` bigint NOT NULL COMMENT '工单ID',
  `operator_id` bigint NOT NULL COMMENT '操作人ID',
  `action` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '动作(如 CREATE, ASSIGN, START, UPLOAD_PHOTO, RESOLVE)',
  `remark` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '操作备注(如"已紧固冷机阀门")',
  `attachment_urls` json NULL COMMENT '现场照片URL集合',
  `created_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_wo_id`(`work_order_id` ASC) USING BTREE,
  CONSTRAINT `fk_log_wo` FOREIGN KEY (`work_order_id`) REFERENCES `work_orders` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 170 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '工单流转日志表' ROW_FORMAT = Dynamic;

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
) ENGINE = InnoDB AUTO_INCREMENT = 203 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '设备运维工单表' ROW_FORMAT = Dynamic;

-- ============================================
-- 插入正确编码的测试数据
-- 确保: UTF-8 无BOM 编码，中文正常显示
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
INSERT IGNORE INTO ai_chat_sessions (user_id, title, is_deleted, created_time, updated_time)
VALUES
(1, 'B1区温度异常分析', 0, DATE_SUB(NOW(), INTERVAL 1 DAY), NOW()),
(1, '本周告警趋势分析', 0, DATE_SUB(NOW(), INTERVAL 6 HOUR), NOW());

-- ============================================
-- 统计查询 - 验证数据插入成功
-- ============================================
SELECT '用户' AS table_name, COUNT(*) AS count FROM users
UNION ALL SELECT '库区', COUNT(*) FROM warehouse_areas
UNION ALL SELECT '设备', COUNT(*) FROM devices
UNION ALL SELECT '传感器历史', COUNT(*) FROM sensor_data
UNION ALL SELECT '告警', COUNT(*) FROM alerts
UNION ALL SELECT '工单', COUNT(*) FROM work_orders
UNION ALL SELECT 'AI会话', COUNT(*) FROM ai_chat_sessions;

SET FOREIGN_KEY_CHECKS = 1;
