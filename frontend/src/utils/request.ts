import axios from 'axios'
import { ElMessage } from 'element-plus'

// 创建axios实例
const baseURL = (import.meta as any)?.env?.VITE_API_BASE_URL || 'http://localhost:8081'
const DEFAULT_TIMEOUT = Number((import.meta as any)?.env?.VITE_API_TIMEOUT) || 60000

const request = axios.create({
  baseURL, // 后端服务地址（可通过环境变量覆盖）
  timeout: DEFAULT_TIMEOUT, // 请求超时时间（默认60秒，可配置）
  headers: {
    'Content-Type': 'application/json'
  }
})

// 请求拦截器
request.interceptors.request.use(
  (config) => {
    // 在发送请求之前做些什么
    return config
  },
  (error) => {
    // 对请求错误做些什么
    return Promise.reject(error)
  }
)

// 响应拦截器
request.interceptors.response.use(
  (response) => {
    // 对响应数据做点什么
    const { data } = response
    
    // 如果是文件下载等特殊响应，直接返回
    if (response.config.responseType === 'blob') {
      return response
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
  (error) => {
    // 对响应错误做点什么
    console.error('请求错误:', error)
    // 专门处理超时错误
    if ((error as any)?.code === 'ECONNABORTED' || /timeout/i.test(String((error as any)?.message))) {
      ElMessage.error(`请求超时，请稍后重试（当前超时：${DEFAULT_TIMEOUT}ms）`)
      return Promise.reject(error)
    }
    
    if (error.response) {
      const { status, data } = error.response
      
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
