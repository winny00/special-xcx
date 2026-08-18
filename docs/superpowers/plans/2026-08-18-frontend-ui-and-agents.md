# 视觉 token 收口 + 根目录 AGENTS.md Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 把次要文字对比度、44px 热区和 focus 环收到现有好大夫布局里，并新增根目录 `AGENTS.md` 让之后每次对话先读项目约定。

**Architecture:** 先写 Agent 规范与 Cursor alwaysApply 规则，再改 CSS/Uno token，最后把硬编码 `#667874` 换成 token，并给空状态/Tab/按钮补热区。不改信息架构、接口、登录协议。主色锁定 `#1B7F6B`。

**Tech Stack:** Vue 3、uni-app / unibest（`apps/mobile`）、Wot Design Uni、UnoCSS、Element Plus（`apps/admin`）、Vitest。

**Spec:** `docs/superpowers/specs/2026-08-18-frontend-ui-and-agents-design.md`

## Global Constraints

- 主色继续 `#1B7F6B`，禁止换成 `#0891B2`。
- muted 从 `#667874` 改为 `#4F635F`；canvas `#F4F7F6`；surface `#FFFFFF`；primary-soft `#E7F4F0`；ink `#1C2B28`。
- 只维护仓库根目录一份 `AGENTS.md`，不要在 `apps/mobile` 或 `apps/admin` 再写独立 Agent 文档。
- 不改首页四宫格、资源卡片结构、详情底栏预约、PC 侧栏导航信息架构。
- 不引入网文字体、新图片、Bento / 横向长滚动落地页。
- 不改后端 API、权限、登录协议、微信登录逻辑。
- 雪花 ID 全程字符串，禁止 `Number(id)`。
- `useCapsuleNav.ts` 继续用运行时 `getMenuButtonBoundingClientRect`，不要用 `#ifdef` 包一层多余括号。
- 自动化测试在 `apps/mobile` 用 Vitest：`pnpm --dir apps/mobile test:run`。
- 未得到用户明确要求时，跳过每个任务末尾的 `git commit` 步骤。

---

## File structure

| 文件 | 职责 |
|------|------|
| `AGENTS.md`（新建，仓库根） | 每次对话先读的项目约定：三端边界、Superpowers 流程、UI token、移动端/PC 坑、验收 URL |
| `.cursor/rules/ui-ux-pro-and-superpowers.mdc` | alwaysApply；第一条强制读根目录 `AGENTS.md` |
| `apps/mobile/src/style/index.scss` | 全局 CSS 变量、H5 focus 环、`prefers-reduced-motion` |
| `apps/mobile/src/App.vue` | 与 `index.scss` 同步的 page 级 CSS 变量（小程序也能吃到） |
| `apps/mobile/uno.config.ts` | Uno `muted` 色，回退 `#4F635F` |
| `apps/mobile/src/style/theme-tokens.test.ts`（新建） | 断言 token 值，并扫描源码不得残留 `#667874` |
| `apps/mobile/src/docs/agents-md.test.ts`（新建） | 断言根目录 `AGENTS.md` 含规定章节 |
| `apps/mobile/src/components/fg-empty-state/fg-empty-state.vue` | 碳图标 + muted + 44px 按钮 |
| `apps/mobile/src/components/fg-empty-state/fg-empty-state.test.ts`（新建） | 空状态文案、图标 class、按钮热区 class |
| `apps/mobile/src/tabbar/index.vue`、`config.ts`、`pages.json` | Tab 未选中色改为 muted |
| 下列页面里的 `text-[#667874]` | 全部改为 `text-muted`：index / list / detail / appointment / organization / me / about / login / resource-card |
| `apps/admin/src/App.vue`、`views/Login.vue` | `#667874` → `#4F635F`；H5/PC focus 环 |
| `apps/admin/src/views/resource/Index.vue` 等 | **核对**状态 Tag 仍有「草稿 / 已发布」文字，不删文字 |

不新建字体包、不改 `server/`。

---

### Task 1: 根目录 AGENTS.md + Cursor 强制读取

**Files:**
- Create: `AGENTS.md`
- Create: `apps/mobile/src/docs/agents-md.test.ts`
- Modify: `.cursor/rules/ui-ux-pro-and-superpowers.mdc`

**Interfaces:**
- Consumes: spec 里 AGENTS.md 七节目录
- Produces: 根目录 `AGENTS.md` 存在且含下列标题（后续任务与对话都依赖这些约定）：`先读本文件`、`产品与三端`、`开发流程`、`UI 规范`、`移动端`、`PC`、`验收`

- [ ] **Step 1: Write the failing test**

Create `apps/mobile/src/docs/agents-md.test.ts`:

```ts
import fs from 'node:fs'
import path from 'node:path'
import { describe, expect, it } from 'vitest'

const agentsPath = path.resolve(__dirname, '../../../../AGENTS.md')

const REQUIRED_HEADINGS = [
  '先读本文件',
  '产品与三端',
  '开发流程',
  'UI 规范',
  '移动端',
  'PC',
  '验收',
]

describe('AGENTS.md', () => {
  it('仓库根目录存在 AGENTS.md', () => {
    expect(fs.existsSync(agentsPath)).toBe(true)
  })

  it('包含规定章节标题', () => {
    const text = fs.readFileSync(agentsPath, 'utf8')
    for (const heading of REQUIRED_HEADINGS) {
      expect(text).toContain(heading)
    }
  })

  it('锁定主色且禁止默认医疗青', () => {
    const text = fs.readFileSync(agentsPath, 'utf8')
    expect(text).toContain('#1B7F6B')
    expect(text).toContain('#4F635F')
    expect(text).not.toMatch(/主色.*#0891B2/)
  })
})
```

- [ ] **Step 2: Run test to verify it fails**

Run:

```bash
pnpm --dir apps/mobile test:run src/docs/agents-md.test.ts
```

Expected: FAIL，`仓库根目录存在 AGENTS.md` 为 false（文件尚不存在）。

- [ ] **Step 3: Write AGENTS.md**

Create `AGENTS.md` at repo root with this exact content:

```markdown
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
```

- [ ] **Step 4: Update the Cursor rule so the first instruction is to read AGENTS.md**

Replace `.cursor/rules/ui-ux-pro-and-superpowers.mdc` with:

```markdown
---
description: Enable UI/UX Pro Max and Superpowers skills for this workspace
alwaysApply: true
---

# 先读 AGENTS.md

每次对话、改代码之前，先完整阅读仓库根目录 `AGENTS.md`。其中的三端边界、UI token、雪花 ID 和胶囊避让约定优先于本文件其余条目。

# Installed agent skills

These skills are installed at `~/.cursor/skills/`. Read the matching `SKILL.md` before acting.

## UI/UX Pro Max (`ui-ux-pro` / `ui-ux-pro-max`)

Use when designing, building, reviewing, or improving UI/UX.

1. Read `~/.cursor/skills/ui-ux-pro-max/SKILL.md`
2. Generate a design system first:

```bash
python3 "$HOME/.cursor/skills/ui-ux-pro-max/scripts/search.py" "<product industry keywords>" --design-system
```

For this repo, default stack guidance is Vue / uni-app / 小程序, not html-tailwind. Brand primary is locked to `#1B7F6B`.

## Superpowers

Use when planning features, debugging, writing tests, executing plans, or reviewing code.

1. Read `~/.cursor/skills/using-superpowers/SKILL.md`
2. Invoke the specific skill (`brainstorming`, `systematic-debugging`, `writing-plans`, `test-driven-development`, etc.) before implementing
3. Announce which skill is in use
```

- [ ] **Step 5: Run test to verify it passes**

Run:

```bash
pnpm --dir apps/mobile test:run src/docs/agents-md.test.ts
```

Expected: PASS（3 tests）。

- [ ] **Step 6: Commit**

Skip unless the user asked to commit.

```bash
git add AGENTS.md .cursor/rules/ui-ux-pro-and-superpowers.mdc apps/mobile/src/docs/agents-md.test.ts
git commit -m "$(cat <<'EOF'
docs: add root AGENTS.md and require agents to read it first

EOF
)"
```

---

### Task 2: 移动端 CSS / Uno token（muted、focus、reduced-motion）

**Files:**
- Create: `apps/mobile/src/style/theme-tokens.test.ts`
- Modify: `apps/mobile/src/style/index.scss`
- Modify: `apps/mobile/src/App.vue`（`<style lang="scss">` 里的变量，约 50–61 行）
- Modify: `apps/mobile/uno.config.ts`（`theme.colors`，约 105–109 行）

**Interfaces:**
- Consumes: Task 1 的 UI 规范色值
- Produces: `--color-muted: #4f635f`（大小写均可）；Uno 颜色名 `muted` = `var(--color-muted,#4F635F)`；H5 `:focus-visible` 3px 主色描边；`prefers-reduced-motion` 把 transition/animation 压到近 0

- [ ] **Step 1: Write the failing test**

Create `apps/mobile/src/style/theme-tokens.test.ts`:

```ts
import fs from 'node:fs'
import path from 'node:path'
import { describe, expect, it } from 'vitest'

const mobileSrc = path.resolve(__dirname, '..')
const scssPath = path.resolve(__dirname, 'index.scss')
const appVuePath = path.resolve(mobileSrc, 'App.vue')
const unoPath = path.resolve(mobileSrc, '../uno.config.ts')

function read(p: string) {
  return fs.readFileSync(p, 'utf8')
}

describe('visual tokens', () => {
  it('index.scss 使用加深后的 muted', () => {
    const scss = read(scssPath)
    expect(scss).toMatch(/--color-muted:\s*#4[fF]635[fF]/)
    expect(scss).not.toMatch(/--color-muted:\s*#667874/)
    expect(scss).toMatch(/--color-primary:\s*#1[bB]7[fF]6[bB]/)
    expect(scss).toMatch(/--wot-color-theme:\s*#1[bB]7[fF]6[bB]/)
  })

  it('index.scss 含 focus-visible 与 prefers-reduced-motion', () => {
    const scss = read(scssPath)
    expect(scss).toMatch(/:focus-visible/)
    expect(scss).toMatch(/3px solid/)
    expect(scss).toMatch(/prefers-reduced-motion:\s*reduce/)
  })

  it('App.vue page 变量与 scss 同步 muted', () => {
    expect(read(appVuePath)).toMatch(/--color-muted:\s*#4[fF]635[fF]/)
  })

  it('Uno 提供 muted 色且主色回退仍为 #1B7F6B', () => {
    const uno = read(unoPath)
    expect(uno).toMatch(/muted:\s*'var\(--color-muted,#4F635F\)'/)
    expect(uno).toMatch(/primary:\s*'var\(--wot-color-theme,#1B7F6B\)'/)
  })
})
```

- [ ] **Step 2: Run test to verify it fails**

Run:

```bash
pnpm --dir apps/mobile test:run src/style/theme-tokens.test.ts
```

Expected: FAIL，`--color-muted` 仍是 `#667874`，且没有 `focus-visible`。

- [ ] **Step 3: Update CSS variables, focus ring, reduced motion**

In `apps/mobile/src/style/index.scss`, change the `:root, page` block and append H5 a11y rules. Keep the existing font-family. Resulting file body after the `.test` block:

```scss
:root,
page {
  --color-canvas: #f4f7f6;
  --color-surface: #ffffff;
  --color-primary: #1b7f6b;
  --color-primary-soft: #e7f4f0;
  --color-ink: #1c2b28;
  --color-muted: #4f635f;
  --wot-color-theme: #1b7f6b;
  --wot-button-primary-bg-color: #1b7f6b;
  background-color: var(--color-canvas);
  color: var(--color-ink);
  font-family: -apple-system, BlinkMacSystemFont, 'PingFang SC', 'Hiragino Sans GB', 'Microsoft YaHei', sans-serif;
}

/* #ifdef H5 */
:focus-visible {
  outline: 3px solid var(--color-primary);
  outline-offset: 2px;
}

@media (prefers-reduced-motion: reduce) {
  *,
  *::before,
  *::after {
    animation-duration: 0.01ms !important;
    animation-iteration-count: 1 !important;
    transition-duration: 0.01ms !important;
  }
}
/* #endif */
```

Keep the rest of `index.scss` (the commented box-sizing block) unchanged.

In `apps/mobile/src/App.vue` `<style lang="scss">`, set the same variables:

```scss
:root,
page {
  --color-canvas: #f4f7f6;
  --color-surface: #ffffff;
  --color-primary: #1b7f6b;
  --color-primary-soft: #e7f4f0;
  --color-ink: #1c2b28;
  --color-muted: #4f635f;
  --wot-color-theme: #1b7f6b;
  --wot-button-primary-bg-color: #1b7f6b;
}
```

In `apps/mobile/uno.config.ts` `theme.colors`:

```ts
    colors: {
      /** 主题色，用法如: text-primary */
      primary: 'var(--wot-color-theme,#1B7F6B)',
      /** 次要文字，用法如: text-muted */
      muted: 'var(--color-muted,#4F635F)',
    },
```

- [ ] **Step 4: Run test to verify it passes**

Run:

```bash
pnpm --dir apps/mobile test:run src/style/theme-tokens.test.ts
```

Expected: PASS。

- [ ] **Step 5: Commit**

Skip unless the user asked to commit.

```bash
git add apps/mobile/src/style/index.scss apps/mobile/src/App.vue apps/mobile/uno.config.ts apps/mobile/src/style/theme-tokens.test.ts
git commit -m "$(cat <<'EOF'
fix: darken muted token and add H5 focus and reduced-motion

EOF
)"
```

---

### Task 3: 去掉移动端硬编码 `#667874`

**Files:**
- Modify: `apps/mobile/src/style/theme-tokens.test.ts`（追加扫描用例）
- Modify: `apps/mobile/src/tabbar/index.vue`（`inactiveColor`）
- Modify: `apps/mobile/src/tabbar/config.ts`（`color`）
- Modify: `apps/mobile/src/pages.json`（`tabBar.color`）
- Modify: 下列 Vue 里所有 `text-[#667874]` / `text-[#667874]` 组合 class → `text-muted`：
  - `apps/mobile/src/pages/index/index.vue`
  - `apps/mobile/src/pages/resource/list.vue`
  - `apps/mobile/src/pages/resource/detail.vue`
  - `apps/mobile/src/pages/resource/appointment.vue`
  - `apps/mobile/src/pages/organization/list.vue`
  - `apps/mobile/src/pages/me/me.vue`
  - `apps/mobile/src/pages/about/about.vue`
  - `apps/mobile/src/pages/auth/login.vue`
  - `apps/mobile/src/components/fg-resource-card/fg-resource-card.vue`
  - `apps/mobile/src/components/fg-empty-state/fg-empty-state.vue`（本任务只改颜色；图标与热区在 Task 4）

**Interfaces:**
- Consumes: Uno `text-muted`（Task 2）
- Produces: `apps/mobile/src` 下（不含 `uni_modules`、测试夹具）源码不再出现字面量 `#667874`

- [ ] **Step 1: Extend the failing scan test**

Append to `apps/mobile/src/style/theme-tokens.test.ts`:

```ts
function walkVueTsJson(dir: string, acc: string[] = []): string[] {
  for (const name of fs.readdirSync(dir)) {
    if (name === 'uni_modules' || name === 'node_modules')
      continue
    const full = path.join(dir, name)
    const stat = fs.statSync(full)
    if (stat.isDirectory()) {
      walkVueTsJson(full, acc)
      continue
    }
    if (/\.(vue|ts|scss|json)$/.test(name) && !name.endsWith('.test.ts'))
      acc.push(full)
  }
  return acc
}

describe('no leftover #667874', () => {
  it('mobile src 不再硬编码旧 muted', () => {
    const files = walkVueTsJson(mobileSrc)
    const hits: string[] = []
    for (const file of files) {
      const text = fs.readFileSync(file, 'utf8')
      if (text.includes('#667874') || text.includes('#667874'.toLowerCase()))
        hits.push(path.relative(mobileSrc, file))
    }
    expect(hits).toEqual([])
  })
})
```

`#667874`.toLowerCase() is the same string; that is fine. Also match lowercase in CSS if any.

- [ ] **Step 2: Run test to verify it fails**

Run:

```bash
pnpm --dir apps/mobile test:run src/style/theme-tokens.test.ts
```

Expected: FAIL，`hits` 列出 index.vue、tabbar 等文件。

- [ ] **Step 3: Replace every leftover**

1. `apps/mobile/src/tabbar/index.vue`:

```ts
const inactiveColor = 'var(--color-muted, #4F635F)'
```

2. `apps/mobile/src/tabbar/config.ts` `_tabbar.color`:

```ts
  color: '#4F635F',
```

3. `apps/mobile/src/pages.json` `tabBar.color`:

```json
    "color": "#4F635F",
```

4. In every listed Vue file, replace `text-[#667874]` with `text-muted`. Do not change `text-[#1B7F6B]` / `text-[#1C2B28]` / `bg-[#F4F7F6]`.

Concrete examples (repeat the same substitution everywhere):

`apps/mobile/src/pages/index/index.vue`:

```vue
        <text class="i-carbon-search mr-2 text-muted" />
```

```vue
          :class="activeCategory === '' ? 'bg-[#1B7F6B] text-white' : 'bg-white text-muted'"
```

```vue
          :class="activeCategory === cat ? 'bg-[#1B7F6B] text-white' : 'bg-white text-muted'"
```

```vue
      <view v-if="loading" class="py-10 text-center text-sm text-muted">
```

`apps/mobile/src/components/fg-resource-card/fg-resource-card.vue`:

```vue
        <text v-if="item.category" class="rounded-full bg-[#F4F7F6] px-2 py-0.5 text-xs text-muted">
```

```vue
      <view class="mt-2 flex items-center justify-between text-xs text-muted">
```

`apps/mobile/src/components/fg-empty-state/fg-empty-state.vue` description line:

```vue
    <view v-if="description" class="mt-1 text-sm leading-relaxed text-muted">
```

`apps/mobile/src/pages/me/me.vue`:

```vue
        <view class="mt-1 truncate text-xs text-muted">
```

```vue
        <text class="i-carbon-chevron-right text-muted" />
```

（两处 chevron 都改。）

`apps/mobile/src/pages/auth/login.vue` 与 `about.vue`、`list.vue`、`appointment.vue`、`organization/list.vue`、`detail.vue`：所有 `text-[#667874]` → `text-muted`。

Do not change `goDetail` / `String(item.id)`。

- [ ] **Step 4: Run tests to verify they pass**

Run:

```bash
pnpm --dir apps/mobile test:run src/style/theme-tokens.test.ts
```

Expected: PASS，`hits` 为空。

Then:

```bash
pnpm --dir apps/mobile test:run
```

Expected: 现有 TabbarItem / store 等测试仍 PASS。

- [ ] **Step 5: Commit**

Skip unless the user asked to commit.

```bash
git add apps/mobile
git commit -m "$(cat <<'EOF'
fix: use muted token instead of hardcoded gray on mobile

EOF
)"
```

---

### Task 4: 空状态碳图标 + 44px 热区

**Files:**
- Create: `apps/mobile/src/components/fg-empty-state/fg-empty-state.test.ts`
- Modify: `apps/mobile/src/components/fg-empty-state/fg-empty-state.vue`
- Modify: `apps/mobile/src/pages/index/index.vue`（搜索条、分类胶囊、「查看更多」）
- Modify: `apps/mobile/src/pages/me/me.vue`（登录按钮）
- Modify: `apps/mobile/src/pages/resource/detail.vue`（机构头像圈、电话旁补「拨打」文字）
- Modify: `apps/mobile/src/tabbar/index.vue`（tab 项 `min-h-11`）
- Modify: `apps/mobile/src/pages/organization/list.vue`（类型 Tab 热区）

**Interfaces:**
- Consumes: Task 3 的 `text-muted`
- Produces: 空状态图标 class 含 `i-carbon-document-blank`；操作按钮 class 含 `min-h-11`；首页搜索条、分类胶囊、「查看更多」、我的「登录」、机构类型 Tab、自定义 Tab 项均含 `min-h-11`；详情电话旁有可见文字「拨打」

- [ ] **Step 1: Write the failing empty-state test**

Create `apps/mobile/src/components/fg-empty-state/fg-empty-state.test.ts`:

```ts
import { mount } from '@vue/test-utils'
import { describe, expect, it } from 'vitest'
import FgEmptyState from './fg-empty-state.vue'

describe('fg-empty-state', () => {
  it('用碳图标而不是「空」字', () => {
    const wrapper = mount(FgEmptyState, { props: { title: '暂无内容' } })
    expect(wrapper.html()).toContain('i-carbon-document-blank')
    expect(wrapper.text()).not.toMatch(/^\s*空/)
  })

  it('描述使用 muted，操作按钮至少 44px 高', () => {
    const wrapper = mount(FgEmptyState, {
      props: {
        title: '暂无推荐资源',
        description: '可以先浏览全部资源',
        actionText: '浏览全部资源',
      },
    })
    expect(wrapper.html()).toContain('text-muted')
    const action = wrapper.findAll('view').find(n => n.text() === '浏览全部资源')
    expect(action).toBeTruthy()
    expect(action!.classes().join(' ')).toContain('min-h-11')
  })
})
```

- [ ] **Step 2: Run test to verify it fails**

Run:

```bash
pnpm --dir apps/mobile test:run src/components/fg-empty-state/fg-empty-state.test.ts
```

Expected: FAIL，当前图标区是文字「空」，按钮没有 `min-h-11`。

- [ ] **Step 3: Implement empty state + hit targets**

Replace `apps/mobile/src/components/fg-empty-state/fg-empty-state.vue` template with:

```vue
<template>
  <view class="flex flex-col items-center px-8 py-16 text-center">
    <view class="mb-3 flex h-12 w-12 items-center justify-center rounded-full bg-[#E7F4F0] text-[#1B7F6B]">
      <text class="i-carbon-document-blank text-xl" />
    </view>
    <view class="text-base font-medium text-[#1C2B28]">
      {{ title }}
    </view>
    <view v-if="description" class="mt-1 text-sm leading-relaxed text-muted">
      {{ description }}
    </view>
    <view
      v-if="actionText"
      class="mt-4 inline-flex min-h-11 items-center rounded-full bg-[#1B7F6B] px-5 text-sm text-white active:opacity-80"
      hover-class="opacity-80"
      @click="emit('action')"
    >
      {{ actionText }}
    </view>
  </view>
</template>
```

Keep the existing `<script>` unchanged.

`apps/mobile/src/pages/index/index.vue` 搜索条改为至少 44px，并给可点控件 `active:opacity-80`：

```vue
      <view class="mt-3 flex min-h-11 items-center rounded-full bg-white px-4 active:opacity-80" @click="goSearch">
```

分类胶囊（两处 `inline-block rounded-full px-3 py-1.5`）改为：

```vue
          class="mr-2 inline-flex min-h-11 items-center rounded-full px-3 text-sm"
```

「查看更多」：

```vue
        <text class="inline-flex min-h-11 items-center text-sm text-[#1B7F6B] active:opacity-80" @click="goResourceList()">查看更多</text>
```

宫格入口图标圈已是 `h-11 w-11`（44px），不要改四宫格结构；给外层 cell 加 `min-h-11 active:opacity-80`：

```vue
        class="flex min-h-11 flex-1 flex-col items-center active:opacity-80"
```

`apps/mobile/src/pages/me/me.vue` 登录按钮：

```vue
      <view
        v-if="!tokenStore.hasLogin"
        class="inline-flex min-h-11 items-center rounded-full bg-[#1B7F6B] px-4 text-sm text-white"
        @click="handleLogin"
      >
        登录
      </view>
```

`apps/mobile/src/pages/resource/detail.vue` 机构头像圈 `h-10 w-10` → `h-11 w-11`；电话图标旁加可见文字：

```vue
        <view v-if="resource.contactPhone" class="ml-2 flex min-h-11 items-center text-sm text-[#1B7F6B]">
          <text class="i-carbon-phone mr-1" />
          <text>拨打</text>
        </view>
```

删除原来的单独 `<text v-if="resource.contactPhone" class="i-carbon-phone text-[#1B7F6B]" />`。

`apps/mobile/src/tabbar/index.vue` tab 项：

```vue
          class="flex min-h-11 flex-1 flex-col items-center justify-center"
```

（外层已是 `h-50px`，`min-h-11` 保证单项热区。）

`apps/mobile/src/pages/organization/list.vue` 类型 Tab 的 class 从 `px-3 py-1` 一类矮胶囊改为含 `min-h-11 inline-flex items-center`。若当前是：

```vue
          :class="orgType === tab.value ? 'bg-[#1B7F6B] text-white' : 'bg-[#F4F7F6] text-muted'"
```

在静态 class 里补 `inline-flex min-h-11 items-center px-3`。

- [ ] **Step 4: Run tests to verify they pass**

Run:

```bash
pnpm --dir apps/mobile test:run src/components/fg-empty-state/fg-empty-state.test.ts src/style/theme-tokens.test.ts src/tabbar/TabbarItem.test.ts
```

Expected: PASS。

- [ ] **Step 5: Commit**

Skip unless the user asked to commit.

```bash
git add apps/mobile/src/components/fg-empty-state apps/mobile/src/pages apps/mobile/src/tabbar/index.vue
git commit -m "$(cat <<'EOF'
fix: meet 44px hit targets and replace empty-state glyph with carbon icon

EOF
)"
```

---

### Task 5: PC muted、focus、状态 Tag 文字核对

**Files:**
- Modify: `apps/mobile/src/style/theme-tokens.test.ts`（追加 admin 扫描）
- Modify: `apps/admin/src/App.vue`
- Modify: `apps/admin/src/views/Login.vue`
- Read-only verify (only edit if a tag has no text):
  - `apps/admin/src/views/resource/Index.vue`（已有 `草稿` / `已发布`）
  - `apps/admin/src/views/organization/Index.vue`（已有 `auditStatusMap` 文字）
  - `apps/admin/src/views/appointment/Index.vue`（已有 `statusMap` 文字）

**Interfaces:**
- Consumes: 同一套 muted `#4F635F`、primary `#1B7F6B`
- Produces: admin 源码（`src/`，不含 `node_modules`/`dist`）无 `#667874`；`:root` 仍有 `--el-color-primary: #1b7f6b`；`body` 或 `#app` 上有 `:focus-visible` 3px 主色描边；三个 CRUD 页的 `el-tag` 插槽内仍有中文状态文案

- [ ] **Step 1: Write the failing admin scan**

Append to `apps/mobile/src/style/theme-tokens.test.ts`:

```ts
const adminSrc = path.resolve(mobileSrc, '../../admin/src')

describe('admin tokens', () => {
  it('不再使用旧 muted，并锁定 Element primary', () => {
    const appVue = fs.readFileSync(path.join(adminSrc, 'App.vue'), 'utf8')
    const loginVue = fs.readFileSync(path.join(adminSrc, 'views/Login.vue'), 'utf8')
    expect(appVue).toMatch(/--el-color-primary:\s*#1b7f6b/)
    expect(appVue).not.toContain('#667874')
    expect(loginVue).not.toContain('#667874')
    expect(appVue).toMatch(/#4[fF]635[fF]/)
    expect(appVue).toMatch(/:focus-visible/)
  })

  it('资源状态 Tag 带文字', () => {
    const text = fs.readFileSync(path.join(adminSrc, 'views/resource/Index.vue'), 'utf8')
    expect(text).toContain('已发布')
    expect(text).toContain('草稿')
    expect(text).toContain('el-tag')
  })
})
```

- [ ] **Step 2: Run test to verify it fails**

Run:

```bash
pnpm --dir apps/mobile test:run src/style/theme-tokens.test.ts
```

Expected: FAIL，`App.vue` 仍含 `#667874` 且没有 `:focus-visible`。

- [ ] **Step 3: Patch admin styles**

In `apps/admin/src/App.vue` scoped styles, replace both `#667874` with `#4F635F`（`.brand-desc` 与 `.user-name`）。

In the **unscoped** `<style>` after `--el-color-primary-dark-2`，keep primary derivatives unchanged, then add:

```css
:focus-visible {
  outline: 3px solid var(--el-color-primary);
  outline-offset: 2px;
}

@media (prefers-reduced-motion: reduce) {
  *,
  *::before,
  *::after {
    animation-duration: 0.01ms !important;
    animation-iteration-count: 1 !important;
    transition-duration: 0.01ms !important;
  }
}
```

`.workbench-filter` 已有 `gap: 12px` / `.main` padding `20px 24px 32px`，不要改侧栏宽度和菜单项文案。

In `apps/admin/src/views/Login.vue` `.form-hint`:

```css
.form-hint {
  margin: 8px 0 28px;
  font-size: 13px;
  color: #4F635F;
}
```

Open the three CRUD views and **do not remove** tag inner text. If any `el-tag` is empty, fill it:

- resource: `{{ row.status === 1 ? '已发布' : '草稿' }}`（已存在则不动）
- organization: `{{ auditStatusMap[row.auditStatus ?? 0] }}`
- appointment: `{{ statusMap[row.appointStatus ?? 0] }}`

- [ ] **Step 4: Run test to verify it passes**

Run:

```bash
pnpm --dir apps/mobile test:run src/style/theme-tokens.test.ts src/docs/agents-md.test.ts src/components/fg-empty-state/fg-empty-state.test.ts
```

Expected: PASS。

Then full mobile suite:

```bash
pnpm --dir apps/mobile test:run
```

Expected: PASS。

- [ ] **Step 5: Commit**

Skip unless the user asked to commit.

```bash
git add apps/admin/src/App.vue apps/admin/src/views/Login.vue apps/mobile/src/style/theme-tokens.test.ts
git commit -m "$(cat <<'EOF'
fix: align admin muted contrast and keyboard focus with mobile tokens

EOF
)"
```

---

### Task 6: 对照 spec 做手工验收（不改代码，除非验收失败）

**Files:**
- None, unless a check fails — then fix in the same file Task 2–5 already named. Do not invent new pages.

**Interfaces:**
- Consumes: Tasks 1–5 的产物
- Produces: 下面清单全部勾上，或附失败文件与补丁

- [ ] **Step 1: H5**

1. `apps/mobile` 已在跑则打开 `http://localhost:9000`，否则 `pnpm --dir apps/mobile dev`。
2. 首页：搜索条高度目测可点；四宫格仍是课程/老师/找机构/预约咨询；分类胶囊可点。
3. 次要灰字应比改前更深（`#4F635F`），不是发雾的 `#667874`。
4. 点一张资源卡片进入详情（URL 里 id 为长数字字符串，不是科学计数法）。
5. 「我的」不是 JSON；未登录时「登录」按钮高度接近 44px。
6. Tab 未选中色与正文次要色一致。
7. 键盘 Tab 到可聚焦控件时有约 3px 青绿描边（H5）。

- [ ] **Step 2: PC**

1. 打开 `http://localhost:5174`。
2. 登录页仍是分栏（品牌 + 表单），hint 文字更深。
3. 登录后侧栏仍是资源 / 机构 / 预约。
4. 资源表状态列同时有颜色和「草稿」或「已发布」文字。

- [ ] **Step 3: 小程序（若本机有开发者工具）**

1. 自定义顶栏「你好，需要什么帮助？」和搜索不与右上角胶囊重叠。
2. 点资源卡片详情能打开。
3. 不要为了小程序再复制一套页面。

若无开发者工具：在 H5 验收记录里写明「小程序未在本机打开」，不要假装测过。

- [ ] **Step 4: Commit**

Skip unless the user asked to commit. No files if nothing failed.

---

## Self-review

1. **Spec coverage**
   - Token 表 → Task 2 + Task 5
   - 移动端变量 / Uno / 浅灰字 / Tab 热区 / 胶囊顶栏（不改 hook，只在 AGENTS.md 写约定）→ Task 1、3、4
   - 空状态与卡片次要信息对比度 → Task 3、4
   - PC 主题变量、工作台间距保持、Tag 文字 → Task 5
   - hover/active、图标按钮补文字 → Task 4（`active:opacity-80`、详情「拨打」）
   - AGENTS.md 七节 + alwaysApply 先读 → Task 1
   - 验收 URL → Task 6
   - 不做：IA、字体包、API、微信登录 → 各任务均未列入

2. **Placeholder scan:** 无 TBD；替换列表与 class 都写了具体字符串。

3. **Type consistency:** Uno 色名 `muted`；CSS `--color-muted`；admin 使用十六进制 `#4F635F`（Element 页没有 Uno）。测试文件路径 `apps/mobile/src/docs/agents-md.test.ts` 与 `theme-tokens.test.ts` 在后续任务中是同一文件追加，不是第二份 token 测试。
