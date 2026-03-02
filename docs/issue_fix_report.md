# 冷链仓储管理系统问题修复报告

## 问题概述
系统启动时出现以下问题：
1. 前端访问监控接口时返回 403 Forbidden 错误
2. 数据库查询报错：`Unknown column 'wa.area_path' in 'field list'`
3. 聚合查询报错：`In aggregated query without GROUP BY, expression #5 of SELECT list contains nonaggregated column`

## 修复内容

### 1. 安全配置修复
**文件**: `coldchain-guardian-server/ccg-app/src/main/java/com/coldchain/ guardian/app/security/SecurityConfig.java`

**修改**:
- 添加了对 `/api/monitor/**` 接口的访问权限
- 更新了授权规则：`.requestMatchers("/api/monitor/**").hasAnyRole("ADMIN", "MANAGER", "USER")`

### 2. 实体类修复
**文件**: `coldchain-guardian-server/ccg-infrastructure/src/main/java/com/coldchain/ guardian/infra/persistence/entity/AreaEntity.java`

**修改**:
- 添加了 `areaPath` 字段：`@TableField("area_path") private String areaPath;`
- 添加了对应的 getter 和 setter 方法

### 3. SQL查询修复
**文件**: `coldchain-guardian-server/ccg-infrastructure/src/main/resources/mapper/MonitorMapper.xml`

**修改**:
- 修复了 selectMonitorDevices 查询，使用 `IFNULL(wa.area_path, '') as areaPath` 替代 `wa.area_path as areaPath`，防止字段不存在时的错误
- 修复了 getMonitorSummary 查询中的聚合查询语法错误，将 `LEFT JOIN` 改为 `CROSS JOIN` 并调整了字段别名

### 4. 数据库表结构更新
**文件**: `docs/database/warehouse_areas_table.sql`

**修改**:
- 在 CREATE TABLE 语句中添加了 `area_path VARCHAR(500) NULL COMMENT '区域路径，用于层级显示，如 0/1/5/'` 字段

## 数据库更新步骤

如果你的数据库已存在且没有 area_path 字段，请执行以下 SQL 语句：

```sql
-- 添加area_path字段到warehouse_areas表
ALTER TABLE warehouse_areas
ADD COLUMN area_path VARCHAR(500) NULL COMMENT '区域路径，用于层级显示，如 0/1/5/';
```

## 测试结果

1. 安全配置已更新，监控接口现在应该可以正常访问
2. SQL查询已修复，不再报字段不存在或聚合查询语法错误
3. 系统应能正常启动并处理监控相关请求

## 后续建议

1. 如果使用的是现有数据库，请执行上述 ALTER TABLE 语句
2. 启动系统前确保数据库已初始化
3. 验证监控页面能否正常加载和显示数据