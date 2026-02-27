# 库区管理模块完整实现

## 1. 概述

库区管理模块是冷链仓储安全管理系统的重要组成部分，实现了对仓储区域的层级化管理。该模块支持从站点(SITE)到库位(BIN)的五级结构，并具备完整的CRUD操作、阈值管理和批量操作功能。

## 2. 数据库设计

根据`docs/database/warehouse_areas_table.sql`文件，`warehouse_areas`表结构如下：

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

## 3. 后端实现

### 3.1 Entity层

`AreaEntity.java`继承自`BaseEntity`，包含所有数据库字段的映射。

### 3.2 Repository层

`AreaRepository.java`提供了基本的数据访问操作，包括：
- 保存、更新、删除操作
- 根据ID、编码查找
- 查找子节点
- 唯一性检查

### 3.3 Service层

`AreaService.java`提供业务逻辑，包括：
- CRUD操作
- 树形结构构建
- 库区移动功能
- 批量操作
- 业务验证

### 3.4 Controller层

`AreaController.java`提供REST API接口：
- `GET /api/areas` - 获取完整树形结构
- `GET /api/areas/{id}` - 获取指定节点
- `GET /api/areas/parent/{parentId}` - 获取子节点列表
- `POST /api/areas` - 创建库区
- `PUT /api/areas/{id}` - 更新库区
- `DELETE /api/areas/{id}` - 删除库区
- `POST /api/areas/{id}/move` - 移动库区
- `POST /api/areas/batch` - 批量操作

## 4. 前端实现

### 4.1 API层

`src/api/area.js`定义了API调用方法。

### 4.2 页面实现

`src/views/warehouse-area/WarehouseAreaManage.vue`实现了完整的用户界面，包括：
- 左侧树形结构展示
- 右侧详情面板
- 搜索过滤功能
- 新增/编辑对话框
- 右键菜单操作
- 状态管理功能

## 5. 功能特性

### 5.1 层级化管理
- 支持SITE → WAREHOUSE → FLOOR → AREA → BIN五级结构
- 可视化的树形结构展示
- 灵活的父子节点关系管理

### 5.2 阈值管理
- 温度阈值：最小/最大温度范围设置
- 湿度阈值：最小/最大湿度范围设置
- 阈值继承机制

### 5.3 完整CRUD操作
- 创建：支持新增各级节点
- 读取：树形结构展示 + 详情面板
- 更新：支持编辑节点信息
- 删除：软删除机制，防止误操作

### 5.4 批量操作
- 批量启用/禁用
- 批量开启/关闭告警
- 批量设置阈值

### 5.5 用户体验
- 左树右详情的经典布局
- 搜索过滤功能
- 右键菜单和行内操作
- 实时状态显示

## 6. API接口文档

### 6.1 获取完整树形结构
```
GET /api/areas
Response:
{
  "success": true,
  "message": "操作成功",
  "data": [
    {
      "id": 1,
      "parentId": null,
      "areaCode": "SITE_MAIN",
      "areaName": "总部园区",
      "areaLevel": "SITE",
      "address": "北京市朝阳区",
      "locationDesc": "主园区",
      "temperatureThresholdMin": -20.00,
      "temperatureThresholdMax": 20.00,
      "humidityThresholdMin": 30.00,
      "humidityThresholdMax": 70.00,
      "alarmEnabled": 1,
      "status": 1,
      "sortNo": 0,
      "remark": "总部园区",
      "creatorId": 1,
      "updaterId": 1,
      "createTime": "2024-01-01T10:00:00",
      "updateTime": "2024-01-01T10:00:00",
      "children": [...]
    }
  ],
  "code": 200
}
```

### 6.2 移动库区
```
POST /api/areas/{id}/move
Params: targetParentId
```

### 6.3 批量操作
```
POST /api/areas/batch
Body:
{
  "action": "enable|disable|enableAlarm|disableAlarm",
  "ids": [1, 2, 3]
}
Response:
{
  "success": true,
  "message": "批量操作成功",
  "data": {
    "processedCount": 3,
    "successIds": [1, 2, 3],
    "failedIds": []
  },
  "code": 200
}
```

## 7. 验证规则

- 库区编码：唯一，2-50个字符，只能包含大写字母、数字、下划线和横线
- 库区名称：2-100个字符
- 层级：必须是预定义值之一（SITE/WAREHOUSE/FLOOR/AREA/BIN）
- 温湿度阈值：合理范围验证
- 防止循环引用：不能将节点移动到自己的子树中

## 8. 安全措施

- JWT Token认证
- 权限控制（area:view）
- 输入参数验证
- SQL注入防护
- 业务逻辑验证

## 9. 错误处理

系统定义了完整的错误码体系：
- 10005: 库区不存在
- 10011: 库区编码已存在
- 10012: 库区存在子库区，无法删除

## 10. 扩展功能

模块设计考虑了未来扩展需求：
- 拖拽排序
- 高级搜索
- 操作日志
- 数据统计
- 导入导出Excel

该模块通过完整的前后端分离架构，实现了现代化的库区管理功能，为冷链仓储安全管理提供了坚实的基础支撑。