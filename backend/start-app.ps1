# 历史数据统计分析工具后端服务启动脚本
# Author: AI Agent
# Version: 1.0.0
# Created: 2024-12-29 18:00:00

Write-Host "========================================" -ForegroundColor Green
Write-Host "历史数据统计分析工具后端服务启动脚本" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Green

# 检查Java环境
Write-Host "检查Java环境..." -ForegroundColor Yellow
try {
    $javaVersion = java -version 2>&1
    if ($LASTEXITCODE -ne 0) {
        throw "Java not found"
    }
    Write-Host "Java环境检查通过" -ForegroundColor Green
} catch {
    Write-Host "错误: 未找到Java环境，请确保已安装Java 17或更高版本" -ForegroundColor Red
    Read-Host "按任意键退出"
    exit 1
}

# 检查Maven环境
Write-Host "检查Maven环境..." -ForegroundColor Yellow
try {
    $mavenVersion = mvn -version 2>&1
    if ($LASTEXITCODE -ne 0) {
        throw "Maven not found"
    }
    Write-Host "Maven环境检查通过" -ForegroundColor Green
} catch {
    Write-Host "错误: 未找到Maven环境，请确保已安装Maven" -ForegroundColor Red
    Read-Host "按任意键退出"
    exit 1
}

# 切换到脚本所在目录
$scriptPath = Split-Path -Parent $MyInvocation.MyCommand.Path
Set-Location $scriptPath
Write-Host "当前目录: $(Get-Location)" -ForegroundColor Cyan

# 清理并编译项目
Write-Host "正在清理并编译项目..." -ForegroundColor Yellow
try {
    mvn clean compile
    if ($LASTEXITCODE -ne 0) {
        throw "Compilation failed"
    }
    Write-Host "编译成功！" -ForegroundColor Green
} catch {
    Write-Host "错误: 项目编译失败" -ForegroundColor Red
    Read-Host "按任意键退出"
    exit 1
}

# 启动应用
Write-Host "正在启动应用..." -ForegroundColor Yellow
Write-Host "应用将在 http://localhost:8080/api 启动" -ForegroundColor Cyan
Write-Host "按 Ctrl+C 停止应用" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Green

try {
    mvn spring-boot:run
} catch {
    Write-Host "应用启动失败，错误代码: $LASTEXITCODE" -ForegroundColor Red
    Read-Host "按任意键退出"
}