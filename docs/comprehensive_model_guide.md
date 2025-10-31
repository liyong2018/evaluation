# 综合减灾能力评估模型使用指南

## 一、模型概述

综合减灾能力评估模型用于融合**乡镇评估模型**和**社区-乡镇评估模型**的评估结果，进行综合减灾能力评估。

### 1.1 数据来源

该模型不直接使用原始数据，而是从 `evaluation_result` 表中读取之前执行的评估结果：

- **乡镇评估模型结果**（modelId=3）：提供乡镇级别的3个一级指标
- **社区-乡镇评估模型结果**（modelId=8）：提供社区聚合后的3个一级指标

### 1.2 评估指标体系

综合模型使用6个一级指标：

| 指标类别 | 指标名称 | 一级权重 | 二级权重 | 来源模型 |
|---------|---------|---------|---------|---------|
| 乡镇 | 灾害管理能力 | 0.53 | 0.33 | 乡镇评估模型 |
| 乡镇 | 灾害备灾能力 | 0.53 | 0.32 | 乡镇评估模型 |
| 乡镇 | 自救转移能力 | 0.53 | 0.35 | 乡镇评估模型 |
| 社区 | 灾害管理能力 | 0.47 | 0.32 | 社区-乡镇评估模型 |
| 社区 | 灾害备灾能力 | 0.47 | 0.31 | 社区-乡镇评估模型 |
| 社区 | 自救转移能力 | 0.47 | 0.37 | 社区-乡镇评估模型 |

## 二、安装配置

### 2.1 执行数据库脚本

```bash
# 连接到数据库
mysql -h<host> -P<port> -u<username> -p<password> <database>

# 执行配置脚本
source /home/user/evaluation/sql/comprehensive_model_setup.sql
```

### 2.2 验证模型创建

```sql
-- 查看模型配置
SELECT
    m.id AS model_id,
    m.model_name,
    s.step_order,
    s.step_name,
    COUNT(a.id) AS algorithm_count
FROM evaluation_model m
LEFT JOIN model_step s ON m.id = s.model_id
LEFT JOIN step_algorithm a ON s.id = a.step_id
WHERE m.model_code = 'COMPREHENSIVE_MODEL'
GROUP BY m.id, m.model_name, s.step_order, s.step_name
ORDER BY s.step_order;
```

期待输出：
```
+-----------+----------------------+------------+-----------------------+-----------------+
| model_id  | model_name           | step_order | step_name             | algorithm_count |
+-----------+----------------------+------------+-----------------------+-----------------+
| X         | 综合减灾能力评估模型  | 1          | 数据融合              | 6               |
| X         | 综合减灾能力评估模型  | 2          | 属性向量归一化        | 6               |
| X         | 综合减灾能力评估模型  | 3          | 二级定权              | 6               |
| X         | 综合减灾能力评估模型  | 4          | TOPSIS优劣解距离      | 12              |
| X         | 综合减灾能力评估模型  | 5          | 能力评估              | 4               |
+-----------+----------------------+------------+-----------------------+-----------------+
```

## 三、执行流程

### 3.1 前置条件

在执行综合评估模型之前，**必须**先执行以下两个模型：

1. **乡镇评估模型**（modelId=3）
2. **社区-乡镇评估模型**（modelId=8）

### 3.2 执行顺序

```
步骤1: 执行乡镇评估模型
    ↓
步骤2: 执行社区-乡镇评估模型
    ↓
步骤3: 执行综合评估模型
```

### 3.3 API调用示例

```bash
# 1. 先执行乡镇评估模型
curl -X POST http://localhost:8081/api/evaluation/execute \
  -H "Content-Type: application/json" \
  -d '{
    "modelId": 3,
    "regionCodes": ["511425001", "511425102", "511425108"],
    "weightConfigId": 1
  }'

# 2. 再执行社区-乡镇评估模型
curl -X POST http://localhost:8081/api/evaluation/execute \
  -H "Content-Type: application/json" \
  -d '{
    "modelId": 8,
    "regionCodes": ["330402001001", "330402001002", ...],
    "weightConfigId": 1
  }'

# 3. 最后执行综合评估模型
curl -X POST http://localhost:8081/api/evaluation/execute \
  -H "Content-Type: application/json" \
  -d '{
    "modelId": <综合模型ID>,
    "regionCodes": ["511425001", "511425102", "511425108"],
    "weightConfigId": 1
  }'
```

## 四、计算步骤详解

### 4.1 步骤1：数据融合

从 `evaluation_result` 表加载前置模型的评估结果：

```sql
-- 示例：加载乡镇评估模型的灾害管理能力分值
SELECT management_capability_score
FROM evaluation_result
WHERE evaluation_model_id = 3
  AND region_code = '511425001'
ORDER BY id DESC
LIMIT 1;
```

**算法配置示例：**
```
qlExpression: @LOAD_EVAL_RESULT:modelId=3,field=management_capability_score
outputParam: TOWNSHIP_MGMT_CAPABILITY
```

### 4.2 步骤2：属性向量归一化

对6个指标进行向量归一化：

**公式：**
```
归一化值 = 本乡镇指标值 / SQRT(SUMSQ(全部乡镇指标值))
```

**算法配置示例：**
```
qlExpression: @NORMALIZE:TOWNSHIP_MGMT_CAPABILITY
outputParam: TOWNSHIP_MGMT_NORM
```

### 4.3 步骤3：二级定权

对归一化后的指标进行加权：

**公式：**
```
定权值 = 归一化值 × 一级权重 × 二级权重
```

**算法配置示例：**
```
# 乡镇灾害管理能力定权
qlExpression: TOWNSHIP_MGMT_NORM * 0.53 * 0.33
outputParam: TOWNSHIP_MGMT_WEIGHTED
```

### 4.4 步骤4：TOPSIS优劣解距离

计算每个能力维度的优解和劣解距离：

**优解距离公式：**
```
D+ = SQRT(Σ(max_i - current_i)²)
```

**劣解距离公式：**
```
D- = SQRT(Σ(min_i - current_i)²)
```

**能力得分公式：**
```
Score = D- / (D+ + D-)
```

**算法配置示例：**
```
# 灾害管理能力（合并乡镇和社区指标）
@TOPSIS_POSITIVE:TOWNSHIP_MGMT_WEIGHTED,COMMUNITY_MGMT_WEIGHTED
@TOPSIS_NEGATIVE:TOWNSHIP_MGMT_WEIGHTED,COMMUNITY_MGMT_WEIGHTED
managementScore = MGMT_NEGATIVE_DISTANCE / (MGMT_NEGATIVE_DISTANCE + MGMT_POSITIVE_DISTANCE)
```

### 4.5 步骤5：能力评估分级

根据均值和标准差进行五级分类：

**分级规则：**

- 如果 μ ≤ 0.5σ（3级分类）：
  - value ≥ μ + 1.5σ → 强
  - value ≥ μ + 0.5σ → 较强
  - 其他 → 中等

- 如果 0.5σ < μ ≤ 1.5σ（4级分类）：
  - value ≥ μ + 1.5σ → 强
  - value ≥ μ + 0.5σ → 较强
  - value ≥ μ - 0.5σ → 中等
  - 其他 → 较弱

- 如果 μ > 1.5σ（5级分类）：
  - value ≥ μ + 1.5σ → 强
  - value ≥ μ + 0.5σ → 较强
  - value ≥ μ - 0.5σ → 中等
  - value ≥ μ - 1.5σ → 较弱
  - 其他 → 弱

**算法配置示例：**
```
qlExpression: @GRADE:managementScore
outputParam: managementGrade
```

## 五、输出结果

### 5.1 输出字段

综合评估模型输出8个字段（4个能力维度 × 2种输出）：

| 字段名 | 类型 | 说明 |
|--------|------|------|
| managementScore | Double | 灾害管理能力得分（0-1） |
| managementGrade | String | 灾害管理能力等级 |
| preparednessScore | Double | 灾害备灾能力得分（0-1） |
| preparednessGrade | String | 灾害备灾能力等级 |
| rescueScore | Double | 自救转移能力得分（0-1） |
| rescueGrade | String | 自救转移能力等级 |
| comprehensiveScore | Double | 综合减灾能力得分（0-1） |
| comprehensiveGrade | String | 综合减灾能力等级 |

### 5.2 结果示例

```json
{
  "modelId": 9,
  "modelName": "综合减灾能力评估模型",
  "executionTime": "2025-10-31T13:30:00",
  "tableData": [
    {
      "regionCode": "511425001",
      "regionName": "青竹街道",
      "managementScore": 0.00000000,
      "managementGrade": "较弱",
      "preparednessScore": 0.43653567,
      "preparednessGrade": "较强",
      "rescueScore": 0.05301789,
      "rescueGrade": "较弱",
      "comprehensiveScore": 0.29859052,
      "comprehensiveGrade": "中等"
    },
    {
      "regionCode": "511425102",
      "regionName": "瑞峰镇",
      "managementScore": 0.76573918,
      "managementGrade": "强",
      "preparednessScore": 0.66860435,
      "preparednessGrade": "强",
      "rescueScore": 0.52716954,
      "rescueGrade": "较强",
      "comprehensiveScore": 0.63767836,
      "comprehensiveGrade": "强"
    }
  ],
  "success": true
}
```

## 六、常见问题

### Q1: 为什么综合模型执行失败？

**A:** 检查前置条件：
1. 确认已执行乡镇评估模型（modelId=3）
2. 确认已执行社区-乡镇评估模型（modelId=8）
3. 确认 `evaluation_result` 表中有对应的记录
4. 确认地区代码匹配

### Q2: 如何查看已有的评估结果？

```sql
-- 查看乡镇评估模型结果
SELECT region_code, region_name,
       management_capability_score,
       support_capability_score,
       self_rescue_capability_score
FROM evaluation_result
WHERE evaluation_model_id = 3
ORDER BY id DESC;

-- 查看社区-乡镇评估模型结果
SELECT region_code, region_name,
       management_capability_score,
       support_capability_score,
       self_rescue_capability_score
FROM evaluation_result
WHERE evaluation_model_id = 8
ORDER BY id DESC;
```

### Q3: 综合模型可以单独为某个乡镇执行吗？

**A:** 可以，但需要注意：
- 该乡镇必须在两个前置模型中都有评估结果
- 分级时如果只有一个乡镇，会使用绝对值分级（而非统计分级）

### Q4: 如何更新权重配置？

修改 SQL 脚本中步骤3的权重值，然后重新执行：

```sql
-- 例如：修改乡镇灾害管理能力的权重
UPDATE step_algorithm
SET ql_expression = 'TOWNSHIP_MGMT_NORM * 0.55 * 0.35'  -- 修改权重
WHERE step_id = <步骤3的ID>
  AND algorithm_code = 'WEIGHT_TOWNSHIP_MGMT';
```

## 七、技术架构

### 7.1 特殊算法标记

综合模型使用以下特殊算法：

| 标记 | 功能 | 参数格式 |
|------|------|---------|
| @LOAD_EVAL_RESULT | 加载评估结果 | modelId=X,field=xxx_score |
| @NORMALIZE | 向量归一化 | 指标字段名 |
| @TOPSIS_POSITIVE | 计算优解距离 | 字段1,字段2,... |
| @TOPSIS_NEGATIVE | 计算劣解距离 | 字段1,字段2,... |
| @GRADE | 能力分级 | 分数字段名 |

### 7.2 数据流转

```
evaluation_result (modelId=3, modelId=8)
    ↓ (@LOAD_EVAL_RESULT)
原始能力分值 (6个指标)
    ↓ (@NORMALIZE)
归一化值 (6个指标)
    ↓ (权重计算)
定权值 (6个指标)
    ↓ (@TOPSIS_POSITIVE, @TOPSIS_NEGATIVE)
优劣解距离 + 能力得分 (4个维度)
    ↓ (@GRADE)
能力等级 (4个维度)
```

## 八、联系支持

如有问题，请联系技术支持或查看项目文档：
- GitHub: https://github.com/your-repo/evaluation
- 文档: /docs目录
