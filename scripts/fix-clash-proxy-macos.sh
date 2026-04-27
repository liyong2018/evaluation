#!/usr/bin/env bash
set -euo pipefail

# One-click self-check and repair for Clash Verge proxy settings on macOS.
# It aligns macOS proxy ports with the actual local Clash listener.

NETWORK_SERVICE="${1:-Wi-Fi}"
HTTP_PORT=""
SOCKS_PORT=""
CLASH_CONFIG="${CLASH_CONFIG:-$HOME/.config/clash/config.yaml}"

log() {
  printf "\n[%s] %s\n" "$(date '+%H:%M:%S')" "$1"
}

has_listen() {
  local port="$1"
  lsof -nP -iTCP:"$port" -sTCP:LISTEN >/dev/null 2>&1
}

extract_yaml_port() {
  local key="$1"
  local file="$2"
  if [ -f "$file" ]; then
    sed -nE "s/^[[:space:]]*${key}[[:space:]]*:[[:space:]]*([0-9]+).*/\\1/p" "$file" | head -n1
  fi
}

pick_ports_from_config() {
  local mixed port socks
  mixed="$(extract_yaml_port "mixed-port" "$CLASH_CONFIG" || true)"
  port="$(extract_yaml_port "port" "$CLASH_CONFIG" || true)"
  socks="$(extract_yaml_port "socks-port" "$CLASH_CONFIG" || true)"

  if [ -n "${mixed:-}" ] && has_listen "$mixed"; then
    HTTP_PORT="$mixed"
    SOCKS_PORT="$mixed"
    log "端口来源: 配置 mixed-port=$mixed"
    return 0
  fi

  if [ -n "${port:-}" ] && has_listen "$port"; then
    HTTP_PORT="$port"
    if [ -n "${socks:-}" ] && has_listen "$socks"; then
      SOCKS_PORT="$socks"
    else
      SOCKS_PORT="$port"
    fi
    log "端口来源: 配置 port=$port socks-port=${socks:-N/A}"
    return 0
  fi

  return 1
}

pick_ports_from_listeners() {
  local mihomo_ports p
  mihomo_ports="$(lsof -nP -iTCP -sTCP:LISTEN 2>/dev/null | awk 'tolower($1) ~ /mihomo|verge-mih/ {split($9, a, ":"); if (a[2] ~ /^[0-9]+$/) print a[2]}' | sort -n | uniq || true)"

  # Prefer core listeners only (mihomo). Avoid selecting Clash Verge UI/API ports.
  if [ -n "$mihomo_ports" ]; then
    HTTP_PORT="$(printf "%s\n" "$mihomo_ports" | head -n1)"
    if printf "%s\n" "$mihomo_ports" | grep -qx "$((HTTP_PORT + 1))"; then
      SOCKS_PORT="$((HTTP_PORT + 1))"
    else
      SOCKS_PORT="$HTTP_PORT"
    fi
    log "端口来源: 监听端口(mihomo) -> HTTP:$HTTP_PORT SOCKS:$SOCKS_PORT"
    return 0
  fi

  # Fallback to well-known default proxy ports only.
  for p in 7890 7897 7891; do
    if has_listen "$p"; then
      HTTP_PORT="$p"
      if [ "$p" = "7890" ] && has_listen 7891; then
        SOCKS_PORT=7891
      else
        SOCKS_PORT="$p"
      fi
      log "端口来源: 默认端口探测 -> HTTP:$HTTP_PORT SOCKS:$SOCKS_PORT"
      return 0
    fi
  done

  return 1
}

pick_ports() {
  if pick_ports_from_config; then
    return
  fi

  if pick_ports_from_listeners; then
    return
  fi

  echo "未检测到可用的 Clash 监听端口。" >&2
  echo "请确认: 1) Clash Verge 内核已运行 2) 系统代理已开启 3) 配置路径 $CLASH_CONFIG 正确" >&2
  exit 1
}

ensure_network_service() {
  if ! networksetup -listallnetworkservices | sed '1d' | grep -Fxq "$NETWORK_SERVICE"; then
    echo "未找到网络服务: $NETWORK_SERVICE" >&2
    echo "可用服务如下：" >&2
    networksetup -listallnetworkservices >&2
    exit 1
  fi
}

apply_proxy() {
  log "设置系统代理到 $NETWORK_SERVICE -> HTTP/HTTPS:$HTTP_PORT SOCKS:$SOCKS_PORT"
  networksetup -setwebproxy "$NETWORK_SERVICE" 127.0.0.1 "$HTTP_PORT"
  networksetup -setsecurewebproxy "$NETWORK_SERVICE" 127.0.0.1 "$HTTP_PORT"
  networksetup -setsocksfirewallproxy "$NETWORK_SERVICE" 127.0.0.1 "$SOCKS_PORT"
}

show_proxy() {
  log "当前系统代理配置"
  networksetup -getwebproxy "$NETWORK_SERVICE"
  networksetup -getsecurewebproxy "$NETWORK_SERVICE"
  networksetup -getsocksfirewallproxy "$NETWORK_SERVICE"
}

test_google() {
  log "连通性测试（通过 HTTP 代理）"
  if curl -I --max-time 15 -x "http://127.0.0.1:${HTTP_PORT}" https://www.google.com >/tmp/clash_google_test.out 2>/tmp/clash_google_test.err; then
    cat /tmp/clash_google_test.out
    log "测试成功：Google 可访问。"
  else
    cat /tmp/clash_google_test.err >&2 || true
    echo "测试失败：请检查节点可用性、Clash 日志和远端服务器时间同步。" >&2
    exit 2
  fi
}

main() {
  ensure_network_service
  pick_ports
  apply_proxy
  show_proxy
  test_google
}

main "$@"
