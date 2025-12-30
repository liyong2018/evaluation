-- 完整的标准减灾能力评估模型配置脚本
-- 基于TOPSIS方法的五步评估流程

USE evaluate_db;

-- 清理现有的步骤3的算法（保留步骤1和2的测试算法）
DELETE FROM step_algorithm WHERE step_id = 17;

-- 更新步骤信息
UPDATE model_step SET 
    step_name = '评估指标赋值',
    description = '根据原始数据计算8个二级指标的初始值'
WHERE id = 15;

UPDATE model_step SET 
    step_name = '属性向量归一化',
    description = '对每个指标进行向量归一化处理'
WHERE id = 16;

UPDATE model_step SET 
    step_name = '定权计算',
    description = '使用权重对归一化后的数据进行加权'
WHERE id = 17;

-- 删除并重新创建步骤1的所有算法
DELETE FROM step_algorithm WHERE step_id = 15;

-- 步骤1：评估指标赋值 (8个算法)
INSERT INTO step_algorithm (step_id, algorithm_name, algorithm_code, algorithm_order, ql_expression, input_params, output_param, description, status, create_time) VALUES
(15, '队伍管理能力计算', 'TEAM_MANAGEMENT', 1, '(management_staff * 1.0 / population) * 10000', NULL, 'teamManagement', '计算队伍管理能力指标', 1, NOW()),
(15, '风险评估能力计算', 'RISK_ASSESSMENT', 2, 'risk_assessment != null && risk_assessment.contains("是") ? 1.0 : 0.0', NULL, 'riskAssessment', '计算风险评估能力指标', 1, NOW()),
(15, '财政投入能力计算', 'FINANCIAL_INPUT', 3, '(funding_amount * 1.0 / population) * 10000', NULL, 'financialInput', '计算财政投入能力指标', 1, NOW()),
(15, '物资储备能力计算', 'MATERIAL_RESERVE', 4, '(material_value * 1.0 / population) * 10000', NULL, 'materialReserve', '计算物资储备能力指标', 1, NOW()),
(15, '医疗保障能力计算', 'MEDICAL_SUPPORT', 5, '(hospital_beds * 1.0 / population) * 10000', NULL, 'medicalSupport', '计算医疗保障能力指标', 1, NOW()),
(15, '自救互救能力计算', 'SELF_RESCUE', 6, '((firefighters + volunteers + militia_reserve) * 1.0 / population) * 10000', NULL, 'selfRescue', '计算自救互救能力指标', 1, NOW()),
(15, '公众避险能力计算', 'PUBLIC_AVOIDANCE', 7, '(training_participants * 1.0 / population) * 100', NULL, 'publicAvoidance', '计算公众避险能力指标', 1, NOW()),
(15, '转移安置能力计算', 'RELOCATION_CAPACITY', 8, 'shelter_capacity * 1.0 / population', NULL, 'relocationCapacity', '计算转移安置能力指标', 1, NOW());

-- 步骤2：属性向量归一化 (8个算法)
-- 注意：这里使用特殊标记 @NORMALIZE，表示需要聚合计算
DELETE FROM step_algorithm WHERE step_id = 16;

INSERT INTO step_algorithm (step_id, algorithm_name, algorithm_code, algorithm_order, ql_expression, input_params, output_param, description, status, create_time) VALUES
(16, '队伍管理能力归一化', 'TEAM_MANAGEMENT_NORM', 1, '@NORMALIZE:teamManagement', NULL, 'teamManagementNorm', '对队伍管理能力进行向量归一化', 1, NOW()),
(16, '风险评估能力归一化', 'RISK_ASSESSMENT_NORM', 2, '@NORMALIZE:riskAssessment', NULL, 'riskAssessmentNorm', '对风险评估能力进行向量归一化', 1, NOW()),
(16, '财政投入能力归一化', 'FINANCIAL_INPUT_NORM', 3, '@NORMALIZE:financialInput', NULL, 'financialInputNorm', '对财政投入能力进行向量归一化', 1, NOW()),
(16, '物资储备能力归一化', 'MATERIAL_RESERVE_NORM', 4, '@NORMALIZE:materialReserve', NULL, 'materialReserveNorm', '对物资储备能力进行向量归一化', 1, NOW()),
(16, '医疗保障能力归一化', 'MEDICAL_SUPPORT_NORM', 5, '@NORMALIZE:medicalSupport', NULL, 'medicalSupportNorm', '对医疗保障能力进行向量归一化', 1, NOW()),
(16, '自救互救能力归一化', 'SELF_RESCUE_NORM', 6, '@NORMALIZE:selfRescue', NULL, 'selfRescueNorm', '对自救互救能力进行向量归一化', 1, NOW()),
(16, '公众避险能力归一化', 'PUBLIC_AVOIDANCE_NORM', 7, '@NORMALIZE:publicAvoidance', NULL, 'publicAvoidanceNorm', '对公众避险能力进行向量归一化', 1, NOW()),
(16, '转移安置能力归一化', 'RELOCATION_CAPACITY_NORM', 8, '@NORMALIZE:relocationCapacity', NULL, 'relocationCapacityNorm', '对转移安置能力进行向量归一化', 1, NOW());

-- 步骤3：定权计算
-- 分为两个子步骤：一级指标定权 和 乡镇街道减灾能力定权
INSERT INTO step_algorithm (step_id, algorithm_name, algorithm_code, algorithm_order, ql_expression, input_params, output_param, description, status, create_time) VALUES
-- 一级指标定权（使用二级权重）
(17, '队伍管理能力定权', 'TEAM_MANAGEMENT_WEIGHT', 1, 'teamManagementNorm * weight_TEAM_MANAGEMENT', NULL, 'teamManagementWeighted', '使用二级权重对队伍管理能力定权', 1, NOW()),
(17, '风险评估能力定权', 'RISK_ASSESSMENT_WEIGHT', 2, 'riskAssessmentNorm * weight_RISK_ASSESSMENT', NULL, 'riskAssessmentWeighted', '使用二级权重对风险评估能力定权', 1, NOW()),
(17, '财政投入能力定权', 'FINANCIAL_INPUT_WEIGHT', 3, 'financialInputNorm * weight_FINANCIAL_INPUT', NULL, 'financialInputWeighted', '使用二级权重对财政投入能力定权', 1, NOW()),
(17, '物资储备能力定权', 'MATERIAL_RESERVE_WEIGHT', 4, 'materialReserveNorm * weight_MATERIAL_RESERVE', NULL, 'materialReserveWeighted', '使用二级权重对物资储备能力定权', 1, NOW()),
(17, '医疗保障能力定权', 'MEDICAL_SUPPORT_WEIGHT', 5, 'medicalSupportNorm * weight_MEDICAL_SUPPORT', NULL, 'medicalSupportWeighted', '使用二级权重对医疗保障能力定权', 1, NOW()),
(17, '自救互救能力定权', 'SELF_RESCUE_WEIGHT', 6, 'selfRescueNorm * weight_SELF_RESCUE', NULL, 'selfRescueWeighted', '使用二级权重对自救互救能力定权', 1, NOW()),
(17, '公众避险能力定权', 'PUBLIC_AVOIDANCE_WEIGHT', 7, 'publicAvoidanceNorm * weight_PUBLIC_AVOIDANCE', NULL, 'publicAvoidanceWeighted', '使用二级权重对公众避险能力定权', 1, NOW()),
(17, '转移安置能力定权', 'RELOCATION_CAPACITY_WEIGHT', 8, 'relocationCapacityNorm * weight_RELOCATION_CAPACITY', NULL, 'relocationCapacityWeighted', '使用二级权重对转移安置能力定权', 1, NOW()),

-- 乡镇街道减灾能力定权（使用一级权重 * 二级权重）
(17, '队伍管理能力综合定权', 'TEAM_MANAGEMENT_TOTAL', 9, 'teamManagementNorm * weight_DISASTER_MANAGEMENT * weight_TEAM_MANAGEMENT', NULL, 'teamManagementTotal', '使用一级和二级权重计算队伍管理能力综合定权', 1, NOW()),
(17, '风险评估能力综合定权', 'RISK_ASSESSMENT_TOTAL', 10, 'riskAssessmentNorm * weight_DISASTER_MANAGEMENT * weight_RISK_ASSESSMENT', NULL, 'riskAssessmentTotal', '使用一级和二级权重计算风险评估能力综合定权', 1, NOW()),
(17, '财政投入能力综合定权', 'FINANCIAL_INPUT_TOTAL', 11, 'financialInputNorm * weight_DISASTER_MANAGEMENT * weight_FINANCIAL_INPUT', NULL, 'financialInputTotal', '使用一级和二级权重计算财政投入能力综合定权', 1, NOW()),
(17, '物资储备能力综合定权', 'MATERIAL_RESERVE_TOTAL', 12, 'materialReserveNorm * weight_DISASTER_PREPAREDNESS * weight_MATERIAL_RESERVE', NULL, 'materialReserveTotal', '使用一级和二级权重计算物资储备能力综合定权', 1, NOW()),
(17, '医疗保障能力综合定权', 'MEDICAL_SUPPORT_TOTAL', 13, 'medicalSupportNorm * weight_DISASTER_PREPAREDNESS * weight_MEDICAL_SUPPORT', NULL, 'medicalSupportTotal', '使用一级和二级权重计算医疗保障能力综合定权', 1, NOW()),
(17, '自救互救能力综合定权', 'SELF_RESCUE_TOTAL', 14, 'selfRescueNorm * weight_SELF_RESCUE_TRANSFER * weight_SELF_RESCUE', NULL, 'selfRescueTotal', '使用一级和二级权重计算自救互救能力综合定权', 1, NOW()),
(17, '公众避险能力综合定权', 'PUBLIC_AVOIDANCE_TOTAL', 15, 'publicAvoidanceNorm * weight_SELF_RESCUE_TRANSFER * weight_PUBLIC_AVOIDANCE', NULL, 'publicAvoidanceTotal', '使用一级和二级权重计算公众避险能力综合定权', 1, NOW()),
(17, '转移安置能力综合定权', 'RELOCATION_CAPACITY_TOTAL', 16, 'relocationCapacityNorm * weight_SELF_RESCUE_TRANSFER * weight_RELOCATION_CAPACITY', NULL, 'relocationCapacityTotal', '使用一级和二级权重计算转移安置能力综合定权', 1, NOW());

-- 添加步骤4和步骤5（如果不存在）
INSERT INTO model_step (model_id, step_name, step_code, step_order, step_type, description, status, create_time)
SELECT 3, '优劣解计算', 'TOPSIS_DISTANCE', 4, 'TOPSIS', 'TOPSIS方法：计算到正理想解和负理想解的距离', 1, NOW()
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM model_step WHERE model_id = 3 AND step_order = 4);

INSERT INTO model_step (model_id, step_name, step_code, step_order, step_type, description, status, create_time)
SELECT 3, '能力值计算与分级', 'CAPABILITY_GRADE', 5, 'GRADING', '计算能力值并进行五级分类', 1, NOW()
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM model_step WHERE model_id = 3 AND step_order = 5);

-- 获取步骤4和5的ID（需要手动查询后使用）
-- 假设步骤4的ID是18，步骤5的ID是19

-- 步骤4：优劣解计算
-- 使用特殊标记 @TOPSIS_POSITIVE 和 @TOPSIS_NEGATIVE
SET @step4_id = (SELECT id FROM model_step WHERE model_id = 3 AND step_order = 4 LIMIT 1);

DELETE FROM step_algorithm WHERE step_id = @step4_id;

INSERT INTO step_algorithm (step_id, algorithm_name, algorithm_code, algorithm_order, ql_expression, input_params, output_param, description, status, create_time) VALUES
-- 一级指标优劣解
(@step4_id, '灾害管理能力正理想解距离', 'DISASTER_MGMT_POSITIVE', 1, '@TOPSIS_POSITIVE:teamManagementWeighted,riskAssessmentWeighted,financialInputWeighted', NULL, 'disasterMgmtPositive', '计算灾害管理能力到正理想解的距离', 1, NOW()),
(@step4_id, '灾害管理能力负理想解距离', 'DISASTER_MGMT_NEGATIVE', 2, '@TOPSIS_NEGATIVE:teamManagementWeighted,riskAssessmentWeighted,financialInputWeighted', NULL, 'disasterMgmtNegative', '计算灾害管理能力到负理想解的距离', 1, NOW()),
(@step4_id, '灾害备灾能力正理想解距离', 'DISASTER_PREP_POSITIVE', 3, '@TOPSIS_POSITIVE:materialReserveWeighted,medicalSupportWeighted', NULL, 'disasterPrepPositive', '计算灾害备灾能力到正理想解的距离', 1, NOW()),
(@step4_id, '灾害备灾能力负理想解距离', 'DISASTER_PREP_NEGATIVE', 4, '@TOPSIS_NEGATIVE:materialReserveWeighted,medicalSupportWeighted', NULL, 'disasterPrepNegative', '计算灾害备灾能力到负理想解的距离', 1, NOW()),
(@step4_id, '自救转移能力正理想解距离', 'SELF_RESCUE_POSITIVE', 5, '@TOPSIS_POSITIVE:selfRescueWeighted,publicAvoidanceWeighted,relocationCapacityWeighted', NULL, 'selfRescuePositive', '计算自救转移能力到正理想解的距离', 1, NOW()),
(@step4_id, '自救转移能力负理想解距离', 'SELF_RESCUE_NEGATIVE', 6, '@TOPSIS_NEGATIVE:selfRescueWeighted,publicAvoidanceWeighted,relocationCapacityWeighted', NULL, 'selfRescueNegative', '计算自救转移能力到负理想解的距离', 1, NOW()),

-- 综合能力优劣解
(@step4_id, '综合能力正理想解距离', 'TOTAL_POSITIVE', 7, '@TOPSIS_POSITIVE:teamManagementTotal,riskAssessmentTotal,financialInputTotal,materialReserveTotal,medicalSupportTotal,selfRescueTotal,publicAvoidanceTotal,relocationCapacityTotal', NULL, 'totalPositive', '计算综合减灾能力到正理想解的距离', 1, NOW()),
(@step4_id, '综合能力负理想解距离', 'TOTAL_NEGATIVE', 8, '@TOPSIS_NEGATIVE:teamManagementTotal,riskAssessmentTotal,financialInputTotal,materialReserveTotal,medicalSupportTotal,selfRescueTotal,publicAvoidanceTotal,relocationCapacityTotal', NULL, 'totalNegative', '计算综合减灾能力到负理想解的距离', 1, NOW());

-- 步骤5：能力值计算与分级
SET @step5_id = (SELECT id FROM model_step WHERE model_id = 3 AND step_order = 5 LIMIT 1);

DELETE FROM step_algorithm WHERE step_id = @step5_id;

INSERT INTO step_algorithm (step_id, algorithm_name, algorithm_code, algorithm_order, ql_expression, input_params, output_param, description, status, create_time) VALUES
-- 能力值计算（贴近度）
(@step5_id, '灾害管理能力值', 'DISASTER_MGMT_SCORE', 1, 'disasterMgmtNegative / (disasterMgmtNegative + disasterMgmtPositive)', NULL, 'disasterMgmtScore', '计算灾害管理能力值', 1, NOW()),
(@step5_id, '灾害备灾能力值', 'DISASTER_PREP_SCORE', 2, 'disasterPrepNegative / (disasterPrepNegative + disasterPrepPositive)', NULL, 'disasterPrepScore', '计算灾害备灾能力值', 1, NOW()),
(@step5_id, '自救转移能力值', 'SELF_RESCUE_SCORE', 3, 'selfRescueNegative / (selfRescueNegative + selfRescuePositive)', NULL, 'selfRescueScore', '计算自救转移能力值', 1, NOW()),
(@step5_id, '综合能力值', 'TOTAL_SCORE', 4, 'totalNegative / (totalNegative + totalPositive)', NULL, 'totalScore', '计算综合减灾能力值', 1, NOW()),

-- 能力分级（使用特殊标记 @GRADE）
(@step5_id, '灾害管理能力分级', 'DISASTER_MGMT_GRADE', 5, '@GRADE:disasterMgmtScore', NULL, 'disasterMgmtGrade', '对灾害管理能力进行分级', 1, NOW()),
(@step5_id, '灾害备灾能力分级', 'DISASTER_PREP_GRADE', 6, '@GRADE:disasterPrepScore', NULL, 'disasterPrepGrade', '对灾害备灾能力进行分级', 1, NOW()),
(@step5_id, '自救转移能力分级', 'SELF_RESCUE_GRADE', 7, '@GRADE:selfRescueScore', NULL, 'selfRescueGrade', '对自救转移能力进行分级', 1, NOW()),
(@step5_id, '综合能力分级', 'TOTAL_GRADE', 8, '@GRADE:totalScore', NULL, 'totalGrade', '对综合减灾能力进行分级', 1, NOW());

-- 验证配置
SELECT 
    ms.step_order,
    ms.step_name,
    ms.step_code,
    COUNT(sa.id) as algorithm_count
FROM model_step ms
LEFT JOIN step_algorithm sa ON ms.id = sa.step_id
WHERE ms.model_id = 3
GROUP BY ms.id, ms.step_order, ms.step_name, ms.step_code
ORDER BY ms.step_order;
