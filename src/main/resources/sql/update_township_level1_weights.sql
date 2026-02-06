-- 更新乡镇减灾能力评估模型一级指标权重
-- 模型ID: 3, 模型名称: 乡镇减灾能力评估模型
-- 一级指标: 灾害管理能力(L1_DISASTER_MANAGEMENT), 灾害备灾能力(L1_DISASTER_PREPAREDNESS), 自救转移能力(L1_SELF_RESCUE_TRANSFER)

-- 首先创建存储过程，用于批量更新权重
DELIMITER $$

DROP PROCEDURE IF EXISTS update_township_level1_weights$$

CREATE PROCEDURE update_township_level1_weights(
    IN p_orgcode VARCHAR(20),
    IN p_year INT,
    IN p_management_weight DECIMAL(10,3),
    IN p_preparedness_weight DECIMAL(10,3),
    IN p_transfer_weight DECIMAL(10,3)
)
BEGIN
    DECLARE v_config_id BIGINT;
    DECLARE v_management_id BIGINT;
    DECLARE v_preparedness_id BIGINT;
    DECLARE v_transfer_id BIGINT;

    -- 查找或创建权重配置
    SELECT id INTO v_config_id
    FROM weight_config
    WHERE orgcode = p_orgcode
      AND YEAR(create_time) = p_year
      AND config_name = '乡镇减灾能力评估模型'
      AND is_deleted = 0
    LIMIT 1;

    -- 如果配置不存在，创建新配置
    IF v_config_id IS NULL THEN
        INSERT INTO weight_config (config_name, description, orgcode, create_time, is_deleted)
        VALUES ('乡镇减灾能力评估模型', '乡镇减灾能力评估模型权重配置', p_orgcode, CONCAT(p_year, '-01-01 00:00:00'), 0);

        SET v_config_id = LAST_INSERT_ID();

        -- 初始化默认指标权重
        INSERT INTO indicator_weight (config_id, indicator_code, indicator_name, indicator_level, weight, parent_id, sort_order, create_time) VALUES
        (v_config_id, 'L1_DISASTER_MANAGEMENT', '灾害管理能力', 1, 0.33, NULL, 1, NOW()),
        (v_config_id, 'L1_DISASTER_PREPAREDNESS', '灾害备灾能力', 1, 0.31, NULL, 2, NOW()),
        (v_config_id, 'L1_SELF_RESCUE_TRANSFER', '自救转移能力', 1, 0.36, NULL, 3, NOW()),
        (v_config_id, 'L2_MANAGEMENT_CAPABILITY', '队伍管理能力', 2, 0, (SELECT id FROM (SELECT id FROM indicator_weight WHERE config_id = v_config_id AND indicator_code = 'L1_DISASTER_MANAGEMENT') AS tmp), 1, NOW()),
        (v_config_id, 'L2_RISK_ASSESSMENT', '风险评估能力', 2, 0, (SELECT id FROM (SELECT id FROM indicator_weight WHERE config_id = v_config_id AND indicator_code = 'L1_DISASTER_MANAGEMENT') AS tmp), 2, NOW()),
        (v_config_id, 'L2_FUNDING', '财政投入能力', 2, 0, (SELECT id FROM (SELECT id FROM indicator_weight WHERE config_id = v_config_id AND indicator_code = 'L1_DISASTER_MANAGEMENT') AS tmp), 3, NOW()),
        (v_config_id, 'L2_MATERIAL', '物资储备能力', 2, 0, (SELECT id FROM (SELECT id FROM indicator_weight WHERE config_id = v_config_id AND indicator_code = 'L1_DISASTER_PREPAREDNESS') AS tmp), 1, NOW()),
        (v_config_id, 'L2_MEDICAL', '医疗保障能力', 2, 0, (SELECT id FROM (SELECT id FROM indicator_weight WHERE config_id = v_config_id AND indicator_code = 'L1_DISASTER_PREPAREDNESS') AS tmp), 2, NOW()),
        (v_config_id, 'L2_SELF_RESCUE', '自救互救能力', 2, 0, (SELECT id FROM (SELECT id FROM indicator_weight WHERE config_id = v_config_id AND indicator_code = 'L1_SELF_RESCUE_TRANSFER') AS tmp), 1, NOW()),
        (v_config_id, 'L2_PUBLIC_AVOIDANCE', '公众避险能力', 2, 0, (SELECT id FROM (SELECT id FROM indicator_weight WHERE config_id = v_config_id AND indicator_code = 'L1_SELF_RESCUE_TRANSFER') AS tmp), 2, NOW()),
        (v_config_id, 'L2_RELOCATION', '转移安置能力', 2, 0, (SELECT id FROM (SELECT id FROM indicator_weight WHERE config_id = v_config_id AND indicator_code = 'L1_SELF_RESCUE_TRANSFER') AS tmp), 3, NOW());
    END IF;

    -- 获取一级指标ID
    SELECT id INTO v_management_id FROM indicator_weight WHERE config_id = v_config_id AND indicator_code = 'L1_DISASTER_MANAGEMENT';
    SELECT id INTO v_preparedness_id FROM indicator_weight WHERE config_id = v_config_id AND indicator_code = 'L1_DISASTER_PREPAREDNESS';
    SELECT id INTO v_transfer_id FROM indicator_weight WHERE config_id = v_config_id AND indicator_code = 'L1_SELF_RESCUE_TRANSFER';

    -- 更新一级指标权重
    UPDATE indicator_weight SET weight = p_management_weight WHERE id = v_management_id;
    UPDATE indicator_weight SET weight = p_preparedness_weight WHERE id = v_preparedness_id;
    UPDATE indicator_weight SET weight = p_transfer_weight WHERE id = v_transfer_id;

END$$

DELIMITER ;

-- 执行批量更新
-- 省级: 四川省 (51)
CALL update_township_level1_weights('51', 2020, 0.33, 0.31, 0.36);

-- 市级
CALL update_township_level1_weights('5132', 2020, 0.33, 0.33, 0.34);  -- 阿坝州
CALL update_township_level1_weights('5119', 2020, 0.33, 0.31, 0.36);  -- 巴中市
CALL update_township_level1_weights('5101', 2020, 0.33, 0.31, 0.36);  -- 成都市
CALL update_township_level1_weights('5117', 2020, 0.33, 0.32, 0.35);  -- 达州市
CALL update_township_level1_weights('5106', 2020, 0.34, 0.31, 0.35);  -- 德阳市
CALL update_township_level1_weights('5133', 2020, 0.34, 0.32, 0.34);  -- 甘孜州
CALL update_township_level1_weights('5116', 2020, 0.34, 0.32, 0.34);  -- 广安市
CALL update_township_level1_weights('5108', 2020, 0.34, 0.34, 0.32);  -- 广元市
CALL update_township_level1_weights('5111', 2020, 0.33, 0.32, 0.35);  -- 乐山市
CALL update_township_level1_weights('5134', 2020, 0.33, 0.32, 0.35);  -- 凉山州
CALL update_township_level1_weights('5105', 2020, 0.33, 0.33, 0.34);  -- 泸州市
CALL update_township_level1_weights('5114', 2020, 0.33, 0.32, 0.35);  -- 眉山市
CALL update_township_level1_weights('5107', 2020, 0.33, 0.32, 0.35);  -- 绵阳市
CALL update_township_level1_weights('5113', 2020, 0.35, 0.32, 0.33);  -- 南充市
CALL update_township_level1_weights('5110', 2020, 0.33, 0.32, 0.35);  -- 内江市
CALL update_township_level1_weights('5104', 2020, 0.34, 0.32, 0.34);  -- 攀枝花市
CALL update_township_level1_weights('5109', 2020, 0.34, 0.32, 0.34);  -- 遂宁市
CALL update_township_level1_weights('5118', 2020, 0.33, 0.32, 0.35);  -- 雅安市
CALL update_township_level1_weights('5115', 2020, 0.33, 0.32, 0.35);  -- 宜宾市
CALL update_township_level1_weights('5120', 2020, 0.34, 0.33, 0.33);  -- 资阳市
CALL update_township_level1_weights('5103', 2020, 0.33, 0.33, 0.34);  -- 自贡市

-- 清理存储过程
DROP PROCEDURE IF EXISTS update_township_level1_weights$$

-- 验证更新结果
SELECT
    wc.orgcode,
    o.name AS org_name,
    iw1.indicator_code AS management_code,
    iw1.weight AS management_weight,
    iw2.indicator_code AS preparedness_code,
    iw2.weight AS preparedness_weight,
    iw3.indicator_code AS transfer_code,
    iw3.weight AS transfer_weight,
    (iw1.weight + iw2.weight + iw3.weight) AS total_weight
FROM weight_config wc
JOIN organization o ON wc.orgcode = o.code
JOIN indicator_weight iw1 ON wc.id = iw1.config_id AND iw1.indicator_code = 'L1_DISASTER_MANAGEMENT'
JOIN indicator_weight iw2 ON wc.id = iw2.config_id AND iw2.indicator_code = 'L1_DISASTER_PREPAREDNESS'
JOIN indicator_weight iw3 ON wc.id = iw3.config_id AND iw3.indicator_code = 'L1_SELF_RESCUE_TRANSFER'
WHERE wc.config_name = '乡镇减灾能力评估模型'
  AND YEAR(wc.create_time) = 2020
  AND wc.is_deleted = 0
ORDER BY o.code;
