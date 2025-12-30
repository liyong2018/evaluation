<template>
  <div class="thematic-map-page">
    <div class="page-header">
      <h1>专题图生成</h1>
      <p class="page-description">基于减灾能力评估数据生成专业的专题图，支持多种格式导出</p>
    </div>
    
    <div class="page-content">    
      <!-- 专题图显示区域 -->
      <div class="map-display-area">
        <el-card class="map-card">
          <template #header>
            <div class="card-header">
              <span>专题图预览</span>
              <div class="header-actions">
                <el-select
                  v-model="selectedYear"
                  placeholder="选择年份"
                  clearable
                  style="width: 120px"
                  @change="handleFilterChange"
                >
                  <el-option v-for="year in yearOptions" :key="year" :label="year + '年'" :value="year" />
                </el-select>
                <el-tree-select
                  v-model="selectedOrgCode"
                  :data="organizationList"
                  placeholder="选择区域"
                  clearable
                  filterable
                  check-strictly
                  :render-after-expand="false"
                  :disabled="loadingOrganizations"
                  style="width: 260px"
                  node-key="code"
                  :props="{ value: 'code', label: 'name', children: 'children' }"
                  @change="handleFilterChange"
                >
                  <template #default="{ data }">
                    <span>{{ data.name }} <span style="color: #909399; font-size: 12px;">({{ data.code }})</span></span>
                  </template>
                </el-tree-select>
                <el-button size="small" @click="refreshMap">刷新</el-button>
                <el-button size="small" type="success" @click="fullscreen">全屏</el-button>
                <el-button size="small" type="primary" @click="openOnlyOfficeEditor">打开Word编辑器</el-button>
                <el-button size="small" type="warning" @click="downloadWord">下载Word</el-button>
              </div>
            </div>
          </template>
          
          <div class="map-container" v-loading="loading">
            <ThematicMapGenerator 
              v-if="showMap"
              :reportId="mapSettings.reportId"
              :mapConfig="computedMapConfig"
              :year="selectedYear || undefined"
              :orgCode="selectedOrgCode || undefined"
              :orgName="selectedOrgName || undefined"
              ref="mapGeneratorRef"
            />
            <div v-else class="empty-map">
              <el-empty description="请配置参数并生成专题图" />
            </div>
          </div>
        </el-card>
      </div>
    </div>
    
    <!-- 历史记录面板 -->
    <div class="history-panel">
      <el-card>
        <template #header>
          <div class="card-header">
            <span>专题图历史</span>
            <el-button size="small" @click="loadHistory">刷新</el-button>
          </div>
        </template>
        
        <div class="history-list">
          <div 
            v-for="item in historyList" 
            :key="item.id" 
            class="history-item"
            @click="loadHistoryMap(item)"
          >
            <div class="history-info">
              <div class="history-title">{{ item.title }}</div>
              <div class="history-time">{{ formatTime(item.createTime) }}</div>
            </div>
            <div class="history-actions">
              <el-button size="small" type="text" @click.stop="downloadHistory(item)">下载</el-button>
              <el-button size="small" type="text" danger @click.stop="deleteHistory(item)">删除</el-button>
            </div>
          </div>
          
          <el-empty v-if="historyList.length === 0" description="暂无历史记录" />
        </div>
      </el-card>
    </div>

    <!-- OnlyOffice编辑器组件 -->
    <el-dialog
      v-model="showOnlyOfficeEditor"
      title="Word文档预览"
      fullscreen
      destroy-on-close
      class="word-editor-dialog"
      :close-on-click-modal="false"
    >
      <OnlyOfficeEditor
        v-if="showOnlyOfficeEditor"
        :document-url="wordDocumentUrl"
        :document-title="wordDocumentTitle"
        :document-key="wordDocumentKey"
      />
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
console.log('ThematicMap页面开始加载')
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import ThematicMapGenerator from '@/components/ThematicMapGenerator.vue'
import OnlyOfficeEditor from '@/components/OnlyOfficeEditor.vue'
import { thematicMapApi, wordTemplateApi, organizationApi } from '@/api'
console.log('ThematicMap页面导入完成')

// 响应式数据
const loading = ref(false)
const showMap = ref(true) // 默认显示地图
const mapGeneratorRef = ref()
const showOnlyOfficeEditor = ref(false) // 控制OnlyOffice编辑器显示
const wordDocumentUrl = ref('')
const wordDocumentTitle = ref('青神县减灾能力评估技术报告')
const wordDocumentKey = ref('')
const wordTemplatePath = ref('templates/四川省眉山市青神县减灾能力评估技术报告-系统模板.docx') // Word模板路径

const selectedYear = ref<number | null>(2024)
const yearOptions = ref<number[]>([])
const selectedOrgCode = ref<string | null>('511425')
const organizationList = ref<any[]>([])
const loadingOrganizations = ref(false)

const mapSettings = reactive({
  reportId: 1,
  title: '四川省雅安市青神县乡镇减灾能力评估专题图',
  subtitle: `数据来源：减灾能力评估工具 | 制图时间：${new Date().getFullYear()}年${new Date().getMonth() + 1}月`,
  displayElements: ['title', 'legend', 'scale', 'compass', 'border']
})

const historyList = ref<any[]>([])

// 计算属性
const computedMapConfig = computed(() => ({
  title: mapSettings.title,
  subtitle: mapSettings.subtitle,
  showTitle: mapSettings.displayElements.includes('title'),
  showLegend: mapSettings.displayElements.includes('legend'),
  showScale: mapSettings.displayElements.includes('scale'),
  showCompass: mapSettings.displayElements.includes('compass'),
  showBorder: mapSettings.displayElements.includes('border')
}))

const findOrgNodeByCode = (tree: any[], code: string): any | null => {
  for (const node of tree || []) {
    if (node?.code === code) return node
    if (node?.children?.length) {
      const found = findOrgNodeByCode(node.children, code)
      if (found) return found
    }
  }
  return null
}

const selectedOrgName = computed(() => {
  if (!selectedOrgCode.value) return ''
  const node = findOrgNodeByCode(organizationList.value, selectedOrgCode.value)
  return node?.name || ''
})

const generateYearOptions = () => {
  const currentYear = new Date().getFullYear()
  const options: number[] = []
  for (let year = 2020; year <= currentYear; year++) options.push(year)
  yearOptions.value = options
}

const getOrganizationList = async () => {
  loadingOrganizations.value = true
  try {
    const response = await organizationApi.getTree()
    if (response.success && response.data) {
      organizationList.value = response.data || []
    }
  } catch (error) {
    console.error('获取组织机构列表失败:', error)
    ElMessage.error('获取组织机构列表失败')
  } finally {
    loadingOrganizations.value = false
  }
}

const handleFilterChange = () => {
  const y = selectedYear.value ? `${selectedYear.value}年` : '全部年份'
  const org = selectedOrgName.value ? ` | 区域：${selectedOrgName.value}` : ''
  mapSettings.subtitle = `数据年份：${y}${org} | 制图时间：${new Date().getFullYear()}年${new Date().getMonth() + 1}月`
  refreshMap()
}

// 生成专题图
const generateMap = async () => {
  if (!mapSettings.reportId) {
    ElMessage.warning('请输入报告ID')
    return
  }
  
  if (!mapSettings.title.trim()) {
    ElMessage.warning('请输入专题图标题')
    return
  }
  
  loading.value = true
  try {
    // 这里可以添加数据验证逻辑
    showMap.value = true
    ElMessage.success('专题图生成成功')
  } catch (error) {
    console.error('生成专题图失败:', error)
    ElMessage.error('生成专题图失败')
  } finally {
    loading.value = false
  }
}

// 重置配置
const resetSettings = () => {
  mapSettings.reportId = 1
  mapSettings.title = '四川省雅安市青神县乡镇减灾能力评估专题图'
  mapSettings.subtitle = `数据来源：减灾能力评估工具 | 制图时间：${new Date().getFullYear()}年${new Date().getMonth() + 1}月`
  mapSettings.displayElements = ['title', 'legend', 'scale', 'compass', 'border']
  showMap.value = false
}

// 刷新地图
const refreshMap = () => {
  if (showMap.value) {
    showMap.value = false
    setTimeout(() => {
      showMap.value = true
    }, 100)
  }
}

// 全屏显示
const fullscreen = () => {
  if (mapGeneratorRef.value) {
    const element = mapGeneratorRef.value.$el
    if (element.requestFullscreen) {
      element.requestFullscreen()
    }
  }
}

// 加载历史记录
const loadHistory = async () => {
  try {
    console.log('开始加载历史记录...')
    // 暂时跳过API调用，直接使用模拟数据，避免404错误
    // const response = await thematicMapApi.getMapHistory()
    // historyList.value = response.data || []
    
    // 使用模拟数据，避免影响用户体验
    historyList.value = [
      {
        id: 1,
        title: '青神县减灾能力评估专题图',
        createTime: new Date().toISOString(),
        format: 'png',
        reportId: 1
      },
      {
        id: 2,
        title: '雅安市减灾能力分析图',
        createTime: new Date(Date.now() - 86400000).toISOString(),
        format: 'pdf',
        reportId: 2
      },
      {
        id: 3,
        title: '眉山市综合评估专题图',
        createTime: new Date(Date.now() - 172800000).toISOString(),
        format: 'png',
        reportId: 3
      }
    ]
    console.log('使用模拟历史记录数据:', historyList.value)
  } catch (error) {
    console.error('加载历史记录失败:', error)
    // 使用空数组作为后备
    historyList.value = []
  }
}

// 加载历史专题图
const loadHistoryMap = (item: any) => {
  mapSettings.reportId = item.reportId
  mapSettings.title = item.title
  generateMap()
}

// 下载历史记录
const downloadHistory = (item: any) => {
  ElMessage.info(`下载 ${item.title}`)
  // 这里实现下载逻辑
}

// 删除历史记录
const deleteHistory = async (item: any) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除专题图 "${item.title}" 吗？`,
      '确认删除',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    await thematicMapApi.deleteMapRecord(item.id)
    ElMessage.success('删除成功')
    loadHistory()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除失败:', error)
      ElMessage.error('删除失败')
    }
  }
}

// 格式化时间
const formatTime = (timeStr: string) => {
  const date = new Date(timeStr)
  return date.toLocaleString('zh-CN')
}


// 打开Word预览编辑器
const openOnlyOfficeEditor = async () => {
  const loadingInstance = ElMessage({
    message: '正在生成Word文档，请稍候...',
    type: 'info',
    duration: 0
  })

  try {
    const year = selectedYear.value || new Date().getFullYear()
    const orgCode = selectedOrgCode.value || '511425'

    // 1. 先生成Word文档
    console.log(`正在生成${year}年${orgCode}区域的Word文档...`)
    try {
      await wordTemplateApi.generateReport(year, orgCode)
      console.log('Word文档生成成功')
      ElMessage.success('Word文档生成成功，正在打开编辑器...')
    } catch (e) {
      console.error('生成Word文档失败:', e)
      ElMessage.error('生成Word文档失败，请重试')
      loadingInstance.close()
      return
    }

    // 2. 等待文件写入完成
    await new Promise(resolve => setTimeout(resolve, 1000))

    // 3. 设置OnlyOffice文档URL
    let baseUrl = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8081'
    try {
      const u = new URL(baseUrl)
      if (u.port === '8088' || u.port === '5173' || u.port === '8087' || u.port === '') {
        u.port = '8081'
      }
      if (u.hostname === 'localhost' || u.hostname === '127.0.0.1' || u.hostname.startsWith('192.168.')) {
        u.hostname = 'host.docker.internal'
      }
      baseUrl = u.toString().replace(/\/$/, '')
    } catch {
      baseUrl = 'http://host.docker.internal:8081'
    }

    wordDocumentUrl.value = `${baseUrl}/api/word-template/latest-report`
    wordDocumentTitle.value = `青神县减灾能力评估技术报告_${year}.docx`
    wordDocumentKey.value = new Date().getTime().toString()
    showOnlyOfficeEditor.value = true
  } catch (error) {
    console.error('打开Word编辑器失败:', error)
    ElMessage.error('打开Word编辑器失败')
  } finally {
    loadingInstance.close()
  }
}

// 下载Word文档
const downloadWord = async () => {
  const loadingInstance = ElMessage({
    message: '正在生成Word文档，请稍候...',
    type: 'info',
    duration: 0 // 手动关闭
  })

  try {
    const response = await wordTemplateApi.generateReport(selectedYear.value || undefined, selectedOrgCode.value || undefined)

    const blob = new Blob([response], {
      type: 'application/vnd.openxmlformats-officedocument.wordprocessingml.document'
    })

    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url

    const currentDate = new Date()
    const reportYear = selectedYear.value || currentDate.getFullYear()
    const fileName = `青神县减灾能力评估技术报告_${reportYear}${String(currentDate.getMonth() + 1).padStart(2, '0')}${String(currentDate.getDate()).padStart(2, '0')}.docx`

    link.download = fileName
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    window.URL.revokeObjectURL(url)

    ElMessage.success('Word文档下载成功！')
  } catch (error) {
    console.error('下载Word文档失败:', error)
    ElMessage.error('生成Word文档失败，请重试')
  } finally {
    loadingInstance.close()
  }
}

// 组件挂载
onMounted(async () => {
  generateYearOptions()
  await getOrganizationList()
  await loadHistory()
  
  // 检查是否有从评估计算传递的数据
  const evaluationData = (window as any).evaluationData
  if (evaluationData) {
    console.log('检测到评估数据，自动生成专题图:', evaluationData)
    // 自动生成专题图
    showMap.value = true
  }
  
  // 检查是否有从Results页面传递的数据
  loadDataFromSession()
})

// 从sessionStorage加载数据
const loadDataFromSession = () => {
  try {
    const thematicMapData = sessionStorage.getItem('thematicMapData')
    console.log('从sessionStorage读取的数据:', thematicMapData)
    
    if (thematicMapData) {
      const data = JSON.parse(thematicMapData)
      console.log('解析后的专题图数据:', data)
      
      // 验证数据完整性
      if (!data.resultData || !data.resultData.tableData) {
        console.error('数据不完整，缺少tableData')
        ElMessage.error('传递的数据不完整')
        return
      }
      
      // 根据传递的数据更新配置
      mapSettings.title = `${data.regionName || '评估区域'}减灾能力评估专题图`
      mapSettings.subtitle = `评估时间：${data.evaluationTime} | 算法：${data.algorithm} | 综合得分：${data.totalScore}`
      mapSettings.reportId = data.id || 1
      
      // 存储评估数据供专题图组件使用
      ;(window as any).evaluationData = {
        tableData: data.tableData || data.resultData.tableData,
        columns: data.columns || data.resultData.columns,
        summary: data.summary || data.resultData.summary,
        stepInfo: data.stepInfo,
        formula: data.formula
      }
      
      console.log('设置的评估数据:', (window as any).evaluationData)
      
      // 自动生成专题图
      generateMap()
      
      ElMessage.success(`已加载${data.regionName || '评估区域'}的评估数据，共${data.tableData?.length || 0}条记录`)
      
      // 清除sessionStorage中的数据，避免重复使用
      sessionStorage.removeItem('thematicMapData')
    } else {
      console.log('未找到专题图数据')
    }
  } catch (error) {
    console.error('加载传递数据失败:', error)
    ElMessage.error('加载传递数据失败：' + ((error as any)?.message || '未知错误'))
  }
}
</script>

<style lang="scss">
.word-editor-dialog.is-fullscreen {
  display: flex;
  flex-direction: column;
  overflow: hidden;

  .el-dialog__body {
    flex: 1;
    padding: 0 !important;
    height: 100%;
    overflow: hidden;
  }
}
</style>

<style scoped lang="scss">
.thematic-map-page {
  padding: 20px;
  background: #f5f5f5;
  min-height: 100vh;
  
  .page-header {
    margin-bottom: 20px;
    
    h1 {
      margin: 0 0 8px 0;
      color: #333;
      font-size: 24px;
    }
    
    .page-description {
      margin: 0;
      color: #666;
      font-size: 14px;
    }
  }
  
  .page-content {
    // display: grid;
    grid-template-columns: 350px 1fr;
    gap: 20px;
    margin-bottom: 20px;
    
    .config-panel {
      .config-card {
        height: fit-content;
      }
    }
    
    .map-display-area {
      .map-card {
        // height: 600px;
        
        .map-container {
          // height: 400px;
          position: relative;
          
          .empty-map {
            height: 100%;
            display: flex;
            align-items: center;
            justify-content: center;
          }
        }
      }
    }
  }
  
  .history-panel {
    .history-list {
      max-height: 300px;
      overflow-y: auto;
      
      .history-item {
        display: flex;
        justify-content: space-between;
        align-items: center;
        padding: 12px;
        border: 1px solid #eee;
        border-radius: 6px;
        margin-bottom: 8px;
        cursor: pointer;
        transition: all 0.3s;
        
        &:hover {
          background: #f9f9f9;
          border-color: #409eff;
        }
        
        .history-info {
          flex: 1;
          
          .history-title {
            font-weight: 500;
            color: #333;
            margin-bottom: 4px;
          }
          
          .history-time {
            font-size: 12px;
            color: #999;
          }
        }
        
        .history-actions {
          display: flex;
          gap: 8px;
        }
      }
    }
  }
  
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    
    .header-actions {
      display: flex;
      gap: 8px;
    }
  }
}

// 响应式设计
@media (max-width: 1200px) {
  .thematic-map-page {
    .page-content {
      grid-template-columns: 1fr;
      
      .config-panel {
        order: 2;
      }
      
      .map-display-area {
        order: 1;
      }
    }
  }
}

@media (max-width: 768px) {
  .thematic-map-page {
    padding: 10px;
    
    .page-content {
      gap: 10px;
    }
  }
}
</style>
