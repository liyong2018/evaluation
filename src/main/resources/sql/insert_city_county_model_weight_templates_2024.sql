SET NAMES utf8mb4;
-- 从《指标体系排版329-川大确认(1).docx》A4/A5/A7表生成市级兜底模板。
-- 目标：区县相关模型按市级 orgcode(4位)兜底；乡镇/社区模型使用 DOCX A4/A5 市级行，不再使用旧固定模板。
-- 当前业务页面请求 2024 年，因此这些兜底模板写入 year=2024，创建时间使用 NOW()。

DROP TABLE IF EXISTS tmp_city_weight_sources;
CREATE TABLE tmp_city_weight_sources AS
SELECT wc.id AS model20_config_id, wc.orgcode, wc.config_name
FROM weight_config wc
WHERE wc.is_deleted = 0
  AND wc.year = 2020
  AND wc.model_id = 20
  AND wc.orgcode REGEXP '^[0-9]{4}$';

DROP TABLE IF EXISTS tmp_township_l1_docx;
CREATE TABLE tmp_township_l1_docx (orgcode VARCHAR(16) PRIMARY KEY, org_name VARCHAR(64), disaster_management DECIMAL(18,12), disaster_preparedness DECIMAL(18,12), self_rescue_transfer DECIMAL(18,12));
INSERT INTO tmp_township_l1_docx (orgcode, org_name, disaster_management, disaster_preparedness, self_rescue_transfer) VALUES
  ('5132', '阿坝州', 0.33, 0.33, 0.34),
  ('5119', '巴中市', 0.33, 0.31, 0.36),
  ('5101', '成都市', 0.33, 0.31, 0.36),
  ('5117', '达州市', 0.33, 0.32, 0.35),
  ('5106', '德阳市', 0.34, 0.31, 0.35),
  ('5133', '甘孜州', 0.34, 0.32, 0.34),
  ('5116', '广安市', 0.34, 0.32, 0.34),
  ('5108', '广元市', 0.34, 0.34, 0.32),
  ('5111', '乐山市', 0.33, 0.32, 0.35),
  ('5134', '凉山州', 0.33, 0.32, 0.35),
  ('5105', '泸州市', 0.33, 0.33, 0.34),
  ('5114', '眉山市', 0.33, 0.32, 0.35),
  ('5107', '绵阳市', 0.33, 0.32, 0.35),
  ('5113', '南充市', 0.35, 0.32, 0.33),
  ('5110', '内江市', 0.33, 0.32, 0.35),
  ('5104', '攀枝花市', 0.34, 0.32, 0.34),
  ('5109', '遂宁市', 0.34, 0.32, 0.34),
  ('5118', '雅安市', 0.33, 0.32, 0.35),
  ('5115', '宜宾市', 0.33, 0.32, 0.35),
  ('5120', '资阳市', 0.34, 0.33, 0.33),
  ('5103', '自贡市', 0.33, 0.33, 0.34);

DROP TABLE IF EXISTS tmp_township_l2_docx;
CREATE TABLE tmp_township_l2_docx (orgcode VARCHAR(16) PRIMARY KEY, org_name VARCHAR(64), management_capability DECIMAL(18,12), risk_assessment DECIMAL(18,12), funding DECIMAL(18,12), material DECIMAL(18,12), medical DECIMAL(18,12), self_rescue DECIMAL(18,12), public_avoidance DECIMAL(18,12), relocation DECIMAL(18,12));
INSERT INTO tmp_township_l2_docx (orgcode, org_name, management_capability, risk_assessment, funding, material, medical, self_rescue, public_avoidance, relocation) VALUES
  ('5132', '阿坝州', 0.36, 0.33, 0.31, 0.53, 0.47, 0.32, 0.32, 0.36),
  ('5119', '巴中市', 0.35, 0.3, 0.35, 0.5, 0.5, 0.32, 0.33, 0.35),
  ('5101', '成都市', 0.35, 0.31, 0.34, 0.51, 0.49, 0.37, 0.32, 0.31),
  ('5117', '达州市', 0.35, 0.33, 0.32, 0.51, 0.49, 0.33, 0.33, 0.34),
  ('5106', '德阳市', 0.34, 0.33, 0.33, 0.51, 0.49, 0.32, 0.33, 0.35),
  ('5133', '甘孜州', 0.36, 0.33, 0.31, 0.51, 0.49, 0.33, 0.32, 0.35),
  ('5116', '广安市', 0.37, 0.31, 0.32, 0.51, 0.49, 0.34, 0.32, 0.34),
  ('5108', '广元市', 0.34, 0.31, 0.35, 0.5, 0.5, 0.34, 0.33, 0.33),
  ('5111', '乐山市', 0.36, 0.32, 0.32, 0.51, 0.49, 0.32, 0.33, 0.35),
  ('5134', '凉山州', 0.35, 0.32, 0.33, 0.51, 0.49, 0.33, 0.34, 0.33),
  ('5105', '泸州市', 0.35, 0.33, 0.32, 0.48, 0.52, 0.33, 0.33, 0.34),
  ('5114', '眉山市', 0.37, 0.31, 0.32, 0.51, 0.49, 0.33, 0.33, 0.34),
  ('5107', '绵阳市', 0.36, 0.34, 0.3, 0.49, 0.51, 0.33, 0.33, 0.34),
  ('5113', '南充市', 0.35, 0.34, 0.31, 0.5, 0.5, 0.33, 0.32, 0.35),
  ('5110', '内江市', 0.35, 0.33, 0.32, 0.5, 0.5, 0.33, 0.34, 0.33),
  ('5104', '攀枝花市', 0.36, 0.33, 0.31, 0.49, 0.51, 0.34, 0.32, 0.34),
  ('5109', '遂宁市', 0.38, 0.32, 0.3, 0.52, 0.48, 0.32, 0.34, 0.34),
  ('5118', '雅安市', 0.37, 0.33, 0.3, 0.55, 0.45, 0.32, 0.33, 0.35),
  ('5115', '宜宾市', 0.37, 0.32, 0.31, 0.52, 0.48, 0.33, 0.33, 0.34),
  ('5120', '资阳市', 0.34, 0.33, 0.33, 0.5, 0.5, 0.32, 0.34, 0.34),
  ('5103', '自贡市', 0.35, 0.33, 0.32, 0.48, 0.52, 0.33, 0.33, 0.34);

DROP TABLE IF EXISTS tmp_community_l1_docx;
CREATE TABLE tmp_community_l1_docx (orgcode VARCHAR(16) PRIMARY KEY, org_name VARCHAR(64), disaster_management DECIMAL(18,12), disaster_preparedness DECIMAL(18,12), self_rescue_transfer DECIMAL(18,12));
INSERT INTO tmp_community_l1_docx (orgcode, org_name, disaster_management, disaster_preparedness, self_rescue_transfer) VALUES
  ('5132', '阿坝州', 0.34, 0.33, 0.33),
  ('5119', '巴中市', 0.32, 0.32, 0.36),
  ('5101', '成都市', 0.33, 0.31, 0.36),
  ('5117', '达州市', 0.33, 0.3, 0.37),
  ('5106', '德阳市', 0.34, 0.31, 0.35),
  ('5133', '甘孜州', 0.34, 0.31, 0.35),
  ('5116', '广安市', 0.34, 0.31, 0.35),
  ('5108', '广元市', 0.35, 0.32, 0.33),
  ('5111', '乐山市', 0.33, 0.31, 0.36),
  ('5134', '凉山州', 0.34, 0.31, 0.35),
  ('5105', '泸州市', 0.33, 0.32, 0.35),
  ('5114', '眉山市', 0.32, 0.31, 0.37),
  ('5107', '绵阳市', 0.33, 0.31, 0.36),
  ('5113', '南充市', 0.34, 0.32, 0.34),
  ('5110', '内江市', 0.33, 0.31, 0.36),
  ('5104', '攀枝花市', 0.33, 0.32, 0.35),
  ('5109', '遂宁市', 0.34, 0.31, 0.35),
  ('5118', '雅安市', 0.33, 0.29, 0.38),
  ('5115', '宜宾市', 0.33, 0.3, 0.37),
  ('5120', '资阳市', 0.34, 0.32, 0.34),
  ('5103', '自贡市', 0.33, 0.31, 0.36);

DROP TABLE IF EXISTS tmp_community_l2_docx;
CREATE TABLE tmp_community_l2_docx (orgcode VARCHAR(16) PRIMARY KEY, org_name VARCHAR(64), plan_construction DECIMAL(18,12), hazard_inspection DECIMAL(18,12), risk_assessment DECIMAL(18,12), funding DECIMAL(18,12), material DECIMAL(18,12), medical DECIMAL(18,12), self_rescue DECIMAL(18,12), public_avoidance DECIMAL(18,12), relocation DECIMAL(18,12));
INSERT INTO tmp_community_l2_docx (orgcode, org_name, plan_construction, hazard_inspection, risk_assessment, funding, material, medical, self_rescue, public_avoidance, relocation) VALUES
  ('5132', '阿坝州', 0.24, 0.28, 0.24, 0.24, 0.52, 0.48, 0.32, 0.33, 0.35),
  ('5119', '巴中市', 0.24, 0.29, 0.23, 0.24, 0.51, 0.49, 0.33, 0.33, 0.34),
  ('5101', '成都市', 0.24, 0.29, 0.24, 0.23, 0.55, 0.45, 0.34, 0.33, 0.33),
  ('5117', '达州市', 0.24, 0.28, 0.25, 0.23, 0.53, 0.47, 0.32, 0.33, 0.35),
  ('5106', '德阳市', 0.26, 0.28, 0.24, 0.22, 0.52, 0.48, 0.33, 0.34, 0.33),
  ('5133', '甘孜州', 0.26, 0.28, 0.25, 0.21, 0.51, 0.49, 0.35, 0.32, 0.33),
  ('5116', '广安市', 0.25, 0.29, 0.24, 0.22, 0.52, 0.48, 0.33, 0.34, 0.33),
  ('5108', '广元市', 0.25, 0.28, 0.23, 0.24, 0.51, 0.49, 0.34, 0.34, 0.32),
  ('5111', '乐山市', 0.27, 0.27, 0.23, 0.23, 0.52, 0.48, 0.32, 0.33, 0.35),
  ('5134', '凉山州', 0.25, 0.28, 0.24, 0.23, 0.52, 0.48, 0.33, 0.34, 0.33),
  ('5105', '泸州市', 0.26, 0.28, 0.24, 0.22, 0.49, 0.51, 0.34, 0.33, 0.33),
  ('5114', '眉山市', 0.25, 0.29, 0.23, 0.23, 0.52, 0.48, 0.33, 0.34, 0.33),
  ('5107', '绵阳市', 0.27, 0.28, 0.25, 0.2, 0.51, 0.49, 0.33, 0.33, 0.34),
  ('5113', '南充市', 0.26, 0.28, 0.24, 0.22, 0.5, 0.5, 0.34, 0.32, 0.34),
  ('5110', '内江市', 0.24, 0.28, 0.24, 0.24, 0.51, 0.49, 0.32, 0.35, 0.33),
  ('5104', '攀枝花市', 0.26, 0.29, 0.24, 0.21, 0.52, 0.48, 0.33, 0.33, 0.34),
  ('5109', '遂宁市', 0.27, 0.28, 0.24, 0.21, 0.52, 0.48, 0.33, 0.33, 0.34),
  ('5118', '雅安市', 0.25, 0.31, 0.24, 0.2, 0.55, 0.45, 0.34, 0.31, 0.35),
  ('5115', '宜宾市', 0.26, 0.28, 0.25, 0.21, 0.52, 0.48, 0.33, 0.33, 0.34),
  ('5120', '资阳市', 0.25, 0.27, 0.24, 0.24, 0.49, 0.51, 0.32, 0.33, 0.35),
  ('5103', '自贡市', 0.25, 0.28, 0.25, 0.22, 0.5, 0.5, 0.33, 0.33, 0.34);

-- 只清理此前脚本生成的市级兜底模板，避免误删人工维护配置。
UPDATE weight_config wc
JOIN tmp_city_weight_sources src ON src.orgcode = wc.orgcode
SET wc.is_deleted = 1, wc.update_time = NOW()
WHERE wc.is_deleted = 0
  AND wc.model_id IN (3, 4, 8, 11)
  AND wc.orgcode REGEXP '^[0-9]{4}$'
  AND wc.description LIKE '%市级兜底%';

INSERT INTO weight_config (config_name, description, orgcode, data_source, year, model_id, create_time, is_deleted)
SELECT '乡镇减灾能力评估模型', '从DOCX A4表提取的市级兜底乡镇减灾能力评估权重', orgcode, 'township', 2024, 3, NOW(), 0
FROM tmp_city_weight_sources;

INSERT INTO weight_config (config_name, description, orgcode, data_source, year, model_id, create_time, is_deleted)
SELECT '社区-行政村能力评估模型', '从DOCX A5表提取的市级兜底社区-行政村能力评估权重', orgcode, 'community', 2024, 4, NOW(), 0
FROM tmp_city_weight_sources;

INSERT INTO weight_config (config_name, description, orgcode, data_source, year, model_id, create_time, is_deleted)
SELECT '社区-乡镇能力评估模型', '从DOCX A5表提取的市级兜底社区-乡镇能力评估权重', orgcode, 'community', 2024, 8, NOW(), 0
FROM tmp_city_weight_sources;

INSERT INTO weight_config (config_name, description, orgcode, data_source, year, model_id, create_time, is_deleted)
SELECT '综合减灾能力评估模型', '从DOCX A7表和市级综合模型派生的市级兜底区县综合减灾能力评估权重', orgcode, 'community', 2024, 11, NOW(), 0
FROM tmp_city_weight_sources;

DROP TABLE IF EXISTS tmp_new_configs;
CREATE TABLE tmp_new_configs AS
SELECT wc.id, wc.orgcode, wc.model_id, src.model20_config_id
FROM weight_config wc
JOIN tmp_city_weight_sources src ON src.orgcode = wc.orgcode
WHERE wc.is_deleted = 0
  AND wc.year = 2024
  AND wc.model_id IN (3, 4, 8, 11)
  AND wc.description LIKE '%市级兜底%';

-- model 3: 区县-乡镇（街道）减灾能力评估，使用 A4-1/A4-2 市级行。
INSERT INTO indicator_weight (config_id, indicator_code, indicator_name, indicator_level, weight, parent_id, sort_order, create_time)
SELECT c.id, 'L1_DISASTER_MANAGEMENT', '灾害管理能力', 1, v.disaster_management, NULL, 1, NOW() FROM tmp_new_configs c JOIN tmp_township_l1_docx v ON v.orgcode = c.orgcode WHERE c.model_id = 3
UNION ALL SELECT c.id, 'L1_DISASTER_PREPAREDNESS', '灾害备灾能力', 1, v.disaster_preparedness, NULL, 2, NOW() FROM tmp_new_configs c JOIN tmp_township_l1_docx v ON v.orgcode = c.orgcode WHERE c.model_id = 3
UNION ALL SELECT c.id, 'L1_SELF_RESCUE_TRANSFER', '自救转移能力', 1, v.self_rescue_transfer, NULL, 3, NOW() FROM tmp_new_configs c JOIN tmp_township_l1_docx v ON v.orgcode = c.orgcode WHERE c.model_id = 3;

INSERT INTO indicator_weight (config_id, indicator_code, indicator_name, indicator_level, weight, parent_id, sort_order, create_time)
SELECT c.id, 'L2_MANAGEMENT_CAPABILITY', '队伍管理能力', 2, v.management_capability, p.id, 1, NOW() FROM tmp_new_configs c JOIN tmp_township_l2_docx v ON v.orgcode = c.orgcode JOIN indicator_weight p ON p.config_id = c.id AND p.indicator_code = 'L1_DISASTER_MANAGEMENT' WHERE c.model_id = 3
UNION ALL SELECT c.id, 'L2_RISK_ASSESSMENT', '风险评估能力', 2, v.risk_assessment, p.id, 2, NOW() FROM tmp_new_configs c JOIN tmp_township_l2_docx v ON v.orgcode = c.orgcode JOIN indicator_weight p ON p.config_id = c.id AND p.indicator_code = 'L1_DISASTER_MANAGEMENT' WHERE c.model_id = 3
UNION ALL SELECT c.id, 'L2_FUNDING', '财政投入能力', 2, v.funding, p.id, 3, NOW() FROM tmp_new_configs c JOIN tmp_township_l2_docx v ON v.orgcode = c.orgcode JOIN indicator_weight p ON p.config_id = c.id AND p.indicator_code = 'L1_DISASTER_MANAGEMENT' WHERE c.model_id = 3
UNION ALL SELECT c.id, 'L2_MATERIAL', '物资储备能力', 2, v.material, p.id, 4, NOW() FROM tmp_new_configs c JOIN tmp_township_l2_docx v ON v.orgcode = c.orgcode JOIN indicator_weight p ON p.config_id = c.id AND p.indicator_code = 'L1_DISASTER_PREPAREDNESS' WHERE c.model_id = 3
UNION ALL SELECT c.id, 'L2_MEDICAL', '医疗保障能力', 2, v.medical, p.id, 5, NOW() FROM tmp_new_configs c JOIN tmp_township_l2_docx v ON v.orgcode = c.orgcode JOIN indicator_weight p ON p.config_id = c.id AND p.indicator_code = 'L1_DISASTER_PREPAREDNESS' WHERE c.model_id = 3
UNION ALL SELECT c.id, 'L2_SELF_RESCUE', '自救互救能力', 2, v.self_rescue, p.id, 6, NOW() FROM tmp_new_configs c JOIN tmp_township_l2_docx v ON v.orgcode = c.orgcode JOIN indicator_weight p ON p.config_id = c.id AND p.indicator_code = 'L1_SELF_RESCUE_TRANSFER' WHERE c.model_id = 3
UNION ALL SELECT c.id, 'L2_PUBLIC_AVOIDANCE', '公众避险能力', 2, v.public_avoidance, p.id, 7, NOW() FROM tmp_new_configs c JOIN tmp_township_l2_docx v ON v.orgcode = c.orgcode JOIN indicator_weight p ON p.config_id = c.id AND p.indicator_code = 'L1_SELF_RESCUE_TRANSFER' WHERE c.model_id = 3
UNION ALL SELECT c.id, 'L2_RELOCATION', '转移安置能力', 2, v.relocation, p.id, 8, NOW() FROM tmp_new_configs c JOIN tmp_township_l2_docx v ON v.orgcode = c.orgcode JOIN indicator_weight p ON p.config_id = c.id AND p.indicator_code = 'L1_SELF_RESCUE_TRANSFER' WHERE c.model_id = 3;

-- model 4/8: 区县-社区（行政村）减灾能力评估，使用 A5-1/A5-2 市级行。
INSERT INTO indicator_weight (config_id, indicator_code, indicator_name, indicator_level, weight, parent_id, sort_order, create_time)
SELECT c.id, 'L1_DISASTER_MANAGEMENT', '灾害管理能力', 1, v.disaster_management, NULL, 1, NOW() FROM tmp_new_configs c JOIN tmp_community_l1_docx v ON v.orgcode = c.orgcode WHERE c.model_id IN (4, 8)
UNION ALL SELECT c.id, 'L1_DISASTER_PREPAREDNESS', '灾害备灾能力', 1, v.disaster_preparedness, NULL, 2, NOW() FROM tmp_new_configs c JOIN tmp_community_l1_docx v ON v.orgcode = c.orgcode WHERE c.model_id IN (4, 8)
UNION ALL SELECT c.id, 'L1_SELF_RESCUE_TRANSFER', '自救转移能力', 1, v.self_rescue_transfer, NULL, 3, NOW() FROM tmp_new_configs c JOIN tmp_community_l1_docx v ON v.orgcode = c.orgcode WHERE c.model_id IN (4, 8);

INSERT INTO indicator_weight (config_id, indicator_code, indicator_name, indicator_level, weight, parent_id, sort_order, create_time)
SELECT c.id, 'L2_PLAN_CONSTRUCTION', '预案建设能力', 2, v.plan_construction, p.id, 1, NOW() FROM tmp_new_configs c JOIN tmp_community_l2_docx v ON v.orgcode = c.orgcode JOIN indicator_weight p ON p.config_id = c.id AND p.indicator_code = 'L1_DISASTER_MANAGEMENT' WHERE c.model_id IN (4, 8)
UNION ALL SELECT c.id, 'L2_HAZARD_INSPECTION', '隐患排查能力', 2, v.hazard_inspection, p.id, 2, NOW() FROM tmp_new_configs c JOIN tmp_community_l2_docx v ON v.orgcode = c.orgcode JOIN indicator_weight p ON p.config_id = c.id AND p.indicator_code = 'L1_DISASTER_MANAGEMENT' WHERE c.model_id IN (4, 8)
UNION ALL SELECT c.id, 'L2_RISK_ASSESSMENT', '风险评估能力', 2, v.risk_assessment, p.id, 3, NOW() FROM tmp_new_configs c JOIN tmp_community_l2_docx v ON v.orgcode = c.orgcode JOIN indicator_weight p ON p.config_id = c.id AND p.indicator_code = 'L1_DISASTER_MANAGEMENT' WHERE c.model_id IN (4, 8)
UNION ALL SELECT c.id, 'L2_FUNDING', '财政投入能力', 2, v.funding, p.id, 4, NOW() FROM tmp_new_configs c JOIN tmp_community_l2_docx v ON v.orgcode = c.orgcode JOIN indicator_weight p ON p.config_id = c.id AND p.indicator_code = 'L1_DISASTER_MANAGEMENT' WHERE c.model_id IN (4, 8)
UNION ALL SELECT c.id, 'L2_MATERIAL', '物资储备能力', 2, v.material, p.id, 5, NOW() FROM tmp_new_configs c JOIN tmp_community_l2_docx v ON v.orgcode = c.orgcode JOIN indicator_weight p ON p.config_id = c.id AND p.indicator_code = 'L1_DISASTER_PREPAREDNESS' WHERE c.model_id IN (4, 8)
UNION ALL SELECT c.id, 'L2_MEDICAL', '医疗保障能力', 2, v.medical, p.id, 6, NOW() FROM tmp_new_configs c JOIN tmp_community_l2_docx v ON v.orgcode = c.orgcode JOIN indicator_weight p ON p.config_id = c.id AND p.indicator_code = 'L1_DISASTER_PREPAREDNESS' WHERE c.model_id IN (4, 8)
UNION ALL SELECT c.id, 'L2_SELF_RESCUE', '自救互救能力', 2, v.self_rescue, p.id, 7, NOW() FROM tmp_new_configs c JOIN tmp_community_l2_docx v ON v.orgcode = c.orgcode JOIN indicator_weight p ON p.config_id = c.id AND p.indicator_code = 'L1_SELF_RESCUE_TRANSFER' WHERE c.model_id IN (4, 8)
UNION ALL SELECT c.id, 'L2_PUBLIC_AVOIDANCE', '公众避险能力', 2, v.public_avoidance, p.id, 8, NOW() FROM tmp_new_configs c JOIN tmp_community_l2_docx v ON v.orgcode = c.orgcode JOIN indicator_weight p ON p.config_id = c.id AND p.indicator_code = 'L1_SELF_RESCUE_TRANSFER' WHERE c.model_id IN (4, 8)
UNION ALL SELECT c.id, 'L2_RELOCATION', '转移安置能力', 2, v.relocation, p.id, 9, NOW() FROM tmp_new_configs c JOIN tmp_community_l2_docx v ON v.orgcode = c.orgcode JOIN indicator_weight p ON p.config_id = c.id AND p.indicator_code = 'L1_SELF_RESCUE_TRANSFER' WHERE c.model_id IN (4, 8);

-- model 11: 区县综合，一级权重由模型20的乡镇/社区一级权重归一化派生；二级权重仍取模型20的 A7-2 乡镇/社区部分。
INSERT INTO indicator_weight (config_id, indicator_code, indicator_name, indicator_level, weight, parent_id, sort_order, create_time)
SELECT c.id, 'L1_TOWNSHIP', '乡镇减灾能力', 1, tw.weight / (tw.weight + cm.weight), NULL, 1, NOW() FROM tmp_new_configs c JOIN indicator_weight tw ON tw.config_id = c.model20_config_id AND tw.indicator_code = 'L1_TOWNSHIP' JOIN indicator_weight cm ON cm.config_id = c.model20_config_id AND cm.indicator_code = 'L1_COMMUNITY' WHERE c.model_id = 11
UNION ALL SELECT c.id, 'L1_COMMUNITY', '社区减灾能力', 1, cm.weight / (tw.weight + cm.weight), NULL, 2, NOW() FROM tmp_new_configs c JOIN indicator_weight tw ON tw.config_id = c.model20_config_id AND tw.indicator_code = 'L1_TOWNSHIP' JOIN indicator_weight cm ON cm.config_id = c.model20_config_id AND cm.indicator_code = 'L1_COMMUNITY' WHERE c.model_id = 11;

INSERT INTO indicator_weight (config_id, indicator_code, indicator_name, indicator_level, weight, parent_id, sort_order, create_time)
SELECT c.id, 'L2_TOWNSHIP_DISASTER_MANAGEMENT', '乡镇-灾害管理能力', 2, src.weight, p.id, 1, NOW() FROM tmp_new_configs c JOIN indicator_weight p ON p.config_id = c.id AND p.indicator_code = 'L1_TOWNSHIP' JOIN indicator_weight src ON src.config_id = c.model20_config_id AND src.indicator_code = 'L2_TWN_DISASTER_MANAGEMENT' WHERE c.model_id = 11
UNION ALL SELECT c.id, 'L2_TOWNSHIP_DISASTER_PREPAREDNESS', '乡镇-灾害备灾能力', 2, src.weight, p.id, 2, NOW() FROM tmp_new_configs c JOIN indicator_weight p ON p.config_id = c.id AND p.indicator_code = 'L1_TOWNSHIP' JOIN indicator_weight src ON src.config_id = c.model20_config_id AND src.indicator_code = 'L2_TWN_DISASTER_PREPAREDNESS' WHERE c.model_id = 11
UNION ALL SELECT c.id, 'L2_TOWNSHIP_SELF_RESCUE_TRANSFER', '乡镇-自救转移能力', 2, src.weight, p.id, 3, NOW() FROM tmp_new_configs c JOIN indicator_weight p ON p.config_id = c.id AND p.indicator_code = 'L1_TOWNSHIP' JOIN indicator_weight src ON src.config_id = c.model20_config_id AND src.indicator_code = 'L2_TWN_SELF_RESCUE_TRANSFER' WHERE c.model_id = 11
UNION ALL SELECT c.id, 'L2_COMMUNITY_DISASTER_MANAGEMENT', '社区-灾害管理能力', 2, src.weight, p.id, 4, NOW() FROM tmp_new_configs c JOIN indicator_weight p ON p.config_id = c.id AND p.indicator_code = 'L1_COMMUNITY' JOIN indicator_weight src ON src.config_id = c.model20_config_id AND src.indicator_code = 'L2_COM_DISASTER_MANAGEMENT' WHERE c.model_id = 11
UNION ALL SELECT c.id, 'L2_COMMUNITY_DISASTER_PREPAREDNESS', '社区-灾害备灾能力', 2, src.weight, p.id, 5, NOW() FROM tmp_new_configs c JOIN indicator_weight p ON p.config_id = c.id AND p.indicator_code = 'L1_COMMUNITY' JOIN indicator_weight src ON src.config_id = c.model20_config_id AND src.indicator_code = 'L2_COM_DISASTER_PREPAREDNESS' WHERE c.model_id = 11
UNION ALL SELECT c.id, 'L2_COMMUNITY_SELF_RESCUE_TRANSFER', '社区-自救转移能力', 2, src.weight, p.id, 6, NOW() FROM tmp_new_configs c JOIN indicator_weight p ON p.config_id = c.id AND p.indicator_code = 'L1_COMMUNITY' JOIN indicator_weight src ON src.config_id = c.model20_config_id AND src.indicator_code = 'L2_COM_SELF_RESCUE_TRANSFER' WHERE c.model_id = 11;

SELECT 'created_city_model_templates' AS section, model_id, COUNT(*) AS config_count FROM tmp_new_configs GROUP BY model_id ORDER BY model_id;

SELECT 'chengdu_model3_docx_check' AS section, iw.indicator_code, iw.weight FROM weight_config wc JOIN indicator_weight iw ON iw.config_id = wc.id WHERE wc.orgcode = '5101' AND wc.year = 2024 AND wc.model_id = 3 AND wc.is_deleted = 0 AND iw.indicator_code IN ('L1_DISASTER_MANAGEMENT','L2_MANAGEMENT_CAPABILITY','L2_RISK_ASSESSMENT','L2_FUNDING','L2_MATERIAL','L2_MEDICAL','L2_SELF_RESCUE','L2_PUBLIC_AVOIDANCE','L2_RELOCATION') ORDER BY iw.indicator_level, iw.sort_order;

SELECT 'chengdu_model4_docx_check' AS section, iw.indicator_code, iw.weight FROM weight_config wc JOIN indicator_weight iw ON iw.config_id = wc.id WHERE wc.orgcode = '5101' AND wc.year = 2024 AND wc.model_id = 4 AND wc.is_deleted = 0 AND iw.indicator_code IN ('L1_DISASTER_MANAGEMENT','L2_PLAN_CONSTRUCTION','L2_HAZARD_INSPECTION','L2_RISK_ASSESSMENT','L2_FUNDING','L2_MATERIAL','L2_MEDICAL','L2_SELF_RESCUE','L2_PUBLIC_AVOIDANCE','L2_RELOCATION') ORDER BY iw.indicator_level, iw.sort_order;

SELECT 'chengdu_model11_primary' AS section, iw.indicator_code, iw.weight FROM weight_config wc JOIN indicator_weight iw ON iw.config_id = wc.id WHERE wc.orgcode = '5101' AND wc.year = 2024 AND wc.model_id = 11 AND wc.is_deleted = 0 AND iw.indicator_code IN ('L1_TOWNSHIP', 'L1_COMMUNITY') ORDER BY iw.indicator_code;

DROP TABLE IF EXISTS tmp_new_configs;
DROP TABLE IF EXISTS tmp_community_l2_docx;
DROP TABLE IF EXISTS tmp_community_l1_docx;
DROP TABLE IF EXISTS tmp_township_l2_docx;
DROP TABLE IF EXISTS tmp_township_l1_docx;
DROP TABLE IF EXISTS tmp_city_weight_sources;
