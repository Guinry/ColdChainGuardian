-- 数据库表结构优化脚本
--
-- 此脚本解决以下问题：
-- 1. 移除 work_orders 表中的冗余字段 created_by
-- 2. 移除 sensor_data 表中的物理外键约束（提升IoT数据写入性能）
-- 3. 为 alerts 表添加告警收敛相关字段（first_time, last_time, trigger_count）
-- 4. 为 work_orders 表添加时间索引

-- 备份重要数据
-- 注意：在实际执行前，务必先备份数据库！

-- 1. 为 work_orders 表添加时间字段索引
ALTER TABLE `work_orders` ADD INDEX `idx_created_time`(`created_time`);

-- 2. 从 work_orders 表移除冗余字段 created_by
-- 首先，如果 created_by 包含有用数据，将其复制到 creator_id（如果 creator_id 为空的话）
-- UPDATE `work_orders` SET `creator_id` = `created_by` WHERE `creator_id` IS NULL AND `created_by` IS NOT NULL;

-- 删除冗余字段 created_by
ALTER TABLE `work_orders` DROP COLUMN `created_by`;

-- 3. 移除 sensor_data 表的物理外键约束（提升 IoT 数据写入性能）
ALTER TABLE `sensor_data` DROP FOREIGN KEY `fk_sensor_device`;

-- 4. 为 alerts 表添加告警收敛相关字段
ALTER TABLE `alerts` ADD COLUMN `first_time` timestamp NULL DEFAULT NULL COMMENT '首次触发时间';
ALTER TABLE `alerts` ADD COLUMN `last_time` timestamp NULL DEFAULT NULL COMMENT '最后触发时间';
ALTER TABLE `alerts` ADD COLUMN `trigger_count` int NOT NULL DEFAULT 1 COMMENT '触发次数';

-- 5. 为 alerts 表添加复合索引以优化告警统计查询
ALTER TABLE `alerts` ADD INDEX `idx_device_alert_time`(`device_id`, `alert_type`, `created_time`);
ALTER TABLE `alerts` ADD INDEX `idx_first_time`(`first_time`);
ALTER TABLE `alerts` ADD INDEX `idx_last_time`(`last_time`);

-- 6. 为 sensor_data 表添加批量查询优化索引
ALTER TABLE `sensor_data` ADD INDEX `idx_device_data_time`(`device_id`, `data_time`);

-- 完成优化
-- 请确保相应的 Java Entity 类也进行相应调整以匹配新的表结构