/**
 * 专题图布局配置类型定义
 */

/**
 * 布局配置
 */
export interface LayoutConfig {
  canvasWidth: number
  canvasHeight: number
  backgroundColor: string
}

/**
 * 元素位置
 */
export interface ElementPosition {
  x: number
  y: number
  width: number
  height: number
}

/**
 * 地图元素配置
 */
export interface MapElementConfig {
  id: string
  type: 'leaflet_map' | 'table' | 'legend' | 'text' | 'chart'
  position: ElementPosition
  config: any
}

/**
 * 专题图布局
 */
export interface ThematicMapLayout {
  layout: LayoutConfig
  elements: MapElementConfig[]
}

/**
 * 地图元素类型配置
 */
export interface LeafletMapConfig {
  center: [number, number] // [latitude, longitude]
  zoom: number
  tileLayer: string
  geojsonData?: any
  minZoom?: number
  maxZoom?: number
}

/**
 * 统计表配置
 */
export interface StatisticalTableConfig {
  title?: string
  columns: TableColumn[]
  dataSource?: string // 'evaluationStatistics' | 'communityStatistics' | 'townshipStatistics'
  rowHeight?: number
  headerStyle?: HeaderStyle
}

/**
 * 表格列定义
 */
export interface TableColumn {
  header: string
  width: string
  align?: 'left' | 'center' | 'right'
}

/**
 * 表头样式
 */
export interface HeaderStyle {
  backgroundColor?: string
  fontWeight?: string
  fontSize?: number
  color?: string
}

/**
 * 图例配置
 */
export interface LegendConfig {
  title?: string
  orientation?: 'vertical' | 'horizontal'
  items: LegendItem[]
}

/**
 * 图例项
 */
export interface LegendItem {
  color: string
  label: string
  value?: string
}

/**
 * 能力等级颜色映射
 */
export const CAPABILITY_LEVEL_COLORS: Record<string, string> = {
  '强': '#52c41a',
  '较强': '#1890ff',
  '中等': '#fa8c16',
  '较弱': '#ff4d4f',
  '弱': '#722ed1'
}

/**
 * 能力等级对应的分数范围
 */
export const CAPABILITY_LEVEL_RANGES: Record<string, { min: number; max: number }> = {
  '强': { min: 90, max: 100 },
  '较强': { min: 80, max: 89 },
  '中等': { min: 70, max: 79 },
  '较弱': { min: 60, max: 69 },
  '弱': { min: 0, max: 59 }
}

/**
 * 根据分数获取能力等级
 */
export function getCapabilityLevel(score: number): string {
  for (const [level, range] of Object.entries(CAPABILITY_LEVEL_RANGES)) {
    if (score >= range.min && score <= range.max) {
      return level
    }
  }
  return '中等'
}

/**
 * 根据能力等级获取颜色
 */
export function getCapabilityColor(level: string): string {
  return CAPABILITY_LEVEL_COLORS[level] || CAPABILITY_LEVEL_COLORS['中等']
}

/**
 * 统计数据项
 */
export interface StatisticsItem {
  level: string
  count: number
  percent: number
}

/**
 * 专题图数据
 */
export interface ThematicMapData {
  regionId: number
  regionName: string
  county: string
  score: number
  capabilityLevel: string
  details: {
    disasterPreventionCapability: number
    emergencyResponseCapability: number
    recoveryReconstructionCapability: number
  }
}
