-- ================================================
-- 组织机构数据去重与增量存储改造脚本
-- 策略：2020年作为基准年，其他年份只保留变更记录
-- ================================================

-- 步骤1: 添加字段（MySQL不支持ADD COLUMN IF NOT EXISTS，需手动检查）
-- 如果字段已存在，请注释掉对应的ALTER语句
ALTER TABLE organization ADD COLUMN is_baseline TINYINT DEFAULT 0 COMMENT '是否为基准记录(2020年)';
ALTER TABLE organization ADD COLUMN baseline_code VARCHAR(32) DEFAULT NULL COMMENT '基准编码(关联基准记录)';

ALTER TABLE grassroots_organization ADD COLUMN is_baseline TINYINT DEFAULT 0 COMMENT '是否为基准记录(2020年)';
ALTER TABLE grassroots_organization ADD COLUMN baseline_code VARCHAR(32) DEFAULT NULL COMMENT '基准编码(关联基准记录)';

-- 步骤2: 标记2020年为基准年
UPDATE organization SET is_baseline = 1 WHERE year = 2020;
UPDATE grassroots_organization SET is_baseline = 1 WHERE year = 2020;

-- 步骤3: 为非基准记录设置baseline_code（指向其对应的基准code）
UPDATE organization o SET baseline_code = (
    SELECT code FROM (
        SELECT code FROM organization o2
        WHERE o2.level = o.level
          AND o2.code = o.code
          AND o2.is_baseline = 1
        LIMIT 1
    ) AS baseline
) WHERE o.is_baseline = 0 AND o.baseline_code IS NULL;

UPDATE grassroots_organization g SET baseline_code = (
    SELECT code FROM (
        SELECT code FROM grassroots_organization g2
        WHERE g2.level = g.level
          AND g2.code = g.code
          AND g2.is_baseline = 1
        LIMIT 1
    ) AS baseline
) WHERE g.is_baseline = 0 AND g.baseline_code IS NULL;

-- 步骤4: 找出每个code在各年份中与基准不同的记录
-- 这里假设"不同"指：name、parent_id、county_id等关键字段不同

-- 创建临时表存储需要保留的变更记录
CREATE TEMPORARY TABLE temp_changes AS
SELECT DISTINCT o1.*
FROM organization o1
INNER JOIN organization o2 ON o1.code = o2.code AND o2.is_baseline = 1
WHERE o1.is_baseline = 0
  AND (
    o1.name != o2.name
    OR (o1.parent_id IS NULL AND o2.parent_id IS NOT NULL)
    OR (o1.parent_id IS NOT NULL AND o2.parent_id IS NULL)
    OR (o1.parent_id IS NOT NULL AND o2.parent_id IS NOT NULL AND o1.parent_id != o2.parent_id)
    OR COALESCE(o1.province_name, '') != COALESCE(o2.province_name, '')
    OR COALESCE(o1.city_name, '') != COALESCE(o2.city_name, '')
    OR COALESCE(o1.county_name, '') != COALESCE(o2.county_name, '')
  );

-- grassroots_organization 同理
CREATE TEMPORARY TABLE temp_grassroots_changes AS
SELECT DISTINCT g1.*
FROM grassroots_organization g1
INNER JOIN grassroots_organization g2 ON g1.code = g2.code AND g2.is_baseline = 1
WHERE g1.is_baseline = 0
  AND (
    g1.name != g2.name
    OR (g1.county_id IS NULL AND g2.county_id IS NOT NULL)
    OR (g1.county_id IS NOT NULL AND g2.county_id IS NULL)
    OR (g1.county_id IS NOT NULL AND g2.county_id IS NOT NULL AND g1.county_id != g2.county_id)
    OR (g1.parent_id IS NULL AND g2.parent_id IS NOT NULL)
    OR (g1.parent_id IS NOT NULL AND g2.parent_id IS NULL)
    OR (g1.parent_id IS NOT NULL AND g2.parent_id IS NOT NULL AND g1.parent_id != g2.parent_id)
  );

-- 步骤5: 删除与基准完全相同的非基准记录
DELETE FROM organization
WHERE is_baseline = 0
  AND id NOT IN (SELECT id FROM temp_changes);

DELETE FROM grassroots_organization
WHERE is_baseline = 0
  AND id NOT IN (SELECT id FROM temp_grassroots_changes);

-- 步骤6: 统计去重结果
SELECT 'organization 去重统计' AS info;
SELECT year, is_baseline, COUNT(*) AS count
FROM organization
GROUP BY year, is_baseline
ORDER BY year;

SELECT 'grassroots_organization 去重统计' AS info;
SELECT year, is_baseline, COUNT(*) AS count
FROM grassroots_organization
GROUP BY year, is_baseline
ORDER BY year;

-- 清理临时表
DROP TEMPORARY TABLE IF EXISTS temp_changes;
DROP TEMPORARY TABLE IF EXISTS temp_grassroots_changes;
