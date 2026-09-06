/**
 * message.js - 消息相关API接口
 *
 * 该文件封装了消息相关的API调用，包含：
 * - getMessages: 获取用户消息列表
 * - getUnreadMessages: 获取未读消息
 * - getUnreadCount: 获取未读消息数量
 * - markAsRead: 标记单条消息为已读
 * - markAllAsRead: 标记所有消息为已读
 */
import request from '../utils/request'

export const messageApi = {
  /**
   * 获取用户消息列表
   * @returns {Promise}
   */
  getMessages() {
    return request({
      url: '/api/xiaoxi',
      method: 'get'
    })
  },

  /**
   * 获取未读消息
   * @returns {Promise}
   */
  getUnreadMessages() {
    return request({
      url: '/api/xiaoxi/weidu',
      method: 'get'
    })
  },

  /**
   * 获取未读消息数量
   * @returns {Promise}
   */
  getUnreadCount() {
    return request({
      url: '/api/xiaoxi/weidu/count',
      method: 'get'
    })
  },

  /**
   * 标记消息为已读
   * @param {Number} id - 消息ID
   * @returns {Promise}
   */
  markAsRead(id) {
    return request({
      url: `/api/xiaoxi/${id}/dushu`,
      method: 'put'
    })
  },

  /**
   * 标记所有消息为已读
   * @returns {Promise}
   */
  markAllAsRead() {
    return request({
      url: '/api/xiaoxi/quanbudushu',
      method: 'put'
    })
  }
}

export default messageApi
