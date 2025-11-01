/**
 * 分析任务控制器
 * @author AI Agent
 * @version 1.0.0
 * @created 2024-12-29 17:00:00
 * @description 文本分析、词频统计、时间轴、地理位置分析相关的REST API接口
 */
package com.historyanalysis.controller;

import com.historyanalysis.entity.AnalysisResult;
import com.historyanalysis.entity.WordFrequency;
import com.historyanalysis.entity.TimelineEvent;
import com.historyanalysis.entity.GeoLocation;
import com.historyanalysis.service.AnalysisService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 分析任务控制器
 * 
 * 提供分析相关的REST API接口：
 * - 创建和管理分析任务
 * - 词频分析和统计
 * - 时间轴事件提取
 * - 地理位置识别
 * - 分析结果查询和导出
 */
@RestController
@RequestMapping("/api/analysis")
@CrossOrigin(origins = "*")
public class AnalysisController {

    private static final Logger logger = LoggerFactory.getLogger(AnalysisController.class);

    @Autowired
    private AnalysisService analysisService;

    /**
     * 创建分析任务
     */
    @PostMapping("/create")
    public ResponseEntity<Map<String, Object>> createAnalysis(@RequestBody Map<String, String> request) {
        logger.info("创建分析任务请求, projectId={}, analysisType={}", 
                   request.get("projectId"), request.get("analysisType"));

        Map<String, Object> response = new HashMap<>();

        try {
            String projectId = request.get("projectId");
            String analysisTypeStr = request.get("analysisType");
            String description = request.get("description");

            // 参数验证
            if (!StringUtils.hasText(projectId) || !StringUtils.hasText(analysisTypeStr)) {
                response.put("success", false);
                response.put("message", "项目ID和分析类型不能为空");
                return ResponseEntity.badRequest().body(response);
            }

            // 解析分析类型
            AnalysisResult.AnalysisType analysisType;
            try {
                analysisType = AnalysisResult.AnalysisType.valueOf(analysisTypeStr.toUpperCase());
            } catch (IllegalArgumentException e) {
                response.put("success", false);
                response.put("message", "无效的分析类型: " + analysisTypeStr);
                return ResponseEntity.badRequest().body(response);
            }

            // 创建分析任务
            AnalysisResult analysisResult = analysisService.createAnalysis(projectId, analysisType, description);

            response.put("success", true);
            response.put("message", "分析任务创建成功");
            response.put("analysis", createAnalysisResponse(analysisResult));

            logger.info("分析任务创建成功, analysisId={}, type={}", analysisResult.getId(), analysisType);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            logger.warn("分析任务创建失败: {}", e.getMessage());
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            logger.error("分析任务创建异常: {}", e.getMessage(), e);
            response.put("success", false);
            response.put("message", "分析任务创建失败，请稍后重试");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 执行词频分析
     */
    @PostMapping("/word-frequency")
    public ResponseEntity<Map<String, Object>> executeWordFrequencyAnalysis(@RequestBody Map<String, String> request) {
        logger.info("执行词频分析请求, projectId={}", request.get("projectId"));

        Map<String, Object> response = new HashMap<>();

        try {
            String projectId = request.get("projectId");
            String description = request.get("description");

            // 参数验证
            if (!StringUtils.hasText(projectId)) {
                response.put("success", false);
                response.put("message", "项目ID不能为空");
                return ResponseEntity.badRequest().body(response);
            }

            // 执行词频分析
            boolean success = analysisService.executeWordFrequencyAnalysis(projectId, description);

            if (success) {
                response.put("success", true);
                response.put("message", "词频分析任务已启动");
            } else {
                response.put("success", false);
                response.put("message", "词频分析任务启动失败");
            }

            logger.info("词频分析任务启动, success={}", success);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            logger.warn("词频分析启动失败: {}", e.getMessage());
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            logger.error("词频分析启动异常: {}", e.getMessage(), e);
            response.put("success", false);
            response.put("message", "词频分析启动失败，请稍后重试");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 执行时间轴分析
     */
    @PostMapping("/timeline")
    public ResponseEntity<Map<String, Object>> executeTimelineAnalysis(@RequestBody Map<String, String> request) {
        logger.info("执行时间轴分析请求, projectId={}", request.get("projectId"));

        Map<String, Object> response = new HashMap<>();

        try {
            String projectId = request.get("projectId");
            String description = request.get("description");

            // 参数验证
            if (!StringUtils.hasText(projectId)) {
                response.put("success", false);
                response.put("message", "项目ID不能为空");
                return ResponseEntity.badRequest().body(response);
            }

            // 执行时间轴分析
            boolean success = analysisService.executeTimelineAnalysis(projectId, description);

            if (success) {
                response.put("success", true);
                response.put("message", "时间轴分析任务已启动");
            } else {
                response.put("success", false);
                response.put("message", "时间轴分析任务启动失败");
            }

            logger.info("时间轴分析任务启动, success={}", success);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            logger.warn("时间轴分析启动失败: {}", e.getMessage());
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            logger.error("时间轴分析启动异常: {}", e.getMessage(), e);
            response.put("success", false);
            response.put("message", "时间轴分析启动失败，请稍后重试");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 执行地理位置分析
     */
    @PostMapping("/geography")
    public ResponseEntity<Map<String, Object>> executeGeographyAnalysis(@RequestBody Map<String, String> request) {
        logger.info("执行地理位置分析请求, projectId={}", request.get("projectId"));

        Map<String, Object> response = new HashMap<>();

        try {
            String projectId = request.get("projectId");
            String description = request.get("description");

            // 参数验证
            if (!StringUtils.hasText(projectId)) {
                response.put("success", false);
                response.put("message", "项目ID不能为空");
                return ResponseEntity.badRequest().body(response);
            }

            // 执行地理位置分析
            boolean success = analysisService.executeGeographyAnalysis(projectId, description);

            if (success) {
                response.put("success", true);
                response.put("message", "地理分析任务已启动");
            } else {
                response.put("success", false);
                response.put("message", "地理分析任务启动失败");
            }

            logger.info("地理分析任务启动, success={}", success);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            logger.warn("地理位置分析启动失败: {}", e.getMessage());
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            logger.error("地理位置分析启动异常: {}", e.getMessage(), e);
            response.put("success", false);
            response.put("message", "地理位置分析启动失败，请稍后重试");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 执行多维度分析
     */
    @PostMapping("/multidimensional")
    public ResponseEntity<Map<String, Object>> executeMultidimensionalAnalysis(@RequestBody Map<String, String> request) {
        logger.info("执行多维度分析请求, projectId={}", request.get("projectId"));

        Map<String, Object> response = new HashMap<>();

        try {
            String projectId = request.get("projectId");
            String description = request.get("description");

            // 参数验证
            if (!StringUtils.hasText(projectId)) {
                response.put("success", false);
                response.put("message", "项目ID不能为空");
                return ResponseEntity.badRequest().body(response);
            }

            // 执行多维度分析
            boolean success = analysisService.executeMultidimensionalAnalysis(projectId, description);

            if (success) {
                response.put("success", true);
                response.put("message", "多维度分析任务已启动");
            } else {
                response.put("success", false);
                response.put("message", "多维度分析任务启动失败");
            }

            logger.info("多维度分析任务启动, success={}", success);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            logger.warn("多维度分析启动失败: {}", e.getMessage());
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            logger.error("多维度分析启动异常: {}", e.getMessage(), e);
            response.put("success", false);
            response.put("message", "多维度分析启动失败，请稍后重试");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 执行文本摘要分析
     */
    @PostMapping("/text-summary")
    public ResponseEntity<Map<String, Object>> executeTextSummaryAnalysis(@RequestBody Map<String, String> request) {
        logger.info("执行文本摘要分析请求, projectId={}", request.get("projectId"));

        Map<String, Object> response = new HashMap<>();

        try {
            String projectId = request.get("projectId");
            String description = request.get("description");

            // 参数验证
            if (!StringUtils.hasText(projectId)) {
                response.put("success", false);
                response.put("message", "项目ID不能为空");
                return ResponseEntity.badRequest().body(response);
            }

            // 执行文本摘要分析
            boolean success = analysisService.executeTextSummaryAnalysis(projectId, description);

            if (success) {
                response.put("success", true);
                response.put("message", "文本摘要分析任务已启动");
            } else {
                response.put("success", false);
                response.put("message", "文本摘要分析任务启动失败");
            }

            logger.info("文本摘要分析任务启动, success={}", success);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            logger.warn("文本摘要分析启动失败: {}", e.getMessage());
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            logger.error("文本摘要分析启动异常: {}", e.getMessage(), e);
            response.put("success", false);
            response.put("message", "文本摘要分析启动失败，请稍后重试");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 获取分析结果
     */
    @GetMapping("/{analysisId}")
    public ResponseEntity<Map<String, Object>> getAnalysisResult(@PathVariable String analysisId) {
        logger.debug("获取分析结果, analysisId={}", analysisId);

        Map<String, Object> response = new HashMap<>();

        try {
            Optional<AnalysisResult> analysisOpt = analysisService.findById(analysisId);

            if (analysisOpt.isPresent()) {
                AnalysisResult analysis = analysisOpt.get();
                response.put("success", true);
                response.put("analysis", createAnalysisResponse(analysis));
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "分析结果不存在");
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            logger.error("获取分析结果异常: {}", e.getMessage(), e);
            response.put("success", false);
            response.put("message", "获取分析结果失败");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 删除分析结果
     */
    @DeleteMapping("/{analysisId}")
    public ResponseEntity<Map<String, Object>> deleteAnalysisResult(@PathVariable String analysisId) {
        logger.info("删除分析结果, analysisId={}", analysisId);

        Map<String, Object> response = new HashMap<>();

        try {
            boolean deleted = analysisService.deleteAnalysisResult(analysisId);

            if (deleted) {
                response.put("success", true);
                response.put("message", "分析结果删除成功");
                logger.info("分析结果删除成功, analysisId={}", analysisId);
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "分析结果不存在或删除失败");
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            logger.error("删除分析结果异常: {}", e.getMessage(), e);
            response.put("success", false);
            response.put("message", "删除分析结果失败");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 获取项目分析列表
     */
    @GetMapping("/project/{projectId}")
    public ResponseEntity<Map<String, Object>> getProjectAnalyses(
            @PathVariable String projectId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(required = false) String analysisType,
            @RequestParam(required = false) String status) {
        
        logger.debug("获取项目分析列表, projectId={}, page={}, size={}, type={}, status={}", 
                    projectId, page, size, analysisType, status);

        Map<String, Object> response = new HashMap<>();

        try {
            // 创建分页和排序参数
            Sort.Direction direction = sortDir.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
            Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

            Page<AnalysisResult> analysisPage;

            // 根据条件查询
            if (StringUtils.hasText(analysisType)) {
                try {
                    AnalysisResult.AnalysisType type = AnalysisResult.AnalysisType.valueOf(analysisType.toUpperCase());
                    analysisPage = analysisService.findAnalysesByProjectAndType(projectId, type, pageable);
                } catch (IllegalArgumentException e) {
                    response.put("success", false);
                    response.put("message", "无效的分析类型");
                    return ResponseEntity.badRequest().body(response);
                }
            } else if (StringUtils.hasText(status)) {
                try {
                    AnalysisResult.AnalysisStatus analysisStatus = AnalysisResult.AnalysisStatus.valueOf(status.toUpperCase());
                    analysisPage = analysisService.findAnalysesByProjectAndStatus(projectId, analysisStatus, pageable);
                } catch (IllegalArgumentException e) {
                    response.put("success", false);
                    response.put("message", "无效的分析状态");
                    return ResponseEntity.badRequest().body(response);
                }
            } else {
                analysisPage = analysisService.findAnalysesByProject(projectId, pageable);
            }

            response.put("success", true);
            response.put("analyses", analysisPage.getContent().stream()
                    .map(this::createAnalysisResponse)
                    .toArray());
            response.put("totalElements", analysisPage.getTotalElements());
            response.put("totalPages", analysisPage.getTotalPages());
            response.put("currentPage", analysisPage.getNumber());
            response.put("pageSize", analysisPage.getSize());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("获取项目分析列表异常: {}", e.getMessage(), e);
            response.put("success", false);
            response.put("message", "获取分析列表失败");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 获取词频分析结果
     */
    @GetMapping("/{analysisId}/word-frequency")
    public ResponseEntity<Map<String, Object>> getWordFrequencyResults(
            @PathVariable String analysisId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size,
            @RequestParam(defaultValue = "frequency") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        
        logger.debug("获取词频分析结果, analysisId={}, page={}, size={}", analysisId, page, size);

        Map<String, Object> response = new HashMap<>();

        try {
            // 创建分页和排序参数
            Sort.Direction direction = sortDir.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
            Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

            Page<WordFrequency> wordPage = analysisService.getWordFrequencyResults(analysisId, pageable);

            response.put("success", true);
            response.put("words", wordPage.getContent().stream()
                    .map(this::createWordFrequencyResponse)
                    .toArray());
            response.put("totalElements", wordPage.getTotalElements());
            response.put("totalPages", wordPage.getTotalPages());
            response.put("currentPage", wordPage.getNumber());
            response.put("pageSize", wordPage.getSize());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("获取词频分析结果异常: {}", e.getMessage(), e);
            response.put("success", false);
            response.put("message", "获取词频结果失败");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 获取时间轴分析结果
     */
    @GetMapping("/{analysisId}/timeline")
    public ResponseEntity<Map<String, Object>> getTimelineResults(
            @PathVariable String analysisId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size,
            @RequestParam(defaultValue = "eventDate") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        
        logger.debug("获取时间轴分析结果, analysisId={}, page={}, size={}", analysisId, page, size);

        Map<String, Object> response = new HashMap<>();

        try {
            // 创建分页和排序参数
            Sort.Direction direction = sortDir.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
            Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

            Page<TimelineEvent> eventPage = analysisService.getTimelineResults(analysisId, pageable);

            response.put("success", true);
            response.put("events", eventPage.getContent().stream()
                    .map(this::createTimelineEventResponse)
                    .toArray());
            response.put("totalElements", eventPage.getTotalElements());
            response.put("totalPages", eventPage.getTotalPages());
            response.put("currentPage", eventPage.getNumber());
            response.put("pageSize", eventPage.getSize());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("获取时间轴分析结果异常: {}", e.getMessage(), e);
            response.put("success", false);
            response.put("message", "获取时间轴结果失败");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 获取地理位置分析结果
     */
    @GetMapping("/{analysisId}/geography")
    public ResponseEntity<Map<String, Object>> getGeographyResults(
            @PathVariable String analysisId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size,
            @RequestParam(defaultValue = "locationName") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        
        logger.debug("获取地理位置分析结果, analysisId={}, page={}, size={}", analysisId, page, size);

        Map<String, Object> response = new HashMap<>();

        try {
            // 创建分页和排序参数
            Sort.Direction direction = sortDir.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
            Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

            Page<GeoLocation> locationPage = analysisService.getGeographyResults(analysisId, pageable);

            response.put("success", true);
            response.put("locations", locationPage.getContent().stream()
                    .map(this::createGeoLocationResponse)
                    .toArray());
            response.put("totalElements", locationPage.getTotalElements());
            response.put("totalPages", locationPage.getTotalPages());
            response.put("currentPage", locationPage.getNumber());
            response.put("pageSize", locationPage.getSize());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("获取地理位置分析结果异常: {}", e.getMessage(), e);
            response.put("success", false);
            response.put("message", "获取地理位置结果失败");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 导出分析结果
     */
    @GetMapping("/{analysisId}/export")
    public ResponseEntity<Map<String, Object>> exportAnalysisResult(@PathVariable String analysisId,
                                                                  @RequestParam(defaultValue = "json") String format) {
        logger.info("导出分析结果, analysisId={}, format={}", analysisId, format);

        Map<String, Object> response = new HashMap<>();

        try {
            String exportPath = analysisService.exportAnalysisResult(analysisId, format);

            response.put("success", true);
            response.put("message", "分析结果导出成功");
            response.put("exportPath", exportPath);

            logger.info("分析结果导出成功, analysisId={}, exportPath={}", analysisId, exportPath);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            logger.warn("分析结果导出失败: {}", e.getMessage());
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            logger.error("分析结果导出异常: {}", e.getMessage(), e);
            response.put("success", false);
            response.put("message", "分析结果导出失败");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 获取分析统计信息
     */
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getAnalysisStatistics(
            @RequestParam(required = false) String projectId,
            @RequestParam(required = false) String userId) {
        
        logger.debug("获取分析统计信息, projectId={}, userId={}", projectId, userId);

        Map<String, Object> response = new HashMap<>();

        try {
            Map<String, Long> statistics;
            
            if (StringUtils.hasText(userId)) {
                statistics = analysisService.getAnalysisStatisticsByUser(userId);
            } else if (StringUtils.hasText(projectId)) {
                statistics = analysisService.getAnalysisStatisticsByProject(projectId);
            } else {
                statistics = analysisService.getAnalysisStatistics();
            }

            response.put("success", true);
            response.put("statistics", statistics);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("获取分析统计信息异常: {}", e.getMessage(), e);
            response.put("success", false);
            response.put("message", "获取统计信息失败");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // ========== 私有辅助方法 ==========

    /**
     * 创建分析结果响应对象
     */
    private Map<String, Object> createAnalysisResponse(AnalysisResult analysis) {
        Map<String, Object> analysisResponse = new HashMap<>();
        analysisResponse.put("id", analysis.getId());
        analysisResponse.put("analysisType", analysis.getAnalysisType().toString());
        analysisResponse.put("status", analysis.getStatus().toString());
        analysisResponse.put("description", analysis.getDescription());
        analysisResponse.put("projectId", analysis.getProject().getId());
        analysisResponse.put("projectTitle", analysis.getProject().getTitle());
        analysisResponse.put("createdAt", analysis.getCreatedAt());
        analysisResponse.put("updatedAt", analysis.getUpdatedAt());
        // 使用 createdAt 作为 startedAt，updatedAt 作为 completedAt（如果已完成）
        analysisResponse.put("startedAt", analysis.getCreatedAt());
        analysisResponse.put("completedAt", analysis.getStatus() == AnalysisResult.AnalysisStatus.COMPLETED ? analysis.getUpdatedAt() : null);
        analysisResponse.put("errorMessage", analysis.getErrorMessage());
        analysisResponse.put("resultData", analysis.getResultData());
        return analysisResponse;
    }

    /**
     * 创建词频响应对象
     */
    private Map<String, Object> createWordFrequencyResponse(WordFrequency word) {
        Map<String, Object> wordResponse = new HashMap<>();
        wordResponse.put("id", word.getId());
        wordResponse.put("word", word.getWord());
        wordResponse.put("frequency", word.getFrequency());
        wordResponse.put("category", word.getCategory());
        wordResponse.put("relevanceScore", word.getRelevanceScore());
        return wordResponse;
    }

    /**
     * 创建时间轴事件响应对象
     */
    private Map<String, Object> createTimelineEventResponse(TimelineEvent event) {
        Map<String, Object> eventResponse = new HashMap<>();
        eventResponse.put("id", event.getId());
        eventResponse.put("eventName", event.getEventName());
        eventResponse.put("eventDescription", event.getDescription()); // 使用 getDescription() 而不是 getEventDescription()
        eventResponse.put("eventDate", event.getEventDate());
        eventResponse.put("dateType", "SPECIFIC"); // 默认值，因为实体类中没有 dateType 字段
        eventResponse.put("confidence", 1.0); // 默认值，因为实体类中没有 confidence 字段
        return eventResponse;
    }

    /**
     * 创建地理位置响应对象
     */
    private Map<String, Object> createGeoLocationResponse(GeoLocation location) {
        Map<String, Object> locationResponse = new HashMap<>();
        locationResponse.put("id", location.getId());
        locationResponse.put("locationName", location.getLocationName());
        locationResponse.put("locationType", location.getLocationType());
        locationResponse.put("latitude", location.getLatitude());
        locationResponse.put("longitude", location.getLongitude());
        locationResponse.put("confidence", 1.0); // 默认值，因为实体类中没有 confidence 字段
        locationResponse.put("metadata", location.getMetadata());
        return locationResponse;
    }
}