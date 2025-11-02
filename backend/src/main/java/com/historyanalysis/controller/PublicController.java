/**
 * 公开API控制器
 * @author AI Agent
 * @version 1.0.0
 * @created 2024-12-29 20:30:00
 * @description 提供不需要认证的公开API接口
 */
package com.historyanalysis.controller;

import com.historyanalysis.service.AnalysisService;
import com.historyanalysis.service.ProjectService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 公开API控制器
 * 
 * 提供不需要认证的公开API接口：
 * - 公开统计数据
 * - 系统状态信息
 * - 演示数据
 */
@RestController
@RequestMapping("/public")
@CrossOrigin(origins = "*")
public class PublicController {

    private static final Logger logger = LoggerFactory.getLogger(PublicController.class);

    @Autowired
    private AnalysisService analysisService;

    @Autowired
    private ProjectService projectService;

    /**
     * 获取公开统计数据
     * 
     * @return 公开统计信息
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getPublicStats() {
        logger.debug("获取公开统计数据");

        try {
            Map<String, Object> response = new HashMap<>();
            Map<String, Object> data = new HashMap<>();

            // 获取基本统计数据（不涉及用户隐私）
            long totalDocuments = 0;
            long totalAnalysis = 0;

            try {
                // 尝试获取实际统计数据
                // 暂时使用默认值，后续可以实现具体的统计逻辑
                totalDocuments = 0;
                totalAnalysis = 0;
            } catch (Exception e) {
                logger.warn("获取统计数据失败，使用默认值: {}", e.getMessage());
                // 使用默认值，不影响接口正常返回
            }

            data.put("totalDocuments", totalDocuments);
            data.put("totalAnalysis", totalAnalysis);
            data.put("systemStatus", "运行中");
            data.put("lastUpdate", LocalDateTime.now());

            // 添加一些演示数据
            data.put("recentAnalysis", java.util.List.of(
                Map.of(
                    "title", "历史文档分析示例",
                    "type", "词频分析",
                    "date", LocalDateTime.now().minusDays(1)
                ),
                Map.of(
                    "title", "时间轴提取示例",
                    "type", "时间轴分析",
                    "date", LocalDateTime.now().minusDays(2)
                )
            ));

            response.put("success", true);
            response.put("message", "获取公开统计数据成功");
            response.put("data", data);
            response.put("timestamp", LocalDateTime.now());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("获取公开统计数据异常: {}", e.getMessage(), e);

            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "获取统计数据失败");
            response.put("timestamp", LocalDateTime.now());

            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * 获取系统信息
     * 
     * @return 系统基本信息
     */
    @GetMapping("/info")
    public ResponseEntity<Map<String, Object>> getSystemInfo() {
        logger.debug("获取系统信息");

        Map<String, Object> response = new HashMap<>();
        Map<String, Object> data = new HashMap<>();

        data.put("name", "历史数据统计分析工具");
        data.put("version", "1.0.0");
        data.put("description", "基于Spring Boot的历史数据统计分析工具");
        data.put("status", "运行中");
        data.put("timestamp", LocalDateTime.now());

        response.put("success", true);
        response.put("data", data);

        return ResponseEntity.ok(response);
    }
}