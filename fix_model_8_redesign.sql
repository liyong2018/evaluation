-- 重新设计模型8的步骤1和步骤2
-- 
-- 步骤1：输出13列原始数据
-- 步骤2：按乡镇聚合并计算9个能力值

-- ========================================
-- 第一部分：修改步骤1 - 输出13列原始数据
-- ========================================

SELECT '========================================' AS info;
SELECT '第一部分：修改步骤1为输出13列原始数据' AS info;
SELECT '========================================' AS info;

SET @step1_id = (SELECT id FROM model_step WHERE model_id = 8 AND step_code = 'COMMUNITY_INDICATORS');

-- 算法1-4：是/否字段转1/0
UPDATE step_algorithm SET 
    algorithm_name = 'Has Emergency Plan',
    algorithm_code = 'HAS_EMERGENCY_PLAN',
    ql_expression = 'has_emergency_plan == "是" ? 1 : 0',
    output_param = 'HAS_EMERGENCY_PLAN'
WHERE step_id = @step1_id AND algorithm_order = 1;

UPDATE step_algorithm SET 
    algorithm_name = 'Has Vulnerable Groups List',
    algorithm_code = 'HAS_VULNERABLE_GROUPS_LIST',
    ql_expression = 'has_vulnerable_groups_list == "是" ? 1 : 0',
    output_param = 'HAS_VULNERABLE_GROUPS_LIST'
WHERE step_id = @step1_id AND algorithm_order = 2;

UPDATE step_algorithm SET 
    algorithm_name = 'Has Disaster Points List',
    algorithm_code = 'HAS_DISASTER_POINTS_LIST',
    ql_expression = 'has_disaster_points_list == "是" ? 1 : 0',
    output_param = 'HAS_DISASTER_POINTS_LIST'
WHERE step_id = @step1_id AND algorithm_order = 3;

UPDATE step_algorithm SET 
    algorithm_name = 'Has Disaster Map',
    algorithm_code = 'HAS_DISASTER_MAP',
    ql_expression = 'has_disaster_map == "是" ? 1 : 0',
    output_param = 'HAS_DISASTER_MAP'
WHERE step_id = @step1_id AND algorithm_order = 4;

-- 算法5-13：数值字段保持原值
UPDATE step_algorithm SET 
    algorithm_name = 'Resident Population',
    algorithm_code = 'RESIDENT_POPULATION_RAW',
    ql_expression = 'resident_population != null ? resident_population : 0',
    output_param = 'RESIDENT_POPULATION'
WHERE step_id = @step1_id AND algorithm_order = 5;

UPDATE step_algorithm SET 
    algorithm_name = 'Funding Amount',
    algorithm_code = 'FUNDING_AMOUNT_RAW',
    ql_expression = 'last_year_funding_amount != null ? last_year_funding_amount : 0.0',
    output_param = 'FUNDING_AMOUNT'
WHERE step_id = @step1_id AND algorithm_order = 6;

UPDATE step_algorithm SET 
    algorithm_name = 'Materials Value',
    algorithm_code = 'MATERIALS_VALUE_RAW',
    ql_expression = 'materials_equipment_value != null ? materials_equipment_value : 0.0',
    output_param = 'MATERIALS_VALUE'
WHERE step_id = @step1_id AND algorithm_order = 7;

UPDATE step_algorithm SET 
    algorithm_name = 'Medical Service Count',
    algorithm_code = 'MEDICAL_SERVICE_COUNT_RAW',
    ql_expression = 'medical_service_count != null ? medical_service_count : 0',
    output_param = 'MEDICAL_SERVICE_COUNT'
WHERE step_id = @step1_id AND algorithm_order = 8;

UPDATE step_algorithm SET 
    algorithm_name = 'Militia Reserve Count',
    algorithm_code = 'MILITIA_RESERVE_COUNT_RAW',
    ql_expression = 'militia_reserve_count != null ? militia_reserve_count : 0',
    output_param = 'MILITIA_RESERVE_COUNT'
WHERE step_id = @step1_id AND algorithm_order = 9;

UPDATE step_algorithm SET 
    algorithm_name = 'Volunteer Count',
    algorithm_code = 'VOLUNTEER_COUNT_RAW',
    ql_expression = 'registered_volunteer_count != null ? registered_volunteer_count : 0',
    output_param = 'VOLUNTEER_COUNT'
WHERE step_id = @step1_id AND algorithm_order = 10;

UPDATE step_algorithm SET 
    algorithm_name = 'Training Participants',
    algorithm_code = 'TRAINING_PARTICIPANTS_RAW',
    ql_expression = 'last_year_training_participants != null ? last_year_training_participants : 0',
    output_param = 'TRAINING_PARTICIPANTS'
WHERE step_id = @step1_id AND algorithm_order = 11;

UPDATE step_algorithm SET 
    algorithm_name = 'Drill Participants',
    algorithm_code = 'DRILL_PARTICIPANTS_RAW',
    ql_expression = 'last_year_drill_participants != null ? last_year_drill_participants : 0',
    output_param = 'DRILL_PARTICIPANTS'
WHERE step_id = @step1_id AND algorithm_order = 12;

UPDATE step_algorithm SET 
    algorithm_name = 'Shelter Capacity',
    algorithm_code = 'SHELTER_CAPACITY_RAW',
    ql_expression = 'emergency_shelter_capacity != null ? emergency_shelter_capacity : 0',
    output_param = 'SHELTER_CAPACITY'
WHERE step_id = @step1_id AND algorithm_order = 13;

SELECT '步骤1修改完成' AS status;

-- ========================================
-- 第二部分：修改步骤2 - 按乡镇聚合并计算能力值
-- ========================================

SELECT '' AS info;
SELECT '========================================' AS info;
SELECT '第二部分：修改步骤2为计算能力值' AS info;
SELECT '========================================' AS info;

SET @step2_id = (SELECT id FROM model_step WHERE model_id = 8 AND step_code = 'TOWNSHIP_AGGREGATION');

-- 删除现有算法
DELETE FROM step_algorithm WHERE step_id = @step2_id;

-- 算法1：预案建设能力 = SUM(HAS_EMERGENCY_PLAN) / COUNT(社区)
INSERT INTO step_algorithm (step_id, algorithm_order, algorithm_name, algorithm_code, ql_expression, output_param, status, create_time)
VALUES (@step2_id, 1, 'Plan Construction Capability', 'PLAN_CONSTRUCTION',
'@TOWNSHIP_AVG:HAS_EMERGENCY_PLAN', 'PLAN_CONSTRUCTION', 1, NOW());

-- 算法2：隐患排查能力 = (SUM(HAS_VULNERABLE_GROUPS_LIST) + SUM(HAS_DISASTER_POINTS_LIST)) / (COUNT(社区) * 2)
INSERT INTO step_algorithm (step_id, algorithm_order, algorithm_name, algorithm_code, ql_expression, output_param, status, create_time)
VALUES (@step2_id, 2, 'Hazard Inspection Capability', 'HAZARD_INSPECTION',
'@TOWNSHIP_HAZARD:HAS_VULNERABLE_GROUPS_LIST,HAS_DISASTER_POINTS_LIST', 'HAZARD_INSPECTION', 1, NOW());

-- 算法3：风险评估能力 = SUM(HAS_DISASTER_MAP) / COUNT(社区)
INSERT INTO step_algorithm (step_id, algorithm_order, algorithm_name, algorithm_code, ql_expression, output_param, status, create_time)
VALUES (@step2_id, 3, 'Risk Assessment Capability', 'RISK_ASSESSMENT',
'@TOWNSHIP_AVG:HAS_DISASTER_MAP', 'RISK_ASSESSMENT', 1, NOW());

-- 算法4：财政投入能力 = SUM(FUNDING_AMOUNT) / SUM(RESIDENT_POPULATION) * 10000
INSERT INTO step_algorithm (step_id, algorithm_order, algorithm_name, algorithm_code, ql_expression, output_param, status, create_time)
VALUES (@step2_id, 4, 'Financial Input Capability', 'FINANCIAL_INPUT',
'@TOWNSHIP_PER_CAPITA:FUNDING_AMOUNT,RESIDENT_POPULATION,10000', 'FINANCIAL_INPUT', 1, NOW());

-- 算法5：物资储备能力 = SUM(MATERIALS_VALUE) / SUM(RESIDENT_POPULATION) * 10000
INSERT INTO step_algorithm (step_id, algorithm_order, algorithm_name, algorithm_code, ql_expression, output_param, status, create_time)
VALUES (@step2_id, 5, 'Material Reserve Capability', 'MATERIAL_RESERVE',
'@TOWNSHIP_PER_CAPITA:MATERIALS_VALUE,RESIDENT_POPULATION,10000', 'MATERIAL_RESERVE', 1, NOW());

-- 算法6：医疗保障能力 = SUM(MEDICAL_SERVICE_COUNT) / SUM(RESIDENT_POPULATION) * 10000
INSERT INTO step_algorithm (step_id, algorithm_order, algorithm_name, algorithm_code, ql_expression, output_param, status, create_time)
VALUES (@step2_id, 6, 'Medical Support Capability', 'MEDICAL_SUPPORT',
'@TOWNSHIP_PER_CAPITA:MEDICAL_SERVICE_COUNT,RESIDENT_POPULATION,10000', 'MEDICAL_SUPPORT', 1, NOW());

-- 算法7：自救互救能力 = (SUM(MILITIA_RESERVE_COUNT) + SUM(VOLUNTEER_COUNT)) / SUM(RESIDENT_POPULATION) * 10000
INSERT INTO step_algorithm (step_id, algorithm_order, algorithm_name, algorithm_code, ql_expression, output_param, status, create_time)
VALUES (@step2_id, 7, 'Self Mutual Aid Capability', 'SELF_MUTUAL_AID',
'@TOWNSHIP_PER_CAPITA_SUM:MILITIA_RESERVE_COUNT,VOLUNTEER_COUNT,RESIDENT_POPULATION,10000', 'SELF_MUTUAL_AID', 1, NOW());

-- 算法8：公众避险能力 = (SUM(TRAINING_PARTICIPANTS) + SUM(DRILL_PARTICIPANTS)) / SUM(RESIDENT_POPULATION) * 100
INSERT INTO step_algorithm (step_id, algorithm_order, algorithm_name, algorithm_code, ql_expression, output_param, status, create_time)
VALUES (@step2_id, 8, 'Public Evacuation Capability', 'PUBLIC_EVACUATION',
'@TOWNSHIP_PER_CAPITA_SUM:TRAINING_PARTICIPANTS,DRILL_PARTICIPANTS,RESIDENT_POPULATION,100', 'PUBLIC_EVACUATION', 1, NOW());

-- 算法9：转移安置能力 = SUM(SHELTER_CAPACITY) / SUM(RESIDENT_POPULATION)
INSERT INTO step_algorithm (step_id, algorithm_order, algorithm_name, algorithm_code, ql_expression, output_param, status, create_time)
VALUES (@step2_id, 9, 'Relocation Shelter Capability', 'RELOCATION_SHELTER',
'@TOWNSHIP_PER_CAPITA:SHELTER_CAPACITY,RESIDENT_POPULATION,1', 'RELOCATION_SHELTER', 1, NOW());

SELECT '步骤2修改完成' AS status;

-- 验证
SELECT '' AS info;
SELECT '==== 验证结果 ====' AS info;

SELECT '步骤1算法数量：' AS label, COUNT(*) AS count FROM step_algorithm WHERE step_id = @step1_id;
SELECT '步骤2算法数量：' AS label, COUNT(*) AS count FROM step_algorithm WHERE step_id = @step2_id;

SELECT '' AS info;
SELECT '警告：步骤2使用了新的特殊标记，需要在代码中实现：' AS warning;
SELECT '@TOWNSHIP_AVG - 按乡镇求平均' AS marker;
SELECT '@TOWNSHIP_HAZARD - 隐患排查特殊计算' AS marker;
SELECT '@TOWNSHIP_PER_CAPITA - 按乡镇求和后除以总人口' AS marker;
SELECT '@TOWNSHIP_PER_CAPITA_SUM - 多个字段求和后除以总人口' AS marker;
