# 社区评估TOPSIS问题最终修复报告

## 修复时间
2025-10-24

## 问题描述

社区评估模型执行后，所有TOPSIS相关的计算结果都是0：
- 灾害管理能力TOPSIS得分 = 0
- 灾害备灾能力TOPSIS得分 = 0
- 自救转移能力TOPSIS得分 = 0

期望结果应该是：
```
社区名称                    灾害管理能力    灾害备灾能力    自救转移能力
凤阳社区                   0.00826962     0.04492583     0.04492941
文林社区居民委员会          0.03028730     0.13381592     0.352
青衣社区居民委员会          0.09261798     0.12816002     0.15
```

## 根本原因分析

经过深入调查，发现了三个关键问题：

### 问题1：算法配置概念错误

**错误配置**：为每个二级指标配置TOPSIS算法（8个算法）
- 预案编制能力TOPSIS
- 隐患排查能力TOPSIS
- 风险评估能力TOPSIS
- 财政投入能力TOPSIS
- 物资储备能力TOPSIS
- 医疗保障能力TOPSIS
- 自救互救能力TOPSIS
- 公众疏散能力TOPSIS

**正确理解**：TOPSIS应该在一级指标层面计算，将多个二级指标聚合成一个一级指标得分

社区评估模型的指标体系：
```
综合减灾能力
├── 一级指标1：灾害管理能力（包含4个二级指标）
│   ├── 预案编制能力
│   ├── 隐患排查能力
│   ├── 风险评估能力
│   └── 财政投入能力
├── 一级指标2：灾害备灾能力（包含2个二级指标）
│   ├── 物资储备能力
│   └── 医疗保障能力
└── 一级指标3：自救转移能力（包含3个二级指标）
    ├── 自救互救能力
    ├── 公众疏散能力
    └── 转移安置能力
```

### 问题2：特殊标记名称不匹配

**数据库配置使用的标记**：
- `@POSITIVE_IDEAL:...`
- `@NEGATIVE_IDEAL:...`
- `@TOPSIS_SCORE:...`

**代码中支持的标记**：
- `@TOPSIS_POSITIVE:...`
- `@TOPSIS_NEGATIVE:...`
- `@GRADE:...`

**问题**：`@POSITIVE_IDEAL`和`@NEGATIVE_IDEAL`标记不被识别，导致算法执行失败返回0。

### 问题3：缺少TOPSIS得分计算实现

代码中已经实现了：
- `TOPSIS_POSITIVE` - 计算到正理想解的距离
- `TOPSIS_NEGATIVE` - 计算到负理想解的距离

但缺少：
- `TOPSIS_SCORE` - 计算最终得分：D- / (D+ + D-)

## 修复内容

### 修复1：数据库配置 - TOPSIS距离计算步骤

**修复前**（8个算法，错误）：
```sql
算法1: 预案编制能力TOPSIS → @POSITIVE_IDEAL:PLAN_CONSTRUCTION_WEIGHT,...
算法2: 隐患排查能力TOPSIS → @NEGATIVE_IDEAL:PLAN_CONSTRUCTION_UNIFIED,...
...
```

**修复后**（6个算法，正确）：
```sql
算法1: 灾害管理能力TOPSIS正理想解 → @TOPSIS_POSITIVE:PLAN_CONSTRUCTION_WEIGHT,HAZARD_INSPECTION_WEIGHT,RISK_ASSESSMENT_WEIGHT,FINANCIAL_INPUT_WEIGHT
算法2: 灾害管理能力TOPSIS负理想解 → @TOPSIS_NEGATIVE:PLAN_CONSTRUCTION_WEIGHT,HAZARD_INSPECTION_WEIGHT,RISK_ASSESSMENT_WEIGHT,FINANCIAL_INPUT_WEIGHT
算法3: 灾害备灾能力TOPSIS正理想解 → @TOPSIS_POSITIVE:MATERIAL_RESERVE_WEIGHT,MEDICAL_SUPPORT_WEIGHT
算法4: 灾害备灾能力TOPSIS负理想解 → @TOPSIS_NEGATIVE:MATERIAL_RESERVE_WEIGHT,MEDICAL_SUPPORT_WEIGHT
算法5: 自救转移能力TOPSIS正理想解 → @TOPSIS_POSITIVE:SELF_MUTUAL_AID_WEIGHT,PUBLIC_EVACUATION_WEIGHT,RELOCATION_SHELTER_WEIGHT
算法6: 自救转移能力TOPSIS负理想解 → @TOPSIS_NEGATIVE:SELF_MUTUAL_AID_WEIGHT,PUBLIC_EVACUATION_WEIGHT,RELOCATION_SHELTER_WEIGHT
```

**关键修改**：
1. 算法数量从8个减少到6个（3个一级指标 × 2个理想解）
2. 算法名称改为一级指标名称
3. 标记从`@POSITIVE_IDEAL`/`@NEGATIVE_IDEAL`改为`@TOPSIS_POSITIVE`/`@TOPSIS_NEGATIVE`
4. 输入参数统一使用`_WEIGHT`（加权后的值）

### 修复2：数据库配置 - 能力值计算与分级步骤

**修复前**（8个算法，名称错误）：
```sql
算法1: 预案编制能力评估 → @TOPSIS_SCORE:MANAGEMENT_POSITIVE_IDEAL,MANAGEMENT_NEGATIVE_IDEAL
算法2: 隐患排查能力评估 → @TOPSIS_SCORE:SUPPORT_POSITIVE_IDEAL,SUPPORT_NEGATIVE_IDEAL
...
```

**修复后**（6个算法，名称正确）：
```sql
算法1: 灾害管理能力TOPSIS得分 → @TOPSIS_SCORE:MANAGEMENT_POSITIVE_IDEAL,MANAGEMENT_NEGATIVE_IDEAL
算法2: 灾害备灾能力TOPSIS得分 → @TOPSIS_SCORE:SUPPORT_POSITIVE_IDEAL,SUPPORT_NEGATIVE_IDEAL
算法3: 自救转移能力TOPSIS得分 → @TOPSIS_SCORE:CAPABILITY_POSITIVE_IDEAL,CAPABILITY_NEGATIVE_IDEAL
算法4: 灾害管理能力分级 → @GRADE:MANAGEMENT_SCORE
算法5: 灾害备灾能力分级 → @GRADE:SUPPORT_SCORE
算法6: 自救转移能力分级 → @GRADE:CAPABILITY_SCORE
```

**关键修改**：
1. 算法数量从8个减少到6个
2. 算法名称改为一级指标名称
3. 删除了多余的综合能力计算

### 修复3：代码实现 - 添加TOPSIS_SCORE支持

**文件**：`src/main/java/com/evaluate/service/impl/SpecialAlgorithmServiceImpl.java`

**添加的代码**：

1. 在switch语句中添加TOPSIS_SCORE分支：
```java
case "TOPSIS_SCORE":
    return calculateTopsisScore(params, currentRegionCode, allRegionData);
```

2. 实现calculateTopsisScore方法：
```java
/**
 * 计算TOPSIS得分
 * 公式：TOPSIS_SCORE = D- / (D+ + D-)
 * 
 * @param params 参数格式："POSITIVE_IDEAL_FIELD,NEGATIVE_IDEAL_FIELD"
 * @param currentRegionCode 当前区域代码
 * @param allRegionData 所有区域数据
 * @return TOPSIS得分（0-1之间）
 */
public Double calculateTopsisScore(
        String params,
        String currentRegionCode,
        Map<String, Map<String, Object>> allRegionData) {
    
    // 1. 解析参数
    String[] fields = params.split(",");
    String positiveField = fields[0].trim();
    String negativeField = fields[1].trim();
    
    // 2. 获取D+和D-
    Map<String, Object> currentData = allRegionData.get(currentRegionCode);
    double dPositive = toDouble(currentData.get(positiveField));
    double dNegative = toDouble(currentData.get(negativeField));
    
    // 3. 计算得分：D- / (D+ + D-)
    double denominator = dPositive + dNegative;
    if (denominator == 0) {
        return 0.0;
    }
    
    return dNegative / denominator;
}
```

3. 在接口中添加方法声明：
```java
/**
 * TOPSIS得分计算：D- / (D+ + D-)
 */
Double calculateTopsisScore(
        String params,
        String currentRegionCode,
        Map<String, Map<String, Object>> allRegionData
);
```

## 执行的修复脚本

1. **fix_community_topsis_config.sql** - 修复TOPSIS距离计算步骤
   - 更新算法名称
   - 修正输入参数
   - 删除多余算法

2. **fix_capability_grade_step.sql** - 修复能力值计算与分级步骤
   - 更新算法名称
   - 删除多余算法

3. **fix_topsis_markers.sql** - 修复特殊标记名称
   - 将`@POSITIVE_IDEAL`改为`@TOPSIS_POSITIVE`
   - 将`@NEGATIVE_IDEAL`改为`@TOPSIS_NEGATIVE`

## 验证步骤

### 1. 验证数据库配置

```sql
-- 检查TOPSIS距离计算配置
SELECT sa.algorithm_order, sa.algorithm_name, sa.ql_expression
FROM step_algorithm sa
JOIN model_step ms ON sa.step_id = ms.id
WHERE ms.model_id = 4 AND ms.step_code = 'TOPSIS_DISTANCE'
ORDER BY sa.algorithm_order;

-- 应该看到6个算法，使用@TOPSIS_POSITIVE和@TOPSIS_NEGATIVE标记

-- 检查能力值计算配置
SELECT sa.algorithm_order, sa.algorithm_name, sa.ql_expression
FROM step_algorithm sa
JOIN model_step ms ON sa.step_id = ms.id
WHERE ms.model_id = 4 AND ms.step_code = 'CAPABILITY_GRADE'
ORDER BY sa.algorithm_order;

-- 应该看到6个算法，前3个使用@TOPSIS_SCORE，后3个使用@GRADE
```

### 2. 重新执行评估

```bash
# 调用API执行评估
curl -X GET "http://localhost:8081/api/evaluation/execute-model?modelId=4&weightConfigId=2"
```

### 3. 检查结果

查看评估结果，应该看到：
- 灾害管理能力得分：0 ~ 1 之间的数值
- 灾害备灾能力得分：0 ~ 1 之间的数值
- 自救转移能力得分：0 ~ 1 之间的数值

不同社区的数值应该有差异，不再全部是0。

## 修复影响范围

### 数据库修改
- **表**：`step_algorithm`
- **影响模型**：社区评估模型（model_id = 4）
- **影响步骤**：
  - TOPSIS_DISTANCE（优劣解计算）
  - CAPABILITY_GRADE（能力值计算与分级）
- **修改算法数量**：
  - TOPSIS_DISTANCE：从8个改为6个
  - CAPABILITY_GRADE：从8个改为6个

### 代码修改
- **文件**：
  - `src/main/java/com/evaluate/service/SpecialAlgorithmService.java`
  - `src/main/java/com/evaluate/service/impl/SpecialAlgorithmServiceImpl.java`
- **新增方法**：`calculateTopsisScore`
- **修改方法**：`executeSpecialAlgorithm`（添加TOPSIS_SCORE分支）

## 技术要点总结

### TOPSIS算法流程

1. **数据准备**：获取各二级指标的加权值（_WEIGHT字段）

2. **计算正理想解距离（D+）**：
   ```
   D+ = SQRT(SUM((max_value - current_value)^2))
   ```
   - 对每个二级指标，找到所有社区中的最大值
   - 计算当前社区与最大值的差的平方和
   - 取平方根

3. **计算负理想解距离（D-）**：
   ```
   D- = SQRT(SUM((min_value - current_value)^2))
   ```
   - 对每个二级指标，找到所有社区中的最小值
   - 计算当前社区与最小值的差的平方和
   - 取平方根

4. **计算TOPSIS得分**：
   ```
   TOPSIS_SCORE = D- / (D+ + D-)
   ```
   - 得分范围：0 ~ 1
   - 得分越高，表示越接近理想解，能力越强

### 特殊标记系统

系统支持的特殊标记（在ql_expression字段中使用）：

1. **@NORMALIZE:indicator_name**
   - 归一化：value / SQRT(SUMSQ(all_values))

2. **@TOPSIS_POSITIVE:indicator1,indicator2,...**
   - 计算到正理想解的距离

3. **@TOPSIS_NEGATIVE:indicator1,indicator2,...**
   - 计算到负理想解的距离

4. **@TOPSIS_SCORE:positive_field,negative_field**
   - 计算TOPSIS得分

5. **@GRADE:score_field**
   - 基于均值和标准差进行能力分级

## 经验教训

1. **理解业务逻辑**：必须清楚区分一级指标和二级指标，TOPSIS在一级指标层面计算

2. **标记名称一致性**：数据库配置中的标记名称必须与代码中支持的标记名称完全一致

3. **完整性检查**：实现算法时要确保整个流程完整，不能只实现中间步骤

4. **参数类型一致性**：正负理想解必须使用相同类型的输入参数（都用_WEIGHT）

5. **配置验证**：修改配置后应该立即验证算法数量和配置正确性

## 后续建议

1. **添加单元测试**：为TOPSIS计算添加单元测试，验证计算逻辑正确性

2. **添加配置验证**：在模型执行前验证算法配置的完整性和正确性

3. **文档完善**：更新系统文档，说明TOPSIS算法的配置方法和计算流程

4. **监控日志**：添加详细的计算日志，便于排查问题

5. **前端展示**：优化前端展示，清晰区分一级指标和二级指标的结果

## 修复人员

Kiro AI Assistant

## 修复日期

2025-10-24
