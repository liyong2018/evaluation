SET NAMES utf8mb4;

-- =============================================================
-- Insert city-level comprehensive disaster reduction weights
-- Model ID: 20 (2020年市级综合减灾能力评估模型)
-- Year: 2020, Data source: baseline
-- =============================================================

-- ─── 四川省 (orgcode=51) ───
DELETE wc FROM weight_config wc LEFT JOIN indicator_weight iw ON iw.config_id = wc.id WHERE wc.orgcode = '51' AND wc.model_id = 20 AND wc.year = 2020 AND wc.is_deleted = 0;
DELETE FROM indicator_weight WHERE config_id IN (SELECT id FROM weight_config WHERE orgcode = '51' AND model_id = 20 AND year = 2020);
INSERT INTO weight_config (config_name, description, orgcode, data_source, year, model_id, is_deleted) VALUES ('综合减灾能力评估权重', '2020年四川省综合减灾能力评估权重', '51', 'baseline', 2020, 20, 0);
SET @config_id = LAST_INSERT_ID();

INSERT INTO indicator_weight (config_id, indicator_code, indicator_name, indicator_level, weight, parent_id, sort_order) VALUES
(@config_id, 'L1_GOVERNMENT', '政府减灾能力', 1, 0.23, NULL, 1),
(@config_id, 'L1_ENTERPRISE', '企业减灾能力', 1, 0.15, NULL, 2),
(@config_id, 'L1_SOCIAL_ORGANIZATION', '社会组织减灾能力', 1, 0.17, NULL, 3),
(@config_id, 'L1_TOWNSHIP', '乡镇（街道）减灾能力', 1, 0.16, NULL, 4),
(@config_id, 'L1_COMMUNITY', '社区（行政区）减灾能力', 1, 0.15, NULL, 5),
(@config_id, 'L1_FAMILY', '家庭减灾能力', 1, 0.14, NULL, 6);

SET @p0 = (SELECT id FROM indicator_weight WHERE config_id = @config_id AND indicator_code = 'L1_GOVERNMENT');
SET @p1 = (SELECT id FROM indicator_weight WHERE config_id = @config_id AND indicator_code = 'L1_ENTERPRISE');
SET @p2 = (SELECT id FROM indicator_weight WHERE config_id = @config_id AND indicator_code = 'L1_SOCIAL_ORGANIZATION');
SET @p3 = (SELECT id FROM indicator_weight WHERE config_id = @config_id AND indicator_code = 'L1_TOWNSHIP');
SET @p4 = (SELECT id FROM indicator_weight WHERE config_id = @config_id AND indicator_code = 'L1_COMMUNITY');
SET @p5 = (SELECT id FROM indicator_weight WHERE config_id = @config_id AND indicator_code = 'L1_FAMILY');

INSERT INTO indicator_weight (config_id, indicator_code, indicator_name, indicator_level, weight, parent_id, sort_order) VALUES
(@config_id, 'L2_GOV_MANAGEMENT', '管理能力', 2, 0.18, @p0, 1),
(@config_id, 'L2_GOV_ENGINEERING', '工程设防能力', 2, 0.17, @p0, 2),
(@config_id, 'L2_GOV_MONITORING', '监测预警能力', 2, 0.17, @p0, 3),
(@config_id, 'L2_GOV_MATERIAL', '物资储备能力', 2, 0.16, @p0, 4),
(@config_id, 'L2_GOV_RESCUE_TEAM', '专业队伍救援能力', 2, 0.17, @p0, 5),
(@config_id, 'L2_GOV_RELOCATION', '转移安置能力', 2, 0.15, @p0, 6),
(@config_id, 'L2_ENT_ENGINEERING_RESCUE', '大型企业应急救援能力', 2, 0.54, @p1, 7),
(@config_id, 'L2_ENT_INSURANCE', '保险和再保险企业减灾能力', 2, 0.46, @p1, 8),
(@config_id, 'L2_SOC_MATERIAL', '物资储备能力', 2, 0.24, @p2, 9),
(@config_id, 'L2_SOC_TRANSPORT', '应急运输能力', 2, 0.25, @p2, 10),
(@config_id, 'L2_SOC_RESCUE', '应急救援能力', 2, 0.27, @p2, 11),
(@config_id, 'L2_SOC_PUBLICITY', '科普宣传能力', 2, 0.24, @p2, 12),
(@config_id, 'L2_TWN_DISASTER_MANAGEMENT', '灾害管理能力', 2, 0.33, @p3, 13),
(@config_id, 'L2_TWN_DISASTER_PREPAREDNESS', '灾害备灾能力', 2, 0.31, @p3, 14),
(@config_id, 'L2_TWN_SELF_RESCUE_TRANSFER', '自救转移能力', 2, 0.36, @p3, 15),
(@config_id, 'L2_COM_DISASTER_MANAGEMENT', '灾害管理能力', 2, 0.33, @p4, 16),
(@config_id, 'L2_COM_DISASTER_PREPAREDNESS', '灾害备灾能力', 2, 0.30, @p4, 17),
(@config_id, 'L2_COM_SELF_RESCUE_TRANSFER', '自救转移能力', 2, 0.37, @p4, 18),
(@config_id, 'L2_FAM_VULNERABILITY', '家庭脆弱性', 2, 0.27, @p5, 19),
(@config_id, 'L2_FAM_MATERIAL', '防灾物资储备能力', 2, 0.21, @p5, 20),
(@config_id, 'L2_FAM_INFORMATION', '灾害信息获取能力', 2, 0.26, @p5, 21),
(@config_id, 'L2_FAM_SELF_RESCUE', '灾害自救互救能力', 2, 0.26, @p5, 22);

-- ─── 阿坝州 (orgcode=5132) ───
DELETE wc FROM weight_config wc LEFT JOIN indicator_weight iw ON iw.config_id = wc.id WHERE wc.orgcode = '5132' AND wc.model_id = 20 AND wc.year = 2020 AND wc.is_deleted = 0;
DELETE FROM indicator_weight WHERE config_id IN (SELECT id FROM weight_config WHERE orgcode = '5132' AND model_id = 20 AND year = 2020);
INSERT INTO weight_config (config_name, description, orgcode, data_source, year, model_id, is_deleted) VALUES ('综合减灾能力评估权重', '2020年阿坝州综合减灾能力评估权重', '5132', 'baseline', 2020, 20, 0);
SET @config_id = LAST_INSERT_ID();

INSERT INTO indicator_weight (config_id, indicator_code, indicator_name, indicator_level, weight, parent_id, sort_order) VALUES
(@config_id, 'L1_GOVERNMENT', '政府减灾能力', 1, 0.20, NULL, 1),
(@config_id, 'L1_ENTERPRISE', '企业减灾能力', 1, 0.16, NULL, 2),
(@config_id, 'L1_SOCIAL_ORGANIZATION', '社会组织减灾能力', 1, 0.15, NULL, 3),
(@config_id, 'L1_TOWNSHIP', '乡镇（街道）减灾能力', 1, 0.17, NULL, 4),
(@config_id, 'L1_COMMUNITY', '社区（行政区）减灾能力', 1, 0.16, NULL, 5),
(@config_id, 'L1_FAMILY', '家庭减灾能力', 1, 0.16, NULL, 6);

SET @p0 = (SELECT id FROM indicator_weight WHERE config_id = @config_id AND indicator_code = 'L1_GOVERNMENT');
SET @p1 = (SELECT id FROM indicator_weight WHERE config_id = @config_id AND indicator_code = 'L1_ENTERPRISE');
SET @p2 = (SELECT id FROM indicator_weight WHERE config_id = @config_id AND indicator_code = 'L1_SOCIAL_ORGANIZATION');
SET @p3 = (SELECT id FROM indicator_weight WHERE config_id = @config_id AND indicator_code = 'L1_TOWNSHIP');
SET @p4 = (SELECT id FROM indicator_weight WHERE config_id = @config_id AND indicator_code = 'L1_COMMUNITY');
SET @p5 = (SELECT id FROM indicator_weight WHERE config_id = @config_id AND indicator_code = 'L1_FAMILY');

INSERT INTO indicator_weight (config_id, indicator_code, indicator_name, indicator_level, weight, parent_id, sort_order) VALUES
(@config_id, 'L2_GOV_MANAGEMENT', '管理能力', 2, 0.18, @p0, 1),
(@config_id, 'L2_GOV_ENGINEERING', '工程设防能力', 2, 0.16, @p0, 2),
(@config_id, 'L2_GOV_MONITORING', '监测预警能力', 2, 0.16, @p0, 3),
(@config_id, 'L2_GOV_MATERIAL', '物资储备能力', 2, 0.17, @p0, 4),
(@config_id, 'L2_GOV_RESCUE_TEAM', '专业队伍救援能力', 2, 0.16, @p0, 5),
(@config_id, 'L2_GOV_RELOCATION', '转移安置能力', 2, 0.17, @p0, 6),
(@config_id, 'L2_ENT_ENGINEERING_RESCUE', '大型企业应急救援能力', 2, 0.52, @p1, 7),
(@config_id, 'L2_ENT_INSURANCE', '保险和再保险企业减灾能力', 2, 0.48, @p1, 8),
(@config_id, 'L2_SOC_MATERIAL', '物资储备能力', 2, 0.25, @p2, 9),
(@config_id, 'L2_SOC_TRANSPORT', '应急运输能力', 2, 0.24, @p2, 10),
(@config_id, 'L2_SOC_RESCUE', '应急救援能力', 2, 0.26, @p2, 11),
(@config_id, 'L2_SOC_PUBLICITY', '科普宣传能力', 2, 0.25, @p2, 12),
(@config_id, 'L2_TWN_DISASTER_MANAGEMENT', '灾害管理能力', 2, 0.33, @p3, 13),
(@config_id, 'L2_TWN_DISASTER_PREPAREDNESS', '灾害备灾能力', 2, 0.33, @p3, 14),
(@config_id, 'L2_TWN_SELF_RESCUE_TRANSFER', '自救转移能力', 2, 0.34, @p3, 15),
(@config_id, 'L2_COM_DISASTER_MANAGEMENT', '灾害管理能力', 2, 0.33, @p4, 16),
(@config_id, 'L2_COM_DISASTER_PREPAREDNESS', '灾害备灾能力', 2, 0.32, @p4, 17),
(@config_id, 'L2_COM_SELF_RESCUE_TRANSFER', '自救转移能力', 2, 0.35, @p4, 18),
(@config_id, 'L2_FAM_VULNERABILITY', '家庭脆弱性', 2, 0.23, @p5, 19),
(@config_id, 'L2_FAM_MATERIAL', '防灾物资储备能力', 2, 0.23, @p5, 20),
(@config_id, 'L2_FAM_INFORMATION', '灾害信息获取能力', 2, 0.28, @p5, 21),
(@config_id, 'L2_FAM_SELF_RESCUE', '灾害自救互救能力', 2, 0.26, @p5, 22);

-- ─── 巴中市 (orgcode=5119) ───
DELETE wc FROM weight_config wc LEFT JOIN indicator_weight iw ON iw.config_id = wc.id WHERE wc.orgcode = '5119' AND wc.model_id = 20 AND wc.year = 2020 AND wc.is_deleted = 0;
DELETE FROM indicator_weight WHERE config_id IN (SELECT id FROM weight_config WHERE orgcode = '5119' AND model_id = 20 AND year = 2020);
INSERT INTO weight_config (config_name, description, orgcode, data_source, year, model_id, is_deleted) VALUES ('综合减灾能力评估权重', '2020年巴中市综合减灾能力评估权重', '5119', 'baseline', 2020, 20, 0);
SET @config_id = LAST_INSERT_ID();

INSERT INTO indicator_weight (config_id, indicator_code, indicator_name, indicator_level, weight, parent_id, sort_order) VALUES
(@config_id, 'L1_GOVERNMENT', '政府减灾能力', 1, 0.19, NULL, 1),
(@config_id, 'L1_ENTERPRISE', '企业减灾能力', 1, 0.15, NULL, 2),
(@config_id, 'L1_SOCIAL_ORGANIZATION', '社会组织减灾能力', 1, 0.17, NULL, 3),
(@config_id, 'L1_TOWNSHIP', '乡镇（街道）减灾能力', 1, 0.18, NULL, 4),
(@config_id, 'L1_COMMUNITY', '社区（行政区）减灾能力', 1, 0.16, NULL, 5),
(@config_id, 'L1_FAMILY', '家庭减灾能力', 1, 0.15, NULL, 6);

SET @p0 = (SELECT id FROM indicator_weight WHERE config_id = @config_id AND indicator_code = 'L1_GOVERNMENT');
SET @p1 = (SELECT id FROM indicator_weight WHERE config_id = @config_id AND indicator_code = 'L1_ENTERPRISE');
SET @p2 = (SELECT id FROM indicator_weight WHERE config_id = @config_id AND indicator_code = 'L1_SOCIAL_ORGANIZATION');
SET @p3 = (SELECT id FROM indicator_weight WHERE config_id = @config_id AND indicator_code = 'L1_TOWNSHIP');
SET @p4 = (SELECT id FROM indicator_weight WHERE config_id = @config_id AND indicator_code = 'L1_COMMUNITY');
SET @p5 = (SELECT id FROM indicator_weight WHERE config_id = @config_id AND indicator_code = 'L1_FAMILY');

INSERT INTO indicator_weight (config_id, indicator_code, indicator_name, indicator_level, weight, parent_id, sort_order) VALUES
(@config_id, 'L2_GOV_MANAGEMENT', '管理能力', 2, 0.17, @p0, 1),
(@config_id, 'L2_GOV_ENGINEERING', '工程设防能力', 2, 0.17, @p0, 2),
(@config_id, 'L2_GOV_MONITORING', '监测预警能力', 2, 0.18, @p0, 3),
(@config_id, 'L2_GOV_MATERIAL', '物资储备能力', 2, 0.16, @p0, 4),
(@config_id, 'L2_GOV_RESCUE_TEAM', '专业队伍救援能力', 2, 0.16, @p0, 5),
(@config_id, 'L2_GOV_RELOCATION', '转移安置能力', 2, 0.16, @p0, 6),
(@config_id, 'L2_ENT_ENGINEERING_RESCUE', '大型企业应急救援能力', 2, 0.51, @p1, 7),
(@config_id, 'L2_ENT_INSURANCE', '保险和再保险企业减灾能力', 2, 0.49, @p1, 8),
(@config_id, 'L2_SOC_MATERIAL', '物资储备能力', 2, 0.25, @p2, 9),
(@config_id, 'L2_SOC_TRANSPORT', '应急运输能力', 2, 0.24, @p2, 10),
(@config_id, 'L2_SOC_RESCUE', '应急救援能力', 2, 0.26, @p2, 11),
(@config_id, 'L2_SOC_PUBLICITY', '科普宣传能力', 2, 0.25, @p2, 12),
(@config_id, 'L2_TWN_DISASTER_MANAGEMENT', '灾害管理能力', 2, 0.33, @p3, 13),
(@config_id, 'L2_TWN_DISASTER_PREPAREDNESS', '灾害备灾能力', 2, 0.32, @p3, 14),
(@config_id, 'L2_TWN_SELF_RESCUE_TRANSFER', '自救转移能力', 2, 0.35, @p3, 15),
(@config_id, 'L2_COM_DISASTER_MANAGEMENT', '灾害管理能力', 2, 0.33, @p4, 16),
(@config_id, 'L2_COM_DISASTER_PREPAREDNESS', '灾害备灾能力', 2, 0.32, @p4, 17),
(@config_id, 'L2_COM_SELF_RESCUE_TRANSFER', '自救转移能力', 2, 0.35, @p4, 18),
(@config_id, 'L2_FAM_VULNERABILITY', '家庭脆弱性', 2, 0.24, @p5, 19),
(@config_id, 'L2_FAM_MATERIAL', '防灾物资储备能力', 2, 0.25, @p5, 20),
(@config_id, 'L2_FAM_INFORMATION', '灾害信息获取能力', 2, 0.26, @p5, 21),
(@config_id, 'L2_FAM_SELF_RESCUE', '灾害自救互救能力', 2, 0.25, @p5, 22);

-- ─── 成都市 (orgcode=5101) ───
DELETE wc FROM weight_config wc LEFT JOIN indicator_weight iw ON iw.config_id = wc.id WHERE wc.orgcode = '5101' AND wc.model_id = 20 AND wc.year = 2020 AND wc.is_deleted = 0;
DELETE FROM indicator_weight WHERE config_id IN (SELECT id FROM weight_config WHERE orgcode = '5101' AND model_id = 20 AND year = 2020);
INSERT INTO weight_config (config_name, description, orgcode, data_source, year, model_id, is_deleted) VALUES ('综合减灾能力评估权重', '2020年成都市综合减灾能力评估权重', '5101', 'baseline', 2020, 20, 0);
SET @config_id = LAST_INSERT_ID();

INSERT INTO indicator_weight (config_id, indicator_code, indicator_name, indicator_level, weight, parent_id, sort_order) VALUES
(@config_id, 'L1_GOVERNMENT', '政府减灾能力', 1, 0.21, NULL, 1),
(@config_id, 'L1_ENTERPRISE', '企业减灾能力', 1, 0.16, NULL, 2),
(@config_id, 'L1_SOCIAL_ORGANIZATION', '社会组织减灾能力', 1, 0.14, NULL, 3),
(@config_id, 'L1_TOWNSHIP', '乡镇（街道）减灾能力', 1, 0.18, NULL, 4),
(@config_id, 'L1_COMMUNITY', '社区（行政区）减灾能力', 1, 0.17, NULL, 5),
(@config_id, 'L1_FAMILY', '家庭减灾能力', 1, 0.14, NULL, 6);

SET @p0 = (SELECT id FROM indicator_weight WHERE config_id = @config_id AND indicator_code = 'L1_GOVERNMENT');
SET @p1 = (SELECT id FROM indicator_weight WHERE config_id = @config_id AND indicator_code = 'L1_ENTERPRISE');
SET @p2 = (SELECT id FROM indicator_weight WHERE config_id = @config_id AND indicator_code = 'L1_SOCIAL_ORGANIZATION');
SET @p3 = (SELECT id FROM indicator_weight WHERE config_id = @config_id AND indicator_code = 'L1_TOWNSHIP');
SET @p4 = (SELECT id FROM indicator_weight WHERE config_id = @config_id AND indicator_code = 'L1_COMMUNITY');
SET @p5 = (SELECT id FROM indicator_weight WHERE config_id = @config_id AND indicator_code = 'L1_FAMILY');

INSERT INTO indicator_weight (config_id, indicator_code, indicator_name, indicator_level, weight, parent_id, sort_order) VALUES
(@config_id, 'L2_GOV_MANAGEMENT', '管理能力', 2, 0.18, @p0, 1),
(@config_id, 'L2_GOV_ENGINEERING', '工程设防能力', 2, 0.16, @p0, 2),
(@config_id, 'L2_GOV_MONITORING', '监测预警能力', 2, 0.17, @p0, 3),
(@config_id, 'L2_GOV_MATERIAL', '物资储备能力', 2, 0.14, @p0, 4),
(@config_id, 'L2_GOV_RESCUE_TEAM', '专业队伍救援能力', 2, 0.19, @p0, 5),
(@config_id, 'L2_GOV_RELOCATION', '转移安置能力', 2, 0.16, @p0, 6),
(@config_id, 'L2_ENT_ENGINEERING_RESCUE', '大型企业应急救援能力', 2, 0.60, @p1, 7),
(@config_id, 'L2_ENT_INSURANCE', '保险和再保险企业减灾能力', 2, 0.40, @p1, 8),
(@config_id, 'L2_SOC_MATERIAL', '物资储备能力', 2, 0.25, @p2, 9),
(@config_id, 'L2_SOC_TRANSPORT', '应急运输能力', 2, 0.24, @p2, 10),
(@config_id, 'L2_SOC_RESCUE', '应急救援能力', 2, 0.27, @p2, 11),
(@config_id, 'L2_SOC_PUBLICITY', '科普宣传能力', 2, 0.24, @p2, 12),
(@config_id, 'L2_TWN_DISASTER_MANAGEMENT', '灾害管理能力', 2, 0.33, @p3, 13),
(@config_id, 'L2_TWN_DISASTER_PREPAREDNESS', '灾害备灾能力', 2, 0.31, @p3, 14),
(@config_id, 'L2_TWN_SELF_RESCUE_TRANSFER', '自救转移能力', 2, 0.36, @p3, 15),
(@config_id, 'L2_COM_DISASTER_MANAGEMENT', '灾害管理能力', 2, 0.33, @p4, 16),
(@config_id, 'L2_COM_DISASTER_PREPAREDNESS', '灾害备灾能力', 2, 0.31, @p4, 17),
(@config_id, 'L2_COM_SELF_RESCUE_TRANSFER', '自救转移能力', 2, 0.36, @p4, 18),
(@config_id, 'L2_FAM_VULNERABILITY', '家庭脆弱性', 2, 0.24, @p5, 19),
(@config_id, 'L2_FAM_MATERIAL', '防灾物资储备能力', 2, 0.21, @p5, 20),
(@config_id, 'L2_FAM_INFORMATION', '灾害信息获取能力', 2, 0.28, @p5, 21),
(@config_id, 'L2_FAM_SELF_RESCUE', '灾害自救互救能力', 2, 0.27, @p5, 22);

-- ─── 达州市 (orgcode=5117) ───
DELETE wc FROM weight_config wc LEFT JOIN indicator_weight iw ON iw.config_id = wc.id WHERE wc.orgcode = '5117' AND wc.model_id = 20 AND wc.year = 2020 AND wc.is_deleted = 0;
DELETE FROM indicator_weight WHERE config_id IN (SELECT id FROM weight_config WHERE orgcode = '5117' AND model_id = 20 AND year = 2020);
INSERT INTO weight_config (config_name, description, orgcode, data_source, year, model_id, is_deleted) VALUES ('综合减灾能力评估权重', '2020年达州市综合减灾能力评估权重', '5117', 'baseline', 2020, 20, 0);
SET @config_id = LAST_INSERT_ID();

INSERT INTO indicator_weight (config_id, indicator_code, indicator_name, indicator_level, weight, parent_id, sort_order) VALUES
(@config_id, 'L1_GOVERNMENT', '政府减灾能力', 1, 0.19, NULL, 1),
(@config_id, 'L1_ENTERPRISE', '企业减灾能力', 1, 0.16, NULL, 2),
(@config_id, 'L1_SOCIAL_ORGANIZATION', '社会组织减灾能力', 1, 0.16, NULL, 3),
(@config_id, 'L1_TOWNSHIP', '乡镇（街道）减灾能力', 1, 0.18, NULL, 4),
(@config_id, 'L1_COMMUNITY', '社区（行政区）减灾能力', 1, 0.16, NULL, 5),
(@config_id, 'L1_FAMILY', '家庭减灾能力', 1, 0.15, NULL, 6);

SET @p0 = (SELECT id FROM indicator_weight WHERE config_id = @config_id AND indicator_code = 'L1_GOVERNMENT');
SET @p1 = (SELECT id FROM indicator_weight WHERE config_id = @config_id AND indicator_code = 'L1_ENTERPRISE');
SET @p2 = (SELECT id FROM indicator_weight WHERE config_id = @config_id AND indicator_code = 'L1_SOCIAL_ORGANIZATION');
SET @p3 = (SELECT id FROM indicator_weight WHERE config_id = @config_id AND indicator_code = 'L1_TOWNSHIP');
SET @p4 = (SELECT id FROM indicator_weight WHERE config_id = @config_id AND indicator_code = 'L1_COMMUNITY');
SET @p5 = (SELECT id FROM indicator_weight WHERE config_id = @config_id AND indicator_code = 'L1_FAMILY');

INSERT INTO indicator_weight (config_id, indicator_code, indicator_name, indicator_level, weight, parent_id, sort_order) VALUES
(@config_id, 'L2_GOV_MANAGEMENT', '管理能力', 2, 0.17, @p0, 1),
(@config_id, 'L2_GOV_ENGINEERING', '工程设防能力', 2, 0.16, @p0, 2),
(@config_id, 'L2_GOV_MONITORING', '监测预警能力', 2, 0.17, @p0, 3),
(@config_id, 'L2_GOV_MATERIAL', '物资储备能力', 2, 0.16, @p0, 4),
(@config_id, 'L2_GOV_RESCUE_TEAM', '专业队伍救援能力', 2, 0.17, @p0, 5),
(@config_id, 'L2_GOV_RELOCATION', '转移安置能力', 2, 0.17, @p0, 6),
(@config_id, 'L2_ENT_ENGINEERING_RESCUE', '大型企业应急救援能力', 2, 0.47, @p1, 7),
(@config_id, 'L2_ENT_INSURANCE', '保险和再保险企业减灾能力', 2, 0.53, @p1, 8),
(@config_id, 'L2_SOC_MATERIAL', '物资储备能力', 2, 0.24, @p2, 9),
(@config_id, 'L2_SOC_TRANSPORT', '应急运输能力', 2, 0.25, @p2, 10),
(@config_id, 'L2_SOC_RESCUE', '应急救援能力', 2, 0.25, @p2, 11),
(@config_id, 'L2_SOC_PUBLICITY', '科普宣传能力', 2, 0.26, @p2, 12),
(@config_id, 'L2_TWN_DISASTER_MANAGEMENT', '灾害管理能力', 2, 0.33, @p3, 13),
(@config_id, 'L2_TWN_DISASTER_PREPAREDNESS', '灾害备灾能力', 2, 0.32, @p3, 14),
(@config_id, 'L2_TWN_SELF_RESCUE_TRANSFER', '自救转移能力', 2, 0.35, @p3, 15),
(@config_id, 'L2_COM_DISASTER_MANAGEMENT', '灾害管理能力', 2, 0.33, @p4, 16),
(@config_id, 'L2_COM_DISASTER_PREPAREDNESS', '灾害备灾能力', 2, 0.31, @p4, 17),
(@config_id, 'L2_COM_SELF_RESCUE_TRANSFER', '自救转移能力', 2, 0.36, @p4, 18),
(@config_id, 'L2_FAM_VULNERABILITY', '家庭脆弱性', 2, 0.22, @p5, 19),
(@config_id, 'L2_FAM_MATERIAL', '防灾物资储备能力', 2, 0.24, @p5, 20),
(@config_id, 'L2_FAM_INFORMATION', '灾害信息获取能力', 2, 0.28, @p5, 21),
(@config_id, 'L2_FAM_SELF_RESCUE', '灾害自救互救能力', 2, 0.26, @p5, 22);

-- ─── 德阳市 (orgcode=5106) ───
DELETE wc FROM weight_config wc LEFT JOIN indicator_weight iw ON iw.config_id = wc.id WHERE wc.orgcode = '5106' AND wc.model_id = 20 AND wc.year = 2020 AND wc.is_deleted = 0;
DELETE FROM indicator_weight WHERE config_id IN (SELECT id FROM weight_config WHERE orgcode = '5106' AND model_id = 20 AND year = 2020);
INSERT INTO weight_config (config_name, description, orgcode, data_source, year, model_id, is_deleted) VALUES ('综合减灾能力评估权重', '2020年德阳市综合减灾能力评估权重', '5106', 'baseline', 2020, 20, 0);
SET @config_id = LAST_INSERT_ID();

INSERT INTO indicator_weight (config_id, indicator_code, indicator_name, indicator_level, weight, parent_id, sort_order) VALUES
(@config_id, 'L1_GOVERNMENT', '政府减灾能力', 1, 0.20, NULL, 1),
(@config_id, 'L1_ENTERPRISE', '企业减灾能力', 1, 0.17, NULL, 2),
(@config_id, 'L1_SOCIAL_ORGANIZATION', '社会组织减灾能力', 1, 0.15, NULL, 3),
(@config_id, 'L1_TOWNSHIP', '乡镇（街道）减灾能力', 1, 0.18, NULL, 4),
(@config_id, 'L1_COMMUNITY', '社区（行政区）减灾能力', 1, 0.16, NULL, 5),
(@config_id, 'L1_FAMILY', '家庭减灾能力', 1, 0.14, NULL, 6);

SET @p0 = (SELECT id FROM indicator_weight WHERE config_id = @config_id AND indicator_code = 'L1_GOVERNMENT');
SET @p1 = (SELECT id FROM indicator_weight WHERE config_id = @config_id AND indicator_code = 'L1_ENTERPRISE');
SET @p2 = (SELECT id FROM indicator_weight WHERE config_id = @config_id AND indicator_code = 'L1_SOCIAL_ORGANIZATION');
SET @p3 = (SELECT id FROM indicator_weight WHERE config_id = @config_id AND indicator_code = 'L1_TOWNSHIP');
SET @p4 = (SELECT id FROM indicator_weight WHERE config_id = @config_id AND indicator_code = 'L1_COMMUNITY');
SET @p5 = (SELECT id FROM indicator_weight WHERE config_id = @config_id AND indicator_code = 'L1_FAMILY');

INSERT INTO indicator_weight (config_id, indicator_code, indicator_name, indicator_level, weight, parent_id, sort_order) VALUES
(@config_id, 'L2_GOV_MANAGEMENT', '管理能力', 2, 0.17, @p0, 1),
(@config_id, 'L2_GOV_ENGINEERING', '工程设防能力', 2, 0.16, @p0, 2),
(@config_id, 'L2_GOV_MONITORING', '监测预警能力', 2, 0.17, @p0, 3),
(@config_id, 'L2_GOV_MATERIAL', '物资储备能力', 2, 0.17, @p0, 4),
(@config_id, 'L2_GOV_RESCUE_TEAM', '专业队伍救援能力', 2, 0.16, @p0, 5),
(@config_id, 'L2_GOV_RELOCATION', '转移安置能力', 2, 0.17, @p0, 6),
(@config_id, 'L2_ENT_ENGINEERING_RESCUE', '大型企业应急救援能力', 2, 0.52, @p1, 7),
(@config_id, 'L2_ENT_INSURANCE', '保险和再保险企业减灾能力', 2, 0.48, @p1, 8),
(@config_id, 'L2_SOC_MATERIAL', '物资储备能力', 2, 0.24, @p2, 9),
(@config_id, 'L2_SOC_TRANSPORT', '应急运输能力', 2, 0.25, @p2, 10),
(@config_id, 'L2_SOC_RESCUE', '应急救援能力', 2, 0.26, @p2, 11),
(@config_id, 'L2_SOC_PUBLICITY', '科普宣传能力', 2, 0.25, @p2, 12),
(@config_id, 'L2_TWN_DISASTER_MANAGEMENT', '灾害管理能力', 2, 0.34, @p3, 13),
(@config_id, 'L2_TWN_DISASTER_PREPAREDNESS', '灾害备灾能力', 2, 0.32, @p3, 14),
(@config_id, 'L2_TWN_SELF_RESCUE_TRANSFER', '自救转移能力', 2, 0.34, @p3, 15),
(@config_id, 'L2_COM_DISASTER_MANAGEMENT', '灾害管理能力', 2, 0.33, @p4, 16),
(@config_id, 'L2_COM_DISASTER_PREPAREDNESS', '灾害备灾能力', 2, 0.32, @p4, 17),
(@config_id, 'L2_COM_SELF_RESCUE_TRANSFER', '自救转移能力', 2, 0.35, @p4, 18),
(@config_id, 'L2_FAM_VULNERABILITY', '家庭脆弱性', 2, 0.23, @p5, 19),
(@config_id, 'L2_FAM_MATERIAL', '防灾物资储备能力', 2, 0.23, @p5, 20),
(@config_id, 'L2_FAM_INFORMATION', '灾害信息获取能力', 2, 0.28, @p5, 21),
(@config_id, 'L2_FAM_SELF_RESCUE', '灾害自救互救能力', 2, 0.26, @p5, 22);

-- ─── 甘孜州 (orgcode=5133) ───
DELETE wc FROM weight_config wc LEFT JOIN indicator_weight iw ON iw.config_id = wc.id WHERE wc.orgcode = '5133' AND wc.model_id = 20 AND wc.year = 2020 AND wc.is_deleted = 0;
DELETE FROM indicator_weight WHERE config_id IN (SELECT id FROM weight_config WHERE orgcode = '5133' AND model_id = 20 AND year = 2020);
INSERT INTO weight_config (config_name, description, orgcode, data_source, year, model_id, is_deleted) VALUES ('综合减灾能力评估权重', '2020年甘孜州综合减灾能力评估权重', '5133', 'baseline', 2020, 20, 0);
SET @config_id = LAST_INSERT_ID();

INSERT INTO indicator_weight (config_id, indicator_code, indicator_name, indicator_level, weight, parent_id, sort_order) VALUES
(@config_id, 'L1_GOVERNMENT', '政府减灾能力', 1, 0.21, NULL, 1),
(@config_id, 'L1_ENTERPRISE', '企业减灾能力', 1, 0.18, NULL, 2),
(@config_id, 'L1_SOCIAL_ORGANIZATION', '社会组织减灾能力', 1, 0.16, NULL, 3),
(@config_id, 'L1_TOWNSHIP', '乡镇（街道）减灾能力', 1, 0.16, NULL, 4),
(@config_id, 'L1_COMMUNITY', '社区（行政区）减灾能力', 1, 0.15, NULL, 5),
(@config_id, 'L1_FAMILY', '家庭减灾能力', 1, 0.14, NULL, 6);

SET @p0 = (SELECT id FROM indicator_weight WHERE config_id = @config_id AND indicator_code = 'L1_GOVERNMENT');
SET @p1 = (SELECT id FROM indicator_weight WHERE config_id = @config_id AND indicator_code = 'L1_ENTERPRISE');
SET @p2 = (SELECT id FROM indicator_weight WHERE config_id = @config_id AND indicator_code = 'L1_SOCIAL_ORGANIZATION');
SET @p3 = (SELECT id FROM indicator_weight WHERE config_id = @config_id AND indicator_code = 'L1_TOWNSHIP');
SET @p4 = (SELECT id FROM indicator_weight WHERE config_id = @config_id AND indicator_code = 'L1_COMMUNITY');
SET @p5 = (SELECT id FROM indicator_weight WHERE config_id = @config_id AND indicator_code = 'L1_FAMILY');

INSERT INTO indicator_weight (config_id, indicator_code, indicator_name, indicator_level, weight, parent_id, sort_order) VALUES
(@config_id, 'L2_GOV_MANAGEMENT', '管理能力', 2, 0.17, @p0, 1),
(@config_id, 'L2_GOV_ENGINEERING', '工程设防能力', 2, 0.16, @p0, 2),
(@config_id, 'L2_GOV_MONITORING', '监测预警能力', 2, 0.17, @p0, 3),
(@config_id, 'L2_GOV_MATERIAL', '物资储备能力', 2, 0.16, @p0, 4),
(@config_id, 'L2_GOV_RESCUE_TEAM', '专业队伍救援能力', 2, 0.17, @p0, 5),
(@config_id, 'L2_GOV_RELOCATION', '转移安置能力', 2, 0.17, @p0, 6),
(@config_id, 'L2_ENT_ENGINEERING_RESCUE', '大型企业应急救援能力', 2, 0.54, @p1, 7),
(@config_id, 'L2_ENT_INSURANCE', '保险和再保险企业减灾能力', 2, 0.46, @p1, 8),
(@config_id, 'L2_SOC_MATERIAL', '物资储备能力', 2, 0.25, @p2, 9),
(@config_id, 'L2_SOC_TRANSPORT', '应急运输能力', 2, 0.24, @p2, 10),
(@config_id, 'L2_SOC_RESCUE', '应急救援能力', 2, 0.26, @p2, 11),
(@config_id, 'L2_SOC_PUBLICITY', '科普宣传能力', 2, 0.25, @p2, 12),
(@config_id, 'L2_TWN_DISASTER_MANAGEMENT', '灾害管理能力', 2, 0.31, @p3, 13),
(@config_id, 'L2_TWN_DISASTER_PREPAREDNESS', '灾害备灾能力', 2, 0.35, @p3, 14),
(@config_id, 'L2_TWN_SELF_RESCUE_TRANSFER', '自救转移能力', 2, 0.34, @p3, 15),
(@config_id, 'L2_COM_DISASTER_MANAGEMENT', '灾害管理能力', 2, 0.33, @p4, 16),
(@config_id, 'L2_COM_DISASTER_PREPAREDNESS', '灾害备灾能力', 2, 0.31, @p4, 17),
(@config_id, 'L2_COM_SELF_RESCUE_TRANSFER', '自救转移能力', 2, 0.36, @p4, 18),
(@config_id, 'L2_FAM_VULNERABILITY', '家庭脆弱性', 2, 0.25, @p5, 19),
(@config_id, 'L2_FAM_MATERIAL', '防灾物资储备能力', 2, 0.22, @p5, 20),
(@config_id, 'L2_FAM_INFORMATION', '灾害信息获取能力', 2, 0.26, @p5, 21),
(@config_id, 'L2_FAM_SELF_RESCUE', '灾害自救互救能力', 2, 0.27, @p5, 22);

-- ─── 广安市 (orgcode=5116) ───
DELETE wc FROM weight_config wc LEFT JOIN indicator_weight iw ON iw.config_id = wc.id WHERE wc.orgcode = '5116' AND wc.model_id = 20 AND wc.year = 2020 AND wc.is_deleted = 0;
DELETE FROM indicator_weight WHERE config_id IN (SELECT id FROM weight_config WHERE orgcode = '5116' AND model_id = 20 AND year = 2020);
INSERT INTO weight_config (config_name, description, orgcode, data_source, year, model_id, is_deleted) VALUES ('综合减灾能力评估权重', '2020年广安市综合减灾能力评估权重', '5116', 'baseline', 2020, 20, 0);
SET @config_id = LAST_INSERT_ID();

INSERT INTO indicator_weight (config_id, indicator_code, indicator_name, indicator_level, weight, parent_id, sort_order) VALUES
(@config_id, 'L1_GOVERNMENT', '政府减灾能力', 1, 0.20, NULL, 1),
(@config_id, 'L1_ENTERPRISE', '企业减灾能力', 1, 0.17, NULL, 2),
(@config_id, 'L1_SOCIAL_ORGANIZATION', '社会组织减灾能力', 1, 0.16, NULL, 3),
(@config_id, 'L1_TOWNSHIP', '乡镇（街道）减灾能力', 1, 0.18, NULL, 4),
(@config_id, 'L1_COMMUNITY', '社区（行政区）减灾能力', 1, 0.15, NULL, 5),
(@config_id, 'L1_FAMILY', '家庭减灾能力', 1, 0.14, NULL, 6);

SET @p0 = (SELECT id FROM indicator_weight WHERE config_id = @config_id AND indicator_code = 'L1_GOVERNMENT');
SET @p1 = (SELECT id FROM indicator_weight WHERE config_id = @config_id AND indicator_code = 'L1_ENTERPRISE');
SET @p2 = (SELECT id FROM indicator_weight WHERE config_id = @config_id AND indicator_code = 'L1_SOCIAL_ORGANIZATION');
SET @p3 = (SELECT id FROM indicator_weight WHERE config_id = @config_id AND indicator_code = 'L1_TOWNSHIP');
SET @p4 = (SELECT id FROM indicator_weight WHERE config_id = @config_id AND indicator_code = 'L1_COMMUNITY');
SET @p5 = (SELECT id FROM indicator_weight WHERE config_id = @config_id AND indicator_code = 'L1_FAMILY');

INSERT INTO indicator_weight (config_id, indicator_code, indicator_name, indicator_level, weight, parent_id, sort_order) VALUES
(@config_id, 'L2_GOV_MANAGEMENT', '管理能力', 2, 0.17, @p0, 1),
(@config_id, 'L2_GOV_ENGINEERING', '工程设防能力', 2, 0.16, @p0, 2),
(@config_id, 'L2_GOV_MONITORING', '监测预警能力', 2, 0.17, @p0, 3),
(@config_id, 'L2_GOV_MATERIAL', '物资储备能力', 2, 0.16, @p0, 4),
(@config_id, 'L2_GOV_RESCUE_TEAM', '专业队伍救援能力', 2, 0.17, @p0, 5),
(@config_id, 'L2_GOV_RELOCATION', '转移安置能力', 2, 0.17, @p0, 6),
(@config_id, 'L2_ENT_ENGINEERING_RESCUE', '大型企业应急救援能力', 2, 0.53, @p1, 7),
(@config_id, 'L2_ENT_INSURANCE', '保险和再保险企业减灾能力', 2, 0.47, @p1, 8),
(@config_id, 'L2_SOC_MATERIAL', '物资储备能力', 2, 0.24, @p2, 9),
(@config_id, 'L2_SOC_TRANSPORT', '应急运输能力', 2, 0.27, @p2, 10),
(@config_id, 'L2_SOC_RESCUE', '应急救援能力', 2, 0.25, @p2, 11),
(@config_id, 'L2_SOC_PUBLICITY', '科普宣传能力', 2, 0.24, @p2, 12),
(@config_id, 'L2_TWN_DISASTER_MANAGEMENT', '灾害管理能力', 2, 0.34, @p3, 13),
(@config_id, 'L2_TWN_DISASTER_PREPAREDNESS', '灾害备灾能力', 2, 0.32, @p3, 14),
(@config_id, 'L2_TWN_SELF_RESCUE_TRANSFER', '自救转移能力', 2, 0.34, @p3, 15),
(@config_id, 'L2_COM_DISASTER_MANAGEMENT', '灾害管理能力', 2, 0.35, @p4, 16),
(@config_id, 'L2_COM_DISASTER_PREPAREDNESS', '灾害备灾能力', 2, 0.31, @p4, 17),
(@config_id, 'L2_COM_SELF_RESCUE_TRANSFER', '自救转移能力', 2, 0.34, @p4, 18),
(@config_id, 'L2_FAM_VULNERABILITY', '家庭脆弱性', 2, 0.24, @p5, 19),
(@config_id, 'L2_FAM_MATERIAL', '防灾物资储备能力', 2, 0.22, @p5, 20),
(@config_id, 'L2_FAM_INFORMATION', '灾害信息获取能力', 2, 0.28, @p5, 21),
(@config_id, 'L2_FAM_SELF_RESCUE', '灾害自救互救能力', 2, 0.26, @p5, 22);

-- ─── 广元市 (orgcode=5108) ───
DELETE wc FROM weight_config wc LEFT JOIN indicator_weight iw ON iw.config_id = wc.id WHERE wc.orgcode = '5108' AND wc.model_id = 20 AND wc.year = 2020 AND wc.is_deleted = 0;
DELETE FROM indicator_weight WHERE config_id IN (SELECT id FROM weight_config WHERE orgcode = '5108' AND model_id = 20 AND year = 2020);
INSERT INTO weight_config (config_name, description, orgcode, data_source, year, model_id, is_deleted) VALUES ('综合减灾能力评估权重', '2020年广元市综合减灾能力评估权重', '5108', 'baseline', 2020, 20, 0);
SET @config_id = LAST_INSERT_ID();

INSERT INTO indicator_weight (config_id, indicator_code, indicator_name, indicator_level, weight, parent_id, sort_order) VALUES
(@config_id, 'L1_GOVERNMENT', '政府减灾能力', 1, 0.19, NULL, 1),
(@config_id, 'L1_ENTERPRISE', '企业减灾能力', 1, 0.15, NULL, 2),
(@config_id, 'L1_SOCIAL_ORGANIZATION', '社会组织减灾能力', 1, 0.16, NULL, 3),
(@config_id, 'L1_TOWNSHIP', '乡镇（街道）减灾能力', 1, 0.17, NULL, 4),
(@config_id, 'L1_COMMUNITY', '社区（行政区）减灾能力', 1, 0.17, NULL, 5),
(@config_id, 'L1_FAMILY', '家庭减灾能力', 1, 0.16, NULL, 6);

SET @p0 = (SELECT id FROM indicator_weight WHERE config_id = @config_id AND indicator_code = 'L1_GOVERNMENT');
SET @p1 = (SELECT id FROM indicator_weight WHERE config_id = @config_id AND indicator_code = 'L1_ENTERPRISE');
SET @p2 = (SELECT id FROM indicator_weight WHERE config_id = @config_id AND indicator_code = 'L1_SOCIAL_ORGANIZATION');
SET @p3 = (SELECT id FROM indicator_weight WHERE config_id = @config_id AND indicator_code = 'L1_TOWNSHIP');
SET @p4 = (SELECT id FROM indicator_weight WHERE config_id = @config_id AND indicator_code = 'L1_COMMUNITY');
SET @p5 = (SELECT id FROM indicator_weight WHERE config_id = @config_id AND indicator_code = 'L1_FAMILY');

INSERT INTO indicator_weight (config_id, indicator_code, indicator_name, indicator_level, weight, parent_id, sort_order) VALUES
(@config_id, 'L2_GOV_MANAGEMENT', '管理能力', 2, 0.17, @p0, 1),
(@config_id, 'L2_GOV_ENGINEERING', '工程设防能力', 2, 0.17, @p0, 2),
(@config_id, 'L2_GOV_MONITORING', '监测预警能力', 2, 0.17, @p0, 3),
(@config_id, 'L2_GOV_MATERIAL', '物资储备能力', 2, 0.16, @p0, 4),
(@config_id, 'L2_GOV_RESCUE_TEAM', '专业队伍救援能力', 2, 0.17, @p0, 5),
(@config_id, 'L2_GOV_RELOCATION', '转移安置能力', 2, 0.16, @p0, 6),
(@config_id, 'L2_ENT_ENGINEERING_RESCUE', '大型企业应急救援能力', 2, 0.52, @p1, 7),
(@config_id, 'L2_ENT_INSURANCE', '保险和再保险企业减灾能力', 2, 0.48, @p1, 8),
(@config_id, 'L2_SOC_MATERIAL', '物资储备能力', 2, 0.25, @p2, 9),
(@config_id, 'L2_SOC_TRANSPORT', '应急运输能力', 2, 0.26, @p2, 10),
(@config_id, 'L2_SOC_RESCUE', '应急救援能力', 2, 0.25, @p2, 11),
(@config_id, 'L2_SOC_PUBLICITY', '科普宣传能力', 2, 0.24, @p2, 12),
(@config_id, 'L2_TWN_DISASTER_MANAGEMENT', '灾害管理能力', 2, 0.35, @p3, 13),
(@config_id, 'L2_TWN_DISASTER_PREPAREDNESS', '灾害备灾能力', 2, 0.32, @p3, 14),
(@config_id, 'L2_TWN_SELF_RESCUE_TRANSFER', '自救转移能力', 2, 0.33, @p3, 15),
(@config_id, 'L2_COM_DISASTER_MANAGEMENT', '灾害管理能力', 2, 0.34, @p4, 16),
(@config_id, 'L2_COM_DISASTER_PREPAREDNESS', '灾害备灾能力', 2, 0.32, @p4, 17),
(@config_id, 'L2_COM_SELF_RESCUE_TRANSFER', '自救转移能力', 2, 0.34, @p4, 18),
(@config_id, 'L2_FAM_VULNERABILITY', '家庭脆弱性', 2, 0.23, @p5, 19),
(@config_id, 'L2_FAM_MATERIAL', '防灾物资储备能力', 2, 0.24, @p5, 20),
(@config_id, 'L2_FAM_INFORMATION', '灾害信息获取能力', 2, 0.27, @p5, 21),
(@config_id, 'L2_FAM_SELF_RESCUE', '灾害自救互救能力', 2, 0.26, @p5, 22);

-- ─── 乐山市 (orgcode=5111) ───
DELETE wc FROM weight_config wc LEFT JOIN indicator_weight iw ON iw.config_id = wc.id WHERE wc.orgcode = '5111' AND wc.model_id = 20 AND wc.year = 2020 AND wc.is_deleted = 0;
DELETE FROM indicator_weight WHERE config_id IN (SELECT id FROM weight_config WHERE orgcode = '5111' AND model_id = 20 AND year = 2020);
INSERT INTO weight_config (config_name, description, orgcode, data_source, year, model_id, is_deleted) VALUES ('综合减灾能力评估权重', '2020年乐山市综合减灾能力评估权重', '5111', 'baseline', 2020, 20, 0);
SET @config_id = LAST_INSERT_ID();

INSERT INTO indicator_weight (config_id, indicator_code, indicator_name, indicator_level, weight, parent_id, sort_order) VALUES
(@config_id, 'L1_GOVERNMENT', '政府减灾能力', 1, 0.20, NULL, 1),
(@config_id, 'L1_ENTERPRISE', '企业减灾能力', 1, 0.17, NULL, 2),
(@config_id, 'L1_SOCIAL_ORGANIZATION', '社会组织减灾能力', 1, 0.16, NULL, 3),
(@config_id, 'L1_TOWNSHIP', '乡镇（街道）减灾能力', 1, 0.17, NULL, 4),
(@config_id, 'L1_COMMUNITY', '社区（行政区）减灾能力', 1, 0.16, NULL, 5),
(@config_id, 'L1_FAMILY', '家庭减灾能力', 1, 0.14, NULL, 6);

SET @p0 = (SELECT id FROM indicator_weight WHERE config_id = @config_id AND indicator_code = 'L1_GOVERNMENT');
SET @p1 = (SELECT id FROM indicator_weight WHERE config_id = @config_id AND indicator_code = 'L1_ENTERPRISE');
SET @p2 = (SELECT id FROM indicator_weight WHERE config_id = @config_id AND indicator_code = 'L1_SOCIAL_ORGANIZATION');
SET @p3 = (SELECT id FROM indicator_weight WHERE config_id = @config_id AND indicator_code = 'L1_TOWNSHIP');
SET @p4 = (SELECT id FROM indicator_weight WHERE config_id = @config_id AND indicator_code = 'L1_COMMUNITY');
SET @p5 = (SELECT id FROM indicator_weight WHERE config_id = @config_id AND indicator_code = 'L1_FAMILY');

INSERT INTO indicator_weight (config_id, indicator_code, indicator_name, indicator_level, weight, parent_id, sort_order) VALUES
(@config_id, 'L2_GOV_MANAGEMENT', '管理能力', 2, 0.17, @p0, 1),
(@config_id, 'L2_GOV_ENGINEERING', '工程设防能力', 2, 0.16, @p0, 2),
(@config_id, 'L2_GOV_MONITORING', '监测预警能力', 2, 0.17, @p0, 3),
(@config_id, 'L2_GOV_MATERIAL', '物资储备能力', 2, 0.16, @p0, 4),
(@config_id, 'L2_GOV_RESCUE_TEAM', '专业队伍救援能力', 2, 0.17, @p0, 5),
(@config_id, 'L2_GOV_RELOCATION', '转移安置能力', 2, 0.17, @p0, 6),
(@config_id, 'L2_ENT_ENGINEERING_RESCUE', '大型企业应急救援能力', 2, 0.52, @p1, 7),
(@config_id, 'L2_ENT_INSURANCE', '保险和再保险企业减灾能力', 2, 0.48, @p1, 8),
(@config_id, 'L2_SOC_MATERIAL', '物资储备能力', 2, 0.24, @p2, 9),
(@config_id, 'L2_SOC_TRANSPORT', '应急运输能力', 2, 0.24, @p2, 10),
(@config_id, 'L2_SOC_RESCUE', '应急救援能力', 2, 0.25, @p2, 11),
(@config_id, 'L2_SOC_PUBLICITY', '科普宣传能力', 2, 0.27, @p2, 12),
(@config_id, 'L2_TWN_DISASTER_MANAGEMENT', '灾害管理能力', 2, 0.34, @p3, 13),
(@config_id, 'L2_TWN_DISASTER_PREPAREDNESS', '灾害备灾能力', 2, 0.33, @p3, 14),
(@config_id, 'L2_TWN_SELF_RESCUE_TRANSFER', '自救转移能力', 2, 0.33, @p3, 15),
(@config_id, 'L2_COM_DISASTER_MANAGEMENT', '灾害管理能力', 2, 0.34, @p4, 16),
(@config_id, 'L2_COM_DISASTER_PREPAREDNESS', '灾害备灾能力', 2, 0.33, @p4, 17),
(@config_id, 'L2_COM_SELF_RESCUE_TRANSFER', '自救转移能力', 2, 0.33, @p4, 18),
(@config_id, 'L2_FAM_VULNERABILITY', '家庭脆弱性', 2, 0.20, @p5, 19),
(@config_id, 'L2_FAM_MATERIAL', '防灾物资储备能力', 2, 0.23, @p5, 20),
(@config_id, 'L2_FAM_INFORMATION', '灾害信息获取能力', 2, 0.29, @p5, 21),
(@config_id, 'L2_FAM_SELF_RESCUE', '灾害自救互救能力', 2, 0.28, @p5, 22);

-- ─── 凉山州 (orgcode=5134) ───
DELETE wc FROM weight_config wc LEFT JOIN indicator_weight iw ON iw.config_id = wc.id WHERE wc.orgcode = '5134' AND wc.model_id = 20 AND wc.year = 2020 AND wc.is_deleted = 0;
DELETE FROM indicator_weight WHERE config_id IN (SELECT id FROM weight_config WHERE orgcode = '5134' AND model_id = 20 AND year = 2020);
INSERT INTO weight_config (config_name, description, orgcode, data_source, year, model_id, is_deleted) VALUES ('综合减灾能力评估权重', '2020年凉山州综合减灾能力评估权重', '5134', 'baseline', 2020, 20, 0);
SET @config_id = LAST_INSERT_ID();

INSERT INTO indicator_weight (config_id, indicator_code, indicator_name, indicator_level, weight, parent_id, sort_order) VALUES
(@config_id, 'L1_GOVERNMENT', '政府减灾能力', 1, 0.19, NULL, 1),
(@config_id, 'L1_ENTERPRISE', '企业减灾能力', 1, 0.17, NULL, 2),
(@config_id, 'L1_SOCIAL_ORGANIZATION', '社会组织减灾能力', 1, 0.15, NULL, 3),
(@config_id, 'L1_TOWNSHIP', '乡镇（街道）减灾能力', 1, 0.17, NULL, 4),
(@config_id, 'L1_COMMUNITY', '社区（行政区）减灾能力', 1, 0.17, NULL, 5),
(@config_id, 'L1_FAMILY', '家庭减灾能力', 1, 0.15, NULL, 6);

SET @p0 = (SELECT id FROM indicator_weight WHERE config_id = @config_id AND indicator_code = 'L1_GOVERNMENT');
SET @p1 = (SELECT id FROM indicator_weight WHERE config_id = @config_id AND indicator_code = 'L1_ENTERPRISE');
SET @p2 = (SELECT id FROM indicator_weight WHERE config_id = @config_id AND indicator_code = 'L1_SOCIAL_ORGANIZATION');
SET @p3 = (SELECT id FROM indicator_weight WHERE config_id = @config_id AND indicator_code = 'L1_TOWNSHIP');
SET @p4 = (SELECT id FROM indicator_weight WHERE config_id = @config_id AND indicator_code = 'L1_COMMUNITY');
SET @p5 = (SELECT id FROM indicator_weight WHERE config_id = @config_id AND indicator_code = 'L1_FAMILY');

INSERT INTO indicator_weight (config_id, indicator_code, indicator_name, indicator_level, weight, parent_id, sort_order) VALUES
(@config_id, 'L2_GOV_MANAGEMENT', '管理能力', 2, 0.17, @p0, 1),
(@config_id, 'L2_GOV_ENGINEERING', '工程设防能力', 2, 0.16, @p0, 2),
(@config_id, 'L2_GOV_MONITORING', '监测预警能力', 2, 0.17, @p0, 3),
(@config_id, 'L2_GOV_MATERIAL', '物资储备能力', 2, 0.16, @p0, 4),
(@config_id, 'L2_GOV_RESCUE_TEAM', '专业队伍救援能力', 2, 0.17, @p0, 5),
(@config_id, 'L2_GOV_RELOCATION', '转移安置能力', 2, 0.17, @p0, 6),
(@config_id, 'L2_ENT_ENGINEERING_RESCUE', '大型企业应急救援能力', 2, 0.49, @p1, 7),
(@config_id, 'L2_ENT_INSURANCE', '保险和再保险企业减灾能力', 2, 0.51, @p1, 8),
(@config_id, 'L2_SOC_MATERIAL', '物资储备能力', 2, 0.25, @p2, 9),
(@config_id, 'L2_SOC_TRANSPORT', '应急运输能力', 2, 0.25, @p2, 10),
(@config_id, 'L2_SOC_RESCUE', '应急救援能力', 2, 0.25, @p2, 11),
(@config_id, 'L2_SOC_PUBLICITY', '科普宣传能力', 2, 0.25, @p2, 12),
(@config_id, 'L2_TWN_DISASTER_MANAGEMENT', '灾害管理能力', 2, 0.34, @p3, 13),
(@config_id, 'L2_TWN_DISASTER_PREPAREDNESS', '灾害备灾能力', 2, 0.33, @p3, 14),
(@config_id, 'L2_TWN_SELF_RESCUE_TRANSFER', '自救转移能力', 2, 0.33, @p3, 15),
(@config_id, 'L2_COM_DISASTER_MANAGEMENT', '灾害管理能力', 2, 0.33, @p4, 16),
(@config_id, 'L2_COM_DISASTER_PREPAREDNESS', '灾害备灾能力', 2, 0.33, @p4, 17),
(@config_id, 'L2_COM_SELF_RESCUE_TRANSFER', '自救转移能力', 2, 0.34, @p4, 18),
(@config_id, 'L2_FAM_VULNERABILITY', '家庭脆弱性', 2, 0.23, @p5, 19),
(@config_id, 'L2_FAM_MATERIAL', '防灾物资储备能力', 2, 0.23, @p5, 20),
(@config_id, 'L2_FAM_INFORMATION', '灾害信息获取能力', 2, 0.28, @p5, 21),
(@config_id, 'L2_FAM_SELF_RESCUE', '灾害自救互救能力', 2, 0.26, @p5, 22);

-- ─── 泸州市 (orgcode=5105) ───
DELETE wc FROM weight_config wc LEFT JOIN indicator_weight iw ON iw.config_id = wc.id WHERE wc.orgcode = '5105' AND wc.model_id = 20 AND wc.year = 2020 AND wc.is_deleted = 0;
DELETE FROM indicator_weight WHERE config_id IN (SELECT id FROM weight_config WHERE orgcode = '5105' AND model_id = 20 AND year = 2020);
INSERT INTO weight_config (config_name, description, orgcode, data_source, year, model_id, is_deleted) VALUES ('综合减灾能力评估权重', '2020年泸州市综合减灾能力评估权重', '5105', 'baseline', 2020, 20, 0);
SET @config_id = LAST_INSERT_ID();

INSERT INTO indicator_weight (config_id, indicator_code, indicator_name, indicator_level, weight, parent_id, sort_order) VALUES
(@config_id, 'L1_GOVERNMENT', '政府减灾能力', 1, 0.19, NULL, 1),
(@config_id, 'L1_ENTERPRISE', '企业减灾能力', 1, 0.17, NULL, 2),
(@config_id, 'L1_SOCIAL_ORGANIZATION', '社会组织减灾能力', 1, 0.16, NULL, 3),
(@config_id, 'L1_TOWNSHIP', '乡镇（街道）减灾能力', 1, 0.17, NULL, 4),
(@config_id, 'L1_COMMUNITY', '社区（行政区）减灾能力', 1, 0.16, NULL, 5),
(@config_id, 'L1_FAMILY', '家庭减灾能力', 1, 0.15, NULL, 6);

SET @p0 = (SELECT id FROM indicator_weight WHERE config_id = @config_id AND indicator_code = 'L1_GOVERNMENT');
SET @p1 = (SELECT id FROM indicator_weight WHERE config_id = @config_id AND indicator_code = 'L1_ENTERPRISE');
SET @p2 = (SELECT id FROM indicator_weight WHERE config_id = @config_id AND indicator_code = 'L1_SOCIAL_ORGANIZATION');
SET @p3 = (SELECT id FROM indicator_weight WHERE config_id = @config_id AND indicator_code = 'L1_TOWNSHIP');
SET @p4 = (SELECT id FROM indicator_weight WHERE config_id = @config_id AND indicator_code = 'L1_COMMUNITY');
SET @p5 = (SELECT id FROM indicator_weight WHERE config_id = @config_id AND indicator_code = 'L1_FAMILY');

INSERT INTO indicator_weight (config_id, indicator_code, indicator_name, indicator_level, weight, parent_id, sort_order) VALUES
(@config_id, 'L2_GOV_MANAGEMENT', '管理能力', 2, 0.17, @p0, 1),
(@config_id, 'L2_GOV_ENGINEERING', '工程设防能力', 2, 0.16, @p0, 2),
(@config_id, 'L2_GOV_MONITORING', '监测预警能力', 2, 0.17, @p0, 3),
(@config_id, 'L2_GOV_MATERIAL', '物资储备能力', 2, 0.16, @p0, 4),
(@config_id, 'L2_GOV_RESCUE_TEAM', '专业队伍救援能力', 2, 0.17, @p0, 5),
(@config_id, 'L2_GOV_RELOCATION', '转移安置能力', 2, 0.17, @p0, 6),
(@config_id, 'L2_ENT_ENGINEERING_RESCUE', '大型企业应急救援能力', 2, 0.52, @p1, 7),
(@config_id, 'L2_ENT_INSURANCE', '保险和再保险企业减灾能力', 2, 0.48, @p1, 8),
(@config_id, 'L2_SOC_MATERIAL', '物资储备能力', 2, 0.24, @p2, 9),
(@config_id, 'L2_SOC_TRANSPORT', '应急运输能力', 2, 0.26, @p2, 10),
(@config_id, 'L2_SOC_RESCUE', '应急救援能力', 2, 0.26, @p2, 11),
(@config_id, 'L2_SOC_PUBLICITY', '科普宣传能力', 2, 0.24, @p2, 12),
(@config_id, 'L2_TWN_DISASTER_MANAGEMENT', '灾害管理能力', 2, 0.33, @p3, 13),
(@config_id, 'L2_TWN_DISASTER_PREPAREDNESS', '灾害备灾能力', 2, 0.33, @p3, 14),
(@config_id, 'L2_TWN_SELF_RESCUE_TRANSFER', '自救转移能力', 2, 0.34, @p3, 15),
(@config_id, 'L2_COM_DISASTER_MANAGEMENT', '灾害管理能力', 2, 0.34, @p4, 16),
(@config_id, 'L2_COM_DISASTER_PREPAREDNESS', '灾害备灾能力', 2, 0.32, @p4, 17),
(@config_id, 'L2_COM_SELF_RESCUE_TRANSFER', '自救转移能力', 2, 0.34, @p4, 18),
(@config_id, 'L2_FAM_VULNERABILITY', '家庭脆弱性', 2, 0.21, @p5, 19),
(@config_id, 'L2_FAM_MATERIAL', '防灾物资储备能力', 2, 0.22, @p5, 20),
(@config_id, 'L2_FAM_INFORMATION', '灾害信息获取能力', 2, 0.29, @p5, 21),
(@config_id, 'L2_FAM_SELF_RESCUE', '灾害自救互救能力', 2, 0.28, @p5, 22);

-- ─── 眉山市 (orgcode=5114) ───
DELETE wc FROM weight_config wc LEFT JOIN indicator_weight iw ON iw.config_id = wc.id WHERE wc.orgcode = '5114' AND wc.model_id = 20 AND wc.year = 2020 AND wc.is_deleted = 0;
DELETE FROM indicator_weight WHERE config_id IN (SELECT id FROM weight_config WHERE orgcode = '5114' AND model_id = 20 AND year = 2020);
INSERT INTO weight_config (config_name, description, orgcode, data_source, year, model_id, is_deleted) VALUES ('综合减灾能力评估权重', '2020年眉山市综合减灾能力评估权重', '5114', 'baseline', 2020, 20, 0);
SET @config_id = LAST_INSERT_ID();

INSERT INTO indicator_weight (config_id, indicator_code, indicator_name, indicator_level, weight, parent_id, sort_order) VALUES
(@config_id, 'L1_GOVERNMENT', '政府减灾能力', 1, 0.21, NULL, 1),
(@config_id, 'L1_ENTERPRISE', '企业减灾能力', 1, 0.15, NULL, 2),
(@config_id, 'L1_SOCIAL_ORGANIZATION', '社会组织减灾能力', 1, 0.16, NULL, 3),
(@config_id, 'L1_TOWNSHIP', '乡镇（街道）减灾能力', 1, 0.18, NULL, 4),
(@config_id, 'L1_COMMUNITY', '社区（行政区）减灾能力', 1, 0.16, NULL, 5),
(@config_id, 'L1_FAMILY', '家庭减灾能力', 1, 0.14, NULL, 6);

SET @p0 = (SELECT id FROM indicator_weight WHERE config_id = @config_id AND indicator_code = 'L1_GOVERNMENT');
SET @p1 = (SELECT id FROM indicator_weight WHERE config_id = @config_id AND indicator_code = 'L1_ENTERPRISE');
SET @p2 = (SELECT id FROM indicator_weight WHERE config_id = @config_id AND indicator_code = 'L1_SOCIAL_ORGANIZATION');
SET @p3 = (SELECT id FROM indicator_weight WHERE config_id = @config_id AND indicator_code = 'L1_TOWNSHIP');
SET @p4 = (SELECT id FROM indicator_weight WHERE config_id = @config_id AND indicator_code = 'L1_COMMUNITY');
SET @p5 = (SELECT id FROM indicator_weight WHERE config_id = @config_id AND indicator_code = 'L1_FAMILY');

INSERT INTO indicator_weight (config_id, indicator_code, indicator_name, indicator_level, weight, parent_id, sort_order) VALUES
(@config_id, 'L2_GOV_MANAGEMENT', '管理能力', 2, 0.18, @p0, 1),
(@config_id, 'L2_GOV_ENGINEERING', '工程设防能力', 2, 0.16, @p0, 2),
(@config_id, 'L2_GOV_MONITORING', '监测预警能力', 2, 0.17, @p0, 3),
(@config_id, 'L2_GOV_MATERIAL', '物资储备能力', 2, 0.16, @p0, 4),
(@config_id, 'L2_GOV_RESCUE_TEAM', '专业队伍救援能力', 2, 0.16, @p0, 5),
(@config_id, 'L2_GOV_RELOCATION', '转移安置能力', 2, 0.17, @p0, 6),
(@config_id, 'L2_ENT_ENGINEERING_RESCUE', '大型企业应急救援能力', 2, 0.51, @p1, 7),
(@config_id, 'L2_ENT_INSURANCE', '保险和再保险企业减灾能力', 2, 0.49, @p1, 8),
(@config_id, 'L2_SOC_MATERIAL', '物资储备能力', 2, 0.25, @p2, 9),
(@config_id, 'L2_SOC_TRANSPORT', '应急运输能力', 2, 0.26, @p2, 10),
(@config_id, 'L2_SOC_RESCUE', '应急救援能力', 2, 0.25, @p2, 11),
(@config_id, 'L2_SOC_PUBLICITY', '科普宣传能力', 2, 0.24, @p2, 12),
(@config_id, 'L2_TWN_DISASTER_MANAGEMENT', '灾害管理能力', 2, 0.33, @p3, 13),
(@config_id, 'L2_TWN_DISASTER_PREPAREDNESS', '灾害备灾能力', 2, 0.32, @p3, 14),
(@config_id, 'L2_TWN_SELF_RESCUE_TRANSFER', '自救转移能力', 2, 0.35, @p3, 15),
(@config_id, 'L2_COM_DISASTER_MANAGEMENT', '灾害管理能力', 2, 0.32, @p4, 16),
(@config_id, 'L2_COM_DISASTER_PREPAREDNESS', '灾害备灾能力', 2, 0.31, @p4, 17),
(@config_id, 'L2_COM_SELF_RESCUE_TRANSFER', '自救转移能力', 2, 0.37, @p4, 18),
(@config_id, 'L2_FAM_VULNERABILITY', '家庭脆弱性', 2, 0.21, @p5, 19),
(@config_id, 'L2_FAM_MATERIAL', '防灾物资储备能力', 2, 0.20, @p5, 20),
(@config_id, 'L2_FAM_INFORMATION', '灾害信息获取能力', 2, 0.32, @p5, 21),
(@config_id, 'L2_FAM_SELF_RESCUE', '灾害自救互救能力', 2, 0.27, @p5, 22);

-- ─── 绵阳市 (orgcode=5107) ───
DELETE wc FROM weight_config wc LEFT JOIN indicator_weight iw ON iw.config_id = wc.id WHERE wc.orgcode = '5107' AND wc.model_id = 20 AND wc.year = 2020 AND wc.is_deleted = 0;
DELETE FROM indicator_weight WHERE config_id IN (SELECT id FROM weight_config WHERE orgcode = '5107' AND model_id = 20 AND year = 2020);
INSERT INTO weight_config (config_name, description, orgcode, data_source, year, model_id, is_deleted) VALUES ('综合减灾能力评估权重', '2020年绵阳市综合减灾能力评估权重', '5107', 'baseline', 2020, 20, 0);
SET @config_id = LAST_INSERT_ID();

INSERT INTO indicator_weight (config_id, indicator_code, indicator_name, indicator_level, weight, parent_id, sort_order) VALUES
(@config_id, 'L1_GOVERNMENT', '政府减灾能力', 1, 0.20, NULL, 1),
(@config_id, 'L1_ENTERPRISE', '企业减灾能力', 1, 0.17, NULL, 2),
(@config_id, 'L1_SOCIAL_ORGANIZATION', '社会组织减灾能力', 1, 0.16, NULL, 3),
(@config_id, 'L1_TOWNSHIP', '乡镇（街道）减灾能力', 1, 0.17, NULL, 4),
(@config_id, 'L1_COMMUNITY', '社区（行政区）减灾能力', 1, 0.16, NULL, 5),
(@config_id, 'L1_FAMILY', '家庭减灾能力', 1, 0.14, NULL, 6);

SET @p0 = (SELECT id FROM indicator_weight WHERE config_id = @config_id AND indicator_code = 'L1_GOVERNMENT');
SET @p1 = (SELECT id FROM indicator_weight WHERE config_id = @config_id AND indicator_code = 'L1_ENTERPRISE');
SET @p2 = (SELECT id FROM indicator_weight WHERE config_id = @config_id AND indicator_code = 'L1_SOCIAL_ORGANIZATION');
SET @p3 = (SELECT id FROM indicator_weight WHERE config_id = @config_id AND indicator_code = 'L1_TOWNSHIP');
SET @p4 = (SELECT id FROM indicator_weight WHERE config_id = @config_id AND indicator_code = 'L1_COMMUNITY');
SET @p5 = (SELECT id FROM indicator_weight WHERE config_id = @config_id AND indicator_code = 'L1_FAMILY');

INSERT INTO indicator_weight (config_id, indicator_code, indicator_name, indicator_level, weight, parent_id, sort_order) VALUES
(@config_id, 'L2_GOV_MANAGEMENT', '管理能力', 2, 0.17, @p0, 1),
(@config_id, 'L2_GOV_ENGINEERING', '工程设防能力', 2, 0.16, @p0, 2),
(@config_id, 'L2_GOV_MONITORING', '监测预警能力', 2, 0.18, @p0, 3),
(@config_id, 'L2_GOV_MATERIAL', '物资储备能力', 2, 0.16, @p0, 4),
(@config_id, 'L2_GOV_RESCUE_TEAM', '专业队伍救援能力', 2, 0.16, @p0, 5),
(@config_id, 'L2_GOV_RELOCATION', '转移安置能力', 2, 0.17, @p0, 6),
(@config_id, 'L2_ENT_ENGINEERING_RESCUE', '大型企业应急救援能力', 2, 0.50, @p1, 7),
(@config_id, 'L2_ENT_INSURANCE', '保险和再保险企业减灾能力', 2, 0.50, @p1, 8),
(@config_id, 'L2_SOC_MATERIAL', '物资储备能力', 2, 0.25, @p2, 9),
(@config_id, 'L2_SOC_TRANSPORT', '应急运输能力', 2, 0.25, @p2, 10),
(@config_id, 'L2_SOC_RESCUE', '应急救援能力', 2, 0.25, @p2, 11),
(@config_id, 'L2_SOC_PUBLICITY', '科普宣传能力', 2, 0.25, @p2, 12),
(@config_id, 'L2_TWN_DISASTER_MANAGEMENT', '灾害管理能力', 2, 0.34, @p3, 13),
(@config_id, 'L2_TWN_DISASTER_PREPAREDNESS', '灾害备灾能力', 2, 0.32, @p3, 14),
(@config_id, 'L2_TWN_SELF_RESCUE_TRANSFER', '自救转移能力', 2, 0.34, @p3, 15),
(@config_id, 'L2_COM_DISASTER_MANAGEMENT', '灾害管理能力', 2, 0.34, @p4, 16),
(@config_id, 'L2_COM_DISASTER_PREPAREDNESS', '灾害备灾能力', 2, 0.32, @p4, 17),
(@config_id, 'L2_COM_SELF_RESCUE_TRANSFER', '自救转移能力', 2, 0.34, @p4, 18),
(@config_id, 'L2_FAM_VULNERABILITY', '家庭脆弱性', 2, 0.23, @p5, 19),
(@config_id, 'L2_FAM_MATERIAL', '防灾物资储备能力', 2, 0.23, @p5, 20),
(@config_id, 'L2_FAM_INFORMATION', '灾害信息获取能力', 2, 0.27, @p5, 21),
(@config_id, 'L2_FAM_SELF_RESCUE', '灾害自救互救能力', 2, 0.27, @p5, 22);

-- ─── 南充市 (orgcode=5113) ───
DELETE wc FROM weight_config wc LEFT JOIN indicator_weight iw ON iw.config_id = wc.id WHERE wc.orgcode = '5113' AND wc.model_id = 20 AND wc.year = 2020 AND wc.is_deleted = 0;
DELETE FROM indicator_weight WHERE config_id IN (SELECT id FROM weight_config WHERE orgcode = '5113' AND model_id = 20 AND year = 2020);
INSERT INTO weight_config (config_name, description, orgcode, data_source, year, model_id, is_deleted) VALUES ('综合减灾能力评估权重', '2020年南充市综合减灾能力评估权重', '5113', 'baseline', 2020, 20, 0);
SET @config_id = LAST_INSERT_ID();

INSERT INTO indicator_weight (config_id, indicator_code, indicator_name, indicator_level, weight, parent_id, sort_order) VALUES
(@config_id, 'L1_GOVERNMENT', '政府减灾能力', 1, 0.20, NULL, 1),
(@config_id, 'L1_ENTERPRISE', '企业减灾能力', 1, 0.17, NULL, 2),
(@config_id, 'L1_SOCIAL_ORGANIZATION', '社会组织减灾能力', 1, 0.16, NULL, 3),
(@config_id, 'L1_TOWNSHIP', '乡镇（街道）减灾能力', 1, 0.17, NULL, 4),
(@config_id, 'L1_COMMUNITY', '社区（行政区）减灾能力', 1, 0.16, NULL, 5),
(@config_id, 'L1_FAMILY', '家庭减灾能力', 1, 0.14, NULL, 6);

SET @p0 = (SELECT id FROM indicator_weight WHERE config_id = @config_id AND indicator_code = 'L1_GOVERNMENT');
SET @p1 = (SELECT id FROM indicator_weight WHERE config_id = @config_id AND indicator_code = 'L1_ENTERPRISE');
SET @p2 = (SELECT id FROM indicator_weight WHERE config_id = @config_id AND indicator_code = 'L1_SOCIAL_ORGANIZATION');
SET @p3 = (SELECT id FROM indicator_weight WHERE config_id = @config_id AND indicator_code = 'L1_TOWNSHIP');
SET @p4 = (SELECT id FROM indicator_weight WHERE config_id = @config_id AND indicator_code = 'L1_COMMUNITY');
SET @p5 = (SELECT id FROM indicator_weight WHERE config_id = @config_id AND indicator_code = 'L1_FAMILY');

INSERT INTO indicator_weight (config_id, indicator_code, indicator_name, indicator_level, weight, parent_id, sort_order) VALUES
(@config_id, 'L2_GOV_MANAGEMENT', '管理能力', 2, 0.18, @p0, 1),
(@config_id, 'L2_GOV_ENGINEERING', '工程设防能力', 2, 0.17, @p0, 2),
(@config_id, 'L2_GOV_MONITORING', '监测预警能力', 2, 0.17, @p0, 3),
(@config_id, 'L2_GOV_MATERIAL', '物资储备能力', 2, 0.16, @p0, 4),
(@config_id, 'L2_GOV_RESCUE_TEAM', '专业队伍救援能力', 2, 0.17, @p0, 5),
(@config_id, 'L2_GOV_RELOCATION', '转移安置能力', 2, 0.15, @p0, 6),
(@config_id, 'L2_ENT_ENGINEERING_RESCUE', '大型企业应急救援能力', 2, 0.54, @p1, 7),
(@config_id, 'L2_ENT_INSURANCE', '保险和再保险企业减灾能力', 2, 0.46, @p1, 8),
(@config_id, 'L2_SOC_MATERIAL', '物资储备能力', 2, 0.23, @p2, 9),
(@config_id, 'L2_SOC_TRANSPORT', '应急运输能力', 2, 0.25, @p2, 10),
(@config_id, 'L2_SOC_RESCUE', '应急救援能力', 2, 0.26, @p2, 11),
(@config_id, 'L2_SOC_PUBLICITY', '科普宣传能力', 2, 0.26, @p2, 12),
(@config_id, 'L2_TWN_DISASTER_MANAGEMENT', '灾害管理能力', 2, 0.35, @p3, 13),
(@config_id, 'L2_TWN_DISASTER_PREPAREDNESS', '灾害备灾能力', 2, 0.32, @p3, 14),
(@config_id, 'L2_TWN_SELF_RESCUE_TRANSFER', '自救转移能力', 2, 0.33, @p3, 15),
(@config_id, 'L2_COM_DISASTER_MANAGEMENT', '灾害管理能力', 2, 0.34, @p4, 16),
(@config_id, 'L2_COM_DISASTER_PREPAREDNESS', '灾害备灾能力', 2, 0.32, @p4, 17),
(@config_id, 'L2_COM_SELF_RESCUE_TRANSFER', '自救转移能力', 2, 0.34, @p4, 18),
(@config_id, 'L2_FAM_VULNERABILITY', '家庭脆弱性', 2, 0.25, @p5, 19),
(@config_id, 'L2_FAM_MATERIAL', '防灾物资储备能力', 2, 0.22, @p5, 20),
(@config_id, 'L2_FAM_INFORMATION', '灾害信息获取能力', 2, 0.27, @p5, 21),
(@config_id, 'L2_FAM_SELF_RESCUE', '灾害自救互救能力', 2, 0.26, @p5, 22);

-- ─── 内江市 (orgcode=5110) ───
DELETE wc FROM weight_config wc LEFT JOIN indicator_weight iw ON iw.config_id = wc.id WHERE wc.orgcode = '5110' AND wc.model_id = 20 AND wc.year = 2020 AND wc.is_deleted = 0;
DELETE FROM indicator_weight WHERE config_id IN (SELECT id FROM weight_config WHERE orgcode = '5110' AND model_id = 20 AND year = 2020);
INSERT INTO weight_config (config_name, description, orgcode, data_source, year, model_id, is_deleted) VALUES ('综合减灾能力评估权重', '2020年内江市综合减灾能力评估权重', '5110', 'baseline', 2020, 20, 0);
SET @config_id = LAST_INSERT_ID();

INSERT INTO indicator_weight (config_id, indicator_code, indicator_name, indicator_level, weight, parent_id, sort_order) VALUES
(@config_id, 'L1_GOVERNMENT', '政府减灾能力', 1, 0.20, NULL, 1),
(@config_id, 'L1_ENTERPRISE', '企业减灾能力', 1, 0.17, NULL, 2),
(@config_id, 'L1_SOCIAL_ORGANIZATION', '社会组织减灾能力', 1, 0.15, NULL, 3),
(@config_id, 'L1_TOWNSHIP', '乡镇（街道）减灾能力', 1, 0.17, NULL, 4),
(@config_id, 'L1_COMMUNITY', '社区（行政区）减灾能力', 1, 0.16, NULL, 5),
(@config_id, 'L1_FAMILY', '家庭减灾能力', 1, 0.15, NULL, 6);

SET @p0 = (SELECT id FROM indicator_weight WHERE config_id = @config_id AND indicator_code = 'L1_GOVERNMENT');
SET @p1 = (SELECT id FROM indicator_weight WHERE config_id = @config_id AND indicator_code = 'L1_ENTERPRISE');
SET @p2 = (SELECT id FROM indicator_weight WHERE config_id = @config_id AND indicator_code = 'L1_SOCIAL_ORGANIZATION');
SET @p3 = (SELECT id FROM indicator_weight WHERE config_id = @config_id AND indicator_code = 'L1_TOWNSHIP');
SET @p4 = (SELECT id FROM indicator_weight WHERE config_id = @config_id AND indicator_code = 'L1_COMMUNITY');
SET @p5 = (SELECT id FROM indicator_weight WHERE config_id = @config_id AND indicator_code = 'L1_FAMILY');

INSERT INTO indicator_weight (config_id, indicator_code, indicator_name, indicator_level, weight, parent_id, sort_order) VALUES
(@config_id, 'L2_GOV_MANAGEMENT', '管理能力', 2, 0.17, @p0, 1),
(@config_id, 'L2_GOV_ENGINEERING', '工程设防能力', 2, 0.16, @p0, 2),
(@config_id, 'L2_GOV_MONITORING', '监测预警能力', 2, 0.17, @p0, 3),
(@config_id, 'L2_GOV_MATERIAL', '物资储备能力', 2, 0.17, @p0, 4),
(@config_id, 'L2_GOV_RESCUE_TEAM', '专业队伍救援能力', 2, 0.17, @p0, 5),
(@config_id, 'L2_GOV_RELOCATION', '转移安置能力', 2, 0.16, @p0, 6),
(@config_id, 'L2_ENT_ENGINEERING_RESCUE', '大型企业应急救援能力', 2, 0.51, @p1, 7),
(@config_id, 'L2_ENT_INSURANCE', '保险和再保险企业减灾能力', 2, 0.49, @p1, 8),
(@config_id, 'L2_SOC_MATERIAL', '物资储备能力', 2, 0.24, @p2, 9),
(@config_id, 'L2_SOC_TRANSPORT', '应急运输能力', 2, 0.25, @p2, 10),
(@config_id, 'L2_SOC_RESCUE', '应急救援能力', 2, 0.25, @p2, 11),
(@config_id, 'L2_SOC_PUBLICITY', '科普宣传能力', 2, 0.26, @p2, 12),
(@config_id, 'L2_TWN_DISASTER_MANAGEMENT', '灾害管理能力', 2, 0.33, @p3, 13),
(@config_id, 'L2_TWN_DISASTER_PREPAREDNESS', '灾害备灾能力', 2, 0.32, @p3, 14),
(@config_id, 'L2_TWN_SELF_RESCUE_TRANSFER', '自救转移能力', 2, 0.35, @p3, 15),
(@config_id, 'L2_COM_DISASTER_MANAGEMENT', '灾害管理能力', 2, 0.34, @p4, 16),
(@config_id, 'L2_COM_DISASTER_PREPAREDNESS', '灾害备灾能力', 2, 0.32, @p4, 17),
(@config_id, 'L2_COM_SELF_RESCUE_TRANSFER', '自救转移能力', 2, 0.34, @p4, 18),
(@config_id, 'L2_FAM_VULNERABILITY', '家庭脆弱性', 2, 0.24, @p5, 19),
(@config_id, 'L2_FAM_MATERIAL', '防灾物资储备能力', 2, 0.23, @p5, 20),
(@config_id, 'L2_FAM_INFORMATION', '灾害信息获取能力', 2, 0.27, @p5, 21),
(@config_id, 'L2_FAM_SELF_RESCUE', '灾害自救互救能力', 2, 0.26, @p5, 22);

-- ─── 攀枝花市 (orgcode=5104) ───
DELETE wc FROM weight_config wc LEFT JOIN indicator_weight iw ON iw.config_id = wc.id WHERE wc.orgcode = '5104' AND wc.model_id = 20 AND wc.year = 2020 AND wc.is_deleted = 0;
DELETE FROM indicator_weight WHERE config_id IN (SELECT id FROM weight_config WHERE orgcode = '5104' AND model_id = 20 AND year = 2020);
INSERT INTO weight_config (config_name, description, orgcode, data_source, year, model_id, is_deleted) VALUES ('综合减灾能力评估权重', '2020年攀枝花市综合减灾能力评估权重', '5104', 'baseline', 2020, 20, 0);
SET @config_id = LAST_INSERT_ID();

INSERT INTO indicator_weight (config_id, indicator_code, indicator_name, indicator_level, weight, parent_id, sort_order) VALUES
(@config_id, 'L1_GOVERNMENT', '政府减灾能力', 1, 0.19, NULL, 1),
(@config_id, 'L1_ENTERPRISE', '企业减灾能力', 1, 0.18, NULL, 2),
(@config_id, 'L1_SOCIAL_ORGANIZATION', '社会组织减灾能力', 1, 0.16, NULL, 3),
(@config_id, 'L1_TOWNSHIP', '乡镇（街道）减灾能力', 1, 0.17, NULL, 4),
(@config_id, 'L1_COMMUNITY', '社区（行政区）减灾能力', 1, 0.16, NULL, 5),
(@config_id, 'L1_FAMILY', '家庭减灾能力', 1, 0.14, NULL, 6);

SET @p0 = (SELECT id FROM indicator_weight WHERE config_id = @config_id AND indicator_code = 'L1_GOVERNMENT');
SET @p1 = (SELECT id FROM indicator_weight WHERE config_id = @config_id AND indicator_code = 'L1_ENTERPRISE');
SET @p2 = (SELECT id FROM indicator_weight WHERE config_id = @config_id AND indicator_code = 'L1_SOCIAL_ORGANIZATION');
SET @p3 = (SELECT id FROM indicator_weight WHERE config_id = @config_id AND indicator_code = 'L1_TOWNSHIP');
SET @p4 = (SELECT id FROM indicator_weight WHERE config_id = @config_id AND indicator_code = 'L1_COMMUNITY');
SET @p5 = (SELECT id FROM indicator_weight WHERE config_id = @config_id AND indicator_code = 'L1_FAMILY');

INSERT INTO indicator_weight (config_id, indicator_code, indicator_name, indicator_level, weight, parent_id, sort_order) VALUES
(@config_id, 'L2_GOV_MANAGEMENT', '管理能力', 2, 0.18, @p0, 1),
(@config_id, 'L2_GOV_ENGINEERING', '工程设防能力', 2, 0.17, @p0, 2),
(@config_id, 'L2_GOV_MONITORING', '监测预警能力', 2, 0.17, @p0, 3),
(@config_id, 'L2_GOV_MATERIAL', '物资储备能力', 2, 0.16, @p0, 4),
(@config_id, 'L2_GOV_RESCUE_TEAM', '专业队伍救援能力', 2, 0.16, @p0, 5),
(@config_id, 'L2_GOV_RELOCATION', '转移安置能力', 2, 0.16, @p0, 6),
(@config_id, 'L2_ENT_ENGINEERING_RESCUE', '大型企业应急救援能力', 2, 0.53, @p1, 7),
(@config_id, 'L2_ENT_INSURANCE', '保险和再保险企业减灾能力', 2, 0.47, @p1, 8),
(@config_id, 'L2_SOC_MATERIAL', '物资储备能力', 2, 0.23, @p2, 9),
(@config_id, 'L2_SOC_TRANSPORT', '应急运输能力', 2, 0.25, @p2, 10),
(@config_id, 'L2_SOC_RESCUE', '应急救援能力', 2, 0.25, @p2, 11),
(@config_id, 'L2_SOC_PUBLICITY', '科普宣传能力', 2, 0.27, @p2, 12),
(@config_id, 'L2_TWN_DISASTER_MANAGEMENT', '灾害管理能力', 2, 0.34, @p3, 13),
(@config_id, 'L2_TWN_DISASTER_PREPAREDNESS', '灾害备灾能力', 2, 0.32, @p3, 14),
(@config_id, 'L2_TWN_SELF_RESCUE_TRANSFER', '自救转移能力', 2, 0.34, @p3, 15),
(@config_id, 'L2_COM_DISASTER_MANAGEMENT', '灾害管理能力', 2, 0.33, @p4, 16),
(@config_id, 'L2_COM_DISASTER_PREPAREDNESS', '灾害备灾能力', 2, 0.32, @p4, 17),
(@config_id, 'L2_COM_SELF_RESCUE_TRANSFER', '自救转移能力', 2, 0.35, @p4, 18),
(@config_id, 'L2_FAM_VULNERABILITY', '家庭脆弱性', 2, 0.25, @p5, 19),
(@config_id, 'L2_FAM_MATERIAL', '防灾物资储备能力', 2, 0.21, @p5, 20),
(@config_id, 'L2_FAM_INFORMATION', '灾害信息获取能力', 2, 0.26, @p5, 21),
(@config_id, 'L2_FAM_SELF_RESCUE', '灾害自救互救能力', 2, 0.28, @p5, 22);

-- ─── 遂宁市 (orgcode=5109) ───
DELETE wc FROM weight_config wc LEFT JOIN indicator_weight iw ON iw.config_id = wc.id WHERE wc.orgcode = '5109' AND wc.model_id = 20 AND wc.year = 2020 AND wc.is_deleted = 0;
DELETE FROM indicator_weight WHERE config_id IN (SELECT id FROM weight_config WHERE orgcode = '5109' AND model_id = 20 AND year = 2020);
INSERT INTO weight_config (config_name, description, orgcode, data_source, year, model_id, is_deleted) VALUES ('综合减灾能力评估权重', '2020年遂宁市综合减灾能力评估权重', '5109', 'baseline', 2020, 20, 0);
SET @config_id = LAST_INSERT_ID();

INSERT INTO indicator_weight (config_id, indicator_code, indicator_name, indicator_level, weight, parent_id, sort_order) VALUES
(@config_id, 'L1_GOVERNMENT', '政府减灾能力', 1, 0.20, NULL, 1),
(@config_id, 'L1_ENTERPRISE', '企业减灾能力', 1, 0.18, NULL, 2),
(@config_id, 'L1_SOCIAL_ORGANIZATION', '社会组织减灾能力', 1, 0.16, NULL, 3),
(@config_id, 'L1_TOWNSHIP', '乡镇（街道）减灾能力', 1, 0.17, NULL, 4),
(@config_id, 'L1_COMMUNITY', '社区（行政区）减灾能力', 1, 0.15, NULL, 5),
(@config_id, 'L1_FAMILY', '家庭减灾能力', 1, 0.14, NULL, 6);

SET @p0 = (SELECT id FROM indicator_weight WHERE config_id = @config_id AND indicator_code = 'L1_GOVERNMENT');
SET @p1 = (SELECT id FROM indicator_weight WHERE config_id = @config_id AND indicator_code = 'L1_ENTERPRISE');
SET @p2 = (SELECT id FROM indicator_weight WHERE config_id = @config_id AND indicator_code = 'L1_SOCIAL_ORGANIZATION');
SET @p3 = (SELECT id FROM indicator_weight WHERE config_id = @config_id AND indicator_code = 'L1_TOWNSHIP');
SET @p4 = (SELECT id FROM indicator_weight WHERE config_id = @config_id AND indicator_code = 'L1_COMMUNITY');
SET @p5 = (SELECT id FROM indicator_weight WHERE config_id = @config_id AND indicator_code = 'L1_FAMILY');

INSERT INTO indicator_weight (config_id, indicator_code, indicator_name, indicator_level, weight, parent_id, sort_order) VALUES
(@config_id, 'L2_GOV_MANAGEMENT', '管理能力', 2, 0.18, @p0, 1),
(@config_id, 'L2_GOV_ENGINEERING', '工程设防能力', 2, 0.17, @p0, 2),
(@config_id, 'L2_GOV_MONITORING', '监测预警能力', 2, 0.17, @p0, 3),
(@config_id, 'L2_GOV_MATERIAL', '物资储备能力', 2, 0.16, @p0, 4),
(@config_id, 'L2_GOV_RESCUE_TEAM', '专业队伍救援能力', 2, 0.16, @p0, 5),
(@config_id, 'L2_GOV_RELOCATION', '转移安置能力', 2, 0.16, @p0, 6),
(@config_id, 'L2_ENT_ENGINEERING_RESCUE', '大型企业应急救援能力', 2, 0.51, @p1, 7),
(@config_id, 'L2_ENT_INSURANCE', '保险和再保险企业减灾能力', 2, 0.49, @p1, 8),
(@config_id, 'L2_SOC_MATERIAL', '物资储备能力', 2, 0.24, @p2, 9),
(@config_id, 'L2_SOC_TRANSPORT', '应急运输能力', 2, 0.25, @p2, 10),
(@config_id, 'L2_SOC_RESCUE', '应急救援能力', 2, 0.26, @p2, 11),
(@config_id, 'L2_SOC_PUBLICITY', '科普宣传能力', 2, 0.25, @p2, 12),
(@config_id, 'L2_TWN_DISASTER_MANAGEMENT', '灾害管理能力', 2, 0.34, @p3, 13),
(@config_id, 'L2_TWN_DISASTER_PREPAREDNESS', '灾害备灾能力', 2, 0.32, @p3, 14),
(@config_id, 'L2_TWN_SELF_RESCUE_TRANSFER', '自救转移能力', 2, 0.34, @p3, 15),
(@config_id, 'L2_COM_DISASTER_MANAGEMENT', '灾害管理能力', 2, 0.34, @p4, 16),
(@config_id, 'L2_COM_DISASTER_PREPAREDNESS', '灾害备灾能力', 2, 0.31, @p4, 17),
(@config_id, 'L2_COM_SELF_RESCUE_TRANSFER', '自救转移能力', 2, 0.35, @p4, 18),
(@config_id, 'L2_FAM_VULNERABILITY', '家庭脆弱性', 2, 0.25, @p5, 19),
(@config_id, 'L2_FAM_MATERIAL', '防灾物资储备能力', 2, 0.23, @p5, 20),
(@config_id, 'L2_FAM_INFORMATION', '灾害信息获取能力', 2, 0.27, @p5, 21),
(@config_id, 'L2_FAM_SELF_RESCUE', '灾害自救互救能力', 2, 0.25, @p5, 22);

-- ─── 雅安市 (orgcode=5118) ───
DELETE wc FROM weight_config wc LEFT JOIN indicator_weight iw ON iw.config_id = wc.id WHERE wc.orgcode = '5118' AND wc.model_id = 20 AND wc.year = 2020 AND wc.is_deleted = 0;
DELETE FROM indicator_weight WHERE config_id IN (SELECT id FROM weight_config WHERE orgcode = '5118' AND model_id = 20 AND year = 2020);
INSERT INTO weight_config (config_name, description, orgcode, data_source, year, model_id, is_deleted) VALUES ('综合减灾能力评估权重', '2020年雅安市综合减灾能力评估权重', '5118', 'baseline', 2020, 20, 0);
SET @config_id = LAST_INSERT_ID();

INSERT INTO indicator_weight (config_id, indicator_code, indicator_name, indicator_level, weight, parent_id, sort_order) VALUES
(@config_id, 'L1_GOVERNMENT', '政府减灾能力', 1, 0.20, NULL, 1),
(@config_id, 'L1_ENTERPRISE', '企业减灾能力', 1, 0.17, NULL, 2),
(@config_id, 'L1_SOCIAL_ORGANIZATION', '社会组织减灾能力', 1, 0.14, NULL, 3),
(@config_id, 'L1_TOWNSHIP', '乡镇（街道）减灾能力', 1, 0.18, NULL, 4),
(@config_id, 'L1_COMMUNITY', '社区（行政区）减灾能力', 1, 0.17, NULL, 5),
(@config_id, 'L1_FAMILY', '家庭减灾能力', 1, 0.14, NULL, 6);

SET @p0 = (SELECT id FROM indicator_weight WHERE config_id = @config_id AND indicator_code = 'L1_GOVERNMENT');
SET @p1 = (SELECT id FROM indicator_weight WHERE config_id = @config_id AND indicator_code = 'L1_ENTERPRISE');
SET @p2 = (SELECT id FROM indicator_weight WHERE config_id = @config_id AND indicator_code = 'L1_SOCIAL_ORGANIZATION');
SET @p3 = (SELECT id FROM indicator_weight WHERE config_id = @config_id AND indicator_code = 'L1_TOWNSHIP');
SET @p4 = (SELECT id FROM indicator_weight WHERE config_id = @config_id AND indicator_code = 'L1_COMMUNITY');
SET @p5 = (SELECT id FROM indicator_weight WHERE config_id = @config_id AND indicator_code = 'L1_FAMILY');

INSERT INTO indicator_weight (config_id, indicator_code, indicator_name, indicator_level, weight, parent_id, sort_order) VALUES
(@config_id, 'L2_GOV_MANAGEMENT', '管理能力', 2, 0.17, @p0, 1),
(@config_id, 'L2_GOV_ENGINEERING', '工程设防能力', 2, 0.17, @p0, 2),
(@config_id, 'L2_GOV_MONITORING', '监测预警能力', 2, 0.17, @p0, 3),
(@config_id, 'L2_GOV_MATERIAL', '物资储备能力', 2, 0.15, @p0, 4),
(@config_id, 'L2_GOV_RESCUE_TEAM', '专业队伍救援能力', 2, 0.16, @p0, 5),
(@config_id, 'L2_GOV_RELOCATION', '转移安置能力', 2, 0.18, @p0, 6),
(@config_id, 'L2_ENT_ENGINEERING_RESCUE', '大型企业应急救援能力', 2, 0.48, @p1, 7),
(@config_id, 'L2_ENT_INSURANCE', '保险和再保险企业减灾能力', 2, 0.52, @p1, 8),
(@config_id, 'L2_SOC_MATERIAL', '物资储备能力', 2, 0.25, @p2, 9),
(@config_id, 'L2_SOC_TRANSPORT', '应急运输能力', 2, 0.24, @p2, 10),
(@config_id, 'L2_SOC_RESCUE', '应急救援能力', 2, 0.24, @p2, 11),
(@config_id, 'L2_SOC_PUBLICITY', '科普宣传能力', 2, 0.27, @p2, 12),
(@config_id, 'L2_TWN_DISASTER_MANAGEMENT', '灾害管理能力', 2, 0.33, @p3, 13),
(@config_id, 'L2_TWN_DISASTER_PREPAREDNESS', '灾害备灾能力', 2, 0.32, @p3, 14),
(@config_id, 'L2_TWN_SELF_RESCUE_TRANSFER', '自救转移能力', 2, 0.35, @p3, 15),
(@config_id, 'L2_COM_DISASTER_MANAGEMENT', '灾害管理能力', 2, 0.33, @p4, 16),
(@config_id, 'L2_COM_DISASTER_PREPAREDNESS', '灾害备灾能力', 2, 0.29, @p4, 17),
(@config_id, 'L2_COM_SELF_RESCUE_TRANSFER', '自救转移能力', 2, 0.38, @p4, 18),
(@config_id, 'L2_FAM_VULNERABILITY', '家庭脆弱性', 2, 0.25, @p5, 19),
(@config_id, 'L2_FAM_MATERIAL', '防灾物资储备能力', 2, 0.20, @p5, 20),
(@config_id, 'L2_FAM_INFORMATION', '灾害信息获取能力', 2, 0.27, @p5, 21),
(@config_id, 'L2_FAM_SELF_RESCUE', '灾害自救互救能力', 2, 0.28, @p5, 22);

-- ─── 宜宾市 (orgcode=5115) ───
DELETE wc FROM weight_config wc LEFT JOIN indicator_weight iw ON iw.config_id = wc.id WHERE wc.orgcode = '5115' AND wc.model_id = 20 AND wc.year = 2020 AND wc.is_deleted = 0;
DELETE FROM indicator_weight WHERE config_id IN (SELECT id FROM weight_config WHERE orgcode = '5115' AND model_id = 20 AND year = 2020);
INSERT INTO weight_config (config_name, description, orgcode, data_source, year, model_id, is_deleted) VALUES ('综合减灾能力评估权重', '2020年宜宾市综合减灾能力评估权重', '5115', 'baseline', 2020, 20, 0);
SET @config_id = LAST_INSERT_ID();

INSERT INTO indicator_weight (config_id, indicator_code, indicator_name, indicator_level, weight, parent_id, sort_order) VALUES
(@config_id, 'L1_GOVERNMENT', '政府减灾能力', 1, 0.18, NULL, 1),
(@config_id, 'L1_ENTERPRISE', '企业减灾能力', 1, 0.16, NULL, 2),
(@config_id, 'L1_SOCIAL_ORGANIZATION', '社会组织减灾能力', 1, 0.16, NULL, 3),
(@config_id, 'L1_TOWNSHIP', '乡镇（街道）减灾能力', 1, 0.17, NULL, 4),
(@config_id, 'L1_COMMUNITY', '社区（行政区）减灾能力', 1, 0.16, NULL, 5),
(@config_id, 'L1_FAMILY', '家庭减灾能力', 1, 0.17, NULL, 6);

SET @p0 = (SELECT id FROM indicator_weight WHERE config_id = @config_id AND indicator_code = 'L1_GOVERNMENT');
SET @p1 = (SELECT id FROM indicator_weight WHERE config_id = @config_id AND indicator_code = 'L1_ENTERPRISE');
SET @p2 = (SELECT id FROM indicator_weight WHERE config_id = @config_id AND indicator_code = 'L1_SOCIAL_ORGANIZATION');
SET @p3 = (SELECT id FROM indicator_weight WHERE config_id = @config_id AND indicator_code = 'L1_TOWNSHIP');
SET @p4 = (SELECT id FROM indicator_weight WHERE config_id = @config_id AND indicator_code = 'L1_COMMUNITY');
SET @p5 = (SELECT id FROM indicator_weight WHERE config_id = @config_id AND indicator_code = 'L1_FAMILY');

INSERT INTO indicator_weight (config_id, indicator_code, indicator_name, indicator_level, weight, parent_id, sort_order) VALUES
(@config_id, 'L2_GOV_MANAGEMENT', '管理能力', 2, 0.16, @p0, 1),
(@config_id, 'L2_GOV_ENGINEERING', '工程设防能力', 2, 0.16, @p0, 2),
(@config_id, 'L2_GOV_MONITORING', '监测预警能力', 2, 0.17, @p0, 3),
(@config_id, 'L2_GOV_MATERIAL', '物资储备能力', 2, 0.16, @p0, 4),
(@config_id, 'L2_GOV_RESCUE_TEAM', '专业队伍救援能力', 2, 0.17, @p0, 5),
(@config_id, 'L2_GOV_RELOCATION', '转移安置能力', 2, 0.18, @p0, 6),
(@config_id, 'L2_ENT_ENGINEERING_RESCUE', '大型企业应急救援能力', 2, 0.51, @p1, 7),
(@config_id, 'L2_ENT_INSURANCE', '保险和再保险企业减灾能力', 2, 0.49, @p1, 8),
(@config_id, 'L2_SOC_MATERIAL', '物资储备能力', 2, 0.24, @p2, 9),
(@config_id, 'L2_SOC_TRANSPORT', '应急运输能力', 2, 0.25, @p2, 10),
(@config_id, 'L2_SOC_RESCUE', '应急救援能力', 2, 0.26, @p2, 11),
(@config_id, 'L2_SOC_PUBLICITY', '科普宣传能力', 2, 0.25, @p2, 12),
(@config_id, 'L2_TWN_DISASTER_MANAGEMENT', '灾害管理能力', 2, 0.33, @p3, 13),
(@config_id, 'L2_TWN_DISASTER_PREPAREDNESS', '灾害备灾能力', 2, 0.32, @p3, 14),
(@config_id, 'L2_TWN_SELF_RESCUE_TRANSFER', '自救转移能力', 2, 0.35, @p3, 15),
(@config_id, 'L2_COM_DISASTER_MANAGEMENT', '灾害管理能力', 2, 0.33, @p4, 16),
(@config_id, 'L2_COM_DISASTER_PREPAREDNESS', '灾害备灾能力', 2, 0.32, @p4, 17),
(@config_id, 'L2_COM_SELF_RESCUE_TRANSFER', '自救转移能力', 2, 0.35, @p4, 18),
(@config_id, 'L2_FAM_VULNERABILITY', '家庭脆弱性', 2, 0.21, @p5, 19),
(@config_id, 'L2_FAM_MATERIAL', '防灾物资储备能力', 2, 0.24, @p5, 20),
(@config_id, 'L2_FAM_INFORMATION', '灾害信息获取能力', 2, 0.27, @p5, 21),
(@config_id, 'L2_FAM_SELF_RESCUE', '灾害自救互救能力', 2, 0.28, @p5, 22);

-- ─── 资阳市 (orgcode=5120) ───
DELETE wc FROM weight_config wc LEFT JOIN indicator_weight iw ON iw.config_id = wc.id WHERE wc.orgcode = '5120' AND wc.model_id = 20 AND wc.year = 2020 AND wc.is_deleted = 0;
DELETE FROM indicator_weight WHERE config_id IN (SELECT id FROM weight_config WHERE orgcode = '5120' AND model_id = 20 AND year = 2020);
INSERT INTO weight_config (config_name, description, orgcode, data_source, year, model_id, is_deleted) VALUES ('综合减灾能力评估权重', '2020年资阳市综合减灾能力评估权重', '5120', 'baseline', 2020, 20, 0);
SET @config_id = LAST_INSERT_ID();

INSERT INTO indicator_weight (config_id, indicator_code, indicator_name, indicator_level, weight, parent_id, sort_order) VALUES
(@config_id, 'L1_GOVERNMENT', '政府减灾能力', 1, 0.19, NULL, 1),
(@config_id, 'L1_ENTERPRISE', '企业减灾能力', 1, 0.16, NULL, 2),
(@config_id, 'L1_SOCIAL_ORGANIZATION', '社会组织减灾能力', 1, 0.16, NULL, 3),
(@config_id, 'L1_TOWNSHIP', '乡镇（街道）减灾能力', 1, 0.17, NULL, 4),
(@config_id, 'L1_COMMUNITY', '社区（行政区）减灾能力', 1, 0.17, NULL, 5),
(@config_id, 'L1_FAMILY', '家庭减灾能力', 1, 0.15, NULL, 6);

SET @p0 = (SELECT id FROM indicator_weight WHERE config_id = @config_id AND indicator_code = 'L1_GOVERNMENT');
SET @p1 = (SELECT id FROM indicator_weight WHERE config_id = @config_id AND indicator_code = 'L1_ENTERPRISE');
SET @p2 = (SELECT id FROM indicator_weight WHERE config_id = @config_id AND indicator_code = 'L1_SOCIAL_ORGANIZATION');
SET @p3 = (SELECT id FROM indicator_weight WHERE config_id = @config_id AND indicator_code = 'L1_TOWNSHIP');
SET @p4 = (SELECT id FROM indicator_weight WHERE config_id = @config_id AND indicator_code = 'L1_COMMUNITY');
SET @p5 = (SELECT id FROM indicator_weight WHERE config_id = @config_id AND indicator_code = 'L1_FAMILY');

INSERT INTO indicator_weight (config_id, indicator_code, indicator_name, indicator_level, weight, parent_id, sort_order) VALUES
(@config_id, 'L2_GOV_MANAGEMENT', '管理能力', 2, 0.18, @p0, 1),
(@config_id, 'L2_GOV_ENGINEERING', '工程设防能力', 2, 0.16, @p0, 2),
(@config_id, 'L2_GOV_MONITORING', '监测预警能力', 2, 0.17, @p0, 3),
(@config_id, 'L2_GOV_MATERIAL', '物资储备能力', 2, 0.16, @p0, 4),
(@config_id, 'L2_GOV_RESCUE_TEAM', '专业队伍救援能力', 2, 0.17, @p0, 5),
(@config_id, 'L2_GOV_RELOCATION', '转移安置能力', 2, 0.16, @p0, 6),
(@config_id, 'L2_ENT_ENGINEERING_RESCUE', '大型企业应急救援能力', 2, 0.50, @p1, 7),
(@config_id, 'L2_ENT_INSURANCE', '保险和再保险企业减灾能力', 2, 0.50, @p1, 8),
(@config_id, 'L2_SOC_MATERIAL', '物资储备能力', 2, 0.25, @p2, 9),
(@config_id, 'L2_SOC_TRANSPORT', '应急运输能力', 2, 0.25, @p2, 10),
(@config_id, 'L2_SOC_RESCUE', '应急救援能力', 2, 0.25, @p2, 11),
(@config_id, 'L2_SOC_PUBLICITY', '科普宣传能力', 2, 0.25, @p2, 12),
(@config_id, 'L2_TWN_DISASTER_MANAGEMENT', '灾害管理能力', 2, 0.34, @p3, 13),
(@config_id, 'L2_TWN_DISASTER_PREPAREDNESS', '灾害备灾能力', 2, 0.33, @p3, 14),
(@config_id, 'L2_TWN_SELF_RESCUE_TRANSFER', '自救转移能力', 2, 0.33, @p3, 15),
(@config_id, 'L2_COM_DISASTER_MANAGEMENT', '灾害管理能力', 2, 0.34, @p4, 16),
(@config_id, 'L2_COM_DISASTER_PREPAREDNESS', '灾害备灾能力', 2, 0.32, @p4, 17),
(@config_id, 'L2_COM_SELF_RESCUE_TRANSFER', '自救转移能力', 2, 0.34, @p4, 18),
(@config_id, 'L2_FAM_VULNERABILITY', '家庭脆弱性', 2, 0.25, @p5, 19),
(@config_id, 'L2_FAM_MATERIAL', '防灾物资储备能力', 2, 0.21, @p5, 20),
(@config_id, 'L2_FAM_INFORMATION', '灾害信息获取能力', 2, 0.29, @p5, 21),
(@config_id, 'L2_FAM_SELF_RESCUE', '灾害自救互救能力', 2, 0.25, @p5, 22);

-- ─── 自贡市 (orgcode=5103) ───
DELETE wc FROM weight_config wc LEFT JOIN indicator_weight iw ON iw.config_id = wc.id WHERE wc.orgcode = '5103' AND wc.model_id = 20 AND wc.year = 2020 AND wc.is_deleted = 0;
DELETE FROM indicator_weight WHERE config_id IN (SELECT id FROM weight_config WHERE orgcode = '5103' AND model_id = 20 AND year = 2020);
INSERT INTO weight_config (config_name, description, orgcode, data_source, year, model_id, is_deleted) VALUES ('综合减灾能力评估权重', '2020年自贡市综合减灾能力评估权重', '5103', 'baseline', 2020, 20, 0);
SET @config_id = LAST_INSERT_ID();

INSERT INTO indicator_weight (config_id, indicator_code, indicator_name, indicator_level, weight, parent_id, sort_order) VALUES
(@config_id, 'L1_GOVERNMENT', '政府减灾能力', 1, 0.20, NULL, 1),
(@config_id, 'L1_ENTERPRISE', '企业减灾能力', 1, 0.17, NULL, 2),
(@config_id, 'L1_SOCIAL_ORGANIZATION', '社会组织减灾能力', 1, 0.16, NULL, 3),
(@config_id, 'L1_TOWNSHIP', '乡镇（街道）减灾能力', 1, 0.18, NULL, 4),
(@config_id, 'L1_COMMUNITY', '社区（行政区）减灾能力', 1, 0.16, NULL, 5),
(@config_id, 'L1_FAMILY', '家庭减灾能力', 1, 0.13, NULL, 6);

SET @p0 = (SELECT id FROM indicator_weight WHERE config_id = @config_id AND indicator_code = 'L1_GOVERNMENT');
SET @p1 = (SELECT id FROM indicator_weight WHERE config_id = @config_id AND indicator_code = 'L1_ENTERPRISE');
SET @p2 = (SELECT id FROM indicator_weight WHERE config_id = @config_id AND indicator_code = 'L1_SOCIAL_ORGANIZATION');
SET @p3 = (SELECT id FROM indicator_weight WHERE config_id = @config_id AND indicator_code = 'L1_TOWNSHIP');
SET @p4 = (SELECT id FROM indicator_weight WHERE config_id = @config_id AND indicator_code = 'L1_COMMUNITY');
SET @p5 = (SELECT id FROM indicator_weight WHERE config_id = @config_id AND indicator_code = 'L1_FAMILY');

INSERT INTO indicator_weight (config_id, indicator_code, indicator_name, indicator_level, weight, parent_id, sort_order) VALUES
(@config_id, 'L2_GOV_MANAGEMENT', '管理能力', 2, 0.18, @p0, 1),
(@config_id, 'L2_GOV_ENGINEERING', '工程设防能力', 2, 0.16, @p0, 2),
(@config_id, 'L2_GOV_MONITORING', '监测预警能力', 2, 0.17, @p0, 3),
(@config_id, 'L2_GOV_MATERIAL', '物资储备能力', 2, 0.16, @p0, 4),
(@config_id, 'L2_GOV_RESCUE_TEAM', '专业队伍救援能力', 2, 0.17, @p0, 5),
(@config_id, 'L2_GOV_RELOCATION', '转移安置能力', 2, 0.16, @p0, 6),
(@config_id, 'L2_ENT_ENGINEERING_RESCUE', '大型企业应急救援能力', 2, 0.49, @p1, 7),
(@config_id, 'L2_ENT_INSURANCE', '保险和再保险企业减灾能力', 2, 0.51, @p1, 8),
(@config_id, 'L2_SOC_MATERIAL', '物资储备能力', 2, 0.23, @p2, 9),
(@config_id, 'L2_SOC_TRANSPORT', '应急运输能力', 2, 0.25, @p2, 10),
(@config_id, 'L2_SOC_RESCUE', '应急救援能力', 2, 0.26, @p2, 11),
(@config_id, 'L2_SOC_PUBLICITY', '科普宣传能力', 2, 0.26, @p2, 12),
(@config_id, 'L2_TWN_DISASTER_MANAGEMENT', '灾害管理能力', 2, 0.33, @p3, 13),
(@config_id, 'L2_TWN_DISASTER_PREPAREDNESS', '灾害备灾能力', 2, 0.33, @p3, 14),
(@config_id, 'L2_TWN_SELF_RESCUE_TRANSFER', '自救转移能力', 2, 0.34, @p3, 15),
(@config_id, 'L2_COM_DISASTER_MANAGEMENT', '灾害管理能力', 2, 0.33, @p4, 16),
(@config_id, 'L2_COM_DISASTER_PREPAREDNESS', '灾害备灾能力', 2, 0.31, @p4, 17),
(@config_id, 'L2_COM_SELF_RESCUE_TRANSFER', '自救转移能力', 2, 0.36, @p4, 18),
(@config_id, 'L2_FAM_VULNERABILITY', '家庭脆弱性', 2, 0.24, @p5, 19),
(@config_id, 'L2_FAM_MATERIAL', '防灾物资储备能力', 2, 0.21, @p5, 20),
(@config_id, 'L2_FAM_INFORMATION', '灾害信息获取能力', 2, 0.30, @p5, 21),
(@config_id, 'L2_FAM_SELF_RESCUE', '灾害自救互救能力', 2, 0.25, @p5, 22);
