-- ============================================================
-- 市州-社会组织减灾能力评估模型 (2020)
-- 纯数据库配置驱动，零新增业务代码
-- model_name 必须包含"社会组织减灾能力"以触发现有数据装载分支
-- ============================================================

-- 1. 新增评估模型
INSERT INTO evaluation_model (id, model_name, model_code, version, description, status)
VALUES (21, '市州-社会组织减灾能力评估', 'SOCIAL_ORGANIZATION_DISASTER_REDUCTION_2020', '1.0.0',
        '基于社会组织调查数据的减灾能力评估模型（2020口径）。指标：物资储备能力、应急运输能力、应急救援能力、科普宣传能力。等权0.25，TOPSIS综合评分。', 1);

-- 2. 新增模型步骤（6步，与企业模型结构对齐）
INSERT INTO model_step (model_id, step_name, step_code, step_order, step_type, description, status) VALUES
(21, '评估指标赋值',       'indicator_assignment',              1, 'CALCULATION',   '从基础数据派生4项社会组织减灾能力指标', 1),
(21, '属性向量归一化',     'attribute_vector_normalization',    2, 'NORMALIZATION', '对4项指标进行向量归一化', 1),
(21, '等权加权',           'equal_weighting',                   3, 'WEIGHTING',     '等权0.25加权', 1),
(21, 'TOPSIS距离计算',     'distance_ideal',                    4, 'TOPSIS',        '计算D+和D-', 1),
(21, '综合能力得分',       'score_calculation',                 5, 'CALCULATION',   'TOPSIS得分计算', 1),
(21, '综合能力分级',       'score_grading',                     6, 'GRADING',       '得分分级', 1);

-- 3. 步骤算法配置
-- 获取刚才插入的 step id
SET @step1 = (SELECT id FROM model_step WHERE model_id=21 AND step_order=1);
SET @step2 = (SELECT id FROM model_step WHERE model_id=21 AND step_order=2);
SET @step3 = (SELECT id FROM model_step WHERE model_id=21 AND step_order=3);
SET @step4 = (SELECT id FROM model_step WHERE model_id=21 AND step_order=4);
SET @step5 = (SELECT id FROM model_step WHERE model_id=21 AND step_order=5);
SET @step6 = (SELECT id FROM model_step WHERE model_id=21 AND step_order=6);

-- Step 1: 指标赋值（从基础表字段派生）
INSERT INTO step_algorithm (step_id, algorithm_name, algorithm_code, algorithm_order, ql_expression, input_params, output_param, description, status) VALUES
(@step1, '物资储备能力',     'SO_INDICATOR_001', 1,
 '(population==0?0:emergency_equipment_material_value/population)',
 '{"required":["emergency_equipment_material_value","population"]}',
 'material_reserve_capacity',
 '物资储备能力 = 应急救援装备物资总价值 / 区域总人口', 1),

(@step1, '应急运输能力',     'SO_INDICATOR_002', 2,
 '(population==0?0:(passenger_vehicle_count+freight_vehicle_count)/population)',
 '{"required":["passenger_vehicle_count","freight_vehicle_count","population"]}',
 'emergency_transport_capacity',
 '应急运输能力 = (自有客车数+自有货运车辆数) / 区域总人口', 1),

(@step1, '应急救援能力',     'SO_INDICATOR_003', 3,
 '(population==0?0:special_operation_vehicle_count/population)',
 '{"required":["special_operation_vehicle_count","population"]}',
 'emergency_rescue_capacity',
 '应急救援能力 = 特种作业车辆数 / 区域总人口', 1),

(@step1, '科普宣传能力',     'SO_INDICATOR_004', 4,
 '(population==0?0:last_year_science_education_audience/population)',
 '{"required":["last_year_science_education_audience","population"]}',
 'science_publicity_capacity',
 '科普宣传能力 = 上一年度科普宣教受众人次 / 区域总人口', 1);

-- Step 2: 归一化
INSERT INTO step_algorithm (step_id, algorithm_name, algorithm_code, algorithm_order, ql_expression, input_params, output_param, description, status) VALUES
(@step2, '物资储备能力归一化',   'SO_NORMALIZE_001', 1, '@NORMALIZE:material_reserve_capacity',   '{}', 'material_reserve_capacity_norm',   '向量归一化', 1),
(@step2, '应急运输能力归一化',   'SO_NORMALIZE_002', 2, '@NORMALIZE:emergency_transport_capacity', '{}', 'emergency_transport_capacity_norm', '向量归一化', 1),
(@step2, '应急救援能力归一化',   'SO_NORMALIZE_003', 3, '@NORMALIZE:emergency_rescue_capacity',   '{}', 'emergency_rescue_capacity_norm',   '向量归一化', 1),
(@step2, '科普宣传能力归一化',   'SO_NORMALIZE_004', 4, '@NORMALIZE:science_publicity_capacity',  '{}', 'science_publicity_capacity_norm',  '向量归一化', 1);

-- Step 3: 等权加权 (各0.25)
INSERT INTO step_algorithm (step_id, algorithm_name, algorithm_code, algorithm_order, ql_expression, input_params, output_param, description, status) VALUES
(@step3, '加权物资储备能力',     'SO_WEIGHTED_001', 1, '(material_reserve_capacity_norm * 0.25)',   '{}', 'w_material_reserve',   '等权0.25', 1),
(@step3, '加权应急运输能力',     'SO_WEIGHTED_002', 2, '(emergency_transport_capacity_norm * 0.25)', '{}', 'w_emergency_transport', '等权0.25', 1),
(@step3, '加权应急救援能力',     'SO_WEIGHTED_003', 3, '(emergency_rescue_capacity_norm * 0.25)',   '{}', 'w_emergency_rescue',   '等权0.25', 1),
(@step3, '加权科普宣传能力',     'SO_WEIGHTED_004', 4, '(science_publicity_capacity_norm * 0.25)',  '{}', 'w_science_publicity',  '等权0.25', 1);

-- Step 4: TOPSIS D+ / D-
INSERT INTO step_algorithm (step_id, algorithm_name, algorithm_code, algorithm_order, ql_expression, input_params, output_param, description, status) VALUES
(@step4, '社会组织减灾能力D+',  'SO_D_PLUS_001',  1, '@TOPSIS_POSITIVE:w_material_reserve,w_emergency_transport,w_emergency_rescue,w_science_publicity',  '{}', 'social_organization_capacity_d_plus',  'TOPSIS正理想解距离', 1),
(@step4, '社会组织减灾能力D-',  'SO_D_MINUS_001', 2, '@TOPSIS_NEGATIVE:w_material_reserve,w_emergency_transport,w_emergency_rescue,w_science_publicity', '{}', 'social_organization_capacity_d_minus', 'TOPSIS负理想解距离', 1);

-- Step 5: TOPSIS得分
INSERT INTO step_algorithm (step_id, algorithm_name, algorithm_code, algorithm_order, ql_expression, input_params, output_param, description, status) VALUES
(@step5, '社会组织减灾能力得分', 'SO_SCORE_001', 1, '@TOPSIS_SCORE:social_organization_capacity_d_plus,social_organization_capacity_d_minus', '{}', 'social_organization_capability_score', 'TOPSIS综合得分', 1);

-- Step 6: 分级
INSERT INTO step_algorithm (step_id, algorithm_name, algorithm_code, algorithm_order, ql_expression, input_params, output_param, description, status) VALUES
(@step6, '社会组织减灾能力分级', 'SO_GRADE_001', 1, '@GRADE:social_organization_capability_score', '{}', 'social_organization_capability_level', '能力等级分级', 1);
