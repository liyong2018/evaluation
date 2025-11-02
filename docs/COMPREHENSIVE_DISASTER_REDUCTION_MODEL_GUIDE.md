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

## 三、数据样例

### 输入数据（来源于乡镇评估模型与社区评估模型结果表）

| 乡镇名称 | 乡镇灾害管理 | 乡镇灾害备灾 | 乡镇自救转移 | 社区灾害管理 | 社区灾害备灾 | 社区自救转移 |
|---------|------------|------------|------------|------------|------------|------------|
| 青竹街道 | 0.00000000 | 0.43653567 | 0.05301789 | 0.26780819 | 0.00000000 | 0.27561257 |
| 汉阳镇 | 0.31506616 | 0.19103905 | 0.53805925 | 0.26725477 | 0.30393406 | 0.35696948 |
| 瑞峰镇 | 0.76573918 | 0.66860435 | 0.52716954 | 0.34800932 | 0.81223286 | 0.50330358 |
| 西龙镇 | 0.04524090 | 0.13399391 | 0.07663073 | 1.00000000 | 0.22977831 | 0.37643990 |
| 高台镇 | 0.20980458 | 0.03925592 | 0.42902375 | 0.40430523 | 0.42251644 | 0.50006385 |
| 白果乡 | 0.04726732 | 0.18231692 | 0.10658940 | 0.55924398 | 0.39615613 | 0.59101631 |
| 罗波乡 | 0.44784560 | 0.32967688 | 0.52579097 | 0.49024527 | 0.49187803 | 0.23564336 |

### 输出结果（综合减灾能力评估）

| 乡镇名称 | 综合减灾能力值 | 综合分级 |
|---------|--------------|---------|
| 青竹街道 | 0.2536 | 较弱 |
| 汉阳镇 | 0.4195 | 中等 |
| 瑞峰镇 | 0.7628 | 强 |
| 西龙镇 | 0.3061 | 较弱 |
| 高台镇 | 0.3714 | 中等 |
| 白果乡 | 0.3105 | 较弱 |
| 罗波乡 | 0.5437 | 较强 |

## 四、算法详解

### 1. 属性向量归一化

**公式**：
```
归一化值 = 原始值 / SQRT(SUM(所有乡镇该指标的原始值²))
```

**示例**（以乡镇灾害管理能力为例）：
```
所有乡镇值: [0.00, 0.32, 0.77, 0.05, 0.21, 0.05, 0.45]
平方和: 0.00² + 0.32² + 0.77² + ... = 0.963
分母: SQRT(0.963) = 0.981
青竹街道归一化: 0.00 / 0.981 = 0.0000
汉阳镇归一化: 0.32 / 0.981 = 0.3259
```

### 2. 定权计算

**公式**：
```
定权值 = 归一化值 × 一级权重 × 二级权重
```

**示例**（汉阳镇灾害管理能力）：
```
定权值 = 0.3259 × 0.53 × 0.33 = 0.0570
```

### 3. TOPSIS 优劣解算法

#### 正理想解距离（D+）

**公式**：
```
D+ = SQRT(SUM((max_value - current_value)²))
```

计算每个指标到最优解的距离。

#### 负理想解距离（D-）

**公式**：
```
D- = SQRT(SUM((min_value - current_value)²))
```

计算每个指标到最差解的距离。

#### 综合能力值

**公式**：
```
综合能力值 = D- / (D+ + D-)
```

值域：[0, 1]，越接近1表示综合能力越强。

### 4. 能力分级

根据均值（μ）和标准差（σ）进行分级：

#### 情况1：μ ≤ 0.5σ（三级分类）
- **强**: value ≥ μ + 1.5σ
- **较强**: value ≥ μ + 0.5σ
- **中等**: value < μ + 0.5σ

#### 情况2：0.5σ < μ ≤ 1.5σ（四级分类）
- **强**: value ≥ μ + 1.5σ
- **较强**: value ≥ μ + 0.5σ
- **中等**: value ≥ μ - 0.5σ
- **较弱**: value < μ - 0.5σ

#### 情况3：μ > 1.5σ（五级分类）
- **强**: value ≥ μ + 1.5σ
- **较强**: value ≥ μ + 0.5σ
- **中等**: value ≥ μ - 0.5σ
- **较弱**: value ≥ μ - 1.5σ
- **弱**: value < μ - 1.5σ

## 五、前置条件

### 1. 数据库配置

#### 步骤1.1：诊断当前配置

**首先运行诊断脚本**检查数据库中的配置状态：

```bash
# 使用MySQL命令行执行诊断
mysql -h192.168.15.203 -P30314 -uroot -p123456 evaluate_db < sql/diagnose_comprehensive_model.sql
```

诊断脚本会检查：
- 综合减灾模型是否存在
- 模型步骤配置是否正确
- 步骤算法配置是否完整
- evaluation_result表中是否有模型3和模型8的数据
- 是否存在重复的区域代码

#### 步骤1.2：修复配置

**如果诊断发现问题**（例如第一步没有数据，或者算法配置不正确），执行修复脚本：

```bash
# 执行修复脚本
mysql -h192.168.15.203 -P30314 -uroot -p123456 evaluate_db < sql/fix_comprehensive_model.sql
```

修复脚本会：
- 更新第一步的名称和代码为"数据融合"（DATA_FUSION）
- 删除旧的算法配置
- 添加6个@LOAD_EVAL_RESULT算法，从evaluation_result表加载数据
- 验证配置是否正确

#### 步骤1.3：全新安装（可选）

**如果是首次配置**，可以直接执行完整配置脚本：

```bash
# 方式1：使用MySQL命令行
mysql -h192.168.15.203 -P30314 -uroot -p123456 evaluate_db < sql/comprehensive_model_setup.sql

# 方式2：使用DBeaver或其他数据库工具
# 直接打开 sql/comprehensive_model_setup.sql 文件并执行
```

⚠️ **注意**：如果数据库中已存在综合减灾模型，执行此脚本会创建重复的模型。建议先运行诊断和修复脚本。

### 2. 评估结果数据

**必须先运行**以下两个评估模型，确保 `evaluation_result` 表中有数据：

1. **乡镇减灾能力评估模型**（模型ID=3）
   - 必须对所选区域的乡镇进行评估
   - 生成灾害管理能力、灾害备灾能力、自救转移能力三个分值

2. **社区-乡镇减灾能力评估模型**（模型ID=8）
   - 必须对所选区域的乡镇进行评估
   - 生成灾害管理能力、灾害备灾能力、自救转移能力三个分值

### 3. 验证数据完整性

执行以下SQL检查数据是否完整：

```sql
-- 检查乡镇评估结果
SELECT
    region_code,
    region_name,
    management_capability_score,
    support_capability_score,
    self_rescue_capability_score
FROM evaluation_result
WHERE evaluation_model_id = 3
AND region_code IN ('你的乡镇代码列表')
ORDER BY create_time DESC;

-- 检查社区-乡镇评估结果
SELECT
    region_code,
    region_name,
    management_capability_score,
    support_capability_score,
    self_rescue_capability_score
FROM evaluation_result
WHERE evaluation_model_id = 8
AND region_code IN ('你的乡镇代码列表')
ORDER BY create_time DESC;
```

## 六、使用步骤

### 步骤1：配置模型（仅需一次）

```sql
-- 执行综合减灾能力评估模型配置脚本
source /path/to/sql/comprehensive_model_setup.sql;

-- 验证模型配置
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

预期输出：
```
model_id | model_name           | step_order | step_name         | algorithm_count
---------|---------------------|------------|------------------|----------------
9        | 综合减灾能力评估模型  | 1          | 数据融合          | 6
9        | 综合减灾能力评估模型  | 2          | 属性向量归一化     | 6
9        | 综合减灾能力评估模型  | 3          | 二级定权          | 6
9        | 综合减灾能力评估模型  | 4          | TOPSIS优劣解距离  | 12
9        | 综合减灾能力评估模型  | 5          | 能力评估          | 4
```

### 步骤2：运行乡镇评估模型

1. 登录系统前端
2. 选择"评估模型" -> "乡镇减灾能力评估模型"
3. 选择要评估的区域（必须包含乡镇级数据）
4. 点击"执行评估"
5. 等待评估完成，查看结果

### 步骤3：运行社区-乡镇评估模型

1. 选择"评估模型" -> "社区-乡镇减灾能力评估模型"
2. 选择相同的区域
3. 点击"执行评估"
4. 等待评估完成，查看结果

### 步骤4：运行综合减灾能力评估模型

1. 选择"评估模型" -> "综合减灾能力评估模型"
2. 选择相同的区域
3. 点击"执行评估"
4. 系统会自动：
   - 从 `evaluation_result` 表加载乡镇和社区评估结果
   - 进行归一化、定权、TOPSIS计算
   - 生成综合减灾能力分值和分级
5. 查看评估结果

## 七、结果查询

### 查询最新评估结果

```sql
SELECT
    region_code,
    region_name,
    management_capability_score AS 灾害管理能力,
    support_capability_score AS 灾害备灾能力,
    self_rescue_capability_score AS 自救转移能力,
    comprehensive_capability_score AS 综合减灾能力,
    management_capability_level AS 管理分级,
    support_capability_level AS 备灾分级,
    self_rescue_capability_level AS 自救分级,
    comprehensive_capability_level AS 综合分级,
    create_time AS 评估时间
FROM evaluation_result
WHERE evaluation_model_id = 9  -- 综合减灾能力评估模型ID
ORDER BY create_time DESC, comprehensive_capability_score DESC;
```

### 对比不同评估结果

```sql
SELECT
    r.region_name AS 乡镇名称,
    MAX(CASE WHEN er.evaluation_model_id = 3 THEN er.comprehensive_capability_score END) AS 乡镇评估,
    MAX(CASE WHEN er.evaluation_model_id = 8 THEN er.comprehensive_capability_score END) AS 社区评估,
    MAX(CASE WHEN er.evaluation_model_id = 9 THEN er.comprehensive_capability_score END) AS 综合评估
FROM evaluation_result er
JOIN region r ON er.region_code = r.region_code
WHERE er.evaluation_model_id IN (3, 8, 9)
GROUP BY r.region_name
ORDER BY 综合评估 DESC;
```

## 八、常见问题

### Q1: 第一步"数据获取"或"数据融合"没有数据，columns和tableData都是空的？

**A**: 这是最常见的问题，可能原因和解决方案：

#### 原因1：数据库配置不正确

**解决方案：**
```bash
# 步骤1：运行诊断脚本
mysql -h192.168.15.203 -P30314 -uroot -p123456 evaluate_db < sql/diagnose_comprehensive_model.sql

# 步骤2：查看诊断结果，特别关注：
# - 第一步的算法数量（应该是6个）
# - 算法的ql_expression（应该包含@LOAD_EVAL_RESULT）

# 步骤3：如果配置不正确，运行修复脚本
mysql -h192.168.15.203 -P30314 -uroot -p123456 evaluate_db < sql/fix_comprehensive_model.sql
```

#### 原因2：evaluation_result表中没有数据

**解决方案：**
```sql
-- 检查模型3和模型8的评估结果
SELECT
    evaluation_model_id,
    COUNT(*) AS count,
    COUNT(DISTINCT region_code) AS region_count
FROM evaluation_result
WHERE evaluation_model_id IN (3, 8)
GROUP BY evaluation_model_id;
```

如果结果为空或数量很少，说明需要先运行：
1. 乡镇减灾能力评估模型（模型ID=3）
2. 社区-乡镇减灾能力评估模型（模型ID=8）

#### 原因3：区域代码不匹配

**解决方案：**
```sql
-- 检查evaluation_result表中的区域代码
SELECT DISTINCT region_code, region_name
FROM evaluation_result
WHERE evaluation_model_id IN (3, 8)
ORDER BY region_code;

-- 确保综合减灾模型评估时选择的区域在上述结果中
```

#### 原因4：后端日志显示错误

**解决方案：**
查看后端日志（通常在 logs/ 目录），搜索关键字：
- `[LOAD_EVAL_RESULT]`
- `未找到评估结果`
- `参数不完整`

根据日志信息进行相应的修复。

### Q2: 为什么综合减灾能力评估模型执行失败？

**A**: 检查以下几点：
1. 是否已执行诊断和修复脚本（见Q1）
2. 是否已先运行乡镇评估模型（模型ID=3）
3. 是否已先运行社区-乡镇评估模型（模型ID=8）
4. 选择的区域是否与前两个模型的评估区域一致

### Q3: 如何确认数据是否加载成功？

**A**: 查看后端日志，搜索 `[LOAD_EVAL_RESULT]` 关键字：
```
[LOAD_EVAL_RESULT] 加载评估结果: params=modelId=3,field=management_capability_score, region=511425108
[LOAD_EVAL_RESULT] 加载成功: modelId=3, region=511425108, field=management_capability_score, value=0.7657
```

### Q4: 综合能力值为什么是0？

**A**: 可能原因：
1. 乡镇或社区评估结果数据为空或为0
2. 数据库中找不到对应区域的评估结果
3. 模型ID或字段名配置错误

解决方法：
```sql
-- 检查evaluation_result表中的数据
SELECT * FROM evaluation_result
WHERE evaluation_model_id IN (3, 8)
AND region_code = '你的区域代码'
ORDER BY create_time DESC;
```

### Q5: 能否修改权重配置？

**A**: 可以，修改 `step_algorithm` 表中的 `ql_expression` 字段：

```sql
-- 示例：修改乡镇灾害管理能力的权重
UPDATE step_algorithm
SET ql_expression = 'TOWNSHIP_MGMT_NORM * 0.6 * 0.4'  -- 新权重：一级0.6，二级0.4
WHERE algorithm_code = 'WEIGHT_TOWNSHIP_MGMT'
AND step_id IN (SELECT id FROM model_step WHERE model_id = 9);
```

### Q6: 分级结果是否符合预期？

**A**: 分级算法完全按照Excel公式实现，基于均值和标准差进行统计分级。如果样本量较小（少于3个乡镇），分级可能不够准确。

## 九、技术架构

### 核心服务类

1. **SpecialAlgorithmServiceImpl**: 特殊算法处理服务
   - `loadEvaluationResult()`: 从数据库加载评估结果
   - `normalize()`: 属性向量归一化
   - `calculateTopsisPositive()`: 计算正理想解距离
   - `calculateTopsisNegative()`: 计算负理想解距离
   - `calculateGrade()`: 能力分级

2. **ModelExecutionServiceImpl**: 模型执行服务
   - 协调各个步骤的执行
   - 管理上下文数据
   - 处理特殊算法标记（@LOAD_EVAL_RESULT, @NORMALIZE, @TOPSIS_*, @GRADE）

3. **EvaluationResultServiceImpl**: 评估结果服务
   - 保存评估结果到数据库
   - 查询历史评估记录

### 数据流

```
1. 用户选择区域并执行综合减灾能力评估模型
   ↓
2. ModelExecutionServiceImpl 开始执行步骤1
   ↓
3. 遇到 @LOAD_EVAL_RESULT 标记
   ↓
4. SpecialAlgorithmServiceImpl.loadEvaluationResult()
   ├─ 从 evaluation_result 表查询乡镇评估结果（modelId=3）
   └─ 从 evaluation_result 表查询社区评估结果（modelId=8）
   ↓
5. 执行步骤2：归一化（@NORMALIZE）
   ↓
6. 执行步骤3：定权（QLExpress表达式）
   ↓
7. 执行步骤4：TOPSIS距离计算（@TOPSIS_POSITIVE, @TOPSIS_NEGATIVE）
   ↓
8. 执行步骤5：分级（@GRADE）
   ↓
9. EvaluationResultServiceImpl.saveEvaluationResults()
   ↓
10. 返回评估结果给前端
```

## 十、参考资料

- [TOPSIS算法详解](https://en.wikipedia.org/wiki/TOPSIS)
- [属性向量归一化方法](https://www.sciencedirect.com/topics/computer-science/vector-normalization)
- [项目完整文档](./PROJECT_GUIDE.md)
- [模型配置指南](./archive/TOPSIS-model-configuration-guide.md)

---

**最后更新**: 2025-11-02
**版本**: 1.0
**维护者**: System
