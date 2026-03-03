-- 为alerts表添加缺少的字段，用于告警统计和跟踪
-- 这些字段用于支持告警分析功能：首次触发时间、最后触发时间、触发次数

ALTER TABLE alerts
ADD COLUMN first_time TIMESTAMP NULL COMMENT '首次触发时间',
ADD COLUMN last_time TIMESTAMP NULL COMMENT '最后一次触发时间',
ADD COLUMN trigger_count INT DEFAULT 1 COMMENT '触发次数';

-- 验证字段添加结果
DESCRIBE alerts;