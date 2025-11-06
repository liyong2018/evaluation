-- 创建专家权重打分记录表
-- 用于存储多个专家对权重配置的打分记录

CREATE TABLE IF NOT EXISTS indicator_weight_score (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    config_id BIGINT NOT NULL COMMENT '权重配置ID',
    indicator_code VARCHAR(50) NOT NULL COMMENT '指标代码',
    weight DOUBLE NOT NULL COMMENT '专家建议的权重值（0-1之间）',
    expert_name VARCHAR(100) NOT NULL COMMENT '专家姓名',
    expert_phone VARCHAR(20) DEFAULT NULL COMMENT '专家电话',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '打分时间',

    INDEX idx_config_id (config_id),
    INDEX idx_indicator_code (indicator_code),
    INDEX idx_expert_name (expert_name),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='专家权重打分记录表';
