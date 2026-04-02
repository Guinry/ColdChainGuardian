USE coldchain_guardian;

-- 更新所有测试用户的密码为 123456 的 BCrypt 哈希
-- BCrypt hash for "123456": $2a$10$SEtSONkJ5WcF94FeQBYqHe0C2d6GzL2.tZ.zVtQ.z.Za5q6O7hKjq

UPDATE users SET password = '$2a$10$SEtSONkJ5WcF94FeQBYqHe0C2d6GzL2.tZ.zVtQ.z.Za5q6O7hKjq' 
WHERE username IN ('admin', 'root', 'manager01', 'staff01', 'staff02', 'testuser');

-- 验证更新
SELECT id, username, real_name, role, status, LEFT(password, 30) as pwd_hash FROM users ORDER BY id;
