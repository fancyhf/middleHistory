# 简化的MySQL安装脚本
# 使用已下载的MSI文件进行安装

Write-Host "=== MySQL 8.4.6 简化安装脚本 ===" -ForegroundColor Green
Write-Host ""

# 检查MSI文件是否存在
$msiPath = "D:\temp\mysql-installer\mysql-8.4.6-winx64.msi"
if (-not (Test-Path $msiPath)) {
    Write-Host "错误: 找不到MSI安装文件: $msiPath" -ForegroundColor Red
    exit 1
}

Write-Host "找到安装文件: $msiPath" -ForegroundColor Green

# 创建安装目录
$installDir = "D:\programenv\MySQL"
Write-Host "创建安装目录: $installDir" -ForegroundColor Yellow
New-Item -ItemType Directory -Path $installDir -Force | Out-Null

# 安装MySQL (需要管理员权限)
Write-Host "开始安装MySQL..." -ForegroundColor Yellow
Write-Host "注意: 此操作需要管理员权限" -ForegroundColor Red

$arguments = @(
    "/i"
    "`"$msiPath`""
    "/qn"
    "INSTALLDIR=`"$installDir`""
    "DATADIR=`"$installDir\data`""
    "MYSQL_ROOT_PASSWORD=root123456"
    "PORT=3306"
    "SERVICENAME=MySQL84"
)

try {
    $process = Start-Process -FilePath "msiexec.exe" -ArgumentList $arguments -Wait -PassThru -Verb RunAs
    
    if ($process.ExitCode -eq 0) {
        Write-Host "MySQL安装成功!" -ForegroundColor Green
    } else {
        Write-Host "MySQL安装失败，退出代码: $($process.ExitCode)" -ForegroundColor Red
        Write-Host "常见错误代码:" -ForegroundColor Yellow
        Write-Host "1602 - 用户取消安装" -ForegroundColor White
        Write-Host "1603 - 安装过程中发生致命错误" -ForegroundColor White
        Write-Host "1619 - 无法打开安装包" -ForegroundColor White
    }
} catch {
    Write-Host "安装过程中发生异常: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host ""
Write-Host "安装完成。" -ForegroundColor Green