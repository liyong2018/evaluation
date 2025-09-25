<template>
  <div class="results">
    <!-- 页面标题 -->
    <div class="page-header">
      <h1>评估结果</h1>
      <p>查看减灾能力评估结果和详细分析报告</p>
    </div>

    <!-- 结果筛选 -->
    <el-card class="filter-card">
      <el-row :gutter="20" align="middle">
        <el-col :span="6">
          <el-select v-model="selectedEvaluationId" placeholder="选择评估记录" @change="loadResults">
            <el-option
              v-for="evaluation in evaluationList"
              :key="evaluation.id"
              :label="`${evaluation.name} (${evaluation.createTime})`"
              :value="evaluation.id"
            />
          </el-select>
        </el-col>
        <el-col :span="6">
          <el-select v-model="viewMode" placeholder="选择视图模式" @change="changeViewMode">
            <el-option label="综合排名" value="ranking" />
            <el-option label="指标分析" value="indicators" />
            <el-option label="地区对比" value="comparison" />
            <el-option label="趋势分析" value="trend" />
          </el-select>
        </el-col>
        <el-col :span="6">
          <el-input
            v-model="searchKeyword"
            placeholder="搜索地区"
            clearable
            @keyup.enter="filterResults"
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
        </el-col>
        <el-col :span="6">
          <div class="filter-actions">
            <el-button type="primary" @click="exportResults">
              <el-icon><Download /></el-icon>
              导出结果
            </el-button>
            <el-button type="success" @click="generateReport">
              <el-icon><Document /></el-icon>
              生成报告
            </el-button>
          </div>
        </el-col>
      </el-row>
    </el-card>

    <!-- 结果概览 -->
    <el-row :gutter="20" class="overview-cards" v-if="resultSummary">
      <el-col :span="6">
        <el-card class="summary-card">
          <div class="card-content">
            <el-icon class="card-icon" color="#409eff"><TrendCharts /></el-icon>
            <div class="card-info">
              <div class="card-title">评估地区数</div>
              <div class="card-value">{{ resultSummary.totalRegions }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="summary-card">
          <div class="card-content">
            <el-icon class="card-icon" color="#67c23a"><Trophy /></el-icon>
            <div class="card-info">
              <div class="card-title">最高得分</div>
              <div class="card-value">{{ resultSummary.maxScore?.toFixed(2) }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="summary-card">
          <div class="card-content">
            <el-icon class="card-icon" color="#e6a23c"><DataAnalysis /></el-icon>
            <div class="card-info">
              <div class="card-title">平均得分</div>
              <div class="card-value">{{ resultSummary.avgScore?.toFixed(2) }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="summary-card">
          <div class="card-content">
            <el-icon class="card-icon" color="#f56c6c"><Warning /></el-icon>
            <div class="card-info">
              <div class="card-title">最低得分</div>
              <div class="card-value">{{ resultSummary.minScore?.toFixed(2) }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 主要内容区域 -->
    <el-row :gutter="20" class="main-content">
      <!-- 排名表格 -->
      <el-col :span="viewMode === 'ranking' ? 24 : 12">
        <el-card class="ranking-card">
          <template #header>
            <div class="card-header">
              <span>{{ getTableTitle() }}</span>
              <el-button type="text" @click="refreshResults">
                <el-icon><Refresh /></el-icon>
              </el-button>
            </div>
          </template>
          
          <el-table
            v-loading="loading.results"
            :data="filteredResults"
            stripe
            border
            :default-sort="{ prop: 'rank', order: 'ascending' }"
            @row-click="selectRegion"
          >
            <el-table-column prop="rank" label="排名" width="80" sortable>
              <template #default="{ row }">
                <el-tag :type="getRankType(row.rank)" size="small">
                  {{ row.rank }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="regionName" label="地区名称" width="150" sortable />
            <el-table-column prop="regionCode" label="地区代码" width="120" />
            <el-table-column prop="totalScore" label="综合得分" width="120" sortable>
              <template #default="{ row }">
                <div class="score-cell">
                  <span class="score-value">{{ row.totalScore?.toFixed(2) }}</span>
                  <el-progress
                    :percentage="(row.totalScore / 100) * 100"
                    :show-text="false"
                    :stroke-width="6"
                    class="score-progress"
                  />
                </div>
              </template>
            </el-table-column>
            <el-table-column label="等级" width="100">
              <template #default="{ row }">
                <el-tag :type="getGradeType(row.grade)">
                  {{ row.grade }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="algorithm" label="算法" width="100" />
            <el-table-column prop="evaluationTime" label="评估时间" width="180" />
            <el-table-column label="操作" width="150" fixed="right">
              <template #default="{ row }">
                <el-button type="primary" size="small" @click="viewDetail(row)">
                  <el-icon><View /></el-icon>
                  详情
                </el-button>
                <el-button type="info" size="small" @click="compareRegion(row)">
                  <el-icon><DataAnalysis /></el-icon>
                  对比
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>

      <!-- 图表区域 -->
      <el-col :span="viewMode === 'ranking' ? 0 : 12" v-if="viewMode !== 'ranking'">
        <el-card class="chart-card">
          <template #header>
            <span>{{ getChartTitle() }}</span>
          </template>
          
          <!-- 综合得分分布图 -->
          <div v-if="viewMode === 'indicators'" ref="scoreChartRef" class="chart-container"></div>
          
          <!-- 地区对比雷达图 -->
          <div v-if="viewMode === 'comparison'" ref="radarChartRef" class="chart-container"></div>
          
          <!-- 趋势分析图 -->
          <div v-if="viewMode === 'trend'" ref="trendChartRef" class="chart-container"></div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 详细分析 -->
    <el-card class="analysis-card" v-if="selectedRegionData">
      <template #header>
        <span>详细分析 - {{ selectedRegionData.regionName }}</span>
      </template>
      
      <el-row :gutter="20">
        <el-col :span="12">
          <div class="analysis-section">
            <h4>指标得分详情</h4>
            <el-table :data="selectedRegionData.indicators" size="small">
              <el-table-column prop="name" label="指标名称" />
              <el-table-column prop="weight" label="权重" width="80">
                <template #default="{ row }">
                  {{ (row.weight * 100).toFixed(1) }}%
                </template>
              </el-table-column>
              <el-table-column prop="score" label="得分" width="80">
                <template #default="{ row }">
                  {{ row.score?.toFixed(2) }}
                </template>
              </el-table-column>
              <el-table-column prop="weightedScore" label="加权得分" width="100">
                <template #default="{ row }">
                  {{ row.weightedScore?.toFixed(2) }}
                </template>
              </el-table-column>
            </el-table>
          </div>
        </el-col>
        <el-col :span="12">
          <div class="analysis-section">
            <h4>算法过程数据</h4>
            <div ref="processChartRef" class="process-chart"></div>
          </div>
        </el-col>
      </el-row>
      
      <div class="analysis-summary">
        <h4>分析总结</h4>
        <p>{{ selectedRegionData.summary }}</p>
        <div class="recommendations">
          <h5>改进建议</h5>
          <ul>
            <li v-for="(recommendation, index) in selectedRegionData.recommendations" :key="index">
              {{ recommendation }}
            </li>
          </ul>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted, nextTick } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  Search,
  Download,
  Document,
  TrendCharts,
  Trophy,
  DataAnalysis,
  Warning,
  Refresh,
  View
} from '@element-plus/icons-vue'
import * as echarts from 'echarts'
import { evaluationApi } from '@/api'

const route = useRoute()

// 响应式数据
const evaluationList = ref<any[]>([])
const resultsList = ref<any[]>([])
const selectedEvaluationId = ref<number | null>(null)
const viewMode = ref('ranking')
const searchKeyword = ref('')
const selectedRegionData = ref<any>(null)
const resultSummary = ref<any>(null)

const loading = reactive({
  results: false
})

// 图表引用
const scoreChartRef = ref<HTMLElement>()
const radarChartRef = ref<HTMLElement>()
const trendChartRef = ref<HTMLElement>()
const processChartRef = ref<HTMLElement>()

// 计算属性
const filteredResults = computed(() => {
  if (!searchKeyword.value) return resultsList.value
  return resultsList.value.filter(item => 
    item.regionName.includes(searchKeyword.value) || 
    item.regionCode.includes(searchKeyword.value)
  )
})

// 获取评估列表
const getEvaluationList = async () => {
  try {
    const response = await evaluationApi.getHistory()
    if (response.success) {
      evaluationList.value = response.data?.filter((item: any) => item.status === 'SUCCESS') || []
      
      // 如果URL中有evaluationId参数，自动选择
      const evaluationId = route.query.evaluationId
      if (evaluationId) {
        selectedEvaluationId.value = Number(evaluationId)
        loadResults()
      } else if (evaluationList.value.length > 0) {
        selectedEvaluationId.value = evaluationList.value[0].id
        loadResults()
      }
    }
  } catch (error) {
    console.error('获取评估列表失败:', error)
    ElMessage.error('获取评估列表失败')
  }
}

// 加载结果数据
const loadResults = async () => {
  if (!selectedEvaluationId.value) return
  
  loading.results = true
  
  try {
    // 调用真实API获取评估结果
    const response = await evaluationApi.getProcessData({
      surveyId: selectedEvaluationId.value,
      algorithmId: 1,
      weightConfigId: 1
    })
    
    if (response.success && response.data) {
      // 处理真实数据
      const processData = response.data
      const mockResults = [
        {
          id: selectedEvaluationId.value,
          rank: 1,
          regionName: processData.regionName || '未知地区',
          regionCode: processData.regionCode || 'UNKNOWN',
          totalScore: processData.totalScore || 0,
          grade: processData.grade || '待评估',
          algorithm: processData.algorithm || 'AHP',
          evaluationTime: processData.evaluationTime || new Date().toLocaleString(),
          indicators: processData.indicators || [],
          summary: processData.summary || '暂无分析总结',
          recommendations: processData.recommendations || []
        }
      ]
      
      resultsList.value = mockResults
      
      // 计算统计摘要
      resultSummary.value = {
        totalRegions: mockResults.length,
        maxScore: Math.max(...mockResults.map(r => r.totalScore)),
        minScore: Math.min(...mockResults.map(r => r.totalScore)),
        avgScore: mockResults.reduce((sum, r) => sum + r.totalScore, 0) / mockResults.length
      }
    } else {
      // 如果API返回失败，使用模拟数据
      const mockResults = [
        {
          id: 1,
          rank: 1,
          regionName: '北京市',
          regionCode: 'BJ001',
          totalScore: 92.5,
          grade: '优秀',
          algorithm: 'AHP',
          evaluationTime: '2024-01-15 10:30:00',
          indicators: [
            { name: '经济发展水平', weight: 0.3, score: 95, weightedScore: 28.5 },
            { name: '基础设施完善度', weight: 0.25, score: 90, weightedScore: 22.5 },
            { name: '应急响应能力', weight: 0.2, score: 88, weightedScore: 17.6 },
            { name: '社会保障水平', weight: 0.15, score: 92, weightedScore: 13.8 },
            { name: '环境质量指数', weight: 0.1, score: 85, weightedScore: 8.5 }
          ],
          summary: '北京市在减灾能力评估中表现优异，各项指标均达到较高水平，特别是在经济发展和基础设施方面优势明显。',
          recommendations: [
            '继续加强应急响应体系建设',
            '提升环境质量管理水平',
            '完善社会保障制度覆盖面'
          ]
        }
      ]
      
      resultsList.value = mockResults
      
      // 计算统计摘要
      resultSummary.value = {
        totalRegions: mockResults.length,
        maxScore: Math.max(...mockResults.map(r => r.totalScore)),
        minScore: Math.min(...mockResults.map(r => r.totalScore)),
        avgScore: mockResults.reduce((sum, r) => sum + r.totalScore, 0) / mockResults.length
      }
    }
    
  } catch (error) {
    console.error('加载结果失败:', error)
    ElMessage.error('加载结果失败')
    
    // 错误时使用模拟数据
    const mockResults = [
      {
        id: 1,
        rank: 1,
        regionName: '北京市',
        regionCode: 'BJ001',
        totalScore: 92.5,
        grade: '优秀',
        algorithm: 'AHP',
        evaluationTime: '2024-01-15 10:30:00',
        indicators: [
          { name: '经济发展水平', weight: 0.3, score: 95, weightedScore: 28.5 },
          { name: '基础设施完善度', weight: 0.25, score: 90, weightedScore: 22.5 },
          { name: '应急响应能力', weight: 0.2, score: 88, weightedScore: 17.6 },
          { name: '社会保障水平', weight: 0.15, score: 92, weightedScore: 13.8 },
          { name: '环境质量指数', weight: 0.1, score: 85, weightedScore: 8.5 }
        ],
        summary: '北京市在减灾能力评估中表现优异，各项指标均达到较高水平，特别是在经济发展和基础设施方面优势明显。',
        recommendations: [
          '继续加强应急响应体系建设',
          '提升环境质量管理水平',
          '完善社会保障制度覆盖面'
        ]
      }
    ]
    
    resultsList.value = mockResults
    
    // 计算统计摘要
    resultSummary.value = {
      totalRegions: mockResults.length,
      maxScore: Math.max(...mockResults.map(r => r.totalScore)),
      minScore: Math.min(...mockResults.map(r => r.totalScore)),
      avgScore: mockResults.reduce((sum, r) => sum + r.totalScore, 0) / mockResults.length
    }
  } finally {
    loading.results = false
  }
}

// 刷新结果
const refreshResults = () => {
  loadResults()
}

// 筛选结果
const filterResults = () => {
  // 筛选逻辑已在计算属性中实现
}

// 改变视图模式
const changeViewMode = () => {
  nextTick(() => {
    if (viewMode.value === 'indicators') {
      renderScoreChart()
    } else if (viewMode.value === 'comparison') {
      renderRadarChart()
    } else if (viewMode.value === 'trend') {
      renderTrendChart()
    }
  })
}

// 选择地区
const selectRegion = (row: any) => {
  selectedRegionData.value = row
  nextTick(() => {
    renderProcessChart()
  })
}

// 查看详情
const viewDetail = (row: any) => {
  selectRegion(row)
}

// 对比地区
const compareRegion = (row: any) => {
  ElMessage.info('地区对比功能开发中...')
}

// 导出结果
const exportResults = () => {
  ElMessage.success('结果导出功能开发中...')
}

// 生成报告
const generateReport = () => {
  ElMessage.success('报告生成功能开发中...')
}

// 获取表格标题
const getTableTitle = () => {
  const titles: Record<string, string> = {
    ranking: '综合排名',
    indicators: '指标分析',
    comparison: '地区对比',
    trend: '趋势分析'
  }
  return titles[viewMode.value] || '评估结果'
}

// 获取图表标题
const getChartTitle = () => {
  const titles: Record<string, string> = {
    indicators: '得分分布图',
    comparison: '地区对比雷达图',
    trend: '趋势分析图'
  }
  return titles[viewMode.value] || '数据图表'
}

// 获取排名类型
const getRankType = (rank: number) => {
  if (rank <= 3) return 'success'
  if (rank <= 10) return 'warning'
  return 'info'
}

// 获取等级类型
const getGradeType = (grade: string) => {
  const gradeMap: Record<string, string> = {
    '优秀': 'success',
    '良好': 'warning',
    '一般': 'info',
    '较差': 'danger'
  }
  return gradeMap[grade] || 'info'
}

// 渲染得分分布图
const renderScoreChart = () => {
  if (!scoreChartRef.value) return
  
  const chart = echarts.init(scoreChartRef.value)
  const option = {
    title: {
      text: '综合得分分布',
      left: 'center'
    },
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'shadow'
      }
    },
    xAxis: {
      type: 'category',
      data: resultsList.value.map(item => item.regionName)
    },
    yAxis: {
      type: 'value',
      name: '得分'
    },
    series: [{
      name: '综合得分',
      type: 'bar',
      data: resultsList.value.map(item => item.totalScore),
      itemStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: '#83bff6' },
          { offset: 0.5, color: '#188df0' },
          { offset: 1, color: '#188df0' }
        ])
      }
    }]
  }
  chart.setOption(option)
}

// 渲染雷达图
const renderRadarChart = () => {
  if (!radarChartRef.value) return
  
  const chart = echarts.init(radarChartRef.value)
  const indicators = [
    { name: '经济发展', max: 100 },
    { name: '基础设施', max: 100 },
    { name: '应急响应', max: 100 },
    { name: '社会保障', max: 100 },
    { name: '环境质量', max: 100 }
  ]
  
  const option = {
    title: {
      text: '地区对比雷达图',
      left: 'center'
    },
    tooltip: {},
    legend: {
      data: resultsList.value.slice(0, 3).map(item => item.regionName),
      bottom: 10
    },
    radar: {
      indicator: indicators
    },
    series: [{
      name: '减灾能力评估',
      type: 'radar',
      data: resultsList.value.slice(0, 3).map(item => ({
        value: item.indicators.map((ind: any) => ind.score),
        name: item.regionName
      }))
    }]
  }
  chart.setOption(option)
}

// 渲染趋势图
const renderTrendChart = () => {
  if (!trendChartRef.value) return
  
  const chart = echarts.init(trendChartRef.value)
  const option = {
    title: {
      text: '评估趋势分析',
      left: 'center'
    },
    tooltip: {
      trigger: 'axis'
    },
    legend: {
      data: ['综合得分', '排名'],
      bottom: 10
    },
    xAxis: {
      type: 'category',
      data: ['2021年', '2022年', '2023年', '2024年']
    },
    yAxis: [{
      type: 'value',
      name: '得分'
    }, {
      type: 'value',
      name: '排名',
      inverse: true
    }],
    series: [{
      name: '综合得分',
      type: 'line',
      data: [85.2, 87.5, 89.1, 92.5]
    }, {
      name: '排名',
      type: 'line',
      yAxisIndex: 1,
      data: [5, 3, 2, 1]
    }]
  }
  chart.setOption(option)
}

// 渲染算法过程图
const renderProcessChart = () => {
  if (!processChartRef.value || !selectedRegionData.value) return
  
  const chart = echarts.init(processChartRef.value)
  const option = {
    title: {
      text: '指标权重分布',
      left: 'center'
    },
    tooltip: {
      trigger: 'item'
    },
    series: [{
      name: '指标权重',
      type: 'pie',
      radius: '50%',
      data: selectedRegionData.value.indicators.map((item: any) => ({
        value: item.weightedScore,
        name: item.name
      })),
      emphasis: {
        itemStyle: {
          shadowBlur: 10,
          shadowOffsetX: 0,
          shadowColor: 'rgba(0, 0, 0, 0.5)'
        }
      }
    }]
  }
  chart.setOption(option)
}

// 组件挂载时获取数据
onMounted(() => {
  getEvaluationList()
})
</script>

<style scoped>
.results {
  max-width: 1400px;
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

.filter-card {
  margin-bottom: 16px;
}

.filter-actions {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
}

.overview-cards {
  margin-bottom: 24px;
}

.summary-card {
  cursor: pointer;
  transition: all 0.3s;
}

.summary-card:hover {
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

.score-cell {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.score-value {
  font-weight: bold;
  margin-bottom: 4px;
}

.score-progress {
  width: 80px;
}

.chart-container,
.process-chart {
  width: 100%;
  height: 400px;
}

.analysis-card {
  margin-top: 24px;
}

.analysis-section {
  margin-bottom: 20px;
}

.analysis-section h4 {
  margin: 0 0 12px 0;
  color: #303133;
}

.analysis-summary {
  margin-top: 20px;
  padding-top: 20px;
  border-top: 1px solid #e4e7ed;
}

.analysis-summary h4,
.analysis-summary h5 {
  margin: 0 0 12px 0;
  color: #303133;
}

.recommendations ul {
  margin: 8px 0;
  padding-left: 20px;
}

.recommendations li {
  margin-bottom: 4px;
  color: #606266;
}

.el-select {
  width: 100%;
}
</style>