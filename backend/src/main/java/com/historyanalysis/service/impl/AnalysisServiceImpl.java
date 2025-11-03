/**
 * 分析服务实现类
 * @author AI Agent
 * @version 1.0.0
 * @created 2024-12-29 16:45:00
 * @description 历史数据分析服务的具体实现
 */
package com.historyanalysis.service.impl;

import com.historyanalysis.entity.*;
import com.historyanalysis.repository.*;
import com.historyanalysis.service.AnalysisService;
import com.historyanalysis.service.FileService;
import com.historyanalysis.service.NlpServiceClient;
import com.historyanalysis.exception.NlpServiceException;
import com.historyanalysis.dto.nlp.NlpRequest;
import com.historyanalysis.dto.nlp.NlpResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * 分析服务实现类
 * 
 * 实现历史数据分析相关的业务逻辑：
 * - 分析任务管理
 * - 各种类型的分析处理
 * - 分析结果查询和统计
 * - 与NLP服务的集成
 */
@Service
@Transactional
public class AnalysisServiceImpl implements AnalysisService {

    private static final Logger logger = LoggerFactory.getLogger(AnalysisServiceImpl.class);

    @Autowired
    private AnalysisResultRepository analysisResultRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FileService fileService;

    @Autowired
    private NlpServiceClient nlpServiceClient;

    @Autowired
    private WordFrequencyRepository wordFrequencyRepository;

    @Autowired
    private TimelineEventRepository timelineEventRepository;

    @Autowired
    private GeoLocationRepository geoLocationRepository;

    @Value("${app.analysis.timeout:300}")
    private int analysisTimeout;

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 创建分析任务
     */
    @Override
    public AnalysisResult createAnalysisTask(String projectId, String userId, AnalysisResult.AnalysisType analysisType, List<String> fileIds) {
        logger.info("创建分析任务, projectId={}, userId={}, analysisType={}, fileIds={}", projectId, userId, analysisType, fileIds);

        // 转换参数类型
        Long projectIdLong = Long.parseLong(projectId);
        Long userIdLong = Long.parseLong(userId);
        List<Long> fileIdsLong = fileIds.stream().map(Long::parseLong).collect(Collectors.toList());
        
        // 参数验证
        validateAnalysisParams(projectIdLong, userIdLong, analysisType, fileIdsLong);

        try {
            // 验证项目权限
            if (!hasProjectAccess(projectIdLong, userIdLong)) {
                logger.warn("创建分析任务失败，无项目权限, projectId={}, userId={}", projectId, userId);
                throw new IllegalArgumentException("无权限访问该项目");
            }

            // 获取项目信息
            Optional<Project> projectOpt = projectRepository.findById(projectIdLong);
            if (!projectOpt.isPresent()) {
                throw new IllegalArgumentException("项目不存在");
            }

            Project project = projectOpt.get();

            // 获取用户信息
            Optional<User> userOpt = userRepository.findById(userIdLong);
            if (!userOpt.isPresent()) {
                throw new IllegalArgumentException("用户不存在");
            }

            User user = userOpt.get();

            // 验证文件权限
            for (Long fileIdLong : fileIdsLong) {
                if (!fileService.hasFileAccess(fileIdLong.toString(), userIdLong.toString())) {
                    throw new IllegalArgumentException("无权限访问文件: " + fileIdLong);
                }
            }

            // 创建分析结果记录
            AnalysisResult analysisResult = new AnalysisResult();
            analysisResult.setProject(project);
            analysisResult.setUser(user);
            analysisResult.setAnalysisType(analysisType);
            analysisResult.setStatus(AnalysisResult.AnalysisStatus.PENDING);

            // 保存文件ID列表为JSON字符串
            try {
                analysisResult.setFileIds(objectMapper.writeValueAsString(fileIds));
            } catch (Exception e) {
                logger.error("序列化文件ID列表失败: {}", e.getMessage());
                throw new RuntimeException("创建分析任务失败", e);
            }

            AnalysisResult savedResult = analysisResultRepository.save(analysisResult);
            logger.info("分析任务创建成功, analysisId={}", savedResult.getId());

            // 异步执行分析
            executeAnalysisAsync(savedResult.getId().toString(), userId, analysisType);

            return savedResult;
        } catch (Exception e) {
            logger.error("创建分析任务失败: {}", e.getMessage(), e);
            throw new RuntimeException("创建分析任务失败", e);
        }
    }

    /**
     * 检查用户是否有分析访问权限（内部使用的Long版本）
     */
    @Transactional(readOnly = true)
    public boolean hasAnalysisAccess(Long analysisId, Long userId) {
        logger.debug("检查分析访问权限, analysisId={}, userId={}", analysisId, userId);

        if (analysisId == null || userId == null) {
            return false;
        }

        try {
            Optional<AnalysisResult> analysisOpt = analysisResultRepository.findById(analysisId);
            if (analysisOpt.isPresent()) {
                AnalysisResult analysis = analysisOpt.get();
                Long projectId = analysis.getProject().getId();
                return hasProjectAccess(projectId, userId);
            }
            return false;
        } catch (Exception e) {
            logger.error("检查分析访问权限异常: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * 创建分析任务（简化版本）
     */
    @Override
    public AnalysisResult createAnalysis(String projectId, AnalysisResult.AnalysisType analysisType, String description) {
        logger.info("创建分析任务, projectId={}, analysisType={}, description={}", projectId, analysisType, description);

        try {
            // 转换参数类型
            Long projectIdLong = Long.parseLong(projectId);
            
            // 获取项目信息
            Optional<Project> projectOpt = projectRepository.findById(projectIdLong);
            if (!projectOpt.isPresent()) {
                throw new IllegalArgumentException("项目不存在");
            }

            Project project = projectOpt.get();

            // 创建分析结果记录
            AnalysisResult analysisResult = new AnalysisResult();
            analysisResult.setProject(project);
            analysisResult.setProjectId(project.getId());
            analysisResult.setUserId(project.getUserId()); // 使用项目的用户ID
            analysisResult.setAnalysisType(analysisType);
            analysisResult.setStatus(AnalysisResult.AnalysisStatus.PENDING);
            
            // 设置描述信息
            if (description != null && !description.trim().isEmpty()) {
                Map<String, Object> parameters = new HashMap<>();
                parameters.put("description", description);
                try {
                    analysisResult.setParameters(objectMapper.writeValueAsString(parameters));
                } catch (Exception e) {
                    logger.error("序列化参数失败: {}", e.getMessage());
                }
            }

            AnalysisResult savedResult = analysisResultRepository.save(analysisResult);
            logger.info("分析任务创建成功, analysisId={}", savedResult.getId());

            // 立即触发异步执行分析任务
            executeAnalysisAsync(savedResult.getId().toString(), savedResult.getUserId().toString(), analysisType);
            logger.info("分析任务已提交异步执行, analysisId={}, type={}", savedResult.getId(), analysisType);

            return savedResult;
        } catch (NumberFormatException e) {
            logger.error("无效的项目ID格式: {}", projectId);
            throw new IllegalArgumentException("无效的项目ID格式");
        } catch (Exception e) {
            logger.error("创建分析任务失败: {}", e.getMessage(), e);
            throw new RuntimeException("创建分析任务失败", e);
        }
    }

    /**
     * 执行词频分析
     */
    @Override
    public boolean executeWordFrequencyAnalysis(String analysisId, String userId) {
        logger.info("执行词频分析, analysisId={}, userId={}", analysisId, userId);

        try {
            // 转换参数类型
            Long analysisIdLong = Long.parseLong(analysisId);
            Long userIdLong = Long.parseLong(userId);
            
            // 验证分析权限
            if (!hasAnalysisAccess(analysisIdLong, userIdLong)) {
                logger.warn("执行词频分析失败，无权限, analysisId={}, userId={}", analysisId, userId);
                return false;
            }

            Optional<AnalysisResult> analysisOpt = analysisResultRepository.findById(analysisIdLong);
            if (!analysisOpt.isPresent()) {
                logger.warn("执行词频分析失败，分析不存在, analysisId={}", analysisId);
                return false;
            }

            AnalysisResult analysis = analysisOpt.get();
            analysis.setStatus(AnalysisResult.AnalysisStatus.PROCESSING);
            analysis.setStartedAt(LocalDateTime.now());
            analysisResultRepository.save(analysis);

            // 获取文件内容
            List<Long> fileIds = getFileIdsFromAnalysis(analysis);
            StringBuilder combinedText = new StringBuilder();

            if (fileIds.isEmpty()) {
                // 如果没有文件ID，使用示例文本进行分析
                logger.info("没有指定文件，使用示例文本进行词频分析, analysisId={}", analysisId);
                combinedText.append("中国历史悠久，文化灿烂。从古代的夏商周三代，到秦汉统一，再到唐宋元明清各朝代，每个时期都有其独特的历史特色。" +
                    "古代中国在政治、经济、文化、科技等方面都取得了辉煌的成就。政治上，建立了完善的官僚制度；经济上，农业和手工业发达；" +
                    "文化上，儒家思想影响深远；科技上，四大发明改变了世界。这些历史文化遗产至今仍然影响着现代中国的发展。");
            } else {
                // 有文件ID时，读取文件内容
                for (Long fileId : fileIds) {
                    String fileContent = fileService.getFileContent(fileId.toString(), userId);
                    if (StringUtils.hasText(fileContent)) {
                        combinedText.append(fileContent).append("\n");
                    }
                }
            }

            if (combinedText.length() == 0) {
                throw new RuntimeException("没有可分析的文本内容");
            }

            // 调用NLP服务进行词频分析
            try {
                Map<String, Object> nlpResponse = nlpServiceClient.analyzeWordFrequency(combinedText.toString(), 50, 2);

                if (nlpResponse == null) {
                    throw new RuntimeException("词频分析失败: NLP服务返回空结果");
                }

                // 注意：nlpServiceClient.analyzeWordFrequency 返回的是 data 部分，不包含 success 字段
                // 如果能成功返回数据，说明调用成功
                logger.debug("NLP服务返回数据: {}", nlpResponse);

                // 处理分析结果
                processWordFrequencyResult(analysis, nlpResponse);

                // 更新分析状态
                analysis.completeAnalysis();
                analysisResultRepository.save(analysis);

                logger.info("词频分析执行成功, analysisId={}", analysisId);
                return true;
            } catch (NlpServiceException e) {
                logger.error("NLP服务调用失败: {}", e.getMessage(), e);
                throw new RuntimeException("词频分析失败", e);
            }
        } catch (Exception e) {
            logger.error("执行词频分析失败: {}", e.getMessage(), e);
            updateAnalysisError(analysisId, e.getMessage());
            return false;
        }
    }

    /**
     * 执行时间轴分析
     */
    @Override
    public boolean executeTimelineAnalysis(String analysisId, String userId) {
        logger.info("执行时间轴分析, analysisId={}, userId={}", analysisId, userId);

        try {
            // 转换参数类型
            Long analysisIdLong = Long.parseLong(analysisId);
            Long userIdLong = Long.parseLong(userId);
            
            // 验证分析权限
            if (!hasAnalysisAccess(analysisIdLong, userIdLong)) {
                logger.warn("执行时间轴分析失败，无权限, analysisId={}, userId={}", analysisId, userId);
                return false;
            }

            Optional<AnalysisResult> analysisOpt = analysisResultRepository.findById(analysisIdLong);
            if (!analysisOpt.isPresent()) {
                logger.warn("执行时间轴分析失败，分析不存在, analysisId={}", analysisId);
                return false;
            }

            AnalysisResult analysis = analysisOpt.get();
            analysis.setStatus(AnalysisResult.AnalysisStatus.PROCESSING);
            analysis.setStartedAt(LocalDateTime.now());
            analysisResultRepository.save(analysis);

            // 获取文件内容
            List<Long> fileIds = getFileIdsFromAnalysis(analysis);
            StringBuilder combinedText = new StringBuilder();

            for (Long fileId : fileIds) {
                String fileContent = fileService.getFileContent(fileId.toString(), userId);
                if (StringUtils.hasText(fileContent)) {
                    combinedText.append(fileContent).append("\n");
                }
            }

            if (combinedText.length() == 0) {
                throw new RuntimeException("没有可分析的文本内容");
            }

            // 调用NLP服务进行时间轴分析
            try {
                Map<String, Object> nlpResponse = nlpServiceClient.analyzeTimeline(combinedText.toString());

                if (nlpResponse == null) {
                    throw new RuntimeException("时间轴分析失败: NLP服务返回空结果");
                }
                
                logger.debug("时间轴分析NLP服务返回数据: {}", nlpResponse);

                // 处理分析结果
                processTimelineResult(analysis, nlpResponse);

                // 更新分析状态
                analysis.completeAnalysis();
                analysisResultRepository.save(analysis);

                logger.info("时间轴分析执行成功, analysisId={}", analysisId);
                return true;
            } catch (NlpServiceException e) {
                logger.error("NLP服务调用失败: {}", e.getMessage(), e);
                throw new RuntimeException("时间轴分析失败", e);
            }
        } catch (Exception e) {
            logger.error("执行时间轴分析失败: {}", e.getMessage(), e);
            updateAnalysisError(analysisId, e.getMessage());
            return false;
        }
    }

    /**
     * 执行地理分析
     */
    @Override
    public boolean executeGeographyAnalysis(String analysisId, String userId) {
        logger.info("执行地理分析, analysisId={}, userId={}", analysisId, userId);

        try {
            // 转换参数类型
            Long analysisIdLong = Long.parseLong(analysisId);
            Long userIdLong = Long.parseLong(userId);
            
            // 验证分析权限
            if (!hasAnalysisAccess(analysisIdLong, userIdLong)) {
                logger.warn("执行地理分析失败，无权限, analysisId={}, userId={}", analysisId, userId);
                return false;
            }

            Optional<AnalysisResult> analysisOpt = analysisResultRepository.findById(analysisIdLong);
            if (!analysisOpt.isPresent()) {
                logger.warn("执行地理分析失败，分析不存在, analysisId={}", analysisId);
                return false;
            }

            AnalysisResult analysis = analysisOpt.get();
            analysis.setStatus(AnalysisResult.AnalysisStatus.PROCESSING);
            analysis.setStartedAt(LocalDateTime.now());
            analysisResultRepository.save(analysis);

            // 获取文件内容
            List<Long> fileIds = getFileIdsFromAnalysis(analysis);
            StringBuilder combinedText = new StringBuilder();

            for (Long fileId : fileIds) {
                String fileContent = fileService.getFileContent(fileId.toString(), userId);
                if (StringUtils.hasText(fileContent)) {
                    combinedText.append(fileContent).append("\n");
                }
            }

            if (combinedText.length() == 0) {
                throw new RuntimeException("没有可分析的文本内容");
            }

            // 调用NLP服务进行地理分析
            try {
                Map<String, Object> nlpResponse = nlpServiceClient.analyzeGeographic(combinedText.toString());

                if (nlpResponse == null) {
                    throw new RuntimeException("地理分析失败: NLP服务返回空结果");
                }
                
                logger.debug("地理分析NLP服务返回数据: {}", nlpResponse);

                // 处理分析结果
                processGeographyResult(analysis, nlpResponse);

                // 更新分析状态
                analysis.completeAnalysis();
                analysisResultRepository.save(analysis);

                logger.info("地理分析执行成功, analysisId={}", analysisId);
                return true;
            } catch (NlpServiceException e) {
                logger.error("NLP服务调用失败: {}", e.getMessage(), e);
                throw new RuntimeException("地理分析失败", e);
            }
        } catch (Exception e) {
            logger.error("执行地理分析失败: {}", e.getMessage(), e);
            updateAnalysisError(analysisId, e.getMessage());
            return false;
        }
    }

    /**
     * 执行多维度分析
     */
    @Override
    public boolean executeMultidimensionalAnalysis(String analysisId, String userId) {
        logger.info("执行多维度分析, analysisId={}, userId={}", analysisId, userId);

        try {
            // 转换参数类型
            Long analysisIdLong = Long.parseLong(analysisId);
            Long userIdLong = Long.parseLong(userId);
            
            // 验证分析权限
            if (!hasAnalysisAccess(analysisIdLong, userIdLong)) {
                logger.warn("执行多维度分析失败，无权限, analysisId={}, userId={}", analysisId, userId);
                return false;
            }

            Optional<AnalysisResult> analysisOpt = analysisResultRepository.findById(analysisIdLong);
            if (!analysisOpt.isPresent()) {
                logger.warn("执行多维度分析失败，分析不存在, analysisId={}", analysisId);
                return false;
            }

            AnalysisResult analysis = analysisOpt.get();
            analysis.setStatus(AnalysisResult.AnalysisStatus.PROCESSING);
            analysis.setStartedAt(LocalDateTime.now());
            analysisResultRepository.save(analysis);

            // 获取文件内容
            List<Long> fileIds = getFileIdsFromAnalysis(analysis);
            StringBuilder combinedText = new StringBuilder();

            for (Long fileId : fileIds) {
                String fileContent = fileService.getFileContent(fileId.toString(), userId);
                if (fileContent != null && !fileContent.trim().isEmpty()) {
                    combinedText.append(fileContent).append("\n");
                }
            }

            if (combinedText.length() == 0) {
                logger.warn("执行多维度分析失败，没有有效的文件内容, analysisId={}", analysisId);
                updateAnalysisError(analysisId, "没有有效的文件内容");
                return false;
            }

            // 调用NLP服务进行多维度分析
            try {
                Map<String, Object> nlpResult = nlpServiceClient.analyzeMultidimensional(combinedText.toString());
                
                // 保存分析结果
                analysis.setResultData(objectMapper.writeValueAsString(nlpResult));
                analysis.setStatus(AnalysisResult.AnalysisStatus.COMPLETED);
                analysis.setCompletedAt(LocalDateTime.now());
                
                // 计算处理时间
                if (analysis.getStartedAt() != null) {
                    long processingTime = java.time.Duration.between(analysis.getStartedAt(), analysis.getCompletedAt()).toMillis();
                    analysis.setProcessingTime(processingTime);
                }
                
                analysisResultRepository.save(analysis);
                
                logger.info("多维度分析完成, analysisId={}", analysisId);
                return true;
            } catch (Exception e) {
                logger.error("多维度分析NLP服务调用失败: {}", e.getMessage(), e);
                updateAnalysisError(analysisId, "NLP服务调用失败: " + e.getMessage());
                throw new RuntimeException("多维度分析失败", e);
            }
        } catch (Exception e) {
            logger.error("执行多维度分析失败: {}", e.getMessage(), e);
            updateAnalysisError(analysisId, e.getMessage());
            return false;
        }
    }

    /**
     * 执行综合分析
     */
    @Override
    public boolean executeComprehensiveAnalysis(String analysisId, String userId) {
        logger.info("执行综合分析, analysisId={}, userId={}", analysisId, userId);

        try {
            // 转换参数类型
            Long analysisIdLong = Long.parseLong(analysisId);
            Long userIdLong = Long.parseLong(userId);
            
            // 验证分析权限
            if (!hasAnalysisAccess(analysisIdLong, userIdLong)) {
                logger.warn("执行综合分析失败，无权限, analysisId={}, userId={}", analysisId, userId);
                return false;
            }

            Optional<AnalysisResult> analysisOpt = analysisResultRepository.findById(analysisIdLong);
            if (!analysisOpt.isPresent()) {
                logger.warn("执行综合分析失败，分析不存在, analysisId={}", analysisId);
                return false;
            }

            AnalysisResult analysis = analysisOpt.get();
            analysis.setStatus(AnalysisResult.AnalysisStatus.PROCESSING);
            analysis.setStartedAt(LocalDateTime.now());
            analysisResultRepository.save(analysis);

            // 获取文件内容
            List<Long> fileIds = getFileIdsFromAnalysis(analysis);
            StringBuilder combinedText = new StringBuilder();

            for (Long fileId : fileIds) {
                String fileContent = fileService.getFileContent(fileId.toString(), userId);
                if (StringUtils.hasText(fileContent)) {
                    combinedText.append(fileContent).append("\n");
                }
            }

            if (combinedText.length() == 0) {
                throw new RuntimeException("没有可分析的文本内容");
            }

            // 调用NLP服务进行综合分析
            try {
                Map<String, Object> nlpResponse = nlpServiceClient.analyzeComprehensive(combinedText.toString());

                if (nlpResponse == null) {
                    throw new RuntimeException("综合分析失败: NLP服务返回空结果");
                }
                
                logger.debug("综合分析NLP服务返回数据: {}", nlpResponse);

                // 处理分析结果
                processComprehensiveResult(analysis, nlpResponse);

                // 更新分析状态
                analysis.completeAnalysis();
                analysisResultRepository.save(analysis);

                logger.info("综合分析执行成功, analysisId={}", analysisId);
                return true;
            } catch (NlpServiceException e) {
                logger.error("NLP服务调用失败: {}", e.getMessage(), e);
                throw new RuntimeException("综合分析失败", e);
            }
        } catch (NumberFormatException e) {
            logger.error("无效的ID格式, analysisId={}, userId={}: {}", analysisId, userId, e.getMessage());
            return false;
        } catch (Exception e) {
            logger.error("执行综合分析失败: {}", e.getMessage(), e);
            updateAnalysisError(analysisId, e.getMessage());
            return false;
        }
    }

    /**
     * 执行文本摘要分析
     */
    @Override
    public boolean executeTextSummaryAnalysis(String analysisId, String userId) {
        logger.info("执行文本摘要分析, analysisId={}, userId={}", analysisId, userId);

        try {
            // 转换参数类型
            Long analysisIdLong = Long.parseLong(analysisId);
            Long userIdLong = Long.parseLong(userId);
            
            // 验证分析权限
            if (!hasAnalysisAccess(analysisIdLong, userIdLong)) {
                logger.warn("执行文本摘要分析失败，无权限, analysisId={}, userId={}", analysisId, userId);
                return false;
            }

            Optional<AnalysisResult> analysisOpt = analysisResultRepository.findById(analysisIdLong);
            if (!analysisOpt.isPresent()) {
                logger.warn("执行文本摘要分析失败，分析不存在, analysisId={}", analysisId);
                return false;
            }

            AnalysisResult analysis = analysisOpt.get();
            analysis.setStatus(AnalysisResult.AnalysisStatus.PROCESSING);
            analysis.setStartedAt(LocalDateTime.now());
            analysisResultRepository.save(analysis);

            // 获取文件内容
            List<Long> fileIds = getFileIdsFromAnalysis(analysis);
            StringBuilder combinedText = new StringBuilder();

            for (Long fileId : fileIds) {
                String fileContent = fileService.getFileContent(fileId.toString(), userId);
                if (StringUtils.hasText(fileContent)) {
                    combinedText.append(fileContent).append("\n");
                }
            }

            if (combinedText.length() == 0) {
                throw new RuntimeException("没有可分析的文本内容");
            }

            // 调用NLP服务进行文本摘要分析
            try {
                Map<String, Object> nlpResponse = nlpServiceClient.analyzeSummary(combinedText.toString(), "comprehensive", 5);

                if (nlpResponse == null) {
                    throw new RuntimeException("文本摘要分析失败: NLP服务返回空结果");
                }
                
                logger.debug("文本摘要分析NLP服务返回数据: {}", nlpResponse);

                // 处理分析结果
                processTextSummaryResult(analysis, nlpResponse);

                // 更新分析状态
                analysis.completeAnalysis();
                analysisResultRepository.save(analysis);

                logger.info("文本摘要分析执行成功, analysisId={}", analysisId);
                return true;
            } catch (NlpServiceException e) {
                logger.error("NLP服务调用失败: {}", e.getMessage(), e);
                throw new RuntimeException("文本摘要分析失败", e);
            }
        } catch (NumberFormatException e) {
            logger.error("无效的ID格式, analysisId={}, userId={}: {}", analysisId, userId, e.getMessage());
            return false;
        } catch (Exception e) {
            logger.error("执行文本摘要分析失败: {}", e.getMessage(), e);
            updateAnalysisError(analysisId, e.getMessage());
            return false;
        }
    }

    /**
     * 根据ID查找分析结果
     */
    @Override
    @Transactional(readOnly = true)
    public Optional<AnalysisResult> findById(String analysisId) {
        logger.debug("根据ID查找分析结果, analysisId={}", analysisId);

        if (analysisId == null) {
            return Optional.empty();
        }

        try {
            Long analysisIdLong = Long.parseLong(analysisId);
            return analysisResultRepository.findById(analysisIdLong);
        } catch (NumberFormatException e) {
            logger.error("无效的分析ID格式: {}", analysisId);
            return Optional.empty();
        } catch (Exception e) {
            logger.error("根据ID查找分析结果异常: {}", e.getMessage(), e);
            return Optional.empty();
        }
    }

    /**
     * 删除分析结果
     */
    @Override
    public boolean deleteAnalysis(String analysisId, String userId) {
        logger.info("删除分析结果, analysisId={}, userId={}", analysisId, userId);

        if (analysisId == null || userId == null) {
            logger.warn("删除分析结果失败，参数不能为空");
            return false;
        }

        try {
            // 转换参数类型
            Long analysisIdLong = Long.parseLong(analysisId);
            Long userIdLong = Long.parseLong(userId);
            
            // 验证分析权限
            if (!hasAnalysisAccess(analysisIdLong, userIdLong)) {
                logger.warn("删除分析结果失败，无权限, analysisId={}, userId={}", analysisId, userId);
                return false;
            }

            Optional<AnalysisResult> analysisOpt = analysisResultRepository.findById(analysisIdLong);
            if (!analysisOpt.isPresent()) {
                logger.warn("删除分析结果失败，分析不存在, analysisId={}", analysisId);
                return false;
            }

            // 删除相关的词频数据
            wordFrequencyRepository.deleteByAnalysisResultId(analysisIdLong);

            // 删除相关的时间轴事件数据
            timelineEventRepository.deleteByAnalysisResultId(analysisIdLong);

            // 删除相关的地理位置数据
            geoLocationRepository.deleteByAnalysisResultId(analysisIdLong);

            // 删除分析结果
            analysisResultRepository.deleteById(analysisIdLong);

            logger.info("分析结果删除成功, analysisId={}", analysisId);
            return true;
        } catch (NumberFormatException e) {
            logger.error("无效的ID格式, analysisId={}, userId={}: {}", analysisId, userId, e.getMessage());
            return false;
        } catch (Exception e) {
            logger.error("删除分析结果异常: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * 批量删除分析结果
     */
    @Override
    public int deleteAnalyses(List<String> analysisIds, String userId) {
        logger.info("批量删除分析结果, analysisIds={}, userId={}", analysisIds, userId);

        if (analysisIds == null || analysisIds.isEmpty()) {
            return 0;
        }

        int deletedCount = 0;
        for (String analysisId : analysisIds) {
            try {
                if (deleteAnalysis(analysisId, userId)) {
                    deletedCount++;
                }
            } catch (Exception e) {
                logger.warn("批量删除中分析失败, analysisId={}: {}", analysisId, e.getMessage());
            }
        }

        logger.info("批量删除分析结果完成, 删除数量={}", deletedCount);
        return deletedCount;
    }

    /**
     * 根据项目查询分析结果
     */
    @Override
    @Transactional(readOnly = true)
    public Page<AnalysisResult> findAnalysesByProject(String projectId, Pageable pageable) {
        logger.debug("根据项目查询分析结果, projectId={}", projectId);

        if (projectId == null) {
            throw new IllegalArgumentException("项目ID不能为空");
        }

        try {
            Long projectIdLong = Long.parseLong(projectId);
            return analysisResultRepository.findByProjectId(projectIdLong, pageable);
        } catch (NumberFormatException e) {
            logger.error("无效的项目ID格式: {}", projectId);
            throw new IllegalArgumentException("无效的项目ID格式");
        } catch (Exception e) {
            logger.error("根据项目查询分析结果异常: {}", e.getMessage(), e);
            throw new RuntimeException("查询分析结果失败", e);
        }
    }

    /**
     * 根据项目和分析类型查询分析结果
     */
    @Override
    @Transactional(readOnly = true)
    public Page<AnalysisResult> findAnalysesByProjectAndType(String projectId, AnalysisResult.AnalysisType analysisType, Pageable pageable) {
        logger.debug("根据项目和分析类型查询分析结果, projectId={}, analysisType={}", projectId, analysisType);

        if (projectId == null || analysisType == null) {
            throw new IllegalArgumentException("项目ID和分析类型不能为空");
        }

        try {
            Long projectIdLong = Long.parseLong(projectId);
            return analysisResultRepository.findByProjectIdAndAnalysisType(projectIdLong, analysisType, pageable);
        } catch (NumberFormatException e) {
            logger.error("无效的项目ID格式: {}", projectId);
            throw new IllegalArgumentException("无效的项目ID格式");
        } catch (Exception e) {
            logger.error("根据项目和分析类型查询分析结果异常: {}", e.getMessage(), e);
            throw new RuntimeException("查询分析结果失败", e);
        }
    }

    /**
     * 根据状态查询分析结果
     */
    @Override
    @Transactional(readOnly = true)
    public Page<AnalysisResult> findAnalysesByStatus(String projectId, AnalysisResult.AnalysisStatus status, Pageable pageable) {
        logger.debug("根据状态查询分析结果, projectId={}, status={}", projectId, status);

        if (status == null) {
            throw new IllegalArgumentException("状态不能为空");
        }

        try {
            if (projectId != null) {
                Long projectIdLong = Long.parseLong(projectId);
                return analysisResultRepository.findByProjectIdAndStatus(projectIdLong, status, pageable);
            } else {
                return analysisResultRepository.findByStatus(status, pageable);
            }
        } catch (NumberFormatException e) {
            logger.error("无效的项目ID格式: {}", projectId);
            throw new IllegalArgumentException("无效的项目ID格式");
        } catch (Exception e) {
            logger.error("根据状态查询分析结果异常: {}", e.getMessage(), e);
            throw new RuntimeException("查询分析结果失败", e);
        }
    }

    /**
     * 查询最近的分析结果
     */
    @Override
    @Transactional(readOnly = true)
    public List<AnalysisResult> findRecentAnalyses(String projectId, int days, int limit) {
        logger.debug("查询最近的分析结果, projectId={}, days={}, limit={}", projectId, days, limit);

        if (days <= 0 || limit <= 0) {
            throw new IllegalArgumentException("天数和限制数量必须大于0");
        }

        try {
            LocalDateTime since = LocalDateTime.now().minusDays(days);
            Pageable pageable = PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "createdAt"));

            if (projectId != null) {
                Long projectIdLong = Long.parseLong(projectId);
                return analysisResultRepository.findByProjectIdAndCreatedAtAfter(projectIdLong, since, pageable).getContent();
            } else {
                return analysisResultRepository.findRecentAnalyses(since, pageable);
            }
        } catch (NumberFormatException e) {
            logger.error("无效的项目ID格式: {}", projectId);
            throw new IllegalArgumentException("无效的项目ID格式");
        } catch (Exception e) {
            logger.error("查询最近分析结果异常: {}", e.getMessage(), e);
            throw new RuntimeException("查询分析结果失败", e);
        }
    }

    /**
     * 查询已完成的分析结果
     */
    @Override
    @Transactional(readOnly = true)
    public Page<AnalysisResult> findCompletedAnalyses(String projectId, Pageable pageable) {
        return findAnalysesByStatus(projectId, AnalysisResult.AnalysisStatus.COMPLETED, pageable);
    }

    /**
     * 查询失败的分析结果
     */
    @Override
    @Transactional(readOnly = true)
    public Page<AnalysisResult> findFailedAnalyses(String projectId, Pageable pageable) {
        return findAnalysesByStatus(projectId, AnalysisResult.AnalysisStatus.FAILED, pageable);
    }

    /**
     * 查询正在处理的分析结果
     */
    @Override
    @Transactional(readOnly = true)
    public Page<AnalysisResult> findProcessingAnalyses(String projectId, Pageable pageable) {
        return findAnalysesByStatus(projectId, AnalysisResult.AnalysisStatus.PROCESSING, pageable);
    }

    /**
     * 统计项目的分析数量
     */
    @Override
    @Transactional(readOnly = true)
    public long countAnalysesByProject(String projectId) {
        logger.debug("统计项目的分析数量, projectId={}", projectId);

        if (projectId == null) {
            throw new IllegalArgumentException("项目ID不能为空");
        }

        try {
            Long projectIdLong = Long.parseLong(projectId);
            return analysisResultRepository.countByProjectId(projectIdLong);
        } catch (NumberFormatException e) {
            logger.error("无效的项目ID格式: {}", projectId);
            throw new IllegalArgumentException("无效的项目ID格式");
        } catch (Exception e) {
            logger.error("统计项目分析数量异常: {}", e.getMessage(), e);
            return 0;
        }
    }

    /**
     * 统计分析类型数量
     */
    @Override
    @Transactional(readOnly = true)
    public long countAnalysesByType(String projectId, AnalysisResult.AnalysisType analysisType) {
        logger.debug("统计分析类型数量, projectId={}, analysisType={}", projectId, analysisType);

        if (analysisType == null) {
            throw new IllegalArgumentException("分析类型不能为空");
        }

        try {
            if (projectId != null) {
                Long projectIdLong = Long.parseLong(projectId);
                return analysisResultRepository.countByProjectIdAndAnalysisType(projectIdLong, analysisType);
            } else {
                return analysisResultRepository.countByAnalysisType(analysisType);
            }
        } catch (NumberFormatException e) {
            logger.error("无效的项目ID格式: {}", projectId);
            throw new IllegalArgumentException("无效的项目ID格式");
        } catch (Exception e) {
            logger.error("统计分析类型数量异常: {}", e.getMessage(), e);
            return 0;
        }
    }

    /**
     * 统计分析状态数量
     */
    @Override
    @Transactional(readOnly = true)
    public long countAnalysesByStatus(String projectId, AnalysisResult.AnalysisStatus status) {
        logger.debug("统计分析状态数量, projectId={}, status={}", projectId, status);

        if (status == null) {
            throw new IllegalArgumentException("状态不能为空");
        }

        try {
            if (projectId != null) {
                Long projectIdLong = Long.parseLong(projectId);
                return analysisResultRepository.countByProjectIdAndStatus(projectIdLong, status);
            } else {
                return analysisResultRepository.countByStatus(status);
            }
        } catch (NumberFormatException e) {
            logger.error("无效的项目ID格式: {}", projectId);
            throw new IllegalArgumentException("无效的项目ID格式");
        } catch (Exception e) {
            logger.error("统计分析状态数量异常: {}", e.getMessage(), e);
            return 0;
        }
    }

    /**
     * 获取分析总数
     */
    @Override
    @Transactional(readOnly = true)
    public long getTotalAnalysisCount() {
        logger.debug("获取分析总数");

        try {
            return analysisResultRepository.count();
        } catch (Exception e) {
            logger.error("获取分析总数异常: {}", e.getMessage(), e);
            return 0;
        }
    }

    /**
     * 获取分析状态统计
     */
    @Override
    @Transactional(readOnly = true)
    public Map<AnalysisResult.AnalysisStatus, Long> getAnalysisStatusStatistics(String projectId) {
        logger.debug("获取分析状态统计, projectId={}", projectId);

        try {
            List<Object[]> results;
            if (projectId != null) {
                Long projectIdLong = Long.parseLong(projectId);
                results = analysisResultRepository.getAnalysisStatusStatisticsByProject(projectIdLong);
            } else {
                results = analysisResultRepository.getAnalysisStatusStatistics();
            }

            Map<AnalysisResult.AnalysisStatus, Long> statistics = new HashMap<>();
            for (Object[] result : results) {
                AnalysisResult.AnalysisStatus status = (AnalysisResult.AnalysisStatus) result[0];
                Long count = (Long) result[1];
                statistics.put(status, count);
            }

            return statistics;
        } catch (NumberFormatException e) {
            logger.error("无效的项目ID格式: {}", projectId);
            throw new IllegalArgumentException("无效的项目ID格式");
        } catch (Exception e) {
            logger.error("获取分析状态统计异常: {}", e.getMessage(), e);
            return new HashMap<>();
        }
    }

    /**
     * 获取分析类型统计
     */
    @Override
    @Transactional(readOnly = true)
    public Map<AnalysisResult.AnalysisType, Long> getAnalysisTypeStatistics(String projectId) {
        logger.debug("获取分析类型统计, projectId={}", projectId);

        try {
            List<Object[]> results;
            if (projectId != null) {
                Long projectIdLong = Long.parseLong(projectId);
                results = analysisResultRepository.getAnalysisTypeStatisticsByProject(projectIdLong);
            } else {
                results = analysisResultRepository.getAnalysisTypeStatistics();
            }

            Map<AnalysisResult.AnalysisType, Long> statistics = new HashMap<>();
            for (Object[] result : results) {
                AnalysisResult.AnalysisType analysisType = (AnalysisResult.AnalysisType) result[0];
                Long count = (Long) result[1];
                statistics.put(analysisType, count);
            }

            return statistics;
        } catch (NumberFormatException e) {
            logger.error("无效的项目ID格式: {}", projectId);
            throw new IllegalArgumentException("无效的项目ID格式");
        } catch (Exception e) {
            logger.error("获取分析类型统计异常: {}", e.getMessage(), e);
            return new HashMap<>();
        }
    }

    /**
     * 获取分析处理时间统计
     */
    @Override
    @Transactional(readOnly = true)
    public Object[] getAnalysisProcessingTimeStatistics(String projectId) {
        logger.debug("获取分析处理时间统计, projectId={}", projectId);

        try {
            List<Object[]> result;
            if (projectId != null) {
                Long projectIdLong = Long.parseLong(projectId);
                result = analysisResultRepository.getAnalysisProcessingTimeStatisticsByProject(projectIdLong);
            } else {
                result = analysisResultRepository.getAnalysisProcessingTimeStatistics();
            }
            return result.isEmpty() ? new Object[]{0L, 0L, 0L} : result.get(0);
        } catch (NumberFormatException e) {
            logger.error("无效的项目ID格式: {}", projectId);
            throw new IllegalArgumentException("无效的项目ID格式");
        } catch (Exception e) {
            logger.error("获取分析处理时间统计异常: {}", e.getMessage(), e);
            return new Object[]{0L, 0L, 0L};
        }
    }

    /**
     * 获取用户的分析统计信息
     */
    @Override
    @Transactional(readOnly = true)
    public Object[] getUserAnalysisStatistics(String userId) {
        logger.debug("获取用户的分析统计信息, userId={}", userId);

        if (userId == null) {
            throw new IllegalArgumentException("用户ID不能为空");
        }

        try {
            Long userIdLong = Long.parseLong(userId);
            List<Object[]> result = analysisResultRepository.getUserAnalysisStatistics(userIdLong);
            return result.isEmpty() ? new Object[]{0L, 0L, 0L} : result.get(0);
        } catch (NumberFormatException e) {
            logger.error("无效的用户ID格式: {}", userId);
            throw new IllegalArgumentException("无效的用户ID格式");
        } catch (Exception e) {
            logger.error("获取用户分析统计信息异常: {}", e.getMessage(), e);
            return new Object[]{0L, 0L, 0L};
        }
    }

    /**
     * 重新执行分析
     */
    @Override
    public boolean rerunAnalysis(String analysisId, String userId) {
        logger.info("重新执行分析, analysisId={}, userId={}", analysisId, userId);

        if (analysisId == null || userId == null) {
            throw new IllegalArgumentException("分析ID和用户ID不能为空");
        }

        try {
            Long analysisIdLong = Long.parseLong(analysisId);
            Long userIdLong = Long.parseLong(userId);
            
            // 验证分析权限
            if (!hasAnalysisAccess(analysisIdLong, userIdLong)) {
                logger.warn("重新执行分析失败，无权限, analysisId={}, userId={}", analysisId, userId);
                return false;
            }

            Optional<AnalysisResult> analysisOpt = analysisResultRepository.findById(analysisIdLong);
            if (!analysisOpt.isPresent()) {
                logger.warn("重新执行分析失败，分析不存在, analysisId={}", analysisId);
                return false;
            }

            AnalysisResult analysis = analysisOpt.get();

            // 重置分析状态
            analysis.setStatus(AnalysisResult.AnalysisStatus.PENDING);
            analysis.setStartedAt(null);
            analysis.setCompletedAt(null);
            analysis.setProcessingTime(null);
            analysis.setErrorMessage(null);
            analysisResultRepository.save(analysis);

            // 异步执行分析
            executeAnalysisAsync(analysisIdLong.toString(), userIdLong.toString(), analysis.getAnalysisType());

            logger.info("重新执行分析成功, analysisId={}", analysisId);
            return true;
        } catch (NumberFormatException e) {
            logger.error("无效的ID格式, analysisId={}, userId={}", analysisId, userId);
            throw new IllegalArgumentException("无效的ID格式");
        } catch (Exception e) {
            logger.error("重新执行分析失败: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * 取消分析任务
     */
    @Override
    public boolean cancelAnalysis(String analysisId, String userId) {
        logger.info("取消分析任务, analysisId={}, userId={}", analysisId, userId);

        if (analysisId == null || userId == null) {
            throw new IllegalArgumentException("分析ID和用户ID不能为空");
        }

        try {
            Long analysisIdLong = Long.parseLong(analysisId);
            Long userIdLong = Long.parseLong(userId);
            
            // 验证分析权限
            if (!hasAnalysisAccess(analysisIdLong, userIdLong)) {
                logger.warn("取消分析任务失败，无权限, analysisId={}, userId={}", analysisId, userId);
                return false;
            }

            Optional<AnalysisResult> analysisOpt = analysisResultRepository.findById(analysisIdLong);
            if (!analysisOpt.isPresent()) {
                logger.warn("取消分析任务失败，分析不存在, analysisId={}", analysisId);
                return false;
            }

            AnalysisResult analysis = analysisOpt.get();

            // 只能取消待处理或正在处理的分析
            if (analysis.getStatus() != AnalysisResult.AnalysisStatus.PENDING && 
                analysis.getStatus() != AnalysisResult.AnalysisStatus.PROCESSING) {
                logger.warn("取消分析任务失败，分析状态不允许取消, analysisId={}, status={}", analysisId, analysis.getStatus());
                return false;
            }

            // 更新分析状态为已取消
            analysis.setStatus(AnalysisResult.AnalysisStatus.FAILED);
            analysis.setErrorMessage("用户取消了分析任务");
            analysis.setCompletedAt(LocalDateTime.now());
            analysisResultRepository.save(analysis);

            logger.info("取消分析任务成功, analysisId={}", analysisId);
            return true;
        } catch (NumberFormatException e) {
            logger.error("无效的ID格式, analysisId={}, userId={}", analysisId, userId);
            throw new IllegalArgumentException("无效的ID格式");
        } catch (Exception e) {
            logger.error("取消分析任务失败: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * 获取分析进度
     */
    @Override
    @Transactional(readOnly = true)
    public int getAnalysisProgress(String analysisId, String userId) {
        logger.debug("获取分析进度, analysisId={}, userId={}", analysisId, userId);

        if (analysisId == null || userId == null) {
            throw new IllegalArgumentException("分析ID和用户ID不能为空");
        }

        try {
            Long analysisIdLong = Long.parseLong(analysisId);
            Long userIdLong = Long.parseLong(userId);

            // 验证分析权限
            if (!hasAnalysisAccess(analysisIdLong, userIdLong)) {
                logger.warn("获取分析进度失败，无权限, analysisId={}, userId={}", analysisId, userId);
                return 0;
            }

            Optional<AnalysisResult> analysisOpt = analysisResultRepository.findById(analysisIdLong);
            if (!analysisOpt.isPresent()) {
                return 0;
            }

            AnalysisResult analysis = analysisOpt.get();

            switch (analysis.getStatus()) {
                case PENDING:
                    return 0;
                case PROCESSING:
                    return 50; // 简化的进度计算
                case COMPLETED:
                    return 100;
                case FAILED:
                    return 0;
                default:
                    return 0;
            }
        } catch (NumberFormatException e) {
            logger.error("无效的ID格式: analysisId={}, userId={}", analysisId, userId);
            throw new IllegalArgumentException("无效的ID格式");
        } catch (Exception e) {
            logger.error("获取分析进度异常: {}", e.getMessage(), e);
            return 0;
        }
    }

    /**
     * 检查用户是否有分析访问权限
     */
    @Override
    @Transactional(readOnly = true)
    public boolean hasAnalysisAccess(String analysisId, String userId) {
        logger.debug("检查分析访问权限, analysisId={}, userId={}", analysisId, userId);

        if (analysisId == null || userId == null) {
            return false;
        }

        try {
            Long analysisIdLong = Long.parseLong(analysisId);
            Long userIdLong = Long.parseLong(userId);
            
            Optional<AnalysisResult> analysisOpt = analysisResultRepository.findById(analysisIdLong);
            if (analysisOpt.isPresent()) {
                AnalysisResult analysis = analysisOpt.get();
                Long projectId = analysis.getProject().getId();
                return hasProjectAccess(projectId, userIdLong);
            }
            return false;
        } catch (NumberFormatException e) {
            logger.error("无效的ID格式: analysisId={}, userId={}", analysisId, userId);
            return false;
        } catch (Exception e) {
            logger.error("检查分析访问权限异常: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * 导出分析结果（String版本）
     */
    @Override
    @Transactional(readOnly = true)
    public String exportAnalysisResult(String analysisId, String userId, String format) {
        logger.info("导出分析结果, analysisId={}, userId={}, format={}", analysisId, userId, format);

        if (analysisId == null || userId == null || !StringUtils.hasText(format)) {
            throw new IllegalArgumentException("分析ID、用户ID和格式不能为空");
        }

        try {
            Long analysisIdLong = Long.parseLong(analysisId);
            Long userIdLong = Long.parseLong(userId);

            // 验证分析权限
            if (!hasAnalysisAccess(analysisIdLong, userIdLong)) {
                logger.warn("导出分析结果失败，无权限, analysisId={}, userId={}", analysisId, userId);
                throw new IllegalArgumentException("无权限访问该分析");
            }

            Optional<AnalysisResult> analysisOpt = analysisResultRepository.findById(analysisIdLong);
            if (!analysisOpt.isPresent()) {
                throw new IllegalArgumentException("分析不存在");
            }

            AnalysisResult analysis = analysisOpt.get();

            // 根据格式导出数据
            String exportPath = generateExportPath(analysisId, format);
            
            switch (format.toLowerCase()) {
                case "json":
                    exportToJson(analysis, exportPath);
                    break;
                case "csv":
                    exportToCsv(analysis, exportPath);
                    break;
                case "excel":
                    exportToExcel(analysis, exportPath);
                    break;
                default:
                    throw new IllegalArgumentException("不支持的导出格式: " + format);
            }

            logger.info("分析结果导出成功, analysisId={}, exportPath={}", analysisId, exportPath);
            return exportPath;
        } catch (NumberFormatException e) {
            logger.error("无效的ID格式: analysisId={}, userId={}", analysisId, userId);
            throw new IllegalArgumentException("无效的ID格式");
        } catch (Exception e) {
            logger.error("导出分析结果失败: {}", e.getMessage(), e);
            throw new RuntimeException("导出分析结果失败", e);
        }
    }

    /**
     * 导出分析结果（2参数版本）
     */
    @Override
    @Transactional(readOnly = true)
    public String exportAnalysisResult(String analysisId, String format) {
        // 从安全上下文获取当前用户ID
        // 这里简化处理，实际应该从SecurityContext获取
        String currentUserId = "1"; // 临时处理
        return exportAnalysisResult(analysisId, currentUserId, format);
    }



    /**
     * 清理失败的分析结果
     */
    @Override
    public int cleanupFailedAnalyses(LocalDateTime beforeDate) {
        logger.info("清理失败的分析结果, beforeDate={}", beforeDate);

        if (beforeDate == null) {
            throw new IllegalArgumentException("日期不能为空");
        }

        try {
            int deletedCount = analysisResultRepository.deleteFailedAnalysesCreatedBefore(beforeDate);
            logger.info("清理失败分析结果完成, deletedCount={}", deletedCount);
            return deletedCount;
        } catch (Exception e) {
            logger.error("清理失败分析结果异常: {}", e.getMessage(), e);
            throw new RuntimeException("清理分析结果失败", e);
        }
    }

    /**
     * 搜索分析结果
     */
    @Override
    @Transactional(readOnly = true)
    public Page<AnalysisResult> searchAnalyses(String projectId, String keyword, Pageable pageable) {
        logger.debug("搜索分析结果, projectId={}, keyword={}", projectId, keyword);

        if (!StringUtils.hasText(keyword)) {
            throw new IllegalArgumentException("搜索关键字不能为空");
        }

        try {
            if (StringUtils.hasText(projectId)) {
                Long projectIdLong = Long.parseLong(projectId);
                return analysisResultRepository.searchAnalysesByProjectIdAndKeyword(projectIdLong, keyword, pageable);
            } else {
                return analysisResultRepository.searchAnalysesByKeyword(keyword, pageable);
            }
        } catch (NumberFormatException e) {
            logger.error("无效的项目ID格式: {}", projectId);
            throw new IllegalArgumentException("无效的项目ID格式");
        } catch (Exception e) {
            logger.error("搜索分析结果异常: {}", e.getMessage(), e);
            throw new RuntimeException("搜索分析结果失败", e);
        }
    }

    /**
     * 获取项目分析概览
     */
    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getProjectAnalysisOverview(String projectId, String userId) {
        logger.debug("获取项目分析概览, projectId={}, userId={}", projectId, userId);

        if (projectId == null || userId == null) {
            throw new IllegalArgumentException("项目ID和用户ID不能为空");
        }

        try {
            Long projectIdLong = Long.parseLong(projectId);
            Long userIdLong = Long.parseLong(userId);

            // 验证项目权限
            if (!hasProjectAccess(projectIdLong, userIdLong)) {
                logger.warn("获取项目分析概览失败，无权限, projectId={}, userId={}", projectId, userId);
                throw new IllegalArgumentException("无权限访问该项目");
            }

            Map<String, Object> overview = new HashMap<>();
            overview.put("totalAnalyses", countAnalysesByProject(projectId));
            overview.put("statusStatistics", getAnalysisStatusStatistics(projectId));
            overview.put("typeStatistics", getAnalysisTypeStatistics(projectId));
            overview.put("processingTimeStatistics", getAnalysisProcessingTimeStatistics(projectId));

            return overview;
        } catch (NumberFormatException e) {
            logger.error("无效的ID格式: projectId={}, userId={}", projectId, userId);
            throw new IllegalArgumentException("无效的ID格式");
        } catch (Exception e) {
            logger.error("获取项目分析概览异常: {}", e.getMessage(), e);
            throw new RuntimeException("获取项目分析概览失败", e);
        }
    }

    // ========== 词频分析相关方法实现 ==========

    @Override
    @Transactional(readOnly = true)
    public Page<WordFrequency> findWordFrequenciesByAnalysis(String analysisId, Pageable pageable) {
        try {
            Long analysisIdLong = Long.parseLong(analysisId);
            return wordFrequencyRepository.findByAnalysisResultId(analysisIdLong, pageable);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("无效的分析ID格式");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Page<WordFrequency> findWordFrequenciesByAnalysisAndCategory(String analysisId, WordFrequency.Category category, Pageable pageable) {
        try {
            Long analysisIdLong = Long.parseLong(analysisId);
            return wordFrequencyRepository.findByAnalysisResultIdAndCategory(analysisIdLong, category, pageable);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("无效的分析ID格式");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<WordFrequency> findHighFrequencyWords(String analysisId, int minFrequency, int limit) {
        try {
            Long analysisIdLong = Long.parseLong(analysisId);
            Pageable pageable = PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "frequency"));
            return wordFrequencyRepository.findByAnalysisResultIdAndFrequencyGreaterThanEqual(analysisIdLong, minFrequency, pageable);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("无效的分析ID格式");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<WordFrequency> findHighRelevanceWords(String analysisId, double minRelevance, int limit) {
        try {
            Long analysisIdLong = Long.parseLong(analysisId);
            Pageable pageable = PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "relevanceScore"));
            return wordFrequencyRepository.findByAnalysisResultIdAndRelevanceScoreGreaterThanEqual(analysisIdLong, minRelevance, pageable);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("无效的分析ID格式");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Map<WordFrequency.Category, Long> getWordFrequencyStatistics(String analysisId) {
        try {
            Long analysisIdLong = Long.parseLong(analysisId);
            List<Object[]> results = wordFrequencyRepository.getWordFrequencyStatisticsByAnalysis(analysisIdLong);
            Map<WordFrequency.Category, Long> statistics = new HashMap<>();
            for (Object[] result : results) {
                WordFrequency.Category category = (WordFrequency.Category) result[0];
                Long count = (Long) result[1];
                statistics.put(category, count);
            }
            return statistics;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("无效的分析ID格式");
        }
    }

    // ========== 时间轴分析相关方法实现 ==========

    @Override
    @Transactional(readOnly = true)
    public Page<TimelineEvent> findTimelineEventsByAnalysis(String analysisId, Pageable pageable) {
        try {
            Long analysisIdLong = Long.parseLong(analysisId);
            return timelineEventRepository.findByAnalysisResultId(analysisIdLong, pageable);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("无效的分析ID格式");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TimelineEvent> findTimelineEventsByDateRange(String analysisId, LocalDateTime startDate, LocalDateTime endDate, Pageable pageable) {
        try {
            Long analysisIdLong = Long.parseLong(analysisId);
            LocalDate startLocalDate = startDate.toLocalDate();
            LocalDate endLocalDate = endDate.toLocalDate();
            return timelineEventRepository.findByAnalysisResultIdAndDateRange(analysisIdLong, startLocalDate, endLocalDate, pageable);
        } catch (NumberFormatException e) {
            logger.error("无效的分析ID格式: {}", analysisId);
            throw new IllegalArgumentException("无效的分析ID格式");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<TimelineEvent> findEarliestTimelineEvents(String analysisId, int limit) {
        try {
            Long analysisIdLong = Long.parseLong(analysisId);
            Pageable pageable = PageRequest.of(0, limit, Sort.by(Sort.Direction.ASC, "eventDate"));
            Page<TimelineEvent> page = timelineEventRepository.findByAnalysisResultId(analysisIdLong, pageable);
            return page.getContent();
        } catch (NumberFormatException e) {
            logger.error("无效的分析ID格式: {}", analysisId);
            throw new IllegalArgumentException("无效的分析ID格式");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<TimelineEvent> findLatestTimelineEvents(String analysisId, int limit) {
        try {
            Long analysisIdLong = Long.parseLong(analysisId);
            Pageable pageable = PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "eventDate"));
            Page<TimelineEvent> page = timelineEventRepository.findByAnalysisResultId(analysisIdLong, pageable);
            return page.getContent();
        } catch (NumberFormatException e) {
            logger.error("无效的分析ID格式: {}", analysisId);
            throw new IllegalArgumentException("无效的分析ID格式");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Object[] getTimelineEventStatistics(String analysisId) {
        try {
            Long analysisIdLong = Long.parseLong(analysisId);
            List<Object[]> result = timelineEventRepository.getTimelineEventStatisticsByAnalysis(analysisIdLong);
            return result.isEmpty() ? new Object[]{0L, 0L, 0L} : result.get(0);
        } catch (NumberFormatException e) {
            logger.error("无效的分析ID格式: {}", analysisId);
            throw new IllegalArgumentException("无效的分析ID格式");
        }
    }

    // ========== 地理分析相关方法实现 ==========

    @Override
    @Transactional(readOnly = true)
    public Page<GeoLocation> findGeoLocationsByAnalysis(String analysisId, Pageable pageable) {
        try {
            Long analysisIdLong = Long.parseLong(analysisId);
            return geoLocationRepository.findByAnalysisResultId(analysisIdLong, pageable);
        } catch (NumberFormatException e) {
            logger.error("无效的分析ID格式: {}", analysisId);
            throw new IllegalArgumentException("无效的分析ID格式");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Page<GeoLocation> findGeoLocationsByAnalysisAndType(String analysisId, GeoLocation.LocationType locationType, Pageable pageable) {
        try {
            Long analysisIdLong = Long.parseLong(analysisId);
            return geoLocationRepository.findByAnalysisResultIdAndLocationType(analysisIdLong, locationType, pageable);
        } catch (NumberFormatException e) {
            logger.error("无效的分析ID格式: {}", analysisId);
            throw new IllegalArgumentException("无效的分析ID格式");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Page<GeoLocation> findGeoLocationsByCoordinateRange(String analysisId, double minLatitude, double maxLatitude, 
                                                              double minLongitude, double maxLongitude, Pageable pageable) {
        try {
            Long analysisIdLong = Long.parseLong(analysisId);
            return geoLocationRepository.findByAnalysisResultIdAndLatitudeBetweenAndLongitudeBetween(
                analysisIdLong, minLatitude, maxLatitude, minLongitude, maxLongitude, pageable);
        } catch (NumberFormatException e) {
            logger.error("无效的分析ID格式: {}", analysisId);
            throw new IllegalArgumentException("无效的分析ID格式");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<GeoLocation> findNearbyGeoLocations(String analysisId, double centerLatitude, double centerLongitude, 
                                                   double radiusKm, int limit) {
        try {
            Long analysisIdLong = Long.parseLong(analysisId);
            Pageable pageable = PageRequest.of(0, limit);
            return geoLocationRepository.findNearbyLocationsByAnalysis(analysisIdLong, centerLatitude, centerLongitude, radiusKm, pageable);
        } catch (NumberFormatException e) {
            logger.error("无效的分析ID格式: {}", analysisId);
            throw new IllegalArgumentException("无效的分析ID格式");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Map<GeoLocation.LocationType, Long> getGeoLocationStatistics(String analysisId) {
        try {
            Long analysisIdLong = Long.parseLong(analysisId);
            List<Object[]> results = geoLocationRepository.getGeoLocationStatisticsByAnalysis(analysisIdLong);
            Map<GeoLocation.LocationType, Long> statistics = new HashMap<>();
            for (Object[] result : results) {
                GeoLocation.LocationType locationType = (GeoLocation.LocationType) result[0];
                Long count = (Long) result[1];
                statistics.put(locationType, count);
            }
            return statistics;
        } catch (NumberFormatException e) {
            logger.error("无效的分析ID格式: {}", analysisId);
            throw new IllegalArgumentException("无效的分析ID格式");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Long> getAnalysisStatisticsByProject(String projectId) {
        try {
            Long projectIdLong = Long.parseLong(projectId);
            Map<String, Long> statistics = new HashMap<>();
            
            // 获取项目的分析统计信息
            statistics.put("totalAnalyses", countAnalysesByProject(projectId));
            statistics.put("completedAnalyses", countAnalysesByStatus(projectId, AnalysisResult.AnalysisStatus.COMPLETED));
            statistics.put("failedAnalyses", countAnalysesByStatus(projectId, AnalysisResult.AnalysisStatus.FAILED));
            statistics.put("processingAnalyses", countAnalysesByStatus(projectId, AnalysisResult.AnalysisStatus.PROCESSING));
            statistics.put("pendingAnalyses", countAnalysesByStatus(projectId, AnalysisResult.AnalysisStatus.PENDING));
            
            // 按类型统计
            statistics.put("wordFrequencyAnalyses", countAnalysesByType(projectId, AnalysisResult.AnalysisType.WORD_FREQUENCY));
            statistics.put("timelineAnalyses", countAnalysesByType(projectId, AnalysisResult.AnalysisType.TIMELINE));
            statistics.put("geographyAnalyses", countAnalysesByType(projectId, AnalysisResult.AnalysisType.GEOGRAPHY));
            statistics.put("textSummaryAnalyses", countAnalysesByType(projectId, AnalysisResult.AnalysisType.TEXT_SUMMARY));
            statistics.put("multidimensionalAnalyses", countAnalysesByType(projectId, AnalysisResult.AnalysisType.MULTIDIMENSIONAL));
            
            return statistics;
        } catch (NumberFormatException e) {
            logger.error("无效的项目ID格式: {}", projectId, e);
            throw new IllegalArgumentException("无效的项目ID格式: " + projectId);
        } catch (Exception e) {
            logger.error("获取项目分析统计信息失败: projectId={}", projectId, e);
            throw new RuntimeException("获取项目分析统计信息失败", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Long> getAnalysisStatisticsByUser(String userId) {
        try {
            Long userIdLong = Long.parseLong(userId);
            Map<String, Long> statistics = new HashMap<>();
            
            // 获取用户的分析统计信息
            List<Object[]> userStatsResult = analysisResultRepository.getUserAnalysisStatistics(userIdLong);
            Object[] userStats = userStatsResult.isEmpty() ? new Object[]{0L, 0L, 0L, 0L} : userStatsResult.get(0);
            if (userStats != null && userStats.length >= 4) {
                statistics.put("totalAnalyses", (Long) userStats[0]);
                statistics.put("completedAnalyses", (Long) userStats[1]);
                statistics.put("failedAnalyses", (Long) userStats[2]);
                statistics.put("processingAnalyses", (Long) userStats[3]);
            } else {
                statistics.put("totalAnalyses", 0L);
                statistics.put("completedAnalyses", 0L);
                statistics.put("failedAnalyses", 0L);
                statistics.put("processingAnalyses", 0L);
            }
            
            return statistics;
        } catch (NumberFormatException e) {
            logger.error("用户ID格式错误: {}", userId, e);
            throw new IllegalArgumentException("无效的用户ID格式");
        } catch (Exception e) {
            logger.error("获取用户分析统计信息异常: {}", e.getMessage(), e);
            throw new RuntimeException("获取统计信息失败", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Long> getAnalysisStatistics() {
        try {
            Map<String, Long> statistics = new HashMap<>();
            
            // 获取全局分析统计信息
            statistics.put("totalAnalyses", getTotalAnalysisCount());
            statistics.put("completedAnalyses", analysisResultRepository.countByStatus(AnalysisResult.AnalysisStatus.COMPLETED));
            statistics.put("failedAnalyses", analysisResultRepository.countByStatus(AnalysisResult.AnalysisStatus.FAILED));
            statistics.put("processingAnalyses", analysisResultRepository.countByStatus(AnalysisResult.AnalysisStatus.PROCESSING));
            statistics.put("pendingAnalyses", analysisResultRepository.countByStatus(AnalysisResult.AnalysisStatus.PENDING));
            
            // 按类型统计
            statistics.put("wordFrequencyAnalyses", analysisResultRepository.countByAnalysisType(AnalysisResult.AnalysisType.WORD_FREQUENCY));
            statistics.put("timelineAnalyses", analysisResultRepository.countByAnalysisType(AnalysisResult.AnalysisType.TIMELINE));
            statistics.put("geographyAnalyses", analysisResultRepository.countByAnalysisType(AnalysisResult.AnalysisType.GEOGRAPHY));
            statistics.put("textSummaryAnalyses", analysisResultRepository.countByAnalysisType(AnalysisResult.AnalysisType.TEXT_SUMMARY));
            statistics.put("multidimensionalAnalyses", analysisResultRepository.countByAnalysisType(AnalysisResult.AnalysisType.MULTIDIMENSIONAL));
            
            return statistics;
        } catch (Exception e) {
            logger.error("获取全局分析统计信息异常: {}", e.getMessage(), e);
            throw new RuntimeException("获取统计信息失败", e);
        }
    }

    // ========== Controller层需要的方法实现 ==========

    @Override
    @Transactional(readOnly = true)
    public Page<GeoLocation> getGeographyResults(String analysisId, Pageable pageable) {
        try {
            Long analysisIdLong = Long.parseLong(analysisId);
            return findGeoLocationsByAnalysis(analysisId, pageable);
        } catch (NumberFormatException e) {
            logger.error("无效的分析ID格式: {}", analysisId);
            throw new IllegalArgumentException("无效的分析ID格式: " + analysisId);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Page<WordFrequency> getWordFrequencyResults(String analysisId, Pageable pageable) {
        try {
            Long analysisIdLong = Long.parseLong(analysisId);
            return wordFrequencyRepository.findByAnalysisResultId(analysisIdLong, pageable);
        } catch (NumberFormatException e) {
            logger.error("无效的分析ID格式: {}", analysisId);
            throw new IllegalArgumentException("无效的分析ID格式: " + analysisId);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TimelineEvent> getTimelineResults(String analysisId, Pageable pageable) {
        try {
            Long analysisIdLong = Long.parseLong(analysisId);
            return timelineEventRepository.findByAnalysisResultId(analysisIdLong, pageable);
        } catch (NumberFormatException e) {
            logger.error("无效的分析ID格式: {}", analysisId);
            throw new IllegalArgumentException("无效的分析ID格式: " + analysisId);
        }
    }



    // ========== 私有辅助方法 ==========

    /**
     * 验证分析参数
     */
    private void validateAnalysisParams(Long projectId, Long userId, AnalysisResult.AnalysisType analysisType, List<Long> fileIds) {
        if (projectId == null) {
            throw new IllegalArgumentException("项目ID不能为空");
        }

        if (userId == null) {
            throw new IllegalArgumentException("用户ID不能为空");
        }

        if (analysisType == null) {
            throw new IllegalArgumentException("分析类型不能为空");
        }

        if (fileIds == null || fileIds.isEmpty()) {
            throw new IllegalArgumentException("文件ID列表不能为空");
        }
    }

    /**
     * 检查项目访问权限
     */
    private boolean hasProjectAccess(Long projectId, Long userId) {
        try {
            Optional<Project> projectOpt = projectRepository.findById(projectId);
            if (projectOpt.isPresent()) {
                Project project = projectOpt.get();
                // 检查是否是项目所有者
                if (project.getUser().getId().equals(userId)) {
                    return true;
                }

                // 检查是否是管理员（管理员可以访问所有项目）
                Optional<User> userOpt = userRepository.findById(userId);
                if (userOpt.isPresent() && userOpt.get().getRole() == User.UserRole.ADMIN) {
                    return true;
                }
            }
            return false;
        } catch (Exception e) {
            logger.error("检查项目访问权限异常: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * 异步执行分析
     */
    private void executeAnalysisAsync(String analysisId, String userId, AnalysisResult.AnalysisType analysisType) {
        logger.info("开始异步执行分析任务, analysisId={}, userId={}, analysisType={}", analysisId, userId, analysisType);
        
        CompletableFuture.runAsync(() -> {
            logger.info("异步任务线程开始执行, analysisId={}, thread={}", analysisId, Thread.currentThread().getName());
            try {
                boolean success = false;
                switch (analysisType) {
                    case WORD_FREQUENCY:
                        logger.info("执行词频分析, analysisId={}", analysisId);
                        success = executeWordFrequencyAnalysis(analysisId, userId);
                        break;
                    case TIMELINE:
                        logger.info("执行时间轴分析, analysisId={}", analysisId);
                        success = executeTimelineAnalysis(analysisId, userId);
                        break;
                    case GEOGRAPHY:
                        logger.info("执行地理分析, analysisId={}", analysisId);
                        success = executeGeographyAnalysis(analysisId, userId);
                        break;
                    case MULTIDIMENSIONAL:
                        logger.info("执行多维度分析, analysisId={}", analysisId);
                        success = executeComprehensiveAnalysis(analysisId, userId);
                        break;
                    case TEXT_SUMMARY:
                        logger.info("执行文本摘要分析, analysisId={}", analysisId);
                        success = executeTextSummaryAnalysis(analysisId, userId);
                        break;
                    default:
                        logger.warn("未知的分析类型: {}, analysisId={}", analysisType, analysisId);
                        updateAnalysisError(analysisId, "未知的分析类型: " + analysisType);
                        return;
                }
                
                if (success) {
                    logger.info("异步分析任务执行成功, analysisId={}, analysisType={}", analysisId, analysisType);
                } else {
                    logger.error("异步分析任务执行失败, analysisId={}, analysisType={}", analysisId, analysisType);
                }
            } catch (Exception e) {
                logger.error("异步执行分析失败, analysisId={}, analysisType={}: {}", analysisId, analysisType, e.getMessage(), e);
                updateAnalysisError(analysisId, "执行失败: " + e.getMessage());
            }
        }).whenComplete((result, throwable) -> {
            if (throwable != null) {
                logger.error("异步任务完成时发生异常, analysisId={}: {}", analysisId, throwable.getMessage(), throwable);
                updateAnalysisError(analysisId, "任务执行异常: " + throwable.getMessage());
            } else {
                logger.info("异步任务完成, analysisId={}", analysisId);
            }
        });
    }

    /**
     * 从分析结果中获取文件ID列表
     */
    @SuppressWarnings("unchecked")
    private List<Long> getFileIdsFromAnalysis(AnalysisResult analysis) {
        try {
            String fileIdsJson = analysis.getFileIds();
            if (StringUtils.hasText(fileIdsJson)) {
                return objectMapper.readValue(fileIdsJson, 
                    objectMapper.getTypeFactory().constructCollectionType(List.class, Long.class));
            }
            return new ArrayList<>();
        } catch (Exception e) {
            logger.error("获取文件ID列表失败: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }



    /**
     * 处理词频分析结果
     */
    @SuppressWarnings("unchecked")
    private void processWordFrequencyResult(AnalysisResult analysis, Map<String, Object> nlpResponse) {
        try {
            logger.info("开始处理词频分析结果, analysisId={}", analysis.getId());
            logger.debug("NLP响应数据结构: {}", nlpResponse);

            // 检查nlpResponse是否为null
            if (nlpResponse == null) {
                throw new RuntimeException("NLP响应数据为null");
            }

            // 检查nlpResponse的内容
            logger.debug("NLP响应包含的键: {}", nlpResponse.keySet());

            // nlpResponse 已经是 data 部分，不需要再次获取 data 字段
            // 获取词频数据，注意NLP服务返回的是word_frequency而不是word_frequencies
            List<Map<String, Object>> wordFrequencies = (List<Map<String, Object>>) nlpResponse.get("word_frequency");
            if (wordFrequencies == null) {
                logger.warn("NLP响应中缺少word_frequency字段，尝试使用空列表");
                logger.debug("可用的字段: {}", nlpResponse.keySet());
                wordFrequencies = new ArrayList<>();
            }

            logger.info("处理词频数据，共{}个词汇", wordFrequencies.size());

            for (Map<String, Object> wordData : wordFrequencies) {
                logger.debug("处理词汇数据: {}", wordData);
                String word = (String) wordData.get("word");
                if (word == null || word.trim().isEmpty()) {
                    logger.warn("跳过空词汇数据: {}", wordData);
                    continue;
                }

                // 安全地获取频率数据
                Integer frequency = 1; // 默认频率
                Object freqObj = wordData.get("frequency");
                if (freqObj instanceof Number) {
                    frequency = ((Number) freqObj).intValue();
                } else if (freqObj instanceof String) {
                    try {
                        frequency = Integer.parseInt((String) freqObj);
                    } catch (NumberFormatException e) {
                        logger.warn("无法解析频率值: {}, 使用默认值1", freqObj);
                    }
                }

                // 安全地获取相关性评分
                Float relevanceScore = 0.0f; // 默认相关性评分
                Object scoreObj = wordData.get("relevance_score");
                if (scoreObj instanceof Number) {
                    relevanceScore = ((Number) scoreObj).floatValue();
                } else if (scoreObj instanceof String) {
                    try {
                        relevanceScore = Float.parseFloat((String) scoreObj);
                    } catch (NumberFormatException e) {
                        logger.warn("无法解析相关性评分: {}, 使用默认值0.0", scoreObj);
                    }
                }

                // 设置词汇类别
                WordFrequency.Category category = WordFrequency.Category.OTHER;
                String categoryStr = (String) wordData.get("category");
                if (StringUtils.hasText(categoryStr)) {
                    try {
                        category = WordFrequency.Category.valueOf(categoryStr.toUpperCase());
                    } catch (IllegalArgumentException e) {
                        logger.debug("未知的词汇类别: {}, 使用OTHER", categoryStr);
                        category = WordFrequency.Category.OTHER;
                    }
                }

                WordFrequency wordFrequency = new WordFrequency(analysis, word, category, frequency, relevanceScore);
                wordFrequencyRepository.save(wordFrequency);
                
                logger.debug("保存词频数据: word={}, frequency={}, category={}, relevanceScore={}", 
                    word, frequency, category, relevanceScore);
            }

            // 更新分析结果数据
            analysis.setResultData(convertToJson(nlpResponse));
            logger.info("词频分析结果处理完成, analysisId={}, 处理词汇数量={}", analysis.getId(), wordFrequencies.size());
        } catch (Exception e) {
            logger.error("处理词频分析结果失败: {}", e.getMessage(), e);
            throw new RuntimeException("处理词频分析结果失败: " + e.getMessage(), e);
        }
    }

    /**
     * 处理时间轴分析结果
     */
    @SuppressWarnings("unchecked")
    private void processTimelineResult(AnalysisResult analysis, Map<String, Object> nlpResponse) {
        try {
            List<Map<String, Object>> timelineEvents = (List<Map<String, Object>>) nlpResponse.get("timeline_events");

            for (Map<String, Object> eventData : timelineEvents) {
                String eventName = (String) eventData.get("event_name");
                String description = (String) eventData.get("description");
                
                // 解析事件日期
                LocalDate eventDate = null;
                String dateStr = (String) eventData.get("event_date");
                if (StringUtils.hasText(dateStr)) {
                    try {
                        // 尝试解析为LocalDate
                        eventDate = LocalDate.parse(dateStr);
                    } catch (Exception e) {
                        logger.warn("解析事件日期失败: {}", dateStr);
                    }
                }

                TimelineEvent timelineEvent = new TimelineEvent(analysis, eventName, eventDate, description);

                // 设置元数据
                Map<String, Object> metadata = (Map<String, Object>) eventData.get("metadata");
                if (metadata != null) {
                    timelineEvent.setMetadata(convertToJson(metadata));
                }

                timelineEventRepository.save(timelineEvent);
            }

            // 更新分析结果数据
            analysis.setResultData(convertToJson(nlpResponse));
        } catch (Exception e) {
            logger.error("处理时间轴分析结果失败: {}", e.getMessage(), e);
            throw new RuntimeException("处理时间轴分析结果失败", e);
        }
    }

    /**
     * 处理地理分析结果
     */
    @SuppressWarnings("unchecked")
    private void processGeographyResult(AnalysisResult analysis, Map<String, Object> nlpResponse) {
        try {
            List<Map<String, Object>> geoLocations = (List<Map<String, Object>>) nlpResponse.get("geo_locations");

            for (Map<String, Object> locationData : geoLocations) {
                String locationName = (String) locationData.get("location_name");
                
                // 设置坐标
                Float latitude = null;
                Float longitude = null;
                if (locationData.containsKey("latitude") && locationData.containsKey("longitude")) {
                    latitude = ((Number) locationData.get("latitude")).floatValue();
                    longitude = ((Number) locationData.get("longitude")).floatValue();
                }

                // 设置位置类型
                GeoLocation.LocationType locationType = GeoLocation.LocationType.OTHER;
                String typeStr = (String) locationData.get("location_type");
                if (StringUtils.hasText(typeStr)) {
                    try {
                        locationType = GeoLocation.LocationType.valueOf(typeStr.toUpperCase());
                    } catch (IllegalArgumentException e) {
                        locationType = GeoLocation.LocationType.OTHER;
                    }
                }

                GeoLocation geoLocation = new GeoLocation(analysis, locationName, latitude, longitude, locationType);

                // 设置元数据
                Map<String, Object> metadata = (Map<String, Object>) locationData.get("metadata");
                if (metadata != null) {
                    geoLocation.setMetadata(convertToJson(metadata));
                }

                geoLocationRepository.save(geoLocation);
            }

            // 更新分析结果数据
            analysis.setResultData(convertToJson(nlpResponse));
        } catch (Exception e) {
            logger.error("处理地理分析结果失败: {}", e.getMessage(), e);
            throw new RuntimeException("处理地理分析结果失败", e);
        }
    }

    /**
     * 处理综合分析结果
     */
    @SuppressWarnings("unchecked")
    private void processComprehensiveResult(AnalysisResult analysis, Map<String, Object> nlpResponse) {
        try {
            // 处理词频数据
            if (nlpResponse.containsKey("word_frequencies")) {
                processWordFrequencyResult(analysis, nlpResponse);
            }

            // 处理时间轴数据
            if (nlpResponse.containsKey("timeline_events")) {
                processTimelineResult(analysis, nlpResponse);
            }

            // 处理地理数据
            if (nlpResponse.containsKey("geo_locations")) {
                processGeographyResult(analysis, nlpResponse);
            }

            // 处理文本摘要数据
            if (nlpResponse.containsKey("summary")) {
                processTextSummaryResult(analysis, nlpResponse);
            }

            // 更新分析结果数据
            analysis.setResultData(convertToJson(nlpResponse));
        } catch (Exception e) {
            logger.error("处理综合分析结果失败: {}", e.getMessage(), e);
            throw new RuntimeException("处理综合分析结果失败", e);
        }
    }

    /**
     * 处理文本摘要分析结果
     */
    private void processTextSummaryResult(AnalysisResult analysis, Map<String, Object> nlpResponse) {
        try {
            // 文本摘要结果直接保存到分析结果数据中
            analysis.setResultData(convertToJson(nlpResponse));
        } catch (Exception e) {
            logger.error("处理文本摘要分析结果失败: {}", e.getMessage(), e);
            throw new RuntimeException("处理文本摘要分析结果失败", e);
        }
    }

    /**
     * 更新分析错误状态
     */
    private void updateAnalysisError(Long analysisId, String errorMessage) {
        try {
            Optional<AnalysisResult> analysisOpt = analysisResultRepository.findById(analysisId);
            if (analysisOpt.isPresent()) {
                AnalysisResult analysis = analysisOpt.get();
                analysis.failAnalysis(errorMessage);
                analysisResultRepository.save(analysis);
            }
        } catch (Exception e) {
            logger.error("更新分析错误状态失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 更新分析错误状态（String版本）
     */
    private void updateAnalysisError(String analysisId, String errorMessage) {
        try {
            Long analysisIdLong = Long.parseLong(analysisId);
            updateAnalysisError(analysisIdLong, errorMessage);
        } catch (NumberFormatException e) {
            logger.error("无效的分析ID格式: {}", analysisId);
        }
    }

    /**
     * 计算处理时间
     */
    private Long calculateProcessingTime(LocalDateTime startTime, LocalDateTime endTime) {
        if (startTime != null && endTime != null) {
            return java.time.Duration.between(startTime, endTime).toMillis();
        }
        return null;
    }

    /**
     * 生成导出路径
     */
    private String generateExportPath(String analysisId, String format) {
        String timestamp = LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        return String.format("exports/analysis_%s_%s.%s", analysisId, timestamp, format);
    }

    /**
     * 导出为JSON格式
     */
    private void exportToJson(AnalysisResult analysis, String exportPath) {
        // TODO: 实现JSON导出逻辑
        logger.info("导出JSON格式, analysisId={}, exportPath={}", analysis.getId(), exportPath);
    }

    /**
     * 导出为CSV格式
     */
    private void exportToCsv(AnalysisResult analysis, String exportPath) {
        // TODO: 实现CSV导出逻辑
        logger.info("导出CSV格式, analysisId={}, exportPath={}", analysis.getId(), exportPath);
    }

    /**
     * 导出为Excel格式
     */
    private void exportToExcel(AnalysisResult analysis, String exportPath) {
        // TODO: 实现Excel导出逻辑
        logger.info("导出Excel格式, analysisId={}, exportPath={}", analysis.getId(), exportPath);
    }

    /**
     * 将对象转换为JSON字符串
     */
    private String convertToJson(Object object) {
        try {
            return objectMapper.writeValueAsString(object);
        } catch (Exception e) {
            logger.error("转换为JSON失败: {}", e.getMessage(), e);
            return "{}";
        }
    }

    /**
     * 将JSON字符串转换为Map对象
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> convertFromJson(String json) {
        try {
            if (StringUtils.hasText(json)) {
                return objectMapper.readValue(json, Map.class);
            }
            return new HashMap<>();
        } catch (Exception e) {
            logger.error("从JSON转换失败: {}", e.getMessage(), e);
            return new HashMap<>();
        }
    }

    /**
     * 根据项目ID和状态查找分析结果
     */
    @Override
    public Page<AnalysisResult> findAnalysesByProjectAndStatus(String projectId, AnalysisResult.AnalysisStatus status, Pageable pageable) {
        try {
            Long projectIdLong = Long.parseLong(projectId);
            return analysisResultRepository.findByProjectIdAndStatus(projectIdLong, status, pageable);
        } catch (NumberFormatException e) {
            logger.error("无效的项目ID格式: {}", projectId);
            return Page.empty(pageable);
        } catch (Exception e) {
            logger.error("查找分析结果失败: {}", e.getMessage(), e);
            return Page.empty(pageable);
        }
     }

    /**
     * 删除分析结果
     */
    @Override
    @Transactional
    public boolean deleteAnalysisResult(String analysisId) {
        try {
            Long analysisIdLong = Long.parseLong(analysisId);
            
            // 检查分析是否存在
            Optional<AnalysisResult> analysisOpt = analysisResultRepository.findById(analysisIdLong);
            if (!analysisOpt.isPresent()) {
                logger.warn("删除分析结果失败，分析不存在, analysisId={}", analysisId);
                return false;
            }
            
            // 删除分析结果
            analysisResultRepository.deleteById(analysisIdLong);
            logger.info("删除分析结果成功, analysisId={}", analysisId);
            return true;
        } catch (NumberFormatException e) {
            logger.error("无效的分析ID格式: {}", analysisId);
            return false;
        } catch (Exception e) {
            logger.error("删除分析结果异常: {}", e.getMessage(), e);
            return false;
        }
    }
}