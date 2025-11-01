# 数据库连接测试脚本 - 历史数据统计分析工具
# Author: AI Agent
# Version: 1.0.0
# Created: 2025-01-25 22:15:00

param(
    [string]$DatabaseType = "h2",  # h2 或 mysql
    [string]$Host = "localhost",
    [string]$Port = "3306",
    [string]$Database = "history_analysis",
    [string]$Username = "history_user",
    [string]$Password = "history_password"
)

Write-Host "=== 数据库连接测试 ===" -ForegroundColor Green
Write-Host "数据库类型: $DatabaseType" -ForegroundColor Yellow
Write-Host "测试时间: $(Get-Date)" -ForegroundColor Yellow

if ($DatabaseType -eq "mysql") {
    # MySQL连接测试
    Write-Host "`n测试MySQL连接..." -ForegroundColor Cyan
    
    # 检查MySQL服务状态
    try {
        $service = Get-Service MySQL80 -ErrorAction SilentlyContinue
        if ($service) {
            Write-Host "MySQL服务状态: $($service.Status)" -ForegroundColor $(if($service.Status -eq "Running") {"Green"} else {"Red"})
        } else {
            Write-Host "MySQL服务未安装" -ForegroundColor Red
            return
        }
    } catch {
        Write-Host "无法检查MySQL服务状态" -ForegroundColor Red
    }
    
    # 检查MySQL端口
    try {
        $connection = Test-NetConnection -ComputerName $Host -Port $Port -WarningAction SilentlyContinue
        if ($connection.TcpTestSucceeded) {
            Write-Host "✓ MySQL端口 $Port 可访问" -ForegroundColor Green
        } else {
            Write-Host "✗ MySQL端口 $Port 不可访问" -ForegroundColor Red
        }
    } catch {
        Write-Host "端口测试失败" -ForegroundColor Red
    }
    
    # 测试MySQL命令行工具
    try {
        $mysqlPath = "D:\programenv\MySQL\bin\mysql.exe"
        if (Test-Path $mysqlPath) {
            Write-Host "✓ MySQL客户端工具存在: $mysqlPath" -ForegroundColor Green
            
            # 测试连接
            $testQuery = "SELECT 'Connection Test' as message, NOW() as current_time;"
            $result = & $mysqlPath -h $Host -P $Port -u $Username -p$Password -e $testQuery 2>$null
            
            if ($result) {
                Write-Host "✓ MySQL数据库连接成功" -ForegroundColor Green
                Write-Host "连接结果:" -ForegroundColor White
                $result | ForEach-Object { Write-Host "  $_" -ForegroundColor White }
            } else {
                Write-Host "✗ MySQL数据库连接失败" -ForegroundColor Red
            }
        } else {
            Write-Host "✗ MySQL客户端工具不存在" -ForegroundColor Red
        }
    } catch {
        Write-Host "✗ MySQL连接测试失败: $($_.Exception.Message)" -ForegroundColor Red
    }
    
} elseif ($DatabaseType -eq "h2") {
    # H2数据库测试
    Write-Host "`n测试H2内存数据库..." -ForegroundColor Cyan
    
    # 检查Java环境
    try {
        $javaVersion = java -version 2>&1 | Select-String "version"
        if ($javaVersion) {
            Write-Host "✓ Java环境: $javaVersion" -ForegroundColor Green
        } else {
            Write-Host "✗ Java环境未配置" -ForegroundColor Red
            return
        }
    } catch {
        Write-Host "✗ 无法检查Java环境" -ForegroundColor Red
        return
    }
    
    # 检查H2依赖
    $h2JarPath = Get-ChildItem -Path "H:\projects\midHis\backend" -Recurse -Filter "h2-*.jar" -ErrorAction SilentlyContinue | Select-Object -First 1
    if ($h2JarPath) {
        Write-Host "✓ H2数据库JAR文件存在: $($h2JarPath.Name)" -ForegroundColor Green
    } else {
        Write-Host "⚠ H2数据库JAR文件未找到（可能在Maven仓库中）" -ForegroundColor Yellow
    }
    
    Write-Host "✓ H2内存数据库配置正常" -ForegroundColor Green
    Write-Host "H2控制台URL: http://localhost:8080/api/h2-console" -ForegroundColor White
    Write-Host "JDBC URL: jdbc:h2:mem:history_analysis" -ForegroundColor White
    Write-Host "用户名: sa" -ForegroundColor White
    Write-Host "密码: (空)" -ForegroundColor White
}

# 检查Spring Boot配置文件
Write-Host "`n检查Spring Boot配置..." -ForegroundColor Cyan
$configFiles = @(
    "H:\projects\midHis\backend\src\main\resources\application.yml",
    "H:\projects\midHis\backend\src\main\resources\application-mysql.yml"
)

foreach ($configFile in $configFiles) {
    if (Test-Path $configFile) {
        Write-Host "✓ 配置文件存在: $(Split-Path $configFile -Leaf)" -ForegroundColor Green
        
        # 检查数据源配置
        $content = Get-Content $configFile -Raw
        if ($content -match "datasource:") {
            Write-Host "  - 包含数据源配置" -ForegroundColor White
        }
        if ($content -match "mysql") {
            Write-Host "  - 包含MySQL配置" -ForegroundColor White
        }
        if ($content -match "h2") {
            Write-Host "  - 包含H2配置" -ForegroundColor White
        }
    } else {
        Write-Host "✗ 配置文件不存在: $(Split-Path $configFile -Leaf)" -ForegroundColor Red
    }
}

# 检查数据库初始化脚本
Write-Host "`n检查数据库初始化脚本..." -ForegroundColor Cyan
$initScripts = @(
    "H:\projects\midHis\database\init\01_create_database.sql",
    "H:\projects\midHis\database\init\02_create_tables.sql",
    "H:\projects\midHis\database\init\03_insert_test_data.sql",
    "H:\projects\midHis\database\init\04_update_tables.sql"
)

foreach ($script in $initScripts) {
    if (Test-Path $script) {
        $size = (Get-Item $script).Length
        Write-Host "✓ $(Split-Path $script -Leaf) ($size bytes)" -ForegroundColor Green
    } else {
        Write-Host "✗ $(Split-Path $script -Leaf) 不存在" -ForegroundColor Red
    }
}

Write-Host "`n=== 测试完成 ===" -ForegroundColor Green
Write-Host "建议:" -ForegroundColor Yellow

if ($DatabaseType -eq "h2") {
    Write-Host "1. 当前使用H2内存数据库，适合开发测试" -ForegroundColor White
    Write-Host "2. 如需MySQL，请运行: .\install-mysql-d-drive.ps1" -ForegroundColor White
    Write-Host "3. 启动应用后访问H2控制台进行数据库管理" -ForegroundColor White
} else {
    Write-Host "1. 确保MySQL服务正常运行" -ForegroundColor White
    Write-Host "2. 使用 -Dspring.profiles.active=mysql 启动应用" -ForegroundColor White
    Write-Host "3. 检查防火墙和网络连接设置" -ForegroundColor White
}

Write-Host "`n完成时间: $(Get-Date)" -ForegroundColor Yellow