-- ============================================
-- 修复模型8步骤1的公式
-- 步骤1应该直接输出13个原始字段，不进行复杂计算
-- ============================================

USE evaluate_db;

-- 更新步骤1的13个算法，改为直接输出原始字段

-- 1. 是否有社区（行政村）应急预案（是为1，否为0）
UPDATE step_algorithm 
SET algorithm_name = '是否有社区（行政村）应急预案',
    output_param = 'HAS_EMERGENCY_PLAN',
    ql_expression = 'has_emergency_plan == "是" ? 1.0 : 0.0',
    description = '是为1，否为0'
WHERE id = 1737;

-- 2. 是否有本辖区弱势人群清单（是为1，否为0）
UPDATE step_algorithm 
SET algorithm_name = '是否有本辖区弱势人群清单',
    output_param = 'HAS_VULNERABLE_GROUPS_LIST',
    ql_expression = 'has_vulnerable_groups_list == "是" ? 1.0 : 0.0',
    description = '是为1，否为0'
WHERE id = 1738;

-- 3. 是否有本辖区地质灾害等隐患点清单（是为1，否为0）
UPDATE step_algorithm 
SET algorithm_name = '是否有本辖区地质灾害等隐患点清单',
    output_param = 'HAS_DISASTER_POINTS_LIST',
    ql_expression = 'has_disaster_points_list == "是" ? 1.0 : 0.0',
    description = '是为1，否为0'
WHERE id = 1739;

-- 4. 是否有社区（行政村）灾害类地图（是为1，否为0）
UPDATE step_algorithm 
SET algorithm_name = '是否有社区（行政村）灾害类地图',
    output_param = 'HAS_DISASTER_MAP',
    ql_expression = 'has_disaster_map == "是" ? 1.0 : 0.0',
    description = '是为1，否为0'
WHERE id = 1740;

-- 5. 常住人口数量（人）
UPDATE step_algorithm 
SET algorithm_name = '常住人口数量',
    output_param = 'RESIDENT_POPULATION',
    ql_expression = 'resident_population != null ? resident_population : 0',
    description = '常住人口数量（人）'
WHERE id = 1741;

-- 6. 上一年度防灾减灾救灾资金投入总金额（万元）
UPDATE step_algorithm 
SET algorithm_name = '上一年度防灾减灾救灾资金投入总金额',
    output_param = 'LAST_YEAR_FUNDING_AMOUNT',
    ql_expression = 'last_year_funding_amount != null ? last_year_funding_amount : 0.0',
    description = '上一年度防灾减灾救灾资金投入总金额（万元）'
WHERE id = 1742;

-- 7. 现有储备物资、装备折合金额（万元）
UPDATE step_algorithm 
SET algorithm_name = '现有储备物资、装备折合金额',
    output_param = 'MATERIALS_EQUIPMENT_VALUE',
    ql_expression = 'materials_equipment_value != null ? materials_equipment_value : 0.0',
    description = '现有储备物资、装备折合金额（万元）'
WHERE id = 1743;

-- 8. 社区医疗卫生服务站或村卫生室数量（个）
UPDATE step_algorithm 
SET algorithm_name = '社区医疗卫生服务站或村卫生室数量',
    output_param = 'MEDICAL_SERVICE_COUNT',
    ql_expression = 'medical_service_count != null ? medical_service_count : 0',
    description = '社区医疗卫生服务站或村卫生室数量（个）'
WHERE id = 1744;

-- 9. 民兵预备役人数（人）
UPDATE step_algorithm 
SET algorithm_name = '民兵预备役人数',
    output_param = 'MILITIA_RESERVE_COUNT',
    ql_expression = 'militia_reserve_count != null ? militia_reserve_count : 0',
    description = '民兵预备役人数（人）'
WHERE id = 1745;

-- 10. 登记注册志愿者人数（人）
UPDATE step_algorithm 
SET algorithm_name = '登记注册志愿者人数',
    output_param = 'REGISTERED_VOLUNTEER_COUNT',
    ql_expression = 'registered_volunteer_count != null ? registered_volunteer_count : 0',
    description = '登记注册志愿者人数（人）'
WHERE id = 1839;

-- 11. 上一年度防灾减灾培训活动培训人次（人次）
UPDATE step_algorithm 
SET algorithm_name = '上一年度防灾减灾培训活动培训人次',
    output_param = 'LAST_YEAR_TRAINING_PARTICIPANTS',
    ql_expression = 'last_year_training_participants != null ? last_year_training_participants : 0',
    description = '上一年度防灾减灾培训活动培训人次（人次）'
WHERE id = 1840;

-- 12. 参与上一年度组织的防灾减灾演练活动的居民（人次）
UPDATE step_algorithm 
SET algorithm_name = '参与上一年度组织的防灾减灾演练活动的居民',
    output_param = 'LAST_YEAR_DRILL_PARTICIPANTS',
    ql_expression = 'last_year_drill_participants != null ? last_year_drill_participants : 0',
    description = '参与上一年度组织的防灾减灾演练活动的居民（人次）'
WHERE id = 1841;

-- 13. 本级灾害应急避难场所容量（人）
UPDATE step_algorithm 
SET algorithm_name = '本级灾害应急避难场所容量',
    output_param = 'EMERGENCY_SHELTER_CAPACITY',
    ql_expression = 'emergency_shelter_capacity != null ? emergency_shelter_capacity : 0',
    description = '本级灾害应急避难场所容量（人）'
WHERE id = 1842;

-- 验证更新结果
SELECT 
    algorithm_order,
    algorithm_name,
    output_param,
    ql_expression,
    description
FROM step_algorithm 
WHERE step_id = 50 
ORDER BY algorithm_order;

-- 说明：
-- 步骤1现在直接输出13个原始字段：
-- 1-4: 是否字段（是为1，否为0）
-- 5-13: 数值字段（直接输出原始值）
-- 
-- 这些原始字段将在步骤2进行乡镇聚合
-- 步骤3及后续步骤将使用聚合后的数据进行TOPSIS计算
