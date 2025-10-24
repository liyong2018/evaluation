-- 检查survey_data表结构
DESCRIBE survey_data;

-- 检查特定区域的调查数据
SELECT
    region_code,
    risk_assessment,
    management_staff,
    population
FROM survey_data
WHERE region_code = '511425001'
LIMIT 1;