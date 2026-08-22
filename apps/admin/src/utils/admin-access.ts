const ROLES_KEY = 'admin_roles'
const CURRENT_ROLE_KEY = 'admin_current_role'

export function isSuperAdmin(roles: string[]) {
  return roles.includes('superadmin')
}

export function isTeacherOnly(roles: string[]) {
  return roles.includes('special_teacher') && !isSuperAdmin(roles)
}

export function canOpenAdminPath(path: string, roles: string[]) {
  if (isSuperAdmin(roles)) return true
  if (!isTeacherOnly(roles)) return false
  return path.startsWith('/teacher') || path.startsWith('/appointment')
}

export function adminHomePath(roles: string[]) {
  return isTeacherOnly(roles) ? '/appointment' : '/dashboard'
}

export function readStoredRoles(): string[] {
  try {
    const raw = localStorage.getItem(ROLES_KEY)
    if (!raw)
      return []
    const parsed = JSON.parse(raw) as unknown
    return Array.isArray(parsed) ? parsed.filter((item): item is string => typeof item === 'string') : []
  }
  catch {
    return []
  }
}

export function readStoredCurrentRole(): string | null {
  return localStorage.getItem(CURRENT_ROLE_KEY)
}

export function persistAdminRoles(roles: string[], currentRole?: string | null) {
  localStorage.setItem(ROLES_KEY, JSON.stringify(roles))
  if (currentRole)
    localStorage.setItem(CURRENT_ROLE_KEY, currentRole)
  else
    localStorage.removeItem(CURRENT_ROLE_KEY)
}

export function clearStoredAdminRoles() {
  localStorage.removeItem(ROLES_KEY)
  localStorage.removeItem(CURRENT_ROLE_KEY)
}
