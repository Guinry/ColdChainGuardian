# 设备管理后端实现文档

## 1. 概述

本文档详细描述了ColdChain Guardian系统的设备管理后端实现，包括设备的增删改查、状态管理、设备分类等功能。

## 2. 数据库表结构

设备表 (devices) 结构如下：

| 字段 | 类型 | 描述 |
|------|------|------|
| id | BIGINT | 主键，自增 |
| device_code | VARCHAR(50) | 设备编码（唯一） |
| device_name | VARCHAR(100) | 设备名称 |
| device_type | VARCHAR(50) | 设备类型（TEMP_HUM / FREEZER / VEHICLE / DOOR ...） |
| model | VARCHAR(50) | 型号 |
| manufacturer | VARCHAR(100) | 厂商 |
| sn | VARCHAR(100) | 序列号 |
| firmware_version | VARCHAR(50) | 固件版本 |
| area_id | BIGINT | 所属库区ID(warehouse_areas.id) |
| location_desc | VARCHAR(200) | 设备位置描述 |
| threshold_mode | VARCHAR(20) | 阈值模式：INHERIT/OVERRIDE |
| temperature_threshold_min | DECIMAL(5,2) | 设备温度下限(覆盖时生效) |
| temperature_threshold_max | DECIMAL(5,2) | 设备温度上限(覆盖时生效) |
| humidity_threshold_min | DECIMAL(5,2) | 设备湿度下限(覆盖时生效) |
| humidity_threshold_max | DECIMAL(5,2) | 设备湿度上限(覆盖时生效) |
| alarm_enabled | TINYINT | 是否启用告警(1是0否) |
| enabled | TINYINT | 启用状态(1启用0禁用) |
| online_status | TINYINT | 在线状态(1在线0离线) |
| last_seen_time | TIMESTAMP | 最后上报/心跳时间 |
| extra | JSON | 扩展信息(JSON) |

## 3. 代码结构

### 3.1 Entity (实体类)
- **文件**: `DeviceEntity.java`
- **路径**: `ccg-infrastructure/src/main/java/com/coldchain/guardian/infra/persistence/entity/DeviceEntity.java`
- **功能**: 映射数据库设备表的实体类，包含所有字段及其注解

### 3.2 Mapper (持久层接口)
- **文件**: `DeviceMapper.java`
- **路径**: `ccg-infrastructure/src/main/java/com/coldchain/guardian/infra/persistence/mapper/DeviceMapper.java`
- **功能**: 继承BaseMapper，提供基础的CRUD操作

### 3.3 Repository (数据访问层)
- **文件**: `DeviceRepository.java`
- **路径**: `ccg-infrastructure/src/main/java/com/coldchain/guardian/infra/persistence/repository/DeviceRepository.java`
- **功能**: 封装对设备数据的访问操作，提供高级查询方法

### 3.4 DTO (数据传输对象)
- **DeviceDto.java**: 设备信息输出DTO
- **CreateDeviceRequestDto.java**: 设备创建/更新请求DTO
- **路径**: `ccg-contract/src/main/java/com/coldchain/guardian/contract/dto/device/`

### 3.5 Service (业务逻辑层)
- **接口**: `DeviceService.java` (定义业务接口)
- **实现**: `DeviceServiceImpl.java` (实现业务逻辑)
- **路径**: `ccg-app/src/main/java/com/coldchain/guardian/app/service/`

### 3.6 Controller (控制器)
- **文件**: `DeviceController.java`
- **路径**: `ccg-app/src/main/java/com/coldchain/guardian/app/controller/DeviceController.java`
- **功能**: 处理HTTP请求，提供RESTful API接口

## 4. API 接口

### 4.1 获取所有设备
- **路径**: `GET /api/devices`
- **功能**: 获取所有设备列表

### 4.2 根据ID获取设备
- **路径**: `GET /api/devices/{id}`
- **功能**: 根据ID获取单个设备信息

### 4.3 根据设备编码获取设备
- **路径**: `GET /api/devices/code/{deviceCode}`
- **功能**: 根据设备编码获取设备信息

### 4.4 根据库区ID获取设备列表
- **路径**: `GET /api/devices/area/{areaId}`
- **功能**: 获取指定库区下的所有设备

### 4.5 根据设备类型获取设备列表
- **路径**: `GET /api/devices/type/{deviceType}`
- **功能**: 获取指定类型的设备列表

### 4.6 根据启用状态获取设备列表
- **路径**: `GET /api/devices/status/{enabled}`
- **功能**: 根据启用状态筛选设备

### 4.7 创建新设备
- **路径**: `POST /api/devices`
- **功能**: 创建新设备

### 4.8 更新设备信息
- **路径**: `PUT /api/devices/{id}`
- **功能**: 更新设备信息

### 4.9 删除设备
- **路径**: `DELETE /api/devices/{id}`
- **功能**: 删除指定设备

### 4.10 启用/禁用设备
- **路径**: `PUT /api/devices/{id}/toggle-status/{enabled}`
- **功能**: 启用或禁用设备

### 4.11 更新设备在线状态
- **路径**: `PUT /api/devices/{id}/online-status/{online}`
- **功能**: 更新设备在线状态

## 5. 主要特性

1. **完整的CRUD操作**: 提供设备的增删改查功能
2. **状态管理**: 支持设备启停和在线状态管理
3. **分类管理**: 支持按设备类型、库区、启用状态等维度查询
4. **数据校验**: 在创建和更新时进行数据验证
5. **异常处理**: 完善的错误处理和响应机制
6. **事务管理**: 在业务层使用@Transactional确保数据一致性

## 6. 使用示例

### 创建设备示例
```json
{
  "deviceCode": "DT001",
  "deviceName": "温度传感器001",
  "deviceType": "TEMP_HUM",
  "model": "TH-01",
  "manufacturer": "Test Corp",
  "areaId": 1,
  "locationDesc": "A栋1层东区",
  "enabled": true,
  "alarmEnabled": true
}
```

### 查询设备示例
```bash
GET /api/devices
GET /api/devices/1
GET /api/devices/area/1
GET /api/devices/type/TEMP_HUM
```