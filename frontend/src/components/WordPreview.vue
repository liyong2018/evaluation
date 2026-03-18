<template>
  <div class="word-preview-container">
    <div class="preview-header">
      <h2>减灾能力评估技术报告预览</h2>
      <div class="header-actions">
        <el-button @click="refreshPreview" icon="Refresh" :loading="loading">
          刷新预览
        </el-button>
        <el-button @click="downloadWord" type="primary" icon="Download">
          下载Word文档
        </el-button>
        <el-button @click="$emit('close')" icon="Close">关闭预览</el-button>
      </div>
    </div>

    <div class="preview-content" v-loading="loading">
      <div v-if="previewData" class="document-preview">
        <!-- 报告标题 -->
        <div class="report-header">
          <h1 class="report-title">{{ previewData.metadata?.title }}</h1>
          <div class="report-meta">
            <p><strong>评估单位：</strong>{{ previewData.metadata?.organization }}</p>
            <p><strong>生成时间：</strong>{{ previewData.metadata?.generateTime }}</p>
            <p><strong>技术支持：</strong>{{ previewData.metadata?.technicalSupport }}</p>
          </div>
        </div>

        <!-- 评估概况 -->
        <div class="report-section">
          <h2>一、评估概况</h2>
          <p>本次评估针对青神县各乡镇的减灾能力进行了全面分析。评估采用了实地调研、数据统计分析等方法，结合{{ currentYear }}年最新数据，对各乡镇的减灾能力进行了量化评估和等级划分。</p>
        </div>

        <!-- 评估结果统计 -->
        <div class="report-section">
          <h2>二、评估结果统计</h2>

          <!-- 乡镇级统计 -->
          <div class="statistics-subsection">
            <h3>1. 乡镇级统计</h3>
            <div class="stats-summary">
              <p>青神县共<strong>{{ previewData.statistics?.totalTownships }}</strong>个乡镇参与评估。</p>
            </div>

            <div class="stats-grid">
              <div class="stat-card" v-for="level in previewData.statistics?.townshipData" :key="level.level">
                <div class="stat-level" :class="getLevelClass(level.level)">{{ level.level }}</div>
                <div class="stat-count">{{ level.count }}个</div>
                <div class="stat-percent">{{ level.percent.toFixed(2) }}%</div>
              </div>
            </div>
          </div>

          <!-- 社区级统计 -->
          <div class="statistics-subsection">
            <h3>2. 社区级统计</h3>
            <div class="stats-summary">
              <p>全县共<strong>{{ previewData.statistics?.totalCommunities }}</strong>个社区（行政村）参与评估。</p>
            </div>

            <div class="stats-grid">
              <div class="stat-card" v-for="level in previewData.statistics?.communityData" :key="level.level">
                <div class="stat-level" :class="getLevelClass(level.level)">{{ level.level }}</div>
                <div class="stat-count">{{ level.count }}个</div>
                <div class="stat-percent">{{ level.percent.toFixed(2) }}%</div>
              </div>
            </div>
          </div>

          <!-- 统计图表 -->
          <div class="statistics-subsection">
            <h3>3. 能力等级分布图</h3>
            <div class="chart-container">
              <div ref="townshipChart" class="chart"></div>
              <div ref="communityChart" class="chart"></div>
            </div>
          </div>
        </div>

        <!-- 评估结论 -->
        <div class="report-section">
          <h2>三、评估结论</h2>
          <div class="conclusion-content">
            <p v-for="(paragraph, index) in formatConclusion(previewData.statistics?.conclusion)" :key="index">
              {{ paragraph }}
            </p>

            <!-- 详细统计 -->
            <div class="detailed-stats">
              <h4>详细统计：</h4>
              <ul>
                <li v-for="level in previewData.statistics?.townshipData" :key="'detail-' + level.level">
                  能力等级为"<span class="level-highlight" :class="getLevelClass(level.level)">{{ level.level }}</span>"的乡镇有
                  <strong>{{ level.count }}</strong>个，占比<strong>{{ level.percent.toFixed(2) }}%</strong>。
                  {{ getLevelDescription(level.level, level.count) }}
                </li>
              </ul>
            </div>
          </div>
        </div>

        <!-- 技术说明 -->
        <div class="report-section">
          <h2>四、技术说明</h2>
          <div class="technical-notes">
            <p><strong>1. 评估方法：</strong>采用TOPSIS算法结合实地调研数据进行综合评估</p>
            <p><strong>2. 数据来源：</strong>各乡镇上报的减灾能力调查数据</p>
            <p><strong>3. 评估标准：</strong>依据国家减灾委员会制定的减灾能力评估指标体系</p>
            <p><strong>4. 技术支持：</strong>减灾能力评估系统</p>
          </div>
        </div>

        <!-- 报告生成时间 -->
        <div class="report-footer">
          <p class="generate-time">报告生成时间：{{ new Date().toLocaleString('zh-CN') }}</p>
        </div>
      </div>

      <div v-else-if="!loading" class="no-data">
        <el-empty description="暂无预览数据" />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, nextTick, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { wordTemplateApi } from '@/api'
import * as echarts from 'echarts'

// 定义组件属性
interface Props {
  year?: number
  orgCode?: string
}

const props = withDefaults(defineProps<Props>(), {
  year: new Date().getFullYear(),
  orgCode: '511425'
})

// 定义组件事件
const emit = defineEmits(['close'])

// 响应式数据
const loading = ref(false)
const previewData = ref<any>(null)
const townshipChart = ref<HTMLElement>()
const communityChart = ref<HTMLElement>()
const currentYear = ref(props.year)

// 监听年份和区域变化
watch(() => [props.year, props.orgCode], ([newYear]) => {
  currentYear.value = newYear as number
  loadPreviewData()
})

// 获取预览数据
const loadPreviewData = async () => {
  loading.value = true
  try {
    const response = await wordTemplateApi.previewReport(props.year, props.orgCode)
    if (response.code === 200) {
      previewData.value = response.data
      await nextTick()
      renderCharts()
    } else {
      ElMessage.error(response.msg || '获取预览数据失败')
    }
  } catch (error) {
    console.error('加载预览数据失败:', error)
    ElMessage.error('加载预览数据失败')
  } finally {
    loading.value = false
  }
}

// 刷新预览
const refreshPreview = () => {
  loadPreviewData()
}

// 下载Word文档
const downloadWord = async () => {
  const loadingMessage = ElMessage({
    message: '正在生成Word文档，请稍候...',
    type: 'info',
    duration: 0
  })
  try {
    const response = await wordTemplateApi.generateReport(props.year, props.orgCode)

    // 从响应中获取数据和文件名
    const blobData = response.data || response
    let fileName = `青神县减灾能力评估技术报告_${new Date().getFullYear()}.docx`

    // 尝试从响应头中获取文件名
    if (response.headers) {
      const contentDisposition = response.headers['content-disposition'] || response.headers['Content-Disposition']
      if (contentDisposition) {
        const filenameStarMatch = /filename\*=UTF-8''([^;]+)/i.exec(contentDisposition)
        if (filenameStarMatch && filenameStarMatch[1]) {
          try {
            fileName = decodeURIComponent(filenameStarMatch[1])
          } catch (e) {
            console.warn('解码文件名失败，使用默认文件名', e)
          }
        }
      }
    }

    const blob = blobData instanceof Blob ? blobData : new Blob([blobData], {
      type: 'application/vnd.openxmlformats-officedocument.wordprocessingml.document'
    })

    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = fileName
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    window.URL.revokeObjectURL(url)

    ElMessage.success('Word文档生成成功！')
  } catch (error) {
    console.error('下载Word文档失败:', error)
    ElMessage.error('生成Word文档失败，请重试')
  } finally {
    loadingMessage.close()
  }
}

// 获取等级样式类
const getLevelClass = (level: string) => {
  switch (level) {
    case '强': return 'level-strong'
    case '较强': return 'level-medium-strong'
    case '中等': return 'level-medium'
    case '较弱': return 'level-weak'
    case '弱': return 'level-very-weak'
    default: return 'level-medium'
  }
}

// 获取等级描述
const getLevelDescription = (level: string, count: number) => {
  if (count === 0) return ''

  switch (level) {
    case '强':
      return '这些乡镇在防灾减灾救灾方面表现突出，基础设施完善，应急响应机制健全。'
    case '较强':
      return '这些乡镇具备较好的减灾能力，在某些方面仍有提升空间。'
    case '中等':
      return '这些乡镇减灾能力处于平均水平，需要进一步加强基础设施建设和应急管理体系完善。'
    case '较弱':
    case '弱':
      return '这些乡镇减灾能力相对薄弱，需要重点关注和加强。'
    default:
      return ''
  }
}

// 格式化评估结论
const formatConclusion = (conclusion: string) => {
  if (!conclusion) return []

  // 按句号或分号分割段落
  return conclusion.split(/[。；]/).filter(p => p.trim()).map(p => p + '。')
}

// 渲染图表
const renderCharts = () => {
  if (!previewData.value) return

  // 乡镇级统计图表
  if (townshipChart.value) {
    const townshipChartInstance = echarts.init(townshipChart.value)
    const townshipOption = {
      title: {
        text: '乡镇级减灾能力分布',
        left: 'center'
      },
      tooltip: {
        trigger: 'item',
        formatter: '{a} <br/>{b}: {c} ({d}%)'
      },
      series: [
        {
          name: '乡镇能力等级',
          type: 'pie',
          radius: '60%',
          data: previewData.value.statistics?.townshipData?.map((item: any) => ({
            value: item.count,
            name: `${item.level} (${item.count}个)`
          })),
          emphasis: {
            itemStyle: {
              shadowBlur: 10,
              shadowOffsetX: 0,
              shadowColor: 'rgba(0, 0, 0, 0.5)'
            }
          }
        }
      ]
    }
    townshipChartInstance.setOption(townshipOption)
  }

  // 社区级统计图表
  if (communityChart.value) {
    const communityChartInstance = echarts.init(communityChart.value)
    const communityOption = {
      title: {
        text: '社区级减灾能力分布',
        left: 'center'
      },
      tooltip: {
        trigger: 'item',
        formatter: '{a} <br/>{b}: {c} ({d}%)'
      },
      series: [
        {
          name: '社区能力等级',
          type: 'pie',
          radius: '60%',
          data: previewData.value.statistics?.communityData?.map((item: any) => ({
            value: item.count,
            name: `${item.level} (${item.count}个)`
          })),
          emphasis: {
            itemStyle: {
              shadowBlur: 10,
              shadowOffsetX: 0,
              shadowColor: 'rgba(0, 0, 0, 0.5)'
            }
          }
        }
      ]
    }
    communityChartInstance.setOption(communityOption)
  }
}

// 组件挂载时加载数据
onMounted(() => {
  loadPreviewData()
})
</script>

<style scoped>
.word-preview-container {
  position: fixed;
  top: 0;
  left: 50%;
  transform: translateX(-50%);
  width: 100%;
  max-width: 1920px;
  bottom: 0;
  background: white;
  z-index: 9999;
  display: flex;
  flex-direction: column;
}

.preview-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px;
  border-bottom: 1px solid #e0e0e0;
  background: #f8f9fa;
}

.preview-header h2 {
  margin: 0;
  color: #333;
}

.header-actions {
  display: flex;
  gap: 10px;
}

.preview-content {
  flex: 1;
  overflow-y: auto;
  padding: 40px;
  background: white;
}

.document-preview {
  max-width: 800px;
  margin: 0 auto;
  font-family: 'Microsoft YaHei', sans-serif;
  line-height: 1.6;
  color: #333;
}

.report-header {
  text-align: center;
  margin-bottom: 40px;
  padding-bottom: 30px;
  border-bottom: 2px solid #e0e0e0;
}

.report-title {
  font-size: 28px;
  font-weight: bold;
  color: #1a1a1a;
  margin-bottom: 20px;
}

.report-meta {
  font-size: 16px;
  color: #666;
}

.report-meta p {
  margin: 8px 0;
}

.report-section {
  margin-bottom: 40px;
}

.report-section h2 {
  font-size: 22px;
  font-weight: bold;
  color: #1a1a1a;
  margin-bottom: 20px;
  padding-bottom: 10px;
  border-bottom: 1px solid #e0e0e0;
}

.statistics-subsection {
  margin-bottom: 30px;
}

.statistics-subsection h3 {
  font-size: 18px;
  font-weight: bold;
  color: #333;
  margin-bottom: 15px;
}

.stats-summary {
  margin-bottom: 20px;
  padding: 15px;
  background: #f8f9fa;
  border-left: 4px solid #1890ff;
  border-radius: 4px;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(120px, 1fr));
  gap: 15px;
  margin-top: 20px;
}

.stat-card {
  text-align: center;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  background: white;
}

.stat-level {
  font-size: 18px;
  font-weight: bold;
  margin-bottom: 10px;
  padding: 5px 10px;
  border-radius: 20px;
}

.level-strong {
  background: #f6ffed;
  color: #52c41a;
  border: 1px solid #b7eb8f;
}

.level-medium-strong {
  background: #e6f7ff;
  color: #1890ff;
  border: 1px solid #91d5ff;
}

.level-medium {
  background: #fff7e6;
  color: #fa8c16;
  border: 1px solid #ffd591;
}

.level-weak {
  background: #fff1f0;
  color: #ff4d4f;
  border: 1px solid #ffccc7;
}

.level-very-weak {
  background: #f9f0ff;
  color: #722ed1;
  border: 1px solid #d3adf7;
}

.stat-count {
  font-size: 24px;
  font-weight: bold;
  color: #333;
  margin-bottom: 5px;
}

.stat-percent {
  font-size: 14px;
  color: #666;
}

.chart-container {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
  margin-top: 20px;
}

.chart {
  height: 300px;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
}

.conclusion-content p {
  margin-bottom: 15px;
  text-align: justify;
}

.detailed-stats {
  margin-top: 20px;
  padding: 20px;
  background: #f8f9fa;
  border-radius: 8px;
}

.detailed-stats h4 {
  margin-bottom: 15px;
  color: #333;
}

.detailed-stats ul {
  list-style: none;
  padding: 0;
}

.detailed-stats li {
  margin-bottom: 10px;
  padding-left: 20px;
  position: relative;
}

.level-highlight {
  padding: 2px 8px;
  border-radius: 4px;
  font-weight: bold;
}

.technical-notes {
  background: #f8f9fa;
  padding: 20px;
  border-radius: 8px;
}

.technical-notes p {
  margin-bottom: 10px;
}

.report-footer {
  margin-top: 60px;
  text-align: center;
  padding-top: 20px;
  border-top: 1px solid #e0e0e0;
}

.generate-time {
  color: #666;
  font-size: 14px;
  font-style: italic;
}

.no-data {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 400px;
}

@media (max-width: 1200px) {
  .preview-content {
    padding: 20px;
  }

  .chart-container {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 768px) {
  .stats-grid {
    grid-template-columns: repeat(auto-fit, minmax(100px, 1fr));
  }

  .preview-header {
    flex-direction: column;
    gap: 15px;
    text-align: center;
  }
}
</style>
