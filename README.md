# ColdChain Guardian - 冷链仓储安全管理系统

[![License](https://img.shields.io/badge/license-Apache%202.0-blue.svg)](LICENSE)
[![Java](https://img.shields.io/badge/language-Java-orange.svg)](https://www.oracle.com/java/)
[![Vue](https://img.shields.io/badge/framework-Vue-brightgreen.svg)](https://vuejs.org/)

基于大语言模型的冷链仓储安全管理系统，集成物联网监控、实时告警、工单管理和AI智能助手，解决冷链物流中的温湿度异常、设备故障、操作不规范等安全问题。

## 项目简介

ColdChain Guardian 是一个专门为冷链仓储设计的智能化安全管理系统。系统利用物联网技术实现实时监控，结合人工智能技术提供智能分析，形成从设备监控、异常预警、工单处理到智能辅助的完整解决方案，有效提升冷链仓储的安全性和运营效率。

## 功能特性

### 🌡️ 实时监控
- 温湿度数据实时采集与监控
- 多库区环境参数集中显示
- 实时数据图表趋势分析
- 设备在线状态监控

### 🔔 智能告警
- 多级告警阈值配置
- 异常自动检测与通知
- 告警事件分类管理
- 告警历史记录追溯

### 📋 工单管理
- 告警自动转工单机制
- 工单智能分配与跟踪
- 处理过程全程记录
- 闭环验证确保质量

### 👥 多角色权限
- 管理员：全面系统管理权限
- 操作员：日常监控操作权限
- 巡检员：设备巡检执行权限
- 管理层：报表分析查看权限

### 🤖 AI智能助手
- 自然语言查询系统数据
- 智能故障诊断建议
- 自动化报告生成
- 风险预警分析

### 📱 移动端支持
- 微信小程序员工端
- 现场操作便捷高效
- 实时消息推送提醒
- 离线数据同步机制

## 技术架构

### 后端技术栈
- **框架**: Spring Boot 4.0+
- **语言**: Java 17+
- **数据库**: MySQL 8.0+, MyBatis-Plus
- **消息协议**: MQTT for IoT, WebSocket for实时通信
- **AI模型**: 通义千问大语言模型
- **构建工具**: Maven

### 前端技术栈
- **框架**: Vue 3, TypeScript
- **UI组件**: Element Plus
- **状态管理**: Pinia
- **路由**: Vue Router
- **构建工具**: Vite

### 系统分层
```
┌─────────────────────────────────────┐
│           AI智能分析层              │
│    (通义千问大语言模型)             │
├─────────────────────────────────────┤
│          前端应用层                 │
│    (Web管理端 + 微信小程序)         │
├─────────────────────────────────────┤
│          后端服务层                 │
│    (SpringBoot + MySQL)            │
├─────────────────────────────────────┤
│         物联网采集层                │
│    (温湿度传感器 + MQTT)           │
└─────────────────────────────────────┘
```

## 快速开始

### 环境准备

1. 安装必要软件
```bash
# Java 17+
java -version

# Node.js 20+
node --version

# MySQL 8.0+
mysql --version

# Maven
mvn -v
```

2. 克隆项目
```bash
git clone https://github.com/yourusername/ColdChainGuardian.git
cd ColdChainGuardian
```

### 后端启动

1. 数据库初始化
```sql
-- 创建数据库
CREATE DATABASE coldchain_guardian CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 执行初始化脚本
SOURCE docs/database/init_db.sql
```

2. 配置环境变量
```bash
# 在 coldchain-guardian-server/src/main/resources/application.yml 中配置数据库连接信息
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/coldchain_guardian?characterEncoding=utf8&useSSL=false&serverTimezone=GMT%2B8
    username: your_username
    password: your_password
```

3. 启动后端服务
```bash
# 进入后端目录
cd coldchain-guardian-server

# 启动服务 (推荐使用IDE或直接运行CcgApplication)
./mvnw spring-boot:run

# 或打包后运行
./mvnw clean package
java -jar target/coldchain-guardian-server-0.0.1-SNAPSHOT.jar
```

### 前端启动

1. 安装依赖
```bash
# 进入前端目录
cd coldchain-guardian-web

# 安装依赖
npm install
```

2. 启动开发服务器
```bash
# 启动开发服务器
npm run dev

# 构建生产版本
npm run build
```

### 配置说明

1. AI模型配置
在配置文件中设置通义千问API密钥:
```yaml
qwen:
  api-key: your-api-key
  model: qwen-max
```

2. MQTT配置
```yaml
mqtt:
  broker-url: tcp://localhost:1883
  client-id: coldchain-guardian-server
  username: your-username
  password: your-password
```

## 项目结构

```
ColdChainGuardian/
├── docs/                          # 项目文档
│   ├── DATABASE_SCHEMA.md         # 数据库设计文档
│   ├── PLAN.md                    # 项目规划文档
│   └── database/                  # 数据库脚本
├── coldchain-guardian-server/     # 后端服务
│   ├── ccg-app/                   # 应用层 (Controller, Service)
│   ├── ccg-common/                # 通用工具
│   ├── ccg-contract/              # 数据契约 (DTO, Enum)
│   └── ccg-infrastructure/        # 基础设施 (Entity, Mapper)
├── coldchain-guardian-web/        # 前端应用
│   ├── src/                       # 源代码
│   ├── public/                    # 静态资源
│   └── config/                    # 构建配置
└── README.md                      # 项目说明
```

## API文档

启动服务后，可通过以下地址访问API文档：
- Swagger UI: http://localhost:8080/swagger-ui/index.html
- API文档: http://localhost:8080/api-docs

## 开发指南

### 代码规范

#### Java规范
- 遵循阿里巴巴Java开发手册
- 使用驼峰命名法
- 统一使用DTO进行数据传输
- Controller层接收请求参数，Service层处理业务逻辑，Repository层负责数据访问

#### Vue规范
- 使用Composition API
- 组件名采用PascalCase
- props定义使用完整格式
- 使用TypeScript增强类型安全

#### Git规范
- 分支命名：`feature/xxx`, `bugfix/xxx`, `hotfix/xxx`
- 提交信息格式："type(scope): description"
- 示例：`feat(user): add user authentication`

### 核心业务流程

1. **设备注册流程**
   设备注册 → 分配设备ID → 关联库区 → 激活设备 → 状态更新

2. **数据监控流程**
   传感器采集 → MQTT上传 → 数据解析 → 存储入库 → 实时展示

3. **告警处理流程**
   参数异常 → 触发告警 → 生成工单 → 分配处理人 → 跟踪处理 → 验收闭环

4. **AI分析流程**
   用户提问 → 语义理解 → 数据查询 → 结果分析 → 智能回答

## 测试策略

### 单元测试
- 服务层业务逻辑全覆盖
- 数据访问层基本操作验证
- 工具类方法验证
- 目标：覆盖率不低于80%

### 集成测试
- API端点功能验证
- 数据库事务测试
- 第三方服务集成测试

### 端到端测试
- 关键用户流程验证
- 前后端联调测试

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
docker run -d \
  -p 8080:8080 \
  -e SPRING_DATASOURCE_URL=jdbc:mysql://mysql:3306/coldchain_guardian \
  -e SPRING_DATASOURCE_USERNAME=user \
  -e SPRING_DATASOURCE_PASSWORD=password \
  coldchain-guardian
```

## 维护与升级

### 监控指标
- 应用健康状态
- 数据库连接池
- API响应时间
- 错误率统计

### 备份策略
- 定期数据库备份
- 配置文件版本管理
- 日志轮转策略

## 贡献指南

我们欢迎社区贡献！

1. Fork 项目
2. 创建功能分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送分支 (`git push origin feature/AmazingFeature`)
5. 开启 Pull Request

## 许可证

本项目采用 Apache License 2.0 许可证。详情请参见 [LICENSE](LICENSE) 文件。

## 联系方式

如有任何问题，请通过以下方式联系我们：
- 项目 Issues: [GitHub Issues](https://github.com/yourusername/ColdChainGuardian/issues)
- 邮箱: your-email@example.com

## 致谢

感谢以下开源项目的支持：
- [Spring Boot](https://spring.io/projects/spring-boot)
- [Vue.js](https://vuejs.org/)
- [Element Plus](https://element-plus.org/)
- [MyBatis-Plus](https://baomidou.com/)

---
<p align="center">ColdChain Guardian - 让冷链更安全，让管理更智能</p>