-- =====================================================
-- 综合减灾能力评估模型配置脚本
-- 说明：该模型从乡镇评估模型和社区-乡镇评估模型的结果中获取数据
-- =====================================================

-- 1. 创建综合评估模型
INSERT INTO evaluation_model (model_name, model_code, description, version, status, is_default, create_time)
VALUES (
    '综合减灾能力评估模型',
    'COMPREHENSIVE_MODEL',
    '融合乡镇评估模型和社区-乡镇评估模型的结果，进行综合减灾能力评估',
    '1.0',
    1,
    0,
    NOW()
);

SET @comprehensive_model_id = LAST_INSERT_ID();

-- 2. 创建模型步骤
-- 步骤1：数据融合（从evaluation_result表获取数据）
INSERT INTO model_step (model_id, step_name, step_code, step_order, step_type, description, status, create_time)
VALUES (
    @comprehensive_model_id,
    '数据融合',
    'DATA_FUSION',
    1,
    'DATA_PREPARATION',
    '从evaluation_result表获取乡镇评估模型和社区-乡镇评估模型的结果，融合为6个一级指标',
    1,
    NOW()
);

SET @step1_id = LAST_INSERT_ID();

-- 步骤2：属性向量归一化
INSERT INTO model_step (model_id, step_name, step_code, step_order, step_type, description, status, create_time)
VALUES (
    @comprehensive_model_id,
    '属性向量归一化',
    'VECTOR_NORMALIZATION',
    2,
    'NORMALIZATION',
    '对6个一级指标进行向量归一化处理',
    1,
    NOW()
);

SET @step2_id = LAST_INSERT_ID();

-- 步骤3：二级定权
INSERT INTO model_step (model_id, step_name, step_code, step_order, step_type, description, status, create_time)
VALUES (
    @comprehensive_model_id,
    '二级定权',
    'SECONDARY_WEIGHTING',
    3,
    'WEIGHTING',
    '对归一化后的指标进行一级和二级权重计算',
    1,
    NOW()
);

SET @step3_id = LAST_INSERT_ID();

-- 步骤4：TOPSIS优劣解距离
INSERT INTO model_step (model_id, step_name, step_code, step_order, step_type, description, status, create_time)
VALUES (
    @comprehensive_model_id,
    'TOPSIS优劣解距离',
    'TOPSIS_DISTANCE',
    4,
    'TOPSIS',
    '计算各指标的优解和劣解距离',
    1,
    NOW()
);

SET @step4_id = LAST_INSERT_ID();

-- 步骤5：能力评估
INSERT INTO model_step (model_id, step_name, step_code, step_order, step_type, description, status, create_time)
VALUES (
    @comprehensive_model_id,
    '能力评估',
    'CAPABILITY_GRADE',
    5,
    'GRADING',
    '计算综合减灾能力值并进行分级',
    1,
    NOW()
);

SET @step5_id = LAST_INSERT_ID();

-- =====================================================
-- 步骤1：数据融合算法配置
-- 这一步需要特殊处理：从evaluation_result表读取数据
-- =====================================================

-- 乡镇评估模型结果提取
INSERT INTO step_algorithm (step_id, algorithm_name, algorithm_code, algorithm_order, ql_expression, input_params, output_param, description, status, create_time)
VALUES
(@step1_id, '提取乡镇灾害管理能力', 'GET_TOWNSHIP_MGMT', 1, '@LOAD_EVAL_RESULT:modelId=3,field=management_capability_score', '', 'TOWNSHIP_MGMT_CAPABILITY', '从乡镇评估模型结果中获取灾害管理能力分值', 1, NOW()),
(@step1_id, '提取乡镇灾害备灾能力', 'GET_TOWNSHIP_PREP', 2, '@LOAD_EVAL_RESULT:modelId=3,field=support_capability_score', '', 'TOWNSHIP_PREP_CAPABILITY', '从乡镇评估模型结果中获取灾害备灾能力分值', 1, NOW()),
(@step1_id, '提取乡镇自救转移能力', 'GET_TOWNSHIP_RESCUE', 3, '@LOAD_EVAL_RESULT:modelId=3,field=self_rescue_capability_score', '', 'TOWNSHIP_RESCUE_CAPABILITY', '从乡镇评估模型结果中获取自救转移能力分值', 1, NOW()),
(@step1_id, '提取社区-乡镇灾害管理能力', 'GET_COMMUNITY_MGMT', 4, '@LOAD_EVAL_RESULT:modelId=8,field=management_capability_score', '', 'COMMUNITY_MGMT_CAPABILITY', '从社区-乡镇评估模型结果中获取灾害管理能力分值', 1, NOW()),
(@step1_id, '提取社区-乡镇灾害备灾能力', 'GET_COMMUNITY_PREP', 5, '@LOAD_EVAL_RESULT:modelId=8,field=support_capability_score', '', 'COMMUNITY_PREP_CAPABILITY', '从社区-乡镇评估模型结果中获取灾害备灾能力分值', 1, NOW()),
(@step1_id, '提取社区-乡镇自救转移能力', 'GET_COMMUNITY_RESCUE', 6, '@LOAD_EVAL_RESULT:modelId=8,field=self_rescue_capability_score', '', 'COMMUNITY_RESCUE_CAPABILITY', '从社区-乡镇评估模型结果中获取自救转移能力分值', 1, NOW());

-- =====================================================
-- 步骤2：属性向量归一化算法配置
-- =====================================================

INSERT INTO step_algorithm (step_id, algorithm_name, algorithm_code, algorithm_order, ql_expression, input_params, output_param, description, status, create_time)
VALUES
(@step2_id, '乡镇灾害管理能力归一化', 'NORM_TOWNSHIP_MGMT', 1, '@NORMALIZE:TOWNSHIP_MGMT_CAPABILITY', 'TOWNSHIP_MGMT_CAPABILITY', 'TOWNSHIP_MGMT_NORM', '向量归一化：本值/SQRT(SUMSQ(全部值))', 1, NOW()),
(@step2_id, '乡镇灾害备灾能力归一化', 'NORM_TOWNSHIP_PREP', 2, '@NORMALIZE:TOWNSHIP_PREP_CAPABILITY', 'TOWNSHIP_PREP_CAPABILITY', 'TOWNSHIP_PREP_NORM', '向量归一化：本值/SQRT(SUMSQ(全部值))', 1, NOW()),
(@step2_id, '乡镇自救转移能力归一化', 'NORM_TOWNSHIP_RESCUE', 3, '@NORMALIZE:TOWNSHIP_RESCUE_CAPABILITY', 'TOWNSHIP_RESCUE_CAPABILITY', 'TOWNSHIP_RESCUE_NORM', '向量归一化：本值/SQRT(SUMSQ(全部值))', 1, NOW()),
(@step2_id, '社区-乡镇灾害管理能力归一化', 'NORM_COMMUNITY_MGMT', 4, '@NORMALIZE:COMMUNITY_MGMT_CAPABILITY', 'COMMUNITY_MGMT_CAPABILITY', 'COMMUNITY_MGMT_NORM', '向量归一化：本值/SQRT(SUMSQ(全部值))', 1, NOW()),
(@step2_id, '社区-乡镇灾害备灾能力归一化', 'NORM_COMMUNITY_PREP', 5, '@NORMALIZE:COMMUNITY_PREP_CAPABILITY', 'COMMUNITY_PREP_CAPABILITY', 'COMMUNITY_PREP_NORM', '向量归一化：本值/SQRT(SUMSQ(全部值))', 1, NOW()),
(@step2_id, '社区-乡镇自救转移能力归一化', 'NORM_COMMUNITY_RESCUE', 6, '@NORMALIZE:COMMUNITY_RESCUE_CAPABILITY', 'COMMUNITY_RESCUE_CAPABILITY', 'COMMUNITY_RESCUE_NORM', '向量归一化：本值/SQRT(SUMSQ(全部值))', 1, NOW());

-- =====================================================
-- 步骤3：二级定权算法配置
-- 权重配置：
-- 一级权重：乡镇0.53，社区0.47
-- 二级权重：灾害管理0.33/0.32，灾害备灾0.32/0.31，自救转移0.35/0.37
-- =====================================================

INSERT INTO step_algorithm (step_id, algorithm_name, algorithm_code, algorithm_order, ql_expression, input_params, output_param, description, status, create_time)
VALUES
(@step3_id, '乡镇灾害管理能力定权', 'WEIGHT_TOWNSHIP_MGMT', 1, 'TOWNSHIP_MGMT_NORM * 0.53 * 0.33', 'TOWNSHIP_MGMT_NORM', 'TOWNSHIP_MGMT_WEIGHTED', '归一化值 × 一级权重(0.53) × 二级权重(0.33)', 1, NOW()),
(@step3_id, '乡镇灾害备灾能力定权', 'WEIGHT_TOWNSHIP_PREP', 2, 'TOWNSHIP_PREP_NORM * 0.53 * 0.32', 'TOWNSHIP_PREP_NORM', 'TOWNSHIP_PREP_WEIGHTED', '归一化值 × 一级权重(0.53) × 二级权重(0.32)', 1, NOW()),
(@step3_id, '乡镇自救转移能力定权', 'WEIGHT_TOWNSHIP_RESCUE', 3, 'TOWNSHIP_RESCUE_NORM * 0.53 * 0.35', 'TOWNSHIP_RESCUE_NORM', 'TOWNSHIP_RESCUE_WEIGHTED', '归一化值 × 一级权重(0.53) × 二级权重(0.35)', 1, NOW()),
(@step3_id, '社区-乡镇灾害管理能力定权', 'WEIGHT_COMMUNITY_MGMT', 4, 'COMMUNITY_MGMT_NORM * 0.47 * 0.32', 'COMMUNITY_MGMT_NORM', 'COMMUNITY_MGMT_WEIGHTED', '归一化值 × 一级权重(0.47) × 二级权重(0.32)', 1, NOW()),
(@step3_id, '社区-乡镇灾害备灾能力定权', 'WEIGHT_COMMUNITY_PREP', 5, 'COMMUNITY_PREP_NORM * 0.47 * 0.31', 'COMMUNITY_PREP_NORM', 'COMMUNITY_PREP_WEIGHTED', '归一化值 × 一级权重(0.47) × 二级权重(0.31)', 1, NOW()),
(@step3_id, '社区-乡镇自救转移能力定权', 'WEIGHT_COMMUNITY_RESCUE', 6, 'COMMUNITY_RESCUE_NORM * 0.47 * 0.37', 'COMMUNITY_RESCUE_NORM', 'COMMUNITY_RESCUE_WEIGHTED', '归一化值 × 一级权重(0.47) × 二级权重(0.37)', 1, NOW());

-- =====================================================
-- 步骤4：TOPSIS优劣解距离计算
-- =====================================================

-- 灾害管理能力（合并乡镇和社区）
INSERT INTO step_algorithm (step_id, algorithm_name, algorithm_code, algorithm_order, ql_expression, input_params, output_param, description, status, create_time)
VALUES
(@step4_id, '灾害管理能力优解距离', 'MGMT_POSITIVE', 1, '@TOPSIS_POSITIVE:TOWNSHIP_MGMT_WEIGHTED,COMMUNITY_MGMT_WEIGHTED', 'TOWNSHIP_MGMT_WEIGHTED,COMMUNITY_MGMT_WEIGHTED', 'MGMT_POSITIVE_DISTANCE', '计算灾害管理能力的优解距离', 1, NOW()),
(@step4_id, '灾害管理能力劣解距离', 'MGMT_NEGATIVE', 2, '@TOPSIS_NEGATIVE:TOWNSHIP_MGMT_WEIGHTED,COMMUNITY_MGMT_WEIGHTED', 'TOWNSHIP_MGMT_WEIGHTED,COMMUNITY_MGMT_WEIGHTED', 'MGMT_NEGATIVE_DISTANCE', '计算灾害管理能力的劣解距离', 1, NOW()),
(@step4_id, '灾害管理能力得分', 'MGMT_SCORE', 3, 'MGMT_NEGATIVE_DISTANCE / (MGMT_NEGATIVE_DISTANCE + MGMT_POSITIVE_DISTANCE)', 'MGMT_NEGATIVE_DISTANCE,MGMT_POSITIVE_DISTANCE', 'managementScore', '劣解距离/(劣解距离+优解距离)', 1, NOW());

-- 灾害备灾能力（合并乡镇和社区）
INSERT INTO step_algorithm (step_id, algorithm_name, algorithm_code, algorithm_order, ql_expression, input_params, output_param, description, status, create_time)
VALUES
(@step4_id, '灾害备灾能力优解距离', 'PREP_POSITIVE', 4, '@TOPSIS_POSITIVE:TOWNSHIP_PREP_WEIGHTED,COMMUNITY_PREP_WEIGHTED', 'TOWNSHIP_PREP_WEIGHTED,COMMUNITY_PREP_WEIGHTED', 'PREP_POSITIVE_DISTANCE', '计算灾害备灾能力的优解距离', 1, NOW()),
(@step4_id, '灾害备灾能力劣解距离', 'PREP_NEGATIVE', 5, '@TOPSIS_NEGATIVE:TOWNSHIP_PREP_WEIGHTED,COMMUNITY_PREP_WEIGHTED', 'TOWNSHIP_PREP_WEIGHTED,COMMUNITY_PREP_WEIGHTED', 'PREP_NEGATIVE_DISTANCE', '计算灾害备灾能力的劣解距离', 1, NOW()),
(@step4_id, '灾害备灾能力得分', 'PREP_SCORE', 6, 'PREP_NEGATIVE_DISTANCE / (PREP_NEGATIVE_DISTANCE + PREP_POSITIVE_DISTANCE)', 'PREP_NEGATIVE_DISTANCE,PREP_POSITIVE_DISTANCE', 'preparednessScore', '劣解距离/(劣解距离+优解距离)', 1, NOW());

-- 自救转移能力（合并乡镇和社区）
INSERT INTO step_algorithm (step_id, algorithm_name, algorithm_code, algorithm_order, ql_expression, input_params, output_param, description, status, create_time)
VALUES
(@step4_id, '自救转移能力优解距离', 'RESCUE_POSITIVE', 7, '@TOPSIS_POSITIVE:TOWNSHIP_RESCUE_WEIGHTED,COMMUNITY_RESCUE_WEIGHTED', 'TOWNSHIP_RESCUE_WEIGHTED,COMMUNITY_RESCUE_WEIGHTED', 'RESCUE_POSITIVE_DISTANCE', '计算自救转移能力的优解距离', 1, NOW()),
(@step4_id, '自救转移能力劣解距离', 'RESCUE_NEGATIVE', 8, '@TOPSIS_NEGATIVE:TOWNSHIP_RESCUE_WEIGHTED,COMMUNITY_RESCUE_WEIGHTED', 'TOWNSHIP_RESCUE_WEIGHTED,COMMUNITY_RESCUE_WEIGHTED', 'RESCUE_NEGATIVE_DISTANCE', '计算自救转移能力的劣解距离', 1, NOW()),
(@step4_id, '自救转移能力得分', 'RESCUE_SCORE', 9, 'RESCUE_NEGATIVE_DISTANCE / (RESCUE_NEGATIVE_DISTANCE + RESCUE_POSITIVE_DISTANCE)', 'RESCUE_NEGATIVE_DISTANCE,RESCUE_POSITIVE_DISTANCE', 'rescueScore', '劣解距离/(劣解距离+优解距离)', 1, NOW());

-- 综合减灾能力（所有6个指标）
INSERT INTO step_algorithm (step_id, algorithm_name, algorithm_code, algorithm_order, ql_expression, input_params, output_param, description, status, create_time)
VALUES
(@step4_id, '综合减灾能力优解距离', 'COMPREHENSIVE_POSITIVE', 10, '@TOPSIS_POSITIVE:TOWNSHIP_MGMT_WEIGHTED,TOWNSHIP_PREP_WEIGHTED,TOWNSHIP_RESCUE_WEIGHTED,COMMUNITY_MGMT_WEIGHTED,COMMUNITY_PREP_WEIGHTED,COMMUNITY_RESCUE_WEIGHTED', 'TOWNSHIP_MGMT_WEIGHTED,TOWNSHIP_PREP_WEIGHTED,TOWNSHIP_RESCUE_WEIGHTED,COMMUNITY_MGMT_WEIGHTED,COMMUNITY_PREP_WEIGHTED,COMMUNITY_RESCUE_WEIGHTED', 'COMPREHENSIVE_POSITIVE_DISTANCE', '计算综合减灾能力的优解距离（6个指标）', 1, NOW()),
(@step4_id, '综合减灾能力劣解距离', 'COMPREHENSIVE_NEGATIVE', 11, '@TOPSIS_NEGATIVE:TOWNSHIP_MGMT_WEIGHTED,TOWNSHIP_PREP_WEIGHTED,TOWNSHIP_RESCUE_WEIGHTED,COMMUNITY_MGMT_WEIGHTED,COMMUNITY_PREP_WEIGHTED,COMMUNITY_RESCUE_WEIGHTED', 'TOWNSHIP_MGMT_WEIGHTED,TOWNSHIP_PREP_WEIGHTED,TOWNSHIP_RESCUE_WEIGHTED,COMMUNITY_MGMT_WEIGHTED,COMMUNITY_PREP_WEIGHTED,COMMUNITY_RESCUE_WEIGHTED', 'COMPREHENSIVE_NEGATIVE_DISTANCE', '计算综合减灾能力的劣解距离（6个指标）', 1, NOW()),
(@step4_id, '综合减灾能力得分', 'COMPREHENSIVE_SCORE', 12, 'COMPREHENSIVE_NEGATIVE_DISTANCE / (COMPREHENSIVE_NEGATIVE_DISTANCE + COMPREHENSIVE_POSITIVE_DISTANCE)', 'COMPREHENSIVE_NEGATIVE_DISTANCE,COMPREHENSIVE_POSITIVE_DISTANCE', 'comprehensiveScore', '劣解距离/(劣解距离+优解距离)', 1, NOW());

-- =====================================================
-- 步骤5：能力评估分级
-- =====================================================

INSERT INTO step_algorithm (step_id, algorithm_name, algorithm_code, algorithm_order, ql_expression, input_params, output_param, description, status, create_time)
VALUES
(@step5_id, '灾害管理能力等级', 'MGMT_GRADE', 1, '@GRADE:managementScore', 'managementScore', 'managementGrade', '根据均值和标准差进行五级分类', 1, NOW()),
(@step5_id, '灾害备灾能力等级', 'PREP_GRADE', 2, '@GRADE:preparednessScore', 'preparednessScore', 'preparednessGrade', '根据均值和标准差进行五级分类', 1, NOW()),
(@step5_id, '自救转移能力等级', 'RESCUE_GRADE', 3, '@GRADE:rescueScore', 'rescueScore', 'rescueGrade', '根据均值和标准差进行五级分类', 1, NOW()),
(@step5_id, '综合减灾能力等级', 'COMPREHENSIVE_GRADE', 4, '@GRADE:comprehensiveScore', 'comprehensiveScore', 'comprehensiveGrade', '根据均值和标准差进行五级分类', 1, NOW());

-- =====================================================
-- 查询验证
-- =====================================================

SELECT
    m.id AS model_id,
    m.model_name,
    s.step_order,
    s.step_name,
    s.step_code,
    COUNT(a.id) AS algorithm_count
FROM evaluation_model m
LEFT JOIN model_step s ON m.id = s.model_id
LEFT JOIN step_algorithm a ON s.id = a.step_id
WHERE m.model_code = 'COMPREHENSIVE_MODEL'
GROUP BY m.id, m.model_name, s.step_order, s.step_name, s.step_code
ORDER BY s.step_order;
