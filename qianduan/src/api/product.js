/**
 * product.js - 商品相关API接口
 *
 * 该文件封装了商品相关的API调用，包含：
 * - getProducts: 获取商品列表（支持分页）
 * - getProductDetail: 获取商品详情
 * - searchProducts: 搜索商品
 * - uploadProduct: 上传/发布新商品
 * - uploadImage: 上传商品图片
 * - updateProduct: 更新商品信息
 * - deleteProduct: 删除商品
 *
 */
import request from '../utils/request'

/**
 * 商品API对象
 * 包含所有商品相关的接口方法
 */
export const productApi = {
  /**
   * 获取商品列表
   * @param {Object} params - 查询参数 { page, limit }
   * @returns {Promise} 商品列表数据
   */
  getProducts(params) {
    return request({
      url: '/api/shangpin',
      method: 'get',
      params,
    })
  },

  /**
   * 获取当前登录用户上架的商品（个人中心「我的商品」）
   * 后端通过请求头里的 JWT 识别用户身份
   * @param {Object} params 查询参数 { page, limit }
   * @returns {Promise} { data: [...], total }
   */
  getMyProducts(params = {}) {
    return request({
      url: '/api/shangpin/my',
      method: 'get',
      params,
    })
  },

  /**
   * 获取商品详情
   * @param {String|Number} id - 商品ID
   * @returns {Promise} 商品详情数据
   */
  getProductDetail(id) {
    return request({
      url: `/api/shangpin/${id}`,
      method: 'get',
    })
  },

  /**
   * 搜索商品
   * @param {String} keyword - 搜索关键词
   * @param {Object} params - 查询参数 { page, limit }
   * @returns {Promise} 搜索结果数据
   */
  searchProducts(keyword, params) {
    return request({
      url: `/api/shangpin/search`,
      method: 'get',
      params: { keyword, ...params },
    })
  },

  /**
   * 筛选商品
   * @param {Object} filterData - 筛选条件对象
   * @param {Object} params - 查询参数 { page, limit }
   * @returns {Promise} 筛选结果数据
   */
  filterProducts(filterData, params) {
    return request({
      url: '/api/shangpin/filter',
      method: 'post',
      data: { filter: filterData, ...params },
    })
  },

  /**
   * 上传/发布新商品
   * @param {Object} product - 商品信息对象
   * @returns {Promise} 发布结果
   */
  uploadProduct(product) {
    return request({
      url: '/api/shangpin',
      method: 'post',
      data: product,
    })
  },

  /**
   * 上传商品图片
   * @param {File|Blob} file - 图片文件
   * @returns {Promise} 上传结果 { coverImageUrl }
   */
  uploadImage(file) {
    const formData = new FormData()
    formData.append('image', file)
    return request({
      url: '/api/shangpin/upload-image',
      method: 'post',
      data: formData,
      headers: {
        'Content-Type': 'multipart/form-data',
      },
    })
  },

  /**
   * 更新商品信息
   * @param {String|Number} id - 商品ID
   * @param {Object} product - 更新后的商品信息
   * @returns {Promise} 更新结果
   */
  updateProduct(id, product) {
    return request({
      url: `/api/shangpin/${id}`,
      method: 'put',
      data: product,
    })
  },

  /**
   * 下架商品（软下架：后端把状态置为「已下架」，不删除数据）
   * @param {String|Number} id 商品ID
   */
  xiajiaProduct(id) {
    return request({
      url: `/api/shangpin/${id}/xiajia`,
      method: 'put',
    })
  },

  /**
   * 删除商品
   * @param {String|Number} id - 商品ID
   * @returns {Promise} 删除结果
   */
  deleteProduct(id) {
    return request({
      url: `/api/shangpin/${id}`,
      method: 'delete',
    })
  },
}

/**
 * 上架商品完整字段接口
 * 用于提交商品数据到后端
 *
 * @typedef {Object} ProductFormData
 * @property {String} accountType - 账号类型
 * @property {String} server - 区服（QQ/微信）
 * @property {String} settlementType - 结算方式
 * @property {String} banRecord - 封禁记录
 * @property {String} faceSelf - 人脸是否本人
 * @property {String} warehouseAssets - 仓库总资产
 * @property {String} pureCoinM - 哈夫克纯币(M)
 * @property {String} trainingLevel - 训练中心等级
 * @property {String} rangeLevel - 靶场等级
 * @property {String} safeBox - 安全箱
 * @property {String} rank - 段位
 * @property {String} accountLevel - 账号等级
 * @property {String} awmBullets - AWM子弹
 * @property {String} sixHead - 六头
 * @property {String} sixArmor - 六甲
 * @property {String} secretKD - 绝密KD
 * @property {Array<String>} knifeSkins - 刀皮
 * @property {Array<String>} operatorSkins - 干员皮
 * @property {Array<String>} weaponSkins - 武器皮
 * @property {String} ownerBanned - 号主禁止项
 * @property {String} ownerGifted - 号主赠送项
 * @property {String} remark - 备注
 * @property {String} personalTag - 个性标签（商品标题）
 * @property {String} tradeStartHour - 方便交易开始时间
 * @property {String} tradeEndHour - 方便交易结束时间
 * @property {String} selfCheck - 打完改密确认
 * @property {String} settlementRatio - 结算比例类型
 * @property {String} quickRatioValue - 快速比例值
 * @property {String} preRent - 预收租金
 * @property {String} deposit - 押金
 * @property {String} coverImageUrl - 封面图片URL
 */

export default productApi
