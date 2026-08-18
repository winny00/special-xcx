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
VITE_SERVER_BASEURL = 'https://api.yourdomain.com'
```

### 后端（RuoYi-Vue-Plus）

编辑 `server/ruoyi-admin/src/main/resources/application-dev.yml`：

```yaml
special:
  wechat:
    app-id: 你的小程序AppID
    app-secret: 你的小程序AppSecret
  oss:
    enabled: false
    bucket: special-edu-bucket
    region: ap-guangzhou
```

执行 SQL 初始化小程序客户端（已包含在 `ry_special.sql`）：

```sql
-- client_id: special_xcx_client_id
-- grant_type: password,xcx,sms
```

## 3. 服务器域名配置

在微信公众平台 → 开发 → 开发管理 → 开发设置 → 服务器域名：

| 类型 | 域名示例 | 说明 |
|------|----------|------|
| request 合法域名 | `https://api.yourdomain.com` | 后端 API |
| uploadFile 合法域名 | `https://cos.ap-guangzhou.myqcloud.com` | OSS 上传（启用后） |
| downloadFile 合法域名 | 同上 | 文件下载 |
| 业务域名 | `https://h5.yourdomain.com` | H5 分享页 |

**注意：** 微信小程序要求 HTTPS，本地开发可在微信开发者工具勾选「不校验合法域名、web-view、TLS 版本以及 HTTPS 证书」。

## 4. 隐私协议

在公众平台 → 设置 → 服务内容声明 → 用户隐私保护指引，声明以下采集项：

- 手机号（预约联系）
- 微信昵称/头像（登录）
- 位置信息（可选，机构附近推荐）

项目隐私政策模板见 `docs/privacy-policy.md`。

## 5. 订阅消息（二期）

在公众平台 → 功能 → 订阅消息，申请以下模板：

- 预约受理通知
- 预约结果通知
- 资源更新提醒

## 6. 腾讯云 COS 配置（文件存储）

1. 开通 [腾讯云 COS](https://cloud.tencent.com/product/cos)
2. 创建存储桶 `special-edu-bucket`，区域选 `ap-guangzhou`
3. 在 RuoYi 后台「系统管理 → OSS 配置」添加 COS 配置
4. 将 COS 域名加入小程序 uploadFile/downloadFile 合法域名

## 7. 本地调试流程

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

## 8. 发布 checklist

- [ ] AppID / AppSecret 已配置
- [ ] 服务器域名已备案并配置 HTTPS
- [ ] 隐私保护指引已提交审核
- [ ] 小程序类目选择「教育 → 特殊教育」或相近类目
- [ ] 内容审核机制就绪（机构资质、资源合规）
- [ ] 生产环境 OSS 已启用
