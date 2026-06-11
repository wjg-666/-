import axios from 'axios'
import { ElMessage } from 'element-plus'
import { getToken, removeToken } from './auth'

const request = axios.create({
  baseURL: '/api',
  timeout: 10000
})

// 请求拦截器
request.interceptors.request.use(
  config => {
    const token = getToken()
    if (token) {
      config.headers['Authorization'] = `Bearer ${token}`
    }
    return config
  },
  error => {
    console.error('请求错误:', error)
    return Promise.reject(error)
  }
)

// 响应拦截器
request.interceptors.response.use(
  response => {
    const res = response.data

    // 直接返回的数组（如评论列表），原样返回
    if (Array.isArray(res)) {
      return res
    }

    // 根据业务状态码判断
    if (res.code !== undefined) {
      if (res.code === 200) {
        // 返回 data 字段，如果没有 data 则返回整个 res（排除 code/message）
        return res.data !== undefined ? res.data : res
      } else if (res.code === 401) {
        removeToken()
        ElMessage.error('登录已过期，请重新登录')
        window.location.href = '/login'
        return Promise.reject(new Error('登录过期'))
      } else {
        ElMessage.error(res.message || '请求失败')
        return Promise.reject(new Error(res.message || '请求失败'))
      }
    }

    return res
  },
  error => {
    console.error('响应错误:', error)

    if (error.response) {
      switch (error.response.status) {
        case 401:
          removeToken()
          ElMessage.error('未授权，请登录')
          window.location.href = '/login'
          break
        case 403:
          ElMessage.error('没有权限')
          break
        case 404:
          ElMessage.error('资源不存在')
          break
        case 500:
          ElMessage.error('服务器错误')
          break
        default:
          ElMessage.error(error.response.data?.message || '请求失败')
      }
    } else if (error.code === 'ECONNABORTED') {
      ElMessage.error('请求超时，请稍后重试')
    } else if (error.message?.includes('Network Error')) {
      ElMessage.error('网络连接失败，请检查后端服务是否启动')
    } else if (error.message && error.message !== 'cancel') {
      ElMessage.error(error.message)
    }

    return Promise.reject(error)
  }
)

export default request
