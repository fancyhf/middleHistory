# 历史数据统计分析工具 - 开发环境配置统计

**文档版本**: v1.0.0  
**创建时间**: 2025-10-31  
**适用项目**: 历史数据统计分析工具  
**维护者**: AI Agent  
**文档类型**: 开发环境配置统计  

## 1. 环境概览

本文档记录历史数据统计分析工具项目的完整开发环境配置，包括所有已安装的开发工具、运行时环境、数据库配置和服务端口分配。

**⚠️ 重要提醒**: 需要启动项目时，请参考 **AI Agent项目启动指南** 文档 (`AI-Agent-项目启动指南.md`)，该文档提供了详细的启动步骤和故障排除方法。

## 2. 已安装开发环境

### 2.1 核心运行时环境

| 环境名称 | 版本 | 安装路径 | 状态 | 备注 |
|---------|------|----------|------|------|
| Node.js | 20.11.0 | d:\nvm4w\nodejs | ✅ 已安装 | 通过NVM管理，可执行文件: d:\nvm4w\nodejs\node.exe |
| Java JDK | 17 | H:\开发环境\Java | ✅ 已安装 | Spring Boot后端 |
| Maven | 3.9.6 | H:\开发环境\Maven\apache-maven-3.9.6 | ✅ 已安装 | Java构建工具 |
| Python | 3.12 | C:\Python312 | ✅ 已安装 | NLP服务运行时 |

### 2.2 数据库和缓存环境

| 数据库 | 版本 | 安装路径 | 端口 | 状态 |
|--------|------|----------|------|------|
| MySQL | 8.4.6 | D:\programenv\MySQL-8.4.6 | 3306 | ✅ 运行中 |
| H2 Database | 内嵌 | 项目内存 | 8080/h2-console | ✅ 可用 |
| Redis | 7.0+ | H:\开发环境\Redis | 6379 | ❌ 需要安装 |

**MySQL详细状态**:
- **配置文件**: D:\programenv\MySQL-8.4.6\my-8.4.ini
- **数据库**: history_analysis
- **用户**: history_user / history_password
- **Root用户**: root / root123456
- **启动命令**: `& "D:\programenv\MySQL-8.4.6\bin\mysqld.exe" --defaults-file="D:\programenv\MySQL-8.4.6\my-8.4.ini" --console`

### 2.3 Node.js详细配置

| 配置项 | 路径/值 | 说明 |
|--------|---------|------|
| Node.js可执行文件 | d:\nvm4w\nodejs\node.exe | 主程序文件 |
| Node.js版本 | v20.11.0 | 当前使用版本 |
| npm全局安装目录 | H:\npm-global | 全局包安装位置 |
| 版本管理工具 | NVM for Windows | 支持多版本切换 |

## 3. 服务端口配置

### 3.1 应用服务端口

| 服务名称 | 端口 | 访问地址 | 状态 | 命令ID |
|---------|------|----------|------|--------|
| 前端服务 (React) | 3000 | http://localhost:3000 | ✅ 运行中 | 84edebc6-61e2-4c3f-8a79-583dae51b401 |
| 后端服务 (Spring Boot) | 8080 | http://localhost:8080/api | ✅ 运行中 | c3e7fcb1-d62a-401c-b6cf-ed6edd66bc59 |
| NLP服务 (Python Flask) | 5001 | http://localhost:5001 | ⚠️ 需要启动 | - |
| H2控制台 | 8080 | http://localhost:8080/h2-console | ✅ 可用 | - |

### 3.2 数据库端口

| 数据库 | 端口 | 连接地址 | 用户名 | 状态 | 命令ID |
|--------|------|----------|--------|------|--------|
| MySQL | 3306 | localhost:3306 | root | ✅ 运行中 | f68c7e79-bfb1-471e-9510-cd7fbb42f2e5 |
| H2 | 内嵌 | mem:testdb | sa | ✅ 可用 | - |

## 4. 环境变量配置

### 4.1 系统环境变量

```bash
# Java环境
JAVA_HOME=H:\开发环境\Java\jdk-17
PATH=%PATH%;%JAVA_HOME%\bin

# Maven环境  
MAVEN_HOME=H:\开发环境\Maven\apache-maven-3.9.6
PATH=%PATH%;%MAVEN_HOME%\bin

# Node.js环境 (通过NVM管理)
NODE_PATH=d:\nvm4w\nodejs
PATH=%PATH%;d:\nvm4w\nodejs
NPM_CONFIG_PREFIX=H:\npm-global

# Python环境
PYTHON_HOME=C:\Python312
PATH=%PATH%;%PYTHON_HOME%;%PYTHON_HOME%\Scripts

# Redis环境 (待安装)
REDIS_HOME=H:\开发环境\Redis
PATH=%PATH%;%REDIS_HOME%
```

### 4.2 项目特定配置

```yaml
# application.yml (后端配置)
server:
  port: 8080
  servlet:
    context-path: /api

spring:
  datasource:
    # H2数据库配置
    url: jdbc:h2:mem:testdb
    username: sa
    password: 
    
  # MySQL配置 (备用)
  # url: jdbc:mysql://localhost:3306/history_analysis
  # username: root
  # password: root123456
  
  # Redis配置
  redis:
    host: localhost
    port: 6379
    database: 0
    timeout: 5000ms
    jedis:
      pool:
        max-active: 8
        max-wait: -1ms
        max-idle: 8
        min-idle: 0
```

## 5. 数据库配置详情

### 5.1 MySQL配置

- **版本**: 8.4.6
- **安装路径**: D:\programenv\MySQL-8.4.6
- **配置文件**: D:\programenv\MySQL-8.4.6\my-8.4.ini
- **数据目录**: D:\programenv\MySQL-8.4.6\data
- **端口**: 3306
- **字符集**: utf8mb4
- **排序规则**: utf8mb4_unicode_ci
- **Root用户**: root / root123456
- **项目用户**: history_user / history_password
- **项目数据库**: history_analysis
- **当前状态**: ✅ 运行中

### 5.2 H2数据库配置

- **类型**: 内存数据库
- **连接URL**: jdbc:h2:mem:testdb
- **用户名**: sa
- **密码**: (空)
- **控制台**: http://localhost:8080/h2-console

## 6. 启动命令记录

### 6.1 当前运行的后台命令

```powershell
# MySQL服务 (使用配置文件启动)
& "D:\programenv\MySQL-8.4.6\bin\mysqld.exe" --defaults-file="D:\programenv\MySQL-8.4.6\my-8.4.ini" --console
# 命令ID: 8deff531-6657-4af7-a461-ce733b4e46fe

# 后端Spring Boot服务 (使用MySQL配置)
$env:MAVEN_HOME = "H:\开发环境\Maven\apache-maven-3.9.6"
$env:PATH = "$env:MAVEN_HOME\bin;$env:PATH"
mvn spring-boot:run -Dspring.profiles.active=mysql
# 命令ID: fe07df15-8857-4938-86a5-4c40e9d6e24c

# 前端React服务
npm run dev
# 命令ID: cc13bd76-bcf3-4de8-8dfc-d3ffbe78b45c

# NLP Python服务
python app.py
# 命令ID: fbd8d42f-6bd6-461e-bf47-64cb9bafdb69
```

**启动顺序说明**: 
1. 首先启动MySQL数据库服务
2. 然后启动NLP服务 (后端依赖)
3. 接着启动Spring Boot后端服务
4. 最后启动React前端服务

**详细启动指南**: 请参考 `AI-Agent-项目启动指南.md` 文档

## 7. 项目结构

```
h:\projects\midHis\
├── backend/                 # Spring Boot后端
│   ├── src/main/java/      # Java源码
│   ├── src/main/resources/ # 配置文件
│   └── pom.xml            # Maven配置
├── frontend/               # React前端
│   ├── src/               # React源码
│   ├── public/            # 静态资源
│   └── package.json       # npm配置
├── nlp-service/           # Python NLP服务
│   ├── app.py            # Flask应用
│   ├── config.py         # 配置文件
│   └── requirements.txt  # Python依赖
└── .trae/                # 项目元数据
    ├── documents/        # 文档目录
    └── rules/           # 项目规则
```

## 8. 安装路径约束遵循情况

### 8.1 已遵循约束的安装

✅ **Java JDK**: H:\开发环境\Java  
✅ **Maven**: H:\开发环境\Maven\apache-maven-3.9.6  
✅ **MySQL**: D:\programenv\MySQL-8.4.6  
✅ **Node.js**: d:\nvm4w\nodejs (通过NVM管理)  
✅ **npm全局目录**: H:\npm-global  

### 8.2 部分遵循约束的安装

⚠️ **Python 3.12**: C:\Python312 (建议迁移到 H:\开发环境\Python)  

### 8.3 待安装组件

❌ **Redis 7.0+**: 需要安装到 H:\开发环境\Redis  

## 9. 性能和监控

### 9.1 服务状态监控

- **前端服务**: 正常运行，端口3000可访问
- **后端服务**: 正常运行，API端点/api可访问  
- **NLP服务**: 正常运行，端口5001可访问
- **MySQL服务**: 正常运行，端口3306可连接

### 9.2 资源使用情况

- **内存使用**: 各服务运行正常
- **端口占用**: 无冲突
- **磁盘空间**: 安装路径符合约束

## 10. 故障排除

### 10.1 常见问题

1. **端口冲突**: 检查端口占用情况
2. **环境变量**: 确认PATH配置正确
3. **权限问题**: 确保对H:\开发环境有读写权限
4. **网络连接**: 检查localhost访问

### 10.2 日志位置

- **后端日志**: 控制台输出
- **前端日志**: 浏览器开发者工具
- **NLP服务日志**: 控制台输出
- **MySQL日志**: D:\programenv\MySQL-8.4.6\data\*.err

## 11. Redis配置详情

### 11.1 Redis系统要求

Redis是系统必需组件，用于以下功能：
- **缓存分析结果**: 提高数据查询性能
- **用户会话管理**: 存储用户登录状态
- **临时数据存储**: 缓存中间计算结果
- **性能优化**: 减少数据库访问压力

### 11.2 Redis配置文件

- **配置文件位置**: database/redis-config.conf
- **内存限制**: 256MB
- **持久化**: RDB + AOF
- **安全设置**: 密码保护

### 11.3 Redis安装要求

- **推荐版本**: Redis 7.0+
- **安装路径**: H:\开发环境\Redis
- **端口**: 6379 (默认)
- **启动方式**: Windows服务或命令行

## 12. 下一步计划

1. **安装Redis服务**到H:\开发环境\Redis
2. **配置Redis开机自启**
3. **启动NLP服务**
4. **验证所有服务连通性**
5. **考虑迁移Python**到H:\开发环境\Python (可选)
6. **配置生产环境**数据库连接
7. **完善监控和日志**系统
8. **添加自动化部署**脚本

---

**最后更新**: 2025-10-31  
**文档状态**: 当前有效  
**维护周期**: 每周更新