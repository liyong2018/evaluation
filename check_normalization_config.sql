-- 检查归一化步骤的配置
-- 确保列名与INDICATOR_ASSIGNMENT步骤的输出列名一致

-- 查看当前归一化步骤的配置
SELECT
    sa.id,
    sa.algorithm_name AS '算法名称',
    sa.algorithm_code AS '算法代码',
    sa.algorithm_order AS '顺序',
    sa.ql_expression AS 'QL表达式',
    sa.output_param AS '输出参数'
FROM step_algorithm sa
JOIN model_step ms ON sa.step_id = ms.id
WHERE ms.step_code = 'VECTOR_NORMALIZATION'
ORDER BY sa.algorithm_order;

-- 查看INDICATOR_ASSIGNMENT步骤的输出参数
SELECT
    sa.id,
    sa.algorithm_name AS '算法名称',
    sa.output_param AS '输出参数（应该被归一化步骤使用）'
FROM step_algorithm sa
JOIN model_step ms ON sa.step_id = ms.id
WHERE ms.step_code = 'INDICATOR_ASSIGNMENT'
ORDER BY sa.algorithm_order;
