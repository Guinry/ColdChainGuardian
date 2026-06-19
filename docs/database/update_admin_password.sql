USE coldchain_guardian;

-- 更新 admin 用户密码为 123456 的 BCrypt 哈希
UPDATE users SET password = '$2a$10$S6J2QKf9UPFBxUGSC2Oj7OqF4pr8F7dy8q7/HfGiBljnXQfnEUE6a' WHERE username = 'admin';

-- 验证更新
SELECT username, role, password FROM users WHERE username = 'admin';
