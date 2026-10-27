/**
 * admin.js - 管理后台 API
 *
 * 全部接口走后端 /api/admin 单一入口（SecurityConfig 限制 ROLE_ADMIN）。
 * 请求经过 utils/request.js：自动带 Token，成功时已解开 { code, message, data } 信封，
 * 调用方拿到的直接就是 data。
 */
import request from '../utils/request'

export const adminApi = {
  /** 看板统计 */
  getDashboard() {
    return request({ url: '/api/admin/dashboard', method: 'get' })
  },

  /** 全部用户（密码字段后端已脱敏） */
  getUsers() {
    return request({ url: '/api/admin/users', method: 'get' })
  },

  /** 封禁 / 解封用户（status：0 正常，1 封禁） */
  banUser(id, status) {
    return request({ url: `/api/admin/users/${id}/ban`, method: 'put', params: { status } })
  },

  /** 修改用户角色（jueSe：0 用户 / 1 卖家 / 2 管理员） */
  updateRole(id, jueSe) {
    return request({ url: `/api/admin/users/${id}/roles`, method: 'put', params: { jueSe } })
  },

  /** 待审核商品列表 */
  getPendingProducts() {
    return request({ url: '/api/admin/products', method: 'get' })
  },

  /**
   * 全部商品（商品管理）
   * @param {{zhuangTai?: number, keyword?: string}} params
   */
  getAllProducts(params) {
    return request({ url: '/api/admin/products/all', method: 'get', params })
  },

  /** 审核商品（result：1 通过，其它值视为拒绝） */
  auditProduct(id, result) {
    return request({ url: `/api/admin/products/${id}/audit`, method: 'put', params: { result } })
  },

  /** 强制下架商品 */
  offlineProduct(id) {
    return request({ url: `/api/admin/products/${id}/offline`, method: 'put' })
  },

  /** 全部订单 */
  getOrders() {
    return request({ url: '/api/admin/orders', method: 'get' })
  },

  /** 纠纷中的订单 */
  getDisputes() {
    return request({ url: '/api/admin/disputes', method: 'get' })
  },

  /** 仲裁纠纷（订单置为已完成，商品重新在售） */
  judgeDispute(id) {
    return request({ url: `/api/admin/disputes/${id}/judge`, method: 'put' })
  },

  /** 风控风险事件日志（可按用户ID / 风险类型过滤） */
  getRiskLogs(params) {
    return request({ url: '/api/admin/risk/logs', method: 'get', params })
  },

  /** 管理员操作日志（最多 200 条） */
  getOperateLogs() {
    return request({ url: '/api/admin/operate/logs', method: 'get' })
  },

  /** 向全部用户发布站内通知 */
  publishNotice(title, content) {
    return request({ url: '/api/admin/notice', method: 'post', params: { title, content } })
  },

  // ==================== 轮播图管理 ====================

  /** 上传图片（通用接口，返回 { url, coverImageUrl, tuPianUrl }） */
  uploadImage(file) {
    const formData = new FormData()
    formData.append('file', file)
    return request({
      url: '/api/upload/image',
      method: 'post',
      data: formData,
      headers: { 'Content-Type': 'multipart/form-data' },
      timeout: 30000,
    })
  },

  /** 全部轮播图（含停用的） */
  getAllLunBoTu() {
    return request({ url: '/api/lunbotu/all', method: 'get' })
  },

  /** 新增轮播图（字段：tuPianUrl / tieTle / lianJieUrl / shunXu / zhuangTai） */
  createLunBoTu(data) {
    return request({ url: '/api/lunbotu', method: 'post', data })
  },

  /** 修改轮播图 */
  updateLunBoTu(id, data) {
    return request({ url: `/api/lunbotu/${id}`, method: 'put', data })
  },

  /** 删除轮播图（后端会同步清理磁盘图片） */
  deleteLunBoTu(id) {
    return request({ url: `/api/lunbotu/${id}`, method: 'delete' })
  },
}
