-- 修复步骤3算法表达式中的权重指标代码
-- 问题：算法表达式使用的权重变量名与数据库中的indicator_code不匹配
-- 解决：将表达式中的权重变量名更新为与indicator_weight表中的indicator_code一致

USE evaluate_db;

SET NAMES utf8mb4;
SET CHARACTER SET utf8mb4;

-- 获取步骤3的ID
SET @step3_id = (SELECT id FROM model_step WHERE model_id = (SELECT id FROM evaluation_model WHERE model_code = 'STANDARD_MODEL' LIMIT 1) AND step_order = 3 LIMIT 1);

-- 更新步骤3的算法表达式，使用正确的权重指标代码
-- 二级指标权重代码映射：
-- weight_TEAM_MANAGEMENT -> weight_L2_MANAGEMENT_CAPABILITY
-- weight_RISK_ASSESSMENT -> weight_L2_RISK_ASSESSMENT
-- weight_FINANCIAL_INPUT -> weight_L2_FUNDING
-- weight_MATERIAL_RESERVE -> weight_L2_MATERIAL
-- weight_MEDICAL_SUPPORT -> weight_L2_MEDICAL
-- weight_SELF_RESCUE -> weight_L2_SELF_RESCUE
-- weight_PUBLIC_AVOIDANCE -> weight_L2_PUBLIC_AVOIDANCE
-- weight_RELOCATION_CAPACITY -> weight_L2_RELOCATION

-- 一级指标权重代码映射：
-- weight_DISASTER_MANAGEMENT -> weight_L1_MANAGEMENT
-- weight_DISASTER_PREPAREDNESS -> weight_L1_PREPARATION
-- weight_SELF_RESCUE_TRANSFER -> weight_L1_SELF_RESCUE

-- 第一部分：一级指标定权（1-8号算法）
UPDATE step_algorithm 
SET ql_expression = 'teamManagementNorm * weight_L2_MANAGEMENT_CAPABILITY'
WHERE step_id = @step3_id AND algorithm_code = 'TEAM_MANAGEMENT_WEIGHT';

UPDATE step_algorithm 
SET ql_expression = 'riskAssessmentNorm * weight_L2_RISK_ASSESSMENT'
WHERE step_id = @step3_id AND algorithm_code = 'RISK_ASSESSMENT_WEIGHT';

UPDATE step_algorithm 
SET ql_expression = 'financialInputNorm * weight_L2_FUNDING'
WHERE step_id = @step3_id AND algorithm_code = 'FINANCIAL_INPUT_WEIGHT';

UPDATE step_algorithm 
SET ql_expression = 'materialReserveNorm * weight_L2_MATERIAL'
WHERE step_id = @step3_id AND algorithm_code = 'MATERIAL_RESERVE_WEIGHT';

UPDATE step_algorithm 
SET ql_expression = 'medicalSupportNorm * weight_L2_MEDICAL'
WHERE step_id = @step3_id AND algorithm_code = 'MEDICAL_SUPPORT_WEIGHT';

UPDATE step_algorithm 
SET ql_expression = 'selfRescueNorm * weight_L2_SELF_RESCUE'
WHERE step_id = @step3_id AND algorithm_code = 'SELF_RESCUE_WEIGHT';

UPDATE step_algorithm 
SET ql_expression = 'publicAvoidanceNorm * weight_L2_PUBLIC_AVOIDANCE'
WHERE step_id = @step3_id AND algorithm_code = 'PUBLIC_AVOIDANCE_WEIGHT';

UPDATE step_algorithm 
SET ql_expression = 'relocationCapacityNorm * weight_L2_RELOCATION'
WHERE step_id = @step3_id AND algorithm_code = 'RELOCATION_CAPACITY_WEIGHT';

-- 第二部分：乡镇街道减灾能力定权（9-16号算法）
UPDATE step_algorithm 
SET ql_expression = 'teamManagementNorm * weight_L1_MANAGEMENT * weight_L2_MANAGEMENT_CAPABILITY'
WHERE step_id = @step3_id AND algorithm_code = 'TEAM_MANAGEMENT_TOTAL';

UPDATE step_algorithm 
SET ql_expression = 'riskAssessmentNorm * weight_L1_MANAGEMENT * weight_L2_RISK_ASSESSMENT'
WHERE step_id = @step3_id AND algorithm_code = 'RISK_ASSESSMENT_TOTAL';

UPDATE step_algorithm 
SET ql_expression = 'financialInputNorm * weight_L1_MANAGEMENT * weight_L2_FUNDING'
WHERE step_id = @step3_id AND algorithm_code = 'FINANCIAL_INPUT_TOTAL';

UPDATE step_algorithm 
SET ql_expression = 'materialReserveNorm * weight_L1_PREPARATION * weight_L2_MATERIAL'
WHERE step_id = @step3_id AND algorithm_code = 'MATERIAL_RESERVE_TOTAL';

UPDATE step_algorithm 
SET ql_expression = 'medicalSupportNorm * weight_L1_PREPARATION * weight_L2_MEDICAL'
WHERE step_id = @step3_id AND algorithm_code = 'MEDICAL_SUPPORT_TOTAL';

UPDATE step_algorithm 
SET ql_expression = 'selfRescueNorm * weight_L1_SELF_RESCUE * weight_L2_SELF_RESCUE'
WHERE step_id = @step3_id AND algorithm_code = 'SELF_RESCUE_TOTAL';

UPDATE step_algorithm 
SET ql_expression = 'publicAvoidanceNorm * weight_L1_SELF_RESCUE * weight_L2_PUBLIC_AVOIDANCE'
WHERE step_id = @step3_id AND algorithm_code = 'PUBLIC_AVOIDANCE_TOTAL';

UPDATE step_algorithm 
SET ql_expression = 'relocationCapacityNorm * weight_L1_SELF_RESCUE * weight_L2_RELOCATION'
WHERE step_id = @step3_id AND algorithm_code = 'RELOCATION_CAPACITY_TOTAL';

-- 验证更新结果
SELECT '==========================================' as status;
SELECT '          更新后的算法表达式            ' as title;
SELECT '==========================================' as status;

SELECT 
    algorithm_order as seq_no,
    algorithm_name as name,
    algorithm_code as code,
    ql_expression as expression
FROM step_algorithm
WHERE step_id = @step3_id
ORDER BY algorithm_order;

SELECT CONCAT('步骤3算法表达式已更新，共 ', COUNT(*), ' 条记录') as result 
FROM step_algorithm 
WHERE step_id = @step3_id;
