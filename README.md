# 特教资源平台

特殊教育平台 — 为自闭症家庭、特教老师、机构、学校提供资源对接及服务。

## 技术栈

| 端 | 技术 |
|----|------|
| 小程序 / H5 / App | unibest + Vue3 + Wot Design Uni |
| Web 管理后台 | Vue3 + Element Plus |
| 后端 | RuoYi-Vue-Plus (Spring Boot 3) |
| 数据库 | MySQL 8 + Redis |

## 项目结构

```
special-xcx/
├── apps/
│   ├── mobile/          # unibest 用户端（小程序+H5+App）
│   └── admin/           # Vue3 管理后台
├── server/              # RuoYi-Vue-Plus 后端 + ruoyi-special 业务模块
└── docs/                # 文档（ER 设计、微信配置、隐私政策）
```

## 快速开始

### 1. 数据库

```bash
# 启动 MySQL + Redis（可选 Docker）
cd server/script/docker && docker compose up -d

# 导入 SQL（按顺序）
mysql -u root -p < server/script/sql/ry_vue.sql
mysql -u root -p < server/script/sql/ry_special.sql
```

### 2. 后端

```bash
cd server
# 配置 application-dev.yml 中的数据库连接
# 配置 special.wechat.app-id / app-secret
./mvnw spring-boot:run -pl ruoyi-admin
# 默认 http://localhost:8080
```

### 3. 移动端

```bash
cd apps/mobile
pnpm install
pnpm dev:h5          # H5  http://localhost:9000
pnpm dev:mp-weixin   # 微信小程序 → 导入 dist/dev/mp-weixin
```

### 4. 管理后台

```bash
cd apps/admin
pnpm install
pnpm dev             # http://localhost:5173
# 默认账号 admin / admin123（RuoYi 初始账号）
```

## 核心功能（MVP）

- 四类角色：家长、特教老师、机构管理员、学校管理员
- 资源库：课程、工具、老师、机构、评估工具
- 预约咨询：表单提交 + 状态流转
- 管理后台：资源/机构/预约 CRUD
- 微信登录：小程序 xcx 授权模式

## 文档

- [数据库 ER 设计](docs/database-er.md)
- [微信小程序配置](docs/wechat-config.md)
- [隐私政策模板](docs/privacy-policy.md)

## 默认 API

| 端 | 路径 |
|----|------|
| 移动端资源列表 | GET /special/mobile/resource/list |
| 移动端预约 | POST /special/mobile/appointment |
| 管理端资源 | /special/resource/** |
| 登录 | POST /auth/login |
