<template>
  <div class="dashboard">
    <!-- 页面标题 -->
    <div class="page-header">
      <h1>系统首页</h1>
      <p>减灾能力评估系统 - 数据概览与快捷操作</p>
    </div>

    <!-- 系统状态卡片 -->
    <el-row :gutter="20" class="status-cards">
      <el-col :span="6">
        <el-card class="status-card">
          <div class="card-content">
            <el-icon class="card-icon" color="#409eff"><DataAnalysis /></el-icon>
            <div class="card-info">
              <div class="card-title">调查数据</div>
              <div class="card-value">{{ systemStats.surveyCount }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="status-card">
          <div class="card-content">
            <el-icon class="card-icon" color="#67c23a"><Setting /></el-icon>
            <div class="card-info">
              <div class="card-title">权重配置</div>
              <div class="card-value">{{ systemStats.weightConfigCount }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="status-card">
          <div class="card-content">
            <el-icon class="card-icon" color="#e6a23c"><TrendCharts /></el-icon>
            <div class="card-info">
              <div class="card-title">评估任务</div>
              <div class="card-value">{{ systemStats.evaluationCount }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="status-card">
          <div class="card-content">
            <el-icon class="card-icon" color="#f56c6c"><Document /></el-icon>
            <div class="card-info">
              <div class="card-title">生成报告</div>
              <div class="card-value">{{ systemStats.reportCount }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 系统信息和快捷操作 -->
    <el-row :gutter="20" class="main-content">
      <!-- 系统信息 -->
      <el-col :span="12">
        <el-card class="info-card">
          <template #header>
            <div class="card-header">
              <span>系统信息</span>
              <el-button type="text" @click="refreshSystemInfo">
                <el-icon><Refresh /></el-icon>
              </el-button>
            </div>
          </template>
          <div v-loading="loading.systemInfo">
            <div class="info-item" v-for="(value, key) in systemInfo" :key="key">
              <span class="info-label">{{ getInfoLabel(key) }}:</span>
              <span class="info-value">{{ value }}</span>
            </div>
          </div>
        </el-card>
      </el-col>

      <!-- 快捷操作 -->
      <el-col :span="12">
        <el-card class="action-card">
          <template #header>
            <span>快捷操作</span>
          </template>
          <div class="action-buttons">
            <el-button type="primary" size="large" @click="goToDataManagement">
              <el-icon><Upload /></el-icon>
              数据导入
            </el-button>
            <el-button type="success" size="large" @click="goToWeightConfig">
              <el-icon><Setting /></el-icon>
              权重配置
            </el-button>
            <el-button type="warning" size="large" @click="goToEvaluation">
              <el-icon><TrendCharts /></el-icon>
              开始评估
            </el-button>
            <el-button type="info" size="large" @click="goToResults">
              <el-icon><View /></el-icon>
              查看结果
            </el-button>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- API接口列表 -->
    <el-card class="api-card">
      <template #header>
        <span>可用API接口</span>
      </template>
      <div v-loading="loading.apis">
        <el-row :gutter="20">
          <el-col :span="8" v-for="(url, name) in apiList" :key="name">
            <div class="api-item">
              <div class="api-name">{{ name }}</div>
              <div class="api-url">{{ url }}</div>
            </div>
          </el-col>
        </el-row>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  DataAnalysis,
  Setting,
  TrendCharts,
  Document,
  Refresh,
  Upload,
  View
} from '@element-plus/icons-vue'
import { systemApi, surveyDataApi, weightConfigApi } from '@/api'

const router = useRouter()

// 响应式数据
const systemInfo = ref<Record<string, any>>({})
const apiList = ref<Record<string, string>>({})
const systemStats = reactive({
  surveyCount: 0,
  weightConfigCount: 0,
  evaluationCount: 0,
  reportCount: 0
})

const loading = reactive({
  systemInfo: false,
  apis: false
})

// 获取系统信息
const getSystemInfo = async () => {
  loading.systemInfo = true
  try {
    const response = await systemApi.getSystemInfo()
    systemInfo.value = {
      system: response.system,
      version: response.version,
      status: response.status,
      time: response.time,
      description: response.description
    }
    apiList.value = response.apis || {}
  } catch (error) {
    console.error('获取系统信息失败:', error)
    ElMessage.error('获取系统信息失败')
  } finally {
    loading.systemInfo = false
  }
}

// 获取统计数据
const getStatistics = async () => {
  try {
    // 获取调查数据数量
    const surveyResponse = await surveyDataApi.getAll()
    systemStats.surveyCount = surveyResponse.data?.length || 0
    
    // 获取权重配置数量
    const weightResponse = await weightConfigApi.getAll()
    systemStats.weightConfigCount = weightResponse.data?.length || 0
    
    // 模拟其他统计数据
    systemStats.evaluationCount = 15
    systemStats.reportCount = 8
  } catch (error) {
    console.error('获取统计数据失败:', error)
  }
}

// 刷新系统信息
const refreshSystemInfo = () => {
  getSystemInfo()
  getStatistics()
}

// 获取信息标签
const getInfoLabel = (key: string) => {
  const labels: Record<string, string> = {
    system: '系统名称',
    version: '系统版本',
    status: '运行状态',
    time: '当前时间',
    description: '系统描述'
  }
  return labels[key] || key
}

// 导航方法
const goToDataManagement = () => router.push('/data-management')
const goToWeightConfig = () => router.push('/weight-config')
const goToEvaluation = () => router.push('/evaluation')
const goToResults = () => router.push('/results')

// 组件挂载时获取数据
onMounted(() => {
  getSystemInfo()
  getStatistics()
})
</script>

<style scoped>
.dashboard {
  max-width: 1200px;
  margin: 0 auto;
}

.page-header {
  margin-bottom: 24px;
}

.page-header h1 {
  margin: 0 0 8px 0;
  font-size: 24px;
  color: #303133;
}

.page-header p {
  margin: 0;
  color: #909399;
  font-size: 14px;
}

.status-cards {
  margin-bottom: 24px;
}

.status-card {
  cursor: pointer;
  transition: all 0.3s;
}

.status-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.card-content {
  display: flex;
  align-items: center;
}

.card-icon {
  font-size: 32px;
  margin-right: 16px;
}

.card-info {
  flex: 1;
}

.card-title {
  font-size: 14px;
  color: #909399;
  margin-bottom: 4px;
}

.card-value {
  font-size: 24px;
  font-weight: bold;
  color: #303133;
}

.main-content {
  margin-bottom: 24px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.info-item {
  display: flex;
  justify-content: space-between;
  padding: 8px 0;
  border-bottom: 1px solid #f0f0f0;
}

.info-item:last-child {
  border-bottom: none;
}

.info-label {
  color: #909399;
  font-weight: 500;
}

.info-value {
  color: #303133;
}

.action-buttons {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
}

.action-buttons .el-button {
  height: 60px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}

.action-buttons .el-button .el-icon {
  margin-bottom: 4px;
  font-size: 18px;
}

.api-card {
  margin-top: 24px;
}

.api-item {
  padding: 12px;
  border: 1px solid #e4e7ed;
  border-radius: 4px;
  margin-bottom: 12px;
}

.api-name {
  font-weight: 500;
  color: #303133;
  margin-bottom: 4px;
}

.api-url {
  font-size: 12px;
  color: #909399;
  font-family: 'Courier New', monospace;
}
</style>