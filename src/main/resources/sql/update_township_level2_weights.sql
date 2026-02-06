-- 更新乡镇减灾能力评估模型二级指标权重
-- 模型ID: 3, 模型名称: 乡镇减灾能力评估模型
-- 二级指标: 队伍管理能力, 风险评估能力, 财政投入能力, 物资储备能力, 医疗保障能力, 自救互救能力, 公众避险能力, 转移安置能力

-- 首先创建存储过程，用于批量更新二级指标权重
DELIMITER $$

DROP PROCEDURE IF EXISTS update_township_level2_weights$$

CREATE PROCEDURE update_township_level2_weights(
    IN p_orgcode VARCHAR(20),
    IN p_year INT,
    IN p_management_capability DECIMAL(10,3),      -- 队伍管理能力
    IN p_risk_assessment DECIMAL(10,3),           -- 风险评估能力
    IN p_funding DECIMAL(10,3),                   -- 财政投入能力
    IN p_material DECIMAL(10,3),                  -- 物资储备能力
    IN p_medical DECIMAL(10,3),                   -- 医疗保障能力
    IN p_self_rescue DECIMAL(10,3),              -- 自救互救能力
    IN p_public_avoidance DECIMAL(10,3),          -- 公众避险能力
    IN p_relocation DECIMAL(10,3)                 -- 转移安置能力
)
BEGIN
    DECLARE v_config_id BIGINT;

    -- 查找权重配置
    SELECT id INTO v_config_id
    FROM weight_config
    WHERE orgcode = p_orgcode
      AND YEAR(create_time) = p_year
      AND config_name = '乡镇减灾能力评估模型'
      AND is_deleted = 0
    LIMIT 1;

    -- 如果配置不存在，先创建
    IF v_config_id IS NULL THEN
        INSERT INTO weight_config (config_name, description, orgcode, create_time, is_deleted)
        VALUES ('乡镇减灾能力评估模型', '乡镇减灾能力评估模型权重配置', p_orgcode, CONCAT(p_year, '-01-01 00:00:00'), 0);
        SET v_config_id = LAST_INSERT_ID();

        -- 初始化默认指标权重
        INSERT INTO indicator_weight (config_id, indicator_code, indicator_name, indicator_level, weight, parent_id, sort_order, create_time) VALUES
        (v_config_id, 'L1_DISASTER_MANAGEMENT', '灾害管理能力', 1, 0.33, NULL, 1, NOW()),
        (v_config_id, 'L1_DISASTER_PREPAREDNESS', '灾害备灾能力', 1, 0.31, NULL, 2, NOW()),
        (v_config_id, 'L1_SELF_RESCUE_TRANSFER', '自救转移能力', 1, 0.36, NULL, 3, NOW()),
        (v_config_id, 'L2_MANAGEMENT_CAPABILITY', '队伍管理能力', 2, 0, NULL, 1, NOW()),
        (v_config_id, 'L2_RISK_ASSESSMENT', '风险评估能力', 2, 0, NULL, 2, NOW()),
        (v_config_id, 'L2_FUNDING', '财政投入能力', 2, 0, NULL, 3, NOW()),
        (v_config_id, 'L2_MATERIAL', '物资储备能力', 2, 0, NULL, 4, NOW()),
        (v_config_id, 'L2_MEDICAL', '医疗保障能力', 2, 0, NULL, 5, NOW()),
        (v_config_id, 'L2_SELF_RESCUE', '自救互救能力', 2, 0, NULL, 6, NOW()),
        (v_config_id, 'L2_PUBLIC_AVOIDANCE', '公众避险能力', 2, 0, NULL, 7, NOW()),
        (v_config_id, 'L2_RELOCATION', '转移安置能力', 2, 0, NULL, 8, NOW());

        -- 更新父级指标ID
        UPDATE indicator_weight SET parent_id = (SELECT id FROM (SELECT id FROM indicator_weight WHERE config_id = v_config_id AND indicator_code = 'L1_DISASTER_MANAGEMENT') AS tmp) WHERE config_id = v_config_id AND indicator_code IN ('L2_MANAGEMENT_CAPABILITY', 'L2_RISK_ASSESSMENT', 'L2_FUNDING');
        UPDATE indicator_weight SET parent_id = (SELECT id FROM (SELECT id FROM indicator_weight WHERE config_id = v_config_id AND indicator_code = 'L1_DISASTER_PREPAREDNESS') AS tmp) WHERE config_id = v_config_id AND indicator_code IN ('L2_MATERIAL', 'L2_MEDICAL');
        UPDATE indicator_weight SET parent_id = (SELECT id FROM (SELECT id FROM indicator_weight WHERE config_id = v_config_id AND indicator_code = 'L1_SELF_RESCUE_TRANSFER') AS tmp) WHERE config_id = v_config_id AND indicator_code IN ('L2_SELF_RESCUE', 'L2_PUBLIC_AVOIDANCE', 'L2_RELOCATION');
    END IF;

    -- 更新二级指标权重
    UPDATE indicator_weight SET weight = p_management_capability WHERE config_id = v_config_id AND indicator_code = 'L2_MANAGEMENT_CAPABILITY';
    UPDATE indicator_weight SET weight = p_risk_assessment WHERE config_id = v_config_id AND indicator_code = 'L2_RISK_ASSESSMENT';
    UPDATE indicator_weight SET weight = p_funding WHERE config_id = v_config_id AND indicator_code = 'L2_FUNDING';
    UPDATE indicator_weight SET weight = p_material WHERE config_id = v_config_id AND indicator_code = 'L2_MATERIAL';
    UPDATE indicator_weight SET weight = p_medical WHERE config_id = v_config_id AND indicator_code = 'L2_MEDICAL';
    UPDATE indicator_weight SET weight = p_self_rescue WHERE config_id = v_config_id AND indicator_code = 'L2_SELF_RESCUE';
    UPDATE indicator_weight SET weight = p_public_avoidance WHERE config_id = v_config_id AND indicator_code = 'L2_PUBLIC_AVOIDANCE';
    UPDATE indicator_weight SET weight = p_relocation WHERE config_id = v_config_id AND indicator_code = 'L2_RELOCATION';

END$$

DELIMITER ;

-- 执行批量更新 (省及各市二级指标权重)
-- 四川省: 队伍管理0.34, 风险评估0.31, 财政投入0.35, 物资储备0.50, 医疗保障0.50, 自救互救0.34, 公众避险0.33, 转移安置0.33
CALL update_township_level2_weights('51', 2020, 0.34, 0.31, 0.35, 0.50, 0.50, 0.34, 0.33, 0.33);

-- 阿坝州
CALL update_township_level2_weights('5132', 2020, 0.36, 0.33, 0.31, 0.53, 0.47, 0.32, 0.32, 0.36);

-- 巴中市
CALL update_township_level2_weights('5119', 2020, 0.35, 0.30, 0.35, 0.50, 0.50, 0.32, 0.33, 0.35);

-- 成都市
CALL update_township_level2_weights('5101', 2020, 0.35, 0.31, 0.34, 0.51, 0.49, 0.37, 0.32, 0.31);

-- 达州市
CALL update_township_level2_weights('5117', 2020, 0.35, 0.33, 0.32, 0.51, 0.49, 0.33, 0.33, 0.34);

-- 德阳市
CALL update_township_level2_weights('5106', 2020, 0.34, 0.33, 0.33, 0.51, 0.49, 0.32, 0.33, 0.35);

-- 甘孜州
CALL update_township_level2_weights('5133', 2020, 0.36, 0.33, 0.31, 0.51, 0.49, 0.33, 0.32, 0.35);

-- 广安市
CALL update_township_level2_weights('5116', 2020, 0.37, 0.31, 0.32, 0.51, 0.49, 0.34, 0.32, 0.34);

-- 广元市
CALL update_township_level2_weights('5108', 2020, 0.34, 0.31, 0.35, 0.50, 0.50, 0.34, 0.33, 0.33);

-- 乐山市
CALL update_township_level2_weights('5111', 2020, 0.36, 0.32, 0.32, 0.51, 0.49, 0.32, 0.33, 0.35);

-- 凉山州
CALL update_township_level2_weights('5134', 2020, 0.35, 0.32, 0.33, 0.51, 0.49, 0.33, 0.34, 0.33);

-- 泸州市
CALL update_township_level2_weights('5105', 2020, 0.35, 0.33, 0.32, 0.48, 0.52, 0.33, 0.33, 0.34);

-- 眉山市
CALL update_township_level2_weights('5114', 2020, 0.37, 0.31, 0.32, 0.51, 0.49, 0.33, 0.33, 0.34);

-- 绵阳市
CALL update_township_level2_weights('5107', 2020, 0.36, 0.34, 0.30, 0.49, 0.51, 0.33, 0.33, 0.34);

-- 南充市
CALL update_township_level2_weights('5113', 2020, 0.35, 0.34, 0.31, 0.50, 0.50, 0.33, 0.32, 0.35);

-- 内江市
CALL update_township_level2_weights('5110', 2020, 0.35, 0.33, 0.32, 0.50, 0.50, 0.33, 0.34, 0.33);

-- 攀枝花市
CALL update_township_level2_weights('5104', 2020, 0.36, 0.33, 0.31, 0.49, 0.51, 0.34, 0.32, 0.34);

-- 遂宁市
CALL update_township_level2_weights('5109', 2020, 0.38, 0.32, 0.30, 0.52, 0.48, 0.32, 0.34, 0.34);

-- 雅安市
CALL update_township_level2_weights('5118', 2020, 0.37, 0.33, 0.30, 0.55, 0.45, 0.32, 0.33, 0.35);

-- 宜宾市
CALL update_township_level2_weights('5115', 2020, 0.37, 0.32, 0.31, 0.52, 0.48, 0.33, 0.33, 0.34);

-- 资阳市
CALL update_township_level2_weights('5120', 2020, 0.34, 0.33, 0.33, 0.50, 0.50, 0.32, 0.34, 0.34);

-- 自贡市
CALL update_township_level2_weights('5103', 2020, 0.35, 0.33, 0.32, 0.48, 0.52, 0.33, 0.33, 0.34);

-- 清理存储过程
DROP PROCEDURE IF EXISTS update_township_level2_weights$$

-- 验证更新结果
SELECT
    wc.orgcode,
    o.name AS org_name,
    -- 灾害管理能力下级指标
    (SELECT weight FROM indicator_weight WHERE config_id = wc.id AND indicator_code = 'L2_MANAGEMENT_CAPABILITY') AS management_capability,
    (SELECT weight FROM indicator_weight WHERE config_id = wc.id AND indicator_code = 'L2_RISK_ASSESSMENT') AS risk_assessment,
    (SELECT weight FROM indicator_weight WHERE config_id = wc.id AND indicator_code = 'L2_FUNDING') AS funding,
    -- 灾害备灾能力下级指标
    (SELECT weight FROM indicator_weight WHERE config_id = wc.id AND indicator_code = 'L2_MATERIAL') AS material,
    (SELECT weight FROM indicator_weight WHERE config_id = wc.id AND indicator_code = 'L2_MEDICAL') AS medical,
    -- 自救转移能力下级指标
    (SELECT weight FROM indicator_weight WHERE config_id = wc.id AND indicator_code = 'L2_SELF_RESCUE') AS self_rescue,
    (SELECT weight FROM indicator_weight WHERE config_id = wc.id AND indicator_code = 'L2_PUBLIC_AVOIDANCE') AS public_avoidance,
    (SELECT weight FROM indicator_weight WHERE config_id = wc.id AND indicator_code = 'L2_RELOCATION') AS relocation
FROM weight_config wc
JOIN organization o ON wc.orgcode = o.code
WHERE wc.config_name = '乡镇减灾能力评估模型'
  AND YEAR(wc.create_time) = 2020
  AND wc.is_deleted = 0
ORDER BY o.code;
