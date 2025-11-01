# MySQL D盘安装脚本 - 历史数据统计分析工具
# Author: AI Agent
# Version: 1.0.0
# Created: 2025-01-25 22:15:00

param(
    [string]$InstallPath = "D:\programenv\MySQL",
    [string]$RootPassword = "root123456",
    [string]$UserName = "history_user",
    [string]$UserPassword = "history_password"
)

Write-Host "=== MySQL D盘安装脚本 ===" -ForegroundColor Green
Write-Host "安装路径: $InstallPath" -ForegroundColor Yellow
Write-Host "开始时间: $(Get-Date)" -ForegroundColor Yellow

# 1. 创建安装目录
Write-Host "`n1. 创建安装目录..." -ForegroundColor Cyan
try {
    New-Item -ItemType Directory -Path "$InstallPath" -Force | Out-Null
    New-Item -ItemType Directory -Path "$InstallPath\data" -Force | Out-Null
    New-Item -ItemType Directory -Path "$InstallPath\logs" -Force | Out-Null
    New-Item -ItemType Directory -Path "$InstallPath\tmp" -Force | Out-Null
    New-Item -ItemType Directory -Path "$InstallPath\backup" -Force | Out-Null
    Write-Host "✓ 目录创建成功" -ForegroundColor Green
} catch {
    Write-Host "✗ 目录创建失败: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}

# 2. 下载MySQL安装包
Write-Host "`n2. 准备MySQL安装包..." -ForegroundColor Cyan
$downloadUrls = @(
    "https://mirrors.aliyun.com/mysql/MySQL-8.0/mysql-8.0.40-winx64.zip",
    "https://dev.mysql.com/get/Downloads/MySQL-8.0/mysql-8.0.40-winx64.zip",
    "https://cdn.mysql.com/Downloads/MySQL-8.0/mysql-8.0.40-winx64.zip"
)

$zipFile = "$InstallPath\mysql-8.0.40-winx64.zip"
$downloadSuccess = $false

foreach ($url in $downloadUrls) {
    Write-Host "尝试从 $url 下载..." -ForegroundColor Yellow
    try {
        Invoke-WebRequest -Uri $url -OutFile $zipFile -TimeoutSec 300
        if (Test-Path $zipFile) {
            $fileSize = (Get-Item $zipFile).Length
            if ($fileSize -gt 100MB) {
                Write-Host "✓ 下载成功，文件大小: $([math]::Round($fileSize/1MB, 2)) MB" -ForegroundColor Green
                $downloadSuccess = $true
                break
            } else {
                Write-Host "✗ 下载的文件太小，可能不完整" -ForegroundColor Red
                Remove-Item $zipFile -Force -ErrorAction SilentlyContinue
            }
        }
    } catch {
        Write-Host "✗ 下载失败: $($_.Exception.Message)" -ForegroundColor Red
    }
}

if (-not $downloadSuccess) {
    Write-Host "`n手动下载说明:" -ForegroundColor Yellow
    Write-Host "1. 访问: https://mirrors.aliyun.com/mysql/MySQL-8.0/" -ForegroundColor White
    Write-Host "2. 下载: mysql-8.0.40-winx64.zip" -ForegroundColor White
    Write-Host "3. 保存到: $zipFile" -ForegroundColor White
    Write-Host "4. 重新运行此脚本" -ForegroundColor White
    
    # 检查是否已有安装包
    if (Test-Path $zipFile) {
        Write-Host "`n发现已存在的安装包，继续安装..." -ForegroundColor Green
        $downloadSuccess = $true
    } else {
        Write-Host "`n请手动下载MySQL安装包后重新运行脚本" -ForegroundColor Red
        exit 1
    }
}

# 3. 解压安装包
Write-Host "`n3. 解压MySQL安装包..." -ForegroundColor Cyan
try {
    Expand-Archive -Path $zipFile -DestinationPath $InstallPath -Force
    $extractedFolder = Get-ChildItem -Path $InstallPath -Directory | Where-Object { $_.Name -like "mysql-*" } | Select-Object -First 1
    if ($extractedFolder) {
        # 移动文件到根目录
        Get-ChildItem -Path $extractedFolder.FullName | Move-Item -Destination $InstallPath -Force
        Remove-Item -Path $extractedFolder.FullName -Recurse -Force
        Write-Host "✓ 解压完成" -ForegroundColor Green
    }
} catch {
    Write-Host "✗ 解压失败: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}

# 4. 复制配置文件
Write-Host "`n4. 配置MySQL..." -ForegroundColor Cyan
try {
    $configSource = "H:\projects\midHis\database\mysql-d-drive-config.ini"
    $configDest = "$InstallPath\my.ini"
    
    if (Test-Path $configSource) {
        Copy-Item -Path $configSource -Destination $configDest -Force
        Write-Host "✓ 配置文件复制成功" -ForegroundColor Green
    } else {
        Write-Host "⚠ 配置文件不存在，使用默认配置" -ForegroundColor Yellow
    }
} catch {
    Write-Host "✗ 配置文件复制失败: $($_.Exception.Message)" -ForegroundColor Red
}

# 5. 初始化MySQL
Write-Host "`n5. 初始化MySQL数据库..." -ForegroundColor Cyan
try {
    $mysqlBin = "$InstallPath\bin\mysqld.exe"
    if (Test-Path $mysqlBin) {
        & $mysqlBin --initialize-insecure --basedir=$InstallPath --datadir="$InstallPath\data"
        Write-Host "✓ MySQL初始化完成" -ForegroundColor Green
    } else {
        Write-Host "✗ MySQL可执行文件不存在" -ForegroundColor Red
        exit 1
    }
} catch {
    Write-Host "✗ MySQL初始化失败: $($_.Exception.Message)" -ForegroundColor Red
}

# 6. 安装MySQL服务
Write-Host "`n6. 安装MySQL Windows服务..." -ForegroundColor Cyan
try {
    & $mysqlBin --install MySQL80 --defaults-file="$InstallPath\my.ini"
    Write-Host "✓ MySQL服务安装成功" -ForegroundColor Green
} catch {
    Write-Host "✗ MySQL服务安装失败: $($_.Exception.Message)" -ForegroundColor Red
}

# 7. 启动MySQL服务
Write-Host "`n7. 启动MySQL服务..." -ForegroundColor Cyan
try {
    Start-Service MySQL80
    Write-Host "✓ MySQL服务启动成功" -ForegroundColor Green
} catch {
    Write-Host "✗ MySQL服务启动失败: $($_.Exception.Message)" -ForegroundColor Red
}

# 8. 设置root密码
Write-Host "`n8. 配置root用户密码..." -ForegroundColor Cyan
try {
    $mysqlClient = "$InstallPath\bin\mysql.exe"
    $sqlCommands = @"
ALTER USER 'root'@'localhost' IDENTIFIED BY '$RootPassword';
FLUSH PRIVILEGES;
"@
    
    $sqlCommands | & $mysqlClient -u root --skip-password
    Write-Host "✓ root密码设置成功" -ForegroundColor Green
} catch {
    Write-Host "✗ root密码设置失败: $($_.Exception.Message)" -ForegroundColor Red
}

# 9. 执行数据库初始化脚本
Write-Host "`n9. 执行数据库初始化脚本..." -ForegroundColor Cyan
$initScripts = @(
    "H:\projects\midHis\database\init\01_create_database.sql",
    "H:\projects\midHis\database\init\02_create_tables.sql",
    "H:\projects\midHis\database\init\03_insert_test_data.sql",
    "H:\projects\midHis\database\init\04_update_tables.sql"
)

foreach ($script in $initScripts) {
    if (Test-Path $script) {
        try {
            Write-Host "执行: $(Split-Path $script -Leaf)" -ForegroundColor Yellow
            & $mysqlClient -u root -p$RootPassword < $script
            Write-Host "✓ 脚本执行成功" -ForegroundColor Green
        } catch {
            Write-Host "✗ 脚本执行失败: $($_.Exception.Message)" -ForegroundColor Red
        }
    } else {
        Write-Host "⚠ 脚本文件不存在: $script" -ForegroundColor Yellow
    }
}

# 10. 验证安装
Write-Host "`n10. 验证MySQL安装..." -ForegroundColor Cyan
try {
    $serviceStatus = Get-Service MySQL80 -ErrorAction SilentlyContinue
    if ($serviceStatus -and $serviceStatus.Status -eq "Running") {
        Write-Host "✓ MySQL服务运行正常" -ForegroundColor Green
    } else {
        Write-Host "✗ MySQL服务未运行" -ForegroundColor Red
    }
    
    # 测试连接
    $testQuery = "SELECT VERSION();"
    $result = & $mysqlClient -u root -p$RootPassword -e $testQuery 2>$null
    if ($result) {
        Write-Host "✓ 数据库连接测试成功" -ForegroundColor Green
        Write-Host "MySQL版本: $($result -split "`n" | Select-Object -Last 1)" -ForegroundColor White
    } else {
        Write-Host "✗ 数据库连接测试失败" -ForegroundColor Red
    }
} catch {
    Write-Host "✗ 验证过程出错: $($_.Exception.Message)" -ForegroundColor Red
}

# 11. 添加环境变量
Write-Host "`n11. 配置环境变量..." -ForegroundColor Cyan
try {
    $currentPath = [Environment]::GetEnvironmentVariable("PATH", "User")
    $mysqlBinPath = "$InstallPath\bin"
    
    if ($currentPath -notlike "*$mysqlBinPath*") {
        $newPath = "$currentPath;$mysqlBinPath"
        [Environment]::SetEnvironmentVariable("PATH", $newPath, "User")
        Write-Host "✓ MySQL bin目录已添加到PATH" -ForegroundColor Green
    } else {
        Write-Host "✓ MySQL bin目录已在PATH中" -ForegroundColor Green
    }
} catch {
    Write-Host "✗ 环境变量配置失败: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host "`n=== 安装完成 ===" -ForegroundColor Green
Write-Host "MySQL安装路径: $InstallPath" -ForegroundColor White
Write-Host "数据库连接信息:" -ForegroundColor White
Write-Host "  主机: localhost" -ForegroundColor White
Write-Host "  端口: 3306" -ForegroundColor White
Write-Host "  Root密码: $RootPassword" -ForegroundColor White
Write-Host "  用户: $UserName" -ForegroundColor White
Write-Host "  密码: $UserPassword" -ForegroundColor White
Write-Host "`n下一步:" -ForegroundColor Yellow
Write-Host "1. 重启PowerShell以使环境变量生效" -ForegroundColor White
Write-Host "2. 使用 'mysql -u root -p' 连接数据库" -ForegroundColor White
Write-Host "3. 更新Spring Boot配置使用MySQL" -ForegroundColor White
Write-Host "`n完成时间: $(Get-Date)" -ForegroundColor Yellow