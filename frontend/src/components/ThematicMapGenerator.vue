<template>
  <div class="thematic-map-container" ref="mapContainer" :class="{ 'fullscreen': isFullscreen, 'compact': props.isCompact }">
    <!-- 顶部标题区域 -->
    <div class="map-header-section" v-show="mapConfigState.showTitle">
      <h1>{{ mapConfigState.title }}</h1>
    </div>

    <!-- 地图主体区域 -->
    <div class="map-body-wrapper">
      <!-- 地图容器 -->
      <div id="map" ref="mapRef" class="map-content"></div>

      <!-- 制图要素覆盖层 -->
      <div class="map-elements-overlay">
        <!-- 图例 - 移动到左下角 -->
        <div class="map-legend" v-show="mapConfigState.showLegend">
          <div class="legend-title">减灾能力</div>
          <div class="legend-items">
            <div v-for="item in legendItems" :key="item.value" class="legend-item">
              <span
                class="legend-color"
                :style="{
                  backgroundColor: item.color,
                  width: (isCommunityVillageLevel ? (item.size || 16) : 16) + 'px',
                  height: (isCommunityVillageLevel ? (item.size || 16) : 16) + 'px',
                  borderRadius: isCommunityVillageLevel ? '50%' : '0'
                }"
              ></span>
              <span class="legend-label">{{ item.label }}</span>
            </div>
          </div>
        </div>
        
        <!-- 数据表格 - 放在右下角 -->
        <div class="map-data-table" v-show="mapConfigState.showDataTable">
          <div class="table-title">{{ tableTitle }}</div>
          <table class="data-table">
            <thead>
              <tr>
                <th>减灾能力等级</th>
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
                <td>{{ props.orgName || '无区域' }}</td>
                <td>{{ getStatistics().strong }}</td>
                <td>{{ getStatistics().mediumStrong }}</td>
                <td>{{ getStatistics().medium }}</td>
                <td>{{ getStatistics().weak }}</td>
                <td>{{ getStatistics().veryWeak }}</td>
                <td>{{ getStatistics().total }}</td>
              </tr>
              <tr>
                <td>占比(%)</td>
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
        <div class="map-scale" v-show="mapConfigState.showScale">
          <div class="scale-bar" ref="scaleBar">
            <div class="scale-line"></div>
            <div class="scale-text">{{ scaleText }}</div>
            <div class="scale-note">注：本图境界不作实地划界依据</div>
          </div>
        </div>
        
        <!-- 指北针 -->
        <div class="map-compass" v-show="mapConfigState.showCompass">
          <div class="compass-icon">⬆</div>
          <div class="compass-text">N</div>
        </div>
        
        <!-- 边框 -->
        <div class="map-border" v-show="mapConfigState.showBorder"></div>
      </div>
      
      <!-- 配置面板 -->
      <div class="config-panel">
        <el-card class="config-card" shadow="hover">
          <template #header>
            <span>制图要素配置</span>
          </template>
          <div class="config-items">
            <el-checkbox v-model="mapConfigState.showTitle" @change="updateMapElements">显示标题</el-checkbox>
            <el-checkbox v-model="mapConfigState.showLegend" @change="updateMapElements">显示图例</el-checkbox>
            <el-checkbox v-model="mapConfigState.showDataTable" @change="updateMapElements">显示数据表</el-checkbox>
            <el-checkbox v-model="mapConfigState.showScale" @change="updateMapElements">显示比例尺</el-checkbox>
            <el-checkbox v-model="mapConfigState.showCompass" @change="updateMapElements">显示指北针</el-checkbox>
            <el-checkbox v-model="mapConfigState.showBorder" @change="updateMapElements">显示边框</el-checkbox>
          </div>
        </el-card>
      </div>
    </div>
    
    <!-- 底部监制信息区域 -->
    <div class="map-footer-section" v-show="mapConfigState.showFooter">
      <div class="footer-left">四川省减灾中心     监制</div>
      <div class="footer-right">{{ currentYearMonth }}</div>
    </div>
  </div>
</template>

<script setup lang="ts">
console.log('ThematicMapGenerator组件开始加载')
import { ref, onMounted, nextTick, withDefaults, defineProps, computed, watch, defineExpose } from 'vue'
import L from 'leaflet'
import 'leaflet/dist/leaflet.css'
import 'leaflet.chinatmsproviders'
import html2canvas from 'html2canvas'
import * as turf from '@turf/turf'
console.log('ThematicMapGenerator组件导入完成')
import { ElMessage, ElCard, ElCheckbox } from 'element-plus'
import { thematicMapApi, organizationBoundaryApi } from '@/api'

let villagePointsCache: any | null = null
let villagePointsCachePromise: Promise<any | null> | null = null

// 缓存大文件数据，避免重复下载
let regionHierarchyCache: any = null
let regionHierarchyLoading: Promise<any> | null = null

// 组件属性
interface Props {
  reportId?: number
  regionData?: any[]
  mapConfig?: Partial<{
    title: string
    subtitle: string
    showTitle: boolean
    showLegend: boolean
    showDataTable: boolean
    showScale: boolean
    showCompass: boolean
    showBorder: boolean
    showFooter: boolean // 新增：控制页脚显示
  }>
  year?: number
  orgCode?: string
  orgId?: number
  orgName?: string
  parentName?: string
  algorithmId?: number
  level?: string  // 数据级别: township(乡镇), community_village(社区-行政村), community_township(社区-乡镇), comprehensive(综合)
  isCompact?: boolean // 是否为紧凑模式（用于组合图）
}

const props = withDefaults(defineProps<Props>(), {
  reportId: 0,
  regionData: () => [],
  mapConfig: undefined,
  year: 2025,
  orgCode: '',
  orgName: '',
  parentName: '',
  algorithmId: 1,
  level: 'township',
  isCompact: false
})

// 响应式数据
const mapContainer = ref<HTMLElement>()
const mapRef = ref<HTMLElement>()
const scaleBar = ref<HTMLElement>()
const map = ref<L.Map | null>(null)
const isFullscreen = ref(false)
const thematicLayer = ref<L.LayerGroup | null>(null)
const villagePointLayer = ref<L.LayerGroup | null>(null)
const baseTileLayer = ref<any | null>(null)
const labelTileLayer = ref<any | null>(null)

// 边界文件索引缓存
const boundaryIndex = ref<{
  availableYears: number[]
  cities: string[]
  yearlyCities: Record<string, string[]>
} | null>(null)

const mapConfigState = ref({
  title: '四川省雅安市青神县乡镇减灾能力评估结果图',
  mainTitle: '减灾能力分级计算减灾能力评估报告',
  subtitle: `数据来源：减灾能力评估工具 | 制图时间：${new Date().getFullYear()}年${new Date().getMonth() + 1}月`,
  showTitle: true,
  showLegend: true,
  showDataTable: true,
  showScale: true,
  showCompass: true,
  showBorder: true,
  showFooter: true
})

if (props.mapConfig) {
  mapConfigState.value = { ...mapConfigState.value, ...props.mapConfig }
}

watch(
  () => props.mapConfig,
  (nextCfg) => {
    if (nextCfg) mapConfigState.value = { ...mapConfigState.value, ...nextCfg }
  },
  { deep: true }
)

const normalizeRegionName = (name: string) => {
  return String(name || '')
    .trim()
    .replace(/\s+/g, '')
    .replace(/街道办事处|街道|镇|乡|社区|行政村|村|省|市|县|区/g, '')
}

const escapeHtml = (value: unknown) => {
  return String(value ?? '')
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
    .replace(/'/g, '&#39;')
}

const getCountyCodeFromOrgCode = (orgCode: unknown) => {
  const raw = String(orgCode ?? '').trim()
  if (!raw) return ''
  return raw.length >= 6 ? raw.slice(0, 6) : raw
}

const normalizeVillageLabel = (value: unknown) => {
  let name = String(value ?? '').trim()
  if (!name) return ''

  name = name.replace(/社区居民委员会$/g, '社区').trim()
  name = name.replace(/社区居委会$/g, '社区').trim()

  name = name.replace(/村民委员会$/g, (m) => {
    const base = name.slice(0, -m.length).trim()
    if (!base) return ''
    return base.endsWith('村') ? base : base + '村'
  }).trim()

  name = name.replace(/村委会$/g, (m) => {
    const base = name.slice(0, -m.length).trim()
    if (!base) return ''
    return base.endsWith('村') ? base : base + '村'
  }).trim()

  if (name.includes('社区')) {
    name = name.replace(/居民委员会$/g, '').trim()
    name = name.replace(/居委会$/g, '').trim()
    name = name.replace(/委员会$/g, '').trim()
  } else {
    name = name.replace(/委员会$/g, '').trim()
  }

  const fullRepeat = name.match(/^(.+?)\1$/)
  if (fullRepeat && fullRepeat[1]) {
    name = fullRepeat[1]
  } else if (name.endsWith('村')) {
    const base = name.slice(0, -1)
    const baseRepeat = base.match(/^(.+?)\1$/)
    if (baseRepeat && baseRepeat[1]) {
      name = baseRepeat[1] + '村'
    }
  }

  return name
}

const extractVillageTail = (value: unknown) => {
  const raw = String(value ?? '').trim()
  if (!raw) return ''

  const tokens = ['街道办事处', '街道', '镇', '乡']
  let bestPos = -1
  let bestLen = 0
  for (const t of tokens) {
    const idx = raw.lastIndexOf(t)
    if (idx >= 0 && idx + t.length > bestPos + bestLen) {
      bestPos = idx
      bestLen = t.length
    }
  }

  if (bestPos < 0) return raw
  return raw.slice(bestPos + bestLen).trim()
}

const getVillageMatchKeys = (value: unknown) => {
  const raw = String(value ?? '').trim()
  if (!raw) return [] as string[]

  const candidates = [
    raw,
    extractVillageTail(raw),
    normalizeVillageLabel(raw),
    extractVillageTail(normalizeVillageLabel(raw))
  ]

  const keys = new Set<string>()
  for (const c of candidates) {
    const k = normalizeRegionName(c)
    if (k) keys.add(k)
  }
  return Array.from(keys)
}

const normalizeCapabilityLevelKey = (value: unknown) => {
  const raw = String(value ?? '').trim()
  if (!raw) return ''

  const normalized = raw.replace(/\s+/g, '')
  if (normalized.includes('较强')) return '较强'
  if (normalized.includes('中等')) return '中等'
  if (normalized.includes('较弱')) return '较弱'
  if (normalized.includes('弱')) return '弱'
  if (normalized.includes('强')) return '强'

  const english = normalized.toLowerCase()
  if (english === 'strong') return '强'
  if (english === 'mediumstrong') return '较强'
  if (english === 'medium') return '中等'
  if (english === 'weak') return '较弱'
  if (english === 'veryweak') return '弱'

  return normalized
}

const setLabelLayerVisible = (visible: boolean) => {
  if (!map.value || !labelTileLayer.value) return

  const has = map.value.hasLayer(labelTileLayer.value as any)
  if (visible && !has) labelTileLayer.value.addTo(map.value)
  if (!visible && has) map.value.removeLayer(labelTileLayer.value as any)
}

const pickVillageDataItem = (index: Map<string, any[]>, featureKey: string) => {
  if (!featureKey) return null

  const direct = index.get(featureKey)
  if (direct && direct.length > 0) return direct[0]

  let best: any | null = null
  let bestScore = Number.POSITIVE_INFINITY
  let bestLenDiff = Number.POSITIVE_INFINITY

  for (const [k, items] of index.entries()) {
    if (!items || items.length === 0) continue

    let score = Number.POSITIVE_INFINITY
    if (k === featureKey) score = 0
    else if (k.endsWith(featureKey)) score = 1
    else if (k.includes(featureKey)) score = 2
    else if (featureKey.includes(k)) score = 3
    else continue

    const lenDiff = Math.abs(k.length - featureKey.length)
    if (score < bestScore || (score === bestScore && lenDiff < bestLenDiff)) {
      best = items[0]
      bestScore = score
      bestLenDiff = lenDiff
    }
  }

  return best
}

const loadVillagePointsGeoJSON = async (): Promise<any | null> => {
  if (villagePointsCache) return villagePointsCache
  if (villagePointsCachePromise) return villagePointsCachePromise

  villagePointsCachePromise = (async () => {
    try {
      const response = await fetch('/village_points/village_points.geojson?t=' + Date.now())
      if (!response.ok) return null

      const geo = await response.json()
      if (!geo || geo.type !== 'FeatureCollection' || !Array.isArray(geo.features)) return null

      villagePointsCache = geo
      return geo
    } catch (e) {
      console.warn('加载行政村点位数据失败:', e)
      return null
    } finally {
      villagePointsCachePromise = null
    }
  })()

  return villagePointsCachePromise
}

const renderVillagePointOverlay = async (data: any) => {
  if (!map.value) return
  if (!thematicLayer.value) return

  if (villagePointLayer.value) {
    thematicLayer.value.removeLayer(villagePointLayer.value as any)
    villagePointLayer.value = null
  }

  const currentLevel = getCurrentLevel()
  if (currentLevel !== 'community_village' && currentLevel !== 'community_township') {
    setLabelLayerVisible(true)
    return
  }

  const countyCode = getCountyCodeFromOrgCode(props.orgCode)
  if (!countyCode) {
    setLabelLayerVisible(true)
    return
  }

  const geo = await loadVillagePointsGeoJSON()
  const allFeatures = Array.isArray(geo?.features) ? geo.features : []
  if (!allFeatures.length) {
    setLabelLayerVisible(true)
    return
  }

  const targetNames = new Set<string>()
  const dataList = Array.isArray(data?.data) ? data.data : []
  if (currentLevel === 'community_village') {
    dataList.forEach((item: any) => {
      const name = item?.regionName || item?.name || ''
      getVillageMatchKeys(name).forEach((k) => targetNames.add(k))
    })
  }

  let countyFeatures = allFeatures.filter((f: any) => {
    const code = String(f?.properties?.code ?? '')
    return code.startsWith(countyCode)
  })

  if (currentLevel === 'community_village' && targetNames.size > 0) {
    const matched = countyFeatures.filter((f: any) => {
      const name = f?.properties?.dzcun || f?.properties?.name || ''
      const key = normalizeRegionName(name)
      return key && targetNames.has(key)
    })
    if (matched.length > 0) countyFeatures = matched
  }

  if (!countyFeatures.length) {
    setLabelLayerVisible(true)
    return
  }

  setLabelLayerVisible(false)

  // 建立数据映射以便快速查找
  const villageDataIndex = new Map<string, any[]>()
  if (currentLevel === 'community_village') {
    dataList.forEach((item: any) => {
      const name = item?.regionName || item?.name || ''
      const keys = getVillageMatchKeys(name)
      keys.forEach((k) => {
        const list = villageDataIndex.get(k) || []
        list.push(item)
        villageDataIndex.set(k, list)
      })
    })
  }

  const group = L.layerGroup()

  countyFeatures.forEach((f: any) => {
    const coords = f?.geometry?.coordinates
    if (!Array.isArray(coords) || coords.length < 2) return
    const lng = Number(coords[0])
    const lat = Number(coords[1])
    if (!Number.isFinite(lng) || !Number.isFinite(lat)) return

    const rawName = f?.properties?.dzcun || f?.properties?.name || ''
    const key = normalizeRegionName(rawName)
    const label = escapeHtml(normalizeVillageLabel(rawName))
    
    let radius = 2
    let fillColor = '#666'

    if (currentLevel === 'community_village') {
      const dataItem = pickVillageDataItem(villageDataIndex, key)
      const level = normalizeCapabilityLevelKey(dataItem?.capabilityLevel || '')
      const legendItem = legendItems.value.find(item =>
        item.value === level || item.label === level
      ) || legendItems.value[2]

      fillColor = legendItem.color
      const size = Number(legendItem.size || 6)
      radius = Math.max(2, size / 2)
    }

    const circle = L.circleMarker([lat, lng], {
      radius,
      color: 'rgba(0,0,0,0.5)',
      weight: 1,
      opacity: 1,
      fillColor,
      fillOpacity: 1,
      interactive: false,
      pane: 'villagePointPane'
    })

    if ((currentLevel === 'community_village' || currentLevel === 'community_township') && label) {
      circle.bindTooltip(label, {
        permanent: true,
        direction: 'right',
        offset: L.point(radius + 4, 0),
        opacity: 1,
        className:
          currentLevel === 'community_village'
            ? 'village-point-tooltip'
            : 'village-point-tooltip village-point-tooltip--small',
        pane: 'villagePointPane'
      })
    }

    group.addLayer(circle)
  })

  villagePointLayer.value = group
  thematicLayer.value.addLayer(group)
}

// 递归查找区域层级信息的辅助函数
const findRegionInHierarchy = (nodes: any, targetName: string): any | null => {
  if (!nodes) return null

  const target = String(targetName || '').trim()
  const targetKey = normalizeRegionName(target)

  const getList = (n: any): any[] => (Array.isArray(n) ? n : Object.values(n || {}))

  const search = (n: any, predicate: (node: any) => boolean): any | null => {
    const list = getList(n)
    for (const node of list) {
      if (!node) continue
      if (predicate(node)) return node
      if (node.children && node.children.length > 0) {
        const found = search(node.children, predicate)
        if (found) return found
      }
    }
    return null
  }

  const exact = search(nodes, (node) => String(node?.name || '').trim() === target)
  if (exact) return exact

  if (targetKey) {
    const normalizedEqual = search(nodes, (node) => normalizeRegionName(node?.name || '') === targetKey)
    if (normalizedEqual) return normalizedEqual

    if (targetKey.length >= 3) {
      const prefix = search(
        nodes,
        (node) => {
          const nodeKey = normalizeRegionName(node?.name || '')
          if (!nodeKey) return false
          return nodeKey.startsWith(targetKey) || targetKey.startsWith(nodeKey)
        }
      )
      if (prefix) return prefix
    }
  }

  return null
}

const currentYearMonth = computed(() => {
  const date = new Date()
  return `${date.getFullYear()}年${date.getMonth() + 1}月`
})

const shouldFilterByOrgName = (orgName: string) => {
  const n = String(orgName || '').trim()
  if (!n) return false
  if (n.includes('省') || n.includes('市') || n.includes('县') || n.includes('区')) return false
  return true
}

const applyOrgFilter = (boundaries: any, thematicData: any[]) => {
  const orgName = props.orgName || ''
  if (!shouldFilterByOrgName(orgName)) {
    return { boundaries, data: thematicData }
  }

  const orgKey = normalizeRegionName(orgName)
  const originalFeatures = Array.isArray(boundaries?.features) ? boundaries.features : []

  const filteredFeatures = originalFeatures.filter((feature: any) => {
    const featureName = feature?.properties?.xiang || feature?.properties?.name || feature?.properties?.NAME || feature?.properties?.XIANG
    const featureKey = normalizeRegionName(featureName)
    if (!featureKey) return false
    return featureKey.includes(orgKey) || orgKey.includes(featureKey)
  })

  const filteredData = (thematicData || []).filter((item: any) => {
    const itemKey = normalizeRegionName(item?.regionName || '')
    if (!itemKey) return false
    return itemKey.includes(orgKey) || orgKey.includes(itemKey)
  })

  if (!filteredFeatures.length) {
    return { boundaries, data: [] }
  }

  return {
    boundaries: { ...boundaries, features: filteredFeatures },
    data: filteredData
  }
}

const legendItems = ref([
  { value: 'strong', label: '强', color: '#137909', size: 18 },
  { value: 'mediumStrong', label: '较强', color: '#46952f', size: 14 },
  { value: 'medium', label: '中等', color: '#79b517', size: 11 },
  { value: 'weak', label: '较弱', color: '#abd17c', size: 9 },
  { value: 'veryWeak', label: '弱', color: '#e2efa8', size: 7 }
])

// 根据级别动态生成表格标题
const tableTitle = computed(() => {
  const titles: Record<string, string> = {
    'township': '乡镇（街道）减灾能力统计表',
    'community_village': '社区（行政村）减灾能力_社区单元统计表',
    'community_township': '社区（行政村）减灾能力_乡镇单元统计表',
    'comprehensive': '综合减灾能力统计表'
  }
  return titles[getCurrentLevel()] || '减灾能力统计表'
})

// 存储当前专题数据用于统计
const currentThematicData = ref<any[]>([])

// 预定义乡镇数据（用于备用和补全）
const regionScores = {
  '青竹街道': { score: 85, disasterPrevention: 82, emergencyResponse: 88, recovery: 85 },
  '汉阳镇': { score: 72, disasterPrevention: 75, emergencyResponse: 68, recovery: 73 },
  '瑞峰镇': { score: 78, disasterPrevention: 80, emergencyResponse: 75, recovery: 79 },
  '西龙镇': { score: 75, disasterPrevention: 72, emergencyResponse: 78, recovery: 75 },
  '高台镇': { score: 68, disasterPrevention: 65, emergencyResponse: 70, recovery: 69 },
  '罗波乡': { score: 82, disasterPrevention: 80, emergencyResponse: 85, recovery: 81 },
  '白果乡': { score: 70, disasterPrevention: 70, emergencyResponse: 70, recovery: 70 }
}

// 用于覆盖props.level的内部变量（支持动态切换级别）
const overrideLevel = ref<string | null>(null)

// 获取当前使用的级别
const getCurrentLevel = () => overrideLevel.value || props.level

const isCommunityVillageLevel = computed(() => getCurrentLevel() === 'community_village')

const lastNoEvaluationDataTipKey = ref('')

const getSelectedRegionLabel = () => {
  const parts = [props.parentName, props.orgName]
    .map((v) => String(v || '').trim())
    .filter(Boolean)
  const label = parts.join('')
  return label || String(props.orgCode || '').trim() || '当前区域'
}

const getCurrentLevelLabel = () => {
  const level = String(getCurrentLevel() || '').trim()
  if (level === 'township') return '乡镇'
  if (level === 'community_village') return '社区'
  if (level === 'community_township') return '社区'
  if (level === 'comprehensive') return '综合'
  if (level === 'comprehensive_composite') return '综合'
  return '综合'
}

const showNoEvaluationDataTip = () => {
  const year = props.year || new Date().getFullYear()
  const regionLabel = getSelectedRegionLabel()
  const levelLabel = getCurrentLevelLabel()
  const key = `${year}-${regionLabel}-${levelLabel}`
  if (lastNoEvaluationDataTipKey.value === key) return
  lastNoEvaluationDataTipKey.value = key
  const message =
    levelLabel === '综合'
      ? `${year}年${regionLabel}没有找到对应的综合评估数据`
      : `${year}年${regionLabel}没有找到对应的${levelLabel}评估数据`
  ElMessage.warning(message)
}

const scaleText = ref('比例尺 1:--')

const calculateCapabilityLevel = (score: unknown): string => {
  const n = Number(score)
  if (!Number.isFinite(n)) return '中等'

  if (n >= 0 && n <= 1) {
    if (n >= 0.8) return '强'
    if (n >= 0.6) return '较强'
    if (n >= 0.4) return '中等'
    if (n >= 0.2) return '较弱'
    return '弱'
  }

  if (n >= 90) return '强'
  if (n >= 80) return '较强'
  if (n >= 70) return '中等'
  if (n >= 60) return '较弱'
  return '弱'
}

const normalizeCapabilityLevel = (rawLevel: unknown, score?: unknown): string => {
  const validLevels = ['强', '较强', '中等', '较弱', '弱']

  if (rawLevel === null || rawLevel === undefined) {
    return calculateCapabilityLevel(score)
  }

  const text = String(rawLevel).trim()
  if (!text) return calculateCapabilityLevel(score)

  const aliases: Record<string, string> = {
    极强: '强',
    极弱: '弱'
  }

  const normalizedText = aliases[text] ?? text
  if (validLevels.includes(normalizedText)) return normalizedText

  const englishMap: Record<string, string> = {
    strong: '强',
    mediumStrong: '较强',
    medium: '中等',
    weak: '较弱',
    veryWeak: '弱'
  }
  if (englishMap[normalizedText]) return englishMap[normalizedText]

  const n = Number(normalizedText)
  if (Number.isFinite(n)) return calculateCapabilityLevel(n)

  return calculateCapabilityLevel(score)
}

// 根据能力等级获取颜色
const getCapabilityColor = (level: string): string => {
  // 处理可能的null/undefined
  if (!level) return '#9e9e9e' // 默认灰色

  // 统一处理中文和英文等级
  switch (level) {
    case '强':
    case 'strong':
      return '#006400' // 深绿色
    case '较强':
    case 'mediumStrong':
      return '#32CD32' // 浅绿色
    case '中等':
    case 'medium':
      return '#9ACD32' // 黄绿色
    case '较弱':
    case 'weak':
      return '#FFFF99' // 浅黄色
    case '弱':
    case 'veryWeak':
      return '#F5F5DC' // 米色
    default:
      console.warn(`未知的能力等级: ${level}，使用默认颜色`)
      return '#9e9e9e' // 灰色
  }
}

// 获取能力等级的文本描述
const getCapabilityText = (level: string): string => {
  if (!level) return '未知'

  // 如果已经是中文，直接返回
  const chineseLevels = ['强', '较强', '中等', '较弱', '弱']
  if (chineseLevels.includes(level)) {
    return level
  }

  // 处理英文等级
  switch (level) {
    case 'strong': return '强'
    case 'mediumStrong': return '较强'
    case 'medium': return '中等'
    case 'weak': return '较弱'
    case 'veryWeak': return '弱'
    default: return level || '未知'
  }
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
    center: [30.65, 104.06], // 默认四川省中心
    zoom: 7,
    attributionControl: false,
    zoomControl: false, // 隐藏默认缩放控件
    preferCanvas: true // 使用Canvas渲染，提高性能并改善html2canvas截图效果
  })
  
  console.log('地图实例创建成功:', map.value)

  const pane = map.value.createPane('villagePointPane')
  pane.style.zIndex = '650'
  pane.style.pointerEvents = 'none'
  
  // 添加天地图底图
  baseTileLayer.value = (L.tileLayer as any).chinaProvider('TianDiTu.Normal.Map', {
    key: '0252639b1589bd33a54817f48d982093',
    attribution: '',
    keepBuffer: 0
  })
  baseTileLayer.value.addTo(map.value)
  
  // 添加天地图标注
  labelTileLayer.value = (L.tileLayer as any).chinaProvider('TianDiTu.Normal.Annotion', {
    key: '0252639b1589bd33a54817f48d982093',
    attribution: '',
    keepBuffer: 0
  })
  labelTileLayer.value.addTo(map.value)
  
  // 监听地图缩放事件，更新比例尺
  map.value.on('zoomend', updateScale)
  map.value.on('moveend', updateScale)
  updateScale()
}

// 从window.evaluationData读取数据
const loadDataFromSession = async () => {
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
          const rawCapabilityLevel =
            row.capabilityLevel ||
            row.comprehensiveCapabilityLevel ||
            row.comprehensiveCapabilityGrade ||
            row.综合能力分级 ||
            row.totalGrade ||
            row.overallGrade ||
            row.overall_grade ||
            row.comprehensiveGrade ||
            row.comprehensive_grade ||
            row.comprehensiveCapability ||
            row.management_grade
          const capabilityLevel = normalizeCapabilityLevel(rawCapabilityLevel, totalScore)
          
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
      
      // 更新地图标题
      if (evaluationData.stepInfo?.stepName) {
        mapConfigState.value.title = `${evaluationData.stepInfo.stepName}减灾能力评估结果图`
      }
      
      const thematicData = processedData.regions.map((region: any) => ({
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
        await renderThematicLayer(processedData)
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

  // 优先加载真实的GeoJSON边界数据（只加载一次）
  let boundaries = null
  let boundaryLoadError = null

  try {
    console.log('步骤1: 加载真实边界数据')
    boundaries = await loadRealBoundaryData()
    if (boundaries && boundaries.features && boundaries.features.length > 0) {
      console.log('边界数据加载完成，类型:', boundaries.type, '特征数量:', boundaries.features?.length)
    } else {
      console.log('边界数据为空，将使用默认边界')
      boundaries = null
    }
  } catch (error) {
    console.error('加载真实边界数据失败:', error)
    boundaryLoadError = error
  }

  // 如果边界数据加载失败，使用备用方案
  if (!boundaries) {
    if (await loadDataFromSession()) {
      console.log('使用备用方案：从sessionStorage加载数据成功')
      return
    }
    // 如果sessionStorage也没有，使用空边界
    boundaries = emptyFeatureCollection()
  }

  // 检查是否有从评估计算传递的数据
  const evaluationData = (window as any).evaluationData
  console.log('从window.evaluationData读取数据:', evaluationData)

  const hasOrgFilter = Boolean(props.orgId || (props.orgCode && props.orgCode.trim()) || (props.orgName && props.orgName.trim()))
  if (!hasOrgFilter && !(evaluationData && evaluationData.tableData)) {
    await renderThematicLayer({ boundaries: emptyFeatureCollection(), data: [] })
    currentThematicData.value = []
    return
  }

  let thematicData: any[] = []

  try {
    if (!props.orgCode || !props.orgCode.trim()) {
      throw new Error('未选择区划，跳过API请求')
    }

    try {
      const apiResponse = await thematicMapApi.getThematicData({
        reportId: props.reportId,
        surveyId: 1,
        algorithmId: props.algorithmId || 1,
        year: props.year,
        orgCode: props.orgCode,
        level: getCurrentLevel()
      })
      const apiData = (apiResponse as any)?.data
      if (Array.isArray(apiData) && apiData.length > 0) {
        thematicData = apiData
        console.log('从API加载专题数据成功:', apiResponse)
      } else {
        const hasRuntimeEvaluationData =
          evaluationData && Array.isArray(evaluationData.tableData) && evaluationData.tableData.length > 0
        if (!hasRuntimeEvaluationData) {
          showNoEvaluationDataTip()
        }
        thematicData = []
      }
    } catch (apiError) {
      console.log('从API加载专题数据失败，尝试使用评估数据:', apiError)

      if (evaluationData && Array.isArray(evaluationData.tableData)) {
        console.log('找到评估数据，基于真实边界生成专题数据...')

        console.log('评估数据条数:', evaluationData.tableData.length, '边界特征数量:', boundaries.features.length)

        if (evaluationData.tableData.length > boundaries.features.length) {
          console.log('评估数据多于边界数据，为每条评估数据生成专题数据')
          thematicData = evaluationData.tableData.map((evaluationRow: any, index: number) => {
            const regionName = evaluationRow.region || evaluationRow.地区名称 || evaluationRow.name || `区域${index + 1}`
            const totalScore = parseFloat(evaluationRow.totalScore || evaluationRow.总分 || evaluationRow.综合得分 || evaluationRow.score || 0)

            const rawCapabilityLevel =
              evaluationRow.capabilityLevel ||
              evaluationRow.comprehensiveCapabilityLevel ||
              evaluationRow.comprehensiveCapabilityGrade ||
              evaluationRow.综合能力分级 ||
              evaluationRow.totalGrade ||
              evaluationRow.overallGrade ||
              evaluationRow.overall_grade ||
              evaluationRow.comprehensiveGrade ||
              evaluationRow.comprehensive_grade ||
              evaluationRow.comprehensiveCapability ||
              evaluationRow.management_grade

            const capabilityLevel = normalizeCapabilityLevel(rawCapabilityLevel, totalScore)

            const matchingFeature =
              boundaries.features.find((feature: any) => {
                const featureName = feature.properties?.xiang || feature.properties?.name || feature.properties?.NAME || feature.properties?.XIANG
                if (!featureName || !regionName) return false

                return (
                  featureName === regionName ||
                  featureName.includes(regionName) ||
                  regionName.includes(featureName) ||
                  featureName.replace(/[镇街道]/g, '') === regionName.replace(/[镇街道]/g, '') ||
                  regionName.replace(/[镇街道]/g, '') === featureName.replace(/[镇街道]/g, '')
                )
              })

            if (!matchingFeature) {
              console.warn(`评估数据 "${regionName}" 未匹配到任何边界特征`)
              return null
            }

            console.log(`评估数据 "${regionName}" 匹配到边界特征:`, matchingFeature?.properties?.xiang || matchingFeature?.properties?.name)

            return {
              regionId: index + 1,
              regionName: regionName,
              county: matchingFeature.properties?.COUNTY || '青神县',
              score: totalScore,
              totalScore: totalScore,
              capabilityLevel: capabilityLevel,
              geometry: matchingFeature.geometry,
              properties: matchingFeature.properties,
              details: {
                disasterPreventionCapability: parseFloat(evaluationRow.disasterPreventionCapability || evaluationRow.灾害预防能力 || evaluationRow.防灾能力 || 0),
                emergencyResponseCapability: parseFloat(evaluationRow.emergencyResponseCapability || evaluationRow.应急响应能力 || evaluationRow.应急能力 || 0),
                recoveryReconstructionCapability: parseFloat(evaluationRow.recoveryReconstructionCapability || evaluationRow.恢复重建能力 || evaluationRow.重建能力 || 0)
              }
            }
          }).filter((item: any) => item !== null)
        } else {
          if (boundaries && boundaries.features && Array.isArray(boundaries.features)) {
            thematicData = boundaries.features
              .filter((feature: any) => feature && feature.properties)
              .map((feature: any, index: number) => {
                const featureRegionName = feature.properties?.xiang || feature.properties?.name || feature.properties?.NAME || feature.properties?.XIANG || `区域${index + 1}`

                // 标准化名称的辅助函数
                const normalizeName = (name: string): string => {
                  if (!name) return ''
                  return name
                    .replace(/(镇|街道|乡|社区|村|居委会|村委会|社区居委会|村民委员会)$/g, '')
                    .replace(/\s+/g, '')
                    .trim()
                }

                const evaluationRow =
                  evaluationData.tableData.find((row: any) => {
                    const rowRegionName = row.regionName || row.地区名称 || row.name
                    if (!rowRegionName || !featureRegionName) return false

                    // 1. 精确匹配
                    if (rowRegionName === featureRegionName) return true

                    // 2. 包含匹配（双向）
                    if (rowRegionName.includes(featureRegionName) || featureRegionName.includes(rowRegionName)) return true

                    // 3. 标准化后匹配
                    const normalizedFeature = normalizeName(featureRegionName)
                    const normalizedRow = normalizeName(rowRegionName)
                    if (normalizedFeature && normalizedRow && normalizedFeature === normalizedRow) return true

                    // 4. 标准化后的包含匹配
                    if (normalizedFeature && normalizedRow &&
                        (normalizedFeature.includes(normalizedRow) || normalizedRow.includes(normalizedFeature))) {
                      return true
                    }

                    return false
                  })

                if (!evaluationRow) {
                  // 尝试使用regionScores作为备用
                  const regionData = regionScores[featureRegionName as keyof typeof regionScores]
                  if (regionData) {
                    return {
                      regionId: index + 1,
                      regionName: featureRegionName,
                      county: feature.properties?.COUNTY || '青神县',
                      score: regionData.score,
                      totalScore: regionData.score,
                      capabilityLevel: regionData.score >= 90 ? '强' : regionData.score >= 80 ? '较强' : regionData.score >= 70 ? '中等' : '较弱',
                      geometry: feature.geometry,
                      properties: feature.properties,
                      details: {
                        disasterPreventionCapability: regionData.disasterPrevention,
                        emergencyResponseCapability: regionData.emergencyResponse,
                        recoveryReconstructionCapability: regionData.recovery
                      }
                    }
                  }
                  return null
                }

                console.log(`边界特征 "${featureRegionName}" 匹配到评估数据:`, evaluationRow?.regionName || evaluationRow?.地区名称)

                const totalScore = parseFloat(evaluationRow?.totalScore || evaluationRow?.总分 || evaluationRow?.综合得分 || evaluationRow?.score || 0)

                const rawCapabilityLevel =
                  evaluationRow?.capabilityLevel ||
                  evaluationRow?.comprehensiveCapabilityLevel ||
                  evaluationRow?.comprehensiveCapabilityGrade ||
                  evaluationRow?.综合能力分级 ||
                  evaluationRow?.totalGrade ||
                  evaluationRow?.overallGrade ||
                  evaluationRow?.overall_grade ||
                  evaluationRow?.comprehensiveGrade ||
                  evaluationRow?.comprehensive_grade ||
                  evaluationRow?.management_grade

                const capabilityLevel = normalizeCapabilityLevel(rawCapabilityLevel, totalScore)

                return {
                  regionId: index + 1,
                  regionName: featureRegionName,
                  county: feature.properties?.COUNTY || '青神县',
                  score: totalScore,
                  totalScore: totalScore,
                  capabilityLevel: capabilityLevel,
                  geometry: feature.geometry,
                  properties: feature.properties,
                  details: {
                    disasterPreventionCapability: parseFloat(
                      evaluationRow?.disasterManagement || evaluationRow?.灾害管理能力值 || evaluationRow?.disasterPreventionCapability || evaluationRow?.灾害预防能力 || evaluationRow?.防灾能力 || 0
                    ),
                    emergencyResponseCapability: parseFloat(
                      evaluationRow?.disasterPreparedness || evaluationRow?.灾害备灾能力值 || evaluationRow?.emergencyResponseCapability || evaluationRow?.应急响应能力 || evaluationRow?.应急能力 || 0
                    ),
                    recoveryReconstructionCapability: parseFloat(
                      evaluationRow?.selfRescueTransfer || evaluationRow?.自救转移能力值 || evaluationRow?.recoveryReconstructionCapability || evaluationRow?.恢复重建能力 || evaluationRow?.重建能力 || 0
                    )
                  }
                }
              }).filter((item: any) => item !== null)
          } else {
            console.warn('边界数据无效，使用空数组')
            thematicData = []
          }
        }

        if (evaluationData.stepInfo?.stepName) {
          mapConfigState.value.title = `${evaluationData.stepInfo.stepName}减灾能力评估结果图`
        }
      } else {
        ElMessage.error('获取评估数据失败')
        thematicData = []
      }
    }

    const processedData = applyOrgFilter(boundaries, thematicData)

    console.log('准备渲染真实边界数据:', processedData)

    currentThematicData.value = processedData.data
    console.log('存储专题数据用于统计:', currentThematicData.value.length, '条记录')

    await renderThematicLayer(processedData)
    return
  } catch (error) {
    console.error('加载专题数据失败:', error)
    ElMessage.error('加载专题数据失败')
  }
}



// 基于真实边界数据生成专题数据
const generateThematicDataFromBoundaries = async (boundaries: any) => {
  if (!boundaries || !boundaries.features || !Array.isArray(boundaries.features)) {
    console.warn('边界数据无效，返回空数组')
    return []
  }

  // 使用currentThematicData中的实际数据
  const existingDataMap = new Map<string, any>()
  if (currentThematicData.value && Array.isArray(currentThematicData.value)) {
    currentThematicData.value.forEach((item: any) => {
      const regionName = item.regionName || item.name
      if (regionName) {
        existingDataMap.set(regionName, item)
      }
    })


  }

  console.log('generateThematicDataFromBoundaries - 现有数据:', existingDataMap.size, '条')

  // 标准化名称的辅助函数
  const normalizeName = (name: string): string => {
    if (!name) return ''
    return name
      .replace(/(镇|街道|乡|社区|村|居委会|村委会|社区居委会|村民委员会)$/g, '')
      .replace(/\s+/g, '')
      .trim()
  }

  console.log('generateThematicDataFromBoundaries - 边界特征数量:', boundaries.features?.length)
  console.log('generateThematicDataFromBoundaries - 现有数据区域:', Array.from(existingDataMap.keys()))

  return boundaries.features
    .filter((feature: any) => feature && feature.properties && feature.geometry) // 过滤无效特征
    .map((feature: any, index: number) => {
    const featureRegionName = feature.properties?.xiang || feature.properties?.name || feature.properties?.NAME || feature.properties?.XIANG || `区域${index + 1}`

    // 使用标准化匹配逻辑查找数据
    let matchedData: any = null
    for (const [dataRegionName, dataItem] of existingDataMap.entries()) {
      // 1. 精确匹配
      if (featureRegionName === dataRegionName) {
        matchedData = dataItem
        break
      }

      // 2. 包含匹配（双向）
      if (featureRegionName.includes(dataRegionName) || dataRegionName.includes(featureRegionName)) {
        matchedData = dataItem
        break
      }

      // 3. 标准化后匹配
      const normalizedFeature = normalizeName(featureRegionName)
      const normalizedData = normalizeName(dataRegionName)
      if (normalizedFeature && normalizedData && normalizedFeature === normalizedData) {
        matchedData = dataItem
        break
      }

      // 4. 标准化后的包含匹配
      if (normalizedFeature && normalizedData &&
          (normalizedFeature.includes(normalizedData) || normalizedData.includes(normalizedFeature))) {
        matchedData = dataItem
        break
      }
    }

    console.log(`边界 "${featureRegionName}" 匹配结果:`, matchedData ? matchedData.regionName : '未匹配')

    // 如果没有匹配到数据，也继续处理，尝试使用regionScores或默认值
    
    // 确保matchedData不为空，方便后续访问属性
    const sourceData = matchedData || {}
    const sourceDetails = sourceData.details || {}
    const defaultData = { score: 70, disasterPrevention: 70, emergencyResponse: 70, recovery: 70 }

    const regionData = regionScores[featureRegionName as keyof typeof regionScores] || {
      score: sourceData.score || sourceData.totalScore || defaultData.score,
      disasterPrevention: sourceDetails.disasterPreventionCapability || defaultData.disasterPrevention,
      emergencyResponse: sourceDetails.emergencyResponseCapability || defaultData.emergencyResponse,
      recovery: sourceDetails.recoveryReconstructionCapability || defaultData.recovery
    }

    return {
      regionId: index + 1,
      regionName: featureRegionName,
      county: feature.properties?.COUNTY || '青神县',
      score: regionData.score,
      capabilityLevel: sourceData.capabilityLevel || (regionData.score >= 90 ? '强' : regionData.score >= 80 ? '较强' : regionData.score >= 70 ? '中等' : '较弱'),
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
  }).filter((item: any) => item !== null) // 过滤掉null值
}

// 更新地图要素显示
const updateMapElements = () => {
  // 强制重新渲染
  nextTick(() => {
    console.log('更新地图要素:', mapConfigState.value)
  })
}

// 渲染专题图层
const renderThematicLayer = async (data: any) => {
  if (!map.value || !data) return
  
  console.log('开始渲染专题图层:', data)
  
  // 清除现有图层
  if (thematicLayer.value) {
    map.value.removeLayer(thematicLayer.value as any)
  }
  
  // 创建新的图层组
  thematicLayer.value = L.layerGroup().addTo(map.value as any)

  const evaluationData = (window as any).evaluationData
  const resolvedOrgName = (props.orgName || (evaluationData && evaluationData.countyName) || '').trim()
  const hasOrgContext = Boolean(props.orgId || (props.orgCode && props.orgCode.trim()) || resolvedOrgName)
  
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
        // ... (省略匹配逻辑，与之前一致)
        thematicInfo = data.data.find((item: any) => {
          if (!item) return false

          // 获取专题数据中的区域名称
          const itemRegionName = item.regionName || item.name || item.county

          if (!itemRegionName || !featureRegionName) return false

          // 1. 精确匹配
          if (itemRegionName === featureRegionName) return true

          // 2. 包含匹配（双向）
          if (itemRegionName.includes(featureRegionName) || featureRegionName.includes(itemRegionName)) return true

          // 3. 标准化后匹配（去除行政区划后缀）
          const normalizeName = (name: string): string => {
            if (!name) return ''
            return name
              .replace(/(镇|街道|乡|社区|村|居委会|村委会|社区居委会|村民委员会)$/g, '')
              .replace(/\s+/g, '')
              .trim()
          }

          const normalizedFeature = normalizeName(featureRegionName)
          const normalizedItem = normalizeName(itemRegionName)
          if (normalizedFeature && normalizedItem && normalizedFeature === normalizedItem) return true

          // 4. 标准化后的包含匹配
          if (normalizedFeature && normalizedItem &&
              (normalizedFeature.includes(normalizedItem) || normalizedItem.includes(normalizedFeature))) {
            return true
          }

          return false
        })
      }

      // 如果没有匹配数据，给予默认值
      if (!thematicInfo) {
        console.log(`区域没有匹配数据: ${featureRegionName}, 使用默认样式`)
      }
      
      // 使用score字段，确保数据正确
      const scoreValue = thematicInfo ? parseFloat(thematicInfo.score || thematicInfo.totalScore || thematicInfo.value || 0) : 0
      // 直接使用专题数据中的capabilityLevel，不再根据分数计算
      const capabilityLevel = thematicInfo?.capabilityLevel || '无数据'
      // 如果没有数据，使用灰色
      const color = thematicInfo ? getCapabilityColor(capabilityLevel) : '#cccccc'
      
      console.log(`最终渲染数据 - 区域: ${featureRegionName}, 分数: ${scoreValue}, 等级: ${capabilityLevel}, 颜色: ${color}`)
      
      const layer = L.geoJSON(feature, {
        style: {
          fillColor: color,
          weight: 1,
          opacity: 0.8,
          color: '#666', // 使用细灰线绘制，参考乡镇样式
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
          ${thematicInfo?.details ? `
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
      
      // 添加文字标注
      const bounds = layer.getBounds()
      const center = bounds.getCenter()
      
      const labelIcon = L.divIcon({
        className: 'map-region-label',
        html: `<div style="text-align: center; font-size: 12px; font-weight: bold; color: #fff; text-shadow: 1px 1px 2px #000; white-space: nowrap; pointer-events: none;">${featureRegionName || ''}</div>`,
        iconSize: [100, 20],
        iconAnchor: [50, 10]
      })
      
      const labelMarker = L.marker(center, {
        icon: labelIcon,
        interactive: false, // 标注不参与交互，避免遮挡下方的区域点击
        zIndexOffset: 1000 // 确保标注在顶层
      })
      
      if (thematicLayer.value) {
        thematicLayer.value.addLayer(layer)
        thematicLayer.value.addLayer(labelMarker)
      }
    })

    if (hasOrgContext) {
      // 绘制县域边界和中心文字
      try {
          // 尝试优先加载静态县界文件，如果失败则使用动态计算
          let staticBoundaryLoaded = false
          // 只有当目标区域是青神县时，才加载静态的青神县边界文件
          const currentCounty = props.orgName || '青神县'
          
          // 禁用静态加载逻辑，强制走通用逻辑
          if (false && currentCounty === '青神县') {
            try {
               const response = await fetch('/county_boundary.json')
               if (response.ok) {
                const boundaryJson = await response.json()
                if (boundaryJson) {
                    // 静态文件加载成功，直接使用
                    console.log('成功加载静态县界数据')
                    staticBoundaryLoaded = true
                    
                    const layerGroup = thematicLayer.value
                    if (layerGroup) {
                        const finalPoly = boundaryJson
                        
                        // 绘制外部红色描边
                        const redBorder = L.geoJSON(finalPoly as any, {
                          style: {
                            color: 'red',
                            weight: 8,
                            opacity: 1,
                            fill: false,
                            lineCap: 'round',
                            lineJoin: 'round'
                          },
                          interactive: false
                        })
                        layerGroup?.addLayer(redBorder)
                        
                        // 绘制内部黑色实线
                        const blackBorder = L.geoJSON(finalPoly as any, {
                          style: {
                            color: 'black',
                            weight: 3,
                            opacity: 1,
                            fill: false,
                            lineCap: 'round',
                            lineJoin: 'round'
                          },
                          interactive: false
                        })
                        layerGroup?.addLayer(blackBorder)
                        
                        // 添加中心文字
                        const center = turf.centerOfMass(finalPoly as any)
                        if (center && center.geometry && center.geometry.coordinates) {
                            const latlng = [center.geometry.coordinates[1], center.geometry.coordinates[0]]
                            const countyName = props.orgName || '青神县'
                            
                            const labelMarker = L.marker(latlng as L.LatLngExpression, {
                              icon: L.divIcon({
                                html: `<div style="
                                    color: red; 
                                    font-weight: bold; 
                                    font-size: 24px; 
                                    text-shadow: 2px 2px 0 #fff, -1px -1px 0 #fff, 2px -1px 0 #fff, -1px 2px 0 #fff; 
                                    white-space: nowrap; 
                                    text-align: center;
                                    transform: translate(-50%, -50%);
                                ">${countyName}</div>`,
                                className: 'county-label-icon',
                                iconSize: [0, 0],
                                iconAnchor: [0, 0]
                              }),
                              interactive: false,
                              zIndexOffset: 2000
                            })
                            layerGroup?.addLayer(labelMarker)
                        }
                    }
                }
             }
          } catch (err) {
             console.warn('加载静态县界数据失败，回退到动态计算:', err)
          }
        } else {
          console.log(`当前区域为 ${currentCounty}，跳过加载青神县静态边界`)
        }

        if (!staticBoundaryLoaded) {
          const currentOrg = (props.orgName || '青神县').trim()
          let outlineRendered = false

          try {
            // 使用缓存避免重复加载大文件
            let hierarchy = regionHierarchyCache
            if (!hierarchy) {
              if (regionHierarchyLoading) {
                console.log('等待region_hierarchy加载完成...')
                hierarchy = await regionHierarchyLoading
              } else {
                console.log('首次加载region_hierarchy.json...')
                regionHierarchyLoading = fetch('/region_hierarchy.json')
                  .then(res => res.json())
                  .then(data => {
                    regionHierarchyCache = data
                    return data
                  })
                hierarchy = await regionHierarchyLoading
                regionHierarchyLoading = null
              }
            } else {
              console.log('使用缓存的region_hierarchy数据')
            }

            const regionInfo = findRegionInHierarchy(hierarchy, currentOrg)

              if (
                regionInfo &&
                (regionInfo.level === 'city' || regionInfo.level === 'province') &&
                regionInfo.geometry &&
                thematicLayer.value
              ) {
                outlineRendered = true

                const geo = {
                  type: 'Feature',
                  properties: {},
                  geometry: regionInfo.geometry
                }

                const redBorder = L.geoJSON(geo as any, {
                  style: {
                    color: 'red',
                    weight: 8,
                    opacity: 1,
                    fill: false,
                    lineCap: 'round',
                    lineJoin: 'round'
                  },
                  interactive: false
                })
                thematicLayer.value.addLayer(redBorder)

                const blackBorder = L.geoJSON(geo as any, {
                  style: {
                    color: 'black',
                    weight: 3,
                    opacity: 1,
                    fill: false,
                    lineCap: 'round',
                    lineJoin: 'round'
                  },
                  interactive: false
                })
                thematicLayer.value.addLayer(blackBorder)

                const name = currentOrg
                let labelLatLng: [number, number] | null = null

                if (regionInfo.center && Array.isArray(regionInfo.center) && regionInfo.center.length === 2) {
                  labelLatLng = [regionInfo.center[1], regionInfo.center[0]]
                } else {
                  try {
                    const center = turf.centerOfMass(geo as any)
                    if (center?.geometry?.coordinates?.length === 2) {
                      labelLatLng = [center.geometry.coordinates[1], center.geometry.coordinates[0]]
                    }
                  } catch (_e) {}
                }

                if (labelLatLng) {
                  const labelMarker = L.marker(labelLatLng as L.LatLngExpression, {
                    icon: L.divIcon({
                      html: `<div style="
                          color: red; 
                          font-weight: bold; 
                          font-size: 24px; 
                          text-shadow: 2px 2px 0 #fff, -1px -1px 0 #fff, 2px -1px 0 #fff, -1px 2px 0 #fff; 
                          white-space: nowrap; 
                          text-align: center;
                          transform: translate(-50%, -50%);
                      ">${name}</div>`,
                      className: 'county-label-icon',
                      iconSize: [0, 0],
                      iconAnchor: [0, 0]
                    }),
                    interactive: false,
                    zIndexOffset: 2000
                  })
                  thematicLayer.value.addLayer(labelMarker)
                }
              }
          } catch (_e) {}

          if (!outlineRendered) {
            const features = data.boundaries.features.filter((f: any) => f && f.geometry)
            if (features.length > 0) {
              let unionPoly = null
              try {
                unionPoly = (turf as any).union(turf.featureCollection(features as any))
              } catch (e) {
                console.warn('Turf union v7 error, trying iterative fallback:', e)
                if (features.length > 0) {
                  unionPoly = features[0]
                  for (let i = 1; i < features.length; i++) {
                    try {
                      const res: any = (turf as any).union(turf.featureCollection([unionPoly, features[i]] as any))
                      if (res) unionPoly = res
                    } catch (err) {
                      console.warn('Iterative union failed:', err)
                    }
                  }
                }
              }

              if (unionPoly && thematicLayer.value) {
                const finalPoly = unionPoly.type === 'FeatureCollection' ? unionPoly.features[0] : unionPoly

                const redBorder = L.geoJSON(finalPoly, {
                  style: {
                    color: 'red',
                    weight: 8,
                    opacity: 1,
                    fill: false,
                    lineCap: 'round',
                    lineJoin: 'round'
                  },
                  interactive: false
                })
                thematicLayer.value.addLayer(redBorder)

                const blackBorder = L.geoJSON(finalPoly, {
                  style: {
                    color: 'black',
                    weight: 3,
                    opacity: 1,
                    fill: false,
                    lineCap: 'round',
                    lineJoin: 'round'
                  },
                  interactive: false
                })
                thematicLayer.value.addLayer(blackBorder)

                try {
                  const center = turf.centerOfMass(finalPoly)
                  if (center && center.geometry && center.geometry.coordinates) {
                    const latlng = [center.geometry.coordinates[1], center.geometry.coordinates[0]]
                    const countyName = props.orgName || '青神县'

                    const labelMarker = L.marker(latlng as L.LatLngExpression, {
                      icon: L.divIcon({
                        html: `<div style="
                            color: red; 
                            font-weight: bold; 
                            font-size: 24px; 
                            text-shadow: 2px 2px 0 #fff, -1px -1px 0 #fff, 2px -1px 0 #fff, -1px 2px 0 #fff; 
                            white-space: nowrap; 
                            text-align: center;
                            transform: translate(-50%, -50%);
                        ">${countyName}</div>`,
                        className: 'county-label-icon',
                        iconSize: [0, 0],
                        iconAnchor: [0, 0]
                      }),
                      interactive: false,
                      zIndexOffset: 2000
                    })
                    thematicLayer.value.addLayer(labelMarker)
                  }
                } catch (centerErr) {
                  console.warn('Calculate center error:', centerErr)
                }
              }
            }
          }
        }
      } catch (e) {
        console.error('绘制县界失败:', e)
      }

      await renderVillagePointOverlay(data)
    }

    // 统计渲染结果
    const renderedCount = thematicLayer.value?.getLayers()?.length || 0
    const dataCount = data.data?.length || 0
    console.log(`=== 渲染完成 === 边界特征: ${data.boundaries.features?.length}, 数据条数: ${dataCount}, 实际渲染: ${renderedCount}, 跳过: ${data.boundaries.features?.length - renderedCount}`)

    if (hasOrgContext) {
      // 调整地图视图到数据范围
      try {
        // 优先尝试使用 region_hierarchy.json 中的 bbox（使用缓存）
        const currentOrg = (props.orgName || '青神县').trim()

        let hierarchy = regionHierarchyCache
        if (!hierarchy && regionHierarchyLoading) {
          hierarchy = await regionHierarchyLoading
        }

        if (hierarchy) {
          const regionInfo = findRegionInHierarchy(hierarchy, currentOrg)

          if (regionInfo && regionInfo.bbox) {
            // region_hierarchy.json 中的 bbox 格式为 [minX, minY, maxX, maxY]
            // Leaflet fitBounds 需要 [[minY, minX], [maxY, maxX]]
            const bbox = regionInfo.bbox
            const bounds = [
              [bbox[1], bbox[0]], // SouthWest: lat, lng
              [bbox[3], bbox[2]]  // NorthEast: lat, lng
            ]
            map.value.fitBounds(bounds as L.LatLngBoundsExpression, { padding: [20, 20] })
            console.log(`使用缓存的 region_hierarchy.json 调整视图到 ${currentOrg}`, bounds)
            return // 如果成功使用了 hierarchy，直接返回
          }
        }

        // 如果没有 hierarchy 数据，回退到计算 GeoJSON bounds
        const bounds = L.geoJSON(data.boundaries).getBounds()
        if (bounds.isValid()) {
          map.value.fitBounds(bounds, { padding: [20, 20] })
          console.log('根据 GeoJSON 调整地图视图到数据范围')
        } else {
          console.warn('GeoJSON bounds无效，跳过调整视图')
        }
      } catch (error) {
        console.error('调整地图视图失败:', error)
        
        // 最后尝试：如果都失败了，且是 GeoJSON 数据，尝试计算 bounds
        try {
           if (data.boundaries && data.boundaries.features && data.boundaries.features.length > 0) {
              const bounds = L.geoJSON(data.boundaries).getBounds()
              if (bounds.isValid()) {
                map.value.fitBounds(bounds, { padding: [20, 20] })
              }
           }
        } catch (e) {
           console.warn('最终视图调整尝试失败', e)
        }
      }
    }
  } else if (data.regions) {
    // 兼容原有的regions数据格式
    // ... (保持原有的data.regions渲染逻辑不变)
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
        dashArray: '0'
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
      
      // 添加文字标注
      const bounds = polygon.getBounds()
      const center = bounds.getCenter()
      
      const labelIcon = L.divIcon({
        className: 'map-region-label',
        html: `<div style="text-align: center; font-size: 12px; font-weight: bold; color: #fff; text-shadow: 1px 1px 2px #000; white-space: nowrap; pointer-events: none;">${region.name || ''}</div>`,
        iconSize: [100, 20],
        iconAnchor: [50, 10]
      })
      
      const labelMarker = L.marker(center, {
        icon: labelIcon,
        interactive: false,
        zIndexOffset: 1000
      })
      
      if (thematicLayer.value) {
        thematicLayer.value.addLayer(polygon)
        thematicLayer.value.addLayer(labelMarker)
      } else {
        polygon.addTo(map.value as any)
        labelMarker.addTo(map.value as any)
      }
    })
  }
  
  console.log('专题图层渲染完成')
}



// 根据数值计算风险等级（已移至文件末尾统一定义）

// 更新比例尺
const updateScale = () => {
  if (!map.value) return

  const denominator = calculateScaleDenominator()
  if (!denominator) return

  const rounded = Math.max(1, Math.round(denominator / 1000) * 1000)
  scaleText.value = `比例尺 1:${String(rounded)}`
}

const calculateScaleDenominator = (): number | null => {
  if (!map.value) return null

  const zoom = map.value.getZoom()
  const lat = map.value.getCenter().lat
  const metersPerPixel = 156543.03392 * Math.cos((lat * Math.PI) / 180) / Math.pow(2, zoom)

  const dpi = 96
  const inchesPerMeter = 39.37007874
  const denominator = metersPerPixel * dpi * inchesPerMeter

  if (!Number.isFinite(denominator) || denominator <= 0) return null
  return denominator
}

// 导出并上传专题图图片供OnlyOffice使用
const exportAndUploadForOnlyOffice = async (): Promise<string | null> => {
  return exportAndUploadForOnlyOfficeWithLevel(props.level)
}

// 导出并上传专题图图片供OnlyOffice使用（支持指定级别）
const exportAndUploadForOnlyOfficeWithLevel = async (level?: string): Promise<string | null> => {
  // 此函数可能被父组件用于生成报告，保留实现
  const targetLevel = level || props.level || 'township'
  const loadingMessage = ElMessage({
    message: `正在生成${targetLevel}级别专题图，请稍候...`,
    type: 'info',
    duration: 0
  })

  try {
    if (!mapContainer.value) {
      ElMessage.error('地图容器未找到')
      return null
    }

    // 如果指定了级别，先重新加载该级别的数据
    if (level && level !== props.level) {
      console.log(`切换到级别: ${level}，重新加载数据...`)
      overrideLevel.value = level
      await loadThematicData()
      // 等待地图渲染完成，增加延时确保Canvas绘制完成
      await new Promise(resolve => setTimeout(resolve, 3000))
    } else {
      // 即使不切换级别，也给予少量缓冲时间，确保图层完全渲染
      await new Promise(resolve => setTimeout(resolve, 1000))
    }

    // 使用html2canvas生成地图截图
    const canvas = await html2canvas(mapContainer.value, {
      useCORS: true,
      scale: 1, // 使用1倍缩放，保持与CSS设置的尺寸一致 (1910px)
      backgroundColor: '#ffffff',
      logging: false
    })

    // 将canvas转换为Blob
    const imageBlob = await new Promise<Blob>((resolve) => {
      canvas.toBlob((blob) => {
        if (blob) {
          resolve(blob)
        } else {
          resolve(new Blob([], { type: 'image/png' }))
        }
      }, 'image/png')
    })

    // 创建File对象，文件名包含级别信息
    const levelSuffix = level || props.level || 'township'
    const imageFile = new File([imageBlob], `thematic_map_${levelSuffix}_${Date.now()}.png`, { type: 'image/png' })

    // 上传到后端
    const response = await thematicMapApi.uploadMapImageWithLevel(
      imageFile,
      props.year || new Date().getFullYear(),
      props.orgCode || '511425',
      levelSuffix
    )

    if (response.success && response.data) {
      const imageUrl = response.data.imageUrl
      console.log('专题图上传成功:', imageUrl)

      // 重置覆盖级别
      overrideLevel.value = null

      loadingMessage.close()
      ElMessage.success('专题图上传成功')

      // 返回图片URL供OnlyOffice使用
      return imageUrl
    } else {
      throw new Error('上传失败：' + (response.message || '未知错误'))
    }
  } catch (error) {
    console.error('导出并上传专题图失败:', error)
    // 即使失败也要重置覆盖级别
    overrideLevel.value = null
    loadingMessage.close()
    ElMessage.error('上传专题图失败：' + ((error as any)?.message || '未知错误'))
    return null
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

    const rawLevel =
      item.capabilityLevel ||
      item.comprehensiveCapabilityLevel ||
      item.comprehensiveCapabilityGrade ||
      item.综合能力分级 ||
      item.totalGrade ||
      item.overallGrade ||
      item.overall_grade ||
      item.comprehensiveGrade ||
      item.comprehensive_grade ||
      item.comprehensiveCapability ||
      item.level

    const score = item.score ?? item.totalScore ?? item.comprehensiveCapabilityScore ?? item.comprehensiveCapability

    let level = normalizeCapabilityLevel(rawLevel, score)

    // 确保level是有效的等级值，如果不是则默认为中等
    const validLevels = ['强', '较强', '中等', '较弱', '弱']
    if (!validLevels.includes(level)) {
      console.warn(`无效的能力等级: ${level}，将归类为中等`)
      level = '中等'
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
    strongPercent: stats.total > 0 ? ((stats.strong / stats.total) * 100).toFixed(2) : '0.00',
    mediumStrongPercent: stats.total > 0 ? ((stats.mediumStrong / stats.total) * 100).toFixed(2) : '0.00',
    mediumPercent: stats.total > 0 ? ((stats.medium / stats.total) * 100).toFixed(2) : '0.00',
    weakPercent: stats.total > 0 ? ((stats.weak / stats.total) * 100).toFixed(2) : '0.00',
    veryWeakPercent: stats.total > 0 ? ((stats.veryWeak / stats.total) * 100).toFixed(2) : '0.00'
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

const normalizeToFeatureCollection = (raw: any, name: string) => {
  if (!raw || typeof raw !== 'object') return null
  if (raw.type === 'FeatureCollection' && Array.isArray(raw.features)) return raw
  if (raw.type === 'Feature' && raw.geometry) {
    return { type: 'FeatureCollection', features: [raw] }
  }
  if (raw.type && raw.coordinates) {
    return {
      type: 'FeatureCollection',
      features: [
        {
          type: 'Feature',
          properties: { name },
          geometry: raw
        }
      ]
    }
  }
  if (raw.geometry && raw.type && raw.type !== 'FeatureCollection') {
    return { type: 'FeatureCollection', features: [raw] }
  }
  return null
}

const looksLikeSubregionBoundaries = (fc: any, targetName: string) => {
  if (!fc || fc.type !== 'FeatureCollection' || !Array.isArray(fc.features)) return false

  const features = fc.features.filter((f: any) => f && f.geometry)
  const count = features.length
  if (count <= 0) return false

  const name = String(targetName || '').trim()
  const isProvince = name.includes('省') && !name.includes('市') && !name.includes('县') && !name.includes('区') && !name.includes('镇') && !name.includes('乡') && !name.includes('街道')
  const isCity = name.includes('市') && !name.includes('县') && !name.includes('区') && !name.includes('镇') && !name.includes('乡') && !name.includes('街道')
  const isCounty = (name.includes('县') || name.includes('区')) && !name.includes('镇') && !name.includes('乡') && !name.includes('街道')
  const needsChildren = isProvince || isCity || isCounty
  if (needsChildren) return count >= 3

  if (count > 1) return true
  const f = features[0]
  const p = f?.properties || {}
  return Boolean(p.xiang || p.XIANG || p.township || p.TOWNSHIP || p.TOWN || p.Town || p.village || p.VILLAGE || p.name || p.NAME)
}

const emptyFeatureCollection = () => ({ type: 'FeatureCollection', features: [] as any[] })

// 加载边界文件索引
const loadBoundaryIndex = async () => {
  if (boundaryIndex.value) {
    return boundaryIndex.value
  }

  try {
    const response = await fetch('/boundaries/index.json?t=' + Date.now())
    if (response.ok) {
      const index = await response.json()
      boundaryIndex.value = index
      return index
    }
  } catch (e) {
    console.warn('加载边界索引失败，将使用默认降级逻辑', e)
  }

  // 返回空索引，表示没有可用的边界文件
  return { availableYears: [], cities: [], yearlyCities: {} }
}

// 检查指定年份和城市的边界文件是否存在
const checkBoundaryExists = async (year: number, cityName: string): Promise<boolean> => {
  const index = await loadBoundaryIndex()

  // 检查是否有该年份的数据
  if (!index.yearlyCities[String(year)]) {
    return false
  }

  // 检查该城市是否在该年份的列表中
  return index.yearlyCities[String(year)].includes(cityName)
}

// 缓存边界数据，避免重复加载API调用
let cachedBoundaries: { key: string; value: any } | null = null
let boundaryLoadInProgress: { key: string; promise: Promise<any> } | null = null

// 加载真实的乡镇边界数据
const loadRealBoundaryData = async () => {
  try {
    console.log('开始加载真实边界数据')

    const evaluationData = (window as any).evaluationData
    const resolvedOrgName = (props.orgName || (evaluationData && evaluationData.countyName) || '').trim()
    const hasOrgContext = Boolean(props.orgId || (props.orgCode && props.orgCode.trim()) || resolvedOrgName)
    if (!hasOrgContext) {
      return emptyFeatureCollection()
    }

    const targetName = resolvedOrgName || '青神县'

    if (props.orgId && props.year) {
      try {
        const cacheKey = `${props.orgId}-${props.year}`

        // 使用缓存或进行中的请求，避免重复调用
        if (cachedBoundaries && cachedBoundaries.key === cacheKey) {
          console.log('使用缓存的边界数据')
          return cachedBoundaries.value
        }

        if (boundaryLoadInProgress && boundaryLoadInProgress.key === cacheKey) {
          console.log('等待进行中的边界数据请求')
          return await boundaryLoadInProgress.promise
        }

        // 创建新的加载Promise
        boundaryLoadInProgress = {
          key: cacheKey,
          promise: (async () => {
            const boundaryResponse = await organizationBoundaryApi.getBoundary(props.orgId, props.year)
          const boundaryData = (boundaryResponse as any)?.data

          const filePath = typeof boundaryData?.filePath === 'string' ? boundaryData.filePath.trim() : ''
          if (filePath) {
            try {
              const res = await fetch(filePath + (filePath.includes('?') ? '&' : '?') + 't=' + Date.now())
              if (res.ok) {
                const json = await res.json()
                const normalized = normalizeToFeatureCollection(json, props.orgName || '')
                if (normalized && looksLikeSubregionBoundaries(normalized, targetName)) {
                  console.log('使用组织边界配置的文件路径加载边界:', filePath)
                  return normalized
                }
              }
            } catch { }
          }

          const coordinates = boundaryData?.boundaryCoordinates
          if (coordinates) {
            try {
              const parsed = typeof coordinates === 'string' ? JSON.parse(coordinates) : coordinates
              const normalized = normalizeToFeatureCollection(parsed, props.orgName || '')
              if (normalized && looksLikeSubregionBoundaries(normalized, targetName)) {
                console.log('使用组织边界配置的坐标数据加载边界')
                return normalized
              }
            } catch { }
          }

          // 如果API返回null（没有数据），返回空结果而不是继续尝试其他方法
          console.log('API返回null，组织没有边界数据配置')
          return null
          })()
        }

        const result = await boundaryLoadInProgress.promise
        if (result && Array.isArray(result.features) && result.features.length > 0) {
          cachedBoundaries = { key: cacheKey, value: result }
        }
        boundaryLoadInProgress = null
        if (result) return result
      } catch (apiError) {
        console.warn('API获取边界数据失败，将尝试其他方法:', apiError)
        boundaryLoadInProgress = null
        // 继续尝试其他方法，不抛出错误
      }
    }

    // 1. 获取目标区域名称
    const targetKey = normalizeRegionName(targetName)
    console.log('目标区域:', targetName)

    // 2. 加载区域层级信息，用于判断所属城市以及层级过滤（使用缓存）
    let filterLevel = 'county'; // 默认为县级过滤
    let hierarchyData = null;
    let targetRegionNode = null;
    let cityName = null;

    try {
        // 使用缓存避免重复加载大文件
        if (regionHierarchyCache) {
            hierarchyData = regionHierarchyCache
            console.log('使用缓存的 region_hierarchy 数据')
        } else if (regionHierarchyLoading) {
            console.log('等待 region_hierarchy 加载...')
            hierarchyData = await regionHierarchyLoading
        } else {
            console.log('首次加载 region_hierarchy.json...')
            regionHierarchyLoading = fetch('/region_hierarchy.json')
                .then(res => res.json())
                .then(data => {
                    regionHierarchyCache = data
                    return data
                })
            hierarchyData = await regionHierarchyLoading
            regionHierarchyLoading = null
        }

        if (hierarchyData) {
            const hierarchyRoot =
              hierarchyData && Array.isArray((hierarchyData as any).children)
                ? hierarchyData
                : (() => {
                    const values = hierarchyData && typeof hierarchyData === 'object' ? Object.values(hierarchyData) : []
                    const provinceNode = (values as any[]).find(v => v && v.level === 'province' && Array.isArray(v.children))
                    if (provinceNode) return provinceNode
                    const anyNode = (values as any[]).find(v => v && Array.isArray(v.children))
                    return anyNode || null
                  })();

            // 查找节点以确定 filterLevel
            targetRegionNode = findRegionInHierarchy(hierarchyData, targetName);
            if (targetRegionNode) {
                filterLevel = targetRegionNode.level || 'county';
                console.log(`目标区域 ${targetName} 的层级为: ${filterLevel}`);
            }

            // 查找所属城市 (用于按需加载边界文件)
            const targetKey = normalizeRegionName(targetName);
            if (hierarchyRoot && (hierarchyRoot as any).children) {
                 for (const city of (hierarchyRoot as any).children) {
                     const cityKey = normalizeRegionName(city.name);
                     // 检查是否是该城市本身
                     if (cityKey === targetKey || cityKey.includes(targetKey) || targetKey.includes(cityKey)) {
                         cityName = city.name;
                         break;
                     }
                     // 检查该城市的下级区县
                     if (city.children) {
                         const found = city.children.find((c: any) => {
                             const cKey = normalizeRegionName(c.name);
                             return cKey === targetKey || cKey.includes(targetKey) || targetKey.includes(cKey);
                         });
                         if (found) {
                             cityName = city.name;
                             break;
                         }
                     }
                 }
            }
        }
    } catch (e) {
        console.warn('加载区域层级信息失败，将默认按县级过滤', e);
    }

    // 3. 决定加载哪个边界文件
    // 只有当明确找到了所属城市，且不是省级视图时，才加载分片文件
    // 如果是省级视图，可能需要加载所有城市的简图（这由 hierarchy 优化逻辑处理），
    // 或者如果 hierarchy 优化没命中，这里回退到 shp.geojson (虽然很慢)
    let fetchUrl = '/shp.geojson'; 
    if (cityName) {
        // 使用 props.year 或默认 2025 (如果 props.year 未定义)
        const year = props.year || 2025;
        fetchUrl = `/boundaries/${year}/city/${cityName}.json`;
        console.log(`定位到城市: ${cityName}, 年份: ${year}, 准备加载分片边界文件: ${fetchUrl}`);
    } else {
        console.warn(`未定位到所属城市 (目标: ${targetName}), 将使用全量边界文件`);
    }

    // 4. 执行加载
    let data;
    try {
        const cacheBuster = () => String(props.year || 0)

        const isJsonResponse = (response: Response) => {
          const contentType = (response.headers.get('content-type') || '').toLowerCase()
          return contentType.includes('json')
        }

        const tryFetchJson = async (url: string) => {
          const response = await fetch(url + (url.includes('?') ? '&' : '?') + 't=' + cacheBuster(), {
            headers: {
              Accept: 'application/json'
            }
          })
          if (!response.ok) return { ok: false as const, status: response.status }
          if (!isJsonResponse(response)) return { ok: false as const, status: response.status }
          try {
            const json = await response.json()
            return { ok: true as const, json }
          } catch {
            return { ok: false as const, status: response.status }
          }
        }

        if (cityName) {
          const requestedYear = props.year || 2025
          const minCityBoundaryYear = 2020
          const startYear = requestedYear

          // 先加载边界索引，过滤出实际存在的文件
          const index = await loadBoundaryIndex()
          const yearCandidates: number[] = []

          // 使用索引检查哪些年份的边界文件存在
          for (let y = startYear; y >= minCityBoundaryYear; y--) {
            // 如果索引中有该年份和城市的数据，则添加到候选列表
            if (index.yearlyCities[String(y)]?.includes(cityName)) {
              yearCandidates.push(y)
            }
          }

          // 构建候选列表：先尝试按年份的文件，最后尝试通用文件
          const candidates = [
            ...yearCandidates.map(y => `/boundaries/${y}/city/${cityName}.json`),
            `/boundaries/city/${cityName}.json`
          ].filter((v, i, a) => a.indexOf(v) === i)

          console.log(`边界文件候选列表 (${cityName}, ${requestedYear}年):`, candidates)

          let loaded: any = null
          for (const u of candidates) {
            const res = await tryFetchJson(u)
            if (res.ok) {
              fetchUrl = u
              loaded = res.json
              break
            }
            console.warn(`加载 ${u} 失败 (status: ${res.status})`)
          }

          if (!loaded) {
            throw new Error(`未找到可用的城市边界文件: ${cityName}`)
          }

          data = loaded
        } else {
          const response = await fetch(fetchUrl + '?t=' + cacheBuster(), {
            headers: {
              Accept: 'application/json'
            }
          })
          if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`)
          }
          if (!isJsonResponse(response)) {
            throw new Error(`Unexpected content-type: ${response.headers.get('content-type') || 'unknown'}`)
          }
          data = await response.json()
        }
    } catch (err) {
        console.warn(`加载 ${fetchUrl} 失败`, err);
        throw err;
    }
    
    // 检查数据有效性
    if (!data || !data.features || !Array.isArray(data.features)) {
      throw new Error('边界数据格式无效')
    }
    
    // 过滤掉无效的features
    data.features = data.features.filter((feature: any) => feature && feature.properties && feature.geometry)
    
    console.log('边界数据加载成功，特征数量:', data.features?.length)
    
    // 后续逻辑保持不变...
    // 但需要移除原有的 targetName 获取和 hierarchy 加载逻辑，因为已经移到前面了


    // 如果目标是省或市，且在 region_hierarchy.json 中找到了对应节点
    // 则优先使用 hierarchy 中的 children 生成 feature collection
    // 这样使用的是简化后的边界，避免加载整个 shp.geojson 导致卡顿
    if (hierarchyData && targetRegionNode) {
         if (filterLevel === 'province' || filterLevel === 'city') {
            console.log(`使用 region_hierarchy.json 生成 ${targetName} (${filterLevel}) 的下级区域边界`);
            
            if (targetRegionNode.children && targetRegionNode.children.length > 0) {
                const features = targetRegionNode.children.map((child: any) => {
                    return {
                        type: 'Feature',
                        properties: {
                            name: child.name,
                            // 兼容 shp.geojson 的属性名
                            COUNTY: child.level === 'county' ? child.name : undefined,
                            CITY: child.level === 'city' ? child.name : undefined,
                            // 添加通用属性
                            level: child.level
                        },
                        geometry: child.geometry
                    };
                }).filter((f: any) => f.geometry); // 确保有 geometry

                if (features.length > 0) {
                    console.log(`从层级数据生成了 ${features.length} 个子区域特征`);
                    return {
                        type: 'FeatureCollection',
                        features: features
                    };
                }
            } else {
                 console.warn(`${targetName} 没有下级区域数据`);
            }
         }
    }

    // 如果没有命中上面的优化逻辑（例如目标是县级，或者 hierarchy 加载失败），则回退到加载 shp.geojson
    // 注意：如果 filterLevel 是 province 但没有 hierarchy，加载 shp.geojson 可能会很慢


    // 过滤边界数据
    const filteredFeatures = data.features
      .filter((feature: any) => feature && feature.properties) // 先过滤掉undefined或无properties的feature
      .filter((feature: any) => {
        const props = feature.properties;
        const fCounty = (props.COUNTY || props.county || '').trim();
        const fCity = (props.CITY || props.city || '').trim();
        const fProvince = (props.PROVINCE || props.province || '四川省').trim();
        const fCountyKey = normalizeRegionName(fCounty)
        const fCityKey = normalizeRegionName(fCity)
        const fProvinceKey = normalizeRegionName(fProvince)

        if (filterLevel === 'city') {
            // 如果目标是市级，匹配城市名称
            return fCityKey && targetKey ? (fCityKey === targetKey || fCityKey.includes(targetKey) || targetKey.includes(fCityKey)) : fCity === targetName
        } else if (filterLevel === 'province') {
             // 如果目标是省级，我们尽量不要在这里返回所有县，因为太多了。
             // 如果走到了这里（说明上面的 region_hierarchy 优化没命中），
             // 只能硬着头皮返回，但建议添加警告或限制
            // 如果目标是省级，匹配省份名称
            return fProvinceKey && targetKey ? (fProvinceKey === targetKey || fProvinceKey.includes(targetKey) || targetKey.includes(fProvinceKey)) : fProvince === targetName
        } else {
            // 默认为县级，匹配县名
            if (!targetKey) return true
            if (!fCountyKey) return false
            return fCountyKey === targetKey || fCountyKey.includes(targetKey) || targetKey.includes(fCountyKey)
        }
      });
    
    console.log(`过滤后的边界数据，从 ${data.features?.length} 个特征过滤到 ${filteredFeatures.length} 个特征 (过滤级别: ${filterLevel}, 目标: ${targetName})`)
    
    // 如果过滤后没有特征，尝试使用备用数据
    if (filteredFeatures.length === 0) {
      console.warn('过滤后没有有效的边界特征，使用备用边界数据')
      return emptyFeatureCollection()
    }
    
    // 返回过滤后的数据
    const filteredData = {
      ...data,
      features: filteredFeatures
    }
    
    return filteredData
  } catch (error) {
    console.error('加载真实边界数据失败:', error)
    return emptyFeatureCollection()
  }
}

// 生成备用边界数据（当真实数据加载失败时使用）
const generateFallbackBoundaries = () => {
  return emptyFeatureCollection()
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

// 添加防抖，避免短时间内多次调用API
let loadDataTimer: any = null
const loadThematicDataDebounced = () => {
  if (loadDataTimer) clearTimeout(loadDataTimer)
  loadDataTimer = setTimeout(() => {
    if (map.value) loadThematicData()
  }, 300)
}

watch(
  () => [props.year, props.orgCode, props.orgName, props.orgId, props.algorithmId, props.reportId],
  () => {
    loadThematicDataDebounced()
  }
)

// 保存到服务器
const saveToServer = async (imageData: string, format: string) => {
  try {
    await thematicMapApi.saveMapImage({
      imageData,
      format,
      reportId: props.reportId,
      title: mapConfigState.value.title,
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

// 暴露方法给父组件
defineExpose({
  exportAndUploadForOnlyOffice,
  exportAndUploadForOnlyOfficeWithLevel,
  getStatistics,
  mapConfigState
})
</script>

<style scoped lang="scss">
.thematic-map-container {
  position: relative;
  width: 100%;
  height: 100%;
  background: #fff;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  
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
  
  // 紧凑模式
  &.compact {
    width: 100%;
    height: 100%;
    
    .config-panel {
        display: none !important;
    }
  }
  
  // 顶部标题区域
  .map-header-section {
    flex: 0 0 auto;
    text-align: center;
    padding: 15px 0;
    background: #fff;
    z-index: 1000;
    
    h1 {
      margin: 0;
      font-size: 32px;
      font-weight: bold;
      color: #000;
      font-family: "SimHei", "Microsoft YaHei", sans-serif;
      letter-spacing: 1px;
    }
  }

  // 地图主体区域
  .map-body-wrapper {
    flex: 1;
    position: relative;
    width: 100%;
    min-height: 0;
    overflow: hidden;
  }

  .map-content {
    width: 100%;
    height: 100%;
    position: relative;
    overflow: hidden;
  }
  
  :deep(.leaflet-container) {
    width: 100%;
    height: 100%;
    overflow: hidden;
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
  
  // 底部监制信息区域
  .map-footer-section {
    flex: 0 0 auto;
    display: flex;
    justify-content: space-between;
    padding: 15px 40px;
    box-sizing: border-box;
    font-size: 20px;
    font-weight: bold;
    color: #000;
    background: #fff;
    z-index: 1000;
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
        border-radius: 0;
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
    left: 50%;
    transform: translateX(-50%);
    
    .scale-bar {
      background: rgba(255, 255, 255, 0.9);
      padding: 6px 12px;
      border-radius: 4px;
      border: 1px solid #ccc;
      text-align: center;
      
      .scale-line {
        display: none;
      }
      
      .scale-text {
        font-size: 14px;
        color: #333;
        text-align: center;
        font-weight: bold;
      }

      .scale-note {
        margin-top: 2px;
        font-size: 12px;
        color: #333;
        text-align: center;
      }
    }
  }
  
  // 指北针样式
  .map-compass {
    position: absolute;
    top: 30px;
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
    z-index: 0;
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
  // 已移除
  
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

:deep(.leaflet-tooltip.village-point-tooltip) {
  background: transparent;
  border: none;
  box-shadow: none;
  padding: 0;
  color: #000;
  font-size: 10px;
  font-weight: bold;
  text-shadow: 1px 1px 0 #fff, -1px -1px 0 #fff, 1px -1px 0 #fff, -1px 1px 0 #fff;
  white-space: nowrap;
}

:deep(.leaflet-tooltip.village-point-tooltip::before) {
  display: none;
}

:deep(.leaflet-tooltip.village-point-tooltip.village-point-tooltip--small) {
  font-size: 9px;
  font-weight: normal;
}
</style>
