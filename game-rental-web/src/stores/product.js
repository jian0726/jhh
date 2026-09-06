/**
 * product.js - 商品状态管理
 *
 * 使用 Pinia 管理商品相关状态，包含：
 * - 商品列表状态
 * - 商品详情状态
 * - 上架表单状态
 * - 加载状态
 *
 * 使用方式：
 * import { useProductStore } from '@/stores/product'
 * const store = useProductStore()
 */
import { defineStore } from 'pinia'
import { productApi } from '../api/product'

export const useProductStore = defineStore('product', {
  state: () => ({
    /**
     * 商品列表
     */
    productList: [],
    
    /**
     * 商品总数
     */
    total: 0,
    
    /**
     * 当前页码
     */
    currentPage: 1,
    
    /**
     * 每页数量
     */
    pageSize: 10,
    
    /**
     * 商品详情
     */
    productDetail: null,
    
    /**
     * 上架表单数据
     */
    shelfFormData: {
      // 账号基本信息
      accountType: '纯币号',
      server: '',
      settlementType: '',
      banRecord: '',
      faceSelf: '',
      
      // 资产信息
      warehouseAssets: '',
      pureCoinM: '',
      trainingLevel: '',
      rangeLevel: '',
      safeBox: '',
      
      // 装备信息
      rank: '',
      accountLevel: '',
      awmBullets: '',
      sixHead: '',
      sixArmor: '',
      secretKD: '',
      
      // 皮肤信息
      knifeSkins: [],
      operatorSkins: [],
      weaponSkins: [],
      
      // 其他信息
      ownerBanned: '',
      ownerGifted: '',
      remark: '',
      personalTag: '',
      tradeStartHour: '',
      tradeEndHour: '',
      selfCheck: '',
      
      // 结算信息
      settlementRatio: 'quick',
      quickRatioValue: '',
      
      // 图片
      coverImageUrl: ''
    },
    
    /**
     * 结算信息
     */
    settlementInfo: {
      settlementRatio: 'quick',
      preRent: '',
      deposit: '',
      halfCoinRatio: ''
    },
    
    /**
     * 是否正在加载
     */
    loading: false,
    
    /**
     * 是否正在提交
     */
    submitting: false,
    
    /**
     * 错误信息
     */
    error: null
  }),
  
  getters: {
    /**
     * 计算预收租金
     */
    calculatedPreRent: (state) => {
      const pureCoinM = parseFloat(state.shelfFormData.pureCoinM) || 0
      const pureCoin = pureCoinM * 100
      if (pureCoin === 0) return '0.00'
      
      if (state.shelfFormData.settlementRatio === 'quick') {
        const ratio = parseFloat(state.shelfFormData.quickRatioValue) || 0
        return ratio > 0 ? (pureCoin / ratio).toFixed(2) : '0.00'
      } else {
        const trainingLevel = parseInt(state.shelfFormData.trainingLevel) || 0
        const standardRatio = (trainingLevel === 7 && state.shelfFormData.safeBox === '顶级安全箱3*3') ? 38 : 35
        return (pureCoin / standardRatio).toFixed(2)
      }
    },
    
    /**
     * 计算押金
     */
    calculatedDeposit: (state) => {
      const trainingLevel = parseInt(state.shelfFormData.trainingLevel) || 0
      const standardRatio = (trainingLevel === 7 && state.shelfFormData.safeBox === '顶级安全箱3*3') ? 38 : 35
      return (standardRatio * 10).toString()
    },
    
    /**
     * 计算哈夫币比值
     */
    calculatedHalfCoinRatio: (state) => {
      if (state.shelfFormData.settlementRatio === 'quick') {
        return state.shelfFormData.quickRatioValue 
          ? `${state.shelfFormData.quickRatioValue}万哈夫币/资产` 
          : ''
      } else {
        const trainingLevel = parseInt(state.shelfFormData.trainingLevel) || 0
        const standardRatio = (trainingLevel === 7 && state.shelfFormData.safeBox === '顶级安全箱3*3') ? 38 : 35
        return `${standardRatio}万哈夫币/资产`
      }
    },
    
    /**
     * 推荐租期
     */
    recommendedRentDays: (state) => {
      const pureCoinM = parseFloat(state.shelfFormData.pureCoinM) || 0
      const pureCoin = pureCoinM * 100
      if (pureCoin === 0) return null
      return pureCoin < 100000000 ? 7 : 14
    },
    
    /**
     * 是否有图片
     */
    hasImage: (state) => !!state.shelfFormData.coverImageUrl,
    
    /**
     * 是否可以提交
     */
    canSubmit: (state) => {
      // 验证必填字段
      const { accountType, server, settlementType, banRecord, faceSelf } = state.shelfFormData
      const { settlementRatio, quickRatioValue } = state.shelfFormData
      const { selfCheck, coverImageUrl } = state.shelfFormData
      
      return (
        accountType &&
        server &&
        settlementType &&
        banRecord &&
        faceSelf &&
        selfCheck &&
        coverImageUrl &&
        (settlementRatio === 'standard' || quickRatioValue)
      )
    }
  },
  
  actions: {
    /**
     * 获取商品列表
     */
    async fetchProducts(params = {}) {
      this.loading = true
      this.error = null
      try {
        const { page = this.currentPage, limit = this.pageSize } = params
        const result = await productApi.getProducts({ page, limit })
        this.productList = result.data || []
        this.total = result.total || 0
        this.currentPage = page
        this.pageSize = limit
      } catch (error) {
        this.error = error.message || '获取商品列表失败'
        console.error('获取商品列表失败:', error)
      } finally {
        this.loading = false
      }
    },
    
    /**
     * 获取商品详情
     */
    async fetchProductDetail(id) {
      this.loading = true
      this.error = null
      try {
        const result = await productApi.getProductDetail(id)
        if (result && !result.message) {
          this.productDetail = result
        } else {
          this.error = result.message || '商品不存在'
        }
      } catch (error) {
        this.error = error.message || '获取商品详情失败'
        console.error('获取商品详情失败:', error)
      } finally {
        this.loading = false
      }
    },
    
    /**
     * 清空商品详情（路由切换时避免闪现旧数据）
     */
    clearDetail() {
      this.productDetail = null
      this.error = null
      this.loading = false
    },

    /**
     * 搜索商品
     */
    async searchProducts(keyword, params = {}) {
      this.loading = true
      this.error = null
      try {
        const { page = 1, limit = this.pageSize } = params
        const result = await productApi.searchProducts(keyword, { page, limit })
        this.productList = result.data || []
        this.total = result.total || 0
        this.currentPage = page
      } catch (error) {
        this.error = error.message || '搜索商品失败'
        console.error('搜索商品失败:', error)
      } finally {
        this.loading = false
      }
    },
    
    /**
     * 上传商品图片
     */
    async uploadImage(file) {
      try {
        const result = await productApi.uploadImage(file)
        if (result && result.coverImageUrl) {
          this.shelfFormData.coverImageUrl = result.coverImageUrl
          return result.coverImageUrl
        }
        throw new Error('上传图片失败')
      } catch (error) {
        console.error('上传图片失败:', error)
        throw error
      }
    },
    
    /**
     * 更新表单数据
     */
    updateFormData(data) {
      this.shelfFormData = { ...this.shelfFormData, ...data }
    },
    
    /**
     * 更新结算信息
     */
    updateSettlementInfo(data) {
      this.settlementInfo = { ...this.settlementInfo, ...data }
    },
    
    /**
     * 重置表单
     */
    resetForm() {
      this.shelfFormData = {
        accountType: '纯币号',
        server: '',
        settlementType: '',
        banRecord: '',
        faceSelf: '',
        warehouseAssets: '',
        pureCoinM: '',
        trainingLevel: '',
        rangeLevel: '',
        safeBox: '',
        rank: '',
        accountLevel: '',
        awmBullets: '',
        sixHead: '',
        sixArmor: '',
        secretKD: '',
        knifeSkins: [],
        operatorSkins: [],
        weaponSkins: [],
        ownerBanned: '',
        ownerGifted: '',
        remark: '',
        personalTag: '',
        tradeStartHour: '',
        tradeEndHour: '',
        selfCheck: '',
        settlementRatio: 'quick',
        quickRatioValue: '',
        coverImageUrl: ''
      }
      this.settlementInfo = {
        settlementRatio: 'quick',
        preRent: '',
        deposit: '',
        halfCoinRatio: ''
      }
    },
    
    /**
     * 提交商品
     */
    async submitProduct() {
      if (!this.canSubmit) {
        throw new Error('请填写完整信息')
      }
      
      this.submitting = true
      this.error = null
      
      try {
        // 构建完整的商品数据
        const productData = {
          ...this.shelfFormData,
          preRent: this.calculatedPreRent,
          deposit: this.calculatedDeposit,
          halfCoinRatio: this.calculatedHalfCoinRatio
        }
        
        const result = await productApi.uploadProduct(productData)
        
        // 如果有图片，先上传图片再提交
        if (productData.coverImageUrl && !productData.coverImageUrl.startsWith('http')) {
          // Base64图片会在提交时一起发送
        }
        
        // 重置表单
        this.resetForm()
        
        // 刷新商品列表
        await this.fetchProducts()
        
        return result
      } catch (error) {
        this.error = error.message || '提交商品失败'
        throw error
      } finally {
        this.submitting = false
      }
    },
    
    /**
     * 删除商品
     */
    async deleteProduct(id) {
      try {
        await productApi.deleteProduct(id)
        await this.fetchProducts()
      } catch (error) {
        console.error('删除商品失败:', error)
        throw error
      }
    }
  }
})

export default useProductStore
