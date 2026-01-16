-- ================================================
-- 修复 admin 用户看不到组织机构的问题
-- 问题原因：sys_role_organization 表中限制了admin只能访问特定组织
-- 解决方案：删除admin角色的组织权限限制（admin应该可以访问所有组织）
-- ================================================

USE evaluate_db;

-- 1. 检查当前admin角色的组织权限
SELECT '=== 修复前：admin角色的组织权限 ===' AS info;
SELECT ro.*, o.name, o.code, o.level
FROM sys_role_organization ro
LEFT JOIN organization o ON ro.organization_id = o.id
WHERE ro.role_id = 1;

-- 2. 删除admin角色的组织权限限制
DELETE FROM sys_role_organization WHERE role_id = 1;

-- 3. 验证修复结果
SELECT '=== 修复后：admin角色的组织权限 ===' AS info;
SELECT COUNT(*) AS restricted_count FROM sys_role_organization WHERE role_id = 1;

-- 注意：restricted_count = 0 表示admin可以访问所有组织（无限制）
