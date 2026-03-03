-- 为alerts表添加完整的历史字段，用于告警全生命周期管理
-- 包括确认时间、解决时间、首次触发时间、最后触发时间、触发次数

ALTER TABLE alerts
ADD COLUMN acknowledged_time TIMESTAMP NULL COMMENT '确认时间',
ADD COLUMN resolved_time TIMESTAMP NULL COMMENT '解决时间',
ADD COLUMN first_time TIMESTAMP NULL COMMENT '首次触发时间',
ADD COLUMN last_time TIMESTAMP NULL COMMENT '最后一次触发时间',
ADD COLUMN trigger_count INT DEFAULT 1 COMMENT '触发次数';

-- 验证字段添加结果
DESCRIBE alerts;