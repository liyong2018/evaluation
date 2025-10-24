-- 检查TOPSIS得分计算步骤是否存在

SELECT '==== 查看社区模型的所有步骤 ====' AS info;
SELECT 
    ms.id,
    ms.step_code,
    ms.step_name,
    ms.step_order
FROM model_step ms
WHERE ms.model_id = 4
ORDER BY ms.step_order;

SELECT '==== 查看TOPSIS_SCORE步骤的算法 ====' AS info;
SELECT 
    sa.id,
    sa.algorithm_order,
    sa.algorithm_name,
    sa.algorithm_code,
    sa.output_param
FROM step_algorithm sa
JOIN model_step ms ON sa.step_id = ms.id
WHERE ms.model_id = 4 AND ms.step_code = 'TOPSIS_SCORE'
ORDER BY sa.algorithm_order;

SELECT '==== 查看所有包含SCORE的步骤 ====' AS info;
SELECT 
    ms.id,
    ms.step_code,
    ms.step_name,
    ms.step_order
FROM model_step ms
WHERE ms.model_id = 4 AND (ms.step_code LIKE '%SCORE%' OR ms.step_name LIKE '%得分%')
ORDER BY ms.step_order;
