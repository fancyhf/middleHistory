/**
 * 文件管理控制器
 * @author AI Agent
 * @version 1.0.0
 * @created 2024-12-29 18:00:00
 */
package com.historyanalysis.controller;

import com.historyanalysis.dto.FileUploadResponse;
import com.historyanalysis.entity.UploadedFile;
import com.historyanalysis.service.FileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

/**
 * 文件管理控制器
 * 处理文件上传、下载、查询、删除等HTTP请求
 */
@RestController
@RequestMapping("/files")
@RequiredArgsConstructor
@Slf4j
public class FileController {

    private final FileService fileService;

    /**
     * 上传文件
     * 
     * @param file 上传的文件
     * @param projectId 项目ID
     * @param authentication 认证信息
     * @return 上传结果
     */
    @PostMapping("/upload")
    public ResponseEntity<Map<String, Object>> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam("projectId") Long projectId,
            Authentication authentication) {
        
        log.info("收到文件上传请求: {}, 项目ID: {}", file.getOriginalFilename(), projectId);
        
        try {
            String userId = authentication.getName();
            UploadedFile uploadedFile = fileService.uploadFile(projectId.toString(), userId, file);
            FileUploadResponse response = new FileUploadResponse(uploadedFile);
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "文件上传成功");
            result.put("data", response);
            
            return ResponseEntity.ok(result);
            
        } catch (IllegalArgumentException e) {
            log.warn("文件上传失败: {}", e.getMessage());
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", e.getMessage());
            
            return ResponseEntity.badRequest().body(result);
            
        } catch (Exception e) {
            log.error("文件上传异常: {}", e.getMessage(), e);
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", "文件上传失败，请稍后重试");
            
            return ResponseEntity.internalServerError().body(result);
        }
    }

    /**
     * 获取文件列表
     * 
     * @param projectId 项目ID
     * @param page 页码（从0开始）
     * @param size 每页大小
     * @param sort 排序字段
     * @param order 排序方向（asc/desc）
     * @param authentication 认证信息
     * @return 文件列表
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getFiles(
            @RequestParam Long projectId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "uploadTime") String sort,
            @RequestParam(defaultValue = "desc") String order,
            Authentication authentication) {
        
        try {
            // 创建排序对象
            Sort.Direction direction = "asc".equalsIgnoreCase(order) ? 
                    Sort.Direction.ASC : Sort.Direction.DESC;
            Sort sortObj = Sort.by(direction, sort);
            
            // 创建分页对象
            Pageable pageable = PageRequest.of(page, size, sortObj);
            
            // 获取文件列表
            Page<FileUploadResponse> files = fileService.getFiles(projectId, pageable, authentication);
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("data", files.getContent());
            result.put("pagination", Map.of(
                "current", page + 1,
                "pageSize", size,
                "total", files.getTotalElements(),
                "totalPages", files.getTotalPages()
            ));
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            log.error("获取文件列表失败: {}", e.getMessage(), e);
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", e.getMessage());
            
            return ResponseEntity.badRequest().body(result);
        }
    }

    /**
     * 根据ID获取文件信息
     * 
     * @param fileId 文件ID
     * @param authentication 认证信息
     * @return 文件信息
     */
    @GetMapping("/{fileId}")
    public ResponseEntity<Map<String, Object>> getFileById(
            @PathVariable Long fileId,
            Authentication authentication) {
        
        try {
            FileUploadResponse file = fileService.getFileById(fileId, authentication);
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("data", file);
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            log.error("获取文件信息失败: {}", e.getMessage(), e);
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", e.getMessage());
            
            return ResponseEntity.badRequest().body(result);
        }
    }

    /**
     * 删除文件
     * 
     * @param fileId 文件ID
     * @param authentication 认证信息
     * @return 删除结果
     */
    @DeleteMapping("/{fileId}")
    public ResponseEntity<Map<String, Object>> deleteFile(
            @PathVariable Long fileId,
            Authentication authentication) {
        
        try {
            fileService.deleteFile(fileId.toString(), authentication.getName());
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "文件删除成功");
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            log.error("删除文件失败: {}", e.getMessage(), e);
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", e.getMessage());
            
            return ResponseEntity.badRequest().body(result);
        }
    }

    /**
     * 搜索文件
     * 
     * @param projectId 项目ID
     * @param keyword 搜索关键词
     * @param page 页码
     * @param size 每页大小
     * @param authentication 认证信息
     * @return 搜索结果
     */
    @GetMapping("/search")
    public ResponseEntity<Map<String, Object>> searchFiles(
            @RequestParam Long projectId,
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Authentication authentication) {
        
        try {
            Pageable pageable = PageRequest.of(page, size, 
                    Sort.by(Sort.Direction.DESC, "uploadTime"));
            
            Page<UploadedFile> files = fileService.searchFiles(
                    projectId.toString(), keyword, pageable);
            
            // 转换为响应DTO
            Page<FileUploadResponse> responseFiles = files.map(FileUploadResponse::new);
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("data", responseFiles.getContent());
            result.put("pagination", Map.of(
                "current", page + 1,
                "pageSize", size,
                "total", responseFiles.getTotalElements(),
                "totalPages", responseFiles.getTotalPages()
            ));
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            log.error("搜索文件失败: {}", e.getMessage(), e);
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", e.getMessage());
            
            return ResponseEntity.badRequest().body(result);
        }
    }

    /**
     * 获取文件内容
     * 
     * @param fileId 文件ID
     * @param authentication 认证信息
     * @return 文件内容
     */
    @GetMapping("/{fileId}/content")
    public ResponseEntity<Map<String, Object>> getFileContent(
            @PathVariable Long fileId,
            Authentication authentication) {
        
        try {
            String content = fileService.getFileContent(fileId.toString(), authentication.getName());
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("data", Map.of("content", content));
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            log.error("获取文件内容失败: {}", e.getMessage(), e);
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", e.getMessage());
            
            return ResponseEntity.badRequest().body(result);
        }
    }

    /**
     * 下载文件
     * 
     * @param fileId 文件ID
     * @param authentication 认证信息
     * @return 文件资源
     */
    @GetMapping("/{fileId}/download")
    public ResponseEntity<Resource> downloadFile(
            @PathVariable Long fileId,
            Authentication authentication) {
        
        try {
            FileUploadResponse fileInfo = fileService.getFileById(fileId, authentication);
            
            // 构建文件路径（这里简化处理，实际应该从FileService获取）
            Path filePath = Paths.get("uploads", fileInfo.getFileName());
            Resource resource = new UrlResource(filePath.toUri());
            
            if (resource.exists() && resource.isReadable()) {
                return ResponseEntity.ok()
                        .contentType(MediaType.APPLICATION_OCTET_STREAM)
                        .header(HttpHeaders.CONTENT_DISPOSITION, 
                                "attachment; filename=\"" + fileInfo.getOriginalFileName() + "\"")
                        .body(resource);
            } else {
                throw new RuntimeException("文件不存在或不可读");
            }
            
        } catch (MalformedURLException e) {
            log.error("文件路径格式错误: {}", e.getMessage(), e);
            throw new RuntimeException("文件路径错误");
        } catch (Exception e) {
            log.error("文件下载失败: {}", e.getMessage(), e);
            throw new RuntimeException("文件下载失败: " + e.getMessage());
        }
    }

    /**
     * 批量上传文件
     * 
     * @param files 文件数组
     * @param projectId 项目ID
     * @param authentication 认证信息
     * @return 批量上传结果
     */
    @PostMapping("/batch-upload")
    public ResponseEntity<Map<String, Object>> batchUploadFiles(
            @RequestParam("files") MultipartFile[] files,
            @RequestParam("projectId") Long projectId,
            Authentication authentication) {
        
        log.info("收到批量文件上传请求，文件数量: {}, 项目ID: {}", files.length, projectId);
        
        Map<String, Object> result = new HashMap<>();
        Map<String, Object> uploadResults = new HashMap<>();
        int successCount = 0;
        int failCount = 0;
        
        for (int i = 0; i < files.length; i++) {
            MultipartFile file = files[i];
            String fileKey = "file_" + i;
            
            try {
                String userId = authentication.getName();
                UploadedFile uploadedFile = fileService.uploadFile(projectId.toString(), userId, file);
                FileUploadResponse response = new FileUploadResponse(uploadedFile);
                uploadResults.put(fileKey, Map.of(
                    "success", true,
                    "fileName", file.getOriginalFilename(),
                    "data", response
                ));
                successCount++;
                
            } catch (Exception e) {
                log.warn("批量上传中文件 {} 失败: {}", file.getOriginalFilename(), e.getMessage());
                uploadResults.put(fileKey, Map.of(
                    "success", false,
                    "fileName", file.getOriginalFilename(),
                    "error", e.getMessage()
                ));
                failCount++;
            }
        }
        
        result.put("success", true);
        result.put("message", String.format("批量上传完成，成功: %d, 失败: %d", successCount, failCount));
        result.put("data", Map.of(
            "results", uploadResults,
            "summary", Map.of(
                "total", files.length,
                "success", successCount,
                "failed", failCount
            )
        ));
        
        return ResponseEntity.ok(result);
    }
}