-- 修复中文乱码问题
USE evaluate_db;

-- 1. 清空现有数据
DELETE FROM step_algorithm;
DELETE FROM model_step;
DELETE FROM evaluation_model;

-- 2. 重新插入默认评估模型（使用正确的UTF-8编码）
INSERT INTO evaluation_model (model_name, model_code, description, version, status, is_default) VALUES
('标准减灾能力评估模型', 'STANDARD_MODEL', '基于TOPSIS算法的标准减灾能力评估模型，包含评估指标赋值、属性向量归一化、定权、优劣解算法、分级等步骤', '1.0', 1, 1);

-- 获取插入的模型ID
SET @model_id = LAST_INSERT_ID();

-- 3. 重新插入模型步骤
INSERT INTO model_step (model_id, step_name, step_code, step_order, step_type, description) VALUES
(@model_id, '评估指标赋值', 'INDICATOR_ASSIGNMENT', 1, 'CALCULATION', '根据调查数据计算8个二级指标的原始值'),
(@model_id, '属性向量归一化', 'VECTOR_NORMALIZATION', 2, 'NORMALIZATION', '对二级指标进行向量归一化处理'),
(@model_id, '二级指标定权', 'SECONDARY_WEIGHTING', 3, 'WEIGHTING', '将归一化值与二级指标权重相乘'),
(@model_id, '一级指标定权', 'PRIMARY_WEIGHTING', 4, 'WEIGHTING', '计算一级指标的定权值'),
(@model_id, '优劣解算法计算', 'TOPSIS_CALCULATION', 5, 'TOPSIS', '基于TOPSIS优劣解算法计算距离'),
(@model_id, '能力值计算', 'CAPABILITY_CALCULATION', 6, 'CALCULATION', '计算最终能力值'),
(@model_id, '能力分级计算', 'GRADING_CALCULATION', 7, 'GRADING', '根据均值和标准差计算能力分级');

-- 4. 重新插入算法公式
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

SELECT 'Chinese encoding fixed successfully!' as message;
