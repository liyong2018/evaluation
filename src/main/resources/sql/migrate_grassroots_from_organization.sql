-- ================================================
-- 组织机构表数据迁移脚本（乡镇和社区）
-- 将 Organization 表中的 level=4（乡镇）和 level=5（社区）数据迁移到 grassroots_organization 表
-- 支持增量存储（is_baseline、baseline_code 字段）
-- ================================================

-- 开始事务
START TRANSACTION;

-- 1. 首先检查当前 grassroots_organization 表结构
-- 如果表不存在或缺少字段，需要先执行 split_organization_tables.sql 创建表

-- 2. 备份原表数据（安全起见）
DROP TABLE IF EXISTS `organization_backup_before_grassroots_migration`;
CREATE TABLE `organization_backup_before_grassroots_migration` LIKE `organization`;
INSERT INTO `organization_backup_before_grassroots_migration`
SELECT * FROM `organization`;

-- 3. 统计待迁移的数据量
SELECT '统计乡镇数据（level=4）:' AS info;
SELECT level, year, COUNT(*) as count FROM organization WHERE level = 4 GROUP BY year ORDER BY year;

SELECT '统计社区数据（level=5）:' AS info;
SELECT level, year, COUNT(*) as count FROM organization WHERE level = 5 GROUP BY year ORDER BY year;

-- 4. 迁移乡镇级数据（level=4）
-- 注意：需要正确设置 county_id（父级区县ID）
INSERT INTO `grassroots_organization` (
  `county_id`,
  `parent_id`,
  `code`,
  `name`,
  `level`,
  `year`,
  `data_source`,
  `province_name`,
  `city_name`,
  `county_name`,
  `township_name`,
  `community_name`,
  `is_baseline`,
  `baseline_code`,
  `is_deleted`,
  `create_time`,
  `update_time`
)
SELECT
  -- county_id: 通过前6位代码匹配找到对应的区县ID
  (
    SELECT o3.id
    FROM organization o3
    WHERE o3.level = 3
      AND o3.year <= o1.year
      AND LEFT(o3.code, 6) = LEFT(o1.code, 6)
      AND o3.is_deleted = 0
    ORDER BY o3.year DESC
    LIMIT 1
  ) AS county_id,
  -- parent_id: 乡镇的 parent_id 指向区县
  (
    SELECT o3.id
    FROM organization o3
    WHERE o3.level = 3
      AND o3.year <= o1.year
      AND LEFT(o3.code, 6) = LEFT(o1.code, 6)
      AND o3.is_deleted = 0
    ORDER BY o3.year DESC
    LIMIT 1
  ) AS parent_id,
  o1.`code`,
  o1.`name`,
  o1.`level`,
  o1.`year`,
  COALESCE(o1.`data_source`, 'TOWNSHIP') AS data_source,
  o1.`province_name`,
  o1.`city_name`,
  o1.`county_name`,
  o1.`township_name`,
  o1.`community_name`,
  COALESCE(o1.`is_baseline`, 1) AS is_baseline,
  COALESCE(o1.`baseline_code`, o1.`code`) AS baseline_code,
  COALESCE(o1.`is_deleted`, 0) AS is_deleted,
  o1.`create_time`,
  o1.`update_time`
FROM `organization` o1
WHERE o1.level = 4;

-- 5. 迁移社区级数据（level=5）
INSERT INTO `grassroots_organization` (
  `county_id`,
  `parent_id`,
  `code`,
  `name`,
  `level`,
  `year`,
  `data_source`,
  `province_name`,
  `city_name`,
  `county_name`,
  `township_name`,
  `community_name`,
  `is_baseline`,
  `baseline_code`,
  `is_deleted`,
  `create_time`,
  `update_time`
)
SELECT
  -- county_id: 通过前6位代码匹配找到对应的区县ID
  (
    SELECT o3.id
    FROM organization o3
    WHERE o3.level = 3
      AND o3.year <= o1.year
      AND LEFT(o3.code, 6) = LEFT(o1.code, 6)
      AND o3.is_deleted = 0
    ORDER BY o3.year DESC
    LIMIT 1
  ) AS county_id,
  -- parent_id: 社区的 parent_id 指向所属乡镇（通过 grassroots_organization 表查找）
  (
    SELECT g2.id
    FROM grassroots_organization g2
    WHERE g2.level = 4
      AND g2.year <= o1.year
      AND LEFT(g2.code, 9) = LEFT(o1.code, 9)
      AND g2.is_deleted = 0
    ORDER BY g2.year DESC
    LIMIT 1
  ) AS parent_id,
  o1.`code`,
  o1.`name`,
  o1.`level`,
  o1.`year`,
  COALESCE(o1.`data_source`, 'COMMUNITY') AS data_source,
  o1.`province_name`,
  o1.`city_name`,
  o1.`county_name`,
  o1.`township_name`,
  o1.`community_name`,
  COALESCE(o1.`is_baseline`, 1) AS is_baseline,
  COALESCE(o1.`baseline_code`, o1.`code`) AS baseline_code,
  COALESCE(o1.`is_deleted`, 0) AS is_deleted,
  o1.`create_time`,
  o1.`update_time`
FROM `organization` o1
WHERE o1.level = 5;

-- 6. 验证迁移结果
SELECT '迁移后统计 - grassroots_organization 表:' AS info;
SELECT level, year, COUNT(*) as count FROM grassroots_organization GROUP BY level, year ORDER BY level, year;

-- 7. 检查是否有 county_id 为 NULL 的记录
SELECT 'county_id 为 NULL 的记录（需要手动处理）:' AS info;
SELECT id, code, name, level, year FROM grassroots_organization WHERE county_id IS NULL LIMIT 10;

-- 8. 检查是否有 parent_id 为 NULL 的记录
SELECT 'parent_id 为 NULL 的记录:' AS info;
SELECT id, code, name, level, year, county_id FROM grassroots_organization WHERE parent_id IS NULL LIMIT 10;

-- 9. 删除 Organization 表中已迁移的数据（level=4 和 level=5）
-- 注意：此操作不可逆，建议先验证数据正确性后再执行
DELETE FROM `organization` WHERE level IN (4, 5);

-- 10. 验证删除后的数据分布
SELECT '删除后 organization 表数据分布:' AS info;
SELECT level, COUNT(*) as count FROM organization GROUP BY level ORDER BY level;

-- 提交事务
COMMIT;

-- ================================================
-- 迁移完成后的检查：
-- 1. organization 表现在只包含 level 1-3 的数据（省、市、县）
-- 2. grassroots_organization 表包含 level 4-5 的数据（乡镇、社区）
-- 3. 检查 grassroots_organization 中 county_id 和 parent_id 是否正确设置
-- 4. 原数据已备份到 organization_backup_before_grassroots_migration 表
-- ================================================

-- 验证查询（执行后手动运行）
-- 检查乡镇数量
-- SELECT COUNT(*) FROM grassroots_organization WHERE level = 4;

-- 检查社区数量
-- SELECT COUNT(*) FROM grassroots_organization WHERE level = 5;

-- 检查有问题的记录（county_id 为空）
-- SELECT * FROM grassroots_organization WHERE county_id IS NULL;

-- 检查有问题的记录（parent_id 为空且 level=5，社区应该有父级乡镇）
-- SELECT * FROM grassroots_organization WHERE level = 5 AND parent_id IS NULL;
