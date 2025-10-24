-- 修复模型8的二级指标定权步骤
-- 将@WEIGHT标记替换为具体的权重值

SELECT '==== 修复前的配置 ====' AS info;
SELECT 
    sa.algorithm_order,
    sa.algorithm_name,
    sa.ql_expression
FROM step_algorithm sa
JOIN model_step ms ON sa.step_id = ms.id
WHERE ms.model_id = 8 AND ms.step_code = 'SECONDARY_WEIGHTING'
ORDER BY sa.algorithm_order;

-- 算法2: Support Weight - 物资储备和医疗保障的加权
-- 物资储备权重: 0.52, 医疗保障权重: 0.48
UPDATE step_algorithm sa
JOIN model_step ms ON sa.step_id = ms.id
SET sa.ql_expression = '(MATERIAL_RESERVE_NORM != null ? MATERIAL_RESERVE_NORM : 0.0) * 0.52 + (MEDICAL_SUPPORT_NORM != null ? MEDICAL_SUPPORT_NORM : 0.0) * 0.48'
WHERE ms.model_id = 8
  AND ms.step_code = 'SECONDARY_WEIGHTING'
  AND sa.algorithm_order = 2;

-- 算法3: Capability Weight - 自救互救和公众疏散的加权
-- 自救互救权重: 0.33, 公众疏散权重: 0.34
UPDATE step_algorithm sa
JOIN model_step ms ON sa.step_id = ms.id
SET sa.ql_expression = '(SELF_MUTUAL_AID_NORM != null ? SELF_MUTUAL_AID_NORM : 0.0) * 0.33 + (PUBLIC_EVACUATION_NORM != null ? PUBLIC_EVACUATION_NORM : 0.0) * 0.34'
WHERE ms.model_id = 8
  AND ms.step_code = 'SECONDARY_WEIGHTING'
  AND sa.algorithm_order = 3;

-- 算法5: Comprehensive Weight - 综合权重计算
-- 灾害管理能力权重: 0.32, 灾害备灾能力权重: 0.31, 自救转移能力权重: 0.37
UPDATE step_algorithm sa
JOIN model_step ms ON sa.step_id = ms.id
SET sa.ql_expression = '(MANAGEMENT_WEIGHT != null ? MANAGEMENT_WEIGHT : 0.0) * 0.32 + (SUPPORT_WEIGHT != null ? SUPPORT_WEIGHT : 0.0) * 0.31 + (CAPABILITY_WEIGHT != null ? CAPABILITY_WEIGHT : 0.0) * 0.37 + (FACILITY_WEIGHT != null ? FACILITY_WEIGHT : 0.0) * 0.37'
WHERE ms.model_id = 8
  AND ms.step_code = 'SECONDARY_WEIGHTING'
  AND sa.algorithm_order = 5;

SELECT '==== 修复后的配置 ====' AS info;
SELECT 
    sa.algorithm_order,
    sa.algorithm_name,
    sa.ql_expression
FROM step_algorithm sa
JOIN model_step ms ON sa.step_id = ms.id
WHERE ms.model_id = 8 AND ms.step_code = 'SECONDARY_WEIGHTING'
ORDER BY sa.algorithm_order;
