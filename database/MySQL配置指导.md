# MySQL配置指导 - 历史数据统计分析工具

## 当前状态

✅ **项目已成功运行在H2数据库上**
- 后端服务已启动 (端口8080)
- H2内存数据库正常工作
- 前端服务正在运行 (端口5173)

## MySQL连接测试结果

### 测试过程
1. ✅ 找到MySQL安装路径: `D:\programenv\MySQL-8.4.6\bin\mysql.exe`
2. ❌ MySQL服务器启动失败 - 配置问题
3. ✅ 项目已配置MySQL驱动依赖 (mysql-connector-java:8.0.33)

### 发现的问题
- MySQL数据目录初始化失败
- InnoDB配置不兼容当前系统
- 服务器启动时出现断言失败

## 推荐解决方案

### 方案1: 使用Docker MySQL (推荐)
```bash
# 1. 安装Docker Desktop
# 2. 在项目database目录运行:
cd H:\projects\midHis\database
docker-compose up -d mysql

# 3. 测试连接:
docker exec -it history_analysis_mysql mysql -u root -proot123456
```

### 方案2: 重新安装MySQL
```powershell
# 1. 完全卸载现有MySQL
# 2. 下载MySQL 8.0 MSI安装包
# 3. 使用默认配置安装到C盘
# 4. 设置root密码为: root123456
```

### 方案3: 继续使用H2数据库
H2数据库已经正常工作，支持SQL标准，适合开发和测试。

## 切换到MySQL的步骤

### 1. 确保MySQL服务运行
```bash
# 检查MySQL服务状态
Get-Service MySQL* | Select-Object Name, Status

# 或使用Docker
docker ps | grep mysql
```

### 2. 测试MySQL连接
```bash
# 使用项目测试脚本
cd H:\projects\midHis\database
.\simple-mysql-test.ps1
```

### 3. 修改application.yml
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/history_analysis?useSSL=false&serverTimezone=UTC&characterEncoding=utf8
    username: root
    password: root123456
    driver-class-name: com.mysql.cj.jdbc.Driver
  jpa:
    database-platform: org.hibernate.dialect.MySQLDialect
    hibernate:
      ddl-auto: update
```

### 4. 执行数据库初始化
```bash
# 创建数据库和表
mysql -u root -proot123456 < database/init/01_create_database.sql
mysql -u root -proot123456 < database/init/02_create_tables.sql
mysql -u root -proot123456 < database/init/03_insert_test_data.sql
```

### 5. 重启后端服务
```bash
cd H:\projects\midHis\backend
mvn spring-boot:run
```

## 环境配置信息

### 已配置的MySQL连接信息
- **主机**: localhost:3306
- **Root用户**: root / root123456
- **应用用户**: history_user / history_password
- **数据库**: history_analysis

### 项目依赖
- ✅ MySQL驱动: mysql-connector-java:8.0.33
- ✅ H2数据库: 已配置为备用方案
- ✅ Spring Data JPA: 支持多数据库

## 故障排除

### 常见问题
1. **MySQL服务无法启动**
   - 检查端口3306是否被占用
   - 查看MySQL错误日志
   - 重新初始化数据目录

2. **连接被拒绝**
   - 确认MySQL服务正在运行
   - 检查防火墙设置
   - 验证用户名密码

3. **字符编码问题**
   - 确保MySQL配置使用utf8mb4
   - 检查连接URL中的字符编码参数

### 日志位置
- MySQL错误日志: `D:\programenv\MySQL-8.4.6\logs\error.log`
- Spring Boot日志: 控制台输出
- 应用日志: `backend/logs/`

## 总结

当前项目已经在H2数据库上成功运行。如需切换到MySQL，建议使用Docker方案，这样可以避免复杂的MySQL配置问题。

**下一步建议**:
1. 验证当前H2版本的功能完整性
2. 如确需MySQL，优先考虑Docker方案
3. 在生产环境部署时再考虑原生MySQL安装

---
**文档版本**: v1.0  
**创建时间**: 2025-10-31 21:20  
**状态**: 项目正常运行 (H2数据库)