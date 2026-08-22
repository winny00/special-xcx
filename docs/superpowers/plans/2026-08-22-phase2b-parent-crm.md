# Phase 2B.2 家长 CRM Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 管理端只读查看 `special_parent` 家长列表与预约历史，列表手机号脱敏，详情可看完整手机号以便回拨。

**Architecture:** 不新建业务表。列表用 `sys_user` ∩ `sys_role.role_key = special_parent` 联表分页，预约次数子查询 `special_appointment`。详情复用 `ISysUserService.selectUserById` + `ISpecialAppointmentService.queryPageList(userId)`。Admin 独立侧栏页 `/parent`，抽屉而非独立路由。

**Tech Stack:** RuoYi-Vue-Plus、MyBatis-Plus、Vue3 + Element Plus

**Spec:** `docs/superpowers/specs/2026-08-21-phase2-ab-modules-design.md` §2B.2（会话批准的收紧范围：只读、不改 Mobile、不建 `special_parent` 表）

## Global Constraints

- 主色 `#1B7F6B`；canvas `#F4F7F6`；muted `#4F635F`
- 雪花 `userId` 前端全程 `String(id)`；禁止 `Number(id)`
- 列表手机号脱敏 `138****1234`（Hutool `DesensitizedUtil.mobilePhone`）；详情返回完整号码
- 预约状态 Tag 必须有文字：0 待处理 / 1 已联系 / 2 已完成 / 3 已取消
- 不改登录 / Clientid / 微信 xcx 协议；不改 Mobile
- 非家长 `userId` 返回业务错误，不是 500
- MySQL 5.7：迁移脚本禁止 `ADD COLUMN IF NOT EXISTS`

---

## File Map

| 文件 | 职责 |
|------|------|
| `server/.../util/SpecialParentSupport.java` | 家长角色判断、列表手机号脱敏 |
| `server/.../domain/vo/SpecialParentVo.java` | 列表行 |
| `server/.../domain/vo/SpecialParentDetailVo.java` | 详情（完整手机 + 最近预约） |
| `server/.../domain/bo/SpecialParentBo.java` | 昵称/手机 keyword |
| `server/.../mapper/SpecialParentMapper.java` + XML | 家长分页联表 |
| `server/.../service/ISpecialParentService.java` + Impl | 列表脱敏、详情校验角色 |
| `server/.../controller/SpecialParentController.java` | `GET /special/parent/list`、`GET /special/parent/{userId}` |
| `server/script/sql/ry_special.sql` | 菜单 `special:parent:list/query` |
| `scripts/ecs/migrate-phase2b-parent.sql` | ECS 可重复执行菜单增量 |
| `apps/admin/src/api/special.ts` | 类型与 API，`userId` 为 string |
| `apps/admin/src/views/parent/Index.vue` | 列表 + 详情抽屉 |
| `apps/admin/src/views/appointment/Index.vue` | 读取 `?userId=` 过滤 |
| `apps/admin/src/router/index.ts`、`FgSidebar.vue` | `/parent` 路由与侧栏 |

---

### Task 1: 脱敏与角色判断（TDD）

**Files:**
- Create: `server/ruoyi-modules/ruoyi-special/src/main/java/org/dromara/special/util/SpecialParentSupport.java`
- Create: `server/ruoyi-modules/ruoyi-special/src/test/java/org/dromara/special/util/SpecialParentSupportTest.java`
- Modify: `server/ruoyi-modules/ruoyi-special/pom.xml`（junit-jupiter test scope）

**Interfaces:**
- Consumes: Hutool `DesensitizedUtil.mobilePhone`、`SysRoleVo.roleKey`
- Produces: `SpecialParentSupport.PARENT_ROLE_KEY = "special_parent"`；`maskPhone(String)`；`isParent(List<SysRoleVo>)`

- [ ] **Step 1: Write the failing test**

```java
@Test
void masksElevenDigitMobile() {
    assertEquals("138****1234", SpecialParentSupport.maskPhone("13800001234"));
}

@Test
void blankPhoneStaysBlank() {
    assertEquals("", SpecialParentSupport.maskPhone(""));
    assertNull(SpecialParentSupport.maskPhone(null));
}

@Test
void detectsParentRole() {
    SysRoleVo parent = new SysRoleVo();
    parent.setRoleKey("special_parent");
    SysRoleVo teacher = new SysRoleVo();
    teacher.setRoleKey("special_teacher");
    assertTrue(SpecialParentSupport.isParent(List.of(parent)));
    assertFalse(SpecialParentSupport.isParent(List.of(teacher)));
    assertFalse(SpecialParentSupport.isParent(List.of()));
    assertFalse(SpecialParentSupport.isParent(null));
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `mvn -f server/pom.xml -pl ruoyi-modules/ruoyi-special -am test -Dtest=SpecialParentSupportTest -q`

Expected: FAIL（类不存在）

- [ ] **Step 3: Minimal implementation**

`maskPhone` 空值原样返回；非空走 `DesensitizedUtil.mobilePhone`。`isParent` 任一 `roleKey` 等于 `special_parent`。

- [ ] **Step 4: Run tests — expected PASS**

---

### Task 2: 列表/详情 Service（TDD）

**Files:**
- Create: Vo / Bo / Mapper XML / Service
- Create: `SpecialParentServiceImplTest.java`（mockito）

**Interfaces:**
- Consumes: `SpecialParentMapper.selectParentPage(Page, keyword)`；`ISysUserService.selectUserById`；`ISpecialAppointmentService.queryPageList`
- Produces:
  - `PageResult<SpecialParentVo> queryPageList(SpecialParentBo bo, PageQuery pageQuery)` — `phone` 已脱敏，`appointmentCount` 为 long
  - `SpecialParentDetailVo queryById(Long userId)` — 完整 `phone`；`appointments` 最多 20 条，按创建时间倒序；用户不存在 → `ServiceException("用户不存在")`；无家长角色 → `ServiceException("该用户不是家长")`

- [ ] **Step 1: Failing service tests**（mock mapper / userService / appointmentService）

```java
@Test
void listMasksPhone() { /* mapper 返回 13800001234，断言 Vo.phone 为 138****1234 */ }

@Test
void detailRejectsNonParent() { /* user.roles = special_teacher → ServiceException 该用户不是家长 */ }

@Test
void detailRejectsMissingUser() { /* selectUserById null → 用户不存在 */ }

@Test
void detailKeepsFullPhoneAndRecentAppointments() { /* phone 原文；pageSize=20 */ }
```

- [ ] **Step 2: Run — expected FAIL**
- [ ] **Step 3: Implement mapper SQL + service**
- [ ] **Step 4: `mvn -pl ruoyi-modules/ruoyi-special -am test -Dtest=SpecialParentSupportTest,SpecialParentServiceImplTest` PASS**
- [ ] **Step 5: Controller** `GET /special/parent/list` 权限 `special:parent:list`；`GET /special/parent/{userId}` 权限 `special:parent:query`

**Mapper SQL 要点：** `sys_user` INNER JOIN `sys_user_role` INNER JOIN `sys_role` WHERE `r.role_key = 'special_parent'` AND `u.del_flag = '0'`；keyword 同时 LIKE `nick_name`、`phone_number`；`appointment_count` 子查询 `special_appointment.del_flag = '0'`；`ORDER BY u.create_time DESC`。

---

### Task 3: SQL 菜单

**Files:**
- Modify: `server/script/sql/ry_special.sql`
- Create: `scripts/ecs/migrate-phase2b-parent.sql`

菜单 ID：

- `1764000000000000007` 家长管理 C `special:parent:list` parent=`1764000000000000001` order=4
- `1764000000000000061` 家长查询 F `special:parent:query`
- `1764000000000000062` 家长列表 F `special:parent:list`（按钮，与 C 菜单权限一致，便于角色勾选）

`ON DUPLICATE KEY UPDATE`；ECS 脚本同样可重复执行。不插业务数据。

---

### Task 4: Admin 列表 + 抽屉 + 预约过滤

**Files:**
- Modify: `apps/admin/src/api/special.ts` — `userId: string`
- Create: `apps/admin/src/views/parent/Index.vue`
- Modify: `apps/admin/src/router/index.ts`、`FgSidebar.vue`、`dashboard/Index.vue`、`appointment/Index.vue`

**列表：** 昵称、脱敏手机、注册时间、预约次数、操作「查看」。搜索框 placeholder「昵称 / 手机号」。

**抽屉：** 头像、昵称、完整手机、注册时间；只读预约表（资源、联系人、状态 Tag 文字、时间）；链接「查看全部预约」→ `/appointment?userId=`。

**预约页：** `onMounted` + `watch(route.query.userId)` 把 `userId` 传给 `listAppointments`；重置时清掉 query。`keep-alive` 只按 path 缓存，必须 watch。

- [ ] **Step 1:** API + 页面 + 路由侧栏
- [ ] **Step 2:** `pnpm --dir apps/admin test:run` 与 `pnpm --dir apps/admin build`

---

## Out of scope

- `special_parent` 表、编辑/删除/拉黑/发消息
- 老师/机构管理员混入列表
- Mobile 改动、2B.1 / 2B.3 / 2B.4
- 提交 git（除非用户明确要求）
