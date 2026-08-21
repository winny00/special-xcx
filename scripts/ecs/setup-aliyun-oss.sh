#!/usr/bin/env bash
# 在 ECS 上配置阿里云 OSS（sys_oss_config），修复封面上传 500。
# 用法（SSH 到 ECS 后）：
#   export OSS_ACCESS_KEY='你的AK'
#   export OSS_SECRET_KEY='你的SK'
#   bash /opt/special/scripts/setup-aliyun-oss.sh
#
# 可选环境变量：
#   OSS_BUCKET=winny
#   OSS_ENDPOINT=oss-cn-guangzhou.aliyuncs.com
#   OSS_REGION=cn-guangzhou
#   OSS_PREFIX=special/
set -euo pipefail

SECRETS=/opt/special/secrets.env
DB_NAME="${DB_NAME:-ry-vue}"

OSS_ACCESS_KEY="${OSS_ACCESS_KEY:-}"
OSS_SECRET_KEY="${OSS_SECRET_KEY:-}"
OSS_BUCKET="${OSS_BUCKET:-winny}"
OSS_ENDPOINT="${OSS_ENDPOINT:-oss-cn-guangzhou.aliyuncs.com}"
OSS_REGION="${OSS_REGION:-cn-guangzhou}"
OSS_PREFIX="${OSS_PREFIX:-special/}"

log() { echo "[setup-aliyun-oss] $*"; }
die() { log "错误: $*"; exit 1; }

[[ -f "$SECRETS" ]] || die "缺少 $SECRETS"
# shellcheck disable=SC1090
source "$SECRETS"
MYSQL_PWD="${MYSQL_ROOT_PASSWORD:?secrets.env 中无 MYSQL_ROOT_PASSWORD}"

if [[ -z "$OSS_ACCESS_KEY" || -z "$OSS_SECRET_KEY" ]]; then
  die "请先 export OSS_ACCESS_KEY 和 OSS_SECRET_KEY（RAM 子账号，勿用主账号）"
fi

if [[ "$OSS_ENDPOINT" == http*://* ]]; then
  die "endpoint 不要带 http(s)://，只填 oss-cn-xxx.aliyuncs.com"
fi

log "当前 OSS 配置："
mysql -uroot -p"$MYSQL_PWD" "$DB_NAME" -N -e \
  "SELECT config_key, bucket_name, endpoint, region, status,
          IF(access_key LIKE 'XXX%' OR access_key='', '未配置', '已配置') AS ak
   FROM sys_oss_config ORDER BY oss_config_id;"

log "写入 aliyun 配置（桶: ${OSS_BUCKET}, endpoint: ${OSS_ENDPOINT}）..."
mysql -uroot -p"$MYSQL_PWD" "$DB_NAME" <<SQL
UPDATE sys_oss_config SET status='N' WHERE config_key='minio';
UPDATE sys_oss_config SET
  access_key='${OSS_ACCESS_KEY}',
  secret_key='${OSS_SECRET_KEY}',
  bucket_name='${OSS_BUCKET}',
  prefix='${OSS_PREFIX}',
  endpoint='${OSS_ENDPOINT}',
  domain_url='',
  is_https='Y',
  region='${OSS_REGION}',
  access_policy='1',
  status='Y',
  update_time=NOW()
WHERE config_key='aliyun';
SQL

log "重启 special-server（刷新 Redis OSS 缓存）..."
systemctl restart special-server

log "等待后端就绪（最多 90s）..."
for i in $(seq 1 30); do
  if curl -sf "http://127.0.0.1:8080/special/mobile/resource/list?pageNum=1&pageSize=1" | grep -q '"code":200'; then
    log "后端已就绪"
    break
  fi
  sleep 3
  if [[ "$i" -eq 30 ]]; then
    tail -30 /opt/special/server/logs/app.log || true
    die "后端启动超时"
  fi
done

log "验证 OSS 连通（S3 HeadBucket，需安装 awscli 可跳过）..."
if command -v aws >/dev/null 2>&1; then
  AWS_ACCESS_KEY_ID="$OSS_ACCESS_KEY" AWS_SECRET_ACCESS_KEY="$OSS_SECRET_KEY" \
    aws --endpoint-url "https://${OSS_ENDPOINT}" s3 ls "s3://${OSS_BUCKET}/" >/dev/null \
    && log "OSS 桶可读" || log "警告: aws s3 ls 失败，请检查 AK/桶名/endpoint 地域"
else
  log "未安装 awscli，跳过桶探测；请直接在 Admin 上传封面测试"
fi

log "完成。请在 Admin 资讯管理上传封面；若仍 500，执行："
log "  grep -A5 '45215131\\|oss/upload\\|S3Storage' /opt/special/server/logs/app.log | tail -40"
