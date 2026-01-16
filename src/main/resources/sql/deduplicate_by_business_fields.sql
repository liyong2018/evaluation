-- ================================================
-- 按业务字段去重（解决parent_id不同导致的重复问题）
-- ================================================

-- 创建临时表存储需要删除的ID
CREATE TEMPORARY TABLE temp_org_delete AS
SELECT o1.id
FROM organization o1
INNER JOIN organization o2 ON o1.code = o2.code AND o2.year = 2020
WHERE o1.year IN (2021, 2022)
  AND o1.name = o2.name
  AND COALESCE(o1.province_name, '') = COALESCE(o2.province_name, '')
  AND COALESCE(o1.city_name, '') = COALESCE(o2.city_name, '')
  AND COALESCE(o1.county_name, '') = COALESCE(o2.county_name, '')
  AND COALESCE(o1.township_name, '') = COALESCE(o2.township_name, '')
  AND COALESCE(o1.community_name, '') = COALESCE(o2.community_name, '');

CREATE TEMPORARY TABLE temp_grassroots_delete AS
SELECT g1.id
FROM grassroots_organization g1
INNER JOIN grassroots_organization g2 ON g1.code = g2.code AND g2.year = 2020
WHERE g1.year IN (2021, 2022)
  AND g1.name = g2.name
  AND COALESCE(g1.province_name, '') = COALESCE(g2.province_name, '')
  AND COALESCE(g1.city_name, '') = COALESCE(g2.city_name, '')
  AND COALESCE(g1.county_name, '') = COALESCE(g2.county_name, '')
  AND COALESCE(g1.township_name, '') = COALESCE(g2.township_name, '')
  AND COALESCE(g1.community_name, '') = COALESCE(g2.community_name, '');

-- 显示将要删除的数量
SELECT '将要删除的重复记录数:' AS '';
SELECT 'organization' AS tbl, COUNT(*) AS count FROM temp_org_delete
UNION ALL
SELECT 'grassroots_organization' AS tbl, COUNT(*) AS count FROM temp_grassroots_delete;

-- 执行删除
DELETE FROM organization WHERE id IN (SELECT id FROM temp_org_delete);
DELETE FROM grassroots_organization WHERE id IN (SELECT id FROM temp_grassroots_delete);

-- 显示去重后结果
SELECT '=== 去重后统计 ===' AS '';
SELECT year, is_baseline, COUNT(*) AS count FROM organization GROUP BY year, is_baseline ORDER BY year;
SELECT '' AS '';
SELECT year, is_baseline, COUNT(*) AS count FROM grassroots_organization GROUP BY year, is_baseline ORDER BY year;

-- 清理临时表
DROP TEMPORARY TABLE IF EXISTS temp_org_delete;
DROP TEMPORARY TABLE IF EXISTS temp_grassroots_delete;
