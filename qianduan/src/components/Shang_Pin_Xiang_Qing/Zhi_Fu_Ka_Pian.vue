<template>
  <div class="pay-card">
    <div class="pay-card-head">订单结算</div>
    <div class="pay-card-body">
      <div class="price-row">
        <span>账号租金（{{ duration }}天）</span>
        <span>¥{{ rentPrice.toFixed(2) }}</span>
      </div>
      <div class="price-row">
        <span>账号押金</span>
        <span>¥{{ deposit.toFixed(2) }}</span>
      </div>
      <div class="deposit-tip">租期结束无违规自动退还</div>
      <div class="price-row total">
        <span>实付金额</span>
        <span class="total-price">
          <small>¥</small>
          <span>{{ totalPrice.toFixed(2) }}</span>
        </span>
      </div>

      <div class="pay-method-title">支付方式</div>
      <div class="pay-method-list">
        <div
          v-for="method in payMethods"
          :key="method.id"
          class="pay-method-item"
          :class="{ active: selectedPayMethod === method.id }"
          @click="selectedPayMethod = method.id"
        >
          <div class="pay-icon">
            <img :src="method.icon" :alt="method.name" />
          </div>
          <span class="pay-name">{{ method.name }}</span>
          <div class="radio-dot"></div>
        </div>
      </div>

      <button class="pay-btn" @click="handlePay">立即支付</button>
      <div class="protocol-tip">支付即表示同意 <a href="#">《用户租赁协议》</a></div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'

const props = defineProps({
  duration: {
    type: Number,
    default: 3,
  },
  rentPrice: {
    type: Number,
    default: 35,
  },
  deposit: {
    type: Number,
    default: 200,
  },
})

const selectedPayMethod = ref('wechat')

const payMethods = [
  {
    id: 'wechat',
    name: '微信支付',
    icon: new URL('../../../img/wechat-pay-line.svg', import.meta.url).href,
  },
  {
    id: 'alipay',
    name: '支付宝',
    icon: new URL('../../../img/alipay-line.svg', import.meta.url).href,
  },
]

const totalPrice = computed(() => props.rentPrice + props.deposit)

const emit = defineEmits(['pay'])

const handlePay = () => {
  const method = payMethods.find((m) => m.id === selectedPayMethod.value)
  emit('pay', {
    method: method.name,
    duration: props.duration,
    totalPrice: totalPrice.value,
  })
}
</script>

<style scoped>
.pay-card {
  width: 100%;
  height: 480px;
  background: #ffffff;
  border-radius: 8px;
  overflow: hidden;
  position: sticky;
  top: 0;
  padding: 20px 24px;
  box-sizing: border-box;
}

.pay-card-head {
  padding-bottom: 12px;
  border-bottom: 1px solid #f2f3f5;
  font-size: 16px;
  font-weight: 600;
  margin-bottom: 16px;
}

.pay-card-body {
  padding: 0;
}

.price-row {
  display: flex;
  justify-content: space-between;
  margin-bottom: 8px;
  font-size: 13px;
  color: #4e5969;
}

.price-row.total {
  padding-top: 8px;
  margin-top: 8px;
  border-top: 1px solid #f2f3f5;
  font-size: 13px;
  align-items: baseline;
}

.total-price {
  font-size: 24px;
  font-weight: 700;
  color: #f53f3f;
}

.total-price small {
  font-size: 14px;
  font-weight: normal;
}

.deposit-tip {
  font-size: 11px;
  color: #86909c;
  margin-top: -4px;
  margin-bottom: 8px;
  text-align: right;
}

/* 支付方式 */
.pay-method-title {
  font-size: 13px;
  font-weight: 500;
  margin: 12px 0 8px;
}

.pay-method-list {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.pay-method-item {
  padding: 10px 12px;
  border: 1px solid #e5e6eb;
  border-radius: 6px;
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  transition: all 0.2s;
  background: #fff;
}

.pay-method-item:hover {
  border-color: #00b1fe;
  background: #fafbfc;
}

.pay-method-item.active {
  border-color: #00b1fe;
  background: #f7faff;
}

.pay-icon {
  width: 24px;
  height: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.pay-icon img {
  width: 100%;
  height: 100%;
  object-fit: contain;
}

.pay-name {
  flex: 1;
  font-size: 13px;
}

.radio-dot {
  width: 16px;
  height: 16px;
  border: 2px solid #c9cdd4;
  border-radius: 50%;
  position: relative;
  transition: all 0.2s;
  flex-shrink: 0;
}

.pay-method-item.active .radio-dot {
  border-color: #00b1fe;
}

.radio-dot::after {
  content: '';
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%) scale(0);
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #00b1fe;
  transition: transform 0.2s;
}

.pay-method-item.active .radio-dot::after {
  transform: translate(-50%, -50%) scale(1);
}

/* 支付按钮 */
.pay-btn {
  width: 100%;
  height: 42px;
  margin-top: 14px;
  background: linear-gradient(135deg, #f53f3f 0%, #d63030 100%);
  color: #fff;
  border: none;
  border-radius: 6px;
  font-size: 15px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s;
  box-shadow: 0 4px 12px rgba(245, 63, 63, 0.3);
}

.pay-btn:hover {
  background: linear-gradient(135deg, #e63030 0%, #c72828 100%);
  box-shadow: 0 6px 16px rgba(245, 63, 63, 0.4);
  transform: translateY(-2px);
}

.pay-btn:active {
  transform: translateY(0);
}

.protocol-tip {
  font-size: 11px;
  color: #86909c;
  text-align: center;
  margin-top: 8px;
}

.protocol-tip a {
  color: #00b1fe;
}
</style>
