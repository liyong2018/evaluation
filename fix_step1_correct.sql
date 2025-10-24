-- 根据algorithm_order正确更新步骤1的13个算法

USE evaluate_db;

-- 获取正确的ID
SET @id1 = (SELECT id FROM step_algorithm WHERE step_id = 50 AND algorithm_order = 1);
SET @id2 = (SELECT id FROM step_algorithm WHERE step_id = 50 AND algorithm_order = 2);
SET @id3 = (SELECT id FROM step_algorithm WHERE step_id = 50 AND algorithm_order = 3);
SET @id4 = (SELECT id FROM step_algorithm WHERE step_id = 50 AND algorithm_order = 4);
SET @id5 = (SELECT id FROM step_algorithm WHERE step_id = 50 AND algorithm_order = 5);
SET @id6 = (SELECT id FROM step_algorithm WHERE step_id = 50 AND algorithm_order = 6);
SET @id7 = (SELECT id FROM step_algorithm WHERE step_id = 50 AND algorithm_order = 7);
SET @id8 = (SELECT id FROM step_algorithm WHERE step_id = 50 AND algorithm_order = 8);
SET @id9 = (SELECT id FROM step_algorithm WHERE step_id = 50 AND algorithm_order = 9);
SET @id10 = (SELECT id FROM step_algorithm WHERE step_id = 50 AND algorithm_order = 10);
SET @id11 = (SELECT id FROM step_algorithm WHERE step_id = 50 AND algorithm_order = 11);
SET @id12 = (SELECT id FROM step_algorithm WHERE step_id = 50 AND algorithm_order = 12);
SET @id13 = (SELECT id FROM step_algorithm WHERE step_id = 50 AND algorithm_order = 13);

-- 1. 是否有社区（行政村）应急预案
UPDATE step_algorithm SET 
    algorithm_name = '是否有社区（行政村）应急预案',
    output_param = 'HAS_EMERGENCY_PLAN',
    ql_expression = 'has_emergency_plan == "是" ? 1.0 : 0.0'
WHERE id = @id1;

-- 2. 是否有本辖区弱势人群清单
UPDATE step_algorithm SET 
    algorithm_name = '是否有本辖区弱势人群清单',
    output_param = 'HAS_VULNERABLE_GROUPS_LIST',
    ql_expression = 'has_vulnerable_groups_list == "是" ? 1.0 : 0.0'
WHERE id = @id2;

-- 3. 是否有本辖区地质灾害等隐患点清单
UPDATE step_algorithm SET 
    algorithm_name = '是否有本辖区地质灾害等隐患点清单',
    output_param = 'HAS_DISASTER_POINTS_LIST',
    ql_expression = 'has_disaster_points_list == "是" ? 1.0 : 0.0'
WHERE id = @id3;

-- 4. 是否有社区（行政村）灾害类地图
UPDATE step_algorithm SET 
    algorithm_name = '是否有社区（行政村）灾害类地图',
    output_param = 'HAS_DISASTER_MAP',
    ql_expression = 'has_disaster_map == "是" ? 1.0 : 0.0'
WHERE id = @id4;

-- 5. 常住人口数量
UPDATE step_algorithm SET 
    algorithm_name = '常住人口数量',
    output_param = 'RESIDENT_POPULATION',
    ql_expression = 'resident_population != null ? resident_population : 0'
WHERE id = @id5;

-- 6. 上一年度防灾减灾救灾资金投入总金额
UPDATE step_algorithm SET 
    algorithm_name = '上一年度防灾减灾救灾资金投入总金额',
    output_param = 'LAST_YEAR_FUNDING_AMOUNT',
    ql_expression = 'last_year_funding_amount != null ? last_year_funding_amount : 0.0'
WHERE id = @id6;

-- 7. 现有储备物资、装备折合金额
UPDATE step_algorithm SET 
    algorithm_name = '现有储备物资、装备折合金额',
    output_param = 'MATERIALS_EQUIPMENT_VALUE',
    ql_expression = 'materials_equipment_value != null ? materials_equipment_value : 0.0'
WHERE id = @id7;

-- 8. 社区医疗卫生服务站或村卫生室数量
UPDATE step_algorithm SET 
    algorithm_name = '社区医疗卫生服务站或村卫生室数量',
    output_param = 'MEDICAL_SERVICE_COUNT',
    ql_expression = 'medical_service_count != null ? medical_service_count : 0'
WHERE id = @id8;

-- 9. 民兵预备役人数
UPDATE step_algorithm SET 
    algorithm_name = '民兵预备役人数',
    output_param = 'MILITIA_RESERVE_COUNT',
    ql_expression = 'militia_reserve_count != null ? militia_reserve_count : 0'
WHERE id = @id9;

-- 10. 登记注册志愿者人数
UPDATE step_algorithm SET 
    algorithm_name = '登记注册志愿者人数',
    output_param = 'REGISTERED_VOLUNTEER_COUNT',
    ql_expression = 'registered_volunteer_count != null ? registered_volunteer_count : 0'
WHERE id = @id10;

-- 11. 上一年度防灾减灾培训活动培训人次
UPDATE step_algorithm SET 
    algorithm_name = '上一年度防灾减灾培训活动培训人次',
    output_param = 'LAST_YEAR_TRAINING_PARTICIPANTS',
    ql_expression = 'last_year_training_participants != null ? last_year_training_participants : 0'
WHERE id = @id11;

-- 12. 参与上一年度组织的防灾减灾演练活动的居民
UPDATE step_algorithm SET 
    algorithm_name = '参与上一年度组织的防灾减灾演练活动的居民',
    output_param = 'LAST_YEAR_DRILL_PARTICIPANTS',
    ql_expression = 'last_year_drill_participants != null ? last_year_drill_participants : 0'
WHERE id = @id12;

-- 13. 本级灾害应急避难场所容量
UPDATE step_algorithm SET 
    algorithm_name = '本级灾害应急避难场所容量',
    output_param = 'EMERGENCY_SHELTER_CAPACITY',
    ql_expression = 'emergency_shelter_capacity != null ? emergency_shelter_capacity : 0'
WHERE id = @id13;

-- 验证
SELECT algorithm_order, algorithm_name, output_param, ql_expression
FROM step_algorithm 
WHERE step_id = 50 
ORDER BY algorithm_order;
