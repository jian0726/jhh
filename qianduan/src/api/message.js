/**
 * message.js - 消息中心 API
 *
 * 与后端 XiaoXiController 对应：
 * - GET /api/xiaoxi                 用户消息列表
 * - GET /api/xiaoxi/weidu           未读消息
 * - GET /api/xiaoxi/weidu/count     未读数量
 * - PUT /api/xiaoxi/{id}/dushu      单条标记已读
 * - PUT /api/xiaoxi/quanbudushu     全部标记已读
 *
 * 后端已在 Controller 层把字段转换为前端契约名（shiFouDuGuo / faSongShiJian）。
 */
import request from '../utils/request'

export const messageApi = {
  /** 用户消息列表 */
  getMessages() {
    return request({
      url: '/api/xiaoxi',
      method: 'get',
    })
  },

  /** 未读消息 */
  getUnreadMessages() {
    return request({
      url: '/api/xiaoxi/weidu',
      method: 'get',
    })
  },

  /** 未读消息数量 */
  getUnreadCount() {
    return request({
      url: '/api/xiaoxi/weidu/count',
      method: 'get',
    })
  },

  /**
   * 标记单条消息为已读
   * @param {Number} id 消息ID
   */
  markAsRead(id) {
    return request({
      url: `/api/xiaoxi/${id}/dushu`,
      method: 'put',
    })
  },

  /** 全部标记为已读 */
  markAllAsRead() {
    return request({
      url: '/api/xiaoxi/quanbudushu',
      method: 'put',
    })
  },
}

export default messageApi
