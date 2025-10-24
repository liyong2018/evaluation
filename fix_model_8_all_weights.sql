-- 修复模型8的所有@WEIGHT标记
-- 包括：二级指标定权步骤 + 能力值计算与分级步骤

SELECT '========================================' AS info;
SELECT '模型8 @WEIGHT标记完整修复脚本' AS info;
SELECT '========================================' AS info;

-- ========================================
-- 第一部分：二级指标定权步骤
-- ========================================

SELECT '' AS info;
SELECT '==== 修复前：二级指标定权步骤 ====' AS info;
SELECT 
    sa.algorithm_order,
    sa.algorithm_name,
    LEFT(sa.ql_expression, 100) AS ql_expression
FROM step_algorithm sa
JOIN model_step ms ON sa.step_id = ms.id
WHERE ms.model_id = 8 AND ms.step_code = 'SECONDARY_WEIGHTING'
ORDER BY sa.algorithm_order;

-- 算法2: Support Weight - 物资储备和医疗保障的加权
UPDATE step_algorithm sa
JOIN model_step ms ON sa.step_id = ms.id
SET sa.ql_expression = '(MATERIAL_RESERVE_NORM != null ? MATERIAL_RESERVE_NORM : 0.0) * 0.52 + (MEDICAL_SUPPORT_NORM != null ? MEDICAL_SUPPORT_NORM : 0.0) * 0.48'
WHERE ms.model_id = 8
  AND ms.step_code = 'SECONDARY_WEIGHTING'
  AND sa.algorithm_order = 2;

-- 算法3: Capability Weight - 自救互救和公众疏散的加权
UPDATE step_algorithm sa
JOIN model_step ms ON sa.step_id = ms.id
SET sa.ql_expression = '(SELF_MUTUAL_AID_NORM != null ? SELF_MUTUAL_AID_NORM : 0.0) * 0.33 + (PUBLIC_EVACUATION_NORM != null ? PUBLIC_EVACUATION_NORM : 0.0) * 0.34'
WHERE ms.model_id = 8
  AND ms.step_code = 'SECONDARY_WEIGHTING'
  AND sa.algorithm_order = 3;

-- 算法5: Comprehensive Weight - 综合权重计算
UPDATE step_algorithm sa
JOIN model_step ms ON sa.step_id = ms.id
SET sa.ql_expression = '(MANAGEMENT_WEIGHT != null ? MANAGEMENT_WEIGHT : 0.0) * 0.32 + (SUPPORT_WEIGHT != null ? SUPPORT_WEIGHT : 0.0) * 0.31 + (CAPABILITY_WEIGHT != null ? CAPABILITY_WEIGHT : 0.0) * 0.37 + (FACILITY_WEIGHT != null ? FACILITY_WEIGHT : 0.0) * 0.37'
WHERE ms.model_id = 8
  AND ms.step_code = 'SECONDARY_WEIGHTING'
  AND sa.algorithm_order = 5;

SELECT '' AS info;
SELECT '==== 修复后：二级指标定权步骤 ====' AS info;
SELECT 
    sa.algorithm_order,
    sa.algorithm_name,
    LEFT(sa.ql_expression, 100) AS ql_expression
FROM step_algorithm sa
JOIN model_step ms ON sa.step_id = ms.id
WHERE ms.model_id = 8 AND ms.step_code = 'SECONDARY_WEIGHTING'
ORDER BY sa.algorithm_order;

-- ========================================
-- 第二部分：能力值计算与分级步骤
-- ========================================

SELECT '' AS info;
SELECT '==== 修复前：能力值计算与分级步骤 ====' AS info;
SELECT 
    sa.algorithm_order,
    sa.algorithm_name,
    sa.ql_expression
FROM step_algorithm sa
JOIN model_step ms ON sa.step_id = ms.id
WHERE ms.model_id = 8 AND ms.step_code = 'CAPABILITY_GRADE'
ORDER BY sa.algorithm_order;

-- 算法1: Capability Score - 将0-1的得分转换为0-100的百分制
UPDATE step_algorithm sa
JOIN model_step ms ON sa.step_id = ms.id
SET sa.ql_expression = 'RELATIVE_CLOSENESS != null ? RELATIVE_CLOSENESS * 100.0 : 50.0'
WHERE ms.model_id = 8
  AND ms.step_code = 'CAPABILITY_GRADE'
  AND sa.algorithm_order = 1;

SELECT '' AS info;
SELECT '==== 修复后：能力值计算与分级步骤 ====' AS info;
SELECT 
    sa.algorithm_order,
    sa.algorithm_name,
    sa.ql_expression
FROM step_algorithm sa
JOIN model_step ms ON sa.step_id = ms.id
WHERE ms.model_id = 8 AND ms.step_code = 'CAPABILITY_GRADE'
ORDER BY sa.algorithm_order;

-- ========================================
-- 验证：检查是否还有@WEIGHT标记
-- ========================================

SELECT '' AS info;
SELECT '==== 验证：检查模型8是否还有@WEIGHT标记 ====' AS info;
SELECT 
    ms.step_name,
    sa.algorithm_name,
    sa.ql_expression
FROM step_algorithm sa
JOIN model_step ms ON sa.step_id = ms.id
WHERE ms.model_id = 8
  AND sa.ql_expression LIKE '%@WEIGHT%'
ORDER BY ms.step_order, sa.algorithm_order;

SELECT '' AS info;
SELECT '如果上面没有结果，说明所有@WEIGHT标记都已修复！' AS info;
SELECT '========================================' AS info;
SELECT '修复完成！' AS info;
SELECT '========================================' AS info;
