# MySQL手动安装脚本
# 需要以管理员身份运行PowerShell

Write-Host "=== MySQL 8.4.6 安装脚本 ===" -ForegroundColor Green
Write-Host ""
Write-Host "安装参数:" -ForegroundColor Yellow
Write-Host "- 安装目录: D:\programenv\MySQL" -ForegroundColor White
Write-Host "- 数据目录: D:\programenv\MySQL\data" -ForegroundColor White  
Write-Host "- Root密码: root123456" -ForegroundColor White
Write-Host "- 端口: 3306" -ForegroundColor White
Write-Host "- 服务名: MySQL84" -ForegroundColor White
Write-Host ""

# 检查是否以管理员身份运行
$isAdmin = ([Security.Principal.WindowsPrincipal] [Security.Principal.WindowsIdentity]::GetCurrent()).IsInRole([Security.Principal.WindowsBuiltInRole] "Administrator")

if (-not $isAdmin) {
    Write-Host "错误: 请以管理员身份运行此脚本!" -ForegroundColor Red
    Write-Host "右键点击PowerShell -> 以管理员身份运行" -ForegroundColor Yellow
    Read-Host "按任意键退出"
    exit 1
}

Write-Host "开始安装MySQL..." -ForegroundColor Green

# 执行安装
$installArgs = @(
    "/i"
    "`"D:\temp\mysql-installer\mysql-8.4.6-winx64.msi`""
    "/quiet"
    "INSTALLDIR=`"D:\programenv\MySQL`""
    "DATADIR=`"D:\programenv\MySQL\data`""
    "MYSQL_ROOT_PASSWORD=root123456"
    "PORT=3306"
    "SERVICENAME=MySQL84"
)

try {
    $process = Start-Process -FilePath "msiexec.exe" -ArgumentList $installArgs -Wait -PassThru
    
    if ($process.ExitCode -eq 0) {
        Write-Host "MySQL安装成功!" -ForegroundColor Green
        
        # 检查服务状态
        Write-Host "检查MySQL服务状态..." -ForegroundColor Yellow
        $service = Get-Service -Name "MySQL84" -ErrorAction SilentlyContinue
        if ($service) {
            Write-Host "MySQL服务已创建: $($service.Status)" -ForegroundColor Green
            
            if ($service.Status -ne "Running") {
                Write-Host "启动MySQL服务..." -ForegroundColor Yellow
                Start-Service -Name "MySQL84"
                Write-Host "MySQL服务已启动" -ForegroundColor Green
            }
        } else {
            Write-Host "警告: 未找到MySQL84服务" -ForegroundColor Yellow
        }
        
        # 检查安装目录
        if (Test-Path "D:\programenv\MySQL") {
            Write-Host "安装目录确认: D:\programenv\MySQL" -ForegroundColor Green
            Get-ChildItem "D:\programenv\MySQL" | Select-Object Name, LastWriteTime
        }
        
    } else {
        Write-Host "MySQL安装失败，退出代码: $($process.ExitCode)" -ForegroundColor Red
    }
} catch {
    Write-Host "安装过程中发生错误: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host ""
Write-Host "安装完成。请检查上述输出信息。" -ForegroundColor Green
Read-Host "按任意键退出"