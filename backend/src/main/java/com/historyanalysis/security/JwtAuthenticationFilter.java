/**
 * JWT认证过滤器
 * @author AI Agent
 * @version 1.0.0
 * @created 2024-12-29 18:00:00
 */
package com.historyanalysis.security;

import com.historyanalysis.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JWT认证过滤器
 * 从请求头中提取JWT令牌，验证并设置用户认证信息
 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
    
    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    /**
     * 执行JWT认证过滤
     * 
     * @param request HTTP请求
     * @param response HTTP响应
     * @param filterChain 过滤器链
     * @throws ServletException Servlet异常
     * @throws IOException IO异常
     */
    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                   @NonNull HttpServletResponse response,
                                   @NonNull FilterChain filterChain) throws ServletException, IOException {
        
        try {
            // 提取JWT令牌
            String jwt = extractJwtFromRequest(request);
            
            // 如果存在有效的JWT令牌且当前未认证
            if (jwt != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                // 从JWT中提取用户名
                String username = jwtService.extractUsername(jwt);
                
                if (username != null) {
                    // 加载用户详情
                    UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                    
                    // 验证JWT令牌
                    if (jwtService.isTokenValid(jwt, userDetails)) {
                        // 创建认证令牌
                        UsernamePasswordAuthenticationToken authToken = 
                            new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                userDetails.getAuthorities()
                            );
                        
                        // 设置认证详情
                        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        
                        // 设置安全上下文
                        SecurityContextHolder.getContext().setAuthentication(authToken);
                        
                        log.debug("用户 {} 通过JWT认证成功", username);
                    } else {
                        log.debug("JWT令牌验证失败，用户: {}", username);
                    }
                }
            }
        } catch (Exception e) {
            log.error("JWT认证过程中发生异常: {}", e.getMessage());
            // 清除安全上下文
            SecurityContextHolder.clearContext();
        }
        
        // 继续过滤器链
        filterChain.doFilter(request, response);
    }

    /**
     * 从请求中提取JWT令牌
     * 
     * @param request HTTP请求
     * @return JWT令牌，如果不存在则返回null
     */
    private String extractJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader(AUTHORIZATION_HEADER);
        
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER_PREFIX)) {
            return bearerToken.substring(BEARER_PREFIX.length());
        }
        
        return null;
    }

    /**
     * 判断是否应该跳过此过滤器
     * 对于某些路径（如认证端点），可以跳过JWT验证
     * 
     * @param request HTTP请求
     * @return 是否应该跳过
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        
        // 跳过认证相关的端点
        return path.startsWith("/api/auth/") ||
               path.startsWith("/api/public/") ||
               path.equals("/actuator/health") ||
               path.equals("/actuator/info") ||
               path.startsWith("/actuator/") ||
               path.startsWith("/api/swagger-ui/") ||
               path.startsWith("/api/v3/api-docs/");
    }
}