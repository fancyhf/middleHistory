/**
 * 时间轴事件数据访问接口
 * @author AI Agent
 * @version 1.0.0
 * @created 2024-12-29 16:00:00
 * @description 时间轴事件实体的数据访问层接口，提供时间轴事件相关的数据库操作
 */
package com.historyanalysis.repository;

import com.historyanalysis.entity.AnalysisResult;
import com.historyanalysis.entity.TimelineEvent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 时间轴事件数据访问接口
 * 
 * 提供时间轴事件相关的数据库操作：
 * - 基础CRUD操作（继承自JpaRepository）
 * - 根据分析结果查询事件
 * - 根据事件日期查询事件
 * - 事件时间排序查询
 * - 事件统计查询
 */
@Repository
public interface TimelineEventRepository extends JpaRepository<TimelineEvent, String> {

    /**
     * 根据分析结果查找时间轴事件列表
     * 
     * @param analysisResult 所属分析结果
     * @return 分析结果的时间轴事件列表
     */
    List<TimelineEvent> findByAnalysisResult(AnalysisResult analysisResult);

    /**
     * 根据分析结果ID查找时间轴事件列表
     * 
     * @param analysisId 分析结果ID
     * @return 分析结果的时间轴事件列表
     */
    List<TimelineEvent> findByAnalysisResultId(Long analysisId);

    /**
     * 根据事件名称模糊查询时间轴事件
     * 
     * @param eventName 事件名称关键字
     * @return 匹配的时间轴事件列表
     */
    List<TimelineEvent> findByEventNameContainingIgnoreCase(String eventName);

    /**
     * 根据事件描述模糊查询时间轴事件
     * 
     * @param description 事件描述关键字
     * @return 匹配的时间轴事件列表
     */
    List<TimelineEvent> findByDescriptionContainingIgnoreCase(String description);

    /**
     * 根据分析结果查找按事件日期升序排列的时间轴事件列表
     * 
     * @param analysisResult 所属分析结果
     * @return 按事件日期升序排列的时间轴事件列表
     */
    List<TimelineEvent> findByAnalysisResultOrderByEventDateAsc(AnalysisResult analysisResult);

    /**
     * 根据分析结果ID查找按事件日期升序排列的时间轴事件列表
     * 
     * @param analysisId 分析结果ID
     * @return 按事件日期升序排列的时间轴事件列表
     */
    List<TimelineEvent> findByAnalysisResultIdOrderByEventDateAsc(Long analysisId);

    /**
     * 根据分析结果查找按事件日期降序排列的时间轴事件列表
     * 
     * @param analysisResult 所属分析结果
     * @return 按事件日期降序排列的时间轴事件列表
     */
    List<TimelineEvent> findByAnalysisResultOrderByEventDateDesc(AnalysisResult analysisResult);

    /**
     * 根据分析结果ID查找按事件日期降序排列的时间轴事件列表
     * 
     * @param analysisId 分析结果ID
     * @return 按事件日期降序排列的时间轴事件列表
     */
    List<TimelineEvent> findByAnalysisResultIdOrderByEventDateDesc(Long analysisId);

    /**
     * 查找指定日期之后的时间轴事件
     * 
     * @param eventDate 事件日期下限
     * @return 时间轴事件列表
     */
    List<TimelineEvent> findByEventDateAfter(LocalDate eventDate);

    /**
     * 查找指定日期之前的时间轴事件
     * 
     * @param eventDate 事件日期上限
     * @return 时间轴事件列表
     */
    List<TimelineEvent> findByEventDateBefore(LocalDate eventDate);

    /**
     * 查找指定日期范围内的时间轴事件
     * 
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 时间轴事件列表
     */
    List<TimelineEvent> findByEventDateBetween(LocalDate startDate, LocalDate endDate);

    /**
     * 根据分析结果查找指定日期范围内的时间轴事件
     * 
     * @param analysisResult 所属分析结果
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 时间轴事件列表
     */
    @Query("SELECT t FROM TimelineEvent t WHERE t.analysisResult = :analysisResult AND t.eventDate BETWEEN :startDate AND :endDate ORDER BY t.eventDate ASC")
    List<TimelineEvent> findByAnalysisResultAndEventDateBetween(@Param("analysisResult") AnalysisResult analysisResult, 
                                                               @Param("startDate") LocalDate startDate, 
                                                               @Param("endDate") LocalDate endDate);

    /**
     * 根据分析结果ID查找指定日期范围内的时间轴事件
     * 
     * @param analysisId 分析结果ID
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 时间轴事件列表
     */
    @Query("SELECT t FROM TimelineEvent t WHERE t.analysisResult.id = :analysisId AND t.eventDate BETWEEN :startDate AND :endDate ORDER BY t.eventDate ASC")
    List<TimelineEvent> findByAnalysisIdAndEventDateBetween(@Param("analysisId") Long analysisId, 
                                                           @Param("startDate") LocalDate startDate, 
                                                           @Param("endDate") LocalDate endDate);

    /**
     * 根据分析结果查找最早的时间轴事件
     * 
     * @param analysisResult 所属分析结果
     * @return 最早的时间轴事件
     */
    @Query("SELECT t FROM TimelineEvent t WHERE t.analysisResult = :analysisResult AND t.eventDate IS NOT NULL ORDER BY t.eventDate ASC")
    List<TimelineEvent> findEarliestEventByAnalysisResult(@Param("analysisResult") AnalysisResult analysisResult, Pageable pageable);

    /**
     * 根据分析结果ID查找最早的时间轴事件
     * 
     * @param analysisId 分析结果ID
     * @return 最早的时间轴事件
     */
    @Query("SELECT t FROM TimelineEvent t WHERE t.analysisResult.id = :analysisId AND t.eventDate IS NOT NULL ORDER BY t.eventDate ASC")
    List<TimelineEvent> findEarliestEventByAnalysisId(@Param("analysisId") Long analysisId, Pageable pageable);

    /**
     * 根据分析结果查找最晚的时间轴事件
     * 
     * @param analysisResult 所属分析结果
     * @return 最晚的时间轴事件
     */
    @Query("SELECT t FROM TimelineEvent t WHERE t.analysisResult = :analysisResult AND t.eventDate IS NOT NULL ORDER BY t.eventDate DESC")
    List<TimelineEvent> findLatestEventByAnalysisResult(@Param("analysisResult") AnalysisResult analysisResult, Pageable pageable);

    /**
     * 根据分析结果ID查找最晚的时间轴事件
     * 
     * @param analysisId 分析结果ID
     * @return 最晚的时间轴事件
     */
    @Query("SELECT t FROM TimelineEvent t WHERE t.analysisResult.id = :analysisId AND t.eventDate IS NOT NULL ORDER BY t.eventDate DESC")
    List<TimelineEvent> findLatestEventByAnalysisId(@Param("analysisId") Long analysisId, Pageable pageable);

    /**
     * 查找有日期信息的时间轴事件
     * 
     * @return 有日期信息的时间轴事件列表
     */
    @Query("SELECT t FROM TimelineEvent t WHERE t.eventDate IS NOT NULL ORDER BY t.eventDate ASC")
    List<TimelineEvent> findEventsWithDate();

    /**
     * 查找没有日期信息的时间轴事件
     * 
     * @return 没有日期信息的时间轴事件列表
     */
    @Query("SELECT t FROM TimelineEvent t WHERE t.eventDate IS NULL")
    List<TimelineEvent> findEventsWithoutDate();

    /**
     * 根据分析结果查找有日期信息的时间轴事件
     * 
     * @param analysisResult 所属分析结果
     * @return 有日期信息的时间轴事件列表
     */
    @Query("SELECT t FROM TimelineEvent t WHERE t.analysisResult = :analysisResult AND t.eventDate IS NOT NULL ORDER BY t.eventDate ASC")
    List<TimelineEvent> findEventsWithDateByAnalysisResult(@Param("analysisResult") AnalysisResult analysisResult);

    /**
     * 根据分析结果ID查找有日期信息的时间轴事件
     * 
     * @param analysisId 分析结果ID
     * @return 有日期信息的时间轴事件列表
     */
    @Query("SELECT t FROM TimelineEvent t WHERE t.analysisResult.id = :analysisId AND t.eventDate IS NOT NULL ORDER BY t.eventDate ASC")
    List<TimelineEvent> findEventsWithDateByAnalysisId(@Param("analysisId") Long analysisId);

    /**
     * 根据分析结果查找没有日期信息的时间轴事件
     * 
     * @param analysisResult 所属分析结果
     * @return 没有日期信息的时间轴事件列表
     */
    @Query("SELECT t FROM TimelineEvent t WHERE t.analysisResult = :analysisResult AND t.eventDate IS NULL")
    List<TimelineEvent> findEventsWithoutDateByAnalysisResult(@Param("analysisResult") AnalysisResult analysisResult);

    /**
     * 根据分析结果ID查找没有日期信息的时间轴事件
     * 
     * @param analysisId 分析结果ID
     * @return 没有日期信息的时间轴事件列表
     */
    @Query("SELECT t FROM TimelineEvent t WHERE t.analysisResult.id = :analysisId AND t.eventDate IS NULL")
    List<TimelineEvent> findEventsWithoutDateByAnalysisId(@Param("analysisId") Long analysisId);

    /**
     * 统计分析结果的时间轴事件数量
     * 
     * @param analysisResult 所属分析结果
     * @return 时间轴事件数量
     */
    long countByAnalysisResult(AnalysisResult analysisResult);

    /**
     * 统计分析结果的时间轴事件数量（根据分析结果ID）
     * 
     * @param analysisId 分析结果ID
     * @return 时间轴事件数量
     */
    long countByAnalysisResultId(Long analysisId);

    /**
     * 统计有日期信息的时间轴事件数量
     * 
     * @return 有日期信息的时间轴事件数量
     */
    @Query("SELECT COUNT(t) FROM TimelineEvent t WHERE t.eventDate IS NOT NULL")
    long countEventsWithDate();

    /**
     * 统计没有日期信息的时间轴事件数量
     * 
     * @return 没有日期信息的时间轴事件数量
     */
    @Query("SELECT COUNT(t) FROM TimelineEvent t WHERE t.eventDate IS NULL")
    long countEventsWithoutDate();

    /**
     * 统计分析结果中有日期信息的时间轴事件数量
     * 
     * @param analysisResult 所属分析结果
     * @return 有日期信息的时间轴事件数量
     */
    @Query("SELECT COUNT(t) FROM TimelineEvent t WHERE t.analysisResult = :analysisResult AND t.eventDate IS NOT NULL")
    long countEventsWithDateByAnalysisResult(@Param("analysisResult") AnalysisResult analysisResult);

    /**
     * 统计分析结果中有日期信息的时间轴事件数量（根据分析结果ID）
     * 
     * @param analysisId 分析结果ID
     * @return 有日期信息的时间轴事件数量
     */
    @Query("SELECT COUNT(t) FROM TimelineEvent t WHERE t.analysisResult.id = :analysisId AND t.eventDate IS NOT NULL")
    long countEventsWithDateByAnalysisId(@Param("analysisId") Long analysisId);

    /**
     * 获取分析结果的时间轴事件统计信息
     * 
     * @param analysisResult 所属分析结果
     * @return 时间轴事件总数
     */
    @Query("SELECT COUNT(t) FROM TimelineEvent t WHERE t.analysisResult = :analysisResult")
    Long getTimelineEventCount(@Param("analysisResult") AnalysisResult analysisResult);

    /**
     * 获取分析结果的时间轴事件统计信息（根据分析结果ID）
     * 
     * @param analysisId 分析结果ID
     * @return 时间轴事件总数
     */
    @Query("SELECT COUNT(t) FROM TimelineEvent t WHERE t.analysisResult.id = :analysisId")
    Long getTimelineEventCountByAnalysisId(@Param("analysisId") Long analysisId);

    /**
     * 获取分析结果的时间跨度信息
     * 
     * @param analysisResult 所属分析结果
     * @return 时间跨度信息数组 [最早日期, 最晚日期]
     */
    @Query("SELECT MIN(t.eventDate), MAX(t.eventDate) FROM TimelineEvent t WHERE t.analysisResult = :analysisResult AND t.eventDate IS NOT NULL")
    List<Object[]> getTimeSpanByAnalysisResult(@Param("analysisResult") AnalysisResult analysisResult);

    /**
     * 获取分析结果的时间跨度信息（根据分析结果ID）
     * 
     * @param analysisId 分析结果ID
     * @return 时间跨度信息数组 [最早日期, 最晚日期]
     */
    @Query("SELECT MIN(t.eventDate), MAX(t.eventDate) FROM TimelineEvent t WHERE t.analysisResult.id = :analysisId AND t.eventDate IS NOT NULL")
    List<Object[]> getTimeSpanByAnalysisId(@Param("analysisId") Long analysisId);

    /**
     * 根据用户查找时间轴事件（通过分析结果和用户关联）
     * 
     * @param userId 用户ID
     * @return 用户的时间轴事件列表
     */
    @Query("SELECT t FROM TimelineEvent t WHERE t.analysisResult.userId = :userId ORDER BY t.eventDate ASC")
    List<TimelineEvent> findTimelineEventsByUserId(@Param("userId") Long userId);

    /**
     * 统计用户的时间轴事件数量
     * 
     * @param userId 用户ID
     * @return 时间轴事件数量
     */
    @Query("SELECT COUNT(t) FROM TimelineEvent t WHERE t.analysisResult.userId = :userId")
    long countTimelineEventsByUserId(@Param("userId") Long userId);

    /**
     * 根据分析结果ID删除时间轴事件数据
     * 
     * @param analysisResultId 分析结果ID
     */
    void deleteByAnalysisResultId(Long analysisResultId);

    /**
     * 根据分析结果ID分页查询时间轴事件
     * 
     * @param analysisId 分析结果ID
     * @param pageable 分页参数
     * @return 分页的时间轴事件列表
     */
    Page<TimelineEvent> findByAnalysisResultId(Long analysisId, Pageable pageable);

    /**
     * 根据分析结果ID和日期范围分页查询时间轴事件
     * 
     * @param analysisId 分析结果ID
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @param pageable 分页参数
     * @return 分页的时间轴事件列表
     */
    @Query("SELECT t FROM TimelineEvent t WHERE t.analysisResult.id = :analysisId AND t.eventDate BETWEEN :startDate AND :endDate ORDER BY t.eventDate")
    Page<TimelineEvent> findByAnalysisResultIdAndDateRange(@Param("analysisId") Long analysisId, 
                                                          @Param("startDate") LocalDate startDate, 
                                                          @Param("endDate") LocalDate endDate, 
                                                          Pageable pageable);

    /**
     * 获取分析结果的时间轴事件统计信息（根据分析结果ID）
     * 
     * @param analysisId 分析结果ID
     * @return 时间轴事件统计信息数组 [总事件数, 有日期事件数, 无日期事件数]
     */
    @Query("SELECT COUNT(t), " +
           "SUM(CASE WHEN t.eventDate IS NOT NULL THEN 1 ELSE 0 END), " +
           "SUM(CASE WHEN t.eventDate IS NULL THEN 1 ELSE 0 END) " +
           "FROM TimelineEvent t WHERE t.analysisResult.id = :analysisId")
    List<Object[]> getTimelineEventStatisticsByAnalysis(@Param("analysisId") Long analysisId);
}