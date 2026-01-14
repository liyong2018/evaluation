<template>
  <div class="thematic-map-page">
    <div class="page-header">
      <h1>评估报告生成</h1>
      <p class="page-description">基于减灾能力评估数据生成专业的专题图，支持多种格式导出</p>
    </div>
    
    <div class="page-content">    
      <!-- 专题图显示区域 -->
      <div class="map-display-area">
        <el-card class="map-card" :body-style="{ padding: '0px' }">
          <template #header>
            <div class="card-header">
              <span>结果图预览</span>
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
                <el-select
                  v-model="selectedLevel"
                  placeholder="级别"
                  style="width: 160px"
                  @change="handleFilterChange"
                >
                  <el-option label="乡镇级" value="township" />
                  <el-option label="社区-行政村级" value="community_village" />
                  <el-option label="社区-乡镇级" value="community_township" />
                  <el-option label="综合" value="comprehensive" />
                  <el-option label="综合（组合图）" value="comprehensive_composite" />
                </el-select>
                <el-button size="small" @click="refreshMap">刷新</el-button>
                <el-button size="small" type="success" @click="fullscreen">全屏</el-button>
                <el-button size="small" type="primary" @click="openOnlyOfficeEditor">生成报告</el-button>
              </div>
            </div>
          </template>
          
          <div class="map-container" v-loading="loading">
            <div class="map-stage">
              <CompositeThematicMap
                v-if="selectedLevel === 'comprehensive_composite' && showMap"
                :year="selectedYear || undefined"
                :orgCode="selectedOrgCode || ''"
                :orgId="selectedOrgId || undefined"
                :orgName="selectedOrgName || undefined"
                ref="compositeMapRef"
              />
              <ThematicMapGenerator
                v-else-if="showMap"
                :reportId="mapSettings.reportId"
                :mapConfig="computedMapConfig"
                :year="selectedYear || undefined"
                :orgCode="selectedOrgCode || undefined"
                :orgId="selectedOrgId || undefined"
                :orgName="selectedOrgName || undefined"
                :parentName="selectedOrgParentName || undefined"
                :level="selectedLevel"
                ref="mapGeneratorRef"
              />
              <div v-else class="empty-map">
                <el-empty description="请配置参数并生成专题图" />
              </div>
            </div>
          </div>
        </el-card>
      </div>

      <!-- 用于生成报告的隐藏地图实例 -->
      <div class="hidden-map-container" style="position: absolute; left: -10000px; top: 0; width: 1856px; height: 1199px; z-index: -1000; pointer-events: none; overflow: hidden;">
        <ThematicMapGenerator
          ref="generationMapRef"
          :reportId="mapSettings.reportId"
          :mapConfig="{ ...computedMapConfig, showTitle: true, showLegend: true, showScale: true, showCompass: true, showBorder: true }"
          :year="selectedYear || undefined"
          :orgCode="selectedOrgCode || undefined"
          :orgId="selectedOrgId || undefined"
          :orgName="selectedOrgName || undefined"
          level="township"
        />
        <div style="width: 1856px; height: 1044px;">
          <CompositeThematicMap
            ref="generationCompositeMapRef"
            :year="selectedYear || undefined"
            :orgCode="selectedOrgCode || ''"
            :orgId="selectedOrgId || undefined"
            :orgName="selectedOrgName || undefined"
          />
        </div>
      </div>
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
import { ElMessage } from 'element-plus'
import CompositeThematicMap from '@/components/CompositeThematicMap.vue'
import ThematicMapGenerator from '@/components/ThematicMapGenerator.vue'
import OnlyOfficeEditor from '@/components/OnlyOfficeEditor.vue'
import { wordTemplateApi, organizationApi, algorithmManagementApi } from '@/api'
console.log('ThematicMap页面导入完成')

// 响应式数据
const loading = ref(false)
const showMap = ref(true) // 默认显示地图
const mapGeneratorRef = ref()
const compositeMapRef = ref()
const generationMapRef = ref()
const generationCompositeMapRef = ref()
const showOnlyOfficeEditor = ref(false) // 控制OnlyOffice编辑器显示
const wordDocumentUrl = ref('')
const wordDocumentTitle = ref('青神县减灾能力评估技术报告')
const wordDocumentKey = ref('')
const wordTemplatePath = ref('templates/四川省眉山市青神县减灾能力评估技术报告-系统模板.docx') // Word模板路径

const selectedYear = ref<number | null>(2025)
const yearOptions = ref<number[]>([])
const selectedOrgCode = ref<string | null>('511425')
const organizationList = ref<any[]>([])
const loadingOrganizations = ref(false)
const lastLoadedOrgTreeYear = ref<number | null>(null)
const orgTreeMaxLevel = 3
const selectedLevel = ref<string>('township') // 默认乡镇级
const algorithmList = ref<any[]>([])
const selectedAlgorithmId = ref<number | null>(null)

const mapSettings = reactive({
  reportId: 1,
  title: '四川省雅安市青神县乡镇减灾能力评估结果图',
  subtitle: `数据来源：减灾能力评估工具 | 制图时间：${new Date().getFullYear()}年${new Date().getMonth() + 1}月`,
  displayElements: ['title', 'legend', 'table', 'scale', 'compass', 'border']
})

// 计算属性
const computedMapConfig = computed(() => ({
  title: mapSettings.title,
  subtitle: mapSettings.subtitle,
  showTitle: mapSettings.displayElements.includes('title'),
  showLegend: mapSettings.displayElements.includes('legend'),
  showDataTable: mapSettings.displayElements.includes('table'),
  showScale: mapSettings.displayElements.includes('scale'),
  showCompass: mapSettings.displayElements.includes('compass'),
  showBorder: mapSettings.displayElements.includes('border')
}))

const findOrgNodeByCode = (tree: any[], code: string): any | null => {
  for (const node of tree || []) {
    // 使用String转换确保比较正确，兼容数字和字符串类型的code
    if (String(node?.code) === String(code)) return node
    if (node?.children?.length) {
      const found = findOrgNodeByCode(node.children, code)
      if (found) return found
    }
  }
  return null
}

const findOrgNodeAndParent = (tree: any[], code: string, parent: any = null): { node: any, parent: any } | null => {
  for (const node of tree || []) {
    if (String(node?.code) === String(code)) return { node, parent }
    if (node?.children?.length) {
      const result = findOrgNodeAndParent(node.children, code, node)
      if (result) return result
    }
  }
  return null
}

const selectedOrgName = computed(() => {
  if (!selectedOrgCode.value) return ''
  const node = findOrgNodeByCode(organizationList.value, selectedOrgCode.value)
  return node?.name || ''
})

const selectedOrgParentName = computed(() => {
  if (!selectedOrgCode.value) return ''
  const result = findOrgNodeAndParent(organizationList.value, selectedOrgCode.value)
  return result?.parent?.name || ''
})

const selectedOrgId = computed(() => {
  if (!selectedOrgCode.value) return null
  const node = findOrgNodeByCode(organizationList.value, selectedOrgCode.value)
  const id = node?.id
  return typeof id === 'number' ? id : id != null ? Number(id) : null
})

const generateYearOptions = () => {
  const options: number[] = []
  for (let year = 2026; year >= 2020; year--) options.push(year)
  yearOptions.value = options
}

const findFirstNodeByLevel = (tree: any[], level: number): any | null => {
  const queue: any[] = [...(tree || [])]
  while (queue.length) {
    const node = queue.shift()
    if (!node) continue
    if (Number(node.level) === level) return node
    if (Array.isArray(node.children) && node.children.length) queue.push(...node.children)
  }
  return null
}

const getOrganizationList = async (year?: number | null) => {
  loadingOrganizations.value = true
  try {
    const response = await organizationApi.getTree({ year: year ?? undefined, maxLevel: orgTreeMaxLevel })
    if (response.success && response.data) {
      organizationList.value = response.data || []
      lastLoadedOrgTreeYear.value = year ?? null
      if (!organizationList.value.length) {
        selectedOrgCode.value = null
        showMap.value = false
        return
      }
      if (selectedOrgCode.value) {
        const exists = findOrgNodeByCode(organizationList.value, selectedOrgCode.value)
        if (!exists) {
          selectedOrgCode.value = null
        }
      }
      if (!selectedOrgCode.value) {
        const firstCounty = findFirstNodeByLevel(organizationList.value, 3)
        if (firstCounty?.code != null) {
          selectedOrgCode.value = String(firstCounty.code)
        }
      }
      showMap.value = Boolean(selectedOrgCode.value)
    }
  } catch (error) {
    console.error('获取组织机构列表失败:', error)
    ElMessage.error('获取组织机构列表失败')
  } finally {
    loadingOrganizations.value = false
  }
}

const handleFilterChange = async () => {
  if (selectedYear.value !== lastLoadedOrgTreeYear.value) {
    await getOrganizationList(selectedYear.value)
  }
  const y = selectedYear.value ? `${selectedYear.value}年` : '全部年份'
  const org = selectedOrgName.value ? ` | 区域：${selectedOrgName.value}` : ''
  const levelNames: Record<string, string> = {
    'township': '乡镇级',
    'community_village': '社区-行政村级',
    'community_township': '社区-乡镇级',
    'comprehensive': '综合',
    'comprehensive_composite': '综合'
  }
  const levelName = levelNames[selectedLevel.value] || '乡镇级'
  const regionName = selectedOrgName.value || '无区域'

  mapSettings.title = `四川省眉山市${regionName}${levelName}减灾能力评估结果图`
  mapSettings.subtitle = `数据年份：${y}${org} | 级别：${levelName} | 制图时间：${new Date().getFullYear()}年${new Date().getMonth() + 1}月`
  showMap.value = Boolean(selectedOrgCode.value)
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
  mapSettings.title = '四川省雅安市青神县乡镇减灾能力评估结果图'
  mapSettings.subtitle = `数据来源：减灾能力评估工具 | 制图时间：${new Date().getFullYear()}年${new Date().getMonth() + 1}月`
  mapSettings.displayElements = ['title', 'legend', 'table', 'scale', 'compass', 'border']
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

// 打开Word预览编辑器
const openOnlyOfficeEditor = async () => {
  if (!selectedOrgCode.value) {
    ElMessage.warning('当前年份无可用区划，无法生成报告')
    return
  }
  let loadingInstance: any = null
  const showLoading = (msg: string) => {
    if (loadingInstance) loadingInstance.close()
    loadingInstance = ElMessage({
      message: msg,
      type: 'info',
      duration: 0
    })
  }

  showLoading('正在生成4张专题图，请稍候...')

  try {
    const year = selectedYear.value || new Date().getFullYear()
    const orgCode = selectedOrgCode.value || '511425'

    // 定义4个级别的专题图
    const levels = [
      { value: 'township', name: '乡镇级' },
      { value: 'community_village', name: '社区-行政村级' },
      { value: 'community_township', name: '社区-乡镇级' },
      { value: 'comprehensive', name: '综合' }
    ]

    const uploadedImages: string[] = []

    // 1. 循环生成并上传4张不同级别的专题图
    for (const level of levels) {
      showLoading(`正在生成${level.name}专题图...`)
      try {
        console.log(`正在生成${level.name}专题图，级别: ${level.value}...`)

        // 如果是综合图，使用组合图生成器
        if (level.value === 'comprehensive') {
          if (generationCompositeMapRef.value) {
            const imageUrl = await generationCompositeMapRef.value.exportAndUploadForOnlyOffice()
            if (imageUrl) {
              uploadedImages.push(imageUrl)
              console.log(`${level.name}专题图（组合图）上传成功:`, imageUrl)
            } else {
              console.warn(`${level.name}专题图（组合图）上传失败，返回null`)
            }
          } else {
             console.error('generationCompositeMapRef 不存在')
          }
        } else {
          // 其他级别使用通用生成器
          if (generationMapRef.value && generationMapRef.value.exportAndUploadForOnlyOfficeWithLevel) {
            const imageUrl = await generationMapRef.value.exportAndUploadForOnlyOfficeWithLevel(level.value)
            if (imageUrl) {
              uploadedImages.push(imageUrl)
              console.log(`${level.name}专题图上传成功:`, imageUrl)
            } else {
              console.warn(`${level.name}专题图上传失败，返回null`)
            }
          } else {
            console.error('generationMapRef 或 exportAndUploadForOnlyOfficeWithLevel 方法不存在')
          }
        }
      } catch (e) {
        console.error(`上传${level.name}专题图失败:`, e)
      }
    }

    if (uploadedImages.length > 0) {
      ElMessage.success(`成功上传${uploadedImages.length}张专题图`)
    } else {
      ElMessage.warning('专题图上传失败，将继续生成报告（不含专题图）')
    }

    // 2. 生成Word文档
    showLoading('正在生成Word文档，请稍候...')
    console.log(`正在生成${year}年${orgCode}区域的Word文档...`)
    try {
      await wordTemplateApi.generateReport(year, orgCode)
      console.log('Word文档生成成功')
    } catch (e) {
      console.error('生成Word文档失败:', e)
      ElMessage.error('生成Word文档失败，请重试')
      if (loadingInstance) loadingInstance.close()
      return
    }

    // 3. 等待文件写入完成
    await new Promise(resolve => setTimeout(resolve, 1000))

    // 4. 设置OnlyOffice文档URL
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

    // 5. 将上传的专题图URLs存储到window对象，供OnlyOffice使用
    if (uploadedImages.length > 0) {
      ;(window as any).uploadedThematicMapUrls = uploadedImages
      console.log('专题图URLs已存储，可供OnlyOffice使用:', uploadedImages)
    }

    showOnlyOfficeEditor.value = true
  } catch (error) {
    console.error('打开Word编辑器失败:', error)
    ElMessage.error('打开Word编辑器失败')
  } finally {
    if (loadingInstance) loadingInstance.close()
  }
}

// 组件挂载
onMounted(async () => {
  generateYearOptions()
  try {
    const response = await algorithmManagementApi.getList()
    if (response.success && Array.isArray(response.data)) {
      algorithmList.value = response.data
      if (selectedAlgorithmId.value == null && algorithmList.value.length > 0) {
        selectedAlgorithmId.value = algorithmList.value[0]?.id ?? null
      }
    }
  } catch (e) {
    algorithmList.value = []
  }
  await getOrganizationList(selectedYear.value)

  // 初始化标题
  await handleFilterChange()

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
      mapSettings.title = `${data.regionName || '评估区域'}减灾能力评估结果图`
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
  padding: 10px 0;
  width: 100%;
  max-width: 100%;
  margin: 0 auto;
  background: #f5f5f5;

  box-sizing: border-box;
  
  .page-header {
    margin-bottom: 10px;
    padding: 0 20px;
    
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
    margin-bottom: 20px;
    display: flex;
    justify-content: center;
    
    .map-display-area {
      width: 100%;
      display: flex;
      justify-content: center;

      .map-card {
        width: 100%;
        max-width: 1920px;
        margin: 0 auto;
        
        .map-container {
          width: 1440px;
          max-height: none; 
          
          aspect-ratio: 1856 / 1199;
          
          max-width: 100%;
          
          position: relative;
          overflow: hidden;
          margin: 0 auto;
          
          .map-stage {
            width: 100%;
            height: 100%;
            flex: 0 0 auto;
            position: relative;
            background: #fff;
            overflow: hidden;
          }

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
