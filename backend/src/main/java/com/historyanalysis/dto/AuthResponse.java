/**
 * 认证响应DTO
 * @author AI Agent
 * @version 1.0.0
 * @created 2024-12-29 18:00:00
 */
package com.historyanalysis.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 用户认证响应DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {

    /**
     * 访问令牌
     */
    private String accessToken;

    /**
     * 刷新令牌
     */
    private String refreshToken;

    /**
     * 令牌类型
     */
    @Builder.Default
    private String tokenType = "Bearer";

    /**
     * 访问令牌过期时间（秒）
     */
    private Long expiresIn;

    /**
     * 用户信息
     */
    private UserInfo user;

    // 手动添加setter方法
    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }

    public void setExpiresIn(Long expiresIn) {
        this.expiresIn = expiresIn;
    }

    public void setUser(UserInfo user) {
        this.user = user;
    }

    /**
     * 用户信息内部类
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserInfo {
        
        /**
         * 用户ID
         */
        private Long id;

        /**
         * 用户名
         */
        private String username;

        /**
         * 邮箱
         */
        private String email;

        /**
         * 真实姓名
         */
        private String realName;

        /**
         * 头像URL
         */
        private String avatarUrl;

        /**
         * 用户角色
         */
        private String role;

        /**
         * 用户状态
         */
        private String status;

        /**
         * 最后登录时间
         */
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime lastLoginTime;

        /**
         * 创建时间
         */
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime createdAt;

        // 手动添加setter方法
        public void setId(Long id) {
            this.id = id;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public void setRealName(String realName) {
            this.realName = realName;
        }

        public void setAvatarUrl(String avatarUrl) {
            this.avatarUrl = avatarUrl;
        }

        public void setRole(String role) {
            this.role = role;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public void setLastLoginTime(LocalDateTime lastLoginTime) {
            this.lastLoginTime = lastLoginTime;
        }

        public void setCreatedAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
        }
    }
}