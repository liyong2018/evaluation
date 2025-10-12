# TOPSIS评估模型完整配置指南

## 概述

本文档说明如何配置完整的TOPSIS（逼近理想解排序法）评估模型，该模型包含5个主要步骤，用于评估乡镇街道的减灾能力。

## 当前状态

✅ **步骤1已配置完成**：评估指标赋值（8个算法）

⏳ **待配置**：步骤2-5需要后端服务扩展支持

## 五步评估流程

### 步骤1：评估指标赋值 ✅ 已完成

根据原始数据计算8个二级指标的初始值。

| 算法名称 | 输出变量 | 表达式 |
|---------|---------|--------|
| 队伍管理能力 | teamManagement | (management_staff * 1.0 / population) * 10000 |
| 风险评估能力 | riskAssessment | risk_assessment != null && risk_assessment.contains("是") ? 1.0 : 0.0 |
| 财政投入能力 | financialInput | (funding_amount * 1.0 / population) * 10000 |
| 物资储备能力 | materialReserve | (material_value * 1.0 / population) * 10000 |
| 医疗保障能力 | medicalSupport | (hospital_beds * 1.0 / population) * 10000 |
| 自救互救能力 | selfRescue | ((firefighters + volunteers + militia_reserve) * 1.0 / population) * 10000 |
| 公众避险能力 | publicAvoidance | (training_participants * 1.0 / population) * 100 |
| 转移安置能力 | relocationCapacity | shelter_capacity * 1.0 / population |

### 步骤2：属性向量归一化 ⏳ 需要后端扩展

对每个指标进行向量归一化处理：

```
归一化值 = 原始值 / SQRT(SUM(所有地区原始值的平方))
```

**实现要点**：
- 需要聚合所有地区的数据
- 计算平方和的平方根
- 每个地区的值除以这个聚合值

**建议实现方式**：
1. 在`ModelExecutionServiceImpl.executeStep()`中识别特殊标记`@NORMALIZE:`
2. 提取所有地区该指标的值
3. 计算SQRT(SUMSQ())
4. 对每个地区执行归一化

### 步骤3：定权计算 ⏳ 需要后端扩展

使用权重对归一化后的数据进行加权，分为两个子部分：

#### 3.1 一级指标定权（8个算法）

使用二级权重：

```
定权值 = 归一化值 * 二级权重
```

#### 3.2 乡镇街道减灾能力定权（8个算法）

使用一级权重和二级权重的乘积：

```
综合定权值 = 归一化值 * 一级权重 * 二级权重
```

**权重配置要求**：

在`indicator_weight`表中需要配置以下权重：

**一级权重**：
- `DISASTER_MANAGEMENT` - 灾害管理能力
- `DISASTER_PREPAREDNESS` - 灾害备灾能力
- `SELF_RESCUE_TRANSFER` - 自救转移能力

**二级权重**：
- `TEAM_MANAGEMENT` - 队伍管理能力
- `RISK_ASSESSMENT` - 风险评估能力
- `FINANCIAL_INPUT` - 财政投入能力
- `MATERIAL_RESERVE` - 物资储备能力
- `MEDICAL_SUPPORT` - 医疗保障能力
- `SELF_RESCUE` - 自救互救能力
- `PUBLIC_AVOIDANCE` - 公众避险能力
- `RELOCATION_CAPACITY` - 转移安置能力

### 步骤4：优劣解计算（TOPSIS距离） ⏳ 需要后端扩展

计算每个地区到正理想解和负理想解的欧氏距离。

#### 4.1 一级指标优劣解（6个算法）

**灾害管理能力**：
```
正理想解距离 = SQRT((max(teamManagementWeighted) - 当前值)² + 
                    (max(riskAssessmentWeighted) - 当前值)² + 
                    (max(financialInputWeighted) - 当前值)²)

负理想解距离 = SQRT((min(teamManagementWeighted) - 当前值)² + 
                    (min(riskAssessmentWeighted) - 当前值)² + 
                    (min(financialInputWeighted) - 当前值)²)
```

**灾害备灾能力**、**自救转移能力**同理。

#### 4.2 综合能力优劣解（2个算法）

使用所有8个指标的综合定权值计算。

**实现要点**：
- 需要找出所有地区的最大值和最小值
- 计算欧氏距离
- 对每个地区返回两个值：到正理想解的距离、到负理想解的距离

### 步骤5：能力值计算与分级 ⏳ 需要后端扩展

#### 5.1 能力值计算（4个算法）

计算贴近度（Closeness Coefficient）：

```
能力值 = 负理想解距离 / (负理想解距离 + 正理想解距离)
```

能力值越接近1，表示越接近正理想解，能力越强。

#### 5.2 能力分级（4个算法）

使用统计方法进行五级分类：强、较强、中等、较弱、弱。

**分级算法**：

```
μ = AVERAGE(所有地区的能力值)
σ = STDEV(所有地区的能力值)

IF μ <= 0.5 * σ:
    IF 能力值 >= μ + 1.5*σ: "强"
    ELSE IF 能力值 >= μ + 0.5*σ: "较强"
    ELSE: "中等"
ELSE IF μ <= 1.5 * σ:
    IF 能力值 >= μ + 1.5*σ: "强"
    ELSE IF 能力值 >= μ + 0.5*σ: "较强"
    ELSE IF 能力值 >= μ - 0.5*σ: "中等"
    ELSE: "较弱"
ELSE:
    IF 能力值 >= μ + 1.5*σ: "强"
    ELSE IF 能力值 >= μ + 0.5*σ: "较强"
    ELSE IF 能力值 >= μ - 0.5*σ: "中等"
    ELSE IF 能力值 >= μ - 1.5*σ: "较弱"
    ELSE: "弱"
```

**实现要点**：
- 需要聚合计算均值和标准差
- 使用统计学的正态分布分类方法

## 后端扩展实现方案

### 方案1：扩展ModelExecutionServiceImpl（推荐）

在`ModelExecutionServiceImpl.executeStep()`中增加特殊表达式的处理：

```java
// 识别特殊标记
if (expression.startsWith("@NORMALIZE:")) {
    // 向量归一化
    String variableName = expression.substring("@NORMALIZE:".length());
    return normalizeVector(regionResults, variableName);
}
else if (expression.startsWith("@TOPSIS_POSITIVE:")) {
    // TOPSIS正理想解距离
    String variables = expression.substring("@TOPSIS_POSITIVE:".length());
    return calculateTopsisPositiveDistance(regionResults, variables.split(","));
}
else if (expression.startsWith("@TOPSIS_NEGATIVE:")) {
    // TOPSIS负理想解距离
    String variables = expression.substring("@TOPSIS_NEGATIVE:".length());
    return calculateTopsisNegativeDistance(regionResults, variables.split(","));
}
else if (expression.startsWith("@GRADE:")) {
    // 能力分级
    String variableName = expression.substring("@GRADE:".length());
    return gradeCapability(regionResults, variableName);
}
else {
    // 标准QLExpress执行
    return qlExpressService.execute(expression, context);
}
```

### 方案2：创建专门的聚合计算服务

创建`AggregateCalculationService`处理所有聚合计算：

```java
public interface AggregateCalculationService {
    // 向量归一化
    Map<String, Double> normalizeVector(Map<String, Map<String, Object>> regionData, String variableName);
    
    // TOPSIS距离计算
    Map<String, Double> calculateTopsisDistance(Map<String, Map<String, Object>> regionData, 
                                                List<String> variables, boolean isPositive);
    
    // 统计分级
    Map<String, String> gradeByStatistics(Map<String, Double> scores);
}
```

## 测试数据准备

### 权重配置

需要在数据库中配置权重：

```sql
-- 假设使用权重配置ID=1
-- 一级权重
INSERT INTO indicator_weight (config_id, indicator_code, indicator_name, indicator_level, weight, parent_id) VALUES
(1, 'DISASTER_MANAGEMENT', '灾害管理能力', 1, 0.4, NULL),
(1, 'DISASTER_PREPAREDNESS', '灾害备灾能力', 1, 0.3, NULL),
(1, 'SELF_RESCUE_TRANSFER', '自救转移能力', 1, 0.3, NULL);

-- 二级权重（灾害管理能力下）
INSERT INTO indicator_weight (config_id, indicator_code, indicator_name, indicator_level, weight, parent_id) VALUES
(1, 'TEAM_MANAGEMENT', '队伍管理能力', 2, 0.35, 1),
(1, 'RISK_ASSESSMENT', '风险评估能力', 2, 0.35, 1),
(1, 'FINANCIAL_INPUT', '财政投入能力', 2, 0.30, 1);

-- 二级权重（灾害备灾能力下）
INSERT INTO indicator_weight (config_id, indicator_code, indicator_name, indicator_level, weight, parent_id) VALUES
(1, 'MATERIAL_RESERVE', '物资储备能力', 2, 0.50, 2),
(1, 'MEDICAL_SUPPORT', '医疗保障能力', 2, 0.50, 2);

-- 二级权重（自救转移能力下）
INSERT INTO indicator_weight (config_id, indicator_code, indicator_name, indicator_level, weight, parent_id) VALUES
(1, 'SELF_RESCUE', '自救互救能力', 2, 0.40, 3),
(1, 'PUBLIC_AVOIDANCE', '公众避险能力', 2, 0.30, 3),
(1, 'RELOCATION_CAPACITY', '转移安置能力', 2, 0.30, 3);
```

## 执行SQL脚本

完整的SQL配置脚本已保存在：
- `docs/update-model-configuration.sql`

执行步骤：
```bash
docker cp docs/update-model-configuration.sql mysql-ccrc:/tmp/
docker exec -i mysql-ccrc mysql -uroot -pHtht1234 evaluate_db -e "source /tmp/update-model-configuration.sql"
```

## 预期输出示例

最终的评估结果表应包含以下列：

| 列名 | 说明 |
|-----|------|
| regionCode | 地区代码 |
| regionName | 地区名称 |
| INDICATOR_ASSIGNMENT_teamManagement | 队伍管理能力初始值 |
| INDICATOR_ASSIGNMENT_riskAssessment | 风险评估能力初始值 |
| ... | 其他指标初始值 |
| VECTOR_NORMALIZATION_teamManagementNorm | 队伍管理能力归一化值 |
| ... | 其他指标归一化值 |
| SECONDARY_WEIGHTING_teamManagementWeighted | 队伍管理能力定权值 |
| ... | 其他指标定权值 |
| TOPSIS_DISTANCE_disasterMgmtPositive | 灾害管理能力正理想解距离 |
| TOPSIS_DISTANCE_disasterMgmtNegative | 灾害管理能力负理想解距离 |
| TOPSIS_DISTANCE_totalPositive | 综合能力正理想解距离 |
| TOPSIS_DISTANCE_totalNegative | 综合能力负理想解距离 |
| CAPABILITY_GRADE_disasterMgmtScore | 灾害管理能力值 |
| CAPABILITY_GRADE_disasterMgmtGrade | 灾害管理能力分级 |
| CAPABILITY_GRADE_totalScore | 综合减灾能力值 |
| CAPABILITY_GRADE_totalGrade | 综合减灾能力分级 |

## 当前可测试的功能

由于步骤1已配置完成，您现在可以：

1. 刷新前端页面（Ctrl+F5）
2. 选择"标准减灾能力评估模型"
3. 选择地区和权重配置
4. 执行评估

预期结果：
- 可以看到8个二级指标的初始计算值
- 这些值应该不再是0，而是根据实际数据计算出的合理值

## 下一步工作

1. **短期**：验证步骤1的输出是否正确
2. **中期**：实现后端聚合计算扩展（步骤2-3）
3. **长期**：完成TOPSIS和分级算法（步骤4-5）

## 参考资料

- TOPSIS方法介绍：https://en.wikipedia.org/wiki/TOPSIS
- QLExpress官方文档
- 当前系统的模型执行框架文档：`docs/model-execution-integration.md`
