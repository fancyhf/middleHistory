/**
 * 项目管理控制器
 * @author AI Agent
 * @version 1.0.0
 * @created 2024-12-29 18:15:00
 */
package com.historyanalysis.controller;

import com.historyanalysis.dto.ProjectRequest;
import com.historyanalysis.dto.ProjectResponse;
import com.historyanalysis.service.ProjectService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 项目管理控制器
 * 处理项目相关的HTTP请求
 */
@RestController
@RequestMapping("/projects")
@RequiredArgsConstructor
public class ProjectController {

    private static final Logger log = LoggerFactory.getLogger(ProjectController.class);
    private final ProjectService projectService;

    /**
     * 创建项目
     * 
     * @param request 项目创建请求
     * @param authentication 认证信息
     * @return 创建结果
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> createProject(
            @Valid @RequestBody ProjectRequest request,
            Authentication authentication) {
        
        log.info("收到创建项目请求: {}", request.getName());
        
        try {
            ProjectResponse project = projectService.createProject(request, authentication);
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "项目创建成功");
            result.put("data", project);
            
            return ResponseEntity.ok(result);
            
        } catch (IllegalArgumentException e) {
            log.warn("项目创建失败: {}", e.getMessage());
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", e.getMessage());
            
            return ResponseEntity.badRequest().body(result);
            
        } catch (Exception e) {
            log.error("项目创建异常: {}", e.getMessage(), e);
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", "项目创建失败，请稍后重试");
            
            return ResponseEntity.internalServerError().body(result);
        }
    }

    /**
     * 获取用户项目列表
     * 
     * @param page 页码（从0开始）
     * @param size 每页大小
     * @param sort 排序字段
     * @param order 排序方向（asc/desc）
     * @param authentication 认证信息
     * @return 项目列表
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getUserProjects(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "updateTime") String sort,
            @RequestParam(defaultValue = "desc") String order,
            Authentication authentication) {
        
        try {
            // 创建排序对象
            Sort.Direction direction = "asc".equalsIgnoreCase(order) ? 
                    Sort.Direction.ASC : Sort.Direction.DESC;
            Sort sortObj = Sort.by(direction, sort);
            
            // 创建分页对象
            Pageable pageable = PageRequest.of(page, size, sortObj);
            
            // 获取项目列表
            Page<ProjectResponse> projects = projectService.getUserProjects(pageable, authentication);
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("data", projects.getContent());
            result.put("pagination", Map.of(
                "current", page + 1,
                "pageSize", size,
                "total", projects.getTotalElements(),
                "totalPages", projects.getTotalPages()
            ));
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            log.error("获取项目列表失败: {}", e.getMessage(), e);
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", e.getMessage());
            
            return ResponseEntity.badRequest().body(result);
        }
    }

    /**
     * 根据ID获取项目详情
     * 
     * @param projectId 项目ID
     * @param authentication 认证信息
     * @return 项目详情
     */
    @GetMapping("/{projectId}")
    public ResponseEntity<Map<String, Object>> getProjectById(
            @PathVariable Long projectId,
            Authentication authentication) {
        
        try {
            ProjectResponse project = projectService.getProjectById(projectId, authentication);
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("data", project);
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            log.error("获取项目详情失败: {}", e.getMessage(), e);
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", e.getMessage());
            
            return ResponseEntity.badRequest().body(result);
        }
    }

    /**
     * 更新项目
     * 
     * @param projectId 项目ID
     * @param request 更新请求
     * @param authentication 认证信息
     * @return 更新结果
     */
    @PutMapping("/{projectId}")
    public ResponseEntity<Map<String, Object>> updateProject(
            @PathVariable Long projectId,
            @Valid @RequestBody ProjectRequest request,
            Authentication authentication) {
        
        log.info("收到更新项目请求: ID={}, 名称={}", projectId, request.getName());
        
        try {
            ProjectResponse project = projectService.updateProject(projectId, request, authentication);
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "项目更新成功");
            result.put("data", project);
            
            return ResponseEntity.ok(result);
            
        } catch (IllegalArgumentException e) {
            log.warn("项目更新失败: {}", e.getMessage());
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", e.getMessage());
            
            return ResponseEntity.badRequest().body(result);
            
        } catch (Exception e) {
            log.error("项目更新异常: {}", e.getMessage(), e);
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", "项目更新失败，请稍后重试");
            
            return ResponseEntity.internalServerError().body(result);
        }
    }

    /**
     * 删除项目
     * 
     * @param projectId 项目ID
     * @param authentication 认证信息
     * @return 删除结果
     */
    @DeleteMapping("/{projectId}")
    public ResponseEntity<Map<String, Object>> deleteProject(
            @PathVariable Long projectId,
            Authentication authentication) {
        
        try {
            projectService.deleteProject(projectId, authentication);
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "项目删除成功");
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            log.error("删除项目失败: {}", e.getMessage(), e);
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", e.getMessage());
            
            return ResponseEntity.badRequest().body(result);
        }
    }

    /**
     * 搜索项目
     * 
     * @param keyword 搜索关键词
     * @param page 页码
     * @param size 每页大小
     * @param authentication 认证信息
     * @return 搜索结果
     */
    @GetMapping("/search")
    public ResponseEntity<Map<String, Object>> searchProjects(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Authentication authentication) {
        
        try {
            Pageable pageable = PageRequest.of(page, size, 
                    Sort.by(Sort.Direction.DESC, "updateTime"));
            
            Page<ProjectResponse> projects = projectService.searchProjects(
                    keyword, pageable, authentication);
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("data", projects.getContent());
            result.put("pagination", Map.of(
                "current", page + 1,
                "pageSize", size,
                "total", projects.getTotalElements(),
                "totalPages", projects.getTotalPages()
            ));
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            log.error("搜索项目失败: {}", e.getMessage(), e);
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", e.getMessage());
            
            return ResponseEntity.badRequest().body(result);
        }
    }

    /**
     * 获取最近项目
     * 
     * @param limit 限制数量
     * @param authentication 认证信息
     * @return 最近项目列表
     */
    @GetMapping("/recent")
    public ResponseEntity<Map<String, Object>> getRecentProjects(
            @RequestParam(defaultValue = "5") int limit,
            Authentication authentication) {
        
        try {
            List<ProjectResponse> projects = projectService.getRecentProjects(limit, authentication);
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("data", projects);
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            log.error("获取最近项目失败: {}", e.getMessage(), e);
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", e.getMessage());
            
            return ResponseEntity.badRequest().body(result);
        }
    }

    /**
     * 获取项目统计信息
     * 
     * @param authentication 认证信息
     * @return 统计信息
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getProjectStats(Authentication authentication) {
        
        try {
            ProjectResponse.ProjectStats stats = projectService.getUserProjectStats(authentication);
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("data", stats);
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            log.error("获取项目统计失败: {}", e.getMessage(), e);
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", e.getMessage());
            
            return ResponseEntity.badRequest().body(result);
        }
    }

    /**
     * 归档项目
     * 
     * @param projectId 项目ID
     * @param authentication 认证信息
     * @return 归档结果
     */
    @PostMapping("/{projectId}/archive")
    public ResponseEntity<Map<String, Object>> archiveProject(
            @PathVariable Long projectId,
            Authentication authentication) {
        
        try {
            projectService.archiveProject(projectId, authentication);
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "项目归档成功");
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            log.error("归档项目失败: {}", e.getMessage(), e);
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", e.getMessage());
            
            return ResponseEntity.badRequest().body(result);
        }
    }

    /**
     * 恢复项目
     * 
     * @param projectId 项目ID
     * @param authentication 认证信息
     * @return 恢复结果
     */
    @PostMapping("/{projectId}/restore")
    public ResponseEntity<Map<String, Object>> restoreProject(
            @PathVariable Long projectId,
            Authentication authentication) {
        
        try {
            projectService.restoreProject(projectId, authentication);
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "项目恢复成功");
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            log.error("恢复项目失败: {}", e.getMessage(), e);
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", e.getMessage());
            
            return ResponseEntity.badRequest().body(result);
        }
    }
}