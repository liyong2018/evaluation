-- 检查模型8中所有使用@WEIGHT的地方

SELECT 
    ms.step_name,
    sa.algorithm_order,
    sa.algorithm_name,
    sa.ql_expression
FROM step_algorithm sa
JOIN model_step ms ON sa.step_id = ms.id
WHERE ms.model_id = 8
  AND sa.ql_expression LIKE '%@WEIGHT%'
ORDER BY ms.step_order, sa.algorithm_order;
