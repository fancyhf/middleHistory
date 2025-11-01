/**
 * 首页控制器
 * @author AI Agent
 * @version 1.0.0
 * @created 2024-12-29 17:00:00
 * @description 处理根路径访问，提供欢迎页面
 */
package com.historyanalysis.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * 首页控制器
 * 
 * 由于应用配置了context-path为/api，此控制器实际处理/api/路径
 * 但通过特殊配置可以处理根路径访问
 */
@Controller
@RequestMapping("/")
public class IndexController {

    private static final Logger logger = LoggerFactory.getLogger(IndexController.class);

    /**
     * 处理根路径访问，返回HTML页面
     * 
     * @return HTML响应
     */
    @GetMapping(value = "/", produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> index() {
        logger.debug("访问根路径，返回欢迎页面");

        try {
            // 读取静态HTML文件
            ClassPathResource resource = new ClassPathResource("static/index.html");
            String html = StreamUtils.copyToString(resource.getInputStream(), StandardCharsets.UTF_8);
            
            return ResponseEntity.ok()
                    .contentType(MediaType.TEXT_HTML)
                    .body(html);
                    
        } catch (IOException e) {
            logger.error("读取index.html文件失败: {}", e.getMessage(), e);
            
            // 返回简单的HTML响应
            String fallbackHtml = """
                <!DOCTYPE html>
                <html lang="zh-CN">
                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                    <title>历史数据统计分析工具</title>
                    <style>
                        body { font-family: Arial, sans-serif; text-align: center; padding: 50px; }
                        .container { max-width: 600px; margin: 0 auto; }
                        h1 { color: #333; }
                        .api-link { color: #007bff; text-decoration: none; }
                        .api-link:hover { text-decoration: underline; }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <h1>🏛️ 历史数据统计分析工具</h1>
                        <p>欢迎使用历史数据统计分析工具！</p>
                        <p>服务正在运行中...</p>
                        <p>
                            <a href="/api" class="api-link">访问 API 接口</a> |
                            <a href="/api/actuator/health" class="api-link">健康检查</a> |
                            <a href="/api/swagger-ui/index.html" class="api-link">API 文档</a>
                        </p>
                        <p><small>版本: 1.0.0 | 端口: 8080</small></p>
                    </div>
                </body>
                </html>
                """;
            
            return ResponseEntity.ok()
                    .contentType(MediaType.TEXT_HTML)
                    .body(fallbackHtml);
        }
    }
}