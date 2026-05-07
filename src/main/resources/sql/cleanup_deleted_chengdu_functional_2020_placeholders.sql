-- ============================================================
-- 清理成都功能区 2020 已删除旧码占位乡镇
--
-- 背景：
-- 服务层历史版本会把 is_deleted=1 的 2020 基准基层行也合并展示，
-- 导致四川天府新区成都直管区、成都东部新区页面出现旧码空街道重复。
--
-- 该脚本只删除已迁移、已标记删除、且无村社下级的旧码占位行。
-- 有效展示行使用 5101717xx / 5101737xx，并通过 baseline_code 保留原始评估源代码。
-- ============================================================

SET NAMES utf8mb4;

SET @suffix = DATE_FORMAT(NOW(), '%Y%m%d_%H%i%s');
SET @backup_sql = CONCAT(
    'CREATE TABLE IF NOT EXISTS grassroots_deleted_functional_placeholder_bak_', @suffix,
    ' AS SELECT * FROM grassroots_organization WHERE year = 2020 AND is_deleted = 1 AND level = 4 ',
    'AND (code REGEXP ''^5101710(18|19|20|21|22|23|24|25)$'' ',
    'OR code REGEXP ''^510173(009|010|011|012|013|014|015|016|017|121|126|127|128|131|132)$'')'
);
PREPARE stmt FROM @backup_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

DELETE g
FROM grassroots_organization g
JOIN (
    SELECT candidate.id
    FROM grassroots_organization candidate
    LEFT JOIN grassroots_organization child
      ON child.year = candidate.year
     AND child.level = 5
     AND (child.parent_id = candidate.id OR child.code LIKE CONCAT(candidate.code, '___'))
    WHERE candidate.year = 2020
      AND candidate.is_deleted = 1
      AND candidate.level = 4
      AND (
          candidate.code REGEXP '^5101710(18|19|20|21|22|23|24|25)$'
          OR candidate.code REGEXP '^510173(009|010|011|012|013|014|015|016|017|121|126|127|128|131|132)$'
      )
    GROUP BY candidate.id
    HAVING COUNT(child.id) = 0
) doomed ON doomed.id = g.id
WHERE g.year = 2020
  AND g.is_deleted = 1
  AND g.level = 4
  AND (
      g.code REGEXP '^5101710(18|19|20|21|22|23|24|25)$'
      OR g.code REGEXP '^510173(009|010|011|012|013|014|015|016|017|121|126|127|128|131|132)$'
  );

SELECT 'remaining_deleted_functional_placeholders' AS metric,
       COUNT(*) AS cnt
FROM grassroots_organization
WHERE year = 2020
  AND is_deleted = 1
  AND level = 4
  AND (
      code REGEXP '^5101710(18|19|20|21|22|23|24|25)$'
      OR code REGEXP '^510173(009|010|011|012|013|014|015|016|017|121|126|127|128|131|132)$'
  );

SELECT 'active_tianfu_2020' AS metric,
       g.level,
       COUNT(*) AS cnt
FROM grassroots_organization g
JOIN organization o ON o.id = g.county_id
WHERE o.code = '510171'
  AND g.year = 2020
  AND g.is_deleted = 0
GROUP BY g.level
ORDER BY g.level;

SELECT 'active_east_2020' AS metric,
       g.level,
       COUNT(*) AS cnt
FROM grassroots_organization g
JOIN organization o ON o.id = g.county_id
WHERE o.code = '510173'
  AND g.year = 2020
  AND g.is_deleted = 0
GROUP BY g.level
ORDER BY g.level;
