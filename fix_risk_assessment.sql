UPDATE step_algorithm 
SET ql_expression = 'riskAssessment != null && riskAssessment.equals("是") ? 1.0 : 0.0' 
WHERE algorithm_code = 'RISK_ASSESSMENT';
