-- 更新步骤1：评估指标赋值
DELETE FROM step_algorithm WHERE step_id = 15;

INSERT INTO step_algorithm (step_id, algorithm_name, algorithm_code, algorithm_order, ql_expression, input_params, output_param, description, status, create_time) VALUES
(15, '队伍管理能力计算', 'TEAM_MANAGEMENT', 1, '(management_staff * 1.0 / population) * 10000', NULL, 'teamManagement', '计算队伍管理能力指标', 1, NOW()),
(15, '风险评估能力计算', 'RISK_ASSESSMENT', 2, 'risk_assessment != null && risk_assessment.contains("是") ? 1.0 : 0.0', NULL, 'riskAssessment', '计算风险评估能力指标', 1, NOW()),
(15, '财政投入能力计算', 'FINANCIAL_INPUT', 3, '(funding_amount * 1.0 / population) * 10000', NULL, 'financialInput', '计算财政投入能力指标', 1, NOW()),
(15, '物资储备能力计算', 'MATERIAL_RESERVE', 4, '(material_value * 1.0 / population) * 10000', NULL, 'materialReserve', '计算物资储备能力指标', 1, NOW()),
(15, '医疗保障能力计算', 'MEDICAL_SUPPORT', 5, '(hospital_beds * 1.0 / population) * 10000', NULL, 'medicalSupport', '计算医疗保障能力指标', 1, NOW()),
(15, '自救互救能力计算', 'SELF_RESCUE', 6, '((firefighters + volunteers + militia_reserve) * 1.0 / population) * 10000', NULL, 'selfRescue', '计算自救互救能力指标', 1, NOW()),
(15, '公众避险能力计算', 'PUBLIC_AVOIDANCE', 7, '(training_participants * 1.0 / population) * 100', NULL, 'publicAvoidance', '计算公众避险能力指标', 1, NOW()),
(15, '转移安置能力计算', 'RELOCATION_CAPACITY', 8, 'shelter_capacity * 1.0 / population', NULL, 'relocationCapacity', '计算转移安置能力指标', 1, NOW());

SELECT '步骤1配置完成' as status, COUNT(*) as algorithm_count FROM step_algorithm WHERE step_id = 15;