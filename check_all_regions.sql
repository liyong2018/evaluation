-- 检查survey_data表中所有区域
SELECT DISTINCT region_code
FROM survey_data
ORDER BY region_code
LIMIT 20;