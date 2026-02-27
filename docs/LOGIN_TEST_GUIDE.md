# 登录功能验证步骤

## 前提条件
确保数据库中存在users表和用户数据：
```sql
-- 检查表是否存在
DESCRIBE users;

-- 检查用户数据
SELECT id, username, role, status, created_time FROM users LIMIT 5;
```

## 启动应用进行测试
```bash
# 在项目根目录下执行
cd coldchain-guardian-server
./mvnw spring-boot:run
```

## 测试登录API
```bash
# 测试登录（假设数据库中有用户名为'admin'的用户）
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "admin123"
  }'
```

预期响应应包含JWT令牌：
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "userId": 1,
  "username": "admin"
}
```

## 验证令牌有效性
```bash
# 使用获取的令牌访问受保护的API
curl -X GET http://localhost:8080/api/some-protected-endpoint \
  -H "Authorization: Bearer YOUR_JWT_TOKEN_HERE"
```

## 常见问题排查
1. 如果收到 "无效的凭证"：
   - 确认用户名和密码是否正确
   - 确认用户状态是否为启用（status=1）

2. 如果收到 "令牌生成失败"：
   - 检查 application.yml 中的 jwt.secret 配置
   - 确保密钥长度足够

3. 如果收到 "用户不存在"：
   - 确认数据库中存在对应的用户
```