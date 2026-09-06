<template>
  <!--
    商城完整商品卡片组件
    展示单个商品的图片、信息等

    交互功能：
    - 点击卡片跳转到商品详情页
    - 鼠标悬停时卡片放大并显示阴影效果
    - 图片加载失败时的容错处理
  -->
  <div class="product-card" @click="handleClick" @mouseenter="isHovered = true" @mouseleave="isHovered = false">
    <!--
      商品图片容器
      - :src动态绑定图片地址，优先使用传入的imageUrl，否则使用默认图片
      - :alt提供无障碍访问描述
      - @error监听图片加载失败事件
    -->
    <div class="product-image">
      <img
        v-if="!hasImageError"
        :src="displayImageUrl"
        :alt="props.product.mingCheng || `Product ${props.product.id}`"
        class="product-img"
        loading="lazy"
        @error="handleImageError"
      />
      <div v-else class="image-fallback">
        <el-icon :size="36" color="#bbb"><Picture /></el-icon>
        <span>暂无图片</span>
      </div>
    </div>

    <div class="product-info">
      <div class="info-header">
        <h3 class="product-title">{{ props.product.mingCheng || '商品账号' }}</h3>
        <div class="product-price">租金：{{ props.product.zuJin || props.product.preRent || props.product.riZuJiaGe || '--' }} 元</div>
      </div>

      <div class="info-tags">
        <span class="info-tag">账号类型：{{ props.product.accountType || '--' }}</span>
        <span class="info-tag">区服：{{ props.product.quFu || props.product.server || '--' }}</span>
        <span class="info-tag">段位：{{ props.product.rank || '--' }}</span>
      </div>

      <div class="info-stats">
        <div class="stat-item">
          <span class="stat-label">总资产</span>
          <span class="stat-value">{{ props.product.zongZiChan || props.product.warehouseAssets || '--' }}</span>
        </div>
        <div class="stat-item">
          <span class="stat-label">纯币</span>
          <span class="stat-value">{{ props.product.pureCoinM || '--' }}M</span>
        </div>
        <div class="stat-item">
          <span class="stat-label">哈夫币比值</span>
          <span class="stat-value">{{ props.product.halfCoinRatio || '--' }}</span>
        </div>
      </div>
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
import { Picture } from '@element-plus/icons-vue'
import { resolveImageUrl } from '@/utils/request'

/**
 * 定义组件接收的Props
 *
 * @property {Object} product - 商品数据对象（必填）
 *                              包含商品的所有信息
 */
const props = defineProps({
  product: {
    type: Object,
    required: true
  }
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
 * 默认图片地址
 * 当未传入图片地址或图片加载失败时使用
 */
const defaultImageUrl = '/img/34cf94222f2f47437cda3b0c61e9462e.png'

/**
 * 计算属性：最终显示的图片URL
 * 优先级：coverImageUrl > tuPianUrl > defaultImageUrl
 */
const displayImageUrl = computed(() => {
  const url = props.product.coverImageUrl || props.product.tuPianUrl
  if (url) {
    return resolveImageUrl(url)
  }
  return defaultImageUrl
})

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
  border: 1px solid #555555;
  padding: 16px;
  width: 100%;
  max-width: 450px;
  box-sizing: border-box;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.25s ease;
}

/**
 * 卡片悬停状态样式
 * - transform: scale(1.015)，轻微放大1.5%
 * - border-color: #d0d0d0，边框颜色加深
 * - box-shadow添加阴影，增强立体感
 */
.product-card:hover {
  transform: scale(1.015);
  border-color: #00B1FE;
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
  height: 220px;
  object-fit: cover;
  background-color: #f5f5f5;
  border: 1px solid #F2F2F2;
  box-sizing: border-box;
  transition: all 0.25s ease;
}

/**
 * 卡片悬停时图片样式
 */
.product-card:hover .product-img {
  opacity: 0.9;
}

/**
 * 图片加载失败占位
 */
.image-fallback {
  width: 100%;
  height: 220px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 8px;
  background: #f5f5f5;
  border-radius: 4px;
  color: #999;
  font-size: 13px;
}

/**
 * 商品信息区域样式
 * - border: 1px solid #F2F2F2，边框颜色
 * - padding: 12px，内边距
 * - min-height: 40px，最小高度保证
 * - border-radius: 4px，小圆角
 * - transition实现悬停时的边框颜色变化
 */
.product-info {
  border: 1px solid #555555;
  padding: 16px;
  min-height: 40px;
  border-radius: 8px;
  transition: all 0.2s ease;
}

.product-title {
  font-size: 18px;
  font-weight: 700;
  margin: 0;
  color: #222;
}

.product-price {
  font-size: 16px;
  color: #00B1FE;
  font-weight: 600;
}

.info-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 16px;
  margin-bottom: 12px;
}

.info-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-bottom: 14px;
}

.info-tag {
  display: inline-flex;
  align-items: center;
  padding: 6px 12px;
  background: #f5f7fb;
  border-radius: 999px;
  font-size: 12px;
  color: #444;
  border: 1px solid #ebeef5;
}

.info-stats {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 12px;
}

.stat-item {
  padding: 12px;
  background: #fafafa;
  border-radius: 8px;
  border: 1px solid #ebeef5;
  text-align: center;
}

.stat-label {
  display: block;
  font-size: 12px;
  color: #888;
  margin-bottom: 6px;
}

.stat-value {
  font-size: 16px;
  color: #222;
  font-weight: 600;
}

/**
 * 卡片悬停时信息区域样式
 * border-color: #d0d0d0，边框颜色加深，与卡片一致
 */
.product-card:hover .product-info {
  border-color: #00B1FE;
}
</style>
