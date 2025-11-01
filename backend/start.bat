@echo off
chcp 65001 > nul
echo ========================================
echo 历史数据统计分析工具后端启动脚本
echo ========================================
echo.

:: 检查Java环境
echo [1/5] 检查Java环境...
java -version > nul 2>&1
if %errorlevel% neq 0 (
    echo ❌ 错误: 未找到Java环境，请确保已安装Java 17或更高版本
    pause
    exit /b 1
)
echo ✅ Java环境检查通过

:: 检查Maven环境
echo.
echo [2/5] 检查Maven环境...
mvn -version > nul 2>&1
if %errorlevel% neq 0 (
    echo ❌ 错误: 未找到Maven环境，请确保已安装Maven
    pause
    exit /b 1
)
echo ✅ Maven环境检查通过

:: 清理并编译项目
echo.
echo [3/5] 清理并编译项目...
call mvn clean compile -DskipTests
if %errorlevel% neq 0 (
    echo ❌ 错误: 项目编译失败
    pause
    exit /b 1
)
echo ✅ 项目编译成功

:: 运行测试
echo.
echo [4/5] 运行单元测试...
call mvn test
if %errorlevel% neq 0 (
    echo ⚠️  警告: 部分测试失败，但继续启动服务
) else (
    echo ✅ 所有测试通过
)

:: 启动应用
echo.
echo [5/5] 启动Spring Boot应用...
echo.
echo 🚀 正在启动历史数据统计分析工具后端服务...
echo 📍 服务地址: http://localhost:8080
echo 📍 API文档: http://localhost:8080/swagger-ui.html
echo 📍 健康检查: http://localhost:8080/actuator/health
echo.
echo 按 Ctrl+C 停止服务
echo ========================================
echo.

call mvn spring-boot:run

echo.
echo 服务已停止
pause