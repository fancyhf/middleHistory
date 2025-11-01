# Simple MySQL Connection Test
# Test MySQL connection with different credentials

Write-Host "=== MySQL Connection Test ===" -ForegroundColor Green

# Test different MySQL paths
$mysqlPaths = @(
    "D:\programenv\MySQL-8.4.6\bin\mysql.exe",
    "D:\programenv\MySQL\bin\mysql.exe",
    "C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe",
    "mysql.exe"
)

$credentials = @(
    @{User="root"; Password="root123456"},
    @{User="root"; Password=""},
    @{User="history_user"; Password="history_password"}
)

foreach ($mysqlPath in $mysqlPaths) {
    if (Test-Path $mysqlPath -ErrorAction SilentlyContinue) {
        Write-Host "Found MySQL at: $mysqlPath" -ForegroundColor Yellow
        
        foreach ($cred in $credentials) {
            Write-Host "Testing connection with user: $($cred.User)" -ForegroundColor Cyan
            
            try {
                if ($cred.Password -eq "") {
                    $result = & $mysqlPath -u $cred.User -e "SELECT 'Connection successful' as status;" 2>&1
                } else {
                    $result = & $mysqlPath -u $cred.User -p$($cred.Password) -e "SELECT 'Connection successful' as status;" 2>&1
                }
                
                if ($LASTEXITCODE -eq 0) {
                    Write-Host "SUCCESS: Connected with $($cred.User)" -ForegroundColor Green
                    Write-Host "Result: $result" -ForegroundColor White
                    exit 0
                } else {
                    Write-Host "FAILED: $result" -ForegroundColor Red
                }
            } catch {
                Write-Host "ERROR: $($_.Exception.Message)" -ForegroundColor Red
            }
        }
    }
}

Write-Host "No working MySQL connection found" -ForegroundColor Red
exit 1