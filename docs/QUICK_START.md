# ColdChain Guardian - 快速启动指南

## 环境准备

在开始之前，请确保您的开发环境中已安装以下软件：

- **Java 17+**: 用于运行后端Spring Boot应用程序
- **Node.js 20+**: 用于运行前端Vue应用程序
- **MySQL 8.0+**: 用作系统的主数据库
- **Maven 3.8+**: 用于构建后端项目

## 启动步骤

### 1. 数据库初始化

首先，启动MySQL服务并创建项目数据库：

```sql
-- 创建数据库
CREATE DATABASE coldchain_guardian CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 创建数据库用户（可选）
CREATE USER 'ccg_user'@'%' IDENTIFIED BY 'your_secure_password';
GRANT ALL PRIVILEGES ON coldchain_guardian.* TO 'ccg_user'@'%';
FLUSH PRIVILEGES;
```

### 2. 配置后端服务

1. 进入后端目录：
```bash
cd coldchain-guardian-server
```

2. 修改数据库配置文件 `ccg-app/src/main/resources/application.yml`：
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/coldchain_guardian?characterEncoding=utf8&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Shanghai
    username: your_db_username  # 替换为实际用户名
    password: your_db_password  # 替换为实际密码
    driver-class-name: com.mysql.cj.jdbc.Driver
```

3. 启动后端服务：
```bash
# 方法1：使用Maven（推荐用于开发）
./mvnw spring-boot:run

# 方法2：打包后运行
./mvnw clean package
java -jar ccg-app/target/coldchain-guardian-server-0.0.1-SNAPSHOT.jar
```

后端服务默认运行在 `http://localhost:8080`

### 3. 启动前端服务

1. 进入前端目录：
```bash
cd coldchain-guardian-web
```

2. 安装依赖：
```bash
npm install
```

3. 启动开发服务器：
```bash
npm run dev
```

前端服务默认运行在 `http://localhost:3000`

### 4. 访问系统

- **前端界面**: http://localhost:3000
- **后端API文档**: http://localhost:8080/swagger-ui/index.html
- **后端健康检查**: http://localhost:8080/actuator/health

## 核心功能演示

系统启动完成后，您可以体验以下核心功能：

### 设备管理
- **设备列表**: 查看所有设备及其状态
- **添加设备**: 注册新的监控设备
- **编辑设备**: 修改设备配置和阈值

### 实时监控
- **数据图表**: 查看设备的温度、湿度历史趋势
- **设备详情**: 查看设备当前状态和参数

### 告警管理
- **告警列表**: 查看设备产生的告警事件
- **告警处理**: 处理和关闭告警
- **告警统计**: 查看告警分析统计

## API接口速览

### 设备管理API
- `GET /api/devices` - 获取设备列表
- `GET /api/devices/{id}` - 获取设备详情
- `POST /api/devices` - 创建设备
- `PUT /api/devices/{id}` - 更新设备
- `DELETE /api/devices/{id}` - 删除设备

### 设备数据API
- `GET /api/devices/{id}/latest` - 获取设备最新数据
- `GET /api/devices/{id}/data` - 获取设备历史数据

### 告警管理API
- `GET /api/alerts/device/{deviceId}` - 获取设备告警列表
- `PUT /api/alerts/{id}/status` - 更新告警状态

## 故障排查

### 常见问题及解决方案

1. **数据库连接失败**
   - 检查MySQL服务是否运行
   - 确认数据库用户名和密码是否正确
   - 检查数据库URL配置

2. **前端无法连接后端**
   - 确认后端服务已在运行
   - 检查前端的API基础URL配置
   - 确认CORS配置是否正确

3. **登录失败**
   - 确认数据库中已存在用户数据
   - 如需初始化用户，可手动添加或运行初始化脚本

### 日志查看
- 后端日志通常输出到控制台或 `logs` 目录
- 前端错误可在浏览器开发者工具Console中查看

## 开发说明

如果您想参与项目开发，请遵循以下规范：

- **代码风格**: 遵循项目现有的代码风格和命名约定
- **提交信息**: 使用规范的Git提交信息格式
- **分支管理**: 功能开发在 `feature/` 分支，Bug修复在 `bugfix/` 分支
- **测试**: 确保新增功能有相应的单元测试

## 技术架构

- **后端**: Spring Boot 4.0+, Java 17+, MySQL 8.0+, MyBatis-Plus
- **前端**: Vue 3, TypeScript, Element Plus, Pinia
- **通信**: RESTful API, HTTP/HTTPS
- **部署**: 支持Docker容器化部署

---

现在系统已准备就绪，您可以开始使用ColdChain Guardian冷链仓储安全管理系统了！