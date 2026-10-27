<template>
  <router-view />
  <AuthDialog />
</template>

<script setup>
import { useUserStore } from './stores/user'
import AuthDialog from './components/Deng_Lu/AuthDialog.vue'

const userStore = useUserStore()
userStore.restoreToken()
</script>

<style>
/**
 * 全局样式重置和基础设置
 * 应用于整个HTML文档，确保跨浏览器一致性
 */
html,
body {
  margin: 0;
  /* 移除默认外边距 */
  padding: 0;
  /* 移除默认内边距 */
  width: 100%;
  /* 占满整个视口宽度 */
  min-height: 100vh;
  /* 最小高度为视口高度 */
  overflow-y: visible;
  /* 允许垂直滚动 */
}

/**
 * 应用容器样式
 * #app 是 main.js 中挂载的目标元素
 */
#app {
  font-family: 'PingFang SC', 'Microsoft YaHei', Avenir, Helvetica, Arial, sans-serif;
  /* 设置字体族，优先使用苹方 */
  -webkit-font-smoothing: antialiased;
  /* WebKit 浏览器字体平滑 */
  -moz-osx-font-smoothing: grayscale;
  /* Firefox 浏览器字体平滑 */
  width: 100%;
  /* 占满整个宽度 */
  min-height: 100vh;
  /* 最小高度为视口高度 */
}

/**
 * 重置原生button元素默认样式
 * 避免Element Plus等UI库的全局样式影响自定义按钮
 */
button.action-btn {
  background: transparent !important;
  border: none !important;
  outline: none !important;
  box-shadow: none !important;
  -webkit-appearance: none !important;
  appearance: none !important;
}
</style>
