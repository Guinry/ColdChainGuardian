# 安全配置建议

## 发现的安全问题

在 `coldchain-guardian-server/ccg-app/src/main/resources/application.yml` 文件中发现了以下安全问题：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/coldchain_guardian?characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai
    username: root
    password: G200410115812  # 明文密码，存在安全隐患
    driver-class-name: com.mysql.cj.jdbc.Driver
  data:
    redis:
      host: 47.104.195.63
      port: 6379
      password: G200410115812  # 明文密码，存在安全隐患
```

## 安全风险

1. **明文密码暴露**：数据库和Redis密码以明文形式存储在配置文件中
2. **源码仓库泄露**：如果配置文件提交到版本控制系统，会导致敏感信息泄露
3. **环境隔离不足**：硬编码的密码难以在不同环境中使用不同的配置

## 安全建议

### 1. 使用环境变量
将敏感信息移至环境变量：

```yaml
spring:
  datasource:
    username: ${DB_USERNAME:root}
    password: ${DB_PASSWORD:}  # 从环境变量读取
  data:
    redis:
      password: ${REDIS_PASSWORD:}  # 从环境变量读取
```

### 2. 使用外部配置文件
创建单独的敏感配置文件并添加到 `.gitignore`：

创建 `config/secrets.yml`：
```yaml
spring:
  datasource:
    password: your_secure_password
  data:
    redis:
      password: your_secure_redis_password
```

### 3. 使用配置中心
对于生产环境，考虑使用配置中心如Spring Cloud Config、Consul或Vault。

### 4. 使用加密配置
使用Jasypt等库对配置文件中的敏感信息进行加密。

## 紧急措施

1. 立即更改当前使用的数据库和Redis密码
2. 将敏感配置项替换为环境变量引用
3. 将包含敏感信息的配置文件加入 `.gitignore`
4. 重新评估访问控制和权限设置

## 部署建议

创建 `.env.example` 文件作为模板：
```
DB_USERNAME=root
DB_PASSWORD=your_secure_password
REDIS_PASSWORD=your_secure_redis_password
JWT_SECRET=your_long_and_secure_jwt_secret
```

## 总结

虽然目前的功能开发工作正常，但为了生产环境的安全，强烈建议实施上述安全措施，避免敏感信息泄露的风险。