import { beforeEach, describe, expect, it, vi } from 'vitest'
import {
  CURRENT_ROLE_STORAGE_KEY,
  clearCurrentRole,
  readCachedRole,
  resolveRole,
  syncCachedRole,
  writeCachedRole,
} from './current-role'

describe('resolveRole', () => {
  it('falls back when cached role was removed', () => {
    expect(resolveRole('special_teacher', ['special_parent'])).toBe('special_parent')
  })

  it('keeps cached teacher when still owned', () => {
    expect(resolveRole('special_teacher', ['special_parent', 'special_teacher'])).toBe('special_teacher')
  })

  it('prefers parent when cache is empty and both are owned', () => {
    expect(resolveRole('', ['special_parent', 'special_teacher'])).toBe('special_parent')
  })

  it('uses teacher when that is the only owned role', () => {
    expect(resolveRole('', ['special_teacher'])).toBe('special_teacher')
  })

  it('returns empty when no special role is owned', () => {
    expect(resolveRole('special_parent', ['admin'])).toBe('')
  })
})

describe('cached role storage', () => {
  beforeEach(() => {
    vi.mocked(uni.getStorageSync).mockReturnValue(null)
  })

  it('writes the role under special_current_role', () => {
    writeCachedRole('special_parent')
    expect(uni.setStorageSync).toHaveBeenCalledWith(CURRENT_ROLE_STORAGE_KEY, 'special_parent')
  })

  it('reads a stored special role', () => {
    vi.mocked(uni.getStorageSync).mockReturnValue('special_teacher')
    expect(readCachedRole()).toBe('special_teacher')
    expect(uni.getStorageSync).toHaveBeenCalledWith(CURRENT_ROLE_STORAGE_KEY)
  })

  it('returns empty when storage is missing or not a special role', () => {
    expect(readCachedRole()).toBe('')
    vi.mocked(uni.getStorageSync).mockReturnValue('admin')
    expect(readCachedRole()).toBe('')
  })

  it('clears the stored role', () => {
    clearCurrentRole()
    expect(uni.removeStorageSync).toHaveBeenCalledWith(CURRENT_ROLE_STORAGE_KEY)
  })

  it('clears stale cache when resolved role is empty', () => {
    vi.mocked(uni.getStorageSync).mockReturnValue('special_teacher')
    syncCachedRole(['admin'])
    expect(uni.removeStorageSync).toHaveBeenCalledWith(CURRENT_ROLE_STORAGE_KEY)
    expect(uni.setStorageSync).not.toHaveBeenCalled()
  })

  it('writes the resolved role after login sync', () => {
    vi.mocked(uni.getStorageSync).mockReturnValue('special_teacher')
    syncCachedRole(['special_parent'])
    expect(uni.setStorageSync).toHaveBeenCalledWith(CURRENT_ROLE_STORAGE_KEY, 'special_parent')
  })
})
