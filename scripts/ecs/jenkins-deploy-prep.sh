#!/usr/bin/env bash
# 在 ECS 上以 root 执行：应用 Nginx、H5 目录、jenkins 部署权限
set -euo pipefail

log() { echo "[jenkins-deploy-prep] $*"; }

if [[ "${EUID:-0}" -ne 0 ]]; then
  echo "请用 root 执行"; exit 1
fi

REPO_NGINX="${REPO_NGINX:-/opt/special/scripts-repo/ecs/nginx-special.conf}"

log "创建 H5 目录 ..."
mkdir -p /var/www/h5
chown -R www-data:www-data /var/www/admin /var/www/h5

log "配置 jenkins 用户写静态目录 ..."
usermod -aG www-data jenkins
chmod -R g+w /var/www/admin /var/www/h5

log "配置 jenkins sudo（后端部署）..."
cat > /etc/sudoers.d/jenkins-deploy <<'EOF'
jenkins ALL=(root) NOPASSWD: /bin/systemctl restart special-server
jenkins ALL=(root) NOPASSWD: /bin/cp /var/lib/jenkins/workspace/*/server/ruoyi-admin/target/ruoyi-admin.jar /opt/special/server/ruoyi-admin.jar
jenkins ALL=(root) NOPASSWD: /bin/cp /opt/special/server/ruoyi-admin.jar /opt/special/backups/ruoyi-admin.jar.*
EOF
visudo -cf /etc/sudoers.d/jenkins-deploy

if [[ -f "$REPO_NGINX" ]]; then
  log "应用 Nginx 配置 ..."
  cp "$REPO_NGINX" /etc/nginx/sites-available/special
  nginx -t
  systemctl reload nginx
else
  log "警告: 未找到 $REPO_NGINX，请手动复制 nginx-special.conf"
fi

log "验证 ..."
curl -sf -o /dev/null -w 'jenkins/login HTTP %{http_code}\n' http://127.0.0.1/jenkins/login || true
curl -sf -o /dev/null -w 'h5/ HTTP %{http_code}\n' http://127.0.0.1/h5/ || true

log "完成。下一步见 scripts/ecs/jenkins-ui-setup.md 与 jenkins-jobs-setup.md"
