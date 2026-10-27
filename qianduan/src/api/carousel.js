/**
 * carousel.js - 轮播图相关API接口
 *
 * 该文件封装了轮播图相关的API调用，包含：
 * - getCarouselItems: 获取轮播图列表
 *
 */
import request from '../utils/request'

/**
 * 轮播图API对象
 * 包含所有轮播图相关的接口方法
 */
export const carouselApi = {
  /**
   * 获取轮播图列表
   * @returns {Promise} 轮播图数据数组
   */
  getCarouselItems() {
    return request({
      url: '/api/lunbotu',
      method: 'get'
    })
  }
}
