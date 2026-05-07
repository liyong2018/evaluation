-- 按市州分组抽取 30% 区县。
-- 修改 @target_year 可切换年份；2025 会使用 2025 年前最新生效的区县组织机构。
-- 抽样使用固定 @sample_seed，保证重复执行结果一致；修改 seed 可重新抽样。

SET @target_year := 2025;
SET @sample_seed := 'county-sample-30pct-20260506';

DROP TEMPORARY TABLE IF EXISTS tmp_effective_counties_for_sample;
CREATE TEMPORARY TABLE tmp_effective_counties_for_sample AS
SELECT
  @target_year AS target_year,
  code AS county_code,
  name AS county_name,
  city_name
FROM (
  SELECT
    o.code,
    o.name,
    o.city_name,
    o.is_deleted,
    ROW_NUMBER() OVER (PARTITION BY o.code ORDER BY o.year DESC, o.id DESC) AS rn
  FROM organization o
  WHERE o.level = 3
    AND o.year <= @target_year
) x
WHERE rn = 1
  AND is_deleted = 0
  AND city_name IS NOT NULL
  AND city_name <> '';

DROP TEMPORARY TABLE IF EXISTS tmp_sampled_counties_30pct;
CREATE TEMPORARY TABLE tmp_sampled_counties_30pct AS
SELECT
  target_year,
  city_name,
  county_code,
  county_name,
  city_county_count,
  sample_count,
  rn AS sample_rank
FROM (
  SELECT
    c.*,
    COUNT(*) OVER (PARTITION BY c.city_name) AS city_county_count,
    CEIL(COUNT(*) OVER (PARTITION BY c.city_name) * 0.30) AS sample_count,
    ROW_NUMBER() OVER (
      PARTITION BY c.city_name
      ORDER BY SHA2(CONCAT(@sample_seed, '|', c.city_name, '|', c.county_code), 256), c.county_code
    ) AS rn
  FROM tmp_effective_counties_for_sample c
) ranked
WHERE rn <= sample_count;

-- 每个市州抽取数量汇总。
SELECT
  city_name,
  MAX(city_county_count) AS total_counties,
  MAX(sample_count) AS sampled_counties,
  ROUND(MAX(sample_count) / MAX(city_county_count) * 100, 2) AS sampled_percent
FROM tmp_sampled_counties_30pct
GROUP BY city_name
ORDER BY city_name;

-- 抽中的区县清单。
SELECT
  target_year,
  city_name,
  county_code,
  county_name,
  sample_rank
FROM tmp_sampled_counties_30pct
ORDER BY city_name, sample_rank, county_code;

-- 如果要抽取乡镇数据，使用下面这个结果集。
SELECT
  sd.*
FROM survey_data sd
JOIN tmp_sampled_counties_30pct sc
  ON sc.target_year = sd.year
 AND sc.county_code = LEFT(sd.region_code, 6)
ORDER BY sc.city_name, sc.county_code, sd.region_code;

-- 如果要抽取社区数据，使用下面这个结果集。
SELECT
  cd.*
FROM community_disaster_reduction_capacity cd
JOIN tmp_sampled_counties_30pct sc
  ON sc.target_year = cd.year
 AND sc.county_code = LEFT(cd.region_code, 6)
ORDER BY sc.city_name, sc.county_code, cd.region_code, cd.community_name;
