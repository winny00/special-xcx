import { describe, expect, it } from 'vitest'
import { prunePhantomTabs, removeTab, upsertTab, type TabItem } from './tab-nav'

const dash: TabItem = { path: '/dashboard', title: '数据概览', closable: false }
const course: TabItem = { path: '/resource/course', title: '课程管理', closable: true }

describe('upsertTab', () => {
  it('adds new tab', () => {
    expect(upsertTab([dash], course)).toEqual([dash, course])
  })
  it('does not duplicate', () => {
    expect(upsertTab([dash, course], course)).toEqual([dash, course])
  })
})

describe('prunePhantomTabs', () => {
  it('drops the untitled 特教管理后台 tab so breadcrumb home selects 数据概览', () => {
    const phantom: TabItem = { path: '/', title: '特教管理后台', closable: true }
    const org: TabItem = { path: '/organization', title: '机构管理', closable: true }
    expect(prunePhantomTabs([dash, phantom, org])).toEqual([dash, org])
  })
})

describe('removeTab', () => {
  it('activates right neighbor when closing middle tab', () => {
    const org: TabItem = { path: '/organization', title: '机构管理', closable: true }
    const { tabs, nextPath } = removeTab([dash, course, org], course.path)
    expect(tabs).toEqual([dash, org])
    expect(nextPath).toBe('/organization')
  })
  it('falls back to dashboard when all closable tabs removed', () => {
    const { tabs, nextPath } = removeTab([dash, course], course.path)
    expect(nextPath).toBe('/dashboard')
    expect(tabs.some(t => t.path === '/dashboard')).toBe(true)
  })
})
