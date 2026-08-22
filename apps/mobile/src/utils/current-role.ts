export const CURRENT_ROLE_STORAGE_KEY = 'special_current_role'

export type SpecialRoleKey = 'special_parent' | 'special_teacher'

function isSpecialRole(value: string): value is SpecialRoleKey {
  return value === 'special_parent' || value === 'special_teacher'
}

export function readCachedRole(): SpecialRoleKey | '' {
  const raw = uni.getStorageSync(CURRENT_ROLE_STORAGE_KEY)
  return typeof raw === 'string' && isSpecialRole(raw) ? raw : ''
}

export function writeCachedRole(role: SpecialRoleKey): void {
  uni.setStorageSync(CURRENT_ROLE_STORAGE_KEY, role)
}

export function clearCurrentRole(): void {
  uni.removeStorageSync(CURRENT_ROLE_STORAGE_KEY)
}

export function resolveRole(cached: string, owned: string[]): SpecialRoleKey | '' {
  if (isSpecialRole(cached) && owned.includes(cached)) {
    return cached
  }
  if (owned.includes('special_parent')) {
    return 'special_parent'
  }
  if (owned.includes('special_teacher')) {
    return 'special_teacher'
  }
  return ''
}

export function syncCachedRole(owned: string[]): void {
  const role = resolveRole(readCachedRole(), owned)
  if (role) {
    writeCachedRole(role)
    return
  }
  clearCurrentRole()
}

export function isTeacherRole(role: string): boolean {
  return role === 'special_teacher'
}

export function roleTagLabel(role: string): '老师' | '家长' {
  return isTeacherRole(role) ? '老师' : '家长'
}

export function canSwitchIdentity(owned: string[]): boolean {
  return owned.includes('special_parent') && owned.includes('special_teacher')
}

export function planColdStartRole(
  cached: string,
  owned: string[],
  serverCurrentRole?: string,
): { role: SpecialRoleKey | '', shouldPut: boolean } {
  const role = resolveRole(cached, owned)
  return {
    role,
    shouldPut: Boolean(role) && role !== serverCurrentRole,
  }
}

export function isPhoneBound(value: unknown): boolean {
  return value === true
}
