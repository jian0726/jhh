<template>
  <header class="top-navbar" :style="gradientStyle">
    <div class="top-row">
      <div class="nav-left">
        <button class="nav-btn back-btn" @click="goHome">
          <img src="../../../img/arrow-go-back-line.svg" alt="返回首页" class="nav-icon" />
          <span>返回首页</span>
        </button>
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
            :src="isSearchHovered ? iconSearchActive : iconSearch"
            alt="搜索"
            class="nav-icon"
          />
        </button>

        <template v-if="!isSearchExpanded">
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
        </template>
      </div>
    </div>
  </header>
</template>

<script setup>
import { ref, watch, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../../stores/auth'
import { useUserStore } from '../../stores/user'
// 悬停态图标必须用 import 引入，Vite 才会处理成构建产物；
// 写成动态字符串（如 :src="cond ? 'xxx.svg' : 'yyy.svg'"）在构建后会 404。
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

const isLogin = computed(() => userStore.isLogin)

watch(isLogin, (newVal) => {
  console.log('登录状态变化:', newVal)
})

const gradientStyle = computed(() => ({
  background: 'linear-gradient(to bottom, #F2F2F2, #FFFFFF)',
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

defineEmits(['toggleSearch'])

const router = useRouter()
const isSearchHovered = ref(false)
const isUserHovered = ref(false)

const goHome = () => {
  router.push('/')
}

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
  height: 100px;
  display: block;
  z-index: 1000;
}

.top-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 100px;
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
  gap: 24px;
  margin-right: 20px;
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

.nav-icon,
.user-icon {
  width: 24px;
  height: 24px;
  transition: filter 0.2s ease;
}

.back-btn {
  gap: 8px;
}

.back-btn .nav-icon {
  width: 22px;
  height: 22px;
}

.nav-btn:hover {
  color: #00b1fe;
}

.nav-btn:hover .nav-icon,
.nav-btn:hover .user-icon {
  filter: brightness(0) saturate(100%) invert(42%) sepia(93%) saturate(1669%) hue-rotate(177deg)
    brightness(100%) contrast(101%);
}

@media (max-width: 480px) {
  .top-row {
    padding: 0 20px;
  }

  .nav-btn {
    font-size: 14px;
  }
}
</style>
