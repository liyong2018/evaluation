-- 检查评估结果表

-- 1. 查看primary_indicator_result表结构
SELECT '==== primary_indicator_result表结构 ====' AS info;
DESC primary_indicator_result;

-- 2. 查看secondary_indicator_result表结构
SELECT '==== secondary_indicator_result表结构 ====' AS info;
DESC secondary_indicator_result;

-- 3. 查看社区模型的一级指标结果
SELECT '==== 社区模型的一级指标结果 ====' AS info;
SELECT 
    region_code,
    indicator_name,
    indicator_value,
    indicator_grade
FROM primary_indicator_result
WHERE model_id = 4
LIMIT 10;

-- 4. 查看社区模型的二级指标结果
SELECT '==== 社区模型的二级指标结果 ====' AS info;
SELECT 
    region_code,
    indicator_name,
    indicator_value
FROM secondary_indicator_result
WHERE model_id = 4
LIMIT 10;
