<template>
  <div class="product-detail">
    <!-- 顶部导航栏 -->
    <Ding_Bu_Dao_Hang_Lan
      :isSearchExpanded="isSearchExpanded"
      @toggleSearch="toggleSearch"
    />

    <!-- 搜索面板 -->
    <Sou_Suo_Mian_Ban
      :isExpanded="isSearchExpanded"
      @search="handleSearch"
    />

    <!-- 详情内容区域 -->
    <main class="detail-content" @click="closeSearch">
      <!-- 加载状态 -->
      <div v-if="loading" class="loading-container">
        <el-icon class="loading-icon" :size="40"><Loading /></el-icon>
        <span>加载中...</span>
      </div>

      <!-- 错误状态 -->
      <div v-else-if="error" class="error-container">
        <p class="error-message">{{ error }}</p>
        <el-button @click="fetchDetail">重试</el-button>
      </div>

      <!-- 数据为空兜底 -->
      <div v-else-if="!productData" class="loading-container">
        <el-icon class="loading-icon" :size="40"><Loading /></el-icon>
        <span>加载中...</span>
      </div>

      <!-- 内容区域 -->
      <template v-else>
        <!-- 左右布局容器 -->
        <div class="detail-layout">
          <!-- 左侧：商品图片 -->
          <Shang_Pin_Tu_Pian :cover-image-url="productData.coverImageUrl" />

          <!-- 右侧：商品信息（可滚动） -->
          <div class="right-panel">
            <div class="scrollable-content">
              <!-- 商品标题 -->
              <Shang_Pin_Biao_Ti
                :personal-tag="productData.personalTag"
                :account-type="productData.accountType"
                :server="productData.server"
              />

              <!-- 商品信息 -->
              <Shang_Pin_Xin_Xi
                :account-type="productData.accountType"
                :server="productData.server"
                :settlement-type="productData.settlementType"
                :ban-record="productData.banRecord"
                :face-self="productData.faceSelf"
                :warehouse-assets="productData.warehouseAssets"
                :pure-coin-m="productData.pureCoinM"
                :training-level="productData.trainingLevel"
                :range-level="productData.rangeLevel"
                :safe-box="productData.safeBox"
                :rank="productData.rank"
                :account-level="productData.accountLevel"
                :awm-bullets="productData.awmBullets"
                :six-head="productData.sixHead"
                :six-armor="productData.sixArmor"
                :secret-kd="productData.secretKD"
                :knife-skins="productData.knifeSkins"
                :operator-skins="productData.operatorSkins"
                :weapon-skins="productData.weaponSkins"
                :owner-banned="productData.ownerBanned"
                :owner-gifted="productData.ownerGifted"
                :remark="productData.remark"
                :trade-start-hour="productData.tradeStartHour"
                :trade-end-hour="productData.tradeEndHour"
              />

              <!-- 结算信息 -->
              <Jie_Suan_Xin_Xi
                :half-coin-ratio="productData.halfCoinRatio"
                :pre-rent="productData.preRent"
                :deposit="productData.deposit"
                :recommended-rent-days="productData.recommendedRentDays"
              />

              <!-- 交易按钮 -->
              <Jiao_Yi_An_Niu />
            </div>
          </div>
        </div>
      </template>
    </main>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch, provide } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Loading } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import Ding_Bu_Dao_Hang_Lan from './components/home/Ding_Bu_Dao_Hang_Lan.vue'
import Sou_Suo_Mian_Ban from './components/home/Sou_Suo_Mian_Ban.vue'
import Shang_Pin_Tu_Pian from './components/detail/Shang_Pin_Tu_Pian.vue'
import Shang_Pin_Biao_Ti from './components/detail/Shang_Pin_Biao_Ti.vue'
import Shang_Pin_Xin_Xi from './components/detail/Shang_Pin_Xin_Xi.vue'
import Jie_Suan_Xin_Xi from './components/detail/Jie_Suan_Xin_Xi.vue'
import Jiao_Yi_An_Niu from './components/detail/Jiao_Yi_An_Niu.vue'
import { useProductStore } from './stores/product'

// 获取路由和路由参数
const route = useRoute()
const router = useRouter()

// 向下传递商品ID获取函数给交易按钮等子组件
provide('getProductId', () => Number(route.params.id))

// 使用 Pinia Store
const productStore = useProductStore()

// 搜索面板状态
const isSearchExpanded = ref(false)

// 计算属性：加载状态
const loading = computed(() => productStore.loading)

// 计算属性：错误信息
const error = computed(() => productStore.error)

// 计算属性：商品数据
const productData = computed(() => productStore.productDetail)

// 获取商品详情
const fetchDetail = async () => {
  const productId = route.params.id
  if (productId) {
    await productStore.fetchProductDetail(productId)
    if (productStore.error) {
      ElMessage.error(productStore.error)
    }
  }
}

// 切换搜索面板
const toggleSearch = () => {
  isSearchExpanded.value = !isSearchExpanded.value
}

// 关闭搜索面板
const closeSearch = () => {
  isSearchExpanded.value = false
}

// 处理搜索
const handleSearch = (keyword) => {
  if (keyword) {
    router.push({ path: '/', query: { search: keyword } })
  }
}

// 监听路由参数变化（组件复用时响应参数变化）
watch(() => route.params.id, (newId) => {
  if (newId) {
    // 切换商品时先清空旧数据，避免闪现上一商品内容
    productStore.clearDetail()
    fetchDetail()
  }
})

// 组件挂载时获取数据（首次进入）
onMounted(() => {
  const productId = route.params.id
  if (productId) {
    productStore.clearDetail()
    fetchDetail()
  }
})
</script>

<style scoped>
.product-detail {
  min-height: 100vh;
  background-color: #f5f5f5;
  font-family: "PingFang SC", "Microsoft YaHei", -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
}

.detail-content {
  padding-top: 140px;
  min-height: calc(100vh - 140px);
}

/* 加载状态 */
.loading-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 400px;
  gap: 16px;
  color: #666;
}

.loading-icon {
  animation: rotate 1s linear infinite;
}

@keyframes rotate {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

/* 错误状态 */
.error-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 400px;
  gap: 16px;
}

.error-message {
  color: #f56c6c;
  font-size: 16px;
}

.detail-layout {
  display: flex;
  gap: 32px;
  padding: 32px;
  max-width: 1400px;
  margin: 0 auto;
}

/* 右侧信息区域 */
.right-panel {
  flex: 1;
  min-width: 0;
  max-height: calc(100vh - 200px);
}

.scrollable-content {
  height: 100%;
  max-height: calc(100vh - 200px);
  overflow-y: auto;
  overflow-x: hidden;
  padding-right: 16px;
}

/* 滚动条样式 */
.scrollable-content::-webkit-scrollbar {
  width: 6px;
}

.scrollable-content::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 3px;
}

.scrollable-content::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 3px;
}

.scrollable-content::-webkit-scrollbar-thumb:hover {
  background: #a1a1a1;
}

/* 响应式 */
@media (max-width: 1024px) {
  .detail-layout {
    flex-direction: column;
  }
}

@media (max-width: 768px) {
  .detail-content {
    padding-top: 100px;
  }
}

@media (max-width: 480px) {
  .detail-content {
    padding-top: 80px;
  }

  .detail-layout {
    padding: 16px;
    gap: 20px;
  }
}
</style>
