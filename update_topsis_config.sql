-- 更新TOPSIS步骤配置，确保使用正确的归一化后的列名
-- 归一化后的列名应该是：
-- managementCapabilityNorm, riskAssessmentCapabilityNorm, fundingCapabilityNorm, materialReserveCapabilityNorm,
-- medicalSupportCapabilityNorm, selfRescueCapabilityNorm, publicAvoidanceCapabilityNorm, relocationCapabilityNorm

-- 但是SECONDARY_WEIGHTING步骤会对这些归一化值进行加权，输出列名需要检查

-- 首先查看SECONDARY_WEIGHTING步骤的输出
SELECT
    sa.algorithm_name AS '算法名称',
    sa.algorithm_code AS '算法代码',
    sa.output_param AS '输出参数（应该被TOPSIS使用）'
FROM step_algorithm sa
JOIN model_step ms ON sa.step_id = ms.id
WHERE ms.step_code = 'SECONDARY_WEIGHTING'
ORDER BY sa.algorithm_order;

-- 查看当前TOPSIS步骤的配置
SELECT
    sa.id,
    sa.algorithm_name AS '算法名称',
    sa.algorithm_code AS '算法代码',
    sa.ql_expression AS 'QL表达式'
FROM step_algorithm sa
JOIN model_step ms ON sa.step_id = ms.id
WHERE ms.step_code = 'TOPSIS_DISTANCE'
ORDER BY sa.algorithm_order;
