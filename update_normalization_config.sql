-- 更新归一化步骤配置，确保列名与INDICATOR_ASSIGNMENT输出一致
-- 正确的列名应该是：
-- managementCapability, riskAssessmentCapability, fundingCapability, materialReserveCapability,
-- medicalSupportCapability, selfRescueCapability, publicAvoidanceCapability, relocationCapability

-- 更新队伍管理能力归一化
UPDATE step_algorithm sa
JOIN model_step ms ON sa.step_id = ms.id
SET sa.ql_expression = '@NORMALIZE:managementCapability',
    sa.output_param = 'managementCapabilityNorm'
WHERE ms.step_code = 'VECTOR_NORMALIZATION'
  AND sa.algorithm_code = 'MANAGEMENT_NORM';

-- 更新风险评估能力归一化
UPDATE step_algorithm sa
JOIN model_step ms ON sa.step_id = ms.id
SET sa.ql_expression = '@NORMALIZE:riskAssessmentCapability',
    sa.output_param = 'riskAssessmentCapabilityNorm'
WHERE ms.step_code = 'VECTOR_NORMALIZATION'
  AND sa.algorithm_code = 'RISK_ASSESSMENT_NORM';

-- 更新财政投入能力归一化
UPDATE step_algorithm sa
JOIN model_step ms ON sa.step_id = ms.id
SET sa.ql_expression = '@NORMALIZE:fundingCapability',
    sa.output_param = 'fundingCapabilityNorm'
WHERE ms.step_code = 'VECTOR_NORMALIZATION'
  AND sa.algorithm_code = 'FUNDING_NORM';

-- 更新物资储备能力归一化
UPDATE step_algorithm sa
JOIN model_step ms ON sa.step_id = ms.id
SET sa.ql_expression = '@NORMALIZE:materialReserveCapability',
    sa.output_param = 'materialReserveCapabilityNorm'
WHERE ms.step_code = 'VECTOR_NORMALIZATION'
  AND sa.algorithm_code = 'MATERIAL_RESERVE_NORM';

-- 更新医疗保障能力归一化
UPDATE step_algorithm sa
JOIN model_step ms ON sa.step_id = ms.id
SET sa.ql_expression = '@NORMALIZE:medicalSupportCapability',
    sa.output_param = 'medicalSupportCapabilityNorm'
WHERE ms.step_code = 'VECTOR_NORMALIZATION'
  AND sa.algorithm_code = 'MEDICAL_SUPPORT_NORM';

-- 更新自救互救能力归一化
UPDATE step_algorithm sa
JOIN model_step ms ON sa.step_id = ms.id
SET sa.ql_expression = '@NORMALIZE:selfRescueCapability',
    sa.output_param = 'selfRescueCapabilityNorm'
WHERE ms.step_code = 'VECTOR_NORMALIZATION'
  AND sa.algorithm_code = 'SELF_RESCUE_NORM';

-- 更新公众避险能力归一化
UPDATE step_algorithm sa
JOIN model_step ms ON sa.step_id = ms.id
SET sa.ql_expression = '@NORMALIZE:publicAvoidanceCapability',
    sa.output_param = 'publicAvoidanceCapabilityNorm'
WHERE ms.step_code = 'VECTOR_NORMALIZATION'
  AND sa.algorithm_code = 'PUBLIC_AVOIDANCE_NORM';

-- 更新转移安置能力归一化
UPDATE step_algorithm sa
JOIN model_step ms ON sa.step_id = ms.id
SET sa.ql_expression = '@NORMALIZE:relocationCapability',
    sa.output_param = 'relocationCapabilityNorm'
WHERE ms.step_code = 'VECTOR_NORMALIZATION'
  AND sa.algorithm_code = 'RELOCATION_NORM';

-- 验证更新结果
SELECT
    sa.algorithm_name AS '算法名称',
    sa.algorithm_code AS '算法代码',
    sa.ql_expression AS 'QL表达式',
    sa.output_param AS '输出参数'
FROM step_algorithm sa
JOIN model_step ms ON sa.step_id = ms.id
WHERE ms.step_code = 'VECTOR_NORMALIZATION'
ORDER BY sa.algorithm_order;
