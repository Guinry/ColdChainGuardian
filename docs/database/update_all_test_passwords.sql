USE coldchain_guardian;

-- 更新所有测试用户的密码为 123456 的 BCrypt 哈希
-- BCrypt hash for "123456": $2a$10$S6J2QKf9UPFBxUGSC2Oj7OqF4pr8F7dy8q7/HfGiBljnXQfnEUE6a

UPDATE users SET password = '$2a$10$S6J2QKf9UPFBxUGSC2Oj7OqF4pr8F7dy8q7/HfGiBljnXQfnEUE6a'
WHERE username IN ('admin', 'manager', 'zhangsan', 'lisi', 'root', 'manager01', 'staff01', 'staff02', 'testuser');

-- 验证更新
SELECT id, username, real_name, role, status, LEFT(password, 30) as pwd_hash FROM users ORDER BY id;
