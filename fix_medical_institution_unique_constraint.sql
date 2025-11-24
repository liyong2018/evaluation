-- 修改医疗机构表唯一约束：从单字段(unique_code)改为复合字段(unique_code, year)
-- 这样可以支持同一医疗机构在不同年份的数据

-- 1. 首先删除现有的唯一约束
-- 注意：约束名称可能是 'unique_code' 或 'medical_institution.unique_code' 或其他自动生成的名称
-- 我们尝试几种可能的约束名称
ALTER TABLE medical_institution DROP INDEX IF EXISTS unique_code;
ALTER TABLE medical_institution DROP INDEX IF EXISTS `medical_institution.unique_code`;
ALTER TABLE medical_institution DROP INDEX IF EXISTS uk_unique_code;

-- 2. 添加新的复合唯一约束
ALTER TABLE medical_institution
ADD CONSTRAINT uk_unique_code_year
UNIQUE (unique_code, year);

-- 3. 验证约束修改成功
SHOW INDEX FROM medical_institution WHERE Key_name = 'uk_unique_code_year';