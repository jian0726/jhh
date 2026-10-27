<template>
  <header class="top-navbar" :style="gradientStyle">
    <div class="top-row">
      <div class="nav-left">
        <img :src="logoIcon" alt="Logo" class="logo-image" />
        <h1 class="title">ClickDone</h1>
      </div>

      <div class="nav-right">
        <button
          class="nav-btn search-btn"
          v-if="!isSearchExpanded"
          @click="emit('toggleSearch')"
          @mouseenter="isSearchHovered = true"
          @mouseleave="isSearchHovered = false"
        >
          <img
            :src="isSearchHovered ? iconSearchActive : iconSearch"
            alt="搜索"
            class="nav-icon"
          />
        </button>

        <button
          class="nav-btn"
          v-if="!isLogin"
          @click="handleLogin"
          @mouseenter="isUserHovered = true"
          @mouseleave="isUserHovered = false"
        >
          <img
            :src="isUserHovered ? iconLoginActive : iconLogin"
            alt="登录"
            class="nav-icon"
          />
        </button>
        <button
          class="nav-btn personal-btn"
          v-else
          @click="goPersonalCenter"
          @mouseenter="isUserHovered = true"
          @mouseleave="isUserHovered = false"
        >
          <img
            :src="isUserHovered ? iconUserActive : iconUser"
            alt="个人中心"
            class="user-icon"
          />
        </button>
      </div>
    </div>
  </header>
</template>

<script setup>
import { ref, watch, computed } from 'vue'
import { useAuthStore } from '../../stores/auth'
import { useUserStore } from '../../stores/user'
import { useRouter } from 'vue-router'
// 图标统一用 import 引入，Vite 才会打进构建产物（动态字符串路径在生产环境会 404）
import logoIcon from '../../../img/b63c172a-83db-4099-b3ab-777949a7f1f8.svg'
import iconSearch from '../../../img/search-line.svg'
import iconSearchActive from '../../../img/search-line-blue.svg'
import iconLogin from '../../../img/login-circle-line.svg'
import iconLoginActive from '../../../img/login-circle-line-blue.svg'
import iconUser from '../../../img/user-line.svg'
import iconUserActive from '../../../img/user-line-blue.svg'

const props = defineProps({
  isSearchExpanded: {
    type: Boolean,
    default: false,
  },
})

const authStore = useAuthStore()
const userStore = useUserStore()
const router = useRouter()
const isSearchHovered = ref(false)
const isUserHovered = ref(false)

const isLogin = computed(() => userStore.isLogin)

const gradientStyle = computed(() => ({
  background: 'linear-gradient(to bottom, #AAAAAA, #FFFFFF)',
  transition: 'background 0.8s ease-in-out',
}))

watch(
  () => props.isSearchExpanded,
  (newVal) => {
    if (newVal) {
      isSearchHovered.value = false
    }
  },
)

const emit = defineEmits(['toggleSearch'])

const handleLogin = () => {
  authStore.openLogin()
}

const goPersonalCenter = () => {
  if (!userStore.isLogin) {
    authStore.openLogin()
  } else {
    router.push('/personal')
  }
}
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
  gap: 24px;
}

.nav-btn {
  min-width: 48px;
  justify-content: center;
  cursor: pointer;
  font-size: 18px;
  font-family: 'PingFang SC', 'Microsoft YaHei', sans-serif;
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
  color: #00b1fe;
}

.nav-btn:hover .nav-icon,
.nav-btn:hover .user-icon {
  filter: brightness(0) saturate(100%) invert(42%) sepia(93%) saturate(1669%) hue-rotate(177deg)
    brightness(100%) contrast(101%);
}

.nav-icon {
  width: 24px;
  height: 24px;
}

.user-icon {
  width: 24px;
  height: 24px;
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

@media (max-width: 480px) {
  .top-row {
    padding: 0 20px;
  }

  .title {
    font-size: 22px;
    letter-spacing: 4px;
  }
}
</style>
