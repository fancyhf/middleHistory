/**
 * API服务配置
 * @author AI Agent
 * @version 1.0.0
 * @created 2024-12-29 18:00:00
 */

import axios, { AxiosInstance, AxiosRequestConfig, AxiosResponse } from 'axios'
import { message } from 'antd'

// API基础配置
const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080'
const NLP_BASE_URL = import.meta.env.VITE_NLP_BASE_URL || 'http://localhost:5001/api'

// 创建主API实例
const apiClient: AxiosInstance = axios.create({
  baseURL: API_BASE_URL,
  timeout: 30000,
  headers: {
    'Content-Type': 'application/json',
  },
  withCredentials: false, // 开发环境CORS支持
})

// 创建NLP服务API实例
const nlpClient: AxiosInstance = axios.create({
  baseURL: NLP_BASE_URL,
  timeout: 60000, // NLP分析可能需要更长时间
  headers: {
    'Content-Type': 'application/json',
  },
  withCredentials: false, // 开发环境CORS支持
})

// 服务状态管理
let serviceStatus = {
  main: false,
  nlp: false,
  lastCheck: new Date()
}

// 检查用户是否已登录
const isUserLoggedIn = (): boolean => {
  return !!localStorage.getItem('access_token')
}

// 请求拦截器 - 添加认证token
apiClient.interceptors.request.use(
  (config: AxiosRequestConfig) => {
    const token = localStorage.getItem('access_token')
    if (token && config.headers) {
      config.headers.Authorization = `Bearer ${token}`
    }
    
    // 开发环境日志
    if (import.meta.env.VITE_DEV_MODE === 'true') {
      console.log('API请求:', config.method?.toUpperCase(), config.url, config.data)
    }
    
    return config
  },
  (error) => {
    console.error('请求拦截器错误:', error)
    return Promise.reject(error)
  }
)

// NLP服务请求拦截器
nlpClient.interceptors.request.use(
  (config: AxiosRequestConfig) => {
    // 开发环境日志
    if (import.meta.env.VITE_DEV_MODE === 'true') {
      console.log('NLP请求:', config.method?.toUpperCase(), config.url, config.data)
    }
    
    return config
  },
  (error) => {
    console.error('NLP请求拦截器错误:', error)
    return Promise.reject(error)
  }
)

// 响应拦截器 - 统一错误处理
apiClient.interceptors.response.use(
  (response: AxiosResponse) => {
    // 更新服务状态
    serviceStatus.main = true
    serviceStatus.lastCheck = new Date()
    
    // 开发环境日志
    if (import.meta.env.VITE_DEV_MODE === 'true') {
      console.log('API响应:', response.status, response.data)
    }
    
    return response
  },
  (error) => {
    // 更新服务状态
    serviceStatus.main = false
    serviceStatus.lastCheck = new Date()
    
    console.error('API响应错误:', error)
    
    if (error.response) {
      const { status, data } = error.response
      
      switch (status) {
        case 401:
          // 只有在用户已登录的情况下才显示"登录已过期"
          if (isUserLoggedIn()) {
            message.error('登录已过期，请重新登录')
            localStorage.removeItem('access_token')
            localStorage.removeItem('refresh_token')
            // 开发环境不自动跳转登录页
            if (import.meta.env.VITE_DEV_MODE !== 'true') {
              window.location.href = '/login'
            }
          } else {
            // 用户未登录，静默处理，不显示错误消息
            console.log('需要登录才能访问此资源')
          }
          break
        case 403:
          if (isUserLoggedIn()) {
            message.error('没有权限访问该资源')
          } else {
            message.info('此功能需要登录后使用')
          }
          break
        case 404:
          message.error('请求的资源不存在')
          break
        case 500:
          message.error('服务器内部错误')
          break
        default:
          message.error(data?.message || '请求失败')
      }
    } else if (error.request) {
      // 网络连接失败，提供友好的提示
      if (import.meta.env.VITE_DEV_MODE === 'true') {
        console.error('网络错误详情:', error.request)
        // 开发环境显示详细错误，但不弹出消息
        console.log(`主API服务连接失败: ${error.message}`)
      } else {
        message.warning('网络连接失败，部分功能可能不可用')
      }
    } else {
      message.error('请求配置错误')
    }
    
    return Promise.reject(error)
  }
)

// NLP服务响应拦截器
nlpClient.interceptors.response.use(
  (response: AxiosResponse) => {
    // 更新服务状态
    serviceStatus.nlp = true
    serviceStatus.lastCheck = new Date()
    
    // 开发环境日志
    if (import.meta.env.VITE_DEV_MODE === 'true') {
      console.log('NLP响应:', response.status, response.data)
    }
    
    return response
  },
  (error) => {
    // 更新服务状态
    serviceStatus.nlp = false
    serviceStatus.lastCheck = new Date()
    
    console.error('NLP服务错误:', error)
    
    if (error.response) {
      const { status, data } = error.response
      const errorMsg = data?.message || `NLP服务错误 (${status})`
      
      // 开发环境显示详细错误
      if (import.meta.env.VITE_DEV_MODE === 'true') {
        console.error('NLP错误详情:', data)
        console.log(`NLP服务响应错误: ${errorMsg}`)
      } else {
        message.warning('NLP分析服务暂时不可用')
      }
    } else if (error.request) {
      if (import.meta.env.VITE_DEV_MODE === 'true') {
        console.error('NLP网络错误详情:', error.request)
        console.log(`NLP服务连接失败: ${error.message}`)
      } else {
        message.warning('NLP分析服务连接失败')
      }
    } else {
      console.error('NLP服务请求配置错误')
    }
    
    return Promise.reject(error)
  }
)

// API响应类型定义
export interface ApiResponse<T = any> {
  success: boolean
  message: string
  data: T
  timestamp: string
}

export interface PaginatedResponse<T> {
  content: T[]
  totalElements: number
  totalPages: number
  size: number
  number: number
  first: boolean
  last: boolean
}

// 通用API请求方法
export const api = {
  // GET请求
  get: <T = any>(url: string, config?: AxiosRequestConfig): Promise<ApiResponse<T>> =>
    apiClient.get(url, config).then(response => response.data),
  
  // POST请求
  post: <T = any>(url: string, data?: any, config?: AxiosRequestConfig): Promise<ApiResponse<T>> =>
    apiClient.post(url, data, config).then(response => response.data),
  
  // PUT请求
  put: <T = any>(url: string, data?: any, config?: AxiosRequestConfig): Promise<ApiResponse<T>> =>
    apiClient.put(url, data, config).then(response => response.data),
  
  // DELETE请求
  delete: <T = any>(url: string, config?: AxiosRequestConfig): Promise<ApiResponse<T>> =>
    apiClient.delete(url, config).then(response => response.data),
  
  // 文件上传
  upload: <T = any>(url: string, formData: FormData, onProgress?: (progress: number) => void): Promise<ApiResponse<T>> =>
    apiClient.post(url, formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
      onUploadProgress: (progressEvent) => {
        if (onProgress && progressEvent.total) {
          const progress = Math.round((progressEvent.loaded * 100) / progressEvent.total)
          onProgress(progress)
        }
      },
    }).then(response => response.data),
}

// NLP服务API请求方法
export const nlpApi = {
  // GET请求
  get: <T = any>(url: string, config?: AxiosRequestConfig): Promise<T> =>
    nlpClient.get(url, config).then(response => response.data),
  
  // POST请求
  post: <T = any>(url: string, data?: any, config?: AxiosRequestConfig): Promise<T> =>
    nlpClient.post(url, data, config).then(response => response.data),
}

// 健康检查方法
export const healthCheck = {
  // 检查主API服务
  checkMainApi: async (): Promise<boolean> => {
    try {
      // 创建一个独立的axios实例，不使用全局拦截器
      const healthClient = axios.create({
        baseURL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080',
        timeout: 5000,
      })
      
      await healthClient.get('/actuator/health')
      serviceStatus.main = true
      serviceStatus.lastCheck = new Date()
      return true
    } catch (error) {
      // 静默处理健康检查错误，不在控制台显示
      serviceStatus.main = false
      serviceStatus.lastCheck = new Date()
      return false
    }
  },
  
  // 检查NLP服务
  checkNlpApi: async (): Promise<boolean> => {
    try {
      await nlpClient.get('/health')
      serviceStatus.nlp = true
      serviceStatus.lastCheck = new Date()
      return true
    } catch (error) {
      console.error('NLP服务不可用:', error)
      serviceStatus.nlp = false
      serviceStatus.lastCheck = new Date()
      return false
    }
  },
  
  // 检查所有服务
  checkAllServices: async (): Promise<{ main: boolean; nlp: boolean }> => {
    const [main, nlp] = await Promise.all([
      healthCheck.checkMainApi(),
      healthCheck.checkNlpApi()
    ])
    
    return { main, nlp }
  }
}

// 服务状态管理
export const serviceStatusManager = {
  // 获取当前服务状态
  getStatus: () => ({ ...serviceStatus }),
  
  // 获取服务状态文本
  getStatusText: (service: 'main' | 'nlp'): string => {
    return serviceStatus[service] ? '在线' : '离线'
  },
  
  // 获取所有服务状态
  getAllStatus: () => ({
    main: { online: serviceStatus.main, text: serviceStatus.main ? '在线' : '离线' },
    nlp: { online: serviceStatus.nlp, text: serviceStatus.nlp ? '在线' : '离线' },
    lastCheck: serviceStatus.lastCheck
  }),
  
  // 重置服务状态
  resetStatus: () => {
    serviceStatus.main = false
    serviceStatus.nlp = false
    serviceStatus.lastCheck = new Date()
  }
}

// 游客模式API - 不需要认证的API调用
export const guestApi = {
  // 获取公开统计数据
  getPublicStats: async () => {
    try {
      return await api.get('/public/stats')
    } catch (error) {
      console.log('获取公开统计数据失败，使用模拟数据')
      // 返回模拟数据以支持游客模式
      return {
        success: true,
        data: {
          totalDocuments: 0,
          totalAnalysis: 0,
          recentAnalysis: []
        }
      }
    }
  },
  
  // 文本分析（游客模式）
  analyzeText: async (text: string) => {
    try {
      return await nlpApi.post('/analyze/guest', { text })
    } catch (error) {
      console.log('NLP服务不可用，提供基础分析')
      // 提供基础的文本分析功能
      return {
        wordCount: text.length,
        characterCount: text.replace(/\s/g, '').length,
        paragraphCount: text.split('\n\n').length,
        sentiment: '中性',
        keywords: []
      }
    }
  }
}

// 导出API实例和服务状态
export { apiClient, nlpClient, serviceStatus }
export default api