-- ===================================================================
-- 诊断乡镇聚合问题的SQL脚本
-- ===================================================================
-- 此脚本用于检查社区-乡镇评估模型（modelId=8）的配置
-- 特别是步骤1（社区指标计算）和步骤2（乡镇聚合）之间的字段映射
-- ===================================================================

-- 1. 检查模型配置
SELECT
    m.id AS model_id,
    m.model_name,
    m.model_code,
    s.id AS step_id,
    s.step_order,
    s.step_name,
    s.step_code,
    s.step_type,
    COUNT(a.id) AS algorithm_count
FROM evaluation_model m
LEFT JOIN model_step s ON m.id = s.model_id
LEFT JOIN step_algorithm a ON s.id = a.step_id
WHERE m.id = 8  -- 社区-乡镇评估模型
GROUP BY m.id, m.model_name, m.model_code, s.id, s.step_order, s.step_name, s.step_code, s.step_type
ORDER BY s.step_order;

-- 2. 检查步骤1（社区指标计算）的算法配置
-- 这些算法的 output_param 就是步骤1输出的字段名
SELECT
    '步骤1 - 社区指标计算' AS step_info,
    a.id,
    a.algorithm_code,
    a.algorithm_name,
    a.ql_expression,
    a.input_params,
    a.output_param,  -- ★ 这是步骤1输出的字段名
    a.calculation_order
FROM step_algorithm a
WHERE a.step_id = (
    SELECT s.id FROM model_step s
    WHERE s.model_id = 8 AND s.step_order = 1
)
ORDER BY a.calculation_order;

-- 3. 检查步骤2（乡镇聚合）的算法配置
-- 这些算法的 input_params 应该引用步骤1的 output_param
SELECT
    '步骤2 - 乡镇聚合' AS step_info,
    a.id,
    a.algorithm_code,
    a.algorithm_name,
    a.ql_expression,
    a.input_params,  -- ★ 这应该匹配步骤1的 output_param
    a.output_param,
    a.region_aggregation,
    a.calculation_order
FROM step_algorithm a
WHERE a.step_id = (
    SELECT s.id FROM model_step s
    WHERE s.model_id = 8 AND s.step_order = 2
)
ORDER BY a.calculation_order;

-- 4. 交叉检查：查看步骤1的输出是否与步骤2的输入匹配
SELECT
    '字段映射检查' AS check_type,
    step1.output_param AS step1_output,
    step2.input_params AS step2_input,
    step2.algorithm_name AS step2_algorithm,
    CASE
        WHEN step2.input_params = step1.output_param THEN '✓ 匹配'
        WHEN step2.input_params LIKE CONCAT('%', step1.output_param, '%') THEN '⚠ 部分匹配'
        ELSE '✗ 不匹配'
    END AS match_status
FROM
    step_algorithm step1
    CROSS JOIN step_algorithm step2
WHERE
    step1.step_id = (SELECT s.id FROM model_step s WHERE s.model_id = 8 AND s.step_order = 1)
    AND step2.step_id = (SELECT s.id FROM model_step s WHERE s.model_id = 8 AND s.step_order = 2)
    AND step1.output_param IS NOT NULL
    AND step2.input_params IS NOT NULL
    AND (
        step2.input_params = step1.output_param
        OR step2.input_params LIKE CONCAT('%', step1.output_param, '%')
    )
ORDER BY step1.calculation_order, step2.calculation_order;

-- 5. 检查最近一次评估结果
SELECT
    er.id,
    er.evaluation_model_id,
    er.region_code,
    er.region_name,
    er.management_capability_score,
    er.support_capability_score,
    er.self_rescue_capability_score,
    er.created_at
FROM evaluation_result er
WHERE er.evaluation_model_id = 8
ORDER BY er.id DESC
LIMIT 10;

-- 6. 检查社区数据表的字段（前3条记录）
SELECT *
FROM community_disaster_reduction_capacity
LIMIT 3;

-- ===================================================================
-- 使用方法：
-- ===================================================================
-- 1. 将此脚本保存为 diagnose_township_aggregation.sql
-- 2. 在命令行执行：
--    mysql -h192.168.15.203 -P30314 -uroot -p123456 evaluate_db < diagnose_township_aggregation.sql > diagnosis_result.txt
-- 3. 或者在MySQL客户端中逐段执行
-- ===================================================================

-- ===================================================================
-- 预期结果：
-- ===================================================================
-- 查询2应该显示步骤1的13个算法，每个都有 output_param
-- 查询3应该显示步骤2的算法，每个的 input_params 应该匹配步骤1的某个 output_param
-- 查询4应该显示所有匹配的字段对，如果没有匹配项，说明配置有问题
-- ===================================================================
