USE coldchain_guardian;

-- 更新 admin 用户密码为 123456 的 BCrypt 哈希
UPDATE users SET password = '$2a$10$SEtSONkJ5WcF94FeQBYqHe0C2d6GzL2.tZ.zVtQ.z.Za5q6O7hKjq' WHERE username = 'admin';

-- 验证更新
SELECT username, role, password FROM users WHERE username = 'admin';
