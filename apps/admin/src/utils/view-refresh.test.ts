import { describe, expect, it } from 'vitest'
import { incrementRefreshTick, viewRefreshKey } from './view-refresh'

describe('viewRefreshKey', () => {
  it('includes path and tick so the router-view remount key changes on refresh', () => {
    expect(viewRefreshKey('/resource/course', 0)).toBe('/resource/course:0')
    expect(viewRefreshKey('/resource/course', 1)).toBe('/resource/course:1')
  })
})

describe('incrementRefreshTick', () => {
  it('bumps the tick so a second click remounts again', () => {
    const tick = incrementRefreshTick(0)
    expect(tick).toBe(1)
    expect(viewRefreshKey('/organization', incrementRefreshTick(tick))).toBe('/organization:2')
  })
})
