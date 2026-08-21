#!/usr/bin/env bash
# 在 ECS 上安装 MinIO（对象存储），供 Admin 封面上传 /resource/oss/upload 使用。
# 生产长期方案仍建议改用腾讯云 COS（见 docs/wechat-config.md §7.2）。
set -euo pipefail

MINIO_USER="${MINIO_USER:-ruoyi}"
MINIO_PASS="${MINIO_PASS:-ruoyi123}"
MINIO_BUCKET="${MINIO_BUCKET:-ruoyi}"
DATA_DIR="${MINIO_DATA_DIR:-/opt/special/minio/data}"
API_PORT="${MINIO_API_PORT:-9000}"
CONSOLE_PORT="${MINIO_CONSOLE_PORT:-9001}"

log() { echo "[install-minio] $*"; }

if ! command -v docker >/dev/null 2>&1; then
  log "安装 Docker..."
  curl -fsSL https://get.docker.com | sh
  systemctl enable --now docker
fi

mkdir -p "$DATA_DIR"

if docker ps -a --format '{{.Names}}' | grep -qx special-minio; then
  log "容器 special-minio 已存在，重启..."
  docker restart special-minio
else
  log "启动 MinIO 容器..."
  docker run -d --name special-minio --restart unless-stopped \
    -p "${API_PORT}:9000" \
    -p "${CONSOLE_PORT}:9001" \
    -e "MINIO_ROOT_USER=${MINIO_USER}" \
    -e "MINIO_ROOT_PASSWORD=${MINIO_PASS}" \
    -v "${DATA_DIR}:/data" \
    minio/minio server /data --console-address ":9001"
fi

log "等待 MinIO 就绪..."
for _ in $(seq 1 30); do
  if curl -sf "http://127.0.0.1:${API_PORT}/minio/health/live" >/dev/null 2>&1; then
    break
  fi
  sleep 1
done

if ! docker run --rm --network host minio/mc alias set local "http://127.0.0.1:${API_PORT}" "$MINIO_USER" "$MINIO_PASS" 2>/dev/null; then
  docker run --rm --network host minio/mc alias set local "http://127.0.0.1:${API_PORT}" "$MINIO_USER" "$MINIO_PASS"
fi
docker run --rm --network host minio/mc mb --ignore-existing "local/${MINIO_BUCKET}"
docker run --rm --network host minio/mc anonymous set download "local/${MINIO_BUCKET}" || true

log "MinIO 已就绪"
log "  API:     http://127.0.0.1:${API_PORT}"
log "  控制台:  http://$(curl -s ifconfig.me 2>/dev/null || echo 'ECS_IP'):${CONSOLE_PORT}  (账号 ${MINIO_USER}/${MINIO_PASS})"
log "RuoYi 默认 sys_oss_config(minio) 已指向 127.0.0.1:9000，上传应可恢复。"
log "小程序图片需 HTTPS 域名，MinIO 仅适合 Admin 内测；上线请配置 COS。"
