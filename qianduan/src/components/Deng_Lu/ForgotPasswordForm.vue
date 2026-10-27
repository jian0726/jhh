<template>
  <el-form
    ref="formRef"
    :model="resetForm"
    :rules="resetRules"
    label-position="top"
    size="large"
    class="auth-form"
    @submit.prevent="submitReset"
  >
    <el-form-item label="邮箱" prop="email">
      <el-input
        v-model="resetForm.email"
        placeholder="请输入注册邮箱"
        clearable
        type="email"
        :disabled="isLoading"
        prefix-icon="Message"
        @blur="handleEmailBlur"
      />
    </el-form-item>

    <el-form-item label="验证码" prop="verifyCode">
      <el-input
        v-model="resetForm.verifyCode"
        placeholder="请输入邮箱验证码"
        clearable
        :disabled="isLoading"
        prefix-icon="Key"
      >
        <template #append>
          <el-button
            @click="sendVerifyCode"
            :disabled="!isEmailValid || isCodeSending || countdownSeconds > 0 || isLoading"
            class="send-code-btn"
          >
            {{ countdownText }}
          </el-button>
        </template>
      </el-input>
    </el-form-item>

    <el-form-item label="新密码" prop="password">
      <el-input
        v-model="resetForm.password"
        type="password"
        placeholder="请输入新密码（至少6位）"
        show-password
        :disabled="isLoading"
        prefix-icon="Lock"
      />
    </el-form-item>

    <el-form-item label="确认密码" prop="confirmPassword">
      <el-input
        v-model="resetForm.confirmPassword"
        type="password"
        placeholder="请再次输入新密码"
        show-password
        :disabled="isLoading"
        prefix-icon="Lock"
        @keyup.enter="submitReset"
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
        {{ isLoading ? '重置中...' : '重置密码' }}
      </el-button>
    </el-form-item>
  </el-form>
</template>

<script setup>
import { reactive, ref, computed } from 'vue'
import { authApi } from '../../api/auth'
import { ElMessage } from 'element-plus'

const emit = defineEmits(['reset-success'])
const formRef = ref(null)
const isLoading = ref(false)
const isCodeSending = ref(false)
const countdownSeconds = ref(0)

const resetForm = reactive({
  email: '',
  verifyCode: '',
  password: '',
  confirmPassword: ''
})

const isEmailValid = computed(() => {
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
  return emailRegex.test(resetForm.email)
})

const countdownText = computed(() => {
  if (countdownSeconds.value > 0) {
    return `${countdownSeconds.value}s`
  }
  return '发送验证码'
})

const validatePassword = (rule, value, callback) => {
  if (!value) {
    callback(new Error('请输入新密码'))
  } else if (value.length < 6) {
    callback(new Error('密码长度至少为6位'))
  } else {
    callback()
  }
}

const validateConfirmPassword = (rule, value, callback) => {
  if (!value) {
    callback(new Error('请再次输入新密码'))
  } else if (value !== resetForm.password) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const resetRules = {
  email: [
    { required: true, message: '请输入邮箱地址', trigger: 'blur' },
    { type: 'email', message: '请输入有效邮箱地址', trigger: 'blur' }
  ],
  verifyCode: [
    { required: true, message: '请输入验证码', trigger: 'blur' }
  ],
  password: [
    { validator: validatePassword, trigger: 'blur' }
  ],
  confirmPassword: [
    { validator: validateConfirmPassword, trigger: 'blur' }
  ]
}

const handleEmailBlur = () => {
  if (resetForm.email && !isEmailValid.value) {
    formRef.value?.validateField('email')
  }
}

const sendVerifyCode = async () => {
  if (!isEmailValid.value) {
    ElMessage.warning('请输入有效的邮箱地址')
    formRef.value?.validateField('email')
    return
  }

  isCodeSending.value = true
  try {
    const response = await authApi.sendVerifyCode({ email: resetForm.email })
    if (response && response.success) {
      ElMessage.success('验证码已发送，请检查邮箱')
      startCountdown()
    } else {
      ElMessage.error(response?.message || '发送验证码失败')
    }
  } catch (error) {
    ElMessage.error(error.message || '发送验证码失败')
  } finally {
    isCodeSending.value = false
  }
}

const startCountdown = () => {
  countdownSeconds.value = 60
  const timer = setInterval(() => {
    countdownSeconds.value--
    if (countdownSeconds.value <= 0) {
      clearInterval(timer)
    }
  }, 1000)
}

const submitReset = async () => {
  if (!formRef.value) return

  try {
    await formRef.value.validate()
  } catch {
    return
  }

  isLoading.value = true
  try {
    const response = await authApi.resetPassword({
      email: resetForm.email,
      verifyCode: resetForm.verifyCode,
      miMa: resetForm.password
    })
    if (response && response.success) {
      emit('reset-success', { email: resetForm.email })
    } else {
      ElMessage.error(response?.message || '重置密码失败')
    }
  } catch (error) {
    ElMessage.error(error.message || '重置密码失败')
  } finally {
    isLoading.value = false
  }
}
</script>

<style scoped>
.auth-form {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.send-code-btn {
  white-space: nowrap;
  min-width: 110px;
  height: 32px;
  font-size: 13px;
}

.submit-button {
  width: 100%;
  height: 44px;
  font-size: 16px;
  font-weight: 500;
}

:deep(.el-input__wrapper) {
  padding: 12px 16px;
  border-radius: 8px;
}

:deep(.el-input-group__append) {
  padding: 0;
  background-color: transparent;
  border-left: none;
}

:deep(.el-input-group__append .el-button) {
  margin: 0;
  height: 100%;
  border-radius: 0 8px 8px 0;
}

:deep(.el-form-item__label) {
  font-weight: 500;
  color: #303133;
}
</style>
