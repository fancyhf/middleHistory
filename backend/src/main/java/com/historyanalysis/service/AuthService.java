/**
 * 认证服务类
 * @author AI Agent
 * @version 1.0.0
 * @created 2024-12-29 18:00:00
 */
package com.historyanalysis.service;

import com.historyanalysis.dto.AuthRequest;
import com.historyanalysis.dto.AuthResponse;
import com.historyanalysis.dto.RegisterRequest;
import com.historyanalysis.entity.User;
import com.historyanalysis.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Qualifier;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * 认证服务类
 * 处理用户注册、登录、令牌刷新等认证相关业务
 */
@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final UserService userService;
    private final UserDetailsService userDetailsService;
    
    public AuthService(UserRepository userRepository, 
                      PasswordEncoder passwordEncoder,
                      JwtService jwtService,
                      AuthenticationManager authenticationManager,
                      UserService userService,
                      @Qualifier("customUserDetailsService") UserDetailsService userDetailsService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
        this.userService = userService;
        this.userDetailsService = userDetailsService;
    }

    /**
     * 用户注册
     * 
     * @param request 注册请求
     * @return 认证响应
     */
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        log.info("用户注册请求: {}", request.getUsername());

        // 验证密码一致性
        if (!request.isPasswordMatched()) {
            throw new IllegalArgumentException("两次输入的密码不一致");
        }

        // 检查用户名是否已存在
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("用户名已存在: " + request.getUsername());
        }

        // 检查邮箱是否已存在
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("邮箱已被注册: " + request.getEmail());
        }

        // 创建新用户
        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setRealName(request.getRealName());
        user.setRole(User.UserRole.USER);
        user.setStatus(User.UserStatus.ACTIVE);

        // 保存用户
        user = userRepository.save(user);
        log.info("用户注册成功: {}, ID: {}", user.getUsername(), user.getId());

        // 生成JWT令牌
        String accessToken = jwtService.generateToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        // 构建响应
        return buildAuthResponse(user, accessToken, refreshToken);
    }

    /**
     * 用户登录
     * 
     * @param request 登录请求
     * @return 认证响应
     */
    @Transactional
    public AuthResponse login(AuthRequest request) {
        log.info("用户登录请求: {}", request.getUsername());

        try {
            // 执行认证
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    request.getUsername(),
                    request.getPassword()
                )
            );

            // 获取用户详情
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            User user = (User) userDetails;

            // 检查用户状态
            validateUserStatus(user);

            // 更新最后登录信息
            updateLastLoginInfo(user);

            // 生成JWT令牌
            String accessToken = jwtService.generateToken(userDetails);
            String refreshToken = jwtService.generateRefreshToken(userDetails);

            log.info("用户登录成功: {}, ID: {}", user.getUsername(), user.getId());

            // 构建响应
            return buildAuthResponse(user, accessToken, refreshToken);

        } catch (BadCredentialsException e) {
            log.warn("用户登录失败 - 凭证错误: {}", request.getUsername());
            throw new BadCredentialsException("用户名或密码错误");
        } catch (DisabledException e) {
            log.warn("用户登录失败 - 账户已禁用: {}", request.getUsername());
            throw new DisabledException("账户已被禁用");
        } catch (LockedException e) {
            log.warn("用户登录失败 - 账户已锁定: {}", request.getUsername());
            throw new LockedException("账户已被锁定");
        }
    }

    /**
     * 刷新访问令牌
     * 
     * @param refreshToken 刷新令牌
     * @return 认证响应
     */
    @Transactional(readOnly = true)
    public AuthResponse refreshToken(String refreshToken) {
        log.debug("刷新令牌请求");

        try {
            // 从刷新令牌中提取用户名
            String username = jwtService.extractUsername(refreshToken);
            
            if (username != null) {
                // 加载用户详情
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                
                // 验证刷新令牌
                if (jwtService.isTokenValid(refreshToken, userDetails)) {
                    User user = (User) userDetails;
                    
                    // 检查用户状态
                    validateUserStatus(user);
                    
                    // 生成新的访问令牌
                    String newAccessToken = jwtService.generateToken(userDetails);
                    
                    log.debug("令牌刷新成功: {}", username);
                    
                    // 构建响应（保持原刷新令牌）
                    return buildAuthResponse(user, newAccessToken, refreshToken);
                }
            }
            
            throw new IllegalArgumentException("无效的刷新令牌");
            
        } catch (Exception e) {
            log.warn("令牌刷新失败: {}", e.getMessage());
            throw new IllegalArgumentException("刷新令牌已过期或无效");
        }
    }

    /**
     * 用户登出
     * 
     * @param username 用户名
     */
    @Transactional(readOnly = true)
    public void logout(String username) {
        log.info("用户登出: {}", username);
        // 在实际应用中，可以将令牌加入黑名单
        // 这里暂时只记录日志
    }

    /**
     * 验证用户状态
     * 
     * @param user 用户实体
     */
    private void validateUserStatus(User user) {
        switch (user.getStatus()) {
            case INACTIVE:
                throw new DisabledException("账户未激活");
            case LOCKED:
                throw new LockedException("账户已被锁定");
            case DELETED:
                throw new DisabledException("账户不存在");
            case ACTIVE:
                // 正常状态，继续处理
                break;
            default:
                throw new DisabledException("账户状态异常");
        }
    }

    /**
     * 更新用户最后登录信息
     * 
     * @param user 用户实体
     */
    private void updateLastLoginInfo(User user) {
        try {
            userRepository.updateLastLoginInfo(
                user.getId(),
                LocalDateTime.now(),
                "127.0.0.1" // 在实际应用中应该获取真实IP
            );
        } catch (Exception e) {
            log.warn("更新用户最后登录信息失败: {}", e.getMessage());
            // 不影响登录流程
        }
    }

    /**
     * 构建认证响应
     * 
     * @param user 用户实体
     * @param accessToken 访问令牌
     * @param refreshToken 刷新令牌
     * @return 认证响应
     */
    private AuthResponse buildAuthResponse(User user, String accessToken, String refreshToken) {
        // 计算令牌过期时间
        long expiresIn = jwtService.getTokenRemainingTime(accessToken) / 1000;

        // 构建用户信息
        AuthResponse.UserInfo userInfo = new AuthResponse.UserInfo();
        userInfo.setId(user.getId());
        userInfo.setUsername(user.getUsername());
        userInfo.setEmail(user.getEmail());
        userInfo.setRealName(user.getRealName());
        userInfo.setAvatarUrl(user.getAvatarUrl());
        userInfo.setRole(user.getRole().name());
        userInfo.setStatus(user.getStatus().name());
        userInfo.setLastLoginTime(user.getLastLoginAt());
        userInfo.setCreatedAt(user.getCreatedAt());

        // 构建响应
        AuthResponse response = new AuthResponse();
        response.setAccessToken(accessToken);
        response.setRefreshToken(refreshToken);
        response.setTokenType("Bearer");
        response.setExpiresIn(expiresIn);
        response.setUser(userInfo);
        return response;
    }

    /**
     * 检查用户名是否可用
     * 
     * @param username 用户名
     * @return 是否可用
     */
    @Transactional(readOnly = true)
    public boolean isUsernameAvailable(String username) {
        return !userRepository.existsByUsername(username);
    }

    /**
     * 检查邮箱是否可用
     * 
     * @param email 邮箱
     * @return 是否可用
     */
    @Transactional(readOnly = true)
    public boolean isEmailAvailable(String email) {
        return !userRepository.existsByEmail(email);
    }
}