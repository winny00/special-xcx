# Jenkins UI 初始化清单（ECS）

在 ECS 上执行 `scripts/ecs/install-jenkins.sh` 后，按本清单完成 Jenkins 向导与插件配置。

**Spec / Plan:** `docs/superpowers/specs/2026-08-19-jenkins-github-cicd-design.md`

## 1. 完成安装向导

浏览器打开 `http://8.134.110.218/jenkins/`。

Nginx 尚未配置 `/jenkins/` 前，可用 SSH 隧道：

```bash
ssh -L 8081:127.0.0.1:8081 root@8.134.110.218
# 然后访问 http://127.0.0.1:8081/jenkins/
```

向导步骤：

1. 输入 `initialAdminPassword`（安装脚本输出，或 `/var/lib/jenkins/secrets/initialAdminPassword`）
2. 选择 **Install suggested plugins**
3. 创建 admin 用户（强密码）
4. Jenkins URL 设为 `http://8.134.110.218/jenkins/`（备案后可改 HTTPS）

## 2. 安装额外插件

**Manage Jenkins → Plugins → Available plugins**，安装：

- GitHub Integration
- Pipeline: GitHub Groovy
- Credentials Binding
- Timestamper

安装后 **Restart Jenkins**。

## 3. 添加 GitHub PAT Credential

**Manage Jenkins → Credentials → System → Global → Add Credentials**

| 字段 | 值 |
|------|-----|
| Kind | Secret text |
| Secret | `<GitHub PAT>`（Task 1 创建的只读 token） |
| ID | `github-pat-readonly` |
| Description | GitHub read-only for special-xcx |

## 4. 配置 Global Tool（可选但推荐）

**Manage Jenkins → Tools**

| 工具 | 名称 | 路径 / 版本 |
|------|------|-------------|
| JDK | `jdk-21` | `/usr/lib/jvm/java-21-openjdk-amd64` |
| Maven | `maven-3` | 自动安装或 `/usr/share/maven` |
| NodeJS | `node-20` | 20.x |

## 5. 下一步

- Task 4：应用 `scripts/ecs/nginx-special.conf`、创建 `/var/www/h5`、配置 jenkins 部署权限
- Task 9：创建 4 条 Pipeline Job（`special-server`、`special-admin`、`special-h5`、`special-mp-weixin`）
- Task 10：配置 GitHub Webhook
