-- 修复成都功能区 2020 基准基层组织层级。
-- 目标：
-- 1. 把原挂在双流/武侯/郫都/简阳下的天府新区、高新区、东部新区街镇村社迁到功能区。
-- 2. 隐藏旧行政区下的功能区残留，避免 2020 页面重复展示。
-- 3. 保留备份表，脚本可重复执行。

SET NAMES utf8mb4;

SET @suffix = DATE_FORMAT(NOW(), '%Y%m%d_%H%i%s');
SET @backup_sql = CONCAT('CREATE TABLE IF NOT EXISTS grassroots_functional_2020_bak_', @suffix, ' AS SELECT * FROM grassroots_organization');
PREPARE stmt FROM @backup_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

DROP TEMPORARY TABLE IF EXISTS tmp_functional_township_mapping;
CREATE TEMPORARY TABLE tmp_functional_township_mapping (
    old_code VARCHAR(32) NOT NULL,
    new_code VARCHAR(32) NOT NULL,
    county_code VARCHAR(32) NOT NULL,
    county_name VARCHAR(128) NOT NULL,
    township_name VARCHAR(128) NOT NULL,
    PRIMARY KEY (old_code, new_code)
);

INSERT INTO tmp_functional_township_mapping (old_code, new_code, county_code, county_name, township_name) VALUES
-- 四川天府新区成都直管区：原双流区成都直管区街道
('510116003', '510171701', '510171', '四川天府新区成都直管区', '华阳街道'),
('510116018', '510171702', '510171', '四川天府新区成都直管区', '万安街道'),
('510116020', '510171703', '510171', '四川天府新区成都直管区', '兴隆街道'),
('510116019', '510171704', '510171', '四川天府新区成都直管区', '正兴街道'),
('510116022', '510171705', '510171', '四川天府新区成都直管区', '新兴街道'),
('510116021', '510171706', '510171', '四川天府新区成都直管区', '煎茶街道'),
('510116025', '510171707', '510171', '四川天府新区成都直管区', '永兴街道'),
('510116023', '510171708', '510171', '四川天府新区成都直管区', '籍田街道'),
('510116024', '510171709', '510171', '四川天府新区成都直管区', '太平街道'),

-- 成都高新区：原武侯/双流/郫都部分街道
('510107062', '510172701', '510172', '成都高新区', '肖家河街道'),
('510107061', '510172702', '510172', '成都高新区', '芳草街街道'),
('510107063', '510172703', '510172', '成都高新区', '石羊街道'),
('510107064', '510172704', '510172', '成都高新区', '桂溪街道'),
('510116004', '510172705', '510172', '成都高新区', '中和街道'),
('510117020', '510172706', '510172', '成都高新区', '西园街道'),
('510117019', '510172707', '510172', '成都高新区', '合作街道'),

-- 成都东部新区：原简阳下的东部新区街镇，迁到 5101737xx 逻辑代码
('510185013', '510173701', '510173', '成都东部新区', '三岔街道'),
('510185009', '510173702', '510173', '成都东部新区', '石盘街道'),
('510185010', '510173703', '510173', '成都东部新区', '养马街道'),
('510185017', '510173704', '510173', '成都东部新区', '丹景街道'),
('510185015', '510173705', '510173', '成都东部新区', '福田街道'),
('510185016', '510173706', '510173', '成都东部新区', '玉成街道'),
('510185014', '510173707', '510173', '成都东部新区', '草池街道'),
('510185012', '510173708', '510173', '成都东部新区', '石板凳街道'),
('510185126', '510173709', '510173', '成都东部新区', '高明镇'),
('510185127', '510173710', '510173', '成都东部新区', '武庙镇'),
('510185128', '510173711', '510173', '成都东部新区', '壮溪镇'),
('510185132', '510173712', '510173', '成都东部新区', '海螺镇'),
('510185131', '510173713', '510173', '成都东部新区', '董家埂镇'),
('510185121', '510173714', '510173', '成都东部新区', '芦葭镇');

-- 东部新区当前库里还存在一批 5101730xx/5101731xx 旧码空节点，也需要隐藏。
DROP TEMPORARY TABLE IF EXISTS tmp_functional_old_prefixes;
CREATE TEMPORARY TABLE tmp_functional_old_prefixes (
    old_code VARCHAR(32) PRIMARY KEY
);

INSERT INTO tmp_functional_old_prefixes (old_code)
SELECT old_code FROM tmp_functional_township_mapping
UNION SELECT '510173009'
UNION SELECT '510173010'
UNION SELECT '510173011'
UNION SELECT '510173012'
UNION SELECT '510173013'
UNION SELECT '510173014'
UNION SELECT '510173015'
UNION SELECT '510173016'
UNION SELECT '510173017'
UNION SELECT '510173121'
UNION SELECT '510173126'
UNION SELECT '510173127'
UNION SELECT '510173128'
UNION SELECT '510173131'
UNION SELECT '510173132'
UNION SELECT '510171018'
UNION SELECT '510171019'
UNION SELECT '510171020'
UNION SELECT '510171021'
UNION SELECT '510171022'
UNION SELECT '510171023'
UNION SELECT '510171024'
UNION SELECT '510171025';

-- 先迁移/修正 2020 街镇基准节点。
INSERT INTO grassroots_organization (
    county_id, parent_id, code, name, level, year, data_source,
    province_name, city_name, county_name, township_name, community_name,
    create_time, update_time, is_deleted, is_baseline, baseline_code
)
SELECT
    county.id,
    county.id,
    mapping.new_code,
    mapping.township_name,
    4,
    2020,
    'MIGRATED_FUNCTIONAL_DISTRICT',
    '四川省',
    '成都市',
    mapping.county_name,
    mapping.township_name,
    NULL,
    NOW(),
    NOW(),
    0,
    1,
    mapping.old_code
FROM tmp_functional_township_mapping mapping
JOIN organization county
  ON county.code = mapping.county_code
 AND county.level = 3
 AND county.is_baseline = 1
WHERE EXISTS (
    SELECT 1
    FROM grassroots_organization source
    WHERE source.code = mapping.old_code
      AND source.year = 2020
      AND source.is_baseline = 1
)
ON DUPLICATE KEY UPDATE
    county_id = VALUES(county_id),
    parent_id = VALUES(parent_id),
    name = VALUES(name),
    level = VALUES(level),
    data_source = VALUES(data_source),
    province_name = VALUES(province_name),
    city_name = VALUES(city_name),
    county_name = VALUES(county_name),
    township_name = VALUES(township_name),
    community_name = VALUES(community_name),
    is_deleted = 0,
    is_baseline = 1,
    baseline_code = VALUES(baseline_code),
    update_time = NOW();

-- 再迁移/修正 2020 村社基准节点，村社代码使用新街镇代码 + 原后三位。
INSERT INTO grassroots_organization (
    county_id, parent_id, code, name, level, year, data_source,
    province_name, city_name, county_name, township_name, community_name,
    create_time, update_time, is_deleted, is_baseline, baseline_code
)
SELECT
    county.id,
    target_township.id,
    CONCAT(mapping.new_code, RIGHT(source.code, 3)),
    source.name,
    5,
    2020,
    'MIGRATED_FUNCTIONAL_DISTRICT',
    '四川省',
    '成都市',
    mapping.county_name,
    mapping.township_name,
    COALESCE(NULLIF(source.community_name, ''), source.name),
    NOW(),
    NOW(),
    0,
    1,
    source.code
FROM tmp_functional_township_mapping mapping
JOIN organization county
  ON county.code = mapping.county_code
 AND county.level = 3
 AND county.is_baseline = 1
JOIN grassroots_organization target_township
  ON target_township.code = mapping.new_code
 AND target_township.year = 2020
 AND target_township.is_baseline = 1
JOIN grassroots_organization source
  ON source.code LIKE CONCAT(mapping.old_code, '___')
 AND source.level = 5
 AND source.year = 2020
 AND source.is_baseline = 1
WHERE source.is_deleted = 0
ON DUPLICATE KEY UPDATE
    county_id = VALUES(county_id),
    parent_id = VALUES(parent_id),
    name = VALUES(name),
    level = VALUES(level),
    data_source = VALUES(data_source),
    province_name = VALUES(province_name),
    city_name = VALUES(city_name),
    county_name = VALUES(county_name),
    township_name = VALUES(township_name),
    community_name = VALUES(community_name),
    is_deleted = 0,
    is_baseline = 1,
    baseline_code = VALUES(baseline_code),
    update_time = NOW();

-- 隐藏旧行政区下的功能区残留。服务层会跳过已删除基准记录。
UPDATE grassroots_organization target
JOIN tmp_functional_old_prefixes old_prefix
  ON target.code = old_prefix.old_code
  OR target.code LIKE CONCAT(old_prefix.old_code, '___')
SET target.is_deleted = 1,
    target.update_time = NOW()
WHERE target.year = 2020
  AND target.is_baseline = 1;

-- 让已迁出的功能区县节点保持可用。
UPDATE organization
SET is_deleted = 0,
    update_time = NOW()
WHERE code IN ('510171', '510172', '510173')
  AND level = 3
  AND is_baseline = 1;

-- 汇总校验。
SELECT 'functional_2020_active_townships' AS metric, county_name, COUNT(*) AS cnt
FROM grassroots_organization
WHERE year = 2020
  AND is_baseline = 1
  AND is_deleted = 0
  AND level = 4
  AND county_name IN ('四川天府新区成都直管区', '成都高新区', '成都东部新区')
GROUP BY county_name
ORDER BY county_name;

SELECT 'functional_2020_active_communities' AS metric, county_name, COUNT(*) AS cnt
FROM grassroots_organization
WHERE year = 2020
  AND is_baseline = 1
  AND is_deleted = 0
  AND level = 5
  AND county_name IN ('四川天府新区成都直管区', '成都高新区', '成都东部新区')
GROUP BY county_name
ORDER BY county_name;

SELECT 'old_functional_residue_active' AS metric, COUNT(*) AS cnt
FROM grassroots_organization target
JOIN tmp_functional_old_prefixes old_prefix
  ON target.code = old_prefix.old_code
  OR target.code LIKE CONCAT(old_prefix.old_code, '___')
WHERE target.year = 2020
  AND target.is_baseline = 1
  AND target.is_deleted = 0;
