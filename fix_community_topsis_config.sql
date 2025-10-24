-- 修复社区评估模型的TOPSIS步骤算法配置
-- 问题：算法名称错误、输入参数类型不一致、算法分组错误

-- 首先查看当前配置
SELECT '==== 修复前的TOPSIS配置 ====' AS info;
SELECT sa.algorithm_order, sa.algorithm_name, sa.algorithm_code, sa.output_param, sa.ql_expression
FROM step_algorithm sa
JOIN model_step ms ON sa.step_id = ms.id
WHERE ms.model_id = 4 AND ms.step_code = 'TOPSIS_DISTANCE'
ORDER BY sa.algorithm_order;

-- 更新算法1：灾害管理能力TOPSIS正理想解
UPDATE step_algorithm sa
JOIN model_step ms ON sa.step_id = ms.id
SET sa.algorithm_name = '灾害管理能力TOPSIS正理想解',
    sa.algorithm_code = 'MANAGEMENT_POSITIVE_IDEAL',
    sa.ql_expression = '@POSITIVE_IDEAL:PLAN_CONSTRUCTION_WEIGHT,HAZARD_INSPECTION_WEIGHT,RISK_ASSESSMENT_WEIGHT,FINANCIAL_INPUT_WEIGHT',
    sa.output_param = 'MANAGEMENT_POSITIVE_IDEAL'
WHERE ms.model_id = 4
  AND ms.step_code = 'TOPSIS_DISTANCE'
  AND sa.algorithm_order = 1;

-- 更新算法2：灾害管理能力TOPSIS负理想解（修正输入参数为_WEIGHT）
UPDATE step_algorithm sa
JOIN model_step ms ON sa.step_id = ms.id
SET sa.algorithm_name = '灾害管理能力TOPSIS负理想解',
    sa.algorithm_code = 'MANAGEMENT_NEGATIVE_IDEAL',
    sa.ql_expression = '@NEGATIVE_IDEAL:PLAN_CONSTRUCTION_WEIGHT,HAZARD_INSPECTION_WEIGHT,RISK_ASSESSMENT_WEIGHT,FINANCIAL_INPUT_WEIGHT',
    sa.output_param = 'MANAGEMENT_NEGATIVE_IDEAL'
WHERE ms.model_id = 4
  AND ms.step_code = 'TOPSIS_DISTANCE'
  AND sa.algorithm_order = 2;

-- 更新算法3：灾害备灾能力TOPSIS正理想解（修正输入参数为_WEIGHT）
UPDATE step_algorithm sa
JOIN model_step ms ON sa.step_id = ms.id
SET sa.algorithm_name = '灾害备灾能力TOPSIS正理想解',
    sa.algorithm_code = 'SUPPORT_POSITIVE_IDEAL',
    sa.ql_expression = '@POSITIVE_IDEAL:MATERIAL_RESERVE_WEIGHT,MEDICAL_SUPPORT_WEIGHT',
    sa.output_param = 'SUPPORT_POSITIVE_IDEAL'
WHERE ms.model_id = 4
  AND ms.step_code = 'TOPSIS_DISTANCE'
  AND sa.algorithm_order = 3;

-- 更新算法4：灾害备灾能力TOPSIS负理想解（修正输入参数为_WEIGHT）
UPDATE step_algorithm sa
JOIN model_step ms ON sa.step_id = ms.id
SET sa.algorithm_name = '灾害备灾能力TOPSIS负理想解',
    sa.algorithm_code = 'SUPPORT_NEGATIVE_IDEAL',
    sa.ql_expression = '@NEGATIVE_IDEAL:MATERIAL_RESERVE_WEIGHT,MEDICAL_SUPPORT_WEIGHT',
    sa.output_param = 'SUPPORT_NEGATIVE_IDEAL'
WHERE ms.model_id = 4
  AND ms.step_code = 'TOPSIS_DISTANCE'
  AND sa.algorithm_order = 4;

-- 更新算法5：自救转移能力TOPSIS正理想解（修正输入参数为_WEIGHT）
UPDATE step_algorithm sa
JOIN model_step ms ON sa.step_id = ms.id
SET sa.algorithm_name = '自救转移能力TOPSIS正理想解',
    sa.algorithm_code = 'CAPABILITY_POSITIVE_IDEAL',
    sa.ql_expression = '@POSITIVE_IDEAL:SELF_MUTUAL_AID_WEIGHT,PUBLIC_EVACUATION_WEIGHT,RELOCATION_SHELTER_WEIGHT',
    sa.output_param = 'CAPABILITY_POSITIVE_IDEAL'
WHERE ms.model_id = 4
  AND ms.step_code = 'TOPSIS_DISTANCE'
  AND sa.algorithm_order = 5;

-- 更新算法6：自救转移能力TOPSIS负理想解（修正输入参数为_WEIGHT）
UPDATE step_algorithm sa
JOIN model_step ms ON sa.step_id = ms.id
SET sa.algorithm_name = '自救转移能力TOPSIS负理想解',
    sa.algorithm_code = 'CAPABILITY_NEGATIVE_IDEAL',
    sa.ql_expression = '@NEGATIVE_IDEAL:SELF_MUTUAL_AID_WEIGHT,PUBLIC_EVACUATION_WEIGHT,RELOCATION_SHELTER_WEIGHT',
    sa.output_param = 'CAPABILITY_NEGATIVE_IDEAL'
WHERE ms.model_id = 4
  AND ms.step_code = 'TOPSIS_DISTANCE'
  AND sa.algorithm_order = 6;

-- 删除算法7和8（这两个算法是多余的，社区模型只需要3个一级指标）
DELETE sa FROM step_algorithm sa
JOIN model_step ms ON sa.step_id = ms.id
WHERE ms.model_id = 4
  AND ms.step_code = 'TOPSIS_DISTANCE'
  AND sa.algorithm_order IN (7, 8);

-- 查看修复后的配置
SELECT '==== 修复后的TOPSIS配置 ====' AS info;
SELECT sa.algorithm_order, sa.algorithm_name, sa.algorithm_code, sa.output_param, sa.ql_expression
FROM step_algorithm sa
JOIN model_step ms ON sa.step_id = ms.id
WHERE ms.model_id = 4 AND ms.step_code = 'TOPSIS_DISTANCE'
ORDER BY sa.algorithm_order;

-- 验证配置正确性
SELECT '==== 验证：一级指标应该有3个（管理、备灾、能力），每个有正负理想解共6个算法 ====' AS info;
SELECT COUNT(*) AS '算法总数（应该是6）'
FROM step_algorithm sa
JOIN model_step ms ON sa.step_id = ms.id
WHERE ms.model_id = 4 AND ms.step_code = 'TOPSIS_DISTANCE';

-- ========================================
-- 修复TOPSIS得分计算步骤
-- ========================================

SELECT '==== 修复前的TOPSIS得分计算配置 ====' AS info;
SELECT sa.algorithm_order, sa.algorithm_name, sa.algorithm_code, sa.output_param, sa.ql_expression
FROM step_algorithm sa
JOIN model_step ms ON sa.step_id = ms.id
WHERE ms.model_id = 4 AND ms.step_code = 'TOPSIS_SCORE'
ORDER BY sa.algorithm_order;

-- 更新算法1：灾害管理能力TOPSIS得分
UPDATE step_algorithm sa
JOIN model_step ms ON sa.step_id = ms.id
SET sa.algorithm_name = '灾害管理能力TOPSIS得分',
    sa.algorithm_code = 'MANAGEMENT_SCORE',
    sa.ql_expression = '@TOPSIS_SCORE:MANAGEMENT_POSITIVE_IDEAL,MANAGEMENT_NEGATIVE_IDEAL',
    sa.output_param = 'MANAGEMENT_SCORE'
WHERE ms.model_id = 4
  AND ms.step_code = 'TOPSIS_SCORE'
  AND sa.algorithm_order = 1;

-- 更新算法2：灾害备灾能力TOPSIS得分
UPDATE step_algorithm sa
JOIN model_step ms ON sa.step_id = ms.id
SET sa.algorithm_name = '灾害备灾能力TOPSIS得分',
    sa.algorithm_code = 'SUPPORT_SCORE',
    sa.ql_expression = '@TOPSIS_SCORE:SUPPORT_POSITIVE_IDEAL,SUPPORT_NEGATIVE_IDEAL',
    sa.output_param = 'SUPPORT_SCORE'
WHERE ms.model_id = 4
  AND ms.step_code = 'TOPSIS_SCORE'
  AND sa.algorithm_order = 2;

-- 更新算法3：自救转移能力TOPSIS得分
UPDATE step_algorithm sa
JOIN model_step ms ON sa.step_id = ms.id
SET sa.algorithm_name = '自救转移能力TOPSIS得分',
    sa.algorithm_code = 'CAPABILITY_SCORE',
    sa.ql_expression = '@TOPSIS_SCORE:CAPABILITY_POSITIVE_IDEAL,CAPABILITY_NEGATIVE_IDEAL',
    sa.output_param = 'CAPABILITY_SCORE'
WHERE ms.model_id = 4
  AND ms.step_code = 'TOPSIS_SCORE'
  AND sa.algorithm_order = 3;

-- 删除多余的算法4-9
DELETE sa FROM step_algorithm sa
JOIN model_step ms ON sa.step_id = ms.id
WHERE ms.model_id = 4
  AND ms.step_code = 'TOPSIS_SCORE'
  AND sa.algorithm_order > 3;

-- 查看修复后的TOPSIS得分配置
SELECT '==== 修复后的TOPSIS得分计算配置 ====' AS info;
SELECT sa.algorithm_order, sa.algorithm_name, sa.algorithm_code, sa.output_param, sa.ql_expression
FROM step_algorithm sa
JOIN model_step ms ON sa.step_id = ms.id
WHERE ms.model_id = 4 AND ms.step_code = 'TOPSIS_SCORE'
ORDER BY sa.algorithm_order;

-- 验证TOPSIS得分算法数量
SELECT '==== 验证：TOPSIS得分算法应该有3个 ====' AS info;
SELECT COUNT(*) AS '算法总数（应该是3）'
FROM step_algorithm sa
JOIN model_step ms ON sa.step_id = ms.id
WHERE ms.model_id = 4 AND ms.step_code = 'TOPSIS_SCORE';
