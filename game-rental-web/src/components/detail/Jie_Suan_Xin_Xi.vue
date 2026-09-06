<template>
  <div class="settlement-section">
    <h2 class="section-title">结算信息</h2>
    <div class="settlement-grid">
      <div class="settlement-item">
        <label>结算比例</label>
        <span>{{ halfCoinRatio || '-' }}</span>
      </div>
      <div class="settlement-item">
        <label>预收租金</label>
        <span class="price">¥ {{ preRent || '0.00' }}</span>
      </div>
      <div class="settlement-item">
        <label>押金</label>
        <span class="price">¥ {{ deposit || '0.00' }}</span>
      </div>
      <div class="settlement-item">
        <label>推荐租期</label>
        <span>{{ recommendedRentDays || '-' }} 天</span>
      </div>
    </div>
  </div>
</template>

<script setup>
defineProps({
  halfCoinRatio: String,
  preRent: String,
  deposit: String,
  recommendedRentDays: [String, Number]
})
</script>

<style scoped>
.settlement-section {
  background-color: #fff;
  border-radius: 12px;
  padding: 24px;
  margin-bottom: 24px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.section-title {
  font-size: 20px;
  font-weight: 600;
  color: #333;
  margin: 0 0 20px 0;
  padding-bottom: 12px;
  border-bottom: 1px solid #f0f0f0;
}

.settlement-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
}

.settlement-item {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.settlement-item label {
  font-size: 14px;
  color: #999;
}

.settlement-item span {
  font-size: 18px;
  color: #333;
  font-weight: 600;
}

.settlement-item span.price {
  color: #ff6b6b;
}
</style>
