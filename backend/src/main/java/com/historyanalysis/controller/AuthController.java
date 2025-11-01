/**
 * 认证控制器
 * @author AI Agent
 * @version 1.0.0
 * @created 2024-12-29 18:00:00
 */
package com.historyanalysis.controller;

import com.historyanalysis.dto.AuthRequest;
import com.historyanalysis.dto.AuthResponse;
import com.historyanalysis.dto.RegisterRequest;
import com.historyanalysis.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 认证控制器
 * 处理用户认证相关的HTTP请求
 */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);
    private final AuthService authService;

    /**
     * 用户注册
     * 
     * @param request 注册请求
     * @return 认证响应
     */
    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> register(@Valid @RequestBody RegisterRequest request) {
        logger.info("收到用户注册请求: {}", request.getUsername());
        
        try {
            AuthResponse authResponse = authService.register(request);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "注册成功");
            response.put("data", authResponse);
            
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            logger.warn("用户注册失败: {}", e.getMessage());
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            logger.error("用户注册异常: {}", e.getMessage(), e);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "注册失败，请稍后重试");
            
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * 用户登录
     * 
     * @param request 登录请求
     * @return 认证响应
     */
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@Valid @RequestBody AuthRequest request) {
        logger.info("收到用户登录请求: {}", request.getUsername());
        
        try {
            AuthResponse authResponse = authService.login(request);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "登录成功");
            response.put("data", authResponse);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.warn("用户登录失败: {}", e.getMessage());
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 刷新访问令牌
     * 
     * @param request 包含刷新令牌的请求
     * @return 新的认证响应
     */
    @PostMapping("/refresh")
    public ResponseEntity<Map<String, Object>> refreshToken(@RequestBody Map<String, String> request) {
        String refreshToken = request.get("refreshToken");
        
        if (refreshToken == null || refreshToken.trim().isEmpty()) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "刷新令牌不能为空");
            
            return ResponseEntity.badRequest().body(response);
        }
        
        try {
            AuthResponse authResponse = authService.refreshToken(refreshToken);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "令牌刷新成功");
            response.put("data", authResponse);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.warn("令牌刷新失败: {}", e.getMessage());
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 用户登出
     * 
     * @param authentication 认证信息
     * @return 响应结果
     */
    @PostMapping("/logout")
    public ResponseEntity<Map<String, Object>> logout(Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()) {
            String username = authentication.getName();
            authService.logout(username);
            
            logger.info("用户登出成功: {}", username);
        }
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "登出成功");
        
        return ResponseEntity.ok(response);
    }

    /**
     * 检查用户名是否可用
     * 
     * @param username 用户名
     * @return 检查结果
     */
    @GetMapping("/check-username")
    public ResponseEntity<Map<String, Object>> checkUsername(@RequestParam String username) {
        boolean available = authService.isUsernameAvailable(username);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("available", available);
        response.put("message", available ? "用户名可用" : "用户名已存在");
        
        return ResponseEntity.ok(response);
    }

    /**
     * 检查邮箱是否可用
     * 
     * @param email 邮箱
     * @return 检查结果
     */
    @GetMapping("/check-email")
    public ResponseEntity<Map<String, Object>> checkEmail(@RequestParam String email) {
        boolean available = authService.isEmailAvailable(email);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("available", available);
        response.put("message", available ? "邮箱可用" : "邮箱已被注册");
        
        return ResponseEntity.ok(response);
    }

    /**
     * 获取当前用户信息
     * 
     * @param authentication 认证信息
     * @return 用户信息
     */
    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> getCurrentUser(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "未认证");
            
            return ResponseEntity.badRequest().body(response);
        }
        
        // 获取用户信息（从认证对象中）
        Object principal = authentication.getPrincipal();
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", principal);
        
        return ResponseEntity.ok(response);
    }

    /**
     * 验证令牌有效性
     * 
     * @param request HTTP请求
     * @param authentication 认证信息
     * @return 验证结果
     */
    @GetMapping("/validate")
    public ResponseEntity<Map<String, Object>> validateToken(HttpServletRequest request, 
                                                           Authentication authentication) {
        Map<String, Object> response = new HashMap<>();
        
        if (authentication != null && authentication.isAuthenticated()) {
            response.put("success", true);
            response.put("valid", true);
            response.put("message", "令牌有效");
            response.put("user", authentication.getPrincipal());
        } else {
            response.put("success", true);
            response.put("valid", false);
            response.put("message", "令牌无效或已过期");
        }
        
        return ResponseEntity.ok(response);
    }
}