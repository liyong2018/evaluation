-- 检查特定区域的调查数据
SELECT
    region_code,
    risk_assessment,
    management_staff,
    population,
    emergency_plan,
    rescue_team,
    material_reserve
FROM survey_data
WHERE region_code = '511425001'
LIMIT 1;