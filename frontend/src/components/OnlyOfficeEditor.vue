<template>
  <div v-if="loadError" class="onlyoffice-fallback">
    <div class="onlyoffice-fallback-title">在线预览不可用</div>
    <div class="onlyoffice-fallback-desc">{{ loadError }}</div>
    <div class="onlyoffice-fallback-actions">
      <a :href="documentUrl" target="_blank" rel="noopener noreferrer">下载/打开报告文件</a>
    </div>
  </div>
  <div v-else id="onlyoffice-editor-container" ref="editorContainer"></div>
</template>

<script setup lang="ts">
import { onMounted, onBeforeUnmount, ref, watch } from 'vue'

const props = defineProps<{
  documentUrl: string
  documentTitle: string
  documentKey?: string
  config?: any
}>()

const editorContainer = ref<HTMLElement | null>(null)
let docEditor: any = null
const loadError = ref<string | null>(null)

const isLocalhostLike = (hostname: string) => {
  return hostname === 'localhost' || hostname === '127.0.0.1' || hostname === '::1'
}

const getOnlyOfficeApiUrl = (): string => {
  const envUrl = import.meta.env.VITE_ONLYOFFICE_API_URL
  if (envUrl) {
    try {
      const u = new URL(envUrl, window.location.origin)
      if (isLocalhostLike(u.hostname)) {
        u.hostname = window.location.hostname
      }
      return u.toString()
    } catch {
      return envUrl
    }
  }
  return new URL('/documentserver/web-apps/apps/api/documents/api.js', window.location.origin).toString()
}

const loadScript = (src: string): Promise<void> => {
  return new Promise((resolve, reject) => {
    if ((window as any).DocsAPI) {
      resolve()
      return
    }
    const script = document.createElement('script')
    script.src = src
    script.type = 'text/javascript'
    script.onload = () => resolve()
    script.onerror = (e) => reject(e)
    document.head.appendChild(script)
  })
}

const initEditor = async () => {
  if (!props.documentUrl) return

  try {
    loadError.value = null
    await loadScript(getOnlyOfficeApiUrl())

    if (docEditor) {
      docEditor.destroyEditor()
      docEditor = null
    }

    const defaultConfig = {
        document: {
          fileType: "docx",
          key: props.documentKey || new Date().getTime().toString(),
          title: props.documentTitle,
          url: props.documentUrl,
          permissions: {
            edit: true, // 允许编辑
            download: true,
            print: true,
            review: true, // 允许修订
            chat: true // 允许聊天（移到这里）
          }
        },
        documentType: "word",
        editorConfig: {
          mode: "edit", // 编辑模式
          lang: "zh-CN",
          customization: {
            zoom: 100,
            autosave: true, // 开启自动保存
            forcesave: true, // 开启强制保存
            comments: true, // 允许批注
            help: false
          },
          callbackUrl: (() => {
            try {
              const u = new URL(props.documentUrl)
              u.pathname = u.pathname.replace(/\/generate-report$/, '/callback')
              u.search = ''
              u.hash = ''
              return u.toString()
            } catch {
              return undefined
            }
          })()
        },
      height: "100%",
      width: "100%",
      type: "desktop" // 桌面模式，非移动端
    }

    // 合并传入的配置
    const config = props.config ? { ...defaultConfig, ...props.config } : defaultConfig
    // 确保URL和Key正确
    config.document.url = props.documentUrl
    config.document.title = props.documentTitle
    if (props.documentKey) {
        config.document.key = props.documentKey
    }

    // @ts-expect-error DocsAPI is injected by runtime script
    if (typeof DocsAPI !== 'undefined') {
        // @ts-expect-error DocsAPI is injected by runtime script
        docEditor = new DocsAPI.DocEditor("onlyoffice-editor-container", config)
    } else {
        console.error('DocsAPI is not defined after script load')
        loadError.value = 'OnlyOffice API 加载完成但未注入 DocsAPI'
    }

  } catch (error) {
    console.error('Failed to load OnlyOffice API:', error)
    const apiUrl = getOnlyOfficeApiUrl()
    loadError.value = `OnlyOffice DocumentServer 不可达或未配置：${apiUrl}`
  }
}

onMounted(() => {
  initEditor()
})

onBeforeUnmount(() => {
  if (docEditor) {
    docEditor.destroyEditor()
    docEditor = null
  }
})

watch(() => props.documentUrl, () => {
  initEditor()
})
</script>

<style scoped>
#onlyoffice-editor-container {
  width: 100%;
  height: 100%;
  min-height: 600px; /* 确保有最小高度 */
  background: #f4f4f4;
}

.onlyoffice-fallback {
  width: 100%;
  min-height: 600px;
  background: #f4f4f4;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  gap: 12px;
  padding: 24px;
  box-sizing: border-box;
}

.onlyoffice-fallback-title {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
}

.onlyoffice-fallback-desc {
  max-width: 860px;
  word-break: break-all;
  text-align: center;
  color: #606266;
  font-size: 14px;
  line-height: 20px;
}

.onlyoffice-fallback-actions a {
  color: #409eff;
  text-decoration: none;
}

.onlyoffice-fallback-actions a:hover {
  text-decoration: underline;
}
</style>
