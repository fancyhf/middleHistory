/**
 * 项目数据访问层接口
 * @author AI Agent
 * @version 1.0.0
 * @created 2024-12-29 18:00:00
 */
package com.historyanalysis.repository;

import com.historyanalysis.entity.Project;
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
 * 项目数据访问层接口
 * 提供项目相关的数据库操作方法
 */
@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {

    /**
     * 根据用户ID查找项目列表
     * 
     * @param userId 用户ID
     * @param pageable 分页参数
     * @return 项目分页列表
     */
    Page<Project> findByUserId(Long userId, Pageable pageable);

    /**
     * 根据用户ID和状态查找项目列表
     * 
     * @param userId 用户ID
     * @param status 项目状态
     * @param pageable 分页参数
     * @return 项目分页列表
     */
    Page<Project> findByUserIdAndStatus(Long userId, Project.ProjectStatus status, Pageable pageable);

    /**
     * 根据用户ID查找非指定状态的项目列表
     * 
     * @param userId 用户ID
     * @param status 排除的项目状态
     * @param pageable 分页参数
     * @return 项目分页列表
     */
    Page<Project> findByUserIdAndStatusNot(Long userId, Project.ProjectStatus status, Pageable pageable);

    /**
     * 根据项目名称和用户ID查找项目
     * 
     * @param name 项目名称
     * @param userId 用户ID
     * @return 项目对象（可选）
     */
    Optional<Project> findByNameAndUserId(String name, Long userId);

    /**
     * 检查项目名称是否已存在（同一用户下，使用name字段）
     * 
     * @param name 项目名称
     * @param userId 用户ID
     * @return 是否存在
     */
    boolean existsByNameAndUserId(String name, Long userId);

    /**
     * 根据状态查找项目列表
     * 
     * @param status 项目状态
     * @param pageable 分页参数
     * @return 项目分页列表
     */
    Page<Project> findByStatus(Project.ProjectStatus status, Pageable pageable);

    /**
     * 根据创建时间范围查找项目
     * 
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param pageable 分页参数
     * @return 项目分页列表
     */
    Page<Project> findByCreatedAtBetween(LocalDateTime startTime, LocalDateTime endTime, Pageable pageable);

    /**
     * 搜索用户的项目（根据项目名称、描述）
     * 
     * @param userId 用户ID
     * @param keyword 搜索关键词
     * @param pageable 分页参数
     * @return 项目分页列表
     */
    @Query("SELECT p FROM Project p WHERE p.userId = :userId AND " +
           "(p.name LIKE %:keyword% OR p.description LIKE %:keyword%)")
    Page<Project> searchUserProjects(@Param("userId") Long userId, 
                                   @Param("keyword") String keyword, 
                                   Pageable pageable);

    /**
     * 全局搜索项目（管理员使用）
     * 
     * @param keyword 搜索关键词
     * @param pageable 分页参数
     * @return 项目分页列表
     */
    @Query("SELECT p FROM Project p WHERE " +
           "p.name LIKE %:keyword% OR p.description LIKE %:keyword%")
    Page<Project> searchAllProjects(@Param("keyword") String keyword, Pageable pageable);

    /**
     * 统计用户项目总数
     * 
     * @param userId 用户ID
     * @return 项目总数
     */
    @Query("SELECT COUNT(p) FROM Project p WHERE p.userId = :userId AND p.status != 'DELETED'")
    Long countUserProjects(@Param("userId") Long userId);

    /**
     * 根据用户ID和状态统计项目数量
     * 
     * @param userId 用户ID
     * @param status 项目状态
     * @return 项目数量
     */
    Long countByUserIdAndStatus(Long userId, Project.ProjectStatus status);

    /**
     * 统计所有项目总数
     * 
     * @return 项目总数
     */
    @Query("SELECT COUNT(p) FROM Project p WHERE p.status != 'DELETED'")
    Long countActiveProjects();

    /**
     * 根据状态统计项目数量
     * 
     * @param status 项目状态
     * @return 项目数量
     */
    Long countByStatus(Project.ProjectStatus status);

    /**
     * 查找用户最近的项目
     * 
     * @param userId 用户ID
     * @param limit 限制数量
     * @return 项目列表
     */
    @Query("SELECT p FROM Project p WHERE p.userId = :userId AND p.status = 'ACTIVE' " +
           "ORDER BY p.updatedAt DESC")
    List<Project> findRecentProjects(@Param("userId") Long userId, Pageable pageable);

    /**
     * 查找今日创建的项目数量
     * 
     * @param startOfDay 今日开始时间
     * @param endOfDay 今日结束时间
     * @return 项目数量
     */
    @Query("SELECT COUNT(p) FROM Project p WHERE p.createdAt BETWEEN :startOfDay AND :endOfDay")
    Long countTodayProjects(@Param("startOfDay") LocalDateTime startOfDay, 
                           @Param("endOfDay") LocalDateTime endOfDay);

    /**
     * 查找用户今日创建的项目数量
     * 
     * @param userId 用户ID
     * @param startOfDay 今日开始时间
     * @param endOfDay 今日结束时间
     * @return 项目数量
     */
    @Query("SELECT COUNT(p) FROM Project p WHERE p.userId = :userId AND " +
           "p.createdAt BETWEEN :startOfDay AND :endOfDay")
    Long countUserTodayProjects(@Param("userId") Long userId,
                               @Param("startOfDay") LocalDateTime startOfDay, 
                               @Param("endOfDay") LocalDateTime endOfDay);

    /**
     * 更新项目文件数量
     * 
     * @param projectId 项目ID
     * @param fileCount 文件数量
     */
    @Modifying
    @Query("UPDATE Project p SET p.fileCount = :fileCount WHERE p.id = :projectId")
    void updateFileCount(@Param("projectId") Long projectId, @Param("fileCount") Integer fileCount);

    /**
     * 更新项目分析数量
     * 
     * @param projectId 项目ID
     * @param analysisCount 分析数量
     */
    @Modifying
    @Query("UPDATE Project p SET p.analysisCount = :analysisCount WHERE p.id = :projectId")
    void updateAnalysisCount(@Param("projectId") Long projectId, @Param("analysisCount") Integer analysisCount);

    /**
     * 批量更新项目状态
     * 
     * @param projectIds 项目ID列表
     * @param status 新状态
     */
    @Modifying
    @Query("UPDATE Project p SET p.status = :status WHERE p.id IN :projectIds")
    void batchUpdateStatus(@Param("projectIds") List<Long> projectIds, 
                          @Param("status") Project.ProjectStatus status);

    /**
     * 查找需要清理的项目（已删除超过指定天数）
     * 
     * @param cutoffTime 截止时间
     * @return 项目列表
     */
    @Query("SELECT p FROM Project p WHERE p.status = 'DELETED' AND p.updatedAt < :cutoffTime")
    List<Project> findProjectsToCleanup(@Param("cutoffTime") LocalDateTime cutoffTime);

    /**
     * 检查项目名称是否已存在（排除指定项目ID）
     * 
     * @param name 项目名称
     * @param userId 用户ID
     * @param id 排除的项目ID
     * @return 是否存在
     */
    boolean existsByNameAndUserIdAndIdNot(String name, Long userId, Long id);

    /**
     * 根据用户ID和关键词搜索项目（排除指定状态）
     * 
     * @param userId 用户ID
     * @param name 项目名称关键词
     * @param description 描述关键词
     * @param status 排除的状态
     * @param pageable 分页参数
     * @return 项目分页列表
     */
    @Query("SELECT p FROM Project p WHERE p.user.id = :userId AND p.status != :status AND (p.name LIKE %:name% OR p.description LIKE %:description%)")
    Page<Project> findByUserIdAndKeywordAndStatusNot(
        @Param("userId") Long userId, 
        @Param("name") String name, 
        @Param("description") String description, 
        @Param("status") Project.ProjectStatus status, 
        Pageable pageable);

    /**
     * 统计用户非指定状态的项目数量
     * 
     * @param userId 用户ID
     * @param status 排除的状态
     * @return 项目数量
     */
    Long countByUserIdAndStatusNot(Long userId, Project.ProjectStatus status);
}