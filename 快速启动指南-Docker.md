# Docker环境快速启动指南

## 🚀 一键执行（推荐）

### 方法1: 运行自动化脚本
```powershell
# 在项目根目录下执行
.\execute_update.ps1
```

如果遇到执行策略限制：
```powershell
powershell -ExecutionPolicy Bypass -File .\execute_update.ps1
```

---

## 📋 手动执行（三步法）

### 第1步: 复制文件到容器
```powershell
docker cp "C:\Users\Administrator\Development\evaluation\update_steps_2_to_5.sql" mysql-ccrc:/tmp/update_steps_2_to_5.sql
```

### 第2步: 执行SQL脚本
```powershell
docker exec -it mysql-ccrc mysql -u root -p --default-character-set=utf8mb4 evaluate_db -e "source /tmp/update_steps_2_to_5.sql"
```
**输入MySQL root密码后回车**

### 第3步: 清理临时文件
```powershell
docker exec mysql-ccrc rm /tmp/update_steps_2_to_5.sql
```

---

## ✅ 快速验证

### 验证算法数量（复制执行）
```powershell
docker exec -it mysql-ccrc mysql -u root -p evaluate_db -e "SELECT ms.step_order as Step, ms.step_name as Name, COUNT(sa.id) as Algorithms FROM model_step ms LEFT JOIN step_algorithm sa ON ms.id = sa.step_id WHERE ms.model_id = (SELECT id FROM evaluation_model WHERE model_code = 'STANDARD_MODEL' LIMIT 1) AND ms.step_order BETWEEN 2 AND 5 GROUP BY ms.id, ms.step_order, ms.step_name ORDER BY ms.step_order;"
```

**预期结果**:
```
+------+------------------+------------+
| Step | Name             | Algorithms |
+------+------------------+------------+
|    2 | 属性向量归一化   |          8 |
|    3 | 定权计算         |         16 |
|    4 | 优劣解计算       |          8 |
|    5 | 能力值计算与分级 |          8 |
+------+------------------+------------+
```

---

## 🔧 常见问题快速解决

### 问题: 容器未运行
```powershell
# 启动容器
docker start mysql-ccrc

# 等待15秒让MySQL完全启动
Start-Sleep -Seconds 15

# 验证容器状态
docker ps | Select-String "mysql-ccrc"
```

### 问题: 中文乱码
```powershell
# 检查字符集
docker exec mysql-ccrc mysql -u root -p -e "SHOW VARIABLES LIKE 'character%';"
```
确保 `character_set_database` 和 `character_set_server` 是 `utf8mb4`

### 问题: 密码输入无反应
使用环境变量方式（不推荐生产环境）：
```powershell
$env:MYSQL_PWD="你的密码"
docker exec -i mysql-ccrc mysql -u root --default-character-set=utf8mb4 evaluate_db < "C:\Users\Administrator\Development\evaluation\update_steps_2_to_5.sql"
$env:MYSQL_PWD=""
```

---

## 📊 预执行检查

### 1. 检查容器状态
```powershell
docker ps --filter "name=mysql-ccrc"
```

### 2. 检查数据库
```powershell
docker exec -it mysql-ccrc mysql -u root -p -e "SHOW DATABASES LIKE 'evaluate_db';"
```

### 3. 检查现有配置
```powershell
docker exec -it mysql-ccrc mysql -u root -p evaluate_db -e "SELECT COUNT(*) FROM step_algorithm;"
```

---

## 💾 备份建议

### 执行前备份（推荐）
```powershell
# 创建备份目录
New-Item -ItemType Directory -Force -Path "C:\Users\Administrator\Development\evaluation\backups"

# 备份数据库
docker exec mysql-ccrc mysqldump -u root -p evaluate_db > "C:\Users\Administrator\Development\evaluation\backups\evaluate_db_$(Get-Date -Format 'yyyyMMdd_HHmmss').sql"
```

### 恢复备份
```powershell
Get-Content "备份文件路径.sql" | docker exec -i mysql-ccrc mysql -u root -p evaluate_db
```

---

## 📁 相关文档

- **`update_steps_2_to_5.sql`**: SQL更新脚本（208行）
- **`更新步骤2-5执行说明.md`**: 详细执行说明（308行）
- **`算法公式参考.md`**: 完整算法公式手册（603行）
- **`Docker环境执行指南.md`**: Docker环境详细指南（473行）
- **`execute_update.ps1`**: 自动化执行脚本（132行）

---

## 🎯 执行流程总览

```
1. 检查Docker容器 (mysql-ccrc) ✓
2. 检查SQL文件存在性 ✓
3. 复制SQL到容器 (/tmp) ✓
4. 执行SQL脚本 (utf8mb4编码) ✓
5. 清理临时文件 ✓
6. 验证更新结果 ✓
```

---

## ⚡ 超快速执行（一行命令）

```powershell
docker cp "C:\Users\Administrator\Development\evaluation\update_steps_2_to_5.sql" mysql-ccrc:/tmp/update_steps_2_to_5.sql && docker exec -it mysql-ccrc mysql -u root -p --default-character-set=utf8mb4 evaluate_db -e "source /tmp/update_steps_2_to_5.sql" && docker exec mysql-ccrc rm /tmp/update_steps_2_to_5.sql
```

**注意**: 需要在第一个命令执行后输入MySQL密码

---

## 📞 需要帮助？

1. 查看详细文档: `Docker环境执行指南.md`
2. 查看算法说明: `算法公式参考.md`
3. 查看容器日志: `docker logs mysql-ccrc --tail 50`
4. 进入容器调试: `docker exec -it mysql-ccrc bash`

---

**更新日期**: 2025-10-12  
**适用环境**: Windows + Docker (mysql-ccrc容器)  
**数据库**: MySQL 8.0 (evaluate_db)
