# 特教资源平台：视觉 token 收口 + 根目录 AGENTS.md

日期：2026-08-18  
状态：待实现（须先经用户确认本 spec，再写实施计划）

## 背景

三端已按「温暖专业 / 好大夫式布局」上线：H5 与小程序共用 `apps/mobile`，PC 在 `apps/admin`。主色为青绿 `#1B7F6B`。仓库内没有 Cursor 会自动加载的根目录 `AGENTS.md`。

本设计用 UI/UX Pro Max 的 **Accessible & Ethical** 原则加强对比、热区和 focus，**不更换品牌色**；并用一份根目录 `AGENTS.md` 约束之后每次对话。

## 目标

1. 三端共用同一套语义色与无障碍下限，页面只做针对性修补，不重做信息架构。
2. 根目录新增 `AGENTS.md`；`.cursor/rules` 始终生效并要求先读该文件。
3. 不改接口、权限、登录协议、微信登录逻辑。

## 决策（已确认）

- 主色继续 `#1B7F6B`，不换成 Pro Max 默认医疗青 `#0891B2`。
- 只维护仓库根目录一份 `AGENTS.md`（不在 `apps/mobile`、`apps/admin` 再写独立 Agent 文档）。
- 落地方式：token + 规范优先；现有搜索/宫格/卡片与 PC 侧栏工作台布局保持不变。

## 视觉 token

| 语义 | 值 | 说明 |
|------|-----|------|
| canvas | `#F4F7F6` | 页面底 |
| surface | `#FFFFFF` | 卡片 |
| primary | `#1B7F6B` | 主色、选中、主按钮 |
| primary-soft | `#E7F4F0` | 浅底标签 |
| ink | `#1C2B28` | 正文 |
| muted | `#4F635F` | 次要文字（由 `#667874` 加深，浅底对比 ≥ 4.5:1） |

- 字体：系统黑体（PingFang SC / 苹方 / 微软雅黑 / -apple-system），不引入网文字体。
- 正文 ≥ 16px；卡片圆角 12px；胶囊 999px。
- 可点区域 ≥ 44px；`:focus-visible` 使用约 3px 主色描边。
- 图标：Uno/碳图标或系统图标，禁止 emoji 当图标。
- 动效 150–300ms；尊重 `prefers-reduced-motion`。
- 移动端：`--color-*`、`--wot-color-theme`、`--wot-button-primary-bg-color`。
- PC：`--el-color-primary` 及 light/dark 派生，与 primary 对齐。

## 页面改动范围

### 改

- 移动端：`apps/mobile` 全局变量与 Uno `primary` 回退色；过浅灰字改 muted；Tab/按钮热区；自定义顶栏继续用胶囊避让；空状态与卡片次要信息对比度。
- PC：`apps/admin` 主题变量与工作台间距；状态 Tag 必须带文字（草稿 / 已发布），不能只靠颜色。
- 可点击控件的 hover/active；图标按钮补可见文字或无障碍名称。

### 不改

- 首页四宫格、资源卡片结构、详情底栏预约、PC 侧栏导航信息架构。
- 真实图片资源、新字体包、Bento / 横向长滚动落地页。
- 后端 API、登录/权限、微信登录。

## AGENTS.md

路径：仓库根目录 `AGENTS.md`。

建议目录：

1. **先读本文件** — 每次对话、改代码前必读。
2. **产品与三端** — mobile = H5+小程序同一源码；admin = PC（5174）；server = RuoYi，不随意改协议。
3. **开发流程** — 新功能：brainstorming → 批准 → writing-plans → 实现；修 bug：systematic-debugging；未批准不写业务代码。
4. **UI 规范** — 上表 token；UI 任务先跑 ui-ux-pro-max，但主色锁定 `#1B7F6B`；默认栈 Vue / uni-app，不是 html-tailwind。
5. **移动端** — `apps/mobile`；H5 `:9000`；雪花 ID 全程字符串；自定义顶栏避让胶囊；避免在会破坏预编译的 TS 里写 `#ifdef` 包裹多余括号。
6. **PC** — `apps/admin`；侧栏工作台；Clientid 头。
7. **验收** — H5 首页可搜可进宫格；我的页无 JSON；PC 登录分栏 + 侧栏；小程序顶栏不挡胶囊。

强制读取：更新 `.cursor/rules/ui-ux-pro-and-superpowers.mdc`（`alwaysApply: true`），正文第一条为「先完整阅读根目录 `AGENTS.md`」。

## 测试

- H5 `http://localhost:9000`：首页、资源详情、我的；对比度与点击热区肉眼可点。
- PC `http://localhost:5174`：登录分栏、三 CRUD 页 Tag 有文字。
- 小程序：自定义顶栏问候语/搜索不与胶囊重叠；点资源卡片详情能打开（ID 为字符串）。

## 明确不做

- 提交 git（除非用户另行要求）。
- 本 spec 通过并完成实施计划之前，不改业务页面代码。
