/**
 * JWT认证入口点
 * @author AI Agent
 * @version 1.0.0
 * @created 2024-12-29 18:00:00
 */
package com.historyanalysis.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT认证入口点
 * 处理未认证用户访问受保护资源时的响应
 */
@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationEntryPoint.class);

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 处理认证异常
     * 当用户未认证或认证失败时，返回统一的错误响应
     * 
     * @param request HTTP请求
     * @param response HTTP响应
     * @param authException 认证异常
     * @throws IOException IO异常
     * @throws ServletException Servlet异常
     */
    @Override
    public void commence(HttpServletRequest request, 
                        HttpServletResponse response,
                        AuthenticationException authException) throws IOException, ServletException {
        
        log.warn("未认证用户尝试访问受保护资源: {} {}", request.getMethod(), request.getRequestURI());
        log.debug("认证异常详情: {}", authException.getMessage());

        // 设置响应状态码和内容类型
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        // 构建错误响应体
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("success", false);
        errorResponse.put("code", "UNAUTHORIZED");
        errorResponse.put("message", "访问被拒绝：请先登录");
        errorResponse.put("timestamp", LocalDateTime.now().toString());
        errorResponse.put("path", request.getRequestURI());
        
        // 根据异常类型提供更具体的错误信息
        String detailMessage = getDetailMessage(authException);
        if (detailMessage != null) {
            errorResponse.put("detail", detailMessage);
        }

        // 写入响应
        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
        response.getWriter().flush();
    }

    /**
     * 根据认证异常类型获取详细错误信息
     * 
     * @param authException 认证异常
     * @return 详细错误信息
     */
    private String getDetailMessage(AuthenticationException authException) {
        String exceptionClass = authException.getClass().getSimpleName();
        
        switch (exceptionClass) {
            case "BadCredentialsException":
                return "用户名或密码错误";
            case "DisabledException":
                return "账户已被禁用";
            case "AccountExpiredException":
                return "账户已过期";
            case "LockedException":
                return "账户已被锁定";
            case "CredentialsExpiredException":
                return "凭证已过期";
            case "InsufficientAuthenticationException":
                return "认证信息不足，请提供有效的访问令牌";
            default:
                return "认证失败，请检查您的登录状态";
        }
    }
}