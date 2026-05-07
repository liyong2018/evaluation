-- ============================================================
-- 清理成都功能区 2020 已迁出源行政区旧节点
--
-- 背景：
-- 天府新区 / 高新区已经按功能区显示为 510171 / 510172。
-- 原双流、武侯、郫都下面对应源代码节点已经 is_deleted=1，
-- 但历史服务查询会加载已删除基准行，导致页面仍在原行政区展示重复街道。
--
-- 本脚本只删除：
-- - 已标记删除的 2020 基准行
-- - 已迁出到功能区的源代码前缀
-- - 乡镇及其已删除村社下级
-- 有效功能区行保留，并通过 baseline_code 继续关联原评估源代码。
-- ============================================================

SET NAMES utf8mb4;

DROP TEMPORARY TABLE IF EXISTS tmp_chengdu_functional_source_prefixes;
CREATE TEMPORARY TABLE tmp_chengdu_functional_source_prefixes (
    code VARCHAR(32) NOT NULL PRIMARY KEY
);

INSERT INTO tmp_chengdu_functional_source_prefixes (code) VALUES
-- 四川天府新区成都直管区：原双流区源代码
('510116003'),
('510116018'),
('510116019'),
('510116020'),
('510116021'),
('510116022'),
('510116023'),
('510116024'),
('510116025'),
-- 成都高新区：原武侯、双流、郫都源代码
('510107061'),
('510107062'),
('510107063'),
('510107064'),
('510116004'),
('510117019'),
('510117020');

SET @suffix = DATE_FORMAT(NOW(), '%Y%m%d_%H%i%s');
SET @backup_sql = CONCAT(
    'CREATE TABLE IF NOT EXISTS grassroots_deleted_functional_source_bak_', @suffix,
    ' AS SELECT g.* FROM grassroots_organization g ',
    'JOIN tmp_chengdu_functional_source_prefixes p ',
    '  ON g.code = p.code OR g.code LIKE CONCAT(p.code, ''___'') ',
    'WHERE g.year = 2020 AND g.is_deleted = 1 AND g.level IN (4, 5)'
);
PREPARE stmt FROM @backup_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

DELETE g
FROM grassroots_organization g
JOIN tmp_chengdu_functional_source_prefixes p
  ON g.code = p.code OR g.code LIKE CONCAT(p.code, '___')
WHERE g.year = 2020
  AND g.is_deleted = 1
  AND g.level IN (4, 5);

SELECT 'remaining_deleted_functional_source_rows' AS metric,
       COUNT(*) AS cnt
FROM grassroots_organization g
JOIN tmp_chengdu_functional_source_prefixes p
  ON g.code = p.code OR g.code LIKE CONCAT(p.code, '___')
WHERE g.year = 2020
  AND g.is_deleted = 1
  AND g.level IN (4, 5);

SELECT 'active_source_residue' AS metric,
       g.level,
       COUNT(*) AS cnt
FROM grassroots_organization g
JOIN tmp_chengdu_functional_source_prefixes p
  ON g.code = p.code OR g.code LIKE CONCAT(p.code, '___')
WHERE g.year = 2020
  AND g.is_deleted = 0
  AND g.level IN (4, 5)
GROUP BY g.level
ORDER BY g.level;

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

SELECT 'active_hightech_2020' AS metric,
       g.level,
       COUNT(*) AS cnt
FROM grassroots_organization g
JOIN organization o ON o.id = g.county_id
WHERE o.code = '510172'
  AND g.year = 2020
  AND g.is_deleted = 0
GROUP BY g.level
ORDER BY g.level;
