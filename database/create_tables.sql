-- 历史数据统计分析工具 - 数据库表创建脚本
-- @author AI Agent
-- @version 1.0.0
-- @created 2024-12-29 21:30:00

USE history_analysis;

-- 1. 用户表
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '用户ID',
    username VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
    email VARCHAR(100) NOT NULL UNIQUE COMMENT '邮箱地址',
    password_hash VARCHAR(255) NOT NULL COMMENT '密码哈希值',
    role ENUM('ADMIN', 'USER') NOT NULL DEFAULT 'USER' COMMENT '用户角色',
    status ENUM('ACTIVE', 'INACTIVE', 'LOCKED', 'DELETED') NOT NULL DEFAULT 'ACTIVE' COMMENT '账户状态',
    real_name VARCHAR(100) COMMENT '真实姓名',
    avatar_url VARCHAR(500) COMMENT '头像URL',
    last_login_at DATETIME COMMENT '最后登录时间',
    last_login_ip VARCHAR(45) COMMENT '最后登录IP',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_username (username),
    INDEX idx_email (email),
    INDEX idx_status (status),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- 2. 项目表
CREATE TABLE IF NOT EXISTS projects (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '项目ID',
    name VARCHAR(200) NOT NULL COMMENT '项目名称',
    description TEXT COMMENT '项目描述',
    status ENUM('ACTIVE', 'INACTIVE', 'ARCHIVED', 'DELETED') NOT NULL DEFAULT 'ACTIVE' COMMENT '项目状态',
    user_id BIGINT NOT NULL COMMENT '项目所有者ID',
    file_count INT NOT NULL DEFAULT 0 COMMENT '文件数量',
    analysis_count INT NOT NULL DEFAULT 0 COMMENT '分析数量',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    UNIQUE KEY uk_name_user (name, user_id),
    INDEX idx_user_id (user_id),
    INDEX idx_status (status),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='项目表';

-- 3. 文件信息表
CREATE TABLE IF NOT EXISTS files (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '文件ID',
    filename VARCHAR(255) NOT NULL COMMENT '文件名',
    original_filename VARCHAR(255) NOT NULL COMMENT '原始文件名',
    file_path VARCHAR(500) NOT NULL COMMENT '文件路径',
    file_type VARCHAR(50) NOT NULL COMMENT '文件类型',
    mime_type VARCHAR(100) COMMENT 'MIME类型',
    file_size BIGINT NOT NULL COMMENT '文件大小（字节）',
    content_hash VARCHAR(64) COMMENT '内容哈希',
    text_content LONGTEXT COMMENT '文本内容',
    text_length INT DEFAULT 0 COMMENT '文本长度',
    encoding VARCHAR(50) COMMENT '文件编码',
    metadata JSON COMMENT '元数据',
    project_id BIGINT NOT NULL COMMENT '所属项目ID',
    user_id BIGINT NOT NULL COMMENT '上传用户ID',
    upload_status ENUM('UPLOADING', 'COMPLETED', 'FAILED', 'DELETED') NOT NULL DEFAULT 'UPLOADING' COMMENT '上传状态',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_project_id (project_id),
    INDEX idx_user_id (user_id),
    INDEX idx_file_type (file_type),
    INDEX idx_upload_status (upload_status),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='文件信息表';

-- 4. 分析结果表
CREATE TABLE IF NOT EXISTS analysis_results (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '分析结果ID',
    analysis_type ENUM('WORD_FREQUENCY', 'TIMELINE_EXTRACTION', 'GEO_LOCATION', 'SENTIMENT_ANALYSIS', 'ENTITY_EXTRACTION') NOT NULL COMMENT '分析类型',
    status ENUM('PENDING', 'PROCESSING', 'COMPLETED', 'FAILED', 'CANCELLED') NOT NULL DEFAULT 'PENDING' COMMENT '分析状态',
    file_ids JSON COMMENT '文件ID列表',
    project_id BIGINT NOT NULL COMMENT '项目ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    parameters JSON COMMENT '分析参数',
    result_data JSON COMMENT '分析结果',
    progress INT DEFAULT 0 COMMENT '分析进度（0-100）',
    error_message TEXT COMMENT '错误信息',
    started_at DATETIME COMMENT '开始时间',
    completed_at DATETIME COMMENT '完成时间',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_project_id (project_id),
    INDEX idx_user_id (user_id),
    INDEX idx_analysis_type (analysis_type),
    INDEX idx_status (status),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='分析结果表';

-- 5. 时间轴事件表
CREATE TABLE IF NOT EXISTS timeline_events (
    id VARCHAR(36) PRIMARY KEY COMMENT '事件ID（UUID）',
    analysis_id BIGINT NOT NULL COMMENT '所属分析结果ID',
    event_name VARCHAR(200) NOT NULL COMMENT '事件名称',
    event_date DATE COMMENT '事件发生日期',
    description TEXT COMMENT '事件描述',
    metadata JSON DEFAULT '{}' COMMENT '事件元数据',
    FOREIGN KEY (analysis_id) REFERENCES analysis_results(id) ON DELETE CASCADE,
    INDEX idx_analysis_id (analysis_id),
    INDEX idx_event_date (event_date),
    INDEX idx_event_name (event_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='时间轴事件表';

-- 6. 地理位置表
CREATE TABLE IF NOT EXISTS geo_locations (
    id VARCHAR(36) PRIMARY KEY COMMENT '位置ID（UUID）',
    analysis_id BIGINT NOT NULL COMMENT '所属分析结果ID',
    location_name VARCHAR(200) NOT NULL COMMENT '位置名称',
    latitude FLOAT COMMENT '纬度',
    longitude FLOAT COMMENT '经度',
    location_type ENUM('CITY', 'PROVINCE', 'COUNTRY', 'MOUNTAIN', 'RIVER', 'LAKE', 'BATTLEFIELD', 'PALACE', 'TEMPLE', 'TOMB', 'BORDER', 'TRADE_ROUTE', 'OTHER') COMMENT '位置类型',
    metadata JSON DEFAULT '{}' COMMENT '位置元数据',
    FOREIGN KEY (analysis_id) REFERENCES analysis_results(id) ON DELETE CASCADE,
    INDEX idx_analysis_id (analysis_id),
    INDEX idx_location_name (location_name),
    INDEX idx_location_type (location_type),
    INDEX idx_coordinates (latitude, longitude)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='地理位置表';

-- 插入测试数据
-- 创建测试用户
INSERT INTO users (username, email, password_hash, role, real_name) VALUES 
('admin', 'admin@example.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iYqiSfFVMLkxNqyNuVdxJOWHNmNe', 'ADMIN', '管理员'),
('testuser', 'test@example.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iYqiSfFVMLkxNqyNuVdxJOWHNmNe', 'USER', '测试用户')
ON DUPLICATE KEY UPDATE username=VALUES(username);

-- 创建测试项目
INSERT INTO projects (name, description, user_id) VALUES 
('历史文档分析项目', '用于分析历史文档的测试项目', 1),
('古代文献研究', '古代文献的词频和时间轴分析', 2)
ON DUPLICATE KEY UPDATE name=VALUES(name);

COMMIT;

-- 显示创建的表
SHOW TABLES;