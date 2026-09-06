/**
 * order.js - 订单相关API接口
 *
 * 该文件封装了订单相关的API调用，包含：
 * - createOrder: 创建订单
 * - getMyOrders: 获取我的订单列表
 * - getOrderDetail: 获取订单详情
 * - payOrder: 支付订单
 * - cancelOrder: 取消订单
 * - confirmOrder: 确认收货
 * - returnOrder: 归还订单
 * - inspectStart: 发起验号
 * - getCredential: 获取登录凭证
 */
import request from '../utils/request'

export const orderApi = {
  /**
   * 创建订单（租赁）
   * @param {Object} data - { shangPinId, zuJieTianShu }
   * @returns {Promise}
   */
  createOrder(data) {
    return request({
      url: '/api/dingdan',
      method: 'post',
      data
    })
  },

  /**
   * 获取我的订单（作为买家）
   * @returns {Promise}
   */
  getMyOrders() {
    return request({
      url: '/api/dingdan',
      method: 'get'
    })
  },

  /**
   * 获取卖出的订单（作为卖家）
   * @param {Number} maiJiaId - 卖家用户ID
   * @returns {Promise}
   */
  getSellerOrders(maiJiaId) {
    return request({
      url: `/api/dingdan/maimai/${maiJiaId}`,
      method: 'get'
    })
  },

  /**
   * 获取订单详情
   * @param {String} dingDanHao - 订单号
   * @returns {Promise}
   */
  getOrderDetail(dingDanHao) {
    return request({
      url: `/api/dingdan/${dingDanHao}`,
      method: 'get'
    })
  },

  /**
   * 支付订单
   * @param {String} dingDanHao - 订单号
   * @returns {Promise}
   */
  payOrder(dingDanHao) {
    return request({
      url: `/api/dingdan/${dingDanHao}/fukuan`,
      method: 'post'
    })
  },

  /**
   * 取消订单
   * @param {String} dingDanHao - 订单号
   * @returns {Promise}
   */
  cancelOrder(dingDanHao) {
    return request({
      url: `/api/dingdan/${dingDanHao}/quxiao`,
      method: 'post'
    })
  },

  /**
   * 确认收货
   * @param {String} dingDanHao - 订单号
   * @returns {Promise}
   */
  confirmOrder(dingDanHao) {
    return request({
      url: `/api/dingdan/${dingDanHao}/confirm`,
      method: 'put'
    })
  },

  /**
   * 归还订单
   * @param {String} dingDanHao - 订单号
   * @returns {Promise}
   */
  returnOrder(dingDanHao) {
    return request({
      url: `/api/dingdan/${dingDanHao}/return`,
      method: 'put'
    })
  },

  /**
   * 发起验号
   * @param {String} dingDanHao - 订单号
   * @returns {Promise}
   */
  inspectStart(dingDanHao) {
    return request({
      url: `/api/dingdan/${dingDanHao}/inspect-start`,
      method: 'post'
    })
  },

  /**
   * 获取登录凭证（临时账号密码）
   * @param {String} dingDanHao - 订单号
   * @returns {Promise}
   */
  getCredential(dingDanHao) {
    return request({
      url: `/api/dingdan/${dingDanHao}/credential`,
      method: 'get'
    })
  }
}

export default orderApi
