<template>
  <!-- 
    通用输入框组件
    提供基础的输入框功能，支持多种类型
  -->
  <div class="input-wrapper">
    <input
      :type="type"
      :placeholder="placeholder"
      :value="modelValue"
      :disabled="disabled"
      @input="$emit('update:modelValue', $event.target.value)"
      class="input-field"
    />
  </div>
</template>

<script setup>
/**
 * InputField.vue - 通用输入框组件
 * 
 * 该组件提供基础的输入框功能，支持复用
 */
defineProps({
  modelValue: {
    type: String,
    default: ''
  },
  type: {
    type: String,
    default: 'text'
  },
  placeholder: {
    type: String,
    default: ''
  },
  disabled: {
    type: Boolean,
    default: false
  }
})

defineEmits(['update:modelValue'])
</script>

<style scoped>
.input-wrapper {
  width: 100%;
}

.input-field {
  width: 100%;
  height: 44px;
  padding: 0 12px;
  border: 1px solid #ccc;
  border-radius: 4px;
  font-size: 14px;
  font-family: "PingFang SC", "Microsoft YaHei", sans-serif;
  box-sizing: border-box;
  outline: none;
  transition: border-color 0.2s ease;
}

.input-field:focus {
  border-color: #00B1FE;
}

.input-field::placeholder {
  color: #999;
}

.input-field:disabled {
  background-color: #f5f5f5;
  color: #999;
}
</style>
