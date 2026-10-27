/**
 * favorites.js - 收藏相关 API
 *
 * 与后端 ShouCangController 对应：
 * - GET    /api/favorites                获取收藏列表（返回 {content, total}）
 * - POST   /api/favorites?shangPinId=xx  添加收藏
 * - DELETE /api/favorites/{shangPinId}   取消收藏
 */
import request from '../utils/request'

export const favoriteApi = {
  /**
   * 获取收藏列表
   * @param {Object} params { page, size }
   */
  getFavorites(params = {}) {
    return request({
      url: '/api/favorites',
      method: 'get',
      params: { page: params.page ?? 0, size: params.size ?? 100 },
    })
  },

  /**
   * 添加收藏
   * @param {Number} shangPinId 商品ID
   */
  addFavorite(shangPinId) {
    return request({
      url: '/api/favorites',
      method: 'post',
      params: { shangPinId },
    })
  },

  /**
   * 取消收藏
   * @param {Number} shangPinId 商品ID
   */
  removeFavorite(shangPinId) {
    return request({
      url: `/api/favorites/${shangPinId}`,
      method: 'delete',
    })
  },
}

export default favoriteApi
