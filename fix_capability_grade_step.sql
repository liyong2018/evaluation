-- 修复能力值计算与分级步骤的算法配置

SELECT '==== 修复前的能力值计算与分级配置 ====' AS info;
SELECT 
    sa.algorithm_order,
    sa.algorithm_name,
    sa.algorithm_code,
    sa.ql_expression,
    sa.output_param
FROM step_algorithm sa
JOIN model_step ms ON sa.step_id = ms.id
WHERE ms.model_id = 4 AND ms.step_code = 'CAPABILITY_GRADE'
ORDER BY sa.algorithm_order;

-- 算法1：灾害管理能力TOPSIS得分（名称错误，应该是"灾害管理能力TOPSIS得分"而不是"预案编制能力评估"）
UPDATE step_algorithm sa
JOIN model_step ms ON sa.step_id = ms.id
SET sa.algorithm_name = '灾害管理能力TOPSIS得分'
WHERE ms.model_id = 4
  AND ms.step_code = 'CAPABILITY_GRADE'
  AND sa.algorithm_order = 1;

-- 算法2：灾害备灾能力TOPSIS得分（名称错误，应该是"灾害备灾能力TOPSIS得分"而不是"隐患排查能力评估"）
UPDATE step_algorithm sa
JOIN model_step ms ON sa.step_id = ms.id
SET sa.algorithm_name = '灾害备灾能力TOPSIS得分'
WHERE ms.model_id = 4
  AND ms.step_code = 'CAPABILITY_GRADE'
  AND sa.algorithm_order = 2;

-- 算法3：自救转移能力TOPSIS得分（名称错误，应该是"自救转移能力TOPSIS得分"而不是"风险评估能力评估"）
UPDATE step_algorithm sa
JOIN model_step ms ON sa.step_id = ms.id
SET sa.algorithm_name = '自救转移能力TOPSIS得分'
WHERE ms.model_id = 4
  AND ms.step_code = 'CAPABILITY_GRADE'
  AND sa.algorithm_order = 3;

-- 算法4：灾害管理能力分级（名称错误，应该是"灾害管理能力分级"而不是"财政投入能力评估"）
UPDATE step_algorithm sa
JOIN model_step ms ON sa.step_id = ms.id
SET sa.algorithm_name = '灾害管理能力分级'
WHERE ms.model_id = 4
  AND ms.step_code = 'CAPABILITY_GRADE'
  AND sa.algorithm_order = 4;

-- 算法5：灾害备灾能力分级（名称错误，应该是"灾害备灾能力分级"而不是"物资储备能力评估"）
UPDATE step_algorithm sa
JOIN model_step ms ON sa.step_id = ms.id
SET sa.algorithm_name = '灾害备灾能力分级'
WHERE ms.model_id = 4
  AND ms.step_code = 'CAPABILITY_GRADE'
  AND sa.algorithm_order = 5;

-- 算法6：自救转移能力分级（名称错误，应该是"自救转移能力分级"而不是"医疗保障能力评估"）
UPDATE step_algorithm sa
JOIN model_step ms ON sa.step_id = ms.id
SET sa.algorithm_name = '自救转移能力分级'
WHERE ms.model_id = 4
  AND ms.step_code = 'CAPABILITY_GRADE'
  AND sa.algorithm_order = 6;

-- 删除算法7和8（这两个算法是多余的，社区模型不需要综合能力计算）
DELETE sa FROM step_algorithm sa
JOIN model_step ms ON sa.step_id = ms.id
WHERE ms.model_id = 4
  AND ms.step_code = 'CAPABILITY_GRADE'
  AND sa.algorithm_order IN (7, 8);

SELECT '==== 修复后的能力值计算与分级配置 ====' AS info;
SELECT 
    sa.algorithm_order,
    sa.algorithm_name,
    sa.algorithm_code,
    sa.ql_expression,
    sa.output_param
FROM step_algorithm sa
JOIN model_step ms ON sa.step_id = ms.id
WHERE ms.model_id = 4 AND ms.step_code = 'CAPABILITY_GRADE'
ORDER BY sa.algorithm_order;

SELECT '==== 验证：应该有6个算法（3个TOPSIS得分 + 3个分级） ====' AS info;
SELECT COUNT(*) AS '算法总数（应该是6）'
FROM step_algorithm sa
JOIN model_step ms ON sa.step_id = ms.id
WHERE ms.model_id = 4 AND ms.step_code = 'CAPABILITY_GRADE';
