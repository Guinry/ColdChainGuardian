# 库区管理模块实现文档

## 模块概述
库区管理模块负责冷链物流仓储中的库区信息维护，包括库区的基本信息管理、层级关系管理、状态控制和阈值设定等功能。根据数据库设计文档，该模块操作`warehouse_areas`表。

## 实现范围
根据PLAN.md、DATABASE_SCHEMA.md和CLAUDE.md的要求，实现了以下功能：

1. **库区CRUD操作**：创建、读取、更新、删除库区信息
2. **层级管理**：支持库区间的父子关系（SITE/WAREHOUSE/FLOOR/AREA/BIN）
3. **阈值管理**：设定温湿度阈值，供下级设备使用
4. **数据验证**：输入参数验证、业务规则验证
5. **错误处理**：统一的错误处理机制

## 数据库表结构
根据DATABASE_SCHEMA.md，使用`warehouse_areas`表结构：

```sql
CREATE TABLE IF NOT EXISTS warehouse_areas (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    parent_id BIGINT NULL COMMENT '上级库区ID，NULL表示顶级',
    area_code VARCHAR(50) NOT NULL UNIQUE COMMENT '库区编码',
    area_name VARCHAR(100) NOT NULL COMMENT '库区名称',
    area_level VARCHAR(20) NOT NULL DEFAULT 'AREA' COMMENT '层级：SITE/WAREHOUSE/FLOOR/AREA/BIN',
    address VARCHAR(200) NULL COMMENT '地址（顶级/仓库级可用）',
    location_desc VARCHAR(200) NULL COMMENT '位置描述（如A栋2层东区）',

    -- 库区默认阈值（设备可覆盖）
    temperature_threshold_min DECIMAL(5,2) DEFAULT -20.00,
    temperature_threshold_max DECIMAL(5,2) DEFAULT 8.00,
    humidity_threshold_min DECIMAL(5,2) DEFAULT 30.00,
    humidity_threshold_max DECIMAL(5,2) DEFAULT 70.00,
    alarm_enabled TINYINT DEFAULT 1,

    status TINYINT DEFAULT 1 COMMENT '1-启用，0-禁用',
    sort_no INT DEFAULT 0 COMMENT '排序',
    remark VARCHAR(500) NULL,

    created_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    creator_id BIGINT,
    updater_id BIGINT,

    INDEX idx_parent (parent_id),
    INDEX idx_level (area_level),
    INDEX idx_status (status),
    CONSTRAINT fk_area_parent FOREIGN KEY (parent_id) REFERENCES warehouse_areas(id)
);
```

## 实现详情

### 1. Entity层 - AreaEntity.java
- 使用`@TableName("warehouse_areas")`注解映射到数据库表
- 字段映射：
  - parentId -> parent_id (上级库区ID)
  - areaCode -> area_code (库区编码)
  - areaName -> area_name (库区名称)
  - areaLevel -> area_level (层级：SITE/WAREHOUSE/FLOOR/AREA/BIN)
  - address -> address (地址)
  - locationDesc -> location_desc (位置描述)
  - temperatureThresholdMin -> temperature_threshold_min (温度最小阈值)
  - temperatureThresholdMax -> temperature_threshold_max (温度最大阈值)
  - humidityThresholdMin -> humidity_threshold_min (湿度最小阈值)
  - humidityThresholdMax -> humidity_threshold_max (湿度最大阈值)
  - alarmEnabled -> alarm_enabled (告警启用标志)
  - status -> status (状态)
  - sortNo -> sort_no (排序号)
  - remark -> remark (备注)
  - creatorId -> creator_id (创建人ID)
  - updaterId -> updater_id (更新人ID)

### 2. Repository层 - AreaRepository.java
- 封装了对库区数据的访问操作
- 提供了基本的CRUD方法
- 提供了按编码、名称查询和唯一性检查的方法
- 提供了查找子库区的方法

### 3. DTO层
- `AreaDto.java`：用于返回给前端的数据传输对象
- `CreateAreaRequestDto.java`：用于接收前端创建/更新请求的传输对象
- 包含了数据验证注解

### 4. Service层 - AreaService.java
- 实现了完整的业务逻辑
- 包括：获取所有库区、根据ID获取库区、根据父ID获取子库区、创建库区、更新库区、删除库区
- 实现了业务规则验证：
  - 库区编码唯一性检查
  - 库区名称唯一性检查
  - 库区存在性检查
  - 删除前检查是否有子库区

### 5. Controller层 - AreaController.java
- 定义了REST API接口
- 路径：`/api/areas`
- 支持的操作：
  - GET `/api/areas` - 获取所有库区
  - GET `/api/areas/parent/{parentId}` - 根据父ID获取子库区
  - GET `/api/areas/{id}` - 根据ID获取库区
  - POST `/api/areas` - 创建库区
  - PUT `/api/areas/{id}` - 更新库区
  - DELETE `/api/areas/{id}` - 删除库区

## 错误处理
- `AREA_NOT_EXISTS` (10005) - 库区不存在
- `AREA_CODE_EXISTS` (10011) - 库区编码已存在
- `AREA_HAS_CHILDREN` (10012) - 库区存在子库区，无法删除
- `PARAMETER_ERROR` (400) - 参数错误

## 安全性考虑
- 输入验证：使用Jakarta Validation进行参数校验
- 业务逻辑验证：防止库区编码和名称重复
- 状态检查：确保操作的库区存在
- 层级完整性：删除前检查子库区存在

## 扩展性考虑
- 代码结构遵循分层架构，易于扩展
- Repository层抽象了数据访问逻辑，便于更换数据源
- DTO层分离了内部实体和外部接口，提高灵活性
- 支持库区层级结构，便于复杂仓储布局管理

## 与整体系统集成
- 与设备管理模块集成：库区ID作为设备的外键
- 与告警系统集成：库区ID用于告警配置和记录
- 与巡检计划集成：库区ID用于巡检计划和记录
- 与隐患上报集成：库区ID用于隐患报告
- 与阈值管理集成：库区设定的阈值供设备使用

## API示例

### 创建顶级库区
```
POST /api/areas
Content-Type: application/json

{
    "areaCode": "WH_A",
    "areaName": "A仓库",
    "areaLevel": "WAREHOUSE",
    "address": "工厂北侧",
    "locationDesc": "A栋一层",
    "temperatureThresholdMin": -18.00,
    "temperatureThresholdMax": -10.00,
    "humidityThresholdMin": 35.00,
    "humidityThresholdMax": 65.00,
    "alarmEnabled": 1,
    "status": 1,
    "sortNo": 1
}
```

### 创建子库区
```
POST /api/areas
Content-Type: application/json

{
    "parentId": 1,
    "areaCode": "WH_A_F1",
    "areaName": "A仓库一楼东区",
    "areaLevel": "AREA",
    "locationDesc": "A仓库一楼东侧",
    "temperatureThresholdMin": -18.00,
    "temperatureThresholdMax": -10.00,
    "humidityThresholdMin": 35.00,
    "humidityThresholdMax": 65.00,
    "alarmEnabled": 1,
    "status": 1,
    "sortNo": 1
}
```

### 更新库区
```
PUT /api/areas/2
Content-Type: application/json

{
    "parentId": 1,
    "areaCode": "WH_A_F1_NEW",
    "areaName": "A仓库一楼西区",
    "areaLevel": "AREA",
    "locationDesc": "A仓库一楼西侧",
    "temperatureThresholdMin": -20.00,
    "temperatureThresholdMax": -8.00,
    "humidityThresholdMin": 30.00,
    "humidityThresholdMax": 70.00,
    "alarmEnabled": 1,
    "status": 1,
    "sortNo": 2
}
```

## 后续优化建议
- 添加分页查询功能
- 添加按条件筛选功能
- 添加软删除支持
- 添加操作审计日志
- 添加批量操作功能