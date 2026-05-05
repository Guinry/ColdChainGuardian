-- 为员工ID 19 添加示例工单数据
-- 员工信息：郭鑫瑞，手机号 19511687612，角色 库管员(STOCK_MANAGER)
-- 修正：使用正确存在的warehouse_id，匹配full_clean_export.sql中的库区数据

-- 添加5个工单给该员工（涵盖不同状态）
-- 已知库区ID: 1(总部), 2(A栋), 3(冷藏区), 4(冷冻区)
INSERT INTO work_orders (id, order_no, title, description, priority, order_type, warehouse_id, device_id, status, creator_id, assigned_to, due_time, created_time, updated_time) VALUES
-- 待处理 - 告警消缺 冷冻区(4)
(11, 'WO-20260402-001', '冷冻A区温度异常处理', '冷冻A区温度持续高于阈值-12℃，需要立即检查制冷设备运行状态并排查原因', 'HIGH', 'ALERT_FIX', 4, 2, 'PENDING', 1, 19, DATE_ADD(NOW(), INTERVAL 2 HOUR), NOW(), NOW()),
-- 待处理 - 日常巡检 冷藏区(3)
(12, 'WO-20260402-002', '每周例行巡检 - 冷藏B区', '按照SOP对冷藏B区进行每周例行安全巡检，检查温湿度记录、设备运行噪音和管道密封情况', 'MEDIUM', 'INSPECTION', 3, NULL, 'PENDING', 1, 19, DATE_ADD(NOW(), INTERVAL 1 DAY), NOW(), NOW()),
-- 处理中 - 设备维护 恒温区在库区5，但没有5，用冷藏区(3)
(13, 'WO-20260402-003', '补充制冷剂 - 冷藏C-05', '冷藏C-05区制冷剂压力偏低，需要现场补充制冷剂并检查管道是否存在泄漏', 'URGENT', 'MAINTENANCE', 3, 6, 'PROCESSING', 1, 19, DATE_ADD(NOW(), INTERVAL 1 HOUR), NOW(), NOW()),
-- 处理中 - 温度传感器校准 冷冻区(4)
(14, 'WO-20260402-004', '校准温度传感器 - 冷冻A-03', '月度校准计划，对冷冻A-03库区温度传感器进行零点校准', 'MEDIUM', 'MAINTENANCE', 4, 5, 'PROCESSING', 1, 19, DATE_ADD(NOW(), INTERVAL 3 HOUR), NOW(), NOW()),
-- 待验收 - 已经处理完成等待审核 冷藏区(3)
(15, 'WO-20260402-005', '更换除湿过滤网 - B-02', '库区B-02除湿机过滤网堵塞，已经更换新过滤网，等待主管验收', 'LOW', 'MAINTENANCE', 3, 6, 'VERIFYING', 1, 19, DATE_ADD(NOW(), INTERVAL 30 MINUTE), NOW(), NOW());
