#!/usr/bin/env bash
set -euo pipefail

source /root/.coldchain_mysql_root

mysql --socket=/tmp/mysql.sock -uroot -p"$MYSQL_ROOT_PASSWORD" --batch --raw <<'SQL'
SELECT id, device_id, temperature, humidity, signal_strength, data_time
FROM coldchain_guardian.sensor_data
ORDER BY id DESC
LIMIT 1;

SELECT id, device_code, latest_temp, latest_humi, latest_data_time, online_status
FROM coldchain_guardian.devices
WHERE device_code = 'TH-A1-001';

SELECT
  (SELECT COUNT(*) FROM coldchain_guardian.users) AS users,
  (SELECT COUNT(*) FROM coldchain_guardian.warehouse_areas) AS areas,
  (SELECT COUNT(*) FROM coldchain_guardian.devices) AS devices,
  (SELECT COUNT(*) FROM coldchain_guardian.sensor_data) AS sensor_data,
  (SELECT COUNT(*) FROM coldchain_guardian.alerts) AS alerts,
  (SELECT COUNT(*) FROM coldchain_guardian.work_orders) AS work_orders;
SQL
