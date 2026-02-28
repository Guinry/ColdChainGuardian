CREATE TABLE IF NOT EXISTS sensor_data (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    device_id BIGINT NOT NULL COMMENT '设备ID',
    temperature DECIMAL(5,2) COMMENT '温度(℃)',
    humidity DECIMAL(5,2) COMMENT '湿度(%)',
    data_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '数据时间戳',
    battery_level DECIMAL(5,2) COMMENT '电池电量(%)',
    signal_strength INT COMMENT '信号强度',
    raw_data TEXT COMMENT '原始数据',
    created_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    -- 可选增强：服务端接收时间（不影响现有逻辑）
    recv_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '服务端接收时间',

    INDEX idx_device_time (device_id, data_time),
    INDEX idx_time (data_time),

    CONSTRAINT fk_sensor_device FOREIGN KEY (device_id) REFERENCES devices(id)
) COMMENT='温湿度原始上报数据';