/**
 * request.js - Axios HTTP请求封装
 *
 * 该文件封装了Axios请求，包含：
 * - 基础配置：baseURL、超时时间、请求头
 * - 请求拦截器：自动添加Token到请求头
 * - 响应拦截器：统一处理后端 { code, message, data } 信封与错误
 *
 * 约定（与后端 com.delta.rental.dto.ApiResponse 对齐）：
 * - 成功：HTTP 2xx，响应体为 { code:200, message, data }，业务数据在 data 中
 * - 失败：HTTP 4xx/5xx，响应体同样为 { code, message, data:{ success:false, message } }
 *
 * 使用方式：
 * import request from '@/utils/request'
 * request({ url: '/api/xxx', method: 'get' })
 */
import axios from 'axios'

/**
 * 基础路径
 * 说明：各 api 模块里的 url 已自带 `/api/` 前缀，因此这里默认使用同源（''），
 * 若 .env 中配置了 VITE_API_BASE_URL（如 http://localhost:8080），则请求该地址。
 * 之前默认值写死成 '/api' 会和 url 里的 '/api' 叠加成 '/api/api/...'，这里做了修正。
 */
const baseURL = import.meta.env.VITE_API_BASE_URL || ''

const request = axios.create({
  baseURL,
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json',
  },
})

/**
 * 请求拦截器：自动携带 Token
 */
request.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => Promise.reject(error),
)

/**
 * 判断是否为后端统一响应信封
 */
const isEnvelope = (body) =>
  body && typeof body === 'object' && 'code' in body && 'data' in body

/**
 * 响应拦截器
 * - 成功：解开统一信封，直接把 data 交给调用方（无 data 字段时返回原始响应）
 * - 失败：把后端 message 透出成 error.message，并保留 error.response 兼容旧写法
 */
request.interceptors.response.use(
  (response) => {
    const body = response.data
    if (isEnvelope(body)) {
      return body.data
    }
    if (body && typeof body === 'object' && body.data !== undefined) {
      return body.data
    }
    return body
  },
  (error) => {
    const res = error.response
    const url = error.config?.url || ''
    // 认证类接口（登录/注册/找回密码）失败属于业务校验，不应触发“登录态失效”跳转
    const isAuthApi = url.includes('/auth/')

    let message = error.message
    if (res) {
      const body = res.data
      message =
        (body && (body.message || (body.data && body.data.message))) ||
        (res.statusText || message)

      if (res.status === 401 && !isAuthApi) {
        // 仅当本地确实存有 token（登录态失效）时才跳转首页；
        // 若本来就没登录，则把“请先登录”的提示交给页面展示，避免生硬跳转
        const hadToken = !!localStorage.getItem('token')
        localStorage.removeItem('token')
        if (hadToken) {
          window.location.href = '/'
        }
      }
    } else {
      message = '网络异常，请检查后端服务是否已启动'
    }

    // 抛出带后端消息的 Error，同时保留 response/config，兼容 error.response?.data?.message 写法
    const normalized = new Error(message)
    normalized.response = res
    normalized.config = error.config
    return Promise.reject(normalized)
  },
)

export default request
