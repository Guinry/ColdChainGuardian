#!/usr/bin/env bash
set -euo pipefail

source /root/.coldchain_mysql_root

mysql --socket=/tmp/mysql.sock -uroot -p"$MYSQL_ROOT_PASSWORD" --batch --raw <<'SQL'
SELECT 'warehouse_areas' AS t, id, area_code, area_name FROM coldchain_guardian.warehouse_areas ORDER BY id;
SELECT 'devices' AS t, id, device_code, area_id FROM coldchain_guardian.devices ORDER BY id;
SELECT 'bad_device_area' AS t, d.id, d.device_code, d.area_id
FROM coldchain_guardian.devices d
LEFT JOIN coldchain_guardian.warehouse_areas w ON w.id = d.area_id
WHERE d.area_id IS NOT NULL AND w.id IS NULL;
SHOW CREATE TABLE coldchain_guardian.work_orders;
SHOW CREATE TABLE coldchain_guardian.devices;
SQL
