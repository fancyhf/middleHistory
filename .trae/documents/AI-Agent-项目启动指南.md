# AI Agent 项目启动指南

**文档版本**: v1.0.0  
**创建时间**: 2025-01-27  
**适用项目**: 历史数据统计分析工具  
**目标用户**: AI Agent  

## 概述

本文档专门为AI Agent提供项目启动的详细指导，包含所有必要的路径、命令和配置信息。

## 1. 项目结构

```
H:\projects\midHis\
├── frontend/          # React前端项目
├── backend/           # Spring Boot后端项目
├── nlp-service/       # Python NLP服务
├── .trae/documents/   # 项目文档
└── scripts/           # 启动脚本
```

## 2. 环境要求检查

### 2.1 必需环境
- **Node.js**: 20.11.0 (安装路径: `d:\nvm4w\nodejs`)
- **Java JDK**: 17 (安装路径: `H:\开发环境\Java`)
- **Maven**: 3.9.6 (安装路径: `H:\开发环境\Maven\apache-maven-3.9.6`)
- **Python**: 3.12 (安装路径: `C:\Python312`)
- **MySQL**: 8.4.6 (安装路径: `D:\programenv\MySQL-8.4.6`)

### 2.2 环境变量检查
```powershell
# 检查环境变量
echo $env:JAVA_HOME
echo $env:MAVEN_HOME
echo $env:PATH
```

## 3. MySQL 数据库启动

### 3.1 MySQL 安装位置
- **安装路径**: `D:\programenv\MySQL-8.4.6`
- **配置文件**: `D:\programenv\MySQL-8.4.6\my-8.4.ini`
- **可执行文件**: `D:\programenv\MySQL-8.4.6\bin\mysqld.exe`

### 3.2 MySQL 启动方法

#### 方法1: 直接启动 (推荐)
```powershell
# 切换到项目目录
cd H:\projects\midHis

# 启动MySQL服务器
& "D:\programenv\MySQL-8.4.6\bin\mysqld.exe" --defaults-file="D:\programenv\MySQL-8.4.6\my-8.4.ini" --console
```

#### 方法2: 检查并启动Windows服务
```powershell
# 检查MySQL服务状态
Get-Service -Name "MySQL*" | Format-Table -AutoSize

# 如果服务存在但未运行，启动服务
Start-Service -Name "MySQL80"  # 或其他MySQL服务名
```

### 3.3 MySQL 连接信息
- **主机**: localhost
- **端口**: 3306
- **数据库**: history_analysis
- **用户名**: history_user
- **密码**: history_password
- **Root密码**: root123456

### 3.4 验证MySQL运行状态
```powershell
# 检查端口占用
netstat -an | findstr :3306

# 测试连接
& "D:\programenv\MySQL-8.4.6\bin\mysql.exe" -u history_user -phistory_password -h localhost -P 3306 -D history_analysis -e "SELECT 1;"
```

## 4. Maven 配置和使用

### 4.1 Maven 位置
- **安装路径**: `H:\开发环境\Maven\apache-maven-3.9.6`
- **可执行文件**: `H:\开发环境\Maven\apache-maven-3.9.6\bin\mvn.cmd`

### 4.2 设置Maven环境变量
```powershell
# 设置Maven环境变量
$env:MAVEN_HOME = "H:\开发环境\Maven\apache-maven-3.9.6"
$env:PATH = "$env:MAVEN_HOME\bin;$env:PATH"

# 验证Maven安装
mvn -version
```

## 5. 后端服务启动 (Spring Boot)

### 5.1 启动前准备
```powershell
# 1. 确保MySQL已启动并运行在端口3306
# 2. 设置Maven环境变量
$env:MAVEN_HOME = "H:\开发环境\Maven\apache-maven-3.9.6"
$env:PATH = "$env:MAVEN_HOME\bin;$env:PATH"

# 3. 切换到后端目录
cd H:\projects\midHis\backend
```

### 5.2 启动Spring Boot应用
```powershell
# 使用MySQL配置启动
mvn spring-boot:run -Dspring.profiles.active=mysql
```

### 5.3 验证后端启动
- **访问地址**: http://localhost:8080
- **健康检查**: http://localhost:8080/actuator/health
- **API文档**: http://localhost:8080/swagger-ui.html

### 5.4 常见问题处理
```powershell
# 如果Maven命令不可用
$env:MAVEN_HOME = "H:\开发环境\Maven\apache-maven-3.9.6"
$env:PATH = "$env:MAVEN_HOME\bin;$env:PATH"

# 如果数据库连接失败，检查MySQL状态
netstat -an | findstr :3306

# 清理并重新编译
mvn clean compile
```

## 6. 前端服务启动 (React)

### 6.1 启动前准备
```powershell
# 切换到前端目录
cd H:\projects\midHis\frontend

# 检查Node.js版本
node --version  # 应该显示 v20.11.0
npm --version
```

### 6.2 启动前端开发服务器
```powershell
# 启动开发服务器
npm run dev
```

### 6.3 验证前端启动
- **访问地址**: http://localhost:3000
- **开发工具**: 浏览器开发者工具可查看控制台日志

## 7. NLP 服务启动 (Python)

### 7.1 启动前准备
```powershell
# 切换到NLP服务目录
cd H:\projects\midHis\nlp-service

# 检查Python版本
python --version  # 应该显示 Python 3.12.x
```

### 7.2 启动NLP服务
```powershell
# 启动Flask应用
python app.py
```

### 7.3 验证NLP服务启动
- **访问地址**: http://localhost:5001
- **健康检查**: http://localhost:5001/api/health

## 8. 完整启动流程

### 8.1 按顺序启动所有服务

#### 步骤1: 启动MySQL
```powershell
# 新建终端1 - 启动MySQL
cd H:\projects\midHis
& "D:\programenv\MySQL-8.4.6\bin\mysqld.exe" --defaults-file="D:\programenv\MySQL-8.4.6\my-8.4.ini" --console
```

#### 步骤2: 启动NLP服务
```powershell
# 新建终端2 - 启动NLP服务
cd H:\projects\midHis\nlp-service
python app.py
```

#### 步骤3: 启动后端服务
```powershell
# 新建终端3 - 启动后端
cd H:\projects\midHis\backend
$env:MAVEN_HOME = "H:\开发环境\Maven\apache-maven-3.9.6"
$env:PATH = "$env:MAVEN_HOME\bin;$env:PATH"
mvn spring-boot:run -Dspring.profiles.active=mysql
```

#### 步骤4: 启动前端服务
```powershell
# 新建终端4 - 启动前端
cd H:\projects\midHis\frontend
npm run dev
```

### 8.2 验证所有服务
```powershell
# 检查所有端口
netstat -an | findstr "3000 3306 5001 8080"

# 应该看到:
# TCP    0.0.0.0:3000           0.0.0.0:0              LISTENING  # 前端
# TCP    0.0.0.0:3306           0.0.0.0:0              LISTENING  # MySQL
# TCP    0.0.0.0:5001           0.0.0.0:0              LISTENING  # NLP服务
# TCP    0.0.0.0:8080           0.0.0.0:0              LISTENING  # 后端
```

## 9. 服务端口分配

| 服务 | 端口 | 访问地址 | 状态检查 |
|------|------|----------|----------|
| 前端 (React) | 3000 | http://localhost:3000 | 浏览器访问 |
| 后端 (Spring Boot) | 8080 | http://localhost:8080 | /actuator/health |
| NLP服务 (Python) | 5001 | http://localhost:5001 | /api/health |
| MySQL数据库 | 3306 | localhost:3306 | mysql客户端连接 |

## 10. 故障排除

### 10.1 MySQL启动失败
```powershell
# 检查端口占用
netstat -an | findstr :3306

# 检查MySQL进程
Get-Process -Name "mysqld" -ErrorAction SilentlyContinue

# 检查配置文件
Test-Path "D:\programenv\MySQL-8.4.6\my-8.4.ini"
```

### 10.2 Maven命令不可用
```powershell
# 重新设置环境变量
$env:MAVEN_HOME = "H:\开发环境\Maven\apache-maven-3.9.6"
$env:PATH = "$env:MAVEN_HOME\bin;$env:PATH"

# 验证
mvn -version
```

### 10.3 端口冲突
```powershell
# 查找占用端口的进程
netstat -ano | findstr :端口号

# 结束进程 (谨慎使用)
taskkill /PID 进程ID /F
```

### 10.4 数据库连接失败
```powershell
# 测试数据库连接
& "D:\programenv\MySQL-8.4.6\bin\mysql.exe" -u history_user -phistory_password -h localhost -P 3306 -D history_analysis

# 如果连接失败，检查用户权限
& "D:\programenv\MySQL-8.4.6\bin\mysql.exe" -u root -proot123456 -e "SHOW GRANTS FOR 'history_user'@'%';"
```

## 11. 快速启动脚本

### 11.1 一键启动脚本 (start-all.ps1)
```powershell
# 创建启动脚本
Write-Host "启动历史数据分析系统..." -ForegroundColor Green

# 启动MySQL
Write-Host "1. 启动MySQL..." -ForegroundColor Yellow
Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd H:\projects\midHis; & 'D:\programenv\MySQL-8.4.6\bin\mysqld.exe' --defaults-file='D:\programenv\MySQL-8.4.6\my-8.4.ini' --console"

Start-Sleep -Seconds 5

# 启动NLP服务
Write-Host "2. 启动NLP服务..." -ForegroundColor Yellow
Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd H:\projects\midHis\nlp-service; python app.py"

Start-Sleep -Seconds 3

# 启动后端
Write-Host "3. 启动后端服务..." -ForegroundColor Yellow
Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd H:\projects\midHis\backend; `$env:MAVEN_HOME = 'H:\开发环境\Maven\apache-maven-3.9.6'; `$env:PATH = '`$env:MAVEN_HOME\bin;`$env:PATH'; mvn spring-boot:run -Dspring.profiles.active=mysql"

Start-Sleep -Seconds 10

# 启动前端
Write-Host "4. 启动前端服务..." -ForegroundColor Yellow
Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd H:\projects\midHis\frontend; npm run dev"

Write-Host "所有服务启动完成！" -ForegroundColor Green
Write-Host "前端访问: http://localhost:3000" -ForegroundColor Cyan
Write-Host "后端API: http://localhost:8080" -ForegroundColor Cyan
```

## 12. 重要提醒

### 12.1 启动顺序
1. **必须先启动MySQL** - 其他服务依赖数据库
2. **NLP服务** - 后端需要调用NLP接口
3. **后端服务** - 前端需要调用后端API
4. **前端服务** - 最后启动用户界面

### 12.2 环境变量
- 每次新开终端都需要重新设置Maven环境变量
- 建议将Maven路径添加到系统PATH中

### 12.3 数据库配置
- 项目使用MySQL作为生产数据库
- 开发时可以使用H2内存数据库 (application.yml)
- 生产环境必须使用MySQL配置 (application-mysql.yml)

---

**维护说明**: 本文档应随着项目配置变更及时更新，确保AI Agent能够准确执行启动流程。