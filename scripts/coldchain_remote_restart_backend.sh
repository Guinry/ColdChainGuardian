#!/usr/bin/env bash
set -euo pipefail

TS=$(date +%F_%H%M%S)
BACKUP="/root/backup/${TS}_coldchain_backend_restart"
mkdir -p "$BACKUP"
chmod 700 "$BACKUP"

[ -f /opt/coldchain-guardian/server/app.jar ] && cp -a /opt/coldchain-guardian/server/app.jar "$BACKUP/app.jar.before"
install -m 0644 /tmp/coldchain-deploy/app.jar /opt/coldchain-guardian/server/app.jar

systemctl restart coldchain-guardian
for i in $(seq 1 60); do
  if curl -fsS http://127.0.0.1:18080/actuator/health >/tmp/coldchain_health.json 2>/tmp/coldchain_health.err; then
    break
  fi
  sleep 2
done

echo "BACKUP=$BACKUP"
echo "SERVICE_STATUS=$(systemctl is-active coldchain-guardian || true)"
echo "HEALTH=$(cat /tmp/coldchain_health.json 2>/dev/null || cat /tmp/coldchain_health.err 2>/dev/null || true)"
