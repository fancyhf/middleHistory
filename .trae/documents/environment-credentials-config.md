# 环境配置管理文档

**文档版本**: v1.2.0  
**创建时间**: 2025-10-31  
**适用项目**: 历史数据统计分析工具  
**维护者**: AI Agent  
**最后更新**: 2025-10-31

⚠️ **AI Agent 重要提醒**
- 遇到数据库连接问题时，请首先查阅本文档中的MySQL配置信息
- 不要随意创建新的数据库用户或修改密码
- 所有配置信息以本文档为准
- 如需修改配置，必须同步更新本文档

## 文档说明

本文档统一管理历史数据统计分析工具项目中所有已安装和配置的工具和环境的用户名、密码、权限信息。

**重要提醒**: 遇到用户名、密码、端口等配置错误时，必须首先查阅本文档，不得随意创建新用户或修改密码。

## 1. 数据库配置

### 1.1 MySQL 8.4.6

#### 基本配置
- **主机**: localhost
- **端口**: 3306
- **Root用户**: root
- **Root密码**: root123456
- **应用用户**: history_user
- **应用密码**: history_password
- **数据库名**: history_analysis

#### 安装和路径信息
- **安装路径**: D:\programenv\MySQL-8.4.6
- **可执行文件**: D:\programenv\MySQL-8.4.6\bin\mysqld.exe
- **客户端工具**: D:\programenv\MySQL-8.4.6\bin\mysql.exe
- **数据目录**: D:\programenv\MySQL-8.4.6\data
- **配置文件**: D:\programenv\MySQL-8.4.6\my.ini
- **日志文件**: D:\programenv\MySQL-8.4.6\data\*.log
- **错误日志**: D:\programenv\MySQL-8.4.6\data\*.err

#### 服务管理
- **启动命令**: `.\mysqld.exe --no-defaults --basedir="D:\programenv\MySQL-8.4.6" --datadir="D:\programenv\MySQL-8.4.6\data" --port=3306 --console`
- **工作目录**: D:\programenv\MySQL-8.4.6\bin
- **服务状态**: ✅ 运行中
- **进程监控**: 通过命令行启动，可在控制台查看实时日志

#### 数据库配置
- **连接URL**: jdbc:mysql://localhost:3306/history_analysis?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
- **字符集**: utf8mb4
- **排序规则**: utf8mb4_unicode_ci
- **数据库状态**: ✅ history_analysis 数据库已创建
- **用户状态**: ✅ history_user 用户已创建并授权

#### 表结构状态
✅ **已创建完整表结构**（8个核心表）:
- `users` - 用户管理表
- `projects` - 项目信息表
- `files` - 文件管理表
- `analysis_results` - 分析结果表
- `timeline_events` - 时间线事件表
- `geo_locations` - 地理位置表
- `uploaded_files` - 上传文件表
- `word_frequency` - 词频统计表

#### 权限配置
- **root用户**: 全部权限（管理员权限）
- **history_user用户**: 应用专用权限
  - SELECT - 查询数据
  - INSERT - 插入数据
  - UPDATE - 更新数据
  - DELETE - 删除数据
  - CREATE - 创建表
  - DROP - 删除表
  - INDEX - 创建索引
  - ALTER - 修改表结构

#### 连接测试命令
```bash
# 使用root用户连接
mysql -u root -p -h localhost -P 3306

# 使用应用用户连接
mysql -u history_user -p -h localhost -P 3306 history_analysis
```

### 1.2 H2数据库

- **连接URL**: jdbc:h2:mem:testdb
- **用户名**: sa
- **密码**: (空)
- **控制台访问**: http://localhost:8080/h2-console
- **驱动类**: org.h2.Driver
- **使用场景**: 开发测试环境

## 2. 缓存和中间件

### 2.1 Redis (待安装)

- **主机**: localhost
- **端口**: 6379
- **密码**: (待设置，建议: redis_history_2024)
- **安装路径**: H:\开发环境\Redis
- **配置文件**: H:\开发环境\Redis\redis.conf
- **数据目录**: H:\开发环境\Redis\data
- **日志文件**: H:\开发环境\Redis\logs\redis.log
- **最大内存**: 512MB
- **持久化**: RDB + AOF

## 3. 应用服务配置

### 3.1 前端服务 (React)

- **端口**: 3000
- **访问地址**: http://localhost:3000
- **开发服务器**: Vite
- **构建目录**: dist/
- **环境变量文件**: .env.local
- **API基础URL**: http://localhost:8080/api
- **服务状态**: 运行中

### 3.2 后端服务 (Spring Boot)

- **端口**: 8080
- **访问地址**: http://localhost:8080/api
- **上下文路径**: /api
- **配置文件**: application.yml
- **数据库类型**: MySQL 8.4.6 (已从H2切换到MySQL)
- **日志级别**: INFO
- **服务状态**: 运行中
- **健康检查**: http://localhost:8080/api/actuator/health

### 3.3 NLP服务 (Python Flask)

- **端口**: 5001
- **访问地址**: http://localhost:5001
- **Python版本**: 3.12
- **虚拟环境**: venv/
- **主要依赖**: Flask, jieba, scikit-learn
- **服务状态**: 需要启动
- **启动命令**: `python app.py`

## 4. 开发工具配置

### 4.1 Node.js

- **版本**: 20.11.0
- **安装路径**: d:\nvm4w\nodejs
- **可执行文件**: d:\nvm4w\nodejs\node.exe
- **npm版本**: 10.2.4
- **npm全局目录**: H:\npm-global
- **npm缓存目录**: H:\npm-cache
- **管理工具**: NVM for Windows
- **NVM路径**: d:\nvm4w
- **环境变量**:
  - NODE_PATH: d:\nvm4w\nodejs
  - NPM_CONFIG_PREFIX: H:\npm-global

### 4.2 Java JDK

- **版本**: 17
- **安装路径**: H:\开发环境\Java
- **JAVA_HOME**: H:\开发环境\Java
- **可执行文件**: H:\开发环境\Java\bin\java.exe
- **编译器**: H:\开发环境\Java\bin\javac.exe
- **JVM参数**: -Xmx2048m -Xms512m

### 4.3 Maven

- **版本**: 3.9.6
- **安装路径**: H:\开发环境\Maven\apache-maven-3.9.6
- **M2_HOME**: H:\开发环境\Maven\apache-maven-3.9.6
- **本地仓库**: H:\开发环境\Maven\repository
- **配置文件**: H:\开发环境\Maven\apache-maven-3.9.6\conf\settings.xml
- **镜像源**: 阿里云镜像

### 4.4 Python

- **版本**: 3.12
- **安装路径**: C:\Python312
- **可执行文件**: C:\Python312\python.exe
- **pip版本**: 最新
- **虚拟环境工具**: venv
- **包安装目录**: C:\Python312\Lib\site-packages

## 5. 安全配置

### 5.1 JWT配置

- **Secret**: historyAnalysisSecretKey2024ForJWTTokenGeneration
- **算法**: HS256
- **访问令牌过期时间**: 24小时
- **刷新令牌过期时间**: 7天
- **发行者**: history-analysis-system
- **受众**: history-analysis-users

### 5.2 CORS配置

- **允许源**: http://localhost:3000
- **允许方法**: GET, POST, PUT, DELETE, OPTIONS
- **允许头部**: Content-Type, Authorization, X-Requested-With
- **允许凭证**: true
- **预检请求缓存**: 3600秒

## 6. 文件存储配置

### 6.1 文件上传

- **上传路径**: ./uploads/
- **绝对路径**: H:\projects\midHis\backend\uploads\
- **允许类型**: txt, doc, docx, pdf
- **最大文件大小**: 10MB
- **文件命名规则**: timestamp_originalname
- **权限**: 读写权限

### 6.2 日志文件

- **应用日志**: logs/application.log
- **错误日志**: logs/error.log
- **访问日志**: logs/access.log
- **日志级别**: INFO
- **日志轮转**: 每日轮转，保留30天

## 7. 环境变量配置

### 7.1 系统环境变量

```bash
# Java相关
JAVA_HOME=H:\开发环境\Java
M2_HOME=H:\开发环境\Maven\apache-maven-3.9.6

# Node.js相关
NODE_PATH=d:\nvm4w\nodejs
NPM_CONFIG_PREFIX=H:\npm-global

# Python相关
PYTHON_HOME=C:\Python312

# PATH环境变量
PATH=%JAVA_HOME%\bin;%M2_HOME%\bin;%NODE_PATH%;%PYTHON_HOME%;%PYTHON_HOME%\Scripts
```

### 7.2 应用环境变量

```bash
# 数据库配置
DB_HOST=localhost
DB_PORT=3306
DB_NAME=history_analysis
DB_USERNAME=history_user
DB_PASSWORD=history_password

# Redis配置
REDIS_HOST=localhost
REDIS_PORT=6379
REDIS_PASSWORD=redis_history_2024

# JWT配置
JWT_SECRET=historyAnalysisSecretKey2024ForJWTTokenGeneration
JWT_EXPIRATION=86400

# 文件上传配置
UPLOAD_PATH=./uploads/
MAX_FILE_SIZE=10485760
```

## 8. 网络和端口配置

### 8.1 端口分配

| 服务 | 端口 | 协议 | 状态 | 访问地址 |
|------|------|------|------|----------|
| 前端React | 3000 | HTTP | 运行中 | http://localhost:3000 |
| 后端Spring Boot | 8080 | HTTP | 运行中 | http://localhost:8080/api |
| NLP服务Flask | 5001 | HTTP | 待启动 | http://localhost:5001 |
| MySQL数据库 | 3306 | TCP | 运行中 | localhost:3306 |
| Redis缓存 | 6379 | TCP | 待安装 | localhost:6379 |
| H2控制台 | 8080 | HTTP | 可用 | http://localhost:8080/h2-console |

### 8.2 防火墙配置

- **入站规则**: 允许端口 3000, 8080, 5001, 3306, 6379
- **出站规则**: 允许HTTP/HTTPS访问
- **本地访问**: 允许localhost访问所有服务

## 9. 备份和恢复

### 9.1 数据库备份

- **MySQL备份脚本**: scripts/backup_mysql.sql
- **备份频率**: 每日自动备份
- **备份路径**: H:\projects\midHis\database\backups\
- **保留策略**: 保留最近30天的备份

### 9.2 配置文件备份

- **应用配置**: backend/src/main/resources/application.yml
- **Maven配置**: H:\开发环境\Maven\apache-maven-3.9.6\conf\settings.xml
- **环境变量**: 记录在本文档中

## 10. 故障排除

### 10.1 常见问题

1. **数据库连接失败**
   - 检查MySQL服务是否运行
   - 验证用户名密码: history_user / history_password
   - 确认端口3306是否开放

2. **端口占用问题**
   - 使用 `netstat -ano | findstr :端口号` 检查端口占用
   - 终止占用进程或更换端口

3. **权限问题**
   - 确认文件夹权限设置
   - 检查用户是否有相应的数据库权限

### 10.2 联系信息

- **技术支持**: AI Agent
- **文档维护**: 开发团队
- **紧急联系**: 项目负责人

## 11. 当前系统架构

```
前端 (React + Vite)     后端 (Spring Boot)     数据库 (MySQL 8.4.6)
http://localhost:3000 → http://localhost:8080 → localhost:3306/history_analysis
```

**架构说明**:
- 前端使用React + Vite开发服务器，运行在3000端口
- 后端使用Spring Boot框架，提供REST API服务，运行在8080端口
- 数据库使用MySQL 8.4.6，运行在3306端口，数据库名为history_analysis
- 前后端通过HTTP协议通信，后端通过JDBC连接MySQL数据库

## 12. 更新记录

| 版本 | 日期 | 更新内容 | 更新人 |
|------|------|----------|--------|
| v1.0.0 | 2025-10-31 | 初始版本，包含所有环境配置信息 | AI Agent |
| v1.1.0 | 2025-10-31 | 成功将项目数据库从H2切换到MySQL 8.4.6，创建了完整的表结构和用户权限 | AI Agent |
| v1.2.0 | 2025-10-31 | 修正和完善MySQL配置信息，添加详细的服务管理信息和AI Agent使用指南 | AI Agent |

---

**注意事项**:
1. 本文档包含敏感信息，请妥善保管
2. 密码定期更新，更新后及时同步本文档
3. 新增服务或修改配置时，必须更新本文档
4. 遇到配置问题时，优先查阅本文档而非重新配置