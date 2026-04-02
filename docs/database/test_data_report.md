# 数据库测试数据验证报告

**生成时间：** 2026-03-19 17:05  
**数据库：** coldchain_guardian (MySQL 8.0.42)

---

## ✅ 1. Admin 用户验证

| 项目 | 状态 | 详情 |
|------|------|------|
| **用户名** | ✅ 存在 | `admin` |
| **用户 ID** | ✅ | `1` |
| **角色** | ✅ | `ADMIN` |
| **密码** | ✅ 已更新 | `123456` (BCrypt 加密) |
| **邮箱** | ✅ | `admin@coldchain-guardian.com` |
| **状态** | ✅ | `启用 (1)` |
| **真实姓名** | ✅ | `Administrator` |

**密码哈希：** `$2a$10$SEtSONkJ5WcF94FeQBYqHe0C2d6GzL2.tZ.zVtQ.z.Za5q6O7hKjq`

---

## 👥 2. 用户角色和权限

### 角色分布

| 角色 | 人数 | 用户列表 | 权限说明 |
|------|------|----------|----------|
| **ADMIN** | 1 | admin | 系统管理员，拥有所有权限 |
| **MANAGER** | 2 | root, manager01 | 经理角色，可管理库区、设备、工单 |
| **STAFF** | 2 | staff01, staff02 | 普通员工，可查看和处理工单 |
| **STOCK_MANAGER** | 1 | 19511687612 | 库存管理员，管理库存相关 |
| **USER** | 2 | testuser, apitest | 普通用户，基础查看权限 |

**总计：8 个测试用户**

### 安全配置说明

根据 `SecurityConfig.java` 配置：

| API 端点 | 允许角色 |
|----------|----------|
| `/api/auth/**` | 所有 (登录接口) |
| `/api/ai-assistant/**` | ADMIN, MANAGER, USER |
| `/api/monitor/**` | ADMIN, MANAGER, USER |
| `/api/areas/**` | ADMIN, MANAGER, USER |
| `/api/devices/**` | ADMIN, MANAGER, USER |
| `/api/work-orders/**` | ADMIN, MANAGER, USER |
| `/api/alerts/**` | ADMIN, MANAGER, USER |
| `/api/dashboard/**` | ADMIN, MANAGER, USER |

---

## 📊 3. 测试数据准备情况

### 数据总览

| 数据类型 | 数量 | 状态 | 说明 |
|----------|------|------|------|
| **用户数据** | 8 条 | ✅ | admin, root, manager01, staff01, staff02 等 |
| **库区数据** | 8 条 | ✅ | SITE/WAREHOUSE/AREA/BIN 完整层级 |
| **设备数据** | 13 条 | ✅ | TEMP_HUM, DOOR_SENSOR, FREEZER, VEHICLE |
| **告警数据** | 22 条 | ✅ | 5 种告警类型，4 种状态 |
| **工单数据** | 12 条 | ✅ | ALERT_FIX, INSPECTION, MAINTENANCE |
| **工单日志** | 18 条 | ✅ | 工单流转记录 |

### 库区层级结构

```
SITE-HD (华东物流中心)
├── WH-A (A 栋医药库)
│   ├── AREA-COOL (2~8℃冷藏库)
│   └── AREA-FREEZE (-20℃冷冻库)
├── WH-B (B 栋医药库)
│   └── AREA-TEMP (常温库区)
│       ├── BIN-A01 (A01 货架)
│       └── BIN-A02 (A02 货架)
```

### 设备类型分布

| 设备类型 | 数量 | 示例 |
|----------|------|------|
| TEMP_HUM (温湿度探头) | 8 | TH-C-001, TH-F-001, TH-B-001 |
| DOOR_SENSOR (门磁) | 3 | DOOR-001, DOOR-002, DOOR-003 |
| FREEZER (超低温冰柜) | 1 | FREEZER-001 |
| VEHICLE (冷藏车) | 1 | VEHICLE-001 |

### 告警类型分布

| 告警类型 | 数量 | 说明 |
|----------|------|------|
| TEMP_HIGH (温度过高) | 5 | 温度超过上限 |
| TEMP_LOW (温度过低) | 3 | 温度低于下限 |
| HUMI_HIGH (湿度过高) | 4 | 湿度超过上限 |
| HUMI_LOW (湿度过低) | 3 | 湿度低于下限 |
| DEVICE_OFFLINE (设备离线) | 5 | 设备通讯中断 |
| **其他告警** | 2 | 历史告警 |

### 工单状态分布

| 状态 | 数量 | 说明 |
|------|------|------|
| PENDING (待处理) | 3 | 等待分配或处理 |
| PROCESSING (处理中) | 3 | 正在处理 |
| COMPLETED (已完成) | 6 | 已完成并关闭 |

---

## 🔍 4. 数据验证查询

### 示例查询 1：用户登录验证
```sql
SELECT username, role, status FROM users WHERE username = 'admin';
-- 预期结果：admin | ADMIN | 1
```

### 示例查询 2：库区层级查询
```sql
SELECT id, area_code, area_name, area_level, parent_id FROM warehouse_areas ORDER BY id;
-- 预期结果：8 条记录，完整的 SITE→WAREHOUSE→AREA→BIN 层级
```

### 示例查询 3：设备状态查询
```sql
SELECT device_code, device_name, online_status, area_id FROM devices LIMIT 5;
-- 预期结果：设备在线/离线状态，关联库区
```

### 示例查询 4：告警统计查询
```sql
SELECT alert_type, status, COUNT(*) as cnt FROM alerts GROUP BY alert_type, status;
-- 预期结果：各类型告警的处理状态统计
```

### 示例查询 5：工单关联查询
```sql
SELECT w.order_no, w.title, w.status, u.real_name as assignee, a.area_name 
FROM work_orders w 
LEFT JOIN users u ON w.assigned_to = u.id 
LEFT JOIN warehouse_areas a ON w.warehouse_id = a.id;
-- 预期结果：工单与处理人、库区的关联信息
```

---

## ✅ 5. 验证结果总结

| 检查项 | 状态 | 说明 |
|--------|------|------|
| **数据库连接** | ✅ 成功 | MySQL 8.0.42 - coldchain_guardian |
| **Admin 用户** | ✅ 存在 | 用户名 admin，密码 123456 (BCrypt) |
| **用户角色** | ✅ 完整 | ADMIN, MANAGER, STAFF, USER 等 5 种角色 |
| **表结构** | ✅ 完整 | 10 个核心表，外键约束正常 |
| **测试数据** | ✅ 充足 | 所有数据类型满足测试要求 |
| **数据关联** | ✅ 正常 | 联表查询验证通过 |

---

## 📁 6. SQL 脚本文件

以下 SQL 脚本已保存至 `C:\Users\23869\Desktop\ColdChainGuardian\docs\database\`：

| 文件名 | 用途 |
|--------|------|
| `coldchain_guardian.sql` | 完整数据库结构 |
| `test_data_simple.sql` | 测试数据插入脚本 |
| `update_admin_password.sql` | Admin 密码更新 |
| `update_all_test_passwords.sql` | 批量密码更新 |
| `init_db.sql` | 数据库初始化脚本 |

---

## 🔐 7. 测试账号清单

所有测试账号密码均为：`123456`

| 用户名 | 密码 | 角色 | 说明 |
|--------|------|------|------|
| admin | 123456 | ADMIN | 系统管理员 |
| root | 123456 | MANAGER | 经理账号 |
| manager01 | 123456 | MANAGER | 经理账号 2 |
| staff01 | 123456 | STAFF | 员工账号 1 |
| staff02 | 123456 | STAFF | 员工账号 2 |
| testuser | 123456 | USER | 普通用户 |
| apitest | 123456 | USER | API 测试用户 |
| 19511687612 | 123456 | STOCK_MANAGER | 库存管理员 |

---

**报告生成完成** ✅  
所有测试数据已准备就绪，可以进行功能测试！
