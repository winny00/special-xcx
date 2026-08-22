import type { RouteLocationNormalized } from 'vue-router'
import { describe, expect, it } from 'vitest'
import { shouldRecordTab, tabFromRoute } from './route-meta'

function route(partial: Partial<RouteLocationNormalized>): RouteLocationNormalized {
  return {
    path: '/',
    meta: {},
    params: {},
    query: {},
    ...partial,
  } as RouteLocationNormalized
}

describe('shouldRecordTab', () => {
  it('skips redirect-only / so it cannot become 特教管理后台', () => {
    expect(shouldRecordTab(route({ path: '/', meta: {} }))).toBe(false)
  })

  it('skips /resource redirect', () => {
    expect(shouldRecordTab(route({ path: '/resource', meta: {} }))).toBe(false)
  })

  it('records organization', () => {
    expect(shouldRecordTab(route({
      path: '/organization',
      meta: { title: '机构管理', breadcrumb: ['机构管理'] },
    }))).toBe(true)
  })
})

describe('tabFromRoute', () => {
  it('uses route title, not the app name fallback', () => {
    expect(tabFromRoute(route({
      path: '/organization',
      meta: { title: '机构管理' },
    })).title).toBe('机构管理')
  })
})
