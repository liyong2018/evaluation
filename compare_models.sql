-- 比较模型4和模型8的二级指标定权步骤

SELECT '==== 模型4（社区-行政村）的二级指标定权 ====' AS info;
SELECT 
    sa.algorithm_order,
    sa.algorithm_name,
    sa.ql_expression
FROM step_algorithm sa
JOIN model_step ms ON sa.step_id = ms.id
WHERE ms.model_id = 4 AND ms.step_code = 'SECONDARY_WEIGHTING'
ORDER BY sa.algorithm_order;

SELECT '==== 模型8（社区-乡镇）的二级指标定权 ====' AS info;
SELECT 
    sa.algorithm_order,
    sa.algorithm_name,
    sa.ql_expression
FROM step_algorithm sa
JOIN model_step ms ON sa.step_id = ms.id
WHERE ms.model_id = 8 AND ms.step_code = 'SECONDARY_WEIGHTING'
ORDER BY sa.algorithm_order;
