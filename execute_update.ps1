# 执行SQL更新脚本（Docker环境）
# 适用于: mysql-ccrc 容器
# 作者: System
# 日期: 2025-10-12

$containerName = "mysql-ccrc"
$sqlFile = "C:\Users\Administrator\Development\evaluation\update_steps_2_to_5.sql"
$database = "evaluate_db"
$username = "root"

Write-Host "=====================================" -ForegroundColor Cyan
Write-Host "  Docker MySQL 数据库更新脚本" -ForegroundColor Cyan
Write-Host "  步骤2-5算法配置更新" -ForegroundColor Cyan
Write-Host "=====================================" -ForegroundColor Cyan
Write-Host ""

# 检查Docker容器是否运行
Write-Host "[1/5] 检查Docker容器状态..." -ForegroundColor Yellow
try {
    $containerStatus = docker ps --filter "name=$containerName" --format "{{.Status}}" 2>$null
    if (-not $containerStatus) {
        Write-Host "❌ 错误: 容器 '$containerName' 未运行" -ForegroundColor Red
        Write-Host ""
        Write-Host "请尝试以下命令启动容器:" -ForegroundColor Yellow
        Write-Host "  docker start $containerName" -ForegroundColor White
        Write-Host ""
        exit 1
    }
    Write-Host "✅ 容器运行正常: $containerStatus" -ForegroundColor Green
} catch {
    Write-Host "❌ 错误: 无法连接到Docker" -ForegroundColor Red
    Write-Host "请确保Docker Desktop正在运行" -ForegroundColor Yellow
    exit 1
}
Write-Host ""

# 检查SQL文件是否存在
Write-Host "[2/5] 检查SQL文件..." -ForegroundColor Yellow
if (-not (Test-Path $sqlFile)) {
    Write-Host "❌ 错误: SQL文件不存在: $sqlFile" -ForegroundColor Red
    exit 1
}
$fileSize = (Get-Item $sqlFile).Length
Write-Host "✅ SQL文件存在 (大小: $([Math]::Round($fileSize/1KB, 2)) KB)" -ForegroundColor Green
Write-Host ""

# 复制SQL文件到容器
Write-Host "[3/5] 复制SQL文件到容器..." -ForegroundColor Yellow
try {
    docker cp $sqlFile "${containerName}:/tmp/update_steps_2_to_5.sql" 2>&1 | Out-Null
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
Write-Host "[4/5] 执行SQL脚本..." -ForegroundColor Yellow
Write-Host "--------------------------------------" -ForegroundColor Gray
Write-Host "请输入MySQL root密码:" -ForegroundColor Cyan
Write-Host ""

$result = docker exec -it $containerName mysql -u $username -p --default-character-set=utf8mb4 $database -e "source /tmp/update_steps_2_to_5.sql" 2>&1

Write-Host ""
Write-Host "--------------------------------------" -ForegroundColor Gray

if ($LASTEXITCODE -eq 0) {
    Write-Host "✅ SQL脚本执行成功" -ForegroundColor Green
} else {
    Write-Host "❌ SQL脚本执行失败" -ForegroundColor Red
    Write-Host ""
    Write-Host "可能的原因:" -ForegroundColor Yellow
    Write-Host "  1. MySQL密码输入错误" -ForegroundColor White
    Write-Host "  2. 数据库 evaluate_db 不存在" -ForegroundColor White
    Write-Host "  3. 表结构不完整" -ForegroundColor White
    Write-Host ""
    Write-Host "请检查错误信息并重试" -ForegroundColor Yellow
    
    # 清理临时文件
    docker exec $containerName rm /tmp/update_steps_2_to_5.sql 2>&1 | Out-Null
    exit 1
}
Write-Host ""

# 清理临时文件
Write-Host "[5/5] 清理临时文件..." -ForegroundColor Yellow
try {
    docker exec $containerName rm /tmp/update_steps_2_to_5.sql 2>&1 | Out-Null
    Write-Host "✅ 清理完成" -ForegroundColor Green
} catch {
    Write-Host "⚠️  临时文件清理失败（可忽略）" -ForegroundColor Yellow
}
Write-Host ""

Write-Host "=====================================" -ForegroundColor Cyan
Write-Host "  🎉 更新完成！" -ForegroundColor Green
Write-Host "=====================================" -ForegroundColor Cyan
Write-Host ""

# 显示验证建议
Write-Host "接下来您可以：" -ForegroundColor Yellow
Write-Host ""
Write-Host "1️⃣  验证算法数量" -ForegroundColor White
Write-Host "   docker exec -it mysql-ccrc mysql -u root -p evaluate_db" -ForegroundColor Gray
Write-Host "   然后执行: SELECT COUNT(*) FROM step_algorithm;" -ForegroundColor Gray
Write-Host ""
Write-Host "2️⃣  查看步骤配置" -ForegroundColor White
Write-Host "   docker exec -it mysql-ccrc mysql -u root -p evaluate_db -e `"" -ForegroundColor Gray
Write-Host "   SELECT ms.step_order, ms.step_name, COUNT(sa.id) as count" -ForegroundColor Gray
Write-Host "   FROM model_step ms LEFT JOIN step_algorithm sa ON ms.id = sa.step_id" -ForegroundColor Gray
Write-Host "   WHERE ms.step_order BETWEEN 2 AND 5 GROUP BY ms.id;`"" -ForegroundColor Gray
Write-Host ""
Write-Host "3️⃣  测试评估流程" -ForegroundColor White
Write-Host "   使用前端界面或API执行完整的评估流程" -ForegroundColor Gray
Write-Host ""

# 提供快速验证命令
Write-Host "快速验证命令（复制并执行）：" -ForegroundColor Cyan
Write-Host "--------------------------------------" -ForegroundColor Gray
Write-Host 'docker exec -it mysql-ccrc mysql -u root -p evaluate_db -e "SELECT ms.step_order as Step, ms.step_name as Name, COUNT(sa.id) as Algorithms FROM model_step ms LEFT JOIN step_algorithm sa ON ms.id = sa.step_id WHERE ms.model_id = (SELECT id FROM evaluation_model WHERE model_code = ''STANDARD_MODEL'' LIMIT 1) AND ms.step_order BETWEEN 2 AND 5 GROUP BY ms.id, ms.step_order, ms.step_name ORDER BY ms.step_order;"' -ForegroundColor White
Write-Host "--------------------------------------" -ForegroundColor Gray
Write-Host ""

Write-Host "预期结果: 步骤2(8个)，步骤3(16个)，步骤4(8个)，步骤5(8个)" -ForegroundColor Green
Write-Host ""
