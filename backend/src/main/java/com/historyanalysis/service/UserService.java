/**
 * 用户服务接口
 * @author AI Agent
 * @version 1.0.0
 * @created 2024-12-29 16:10:00
 * @description 用户管理服务接口，定义用户相关的业务操作
 */
package com.historyanalysis.service;

import com.historyanalysis.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;


import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 用户服务接口
 * 
 * 定义用户管理相关的业务操作：
 * - 用户注册和认证
 * - 用户信息管理
 * - 用户查询和统计
 * - 用户权限管理
 */
public interface UserService {

    /**
     * 用户注册
     * 
     * @param email 邮箱
     * @param password 密码
     * @param name 姓名
     * @param role 角色
     * @return 注册成功的用户
     * @throws IllegalArgumentException 当邮箱已存在或参数无效时
     */
    User registerUser(String email, String password, String name, User.UserRole role);

    /**
     * 用户登录验证
     * 
     * @param email 邮箱
     * @param password 密码
     * @return 验证成功的用户，验证失败返回空
     */
    Optional<User> authenticateUser(String email, String password);

    /**
     * 根据ID查找用户
     * 
     * @param id 用户ID
     * @return 用户信息，不存在返回空
     */
    Optional<User> findById(String id);

    /**
     * 根据邮箱查找用户
     * 
     * @param email 邮箱
     * @return 用户信息，不存在返回空
     */
    Optional<User> findByEmail(String email);

    /**
     * 更新用户信息（角色）
     * 
     * @param userId 用户ID
     * @param name 新姓名
     * @param role 新角色
     * @return 更新后的用户信息
     * @throws IllegalArgumentException 当用户不存在时
     */
    User updateUser(String userId, String name, User.UserRole role);

    /**
     * 更新用户基本信息（姓名和邮箱）
     * 
     * @param userId 用户ID
     * @param name 新姓名
     * @param email 新邮箱
     * @return 更新后的用户信息
     * @throws IllegalArgumentException 当用户不存在时
     */
    User updateUser(String userId, String name, String email);

    /**
     * 修改用户密码
     * 
     * @param userId 用户ID
     * @param oldPassword 旧密码
     * @param newPassword 新密码
     * @return 是否修改成功
     */
    boolean changePassword(String userId, String oldPassword, String newPassword);

    /**
     * 重置用户密码（管理员操作）
     * 
     * @param userId 用户ID
     * @param newPassword 新密码
     * @return 是否重置成功
     */
    boolean resetPassword(String userId, String newPassword);

    /**
     * 删除用户
     * 
     * @param userId 用户ID
     * @return 是否删除成功
     */
    boolean deleteUser(String userId);

    /**
     * 分页查询所有用户
     * 
     * @param pageable 分页参数
     * @return 用户分页结果
     */
    Page<User> findAllUsers(Pageable pageable);

    /**
     * 根据角色查询用户
     * 
     * @param role 用户角色
     * @param pageable 分页参数
     * @return 用户分页结果
     */
    Page<User> findUsersByRole(User.UserRole role, Pageable pageable);

    /**
     * 根据姓名模糊查询用户
     * 
     * @param name 姓名关键字
     * @param pageable 分页参数
     * @return 用户分页结果
     */
    Page<User> findUsersByName(String name, Pageable pageable);

    /**
     * 查询最近注册的用户
     * 
     * @param days 最近天数
     * @return 最近注册的用户列表
     */
    List<User> findRecentUsers(int days);

    /**
     * 查询活跃用户（有项目的用户）
     * 
     * @return 活跃用户列表
     */
    List<User> findActiveUsers();

    /**
     * 查询非活跃用户（没有项目的用户）
     * 
     * @return 非活跃用户列表
     */
    List<User> findInactiveUsers();

    /**
     * 根据项目数量查询用户
     * 
     * @param minProjectCount 最小项目数量
     * @param maxProjectCount 最大项目数量
     * @return 用户列表
     */
    List<User> findUsersByProjectCount(int minProjectCount, int maxProjectCount);

    /**
     * 统计用户总数
     * 
     * @return 用户总数
     */
    long getTotalUserCount();

    /**
     * 统计学生用户数量
     * 
     * @return 学生用户数量
     */
    long getStudentCount();

    /**
     * 统计教师用户数量
     * 
     * @return 教师用户数量
     */
    long getTeacherCount();

    /**
     * 统计最近注册的用户数量
     * 
     * @param days 最近天数
     * @return 最近注册的用户数量
     */
    long getRecentUserCount(int days);

    /**
     * 统计活跃用户数量
     * 
     * @return 活跃用户数量
     */
    long getActiveUserCount();

    /**
     * 获取用户统计信息
     * 
     * @return 用户统计信息 [总用户数, 学生数, 教师数, 活跃用户数, 最近7天注册数]
     */
    Object[] getUserStatistics();

    /**
     * 检查邮箱是否已存在
     * 
     * @param email 邮箱
     * @return 是否已存在
     */
    boolean isEmailExists(String email);

    /**
     * 验证用户权限
     * 
     * @param userId 用户ID
     * @param requiredRole 需要的角色
     * @return 是否有权限
     */
    boolean hasPermission(String userId, User.UserRole requiredRole);

    /**
     * 批量删除非活跃用户
     * 
     * @param beforeDate 创建时间早于此日期且没有项目的用户将被删除
     * @return 删除的用户数量
     */
    int deleteInactiveUsers(LocalDateTime beforeDate);

    /**
     * 启用用户账户
     * 
     * @param userId 用户ID
     * @return 是否启用成功
     */
    boolean enableUser(String userId);

    /**
     * 禁用用户账户
     * 
     * @param userId 用户ID
     * @return 是否禁用成功
     */
    boolean disableUser(String userId);

    /**
     * 检查用户是否启用
     * 
     * @param userId 用户ID
     * @return 是否启用
     */
    boolean isUserEnabled(String userId);

    /**
     * 获取用户的项目统计信息
     * 
     * @param userId 用户ID
     * @return 项目统计信息 [项目总数, 已完成项目数, 进行中项目数]
     */
    Object[] getUserProjectStatistics(String userId);

    /**
     * 获取用户的分析统计信息
     * 
     * @param userId 用户ID
     * @return 分析统计信息 [分析总数, 成功分析数, 失败分析数]
     */
    Object[] getUserAnalysisStatistics(String userId);
}