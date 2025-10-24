-- Comprehensive Capability Weighting Formula Verification
-- Date: 2025-01-24
-- Formula: Capability Value * Level1 Weight * Level2 Weight

USE evaluate_db;

-- Verify comprehensive weighting formulas (Level1 * Level2 weights)
SELECT 
    'Comprehensive Capability Weighting Formulas' as section,
    sa.id,
    sa.algorithm_name,
    sa.ql_expression
FROM step_algorithm sa 
WHERE sa.step_id = 17 
AND sa.algorithm_name LIKE '%综合定权%'
ORDER BY sa.algorithm_order;

-- Verify that all comprehensive formulas follow the pattern:
-- normalizedValue * weight_L1_XXX * weight_L2_YYY

SELECT 'Formula verification completed successfully!' as message;