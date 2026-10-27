<template>
  <header class="admin-navbar">
    <div class="nav-left">
      <span class="brand">三角洲行动</span>
      <span class="brand-sub">管理后台</span>
    </div>

    <div class="nav-right">
      <a class="store-link" :href="storeUrl" target="_blank" rel="noopener">
        <img src="../../../img/home-line.svg" alt="商城前台" class="nav-icon" />
        <span>商城前台</span>
      </a>
      <span class="admin-name">{{ adminName }}</span>
      <el-button size="small" text class="logout-btn" @click="handleLogout">退出登录</el-button>
    </div>
  </header>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessageBox, ElMessage } from 'element-plus'
import { useUserStore } from '../../stores/user'

const router = useRouter()
const userStore = useUserStore()

const storeUrl = import.meta.env.VITE_STORE_URL || 'http://localhost:5173'
const adminName = computed(() => userStore.userInfo?.yongHuMing || '管理员')

const handleLogout = async () => {
  try {
    await ElMessageBox.confirm('确定要退出管理后台吗？', '提示', { type: 'warning' })
    userStore.logout()
    ElMessage.success('已退出登录')
    router.replace('/login')
  } catch (err) {
    if (err !== 'cancel' && err !== 'close') {
      // 用户取消，无需处理
    }
  }
}
</script>

<style scoped>
/* 与商城顶部导航一致的渐变与高度，保持整体风格统一 */
.admin-navbar {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  height: 100px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 28px 0 24px;
  background: linear-gradient(to bottom, #f2f2f2, #ffffff);
  z-index: 1000;
}

.nav-left {
  display: flex;
  align-items: baseline;
  gap: 10px;
}

.brand {
  font-size: 22px;
  font-weight: 600;
  color: #333333;
}

.brand-sub {
  font-size: 14px;
  color: #00b1fe;
  padding: 2px 8px;
  background: #e6f7ff;
  border-radius: 4px;
}

.nav-right {
  display: flex;
  align-items: center;
  gap: 18px;
}

.store-link {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 14px;
  color: #333333;
  text-decoration: none;
}

.store-link:hover {
  color: #00b1fe;
}

.nav-icon {
  width: 20px;
  height: 20px;
}

.admin-name {
  font-size: 14px;
  color: #666666;
}

.logout-btn {
  font-size: 14px;
  color: #ff6b6b;
}
</style>
