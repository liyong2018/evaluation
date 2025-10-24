-- 诊断社区评估TOPSIS计算问题

-- 1. 查看TOPSIS步骤的算法配置
SELECT '==== TOPSIS算法配置 ====' AS info;
SELECT 
    sa.algorithm_order,
    sa.algorithm_name,
    sa.algorithm_code,
    sa.ql_expression,
    sa.output_param
FROM step_algorithm sa
JOIN model_step ms ON sa.step_id = ms.id
WHERE ms.model_id = 4 AND ms.step_code = 'TOPSIS_DISTANCE'
ORDER BY sa.algorithm_order;

-- 2. 查看加权步骤的输出参数
SELECT '==== 加权步骤输出参数 ====' AS info;
SELECT 
    sa.algorithm_order,
    sa.algorithm_name,
    sa.output_param
FROM step_algorithm sa
JOIN model_step ms ON sa.step_id = ms.id
WHERE ms.model_id = 4 AND ms.step_code = 'WEIGHTING'
ORDER BY sa.algorithm_order;

-- 3. 查看实际数据中的加权值
SELECT '==== 实际加权值数据 ====' AS info;
SELECT 
    region_name,
    PLAN_CONSTRUCTION_WEIGHT,
    HAZARD_INSPECTION_WEIGHT,
    RISK_ASSESSMENT_WEIGHT,
    FINANCIAL_INPUT_WEIGHT
FROM survey_data
WHERE model_id = 4
LIMIT 3;

-- 4. 查看TOPSIS计算结果
SELECT '==== TOPSIS计算结果 ====' AS info;
SELECT 
    region_name,
    MANAGEMENT_POSITIVE_IDEAL,
    MANAGEMENT_NEGATIVE_IDEAL,
    MANAGEMENT_SCORE
FROM survey_data
WHERE model_id = 4
LIMIT 3;
