-- 为 medical_institution 表添加索引以提升查询性能

-- 复合索引：year + org_code (支持 eq + likeRight 查询)
CREATE INDEX idx_medical_institution_year_org ON medical_institution(year, org_code);

-- 单独为 year 创建索引
CREATE INDEX idx_medical_institution_year ON medical_institution(year);

-- 为 org_code 创建索引
CREATE INDEX idx_medical_institution_org_code ON medical_institution(org_code);

-- 为 create_time 创建索引（用于排序）
CREATE INDEX idx_medical_institution_create_time ON medical_institution(create_time);

-- 分析表以更新统计信息
ANALYZE TABLE medical_institution;
