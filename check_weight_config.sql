-- 检查权重配置

-- 1. 查看indicator_weight表结构
SELECT '==== indicator_weight表结构 ====' AS info;
DESC indicator_weight;

-- 2. 查看weightConfigId=2的权重配置
SELECT '==== weightConfigId=2的权重配置 ====' AS info;
SELECT 
    indicator_code,
    indicator_name,
    weight_value
FROM indicator_weight
WHERE config_id = 2
ORDER BY indicator_code;

-- 3. 查看模型8使用的权重标识
SELECT '==== 模型8中使用的权重标识 ====' AS info;
SELECT DISTINCT
    SUBSTRING_INDEX(SUBSTRING_INDEX(sa.ql_expression, '@WEIGHT(', -1), ')', 1) AS weight_identifier
FROM step_algorithm sa
JOIN model_step ms ON sa.step_id = ms.id
WHERE ms.model_id = 8
  AND sa.ql_expression LIKE '%@WEIGHT%'
ORDER BY weight_identifier;
