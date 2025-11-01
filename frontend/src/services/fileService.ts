/**
 * 文件管理服务
 * @author AI Agent
 * @version 1.0.0
 * @created 2024-12-29 18:00:00
 */

import { api, ApiResponse, PaginatedResponse } from './api'

// 文件状态枚举
export type FileStatus = 'UPLOADING' | 'PROCESSING' | 'PROCESSED' | 'FAILED'

// 文件类型枚举
export type FileType = 'txt' | 'doc' | 'docx' | 'pdf' | 'rtf'

// 文件接口
export interface FileInfo {
  id: number
  filename: string
  originalFilename: string
  filePath: string
  fileSize: number
  fileType: FileType
  mimeType: string
  projectId: number
  userId: number
  contentHash: string
  textContent?: string
  textLength: number
  status: FileStatus
  errorMessage?: string
  uploadProgress?: number
  createdAt: string
  updatedAt: string
}

// 文件上传请求接口
export interface FileUploadRequest {
  projectId: number
  file: File
  description?: string
}

// 文件查询参数接口
export interface FileQueryParams {
  page?: number
  size?: number
  projectId?: number
  status?: FileStatus
  fileType?: FileType
  keyword?: string
  sortBy?: string
  sortDirection?: 'ASC' | 'DESC'
}

// 文件统计信息接口
export interface FileStats {
  totalFiles: number
  totalSize: number
  processingFiles: number
  processedFiles: number
  failedFiles: number
  fileTypeDistribution: Record<FileType, number>
  recentUploads: FileInfo[]
}

// 批量上传结果接口
export interface BatchUploadResult {
  successCount: number
  failedCount: number
  results: Array<{
    filename: string
    success: boolean
    fileId?: number
    error?: string
  }>
}

// 文件内容预览接口
export interface FilePreview {
  fileId: number
  filename: string
  content: string
  contentType: 'text' | 'html'
  totalLength: number
  previewLength: number
}

// 文件服务类
class FileService {
  /**
   * 上传单个文件
   */
  async uploadFile(
    uploadRequest: FileUploadRequest,
    onProgress?: (progress: number) => void
  ): Promise<FileInfo> {
    try {
      const formData = new FormData()
      formData.append('file', uploadRequest.file)
      formData.append('projectId', uploadRequest.projectId.toString())
      if (uploadRequest.description) {
        formData.append('description', uploadRequest.description)
      }

      const response: ApiResponse<FileInfo> = await api.upload(
        '/files/upload',
        formData,
        onProgress
      )

      if (response.success && response.data) {
        return response.data
      } else {
        throw new Error(response.message || '文件上传失败')
      }
    } catch (error) {
      console.error('文件上传失败:', error)
      throw error
    }
  }

  /**
   * 批量上传文件
   */
  async uploadFiles(
    projectId: number,
    files: File[],
    onProgress?: (progress: number) => void
  ): Promise<BatchUploadResult> {
    try {
      const formData = new FormData()
      formData.append('projectId', projectId.toString())
      
      files.forEach((file, index) => {
        formData.append(`files`, file)
      })

      const response: ApiResponse<BatchUploadResult> = await api.upload(
        '/files/batch-upload',
        formData,
        onProgress
      )

      if (response.success && response.data) {
        return response.data
      } else {
        throw new Error(response.message || '批量上传失败')
      }
    } catch (error) {
      console.error('批量上传失败:', error)
      throw error
    }
  }

  /**
   * 获取文件列表
   */
  async getFiles(params?: FileQueryParams): Promise<PaginatedResponse<FileInfo>> {
    try {
      const response: ApiResponse<PaginatedResponse<FileInfo>> = await api.get('/files', {
        params: {
          page: params?.page || 0,
          size: params?.size || 10,
          projectId: params?.projectId,
          status: params?.status,
          fileType: params?.fileType,
          keyword: params?.keyword,
          sortBy: params?.sortBy || 'createdAt',
          sortDirection: params?.sortDirection || 'DESC'
        }
      })

      if (response.success && response.data) {
        return response.data
      } else {
        throw new Error(response.message || '获取文件列表失败')
      }
    } catch (error) {
      console.error('获取文件列表失败:', error)
      throw error
    }
  }

  /**
   * 根据ID获取文件详情
   */
  async getFileById(id: number): Promise<FileInfo> {
    try {
      const response: ApiResponse<FileInfo> = await api.get(`/files/${id}`)

      if (response.success && response.data) {
        return response.data
      } else {
        throw new Error(response.message || '获取文件详情失败')
      }
    } catch (error) {
      console.error('获取文件详情失败:', error)
      throw error
    }
  }

  /**
   * 获取项目的文件列表
   */
  async getProjectFiles(projectId: number): Promise<FileInfo[]> {
    try {
      const response: ApiResponse<FileInfo[]> = await api.get(`/projects/${projectId}/files`)

      if (response.success && response.data) {
        return response.data
      } else {
        throw new Error(response.message || '获取项目文件失败')
      }
    } catch (error) {
      console.error('获取项目文件失败:', error)
      throw error
    }
  }

  /**
   * 删除文件
   */
  async deleteFile(id: number): Promise<void> {
    try {
      const response: ApiResponse = await api.delete(`/files/${id}`)

      if (!response.success) {
        throw new Error(response.message || '删除文件失败')
      }
    } catch (error) {
      console.error('删除文件失败:', error)
      throw error
    }
  }

  /**
   * 批量删除文件
   */
  async deleteFiles(fileIds: number[]): Promise<void> {
    try {
      const response: ApiResponse = await api.post('/files/batch-delete', { fileIds })

      if (!response.success) {
        throw new Error(response.message || '批量删除文件失败')
      }
    } catch (error) {
      console.error('批量删除文件失败:', error)
      throw error
    }
  }

  /**
   * 重新处理文件
   */
  async reprocessFile(id: number): Promise<FileInfo> {
    try {
      const response: ApiResponse<FileInfo> = await api.post(`/files/${id}/reprocess`)

      if (response.success && response.data) {
        return response.data
      } else {
        throw new Error(response.message || '重新处理文件失败')
      }
    } catch (error) {
      console.error('重新处理文件失败:', error)
      throw error
    }
  }

  /**
   * 获取文件内容预览
   */
  async getFilePreview(id: number, maxLength: number = 1000): Promise<FilePreview> {
    try {
      const response: ApiResponse<FilePreview> = await api.get(`/files/${id}/preview`, {
        params: { maxLength }
      })

      if (response.success && response.data) {
        return response.data
      } else {
        throw new Error(response.message || '获取文件预览失败')
      }
    } catch (error) {
      console.error('获取文件预览失败:', error)
      throw error
    }
  }

  /**
   * 下载文件
   */
  async downloadFile(id: number): Promise<Blob> {
    try {
      const response = await api.get(`/files/${id}/download`, {
        responseType: 'blob'
      })

      return response.data
    } catch (error) {
      console.error('下载文件失败:', error)
      throw error
    }
  }

  /**
   * 获取文件统计信息
   */
  async getFileStats(projectId?: number): Promise<FileStats> {
    try {
      const response: ApiResponse<FileStats> = await api.get('/files/stats', {
        params: { projectId }
      })

      if (response.success && response.data) {
        return response.data
      } else {
        throw new Error(response.message || '获取文件统计失败')
      }
    } catch (error) {
      console.error('获取文件统计失败:', error)
      throw error
    }
  }

  /**
   * 搜索文件
   */
  async searchFiles(keyword: string, projectId?: number, limit: number = 10): Promise<FileInfo[]> {
    try {
      const response: ApiResponse<FileInfo[]> = await api.get('/files/search', {
        params: { keyword, projectId, limit }
      })

      if (response.success && response.data) {
        return response.data
      } else {
        throw new Error(response.message || '搜索文件失败')
      }
    } catch (error) {
      console.error('搜索文件失败:', error)
      throw error
    }
  }

  /**
   * 检查文件上传状态
   */
  async checkUploadStatus(fileIds: number[]): Promise<Record<number, FileStatus>> {
    try {
      const response: ApiResponse<Record<number, FileStatus>> = await api.post('/files/check-status', {
        fileIds
      })

      if (response.success && response.data) {
        return response.data
      } else {
        throw new Error(response.message || '检查文件状态失败')
      }
    } catch (error) {
      console.error('检查文件状态失败:', error)
      throw error
    }
  }

  /**
   * 获取支持的文件类型
   */
  async getSupportedFileTypes(): Promise<Array<{ type: FileType; mimeTypes: string[]; maxSize: number }>> {
    try {
      const response: ApiResponse<Array<{ type: FileType; mimeTypes: string[]; maxSize: number }>> = 
        await api.get('/files/supported-types')

      if (response.success && response.data) {
        return response.data
      } else {
        throw new Error(response.message || '获取支持的文件类型失败')
      }
    } catch (error) {
      console.error('获取支持的文件类型失败:', error)
      throw error
    }
  }

  /**
   * 验证文件
   */
  validateFile(file: File): { valid: boolean; error?: string } {
    // 支持的文件类型
    const supportedTypes = ['text/plain', 'application/pdf', 'application/msword', 
                           'application/vnd.openxmlformats-officedocument.wordprocessingml.document']
    
    // 最大文件大小 (10MB)
    const maxSize = 10 * 1024 * 1024

    if (!supportedTypes.includes(file.type)) {
      return { valid: false, error: '不支持的文件类型' }
    }

    if (file.size > maxSize) {
      return { valid: false, error: '文件大小超过限制（10MB）' }
    }

    if (file.size === 0) {
      return { valid: false, error: '文件为空' }
    }

    return { valid: true }
  }

  /**
   * 格式化文件大小
   */
  formatFileSize(bytes: number): string {
    if (bytes === 0) return '0 B'
    
    const k = 1024
    const sizes = ['B', 'KB', 'MB', 'GB']
    const i = Math.floor(Math.log(bytes) / Math.log(k))
    
    return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
  }

  /**
   * 获取文件类型图标
   */
  getFileTypeIcon(fileType: FileType): string {
    const iconMap: Record<FileType, string> = {
      txt: 'file-text',
      doc: 'file-word',
      docx: 'file-word',
      pdf: 'file-pdf',
      rtf: 'file-text'
    }
    
    return iconMap[fileType] || 'file'
  }
}

// 创建文件服务实例
const fileService = new FileService()

export default fileService