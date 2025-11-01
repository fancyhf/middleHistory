/**
 * 用户认证服务
 * @author AI Agent
 * @version 1.0.0
 * @created 2024-12-29 18:00:00
 */

import { api, ApiResponse } from './api'

// 用户信息接口
export interface User {
  id: number
  username: string
  email: string
  fullName: string
  role: 'ADMIN' | 'USER'
  status: 'ACTIVE' | 'INACTIVE' | 'LOCKED'
  avatar?: string
  lastLoginAt?: string
  createdAt: string
  updatedAt: string
}

// 登录请求接口
export interface LoginRequest {
  username: string
  password: string
  rememberMe?: boolean
}

// 登录响应接口
export interface LoginResponse {
  accessToken: string
  refreshToken: string
  tokenType: string
  expiresIn: number
  user: User
}

// 注册请求接口
export interface RegisterRequest {
  username: string
  email: string
  password: string
  fullName: string
}

// Token刷新响应接口
export interface RefreshTokenResponse {
  accessToken: string
  refreshToken: string
  tokenType: string
  expiresIn: number
}

// 认证服务类
class AuthService {
  private readonly TOKEN_KEY = 'access_token'
  private readonly REFRESH_TOKEN_KEY = 'refresh_token'
  private readonly USER_KEY = 'user_info'

  /**
   * 用户登录
   */
  async login(credentials: LoginRequest): Promise<LoginResponse> {
    try {
      const response: ApiResponse<LoginResponse> = await api.post('/auth/login', credentials)
      
      if (response.success && response.data) {
        // 保存token和用户信息
        this.setTokens(response.data.accessToken, response.data.refreshToken)
        this.setUserInfo(response.data.user)
        
        return response.data
      } else {
        throw new Error(response.message || '登录失败')
      }
    } catch (error) {
      console.error('登录失败:', error)
      throw error
    }
  }

  /**
   * 用户注册
   */
  async register(userData: RegisterRequest): Promise<User> {
    try {
      const response: ApiResponse<User> = await api.post('/auth/register', userData)
      
      if (response.success && response.data) {
        return response.data
      } else {
        throw new Error(response.message || '注册失败')
      }
    } catch (error) {
      console.error('注册失败:', error)
      throw error
    }
  }

  /**
   * 用户登出
   */
  async logout(): Promise<void> {
    try {
      await api.post('/auth/logout')
    } catch (error) {
      console.error('登出请求失败:', error)
    } finally {
      // 清除本地存储的认证信息
      this.clearAuthData()
    }
  }

  /**
   * 刷新访问令牌
   */
  async refreshToken(): Promise<RefreshTokenResponse> {
    try {
      const refreshToken = this.getRefreshToken()
      if (!refreshToken) {
        throw new Error('没有刷新令牌')
      }

      const response: ApiResponse<RefreshTokenResponse> = await api.post('/auth/refresh', {
        refreshToken
      })

      if (response.success && response.data) {
        // 更新token
        this.setTokens(response.data.accessToken, response.data.refreshToken)
        return response.data
      } else {
        throw new Error(response.message || '令牌刷新失败')
      }
    } catch (error) {
      console.error('令牌刷新失败:', error)
      // 刷新失败，清除认证信息
      this.clearAuthData()
      throw error
    }
  }

  /**
   * 获取当前用户信息
   */
  async getCurrentUser(): Promise<User> {
    try {
      const response: ApiResponse<User> = await api.get('/auth/me')
      
      if (response.success && response.data) {
        // 更新本地用户信息
        this.setUserInfo(response.data)
        return response.data
      } else {
        throw new Error(response.message || '获取用户信息失败')
      }
    } catch (error) {
      console.error('获取用户信息失败:', error)
      throw error
    }
  }

  /**
   * 验证令牌有效性
   */
  async validateToken(): Promise<boolean> {
    try {
      const response: ApiResponse<{ valid: boolean }> = await api.get('/auth/validate')
      return response.success && response.data?.valid === true
    } catch (error) {
      console.error('令牌验证失败:', error)
      return false
    }
  }

  /**
   * 修改密码
   */
  async changePassword(oldPassword: string, newPassword: string): Promise<void> {
    try {
      const response: ApiResponse = await api.post('/auth/change-password', {
        oldPassword,
        newPassword
      })

      if (!response.success) {
        throw new Error(response.message || '密码修改失败')
      }
    } catch (error) {
      console.error('密码修改失败:', error)
      throw error
    }
  }

  /**
   * 保存访问令牌和刷新令牌
   */
  private setTokens(accessToken: string, refreshToken: string): void {
    localStorage.setItem(this.TOKEN_KEY, accessToken)
    localStorage.setItem(this.REFRESH_TOKEN_KEY, refreshToken)
  }

  /**
   * 获取访问令牌
   */
  getAccessToken(): string | null {
    return localStorage.getItem(this.TOKEN_KEY)
  }

  /**
   * 获取刷新令牌
   */
  getRefreshToken(): string | null {
    return localStorage.getItem(this.REFRESH_TOKEN_KEY)
  }

  /**
   * 保存用户信息
   */
  private setUserInfo(user: User): void {
    localStorage.setItem(this.USER_KEY, JSON.stringify(user))
  }

  /**
   * 获取用户信息
   */
  getUserInfo(): User | null {
    try {
      const userStr = localStorage.getItem(this.USER_KEY)
      return userStr ? JSON.parse(userStr) : null
    } catch (error) {
      console.error('解析用户信息失败:', error)
      return null
    }
  }

  /**
   * 检查是否已登录
   */
  isAuthenticated(): boolean {
    return !!this.getAccessToken()
  }

  /**
   * 检查用户角色
   */
  hasRole(role: string): boolean {
    const user = this.getUserInfo()
    return user?.role === role
  }

  /**
   * 检查是否为管理员
   */
  isAdmin(): boolean {
    return this.hasRole('ADMIN')
  }

  /**
   * 清除认证数据
   */
  private clearAuthData(): void {
    localStorage.removeItem(this.TOKEN_KEY)
    localStorage.removeItem(this.REFRESH_TOKEN_KEY)
    localStorage.removeItem(this.USER_KEY)
  }

  /**
   * 自动刷新令牌
   */
  async autoRefreshToken(): Promise<void> {
    if (!this.isAuthenticated()) {
      return
    }

    try {
      await this.refreshToken()
    } catch (error) {
      console.error('自动刷新令牌失败:', error)
      // 跳转到登录页面
      window.location.href = '/login'
    }
  }
}

// 创建认证服务实例
const authService = new AuthService()

// 设置定时刷新令牌（每50分钟刷新一次，假设令牌有效期为1小时）
setInterval(() => {
  authService.autoRefreshToken()
}, 50 * 60 * 1000)

export default authService