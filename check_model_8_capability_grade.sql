-- 检查模型8的能力值计算与分级步骤

SELECT 
    sa.algorithm_order,
    sa.algorithm_name,
    sa.ql_expression
FROM step_algorithm sa
JOIN model_step ms ON sa.step_id = ms.id
WHERE ms.model_id = 8 AND ms.step_code = 'CAPABILITY_GRADE'
ORDER BY sa.algorithm_order;
