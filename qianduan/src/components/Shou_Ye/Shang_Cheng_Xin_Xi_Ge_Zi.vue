<template>
  <div class="info-grid">
    <div class="info-item">
      <span class="item-label">总资产</span>
      <span class="item-value">{{ product.zongZiChan || product.warehouseAssets || '--' }}</span>
    </div>
    <div class="info-item">
      <span class="item-label">纯币</span>
      <span class="item-value">{{ product.pureCoinM || '--' }}M</span>
    </div>
    <div class="info-item">
      <span class="item-label">比值</span>
      <span class="item-value">{{ product.halfCoinRatio || product.ratio || '--' }}</span>
    </div>
    <div class="info-item">
      <span class="item-label">账号类型</span>
      <span class="item-value">{{ product.accountType || '--' }}</span>
    </div>
    <div class="info-item">
      <span class="item-label">区服</span>
      <span class="item-value">{{ product.quFu || product.server || '--' }}</span>
    </div>
    <div class="info-item">
      <span class="item-label">段位</span>
      <span class="item-value">{{ product.rank || '--' }}</span>
    </div>
  </div>
</template>

<script setup>
/**
 * Shang_Cheng_Xin_Xi_Ge_Zi.vue - 商城商品信息网格组件
 *
 * 展示商品的6个核心信息项：总资产、纯币、比值、账号类型、区服、段位
 */
defineProps({
  product: {
    type: Object,
    required: true,
  },
})
</script>

<style scoped>
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
