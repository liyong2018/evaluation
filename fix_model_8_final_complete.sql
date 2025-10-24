-- 模型8最终完整修复脚本
-- 包括：@WEIGHT标记 + 字段映射 + 归一化输入字段

SELECT '========================================' AS info;
SELECT '模型8最终完整修复脚本' AS info;
SELECT '========================================' AS info;

-- ========================================
-- 第一部分：修复@WEIGHT标记
-- ========================================

SELECT '' AS info;
SELECT '第一部分：修复@WEIGHT标记...' AS info;

UPDATE step_algorithm sa
JOIN model_step ms ON sa.step_id = ms.id
SET sa.ql_expression = '(MATERIAL_RESERVE_NORM != null ? MATERIAL_RESERVE_NORM : 0.0) * 0.52 + (MEDICAL_SUPPORT_NORM != null ? MEDICAL_SUPPORT_NORM : 0.0) * 0.48'
WHERE ms.model_id = 8 AND ms.step_code = 'SECONDARY_WEIGHTING' AND sa.algorithm_order = 2;

UPDATE step_algorithm sa
JOIN model_step ms ON sa.step_id = ms.id
SET sa.ql_expression = '(SELF_MUTUAL_AID_NORM != null ? SELF_MUTUAL_AID_NORM : 0.0) * 0.33 + (PUBLIC_EVACUATION_NORM != null ? PUBLIC_EVACUATION_NORM : 0.0) * 0.34'
WHERE ms.model_id = 8 AND ms.step_code = 'SECONDARY_WEIGHTING' AND sa.algorithm_order = 3;

UPDATE step_algorithm sa
JOIN model_step ms ON sa.step_id = ms.id
SET sa.ql_expression = '(MANAGEMENT_WEIGHT != null ? MANAGEMENT_WEIGHT : 0.0) * 0.32 + (SUPPORT_WEIGHT != null ? SUPPORT_WEIGHT : 0.0) * 0.31 + (CAPABILITY_WEIGHT != null ? CAPABILITY_WEIGHT : 0.0) * 0.37 + (FACILITY_WEIGHT != null ? FACILITY_WEIGHT : 0.0) * 0.37'
WHERE ms.model_id = 8 AND ms.step_code = 'SECONDARY_WEIGHTING' AND sa.algorithm_order = 5;

UPDATE step_algorithm sa
JOIN model_step ms ON sa.step_id = ms.id
SET sa.ql_expression = 'RELATIVE_CLOSENESS != null ? RELATIVE_CLOSENESS * 100.0 : 50.0'
WHERE ms.model_id = 8 AND ms.step_code = 'CAPABILITY_GRADE' AND sa.algorithm_order = 1;

SELECT '@WEIGHT标记修复完成' AS status;

-- ========================================
-- 第二部分：修复输出参数字段名
-- ========================================

SELECT '' AS info;
SELECT '第二部分：修复输出参数字段名...' AS info;

UPDATE step_algorithm sa
JOIN model_step ms ON sa.step_id = ms.id
SET sa.output_param = 'RISK_ASSESSMENT_NORM'
WHERE ms.model_id = 8 AND ms.step_code = 'VECTOR_NORMALIZATION' AND sa.algorithm_order = 3;

UPDATE step_algorithm sa
JOIN model_step ms ON sa.step_id = ms.id
SET sa.output_param = 'MATERIAL_RESERVE_NORM'
WHERE ms.model_id = 8 AND ms.step_code = 'VECTOR_NORMALIZATION' AND sa.algorithm_order = 5;

UPDATE step_algorithm sa
JOIN model_step ms ON sa.step_id = ms.id
SET sa.output_param = 'MEDICAL_SUPPORT_NORM'
WHERE ms.model_id = 8 AND ms.step_code = 'VECTOR_NORMALIZATION' AND sa.algorithm_order = 6;

UPDATE step_algorithm sa
JOIN model_step ms ON sa.step_id = ms.id
SET sa.output_param = 'TOWNSHIP_AID_AGG'
WHERE ms.model_id = 8 AND ms.step_code = 'TOWNSHIP_AGGREGATION' AND sa.algorithm_order = 7;

SELECT '输出参数字段名修复完成' AS status;

-- ========================================
-- 第三部分：修复归一化输入字段名
-- ========================================

SELECT '' AS info;
SELECT '第三部分：修复归一化输入字段名...' AS info;

UPDATE step_algorithm sa
JOIN model_step ms ON sa.step_id = ms.id
SET sa.ql_expression = '@NORMALIZE:RISK_ASSESSMENT'
WHERE ms.model_id = 8 AND ms.step_code = 'VECTOR_NORMALIZATION' AND sa.algorithm_order = 3;

UPDATE step_algorithm sa
JOIN model_step ms ON sa.step_id = ms.id
SET sa.ql_expression = '@NORMALIZE:MATERIAL_RESERVE'
WHERE ms.model_id = 8 AND ms.step_code = 'VECTOR_NORMALIZATION' AND sa.algorithm_order = 5;

UPDATE step_algorithm sa
JOIN model_step ms ON sa.step_id = ms.id
SET sa.ql_expression = '@NORMALIZE:MEDICAL_SUPPORT'
WHERE ms.model_id = 8 AND ms.step_code = 'VECTOR_NORMALIZATION' AND sa.algorithm_order = 6;

SELECT '归一化输入字段名修复完成' AS status;

-- ========================================
-- 验证修复结果
-- ========================================

SELECT '' AS info;
SELECT '========================================' AS info;
SELECT '验证修复结果' AS info;
SELECT '========================================' AS info;

SELECT '' AS info;
SELECT '1. 检查@WEIGHT标记（应该为0）' AS check_item;
SELECT COUNT(*) AS '剩余@WEIGHT标记数量'
FROM step_algorithm sa
JOIN model_step ms ON sa.step_id = ms.id
WHERE ms.model_id = 8 AND sa.ql_expression LIKE '%@WEIGHT%';

SELECT '' AS info;
SELECT '2. 检查归一化步骤配置' AS check_item;
SELECT 
    algorithm_order AS '顺序',
    algorithm_name AS '算法',
    ql_expression AS '表达式',
    output_param AS '输出'
FROM step_algorithm sa
JOIN model_step ms ON sa.step_id = ms.id
WHERE ms.model_id = 8 AND ms.step_code = 'VECTOR_NORMALIZATION'
ORDER BY algorithm_order;

SELECT '' AS info;
SELECT '========================================' AS info;
SELECT '修复完成！请重新执行评估测试' AS info;
SELECT '========================================' AS info;
