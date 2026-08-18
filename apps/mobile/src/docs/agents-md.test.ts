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
    expect(text).toContain('禁止换成')
    expect(text).toContain('#0891B2')
  })
})
