<!-- Shang_Cheng_Lie_Biao.vue 优化 -->
<template>
  <!--
    商品列表容器
    包含商品网格、加载提示等元素

    功能说明：
    - 使用CSS Grid布局展示商品卡片
    - 支持无限滚动加载更多内容
    - 显示加载状态和"没有更多"提示
  -->
  <div class="product-list">
    <!--
      商品网格容器
      使用v-for循环渲染商品卡片组件

      特性：
      - displayedProducts: 当前已加载的商品列表
      - :key="index": 使用索引作为key（生产环境建议使用唯一ID）
      - animationDelay:  staggered动画效果，每个卡片延迟0.1s出现
    -->
    <div class="product-grid">
      <Shang_Cheng_Wan_Zheng_Ka_Pian
        v-for="(product, index) in displayedProducts"
        :key="product.id || index"
        :product="product"
        :style="{ animationDelay: `${index * 0.08}s` }"
      />
    </div>

    <!--
      加载提示组件
      - 第一个实例：正在加载更多时显示"加载中..."
      - 第二个实例：没有更多商品时显示"没有更多了"
    -->
    <Jia_Zai_Ti_Shi :loading="isLoading" :show="hasMore && isLoading" />
    <Jia_Zai_Ti_Shi :loading="false" :show="!hasMore && displayedProducts.length > 0" />
  </div>
</template>

<script setup>
/**
 * Shang_Cheng_Lie_Biao.vue - 商城商品列表组件
 *
 * 该组件负责展示商品列表，实现以下功能：
 * - 分页加载商品数据
 * - 无限滚动：接近页面底部时自动加载更多内容
 * - 商品卡片入场动画
 * - 响应式布局：适配不同屏幕尺寸
 *
 * 性能优化：
 * - 使用setTimeout模拟异步加载，避免阻塞UI
 * - 防抖处理：isLoading和hasMore标志防止重复加载
 */
import { ref, onMounted, onUnmounted } from 'vue'
import Shang_Cheng_Wan_Zheng_Ka_Pian from './Shang_Cheng_Wan_Zheng_Ka_Pian.vue'
import Jia_Zai_Ti_Shi from './Jia_Zai_Ti_Shi.vue'
import { productApi } from '../../api/product'

/**
 * 定义组件接收的Props
 *
 * @property {Number} totalProducts - 商品总数，默认50个
 * @property {Number} initialLoad - 首次加载数量，默认16个
 * @property {Number} loadMoreCount - 每次加载更多数量，默认16个
 * @property {String} searchKeyword - 搜索关键词
 */
const props = defineProps({
  totalProducts: {
    type: Number,
    default: 50,
  },
  initialLoad: {
    type: Number,
    default: 16,
  },
  loadMoreCount: {
    type: Number,
    default: 16,
  },
  searchKeyword: {
    type: String,
    default: '',
  },
  filterData: {
    type: Object,
    default: () => ({}),
  },
})

/**
 * 响应式状态定义
 */
// 当前已展示的商品列表数组
const displayedProducts = ref([])

// 是否还有更多商品可加载
const hasMore = ref(true)

// 是否正在加载中，用于防止重复请求
const isLoading = ref(false)

// 当前页码
const currentPage = ref(1)

/**
 * 初始化商品数据
 *
 * 功能：
 * - 根据是否有搜索关键词调用不同的API
 * - 检查是否还有更多商品，更新hasMore状态
 */
const initProducts = async () => {
  isLoading.value = true
  try {
    let response
    if (props.filterData && Object.keys(props.filterData).length > 0) {
      // 有筛选条件时调用筛选接口
      response = await productApi.filterProducts(props.filterData, {
        page: currentPage.value,
        limit: props.initialLoad,
      })
    } else if (props.searchKeyword) {
      response = await productApi.searchProducts(props.searchKeyword, {
        page: currentPage.value,
        limit: props.initialLoad,
      })
    } else {
      response = await productApi.getProducts({
        page: currentPage.value,
        limit: props.initialLoad,
      })
    }
    const data = response.data || response
    displayedProducts.value = data
    hasMore.value = data.length >= props.initialLoad
  } catch (error) {
    console.error('加载商品列表失败:', error)
    displayedProducts.value = []
    hasMore.value = false
  }
  isLoading.value = false
}

/**
 * 加载更多商品
 *
 * 逻辑流程：
 * 1. 检查是否正在加载或已无更多商品，若是则直接返回
 * 2. 设置isLoading为true，显示加载状态
 * 3. 根据是否有搜索关键词调用不同的API
 * 4. 将新商品添加到displayedProducts数组
 * 5. 检查是否还有更多商品，更新hasMore状态
 * 6. 设置isLoading为false，隐藏加载状态
 */
const loadMoreProducts = async () => {
  if (isLoading.value || !hasMore.value) return

  isLoading.value = true
  currentPage.value++

  try {
    let response
    if (props.filterData && Object.keys(props.filterData).length > 0) {
      response = await productApi.filterProducts(props.filterData, {
        page: currentPage.value,
        limit: props.loadMoreCount,
      })
    } else if (props.searchKeyword) {
      response = await productApi.searchProducts(props.searchKeyword, {
        page: currentPage.value,
        limit: props.loadMoreCount,
      })
    } else {
      response = await productApi.getProducts({
        page: currentPage.value,
        limit: props.loadMoreCount,
      })
    }
    const data = response.data || response

    if (data.length === 0) {
      hasMore.value = false
    } else {
      displayedProducts.value = [...displayedProducts.value, ...data]
      hasMore.value = data.length >= props.loadMoreCount
    }
  } catch (error) {
    console.error('加载更多商品失败:', error)
    currentPage.value--
    hasMore.value = false
  }

  isLoading.value = false
}

/**
 * 刷新商品列表
 * 重置页码并重新加载数据
 */
const refresh = () => {
  currentPage.value = 1
  displayedProducts.value = []
  hasMore.value = true
  initProducts()
}

// 暴露refresh方法供父组件调用
defineExpose({ refresh })

/**
 * 滚动事件处理函数
 *
 * 实现无限滚动：
 * - 监听window滚动事件
 * - 当滚动到距离底部600px时触发加载
 * - 防止在加载中或无更多商品时重复触发
 *
 * 计算公式：
 * scrollTop + windowHeight >= documentHeight - 600
 */
const handleScroll = () => {
  if (!hasMore.value || isLoading.value) return

  const scrollTop = window.pageYOffset || document.documentElement.scrollTop
  const windowHeight = window.innerHeight || document.documentElement.clientHeight
  const documentHeight = document.documentElement.scrollHeight

  if (scrollTop + windowHeight >= documentHeight - 600) {
    loadMoreProducts()
  }
}

/**
 * 组件挂载生命周期钩子
 *
 * 执行顺序：
 * 1. 初始化商品数据
 * 2. 添加滚动事件监听器
 */
onMounted(() => {
  initProducts()
  window.addEventListener('scroll', handleScroll)
})

/**
 * 组件卸载生命周期钩子
 *
 * 清理工作：
 * - 移除滚动事件监听器，防止内存泄漏
 */
onUnmounted(() => {
  window.removeEventListener('scroll', handleScroll)
})
</script>

<style scoped>
/* 容器内边距：给左右稍留白，但不要过大 */
.product-list {
  padding: 8px 24px;
  box-sizing: border-box;
}

/* 大屏：固定 4 列，增大列间距，网格占满父容器宽度 */
.product-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(320px, 1fr));
  column-gap: 24px;
  row-gap: 40px;
  width: 100%;
  max-width: 1800px;
  margin: 0 auto;
  justify-items: stretch;
  align-items: start;
}

/* 入场动画（保留原动画） */
@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(20px);
  }

  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* 作用于子组件根元素的样式（保持动画） */
:deep(.product-card) {
  animation: fadeInUp 0.4s ease-out forwards;
  opacity: 0;
}

/* 中等屏：3 列 */
@media (max-width: 1400px) {
  .product-grid {
    grid-template-columns: repeat(3, minmax(200px, 1fr));
    column-gap: 36px;
  }
}

/* 原有响应性断点：1024px */
@media (max-width: 1024px) {
  .product-grid {
    grid-template-columns: repeat(auto-fill, minmax(250px, 1fr));
    gap: 36px;
  }
}

@media (max-width: 768px) {
  .product-list {
    padding: 16px;
  }

  .product-grid {
    grid-template-columns: repeat(auto-fill, minmax(140px, 1fr));
    gap: 28px;
  }
}

@media (max-width: 480px) {
  .product-list {
    padding: 12px;
  }

  .product-grid {
    grid-template-columns: repeat(2, 1fr);
    gap: 16px;
  }
}
</style>
