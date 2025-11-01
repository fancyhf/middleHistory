/**
 * 项目实体类
 * @author AI Agent
 * @version 1.0.0
 * @created 2024-12-29 18:00:00
 */
package com.historyanalysis.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

/**
 * 项目实体类
 * 用于管理用户的分析项目
 */
@Entity
@Table(name = "projects")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class Project {

    /**
     * 项目ID - 主键
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /**
     * 项目名称
     */
    @NotBlank(message = "项目名称不能为空")
    @Size(max = 200, message = "项目名称长度不能超过200个字符")
    @Column(name = "name", nullable = false, length = 200)
    private String name;

    /**
     * 项目描述
     */
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    /**
     * 项目状态
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private ProjectStatus status = ProjectStatus.ACTIVE;

    /**
     * 项目所有者ID
     */
    @Column(name = "user_id", nullable = false)
    private Long userId;

    /**
     * 项目所有者 - 多对一关联
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private User user;

    /**
     * 文件数量
     */
    @Column(name = "file_count", nullable = false)
    private Integer fileCount = 0;

    /**
     * 分析数量
     */
    @Column(name = "analysis_count", nullable = false)
    private Integer analysisCount = 0;

    /**
     * 项目标签 - 逗号分隔的标签字符串
     */
    @Column(name = "tags", length = 500)
    private String tags;

    /**
     * 项目设置 - JSON格式存储
     */
    @Column(name = "settings", columnDefinition = "JSON")
    private String settings;

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
     * 项目状态枚举
     */
    public enum ProjectStatus {
        ACTIVE("活跃"),
        ARCHIVED("已归档"),
        DELETED("已删除");

        private final String description;

        ProjectStatus(String description) {
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

    /**
     * 增加文件数量
     */
    public void incrementFileCount() {
        this.fileCount = (this.fileCount == null ? 0 : this.fileCount) + 1;
    }

    /**
     * 减少文件数量
     */
    public void decrementFileCount() {
        this.fileCount = Math.max(0, (this.fileCount == null ? 0 : this.fileCount) - 1);
    }

    /**
     * 增加分析数量
     */
    public void incrementAnalysisCount() {
        this.analysisCount = (this.analysisCount == null ? 0 : this.analysisCount) + 1;
    }

    /**
     * 减少分析数量
     */
    public void decrementAnalysisCount() {
        this.analysisCount = Math.max(0, (this.analysisCount == null ? 0 : this.analysisCount) - 1);
    }

    /**
     * 获取项目ID
     */
    public Long getId() {
        return id;
    }

    /**
     * 获取用户ID
     */
    public Long getUserId() {
        return userId;
    }

    /**
     * 获取项目名称
     */
    public String getName() {
        return name;
    }

    /**
     * 获取用户
     */
    public User getUser() {
        return user;
    }

    /**
     * 获取项目状态
     */
    public ProjectStatus getStatus() {
        return status;
    }

    /**
     * 获取项目描述
     */
    public String getDescription() {
        return description;
    }

    /**
     * 获取项目标签
     */
    public String getTags() {
        return tags;
    }

    /**
     * 获取项目设置
     */
    public String getSettings() {
        return settings;
    }

    /**
     * 获取文件数量
     */
    public Integer getFileCount() {
        return fileCount;
    }

    /**
     * 获取分析数量
     */
    public Integer getAnalysisCount() {
        return analysisCount;
    }

    /**
     * 获取创建时间
     */
    public LocalDateTime getCreateTime() {
        return createdAt;
    }

    /**
     * 获取更新时间
     */
    public LocalDateTime getUpdateTime() {
        return updatedAt;
    }

    /**
     * 设置项目名称
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * 设置项目描述
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * 设置项目设置
     */
    public void setSettings(String settings) {
        this.settings = settings;
    }

    /**
     * 设置项目标签
     */
    public void setTags(String tags) {
        this.tags = tags;
    }

    /**
     * 设置项目状态
     */
    public void setStatus(ProjectStatus status) {
        this.status = status;
    }

    /**
     * 设置用户
     */
    public void setUser(User user) {
        this.user = user;
        if (user != null) {
            this.userId = user.getId();
        }
    }

    /**
     * 设置用户ID
     */
    public void setUserId(Long userId) {
        this.userId = userId;
    }

    /**
     * 设置文件数量
     */
    public void setFileCount(Integer fileCount) {
        this.fileCount = fileCount;
    }

    /**
     * 设置分析数量
     */
    public void setAnalysisCount(Integer analysisCount) {
        this.analysisCount = analysisCount;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createdAt = createTime;
    }

    public void setUpdateTime(LocalDateTime updateTime) {
        this.updatedAt = updateTime;
    }

    /**
     * 获取项目标题（别名为name）
     */
    public String getTitle() {
        return this.name;
    }

    /**
     * 设置项目标题（别名为name）
     */
    public void setTitle(String title) {
        this.name = title;
    }
}