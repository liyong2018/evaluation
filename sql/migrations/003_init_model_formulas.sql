-- 初始化模型公式数据
-- 基于QLExpress表达式的评估算法配置

USE evaluate_db;

-- 获取默认模型ID和步骤ID
SET @model_id = (SELECT id FROM evaluation_model WHERE model_code = 'STANDARD_MODEL' LIMIT 1);
SET @step1_id = (SELECT id FROM model_step WHERE model_id = @model_id AND step_code = 'INDICATOR_ASSIGNMENT' LIMIT 1);
SET @step2_id = (SELECT id FROM model_step WHERE model_id = @model_id AND step_code = 'VECTOR_NORMALIZATION' LIMIT 1);
SET @step3_id = (SELECT id FROM model_step WHERE model_id = @model_id AND step_code = 'SECONDARY_WEIGHTING' LIMIT 1);

-- 步骤1: 评估指标赋值算法
INSERT INTO step_algorithm (step_id, algorithm_name, algorithm_code, algorithm_order, ql_expression, output_param, description, status) VALUES
(@step1_id, '队伍管理能力计算', 'MANAGEMENT_CAPABILITY', 1, '(management_staff / population) * 10000', 'management_capability', '队伍管理能力=(本级灾害管理工作人员总数/常住人口数量)*10000', 1),
(@step1_id, '风险评估能力计算', 'RISK_ASSESSMENT_CAPABILITY', 2, 'risk_assessment == "是" ? 1 : 0', 'risk_assessment_capability', '风险评估能力=IF(是否开展风险评估="是",1,0)', 1),
(@step1_id, '财政投入能力计算', 'FUNDING_CAPABILITY', 3, '(funding_amount / population) * 10000', 'funding_capability', '财政投入能力=(防灾减灾救灾资金投入总金额/常住人口数量)*10000', 1),
(@step1_id, '物资储备能力计算', 'MATERIAL_CAPABILITY', 4, '(material_value / population) * 10000', 'material_capability', '物资储备能力=(现有储备物资装备折合金额/常住人口数量)*10000', 1),
(@step1_id, '医疗保障能力计算', 'MEDICAL_CAPABILITY', 5, '(hospital_beds / population) * 10000', 'medical_capability', '医疗保障能力=(实有住院床位数/常住人口数量)*10000', 1),
(@step1_id, '自救互救能力计算', 'SELF_RESCUE_CAPABILITY', 6, '((firefighters + volunteers + militia_reserve) / population) * 10000', 'self_rescue_capability', '自救互救能力=((消防员+志愿者+民兵预备役)/常住人口数量)*10000', 1),
(@step1_id, '公众避险能力计算', 'PUBLIC_AVOIDANCE_CAPABILITY', 7, '(training_participants / population) * 10000', 'public_avoidance_capability', '公众避险能力=(应急管理培训参与人次/常住人口数量)*10000', 1),
(@step1_id, '转移安置能力计算', 'RELOCATION_CAPABILITY', 8, '(shelter_capacity / population) * 10000', 'relocation_capability', '转移安置能力=(应急避难场所容量/常住人口数量)*10000', 1);

-- 步骤2: 属性向量归一化算法
INSERT INTO step_algorithm (step_id, algorithm_name, algorithm_code, algorithm_order, ql_expression, output_param, description, status) VALUES
(@step2_id, '向量归一化通用公式', 'VECTOR_NORMALIZE', 1, 'value / SQRT(SUMSQ(all_values))', 'normalized_value', '归一化值=原始值/SQRT(SUMSQ(所有原始值))', 1);

-- 步骤3: 二级指标定权算法
INSERT INTO step_algorithm (step_id, algorithm_name, algorithm_code, algorithm_order, ql_expression, output_param, description, status) VALUES
(@step3_id, '二级指标定权公式', 'SECONDARY_WEIGHT', 1, 'normalized_value * weight', 'weighted_value', '定权值=归一化值*权重', 1);

SELECT 'Model formulas initialized successfully!' as message;