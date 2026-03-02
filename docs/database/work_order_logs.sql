CREATE TABLE IF NOT EXISTS work_order_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    work_order_id BIGINT NOT NULL COMMENT '工单ID',
    operator_id BIGINT NOT NULL COMMENT '操作人ID',

    action VARCHAR(50) NOT NULL COMMENT '动作(如 CREATE, ASSIGN, START, UPLOAD_PHOTO, RESOLVE)',
    remark TEXT COMMENT '操作备注(如"已紧固冷机阀门")',
    attachment_urls JSON NULL COMMENT '现场照片URL集合',

    created_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',

    INDEX idx_wo_id (work_order_id),
    CONSTRAINT fk_log_wo FOREIGN KEY (work_order_id) REFERENCES work_orders(id)
) COMMENT='工单流转日志表';