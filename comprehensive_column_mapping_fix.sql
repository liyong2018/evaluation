-- 完整的列名映射修复脚本
-- 确保每个步骤的输出列名与下一个步骤的输入列名完全匹配

-- ===========================
-- 步骤1: INDICATOR_ASSIGNMENT（评估指标赋值）
-- 输出列：managementCapability, riskAssessmentCapability, fundingCapability, materialReserveCapability,
--        medicalSupportCapability, selfRescueCapability, publicAvoidanceCapability, relocationCapability
-- ===========================

-- ===========================
-- 步骤2: VECTOR_NORMALIZATION（属性向量归一化）
-- 输入：步骤1的输出列
-- 输出列：列名+Norm（例如：managementCapabilityNorm）
-- ===========================

-- 更新归一化步骤的QL表达式
UPDATE step_algorithm sa
JOIN model_step ms ON sa.step_id = ms.id
SET
    sa.ql_expression = CASE sa.algorithm_code
        WHEN 'MANAGEMENT_NORM' THEN '@NORMALIZE:managementCapability'
        WHEN 'RISK_ASSESSMENT_NORM' THEN '@NORMALIZE:riskAssessmentCapability'
        WHEN 'FUNDING_NORM' THEN '@NORMALIZE:fundingCapability'
        WHEN 'MATERIAL_RESERVE_NORM' THEN '@NORMALIZE:materialReserveCapability'
        WHEN 'MEDICAL_SUPPORT_NORM' THEN '@NORMALIZE:medicalSupportCapability'
        WHEN 'SELF_RESCUE_NORM' THEN '@NORMALIZE:selfRescueCapability'
        WHEN 'PUBLIC_AVOIDANCE_NORM' THEN '@NORMALIZE:publicAvoidanceCapability'
        WHEN 'RELOCATION_NORM' THEN '@NORMALIZE:relocationCapability'
        ELSE sa.ql_expression
    END,
    sa.output_param = CASE sa.algorithm_code
        WHEN 'MANAGEMENT_NORM' THEN 'managementCapabilityNorm'
        WHEN 'RISK_ASSESSMENT_NORM' THEN 'riskAssessmentCapabilityNorm'
        WHEN 'FUNDING_NORM' THEN 'fundingCapabilityNorm'
        WHEN 'MATERIAL_RESERVE_NORM' THEN 'materialReserveCapabilityNorm'
        WHEN 'MEDICAL_SUPPORT_NORM' THEN 'medicalSupportCapabilityNorm'
        WHEN 'SELF_RESCUE_NORM' THEN 'selfRescueCapabilityNorm'
        WHEN 'PUBLIC_AVOIDANCE_NORM' THEN 'publicAvoidanceCapabilityNorm'
        WHEN 'RELOCATION_NORM' THEN 'relocationCapabilityNorm'
        ELSE sa.output_param
    END
WHERE ms.step_code = 'VECTOR_NORMALIZATION';

-- ===========================
-- 步骤3: SECONDARY_WEIGHTING（二级指标定权）
-- 输入：步骤2的输出列（Norm结尾的列）
-- 输出列：列名+Weighted（例如：managementCapabilityWeighted）
-- ===========================

-- ===========================
-- 步骤4: TOPSIS_DISTANCE（TOPSIS评分）
-- 输入：步骤3的输出列（Weighted结尾的列）
-- 输出列：各一级指标的优解和劣解距离
-- ===========================

-- 更新TOPSIS步骤的QL表达式
UPDATE step_algorithm sa
JOIN model_step ms ON sa.step_id = ms.id
SET
    sa.ql_expression = CASE
        -- 灾害管理能力（包括：队伍管理、风险评估、财政投入）
        WHEN sa.algorithm_name LIKE '%灾害管理能力优解%' OR sa.output_param = 'disasterMgmtPositive'
            THEN '@TOPSIS_POSITIVE:managementCapabilityWeighted,riskAssessmentCapabilityWeighted,fundingCapabilityWeighted'
        WHEN sa.algorithm_name LIKE '%灾害管理能力劣解%' OR sa.output_param = 'disasterMgmtNegative'
            THEN '@TOPSIS_NEGATIVE:managementCapabilityWeighted,riskAssessmentCapabilityWeighted,fundingCapabilityWeighted'

        -- 灾害备灾能力（包括：物资储备、医疗保障）
        WHEN sa.algorithm_name LIKE '%灾害备灾能力优解%' OR sa.output_param = 'disasterPrepPositive'
            THEN '@TOPSIS_POSITIVE:materialReserveCapabilityWeighted,medicalSupportCapabilityWeighted'
        WHEN sa.algorithm_name LIKE '%灾害备灾能力劣解%' OR sa.output_param = 'disasterPrepNegative'
            THEN '@TOPSIS_NEGATIVE:materialReserveCapabilityWeighted,medicalSupportCapabilityWeighted'

        -- 自救转移能力（包括：自救互救、公众避险、转移安置）
        WHEN sa.algorithm_name LIKE '%自救转移能力优解%' OR sa.output_param = 'selfRescuePositive'
            THEN '@TOPSIS_POSITIVE:selfRescueCapabilityWeighted,publicAvoidanceCapabilityWeighted,relocationCapabilityWeighted'
        WHEN sa.algorithm_name LIKE '%自救转移能力劣解%' OR sa.output_param = 'selfRescueNegative'
            THEN '@TOPSIS_NEGATIVE:selfRescueCapabilityWeighted,publicAvoidanceCapabilityWeighted,relocationCapabilityWeighted'

        -- 综合减灾能力（包括：所有8个二级指标）
        WHEN sa.algorithm_name LIKE '%综合减灾能力优解%' OR sa.output_param = 'comprehensivePositive'
            THEN '@TOPSIS_POSITIVE:managementCapabilityWeighted,riskAssessmentCapabilityWeighted,fundingCapabilityWeighted,materialReserveCapabilityWeighted,medicalSupportCapabilityWeighted,selfRescueCapabilityWeighted,publicAvoidanceCapabilityWeighted,relocationCapabilityWeighted'
        WHEN sa.algorithm_name LIKE '%综合减灾能力劣解%' OR sa.output_param = 'comprehensiveNegative'
            THEN '@TOPSIS_NEGATIVE:managementCapabilityWeighted,riskAssessmentCapabilityWeighted,fundingCapabilityWeighted,materialReserveCapabilityWeighted,medicalSupportCapabilityWeighted,selfRescueCapabilityWeighted,publicAvoidanceCapabilityWeighted,relocationCapabilityWeighted'

        ELSE sa.ql_expression
    END
WHERE ms.step_code = 'TOPSIS_DISTANCE';

-- 验证所有步骤的配置
SELECT '===== 步骤1: INDICATOR_ASSIGNMENT =====' as '';
SELECT
    sa.algorithm_name AS '算法名称',
    sa.output_param AS '输出列名'
FROM step_algorithm sa
JOIN model_step ms ON sa.step_id = ms.id
WHERE ms.step_code = 'INDICATOR_ASSIGNMENT'
ORDER BY sa.algorithm_order;

SELECT '===== 步骤2: VECTOR_NORMALIZATION =====' as '';
SELECT
    sa.algorithm_name AS '算法名称',
    sa.ql_expression AS 'QL表达式（输入列）',
    sa.output_param AS '输出列名'
FROM step_algorithm sa
JOIN model_step ms ON sa.step_id = ms.id
WHERE ms.step_code = 'VECTOR_NORMALIZATION'
ORDER BY sa.algorithm_order;

SELECT '===== 步骤3: SECONDARY_WEIGHTING =====' as '';
SELECT
    sa.algorithm_name AS '算法名称',
    sa.output_param AS '输出列名'
FROM step_algorithm sa
JOIN model_step ms ON sa.step_id = ms.id
WHERE ms.step_code = 'SECONDARY_WEIGHTING'
ORDER BY sa.algorithm_order;

SELECT '===== 步骤4: TOPSIS_DISTANCE =====' as '';
SELECT
    sa.algorithm_name AS '算法名称',
    sa.ql_expression AS 'QL表达式（输入列）',
    sa.output_param AS '输出列名'
FROM step_algorithm sa
JOIN model_step ms ON sa.step_id = ms.id
WHERE ms.step_code = 'TOPSIS_DISTANCE'
ORDER BY sa.algorithm_order;
