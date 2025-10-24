-- 修复模型8的输出参数名称不匹配问题

SELECT '==== 修复前的归一化步骤输出参数 ====' AS info;
SELECT 
    algorithm_order,
    algorithm_name,
    output_param
FROM step_algorithm sa
JOIN model_step ms ON sa.step_id = ms.id
WHERE ms.model_id = 8 AND ms.step_code = 'VECTOR_NORMALIZATION'
ORDER BY algorithm_order;

-- 修复算法3：Risk Assessment Norm
UPDATE step_algorithm sa
JOIN model_step ms ON sa.step_id = ms.id
SET sa.output_param = 'RISK_ASSESSMENT_NORM'
WHERE ms.model_id = 8
  AND ms.step_code = 'VECTOR_NORMALIZATION'
  AND sa.algorithm_order = 3;

-- 修复算法5：Material Reserve Norm
UPDATE step_algorithm sa
JOIN model_step ms ON sa.step_id = ms.id
SET sa.output_param = 'MATERIAL_RESERVE_NORM'
WHERE ms.model_id = 8
  AND ms.step_code = 'VECTOR_NORMALIZATION'
  AND sa.algorithm_order = 5;

-- 修复算法6：Medical Support Norm
UPDATE step_algorithm sa
JOIN model_step ms ON sa.step_id = ms.id
SET sa.output_param = 'MEDICAL_SUPPORT_NORM'
WHERE ms.model_id = 8
  AND ms.step_code = 'VECTOR_NORMALIZATION'
  AND sa.algorithm_order = 6;

-- 修复乡镇数据聚合步骤的算法7：Township Aid Aggregation
UPDATE step_algorithm sa
JOIN model_step ms ON sa.step_id = ms.id
SET sa.output_param = 'TOWNSHIP_AID_AGG'
WHERE ms.model_id = 8
  AND ms.step_code = 'TOWNSHIP_AGGREGATION'
  AND sa.algorithm_order = 7;

SELECT '==== 修复后的归一化步骤输出参数 ====' AS info;
SELECT 
    algorithm_order,
    algorithm_name,
    output_param
FROM step_algorithm sa
JOIN model_step ms ON sa.step_id = ms.id
WHERE ms.model_id = 8 AND ms.step_code = 'VECTOR_NORMALIZATION'
ORDER BY algorithm_order;

SELECT '==== 验证：检查乡镇聚合步骤 ====' AS info;
SELECT 
    algorithm_order,
    algorithm_name,
    output_param
FROM step_algorithm sa
JOIN model_step ms ON sa.step_id = ms.id
WHERE ms.model_id = 8 AND ms.step_code = 'TOWNSHIP_AGGREGATION'
ORDER BY algorithm_order;
