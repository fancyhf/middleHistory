/**
 * 词频数据访问接口
 * @author AI Agent
 * @version 1.0.0
 * @created 2024-12-29 15:55:00
 * @description 词频实体的数据访问层接口，提供词频相关的数据库操作
 */
package com.historyanalysis.repository;

import com.historyanalysis.entity.AnalysisResult;
import com.historyanalysis.entity.WordFrequency;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 词频数据访问接口
 * 
 * 提供词频相关的数据库操作：
 * - 基础CRUD操作（继承自JpaRepository）
 * - 根据分析结果查询词频
 * - 根据词汇类别查询词频
 * - 词频统计和排序查询
 * - 高频词汇查询
 */
@Repository
public interface WordFrequencyRepository extends JpaRepository<WordFrequency, String> {

    /**
     * 根据分析结果查找词频列表
     * 
     * @param analysisResult 所属分析结果
     * @return 分析结果的词频列表
     */
    List<WordFrequency> findByAnalysisResult(AnalysisResult analysisResult);

    /**
     * 根据分析结果ID查找词频列表
     * 
     * @param analysisId 分析结果ID
     * @return 分析结果的词频列表
     */
    List<WordFrequency> findByAnalysisResultId(Long analysisId);

    /**
     * 根据词汇类别查找词频记录
     * @param category 词汇类别
     * @return 词频列表
     */
    List<WordFrequency> findByCategory(WordFrequency.Category category);

    /**
     * 根据分析结果和词汇类别查找词频记录
     * @param analysisResult 分析结果
     * @param category 词汇类别
     * @return 词频列表
     */
    List<WordFrequency> findByAnalysisResultAndCategory(AnalysisResult analysisResult, WordFrequency.Category category);

    /**
     * 根据分析结果ID和类别查找词频列表
     * 
     * @param analysisId 分析结果ID
     * @param category 词汇类别
     * @return 词频列表
     */
    List<WordFrequency> findByAnalysisResultIdAndCategory(Long analysisId, WordFrequency.Category category);

    /**
     * 根据词汇内容模糊查询词频
     * 
     * @param word 词汇内容关键字
     * @return 匹配的词频列表
     */
    List<WordFrequency> findByWordContainingIgnoreCase(String word);

    /**
     * 根据分析结果查找按频次降序排列的词频列表
     * 
     * @param analysisResult 所属分析结果
     * @return 按频次降序排列的词频列表
     */
    List<WordFrequency> findByAnalysisResultOrderByFrequencyDesc(AnalysisResult analysisResult);

    /**
     * 根据分析结果ID查找按频次降序排列的词频列表
     * 
     * @param analysisId 分析结果ID
     * @return 按频次降序排列的词频列表
     */
    List<WordFrequency> findByAnalysisResultIdOrderByFrequencyDesc(Long analysisId);

    /**
     * 根据分析结果查找按相关性得分降序排列的词频列表
     * 
     * @param analysisResult 所属分析结果
     * @return 按相关性得分降序排列的词频列表
     */
    List<WordFrequency> findByAnalysisResultOrderByRelevanceScoreDesc(AnalysisResult analysisResult);

    /**
     * 根据分析结果ID查找按相关性得分降序排列的词频列表
     * 
     * @param analysisId 分析结果ID
     * @return 按相关性得分降序排列的词频列表
     */
    List<WordFrequency> findByAnalysisResultIdOrderByRelevanceScoreDesc(Long analysisId);

    /**
     * 根据分析结果查找高频词汇（频次大于指定值）
     * 
     * @param analysisResult 所属分析结果
     * @param minFrequency 最小频次
     * @return 高频词汇列表
     */
    @Query("SELECT w FROM WordFrequency w WHERE w.analysisResult = :analysisResult AND w.frequency >= :minFrequency ORDER BY w.frequency DESC")
    List<WordFrequency> findHighFrequencyWords(@Param("analysisResult") AnalysisResult analysisResult, 
                                              @Param("minFrequency") Integer minFrequency);

    /**
     * 根据分析结果ID查找高频词汇（频次大于指定值）
     * 
     * @param analysisId 分析结果ID
     * @param minFrequency 最小频次
     * @return 高频词汇列表
     */
    @Query("SELECT w FROM WordFrequency w WHERE w.analysisResult.id = :analysisId AND w.frequency >= :minFrequency ORDER BY w.frequency DESC")
    List<WordFrequency> findHighFrequencyWordsByAnalysisId(@Param("analysisId") Long analysisId, 
                                                          @Param("minFrequency") Integer minFrequency);

    /**
     * 根据分析结果查找高相关性词汇（相关性得分大于指定值）
     * 
     * @param analysisResult 所属分析结果
     * @param minScore 最小相关性得分
     * @return 高相关性词汇列表
     */
    @Query("SELECT w FROM WordFrequency w WHERE w.analysisResult = :analysisResult AND w.relevanceScore >= :minScore ORDER BY w.relevanceScore DESC")
    List<WordFrequency> findHighRelevanceWords(@Param("analysisResult") AnalysisResult analysisResult, 
                                              @Param("minScore") Float minScore);

    /**
     * 根据分析结果ID查找高相关性词汇（相关性得分大于指定值）
     * 
     * @param analysisId 分析结果ID
     * @param minScore 最小相关性得分
     * @return 高相关性词汇列表
     */
    @Query("SELECT w FROM WordFrequency w WHERE w.analysisResult.id = :analysisId AND w.relevanceScore >= :minScore ORDER BY w.relevanceScore DESC")
    List<WordFrequency> findHighRelevanceWordsByAnalysisId(@Param("analysisId") Long analysisId, 
                                                          @Param("minScore") Float minScore);

    /**
     * 根据分析结果查找前N个高频词汇
     * 
     * @param analysisResult 所属分析结果
     * @param pageable 分页参数
     * @return 前N个高频词汇列表
     */
    @Query("SELECT w FROM WordFrequency w WHERE w.analysisResult = :analysisResult ORDER BY w.frequency DESC")
    List<WordFrequency> findTopFrequencyWords(@Param("analysisResult") AnalysisResult analysisResult, 
                                             Pageable pageable);

    /**
     * 根据分析结果ID查找前N个高频词汇
     * 
     * @param analysisId 分析结果ID
     * @param pageable 分页参数
     * @return 前N个高频词汇列表
     */
    @Query("SELECT w FROM WordFrequency w WHERE w.analysisResult.id = :analysisId ORDER BY w.frequency DESC")
    List<WordFrequency> findTopFrequencyWordsByAnalysisId(@Param("analysisId") Long analysisId, 
                                                         Pageable pageable);

    /**
     * 根据分析结果和类别查找前N个高频词汇
     * 
     * @param analysisResult 所属分析结果
     * @param category 词汇类别
     * @param pageable 分页参数
     * @return 前N个高频词汇列表
     */
    @Query("SELECT w FROM WordFrequency w WHERE w.analysisResult = :analysisResult AND w.category = :category ORDER BY w.frequency DESC")
    List<WordFrequency> findTopFrequencyWordsByCategory(@Param("analysisResult") AnalysisResult analysisResult, 
                                                       @Param("category") WordFrequency.Category category, 
                                                       Pageable pageable);

    /**
     * 根据分析结果ID和类别查找前N个高频词汇
     * 
     * @param analysisId 分析结果ID
     * @param category 词汇类别
     * @param pageable 分页参数
     * @return 前N个高频词汇列表
     */
    @Query("SELECT w FROM WordFrequency w WHERE w.analysisResult.id = :analysisId AND w.category = :category ORDER BY w.frequency DESC")
    List<WordFrequency> findTopFrequencyWordsByCategoryAndAnalysisId(@Param("analysisId") Long analysisId,
                                                                    @Param("category") WordFrequency.Category category,
                                                                    Pageable pageable);

    /**
     * 统计分析结果的词频数量
     * 
     * @param analysisResult 所属分析结果
     * @return 词频数量
     */
    long countByAnalysisResult(AnalysisResult analysisResult);

    /**
     * 统计分析结果的词频数量（根据分析结果ID）
     * 
     * @param analysisId 分析结果ID
     * @return 词频数量
     */
    long countByAnalysisResultId(Long analysisId);

    /**
     * 统计指定类别的词频数量
     * 
     * @param category 词汇类别
     * @return 词频数量
     */
    long countByCategory(WordFrequency.Category category);

    /**
     * 统计分析结果中指定类别的词频数量
     * 
     * @param analysisResult 所属分析结果
     * @param category 词汇类别
     * @return 词频数量
     */
    long countByAnalysisResultAndCategory(AnalysisResult analysisResult, WordFrequency.Category category);

    /**
     * 统计分析结果中指定类别的词频数量（根据分析结果ID）
     * 
     * @param analysisId 分析结果ID
     * @param category 词汇类别
     * @return 词频数量
     */
    long countByAnalysisResultIdAndCategory(Long analysisId, WordFrequency.Category category);

    /**
     * 获取分析结果的词频统计信息
     * 
     * @param analysisResult 所属分析结果
     * @return 词频总数
     */
    @Query("SELECT COUNT(w) FROM WordFrequency w WHERE w.analysisResult = :analysisResult")
    Long getWordFrequencyCount(@Param("analysisResult") AnalysisResult analysisResult);

    /**
     * 获取分析结果的词频统计信息（根据分析结果ID）
     * 
     * @param analysisId 分析结果ID
     * @return 词频总数
     */
    @Query("SELECT COUNT(w) FROM WordFrequency w WHERE w.analysisResult.id = :analysisId")
    Long getWordFrequencyCountByAnalysisId(@Param("analysisId") Long analysisId);

    /**
     * 获取分析结果的频次统计信息
     * 
     * @param analysisResult 所属分析结果
     * @return 频次统计信息数组 [总频次, 平均频次, 最大频次, 最小频次]
     */
    @Query("SELECT SUM(w.frequency), AVG(w.frequency), MAX(w.frequency), MIN(w.frequency) FROM WordFrequency w WHERE w.analysisResult = :analysisResult")
    List<Object[]> getFrequencyStatistics(@Param("analysisResult") AnalysisResult analysisResult);

    /**
     * 获取分析结果的频次统计信息（根据分析结果ID）
     * 
     * @param analysisId 分析结果ID
     * @return 频次统计信息数组 [总频次, 平均频次, 最大频次, 最小频次]
     */
    @Query("SELECT SUM(w.frequency), AVG(w.frequency), MAX(w.frequency), MIN(w.frequency) FROM WordFrequency w WHERE w.analysisResult.id = :analysisId")
    List<Object[]> getFrequencyStatisticsByAnalysisId(@Param("analysisId") Long analysisId);

    /**
     * 根据用户ID查找词频列表
     * 
     * @param userId 用户ID
     * @return 词频列表
     */
    @Query("SELECT w FROM WordFrequency w WHERE w.analysisResult.userId = :userId ORDER BY w.frequency DESC")
    List<WordFrequency> findWordFrequenciesByUserId(@Param("userId") Long userId);

    /**
     * 统计用户的词频数量
     * 
     * @param userId 用户ID
     * @return 词频数量
     */
    @Query("SELECT COUNT(w) FROM WordFrequency w WHERE w.analysisResult.userId = :userId")
    Long countWordFrequenciesByUserId(@Param("userId") Long userId);

    /**
     * 根据分析结果ID删除词频数据
     * 
     * @param analysisResultId 分析结果ID
     */
    void deleteByAnalysisResultId(Long analysisResultId);

    /**
     * 根据分析结果ID查找词频列表（分页）
     * 
     * @param analysisId 分析结果ID
     * @param pageable 分页参数
     * @return 分析结果的词频分页列表
     */
    Page<WordFrequency> findByAnalysisResultId(Long analysisId, Pageable pageable);

    /**
     * 根据分析结果ID和类别查找词频列表（分页）
     * 
     * @param analysisId 分析结果ID
     * @param category 词汇类别
     * @param pageable 分页参数
     * @return 词频分页列表
     */
    Page<WordFrequency> findByAnalysisResultIdAndCategory(Long analysisId, WordFrequency.Category category, Pageable pageable);

    /**
     * 根据分析结果ID和最小频率查找词频列表
     * 
     * @param analysisId 分析结果ID
     * @param minFrequency 最小频率
     * @param pageable 分页参数
     * @return 词频列表
     */
    List<WordFrequency> findByAnalysisResultIdAndFrequencyGreaterThanEqual(Long analysisId, int minFrequency, Pageable pageable);

    /**
     * 根据分析结果ID和最小相关性分数查找词频列表
     * 
     * @param analysisId 分析结果ID
     * @param minRelevance 最小相关性分数
     * @param pageable 分页参数
     * @return 词频列表
     */
    List<WordFrequency> findByAnalysisResultIdAndRelevanceScoreGreaterThanEqual(Long analysisId, double minRelevance, Pageable pageable);

    /**
     * 获取分析结果的词频统计信息
     * 
     * @param analysisId 分析结果ID
     * @return 统计信息
     */
    @Query("SELECT w.category, COUNT(w) FROM WordFrequency w WHERE w.analysisResult.id = :analysisId GROUP BY w.category")
    List<Object[]> getWordFrequencyStatisticsByAnalysis(@Param("analysisId") Long analysisId);
}