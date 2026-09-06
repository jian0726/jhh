<template>
  <div class="product-title-section">
    <h1 class="product-title">{{ personalTag || '商品标题' }}</h1>
    <div class="product-subtitle">
      <span class="tag">{{ accountType || '-' }}</span>
      <span class="tag">{{ server || '-' }}</span>
    </div>
  </div>
</template>

<script setup>
defineProps({
  personalTag: {
    type: String,
    default: ''
  },
  accountType: {
    type: String,
    default: ''
  },
  server: {
    type: String,
    default: ''
  }
})
</script>

<style scoped>
.product-title-section {
  margin-bottom: 32px;
}

.product-title {
  font-size: 28px;
  font-weight: 600;
  color: #333;
  margin: 0 0 16px 0;
}

.product-subtitle {
  display: flex;
  gap: 12px;
}

.tag {
  padding: 4px 12px;
  background-color: #f0f9ff;
  color: #00B1FE;
  border-radius: 4px;
  font-size: 14px;
}
</style>
