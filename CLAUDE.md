# ColdChain Guardian - 基于大语言模型的冷链仓储安全管理系统

## 项目概述

ColdChain Guardian是一个集成物联网监控、实时告警、工单管理和大语言模型智能助手的冷链仓储安全管理系统。该系统旨在解决冷链物流中的温湿度异常、设备故障、操作不规范等安全问题，通过数字化和智能化手段提升冷链仓储的安全管理水平。

## 项目目标

1. 构建完整的冷链环境监测体系，实现温湿度数据的实时采集与监控
2. 建立智能预警机制，及时发现并处理潜在安全隐患
3. 实现告警事件到工单处理的完整闭环管理
4. 集成大语言模型，提供智能对话式查询和分析能力
5. 为冷链仓储安全管理提供可落地的工程实践参考

## 系统架构

### 技术栈
- **后端**: Spring Boot 4.0+, Java 17+, MySQL 8.0+
- **前端**: Vue 3, Element Plus, Pinia, Vue Router
- **通信协议**: MQTT for IoT, WebSocket for real-time updates
- **AI模型**: 通义千问大语言模型
- **构建工具**: Maven, Vite
- **数据库**: MyBatis-Plus for ORM

### 系统分层
1. **物联网采集层**: 温湿度传感器 + MQTT协议上传数据
2. **后端服务层**: SpringBoot微服务 + MySQL数据库
3. **前端应用层**: Web管理端 + 微信小程序员工端
4. **智能分析层**: 通义千问大语言模型

## 项目结构

### 后端目录结构
```
coldchain-guardian-server/
├── ccg-app/                         # 启动 + Web层 + 应用服务
│   └── src/main/java/com/coldchain/guardian/app/
│       ├── CcgApplication.java
│       ├── controller/              # REST接口
│       │   ├── AuthController.java
│       │   ├── AreaController.java
│       │   ├── DeviceController.java
│       │   ├── TelemetryController.java
│       │   ├── AlertController.java
│       │   ├── WorkOrderController.java
│       │   └── DashboardController.java
│       ├── service/                 # 业务Service（核心逻辑）
│       │   ├── AuthService.java
│       │   ├── AreaService.java
│       │   ├── DeviceService.java
│       │   ├── TelemetryService.java
│       │   ├── AlertService.java
│       │   ├── WorkOrderService.java
│       │   └── DashboardService.java
│       ├── security/                # JWT + Security配置
│       ├── config/                  # Swagger/CORS/WebSocket配置
│       ├── exception/               # 全局异常、业务异常
│       └── scheduler/               # 定时任务（离线检测、日报）
│
├── ccg-infrastructure/              # MyBatis + DB实体 + 外部接入实现
│   └── src/main/java/com/coldchain/guardian/infra/
│       ├── persistence/
│       │   ├── entity/              # DO/Entity（对应数据库表）
│       │   ├── mapper/              # MyBatis Mapper接口
│       │   └── repository/          # 可选：封装复杂查询
│       ├── mqtt/                    # 后期：MQTT接入
│       ├── websocket/               # 后期：WS推送
│       └── llm/                     # 后期：通义千问封装
│
├── ccg-contract/                    # DTO/VO/Enum（对外契约）
│   └── src/main/java/com/coldchain/guardian/contract/
│       ├── dto/
│       │   ├── auth/
│       │   ├── area/
│       │   ├── device/
│       │   ├── telemetry/
│       │   ├── alert/
│       │   └── workorder/
│       ├── vo/
│       └── enums/
│
└── ccg-common/                      # 通用工具与统一返回
    └── src/main/java/com/coldchain/guardian/common/
        ├── api/                     # ApiResponse、PageResponse
        ├── util/                    # JsonUtils等
        ├── constant/
        └── exception/               # ErrorCode/BusinessException
```

### 前端目录结构
```
coldchain-guardian-web/
├── public/                          # 静态资源
├── src/
│   ├── assets/                      # 静态资源
│   ├── components/                  # 通用组件
│   ├── views/                       # 页面组件
│   ├── router/                      # 路由配置
│   ├── store/                       # 状态管理 (Pinia)
│   ├── utils/                       # 工具函数
│   └── App.vue                      # 根组件
├── package.json                     # 依赖配置
└── vite.config.js                   # 构建配置
```

## 核心功能模块

### 1. 用户认证与权限管理
- 用户注册/登录/注销
- 角色管理 (管理员、员工、管理层)
- JWT令牌认证
- 权限控制

### 2. 库区与设备管理
- 库区信息维护
- 传感器设备注册与配置
- 设备状态监控
- 设备校准管理

### 3. 实时监控
- 温湿度数据实时展示
- 实时图表趋势分析
- 多时间维度数据查看
- 设备在线状态监控

### 4. 告警系统
- 阈值规则配置
- 异常检测与告警生成
- 告警分级管理
- 告警处理流程

### 5. 工单管理
- 告警转工单机制
- 工单分配与跟踪
- 处理结果验收
- 闭环验证

### 6. 巡检与隐患管理
- 巡检计划制定
- 巡检记录管理
- 隐患上报与跟踪
- 风险评估

### 7. AI智能助手
- 自然语言查询
- 告警总结生成
- 处置建议提供
- 自动化报告生成

## 开发指南

### 环境准备
1. 安装Java 17+、Node.js 20+、MySQL 8.0+
2. 配置数据库连接信息
3. 启动Redis缓存服务（可选）
4. 配置通义千问API密钥

### 后端启动
```bash
# 进入后端目录
cd coldchain-guardian-server

# 使用Maven打包并启动
./mvnw spring-boot:run

# 或者打包部署
./mvnw clean package
java -jar target/coldchain-guardian-server-0.0.1-SNAPSHOT.jar
```

### 前端启动
```bash
# 进入前端目录
cd coldchain-guardian-web

# 安装依赖
npm install

# 启动开发服务器
npm run dev
```

### 数据库初始化
```sql
-- 创建数据库
CREATE DATABASE coldchain_guardian CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 执行初始化脚本
SOURCE docs/database/init_db.sql
```

## 代码规范

### Java规范
- 使用驼峰命名法
- 遵循Spring Boot最佳实践
- 统一使用DTO进行数据传输
- 编写单元测试，覆盖率不低于80%

### Vue规范
- 使用Composition API
- 组件名采用PascalCase
- props定义使用完整格式
- 使用TypeScript增强类型安全

### Git规范
- 分支命名：feature/xxx, bugfix/xxx, hotfix/xxx
- 提交信息使用英文，格式："type(scope): description"
- 示例：`feat(user): add user authentication`

## 测试策略

### 单元测试
- 服务层业务逻辑全覆盖
- 数据访问层基本操作验证
- 工具类方法验证

### 集成测试
- API端点功能验证
- 数据库事务测试
- 第三方服务集成测试

### 端到端测试
- 关键用户流程验证
- 前后端联调测试
- 性能和压力测试

## 部署说明

### 生产环境配置
- 配置外部数据库连接
- 设置生产环境API密钥
- 配置SSL证书
- 配置反向代理(Nginx)

### Docker部署
```dockerfile
# 构建镜像
docker build -t coldchain-guardian .

# 运行容器
docker run -d -p 8080:8080 coldchain-guardian
```

## 维护与升级

### 监控
- 应用健康检查
- 数据库性能监控
- API响应时间监控
- 错误日志收集

### 备份策略
- 定期数据库备份
- 配置文件版本管理
- 日志轮转策略

## 项目进度

根据开题报告计划：
- 2026.2.17-2026.3.16: 完成后端开发与传感器数据接入
- 2026.3.17-2026.4.10: 实现异常预警机制与实时推送
- 2026.4.11-2026.4.25: 完成管理员Web端开发
- 2026.4.26-2026.5.10: 完成员工微信小程序开发
- 2026.5.11-2026.5.20: 接入通义千问大语言模型
- 2026.5.21-2026.5.30: 完成毕业论文撰写

## 团队分工

此为个人毕业设计项目，所有功能模块均由单一开发者完成。

## 质量标准

- 代码符合编码规范，通过静态检查
- 单元测试覆盖率达到80%以上
- 系统性能满足预期要求
- 用户界面友好，交互体验良好
- 文档完整，便于后续维护

## 风险与应对

### 技术风险
- AI模型响应不稳定：设计降级方案，保证基础功能
- IoT设备连接不稳定：实现断线重连和本地缓存

### 时间风险
- 进度延误：优先完成核心功能，非关键功能可延后
- 技术难点：提前调研，准备替代方案