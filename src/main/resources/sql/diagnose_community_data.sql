-- ============================================================
-- 诊断和修复社区数据parent_id问题
-- ============================================================

-- 1. 查看2025年旌阳区的所有乡镇
SELECT id, code, name, level, parent_id, county_id, year, is_baseline
FROM grassroots_organization
WHERE county_name LIKE '%旌阳%' AND level = 4 AND year = 2025
ORDER BY code;

-- 2. 查看2025年旌阳区的所有社区
SELECT id, code, name, level, parent_id, county_id, year, is_baseline, township_name
FROM grassroots_organization
WHERE county_name LIKE '%旌阳%' AND level = 5 AND year = 2025
ORDER BY code;

-- 3. 查看某个乡镇（如旌阳街道）下的社区 - 使用正确的乡镇ID
-- 请将下面的6046替换为实际的乡镇ID
SELECT child.id, child.code, child.name, child.parent_id, child.township_name,
       parent.id as township_id, parent.name as township_name
FROM grassroots_organization child
LEFT JOIN grassroots_organization parent
    ON child.parent_id = parent.id
WHERE child.level = 5
  AND child.year = 2025
  AND child.township_name LIKE '%旌阳街道%';

-- ============================================================
-- 修复方案1：根据township_name匹配更新parent_id
-- ============================================================
UPDATE grassroots_organization child
INNER JOIN grassroots_organization parent
    ON child.township_name = parent.name
    AND child.county_name = parent.county_name
    AND child.level = 5
    AND parent.level = 4
    AND child.year = 2025
    AND parent.year = 2025
SET child.parent_id = parent.id
WHERE child.parent_id IS NULL
   OR child.parent_id != parent.id;

-- ============================================================
-- 修复方案2：如果社区数据完全没有，需要重新导入
-- ============================================================
-- 检查是否有导入来源为社区数据的记录
SELECT COUNT(*) as community_count, data_source
FROM grassroots_organization
WHERE level = 5 AND year = 2025
GROUP BY data_source;

-- ============================================================
-- 验证修复结果
-- ============================================================
-- 查看修复后的旌阳街道及其社区
SELECT parent.id as township_id, parent.name as township_name, parent.code as township_code,
       COUNT(child.id) as community_count
FROM grassroots_organization parent
LEFT JOIN grassroots_organization child
    ON child.parent_id = parent.id
    AND child.level = 5
    AND child.year = 2025
WHERE parent.name LIKE '%旌阳街道%'
  AND parent.level = 4
  AND parent.year = 2025
GROUP BY parent.id, parent.name, parent.code;
