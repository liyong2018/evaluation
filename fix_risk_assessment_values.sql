-- 修复风险评估能力计算问题
-- 问题描述：算法期望 riskAssessment 等于 "是"，但数据库中存储的是"低"，导致计算结果为0
-- 解决方案：修改算法表达式，支持多种风险评估值

-- 修复队伍管理能力算法表达式（算法ID 23）
UPDATE step_algorithm
SET ql_expression = '(management_staff * 1.0 / population) * 10000',
    description = '队伍管理能力 = (管理人员数量/常住人口) * 10000'
WHERE algorithm_code = 'TEAM_MANAGEMENT';

-- 修复风险评估能力算法表达式（算法ID 24）
UPDATE step_algorithm
SET ql_expression = 'riskAssessment != null && (riskAssessment.equals("是") || riskAssessment.equals("低") || riskAssessment.equals("中") || riskAssessment.equals("高")) ? 1.0 : 0.0',
    description = '风险评估能力 = if(开展风险评估且值为"是"、"低"、"中"、"高"中的任意一种，则得分为1，否则为0)'
WHERE algorithm_code = 'RISK_ASSESSMENT';

-- 修复二级计算中的风险评估算法表达式（算法ID 112）
UPDATE step_algorithm
SET ql_expression = 'riskAssessment != null && (riskAssessment.equals("是") || riskAssessment.equals("低") || riskAssessment.equals("中") || riskAssessment.equals("高")) ? 1.0 : 0.0',
    description = '风险评估能力（属性向量归一化）= if(开展风险评估且值为"是"、"低"、"中"、"高"中的任意一种，则得分为1，否则为0)'
WHERE algorithm_code = 'SC_RISK_ASSESSMENT';

COMMIT;