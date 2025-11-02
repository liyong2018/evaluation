-- =====================================================
-- 综合减灾能力评估模型诊断脚本
-- =====================================================

-- 1. 查询综合减灾能力评估模型信息
SELECT '=== 综合减灾能力评估模型信息 ===' AS info;

SELECT
    id AS model_id,
    model_name,
    model_code,
    description,
    version,
    status,
    create_time
FROM evaluation_model
WHERE model_code = 'COMPREHENSIVE_MODEL'
   OR model_name LIKE '%综合减灾%'
ORDER BY id;

-- 2. 查询模型步骤配置
SELECT '=== 模型步骤配置 ===' AS info;

SELECT
    ms.id AS step_id,
    ms.model_id,
    ms.step_name,
    ms.step_code,
    ms.step_order,
    ms.step_type,
    ms.description,
    ms.status
FROM model_step ms
WHERE ms.model_id IN (
    SELECT id FROM evaluation_model
    WHERE model_code = 'COMPREHENSIVE_MODEL'
       OR model_name LIKE '%综合减灾%'
)
ORDER BY ms.step_order;

-- 3. 查询步骤算法配置
SELECT '=== 步骤算法配置 ===' AS info;

SELECT
    sa.id AS algorithm_id,
    ms.step_name,
    ms.step_order,
    sa.algorithm_name,
    sa.algorithm_code,
    sa.algorithm_order,
    sa.ql_expression,
    sa.output_param
FROM step_algorithm sa
JOIN model_step ms ON sa.step_id = ms.id
WHERE ms.model_id IN (
    SELECT id FROM evaluation_model
    WHERE model_code = 'COMPREHENSIVE_MODEL'
       OR model_name LIKE '%综合减灾%'
)
ORDER BY ms.step_order, sa.algorithm_order;

-- 4. 检查evaluation_result表中的数据
SELECT '=== evaluation_result表数据检查 ===' AS info;

-- 检查模型3（乡镇评估模型）的结果数量
SELECT
    '模型3-乡镇评估' AS model_name,
    COUNT(*) AS result_count,
    COUNT(DISTINCT region_code) AS region_count,
    MAX(create_time) AS latest_time
FROM evaluation_result
WHERE evaluation_model_id = 3;

-- 检查模型8（社区-乡镇评估模型）的结果数量
SELECT
    '模型8-社区评估' AS model_name,
    COUNT(*) AS result_count,
    COUNT(DISTINCT region_code) AS region_count,
    MAX(create_time) AS latest_time
FROM evaluation_result
WHERE evaluation_model_id = 8;

-- 5. 查看最近的评估结果（前10条）
SELECT '=== 最近的评估结果（模型3和8） ===' AS info;

SELECT
    er.id,
    em.model_name,
    er.region_code,
    er.region_name,
    er.management_capability_score,
    er.support_capability_score,
    er.self_rescue_capability_score,
    er.create_time
FROM evaluation_result er
LEFT JOIN evaluation_model em ON er.evaluation_model_id = em.id
WHERE er.evaluation_model_id IN (3, 8)
ORDER BY er.create_time DESC
LIMIT 10;

-- 6. 检查是否有重复的区域代码
SELECT '=== 重复的区域代码检查 ===' AS info;

SELECT
    region_code,
    region_name,
    evaluation_model_id,
    COUNT(*) AS count
FROM evaluation_result
WHERE evaluation_model_id IN (3, 8)
GROUP BY region_code, region_name, evaluation_model_id
HAVING COUNT(*) > 1
ORDER BY count DESC;
