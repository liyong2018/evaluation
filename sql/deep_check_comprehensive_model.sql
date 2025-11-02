-- =====================================================
-- 综合减灾能力评估模型 - 深度数据检查脚本
-- 用于排查第一步显示0的问题
-- =====================================================

-- 1. 检查evaluation_result表中模型3和模型8的数据情况
SELECT '=== 1. evaluation_result表数据概览 ===' AS info;

SELECT
    evaluation_model_id AS 模型ID,
    em.model_name AS 模型名称,
    COUNT(*) AS 记录总数,
    COUNT(DISTINCT region_code) AS 区域数量,
    MIN(create_time) AS 最早时间,
    MAX(create_time) AS 最新时间
FROM evaluation_result er
LEFT JOIN evaluation_model em ON er.evaluation_model_id = em.id
WHERE er.evaluation_model_id IN (3, 8)
GROUP BY er.evaluation_model_id, em.model_name;

-- 2. 查看模型3和模型8的所有区域代码和区域名称
SELECT '=== 2. 模型3和8的所有区域列表 ===' AS info;

SELECT DISTINCT
    er.evaluation_model_id AS 模型ID,
    er.region_code AS 区域代码,
    er.region_name AS 区域名称,
    MAX(er.create_time) AS 最新评估时间
FROM evaluation_result er
WHERE er.evaluation_model_id IN (3, 8)
GROUP BY er.evaluation_model_id, er.region_code, er.region_name
ORDER BY er.evaluation_model_id, er.region_code;

-- 3. 检查每个区域在两个模型中的数据完整性
SELECT '=== 3. 区域数据完整性检查 ===' AS info;

SELECT
    COALESCE(t1.region_code, t2.region_code) AS 区域代码,
    COALESCE(t1.region_name, t2.region_name) AS 区域名称,
    CASE WHEN t1.region_code IS NOT NULL THEN '✓' ELSE '✗' END AS 有模型3数据,
    CASE WHEN t2.region_code IS NOT NULL THEN '✓' ELSE '✗' END AS 有模型8数据,
    t1.management_capability_score AS 模型3_管理能力,
    t1.support_capability_score AS 模型3_备灾能力,
    t1.self_rescue_capability_score AS 模型3_自救能力,
    t2.management_capability_score AS 模型8_管理能力,
    t2.support_capability_score AS 模型8_备灾能力,
    t2.self_rescue_capability_score AS 模型8_自救能力
FROM (
    SELECT region_code, region_name,
           management_capability_score,
           support_capability_score,
           self_rescue_capability_score
    FROM evaluation_result
    WHERE evaluation_model_id = 3
) t1
FULL OUTER JOIN (
    SELECT region_code, region_name,
           management_capability_score,
           support_capability_score,
           self_rescue_capability_score
    FROM evaluation_result
    WHERE evaluation_model_id = 8
) t2 ON t1.region_code = t2.region_code
ORDER BY COALESCE(t1.region_code, t2.region_code);

-- 4. 检查综合减灾模型的算法配置
SELECT '=== 4. 综合减灾模型第一步算法配置 ===' AS info;

SELECT
    sa.algorithm_order AS 顺序,
    sa.algorithm_name AS 算法名称,
    sa.algorithm_code AS 算法代码,
    sa.ql_expression AS QL表达式,
    sa.output_param AS 输出参数,
    sa.status AS 状态
FROM step_algorithm sa
JOIN model_step ms ON sa.step_id = ms.id
JOIN evaluation_model em ON ms.model_id = em.id
WHERE em.model_code = 'COMPREHENSIVE_MODEL'
  AND ms.step_order = 1
ORDER BY sa.algorithm_order;

-- 5. 模拟@LOAD_EVAL_RESULT查询（针对某个具体区域）
SELECT '=== 5. 模拟数据加载（请将511425108替换为您的区域代码） ===' AS info;

-- 请在这里替换为您实际选择的区域代码
SET @test_region_code = '511425108';  -- 修改这里！

-- 模拟加载模型3的数据
SELECT
    '模型3-乡镇评估' AS 来源,
    @test_region_code AS 查询区域代码,
    region_code AS 实际区域代码,
    region_name AS 区域名称,
    management_capability_score AS 灾害管理能力,
    support_capability_score AS 灾害备灾能力,
    self_rescue_capability_score AS 自救转移能力,
    create_time AS 评估时间
FROM evaluation_result
WHERE evaluation_model_id = 3
  AND region_code = @test_region_code
ORDER BY create_time DESC
LIMIT 1;

-- 模拟加载模型8的数据
SELECT
    '模型8-社区评估' AS 来源,
    @test_region_code AS 查询区域代码,
    region_code AS 实际区域代码,
    region_name AS 区域名称,
    management_capability_score AS 灾害管理能力,
    support_capability_score AS 灾害备灾能力,
    self_rescue_capability_score AS 自救转移能力,
    create_time AS 评估时间
FROM evaluation_result
WHERE evaluation_model_id = 8
  AND region_code = @test_region_code
ORDER BY create_time DESC
LIMIT 1;

-- 6. 检查是否有NULL值
SELECT '=== 6. 检查NULL值情况 ===' AS info;

SELECT
    evaluation_model_id AS 模型ID,
    COUNT(*) AS 总记录数,
    SUM(CASE WHEN management_capability_score IS NULL THEN 1 ELSE 0 END) AS 管理能力为NULL,
    SUM(CASE WHEN support_capability_score IS NULL THEN 1 ELSE 0 END) AS 备灾能力为NULL,
    SUM(CASE WHEN self_rescue_capability_score IS NULL THEN 1 ELSE 0 END) AS 自救能力为NULL,
    SUM(CASE WHEN management_capability_score = 0 THEN 1 ELSE 0 END) AS 管理能力为0,
    SUM(CASE WHEN support_capability_score = 0 THEN 1 ELSE 0 END) AS 备灾能力为0,
    SUM(CASE WHEN self_rescue_capability_score = 0 THEN 1 ELSE 0 END) AS 自救能力为0
FROM evaluation_result
WHERE evaluation_model_id IN (3, 8)
GROUP BY evaluation_model_id;

-- 7. 检查最近的执行记录
SELECT '=== 7. 最近的模型执行记录 ===' AS info;

SELECT
    mer.id AS 执行ID,
    em.model_name AS 模型名称,
    mer.execution_code AS 执行代码,
    mer.region_ids AS 区域ID列表,
    mer.execution_status AS 执行状态,
    mer.start_time AS 开始时间,
    mer.end_time AS 结束时间,
    mer.error_message AS 错误信息
FROM model_execution_record mer
LEFT JOIN evaluation_model em ON mer.model_id = em.id
WHERE em.model_code = 'COMPREHENSIVE_MODEL'
ORDER BY mer.start_time DESC
LIMIT 5;
