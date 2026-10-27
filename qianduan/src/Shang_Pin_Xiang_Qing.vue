<template>
  <div class="product-detail">
    <!-- 顶部导航栏 -->
    <Ding_Bu_Dao_Hang_Lan :isSearchExpanded="false" />

    <!-- 网格布局的主体容器 -->
    <div class="page-container">
      <!-- 第一行：商品基本信息（占满整行） -->
      <div class="grid-item item-product-info">
        <div class="info-wrapper">
          <Shang_Pin_Xin_Xi :product="productInfo" />
          <button class="favorite-btn" :class="{ active: isFavorited }" @click="toggleFavorite">
            <img :src="isFavorited ? iconStarActive : iconStar" alt="收藏" class="fav-icon" />
            <span>{{ isFavorited ? '已收藏' : '收藏' }}</span>
          </button>
        </div>
      </div>

      <!-- 第二行左侧：租期选择 -->
      <div class="grid-item item-duration">
        <Zu_Qi_Xuan_Ze
          :initialDuration="selectedDuration"
          @duration-change="handleDurationChange"
        />
      </div>

      <!-- 第二行右侧：图片展示区 -->
      <div class="grid-item item-image">
        <Tu_Pian_Zhan_Shi :images="gallerySource" />
      </div>

      <!-- 第三行：商品属性（占满整行） -->
      <div class="grid-item item-product-attr">
        <Shang_Pin_Shu_Xing :product="productData" />
      </div>

      <!-- 第四行左侧：租用说明 -->
      <div class="grid-item item-notice">
        <Zu_Yong_Shuo_Ming />
      </div>

      <!-- 第四行右侧：支付卡片 -->
      <div class="grid-item item-pay">
        <Zhi_Fu_Ka_Pian
          :duration="selectedDuration"
          :rentPrice="rentPrice"
          :deposit="deposit"
          @pay="handlePay"
        />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { productApi } from './api/product'
import { favoriteApi } from './api/favorites'
import { orderApi } from './api/order'
import { collectImageUrls } from './utils/imageUrl'
import { useUserStore } from './stores/user'
import { useAuthStore } from './stores/auth'
import iconStar from '../img/star-line.svg'
import iconStarActive from '../img/star-line-blue.svg'
import Ding_Bu_Dao_Hang_Lan from './components/Shou_Ye/Ding_Bu_Dao_Hang_Lan.vue'
import Tu_Pian_Zhan_Shi from './components/Shang_Pin_Xiang_Qing/Tu_Pian_Zhan_Shi.vue'
import Shang_Pin_Xin_Xi from './components/Shang_Pin_Xiang_Qing/Shang_Pin_Xin_Xi.vue'
import Shang_Pin_Shu_Xing from './components/Shang_Pin_Xiang_Qing/Shang_Pin_Shu_Xing.vue'
import Zu_Qi_Xuan_Ze from './components/Shang_Pin_Xiang_Qing/Zu_Qi_Xuan_Ze.vue'
import Zu_Yong_Shuo_Ming from './components/Shang_Pin_Xiang_Qing/Zu_Yong_Shuo_Ming.vue'
import Zhi_Fu_Ka_Pian from './components/Shang_Pin_Xiang_Qing/Zhi_Fu_Ka_Pian.vue'

// 商品数据（实际应该从API获取）
const productData = ref({
  title: '满级60级全干员丨12款近战皮肤丨3×3安全箱',
  tags: ['纯币号', '黑鹰段位', '满配成品号', '秒上号'],
  description:
    '官方认证优质卖家，所有账号均人工审核，账号信息与描述一致，售后全程保障，租号期间遇到任何问题可随时联系客服处理。',
  seller: '官方认证店',
  rentCount: 328,
  rating: 98.6,

  // 上架时填写的所有信息
  accountType: '纯币号',
  server: 'QQ',
  settlementType: '哈夫币必须打完',
  banRecord: '无封禁记录',
  faceSelf: '是',
  warehouseAssets: '25',
  pureCoinM: '15',
  halfCoinRatio: '38',
  trainingLevel: '6',
  rangeLevel: '6',
  safeBox: '顶级安全箱3*3',
  rank: '黑鹰',
  accountLevel: '60',
  awmBullets: '500',
  sixHead: '2',
  sixArmor: '3',
  secretKD: '2.5',
  knifeSkins: ['北极星', '暗星', '坠星者', '信条'],
  operatorSkins: ['红狼-蚀金玫瑰', '威龙-蛟龙特战队', '露娜-黑天际线'],
  weaponSkins: ['KC17造物纪元', 'QBZ95王牌之剑', 'M250电玩S2'],
  ownerBanned: '禁止使用外挂',
  ownerGifted: '赠送1000哈夫币',
  remark: '账号优质，可放心租用',
  personalTag: '优质卖家',
  tradeStartHour: '09',
  tradeEndHour: '22',
  extraItemCharge: '需要计费',
  awmBulletPrice: 5,
  sixHeadPrice: 20,
  sixArmorPrice: 15,
})

// 商品基本信息（用于 Shang_Pin_Xin_Xi 组件）
const productInfo = computed(() => ({
  title: productData.value.title,
  tags: productData.value.tags,
  description: productData.value.remark || productData.value.description,
  seller: productData.value.seller,
  rentCount: productData.value.rentCount,
  rating: productData.value.rating,
}))

// 租期相关
const selectedDuration = ref(3)
const rentPrice = ref(35)
const deposit = ref(200)

const handleDurationChange = (days) => {
  selectedDuration.value = days
  // 租期不影响价格，价格固定
}

// 路由参数
const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const authStore = useAuthStore()

// ========== 收藏 ==========
const isFavorited = ref(false)

/** 拉取当前用户的收藏列表，判断本商品是否已收藏 */
const loadFavoriteState = async () => {
  if (!userStore.isLogin || !route.params.id) return
  try {
    const res = await favoriteApi.getFavorites({ page: 0, size: 200 })
    const list = Array.isArray(res) ? res : (res?.content || res?.data || [])
    isFavorited.value = list.some((item) => String(item.id) === String(route.params.id))
  } catch (error) {
    console.warn('获取收藏状态失败：', error)
  }
}

/** 收藏 / 取消收藏（未登录先弹登录框） */
const toggleFavorite = async () => {
  if (!userStore.isLogin) {
    authStore.openLogin(() => toggleFavorite())
    ElMessage.info('请先登录后再收藏')
    return
  }
  const shangPinId = Number(route.params.id)
  if (!shangPinId) {
    ElMessage.error('商品信息缺失，无法收藏')
    return
  }
  try {
    if (isFavorited.value) {
      await favoriteApi.removeFavorite(shangPinId)
      isFavorited.value = false
      ElMessage.success('已取消收藏')
    } else {
      await favoriteApi.addFavorite(shangPinId)
      isFavorited.value = true
      ElMessage.success('收藏成功')
    }
  } catch (error) {
    ElMessage.error(error?.message || '操作失败')
  }
}

/**
 * 下单：调用后端创建订单（订单初始状态为「待支付」），
 * 创建成功后跳到个人中心的买家订单页，由用户在那边完成支付。
 */
const handlePay = async (payInfo) => {
  if (!userStore.isLogin) {
    authStore.openLogin(() => handlePay(payInfo))
    ElMessage.info('请先登录后再下单')
    return
  }
  const shangPinId = Number(route.params.id)
  if (!shangPinId) {
    ElMessage.error('商品信息缺失，无法下单')
    return
  }

  try {
    await ElMessageBox.confirm(
      `租期：${payInfo.duration} 天\n应付金额：¥${Number(payInfo.totalPrice).toFixed(2)}（含押金）\n支付方式：${payInfo.method}`,
      '确认创建订单',
      { type: 'info', confirmButtonText: '创建订单', cancelButtonText: '再想想' },
    )
  } catch {
    return // 用户取消
  }

  try {
    await orderApi.createOrder({ shangPinId, zuJieTianShu: payInfo.duration })
    ElMessage.success('订单创建成功，请在个人中心完成支付')
    router.push({ path: '/personal', query: { tab: 'order-buyer' } })
  } catch (error) {
    ElMessage.error(error?.message || '下单失败')
  }
}

// 后端返回的原始商品详情（图片等字段从这里取）
const detailData = ref(null)

/**
 * 图片展示区的数据源。
 * 后端托管图片：优先使用详情接口返回的 coverImageUrl / tuPianUrl / images，
 * 解析后交给 Tu_Pian_Zhan_Shi 渲染。
 */
const gallerySource = computed(() => {
  const source = detailData.value
    ? { ...productData.value, ...detailData.value }
    : productData.value
  return collectImageUrls(source)
})

/**
 * 用后端返回的真实商品数据覆盖示例数据
 * 仅覆盖后端确实返回的字段，缺失字段保留页面默认值，保证页面始终可渲染
 */
const applyProductDetail = (data) => {
  if (!data) return
  detailData.value = data
  const p = productData.value
  p.title = data.biaoTi || data.mingCheng || data.personalTag || p.title
  p.description = data.remark || data.miaoShu || p.description
  p.seller = data.seller || p.seller

  const fields = [
    'accountType',
    'server',
    'settlementType',
    'banRecord',
    'faceSelf',
    'warehouseAssets',
    'pureCoinM',
    'halfCoinRatio',
    'trainingLevel',
    'rangeLevel',
    'safeBox',
    'rank',
    'accountLevel',
    'awmBullets',
    'sixHead',
    'sixArmor',
    'secretKD',
    'knifeSkins',
    'operatorSkins',
    'weaponSkins',
    'ownerBanned',
    'ownerGifted',
    'remark',
    'personalTag',
    'tradeStartHour',
    'tradeEndHour',
    'extraItemCharge',
  ]
  fields.forEach((key) => {
    const value = data[key]
    if (value !== undefined && value !== null && value !== '') {
      p[key] = value
    }
  })

  const rent = Number(data.preRent ?? data.zuJin ?? data.riZuJiaGe)
  if (!Number.isNaN(rent) && rent > 0) {
    rentPrice.value = rent
  }
  const dep = Number(data.deposit)
  if (!Number.isNaN(dep) && dep > 0) {
    deposit.value = dep
  }
}

onMounted(async () => {
  const id = route.params.id
  if (!id) return
  try {
    const data = await productApi.getProductDetail(id)
    if (data && !data.message) {
      applyProductDetail(data)
    }
  } catch (error) {
    console.warn('加载商品详情失败，展示示例数据：', error)
  }
  // 已登录则回显收藏状态
  loadFavoriteState()
})

// 通过弹窗登录成功后，补一次收藏状态查询
watch(
  () => userStore.isLogin,
  (logged) => {
    if (logged) loadFavoriteState()
  },
)
</script>

<style scoped>
.product-detail {
  min-height: 100vh;
  background-color: #f5f5f5;
  font-family:
    'PingFang SC',
    'Microsoft YaHei',
    -apple-system,
    BlinkMacSystemFont,
    'Segoe UI',
    Roboto,
    'Helvetica Neue',
    Arial,
    sans-serif;
}

/* ========== 页面主体 - 网格布局 ========== */
.page-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 120px 20px 20px;
  display: grid;
  grid-template-columns: 280px 1fr;
  grid-template-rows: auto;
  gap: 20px;
}

/* 商品信息：占满整行 */
.item-product-info {
  grid-column: 1 / -1;
}

/* 商品信息 + 收藏按钮 */
.info-wrapper {
  position: relative;
}

.favorite-btn {
  position: absolute;
  top: 20px;
  right: 24px;
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 16px;
  font-size: 14px;
  font-family: inherit;
  color: #666;
  background: #fff;
  border: 1px solid #dcdfe6;
  border-radius: 20px;
  cursor: pointer;
  transition: all 0.2s;
}

.favorite-btn:hover {
  color: #00b1fe;
  border-color: #00b1fe;
  background: #f0f9ff;
}

.favorite-btn.active {
  color: #00b1fe;
  border-color: #00b1fe;
  background: #ecf5ff;
}

.fav-icon {
  width: 18px;
  height: 18px;
}

@media (max-width: 768px) {
  .favorite-btn {
    top: 12px;
    right: 12px;
    padding: 6px 12px;
    font-size: 13px;
  }
}

/* 商品属性：占满整行 */
.item-product-attr {
  grid-column: 1 / -1;
}

/* 租期选择：左侧 */
.item-duration {
  grid-column: 1 / 2;
}

/* 图片展示：右侧 */
.item-image {
  grid-column: 2 / -1;
}

/* 租用说明：左侧 */
.item-notice {
  grid-column: 1 / 2;
}

/* 支付卡片：右侧 */
.item-pay {
  grid-column: 2 / -1;
}
</style>
