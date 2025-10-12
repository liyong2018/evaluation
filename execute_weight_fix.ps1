# 修复权重指标代码不匹配问题
# 问题：步骤3算法表达式中的权重变量名与数据库indicator_weight表中的indicator_code不一致
# 解决：执行SQL脚本更新算法表达式

$containerName = "mysql-ccrc"
$sqlFile = "C:\Users\Administrator\Development\evaluation\fix_algorithm_weight_codes.sql"
$database = "evaluate_db"
$username = "root"
$password = "Htht1234"

Write-Host "=====================================" -ForegroundColor Cyan
Write-Host "  修复权重指标代码不匹配问题" -ForegroundColor Cyan
Write-Host "=====================================" -ForegroundColor Cyan
Write-Host ""

# 检查Docker容器是否运行
Write-Host "[1/4] 检查Docker容器状态..." -ForegroundColor Yellow
try {
    $containerStatus = docker ps --filter "name=$containerName" --format "{{.Status}}" 2>$null
    if (-not $containerStatus) {
        Write-Host "❌ 错误: 容器 '$containerName' 未运行" -ForegroundColor Red
        exit 1
    }
    Write-Host "✅ 容器运行正常" -ForegroundColor Green
} catch {
    Write-Host "❌ 错误: 无法连接到Docker" -ForegroundColor Red
    exit 1
}
Write-Host ""

# 检查SQL文件是否存在
Write-Host "[2/4] 检查SQL文件..." -ForegroundColor Yellow
if (-not (Test-Path $sqlFile)) {
    Write-Host "❌ 错误: SQL文件不存在: $sqlFile" -ForegroundColor Red
    exit 1
}
Write-Host "✅ SQL文件存在" -ForegroundColor Green
Write-Host ""

# 复制SQL文件到容器
Write-Host "[3/4] 复制SQL文件到容器..." -ForegroundColor Yellow
try {
    docker cp $sqlFile "${containerName}:/tmp/fix_algorithm_weight_codes.sql" 2>&1 | Out-Null
    if ($LASTEXITCODE -eq 0) {
        Write-Host "✅ 文件复制成功" -ForegroundColor Green
    } else {
        Write-Host "❌ 文件复制失败" -ForegroundColor Red
        exit 1
    }
} catch {
    Write-Host "❌ 文件复制失败: $_" -ForegroundColor Red
    exit 1
}
Write-Host ""

# 执行SQL脚本
Write-Host "[4/4] 执行SQL脚本..." -ForegroundColor Yellow
Write-Host "--------------------------------------" -ForegroundColor Gray

$result = docker exec $containerName mysql -u $username -p$password --default-character-set=utf8mb4 $database -e "source /tmp/fix_algorithm_weight_codes.sql" 2>&1

Write-Host ""
Write-Host "--------------------------------------" -ForegroundColor Gray

if ($LASTEXITCODE -eq 0) {
    Write-Host "✅ SQL脚本执行成功" -ForegroundColor Green
} else {
    Write-Host "❌ SQL脚本执行失败" -ForegroundColor Red
    Write-Host $result -ForegroundColor Red
    docker exec $containerName rm /tmp/fix_algorithm_weight_codes.sql 2>&1 | Out-Null
    exit 1
}
Write-Host ""

# 清理临时文件
docker exec $containerName rm /tmp/fix_algorithm_weight_codes.sql 2>&1 | Out-Null

Write-Host "=====================================" -ForegroundColor Cyan
Write-Host "  🎉 修复完成！" -ForegroundColor Green
Write-Host "=====================================" -ForegroundColor Cyan
Write-Host ""

Write-Host "问题说明：" -ForegroundColor Yellow
Write-Host "  原因：步骤3算法表达式使用的权重变量名与数据库不匹配" -ForegroundColor White
Write-Host "  例如：表达式用 weight_TEAM_MANAGEMENT，数据库是 weight_L2_MANAGEMENT_CAPABILITY" -ForegroundColor White
Write-Host ""
Write-Host "已更新的映射关系：" -ForegroundColor Yellow
Write-Host "  二级指标：" -ForegroundColor White
Write-Host "    • TEAM_MANAGEMENT → L2_MANAGEMENT_CAPABILITY" -ForegroundColor Gray
Write-Host "    • RISK_ASSESSMENT → L2_RISK_ASSESSMENT" -ForegroundColor Gray
Write-Host "    • FINANCIAL_INPUT → L2_FUNDING" -ForegroundColor Gray
Write-Host "    • MATERIAL_RESERVE → L2_MATERIAL" -ForegroundColor Gray
Write-Host "    • MEDICAL_SUPPORT → L2_MEDICAL" -ForegroundColor Gray
Write-Host "    • SELF_RESCUE → L2_SELF_RESCUE" -ForegroundColor Gray
Write-Host "    • PUBLIC_AVOIDANCE → L2_PUBLIC_AVOIDANCE" -ForegroundColor Gray
Write-Host "    • RELOCATION_CAPACITY → L2_RELOCATION" -ForegroundColor Gray
Write-Host ""
Write-Host "  一级指标：" -ForegroundColor White
Write-Host "    • DISASTER_MANAGEMENT → L1_MANAGEMENT" -ForegroundColor Gray
Write-Host "    • DISASTER_PREPAREDNESS → L1_PREPARATION" -ForegroundColor Gray
Write-Host "    • SELF_RESCUE_TRANSFER → L1_SELF_RESCUE" -ForegroundColor Gray
Write-Host ""

Write-Host "现在可以重新执行评估模型了！" -ForegroundColor Green
Write-Host ""
