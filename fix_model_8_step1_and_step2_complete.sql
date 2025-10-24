-- 完全重构模型8的步骤1和步骤2
-- 步骤1：输出13列原始数据（是/否转1/0，数值保持原值）
-- 步骤2：根据乡镇求和后计算9个能力值

-- ========================================
-- 第一部分：修改步骤1 - 输出原始数据
-- ========================================

SELECT '==== 第一部分：修改步骤1为输出原始数据 ====' AS info;

-- 获取步骤ID
SET @step1_id = (SELECT id FROM model_step WHERE model_id = 8 AND step_code = 'COMMUNITY_INDICATORS');

-- 修改算法1：是否有应急预案（是→1，否→0）
UPDATE step_algorithm SET 
    ql_expression = 'has_emergency_plan == "是" ? 1 : 0',
    output_param = 'HAS_EMERGENCY_PLAN'
WHERE step_id = @step1_id AND algorithm_order = 1;

-- 修改算法2：是否有弱势人群清单（是→1，否→0）
UPDATE step_algorithm SET 
    algorithm_name = 'Has Vulnerable Groups List',
    algorithm_code = 'HAS_VULNERABLE_GROUPS_LIST',
    ql_expression = 'has_vulnerable_groups_list == "是" ? 1 : 0',
    output_param = 'HAS_VULNERABLE_GROUPS_LIST'
WHERE step_id = @step1_id AND algorithm_order = 2;

-- 修改算法3：是否有隐患点清单（是→1，否→0）
UPDATE step_algorithm SET 
    algorithm_name = 'Has Disaster Points List',
    algorithm_code = 'HAS_DISASTER_POINTS_LIST',
    ql_expression = 'has_disaster_points_list == "是" ? 1 : 0',
    output_param = 'HAS_DISASTER_POINTS_LIST'
WHERE step_id = @step1_id AND algorithm_order = 3;

-- 修改算法4：是否有灾害地图（是→1，否→0）
UPDATE step_algorithm SET 
    algorithm_name = 'Has Disaster Map',
    algorithm_code = 'HAS_DISASTER_MAP',
    ql_expression = 'has_disaster_map == "是" ? 1 : 0',
    output_param = 'HAS_DISASTER_MAP'
WHERE step_id = @step1_id AND algorithm_order = 4;

-- 修改算法5：常住人口（原值）
UPDATE step_algorithm SET 
    algorithm_name = 'Resident Population',
    algorithm_code = 'RESIDENT_POPULATION_RAW',
    ql_expression = 'resident_population != null ? resident_population : 0',
    output_param = 'RESIDENT_POPULATION'
WHERE step_id = @step1_id AND algorithm_order = 5;

-- 修改算法6：资金投入（原值）
UPDATE step_algorithm SET 
    algorithm_name = 'Funding Amount',
    algorithm_code = 'FUNDING_AMOUNT_RAW',
    ql_expression = 'last_year_funding_amount != null ? last_year_funding_amount : 0.0',
    output_param = 'FUNDING_AMOUNT'
WHERE step_id = @step1_id AND algorithm_order = 6;

-- 修改算法7：物资装备金额（原值）
UPDATE step_algorithm SET 
    algorithm_name = 'Materials Value',
    algorithm_code = 'MATERIALS_VALUE_RAW',
    ql_expression = 'materials_equipment_value != null ? materials_equipment_value : 0.0',
    output_param = 'MATERIALS_VALUE'
WHERE step_id = @step1_id AND algorithm_order = 7;

-- 修改算法8：医疗服务站数量（原值）
UPDATE step_algorithm SET 
    algorithm_name = 'Medical Service Count',
    algorithm_code = 'MEDICAL_SERVICE_COUNT_RAW',
    ql_expression = 'medical_service_count != null ? medical_service_count : 0',
    output_param = 'MEDICAL_SERVICE_COUNT'
WHERE step_id = @step1_id AND algorithm_order = 8;

-- 修改算法9：民兵预备役人数（原值）
UPDATE step_algorithm SET 
    algorithm_name = 'Militia Reserve Count',
    algorithm_code = 'MILITIA_RESERVE_COUNT_RAW',
    ql_expression = 'militia_reserve_count != null ? militia_reserve_count : 0',
    output_param = 'MILITIA_RESERVE_COUNT'
WHERE step_id = @step1_id AND algorithm_order = 9;

-- 修改算法10：志愿者人数（原值）
UPDATE step_algorithm SET 
    ql_expression = 'registered_volunteer_count != null ? registered_volunteer_count : 0',
    output_param = 'VOLUNTEER_COUNT'
WHERE step_id = @step1_id AND algorithm_order = 10;

-- 修改算法11：培训人次（原值）
UPDATE step_algorithm SET 
    algorithm_name = 'Training Participants',
    algorithm_code = 'TRAINING_PARTICIPANTS_RAW',
    ql_expression = 'last_year_training_participants != null ? last_year_training_participants : 0',
    output_param = 'TRAINING_PARTICIPANTS'
WHERE step_id = @step1_id AND algorithm_order = 11;

-- 修改算法12：演练人次（原值）
UPDATE step_algorithm SET 
    algorithm_name = 'Drill Participants',
    algorithm_code = 'DRILL_PARTICIPANTS_RAW',
    ql_expression = 'last_year_drill_participants != null ? last_year_drill_participants : 0',
    output_param = 'DRILL_PARTICIPANTS'
WHERE step_id = @step1_id AND algorithm_order = 12;

-- 修改算法13：避难场所容量（原值）
UPDATE step_algorithm SET 
    ql_expression = 'emergency_shelter_capacity != null ? emergency_shelter_capacity : 0',
    output_param = 'SHELTER_CAPACITY'
WHERE step_id = @step1_id AND algorithm_order = 13;

SELECT '步骤1修改完成：现在输出13列原始数据' AS status;

-- ========================================
-- 第二部分：修改步骤2 - 计算9个能力值
-- ========================================

SELECT '' AS info;
SELECT '==== 第二部分：修改步骤2为计算能力值 ====' AS info;

-- 获取步骤2的ID
SET @step2_id = (SELECT id FROM model_step WHERE model_id = 8 AND step_code = 'TOWNSHIP_AGGREGATION');

-- 删除现有的9个简单聚合算法
DELETE FROM step_algorithm WHERE step_id = @step2_id;

-- 添加新的9个能力值计算算法

-- 算法1：预案建设能力 = 有预案的社区数 / 总社区数
INSERT INTO step_algorithm (step_id, algorithm_order, algorithm_name, algorithm_code, ql_expression, output_param, status, create_time)
VALUES (@step2_id, 1, 'Plan Construction Capability', 'PLAN_CONSTRUCTION', 
'@SUM:HAS_EMERGENCY_PLAN', 'PLAN_CONSTRUCTION', 1, NOW());

-- 算法2：隐患排查能力 = (有弱势人群清单的社区数 + 有隐患点清单的社区数) / (总社区数 * 2)
INSERT INTO step_algorithm (step_id, algorithm_order, algorithm_name, algorithm_code, ql_expression, output_param, status, create_time)
VALUES (@step2_id, 2, 'Hazard Inspection Capability', 'HAZARD_INSPECTION',
'@SUM:HAS_VULNERABLE_GROUPS_LIST,HAS_DISASTER_POINTS_LIST', 'HAZARD_INSPECTION', 1, NOW());

-- 算法3：风险评估能力 = 有灾害地图的社区数 / 总社区数
INSERT INTO step_algorithm (step_id, algorithm_order, algorithm_name, algorithm_code, ql_expression, output_param, status, create_time)
VALUES (@step2_id, 3, 'Risk Assessment Capability', 'RISK_ASSESSMENT',
'@SUM:HAS_DISASTER_MAP', 'RISK_ASSESSMENT', 1, NOW());

-- 算法4：财政投入能力 = 总资金投入 / 总人口 * 10000
INSERT INTO step_algorithm (step_id, algorithm_order, algorithm_name, algorithm_code, ql_expression, output_param, status, create_time)
VALUES (@step2_id, 4, 'Financial Input Capability', 'FINANCIAL_INPUT',
'@SUM:FUNDING_AMOUNT', 'FINANCIAL_INPUT', 1, NOW());

-- 算法5：物资储备能力 = 总物资金额 / 总人口 * 10000
INSERT INTO step_algorithm (step_id, algorithm_order, algorithm_name, algorithm_code, ql_expression, output_param, status, create_time)
VALUES (@step2_id, 5, 'Material Reserve Capability', 'MATERIAL_RESERVE',
'@SUM:MATERIALS_VALUE', 'MATERIAL_RESERVE', 1, NOW());

-- 算法6：医疗保障能力 = 总医疗服务站数 / 总人口 * 10000
INSERT INTO step_algorithm (step_id, algorithm_order, algorithm_name, algorithm_code, ql_expression, output_param, status, create_time)
VALUES (@step2_id, 6, 'Medical Support Capability', 'MEDICAL_SUPPORT',
'@SUM:MEDICAL_SERVICE_COUNT', 'MEDICAL_SUPPORT', 1, NOW());

-- 算法7：自救互救能力 = (总民兵数 + 总志愿者数) / 总人口 * 10000
INSERT INTO step_algorithm (step_id, algorithm_order, algorithm_name, algorithm_code, ql_expression, output_param, status, create_time)
VALUES (@step2_id, 7, 'Self Mutual Aid Capability', 'SELF_MUTUAL_AID',
'@SUM:MILITIA_RESERVE_COUNT,VOLUNTEER_COUNT', 'SELF_MUTUAL_AID', 1, NOW());

-- 算法8：公众避险能力 = (总培训人次 + 总演练人次) / 总人口 * 100
INSERT INTO step_algorithm (step_id, algorithm_order, algorithm_name, algorithm_code, ql_expression, output_param, status, create_time)
VALUES (@step2_id, 8, 'Public Evacuation Capability', 'PUBLIC_EVACUATION',
'@SUM:TRAINING_PARTICIPANTS,DRILL_PARTICIPANTS', 'PUBLIC_EVACUATION', 1, NOW());

-- 算法9：转移安置能力 = 总避难场所容量 / 总人口
INSERT INTO step_algorithm (step_id, algorithm_order, algorithm_name, algorithm_code, ql_expression, output_param, status, create_time)
VALUES (@step2_id, 9, 'Relocation Shelter Capability', 'RELOCATION_SHELTER',
'@SUM:SHELTER_CAPACITY', 'RELOCATION_SHELTER', 1, NOW());

-- 还需要一个算法来聚合总人口
INSERT INTO step_algorithm (step_id, algorithm_order, algorithm_name, algorithm_code, ql_expression, output_param, status, create_time)
VALUES (@step2_id, 10, 'Total Population', 'TOTAL_POPULATION',
'@SUM:RESIDENT_POPULATION', 'TOTAL_POPULATION', 1, NOW());

SELECT '步骤2修改完成：现在计算9个能力值' AS status;

-- ========================================
-- 验证修改结果
-- ========================================

SELECT '' AS info;
SELECT '==== 验证修改结果 ====' AS info;

SELECT '步骤1：应该有13个算法输出原始数据' AS check_item;
SELECT COUNT(*) AS '算法数量' FROM step_algorithm WHERE step_id = @step1_id;

SELECT '步骤2：应该有10个算法（9个能力值+1个总人口）' AS check_item;
SELECT COUNT(*) AS '算法数量' FROM step_algorithm WHERE step_id = @step2_id;

SELECT '' AS info;
SELECT '注意：步骤2使用了@SUM标记，需要在代码中实现该标记的支持！' AS warning;
