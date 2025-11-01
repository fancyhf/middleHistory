/**
 * 用户管理控制器
 * @author AI Agent
 * @version 1.0.0
 * @created 2024-12-29 17:00:00
 * @description 用户注册、登录、管理相关的REST API接口
 */
package com.historyanalysis.controller;

import com.historyanalysis.entity.User;
import com.historyanalysis.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 用户管理控制器
 * 
 * 提供用户相关的REST API接口：
 * - 用户注册和登录
 * - 用户信息管理
 * - 用户查询和统计
 * - 密码管理
 */
@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private UserService userService;

    /**
     * 用户注册
     */
    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> register(@Valid @RequestBody Map<String, String> request) {
        logger.info("用户注册请求, email={}", request.get("email"));

        Map<String, Object> response = new HashMap<>();

        try {
            String email = request.get("email");
            String password = request.get("password");
            String name = request.get("name");
            String roleStr = request.get("role");

            // 参数验证
            if (!StringUtils.hasText(email) || !StringUtils.hasText(password) || !StringUtils.hasText(name)) {
                response.put("success", false);
                response.put("message", "邮箱、密码和姓名不能为空");
                return ResponseEntity.badRequest().body(response);
            }

            // 解析角色
            User.UserRole role = User.UserRole.USER; // 默认为普通用户
            if (StringUtils.hasText(roleStr)) {
                try {
                    role = User.UserRole.valueOf(roleStr.toUpperCase());
                } catch (IllegalArgumentException e) {
                    logger.warn("无效的角色类型: {}", roleStr);
                }
            }

            // 执行注册
            User user = userService.registerUser(email, password, name, role);

            response.put("success", true);
            response.put("message", "注册成功");
            response.put("user", createUserResponse(user));

            logger.info("用户注册成功, userId={}, email={}", user.getId(), user.getEmail());
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            logger.warn("用户注册失败: {}", e.getMessage());
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            logger.error("用户注册异常: {}", e.getMessage(), e);
            response.put("success", false);
            response.put("message", "注册失败，请稍后重试");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 用户登录
     */
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody Map<String, String> request) {
        logger.info("用户登录请求, email={}", request.get("email"));

        Map<String, Object> response = new HashMap<>();

        try {
            String email = request.get("email");
            String password = request.get("password");

            // 参数验证
            if (!StringUtils.hasText(email) || !StringUtils.hasText(password)) {
                response.put("success", false);
                response.put("message", "邮箱和密码不能为空");
                return ResponseEntity.badRequest().body(response);
            }

            // 执行登录
            Optional<User> userOpt = userService.authenticateUser(email, password);

            if (userOpt.isPresent()) {
                User user = userOpt.get();
                response.put("success", true);
                response.put("message", "登录成功");
                response.put("user", createUserResponse(user));
                response.put("token", generateToken(user)); // TODO: 实现JWT token生成

                logger.info("用户登录成功, userId={}, email={}", user.getId(), user.getEmail());
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "邮箱或密码错误");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }
        } catch (Exception e) {
            logger.error("用户登录异常: {}", e.getMessage(), e);
            response.put("success", false);
            response.put("message", "登录失败，请稍后重试");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 获取用户信息
     */
    @GetMapping("/{userId}")
    public ResponseEntity<Map<String, Object>> getUserInfo(@PathVariable String userId) {
        logger.debug("获取用户信息, userId={}", userId);

        Map<String, Object> response = new HashMap<>();

        try {
            Optional<User> userOpt = userService.findById(userId);

            if (userOpt.isPresent()) {
                User user = userOpt.get();
                response.put("success", true);
                response.put("user", createUserResponse(user));
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "用户不存在");
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            logger.error("获取用户信息异常: {}", e.getMessage(), e);
            response.put("success", false);
            response.put("message", "获取用户信息失败");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 更新用户信息
     */
    @PutMapping("/{userId}")
    public ResponseEntity<Map<String, Object>> updateUser(@PathVariable String userId, 
                                                         @RequestBody Map<String, String> request) {
        logger.info("更新用户信息, userId={}", userId);

        Map<String, Object> response = new HashMap<>();

        try {
            String name = request.get("name");
            String email = request.get("email");

            User updatedUser = userService.updateUser(userId, name, email);

            response.put("success", true);
            response.put("message", "用户信息更新成功");
            response.put("user", createUserResponse(updatedUser));

            logger.info("用户信息更新成功, userId={}", userId);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            logger.warn("更新用户信息失败: {}", e.getMessage());
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            logger.error("更新用户信息异常: {}", e.getMessage(), e);
            response.put("success", false);
            response.put("message", "更新用户信息失败");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 删除用户
     */
    @DeleteMapping("/{userId}")
    public ResponseEntity<Map<String, Object>> deleteUser(@PathVariable String userId) {
        logger.info("删除用户, userId={}", userId);

        Map<String, Object> response = new HashMap<>();

        try {
            boolean deleted = userService.deleteUser(userId);

            if (deleted) {
                response.put("success", true);
                response.put("message", "用户删除成功");
                logger.info("用户删除成功, userId={}", userId);
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "用户不存在或删除失败");
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            logger.error("删除用户异常: {}", e.getMessage(), e);
            response.put("success", false);
            response.put("message", "删除用户失败");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 修改密码
     */
    @PutMapping("/{userId}/password")
    public ResponseEntity<Map<String, Object>> changePassword(@PathVariable String userId,
                                                             @RequestBody Map<String, String> request) {
        logger.info("修改密码, userId={}", userId);

        Map<String, Object> response = new HashMap<>();

        try {
            String oldPassword = request.get("oldPassword");
            String newPassword = request.get("newPassword");

            // 参数验证
            if (!StringUtils.hasText(oldPassword) || !StringUtils.hasText(newPassword)) {
                response.put("success", false);
                response.put("message", "旧密码和新密码不能为空");
                return ResponseEntity.badRequest().body(response);
            }

            boolean changed = userService.changePassword(userId, oldPassword, newPassword);

            if (changed) {
                response.put("success", true);
                response.put("message", "密码修改成功");
                logger.info("密码修改成功, userId={}", userId);
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "旧密码错误或用户不存在");
                return ResponseEntity.badRequest().body(response);
            }
        } catch (Exception e) {
            logger.error("修改密码异常: {}", e.getMessage(), e);
            response.put("success", false);
            response.put("message", "修改密码失败");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 重置密码
     */
    @PostMapping("/{userId}/reset-password")
    public ResponseEntity<Map<String, Object>> resetPassword(@PathVariable String userId,
                                                            @RequestBody Map<String, String> request) {
        logger.info("重置密码, userId={}", userId);

        Map<String, Object> response = new HashMap<>();

        try {
            String newPassword = request.get("newPassword");

            // 参数验证
            if (!StringUtils.hasText(newPassword)) {
                response.put("success", false);
                response.put("message", "新密码不能为空");
                return ResponseEntity.badRequest().body(response);
            }

            boolean reset = userService.resetPassword(userId, newPassword);

            if (reset) {
                response.put("success", true);
                response.put("message", "密码重置成功");
                logger.info("密码重置成功, userId={}", userId);
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "用户不存在或重置失败");
                return ResponseEntity.badRequest().body(response);
            }
        } catch (Exception e) {
            logger.error("重置密码异常: {}", e.getMessage(), e);
            response.put("success", false);
            response.put("message", "重置密码失败");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 查询用户列表
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) String name) {
        
        logger.debug("查询用户列表, page={}, size={}, sortBy={}, sortDir={}, role={}, name={}", 
                    page, size, sortBy, sortDir, role, name);

        Map<String, Object> response = new HashMap<>();

        try {
            // 创建分页和排序参数
            Sort.Direction direction = sortDir.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
            Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

            Page<User> userPage;

            // 根据条件查询
            if (StringUtils.hasText(role)) {
                try {
                    User.UserRole userRole = User.UserRole.valueOf(role.toUpperCase());
                    userPage = userService.findUsersByRole(userRole, pageable);
                } catch (IllegalArgumentException e) {
                    response.put("success", false);
                    response.put("message", "无效的角色类型");
                    return ResponseEntity.badRequest().body(response);
                }
            } else if (StringUtils.hasText(name)) {
                userPage = userService.findUsersByName(name, pageable);
            } else {
                userPage = userService.findAllUsers(pageable);
            }

            response.put("success", true);
            response.put("users", userPage.getContent().stream()
                    .map(this::createUserResponse)
                    .toArray());
            response.put("totalElements", userPage.getTotalElements());
            response.put("totalPages", userPage.getTotalPages());
            response.put("currentPage", userPage.getNumber());
            response.put("pageSize", userPage.getSize());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("查询用户列表异常: {}", e.getMessage(), e);
            response.put("success", false);
            response.put("message", "查询用户列表失败");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 获取用户统计信息
     */
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getUserStatistics() {
        logger.debug("获取用户统计信息");

        Map<String, Object> response = new HashMap<>();

        try {
            long totalUsers = userService.getTotalUserCount();
            long studentCount = userService.getStudentCount();
            long teacherCount = userService.getTeacherCount();

            Map<String, Object> statistics = new HashMap<>();
            statistics.put("totalUsers", totalUsers);
            statistics.put("studentCount", studentCount);
            statistics.put("teacherCount", teacherCount);

            response.put("success", true);
            response.put("statistics", statistics);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("获取用户统计信息异常: {}", e.getMessage(), e);
            response.put("success", false);
            response.put("message", "获取统计信息失败");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 获取最近注册的用户
     */
    @GetMapping("/recent")
    public ResponseEntity<Map<String, Object>> getRecentUsers(
            @RequestParam(defaultValue = "7") int days,
            @RequestParam(defaultValue = "10") int limit) {
        
        logger.debug("获取最近注册的用户, days={}, limit={}", days, limit);

        Map<String, Object> response = new HashMap<>();

        try {
            List<User> recentUsers = userService.findRecentUsers(days);

            response.put("success", true);
            response.put("users", recentUsers.stream()
                    .map(this::createUserResponse)
                    .toArray());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("获取最近注册用户异常: {}", e.getMessage(), e);
            response.put("success", false);
            response.put("message", "获取最近用户失败");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 获取活跃用户
     */
    @GetMapping("/active")
    public ResponseEntity<Map<String, Object>> getActiveUsers(
            @RequestParam(defaultValue = "30") int days,
            @RequestParam(defaultValue = "10") int limit) {
        
        logger.debug("获取活跃用户, days={}, limit={}", days, limit);

        Map<String, Object> response = new HashMap<>();

        try {
            List<User> activeUsers = userService.findActiveUsers();

            response.put("success", true);
            response.put("users", activeUsers.stream()
                    .map(this::createUserResponse)
                    .toArray());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("获取活跃用户异常: {}", e.getMessage(), e);
            response.put("success", false);
            response.put("message", "获取活跃用户失败");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // ========== 私有辅助方法 ==========

    /**
     * 创建用户响应对象（不包含敏感信息）
     */
    private Map<String, Object> createUserResponse(User user) {
        Map<String, Object> userResponse = new HashMap<>();
        userResponse.put("id", user.getId());
        userResponse.put("email", user.getEmail());
        userResponse.put("name", user.getRealName());
        userResponse.put("role", user.getRole().toString());
        userResponse.put("createdAt", user.getCreatedAt());
        userResponse.put("updatedAt", user.getUpdatedAt());
        userResponse.put("lastLoginAt", user.getLastLoginAt());
        return userResponse;
    }

    /**
     * 生成JWT token（临时实现）
     */
    private String generateToken(User user) {
        // TODO: 实现JWT token生成逻辑
        return "temp_token_" + user.getId();
    }
}