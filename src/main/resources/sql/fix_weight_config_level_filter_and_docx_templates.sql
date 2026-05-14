-- 修正自动创建的同名权重配置：用已从 DOCX 抽取的市级权重模板覆盖默认权重。
-- 说明：
-- 1. 列表由代码按组织机构层级筛选；这里仅修正已落库的配置数据。
-- 2. 复制范围按同一 orgcode、同一年份匹配，不绑定具体市县代码。
-- 3. 市州综合模型从 2020 市级综合权重表复制完整体系，避免默认综合模板覆盖有效数据。

DROP TEMPORARY TABLE IF EXISTS tmp_weight_template_mapping;
CREATE TEMPORARY TABLE tmp_weight_template_mapping (
    source_name VARCHAR(255) NOT NULL,
    source_model_id BIGINT NOT NULL,
    source_year INT NULL,
    target_name VARCHAR(255) NOT NULL,
    target_model_id BIGINT NOT NULL
);

INSERT INTO tmp_weight_template_mapping (source_name, source_model_id, source_year, target_name, target_model_id) VALUES
('乡镇减灾能力评估模型', 3, NULL, '区县-乡镇（街道）减灾能力评估', 3),
('社区-行政村能力评估模型', 4, NULL, '区县-社区（行政村）减灾能力（社区单元）评估', 4),
('社区-乡镇能力评估模型', 8, NULL, '区县-社区（行政村）减灾能力（乡镇单元）评估', 8),
('综合减灾能力评估模型', 11, NULL, '区县综合减灾能力评估', 11),
('社区-行政村能力评估模型', 4, NULL, '市州-社区（行政村）减灾能力（区县单元）评估', 17),
('乡镇减灾能力评估模型', 3, NULL, '市州-乡镇（街道）减灾能力（区县单元）评估', 19),
('综合减灾能力评估权重', 20, 2020, '市州综合减灾能力评估模型', 20);

DROP TEMPORARY TABLE IF EXISTS tmp_weight_config_copy;
CREATE TEMPORARY TABLE tmp_weight_config_copy AS
SELECT
    src.id AS source_config_id,
    tgt.id AS target_config_id,
    src.data_source AS source_data_source
FROM tmp_weight_template_mapping m
JOIN weight_config tgt
  ON tgt.config_name = m.target_name
 AND tgt.model_id = m.target_model_id
 AND tgt.is_deleted = 0
JOIN weight_config src
  ON src.orgcode = tgt.orgcode
 AND src.config_name = m.source_name
 AND src.model_id = m.source_model_id
 AND src.is_deleted = 0
 AND (
      (m.source_year IS NULL AND src.year = tgt.year)
      OR (m.source_year IS NOT NULL AND src.year = m.source_year)
 )
WHERE tgt.year >= 2024;

DELETE iw
FROM indicator_weight iw
JOIN tmp_weight_config_copy c ON c.target_config_id = iw.config_id
WHERE iw.indicator_level <> 1;

DELETE iw
FROM indicator_weight iw
JOIN tmp_weight_config_copy c ON c.target_config_id = iw.config_id
WHERE iw.indicator_level = 1;

INSERT INTO indicator_weight (
    config_id,
    indicator_code,
    indicator_name,
    indicator_level,
    weight,
    parent_id,
    sort_order,
    create_time
)
SELECT
    c.target_config_id,
    src_iw.indicator_code,
    src_iw.indicator_name,
    src_iw.indicator_level,
    src_iw.weight,
    NULL,
    src_iw.sort_order,
    NOW()
FROM tmp_weight_config_copy c
JOIN indicator_weight src_iw ON src_iw.config_id = c.source_config_id
WHERE src_iw.indicator_level = 1;

INSERT INTO indicator_weight (
    config_id,
    indicator_code,
    indicator_name,
    indicator_level,
    weight,
    parent_id,
    sort_order,
    create_time
)
SELECT
    c.target_config_id,
    src_iw.indicator_code,
    src_iw.indicator_name,
    src_iw.indicator_level,
    src_iw.weight,
    tgt_parent.id,
    src_iw.sort_order,
    NOW()
FROM tmp_weight_config_copy c
JOIN indicator_weight src_iw ON src_iw.config_id = c.source_config_id
LEFT JOIN indicator_weight src_parent ON src_parent.id = src_iw.parent_id
LEFT JOIN indicator_weight tgt_parent
  ON tgt_parent.config_id = c.target_config_id
 AND tgt_parent.indicator_code = src_parent.indicator_code
WHERE src_iw.indicator_level <> 1;

UPDATE weight_config tgt
JOIN tmp_weight_config_copy c ON c.target_config_id = tgt.id
SET tgt.data_source = c.source_data_source,
    tgt.update_time = NOW();

SELECT
    tgt.id AS target_config_id,
    tgt.config_name,
    tgt.orgcode,
    tgt.year,
    tgt.model_id,
    COUNT(iw.id) AS weight_count
FROM tmp_weight_config_copy c
JOIN weight_config tgt ON tgt.id = c.target_config_id
LEFT JOIN indicator_weight iw ON iw.config_id = tgt.id
GROUP BY tgt.id, tgt.config_name, tgt.orgcode, tgt.year, tgt.model_id
ORDER BY tgt.orgcode, tgt.model_id, tgt.id;
