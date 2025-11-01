/**
 * 用户服务实现类
 * @author AI Agent
 * @version 1.0.0
 * @created 2024-12-29 16:15:00
 * @description 用户管理服务的具体实现，提供用户相关的业务逻辑处理
 */
package com.historyanalysis.service.impl;

import com.historyanalysis.entity.User;
import com.historyanalysis.repository.UserRepository;
import com.historyanalysis.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * 用户服务实现类
 * 
 * 实现用户管理相关的业务逻辑：
 * - 用户注册和认证
 * - 用户信息管理
 * - 用户查询和统计
 * - 用户权限管理
 */
@Service
@Transactional
public class UserServiceImpl implements UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

    /**
     * 邮箱格式验证正则表达式
     */
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$"
    );

    /**
     * 密码强度验证正则表达式（至少8位，包含字母和数字）
     */
    private static final Pattern PASSWORD_PATTERN = Pattern.compile(
        "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d@$!%*#?&]{8,}$"
    );

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;



    /**
     * 用户注册
     */
    @Override
    public User registerUser(String email, String password, String name, User.UserRole role) {
        logger.info("开始用户注册, email={}, name={}, role={}", email, name, role);

        // 参数验证
        validateRegistrationParams(email, password, name, role);

        // 检查邮箱是否已存在
        if (userRepository.findByEmail(email).isPresent()) {
            logger.warn("用户注册失败，邮箱已存在: {}", email);
            throw new IllegalArgumentException("邮箱已存在");
        }

        try {
            // 创建新用户
            User user = new User();
            user.setEmail(email);
            user.setPasswordHash(passwordEncoder.encode(password));
            user.setRealName(name);
            user.setRole(role);

            // 保存用户
            User savedUser = userRepository.save(user);
            logger.info("用户注册成功, userId={}, email={}", savedUser.getId(), savedUser.getEmail());

            return savedUser;
        } catch (Exception e) {
            logger.error("用户注册失败: {}", e.getMessage(), e);
            throw new RuntimeException("用户注册失败", e);
        }
    }

    /**
     * 用户登录验证
     */
    @Override
    @Transactional(readOnly = true)
    public Optional<User> authenticateUser(String email, String password) {
        logger.debug("用户登录验证, email={}", email);

        if (!StringUtils.hasText(email) || !StringUtils.hasText(password)) {
            logger.warn("登录验证失败，邮箱或密码为空");
            return Optional.empty();
        }

        try {
            Optional<User> userOpt = userRepository.findByEmail(email);
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                if (passwordEncoder.matches(password, user.getPassword())) {
                    logger.info("用户登录验证成功, userId={}, email={}", user.getId(), user.getEmail());
                    return Optional.of(user);
                } else {
                    logger.warn("用户登录验证失败，密码错误, email={}", email);
                }
            } else {
                logger.warn("用户登录验证失败，用户不存在, email={}", email);
            }
        } catch (Exception e) {
            logger.error("用户登录验证异常: {}", e.getMessage(), e);
        }

        return Optional.empty();
    }

    /**
     * 根据ID查找用户
     */
    @Override
    @Transactional(readOnly = true)
    public Optional<User> findById(String id) {
        logger.debug("根据ID查找用户, userId={}", id);

        if (!StringUtils.hasText(id)) {
            return Optional.empty();
        }

        try {
            Long idLong = Long.parseLong(id);
            return userRepository.findById(idLong);
        } catch (NumberFormatException e) {
            logger.error("无效的用户ID格式: {}", id);
            return Optional.empty();
        } catch (Exception e) {
            logger.error("根据ID查找用户异常: {}", e.getMessage(), e);
            return Optional.empty();
        }
    }

    /**
     * 根据邮箱查找用户
     */
    @Override
    @Transactional(readOnly = true)
    public Optional<User> findByEmail(String email) {
        logger.debug("根据邮箱查找用户, email={}", email);

        if (!StringUtils.hasText(email)) {
            return Optional.empty();
        }

        try {
            return userRepository.findByEmail(email);
        } catch (Exception e) {
            logger.error("根据邮箱查找用户异常: {}", e.getMessage(), e);
            return Optional.empty();
        }
    }

    /**
     * 更新用户信息（包含角色）
     */
    @Override
    public User updateUser(String userId, String name, User.UserRole role) {
        logger.info("更新用户信息, userId={}, name={}, role={}", userId, name, role);

        if (!StringUtils.hasText(userId)) {
            throw new IllegalArgumentException("用户ID不能为空");
        }

        if (!StringUtils.hasText(name)) {
            throw new IllegalArgumentException("姓名不能为空");
        }

        if (role == null) {
            throw new IllegalArgumentException("角色不能为空");
        }

        try {
            Long userIdLong = Long.parseLong(userId);
            Optional<User> userOpt = userRepository.findById(userIdLong);
            if (!userOpt.isPresent()) {
                logger.warn("更新用户信息失败，用户不存在: {}", userId);
                throw new IllegalArgumentException("用户不存在");
            }

            User user = userOpt.get();
            user.setRealName(name);
            user.setRole(role);

            User updatedUser = userRepository.save(user);
            logger.info("用户信息更新成功, userId={}", updatedUser.getId());

            return updatedUser;
        } catch (Exception e) {
            logger.error("更新用户信息失败: {}", e.getMessage(), e);
            throw new RuntimeException("更新用户信息失败", e);
        }
    }

    /**
     * 更新用户基本信息（姓名和邮箱）
     */
    public User updateUser(String userId, String name, String email) {
        logger.info("更新用户基本信息, userId={}, name={}, email={}", userId, name, email);

        if (!StringUtils.hasText(userId)) {
            throw new IllegalArgumentException("用户ID不能为空");
        }

        try {
            Long userIdLong = Long.parseLong(userId);
            Optional<User> userOpt = userRepository.findById(userIdLong);
            if (!userOpt.isPresent()) {
                logger.warn("更新用户信息失败，用户不存在: {}", userId);
                throw new IllegalArgumentException("用户不存在");
            }

            User user = userOpt.get();
            if (StringUtils.hasText(name)) {
                user.setRealName(name);
            }
            if (StringUtils.hasText(email)) {
                // 检查邮箱是否已被其他用户使用
                Optional<User> existingUser = userRepository.findByEmail(email);
                if (existingUser.isPresent() && !existingUser.get().getId().equals(user.getId())) {
                    throw new IllegalArgumentException("邮箱已被其他用户使用");
                }
                user.setEmail(email);
            }

            User updatedUser = userRepository.save(user);
            logger.info("用户基本信息更新成功, userId={}", updatedUser.getId());

            return updatedUser;
        } catch (NumberFormatException e) {
            logger.error("用户ID格式错误: {}", userId);
            throw new IllegalArgumentException("用户ID格式错误");
        } catch (Exception e) {
            logger.error("更新用户基本信息失败: {}", e.getMessage(), e);
            throw new RuntimeException("更新用户基本信息失败", e);
        }
    }

    /**
     * 修改用户密码
     */
    @Override
    public boolean changePassword(String userId, String oldPassword, String newPassword) {
        logger.info("修改用户密码, userId={}", userId);

        if (!StringUtils.hasText(userId) || !StringUtils.hasText(oldPassword) || !StringUtils.hasText(newPassword)) {
            logger.warn("修改密码失败，参数不能为空");
            return false;
        }

        if (!PASSWORD_PATTERN.matcher(newPassword).matches()) {
            logger.warn("修改密码失败，新密码格式不正确");
            throw new IllegalArgumentException("密码必须至少8位，包含字母和数字");
        }

        try {
            Long userIdLong = Long.parseLong(userId);
            Optional<User> userOpt = userRepository.findById(userIdLong);
            if (!userOpt.isPresent()) {
                logger.warn("修改密码失败，用户不存在: {}", userId);
                return false;
            }

            User user = userOpt.get();
            if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
                logger.warn("修改密码失败，旧密码错误, userId={}", userId);
                return false;
            }

            user.setPasswordHash(passwordEncoder.encode(newPassword));
            userRepository.save(user);

            logger.info("用户密码修改成功, userId={}", userId);
            return true;
        } catch (Exception e) {
            logger.error("修改用户密码异常: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * 重置用户密码（管理员操作）
     */
    @Override
    public boolean resetPassword(String userId, String newPassword) {
        logger.info("重置用户密码, userId={}", userId);

        if (!StringUtils.hasText(userId) || !StringUtils.hasText(newPassword)) {
            logger.warn("重置密码失败，参数不能为空");
            return false;
        }

        if (!PASSWORD_PATTERN.matcher(newPassword).matches()) {
            logger.warn("重置密码失败，新密码格式不正确");
            throw new IllegalArgumentException("密码必须至少8位，包含字母和数字");
        }

        try {
            Long userIdLong = Long.parseLong(userId);
            Optional<User> userOpt = userRepository.findById(userIdLong);
            if (!userOpt.isPresent()) {
                logger.warn("重置密码失败，用户不存在: {}", userId);
                return false;
            }

            User user = userOpt.get();
            user.setPasswordHash(passwordEncoder.encode(newPassword));
            userRepository.save(user);

            logger.info("用户密码重置成功, userId={}", userId);
            return true;
        } catch (Exception e) {
            logger.error("重置用户密码异常: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * 删除用户
     */
    @Override
    public boolean deleteUser(String userId) {
        logger.info("删除用户, userId={}", userId);

        if (!StringUtils.hasText(userId)) {
            logger.warn("删除用户失败，用户ID不能为空");
            return false;
        }

        try {
            Long userIdLong = Long.parseLong(userId);
            if (!userRepository.existsById(userIdLong)) {
                logger.warn("删除用户失败，用户不存在: {}", userId);
                return false;
            }

            userRepository.deleteById(userIdLong);
            logger.info("用户删除成功, userId={}", userId);
            return true;
        } catch (Exception e) {
            logger.error("删除用户异常: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * 分页查询所有用户
     */
    @Override
    @Transactional(readOnly = true)
    public Page<User> findAllUsers(Pageable pageable) {
        logger.debug("分页查询所有用户, page={}, size={}", pageable.getPageNumber(), pageable.getPageSize());

        try {
            return userRepository.findAll(pageable);
        } catch (Exception e) {
            logger.error("分页查询用户异常: {}", e.getMessage(), e);
            throw new RuntimeException("查询用户失败", e);
        }
    }

    /**
     * 根据角色查询用户
     */
    @Override
    @Transactional(readOnly = true)
    public Page<User> findUsersByRole(User.UserRole role, Pageable pageable) {
        logger.debug("根据角色查询用户, role={}, page={}, size={}", role, pageable.getPageNumber(), pageable.getPageSize());

        if (role == null) {
            throw new IllegalArgumentException("角色不能为空");
        }

        try {
            return userRepository.findByRole(role, pageable);
        } catch (Exception e) {
            logger.error("根据角色查询用户异常: {}", e.getMessage(), e);
            throw new RuntimeException("查询用户失败", e);
        }
    }

    /**
     * 根据姓名模糊查询用户
     */
    @Override
    @Transactional(readOnly = true)
    public Page<User> findUsersByName(String name, Pageable pageable) {
        logger.debug("根据姓名模糊查询用户, name={}, page={}, size={}", name, pageable.getPageNumber(), pageable.getPageSize());

        if (!StringUtils.hasText(name)) {
            throw new IllegalArgumentException("姓名不能为空");
        }

        try {
            return userRepository.findByRealNameContainingIgnoreCase(name, pageable);
        } catch (Exception e) {
            logger.error("根据姓名查询用户异常: {}", e.getMessage(), e);
            throw new RuntimeException("查询用户失败", e);
        }
    }

    /**
     * 查询最近注册的用户
     */
    @Override
    @Transactional(readOnly = true)
    public List<User> findRecentUsers(int days) {
        logger.debug("查询最近注册的用户, days={}", days);

        if (days <= 0) {
            throw new IllegalArgumentException("天数必须大于0");
        }

        try {
            LocalDateTime since = LocalDateTime.now().minusDays(days);
            return userRepository.findRecentUsers(since);
        } catch (Exception e) {
            logger.error("查询最近注册用户异常: {}", e.getMessage(), e);
            throw new RuntimeException("查询用户失败", e);
        }
    }

    /**
     * 查询活跃用户（有项目的用户）
     */
    @Override
    @Transactional(readOnly = true)
    public List<User> findActiveUsers() {
        logger.debug("查询活跃用户");

        try {
            return userRepository.findUsersWithProjects();
        } catch (Exception e) {
            logger.error("查询活跃用户异常: {}", e.getMessage(), e);
            throw new RuntimeException("查询用户失败", e);
        }
    }

    /**
     * 查询非活跃用户（没有项目的用户）
     */
    @Override
    @Transactional(readOnly = true)
    public List<User> findInactiveUsers() {
        logger.debug("查询非活跃用户");

        try {
            return userRepository.findUsersWithoutProjects();
        } catch (Exception e) {
            logger.error("查询非活跃用户异常: {}", e.getMessage(), e);
            throw new RuntimeException("查询用户失败", e);
        }
    }

    /**
     * 根据项目数量查询用户
     */
    @Override
    @Transactional(readOnly = true)
    public List<User> findUsersByProjectCount(int minProjectCount, int maxProjectCount) {
        logger.debug("根据项目数量查询用户, minCount={}, maxCount={}", minProjectCount, maxProjectCount);

        if (minProjectCount < 0 || maxProjectCount < minProjectCount) {
            throw new IllegalArgumentException("项目数量参数无效");
        }

        try {
            return userRepository.findUsersByProjectCount(minProjectCount, maxProjectCount);
        } catch (Exception e) {
            logger.error("根据项目数量查询用户异常: {}", e.getMessage(), e);
            throw new RuntimeException("查询用户失败", e);
        }
    }

    /**
     * 统计用户总数
     */
    @Override
    @Transactional(readOnly = true)
    public long getTotalUserCount() {
        logger.debug("统计用户总数");

        try {
            return userRepository.count();
        } catch (Exception e) {
            logger.error("统计用户总数异常: {}", e.getMessage(), e);
            return 0;
        }
    }

    /**
     * 统计学生用户数量
     */
    @Override
    @Transactional(readOnly = true)
    public long getStudentCount() {
        logger.debug("统计学生用户数量");

        try {
            return userRepository.countByRole(User.UserRole.USER);
        } catch (Exception e) {
            logger.error("统计学生用户数量异常: {}", e.getMessage(), e);
            return 0;
        }
    }

    /**
     * 统计教师用户数量
     */
    @Override
    @Transactional(readOnly = true)
    public long getTeacherCount() {
        logger.debug("统计教师用户数量");

        try {
            return userRepository.countByRole(User.UserRole.ADMIN);
        } catch (Exception e) {
            logger.error("统计教师用户数量异常: {}", e.getMessage(), e);
            return 0;
        }
    }

    /**
     * 统计最近注册的用户数量
     */
    @Override
    @Transactional(readOnly = true)
    public long getRecentUserCount(int days) {
        logger.debug("统计最近注册的用户数量, days={}", days);

        if (days <= 0) {
            throw new IllegalArgumentException("天数必须大于0");
        }

        try {
            LocalDateTime since = LocalDateTime.now().minusDays(days);
            return userRepository.countRecentUsers(since);
        } catch (Exception e) {
            logger.error("统计最近注册用户数量异常: {}", e.getMessage(), e);
            return 0;
        }
    }

    /**
     * 统计活跃用户数量
     */
    @Override
    @Transactional(readOnly = true)
    public long getActiveUserCount() {
        logger.debug("统计活跃用户数量");

        try {
            return userRepository.countUsersWithProjects();
        } catch (Exception e) {
            logger.error("统计活跃用户数量异常: {}", e.getMessage(), e);
            return 0;
        }
    }

    /**
     * 获取用户统计信息
     */
    @Override
    @Transactional(readOnly = true)
    public Object[] getUserStatistics() {
        logger.debug("获取用户统计信息");

        try {
            LocalDateTime weekAgo = LocalDateTime.now().minusWeeks(1);
            
            Long totalUsers = userRepository.getTotalUserCount();
            Long newUsersThisWeek = userRepository.getNewUsersThisWeek(weekAgo);
            Long activeUsersThisWeek = userRepository.getActiveUsersThisWeek(weekAgo);
            
            return new Object[]{
                totalUsers != null ? totalUsers : 0L,
                newUsersThisWeek != null ? newUsersThisWeek : 0L,
                activeUsersThisWeek != null ? activeUsersThisWeek : 0L,
                0L // usersWithProjects - 暂时设为0，后续可以添加单独的查询
            };
        } catch (Exception e) {
            logger.error("获取用户统计信息异常: {}", e.getMessage(), e);
            return new Object[]{0L, 0L, 0L, 0L};
        }
    }

    /**
     * 检查邮箱是否已存在
     */
    @Override
    @Transactional(readOnly = true)
    public boolean isEmailExists(String email) {
        logger.debug("检查邮箱是否已存在, email={}", email);

        if (!StringUtils.hasText(email)) {
            return false;
        }

        try {
            return userRepository.findByEmail(email).isPresent();
        } catch (Exception e) {
            logger.error("检查邮箱是否存在异常: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * 验证用户权限
     */
    @Override
    @Transactional(readOnly = true)
    public boolean hasPermission(String userId, User.UserRole requiredRole) {
        logger.debug("验证用户权限, userId={}, requiredRole={}", userId, requiredRole);

        if (!StringUtils.hasText(userId) || requiredRole == null) {
            return false;
        }

        try {
            Long userIdLong = Long.parseLong(userId);
            Optional<User> userOpt = userRepository.findById(userIdLong);
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                // 教师角色拥有所有权限，学生只能访问学生权限
                return user.getRole() == User.UserRole.ADMIN || user.getRole() == requiredRole;
            }
            return false;
        } catch (NumberFormatException e) {
            logger.error("用户ID格式错误: {}", userId);
            return false;
        } catch (Exception e) {
            logger.error("验证用户权限异常: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * 批量删除非活跃用户
     */
    @Override
    public int deleteInactiveUsers(LocalDateTime beforeDate) {
        logger.info("批量删除非活跃用户, beforeDate={}", beforeDate);

        if (beforeDate == null) {
            throw new IllegalArgumentException("日期不能为空");
        }

        try {
            int deletedCount = userRepository.deleteUsersWithoutProjectsCreatedBefore(beforeDate);
            logger.info("批量删除非活跃用户完成, deletedCount={}", deletedCount);
            return deletedCount;
        } catch (Exception e) {
            logger.error("批量删除非活跃用户异常: {}", e.getMessage(), e);
            throw new RuntimeException("删除用户失败", e);
        }
    }

    /**
     * 启用用户账户
     */
    @Override
    public boolean enableUser(String userId) {
        logger.info("启用用户账户, userId={}", userId);
        // 注意：当前User实体没有enabled字段，这里只是预留接口
        // 如果需要实现用户启用/禁用功能，需要在User实体中添加enabled字段
        return true;
    }

    /**
     * 禁用用户账户
     */
    @Override
    public boolean disableUser(String userId) {
        logger.info("禁用用户账户, userId={}", userId);
        // 注意：当前User实体没有enabled字段，这里只是预留接口
        // 如果需要实现用户启用/禁用功能，需要在User实体中添加enabled字段
        return true;
    }

    /**
     * 检查用户是否启用
     */
    @Override
    @Transactional(readOnly = true)
    public boolean isUserEnabled(String userId) {
        logger.debug("检查用户是否启用, userId={}", userId);
        // 注意：当前User实体没有enabled字段，这里只是预留接口
        // 如果需要实现用户启用/禁用功能，需要在User实体中添加enabled字段
        return true;
    }

    /**
     * 获取用户的项目统计信息
     */
    @Override
    @Transactional(readOnly = true)
    public Object[] getUserProjectStatistics(String userId) {
        logger.debug("获取用户的项目统计信息, userId={}", userId);

        if (!StringUtils.hasText(userId)) {
            throw new IllegalArgumentException("用户ID不能为空");
        }

        try {
            // 这里需要调用ProjectRepository的相关方法
            // 暂时返回默认值，实际实现需要在ProjectService中完成
            return new Object[]{0L, 0L, 0L};
        } catch (Exception e) {
            logger.error("获取用户项目统计信息异常: {}", e.getMessage(), e);
            return new Object[]{0L, 0L, 0L};
        }
    }

    /**
     * 获取用户的分析统计信息
     */
    @Override
    @Transactional(readOnly = true)
    public Object[] getUserAnalysisStatistics(String userId) {
        logger.debug("获取用户的分析统计信息, userId={}", userId);

        if (!StringUtils.hasText(userId)) {
            throw new IllegalArgumentException("用户ID不能为空");
        }

        try {
            // 这里需要调用AnalysisResultRepository的相关方法
            // 暂时返回默认值，实际实现需要在AnalysisService中完成
            return new Object[]{0L, 0L, 0L};
        } catch (Exception e) {
            logger.error("获取用户分析统计信息异常: {}", e.getMessage(), e);
            return new Object[]{0L, 0L, 0L};
        }
    }

    /**
     * 验证注册参数
     * 
     * @param email 邮箱
     * @param password 密码
     * @param name 姓名
     * @param role 角色
     */
    private void validateRegistrationParams(String email, String password, String name, User.UserRole role) {
        if (!StringUtils.hasText(email)) {
            throw new IllegalArgumentException("邮箱不能为空");
        }

        if (!EMAIL_PATTERN.matcher(email).matches()) {
            throw new IllegalArgumentException("邮箱格式不正确");
        }

        if (!StringUtils.hasText(password)) {
            throw new IllegalArgumentException("密码不能为空");
        }

        if (!PASSWORD_PATTERN.matcher(password).matches()) {
            throw new IllegalArgumentException("密码必须至少8位，包含字母和数字");
        }

        if (!StringUtils.hasText(name)) {
            throw new IllegalArgumentException("姓名不能为空");
        }

        if (name.length() > 50) {
            throw new IllegalArgumentException("姓名长度不能超过50个字符");
        }

        if (role == null) {
            throw new IllegalArgumentException("角色不能为空");
        }
    }
}