/**
 * 项目服务实现类
 * Author: AI Agent
 * Version: 1.0.0
 * Created: 2025-01-27
 */
package com.historyanalysis.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.historyanalysis.entity.Project;
import com.historyanalysis.entity.User;
import com.historyanalysis.repository.ProjectRepository;
import com.historyanalysis.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 项目服务实现类
 * 
 * 实现项目管理相关的业务逻辑：
 * - 项目创建和管理
 * - 项目查询和统计
 * - 项目设置管理
 * - 项目权限控制
 */
@Service
@Transactional
public class ProjectServiceImpl {

    private static final Logger logger = LoggerFactory.getLogger(ProjectServiceImpl.class);

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * 创建项目
     */
    @Transactional
    public Project createProject(String userId, String title, String description, Map<String, Object> settings) {
        logger.info("创建新项目, userId={}, title={}", userId, title);

        // 参数验证
        validateCreateProjectParams(userId, title, description);

        try {
            // 验证用户是否存在
            Long userIdLong = Long.parseLong(userId);
            Optional<User> userOpt = userRepository.findById(userIdLong);
            if (!userOpt.isPresent()) {
                logger.warn("创建项目失败，用户不存在: {}", userId);
                throw new IllegalArgumentException("用户不存在");
            }

            User user = userOpt.get();

            // 创建项目
            Project project = new Project();
            project.setUser(user);
            project.setName(title);
            project.setDescription(description);

            // 设置默认配置
            if (settings == null) {
                settings = getDefaultProjectSettings();
            }
            project.setSettings(convertSettingsToJson(settings));

            // 设置默认状态
            project.setStatus(Project.ProjectStatus.ACTIVE);
            project.setFileCount(0);
            project.setAnalysisCount(0);
            // 创建和更新时间由@PrePersist自动设置

            Project savedProject = projectRepository.save(project);
            logger.info("项目创建成功, projectId={}", savedProject.getId());
            return savedProject;

        } catch (NumberFormatException e) {
            logger.error("无效的用户ID格式: {}", userId);
            throw new IllegalArgumentException("无效的用户ID格式");
        } catch (Exception e) {
            logger.error("创建项目异常: {}", e.getMessage(), e);
            throw new RuntimeException("创建项目失败", e);
        }
    }

    /**
     * 根据ID查找项目
     */
    @Transactional(readOnly = true)
    public Optional<Project> findById(String projectId) {
        logger.debug("根据ID查找项目, projectId={}", projectId);

        if (!StringUtils.hasText(projectId)) {
            logger.warn("项目ID不能为空");
            return Optional.empty();
        }

        try {
            Long id = Long.parseLong(projectId);
            return projectRepository.findById(id);
        } catch (NumberFormatException e) {
            logger.warn("无效的项目ID格式: {}", projectId);
            return Optional.empty();
        } catch (Exception e) {
            logger.error("根据ID查找项目异常: {}", e.getMessage(), e);
            return Optional.empty();
        }
    }

    /**
     * 更新项目信息
     */
    public Project updateProject(String projectId, String title, String description, Map<String, Object> settings) {
        logger.info("更新项目信息, projectId={}, title={}", projectId, title);

        if (!StringUtils.hasText(projectId)) {
            throw new IllegalArgumentException("项目ID不能为空");
        }

        if (!StringUtils.hasText(title)) {
            throw new IllegalArgumentException("项目标题不能为空");
        }

        try {
            Long id = Long.parseLong(projectId);
            Optional<Project> projectOpt = projectRepository.findById(id);
            if (!projectOpt.isPresent()) {
                logger.warn("更新项目失败，项目不存在: {}", projectId);
                throw new IllegalArgumentException("项目不存在");
            }

            Project project = projectOpt.get();
            project.setName(title);
            project.setDescription(description);

            if (settings != null) {
                project.setSettings(convertSettingsToJson(settings));
            }

            // 更新时间由@PreUpdate自动设置

            Project updatedProject = projectRepository.save(project);
            logger.info("项目更新成功, projectId={}", projectId);
            return updatedProject;

        } catch (NumberFormatException e) {
            logger.error("无效的项目ID格式: {}", projectId);
            throw new IllegalArgumentException("无效的项目ID格式");
        } catch (Exception e) {
            logger.error("更新项目异常: {}", e.getMessage(), e);
            throw new RuntimeException("更新项目失败", e);
        }
    }

    /**
     * 删除项目
     */
    public boolean deleteProject(String projectId, String userId) {
        logger.info("删除项目, projectId={}, userId={}", projectId, userId);

        if (!StringUtils.hasText(projectId) || !StringUtils.hasText(userId)) {
            logger.warn("删除项目失败，参数不能为空");
            return false;
        }

        try {
            // 验证项目是否存在和权限
            if (!hasProjectAccess(projectId, userId)) {
                logger.warn("删除项目失败，无权限或项目不存在, projectId={}, userId={}", projectId, userId);
                return false;
            }

            Long id = Long.parseLong(projectId);
            projectRepository.deleteById(id);
            logger.info("项目删除成功, projectId={}", projectId);
            return true;
        } catch (Exception e) {
            logger.error("删除项目异常: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * 根据用户查询项目
     */
    @Transactional(readOnly = true)
    public Page<Project> findProjectsByUser(String userId, Pageable pageable) {
        logger.debug("根据用户查询项目, userId={}, page={}, size={}", userId, pageable.getPageNumber(), pageable.getPageSize());

        if (!StringUtils.hasText(userId)) {
            throw new IllegalArgumentException("用户ID不能为空");
        }

        try {
            Long id = Long.parseLong(userId);
            return projectRepository.findByUserId(id, pageable);
        } catch (Exception e) {
            logger.error("根据用户查询项目异常: {}", e.getMessage(), e);
            throw new RuntimeException("查询项目失败", e);
        }
    }

    /**
     * 根据用户和标题模糊查询项目
     */
    @Transactional(readOnly = true)
    public Page<Project> findProjectsByUserAndTitle(String userId, String title, Pageable pageable) {
        logger.debug("根据用户和标题查询项目, userId={}, title={}", userId, title);

        if (!StringUtils.hasText(userId)) {
            throw new IllegalArgumentException("用户ID不能为空");
        }

        try {
            Long id = Long.parseLong(userId);
            if (StringUtils.hasText(title)) {
                return projectRepository.findByUserIdAndKeywordAndStatusNot(
            id, title, title, Project.ProjectStatus.DELETED, pageable);
            } else {
                return projectRepository.findByUserId(id, pageable);
            }
        } catch (Exception e) {
            logger.error("根据用户和标题查询项目异常: {}", e.getMessage(), e);
            throw new RuntimeException("查询项目失败", e);
        }
    }

    /**
     * 验证项目访问权限
     */
    private boolean hasProjectAccess(String projectId, String userId) {
        try {
            Long id = Long.parseLong(projectId);
            Long userIdLong = Long.parseLong(userId);
            Optional<Project> projectOpt = projectRepository.findById(id);
            
            if (!projectOpt.isPresent()) {
                return false;
            }
            
            Project project = projectOpt.get();
            return project.getUser().getId().equals(userIdLong);
        } catch (Exception e) {
            logger.error("验证项目访问权限异常: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * 验证创建项目参数
     */
    private void validateCreateProjectParams(String userId, String title, String description) {
        if (!StringUtils.hasText(userId)) {
            throw new IllegalArgumentException("用户ID不能为空");
        }
        if (!StringUtils.hasText(title)) {
            throw new IllegalArgumentException("项目标题不能为空");
        }
        if (title.length() > 100) {
            throw new IllegalArgumentException("项目标题长度不能超过100个字符");
        }
        if (description != null && description.length() > 500) {
            throw new IllegalArgumentException("项目描述长度不能超过500个字符");
        }
    }

    /**
     * 获取默认项目设置
     */
    private Map<String, Object> getDefaultProjectSettings() {
        Map<String, Object> settings = new HashMap<>();
        settings.put("autoAnalysis", true);
        settings.put("maxFileSize", 10485760); // 10MB
        settings.put("allowedFileTypes", List.of("txt", "doc", "docx", "pdf"));
        settings.put("analysisLanguage", "zh");
        return settings;
    }

    /**
     * 将设置对象转换为JSON字符串
     */
    private String convertSettingsToJson(Map<String, Object> settings) {
        try {
            return objectMapper.writeValueAsString(settings);
        } catch (JsonProcessingException e) {
            logger.error("转换设置为JSON异常: {}", e.getMessage(), e);
            return "{}";
        }
    }
}