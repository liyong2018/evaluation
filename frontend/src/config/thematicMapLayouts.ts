/**
 * 专题图布局预设
 */
import type { ThematicMapLayout } from '@/types/thematic-map'

/**
 * 默认布局：地图在左侧，统计表在右上，图例在右下
 * 适合：标准的Word报告插图
 */
export const defaultLayout: ThematicMapLayout = {
  layout: {
    canvasWidth: 1200,
    canvasHeight: 800,
    backgroundColor: '#ffffff'
  },
  elements: [
    {
      id: 'map_view',
      type: 'leaflet_map',
      position: { x: 0, y: 0, width: 850, height: 800 },
      config: {
        center: [29.8, 103.8], // 青神县中心坐标
        zoom: 10,
        tileLayer: 'https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png',
        minZoom: 8,
        maxZoom: 15
      }
    },
    {
      id: 'statistical_table',
      type: 'table',
      position: { x: 860, y: 0, width: 340, height: 400 },
      config: {
        title: '减灾能力等级统计',
        columns: [
          { header: '等级', width: '33%', align: 'center' },
          { header: '数量', width: '33%', align: 'center' },
          { header: '占比', width: '34%', align: 'center' }
        ],
        dataSource: 'evaluationStatistics',
        rowHeight: 35,
        headerStyle: {
          backgroundColor: '#f0f0f0',
          fontWeight: 'bold',
          fontSize: 14,
          color: '#333333'
        }
      }
    },
    {
      id: 'legend',
      type: 'legend',
      position: { x: 860, y: 410, width: 340, height: 390 },
      config: {
        title: '能力等级图例',
        orientation: 'vertical',
        items: [
          { color: '#52c41a', label: '强', value: '90-100分' },
          { color: '#1890ff', label: '较强', value: '80-89分' },
          { color: '#fa8c16', label: '中等', value: '70-79分' },
          { color: '#ff4d4f', label: '较弱', value: '60-69分' },
          { color: '#722ed1', label: '弱', value: '0-59分' }
        ]
      }
    }
  ]
}

/**
 * 横向布局：地图在上，统计表和图例在下
 * 适合：横向显示的专题图
 */
export const horizontalLayout: ThematicMapLayout = {
  layout: {
    canvasWidth: 1200,
    canvasHeight: 900,
    backgroundColor: '#ffffff'
  },
  elements: [
    {
      id: 'map_view',
      type: 'leaflet_map',
      position: { x: 0, y: 0, width: 1200, height: 550 },
      config: {
        center: [29.8, 103.8],
        zoom: 10,
        tileLayer: 'https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png'
      }
    },
    {
      id: 'statistical_table',
      type: 'table',
      position: { x: 0, y: 560, width: 600, height: 340 },
      config: {
        title: '减灾能力等级统计',
        columns: [
          { header: '等级', width: '33%', align: 'center' },
          { header: '数量', width: '33%', align: 'center' },
          { header: '占比', width: '34%', align: 'center' }
        ],
        dataSource: 'evaluationStatistics'
      }
    },
    {
      id: 'legend',
      type: 'legend',
      position: { x: 610, y: 560, width: 590, height: 340 },
      config: {
        title: '能力等级图例',
        orientation: 'horizontal',
        items: [
          { color: '#52c41a', label: '强' },
          { color: '#1890ff', label: '较强' },
          { color: '#fa8c16', label: '中等' },
          { color: '#ff4d4f', label: '较弱' },
          { color: '#722ed1', label: '弱' }
        ]
      }
    }
  ]
}

/**
 * 简洁布局：只有地图和图例，无统计表
 * 适合：只需要地图可视化的场景
 */
export const minimalLayout: ThematicMapLayout = {
  layout: {
    canvasWidth: 1000,
    canvasHeight: 700,
    backgroundColor: '#ffffff'
  },
  elements: [
    {
      id: 'map_view',
      type: 'leaflet_map',
      position: { x: 0, y: 0, width: 700, height: 700 },
      config: {
        center: [29.8, 103.8],
        zoom: 10,
        tileLayer: 'https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png'
      }
    },
    {
      id: 'legend',
      type: 'legend',
      position: { x: 710, y: 0, width: 290, height: 700 },
      config: {
        title: '能力等级图例',
        orientation: 'vertical',
        items: [
          { color: '#52c41a', label: '强' },
          { color: '#1890ff', label: '较强' },
          { color: '#fa8c16', label: '中等' },
          { color: '#ff4d4f', label: '较弱' },
          { color: '#722ed1', label: '弱' }
        ]
      }
    }
  ]
}

/**
 * 大地图布局：地图占据大部分空间，统计表和图例以浮层形式显示
 * 适合：需要突出地图细节的场景
 */
export const largeMapLayout: ThematicMapLayout = {
  layout: {
    canvasWidth: 1400,
    canvasHeight: 900,
    backgroundColor: '#ffffff'
  },
  elements: [
    {
      id: 'map_view',
      type: 'leaflet_map',
      position: { x: 0, y: 0, width: 1000, height: 900 },
      config: {
        center: [29.8, 103.8],
        zoom: 9,
        tileLayer: 'https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png'
      }
    },
    {
      id: 'statistical_table',
      type: 'table',
      position: { x: 1010, y: 0, width: 380, height: 450 },
      config: {
        title: '减灾能力等级统计',
        columns: [
          { header: '等级', width: '33%', align: 'center' },
          { header: '数量', width: '33%', align: 'center' },
          { header: '占比', width: '34%', align: 'center' }
        ],
        dataSource: 'evaluationStatistics'
      }
    },
    {
      id: 'legend',
      type: 'legend',
      position: { x: 1010, y: 460, width: 380, height: 440 },
      config: {
        title: '能力等级图例',
        orientation: 'vertical',
        items: [
          { color: '#52c41a', label: '强', value: '90-100分' },
          { color: '#1890ff', label: '较强', value: '80-89分' },
          { color: '#fa8c16', label: '中等', value: '70-79分' },
          { color: '#ff4d4f', label: '较弱', value: '60-69分' },
          { color: '#722ed1', label: '弱', value: '0-59分' }
        ]
      }
    }
  ]
}

// 导出所有布局
export const layouts = {
  default: defaultLayout,
  horizontal: horizontalLayout,
  minimal: minimalLayout,
  largeMap: largeMapLayout
}

/**
 * 获取默认布局
 */
export function getDefaultLayout(): ThematicMapLayout {
  return defaultLayout
}

/**
 * 根据名称获取布局
 */
export function getLayoutByName(name: string): ThematicMapLayout {
  return layouts[name as keyof typeof layouts] || defaultLayout
}
