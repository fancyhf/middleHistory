-- 更新数据库表结构以匹配实体类
-- @author AI Agent
-- @version 1.0.0
-- @created 2024-12-29 18:30:00

USE history_analysis;

-- 更新用户表结构
ALTER TABLE users 
    CHANGE COLUMN full_name real_name VARCHAR(100) COMMENT '真实姓名',
    ADD COLUMN role ENUM('ADMIN', 'USER') DEFAULT 'USER' COMMENT '用户角色' AFTER password_hash,
    ADD COLUMN last_login_ip VARCHAR(45) COMMENT '最后登录IP' AFTER last_login_at,
    MODIFY COLUMN status ENUM('ACTIVE', 'INACTIVE', 'LOCKED', 'DELETED') DEFAULT 'ACTIVE' COMMENT '用户状态';

-- 更新文件信息表结构
ALTER TABLE file_info
    CHANGE COLUMN original_name filename VARCHAR(500) NOT NULL COMMENT '文件名',
    ADD COLUMN original_filename VARCHAR(500) NOT NULL COMMENT '原始文件名' AFTER filename,
    CHANGE COLUMN stored_name file_path VARCHAR(1000) NOT NULL COMMENT '文件路径',
    ADD COLUMN mime_type VARCHAR(100) COMMENT 'MIME类型' AFTER file_type,
    ADD COLUMN content_hash VARCHAR(64) COMMENT '内容哈希' AFTER mime_type,
    ADD COLUMN text_content LONGTEXT COMMENT '文本内容' AFTER content_hash,
    ADD COLUMN text_length INT COMMENT '文本长度' AFTER text_content,
    ADD COLUMN encoding VARCHAR(50) COMMENT '文件编码' AFTER text_length,
    ADD COLUMN metadata JSON COMMENT '元数据' AFTER encoding,
    DROP COLUMN stored_name,
    DROP COLUMN md5_hash,
    DROP COLUMN content_preview,
    DROP COLUMN upload_time,
    DROP COLUMN analysis_count,
    DROP COLUMN last_analysis_at,
    MODIFY COLUMN status ENUM('UPLOADED', 'PROCESSING', 'PROCESSED', 'ERROR') DEFAULT 'UPLOADED' COMMENT '处理状态';

-- 更新分析结果表结构（基于AnalysisResult实体）
DROP TABLE IF EXISTS analysis_results;
CREATE TABLE analysis_results (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '结果ID',
    analysis_type ENUM('WORD_FREQUENCY', 'TIMELINE_ANALYSIS', 'GEOGRAPHIC_ANALYSIS', 'TEXT_SUMMARY', 'SENTIMENT_ANALYSIS') NOT NULL COMMENT '分析类型',
    status ENUM('PENDING', 'PROCESSING', 'COMPLETED', 'FAILED') DEFAULT 'PENDING' COMMENT '分析状态',
    file_ids JSON COMMENT '文件ID列表（JSON数组）',
    project_id BIGINT NOT NULL COMMENT '项目ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    parameters JSON COMMENT '分析参数（JSON格式）',
    result_data JSON COMMENT '分析结果数据（JSON格式）',
    progress INT DEFAULT 0 COMMENT '进度百分比',
    error_message TEXT COMMENT '错误信息',
    execution_time INT COMMENT '执行时间（毫秒）',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    
    FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_project_id (project_id),
    INDEX idx_user_id (user_id),
    INDEX idx_analysis_type (analysis_type),
    INDEX idx_status (status),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='分析结果表';

-- 删除不需要的表
DROP TABLE IF EXISTS analysis_tasks;
DROP TABLE IF EXISTS user_sessions;
DROP TABLE IF EXISTS system_logs;

-- 更新索引
ALTER TABLE users ADD INDEX idx_role (role);
ALTER TABLE file_info ADD INDEX idx_mime_type (mime_type);
ALTER TABLE file_info ADD INDEX idx_content_hash (content_hash);