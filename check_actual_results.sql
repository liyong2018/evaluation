-- 检查实际的评估结果数据

-- 1. 检查加权后的数据是否存在
SELECT '==== 检查加权后的数据（_WEIGHT字段） ====' AS info;
SELECT 
    region_name,
    PLAN_CONSTRUCTION_WEIGHT,
    HAZARD_INSPECTION_WEIGHT,
    RISK_ASSESSMENT_WEIGHT,
    FINANCIAL_INPUT_WEIGHT,
    MATERIAL_RESERVE_WEIGHT,
    MEDICAL_SUPPORT_WEIGHT
FROM survey_data
WHERE model_id = 4
LIMIT 5;

-- 2. 检查TOPSIS正负理想解数据
SELECT '==== 检查TOPSIS正负理想解数据 ====' AS info;
SELECT 
    region_name,
    MANAGEMENT_POSITIVE_IDEAL,
    MANAGEMENT_NEGATIVE_IDEAL,
    SUPPORT_POSITIVE_IDEAL,
    SUPPORT_NEGATIVE_IDEAL,
    CAPABILITY_POSITIVE_IDEAL,
    CAPABILITY_NEGATIVE_IDEAL
FROM survey_data
WHERE model_id = 4
LIMIT 5;

-- 3. 检查TOPSIS得分数据
SELECT '==== 检查TOPSIS得分数据 ====' AS info;
SELECT 
    region_name,
    MANAGEMENT_SCORE,
    SUPPORT_SCORE,
    CAPABILITY_SCORE
FROM survey_data
WHERE model_id = 4
LIMIT 5;

-- 4. 检查survey_data表中是否有这些字段
SELECT '==== 检查survey_data表结构（TOPSIS相关字段） ====' AS info;
SELECT COLUMN_NAME, DATA_TYPE, IS_NULLABLE
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_SCHEMA = 'evaluate_db'
  AND TABLE_NAME = 'survey_data'
  AND (COLUMN_NAME LIKE '%POSITIVE_IDEAL%' 
    OR COLUMN_NAME LIKE '%NEGATIVE_IDEAL%'
    OR COLUMN_NAME LIKE '%_SCORE'
    OR COLUMN_NAME LIKE '%_WEIGHT')
ORDER BY COLUMN_NAME;

-- 5. 检查当前的TOPSIS算法配置
SELECT '==== 当前TOPSIS距离计算配置 ====' AS info;
SELECT 
    sa.algorithm_order,
    sa.algorithm_name,
    sa.output_param,
    sa.ql_expression
FROM step_algorithm sa
JOIN model_step ms ON sa.step_id = ms.id
WHERE ms.model_id = 4 AND ms.step_code = 'TOPSIS_DISTANCE'
ORDER BY sa.algorithm_order;

-- 6. 检查能力值计算配置
SELECT '==== 当前能力值计算配置 ====' AS info;
SELECT 
    sa.algorithm_order,
    sa.algorithm_name,
    sa.output_param,
    sa.ql_expression
FROM step_algorithm sa
JOIN model_step ms ON sa.step_id = ms.id
WHERE ms.model_id = 4 AND ms.step_code = 'CAPABILITY_GRADE'
ORDER BY sa.algorithm_order;
