import { defineStore } from 'pinia'
import { ref } from 'vue'
import { getToken, removeToken, setToken } from '@/api/request'

export const useAuthStore = defineStore('auth', () => {
  const token = ref<string | null>(getToken())

  function saveToken(value: string) {
    token.value = value
    setToken(value)
  }

  function logout() {
    token.value = null
    removeToken()
  }

  const isLoggedIn = () => !!token.value

  return { token, saveToken, logout, isLoggedIn }
})
