# Jenkins Job 与 GitHub Webhook 配置清单

完成 [jenkins-ui-setup.md](./jenkins-ui-setup.md) 与 ECS Nginx/目录权限后，按本清单创建 4 条 Job 并启用 Webhook。

## 前置条件

- [ ] GitHub 私有仓库已 push（`main` 分支）
- [ ] Jenkins 已安装且可访问 `http://8.134.110.218/jenkins/`
- [ ] Credential `github-pat-readonly` 已配置
- [ ] `/var/www/h5` 已创建，`jenkins` 用户在 `www-data` 组
- [ ] `/etc/sudoers.d/jenkins-deploy` 已配置（后端 jar 部署）
- [ ] 微信公众平台已添加 IP 白名单 `8.134.110.218`（小程序 Job）

## Task 9：创建 4 条 Pipeline Job

对每个 Job：**New Item → Pipeline**，名称与 Script Path 如下。

| Job 名 | Script Path | 自动触发 |
|--------|-------------|----------|
| `special-server` | `jenkins/Jenkinsfile.server` | 是 |
| `special-admin` | `jenkins/Jenkinsfile.admin` | 是 |
| `special-h5` | `jenkins/Jenkinsfile.h5` | 是 |
| `special-mp-weixin` | `jenkins/Jenkinsfile.mp-weixin` | **否（仅手动）** |

### 每条 Job 的 SCM 配置

- Definition: **Pipeline script from SCM**
- SCM: Git
- Repository URL: `https://github.com/<YOUR_GITHUB_USER>/special-xcx.git`
- Credentials: `github-pat-readonly`
- Branch Specifier: `*/main`
- Script Path: 见上表

### Build Triggers（自动 Job 三条）

`special-server` / `special-admin` / `special-h5`：

- [x] **GitHub hook trigger for GITScm polling**
- [ ] Poll SCM（保持关闭）

`special-mp-weixin`：**不勾选任何 trigger**。

### 手动验证顺序

1. **Build Now** `special-admin` → `curl http://127.0.0.1/login` 返回 200
2. **Build Now** `special-server` → API health `"code":200`
3. **Build Now** `special-h5` → `http://8.134.110.218/h5/` 可打开
4. **Build with Parameters** `special-mp-weixin` → 微信后台出现开发版

### 小程序 Job 额外 Credential

| ID | Kind | 内容 |
|----|------|------|
| `wechat-upload-key` | Secret file | `private.wxf70d043d359a1586.key` |

## Task 10：GitHub Webhook

### Jenkins GitHub Server

**Manage Jenkins → System → GitHub**

- Add GitHub Server
- API URL: `https://api.github.com`
- Credentials: `github-pat-readonly`
- Test connection → Success

### GitHub 仓库 Webhook

**Settings → Webhooks → Add webhook**

| 字段 | 值 |
|------|-----|
| Payload URL | `http://8.134.110.218/jenkins/github-webhook/` |
| Content type | application/json |
| Events | Just the push event |

保存后 **Recent Deliveries** 应显示 `200`。

### 路径过滤验证

| 改动目录 | 预期触发 Job |
|----------|--------------|
| `server/**` | 仅 `special-server` |
| `apps/admin/**` | 仅 `special-admin` |
| `apps/mobile/**` | 仅 `special-h5`（**不**触发 mp） |

## 故障排查

| 现象 | 检查 |
|------|------|
| clone 403 | PAT 权限、Credential ID |
| mvn OOM | ECS 内存；Maven `-T 1C` 已在 Jenkinsfile |
| rsync 权限 denied | jenkins 是否在 www-data 组 |
| sudo cp jar 失败 | `/etc/sudoers.d/jenkins-deploy` |
| mp 上传失败 | IP 白名单、私钥 appid |
| Webhook 不触发 | Nginx `/jenkins/` 反代、GitHub delivery 状态码 |
