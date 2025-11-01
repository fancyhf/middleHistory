-- 历史数据统计分析工具数据库初始化脚本
-- @author AI Agent
-- @version 1.0.0
-- @created 2024-12-29 18:10:00

-- 创建数据库
CREATE DATABASE IF NOT EXISTS history_analysis 
CHARACTER SET utf8mb4 
COLLATE utf8mb4_unicode_ci;

-- 创建用户并授权
CREATE USER IF NOT EXISTS 'history_user'@'localhost' IDENTIFIED BY 'history_password';
GRANT ALL PRIVILEGES ON history_analysis.* TO 'history_user'@'localhost';
FLUSH PRIVILEGES;

-- 使用数据库
USE history_analysis;

-- 设置时区
SET time_zone = '+08:00';