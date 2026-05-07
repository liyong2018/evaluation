-- ============================================================
-- 组织机构年度增量规范化脚本
-- 目标：
-- 1. 保留现有数据并创建备份表。
-- 2. 将 2020 年组织机构规范为完整基准数据。
-- 3. 将 2024、2025 年组织机构整理为相对上一可比年份的增量数据。
-- 4. 删除 2024、2025 年中与上一可比年份完全一致的冗余组织记录。
--
-- 适用数据库：MySQL 8+
-- 执行前建议先运行：
--   src/main/resources/sql/validate_organization_incremental_2020_2024_2025.sql
--
-- 重要说明：
-- - 本脚本不会把“目标年份评估数据缺失”自动视为组织撤销。
--   缺失项会写入 org_incremental_delete_candidate_2020_2025，需人工确认后再软删除。
-- - 评估数据表 survey_data / community_disaster_reduction_capacity 不会被删除。
-- - organization 只承载省、市、县；grassroots_organization 承载乡镇、社区村。
-- ============================================================

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ============================================================
-- 0. 备份当前正式表
-- ============================================================

CREATE TABLE IF NOT EXISTS organization_backup_before_incremental_20260501 AS
SELECT * FROM organization;

CREATE TABLE IF NOT EXISTS grassroots_organization_backup_before_incremental_20260501 AS
SELECT * FROM grassroots_organization;

-- ============================================================
-- 1. 从评估数据抽取规范化组织源
-- ============================================================

DROP TEMPORARY TABLE IF EXISTS tmp_org_source_admin;
CREATE TEMPORARY TABLE tmp_org_source_admin (
    year INT NOT NULL,
    code VARCHAR(32) NOT NULL,
    name VARCHAR(128) NOT NULL,
    level TINYINT NOT NULL,
    parent_code VARCHAR(32) NULL,
    province_name VARCHAR(128) NULL,
    city_name VARCHAR(128) NULL,
    county_name VARCHAR(128) NULL,
    data_source VARCHAR(32) NOT NULL,
    PRIMARY KEY (year, code)
) ENGINE=Memory;

DROP TEMPORARY TABLE IF EXISTS tmp_org_source_grassroots;
CREATE TEMPORARY TABLE tmp_org_source_grassroots (
    year INT NOT NULL,
    code VARCHAR(32) NOT NULL,
    name VARCHAR(128) NOT NULL,
    level TINYINT NOT NULL,
    county_code VARCHAR(32) NOT NULL,
    parent_code VARCHAR(32) NULL,
    province_name VARCHAR(128) NULL,
    city_name VARCHAR(128) NULL,
    county_name VARCHAR(128) NULL,
    township_name VARCHAR(128) NULL,
    community_name VARCHAR(128) NULL,
    data_source VARCHAR(32) NOT NULL,
    PRIMARY KEY (year, code)
) ENGINE=Memory;

INSERT IGNORE INTO tmp_org_source_admin (
    year, code, name, level, parent_code,
    province_name, city_name, county_name, data_source
)
SELECT year, code, name, level, parent_code,
       province_name, city_name, county_name, data_source
FROM (
    SELECT
        sd.year,
        LEFT(TRIM(sd.region_code), 2) AS code,
        MAX(NULLIF(TRIM(sd.province), '')) AS name,
        1 AS level,
        NULL AS parent_code,
        MAX(NULLIF(TRIM(sd.province), '')) AS province_name,
        NULL AS city_name,
        NULL AS county_name,
        'SURVEY_DATA' AS data_source
    FROM survey_data sd
    WHERE sd.is_deleted = 0
      AND sd.year IN (2020, 2024, 2025)
      AND CHAR_LENGTH(TRIM(sd.region_code)) >= 2
      AND NULLIF(TRIM(sd.province), '') IS NOT NULL
    GROUP BY sd.year, LEFT(TRIM(sd.region_code), 2)

    UNION ALL

    SELECT
        sd.year,
        LEFT(TRIM(sd.region_code), 4) AS code,
        MAX(NULLIF(TRIM(sd.city), '')) AS name,
        2 AS level,
        LEFT(TRIM(sd.region_code), 2) AS parent_code,
        MAX(NULLIF(TRIM(sd.province), '')) AS province_name,
        MAX(NULLIF(TRIM(sd.city), '')) AS city_name,
        NULL AS county_name,
        'SURVEY_DATA' AS data_source
    FROM survey_data sd
    WHERE sd.is_deleted = 0
      AND sd.year IN (2020, 2024, 2025)
      AND CHAR_LENGTH(TRIM(sd.region_code)) >= 4
      AND NULLIF(TRIM(sd.city), '') IS NOT NULL
    GROUP BY sd.year, LEFT(TRIM(sd.region_code), 4), LEFT(TRIM(sd.region_code), 2)

    UNION ALL

    SELECT
        sd.year,
        LEFT(TRIM(sd.region_code), 6) AS code,
        MAX(NULLIF(TRIM(sd.county), '')) AS name,
        3 AS level,
        LEFT(TRIM(sd.region_code), 4) AS parent_code,
        MAX(NULLIF(TRIM(sd.province), '')) AS province_name,
        MAX(NULLIF(TRIM(sd.city), '')) AS city_name,
        MAX(NULLIF(TRIM(sd.county), '')) AS county_name,
        'SURVEY_DATA' AS data_source
    FROM survey_data sd
    WHERE sd.is_deleted = 0
      AND sd.year IN (2020, 2024, 2025)
      AND CHAR_LENGTH(TRIM(sd.region_code)) >= 6
      AND NULLIF(TRIM(sd.county), '') IS NOT NULL
    GROUP BY sd.year, LEFT(TRIM(sd.region_code), 6), LEFT(TRIM(sd.region_code), 4)

    UNION ALL

    SELECT
        cd.year,
        LEFT(TRIM(cd.region_code), 2) AS code,
        MAX(NULLIF(TRIM(cd.province_name), '')) AS name,
        1 AS level,
        NULL AS parent_code,
        MAX(NULLIF(TRIM(cd.province_name), '')) AS province_name,
        NULL AS city_name,
        NULL AS county_name,
        'COMMUNITY_DATA' AS data_source
    FROM community_disaster_reduction_capacity cd
    WHERE cd.year IN (2020, 2024, 2025)
      AND CHAR_LENGTH(TRIM(cd.region_code)) >= 2
      AND NULLIF(TRIM(cd.province_name), '') IS NOT NULL
    GROUP BY cd.year, LEFT(TRIM(cd.region_code), 2)

    UNION ALL

    SELECT
        cd.year,
        LEFT(TRIM(cd.region_code), 4) AS code,
        MAX(NULLIF(TRIM(cd.city_name), '')) AS name,
        2 AS level,
        LEFT(TRIM(cd.region_code), 2) AS parent_code,
        MAX(NULLIF(TRIM(cd.province_name), '')) AS province_name,
        MAX(NULLIF(TRIM(cd.city_name), '')) AS city_name,
        NULL AS county_name,
        'COMMUNITY_DATA' AS data_source
    FROM community_disaster_reduction_capacity cd
    WHERE cd.year IN (2020, 2024, 2025)
      AND CHAR_LENGTH(TRIM(cd.region_code)) >= 4
      AND NULLIF(TRIM(cd.city_name), '') IS NOT NULL
    GROUP BY cd.year, LEFT(TRIM(cd.region_code), 4), LEFT(TRIM(cd.region_code), 2)

    UNION ALL

    SELECT
        cd.year,
        LEFT(TRIM(cd.region_code), 6) AS code,
        MAX(NULLIF(TRIM(cd.county_name), '')) AS name,
        3 AS level,
        LEFT(TRIM(cd.region_code), 4) AS parent_code,
        MAX(NULLIF(TRIM(cd.province_name), '')) AS province_name,
        MAX(NULLIF(TRIM(cd.city_name), '')) AS city_name,
        MAX(NULLIF(TRIM(cd.county_name), '')) AS county_name,
        'COMMUNITY_DATA' AS data_source
    FROM community_disaster_reduction_capacity cd
    WHERE cd.year IN (2020, 2024, 2025)
      AND CHAR_LENGTH(TRIM(cd.region_code)) >= 6
      AND NULLIF(TRIM(cd.county_name), '') IS NOT NULL
    GROUP BY cd.year, LEFT(TRIM(cd.region_code), 6), LEFT(TRIM(cd.region_code), 4)
) src
WHERE src.name IS NOT NULL;

INSERT IGNORE INTO tmp_org_source_grassroots (
    year, code, name, level, county_code, parent_code,
    province_name, city_name, county_name, township_name, community_name, data_source
)
SELECT
    sd.year,
    LEFT(TRIM(sd.region_code), 9) AS code,
    MAX(NULLIF(TRIM(sd.township), '')) AS name,
    4 AS level,
    LEFT(TRIM(sd.region_code), 6) AS county_code,
    LEFT(TRIM(sd.region_code), 6) AS parent_code,
    MAX(NULLIF(TRIM(sd.province), '')) AS province_name,
    MAX(NULLIF(TRIM(sd.city), '')) AS city_name,
    MAX(NULLIF(TRIM(sd.county), '')) AS county_name,
    MAX(NULLIF(TRIM(sd.township), '')) AS township_name,
    NULL AS community_name,
    'SURVEY_DATA' AS data_source
FROM survey_data sd
WHERE sd.is_deleted = 0
  AND sd.year IN (2020, 2024, 2025)
  AND CHAR_LENGTH(TRIM(sd.region_code)) >= 9
  AND NULLIF(TRIM(sd.township), '') IS NOT NULL
GROUP BY sd.year, LEFT(TRIM(sd.region_code), 9), LEFT(TRIM(sd.region_code), 6)

UNION ALL

SELECT
    cd.year,
    LEFT(TRIM(cd.region_code), 9) AS code,
    MAX(NULLIF(TRIM(cd.township_name), '')) AS name,
    4 AS level,
    LEFT(TRIM(cd.region_code), 6) AS county_code,
    LEFT(TRIM(cd.region_code), 6) AS parent_code,
    MAX(NULLIF(TRIM(cd.province_name), '')) AS province_name,
    MAX(NULLIF(TRIM(cd.city_name), '')) AS city_name,
    MAX(NULLIF(TRIM(cd.county_name), '')) AS county_name,
    MAX(NULLIF(TRIM(cd.township_name), '')) AS township_name,
    NULL AS community_name,
    'COMMUNITY_DATA' AS data_source
FROM community_disaster_reduction_capacity cd
WHERE cd.year IN (2020, 2024, 2025)
  AND CHAR_LENGTH(TRIM(cd.region_code)) >= 9
  AND NULLIF(TRIM(cd.township_name), '') IS NOT NULL
GROUP BY cd.year, LEFT(TRIM(cd.region_code), 9), LEFT(TRIM(cd.region_code), 6)

UNION ALL

SELECT
    cd.year,
    TRIM(cd.region_code) AS code,
    MAX(NULLIF(TRIM(cd.community_name), '')) AS name,
    5 AS level,
    LEFT(TRIM(cd.region_code), 6) AS county_code,
    LEFT(TRIM(cd.region_code), 9) AS parent_code,
    MAX(NULLIF(TRIM(cd.province_name), '')) AS province_name,
    MAX(NULLIF(TRIM(cd.city_name), '')) AS city_name,
    MAX(NULLIF(TRIM(cd.county_name), '')) AS county_name,
    MAX(NULLIF(TRIM(cd.township_name), '')) AS township_name,
    MAX(NULLIF(TRIM(cd.community_name), '')) AS community_name,
    'COMMUNITY_DATA' AS data_source
FROM community_disaster_reduction_capacity cd
WHERE cd.year IN (2020, 2024, 2025)
  AND CHAR_LENGTH(TRIM(cd.region_code)) >= 12
  AND NULLIF(TRIM(cd.community_name), '') IS NOT NULL
GROUP BY cd.year, TRIM(cd.region_code), LEFT(TRIM(cd.region_code), 6), LEFT(TRIM(cd.region_code), 9);

-- 部分年度社区表的 region_code 只保存到乡镇级（9位），社区编码需要通过
-- “乡镇编码 + 社区名称”回查既有 2020/2024 社区组织。只有唯一匹配时才纳入增量。
INSERT IGNORE INTO tmp_org_source_grassroots (
    year, code, name, level, county_code, parent_code,
    province_name, city_name, county_name, township_name, community_name, data_source
)
SELECT
    cd.year,
    resolved.resolved_code AS code,
    MAX(NULLIF(TRIM(cd.community_name), '')) AS name,
    5 AS level,
    LEFT(TRIM(cd.region_code), 6) AS county_code,
    LEFT(TRIM(cd.region_code), 9) AS parent_code,
    MAX(NULLIF(TRIM(cd.province_name), '')) AS province_name,
    MAX(NULLIF(TRIM(cd.city_name), '')) AS city_name,
    MAX(NULLIF(TRIM(cd.county_name), '')) AS county_name,
    MAX(NULLIF(TRIM(cd.township_name), '')) AS township_name,
    MAX(NULLIF(TRIM(cd.community_name), '')) AS community_name,
    'COMMUNITY_DATA_RESOLVED' AS data_source
FROM community_disaster_reduction_capacity cd
JOIN (
    SELECT
        LEFT(code, 9) AS township_code,
        name AS community_name,
        MIN(code) AS resolved_code
    FROM grassroots_organization
    WHERE level = 5
      AND is_deleted = 0
      AND (year IN (2020, 2024) OR is_baseline = 1)
    GROUP BY LEFT(code, 9), name
    HAVING COUNT(DISTINCT code) = 1
) resolved
  ON resolved.township_code = LEFT(TRIM(cd.region_code), 9)
 AND resolved.community_name = TRIM(cd.community_name)
WHERE cd.year IN (2024, 2025)
  AND CHAR_LENGTH(TRIM(cd.region_code)) = 9
  AND NULLIF(TRIM(cd.community_name), '') IS NOT NULL
GROUP BY cd.year, resolved.resolved_code, LEFT(TRIM(cd.region_code), 6), LEFT(TRIM(cd.region_code), 9);

-- ============================================================
-- 2. 2020 基准数据写入/修正
-- ============================================================

INSERT INTO organization (
    parent_id, code, name, level, year, data_source,
    province_name, city_name, county_name,
    is_baseline, baseline_code, is_deleted, create_time, update_time
)
SELECT
    p.id AS parent_id,
    s.code,
    s.name,
    s.level,
    2020,
    s.data_source,
    s.province_name,
    s.city_name,
    s.county_name,
    1,
    s.code,
    0,
    NOW(),
    NOW()
FROM tmp_org_source_admin s
LEFT JOIN organization p
       ON p.code = s.parent_code
      AND p.year = 2020
      AND p.is_deleted = 0
WHERE s.year = 2020
ON DUPLICATE KEY UPDATE
    name = VALUES(name),
    level = VALUES(level),
    parent_id = VALUES(parent_id),
    data_source = VALUES(data_source),
    province_name = VALUES(province_name),
    city_name = VALUES(city_name),
    county_name = VALUES(county_name),
    is_baseline = 1,
    baseline_code = VALUES(baseline_code),
    is_deleted = 0,
    update_time = NOW();

INSERT INTO grassroots_organization (
    county_id, parent_id, code, name, level, year, data_source,
    province_name, city_name, county_name, township_name, community_name,
    is_baseline, baseline_code, is_deleted, create_time, update_time
)
SELECT
    county.id AS county_id,
    CASE WHEN s.level = 4 THEN NULL ELSE township.id END AS parent_id,
    s.code,
    s.name,
    s.level,
    2020,
    s.data_source,
    s.province_name,
    s.city_name,
    s.county_name,
    s.township_name,
    s.community_name,
    1,
    s.code,
    0,
    NOW(),
    NOW()
FROM tmp_org_source_grassroots s
JOIN organization county
  ON county.code = s.county_code
 AND county.year = 2020
 AND county.is_deleted = 0
LEFT JOIN grassroots_organization township
  ON township.code = s.parent_code
 AND township.year = 2020
 AND township.is_deleted = 0
WHERE s.year = 2020
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
    is_baseline = 1,
    baseline_code = VALUES(baseline_code),
    is_deleted = 0,
    update_time = NOW();

-- ============================================================
-- 3. 标记现有 2020 记录为基准，并修正基准层级关系
-- ============================================================

UPDATE organization
SET is_baseline = 1,
    baseline_code = code,
    is_deleted = 0,
    update_time = NOW()
WHERE year = 2020;

UPDATE grassroots_organization
SET is_baseline = 1,
    baseline_code = code,
    is_deleted = 0,
    update_time = NOW()
WHERE year = 2020;

UPDATE organization o
JOIN tmp_org_source_admin s
  ON s.code = o.code
 AND s.year = o.year
LEFT JOIN organization p
  ON p.code = s.parent_code
 AND p.year = 2020
 AND p.is_deleted = 0
SET o.parent_id = p.id,
    o.update_time = NOW()
WHERE o.year = 2020
  AND o.level IN (2, 3);

UPDATE grassroots_organization g
JOIN tmp_org_source_grassroots s
  ON s.code = g.code
 AND s.year = g.year
JOIN organization county
  ON county.code = s.county_code
 AND county.year = 2020
 AND county.is_deleted = 0
LEFT JOIN grassroots_organization township
  ON township.code = s.parent_code
 AND township.year = 2020
 AND township.is_deleted = 0
SET g.county_id = county.id,
    g.parent_id = CASE WHEN g.level = 4 THEN NULL ELSE township.id END,
    g.update_time = NOW()
WHERE g.year = 2020;

-- ============================================================
-- 4. 生成并应用 2024/2025 年度增量
-- ============================================================

DROP TEMPORARY TABLE IF EXISTS tmp_admin_delta;
CREATE TEMPORARY TABLE tmp_admin_delta AS
SELECT
    cur.*,
    prev.year AS previous_year
FROM tmp_org_source_admin cur
LEFT JOIN tmp_org_source_admin prev
       ON prev.code = cur.code
      AND prev.year = (
          SELECT MAX(p2.year)
          FROM tmp_org_source_admin p2
          WHERE p2.code = cur.code
            AND p2.year < cur.year
      )
WHERE cur.year IN (2024, 2025)
  AND (
      prev.code IS NULL
      OR COALESCE(cur.name, '') <> COALESCE(prev.name, '')
      OR COALESCE(cur.level, 0) <> COALESCE(prev.level, 0)
      OR COALESCE(cur.parent_code, '') <> COALESCE(prev.parent_code, '')
      OR COALESCE(cur.province_name, '') <> COALESCE(prev.province_name, '')
      OR COALESCE(cur.city_name, '') <> COALESCE(prev.city_name, '')
      OR COALESCE(cur.county_name, '') <> COALESCE(prev.county_name, '')
  );

DROP TEMPORARY TABLE IF EXISTS tmp_grassroots_delta;
CREATE TEMPORARY TABLE tmp_grassroots_delta AS
SELECT
    cur.*,
    prev.year AS previous_year
FROM tmp_org_source_grassroots cur
LEFT JOIN tmp_org_source_grassroots prev
       ON prev.code = cur.code
      AND prev.year = (
          SELECT MAX(p2.year)
          FROM tmp_org_source_grassroots p2
          WHERE p2.code = cur.code
            AND p2.year < cur.year
      )
WHERE cur.year IN (2024, 2025)
  AND (
      prev.code IS NULL
      OR COALESCE(cur.name, '') <> COALESCE(prev.name, '')
      OR COALESCE(cur.level, 0) <> COALESCE(prev.level, 0)
      OR COALESCE(cur.county_code, '') <> COALESCE(prev.county_code, '')
      OR COALESCE(cur.parent_code, '') <> COALESCE(prev.parent_code, '')
      OR COALESCE(cur.province_name, '') <> COALESCE(prev.province_name, '')
      OR COALESCE(cur.city_name, '') <> COALESCE(prev.city_name, '')
      OR COALESCE(cur.county_name, '') <> COALESCE(prev.county_name, '')
      OR COALESCE(cur.township_name, '') <> COALESCE(prev.township_name, '')
      OR COALESCE(cur.community_name, '') <> COALESCE(prev.community_name, '')
  );

INSERT INTO organization (
    parent_id, code, name, level, year, data_source,
    province_name, city_name, county_name,
    is_baseline, baseline_code, is_deleted, create_time, update_time
)
SELECT
    p.id AS parent_id,
    d.code,
    d.name,
    d.level,
    d.year,
    d.data_source,
    d.province_name,
    d.city_name,
    d.county_name,
    0,
    d.code,
    0,
    NOW(),
    NOW()
FROM tmp_admin_delta d
LEFT JOIN organization p
       ON p.code = d.parent_code
      AND p.year = 2020
      AND p.is_deleted = 0
ON DUPLICATE KEY UPDATE
    name = VALUES(name),
    level = VALUES(level),
    parent_id = VALUES(parent_id),
    data_source = VALUES(data_source),
    province_name = VALUES(province_name),
    city_name = VALUES(city_name),
    county_name = VALUES(county_name),
    is_baseline = 0,
    baseline_code = VALUES(baseline_code),
    is_deleted = 0,
    update_time = NOW();

INSERT INTO grassroots_organization (
    county_id, parent_id, code, name, level, year, data_source,
    province_name, city_name, county_name, township_name, community_name,
    is_baseline, baseline_code, is_deleted, create_time, update_time
)
SELECT
    county.id AS county_id,
    CASE WHEN d.level = 4 THEN NULL ELSE township.id END AS parent_id,
    d.code,
    d.name,
    d.level,
    d.year,
    d.data_source,
    d.province_name,
    d.city_name,
    d.county_name,
    d.township_name,
    d.community_name,
    0,
    d.code,
    0,
    NOW(),
    NOW()
FROM tmp_grassroots_delta d
JOIN organization county
  ON county.code = d.county_code
 AND county.year = 2020
 AND county.is_deleted = 0
LEFT JOIN grassroots_organization township
  ON township.code = d.parent_code
 AND township.year = 2020
 AND township.is_deleted = 0
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
    is_baseline = 0,
    baseline_code = VALUES(baseline_code),
    is_deleted = 0,
    update_time = NOW();

-- 修正年度增量层级关系。年度记录只保存增量，父级优先指向同年增量；
-- 若同年没有父级增量，则回退指向 2020 基准父级。
UPDATE organization o
JOIN tmp_admin_delta d
  ON d.code = o.code
 AND d.year = o.year
LEFT JOIN organization p_year
  ON p_year.code = d.parent_code
 AND p_year.year = d.year
 AND p_year.is_deleted = 0
LEFT JOIN organization p_base
  ON p_base.code = d.parent_code
 AND p_base.year = 2020
 AND p_base.is_deleted = 0
SET o.parent_id = COALESCE(p_year.id, p_base.id),
    o.update_time = NOW()
WHERE o.year IN (2024, 2025)
  AND o.level IN (2, 3);

UPDATE grassroots_organization g
JOIN tmp_grassroots_delta d
  ON d.code = g.code
 AND d.year = g.year
JOIN organization county
  ON county.code = d.county_code
 AND county.year = 2020
 AND county.is_deleted = 0
LEFT JOIN grassroots_organization township_year
  ON township_year.code = d.parent_code
 AND township_year.year = d.year
 AND township_year.is_deleted = 0
LEFT JOIN grassroots_organization township_base
  ON township_base.code = d.parent_code
 AND township_base.year = 2020
 AND township_base.is_deleted = 0
SET g.county_id = county.id,
    g.parent_id = CASE WHEN g.level = 4 THEN NULL ELSE COALESCE(township_year.id, township_base.id) END,
    g.update_time = NOW()
WHERE g.year IN (2024, 2025);

-- ============================================================
-- 5. 清理已存在的年度冗余记录
--    仅清理能够从评估源数据证明“与上一可比年份完全一致”的 2024/2025 记录。
-- ============================================================

DELETE o
FROM organization o
JOIN tmp_org_source_admin cur
  ON cur.code = o.code
 AND cur.year = o.year
JOIN tmp_org_source_admin prev
  ON prev.code = cur.code
 AND prev.year = (
     SELECT MAX(p2.year)
     FROM tmp_org_source_admin p2
     WHERE p2.code = cur.code
       AND p2.year < cur.year
 )
WHERE o.year IN (2024, 2025)
  AND COALESCE(o.is_baseline, 0) = 0
  AND COALESCE(cur.name, '') = COALESCE(prev.name, '')
  AND COALESCE(cur.level, 0) = COALESCE(prev.level, 0)
  AND COALESCE(cur.parent_code, '') = COALESCE(prev.parent_code, '')
  AND COALESCE(cur.province_name, '') = COALESCE(prev.province_name, '')
  AND COALESCE(cur.city_name, '') = COALESCE(prev.city_name, '')
  AND COALESCE(cur.county_name, '') = COALESCE(prev.county_name, '');

DELETE g
FROM grassroots_organization g
JOIN tmp_org_source_grassroots cur
  ON cur.code = g.code
 AND cur.year = g.year
JOIN tmp_org_source_grassroots prev
  ON prev.code = cur.code
 AND prev.year = (
     SELECT MAX(p2.year)
     FROM tmp_org_source_grassroots p2
     WHERE p2.code = cur.code
       AND p2.year < cur.year
 )
WHERE g.year IN (2024, 2025)
  AND COALESCE(g.is_baseline, 0) = 0
  AND COALESCE(cur.name, '') = COALESCE(prev.name, '')
  AND COALESCE(cur.level, 0) = COALESCE(prev.level, 0)
  AND COALESCE(cur.county_code, '') = COALESCE(prev.county_code, '')
  AND COALESCE(cur.parent_code, '') = COALESCE(prev.parent_code, '')
  AND COALESCE(cur.province_name, '') = COALESCE(prev.province_name, '')
  AND COALESCE(cur.city_name, '') = COALESCE(prev.city_name, '')
  AND COALESCE(cur.county_name, '') = COALESCE(prev.county_name, '')
  AND COALESCE(cur.township_name, '') = COALESCE(prev.township_name, '')
  AND COALESCE(cur.community_name, '') = COALESCE(prev.community_name, '');

-- ============================================================
-- 6. 输出疑似撤销候选，不自动软删除
-- ============================================================

DROP TABLE IF EXISTS org_incremental_delete_candidate_2020_2025;
CREATE TABLE org_incremental_delete_candidate_2020_2025 AS
SELECT
    'ADMIN' AS org_table,
    prev.year AS previous_year,
    next_year.year AS target_year,
    prev.code,
    prev.name,
    prev.level,
    prev.parent_code
FROM tmp_org_source_admin prev
JOIN (
    SELECT 2024 AS year
    UNION ALL
    SELECT 2025 AS year
) next_year
  ON next_year.year > prev.year
 AND next_year.year = (
     SELECT MIN(y2.year)
     FROM (
         SELECT 2024 AS year
         UNION ALL
         SELECT 2025 AS year
     ) y2
     WHERE y2.year > prev.year
 )
LEFT JOIN tmp_org_source_admin cur
  ON cur.code = prev.code
 AND cur.year = next_year.year
WHERE prev.year IN (2020, 2024)
  AND cur.code IS NULL

UNION ALL

SELECT
    'GRASSROOTS' AS org_table,
    prev.year AS previous_year,
    next_year.year AS target_year,
    prev.code,
    prev.name,
    prev.level,
    prev.parent_code
FROM tmp_org_source_grassroots prev
JOIN (
    SELECT 2024 AS year
    UNION ALL
    SELECT 2025 AS year
) next_year
  ON next_year.year > prev.year
 AND next_year.year = (
     SELECT MIN(y2.year)
     FROM (
         SELECT 2024 AS year
         UNION ALL
         SELECT 2025 AS year
     ) y2
     WHERE y2.year > prev.year
 )
LEFT JOIN tmp_org_source_grassroots cur
  ON cur.code = prev.code
 AND cur.year = next_year.year
WHERE prev.year IN (2020, 2024)
  AND cur.code IS NULL;

DROP TABLE IF EXISTS org_community_code_resolution_issue_2025;
CREATE TABLE org_community_code_resolution_issue_2025 AS
SELECT
    cd.year,
    TRIM(cd.region_code) AS township_code,
    TRIM(cd.community_name) AS community_name,
    COUNT(DISTINCT g.code) AS match_count,
    MIN(g.code) AS resolved_code,
    CASE
        WHEN COUNT(DISTINCT g.code) = 0 THEN 'NO_MATCH'
        WHEN COUNT(DISTINCT g.code) = 1 THEN 'RESOLVED'
        ELSE 'AMBIGUOUS'
    END AS match_status
FROM community_disaster_reduction_capacity cd
LEFT JOIN grassroots_organization g
  ON g.level = 5
 AND g.is_deleted = 0
 AND (g.year IN (2020, 2024) OR g.is_baseline = 1)
 AND LEFT(g.code, 9) = LEFT(TRIM(cd.region_code), 9)
 AND g.name = TRIM(cd.community_name)
WHERE cd.year = 2025
  AND CHAR_LENGTH(TRIM(cd.region_code)) = 9
  AND NULLIF(TRIM(cd.community_name), '') IS NOT NULL
GROUP BY cd.year, TRIM(cd.region_code), TRIM(cd.community_name);

-- ============================================================
-- 7. 汇总结果
-- ============================================================

SELECT 'organization_backup_before_incremental_20260501' AS table_name, COUNT(*) AS row_count
FROM organization_backup_before_incremental_20260501
UNION ALL
SELECT 'grassroots_organization_backup_before_incremental_20260501', COUNT(*)
FROM grassroots_organization_backup_before_incremental_20260501
UNION ALL
SELECT 'organization_current', COUNT(*)
FROM organization
UNION ALL
SELECT 'grassroots_organization_current', COUNT(*)
FROM grassroots_organization
UNION ALL
SELECT 'delete_candidates_need_manual_review', COUNT(*)
FROM org_incremental_delete_candidate_2020_2025
UNION ALL
SELECT 'community_code_resolution_issue_2025_total', COUNT(*)
FROM org_community_code_resolution_issue_2025
UNION ALL
SELECT 'community_code_resolution_issue_2025_unresolved', COUNT(*)
FROM org_community_code_resolution_issue_2025
WHERE match_status <> 'RESOLVED';

SELECT year, is_baseline, COUNT(*) AS row_count
FROM organization
GROUP BY year, is_baseline
ORDER BY year, is_baseline;

SELECT year, is_baseline, COUNT(*) AS row_count
FROM grassroots_organization
GROUP BY year, is_baseline
ORDER BY year, is_baseline;

SET FOREIGN_KEY_CHECKS = 1;
