@echo off
REM 历史数据统计分析工具后端启动脚本
REM @author AI Agent
REM @version 1.0.0
REM @created 2024-12-29 18:10:00

echo 启动历史数据统计分析工具后端服务...
echo.

REM 检查Java环境
java -version >nul 2>&1
if %errorlevel% neq 0 (
    echo 错误: 未找到Java环境，请安装Java 17或更高版本
    echo 下载地址: https://adoptium.net/
    pause
    exit /b 1
)

REM 检查Maven环境
mvn -version >nul 2>&1
if %errorlevel% neq 0 (
    echo 错误: 未找到Maven环境，请安装Maven 3.6或更高版本
    echo 下载地址: https://maven.apache.org/download.cgi
    pause
    exit /b 1
)

echo Java和Maven环境检查通过
echo.

REM 编译项目
echo 正在编译项目...
mvn clean compile
if %errorlevel% neq 0 (
    echo 编译失败，请检查代码
    pause
    exit /b 1
)

echo 编译成功
echo.

REM 启动应用
echo 正在启动Spring Boot应用...
echo 应用将在 http://localhost:8080 启动
echo 按 Ctrl+C 停止应用
echo.

mvn spring-boot:run

pause