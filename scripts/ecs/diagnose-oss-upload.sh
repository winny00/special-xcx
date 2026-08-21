#!/usr/bin/env bash
# 排查 Admin 封面上传 500：查看 OSS 配置与 app.log 堆栈
set -euo pipefail

SECRETS=/opt/special/secrets.env
LOG=/opt/special/server/logs/app.log
ERR_ID="${1:-}"

log() { echo "[diagnose-oss] $*"; }

[[ -f "$SECRETS" ]] && source "$SECRETS" || true
MYSQL_PWD="${MYSQL_ROOT_PASSWORD:-root}"

log "=== sys_oss_config（默认项）==="
mysql -uroot -p"$MYSQL_PWD" ry-vue -N -e \
  "SELECT config_key, bucket_name, endpoint, region, is_https, status,
          CONCAT(LEFT(access_key,8),'...') AS ak
   FROM sys_oss_config WHERE status='Y'\G" 2>/dev/null || log "MySQL 查询失败"

log ""
log "=== 8080 健康 ==="
curl -sf "http://127.0.0.1:8080/special/mobile/resource/list?pageNum=1&pageSize=1" | head -c 100 || log "8080 不可用"

log ""
if [[ -n "$ERR_ID" ]]; then
  log "=== 错误编号 ${ERR_ID} 附近日志 ==="
  grep -A15 "$ERR_ID" "$LOG" 2>/dev/null | tail -20 || log "未找到该错误编号"
else
  log "=== 最近 oss/upload 异常（用法: bash diagnose-oss-upload.sh 08647944）==="
  grep -A12 'oss/upload\|S3Exception\|chunked\|InvalidAccessKey\|AccessDenied' "$LOG" 2>/dev/null | tail -40 || log "无相关日志"
fi
