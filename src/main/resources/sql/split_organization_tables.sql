-- ================================================
-- 组织机构表拆分迁移脚本
-- 将乡镇(level=4)和社区(level=5)数据迁移到新表
-- ================================================

-- 1. 创建基层组织机构表（乡镇和社区）
CREATE TABLE IF NOT EXISTS `grassroots_organization` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `county_id` bigint NOT NULL COMMENT '所属区县ID（关联organization表）',
  `parent_id` bigint DEFAULT NULL COMMENT '父级机构ID（乡镇的parent_id指向区县，社区的parent_id指向乡镇）',
  `code` varchar(32) NOT NULL COMMENT '机构编码（行政区划代码）',
  `name` varchar(128) NOT NULL COMMENT '机构名称',
  `level` tinyint NOT NULL COMMENT '级别：4乡镇、5社区',
  `year` int DEFAULT NULL COMMENT '数据所属年份',
  `data_source` varchar(32) NOT NULL COMMENT '来源：COMMUNITY/TOWNSHIP/IMPORT 等',
  `province_name` varchar(128) DEFAULT NULL COMMENT '省名称',
  `city_name` varchar(128) DEFAULT NULL COMMENT '市名称',
  `county_name` varchar(128) DEFAULT NULL COMMENT '县名称',
  `township_name` varchar(128) DEFAULT NULL COMMENT '乡镇名称',
  `community_name` varchar(128) DEFAULT NULL COMMENT '社区名称',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` tinyint NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_grassroots_code_year` (`code`, `year`),
  KEY `idx_grassroots_county` (`county_id`),
  KEY `idx_grassroots_parent` (`parent_id`),
  KEY `idx_grassroots_level` (`level`),
  KEY `idx_grassroots_year` (`year`),
  CONSTRAINT `fk_grassroots_county` FOREIGN KEY (`county_id`) REFERENCES `organization` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='基层组织机构表（乡镇和社区）';

-- 2. 迁移乡镇级数据（level=4）
INSERT INTO `grassroots_organization` (
  `county_id`, `parent_id`, `code`, `name`, `level`, `year`, `data_source`,
  `province_name`, `city_name`, `county_name`, `township_name`, `community_name`,
  `create_time`, `update_time`, `is_deleted`
)
SELECT
  -- 找到该乡镇所属的区县ID
  (SELECT id FROM (
    SELECT o2.id, o2.code, o2.level, o2.year
    FROM organization o2
    WHERE o2.level = 3
      AND o2.year = o1.year
      AND LEFT(o2.code, 6) = LEFT(o1.code, 6)
    LIMIT 1
  ) AS county),
  NULL AS parent_id,
  o1.code,
  o1.name,
  o1.level,
  o1.year,
  o1.data_source,
  o1.province_name,
  o1.city_name,
  o1.county_name,
  o1.township_name,
  o1.community_name,
  o1.create_time,
  o1.update_time,
  o1.is_deleted
FROM organization o1
WHERE o1.level = 4 AND o1.is_deleted = 0;

-- 3. 更新乡镇的parent_id，指向对应的区县
UPDATE grassroots_organization g
INNER JOIN organization o ON g.code = o.code AND g.year = o.year
SET g.parent_id = (
  SELECT id FROM (
    SELECT o2.id
    FROM organization o2
    WHERE o2.level = 3
      AND o2.year = g.year
      AND LEFT(o2.code, 6) = LEFT(g.code, 6)
    LIMIT 1
  ) AS county_id
)
WHERE g.level = 4;

-- 4. 迁移社区级数据（level=5）
INSERT INTO `grassroots_organization` (
  `county_id`, `parent_id`, `code`, `name`, `level`, `year`, `data_source`,
  `province_name`, `city_name`, `county_name`, `township_name`, `community_name`,
  `create_time`, `update_time`, `is_deleted`
)
SELECT
  -- 找到该社区所属的区县ID
  (SELECT id FROM (
    SELECT o2.id, o2.code, o2.level, o2.year
    FROM organization o2
    WHERE o2.level = 3
      AND o2.year = o1.year
      AND LEFT(o2.code, 6) = LEFT(o1.code, 6)
    LIMIT 1
  ) AS county),
  -- parent_id将后续更新，指向所属乡镇
  NULL AS parent_id,
  o1.code,
  o1.name,
  o1.level,
  o1.year,
  o1.data_source,
  o1.province_name,
  o1.city_name,
  o1.county_name,
  o1.township_name,
  o1.community_name,
  o1.create_time,
  o1.update_time,
  o1.is_deleted
FROM organization o1
WHERE o1.level = 5 AND o1.is_deleted = 0;

-- 5. 更新社区的parent_id，指向对应的乡镇
UPDATE grassroots_organization g
INNER JOIN organization o ON g.code = o.code AND g.year = o.year
SET g.parent_id = (
  SELECT id FROM (
    SELECT g2.id
    FROM grassroots_organization g2
    WHERE g2.level = 4
      AND g2.year = g.year
      AND LEFT(g2.code, 9) = LEFT(g.code, 9)
    LIMIT 1
  ) AS township_id
)
WHERE g.level = 5;

-- 6. 确保county_id正确设置（重新更新一遍）
UPDATE grassroots_organization g
SET g.county_id = (
  SELECT id FROM (
    SELECT o.id
    FROM organization o
    WHERE o.level = 3
      AND o.year = g.year
      AND LEFT(o.code, 6) = LEFT(g.code, 6)
    LIMIT 1
  ) AS county_id
)
WHERE g.county_id IS NULL;

-- 7. 备份原表数据（可选，创建备份表）
CREATE TABLE IF NOT EXISTS `organization_backup` LIKE `organization`;
INSERT INTO `organization_backup`
SELECT * FROM `organization`;

-- 8. 删除已迁移的数据（level=4 和 level=5）
-- 注意：执行前请确认数据已正确迁移
-- DELETE FROM organization WHERE level IN (4, 5);

-- ================================================
-- 迁移完成后的说明：
-- 1. organization 表现在只包含 level 1-3 的数据（省、市、县）
-- 2. grassroots_organization 表包含 level 4-5 的数据（乡镇、社区）
-- 3. 通过 county_id 关联两表
-- 4. 原数据已备份到 organization_backup 表
-- ================================================
