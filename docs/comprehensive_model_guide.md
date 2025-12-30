# 综合减灾能力评估模型使用指南

## 一、模型概述

综合减灾能力评估模型是基于**乡镇减灾能力评估模型（模型ID=3）**和**社区-乡镇减灾能力评估模型（模型ID=8）**的结果，进行二次融合评估的高级模型。

### 数据来源
- 从 `evaluation_result` 表中查询乡镇和社区评估模型的结果数据
- 提取三个一级指标：灾害管理能力、灾害备灾能力、自救转移能力
- 每个指标分为乡镇层面和社区层面，共6个数据点

### 评估流程

```
步骤1：数据融合
       └─ 从 evaluation_result 表加载乡镇和社区评估结果（6个指标）

步骤2：属性向量归一化
       └─ 对6个指标进行向量归一化：value / SQRT(SUM(all_values²))

步骤3：二级定权
       └─ 归一化值 × 一级权重 × 二级权重

步骤4：TOPSIS优劣解距离
       └─ 计算正理想解距离（D+）和负理想解距离（D-）

步骤5：能力评估与分级
       └─ 能力值 = D- / (D+ + D-)
       └─ 根据均值和标准差进行五级分类（强、较强、中等、较弱、弱）
```

## 二、权重配置

### 一级权重（地域层级）
- **乡镇层级**: 0.53
- **社区层级**: 0.47

### 二级权重（能力类型）

| 能力类型 | 乡镇二级权重 | 社区二级权重 |
|---------|------------|------------|
| 灾害管理能力 | 0.33 | 0.32 |
| 灾害备灾能力 | 0.32 | 0.31 |
| 自救转移能力 | 0.35 | 0.37 |

### 综合权重计算公式

以乡镇灾害管理能力为例：
```
定权值 = 归一化值 × 0.53（一级权重） × 0.33（二级权重）
```

## 三、安装与配置

### 3.1 执行数据库脚本

如果是首次配置或需要重置模型，请执行对应的 SQL 脚本：

```bash
# 连接到数据库
mysql -h127.0.0.1 -P30314 -uroot -p123456 evaluate_db

# 执行配置脚本
source sql/comprehensive_model_setup.sql
```

### 3.2 验证模型创建

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

## 四、执行流程

### 4.1 前置条件

在执行综合评估模型之前，**必须**先执行以下两个模型，确保 `evaluation_result` 表中有数据：

1. **乡镇评估模型**（modelId=3）
2. **社区-乡镇评估模型**（modelId=8）

### 4.2 API 调用示例

```bash
# 1. 执行综合评估模型
curl -X POST http://localhost:8081/api/evaluation/execute-model \
  -H "Content-Type: application/json" \
  -d '{
    "modelId": 9,
    "regionCodes": ["511425001", "511425102"],
    "weightConfigId": 1
  }'
```

## 五、算法详解

### 1. 属性向量归一化
**公式**：`归一化值 = 原始值 / SQRT(SUM(所有乡镇该指标的原始值²))`

### 2. 定权计算
**公式**：`定权值 = 归一化值 × 一级权重 × 二级权重`

### 3. TOPSIS 优劣解算法
- **正理想解距离（D+）**: `SQRT(SUM((max_value - current_value)²))`
- **负理想解距离（D-）**: `SQRT(SUM((min_value - current_value)²))`
- **综合能力值**: `D- / (D+ + D-)`

### 4. 能力分级
根据均值（μ）和标准差（σ）进行分级（五级分类）：
- **强**: value ≥ μ + 1.5σ
- **较强**: value ≥ μ + 0.5σ
- **中等**: value ≥ μ - 0.5σ
- **较弱**: value ≥ μ - 1.5σ
- **弱**: value < μ - 1.5σ

## 六、常见问题 (FAQ)

**Q: 第一步数据融合没有数据？**
A: 确保已先运行模型3和模型8。可以使用 `sql/diagnose_comprehensive_model.sql` 进行诊断。

**Q: 综合能力值为0？**
A: 检查前置模型的结果是否已正确保存到 `evaluation_result` 表。

---
**最后更新**: 2025-12-30
