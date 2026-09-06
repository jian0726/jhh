/**
 * auth.js - Pinia认证模态框状态管理
 *
 * 负责控制认证弹窗的显示、隐藏和登录/注册模式切换。
 * 支持登录后自动执行待处理操作（如购买、发布等）。
 */
import { defineStore } from 'pinia'

export const useAuthStore = defineStore('auth', {
  state: () => ({
    visible: false,
    mode: 'login',
    pendingAction: null // 登录成功后执行的待处理操作
  }),
  actions: {
    openLogin(callback) {
      this.mode = 'login'
      this.visible = true
      if (callback) {
        this.pendingAction = callback
      }
    },
    openRegister() {
      this.mode = 'register'
      this.visible = true
      this.pendingAction = null
    },
    openResetPassword() {
      this.mode = 'reset'
      this.visible = true
      this.pendingAction = null
    },
    close() {
      this.visible = false
      this.pendingAction = null
    },
    executePendingAction() {
      if (this.pendingAction && typeof this.pendingAction === 'function') {
        this.pendingAction()
        this.pendingAction = null
      }
    }
  }
})
