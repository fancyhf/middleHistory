/**
 * 用户实体类
 * @author AI Agent
 * @version 1.0.0
 * @created 2024-12-29 18:00:00
 */
package com.historyanalysis.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;

/**
 * 用户实体类
 * 实现Spring Security的UserDetails接口以支持认证和授权
 */
@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class User implements UserDetails {

    /**
     * 用户ID - 主键
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /**
     * 用户名 - 唯一标识
     */
    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 50, message = "用户名长度必须在3-50个字符之间")
    @Column(name = "username", unique = true, nullable = false, length = 50)
    private String username;

    /**
     * 邮箱地址 - 唯一标识
     */
    @NotBlank(message = "邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    @Column(name = "email", unique = true, nullable = false, length = 100)
    private String email;

    /**
     * 密码哈希值
     */
    @NotBlank(message = "密码不能为空")
    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    /**
     * 用户角色
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 20)
    private UserRole role = UserRole.USER;

    /**
     * 账户状态
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private UserStatus status = UserStatus.ACTIVE;

    /**
     * 真实姓名
     */
    @Column(name = "real_name", length = 100)
    private String realName;

    /**
     * 头像URL
     */
    @Column(name = "avatar_url", length = 500)
    private String avatarUrl;

    /**
     * 最后登录时间
     */
    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    /**
     * 最后登录IP
     */
    @Column(name = "last_login_ip", length = 45)
    private String lastLoginIp;

    /**
     * 创建时间
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * 用户角色枚举
     */
    public enum UserRole {
        ADMIN("管理员"),
        USER("普通用户");

        private final String description;

        UserRole(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * 用户状态枚举
     */
    public enum UserStatus {
        ACTIVE("激活"),
        INACTIVE("未激活"),
        LOCKED("锁定"),
        DELETED("已删除");

        private final String description;

        UserStatus(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * JPA生命周期回调 - 创建前
     */
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * JPA生命周期回调 - 更新前
     */
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // Spring Security UserDetails接口实现

    /**
     * 获取用户权限
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    /**
     * 获取密码
     */
    @Override
    public String getPassword() {
        return passwordHash;
    }

    /**
     * 获取用户名
     */
    @Override
    public String getUsername() {
        return username;
    }

    /**
     * 账户是否未过期
     */
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    /**
     * 账户是否未锁定
     */
    @Override
    public boolean isAccountNonLocked() {
        return status != UserStatus.LOCKED;
    }

    /**
     * 凭证是否未过期
     */
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    /**
     * 账户是否启用
     */
    @Override
    public boolean isEnabled() {
        return status == UserStatus.ACTIVE;
    }

    // 手动添加getId方法以确保编译通过
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    /**
     * 获取用户角色
     */
    public UserRole getRole() {
        return role;
    }

    // 手动添加setter方法
    public void setUsername(String username) {
        this.username = username;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public void setRealName(String realName) {
        this.realName = realName;
    }

    public void setRole(UserRole role) {
        this.role = role;
    }

    public void setStatus(UserStatus status) {
        this.status = status;
    }

    public String getEmail() {
        return email;
    }

    public String getRealName() {
        return realName;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public LocalDateTime getLastLoginAt() {
        return lastLoginAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public UserStatus getStatus() {
        return status;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }


}