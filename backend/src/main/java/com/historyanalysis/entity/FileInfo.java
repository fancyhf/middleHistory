/**
 * 文件信息实体类
 * @author AI Agent
 * @version 1.0.0
 * @created 2024-12-29 18:00:00
 */
package com.historyanalysis.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

/**
 * 文件信息实体类
 * 用于管理上传的文件信息
 */
@Entity
@Table(name = "files")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class FileInfo {

    /**
     * 文件ID - 主键
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /**
     * 文件名
     */
    @NotBlank(message = "文件名不能为空")
    @Size(max = 255, message = "文件名长度不能超过255个字符")
    @Column(name = "filename", nullable = false, length = 255)
    private String filename;

    /**
     * 原始文件名
     */
    @NotBlank(message = "原始文件名不能为空")
    @Size(max = 255, message = "原始文件名长度不能超过255个字符")
    @Column(name = "original_filename", nullable = false, length = 255)
    private String originalFilename;

    /**
     * 文件路径
     */
    @NotBlank(message = "文件路径不能为空")
    @Column(name = "file_path", nullable = false, length = 500)
    private String filePath;

    /**
     * 文件类型
     */
    @NotBlank(message = "文件类型不能为空")
    @Column(name = "file_type", nullable = false, length = 50)
    private String fileType;

    /**
     * MIME类型
     */
    @Column(name = "mime_type", length = 100)
    private String mimeType;

    /**
     * 文件大小（字节）
     */
    @NotNull(message = "文件大小不能为空")
    @Column(name = "file_size", nullable = false)
    private Long fileSize;

    /**
     * 内容哈希
     */
    @Column(name = "content_hash", length = 64)
    private String contentHash;

    /**
     * 文本内容
     */
    @Column(name = "text_content", columnDefinition = "LONGTEXT")
    private String textContent;

    /**
     * 文本长度
     */
    @Column(name = "text_length")
    private Integer textLength = 0;

    /**
     * 文件编码
     */
    @Column(name = "encoding", length = 20)
    private String encoding = "UTF-8";

    /**
     * 处理状态
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private FileStatus status = FileStatus.UPLOADED;

    /**
     * 项目ID
     */
    @NotNull(message = "项目ID不能为空")
    @Column(name = "project_id", nullable = false)
    private Long projectId;

    /**
     * 所属项目 - 多对一关联
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", insertable = false, updatable = false)
    private Project project;

    /**
     * 上传用户ID
     */
    @NotNull(message = "上传用户ID不能为空")
    @Column(name = "user_id", nullable = false)
    private Long userId;

    /**
     * 上传用户 - 多对一关联
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private User user;

    /**
     * 错误信息
     */
    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    /**
     * 文件元数据
     */
    @Column(name = "metadata", columnDefinition = "JSON")
    private String metadata;

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
     * 文件状态枚举
     */
    public enum FileStatus {
        UPLOADED("已上传"),
        PROCESSING("处理中"),
        PROCESSED("已处理"),
        ERROR("处理失败"),
        DELETED("已删除");

        private final String description;

        FileStatus(String description) {
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
     * 获取文件扩展名
     */
    public String getFileExtension() {
        if (originalFilename == null || !originalFilename.contains(".")) {
            return "";
        }
        return originalFilename.substring(originalFilename.lastIndexOf(".") + 1).toLowerCase();
    }

    /**
     * 获取格式化的文件大小
     * @return 格式化的文件大小字符串
     */
    public String getFormattedFileSize() {
        if (fileSize == null) return "0 B";
        
        long size = fileSize;
        String[] units = {"B", "KB", "MB", "GB", "TB"};
        int unitIndex = 0;
        
        while (size >= 1024 && unitIndex < units.length - 1) {
            size /= 1024;
            unitIndex++;
        }
        
        return String.format("%.1f %s", (double) size, units[unitIndex]);
    }

    // 手动添加getter和setter方法以确保编译通过
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getFilename() { return filename; }
    public void setFilename(String filename) { this.filename = filename; }
    
    public String getOriginalFilename() { return originalFilename; }
    public void setOriginalFilename(String originalFilename) { this.originalFilename = originalFilename; }
    
    public String getFilePath() { return filePath; }
    public void setFilePath(String filePath) { this.filePath = filePath; }
    
    public String getFileType() { return fileType; }
    public void setFileType(String fileType) { this.fileType = fileType; }
    
    public String getMimeType() { return mimeType; }
    public void setMimeType(String mimeType) { this.mimeType = mimeType; }
    
    public Long getFileSize() { return fileSize; }
    public void setFileSize(Long fileSize) { this.fileSize = fileSize; }
    
    public String getContentHash() { return contentHash; }
    public void setContentHash(String contentHash) { this.contentHash = contentHash; }
    
    public String getTextContent() { return textContent; }
    public void setTextContent(String textContent) { this.textContent = textContent; }
    
    public Integer getTextLength() { return textLength; }
    public void setTextLength(Integer textLength) { this.textLength = textLength; }
    
    public String getEncoding() { return encoding; }
    public void setEncoding(String encoding) { this.encoding = encoding; }
    
    public FileStatus getStatus() { return status; }
    public void setStatus(FileStatus status) { this.status = status; }
    
    public Long getProjectId() { return projectId; }
    public void setProjectId(Long projectId) { this.projectId = projectId; }
    
    public Project getProject() { return project; }
    public void setProject(Project project) { this.project = project; }
    
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    
    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
    
    public String getMetadata() { return metadata; }
    public void setMetadata(String metadata) { this.metadata = metadata; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}