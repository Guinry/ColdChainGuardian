#!/usr/bin/env bash
set -euo pipefail

DOMAIN="coldchain.guinry.cn"
WEB_ROOT="/www/wwwroot/coldchain-guardian-web"
NGINX_CONF="/www/server/nginx/conf/vhost/nginx/${DOMAIN}.conf"
SSL_DIR="/etc/coldchain-guardian/ssl"
ACME="/root/.acme.sh/acme.sh"
TS=$(date +%F_%H%M%S)
BACKUP="/root/backup/${TS}_coldchain_https"

mkdir -p "$BACKUP" "$SSL_DIR"
chmod 700 "$BACKUP" "$SSL_DIR"
[ -f "$NGINX_CONF" ] && cp -a "$NGINX_CONF" "$BACKUP/${DOMAIN}.conf.before_https" || true

if [ ! -x "$ACME" ]; then
  curl -fsSL https://get.acme.sh | sh -s email=admin@guinry.cn
fi

"$ACME" --set-default-ca --server letsencrypt

cat >"$NGINX_CONF" <<'NGINX_HTTP'
server {
    listen 80;
    server_name coldchain.guinry.cn;

    root /www/wwwroot/coldchain-guardian-web;
    index index.html;

    access_log /www/server/nginx/logs/coldchain.guinry.cn.access.log;
    error_log /www/server/nginx/logs/coldchain.guinry.cn.error.log;

    client_max_body_size 20m;

    location /.well-known/acme-challenge/ {
        root /www/wwwroot/coldchain-guardian-web;
        try_files $uri =404;
    }

    location = /api/actuator/health {
        proxy_pass http://127.0.0.1:18080/actuator/health;
        proxy_http_version 1.1;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }

    location /api/ {
        proxy_pass http://127.0.0.1:18080;
        proxy_http_version 1.1;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        proxy_read_timeout 300s;
        proxy_send_timeout 300s;
        proxy_buffering off;
    }

    location /actuator/ {
        proxy_pass http://127.0.0.1:18080;
        proxy_http_version 1.1;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }

    location /ws/ {
        proxy_pass http://127.0.0.1:18080;
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection "upgrade";
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        proxy_read_timeout 3600s;
    }

    location / {
        try_files $uri $uri/ /index.html;
    }
}
NGINX_HTTP

nginx -t
systemctl reload nginx

"$ACME" --issue -d "$DOMAIN" -w "$WEB_ROOT" --server letsencrypt --keylength ec-256
"$ACME" --install-cert -d "$DOMAIN" --ecc \
  --fullchain-file "$SSL_DIR/fullchain.cer" \
  --key-file "$SSL_DIR/${DOMAIN}.key" \
  --reloadcmd "nginx -t && systemctl reload nginx"

cat >"$NGINX_CONF" <<'NGINX_SSL'
server {
    listen 80;
    server_name coldchain.guinry.cn;

    root /www/wwwroot/coldchain-guardian-web;

    location /.well-known/acme-challenge/ {
        root /www/wwwroot/coldchain-guardian-web;
        try_files $uri =404;
    }

    location / {
        return 301 https://$host$request_uri;
    }
}

server {
    listen 443 ssl http2;
    server_name coldchain.guinry.cn;

    root /www/wwwroot/coldchain-guardian-web;
    index index.html;

    access_log /www/server/nginx/logs/coldchain.guinry.cn.access.log;
    error_log /www/server/nginx/logs/coldchain.guinry.cn.error.log;

    ssl_certificate /etc/coldchain-guardian/ssl/fullchain.cer;
    ssl_certificate_key /etc/coldchain-guardian/ssl/coldchain.guinry.cn.key;
    ssl_session_timeout 1d;
    ssl_session_cache shared:SSL:10m;
    ssl_protocols TLSv1.2 TLSv1.3;
    ssl_ciphers HIGH:!aNULL:!MD5;
    ssl_prefer_server_ciphers off;

    client_max_body_size 20m;

    location = /api/actuator/health {
        proxy_pass http://127.0.0.1:18080/actuator/health;
        proxy_http_version 1.1;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }

    location /api/ {
        proxy_pass http://127.0.0.1:18080;
        proxy_http_version 1.1;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        proxy_read_timeout 300s;
        proxy_send_timeout 300s;
        proxy_buffering off;
    }

    location /actuator/ {
        proxy_pass http://127.0.0.1:18080;
        proxy_http_version 1.1;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }

    location /ws/ {
        proxy_pass http://127.0.0.1:18080;
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection "upgrade";
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        proxy_read_timeout 3600s;
    }

    location / {
        try_files $uri $uri/ /index.html;
    }
}
NGINX_SSL

nginx -t
systemctl reload nginx

echo "BACKUP=$BACKUP"
echo "CERT=$(openssl x509 -in "$SSL_DIR/fullchain.cer" -noout -subject -issuer -dates | tr '\n' ';')"
echo "HTTPS_READY"
