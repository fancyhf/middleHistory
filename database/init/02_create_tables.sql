-- 历史数据统计分析工具表结构创建脚本
-- @author AI Agent
-- @version 1.0.0
-- @created 2024-12-29 18:10:00

USE history_analysis;

-- 用户表
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '用户ID',
    username VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
    email VARCHAR(100) NOT NULL UNIQUE COMMENT '邮箱',
    password_hash VARCHAR(255) NOT NULL COMMENT '密码哈希',
    full_name VARCHAR(100) COMMENT '全名',
    avatar_url VARCHAR(500) COMMENT '头像URL',
    status ENUM('ACTIVE', 'INACTIVE', 'SUSPENDED') DEFAULT 'ACTIVE' COMMENT '用户状态',
    last_login_at TIMESTAMP NULL COMMENT '最后登录时间',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    
    INDEX idx_username (username),
    INDEX idx_email (email),
    INDEX idx_status (status),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- 项目表
CREATE TABLE IF NOT EXISTS projects (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '项目ID',
    name VARCHAR(200) NOT NULL COMMENT '项目名称',
    description TEXT COMMENT '项目描述',
    user_id BIGINT NOT NULL COMMENT '创建用户ID',
    tags JSON COMMENT '项目标签',
    settings JSON COMMENT '项目设置',
    status ENUM('ACTIVE', 'ARCHIVED', 'DELETED') DEFAULT 'ACTIVE' COMMENT '项目状态',
    file_count INT DEFAULT 0 COMMENT '文件数量',
    analysis_count INT DEFAULT 0 COMMENT '分析次数',
    total_file_size BIGINT DEFAULT 0 COMMENT '总文件大小（字节）',
    last_analysis_at TIMESTAMP NULL COMMENT '最后分析时间',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_user_id (user_id),
    INDEX idx_name (name),
    INDEX idx_status (status),
    INDEX idx_created_at (created_at),
    INDEX idx_last_analysis_at (last_analysis_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='项目表';

-- 文件信息表
CREATE TABLE IF NOT EXISTS file_info (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '文件ID',
    project_id BIGINT NOT NULL COMMENT '项目ID',
    original_name VARCHAR(500) NOT NULL COMMENT '原始文件名',
    stored_name VARCHAR(500) NOT NULL COMMENT '存储文件名',
    file_path VARCHAR(1000) NOT NULL COMMENT '文件路径',
    file_type VARCHAR(50) NOT NULL COMMENT '文件类型',
    file_size BIGINT NOT NULL COMMENT '文件大小（字节）',
    md5_hash VARCHAR(32) NOT NULL COMMENT 'MD5哈希值',
    content_preview TEXT COMMENT '内容预览',
    upload_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '上传时间',
    status ENUM('UPLOADED', 'PROCESSING', 'PROCESSED', 'ERROR', 'DELETED') DEFAULT 'UPLOADED' COMMENT '文件状态',
    error_message TEXT COMMENT '错误信息',
    analysis_count INT DEFAULT 0 COMMENT '分析次数',
    last_analysis_at TIMESTAMP NULL COMMENT '最后分析时间',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    
    FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE CASCADE,
    INDEX idx_project_id (project_id),
    INDEX idx_original_name (original_name),
    INDEX idx_file_type (file_type),
    INDEX idx_md5_hash (md5_hash),
    INDEX idx_status (status),
    INDEX idx_upload_time (upload_time),
    INDEX idx_last_analysis_at (last_analysis_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='文件信息表';

-- 分析任务表
CREATE TABLE IF NOT EXISTS analysis_tasks (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '任务ID',
    project_id BIGINT NOT NULL COMMENT '项目ID',
    file_id BIGINT COMMENT '文件ID（可选）',
    task_type ENUM('TEXT_ANALYSIS', 'WORD_FREQUENCY', 'TIMELINE_EXTRACTION', 'GEOGRAPHY_ANALYSIS', 'TEXT_SUMMARY') NOT NULL COMMENT '任务类型',
    task_name VARCHAR(200) NOT NULL COMMENT '任务名称',
    parameters JSON COMMENT '任务参数',
    status ENUM('PENDING', 'RUNNING', 'COMPLETED', 'FAILED', 'CANCELLED') DEFAULT 'PENDING' COMMENT '任务状态',
    progress INT DEFAULT 0 COMMENT '进度百分比',
    result JSON COMMENT '分析结果',
    error_message TEXT COMMENT '错误信息',
    started_at TIMESTAMP NULL COMMENT '开始时间',
    completed_at TIMESTAMP NULL COMMENT '完成时间',
    execution_time INT COMMENT '执行时间（毫秒）',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    
    FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE CASCADE,
    FOREIGN KEY (file_id) REFERENCES file_info(id) ON DELETE SET NULL,
    INDEX idx_project_id (project_id),
    INDEX idx_file_id (file_id),
    INDEX idx_task_type (task_type),
    INDEX idx_status (status),
    INDEX idx_created_at (created_at),
    INDEX idx_completed_at (completed_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='分析任务表';

-- 分析结果表
CREATE TABLE IF NOT EXISTS analysis_results (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '结果ID',
    task_id BIGINT NOT NULL COMMENT '任务ID',
    result_type VARCHAR(50) NOT NULL COMMENT '结果类型',
    result_data JSON NOT NULL COMMENT '结果数据',
    metadata JSON COMMENT '元数据',
    file_size BIGINT COMMENT '结果文件大小',
    cache_key VARCHAR(255) COMMENT '缓存键',
    expires_at TIMESTAMP NULL COMMENT '过期时间',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    
    FOREIGN KEY (task_id) REFERENCES analysis_tasks(id) ON DELETE CASCADE,
    INDEX idx_task_id (task_id),
    INDEX idx_result_type (result_type),
    INDEX idx_cache_key (cache_key),
    INDEX idx_expires_at (expires_at),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='分析结果表';

-- 用户会话表
CREATE TABLE IF NOT EXISTS user_sessions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '会话ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    session_token VARCHAR(255) NOT NULL UNIQUE COMMENT '会话令牌',
    refresh_token VARCHAR(255) COMMENT '刷新令牌',
    ip_address VARCHAR(45) COMMENT 'IP地址',
    user_agent TEXT COMMENT '用户代理',
    expires_at TIMESTAMP NOT NULL COMMENT '过期时间',
    last_activity_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '最后活动时间',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_user_id (user_id),
    INDEX idx_session_token (session_token),
    INDEX idx_expires_at (expires_at),
    INDEX idx_last_activity_at (last_activity_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户会话表';

-- 系统日志表
CREATE TABLE IF NOT EXISTS system_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '日志ID',
    user_id BIGINT COMMENT '用户ID',
    action VARCHAR(100) NOT NULL COMMENT '操作类型',
    resource_type VARCHAR(50) COMMENT '资源类型',
    resource_id BIGINT COMMENT '资源ID',
    details JSON COMMENT '详细信息',
    ip_address VARCHAR(45) COMMENT 'IP地址',
    user_agent TEXT COMMENT '用户代理',
    status ENUM('SUCCESS', 'FAILURE', 'WARNING') DEFAULT 'SUCCESS' COMMENT '状态',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL,
    INDEX idx_user_id (user_id),
    INDEX idx_action (action),
    INDEX idx_resource_type (resource_type),
    INDEX idx_resource_id (resource_id),
    INDEX idx_status (status),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统日志表';

-- 系统配置表
CREATE TABLE IF NOT EXISTS system_config (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '配置ID',
    config_key VARCHAR(100) NOT NULL UNIQUE COMMENT '配置键',
    config_value TEXT COMMENT '配置值',
    config_type ENUM('STRING', 'INTEGER', 'BOOLEAN', 'JSON') DEFAULT 'STRING' COMMENT '配置类型',
    description TEXT COMMENT '配置描述',
    is_public BOOLEAN DEFAULT FALSE COMMENT '是否公开',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    
    INDEX idx_config_key (config_key),
    INDEX idx_is_public (is_public)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统配置表';