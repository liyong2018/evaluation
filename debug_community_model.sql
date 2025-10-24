-- 检查社区模型的配置

-- 1. 查看社区模型的基本信息
SELECT * FROM evaluation_model WHERE id = 4;

-- 2. 查看社区模型的所有步骤
SELECT
    ms.id AS step_id,
    ms.step_order AS '顺序',
    ms.step_code AS '步骤代码',
    ms.step_name AS '步骤名称'
FROM model_step ms
WHERE ms.model_id = 4
ORDER BY ms.step_order;

-- 3. 查看INDICATOR_ASSIGNMENT步骤的所有算法及其输出列名
SELECT
    sa.algorithm_order AS '顺序',
    sa.algorithm_name AS '算法名称',
    sa.ql_expression AS 'QL表达式',
    sa.output_param AS '输出列名'
FROM step_algorithm sa
JOIN model_step ms ON sa.step_id = ms.id
WHERE ms.model_id = 4
  AND ms.step_code = 'INDICATOR_ASSIGNMENT'
ORDER BY sa.algorithm_order;

-- 4. 查看VECTOR_NORMALIZATION步骤的算法配置
SELECT
    sa.algorithm_order AS '顺序',
    sa.algorithm_name AS '算法名称',
    sa.ql_expression AS 'QL表达式（期望的输入列名）',
    sa.output_param AS '输出列名'
FROM step_algorithm sa
JOIN model_step ms ON sa.step_id = ms.id
WHERE ms.model_id = 4
  AND ms.step_code = 'VECTOR_NORMALIZATION'
ORDER BY sa.algorithm_order;

-- 5. 查看SECONDARY_WEIGHTING步骤的算法配置
SELECT
    sa.algorithm_order AS '顺序',
    sa.algorithm_name AS '算法名称',
    sa.ql_expression AS 'QL表达式',
    sa.output_param AS '输出列名'
FROM step_algorithm sa
JOIN model_step ms ON sa.step_id = ms.id
WHERE ms.model_id = 4
  AND ms.step_code = 'SECONDARY_WEIGHTING'
ORDER BY sa.algorithm_order;

-- 6. 检查社区数据表中是否有测试数据
SELECT COUNT(*) AS '社区数据记录数' FROM community_disaster_reduction_capacity;
SELECT * FROM community_disaster_reduction_capacity WHERE region_code = '511425001001' LIMIT 1;
