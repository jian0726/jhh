<template>
  <!--
    首页交易平台主容器
    包含顶部导航栏、搜索面板和商品列表三个主要部分
  -->
  <div class="trading-platform">
    <!--
      顶部导航栏组件
      - isSearchExpanded: 控制搜索面板是否展开
      - carouselIndex: 当前轮播图索引，用于控制导航栏渐变颜色
      - @toggleSearch: 切换搜索面板显示状态的事件处理
    -->
    <Ding_Bu_Dao_Hang
      :isSearchExpanded="isSearchExpanded"
      :carouselIndex="currentCarouselIndex"
      @toggleSearch="toggleSearch"
    />

    <!--
      搜索面板组件
      - isExpanded: 控制搜索面板的展开/收起状态
      - @search: 用户执行搜索时触发的事件
      - @close: 点击外部区域时触发的事件
    -->
    <Sou_Suo_Mian_Ban :isExpanded="isSearchExpanded" @search="handleSearch" @close="closeSearch" />

    <!--
      轮播图组件
      展示商品促销信息或广告内容
    -->
    <Lun_Bo_Tu @change="handleCarouselChange" />

    <!--
      间距占位盒子
      增加轮播图与按钮区域之间的距离
    -->
    <div class="spacing-box"></div>

    <!--
      功能按钮区域
      位于轮播图和商品列表之间
    -->
    <Gong_Neng_An_Niu />
    <el-divider />
    <!--
      主内容区域
      点击主内容区域时会关闭搜索面板
      包含商城商品列表组件
    -->
    <main class="main-content" @click="closeSearch">
      <Shang_Cheng_Lie_Biao ref="productListRef" :search-keyword="searchKeyword" />
    </main>
  </div>
</template>

<script setup>
/**
 * HomePage.vue - 首页组件
 *
 * 这是应用的主页面，整合了以下功能模块：
 * - 顶部导航栏（Ding_Bu_Dao_Hang）
 * - 搜索面板（Sou_Suo_Mian_Ban）
 * - 商品列表（Shang_Cheng_Lie_Biao）
 *
 * 主要职责：
 * - 管理搜索面板的展开/收起状态
 * - 协调各子组件之间的交互
 * - 处理用户搜索操作
 */
import { ref } from 'vue'

// 导入子组件
import Ding_Bu_Dao_Hang from './components/Shou_Ye/Ding_Bu_Dao_Hang.vue'
import Sou_Suo_Mian_Ban from './components/Shou_Ye/Sou_Suo_Mian_Ban.vue'
import Lun_Bo_Tu from './components/Shou_Ye/Lun_Bo_Tu.vue'
import Gong_Neng_An_Niu from './components/Shou_Ye/Gong_Neng_An_Niu.vue'
import Shang_Cheng_Lie_Biao from './components/Shou_Ye/Shang_Cheng_Lie_Biao.vue'
import { productApi } from './api/product'

/**
 * 响应式状态：搜索面板是否展开
 * false表示收起，true表示展开
 */
const isSearchExpanded = ref(false)

/**
 * 当前搜索关键词
 */
const searchKeyword = ref('')

/**
 * 商品列表组件引用
 */
const productListRef = ref(null)

/**
 * 当前轮播图索引
 */
const currentCarouselIndex = ref(0)

/**
 * 处理轮播图切换事件
 */
const handleCarouselChange = (index) => {
  currentCarouselIndex.value = index
}

/**
 * 切换搜索面板的展开/收起状态
 * 由顶部导航栏的搜索按钮触发
 */
const toggleSearch = () => {
  isSearchExpanded.value = !isSearchExpanded.value
}

/**
 * 关闭搜索面板
 * 当用户点击主内容区域时调用，提升用户体验
 */
const closeSearch = () => {
  isSearchExpanded.value = false
}

/**
 * 处理搜索事件
 * @param {string} keyword - 用户输入的搜索关键词
 */
const handleSearch = async (keyword) => {
  searchKeyword.value = keyword
  closeSearch()

  try {
    const response = await productApi.searchProducts(keyword)
    console.log('搜索结果:', response)

    // 如果商品列表组件有刷新方法，则调用它
    if (productListRef.value && typeof productListRef.value.refresh === 'function') {
      productListRef.value.refresh()
    }
  } catch (error) {
    console.error('搜索失败:', error)
    alert('搜索失败: ' + (error.response?.data?.message || error.message))
  }
}
</script>

<style scoped>
/**
 * 重置所有元素的默认样式
 * 使用通配符选择器确保边距和内边距为0
 * box-sizing设置为border-box，使元素宽度包含padding和border
 */
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

/**
 * 首页容器样式
 * - 最小高度 100vh，占满整个视口
 * - 背景色白色
 * - 使用苹方字体，中文显示更清晰
 */
.trading-platform {
  min-height: 100vh;
  padding: 120px 20px 20px;
  background-color: #ffffffff;
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

/**
 * 轮播图与功能按钮之间的间距盒子
 * - 作为占位元素，确保两个区域之间有稳定距离
 */
.spacing-box {
  width: 100%;
  height: 40px;
}

/**
 * 主内容区域样式
 * - padding-top: 40px，与功能按钮区域和其他模块保持一致
 * - min-height确保内容区域至少占满剩余视口高度
 * - background-color: #FFFFFF，设置独立背景色，避免渐变背景影响商品列表
 */
.main-content {
  padding-top: 40px;
  min-height: calc(100vh - 140px);
  background-color: #ffffff;
}
</style>
