import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem('token') || '')
  const user = ref(JSON.parse(localStorage.getItem('user') || 'null'))
  const farmerProfile = ref(JSON.parse(localStorage.getItem('farmerProfile') || 'null'))

  const isLoggedIn = computed(() => !!token.value && !!user.value)
  const isFarmer = computed(() => isLoggedIn.value && user.value.role === 'FARMER')
  const isAdmin = computed(() => isLoggedIn.value && user.value.role === 'ADMIN')

  function setUser(userData, tokenValue, profileData) {
    user.value = userData
    token.value = tokenValue
    localStorage.setItem('user', JSON.stringify(userData))
    localStorage.setItem('token', tokenValue)

    if (profileData) {
      farmerProfile.value = profileData
      localStorage.setItem('farmerProfile', JSON.stringify(profileData))
    } else {
      farmerProfile.value = null
      localStorage.removeItem('farmerProfile')
    }
  }

  function logout() {
    user.value = null
    token.value = ''
    farmerProfile.value = null
    localStorage.removeItem('user')
    localStorage.removeItem('token')
    localStorage.removeItem('farmerProfile')
    // Optionally, redirect to login page
    window.location.href = '/login'
  }

  return { user, token, farmerProfile, isLoggedIn, isFarmer, isAdmin, setUser, logout }
})
