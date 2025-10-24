-- 检查能力值计算与分级步骤的配置

SELECT '==== 能力值计算与分级步骤的算法配置 ====' AS info;
SELECT 
    sa.id,
    sa.algorithm_order,
    sa.algorithm_name,
    sa.algorithm_code,
    sa.ql_expression,
    sa.output_param
FROM step_algorithm sa
JOIN model_step ms ON sa.step_id = ms.id
WHERE ms.model_id = 4 AND ms.step_code = 'CAPABILITY_GRADE'
ORDER BY sa.algorithm_order;
