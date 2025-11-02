-- 手动修复：将括号格式改为冒号格式
-- 找到综合减灾模型的第二步

-- 1. 查看当前配置
SELECT
    sa.id,
    sa.algorithm_name,
    sa.ql_expression AS 当前格式_错误,
    REPLACE(sa.ql_expression, '(', ':') AS 应该改为,
    REPLACE(REPLACE(sa.ql_expression, '(', ':'), ')', '') AS 最终格式_正确
FROM step_algorithm sa
JOIN model_step ms ON sa.step_id = ms.id
JOIN evaluation_model em ON ms.model_id = em.id
WHERE em.model_code = 'COMPREHENSIVE_MODEL'
  AND ms.step_order = 2
ORDER BY sa.algorithm_order;

-- 2. 执行修复（将 @NORMALIZE(xxx) 改为 @NORMALIZE:xxx）
UPDATE step_algorithm sa
JOIN model_step ms ON sa.step_id = ms.id
JOIN evaluation_model em ON ms.model_id = em.id
SET sa.ql_expression = REPLACE(REPLACE(sa.ql_expression, '(', ':'), ')', '')
WHERE em.model_code = 'COMPREHENSIVE_MODEL'
  AND ms.step_order = 2
  AND sa.ql_expression LIKE '@NORMALIZE(%';

-- 3. 验证修复结果
SELECT
    sa.algorithm_order,
    sa.algorithm_name,
    sa.ql_expression AS 修复后的表达式
FROM step_algorithm sa
JOIN model_step ms ON sa.step_id = ms.id
JOIN evaluation_model em ON ms.model_id = em.id
WHERE em.model_code = 'COMPREHENSIVE_MODEL'
  AND ms.step_order = 2
ORDER BY sa.algorithm_order;
