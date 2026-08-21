# Phase 2A.3 家长中心 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task.

**Goal:** 登录家长可在「我的」查看资料卡片与「我的预约」列表/详情；预约与 Admin 状态一致，雪花 ID 字符串跳转。

**Architecture:** 新建 `SpecialMobileMeController`（需登录，非 `@SaIgnore`）；资料读写复用 `ISysUserService`；预约列表按 `user_id = LoginHelper.getUserId()` 过滤。

**Spec:** `docs/superpowers/specs/2026-08-21-phase2-ab-modules-design.md` §2A.3

## Global Constraints

- 主色 `#1B7F6B`；状态 Tag 必须有文字
- 预约状态与 SQL 一致：0 待处理 / 1 已联系 / 2 已完成 / 3 已取消
- 手机号返回脱敏；雪花 ID 全程字符串

---

## File Map

| 文件 | 职责 |
|------|------|
| `server/.../SpecialMobileMeController.java` | profile + appointments API |
| `server/.../ISpecialMobileMeService.java` + Impl | 资料组装、预约归属校验 |
| `server/.../ISpecialAppointmentService.java` | 新增 queryMy* 方法 |
| `server/ruoyi-special/pom.xml` | 依赖 `ruoyi-system`（用户资料更新） |
| `apps/mobile/src/api/me.ts` | 家长中心 API |
| `apps/mobile/src/pages/me/me.vue` | 增强资料卡 + 预约入口 |
| `apps/mobile/src/pages/me/appointments.vue` | 预约列表 |
| `apps/mobile/src/pages/me/appointment-detail.vue` | 预约详情 |
| `apps/mobile/src/components/fg-profile-card/` | 支持头像 URL |

---

### Task 1: 后端 Me API

- [ ] `GET/PUT /special/mobile/me/profile`
- [ ] `GET /special/mobile/me/appointments`、`GET .../{id}`（校验 userId）

### Task 2: Mobile 我的页 + 预约页

- [ ] API 封装、未登录引导登录
- [ ] 列表/详情页 + 状态 Tag 文字

### Task 3: 验证

- [ ] `pnpm --dir apps/mobile test:run`
- [ ] `pnpm --dir apps/admin build`
