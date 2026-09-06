<template>
  <!-- 
    加载提示组件
    显示加载状态或"没有更多"提示
    
    功能说明：
    - v-if="show"控制组件是否渲染
    - 根据loading状态显示不同文本
  -->
  <div class="loading-tip" v-if="show">
    <span>{{ loading ? '加载中...' : '没有更多了' }}</span>
  </div>
</template>

<script setup>
/**
 * Jia_Zai_Ti_Shi.vue - 加载提示组件
 * 
 * 该组件用于在商品列表中显示加载状态，提供两种提示：
 * - 加载中：正在获取更多数据时显示
 * - 没有更多了：所有数据已加载完毕时显示
 * 
 * 使用场景：
 * - 无限滚动加载时的底部提示
 * - 分页加载的状态反馈
 */

/**
 * 定义组件接收的Props
 * 
 * @property {Boolean} loading - 是否正在加载中
 *                               true: 显示"加载中..."
 *                               false: 显示"没有更多了"
 * @property {Boolean} show - 是否显示该提示组件
 *                            true: 渲染并显示提示
 *                            false: 不渲染该组件
 */
defineProps({
  loading: {
    type: Boolean,
    default: false
  },
  show: {
    type: Boolean,
    default: false
  }
})
</script>

<style scoped>
/**
 * 加载提示容器样式
 * - text-align: center，文字居中对齐
 * - padding: 20px，上下留白
 * - color: #999，浅灰色文字
 * - font-size: 14px，较小字号
 * - 使用苹方字体，中文显示更清晰
 */
.loading-tip {
  text-align: center;
  padding: 20px;
  color: #999; 
  font-size: 14px;
  font-family: "PingFang SC", "Microsoft YaHei", sans-serif;
}
</style>
