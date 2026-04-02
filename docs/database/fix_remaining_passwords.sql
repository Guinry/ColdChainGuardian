USE coldchain_guardian;

-- 更新剩余用户的密码
UPDATE users SET password = '$2a$10$SEtSONkJ5WcF94FeQBYqHe0C2d6GzL2.tZ.zVtQ.z.Za5q6O7hKjq' 
WHERE username IN ('19511687612', 'apitest');

-- 验证所有用户
SELECT id, username, real_name, role, status, 
       CASE WHEN password LIKE '$2a$10$SEtSONkJ5WcF94FeQBYqHe0%' THEN '123456' ELSE 'other' END as pwd_status
FROM users ORDER BY id;
