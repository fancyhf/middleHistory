-- 历史数据统计分析工具数据库初始化脚本
-- Author: AI Agent
-- Version: 1.0.0
-- Created: 2024-12-29 17:50:00

-- 创建数据库
CREATE DATABASE IF NOT EXISTS history_analysis 
CHARACTER SET utf8mb4 
COLLATE utf8mb4_unicode_ci;

USE history_analysis;

-- 用户表
CREATE TABLE users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '用户ID',
    username VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
    email VARCHAR(100) NOT NULL UNIQUE COMMENT '邮箱',
    password_hash VARCHAR(255) NOT NULL COMMENT '密码哈希',
    full_name VARCHAR(100) COMMENT '全名',
    avatar_url VARCHAR(255) COMMENT '头像URL',
    role ENUM('USER', 'ADMIN') DEFAULT 'USER' COMMENT '用户角色',
    status ENUM('ACTIVE', 'INACTIVE', 'SUSPENDED') DEFAULT 'ACTIVE' COMMENT '用户状态',
    last_login_at TIMESTAMP NULL COMMENT '最后登录时间',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    
    INDEX idx_username (username),
    INDEX idx_email (email),
    INDEX idx_status (status),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB COMMENT='用户表';

-- 项目表
CREATE TABLE projects (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '项目ID',
    name VARCHAR(200) NOT NULL COMMENT '项目名称',
    description TEXT COMMENT '项目描述',
    user_id BIGINT NOT NULL COMMENT '创建用户ID',
    status ENUM('ACTIVE', 'ARCHIVED', 'DELETED') DEFAULT 'ACTIVE' COMMENT '项目状态',
    settings JSON COMMENT '项目设置',
    file_count INT DEFAULT 0 COMMENT '文件数量',
    analysis_count INT DEFAULT 0 COMMENT '分析次数',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    
    INDEX idx_user_id (user_id),
    INDEX idx_status (status),
    INDEX idx_created_at (created_at),
    INDEX idx_name (name),
    
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB COMMENT='项目表';

-- 文件表
CREATE TABLE files (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '文件ID',
    filename VARCHAR(255) NOT NULL COMMENT '文件名',
    original_filename VARCHAR(255) NOT NULL COMMENT '原始文件名',
    file_path VARCHAR(500) NOT NULL COMMENT '文件路径',
    file_size BIGINT NOT NULL COMMENT '文件大小（字节）',
    file_type VARCHAR(50) NOT NULL COMMENT '文件类型',
    mime_type VARCHAR(100) COMMENT 'MIME类型',
    project_id BIGINT NOT NULL COMMENT '所属项目ID',
    user_id BIGINT NOT NULL COMMENT '上传用户ID',
    content_hash VARCHAR(64) COMMENT '内容哈希',
    text_content LONGTEXT COMMENT '提取的文本内容',
    text_length INT DEFAULT 0 COMMENT '文本长度',
    encoding VARCHAR(20) DEFAULT 'UTF-8' COMMENT '文件编码',
    status ENUM('UPLOADED', 'PROCESSING', 'PROCESSED', 'ERROR') DEFAULT 'UPLOADED' COMMENT '处理状态',
    error_message TEXT COMMENT '错误信息',
    metadata JSON COMMENT '文件元数据',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    
    INDEX idx_project_id (project_id),
    INDEX idx_user_id (user_id),
    INDEX idx_filename (filename),
    INDEX idx_file_type (file_type),
    INDEX idx_status (status),
    INDEX idx_created_at (created_at),
    INDEX idx_content_hash (content_hash),
    
    FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB COMMENT='文件表';

-- 分析结果表
CREATE TABLE analysis_results (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '分析结果ID',
    project_id BIGINT NOT NULL COMMENT '项目ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    analysis_type ENUM('WORD_FREQUENCY', 'TIMELINE', 'GEOGRAPHY', 'TEXT_SUMMARY', 'MULTIDIMENSIONAL') NOT NULL COMMENT '分析类型',
    file_ids JSON COMMENT '分析的文件ID列表',
    parameters JSON COMMENT '分析参数',
    result_data JSON COMMENT '分析结果数据',
    status ENUM('PENDING', 'PROCESSING', 'COMPLETED', 'FAILED') DEFAULT 'PENDING' COMMENT '分析状态',
    progress INT DEFAULT 0 COMMENT '分析进度（0-100）',
    error_message TEXT COMMENT '错误信息',
    execution_time INT COMMENT '执行时间（毫秒）',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    
    INDEX idx_project_id (project_id),
    INDEX idx_user_id (user_id),
    INDEX idx_analysis_type (analysis_type),
    INDEX idx_status (status),
    INDEX idx_created_at (created_at),
    
    FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB COMMENT='分析结果表';

-- 词频分析结果表
CREATE TABLE word_frequency_results (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'ID',
    analysis_id BIGINT NOT NULL COMMENT '分析结果ID',
    word VARCHAR(100) NOT NULL COMMENT '词语',
    frequency INT NOT NULL COMMENT '词频',
    category VARCHAR(50) COMMENT '词语分类',
    weight DECIMAL(10,6) COMMENT '权重',
    rank_position INT COMMENT '排名位置',
    
    INDEX idx_analysis_id (analysis_id),
    INDEX idx_word (word),
    INDEX idx_frequency (frequency),
    INDEX idx_category (category),
    INDEX idx_rank_position (rank_position),
    
    FOREIGN KEY (analysis_id) REFERENCES analysis_results(id) ON DELETE CASCADE
) ENGINE=InnoDB COMMENT='词频分析结果表';

-- 时间轴事件表
CREATE TABLE timeline_events (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'ID',
    analysis_id BIGINT NOT NULL COMMENT '分析结果ID',
    event_text TEXT NOT NULL COMMENT '事件文本',
    event_type VARCHAR(50) COMMENT '事件类型',
    time_expression VARCHAR(200) COMMENT '时间表达式',
    parsed_time VARCHAR(100) COMMENT '解析后的时间',
    year_start INT COMMENT '开始年份',
    year_end INT COMMENT '结束年份',
    dynasty VARCHAR(50) COMMENT '朝代',
    confidence DECIMAL(5,3) COMMENT '置信度',
    importance_score DECIMAL(5,3) COMMENT '重要性得分',
    entities JSON COMMENT '相关实体',
    location VARCHAR(200) COMMENT '地点',
    
    INDEX idx_analysis_id (analysis_id),
    INDEX idx_event_type (event_type),
    INDEX idx_year_start (year_start),
    INDEX idx_year_end (year_end),
    INDEX idx_dynasty (dynasty),
    INDEX idx_confidence (confidence),
    INDEX idx_importance_score (importance_score),
    
    FOREIGN KEY (analysis_id) REFERENCES analysis_results(id) ON DELETE CASCADE
) ENGINE=InnoDB COMMENT='时间轴事件表';

-- 地理位置表
CREATE TABLE geo_locations (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'ID',
    analysis_id BIGINT NOT NULL COMMENT '分析结果ID',
    original_name VARCHAR(200) NOT NULL COMMENT '原始地名',
    standard_name VARCHAR(200) COMMENT '标准地名',
    location_type VARCHAR(50) COMMENT '地点类型',
    level VARCHAR(50) COMMENT '地理级别',
    province VARCHAR(100) COMMENT '省份',
    latitude DECIMAL(10,7) COMMENT '纬度',
    longitude DECIMAL(10,7) COMMENT '经度',
    confidence DECIMAL(5,3) COMMENT '置信度',
    occurrence_count INT DEFAULT 1 COMMENT '出现次数',
    aliases JSON COMMENT '别名列表',
    historical_names JSON COMMENT '历史名称',
    modern_name VARCHAR(200) COMMENT '现代名称',
    
    INDEX idx_analysis_id (analysis_id),
    INDEX idx_original_name (original_name),
    INDEX idx_standard_name (standard_name),
    INDEX idx_location_type (location_type),
    INDEX idx_province (province),
    INDEX idx_coordinates (latitude, longitude),
    INDEX idx_confidence (confidence),
    
    FOREIGN KEY (analysis_id) REFERENCES analysis_results(id) ON DELETE CASCADE
) ENGINE=InnoDB COMMENT='地理位置表';

-- 系统配置表
CREATE TABLE system_config (
    id INT PRIMARY KEY AUTO_INCREMENT COMMENT 'ID',
    config_key VARCHAR(100) NOT NULL UNIQUE COMMENT '配置键',
    config_value TEXT COMMENT '配置值',
    config_type ENUM('STRING', 'INTEGER', 'BOOLEAN', 'JSON') DEFAULT 'STRING' COMMENT '配置类型',
    description VARCHAR(500) COMMENT '配置描述',
    is_public BOOLEAN DEFAULT FALSE COMMENT '是否公开',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    
    INDEX idx_config_key (config_key),
    INDEX idx_is_public (is_public)
) ENGINE=InnoDB COMMENT='系统配置表';

-- 操作日志表
CREATE TABLE operation_logs (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'ID',
    user_id BIGINT COMMENT '用户ID',
    operation_type VARCHAR(50) NOT NULL COMMENT '操作类型',
    resource_type VARCHAR(50) COMMENT '资源类型',
    resource_id BIGINT COMMENT '资源ID',
    operation_desc TEXT COMMENT '操作描述',
    request_params JSON COMMENT '请求参数',
    response_data JSON COMMENT '响应数据',
    ip_address VARCHAR(45) COMMENT 'IP地址',
    user_agent TEXT COMMENT '用户代理',
    execution_time INT COMMENT '执行时间（毫秒）',
    status ENUM('SUCCESS', 'FAILED') DEFAULT 'SUCCESS' COMMENT '操作状态',
    error_message TEXT COMMENT '错误信息',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    
    INDEX idx_user_id (user_id),
    INDEX idx_operation_type (operation_type),
    INDEX idx_resource_type (resource_type),
    INDEX idx_resource_id (resource_id),
    INDEX idx_status (status),
    INDEX idx_created_at (created_at),
    
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL
) ENGINE=InnoDB COMMENT='操作日志表';

-- 插入系统配置数据
INSERT INTO system_config (config_key, config_value, config_type, description, is_public) VALUES
('system.name', '历史数据统计分析工具', 'STRING', '系统名称', TRUE),
('system.version', '1.0.0', 'STRING', '系统版本', TRUE),
('system.description', '基于NLP技术的历史文献分析工具', 'STRING', '系统描述', TRUE),
('file.max_size', '52428800', 'INTEGER', '文件最大大小（字节）', FALSE),
('file.allowed_types', '["txt", "doc", "docx", "pdf"]', 'JSON', '允许的文件类型', FALSE),
('analysis.max_concurrent', '10', 'INTEGER', '最大并发分析数', FALSE),
('analysis.timeout', '300', 'INTEGER', '分析超时时间（秒）', FALSE),
('cache.enabled', 'true', 'BOOLEAN', '是否启用缓存', FALSE),
('cache.ttl', '3600', 'INTEGER', '缓存过期时间（秒）', FALSE);

-- 创建默认管理员用户
INSERT INTO users (username, email, password_hash, full_name, role, status) VALUES
('admin', 'admin@historyanalysis.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lbdOIGGrAHKtYON.y', '系统管理员', 'ADMIN', 'ACTIVE');

-- 创建示例项目
INSERT INTO projects (name, description, user_id, status) VALUES
('历史文献分析示例', '这是一个历史文献分析的示例项目，用于演示系统功能', 1, 'ACTIVE');

-- 创建视图：用户统计
CREATE VIEW user_statistics AS
SELECT 
    u.id,
    u.username,
    u.full_name,
    u.created_at,
    COUNT(DISTINCT p.id) as project_count,
    COUNT(DISTINCT f.id) as file_count,
    COUNT(DISTINCT ar.id) as analysis_count,
    MAX(u.last_login_at) as last_login_at
FROM users u
LEFT JOIN projects p ON u.id = p.user_id AND p.status = 'ACTIVE'
LEFT JOIN files f ON u.id = f.user_id
LEFT JOIN analysis_results ar ON u.id = ar.user_id
WHERE u.status = 'ACTIVE'
GROUP BY u.id, u.username, u.full_name, u.created_at;

-- 创建视图：项目统计
CREATE VIEW project_statistics AS
SELECT 
    p.id,
    p.name,
    p.user_id,
    u.username,
    p.created_at,
    COUNT(DISTINCT f.id) as file_count,
    COUNT(DISTINCT ar.id) as analysis_count,
    SUM(f.file_size) as total_file_size,
    MAX(ar.created_at) as last_analysis_at
FROM projects p
LEFT JOIN users u ON p.user_id = u.id
LEFT JOIN files f ON p.id = f.project_id
LEFT JOIN analysis_results ar ON p.id = ar.project_id
WHERE p.status = 'ACTIVE'
GROUP BY p.id, p.name, p.user_id, u.username, p.created_at;

-- 创建存储过程：清理过期数据
DELIMITER //
CREATE PROCEDURE CleanExpiredData()
BEGIN
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        RESIGNAL;
    END;
    
    START TRANSACTION;
    
    -- 删除30天前的操作日志
    DELETE FROM operation_logs 
    WHERE created_at < DATE_SUB(NOW(), INTERVAL 30 DAY);
    
    -- 删除已删除项目的相关数据（软删除后7天）
    DELETE f, ar, wfr, te, gl 
    FROM projects p
    LEFT JOIN files f ON p.id = f.project_id
    LEFT JOIN analysis_results ar ON p.id = ar.project_id
    LEFT JOIN word_frequency_results wfr ON ar.id = wfr.analysis_id
    LEFT JOIN timeline_events te ON ar.id = te.analysis_id
    LEFT JOIN geo_locations gl ON ar.id = gl.analysis_id
    WHERE p.status = 'DELETED' 
    AND p.updated_at < DATE_SUB(NOW(), INTERVAL 7 DAY);
    
    -- 删除已删除的项目
    DELETE FROM projects 
    WHERE status = 'DELETED' 
    AND updated_at < DATE_SUB(NOW(), INTERVAL 7 DAY);
    
    COMMIT;
END //
DELIMITER ;

-- 创建触发器：更新项目统计
DELIMITER //
CREATE TRIGGER update_project_file_count 
AFTER INSERT ON files
FOR EACH ROW
BEGIN
    UPDATE projects 
    SET file_count = (
        SELECT COUNT(*) 
        FROM files 
        WHERE project_id = NEW.project_id
    )
    WHERE id = NEW.project_id;
END //

CREATE TRIGGER update_project_analysis_count 
AFTER INSERT ON analysis_results
FOR EACH ROW
BEGIN
    UPDATE projects 
    SET analysis_count = (
        SELECT COUNT(*) 
        FROM analysis_results 
        WHERE project_id = NEW.project_id
    )
    WHERE id = NEW.project_id;
END //
DELIMITER ;