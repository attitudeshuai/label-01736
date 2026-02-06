import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { authApi } from '@/api/auth'
import router from '@/router'

export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem('token') || '')
  const userInfo = ref(JSON.parse(localStorage.getItem('userInfo') || '{}'))
  
  const role = computed(() => userInfo.value.role)
  const isAdmin = computed(() => userInfo.value.role === 1)
  const isTeacher = computed(() => userInfo.value.role === 2)
  const isStudent = computed(() => userInfo.value.role === 3)
  
  const setToken = (newToken) => {
    token.value = newToken
    localStorage.setItem('token', newToken)
  }
  
  const setUserInfo = (info) => {
    userInfo.value = info
    localStorage.setItem('userInfo', JSON.stringify(info))
  }
  
  const login = async (username, password) => {
    const res = await authApi.login(username, password)
    setToken(res.data.token)
    setUserInfo({
      id: res.data.userId,
      username: res.data.username,
      realName: res.data.realName,
      role: res.data.role
    })
    return res
  }
  
  const logout = async () => {
    try {
      await authApi.logout()
    } finally {
      token.value = ''
      userInfo.value = {}
      localStorage.removeItem('token')
      localStorage.removeItem('userInfo')
      router.push('/login')
    }
  }
  
  const fetchUserInfo = async () => {
    const res = await authApi.getUserInfo()
    setUserInfo(res.data)
  }
  
  return {
    token,
    userInfo,
    role,
    isAdmin,
    isTeacher,
    isStudent,
    setToken,
    setUserInfo,
    login,
    logout,
    fetchUserInfo
  }
})
