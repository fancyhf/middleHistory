/**
 * 用户数据访问层接口
 * @author AI Agent
 * @version 1.0.0
 * @created 2024-12-29 18:00:00
 */
package com.historyanalysis.repository;

import com.historyanalysis.entity.User;
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
 * 用户数据访问层接口
 * 提供用户相关的数据库操作方法
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * 根据用户名查找用户
     * 
     * @param username 用户名
     * @return 用户信息（可选）
     */
    Optional<User> findByUsername(String username);

    /**
     * 根据邮箱查找用户
     * 
     * @param email 邮箱地址
     * @return 用户信息（可选）
     */
    Optional<User> findByEmail(String email);

    /**
     * 根据用户名或邮箱查找用户
     * 
     * @param username 用户名
     * @param email 邮箱地址
     * @return 用户信息（可选）
     */
    Optional<User> findByUsernameOrEmail(String username, String email);

    /**
     * 检查用户名是否存在
     * 
     * @param username 用户名
     * @return 是否存在
     */
    boolean existsByUsername(String username);

    /**
     * 检查邮箱是否存在
     * 
     * @param email 邮箱地址
     * @return 是否存在
     */
    boolean existsByEmail(String email);

    /**
     * 根据状态查找用户列表
     * 
     * @param status 用户状态
     * @param pageable 分页参数
     * @return 用户分页列表
     */
    Page<User> findByStatus(User.UserStatus status, Pageable pageable);

    /**
     * 根据角色查找用户列表
     * 
     * @param role 用户角色
     * @param pageable 分页参数
     * @return 用户分页列表
     */
    Page<User> findByRole(User.UserRole role, Pageable pageable);

    /**
     * 根据创建时间范围查找用户
     * 
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param pageable 分页参数
     * @return 用户分页列表
     */
    Page<User> findByCreatedAtBetween(LocalDateTime startTime, LocalDateTime endTime, Pageable pageable);

    /**
     * 根据最后登录时间范围查找用户
     * 
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param pageable 分页参数
     * @return 用户分页列表
     */
    Page<User> findByLastLoginAtBetween(LocalDateTime startTime, LocalDateTime endTime, Pageable pageable);

    /**
     * 搜索用户（根据用户名、邮箱、真实姓名）
     * 
     * @param keyword 搜索关键词
     * @param pageable 分页参数
     * @return 用户分页列表
     */
    @Query("SELECT u FROM User u WHERE " +
           "u.username LIKE %:keyword% OR " +
           "u.email LIKE %:keyword% OR " +
           "u.realName LIKE %:keyword%")
    Page<User> searchUsers(@Param("keyword") String keyword, Pageable pageable);

    /**
     * 统计用户总数
     * 
     * @return 用户总数
     */
    @Query("SELECT COUNT(u) FROM User u WHERE u.status != 'DELETED'")
    Long countActiveUsers();

    /**
     * 根据状态统计用户数量
     * 
     * @param status 用户状态
     * @return 用户数量
     */
    Long countByStatus(User.UserStatus status);

    /**
     * 根据角色统计用户数量
     * 
     * @param role 用户角色
     * @return 用户数量
     */
    Long countByRole(User.UserRole role);

    /**
     * 查找指定时间之后未登录的用户
     * 
     * @param lastLoginTime 最后登录时间阈值
     * @return 用户列表
     */
    @Query("SELECT u FROM User u WHERE u.lastLoginAt < :lastLoginTime OR u.lastLoginAt IS NULL")
    List<User> findInactiveUsers(@Param("lastLoginTime") LocalDateTime lastLoginTime);

    /**
     * 查找今日新注册用户数量
     * 
     * @param startOfDay 今日开始时间
     * @param endOfDay 今日结束时间
     * @return 新注册用户数量
     */
    @Query("SELECT COUNT(u) FROM User u WHERE u.createdAt BETWEEN :startOfDay AND :endOfDay")
    Long countTodayRegistrations(@Param("startOfDay") LocalDateTime startOfDay, 
                                @Param("endOfDay") LocalDateTime endOfDay);

    /**
     * 查找今日活跃用户数量
     * 
     * @param startOfDay 今日开始时间
     * @param endOfDay 今日结束时间
     * @return 活跃用户数量
     */
    @Query("SELECT COUNT(u) FROM User u WHERE u.lastLoginAt BETWEEN :startOfDay AND :endOfDay")
    Long countTodayActiveUsers(@Param("startOfDay") LocalDateTime startOfDay, 
                              @Param("endOfDay") LocalDateTime endOfDay);

    /**
     * 更新用户最后登录信息
     * 
     * @param userId 用户ID
     * @param lastLoginAt 最后登录时间
     * @param lastLoginIp 最后登录IP
     */
    @Query("UPDATE User u SET u.lastLoginAt = :lastLoginAt, u.lastLoginIp = :lastLoginIp WHERE u.id = :userId")
    void updateLastLoginInfo(@Param("userId") Long userId, 
                            @Param("lastLoginAt") LocalDateTime lastLoginAt, 
                            @Param("lastLoginIp") String lastLoginIp);

    /**
     * 根据姓名模糊查询用户（忽略大小写）
     * 
     * @param name 姓名关键字
     * @param pageable 分页参数
     * @return 用户分页结果
     */
    Page<User> findByRealNameContainingIgnoreCase(String name, Pageable pageable);

    /**
     * 查找最近注册的用户
     * 
     * @param since 起始时间
     * @return 用户列表
     */
    @Query("SELECT u FROM User u WHERE u.createdAt >= :since ORDER BY u.createdAt DESC")
    List<User> findRecentUsers(@Param("since") LocalDateTime since);

    /**
     * 查找有项目的用户
     * 
     * @return 用户列表
     */
    @Query("SELECT DISTINCT u FROM User u WHERE u.id IN (SELECT DISTINCT p.userId FROM Project p WHERE p.status != 'DELETED')")
    List<User> findUsersWithProjects();

    /**
     * 查找没有项目的用户
     * 
     * @return 用户列表
     */
    @Query("SELECT u FROM User u WHERE u.id NOT IN (SELECT DISTINCT p.userId FROM Project p WHERE p.status != 'DELETED')")
    List<User> findUsersWithoutProjects();

    /**
     * 根据项目数量范围查找用户
     * 
     * @param minCount 最小项目数
     * @param maxCount 最大项目数
     * @return 用户列表
     */
    @Query("SELECT u FROM User u WHERE (SELECT COUNT(p) FROM Project p WHERE p.userId = u.id AND p.status != 'DELETED') BETWEEN :minCount AND :maxCount")
    List<User> findUsersByProjectCount(@Param("minCount") int minCount, @Param("maxCount") int maxCount);

    /**
     * 统计最近注册的用户数量
     * 
     * @param since 起始时间
     * @return 用户数量
     */
    @Query("SELECT COUNT(u) FROM User u WHERE u.createdAt >= :since")
    Long countRecentUsers(@Param("since") LocalDateTime since);

    /**
     * 统计有项目的用户数量
     * 
     * @return 用户数量
     */
    @Query("SELECT COUNT(DISTINCT p.userId) FROM Project p WHERE p.status != 'DELETED'")
    Long countUsersWithProjects();

    /**
     * 获取用户统计信息
     * 
     * @return 用户统计数据
     */
    @Query("SELECT COUNT(u) FROM User u")
    Long getTotalUserCount();
    
    @Query("SELECT COUNT(u) FROM User u WHERE u.createdAt >= :weekAgo")
    Long getNewUsersThisWeek(@Param("weekAgo") LocalDateTime weekAgo);
    
    @Query("SELECT COUNT(u) FROM User u WHERE u.lastLoginAt >= :weekAgo")
    Long getActiveUsersThisWeek(@Param("weekAgo") LocalDateTime weekAgo);



    /**
     * 删除指定时间之前创建且没有项目的用户
     * 
     * @param beforeDate 创建时间阈值
     * @return 删除的用户数量
     */
    @Modifying
    @Query("DELETE FROM User u WHERE u.createdAt < :beforeDate AND u.id NOT IN (SELECT DISTINCT p.userId FROM Project p WHERE p.status != 'DELETED')")
    int deleteUsersWithoutProjectsCreatedBefore(@Param("beforeDate") LocalDateTime beforeDate);
}