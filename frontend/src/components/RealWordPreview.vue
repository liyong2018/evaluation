<template>
  <div class="real-word-preview-container">
    <div class="preview-header">
      <h2>Word文档预览 (Mammoth.js)</h2>
      <div class="header-actions">
        <el-button @click="loadWordPreview" icon="Refresh" :loading="loading">
          刷新预览
        </el-button>
        <el-button @click="downloadWord" type="primary" icon="Download">
          下载Word文档
        </el-button>
        <el-button @click="$emit('close')" icon="Close">关闭预览</el-button>
      </div>
    </div>

    <div class="preview-content" v-loading="loading">
      <div v-if="htmlContent" class="word-document-preview">
        <div class="document-info">
          <el-alert
            title="Word文档HTML预览"
            type="info"
            :closable="false"
            show-icon
          >
            <template #default>
              <p>此预览使用Mammoth.js将Word文档转换为HTML，尽可能保持原始格式</p>
            </template>
          </el-alert>
        </div>

        <div class="document-content" v-html="htmlContent"></div>
      </div>

      <div v-else-if="!loading" class="no-data">
        <el-empty description="暂无Word文档内容" />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, defineProps, withDefaults } from 'vue'
import { ElMessage } from 'element-plus'
import mammoth from 'mammoth'
import { wordTemplateApi } from '@/api'

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
const htmlContent = ref('')

// 使用Mammoth.js加载Word文档预览
const loadWordPreview = async () => {
  loading.value = true
  try {
    // 获取Word文档文件
    // 调用 generateReport 接口获取生成的报告
    const response = await wordTemplateApi.generateReport(props.year, props.orgCode)

    if (!response) {
      throw new Error('未获取到文档数据')
    }

    // axios返回 {data, headers} 格式
    const blob = response.data || (response.data !== undefined ? response.data : response)
    const arrayBuffer = await blob.arrayBuffer()

    // 使用Mammoth.js转换Word文档为HTML
    const result = await mammoth.convertToHtml({ arrayBuffer })

    if (result.messages && result.messages.length > 0) {
      console.log('Mammoth转换消息:', result.messages)
      ElMessage.warning(`文档转换完成，但有${result.messages.length}个警告信息，详见控制台`)
    }

    htmlContent.value = result.value
    ElMessage.success('Word文档预览加载成功')
  } catch (error) {
    console.error('加载Word文档预览失败:', error)
    ElMessage.error('加载Word文档预览失败: ' + (error as Error).message)
  } finally {
    loading.value = false
  }
}

// 监听props变化
import { watch } from 'vue'
watch(() => [props.year, props.orgCode], () => {
  loadWordPreview()
})

// 下载Word文档
const downloadWord = async () => {
  const loadingInstance = ElMessage({ message: '正在生成Word文档，请稍候...', type: 'info', duration: 0 })
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

    const blob = new Blob([blobData], {
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
    loadingInstance.close()
  }
}

// 组件挂载时加载数据
onMounted(() => {
  loadWordPreview()
})
</script>

<style scoped>
.real-word-preview-container {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
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
  padding: 20px;
  background: white;
}

.word-document-preview {
  max-width: 1000px;
  margin: 0 auto;
}

.document-info {
  margin-bottom: 20px;
}

.document-content {
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  padding: 30px;
  background: white;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  font-family: 'Microsoft YaHei', 'SimSun', sans-serif;
  line-height: 1.6;
}

/* 深度样式：针对Mammoth.js生成的HTML内容 */
:deep(.document-content) h1 {
  font-size: 24px;
  font-weight: bold;
  margin: 20px 0 16px 0;
  color: #333;
  text-align: center;
}

:deep(.document-content) h2 {
  font-size: 20px;
  font-weight: bold;
  margin: 18px 0 14px 0;
  color: #333;
}

:deep(.document-content) h3 {
  font-size: 18px;
  font-weight: bold;
  margin: 16px 0 12px 0;
  color: #333;
}

:deep(.document-content) p {
  margin: 12px 0;
  text-align: justify;
  text-indent: 2em;
  font-size: 16px;
  line-height: 1.8;
}

:deep(.document-content) table {
  width: 100%;
  border-collapse: collapse;
  margin: 16px 0;
  font-size: 14px;
}

:deep(.document-content) table th,
:deep(.document-content) table td {
  border: 1px solid #ddd;
  padding: 8px 12px;
  text-align: left;
  vertical-align: top;
}

:deep(.document-content) table th {
  background-color: #f5f5f5;
  font-weight: bold;
}

:deep(.document-content) ul,
:deep(.document-content) ol {
  margin: 12px 0;
  padding-left: 30px;
}

:deep(.document-content) li {
  margin: 6px 0;
  font-size: 16px;
}

:deep(.document-content) strong {
  font-weight: bold;
  color: #333;
}

:deep(.document-content) em {
  font-style: italic;
}

/* 优化图片显示样式 */
:deep(.document-content) img {
  max-width: 100%;
  height: auto;
  display: block;
  margin: 15px auto;
  border: 1px solid #ddd;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  transition: all 0.3s ease;

  &:hover {
    border-color: #409eff;
    box-shadow: 0 6px 16px rgba(64, 158, 255, 0.3);
    transform: translateY(-2px);
  }

  /* 控制图片最大高度，避免过大图片影响阅读 */
  &:not([height]):not([width]) {
    max-height: 500px;
    object-fit: contain;
  }

  /* 移动端优化 */
  @media (max-width: 768px) {
    &:not([height]):not([width]) {
      max-height: 350px;
    }
    margin: 10px auto;
    border-radius: 6px;
  }
}

.no-data {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 400px;
}

@media (max-width: 768px) {
  .preview-header {
    flex-direction: column;
    gap: 15px;
    text-align: center;
  }

  .preview-content {
    padding: 10px;
  }

  .document-content {
    padding: 20px;
  }

  :deep(.document-content) p {
    font-size: 14px;
  }
}
</style>
