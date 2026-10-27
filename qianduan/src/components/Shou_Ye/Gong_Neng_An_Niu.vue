<template>
  <div class="action-buttons">
    <button class="action-btn" @click="handleShelf">上架</button>
    <button class="action-btn" @click="handleFavorite">收藏</button>
    <button class="action-btn" @click="handleMessage">消息</button>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { useUserStore } from '../../stores/user'
import { useAuthStore } from '../../stores/auth'

const router = useRouter()
const userStore = useUserStore()
const authStore = useAuthStore()

/**
 * 处理上架按钮点击事件
 */
const handleShelf = () => {
  router.push('/shelf')
}

/**
 * 处理收藏按钮点击事件
 * 判断用户是否登录，未登录显示登录窗口，已登录跳转到个人中心收藏页
 */
const handleFavorite = () => {
  if (!userStore.isLogin) {
    authStore.openLogin()
  } else {
    router.push('/personal?tab=favorite')
  }
}

/**
 * 处理消息按钮点击事件
 * 判断用户是否登录，未登录显示登录窗口，已登录跳转到个人中心消息页
 */
const handleMessage = () => {
  if (!userStore.isLogin) {
    authStore.openLogin()
  } else {
    router.push('/personal?tab=message')
  }
}
</script>

<style scoped>
/**
 * 功能按钮区域样式
 * - flex布局，按钮水平排列
 * - 居中对齐
 * - margin: 32px 0，统一与其他模块的垂直间距
 */
.action-buttons {
  display: flex;
  justify-content: center;
  gap: 350px;
  margin: 32px;
}

/**
 * 功能按钮样式
 * - 字体加粗，增强视觉层次
 * - 颜色#333333（深灰色）
 * - 无背景、无边框，纯文字按钮
 * - transition实现平滑的颜色过渡效果
 */
.action-btn {
  min-width: 60px;
  justify-content: center;
  cursor: pointer;
  font-size: 20px;
  color: #333333;
  font-weight: 450;
  padding: 8px 15px;
  background: none;
  border: none;
  outline: none;
  box-shadow: none;
  -webkit-appearance: none;
  appearance: none;
  display: flex;
  align-items: center;
  transition: color 0.2s ease;
}

.action-btn:focus,
.action-btn:active,
.action-btn:visited {
  background: none;
  border: none;
  outline: none;
  box-shadow: none;
}

/**
 * 功能按钮悬停状态
 * 颜色变为#00B1FE，与顶部导航栏按钮悬浮效果一致
 */
.action-btn:hover {
  color: #00b1fe;
}
</style>
