-- 检查队伍管理能力相关的算法表达式
SELECT
    sa.id,
    sa.algorithm_code,
    sa.step_id,
    sa.ql_expression,
    sa.description
FROM step_algorithm sa
WHERE sa.algorithm_code LIKE '%TEAM%' OR sa.algorithm_code LIKE '%MANAGEMENT%'
ORDER BY sa.step_id, sa.algorithm_order;