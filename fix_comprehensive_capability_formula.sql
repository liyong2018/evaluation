-- Fix comprehensive capability weighting formula variable names
-- Date: 2025-01-24
-- Problem: Database uses "teamManagementTotal" but Java code uses "teamManagementWeighted"
-- Solution: Update variable names to match Java implementation

USE evaluate_db;

-- Check current configuration
SELECT 
    sa.id,
    sa.algorithm_name,
    sa.ql_expression,
    sa.description
FROM step_algorithm sa 
WHERE sa.step_id = 18 
AND sa.algorithm_name LIKE '%综合减灾能力%'
ORDER BY sa.algorithm_order;

-- Update comprehensive capability TOPSIS positive formula
-- Change from: teamManagementTotal -> teamManagementWeighted
UPDATE step_algorithm 
SET ql_expression = '@TOPSIS_POSITIVE:teamManagementWeighted,riskAssessmentWeighted,financialInputWeighted,materialReserveWeighted,medicalSupportWeighted,selfRescueWeighted,publicAvoidanceWeighted,relocationCapacityWeighted',
    description = '综合减灾能力（优）=SQRT((队伍管理能力最大值-本乡镇队伍管理能力)^2+(风险评估能力最大值-本乡镇风险评估能力)^2+(财政投入能力最大值-本乡镇财政投入能力)^2+(物资储备能力最大值-本乡镇物资储备能力)^2+(医疗保障能力最大值-本乡镇医疗保障能力)^2+(自救互救能力最大值-本乡镇自救互救能力)^2+(公众避险能力最大值-本乡镇公众避险能力)^2+(转移安置能力最大值-本乡镇转移安置能力)^2)'
WHERE id = 61;

-- Update comprehensive capability TOPSIS negative formula  
-- Change from: teamManagementTotal -> teamManagementWeighted
UPDATE step_algorithm 
SET ql_expression = '@TOPSIS_NEGATIVE:teamManagementWeighted,riskAssessmentWeighted,financialInputWeighted,materialReserveWeighted,medicalSupportWeighted,selfRescueWeighted,publicAvoidanceWeighted,relocationCapacityWeighted',
    description = '综合减灾能力（差）=SQRT((队伍管理能力最小值-本乡镇队伍管理能力)^2+(风险评估能力最小值-本乡镇风险评估能力)^2+(财政投入能力最小值-本乡镇财政投入能力)^2+(物资储备能力最小值-本乡镇物资储备能力)^2+(医疗保障能力最小值-本乡镇医疗保障能力)^2+(自救互救能力最小值-本乡镇自救互救能力)^2+(公众避险能力最小值-本乡镇公众避险能力)^2+(转移安置能力最小值-本乡镇转移安置能力)^2)'
WHERE id = 62;

-- Verify the update
SELECT 
    sa.id,
    sa.algorithm_name,
    sa.ql_expression,
    sa.description
FROM step_algorithm sa 
WHERE sa.step_id = 18 
AND sa.algorithm_name LIKE '%综合减灾能力%'
ORDER BY sa.algorithm_order;

SELECT 'Comprehensive capability formula variable names fixed successfully!' as message;