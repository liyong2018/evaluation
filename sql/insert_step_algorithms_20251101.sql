-- =====================================================
-- 插入步骤3/4/5的定权、TOPSIS计算、能力值分级公式
-- 说明：
--   1. 该脚本使用插入语句（INSERT ... SELECT ... WHERE NOT EXISTS）
--      如果目标记录已经存在则跳过，避免重复。
--   2. 执行前建议备份 step_algorithm 表。
--   3. 请在目标数据库连接中执行本脚本。
-- =====================================================

-- 绑定综合模型及各步骤 ID
SET @model_id   := (SELECT id FROM evaluation_model WHERE model_code = 'COMPREHENSIVE_MODEL' LIMIT 1);
SET @step3_id   := (SELECT id FROM model_step WHERE model_id = @model_id AND step_code = 'SECONDARY_WEIGHTING'      LIMIT 1);
SET @step4_id   := (SELECT id FROM model_step WHERE model_id = @model_id AND step_code = 'TOPSIS_DISTANCE'         LIMIT 1);
SET @step5_id   := (SELECT id FROM model_step WHERE model_id = @model_id AND step_code = 'CAPABILITY_GRADE'        LIMIT 1);

-- =====================================================
-- 步骤3：二级定权（6条）
-- =====================================================
INSERT INTO step_algorithm (step_id, algorithm_name, algorithm_code, algorithm_order,
                            ql_expression, input_params, output_param, description, status, create_time)
SELECT @step3_id, '乡镇灾害管理能力定权', 'WEIGHT_TOWNSHIP_MGMT', 1,
       'TOWNSHIP_MGMT_NORM * 0.53 * 0.33',
       'TOWNSHIP_MGMT_NORM', 'TOWNSHIP_MGMT_WEIGHTED',
       '归一化值 × 一级权重(0.53) × 二级权重(0.33)', 1, NOW()
WHERE NOT EXISTS (SELECT 1 FROM step_algorithm WHERE step_id = @step3_id AND algorithm_code = 'WEIGHT_TOWNSHIP_MGMT');

INSERT INTO step_algorithm (step_id, algorithm_name, algorithm_code, algorithm_order,
                            ql_expression, input_params, output_param, description, status, create_time)
SELECT @step3_id, '乡镇灾害备灾能力定权', 'WEIGHT_TOWNSHIP_PREP', 2,
       'TOWNSHIP_PREP_NORM * 0.53 * 0.32',
       'TOWNSHIP_PREP_NORM', 'TOWNSHIP_PREP_WEIGHTED',
       '归一化值 × 一级权重(0.53) × 二级权重(0.32)', 1, NOW()
WHERE NOT EXISTS (SELECT 1 FROM step_algorithm WHERE step_id = @step3_id AND algorithm_code = 'WEIGHT_TOWNSHIP_PREP');

INSERT INTO step_algorithm (step_id, algorithm_name, algorithm_code, algorithm_order,
                            ql_expression, input_params, output_param, description, status, create_time)
SELECT @step3_id, '乡镇自救转移能力定权', 'WEIGHT_TOWNSHIP_RESCUE', 3,
       'TOWNSHIP_RESCUE_NORM * 0.53 * 0.35',
       'TOWNSHIP_RESCUE_NORM', 'TOWNSHIP_RESCUE_WEIGHTED',
       '归一化值 × 一级权重(0.53) × 二级权重(0.35)', 1, NOW()
WHERE NOT EXISTS (SELECT 1 FROM step_algorithm WHERE step_id = @step3_id AND algorithm_code = 'WEIGHT_TOWNSHIP_RESCUE');

INSERT INTO step_algorithm (step_id, algorithm_name, algorithm_code, algorithm_order,
                            ql_expression, input_params, output_param, description, status, create_time)
SELECT @step3_id, '社区-乡镇灾害管理能力定权', 'WEIGHT_COMMUNITY_MGMT', 4,
       'COMMUNITY_MGMT_NORM * 0.47 * 0.32',
       'COMMUNITY_MGMT_NORM', 'COMMUNITY_MGMT_WEIGHTED',
       '归一化值 × 一级权重(0.47) × 二级权重(0.32)', 1, NOW()
WHERE NOT EXISTS (SELECT 1 FROM step_algorithm WHERE step_id = @step3_id AND algorithm_code = 'WEIGHT_COMMUNITY_MGMT');

INSERT INTO step_algorithm (step_id, algorithm_name, algorithm_code, algorithm_order,
                            ql_expression, input_params, output_param, description, status, create_time)
SELECT @step3_id, '社区-乡镇灾害备灾能力定权', 'WEIGHT_COMMUNITY_PREP', 5,
       'COMMUNITY_PREP_NORM * 0.47 * 0.31',
       'COMMUNITY_PREP_NORM', 'COMMUNITY_PREP_WEIGHTED',
       '归一化值 × 一级权重(0.47) × 二级权重(0.31)', 1, NOW()
WHERE NOT EXISTS (SELECT 1 FROM step_algorithm WHERE step_id = @step3_id AND algorithm_code = 'WEIGHT_COMMUNITY_PREP');

INSERT INTO step_algorithm (step_id, algorithm_name, algorithm_code, algorithm_order,
                            ql_expression, input_params, output_param, description, status, create_time)
SELECT @step3_id, '社区-乡镇自救转移能力定权', 'WEIGHT_COMMUNITY_RESCUE', 6,
       'COMMUNITY_RESCUE_NORM * 0.47 * 0.37',
       'COMMUNITY_RESCUE_NORM', 'COMMUNITY_RESCUE_WEIGHTED',
       '归一化值 × 一级权重(0.47) × 二级权重(0.37)', 1, NOW()
WHERE NOT EXISTS (SELECT 1 FROM step_algorithm WHERE step_id = @step3_id AND algorithm_code = 'WEIGHT_COMMUNITY_RESCUE');

-- =====================================================
-- 步骤4：TOPSIS优劣解与得分（12条）
-- =====================================================
INSERT INTO step_algorithm (step_id, algorithm_name, algorithm_code, algorithm_order,
                            ql_expression, input_params, output_param, description, status, create_time)
SELECT @step4_id, '灾害管理能力优解距离', 'MGMT_POSITIVE', 1,
       '@TOPSIS_POSITIVE:TOWNSHIP_MGMT_WEIGHTED,COMMUNITY_MGMT_WEIGHTED',
       'TOWNSHIP_MGMT_WEIGHTED,COMMUNITY_MGMT_WEIGHTED', 'MGMT_POSITIVE_DISTANCE',
       '计算灾害管理能力的优解距离', 1, NOW()
WHERE NOT EXISTS (SELECT 1 FROM step_algorithm WHERE step_id = @step4_id AND algorithm_code = 'MGMT_POSITIVE');

INSERT INTO step_algorithm (step_id, algorithm_name, algorithm_code, algorithm_order,
                            ql_expression, input_params, output_param, description, status, create_time)
SELECT @step4_id, '灾害管理能力劣解距离', 'MGMT_NEGATIVE', 2,
       '@TOPSIS_NEGATIVE:TOWNSHIP_MGMT_WEIGHTED,COMMUNITY_MGMT_WEIGHTED',
       'TOWNSHIP_MGMT_WEIGHTED,COMMUNITY_MGMT_WEIGHTED', 'MGMT_NEGATIVE_DISTANCE',
       '计算灾害管理能力的劣解距离', 1, NOW()
WHERE NOT EXISTS (SELECT 1 FROM step_algorithm WHERE step_id = @step4_id AND algorithm_code = 'MGMT_NEGATIVE');

INSERT INTO step_algorithm (step_id, algorithm_name, algorithm_code, algorithm_order,
                            ql_expression, input_params, output_param, description, status, create_time)
SELECT @step4_id, '灾害管理能力得分', 'MGMT_SCORE', 3,
       'MGMT_NEGATIVE_DISTANCE / (MGMT_NEGATIVE_DISTANCE + MGMT_POSITIVE_DISTANCE)',
       'MGMT_NEGATIVE_DISTANCE,MGMT_POSITIVE_DISTANCE', 'managementScore',
       '劣解距离/(劣解距离+优解距离)', 1, NOW()
WHERE NOT EXISTS (SELECT 1 FROM step_algorithm WHERE step_id = @step4_id AND algorithm_code = 'MGMT_SCORE');

INSERT INTO step_algorithm (step_id, algorithm_name, algorithm_code, algorithm_order,
                            ql_expression, input_params, output_param, description, status, create_time)
SELECT @step4_id, '灾害备灾能力优解距离', 'PREP_POSITIVE', 4,
       '@TOPSIS_POSITIVE:TOWNSHIP_PREP_WEIGHTED,COMMUNITY_PREP_WEIGHTED',
       'TOWNSHIP_PREP_WEIGHTED,COMMUNITY_PREP_WEIGHTED', 'PREP_POSITIVE_DISTANCE',
       '计算灾害备灾能力的优解距离', 1, NOW()
WHERE NOT EXISTS (SELECT 1 FROM step_algorithm WHERE step_id = @step4_id AND algorithm_code = 'PREP_POSITIVE');

INSERT INTO step_algorithm (step_id, algorithm_name, algorithm_code, algorithm_order,
                            ql_expression, input_params, output_param, description, status, create_time)
SELECT @step4_id, '灾害备灾能力劣解距离', 'PREP_NEGATIVE', 5,
       '@TOPSIS_NEGATIVE:TOWNSHIP_PREP_WEIGHTED,COMMUNITY_PREP_WEIGHTED',
       'TOWNSHIP_PREP_WEIGHTED,COMMUNITY_PREP_WEIGHTED', 'PREP_NEGATIVE_DISTANCE',
       '计算灾害备灾能力的劣解距离', 1, NOW()
WHERE NOT EXISTS (SELECT 1 FROM step_algorithm WHERE step_id = @step4_id AND algorithm_code = 'PREP_NEGATIVE');

INSERT INTO step_algorithm (step_id, algorithm_name, algorithm_code, algorithm_order,
                            ql_expression, input_params, output_param, description, status, create_time)
SELECT @step4_id, '灾害备灾能力得分', 'PREP_SCORE', 6,
       'PREP_NEGATIVE_DISTANCE / (PREP_NEGATIVE_DISTANCE + PREP_POSITIVE_DISTANCE)',
       'PREP_NEGATIVE_DISTANCE,PREP_POSITIVE_DISTANCE', 'preparednessScore',
       '劣解距离/(劣解距离+优解距离)', 1, NOW()
WHERE NOT EXISTS (SELECT 1 FROM step_algorithm WHERE step_id = @step4_id AND algorithm_code = 'PREP_SCORE');

INSERT INTO step_algorithm (step_id, algorithm_name, algorithm_code, algorithm_order,
                            ql_expression, input_params, output_param, description, status, create_time)
SELECT @step4_id, '自救转移能力优解距离', 'RESCUE_POSITIVE', 7,
       '@TOPSIS_POSITIVE:TOWNSHIP_RESCUE_WEIGHTED,COMMUNITY_RESCUE_WEIGHTED',
       'TOWNSHIP_RESCUE_WEIGHTED,COMMUNITY_RESCUE_WEIGHTED', 'RESCUE_POSITIVE_DISTANCE',
       '计算自救转移能力的优解距离', 1, NOW()
WHERE NOT EXISTS (SELECT 1 FROM step_algorithm WHERE step_id = @step4_id AND algorithm_code = 'RESCUE_POSITIVE');

INSERT INTO step_algorithm (step_id, algorithm_name, algorithm_code, algorithm_order,
                            ql_expression, input_params, output_param, description, status, create_time)
SELECT @step4_id, '自救转移能力劣解距离', 'RESCUE_NEGATIVE', 8,
       '@TOPSIS_NEGATIVE:TOWNSHIP_RESCUE_WEIGHTED,COMMUNITY_RESCUE_WEIGHTED',
       'TOWNSHIP_RESCUE_WEIGHTED,COMMUNITY_RESCUE_WEIGHTED', 'RESCUE_NEGATIVE_DISTANCE',
       '计算自救转移能力的劣解距离', 1, NOW()
WHERE NOT EXISTS (SELECT 1 FROM step_algorithm WHERE step_id = @step4_id AND algorithm_code = 'RESCUE_NEGATIVE');

INSERT INTO step_algorithm (step_id, algorithm_name, algorithm_code, algorithm_order,
                            ql_expression, input_params, output_param, description, status, create_time)
SELECT @step4_id, '自救转移能力得分', 'RESCUE_SCORE', 9,
       'RESCUE_NEGATIVE_DISTANCE / (RESCUE_NEGATIVE_DISTANCE + RESCUE_POSITIVE_DISTANCE)',
       'RESCUE_NEGATIVE_DISTANCE,RESCUE_POSITIVE_DISTANCE', 'rescueScore',
       '劣解距离/(劣解距离+优解距离)', 1, NOW()
WHERE NOT EXISTS (SELECT 1 FROM step_algorithm WHERE step_id = @step4_id AND algorithm_code = 'RESCUE_SCORE');

INSERT INTO step_algorithm (step_id, algorithm_name, algorithm_code, algorithm_order,
                            ql_expression, input_params, output_param, description, status, create_time)
SELECT @step4_id, '综合减灾能力优解距离', 'COMPREHENSIVE_POSITIVE', 10,
       '@TOPSIS_POSITIVE:TOWNSHIP_MGMT_WEIGHTED,TOWNSHIP_PREP_WEIGHTED,TOWNSHIP_RESCUE_WEIGHTED,COMMUNITY_MGMT_WEIGHTED,COMMUNITY_PREP_WEIGHTED,COMMUNITY_RESCUE_WEIGHTED',
       'TOWNSHIP_MGMT_WEIGHTED,TOWNSHIP_PREP_WEIGHTED,TOWNSHIP_RESCUE_WEIGHTED,COMMUNITY_MGMT_WEIGHTED,COMMUNITY_PREP_WEIGHTED,COMMUNITY_RESCUE_WEIGHTED',
       'COMPREHENSIVE_POSITIVE_DISTANCE', '计算综合减灾能力的优解距离（6个指标）', 1, NOW()
WHERE NOT EXISTS (SELECT 1 FROM step_algorithm WHERE step_id = @step4_id AND algorithm_code = 'COMPREHENSIVE_POSITIVE');

INSERT INTO step_algorithm (step_id, algorithm_name, algorithm_code, algorithm_order,
                            ql_expression, input_params, output_param, description, status, create_time)
SELECT @step4_id, '综合减灾能力劣解距离', 'COMPREHENSIVE_NEGATIVE', 11,
       '@TOPSIS_NEGATIVE:TOWNSHIP_MGMT_WEIGHTED,TOWNSHIP_PREP_WEIGHTED,TOWNSHIP_RESCUE_WEIGHTED,COMMUNITY_MGMT_WEIGHTED,COMMUNITY_PREP_WEIGHTED,COMMUNITY_RESCUE_WEIGHTED',
       'TOWNSHIP_MGMT_WEIGHTED,TOWNSHIP_PREP_WEIGHTED,TOWNSHIP_RESCUE_WEIGHTED,COMMUNITY_MGMT_WEIGHTED,COMMUNITY_PREP_WEIGHTED,COMMUNITY_RESCUE_WEIGHTED',
       'COMPREHENSIVE_NEGATIVE_DISTANCE', '计算综合减灾能力的劣解距离（6个指标）', 1, NOW()
WHERE NOT EXISTS (SELECT 1 FROM step_algorithm WHERE step_id = @step4_id AND algorithm_code = 'COMPREHENSIVE_NEGATIVE');

INSERT INTO step_algorithm (step_id, algorithm_name, algorithm_code, algorithm_order,
                            ql_expression, input_params, output_param, description, status, create_time)
SELECT @step4_id, '综合减灾能力得分', 'COMPREHENSIVE_SCORE', 12,
       'COMPREHENSIVE_NEGATIVE_DISTANCE / (COMPREHENSIVE_NEGATIVE_DISTANCE + COMPREHENSIVE_POSITIVE_DISTANCE)',
       'COMPREHENSIVE_NEGATIVE_DISTANCE,COMPREHENSIVE_POSITIVE_DISTANCE', 'comprehensiveScore',
       '劣解距离/(劣解距离+优解距离)', 1, NOW()
WHERE NOT EXISTS (SELECT 1 FROM step_algorithm WHERE step_id = @step4_id AND algorithm_code = 'COMPREHENSIVE_SCORE');

-- =====================================================
-- 步骤5：能力等级划分（4条）
-- =====================================================
INSERT INTO step_algorithm (step_id, algorithm_name, algorithm_code, algorithm_order,
                            ql_expression, input_params, output_param, description, status, create_time)
SELECT @step5_id, '灾害管理能力等级', 'MGMT_GRADE', 1,
       '@GRADE:managementScore',
       'managementScore', 'managementGrade',
       '基于均值±标准差划分灾害管理能力等级', 1, NOW()
WHERE NOT EXISTS (SELECT 1 FROM step_algorithm WHERE step_id = @step5_id AND algorithm_code = 'MGMT_GRADE');

INSERT INTO step_algorithm (step_id, algorithm_name, algorithm_code, algorithm_order,
                            ql_expression, input_params, output_param, description, status, create_time)
SELECT @step5_id, '灾害备灾能力等级', 'PREP_GRADE', 2,
       '@GRADE:preparednessScore',
       'preparednessScore', 'preparednessGrade',
       '基于均值±标准差划分灾害备灾能力等级', 1, NOW()
WHERE NOT EXISTS (SELECT 1 FROM step_algorithm WHERE step_id = @step5_id AND algorithm_code = 'PREP_GRADE');

INSERT INTO step_algorithm (step_id, algorithm_name, algorithm_code, algorithm_order,
                            ql_expression, input_params, output_param, description, status, create_time)
SELECT @step5_id, '自救转移能力等级', 'RESCUE_GRADE', 3,
       '@GRADE:rescueScore',
       'rescueScore', 'rescueGrade',
       '基于均值±标准差划分自救转移能力等级', 1, NOW()
WHERE NOT EXISTS (SELECT 1 FROM step_algorithm WHERE step_id = @step5_id AND algorithm_code = 'RESCUE_GRADE');

INSERT INTO step_algorithm (step_id, algorithm_name, algorithm_code, algorithm_order,
                            ql_expression, input_params, output_param, description, status, create_time)
SELECT @step5_id, '综合减灾能力等级', 'COMPREHENSIVE_GRADE', 4,
       '@GRADE:comprehensiveScore',
       'comprehensiveScore', 'comprehensiveGrade',
       '基于均值±标准差划分综合减灾能力等级', 1, NOW()
WHERE NOT EXISTS (SELECT 1 FROM step_algorithm WHERE step_id = @step5_id AND algorithm_code = 'COMPREHENSIVE_GRADE');
