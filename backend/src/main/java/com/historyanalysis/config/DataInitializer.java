/**
 * 数据初始化组件
 * @author AI Agent
 * @version 1.0.0
 * @created 2024-12-29 18:00:00
 */
package com.historyanalysis.config;

import com.historyanalysis.entity.User;
import com.historyanalysis.entity.Project;
import com.historyanalysis.entity.FileInfo;
import com.historyanalysis.entity.AnalysisResult;
import com.historyanalysis.repository.UserRepository;
import com.historyanalysis.repository.ProjectRepository;
import com.historyanalysis.repository.FileInfoRepository;
import com.historyanalysis.repository.AnalysisResultRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * H2数据库初始化组件
 * 
 * 在应用启动时自动创建测试数据，包括：
 * - 测试用户
 * - 示例项目
 * - 测试文件信息
 * - 分析任务示例
 */
@Component
@Profile("h2")
public class DataInitializer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private FileInfoRepository fileInfoRepository;

    @Autowired
    private AnalysisResultRepository analysisResultRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        logger.info("开始初始化H2数据库测试数据...");

        try {
            // 检查是否已经初始化过
            if (userRepository.count() > 0) {
                logger.info("数据库已包含数据，跳过初始化");
                return;
            }

            // 创建测试用户
            createTestUsers();

            // 创建示例项目
            createTestProjects();

            // 创建测试文件信息
            createTestFiles();

            // 创建分析结果示例
            createTestAnalysisResults();

            logger.info("H2数据库测试数据初始化完成");

        } catch (Exception e) {
            logger.error("数据库初始化失败: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 创建测试用户
     */
    private void createTestUsers() {
        logger.info("创建测试用户...");

        // 管理员用户
        User admin = new User();
        admin.setUsername("admin");
        admin.setEmail("admin@historyanalysis.com");
        admin.setPasswordHash(passwordEncoder.encode("admin123"));
        admin.setRealName("系统管理员");
        admin.setRole(User.UserRole.ADMIN);
        admin.setStatus(User.UserStatus.ACTIVE);
        admin.setCreatedAt(LocalDateTime.now());
        admin.setUpdatedAt(LocalDateTime.now());
        userRepository.save(admin);

        // 历史学家用户
        User historian = new User();
        historian.setUsername("historian1");
        historian.setEmail("historian1@historyanalysis.com");
        historian.setPasswordHash(passwordEncoder.encode("historian123"));
        historian.setRealName("张历史");
        historian.setRole(User.UserRole.USER);
        historian.setStatus(User.UserStatus.ACTIVE);
        historian.setCreatedAt(LocalDateTime.now());
        historian.setUpdatedAt(LocalDateTime.now());
        userRepository.save(historian);

        // 研究员用户
        User researcher = new User();
        researcher.setUsername("researcher1");
        researcher.setEmail("researcher1@historyanalysis.com");
        researcher.setPasswordHash(passwordEncoder.encode("researcher123"));
        researcher.setRealName("李研究");
        researcher.setRole(User.UserRole.USER);
        researcher.setStatus(User.UserStatus.ACTIVE);
        researcher.setCreatedAt(LocalDateTime.now());
        researcher.setUpdatedAt(LocalDateTime.now());
        userRepository.save(researcher);

        logger.info("测试用户创建完成");
    }

    /**
     * 创建示例项目
     */
    private void createTestProjects() {
        logger.info("创建示例项目...");

        User admin = userRepository.findByUsername("admin").orElse(null);
        User historian = userRepository.findByUsername("historian1").orElse(null);

        if (admin != null) {
            // 明清史料分析项目
            Project project1 = new Project();
            project1.setName("明清史料分析");
            project1.setDescription("对明清时期的历史文献进行文本分析和数据挖掘，包括词频统计、时间轴分析、地理位置识别等功能。");
            project1.setUserId(admin.getId());
            project1.setStatus(Project.ProjectStatus.ACTIVE);
            project1.setCreatedAt(LocalDateTime.now());
            project1.setUpdatedAt(LocalDateTime.now());
            projectRepository.save(project1);

            // 古代地理研究项目
            Project project2 = new Project();
            project2.setName("古代地理研究");
            project2.setDescription("研究古代中国的地理变迁和行政区划演变，通过文本分析识别地名和地理信息。");
            project2.setUserId(admin.getId());
            project2.setStatus(Project.ProjectStatus.ACTIVE);
            project2.setCreatedAt(LocalDateTime.now());
            project2.setUpdatedAt(LocalDateTime.now());
            projectRepository.save(project2);
        }

        if (historian != null) {
            // 史记文本研究项目
            Project project3 = new Project();
            project3.setName("史记文本研究");
            project3.setDescription("对《史记》文本进行深度分析，研究其语言特点、历史事件和人物关系。");
            project3.setUserId(historian.getId());
            project3.setStatus(Project.ProjectStatus.ACTIVE);
            project3.setCreatedAt(LocalDateTime.now());
            project3.setUpdatedAt(LocalDateTime.now());
            projectRepository.save(project3);
        }

        logger.info("示例项目创建完成");
    }

    /**
     * 创建测试文件信息
     */
    private void createTestFiles() {
        logger.info("创建测试文件信息...");

        // 获取用户
        User admin = userRepository.findByUsername("admin").orElse(null);
        if (admin == null) {
            logger.warn("未找到admin用户，跳过文件创建");
            return;
        }

        Project project1 = projectRepository.findByNameAndUserId("明清史料分析", admin.getId()).orElse(null);
        Project project2 = projectRepository.findByNameAndUserId("古代地理研究", admin.getId()).orElse(null);

        if (project1 != null) {
            // 明清史料文件
            FileInfo file1 = new FileInfo();
            file1.setOriginalFilename("明史选段.txt");
            file1.setFilename("ming_history_sample.txt");
            file1.setFilePath("/uploads/ming_history_sample.txt");
            file1.setFileSize(15680L);
            file1.setFileType("txt");
            file1.setMimeType("text/plain");
            file1.setProjectId(project1.getId());
            file1.setUserId(admin.getId());
            file1.setCreatedAt(LocalDateTime.now());
            file1.setUpdatedAt(LocalDateTime.now());
            fileInfoRepository.save(file1);

            FileInfo file2 = new FileInfo();
            file2.setOriginalFilename("清史稿摘录.txt");
            file2.setFilename("qing_history_excerpt.txt");
            file2.setFilePath("/uploads/qing_history_excerpt.txt");
            file2.setFileSize(23450L);
            file2.setFileType("txt");
            file2.setMimeType("text/plain");
            file2.setProjectId(project1.getId());
            file2.setUserId(admin.getId());
            file2.setCreatedAt(LocalDateTime.now());
            file2.setUpdatedAt(LocalDateTime.now());
            fileInfoRepository.save(file2);
        }

        if (project2 != null) {
            // 地理研究文件
            FileInfo file3 = new FileInfo();
            file3.setOriginalFilename("古代地名考证.txt");
            file3.setFilename("ancient_place_names.txt");
            file3.setFilePath("/uploads/ancient_place_names.txt");
            file3.setFileSize(18920L);
            file3.setFileType("txt");
            file3.setMimeType("text/plain");
            file3.setProjectId(project2.getId());
            file3.setUserId(admin.getId());
            file3.setCreatedAt(LocalDateTime.now());
            file3.setUpdatedAt(LocalDateTime.now());
            fileInfoRepository.save(file3);
        }

        logger.info("测试文件信息创建完成");
    }

    /**
     * 创建分析结果示例
     */
    private void createTestAnalysisResults() {
        logger.info("创建分析结果示例...");

        User admin = userRepository.findByUsername("admin").orElse(null);
        if (admin == null) {
            logger.warn("未找到admin用户，跳过分析结果创建");
            return;
        }
        
        Project project1 = projectRepository.findByNameAndUserId("明清史料分析", admin.getId()).orElse(null);

        if (project1 != null && admin != null) {
            // 词频分析结果
            AnalysisResult result1 = new AnalysisResult();
            result1.setProjectId(project1.getId());
            result1.setUserId(admin.getId());
            result1.setAnalysisType(AnalysisResult.AnalysisType.WORD_FREQUENCY);
            result1.setStatus(AnalysisResult.AnalysisStatus.PENDING);
            // 设置分析参数，包含描述信息
            result1.setParameters("{\"description\": \"明清史料词频统计分析\", \"minWordLength\": 2, \"maxWords\": 100}");
            result1.setCreatedAt(LocalDateTime.now());
            result1.setUpdatedAt(LocalDateTime.now());
            analysisResultRepository.save(result1);

            // 时间轴分析结果
            AnalysisResult result2 = new AnalysisResult();
            result2.setProjectId(project1.getId());
            result2.setUserId(admin.getId());
            result2.setAnalysisType(AnalysisResult.AnalysisType.TIMELINE);
            result2.setStatus(AnalysisResult.AnalysisStatus.PENDING);
            // 设置分析参数，包含描述信息
            result2.setParameters("{\"description\": \"明清历史事件时间轴分析\", \"model\": \"sentiment\", \"threshold\": 0.5}");
            result2.setCreatedAt(LocalDateTime.now());
            result2.setUpdatedAt(LocalDateTime.now());
            analysisResultRepository.save(result2);

            // 地理分析结果
            AnalysisResult result3 = new AnalysisResult();
            result3.setProjectId(project1.getId());
            result3.setUserId(admin.getId());
            result3.setAnalysisType(AnalysisResult.AnalysisType.GEOGRAPHY);
            result3.setStatus(AnalysisResult.AnalysisStatus.PENDING);
            // 设置分析参数，包含描述信息
            result3.setParameters("{\"description\": \"明清史料地理位置识别\", \"extractCount\": 20, \"algorithm\": \"tfidf\"}");
            result3.setCreatedAt(LocalDateTime.now());
            result3.setUpdatedAt(LocalDateTime.now());
            analysisResultRepository.save(result3);
        }

        logger.info("分析结果示例创建完成");
    }
}