#!/usr/bin/env bash
set -euo pipefail

source /root/.coldchain_mysql_root

mysql --socket=/tmp/mysql.sock -uroot -p"$MYSQL_ROOT_PASSWORD" coldchain_guardian <<'SQL'
UPDATE users
SET password = '$2a$10$S6J2QKf9UPFBxUGSC2Oj7OqF4pr8F7dy8q7/HfGiBljnXQfnEUE6a',
    updated_time = NOW()
WHERE username IN ('admin', 'manager', 'zhangsan', 'lisi');

SELECT username, role, status, LEFT(password, 31) AS hash_prefix
FROM users
ORDER BY id;
SQL
