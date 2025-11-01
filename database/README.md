# 数据库初始化指南

## 概述

本目录包含历史数据统计分析工具的数据库初始化脚本和配置文件。

## 文件结构

```
database/
├── init/
│   ├── 01_create_database.sql    # 创建数据库和用户
│   ├── 02_create_tables.sql      # 创建表结构
│   └── 03_insert_test_data.sql   # 插入测试数据
├── redis/
│   └── redis_config.conf         # Redis配置文件
└── README.md                     # 本文件
```

## MySQL数据库初始化

### 前置条件

1. 安装MySQL 8.0或更高版本
2. 确保MySQL服务正在运行
3. 具有创建数据库和用户的权限

### 初始化步骤

#### 方法一：使用MySQL命令行工具

```bash
# 1. 创建数据库和用户
mysql -u root -p < init/01_create_database.sql

# 2. 创建表结构
mysql -u root -p < init/02_create_tables.sql

# 3. 插入测试数据
mysql -u root -p < init/03_insert_test_data.sql
```

#### 方法二：使用MySQL Workbench

1. 打开MySQL Workbench
2. 连接到MySQL服务器
3. 依次执行以下SQL文件：
   - `init/01_create_database.sql`
   - `init/02_create_tables.sql`
   - `init/03_insert_test_data.sql`

#### 方法三：使用phpMyAdmin

1. 登录phpMyAdmin
2. 点击"SQL"选项卡
3. 复制并执行每个SQL文件的内容

### 数据库配置

创建完成后，请更新Spring Boot应用的配置文件：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/history_analysis?useUnicode=true&characterEncoding=utf8mb4&useSSL=false&serverTimezone=Asia/Shanghai
    username: history_user
    password: history_password
    driver-class-name: com.mysql.cj.jdbc.Driver
```

## Redis配置

### 安装Redis

#### Windows
1. 下载Redis for Windows
2. 解压到指定目录
3. 使用提供的配置文件启动Redis

#### Linux/macOS
```bash
# 安装Redis
sudo apt-get install redis-server  # Ubuntu/Debian
brew install redis                 # macOS

# 使用自定义配置启动
redis-server redis/redis_config.conf
```

### Redis配置说明

配置文件 `redis/redis_config.conf` 包含以下主要设置：

- **端口**: 6379（默认）
- **内存限制**: 256MB
- **持久化**: 启用RDB和AOF
- **过期策略**: allkeys-lru
- **慢查询日志**: 启用

## 数据库表结构说明

### 核心表

1. **users** - 用户表
   - 存储用户基本信息、认证信息
   - 支持用户状态管理

2. **projects** - 项目表
   - 项目基本信息和设置
   - 支持标签和自定义配置

3. **file_info** - 文件信息表
   - 文件上传和存储信息
   - 文件状态跟踪

4. **analysis_tasks** - 分析任务表
   - 分析任务的生命周期管理
   - 支持多种分析类型

5. **analysis_results** - 分析结果表
   - 存储分析结果数据
   - 支持结果缓存

### 辅助表

6. **user_sessions** - 用户会话表
   - JWT令牌管理
   - 会话安全跟踪

7. **system_logs** - 系统日志表
   - 用户操作审计
   - 系统事件记录

8. **system_config** - 系统配置表
   - 动态配置管理
   - 支持不同数据类型

## 测试数据说明

测试数据包含：

- 3个测试用户（admin, historian1, researcher1）
- 3个示例项目（明清史料分析、古代地理研究、史记文本研究）
- 6个测试文件
- 6个分析任务示例
- 4个分析结果示例
- 系统配置项

## 性能优化

### 索引策略

- 主键使用自增BIGINT
- 外键字段建立索引
- 查询频繁的字段建立复合索引
- 时间字段建立索引支持范围查询

### 分区策略

对于大数据量表，可考虑按时间分区：

```sql
-- 示例：按月分区system_logs表
ALTER TABLE system_logs PARTITION BY RANGE (YEAR(created_at) * 100 + MONTH(created_at)) (
    PARTITION p202412 VALUES LESS THAN (202501),
    PARTITION p202501 VALUES LESS THAN (202502),
    -- 更多分区...
);
```

## 备份和恢复

### 数据备份

```bash
# 完整备份
mysqldump -u history_user -p history_analysis > backup_$(date +%Y%m%d).sql

# 仅结构备份
mysqldump -u history_user -p --no-data history_analysis > structure_backup.sql

# 仅数据备份
mysqldump -u history_user -p --no-create-info history_analysis > data_backup.sql
```

### 数据恢复

```bash
# 恢复数据库
mysql -u history_user -p history_analysis < backup_20241229.sql
```

## 监控和维护

### 性能监控

- 定期检查慢查询日志
- 监控表大小和索引使用情况
- 检查连接池状态

### 定期维护

- 清理过期的分析结果
- 归档历史日志数据
- 优化表结构和索引

## 故障排除

### 常见问题

1. **连接失败**
   - 检查MySQL服务状态
   - 验证用户名密码
   - 确认网络连接

2. **权限错误**
   - 检查用户权限设置
   - 验证数据库访问权限

3. **性能问题**
   - 检查索引使用情况
   - 分析慢查询日志
   - 考虑增加内存配置

## 联系信息

如有问题，请联系开发团队或查看项目文档。