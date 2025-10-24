# 社区评估TOPSIS配置问题诊断报告

## 问题描述

社区评估模型的TOPSIS计算结果全部为0：
- 预案编制能力TOPSIS = 0
- 隐患排查能力TOPSIS = 0
- 风险评估能力TOPSIS = 0
- 财政投入能力TOPSIS = 0
- 物资储备能力TOPSIS = 0
- 医疗保障能力TOPSIS = 0
- 自救互救能力TOPSIS = 0
- 公众疏散能力TOPSIS = 0

但期望的结果应该是：
```
社区名称                    灾害管理能力    灾害备灾能力    自救转移能力
凤阳社区4492941            0.00826962     0.04492583     0.04492941
文林社区居民委员会          0.03028730     0.13381592     0.352
青衣社区居民委员会          0.09261798     0.12816002     0.
花园社区居民委员会          0.15890620     0.03660647     0.455
建华社区居民委员会          0.15876504     0.12942121     0.24187392
XX社区居民委员会            0.02052674     0.21268        0.3986
```

## 问题根源

### 1. 算法名称配置错误

**当前错误配置**（使用二级指标名称）：
- 算法1：预案编制能力TOPSIS
- 算法2：隐患排查能力TOPSIS
- 算法3：风险评估能力TOPSIS
- 算法4：财政投入能力TOPSIS
- 算法5：物资储备能力TOPSIS
- 算法6：医疗保障能力TOPSIS
- 算法7：自救互救能力TOPSIS
- 算法8：公众疏散能力TOPSIS

**正确配置**（应该使用一级指标名称）：
- 算法1：灾害管理能力TOPSIS正理想解
- 算法2：灾害管理能力TOPSIS负理想解
- 算法3：灾害备灾能力TOPSIS正理想解
- 算法4：灾害备灾能力TOPSIS负理想解
- 算法5：自救转移能力TOPSIS正理想解
- 算法6：自救转移能力TOPSIS负理想解

### 2. 输入参数类型不一致

**TOPSIS距离计算步骤**：
- 算法1（正理想解）：使用 `_WEIGHT` 参数（加权后的值）✓
- 算法2（负理想解）：使用 `_UNIFIED` 参数（归一化值）✗

**问题**：正理想解和负理想解使用了不同类型的输入参数，导致计算错误。

**正确做法**：正理想解和负理想解都应该使用 `_WEIGHT` 参数（加权后的值）。

### 3. 算法分组错误

社区评估模型有3个一级指标，每个一级指标包含多个二级指标：

**一级指标1：灾害管理能力**（包含4个二级指标）
- 预案编制能力（PLAN_CONSTRUCTION_WEIGHT）
- 隐患排查能力（HAZARD_INSPECTION_WEIGHT）
- 风险评估能力（RISK_ASSESSMENT_WEIGHT）
- 财政投入能力（FINANCIAL_INPUT_WEIGHT）

**一级指标2：灾害备灾能力**（包含2个二级指标）
- 物资储备能力（MATERIAL_RESERVE_WEIGHT）
- 医疗保障能力（MEDICAL_SUPPORT_WEIGHT）

**一级指标3：自救转移能力**（包含3个二级指标）
- 自救互救能力（SELF_MUTUAL_AID_WEIGHT）
- 公众疏散能力（PUBLIC_EVACUATION_WEIGHT）
- 转移安置能力（RELOCATION_SHELTER_WEIGHT）

**当前错误配置**：
- 为每个二级指标都配置了TOPSIS算法（共8个）
- 这是错误的，因为TOPSIS应该在一级指标层面计算

**正确配置**：
- 为每个一级指标配置正理想解和负理想解（共6个算法）
- 算法1-2：灾害管理能力的正负理想解
- 算法3-4：灾害备灾能力的正负理想解
- 算法5-6：自救转移能力的正负理想解

### 4. TOPSIS得分计算步骤配置错误

**当前错误配置**：
- 算法1-9：为每个二级指标都配置了TOPSIS得分计算

**正确配置**：
- 算法1：灾害管理能力TOPSIS得分（输入：MANAGEMENT_POSITIVE_IDEAL, MANAGEMENT_NEGATIVE_IDEAL）
- 算法2：灾害备灾能力TOPSIS得分（输入：SUPPORT_POSITIVE_IDEAL, SUPPORT_NEGATIVE_IDEAL）
- 算法3：自救转移能力TOPSIS得分（输入：CAPABILITY_POSITIVE_IDEAL, CAPABILITY_NEGATIVE_IDEAL）

## 修复方案

### 修复TOPSIS距离计算步骤

```sql
-- 算法1：灾害管理能力TOPSIS正理想解
UPDATE step_algorithm sa
JOIN model_step ms ON sa.step_id = ms.id
SET sa.algorithm_name = '灾害管理能力TOPSIS正理想解',
    sa.algorithm_code = 'MANAGEMENT_POSITIVE_IDEAL',
    sa.ql_expression = '@POSITIVE_IDEAL:PLAN_CONSTRUCTION_WEIGHT,HAZARD_INSPECTION_WEIGHT,RISK_ASSESSMENT_WEIGHT,FINANCIAL_INPUT_WEIGHT',
    sa.output_param = 'MANAGEMENT_POSITIVE_IDEAL'
WHERE ms.model_id = 4 AND ms.step_code = 'TOPSIS_DISTANCE' AND sa.algorithm_order = 1;

-- 算法2：灾害管理能力TOPSIS负理想解（修正为使用_WEIGHT）
UPDATE step_algorithm sa
JOIN model_step ms ON sa.step_id = ms.id
SET sa.algorithm_name = '灾害管理能力TOPSIS负理想解',
    sa.algorithm_code = 'MANAGEMENT_NEGATIVE_IDEAL',
    sa.ql_expression = '@NEGATIVE_IDEAL:PLAN_CONSTRUCTION_WEIGHT,HAZARD_INSPECTION_WEIGHT,RISK_ASSESSMENT_WEIGHT,FINANCIAL_INPUT_WEIGHT',
    sa.output_param = 'MANAGEMENT_NEGATIVE_IDEAL'
WHERE ms.model_id = 4 AND ms.step_code = 'TOPSIS_DISTANCE' AND sa.algorithm_order = 2;

-- 算法3：灾害备灾能力TOPSIS正理想解
UPDATE step_algorithm sa
JOIN model_step ms ON sa.step_id = ms.id
SET sa.algorithm_name = '灾害备灾能力TOPSIS正理想解',
    sa.algorithm_code = 'SUPPORT_POSITIVE_IDEAL',
    sa.ql_expression = '@POSITIVE_IDEAL:MATERIAL_RESERVE_WEIGHT,MEDICAL_SUPPORT_WEIGHT',
    sa.output_param = 'SUPPORT_POSITIVE_IDEAL'
WHERE ms.model_id = 4 AND ms.step_code = 'TOPSIS_DISTANCE' AND sa.algorithm_order = 3;

-- 算法4：灾害备灾能力TOPSIS负理想解
UPDATE step_algorithm sa
JOIN model_step ms ON sa.step_id = ms.id
SET sa.algorithm_name = '灾害备灾能力TOPSIS负理想解',
    sa.algorithm_code = 'SUPPORT_NEGATIVE_IDEAL',
    sa.ql_expression = '@NEGATIVE_IDEAL:MATERIAL_RESERVE_WEIGHT,MEDICAL_SUPPORT_WEIGHT',
    sa.output_param = 'SUPPORT_NEGATIVE_IDEAL'
WHERE ms.model_id = 4 AND ms.step_code = 'TOPSIS_DISTANCE' AND sa.algorithm_order = 4;

-- 算法5：自救转移能力TOPSIS正理想解
UPDATE step_algorithm sa
JOIN model_step ms ON sa.step_id = ms.id
SET sa.algorithm_name = '自救转移能力TOPSIS正理想解',
    sa.algorithm_code = 'CAPABILITY_POSITIVE_IDEAL',
    sa.ql_expression = '@POSITIVE_IDEAL:SELF_MUTUAL_AID_WEIGHT,PUBLIC_EVACUATION_WEIGHT,RELOCATION_SHELTER_WEIGHT',
    sa.output_param = 'CAPABILITY_POSITIVE_IDEAL'
WHERE ms.model_id = 4 AND ms.step_code = 'TOPSIS_DISTANCE' AND sa.algorithm_order = 5;

-- 算法6：自救转移能力TOPSIS负理想解
UPDATE step_algorithm sa
JOIN model_step ms ON sa.step_id = ms.id
SET sa.algorithm_name = '自救转移能力TOPSIS负理想解',
    sa.algorithm_code = 'CAPABILITY_NEGATIVE_IDEAL',
    sa.ql_expression = '@NEGATIVE_IDEAL:SELF_MUTUAL_AID_WEIGHT,PUBLIC_EVACUATION_WEIGHT,RELOCATION_SHELTER_WEIGHT',
    sa.output_param = 'CAPABILITY_NEGATIVE_IDEAL'
WHERE ms.model_id = 4 AND ms.step_code = 'TOPSIS_DISTANCE' AND sa.algorithm_order = 6;

-- 删除多余的算法7和8
DELETE sa FROM step_algorithm sa
JOIN model_step ms ON sa.step_id = ms.id
WHERE ms.model_id = 4 AND ms.step_code = 'TOPSIS_DISTANCE' AND sa.algorithm_order IN (7, 8);
```

### 修复TOPSIS得分计算步骤

```sql
-- 算法1：灾害管理能力TOPSIS得分
UPDATE step_algorithm sa
JOIN model_step ms ON sa.step_id = ms.id
SET sa.algorithm_name = '灾害管理能力TOPSIS得分',
    sa.algorithm_code = 'MANAGEMENT_SCORE',
    sa.ql_expression = '@TOPSIS_SCORE:MANAGEMENT_POSITIVE_IDEAL,MANAGEMENT_NEGATIVE_IDEAL',
    sa.output_param = 'MANAGEMENT_SCORE'
WHERE ms.model_id = 4 AND ms.step_code = 'TOPSIS_SCORE' AND sa.algorithm_order = 1;

-- 算法2：灾害备灾能力TOPSIS得分
UPDATE step_algorithm sa
JOIN model_step ms ON sa.step_id = ms.id
SET sa.algorithm_name = '灾害备灾能力TOPSIS得分',
    sa.algorithm_code = 'SUPPORT_SCORE',
    sa.ql_expression = '@TOPSIS_SCORE:SUPPORT_POSITIVE_IDEAL,SUPPORT_NEGATIVE_IDEAL',
    sa.output_param = 'SUPPORT_SCORE'
WHERE ms.model_id = 4 AND ms.step_code = 'TOPSIS_SCORE' AND sa.algorithm_order = 2;

-- 算法3：自救转移能力TOPSIS得分
UPDATE step_algorithm sa
JOIN model_step ms ON sa.step_id = ms.id
SET sa.algorithm_name = '自救转移能力TOPSIS得分',
    sa.algorithm_code = 'CAPABILITY_SCORE',
    sa.ql_expression = '@TOPSIS_SCORE:CAPABILITY_POSITIVE_IDEAL,CAPABILITY_NEGATIVE_IDEAL',
    sa.output_param = 'CAPABILITY_SCORE'
WHERE ms.model_id = 4 AND ms.step_code = 'TOPSIS_SCORE' AND sa.algorithm_order = 3;

-- 删除多余的算法4-9
DELETE sa FROM step_algorithm sa
JOIN model_step ms ON sa.step_id = ms.id
WHERE ms.model_id = 4 AND ms.step_code = 'TOPSIS_SCORE' AND sa.algorithm_order > 3;
```

## 执行修复

### 方法1：使用PowerShell脚本（推荐）

```powershell
.\fix_community_topsis.ps1
```

### 方法2：手动执行SQL

```powershell
Get-Content fix_community_topsis_config.sql | mysql -h192.168.15.203 -P30314 -uroot -p123456 evaluate_db
```

## 验证修复

修复后，重新执行社区评估，应该看到：

1. **TOPSIS距离计算结果**：
   - MANAGEMENT_POSITIVE_IDEAL > 0
   - MANAGEMENT_NEGATIVE_IDEAL > 0
   - SUPPORT_POSITIVE_IDEAL > 0
   - SUPPORT_NEGATIVE_IDEAL > 0
   - CAPABILITY_POSITIVE_IDEAL > 0
   - CAPABILITY_NEGATIVE_IDEAL > 0

2. **TOPSIS得分结果**：
   - MANAGEMENT_SCORE（灾害管理能力）：0 ~ 1 之间
   - SUPPORT_SCORE（灾害备灾能力）：0 ~ 1 之间
   - CAPABILITY_SCORE（自救转移能力）：0 ~ 1 之间

3. **最终评估结果**：
   - 每个社区的三个一级指标都应该有合理的数值
   - 数值范围应该在 0 到 1 之间
   - 不同社区的数值应该有差异

## 影响范围

- 影响模型：社区评估模型（model_id = 4）
- 影响步骤：
  - TOPSIS距离计算步骤（TOPSIS_DISTANCE）
  - TOPSIS得分计算步骤（TOPSIS_SCORE）
- 影响算法：共修改9个算法配置，删除5个多余算法

## 修复时间

2025-10-24

## 修复人员

Kiro AI Assistant
