# Phase 2A.2 资讯/政策 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 管理端可 CRUD 政策/资讯；移动端首页展示「政策资讯」区块，支持列表与详情浏览（已发布、浏览量 +1）。

**Architecture:** 新建 `special_article` 表与 RuoYi 标准 Domain/Mapper/Service/Controller 栈；Admin 独立侧栏「资讯管理」；Mobile 走 `@SaIgnore` 的 `/special/mobile/article/*`，与资源模块同模式。

**Tech Stack:** RuoYi-Vue-Plus、Vue3 + Element Plus、uni-app / Wot Design Uni

**Spec:** `docs/superpowers/specs/2026-08-21-phase2-ab-modules-design.md` §2A.2

## Global Constraints

- 主色 `#1B7F6B`；canvas `#F4F7F6`；muted `#4F635F`
- 移动端雪花 ID 全程 `String(id)`；不改登录/Clientid 协议
- 状态 Tag 必须有文字（草稿 / 已发布 / 已下架）
- 封面复用 2A.1 `FgCoverUpload` + OSS URL
- 正文一期 HTML 字符串存储；Mobile 用 `rich-text` 渲染

---

## File Map

| 文件 | 职责 |
|------|------|
| `server/script/sql/ry_special.sql` | 建表、菜单权限、示例数据 |
| `server/.../SpecialArticle.java` + Bo/Vo/Mapper/Service/Controller | 后端 CRUD + 发布查询 |
| `server/.../SpecialMobileController.java` | 追加 article list/detail |
| `apps/admin/src/api/special.ts` | Article 类型与 CRUD API |
| `apps/admin/src/views/article/Index.vue` | 资讯管理页 |
| `apps/admin/src/router/index.ts` + `App.vue` | 路由与侧栏 |
| `apps/mobile/src/api/types/special.ts` + `special.ts` | 类型与 API |
| `apps/mobile/src/pages/article/list.vue` | 资讯列表 |
| `apps/mobile/src/pages/article/detail.vue` | 资讯详情 |
| `apps/mobile/src/pages/index/index.vue` | 首页政策资讯区块 |

---

### Task 1: 数据库与后端实体

**Files:**
- Modify: `server/script/sql/ry_special.sql`
- Create: `SpecialArticle.java`, `SpecialArticleBo.java`, `SpecialArticleVo.java`
- Create: `SpecialArticleMapper.java`, `ISpecialArticleService.java`, `SpecialArticleServiceImpl.java`
- Create: `SpecialArticleController.java`
- Modify: `SpecialMobileController.java`

**Interfaces:**
- Admin: `GET/POST/PUT/DELETE /special/article/**`，权限 `special:article:*`
- Mobile: `GET /special/mobile/article/list`（status=1）、`GET /special/mobile/article/{id}`（view_count+1）

- [ ] **Step 1:** SQL 建表 `special_article`、菜单 1764000000000000006、按钮 51–54、示例 2–3 条
- [ ] **Step 2:** Java 实体与服务（发布列表按 `publish_time DESC`；发布时若 `publishTime` 为空则写当前时间）
- [ ] **Step 3:** Mobile 端点注入 ArticleService
- [ ] **Step 4:** `mvn -pl ruoyi-modules/ruoyi-special -am compile -q` 通过

---

### Task 2: Admin 资讯管理

**Files:**
- Modify: `apps/admin/src/api/special.ts`
- Create: `apps/admin/src/views/article/Index.vue`
- Modify: `apps/admin/src/router/index.ts`, `apps/admin/src/App.vue`

- [ ] **Step 1:** API 封装 list/get/add/update/delete
- [ ] **Step 2:** 列表（标题、分类、状态 Tag、发布时间、封面缩略图）
- [ ] **Step 3:** 表单（标题、分类 policy/news/guide、摘要、封面、正文 textarea、状态）
- [ ] **Step 4:** 侧栏「资讯管理」+ 路由 `/article`
- [ ] **Step 5:** `pnpm --dir apps/admin build` 通过

---

### Task 3: Mobile 资讯浏览

**Files:**
- Modify: `apps/mobile/src/api/types/special.ts`, `special.ts`
- Create: `pages/article/list.vue`, `pages/article/detail.vue`
- Modify: `pages/index/index.vue`

- [ ] **Step 1:** API `getArticleList` / `getArticleDetail`
- [ ] **Step 2:** 列表页（分类筛选胶囊、卡片摘要）
- [ ] **Step 3:** 详情页（封面、rich-text 正文、浏览量）
- [ ] **Step 4:** 首页「政策资讯」3–5 条 + 查看更多
- [ ] **Step 5:** `pnpm --dir apps/mobile test:run` 通过

---

## 验收清单

- [ ] Admin `/article`：增删改查、状态 Tag 有文字、封面可上传
- [ ] Mobile 首页可见政策资讯；列表/详情 ID 为字符串跳转正常
- [ ] 仅已发布（status=1）在 Mobile 展示；详情浏览量递增
- [ ] ECS：执行 SQL 增量后重启后端、重编 admin dist

---

## ECS 部署备注

```bash
# 在 ECS MySQL 执行 ry_special.sql 中 phase2a-article 段
sudo systemctl restart special-server
# admin dist 同步 + nginx reload（同 2A.1）
```
