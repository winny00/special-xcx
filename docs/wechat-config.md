# 微信小程序配置指南

## 1. 注册小程序账号

1. 访问 [微信公众平台](https://mp.weixin.qq.com/) 注册「小程序」类型账号
2. 完成主体认证（企业/个体工商户/政府等）
3. 在「开发 → 开发管理 → 开发设置」获取 **AppID** 和 **AppSecret**

## 2. 项目配置

### 移动端（unibest）

编辑 `apps/mobile/env/.env`：

```env
VITE_WX_APPID = '你的小程序AppID'
VITE_RUOYI_CLIENT_ID = 'special_xcx_client_id'
```

编辑 `apps/mobile/env/.env.development`：

```env
# 开发环境后端地址（需 HTTPS 或在开发者工具中勾选「不校验合法域名」）
VITE_SERVER_BASEURL = 'http://localhost:8080'
```

生产环境编辑 `apps/mobile/env/.env.production`：

```env
# 备案 + HTTPS 前可暂用 IP（仅开发者工具内测）
VITE_SERVER_BASEURL = 'http://8.134.110.218'
VITE_SERVER_BASEURL__WEIXIN_RELEASE = 'http://8.134.110.218'

# 备案完成后改为（须与微信 request 合法域名一致）
# VITE_SERVER_BASEURL = 'https://api.yourdomain.com'
# VITE_SERVER_BASEURL__WEIXIN_RELEASE = 'https://api.yourdomain.com'
```

生产环境后端配置在 ECS `/opt/special/config/application-prod.yml`（勿提交 Git）：

```yaml
special:
  wechat:
    app-id: 你的小程序AppID
    app-secret: 你的小程序AppSecret
```

本地开发仍用 `application-dev.yml`。

执行 SQL 初始化小程序客户端（已包含在 `ry_special.sql`）：

```sql
-- client_id: special_xcx_client_id
-- grant_type: password,xcx,sms
```

## 3. ECS 生产部署（当前环境）

| 项 | 值 |
|----|-----|
| ECS 公网 IP | `8.134.110.218` |
| 管理后台 | http://8.134.110.218/login |
| API 示例 | http://8.134.110.218/special/mobile/resource/list |
| H5 家长端 | http://8.134.110.218/h5/ |

**CI/CD（GitHub Actions）**

- 仓库：GitHub 私有 `special-xcx`
- push `main`：改 `server/` → Deploy server；改 `apps/admin/` → Deploy admin；改 `apps/mobile/` → Deploy H5
- 一键：Actions → Deploy ECS（后端 + 管理后台 + H5）
- 微信小程序仍在开发者工具上传 / 公众平台提审，不走 CI

**Mac 一键安全加固**（改 admin/MySQL/Redis 密码、日志轮转、每日备份）：

```bash
bash scripts/ecs/install-from-mac.sh
```

**域名 + HTTPS**（备案且 A 记录指向 ECS 后）：

```bash
bash /opt/special/scripts-repo/ecs/setup-https.sh api.yourdomain.com your@email.com
```

**安全组**：仅开放 22（建议限源 IP）、80、443；关闭 8080、3306、6379 对公网。

正式版小程序须 HTTPS + 备案域名；开发者工具可勾选「不校验合法域名」用 IP 内测。

## 4. 服务器域名配置（微信后台）

在微信公众平台 → 开发 → 开发管理 → 开发设置 → 服务器域名：

| 类型 | 域名示例 | 说明 |
|------|----------|------|
| request 合法域名 | `https://api.yourdomain.com` | 后端 API |
| uploadFile 合法域名 | `https://cos.ap-guangzhou.myqcloud.com` | OSS 上传（启用后） |
| downloadFile 合法域名 | 同上 | 文件下载 |
| 业务域名 | `https://h5.yourdomain.com` | H5 分享页 |

**注意：** request 合法域名必须是 **HTTPS**，不能使用 IP。

## 5. 隐私协议

在公众平台 → 设置 → 服务内容声明 → 用户隐私保护指引，声明以下采集项：

- 手机号（预约联系）
- 微信昵称/头像（登录）
- 位置信息（可选，机构附近推荐）

项目隐私政策模板见 `docs/privacy-policy.md`。

## 6. 订阅消息（二期）

在公众平台 → 功能 → 订阅消息，申请以下模板：

- 预约受理通知
- 预约结果通知
- 资源更新提醒

## 7. 对象存储配置（MinIO 开发 / COS 生产）

RuoYi 上传走 `POST /resource/oss/upload`，存储后端由数据库 `sys_oss_config` 决定（非 yml 直连）。`special.oss.enabled` 仅为二期特性开关。

### 7.1 本地开发（MinIO）

```bash
cd server/script/docker && docker compose up -d minio
```

| 项 | 值 |
|----|-----|
| MinIO API | `127.0.0.1:9000` |
| 控制台 | http://127.0.0.1:9001 |
| 账号 | `ruoyi` / `ruoyi123` |

RuoYi 默认 `sys_oss_config` id=1 已指向 `127.0.0.1:9000`、bucket `ruoyi`、`status='Y'`。启动 MinIO 后即可在 Admin 上传封面，无需改 `application-dev.yml`（其中 `special.oss.enabled` 保持 `false`）。

### 7.2 生产 ECS（腾讯云 COS）

1. 开通 [腾讯云 COS](https://cloud.tencent.com/product/cos)
2. 创建存储桶 `special-edu-bucket`，区域 `ap-guangzhou`，建议公有读私有写（或绑定 CDN HTTPS 域名）
3. 获取 SecretId / SecretKey
4. 登录 Admin → **系统管理 → 对象存储配置** → 编辑 `qcloud` 行或新增：
   - `config_key`: `special-cos`
   - `endpoint`: `cos.ap-guangzhou.myqcloud.com`
   - `bucket_name`: 你的桶名
   - `region`: `ap-guangzhou`
   - `is_https`: `Y`
   - `domain_url`: 可选 CDN 自定义域名（小程序图片推荐 HTTPS）
   - **设为默认** `status='Y'`
5. ECS `/opt/special/config/application-prod.yml` 可写（勿提交 Git）：

```yaml
special:
  oss:
    enabled: true
    bucket: special-edu-bucket
    region: ap-guangzhou
```

（仅开关用途；实际上传仍走 Admin 对象存储配置。）

### 7.3 微信小程序合法域名

公众平台 → 开发 → 开发管理 → 开发设置 → 服务器域名：

| 类型 | 域名示例 |
|------|----------|
| uploadFile 合法域名 | `https://special-edu-bucket.cos.ap-guangzhou.myqcloud.com` 或 CDN 域名 |
| downloadFile 合法域名 | 同上 |

须 **HTTPS**，不能使用 IP。本地 MinIO（`127.0.0.1:9000`）仅 Admin/H5 内测可用；小程序真机须 COS 或 CDN 域名。

### 7.4 手工验证上传 API

Admin 登录获取 Token 后：

```bash
curl -X POST http://localhost:8080/resource/oss/upload \
  -H "Authorization: Bearer <token>" \
  -H "Clientid: e5cd7e4891bf95d1d19206ce24a7b32e" \
  -F "file=@/path/to/test.jpg"
```

Expected: `{"code":200,"data":{"url":"http(s)://...","fileName":"test.jpg","ossId":"..."}}`

`Clientid` 须与 Admin 登录一致（见 `apps/admin/src/api/request.ts` 的 `CLIENT_ID`）。

## 8. 本地调试流程

```bash
# 1. 启动后端（需 MySQL + Redis）
cd server
docker compose -f script/docker/docker-compose.yml up -d  # 可选
# 导入 ry_vue.sql + ry_special.sql
./mvnw spring-boot:run -pl ruoyi-admin

# 2. 启动移动端
cd apps/mobile
pnpm dev:mp-weixin

# 3. 微信开发者工具导入 dist/dev/mp-weixin 目录
```

## 9. 发布 checklist

- [ ] AppID / AppSecret 已配置
- [ ] 服务器域名已备案并配置 HTTPS
- [ ] 隐私保护指引已提交审核
- [ ] 小程序类目选择「教育 → 特殊教育」或相近类目
- [ ] 内容审核机制就绪（机构资质、资源合规）
- [ ] 生产环境 OSS 已启用
