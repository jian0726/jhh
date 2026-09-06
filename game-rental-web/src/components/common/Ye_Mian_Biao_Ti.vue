<template>
  <!-- 
    页面标题组件
    用于在页面左上角显示标题，保持统一的样式和布局
  -->
  <div class="page-title-wrapper">
    <h1 class="page-title">{{ title }}</h1>
  </div>
</template>

<script setup>
/**
 * PageTitle.vue - 页面标题组件
 * 
 * 该组件用于在页面左上角显示标题，提供以下功能：
 * - 统一的标题样式
 * - 支持自定义标题内容
 * - 响应式设计，适配不同屏幕尺寸
 * 
 * 设计规范：
 * - 字体大小：36px（大屏）/ 28px（中等屏）/ 24px（小屏）
 * - 字体粗细：600
 * - 颜色：#333333
 * - 对齐方式：左对齐
 */
defineProps({
  /**
   * 页面标题内容
   */
  title: {
    type: String,
    required: true
  }
})
</script>

<style scoped>
/**
 * 标题容器样式
 * - 宽度100%，确保占据整行
 */
.page-title-wrapper {
  width: 100%;
}

/**
 * 页面标题样式
 * - 字体大小 36px，字重 400
 * - 使用苹方字体，中文显示更清晰
 * - 颜色#333333，深灰色
 * - 左对齐
 * - 外边距：上下各 20px
 */
.page-title {
  font-size: 36px;
  font-weight: 400;
  font-family: "PingFang SC", "Microsoft YaHei", sans-serif;
  color: #333333;
  text-align: left;
  margin: 20px 0 20px 80px;
  padding: 0;
}

/**
 * 响应式设计：中等屏幕适配
 * 当屏幕宽度小于1024px时：
 * - 字体大小减小至28px
 */
@media (max-width: 1024px) {
  .page-title {
    font-size: 28px;
  }
}

/**
 * 响应式设计：小屏幕适配
 * 当屏幕宽度小于768px时：
 * - 字体大小减小至24px
 * - 外边距调整为上下各16px
 */
@media (max-width: 768px) {
  .page-title {
    font-size: 24px;
    margin: 16px 0;
  }
}

/**
 * 响应式设计：超小屏幕适配
 * 当屏幕宽度小于480px时：
 * - 字体大小减小至20px
 * - 外边距调整为上下各12px
 */
@media (max-width: 480px) {
  .page-title {
    font-size: 20px;
    margin: 12px 0;
  }
}
</style>
