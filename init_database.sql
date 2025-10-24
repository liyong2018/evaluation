-- 减灾能力评估系统数据库初始化脚本
-- 创建时间: 2025-01-24
-- 说明: 包含所有表结构创建和初始化数据

-- 使用evaluate_db数据库
USE evaluate_db;

-- 删除已存在的表
DROP TABLE IF EXISTS report;
DROP TABLE IF EXISTS primary_indicator_result;
DROP TABLE IF EXISTS secondary_indicator_result;
DROP TABLE IF EXISTS formula_config;
DROP TABLE IF EXISTS algorithm_step;
DROP TABLE IF EXISTS algorithm_config;
DROP TABLE IF EXISTS indicator_weight;
DROP TABLE IF EXISTS weight_config;
DROP TABLE IF EXISTS survey_data;
DROP TABLE IF EXISTS community_disaster_reduction_capacity;

-- 1. 创建调查数据表
CREATE TABLE survey_data (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    region_code VARCHAR(20) NOT NULL COMMENT '行政区代码',
    province VARCHAR(255) NOT NULL COMMENT '省名称',
    city VARCHAR(255) NOT NULL COMMENT '市名称', 
    county VARCHAR(255) NOT NULL COMMENT '县名称',
    township VARCHAR(255) NOT NULL COMMENT '乡镇名称',
    population BIGINT NOT NULL COMMENT '常住人口数量',
    management_staff INT NOT NULL COMMENT '本级灾害管理工作人员总数',
    risk_assessment VARCHAR(10) NOT NULL COMMENT '是否开展风险评估',
    funding_amount DECIMAL(15,2) NOT NULL COMMENT '防灾减灾救灾资金投入总金额(万元)',
    material_value DECIMAL(15,2) NOT NULL COMMENT '现有储备物资装备折合金额(万元)',
    hospital_beds INT NOT NULL COMMENT '实有住院床位数',
    firefighters INT NOT NULL COMMENT '消防员数量',
    volunteers INT NOT NULL COMMENT '志愿者人数',
    militia_reserve INT NOT NULL COMMENT '民兵预备役人数',
    training_participants INT NOT NULL COMMENT '应急管理培训和演练参与人次',
    shelter_capacity INT NOT NULL COMMENT '本级灾害应急避难场所容量',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_deleted TINYINT(1) DEFAULT 0
) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 创建索引
CREATE INDEX idx_survey_data_region ON survey_data(region_code);
CREATE INDEX idx_survey_data_township ON survey_data(township);
CREATE INDEX idx_survey_data_create_time ON survey_data(create_time DESC);

-- 1.5. 创建社区行政村减灾能力数据表
CREATE TABLE community_disaster_reduction_capacity (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    region_code VARCHAR(50) NOT NULL COMMENT '行政区代码',
    province_name VARCHAR(100) DEFAULT NULL COMMENT '省名称',
    city_name VARCHAR(100) DEFAULT NULL COMMENT '市名称',
    county_name VARCHAR(100) DEFAULT NULL COMMENT '县名称',
    township_name VARCHAR(100) DEFAULT NULL COMMENT '乡镇名称',
    community_name VARCHAR(100) NOT NULL COMMENT '社区（行政村）名称',
    has_emergency_plan VARCHAR(10) DEFAULT '否' COMMENT '是否有社区（行政村）应急预案（是/否）',
    has_vulnerable_groups_list VARCHAR(10) DEFAULT '否' COMMENT '是否有本辖区弱势人群清单（是/否）',
    has_disaster_points_list VARCHAR(10) DEFAULT '否' COMMENT '是否有本辖区地质灾害等隐患点清单（是/否）',
    has_disaster_map VARCHAR(10) DEFAULT '否' COMMENT '是否有社区（行政村）灾害类地图（是/否）',
    resident_population INT DEFAULT 0 COMMENT '常住人口数量（人）',
    last_year_funding_amount DECIMAL(12,2) DEFAULT 0.00 COMMENT '上一年度防灾减灾救灾资金投入总金额（万元）',
    materials_equipment_value DECIMAL(12,2) DEFAULT 0.00 COMMENT '现有储备物资、装备折合金额（万元）',
    medical_service_count INT DEFAULT 0 COMMENT '社区医疗卫生服务站或村卫生室数量（个）',
    militia_reserve_count INT DEFAULT 0 COMMENT '民兵预备役人数（人）',
    registered_volunteer_count INT DEFAULT 0 COMMENT '登记注册志愿者人数（人）',
    last_year_training_participants INT DEFAULT 0 COMMENT '上一年度防灾减灾培训活动培训人次（人次）',
    last_year_drill_participants INT DEFAULT 0 COMMENT '参与上一年度组织的防灾减灾演练活动的居民(人次)',
    emergency_shelter_capacity INT DEFAULT 0 COMMENT '本级灾害应急避难场所容量（人）',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT '社区行政村减灾能力数据表';

-- 创建社区减灾能力表索引
CREATE UNIQUE INDEX uk_region_community ON community_disaster_reduction_capacity(region_code, community_name);
CREATE INDEX idx_community_region_code ON community_disaster_reduction_capacity(region_code);
CREATE INDEX idx_community_province_name ON community_disaster_reduction_capacity(province_name);
CREATE INDEX idx_community_city_name ON community_disaster_reduction_capacity(city_name);
CREATE INDEX idx_community_county_name ON community_disaster_reduction_capacity(county_name);
CREATE INDEX idx_community_township_name ON community_disaster_reduction_capacity(township_name);

-- 2. 创建权重配置表
CREATE TABLE weight_config (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    config_name VARCHAR(100) NOT NULL COMMENT '配置名称',
    description TEXT COMMENT '描述',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_deleted TINYINT(1) DEFAULT 0
) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 3. 创建指标权重表
CREATE TABLE indicator_weight (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    config_id BIGINT NOT NULL COMMENT '权重配置ID',
    indicator_code VARCHAR(50) NOT NULL COMMENT '指标编码',
    indicator_name VARCHAR(100) NOT NULL COMMENT '指标名称',
    indicator_level INT NOT NULL COMMENT '指标级别(1-一级指标,2-二级指标)',
    weight DECIMAL(5,4) NOT NULL COMMENT '权重值',
    min_value DECIMAL(10,4) DEFAULT 0.0 COMMENT '最小值',
    max_value DECIMAL(10,4) DEFAULT 100.0 COMMENT '最大值',
    parent_id BIGINT COMMENT '父指标ID',
    sort_order INT DEFAULT 0 COMMENT '排序序号',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (config_id) REFERENCES weight_config(id),
    FOREIGN KEY (parent_id) REFERENCES indicator_weight(id)
) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 创建索引
CREATE INDEX idx_indicator_weight_config ON indicator_weight(config_id);
CREATE INDEX idx_indicator_weight_parent ON indicator_weight(parent_id);
CREATE INDEX idx_indicator_weight_level ON indicator_weight(indicator_level);

-- 4. 创建算法配置表
CREATE TABLE algorithm_config (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    config_name VARCHAR(100) NOT NULL COMMENT '算法配置名称',
    description VARCHAR(255) COMMENT '算法配置描述',
    version VARCHAR(20) DEFAULT '1.0' COMMENT '算法版本',
    status INT DEFAULT 1 COMMENT '状态(1-启用,0-禁用)',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 5. 创建算法步骤表
CREATE TABLE algorithm_step (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    algorithm_config_id BIGINT NOT NULL COMMENT '算法配置ID',
    step_name VARCHAR(100) NOT NULL COMMENT '步骤名称',
    step_code VARCHAR(50) NOT NULL COMMENT '步骤编码',
    step_order INT NOT NULL COMMENT '执行顺序',
    input_data VARCHAR(255) COMMENT '输入数据描述',
    output_data VARCHAR(255) COMMENT '输出数据描述',
    description VARCHAR(500) COMMENT '步骤描述',
    status INT DEFAULT 1 COMMENT '状态(1-启用,0-禁用)',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (algorithm_config_id) REFERENCES algorithm_config(id)
) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 创建索引
CREATE INDEX idx_algorithm_step_config ON algorithm_step(algorithm_config_id);
CREATE INDEX idx_algorithm_step_order ON algorithm_step(step_order);

-- 6. 创建公式配置表
CREATE TABLE formula_config (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    algorithm_step_id BIGINT NOT NULL COMMENT '算法步骤ID',
    formula_name VARCHAR(100) NOT NULL COMMENT '公式名称',
    formula_expression TEXT NOT NULL COMMENT '公式表达式',
    input_variables VARCHAR(500) COMMENT '输入变量列表(JSON格式)',
    output_variable VARCHAR(100) COMMENT '输出变量名',
    description VARCHAR(500) COMMENT '公式描述',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (algorithm_step_id) REFERENCES algorithm_step(id)
) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 创建索引
CREATE INDEX idx_formula_config_step ON formula_config(algorithm_step_id);

-- 7. 创建二级指标结果表
CREATE TABLE secondary_indicator_result (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    survey_data_id BIGINT NOT NULL,
    config_id BIGINT NOT NULL,
    
    -- 二级指标原始计算值（评估指标赋值步骤结果）
    management_capability DECIMAL(10,6) COMMENT '队伍管理能力原始值',
    risk_assessment_capability DECIMAL(10,6) COMMENT '风险评估能力原始值',
    funding_capability DECIMAL(10,6) COMMENT '财政投入能力原始值',
    material_capability DECIMAL(10,6) COMMENT '物资储备能力原始值',
    medical_capability DECIMAL(10,6) COMMENT '医疗保障能力原始值',
    self_rescue_capability DECIMAL(10,6) COMMENT 'selves互救能力原始值',
    public_avoidance_capability DECIMAL(10,6) COMMENT '公众避险能力原始值',
    relocation_capability DECIMAL(10,6) COMMENT '转移安置能力原始值',
    
    -- 二级指标归一化值（属性向量归一化步骤结果）
    management_normalized DECIMAL(10,6) COMMENT '队伍管理能力归一化值',
    risk_assessment_normalized DECIMAL(10,6) COMMENT '风险评估能力归一化值',
    funding_normalized DECIMAL(10,6) COMMENT '财政投入能力归一化值',
    material_normalized DECIMAL(10,6) COMMENT '物资储备能力归一化值',
    medical_normalized DECIMAL(10,6) COMMENT '医疗保障能力归一化值',
    self_rescue_normalized DECIMAL(10,6) COMMENT 'selves互救能力归一化值',
    public_avoidance_normalized DECIMAL(10,6) COMMENT '公众避险能力归一化值',
    relocation_normalized DECIMAL(10,6) COMMENT '转移安置能力归一化值',
    
    calculate_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (survey_data_id) REFERENCES survey_data(id),
    FOREIGN KEY (config_id) REFERENCES weight_config(id)
) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 创建索引
CREATE INDEX idx_secondary_result_survey ON secondary_indicator_result(survey_data_id);
CREATE INDEX idx_secondary_result_config ON secondary_indicator_result(config_id);
CREATE INDEX idx_secondary_result_time ON secondary_indicator_result(calculate_time DESC);

-- 8. 创建一级指标结果表
CREATE TABLE primary_indicator_result (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    secondary_result_id BIGINT NOT NULL,
    
    -- 一级指标计算结果（优劣解算法结果）
    level1_management DECIMAL(10,6) COMMENT '灾害管理能力',
    level1_preparation DECIMAL(10,6) COMMENT '灾害备灾能力', 
    level1_self_rescue DECIMAL(10,6) COMMENT '自救转移能力',
    
    -- 一级指标分级结果
    management_grade VARCHAR(10) COMMENT '灾害管理能力分级',
    preparation_grade VARCHAR(10) COMMENT '灾害备灾能力分级',
    self_rescue_grade VARCHAR(10) COMMENT '自救转移能力分级',
    
    -- 综合减灾能力评估结果
    overall_capability DECIMAL(10,6) COMMENT '综合减灾能力数值',
    overall_grade VARCHAR(10) COMMENT '综合减灾能力分级',
    
    calculate_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (secondary_result_id) REFERENCES secondary_indicator_result(id)
) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 创建索引
CREATE INDEX idx_primary_result_secondary ON primary_indicator_result(secondary_result_id);
CREATE INDEX idx_primary_result_time ON primary_indicator_result(calculate_time DESC);

-- 9. 创建报告表
CREATE TABLE report (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    primary_result_id BIGINT NOT NULL,
    report_name VARCHAR(100) NOT NULL COMMENT '报告名称',
    report_type VARCHAR(20) NOT NULL COMMENT '报告类型(PDF/WORD/MAP)',
    file_path VARCHAR(255) COMMENT '报告文件路径',
    map_image_path VARCHAR(255) COMMENT '专题图路径',
    generate_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (primary_result_id) REFERENCES primary_indicator_result(id)
) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 创建索引
CREATE INDEX idx_report_primary ON report(primary_result_id);
CREATE INDEX idx_report_time ON report(generate_time DESC);

-- ========== 初始化数据 ==========

-- 初始化调查数据
INSERT INTO survey_data (region_code, province, city, county, township, management_staff, population, risk_assessment, funding_amount, material_value, hospital_beds, firefighters, volunteers, militia_reserve, training_participants, shelter_capacity) VALUES
('511425001', '四川', '眉山', '青神', '青竹', 2, 102379, '是', 20, 9, 1010, 26, 1126, 182, 280, 500),
('511425102', '四川', '眉山', '青神', '汉阳', 2, 6335, '是', 70, 3, 22, 5, 322, 7, 900, 1200),
('511425108', '四川', '眉山', '青神', '瑞峰', 52, 8227, '是', 63, 20, 36, 0, 373, 24, 1658, 780),
('511425110', '四川', '眉山', '青神', '西龙', 2, 14051, '是', 20, 7, 22, 0, 81, 55, 320, 500),
('511425112', '四川', '眉山', '青神', '高台', 4, 13786, '是', 93, 2, 28, 0, 702, 348, 672, 1500),
('511425217', '四川', '眉山', '青神', '白果', 2, 13523, '是', 20, 8, 34, 0, 2, 65, 320, 1000),
('511425218', '四川', '眉山', '青神', '罗波', 12, 9689, '是', 150, 10, 30, 0, 94, 106, 300, 5000);

-- 初始化默认权重配置
INSERT INTO weight_config (config_name, description) 
VALUES ('默认权重配置', '系统默认的减灾能力评估指标权重配置');

-- 初始化指标权重数据
-- 一级指标
INSERT INTO indicator_weight (config_id, indicator_code, indicator_name, indicator_level, weight, parent_id, sort_order) VALUES
(1, 'L1_MANAGEMENT', '灾害管理能力', 1, 0.33, NULL, 1),
(1, 'L1_PREPARATION', '灾害备灾能力', 1, 0.32, NULL, 2),
(1, 'L1_SELF_RESCUE', '自救转移能力', 1, 0.35, NULL, 3);

-- 二级指标(灾害管理能力下的子指标)
INSERT INTO indicator_weight (config_id, indicator_code, indicator_name, indicator_level, weight, parent_id, sort_order) VALUES
(1, 'L2_MANAGEMENT_CAPABILITY', '队伍管理能力', 2, 0.37, 1, 1),
(1, 'L2_RISK_ASSESSMENT', '风险评估能力', 2, 0.31, 1, 2),
(1, 'L2_FUNDING', '财政投入能力', 2, 0.32, 1, 3);

-- 二级指标(灾害备灾能力下的子指标)
INSERT INTO indicator_weight (config_id, indicator_code, indicator_name, indicator_level, weight, parent_id, sort_order) VALUES
(1, 'L2_MATERIAL', '物资储备能力', 2, 0.51, 2, 1),
(1, 'L2_MEDICAL', '医疗保障能力', 2, 0.49, 2, 2);

-- 二级指标(selves互救能力下的子指标)
INSERT INTO indicator_weight (config_id, indicator_code, indicator_name, indicator_level, weight, parent_id, sort_order) VALUES
(1, 'L2_SELF_RESCUE', 'selves互救能力', 2, 0.33, 3, 1),
(1, 'L2_PUBLIC_AVOIDANCE', '公众避险能力', 2, 0.33, 3, 2),
(1, 'L2_RELOCATION', '转移安置能力', 2, 0.34, 3, 3);

-- 初始化默认算法配置
INSERT INTO algorithm_config (config_name, description, version) 
VALUES ('默认减灾能力评估算法', '标准的减灾能力评估算法流程配置', '1.0');

-- 初始化默认算法步骤
INSERT INTO algorithm_step (algorithm_config_id, step_name, step_code, step_order, input_data, output_data, description) VALUES
(1, '二级指标计算', 'SECONDARY_CALCULATION', 1, '调查数据', '二级指标原始值', '根据调查数据计算8个二级指标的原始值'),
(1, '属性向量归一化', 'NORMALIZATION', 2, '二级指标原始值', '二级指标归一化值', '对二级指标进行向量归一化处理'),
(1, '二级指标定权', 'SECONDARY_WEIGHTING', 3, '二级指标归一化值,指标权重', '二级指标定权值', '将归一化值与二级指标权重相乘'),
(1, '优劣解算法计算', 'TOPSIS_CALCULATION', 4, '二级指标定权值', '一级指标值', '基于TOPSIS优劣解算法计算一级指标'),
(1, '能力分级计算', 'GRADING_CALCULATION', 5, '一级指标值', '能力分级结果', '根据均值和标准差计算能力分级');

-- 初始化默认公式配置
INSERT INTO formula_config (algorithm_step_id, formula_name, formula_expression, input_variables, output_variable, description) VALUES
-- 二级指标计算公式
(1, '队伍管理能力计算', '(management_staff/population)*10000', '["management_staff","population"]', 'management_capability', '队伍管理能力=(本级灾害管理工作人员总数/常住人口数量)*10000'),
(1, '风险评估能力计算', 'IF(risk_assessment="是",1,0)', '["risk_assessment"]', 'risk_assessment_capability', '风险评估能力=IF(是否开展风险评估="是",1,0)'),
(1, '财政投入能力计算', '(funding_amount/population)*10000', '["funding_amount","population"]', 'funding_capability', '财政投入能力=(防灾减灾救灾资金投入总金额/常住人口数量)*10000'),
(1, '物资储备能力计算', '(material_value/population)*10000', '["material_value","population"]', 'material_capability', '物资储备能力=(现有储备物资装备折合金额/常住人口数量)*10000'),
(1, '医疗保障能力计算', '(hospital_beds/population)*10000', '["hospital_beds","population"]', 'medical_capability', '医疗保障能力=(实有住院床位数/常住人口数量)*10000'),
(1, 'selves互救能力计算', '((firefighters+volunteers+militia_reserve)/population)*10000', '["firefighters","volunteers","militia_reserve","population"]', 'self_rescue_capability', '自救互救能力=(消防员数量+志愿者人数+民兵预备役人数)/常住人口数量)*10000'),
(1, '公众避险能力计算', '(training_participants/population)*10000', '["training_participants","population"]', 'public_avoidance_capability', '公众避险能力=(应急管理培训和演练参与人次/常住人口数量)*10000'),
(1, '转移安置能力计算', '(shelter_capacity/population)*10000', '["shelter_capacity","population"]', 'relocation_capability', '转移安置能力=(本级灾害应急避难场所容量/常住人口数量)*10000'),
-- 归一化公式
(2, '属性向量归一化公式', 'value/SQRT(SUMSQ(all_values))', '["value","all_values"]', 'normalized_value', '归一化值=原始值/SQRT(SUMSQ(所有原始值))'),
-- 定权公式
(3, '二级指标定权公式', 'normalized_value*weight', '["normalized_value","weight"]', 'weighted_value', '定权值=归一化值*权重'),
-- 优劣解算法公式
(4, '正理想解距离公式', 'SQRT(SUMSQ(max_values-current_values))', '["max_values","current_values"]', 'positive_distance', '正理想解距离=SQRT(SUMSQ(最大值-当前值))'),
(4, '负理想解距离公式', 'SQRT(SUMSQ(min_values-current_values))', '["min_values","current_values"]', 'negative_distance', '负理想解距离=SQRT(SUMSQ(最小值-当前值))'),
(4, 'TOPSIS得分公式', 'negative_distance/(negative_distance+positive_distance)', '["negative_distance","positive_distance"]', 'topsis_score', 'TOPSIS得分=负理想解距离/(负理想解距离+正理想解距离)'),
-- 分级公式
(5, '能力分级公式', 'IF(mean<=0.5*stdev,IF(value>=mean+1.5*stdev,"强",IF(value>=mean+0.5*stdev,"较强","中等")),IF(mean<=1.5*stdev,IF(value>=mean+1.5*stdev,"强",IF(value>=mean+0.5*stdev,"较强",IF(value>=mean-0.5*stdev,"中等","较弱"))),IF(value>=mean+1.5*stdev,"强",IF(value>=mean+0.5*stdev,"较强",IF(value>=mean-0.5*stdev,"中等",IF(value>=mean-1.5*stdev,"较弱","弱"))))))', '["value","mean","stdev"]', 'grade', '基于均值和标准差的五级分类公式');

-- 初始化完成
SELECT 'Database initialization completed successfully!' as message;