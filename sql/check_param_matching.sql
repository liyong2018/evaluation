-- =====================================================
-- 检查第一步的output_param配置是否正确
-- =====================================================

-- 查看第一步（DATA_FETCH或DATA_FUSION）的算法配置
SELECT
    '=== 第一步算法配置（输出参数很关键！） ===' AS info;

SELECT
    sa.algorithm_order AS 顺序,
    sa.algorithm_name AS 算法名称,
    sa.algorithm_code AS 算法代码,
    sa.ql_expression AS QL表达式,
    sa.output_param AS 输出参数,
    LENGTH(sa.output_param) AS 参数长度,
    HEX(sa.output_param) AS 参数十六进制
FROM step_algorithm sa
JOIN model_step ms ON sa.step_id = ms.id
JOIN evaluation_model em ON ms.model_id = em.id
WHERE em.model_code = 'COMPREHENSIVE_MODEL'
  AND ms.step_order = 1
ORDER BY sa.algorithm_order;

-- 查看第二步的输入参数（应该与第一步的输出参数匹配）
SELECT
    '=== 第二步算法配置（输入参数应与第一步输出参数匹配） ===' AS info;

SELECT
    sa.algorithm_order AS 顺序,
    sa.algorithm_name AS 算法名称,
    sa.algorithm_code AS 算法代码,
    sa.ql_expression AS QL表达式,
    SUBSTRING_INDEX(sa.ql_expression, ':', -1) AS 提取的参数名称,
    sa.output_param AS 输出参数
FROM step_algorithm sa
JOIN model_step ms ON sa.step_id = ms.id
JOIN evaluation_model em ON ms.model_id = em.id
WHERE em.model_code = 'COMPREHENSIVE_MODEL'
  AND ms.step_order = 2
ORDER BY sa.algorithm_order;

-- 对比分析：第一步的输出是否与第二步的输入匹配
SELECT
    '=== 参数匹配分析 ===' AS info;

SELECT
    step1.algorithm_order AS 第一步顺序,
    step1.output_param AS 第一步输出参数,
    step2.ql_expression AS 第二步表达式,
    SUBSTRING_INDEX(step2.ql_expression, ':', -1) AS 第二步需要的参数,
    CASE
        WHEN step1.output_param = SUBSTRING_INDEX(step2.ql_expression, ':', -1) THEN '✅ 匹配'
        WHEN TRIM(step1.output_param) = TRIM(SUBSTRING_INDEX(step2.ql_expression, ':', -1)) THEN '⚠️ 有空格但可匹配'
        ELSE '❌ 不匹配！'
    END AS 匹配状态
FROM (
    SELECT sa.algorithm_order, sa.output_param
    FROM step_algorithm sa
    JOIN model_step ms ON sa.step_id = ms.id
    JOIN evaluation_model em ON ms.model_id = em.id
    WHERE em.model_code = 'COMPREHENSIVE_MODEL' AND ms.step_order = 1
) step1
LEFT JOIN (
    SELECT sa.algorithm_order, sa.ql_expression
    FROM step_algorithm sa
    JOIN model_step ms ON sa.step_id = ms.id
    JOIN evaluation_model em ON ms.model_id = em.id
    WHERE em.model_code = 'COMPREHENSIVE_MODEL' AND ms.step_order = 2
) step2 ON step1.algorithm_order = step2.algorithm_order
ORDER BY step1.algorithm_order;

-- 检查是否有隐藏字符或空格
SELECT
    '=== 检查output_param中的隐藏字符 ===' AS info;

SELECT
    algorithm_name,
    output_param AS 原始值,
    TRIM(output_param) AS 去空格后,
    CHAR_LENGTH(output_param) AS 原始长度,
    CHAR_LENGTH(TRIM(output_param)) AS 去空格后长度,
    CASE
        WHEN CHAR_LENGTH(output_param) > CHAR_LENGTH(TRIM(output_param)) THEN '⚠️ 有前后空格'
        ELSE '✅ 无空格'
    END AS 空格检查
FROM step_algorithm sa
JOIN model_step ms ON sa.step_id = ms.id
JOIN evaluation_model em ON ms.model_id = em.id
WHERE em.model_code = 'COMPREHENSIVE_MODEL'
  AND ms.step_order = 1
ORDER BY algorithm_order;
