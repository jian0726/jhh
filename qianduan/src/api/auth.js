/**
 * auth.js - 认证相关API接口
 *
 * 该文件封装了用户认证相关的API调用，包含：
 * - login: 用户登录
 * - register: 用户注册
 * - sendCode: 发送验证码
 * - getUserInfo: 获取用户信息
 *
 */
import request from '../utils/request'

/**
 * 认证API对象
 * 包含所有认证相关的接口方法
 */
export const authApi = {
  /**
   * 用户登录
   * @param {Object} data - 登录信息 { phone, password }
   * @returns {Promise} 登录结果
   */
  login(data) {
    return request({
      url: '/api/auth/denglu',
      method: 'post',
      data
    })
  },

  /**
   * 用户注册
   * @param {Object} data - 注册信息 { phone, password, code }
   * @returns {Promise} 注册结果
   */
  register(data) {
    return request({
      url: '/api/auth/zhuce',
      method: 'post',
      data
    })
  },

  /**
   * 发送手机验证码
   * @param {String} phone - 手机号码
   * @returns {Promise} 发送结果
   */
  sendCode(phone) {
    return request({
      url: `/api/auth/fasong-yanzhengma?shouJiHao=${phone}`,
      method: 'get'
    })
  },

  /**
   * 发送邮箱验证码
   * @param {Object} data - 包含邮箱的数据对象 { email }
   * @returns {Promise} 发送结果
   */
  sendVerifyCode(data) {
    return request({
      url: '/api/auth/send-email-code',
      method: 'post',
      data
    })
  },

  /**
   * 重置密码
   * @param {Object} data - 重置信息 { email, verifyCode, miMa }
   * @returns {Promise} 重置结果
   */
  resetPassword(data) {
    return request({
      url: '/api/auth/reset-password',
      method: 'post',
      data
    })
  },

  /**
   * 获取用户信息
   * @param {String|Number} id - 用户ID
   * @returns {Promise} 用户信息
   */
  getUserInfo(id) {
    return request({
      url: `/api/auth/yonghu/${id}`,
      method: 'get'
    })
  }
}
