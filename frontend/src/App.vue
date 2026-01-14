<template>
  <div id="app">
    <el-container class="app-container" v-if="userStore.isLoggedIn || route.path === '/login'">
      <!-- 顶部导航栏 -->
      <el-header class="app-header" v-if="userStore.isLoggedIn">
        <div class="header-content">
          <div class="logo">
            <el-icon class="logo-icon"><DataAnalysis /></el-icon>
            <span class="logo-text">减灾能力评估工具</span>
          </div>
          <div class="menu-spacer"></div>
          <nav class="header-nav">
            <router-link to="/data-management" class="nav-item" :class="{ active: activeIndex === '/data-management' }">数据管理</router-link>
            <router-link to="/weight-config" class="nav-item" :class="{ active: activeIndex === '/weight-config' }">权重配置</router-link>
            <router-link v-if="userStore.isAdmin" to="/model-management" class="nav-item" :class="{ active: activeIndex === '/model-management' }">模型管理</router-link>
            <router-link to="/evaluation" class="nav-item" :class="{ active: activeIndex === '/evaluation' }">评估计算</router-link>
            <router-link to="/thematic-map" class="nav-item" :class="{ active: activeIndex === '/thematic-map' }">评估报告</router-link>
            
            <!-- 系统管理下拉菜单 -->
            <el-dropdown v-if="userStore.isAdmin" class="nav-dropdown" :class="{ active: activeIndex.startsWith('/system') }">
              <span class="nav-item dropdown-trigger">
                系统管理
                <el-icon class="el-icon--right"><ArrowDown /></el-icon>
              </span>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item @click="router.push('/system/user')">用户管理</el-dropdown-item>
                  <el-dropdown-item @click="router.push('/system/role')">角色管理</el-dropdown-item>
                  <el-dropdown-item @click="router.push('/system/organization')">组织机构管理</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </nav>
          <div class="user-section">
            <el-dropdown @command="handleUserCommand">
              <span class="user-info">
                <el-icon><User /></el-icon>
                {{ userStore.username }}
                <el-icon class="el-icon--right"><ArrowDown /></el-icon>
              </span>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item disabled>当前用户: {{ userStore.username }}</el-dropdown-item>
                  <el-dropdown-item divided command="logout">退出登录</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
        </div>
      </el-header>

      <!-- 主要内容区域 -->
      <el-main class="app-main" :class="{ 'no-header': !userStore.isLoggedIn }">
        <RouterView />
      </el-main>
    </el-container>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessageBox, ElMessage } from 'element-plus'
import { DataAnalysis, User, ArrowDown, Setting } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const activeIndex = computed(() => route.path)

const handleUserCommand = (command: string) => {
  if (command === 'logout') {
    ElMessageBox.confirm('确定要退出登录吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }).then(() => {
      userStore.logout()
      ElMessage.success('已退出登录')
      router.push('/login')
    }).catch(() => {})
  }
}
</script>

<style scoped>
.app-container {
  min-height: 100vh;
}

.app-header {
  background-color: #fff;
  border-bottom: 1px solid #e4e7ed;
  padding: 0;
}

.header-content {
  display: flex;
  align-items: center;
  height: 100%;
  max-width: 1920px;
  margin: 0 auto;
  padding: 0 20px;
}

.logo {
  display: flex;
  align-items: center;
  font-size: 20px;
  font-weight: bold;
  color: #409eff;
}

.logo-icon {
  font-size: 24px;
  margin-right: 8px;
}

.logo-text {
  color: #303133;
}

.menu-spacer {
  flex: 1;
}

.header-nav {
  display: flex;
  align-items: center;
  gap: 0;
}

.nav-item {
  padding: 0 20px;
  height: 60px;
  display: flex;
  align-items: center;
  text-decoration: none;
  color: #606266;
  font-size: 14px;
  border-bottom: 2px solid transparent;
  transition: all 0.3s;
}

.nav-item:hover {
  color: #409eff;
  background-color: #ecf5ff;
}

.nav-item.active {
  color: #409eff;
  border-bottom-color: #409eff;
}

.nav-dropdown {
  height: 60px;
  display: flex;
  align-items: center;
}

.nav-dropdown.active .dropdown-trigger {
  color: #409eff;
  border-bottom-color: #409eff;
}

.dropdown-trigger {
  cursor: pointer;
  display: flex;
  align-items: center;
}

.user-section {
  margin-left: 20px;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 8px 16px;
  background-color: #f5f7fa;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
  color: #606266;
  transition: all 0.3s;
}

.user-info:hover {
  background-color: #ecf5ff;
  color: #409eff;
}

.app-main {
  background-color: #f5f7fa;
  padding: 20px;
  min-height: calc(100vh - 60px);
}

.app-main.no-header {
  min-height: 100vh;
  padding: 0;
}
</style>
