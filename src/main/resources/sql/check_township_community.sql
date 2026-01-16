-- ============================================================
-- 检查乡镇和社区数据关系
-- ============================================================

-- 检查1：查看所有2025年的乡镇数据
SELECT
    id,
    code,
    name,
    parent_id,
    county_id,
    year,
    is_baseline
FROM grassroots_organization
WHERE level = 4 AND year = 2025
ORDER BY county_name, code
LIMIT 100;

-- 检查2：查看所有2025年的社区数据
SELECT
    id,
    code,
    name,
    parent_id,
    township_name,
    year,
    is_baseline
FROM grassroots_organization
WHERE level = 5 AND year = 2025
ORDER BY county_name, code
LIMIT 100;

-- 检查3：查找parent_id为NULL的社区数据（问题数据）
SELECT
    id,
    code,
    name,
    parent_id,
    township_name,
    county_name
FROM grassroots_organization
WHERE level = 5
  AND year = 2025
  AND (parent_id IS NULL OR parent_id = 0)
ORDER BY county_name, township_name, code;

-- 检查4：查找应该匹配但parent_id不匹配的社区
SELECT
    child.id as community_id,
    child.code as community_code,
    child.name as community_name,
    child.parent_id as current_parent_id,
    child.township_name,
    parent.id as correct_parent_id,
    parent.name as parent_name_match,
    CASE
        WHEN child.parent_id IS NULL THEN 'parent_id为NULL'
        WHEN child.parent_id = parent.id THEN '匹配正确'
        ELSE 'parent_id不匹配'
    END as status
FROM grassroots_organization child
LEFT JOIN grassroots_organization parent
    ON child.township_name = parent.name
    AND child.county_name = parent.county_name
    AND parent.level = 4
    AND parent.year = 2025
WHERE child.level = 5
  AND child.year = 2025
ORDER BY child.county_name, child.township_name, child.code
LIMIT 200;

-- 检查5：统计每个区县的乡镇和社区数量
SELECT
    county_name,
    COUNT(DISTINCT CASE WHEN level = 4 THEN id END) as township_count,
    COUNT(DISTINCT CASE WHEN level = 5 THEN id END) as community_count,
    COUNT(DISTINCT CASE WHEN level = 5 AND parent_id IS NOT NULL THEN id END) as community_with_parent
FROM grassroots_organization
WHERE year = 2025
GROUP BY county_name
ORDER BY county_name;
