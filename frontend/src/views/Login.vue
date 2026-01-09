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
          <el-input
            v-model="loginForm.username"
            placeholder="请输入用户名"
            size="large"
            @keyup.enter="handleLogin"
          >
            <template #prefix>
              <el-icon><User /></el-icon>
            </template>
          </el-input>
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
          <div style="display: flex; gap: 12px; width: 100%;">
            <el-button
              type="primary"
              size="large"
              style="flex: 1"
              :loading="loading"
              @click="handleLogin"
            >
              登 录
            </el-button>
            <el-button
              type="success"
              size="large"
              style="flex: 1"
              @click="showRegisterDialog = true"
            >
              注 册
            </el-button>
          </div>
        </el-form-item>
      </el-form>
    </div>

    <!-- 注册对话框 -->
    <el-dialog
      v-model="showRegisterDialog"
      title="用户注册"
      width="400px"
      :close-on-click-modal="false"
    >
      <el-form
        ref="registerFormRef"
        :model="registerForm"
        :rules="registerRules"
        label-width="80px"
      >
        <el-form-item label="用户名" prop="username">
          <el-input
            v-model="registerForm.username"
            placeholder="请输入用户名"
            @keyup.enter="handleRegister"
          />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input
            v-model="registerForm.password"
            type="password"
            placeholder="请输入密码"
            show-password
            @keyup.enter="handleRegister"
          />
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input
            v-model="registerForm.confirmPassword"
            type="password"
            placeholder="请再次输入密码"
            show-password
            @keyup.enter="handleRegister"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showRegisterDialog = false">取 消</el-button>
        <el-button type="primary" :loading="registerLoading" @click="handleRegister">
          注 册
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
defineOptions({ name: 'LoginView' })
import { ref, reactive } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { Lock, User } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import { userApi, roleApi } from '@/api'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const loginFormRef = ref<FormInstance>()
const registerFormRef = ref<FormInstance>()
const loading = ref(false)
const registerLoading = ref(false)
const showRegisterDialog = ref(false)

const loginForm = reactive({
  username: '',
  password: ''
})

const registerForm = reactive({
  username: '',
  password: '',
  confirmPassword: ''
})

// 自定义验证确认密码
const validateConfirmPassword = (rule: any, value: any, callback: any) => {
  if (value === '') {
    callback(new Error('请再次输入密码'))
  } else if (value !== registerForm.password) {
    callback(new Error('两次输入密码不一致'))
  } else {
    callback()
  }
}

// 自定义验证用户名是否存在（异步调用后端）
const validateUsernameExists = (rule: any, value: any, callback: any) => {
  if (!value) {
    callback()
    return
  }
  userApi.checkExists(value)
    .then((res: any) => {
      if (res?.data === true) {
        callback(new Error('用户名已存在'))
        return
      }
      callback()
    })
    .catch(() => {
      callback()
    })
}

const loginRules: FormRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

const registerRules: FormRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 2, max: 20, message: '用户名长度在 2 到 20 个字符', trigger: 'blur' },
    { validator: validateUsernameExists, trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于 6 个字符', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入密码', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' }
  ]
}

const handleLogin = async () => {
  if (!loginFormRef.value) return

  await loginFormRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        const res = await userApi.login({
          username: loginForm.username,
          password: loginForm.password
        })

        if (res.code === 200 && res.data) {
          const userId = res.data.id

          // 检查用户是否有关联的组织机构
          try {
            // 1. 获取用户角色
            const rolesRes = await userApi.getUserRoles(userId)
            if (rolesRes.success && rolesRes.data && rolesRes.data.length > 0) {
              const roleId = rolesRes.data[0]

              // 2. 获取角色关联的组织机构
              const orgsRes = await roleApi.getRoleOrganizations(roleId)
              if (orgsRes.success && orgsRes.data && orgsRes.data.length > 0) {
                // 用户有关联的组织机构，允许登录
                await userStore.login({
                  id: userId,
                  username: res.data.username,
                  isAdmin: res.data.isAdmin || false
                })

                ElMessage.success('登录成功')

                const redirect = (route.query.redirect as string) || '/data-management'
                router.push(redirect)
              } else {
                // 用户没有关联任何组织机构，拒绝登录
                ElMessage.error('您尚未分配任何组织机构权限，无法登录系统。请联系管理员。')
              }
            } else {
              // 用户没有分配任何角色，拒绝登录
              ElMessage.error('您尚未分配任何角色，无法登录系统。请联系管理员。')
            }
          } catch (orgCheckError) {
            console.error('检查组织机构权限失败:', orgCheckError)
            // 权限检查失败，拒绝登录以确保安全
            ElMessage.error('登录验证失败，请稍后重试或联系管理员。')
          }
        } else {
          ElMessage.error(res.msg || '用户名或密码错误')
        }
      } catch (error: any) {
        console.error('登录失败:', error)
        ElMessage.error(error?.response?.data?.msg || '登录失败')
      } finally {
        loading.value = false
      }
    }
  })
}

const handleRegister = async () => {
  if (!registerFormRef.value) return

  await registerFormRef.value.validate(async (valid) => {
    if (valid) {
      registerLoading.value = true
      try {
        const res = await userApi.register({
          username: registerForm.username,
          password: registerForm.password
        })

        if (res.code === 200) {
          const newUsername = registerForm.username
          ElMessage.success('注册成功，请登录')
          showRegisterDialog.value = false
          // 清空注册表单
          registerForm.username = ''
          registerForm.password = ''
          registerForm.confirmPassword = ''
          // 预填登录表单
          loginForm.username = newUsername
          loginForm.password = ''
        } else {
          ElMessage.error(res.msg || '注册失败')
        }
      } catch (error: any) {
        console.error('注册失败:', error)
        ElMessage.error(error?.response?.data?.msg || '注册失败')
      } finally {
        registerLoading.value = false
      }
    }
  })
}

// 监听注册对话框关闭，重置表单
const handleDialogClose = () => {
  registerFormRef.value?.resetFields()
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
