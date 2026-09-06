/**
 * request.js - Axios HTTP请求封装
 *
 * 该文件封装了Axios请求，包含：
 * - 基础配置：baseURL、超时时间、请求头
 * - 请求拦截器：自动添加Token到请求头
 * - 响应拦截器：统一处理响应数据和错误
 *
 * 使用方式：
 * import request from '@/utils/request'
 * request({ url: '/api/xxx', method: 'get' })
 */
import axios from 'axios'
import { ElMessage } from 'element-plus'

/**
 * 创建Axios实例
 * 配置基础URL、超时时间和默认请求头
 */
const request = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '/',  // API基础路径（空值时使用根路径，配合Vite代理）
  timeout: 10000,                                         // 请求超时时间（10秒）
  headers: {
    'Content-Type': 'application/json'                    // 默认请求头
  }
})

/**
 * 请求拦截器
 * 在发送请求前自动添加Token到Authorization头
 * 用于身份验证
 */
request.interceptors.request.use(
  config => {
    // 从localStorage获取Token
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  error => {
    return Promise.reject(error)
  }
)

/**
 * 响应拦截器
 * 统一处理响应数据和错误
 * - 成功响应：返回data字段或整个响应数据
 * - 错误响应：根据状态码处理不同错误
 */
request.interceptors.response.use(
  response => {
    // 如果响应中有data字段，返回data，否则返回整个响应
    if (response.data && response.data.data !== undefined) {
      return response.data.data
    }
    return response.data
  },
  async error => {
    // 处理HTTP错误状态码
    if (error.response) {
      switch (error.response.status) {
        case 401:
          // 未授权：清除Token并跳转到登录页
          localStorage.removeItem('token')
          // 使用lazy import避免循环依赖
          const { useUserStore } = await import('../stores/user')
          const userStore = useUserStore()
          userStore.token = ''
          userStore.userInfo = null
          
          // 使用router跳转而不是window.location.href
          const router = await import('../router')
          router.default.push('/login')
          ElMessage.warning('登录已过期，请重新登录')
          break
        case 403:
          // 禁止访问
          ElMessage.error('没有权限访问该资源')
          break
        case 404:
          // 资源不存在
          ElMessage.error('请求的资源不存在')
          break
        case 500:
          // 服务器错误
          ElMessage.error('服务器内部错误，请稍后重试')
          break
        default:
          // 其他错误
          ElMessage.error(error.response.data?.message || '请求失败')
      }
    } else {
      // 网络错误
      ElMessage.error('网络连接失败，请检查网络')
    }
    return Promise.reject(error)
  }
)

const API_BASE = import.meta.env.VITE_API_BASE_URL || ''

export function resolveImageUrl(url) {
  if (!url) return ''
  if (/^(data:|blob:|https?:\/\/)/i.test(url)) return url
  return API_BASE + url
}

export default request
