const TOKEN_KEY = 'blog_token'
const USER_KEY = 'blog_user'

// 获取Token
export function getToken() {
  return localStorage.getItem(TOKEN_KEY)
}

// 设置Token
export function setToken(token) {
  localStorage.setItem(TOKEN_KEY, token)
}

// 移除Token
export function removeToken() {
  localStorage.removeItem(TOKEN_KEY)
  localStorage.removeItem(USER_KEY)
}

// 获取用户信息
export function getUserInfo() {
  const userStr = localStorage.getItem(USER_KEY)
  if (userStr) {
    try {
      return JSON.parse(userStr)
    } catch (e) {
      return null
    }
  }
  return null
}

// 设置用户信息
export function setUserInfo(user) {
  localStorage.setItem(USER_KEY, JSON.stringify(user))
}

// 检查是否已登录
export function isLoggedIn() {
  return !!getToken()
}

// 清除所有认证信息
export function clearAuth() {
  removeToken()
}
