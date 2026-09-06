<template>
  <!--
    协议同意和操作按钮组件
    用于上架页面的底部操作区域

    功能说明：
    - 协议勾选：用户必须同意《虚拟资产发布协议》才能提交
    - 重置按钮：清空所有表单数据
    - 提交按钮：发布商品，未同意协议时禁用
  -->
  <div class="agreement-actions">
    <!-- 协议勾选框 -->
    <el-checkbox v-model="agreed" class="agreement-checkbox">
      我已阅读并同意相关协议
      <a href="#" class="agreement-link">《虚拟资产发布协议》</a>
    </el-checkbox>

    <!-- 操作按钮组 -->
    <div class="button-group">
      <!-- 重置按钮：点击后清空表单 -->
      <el-button type="default" @click="handleReset" class="btn-reset">重置</el-button>
      <!-- 提交按钮：未同意协议时禁用 -->
      <el-button 
        type="primary" 
        :disabled="disabled" 
        @click="handleSubmit" 
        class="btn-submit"
      >
        立即发布
      </el-button>
    </div>
  </div>
</template>

<script setup>
/**
 * Xie_Yi_Tong_Yi_He_An_Niu.vue - 协议同意和操作按钮组件
 *
 * 该组件用于上架页面的底部操作区域，包含：
 * - 协议勾选：用户必须同意协议才能提交
 * - 重置按钮：清空所有表单数据
 * - 提交按钮：发布商品
 *
 * 交互功能：
 * - 协议状态变化时触发 agreement-change 事件
 * - 点击重置时触发 reset 事件
 * - 点击提交时触发 submit 事件
 */
import { ref, watch } from 'vue'

/**
 * 定义组件接收的Props
 *
 * @property {Boolean} disabled - 提交按钮是否禁用
 *                                true: 禁用状态，false: 可点击
 */
const props = defineProps({
  disabled: {
    type: Boolean,
    default: true
  }
})

/**
 * 定义组件事件
 *
 * @event agreement-change - 协议勾选状态变化时触发
 *                           传递是否同意的布尔值
 * @event reset - 点击重置按钮时触发
 * @event submit - 点击提交按钮时触发
 */
const emit = defineEmits(['reset', 'submit', 'agreement-change'])

/**
 * 协议同意状态
 * true: 已同意，false: 未同意
 */
const agreed = ref(false)

/**
 * 监听协议状态变化
 * 当用户勾选/取消勾选时通知父组件
 */
watch(agreed, (newVal) => {
  emit('agreement-change', newVal)
})

/**
 * 处理重置按钮点击事件
 * 确认后将重置所有表单数据
 */
const handleReset = () => {
  if (confirm('确定要重置所有内容吗？')) {
    agreed.value = false
    emit('reset')
  }
}

/**
 * 处理提交按钮点击事件
 * 检查是否同意协议，未同意则提示警告
 */
const handleSubmit = () => {
  if (!agreed.value) {
    ElMessage.warning('请先阅读并同意相关协议')
    return
  }
  emit('submit')
}

/**
 * 重置协议状态
 * 供父组件调用，用于表单重置时同步重置协议状态
 */
const resetAgreement = () => {
  agreed.value = false
}

// 暴露resetAgreement方法供父组件调用
defineExpose({
  resetAgreement
})
</script>

<style scoped>
.agreement-actions {
  padding-top: 20px;
  border-top: 1px solid #f0f0f0;
}

.agreement-checkbox {
  padding: 12px;
  background-color: #f8f9fa;
  border-radius: 6px;
  margin-bottom: 20px;
}

.agreement-link {
  color: #00B1FE;
  text-decoration: none;
  font-weight: 500;
}

.agreement-link:hover {
  text-decoration: underline;
}

.button-group {
  display: flex;
  gap: 12px;
}

.button-group .el-button {
  flex: 1;
  height: 44px;
  font-size: 14px;
  font-weight: 500;
}
</style>