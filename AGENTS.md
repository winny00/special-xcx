# AGENTS.md

## 先读本文件

每次对话、改代码、查 bug 之前，先完整阅读本文件。不要凭记忆跳过三端边界、ID 类型和 UI token。

本仓库 Cursor 规则（`.cursor/rules/ui-ux-pro-and-superpowers.mdc`，alwaysApply）也会要求先读本文件。

## 产品与三端

特教资源平台：家长侧找课程/老师/机构并预约，管理侧做资源、机构、预约 CRUD。

| 端 | 路径 | 说明 |
|----|------|------|
| 移动端 H5 + 微信小程序 | `apps/mobile` | **同一份源码**（unibest / Vue3 / Wot Design Uni）。不要为小程序再开一套页面。 |
| PC 管理后台 | `apps/admin` | Vue3 + Element Plus，开发端口 **5174**。 |
| 后端 | `server` | RuoYi-Vue-Plus，默认 **8080**。不要为了前端方便改登录/权限协议。 |

不要在 `apps/mobile` 或 `apps/admin` 再写独立的 Agent.md。

## 开发流程

新功能或改交互：先 `brainstorming` → 用户批准设计 → `writing-plans` → 再实现。未批准不写业务代码。

修 bug：先 `systematic-debugging`，不要先猜补丁。

UI 任务：先读 `~/.cursor/skills/ui-ux-pro-max/SKILL.md`，再跑：

```bash
python3 "$HOME/.cursor/skills/ui-ux-pro-max/scripts/search.py" "<product industry keywords>" --design-system
```

默认栈是 Vue / uni-app / 小程序，**不是** html-tailwind。Pro Max 色板只用来核对对比度与反模式；**主色锁定 `#1B7F6B`，禁止换成 `#0891B2`。**

## UI 规范

现有布局是「温暖专业 / 好大夫式」：搜索、四宫格、胶囊分类、资源卡片、底栏预约、PC 侧栏工作台。只修对比度、热区、focus，不重做信息架构。

| 语义 | 值 |
|------|-----|
| canvas | `#F4F7F6` |
| surface | `#FFFFFF` |
| primary | `#1B7F6B` |
| primary-soft | `#E7F4F0` |
| ink | `#1C2B28` |
| muted | `#4F635F` |

- 系统黑体（PingFang SC / 苹方 / 微软雅黑 / -apple-system），不引入网文字体。
- 移动端正文 ≥ 16px；卡片圆角 12px；胶囊 999px。
- 可点区域 ≥ 44px；`:focus-visible` 约 3px 主色描边。
- 图标用 Uno 碳图标或系统图标，禁止 emoji 当图标。
- 动效 150–300ms；尊重 `prefers-reduced-motion`。
- 移动端同步 `--color-*`、`--wot-color-theme`、`--wot-button-primary-bg-color`。
- PC 同步 `--el-color-primary` 及 light/dark 派生。
- 状态 Tag 必须有文字（草稿 / 已发布），不能只靠颜色。

## 移动端

- 目录：`apps/mobile`。H5 开发：`http://localhost:9000`。
- 雪花 ID **全程字符串**。路由 query、列表 `:key`、`navigateTo` 都用 `String(id)`。禁止 `Number(id)`，会丢精度导致详情 404。
- 首页自定义顶栏必须用 `useCapsuleNav()` 避让微信胶囊。该 hook 里用运行时 `getMenuButtonBoundingClientRect`，不要在会破坏预编译的 TS 里用 `#ifdef` 包多余括号。
- Tab 文案走 i18n 键（`%tabbar.*%`），不要把纯中文直接丢进 `t()`。
- 测试：`pnpm --dir apps/mobile test:run`。

## PC

- 目录：`apps/admin`。开发：`http://localhost:5174`。
- 每个请求必须带 Header `Clientid`（见 `apps/admin/src/api/request.ts` 的 `CLIENT_ID`），并与登录时一致，否则后端判定登录异常。
- 布局：登录分栏 + 登录后侧栏工作台（资源 / 机构 / 预约）。不要改成顶栏主导航。

## 验收

- H5 `http://localhost:9000`：首页可搜索、四宫格可进、资源详情能打开、我的页不是 JSON 原文。
- PC `http://localhost:5174`：登录分栏、侧栏三个 CRUD、状态 Tag 有文字。
- 小程序：自定义顶栏问候语/搜索不挡胶囊；点资源卡片详情能打开（ID 为字符串）。
