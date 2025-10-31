# 步骤1缺失13列数据诊断

## 问题描述

用户执行社区-乡镇评估模型时，步骤1应该计算13个社区指标，但这些列在结果表中没有显示。

### 预期行为
```
| regionCode    | regionName | 指标1 | 指标2 | 指标3 | ... | 指标13 |
|---------------|-----------|------|------|------|-----|--------|
| 330402001001  | 社区A     | 0.75 | 0.82 | 0.91 | ... | 0.68  |
| 330402001002  | 社区B     | 0.81 | 0.77 | 0.85 | ... | 0.72  |
```

### 实际行为
```
| regionCode    | regionName | townshipName |
|---------------|-----------|-------------|
| 330402001001  | 社区A     | 青竹街道     |
| 330402001002  | 社区B     | 青竹街道     |
```

---

## 根本原因

### 原因1：步骤1的算法执行失败
如果步骤1的算法表达式有错误，算法可能执行失败或返回null，导致没有输出。

### 原因2：输出参数未正确配置
如果步骤1的算法没有配置`output_param`，即使计算成功，也不会保存到结果中。

### 原因3：数据源缺失
如果社区数据表(`community_disaster_reduction_capacity`)中没有数据或缺少必需的字段，算法无法计算。

---

## 诊断步骤

### 步骤A：检查社区数据是否存在

```sql
-- 检查社区数据表
SELECT
    COUNT(*) AS total_records,
    COUNT(DISTINCT region_code) AS unique_regions
FROM community_disaster_reduction_capacity;

-- 查看前3条记录的所有字段
SELECT * FROM community_disaster_reduction_capacity LIMIT 3;

-- 检查特定社区是否存在
SELECT * FROM community_disaster_reduction_capacity
WHERE region_code IN ('330402001001', '330402001002', '330402001003');
```

**预期结果：**
- total_records > 0
- 能查询到具体的社区记录

**如果返回0条记录：**
- 说明数据库中没有社区数据，需要先导入数据
- 参考：`sql/archive/community_disaster_reduction_capacity.sql`

### 步骤B：检查步骤1的算法配置

```sql
-- 查看步骤1的所有算法
SELECT
    id,
    algorithm_code,
    algorithm_name,
    ql_expression,
    input_params,
    output_param,
    calculation_order,
    status
FROM step_algorithm
WHERE step_id = (
    SELECT id FROM model_step
    WHERE model_id = 8 AND step_order = 1
)
ORDER BY calculation_order;
```

**检查要点：**
1. `output_param` 不能为空
2. `ql_expression` 应该是有效的表达式
3. `status` 应该是 1（启用）
4. 应该有13个算法（对应13个指标）

**示例正确配置：**
```
algorithm_code: CALC_MGMT_1
algorithm_name: 灾害管理机构设立
ql_expression: IF(disaster_mgmt_org == "是", 1, 0)
output_param: MGMT_ORG_SCORE
status: 1
```

**常见错误：**
```
❌ output_param: NULL                    # 没有输出参数
❌ ql_expression: disaster_mgmt_org      # 变量不存在于community表
❌ status: 0                             # 算法被禁用
```

### 步骤C：验证算法表达式的变量名

```sql
-- 获取community表的所有列名
SHOW COLUMNS FROM community_disaster_reduction_capacity;

-- 或者
SELECT COLUMN_NAME, DATA_TYPE
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_NAME = 'community_disaster_reduction_capacity'
  AND TABLE_SCHEMA = 'evaluate_db'
ORDER BY ORDINAL_POSITION;
```

然后检查步骤1算法的`ql_expression`中引用的变量名是否与这些列名匹配。

**注意大小写和下划线：**
- 数据库列名：`disaster_mgmt_org`
- 算法表达式：`disaster_mgmt_org` ✓ 或 `disaster_Mgmt_Org` ✗

### 步骤D：检查最近的执行日志

重启应用后，执行模型并查看日志，寻找以下信息：

```
# 步骤1开始执行
INFO - 执行步骤: STEP_1 - 社区指标计算, order=1

# 为每个社区执行算法
INFO - 为地区 330402001001 执行非GRADE算法
DEBUG - 执行算法: CALC_MGMT_1 - 灾害管理机构设立
DEBUG - 算法 CALC_MGMT_1 执行结果: 1.0

# 步骤1执行完成
INFO - 步骤 STEP_1 执行完成
INFO - 步骤1 的输出参数: [灾害管理机构设立, 灾害管理预案编制, ...]
```

**如果看到错误：**
```
ERROR - 算法 CALC_MGMT_1 执行失败: 变量 disaster_mgmt_org 未定义
```
说明算法表达式中的变量名与数据库字段名不匹配。

---

## 解决方案

### 解决方案1：修复缺失的output_param

```sql
-- 示例：为算法添加output_param
UPDATE step_algorithm
SET output_param = 'MGMT_ORG_SCORE'
WHERE step_id = (SELECT id FROM model_step WHERE model_id = 8 AND step_order = 1)
  AND algorithm_code = 'CALC_MGMT_1'
  AND output_param IS NULL;
```

### 解决方案2：修复变量名不匹配

```sql
-- 示例：将表达式中的变量名改为正确的数据库字段名
UPDATE step_algorithm
SET ql_expression = 'IF(disaster_mgmt_org == "是", 1, 0)'
WHERE step_id = (SELECT id FROM model_step WHERE model_id = 8 AND step_order = 1)
  AND algorithm_code = 'CALC_MGMT_1';
```

### 解决方案3：启用被禁用的算法

```sql
-- 启用所有步骤1的算法
UPDATE step_algorithm
SET status = 1
WHERE step_id = (SELECT id FROM model_step WHERE model_id = 8 AND step_order = 1)
  AND status = 0;
```

### 解决方案4：重新创建算法配置

如果配置严重错误，建议重新创建：

```sql
-- 1. 删除现有算法（谨慎操作！）
DELETE FROM step_algorithm
WHERE step_id = (SELECT id FROM model_step WHERE model_id = 8 AND step_order = 1);

-- 2. 重新插入正确的算法配置
-- 参考 sql/migrations 中的初始化脚本
```

---

## 快速诊断脚本

将以下内容保存为 `quick_diagnose_step1.sql`：

```sql
-- ===== 快速诊断步骤1配置 =====

-- 1. 检查社区数据
SELECT '1. 社区数据检查' AS check_name;
SELECT COUNT(*) AS total_communities FROM community_disaster_reduction_capacity;

-- 2. 检查步骤1配置
SELECT '2. 步骤1算法配置' AS check_name;
SELECT
    COUNT(*) AS total_algorithms,
    SUM(CASE WHEN output_param IS NOT NULL THEN 1 ELSE 0 END) AS with_output,
    SUM(CASE WHEN output_param IS NULL THEN 1 ELSE 0 END) AS without_output,
    SUM(CASE WHEN status = 1 THEN 1 ELSE 0 END) AS enabled,
    SUM(CASE WHEN status = 0 THEN 1 ELSE 0 END) AS disabled
FROM step_algorithm
WHERE step_id = (SELECT id FROM model_step WHERE model_id = 8 AND step_order = 1);

-- 3. 列出所有步骤1算法
SELECT '3. 步骤1算法列表' AS check_name;
SELECT
    algorithm_code,
    algorithm_name,
    LEFT(ql_expression, 50) AS expression_preview,
    output_param,
    status
FROM step_algorithm
WHERE step_id = (SELECT id FROM model_step WHERE model_id = 8 AND step_order = 1)
ORDER BY calculation_order;

-- 4. 检查community表字段
SELECT '4. Community表字段' AS check_name;
SELECT COLUMN_NAME
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_NAME = 'community_disaster_reduction_capacity'
  AND TABLE_SCHEMA = DATABASE()
ORDER BY ORDINAL_POSITION;

-- 5. 检查最近的执行结果
SELECT '5. 最近执行结果' AS check_name;
SELECT
    er.id,
    er.region_code,
    er.region_name,
    er.created_at
FROM evaluation_result er
WHERE er.evaluation_model_id = 8
ORDER BY er.created_at DESC
LIMIT 5;
```

执行：
```bash
mysql -h192.168.15.203 -P30314 -uroot -p123456 evaluate_db < quick_diagnose_step1.sql
```

---

## 预期结果

### 修复后的正常日志

```
INFO - 执行步骤: STEP_1 - 社区指标计算, order=1
INFO - 为地区 330402001001 执行非GRADE算法
DEBUG - 执行算法: CALC_MGMT_1 - 灾害管理机构设立
DEBUG - 算法 CALC_MGMT_1 执行结果: 1.00000000
INFO - 为地区 330402001001 执行非GRADE算法完成，共 13 个输出
INFO - 步骤 STEP_1 执行完成
INFO - 步骤1 的输出参数: [灾害管理机构设立, 灾害管理预案编制, 应急演练开展, ...]
```

### 修复后的正常表格

```
| regionCode    | regionName | 灾害管理机构设立 | 灾害管理预案编制 | 应急演练开展 | ... |
|---------------|-----------|----------------|----------------|------------|-----|
| 330402001001  | 社区A     | 1.00000000     | 1.00000000     | 0.00000000 | ... |
| 330402001002  | 社区B     | 1.00000000     | 0.00000000     | 1.00000000 | ... |
```

---

## 相关文档

- [乡镇聚合问题分析](./TOWNSHIP_AGGREGATION_ISSUE_ANALYSIS.md)
- [诊断SQL脚本](./diagnose_township_aggregation.sql)
- [如何重启后端服务](./archive/如何重启后端服务.md)

---

**更新时间：** 2025-10-31
**文档版本：** 1.0
**作者：** Claude Code
