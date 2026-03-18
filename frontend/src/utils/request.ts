import axios, { type AxiosError, type AxiosResponse, type InternalAxiosRequestConfig } from 'axios'
import { ElMessage } from 'element-plus'

// 创建axios实例
const baseURL = (import.meta as any)?.env?.VITE_API_BASE_URL || ''
const DEFAULT_TIMEOUT = Number((import.meta as any)?.env?.VITE_API_TIMEOUT) || 60000

const request: any = axios.create({
  baseURL,
  timeout: DEFAULT_TIMEOUT, // 请求超时时间（默认60秒，可配置）
  headers: {
    'Content-Type': 'application/json'
  }
})

// 请求拦截器
request.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    // 在发送请求之前做些什么
    const userInfoStr = localStorage.getItem('userInfo')
    if (userInfoStr) {
      try {
        const userInfo = JSON.parse(userInfoStr)
        if (userInfo && userInfo.username) {
          config.headers['X-Current-User'] = userInfo.username
        }
      } catch (e) {
        console.error('解析用户信息失败', e)
      }
    }
    return config
  },
  (error: AxiosError) => {
    // 对请求错误做些什么
    return Promise.reject(error)
  }
)

// 响应拦截器
request.interceptors.response.use(
  (response: AxiosResponse) => {
    // 对响应数据做点什么
    const { data } = response

    // 如果是文件下载等特殊响应，返回完整response对象以便获取响应头中的文件名
    if (response.config.responseType === 'blob') {
      return { data, headers: response.headers }
    }

    // 统一处理后端返回的Result格式
    if (data && typeof data === 'object') {
      if (data.success === false) {
        ElMessage.error(data.message || '请求失败')
        return Promise.reject(new Error(data.message || '请求失败'))
      }
      return data
    }

    return data
  },
  (error: AxiosError) => {
    // 对响应错误做点什么
    console.error('请求错误:', error)
    // 专门处理超时错误
    if ((error as any)?.code === 'ECONNABORTED' || /timeout/i.test(String((error as any)?.message))) {
      const requestUrl = error.config?.url || ''
      const timeoutValue = Number(error.config?.timeout) || DEFAULT_TIMEOUT
      const isImportRequest = requestUrl.includes('/import')
      const msg = isImportRequest
        ? '导入请求超时，但数据可能仍在后台处理中。请稍后刷新列表查看导入结果。'
        : `请求超时，请稍后重试（当前超时：${timeoutValue}ms）`
      ElMessage.warning(msg)
      return Promise.reject(error)
    }
    
    if (error.response) {
      const { status } = error.response
      const data: any = (error.response as any).data
      
      switch (status) {
        case 400:
          ElMessage.error(data?.message || '请求参数错误')
          break
        case 401:
          ElMessage.error('未授权，请重新登录')
          break
        case 403:
          ElMessage.error('拒绝访问')
          break
        case 404:
          ElMessage.error('请求的资源不存在')
          break
        case 500:
          ElMessage.error(data?.message || '服务器内部错误')
          break
        default:
          ElMessage.error(data?.message || '网络错误')
      }
    } else if (error.request) {
      ElMessage.error('网络连接失败，请检查网络')
    } else {
      ElMessage.error('请求配置错误')
    }
    
    return Promise.reject(error)
  }
)

export default request
