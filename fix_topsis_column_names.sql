-- 修复TOPSIS步骤的列名配置
-- 确保使用SECONDARY_WEIGHTING步骤输出的正确列名

-- 灾害管理能力包括：队伍管理、风险评估、财政投入
-- 正确的列名：managementCapabilityWeighted, riskAssessmentCapabilityWeighted, fundingCapabilityWeighted
UPDATE step_algorithm sa
JOIN model_step ms ON sa.step_id = ms.id
SET sa.ql_expression = '@TOPSIS_POSITIVE:managementCapabilityWeighted,riskAssessmentCapabilityWeighted,fundingCapabilityWeighted'
WHERE ms.step_code = 'TOPSIS_DISTANCE'
  AND sa.algorithm_code LIKE '%DISASTER_MGMT_POSITIVE%'
  OR (ms.step_code = 'TOPSIS_DISTANCE' AND sa.algorithm_name LIKE '%灾害管理能力优解%');

UPDATE step_algorithm sa
JOIN model_step ms ON sa.step_id = ms.id
SET sa.ql_expression = '@TOPSIS_NEGATIVE:managementCapabilityWeighted,riskAssessmentCapabilityWeighted,fundingCapabilityWeighted'
WHERE ms.step_code = 'TOPSIS_DISTANCE'
  AND sa.algorithm_code LIKE '%DISASTER_MGMT_NEGATIVE%'
  OR (ms.step_code = 'TOPSIS_DISTANCE' AND sa.algorithm_name LIKE '%灾害管理能力劣解%');

-- 灾害备灾能力包括：物资储备、医疗保障
-- 正确的列名：materialReserveCapabilityWeighted, medicalSupportCapabilityWeighted
UPDATE step_algorithm sa
JOIN model_step ms ON sa.step_id = ms.id
SET sa.ql_expression = '@TOPSIS_POSITIVE:materialReserveCapabilityWeighted,medicalSupportCapabilityWeighted'
WHERE ms.step_code = 'TOPSIS_DISTANCE'
  AND sa.algorithm_code LIKE '%DISASTER_PREP_POSITIVE%'
  OR (ms.step_code = 'TOPSIS_DISTANCE' AND sa.algorithm_name LIKE '%灾害备灾能力优解%');

UPDATE step_algorithm sa
JOIN model_step ms ON sa.step_id = ms.id
SET sa.ql_expression = '@TOPSIS_NEGATIVE:materialReserveCapabilityWeighted,medicalSupportCapabilityWeighted'
WHERE ms.step_code = 'TOPSIS_DISTANCE'
  AND sa.algorithm_code LIKE '%DISASTER_PREP_NEGATIVE%'
  OR (ms.step_code = 'TOPSIS_DISTANCE' AND sa.algorithm_name LIKE '%灾害备灾能力劣解%');

-- 自救转移能力包括：自救互救、公众避险、转移安置
-- 正确的列名：selfRescueCapabilityWeighted, publicAvoidanceCapabilityWeighted, relocationCapabilityWeighted
UPDATE step_algorithm sa
JOIN model_step ms ON sa.step_id = ms.id
SET sa.ql_expression = '@TOPSIS_POSITIVE:selfRescueCapabilityWeighted,publicAvoidanceCapabilityWeighted,relocationCapabilityWeighted'
WHERE ms.step_code = 'TOPSIS_DISTANCE'
  AND sa.algorithm_code LIKE '%SELF_RESCUE_POSITIVE%'
  OR (ms.step_code = 'TOPSIS_DISTANCE' AND sa.algorithm_name LIKE '%自救转移能力优解%');

UPDATE step_algorithm sa
JOIN model_step ms ON sa.step_id = ms.id
SET sa.ql_expression = '@TOPSIS_NEGATIVE:selfRescueCapabilityWeighted,publicAvoidanceCapabilityWeighted,relocationCapabilityWeighted'
WHERE ms.step_code = 'TOPSIS_DISTANCE'
  AND sa.algorithm_code LIKE '%SELF_RESCUE_NEGATIVE%'
  OR (ms.step_code = 'TOPSIS_DISTANCE' AND sa.algorithm_name LIKE '%自救转移能力劣解%');

-- 综合减灾能力包括所有8个二级指标
-- 正确的列名：全部加权后的列
UPDATE step_algorithm sa
JOIN model_step ms ON sa.step_id = ms.id
SET sa.ql_expression = '@TOPSIS_POSITIVE:managementCapabilityWeighted,riskAssessmentCapabilityWeighted,fundingCapabilityWeighted,materialReserveCapabilityWeighted,medicalSupportCapabilityWeighted,selfRescueCapabilityWeighted,publicAvoidanceCapabilityWeighted,relocationCapabilityWeighted'
WHERE ms.step_code = 'TOPSIS_DISTANCE'
  AND (sa.algorithm_code LIKE '%COMPREHENSIVE_POSITIVE%' OR sa.algorithm_name LIKE '%综合减灾能力优解%');

UPDATE step_algorithm sa
JOIN model_step ms ON sa.step_id = ms.id
SET sa.ql_expression = '@TOPSIS_NEGATIVE:managementCapabilityWeighted,riskAssessmentCapabilityWeighted,fundingCapabilityWeighted,materialReserveCapabilityWeighted,medicalSupportCapabilityWeighted,selfRescueCapabilityWeighted,publicAvoidanceCapabilityWeighted,relocationCapabilityWeighted'
WHERE ms.step_code = 'TOPSIS_DISTANCE'
  AND (sa.algorithm_code LIKE '%COMPREHENSIVE_NEGATIVE%' OR sa.algorithm_name LIKE '%综合减灾能力劣解%');

-- 验证更新结果
SELECT
    sa.algorithm_name AS '算法名称',
    sa.algorithm_code AS '算法代码',
    sa.ql_expression AS 'QL表达式（已更新）'
FROM step_algorithm sa
JOIN model_step ms ON sa.step_id = ms.id
WHERE ms.step_code = 'TOPSIS_DISTANCE'
ORDER BY sa.algorithm_order;
