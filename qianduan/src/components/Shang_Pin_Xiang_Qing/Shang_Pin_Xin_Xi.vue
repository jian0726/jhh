<template>
  <div class="product-info">
    <h1 class="product-name">{{ product.title }}</h1>
    <div class="product-tags">
      <span v-for="(tag, index) in product.tags" :key="index" class="product-tag">
        {{ tag }}
      </span>
    </div>
    <div class="product-description">
      {{ product.description }}
    </div>
    <div class="seller-info">
      <span>卖家：{{ product.seller }}</span>
      <span>已出租 {{ product.rentCount }} 次</span>
      <span>好评率 {{ product.rating }}%</span>
    </div>
  </div>
</template>

<script setup>
defineProps({
  product: {
    type: Object,
    required: true,
    default: () => ({
      title: '',
      tags: [],
      description: '',
      seller: '',
      rentCount: 0,
      rating: 0,
    }),
  },
})
</script>

<style scoped>
.product-info {
  background: #ffffff;
  border-radius: 8px;
  padding: 24px;
}

.product-name {
  font-size: 20px;
  font-weight: 600;
  margin-bottom: 12px;
  line-height: 1.4;
  color: #1d2129;
}

.product-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 16px;
}

.product-tag {
  padding: 4px 12px;
  background: #f7f8fa;
  color: #00b1fe;
  font-size: 13px;
  border-radius: 4px;
  font-weight: 500;
}

.product-description {
  font-size: 14px;
  color: #4e5969;
  line-height: 1.8;
  padding: 16px;
  background: #f7f8fa;
  border-radius: 6px;
}

.seller-info {
  font-size: 13px;
  color: #86909c;
  display: flex;
  align-items: center;
  gap: 16px;
  margin-top: 16px;
  padding-top: 16px;
  border-top: 1px solid #f2f3f5;
}
</style>
