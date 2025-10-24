-- 追踪模型8的执行过程

-- 1. 检查社区指标计算的输入字段
SELECT '==== 检查社区数据（输入） ====' AS info;
SELECT 
    region_code,
    has_emergency_plan,
    has_vulnerable_groups_list,
    has_disaster_points_list,
    has_disaster_map,
    resident_population,
    last_year_funding_amount,
    materials_equipment_value
FROM community_disaster_reduction_capacity
WHERE region_code IN ('511425001001', '511425001002', '511425001003')
ORDER BY region_code;

-- 2. 检查归一化步骤的输入来源
SELECT '==== 归一化步骤配置 ====' AS info;
SELECT 
    sa.algorithm_order,
    sa.algorithm_name,
    sa.ql_expression,
    sa.output_param
FROM step_algorithm sa
JOIN model_step ms ON sa.step_id = ms.id
WHERE ms.model_id = 8 AND ms.step_code = 'VECTOR_NORMALIZATION'
ORDER BY sa.algorithm_order;

-- 3. 检查归一化步骤的输入字段是否存在
SELECT '==== 检查归一化步骤的输入字段 ====' AS info;
SELECT '算法1期望输入: PLAN_CONSTRUCTION' AS check_item;
SELECT '算法2期望输入: HAZARD_INSPECTION' AS check_item;
SELECT '算法3期望输入: riskAssessmentCapability (通过@NORMALIZE)' AS check_item;
SELECT '算法4期望输入: FINANCIAL_INPUT' AS check_item;
SELECT '算法5期望输入: materialReserveCapability (通过@NORMALIZE)' AS check_item;
SELECT '算法6期望输入: medicalSupportCapability (通过@NORMALIZE)' AS check_item;

-- 4. 检查乡镇聚合步骤的输入
SELECT '==== 乡镇聚合步骤配置 ====' AS info;
SELECT 
    sa.algorithm_order,
    sa.algorithm_name,
    sa.ql_expression,
    sa.output_param
FROM step_algorithm sa
JOIN model_step ms ON sa.step_id = ms.id
WHERE ms.model_id = 8 AND ms.step_code = 'TOWNSHIP_AGGREGATION'
ORDER BY sa.algorithm_order;

-- 5. 检查社区指标计算步骤的输出
SELECT '==== 社区指标计算步骤输出 ====' AS info;
SELECT 
    sa.algorithm_order,
    sa.algorithm_name,
    sa.output_param
FROM step_algorithm sa
JOIN model_step ms ON sa.step_id = ms.id
WHERE ms.model_id = 8 AND ms.step_code = 'COMMUNITY_INDICATORS'
ORDER BY sa.algorithm_order;
