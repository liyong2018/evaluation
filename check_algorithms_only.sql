-- 检查算法表达式
SELECT
    sa.id,
    sa.algorithm_code,
    sa.step_id,
    sa.ql_expression,
    sa.description,
    sa.algorithm_order
FROM step_algorithm sa
WHERE sa.step_id IN (2, 3, 4, 5)  -- 社区模型的主要步骤
ORDER BY sa.step_id, sa.algorithm_order;