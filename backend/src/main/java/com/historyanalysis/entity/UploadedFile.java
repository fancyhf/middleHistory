/**
 * 上传文件实体类
 * @author AI Agent
 * @version 1.0.0
 * @created 2024-12-29 15:30:00
 * @description 上传文件信息实体，包含文件基本信息和提取的文本内容
 */
package com.historyanalysis.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * 上传文件实体类
 * 
 * 包含上传文件的信息：
 * - 文件ID（主键）
 * - 所属项目
 * - 文件名
 * - 文件路径
 * - 文件类型
 * - 提取的文本内容
 * - 上传时间
 */
@Entity
@Table(name = "uploaded_files")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@EntityListeners(AuditingEntityListener.class)
public class UploadedFile {

    /**
     * 文件ID，主键，使用UUID策略生成
     */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", length = 36)
    private String id;

    /**
     * 文件所属项目
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    @JsonIgnore
    private Project project;

    /**
     * 原始文件名
     */
    @NotBlank(message = "文件名不能为空")
    @Size(max = 255, message = "文件名长度不能超过255个字符")
    @Column(name = "filename", nullable = false)
    private String filename;

    /**
     * 文件存储路径
     */
    @NotBlank(message = "文件路径不能为空")
    @Size(max = 500, message = "文件路径长度不能超过500个字符")
    @Column(name = "file_path", nullable = false, length = 500)
    private String filePath;

    /**
     * 文件类型枚举
     */
    public enum FileType {
        PDF("application/pdf", "PDF文档"),
        DOC("application/msword", "Word文档"),
        DOCX("application/vnd.openxmlformats-officedocument.wordprocessingml.document", "Word文档"),
        TXT("text/plain", "纯文本文件"),
        HTML("text/html", "HTML文件");

        private final String mimeType;
        private final String description;

        FileType(String mimeType, String description) {
            this.mimeType = mimeType;
            this.description = description;
        }

        public String getMimeType() {
            return mimeType;
        }

        public String getDescription() {
            return description;
        }

        /**
         * 根据MIME类型获取文件类型枚举
         * 
         * @param mimeType MIME类型
         * @return 对应的文件类型枚举，如果不支持则返回null
         */
        public static FileType fromMimeType(String mimeType) {
            for (FileType type : values()) {
                if (type.getMimeType().equals(mimeType)) {
                    return type;
                }
            }
            return null;
        }

        /**
         * 检查是否为支持的文件类型
         * 
         * @param mimeType MIME类型
         * @return 如果支持返回true，否则返回false
         */
        public static boolean isSupported(String mimeType) {
            return fromMimeType(mimeType) != null;
        }
    }

    /**
     * 文件类型
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "file_type", nullable = false, length = 50)
    private FileType fileType;

    /**
     * 从文件中提取的文本内容
     * 用于后续的NLP分析
     */
    @Column(name = "extracted_text", columnDefinition = "LONGTEXT")
    private String extractedText;

    /**
     * 文件大小（字节）
     */
    @Column(name = "file_size")
    private Long fileSize;

    /**
     * 文件上传时间，自动设置
     */
    @CreatedDate
    @Column(name = "uploaded_at", nullable = false, updatable = false)
    private LocalDateTime uploadedAt;

    /**
     * 构造函数 - 创建新的上传文件记录
     * 
     * @param project 所属项目
     * @param filename 文件名
     * @param filePath 文件路径
     * @param fileType 文件类型
     */
    public UploadedFile(Project project, String filename, String filePath, FileType fileType) {
        this.project = project;
        this.filename = filename;
        this.filePath = filePath;
        this.fileType = fileType;
    }

    /**
     * 构造函数 - 创建包含文件大小的上传文件记录
     * 
     * @param project 所属项目
     * @param filename 文件名
     * @param filePath 文件路径
     * @param fileType 文件类型
     * @param fileSize 文件大小
     */
    public UploadedFile(Project project, String filename, String filePath, FileType fileType, Long fileSize) {
        this.project = project;
        this.filename = filename;
        this.filePath = filePath;
        this.fileType = fileType;
        this.fileSize = fileSize;
    }

    /**
     * 获取项目ID
     * 
     * @return 项目ID
     */
    public Long getProjectId() {
        return project != null ? project.getId() : null;
    }

    /**
     * 检查是否已提取文本内容
     * 
     * @return 如果已提取文本返回true，否则返回false
     */
    public boolean hasExtractedText() {
        return extractedText != null && !extractedText.trim().isEmpty();
    }

    /**
     * 获取文件扩展名
     * 
     * @return 文件扩展名（不包含点号）
     */
    public String getFileExtension() {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
    }

    /**
     * 获取不包含扩展名的文件名
     * 
     * @return 不包含扩展名的文件名
     */
    public String getFilenameWithoutExtension() {
        if (filename == null || !filename.contains(".")) {
            return filename;
        }
        return filename.substring(0, filename.lastIndexOf("."));
    }

    /**
     * 格式化文件大小为可读字符串
     * 
     * @return 格式化的文件大小字符串
     */
    public String getFormattedFileSize() {
        if (fileSize == null) {
            return "未知";
        }
        
        if (fileSize < 1024) {
            return fileSize + " B";
        } else if (fileSize < 1024 * 1024) {
            return String.format("%.1f KB", fileSize / 1024.0);
        } else if (fileSize < 1024 * 1024 * 1024) {
            return String.format("%.1f MB", fileSize / (1024.0 * 1024.0));
        } else {
            return String.format("%.1f GB", fileSize / (1024.0 * 1024.0 * 1024.0));
        }
    }

    /**
     * 检查文件是否为文档类型
     * 
     * @return 如果是文档类型返回true，否则返回false
     */
    public boolean isDocumentType() {
        return fileType == FileType.PDF || fileType == FileType.DOC || fileType == FileType.DOCX;
    }

    /**
     * 检查文件是否为文本类型
     * 
     * @return 如果是文本类型返回true，否则返回false
     */
    public boolean isTextType() {
        return fileType == FileType.TXT || fileType == FileType.HTML;
    }

    // Getter and Setter methods for all fields
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public FileType getFileType() {
        return fileType;
    }

    public void setFileType(FileType fileType) {
        this.fileType = fileType;
    }

    public String getExtractedText() {
        return extractedText;
    }

    public void setExtractedText(String extractedText) {
        this.extractedText = extractedText;
    }

    public Long getFileSize() {
        return fileSize;
    }

    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }

    public LocalDateTime getUploadedAt() {
        return uploadedAt;
    }

    public void setUploadedAt(LocalDateTime uploadedAt) {
        this.uploadedAt = uploadedAt;
    }
}