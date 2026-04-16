SET NAMES utf8mb4;
SET @model_id := (
    SELECT em.id
    FROM evaluation_model em
    WHERE em.status = 1
      AND em.model_name LIKE '%政府减灾能力%'
    ORDER BY em.id DESC
    LIMIT 1
);

SET @step5_id := (
    SELECT ms.id
    FROM model_step ms
    WHERE ms.model_id = @model_id
      AND ms.step_code = 'distance_to_ideal'
      AND ms.status = 1
    LIMIT 1
);

SET @step6_id := (
    SELECT ms.id
    FROM model_step ms
    WHERE ms.model_id = @model_id
      AND ms.step_code = 'primary_indicator_value'
      AND ms.status = 1
    LIMIT 1
);

SET @step7_id := (
    SELECT ms.id
    FROM model_step ms
    WHERE ms.model_id = @model_id
      AND ms.step_code = 'primary_indicator_level'
      AND ms.status = 1
    LIMIT 1
);

DELETE FROM step_algorithm
WHERE (step_id = @step5_id AND algorithm_code IN ('EL_D_PLUS_007', 'EL_D_MINUS_007'))
   OR (step_id = @step6_id AND algorithm_code IN ('EL_PRIMARY_SCORE_007'))
   OR (step_id = @step7_id AND algorithm_code IN ('EL_PRIMARY_GRADE_007'));

INSERT INTO step_algorithm (
    step_id,
    algorithm_name,
    algorithm_code,
    algorithm_order,
    ql_expression,
    input_params,
    output_param,
    description,
    status
)
SELECT
    @step5_id,
    '政府减灾能力D+公式',
    'EL_D_PLUS_007',
    13,
    '@TOPSIS_POSITIVE:management_staff_count,expert_staff_total,disaster_prevention_plan_total,emergency_plan_total,disaster_investment_ratio,flood_control_capability,geo_treatment_ratio,seawall_length_ratio,forest_firebreak_density,meteorological_station_density,hydrological_station_density,seismic_monitoring_capacity,geological_monitoring_density,ocean_monitoring_density,forest_warning_density,per_capita_storage_ratio,per_capita_rescue_material_ratio,comprehensive_fire_rescue_capability,forest_fire_rescue_capability,earthquake_rescue_capability,mine_tunnel_rescue_capability,hazchem_oilgas_rescue_capability,maritime_rescue_capability,medical_rescue_capability,emergency_communication_capability,shelter_capacity_rate,road_network_density',
    '{"required":["weighted_matrix"]}',
    'government_disaster_reduction_capability_d_plus',
    '计算政府减灾能力正理想解距离',
    1
FROM DUAL
WHERE @step5_id IS NOT NULL;

INSERT INTO step_algorithm (
    step_id,
    algorithm_name,
    algorithm_code,
    algorithm_order,
    ql_expression,
    input_params,
    output_param,
    description,
    status
)
SELECT
    @step5_id,
    '政府减灾能力D-公式',
    'EL_D_MINUS_007',
    14,
    '@TOPSIS_NEGATIVE:management_staff_count,expert_staff_total,disaster_prevention_plan_total,emergency_plan_total,disaster_investment_ratio,flood_control_capability,geo_treatment_ratio,seawall_length_ratio,forest_firebreak_density,meteorological_station_density,hydrological_station_density,seismic_monitoring_capacity,geological_monitoring_density,ocean_monitoring_density,forest_warning_density,per_capita_storage_ratio,per_capita_rescue_material_ratio,comprehensive_fire_rescue_capability,forest_fire_rescue_capability,earthquake_rescue_capability,mine_tunnel_rescue_capability,hazchem_oilgas_rescue_capability,maritime_rescue_capability,medical_rescue_capability,emergency_communication_capability,shelter_capacity_rate,road_network_density',
    '{"required":["weighted_matrix"]}',
    'government_disaster_reduction_capability_d_minus',
    '计算政府减灾能力负理想解距离',
    1
FROM DUAL
WHERE @step5_id IS NOT NULL;

INSERT INTO step_algorithm (
    step_id,
    algorithm_name,
    algorithm_code,
    algorithm_order,
    ql_expression,
    input_params,
    output_param,
    description,
    status
)
SELECT
    @step6_id,
    '政府减灾能力值公式',
    'EL_PRIMARY_SCORE_007',
    7,
    '@TOPSIS_SCORE:government_disaster_reduction_capability_d_plus,government_disaster_reduction_capability_d_minus',
    '{"required":["d_plus","d_minus"]}',
    'government_disaster_reduction_capability',
    '计算政府减灾能力值',
    1
FROM DUAL
WHERE @step6_id IS NOT NULL;

INSERT INTO step_algorithm (
    step_id,
    algorithm_name,
    algorithm_code,
    algorithm_order,
    ql_expression,
    input_params,
    output_param,
    description,
    status
)
SELECT
    @step7_id,
    '政府减灾能力分级公式',
    'EL_PRIMARY_GRADE_007',
    7,
    '@GRADE:government_disaster_reduction_capability',
    '{"required":["primary_indicator_values"]}',
    'government_disaster_reduction_capability',
    '政府减灾能力分级',
    1
FROM DUAL
WHERE @step7_id IS NOT NULL;

SELECT
    @model_id AS model_id,
    @step5_id AS step5_id,
    @step6_id AS step6_id,
    @step7_id AS step7_id;

SELECT
    sa.step_id,
    sa.algorithm_order,
    sa.algorithm_code,
    sa.algorithm_name,
    sa.output_param
FROM step_algorithm sa
WHERE sa.step_id IN (@step5_id, @step6_id, @step7_id)
  AND sa.algorithm_code IN (
      'EL_D_PLUS_007',
      'EL_D_MINUS_007',
      'EL_PRIMARY_SCORE_007',
      'EL_PRIMARY_GRADE_007'
  )
ORDER BY sa.step_id, sa.algorithm_order;
