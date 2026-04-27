SET NAMES utf8mb4;

-- 2020 家庭减灾能力评估模型
-- Excel 口径：逐户赋值 -> 区县汇总 -> 向量归一化 -> 定权 -> TOPSIS -> μ/σ 分级

INSERT INTO evaluation_model (id, model_name, model_code, version, description, status)
VALUES (16, '2020年家庭减灾能力评估模型', 'FAMILY_DISASTER_REDUCTION_2020', '1.0.0',
        '基于家庭调查数据的减灾能力评估（Excel验算口径）', 1)
ON DUPLICATE KEY UPDATE model_name = VALUES(model_name), description = VALUES(description);

DELETE FROM step_algorithm WHERE step_id IN (SELECT id FROM model_step WHERE model_id = 16);
DELETE FROM model_step WHERE model_id = 16;

INSERT INTO model_step (model_id, step_name, step_code, step_order, step_type, description, status)
VALUES
(16, '数据聚合', 'data_aggregation', 1, 'CALCULATION', '家庭调查原始数据按区县汇总', 1),
(16, '指标赋值', 'indicator_assignment', 2, 'CALCULATION', '区县汇总后的二级指标赋值', 1),
(16, '数据归一化', 'normalization', 3, 'NORMALIZATION', '对正向指标执行向量归一化', 1),
(16, '指标定权', 'weighting', 4, 'CALCULATION', '一级/二级权重定权', 1),
(16, '贴近度与等级', 'score_and_grade', 5, 'GRADING', '计算一级指标、综合得分与等级', 1);

SET @step1_id = (SELECT id FROM model_step WHERE model_id = 16 AND step_code = 'data_aggregation' LIMIT 1);
SET @step2_id = (SELECT id FROM model_step WHERE model_id = 16 AND step_code = 'indicator_assignment' LIMIT 1);
SET @step3_id = (SELECT id FROM model_step WHERE model_id = 16 AND step_code = 'normalization' LIMIT 1);
SET @step4_id = (SELECT id FROM model_step WHERE model_id = 16 AND step_code = 'weighting' LIMIT 1);
SET @step5_id = (SELECT id FROM model_step WHERE model_id = 16 AND step_code = 'score_and_grade' LIMIT 1);

-- Step1: 数据聚合（社区/家庭明细聚合到区县）
INSERT INTO step_algorithm (step_id, algorithm_name, algorithm_code, algorithm_order, ql_expression, input_params, output_param, description, status)
VALUES
(@step1_id, '0-10岁人数（区县汇总）', 'FAM_AGG_001', 1, 'age_0_10_count', '{}', 'age_0_10_count_value', '', 1),
(@step1_id, '65岁及以上人数（区县汇总）', 'FAM_AGG_002', 2, 'age_65_plus_count', '{}', 'age_65_plus_count_value', '', 1),
(@step1_id, '残障人数（区县汇总）', 'FAM_AGG_003', 3, 'disabled_count', '{}', 'disabled_count_value', '', 1),
(@step1_id, '家庭总人数（区县汇总）', 'FAM_AGG_004', 4, 'total_people', '{}', 'total_people_value', '', 1),
(@step1_id, '慢性病人数（区县汇总）', 'FAM_AGG_005', 5, 'chronic_disease_count', '{}', 'chronic_disease_count_value', '', 1),
(@step1_id, '应急物品储备（区县汇总）', 'FAM_AGG_006', 6, 'emergency_supplies', '{}', 'emergency_supplies_agg', '', 1),
(@step1_id, '饮用水储量（区县汇总）', 'FAM_AGG_007', 7, 'water_reserve_days', '{}', 'water_reserve_agg', '', 1),
(@step1_id, '方便食物储量（区县汇总）', 'FAM_AGG_008', 8, 'food_reserve_days', '{}', 'food_reserve_agg', '', 1),
(@step1_id, '社区联系群（区县汇总）', 'FAM_AGG_009', 9, 'in_community_group', '{}', 'in_community_group_agg', '', 1),
(@step1_id, '工作人员联系方式（区县汇总）', 'FAM_AGG_010', 10, 'know_staff_contact', '{}', 'know_staff_contact_agg', '', 1),
(@step1_id, '预警信息种类（区县汇总）', 'FAM_AGG_011', 11, 'received_warning_types', '{}', 'received_warning_types_agg', '', 1),
(@step1_id, '避难路线（区县汇总）', 'FAM_AGG_012', 12, 'know_evacuation_route', '{}', 'know_evacuation_route_agg', '', 1),
(@step1_id, '应急演练次数（区县汇总）', 'FAM_AGG_013', 13, 'drill_participation_count', '{}', 'drill_participation_agg', '', 1),
(@step1_id, '急救培训（区县汇总）', 'FAM_AGG_014', 14, 'first_aid_training', '{}', 'first_aid_training_agg', '', 1),
(@step1_id, '急救技能数量（区县汇总）', 'FAM_AGG_015', 15, 'mastered_first_aid_skills', '{}', 'mastered_first_aid_skills_agg', '', 1);

-- Step2: 区县级二级指标赋值
INSERT INTO step_algorithm (step_id, algorithm_name, algorithm_code, algorithm_order, ql_expression, input_params, output_param, description, status)
VALUES
(@step2_id, '家庭脆弱人员占比', 'FAM_ASSIGN_001', 1, 'total_people_value == 0 ? 0 : (age_0_10_count_value + age_65_plus_count_value + disabled_count_value) / total_people_value', '{}', 'vul_population_ratio', '', 1),
(@step2_id, '慢性病人员占比', 'FAM_ASSIGN_002', 2, 'total_people_value == 0 ? 0 : chronic_disease_count_value / total_people_value', '{}', 'chronic_ratio', '', 1),
(@step2_id, '应急物品储备', 'FAM_ASSIGN_003', 3, 'emergency_supplies_agg', '{}', 'emergency_supplies_value', '', 1),
(@step2_id, '饮用水储量', 'FAM_ASSIGN_004', 4, 'water_reserve_agg', '{}', 'water_reserve_value', '', 1),
(@step2_id, '方便食物储量', 'FAM_ASSIGN_005', 5, 'food_reserve_agg', '{}', 'food_reserve_value', '', 1),
(@step2_id, '社区联系群', 'FAM_ASSIGN_006', 6, 'in_community_group_agg', '{}', 'in_community_group_value', '', 1),
(@step2_id, '工作人员联系方式', 'FAM_ASSIGN_007', 7, 'know_staff_contact_agg', '{}', 'know_staff_contact_value', '', 1),
(@step2_id, '预警信息种类', 'FAM_ASSIGN_008', 8, 'received_warning_types_agg', '{}', 'received_warning_types_value', '', 1),
(@step2_id, '避难路线', 'FAM_ASSIGN_009', 9, 'know_evacuation_route_agg', '{}', 'know_evacuation_route_value', '', 1),
(@step2_id, '应急演练次数', 'FAM_ASSIGN_010', 10, 'drill_participation_agg', '{}', 'drill_participation_value', '', 1),
(@step2_id, '急救培训', 'FAM_ASSIGN_011', 11, 'first_aid_training_agg', '{}', 'first_aid_training_value', '', 1),
(@step2_id, '急救技能数量', 'FAM_ASSIGN_012', 12, 'mastered_first_aid_skills_agg', '{}', 'mastered_first_aid_skills_value', '', 1);

-- Step3: 向量归一化
INSERT INTO step_algorithm (step_id, algorithm_name, algorithm_code, algorithm_order, ql_expression, input_params, output_param, description, status)
VALUES
(@step3_id, '家庭脆弱人员占比归一化', 'FAM_NORM_001', 1, '@NORMALIZE:vul_population_ratio', '{}', 'vul_population_ratio_norm', '', 1),
(@step3_id, '慢性病人员占比归一化', 'FAM_NORM_002', 2, '@NORMALIZE:chronic_ratio', '{}', 'chronic_ratio_norm', '', 1),
(@step3_id, '应急物品储备归一化', 'FAM_NORM_003', 3, '@NORMALIZE:emergency_supplies_value', '{}', 'emergency_supplies_value_norm', '', 1),
(@step3_id, '饮用水储量归一化', 'FAM_NORM_004', 4, '@NORMALIZE:water_reserve_value', '{}', 'water_reserve_value_norm', '', 1),
(@step3_id, '方便食物储量归一化', 'FAM_NORM_005', 5, '@NORMALIZE:food_reserve_value', '{}', 'food_reserve_value_norm', '', 1),
(@step3_id, '社区联系群归一化', 'FAM_NORM_006', 6, '@NORMALIZE:in_community_group_value', '{}', 'in_community_group_value_norm', '', 1),
(@step3_id, '工作人员联系方式归一化', 'FAM_NORM_007', 7, '@NORMALIZE:know_staff_contact_value', '{}', 'know_staff_contact_value_norm', '', 1),
(@step3_id, '预警信息种类归一化', 'FAM_NORM_008', 8, '@NORMALIZE:received_warning_types_value', '{}', 'received_warning_types_value_norm', '', 1),
(@step3_id, '避难路线归一化', 'FAM_NORM_009', 9, '@NORMALIZE:know_evacuation_route_value', '{}', 'know_evacuation_route_value_norm', '', 1),
(@step3_id, '应急演练次数归一化', 'FAM_NORM_010', 10, '@NORMALIZE:drill_participation_value', '{}', 'drill_participation_value_norm', '', 1),
(@step3_id, '急救培训归一化', 'FAM_NORM_011', 11, '@NORMALIZE:first_aid_training_value', '{}', 'first_aid_training_value_norm', '', 1),
(@step3_id, '急救技能数量归一化', 'FAM_NORM_012', 12, '@NORMALIZE:mastered_first_aid_skills_value', '{}', 'mastered_first_aid_skills_value_norm', '', 1);

-- Step4: 定权
INSERT INTO step_algorithm (step_id, algorithm_name, algorithm_code, algorithm_order, ql_expression, input_params, output_param, description, status)
VALUES
(@step4_id, '家庭脆弱人员占比定权', 'FAM_WEIGHT_001', 1, 'vul_population_ratio * 0.132', '{}', 'w_vul_population_ratio', '', 1),
(@step4_id, '慢性病人员占比定权', 'FAM_WEIGHT_002', 2, 'chronic_ratio_norm * 0.108', '{}', 'w_chronic_ratio', '', 1),
(@step4_id, '应急物品储备定权', 'FAM_WEIGHT_003', 3, 'emergency_supplies_value_norm * 0.0693', '{}', 'w_emergency_supplies', '', 1),
(@step4_id, '饮用水储量定权', 'FAM_WEIGHT_004', 4, 'water_reserve_value_norm * 0.0693', '{}', 'w_water_reserve', '', 1),
(@step4_id, '方便食物储量定权', 'FAM_WEIGHT_005', 5, 'food_reserve_value_norm * 0.0714', '{}', 'w_food_reserve', '', 1),
(@step4_id, '社区联系群定权', 'FAM_WEIGHT_006', 6, 'in_community_group_value_norm * 0.0899', '{}', 'w_in_community_group', '', 1),
(@step4_id, '工作人员联系方式定权', 'FAM_WEIGHT_007', 7, 'know_staff_contact_value_norm * 0.0957', '{}', 'w_know_staff_contact', '', 1),
(@step4_id, '预警信息种类定权', 'FAM_WEIGHT_008', 8, 'received_warning_types_value_norm * 0.1044', '{}', 'w_received_warning_types', '', 1),
(@step4_id, '避难路线定权', 'FAM_WEIGHT_009', 9, 'know_evacuation_route_value_norm * 0.0806', '{}', 'w_know_evacuation_route', '', 1),
(@step4_id, '应急演练次数定权', 'FAM_WEIGHT_010', 10, 'drill_participation_value_norm * 0.065', '{}', 'w_drill_participation', '', 1),
(@step4_id, '急救培训定权', 'FAM_WEIGHT_011', 11, 'first_aid_training_value_norm * 0.0572', '{}', 'w_first_aid_training', '', 1),
(@step4_id, '急救技能数量定权', 'FAM_WEIGHT_012', 12, 'mastered_first_aid_skills_value_norm * 0.0572', '{}', 'w_mastered_first_aid_skills', '', 1),

(@step4_id, '一级指标-家庭脆弱性-家庭脆弱人员占比', 'FAM_L1_WEIGHT_001', 13, 'vul_population_ratio * 0.55', '{}', 'l1_vul_item_1', '', 1),
(@step4_id, '一级指标-家庭脆弱性-慢性病人员占比', 'FAM_L1_WEIGHT_002', 14, 'chronic_ratio_norm * 0.45', '{}', 'l1_vul_item_2', '', 1),
(@step4_id, '一级指标-物资储备-应急物品储备', 'FAM_L1_WEIGHT_003', 15, 'emergency_supplies_value_norm * 0.33', '{}', 'l1_mat_item_1', '', 1),
(@step4_id, '一级指标-物资储备-饮用水储量', 'FAM_L1_WEIGHT_004', 16, 'water_reserve_value_norm * 0.33', '{}', 'l1_mat_item_2', '', 1),
(@step4_id, '一级指标-物资储备-方便食物储量', 'FAM_L1_WEIGHT_005', 17, 'food_reserve_value_norm * 0.34', '{}', 'l1_mat_item_3', '', 1),
(@step4_id, '一级指标-信息获取-社区联系群', 'FAM_L1_WEIGHT_006', 18, 'in_community_group_value_norm * 0.31', '{}', 'l1_info_item_1', '', 1),
(@step4_id, '一级指标-信息获取-工作人员联系方式', 'FAM_L1_WEIGHT_007', 19, 'know_staff_contact_value_norm * 0.33', '{}', 'l1_info_item_2', '', 1),
(@step4_id, '一级指标-信息获取-预警信息种类', 'FAM_L1_WEIGHT_008', 20, 'received_warning_types_value_norm * 0.36', '{}', 'l1_info_item_3', '', 1),
(@step4_id, '一级指标-自救互救-避难路线', 'FAM_L1_WEIGHT_009', 21, 'know_evacuation_route_value_norm * 0.31', '{}', 'l1_self_item_1', '', 1),
(@step4_id, '一级指标-自救互救-应急演练次数', 'FAM_L1_WEIGHT_010', 22, 'drill_participation_value_norm * 0.25', '{}', 'l1_self_item_2', '', 1),
(@step4_id, '一级指标-自救互救-急救培训', 'FAM_L1_WEIGHT_011', 23, 'first_aid_training_value_norm * 0.22', '{}', 'l1_self_item_3', '', 1),
(@step4_id, '一级指标-自救互救-急救技能数量', 'FAM_L1_WEIGHT_012', 24, 'mastered_first_aid_skills_value_norm * 0.22', '{}', 'l1_self_item_4', '', 1);

-- Step5: 一级指标、综合得分和等级
INSERT INTO step_algorithm (step_id, algorithm_name, algorithm_code, algorithm_order, ql_expression, input_params, output_param, description, status)
VALUES
(@step5_id, '家庭脆弱性D+', 'FAM_SCORE_001', 1, '@TOPSIS_POSITIVE:l1_vul_item_1,l1_vul_item_2', '{}', 'l1_vul_d_plus', '', 1),
(@step5_id, '家庭脆弱性D-', 'FAM_SCORE_002', 2, '@TOPSIS_NEGATIVE:l1_vul_item_1,l1_vul_item_2', '{}', 'l1_vul_d_minus', '', 1),
(@step5_id, '家庭脆弱性得分', 'FAM_SCORE_003', 3, '@TOPSIS_SCORE:l1_vul_d_plus,l1_vul_d_minus', '{}', 'l1_vul_score', '', 1),

(@step5_id, '防灾物资储备能力D+', 'FAM_SCORE_004', 4, '@TOPSIS_POSITIVE:l1_mat_item_1,l1_mat_item_2,l1_mat_item_3', '{}', 'l1_mat_d_plus', '', 1),
(@step5_id, '防灾物资储备能力D-', 'FAM_SCORE_005', 5, '@TOPSIS_NEGATIVE:l1_mat_item_1,l1_mat_item_2,l1_mat_item_3', '{}', 'l1_mat_d_minus', '', 1),
(@step5_id, '防灾物资储备能力得分', 'FAM_SCORE_006', 6, '@TOPSIS_SCORE:l1_mat_d_plus,l1_mat_d_minus', '{}', 'l1_mat_score', '', 1),

(@step5_id, '灾害信息获取能力D+', 'FAM_SCORE_007', 7, '@TOPSIS_POSITIVE:l1_info_item_1,l1_info_item_2,l1_info_item_3', '{}', 'l1_info_d_plus', '', 1),
(@step5_id, '灾害信息获取能力D-', 'FAM_SCORE_008', 8, '@TOPSIS_NEGATIVE:l1_info_item_1,l1_info_item_2,l1_info_item_3', '{}', 'l1_info_d_minus', '', 1),
(@step5_id, '灾害信息获取能力得分', 'FAM_SCORE_009', 9, '@TOPSIS_SCORE:l1_info_d_plus,l1_info_d_minus', '{}', 'l1_info_score', '', 1),

(@step5_id, '灾害自救互救能力D+', 'FAM_SCORE_010', 10, '@TOPSIS_POSITIVE:l1_self_item_1,l1_self_item_2,l1_self_item_3,l1_self_item_4', '{}', 'l1_self_d_plus', '', 1),
(@step5_id, '灾害自救互救能力D-', 'FAM_SCORE_011', 11, '@TOPSIS_NEGATIVE:l1_self_item_1,l1_self_item_2,l1_self_item_3,l1_self_item_4', '{}', 'l1_self_d_minus', '', 1),
(@step5_id, '灾害自救互救能力得分', 'FAM_SCORE_012', 12, '@TOPSIS_SCORE:l1_self_d_plus,l1_self_d_minus', '{}', 'l1_self_score', '', 1),

(@step5_id, '家庭综合能力D+', 'FAM_SCORE_013', 13, '@TOPSIS_POSITIVE:w_vul_population_ratio,w_chronic_ratio,w_emergency_supplies,w_water_reserve,w_food_reserve,w_in_community_group,w_know_staff_contact,w_received_warning_types,w_know_evacuation_route,w_drill_participation,w_first_aid_training,w_mastered_first_aid_skills', '{}', 'family_d_plus', '', 1),
(@step5_id, '家庭综合能力D-', 'FAM_SCORE_014', 14, '@TOPSIS_NEGATIVE:w_vul_population_ratio,w_chronic_ratio,w_emergency_supplies,w_water_reserve,w_food_reserve,w_in_community_group,w_know_staff_contact,w_received_warning_types,w_know_evacuation_route,w_drill_participation,w_first_aid_training,w_mastered_first_aid_skills', '{}', 'family_d_minus', '', 1),
(@step5_id, '家庭综合能力得分', 'FAM_SCORE_015', 15, '@TOPSIS_SCORE:family_d_plus,family_d_minus', '{}', 'family_capability_score', '', 1),

(@step5_id, '家庭脆弱性等级', 'FAM_GRADE_001', 16, '@GRADE:l1_vul_score', '{}', 'l1_vul_level', '', 1),
(@step5_id, '防灾物资储备能力等级', 'FAM_GRADE_002', 17, '@GRADE:l1_mat_score', '{}', 'l1_mat_level', '', 1),
(@step5_id, '灾害信息获取能力等级', 'FAM_GRADE_003', 18, '@GRADE:l1_info_score', '{}', 'l1_info_level', '', 1),
(@step5_id, '灾害自救互救能力等级', 'FAM_GRADE_004', 19, '@GRADE:l1_self_score', '{}', 'l1_self_level', '', 1),
(@step5_id, '家庭综合能力等级', 'FAM_GRADE_005', 20, '@GRADE:family_capability_score', '{}', 'family_capability_level', '', 1),

(@step5_id, '管理能力兼容字段', 'FAM_ALIAS_001', 21, 'l1_vul_score', '{}', 'management_capability_score', '', 1),
(@step5_id, '备灾能力兼容字段', 'FAM_ALIAS_002', 22, 'l1_mat_score', '{}', 'support_capability_score', '', 1),
(@step5_id, '自救能力兼容字段', 'FAM_ALIAS_003', 23, 'l1_self_score', '{}', 'self_rescue_capability_score', '', 1),
(@step5_id, '综合能力兼容字段', 'FAM_ALIAS_004', 24, 'family_capability_score', '{}', 'comprehensive_capability_score', '', 1),
(@step5_id, '综合等级兼容字段', 'FAM_ALIAS_005', 25, 'family_capability_level', '{}', 'comprehensive_capability_level', '', 1);
