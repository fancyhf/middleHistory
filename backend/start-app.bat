@echo off
REM 历史数据统计分析工具后端服务启动脚本
REM Author: AI Agent
REM Version: 1.0.0
REM Created: 2024-12-29 18:00:00

echo ========================================
echo 历史数据统计分析工具后端服务启动脚本
echo ========================================

REM 检查Java环境
echo 检查Java环境...
java -version >nul 2>&1
if %errorlevel% neq 0 (
    echo 错误: 未找到Java环境，请确保已安装Java 17或更高版本
    pause
    exit /b 1
)

REM 检查Maven环境
echo 检查Maven环境...
mvn -version >nul 2>&1
if %errorlevel% neq 0 (
    echo 错误: 未找到Maven环境，请确保已安装Maven
    pause
    exit /b 1
)

REM 切换到项目目录
cd /d "%~dp0"
echo 当前目录: %cd%

REM 清理并编译项目
echo 正在清理并编译项目...
mvn clean compile
if %errorlevel% neq 0 (
    echo 错误: 项目编译失败
    pause
    exit /b 1
)

echo 编译成功！

REM 启动应用
echo 正在启动应用...
echo 应用将在 http://localhost:8080/api 启动
echo 按 Ctrl+C 停止应用
echo ========================================

mvn spring-boot:run

REM 如果应用异常退出
if %errorlevel% neq 0 (
    echo 应用启动失败，错误代码: %errorlevel%
    pause
)