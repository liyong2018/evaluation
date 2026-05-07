-- 修复 2025 年成都功能区业务数据归属：
-- 1. 医疗卫生机构表 medical_institution
-- 2. 社区行政村减灾能力表 community_disaster_reduction_capacity
--
-- 背景：2025 源数据里部分行仍使用武侯/双流/郫都/简阳的行政源代码，
-- 页面按功能区代码（510171/510172/510173）筛选时会漏数，或展示旧区县名称。

CREATE TEMPORARY TABLE tmp_chengdu_functional_district_mapping (
  source_code VARCHAR(16) COLLATE utf8mb4_unicode_ci PRIMARY KEY,
  target_code VARCHAR(16) COLLATE utf8mb4_unicode_ci NOT NULL,
  county_code VARCHAR(16) COLLATE utf8mb4_unicode_ci NOT NULL,
  county_name VARCHAR(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  township_name VARCHAR(64) COLLATE utf8mb4_unicode_ci NOT NULL
) ENGINE=Memory DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO tmp_chengdu_functional_district_mapping
  (source_code, target_code, county_code, county_name, township_name)
VALUES
  ('510116003', '510171701', '510171', '四川天府新区成都直管区', '华阳街道'),
  ('510116018', '510171702', '510171', '四川天府新区成都直管区', '万安街道'),
  ('510116020', '510171703', '510171', '四川天府新区成都直管区', '兴隆街道'),
  ('510116019', '510171704', '510171', '四川天府新区成都直管区', '正兴街道'),
  ('510116022', '510171705', '510171', '四川天府新区成都直管区', '新兴街道'),
  ('510116021', '510171706', '510171', '四川天府新区成都直管区', '煎茶街道'),
  ('510116025', '510171707', '510171', '四川天府新区成都直管区', '永兴街道'),
  ('510116023', '510171708', '510171', '四川天府新区成都直管区', '籍田街道'),
  ('510116024', '510171709', '510171', '四川天府新区成都直管区', '太平街道'),
  ('510107062', '510172701', '510172', '成都高新区', '肖家河街道'),
  ('510107061', '510172702', '510172', '成都高新区', '芳草街街道'),
  ('510107063', '510172703', '510172', '成都高新区', '石羊街道'),
  ('510107064', '510172704', '510172', '成都高新区', '桂溪街道'),
  ('510116004', '510172705', '510172', '成都高新区', '中和街道'),
  ('510117020', '510172706', '510172', '成都高新区', '西园街道'),
  ('510117019', '510172707', '510172', '成都高新区', '合作街道'),
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
  ('510185121', '510173714', '510173', '成都东部新区', '芦葭镇'),
  ('510173715', '510173714', '510173', '成都东部新区', '芦葭镇');

CREATE TABLE IF NOT EXISTS medical_institution_bak_2025_functional_org_repair_20260504 AS
SELECT mi.*
FROM medical_institution mi
JOIN tmp_chengdu_functional_district_mapping m
  ON LEFT(mi.org_code, 9) COLLATE utf8mb4_unicode_ci IN (m.source_code, m.target_code)
WHERE mi.year = 2025;

CREATE TABLE IF NOT EXISTS community_capacity_bak_2025_functional_org_repair_20260504 AS
SELECT c.*
FROM community_disaster_reduction_capacity c
JOIN tmp_chengdu_functional_district_mapping m
  ON LEFT(c.region_code, 9) COLLATE utf8mb4_unicode_ci IN (m.source_code, m.target_code)
WHERE c.year = 2025;

UPDATE medical_institution mi
JOIN tmp_chengdu_functional_district_mapping m
  ON LEFT(mi.org_code, 9) COLLATE utf8mb4_unicode_ci IN (m.source_code, m.target_code)
SET
  mi.org_code = CASE
    WHEN LEFT(mi.org_code, 9) COLLATE utf8mb4_unicode_ci = m.source_code THEN CONCAT(m.target_code, SUBSTRING(mi.org_code, 10))
    ELSE mi.org_code
  END,
  mi.province = '四川省',
  mi.city = '成都市',
  mi.county = m.county_name,
  mi.township = m.township_name,
  mi.update_time = NOW()
WHERE mi.year = 2025;

UPDATE community_disaster_reduction_capacity c
JOIN tmp_chengdu_functional_district_mapping m
  ON LEFT(c.region_code, 9) COLLATE utf8mb4_unicode_ci IN (m.source_code, m.target_code)
SET
  c.region_code = CASE
    WHEN LEFT(c.region_code, 9) COLLATE utf8mb4_unicode_ci = m.source_code THEN CONCAT(m.target_code, SUBSTRING(c.region_code, 10))
    ELSE c.region_code
  END,
  c.province_name = '四川省',
  c.city_name = '成都市',
  c.county_name = m.county_name,
  c.township_name = m.township_name,
  c.update_time = NOW()
WHERE c.year = 2025;

-- 验证 1：应为 0，表示旧源代码不再残留在 2025 社区/医疗业务表。
SELECT 'medical_old_source_prefix_remaining' AS item, COUNT(*) AS cnt
FROM medical_institution mi
WHERE mi.year = 2025
  AND LEFT(mi.org_code, 9) IN (
    '510116003', '510116018', '510116020', '510116019', '510116022',
    '510116021', '510116025', '510116023', '510116024', '510107062',
    '510107061', '510107063', '510107064', '510116004', '510117020',
    '510117019', '510185013', '510185009', '510185010', '510185017',
    '510185015', '510185016', '510185014', '510185012', '510185126',
    '510185127', '510185128', '510185132', '510185131', '510185121',
    '510173715'
  )
UNION ALL
SELECT 'community_old_source_prefix_remaining' AS item, COUNT(*) AS cnt
FROM community_disaster_reduction_capacity c
WHERE c.year = 2025
  AND LEFT(c.region_code, 9) IN (
    '510116003', '510116018', '510116020', '510116019', '510116022',
    '510116021', '510116025', '510116023', '510116024', '510107062',
    '510107061', '510107063', '510107064', '510116004', '510117020',
    '510117019', '510185013', '510185009', '510185010', '510185017',
    '510185015', '510185016', '510185014', '510185012', '510185126',
    '510185127', '510185128', '510185132', '510185131', '510185121',
    '510173715'
  );

-- 验证 2：功能区业务数据现在能按 510171/510172/510173 前缀查到。
SELECT LEFT(org_code, 6) AS county_code, county AS county_name, COUNT(*) AS cnt
FROM medical_institution
WHERE year = 2025 AND LEFT(org_code, 6) IN ('510171', '510172', '510173')
GROUP BY LEFT(org_code, 6), county
ORDER BY county_code, county_name;

SELECT LEFT(region_code, 6) AS county_code, county_name, COUNT(*) AS cnt
FROM community_disaster_reduction_capacity
WHERE year = 2025 AND LEFT(region_code, 6) IN ('510171', '510172', '510173')
GROUP BY LEFT(region_code, 6), county_name
ORDER BY county_code, county_name;
