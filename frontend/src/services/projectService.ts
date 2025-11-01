/**
 * 项目管理服务
 * @author AI Agent
 * @version 1.0.0
 * @created 2024-12-29 18:00:00
 */

import { api, ApiResponse, PaginatedResponse } from './api'

// 项目状态枚举
export type ProjectStatus = 'ACTIVE' | 'ARCHIVED' | 'DELETED'

// 项目接口
export interface Project {
  id: number
  name: string
  description: string
  userId: number
  status: ProjectStatus
  settings: ProjectSettings
  fileCount: number
  analysisCount: number
  createdAt: string
  updatedAt: string
}

// 项目设置接口
export interface ProjectSettings {
  analysisTypes: string[]
  language: string
  autoAnalysis: boolean
  focusPeriod?: string
  includePoetry?: boolean
  compareDynasties?: string[]
  period?: string
  includeArchaeology?: boolean
  focus?: string
  timeSpan?: string
}

// 创建项目请求接口
export interface CreateProjectRequest {
  name: string
  description: string
  settings?: Partial<ProjectSettings>
}

// 更新项目请求接口
export interface UpdateProjectRequest {
  name?: string
  description?: string
  settings?: Partial<ProjectSettings>
  status?: ProjectStatus
}

// 项目查询参数接口
export interface ProjectQueryParams {
  page?: number
  size?: number
  status?: ProjectStatus
  keyword?: string
  sortBy?: string
  sortDirection?: 'ASC' | 'DESC'
}

// 项目统计信息接口
export interface ProjectStats {
  totalProjects: number
  activeProjects: number
  archivedProjects: number
  totalFiles: number
  totalAnalyses: number
  recentActivity: ProjectActivity[]
}

// 项目活动接口
export interface ProjectActivity {
  id: number
  projectId: number
  projectName: string
  activityType: 'CREATE' | 'UPDATE' | 'UPLOAD' | 'ANALYZE'
  description: string
  createdAt: string
}

// 项目服务类
class ProjectService {
  /**
   * 获取项目列表
   */
  async getProjects(params?: ProjectQueryParams): Promise<PaginatedResponse<Project>> {
    try {
      const response: ApiResponse<PaginatedResponse<Project>> = await api.get('/projects', {
        params: {
          page: params?.page || 0,
          size: params?.size || 10,
          status: params?.status,
          keyword: params?.keyword,
          sortBy: params?.sortBy || 'updatedAt',
          sortDirection: params?.sortDirection || 'DESC'
        }
      })

      if (response.success && response.data) {
        return response.data
      } else {
        throw new Error(response.message || '获取项目列表失败')
      }
    } catch (error) {
      console.error('获取项目列表失败:', error)
      throw error
    }
  }

  /**
   * 根据ID获取项目详情
   */
  async getProjectById(id: number): Promise<Project> {
    try {
      const response: ApiResponse<Project> = await api.get(`/projects/${id}`)

      if (response.success && response.data) {
        return response.data
      } else {
        throw new Error(response.message || '获取项目详情失败')
      }
    } catch (error) {
      console.error('获取项目详情失败:', error)
      throw error
    }
  }

  /**
   * 创建新项目
   */
  async createProject(projectData: CreateProjectRequest): Promise<Project> {
    try {
      const response: ApiResponse<Project> = await api.post('/projects', projectData)

      if (response.success && response.data) {
        return response.data
      } else {
        throw new Error(response.message || '创建项目失败')
      }
    } catch (error) {
      console.error('创建项目失败:', error)
      throw error
    }
  }

  /**
   * 更新项目
   */
  async updateProject(id: number, projectData: UpdateProjectRequest): Promise<Project> {
    try {
      const response: ApiResponse<Project> = await api.put(`/projects/${id}`, projectData)

      if (response.success && response.data) {
        return response.data
      } else {
        throw new Error(response.message || '更新项目失败')
      }
    } catch (error) {
      console.error('更新项目失败:', error)
      throw error
    }
  }

  /**
   * 删除项目
   */
  async deleteProject(id: number): Promise<void> {
    try {
      const response: ApiResponse = await api.delete(`/projects/${id}`)

      if (!response.success) {
        throw new Error(response.message || '删除项目失败')
      }
    } catch (error) {
      console.error('删除项目失败:', error)
      throw error
    }
  }

  /**
   * 归档项目
   */
  async archiveProject(id: number): Promise<Project> {
    try {
      const response: ApiResponse<Project> = await api.put(`/projects/${id}/archive`)

      if (response.success && response.data) {
        return response.data
      } else {
        throw new Error(response.message || '归档项目失败')
      }
    } catch (error) {
      console.error('归档项目失败:', error)
      throw error
    }
  }

  /**
   * 恢复项目
   */
  async restoreProject(id: number): Promise<Project> {
    try {
      const response: ApiResponse<Project> = await api.put(`/projects/${id}/restore`)

      if (response.success && response.data) {
        return response.data
      } else {
        throw new Error(response.message || '恢复项目失败')
      }
    } catch (error) {
      console.error('恢复项目失败:', error)
      throw error
    }
  }

  /**
   * 复制项目
   */
  async duplicateProject(id: number, newName: string): Promise<Project> {
    try {
      const response: ApiResponse<Project> = await api.post(`/projects/${id}/duplicate`, {
        name: newName
      })

      if (response.success && response.data) {
        return response.data
      } else {
        throw new Error(response.message || '复制项目失败')
      }
    } catch (error) {
      console.error('复制项目失败:', error)
      throw error
    }
  }

  /**
   * 获取项目统计信息
   */
  async getProjectStats(): Promise<ProjectStats> {
    try {
      const response: ApiResponse<ProjectStats> = await api.get('/projects/stats')

      if (response.success && response.data) {
        return response.data
      } else {
        throw new Error(response.message || '获取项目统计失败')
      }
    } catch (error) {
      console.error('获取项目统计失败:', error)
      throw error
    }
  }

  /**
   * 获取用户的项目列表（简化版）
   */
  async getUserProjects(): Promise<Project[]> {
    try {
      const response: ApiResponse<Project[]> = await api.get('/projects/my')

      if (response.success && response.data) {
        return response.data
      } else {
        throw new Error(response.message || '获取用户项目失败')
      }
    } catch (error) {
      console.error('获取用户项目失败:', error)
      throw error
    }
  }

  /**
   * 搜索项目
   */
  async searchProjects(keyword: string, limit: number = 10): Promise<Project[]> {
    try {
      const response: ApiResponse<Project[]> = await api.get('/projects/search', {
        params: { keyword, limit }
      })

      if (response.success && response.data) {
        return response.data
      } else {
        throw new Error(response.message || '搜索项目失败')
      }
    } catch (error) {
      console.error('搜索项目失败:', error)
      throw error
    }
  }

  /**
   * 获取项目活动记录
   */
  async getProjectActivities(projectId: number, limit: number = 20): Promise<ProjectActivity[]> {
    try {
      const response: ApiResponse<ProjectActivity[]> = await api.get(`/projects/${projectId}/activities`, {
        params: { limit }
      })

      if (response.success && response.data) {
        return response.data
      } else {
        throw new Error(response.message || '获取项目活动失败')
      }
    } catch (error) {
      console.error('获取项目活动失败:', error)
      throw error
    }
  }

  /**
   * 导出项目数据
   */
  async exportProject(id: number, format: 'JSON' | 'CSV' | 'EXCEL' = 'JSON'): Promise<Blob> {
    try {
      const response = await api.get(`/projects/${id}/export`, {
        params: { format },
        responseType: 'blob'
      })

      return response.data
    } catch (error) {
      console.error('导出项目失败:', error)
      throw error
    }
  }

  /**
   * 批量操作项目
   */
  async batchUpdateProjects(
    projectIds: number[], 
    operation: 'ARCHIVE' | 'RESTORE' | 'DELETE',
    data?: any
  ): Promise<void> {
    try {
      const response: ApiResponse = await api.post('/projects/batch', {
        projectIds,
        operation,
        data
      })

      if (!response.success) {
        throw new Error(response.message || '批量操作失败')
      }
    } catch (error) {
      console.error('批量操作项目失败:', error)
      throw error
    }
  }
}

// 创建项目服务实例
const projectService = new ProjectService()

export default projectService