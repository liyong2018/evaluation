# 标准减灾能力评估模型 - 步骤2至5算法配置更新

## 📦 更新包概述

本更新包包含标准减灾能力评估模型（STANDARD_MODEL）的**步骤2至步骤5**的完整算法配置，共计**40条算法记录**。

### 更新内容
- **步骤2**: 属性向量归一化（8个算法）
- **步骤3**: 定权计算（16个算法）
- **步骤4**: 优劣解计算（8个算法）
- **步骤5**: 能力值计算与分级（8个算法）

### 环境要求
- **数据库**: MySQL 8.0+ (Docker容器: mysql-ccrc)
- **字符集**: UTF-8 (utf8mb4)
- **操作系统**: Windows + PowerShell
- **数据库名**: evaluate_db

---

## 🚀 快速开始

### 选项1: 自动化脚本（推荐）⭐
```powershell
# 一键执行
.\execute_update.ps1
```

### 选项2: 手动三步法
```powershell
# 步骤1: 复制文件
docker cp "C:\Users\Administrator\Development\evaluation\update_steps_2_to_5.sql" mysql-ccrc:/tmp/update_steps_2_to_5.sql

# 步骤2: 执行脚本（输入密码）
docker exec -it mysql-ccrc mysql -u root -p --default-character-set=utf8mb4 evaluate_db -e "source /tmp/update_steps_2_to_5.sql"

# 步骤3: 清理临时文件
docker exec mysql-ccrc rm /tmp/update_steps_2_to_5.sql
```

### 快速验证
```powershell
docker exec -it mysql-ccrc mysql -u root -p evaluate_db -e "SELECT ms.step_order, COUNT(sa.id) as count FROM model_step ms LEFT JOIN step_algorithm sa ON ms.id = sa.step_id WHERE ms.step_order BETWEEN 2 AND 5 GROUP BY ms.id ORDER BY ms.step_order;"
```

**预期结果**: 步骤2(8个)、步骤3(16个)、步骤4(8个)、步骤5(8个)

---

## 📚 文档导航

### 1. 快速入门文档 ⚡
- **[快速启动指南-Docker.md](快速启动指南-Docker.md)** (171行)
  - 最快速的执行方法
  - 常见问题快速解决
  - 一键命令集合

### 2. 执行指南文档 📖
- **[Docker环境执行指南.md](Docker环境执行指南.md)** (473行)
  - 4种详细执行方法
  - 完整的故障排除指南
  - Docker容器操作手册
  - 数据备份与恢复

### 3. 更新说明文档 📋
- **[更新步骤2-5执行说明.md](更新步骤2-5执行说明.md)** (308行)
  - 多种执行方式（MySQL命令行、PowerShell、Workbench）
  - 执行前后检查清单
  - 特殊标记说明（@NORMALIZE、@TOPSIS_POSITIVE、@GRADE）
  - 权重变量说明

### 4. 算法公式文档 📐
- **[算法公式参考.md](算法公式参考.md)** (603行)
  - 完整的算法公式手册
  - 五个步骤的详细公式推导
  - 数据流转示例（含实际计算）
  - Excel原始公式对照
  - 常见问题解答

### 5. 核心文件 💾
- **[update_steps_2_to_5.sql](update_steps_2_to_5.sql)** (208行)
  - UTF-8编码的SQL更新脚本
  - 包含40条算法配置
  - 自动验证和统计功能

- **[execute_update.ps1](execute_update.ps1)** (132行)
  - PowerShell自动化脚本
  - 5步自动执行流程
  - 彩色输出和进度提示

---

## 📊 算法配置详情

### 步骤2: 属性向量归一化（8个算法）
对8个二级指标进行向量归一化处理
```
公式: 归一化值 = 原始值 / SQRT(SUMSQ(所有值))
```
- 队伍管理能力归一化
- 风险评估能力归一化
- 财政投入能力归一化
- 物资储备能力归一化
- 医疗保障能力归一化
- 自救互救能力归一化
- 公众避险能力归一化
- 转移安置能力归一化

### 步骤3: 定权计算（16个算法）
#### 第一部分：一级指标定权（8个）
```
公式: 定权值 = 归一化值 × 二级权重
```

#### 第二部分：综合定权（8个）
```
公式: 综合定权值 = 归一化值 × 一级权重 × 二级权重
```

### 步骤4: 优劣解计算（8个算法）
基于TOPSIS方法计算距离
```
正理想解: SQRT(Σ(最大值-当前值)²)
负理想解: SQRT(Σ(最小值-当前值)²)
```
- 灾害管理能力优/差解
- 灾害备灾能力优/差解
- 自救转移能力优/差解
- 综合减灾能力优/差解

### 步骤5: 能力值计算与分级（8个算法）
#### 能力值计算（4个）
```
公式: 能力值 = 差解 / (差解 + 优解)
```

#### 能力分级（4个）
基于均值(μ)和标准差(σ)进行五级分类
- **强**: value ≥ μ+1.5σ
- **较强**: μ+0.5σ ≤ value < μ+1.5σ
- **中等**: μ-0.5σ ≤ value < μ+0.5σ
- **较弱**: μ-1.5σ ≤ value < μ-0.5σ
- **弱**: value < μ-1.5σ

---

## ⚙️ 技术细节

### 特殊标记说明
脚本使用以下特殊标记，由后端服务识别并处理：

#### @NORMALIZE
- **用途**: 向量归一化
- **示例**: `@NORMALIZE:teamManagement`
- **处理**: 计算所有区域的平方和平方根，为每个区域生成归一化值

#### @TOPSIS_POSITIVE / @TOPSIS_NEGATIVE
- **用途**: TOPSIS优劣解计算
- **示例**: `@TOPSIS_POSITIVE:teamManagementWeighted,riskAssessmentWeighted`
- **处理**: 找到最大值/最小值，计算欧氏距离

#### @GRADE
- **用途**: 基于统计的分级
- **示例**: `@GRADE:disasterMgmtScore`
- **处理**: 计算均值和标准差，应用分级规则

### 权重变量
权重从 `indicator_weight` 表动态获取：

**一级指标权重**:
- weight_DISASTER_MANAGEMENT (灾害管理能力)
- weight_DISASTER_PREPAREDNESS (灾害备灾能力)
- weight_SELF_RESCUE_TRANSFER (自救转移能力)

**二级指标权重**:
- weight_TEAM_MANAGEMENT (队伍管理能力)
- weight_RISK_ASSESSMENT (风险评估能力)
- weight_FINANCIAL_INPUT (财政投入能力)
- weight_MATERIAL_RESERVE (物资储备能力)
- weight_MEDICAL_SUPPORT (医疗保障能力)
- weight_SELF_RESCUE (自救互救能力)
- weight_PUBLIC_AVOIDANCE (公众避险能力)
- weight_RELOCATION_CAPACITY (转移安置能力)

---

## 🔍 执行前检查

### 1. Docker容器状态
```powershell
docker ps --filter "name=mysql-ccrc"
```

### 2. 数据库连接
```powershell
docker exec -it mysql-ccrc mysql -u root -p -e "SELECT VERSION();"
```

### 3. 模型配置
```powershell
docker exec -it mysql-ccrc mysql -u root -p evaluate_db -e "SELECT * FROM evaluation_model WHERE model_code = 'STANDARD_MODEL';"
```

### 4. 步骤1算法（应该是8个）
```powershell
docker exec -it mysql-ccrc mysql -u root -p evaluate_db -e "SELECT COUNT(*) FROM step_algorithm WHERE step_id = (SELECT id FROM model_step WHERE step_order = 1 LIMIT 1);"
```

---

## ✅ 执行后验证

### 验证算法总数
```powershell
docker exec -it mysql-ccrc mysql -u root -p evaluate_db -e "SELECT COUNT(*) as total FROM step_algorithm;"
```
**预期**: 48个算法（步骤1的8个 + 步骤2-5的40个）

### 验证各步骤算法数
```powershell
docker exec -it mysql-ccrc mysql -u root -p evaluate_db -e "
SELECT 
    ms.step_order as '步骤',
    ms.step_name as '名称',
    COUNT(sa.id) as '算法数'
FROM model_step ms
LEFT JOIN step_algorithm sa ON ms.id = sa.step_id
WHERE ms.model_id = (SELECT id FROM evaluation_model WHERE model_code = 'STANDARD_MODEL' LIMIT 1)
GROUP BY ms.id
ORDER BY ms.step_order;
"
```

### 检查中文编码
```powershell
docker exec -it mysql-ccrc mysql -u root -p evaluate_db -e "
SELECT algorithm_name, description 
FROM step_algorithm 
WHERE step_id = (SELECT id FROM model_step WHERE step_order = 2 LIMIT 1) 
LIMIT 3;
"
```

---

## 💾 数据备份

### 执行前备份（强烈推荐）
```powershell
# 创建备份目录
New-Item -ItemType Directory -Force -Path ".\backups"

# 备份整个数据库
docker exec mysql-ccrc mysqldump -u root -p evaluate_db > ".\backups\evaluate_db_$(Get-Date -Format 'yyyyMMdd_HHmmss').sql"
```

### 仅备份算法表
```powershell
docker exec mysql-ccrc mysqldump -u root -p evaluate_db step_algorithm > ".\backups\step_algorithm_backup.sql"
```

### 恢复备份
```powershell
Get-Content ".\backups\backup_file.sql" | docker exec -i mysql-ccrc mysql -u root -p evaluate_db
```

---

## 🐛 故障排除

### 问题1: 容器未运行
```powershell
docker start mysql-ccrc
Start-Sleep -Seconds 15
docker ps | Select-String "mysql-ccrc"
```

### 问题2: 权限不足
确保使用root用户或具有足够权限的用户

### 问题3: 中文乱码
脚本已包含字符集设置：
```sql
SET NAMES utf8mb4;
SET CHARACTER SET utf8mb4;
```

### 问题4: 找不到模型
确保标准模型已创建：
```sql
SELECT * FROM evaluation_model WHERE model_code = 'STANDARD_MODEL';
```

### 问题5: 外键约束错误
确保 `model_step` 表中存在步骤记录

---

## 📈 后续步骤

更新完成后，您可以：

1. **测试评估流程**
   - 通过前端界面执行完整评估
   - 或使用API调用评估接口

2. **查看执行记录**
   - 检查 `model_execution_record` 表
   - 查看 `step_execution_result` 表

3. **调整权重配置**
   - 在 `indicator_weight` 表中修改权重值
   - 无需修改代码即可生效

4. **自定义算法**
   - 修改 `step_algorithm` 表中的 `ql_expression` 字段
   - 在不修改代码的情况下调整算法逻辑

---

## 📞 获取帮助

### 查看日志
```powershell
# 容器日志
docker logs mysql-ccrc --tail 50

# 应用日志
Get-Content logs\application.log -Tail 50
```

### 调试模式
```powershell
# 进入容器
docker exec -it mysql-ccrc bash

# 连接MySQL
mysql -u root -p evaluate_db
```

### 相关资源
- MySQL官方文档: https://dev.mysql.com/doc/
- Docker官方文档: https://docs.docker.com/
- 项目WARP文档: `WARP.md`
- QLExpress指南: `QLExpress实施指南.md`

---

## 📋 文件清单

### 核心文件
- ✅ `update_steps_2_to_5.sql` (208行) - SQL更新脚本
- ✅ `execute_update.ps1` (132行) - 自动化执行脚本

### 文档文件
- ✅ `快速启动指南-Docker.md` (171行) - 快速入门
- ✅ `Docker环境执行指南.md` (473行) - 详细指南
- ✅ `更新步骤2-5执行说明.md` (308行) - 执行说明
- ✅ `算法公式参考.md` (603行) - 公式手册
- ✅ `步骤2-5更新README.md` (本文件) - 总览文档

### 辅助文件
- 📁 `backups/` - 备份目录（需手动创建）
- 📄 `temp_update_step1.sql` - 步骤1配置参考

---

## 🎯 预期成果

执行本更新后，系统将具备：

- ✅ **完整的评估算法**: 步骤1-5共48条算法
- ✅ **数据归一化能力**: 消除不同指标的量纲影响
- ✅ **权重化评估**: 支持自定义权重配置
- ✅ **TOPSIS优劣解**: 科学的多准则决策分析
- ✅ **智能分级**: 基于统计的五级能力分类
- ✅ **零中文乱码**: 完整的UTF-8支持
- ✅ **可配置性**: 无需修改代码即可调整算法

---

## 📊 统计信息

- **SQL脚本行数**: 208行
- **算法总数**: 40条
- **步骤覆盖**: 2-5（共4个步骤）
- **文档总页数**: 约1,900行
- **支持的评估指标**: 8个二级指标
- **生成的输出变量**: 40+个
- **支持的分级级别**: 5级（强/较强/中等/较弱/弱）

---

**更新版本**: 1.0  
**更新日期**: 2025-10-12  
**适用模型**: STANDARD_MODEL (标准减灾能力评估模型)  
**兼容性**: MySQL 8.0+ / Docker环境  
**字符编码**: UTF-8 (utf8mb4)  
**维护状态**: ✅ 活跃维护

---

## 💡 提示

- 推荐使用 `execute_update.ps1` 脚本，它会自动处理所有步骤
- 执行前务必备份数据库
- 如遇到问题，请先查看 `Docker环境执行指南.md`
- 验证结果时注意检查中文字符是否正常显示
- 更新后建议立即进行一次完整的评估测试

**祝您使用顺利！** 🎉
