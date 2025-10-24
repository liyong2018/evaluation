-- 检查社区-乡镇能力评估模型（modelId=8）的配置

-- 1. 查看模型信息
SELECT '==== 模型信息 ====' AS info;
SELECT id, model_name, model_code, status
FROM evaluation_model
WHERE id = 8;

-- 2. 查看模型的所有步骤
SELECT '==== 模型步骤 ====' AS info;
SELECT 
    ms.id,
    ms.step_code,
    ms.step_name,
    ms.step_order
FROM model_step ms
WHERE ms.model_id = 8
ORDER BY ms.step_order;

-- 3. 查看二级指标定权步骤的算法配置
SELECT '==== 二级指标定权步骤算法 ====' AS info;
SELECT 
    sa.algorithm_order,
    sa.algorithm_name,
    sa.algorithm_code,
    sa.ql_expression,
    sa.output_param
FROM step_algorithm sa
JOIN model_step ms ON sa.step_id = ms.id
WHERE ms.model_id = 8 AND ms.step_code = 'SECONDARY_WEIGHTING'
ORDER BY sa.algorithm_order;

-- 4. 查看所有步骤的算法配置
SELECT '==== 所有步骤的算法配置 ====' AS info;
SELECT 
    ms.step_order,
    ms.step_name,
    sa.algorithm_order,
    sa.algorithm_name,
    LEFT(sa.ql_expression, 80) AS ql_expression
FROM step_algorithm sa
JOIN model_step ms ON sa.step_id = ms.id
WHERE ms.model_id = 8
ORDER BY ms.step_order, sa.algorithm_order;
