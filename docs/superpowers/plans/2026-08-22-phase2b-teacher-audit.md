# Phase 2B.1 + 2B.3 老师档案与审核增强 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 管理端可维护独立老师档案并在审核中心批量通过/拒绝（拒绝必填备注）；小程序首页「老师」走已通过档案列表/详情。

**Architecture:** 新表 `special_teacher`，不自动双写 `special_resource`。机构/资源/老师共用 `SpecialAuditBo` + `SpecialAuditSupport`（拒绝必须备注）。审核中心三 Tab 调三个 `PUT .../audit`。Mobile 只读已通过老师。

**Tech Stack:** RuoYi-Vue-Plus、MyBatis-Plus、Vue3 + Element Plus、uni-app

**Spec:** `docs/superpowers/specs/2026-08-21-phase2-ab-modules-design.md` §2B.1 + §2B.3（会话批准：不迁旧老师资源、不做账号绑定、不上 Warm-Flow）

## Global Constraints

- 主色 `#1B7F6B`；canvas `#F4F7F6`；muted `#4F635F`
- 雪花 ID 前端全程 `String(id)`；禁止 `Number(id)`
- 状态 Tag 必须有文字：老师 待审/已通过/已拒绝；机构同；资源 草稿/已发布/已下架
- 拒绝（状态=2）必须填写 `remark`；通过备注可选
- 不改登录 / Clientid / 微信 xcx 协议
- MySQL 5.7：迁移禁止 `ADD COLUMN IF NOT EXISTS`
- 不自动把 `resource_type=teacher` 迁到新表

---

## File Map

| 文件 | 职责 |
|------|------|
| `SpecialAuditSupport.java` | 拒绝必填备注 |
| `SpecialAuditBo.java` | `ids` + `status` + `remark` |
| `SpecialTeacher*` Entity/Bo/Vo/Mapper/Service/Controller | 老师 CRUD + 审核 + Mobile 已通过列表 |
| `SpecialOrganization/Resource` 实体与 Service | 审核字段 + `audit()` |
| `SpecialDashboard*` | `teacherAuditPending` |
| `ry_special.sql` + `scripts/ecs/migrate-phase2b-teacher-audit.sql` | 建表、审核列、菜单 |
| `apps/admin/views/teacher/Index.vue` | 老师档案 CRUD |
| `apps/admin/views/audit/Index.vue` | 三 Tab + 批量审核 |
| `apps/mobile/pages/teacher/*` | 列表/详情；首页「老师」入口 |

---

### Task 1: 审核规则（TDD）

**Produces:** `SpecialAuditSupport.requireRemarkWhenReject(Integer status, String remark)`；拒绝=2 且备注空白抛 `ServiceException("拒绝时必须填写审核备注")`

### Task 2: 老师档案后端 + 审核接口

**Produces:**
- `GET/POST/PUT/DELETE /special/teacher/**` 权限 `special:teacher:*`
- `PUT /special/teacher/audit`、`/special/organization/audit`、`/special/resource/audit`
- `GET /special/mobile/teacher/list`、`GET /special/mobile/teacher/{id}`（仅 status=1）
- 老师默认 status=0；未通过详情抛「老师不存在或未通过审核」

### Task 3: SQL + ECS 迁移

**Produces:** `special_teacher`；三表 `audit_remark/audit_by/audit_time`；菜单 1764000000000000008；workflow 执行新 migrate

### Task 4: Admin 老师页 + 审核中心 + 概览

**Produces:** `/teacher` 侧栏；审核中心三 Tab 多选通过/拒绝；概览老师待审数

### Task 5: Mobile 老师列表/详情

**Produces:** `/pages/teacher/list`、`detail?id=`；首页「老师」改走档案列表
