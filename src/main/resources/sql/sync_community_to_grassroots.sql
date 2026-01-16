-- ============================================================
-- 将社区减灾能力表的数据同步到grassroots_organization表
-- ============================================================

INSERT IGNORE INTO grassroots_organization (
    code, name, level, year, data_source, county_id, parent_id,
    province_name, city_name, county_name, township_name, community_name,
    is_baseline, is_deleted, create_time, update_time
)
SELECT
    c.region_code as code,
    c.community_name as name,
    5 as level,
    c.year as year,
    'COMMUNITY_DATA' as data_source,
    t.county_id as county_id,
    t.id as parent_id,
    c.province_name,
    c.city_name,
    c.county_name,
    COALESCE(c.township_name, t.name) as township_name,
    c.community_name,
    0 as is_baseline,
    0 as is_deleted,
    NOW() as create_time,
    NOW() as update_time
FROM community_disaster_reduction_capacity c
INNER JOIN grassroots_organization t
    ON SUBSTRING(c.region_code, 1, 9) = t.code
    AND c.county_name = t.county_name
    AND t.year = c.year
    AND t.level = 4
WHERE c.year = 2025;

-- 验证结果
SELECT
    t.name as township_name,
    COUNT(g.id) as community_count
FROM grassroots_organization t
LEFT JOIN grassroots_organization g ON g.parent_id = t.id AND g.year = t.year AND g.level = 5
WHERE t.year = 2025 AND t.level = 4
GROUP BY t.id, t.name
ORDER BY community_count DESC;
