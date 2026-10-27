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
     * 从 localStorage 的 token 反解用户ID并拉取完整用户信息。
     * 解决刷新页面后 userInfo 丢失、个人中心显示空数据的问题。
     */
    async restoreUserInfo() {
      if (!this.token) return
      if (isTokenExpired(this.token)) {
        this.logout()
        return
      }
      try {
        const userId = extractUserId(this.token)
        if (userId !== null && userId !== undefined) {
          const info = await authApi.getUserInfo(userId)
          if (info) {
            this.userInfo = info
          }
        }
      } catch {
        // token 失效（401）时拦截器已处理，这里静默即可，避免误踢用户
      }
    },

    async login(payload) {
      const response = await authApi.login(payload)
      // 后端返回 { success, message, token, user }，拦截器已解开外层信封
      if (response && (response.success || response.token)) {
        this.token = response.token
        this.userInfo = response.user || response.yongHu || null
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
