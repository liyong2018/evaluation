-- 给 weight_config 表添加 orgcode 字段，用于关联组织机构

-- 添加 orgcode 字段
ALTER TABLE weight_config
ADD COLUMN orgcode VARCHAR(32) NULL COMMENT '组织机构编码（行政区划代码）' AFTER description;

-- 添加索引以优化按组织机构查询的性能
CREATE INDEX idx_weight_config_orgcode ON weight_config(orgcode);

-- 添加外键约束（可选，如果organization表已存在）
-- ALTER TABLE weight_config
-- ADD CONSTRAINT fk_weight_config_organization
-- FOREIGN KEY (orgcode) REFERENCES organization(code);
