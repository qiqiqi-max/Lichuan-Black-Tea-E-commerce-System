import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

const USER_STORAGE_KEY = 'user'
const TOKEN_STORAGE_KEY = 'token'

export const useUserStore = defineStore('user', () => {
  const token = ref(sessionStorage.getItem(TOKEN_STORAGE_KEY) || '')
  const user = ref(JSON.parse(sessionStorage.getItem(USER_STORAGE_KEY) || '{}'))

  const role = computed(() => user.value?.role || '')

  function login(userData, tokenValue) {
    user.value = userData
    token.value = tokenValue
    sessionStorage.setItem(USER_STORAGE_KEY, JSON.stringify(userData))
    sessionStorage.setItem(TOKEN_STORAGE_KEY, tokenValue)
  }

  function logout() {
    user.value = {}
    token.value = ''
    sessionStorage.removeItem(USER_STORAGE_KEY)
    sessionStorage.removeItem(TOKEN_STORAGE_KEY)
  }

  return { user, token, role, login, logout }
})
