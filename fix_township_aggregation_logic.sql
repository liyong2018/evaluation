-- ============================================
-- 修复模型8步骤2：乡镇数据聚合逻辑
-- 需求：按乡镇分组求和，然后计算占比（乡镇内能力和值 / 乡镇内社区的数量）
-- ============================================

USE evaluate_db;

-- 更新步骤2的9个聚合算法
-- 使用 @SUM 标记进行分组求和，然后除以社区数量得到平均值

-- 1. 预案编制能力聚合
UPDATE step_algorithm 
SET ql_expression = '@SUM(PLAN_CONSTRUCTION) / @COUNT(*)',
    description = '按乡镇分组求和预案编制能力，然后除以社区数量得到平均值'
WHERE id = 1752;

-- 2. 隐患排查能力聚合
UPDATE step_algorithm 
SET ql_expression = '@SUM(HAZARD_INSPECTION) / @COUNT(*)',
    description = '按乡镇分组求和隐患排查能力，然后除以社区数量得到平均值'
WHERE id = 1753;

-- 3. 风险评估能力聚合
UPDATE step_algorithm 
SET ql_expression = '@SUM(RISK_ASSESSMENT) / @COUNT(*)',
    description = '按乡镇分组求和风险评估能力，然后除以社区数量得到平均值'
WHERE id = 1754;

-- 4. 资金投入能力聚合
UPDATE step_algorithm 
SET ql_expression = '@SUM(FINANCIAL_INPUT) / @COUNT(*)',
    description = '按乡镇分组求和资金投入能力，然后除以社区数量得到平均值'
WHERE id = 1755;

-- 5. 物资储备能力聚合
UPDATE step_algorithm 
SET ql_expression = '@SUM(MATERIAL_RESERVE) / @COUNT(*)',
    description = '按乡镇分组求和物资储备能力，然后除以社区数量得到平均值'
WHERE id = 1756;

-- 6. 医疗保障能力聚合
UPDATE step_algorithm 
SET ql_expression = '@SUM(MEDICAL_SUPPORT) / @COUNT(*)',
    description = '按乡镇分组求和医疗保障能力，然后除以社区数量得到平均值'
WHERE id = 1757;

-- 7. 自救互救能力聚合
UPDATE step_algorithm 
SET ql_expression = '@SUM(SELF_MUTUAL_AID) / @COUNT(*)',
    description = '按乡镇分组求和自救互救能力，然后除以社区数量得到平均值'
WHERE id = 1758;

-- 8. 公众疏散能力聚合
UPDATE step_algorithm 
SET ql_expression = '@SUM(PUBLIC_EVACUATION) / @COUNT(*)',
    description = '按乡镇分组求和公众疏散能力，然后除以社区数量得到平均值'
WHERE id = 1759;

-- 9. 转移安置能力聚合
UPDATE step_algorithm 
SET ql_expression = '@SUM(RELOCATION_SHELTER) / @COUNT(*)',
    description = '按乡镇分组求和转移安置能力，然后除以社区数量得到平均值'
WHERE id = 1760;

-- 验证更新结果
SELECT 
    id,
    algorithm_name,
    algorithm_order,
    output_param,
    ql_expression,
    description
FROM step_algorithm 
WHERE step_id = 51 
ORDER BY algorithm_order;

-- 说明：
-- @SUM(字段名) - 按分组字段（乡镇）对指定字段求和
-- @COUNT(*) - 统计每个分组（乡镇）内的记录数（社区数量）
-- 最终结果 = 乡镇内能力和值 / 乡镇内社区的数量
