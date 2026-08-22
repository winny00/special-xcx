# 一人一号多角色 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 一个手机号一个 `sys_user`，可叠家长/老师；小程序支持手机号密码 + 微信一键 + 家长注册；老师仅后台开通，两端只看自己的资料和预约。

**Architecture:** 不改 `POST /auth/login` 形态与 `Clientid`。角色继续走 `sys_user_role`。`currentRole` 存在 Sa-Token token 会话键 `specialCurrentRole`。手机号当合并键叠角色。预约加可空 `teacher_id` 作为老师数据范围。

**Tech Stack:** RuoYi-Vue-Plus / Sa-Token、JUnit 5 + Mockito、uni-app Vue3 + Vitest、Vue3 Element Plus

**Spec:** `docs/superpowers/specs/2026-08-22-identity-roles-design.md`

## Global Constraints

- 不改 `POST /auth/login` 的 `clientId` + `grantType` 约定；小程序 `special_xcx_client_id`，PC `e5cd7e4891bf95d1d19206ce24a7b32e`
- 雪花 ID 前端全程 `String(id)`；禁止 `Number(id)`
- 主色 `#1B7F6B`；身份 Tag 必须有文字（家长 / 老师）；可点区域 ≥ 44px
- `grantType=sms` 不当主登录入口；本计划不做解绑微信、忘记密码自助、机构/学校管理员、菜单树
- MySQL 5.7：加列用 `scripts/ecs/migrate-phase2b-teacher-audit.sql` 同款存储过程，禁止 `ADD COLUMN IF NOT EXISTS`
- 纯逻辑先写失败测试再写实现；命令：后端 `mvn -f server/pom.xml -pl ruoyi-modules/ruoyi-special -am test -Dtest=<Class> -q`；小程序 `pnpm --dir apps/mobile test:run`；后台 `pnpm --dir apps/admin exec vitest run <file>`

---

## File Map

| 文件 | 职责 |
|------|------|
| `server/.../util/SpecialIdentitySupport.java` | 手机号判断、默认角色、PC 准入、叠角色校验、切换校验 |
| `server/.../util/SpecialBindPhonePlanner.java` | 绑手机：写号 / 合并 / 冲突 |
| `server/.../util/SpecialCurrentRoleStore.java` | token 会话读写 `specialCurrentRole` |
| `server/.../service/ISpecialAccountService.java` + Impl | 后台开关角色、重置密码、建老师时合并账号 |
| `server/.../controller/SpecialAccountController.java` | `/special/account/**` |
| `PasswordAuthStrategy.java` | 11 位按手机号查；PC 仅家长拒绝；登录后写 currentRole |
| `AuthController.java` | xcx client 注册走家长开通；`PUT /auth/current-role` |
| `CaptchaController.java` | 测试码 / 短信未开通文案 |
| `SpecialMobileMeController.java` | bind-phone、teacher-profile、预约按 currentRole 分流 |
| `SpecialAppointment.java` + SQL | 可空 `teacher_id` |
| `apps/mobile/src/utils/current-role.ts` | 本地缓存与回落 |
| `apps/mobile/src/pages/auth/login.vue` / `register.vue` / `bind-phone.vue` | 登录注册绑手机 |
| `apps/mobile/src/pages/index/index.vue` / `me/me.vue` | 按角色切首页与「我的」 |
| `apps/admin/src/utils/teacher-payload.ts` | 增加手机号/初始密码 |
| `apps/admin/src/utils/admin-access.ts` | 超管 vs 老师侧栏/路由 |
| `apps/admin/src/views/account/Index.vue` | 用户角色页 |
| `scripts/ecs/migrate-identity-roles.sql` | `teacher_id`、菜单、老师角色权限 |

---

### Task 1: 身份规则纯函数

**Files:**
- Create: `server/ruoyi-modules/ruoyi-special/src/main/java/org/dromara/special/util/SpecialIdentitySupport.java`
- Create: `server/ruoyi-modules/ruoyi-special/src/test/java/org/dromara/special/util/SpecialIdentitySupportTest.java`

**Interfaces:**
- Consumes: 无
- Produces:
  - `PARENT_ROLE_KEY = "special_parent"`
  - `TEACHER_ROLE_KEY = "special_teacher"`
  - `SUPERADMIN_ROLE_KEY = "superadmin"`
  - `PC_CLIENT_ID = "e5cd7e4891bf95d1d19206ce24a7b32e"`
  - `XCX_CLIENT_ID = "special_xcx_client_id"`
  - `boolean isPhoneLogin(String username)` — 恰好 11 位数字
  - `String defaultCurrentRole(Set<String> roleKeys)` — 有家长用家长，否则老师，否则 `null`
  - `boolean canSwitchTo(Set<String> owned, String target)`
  - `boolean canAccessPcAdmin(Set<String> roleKeys)` — 含 `superadmin` 或 `special_teacher`
  - `void assertKeepAtLeastOneRole(boolean parent, boolean teacher)` — 两个都 false 抛 `IllegalArgumentException("至少保留一个角色")`

- [ ] **Step 1: Write the failing test**

```java
@Tags({@Tag("local"), @Tag("dev"), @Tag("prod")})
class SpecialIdentitySupportTest {
    @Test
    void phoneLoginIsElevenDigits() {
        assertTrue(SpecialIdentitySupport.isPhoneLogin("13800138000"));
        assertFalse(SpecialIdentitySupport.isPhoneLogin("admin"));
        assertFalse(SpecialIdentitySupport.isPhoneLogin("1380013800"));
        assertFalse(SpecialIdentitySupport.isPhoneLogin(null));
    }

    @Test
    void defaultRolePrefersParentThenTeacher() {
        assertEquals("special_parent", SpecialIdentitySupport.defaultCurrentRole(Set.of("special_parent", "special_teacher")));
        assertEquals("special_teacher", SpecialIdentitySupport.defaultCurrentRole(Set.of("special_teacher")));
        assertNull(SpecialIdentitySupport.defaultCurrentRole(Set.of("superadmin")));
    }

    @Test
    void switchOnlyToOwnedSpecialRoles() {
        Set<String> both = Set.of("special_parent", "special_teacher");
        assertTrue(SpecialIdentitySupport.canSwitchTo(both, "special_teacher"));
        assertFalse(SpecialIdentitySupport.canSwitchTo(Set.of("special_parent"), "special_teacher"));
        assertFalse(SpecialIdentitySupport.canSwitchTo(both, "superadmin"));
    }

    @Test
    void pcAccess() {
        assertTrue(SpecialIdentitySupport.canAccessPcAdmin(Set.of("superadmin")));
        assertTrue(SpecialIdentitySupport.canAccessPcAdmin(Set.of("special_teacher", "special_parent")));
        assertFalse(SpecialIdentitySupport.canAccessPcAdmin(Set.of("special_parent")));
    }

    @Test
    void cannotDropLastRole() {
        assertThrows(IllegalArgumentException.class,
            () -> SpecialIdentitySupport.assertKeepAtLeastOneRole(false, false));
        SpecialIdentitySupport.assertKeepAtLeastOneRole(true, false);
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `mvn -f server/pom.xml -pl ruoyi-modules/ruoyi-special -am test -Dtest=SpecialIdentitySupportTest -q`

Expected: FAIL（类不存在）

- [ ] **Step 3: Minimal implementation** matching the assertions above.

- [ ] **Step 4: Re-run — expected PASS**

- [ ] **Step 5: Commit**

```bash
git add server/ruoyi-modules/ruoyi-special/src/main/java/org/dromara/special/util/SpecialIdentitySupport.java \
  server/ruoyi-modules/ruoyi-special/src/test/java/org/dromara/special/util/SpecialIdentitySupportTest.java
git commit -m "feat(special): encode parent/teacher identity rules"
```

---

### Task 2: 密码登录认手机号、PC 拒家长、写入 currentRole

**Files:**
- Create: `server/ruoyi-modules/ruoyi-special/src/main/java/org/dromara/special/util/SpecialCurrentRoleStore.java`
- Modify: `server/ruoyi-admin/src/main/java/org/dromara/web/service/impl/PasswordAuthStrategy.java`
- Modify: `server/ruoyi-admin/src/main/java/org/dromara/web/service/impl/XcxAuthStrategy.java`（登录成功后同样 `SpecialCurrentRoleStore.applyDefault`）
- Modify: `server/ruoyi-api/src/main/java/org/dromara/system/api/model/LoginUser.java` 增加 `String currentRole`
- Test: `server/ruoyi-modules/ruoyi-special/src/test/java/org/dromara/special/util/SpecialCurrentRoleStoreTest.java`（只测 `pickRoleForLogin`：PC 老师强制 `special_teacher`；xcx 用 defaultCurrentRole）

**Interfaces:**
- Consumes: `SpecialIdentitySupport`
- Produces:
  - `SpecialCurrentRoleStore.SESSION_KEY = "specialCurrentRole"`
  - `String pickRoleForLogin(String clientId, Set<String> roleKeys)`
    - PC + 老师（非超管）→ `special_teacher`
    - 其他 → `defaultCurrentRole(roleKeys)`
  - `void write(String roleKey)` — `StpUtil.getTokenSession().set(SESSION_KEY, roleKey)`，并 `LoginHelper.getLoginUser().setCurrentRole(roleKey)`
  - `String read()` — 会话没有则 `null`

PasswordAuthStrategy 改动要点（保持 `grantType=password` 请求体字段仍为 `username`）：

```java
SysUserVo user;
if (SpecialIdentitySupport.isPhoneLogin(username)) {
    user = userMapper.lambda().eq(SysUser::getPhoneNumber, username).voOne();
    if (user == null) {
        user = loadUserByUsername(username);
    } else if (SystemConstants.DISABLE.equals(user.getStatus())) {
        throw new UserException("user.blocked", username);
    }
} else {
    user = loadUserByUsername(username);
}
LoginUser loginUser = loginService.buildLoginUser(user);
if (SpecialIdentitySupport.PC_CLIENT_ID.equals(client.getClientId())
    && !SpecialIdentitySupport.canAccessPcAdmin(loginUser.getRolePermission())) {
    throw new ServiceException("无后台权限");
}
LoginHelper.login(loginUser, model);
SpecialCurrentRoleStore.write(
    SpecialCurrentRoleStore.pickRoleForLogin(client.getClientId(), loginUser.getRolePermission()));
```

注意：`SysUser` 手机字段为 `phoneNumber`。无家长且无老师且非超管时，xcx 登录后 `pickRoleForLogin` 为 null，抛 `ServiceException("账号未开通")`。

- [ ] **Step 1: Failing test** for `pickRoleForLogin`:

```java
assertEquals("special_teacher", SpecialCurrentRoleStore.pickRoleForLogin(
    SpecialIdentitySupport.PC_CLIENT_ID, Set.of("special_teacher", "special_parent")));
assertEquals("special_parent", SpecialCurrentRoleStore.pickRoleForLogin(
    SpecialIdentitySupport.XCX_CLIENT_ID, Set.of("special_parent", "special_teacher")));
```

- [ ] **Step 2: Run** `mvn -f server/pom.xml -pl ruoyi-modules/ruoyi-special -am test -Dtest=SpecialCurrentRoleStoreTest -q` — FAIL

- [ ] **Step 3: Implement store + wire PasswordAuthStrategy / XcxAuthStrategy**

- [ ] **Step 4: Re-run identity + current-role tests — PASS**

- [ ] **Step 5: Commit** `feat(auth): login by phone and seed currentRole`

---

### Task 3: 家长注册 + 验证码测试通道

**Files:**
- Create: `server/ruoyi-modules/ruoyi-special/src/main/java/org/dromara/special/service/impl/SpecialParentRegisterService.java`
- Create: `server/ruoyi-modules/ruoyi-special/src/test/java/org/dromara/special/service/impl/SpecialParentRegisterServiceTest.java`
- Modify: `AuthController.register`：`XCX_CLIENT_ID.equals(user.getClientId())` 时不看 `selectRegisterEnabled()`，改调 `SpecialParentRegisterService.register(user)`；其它 client 仍走原逻辑（默认关闭）
- Modify: `CaptchaController.smsCode`：若 `SmsFactory.getSmsBlend("config1")` 失败或配置占位，且 `special.sms.test-code-enabled=true`（只在 `application-dev.yml` 设 true），把 4 位码写入 Redis 并 `log.info("SMS test code {} -> {}", phoneNumber, code)`，返回成功；生产 yml 该开关 false，未配置短信则 `R.fail("短信通道未开通")`

**Interfaces:**
- Consumes: `RegisterBody.username` 当作手机号，`RegisterBody.password`，`RegisterBody.code` 当作短信验证码
- Produces: `void register(RegisterBody body)`
  - 校验 11 位手机号
  - Redis `CAPTCHA_CODE_KEY + phone` 与 `body.getCode()` 一致，否则抛「验证码无效」
  - 无用户：建 `sys_user`（`userName=phone`，`phonenumber=phone`，BCrypt 密码）+ `special_parent`
  - 有用户无家长：加家长；若 password 非空则更新密码
  - 已是家长：抛「账号已注册，请直接登录」

Mockito 测试（不启 Spring）：mock `SysUserMapper` / `ISysUserService` / `ISysRoleService` / Redis 包装。若 Redis 难 mock，把「验证码是否匹配」抽成 `SpecialIdentitySupport.smsCodeMatches(String expected, String actual)` 单测，Service 测叠角色分支。

```java
@Test
void alreadyParentIsRejected() {
    when(userMapper.findByPhone("13800138000")).thenReturn(existingParent);
    when(roleService.hasRole(existingParent.getUserId(), "special_parent")).thenReturn(true);
    ServiceException ex = assertThrows(ServiceException.class, () -> service.register(body));
    assertEquals("账号已注册，请直接登录", ex.getMessage());
}
```

`findByPhone` / `hasRole` 若仓库没有，在 Service 内用已有 `lambda().eq(SysUser::getPhonenumber, phone).voOne()` 与 `sys_user_role` 查询，测试 mock mapper。

- [ ] **Step 1–4:** 红灯 → 实现 → 绿灯

- [ ] **Step 5: Commit** `feat(auth): register parent by phone and sms code`

---

### Task 4: 切换身份接口 + getInfo 返回 currentRole

**Files:**
- Modify: `AuthController.java` 增加（**不要** `@SaIgnore`）：

```java
@PutMapping("/current-role")
public R<Void> switchCurrentRole(@RequestBody Map<String, String> body) {
    String roleKey = body.get("roleKey");
    LoginUser loginUser = LoginHelper.getLoginUser();
    if (!SpecialIdentitySupport.canSwitchTo(loginUser.getRolePermission(), roleKey)) {
        return R.fail("当前账号没有该身份");
    }
    SpecialCurrentRoleStore.write(roleKey);
    return R.ok();
}
```

- Modify: `UserInfoVo.java` 增加 `String currentRole`、`Boolean phoneBound`
- Modify: `SysUserController.getInfo`：`userInfoVo.setCurrentRole(SpecialCurrentRoleStore.read())`；若会话空则 `write(pickRoleForLogin(loginUser.getClientKey() 对应 clientId, roles))`；`phoneBound = StringUtils.isNotBlank(user.getPhonenumber())`

**Interfaces:**
- Consumes: Task 2 store
- Produces: `PUT /auth/current-role` `{ "roleKey": "special_parent" | "special_teacher" }`；`GET /system/user/getInfo` 含 `currentRole`、`phoneBound`、原 `roles`

- [ ] **Step 1:** `SpecialIdentitySupportTest` 已覆盖非法切换。给 `SpecialCurrentRoleStore.write/read` 若无法单测 Sa-Token，则手工：登录后 PUT 非法角色应 200 且 `code != 200` 或 R.fail。自动化：抽出 `switchOrMessage(owned, target)` 返回 `Optional<String>` 错误文案，单测它。

```java
assertEquals("当前账号没有该身份",
    SpecialIdentitySupport.switchError(Set.of("special_parent"), "special_teacher"));
assertNull(SpecialIdentitySupport.switchError(Set.of("special_parent", "special_teacher"), "special_teacher"));
```

把 `switchError` 加进 Task 1 的类（若 Task 1 已提交，本任务补方法 + 测试）。

- [ ] **Step 2–4:** 实现 AuthController + getInfo

- [ ] **Step 5: Commit** `feat(auth): switch currentRole in token session`

---

### Task 5: 绑手机合并规划器 + bind-phone（短信）

**Files:**
- Create: `server/ruoyi-modules/ruoyi-special/src/main/java/org/dromara/special/util/SpecialBindPhonePlanner.java`
- Create: `.../SpecialBindPhonePlannerTest.java`
- Create: `BindPhoneBody`：`phone`、`smsCode`、`wxPhoneCode`（本任务只处理 phone+smsCode；`wxPhoneCode` Task 11）
- Modify: `SpecialMobileMeController` `POST /special/mobile/me/bind-phone`
- Modify: `ISpecialMobileMeService` + Impl：事务内写号或合并

**Interfaces:**
- Produces:

```java
public enum BindAction { WRITE_PHONE, MERGE, REJECT }
public record BindPlan(BindAction action, Long keepUserId, Long disableUserId, String message) {}

public static BindPlan plan(Long currentUserId, String currentPhone, Long phoneOwnerId, boolean openidBoundToOther)
```

规则：
- `openidBoundToOther` → `REJECT`「该微信已绑定其他账号」
- `phoneOwnerId == null` → `WRITE_PHONE` keep=current
- `phoneOwnerId.equals(currentUserId)` → `WRITE_PHONE`
- 否则 `MERGE` keep=`phoneOwnerId` disable=`currentUserId`

合并实现：更新 `sys_social.user_id`；`special_appointment.user_id` 从 disable 改到 keep；keep 无家长则加家长；disable `status=停用`；签发 keep 的新 token，返回 `{ access_token, expire_in, client_id }`（与 LoginVo 同字段）。前端换 token。

- [ ] **Step 1: Failing tests** for `WRITE_PHONE` / `MERGE` / `REJECT`

- [ ] **Step 2:** `mvn ... -Dtest=SpecialBindPhonePlannerTest` FAIL

- [ ] **Step 3:** Planner + Service 事务实现；未登录或验证码错走现有异常

- [ ] **Step 4:** PASS

- [ ] **Step 5: Commit** `feat(special): bind phone and merge wechat temp users`

---

### Task 6: 小程序角色缓存 + 登录/注册页

**Files:**
- Create: `apps/mobile/src/utils/current-role.ts`
- Create: `apps/mobile/src/utils/current-role.test.ts`
- Modify: `apps/mobile/src/api/login.ts` — `register({ username: phone, password, code, clientId, grantType 不需要 })` POST `/auth/register`；`switchCurrentRole(roleKey)` PUT `/auth/current-role`；`getUserInfo` 映射 `currentRole`、`phoneBound`
- Modify: `apps/mobile/src/pages/auth/login.vue` — 文案「手机号」；保留密码登录；`#ifdef MP-WEIXIN` 微信一键；链接注册
- Modify: `apps/mobile/src/pages/auth/register.vue` — 手机号 + 验证码 + 密码；发码 `GET /resource/sms/code?phoneNumber=`
- Modify: `apps/mobile/src/store/token.ts` — `logout` 调 `clearCurrentRole()`

**Interfaces:**
- Produces:

```ts
export const CURRENT_ROLE_STORAGE_KEY = 'special_current_role'
export type SpecialRoleKey = 'special_parent' | 'special_teacher'
export function readCachedRole(): SpecialRoleKey | ''
export function writeCachedRole(role: SpecialRoleKey): void
export function clearCurrentRole(): void
export function resolveRole(cached: string, owned: string[]): SpecialRoleKey | ''
// cached 在 owned 里则用 cached；否则有 special_parent 用家长；否则老师；否则 ''
```

```ts
it('falls back when cached role was removed', () => {
  expect(resolveRole('special_teacher', ['special_parent'])).toBe('special_parent')
})
it('keeps cached teacher when still owned', () => {
  expect(resolveRole('special_teacher', ['special_parent', 'special_teacher'])).toBe('special_teacher')
})
```

登录页 input：`type="number"` 或 `maxlength="11"`；placeholder「请输入手机号」；高度 `h-11`（44px）。不要写「如 admin」。

登录成功 / 微信登录成功后：`getUserInfo` → `writeCachedRole(resolveRole(readCachedRole(), roles))`；若 `phoneBound===false` 不在登录页拦截（浏览仍允许）。

- [ ] **Step 1:** 写 `current-role.test.ts`，`pnpm --dir apps/mobile test:run` FAIL

- [ ] **Step 2:** 实现 `current-role.ts`，测试 PASS

- [ ] **Step 3:** 改 login/register/API/store

- [ ] **Step 4:** `pnpm --dir apps/mobile test:run` PASS

- [ ] **Step 5: Commit** `feat(mobile): phone login/register and role cache`

---

### Task 7: 小程序「我的」切换 + 老师/家长首页

**Files:**
- Create: `apps/mobile/src/pages/auth/bind-phone.vue`（手机号 + 短信验证码；调 bind-phone）
- Modify: `apps/mobile/src/pages/me/me.vue`
- Modify: `apps/mobile/src/pages/index/index.vue`
- Modify: `apps/mobile/src/api/me.ts` — profile 增加 `currentRole`、`roles: string[]`、`phoneBound`；`getMyTeacherProfile` / `updateMyTeacherProfile`
- Modify: `SpecialMobileProfileVo` 增加 `currentRole`、`List<String> roles`、`Boolean phoneBound`（完整手机号不要回列表，继续脱敏 `phone`）
- Modify: `SpecialMobileMeServiceImpl.getProfile` 用 `SpecialCurrentRoleStore.read()` 填 `currentRole`，不要再 `resolvePrimaryRole` 强行家长

**「我的」行为：**
- Tag 文字：`currentRole===special_teacher` → 「老师」否则 「家长」
- `roles` 同时含两个才显示「切换身份」；点选后 `PUT /auth/current-role` + `writeCachedRole` + `uni.switchTab({ url: '/pages/index/index' })`
- 未 `phoneBound`：点「我的预约」/「老师资料」先 `navigateTo` bind-phone
- 老师菜单：老师资料、收到的预约（同一 appointments 页，后端已按角色分流）、切换、关于、退出
- 家长菜单：我的预约、关于、退出
- `logout` 清角色缓存

**首页：** `currentRole===special_teacher` 时不渲染四宫格/搜索/分类资源流；改为老师摘要卡（姓名、待处理预约数、按钮去预约列表）。家长身份保持现有首页。`onShow` 读缓存角色以切换视图。

冷启动：`App.vue` 或 token store 有 token 时 `getUserInfo` + `resolveRole` + 若与服务器 `currentRole` 不同则 PUT 一次恢复。

- [ ] **Step 1:** 扩展 `current-role.test.ts` 已覆盖回落；UI 无单测则手工验收清单写入 PR 描述

- [ ] **Step 2–4:** 实现页面与 profile 字段；`pnpm --dir apps/mobile test:run` PASS

- [ ] **Step 5: Commit** `feat(mobile): switch parent/teacher identity in me and home`

---

### Task 8: 预约 `teacher_id` + 老师收到的预约

**Files:**
- Create: `scripts/ecs/migrate-identity-roles.sql`（本任务只加列；菜单放到 Task 9）
- Modify: `server/script/sql/ry_special.sql` 的 `CREATE TABLE special_appointment` 增加 `teacher_id bigint(20) DEFAULT NULL COMMENT '老师档案ID'`（新库）
- Modify: `SpecialAppointment` / Bo / Vo — `Long teacherId`，JSON 出参前端当字符串
- Modify: `apps/mobile/src/pages/resource/appointment.vue` 接收 `teacherId` query
- Modify: `apps/mobile/src/pages/teacher/detail.vue` — 预约入口：`/pages/resource/appointment?title=&teacherId=`，若老师有 `resourceId` 则同时带上
- 校验：`resourceId != null || teacherId != null`，两者都空拒绝。DB 将 `resource_id` 改为可空（与 migrate 一起），老师详情预约允许只有 `teacher_id`
- Modify: `SpecialMobileMeServiceImpl.listMyAppointments` / `getMyAppointment`：
  - `currentRole=special_parent` → `userId=自己`
  - `currentRole=special_teacher` → `teacherId=自己档案 id`（`special_teacher.user_id=自己`）
- Modify: 创建预约：未 `phoneBound` 拒绝「请先绑定手机号」

**migrate 加列：**

```sql
CALL special_add_column('special_appointment', 'teacher_id',
  'ALTER TABLE special_appointment ADD COLUMN teacher_id bigint(20) DEFAULT NULL COMMENT ''老师档案ID'' AFTER user_id');
```

并把 `resource_id` 改为可空（若当前 NOT NULL）：另写 procedure `special_modify_column` 或单独 ALTER，执行前查 `information_schema`。

- [ ] **Step 1:** `SpecialAppointmentBoValidationTest` 增加：仅 teacherId 合法；两者都空失败。先写失败测试。

- [ ] **Step 2:** `mvn ... -Dtest=SpecialAppointmentBoValidationTest` FAIL（新断言）

- [ ] **Step 3:** 实体 + 校验 + me 分流 + 老师详情入口

- [ ] **Step 4:** 该测试 + mobile test:run PASS

- [ ] **Step 5: Commit** `feat(special): attach appointments to teacher profiles`

---

### Task 9: 后台用户角色 + 老师档案绑账号

**Files:**
- Create: `SpecialAccountController` `GET /special/account/list`、`PUT /special/account/{userId}/roles`、`PUT /special/account/{userId}/password`
- Create: Vo：`userId` 字符串、`phone` 脱敏、`nickname`、`roles: string[]`、`status`
- Body：`{ "parent": true, "teacher": false }` — 调 `assertKeepAtLeastOneRole`；改 `sys_user_role`；勾选老师但无档案 → 返回业务码/文案「请先补全老师档案」`needTeacherProfile=true`
- Password body：`{ "password": "..." }` BCrypt 更新
- Modify: 老师新增/更新：Bo 增加 `phone`、`initPassword`；Service 按 spec 合并用户并写 `user_id`；已是老师抛「该老师账号已存在」
- Modify: `apps/admin/src/utils/teacher-payload.ts` + `teacher-payload.test.ts`：有 `phone` 则带上；新建且无 `id` 时 `initPassword` 必填（缺则 throw「请填写初始密码」）；已有用户编辑不带密码
- Create: `apps/admin/src/views/account/Index.vue` + router `/account` + `FgSidebar` 项「用户角色」（仅超管显示，Task 10 接 getInfo）
- Modify: `apps/admin/src/views/teacher/Index.vue` 表单：手机号、初始密码（新增时）
- Modify: `migrate-identity-roles.sql` + `ry_special.sql`：菜单 `special:account:list` / `special:account:edit`

权限：`@SaCheckPermission("special:account:list")` 等。超管已有 `*:*:*`。

给 `special_teacher` 角色挂 `special:teacher:query`、`special:teacher:edit`、`special:appointment:list`、`special:appointment:query`、`special:appointment:edit`。不挂 account、resource、org、parent、audit。

- [ ] **Step 1:** 扩展 `teacher-payload.test.ts`：

```ts
it('requires initPassword on create', () => {
  expect(() => buildTeacherPayload({ name: '周老师', status: 0, phone: '13800138000' }))
    .toThrow('请填写初始密码')
})
it('keeps phone as string', () => {
  const payload = buildTeacherPayload({
    name: '周老师', status: 0, phone: '13800138000', initPassword: 'Abcd1234',
  })
  expect(payload.phone).toBe('13800138000')
  expect(payload.initPassword).toBe('Abcd1234')
})
```

- [ ] **Step 2:** `pnpm --dir apps/admin exec vitest run src/utils/teacher-payload.test.ts` FAIL

- [ ] **Step 3:** payload + 后端合并 + 页面

- [ ] **Step 4:** vitest PASS；`mvn ... -Dtest=SpecialIdentitySupportTest,SpecialTeacherServiceImplTest`

老师 Service 增加 mock 测试：已有家长手机号开通老师时只加角色不新建用户（mock userMapper）。

- [ ] **Step 5: Commit** `feat(admin): assign parent/teacher roles and bind teacher accounts`

---

### Task 10: 老师登 PC 只看自己

**Files:**
- Create: `apps/admin/src/utils/admin-access.ts` + `admin-access.test.ts`
- Modify: `apps/admin/src/store/auth.ts` — 登录后 `GET /system/user/getInfo`，存 `roles`、`currentRole`
- Modify: `FgSidebar.vue` — `isSuperAdmin` 显示全菜单；老师只显示「我的资料」`/teacher/me`、「我的预约」`/appointment`
- Modify: `router/index.ts` — 老师访问 `/resource` `/organization` `/parent` `/audit` `/account` `/dashboard` 重定向 `/appointment`
- Modify: `SpecialTeacherController` / `SpecialAppointmentController`：若登录者不是超管且有 `special_teacher`，列表/改强制 `user_id`/`teacher_id` = 自己档案；改审核状态拒绝
- Create: `apps/admin/src/views/teacher/Me.vue` 或老师 Index 在 `?self=1` 只加载自己（优先独立页，避免和全量列表打架）

```ts
export function isSuperAdmin(roles: string[]) {
  return roles.includes('superadmin')
}
export function isTeacherOnly(roles: string[]) {
  return roles.includes('special_teacher') && !isSuperAdmin(roles)
}
export function canOpenAdminPath(path: string, roles: string[]) {
  if (isSuperAdmin(roles)) return true
  if (!isTeacherOnly(roles)) return false
  return path.startsWith('/teacher') || path.startsWith('/appointment')
}
```

```ts
it('blocks teacher from account and resources', () => {
  expect(canOpenAdminPath('/account', ['special_teacher'])).toBe(false)
  expect(canOpenAdminPath('/appointment', ['special_teacher'])).toBe(true)
  expect(canOpenAdminPath('/dashboard', ['superadmin'])).toBe(true)
})
```

老师进后台默认 `router.replace('/appointment')`。Login.vue placeholder 改为「用户名或手机号」。

后端 403：`if (isTeacherOnly && !owning)` throw `ServiceException("没有权限访问")`。

- [ ] **Step 1:** `admin-access.test.ts` FAIL

- [ ] **Step 2:** 实现 util PASS

- [ ] **Step 3:** store / sidebar / router / 后端数据范围

- [ ] **Step 4:** vitest + 相关 mvn 测试 PASS

- [ ] **Step 5: Commit** `feat(admin): scope teacher workbench to self`

---

### Task 11: 微信手机号快速验证 + 生产关闭测试码

**Files:**
- Modify: `SpecialMobileMeServiceImpl.bindPhone`：若 `wxPhoneCode` 非空，调用微信 `phonenumber.getPhoneNumber`（现有 `WechatMiniProgramProperties` app-id/secret）换真号，再走 Task 5 planner
- Modify: `bind-phone.vue` / `register.vue`：`#ifdef MP-WEIXIN` 使用 `button open-type="getPhoneNumber"`，成功把 `code` 当 `wxPhoneCode`；失败再走短信
- Modify: `application-dev.yml` `special.sms.test-code-enabled: true`；prod 配置 **不入 Git** 为 false
- H5 注册：无微信按钮；短信失败展示接口返回的「短信通道未开通」

不要接 `grantType=sms` 登录。

- [ ] **Step 1:** `SpecialBindPhonePlanner` 已覆盖合并。新增 `SpecialWxPhoneParser` 若只是 JSON 取 `phone_info.purePhoneNumber`，单测解析微信返回样例。

- [ ] **Step 2–4:** 接微信 API；开发环境无真实微信时允许只测短信路径

- [ ] **Step 5: Commit** `feat(mobile): bind wechat phone number before sms fallback`

---

## Spec coverage

| Spec 条目 | 任务 |
|-----------|------|
| 一人一号、叠角色、手机号合并键 | 1, 3, 5, 9 |
| currentRole 会话 + 缓存 + 切换 | 2, 4, 6, 7 |
| 小程序手机号密码 + 微信一键 + 注册 | 2, 3, 6 |
| 微信先登再绑手机、合并临时号 | 5, 7, 11 |
| 验证码通道（短信测试码 / 微信号 / 生产关测试码） | 3, 11 |
| 仅家长拒 PC | 2 |
| 老师两端只看自己；首页老师摘要 | 7, 8, 10 |
| 用户角色页、老师档案绑账号、存量不自动建号 | 9 |
| 预约 `teacher_id` | 8 |
| 不改登录协议 / Clientid | 全局 |
| 不做机构管理员、菜单树、sms 主登录、解绑、自助找回密码 | 无对应任务 |

## 手工验收（全部任务完成后）

- 小程序：手机号注册家长 → 密码登录 → 微信新用户一键 → 绑手机
- 后台建老师（手机号+初始密码）→ 小程序密码登录仅老师无切换 → 再叠家长后可切换
- 切换后杀进程重开仍是上次角色
- 老师登 PC：只有资料和预约；进不了资源/机构/用户角色
- 超管可给已有家长开通老师
- 未过审老师不出现在公开列表，但能登录改资料
- 开发能用测试码；prod 关闭测试码；未开短信时 H5 提示「短信通道未开通」
