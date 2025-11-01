/**
 * 文件信息数据访问层接口
 * @author AI Agent
 * @version 1.0.0
 * @created 2024-12-29 18:00:00
 */
package com.historyanalysis.repository;

import com.historyanalysis.entity.FileInfo;
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
 * 文件信息数据访问层接口
 * 提供文件相关的数据库操作方法
 */
@Repository
public interface FileInfoRepository extends JpaRepository<FileInfo, Long> {

    /**
     * 根据项目ID查找文件列表
     * 
     * @param projectId 项目ID
     * @param pageable 分页参数
     * @return 文件分页列表
     */
    Page<FileInfo> findByProjectId(Long projectId, Pageable pageable);

    /**
     * 根据用户ID查找文件列表
     * 
     * @param userId 用户ID
     * @param pageable 分页参数
     * @return 文件分页列表
     */
    Page<FileInfo> findByUserId(Long userId, Pageable pageable);

    /**
     * 根据项目ID和状态查找文件列表
     * 
     * @param projectId 项目ID
     * @param status 文件状态
     * @param pageable 分页参数
     * @return 文件分页列表
     */
    Page<FileInfo> findByProjectIdAndStatus(Long projectId, FileInfo.FileStatus status, Pageable pageable);

    /**
     * 根据文件类型查找文件列表
     * 
     * @param fileType 文件类型
     * @param pageable 分页参数
     * @return 文件分页列表
     */
    Page<FileInfo> findByFileType(String fileType, Pageable pageable);

    /**
     * 根据内容哈希查找文件
     * 
     * @param contentHash 内容哈希值
     * @return 文件信息
     */
    Optional<FileInfo> findByContentHash(String contentHash);

    /**
     * 检查内容哈希和用户ID是否存在
     * 
     * @param contentHash 内容哈希值
     * @param userId 用户ID
     * @return 是否存在
     */
    boolean existsByContentHashAndUserId(String contentHash, Long userId);

    /**
     * 根据项目ID和关键词搜索文件
     * 
     * @param projectId 项目ID
     * @param keyword 搜索关键词
     * @param pageable 分页参数
     * @return 文件分页列表
     */
    @Query("SELECT f FROM FileInfo f WHERE f.projectId = :projectId AND " +
           "(f.filename LIKE %:keyword% OR f.originalFilename LIKE %:keyword%)")
    Page<FileInfo> searchProjectFiles(@Param("projectId") Long projectId, 
                                    @Param("keyword") String keyword, 
                                    Pageable pageable);

    /**
     * 根据用户ID和关键词搜索文件
     * 
     * @param userId 用户ID
     * @param keyword 搜索关键词
     * @param pageable 分页参数
     * @return 文件分页列表
     */
    @Query("SELECT f FROM FileInfo f WHERE f.userId = :userId AND " +
           "(f.filename LIKE %:keyword% OR f.originalFilename LIKE %:keyword%)")
    Page<FileInfo> searchUserFiles(@Param("userId") Long userId, 
                                 @Param("keyword") String keyword, 
                                 Pageable pageable);

    /**
     * 根据关键词搜索文件
     * 
     * @param projectId 项目ID
     * @param keyword 搜索关键词
     * @param pageable 分页参数
     * @return 文件分页列表
     */
    @Query("SELECT f FROM FileInfo f WHERE f.projectId = :projectId AND (" +
           "f.filename LIKE %:keyword% OR " +
           "f.originalFilename LIKE %:keyword% OR " +
           "f.textContent LIKE %:keyword%)")
    Page<FileInfo> searchByKeyword(@Param("projectId") Long projectId, @Param("keyword") String keyword, Pageable pageable);

    /**
     * 统计项目文件总数
     * 
     * @param projectId 项目ID
     * @return 文件总数
     */
    @Query("SELECT COUNT(f) FROM FileInfo f WHERE f.projectId = :projectId AND f.status != 'DELETED'")
    Long countProjectFiles(@Param("projectId") Long projectId);

    /**
     * 根据项目ID和状态统计文件数量
     * 
     * @param projectId 项目ID
     * @param status 文件状态
     * @return 文件数量
     */
    Long countByProjectIdAndStatus(Long projectId, FileInfo.FileStatus status);

    /**
     * 统计用户文件总数
     * 
     * @param userId 用户ID
     * @return 文件总数
     */
    @Query("SELECT COUNT(f) FROM FileInfo f WHERE f.userId = :userId AND f.status != 'DELETED'")
    Long countUserFiles(@Param("userId") Long userId);

    /**
     * 根据用户ID统计文件数量
     * 
     * @param userId 用户ID
     * @return 文件数量
     */
    Long countByUserId(Long userId);

    /**
     * 根据用户ID统计文件总大小
     */
    @Query("SELECT COALESCE(SUM(f.fileSize), 0) FROM FileInfo f WHERE f.userId = :userId")
    Long sumFileSizeByUserId(@Param("userId") Long userId);

    /**
     * 根据项目ID统计文件总大小
     */
    @Query("SELECT COALESCE(SUM(f.fileSize), 0) FROM FileInfo f WHERE f.projectId = :projectId")
    Long sumFileSizeByProjectId(@Param("projectId") Long projectId);

    /**
     * 根据项目ID查找最后上传时间
     */
    @Query("SELECT MAX(f.createdAt) FROM FileInfo f WHERE f.projectId = :projectId")
    LocalDateTime findLastUploadTimeByProjectId(@Param("projectId") Long projectId);

    /**
     * 根据用户ID和状态统计文件数量
     * 
     * @param userId 用户ID
     * @param status 文件状态
     * @return 文件数量
     */
    Long countByUserIdAndStatus(Long userId, FileInfo.FileStatus status);

    /**
     * 统计项目文件总大小
     * 
     * @param projectId 项目ID
     * @return 文件总大小（字节）
     */
    @Query("SELECT COALESCE(SUM(f.fileSize), 0) FROM FileInfo f WHERE f.projectId = :projectId AND f.status != 'DELETED'")
    Long sumProjectFileSize(@Param("projectId") Long projectId);

    /**
     * 统计用户文件总大小
     * 
     * @param userId 用户ID
     * @return 文件总大小（字节）
     */
    @Query("SELECT COALESCE(SUM(f.fileSize), 0) FROM FileInfo f WHERE f.userId = :userId AND f.status != 'DELETED'")
    Long sumUserFileSize(@Param("userId") Long userId);

    /**
     * 根据文件类型统计数量
     * 
     * @param projectId 项目ID
     * @param fileType 文件类型
     * @return 文件数量
     */
    Long countByProjectIdAndFileType(Long projectId, String fileType);

    /**
     * 查找项目中最近上传的文件
     * 
     * @param projectId 项目ID
     * @param pageable 分页参数
     * @return 文件列表
     */
    @Query("SELECT f FROM FileInfo f WHERE f.projectId = :projectId AND f.status != 'DELETED' " +
           "ORDER BY f.createdAt DESC")
    List<FileInfo> findRecentProjectFiles(@Param("projectId") Long projectId, Pageable pageable);

    /**
     * 查找用户最近上传的文件
     * 
     * @param userId 用户ID
     * @param pageable 分页参数
     * @return 文件列表
     */
    @Query("SELECT f FROM FileInfo f WHERE f.userId = :userId AND f.status != 'DELETED' " +
           "ORDER BY f.createdAt DESC")
    List<FileInfo> findRecentUserFiles(@Param("userId") Long userId, Pageable pageable);

    /**
     * 统计今日上传的文件数量
     * 
     * @param startOfDay 今日开始时间
     * @param endOfDay 今日结束时间
     * @return 文件数量
     */
    @Query("SELECT COUNT(f) FROM FileInfo f WHERE f.createdAt BETWEEN :startOfDay AND :endOfDay")
    Long countTodayFiles(@Param("startOfDay") LocalDateTime startOfDay, 
                        @Param("endOfDay") LocalDateTime endOfDay);

    /**
     * 统计用户今日上传的文件数量
     * 
     * @param userId 用户ID
     * @param startOfDay 今日开始时间
     * @param endOfDay 今日结束时间
     * @return 文件数量
     */
    @Query("SELECT COUNT(f) FROM FileInfo f WHERE f.userId = :userId AND " +
           "f.createdAt BETWEEN :startOfDay AND :endOfDay")
    Long countUserTodayFiles(@Param("userId") Long userId,
                            @Param("startOfDay") LocalDateTime startOfDay, 
                            @Param("endOfDay") LocalDateTime endOfDay);

    /**
     * 查找处理失败的文件
     * 
     * @return 处理失败文件列表
     */
    @Query("SELECT f FROM FileInfo f WHERE f.status = 'ERROR' ORDER BY f.createdAt DESC")
    List<FileInfo> findFailedFiles();

    /**
     * 更新文件错误信息
     * 
     * @param fileId 文件ID
     * @param errorMessage 错误信息
     */
    @Modifying
    @Query("UPDATE FileInfo f SET f.errorMessage = :errorMessage, " +
           "f.updatedAt = CURRENT_TIMESTAMP WHERE f.id = :fileId")
    void updateErrorMessage(@Param("fileId") Long fileId, 
                           @Param("errorMessage") String errorMessage);

    /**
     * 批量更新文件状态
     * 
     * @param fileIds 文件ID列表
     * @param status 状态
     */
    @Modifying
    @Query("UPDATE FileInfo f SET f.status = :status WHERE f.id IN :fileIds")
    void batchUpdateStatus(@Param("fileIds") List<Long> fileIds, 
                          @Param("status") FileInfo.FileStatus status);

    /**
     * 查找需要清理的文件
     * 
     * @param cutoffTime 截止时间
     * @return 需要清理的文件列表
     */
    @Query("SELECT f FROM FileInfo f WHERE f.status = 'DELETED' AND f.createdAt < :cutoffTime")
    List<FileInfo> findFilesToCleanup(@Param("cutoffTime") LocalDateTime cutoffTime);

    /**
     * 根据项目ID列表查找文件
     * 
     * @param projectIds 项目ID列表
     * @return 文件列表
     */
    @Query("SELECT f FROM FileInfo f WHERE f.projectId IN :projectIds")
    List<FileInfo> findByProjectIds(@Param("projectIds") List<Long> projectIds);
}