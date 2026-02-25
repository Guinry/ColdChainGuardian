# ColdChain Guardian - 项目规划文档

## 项目概述

ColdChain Guardian是一个集成物联网监控、实时告警、工单管理和大语言模型智能助手的冷链仓储安全管理系统。该系统旨在解决冷链物流中的温湿度异常、设备故障、操作不规范等安全问题，通过数字化和智能化手段提升冷链仓储的安全管理水平。

## 当前项目结构

```
ColdChainGuardian/
├── .git/                          # Git 版本控制
├── .idea/                        # IDE 配置
├── .mvn/                         # Maven wrapper 配置
├── CLAUDE.md                     # 项目说明文档
├── docs/                         # 项目文档
│   ├── DATABASE_SCHEMA.md        # 数据库设计方案
│   ├── database/                 # 数据库相关脚本
│   └── PLAN.md                   # 本文件，项目规划文档
├── coldchain-guardian-server/    # 后端服务
│   ├── .gitignore
│   ├── .mvn/
│   ├── ccg-app/                  # 启动 + Web层 + 应用服务
│   │   ├── pom.xml
│   │   ├── src/main/java/com/coldchain/guardian/app/
│   │   │   ├── CcgApplication.java
│   │   │   ├── config/           # 配置类
│   │   │   │   ├── CorsConfig.java
│   │   │   │   ├── SwaggerConfig.java
│   │   │   │   └── WebSocketConfig.java
│   │   │   ├── controller/       # 控制器层
│   │   │   │   ├── AlertController.java
│   │   │   │   ├── AreaController.java
│   │   │   │   ├── AuthController.java
│   │   │   │   ├── DashboardController.java
│   │   │   │   ├── DeviceController.java
│   │   │   │   ├── TelemetryController.java
│   │   │   │   └── WorkOrderController.java
│   │   │   ├── exception/        # 异常处理
│   │   │   │   ├── BusinessException.java
│   │   │   │   └── GlobalExceptionHandler.java
│   │   │   ├── scheduler/        # 定时任务
│   │   │   │   ├── DailyReportJob.java
│   │   │   │   └── DeviceOfflineDetectionJob.java
│   │   │   ├── security/         # 安全配置
│   │   │   │   ├── JwtConfig.java
│   │   │   │   └── SecurityConfig.java
│   │   │   └── service/          # 业务服务层
│   │   │       ├── AlertService.java
│   │   │       ├── AreaService.java
│   │   │       ├── AuthService.java
│   │   │       ├── DashboardService.java
│   │   │       ├── DeviceService.java
│   │   │       ├── TelemetryService.java
│   │   │       └── WorkOrderService.java
│   ├── ccg-common/               # 通用工具与统一返回
│   │   ├── pom.xml
│   │   └── src/main/java/com/coldchain/guardian/common/
│   │       ├── api/              # API响应封装
│   │       │   ├── ApiResponse.java
│   │       │   └── PageResponse.java
│   │       ├── constant/         # 常量
│   │       │   └── Constants.java
│   │       ├── exception/        # 通用异常
│   │       │   ├── BusinessException.java
│   │       │   └── ErrorCode.java
│   │       └── util/             # 工具类
│   │           └── JsonUtils.java
│   ├── ccg-contract/             # DTO/VO/Enum（对外契约）
│   │   ├── pom.xml
│   │   └── src/main/java/com/coldchain/guardian/contract/
│   │       ├── dto/              # 数据传输对象
│   │       │   ├── alert/        # 告警相关
│   │       │   ├── area/         # 库区相关
│   │       │   ├── auth/         # 认证相关
│   │       │   ├── device/       # 设备相关
│   │       │   ├── telemetry/    # 遥测相关
│   │       │   └── workorder/    # 工单相关
│   │       ├── enums/            # 枚举
│   │       │   ├── AlertSeverity.java
│   │       │   ├── Priority.java
│   │       │   └── WorkOrderStatus.java
│   │       └── vo/               # 视图对象
│   │           └── ApiResponse.java
│   ├── ccg-infrastructure/       # 持久化、外部接入实现
│   │   ├── pom.xml
│   │   └── src/main/java/com/coldchain/guardian/infra/
│   │       ├── llm/              # 大语言模型接入
│   │       │   ├── QwenClient.java
│   │       │   └── QwenConfig.java
│   │       ├── mqtt/             # MQTT协议接入
│   │       │   ├── MqttClient.java
│   │       │   └── MqttConfig.java
│   │       ├── persistence/      # 持久化层
│   │       │   ├── entity/       # 实体类
│   │       │   ├── mapper/       # 数据访问层
│   │       │   └── repository/   # 仓储层
│   │       └── websocket/        # WebSocket支持
│   │           ├── WebSocketConfig.java
│   │           └── WebSocketHandler.java
│   └── pom.xml                   # 主pom文件，定义多模块结构
├── coldchain-guardian-web/       # 前端项目
│   ├── .gitignore
│   ├── package.json
│   ├── package-lock.json
│   ├── public/
│   ├── src/
│   │   ├── App.vue
│   │   ├── main.js
│   │   ├── assets/
│   │   ├── components/
│   │   ├── router/
│   │   ├── store/
│   │   ├── styles/
│   │   ├── utils/
│   │   └── views/
│   └── vite.config.js
├── pom.xml                       # 根pom文件
└── .gitignore
```

## 已完成的功能

### 1. 项目基础结构搭建
- ✅ 创建了多模块Maven项目结构 (ccg-app, ccg-common, ccg-contract, ccg-infrastructure)
- ✅ 配置了基础的pom.xml依赖管理
- ✅ 设计了完整的包结构和分层架构

### 2. 通用组件开发
- ✅ 创建了统一的API响应封装 (ApiResponse, PageResponse)
- ✅ 定义了通用异常处理机制 (BusinessException, GlobalExceptionHandler)
- ✅ 定义了错误码枚举 (ErrorCode)
- ✅ 实现了JSON工具类 (JsonUtils)
- ✅ 创建了常量类 (Constants)

### 3. 数据契约定义
- ✅ 定义了告警相关的DTO (AlertDto, CreateAlertRequestDto)
- ✅ 定义了库区相关的DTO (AreaDto, CreateAreaRequestDto)
- ✅ 定义了认证相关的DTO (LoginRequestDto, LoginResponseDto)
- ✅ 定义了设备相关的DTO (DeviceDto, CreateDeviceRequestDto)
- ✅ 定义了遥测相关的DTO (TelemetryDto, TelemetryUploadRequestDto)
- ✅ 定义了工单相关的DTO (WorkOrderDto, CreateWorkOrderRequestDto)
- ✅ 定义了必要的枚举 (AlertSeverity, Priority, WorkOrderStatus)

### 4. 后端框架搭建
- ✅ 创建了基础的控制器类 (AuthController, AreaController, DeviceController等)
- ✅ 创建了基础的服务类 (AuthService, AreaService, DeviceService等)
- ✅ 配置了安全认证相关组件 (JwtConfig, SecurityConfig)
- ✅ 配置了跨域(CORS)、API文档(Swagger)支持
- ✅ 配置了WebSocket实时通信支持

### 5. 数据持久化层基础
- ✅ 创建了基础实体类 (BaseEntity, UserEntity, DeviceEntity, AreaEntity, AlertEntity, WorkOrderEntity, TelemetryEntity)
- ✅ 创建了数据访问层接口 (UserMapper, DeviceMapper, AreaMapper等)
- ✅ 创建了用户仓储层实现 (UserRepository)

### 6. 扩展功能基础
- ✅ 配置了定时任务框架 (DailyReportJob, DeviceOfflineDetectionJob)
- ✅ 初步配置了MQTT协议接入 (MqttClient, MqttConfig)
- ✅ 初步配置了大语言模型接入 (QwenClient, QwenConfig)

### 7. 前端基础结构
- ✅ 创建了Vue 3前端项目结构
- ✅ 配置了Router、Store (Pinia)、组件结构

## 待完成的功能

### 1. 用户认证与权限管理
- ⏳ 实现用户注册/登录/注销功能
- ⏳ 实现角色管理 (管理员、员工、管理层)
- ⏳ 实现JWT令牌认证
- ⏳ 实现权限控制逻辑

### 2. 库区与设备管理
- ⏳ 实现库区信息维护的完整CRUD操作
- ⏳ 实现传感器设备注册与配置
- ⏳ 实现设备状态监控
- ⏳ 实现设备校准管理

### 3. 实时监控
- ⏳ 实现温湿度数据实时展示
- ⏳ 实现实时图表趋势分析
- ⏳ 实现多时间维度数据查看
- ⏳ 实现设备在线状态监控

### 4. 告警系统
- ⏳ 实现阈值规则配置
- ⏳ 实现异常检测与告警生成功能
- ⏳ 实现告警分级管理
- ⏳ 实现告警处理流程

### 5. 工单管理
- ⏳ 实现告警转工单机制
- ⏳ 实现工单分配与跟踪
- ⏳ 实现处理结果验收
- ⏳ 实现闭环验证

### 6. 巡检与隐患管理
- ⏳ 实现巡检计划制定
- ⏳ 实现巡检记录管理
- ⏳ 实现隐患上报与跟踪
- ⏳ 实现风险评估功能

### 7. AI智能助手
- ⏳ 实现自然语言查询功能
- ⏳ 实现告警总结生成功能
- ⏳ 实现处置建议提供
- ⏳ 实现自动化报告生成功能

### 8. 物联网数据接入
- ⏳ 实现温湿度传感器数据采集
- ⏳ 完善MQTT协议集成
- ⏳ 实现数据预处理与验证

### 9. 前端功能实现
- ⏳ 实现用户认证页面
- ⏳ 实现库区设备管理界面
- ⏳ 实现实时监控可视化界面
- ⏳ 实现告警管理界面
- ⏳ 实现工单处理界面
- ⏳ 实现AI助手交互界面

### 10. 数据库设计与集成
- ⏳ 完善数据库表结构设计
- ⏳ 实现MyBatis-Plus集成
- ⏳ 创建数据库初始化脚本

### 11. 测试与部署
- ⏳ 编写单元测试，确保覆盖率不低于80%
- ⏳ 实现集成测试
- ⏳ 配置CI/CD流程
- ⏳ 准备生产环境部署方案

## 项目进度

根据开题报告计划：
- 2026.2.17-2026.3.16: 完成后端开发与传感器数据接入 (进行中)
- 2026.3.17-2026.4.10: 实现异常预警机制与实时推送 (待开始)
- 2026.4.11-2026.4.25: 完成管理员Web端开发 (待开始)
- 2026.4.26-2026.5.10: 完成员工微信小程序开发 (待开始)
- 2026.5.11-2026.5.20: 接入通义千问大语言模型 (待开始)
- 2026.5.21-2026.5.30: 完成毕业论文撰写 (待开始)

## 项目状态

当前日期: 2026-02-25

项目当前状态为初期开发阶段，已经完成了基础架构搭建，包括多模块项目结构、分层架构设计、基础类定义等工作。接下来需要实现具体业务逻辑，特别是用户认证、设备管理、数据监控等核心功能。