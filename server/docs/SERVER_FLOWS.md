# Server 后端全量流程解析

> 基于 RuoYi-Vue-Plus 6.0.0 特教平台后端，代码路径 `/server`  
> 入口：`ruoyi-admin` | 端口：8080 | API 文档：http://localhost:8080/doc.html

---

## 一、项目总览

**技术栈：** Spring Boot 4.1 + Java 21 + MyBatis-Plus 3.5 + Sa-Token JWT 1.45 + Redisson 4.6 + MySQL + Warm-Flow 1.8 + LiteFlow 2.16

### 1.1 模块架构

```
server/
├── ruoyi-admin/          # Spring Boot 启动入口，聚合所有业务模块
├── ruoyi-api/            # 跨模块 API 契约（LoginUser、DTO、事件）
├── ruoyi-common/         # 25 个公共基础设施子模块
├── ruoyi-modules/        # 7 个业务模块
│   ├── ruoyi-system      # 系统管理（用户/角色/菜单/字典/OSS/日志）
│   ├── ruoyi-special     # 【项目核心】特教资源/机构/预约
│   ├── ruoyi-workflow    # Warm-Flow 工作流
│   ├── ruoyi-ai          # AI 集成
│   ├── ruoyi-job         # SnailJob 定时任务客户端
│   ├── ruoyi-gen         # 代码生成器
│   └── ruoyi-demo        # 示例/演示 API
├── ruoyi-extend/          # 独立部署扩展服务
│   ├── ruoyi-monitor-admin
│   ├── ruoyi-snailjob-server
│   └── ruoyi-snailai-server
└── script/sql/           # 数据库初始化脚本
```

**启动命令：**

```bash
mysql -u root -p ry-vue < script/sql/ry_vue.sql
mysql -u root -p ry-vue < script/sql/ry_special.sql
./mvnw spring-boot:run -pl ruoyi-admin
```

**主类：** `org.dromara.DromaraApplication`（`ruoyi-admin/src/main/java/org/dromara/DromaraApplication.java`）

### 1.2 依赖关系

```
ruoyi-admin
  ├── ruoyi-api
  ├── ruoyi-system / ruoyi-special / ruoyi-workflow / ruoyi-ai / ruoyi-job / ruoyi-demo
  └── ruoyi-common-{satoken, security, web, mybatis, redis, oss, push, ...}
```

---

## 二、HTTP 请求全链路

每个 HTTP 请求从进入到响应，依次经过以下层次：

```
Client
  ↓
[1] SaTokenContextFilter      — 初始化 Sa-Token 上下文（支持 SSE/Async/Error）
  ↓
[2] CryptoFilter                — RSA/AES 接口加解密（dev 默认关闭）
  ↓
[3] XssFilter                   — XSS 过滤（xss.enabled=true 时生效）
  ↓
[4] RepeatableFilter            — 包装 RequestBody 支持重复读取
  ↓
[5] CorsFilter                  — 跨域处理
  ↓
[6] SaServletFilter             — /actuator/** Basic Auth
  ↓
[7] SaInterceptor               — 全局登录校验 + clientid 绑定 + 路径/IP 白名单
  ↓
[8] PlusWebInvokeTimeInterceptor — 请求参数日志 + 耗时统计
  ↓
[9] AOP 切面                    — @Log / @RepeatSubmit / @RateLimiter / @DataPermission
  ↓
[10] Controller → Service → Mapper → MySQL / Redis
  ↓
[11] ResponseEnhancementAdvice  — JSON 字段增强（翻译/脱敏）
  ↓
Client ← R<T> JSON 响应
```

### 2.1 关键配置文件

| 文件 | 路径 |
|------|------|
| 主配置 | `ruoyi-admin/src/main/resources/application.yml` |
| 开发环境 | `ruoyi-admin/src/main/resources/application-dev.yml` |
| 安全拦截 | `ruoyi-common-security/.../config/SecurityConfig.java` |
| 过滤器注册 | `ruoyi-common-web/.../config/FilterConfig.java` |
| 日志配置 | `ruoyi-admin/src/main/resources/logback-plus.xml` |

### 2.2 AOP 横切关注点

| 注解 | 作用 | 实现 |
|------|------|------|
| `@Log` | 操作审计 | `LogAspect` → 异步 `OperLogEvent` → `sys_oper_log` |
| `@RepeatSubmit` | 防重复提交 | `RepeatSubmitAspect` + Redis |
| `@RateLimiter` | 接口限流 | `RateLimiterAspect` + Redisson |
| `@DataPermission` | 行级数据权限 | `PlusDataPermissionInterceptor` 注入 SQL WHERE |

### 2.3 统一响应与异常

**响应格式：**

```json
{ "code": 200, "msg": "操作成功", "data": { ... } }
```

**异常处理器：**

| 处理器 | 场景 |
|--------|------|
| `GlobalExceptionHandler` | 参数校验、业务异常、500 |
| `SaTokenExceptionHandler` | 401 未登录 / 403 无权限 |
| `MybatisExceptionHandler` | 数据库异常、重复键 |
| `RedisExceptionHandler` | Redis 异常 |
| `FlowExceptionHandler` | 工作流异常 |

---

## 三、认证与授权流程

### 3.1 登录流程

**入口：** `POST /auth/login` → `AuthController.login()`（类级 `@SaIgnore`）

```
POST /auth/login
  ↓ 解析 LoginBody（clientId + grantType + 凭证）
  ↓ 校验 sys_client 表（客户端存在 / grantType 授权 / 状态正常）
  ↓ 策略分发 IAuthStrategy.login(body, client, grantType)
  ↓ buildLoginUser() — 并行加载菜单权限、角色、部门、岗位
  ↓ LoginHelper.login() — StpUtil.login + Redis Session
  ↓ 返回 LoginVo { access_token, expire_in, client_id, openid? }
  ↓ 5 秒后异步推送欢迎消息（MessageService.publishMessage）
```

**5 种 grantType 策略：**

| grantType | Bean | 类 | 说明 |
|-----------|------|-----|------|
| `password` | `passwordAuthStrategy` | `PasswordAuthStrategy` | 用户名 + BCrypt 密码 + 验证码 |
| `xcx` | `xcxAuthStrategy` | `XcxAuthStrategy` | 微信小程序 wx.login code |
| `sms` | `smsAuthStrategy` | `SmsAuthStrategy` | 手机号 + 短信验证码 |
| `email` | `emailAuthStrategy` | `EmailAuthStrategy` | 邮箱 + 验证码 |
| `social` | `socialAuthStrategy` | `SocialAuthStrategy` | 第三方 OAuth（微信/QQ 等） |

**密码登录详细步骤：**

1. 校验验证码（Redis，dev 环境 `captcha.enable: false` 可关闭）
2. 按 username 查 `sys_user`
3. BCrypt 密码比对；失败计数 Redis key `pwd_err_cnt:{username}`，5 次锁定 10 分钟
4. `SysLoginService.buildLoginUser()` 并行加载权限/角色/部门
5. `LoginHelper.login()` 签发 JWT，完整 `LoginUser` 存入 Redis Session

**小程序登录（xcx）：**

1. 前端 `wx.login()` 获取 `xcxCode`
2. 后端调用微信 `jscode2session` 换取 `openid`（配置 `special.wechat.app-id/app-secret`）
3. **TODO：** `XcxAuthStrategy.loadUserByOpenid()` 尚未实现用户绑定，当前返回空 `SysUserVo`
4. 构建 `XcxLoginUser`，签发 Token

**请求头格式：**

```
Authorization: Bearer <jwt_token>
clientid: <client_id>
```

### 3.2 其他认证接口

| 接口 | 说明 |
|------|------|
| `POST /auth/logout` | 登出：StpUtil.logout + 清除在线缓存 + 写登录日志 |
| `POST /auth/register` | 注册（需系统配置开启） |
| `GET /auth/binding/{source}` | 第三方 OAuth 跳转地址 |
| `POST /auth/social/callback` | 社交账号绑定（需已登录） |
| `DELETE /auth/unlock/{socialId}` | 取消社交授权 |
| `GET /auth/code` | 图形验证码 |
| `GET /resource/sms/code` | 短信验证码 |
| `GET /resource/email/code` | 邮箱验证码 |

### 3.3 Token 生命周期

| 环节 | 实现 |
|------|------|
| 签发 | `LoginHelper.login()` → `StpUtil.login(loginId, extras)` |
| LoginId | `{userType}:{userId}`（如 `sys_user:1`） |
| 存储 | `PlusSaTokenDao`（Caffeine 本地 5s + Redis） |
| JWT 配置 | `is-concurrent: true`（多设备），`is-share: false`（独立 Token） |
| Session 内容 | 完整 `LoginUser`（权限/角色/部门）存 Redis，不在 JWT payload |
| Token 扩展字段 | userId, userName, deptId, clientid, clientAccessPath, clientIpWhitelist |
| 登出 | `StpUtil.logout()` + 清除 `ONLINE_TOKEN_KEY` + `LoginInfoEvent` |

### 3.4 RBAC 权限校验

**数据模型：**

```
sys_user ──< sys_user_role >── sys_role ──< sys_role_menu >── sys_menu(perms)
```

**校验流程：**

```
Controller 方法
  ↓ @SaCheckPermission("module:resource:action")?
  ↓ SaPermissionImpl.getPermissionList()
  ↓ 从 Token Session 读取 LoginUser.menuPermission
  ↓ 包含所需权限? → 否：403 NotPermissionException
  ↓ @DataPermission?
  ↓ PlusDataPermissionHandler 注入 SQL WHERE（按角色 dataScope）
  ↓ 执行业务逻辑
```

- **权限格式：** `模块:资源:操作`（如 `special:appointment:list`）
- **超级管理员（userId=1）：** 自动获得 `*:*:*` 和 `admin` 角色
- **数据权限 dataScope：** 全部 / 自定义 / 本部门 / 本部门及以下 / 仅本人

### 3.5 多客户端认证

`sys_client` 表支持 Web / 小程序 / App 等多客户端：

- 每个客户端配置允许的 `grantType`、设备类型、路径白名单、IP 白名单
- 每次请求校验 Header `clientid` 与 Token 中 `clientid` 一致，否则 `-100` 错误
- 可选路径白名单（`clientAccessPath`）和 IP 白名单（`clientIpWhitelist`）校验

---

## 四、系统管理模块（ruoyi-system）

### 4.1 用户管理 — `/system/user`

| 接口 | 流程 |
|------|------|
| `GET /getInfo` | 当前用户信息 + permissions + roles |
| `GET /list` | 分页查询（含数据权限过滤） |
| `POST` | 新增 → BCrypt 加密密码 → 关联角色/岗位 |
| `PUT /authRole` | 分配角色 |
| `PUT /resetPwd` | 重置密码 |
| `POST /importData` | Excel 批量导入 |

### 4.2 角色与菜单

**角色** — `/system/role`：CRUD + `PUT /permission` 批量更新菜单权限 + 数据权限范围

**菜单** — `/system/menu`：
- `GET /getRouters` — 前端动态路由（按权限过滤菜单树）
- CRUD + 级联删除

### 4.3 字典与配置

- **字典：** `/system/dict/type` + `/system/dict/data`，Redis 缓存，`DELETE /refreshCache` 刷新
- **参数：** `/system/config`，Redis 缓存

### 4.4 文件上传 — `/resource/oss`

```
MultipartFile
  → SysOssServiceImpl.upload()
  → OssFactory.instance()（从 Redis 读 SYS_OSS_CONFIG）
  → DefaultOssClientImpl（S3 协议）
  → MinIO / 阿里云 / 腾讯云 COS
  → 写入 sys_oss 元数据
```

- 单文件 10MB，总请求 20MB
- OSS 配置支持多租户热更新（`OssConfigChangeListener`）

### 4.5 监控 — `/monitor/*`

| 路径 | 功能 |
|------|------|
| `/monitor/online` | 在线用户查看 / 强制踢出 |
| `/monitor/operlog` | 操作日志查询 / 导出 / 清理 |
| `/monitor/loginInfo` | 登录日志 / 解锁账号 |
| `/monitor/cache` | Redis 缓存信息 |

---

## 五、特教业务模块（ruoyi-special）— 项目核心

### 5.1 数据模型

| 表 | 实体 | 说明 |
|----|------|------|
| `special_resource` | `SpecialResource` | 特教资源（course/tool/teacher/org/assessment） |
| `special_organization` | `SpecialOrganization` | 机构/学校 |
| `special_appointment` | `SpecialAppointment` | 预约记录 |

**状态机：**

| 实体 | 状态流转 |
|------|----------|
| 资源 status | 0 草稿 → 1 已发布 → 2 已下架 |
| 机构 audit_status | 0 待审 → 1 通过 → 2 拒绝 |
| 机构 status | 0 停用 / 1 正常 |
| 预约 appoint_status | 0 待处理 → 1 已联系 → 2 已完成 → 3 已取消 |

### 5.2 管理端 CRUD

标准 RuoYi 三层结构：

```
Controller (@SaCheckPermission)
  → ISpecial*Service
  → Special*Mapper (BaseMapperPlus)
  → MySQL
```

| 模块 | 路径 | 权限前缀 |
|------|------|----------|
| 资源 | `/special/resource` | `special:resource:*` |
| 机构 | `/special/organization` | `special:organization:*` |
| 预约 | `/special/appointment` | `special:appointment:*` |

标准接口：`GET /list` | `GET /{id}` | `POST` | `PUT` | `DELETE /{ids}` | `POST /export`

### 5.3 移动端公开 API — `/special/mobile`

`SpecialMobileController` 标注 `@SaIgnore`，**无需登录**。

| 接口 | Service 方法 | 业务规则 |
|------|-------------|----------|
| `GET /resource/list` | `queryPublishedPageList()` | 仅 `status=1` 已发布资源 |
| `GET /resource/{id}` | `queryPublishedById()` | 已发布资源详情 + 浏览量 +1 |
| `GET /organization/list` | `queryApprovedPageList()` | 仅 `audit_status=1` 且 `status=1` |
| `POST /appointment` | `createMobileAppointment()` | 见下方流程 |

**预约创建流程：**

```
POST /special/mobile/appointment
  ↓ 校验 resourceId 对应资源存在且 status=1
  ↓ 自动填充 resourceTitle 快照
  ↓ 若已登录 → 自动关联 userId（LoginHelper.getUserId()）
  ↓ 若未登录 → 匿名预约（userId 为空）
  ↓ 默认 appointStatus=0（待处理）
  ↓ 写入 special_appointment
```

### 5.4 特教角色体系

| role_key | 说明 |
|----------|------|
| `special_parent` | 家长 |
| `special_teacher` | 特教老师 |
| `special_org_admin` | 机构管理员 |
| `special_school_admin` | 学校管理员 |

---

## 六、工作流模块（ruoyi-workflow）

基于 **Warm-Flow** 引擎 + **LiteFlow** 规则编排。

### 6.1 流程定义 — `/workflow/definition`

- CRUD + `PUT /publish/{id}` 发布 + `PUT /unPublish/{id}` 取消发布
- `POST /importDef` / `POST /exportDef/{id}` 导入导出
- `GET /xmlString/{id}` 获取流程 XML

### 6.2 流程实例 — `/workflow/instance`

- `GET /pageByRunning` — 运行中实例
- `GET /pageByFinish` — 已完成实例
- `GET /getInfo/{businessId}` — 按业务 ID 查实例
- `PUT /cancelProcessApply` — 撤销申请
- `DELETE /deleteByInstanceIds/{instanceIds}` — 删除实例

### 6.3 任务处理 — `/workflow/task`

| 接口 | 说明 |
|------|------|
| `POST /startWorkFlow` | 启动流程实例 |
| `POST /completeTask` | 审批/完成任务 |
| `GET /pageByTaskWait` | 我的待办 |
| `GET /pageByTaskFinish` | 我的已办 |
| `POST /backProcess` | 退回 |
| `POST /terminationTask` | 终止流程 |
| `POST /urgeTask` | 催办 |

**LiteFlow 节点编排：** `start/` → `operation/` → `complete/` → `instance/`

**示例业务：** `TestLeaveController`（`/workflow/leave`）— 请假流程 demo，`POST /submitAndFlowStart` 提交并启动。

---

## 七、消息推送流程

默认 **SSE**，可切换 WebSocket（`message.transport`）。

```
GET /resource/message（需登录，建立 SSE 连接）
  → SseEmitterSessionManager.connect(userId, tokenValue)
  → 业务触发 MessageService.publishMessage()
  → Redis Pub/Sub（MessageTopicListener）
  → 集群广播到所有 SSE Session

GET /resource/message/close（@SaIgnore，关闭连接）
```

配置（`application.yml`）：

```yaml
message:
  enabled: true
  transport: sse          # 或 websocket
  path: /resource/message
```

---

## 八、定时任务（ruoyi-job）

使用 **SnailJob** 分布式调度（非 Spring `@Scheduled`）。

```
SnailJob Server（ruoyi-extend/ruoyi-snailjob-server）
  → 调度
SnailJob Client（snail-job.enabled=true）
  → @JobExecutor 注解任务类
  → WechatBillTask / AlipayBillTask 等
```

- dev 环境默认 `snail-job.enabled: false`
- 任务类：`ruoyi-modules/ruoyi-job/src/main/java/org/dromara/job/snailjob/`

---

## 九、缓存架构

```
@Cacheable → PlusSpringCacheManager
  ├── Caffeine L1 本地缓存（30s）
  └── Redis L2 分布式缓存

Redis 用途：
  ├── Sa-Token Session
  ├── 验证码
  ├── 密码错误计数（pwd_err_cnt:{username}）
  ├── 在线用户（ONLINE_TOKEN_KEY）
  ├── 字典/配置/OSS 配置
  ├── 防重复提交
  └── 接口限流
```

缓存命名：`cacheName#ttl#maxIdle#maxSize#local`（见 `CacheNames.java`）

---

## 十、数据库访问流程

```
Service
  → Mapper（extends BaseMapperPlus<Entity, Vo>）
  → 查询类型：
      ├── 简单 CRUD → selectVoById / selectVoPage / selectVoList
      ├── 复杂查询 → XML Mapper（mapper/**/*Mapper.xml）
      └── 关联查询 → MyBatis-Join (MPJ)
  → PlusDataPermissionInterceptor（数据权限 WHERE）
  → PaginationInnerInterceptor（分页）
  → OptimisticLockerInnerInterceptor（乐观锁）
  → InjectionMetaObjectHandler（自动填充 createTime/updateTime）
  → HikariCP → MySQL（ry-vue）
```

| 配置项 | 值 |
|--------|-----|
| 主键策略 | Snowflake 分布式 ID（ASSIGN_ID） |
| 逻辑删除 | `@TableLogic` → `delFlag` |
| 数据源 | dynamic-datasource，dev 单库 `master` |
| Mapper 扫描 | `org.dromara.**.mapper` |

---

## 十一、Service 层标准模式

所有业务模块遵循统一分层：

```
Controller (extends BaseController)
  ↓ @SaCheckPermission / @SaIgnore
  ↓ @Validated + AddGroup/EditGroup/QueryGroup
IService (interface)
  ↓
ServiceImpl (@Service, @RequiredArgsConstructor)
  ↓ MapstructUtils.convert(bo, Entity.class)
  ↓ LambdaQueryWrapper + buildQueryWrapper(bo)
Mapper (extends BaseMapperPlus<Entity, Vo>)
  ↓ selectVoById / selectVoPage / selectVoList
Entity (@TableName) ↔ BO (输入) ↔ VO (输出)
```

**通用约定：**

- 分页：`PageQuery` → `PageResult<T>`
- 响应：`R<T>` 包装，`toAjax(boolean)` 用于写操作
- 校验：`@Validated` + 分组校验
- 事务：`@Transactional` 用于多步写入
- 缓存：`@Cacheable` / `@CacheEvict` 用于系统模块
- 审计：`@Log(title, businessType)` 自动记录操作日志

---

## 十二、启动与初始化

```
./mvnw spring-boot:run -pl ruoyi-admin
  → DromaraApplication.main()
  → Spring Boot AutoConfiguration（25 个 common 模块）
  → AllUrlHandler 扫描所有 @RequestMapping
  → SaInterceptor 注册路由规则
  → MyBatis MapperScan
  → Redis/Redisson 连接
  → 应用就绪 :8080
```

**SQL 脚本：**

| 文件 | 内容 |
|------|------|
| `script/sql/ry_vue.sql` | RuoYi 基础表 |
| `script/sql/ry_special.sql` | 特教业务表 + 角色/菜单 |
| `script/sql/ry_workflow.sql` | 工作流表 |
| `script/sql/ry_job.sql` | SnailJob 表 |
| `script/sql/ry_ai.sql` | AI 模块表 |

---

## 十三、API 路由总览

| 前缀 | 模块 | 认证 |
|------|------|------|
| `/auth/*` | 认证（登录/注册/登出） | `@SaIgnore` |
| `/system/*` | 系统管理 | 需登录 + 权限 |
| `/monitor/*` | 系统监控 | 需登录 + 权限 |
| `/resource/oss/*` | 文件上传/下载 | 需登录 |
| `/resource/message` | SSE 消息推送 | 需登录（close 端点 `@SaIgnore`） |
| `/special/resource/*` | 特教资源管理 | 需登录 + 权限 |
| `/special/organization/*` | 特教机构管理 | 需登录 + 权限 |
| `/special/appointment/*` | 特教预约管理 | 需登录 + 权限 |
| `/special/mobile/*` | 移动端公开 API | `@SaIgnore` |
| `/workflow/*` | 工作流 | 需登录 + 权限 |
| `/tool/gen/*` | 代码生成 | 需登录 + 权限 |
| `/demo/*` | 示例/演示 | 需登录 |
| `/snail-ai/*` | AI 接口 | 条件启用 |

---

## 十四、待完善项

1. **小程序用户绑定：** `XcxAuthStrategy.loadUserByOpenid()` 返回空 `SysUserVo`，需实现 openid → sys_user 绑定逻辑
2. **特教 OSS：** `special.oss` 腾讯云 COS 配置存在但 `enabled: false`
3. **SnailJob/SnailAI：** dev 环境默认关闭，生产需单独启动 extend 服务

---

## 十五、端到端业务场景示例

### 场景 A：管理端发布资源

```
1. POST /auth/login（grantType=password）→ 获取 Token
2. GET /system/menu/getRouters → 获取动态菜单
3. POST /special/resource（special:resource:add）→ 创建草稿 status=0
4. PUT /special/resource（special:resource:edit）→ 修改 status=1 发布
5. 移动端即可通过 GET /special/mobile/resource/list 看到
```

### 场景 B：小程序用户浏览并预约

```
1. GET /special/mobile/resource/list → 浏览已发布资源（无需登录）
2. GET /special/mobile/resource/{id} → 查看详情（浏览量+1）
3. GET /special/mobile/organization/list → 浏览已审核机构
4. POST /special/mobile/appointment → 提交预约（无需登录，填写 contactName/Phone）
5. 管理端 GET /special/appointment/list → 查看待处理预约
6. PUT /special/appointment → 更新 appointStatus=1 已联系
```

### 场景 C：小程序登录（待完善）

```
1. wx.login() → 获取 xcxCode
2. POST /auth/login（grantType=xcx, xcxCode, clientId）→ 换取 openid + Token
3. 【TODO】openid 绑定 sys_user 尚未实现
4. 登录后 POST /special/mobile/appointment → 自动关联 userId
```
