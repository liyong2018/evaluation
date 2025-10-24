SELECT 
    algorithm_order,
    algorithm_name,
    ql_expression,
    output_param
FROM step_algorithm sa
JOIN model_step ms ON sa.step_id = ms.id
WHERE ms.model_id = 8 
  AND ms.step_code = 'COMMUNITY_INDICATORS' 
  AND algorithm_order <= 4
ORDER BY algorithm_order;
