<template>
  <header class="top-navbar" :style="gradientStyle">
    <div class="top-row">
      <div class="nav-left">
        <button class="nav-btn back-btn" @click="goHome">返回首页</button>
      </div>

      <div class="nav-right">
        <button
          class="nav-btn search-btn"
          v-if="!isSearchExpanded"
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

        <template v-if="!isSearchExpanded">
          <button class="nav-btn" v-if="!isLogin" @click="handleLogin">登录</button>
          <div v-else class="user-info" @click="goPersonalCenter">
            <div class="user-avatar">{{ avatarLetter }}</div>
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
import { useRouter } from 'vue-router'
import { useAuthStore } from '../../stores/auth'
import { useUserStore } from '../../stores/user'
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

watch(isLogin, async (val) => {
  if (val) fetchUnreadCount()
})

watch(() => props.isSearchExpanded, (newVal) => {
  if (newVal) isSearchHovered.value = false
})

const fetchUnreadCount = async () => {
  try {
    const res = await messageApi.getUnreadCount()
    unreadCount.value = res?.count || 0
  } catch {
    unreadCount.value = 0
  }
}

defineEmits(['toggleSearch'])

const goHome = () => router.push('/')

const handleLogin = () => authStore.openLogin()

const goPersonalCenter = () => router.push('/personal')

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
  padding-left: 0;
  padding-right: 0;
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
  margin-right: 20px;
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

.nav-btn:hover {
  color: #00B1FE;
}

.nav-icon {
  width: 20px;
  height: 20px;
}

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

  .nav-btn {
    font-size: 14px;
    padding: 4px 6px;
    min-width: 36px;
  }

  .user-name {
    display: none;
  }
}
</style>
