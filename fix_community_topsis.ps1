# 修复社区评估TOPSIS配置的PowerShell脚本

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "社区评估TOPSIS配置修复脚本" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

$dbHost = "192.168.15.203"
$dbPort = "30314"
$dbUser = "root"
$dbPass = "123456"
$dbName = "evaluate_db"
$sqlFile = "fix_community_topsis_config.sql"

Write-Host "数据库连接信息:" -ForegroundColor Yellow
Write-Host "  主机: $dbHost" -ForegroundColor Gray
Write-Host "  端口: $dbPort" -ForegroundColor Gray
Write-Host "  数据库: $dbName" -ForegroundColor Gray
Write-Host ""

# 检查SQL文件是否存在
if (-not (Test-Path $sqlFile)) {
    Write-Host "错误: 找不到SQL文件 $sqlFile" -ForegroundColor Red
    exit 1
}

Write-Host "正在执行修复..." -ForegroundColor Yellow
Write-Host ""

# 执行SQL文件
try {
    $content = Get-Content $sqlFile -Raw -Encoding UTF8
    $content | mysql -h$dbHost -P$dbPort -u$dbUser -p$dbPass $dbName 2>&1 | ForEach-Object {
        if ($_ -match "Warning") {
            # 忽略密码警告
        } elseif ($_ -match "ERROR") {
            Write-Host $_ -ForegroundColor Red
        } else {
            Write-Host $_ -ForegroundColor Green
        }
    }
    
    Write-Host ""
    Write-Host "========================================" -ForegroundColor Cyan
    Write-Host "修复完成！" -ForegroundColor Green
    Write-Host "========================================" -ForegroundColor Cyan
    Write-Host ""
    Write-Host "下一步操作:" -ForegroundColor Yellow
    Write-Host "1. 重新执行社区评估" -ForegroundColor Gray
    Write-Host "2. 检查TOPSIS计算结果是否正确" -ForegroundColor Gray
    Write-Host ""
    
} catch {
    Write-Host "执行失败: $_" -ForegroundColor Red
    exit 1
}
