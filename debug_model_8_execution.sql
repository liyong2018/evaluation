-- 调试模型8的执行问题

-- 1. 检查社区指标计算步骤
SELECT '==== 步骤1：社区指标计算 ====' AS info;
SELECT 
    sa.algorithm_order,
    sa.algorithm_name,
    LEFT(sa.ql_expression, 80) AS ql_expression,
    sa.output_param
FROM step_algorithm sa
JOIN model_step ms ON sa.step_id = ms.id
WHERE ms.model_id = 8 AND ms.step_code = 'COMMUNITY_INDICATORS'
ORDER BY sa.algorithm_order;

-- 2. 检查乡镇数据聚合步骤
SELECT '==== 步骤2：乡镇数据聚合 ====' AS info;
SELECT 
    sa.algorithm_order,
    sa.algorithm_name,
    sa.ql_expression,
    sa.output_param
FROM step_algorithm sa
JOIN model_step ms ON sa.step_id = ms.id
WHERE ms.model_id = 8 AND ms.step_code = 'TOWNSHIP_AGGREGATION'
ORDER BY sa.algorithm_order;

-- 3. 检查属性向量归一化步骤
SELECT '==== 步骤3：属性向量归一化 ====' AS info;
SELECT 
    sa.algorithm_order,
    sa.algorithm_name,
    LEFT(sa.ql_expression, 80) AS ql_expression,
    sa.output_param
FROM step_algorithm sa
JOIN model_step ms ON sa.step_id = ms.id
WHERE ms.model_id = 8 AND ms.step_code = 'VECTOR_NORMALIZATION'
ORDER BY sa.algorithm_order;

-- 4. 检查优劣解计算步骤
SELECT '==== 步骤5：优劣解计算 ====' AS info;
SELECT 
    sa.algorithm_order,
    sa.algorithm_name,
    LEFT(sa.ql_expression, 80) AS ql_expression,
    sa.output_param
FROM step_algorithm sa
JOIN model_step ms ON sa.step_id = ms.id
WHERE ms.model_id = 8 AND ms.step_code = 'TOPSIS_DISTANCE'
ORDER BY sa.algorithm_order;

-- 5. 检查community_disaster_reduction_capacity表是否有数据
SELECT '==== 检查社区数据表 ====' AS info;
SELECT COUNT(*) AS '社区数据总数'
FROM community_disaster_reduction_capacity;

SELECT '==== 检查社区数据样例 ====' AS info;
SELECT 
    region_code,
    has_emergency_plan,
    resident_population,
    last_year_funding_amount
FROM community_disaster_reduction_capacity
LIMIT 3;
