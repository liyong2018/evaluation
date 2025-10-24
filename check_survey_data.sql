-- 检查调查数据
SELECT
    region_code,
    risk_assessment,
    management_staff,
    population
FROM survey_data
WHERE region_code = '110112'  -- 测试区域
LIMIT 10;