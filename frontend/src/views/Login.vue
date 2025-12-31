<template>
  <div class="login-container">
    <div class="login-card">
      <div class="login-header">
        <h1>减灾能力评估工具</h1>
        <p>Disaster Reduction Capability Evaluation Tools</p>
      </div>

      <el-form
        ref="loginFormRef"
        :model="loginForm"
        :rules="loginRules"
        class="login-form"
        @submit.prevent="handleLogin"
      >
        <el-form-item prop="username">
          <el-select
            v-model="loginForm.username"
            placeholder="请选择用户"
            size="large"
            style="width: 100%"
            filterable
          >
            <el-option
              v-for="user in userOptions"
              :key="user.value"
              :label="user.label"
              :value="user.value"
            />
          </el-select>
        </el-form-item>

        <el-form-item prop="password">
          <el-input
            v-model="loginForm.password"
            type="password"
            placeholder="请输入密码"
            size="large"
            show-password
            @keyup.enter="handleLogin"
          >
            <template #prefix>
              <el-icon><Lock /></el-icon>
            </template>
          </el-input>
        </el-form-item>

        <el-form-item>
          <el-button
            type="primary"
            size="large"
            style="width: 100%"
            :loading="loading"
            @click="handleLogin"
          >
            登 录
          </el-button>
        </el-form-item>
      </el-form>

      <div class="login-footer">
        <p>默认密码: 123456 (admin除外)</p>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
defineOptions({ name: 'Login' })
import { ref, reactive } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { Lock } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import { getUserOptions, validateUser } from '@/config/users'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const loginFormRef = ref<FormInstance>()
const loading = ref(false)

// 从配置文件获取用户选项
const userOptions = getUserOptions()

const loginForm = reactive({
  username: '',
  password: ''
})

const loginRules: FormRules = {
  username: [{ required: true, message: '请选择用户', trigger: 'change' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

const handleLogin = async () => {
  if (!loginFormRef.value) return

  await loginFormRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        // 从配置文件验证用户
        const userConfig = validateUser(loginForm.username, loginForm.password)
        if (!userConfig) {
          ElMessage.error('用户名或密码错误')
          loading.value = false
          return
        }

        // 登录成功，保存用户信息
        await userStore.login({
          username: userConfig.value,
          isAdmin: userConfig.isAdmin || false
        })

        ElMessage.success('登录成功')

        // 跳转到数据管理页面
        const redirect = (route.query.redirect as string) || '/data-management'
        router.push(redirect)
      } catch (error) {
        console.error('登录失败:', error)
        ElMessage.error('登录失败')
      } finally {
        loading.value = false
      }
    }
  })
}
</script>

<style scoped>
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.login-card {
  width: 420px;
  padding: 40px;
  background: rgba(255, 255, 255, 0.95);
  border-radius: 16px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
  backdrop-filter: blur(10px);
}

.login-header {
  text-align: center;
  margin-bottom: 40px;
}

.login-header h1 {
  font-size: 28px;
  color: #333;
  margin: 0 0 8px 0;
  font-weight: 600;
}

.login-header p {
  font-size: 14px;
  color: #999;
  margin: 0;
}

.login-form {
  margin-top: 30px;
}

.login-form :deep(.el-form-item) {
  margin-bottom: 24px;
}

.login-footer {
  margin-top: 30px;
  text-align: center;
  color: #999;
  font-size: 13px;
}

.login-footer p {
  margin: 0;
}
</style>
