# 库区管理API接口文档

## 1. 概述

本文档描述了库区管理模块的API接口规范，包括请求参数、响应格式、错误码等信息。

## 2. 通用规范

### 2.1 基础URL
```
http://localhost:8080/api
```

### 2.2 认证方式
- 所有接口需要Bearer Token认证
- 请求头: `Authorization: Bearer {token}`

### 2.3 通用响应格式
```json
{
  "success": true,
  "message": "操作成功",
  "data": {},
  "code": 200
}
```

### 2.4 通用错误码
| 错误码 | 说明 |
|--------|------|
| 200 | 成功 |
| 400 | 参数错误 |
| 401 | 未授权 |
| 403 | 禁止访问 |
| 404 | 资源不存在 |
| 500 | 服务器内部错误 |

## 3. 业务错误码
| 错误码 | 说明 | 备注 |
|--------|------|------|
| 10005 | 库区不存在 | 库区ID无效或已被删除 |
| 10011 | 库区编码已存在 | areaCode重复 |
| 10012 | 库区存在子库区，无法删除 | 删除前需先删除所有子节点 |

## 4. 接口详情

### 4.1 获取库区树结构
**接口**: `GET /areas`

**描述**: 获取整个库区树结构

**请求参数**: 无

**请求示例**:
```http
GET /api/areas
Authorization: Bearer {token}
```

**响应示例**:
```json
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
      "children": [
        {
          "id": 2,
          "parentId": 1,
          "areaCode": "WH_A",
          "areaName": "A仓库",
          "areaLevel": "WAREHOUSE",
          "address": "园区A区",
          "locationDesc": "A栋仓库",
          "temperatureThresholdMin": -18.00,
          "temperatureThresholdMax": 4.00,
          "humidityThresholdMin": 35.00,
          "humidityThresholdMax": 65.00,
          "alarmEnabled": 1,
          "status": 1,
          "sortNo": 1,
          "remark": "冷链仓库",
          "children": []
        }
      ]
    }
  ],
  "code": 200
}
```

### 4.2 根据ID获取库区详情
**接口**: `GET /areas/{id}`

**描述**: 获取指定库区的详细信息

**路径参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | integer | 是 | 库区ID |

**请求示例**:
```http
GET /api/areas/1
Authorization: Bearer {token}
```

**响应示例**:
```json
{
  "success": true,
  "message": "操作成功",
  "data": {
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
    "updateTime": "2024-01-01T10:00:00"
  },
  "code": 200
}
```

### 4.3 获取子库区列表
**接口**: `GET /areas/parent/{parentId}`

**描述**: 获取指定父库区的子库区列表

**路径参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| parentId | integer | 是 | 父库区ID |

**请求示例**:
```http
GET /api/areas/parent/1
Authorization: Bearer {token}
```

**响应示例**:
```json
{
  "success": true,
  "message": "操作成功",
  "data": [
    {
      "id": 2,
      "parentId": 1,
      "areaCode": "WH_A",
      "areaName": "A仓库",
      "areaLevel": "WAREHOUSE",
      "address": "园区A区",
      "locationDesc": "A栋仓库",
      "temperatureThresholdMin": -18.00,
      "temperatureThresholdMax": 4.00,
      "humidityThresholdMin": 35.00,
      "humidityThresholdMax": 65.00,
      "alarmEnabled": 1,
      "status": 1,
      "sortNo": 1,
      "remark": "冷链仓库"
    }
  ],
  "code": 200
}
```

### 4.4 创建库区
**接口**: `POST /areas`

**描述**: 创建新的库区

**请求参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| parentId | integer | 否 | 父库区ID，顶级库区设为null |
| areaCode | string | 是 | 库区编码，2-50个字符，只能包含大写字母、数字、下划线和横线 |
| areaName | string | 是 | 库区名称，2-100个字符 |
| areaLevel | string | 是 | 库区层级：SITE/WAREHOUSE/FLOOR/AREA/BIN |
| address | string | 否 | 地址，仅SITE/WAREHOUSE可填 |
| locationDesc | string | 否 | 位置描述 |
| temperatureThresholdMin | number | 否 | 温度最小阈值，默认-20.00 |
| temperatureThresholdMax | number | 否 | 温度最大阈值，默认8.00 |
| humidityThresholdMin | number | 否 | 湿度最小阈值，默认30.00 |
| humidityThresholdMax | number | 否 | 湿度最大阈值，默认70.00 |
| alarmEnabled | integer | 否 | 是否启用告警，1-启用，0-禁用，默认1 |
| status | integer | 否 | 启用状态，1-启用，0-禁用，默认1 |
| sortNo | integer | 否 | 排序号，默认0 |
| remark | string | 否 | 备注 |

**请求示例**:
```http
POST /api/areas
Content-Type: application/json
Authorization: Bearer {token}

{
  "parentId": 1,
  "areaCode": "F1_A",
  "areaName": "一楼A区",
  "areaLevel": "FLOOR",
  "locationDesc": "A仓库一楼东侧",
  "temperatureThresholdMin": -15.00,
  "temperatureThresholdMax": 2.00,
  "humidityThresholdMin": 40.00,
  "humidityThresholdMax": 60.00,
  "alarmEnabled": 1,
  "status": 1,
  "sortNo": 1,
  "remark": "冷冻区"
}
```

**响应示例**:
```json
{
  "success": true,
  "message": "创建成功",
  "data": {
    "id": 3,
    "parentId": 1,
    "areaCode": "F1_A",
    "areaName": "一楼A区",
    "areaLevel": "FLOOR",
    "locationDesc": "A仓库一楼东侧",
    "temperatureThresholdMin": -15.00,
    "temperatureThresholdMax": 2.00,
    "humidityThresholdMin": 40.00,
    "humidityThresholdMax": 60.00,
    "alarmEnabled": 1,
    "status": 1,
    "sortNo": 1,
    "remark": "冷冻区"
  },
  "code": 200
}
```

### 4.5 更新库区
**接口**: `PUT /areas/{id}`

**描述**: 更新库区信息

**路径参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | integer | 是 | 库区ID |

**请求参数**: 与创建库区相同

**请求示例**:
```http
PUT /api/areas/3
Content-Type: application/json
Authorization: Bearer {token}

{
  "id": 3,
  "parentId": 1,
  "areaCode": "F1_A",
  "areaName": "一楼A区（已更新）",
  "areaLevel": "FLOOR",
  "locationDesc": "A仓库一楼东侧",
  "temperatureThresholdMin": -15.00,
  "temperatureThresholdMax": 2.00,
  "humidityThresholdMin": 40.00,
  "humidityThresholdMax": 60.00,
  "alarmEnabled": 0,
  "status": 1,
  "sortNo": 1,
  "remark": "冷冻区"
}
```

**响应示例**:
```json
{
  "success": true,
  "message": "更新成功",
  "data": {
    "id": 3,
    "parentId": 1,
    "areaCode": "F1_A",
    "areaName": "一楼A区（已更新）",
    "areaLevel": "FLOOR",
    "locationDesc": "A仓库一楼东侧",
    "temperatureThresholdMin": -15.00,
    "temperatureThresholdMax": 2.00,
    "humidityThresholdMin": 40.00,
    "humidityThresholdMax": 60.00,
    "alarmEnabled": 0,
    "status": 1,
    "sortNo": 1,
    "remark": "冷冻区"
  },
  "code": 200
}
```

### 4.6 删除库区
**接口**: `DELETE /areas/{id}`

**描述**: 删除库区（软删除，实际为禁用操作）

**路径参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | integer | 是 | 库区ID |

**请求示例**:
```http
DELETE /api/areas/3
Authorization: Bearer {token}
```

**响应示例**:
```json
{
  "success": true,
  "message": "删除成功",
  "data": null,
  "code": 200
}
```

### 4.7 移动库区
**接口**: `POST /areas/{id}/move`

**描述**: 移动库区到另一个父库区下

**路径参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | integer | 是 | 要移动的库区ID |

**请求参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| targetParentId | integer | 是 | 目标父库区ID |

**请求示例**:
```http
POST /api/areas/3/move
Content-Type: application/json
Authorization: Bearer {token}

{
  "targetParentId": 2
}
```

**响应示例**:
```json
{
  "success": true,
  "message": "移动成功",
  "data": null,
  "code": 200
}
```

### 4.8 批量操作
**接口**: `POST /areas/batch`

**描述**: 批量执行操作（启用/禁用/开启/关闭告警等）

**请求参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| action | string | 是 | 操作类型：enable/disable/enableAlarm/disableAlarm |
| ids | array | 是 | 库区ID数组 |

**请求示例**:
```http
POST /api/areas/batch
Content-Type: application/json
Authorization: Bearer {token}

{
  "action": "enable",
  "ids": [3, 4, 5]
}
```

**响应示例**:
```json
{
  "success": true,
  "message": "批量操作成功",
  "data": {
    "processedCount": 3,
    "successIds": [3, 4, 5],
    "failedIds": []
  },
  "code": 200
}
```

## 5. 前端调用示例

### JavaScript/Axios调用示例
```javascript
import { areaApi } from '@/api/area';

// 获取库区树
async function getAreaTree() {
  try {
    const response = await areaApi.getAreaTree();
    return response.data;
  } catch (error) {
    console.error('获取库区树失败:', error);
    throw error;
  }
}

// 创建库区
async function createArea(areaData) {
  try {
    const response = await areaApi.createArea(areaData);
    return response.data;
  } catch (error) {
    console.error('创建库区失败:', error);
    throw error;
  }
}

// 更新库区
async function updateArea(id, areaData) {
  try {
    const response = await areaApi.updateArea(id, areaData);
    return response.data;
  } catch (error) {
    console.error('更新库区失败:', error);
    throw error;
  }
}
```