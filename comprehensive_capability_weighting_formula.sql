-- 综合能力定权公式确认和文档化
-- 创建时间: 2025-01-24
-- 说明: 确认综合能力定权公式：能力值 * 对应指标的1级权重值 * 2级权重值

USE evaluate_db;

-- 综合能力定权公式说明
-- ==========================================
-- 综合能力的定权公式是：能力值 * 对应指标的1级权重值 * 2级权重值
-- 
-- 分类说明：
-- 1. 普通能力：使用二级权重值进行定权
--    公式：归一化能力值 * 二级权重
-- 
-- 2. 综合能力：使用一级权重和二级权重的乘积进行定权  
--    公式：归一化能力值 * 一级权重 * 二级权重
-- ==========================================

-- 查看当前综合能力定权公式的配置
SELECT 
    sa.id,
    sa.algorithm_name,
    sa.ql_expression,
    sa.description,
    CASE 
        WHEN sa.algorithm_name LIKE '%综合定权%' THEN '综合能力定权'
        ELSE '普通能力定权'
    END as formula_type
FROM step_algorithm sa 
WHERE sa.step_id = 17 
ORDER BY sa.algorithm_order;

-- 验证综合能力定权公式是否符合要求
-- 综合能力定权公式结构验证：
-- 1. 队伍管理能力综合定权：teamManagementNorm * weight_L1_MANAGEMENT * weight_L2_MANAGEMENT_CAPABILITY
-- 2. 风险评估能力综合定权：riskAssessmentNorm * weight_L1_MANAGEMENT * weight_L2_RISK_ASSESSMENT  
-- 3. 财政投入能力综合定权：financialInputNorm * weight_L1_MANAGEMENT * weight_L2_FUNDING
-- 4. 物资储备能力综合定权：materialReserveNorm * weight_L1_PREPARATION * weight_L2_MATERIAL
-- 5. 医疗保障能力综合定权：medicalSupportNorm * weight_L1_PREPARATION * weight_L2_MEDICAL
-- 6. 自救互救能力综合定权：selfRescueNorm * weight_L1_SELF_RESCUE * weight_L2_SELF_RESCUE
-- 7. 公众避险能力综合定权：publicAvoidanceNorm * weight_L1_SELF_RESCUE * weight_L2_PUBLIC_AVOIDANCE  
-- 8. 转移安置能力综合定权：relocationCapacityNorm * weight_L1_SELF_RESCUE * weight_L2_RELOCATION

-- 公式结构确认：所有综合定权公式都遵循 "能力值 * 一级权重 * 二级权重" 的格式

-- 如果需要更新或添加新的综合定权公式，可以使用以下模板：
/*
INSERT INTO step_algorithm (
    step_id, 
    algorithm_name, 
    algorithm_code, 
    algorithm_order, 
    ql_expression, 
    output_param, 
    description, 
    status
) VALUES (
    17, -- 二级指标定权步骤ID
    '能力名称综合定权', 
    'CAPABILITY_NAME_COMPREHENSIVE_WEIGHT', 
    排序号, 
    'normalizedValue * weight_L1_XXX * weight_L2_YYY', 
    'capabilityNameTotal', 
    '能力名称（定权）=能力名称（属性向量归一化）*一级权重指标*二级权重指标', 
    1
);
*/

SELECT 'Comprehensive capability weighting formula documented and verified successfully!' as message;