import { defineStore } from 'pinia'
import { ref } from 'vue'
import request from '@/utils/request'
import { getToken, setToken, removeToken, getUserInfo, setUserInfo } from '@/utils/auth'

export const useUserStore = defineStore('user', () => {
  const user = ref(getUserInfo())
  const token = ref(getToken())

  function login(loginForm) {
    return request.post('/auth/login', loginForm).then(res => {
      token.value = res.token
      user.value = res.user
      setToken(res.token)
      setUserInfo(res.user)
    })
  }

  function logout() {
    token.value = ''
    user.value = null
    removeToken()
  }

  return { user, token, login, logout }
})
