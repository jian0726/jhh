/**
 * order.js - 订单相关 API
 *
 * 与后端 DingDanController 对应：
 * - POST /api/dingdan                          创建订单
 * - GET  /api/dingdan                          我的订单（我是买家）
 * - GET  /api/dingdan/maimai/{maiJiaId}        我卖出的订单（我是卖家）
 * - GET  /api/dingdan/{dingDanHao}             订单详情
 * - POST /api/dingdan/{dingDanHao}/fukuan      支付
 * - POST /api/dingdan/{dingDanHao}/quxiao      取消
 * - PUT  /api/dingdan/{dingDanHao}/confirm     确认收货
 * - PUT  /api/dingdan/{dingDanHao}/return      归还
 * - POST /api/dingdan/{dingDanHao}/inspect-start  发起验号
 * - GET  /api/dingdan/{dingDanHao}/credential  获取临时登录凭证
 */
import request from '../utils/request'

export const orderApi = {
  /**
   * 创建订单（租赁）
   * @param {Object} data { shangPinId, zuJieTianShu }
   */
  createOrder(data) {
    return request({
      url: '/api/dingdan',
      method: 'post',
      data,
    })
  },

  /** 我的订单（我是买家） */
  getMyOrders() {
    return request({
      url: '/api/dingdan',
      method: 'get',
    })
  },

  /**
   * 我卖出的订单（我是卖家）
   * @param {Number} maiJiaId 卖家用户ID
   */
  getSellerOrders(maiJiaId) {
    return request({
      url: `/api/dingdan/maimai/${maiJiaId}`,
      method: 'get',
    })
  },

  /**
   * 订单详情
   * @param {String} dingDanHao 订单号
   */
  getOrderDetail(dingDanHao) {
    return request({
      url: `/api/dingdan/${dingDanHao}`,
      method: 'get',
    })
  },

  /**
   * 支付订单
   * @param {String} dingDanHao 订单号
   */
  payOrder(dingDanHao) {
    return request({
      url: `/api/dingdan/${dingDanHao}/fukuan`,
      method: 'post',
    })
  },

  /**
   * 取消订单
   * @param {String} dingDanHao 订单号
   */
  cancelOrder(dingDanHao) {
    return request({
      url: `/api/dingdan/${dingDanHao}/quxiao`,
      method: 'post',
    })
  },

  /**
   * 确认收货
   * @param {String} dingDanHao 订单号
   */
  confirmOrder(dingDanHao) {
    return request({
      url: `/api/dingdan/${dingDanHao}/confirm`,
      method: 'put',
    })
  },

  /**
   * 归还订单
   * @param {String} dingDanHao 订单号
   */
  returnOrder(dingDanHao) {
    return request({
      url: `/api/dingdan/${dingDanHao}/return`,
      method: 'put',
    })
  },

  /**
   * 发起验号
   * @param {String} dingDanHao 订单号
   */
  inspectStart(dingDanHao) {
    return request({
      url: `/api/dingdan/${dingDanHao}/inspect-start`,
      method: 'post',
    })
  },

  /**
   * 获取登录凭证（临时账号密码）
   * @param {String} dingDanHao 订单号
   */
  getCredential(dingDanHao) {
    return request({
      url: `/api/dingdan/${dingDanHao}/credential`,
      method: 'get',
    })
  },
}

export default orderApi
