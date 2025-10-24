-- 修复模型8归一化步骤的输入字段名

SELECT '==== 修复前的归一化步骤 ====' AS info;
SELECT 
    algorithm_order,
    algorithm_name,
    ql_expression,
    output_param
FROM step_algorithm sa
JOIN model_step ms ON sa.step_id = ms.id
WHERE ms.model_id = 8 AND ms.step_code = 'VECTOR_NORMALIZATION'
ORDER BY algorithm_order;

-- 算法3：Risk Assessment Norm
-- 输入应该是RISK_ASSESSMENT，而不是riskAssessmentCapability
UPDATE step_algorithm sa
JOIN model_step ms ON sa.step_id = ms.id
SET sa.ql_expression = '@NORMALIZE:RISK_ASSESSMENT'
WHERE ms.model_id = 8
  AND ms.step_code = 'VECTOR_NORMALIZATION'
  AND sa.algorithm_order = 3;

-- 算法5：Material Reserve Norm
-- 输入应该是MATERIAL_RESERVE，而不是materialReserveCapability
UPDATE step_algorithm sa
JOIN model_step ms ON sa.step_id = ms.id
SET sa.ql_expression = '@NORMALIZE:MATERIAL_RESERVE'
WHERE ms.model_id = 8
  AND ms.step_code = 'VECTOR_NORMALIZATION'
  AND sa.algorithm_order = 5;

-- 算法6：Medical Support Norm
-- 输入应该是MEDICAL_SUPPORT，而不是medicalSupportCapability
UPDATE step_algorithm sa
JOIN model_step ms ON sa.step_id = ms.id
SET sa.ql_expression = '@NORMALIZE:MEDICAL_SUPPORT'
WHERE ms.model_id = 8
  AND ms.step_code = 'VECTOR_NORMALIZATION'
  AND sa.algorithm_order = 6;

SELECT '==== 修复后的归一化步骤 ====' AS info;
SELECT 
    algorithm_order,
    algorithm_name,
    ql_expression,
    output_param
FROM step_algorithm sa
JOIN model_step ms ON sa.step_id = ms.id
WHERE ms.model_id = 8 AND ms.step_code = 'VECTOR_NORMALIZATION'
ORDER BY algorithm_order;
