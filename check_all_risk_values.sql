-- 检查所有区域的风险评估值分布
SELECT
    risk_assessment,
    COUNT(*) as count,
    GROUP_CONCAT(DISTINCT region_code) as regions
FROM survey_data
GROUP BY risk_assessment
ORDER BY count DESC;