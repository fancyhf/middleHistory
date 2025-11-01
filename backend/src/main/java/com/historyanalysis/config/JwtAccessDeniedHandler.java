/**
 * JWT访问拒绝处理器
 * @author AI Agent
 * @version 1.0.0
 * @created 2024-12-29 17:20:00
 * @description 处理已认证用户访问无权限资源的情况
 */
package com.historyanalysis.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT访问拒绝处理器
 * 
 * 当已认证用户尝试访问无权限的资源时，
 * 返回403禁止访问错误响应
 */
@Component
public class JwtAccessDeniedHandler implements AccessDeniedHandler {

    private static final Logger logger = LoggerFactory.getLogger(JwtAccessDeniedHandler.class);

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                      AccessDeniedException accessDeniedException) throws IOException, ServletException {
        
        logger.warn("用户访问被拒绝: {} {}, 原因: {}", 
                   request.getMethod(), request.getRequestURI(), accessDeniedException.getMessage());
        
        // 设置响应状态码和内容类型
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType("application/json;charset=UTF-8");
        
        // 构建错误响应
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("success", false);
        errorResponse.put("error", "FORBIDDEN");
        errorResponse.put("message", "访问被拒绝，权限不足");
        errorResponse.put("timestamp", System.currentTimeMillis());
        errorResponse.put("path", request.getRequestURI());
        
        // 写入响应
        ObjectMapper objectMapper = new ObjectMapper();
        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
    }
}