-- 给 indicator_weight_score 表添加 orgcode 字段
-- 用于记录专家打分所属的组织机构

ALTER TABLE indicator_weight_score
ADD COLUMN orgcode VARCHAR(32) NULL COMMENT '组织机构编码（行政区划代码）' AFTER config_id;

-- 添加索引以优化按组织机构查询
CREATE INDEX idx_orgcode ON indicator_weight_score(orgcode);

-- 添加组合索引，优化按配置和组织机构查询
CREATE INDEX idx_config_orgcode ON indicator_weight_score(config_id, orgcode);
