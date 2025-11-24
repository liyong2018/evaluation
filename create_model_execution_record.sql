-- 创建模型执行记录表
CREATE TABLE IF NOT EXISTS model_execution_record (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    model_id BIGINT NOT NULL COMMENT '模型ID',
    execution_code VARCHAR(100) NOT NULL COMMENT '执行代码',
    region_ids TEXT COMMENT '区域ID列表',
    weight_config_id BIGINT COMMENT '权重配置ID',
    execution_status VARCHAR(50) NOT NULL COMMENT '执行状态',
    start_time DATETIME COMMENT '开始时间',
    end_time DATETIME COMMENT '结束时间',
    error_message TEXT COMMENT '错误信息',
    result_summary TEXT COMMENT '结果摘要',
    create_by VARCHAR(100) COMMENT '创建人',
    year INT NOT NULL COMMENT '年份',
    org_code VARCHAR(50) COMMENT '组织机构代码',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_model_execution_record_model_id (model_id),
    INDEX idx_model_execution_record_year (year),
    INDEX idx_model_execution_record_org_code (org_code),
    INDEX idx_model_execution_record_execution_code (execution_code),
    INDEX idx_model_execution_record_status (execution_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='模型执行记录表';