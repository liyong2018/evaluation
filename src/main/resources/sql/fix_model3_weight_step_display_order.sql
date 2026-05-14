-- 区县-乡镇（街道）模型 step3 同时保留两类定权：
-- 1) 二级指标定权：归一化值 * 二级权重，用于页面上“队伍管理能力定权”等列。
-- 2) 综合定权：归一化值 * 一级权重 * 二级权重，保留给后续综合 TOPSIS 使用。
--
-- 之前综合定权排在前面且也命名为“定权”，导致页面第一列看起来像二级定权但数值被一级权重再乘了一次。

UPDATE step_algorithm sa
JOIN model_step ms ON ms.id = sa.step_id
SET
    sa.algorithm_order = CASE sa.algorithm_code
        WHEN 'MANAGEMENT_SECONDARY' THEN 1
        WHEN 'RISK_ASSESSMENT_SECONDARY' THEN 2
        WHEN 'FUNDING_SECONDARY' THEN 3
        WHEN 'MATERIAL_RESERVE_SECONDARY' THEN 4
        WHEN 'MEDICAL_SUPPORT_SECONDARY' THEN 5
        WHEN 'SELF_RESCUE_SECONDARY' THEN 6
        WHEN 'PUBLIC_AVOIDANCE_SECONDARY' THEN 7
        WHEN 'RELOCATION_SECONDARY' THEN 8
        WHEN 'MANAGEMENT_WEIGHTED' THEN 101
        WHEN 'RISK_ASSESSMENT_WEIGHTED' THEN 102
        WHEN 'FUNDING_WEIGHTED' THEN 103
        WHEN 'MATERIAL_RESERVE_WEIGHTED' THEN 104
        WHEN 'MEDICAL_SUPPORT_WEIGHTED' THEN 105
        WHEN 'SELF_RESCUE_WEIGHTED' THEN 106
        WHEN 'PUBLIC_AVOIDANCE_WEIGHTED' THEN 107
        WHEN 'RELOCATION_WEIGHTED' THEN 108
        ELSE sa.algorithm_order
    END,
    sa.algorithm_name = CASE sa.algorithm_code
        WHEN 'MANAGEMENT_SECONDARY' THEN '队伍管理能力定权'
        WHEN 'RISK_ASSESSMENT_SECONDARY' THEN '风险评估能力定权'
        WHEN 'FUNDING_SECONDARY' THEN '财政投入能力定权'
        WHEN 'MATERIAL_RESERVE_SECONDARY' THEN '物资储备能力定权'
        WHEN 'MEDICAL_SUPPORT_SECONDARY' THEN '医疗保障能力定权'
        WHEN 'SELF_RESCUE_SECONDARY' THEN '自救互救能力定权'
        WHEN 'PUBLIC_AVOIDANCE_SECONDARY' THEN '公众避险能力定权'
        WHEN 'RELOCATION_SECONDARY' THEN '转移安置能力定权'
        WHEN 'MANAGEMENT_WEIGHTED' THEN '队伍管理能力综合定权'
        WHEN 'RISK_ASSESSMENT_WEIGHTED' THEN '风险评估能力综合定权'
        WHEN 'FUNDING_WEIGHTED' THEN '财政投入能力综合定权'
        WHEN 'MATERIAL_RESERVE_WEIGHTED' THEN '物资储备能力综合定权'
        WHEN 'MEDICAL_SUPPORT_WEIGHTED' THEN '医疗保障能力综合定权'
        WHEN 'SELF_RESCUE_WEIGHTED' THEN '自救互救能力综合定权'
        WHEN 'PUBLIC_AVOIDANCE_WEIGHTED' THEN '公众避险能力综合定权'
        WHEN 'RELOCATION_WEIGHTED' THEN '转移安置能力综合定权'
        ELSE sa.algorithm_name
    END
WHERE ms.model_id = 3
  AND ms.step_order = 3
  AND (
      sa.algorithm_code LIKE '%\_SECONDARY'
      OR sa.algorithm_code LIKE '%\_WEIGHTED'
  );

SELECT sa.algorithm_order, sa.algorithm_code, sa.algorithm_name, sa.ql_expression, sa.output_param
FROM model_step ms
JOIN step_algorithm sa ON sa.step_id = ms.id
WHERE ms.model_id = 3 AND ms.step_order = 3
ORDER BY sa.algorithm_order, sa.id;
