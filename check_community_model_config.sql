-- 检查社区模型(modelId=4)的配置

-- 查看模型基本信息
SELECT * FROM evaluation_model WHERE id = 4;

-- 查看模型的所有步骤
SELECT
    ms.id,
    ms.step_name AS '步骤名称',
    ms.step_code AS '步骤代码',
    ms.step_order AS '顺序'
FROM model_step ms
WHERE ms.model_id = 4
ORDER BY ms.step_order;

-- 查看二级指标定权步骤的所有算法
SELECT
    sa.id,
    sa.algorithm_name AS '算法名称',
    sa.algorithm_code AS '算法代码',
    sa.algorithm_order AS '顺序',
    sa.ql_expression AS 'QL表达式',
    sa.input_params AS '输入参数',
    sa.output_param AS '输出参数'
FROM step_algorithm sa
JOIN model_step ms ON sa.step_id = ms.id
WHERE ms.model_id = 4
  AND ms.step_code = 'SECONDARY_WEIGHTING'
ORDER BY sa.algorithm_order;

-- 查看归一化步骤的所有算法（上一步）
SELECT
    sa.id,
    sa.algorithm_name AS '算法名称',
    sa.algorithm_code AS '算法代码',
    sa.ql_expression AS 'QL表达式',
    sa.output_param AS '输出参数'
FROM step_algorithm sa
JOIN model_step ms ON sa.step_id = ms.id
WHERE ms.model_id = 4
  AND ms.step_code = 'VECTOR_NORMALIZATION'
ORDER BY sa.algorithm_order;
