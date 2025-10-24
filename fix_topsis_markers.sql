-- 修复TOPSIS标记名称
-- 问题：数据库配置使用@POSITIVE_IDEAL和@NEGATIVE_IDEAL
-- 但代码中只支持@TOPSIS_POSITIVE和@TOPSIS_NEGATIVE

SELECT '==== 修复前的TOPSIS标记 ====' AS info;
SELECT 
    sa.algorithm_order,
    sa.algorithm_name,
    sa.ql_expression
FROM step_algorithm sa
JOIN model_step ms ON sa.step_id = ms.id
WHERE ms.model_id = 4 AND ms.step_code = 'TOPSIS_DISTANCE'
ORDER BY sa.algorithm_order;

-- 修复算法1：灾害管理能力TOPSIS正理想解
UPDATE step_algorithm sa
JOIN model_step ms ON sa.step_id = ms.id
SET sa.ql_expression = '@TOPSIS_POSITIVE:PLAN_CONSTRUCTION_WEIGHT,HAZARD_INSPECTION_WEIGHT,RISK_ASSESSMENT_WEIGHT,FINANCIAL_INPUT_WEIGHT'
WHERE ms.model_id = 4
  AND ms.step_code = 'TOPSIS_DISTANCE'
  AND sa.algorithm_order = 1;

-- 修复算法2：灾害管理能力TOPSIS负理想解
UPDATE step_algorithm sa
JOIN model_step ms ON sa.step_id = ms.id
SET sa.ql_expression = '@TOPSIS_NEGATIVE:PLAN_CONSTRUCTION_WEIGHT,HAZARD_INSPECTION_WEIGHT,RISK_ASSESSMENT_WEIGHT,FINANCIAL_INPUT_WEIGHT'
WHERE ms.model_id = 4
  AND ms.step_code = 'TOPSIS_DISTANCE'
  AND sa.algorithm_order = 2;

-- 修复算法3：灾害备灾能力TOPSIS正理想解
UPDATE step_algorithm sa
JOIN model_step ms ON sa.step_id = ms.id
SET sa.ql_expression = '@TOPSIS_POSITIVE:MATERIAL_RESERVE_WEIGHT,MEDICAL_SUPPORT_WEIGHT'
WHERE ms.model_id = 4
  AND ms.step_code = 'TOPSIS_DISTANCE'
  AND sa.algorithm_order = 3;

-- 修复算法4：灾害备灾能力TOPSIS负理想解
UPDATE step_algorithm sa
JOIN model_step ms ON sa.step_id = ms.id
SET sa.ql_expression = '@TOPSIS_NEGATIVE:MATERIAL_RESERVE_WEIGHT,MEDICAL_SUPPORT_WEIGHT'
WHERE ms.model_id = 4
  AND ms.step_code = 'TOPSIS_DISTANCE'
  AND sa.algorithm_order = 4;

-- 修复算法5：自救转移能力TOPSIS正理想解
UPDATE step_algorithm sa
JOIN model_step ms ON sa.step_id = ms.id
SET sa.ql_expression = '@TOPSIS_POSITIVE:SELF_MUTUAL_AID_WEIGHT,PUBLIC_EVACUATION_WEIGHT,RELOCATION_SHELTER_WEIGHT'
WHERE ms.model_id = 4
  AND ms.step_code = 'TOPSIS_DISTANCE'
  AND sa.algorithm_order = 5;

-- 修复算法6：自救转移能力TOPSIS负理想解
UPDATE step_algorithm sa
JOIN model_step ms ON sa.step_id = ms.id
SET sa.ql_expression = '@TOPSIS_NEGATIVE:SELF_MUTUAL_AID_WEIGHT,PUBLIC_EVACUATION_WEIGHT,RELOCATION_SHELTER_WEIGHT'
WHERE ms.model_id = 4
  AND ms.step_code = 'TOPSIS_DISTANCE'
  AND sa.algorithm_order = 6;

SELECT '==== 修复后的TOPSIS标记 ====' AS info;
SELECT 
    sa.algorithm_order,
    sa.algorithm_name,
    sa.ql_expression
FROM step_algorithm sa
JOIN model_step ms ON sa.step_id = ms.id
WHERE ms.model_id = 4 AND ms.step_code = 'TOPSIS_DISTANCE'
ORDER BY sa.algorithm_order;

-- 同时检查能力值计算步骤是否也需要修复
SELECT '==== 检查能力值计算步骤的TOPSIS_SCORE标记 ====' AS info;
SELECT 
    sa.algorithm_order,
    sa.algorithm_name,
    sa.ql_expression
FROM step_algorithm sa
JOIN model_step ms ON sa.step_id = ms.id
WHERE ms.model_id = 4 AND ms.step_code = 'CAPABILITY_GRADE'
  AND sa.ql_expression LIKE '%TOPSIS%'
ORDER BY sa.algorithm_order;
