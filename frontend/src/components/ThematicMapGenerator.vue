<template>
  <div class="thematic-map-container" ref="mapContainer" :class="{ 'fullscreen': isFullscreen }">
    <!-- 地图容器 -->
    <div id="map" ref="mapRef" class="map-content"></div>
    
    <!-- 全屏控制按钮 -->

    
    <!-- 制图要素覆盖层 -->
    <div class="map-elements-overlay">
      <!-- 标题 -->
      <div class="map-title" v-show="mapConfig.showTitle">
        <h2>{{ mapConfig.title }}</h2>
        <p class="subtitle">{{ mapConfig.subtitle }}</p>
      </div>
      
      <!-- 图例 - 移动到左下角 -->
      <div class="map-legend" v-show="mapConfig.showLegend">
        <div class="legend-title">减灾能力</div>
        <div class="legend-items">
          <div v-for="item in legendItems" :key="item.value" class="legend-item">
            <span class="legend-color" :style="{backgroundColor: item.color}"></span>
            <span class="legend-label">{{ item.label }}</span>
          </div>
        </div>
      </div>
      
      <!-- 数据表格 - 放在右下角 -->
      <div class="map-data-table" v-show="mapConfig.showDataTable">
        <div class="table-title">乡镇（街道）减灾能力统计表</div>
        <table class="data-table">
          <thead>
            <tr>
              <th>强</th>
              <th>较强</th>
              <th>中等</th>
              <th>较弱</th>
              <th>弱</th>
              <th>总数</th>
            </tr>
          </thead>
          <tbody>
            <tr>
              <td>{{ getStatistics().strong }}</td>
              <td>{{ getStatistics().mediumStrong }}</td>
              <td>{{ getStatistics().medium }}</td>
              <td>{{ getStatistics().weak }}</td>
              <td>{{ getStatistics().veryWeak }}</td>
              <td>{{ getStatistics().total }}</td>
            </tr>
            <tr>
              <td>{{ getStatistics().strongPercent }}%</td>
              <td>{{ getStatistics().mediumStrongPercent }}%</td>
              <td>{{ getStatistics().mediumPercent }}%</td>
              <td>{{ getStatistics().weakPercent }}%</td>
              <td>{{ getStatistics().veryWeakPercent }}%</td>
              <td>100%</td>
            </tr>
          </tbody>
        </table>
      </div>
      
      <!-- 比例尺 -->
      <div class="map-scale" v-show="mapConfig.showScale">
        <div class="scale-bar" ref="scaleBar">
          <div class="scale-line"></div>
          <div class="scale-text">{{ scaleText }}</div>
        </div>
      </div>
      
      <!-- 指北针 -->
      <div class="map-compass" v-show="mapConfig.showCompass">
        <div class="compass-icon">⬆</div>
        <div class="compass-text">N</div>
      </div>
      
      <!-- 边框 -->
      <div class="map-border" v-show="mapConfig.showBorder"></div>
    </div>
    
    <!-- 配置面板 -->
    <div class="config-panel">
      <el-card class="config-card" shadow="hover">
        <template #header>
          <span>制图要素配置</span>
        </template>
        <div class="config-items">
          <el-checkbox v-model="mapConfig.showTitle" @change="updateMapElements">显示标题</el-checkbox>
          <el-checkbox v-model="mapConfig.showLegend" @change="updateMapElements">显示图例</el-checkbox>
          <el-checkbox v-model="mapConfig.showDataTable" @change="updateMapElements">显示数据表</el-checkbox>
          <el-checkbox v-model="mapConfig.showScale" @change="updateMapElements">显示比例尺</el-checkbox>
          <el-checkbox v-model="mapConfig.showCompass" @change="updateMapElements">显示指北针</el-checkbox>
          <el-checkbox v-model="mapConfig.showBorder" @change="updateMapElements">显示边框</el-checkbox>
        </div>
      </el-card>
    </div>
    
    <!-- 导出控制面板 -->
    <div class="export-panel">
      <el-button @click="exportAsPNG" type="primary">导出PNG</el-button>
      <el-button @click="exportAsPDF" type="success">导出PDF</el-button>
      <el-button @click="exportAsWord" type="warning">导出Word</el-button>
    </div>
  </div>
</template>

<script setup lang="ts">
console.log('ThematicMapGenerator组件开始加载')
import { ref, onMounted, nextTick, withDefaults, defineProps, computed } from 'vue'
import L from 'leaflet'
import 'leaflet/dist/leaflet.css'
import 'leaflet.chinatmsproviders'
import html2canvas from 'html2canvas'
console.log('ThematicMapGenerator组件导入完成')
import jsPDF from 'jspdf'
import { Document, Packer, Paragraph, ImageRun, Table, TableRow, TableCell, WidthType, AlignmentType } from 'docx'
import { ElMessage, ElCard, ElCheckbox } from 'element-plus'
import { thematicMapApi, regionApi, evaluationApi } from '@/api'

// 组件属性
interface Props {
  reportId?: number
  regionData?: any[]
}

const props = withDefaults(defineProps<Props>(), {
  reportId: 0,
  regionData: () => []
})

// 响应式数据
const mapContainer = ref<HTMLElement>()
const mapRef = ref<HTMLElement>()
const scaleBar = ref<HTMLElement>()
const map = ref<L.Map | null>(null)
const isFullscreen = ref(false)
const thematicLayer = ref<L.LayerGroup | null>(null)

const mapConfig = ref({
  title: '四川省雅安市青神县乡镇减灾能力评估专题图',
  mainTitle: '减灾能力分级计算减灾能力评估报告',
  subtitle: `数据来源：减灾能力评估系统 | 制图时间：${new Date().getFullYear()}年${new Date().getMonth() + 1}月`,
  showTitle: true,
  showLegend: true,
  showDataTable: true,
  showScale: true,
  showCompass: true,
  showBorder: true
})

const legendItems = ref([
  { value: 'strong', label: '强', color: '#137909' },        // 深绿色
  { value: 'mediumStrong', label: '较强', color: '#46952f' }, // 浅绿色  
  { value: 'medium', label: '中等', color: '#79b517' },      // 黄绿色
  { value: 'weak', label: '较弱', color: '#abd17c' },        // 浅黄色
  { value: 'veryWeak', label: '弱', color: '#e2efa8' }       // 米色
])

// 存储当前专题数据用于统计
const currentThematicData = ref<any[]>([])

const scaleText = ref('0    5    10 km')

// 注释掉不再使用的分数计算函数
// const calculateCapabilityLevel = (score: number): string => {
//   if (score >= 90) return '强'
//   else if (score >= 80) return '较强'
//   else if (score >= 70) return '中等'
//   else if (score >= 60) return '较弱'
//   else return '弱'
// }

// 根据能力等级获取颜色
const getCapabilityColor = (level: string): string => {
  switch (level) {
    case '强':
      return '#006400' // 深绿色
    case '较强':
      return '#32CD32' // 浅绿色
    case '中等':
      return '#9ACD32' // 黄绿色
    case '较弱':
      return '#FFFF99' // 浅黄色
    case '弱':
      return '#F5F5DC' // 米色
    default:
      return '#9e9e9e' // 灰色
  }
}

// 获取能力等级的文本描述
const getCapabilityText = (level: string): string => {
  return level || '未知'
}

// 注释掉不再使用的分数计算函数
// const getCapabilityLevelText = (score: number): string => {
//   if (score >= 90) return '强'
//   else if (score >= 80) return '较强'
//   else if (score >= 70) return '中等'
//   else if (score >= 60) return '较弱'
//   else return '弱'
// }

// 全屏功能
const toggleFullscreen = () => {
  isFullscreen.value = !isFullscreen.value
  
  // 延迟调整地图大小，确保容器尺寸变化完成
  nextTick(() => {
    setTimeout(() => {
      if (map.value) {
        map.value.invalidateSize()
      }
    }, 100)
  })
}

// 地图初始化
const initMap = () => {
  console.log('开始初始化地图，mapRef.value:', mapRef.value)
  if (!mapRef.value) {
    console.error('mapRef.value为空，无法初始化地图')
    return
  }
  
  console.log('创建Leaflet地图实例')
  map.value = L.map(mapRef.value, {
    center: [30.0572, 103.9478], // 青神县坐标
    zoom: 11,
    zoomControl: false // 隐藏默认缩放控件
  })
  
  console.log('地图实例创建成功:', map.value)
  
  // 添加天地图底图
  const baseLayer = L.tileLayer.chinaProvider('TianDiTu.Terrain.Map', {
    key: '0252639b1589bd33a54817f48d982093',
    attribution: '© 天地图'
  })
  baseLayer.addTo(map.value)
  
  // 添加天地图标注
  const labelLayer = L.tileLayer.chinaProvider('TianDiTu.Terrain.Annotion', {
    key: '0252639b1589bd33a54817f48d982093'
  })
  labelLayer.addTo(map.value)
  
  // 监听地图缩放事件，更新比例尺
  map.value.on('zoomend', updateScale)
}

// 从window.evaluationData读取数据
const loadDataFromSession = () => {
  try {
    // 检查是否有从评估计算传递的数据
    const evaluationData = (window as any).evaluationData
    console.log('从window.evaluationData读取数据:', evaluationData)
    
    if (evaluationData && evaluationData.tableData) {
      console.log('找到评估数据，开始处理...')
      console.log('评估数据条数:', evaluationData.tableData.length)
      
      // 处理表格数据，转换为专题图数据
      const processedData = {
        regions: evaluationData.tableData.map((row: any, index: number) => {
          // 从表格数据中提取信息
          const regionName = row.region || row.地区名称 || row.name || `区域${index + 1}`
          const totalScore = parseFloat(row.totalScore || row.总分 || row.综合得分 || row.score || 0)
          
          // 直接使用二维表中的灾害管理能力字段，不再根据分数计算
          const capabilityLevel = row.comprehensiveCapability || row.综合能力分级 || row.management_grade || '中等'
          
          return {
            name: regionName,
            coordinates: generateMockBoundaries(index + 1),
            capabilityLevel: capabilityLevel,
            score: totalScore,
            details: {
              disasterPreventionCapability: parseFloat(row.disasterPreventionCapability || row.灾害预防能力 || row.防灾能力 || 0),
              emergencyResponseCapability: parseFloat(row.emergencyResponseCapability || row.应急响应能力 || row.应急能力 || 0),
              recoveryReconstructionCapability: parseFloat(row.recoveryReconstructionCapability || row.恢复重建能力 || row.重建能力 || 0)
            }
          }
        })
      }
      
      console.log('处理后的专题图数据:', processedData)
      console.log('生成的专题数据:', processedData.regions)
      
  // 存储当前专题数据用于统计和导出
  currentThematicData.value = processedData
  console.log('存储专题数据用于统计:', currentThematicData.value.length, '条记录')
  
  // 更新地图标题
  if (evaluationData.stepInfo?.stepName) {
    mapConfig.value.title = `${evaluationData.stepInfo.stepName}减灾能力评估专题图`
  }
      
      const thematicData = processedData.regions.map(region => ({
          regionName: region.name,
          capabilityLevel: region.capabilityLevel,
          totalScore: region.score,
          score: region.score,
          disasterPreventionCapability: region.details.disasterPreventionCapability,
          emergencyResponseCapability: region.details.emergencyResponseCapability,
          recoveryReconstructionCapability: region.details.recoveryReconstructionCapability
        }))
        
        console.log('转换后的专题数据用于统计:', thematicData)
        
        // 存储当前专题数据用于统计和导出
        currentThematicData.value = thematicData
        console.log('存储专题数据用于统计:', currentThematicData.value.length, '条记录')
        
        // 渲染专题图层
        renderThematicLayer(processedData)
      return true
    }
  } catch (error) {
    console.error('读取评估数据失败:', error)
  }
  return false
}

// 加载专题数据
const loadThematicData = async () => {
  console.log('=== 开始加载专题数据 ===')
  
  // 优先加载真实的GeoJSON边界数据
  try {
    console.log('步骤1: 加载真实边界数据')
    const boundaries = await loadRealBoundaryData()
    console.log('边界数据加载完成，类型:', boundaries.type, '特征数量:', boundaries.features?.length)
    
    // 检查是否有从评估计算传递的数据
    const evaluationData = (window as any).evaluationData
    console.log('从window.evaluationData读取数据:', evaluationData)
    
    let thematicData = []
    if (evaluationData && evaluationData.tableData) {
      console.log('找到评估数据，基于真实边界生成专题数据...')
      
      // 基于真实边界和评估数据生成专题图数据
      console.log('评估数据条数:', evaluationData.tableData.length, '边界特征数量:', boundaries.features.length)
      
      // 如果评估数据多于边界数据，需要为每条评估数据找到对应的边界或生成边界
      if (evaluationData.tableData.length > boundaries.features.length) {
        console.log('评估数据多于边界数据，为每条评估数据生成专题数据')
        thematicData = evaluationData.tableData.map((evaluationRow: any, index: number) => {
          const regionName = evaluationRow.region || evaluationRow.地区名称 || evaluationRow.name || `区域${index + 1}`
          const totalScore = parseFloat(evaluationRow.totalScore || evaluationRow.总分 || evaluationRow.综合得分 || evaluationRow.score || 0)
          
          // 直接使用二维表中的综合能力分级字段（优先使用第5步的分级结果）
          const capabilityLevel = evaluationRow.comprehensiveCapabilityGrade || 
                                 evaluationRow.综合能力分级 || 
                                 evaluationRow.totalGrade ||
                                 evaluationRow.comprehensiveCapability || 
                                 evaluationRow.management_grade || '中等'
          
          // 尝试找到匹配的边界特征，使用更精确的匹配逻辑
          const matchingFeature = boundaries.features.find((feature: any) => {
            const featureName = feature.properties?.xiang || feature.properties?.name || feature.properties?.NAME || feature.properties?.XIANG
            if (!featureName || !regionName) return false
            
            // 精确匹配或包含匹配
            return featureName === regionName || 
                   featureName.includes(regionName) || 
                   regionName.includes(featureName) ||
                   // 去除"镇"、"街道"等后缀再匹配
                   featureName.replace(/[镇街道]/g, '') === regionName.replace(/[镇街道]/g, '') ||
                   regionName.replace(/[镇街道]/g, '') === featureName.replace(/[镇街道]/g, '')
          }) || boundaries.features[index % boundaries.features.length] // 如果找不到匹配的，循环使用边界
          
          console.log(`评估数据 "${regionName}" 匹配到边界特征:`, matchingFeature?.properties?.xiang || matchingFeature?.properties?.name)
          
          return {
            regionId: index + 1,
            regionName: regionName,
            county: matchingFeature.properties?.COUNTY || '青神县',
            score: totalScore,
            totalScore: totalScore, // 添加totalScore字段
            capabilityLevel: capabilityLevel,
            geometry: matchingFeature.geometry,
            properties: matchingFeature.properties,
            details: {
              disasterPreventionCapability: parseFloat(evaluationRow.disasterPreventionCapability || evaluationRow.灾害预防能力 || evaluationRow.防灾能力 || 0),
              emergencyResponseCapability: parseFloat(evaluationRow.emergencyResponseCapability || evaluationRow.应急响应能力 || evaluationRow.应急能力 || 0),
              recoveryReconstructionCapability: parseFloat(evaluationRow.recoveryReconstructionCapability || evaluationRow.恢复重建能力 || evaluationRow.重建能力 || 0)
            }
          }
        })
      } else {
        // 原有逻辑：基于边界数据生成专题数据
        if (boundaries && boundaries.features && Array.isArray(boundaries.features)) {
          thematicData = boundaries.features
            .filter((feature: any) => feature && feature.properties) // 过滤掉undefined或无properties的特征
            .map((feature: any, index: number) => {
              const featureRegionName = feature.properties?.xiang || feature.properties?.name || feature.properties?.NAME || feature.properties?.XIANG || `区域${index + 1}`
              
              // 尝试找到匹配的评估数据
              const evaluationRow = evaluationData.tableData.find((row: any) => {
                const rowRegionName = row.regionName || row.地区名称 || row.name
                if (!rowRegionName || !featureRegionName) return false
                
                // 精确匹配或包含匹配
                return rowRegionName === featureRegionName || 
                       rowRegionName.includes(featureRegionName) || 
                       featureRegionName.includes(rowRegionName) ||
                       // 去除"镇"、"街道"等后缀再匹配
                       rowRegionName.replace(/[镇街道]/g, '') === featureRegionName.replace(/[镇街道]/g, '') ||
                       featureRegionName.replace(/[镇街道]/g, '') === rowRegionName.replace(/[镇街道]/g, '')
              }) || evaluationData.tableData[index % evaluationData.tableData.length] // 如果找不到匹配的，循环使用评估数据
              
              console.log(`边界特征 "${featureRegionName}" 匹配到评估数据:`, evaluationRow?.regionName || evaluationRow?.地区名称)
              
              const totalScore = parseFloat(evaluationRow?.totalScore || evaluationRow?.总分 || evaluationRow?.综合得分 || evaluationRow?.score || 0)
              
              // 优先使用第5步的分级结果
              const capabilityLevel = evaluationRow?.comprehensiveCapabilityGrade || 
                                     evaluationRow?.综合能力分级 || 
                                     evaluationRow?.totalGrade ||
                                     evaluationRow?.disasterManagement || 
                                     evaluationRow?.灾害管理能力 || 
                                     evaluationRow?.management_grade || '中等'
              
              return {
                regionId: index + 1,
                regionName: featureRegionName,
                county: feature.properties?.COUNTY || '青神县',
                score: totalScore,
                totalScore: totalScore, // 添加totalScore字段
                capabilityLevel: capabilityLevel,
                geometry: feature.geometry,
                properties: feature.properties,
                details: {
                  disasterPreventionCapability: parseFloat(evaluationRow?.disasterManagement || evaluationRow?.灾害管理能力值 || evaluationRow?.disasterPreventionCapability || evaluationRow?.灾害预防能力 || evaluationRow?.防灾能力 || 0),
                  emergencyResponseCapability: parseFloat(evaluationRow?.disasterPreparedness || evaluationRow?.灾害备灾能力值 || evaluationRow?.emergencyResponseCapability || evaluationRow?.应急响应能力 || evaluationRow?.应急能力 || 0),
                  recoveryReconstructionCapability: parseFloat(evaluationRow?.selfRescueTransfer || evaluationRow?.自救转移能力值 || evaluationRow?.recoveryReconstructionCapability || evaluationRow?.恢复重建能力 || evaluationRow?.重建能力 || 0)
                }
              }
            })
        } else {
          console.warn('边界数据无效，使用空数组')
          thematicData = []
        }
      }
      
      // 更新地图标题
      if (evaluationData.stepInfo?.stepName) {
        mapConfig.value.title = `${evaluationData.stepInfo.stepName}减灾能力评估专题图`
      }
    } else {
      console.log('未找到评估数据，基于真实边界生成模拟数据')
      thematicData = generateThematicDataFromBoundaries(boundaries)
    }
    
    // 使用真实边界数据和专题数据进行渲染
    const processedData = {
      boundaries: boundaries,
      data: thematicData
    }
    
    console.log('准备渲染真实边界数据:', processedData)
    
    // 存储当前专题数据用于统计和导出
    currentThematicData.value = thematicData
    console.log('存储专题数据用于统计:', currentThematicData.value.length, '条记录')
    
    renderThematicLayer(processedData)
    return
  } catch (error) {
    console.error('加载真实边界数据失败，尝试备用方案:', error)
  }
  
  // 备用方案：从sessionStorage读取数据
  if (loadDataFromSession()) {
    console.log('使用备用方案：从sessionStorage加载数据成功')
    return
  }
  
  try {
    console.log('步骤1: 加载真实边界数据')
    
    // 加载真实的边界数据
    const boundaries = await loadRealBoundaryData()
    
    // 检查边界数据是否有效
    if (!boundaries || !boundaries.features || !Array.isArray(boundaries.features)) {
      throw new Error('边界数据无效或为空')
    }
    
    console.log('边界数据加载完成，类型:', boundaries.type, '特征数量:', boundaries.features?.length)
    
    // 尝试从API获取专题数据
    let thematicData = []
    try {
      console.log('步骤2: 尝试从API获取专题数据')
      const response = await thematicMapApi.getThematicData({
        reportId: props.reportId,
        surveyId: 1, // 默认调查ID
        algorithmId: 1 // 默认算法ID
      })
      
      if (response.data && response.data.length > 0) {
        thematicData = response.data
        console.log('从API加载专题数据成功:', response)
      } else {
        throw new Error('API响应数据为空')
      }
    } catch (apiError) {
      console.log('步骤2失败: API不可用，生成基于真实边界的模拟数据')
      // 基于真实边界数据生成模拟的专题数据
      thematicData = generateThematicDataFromBoundaries(boundaries)
      console.log('生成的模拟专题数据:', thematicData.slice(0, 3))
    }
    
    // 直接使用真实边界数据和专题数据进行渲染
    const processedData = {
      boundaries: boundaries,
      data: thematicData
    }
    
    console.log('步骤3: 准备渲染数据')
    console.log('边界特征数量:', processedData.boundaries.features?.length)
    console.log('专题数据数量:', processedData.data?.length)
    
    // 渲染专题图层
    renderThematicLayer(processedData)
    
    // 存储当前专题数据用于统计和导出
    currentThematicData.value = thematicData
    console.log('存储专题数据用于统计:', currentThematicData.value.length, '条记录')
    
    console.log('=== 专题数据加载完成 ===')
  } catch (error) {
    console.error('加载专题数据失败:', error)
    ElMessage.error('加载专题数据失败')
    
    // 出错时使用备用边界数据
    try {
      console.log('使用备用方案')
      const boundaries = generateFallbackBoundaries()
      
      // 检查备用边界数据是否有效
      if (boundaries && boundaries.features && Array.isArray(boundaries.features)) {
        const fallbackData = {
           boundaries: boundaries,
           data: generateThematicDataFromBoundaries(boundaries)
         }
        console.log('使用备用数据渲染:', fallbackData)
        renderThematicLayer(fallbackData)
      } else {
        throw new Error('备用边界数据也无效')
      }
    } catch (fallbackError) {
      console.error('加载备用数据也失败:', fallbackError)
      ElMessage.error('无法加载地图数据')
    }
  }
}



// 基于真实边界数据生成专题数据
const generateThematicDataFromBoundaries = (boundaries: any) => {
  if (!boundaries || !boundaries.features || !Array.isArray(boundaries.features)) {
    console.warn('边界数据无效，返回空数组')
    return []
  }
  
  // 为有数据的乡镇生成真实的专题数据
  const regionScores = {
    '青竹街道': { score: 85, disasterPrevention: 82, emergencyResponse: 88, recovery: 85 },
    '汉阳镇': { score: 72, disasterPrevention: 75, emergencyResponse: 68, recovery: 73 },
    '瑞峰镇': { score: 78, disasterPrevention: 80, emergencyResponse: 75, recovery: 79 }
  }
  
  return boundaries.features
    .filter((feature: any) => feature && feature.properties && feature.geometry) // 过滤无效特征
    .map((feature: any, index: number) => {
    const regionName = feature.properties?.xiang || feature.properties?.name || `区域${index + 1}`
    
    // 使用预定义的分数，如果没有则生成随机分数
    const regionData = regionScores[regionName as keyof typeof regionScores] || {
      score: Math.floor(Math.random() * 40) + 60,
      disasterPrevention: Math.floor(Math.random() * 30) + 70,
      emergencyResponse: Math.floor(Math.random() * 30) + 70,
      recovery: Math.floor(Math.random() * 30) + 70
    }
    
    return {
      regionId: index + 1,
      regionName: regionName,
      county: feature.properties?.COUNTY || '青神县',
      score: regionData.score,
      capabilityLevel: regionData.score >= 90 ? '强' : regionData.score >= 80 ? '较强' : regionData.score >= 70 ? '中等' : '较弱',
      geometry: feature.geometry,
      properties: feature.properties,
      details: {
        disasterPreventionCapability: regionData.disasterPrevention,
        emergencyResponseCapability: regionData.emergencyResponse,
        recoveryReconstructionCapability: regionData.recovery
      },
      evaluationData: {
        totalScore: regionData.score,
        details: {
          infrastructure: regionData.disasterPrevention,
          environment: regionData.emergencyResponse,
          economy: regionData.recovery,
          social: Math.floor((regionData.disasterPrevention + regionData.emergencyResponse + regionData.recovery) / 3)
        }
      }
    }
  })
}

// 更新地图要素显示
const updateMapElements = () => {
  // 强制重新渲染
  nextTick(() => {
    console.log('更新地图要素:', mapConfig.value)
  })
}

// 渲染专题图层
const renderThematicLayer = (data: any) => {
  if (!map.value || !data) return
  
  console.log('开始渲染专题图层:', data)
  
  // 清除现有图层
  if (thematicLayer.value) {
    map.value.removeLayer(thematicLayer.value)
  }
  
  // 创建新的图层组
  thematicLayer.value = L.layerGroup().addTo(map.value)
  
  // 渲染边界数据
  if (data.boundaries && data.boundaries.features && Array.isArray(data.boundaries.features)) {
    console.log(`渲染 ${data.boundaries.features.length} 个边界要素`)
    
    data.boundaries.features
      .filter((feature: any) => feature && feature.properties && feature.geometry) // 过滤无效特征
      .forEach((feature: any, index: number) => {
      // 获取边界特征的区域名称（多种可能的属性名）
      const featureRegionName = feature.properties?.xiang || feature.properties?.name || feature.properties?.NAME || feature.properties?.XIANG
      
      console.log(`处理边界特征 ${index}:`, featureRegionName, '属性:', feature.properties)
      
      // 从专题数据中查找对应的数据，使用更精确的匹配逻辑
      let thematicInfo = null
      
      if (data.data && Array.isArray(data.data)) {
        thematicInfo = data.data.find((item: any) => {
          if (!item) return false
          
          // 获取专题数据中的区域名称
          const itemRegionName = item.regionName || item.name || item.county
          
          if (!itemRegionName || !featureRegionName) return false
          
          // 精确匹配或包含匹配
          return itemRegionName === featureRegionName || 
                 itemRegionName.includes(featureRegionName) || 
                 featureRegionName.includes(itemRegionName) ||
                 // 去除"镇"、"街道"等后缀再匹配
                 itemRegionName.replace(/[镇街道]/g, '') === featureRegionName.replace(/[镇街道]/g, '') ||
                 featureRegionName.replace(/[镇街道]/g, '') === itemRegionName.replace(/[镇街道]/g, '')
        })
        
        console.log(`边界特征 "${featureRegionName}" 匹配到的专题数据:`, thematicInfo)
      }
      
      // 如果没有找到匹配的数据，使用默认值或按索引匹配
      if (!thematicInfo) {
        if (data.data && data.data[index]) {
          thematicInfo = data.data[index]
          console.log(`使用索引 ${index} 匹配的数据:`, thematicInfo)
        } else {
          thematicInfo = {
            regionName: featureRegionName,
            value: Math.floor(Math.random() * 40) + 60,
            score: Math.floor(Math.random() * 40) + 60,
            capabilityLevel: 'medium'
          }
          console.log(`使用默认数据:`, thematicInfo)
        }
      }
      
      // 使用score字段，确保数据正确
      const scoreValue = parseFloat(thematicInfo.score || thematicInfo.totalScore || thematicInfo.value || 0)
      // 直接使用专题数据中的capabilityLevel，不再根据分数计算
      const capabilityLevel = thematicInfo.capabilityLevel || '中等'
      const color = getCapabilityColor(capabilityLevel)
      
      console.log(`最终渲染数据 - 区域: ${featureRegionName}, 分数: ${scoreValue}, 等级: ${capabilityLevel}, 颜色: ${color}`)
      
      const layer = L.geoJSON(feature, {
        style: {
          fillColor: color,
          weight: 1,
          opacity: 0.8,
          color: '#333',
          fillOpacity: 0.6
        }
      }).bindPopup(`
        <div style="min-width: 200px;">
          <h4 style="margin: 0 0 10px 0; color: #333;">${feature.properties?.COUNTY || '未知县'} - ${featureRegionName || '未知乡镇'}</h4>
          <p style="margin: 5px 0;"><strong>灾害管理能力:</strong> <span style="color: ${color}; font-weight: bold;">${getCapabilityText(capabilityLevel)}</span></p>
          <p style="margin: 5px 0;"><strong>评估分数:</strong> ${scoreValue.toFixed(2)}</p>
          <p style="margin: 5px 0;"><strong>综合减灾能力值:</strong> ${scoreValue.toFixed(2)}</p>
          <p style="margin: 5px 0;"><strong>行政区划:</strong> ${feature.properties?.CITY || '未知市州'}</p>
          <p style="margin: 5px 0;"><strong>面积:</strong> ${feature.properties?.Shape_Area ? (feature.properties.Shape_Area * 100000000).toFixed(2) + ' 平方米' : 'N/A'}</p>
          ${thematicInfo.details ? `
          <hr style="margin: 10px 0;">
          <p style="margin: 5px 0;"><strong>灾害预防能力:</strong> ${thematicInfo.details.disasterPreventionCapability}</p>
          <p style="margin: 5px 0;"><strong>应急响应能力:</strong> ${thematicInfo.details.emergencyResponseCapability}</p>
          <p style="margin: 5px 0;"><strong>恢复重建能力:</strong> ${thematicInfo.details.recoveryReconstructionCapability}</p>
          ` : ''}
        </div>
      `)
      
      // 添加鼠标悬停效果
      layer.on('mouseover', function(e: any) {
        const layer = e.target
        layer.setStyle({
          weight: 3,
          opacity: 1,
          fillOpacity: 0.8
        })
      })
      
      layer.on('mouseout', function(e: any) {
        const layer = e.target
        layer.setStyle({
          weight: 1,
          opacity: 0.8,
          fillOpacity: 0.6
        })
      })
      
      if (thematicLayer.value) {
        thematicLayer.value.addLayer(layer)
      }
    })
    
    // 调整地图视图到数据范围
    try {
      const bounds = L.geoJSON(data.boundaries).getBounds()
      map.value.fitBounds(bounds, { padding: [20, 20] })
      console.log('地图视图已调整到数据范围')
    } catch (error) {
      console.error('调整地图视图失败:', error)
    }
  } else if (data.regions) {
    // 兼容原有的regions数据格式
    data.regions.forEach((region: any, index: number) => {
      console.log(`渲染区域 ${index + 1}:`, {
        name: region.name,
        coordinates: region.coordinates,
        capabilityLevel: region.capabilityLevel,
        coordinatesLength: region.coordinates.length
      })
      
      // 根据灾害管理能力等级设置颜色
      const color = getCapabilityColor(region.capabilityLevel)
      
      // 创建多边形图层
      const polygon = L.polygon(region.coordinates, {
        fillColor: color,
        fillOpacity: 0.7,
        color: '#333',
        weight: 2,
        dashArray: null
      })
      
      // 添加弹窗信息
      let popupContent = `
        <div class="region-popup">
          <h4>${region.name}</h4>
          <p>灾害管理能力: ${getCapabilityText(region.capabilityLevel)}</p>
          <p>评估得分: ${region.score}</p>
      `
      
      if (region.details) {
        popupContent += `
          <p>灾害预防能力: ${region.details.disasterPreventionCapability}</p>
          <p>应急响应能力: ${region.details.emergencyResponseCapability}</p>
          <p>恢复重建能力: ${region.details.recoveryReconstructionCapability}</p>
        `
      }
      
      popupContent += `</div>`
      
      polygon.bindPopup(popupContent)
      
      if (thematicLayer.value) {
        thematicLayer.value.addLayer(polygon)
      } else {
        polygon.addTo(map.value!)
      }
    })
  }
  
  console.log('专题图层渲染完成')
}



// 根据数值计算风险等级（已移至文件末尾统一定义）

// 更新比例尺
const updateScale = () => {
  if (!map.value) return
  
  const zoom = map.value.getZoom()
  const scale = calculateScale(zoom)
  scaleText.value = scale
}

// 计算比例尺文本
const calculateScale = (zoom: number): string => {
  const scales: Record<number, string> = {
    8: '0    20    40 km',
    9: '0    10    20 km',
    10: '0    5    10 km',
    11: '0    2    4 km',
    12: '0    1    2 km'
  }
  return scales[zoom] || '0    5    10 km'
}

// 导出为PNG
const exportAsPNG = async () => {
  try {
    if (!mapContainer.value) return
    
    const canvas = await html2canvas(mapContainer.value, {
      useCORS: true,
      scale: 2, // 提高分辨率
      backgroundColor: '#ffffff'
    })
    
    // 下载图片
    const link = document.createElement('a')
    link.download = `专题图_${new Date().getTime()}.png`
    link.href = canvas.toDataURL('image/png')
    link.click()
    
    // 保存到后端
    await saveToServer(canvas.toDataURL('image/png'), 'png')
    
    ElMessage.success('PNG导出成功')
  } catch (error) {
    console.error('PNG导出失败:', error)
    ElMessage.error('PNG导出失败')
  }
}

// 导出为PDF
const exportAsPDF = async () => {
  try {
    if (!mapContainer.value) return
    
    const canvas = await html2canvas(mapContainer.value, {
      useCORS: true,
      scale: 2
    })
    
    const pdf = new jsPDF('landscape', 'mm', 'a4')
    const imgData = canvas.toDataURL('image/png')
    
    // 计算图片在PDF中的尺寸
    const pdfWidth = pdf.internal.pageSize.getWidth()
    const pdfHeight = pdf.internal.pageSize.getHeight()
    const imgWidth = canvas.width
    const imgHeight = canvas.height
    const ratio = Math.min(pdfWidth / imgWidth, pdfHeight / imgHeight)
    
    const finalWidth = imgWidth * ratio
    const finalHeight = imgHeight * ratio
    
    pdf.addImage(imgData, 'PNG', 0, 0, finalWidth, finalHeight)
    pdf.save(`专题图_${new Date().getTime()}.pdf`)
    
    ElMessage.success('PDF导出成功')
  } catch (error) {
    console.error('PDF导出失败:', error)
    ElMessage.error('PDF导出失败')
  }
}

// 统计数据计算函数
const getStatistics = () => {
  const stats = {
    strong: 0,
    mediumStrong: 0,
    medium: 0,
    weak: 0,
    veryWeak: 0,
    total: 0
  }
  
  console.log('统计数据计算，当前专题数据:', currentThematicData.value)
  
  currentThematicData.value.forEach(item => {
    stats.total++
    
    // 优先使用第5步的分级结果（综合能力分级）
    let level = item.comprehensiveCapabilityGrade || item.综合能力分级 || item.capabilityLevel
    
    // 如果没有分级结果，尝试从其他字段获取
    if (!level) {
      level = item.comprehensiveCapability || item.level || '未知'
      
      // 如果comprehensiveCapability是数值，需要根据数值范围判断等级
      if (typeof level === 'number' || (typeof level === 'string' && !isNaN(parseFloat(level)))) {
        const numValue = typeof level === 'number' ? level : parseFloat(level)
        // 根据TOPSIS算法结果的数值范围进行分级（0-1之间）
        if (numValue >= 0.8) level = '强'
        else if (numValue >= 0.6) level = '较强'
        else if (numValue >= 0.4) level = '中等'
        else if (numValue >= 0.2) level = '较弱'
        else level = '弱'
      }
    }
    
    console.log(`区域: ${item.regionName}, 能力等级: ${level}`)
    
    switch (level) {
      case '强':
        stats.strong++
        break
      case '较强':
        stats.mediumStrong++
        break
      case '中等':
        stats.medium++
        break
      case '较弱':
        stats.weak++
        break
      case '弱':
        stats.veryWeak++
        break
      default:
        console.warn(`未知的能力等级: ${level}，将归类为中等`)
        stats.medium++
    }
  })
  
  console.log('统计结果:', stats)
  
  return {
    ...stats,
    strongPercent: stats.total > 0 ? ((stats.strong / stats.total) * 100).toFixed(1) : '0.0',
    mediumStrongPercent: stats.total > 0 ? ((stats.mediumStrong / stats.total) * 100).toFixed(1) : '0.0',
    mediumPercent: stats.total > 0 ? ((stats.medium / stats.total) * 100).toFixed(1) : '0.0',
    weakPercent: stats.total > 0 ? ((stats.weak / stats.total) * 100).toFixed(1) : '0.0',
    veryWeakPercent: stats.total > 0 ? ((stats.veryWeak / stats.total) * 100).toFixed(1) : '0.0'
  }
}

// 导出为Word文档
const exportAsWord = async () => {
  try {
    // 获取整个地图容器，包括覆盖层元素
    const mapElement = mapContainer.value
    if (!mapElement) {
      ElMessage.error('地图容器未找到')
      return
    }

    // 生成地图截图
    const canvas = await html2canvas(mapElement, {
      useCORS: true,
      allowTaint: true,
      scale: 2,
      backgroundColor: '#ffffff',
      logging: false,
      width: mapElement.offsetWidth,
      height: mapElement.offsetHeight,
      scrollX: 0,
      scrollY: 0
    })

    // 将canvas转换为blob
    const imageBlob = await new Promise<Blob>((resolve) => {
      canvas.toBlob((blob) => {
        resolve(blob!)
      }, 'image/png')
    })

    // 读取图片数据
    const imageArrayBuffer = await imageBlob.arrayBuffer()
    
    // 获取统计数据
    const stats = getStatistics()
    
    // 创建Word文档
    const doc = new Document({
      sections: [{
        properties: {},
        children: [
          // 主标题
          new Paragraph({
            children: [{
              text: mapConfig.value.mainTitle,
              bold: true,
              size: 36
            }],
            alignment: AlignmentType.CENTER,
            spacing: { after: 600 }
          }),
          
          // 副标题
          new Paragraph({
            children: [{
              text: mapConfig.value.title,
              bold: true,
              size: 28
            }],
            alignment: AlignmentType.CENTER,
            spacing: { after: 400 }
          }),
          
          // 地图图片
          new Paragraph({
            children: [
              new ImageRun({
                data: imageArrayBuffer,
                transformation: {
                  width: 600,
                  height: 400
                }
              })
            ],
            alignment: AlignmentType.CENTER,
            spacing: { after: 400 }
          }),
          
          // 统计表格标题
          new Paragraph({
            children: [{
              text: '乡镇（街道）减灾能力统计表',
              bold: true,
              size: 24
            }],
            alignment: AlignmentType.CENTER,
            spacing: { after: 200 }
          }),
          
          // 统计表格
          new Table({
            width: {
              size: 100,
              type: WidthType.PERCENTAGE
            },
            rows: [
              // 表头
              new TableRow({
                children: [
                  new TableCell({ children: [new Paragraph({ text: '强', alignment: AlignmentType.CENTER })] }),
                  new TableCell({ children: [new Paragraph({ text: '较强', alignment: AlignmentType.CENTER })] }),
                  new TableCell({ children: [new Paragraph({ text: '中等', alignment: AlignmentType.CENTER })] }),
                  new TableCell({ children: [new Paragraph({ text: '较弱', alignment: AlignmentType.CENTER })] }),
                  new TableCell({ children: [new Paragraph({ text: '弱', alignment: AlignmentType.CENTER })] }),
                  new TableCell({ children: [new Paragraph({ text: '总数', alignment: AlignmentType.CENTER })] })
                ]
              }),
              // 数量行
              new TableRow({
                children: [
                  new TableCell({ children: [new Paragraph({ text: stats.strong.toString(), alignment: AlignmentType.CENTER })] }),
                  new TableCell({ children: [new Paragraph({ text: stats.mediumStrong.toString(), alignment: AlignmentType.CENTER })] }),
                  new TableCell({ children: [new Paragraph({ text: stats.medium.toString(), alignment: AlignmentType.CENTER })] }),
                  new TableCell({ children: [new Paragraph({ text: stats.weak.toString(), alignment: AlignmentType.CENTER })] }),
                  new TableCell({ children: [new Paragraph({ text: stats.veryWeak.toString(), alignment: AlignmentType.CENTER })] }),
                  new TableCell({ children: [new Paragraph({ text: stats.total.toString(), alignment: AlignmentType.CENTER })] })
                ]
              }),
              // 百分比行
              new TableRow({
                children: [
                  new TableCell({ children: [new Paragraph({ text: `${stats.strongPercent}%`, alignment: AlignmentType.CENTER })] }),
                  new TableCell({ children: [new Paragraph({ text: `${stats.mediumStrongPercent}%`, alignment: AlignmentType.CENTER })] }),
                  new TableCell({ children: [new Paragraph({ text: `${stats.mediumPercent}%`, alignment: AlignmentType.CENTER })] }),
                  new TableCell({ children: [new Paragraph({ text: `${stats.weakPercent}%`, alignment: AlignmentType.CENTER })] }),
                  new TableCell({ children: [new Paragraph({ text: `${stats.veryWeakPercent}%`, alignment: AlignmentType.CENTER })] }),
                  new TableCell({ children: [new Paragraph({ text: '100%', alignment: AlignmentType.CENTER })] })
                ]
              })
            ]
          }),
          
          // 详细数据表格标题
          new Paragraph({
            children: [{
              text: '乡镇（街道）减灾能力详细数据表',
              bold: true,
              size: 24
            }],
            alignment: AlignmentType.CENTER,
            spacing: { after: 200, before: 400 }
          }),
          
          // 详细数据表格
          new Table({
            width: {
              size: 100,
              type: WidthType.PERCENTAGE
            },
            rows: [
              // 详细数据表头
              new TableRow({
                children: [
                  new TableCell({ children: [new Paragraph({ text: '地区', alignment: AlignmentType.CENTER })] }),
                  new TableCell({ children: [new Paragraph({ text: '灾害管理能力值', alignment: AlignmentType.CENTER })] }),
                  new TableCell({ children: [new Paragraph({ text: '灾害备灾能力值', alignment: AlignmentType.CENTER })] }),
                  new TableCell({ children: [new Paragraph({ text: '自救转移能力值', alignment: AlignmentType.CENTER })] }),
                  new TableCell({ children: [new Paragraph({ text: '综合减灾能力值', alignment: AlignmentType.CENTER })] }),
                  new TableCell({ children: [new Paragraph({ text: '灾害管理分级', alignment: AlignmentType.CENTER })] }),
                  new TableCell({ children: [new Paragraph({ text: '灾害备灾分级', alignment: AlignmentType.CENTER })] }),
                  new TableCell({ children: [new Paragraph({ text: '自救转移分级', alignment: AlignmentType.CENTER })] }),
                  new TableCell({ children: [new Paragraph({ text: '综合能力分级', alignment: AlignmentType.CENTER })] })
                ]
              }),
              // 详细数据行
              ...currentThematicData.value.map(item => 
                new TableRow({
                  children: [
                    new TableCell({ children: [new Paragraph({ text: item.name || item.regionName || '未知', alignment: AlignmentType.CENTER })] }),
                    new TableCell({ children: [new Paragraph({ text: (item.details?.disasterPreventionCapability || 0).toFixed(4), alignment: AlignmentType.CENTER })] }),
                    new TableCell({ children: [new Paragraph({ text: (item.details?.emergencyResponseCapability || 0).toFixed(4), alignment: AlignmentType.CENTER })] }),
                    new TableCell({ children: [new Paragraph({ text: (item.details?.recoveryReconstructionCapability || 0).toFixed(4), alignment: AlignmentType.CENTER })] }),
                    new TableCell({ children: [new Paragraph({ text: (item.score || 0).toFixed(4), alignment: AlignmentType.CENTER })] }),
                    new TableCell({ children: [new Paragraph({ text: item.details?.disasterManagementGrade || item.disasterManagementGrade || '中等', alignment: AlignmentType.CENTER })] }),
                    new TableCell({ children: [new Paragraph({ text: item.details?.disasterPreparednessGrade || item.disasterPreparednessGrade || '中等', alignment: AlignmentType.CENTER })] }),
                    new TableCell({ children: [new Paragraph({ text: item.details?.selfRescueTransferGrade || item.selfRescueTransferGrade || '中等', alignment: AlignmentType.CENTER })] }),
                    new TableCell({ children: [new Paragraph({ text: item.details?.comprehensiveCapabilityGrade || item.comprehensiveCapabilityGrade || item.capabilityLevel || '中等', alignment: AlignmentType.CENTER })] })
                  ]
                })
              )
            ]
          })
        ]
      }]
    })

    // 生成Word文档 - 使用toBlob方法以支持浏览器环境
    const blob = await Packer.toBlob(doc)
    
    // 创建下载链接
    const url = URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.download = `专题图报告_${new Date().getTime()}.docx`
    link.href = url
    link.click()
    
    // 清理URL对象
    URL.revokeObjectURL(url)

    ElMessage.success('Word文档导出成功')
  } catch (error) {
    console.error('Word导出失败:', error)
    ElMessage.error('Word导出失败')
  }
}

// 处理专题数据
const processThematicData = (thematicData: any[], regionData: any[]) => {
  // 存储当前数据用于统计
  currentThematicData.value = thematicData
  
  const regions = thematicData.map(item => {
    const region = regionData.find(r => r.id === item.regionId)
    return {
      name: region?.name || item.regionName,
      coordinates: item.boundaries || generateMockBoundaries(item.regionId),
      capabilityLevel: calculateCapabilityLevel(item.score),
      score: item.score
    }
  })
  
  return { regions }
}

// 加载真实的乡镇边界数据
const loadRealBoundaryData = async () => {
  try {
    console.log('开始加载真实边界数据')
    const response = await fetch('/public/shp.geojson')
    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`)
    }
    const data = await response.json()
    
    // 检查数据有效性
    if (!data || !data.features || !Array.isArray(data.features)) {
      throw new Error('边界数据格式无效')
    }
    
    // 过滤掉无效的features
    data.features = data.features.filter((feature: any) => feature && feature.properties && feature.geometry)
    
    console.log('真实边界数据加载成功，特征数量:', data.features?.length)
    // console.log('前3个特征的属性:', data.features?.slice(0, 3).map((f: any) => f.properties))
    
    // 检查是否有评估数据，如果有则根据评估数据中的乡镇名称过滤边界数据
    const evaluationData = (window as any).evaluationData
    if (evaluationData && evaluationData.tableData && evaluationData.tableData.length > 0) {
      const countyName = evaluationData.countyName || '青神县'; // 默认县名
      console.log('根据评估数据过滤边界数据，县名:', countyName, '评估数据条数:', evaluationData.tableData.length);

      // 从评估数据中提取所有乡镇名称
      const townshipNames = evaluationData.tableData
        .filter((row: any) => row) // 先过滤掉undefined的行
        .map((row: any) => {
          return row.township || row.region || row.地区名称 || row.name;
        })
        .filter((name: string) => name); // 过滤掉空值

      console.log('提取的乡镇名称:', townshipNames);

      // 根据乡镇名称和县名过滤边界数据
      const filteredFeatures = data.features
        .filter((feature: any) => feature && feature.properties) // 先过滤掉undefined或无properties的feature
        .filter((feature: any) => {
          const featureCounty = feature.properties.COUNTY || feature.properties.county;
          if (featureCounty !== countyName) {
            return false;
          }
          const regionName = feature.properties?.xiang || feature.properties?.name || feature.properties?.NAME;
          return townshipNames.some((townshipName: string) => 
            regionName && (regionName.includes(townshipName) || townshipName.includes(regionName))
          );
        });
      
      console.log('过滤后的边界特征数量:', filteredFeatures.length)
      
      // 返回过滤后的数据
      const filteredData = {
        ...data,
        features: filteredFeatures
      }
      
      return filteredData
    }
    
    // 如果没有评估数据，使用原有的过滤逻辑
    const targetRegions = ['青竹街道', '汉阳镇', '瑞峰镇']
    console.log('目标乡镇列表:', targetRegions)
    
    // 过滤边界数据，只保留有数据的乡镇
    const filteredFeatures = data.features
      .filter((feature: any) => feature && feature.properties) // 先过滤掉undefined或无properties的feature
      .filter((feature: any) => {
        const regionName = feature.properties?.xiang || feature.properties?.name || feature.properties?.NAME
        const isTarget = targetRegions.some(target => 
          regionName && (regionName.includes(target) || target.includes(regionName))
        )
        if (isTarget) {
          console.log('匹配到目标乡镇:', regionName, '属性:', feature.properties)
        }
        return isTarget
      })
    
    console.log(`过滤后的边界数据，从 ${data.features?.length} 个特征过滤到 ${filteredFeatures.length} 个特征`)
    
    // 返回过滤后的数据
    const filteredData = {
      ...data,
      features: filteredFeatures
    }
    
    return filteredData
  } catch (error) {
    console.error('加载真实边界数据失败:', error)
    return generateFallbackBoundaries()
  }
}

// 生成备用边界数据（当真实数据加载失败时使用）
const generateFallbackBoundaries = () => {
  return {
    type: 'FeatureCollection',
    features: [
      {
        type: 'Feature',
        properties: {
          name: '青竹街道',
          COUNTY: '青神县',
          xiang: '青竹街道'
        },
        geometry: {
          type: 'Polygon',
          coordinates: [[
            [103.84, 29.83],
            [103.86, 29.83],
            [103.86, 29.85],
            [103.84, 29.85],
            [103.84, 29.83]
          ]]
        }
      },
      {
        type: 'Feature',
        properties: {
          name: '汉阳镇',
          COUNTY: '青神县',
          xiang: '汉阳镇'
        },
        geometry: {
          type: 'Polygon',
          coordinates: [[
            [103.82, 29.81],
            [103.84, 29.81],
            [103.84, 29.83],
            [103.82, 29.83],
            [103.82, 29.81]
          ]]
        }
      },
      {
        type: 'Feature',
        properties: {
          name: '瑞峰镇',
          COUNTY: '青神县',
          xiang: '瑞峰镇'
        },
        geometry: {
          type: 'Polygon',
          coordinates: [[
            [103.80, 29.79],
            [103.82, 29.79],
            [103.82, 29.81],
            [103.80, 29.81],
            [103.80, 29.79]
          ]]
        }
      }
    ]
  }
}

// 生成模拟边界数据 - 使用更真实的乡镇边界形状
const generateMockBoundaries = (regionId: number) => {
  const mockBoundaries = {
    1: [ // 青神县城区 - 不规则多边形
      [30.0672, 103.9378], [30.0680, 103.9420], [30.0690, 103.9450],
      [30.0685, 103.9480], [30.0675, 103.9520], [30.0660, 103.9540],
      [30.0640, 103.9550], [30.0620, 103.9545], [30.0600, 103.9530],
      [30.0590, 103.9510], [30.0585, 103.9480], [30.0590, 103.9450],
      [30.0600, 103.9420], [30.0620, 103.9390], [30.0640, 103.9375],
      [30.0660, 103.9370], [30.0672, 103.9378]
    ],
    2: [ // 南城镇 - 不规则多边形
      [30.0372, 103.9278], [30.0385, 103.9290], [30.0395, 103.9320],
      [30.0400, 103.9350], [30.0390, 103.9380], [30.0375, 103.9400],
      [30.0360, 103.9420], [30.0340, 103.9430], [30.0320, 103.9425],
      [30.0300, 103.9410], [30.0290, 103.9390], [30.0285, 103.9360],
      [30.0290, 103.9330], [30.0305, 103.9300], [30.0325, 103.9280],
      [30.0350, 103.9275], [30.0372, 103.9278]
    ],
    3: [ // 西龙镇 - 不规则多边形
      [30.0772, 103.9178], [30.0790, 103.9190], [30.0805, 103.9220],
      [30.0815, 103.9250], [30.0810, 103.9280], [30.0800, 103.9310],
      [30.0785, 103.9330], [30.0765, 103.9340], [30.0745, 103.9335],
      [30.0725, 103.9320], [30.0710, 103.9300], [30.0705, 103.9270],
      [30.0710, 103.9240], [30.0725, 103.9210], [30.0745, 103.9185],
      [30.0765, 103.9175], [30.0772, 103.9178]
    ],
    4: [ // 黑龙镇 - 不规则多边形
      [30.0472, 103.9578], [30.0490, 103.9590], [30.0505, 103.9620],
      [30.0515, 103.9650], [30.0510, 103.9680], [30.0495, 103.9710],
      [30.0475, 103.9730], [30.0450, 103.9735], [30.0425, 103.9720],
      [30.0405, 103.9700], [30.0395, 103.9670], [30.0400, 103.9640],
      [30.0415, 103.9610], [30.0435, 103.9585], [30.0455, 103.9575],
      [30.0472, 103.9578]
    ]
  }
  
  // 如果有预定义的边界数据，使用它；否则生成基于regionId的边界
  if (mockBoundaries[regionId as keyof typeof mockBoundaries]) {
    return mockBoundaries[regionId as keyof typeof mockBoundaries]
  }
  
  // 为其他regionId生成不规则边界
  const baseCoords = [30.0572, 103.9478]
  const offsetX = (regionId % 3 - 1) * 0.03
  const offsetY = Math.floor(regionId / 3) * 0.03
  const centerX = baseCoords[0] + offsetY
  const centerY = baseCoords[1] + offsetX
  
  // 生成不规则多边形（模拟真实乡镇边界）
  const points = []
  const numPoints = 8 + (regionId % 4) // 8-11个点
  for (let i = 0; i < numPoints; i++) {
    const angle = (i / numPoints) * 2 * Math.PI
    const radius = 0.015 + (Math.sin(angle * 3) * 0.005) // 变化的半径
    const x = centerX + Math.cos(angle) * radius
    const y = centerY + Math.sin(angle) * radius
    points.push([x, y])
  }
  // 闭合多边形
  points.push(points[0])
  
  return points
}



// 保存到服务器
const saveToServer = async (imageData: string, format: string) => {
  try {
    await thematicMapApi.saveMapImage({
      imageData,
      format,
      reportId: props.reportId,
      title: mapConfig.value.title,
      description: `专题图导出 - ${format.toUpperCase()}格式`
    })
    console.log('保存到服务器成功:', { format, reportId: props.reportId })
  } catch (error) {
    console.error('保存到服务器失败:', error)
    ElMessage.warning('图片已导出，但保存到服务器失败')
  }
}

// 组件挂载
onMounted(async () => {
  await nextTick()
  console.log('ThematicMapGenerator组件开始初始化')
  initMap()
  
  // 等待地图初始化完成
  setTimeout(() => {
    console.log('开始加载专题数据')
    loadThematicData()
  }, 1000)
})
</script>

<style scoped lang="scss">
.thematic-map-container {
  position: relative;
  width: 100%;
  height: 800px;
  min-height: 800px;
  background: #fff;
  
  // 全屏模式
  &.fullscreen {
    position: fixed;
    top: 0;
    left: 0;
    width: 100vw;
    height: 100vh;
    z-index: 9999;
    background: #fff;
  }
  
  .map-content {
    width: 100%;
    height: 100%;
    position: relative;
  }
  
  .map-elements-overlay {
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    pointer-events: none;
    z-index: 1000;
  }
  
  // 标题样式
  .map-title {
    position: absolute;
    top: 20px;
    left: 50%;
    transform: translateX(-50%);
    text-align: center;
    background: rgba(255, 255, 255, 0.9);
    padding: 15px 30px;
    border-radius: 8px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
    
    h2 {
      margin: 0 0 5px 0;
      font-size: 18px;
      font-weight: bold;
      color: #333;
    }
    
    .subtitle {
      margin: 0;
      font-size: 12px;
      color: #666;
    }
  }
  
  // 图例样式 - 移动到左下角
  .map-legend {
    position: absolute;
    bottom: 20px;
    left: 20px;
    background: rgba(255, 255, 255, 0.95);
    padding: 15px;
    border-radius: 8px;
    box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
    z-index: 1000;
    min-width: 120px;
    
    .legend-title {
      font-weight: bold;
      margin-bottom: 10px;
      font-size: 14px;
      color: #333;
      text-align: center;
    }
    
    .legend-items {
      display: flex;
      flex-direction: column;
      gap: 8px;
    }
    
    .legend-item {
      display: flex;
      align-items: center;
      gap: 8px;
      font-size: 12px;
      
      .legend-color {
        width: 16px;
        height: 16px;
        border-radius: 3px;
        border: 1px solid #ccc;
      }
      
      .legend-label {
        color: #555;
      }
    }
  }
  
  // 数据表格样式 - 放在右下角
  .map-data-table {
    position: absolute;
    bottom: 20px;
    right: 20px;
    background: rgba(255, 255, 255, 0.95);
    padding: 15px;
    border-radius: 8px;
    box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
    z-index: 1000;
    min-width: 300px;
    
    .table-title {
      font-weight: bold;
      margin-bottom: 10px;
      font-size: 14px;
      color: #333;
      text-align: center;
    }
    
    .data-table {
      width: 100%;
      border-collapse: collapse;
      font-size: 12px;
      
      th,
      td {
        border: 1px solid #ddd;
        padding: 6px 8px;
        text-align: center;
      }
      
      th {
        background-color: #f5f5f5;
        font-weight: bold;
        color: #333;
      }
      
      td {
        color: #555;
      }
      
      tr:nth-child(even) {
        background-color: #f9f9f9;
      }
    }
  }
  
  // 比例尺样式
  .map-scale {
    position: absolute;
    bottom: 20px;
    left: 20px;
    
    .scale-bar {
      background: rgba(255, 255, 255, 0.9);
      padding: 8px 12px;
      border-radius: 4px;
      border: 1px solid #ccc;
      
      .scale-line {
        height: 2px;
        background: #333;
        margin-bottom: 4px;
        position: relative;
        width: 80px;
        
        &::before,
        &::after {
          content: '';
          position: absolute;
          width: 1px;
          height: 6px;
          background: #333;
          top: -2px;
        }
        
        &::before {
          left: 0;
        }
        
        &::after {
          right: 0;
        }
      }
      
      .scale-text {
        font-size: 11px;
        color: #333;
        text-align: center;
      }
    }
  }
  
  // 指北针样式
  .map-compass {
    position: absolute;
    top: 80px;
    right: 20px;
    background: rgba(255, 255, 255, 0.9);
    width: 40px;
    height: 40px;
    border-radius: 50%;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
    
    .compass-icon {
      font-size: 16px;
      color: #d32f2f;
    }
    
    .compass-text {
      font-size: 10px;
      font-weight: bold;
      color: #333;
    }
  }
  
  // 边框样式
  .map-border {
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    border: 3px solid #333;
    box-sizing: border-box;
  }
  
  // 配置面板
  .config-panel {
    position: absolute;
    top: 20px;
    left: 20px;
    z-index: 1001;
    width: 200px;
    
    .config-card {
      .config-items {
        display: flex;
        flex-direction: column;
        gap: 8px;
        
        .el-checkbox {
          margin: 0;
        }
      }
    }
  }
  
  // 导出面板
  .export-panel {
    position: absolute;
    top: 20px;
    right: 20px;
    z-index: 1001;
    
    .el-button {
      margin-left: 8px;
    }
  }
  
  // 全屏控制按钮
  .fullscreen-controls {
    position: absolute;
    top: 20px;
    right: 200px;
    z-index: 1002;
    
    .el-button {
      box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
    }
  }
}

// 弹窗样式
:deep(.region-popup) {
  h4 {
    margin: 0 0 8px 0;
    color: #333;
  }
  
  p {
    margin: 4px 0;
    font-size: 12px;
    color: #666;
  }
}
</style>