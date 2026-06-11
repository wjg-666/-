import { defineStore } from 'pinia'
import { ref } from 'vue'
import request from '@/utils/request'

export const useUserStore = defineStore('user', () => {
  const user = ref(JSON.parse(localStorage.getItem('user') || 'null'))
  const token = ref(localStorage.getItem('token') || '')

  function login(loginForm) {
    return request.post('/auth/login', loginForm).then(res => {
      token.value = res.token
      user.value = res.user
      localStorage.setItem('token', res.token)
      localStorage.setItem('user', JSON.stringify(res.user))
    })
  }

  function logout() {
    token.value = ''
    user.value = null
    localStorage.removeItem('token')
    localStorage.removeItem('user')
  }

  return { user, token, login, logout }
})
