-- 检查算法表达式和数据库实际数据
-- 问题：很多评估指标赋值都是0，需要检查算法表达式和数据匹配

-- 先检查step_algorithm表结构
DESCRIBE step_algorithm;

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

-- 检查调查数据中的风险评估字段实际值
SELECT
    region_code,
    risk_assessment,
    management_staff,
    population
FROM survey_data
WHERE region_code = '110112'  -- 测试区域
LIMIT 10;