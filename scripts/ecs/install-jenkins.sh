#!/usr/bin/env bash
# 在 ECS 上以 root 执行：安装 Jenkins + 构建依赖
set -euo pipefail

log() { echo "[install-jenkins] $*"; }

if [[ "${EUID:-0}" -ne 0 ]]; then
  echo "请用 root 执行"; exit 1
fi

export DEBIAN_FRONTEND=noninteractive

log "安装基础依赖 ..."
apt-get update
apt-get install -y curl gnupg apt-transport-https ca-certificates git rsync

log "安装 OpenJDK 21 ..."
apt-get install -y openjdk-21-jdk
java -version

log "安装 Maven ..."
apt-get install -y maven
mvn -version

log "安装 Node.js 20 ..."
curl -fsSL https://deb.nodesource.com/setup_20.x | bash -
apt-get install -y nodejs
node -v
npm -v

log "安装 pnpm ..."
npm install -g pnpm@10
pnpm -v

log "安装 Jenkins LTS ..."
install -d -m 0755 /usr/share/keyrings
curl -fsSL https://pkg.jenkins.io/debian-stable/jenkins.io-2023.key \
  | gpg --dearmor -o /usr/share/keyrings/jenkins-keyring.gpg
echo "deb [signed-by=/usr/share/keyrings/jenkins-keyring.gpg] https://pkg.jenkins.io/debian-stable binary/" \
  > /etc/apt/sources.list.d/jenkins.list
apt-get update
apt-get install -y jenkins

log "配置 Jenkins 端口 8081 + prefix /jenkins ..."
mkdir -p /etc/systemd/system/jenkins.service.d
cat > /etc/systemd/system/jenkins.service.d/override.conf <<'EOF'
[Service]
Environment="JENKINS_PORT=8081"
Environment="JENKINS_PREFIX=/jenkins"
Environment="JENKINS_OPTS=--prefix=/jenkins"
EOF
systemctl daemon-reload
systemctl enable jenkins
systemctl restart jenkins

log "等待 Jenkins 启动 ..."
for i in $(seq 1 30); do
  if curl -sf http://127.0.0.1:8081/jenkins/login >/dev/null 2>&1; then
    break
  fi
  sleep 2
done

INITIAL=$(grep -oP '(?<=Password: ).+' /var/lib/jenkins/secrets/initialAdminPassword 2>/dev/null || true)
log "Jenkins 初始管理员密码: ${INITIAL:-见 /var/lib/jenkins/secrets/initialAdminPassword}"

log "完成。下一步：浏览器访问 http://<ECS-IP>/jenkins/ 完成向导"
