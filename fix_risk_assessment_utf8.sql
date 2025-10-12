UPDATE step_algorithm 
SET ql_expression = CONCAT('riskAssessment != null && riskAssessment.equals(', CHAR(0x22), '是', CHAR(0x22), ') ? 1.0 : 0.0')
WHERE algorithm_code = 'RISK_ASSESSMENT';
