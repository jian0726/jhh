/**
 * user.js - Pinia用户状态管理
 *
 * 负责保存Token、用户信息以及登录/退出逻辑。
 */
import { defineStore } from 'pinia'
import { authApi } from '../api/auth'
import { extractUserId, isTokenExpired } from '../utils/jwt'

export const useUserStore = defineStore('user', {
  state: () => ({
    token: localStorage.getItem('token') || '',
    userInfo: null
  }),
  getters: {
    isLogin: (state) => !!state.token
  },
  actions: {
    restoreToken() {
      const token = localStorage.getItem('token')
      if (token) {
        this.token = token
      }
    },

    /**
     * 从 JWT Token 中解析用户ID，然后获取完整用户信息
     * 解决页面刷新后 userInfo 丢失的问题
     */
    async restoreUserInfo() {
      if (!this.token) return
      
      // 检查token是否过期
      if (isTokenExpired(this.token)) {
        this.logout()
        return
      }
      
      try {
        // 使用安全的JWT解析工具函数
        const userId = extractUserId(this.token)
        if (userId) {
          const info = await authApi.getUserInfo(userId)
          if (info) {
            this.userInfo = info
          }
        }
      } catch {
        // JWT 解析失败或API调用失败，token可能已过期
        this.logout()
      }
    },
    async login(payload) {
      const response = await authApi.login(payload)
      // 后端返回 {token, yongHu} 被 interceptor 解包后直接得到
      if (response && response.token) {
        this.token = response.token
        this.userInfo = response.yongHu || response.user || null
        localStorage.setItem('token', response.token)
        return response
      }
      throw new Error(response?.message || '登录失败')
    },
    logout() {
      this.token = ''
      this.userInfo = null
      localStorage.removeItem('token')
    },
    async getUserInfo(id) {
      const response = await authApi.getUserInfo(id)
      if (response) {
        this.userInfo = response
        return response
      }
      throw new Error('获取用户信息失败')
    }
  }
})
