-- 检查模型8的字段映射问题

-- 归一化步骤的输出
SELECT '==== 归一化步骤输出 ====' AS info;
SELECT 
    algorithm_order,
    algorithm_name,
    output_param
FROM step_algorithm sa
JOIN model_step ms ON sa.step_id = ms.id
WHERE ms.model_id = 8 AND ms.step_code = 'VECTOR_NORMALIZATION'
ORDER BY algorithm_order;

-- 二级指标定权步骤使用的输入字段
SELECT '==== 二级指标定权步骤 ====' AS info;
SELECT 
    algorithm_order,
    algorithm_name,
    ql_expression
FROM step_algorithm sa
JOIN model_step ms ON sa.step_id = ms.id
WHERE ms.model_id = 8 AND ms.step_code = 'SECONDARY_WEIGHTING'
ORDER BY algorithm_order;
