-- ============================================================
-- 快速诊断：检查旌阳街道和社区数据
-- ============================================================

-- 1. 查找"旌阳街道"的ID和基本信息
SELECT '=== 旌阳街道信息 ===' as info;
SELECT
    id,
    code,
    name,
    level,
    parent_id,
    county_id,
    year,
    is_baseline
FROM grassroots_organization
WHERE name LIKE '%旌阳街道%'
  AND year = 2025;

-- 2. 查找2025年旌阳区的所有社区数据（不限parent_id）
SELECT '=== 2025年旌阳区所有社区 ===' as info;
SELECT
    id,
    code,
    name,
    parent_id,
    township_name,
    year,
    is_baseline
FROM grassroots_organization
WHERE level = 5
  AND year = 2025
  AND county_name LIKE '%旌阳%'
ORDER BY township_name, code
LIMIT 50;

-- 3. 统计旌阳区2025年的乡镇和社区数量
SELECT '=== 旌阳区数据统计 ===' as info;
SELECT
    '乡镇数量' as type,
    COUNT(*) as count
FROM grassroots_organization
WHERE level = 4 AND year = 2025 AND county_name LIKE '%旌阳%'
UNION ALL
SELECT
    '社区数量' as type,
    COUNT(*) as count
FROM grassroots_organization
WHERE level = 5 AND year = 2025 AND county_name LIKE '%旌阳%'
UNION ALL
SELECT
    '有parent_id的社区' as type,
    COUNT(*) as count
FROM grassroots_organization
WHERE level = 5 AND year = 2025 AND county_name LIKE '%旌阳%' AND parent_id IS NOT NULL;

-- 4. 查找parent_id不匹配的社区
SELECT '=== parent_id不匹配检查 ===' as info;
SELECT
    child.id as community_id,
    child.code as community_code,
    child.name as community_name,
    child.parent_id as current_parent_id,
    child.township_name,
    parent.id as expected_parent_id,
    parent.name as expected_township_name
FROM grassroots_organization child
LEFT JOIN grassroots_organization parent
    ON child.township_name = parent.name
    AND child.county_name = parent.county_name
    AND parent.level = 4
    AND parent.year = 2025
WHERE child.level = 5
  AND child.year = 2025
  AND child.county_name LIKE '%旌阳%'
ORDER BY child.township_name, child.code
LIMIT 50;
