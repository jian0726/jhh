<template>
  <!--
    筛选结果页面
    包含顶部导航栏和筛选后的商品列表
  -->
  <div class="filter-page">
    <!-- 顶部导航栏 -->
    <Ding_Bu_Dao_Hang_Lan :isSearchExpanded="false" @toggleSearch="goHome" />

    <!-- 主内容区域 -->
    <main class="main-content">
      <!-- 筛选条件展示 -->
      <div v-if="hasFilters" class="filter-summary">
        <span class="summary-label">筛选条件：</span>
        <span class="summary-text">{{ filterSummaryText }}</span>
        <button class="clear-btn" @click="goHome">重新筛选</button>
      </div>

      <!-- 商品列表 -->
      <Shang_Cheng_Lie_Biao ref="productListRef" :filter-data="filterData" />
    </main>
  </div>
</template>

<script setup>
/**
 * Shang_Ping_Shai_Xuan.vue - 筛选结果页面
 *
 * 显示用户筛选后的商品列表
 */
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import Ding_Bu_Dao_Hang_Lan from './components/Shou_Ye/Ding_Bu_Dao_Hang_Lan.vue'
import Shang_Cheng_Lie_Biao from './components/Shou_Ye/Shang_Cheng_Lie_Biao.vue'

const router = useRouter()
const route = useRoute()
const productListRef = ref(null)

// 从路由 query 参数获取筛选数据
const filterData = computed(() => {
  try {
    const data = route.query.filters
    return data ? JSON.parse(decodeURIComponent(data)) : {}
  } catch {
    return {}
  }
})

// 是否有筛选条件
const hasFilters = computed(() => {
  return filterData.value && Object.keys(filterData.value).length > 0
})

// 筛选条件摘要文本
const filterSummaryText = computed(() => {
  const parts = []
  const data = filterData.value

  if (data.accountType?.length) parts.push(`账号类型: ${data.accountType.join('/')}`)
  if (data.accountLevel) parts.push(`等级: ${data.accountLevel}级`)
  if (data.training?.length) parts.push(`训练中心: ${data.training.join('/')}`)
  if (data.shootingRange?.length) parts.push(`靶场: ${data.shootingRange.join('/')}`)
  if (data.diveCenter?.length) parts.push(`潜水中心: ${data.diveCenter.join('/')}`)
  if (data.melee?.length) parts.push(`近战刀皮: ${data.melee.length}个`)
  if (data.agent?.length) parts.push(`干员外观: ${data.agent.length}个`)
  if (data.gunSkin?.length) parts.push(`典藏枪皮: ${data.gunSkin.length}个`)
  if (data.safeBox?.length) parts.push(`安全箱: ${data.safeBox.join('/')}`)
  if (data.rank?.length) parts.push(`段位: ${data.rank.join('/')}`)
  if (data.priceMin || data.priceMax) {
    const min = data.priceMin || '0'
    const max = data.priceMax || '不限'
    parts.push(`价格: ${min}-${max}元`)
  }

  return parts.join(' | ')
})

// 返回首页
const goHome = () => {
  router.push('/')
}

onMounted(() => {
  // 页面加载时自动刷新商品列表
  if (productListRef.value) {
    productListRef.value.refresh()
  }
})
</script>

<style scoped>
.filter-page {
  min-height: 100vh;
  padding-top: 120px;
  background-color: #ffffff;
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

.main-content {
  padding: 20px 0;
  min-height: calc(100vh - 140px);
  background-color: #ffffff;
}

.filter-summary {
  max-width: 1800px;
  margin: 0 auto 20px;
  padding: 12px 24px;
  background: #f7f8fa;
  border-radius: 8px;
  display: flex;
  align-items: center;
  gap: 12px;
}

.summary-label {
  font-size: 14px;
  color: #666;
  white-space: nowrap;
}

.summary-text {
  font-size: 14px;
  color: #333;
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.clear-btn {
  padding: 6px 16px;
  background: #00b1fe;
  color: #fff;
  border: none;
  border-radius: 6px;
  font-size: 14px;
  cursor: pointer;
  white-space: nowrap;
  transition: background 0.2s;
}

.clear-btn:hover {
  background: #009de0;
}
</style>
