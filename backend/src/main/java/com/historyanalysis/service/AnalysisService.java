/**
 * 分析服务接口
 * @author AI Agent
 * @version 1.0.0
 * @created 2024-12-29 16:40:00
 * @description 历史数据分析服务接口，提供各种分析功能
 */
package com.historyanalysis.service;

import com.historyanalysis.entity.AnalysisResult;
import com.historyanalysis.entity.GeoLocation;
import com.historyanalysis.entity.TimelineEvent;
import com.historyanalysis.entity.WordFrequency;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 分析服务接口
 * 
 * 提供历史数据分析相关的业务操作：
 * - 文本分析和处理
 * - 词频统计和分类
 * - 时间轴事件提取
 * - 地理位置识别
 * - 多维度分析
 */
public interface AnalysisService {

    /**
     * 创建分析任务
     * 
     * @param projectId 项目ID
     * @param userId 用户ID
     * @param analysisType 分析类型
     * @param fileIds 文件ID列表
     * @return 分析结果
     */
    AnalysisResult createAnalysisTask(String projectId, String userId, AnalysisResult.AnalysisType analysisType, List<String> fileIds);

    /**
     * 创建分析任务（简化版本）
     * 
     * @param projectId 项目ID
     * @param analysisType 分析类型
     * @param description 分析描述
     * @return 分析结果
     */
    AnalysisResult createAnalysis(String projectId, AnalysisResult.AnalysisType analysisType, String description);

    /**
     * 执行词频分析
     * 
     * @param analysisId 分析ID
     * @param userId 用户ID
     * @return 是否成功
     */
    boolean executeWordFrequencyAnalysis(String analysisId, String userId);

    /**
     * 执行时间轴分析
     * 
     * @param analysisId 分析ID
     * @param userId 用户ID
     * @return 是否成功
     */
    boolean executeTimelineAnalysis(String analysisId, String userId);

    /**
     * 执行地理分析
     * 
     * @param analysisId 分析ID
     * @param userId 用户ID
     * @return 是否成功
     */
    boolean executeGeographyAnalysis(String analysisId, String userId);

    /**
     * 执行多维度分析
     * 
     * @param analysisId 分析ID
     * @param userId 用户ID
     * @return 是否成功
     */
    boolean executeMultidimensionalAnalysis(String analysisId, String userId);

    /**
     * 执行综合分析
     * 
     * @param analysisId 分析ID
     * @param userId 用户ID
     * @return 是否成功
     */
    boolean executeComprehensiveAnalysis(String analysisId, String userId);

    /**
     * 执行文本摘要分析
     * 
     * @param analysisId 分析ID
     * @param userId 用户ID
     * @return 是否成功
     */
    boolean executeTextSummaryAnalysis(String analysisId, String userId);

    /**
     * 根据ID查找分析结果
     * 
     * @param analysisId 分析ID
     * @return 分析结果
     */
    Optional<AnalysisResult> findById(String analysisId);

    /**
     * 删除分析结果
     * 
     * @param analysisId 分析ID
     * @param userId 用户ID
     * @return 是否成功
     */
    boolean deleteAnalysis(String analysisId, String userId);

    /**
     * 删除分析结果（简化版本，不需要用户ID）
     * 
     * @param analysisId 分析ID
     * @return 是否成功
     */
    boolean deleteAnalysisResult(String analysisId);

    /**
     * 批量删除分析结果
     * 
     * @param analysisIds 分析ID列表
     * @param userId 用户ID
     * @return 删除数量
     */
    int deleteAnalyses(List<String> analysisIds, String userId);

    /**
     * 根据项目查询分析结果
     * 
     * @param projectId 项目ID
     * @param pageable 分页参数
     * @return 分析结果分页
     */
    Page<AnalysisResult> findAnalysesByProject(String projectId, Pageable pageable);

    /**
     * 根据项目和分析类型查询分析结果
     * 
     * @param projectId 项目ID
     * @param analysisType 分析类型
     * @param pageable 分页参数
     * @return 分析结果分页
     */
    Page<AnalysisResult> findAnalysesByProjectAndType(String projectId, AnalysisResult.AnalysisType analysisType, Pageable pageable);

    /**
     * 根据项目和状态查询分析结果
     * 
     * @param projectId 项目ID
     * @param status 分析状态
     * @param pageable 分页参数
     * @return 分析结果分页
     */
    Page<AnalysisResult> findAnalysesByStatus(String projectId, AnalysisResult.AnalysisStatus status, Pageable pageable);

    /**
     * 根据项目和状态查询分析结果
     * 
     * @param projectId 项目ID
     * @param status 分析状态
     * @param pageable 分页参数
     * @return 分析结果分页
     */
    Page<AnalysisResult> findAnalysesByProjectAndStatus(String projectId, AnalysisResult.AnalysisStatus status, Pageable pageable);

    /**
     * 查询最近的分析结果
     * 
     * @param projectId 项目ID
     * @param days 天数
     * @param limit 限制数量
     * @return 分析结果列表
     */
    List<AnalysisResult> findRecentAnalyses(String projectId, int days, int limit);

    /**
     * 查询已完成的分析结果
     * 
     * @param projectId 项目ID
     * @param pageable 分页参数
     * @return 分析结果分页
     */
    Page<AnalysisResult> findCompletedAnalyses(String projectId, Pageable pageable);

    /**
     * 查询失败的分析结果
     * 
     * @param projectId 项目ID
     * @param pageable 分页参数
     * @return 分析结果分页
     */
    Page<AnalysisResult> findFailedAnalyses(String projectId, Pageable pageable);

    /**
     * 查询正在处理的分析结果
     * 
     * @param projectId 项目ID
     * @param pageable 分页参数
     * @return 分析结果分页
     */
    Page<AnalysisResult> findProcessingAnalyses(String projectId, Pageable pageable);

    /**
     * 统计项目的分析数量
     * 
     * @param projectId 项目ID
     * @return 分析数量
     */
    long countAnalysesByProject(String projectId);

    /**
     * 统计分析类型数量
     * 
     * @param projectId 项目ID
     * @param analysisType 分析类型
     * @return 分析数量
     */
    long countAnalysesByType(String projectId, AnalysisResult.AnalysisType analysisType);

    /**
     * 统计分析状态数量
     * 
     * @param projectId 项目ID
     * @param status 状态
     * @return 分析数量
     */
    long countAnalysesByStatus(String projectId, AnalysisResult.AnalysisStatus status);

    /**
     * 获取分析总数
     * 
     * @return 分析总数
     */
    long getTotalAnalysisCount();

    /**
     * 获取分析状态统计
     * 
     * @param projectId 项目ID
     * @return 状态统计
     */
    Map<AnalysisResult.AnalysisStatus, Long> getAnalysisStatusStatistics(String projectId);

    /**
     * 获取分析类型统计
     * 
     * @param projectId 项目ID
     * @return 类型统计
     */
    Map<AnalysisResult.AnalysisType, Long> getAnalysisTypeStatistics(String projectId);

    /**
     * 获取分析处理时间统计
     * 
     * @param projectId 项目ID
     * @return 处理时间统计
     */
    Object[] getAnalysisProcessingTimeStatistics(String projectId);

    /**
     * 获取用户的分析统计信息
     * 
     * @param userId 用户ID
     * @return 分析统计信息
     */
    Object[] getUserAnalysisStatistics(String userId);

    /**
     * 重新执行分析
     * 
     * @param analysisId 分析ID
     * @param userId 用户ID
     * @return 是否成功
     */
    boolean rerunAnalysis(String analysisId, String userId);

    /**
     * 取消分析任务
     * 
     * @param analysisId 分析ID
     * @param userId 用户ID
     * @return 是否成功
     */
    boolean cancelAnalysis(String analysisId, String userId);

    /**
     * 获取分析进度
     * 
     * @param analysisId 分析ID
     * @param userId 用户ID
     * @return 进度百分比
     */
    int getAnalysisProgress(String analysisId, String userId);

    /**
     * 检查用户是否有分析访问权限
     * 
     * @param analysisId 分析ID
     * @param userId 用户ID
     * @return 是否有权限
     */
    boolean hasAnalysisAccess(String analysisId, String userId);

    /**
     * 导出分析结果
     * 
     * @param analysisId 分析ID
     * @param userId 用户ID
     * @param format 导出格式
     * @return 导出文件路径
     */
    String exportAnalysisResult(String analysisId, String userId, String format);

    /**
     * 清理失败的分析结果
     * 
     * @param beforeDate 日期之前
     * @return 清理数量
     */
    int cleanupFailedAnalyses(LocalDateTime beforeDate);

    /**
     * 搜索分析结果
     * 
     * @param projectId 项目ID
     * @param keyword 关键字
     * @param pageable 分页参数
     * @return 分析结果分页
     */
    Page<AnalysisResult> searchAnalyses(String projectId, String keyword, Pageable pageable);

    /**
     * 获取项目分析概览
     * 
     * @param projectId 项目ID
     * @param userId 用户ID
     * @return 分析概览
     */
    Map<String, Object> getProjectAnalysisOverview(String projectId, String userId);

    // ========== 词频分析相关方法 ==========

    /**
     * 根据分析结果查询词频
     * 
     * @param analysisId 分析ID
     * @param pageable 分页参数
     * @return 词频分页
     */
    Page<WordFrequency> findWordFrequenciesByAnalysis(String analysisId, Pageable pageable);

    /**
     * 根据分析结果和类别查询词频
     * 
     * @param analysisId 分析ID
     * @param category 类别
     * @param pageable 分页参数
     * @return 词频分页
     */
    Page<WordFrequency> findWordFrequenciesByAnalysisAndCategory(String analysisId, WordFrequency.Category category, Pageable pageable);

    /**
     * 查询高频词汇
     * 
     * @param analysisId 分析ID
     * @param minFrequency 最小频率
     * @param limit 限制数量
     * @return 词频列表
     */
    List<WordFrequency> findHighFrequencyWords(String analysisId, int minFrequency, int limit);

    /**
     * 查询高相关性词汇
     * 
     * @param analysisId 分析ID
     * @param minRelevance 最小相关性
     * @param limit 限制数量
     * @return 词频列表
     */
    List<WordFrequency> findHighRelevanceWords(String analysisId, double minRelevance, int limit);

    /**
     * 获取词频统计
     * 
     * @param analysisId 分析ID
     * @return 词频统计
     */
    Map<WordFrequency.Category, Long> getWordFrequencyStatistics(String analysisId);

    // ========== 时间轴分析相关方法 ==========

    /**
     * 根据分析结果查询时间轴事件
     * 
     * @param analysisId 分析ID
     * @param pageable 分页参数
     * @return 时间轴事件分页
     */
    Page<TimelineEvent> findTimelineEventsByAnalysis(String analysisId, Pageable pageable);

    /**
     * 根据日期范围查询时间轴事件
     * 
     * @param analysisId 分析ID
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @param pageable 分页参数
     * @return 时间轴事件分页
     */
    Page<TimelineEvent> findTimelineEventsByDateRange(String analysisId, LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);

    /**
     * 查询最早的时间轴事件
     * 
     * @param analysisId 分析ID
     * @param limit 限制数量
     * @return 时间轴事件列表
     */
    List<TimelineEvent> findEarliestTimelineEvents(String analysisId, int limit);

    /**
     * 查询最晚的时间轴事件
     * 
     * @param analysisId 分析ID
     * @param limit 限制数量
     * @return 时间轴事件列表
     */
    List<TimelineEvent> findLatestTimelineEvents(String analysisId, int limit);

    /**
     * 获取时间轴事件统计
     * 
     * @param analysisId 分析ID
     * @return 时间轴事件统计
     */
    Object[] getTimelineEventStatistics(String analysisId);

    // ========== 地理分析相关方法 ==========

    /**
     * 根据分析结果查询地理位置
     * 
     * @param analysisId 分析ID
     * @param pageable 分页参数
     * @return 地理位置分页
     */
    Page<GeoLocation> findGeoLocationsByAnalysis(String analysisId, Pageable pageable);

    /**
     * 根据分析结果和位置类型查询地理位置
     * 
     * @param analysisId 分析ID
     * @param locationType 位置类型
     * @param pageable 分页参数
     * @return 地理位置分页
     */
    Page<GeoLocation> findGeoLocationsByAnalysisAndType(String analysisId, GeoLocation.LocationType locationType, Pageable pageable);

    /**
     * 根据坐标范围查询地理位置
     * 
     * @param analysisId 分析ID
     * @param minLatitude 最小纬度
     * @param maxLatitude 最大纬度
     * @param minLongitude 最小经度
     * @param maxLongitude 最大经度
     * @param pageable 分页参数
     * @return 地理位置分页
     */
    Page<GeoLocation> findGeoLocationsByCoordinateRange(String analysisId, double minLatitude, double maxLatitude, 
                                                       double minLongitude, double maxLongitude, Pageable pageable);

    /**
     * 查询附近的地理位置
     * 
     * @param analysisId 分析ID
     * @param centerLatitude 中心纬度
     * @param centerLongitude 中心经度
     * @param radiusKm 半径（公里）
     * @param limit 限制数量
     * @return 地理位置列表
     */
    List<GeoLocation> findNearbyGeoLocations(String analysisId, double centerLatitude, double centerLongitude, 
                                           double radiusKm, int limit);

    /**
     * 获取地理位置统计
     * 
     * @param analysisId 分析ID
     * @return 地理位置统计
     */
    Map<GeoLocation.LocationType, Long> getGeoLocationStatistics(String analysisId);

    // ========== Controller层需要的方法 ==========

    /**
     * 获取词频分析结果
     * 
     * @param analysisId 分析ID
     * @param pageable 分页参数
     * @return 词频结果分页
     */
    Page<WordFrequency> getWordFrequencyResults(String analysisId, Pageable pageable);

    /**
     * 获取时间轴分析结果
     * 
     * @param analysisId 分析ID
     * @param pageable 分页参数
     * @return 时间轴事件分页
     */
    Page<TimelineEvent> getTimelineResults(String analysisId, Pageable pageable);

    /**
     * 获取地理位置分析结果
     * 
     * @param analysisId 分析ID
     * @param pageable 分页参数
     * @return 地理位置分页
     */
    Page<GeoLocation> getGeographyResults(String analysisId, Pageable pageable);

    /**
     * 导出分析结果（简化版本）
     * 
     * @param analysisId 分析ID
     * @param format 导出格式
     * @return 导出路径
     */
    String exportAnalysisResult(String analysisId, String format);

    /**
     * 获取分析统计信息
     * 
     * @return 统计信息
     */
    Map<String, Long> getAnalysisStatistics();

    /**
     * 根据用户获取分析统计信息
     * 
     * @param userId 用户ID
     * @return 统计信息
     */
    Map<String, Long> getAnalysisStatisticsByUser(String userId);

    /**
     * 根据项目获取分析统计信息
     * 
     * @param projectId 项目ID
     * @return 统计信息
     */
    Map<String, Long> getAnalysisStatisticsByProject(String projectId);
}