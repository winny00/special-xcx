#!/usr/bin/env bash
# 启动特教平台全部服务
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
MYSQL="/opt/homebrew/opt/mysql/bin/mysql"
REDIS_SERVER="/opt/homebrew/opt/redis/bin/redis-server"
JAVA="${JAVA_HOME:+${JAVA_HOME}/bin/}java"

log() { echo "[start-all] $*"; }

wait_port() {
  local port=$1 name=$2
  for _ in $(seq 1 120); do
    nc -z localhost "$port" 2>/dev/null && { log "$name 已就绪 (:$port)"; return 0; }
    sleep 1
  done
  echo "等待 $name (:$port) 超时"; return 1
}

# MySQL
if ! nc -z localhost 3306 2>/dev/null; then
  log "启动 MySQL..."
  brew services start mysql
  wait_port 3306 "MySQL"
fi

# Redis（使用项目本地配置，避免 brew 默认配置加载失败模块）
if ! redis-cli -a ruoyi123 ping >/dev/null 2>&1; then
  brew services stop redis 2>/dev/null || true
  log "启动 Redis..."
  "$REDIS_SERVER" "$ROOT/scripts/redis-local.conf"
  wait_port 6379 "Redis"
fi

# 初始化数据库
"$MYSQL" -uroot -e "CREATE DATABASE IF NOT EXISTS \`ry-vue\` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;"
if ! "$MYSQL" -uroot ry-vue -e "SELECT 1 FROM sys_user LIMIT 1;" >/dev/null 2>&1; then
  log "导入 ry_vue.sql..."
  "$MYSQL" -uroot ry-vue < "$ROOT/server/script/sql/ry_vue.sql"
fi
if ! "$MYSQL" -uroot ry-vue -e "SELECT 1 FROM special_resource LIMIT 1;" >/dev/null 2>&1; then
  log "导入 ry_special.sql..."
  "$MYSQL" -uroot ry-vue < "$ROOT/server/script/sql/ry_special.sql"
fi

# 后端（Homebrew MySQL 默认空密码，通过参数覆盖 application-dev.yml 中的 root）
if ! nc -z localhost 8080 2>/dev/null; then
  log "启动后端..."
  (cd "$ROOT/server/ruoyi-admin" && nohup "$JAVA" -jar target/ruoyi-admin.jar \
    --spring.profiles.active=dev \
    --spring.datasource.dynamic.datasource.master.password= \
    --api-decrypt.enabled=false \
    --captcha.enable=false \
    > /tmp/special-ruoyi.log 2>&1 &)
  wait_port 8080 "后端"
fi

if ! nc -z localhost 5174 2>/dev/null; then
  log "启动管理后台..."
  (cd "$ROOT/apps/admin" && nohup npm run dev > /tmp/special-admin.log 2>&1 &)
  wait_port 5174 "管理后台"
fi

if ! nc -z localhost 9000 2>/dev/null; then
  log "启动移动端 H5..."
  (cd "$ROOT/apps/mobile" && nohup pnpm dev:h5 > /tmp/special-mobile.log 2>&1 &)
  wait_port 9000 "移动端 H5"
fi

cat <<'EOF'

✅ 全部服务已启动
  后端:       http://localhost:8080  (doc: /doc.html)
  管理后台:   http://localhost:5174  (admin / admin123)
  移动端 H5:  http://localhost:9000

日志: /tmp/special-{ruoyi,admin,mobile}.log

EOF
