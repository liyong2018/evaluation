-- 为 community_disaster_reduction_capacity 表添加索引以提升查询性能

-- 复合索引：region_code (支持 likeRight 查询)
CREATE INDEX idx_community_region_code ON community_disaster_reduction_capacity(region_code);

-- 为 community_name 创建索引（用于搜索）
CREATE INDEX idx_community_name ON community_disaster_reduction_capacity(community_name);

-- 为 create_time 创建索引（用于排序）
CREATE INDEX idx_community_create_time ON community_disaster_reduction_capacity(create_time);

-- 分析表以更新统计信息
ANALYZE TABLE community_disaster_reduction_capacity;
