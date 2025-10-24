-- 修复模型8的能力值计算与分级步骤
-- 将@WEIGHT(PERCENTAGE_MULTIPLIER)替换为100（将0-1的得分转换为0-100的百分制）

SELECT '==== 修复前的配置 ====' AS info;
SELECT 
    sa.algorithm_order,
    sa.algorithm_name,
    sa.ql_expression
FROM step_algorithm sa
JOIN model_step ms ON sa.step_id = ms.id
WHERE ms.model_id = 8 AND ms.step_code = 'CAPABILITY_GRADE'
ORDER BY sa.algorithm_order;

-- 算法1: Capability Score
-- RELATIVE_CLOSENESS是0-1之间的值，乘以100转换为百分制
UPDATE step_algorithm sa
JOIN model_step ms ON sa.step_id = ms.id
SET sa.ql_expression = 'RELATIVE_CLOSENESS != null ? RELATIVE_CLOSENESS * 100.0 : 50.0'
WHERE ms.model_id = 8
  AND ms.step_code = 'CAPABILITY_GRADE'
  AND sa.algorithm_order = 1;

SELECT '==== 修复后的配置 ====' AS info;
SELECT 
    sa.algorithm_order,
    sa.algorithm_name,
    sa.ql_expression
FROM step_algorithm sa
JOIN model_step ms ON sa.step_id = ms.id
WHERE ms.model_id = 8 AND ms.step_code = 'CAPABILITY_GRADE'
ORDER BY sa.algorithm_order;
