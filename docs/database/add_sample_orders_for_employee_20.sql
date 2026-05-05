-- 为员工ID 20 添加示例工单数据
-- 员工信息：测试01，手机号 13188751661，角色 机修工(TECHNICIAN)
-- 修正：使用正确存在的warehouse_id，匹配full_clean_export.sql中的库区数据

-- 添加6个工单给该员工（涵盖不同状态）
-- 正确的库区ID（来自full_clean_export.sql）：
-- ID 1: 聊城冷链中心 (SITE 站点级)
-- ID 2: A 栋冷冻库 (WAREHOUSE 仓库级)
-- ID 3: B 栋冷藏库 (WAREHOUSE 仓库级)
-- ID 4: A区一层 (FLOOR 楼层级 - 属于A栋)
-- ID 5: A区二层 (FLOOR 楼层级 - 属于A栋)
-- ID 6: B区一层 (FLOOR 楼层级 - 属于B栋)
-- ID 8: A01 货架 (BIN 储区级 - 属于A区一层)
-- ID 10: B01 货架 (BIN 储区级 - 属于B区一层)
--
-- 正确的设备ID：
-- ID 1: A1区温度湿度传感器 1号 (area_id=8)
-- ID 2: A1区温度湿度传感器 2号 (area_id=8)
-- ID 3: A2区温度湿度传感器 1号 (area_id=9)
-- ID 4: B1区温度湿度传感器 1号 (area_id=10)
-- ID 5: B1区温度湿度传感器 2号 (area_id=10)
-- ID 6: B1区温度湿度传感器 3号 (area_id=10)
-- ID 7: A1区入口门磁传感器 (area_id=8)
-- ID 8: B1区入口门磁传感器 (area_id=10)
-- ID 9: 超低温保存冰柜 (area_id=4)

INSERT INTO work_orders (id, order_no, title, description, priority, order_type, warehouse_id, device_id, status, creator_id, assigned_to, due_time, created_time, updated_time) VALUES
-- 待处理 - 设备维修 B栋冷藏库 B01货架 (warehouse_id=6, device_id=6)
(22, 'WO-20260403-001', '维修冷藏区除湿机', 'B区一层B01货架除湿机噪音过大，可能轴承磨损，需要停机检查维修', 'HIGH', 'MAINTENANCE', 6, 6, 'PENDING', 1, 20, DATE_ADD(NOW(), INTERVAL 2 HOUR), NOW(), NOW()),
-- 待处理 - 告警消缺 A栋冷冻库 A区二层 (warehouse_id=5, device_id=3)
(23, 'WO-20260403-002', '处理传感器离线故障', 'A区二层门口温度传感器离线超过30分钟，需要排查线路故障', 'URGENT', 'ALERT_FIX', 5, 3, 'PENDING', 1, 20, DATE_ADD(NOW(), INTERVAL 30 MINUTE), NOW(), NOW()),
-- 处理中 - 更换传感器 A栋冷冻库 A区一层 (warehouse_id=4, device_id=1)
(24, 'WO-20260403-003', '更换温度传感器探头', 'A区一层A01货架温度传感器读数偏差超过2℃，需要更换新探头并重新校准', 'MEDIUM', 'MAINTENANCE', 4, 1, 'PROCESSING', 1, 20, DATE_ADD(NOW(), INTERVAL 1 DAY), NOW(), NOW()),
-- 处理中 - 制冷系统保养 A栋冷冻库 (warehouse_id=2)
(25, 'WO-20260403-004', 'A栋主制冷机组季度保养', '按照季度维护计划，对A栋主制冷机组进行常规保养，清洁散热片，检查制冷剂压力', 'MEDIUM', 'INSPECTION', 2, NULL, 'PROCESSING', 1, 20, DATE_ADD(NOW(), INTERVAL 2 DAY), NOW(), NOW()),
-- 待验收 - 已经处理完成等待审核 B栋冷藏库 (warehouse_id=3)
(26, 'WO-20260403-005', '修复冷藏区库房门锁', 'B栋一层B01库区门锁损坏无法正常锁闭，已经更换新锁，等待验收', 'LOW', 'MAINTENANCE', 3, NULL, 'VERIFYING', 1, 20, DATE_ADD(NOW(), INTERVAL 30 MINUTE), NOW(), NOW()),
-- 已完成 - 历史工单 总部库区 (warehouse_id=1)
(27, 'WO-20260403-006', '总部库区空调系统维修', '总部办公区空调不制冷，已经维修完成', 'MEDIUM', 'MAINTENANCE', 1, NULL, 'COMPLETED', 1, 20, DATE_SUB(NOW(), INTERVAL 3 DAY), DATE_SUB(NOW(), INTERVAL 2 DAY), DATE_SUB(NOW(), INTERVAL 2 DAY));
