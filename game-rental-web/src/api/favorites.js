/**
 * favorites.js - 收藏相关API接口
 *
 * 该文件封装了商品收藏相关的API调用，包含：
 * - getFavorites: 获取收藏列表
 * - addFavorite: 添加收藏
 * - removeFavorite: 取消收藏
 */
import request from '../utils/request'

export const favoriteApi = {
  /**
   * 获取收藏列表
   * @param {Object} params - { page, size }
   * @returns {Promise} 收藏商品列表
   */
  getFavorites(params = {}) {
    return request({
      url: '/api/favorites',
      method: 'get',
      params: { page: params.page || 0, size: params.size || 10 }
    })
  },

  /**
   * 添加收藏
   * @param {Number} shangPinId - 商品ID
   * @returns {Promise}
   */
  addFavorite(shangPinId) {
    return request({
      url: '/api/favorites',
      method: 'post',
      params: { shangPinId }
    })
  },

  /**
   * 取消收藏
   * @param {Number} shangPinId - 商品ID
   * @returns {Promise}
   */
  removeFavorite(shangPinId) {
    return request({
      url: `/api/favorites/${shangPinId}`,
      method: 'delete'
    })
  }
}

export default favoriteApi
