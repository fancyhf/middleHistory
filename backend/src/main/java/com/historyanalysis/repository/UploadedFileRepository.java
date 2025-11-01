/**
 * 上传文件数据访问接口
 * @author AI Agent
 * @version 1.0.0
 * @created 2024-12-29 15:45:00
 * @description 上传文件实体的数据访问层接口，提供文件相关的数据库操作
 */
package com.historyanalysis.repository;

import com.historyanalysis.entity.Project;
import com.historyanalysis.entity.UploadedFile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 上传文件数据访问接口
 * 
 * 提供上传文件相关的数据库操作：
 * - 基础CRUD操作（继承自JpaRepository）
 * - 根据项目查询文件
 * - 根据文件类型查询文件
 * - 文件统计查询
 * - 文件大小查询
 */
@Repository
public interface UploadedFileRepository extends JpaRepository<UploadedFile, String> {

    /**
     * 根据项目查找文件列表
     * 
     * @param project 所属项目
     * @return 项目的文件列表
     */
    List<UploadedFile> findByProject(Project project);

    /**
     * 根据项目ID查找文件列表
     * 
     * @param projectId 项目ID
     * @return 项目的文件列表
     */
    @Query("SELECT uf FROM UploadedFile uf WHERE uf.project.id = :projectId")
    List<UploadedFile> findByProjectId(@Param("projectId") Long projectId);

    /**
     * 根据项目ID和文件名查找文件
     * 
     * @param projectId 项目ID
     * @param filename 文件名
     * @return 文件信息的Optional包装
     */
    @Query("SELECT uf FROM UploadedFile uf WHERE uf.project.id = :projectId AND uf.filename = :filename")
    Optional<UploadedFile> findByProjectIdAndFilename(@Param("projectId") Long projectId, @Param("filename") String filename);

    /**
     * 根据文件类型查找文件列表
     * 
     * @param fileType 文件类型
     * @return 指定类型的文件列表
     */
    List<UploadedFile> findByFileType(UploadedFile.FileType fileType);

    /**
     * 根据文件名模糊查询文件
     * 
     * @param filename 文件名关键字
     * @return 匹配的文件列表
     */
    List<UploadedFile> findByFilenameContainingIgnoreCase(String filename);

    /**
     * 查找指定时间之后上传的文件
     * 
     * @param uploadedAfter 上传时间下限
     * @return 文件列表
     */
    List<UploadedFile> findByUploadedAtAfter(LocalDateTime uploadedAfter);

    /**
     * 根据项目查找最近上传的文件
     * 
     * @param project 所属项目
     * @param pageable 分页参数
     * @return 最近上传的文件列表
     */
    @Query("SELECT f FROM UploadedFile f WHERE f.project = :project ORDER BY f.uploadedAt DESC")
    List<UploadedFile> findRecentFilesByProject(@Param("project") Project project, Pageable pageable);

    /**
     * 根据项目ID查找最近上传的文件
     * 
     * @param projectId 项目ID
     * @param pageable 分页参数
     * @return 最近上传的文件列表
     */
    @Query("SELECT f FROM UploadedFile f WHERE f.project.id = :projectId ORDER BY f.uploadedAt DESC")
    List<UploadedFile> findRecentFilesByProjectId(@Param("projectId") String projectId, Pageable pageable);

    /**
     * 统计项目的文件数量
     * 
     * @param project 所属项目
     * @return 文件数量
     */
    long countByProject(Project project);

    /**
     * 统计项目的文件数量
     * 
     * @param projectId 项目ID
     * @return 文件数量
     */
    @Query("SELECT COUNT(uf) FROM UploadedFile uf WHERE uf.project.id = :projectId")
    long countByProjectId(@Param("projectId") Long projectId);

    /**
     * 统计指定类型的文件数量
     * 
     * @param fileType 文件类型
     * @return 文件数量
     */
    long countByFileType(UploadedFile.FileType fileType);

    /**
     * 统计总文件数量
     * 
     * @return 总文件数量
     */
    @Query("SELECT COUNT(f) FROM UploadedFile f")
    long countTotalFiles();

    /**
     * 查找指定时间范围内上传的文件
     * 
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 文件列表
     */
    @Query("SELECT f FROM UploadedFile f WHERE f.uploadedAt BETWEEN :startTime AND :endTime ORDER BY f.uploadedAt DESC")
    List<UploadedFile> findFilesByUploadedAtBetween(@Param("startTime") LocalDateTime startTime, 
                                                   @Param("endTime") LocalDateTime endTime);

    /**
     * 查找已提取文本的文件
     * 
     * @return 已提取文本的文件列表
     */
    @Query("SELECT f FROM UploadedFile f WHERE f.extractedText IS NOT NULL AND f.extractedText != ''")
    List<UploadedFile> findFilesWithExtractedText();

    /**
     * 查找未提取文本的文件
     * 
     * @return 未提取文本的文件列表
     */
    @Query("SELECT f FROM UploadedFile f WHERE f.extractedText IS NULL OR f.extractedText = ''")
    List<UploadedFile> findFilesWithoutExtractedText();

    /**
     * 根据文件大小范围查找文件
     * 
     * @param minSize 最小文件大小（字节）
     * @param maxSize 最大文件大小（字节）
     * @return 文件列表
     */
    @Query("SELECT f FROM UploadedFile f WHERE f.fileSize BETWEEN :minSize AND :maxSize ORDER BY f.fileSize DESC")
    List<UploadedFile> findFilesBySizeRange(@Param("minSize") Long minSize, @Param("maxSize") Long maxSize);

    /**
     * 查找大文件（超过指定大小）
     * 
     * @param minSize 最小文件大小（字节）
     * @return 大文件列表
     */
    @Query("SELECT f FROM UploadedFile f WHERE f.fileSize >= :minSize ORDER BY f.fileSize DESC")
    List<UploadedFile> findLargeFiles(@Param("minSize") Long minSize);

    /**
     * 查找小文件（小于指定大小）
     * 
     * @param maxSize 最大文件大小（字节）
     * @return 小文件列表
     */
    @Query("SELECT f FROM UploadedFile f WHERE f.fileSize <= :maxSize ORDER BY f.fileSize ASC")
    List<UploadedFile> findSmallFiles(@Param("maxSize") Long maxSize);

    /**
     * 获取文件统计信息
     * 
     * @return 文件总数
     */
    @Query("SELECT COUNT(f) FROM UploadedFile f")
    Long getFileCount();

    /**
     * 获取文件大小统计信息
     * 
     * @return 文件大小统计信息数组 [总大小, 平均大小, 最大大小, 最小大小]
     */
    @Query("SELECT SUM(f.fileSize), AVG(f.fileSize), MAX(f.fileSize), MIN(f.fileSize) FROM UploadedFile f")
    List<Object[]> getFileSizeStatistics();

    /**
     * 根据项目和文件类型查找文件
     * 
     * @param project 所属项目
     * @param fileType 文件类型
     * @return 文件列表
     */
    List<UploadedFile> findByProjectAndFileType(Project project, UploadedFile.FileType fileType);

    /**
     * 根据项目ID和文件类型查找文件
     * 
     * @param projectId 项目ID
     * @param fileType 文件类型
     * @return 文件列表
     */
    @Query("SELECT uf FROM UploadedFile uf WHERE uf.project.id = :projectId AND uf.fileType = :fileType")
    List<UploadedFile> findByProjectIdAndFileType(@Param("projectId") Long projectId, @Param("fileType") UploadedFile.FileType fileType);

    /**
     * 查找最近上传的文件
     * 
     * @param pageable 分页参数
     * @return 最近上传的文件列表
     */
    @Query("SELECT f FROM UploadedFile f ORDER BY f.uploadedAt DESC")
    List<UploadedFile> findRecentFiles(Pageable pageable);

    /**
     * 查找最大的文件
     * 
     * @param pageable 分页参数
     * @return 最大的文件列表
     */
    @Query("SELECT f FROM UploadedFile f ORDER BY f.fileSize DESC")
    List<UploadedFile> findLargestFiles(Pageable pageable);

    /**
     * 根据用户查找文件（通过项目关联）
     * 
     * @param userId 用户ID
     * @return 用户的文件列表
     */
    @Query("SELECT f FROM UploadedFile f WHERE f.project.user.id = :userId ORDER BY f.uploadedAt DESC")
    List<UploadedFile> findFilesByUserId(@Param("userId") Long userId);

    /**
     * 统计用户的文件数量（通过项目关联）
     * 
     * @param userId 用户ID
     * @return 文件数量
     */
    @Query("SELECT COUNT(f) FROM UploadedFile f WHERE f.project.user.id = :userId")
    long countFilesByUserId(@Param("userId") Long userId);

    /**
     * 删除指定时间之前上传且未处理的文件
     */
    @Modifying
    @Query("DELETE FROM UploadedFile f WHERE f.uploadedAt < :uploadedBefore AND (f.extractedText IS NULL OR f.extractedText = '')")
    void deleteUnprocessedFilesUploadedBefore(@Param("uploadedBefore") LocalDateTime uploadedBefore);

    // 添加缺少的方法

    /**
     * 根据项目ID查找已提取文本的文件
     */
    @Query("SELECT f FROM UploadedFile f WHERE f.project.id = :projectId AND f.extractedText IS NOT NULL AND f.extractedText != ''")
    List<UploadedFile> findByProjectIdAndExtractedTextIsNotNull(@Param("projectId") Long projectId);

    /**
     * 根据项目ID查找未提取文本的文件
     */
    @Query("SELECT f FROM UploadedFile f WHERE f.project.id = :projectId AND (f.extractedText IS NULL OR f.extractedText = '')")
    List<UploadedFile> findByProjectIdAndExtractedTextIsNull(@Param("projectId") Long projectId);

    /**
     * 根据项目ID和文件类型统计文件数量
     */
    @Query("SELECT COUNT(uf) FROM UploadedFile uf WHERE uf.project.id = :projectId AND uf.fileType = :fileType")
    long countByProjectIdAndFileType(@Param("projectId") Long projectId, @Param("fileType") UploadedFile.FileType fileType);

    /**
     * 获取项目文件大小统计
     */
    @Query("SELECT SUM(f.fileSize), AVG(f.fileSize), MAX(f.fileSize), MIN(f.fileSize) FROM UploadedFile f WHERE f.project.id = :projectId")
    List<Object[]> getFileSizeStatisticsByProject(@Param("projectId") Long projectId);

    /**
     * 获取文件类型统计（按项目）
     */
    @Query("SELECT f.fileType, COUNT(f) FROM UploadedFile f WHERE f.project.id = :projectId GROUP BY f.fileType")
    List<Object[]> getFileTypeStatistics(@Param("projectId") Long projectId);

    /**
     * 获取文件类型统计（全局）
     */
    @Query("SELECT f.fileType, COUNT(f) FROM UploadedFile f GROUP BY f.fileType")
    List<Object[]> getFileTypeStatistics();

    /**
     * 根据项目ID和关键词搜索文件
     */
    @Query("SELECT f FROM UploadedFile f WHERE f.project.id = :projectId AND " +
           "(LOWER(f.filename) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(f.extractedText) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    org.springframework.data.domain.Page<UploadedFile> searchByProjectIdAndKeyword(
        @Param("projectId") Long projectId, 
        @Param("keyword") String keyword, 
        org.springframework.data.domain.Pageable pageable);

    /**
     * 根据项目ID查找指定天数内的文件
     */
    @Query("SELECT f FROM UploadedFile f WHERE f.project.id = :projectId AND f.uploadedAt >= :afterDate ORDER BY f.uploadedAt DESC")
    List<UploadedFile> findByProjectIdAndUploadedAtAfter(@Param("projectId") Long projectId, @Param("afterDate") LocalDateTime afterDate);

    /**
     * 根据项目ID查找大文件
     */
    @Query("SELECT f FROM UploadedFile f WHERE f.project.id = :projectId AND f.fileSize >= :minSize ORDER BY f.fileSize DESC")
    List<UploadedFile> findByProjectIdAndFileSizeGreaterThanEqual(@Param("projectId") Long projectId, @Param("minSize") Long minSize);

    /**
     * 根据项目ID查找小文件
     */
    @Query("SELECT f FROM UploadedFile f WHERE f.project.id = :projectId AND f.fileSize <= :maxSize ORDER BY f.fileSize ASC")
    List<UploadedFile> findByProjectIdAndFileSizeLessThanEqual(@Param("projectId") Long projectId, @Param("maxSize") Long maxSize);

    /**
     * 根据项目ID和文件名模糊查询
     */
    @Query("SELECT f FROM UploadedFile f WHERE f.project.id = :projectId AND LOWER(f.filename) LIKE LOWER(CONCAT('%', :filename, '%'))")
    org.springframework.data.domain.Page<UploadedFile> findByProjectIdAndFilenameContaining(
        @Param("projectId") Long projectId, 
        @Param("filename") String filename, 
        org.springframework.data.domain.Pageable pageable);

    /**
     * 根据项目ID和文件类型分页查询
     */
    @Query("SELECT f FROM UploadedFile f WHERE f.project.id = :projectId AND f.fileType = :fileType")
    org.springframework.data.domain.Page<UploadedFile> findByProjectIdAndFileType(
        @Param("projectId") Long projectId, 
        @Param("fileType") UploadedFile.FileType fileType, 
        org.springframework.data.domain.Pageable pageable);

    /**
     * 根据项目ID分页查询
     */
    @Query("SELECT f FROM UploadedFile f WHERE f.project.id = :projectId")
    org.springframework.data.domain.Page<UploadedFile> findByProjectId(
        @Param("projectId") Long projectId, 
        org.springframework.data.domain.Pageable pageable);

    /**
     * 获取用户文件统计
     */
    @Query("SELECT COUNT(f), SUM(f.fileSize) FROM UploadedFile f WHERE f.project.user.id = :userId")
    List<Object[]> getUserFileStatistics(@Param("userId") Long userId);

    /**
     * 搜索文件（全局搜索）
     */
    @Query("SELECT f FROM UploadedFile f WHERE " +
           "f.filename LIKE %:keyword% OR " +
           "f.extractedText LIKE %:keyword%")
    Page<UploadedFile> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);



    /**
     * 根据文件名模糊查询（忽略大小写）
     */
    @Query("SELECT f FROM UploadedFile f WHERE LOWER(f.filename) LIKE LOWER(CONCAT('%', :filename, '%'))")
    Page<UploadedFile> findByFilenameContainingIgnoreCase(@Param("filename") String filename, Pageable pageable);

    /**
     * 查询最近文件（带分页）
     */
    @Query("SELECT f FROM UploadedFile f WHERE f.uploadedAt >= :afterDate ORDER BY f.uploadedAt DESC")
    List<UploadedFile> findRecentFiles(@Param("afterDate") LocalDateTime afterDate, Pageable pageable);

    /**
     * 查询大文件（带分页）
     */
    @Query("SELECT f FROM UploadedFile f WHERE f.fileSize >= :minSize ORDER BY f.fileSize DESC")
    List<UploadedFile> findLargeFiles(@Param("minSize") Long minSize, Pageable pageable);

    /**
     * 查询小文件（带分页）
     */
    @Query("SELECT f FROM UploadedFile f WHERE f.fileSize <= :maxSize ORDER BY f.fileSize ASC")
    List<UploadedFile> findSmallFiles(@Param("maxSize") Long maxSize, Pageable pageable);

    /**
     * 根据项目ID查找指定天数内的文件（带分页）
     */
    @Query("SELECT f FROM UploadedFile f WHERE f.project.id = :projectId AND f.uploadedAt >= :afterDate ORDER BY f.uploadedAt DESC")
    Page<UploadedFile> findByProjectIdAndUploadedAtAfter(@Param("projectId") Long projectId, @Param("afterDate") LocalDateTime afterDate, Pageable pageable);

    /**
     * 根据项目ID查找大文件（带分页）
     */
    @Query("SELECT f FROM UploadedFile f WHERE f.project.id = :projectId AND f.fileSize >= :minSize ORDER BY f.fileSize DESC")
    Page<UploadedFile> findByProjectIdAndFileSizeGreaterThanEqual(@Param("projectId") Long projectId, @Param("minSize") Long minSize, Pageable pageable);

    /**
     * 根据项目ID查找小文件（带分页）
     */
    @Query("SELECT f FROM UploadedFile f WHERE f.project.id = :projectId AND f.fileSize <= :maxSize ORDER BY f.fileSize ASC")
    Page<UploadedFile> findByProjectIdAndFileSizeLessThanEqual(@Param("projectId") Long projectId, @Param("maxSize") Long maxSize, Pageable pageable);
}