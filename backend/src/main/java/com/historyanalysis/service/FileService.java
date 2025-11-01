/**
 * 文件服务接口
 * @author AI Agent
 * @version 1.0.0
 * @created 2024-12-29 18:00:00
 */
package com.historyanalysis.service;

import com.historyanalysis.entity.UploadedFile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 文件服务接口
 * 定义文件上传、处理、查询等业务操作
 */
public interface FileService {

    /**
     * 上传文件到项目
     */
    UploadedFile uploadFile(String projectId, String userId, MultipartFile file);

    /**
     * 批量上传文件
     */
    List<UploadedFile> uploadFiles(String projectId, String userId, List<MultipartFile> files);

    /**
     * 根据ID查找文件
     */
    Optional<UploadedFile> findById(String fileId);

    /**
     * 根据ID获取文件信息（用于API响应）
     */
    com.historyanalysis.dto.FileUploadResponse getFileById(Long fileId, org.springframework.security.core.Authentication authentication);

    /**
     * 获取项目文件列表（分页）
     */
    org.springframework.data.domain.Page<com.historyanalysis.dto.FileUploadResponse> getFiles(Long projectId, org.springframework.data.domain.Pageable pageable, org.springframework.security.core.Authentication authentication);

    /**
     * 删除文件
     */
    boolean deleteFile(String fileId, String userId);

    /**
     * 批量删除文件
     */
    int deleteFiles(List<String> fileIds, String userId);

    /**
     * 根据项目查找文件
     */
    Page<UploadedFile> findFilesByProject(String projectId, Pageable pageable);

    /**
     * 根据项目和文件类型查找文件
     */
    Page<UploadedFile> findFilesByProjectAndType(String projectId, UploadedFile.FileType fileType, Pageable pageable);

    /**
     * 根据文件名查找文件
     */
    Page<UploadedFile> findFilesByFilename(String projectId, String filename, Pageable pageable);

    /**
     * 查找最近上传的文件
     */
    List<UploadedFile> findRecentFiles(String projectId, int days, int limit);

    /**
     * 查找大文件
     */
    List<UploadedFile> findLargeFiles(String projectId, long minSize, int limit);

    /**
     * 查找小文件
     */
    List<UploadedFile> findSmallFiles(String projectId, long maxSize, int limit);

    /**
     * 查找已提取文本的文件
     */
    List<UploadedFile> findFilesWithExtractedText(String projectId);

    /**
     * 查找未提取文本的文件
     */
    List<UploadedFile> findFilesWithoutExtractedText(String projectId);

    /**
     * 统计项目文件数量
     */
    long countFilesByProject(String projectId);

    /**
     * 统计文件类型数量
     */
    long countFilesByType(String projectId, UploadedFile.FileType fileType);

    /**
     * 获取总文件数量
     */
    long getTotalFileCount();

    /**
     * 获取文件大小统计
     */
    Object[] getFileSizeStatistics(String projectId);

    /**
     * 获取文件类型统计
     */
    Map<UploadedFile.FileType, Long> getFileTypeStatistics(String projectId);

    /**
     * 获取用户文件统计
     */
    Object[] getUserFileStatistics(String userId);

    /**
     * 提取文件文本
     */
    boolean extractFileText(String fileId, String userId);

    /**
     * 批量提取文件文本
     */
    int extractAllFileTexts(String projectId, String userId);

    /**
     * 获取文件内容
     */
    String getFileContent(String fileId, String userId);

    /**
     * 更新文件信息
     */
    UploadedFile updateFileInfo(String fileId, String userId, String filename, String extractedText);

    /**
     * 检查文件访问权限
     */
    boolean hasFileAccess(String fileId, String userId);

    /**
     * 验证文件类型
     */
    boolean isValidFileType(MultipartFile file);

    /**
     * 验证文件大小
     */
    boolean isValidFileSize(MultipartFile file, long maxSize);

    /**
     * 获取文件下载路径
     */
    String getFileDownloadPath(String fileId, String userId);

    /**
     * 清理未处理的文件
     */
    int cleanupUnprocessedFiles(LocalDateTime beforeDate);

    /**
     * 搜索文件
     */
    Page<UploadedFile> searchFiles(String projectId, String keyword, Pageable pageable);

    /**
     * 获取项目文件概览
     */
    Map<String, Object> getProjectFileOverview(String projectId, String userId);
}