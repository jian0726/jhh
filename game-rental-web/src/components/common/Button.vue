<template>
  <!-- 
    通用按钮组件
    提供基础的按钮功能
  -->
  <button
    :type="type"
    :disabled="disabled"
    :class="['btn', { 'btn-disabled': disabled }]"
    @click="$emit('click')"
  >
    <slot></slot>
  </button>
</template>

<script setup>
/**
 * Button.vue - 通用按钮组件
 * 
 * 该组件提供基础的按钮功能，支持复用
 */
defineProps({
  type: {
    type: String,
    default: 'button'
  },
  disabled: {
    type: Boolean,
    default: false
  }
})

defineEmits(['click'])
</script>

<style scoped>
.btn {
  height: 44px;
  padding: 0 32px;
  background-color: #00B1FE;
  color: #fff;
  border: none;
  border-radius: 4px;
  font-size: 16px;
  font-family: "PingFang SC", "Microsoft YaHei", sans-serif;
  cursor: pointer;
  transition: background-color 0.2s ease;
}

.btn:hover:not(:disabled) {
  background-color: #0094d9;
}

.btn-disabled {
  background-color: #999;
  cursor: not-allowed;
}
</style>
