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

 Date: 04/03/2026 20:13:59
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
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_session_time`(`session_id` ASC, `created_time` ASC) USING BTREE COMMENT '用于按时间顺序拉取某个会话的所有聊天记录',
  CONSTRAINT `fk_msg_session` FOREIGN KEY (`session_id`) REFERENCES `ai_chat_sessions` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = 'AI助手-消息明细表' ROW_FORMAT = Dynamic;

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
) ENGINE = InnoDB AUTO_INCREMENT = 9 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = 'AI助手-会话表' ROW_FORMAT = Dynamic;

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
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '告警配置' ROW_FORMAT = Dynamic;

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
  INDEX `fk_alerts_device`(`device_id` ASC) USING BTREE,
  INDEX `fk_alerts_work_order`(`work_order_id` ASC) USING BTREE,
  INDEX `idx_status_level_time`(`status` ASC, `alert_level` ASC, `created_time` DESC) USING BTREE,
  CONSTRAINT `fk_alerts_device` FOREIGN KEY (`device_id`) REFERENCES `devices` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_alerts_work_order` FOREIGN KEY (`work_order_id`) REFERENCES `work_orders` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '告警记录表' ROW_FORMAT = Dynamic;

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
  CONSTRAINT `fk_device_area` FOREIGN KEY (`area_id`) REFERENCES `warehouse_areas` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 15 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '设备表' ROW_FORMAT = Dynamic;

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
  `recv_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '服务端接收时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_device_time`(`device_id` ASC, `data_time` ASC) USING BTREE,
  INDEX `idx_time`(`data_time` ASC) USING BTREE,
  INDEX `idx_device_time_desc`(`device_id` ASC, `data_time` DESC) USING BTREE,
  CONSTRAINT `fk_sensor_device` FOREIGN KEY (`device_id`) REFERENCES `devices` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '温湿度原始上报数据' ROW_FORMAT = Dynamic;

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
  `created_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `creator_id` bigint NULL DEFAULT NULL,
  `updater_id` bigint NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `username`(`username` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

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
) ENGINE = InnoDB AUTO_INCREMENT = 12 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

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
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '工单流转日志表' ROW_FORMAT = Dynamic;

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
  CONSTRAINT `fk_wo_warehouse` FOREIGN KEY (`warehouse_id`) REFERENCES `warehouse_areas` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '设备运维工单表' ROW_FORMAT = Dynamic;

SET FOREIGN_KEY_CHECKS = 1;
