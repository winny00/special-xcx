import { defineStore } from 'pinia'
import { ref } from 'vue'
import request, { getToken, removeToken, setToken } from '@/api/request'
import {
  adminHomePath,
  clearStoredAdminRoles,
  persistAdminRoles,
  readStoredCurrentRole,
  readStoredRoles,
} from '@/utils/admin-access'

export interface AdminProfile {
  roles?: string[]
  currentRole?: string
  user?: { userId?: string | number }
}

export const useAuthStore = defineStore('auth', () => {
  const token = ref<string | null>(getToken())
  const roles = ref<string[]>(readStoredRoles())
  const currentRole = ref<string | null>(readStoredCurrentRole())
  const userId = ref('')

  function saveToken(value: string) {
    token.value = value
    setToken(value)
  }

  function applyProfile(info: AdminProfile) {
    roles.value = [...(info.roles || [])]
    currentRole.value = info.currentRole || null
    userId.value = info.user?.userId == null ? '' : String(info.user.userId)
    persistAdminRoles(roles.value, currentRole.value)
  }

  async function fetchProfile() {
    const info = await request.get<AdminProfile>('/system/user/getInfo')
    applyProfile(info)
    return info
  }

  function logout() {
    token.value = null
    roles.value = []
    currentRole.value = null
    userId.value = ''
    removeToken()
    clearStoredAdminRoles()
  }

  const isLoggedIn = () => !!token.value
  const homePath = () => adminHomePath(roles.value)

  return {
    token,
    roles,
    currentRole,
    userId,
    saveToken,
    fetchProfile,
    applyProfile,
    logout,
    isLoggedIn,
    homePath,
  }
})
