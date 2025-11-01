/**
 * 分析服务
 * @author AI Agent
 * @version 1.0.0
 * @created 2024-12-29 18:00:00
 */

import { api, nlpApi, ApiResponse, PaginatedResponse } from './api'

// 分析类型枚举
export type AnalysisType = 'WORD_FREQUENCY' | 'TIMELINE' | 'GEOGRAPHY' | 'TEXT_SUMMARY' | 'MULTIDIMENSIONAL'

// 分析状态枚举
export type AnalysisStatus = 'PENDING' | 'RUNNING' | 'COMPLETED' | 'FAILED' | 'CANCELLED'

// 分析结果接口
export interface AnalysisResult {
  id: number
  projectId: number
  userId: number
  analysisType: AnalysisType
  fileIds: number[]
  parameters: Record<string, any>
  resultData: any
  status: AnalysisStatus
  progress: number
  executionTime: number
  errorMessage?: string
  createdAt: string
  updatedAt: string
}

// 词频分析结果接口
export interface WordFrequencyResult {
  totalWords: number
  uniqueWords: number
  lexicalDiversity: number
  averageWordLength: number
  topWords: Array<{
    word: string
    frequency: number
    category: string
    weight: number
    rank: number
  }>
  wordCloud: Array<{
    text: string
    value: number
    fontSize: number
    color: string
  }>
  categoryDistribution: Record<string, number>
  statistics: {
    totalWords: number
    uniqueWords: number
    lexicalDiversity: number
    averageWordLength: number
  }
}

// 时间轴分析结果接口
export interface TimelineResult {
  events: Array<{
    id: string
    eventText: string
    eventType: string
    timeExpression: string
    parsedTime: string
    yearStart: number
    yearEnd?: number
    dynasty?: string
    confidence: number
    importanceScore: number
    entities: {
      persons?: string[]
      locations?: string[]
      organizations?: string[]
      events?: string[]
    }
    location?: string
  }>
  statistics: {
    totalEvents: number
    timeSpan: { start: number; end: number }
    dynastyDistribution: Record<string, number>
    eventTypeDistribution: Record<string, number>
    averageConfidence: number
  }
}

// 地理分析结果接口
export interface GeographyResult {
  locations: Array<{
    id: string
    originalName: string
    standardName: string
    locationType: string
    level: string
    province?: string
    latitude?: number
    longitude?: number
    confidence: number
    occurrenceCount: number
    aliases: string[]
    historicalNames: string[]
    modernName: string
    context: string[]
  }>
  statistics: {
    totalLocations: number
    typeDistribution: Record<string, number>
    levelDistribution: Record<string, number>
    provinceDistribution: Record<string, number>
    averageConfidence: number
  }
  geographicalDistribution: {
    coordinates: Array<{ lat: number; lng: number; count: number; name: string }>
    regions: Record<string, number>
  }
}

// 文本摘要结果接口
export interface TextSummaryResult {
  summary: string
  keyTopics: Array<{
    topic: string
    category: string
    score: number
    keywords: string[]
  }>
  extractedSentences: Array<{
    sentence: string
    score: number
    position: number
  }>
  statistics: {
    originalLength: number
    summaryLength: number
    compressionRatio: number
    sentenceCount: number
    keywordCount: number
  }
}

// 多维分析结果接口
export interface MultidimensionalResult {
  wordFrequency: Partial<WordFrequencyResult>
  timeline: Partial<TimelineResult>
  geography: Partial<GeographyResult>
  textSummary: Partial<TextSummaryResult>
  correlations: {
    timeLocationCorrelation: Array<{
      time: string
      locations: string[]
      events: string[]
    }>
    keywordTimeCorrelation: Array<{
      keyword: string
      timeDistribution: Record<string, number>
    }>
    locationEventCorrelation: Array<{
      location: string
      events: string[]
      frequency: number
    }>
  }
  insights: string[]
}

// 分析请求接口
export interface AnalysisRequest {
  projectId: number
  analysisType: AnalysisType
  fileIds: number[]
  parameters?: Record<string, any>
}

// 分析参数接口
export interface AnalysisParameters {
  // 词频分析参数
  wordFrequency?: {
    maxWords?: number
    minFrequency?: number
    filterStopwords?: boolean
    includeCategories?: string[]
    customDictionary?: string[]
  }
  
  // 时间轴分析参数
  timeline?: {
    enableDynastyMapping?: boolean
    minConfidence?: number
    includeRelativeTime?: boolean
    focusPeriod?: string
  }
  
  // 地理分析参数
  geography?: {
    enableCoordinates?: boolean
    minConfidence?: number
    includeHistoricalNames?: boolean
    focusRegion?: string
  }
  
  // 文本摘要参数
  textSummary?: {
    maxSentences?: number
    maxLength?: number
    includeKeywords?: boolean
    summaryType?: 'extractive' | 'abstractive'
  }
  
  // 多维分析参数
  multidimensional?: {
    includeAllTypes?: boolean
    focusKeywords?: string[]
    timeRange?: { start: number; end: number }
    enableCorrelations?: boolean
  }
}

// 分析查询参数接口
export interface AnalysisQueryParams {
  page?: number
  size?: number
  projectId?: number
  analysisType?: AnalysisType
  status?: AnalysisStatus
  sortBy?: string
  sortDirection?: 'ASC' | 'DESC'
}

// 分析服务类
class AnalysisService {
  /**
   * 创建分析任务
   */
  async createAnalysis(request: AnalysisRequest): Promise<AnalysisResult> {
    try {
      const response: ApiResponse<AnalysisResult> = await api.post('/analysis', request)

      if (response.success && response.data) {
        return response.data
      } else {
        throw new Error(response.message || '创建分析任务失败')
      }
    } catch (error) {
      console.error('创建分析任务失败:', error)
      throw error
    }
  }

  /**
   * 获取分析结果列表
   */
  async getAnalysisResults(params?: AnalysisQueryParams): Promise<PaginatedResponse<AnalysisResult>> {
    try {
      const response: ApiResponse<PaginatedResponse<AnalysisResult>> = await api.get('/analysis', {
        params: {
          page: params?.page || 0,
          size: params?.size || 10,
          projectId: params?.projectId,
          analysisType: params?.analysisType,
          status: params?.status,
          sortBy: params?.sortBy || 'createdAt',
          sortDirection: params?.sortDirection || 'DESC'
        }
      })

      if (response.success && response.data) {
        return response.data
      } else {
        throw new Error(response.message || '获取分析结果失败')
      }
    } catch (error) {
      console.error('获取分析结果失败:', error)
      throw error
    }
  }

  /**
   * 根据ID获取分析结果
   */
  async getAnalysisById(id: number): Promise<AnalysisResult> {
    try {
      const response: ApiResponse<AnalysisResult> = await api.get(`/analysis/${id}`)

      if (response.success && response.data) {
        return response.data
      } else {
        throw new Error(response.message || '获取分析结果失败')
      }
    } catch (error) {
      console.error('获取分析结果失败:', error)
      throw error
    }
  }

  /**
   * 取消分析任务
   */
  async cancelAnalysis(id: number): Promise<void> {
    try {
      const response: ApiResponse = await api.post(`/analysis/${id}/cancel`)

      if (!response.success) {
        throw new Error(response.message || '取消分析任务失败')
      }
    } catch (error) {
      console.error('取消分析任务失败:', error)
      throw error
    }
  }

  /**
   * 删除分析结果
   */
  async deleteAnalysis(id: number): Promise<void> {
    try {
      const response: ApiResponse = await api.delete(`/analysis/${id}`)

      if (!response.success) {
        throw new Error(response.message || '删除分析结果失败')
      }
    } catch (error) {
      console.error('删除分析结果失败:', error)
      throw error
    }
  }

  /**
   * 重新运行分析
   */
  async rerunAnalysis(id: number): Promise<AnalysisResult> {
    try {
      const response: ApiResponse<AnalysisResult> = await api.post(`/analysis/${id}/rerun`)

      if (response.success && response.data) {
        return response.data
      } else {
        throw new Error(response.message || '重新运行分析失败')
      }
    } catch (error) {
      console.error('重新运行分析失败:', error)
      throw error
    }
  }

  /**
   * 导出分析结果
   */
  async exportAnalysis(id: number, format: 'JSON' | 'CSV' | 'EXCEL' | 'PDF' = 'JSON'): Promise<Blob> {
    try {
      const response = await api.get(`/analysis/${id}/export`, {
        params: { format },
        responseType: 'blob'
      })

      return response.data
    } catch (error) {
      console.error('导出分析结果失败:', error)
      throw error
    }
  }

  /**
   * 获取项目的分析结果
   */
  async getProjectAnalyses(projectId: number): Promise<AnalysisResult[]> {
    try {
      const response: ApiResponse<AnalysisResult[]> = await api.get(`/projects/${projectId}/analysis`)

      if (response.success && response.data) {
        return response.data
      } else {
        throw new Error(response.message || '获取项目分析结果失败')
      }
    } catch (error) {
      console.error('获取项目分析结果失败:', error)
      throw error
    }
  }

  // NLP服务直接调用方法

  /**
   * 直接调用词频分析
   */
  async analyzeWordFrequency(text: string, parameters?: AnalysisParameters['wordFrequency']): Promise<WordFrequencyResult> {
    try {
      const response = await nlpApi.post<WordFrequencyResult>('/word-frequency', {
        text,
        ...parameters
      })

      return response
    } catch (error) {
      console.error('词频分析失败:', error)
      throw error
    }
  }

  /**
   * 直接调用时间轴分析
   */
  async analyzeTimeline(text: string, parameters?: AnalysisParameters['timeline']): Promise<TimelineResult> {
    try {
      const response = await nlpApi.post<TimelineResult>('/timeline', {
        text,
        ...parameters
      })

      return response
    } catch (error) {
      console.error('时间轴分析失败:', error)
      throw error
    }
  }

  /**
   * 直接调用地理分析
   */
  async analyzeGeography(text: string, parameters?: AnalysisParameters['geography']): Promise<GeographyResult> {
    try {
      const response = await nlpApi.post<GeographyResult>('/geography', {
        text,
        ...parameters
      })

      return response
    } catch (error) {
      console.error('地理分析失败:', error)
      throw error
    }
  }

  /**
   * 直接调用文本摘要
   */
  async analyzeTextSummary(text: string, parameters?: AnalysisParameters['textSummary']): Promise<TextSummaryResult> {
    try {
      const response = await nlpApi.post<TextSummaryResult>('/summarize', {
        text,
        ...parameters
      })

      return response
    } catch (error) {
      console.error('文本摘要失败:', error)
      throw error
    }
  }

  /**
   * 直接调用多维分析
   */
  async analyzeMultidimensional(text: string, parameters?: AnalysisParameters['multidimensional']): Promise<MultidimensionalResult> {
    try {
      const response = await nlpApi.post<MultidimensionalResult>('/multidimensional', {
        text,
        ...parameters
      })

      return response
    } catch (error) {
      console.error('多维分析失败:', error)
      throw error
    }
  }

  /**
   * 检查NLP服务健康状态
   */
  async checkNlpHealth(): Promise<{ status: string; version: string; uptime: number }> {
    try {
      const response = await nlpApi.get<{ status: string; version: string; uptime: number }>('/health')
      return response
    } catch (error) {
      console.error('检查NLP服务状态失败:', error)
      throw error
    }
  }

  /**
   * 获取分析进度
   */
  async getAnalysisProgress(id: number): Promise<{ progress: number; status: AnalysisStatus; message?: string }> {
    try {
      const response: ApiResponse<{ progress: number; status: AnalysisStatus; message?: string }> = 
        await api.get(`/analysis/${id}/progress`)

      if (response.success && response.data) {
        return response.data
      } else {
        throw new Error(response.message || '获取分析进度失败')
      }
    } catch (error) {
      console.error('获取分析进度失败:', error)
      throw error
    }
  }

  /**
   * 批量创建分析任务
   */
  async batchCreateAnalysis(requests: AnalysisRequest[]): Promise<AnalysisResult[]> {
    try {
      const response: ApiResponse<AnalysisResult[]> = await api.post('/analysis/batch', { requests })

      if (response.success && response.data) {
        return response.data
      } else {
        throw new Error(response.message || '批量创建分析任务失败')
      }
    } catch (error) {
      console.error('批量创建分析任务失败:', error)
      throw error
    }
  }

  /**
   * 获取分析类型的默认参数
   */
  getDefaultParameters(analysisType: AnalysisType): AnalysisParameters {
    const defaults: Record<AnalysisType, AnalysisParameters> = {
      WORD_FREQUENCY: {
        wordFrequency: {
          maxWords: 50,
          minFrequency: 2,
          filterStopwords: true,
          includeCategories: ['历史人物', '地理位置', '历史事件', '朝代', '文化概念']
        }
      },
      TIMELINE: {
        timeline: {
          enableDynastyMapping: true,
          minConfidence: 0.5,
          includeRelativeTime: true
        }
      },
      GEOGRAPHY: {
        geography: {
          enableCoordinates: true,
          minConfidence: 0.3,
          includeHistoricalNames: true
        }
      },
      TEXT_SUMMARY: {
        textSummary: {
          maxSentences: 5,
          maxLength: 500,
          includeKeywords: true,
          summaryType: 'extractive'
        }
      },
      MULTIDIMENSIONAL: {
        multidimensional: {
          includeAllTypes: true,
          enableCorrelations: true
        }
      }
    }

    return defaults[analysisType] || {}
  }

  /**
   * 格式化分析类型显示名称
   */
  getAnalysisTypeName(type: AnalysisType): string {
    const names: Record<AnalysisType, string> = {
      WORD_FREQUENCY: '词频分析',
      TIMELINE: '时间轴分析',
      GEOGRAPHY: '地理分析',
      TEXT_SUMMARY: '文本摘要',
      MULTIDIMENSIONAL: '多维分析'
    }

    return names[type] || type
  }

  /**
   * 格式化分析状态显示名称
   */
  getAnalysisStatusName(status: AnalysisStatus): string {
    const names: Record<AnalysisStatus, string> = {
      PENDING: '等待中',
      RUNNING: '运行中',
      COMPLETED: '已完成',
      FAILED: '失败',
      CANCELLED: '已取消'
    }

    return names[status] || status
  }
}

// 创建分析服务实例
const analysisService = new AnalysisService()

export default analysisService