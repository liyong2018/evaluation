-- 检查确切区域代码511425001的数据
SELECT
    region_code,
    risk_assessment,
    management_staff,
    population
FROM survey_data
WHERE region_code = '511425001';