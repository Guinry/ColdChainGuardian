#!/usr/bin/env bash
set -euo pipefail

TS=$(date +%F_%H%M%S)
BACKUP="/root/backup/${TS}_coldchain_web_only"
WEB_ROOT="/www/wwwroot/coldchain-guardian-web"

mkdir -p "$BACKUP"
chmod 700 "$BACKUP"

[ -d "$WEB_ROOT" ] && tar -czf "$BACKUP/web-root.tar.gz" -C /www/wwwroot coldchain-guardian-web

rm -rf "${WEB_ROOT}.new"
mkdir -p "${WEB_ROOT}.new"
unzip -q /tmp/coldchain-web-dist-favicon.zip -d "${WEB_ROOT}.new"

rm -rf "${WEB_ROOT}.old"
[ -d "$WEB_ROOT" ] && mv "$WEB_ROOT" "${WEB_ROOT}.old"
mv "${WEB_ROOT}.new" "$WEB_ROOT"
chown -R root:root "$WEB_ROOT"

nginx -t
systemctl reload nginx

echo "BACKUP=$BACKUP"
echo "TITLE=$(grep -o '<title>[^<]*</title>' "$WEB_ROOT/index.html" | head -1)"
echo "FAVICON=$(test -f "$WEB_ROOT/favicon.svg" && echo exists || echo missing)"
