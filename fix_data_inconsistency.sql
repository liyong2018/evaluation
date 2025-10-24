-- 修复数据不一致问题：基于实际运行数据库中的值来修复算法表达式
-- 问题分析：当前运行数据库中 risk_assessment 字段存储的是 '低'，但算法期望 '是'
-- 解决方案：修改算法表达式，支持实际的数据值

-- 1. 修复风险评估能力算法表达式（算法ID 24）
UPDATE step_algorithm
SET ql_expression = 'riskAssessment != null && (riskAssessment.equals("是") || riskAssessment.equals("低") || riskAssessment.equals("中") || riskAssessment.equals("高")) ? 1.0 : 0.0',
    description = '风险评估能力 = if(开展风险评估且值为"是"、"低"、"中"、"高"中的任意一种，则得分为1，否则为0)'
WHERE algorithm_code = 'RISK_ASSESSMENT';

-- 2. 修复二级计算中的风险评估算法表达式（算法ID 112）
UPDATE step_algorithm
SET ql_expression = 'riskAssessment != null && (riskAssessment.equals("是") || riskAssessment.equals("低") || riskAssessment.equals("中") || riskAssessment.equals("高")) ? 1.0 : 0.0',
    description = '风险评估能力（属性向量归一化）= if(开展风险评估且值为"是"、"低"、"中"、"高"中的任意一种，则得分为1，否则为0)'
WHERE algorithm_code = 'SC_RISK_ASSESSMENT';

-- 3. 同时检查其他可能存在的相关算法，如果有的话也一并修复
-- （这里只处理明确找到的算法ID）

-- 4. 记录修复操作
INSERT INTO system_log (log_type, log_content, create_time)
VALUES ('DATA_FIX', '修复风险评估算法表达式以支持实际数据值：是、低、中、高', NOW())
ON DUPLICATE KEY UPDATE log_content = VALUES(log_content);

COMMIT;