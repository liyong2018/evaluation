-- 创建模型管理相关表结构
-- 用于支持QLExpress动态规则引擎的模型配置

USE evaluate_db;

-- 1. 创建评估模型表
CREATE TABLE evaluation_model (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    model_name VARCHAR(100) NOT NULL COMMENT '模型名称',
    model_code VARCHAR(50) NOT NULL UNIQUE COMMENT '模型编码',
    description TEXT COMMENT '模型描述',
    version VARCHAR(20) DEFAULT '1.0' COMMENT '模型版本',
    status INT DEFAULT 1 COMMENT '状态(1-启用,0-禁用)',
    is_default TINYINT(1) DEFAULT 0 COMMENT '是否默认模型',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    create_by VARCHAR(50) COMMENT '创建人',
    update_by VARCHAR(50) COMMENT '更新人'
) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 2. 创建模型步骤表
CREATE TABLE model_step (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    model_id BIGINT NOT NULL COMMENT '模型ID',
    step_name VARCHAR(100) NOT NULL COMMENT '步骤名称',
    step_code VARCHAR(50) NOT NULL COMMENT '步骤编码',
    step_order INT NOT NULL COMMENT '执行顺序',
    step_type VARCHAR(20) NOT NULL COMMENT '步骤类型(CALCULATION/NORMALIZATION/WEIGHTING/TOPSIS/GRADING)',
    description TEXT COMMENT '步骤描述',
    input_variables TEXT COMMENT '输入变量(JSON格式)',
    output_variables TEXT COMMENT '输出变量(JSON格式)',
    depends_on VARCHAR(255) COMMENT '依赖步骤ID(逗号分隔)',
    status INT DEFAULT 1 COMMENT '状态(1-启用,0-禁用)',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (model_id) REFERENCES evaluation_model(id) ON DELETE CASCADE
) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 3. 创建步骤算法表
CREATE TABLE step_algorithm (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    step_id BIGINT NOT NULL COMMENT '步骤ID',
    algorithm_name VARCHAR(100) NOT NULL COMMENT '算法名称',
    algorithm_code VARCHAR(50) NOT NULL COMMENT '算法编码',
    algorithm_order INT NOT NULL COMMENT '算法执行顺序',
    ql_expression TEXT NOT NULL COMMENT 'QLExpress表达式',
    input_params TEXT COMMENT '输入参数定义(JSON格式)',
    output_param VARCHAR(100) COMMENT '输出参数名',
    description TEXT COMMENT '算法描述',
    status INT DEFAULT 1 COMMENT '状态(1-启用,0-禁用)',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (step_id) REFERENCES model_step(id) ON DELETE CASCADE
) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 4. 创建模型执行记录表
CREATE TABLE model_execution_record (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    model_id BIGINT NOT NULL COMMENT '模型ID',
    execution_code VARCHAR(50) NOT NULL UNIQUE COMMENT '执行编码',
    region_ids TEXT COMMENT '执行地区ID列表(JSON格式)',
    weight_config_id BIGINT COMMENT '权重配置ID',
    execution_status VARCHAR(20) DEFAULT 'RUNNING' COMMENT '执行状态(RUNNING/SUCCESS/FAILED)',
    start_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    end_time TIMESTAMP NULL,
    error_message TEXT COMMENT '错误信息',
    result_summary TEXT COMMENT '执行结果摘要(JSON格式)',
    create_by VARCHAR(50) COMMENT '执行人',
    FOREIGN KEY (model_id) REFERENCES evaluation_model(id),
    FOREIGN KEY (weight_config_id) REFERENCES weight_config(id)
) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 5. 创建步骤执行结果表
CREATE TABLE step_execution_result (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    execution_record_id BIGINT NOT NULL COMMENT '执行记录ID',
    step_id BIGINT NOT NULL COMMENT '步骤ID',
    region_code VARCHAR(20) NOT NULL COMMENT '地区代码',
    step_input TEXT COMMENT '步骤输入数据(JSON格式)',
    step_output TEXT COMMENT '步骤输出数据(JSON格式)',
    execution_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    duration_ms BIGINT COMMENT '执行耗时(毫秒)',
    status VARCHAR(20) DEFAULT 'SUCCESS' COMMENT '执行状态(SUCCESS/FAILED)',
    error_message TEXT COMMENT '错误信息',
    FOREIGN KEY (execution_record_id) REFERENCES model_execution_record(id) ON DELETE CASCADE,
    FOREIGN KEY (step_id) REFERENCES model_step(id)
) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 创建索引
CREATE INDEX idx_evaluation_model_code ON evaluation_model(model_code);
CREATE INDEX idx_evaluation_model_status ON evaluation_model(status);
CREATE INDEX idx_model_step_model_order ON model_step(model_id, step_order);
CREATE INDEX idx_step_algorithm_step_order ON step_algorithm(step_id, algorithm_order);
CREATE INDEX idx_execution_record_model ON model_execution_record(model_id);
CREATE INDEX idx_execution_record_status ON model_execution_record(execution_status);
CREATE INDEX idx_step_result_execution ON step_execution_result(execution_record_id);
CREATE INDEX idx_step_result_region ON step_execution_result(region_code);

-- 初始化默认评估模型
INSERT INTO evaluation_model (model_name, model_code, description, is_default) VALUES
('标准减灾能力评估模型', 'STANDARD_MODEL', '基于TOPSIS算法的标准减灾能力评估模型，包含评估指标赋值、属性向量归一化、定权、优劣解算法、分级等步骤', 1);

-- 获取插入的模型ID
SET @model_id = LAST_INSERT_ID();

-- 初始化模型步骤
INSERT INTO model_step (model_id, step_name, step_code, step_order, step_type, description) VALUES
(@model_id, '评估指标赋值', 'INDICATOR_ASSIGNMENT', 1, 'CALCULATION', '根据调查数据计算8个二级指标的原始值'),
(@model_id, '属性向量归一化', 'VECTOR_NORMALIZATION', 2, 'NORMALIZATION', '对二级指标进行向量归一化处理'),
(@model_id, '二级指标定权', 'SECONDARY_WEIGHTING', 3, 'WEIGHTING', '将归一化值与二级指标权重相乘'),
(@model_id, '一级指标定权', 'PRIMARY_WEIGHTING', 4, 'WEIGHTING', '计算一级指标的定权值'),
(@model_id, '优劣解算法计算', 'TOPSIS_CALCULATION', 5, 'TOPSIS', '基于TOPSIS优劣解算法计算距离'),
(@model_id, '能力值计算', 'CAPABILITY_CALCULATION', 6, 'CALCULATION', '计算最终能力值'),
(@model_id, '能力分级计算', 'GRADING_CALCULATION', 7, 'GRADING', '根据均值和标准差计算能力分级');

SELECT 'Model management tables created successfully!' as message;