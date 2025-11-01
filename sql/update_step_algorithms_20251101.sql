-- 更新二级定权、TOPSIS计算与能力值分级公式
-- 执行前请先备份 step_algorithm 表

-- 步骤3：定权
UPDATE step_algorithm
SET
    ql_expression = 'TOWNSHIP_MGMT_NORM * 0.53 * 0.33',
    input_params = 'TOWNSHIP_MGMT_NORM',
    output_param = 'TOWNSHIP_MGMT_WEIGHTED',
    description = '归一化值 × 一级权重(0.53) × 二级权重(0.33)',
    status = 1,
    update_time = NOW()
WHERE algorithm_code = 'WEIGHT_TOWNSHIP_MGMT';

UPDATE step_algorithm
SET
    ql_expression = 'TOWNSHIP_PREP_NORM * 0.53 * 0.32',
    input_params = 'TOWNSHIP_PREP_NORM',
    output_param = 'TOWNSHIP_PREP_WEIGHTED',
    description = '归一化值 × 一级权重(0.53) × 二级权重(0.32)',
    status = 1,
    update_time = NOW()
WHERE algorithm_code = 'WEIGHT_TOWNSHIP_PREP';

UPDATE step_algorithm
SET
    ql_expression = 'TOWNSHIP_RESCUE_NORM * 0.53 * 0.35',
    input_params = 'TOWNSHIP_RESCUE_NORM',
    output_param = 'TOWNSHIP_RESCUE_WEIGHTED',
    description = '归一化值 × 一级权重(0.53) × 二级权重(0.35)',
    status = 1,
    update_time = NOW()
WHERE algorithm_code = 'WEIGHT_TOWNSHIP_RESCUE';

UPDATE step_algorithm
SET
    ql_expression = 'COMMUNITY_MGMT_NORM * 0.47 * 0.32',
    input_params = 'COMMUNITY_MGMT_NORM',
    output_param = 'COMMUNITY_MGMT_WEIGHTED',
    description = '归一化值 × 一级权重(0.47) × 二级权重(0.32)',
    status = 1,
    update_time = NOW()
WHERE algorithm_code = 'WEIGHT_COMMUNITY_MGMT';

UPDATE step_algorithm
SET
    ql_expression = 'COMMUNITY_PREP_NORM * 0.47 * 0.31',
    input_params = 'COMMUNITY_PREP_NORM',
    output_param = 'COMMUNITY_PREP_WEIGHTED',
    description = '归一化值 × 一级权重(0.47) × 二级权重(0.31)',
    status = 1,
    update_time = NOW()
WHERE algorithm_code = 'WEIGHT_COMMUNITY_PREP';

UPDATE step_algorithm
SET
    ql_expression = 'COMMUNITY_RESCUE_NORM * 0.47 * 0.37',
    input_params = 'COMMUNITY_RESCUE_NORM',
    output_param = 'COMMUNITY_RESCUE_WEIGHTED',
    description = '归一化值 × 一级权重(0.47) × 二级权重(0.37)',
    status = 1,
    update_time = NOW()
WHERE algorithm_code = 'WEIGHT_COMMUNITY_RESCUE';


-- 步骤4：TOPSIS 优劣解与得分
UPDATE step_algorithm
SET
    ql_expression = '@TOPSIS_POSITIVE:TOWNSHIP_MGMT_WEIGHTED,COMMUNITY_MGMT_WEIGHTED',
    input_params = 'TOWNSHIP_MGMT_WEIGHTED,COMMUNITY_MGMT_WEIGHTED',
    output_param = 'MGMT_POSITIVE_DISTANCE',
    description = '计算灾害管理能力的优解距离',
    status = 1,
    update_time = NOW()
WHERE algorithm_code = 'MGMT_POSITIVE';

UPDATE step_algorithm
SET
    ql_expression = '@TOPSIS_NEGATIVE:TOWNSHIP_MGMT_WEIGHTED,COMMUNITY_MGMT_WEIGHTED',
    input_params = 'TOWNSHIP_MGMT_WEIGHTED,COMMUNITY_MGMT_WEIGHTED',
    output_param = 'MGMT_NEGATIVE_DISTANCE',
    description = '计算灾害管理能力的劣解距离',
    status = 1,
    update_time = NOW()
WHERE algorithm_code = 'MGMT_NEGATIVE';

UPDATE step_algorithm
SET
    ql_expression = 'MGMT_NEGATIVE_DISTANCE / (MGMT_NEGATIVE_DISTANCE + MGMT_POSITIVE_DISTANCE)',
    input_params = 'MGMT_NEGATIVE_DISTANCE,MGMT_POSITIVE_DISTANCE',
    output_param = 'managementScore',
    description = '灾害管理能力得分=劣解距离/(劣解距离+优解距离)',
    status = 1,
    update_time = NOW()
WHERE algorithm_code = 'MGMT_SCORE';

UPDATE step_algorithm
SET
    ql_expression = '@TOPSIS_POSITIVE:TOWNSHIP_PREP_WEIGHTED,COMMUNITY_PREP_WEIGHTED',
    input_params = 'TOWNSHIP_PREP_WEIGHTED,COMMUNITY_PREP_WEIGHTED',
    output_param = 'PREP_POSITIVE_DISTANCE',
    description = '计算灾害备灾能力的优解距离',
    status = 1,
    update_time = NOW()
WHERE algorithm_code = 'PREP_POSITIVE';

UPDATE step_algorithm
SET
    ql_expression = '@TOPSIS_NEGATIVE:TOWNSHIP_PREP_WEIGHTED,COMMUNITY_PREP_WEIGHTED',
    input_params = 'TOWNSHIP_PREP_WEIGHTED,COMMUNITY_PREP_WEIGHTED',
    output_param = 'PREP_NEGATIVE_DISTANCE',
    description = '计算灾害备灾能力的劣解距离',
    status = 1,
    update_time = NOW()
WHERE algorithm_code = 'PREP_NEGATIVE';

UPDATE step_algorithm
SET
    ql_expression = 'PREP_NEGATIVE_DISTANCE / (PREP_NEGATIVE_DISTANCE + PREP_POSITIVE_DISTANCE)',
    input_params = 'PREP_NEGATIVE_DISTANCE,PREP_POSITIVE_DISTANCE',
    output_param = 'preparednessScore',
    description = '灾害备灾能力得分=劣解距离/(劣解距离+优解距离)',
    status = 1,
    update_time = NOW()
WHERE algorithm_code = 'PREP_SCORE';

UPDATE step_algorithm
SET
    ql_expression = '@TOPSIS_POSITIVE:TOWNSHIP_RESCUE_WEIGHTED,COMMUNITY_RESCUE_WEIGHTED',
    input_params = 'TOWNSHIP_RESCUE_WEIGHTED,COMMUNITY_RESCUE_WEIGHTED',
    output_param = 'RESCUE_POSITIVE_DISTANCE',
    description = '计算自救转移能力的优解距离',
    status = 1,
    update_time = NOW()
WHERE algorithm_code = 'RESCUE_POSITIVE';

UPDATE step_algorithm
SET
    ql_expression = '@TOPSIS_NEGATIVE:TOWNSHIP_RESCUE_WEIGHTED,COMMUNITY_RESCUE_WEIGHTED',
    input_params = 'TOWNSHIP_RESCUE_WEIGHTED,COMMUNITY_RESCUE_WEIGHTED',
    output_param = 'RESCUE_NEGATIVE_DISTANCE',
    description = '计算自救转移能力的劣解距离',
    status = 1,
    update_time = NOW()
WHERE algorithm_code = 'RESCUE_NEGATIVE';

UPDATE step_algorithm
SET
    ql_expression = 'RESCUE_NEGATIVE_DISTANCE / (RESCUE_NEGATIVE_DISTANCE + RESCUE_POSITIVE_DISTANCE)',
    input_params = 'RESCUE_NEGATIVE_DISTANCE,RESCUE_POSITIVE_DISTANCE',
    output_param = 'rescueScore',
    description = '自救转移能力得分=劣解距离/(劣解距离+优解距离)',
    status = 1,
    update_time = NOW()
WHERE algorithm_code = 'RESCUE_SCORE';

UPDATE step_algorithm
SET
    ql_expression = '@TOPSIS_POSITIVE:TOWNSHIP_MGMT_WEIGHTED,TOWNSHIP_PREP_WEIGHTED,TOWNSHIP_RESCUE_WEIGHTED,COMMUNITY_MGMT_WEIGHTED,COMMUNITY_PREP_WEIGHTED,COMMUNITY_RESCUE_WEIGHTED',
    input_params = 'TOWNSHIP_MGMT_WEIGHTED,TOWNSHIP_PREP_WEIGHTED,TOWNSHIP_RESCUE_WEIGHTED,COMMUNITY_MGMT_WEIGHTED,COMMUNITY_PREP_WEIGHTED,COMMUNITY_RESCUE_WEIGHTED',
    output_param = 'COMPREHENSIVE_POSITIVE_DISTANCE',
    description = '计算综合减灾能力的优解距离（6个指标）',
    status = 1,
    update_time = NOW()
WHERE algorithm_code = 'COMPREHENSIVE_POSITIVE';

UPDATE step_algorithm
SET
    ql_expression = '@TOPSIS_NEGATIVE:TOWNSHIP_MGMT_WEIGHTED,TOWNSHIP_PREP_WEIGHTED,TOWNSHIP_RESCUE_WEIGHTED,COMMUNITY_MGMT_WEIGHTED,COMMUNITY_PREP_WEIGHTED,COMMUNITY_RESCUE_WEIGHTED',
    input_params = 'TOWNSHIP_MGMT_WEIGHTED,TOWNSHIP_PREP_WEIGHTED,TOWNSHIP_RESCUE_WEIGHTED,COMMUNITY_MGMT_WEIGHTED,COMMUNITY_PREP_WEIGHTED,COMMUNITY_RESCUE_WEIGHTED',
    output_param = 'COMPREHENSIVE_NEGATIVE_DISTANCE',
    description = '计算综合减灾能力的劣解距离（6个指标）',
    status = 1,
    update_time = NOW()
WHERE algorithm_code = 'COMPREHENSIVE_NEGATIVE';

UPDATE step_algorithm
SET
    ql_expression = 'COMPREHENSIVE_NEGATIVE_DISTANCE / (COMPREHENSIVE_NEGATIVE_DISTANCE + COMPREHENSIVE_POSITIVE_DISTANCE)',
    input_params = 'COMPREHENSIVE_NEGATIVE_DISTANCE,COMPREHENSIVE_POSITIVE_DISTANCE',
    output_param = 'comprehensiveScore',
    description = '综合减灾能力得分=劣解距离/(劣解距离+优解距离)',
    status = 1,
    update_time = NOW()
WHERE algorithm_code = 'COMPREHENSIVE_SCORE';


-- 步骤5：能力等级划分
UPDATE step_algorithm
SET
    ql_expression = '@GRADE:managementScore',
    input_params = 'managementScore',
    output_param = 'managementGrade',
    description = '按均值±标准差(五级)划分灾害管理能力等级',
    status = 1,
    update_time = NOW()
WHERE algorithm_code = 'MGMT_GRADE';

UPDATE step_algorithm
SET
    ql_expression = '@GRADE:preparednessScore',
    input_params = 'preparednessScore',
    output_param = 'preparednessGrade',
    description = '按均值±标准差(五级)划分灾害备灾能力等级',
    status = 1,
    update_time = NOW()
WHERE algorithm_code = 'PREP_GRADE';

UPDATE step_algorithm
SET
    ql_expression = '@GRADE:rescueScore',
    input_params = 'rescueScore',
    output_param = 'rescueGrade',
    description = '按均值±标准差(五级)划分自救转移能力等级',
    status = 1,
    update_time = NOW()
WHERE algorithm_code = 'RESCUE_GRADE';

UPDATE step_algorithm
SET
    ql_expression = '@GRADE:comprehensiveScore',
    input_params = 'comprehensiveScore',
    output_param = 'comprehensiveGrade',
    description = '按均值±标准差(五级)划分综合减灾能力等级',
    status = 1,
    update_time = NOW()
WHERE algorithm_code = 'COMPREHENSIVE_GRADE';
