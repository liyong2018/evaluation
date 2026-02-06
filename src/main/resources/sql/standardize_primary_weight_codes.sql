START TRANSACTION;

SELECT 'step_algorithm:weight_L1_MANAGEMENT' AS item, COUNT(1) AS cnt
FROM step_algorithm
WHERE ql_expression LIKE '%weight_L1_MANAGEMENT%';
SELECT 'step_algorithm:weight_L1_PREPARATION' AS item, COUNT(1) AS cnt
FROM step_algorithm
WHERE ql_expression LIKE '%weight_L1_PREPARATION%';
SELECT 'step_algorithm:weight_L1_SELF_RESCUE' AS item, COUNT(1) AS cnt
FROM step_algorithm
WHERE ql_expression LIKE '%weight_L1_SELF_RESCUE%';

UPDATE step_algorithm
SET ql_expression = REPLACE(ql_expression, 'weight_L1_MANAGEMENT', 'weight_L1_DISASTER_MANAGEMENT')
WHERE ql_expression LIKE '%weight_L1_MANAGEMENT%';
UPDATE step_algorithm
SET ql_expression = REPLACE(ql_expression, 'weight_L1_PREPARATION', 'weight_L1_DISASTER_PREPAREDNESS')
WHERE ql_expression LIKE '%weight_L1_PREPARATION%';
UPDATE step_algorithm
SET ql_expression = REPLACE(ql_expression, 'weight_L1_SELF_RESCUE', 'weight_L1_SELF_RESCUE_TRANSFER')
WHERE ql_expression LIKE '%weight_L1_SELF_RESCUE%';

DELETE iw_old
FROM indicator_weight iw_old
JOIN indicator_weight iw_new
  ON iw_new.config_id = iw_old.config_id
 AND iw_new.indicator_code = 'L1_DISASTER_MANAGEMENT'
WHERE iw_old.indicator_code = 'L1_MANAGEMENT';
DELETE iw_old
FROM indicator_weight iw_old
JOIN indicator_weight iw_new
  ON iw_new.config_id = iw_old.config_id
 AND iw_new.indicator_code = 'L1_DISASTER_PREPAREDNESS'
WHERE iw_old.indicator_code = 'L1_PREPARATION';
DELETE iw_old
FROM indicator_weight iw_old
JOIN indicator_weight iw_new
  ON iw_new.config_id = iw_old.config_id
 AND iw_new.indicator_code = 'L1_SELF_RESCUE_TRANSFER'
WHERE iw_old.indicator_code = 'L1_SELF_RESCUE';

UPDATE indicator_weight
SET indicator_code = 'L1_DISASTER_MANAGEMENT'
WHERE indicator_code = 'L1_MANAGEMENT';
UPDATE indicator_weight
SET indicator_code = 'L1_DISASTER_PREPAREDNESS'
WHERE indicator_code = 'L1_PREPARATION';
UPDATE indicator_weight
SET indicator_code = 'L1_SELF_RESCUE_TRANSFER'
WHERE indicator_code = 'L1_SELF_RESCUE';

UPDATE indicator_weight_score
SET indicator_code = 'L1_DISASTER_MANAGEMENT'
WHERE indicator_code = 'L1_MANAGEMENT';
UPDATE indicator_weight_score
SET indicator_code = 'L1_DISASTER_PREPAREDNESS'
WHERE indicator_code = 'L1_PREPARATION';
UPDATE indicator_weight_score
SET indicator_code = 'L1_SELF_RESCUE_TRANSFER'
WHERE indicator_code = 'L1_SELF_RESCUE';

SELECT 'indicator_weight:L1_MANAGEMENT' AS item, COUNT(1) AS cnt
FROM indicator_weight
WHERE indicator_code = 'L1_MANAGEMENT';
SELECT 'indicator_weight:L1_PREPARATION' AS item, COUNT(1) AS cnt
FROM indicator_weight
WHERE indicator_code = 'L1_PREPARATION';
SELECT 'indicator_weight:L1_SELF_RESCUE' AS item, COUNT(1) AS cnt
FROM indicator_weight
WHERE indicator_code = 'L1_SELF_RESCUE';

SELECT 'indicator_weight_score:L1_MANAGEMENT' AS item, COUNT(1) AS cnt
FROM indicator_weight_score
WHERE indicator_code = 'L1_MANAGEMENT';
SELECT 'indicator_weight_score:L1_PREPARATION' AS item, COUNT(1) AS cnt
FROM indicator_weight_score
WHERE indicator_code = 'L1_PREPARATION';
SELECT 'indicator_weight_score:L1_SELF_RESCUE' AS item, COUNT(1) AS cnt
FROM indicator_weight_score
WHERE indicator_code = 'L1_SELF_RESCUE';

COMMIT;
