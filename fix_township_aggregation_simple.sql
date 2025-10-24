-- ============================================
-- 修复模型8步骤2：乡镇数据聚合逻辑（简化方案）
-- 方案：步骤2简单传递步骤1的计算结果，保持社区级别的数据
-- 如需乡镇级别聚合，在前端或报表层面实现
-- ============================================

USE evaluate_db;

-- 更新步骤2的描述，说明这一步只是数据传递
UPDATE model_step 
SET description = '传递社区级别的能力评估数据（乡镇聚合在前端实现）'
WHERE id = 51;

-- 更新步骤2的9个算法，改为简单传递步骤1的输出
-- 这样可以保持数据流的完整性，同时在前端可以按township_name分组聚合

-- 1. 预案编制能力传递
UPDATE step_algorithm 
SET ql_expression = 'PLAN_CONSTRUCTION',
    description = '传递社区预案编制能力值'
WHERE id = 1752;

-- 2. 隐患排查能力传递
UPDATE step_algorithm 
SET ql_expression = 'HAZARD_INSPECTION',
    description = '传递社区隐患排查能力值'
WHERE id = 1753;

-- 3. 风险评估能力传递
UPDATE step_algorithm 
SET ql_expression = 'RISK_ASSESSMENT',
    description = '传递社区风险评估能力值'
WHERE id = 1754;

-- 4. 资金投入能力传递
UPDATE step_algorithm 
SET ql_expression = 'FINANCIAL_INPUT',
    description = '传递社区资金投入能力值'
WHERE id = 1755;

-- 5. 物资储备能力传递
UPDATE step_algorithm 
SET ql_expression = 'MATERIAL_RESERVE',
    description = '传递社区物资储备能力值'
WHERE id = 1756;

-- 6. 医疗保障能力传递
UPDATE step_algorithm 
SET ql_expression = 'MEDICAL_SUPPORT',
    description = '传递社区医疗保障能力值'
WHERE id = 1757;

-- 7. 自救互救能力传递
UPDATE step_algorithm 
SET ql_expression = 'SELF_MUTUAL_AID',
    description = '传递社区自救互救能力值'
WHERE id = 1758;

-- 8. 公众疏散能力传递
UPDATE step_algorithm 
SET ql_expression = 'PUBLIC_EVACUATION',
    description = '传递社区公众疏散能力值'
WHERE id = 1759;

-- 9. 转移安置能力传递
UPDATE step_algorithm 
SET ql_expression = 'RELOCATION_SHELTER',
    description = '传递社区转移安置能力值'
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
-- 1. 此方案保持社区级别的数据完整性
-- 2. 前端可以通过township_name字段进行分组聚合
-- 3. 聚合逻辑：按township_name分组，对每个能力值求和后除以社区数量
-- 4. 前端实现示例（伪代码）：
--    SELECT township_name, 
--           AVG(PLAN_CONSTRUCTION) as avg_plan,
--           AVG(HAZARD_INSPECTION) as avg_hazard,
--           ...
--    FROM results
--    GROUP BY township_name
