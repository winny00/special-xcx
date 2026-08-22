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
