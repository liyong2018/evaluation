-- ===================================================================
-- 快速诊断步骤1配置
-- ===================================================================
-- 此脚本用于快速检查步骤1（社区指标计算）的配置问题
-- 如果步骤1的13列数据没有显示，使用此脚本诊断
-- ===================================================================

-- 1. 检查社区数据是否存在
SELECT '========== 1. 社区数据检查 ==========' AS '';
SELECT
    COUNT(*) AS total_communities,
    COUNT(DISTINCT region_code) AS unique_regions,
    COUNT(DISTINCT township_name) AS unique_townships
FROM community_disaster_reduction_capacity;

-- 显示前3条社区数据
SELECT '前3条社区数据:' AS '';
SELECT
    region_code,
    community_name,
    township_name,
    disaster_mgmt_org,
    emergency_plan,
    emergency_drill
FROM community_disaster_reduction_capacity
LIMIT 3;

-- 2. 检查步骤1算法配置统计
SELECT '========== 2. 步骤1算法配置统计 ==========' AS '';
SELECT
    COUNT(*) AS total_algorithms,
    SUM(CASE WHEN output_param IS NOT NULL AND output_param != '' THEN 1 ELSE 0 END) AS with_output_param,
    SUM(CASE WHEN output_param IS NULL OR output_param = '' THEN 1 ELSE 0 END) AS without_output_param,
    SUM(CASE WHEN status = 1 THEN 1 ELSE 0 END) AS enabled,
    SUM(CASE WHEN status = 0 THEN 1 ELSE 0 END) AS disabled,
    SUM(CASE WHEN ql_expression IS NULL OR ql_expression = '' THEN 1 ELSE 0 END) AS missing_expression
FROM step_algorithm
WHERE step_id = (SELECT id FROM model_step WHERE model_id = 8 AND step_order = 1);

-- 3. 列出所有步骤1算法的详细信息
SELECT '========== 3. 步骤1算法详细列表 ==========' AS '';
SELECT
    calculation_order AS 顺序,
    algorithm_code AS 算法代码,
    algorithm_name AS 算法名称,
    LEFT(ql_expression, 60) AS 表达式预览,
    output_param AS 输出参数,
    CASE WHEN status = 1 THEN '启用' ELSE '禁用' END AS 状态,
    CASE
        WHEN output_param IS NULL OR output_param = '' THEN '❌ 缺少输出参数'
        WHEN ql_expression IS NULL OR ql_expression = '' THEN '❌ 缺少表达式'
        WHEN status = 0 THEN '⚠ 算法被禁用'
        ELSE '✓ 配置正常'
    END AS 检查结果
FROM step_algorithm
WHERE step_id = (SELECT id FROM model_step WHERE model_id = 8 AND step_order = 1)
ORDER BY calculation_order;

-- 4. 检查community表的所有字段名
SELECT '========== 4. Community表字段列表 ==========' AS '';
SELECT
    ORDINAL_POSITION AS 位置,
    COLUMN_NAME AS 字段名,
    DATA_TYPE AS 数据类型,
    CASE WHEN IS_NULLABLE = 'YES' THEN '可空' ELSE '非空' END AS 是否可空
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_NAME = 'community_disaster_reduction_capacity'
  AND TABLE_SCHEMA = DATABASE()
ORDER BY ORDINAL_POSITION;

-- 5. 检查算法表达式中的变量是否存在于community表
SELECT '========== 5. 变量名匹配检查 ==========' AS '';
SELECT
    sa.algorithm_name AS 算法名称,
    sa.ql_expression AS 表达式,
    CASE
        WHEN sa.ql_expression LIKE '%disaster_mgmt_org%' THEN
            CASE WHEN EXISTS (
                SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS
                WHERE TABLE_NAME = 'community_disaster_reduction_capacity'
                  AND COLUMN_NAME = 'disaster_mgmt_org'
            ) THEN '✓ disaster_mgmt_org'
            ELSE '❌ disaster_mgmt_org 不存在'
            END
        WHEN sa.ql_expression LIKE '%emergency_plan%' THEN
            CASE WHEN EXISTS (
                SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS
                WHERE TABLE_NAME = 'community_disaster_reduction_capacity'
                  AND COLUMN_NAME = 'emergency_plan'
            ) THEN '✓ emergency_plan'
            ELSE '❌ emergency_plan 不存在'
            END
        ELSE '需要手动检查'
    END AS 变量检查
FROM step_algorithm sa
WHERE sa.step_id = (SELECT id FROM model_step WHERE model_id = 8 AND step_order = 1)
  AND sa.ql_expression IS NOT NULL
LIMIT 10;

-- 6. 检查最近的执行结果
SELECT '========== 6. 最近5次执行结果 ==========' AS '';
SELECT
    er.id AS 结果ID,
    er.region_code AS 地区代码,
    er.region_name AS 地区名称,
    er.management_capability_score AS 管理能力分值,
    er.support_capability_score AS 备灾能力分值,
    er.self_rescue_capability_score AS 自救能力分值,
    DATE_FORMAT(er.created_at, '%Y-%m-%d %H:%i:%s') AS 创建时间
FROM evaluation_result er
WHERE er.evaluation_model_id = 8
ORDER BY er.created_at DESC
LIMIT 5;

-- 7. 问题总结
SELECT '========== 7. 问题总结 ==========' AS '';
SELECT
    CASE
        WHEN (SELECT COUNT(*) FROM community_disaster_reduction_capacity) = 0 THEN
            '❌ 严重问题：社区数据表为空，需要导入数据'
        WHEN (SELECT COUNT(*) FROM step_algorithm WHERE step_id = (SELECT id FROM model_step WHERE model_id = 8 AND step_order = 1) AND (output_param IS NULL OR output_param = '')) > 0 THEN
            '❌ 配置问题：部分算法缺少output_param'
        WHEN (SELECT COUNT(*) FROM step_algorithm WHERE step_id = (SELECT id FROM model_step WHERE model_id = 8 AND step_order = 1) AND status = 0) > 0 THEN
            '⚠ 配置问题：部分算法被禁用'
        WHEN (SELECT COUNT(*) FROM step_algorithm WHERE step_id = (SELECT id FROM model_step WHERE model_id = 8 AND step_order = 1)) < 10 THEN
            '⚠ 配置问题：步骤1的算法数量少于预期（应该有13个）'
        ELSE
            '✓ 配置看起来正常，请检查应用程序日志'
    END AS 诊断结果,
    CASE
        WHEN (SELECT COUNT(*) FROM community_disaster_reduction_capacity) = 0 THEN
            '执行 sql/archive/community_disaster_reduction_capacity.sql 导入数据'
        WHEN (SELECT COUNT(*) FROM step_algorithm WHERE step_id = (SELECT id FROM model_step WHERE model_id = 8 AND step_order = 1) AND (output_param IS NULL OR output_param = '')) > 0 THEN
            '使用 UPDATE step_algorithm SET output_param = ... 添加输出参数'
        WHEN (SELECT COUNT(*) FROM step_algorithm WHERE step_id = (SELECT id FROM model_step WHERE model_id = 8 AND step_order = 1) AND status = 0) > 0 THEN
            '使用 UPDATE step_algorithm SET status = 1 启用算法'
        ELSE
            '重启应用程序，执行模型，查看详细日志'
    END AS 建议操作;

-- ===================================================================
-- 使用方法：
-- ===================================================================
-- 方法1（推荐）：
--   mysql -h192.168.15.203 -P30314 -uroot -p123456 evaluate_db < quick_diagnose_step1.sql > step1_diagnosis.txt
--   cat step1_diagnosis.txt
--
-- 方法2：
--   在MySQL客户端中逐段复制粘贴执行
-- ===================================================================

-- ===================================================================
-- 预期输出：
-- ===================================================================
-- 如果配置正常，应该看到：
--   - 社区数据：total_communities > 0
--   - 算法数量：total_algorithms = 13
--   - 所有算法：with_output_param = 13, enabled = 13
--   - 检查结果：所有算法都是 '✓ 配置正常'
--   - 问题总结：'✓ 配置看起来正常'
-- ===================================================================
