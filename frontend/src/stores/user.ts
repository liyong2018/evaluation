import { ref, computed } from 'vue'
import { defineStore } from 'pinia'

export interface UserInfo {
  id?: number
  username: string
  isAdmin: boolean
}

export const useUserStore = defineStore('user', () => {
  // 状态
  const token = ref<string>(localStorage.getItem('token') || '')
  const userInfo = ref<UserInfo | null>(
    localStorage.getItem('userInfo')
      ? JSON.parse(localStorage.getItem('userInfo')!)
      : null
  )

  // 计算属性
  const isLoggedIn = computed(() => !!token.value && !!userInfo.value)
  const isAdmin = computed(() => userInfo.value?.isAdmin || false)
  const username = computed(() => userInfo.value?.username || '')

  // 方法
  function login(user: UserInfo) {
    const tokenValue = `token_${Date.now()}`
    token.value = tokenValue
    userInfo.value = user

    // 持久化到 localStorage
    localStorage.setItem('token', tokenValue)
    localStorage.setItem('userInfo', JSON.stringify(user))
  }

  function logout() {
    token.value = ''
    userInfo.value = null

    // 清除 localStorage
    localStorage.removeItem('token')
    localStorage.removeItem('userInfo')
  }

  return {
    token,
    userInfo,
    isLoggedIn,
    isAdmin,
    username,
    login,
    logout
  }
})
