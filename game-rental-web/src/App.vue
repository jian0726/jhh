<template>
  <router-view v-slot="{ Component, route }">
    <transition name="page-fade">
      <component :is="Component" :key="route.fullPath" />
    </transition>
  </router-view>
  <AuthDialog />
</template>

<script setup>
import { useUserStore } from './stores/user'
import AuthDialog from './components/auth/AuthDialog.vue'

const userStore = useUserStore()
userStore.restoreToken()
// 如果有 token 但缺少用户信息，自动恢复
if (userStore.token && !userStore.userInfo) {
  userStore.restoreUserInfo()
}
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
 * 路由页面切换过渡动画
 */
.page-fade-enter-active,
.page-fade-leave-active {
  transition: opacity 0.2s ease, transform 0.2s ease;
}

.page-fade-enter-from {
  opacity: 0;
  transform: translateY(8px);
}

.page-fade-leave-to {
  opacity: 0;
  transform: translateY(-8px);
}

/**
 * 应用容器样式
 * #app 是 main.js 中挂载的目标元素
 */
#app {
  font-family: "PingFang SC", "Microsoft YaHei", Avenir, Helvetica, Arial, sans-serif;
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
</style>
