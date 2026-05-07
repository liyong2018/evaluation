-- ============================================================
-- 组织机构年度增量规范化回滚脚本
-- 对应执行脚本：
--   src/main/resources/sql/normalize_organization_incremental_2020_2024_2025.sql
--
-- 说明：
-- - 该脚本使用执行脚本创建的备份表恢复 organization / grassroots_organization。
-- - 评估数据表 survey_data / community_disaster_reduction_capacity 不会被修改。
-- - 执行前请确认两个备份表存在且行数符合预期。
-- ============================================================

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

SELECT 'organization_backup_before_incremental_20260501' AS table_name, COUNT(*) AS row_count
FROM organization_backup_before_incremental_20260501
UNION ALL
SELECT 'grassroots_organization_backup_before_incremental_20260501', COUNT(*)
FROM grassroots_organization_backup_before_incremental_20260501;

TRUNCATE TABLE grassroots_organization;
TRUNCATE TABLE organization;

INSERT INTO organization
SELECT * FROM organization_backup_before_incremental_20260501;

INSERT INTO grassroots_organization
SELECT * FROM grassroots_organization_backup_before_incremental_20260501;

DROP TABLE IF EXISTS org_incremental_delete_candidate_2020_2025;

SELECT 'organization_restored' AS table_name, COUNT(*) AS row_count
FROM organization
UNION ALL
SELECT 'grassroots_organization_restored', COUNT(*)
FROM grassroots_organization;

SET FOREIGN_KEY_CHECKS = 1;
