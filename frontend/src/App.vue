<template>
  <div id="app">
    <el-container class="app-container">
      <!-- 顶部导航栏 -->
      <el-header class="app-header">
        <div class="header-content">
          <div class="logo">
            <el-icon class="logo-icon"><DataAnalysis /></el-icon>
            <span class="logo-text">减灾能力评估系统</span>
          </div>
          <el-menu
            :default-active="activeIndex"
            class="header-menu"
            mode="horizontal"
            @select="handleSelect"
          >
            <el-menu-item index="/dashboard">系统首页</el-menu-item>
            <el-menu-item index="/data-management">数据管理</el-menu-item>
            <el-menu-item index="/weight-config">权重配置</el-menu-item>
            <el-menu-item index="/evaluation">评估计算</el-menu-item>
            <!-- <el-menu-item index="/results">结果展示</el-menu-item> -->
            <el-menu-item index="/thematic-map">专题图</el-menu-item>
          </el-menu>
        </div>
      </el-header>
      
      <!-- 主要内容区域 -->
      <el-main class="app-main">
        <RouterView />
      </el-main>
    </el-container>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { DataAnalysis } from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()

const activeIndex = computed(() => route.path)

const handleSelect = (key: string) => {
  router.push(key)
}
</script>

<style scoped>
.app-container {
  height: 100vh;
}

.app-header {
  background-color: #fff;
  border-bottom: 1px solid #e4e7ed;
  padding: 0;
}

.header-content {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 100%;
  max-width: 1200px;
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

.header-menu {
  border-bottom: none;
}

.app-main {
  background-color: #f5f7fa;
  padding: 20px;
  min-height: calc(100vh - 60px);
}

/* 全局样式重置 */
:deep(.el-menu--horizontal > .el-menu-item) {
  border-bottom: 2px solid transparent;
}

:deep(.el-menu--horizontal > .el-menu-item.is-active) {
  border-bottom-color: #409eff;
  color: #409eff;
}
</style>
