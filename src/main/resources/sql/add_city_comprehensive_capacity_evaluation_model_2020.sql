SET NAMES utf8mb4;

-- 1) 新增模型（ID=20，独立于现有综合模型）
INSERT INTO evaluation_model (id, model_name, model_code, version, description, status)
VALUES (20, '2020年市级综合减灾能力评估模型', 'CITY_COMPREHENSIVE_DISASTER_REDUCTION_2020', '1.0.0',
        '基于前置评估结果（政府/企业/社会组织/乡镇区县单元/社区区县单元/家庭）的市级综合减灾能力评估模型', 1)
ON DUPLICATE KEY UPDATE model_name = VALUES(model_name),
                        model_code = VALUES(model_code),
                        description = VALUES(description),
                        status = VALUES(status);

-- 2) 幂等清理旧步骤和算法
DELETE FROM step_algorithm WHERE step_id IN (SELECT id FROM model_step WHERE model_id = 20);
DELETE FROM model_step WHERE model_id = 20;

-- 3) 新增步骤（贴近度与等级合并为一步）
INSERT INTO model_step (model_id, step_name, step_code, step_order, step_type, description, status) VALUES
(20, '前置评估结果加载', 'load_source_results', 1, 'CALCULATION', '加载前置模型一级指标值', 1),
(20, '指标归一化', 'data_normalization', 2, 'NORMALIZATION', '向量归一化', 1),
(20, '指标加权', 'weighted_matrix', 3, 'WEIGHTING', '固定权重加权', 1),
(20, '理想解距离计算', 'distance_to_ideal', 4, 'TOPSIS', '计算D+与D-', 1),
(20, '贴近度与等级', 'score_and_grade', 5, 'GRADING', '计算Ci并分级', 1);

SET @step1_id = (SELECT id FROM model_step WHERE model_id = 20 AND step_code = 'load_source_results' LIMIT 1);
SET @step2_id = (SELECT id FROM model_step WHERE model_id = 20 AND step_code = 'data_normalization' LIMIT 1);
SET @step3_id = (SELECT id FROM model_step WHERE model_id = 20 AND step_code = 'weighted_matrix' LIMIT 1);
SET @step4_id = (SELECT id FROM model_step WHERE model_id = 20 AND step_code = 'distance_to_ideal' LIMIT 1);
SET @step5_id = (SELECT id FROM model_step WHERE model_id = 20 AND step_code = 'score_and_grade' LIMIT 1);

-- 4) Step1: 数据加载（modelKey由后端解析为对应前置模型ID，按同模型/同年/同组织/同地区取最后一条）
INSERT INTO step_algorithm (step_id, algorithm_name, algorithm_code, algorithm_order, ql_expression, input_params, output_param, description, status) VALUES
(@step1_id, '政府-管理能力', 'LOAD_GOV_MGMT', 1, '@LOAD_EVAL_RESULT:modelKey=government,stepCode=primary_indicator_value,field=management_capability', '{}', 'gov_management', '', 1),
(@step1_id, '政府-工程设防能力', 'LOAD_GOV_ENG', 2, '@LOAD_EVAL_RESULT:modelKey=government,stepCode=primary_indicator_value,field=engineering_defense_capability', '{}', 'gov_engineering', '', 1),
(@step1_id, '政府-监测预警能力', 'LOAD_GOV_MONITOR', 3, '@LOAD_EVAL_RESULT:modelKey=government,stepCode=primary_indicator_value,field=monitoring_warning_capability', '{}', 'gov_monitoring', '', 1),
(@step1_id, '政府-物资储备能力', 'LOAD_GOV_MATERIAL', 4, '@LOAD_EVAL_RESULT:modelKey=government,stepCode=primary_indicator_value,field=material_reserve_capability', '{}', 'gov_material', '', 1),
(@step1_id, '政府-专业队伍救援能力', 'LOAD_GOV_TEAM', 5, '@LOAD_EVAL_RESULT:modelKey=government,stepCode=primary_indicator_value,field=professional_rescue_capability', '{}', 'gov_rescue_team', '', 1),
(@step1_id, '政府-转移安置能力', 'LOAD_GOV_TRANSFER', 6, '@LOAD_EVAL_RESULT:modelKey=government,stepCode=primary_indicator_value,field=relocation_resettlement_capability', '{}', 'gov_relocation', '', 1),

(@step1_id, '企业-大型工程建设等企业应急救援能力', 'LOAD_ENT_ENG', 7, '@LOAD_EVAL_RESULT:modelKey=enterprise,stepCode=primary_indicator_value,field=engineering_rescue_capacity', '{}', 'enterprise_engineering', '', 1),
(@step1_id, '企业-保险和再保险企业救灾能力', 'LOAD_ENT_INSURANCE', 8, '@LOAD_EVAL_RESULT:modelKey=enterprise,stepCode=primary_indicator_value,field=insurance_reinsurance_capacity', '{}', 'enterprise_insurance', '', 1),

(@step1_id, '社会组织-物资储备能力', 'LOAD_SOC_MATERIAL', 9, '@LOAD_EVAL_RESULT:modelKey=socialOrganization,stepCode=indicator_assignment,field=large_excavator_owning_rate', '{}', 'social_material', '', 1),
(@step1_id, '社会组织-应急运输能力', 'LOAD_SOC_TRANSPORT', 10, '@LOAD_EVAL_RESULT:modelKey=socialOrganization,stepCode=indicator_assignment,field=large_truck_crane_owning_rate', '{}', 'social_transport', '', 1),
(@step1_id, '社会组织-应急救援能力', 'LOAD_SOC_RESCUE', 11, '@LOAD_EVAL_RESULT:modelKey=socialOrganization,stepCode=indicator_assignment,field=large_loader_owning_rate', '{}', 'social_rescue', '', 1),
(@step1_id, '社会组织-科普宣传能力', 'LOAD_SOC_PUBLICITY', 12, '@LOAD_EVAL_RESULT:modelKey=socialOrganization,stepCode=indicator_assignment,field=disaster_insurance_claim_capacity', '{}', 'social_publicity', '', 1),

(@step1_id, '乡镇（区县单元）-灾害管理能力', 'LOAD_TOWN_MGMT', 13, '@LOAD_EVAL_RESULT:modelKey=townshipCountyUnit,stepCode=CAPABILITY_GRADE,field=management_capability_score', '{}', 'township_management', '', 1),
(@step1_id, '乡镇（区县单元）-灾害备灾能力', 'LOAD_TOWN_PREP', 14, '@LOAD_EVAL_RESULT:modelKey=townshipCountyUnit,stepCode=CAPABILITY_GRADE,field=support_capability_score', '{}', 'township_preparedness', '', 1),
(@step1_id, '乡镇（区县单元）-自救转移能力', 'LOAD_TOWN_SELF', 15, '@LOAD_EVAL_RESULT:modelKey=townshipCountyUnit,stepCode=CAPABILITY_GRADE,field=self_rescue_capability_score', '{}', 'township_self_rescue', '', 1),

(@step1_id, '社区（区县单元）-灾害管理能力', 'LOAD_COMM_MGMT', 16, '@LOAD_EVAL_RESULT:modelKey=communityCountyUnit,stepCode=CAPABILITY_GRADE,field=management_capability_score', '{}', 'community_management', '', 1),
(@step1_id, '社区（区县单元）-灾害备灾能力', 'LOAD_COMM_PREP', 17, '@LOAD_EVAL_RESULT:modelKey=communityCountyUnit,stepCode=CAPABILITY_GRADE,field=support_capability_score', '{}', 'community_preparedness', '', 1),
(@step1_id, '社区（区县单元）-自救转移能力', 'LOAD_COMM_SELF', 18, '@LOAD_EVAL_RESULT:modelKey=communityCountyUnit,stepCode=CAPABILITY_GRADE,field=self_rescue_capability_score', '{}', 'community_self_rescue', '', 1),

(@step1_id, '家庭-脆弱性', 'LOAD_FAM_VULN', 19, '@LOAD_EVAL_RESULT:modelKey=family,stepCode=score_and_grade,field=l1_vul_score', '{}', 'family_vulnerability', '', 1),
(@step1_id, '家庭-防灾物资储备能力', 'LOAD_FAM_MATERIAL', 20, '@LOAD_EVAL_RESULT:modelKey=family,stepCode=score_and_grade,field=l1_mat_score', '{}', 'family_material', '', 1),
(@step1_id, '家庭-灾害信息获取能力', 'LOAD_FAM_INFO', 21, '@LOAD_EVAL_RESULT:modelKey=family,stepCode=score_and_grade,field=l1_info_score', '{}', 'family_information', '', 1),
(@step1_id, '家庭-灾害自救互救能力', 'LOAD_FAM_SELF', 22, '@LOAD_EVAL_RESULT:modelKey=family,stepCode=score_and_grade,field=l1_self_score', '{}', 'family_self_rescue', '', 1);

-- 5) Step2: 归一化
INSERT INTO step_algorithm (step_id, algorithm_name, algorithm_code, algorithm_order, ql_expression, input_params, output_param, description, status) VALUES
(@step2_id, '政府-管理能力归一化', 'NORM_GOV_MGMT', 1, '@NORMALIZE:gov_management', '{}', 'gov_management_norm', '', 1),
(@step2_id, '政府-工程设防能力归一化', 'NORM_GOV_ENG', 2, '@NORMALIZE:gov_engineering', '{}', 'gov_engineering_norm', '', 1),
(@step2_id, '政府-监测预警能力归一化', 'NORM_GOV_MONITOR', 3, '@NORMALIZE:gov_monitoring', '{}', 'gov_monitoring_norm', '', 1),
(@step2_id, '政府-物资储备能力归一化', 'NORM_GOV_MATERIAL', 4, '@NORMALIZE:gov_material', '{}', 'gov_material_norm', '', 1),
(@step2_id, '政府-专业队伍救援能力归一化', 'NORM_GOV_TEAM', 5, '@NORMALIZE:gov_rescue_team', '{}', 'gov_rescue_team_norm', '', 1),
(@step2_id, '政府-转移安置能力归一化', 'NORM_GOV_TRANSFER', 6, '@NORMALIZE:gov_relocation', '{}', 'gov_relocation_norm', '', 1),
(@step2_id, '企业-工程救援归一化', 'NORM_ENT_ENG', 7, '@NORMALIZE:enterprise_engineering', '{}', 'enterprise_engineering_norm', '', 1),
(@step2_id, '企业-保险归一化', 'NORM_ENT_INSURANCE', 8, '@NORMALIZE:enterprise_insurance', '{}', 'enterprise_insurance_norm', '', 1),
(@step2_id, '社会组织-物资归一化', 'NORM_SOC_MATERIAL', 9, '@NORMALIZE:social_material', '{}', 'social_material_norm', '', 1),
(@step2_id, '社会组织-运输归一化', 'NORM_SOC_TRANSPORT', 10, '@NORMALIZE:social_transport', '{}', 'social_transport_norm', '', 1),
(@step2_id, '社会组织-救援归一化', 'NORM_SOC_RESCUE', 11, '@NORMALIZE:social_rescue', '{}', 'social_rescue_norm', '', 1),
(@step2_id, '社会组织-科普归一化', 'NORM_SOC_PUBLICITY', 12, '@NORMALIZE:social_publicity', '{}', 'social_publicity_norm', '', 1),
(@step2_id, '乡镇-管理归一化', 'NORM_TOWN_MGMT', 13, '@NORMALIZE:township_management', '{}', 'township_management_norm', '', 1),
(@step2_id, '乡镇-备灾归一化', 'NORM_TOWN_PREP', 14, '@NORMALIZE:township_preparedness', '{}', 'township_preparedness_norm', '', 1),
(@step2_id, '乡镇-自救归一化', 'NORM_TOWN_SELF', 15, '@NORMALIZE:township_self_rescue', '{}', 'township_self_rescue_norm', '', 1),
(@step2_id, '社区-管理归一化', 'NORM_COMM_MGMT', 16, '@NORMALIZE:community_management', '{}', 'community_management_norm', '', 1),
(@step2_id, '社区-备灾归一化', 'NORM_COMM_PREP', 17, '@NORMALIZE:community_preparedness', '{}', 'community_preparedness_norm', '', 1),
(@step2_id, '社区-自救归一化', 'NORM_COMM_SELF', 18, '@NORMALIZE:community_self_rescue', '{}', 'community_self_rescue_norm', '', 1),
(@step2_id, '家庭-脆弱性归一化', 'NORM_FAM_VULN', 19, '@NORMALIZE:family_vulnerability', '{}', 'family_vulnerability_norm', '', 1),
(@step2_id, '家庭-物资归一化', 'NORM_FAM_MATERIAL', 20, '@NORMALIZE:family_material', '{}', 'family_material_norm', '', 1),
(@step2_id, '家庭-信息归一化', 'NORM_FAM_INFO', 21, '@NORMALIZE:family_information', '{}', 'family_information_norm', '', 1),
(@step2_id, '家庭-自救归一化', 'NORM_FAM_SELF', 22, '@NORMALIZE:family_self_rescue', '{}', 'family_self_rescue_norm', '', 1);

-- 6) Step3: 固定权重加权（权重按样例Excel）
INSERT INTO step_algorithm (step_id, algorithm_name, algorithm_code, algorithm_order, ql_expression, input_params, output_param, description, status) VALUES
(@step3_id, 'W1', 'WEIGHT_01', 1, 'gov_management_norm * 0.034', '{}', 'w_gov_management', '', 1),
(@step3_id, 'W2', 'WEIGHT_02', 2, 'gov_engineering_norm * 0.032', '{}', 'w_gov_engineering', '', 1),
(@step3_id, 'W3', 'WEIGHT_03', 3, 'gov_monitoring_norm * 0.034', '{}', 'w_gov_monitoring', '', 1),
(@step3_id, 'W4', 'WEIGHT_04', 4, 'gov_material_norm * 0.034', '{}', 'w_gov_material', '', 1),
(@step3_id, 'W5', 'WEIGHT_05', 5, 'gov_rescue_team_norm * 0.032', '{}', 'w_gov_rescue_team', '', 1),
(@step3_id, 'W6', 'WEIGHT_06', 6, 'gov_relocation_norm * 0.034', '{}', 'w_gov_relocation', '', 1),
(@step3_id, 'W7', 'WEIGHT_07', 7, 'enterprise_engineering_norm * 0.0884', '{}', 'w_enterprise_engineering', '', 1),
(@step3_id, 'W8', 'WEIGHT_08', 8, 'enterprise_insurance_norm * 0.0816', '{}', 'w_enterprise_insurance', '', 1),
(@step3_id, 'W9', 'WEIGHT_09', 9, 'social_material_norm * 0.036', '{}', 'w_social_material', '', 1),
(@step3_id, 'W10', 'WEIGHT_10', 10, 'social_transport_norm * 0.0375', '{}', 'w_social_transport', '', 1),
(@step3_id, 'W11', 'WEIGHT_11', 11, 'social_rescue_norm * 0.039', '{}', 'w_social_rescue', '', 1),
(@step3_id, 'W12', 'WEIGHT_12', 12, 'social_publicity_norm * 0.0375', '{}', 'w_social_publicity', '', 1),
(@step3_id, 'W13', 'WEIGHT_13', 13, 'township_management_norm * 0.0612', '{}', 'w_township_management', '', 1),
(@step3_id, 'W14', 'WEIGHT_14', 14, 'township_preparedness_norm * 0.0576', '{}', 'w_township_preparedness', '', 1),
(@step3_id, 'W15', 'WEIGHT_15', 15, 'township_self_rescue_norm * 0.0612', '{}', 'w_township_self_rescue', '', 1),
(@step3_id, 'W16', 'WEIGHT_16', 16, 'community_management_norm * 0.0528', '{}', 'w_community_management', '', 1),
(@step3_id, 'W17', 'WEIGHT_17', 17, 'community_preparedness_norm * 0.0512', '{}', 'w_community_preparedness', '', 1),
(@step3_id, 'W18', 'WEIGHT_18', 18, 'community_self_rescue_norm * 0.056', '{}', 'w_community_self_rescue', '', 1),
(@step3_id, 'W19', 'WEIGHT_19', 19, 'family_vulnerability_norm * 0.0322', '{}', 'w_family_vulnerability', '', 1),
(@step3_id, 'W20', 'WEIGHT_20', 20, 'family_material_norm * 0.0322', '{}', 'w_family_material', '', 1),
(@step3_id, 'W21', 'WEIGHT_21', 21, 'family_information_norm * 0.0392', '{}', 'w_family_information', '', 1),
(@step3_id, 'W22', 'WEIGHT_22', 22, 'family_self_rescue_norm * 0.0364', '{}', 'w_family_self_rescue', '', 1);

-- 7) Step4: D+ / D-
INSERT INTO step_algorithm (step_id, algorithm_name, algorithm_code, algorithm_order, ql_expression, input_params, output_param, description, status) VALUES
(@step4_id, 'D+', 'TOPSIS_D_PLUS', 1,
 '@TOPSIS_POSITIVE:w_gov_management,w_gov_engineering,w_gov_monitoring,w_gov_material,w_gov_rescue_team,w_gov_relocation,w_enterprise_engineering,w_enterprise_insurance,w_social_material,w_social_transport,w_social_rescue,w_social_publicity,w_township_management,w_township_preparedness,w_township_self_rescue,w_community_management,w_community_preparedness,w_community_self_rescue,w_family_vulnerability,w_family_material,w_family_information,w_family_self_rescue',
 '{}', 'topsis_d_plus', '', 1),
(@step4_id, 'D-', 'TOPSIS_D_MINUS', 2,
 '@TOPSIS_NEGATIVE:w_gov_management,w_gov_engineering,w_gov_monitoring,w_gov_material,w_gov_rescue_team,w_gov_relocation,w_enterprise_engineering,w_enterprise_insurance,w_social_material,w_social_transport,w_social_rescue,w_social_publicity,w_township_management,w_township_preparedness,w_township_self_rescue,w_community_management,w_community_preparedness,w_community_self_rescue,w_family_vulnerability,w_family_material,w_family_information,w_family_self_rescue',
 '{}', 'topsis_d_minus', '', 1);

-- 8) Step5: Ci + 等级（合并）
INSERT INTO step_algorithm (step_id, algorithm_name, algorithm_code, algorithm_order, ql_expression, input_params, output_param, description, status) VALUES
(@step5_id, '综合减灾能力值', 'TOPSIS_CI', 1, 'topsis_d_minus / (topsis_d_plus + topsis_d_minus)', '{}', 'comprehensive_capability_score', '', 1),
(@step5_id, '综合减灾能力等级', 'TOPSIS_GRADE', 2, '@GRADE:comprehensive_capability_score', '{}', 'comprehensive_capability_level', '', 1);
