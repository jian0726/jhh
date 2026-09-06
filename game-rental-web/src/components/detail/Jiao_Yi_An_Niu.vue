<template>
  <div class="action-section">
    <el-button
      type="primary"
      size="large"
      class="rent-btn"
      :loading="renting"
      @click="handleRent"
    >
      {{ renting ? '下单中...' : '立即租赁' }}
    </el-button>
    <el-button
      size="large"
      class="collect-btn"
      :type="isFavorited ? 'danger' : 'default'"
      :loading="favLoading"
      @click="handleFavorite"
    >
      {{ isFavorited ? '已收藏' : '收藏商品' }}
    </el-button>
  </div>
</template>

<script setup>
import { ref, inject, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useAuthStore } from '../../stores/auth'
import { useUserStore } from '../../stores/user'
import { orderApi } from '../../api/order'
import { favoriteApi } from '../../api/favorites'

const authStore = useAuthStore()
const userStore = useUserStore()

// 从父组件注入获取商品ID的函数
const getProductId = inject('getProductId', null)
const productId = computed(() => (getProductId ? getProductId() : null))

const renting = ref(false)
const favLoading = ref(false)
const isFavorited = ref(false)

const handleRent = async () => {
  const id = productId.value
  if (!userStore.isLogin) {
    authStore.openLogin()
    return
  }
  if (!id) {
    ElMessage.warning('商品ID缺失')
    return
  }

  try {
    const { value: days } = await ElMessageBox.prompt('请输入租赁天数', '租赁', {
      inputValue: 1,
      inputPlaceholder: '最少1天',
      inputValidator: (val) => val > 0 || '请输入大于0的天数'
    })

    renting.value = true
    await orderApi.createOrder({
      shangPinId: id,
      zuJieTianShu: parseInt(days)
    })
    ElMessage.success('下单成功，请前往个人中心支付')
  } catch (err) {
    if (err !== 'cancel' && err !== 'close') {
      ElMessage.error(err.message || '下单失败')
    }
  } finally {
    renting.value = false
  }
}

const handleFavorite = async () => {
  const id = productId.value
  if (!userStore.isLogin) {
    authStore.openLogin()
    return
  }
  if (!id) {
    ElMessage.warning('商品ID缺失')
    return
  }

  favLoading.value = true
  try {
    if (isFavorited.value) {
      await favoriteApi.removeFavorite(id)
      isFavorited.value = false
      ElMessage.success('已取消收藏')
    } else {
      await favoriteApi.addFavorite(id)
      isFavorited.value = true
      ElMessage.success('收藏成功')
    }
  } catch (err) {
    ElMessage.error(err.message || '操作失败')
  } finally {
    favLoading.value = false
  }
}
</script>

<style scoped>
.action-section {
  display: flex;
  gap: 16px;
  padding: 24px;
  background-color: #fff;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.rent-btn {
  flex: 1;
  height: 48px;
  font-size: 18px;
  font-weight: 600;
}

.collect-btn {
  width: 140px;
  height: 48px;
  font-size: 16px;
}
</style>
