SET NAMES utf8mb4;

-- 1. 插入模型 (ID=16)
INSERT INTO evaluation_model (id, model_name, model_code, version, description, status)
VALUES (16, '2020年家庭减灾能力评估模型', 'FAMILY_DISASTER_REDUCTION_2020', '1.0.0', '基于家庭调查数据的减灾能力评估', 1)
ON DUPLICATE KEY UPDATE model_name = VALUES(model_name), description = VALUES(description);

-- 2. 清理已有步骤和算法（保证幂等操作）
DELETE FROM step_algorithm WHERE step_id IN (SELECT id FROM model_step WHERE model_id = 16);
DELETE FROM model_step WHERE model_id = 16;

-- 3. 插入步骤
-- Step 1: 数据加载
INSERT INTO model_step (model_id, step_name, step_code, step_order, step_type, description, status)
VALUES (16, '数据加载', 'data_loading', 1, 'LOAD_DATA', '加载家庭调查数据', 1);

-- Step 2: 区域加权聚合
INSERT INTO model_step (model_id, step_name, step_code, step_order, step_type, description, status)
VALUES (16, '区域加权聚合', 'regional_aggregation', 2, 'AGGREGATION', '按区域加权聚合家庭数据', 1);

-- Step 3: 归一化
INSERT INTO model_step (model_id, step_name, step_code, step_order, step_type, description, status)
VALUES (16, '数据归一化', 'data_normalization', 3, 'NORMALIZATION', '将聚合后的数据进行归一化处理', 1);

-- Step 4: TOPSIS距离计算
INSERT INTO model_step (model_id, step_name, step_code, step_order, step_type, description, status)
VALUES (16, 'TOPSIS距离计算', 'distance_to_ideal', 4, 'TOPSIS', '计算正负理想解距离', 1);

-- Step 5: 结果评级
INSERT INTO model_step (model_id, step_name, step_code, step_order, step_type, description, status)
VALUES (16, '结果评级', 'primary_indicator_level', 5, 'GRADING', '计算最终能力值并评级', 1);

-- 获取步骤ID
SET @step1_id = (SELECT id FROM model_step WHERE model_id = 16 AND step_code = 'data_loading' LIMIT 1);
SET @step2_id = (SELECT id FROM model_step WHERE model_id = 16 AND step_code = 'regional_aggregation' LIMIT 1);
SET @step3_id = (SELECT id FROM model_step WHERE model_id = 16 AND step_code = 'data_normalization' LIMIT 1);
SET @step4_id = (SELECT id FROM model_step WHERE model_id = 16 AND step_code = 'distance_to_ideal' LIMIT 1);
SET @step5_id = (SELECT id FROM model_step WHERE model_id = 16 AND step_code = 'primary_indicator_level' LIMIT 1);

-- 4. 插入算法
-- Step 1: 数据加载
INSERT INTO step_algorithm (step_id, algorithm_name, algorithm_code, algorithm_order, ql_expression, input_params, output_param, description, status)
VALUES (@step1_id, '加载家庭调查数据', 'EL_FAMILY_DATA_LOAD', 1, '@LOAD_DATA:family_disaster_reduction_capacity_2020', '{"required":["regionCode"]}', 'family_raw_data', '加载家庭调查数据', 1);

-- Step 2: 区域加权聚合
INSERT INTO step_algorithm (step_id, algorithm_name, algorithm_code, algorithm_order, ql_expression, input_params, output_param, description, status)
VALUES (@step2_id, '家庭数据区域加权聚合', 'EL_FAMILY_AGG', 1, '@AGGREGATE_WEIGHTED:family_raw_data', '{"required":["family_raw_data","weight"]}', 'family_aggregated_data', '家庭数据加权聚合', 1);

-- Step 3: 归一化
INSERT INTO step_algorithm (step_id, algorithm_name, algorithm_code, algorithm_order, ql_expression, input_params, output_param, description, status)
VALUES (@step3_id, '家庭数据归一化', 'EL_FAMILY_NORM', 1, '@NORMALIZE:family_aggregated_data', '{"required":["family_aggregated_data"]}', 'family_normalized_data', '家庭数据归一化', 1);

-- Step 4: TOPSIS距离计算
INSERT INTO step_algorithm (step_id, algorithm_name, algorithm_code, algorithm_order, ql_expression, input_params, output_param, description, status)
VALUES (@step4_id, '家庭减灾能力D+公式', 'EL_FAMILY_D_PLUS', 1, '@TOPSIS_POSITIVE:emergency_supplies,water_reserve_days,food_reserve_days,in_community_group,know_staff_contact,received_warning_types,know_evacuation_route,drill_participation_count,first_aid_training,mastered_first_aid_skills', '{"required":["weighted_matrix"]}', 'family_d_plus', '计算家庭减灾能力正理想解距离', 1);

INSERT INTO step_algorithm (step_id, algorithm_name, algorithm_code, algorithm_order, ql_expression, input_params, output_param, description, status)
VALUES (@step4_id, '家庭减灾能力D-公式', 'EL_FAMILY_D_MINUS', 2, '@TOPSIS_NEGATIVE:emergency_supplies,water_reserve_days,food_reserve_days,in_community_group,know_staff_contact,received_warning_types,know_evacuation_route,drill_participation_count,first_aid_training,mastered_first_aid_skills', '{"required":["weighted_matrix"]}', 'family_d_minus', '计算家庭减灾能力负理想解距离', 1);

INSERT INTO step_algorithm (step_id, algorithm_name, algorithm_code, algorithm_order, ql_expression, input_params, output_param, description, status)
VALUES (@step4_id, '家庭减灾能力值公式', 'EL_FAMILY_SCORE', 3, 'family_d_minus / (family_d_plus + family_d_minus)', '{"required":["family_d_plus","family_d_minus"]}', 'family_capability_score', '计算家庭减灾能力得分', 1);

-- Step 5: 结果评级
INSERT INTO step_algorithm (step_id, algorithm_name, algorithm_code, algorithm_order, ql_expression, input_params, output_param, description, status)
VALUES (@step5_id, '家庭减灾能力评级', 'EL_FAMILY_GRADE', 1, '@GRADE:family_capability_score', '{"required":["family_capability_score"]}', 'family_capability_level', '家庭减灾能力结果评级', 1);
