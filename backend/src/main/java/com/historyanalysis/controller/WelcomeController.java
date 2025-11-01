/**
 * 欢迎页面控制器
 * @author AI Agent
 * @version 1.0.0
 * @created 2024-12-29 17:00:00
 * @description 处理根路径访问和系统信息展示
 */
package com.historyanalysis.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 欢迎页面控制器
 * 
 * 提供系统基本信息和欢迎页面：
 * - 根路径欢迎信息
 * - 系统状态和版本信息
 * - API文档链接
 */
@RestController
@CrossOrigin(origins = "*")
public class WelcomeController {

    private static final Logger logger = LoggerFactory.getLogger(WelcomeController.class);

    @Value("${spring.application.name:历史数据统计分析工具}")
    private String applicationName;

    @Value("${spring.application.version:1.0.0}")
    private String applicationVersion;

    @Value("${spring.application.description:基于Spring Boot的历史数据统计分析工具}")
    private String applicationDescription;

    /**
     * 根路径欢迎页面
     * 
     * @return 系统信息和欢迎消息
     */
    @GetMapping("/")
    public ResponseEntity<Map<String, Object>> welcome() {
        logger.debug("访问根路径欢迎页面");

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "欢迎使用历史数据统计分析工具");
        response.put("application", Map.of(
            "name", applicationName,
            "version", applicationVersion,
            "description", applicationDescription
        ));
        response.put("timestamp", LocalDateTime.now());
        response.put("endpoints", Map.of(
            "health", "/api/actuator/health",
            "api_docs", "/api/v3/api-docs",
            "swagger_ui", "/api/swagger-ui/index.html",
            "auth", Map.of(
                "login", "/api/auth/login",
                "register", "/api/auth/register"
            ),
            "api_base", "/api"
        ));

        return ResponseEntity.ok(response);
    }

    /**
     * API根路径信息
     * 
     * @return API基本信息
     */
    @GetMapping("/api")
    public ResponseEntity<Map<String, Object>> apiInfo() {
        logger.debug("访问API根路径");

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "历史数据统计分析工具 API");
        response.put("version", "v1.0");
        response.put("timestamp", LocalDateTime.now());
        response.put("available_endpoints", Map.of(
            "authentication", "/api/auth/*",
            "users", "/api/users/*",
            "projects", "/api/projects/*",
            "files", "/api/files/*",
            "analysis", "/api/analysis/*",
            "health_check", "/actuator/health",
            "api_documentation", "/api/v3/api-docs"
        ));

        return ResponseEntity.ok(response);
    }
}