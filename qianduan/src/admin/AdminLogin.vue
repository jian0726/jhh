<template>
  <div class="admin-login-page">
    <div class="login-card">
      <div class="login-brand">
        <h1>三角洲行动 · 管理后台</h1>
        <p>仅限管理员账号登录</p>
      </div>

      <el-alert
        v-if="deniedTip"
        title="当前账号不是管理员，已退出登录"
        type="warning"
        :closable="false"
        show-icon
        class="login-alert"
      />

      <el-form :model="form" label-position="top" @keyup.enter="handleLogin">
        <el-form-item label="用户名">
          <el-input v-model="form.yongHuMing" placeholder="请输入管理员用户名" size="large" clearable />
        </el-form-item>
        <el-form-item label="密码">
          <el-input
            v-model="form.miMa"
            type="password"
            placeholder="请输入密码"
            size="large"
            show-password
            clearable
          />
        </el-form-item>
        <el-button type="primary" size="large" class="login-btn" :loading="loading" @click="handleLogin">
          登 录
        </el-button>
      </el-form>

      <div class="login-footer">
        <a :href="storeUrl" target="_blank" rel="noopener">前往商城前台</a>
        <span class="divider">|</span>
        <span class="hint">默认账号 admin / admin123</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '../stores/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const form = ref({ yongHuMing: '', miMa: '' })
const loading = ref(false)

const storeUrl = import.meta.env.VITE_STORE_URL || 'http://localhost:5173'
const deniedTip = computed(() => route.query.denied === '1')

const handleLogin = async () => {
  if (!form.value.yongHuMing.trim() || !form.value.miMa) {
    ElMessage.warning('请输入用户名和密码')
    return
  }
  loading.value = true
  try {
    await userStore.login({
      yongHuMing: form.value.yongHuMing.trim(),
      miMa: form.value.miMa,
    })
    // 关键校验：不是管理员直接踢回登录页
    if (userStore.userInfo?.jueSe !== 2) {
      userStore.logout()
      ElMessage.error('该账号不是管理员，无法登录后台')
      return
    }
    ElMessage.success('登录成功')
    const redirect = typeof route.query.redirect === 'string' ? route.query.redirect : '/'
    router.replace(redirect)
  } catch (err) {
    ElMessage.error(err?.message || '登录失败，请检查用户名和密码')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  // 落地页已登录但不是管理员时，清掉残留登录态
  if (userStore.isLogin && userStore.userInfo && userStore.userInfo.jueSe !== 2) {
    userStore.logout()
  }
})
</script>

<style scoped>
.admin-login-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(180deg, #eef4f8 0%, #f7f9fb 55%, #ffffff 100%);
}

.login-card {
  width: 400px;
  padding: 40px 36px 28px;
  background: #ffffff;
  border-radius: 14px;
  box-shadow: 0 10px 40px rgba(0, 60, 100, 0.08);
}

.login-brand {
  text-align: center;
  margin-bottom: 26px;
}

.login-brand h1 {
  font-size: 22px;
  font-weight: 600;
  color: #333333;
  margin: 0 0 8px;
}

.login-brand p {
  font-size: 13px;
  color: #999999;
  margin: 0;
}

.login-alert {
  margin-bottom: 18px;
}

.login-btn {
  width: 100%;
  margin-top: 6px;
  font-size: 16px;
  letter-spacing: 4px;
}

.login-footer {
  margin-top: 22px;
  text-align: center;
  font-size: 12px;
  color: #999999;
}

.login-footer a {
  color: #00b1fe;
  text-decoration: none;
}

.login-footer .divider {
  margin: 0 8px;
  color: #dcdfe6;
}
</style>
