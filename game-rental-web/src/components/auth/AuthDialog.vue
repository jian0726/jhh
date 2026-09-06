<template>
  <el-dialog
    v-model="authStore.visible"
    width="480px"
    top="12vh"
    :close-on-click-modal="true"
    :close-on-press-escape="true"
    :destroy-on-close="false"
    @close="authStore.close"
  >
    <template #title>
      <div class="dialog-title">{{ dialogTitle }}</div>
    </template>

    <div class="dialog-body">
      <LoginForm
        ref="loginFormRef"
        v-if="authStore.mode === 'login'"
        :initial-username="prefillUsername"
        @login-success="handleLoginSuccess"
      />
      <RegisterForm
        v-else-if="authStore.mode === 'register'"
        @register-success="handleRegisterSuccess"
      />
      <ForgotPasswordForm
        v-else
        @reset-success="handleResetSuccess"
      />
    </div>

    <div class="dialog-footer">
      <span v-if="authStore.mode === 'login'">
        没有账号？
        <a href="#" @click.prevent="authStore.openRegister()">注册</a>
     
      </span>
      <span v-else-if="authStore.mode === 'register'">
        已有账号？
        <a href="#" @click.prevent="authStore.openLogin()">登录</a>
      </span>
      <span v-else>
        <a href="#" @click.prevent="authStore.openLogin()">登录</a>
      </span>
    </div>
  </el-dialog>
</template>

<script setup>
import { computed, ref, nextTick } from 'vue'
import { useAuthStore } from '../../stores/auth'
import { ElMessage } from 'element-plus'
import LoginForm from './LoginForm.vue'
import RegisterForm from './RegisterForm.vue'
import ForgotPasswordForm from './ForgotPasswordForm.vue'

const authStore = useAuthStore()
const loginFormRef = ref(null)
const prefillUsername = ref('')
const dialogTitle = computed(() => {
  if (authStore.mode === 'login') return '登录'
  if (authStore.mode === 'register') return '注册'
  return '找回密码'
})

const handleLoginSuccess = () => {
  // 登录成功，执行待处理的操作后关闭弹窗
  authStore.executePendingAction()
  authStore.close()
}

const handleRegisterSuccess = async (payload) => {
  ElMessage.success('注册成功，请登录')
  authStore.openLogin()
  await nextTick()
  if (payload?.email) {
    prefillUsername.value = payload.email
    ElMessage.info(`邮箱 ${payload.email} 已自动填充，请输入密码登录`)
  }
}

const handleResetSuccess = async (payload) => {
  ElMessage.success('密码已重置，请使用新密码登录')
  authStore.openLogin()
  await nextTick()
  if (payload?.email) {
    prefillUsername.value = payload.email
  }
}
</script>

<style scoped>
.dialog-title {
  font-size: 20px;
  font-weight: 600;
}

.dialog-body {
  padding-top: 10px;
}

.dialog-footer {
  text-align: center;
  margin-top: 16px;
  font-size: 14px;
  color: #666;
}

.dialog-footer a {
  color: #409EFF;
  text-decoration: none;
}

.dialog-footer a:hover {
  color: #66b1ff;
}
</style>

