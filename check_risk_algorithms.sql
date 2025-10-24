-- 检查风险评估相关的算法表达式
SELECT
    sa.id,
    sa.algorithm_code,
    sa.step_id,
    sa.ql_expression,
    sa.description
FROM step_algorithm sa
WHERE sa.algorithm_code LIKE '%RISK%' OR sa.algorithm_code LIKE '%TEAM%'
ORDER BY sa.step_id, sa.algorithm_order;