-- ============================================
-- 修复模型8步骤2的输出参数名称
-- 使其与步骤3的输入参数名称匹配
-- ============================================

USE evaluate_db;

-- 更新步骤2的输出参数名称，使其与步骤1的输出一致
-- 这样步骤3（归一化）就能正确找到输入字段

UPDATE step_algorithm SET output_param = 'PLAN_CONSTRUCTION' WHERE id = 1752;
UPDATE step_algorithm SET output_param = 'HAZARD_INSPECTION' WHERE id = 1753;
UPDATE step_algorithm SET output_param = 'RISK_ASSESSMENT' WHERE id = 1754;
UPDATE step_algorithm SET output_param = 'FINANCIAL_INPUT' WHERE id = 1755;
UPDATE step_algorithm SET output_param = 'MATERIAL_RESERVE' WHERE id = 1756;
UPDATE step_algorithm SET output_param = 'MEDICAL_SUPPORT' WHERE id = 1757;
UPDATE step_algorithm SET output_param = 'SELF_MUTUAL_AID' WHERE id = 1758;
UPDATE step_algorithm SET output_param = 'PUBLIC_EVACUATION' WHERE id = 1759;
UPDATE step_algorithm SET output_param = 'RELOCATION_SHELTER' WHERE id = 1760;

-- 验证更新结果
SELECT 
    id,
    algorithm_name,
    algorithm_order,
    output_param,
    ql_expression
FROM step_algorithm 
WHERE step_id = 51 
ORDER BY algorithm_order;
