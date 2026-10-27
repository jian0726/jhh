<template>
  <div class="duration-selector">
    <div class="duration-title">选择租期</div>
    <div class="duration-list">
      <div
        v-for="duration in durations"
        :key="duration.days"
        class="duration-item"
        :class="{ active: selectedDuration === duration.days }"
        @click="selectDuration(duration.days)"
      >
        {{ duration.days }}天
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'

const props = defineProps({
  initialDuration: {
    type: Number,
    default: 3,
  },
})

const emit = defineEmits(['duration-change'])

const selectedDuration = ref(props.initialDuration)

// 租期选项
const durations = [
  { days: 1, price: 12.8 },
  { days: 3, price: 35 },
  { days: 7, price: 78 },
  { days: 15, price: 158 },
  { days: 30, price: 298 },
]

const selectDuration = (days) => {
  selectedDuration.value = days
  emit('duration-change', days)
}
</script>

<style scoped>
.duration-selector {
  background: #ffffff;
  border-radius: 8px;
  padding: 20px 24px;
  box-sizing: border-box;
}

.duration-title {
  font-size: 16px;
  font-weight: 600;
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 1px solid #f2f3f5;
}

.duration-list {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.duration-item {
  padding: 10px 22px;
  border: 1px solid #e5e6eb;
  border-radius: 6px;
  cursor: pointer;
  text-align: center;
  transition: all 0.2s;
  min-width: 70px;
  font-size: 14px;
  background: #fff;
}

.duration-item:hover {
  border-color: #00b1fe;
  color: #00b1fe;
  transform: translateY(-2px);
  box-shadow: 0 4px 8px rgba(22, 93, 255, 0.15);
}

.duration-item.active {
  border-color: #00b1fe;
  background: #f2f6ff;
  color: #00b1fe;
  font-weight: 500;
}
</style>
