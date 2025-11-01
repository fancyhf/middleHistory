/**
 * 项目管理服务
 * @author AI Agent
 * @version 1.0.0
 * @created 2024-12-29 18:08:00
 */
package com.historyanalysis.service;

import com.historyanalysis.dto.ProjectRequest;
import com.historyanalysis.dto.ProjectResponse;
import com.historyanalysis.entity.AnalysisResult;
import com.historyanalysis.entity.Project;
import com.historyanalysis.entity.User;
import com.historyanalysis.repository.AnalysisResultRepository;
import com.historyanalysis.repository.FileInfoRepository;
import com.historyanalysis.repository.ProjectRepository;
import com.historyanalysis.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 项目管理服务类
 * 处理项目的创建、查询、更新、删除等业务逻辑
 */
@Service
@RequiredArgsConstructor
public class ProjectService {

    private static final Logger log = LoggerFactory.getLogger(ProjectService.class);

    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final FileInfoRepository fileInfoRepository;
    private final AnalysisResultRepository analysisResultRepository;

    /**
     * 创建项目
     * 
     * @param request 项目请求
     * @param authentication 认证信息
     * @return 项目响应
     */
    @Transactional
    public ProjectResponse createProject(ProjectRequest request, Authentication authentication) {
        log.info("创建项目: {}, 用户: {}", request.getName(), authentication.getName());
        
        // 获取当前用户
        User user = getUserFromAuthentication(authentication);
        
        // 检查项目名称是否重复
        if (projectRepository.existsByNameAndUserId(request.getName(), user.getId())) {
            throw new IllegalArgumentException("项目名称已存在");
        }
        
        // 创建项目实体
        Project project = new Project();
        project.setName(request.getName());
        project.setDescription(request.getDescription());
        project.setTags(request.getTags());
        project.setSettings(request.getSettings());
        project.setStatus(Project.ProjectStatus.ACTIVE);
        project.setUser(user);
        project.setFileCount(0);
        project.setAnalysisCount(0);
        project.setCreateTime(LocalDateTime.now());
        project.setUpdateTime(LocalDateTime.now());
        
        // 保存项目
        project = projectRepository.save(project);
        
        log.info("项目创建成功: ID={}, 名称={}", project.getId(), project.getName());
        
        return convertToResponse(project);
    }

    /**
     * 获取用户项目列表
     * 
     * @param pageable 分页参数
     * @param authentication 认证信息
     * @return 项目列表
     */
    public Page<ProjectResponse> getUserProjects(Pageable pageable, Authentication authentication) {
        User user = getUserFromAuthentication(authentication);
        
        Page<Project> projects = projectRepository.findByUserIdAndStatusNot(
                user.getId(), Project.ProjectStatus.DELETED, pageable);
        
        return projects.map(this::convertToResponse);
    }

    /**
     * 根据ID获取项目
     * 
     * @param projectId 项目ID
     * @param authentication 认证信息
     * @return 项目响应
     */
    public ProjectResponse getProjectById(Long projectId, Authentication authentication) {
        Project project = getProjectWithPermissionCheck(projectId, authentication);
        return convertToResponseWithStats(project);
    }

    /**
     * 更新项目
     * 
     * @param projectId 项目ID
     * @param request 更新请求
     * @param authentication 认证信息
     * @return 更新后的项目
     */
    @Transactional
    public ProjectResponse updateProject(Long projectId, ProjectRequest request, Authentication authentication) {
        log.info("更新项目: ID={}, 用户: {}", projectId, authentication.getName());
        
        Project project = getProjectWithPermissionCheck(projectId, authentication);
        
        // 检查项目名称是否重复（排除当前项目）
        if (!project.getName().equals(request.getName()) && 
            projectRepository.existsByNameAndUserIdAndIdNot(request.getName(), project.getUser().getId(), projectId)) {
            throw new IllegalArgumentException("项目名称已存在");
        }
        
        // 更新项目信息
        project.setName(request.getName());
        project.setDescription(request.getDescription());
        project.setTags(request.getTags());
        project.setSettings(request.getSettings());
        project.setUpdateTime(LocalDateTime.now());
        
        project = projectRepository.save(project);
        
        log.info("项目更新成功: ID={}, 名称={}", project.getId(), project.getName());
        
        return convertToResponse(project);
    }

    /**
     * 删除项目（软删除）
     * 
     * @param projectId 项目ID
     * @param authentication 认证信息
     */
    @Transactional
    public void deleteProject(Long projectId, Authentication authentication) {
        log.info("删除项目: ID={}, 用户: {}", projectId, authentication.getName());
        
        Project project = getProjectWithPermissionCheck(projectId, authentication);
        
        // 软删除项目
        project.setStatus(Project.ProjectStatus.DELETED);
        project.setUpdateTime(LocalDateTime.now());
        
        projectRepository.save(project);
        
        log.info("项目删除成功: ID={}, 名称={}", project.getId(), project.getName());
    }

    /**
     * 搜索项目
     * 
     * @param keyword 搜索关键词
     * @param pageable 分页参数
     * @param authentication 认证信息
     * @return 搜索结果
     */
    public Page<ProjectResponse> searchProjects(String keyword, Pageable pageable, Authentication authentication) {
        User user = getUserFromAuthentication(authentication);
        
        Page<Project> projects = projectRepository.findByUserIdAndKeywordAndStatusNot(
                user.getId(), keyword, keyword, Project.ProjectStatus.DELETED, pageable);
        
        return projects.map(this::convertToResponse);
    }

    /**
     * 获取项目统计信息
     * 
     * @param authentication 认证信息
     * @return 统计信息
     */
    public ProjectResponse.ProjectStats getUserProjectStats(Authentication authentication) {
        User user = getUserFromAuthentication(authentication);
        
        ProjectResponse.ProjectStats stats = new ProjectResponse.ProjectStats();
        
        // 统计项目数量
        long totalProjects = projectRepository.countByUserIdAndStatusNot(user.getId(), Project.ProjectStatus.DELETED);
        
        // 统计文件数量和大小
        long totalFiles = fileInfoRepository.countByUserId(user.getId());
        Long totalFileSize = fileInfoRepository.sumFileSizeByUserId(user.getId());
        
        // 统计分析数量
        long totalAnalysis = analysisResultRepository.countByUserId(user.getId());
        long completedAnalysis = analysisResultRepository.countByUserIdAndStatus(
                user.getId(), AnalysisResult.AnalysisStatus.COMPLETED);
        
        stats.setTotalFileSize(totalFileSize != null ? totalFileSize : 0L);
        stats.setTotalFileSizeFormatted(formatFileSize(stats.getTotalFileSize()));
        stats.setCompletedAnalysisCount((int) completedAnalysis);
        
        return stats;
    }

    /**
     * 归档项目
     * 
     * @param projectId 项目ID
     * @param authentication 认证信息
     */
    @Transactional
    public void archiveProject(Long projectId, Authentication authentication) {
        log.info("归档项目: ID={}, 用户: {}", projectId, authentication.getName());
        
        Project project = getProjectWithPermissionCheck(projectId, authentication);
        
        project.setStatus(Project.ProjectStatus.ARCHIVED);
        project.setUpdateTime(LocalDateTime.now());
        
        projectRepository.save(project);
        
        log.info("项目归档成功: ID={}, 名称={}", project.getId(), project.getName());
    }

    /**
     * 恢复项目
     * 
     * @param projectId 项目ID
     * @param authentication 认证信息
     */
    @Transactional
    public void restoreProject(Long projectId, Authentication authentication) {
        log.info("恢复项目: ID={}, 用户: {}", projectId, authentication.getName());
        
        Project project = getProjectWithPermissionCheck(projectId, authentication);
        
        project.setStatus(Project.ProjectStatus.ACTIVE);
        project.setUpdateTime(LocalDateTime.now());
        
        projectRepository.save(project);
        
        log.info("项目恢复成功: ID={}, 名称={}", project.getId(), project.getName());
    }

    /**
     * 获取最近项目
     * 
     * @param limit 限制数量
     * @param authentication 认证信息
     * @return 最近项目列表
     */
    public List<ProjectResponse> getRecentProjects(int limit, Authentication authentication) {
        User user = getUserFromAuthentication(authentication);
        
        Pageable pageable = Pageable.ofSize(limit);
        List<Project> projects = projectRepository.findRecentProjects(user.getId(), pageable);
        
        return projects.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    /**
     * 从认证信息获取用户
     * 
     * @param authentication 认证信息
     * @return 用户实体
     */
    private User getUserFromAuthentication(Authentication authentication) {
        String username = authentication.getName();
        return userRepository.findByUsernameOrEmail(username, username)
                .orElseThrow(() -> new IllegalArgumentException("用户不存在"));
    }

    /**
     * 获取项目并检查权限
     * 
     * @param projectId 项目ID
     * @param authentication 认证信息
     * @return 项目实体
     */
    private Project getProjectWithPermissionCheck(Long projectId, Authentication authentication) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("项目不存在"));
        
        User user = getUserFromAuthentication(authentication);
        
        // 检查项目所有权
        if (!project.getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException("无权访问该项目");
        }
        
        // 检查项目状态
        if (project.getStatus() == Project.ProjectStatus.DELETED) {
            throw new IllegalArgumentException("项目已删除");
        }
        
        return project;
    }

    /**
     * 转换为响应DTO
     * 
     * @param project 项目实体
     * @return 项目响应DTO
     */
    private ProjectResponse convertToResponse(Project project) {
        ProjectResponse response = new ProjectResponse();
        response.setId(project.getId());
        response.setName(project.getName());
        response.setDescription(project.getDescription());
        response.setTags(project.getTags());
        response.setStatus(project.getStatus());
        response.setSettings(project.getSettings());
        response.setFileCount(project.getFileCount());
        response.setAnalysisCount(project.getAnalysisCount());
        response.setCreateTime(project.getCreateTime());
        response.setUpdateTime(project.getUpdateTime());
        
        // 设置用户信息
        ProjectResponse.UserInfo userInfo = new ProjectResponse.UserInfo();
        userInfo.setId(project.getUser().getId());
        userInfo.setUsername(project.getUser().getUsername());
        userInfo.setRealName(project.getUser().getRealName());
        userInfo.setAvatarUrl(project.getUser().getAvatarUrl());
        response.setUser(userInfo);
        
        return response;
    }

    /**
     * 转换为带统计信息的响应DTO
     * 
     * @param project 项目实体
     * @return 项目响应DTO
     */
    private ProjectResponse convertToResponseWithStats(Project project) {
        ProjectResponse response = convertToResponse(project);
        
        // 获取统计信息
        ProjectResponse.ProjectStats stats = new ProjectResponse.ProjectStats();
        
        // 文件统计
        Long totalFileSize = fileInfoRepository.sumFileSizeByProjectId(project.getId());
        stats.setTotalFileSize(totalFileSize != null ? totalFileSize : 0L);
        stats.setTotalFileSizeFormatted(formatFileSize(stats.getTotalFileSize()));
        
        // 分析统计
        int completedAnalysis = Math.toIntExact(analysisResultRepository.countByProjectIdAndStatus(
                project.getId(), AnalysisResult.AnalysisStatus.COMPLETED));
        int runningAnalysis = Math.toIntExact(analysisResultRepository.countByProjectIdAndStatus(
                project.getId(), AnalysisResult.AnalysisStatus.PROCESSING));
        int failedAnalysis = Math.toIntExact(analysisResultRepository.countByProjectIdAndStatus(
                project.getId(), AnalysisResult.AnalysisStatus.FAILED));
        
        stats.setCompletedAnalysisCount(completedAnalysis);
        stats.setRunningAnalysisCount(runningAnalysis);
        stats.setFailedAnalysisCount(failedAnalysis);
        
        // 最近时间
        stats.setLastUploadTime(fileInfoRepository.findLastUploadTimeByProjectId(project.getId()));
        stats.setLastAnalysisTime(analysisResultRepository.findLastAnalysisTimeByProjectId(project.getId()));
        
        response.setStats(stats);
        
        return response;
    }

    /**
     * 格式化文件大小
     * 
     * @param size 文件大小（字节）
     * @return 格式化后的大小
     */
    private String formatFileSize(Long size) {
        if (size == null || size == 0) {
            return "0 B";
        }
        
        String[] units = {"B", "KB", "MB", "GB", "TB"};
        int unitIndex = 0;
        double fileSize = size.doubleValue();
        
        while (fileSize >= 1024 && unitIndex < units.length - 1) {
            fileSize /= 1024;
            unitIndex++;
        }
        
        return String.format("%.2f %s", fileSize, units[unitIndex]);
    }
}