﻿<template>
  <header class="top-navbar" :style="gradientStyle">
    <div class="top-row">
      <div class="nav-left">
        <img src="/img/b63c172a-83db-4099-b3ab-777949a7f1f8.svg" alt="Logo" class="logo-image" />
        <h1 class="title">ClickDone</h1>
      </div>

      <div class="nav-right">
        <button
          class="nav-btn search-btn"
          @click="$emit('toggleSearch')"
          @mouseenter="isSearchHovered = true"
          @mouseleave="isSearchHovered = false"
        >
          <img
            :src="isSearchHovered ? '/img/search copy.svg' : '/img/search.svg'"
            alt="搜索"
            class="nav-icon"
          />
        </button>

        <!-- 未登录：显示登录按钮 -->
        <template v-if="!isSearchExpanded">
          <button class="nav-btn" v-if="!isLogin" @click="handleLogin">登录</button>

          <!-- 已登录：显示用户信息 -->
          <div v-else class="user-info" @click="goPersonalCenter">
            <div class="user-avatar">
              {{ avatarLetter }}
            </div>
            <span class="user-name">{{ userName }}</span>
            <span v-if="unreadCount > 0" class="unread-badge">{{ unreadCount > 99 ? '99+' : unreadCount }}</span>
          </div>
        </template>
      </div>
    </div>
  </header>
</template>

<script setup>
import { ref, watch, computed, onMounted } from 'vue'
import { useAuthStore } from '../../stores/auth'
import { useUserStore } from '../../stores/user'
import { useRouter } from 'vue-router'
import { messageApi } from '../../api/message'

const props = defineProps({
  isSearchExpanded: {
    type: Boolean,
    default: false
  }
})

const authStore = useAuthStore()
const userStore = useUserStore()
const router = useRouter()
const isSearchHovered = ref(false)
const unreadCount = ref(0)

const isLogin = computed(() => userStore.isLogin)

const avatarLetter = computed(() => {
  const name = userStore.userInfo?.yongHuMing || 'U'
  return name.charAt(0).toUpperCase()
})

const userName = computed(() => {
  return userStore.userInfo?.yongHuMing || '用户'
})

const gradientStyle = computed(() => ({
  background: 'linear-gradient(to bottom, #d6cabf, #FFFFFF)',
  transition: 'background 0.8s ease-in-out'
}))

watch(() => props.isSearchExpanded, (newVal) => {
  if (newVal) {
    isSearchHovered.value = false
  }
})

// 监听登录状态变化，登录后刷新未读消息数
watch(isLogin, async (val) => {
  if (val) fetchUnreadCount()
})

const fetchUnreadCount = async () => {
  try {
    const res = await messageApi.getUnreadCount()
    unreadCount.value = res?.count || 0
  } catch {
    unreadCount.value = 0
  }
}

const emit = defineEmits(['toggleSearch'])

const handleLogin = () => {
  authStore.openLogin()
}

const goPersonalCenter = () => {
  router.push('/personal')
}

onMounted(() => {
  if (isLogin.value) fetchUnreadCount()
})
</script>

<style scoped>
.top-navbar {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  height: 120px;
  display: block;
  z-index: 1000;
}

.top-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 120px;
  padding-left: 20px;
  padding-right: 20px;
}

.nav-left {
  display: flex;
  align-items: center;
  gap: 5px;
  margin-left: 20px;
  margin-right: 20px;
}

.nav-right {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 16px;
}

.nav-btn {
  min-width: 48px;
  justify-content: center;
  cursor: pointer;
  font-size: 18px;
  font-family: "PingFang SC", "Microsoft YaHei", sans-serif;
  font-weight: 600;
  color: #333333;
  padding: 5px 10px;
  background: none;
  border: none;
  display: flex;
  align-items: center;
  transition: color 0.2s ease;
  position: relative;
  z-index: 1001;
}

.search-btn {
  z-index: 1001;
}

.nav-btn:hover {
  color: #00B1FE;
}

.nav-icon {
  width: 20px;
  height: 20px;
}

.logo-image {
  width: 110px;
  height: 110px;
  object-fit: contain;
}

.title {
  font-family: Impact, sans-serif;
  font-size: 30px;
  font-weight: 400;
  letter-spacing: 4px;
  color: #333333;
}

/* 用户信息区域 */
.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  padding: 4px 12px 4px 4px;
  border-radius: 24px;
  transition: background 0.2s;
  position: relative;
  z-index: 1001;
}

.user-info:hover {
  background: rgba(0, 0, 0, 0.04);
}

.user-avatar {
  width: 34px;
  height: 34px;
  border-radius: 50%;
  background: linear-gradient(135deg, #00B1FE, #0077cc);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 15px;
  font-weight: 700;
  flex-shrink: 0;
}

.user-name {
  font-size: 15px;
  font-weight: 500;
  color: #333;
  max-width: 100px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.unread-badge {
  position: absolute;
  top: -2px;
  right: -2px;
  min-width: 18px;
  height: 18px;
  padding: 0 5px;
  background: #f56c6c;
  color: #fff;
  border-radius: 9px;
  font-size: 11px;
  font-weight: 600;
  display: flex;
  align-items: center;
  justify-content: center;
  line-height: 1;
}

@media (max-width: 768px) {
  .top-navbar,
  .top-row {
    height: 80px;
  }

  .logo-image {
    width: 70px;
    height: 70px;
  }

  .title {
    font-size: 24px;
    letter-spacing: 3px;
  }

  .user-name {
    max-width: 60px;
    font-size: 14px;
  }
}

@media (max-width: 480px) {
  .top-navbar,
  .top-row {
    height: 64px;
  }

  .top-row {
    padding: 0 12px;
  }

  .logo-image {
    width: 54px;
    height: 54px;
  }

  .title {
    font-size: 18px;
    letter-spacing: 2px;
  }

  .user-name {
    display: none;
  }

  .nav-btn {
    font-size: 15px;
    padding: 4px 6px;
    min-width: 36px;
  }
}
</style>


