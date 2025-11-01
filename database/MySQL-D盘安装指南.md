# MySQL D盘安装指南 - 历史数据统计分析工具

**Author**: AI Agent  
**Version**: 1.0.0  
**Created**: 2025-01-25 22:15:00  
**适用项目**: 历史数据统计分析工具

## 1. 安装前准备

### 1.1 系统要求
- Windows 10/11 64位系统
- 至少2GB可用磁盘空间（D盘）
- 管理员权限

### 1.2 下载MySQL安装包
从阿里云镜像站下载MySQL 8.0最新版本：
- 镜像地址：https://mirrors.aliyun.com/mysql/
- 选择：MySQL Community Server 8.0.x
- 文件类型：Windows (x86, 64-bit), MSI Installer

## 2. 安装步骤

### 2.1 创建安装目录
```powershell
# 创建MySQL安装目录
New-Item -ItemType Directory -Path "D:\programenv\MySQL" -Force
New-Item -ItemType Directory -Path "D:\programenv\MySQL\data" -Force
New-Item -ItemType Directory -Path "D:\programenv\MySQL\logs" -Force
New-Item -ItemType Directory -Path "D:\programenv\MySQL\tmp" -Force
```

### 2.2 运行MSI安装程序
1. 双击下载的MSI文件
2. 选择"Custom"自定义安装
3. 设置安装路径为：`D:\programenv\MySQL`
4. 选择以下组件：
   - MySQL Server 8.0.x
   - MySQL Workbench
   - MySQL Shell
   - Connector/J (Java连接器)

### 2.3 配置MySQL服务
1. **服务器配置类型**：Development Computer
2. **连接设置**：
   - Port: 3306
   - X Protocol Port: 33060
3. **认证方法**：Use Strong Password Encryption
4. **账户和角色**：
   - Root密码：`root123456`
   - 创建用户账户：`history_user` / `history_password`
5. **Windows服务**：
   - 服务名：MySQL80
   - 启动类型：Automatic
   - 以系统账户运行

## 3. 配置文件设置

### 3.1 复制配置文件
```powershell
# 复制项目配置文件到MySQL安装目录
Copy-Item "H:\projects\midHis\database\mysql-d-drive-config.ini" "D:\programenv\MySQL\my.ini"
```

### 3.2 重启MySQL服务
```powershell
# 重启MySQL服务以应用新配置
Restart-Service MySQL80
```

## 4. 数据库初始化

### 4.1 连接MySQL
```powershell
# 使用MySQL命令行客户端连接
mysql -u root -p
# 输入密码：root123456
```

### 4.2 执行初始化脚本
```sql
-- 1. 创建数据库和用户
SOURCE H:/projects/midHis/database/init/01_create_database.sql;

-- 2. 创建表结构
USE history_analysis;
SOURCE H:/projects/midHis/database/init/02_create_tables.sql;

-- 3. 插入测试数据
SOURCE H:/projects/midHis/database/init/03_insert_test_data.sql;

-- 4. 更新表结构
SOURCE H:/projects/midHis/database/init/04_update_tables.sql;
```

### 4.3 验证安装
```sql
-- 检查数据库
SHOW DATABASES;

-- 检查表结构
USE history_analysis;
SHOW TABLES;

-- 检查测试数据
SELECT COUNT(*) FROM history_documents;
SELECT COUNT(*) FROM word_frequency_results;
```

## 5. 项目配置更新

### 5.1 更新Spring Boot配置
编辑 `backend/src/main/resources/application.yml`：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/history_analysis?useUnicode=true&characterEncoding=utf8mb4&useSSL=false&serverTimezone=Asia/Shanghai
    username: history_user
    password: history_password
    driver-class-name: com.mysql.cj.jdbc.Driver
    hikari:
      maximum-pool-size: 20
      minimum-idle: 5
      idle-timeout: 300000
      connection-timeout: 20000
      
  jpa:
    hibernate:
      ddl-auto: validate
    show-sql: true
    properties:
      hibernate:
        dialect: org.hibernate.dialect.MySQL8Dialect
        format_sql: true
```

### 5.2 添加MySQL依赖
确保 `backend/pom.xml` 包含MySQL连接器：

```xml
<dependency>
    <groupId>mysql</groupId>
    <artifactId>mysql-connector-java</artifactId>
    <scope>runtime</scope>
</dependency>
```

## 6. 验证和测试

### 6.1 服务状态检查
```powershell
# 检查MySQL服务状态
Get-Service MySQL80

# 检查端口监听
netstat -an | findstr :3306
```

### 6.2 连接测试
```powershell
# 测试数据库连接
mysql -u history_user -p -h localhost -P 3306 history_analysis
```

### 6.3 应用程序测试
```powershell
# 启动Spring Boot应用
cd H:\projects\midHis\backend
mvn spring-boot:run
```

## 7. 故障排除

### 7.1 常见问题
1. **服务启动失败**：
   - 检查配置文件语法
   - 查看错误日志：`D:\programenv\MySQL\logs\error.log`
   - 确保端口3306未被占用

2. **连接被拒绝**：
   - 检查防火墙设置
   - 验证用户权限
   - 确认服务正在运行

3. **字符集问题**：
   - 确保配置文件中字符集设置正确
   - 检查数据库和表的字符集

### 7.2 日志文件位置
- 错误日志：`D:\programenv\MySQL\logs\error.log`
- 慢查询日志：`D:\programenv\MySQL\logs\slow.log`
- 二进制日志：`D:\programenv\MySQL\logs\mysql-bin.*`

## 8. 备份和维护

### 8.1 数据备份
```powershell
# 创建数据库备份
mysqldump -u root -p history_analysis > D:\programenv\MySQL\backup\history_analysis_backup.sql
```

### 8.2 定期维护
- 定期清理日志文件
- 监控磁盘空间使用
- 更新MySQL版本

## 9. 安全建议

1. **密码安全**：
   - 使用强密码
   - 定期更换密码
   - 限制root用户远程访问

2. **网络安全**：
   - 配置防火墙规则
   - 使用SSL连接（生产环境）
   - 限制访问IP地址

3. **文件权限**：
   - 设置适当的文件和目录权限
   - 定期检查配置文件安全性

---

**注意事项**：
- 严格遵守D盘安装要求，不得安装到C盘
- 安装过程中如遇到网络问题，可尝试使用离线安装包
- 建议在安装前创建系统还原点
- 生产环境部署时需要额外的安全配置