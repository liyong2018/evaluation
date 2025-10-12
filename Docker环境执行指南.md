# Docker环境下执行SQL更新脚本指南

## 环境说明

- **容器名称**: `mysql-ccrc`
- **数据库**: `evaluate_db`
- **脚本文件**: `update_steps_2_to_5.sql`
- **编码**: UTF-8 (utf8mb4)

---

## 执行方法

### 方法1: 使用docker exec执行SQL文件（推荐）

#### 步骤1: 将SQL文件复制到容器内
```powershell
# 从宿主机复制SQL文件到Docker容器
docker cp "C:\Users\Administrator\Development\evaluation\update_steps_2_to_5.sql" mysql-ccrc:/tmp/update_steps_2_to_5.sql
```

#### 步骤2: 在容器内执行SQL脚本
```powershell
# 执行SQL脚本（需要输入MySQL root密码）
docker exec -it mysql-ccrc mysql -u root -p --default-character-set=utf8mb4 evaluate_db -e "source /tmp/update_steps_2_to_5.sql"
```

#### 步骤3: 清理临时文件（可选）
```powershell
# 删除容器内的临时SQL文件
docker exec mysql-ccrc rm /tmp/update_steps_2_to_5.sql
```

---

### 方法2: 通过管道直接执行（一行命令）

```powershell
# 直接通过管道执行SQL（需要输入MySQL root密码）
Get-Content "C:\Users\Administrator\Development\evaluation\update_steps_2_to_5.sql" -Raw -Encoding UTF8 | docker exec -i mysql-ccrc mysql -u root -p --default-character-set=utf8mb4 evaluate_db
```

**注意**: 使用此方法时，密码提示可能不明显，请直接输入密码后按回车。

---

### 方法3: 使用MySQL客户端连接容器（适合调试）

#### 步骤1: 进入MySQL命令行
```powershell
# 进入容器的MySQL命令行
docker exec -it mysql-ccrc mysql -u root -p evaluate_db
```

#### 步骤2: 设置字符编码
```sql
SET NAMES utf8mb4;
SET CHARACTER SET utf8mb4;
```

#### 步骤3: 执行SQL脚本
```sql
-- 在MySQL命令行中执行
source /tmp/update_steps_2_to_5.sql;
```
**前提**: 需要先使用方法1的步骤1将文件复制到容器内。

---

### 方法4: 使用docker exec一次性执行（无需复制文件）

```powershell
# 将SQL内容直接传递给容器内的MySQL（需要输入密码）
docker exec -i mysql-ccrc sh -c 'mysql -u root -p --default-character-set=utf8mb4 evaluate_db' < "C:\Users\Administrator\Development\evaluation\update_steps_2_to_5.sql"
```

---

## 快速执行脚本

### 完整自动化脚本（PowerShell）

将以下内容保存为 `execute_update.ps1`:

```powershell
# 执行SQL更新脚本（Docker环境）
# 作者：System
# 日期：2025-10-12

$containerName = "mysql-ccrc"
$sqlFile = "C:\Users\Administrator\Development\evaluation\update_steps_2_to_5.sql"
$database = "evaluate_db"
$username = "root"

Write-Host "=====================================" -ForegroundColor Cyan
Write-Host "  Docker MySQL 数据库更新脚本" -ForegroundColor Cyan
Write-Host "=====================================" -ForegroundColor Cyan
Write-Host ""

# 检查Docker容器是否运行
Write-Host "[1/5] 检查Docker容器状态..." -ForegroundColor Yellow
$containerStatus = docker ps --filter "name=$containerName" --format "{{.Status}}"
if (-not $containerStatus) {
    Write-Host "❌ 错误: 容器 '$containerName' 未运行" -ForegroundColor Red
    Write-Host "请先启动容器: docker start $containerName" -ForegroundColor Yellow
    exit 1
}
Write-Host "✅ 容器运行正常: $containerStatus" -ForegroundColor Green
Write-Host ""

# 检查SQL文件是否存在
Write-Host "[2/5] 检查SQL文件..." -ForegroundColor Yellow
if (-not (Test-Path $sqlFile)) {
    Write-Host "❌ 错误: SQL文件不存在: $sqlFile" -ForegroundColor Red
    exit 1
}
Write-Host "✅ SQL文件存在" -ForegroundColor Green
Write-Host ""

# 复制SQL文件到容器
Write-Host "[3/5] 复制SQL文件到容器..." -ForegroundColor Yellow
docker cp $sqlFile "${containerName}:/tmp/update_steps_2_to_5.sql" 2>&1 | Out-Null
if ($LASTEXITCODE -eq 0) {
    Write-Host "✅ 文件复制成功" -ForegroundColor Green
} else {
    Write-Host "❌ 文件复制失败" -ForegroundColor Red
    exit 1
}
Write-Host ""

# 执行SQL脚本
Write-Host "[4/5] 执行SQL脚本..." -ForegroundColor Yellow
Write-Host "请输入MySQL root密码:" -ForegroundColor Cyan
docker exec -it $containerName mysql -u $username -p --default-character-set=utf8mb4 $database -e "source /tmp/update_steps_2_to_5.sql"

if ($LASTEXITCODE -eq 0) {
    Write-Host "✅ SQL脚本执行成功" -ForegroundColor Green
} else {
    Write-Host "❌ SQL脚本执行失败" -ForegroundColor Red
    Write-Host "请检查MySQL密码是否正确" -ForegroundColor Yellow
    exit 1
}
Write-Host ""

# 清理临时文件
Write-Host "[5/5] 清理临时文件..." -ForegroundColor Yellow
docker exec $containerName rm /tmp/update_steps_2_to_5.sql 2>&1 | Out-Null
Write-Host "✅ 清理完成" -ForegroundColor Green
Write-Host ""

Write-Host "=====================================" -ForegroundColor Cyan
Write-Host "  🎉 更新完成！" -ForegroundColor Green
Write-Host "=====================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "接下来您可以：" -ForegroundColor Yellow
Write-Host "1. 验证更新结果: docker exec -it mysql-ccrc mysql -u root -p evaluate_db" -ForegroundColor White
Write-Host "2. 查看算法配置: SELECT * FROM step_algorithm WHERE step_id IN (16,17,18,19);" -ForegroundColor White
Write-Host "3. 测试评估流程: 通过前端或API执行评估" -ForegroundColor White
```

### 执行PowerShell脚本

```powershell
# 方式1: 直接执行
.\execute_update.ps1

# 方式2: 如果遇到执行策略限制
powershell -ExecutionPolicy Bypass -File .\execute_update.ps1
```

---

## 执行前检查清单

### 1. 检查Docker容器状态
```powershell
# 查看容器是否运行
docker ps | Select-String "mysql-ccrc"

# 如果容器未运行，启动容器
docker start mysql-ccrc

# 查看容器日志（确认MySQL已完全启动）
docker logs mysql-ccrc --tail 20
```

### 2. 检查MySQL连接
```powershell
# 测试连接（需要输入密码）
docker exec -it mysql-ccrc mysql -u root -p -e "SELECT VERSION();"

# 检查数据库是否存在
docker exec -it mysql-ccrc mysql -u root -p -e "SHOW DATABASES LIKE 'evaluate_db';"
```

### 3. 检查现有模型配置
```powershell
# 查看当前的模型步骤
docker exec -it mysql-ccrc mysql -u root -p evaluate_db -e "SELECT id, step_order, step_name FROM model_step WHERE model_id = (SELECT id FROM evaluation_model WHERE model_code = 'STANDARD_MODEL' LIMIT 1) ORDER BY step_order;"

# 查看步骤1的算法数量（应该是8个）
docker exec -it mysql-ccrc mysql -u root -p evaluate_db -e "SELECT COUNT(*) as step1_count FROM step_algorithm WHERE step_id = (SELECT id FROM model_step WHERE model_id = (SELECT id FROM evaluation_model WHERE model_code = 'STANDARD_MODEL' LIMIT 1) AND step_order = 1 LIMIT 1);"
```

---

## 执行后验证

### 1. 快速验证
```powershell
# 验证步骤2-5的算法数量
docker exec -it mysql-ccrc mysql -u root -p evaluate_db -e "
SELECT 
    ms.step_order as '步骤',
    ms.step_name as '名称',
    COUNT(sa.id) as '算法数'
FROM model_step ms
LEFT JOIN step_algorithm sa ON ms.id = sa.step_id
WHERE ms.model_id = (SELECT id FROM evaluation_model WHERE model_code = 'STANDARD_MODEL' LIMIT 1)
AND ms.step_order BETWEEN 2 AND 5
GROUP BY ms.id, ms.step_order, ms.step_name
ORDER BY ms.step_order;
"
```

**预期结果**:
- 步骤2: 8个算法
- 步骤3: 16个算法
- 步骤4: 8个算法
- 步骤5: 8个算法

### 2. 详细验证
```powershell
# 查看步骤2的算法详情
docker exec -it mysql-ccrc mysql -u root -p evaluate_db -e "
SELECT 
    algorithm_order,
    algorithm_name,
    algorithm_code,
    output_param
FROM step_algorithm
WHERE step_id = (
    SELECT id FROM model_step 
    WHERE model_id = (SELECT id FROM evaluation_model WHERE model_code = 'STANDARD_MODEL' LIMIT 1)
    AND step_order = 2
    LIMIT 1
)
ORDER BY algorithm_order;
"
```

### 3. 检查中文编码
```powershell
# 验证中文字符是否正确显示
docker exec -it mysql-ccrc mysql -u root -p evaluate_db -e "
SELECT 
    id,
    algorithm_name,
    description
FROM step_algorithm
WHERE step_id = (
    SELECT id FROM model_step 
    WHERE model_id = (SELECT id FROM evaluation_model WHERE model_code = 'STANDARD_MODEL' LIMIT 1)
    AND step_order = 2
    LIMIT 1
)
LIMIT 3;
"
```

---

## 常见问题与解决方案

### 问题1: 密码输入没有反应
**原因**: Docker exec -it 模式下的密码提示可能不明显

**解决方案**:
```powershell
# 使用环境变量传递密码（不推荐生产环境）
$env:MYSQL_PWD="你的密码"
docker exec -i mysql-ccrc mysql -u root --default-character-set=utf8mb4 evaluate_db < "C:\Users\Administrator\Development\evaluation\update_steps_2_to_5.sql"
$env:MYSQL_PWD=""  # 清除密码
```

### 问题2: 容器内文件路径问题
**解决方案**: 使用容器内的 /tmp 目录，这是标准的临时目录
```powershell
# 确认/tmp目录可写
docker exec mysql-ccrc ls -la /tmp
```

### 问题3: 中文乱码
**解决方案**: 确保使用 utf8mb4 字符集
```powershell
# 检查容器内MySQL字符集配置
docker exec mysql-ccrc mysql -u root -p -e "SHOW VARIABLES LIKE 'character%';"

# 在SQL脚本中已包含字符集设置
# SET NAMES utf8mb4;
# SET CHARACTER SET utf8mb4;
```

### 问题4: 权限不足
**解决方案**: 确保使用root用户或有足够权限的用户
```powershell
# 检查当前用户权限
docker exec -it mysql-ccrc mysql -u root -p -e "SHOW GRANTS FOR 'root'@'%';"
```

### 问题5: Docker容器未运行
**解决方案**:
```powershell
# 查看所有容器（包括停止的）
docker ps -a | Select-String "mysql-ccrc"

# 启动容器
docker start mysql-ccrc

# 等待MySQL完全启动（约10-30秒）
Start-Sleep -Seconds 15

# 验证MySQL服务已启动
docker exec mysql-ccrc mysqladmin -u root -p ping
```

---

## 数据备份建议

### 执行前备份
```powershell
# 备份step_algorithm表
docker exec mysql-ccrc mysqldump -u root -p evaluate_db step_algorithm > "C:\Users\Administrator\Development\evaluation\backups\step_algorithm_backup_$(Get-Date -Format 'yyyyMMdd_HHmmss').sql"

# 备份model_step表
docker exec mysql-ccrc mysqldump -u root -p evaluate_db model_step > "C:\Users\Administrator\Development\evaluation\backups\model_step_backup_$(Get-Date -Format 'yyyyMMdd_HHmmss').sql"

# 完整备份evaluate_db数据库
docker exec mysql-ccrc mysqldump -u root -p evaluate_db > "C:\Users\Administrator\Development\evaluation\backups\evaluate_db_full_backup_$(Get-Date -Format 'yyyyMMdd_HHmmss').sql"
```

### 恢复备份（如果需要）
```powershell
# 恢复数据库
Get-Content "C:\Users\Administrator\Development\evaluation\backups\evaluate_db_full_backup_20251012_132500.sql" | docker exec -i mysql-ccrc mysql -u root -p evaluate_db
```

---

## 性能优化建议

### 1. 查看容器资源使用
```powershell
# 查看容器资源使用情况
docker stats mysql-ccrc --no-stream
```

### 2. 检查MySQL慢查询
```powershell
# 查看慢查询日志
docker exec mysql-ccrc mysql -u root -p -e "SHOW VARIABLES LIKE 'slow_query%';"
```

---

## 网络访问配置

如果需要从宿主机直接访问MySQL（不通过Docker exec）:

### 1. 检查端口映射
```powershell
# 查看容器端口映射
docker port mysql-ccrc
```

### 2. 使用宿主机MySQL客户端
```powershell
# 如果端口映射为 3306:3306
mysql -h 127.0.0.1 -P 3306 -u root -p --default-character-set=utf8mb4 evaluate_db < "C:\Users\Administrator\Development\evaluation\update_steps_2_to_5.sql"
```

---

## 监控和日志

### 查看容器日志
```powershell
# 实时查看容器日志
docker logs -f mysql-ccrc

# 查看最近50行日志
docker logs mysql-ccrc --tail 50

# 查看特定时间段的日志
docker logs mysql-ccrc --since "2025-10-12T13:00:00"
```

### 进入容器Shell调试
```powershell
# 进入容器bash
docker exec -it mysql-ccrc bash

# 在容器内查看MySQL进程
ps aux | grep mysql

# 查看MySQL数据目录
ls -la /var/lib/mysql
```

---

## 推荐执行流程

**最佳实践步骤**:

```powershell
# 1. 检查环境
docker ps | Select-String "mysql-ccrc"

# 2. 创建备份目录
New-Item -ItemType Directory -Force -Path "C:\Users\Administrator\Development\evaluation\backups"

# 3. 备份数据库
docker exec mysql-ccrc mysqldump -u root -p evaluate_db > "C:\Users\Administrator\Development\evaluation\backups\evaluate_db_$(Get-Date -Format 'yyyyMMdd_HHmmss').sql"

# 4. 执行更新脚本（推荐使用自动化脚本）
.\execute_update.ps1

# 5. 验证结果
docker exec -it mysql-ccrc mysql -u root -p evaluate_db -e "
SELECT ms.step_order, ms.step_name, COUNT(sa.id) as algorithm_count
FROM model_step ms
LEFT JOIN step_algorithm sa ON ms.id = sa.step_id
WHERE ms.model_id = (SELECT id FROM evaluation_model WHERE model_code = 'STANDARD_MODEL' LIMIT 1)
AND ms.step_order BETWEEN 2 AND 5
GROUP BY ms.id, ms.step_order, ms.step_name
ORDER BY ms.step_order;"
```

---

## 附录：有用的Docker命令

```powershell
# 查看容器详细信息
docker inspect mysql-ccrc

# 查看容器网络配置
docker inspect mysql-ccrc | Select-String "IPAddress"

# 查看容器挂载的卷
docker inspect mysql-ccrc | Select-String "Mounts" -Context 5

# 重启容器
docker restart mysql-ccrc

# 停止容器
docker stop mysql-ccrc

# 启动容器
docker start mysql-ccrc

# 查看容器环境变量
docker exec mysql-ccrc env | Select-String "MYSQL"
```

---

**文档版本**: 1.0  
**最后更新**: 2025-10-12  
**适用环境**: Docker MySQL 容器 (mysql-ccrc)  
**操作系统**: Windows + PowerShell
