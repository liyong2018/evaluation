-- 为模型8的步骤1添加原始数据字段输出
-- 当前只有9个算法，需要添加4个算法输出原始数据字段

-- 首先查看当前配置
SELECT '==== 当前步骤1配置（9个算法） ====' AS info;
SELECT 
    algorithm_order,
    algorithm_name,
    output_param
FROM step_algorithm sa
JOIN model_step ms ON sa.step_id = ms.id
WHERE ms.model_id = 8 AND ms.step_code = 'COMMUNITY_INDICATORS'
ORDER BY algorithm_order;

-- 获取步骤ID
SET @step_id = (SELECT id FROM model_step WHERE model_id = 8 AND step_code = 'COMMUNITY_INDICATORS');

-- 添加算法10：常住人口数量
INSERT INTO step_algorithm (step_id, algorithm_order, algorithm_name, algorithm_code, ql_expression, output_param, status, create_time)
VALUES (@step_id, 10, 'Resident Population', 'RESIDENT_POPULATION', 'resident_population != null ? resident_population : 0', 'RESIDENT_POPULATION', 1, NOW());

-- 添加算法11：资金投入
INSERT INTO step_algorithm (step_id, algorithm_order, algorithm_name, algorithm_code, ql_expression, output_param, status, create_time)
VALUES (@step_id, 11, 'Funding Amount', 'FUNDING_AMOUNT', 'last_year_funding_amount != null ? last_year_funding_amount : 0.0', 'FUNDING_AMOUNT', 1, NOW());

-- 添加算法12：物资装备金额
INSERT INTO step_algorithm (step_id, algorithm_order, algorithm_name, algorithm_code, ql_expression, output_param, status, create_time)
VALUES (@step_id, 12, 'Materials Value', 'MATERIALS_VALUE', 'materials_equipment_value != null ? materials_equipment_value : 0.0', 'MATERIALS_VALUE', 1, NOW());

-- 添加算法13：避难场所容量
INSERT INTO step_algorithm (step_id, algorithm_order, algorithm_name, algorithm_code, ql_expression, output_param, status, create_time)
VALUES (@step_id, 13, 'Shelter Capacity', 'SHELTER_CAPACITY', 'emergency_shelter_capacity != null ? emergency_shelter_capacity : 0', 'SHELTER_CAPACITY', 1, NOW());

SELECT '==== 添加后的步骤1配置（13个算法） ====' AS info;
SELECT 
    algorithm_order,
    algorithm_name,
    output_param
FROM step_algorithm sa
JOIN model_step ms ON sa.step_id = ms.id
WHERE ms.model_id = 8 AND ms.step_code = 'COMMUNITY_INDICATORS'
ORDER BY algorithm_order;

SELECT '==== 验证：应该有13个算法 ====' AS info;
SELECT COUNT(*) AS '算法数量'
FROM step_algorithm sa
JOIN model_step ms ON sa.step_id = ms.id
WHERE ms.model_id = 8 AND ms.step_code = 'COMMUNITY_INDICATORS';
