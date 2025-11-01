/**
 * 文件上传响应DTO
 * @author AI Agent
 * @version 1.0.0
 * @created 2024-12-29 18:00:00
 */
package com.historyanalysis.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.historyanalysis.entity.UploadedFile;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 文件上传响应DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileUploadResponse {

    /**
     * 从UploadedFile实体创建响应对象的构造函数
     */
    public FileUploadResponse(UploadedFile uploadedFile) {
        this.id = Long.valueOf(uploadedFile.getId());
        this.fileName = uploadedFile.getFilename();
        this.originalFileName = uploadedFile.getFilename();
        this.fileType = uploadedFile.getFileType().name();
        this.fileSize = uploadedFile.getFileSize();
        this.formattedSize = formatFileSize(uploadedFile.getFileSize());
        this.status = "UPLOADED";
        this.processStatus = "PENDING";
        this.projectId = Long.valueOf(uploadedFile.getProject().getId());
        this.uploadTime = uploadedFile.getUploadedAt();
        this.canAnalyze = true;
    }

    /**
     * 格式化文件大小
     */
    private String formatFileSize(Long size) {
        if (size == null) return "0 B";
        if (size < 1024) return size + " B";
        if (size < 1024 * 1024) return String.format("%.1f KB", size / 1024.0);
        if (size < 1024 * 1024 * 1024) return String.format("%.1f MB", size / (1024.0 * 1024));
        return String.format("%.1f GB", size / (1024.0 * 1024 * 1024));
    }

    /**
     * 文件ID
     */
    private Long id;

    /**
     * 文件名
     */
    private String fileName;

    /**
     * 原始文件名
     */
    private String originalFileName;

    /**
     * 文件类型
     */
    private String fileType;

    /**
     * 文件大小（字节）
     */
    private Long fileSize;

    /**
     * 格式化的文件大小
     */
    private String formattedSize;

    /**
     * 文件状态
     */
    private String status;

    /**
     * 处理状态
     */
    private String processStatus;

    /**
     * 项目ID
     */
    private Long projectId;

    /**
     * 文件MD5哈希值
     */
    private String md5Hash;

    /**
     * 内容预览
     */
    private String contentPreview;

    /**
     * 上传时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime uploadTime;

    /**
     * 处理时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime processedTime;

    /**
     * 错误信息
     */
    private String errorMessage;

    /**
     * 下载URL
     */
    private String downloadUrl;

    /**
     * 是否可以分析
     */
    private Boolean canAnalyze;

    /**
     * 获取文件名（兼容性方法）
     */
    public String getFileName() {
        return this.fileName;
    }

    /**
     * 获取原始文件名（兼容性方法）
     */
    public String getOriginalFileName() {
        return this.originalFileName;
    }
}