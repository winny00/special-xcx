#!/usr/bin/env bash
# ECS 上安全部署后端：同步 prod 配置 → 备份 jar → 重启 → 等 8080 就绪 → 失败自动回滚
# 用法: bash deploy-server.sh /tmp/ruoyi-admin.jar
set -euo pipefail

JAR_SRC="${1:?用法: deploy-server.sh /path/to/ruoyi-admin.jar}"
DEPLOY_DIR=/opt/special/server
BACKUP_DIR=/opt/special/backups
JAR_NAME=ruoyi-admin.jar
SECRETS=/opt/special/secrets.env
CONFIG=/opt/special/config/application-prod.yml
HEALTH_8080="http://127.0.0.1:8080/special/mobile/resource/list?pageNum=1&pageSize=1"
HEALTH_NGINX="http://127.0.0.1/special/mobile/resource/list?pageNum=1&pageSize=1"
MAX_WAIT="${DEPLOY_MAX_WAIT:-40}"

log() { echo "[deploy-server] $*"; }

[[ -f "$JAR_SRC" ]] || { log "缺少 jar: $JAR_SRC"; exit 1; }
[[ -f "$SECRETS" ]] || { log "缺少 $SECRETS，请先运行 harden-and-maintain.sh"; exit 1; }
# shellcheck disable=SC1090
source "$SECRETS"

mkdir -p /opt/special/config "$DEPLOY_DIR/logs" "$BACKUP_DIR"

# 每次部署前同步 prod 配置，避免 MySQL/Redis 密码漂移导致重启后 502
cat > "$CONFIG" <<EOF
special:
  wechat:
    app-id: wxf70d043d359a1586
    app-secret: b5de3db805f9b6f14880197b5bd18696
  oss:
    enabled: false

spring:
  datasource:
    dynamic:
      datasource:
        master:
          password: ${MYSQL_ROOT_PASSWORD}
  data:
    redis:
      password: ${REDIS_PASSWORD}

captcha:
  enable: false

api-decrypt:
  enabled: false
EOF
chmod 600 "$CONFIG"
log "已同步 application-prod.yml"

TS=$(date +%Y%m%d-%H%M%S)
PREV_BACKUP=""
if [[ -f "$DEPLOY_DIR/$JAR_NAME" ]]; then
  PREV_BACKUP="$BACKUP_DIR/${JAR_NAME}.${TS}"
  cp "$DEPLOY_DIR/$JAR_NAME" "$PREV_BACKUP"
  log "已备份旧 jar → $PREV_BACKUP"
fi

install -m 644 "$JAR_SRC" "$DEPLOY_DIR/$JAR_NAME"
log "jar 已更新"

systemctl restart special-server
log "等待后端就绪（最多 $((MAX_WAIT * 3))s，2G 机器冷启动较慢）..."

for i in $(seq 1 "$MAX_WAIT"); do
  if curl -sf "$HEALTH_8080" | grep -q '"code":200'; then
    log "8080 就绪 (${i}x3s)"
    if curl -sf "$HEALTH_NGINX" | grep -q '"code":200'; then
      log "nginx 反代 OK，部署完成"
    else
      log "8080 OK 但 nginx 未通，请检查 nginx"
    fi
    exit 0
  fi
  if ! systemctl is-active --quiet special-server; then
    log "special-server 进程已退出（第 ${i} 次检查）"
    break
  fi
  sleep 3
done

log "新 jar 启动失败，回滚上一版本..."
if [[ -n "$PREV_BACKUP" && -f "$PREV_BACKUP" ]]; then
  cp "$PREV_BACKUP" "$DEPLOY_DIR/$JAR_NAME"
  systemctl restart special-server
  for i in $(seq 1 20); do
    if curl -sf "$HEALTH_8080" | grep -q '"code":200'; then
      log "回滚成功，旧版本已恢复"
      exit 1
    fi
    sleep 3
  done
fi

log "回滚后仍未就绪，请人工排查："
tail -50 "$DEPLOY_DIR/logs/app.log" 2>/dev/null || true
systemctl status special-server --no-pager || true
exit 1
