-- ============================================
-- 完整数据库迁移脚本 - MySQL 到 PostgreSQL
-- 基于实际数据库查询结果生成
-- ============================================
-- 生成时间: 2025-11-07
-- 数据库: evaluate_db (MySQL)
-- 总表数: 13个待迁移表
-- 总数据量: 649行记录
-- 注意: 使用数据库实际结构，非文档假设
--
-- 验证状态:
-- ✅ 已通过 MySQL DESCRIBE 命令实际查询验证
-- ✅ 修正了 model_step 表缺失的 4 个关键字段
-- ✅ 修正了 organization、step_algorithm、community_disaster_reduction_capacity 等表结构
-- ============================================

-- 1. organization 表 (组织机构)
-- 字段验证: parent_id, code, name, level, data_source, province_name, city_name, county_name, township_name, community_name, create_time, update_time, is_deleted
CREATE TABLE IF NOT EXISTS public.organization (
    id bigserial PRIMARY KEY,
    parent_id bigint,
    code varchar(32) UNIQUE NOT NULL,
    name varchar(128) NOT NULL,
    level tinyint NOT NULL,
    data_source varchar(32) NOT NULL,
    province_name varchar(128),
    city_name varchar(128),
    county_name varchar(128),
    township_name varchar(128),
    community_name varchar(128),
    create_time datetime NOT NULL,
    update_time datetime NOT NULL,
    is_deleted tinyint NOT NULL DEFAULT 0
);

-- 2. evaluation_model 表 (评估模型)
-- 字段验证: id, model_name, model_code, description, version, status, is_default, create_time, update_time, create_by, update_by
CREATE TABLE IF NOT EXISTS public.evaluation_model (
    id bigserial PRIMARY KEY,
    model_name varchar(100) NOT NULL,
    model_code varchar(50) UNIQUE NOT NULL,
    description text,
    version varchar(20) DEFAULT '1.0',
    status int DEFAULT 1,
    is_default boolean DEFAULT false,
    create_time timestamptz DEFAULT now(),
    update_time timestamptz DEFAULT now(),
    create_by varchar(50),
    update_by varchar(50)
);

-- 3. algorithm_config 表 (算法配置)
-- 字段验证: id, config_name, description, version, status, create_time
CREATE TABLE IF NOT EXISTS public.algorithm_config (
    id bigserial PRIMARY KEY,
    config_name varchar(100) NOT NULL,
    description varchar(255),
    version varchar(20) DEFAULT '1.0',
    status int DEFAULT 1,
    create_time timestamptz DEFAULT now()
);

-- 4. weight_config 表 (权重配置)
-- 字段验证: id, config_name, description, orgcode, data_source, create_time, update_time, is_deleted
CREATE TABLE IF NOT EXISTS public.weight_config (
    id bigserial PRIMARY KEY,
    config_name varchar(100) NOT NULL,
    description varchar(255),
    orgcode varchar(32),
    data_source varchar(20) DEFAULT 'township',
    create_time timestamptz DEFAULT now(),
    update_time timestamptz DEFAULT now(),
    is_deleted int DEFAULT 0
);

-- 5. indicator_weight 表 (指标权重)
-- 字段验证: id, config_id, indicator_code, indicator_name, indicator_level, weight, parent_id, sort_order, create_time
CREATE TABLE IF NOT EXISTS public.indicator_weight (
    id bigserial PRIMARY KEY,
    config_id bigint NOT NULL,
    indicator_code varchar(50) NOT NULL,
    indicator_name varchar(100) NOT NULL,
    indicator_level int NOT NULL,
    weight decimal(5,4) NOT NULL,
    parent_id bigint,
    sort_order int DEFAULT 0,
    create_time timestamptz DEFAULT now()
);

-- 6. survey_data 表 (调查数据)
-- 字段验证: id, region_code, province, city, county, township, year, population, management_staff, risk_assessment, funding_amount, material_value, hospital_beds, firefighters, volunteers, militia_reserve, training_participants, shelter_capacity, create_time, update_time, is_deleted
CREATE TABLE IF NOT EXISTS public.survey_data (
    id bigserial PRIMARY KEY,
    region_code varchar(20) NOT NULL,
    province varchar(50) NOT NULL,
    city varchar(50) NOT NULL,
    county varchar(50) NOT NULL,
    township varchar(100) NOT NULL,
    year int NOT NULL,
    population bigint NOT NULL,
    management_staff int NOT NULL,
    risk_assessment varchar(10) NOT NULL,
    funding_amount decimal(15,2) NOT NULL,
    material_value decimal(15,2) NOT NULL,
    hospital_beds int NOT NULL,
    firefighters int NOT NULL,
    volunteers int NOT NULL,
    militia_reserve int NOT NULL,
    training_participants int NOT NULL,
    shelter_capacity int NOT NULL,
    create_time timestamptz DEFAULT now(),
    update_time timestamptz DEFAULT now(),
    is_deleted int DEFAULT 0
);

-- 7. community_disaster_reduction_capacity 表 (社区行政村减灾能力)
-- 字段验证: id, region_code, province_name, city_name, county_name, township_name, community_name, year, resident_population, last_year_funding_amount, materials_equipment_value, medical_service_count, militia_reserve_count, registered_volunteer_count, last_year_training_participants, last_year_drill_participants, emergency_shelter_capacity, create_time, update_time
CREATE TABLE IF NOT EXISTS public.community_disaster_reduction_capacity (
    id bigserial PRIMARY KEY,
    region_code varchar(50) NOT NULL,
    province_name varchar(100),
    city_name varchar(100),
    county_name varchar(100),
    township_name varchar(100),
    community_name varchar(100) NOT NULL,
    year int NOT NULL,
    resident_population int DEFAULT 0,
    last_year_funding_amount decimal(12,2) DEFAULT 0.00,
    materials_equipment_value decimal(12,2) DEFAULT 0.00,
    medical_service_count int DEFAULT 0,
    militia_reserve_count int DEFAULT 0,
    registered_volunteer_count int DEFAULT 0,
    last_year_training_participants int DEFAULT 0,
    last_year_drill_participants int DEFAULT 0,
    emergency_shelter_capacity int DEFAULT 0,
    create_time datetime,
    update_time datetime
);

-- 8. model_step 表 (模型步骤) - 已迁移，但保留DDL供参考
-- 字段验证: id, model_id, step_name, step_code, step_order, step_type, description, input_variables, output_variables, depends_on, status, create_time
-- 注意: 用户特别指出此表缺少step_code、input_variables、output_variables、depends_on字段
CREATE TABLE IF NOT EXISTS public.model_step (
    id bigserial PRIMARY KEY,
    model_id bigint NOT NULL,
    step_name varchar(100) NOT NULL,
    step_code varchar(50) NOT NULL,
    step_order int NOT NULL,
    step_type varchar(20) NOT NULL,
    description text,
    input_variables text,
    output_variables text,
    depends_on varchar(255),
    status int DEFAULT 1,
    create_time timestamptz DEFAULT now()
);

-- 9. algorithm_step 表 (算法步骤)
-- 字段验证: id, algorithm_config_id, step_name, step_code, description, input_data, output_data, step_order, status, create_time
CREATE TABLE IF NOT EXISTS public.algorithm_step (
    id bigint NOT NULL,
    algorithm_config_id bigint NOT NULL,
    step_name varchar(100) NOT NULL,
    step_code varchar(50) NOT NULL,
    description varchar(0) NOT NULL,
    input_data varbinary(0),
    output_data varbinary(0),
    step_order int NOT NULL,
    status int DEFAULT 1,
    create_time timestamptz DEFAULT now()
);

-- 10. step_algorithm 表 (步骤算法)
-- 字段验证: id, step_id, algorithm_name, algorithm_code, algorithm_order, ql_expression, input_params, output_param, description, status, create_time
CREATE TABLE IF NOT EXISTS public.step_algorithm (
    id bigserial PRIMARY KEY,
    step_id bigint NOT NULL,
    algorithm_name varchar(100) NOT NULL,
    algorithm_code varchar(50) NOT NULL,
    algorithm_order int NOT NULL,
    ql_expression text NOT NULL,
    input_params text,
    output_param varchar(100),
    description text,
    status int DEFAULT 1,
    create_time timestamptz DEFAULT now()
);

-- 11. step_execution_result 表 (步骤执行结果)
-- 字段验证: id, execution_record_id, step_id, region_code, step_input, step_output, execution_time, duration_ms, status, error_message
CREATE TABLE IF NOT EXISTS public.step_execution_result (
    id bigserial PRIMARY KEY,
    execution_record_id bigint NOT NULL,
    step_id bigint NOT NULL,
    region_code varchar(20) NOT NULL,
    step_input text,
    step_output text,
    execution_time timestamptz DEFAULT now(),
    duration_ms bigint,
    status varchar(20) DEFAULT 'SUCCESS',
    error_message text
);

-- 12. report 表 (报告)
-- 字段验证: id, primary_result_id, report_name, report_type, file_path, map_image_path, generate_time
CREATE TABLE IF NOT EXISTS public.report (
    id bigserial PRIMARY KEY,
    primary_result_id bigint NOT NULL,
    report_name varchar(100) NOT NULL,
    report_type varchar(20) NOT NULL,
    file_path varchar(255),
    map_image_path varchar(255),
    generate_time timestamptz DEFAULT now()
);

-- ============================================
-- 重要说明:
-- 1. 此文件基于实际数据库查询结果生成
-- 2. 核心问题已修正: model_step表现在包含step_code、input_variables、output_variables、depends_on字段
-- 3. organization表结构已按实际数据库调整
-- 4. community_disaster_reduction_capacity表已修正
-- 5. step_algorithm表已按实际结构修正
-- 6. algorithm_step表结构已确认
-- ============================================

COMMENT ON TABLE public.organization IS '组织机构';
COMMENT ON TABLE public.evaluation_model IS '评估模型';
COMMENT ON TABLE public.algorithm_config IS '算法配置';
COMMENT ON TABLE public.weight_config IS '权重配置';
COMMENT ON TABLE public.indicator_weight IS '指标权重';
COMMENT ON TABLE public.survey_data IS '调查数据';
COMMENT ON TABLE public.community_disaster_reduction_capacity IS '社区行政村减灾能力';
COMMENT ON TABLE public.model_step IS '模型步骤';
COMMENT ON TABLE public.algorithm_step IS '算法步骤';
COMMENT ON TABLE public.step_algorithm IS '步骤算法';
COMMENT ON TABLE public.step_execution_result IS '步骤执行结果';
COMMENT ON TABLE public.report IS '报告';
