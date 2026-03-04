# 数据库表结构优化说明

## 背景
针对冷链物联网系统的特点，对数据库表结构进行优化，以提升 IoT 数据写入性能和系统可维护性。

## 优化内容

### 1. Work Orders 表优化
- **问题**: 存在 `creator_id` 和 `created_by` 两个表达相同业务含义的冗余字段
- **解决方案**:
  - 保留 `creator_id` 字段作为主要的创建人字段
  - 移除 `created_by` 冗余字段
- **影响**: 需要更新 `WorkOrderEntity` 映射关系

### 2. Sensor Data 表优化
- **问题**: 物理外键约束 `fk_sensor_device` 影响 IoT 高频数据写入性能
- **解决方案**:
  - 移除物理外键约束
  - 保留逻辑关联，通过应用层保证数据一致性
- **影响**: 大幅提升传感器数据写入性能

### 3. Alerts 表优化
- **问题**: 缺少告警收敛相关字段，无法有效处理高频重复告警
- **解决方案**:
  - 添加 `first_time`: 首次触发时间
  - 添加 `last_time`: 最后触发时间
  - 添加 `trigger_count`: 触发次数
- **影响**: 支持告警收敛功能，避免告警爆炸

### 4. 索引优化
- 为 `work_orders` 添加时间字段索引
- 为 `alerts` 添加复合索引以优化查询性能
- 为 `sensor_data` 添加批量查询优化索引

## 执行步骤

1. **备份现有数据**（重要！）
   ```bash
   mysqldump -u username -p coldchain_guardian > backup_before_optimization.sql
   ```

2. **执行优化脚本**
   ```bash
   mysql -u username -p coldchain_guardian < docs/database/optimize_schema.sql
   ```

3. **验证更改**
   - 检查表结构是否符合预期
   - 测试数据写入和查询功能

## 代码更新

- `AlertEntity.java`: 添加告警收敛相关字段映射
- `WorkOrderEntity.java`: 更新字段映射以对应数据库变更
- 相关 Service 和 Repository 可能需要根据字段变更进行调整

## 注意事项

1. 执行前必须完整备份数据库
2. 建议在非生产环境先测试
3. 更新应用代码以匹配新的表结构
4. 这些优化特别适合 IoT 场景下的大数据量写入需求