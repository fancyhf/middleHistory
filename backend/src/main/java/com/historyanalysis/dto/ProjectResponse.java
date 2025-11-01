/**
 * 项目响应DTO
 * @author AI Agent
 * @version 1.0.0
 * @created 2024-12-29 18:06:00
 */
package com.historyanalysis.dto;

import com.historyanalysis.entity.Project;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 项目响应DTO
 */
@Data
public class ProjectResponse {

    /**
     * 项目ID
     */
    private Long id;

    /**
     * 项目名称
     */
    private String name;

    /**
     * 项目描述
     */
    private String description;

    /**
     * 项目标签
     */
    private String tags;

    /**
     * 项目状态
     */
    private Project.ProjectStatus status;

    /**
     * 项目设置
     */
    private String settings;

    /**
     * 文件数量
     */
    private Integer fileCount;

    /**
     * 分析数量
     */
    private Integer analysisCount;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 用户信息
     */
    private UserInfo user;

    /**
     * 用户信息内部类
     */
    @Data
    public static class UserInfo {
        private Long id;
        private String username;
        private String realName;
        private String avatarUrl;

        public void setId(Long id) {
            this.id = id;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public void setRealName(String realName) {
            this.realName = realName;
        }

        public void setAvatarUrl(String avatarUrl) {
            this.avatarUrl = avatarUrl;
        }
    }

    /**
     * 项目统计信息
     */
    @Data
    public static class ProjectStats {
        /**
         * 总文件大小（字节）
         */
        private Long totalFileSize;

        /**
         * 总文件大小（格式化）
         */
        private String totalFileSizeFormatted;

        /**
         * 总项目数量
         */
        private Integer totalProjectCount;

        /**
         * 总文件数量
         */
        private Integer totalFileCount;

        /**
         * 总分析数量
         */
        private Integer totalAnalysisCount;

        /**
         * 已完成分析数量
         */
        private Integer completedAnalysisCount;

        /**
         * 运行中分析数量
         */
        private Integer runningAnalysisCount;

        /**
         * 失败分析数量
         */
        private Integer failedAnalysisCount;

        /**
         * 最后上传时间
         */
        private LocalDateTime lastUploadTime;

        /**
         * 最后分析时间
         */
        private LocalDateTime lastAnalysisTime;

        // 手动添加setter方法以确保编译通过
        public void setTotalFileSize(Long totalFileSize) {
            this.totalFileSize = totalFileSize;
        }

        public Long getTotalFileSize() {
            return totalFileSize;
        }

        public void setCompletedAnalysisCount(Integer completedAnalysisCount) {
            this.completedAnalysisCount = completedAnalysisCount;
        }

        public void setTotalFileSizeFormatted(String totalFileSizeFormatted) {
            this.totalFileSizeFormatted = totalFileSizeFormatted;
        }

        public void setRunningAnalysisCount(int runningAnalysisCount) {
            this.runningAnalysisCount = runningAnalysisCount;
        }

        public void setFailedAnalysisCount(int failedAnalysisCount) {
            this.failedAnalysisCount = failedAnalysisCount;
        }

        public void setLastUploadTime(LocalDateTime lastUploadTime) {
            this.lastUploadTime = lastUploadTime;
        }

        public void setLastAnalysisTime(LocalDateTime lastAnalysisTime) {
            this.lastAnalysisTime = lastAnalysisTime;
        }
    }

    /**
     * 项目统计信息
     */
    private ProjectStats stats;

    // 手动添加setter方法
    public void setId(Long id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public void setStatus(Project.ProjectStatus status) {
        this.status = status;
    }

    public void setSettings(String settings) {
        this.settings = settings;
    }

    public void setFileCount(Integer fileCount) {
        this.fileCount = fileCount;
    }

    public void setAnalysisCount(Integer analysisCount) {
        this.analysisCount = analysisCount;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public void setStats(ProjectStats stats) {
        this.stats = stats;
    }

    public void setUser(UserInfo user) {
        this.user = user;
    }
    
    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }
}