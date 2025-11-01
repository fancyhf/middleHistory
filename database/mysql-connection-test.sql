-- MySQL连接和功能测试脚本
-- @author AI Agent
-- @version 1.0.0
-- @created 2025-10-31

-- 显示MySQL版本
SELECT VERSION() as mysql_version;

-- 显示当前数据库
SELECT DATABASE() as current_database;

-- 显示字符集设置
SHOW VARIABLES LIKE 'character_set%';

-- 显示所有数据库
SHOW DATABASES;

-- 使用history_analysis数据库
USE history_analysis;

-- 显示所有表
SHOW TABLES;

-- 检查用户权限
SHOW GRANTS FOR 'history_user'@'localhost';

-- 测试表结构
DESCRIBE users;
DESCRIBE projects;
DESCRIBE file_info;

-- 测试插入和查询
INSERT INTO users (username, email, password_hash, full_name) 
VALUES ('test_user', 'test@example.com', 'hashed_password', 'Test User');

SELECT * FROM users WHERE username = 'test_user';

-- 清理测试数据
DELETE FROM users WHERE username = 'test_user';

-- 显示表统计信息
SELECT 
    table_name,
    table_rows,
    data_length,
    index_length,
    (data_length + index_length) as total_size
FROM information_schema.tables 
WHERE table_schema = 'history_analysis';

SELECT 'MySQL安装和配置测试完成！' as test_result;