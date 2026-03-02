-- 添加area_path字段到warehouse_areas表
ALTER TABLE warehouse_areas
ADD COLUMN area_path VARCHAR(500) NULL COMMENT '区域路径，用于层级显示，如 0/1/5/';