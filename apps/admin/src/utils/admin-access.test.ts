import { describe, expect, it } from 'vitest'
import { canOpenAdminPath, isSuperAdmin, isTeacherOnly } from './admin-access'

describe('admin-access', () => {
  it('blocks teacher from account and resources', () => {
    expect(canOpenAdminPath('/account', ['special_teacher'])).toBe(false)
    expect(canOpenAdminPath('/appointment', ['special_teacher'])).toBe(true)
    expect(canOpenAdminPath('/dashboard', ['superadmin'])).toBe(true)
  })

  it('treats special_teacher without superadmin as teacher-only', () => {
    expect(isTeacherOnly(['special_teacher'])).toBe(true)
    expect(isTeacherOnly(['special_teacher', 'special_parent'])).toBe(true)
    expect(isTeacherOnly(['special_teacher', 'superadmin'])).toBe(false)
    expect(isSuperAdmin(['superadmin'])).toBe(true)
  })

  it('lets teachers open own profile and appointments only', () => {
    expect(canOpenAdminPath('/teacher/me', ['special_teacher'])).toBe(true)
    expect(canOpenAdminPath('/teacher', ['special_teacher'])).toBe(true)
    expect(canOpenAdminPath('/resource', ['special_teacher'])).toBe(false)
    expect(canOpenAdminPath('/organization', ['special_teacher'])).toBe(false)
    expect(canOpenAdminPath('/parent', ['special_teacher'])).toBe(false)
    expect(canOpenAdminPath('/audit', ['special_teacher'])).toBe(false)
    expect(canOpenAdminPath('/dashboard', ['special_teacher'])).toBe(false)
  })
})
