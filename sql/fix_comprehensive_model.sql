-- =====================================================
-- 综合减灾能力评估模型修复脚本
-- 说明：更新现有的综合减灾模型配置，添加@LOAD_EVAL_RESULT算法
-- =====================================================

-- 获取综合减灾模型ID
SET @model_id = (SELECT id FROM evaluation_model
                 WHERE model_code = 'COMPREHENSIVE_MODEL'
                    OR model_name LIKE '%综合减灾%'
                 LIMIT 1);

-- 检查模型是否存在
SELECT CONCAT('找到模型ID: ', COALESCE(@model_id, 'NULL')) AS status;

-- 如果模型不存在，创建它
INSERT INTO evaluation_model (model_name, model_code, description, version, status, is_default, create_time)
SELECT
    '综合减灾能力评估模型',
    'COMPREHENSIVE_MODEL',
    '融合乡镇评估模型和社区-乡镇评估模型的结果，进行综合减灾能力评估',
    '1.0',
    1,
    0,
    NOW()
WHERE NOT EXISTS (
    SELECT 1 FROM evaluation_model
    WHERE model_code = 'COMPREHENSIVE_MODEL'
       OR model_name LIKE '%综合减灾%'
);

-- 重新获取模型ID（如果刚创建的话）
SET @model_id = (SELECT id FROM evaluation_model
                 WHERE model_code = 'COMPREHENSIVE_MODEL'
                    OR model_name LIKE '%综合减灾%'
                 LIMIT 1);

-- 获取第一步的step_id
SET @step1_id = (SELECT id FROM model_step
                 WHERE model_id = @model_id
                   AND step_order = 1
                 LIMIT 1);

SELECT CONCAT('第一步骤ID: ', COALESCE(@step1_id, 'NULL')) AS status;

-- 如果第一步不存在，创建它
INSERT INTO model_step (model_id, step_name, step_code, step_order, step_type, description, status, create_time)
SELECT
    @model_id,
    '数据融合',
    'DATA_FUSION',
    1,
    'DATA_PREPARATION',
    '从evaluation_result表获取乡镇评估模型和社区-乡镇评估模型的结果，融合为6个一级指标',
    1,
    NOW()
WHERE @step1_id IS NULL;

-- 重新获取第一步ID
SET @step1_id = (SELECT id FROM model_step
                 WHERE model_id = @model_id
                   AND step_order = 1
                 LIMIT 1);

-- 更新第一步的名称和代码（如果存在但名称不对）
UPDATE model_step
SET step_name = '数据融合',
    step_code = 'DATA_FUSION',
    step_type = 'DATA_PREPARATION',
    description = '从evaluation_result表获取乡镇评估模型和社区-乡镇评估模型的结果，融合为6个一级指标'
WHERE id = @step1_id;

-- 删除第一步的旧算法配置
DELETE FROM step_algorithm WHERE step_id = @step1_id;

-- 插入新的算法配置（使用@LOAD_EVAL_RESULT）
INSERT INTO step_algorithm (step_id, algorithm_name, algorithm_code, algorithm_order, ql_expression, input_params, output_param, description, status, create_time)
VALUES
(@step1_id, '提取乡镇灾害管理能力', 'GET_TOWNSHIP_MGMT', 1, '@LOAD_EVAL_RESULT:modelId=3,field=management_capability_score', '', 'TOWNSHIP_MGMT_CAPABILITY', '从乡镇评估模型结果中获取灾害管理能力分值', 1, NOW()),
(@step1_id, '提取乡镇灾害备灾能力', 'GET_TOWNSHIP_PREP', 2, '@LOAD_EVAL_RESULT:modelId=3,field=support_capability_score', '', 'TOWNSHIP_PREP_CAPABILITY', '从乡镇评估模型结果中获取灾害备灾能力分值', 1, NOW()),
(@step1_id, '提取乡镇自救转移能力', 'GET_TOWNSHIP_RESCUE', 3, '@LOAD_EVAL_RESULT:modelId=3,field=self_rescue_capability_score', '', 'TOWNSHIP_RESCUE_CAPABILITY', '从乡镇评估模型结果中获取自救转移能力分值', 1, NOW()),
(@step1_id, '提取社区-乡镇灾害管理能力', 'GET_COMMUNITY_MGMT', 4, '@LOAD_EVAL_RESULT:modelId=8,field=management_capability_score', '', 'COMMUNITY_MGMT_CAPABILITY', '从社区-乡镇评估模型结果中获取灾害管理能力分值', 1, NOW()),
(@step1_id, '提取社区-乡镇灾害备灾能力', 'GET_COMMUNITY_PREP', 5, '@LOAD_EVAL_RESULT:modelId=8,field=support_capability_score', '', 'COMMUNITY_PREP_CAPABILITY', '从社区-乡镇评估模型结果中获取灾害备灾能力分值', 1, NOW()),
(@step1_id, '提取社区-乡镇自救转移能力', 'GET_COMMUNITY_RESCUE', 6, '@LOAD_EVAL_RESULT:modelId=8,field=self_rescue_capability_score', '', 'COMMUNITY_RESCUE_CAPABILITY', '从社区-乡镇评估模型结果中获取自救转移能力分值', 1, NOW());

-- 验证配置
SELECT '=== 验证第一步算法配置 ===' AS info;

SELECT
    sa.algorithm_order,
    sa.algorithm_name,
    sa.algorithm_code,
    sa.ql_expression,
    sa.output_param
FROM step_algorithm sa
WHERE sa.step_id = @step1_id
ORDER BY sa.algorithm_order;

SELECT CONCAT('第一步共配置了 ', COUNT(*), ' 个算法') AS summary
FROM step_algorithm
WHERE step_id = @step1_id;
