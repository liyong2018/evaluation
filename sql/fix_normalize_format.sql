-- =====================================================
-- 修复综合减灾模型第二步（归一化）的算法配置
-- 问题：@NORMALIZE(xxx) 应该改为 @NORMALIZE:xxx
-- =====================================================

-- 获取综合减灾模型ID
SET @model_id = (SELECT id FROM evaluation_model
                 WHERE model_code = 'COMPREHENSIVE_MODEL'
                    OR model_name LIKE '%综合减灾%'
                 LIMIT 1);

-- 获取第二步的step_id
SET @step2_id = (SELECT id FROM model_step
                 WHERE model_id = @model_id
                   AND step_order = 2
                 LIMIT 1);

SELECT CONCAT('综合减灾模型ID: ', COALESCE(@model_id, 'NULL')) AS status;
SELECT CONCAT('第二步ID: ', COALESCE(@step2_id, 'NULL')) AS status;

-- 显示当前的错误配置
SELECT '=== 当前的归一化算法配置（错误） ===' AS info;
SELECT
    algorithm_order AS 顺序,
    algorithm_name AS 算法名称,
    ql_expression AS 当前表达式_错误
FROM step_algorithm
WHERE step_id = @step2_id
ORDER BY algorithm_order;

-- 更新第二步的归一化算法（修正格式）
UPDATE step_algorithm
SET ql_expression = '@NORMALIZE:TOWNSHIP_MGMT_CAPABILITY'
WHERE step_id = @step2_id
  AND algorithm_code = 'NORM_TOWNSHIP_MGMT';

UPDATE step_algorithm
SET ql_expression = '@NORMALIZE:TOWNSHIP_PREP_CAPABILITY'
WHERE step_id = @step2_id
  AND algorithm_code = 'NORM_TOWNSHIP_PREP';

UPDATE step_algorithm
SET ql_expression = '@NORMALIZE:TOWNSHIP_RESCUE_CAPABILITY'
WHERE step_id = @step2_id
  AND algorithm_code = 'NORM_TOWNSHIP_RESCUE';

UPDATE step_algorithm
SET ql_expression = '@NORMALIZE:COMMUNITY_MGMT_CAPABILITY'
WHERE step_id = @step2_id
  AND algorithm_code = 'NORM_COMMUNITY_MGMT';

UPDATE step_algorithm
SET ql_expression = '@NORMALIZE:COMMUNITY_PREP_CAPABILITY'
WHERE step_id = @step2_id
  AND algorithm_code = 'NORM_COMMUNITY_PREP';

UPDATE step_algorithm
SET ql_expression = '@NORMALIZE:COMMUNITY_RESCUE_CAPABILITY'
WHERE step_id = @step2_id
  AND algorithm_code = 'NORM_COMMUNITY_RESCUE';

-- 验证修复后的配置
SELECT '=== 修复后的归一化算法配置（正确） ===' AS info;
SELECT
    algorithm_order AS 顺序,
    algorithm_name AS 算法名称,
    ql_expression AS 修复后表达式_正确
FROM step_algorithm
WHERE step_id = @step2_id
ORDER BY algorithm_order;

SELECT '✅ 归一化算法格式已修复！现在应该使用冒号(:)而不是括号()' AS result;
