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
