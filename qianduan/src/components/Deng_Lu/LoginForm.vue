<template>
  <el-form
    ref="formRef"
    :model="loginForm"
    :rules="loginRules"
    label-position="top"
    size="large"
    class="auth-form"
    @submit.prevent="submitLogin"
  >
    <el-form-item label="邮箱/用户名" prop="username">
      <el-input
        v-model="loginForm.username"
        placeholder="请输入邮箱或用户名"
        clearable
        :disabled="isLoading"
        prefix-icon="User"
      />
    </el-form-item>

    <el-form-item label="密码" prop="password">
      <el-input
        v-model="loginForm.password"
        type="password"
        placeholder="请输入密码"
        show-password
        :disabled="isLoading"
        prefix-icon="Lock"
        @keyup.enter="submitLogin"
      />
    </el-form-item>

    <el-form-item>
      <el-button
        type="primary"
        native-type="submit"
        class="submit-button"
        :loading="isLoading"
        :disabled="isLoading"
      >
        {{ isLoading ? '登录中...' : '登录' }}
      </el-button>
    </el-form-item>

    <div class="forgot-password">
      <a href="#" @click.prevent="handleForgotPassword">忘记密码？</a>
    </div>
  </el-form>
</template>

<script setup>
import { reactive, ref, watch } from 'vue'
import { useUserStore } from '../../stores/user'
import { useAuthStore } from '../../stores/auth'
import { ElMessage } from 'element-plus'

const props = defineProps({
  initialUsername: {
    type: String,
    default: '',
  },
})

const emit = defineEmits(['login-success'])
const userStore = useUserStore()
const authStore = useAuthStore()
const formRef = ref(null)
const isLoading = ref(false)

const loginForm = reactive({
  username: '',
  password: '',
})

watch(
  () => props.initialUsername,
  (newUsername) => {
    if (newUsername) {
      loginForm.username = newUsername
    }
  },
  { immediate: true },
)

// 表单验证规则
const loginRules = {
  username: [{ required: true, message: '请输入邮箱或用户名', trigger: 'blur' }],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度至少为6位', trigger: 'blur' },
  ],
}

const submitLogin = async () => {
  // 使用 Element Plus 表单验证
  if (!formRef.value) return

  try {
    await formRef.value.validate()
  } catch {
    return
  }

  isLoading.value = true
  try {
    await userStore.login({
      yongHuMing: loginForm.username,
      miMa: loginForm.password,
    })
    ElMessage.success('登录成功')

    // 执行待处理的操作（如购买、发布等）
    if (authStore.pendingAction) {
      authStore.executePendingAction()
    }

    emit('login-success')
  } catch (error) {
    // 后端返回的错误消息会直接显示
    ElMessage.error(error.message || '登录失败')
  } finally {
    isLoading.value = false
  }
}

const handleForgotPassword = () => {
  authStore.openResetPassword()
}
</script>

<style scoped>
.auth-form {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.submit-button {
  width: 100%;
  height: 44px;
  font-size: 16px;
  font-weight: 500;
}

.forgot-password {
  text-align: right;
  font-size: 13px;
  margin-top: -8px;
}

.forgot-password a {
  color: #00b1fe;
  text-decoration: none;
  cursor: pointer;
  transition: color 0.2s ease;
}

.forgot-password a:hover {
  color: #00b1fe;
  text-decoration: underline;
}

:deep(.el-input__wrapper) {
  padding: 12px 16px;
  border-radius: 8px;
}

:deep(.el-form-item__label) {
  font-weight: 500;
  color: #333333;
}
</style>
