/**
 * 分析结果实体类
 * @author AI Agent
 * @version 1.0.0
 * @created 2024-12-29 18:00:00
 */
package com.historyanalysis.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

/**
 * 分析结果实体类
 * 用于存储各种类型的分析结果
 */
@Entity
@Table(name = "analysis_results")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class AnalysisResult {

    /**
     * 分析结果ID - 主键
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /**
     * 分析类型
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "analysis_type", nullable = false, length = 30)
    private AnalysisType analysisType;

    /**
     * 分析状态
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private AnalysisStatus status = AnalysisStatus.PENDING;

    /**
     * 文件ID列表 - JSON格式存储
     */
    @Column(name = "file_ids", columnDefinition = "JSON")
    private String fileIds;

    /**
     * 项目ID
     */
    @NotNull(message = "项目ID不能为空")
    @Column(name = "project_id", nullable = false)
    private Long projectId;

    /**
     * 关联项目 - 多对一关联
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", insertable = false, updatable = false)
    private Project project;

    /**
     * 用户ID
     */
    @NotNull(message = "用户ID不能为空")
    @Column(name = "user_id", nullable = false)
    private Long userId;

    /**
     * 关联用户 - 多对一关联
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private User user;

    /**
     * 分析参数 - JSON格式存储
     */
    @Column(name = "parameters", columnDefinition = "JSON")
    private String parameters;

    /**
     * 分析结果 - JSON格式存储
     */
    @Column(name = "result_data", columnDefinition = "JSON")
    private String resultData;

    /**
     * 分析进度（0-100）
     */
    @Column(name = "progress")
    private Integer progress = 0;

    /**
     * 错误信息
     */
    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    /**
     * 执行时间（毫秒）
     */
    @Column(name = "execution_time")
    private Integer executionTime;

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
     * 完成时间
     */
    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    /**
     * 处理时间（毫秒）
     */
    @Column(name = "processing_time")
    private Long processingTime;

    /**
     * 开始时间
     */
    @Column(name = "started_at")
    private LocalDateTime startedAt;

    /**
     * 分析类型枚举
     */
    public enum AnalysisType {
        WORD_FREQUENCY("词频分析"),
        TIMELINE("时间轴分析"),
        GEOGRAPHY("地理分析"),
        TEXT_SUMMARY("文本摘要"),
        MULTIDIMENSIONAL("多维分析");

        private final String description;

        AnalysisType(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * 分析状态枚举
     */
    public enum AnalysisStatus {
        PENDING("待处理"),
        PROCESSING("处理中"),
        COMPLETED("已完成"),
        FAILED("失败");

        private final String description;

        AnalysisStatus(String description) {
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
     * 开始分析
     */
    public void startAnalysis() {
        this.status = AnalysisStatus.PROCESSING;
        this.progress = 0;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 更新分析进度
     */
    public void updateProgress(int progress) {
        this.progress = Math.max(0, Math.min(100, progress));
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 完成分析
     */
    public void completeAnalysis() {
        this.status = AnalysisStatus.COMPLETED;
        this.progress = 100;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 分析失败
     */
    public void failAnalysis(String errorMessage) {
        this.status = AnalysisStatus.FAILED;
        this.errorMessage = errorMessage;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 检查分析是否完成
     */
    public boolean isCompleted() {
        return status == AnalysisStatus.COMPLETED || status == AnalysisStatus.FAILED;
    }

    /**
     * 检查分析是否成功
     */
    public boolean isSuccessful() {
        return status == AnalysisStatus.COMPLETED && errorMessage == null;
    }





    /**
     * 获取分析描述
     * 从parameters中提取description字段
     */
    @SuppressWarnings("unchecked")
    public String getDescription() {
        if (parameters == null || parameters.trim().isEmpty()) {
            return null;
        }
        
        try {
            // 解析JSON参数
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            java.util.Map<String, Object> paramMap = mapper.readValue(parameters, java.util.Map.class);
            Object description = paramMap.get("description");
            return description != null ? description.toString() : null;
        } catch (Exception e) {
            // 如果解析失败，返回null
            return null;
        }
    }

    // 手动添加必要的getter/setter方法以解决Lombok编译问题
    public String getFileIds() {
        return fileIds;
    }

    public void setFileIds(String fileIds) {
        this.fileIds = fileIds;
    }

    public String getResultData() {
        return resultData;
    }

    public void setResultData(String resultData) {
        this.resultData = resultData;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public AnalysisType getAnalysisType() {
        return analysisType;
    }

    public void setAnalysisType(AnalysisType analysisType) {
        this.analysisType = analysisType;
    }

    public AnalysisStatus getStatus() {
        return status;
    }

    public void setStatus(AnalysisStatus status) {
        this.status = status;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getParameters() {
        return parameters;
    }

    public void setParameters(String parameters) {
        this.parameters = parameters;
    }

    public Integer getProgress() {
        return progress;
    }

    public void setProgress(Integer progress) {
        this.progress = progress;
    }

    public Integer getExecutionTime() {
        return executionTime;
    }

    public void setExecutionTime(Integer executionTime) {
        this.executionTime = executionTime;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public LocalDateTime getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(LocalDateTime startedAt) {
        this.startedAt = startedAt;
    }

    public Long getProcessingTime() {
        return processingTime;
    }

    public void setProcessingTime(Long processingTime) {
        this.processingTime = processingTime;
    }
}