<template>
  <div class="composite-map-container" ref="containerRef">
    <!-- 顶部标题区域 -->
    <div class="composite-header">
      <h1>{{ mapTitle }}</h1>
    </div>

    <!-- 中间地图区域 -->
    <div class="map-content-wrapper">
      <!-- 左侧 3/4 主图区域 -->
      <div class="main-map-area">
        <ThematicMapGenerator
          ref="mainMapRef"
          :year="year"
          :orgCode="orgCode"
          level="comprehensive"
          :isCompact="true"
          :mapConfig="{ 
            showTitle: false, 
            showLegend: true, 
            showDataTable: true, 
            showScale: true, 
            showCompass: true, 
            showBorder: false,
            showFooter: false
          }"
          class="main-map"
        />
      </div>

      <!-- 右侧 1/4 副图区域 -->
      <div class="side-maps-area">
        <!-- 上：乡镇级 -->
        <div class="side-map-item">
          <ThematicMapGenerator
            :year="year"
            :orgCode="orgCode"
            level="township"
            :isCompact="true"
            :mapConfig="{ 
              showTitle: false, 
              showLegend: true, 
              showDataTable: true, 
              showScale: false, 
              showCompass: true, 
              showBorder: false,
              showFooter: false,
              title: '乡镇（街道）减灾能力评估结果图' 
            }"
            class="side-map"
          />
        </div>

        <!-- 中：社区-乡镇级 -->
        <div class="side-map-item">
          <ThematicMapGenerator
            :year="year"
            :orgCode="orgCode"
            level="community_township"
            :isCompact="true"
            :mapConfig="{ 
              showTitle: false, 
              showLegend: true, 
              showDataTable: true, 
              showScale: false, 
              showCompass: true, 
              showBorder: false,
              showFooter: false,
              title: '社区（行政村）减灾能力（乡镇单元）评估结果图' 
            }"
            class="side-map"
          />
        </div>

        <!-- 下：社区-行政村级 -->
        <div class="side-map-item">
          <ThematicMapGenerator
            :year="year"
            :orgCode="orgCode"
            level="community_village"
            :isCompact="true"
            :mapConfig="{ 
              showTitle: false, 
              showLegend: true, 
              showDataTable: true, 
              showScale: false, 
              showCompass: true, 
              showBorder: false,
              showFooter: false,
              title: '社区（行政村）减灾能力（社区单元）评估结果图' 
            }"
            class="side-map"
          />
        </div>
      </div>
    </div>

    <!-- 底部监制信息区域 -->
    <div class="composite-footer">
      <div class="footer-left">四川省减灾中心     监制</div>
      <div class="footer-right">{{ currentYearMonth }}</div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, defineExpose, computed } from 'vue'
import ThematicMapGenerator from './ThematicMapGenerator.vue'
import html2canvas from 'html2canvas'
import { thematicMapApi } from '@/api'
import { ElMessage } from 'element-plus'

interface Props {
  year?: number
  orgCode?: string
  orgName?: string
}

const props = withDefaults(defineProps<Props>(), {
  year: 2025,
  orgCode: '511425'
})

const mapTitle = computed(() => {
  const region = props.orgName || '青神县'
  return `${region}综合减灾能力评估结果图`
})

const currentYearMonth = computed(() => {
  const date = new Date()
  const year = props.year || date.getFullYear()
  const month = date.getMonth() + 1
  return `${year}年${month}月`
})

const mainMapRef = ref()
const containerRef = ref<HTMLElement>()

// 导出并上传组合图
const exportAndUploadForOnlyOffice = async () => {
  if (!containerRef.value) return null
  
  try {
    console.log('开始生成组合专题图...')
    
    // 等待地图完全渲染
    await new Promise(resolve => setTimeout(resolve, 2000))
    
    const canvas = await html2canvas(containerRef.value, {
      useCORS: true,
      allowTaint: true,
      backgroundColor: '#ffffff',
      scale: 2 // 提高清晰度
    })
    
    const imageData = canvas.toDataURL('image/png')
    
    // 将base64转换为blob
    const arr = imageData.split(',')
    const mime = arr[0].match(/:(.*?);/)?.[1]
    const bstr = atob(arr[1])
    let n = bstr.length
    const u8arr = new Uint8Array(n)
    while (n--) {
      u8arr[n] = bstr.charCodeAt(n)
    }
    const blob = new Blob([u8arr], { type: mime })

    // 上传到服务器
    // 创建File对象
    const imageFile = new File([blob], `composite_map_${props.year}.png`, { type: 'image/png' })
    
    // 使用 uploadMapImageWithLevel 接口
    const response = await thematicMapApi.uploadMapImageWithLevel(
      imageFile,
      props.year || 2025,
      props.orgCode || '511425',
      'comprehensive'
    )

    if (response.success && response.data) {
        return response.data.imageUrl // 返回图片URL
    }
    return null
    
  } catch (error) {
    console.error('生成组合图失败:', error)
    ElMessage.error('生成组合图失败')
    return null
  }
}

defineExpose({
  exportAndUploadForOnlyOffice
})
</script>

<style scoped lang="scss">
.composite-map-container {
  display: flex;
  flex-direction: column;
  width: 1920px;
  height: 1080px;
  background: white;
  border: 1px solid #ddd;
  box-sizing: border-box;
  overflow: hidden;
  padding: 0;
}

.composite-header {
  flex: 0 0 auto;
  text-align: center;
  padding: 20px 0;
  background: #fff;
  
  h1 {
    margin: 0;
    font-size: 42px;
    font-weight: bold;
    color: #000;
    font-family: "SimHei", "Microsoft YaHei", sans-serif;
    letter-spacing: 2px;
  }
}

.map-content-wrapper {
  flex: 1;
  display: flex;
  gap: 15px;
  padding: 0 15px;
  min-height: 0;
  margin-bottom: 0;
}

.main-map-area {
  flex: 3;
  height: 100%;
  position: relative;
  border: 2px solid #333;
  box-sizing: border-box;
  
  :deep(.thematic-map-container.compact) {
    .map-border {
       display: none !important;
    }
    .map-header-section, .map-footer-section {
       display: none !important;
    }
    .map-legend {
        bottom: 20px;
        left: 20px;
    }
  }
}

.side-maps-area {
  flex: 1;
  height: 100%;
  display: flex;
  flex-direction: column;
  border: 2px solid #333;
  box-sizing: border-box;
  
  .side-map-item {
    flex: 1;
    position: relative;
    border-bottom: 2px solid #333;
    overflow: hidden;
    
    &:last-child {
      border-bottom: none;
    }
    
    :deep(.thematic-map-container.compact) {
      .map-border {
        display: none !important;
      }
      .map-header-section, .map-footer-section {
         display: none !important;
      }
      .map-legend {
        transform: scale(0.6);
        transform-origin: bottom left;
        bottom: 5px;
        left: 5px;
        padding: 5px;
        min-width: 80px;
        
        .legend-title {
            font-size: 10px;
            margin-bottom: 5px;
        }
        .legend-item {
            gap: 4px;
            font-size: 10px;
            .legend-color {
                width: 12px;
                height: 12px;
            }
        }
      }
      
      .map-data-table {
        transform: scale(0.6);
        transform-origin: bottom right;
        bottom: 5px;
        right: 5px;
        padding: 5px;
        min-width: 150px;
        
        .table-title {
            font-size: 10px;
            margin-bottom: 5px;
        }
        
        .data-table {
            font-size: 10px;
            
            th, td {
                padding: 2px 4px;
            }
        }
      }

      .map-compass {
         transform: scale(0.6);
         top: 10px;
         right: 10px;
      }
    }
  }
}

.composite-footer {
  flex: 0 0 auto;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 40px 20px 40px;
  font-size: 24px;
  font-weight: bold;
  color: #000;
  background: #fff;
  font-family: "SimHei", "Microsoft YaHei", sans-serif;
}
</style>
