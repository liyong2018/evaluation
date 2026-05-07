#!/usr/bin/env bash
set -euo pipefail

XRAY_CONF="/usr/local/etc/xray/config.json"
XRAY_CERT_DIR="/usr/local/etc/xray/cert"
V2RAY_864_CONF="/etc/v2ray/conf/VMess-TCP-864.json"
TS="$(date +%Y%m%d%H%M%S)"

mkdir -p "$XRAY_CERT_DIR"
cp -a "$XRAY_CONF" "${XRAY_CONF}.bak.${TS}" 2>/dev/null || true
cp -a "$V2RAY_864_CONF" "${V2RAY_864_CONF}.bak.${TS}" 2>/dev/null || true

openssl req -x509 -nodes -newkey rsa:2048 \
  -keyout "$XRAY_CERT_DIR/server.key" \
  -out "$XRAY_CERT_DIR/server.crt" \
  -days 3650 \
  -subj "/CN=www.microsoft.com"
chmod 644 "$XRAY_CERT_DIR/server.crt" "$XRAY_CERT_DIR/server.key"

cat > "$XRAY_CONF" <<'JSON'
{
  "log": {
    "access": "/var/log/xray/access.log",
    "error": "/var/log/xray/error.log",
    "loglevel": "warning"
  },
  "inbounds": [
    {
      "listen": "0.0.0.0",
      "port": 864,
      "protocol": "vless",
      "settings": {
        "clients": [
          {
            "id": "4a43a437-4f42-4cf8-8584-3c7759d99af6",
            "email": "default"
          }
        ],
        "decryption": "none"
      },
      "streamSettings": {
        "network": "tcp",
        "security": "tls",
        "tlsSettings": {
          "alpn": ["http/1.1"],
          "certificates": [
            {
              "certificateFile": "/usr/local/etc/xray/cert/server.crt",
              "keyFile": "/usr/local/etc/xray/cert/server.key"
            }
          ]
        }
      },
      "sniffing": {
        "enabled": true,
        "destOverride": ["http", "tls"]
      }
    }
  ],
  "outbounds": [
    {
      "protocol": "freedom",
      "tag": "direct"
    }
  ]
}
JSON

xray run -test -config "$XRAY_CONF"

if [ -f "$V2RAY_864_CONF" ]; then
  mv "$V2RAY_864_CONF" "/etc/v2ray/conf/VMess-TCP-864.json.disabled.${TS}"
fi

systemctl restart v2ray
systemctl restart xray
sleep 2

echo '--- SS ---'
ss -lntp | grep -E ':443|:864|:8443' || true

echo '--- XRAY STATUS ---'
systemctl --no-pager --full status xray | sed -n '1,20p'

echo '--- CERT ---'
openssl x509 -in "$XRAY_CERT_DIR/server.crt" -noout -subject -issuer -dates
