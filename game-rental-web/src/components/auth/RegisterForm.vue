<template>
  <div class="register-form-wrapper">
    <!-- 注册方式切换 -->
    <div class="tab-switch">
      <button :class="['tab-btn', { active: registerMode === 'phone' }]" @click="registerMode = 'phone'" type="button">
        手机号注册
      </button>
      <button :class="['tab-btn', { active: registerMode === 'email' }]" @click="registerMode = 'email'" type="button">
        邮箱注册
      </button>
    </div>

    <!-- 手机号注册 -->
    <el-form
      v-if="registerMode === 'phone'"
      ref="phoneFormRef"
      :model="phoneForm"
      :rules="phoneRules"
      label-position="top"
      size="large"
      class="auth-form"
      @submit.prevent="submitPhoneRegister"
    >
      <el-form-item label="用户名" prop="username">
        <el-input v-model="phoneForm.username" placeholder="请输入用户名" clearable :disabled="isLoading" />
      </el-form-item>
      <el-form-item label="手机号" prop="phone">
        <el-input v-model="phoneForm.phone" placeholder="请输入手机号" clearable :disabled="isLoading" />
      </el-form-item>
      <el-form-item label="短信验证码" prop="smsCode">
        <el-input v-model="phoneForm.smsCode" placeholder="请输入短信验证码" clearable :disabled="isLoading">
          <template #append>
            <el-button
              @click="sendSmsCode"
              :disabled="!isPhoneValid || isCodeSending || countdownSeconds > 0 || isLoading"
              class="send-code-btn"
            >
              {{ countdownText }}
            </el-button>
          </template>
        </el-input>
      </el-form-item>
      <el-form-item label="密码" prop="password">
        <el-input v-model="phoneForm.password" type="password" placeholder="请输入密码（至少6位）" show-password :disabled="isLoading" />
      </el-form-item>
      <el-form-item label="确认密码" prop="confirmPassword">
        <el-input v-model="phoneForm.confirmPassword" type="password" placeholder="请再次输入密码" show-password :disabled="isLoading" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" native-type="submit" class="submit-button" :loading="isLoading" :disabled="isLoading">
          {{ isLoading ? '注册中...' : '立即注册' }}
        </el-button>
      </el-form-item>
    </el-form>

    <!-- 邮箱注册 -->
    <el-form
      v-else
      ref="emailFormRef"
      :model="emailForm"
      :rules="emailRules"
      label-position="top"
      size="large"
      class="auth-form"
      @submit.prevent="submitEmailRegister"
    >
      <el-form-item label="邮箱" prop="email">
        <el-input v-model="emailForm.email" placeholder="请输入邮箱地址" clearable type="email" :disabled="isLoading" @blur="handleEmailBlur" />
      </el-form-item>
      <el-form-item label="验证码" prop="verifyCode">
        <el-input v-model="emailForm.verifyCode" placeholder="请输入邮箱验证码" clearable :disabled="isLoading">
          <template #append>
            <el-button
              @click="sendEmailCode"
              :disabled="!isEmailValid || isCodeSending || countdownSeconds > 0 || isLoading"
              class="send-code-btn"
            >
              {{ countdownText }}
            </el-button>
          </template>
        </el-input>
      </el-form-item>
      <el-form-item label="密码" prop="password">
        <el-input v-model="emailForm.password" type="password" placeholder="请输入密码（至少6位）" show-password :disabled="isLoading" />
      </el-form-item>
      <el-form-item label="确认密码" prop="confirmPassword">
        <el-input v-model="emailForm.confirmPassword" type="password" placeholder="请再次输入密码" show-password :disabled="isLoading" @keyup.enter="submitEmailRegister" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" native-type="submit" class="submit-button" :loading="isLoading" :disabled="isLoading">
          {{ isLoading ? '注册中...' : '立即注册' }}
        </el-button>
      </el-form-item>
    </el-form>
  </div>
</template>

<script setup>
import { reactive, ref, computed } from 'vue'
import { authApi } from '../../api/auth'
import { ElMessage } from 'element-plus'

const emit = defineEmits(['register-success'])
const phoneFormRef = ref(null)
const emailFormRef = ref(null)
const isLoading = ref(false)
const isCodeSending = ref(false)
const countdownSeconds = ref(0)
const registerMode = ref('phone')

const phoneForm = reactive({
  username: '',
  phone: '',
  smsCode: '',
  password: '',
  confirmPassword: ''
})

const emailForm = reactive({
  email: '',
  verifyCode: '',
  password: '',
  confirmPassword: ''
})

// 手机号格式校验
const isPhoneValid = computed(() => {
  return /^1[3-9]\d{9}$/.test(phoneForm.phone)
})

// 邮箱格式校验
const isEmailValid = computed(() => {
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
  return emailRegex.test(emailForm.email)
})

// 倒计时文本
const countdownText = computed(() => {
  if (countdownSeconds.value > 0) return `${countdownSeconds.value}s`
  return '发送验证码'
})

// 密码验证器
const makePasswordValidator = (formObj) => (rule, value, callback) => {
  if (!value) {
    callback(new Error('请输入密码'))
  } else if (value.length < 6) {
    callback(new Error('密码长度至少为6位'))
  } else {
    callback()
  }
}

// 确认密码验证器
const makeConfirmValidator = (formObj) => (rule, value, callback) => {
  if (!value) {
    callback(new Error('请再次输入密码'))
  } else if (value !== formObj.password) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

// 手机号注册表单规则
const phoneRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入有效的手机号', trigger: 'blur' }
  ],
  smsCode: [{ required: true, message: '请输入短信验证码', trigger: 'blur' }],
  password: [{ validator: makePasswordValidator(phoneForm), trigger: 'blur' }],
  confirmPassword: [{ validator: makeConfirmValidator(phoneForm), trigger: 'blur' }]
}

// 邮箱注册表单规则
const emailRules = {
  email: [
    { required: true, message: '请输入邮箱地址', trigger: 'blur' },
    { type: 'email', message: '请输入有效的邮箱地址', trigger: 'blur' }
  ],
  verifyCode: [{ required: true, message: '请输入验证码', trigger: 'blur' }],
  password: [{ validator: makePasswordValidator(emailForm), trigger: 'blur' }],
  confirmPassword: [{ validator: makeConfirmValidator(emailForm), trigger: 'blur' }]
}

const handleEmailBlur = () => {
  if (emailForm.email && !isEmailValid.value) {
    emailFormRef.value?.validateField('email')
  }
}

const startCountdown = () => {
  countdownSeconds.value = 60
  const timer = setInterval(() => {
    countdownSeconds.value--
    if (countdownSeconds.value <= 0) clearInterval(timer)
  }, 1000)
}

// 发送短信验证码
const sendSmsCode = async () => {
  if (!isPhoneValid.value) {
    ElMessage.warning('请输入有效的手机号')
    return
  }
  isCodeSending.value = true
  try {
    await authApi.sendCode(phoneForm.phone)
    ElMessage.success('验证码已发送')
    startCountdown()
  } catch (error) {
    ElMessage.error(error.message || '发送验证码失败')
  } finally {
    isCodeSending.value = false
  }
}

// 发送邮箱验证码
const sendEmailCode = async () => {
  if (!isEmailValid.value) {
    ElMessage.warning('请输入有效的邮箱地址')
    emailFormRef.value?.validateField('email')
    return
  }
  isCodeSending.value = true
  try {
    await authApi.sendVerifyCode({ email: emailForm.email })
    ElMessage.success('验证码已发送，请检查邮箱')
    startCountdown()
  } catch (error) {
    ElMessage.error(error.message || '发送验证码失败')
  } finally {
    isCodeSending.value = false
  }
}

// 手机号提交注册
const submitPhoneRegister = async () => {
  if (!phoneFormRef.value) return
  try {
    await phoneFormRef.value.validate()
  } catch { return }

  isLoading.value = true
  try {
    const response = await authApi.register({
      yongHuMing: phoneForm.username,
      shouJiHao: phoneForm.phone,
      miMa: phoneForm.password,
      verifyCode: phoneForm.smsCode
    })
    // 后端成功返回 {token, yongHu}，直接跳转登录
    ElMessage.success('注册成功，请登录')
    emit('register-success', { phone: phoneForm.phone })
  } catch (error) {
    ElMessage.error(error.message || '注册失败')
  } finally {
    isLoading.value = false
  }
}

// 邮箱提交注册
const submitEmailRegister = async () => {
  if (!emailFormRef.value) return
  try {
    await emailFormRef.value.validate()
  } catch { return }

  isLoading.value = true
  try {
    await authApi.register({
      email: emailForm.email,
      verifyCode: emailForm.verifyCode,
      miMa: emailForm.password
    })
    // 后端成功返回 {token, yongHu}
    ElMessage.success('注册成功，请登录')
    emit('register-success', { email: emailForm.email })
  } catch (error) {
    ElMessage.error(error.message || '注册失败')
  } finally {
    isLoading.value = false
  }
}
</script>

<style scoped>
.register-form-wrapper {
  display: flex;
  flex-direction: column;
}

.tab-switch {
  display: flex;
  margin-bottom: 24px;
  border-bottom: 1px solid #eee;
}

.tab-btn {
  flex: 1;
  height: 40px;
  background: none;
  border: none;
  font-size: 15px;
  color: #666;
  cursor: pointer;
  position: relative;
  transition: color 0.2s ease;
}

.tab-btn:hover {
  color: #409EFF;
}

.tab-btn.active {
  color: #409EFF;
  font-weight: 500;
}

.tab-btn.active::after {
  content: '';
  position: absolute;
  bottom: -1px;
  left: 20%;
  right: 20%;
  height: 2px;
  background-color: #409EFF;
}

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

