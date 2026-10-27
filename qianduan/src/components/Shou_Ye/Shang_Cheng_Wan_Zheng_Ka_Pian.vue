<template>
  <!--
    商城完整商品卡片组件
    展示单个商品的图片、信息等

    交互功能：
    - 点击卡片跳转到商品详情页
    - 鼠标悬停时卡片放大并显示阴影效果
    - 图片加载失败时的容错处理
  -->
  <div
    class="product-card"
    @click="handleClick"
    @mouseenter="isHovered = true"
    @mouseleave="isHovered = false"
  >
    <!--
      商品图片容器
      - :src动态绑定图片地址，优先使用传入的imageUrl，否则使用默认图片
      - :alt提供无障碍访问描述
      - @error监听图片加载失败事件
    -->
    <div class="product-image">
      <img
        v-if="displayImageUrl && !hasImageError"
        :src="displayImageUrl"
        :alt="props.product.mingCheng || `Product ${props.product.id}`"
        class="product-img"
        loading="lazy"
        @error="handleImageError"
      />
      <div v-else class="product-img product-img--empty">暂无图片</div>
    </div>

    <!-- 上部：价格 -->
    <div class="info-price">
      <span class="price-label">租金：</span>
      <span class="price-value">{{ props.product.zuJin || props.product.preRent || '--' }} 元</span>
    </div>

    <!-- 中部：6个信息项 -->
    <div class="info-grid">
      <div class="info-item">
        <span class="item-label">总资产</span>
        <span class="item-value"
          >{{ props.product.zongZiChan || props.product.warehouseAssets || '--' }}M</span
        >
      </div>
      <div class="info-item">
        <span class="item-label">纯币</span>
        <span class="item-value">{{ props.product.pureCoinM || '--' }}M</span>
      </div>
      <div class="info-item">
        <span class="item-label">比值</span>
        <span class="item-value"
          >{{ props.product.halfCoinRatio || props.product.ratio || '--' }}:1</span
        >
      </div>
      <div class="info-item">
        <span class="item-label">账号类型</span>
        <span class="item-value">{{ props.product.accountType || '--' }}</span>
      </div>
      <div class="info-item">
        <span class="item-label">区服</span>
        <span class="item-value">{{ props.product.quFu || props.product.server || '--' }}</span>
      </div>
      <div class="info-item">
        <span class="item-label">段位</span>
        <span class="item-value">{{ props.product.rank || '--' }}</span>
      </div>
    </div>

    <!-- 下部：标题 -->
    <div class="info-title">
      <h3 class="product-title">{{ props.product.mingCheng || '商品账号' }}</h3>
    </div>
  </div>
</template>

<script setup>
/**
 * Shang_Cheng_Wan_Zheng_Ka_Pian.vue - 商城商品卡片组件
 *
 * 该组件负责展示单个商品的卡片视图，包含：
 * - 商品封面图片展示
 * - 商品信息：名称、租金、账号类型、区服、段位
 * - 数据统计：总资产、纯币、哈夫币比值
 * - 悬停交互效果（放大、阴影、边框变色）
 * - 点击跳转商品详情页
 * - 图片加载失败的容错处理
 *
 * 接口字段说明（与数据库字段对应）：
 * - product.mingCheng: 商品名称 [String]
 * - product.zuJin / product.preRent: 租金 [Number]
 * - product.accountType: 账号类型 [String]
 * - product.quFu / product.server: 区服 [String]
 * - product.rank: 段位 [String]
 * - product.zongZiChan / product.warehouseAssets: 总资产 [String]
 * - product.pureCoinM: 纯币(M) [String]
 * - product.halfCoinRatio: 哈夫币比值 [String]
 * - product.coverImageUrl / product.tuPianUrl: 封面图URL [String]
 * - product.id: 商品ID [Number]
 */
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { pickImageUrl } from '../../utils/imageUrl'

/**
 * 定义组件接收的Props
 *
 * @property {Object} product - 商品数据对象（必填）
 *                              包含商品的所有信息
 */
const props = defineProps({
  product: {
    type: Object,
    required: true,
  },
})

/**
 * 获取Vue Router实例
 * 用于编程式导航到商品详情页
 */
const router = useRouter()

/**
 * 响应式状态：鼠标是否悬停在卡片上
 * 用于控制悬停时的样式变化
 */
const isHovered = ref(false)

/**
 * 响应式状态：图片是否加载失败
 * 用于错误处理和降级显示
 */
const hasImageError = ref(false)

/**
 * 计算属性：最终显示的图片URL
 * 图片由后端托管，统一用 pickImageUrl 解析
 * 优先级：coverImageUrl > tuPianUrl > imageUrl > images[0]
 */
const displayImageUrl = computed(() => pickImageUrl(props.product))

/**
 * 处理卡片点击事件
 * 跳转到商品详情页，使用商品ID作为路由参数
 */
const handleClick = () => {
  const productId = props.product.id || ''
  if (productId) {
    router.push(`/product/${productId}`)
  }
}

/**
 * 处理图片加载失败事件
 * 标记图片加载失败，使用默认图片降级显示
 */
const handleImageError = () => {
  hasImageError.value = true
}
</script>

<style scoped>
/**
 * 商品卡片容器样式
 * - 背景透明，由边框定义边界
 * - 边框1px solid ##F2F2F2，边框颜色
 * - 固定宽度350px
 * - padding: 16px，内容与边框间距
 * - border-radius: 8px，圆角设计
 * - cursor: pointer，鼠标悬停显示手型
 * - transition: all 0.25s ease，所有属性平滑过渡
 */
.product-card {
  background-color: transparent;
  border: 1px solid #dcdfe6;
  padding: 12px;
  width: 100%;
  max-width: 420px;
  box-sizing: border-box;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.25s ease;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

/**
 * 卡片悬停状态样式
 * - transform: scale(1.015)，轻微放大1.5%
 * - border-color: #d0d0d0，边框颜色加深
 * - box-shadow添加阴影，增强立体感
 */
.product-card:hover {
  transform: scale(1.015);
  border-color: #00b1fe;
  box-shadow: 0 4px 12px rgba(0, 177, 254, 0.15);
}

/**
 * 商品图片容器样式
 * - margin-bottom: 16px，与下方信息区域间距
 */
.product-image {
  margin-bottom: 16px;
}

/**
 * 商品图片样式
 * - width: 100%，填满容器宽度
 * - height: 162px，固定高度
 * - object-fit: cover，保持比例并裁剪填充
 * - background-color: #f5f5f5，加载前的背景色
 * - border: 1px solid #F2F2F2
 * - transition实现悬停时的透明度变化
 */
.product-img {
  width: 100%;
  height: 240px;
  object-fit: cover;
  background-color: #f5f5f5;
  border: 1px solid #dcdfe6;
  box-sizing: border-box;
  transition: all 0.25s ease;
}

/* 无图片时的占位（后端未提供图片或加载失败） */
.product-img--empty {
  display: flex;
  align-items: center;
  justify-content: center;
  color: #9aa0a6;
  font-size: 14px;
}

/**
 * 卡片悬停时图片样式
 * opacity: 0.9，略微降低透明度，配合卡片整体效果
 */
.product-card:hover .product-img {
  opacity: 0.9;
}

/* 上部：价格 */
.info-price {
  display: flex;
  align-items: center;
  padding: 6px 8px;
  margin: 0;
}

.price-label {
  font-size: 14px;
  color: #666;
}

.price-value {
  font-size: 16px;
  color: #00b1fe;
  font-weight: 600;
}

/* 下部：标题 */
.info-title {
  padding: 6px 8px;
  margin: 0;
}

.product-title {
  font-size: 14px;
  font-weight: 600;
  margin: 0;
  color: #333;
}

/* 中部：6个信息项 */
.info-grid {
  display: grid;
  grid-template-columns: repeat(6, 1fr);
  gap: 16px;
  padding: 0 4px;
}

.info-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 8px 4px;
  border-radius: 6px;
  min-height: 42px;
  white-space: nowrap;
}

.item-label {
  font-size: 12px;
  color: #666;
  margin-bottom: 4px;
}

.item-value {
  font-size: 14px;
  color: #333;
  font-weight: 600;
  text-align: center;
}
</style>
