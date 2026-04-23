-- 修复 Model 12（市州-政府减灾能力评估）步骤1配置
-- 目标：
-- 1. 删除旧步骤1（原始字段直出）
-- 2. 将旧步骤2提升为新步骤1（输出规范要求的二次计算指标）
-- 3. 顺延后续步骤顺序
-- 4. 修正专业队伍救援中森林消防/航空护林的口径为“按森林面积”

START TRANSACTION;

SET @model_id = 12;
SET @old_step1_id = (
    SELECT id FROM model_step
    WHERE model_id = @model_id AND step_code = 'indicator_assignment'
    ORDER BY step_order
    LIMIT 1
);
SET @old_step2_id = (
    SELECT id FROM model_step
    WHERE model_id = @model_id AND step_code = 'secondary_indicator_assignment'
    ORDER BY step_order
    LIMIT 1
);

-- 1) 删除旧步骤1算法与步骤
DELETE FROM step_algorithm WHERE step_id = @old_step1_id;
DELETE FROM model_step WHERE id = @old_step1_id;

-- 2) 将旧步骤2提升为新步骤1
UPDATE model_step
SET step_name = '评估指标赋值',
    step_code = 'indicator_assignment',
    step_order = 1,
    step_type = 'CALCULATION',
    description = '按技术规范输出步骤1指标数值（经公式换算后）',
    input_variables = JSON_ARRAY(
        'management_staff',
        'expert_staff_count',
        'disaster_prevention_plan_count',
        'emergency_plan_count',
        'education_expenditure',
        'science_expenditure',
        'agriculture_water_expenditure',
        'natural_resources_expenditure',
        'grain_reserve_expenditure',
        'disaster_emergency_expenditure',
        'regional_gdp',
        'standard_flood_dike_length',
        'built_flood_dike_length',
        'reinforced_reservoir_dam_count',
        'reservoir_dam_count',
        'reinforced_sluice_count',
        'sluice_count',
        'geological_hazard_point_count',
        'completed_geological_treatment_count',
        'seawall_total_length',
        'coastline_length',
        'forest_fire_project_mileage',
        'forest_area',
        'regional_area',
        'meteorological_station_count',
        'hydrological_station_count',
        'seismic_station_count',
        'geological_monitoring_station_count',
        'ocean_monitoring_station_count',
        'forest_fire_warning_station_count',
        'population',
        'effective_storage_capacity',
        'living_material_value',
        'rescue_material_value',
        'other_material_value',
        'firefighters',
        'fire_station_count',
        'fire_truck_count',
        'forest_fire_team_personnel',
        'forest_fire_vehicle_vessel_count',
        'aviation_rescue_team_personnel',
        'fixed_wing_aircraft_count',
        'helicopter_count',
        'earthquake_rescue_team_personnel',
        'detection_equipment_total',
        'search_equipment_total',
        'rescue_equipment_total',
        'medical_equipment_total',
        'communication_equipment_total',
        'information_equipment_total',
        'logistics_equipment_total',
        'vehicle_equipment_total',
        'mine_tunnel_rescue_personnel',
        'mine_tunnel_enterprise_count',
        'drill_machine_count',
        'drainage_equipment_count',
        'mobile_drainage_power_equipment_count',
        'rapid_fire_suppression_equipment_count',
        'detection_prospecting_equipment_count',
        'rapid_support_equipment_count',
        'large_offroad_crane_count',
        'mine_tunnel_satcom_command_vehicle_count',
        'hazchem_oilgas_team_personnel',
        'hazchem_oilgas_enterprise_count',
        'aerial_ladder_jet_vehicle_count',
        'heavy_foam_fire_truck_count',
        'foam_tanker_count',
        'turbojet_fire_truck_count',
        'foam_supply_truck_count',
        'dry_powder_fire_truck_count',
        'engineering_leak_blocking_vehicle_count',
        'breaking_tools_count',
        'leak_blocking_tools_count',
        'gas_supply_fire_truck_count',
        'long_distance_water_supply_vehicle_count',
        'aerial_triphase_jet_fire_truck_count',
        'chemical_decon_vehicle_count',
        'large_flow_trailer_fire_cannon_count',
        'hazchem_oilgas_satcom_command_vehicle_count',
        'maritime_rescue_team_personnel',
        'inflatable_boat_count',
        'assault_boat_count',
        'salvage_ship_count',
        'maritime_rescue_helicopter_count',
        'inflatable_board_count',
        'water_robot_count',
        'drone_count',
        'hospital_beds',
        'health_technicians_total',
        'transport_ambulance_count',
        'monitoring_ambulance_count',
        'negative_pressure_ambulance_count',
        'emergency_comm_base_station_count',
        'emergency_comm_vehicle_count',
        'shelter_capacity',
        'road_total_mileage'
    ),
    depends_on = ''
WHERE id = @old_step2_id;

-- 3) 顺延后续步骤顺序
UPDATE model_step
SET step_order = 2
WHERE model_id = @model_id AND step_code = 'attribute_vector_normalization';

UPDATE model_step
SET step_order = 3
WHERE model_id = @model_id AND step_code = 'secondary_indicator_weighting';

UPDATE model_step
SET step_order = 4
WHERE model_id = @model_id AND step_code = 'distance_to_ideal';

UPDATE model_step
SET step_order = 5
WHERE model_id = @model_id AND step_code = 'primary_indicator_value';

UPDATE model_step
SET step_order = 6
WHERE model_id = @model_id AND step_code = 'primary_indicator_level';

-- 4) 修正步骤1算法说明
UPDATE step_algorithm
SET algorithm_name = '管理队伍人数',
    description = '步骤1指标赋值: 管理队伍总人数'
WHERE step_id = @old_step2_id AND algorithm_code = 'EL_SECONDARY_001';

UPDATE step_algorithm
SET algorithm_name = '专家队伍人数',
    description = '步骤1指标赋值: 专家队伍总人数'
WHERE step_id = @old_step2_id AND algorithm_code = 'EL_SECONDARY_002';

UPDATE step_algorithm
SET algorithm_name = '防灾减灾规划',
    description = '步骤1指标赋值: 防灾减灾规划总数'
WHERE step_id = @old_step2_id AND algorithm_code = 'EL_SECONDARY_003';

UPDATE step_algorithm
SET algorithm_name = '应急预案数量',
    description = '步骤1指标赋值: 应急预案总数'
WHERE step_id = @old_step2_id AND algorithm_code = 'EL_SECONDARY_004';

UPDATE step_algorithm
SET algorithm_name = '防灾减灾投入',
    description = '步骤1指标赋值: 防灾减灾投入总金额/区域GDP'
WHERE step_id = @old_step2_id AND algorithm_code = 'EL_SECONDARY_005';

UPDATE step_algorithm
SET algorithm_name = '工程防洪能力',
    description = '步骤1指标赋值: 工程防洪能力'
WHERE step_id = @old_step2_id AND algorithm_code = 'EL_SECONDARY_006';

UPDATE step_algorithm
SET algorithm_name = '地质灾害的防治工程比例',
    description = '步骤1指标赋值: 地质灾害的防治工程比例'
WHERE step_id = @old_step2_id AND algorithm_code = 'EL_SECONDARY_007';

UPDATE step_algorithm
SET algorithm_name = '海堤工程长度比例',
    description = '步骤1指标赋值: 海堤工程长度比例'
WHERE step_id = @old_step2_id AND algorithm_code = 'EL_SECONDARY_008';

UPDATE step_algorithm
SET algorithm_name = '林区防火阻隔和防火道路网密度',
    description = '步骤1指标赋值: 林区防火阻隔和防火道路网密度'
WHERE step_id = @old_step2_id AND algorithm_code = 'EL_SECONDARY_009';

UPDATE step_algorithm
SET algorithm_name = '气象站点密度',
    description = '步骤1指标赋值: 气象站点密度'
WHERE step_id = @old_step2_id AND algorithm_code = 'EL_SECONDARY_010';

UPDATE step_algorithm
SET algorithm_name = '水文站点密度',
    description = '步骤1指标赋值: 水文站点密度'
WHERE step_id = @old_step2_id AND algorithm_code = 'EL_SECONDARY_011';

UPDATE step_algorithm
SET algorithm_name = '地震台网监测点密度',
    description = '步骤1指标赋值: 地震台网监测点密度'
WHERE step_id = @old_step2_id AND algorithm_code = 'EL_SECONDARY_012';

UPDATE step_algorithm
SET algorithm_name = '地质灾害监测点比例',
    description = '步骤1指标赋值: 地质灾害监测点比例'
WHERE step_id = @old_step2_id AND algorithm_code = 'EL_SECONDARY_013';

UPDATE step_algorithm
SET algorithm_name = '海洋灾害监测点密度',
    description = '步骤1指标赋值: 海洋灾害监测点密度'
WHERE step_id = @old_step2_id AND algorithm_code = 'EL_SECONDARY_014';

UPDATE step_algorithm
SET algorithm_name = '林草防火监测预警站点密度',
    description = '步骤1指标赋值: 林草防火监测预警站点密度'
WHERE step_id = @old_step2_id AND algorithm_code = 'EL_SECONDARY_015';

UPDATE step_algorithm
SET algorithm_name = '人均储备库容率',
    description = '步骤1指标赋值: 人均储备库容率'
WHERE step_id = @old_step2_id AND algorithm_code = 'EL_SECONDARY_016';

UPDATE step_algorithm
SET algorithm_name = '人均救援物资储备率',
    description = '步骤1指标赋值: 人均救援物资储备率'
WHERE step_id = @old_step2_id AND algorithm_code = 'EL_SECONDARY_017';

UPDATE step_algorithm
SET algorithm_name = '综合消防、政府与企事业专职消防救援能力',
    description = '步骤1指标赋值: 综合消防、政府与企事业专职消防救援能力'
WHERE step_id = @old_step2_id AND algorithm_code = 'EL_SECONDARY_018';

UPDATE step_algorithm
SET algorithm_name = '森林消防救援能力',
    description = '步骤1指标赋值: 森林消防救援能力'
WHERE step_id = @old_step2_id AND algorithm_code = 'EL_SECONDARY_019';

UPDATE step_algorithm
SET algorithm_name = '航空护林能力',
    description = '步骤1指标赋值: 航空护林能力'
WHERE step_id = @old_step2_id AND algorithm_code = 'EL_SECONDARY_020';

UPDATE step_algorithm
SET algorithm_name = '地震救援能力',
    description = '步骤1指标赋值: 地震救援能力'
WHERE step_id = @old_step2_id AND algorithm_code = 'EL_SECONDARY_021';

UPDATE step_algorithm
SET algorithm_name = '矿山/隧道救援能力',
    description = '步骤1指标赋值: 矿山/隧道救援能力'
WHERE step_id = @old_step2_id AND algorithm_code = 'EL_SECONDARY_022';

UPDATE step_algorithm
SET algorithm_name = '危化/油气救援能力',
    description = '步骤1指标赋值: 危化/油气救援能力'
WHERE step_id = @old_step2_id AND algorithm_code = 'EL_SECONDARY_023';

UPDATE step_algorithm
SET algorithm_name = '海事救援能力',
    description = '步骤1指标赋值: 海事救援能力'
WHERE step_id = @old_step2_id AND algorithm_code = 'EL_SECONDARY_024';

UPDATE step_algorithm
SET algorithm_name = '医疗救援能力',
    description = '步骤1指标赋值: 医疗救援能力'
WHERE step_id = @old_step2_id AND algorithm_code = 'EL_SECONDARY_025';

UPDATE step_algorithm
SET algorithm_name = '应急通信能力',
    description = '步骤1指标赋值: 应急通信能力'
WHERE step_id = @old_step2_id AND algorithm_code = 'EL_SECONDARY_026';

UPDATE step_algorithm
SET algorithm_name = '应急避难场所容纳率',
    description = '步骤1指标赋值: 应急避难场所容纳率'
WHERE step_id = @old_step2_id AND algorithm_code = 'EL_SECONDARY_027';

UPDATE step_algorithm
SET algorithm_name = '路网密度',
    description = '步骤1指标赋值: 路网密度'
WHERE step_id = @old_step2_id AND algorithm_code = 'EL_SECONDARY_028';

-- 5) 修正规范口径不一致的专业救援公式
UPDATE step_algorithm
SET ql_expression = '((forest_area == 0 ? 0 : forest_fire_team_personnel / forest_area) + (forest_area == 0 ? 0 : forest_fire_vehicle_vessel_count / forest_area)) / 2'
WHERE step_id = @old_step2_id AND algorithm_code = 'EL_SECONDARY_019';

UPDATE step_algorithm
SET ql_expression = '((forest_area == 0 ? 0 : aviation_rescue_team_personnel / forest_area) + (forest_area == 0 ? 0 : (fixed_wing_aircraft_count + helicopter_count) / forest_area)) / 2'
WHERE step_id = @old_step2_id AND algorithm_code = 'EL_SECONDARY_020';

UPDATE step_algorithm
SET algorithm_name = CASE algorithm_code
    WHEN 'EL_WEIGHTED_018' THEN '综合消防、政府与企事业专职消防救援能力定权'
    WHEN 'EL_WEIGHTED_019' THEN '森林消防救援能力定权'
    WHEN 'EL_WEIGHTED_020' THEN '航空护林能力定权'
    WHEN 'EL_WEIGHTED_021' THEN '地震救援能力定权'
    WHEN 'EL_WEIGHTED_022' THEN '矿山/隧道救援能力定权'
    WHEN 'EL_WEIGHTED_023' THEN '危化/油气救援能力定权'
    WHEN 'EL_WEIGHTED_024' THEN '海事救援能力定权'
    WHEN 'EL_WEIGHTED_025' THEN '医疗救援能力定权'
    WHEN 'EL_WEIGHTED_026' THEN '应急通信能力定权'
    ELSE algorithm_name
END,
description = '按Excel样例口径汇总专业救援能力，仅保留综合消防、森林消防、医疗救援、应急通信参与赋权',
ql_expression = CASE algorithm_code
    WHEN 'EL_WEIGHTED_018' THEN '(comprehensive_fire_rescue_capability * 0.15)'
    WHEN 'EL_WEIGHTED_019' THEN '(forest_fire_rescue_capability * 0.12)'
    WHEN 'EL_WEIGHTED_020' THEN '(aviation_forest_rescue_capability * 0.0)'
    WHEN 'EL_WEIGHTED_021' THEN '(earthquake_rescue_capability * 0.0)'
    WHEN 'EL_WEIGHTED_022' THEN '(mine_tunnel_rescue_capability * 0.0)'
    WHEN 'EL_WEIGHTED_023' THEN '(hazchem_oilgas_rescue_capability * 0.0)'
    WHEN 'EL_WEIGHTED_024' THEN '(maritime_rescue_capability * 0.0)'
    WHEN 'EL_WEIGHTED_025' THEN '(medical_rescue_capability * 0.14)'
    WHEN 'EL_WEIGHTED_026' THEN '(emergency_communication_capability * 0.14)'
    ELSE ql_expression
END
WHERE step_id = (
    SELECT id
    FROM model_step
    WHERE model_id = @model_id
      AND step_code = 'secondary_indicator_weighting'
    LIMIT 1
)
AND algorithm_code IN (
    'EL_WEIGHTED_018', 'EL_WEIGHTED_019', 'EL_WEIGHTED_020',
    'EL_WEIGHTED_021', 'EL_WEIGHTED_022', 'EL_WEIGHTED_023',
    'EL_WEIGHTED_024', 'EL_WEIGHTED_025', 'EL_WEIGHTED_026'
);

COMMIT;
