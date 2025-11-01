/**
 * 分析结果数据访问层接口
 * @author AI Agent
 * @version 1.0.0
 * @created 2024-12-29 18:00:00
 */
package com.historyanalysis.repository;

import com.historyanalysis.entity.AnalysisResult;
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
 * 分析结果数据访问层接口
 * 提供分析结果相关的数据库操作方法
 */
@Repository
public interface AnalysisResultRepository extends JpaRepository<AnalysisResult, Long> {

    /**
     * 根据项目ID查找分析结果列表
     * 
     * @param projectId 项目ID
     * @param pageable 分页参数
     * @return 分析结果分页列表
     */
    @Query("SELECT ar FROM AnalysisResult ar WHERE ar.project.id = :projectId")
    Page<AnalysisResult> findByProjectId(@Param("projectId") Long projectId, Pageable pageable);

    /**
     * 根据用户ID查找分析结果列表
     * 
     * @param userId 用户ID
     * @param pageable 分页参数
     * @return 分析结果分页列表
     */
    @Query("SELECT ar FROM AnalysisResult ar WHERE ar.user.id = :userId")
    Page<AnalysisResult> findByUserId(@Param("userId") Long userId, Pageable pageable);

    /**
     * 根据分析类型查找分析结果列表
     * 
     * @param analysisType 分析类型
     * @param pageable 分页参数
     * @return 分析结果分页列表
     */
    Page<AnalysisResult> findByAnalysisType(AnalysisResult.AnalysisType analysisType, Pageable pageable);

    /**
     * 根据状态查找分析结果列表
     * 
     * @param status 分析状态
     * @param pageable 分页参数
     * @return 分析结果分页列表
     */
    Page<AnalysisResult> findByStatus(AnalysisResult.AnalysisStatus status, Pageable pageable);

    /**
     * 根据项目ID和分析类型查找分析结果
     * 
     * @param projectId 项目ID
     * @param analysisType 分析类型
     * @param pageable 分页参数
     * @return 分析结果分页列表
     */
    @Query("SELECT ar FROM AnalysisResult ar WHERE ar.project.id = :projectId AND ar.analysisType = :analysisType")
    Page<AnalysisResult> findByProjectIdAndAnalysisType(@Param("projectId") Long projectId, 
                                                       @Param("analysisType") AnalysisResult.AnalysisType analysisType, 
                                                       Pageable pageable);

    /**
     * 根据项目ID和状态查找分析结果列表
     * 
     * @param projectId 项目ID
     * @param status 分析状态
     * @param pageable 分页参数
     * @return 分析结果分页列表
     */
    @Query("SELECT ar FROM AnalysisResult ar WHERE ar.project.id = :projectId AND ar.status = :status")
    Page<AnalysisResult> findByProjectIdAndStatus(@Param("projectId") Long projectId, 
                                                 @Param("status") AnalysisResult.AnalysisStatus status, 
                                                 Pageable pageable);

    /**
     * 根据用户ID和状态查找分析结果列表
     * 
     * @param userId 用户ID
     * @param status 分析状态
     * @param pageable 分页参数
     * @return 分析结果分页列表
     */
    @Query("SELECT ar FROM AnalysisResult ar WHERE ar.user.id = :userId AND ar.status = :status")
    Page<AnalysisResult> findByUserIdAndStatus(@Param("userId") Long userId, 
                                              @Param("status") AnalysisResult.AnalysisStatus status, 
                                              Pageable pageable);

    /**
     * 统计项目分析结果总数
     * 
     * @param projectId 项目ID
     * @return 分析结果总数
     */
    @Query("SELECT COUNT(ar) FROM AnalysisResult ar WHERE ar.project.id = :projectId")
    Long countByProjectId(@Param("projectId") Long projectId);

    /**
     * 根据项目ID和分析类型统计分析结果数量
     * 
     * @param projectId 项目ID
     * @param analysisType 分析类型
     * @return 分析结果数量
     */
    @Query("SELECT COUNT(ar) FROM AnalysisResult ar WHERE ar.project.id = :projectId AND ar.analysisType = :analysisType")
    Long countByProjectIdAndAnalysisType(@Param("projectId") Long projectId, @Param("analysisType") AnalysisResult.AnalysisType analysisType);

    /**
     * 根据项目ID和状态统计分析结果数量
     * 
     * @param projectId 项目ID
     * @param status 分析状态
     * @return 分析结果数量
     */
    @Query("SELECT COUNT(ar) FROM AnalysisResult ar WHERE ar.project.id = :projectId AND ar.status = :status")
    Long countByProjectIdAndStatus(@Param("projectId") Long projectId, @Param("status") AnalysisResult.AnalysisStatus status);

    /**
     * 统计用户分析结果总数
     * 
     * @param userId 用户ID
     * @return 分析结果总数
     */
    @Query("SELECT COUNT(ar) FROM AnalysisResult ar WHERE ar.user.id = :userId")
    Long countByUserId(@Param("userId") Long userId);

    /**
     * 根据用户ID和状态统计分析结果数量
     * 
     * @param userId 用户ID
     * @param status 分析状态
     * @return 分析结果数量
     */
    @Query("SELECT COUNT(ar) FROM AnalysisResult ar WHERE ar.user.id = :userId AND ar.status = :status")
    Long countByUserIdAndStatus(@Param("userId") Long userId, @Param("status") AnalysisResult.AnalysisStatus status);

    /**
     * 根据分析类型统计分析结果数量
     * 
     * @param analysisType 分析类型
     * @return 分析结果数量
     */
    Long countByAnalysisType(AnalysisResult.AnalysisType analysisType);

    /**
     * 根据状态统计分析结果数量
     * 
     * @param status 分析状态
     * @return 分析结果数量
     */
    Long countByStatus(AnalysisResult.AnalysisStatus status);

    /**
     * 查找项目中最近的分析结果
     * 
     * @param projectId 项目ID
     * @param pageable 分页参数
     * @return 分析结果列表
     */
    @Query("SELECT ar FROM AnalysisResult ar WHERE ar.project.id = :projectId " +
           "ORDER BY ar.createdAt DESC")
    List<AnalysisResult> findRecentProjectAnalysis(@Param("projectId") Long projectId, Pageable pageable);

    /**
     * 查找用户最近的分析结果
     * 
     * @param userId 用户ID
     * @param pageable 分页参数
     * @return 分析结果列表
     */
    @Query("SELECT ar FROM AnalysisResult ar WHERE ar.user.id = :userId " +
           "ORDER BY ar.createdAt DESC")
    List<AnalysisResult> findRecentUserAnalysis(@Param("userId") Long userId, Pageable pageable);

    /**
     * 查找今日创建的分析结果数量
     * 
     * @param startOfDay 今日开始时间
     * @param endOfDay 今日结束时间
     * @return 分析结果数量
     */
    @Query("SELECT COUNT(ar) FROM AnalysisResult ar WHERE ar.createdAt BETWEEN :startOfDay AND :endOfDay")
    Long countTodayAnalysis(@Param("startOfDay") LocalDateTime startOfDay, 
                           @Param("endOfDay") LocalDateTime endOfDay);

    /**
     * 查找用户今日创建的分析结果数量
     * 
     * @param userId 用户ID
     * @param startOfDay 今日开始时间
     * @param endOfDay 今日结束时间
     * @return 分析结果数量
     */
    @Query("SELECT COUNT(ar) FROM AnalysisResult ar WHERE ar.user.id = :userId AND " +
           "ar.createdAt BETWEEN :startOfDay AND :endOfDay")
    Long countUserTodayAnalysis(@Param("userId") Long userId,
                               @Param("startOfDay") LocalDateTime startOfDay, 
                               @Param("endOfDay") LocalDateTime endOfDay);

    /**
     * 查找今日完成的分析结果数量
     * 
     * @param startOfDay 今日开始时间
     * @param endOfDay 今日结束时间
     * @return 分析结果数量
     */
    @Query("SELECT COUNT(ar) FROM AnalysisResult ar WHERE ar.status = 'COMPLETED' AND " +
           "ar.completedAt BETWEEN :startOfDay AND :endOfDay")
    Long countTodayCompletedAnalysis(@Param("startOfDay") LocalDateTime startOfDay, 
                                    @Param("endOfDay") LocalDateTime endOfDay);

    /**
     * 查找运行中的分析任务
     * 
     * @return 分析结果列表
     */
    @Query("SELECT ar FROM AnalysisResult ar WHERE ar.status = 'PROCESSING' ORDER BY ar.startedAt ASC")
    List<AnalysisResult> findRunningAnalysis();

    /**
     * 查找待处理的分析任务
     * 
     * @return 分析结果列表
     */
    @Query("SELECT ar FROM AnalysisResult ar WHERE ar.status = 'PENDING' ORDER BY ar.createdAt ASC")
    List<AnalysisResult> findPendingAnalysis();

    /**
     * 查找失败的分析任务
     * 
     * @return 分析结果列表
     */
    @Query("SELECT ar FROM AnalysisResult ar WHERE ar.status = 'FAILED' ORDER BY ar.completedAt DESC")
    List<AnalysisResult> findFailedAnalysis();

    /**
     * 查找超时的分析任务
     * 
     * @param timeoutThreshold 超时阈值时间
     * @return 分析结果列表
     */
    @Query("SELECT ar FROM AnalysisResult ar WHERE ar.status = 'PROCESSING' AND " +
           "ar.startedAt < :timeoutThreshold")
    List<AnalysisResult> findTimeoutAnalysis(@Param("timeoutThreshold") LocalDateTime timeoutThreshold);

    /**
     * 计算平均处理时间
     * 
     * @param analysisType 分析类型
     * @return 平均处理时间（毫秒）
     */
    @Query("SELECT AVG(ar.processingTime) FROM AnalysisResult ar WHERE " +
           "ar.analysisType = :analysisType AND ar.status = 'COMPLETED' AND ar.processingTime IS NOT NULL")
    Double getAverageProcessingTime(@Param("analysisType") AnalysisResult.AnalysisType analysisType);

    /**
     * 更新分析状态
     * 
     * @param resultId 结果ID
     * @param status 新状态
     * @param errorMessage 错误信息（可选）
     */
    @Modifying
    @Query("UPDATE AnalysisResult ar SET ar.status = :status, ar.errorMessage = :errorMessage " +
           "WHERE ar.id = :resultId")
    void updateStatus(@Param("resultId") Long resultId, 
                     @Param("status") AnalysisResult.AnalysisStatus status,
                     @Param("errorMessage") String errorMessage);

    /**
     * 批量取消分析任务
     * 
     * @param resultIds 结果ID列表
     */
    @Modifying
    @Query("UPDATE AnalysisResult ar SET ar.status = 'CANCELLED', ar.completedAt = CURRENT_TIMESTAMP " +
           "WHERE ar.id IN :resultIds AND ar.status IN ('PENDING', 'PROCESSING')")
    void batchCancelAnalysis(@Param("resultIds") List<Long> resultIds);

    /**
     * 根据项目ID列表查找分析结果
     * 
     * @param projectIds 项目ID列表
     * @return 分析结果列表
     */
    @Query("SELECT ar FROM AnalysisResult ar WHERE ar.project.id IN :projectIds")
    List<AnalysisResult> findByProjectIds(@Param("projectIds") List<Long> projectIds);

    /**
     * 查找需要清理的分析结果（超过指定天数）
     * 
     * @param cutoffTime 截止时间/**
     * 查找需要清理的分析结果
     */
    @Query("SELECT ar FROM AnalysisResult ar WHERE ar.completedAt < :cutoffTime")
    List<AnalysisResult> findAnalysisToCleanup(@Param("cutoffTime") LocalDateTime cutoffTime);

    /**
     * 根据项目ID查找最后分析时间
     */
    @Query("SELECT MAX(ar.completedAt) FROM AnalysisResult ar WHERE ar.project.id = :projectId")
    LocalDateTime findLastAnalysisTimeByProjectId(@Param("projectId") Long projectId);

    /**
     * 根据项目ID和创建时间查找分析结果
     * 
     * @param projectId 项目ID
     * @param createdAfter 创建时间之后
     * @param pageable 分页参数
     * @return 分析结果分页列表
     */
    @Query("SELECT ar FROM AnalysisResult ar WHERE ar.project.id = :projectId AND ar.createdAt > :createdAfter ORDER BY ar.createdAt DESC")
    Page<AnalysisResult> findByProjectIdAndCreatedAtAfter(@Param("projectId") Long projectId, 
                                                         @Param("createdAfter") LocalDateTime createdAfter, 
                                                         Pageable pageable);

    /**
     * 查找最近的分析结果
     * 
     * @param createdAfter 创建时间之后
     * @param pageable 分页参数
     * @return 分析结果列表
     */
    @Query("SELECT ar FROM AnalysisResult ar WHERE ar.createdAt > :createdAfter ORDER BY ar.createdAt DESC")
    List<AnalysisResult> findRecentAnalyses(@Param("createdAfter") LocalDateTime createdAfter, 
                                           Pageable pageable);

    /**
     * 获取分析状态统计
     * 
     * @return 状态统计结果
     */
    @Query("SELECT ar.status, COUNT(ar) FROM AnalysisResult ar GROUP BY ar.status")
    List<Object[]> getAnalysisStatusStatistics();

    /**
     * 根据项目ID获取分析状态统计
     * 
     * @param projectId 项目ID
     * @return 状态统计结果
     */
    @Query("SELECT ar.status, COUNT(ar) FROM AnalysisResult ar WHERE ar.project.id = :projectId GROUP BY ar.status")
    List<Object[]> getAnalysisStatusStatisticsByProject(@Param("projectId") Long projectId);

    /**
     * 获取分析类型统计
     * 
     * @return 类型统计结果
     */
    @Query("SELECT ar.analysisType, COUNT(ar) FROM AnalysisResult ar GROUP BY ar.analysisType")
    List<Object[]> getAnalysisTypeStatistics();

    /**
     * 根据项目ID获取分析类型统计
     * 
     * @param projectId 项目ID
     * @return 类型统计结果
     */
    @Query("SELECT ar.analysisType, COUNT(ar) FROM AnalysisResult ar WHERE ar.project.id = :projectId GROUP BY ar.analysisType")
    List<Object[]> getAnalysisTypeStatisticsByProject(@Param("projectId") Long projectId);

    /**
     * 获取分析处理时间统计
     * 
     * @return 处理时间统计结果
     */
    @Query("SELECT AVG(ar.executionTime), MIN(ar.executionTime), MAX(ar.executionTime), COUNT(ar) FROM AnalysisResult ar WHERE ar.executionTime IS NOT NULL")
    List<Object[]> getAnalysisProcessingTimeStatistics();

    /**
     * 根据项目ID获取分析处理时间统计
     * 
     * @param projectId 项目ID
     * @return 处理时间统计结果
     */
    @Query("SELECT AVG(ar.executionTime), MIN(ar.executionTime), MAX(ar.executionTime), COUNT(ar) FROM AnalysisResult ar WHERE ar.project.id = :projectId AND ar.executionTime IS NOT NULL")
    List<Object[]> getAnalysisProcessingTimeStatisticsByProject(@Param("projectId") Long projectId);





    /**
     * 获取用户分析统计信息
     */
    @Query("SELECT COUNT(ar), AVG(ar.processingTime), MAX(ar.processingTime) FROM AnalysisResult ar WHERE ar.user.id = :userId")
    List<Object[]> getUserAnalysisStatistics(@Param("userId") Long userId);

    /**
     * 根据项目ID和关键字搜索分析结果
     * 
     * @param projectId 项目ID
     * @param keyword 搜索关键字
     * @param pageable 分页参数
     * @return 分析结果分页列表
     */
    @Query("SELECT ar FROM AnalysisResult ar WHERE ar.project.id = :projectId AND " +
           "(ar.parameters LIKE %:keyword% OR ar.resultData LIKE %:keyword% OR ar.errorMessage LIKE %:keyword%)")
    Page<AnalysisResult> searchAnalysesByProjectIdAndKeyword(@Param("projectId") Long projectId, 
                                                            @Param("keyword") String keyword, 
                                                            Pageable pageable);

    /**
     * 根据关键字搜索分析结果
     * 
     * @param keyword 搜索关键字
     * @param pageable 分页参数
     * @return 分析结果分页列表
     */
    @Query("SELECT ar FROM AnalysisResult ar WHERE " +
           "(ar.parameters LIKE %:keyword% OR ar.resultData LIKE %:keyword% OR ar.errorMessage LIKE %:keyword%)")
    Page<AnalysisResult> searchAnalysesByKeyword(@Param("keyword") String keyword, Pageable pageable);

    /**
     * 删除指定时间之前创建的失败分析结果
     * 
     * @param beforeDate 时间阈值
     * @return 删除的记录数
     */
    @Modifying
    @Query("DELETE FROM AnalysisResult ar WHERE ar.status = 'FAILED' AND ar.createdAt < :beforeDate")
    int deleteFailedAnalysesCreatedBefore(@Param("beforeDate") LocalDateTime beforeDate);
}