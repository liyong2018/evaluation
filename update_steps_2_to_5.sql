-- 更新标准减灾能力评估模型 - 步骤2到步骤5
-- 包含：属性向量归一化、定权计算、优劣解算法计算、能力值计算与分级
-- 编码：UTF-8
-- 创建时间：2025-10-12

USE evaluate_db;

SET NAMES utf8mb4;
SET CHARACTER SET utf8mb4;

-- ============================================================================
-- 步骤2：属性向量归一化
-- ============================================================================
-- 获取步骤2的ID
SET @step2_id = (SELECT id FROM model_step WHERE model_id = (SELECT id FROM evaluation_model WHERE model_code = 'STANDARD_MODEL' LIMIT 1) AND step_order = 2 LIMIT 1);

-- 清理现有数据
DELETE FROM step_algorithm WHERE step_id = @step2_id;

-- 插入归一化算法（8个二级指标）
-- 归一化公式：本乡镇指标值 / SQRT(SUMSQ(全部乡镇指标值))
-- 使用特殊标记 @NORMALIZE 表示需要聚合所有区域的数据进行计算
INSERT INTO step_algorithm (step_id, algorithm_name, algorithm_code, algorithm_order, ql_expression, input_params, output_param, description, status, create_time) VALUES
(@step2_id, '队伍管理能力归一化', 'TEAM_MANAGEMENT_NORM', 1, '@NORMALIZE:teamManagement', NULL, 'teamManagementNorm', '队伍管理能力（属性向量归一化）=本乡镇队伍管理能力/SQRT(SUMSQ(全部乡镇队伍管理能力))', 1, NOW()),
(@step2_id, '风险评估能力归一化', 'RISK_ASSESSMENT_NORM', 2, '@NORMALIZE:riskAssessment', NULL, 'riskAssessmentNorm', '风险评估能力（属性向量归一化）=本乡镇风险评估能力/SQRT(SUMSQ(全部乡镇风险评估能力))', 1, NOW()),
(@step2_id, '财政投入能力归一化', 'FINANCIAL_INPUT_NORM', 3, '@NORMALIZE:financialInput', NULL, 'financialInputNorm', '财政投入能力（属性向量归一化）=本乡镇财政投入能力/SQRT(SUMSQ(全部乡镇财政投入能力))', 1, NOW()),
(@step2_id, '物资储备能力归一化', 'MATERIAL_RESERVE_NORM', 4, '@NORMALIZE:materialReserve', NULL, 'materialReserveNorm', '物资储备能力（属性向量归一化）=本乡镇物资储备能力/SQRT(SUMSQ(全部乡镇物资储备能力))', 1, NOW()),
(@step2_id, '医疗保障能力归一化', 'MEDICAL_SUPPORT_NORM', 5, '@NORMALIZE:medicalSupport', NULL, 'medicalSupportNorm', '医疗保障能力（属性向量归一化）=本乡镇医疗保障能力/SQRT(SUMSQ(全部乡镇医疗保障能力))', 1, NOW()),
(@step2_id, '自救互救能力归一化', 'SELF_RESCUE_NORM', 6, '@NORMALIZE:selfRescue', NULL, 'selfRescueNorm', '自救互救能力（属性向量归一化）=本乡镇自救互救能力/SQRT(SUMSQ(全部乡镇自救互救能力))', 1, NOW()),
(@step2_id, '公众避险能力归一化', 'PUBLIC_AVOIDANCE_NORM', 7, '@NORMALIZE:publicAvoidance', NULL, 'publicAvoidanceNorm', '公众避险能力（属性向量归一化）=本乡镇公众避险能力/SQRT(SUMSQ(全部乡镇公众避险能力))', 1, NOW()),
(@step2_id, '转移安置能力归一化', 'RELOCATION_CAPACITY_NORM', 8, '@NORMALIZE:relocationCapacity', NULL, 'relocationCapacityNorm', '转移安置能力（属性向量归一化）=本乡镇转移安置能力/SQRT(SUMSQ(全部乡镇转移安置能力))', 1, NOW());

SELECT CONCAT('步骤2配置完成，共插入 ', COUNT(*), ' 条算法记录') as result FROM step_algorithm WHERE step_id = @step2_id;

-- ============================================================================
-- 步骤3：定权计算
-- ============================================================================
-- 获取步骤3的ID
SET @step3_id = (SELECT id FROM model_step WHERE model_id = (SELECT id FROM evaluation_model WHERE model_code = 'STANDARD_MODEL' LIMIT 1) AND step_order = 3 LIMIT 1);

-- 清理现有数据
DELETE FROM step_algorithm WHERE step_id = @step3_id;

-- 插入定权算法
-- 分为两部分：
-- 1. 一级指标定权（使用二级权重）
-- 2. 乡镇街道减灾能力定权（使用一级权重 * 二级权重）

-- 第一部分：一级指标定权（1-8号算法）
-- 公式：归一化值 * 二级权重
INSERT INTO step_algorithm (step_id, algorithm_name, algorithm_code, algorithm_order, ql_expression, input_params, output_param, description, status, create_time) VALUES
(@step3_id, '队伍管理能力定权', 'TEAM_MANAGEMENT_WEIGHT', 1, 'teamManagementNorm * weight_TEAM_MANAGEMENT', NULL, 'teamManagementWeighted', '队伍管理能力（定权）=队伍管理能力（属性向量归一化）*队伍管理能力二级权重指标', 1, NOW()),
(@step3_id, '风险评估能力定权', 'RISK_ASSESSMENT_WEIGHT', 2, 'riskAssessmentNorm * weight_RISK_ASSESSMENT', NULL, 'riskAssessmentWeighted', '风险评估能力（定权）=风险评估能力（属性向量归一化）*风险评估能力二级权重指标', 1, NOW()),
(@step3_id, '财政投入能力定权', 'FINANCIAL_INPUT_WEIGHT', 3, 'financialInputNorm * weight_FINANCIAL_INPUT', NULL, 'financialInputWeighted', '财政投入能力（定权）=财政投入能力（属性向量归一化）*财政投入能力二级权重指标', 1, NOW()),
(@step3_id, '物资储备能力定权', 'MATERIAL_RESERVE_WEIGHT', 4, 'materialReserveNorm * weight_MATERIAL_RESERVE', NULL, 'materialReserveWeighted', '物资储备能力（定权）=物资储备能力（属性向量归一化）*物资储备能力二级权重指标', 1, NOW()),
(@step3_id, '医疗保障能力定权', 'MEDICAL_SUPPORT_WEIGHT', 5, 'medicalSupportNorm * weight_MEDICAL_SUPPORT', NULL, 'medicalSupportWeighted', '医疗保障能力（定权）=医疗保障能力（属性向量归一化）*医疗保障能力二级权重指标', 1, NOW()),
(@step3_id, '自救互救能力定权', 'SELF_RESCUE_WEIGHT', 6, 'selfRescueNorm * weight_SELF_RESCUE', NULL, 'selfRescueWeighted', '自救互救能力（定权）=自救互救能力（属性向量归一化）*自救互救能力二级权重指标', 1, NOW()),
(@step3_id, '公众避险能力定权', 'PUBLIC_AVOIDANCE_WEIGHT', 7, 'publicAvoidanceNorm * weight_PUBLIC_AVOIDANCE', NULL, 'publicAvoidanceWeighted', '公众避险能力（定权）=公众避险能力（属性向量归一化）*公众避险能力二级权重指标', 1, NOW()),
(@step3_id, '转移安置能力定权', 'RELOCATION_CAPACITY_WEIGHT', 8, 'relocationCapacityNorm * weight_RELOCATION_CAPACITY', NULL, 'relocationCapacityWeighted', '转移安置能力（定权）=转移安置能力（属性向量归一化）*转移安置能力二级权重指标', 1, NOW());

-- 第二部分：乡镇街道减灾能力定权（9-16号算法）
-- 公式：归一化值 * 一级权重 * 二级权重
INSERT INTO step_algorithm (step_id, algorithm_name, algorithm_code, algorithm_order, ql_expression, input_params, output_param, description, status, create_time) VALUES
(@step3_id, '队伍管理能力综合定权', 'TEAM_MANAGEMENT_TOTAL', 9, 'teamManagementNorm * weight_DISASTER_MANAGEMENT * weight_TEAM_MANAGEMENT', NULL, 'teamManagementTotal', '队伍管理能力（定权）=队伍管理能力（属性向量归一化）*灾害管理能力一级权重指标*队伍管理能力二级权重指标', 1, NOW()),
(@step3_id, '风险评估能力综合定权', 'RISK_ASSESSMENT_TOTAL', 10, 'riskAssessmentNorm * weight_DISASTER_MANAGEMENT * weight_RISK_ASSESSMENT', NULL, 'riskAssessmentTotal', '风险评估能力（定权）=风险评估能力（属性向量归一化）*灾害管理能力一级权重指标*风险评估能力二级权重指标', 1, NOW()),
(@step3_id, '财政投入能力综合定权', 'FINANCIAL_INPUT_TOTAL', 11, 'financialInputNorm * weight_DISASTER_MANAGEMENT * weight_FINANCIAL_INPUT', NULL, 'financialInputTotal', '财政投入能力（定权）=财政投入能力（属性向量归一化）*灾害管理能力一级权重指标*财政投入能力二级权重指标', 1, NOW()),
(@step3_id, '物资储备能力综合定权', 'MATERIAL_RESERVE_TOTAL', 12, 'materialReserveNorm * weight_DISASTER_PREPAREDNESS * weight_MATERIAL_RESERVE', NULL, 'materialReserveTotal', '物资储备能力（定权）=物资储备能力（属性向量归一化）*灾害备灾能力一级权重指标*物资储备能力二级权重指标', 1, NOW()),
(@step3_id, '医疗保障能力综合定权', 'MEDICAL_SUPPORT_TOTAL', 13, 'medicalSupportNorm * weight_DISASTER_PREPAREDNESS * weight_MEDICAL_SUPPORT', NULL, 'medicalSupportTotal', '医疗保障能力（定权）=医疗保障能力（属性向量归一化）*灾害备灾能力一级权重指标*医疗保障能力二级权重指标', 1, NOW()),
(@step3_id, '自救互救能力综合定权', 'SELF_RESCUE_TOTAL', 14, 'selfRescueNorm * weight_SELF_RESCUE_TRANSFER * weight_SELF_RESCUE', NULL, 'selfRescueTotal', '自救互救能力（定权）=自救互救能力（属性向量归一化）*自救转移能力一级权重指标*自救互救能力二级权重指标', 1, NOW()),
(@step3_id, '公众避险能力综合定权', 'PUBLIC_AVOIDANCE_TOTAL', 15, 'publicAvoidanceNorm * weight_SELF_RESCUE_TRANSFER * weight_PUBLIC_AVOIDANCE', NULL, 'publicAvoidanceTotal', '公众避险能力（定权）=公众避险能力（属性向量归一化）*自救转移能力一级权重指标*公众避险能力二级权重指标', 1, NOW()),
(@step3_id, '转移安置能力综合定权', 'RELOCATION_CAPACITY_TOTAL', 16, 'relocationCapacityNorm * weight_SELF_RESCUE_TRANSFER * weight_RELOCATION_CAPACITY', NULL, 'relocationCapacityTotal', '转移安置能力（定权）=转移安置能力（属性向量归一化）*自救转移能力一级权重指标*转移安置能力二级权重指标', 1, NOW());

SELECT CONCAT('步骤3配置完成，共插入 ', COUNT(*), ' 条算法记录') as result FROM step_algorithm WHERE step_id = @step3_id;

-- ============================================================================
-- 步骤4：优劣解算法计算
-- ============================================================================
-- 获取步骤4的ID（如不存在则创建）
SET @model_id = (SELECT id FROM evaluation_model WHERE model_code = 'STANDARD_MODEL' LIMIT 1);
SET @step4_id = (SELECT id FROM model_step WHERE model_id = @model_id AND step_order = 4 LIMIT 1);

-- 如果步骤4不存在，则创建
INSERT INTO model_step (model_id, step_name, step_code, step_order, step_type, description, status, create_time)
SELECT @model_id, '优劣解计算', 'TOPSIS_DISTANCE', 4, 'TOPSIS', 'TOPSIS方法：计算到正理想解（优）和负理想解（差）的距离', 1, NOW()
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM model_step WHERE model_id = @model_id AND step_order = 4);

-- 重新获取步骤4的ID
SET @step4_id = (SELECT id FROM model_step WHERE model_id = @model_id AND step_order = 4 LIMIT 1);

-- 清理现有数据
DELETE FROM step_algorithm WHERE step_id = @step4_id;

-- 插入优劣解算法
-- 使用特殊标记 @TOPSIS_POSITIVE（优） 和 @TOPSIS_NEGATIVE（差）
-- 公式：
-- 优 = SQRT((指标最大值 - 本乡镇指标值)^2 + ...)
-- 差 = SQRT((指标最小值 - 本乡镇指标值)^2 + ...)

-- 第一部分：一级指标优劣解计算（1-6号算法）
INSERT INTO step_algorithm (step_id, algorithm_name, algorithm_code, algorithm_order, ql_expression, input_params, output_param, description, status, create_time) VALUES
(@step4_id, '灾害管理能力优解', 'DISASTER_MGMT_POSITIVE', 1, '@TOPSIS_POSITIVE:teamManagementWeighted,riskAssessmentWeighted,financialInputWeighted', NULL, 'disasterMgmtPositive', '灾害管理能力（优）=SQRT((队伍管理能力最大值-本乡镇队伍管理能力)^2+(风险评估能力最大值-本乡镇风险评估能力)^2+(财政投入能力最大值-本乡镇财政投入能力)^2)', 1, NOW()),
(@step4_id, '灾害管理能力差解', 'DISASTER_MGMT_NEGATIVE', 2, '@TOPSIS_NEGATIVE:teamManagementWeighted,riskAssessmentWeighted,financialInputWeighted', NULL, 'disasterMgmtNegative', '灾害管理能力（差）=SQRT((队伍管理能力最小值-本乡镇队伍管理能力)^2+(风险评估能力最小值-本乡镇风险评估能力)^2+(财政投入能力最小值-本乡镇财政投入能力)^2)', 1, NOW()),
(@step4_id, '灾害备灾能力优解', 'DISASTER_PREP_POSITIVE', 3, '@TOPSIS_POSITIVE:materialReserveWeighted,medicalSupportWeighted', NULL, 'disasterPrepPositive', '灾害备灾能力（优）=SQRT((物资储备能力最大值-本乡镇物资储备能力)^2+(医疗保障能力最大值-本乡镇医疗保障能力)^2)', 1, NOW()),
(@step4_id, '灾害备灾能力差解', 'DISASTER_PREP_NEGATIVE', 4, '@TOPSIS_NEGATIVE:materialReserveWeighted,medicalSupportWeighted', NULL, 'disasterPrepNegative', '灾害备灾能力（差）=SQRT((物资储备能力最小值-本乡镇物资储备能力)^2+(医疗保障能力最小值-本乡镇医疗保障能力)^2)', 1, NOW()),
(@step4_id, '自救转移能力优解', 'SELF_RESCUE_POSITIVE', 5, '@TOPSIS_POSITIVE:selfRescueWeighted,publicAvoidanceWeighted,relocationCapacityWeighted', NULL, 'selfRescuePositive', '自救转移能力（优）=SQRT((自救互救能力最大值-本乡镇自救互救能力)^2+(公众避险能力最大值-本乡镇公众避险能力)^2+(转移安置能力最大值-本乡镇转移安置能力)^2)', 1, NOW()),
(@step4_id, '自救转移能力差解', 'SELF_RESCUE_NEGATIVE', 6, '@TOPSIS_NEGATIVE:selfRescueWeighted,publicAvoidanceWeighted,relocationCapacityWeighted', NULL, 'selfRescueNegative', '自救转移能力（差）=SQRT((自救互救能力最小值-本乡镇自救互救能力)^2+(公众避险能力最小值-本乡镇公众避险能力)^2+(转移安置能力最小值-本乡镇转移安置能力)^2)', 1, NOW());

-- 第二部分：乡镇街道减灾能力优劣解计算（7-8号算法）
INSERT INTO step_algorithm (step_id, algorithm_name, algorithm_code, algorithm_order, ql_expression, input_params, output_param, description, status, create_time) VALUES
(@step4_id, '综合减灾能力优解', 'TOTAL_POSITIVE', 7, '@TOPSIS_POSITIVE:teamManagementTotal,riskAssessmentTotal,financialInputTotal,materialReserveTotal,medicalSupportTotal,selfRescueTotal,publicAvoidanceTotal,relocationCapacityTotal', NULL, 'totalPositive', '乡镇名称（优）=SQRT((队伍管理能力最大值-本乡镇队伍管理能力)^2+(风险评估能力最大值-本乡镇风险评估能力)^2+(财政投入能力最大值-本乡镇财政投入能力)^2+(物资储备能力最大值-本乡镇物资储备能力)^2+(医疗保障能力最大值-本乡镇医疗保障能力)^2+(自救互救能力最大值-本乡镇自救互救能力)^2+(公众避险能力最大值-本乡镇公众避险能力)^2+(转移安置能力最大值-本乡镇转移安置能力)^2)', 1, NOW()),
(@step4_id, '综合减灾能力差解', 'TOTAL_NEGATIVE', 8, '@TOPSIS_NEGATIVE:teamManagementTotal,riskAssessmentTotal,financialInputTotal,materialReserveTotal,medicalSupportTotal,selfRescueTotal,publicAvoidanceTotal,relocationCapacityTotal', NULL, 'totalNegative', '乡镇名称（差）=SQRT((队伍管理能力最小值-本乡镇队伍管理能力)^2+(风险评估能力最小值-本乡镇风险评估能力)^2+(财政投入能力最小值-本乡镇财政投入能力)^2+(物资储备能力最小值-本乡镇物资储备能力)^2+(医疗保障能力最小值-本乡镇医疗保障能力)^2+(自救互救能力最小值-本乡镇自救互救能力)^2+(公众避险能力最小值-本乡镇公众避险能力)^2+(转移安置能力最小值-本乡镇转移安置能力)^2)', 1, NOW());

SELECT CONCAT('步骤4配置完成，共插入 ', COUNT(*), ' 条算法记录') as result FROM step_algorithm WHERE step_id = @step4_id;

-- ============================================================================
-- 步骤5：能力值计算与分级
-- ============================================================================
-- 获取步骤5的ID（如不存在则创建）
SET @step5_id = (SELECT id FROM model_step WHERE model_id = @model_id AND step_order = 5 LIMIT 1);

-- 如果步骤5不存在，则创建
INSERT INTO model_step (model_id, step_name, step_code, step_order, step_type, description, status, create_time)
SELECT @model_id, '能力值计算与分级', 'CAPABILITY_GRADE', 5, 'GRADING', '计算能力值（贴近度）并进行五级分类（强/较强/中等/较弱/弱）', 1, NOW()
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM model_step WHERE model_id = @model_id AND step_order = 5);

-- 重新获取步骤5的ID
SET @step5_id = (SELECT id FROM model_step WHERE model_id = @model_id AND step_order = 5 LIMIT 1);

-- 清理现有数据
DELETE FROM step_algorithm WHERE step_id = @step5_id;

-- 插入能力值计算与分级算法

-- 第一部分：能力值计算（1-4号算法）
-- 公式：能力值 = 差解距离 / (差解距离 + 优解距离)
INSERT INTO step_algorithm (step_id, algorithm_name, algorithm_code, algorithm_order, ql_expression, input_params, output_param, description, status, create_time) VALUES
(@step5_id, '灾害管理能力值', 'DISASTER_MGMT_SCORE', 1, 'disasterMgmtNegative / (disasterMgmtNegative + disasterMgmtPositive)', NULL, 'disasterMgmtScore', '灾害管理能力=灾害管理能力（差）/(灾害管理能力（差）+灾害管理能力（优）)', 1, NOW()),
(@step5_id, '灾害备灾能力值', 'DISASTER_PREP_SCORE', 2, 'disasterPrepNegative / (disasterPrepNegative + disasterPrepPositive)', NULL, 'disasterPrepScore', '灾害备灾能力=灾害备灾能力（差）/(灾害备灾能力（差）+灾害备灾能力（优）)', 1, NOW()),
(@step5_id, '自救转移能力值', 'SELF_RESCUE_SCORE', 3, 'selfRescueNegative / (selfRescueNegative + selfRescuePositive)', NULL, 'selfRescueScore', '自救转移能力=自救转移能力（差）/(自救转移能力（差）+自救转移能力（优）)', 1, NOW()),
(@step5_id, '综合减灾能力值', 'TOTAL_SCORE', 4, 'totalNegative / (totalNegative + totalPositive)', NULL, 'totalScore', '灾害管理能力=灾害管理能力（差）/(灾害管理能力（差）+灾害管理能力（优）)', 1, NOW());

-- 第二部分：能力分级（5-8号算法）
-- 使用特殊标记 @GRADE 表示需要基于均值和标准差进行分级
-- 分级规则：
-- 如果 μ <= 0.5σ:
--   value >= μ+1.5σ → 强
--   value >= μ+0.5σ → 较强
--   否则 → 中等
-- 如果 μ <= 1.5σ:
--   value >= μ+1.5σ → 强
--   value >= μ+0.5σ → 较强
--   value >= μ-0.5σ → 中等
--   否则 → 较弱
-- 否则:
--   value >= μ+1.5σ → 强
--   value >= μ+0.5σ → 较强
--   value >= μ-0.5σ → 中等
--   value >= μ-1.5σ → 较弱
--   否则 → 弱

INSERT INTO step_algorithm (step_id, algorithm_name, algorithm_code, algorithm_order, ql_expression, input_params, output_param, description, status, create_time) VALUES
(@step5_id, '灾害管理能力分级', 'DISASTER_MGMT_GRADE', 5, '@GRADE:disasterMgmtScore', NULL, 'disasterMgmtGrade', '灾害管理能力（分级）：基于均值μ和标准差σ进行五级分类', 1, NOW()),
(@step5_id, '灾害备灾能力分级', 'DISASTER_PREP_GRADE', 6, '@GRADE:disasterPrepScore', NULL, 'disasterPrepGrade', '灾害备灾能力（分级）：基于均值μ和标准差σ进行五级分类', 1, NOW()),
(@step5_id, '自救转移能力分级', 'SELF_RESCUE_GRADE', 7, '@GRADE:selfRescueScore', NULL, 'selfRescueGrade', '自救转移能力（分级）：基于均值μ和标准差σ进行五级分类', 1, NOW()),
(@step5_id, '综合减灾能力分级', 'TOTAL_GRADE', 8, '@GRADE:totalScore', NULL, 'totalGrade', '乡镇（街道）减灾能力（分级）：基于均值μ和标准差σ进行五级分类', 1, NOW());

SELECT CONCAT('步骤5配置完成，共插入 ', COUNT(*), ' 条算法记录') as result FROM step_algorithm WHERE step_id = @step5_id;

-- ============================================================================
-- 验证配置结果
-- ============================================================================
SELECT 
    '=' as separator,
    '配置完成统计' as title,
    '=' as separator2;

SELECT 
    ms.step_order as '步骤序号',
    ms.step_name as '步骤名称',
    ms.step_code as '步骤编码',
    ms.step_type as '步骤类型',
    COUNT(sa.id) as '算法数量'
FROM model_step ms
LEFT JOIN step_algorithm sa ON ms.id = sa.step_id
WHERE ms.model_id = @model_id AND ms.step_order BETWEEN 2 AND 5
GROUP BY ms.id, ms.step_order, ms.step_name, ms.step_code, ms.step_type
ORDER BY ms.step_order;

SELECT 
    '=' as separator,
    '详细算法列表' as title,
    '=' as separator2;

SELECT 
    ms.step_order as '步骤',
    sa.algorithm_order as '序号',
    sa.algorithm_name as '算法名称',
    sa.algorithm_code as '算法编码',
    sa.output_param as '输出参数',
    LEFT(sa.description, 50) as '描述'
FROM model_step ms
INNER JOIN step_algorithm sa ON ms.id = sa.step_id
WHERE ms.model_id = @model_id AND ms.step_order BETWEEN 2 AND 5
ORDER BY ms.step_order, sa.algorithm_order;

SELECT '更新完成！步骤2-5的算法配置已全部更新。' as message;
