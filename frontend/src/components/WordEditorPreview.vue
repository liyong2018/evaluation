<template>
  <div class="word-editor-preview-container">
    <!-- 工具栏 -->
    <div class="editor-toolbar">
      <div class="toolbar-section">
        <h2 class="editor-title">Word文档编辑器</h2>
      </div>

      <div class="toolbar-section">
        <!-- 格式工具栏 -->
        <div class="format-toolbar">
          <el-button-group size="small">
            <el-button @click="execCommand('bold')" :type="isCommandActive('bold') ? 'primary' : 'default'" icon="Bold" title="加粗 (Ctrl+B)"/>
            <el-button @click="execCommand('italic')" :type="isCommandActive('italic') ? 'primary' : 'default'" icon="Italic" title="斜体 (Ctrl+I)"/>
            <el-button @click="execCommand('underline')" :type="isCommandActive('underline') ? 'primary' : 'default'" icon="Underline" title="下划线 (Ctrl+U)"/>
          </el-button-group>

          <el-select v-model="fontSize" @change="changeFontSize" size="small" style="width: 100px; margin-left: 10px">
            <el-option label="10px" value="10px" />
            <el-option label="12px" value="12px" />
            <el-option label="14px" value="14px" />
            <el-option label="16px" value="16px" />
            <el-option label="18px" value="18px" />
            <el-option label="20px" value="20px" />
            <el-option label="24px" value="24px" />
            <el-option label="28px" value="28px" />
            <el-option label="32px" value="32px" />
          </el-select>

          <el-select v-model="fontFamily" @change="changeFontFamily" size="small" style="width: 120px; margin-left: 10px">
            <el-option label="宋体" value="SimSun" />
            <el-option label="微软雅黑" value="Microsoft YaHei" />
            <el-option label="黑体" value="SimHei" />
            <el-option label="楷体" value="KaiTi" />
            <el-option label="Arial" value="Arial" />
            <el-option label="Times New Roman" value="Times New Roman" />
          </el-select>

          <el-button-group size="small" style="margin-left: 10px">
            <el-button @click="execCommand('justifyLeft')" :type="isCommandActive('justifyLeft') ? 'primary' : 'default'" title="左对齐"/>
            <el-button @click="execCommand('justifyCenter')" :type="isCommandActive('justifyCenter') ? 'primary' : 'default'" title="居中对齐"/>
            <el-button @click="execCommand('justifyRight')" :type="isCommandActive('justifyRight') ? 'primary' : 'default'" title="右对齐"/>
            <el-button @click="execCommand('justifyFull')" :type="isCommandActive('justifyFull') ? 'primary' : 'default'" title="两端对齐"/>
          </el-button-group>

          <!-- 样式快速选择 -->
          <el-button-group size="small">
            <el-button @click="applyTextStyle('cover-main-title')" size="small" title="封面主标题">
              封面主标题
            </el-button>
            <el-button @click="applyTextStyle('cover-sub-title')" size="small" title="封面副标题">
              封面副标题
            </el-button>
            <el-button @click="applyTextStyle('toc-title')" size="small" title="目录标题">
              目录标题
            </el-button>
            <el-button @click="applyTextStyle('chapter-title')" size="small" title="章标题">
              章标题
            </el-button>
          </el-button-group>

          <!-- 背景色选择器 -->
          <div class="toolbar-section">
            <span class="toolbar-label">背景色:</span>
            <el-color-picker v-model="highlightColor" size="small" @change="applyHighlightColor" title="选择背景色"/>
          </div>
        </div>
      </div>

      <div class="toolbar-actions">
        <el-button-group>
          <el-button @click="loadDocument" icon="Refresh" :loading="loading" size="small">
            重新加载
          </el-button>
          <el-button @click="openWithOffice" type="primary" icon="Office" size="small">
            使用本地Office打开
          </el-button>
          <el-button @click="downloadTemplate" icon="Download" size="small">
            下载模板
          </el-button>
          <el-button @click="saveDocument" icon="Document" size="small" :disabled="!hasChanges">
            保存更改
          </el-button>
          <el-button @click="downloadEdited" icon="Download" type="success" size="small" :disabled="!hasChanges">
            下载修改版
          </el-button>
          <el-button @click="downloadOriginal" icon="Download" size="small">
            下载原文档
          </el-button>
          <el-button @click="$emit('close')" icon="Close" size="small">
            关闭
          </el-button>
        </el-button-group>
      </div>
    </div>

    <!-- 编辑器主体 -->
    <div class="editor-main" v-loading="loading">
      <!-- 页面设置 -->
      <div class="page-settings" v-if="!loading">
        <div class="page-info">
          <el-alert
            title="可编辑Word文档"
            type="success"
            :closable="false"
            show-icon
          >
            <template #default>
              <p>您可以直接编辑下方文档内容，修改后会自动标记为已更改状态</p>
              <el-tag v-if="hasChanges" type="warning" size="small">有未保存的更改</el-tag>
            </template>
          </el-alert>
        </div>

        <div class="page-controls">
          <el-form :inline="true" size="small">
            <el-form-item label="页面大小">
              <el-select v-model="pageSize" @change="updatePageLayout" style="width: 120px">
                <el-option label="A4" value="a4" />
                <el-option label="A3" value="a3" />
                <el-option label="Letter" value="letter" />
              </el-select>
            </el-form-item>

            <el-form-item label="缩放">
              <el-slider
                v-model="zoomLevel"
                :min="50"
                :max="150"
                :step="10"
                @change="updateZoom"
                style="width: 100px"
              />
              <span class="zoom-text">{{ zoomLevel }}%</span>
            </el-form-item>
          </el-form>
        </div>
      </div>

      <!-- 文档编辑区域 -->
      <div class="document-container" ref="documentContainer">
        <div
          class="document-page"
          :style="pageStyle"
          contenteditable="true"
          ref="editableContent"
          @input="handleContentChange"
          @paste="handlePaste"
          v-html="documentContent"
        ></div>
      </div>
    </div>

    <!-- 状态栏 -->
    <div class="status-bar">
      <div class="status-info">
        <span v-if="!loading">字数: {{ wordCount }} | 页数: {{ pageCount }}</span>
      </div>
      <div class="status-actions">
        <el-button v-if="hasChanges" @click="saveDocument" type="primary" size="small">
          保存更改
        </el-button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import mammoth from 'mammoth'
import request from '@/utils/request'

// 定义组件事件
const emit = defineEmits(['close'])

// 响应式数据
const loading = ref(false)
const documentContent = ref('')
const originalContent = ref('')
const hasChanges = ref(false)
const pageSize = ref('a4')
const zoomLevel = ref(100)
const wordCount = ref(0)
const pageCount = ref(1)
const fontSize = ref('14px')
const fontFamily = ref('Microsoft YaHei')
const highlightColor = ref('#FFFF00')

// DOM引用
const editableContent = ref<HTMLElement>()
const documentContainer = ref<HTMLElement>()

// 页面样式计算
const pageStyle = computed(() => {
  const sizes = {
    a4: { width: '210mm', height: '297mm' },
    a3: { width: '297mm', height: '420mm' },
    letter: { width: '8.5in', height: '11in' }
  }

  const size = sizes[pageSize.value as keyof typeof sizes] || sizes.a4
  const zoom = zoomLevel.value / 100

  return {
    width: `calc(${size.width} * ${zoom})`,
    minHeight: `calc(${size.height} * ${zoom})`,
    padding: '25mm', // Word标准页面边距
    backgroundColor: 'white',
    boxShadow: '0 0 20px rgba(0, 0, 0, 0.1)',
    borderRadius: '4px',
    fontSize: `${14 * zoom}px`,
    lineHeight: 1.8, // Word标准行距
    // 设置CSS计数器用于标题编号
    counterReset: 'h1-counter 0 h2-counter 0 h3-counter 0 h4-counter 0 h5-counter 0 h6-counter 0',
    fontFamily: '"Microsoft YaHei", "SimSun", "宋体", serif'
  }
})

// 加载文档
const loadDocument = async () => {
  loading.value = true
  try {
    const response = await fetch('/api/word-template/download-template', {
      method: 'GET',
      headers: {
        'Accept': 'application/vnd.openxmlformats-officedocument.wordprocessingml.document'
      }
    })

    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`)
    }

    const arrayBuffer = await response.arrayBuffer()

    // 使用Mammoth.js转换，并保持更多样式信息
    const result = await mammoth.convertToHtml(
      { arrayBuffer },
      {
        styleMap: [
          // 标题样式映射 - 支持多级标题编号
          "p[style-name='Heading 1'] => h1:fresh",
          "p[style-name='标题 1'] => h1:fresh",
          "p[style-name='Heading 2'] => h2:fresh",
          "p[style-name='标题 2'] => h2:fresh",
          "p[style-name='Heading 3'] => h3:fresh",
          "p[style-name='标题 3'] => h3:fresh",
          "p[style-name='Heading 4'] => h4:fresh",
          "p[style-name='标题 4'] => h4:fresh",
          "p[style-name='Heading 5'] => h5:fresh",
          "p[style-name='标题 5'] => h5:fresh",
          "p[style-name='Heading 6'] => h6:fresh",
          "p[style-name='标题 6'] => h6:fresh",
          "p[style-name='Title'] => h1.title:fresh",
          "p[style-name='标题'] => h1.title:fresh",
          "p[style-name='Subtitle'] => h2.subtitle:fresh",
          "p[style-name='副标题'] => h2.subtitle:fresh",

          // 根据样式ID映射（基于实际Word模板）
          "p[style-id='2'] => h1.cover-year:fresh",            // 封面年份（2025年）
          "p[style-id='125'] => h1.toc-title:fresh",          // 目录标题
          "p[style-id='25'] => div.toc-1:fresh",              // 一级目录
          "p[style-id='29'] => div.toc-2:fresh",              // 二级目录
          "p[style-id='4'] => h2.chapter-title:fresh",        // 章标题
          "p[style-id='5'] => h3.section-title:fresh",        // 节标题
          "p[style-id='6'] => h4.subsection-title:fresh",     // 子节标题
          "p[style-id='14'] => h4.table-title:fresh",         // 表标题

          // 封面样式映射 - 基于段落内容和位置
          "p[style-id='0'][text*='四川省眉山市青神县减灾能力评估'] => h1.cover-main-title:fresh",
          "p[style-id='0'][text*='技术报告'] => h1.cover-sub-title:fresh",
          "p[style-id='0'][text*='编制单位'] => p.cover-info:fresh",
          "p[style-id='0'][text*='编制时间'] => p.cover-info:fresh",
          "p[style-id='0'][text*='绿色：自动替换'] => p.cover-note:fresh",

          // 目录样式映射
          "p[style-name='TOC Heading'] => div.toc-heading:fresh",
          "p[style-name='目录标题'] => div.toc-heading:fresh",
          "p[style-name='TOC 1'] => div.toc-1:fresh",
          "p[style-name='目录 1'] => div.toc-1:fresh",
          "p[style-name='TOC 2'] => div.toc-2:fresh",
          "p[style-name='目录 2'] => div.toc-2:fresh",
          "p[style-name='TOC 3'] => div.toc-3:fresh",
          "p[style-name='目录 3'] => div.toc-3:fresh",

          // 正文样式映射
          "p[style-name='Normal'] => p.normal:fresh",
          "p[style-name='正文'] => p.normal:fresh",
          "p[style-name='Body Text'] => p.body-text:fresh",
          "p[style-name='正文文本'] => p.body-text:fresh",
          "p[style-name='List Paragraph'] => p.list-paragraph:fresh",
          "p[style-name='列表段落'] => p.list-paragraph:fresh",

          // 字符样式映射 - 包含背景色
          "r[style-name='Strong'] => strong:fresh",
          "r[style-name='Emphasis'] => em:fresh",
          "r[style-name='Hyperlink'] => a:fresh",
          "r[style-name='链接'] => a:fresh",
          "r[highlight-color='green'] => mark.highlight-green:fresh",
          "r[highlight-color='yellow'] => mark.highlight-yellow:fresh",
          "r[highlight-color='red'] => mark.highlight-red:fresh",
          "r[highlight-color='blue'] => mark.highlight-blue:fresh",

          // 表格样式映射
          "table[style-name='Table Grid'] => table.word-table:fresh",
          "table[style-name='网格表'] => table.word-table:fresh",
          "tr => tr:fresh",
          "td => td:fresh",
          "th => th:fresh",

          // 默认段落映射
          "p => p.fresh"
        ],
        includeDefaultStyleMap: true,
        convertImage: mammoth.images.imgElement(function(image) {
          return image.read("base64").then(function(imageBuffer) {
            // 控制图片最大尺寸，避免图片过大影响显示
            let style = "max-width: 100%; height: auto; display: block; margin: 15px auto;";

            // 添加图片样式
            style += "border: 1px solid #ddd; border-radius: 4px; box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);";

            return {
              src: "data:" + image.contentType + ";base64," + imageBuffer,
              style: style,
              class: "word-image"
            }
          })
        })
      }
    )

    if (result.messages && result.messages.length > 0) {
      console.log('Mammoth转换消息:', result.messages)
    }

    // 处理转换后的HTML内容
    let processedHtml = result.value

    // 添加编辑器特定的样式和属性
    processedHtml = enhanceHtmlForEditing(processedHtml)

    documentContent.value = processedHtml
    originalContent.value = processedHtml
    hasChanges.value = false

    // 延迟更新字数统计
    setTimeout(() => {
      updateWordCount()
    }, 100)

    ElMessage.success('文档加载成功，现在可以编辑内容')
  } catch (error) {
    console.error('加载文档失败:', error)
    ElMessage.error('加载文档失败: ' + (error as Error).message)
  } finally {
    loading.value = false
  }
}

// 增强HTML以支持编辑
const enhanceHtmlForEditing = (html: string): string => {
  return html
    // 处理标题
    .replace(/<h1([^>]*)>/g, '<h1$1 contenteditable="true">')
    .replace(/<h2([^>]*)>/g, '<h2$1 contenteditable="true">')
    .replace(/<h3([^>]*)>/g, '<h3$1 contenteditable="true">')

    // 处理段落
    .replace(/<p([^>]*)>/g, '<p$1 contenteditable="true">')

    // 处理表格
    .replace(/<table([^>]*)>/g, '<table$1 class="editable-table">')
    .replace(/<td([^>]*)>/g, '<td$1 contenteditable="true">')
    .replace(/<th([^>]*)>/g, '<th$1 contenteditable="true">')

    // 处理列表
    .replace(/<ul([^>]*)>/g, '<ul$1 class="editable-list">')
    .replace(/<ol([^>]*)>/g, '<ol$1 class="editable-list">')
    .replace(/<li([^>]*)>/g, '<li$1 contenteditable="true">')

    // 确保图片有合理的大小限制
    .replace(/<img([^>]*)>/g, '<img$1 style="max-width: 100%; height: auto;" />')
}

// 处理内容变化
const handleContentChange = () => {
  hasChanges.value = true
  updateWordCount()
}

// 处理粘贴事件
const handlePaste = (event: ClipboardEvent) => {
  event.preventDefault()

  const clipboardData = event.clipboardData
  if (!clipboardData) return

  // 获取纯文本
  const text = clipboardData.getData('text/plain')

  // 获取HTML内容
  const html = clipboardData.getData('text/html')

  if (html && html.trim()) {
    // 清理HTML内容，移除可能的有害标签
    const cleanHtml = cleanPastedHtml(html)
    document.execCommand('insertHTML', false, cleanHtml)
  } else if (text) {
    // 插入纯文本
    document.execCommand('insertText', false, text)
  }

  hasChanges.value = true
}

// 清理粘贴的HTML
const cleanPastedHtml = (html: string): string => {
  // 创建临时DOM元素来清理HTML
  const tempDiv = document.createElement('div')
  tempDiv.innerHTML = html

  // 移除脚本和样式标签
  const scripts = tempDiv.querySelectorAll('script, style')
  scripts.forEach(el => el.remove())

  // 保留基本格式化标签
  const allowedTags = ['p', 'br', 'strong', 'em', 'u', 'i', 'b', 'ul', 'ol', 'li', 'table', 'tr', 'td', 'th', 'h1', 'h2', 'h3', 'h4', 'h5', 'h6']

  // 递归清理元素
  const cleanElement = (element: Element) => {
    const tagName = element.tagName.toLowerCase()
    if (!allowedTags.includes(tagName) && tagName !== 'div' && tagName !== 'span') {
      // 将不允许的标签替换为div
      const newDiv = document.createElement('div')
      newDiv.innerHTML = element.innerHTML
      element.parentNode?.replaceChild(newDiv, element)
      return newDiv
    }

    // 递归处理子元素
    Array.from(element.children).forEach(cleanElement)
    return element
  }

  Array.from(tempDiv.children).forEach(cleanElement)

  return tempDiv.innerHTML
}

// 更新字数统计
const updateWordCount = () => {
  if (!editableContent.value) return

  const text = editableContent.value.innerText || ''
  wordCount.value = text.length

  // 简单估算页数（假设每页约500字）
  pageCount.value = Math.max(1, Math.ceil(wordCount.value / 500))
}

// 保存文档
const saveDocument = async () => {
  if (!hasChanges.value) {
    ElMessage.info('没有需要保存的更改')
    return
  }

  let loadingMessage: { close: () => void } | null = null
  try {
    await ElMessageBox.confirm(
      '确定要保存当前更改吗？这将更新Word模板内容。',
      '确认保存',
      {
        confirmButtonText: '保存',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    loadingMessage = ElMessage({ message: '正在保存文档，请稍候...', type: 'info', duration: 0 })

    // 获取编辑器中的HTML内容
    const editedContent = editableContent.value?.innerHTML || ''

    // 调用后端API保存
    const response = await request.post('/api/word-template/save-edited-content', {
      htmlContent: editedContent
    })

    if (response.success) {
      originalContent.value = documentContent.value
      hasChanges.value = false
      ElMessage.success('文档保存成功！')
    } else {
      throw new Error(response.message || '保存失败')
    }
  } catch (error: any) {
    if (error.message !== 'cancel') {
      console.error('保存文档失败:', error)
      ElMessage.error('保存文档失败: ' + (error.message || '未知错误'))
    }
  } finally {
    loadingMessage?.close()
  }
}

// 下载原文档
const downloadOriginal = async () => {
  let loadingMessage: { close: () => void } | null = null
  try {
    loadingMessage = ElMessage({ message: '正在生成文档，请稍候...', type: 'info', duration: 0 })

    // 使用fetch获取Word文档
    const response = await fetch('/api/word-template/download-template', {
      method: 'GET',
      headers: {
        'Accept': 'application/vnd.openxmlformats-officedocument.wordprocessingml.document'
      }
    })

    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`)
    }

    const arrayBuffer = await response.arrayBuffer()
    const blob = new Blob([arrayBuffer], {
      type: 'application/vnd.openxmlformats-officedocument.wordprocessingml.document'
    })

    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url

    const currentDate = new Date()
    const fileName = `青神县减灾能力评估技术报告_${currentDate.getFullYear()}${String(currentDate.getMonth() + 1).padStart(2, '0')}${String(currentDate.getDate()).padStart(2, '0')}.docx`

    link.download = fileName
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    window.URL.revokeObjectURL(url)

    ElMessage.success('文档生成成功！')
  } catch (error) {
    console.error('下载文档失败:', error)
    ElMessage.error('生成文档失败，请重试')
  } finally {
    loadingMessage?.close()
  }
}

// 更新页面布局
const updatePageLayout = () => {
  // 页面布局会通过计算属性自动更新
}

// 更新缩放
const updateZoom = () => {
  // 缩放会通过计算属性自动更新
}

// 执行格式化命令
const execCommand = (command: string, value?: string) => {
  if (editableContent.value) {
    editableContent.value.focus()
    document.execCommand(command, false, value ?? undefined)
    handleContentChange()
  }
}

// 检查命令是否激活
const isCommandActive = (command: string): boolean => {
  if (editableContent.value) {
    return document.queryCommandState(command)
  }
  return false
}

// 应用文本样式
const applyTextStyle = (styleType: string) => {
  if (editableContent.value) {
    const selection = window.getSelection()
    if (selection && selection.rangeCount > 0) {
      const range = selection.getRangeAt(0)
      const selectedText = range.toString()

      if (selectedText) {
        // 创建带样式的元素
        const styledElement = document.createElement('span')
        styledElement.className = styleType
        styledElement.style.fontSize = getStyleFontSize(styleType)
        styledElement.style.fontFamily = getStyleFontFamily(styleType)
        styledElement.style.fontWeight = getStyleFontWeight(styleType)
        styledElement.style.textAlign = getStyleTextAlign(styleType)
        styledElement.style.display = 'block'
        styledElement.style.margin = getStyleMargin(styleType)

        // 替换选中的内容
        range.deleteContents()
        range.insertNode(styledElement)
        styledElement.textContent = selectedText

        // 清除选择
        selection.removeAllRanges()

        handleContentChange()
      }
    }
  }
}

// 获取样式字体大小
const getStyleFontSize = (styleType: string): string => {
  const sizes: { [key: string]: string } = {
    'cover-main-title': '32pt',
    'cover-sub-title': '28pt',
    'toc-title': '22pt',
    'chapter-title': '18pt'
  }
  return sizes[styleType] || '14pt'
}

// 获取样式字体系列
const getStyleFontFamily = (styleType: string): string => {
  const families: { [key: string]: string } = {
    'cover-main-title': '"SimHei", "黑体", "Microsoft YaHei", sans-serif',
    'cover-sub-title': '"SimHei", "黑体", "Microsoft YaHei", sans-serif',
    'toc-title': '"Microsoft YaHei", "SimHei", sans-serif',
    'chapter-title': '"Microsoft YaHei", "SimHei", sans-serif'
  }
  return families[styleType] || '"Microsoft YaHei", sans-serif'
}

// 获取样式字体粗细
const getStyleFontWeight = (styleType: string): string => {
  return 'bold'
}

// 获取样式文本对齐
const getStyleTextAlign = (styleType: string): string => {
  return 'center'
}

// 获取样式边距
const getStyleMargin = (styleType: string): string => {
  const margins: { [key: string]: string } = {
    'cover-main-title': '50pt 0 15pt 0',
    'cover-sub-title': '20pt 0 15pt 0',
    'toc-title': '30pt 0 20pt 0',
    'chapter-title': '24pt 0 16pt 0'
  }
  return margins[styleType] || '12pt 0'
}

// 应用背景色
const applyHighlightColor = (color: string) => {
  if (editableContent.value) {
    const selection = window.getSelection()
    if (selection && selection.rangeCount > 0) {
      const range = selection.getRangeAt(0)
      const selectedText = range.toString()

      if (selectedText) {
        // 创建带背景色的元素
        const highlightElement = document.createElement('mark')
        highlightElement.style.backgroundColor = color
        highlightElement.style.padding = '2px 4px'
        highlightElement.style.borderRadius = '2px'

        // 替换选中的内容
        range.deleteContents()
        range.insertNode(highlightElement)
        highlightElement.textContent = selectedText

        // 清除选择
        selection.removeAllRanges()

        handleContentChange()
      }
    }
  }
}

// 改变字体大小
const changeFontSize = () => {
  if (editableContent.value) {
    const selection = window.getSelection()
    if (selection && selection.rangeCount > 0) {
      const range = selection.getRangeAt(0)
      if (!range.collapsed) {
        execCommand('fontSize', '7') // 先设置为任意值
        const spans = editableContent.value.querySelectorAll('font[size="7"]')
        spans.forEach((span) => {
          span.removeAttribute('size')
          ;(span as HTMLElement).style.fontSize = fontSize.value
        })
      } else {
        // 如果没有选中文本，设置当前段落的字体大小
        const currentElement = selection.focusNode?.parentElement
        if (currentElement) {
          currentElement.style.fontSize = fontSize.value
          handleContentChange()
        }
      }
    }
  }
}

// 改变字体
const changeFontFamily = () => {
  if (editableContent.value) {
    const selection = window.getSelection()
    if (selection && selection.rangeCount > 0) {
      const range = selection.getRangeAt(0)
      if (!range.collapsed) {
        execCommand('fontName', fontFamily.value)
      } else {
        // 如果没有选中文本，设置当前段落的字体
        const currentElement = selection.focusNode?.parentElement
        if (currentElement) {
          currentElement.style.fontFamily = fontFamily.value
          handleContentChange()
        }
      }
    }
  }
}

// 使用本地Office打开文档
const openWithOffice = async () => {
  try {
    // 下载Word模板文件
    const response = await fetch('/api/word-template/download-template')
    if (!response.ok) {
      throw new Error('获取模板文件失败')
    }

    const blob = await response.blob()
    const fileUrl = URL.createObjectURL(blob)

    // 创建临时文件下载
    const link = document.createElement('a')
    link.href = fileUrl
    link.download = '减灾能力评估技术报告.docx'
    link.click()

    // 尝试使用Office协议直接打开
    setTimeout(() => {
      // Microsoft Office协议
      try {
        window.location.href = `ms-word:ofe|u|${encodeURIComponent(fileUrl)}`
      } catch (e) {
        console.log('MS Office协议不可用')
      }

      // WPS协议
      try {
        window.location.href = `wps://open|path|${encodeURIComponent(link.href)}`
      } catch (e) {
        console.log('WPS协议不可用')
      }
    }, 1000)

    ElMessage.success('正在启动Office，请稍候...')
    URL.revokeObjectURL(fileUrl)

  } catch (error) {
    console.error('启动Office失败:', error)
    const message = error instanceof Error ? error.message : String(error)
    ElMessage.error('启动Office失败: ' + message)
  }
}

// 下载模板文件
const downloadTemplate = async () => {
  try {
    const response = await fetch('/api/word-template/download-template')
    if (!response.ok) {
      throw new Error('获取模板文件失败')
    }

    const blob = await response.blob()
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = `四川省眉山市青神县减灾能力评估技术报告-系统模板.docx`
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    window.URL.revokeObjectURL(url)

    ElMessage.success('模板下载成功')
  } catch (error) {
    console.error('下载模板失败:', error)
    const message = error instanceof Error ? error.message : String(error)
    ElMessage.error('下载模板失败: ' + message)
  }
}

// 下载修改后的Word文档
const downloadEdited = async () => {
  let loadingMessage: { close: () => void } | null = null
  try {
    if (!hasChanges.value) {
      ElMessage.info('没有修改内容需要下载')
      return
    }

    await ElMessageBox.confirm(
      '确定要下载修改后的Word文档吗？',
      '确认下载',
      {
        confirmButtonText: '下载',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    loadingMessage = ElMessage({ message: '正在生成修改后的Word文档，请稍候...', type: 'info', duration: 0 })

    // 获取编辑器中的HTML内容
    const editedContent = editableContent.value?.innerHTML || ''

    // 调用后端API生成修改后的文档
    const response = await fetch('/api/word-template/download-edited', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({
        htmlContent: editedContent
      })
    })

    if (!response.ok) {
      const errorText = await response.text();
      console.error('下载请求失败:', response.status, errorText);
      throw new Error(`服务器返回错误: ${response.status} ${errorText || ''}`);
    }

    // 检查响应内容大小
    const contentLength = response.headers.get('content-length');
    if (contentLength && parseInt(contentLength) === 0) {
      throw new Error('生成的文档内容为空');
    }

    const arrayBuffer = await response.arrayBuffer()
    if (arrayBuffer.byteLength === 0) {
      throw new Error('下载的文件内容为空');
    }

    const blob = new Blob([arrayBuffer], {
      type: 'application/vnd.openxmlformats-officedocument.wordprocessingml.document'
    })

    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url

    const currentDate = new Date()
    const fileName = `青神县减灾能力评估技术报告_修改版_${currentDate.getFullYear()}${String(currentDate.getMonth() + 1).padStart(2, '0')}${String(currentDate.getDate()).padStart(2, '0')}.docx`

    link.download = fileName
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    window.URL.revokeObjectURL(url)

    ElMessage.success('修改后的Word文档下载成功！')
  } catch (error: any) {
    if (error.message !== 'cancel') {
      console.error('下载修改后的Word文档失败:', error)
      ElMessage.error('生成修改后的Word文档失败: ' + (error.message || '未知错误'))
    }
  } finally {
    loadingMessage?.close()
  }
}

// 组件挂载时加载数据
onMounted(() => {
  loadDocument()

  // 添加键盘快捷键
  document.addEventListener('keydown', handleKeydown)
})

// 处理键盘快捷键
const handleKeydown = (event: KeyboardEvent) => {
  if (event.ctrlKey || event.metaKey) {
    switch (event.key) {
      case 'b':
        event.preventDefault()
        execCommand('bold')
        break
      case 'i':
        event.preventDefault()
        execCommand('italic')
        break
      case 'u':
        event.preventDefault()
        execCommand('underline')
        break
      case 's':
        event.preventDefault()
        saveDocument()
        break
    }
  }
}
</script>

<style scoped>
.word-editor-preview-container {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: #f5f5f5;
  z-index: 9999;
  display: flex;
  flex-direction: column;
}

/* 工具栏样式 */
.editor-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 20px;
  background: white;
  border-bottom: 1px solid #e0e0e0;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.toolbar-section {
  display: flex;
  align-items: center;
  gap: 15px;
  flex-wrap: wrap;
}

.toolbar-label {
  font-size: 12px;
  color: #666;
  white-space: nowrap;
  margin-right: 5px;
}

.format-toolbar {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 0;
  border-top: 1px solid #e0e0e0;
  border-bottom: 1px solid #e0e0e0;
}

.editor-title {
  margin: 0;
  color: #333;
  font-size: 18px;
  font-weight: 600;
}

.toolbar-actions {
  display: flex;
  gap: 10px;
}

/* 编辑器主体 */
.editor-main {
  flex: 1;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

.page-settings {
  padding: 15px 20px;
  background: white;
  border-bottom: 1px solid #e0e0e0;
}

.page-info {
  margin-bottom: 15px;
}

.page-controls {
  display: flex;
  align-items: center;
  gap: 20px;
}

.zoom-text {
  margin-left: 10px;
  font-size: 14px;
  color: #666;
}

/* 文档容器 */
.document-container {
  flex: 1;
  overflow: auto;
  padding: 20px;
  display: flex;
  justify-content: center;
}

.document-page {
  position: relative;
  background: white;
  border: 1px solid #ddd;
  transition: all 0.3s ease;
  outline: none;
}

.document-page:focus {
  border-color: #409eff;
  box-shadow: 0 0 0 2px rgba(64, 158, 255, 0.2);
}

/* 编辑器内容样式 - 优化Word文档格式 */
.document-page :deep(h1) {
  font-size: 22pt;
  font-weight: bold;
  margin: 24pt 0 18pt 0;
  color: #000000;
  text-align: center;
  line-height: 1.5;
  font-family: "Microsoft YaHei", "SimHei", sans-serif;
}

.document-page :deep(h1::before) {
  content: counter(h1-counter) "、 ";
  counter-increment: h1-counter;
  font-weight: bold;
}

.document-page :deep(h2) {
  font-size: 18pt;
  font-weight: bold;
  margin: 20pt 0 16pt 0;
  color: #000000;
  line-height: 1.4;
  font-family: "Microsoft YaHei", "SimHei", sans-serif;
  text-indent: 0;
}

.document-page :deep(h2::before) {
  content: counter(h1-counter) "." counter(h2-counter) " ";
  counter-increment: h2-counter;
  font-weight: bold;
}

.document-page :deep(h3) {
  font-size: 16pt;
  font-weight: bold;
  margin: 16pt 0 12pt 0;
  color: #000000;
  line-height: 1.3;
  font-family: "Microsoft YaHei", "SimHei", sans-serif;
  text-indent: 0;
}

.document-page :deep(h3::before) {
  content: counter(h1-counter) "." counter(h2-counter) "." counter(h3-counter) " ";
  counter-increment: h3-counter;
  font-weight: bold;
}

.document-page :deep(h4) {
  font-size: 14pt;
  font-weight: bold;
  margin: 14pt 0 10pt 0;
  color: #000000;
  line-height: 1.3;
  font-family: "Microsoft YaHei", "SimHei", sans-serif;
  text-indent: 0;
}

.document-page :deep(h4::before) {
  content: counter(h1-counter) "." counter(h2-counter) "." counter(h3-counter) "." counter(h4-counter) " ";
  counter-increment: h4-counter;
  font-weight: bold;
}

.document-page :deep(h5) {
  font-size: 12pt;
  font-weight: bold;
  margin: 12pt 0 8pt 0;
  color: #000000;
  line-height: 1.2;
  font-family: "Microsoft YaHei", "SimHei", sans-serif;
  text-indent: 0;
}

.document-page :deep(h5::before) {
  content: "(" counter(h1-counter) "." counter(h2-counter) "." counter(h3-counter) "." counter(h4-counter) "." counter(h5-counter) ") ";
  counter-increment: h5-counter;
  font-weight: bold;
}

.document-page :deep(h6) {
  font-size: 10pt;
  font-weight: bold;
  margin: 10pt 0 6pt 0;
  color: #000000;
  line-height: 1.2;
  font-family: "Microsoft YaHei", "SimHei", sans-serif;
  text-indent: 0;
}

/* 封面标题样式 */
.document-page :deep(h1.cover-main-title) {
  font-size: 32pt;
  font-weight: bold;
  text-align: center;
  margin: 50pt 0 15pt 0;
  color: #000000;
  font-family: "SimHei", "黑体", "Microsoft YaHei", sans-serif;
  letter-spacing: 3pt;
  line-height: 1.3;
}

.document-page :deep(h1.cover-sub-title) {
  font-size: 28pt;
  font-weight: bold;
  text-align: center;
  margin: 20pt 0 15pt 0;
  color: #000000;
  font-family: "SimHei", "黑体", "Microsoft YaHei", sans-serif;
  letter-spacing: 2pt;
}

.document-page :deep(h1.cover-year) {
  font-size: 20pt;
  font-weight: bold;
  text-align: center;
  margin: 15pt 0 30pt 0;
  color: #000000;
  font-family: "SimSun", "宋体", serif;
  letter-spacing: 1pt;
}

.document-page :deep(p.cover-note) {
  font-size: 12pt;
  text-align: center;
  margin: 20pt 0 40pt 0;
  color: #666666;
  font-family: "SimSun", "宋体", serif;
  border: 1px dashed #ccc;
  padding: 10pt;
  background-color: #f9f9f9;
}

.document-page :deep(p.cover-info) {
  font-size: 14pt;
  text-align: left;
  margin: 10pt 0;
  color: #000000;
  font-family: "SimSun", "宋体", serif;
  text-indent: 2em;
  line-height: 1.8;
}

.document-page :deep(h1.cover-title) {
  font-size: 28pt;
  font-weight: bold;
  text-align: center;
  margin: 30pt 0 20pt 0;
  color: #000000;
  font-family: "SimHei", "黑体", "Microsoft YaHei", sans-serif;
  letter-spacing: 2pt;
}

/* 目录标题样式 */
.document-page :deep(h1.toc-title) {
  font-size: 22pt;
  font-weight: bold;
  text-align: center;
  margin: 30pt 0 20pt 0;
  color: #000000;
  font-family: "Microsoft YaHei", "SimHei", sans-serif;
  text-indent: 0;
}

.document-page :deep(h1.toc-title::before) {
  content: "目  录";
}

/* 目录样式 */
.document-page :deep(.toc-heading) {
  font-size: 16pt;
  font-weight: bold;
  text-align: center;
  margin: 20pt 0 15pt 0;
  color: #000000;
  font-family: "Microsoft YaHei", "SimHei", sans-serif;
}

.document-page :deep(.toc-heading::before) {
  content: "目  录";
}

.document-page :deep(.toc-1) {
  font-size: 12pt;
  margin: 8pt 0 6pt 0;
  color: #000000;
  text-align: left;
  font-family: "SimSun", "宋体", serif;
  text-indent: 0;
  line-height: 1.8;
}

.document-page :deep(.toc-1::before) {
  content: attr(data-level) "、 ";
  font-weight: bold;
}

.document-page :deep(.toc-2) {
  font-size: 11pt;
  margin: 6pt 0 4pt 0;
  color: #000000;
  text-align: left;
  font-family: "SimSun", "宋体", serif;
  text-indent: 2em;
  line-height: 1.6;
}

.document-page :deep(.toc-2::before) {
  content: attr(data-level) ". ";
  font-weight: normal;
}

.document-page :deep(.toc-3) {
  font-size: 10pt;
  margin: 4pt 0 2pt 0;
  color: #000000;
  text-align: left;
  font-family: "SimSun", "宋体", serif;
  text-indent: 4em;
  line-height: 1.5;
}

.document-page :deep(.toc-3::before) {
  content: attr(data-level) ". ";
  font-weight: normal;
}

/* 章节标题样式 */
.document-page :deep(h2.chapter-title) {
  font-size: 18pt;
  font-weight: bold;
  margin: 24pt 0 16pt 0;
  color: #000000;
  font-family: "Microsoft YaHei", "SimHei", sans-serif;
  text-align: center;
  text-indent: 0;
  line-height: 1.4;
}

.document-page :deep(h3.section-title) {
  font-size: 14pt;
  font-weight: bold;
  margin: 18pt 0 12pt 0;
  color: #000000;
  font-family: "Microsoft YaHei", "SimHei", sans-serif;
  text-indent: 0;
  line-height: 1.4;
}

.document-page :deep(h4.subsection-title) {
  font-size: 12pt;
  font-weight: bold;
  margin: 14pt 0 8pt 0;
  color: #000000;
  font-family: "Microsoft YaHei", "SimHei", sans-serif;
  text-indent: 0;
  line-height: 1.3;
}

.document-page :deep(h4.table-title) {
  font-size: 12pt;
  font-weight: bold;
  margin: 16pt 0 8pt 0;
  color: #000000;
  font-family: "Microsoft YaHei", "SimHei", sans-serif;
  text-align: center;
  text-indent: 0;
}

/* 背景高亮样式 */
.document-page :deep(mark.highlight-green) {
  background-color: #90EE90 !important;
  color: #000000;
  padding: 2px 4px;
  border-radius: 2px;
}

.document-page :deep(mark.highlight-yellow) {
  background-color: #FFFF00 !important;
  color: #000000;
  padding: 2px 4px;
  border-radius: 2px;
}

.document-page :deep(mark.highlight-red) {
  background-color: #FFB6C1 !important;
  color: #000000;
  padding: 2px 4px;
  border-radius: 2px;
}

.document-page :deep(mark.highlight-blue) {
  background-color: #ADD8E6 !important;
  color: #000000;
  padding: 2px 4px;
  border-radius: 2px;
}

/* 特殊内容样式 */
.document-page :deep(.cover-info) {
  text-align: center;
  margin: 20pt 0;
  font-family: "SimSun", "宋体", serif;
  font-size: 12pt;
}

.document-page :deep(.cover-separator) {
  text-align: center;
  margin: 15pt 0;
  font-size: 10pt;
  color: #666666;
  font-style: italic;
}

/* 用户自定义样式 - 支持手动调整 */
.document-page :deep(.cover-main-title) {
  font-size: 32pt;
  font-weight: bold;
  text-align: center;
  margin: 50pt 0 15pt 0;
  color: #000000;
  font-family: "SimHei", "黑体", "Microsoft YaHei", sans-serif;
  letter-spacing: 3pt;
  line-height: 1.3;
}

.document-page :deep(.cover-sub-title) {
  font-size: 28pt;
  font-weight: bold;
  text-align: center;
  margin: 20pt 0 15pt 0;
  color: #000000;
  font-family: "SimHei", "黑体", "Microsoft YaHei", sans-serif;
  letter-spacing: 2pt;
}

.document-page :deep(.toc-title) {
  font-size: 22pt;
  font-weight: bold;
  text-align: center;
  margin: 30pt 0 20pt 0;
  color: #000000;
  font-family: "Microsoft YaHei", "SimHei", sans-serif;
}

.document-page :deep(.chapter-title) {
  font-size: 18pt;
  font-weight: bold;
  text-align: center;
  margin: 24pt 0 16pt 0;
  color: #000000;
  font-family: "Microsoft YaHei", "SimHei", sans-serif;
}

.document-page :deep(p) {
  margin: 12pt 0;
  text-align: justify;
  line-height: 1.8; /* Word标准行距：1.8倍行距 */
  font-size: 12pt; /* Word标准正文字号 */
  font-family: "SimSun", "宋体", serif;
  text-indent: 2em; /* 首行缩进2个字符 */
  min-height: 1.8em;
  word-spacing: 0.05em; /* 字间距 */
  letter-spacing: 0.01em;
}

.document-page :deep(p:first-child),
.document-page :deep(p.title),
.document-page :deep(p.normal:first-child) {
  text-indent: 0;
}

/* 表格样式优化 */
.document-page :deep(table.word-table) {
  width: 100%;
  border-collapse: collapse;
  margin: 16pt 0;
  font-size: 11pt;
  border: 1pt solid #000;
}

.document-page :deep(table.word-table th),
.document-page :deep(table.word-table td) {
  border: 1pt solid #000;
  padding: 6pt 8pt;
  text-align: center;
  vertical-align: middle;
  font-family: "SimSun", "宋体", serif;
  line-height: 1.5;
}

.document-page :deep(table.word-table th) {
  background-color: #f5f5f5;
  font-weight: bold;
}

/* 特殊段落样式 */
.document-page :deep(p.title) {
  font-size: 18pt;
  font-weight: bold;
  text-align: center;
  margin: 24pt 0 18pt 0;
  font-family: "Microsoft YaHei", "SimHei", sans-serif;
}

.document-page :deep(p.normal) {
  font-size: 10.5pt;
  text-align: justify;
  line-height: 1.8;
  font-family: "SimSun", "宋体", serif;
  text-indent: 2em;
  margin: 12pt 0;
}

.document-page :deep(p.body-text) {
  font-size: 10.5pt;
  line-height: 1.8;
  font-family: "SimSun", "宋体", serif;
  text-indent: 2em;
  margin: 12pt 0;
}

.document-page :deep(p.list-paragraph) {
  font-size: 10.5pt;
  line-height: 1.5;
  font-family: "SimSun", "宋体", serif;
  margin: 6pt 0;
  text-indent: 0;
}

.document-page :deep(.editable-table) {
  width: 100%;
  border-collapse: collapse;
  margin: 16px 0;
  font-size: 14px;
  border: 1px solid #ddd;
}

.document-page :deep(.editable-table th),
.document-page :deep(.editable-table td) {
  border: 1px solid #ddd;
  padding: 8px 12px;
  text-align: left;
  vertical-align: top;
}

.document-page :deep(.editable-table th) {
  background-color: #f5f5f5;
  font-weight: bold;
}

.document-page :deep(.editable-list) {
  margin: 12px 0;
  padding-left: 30px;
}

.document-page :deep(.editable-list li) {
  margin: 6px 0;
  font-size: 16px;
  line-height: 1.6;
}

.document-page :deep(strong) {
  font-weight: bold;
  color: #333;
}

.document-page :deep(em) {
  font-style: italic;
}

.document-page :deep(img) {
  max-width: 100%;
  height: auto;
  display: block;
  margin: 10px auto;
  border: 1px solid #ddd;
  border-radius: 4px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;

  &:hover {
    border-color: #409eff;
    box-shadow: 0 4px 12px rgba(64, 158, 255, 0.2);
    transform: scale(1.02);
  }

  /* 确保图片不会太大，影响页面布局 */
  &:not([height]):not([width]) {
    max-height: 400px;
    object-fit: contain;
  }
}

/* 编辑状态指示 */
.document-page[contenteditable="true"] {
  cursor: text;
}

.document-page[contenteditable="true"]:focus {
  background: #fafafa;
}

/* 状态栏 */
.status-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 20px;
  background: white;
  border-top: 1px solid #e0e0e0;
  font-size: 12px;
  color: #666;
}

.status-info {
  display: flex;
  gap: 15px;
}

.status-actions {
  display: flex;
  gap: 10px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .editor-toolbar {
    flex-direction: column;
    gap: 10px;
    padding: 10px;
  }

  .page-settings {
    padding: 10px;
  }

  .page-controls {
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;
  }

  .document-container {
    padding: 10px;
  }

  .status-bar {
    flex-direction: column;
    gap: 10px;
    padding: 10px;
  }
}

/* 滚动条样式 */
.document-container::-webkit-scrollbar {
  width: 8px;
  height: 8px;
}

.document-container::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 4px;
}

.document-container::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 4px;
}

.document-container::-webkit-scrollbar-thumb:hover {
  background: #a8a8a8;
}
</style>
