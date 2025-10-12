UPDATE step_algorithm 
SET ql_expression = CONCAT('riskAssessment != null && riskAssessment.equals("', UNHEX('E698AF'), '") ? 1.0 : 0.0')
WHERE algorithm_code = 'RISK_ASSESSMENT';
