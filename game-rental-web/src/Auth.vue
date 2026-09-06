<template>
  <div class="auth-page">
    <Ding_Bu_Dao_Hang_Lan :isSearchExpanded="isSearchExpanded" @toggleSearch="toggleSearch" />

    <Sou_Suo_Mian_Ban :isExpanded="isSearchExpanded" @search="handleSearch" />

    <main class="auth-content" @click="closeSearch">
      <Ye_Mian_Biao_Ti :title="pageTitle" />

      <transition name="fade" mode="out-in">
        <section class="auth-form" :key="currentTab">
          <template v-if="currentTab === 'login'">
            <div class="form-header">
              <h2 class="form-title">登录 简之行</h2>
            </div>

            <div class="tab-switch">
              <button :class="['tab-btn', { active: loginMode === 'account' }]" @click="loginMode = 'account'">
                账号登录
              </button>
              <button :class="['tab-btn', { active: loginMode === 'phone' }]" @click="loginMode = 'phone'">
                手机号登录
              </button>
            </div>

            <el-form :model="loginForm" class="form-content" label-position="top" size="large" @submit.prevent="handleLoginSubmit">
              <template v-if="loginMode === 'account'">
                <el-form-item prop="username">
                  <el-input v-model="loginForm.username" placeholder="请输入用户名" clearable />
                </el-form-item>
                <el-form-item prop="password">
                  <el-input v-model="loginForm.password" type="password" placeholder="请输入密码" clearable />
                </el-form-item>
              </template>

              <template v-else>
                <el-form-item prop="phone">
                  <el-input v-model="loginForm.phone" placeholder="请输入手机号" clearable />
                </el-form-item>
                <el-form-item class="code-input-wrapper" prop="code">
                <el-input v-model="loginForm.code" placeholder="请输入短信验证码" clearable>
                  <template #append>
                    <el-button type="primary" :disabled="loginCodeCountdown > 0" @click="getLoginCode">
                      {{ loginCodeText }}
                    </el-button>
                  </template>
                </el-input>
              </el-form-item>
              </template>

              <div class="form-links">
                <a href="#" class="link" @click.prevent="handleForgotPassword">忘记密码</a>
                <router-link class="link" :to="{ name: 'register' }">注册</router-link>
                <a href="#" class="link" @click.prevent="handleOtherLogin">其他登录方式</a>
              </div>

              <el-button type="primary" native-type="submit" class="submit-button">登录</el-button>
            </el-form>
          </template>

          <template v-else>
            <div class="form-header">
              <h2 class="form-title">注册 简之行</h2>
            </div>

            <div class="tab-switch">
              <button :class="['tab-btn', { active: registerMode === 'phone' }]" @click="registerMode = 'phone'">
                手机号注册
              </button>
              <button :class="['tab-btn', { active: registerMode === 'email' }]" @click="registerMode = 'email'">
                邮箱注册
              </button>
            </div>

            <!-- 手机号注册 -->
            <el-form v-if="registerMode === 'phone'" :model="registerForm" class="form-content" label-position="top" size="large" @submit.prevent="handleRegisterSubmit">
              <el-form-item prop="username">
                <el-input v-model="registerForm.username" placeholder="请输入用户名" clearable />
              </el-form-item>
              <el-form-item prop="phone">
                <el-input v-model="registerForm.phone" placeholder="请输入手机号" clearable />
              </el-form-item>
              <el-form-item class="code-input-wrapper" prop="code">
                <el-input v-model="registerForm.code" placeholder="请输入短信验证码" clearable>
                  <template #append>
                    <el-button type="primary" :disabled="registerCodeCountdown > 0" @click="getRegisterCode">
                      {{ registerCodeText }}
                    </el-button>
                  </template>
                </el-input>
              </el-form-item>
              <el-form-item prop="password">
                <el-input v-model="registerForm.password" type="password" placeholder="请输入密码" clearable />
              </el-form-item>
              <el-form-item prop="confirmPassword">
                <el-input v-model="registerForm.confirmPassword" type="password" placeholder="请确认密码" clearable />
              </el-form-item>

              <div class="form-links">
                <router-link class="link" :to="{ name: 'login' }">已有账号，立即登录</router-link>
              </div>

              <el-button type="primary" native-type="submit" class="submit-button">注册</el-button>
            </el-form>

            <!-- 邮箱注册 -->
            <el-form v-else :model="emailRegisterForm" class="form-content" label-position="top" size="large" @submit.prevent="handleEmailRegisterSubmit">
              <el-form-item prop="email">
                <el-input v-model="emailRegisterForm.email" placeholder="请输入邮箱" clearable type="email" />
              </el-form-item>
              <el-form-item class="code-input-wrapper" prop="verifyCode">
                <el-input v-model="emailRegisterForm.verifyCode" placeholder="请输入邮箱验证码" clearable>
                  <template #append>
                    <el-button type="primary" :disabled="emailCodeCountdown > 0" @click="getEmailRegisterCode">
                      {{ emailCodeText }}
                    </el-button>
                  </template>
                </el-input>
              </el-form-item>
              <el-form-item prop="password">
                <el-input v-model="emailRegisterForm.password" type="password" placeholder="请输入密码" clearable />
              </el-form-item>
              <el-form-item prop="confirmPassword">
                <el-input v-model="emailRegisterForm.confirmPassword" type="password" placeholder="请确认密码" clearable />
              </el-form-item>

              <div class="form-links">
                <router-link class="link" :to="{ name: 'login' }">已有账号，立即登录</router-link>
              </div>

              <el-button type="primary" native-type="submit" class="submit-button">注册</el-button>
            </el-form>
          </template>
        </section>
      </transition>
    </main>
  </div>
</template>

<script setup>
defineOptions({ name: 'AuthPage' })
import { ref, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import Ding_Bu_Dao_Hang_Lan from './components/home/Ding_Bu_Dao_Hang_Lan.vue'
import Sou_Suo_Mian_Ban from './components/home/Sou_Suo_Mian_Ban.vue'
import Ye_Mian_Biao_Ti from './components/common/Ye_Mian_Biao_Ti.vue'
import { authApi } from './api/auth'
import { useUserStore } from './stores/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const isSearchExpanded = ref(false)
const loginMode = ref('phone')
const registerMode = ref('phone')

const loginForm = ref({
  username: '',
  password: '',
  phone: '',
  code: ''
})

const registerForm = ref({
  username: '',
  phone: '',
  code: '',
  password: '',
  confirmPassword: ''
})

const emailRegisterForm = ref({
  email: '',
  verifyCode: '',
  password: '',
  confirmPassword: ''
})

const loginCodeCountdown = ref(0)
const registerCodeCountdown = ref(0)
const emailCodeCountdown = ref(0)

const currentTab = computed(() => route.name === 'register' ? 'register' : 'login')
const pageTitle = computed(() => (currentTab.value === 'register' ? '注册' : '登录'))

const loginCodeText = computed(() => {
  return loginCodeCountdown.value > 0 ? `${loginCodeCountdown.value}s` : '获取验证码'
})

const registerCodeText = computed(() => {
  return registerCodeCountdown.value > 0 ? `${registerCodeCountdown.value}s` : '获取验证码'
})

const emailCodeText = computed(() => {
  return emailCodeCountdown.value > 0 ? `${emailCodeCountdown.value}s` : '获取验证码'
})

const toggleSearch = () => {
  isSearchExpanded.value = !isSearchExpanded.value
}

const closeSearch = () => {
  isSearchExpanded.value = false
}

const handleSearch = (keyword) => {
  ElMessage.info(`搜索：${keyword}`)
}

const startCountdown = (ref) => {
  ref.value = 60
  const timer = setInterval(() => {
    ref.value--
    if (ref.value <= 0) clearInterval(timer)
  }, 1000)
}

const getLoginCode = async () => {
  if (loginCodeCountdown.value > 0) return
  if (!loginForm.value.phone) {
    ElMessage.warning('请先输入手机号')
    return
  }
  try {
    const response = await authApi.sendCode(loginForm.value.phone)
    ElMessage.success(`验证码已发送：${response.yanZhengMa || '请查收短信'}`)
    startCountdown(loginCodeCountdown)
  } catch (error) {
    ElMessage.error('发送验证码失败：' + (error.response?.data?.message || error.message))
  }
}

const getRegisterCode = async () => {
  if (registerCodeCountdown.value > 0) return
  if (!registerForm.value.phone) {
    ElMessage.warning('请先输入手机号')
    return
  }
  try {
    const response = await authApi.sendCode(registerForm.value.phone)
    ElMessage.success(`验证码已发送：${response.yanZhengMa || '请查收短信'}`)
    startCountdown(registerCodeCountdown)
  } catch (error) {
    ElMessage.error('发送验证码失败：' + (error.response?.data?.message || error.message))
  }
}

const handleLoginSubmit = async () => {
  if (loginMode.value === 'account') {
    if (!loginForm.value.username || !loginForm.value.password) {
      ElMessage.warning('请输入用户名和密码')
      return
    }
    try {
      const response = await authApi.login({
        yongHuMing: loginForm.value.username,
        miMa: loginForm.value.password
      })
      userStore.token = response.token
      userStore.userInfo = response.yongHu || response.user || null
      localStorage.setItem('token', response.token)
      ElMessage.success('登录成功')
      // 跳转到之前保存的页面，如果没有则跳转到首页
      const redirect = route.query.redirect || '/'
      router.push(redirect)
    } catch (error) {
      ElMessage.error('登录失败：' + (error.response?.data?.message || error.message))
    }
  } else {
    if (!loginForm.value.phone || !loginForm.value.code) {
      ElMessage.warning('请输入手机号和验证码')
      return
    }
    try {
      const response = await authApi.login({
        yongHuMing: loginForm.value.phone,
        miMa: loginForm.value.code
      })
      userStore.token = response.token
      userStore.userInfo = response.yongHu || response.user || null
      localStorage.setItem('token', response.token)
      ElMessage.success('登录成功')
      // 跳转到之前保存的页面，如果没有则跳转到首页
      const redirect = route.query.redirect || '/'
      router.push(redirect)
    } catch (error) {
      ElMessage.error('登录失败：' + (error.response?.data?.message || error.message))
    }
  }
}

const handleRegisterSubmit = async () => {
  if (!registerForm.value.username) { ElMessage.warning('请输入用户名'); return }
  if (!registerForm.value.phone) { ElMessage.warning('请输入手机号'); return }
  if (!registerForm.value.code) { ElMessage.warning('请输入验证码'); return }
  if (!registerForm.value.password) { ElMessage.warning('请输入密码'); return }
  if (registerForm.value.password !== registerForm.value.confirmPassword) {
    ElMessage.warning('两次输入的密码不一致'); return
  }
  try {
    await authApi.register({
      yongHuMing: registerForm.value.username,
      shouJiHao: registerForm.value.phone,
      miMa: registerForm.value.password,
      verifyCode: registerForm.value.code
    })
    ElMessage.success('注册成功，请登录')
    router.push({ name: 'login' })
  } catch (error) {
    ElMessage.error('注册失败：' + (error.response?.data?.message || error.message))
  }
}

const getEmailRegisterCode = async () => {
  if (emailCodeCountdown.value > 0) return
  if (!emailRegisterForm.value.email) {
    ElMessage.warning('请先输入邮箱')
    return
  }
  try {
    await authApi.sendVerifyCode({ email: emailRegisterForm.value.email })
    ElMessage.success('验证码已发送，请检查邮箱')
    startCountdown(emailCodeCountdown)
  } catch (error) {
    ElMessage.error('发送验证码失败：' + (error.response?.data?.message || error.message))
  }
}

const handleEmailRegisterSubmit = async () => {
  if (!emailRegisterForm.value.email) { ElMessage.warning('请输入邮箱'); return }
  if (!emailRegisterForm.value.verifyCode) { ElMessage.warning('请输入验证码'); return }
  if (!emailRegisterForm.value.password) { ElMessage.warning('请输入密码'); return }
  if (emailRegisterForm.value.password !== emailRegisterForm.value.confirmPassword) {
    ElMessage.warning('两次输入的密码不一致'); return
  }
  try {
    await authApi.register({
      email: emailRegisterForm.value.email,
      verifyCode: emailRegisterForm.value.verifyCode,
      miMa: emailRegisterForm.value.password
    })
    ElMessage.success('注册成功，请登录')
    router.push({ name: 'login' })
  } catch (error) {
    ElMessage.error('注册失败：' + (error.response?.data?.message || error.message))
  }
}

const handleForgotPassword = () => {
  ElMessage.info('忘记密码功能已开启，请使用邮箱重置')
}

const handleOtherLogin = () => {
  ElMessage.info('其他登录方式开发中')
}
</script>

<style scoped>
.auth-page {
  min-height: 100vh;
  background-color: #f5f5f5;
  font-family: "PingFang SC", "Microsoft YaHei", -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
}

.auth-content {
  padding-top: 80px;
  min-height: calc(100vh - 80px);
  display: flex;
  flex-direction: column;
}

.auth-tab-switch {
  display: flex;
  width: 100%;
  max-width: 500px;
  margin: 0 auto 20px;
  padding: 0 40px;
  gap: 20px;
}

.auth-tab-btn {
  flex: 1;
  height: 44px;
  background: none;
  border: 1px solid #ccc;
  border-radius: 4px;
  font-size: 16px;
  color: #666;
  cursor: pointer;
  transition: all 0.2s ease;
}

.auth-tab-btn:hover {
  border-color: #00B1FE;
  color: #00B1FE;
}

.auth-tab-btn.active {
  background-color: #00B1FE;
  border-color: #00B1FE;
  color: #fff;
}

.auth-form {
  width: 100%;
  max-width: 500px;
  margin: 80px auto 60px;
  padding: 0 40px;
}

.form-header {
  text-align: center;
  margin-bottom: 30px;
}

.form-title {
  font-size: 24px;
  font-weight: 400;
  color: #333;
  margin: 0;
}

.tab-switch {
  display: flex;
  margin-bottom: 30px;
  border-bottom: 1px solid #eee;
}

.tab-btn {
  flex: 1;
  height: 40px;
  background: none;
  border: none;
  font-size: 16px;
  color: #666;
  cursor: pointer;
  position: relative;
  transition: color 0.2s ease;
}

.tab-btn:hover {
  color: #00B1FE;
}

.tab-btn.active {
  color: #00B1FE;
}

.tab-btn.active::after {
  content: '';
  position: absolute;
  bottom: -1px;
  left: 0;
  right: 0;
  height: 2px;
  background-color: #00B1FE;
}

.form-content {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.code-input-wrapper {
  display: flex;
  gap: 12px;
  align-items: stretch;
  width: 100%;
}

.code-input-wrapper :deep(.el-input) {
  flex: 1;
  min-width: 0;
}

.code-input-wrapper :deep(.el-input__inner) {
  width: 100%;
  min-height: 44px;
  border-top-right-radius: 0;
  border-bottom-right-radius: 0;
}

.code-input-wrapper :deep(.el-input-group__append .el-button) {
  min-width: 96px;
  height: 100%;
  width: auto;
  padding: 0 10px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border-top-left-radius: 0;
  border-bottom-left-radius: 0;
}

.form-links {
  display: flex;
  justify-content: flex-end;
  gap: 20px;
  flex-wrap: wrap;
}

.link {
  font-size: 14px;
  color: #666;
  text-decoration: none;
  transition: color 0.2s ease;
}

.link:hover {
  color: #00B1FE;
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s ease, transform 0.2s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
  transform: translateY(8px);
}

.form-content :deep(.el-button) {
  width: auto;
}
.submit-button {
  width: 100%;
}
</style>
