import { beforeEach, describe, expect, it, vi } from 'vitest'
import {
  CURRENT_ROLE_STORAGE_KEY,
  canSwitchIdentity,
  clearCurrentRole,
  isTeacherRole,
  planColdStartRole,
  readCachedRole,
  resolveRole,
  roleTagLabel,
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

describe('identity display and switch helpers', () => {
  it('labels teacher as 老师 and everything else as 家长', () => {
    expect(roleTagLabel('special_teacher')).toBe('老师')
    expect(roleTagLabel('special_parent')).toBe('家长')
    expect(roleTagLabel('')).toBe('家长')
  })

  it('shows 切换身份 only when both special roles are owned', () => {
    expect(canSwitchIdentity(['special_parent', 'special_teacher'])).toBe(true)
    expect(canSwitchIdentity(['special_teacher', 'special_parent', 'admin'])).toBe(true)
    expect(canSwitchIdentity(['special_parent'])).toBe(false)
    expect(canSwitchIdentity(['special_teacher'])).toBe(false)
    expect(canSwitchIdentity([])).toBe(false)
  })

  it('treats only special_teacher as the teacher home role', () => {
    expect(isTeacherRole('special_teacher')).toBe(true)
    expect(isTeacherRole('special_parent')).toBe(false)
    expect(isTeacherRole('')).toBe(false)
  })
})

describe('planColdStartRole', () => {
  it('puts the cached teacher role when the server still has parent', () => {
    expect(planColdStartRole('special_teacher', ['special_parent', 'special_teacher'], 'special_parent')).toEqual({
      role: 'special_teacher',
      shouldPut: true,
    })
  })

  it('skips PUT when cache already matches the server role', () => {
    expect(planColdStartRole('special_teacher', ['special_parent', 'special_teacher'], 'special_teacher')).toEqual({
      role: 'special_teacher',
      shouldPut: false,
    })
  })

  it('falls back to parent and skips PUT when the cached teacher was revoked', () => {
    expect(planColdStartRole('special_teacher', ['special_parent'], 'special_parent')).toEqual({
      role: 'special_parent',
      shouldPut: false,
    })
  })

  it('defaults empty cache to parent and puts when the server is still teacher', () => {
    expect(planColdStartRole('', ['special_parent', 'special_teacher'], 'special_teacher')).toEqual({
      role: 'special_parent',
      shouldPut: true,
    })
  })

  it('does not PUT when no special role can be resolved', () => {
    expect(planColdStartRole('special_parent', ['admin'], 'special_parent')).toEqual({
      role: '',
      shouldPut: false,
    })
  })
})
