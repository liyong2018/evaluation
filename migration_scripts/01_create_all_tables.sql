-- ============================================
-- 完整数据库迁移脚本 - MySQL 到 PostgreSQL
-- ============================================

-- 1. organization 表 (组织机构)
CREATE TABLE IF NOT EXISTS public.organization (
    id bigserial PRIMARY KEY,
    parent_id bigint,
    code varchar(32) UNIQUE NOT NULL,
    name varchar(100) NOT NULL,
    level int DEFAULT 1,
    full_name varchar(255),
    status int DEFAULT 1,
    sort_order int DEFAULT 0,
    create_time timestamptz DEFAULT now(),
    update_time timestamptz DEFAULT now(),
    create_by varchar(50),
    update_by varchar(50),
    is_deleted int DEFAULT 0
);

-- 2. evaluation_model 表 (评估模型)
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
CREATE TABLE IF NOT EXISTS public.algorithm_config (
    id bigserial PRIMARY KEY,
    config_name varchar(100) NOT NULL,
    description text,
    version varchar(20) DEFAULT '1.0',
    algorithm_type varchar(50),
    parameters text,
    status int DEFAULT 1,
    create_time timestamptz DEFAULT now(),
    update_time timestamptz DEFAULT now(),
    create_by varchar(50),
    update_by varchar(50)
);

-- 4. weight_config 表 (权重配置)
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
CREATE TABLE IF NOT EXISTS public.community_disaster_reduction_capacity (
    id bigserial PRIMARY KEY,
    region_code varchar(20) NOT NULL,
    community_name varchar(100) NOT NULL,
    region_level varchar(20),
    population bigint DEFAULT 0,
    households int DEFAULT 0,
    elderly_count int DEFAULT 0,
    disabled_count int DEFAULT 0,
    children_count int DEFAULT 0,
    evacuation_shelters int DEFAULT 0,
    shelter_capacity int DEFAULT 0,
    emergency_kits int DEFAULT 0,
    training_sessions int DEFAULT 0,
    training_participants int DEFAULT 0,
    volunteers int DEFAULT 0,
    rescue_equipment_value decimal(15,2) DEFAULT 0,
    annual_budget decimal(15,2) DEFAULT 0,
    year int,
    create_time timestamptz DEFAULT now(),
    update_time timestamptz DEFAULT now(),
    is_deleted int DEFAULT 0
);

-- 8. model_step 表 (模型步骤)
CREATE TABLE IF NOT EXISTS public.model_step (
    id bigserial PRIMARY KEY,
    model_id bigint NOT NULL,
    step_order int NOT NULL,
    step_name varchar(100) NOT NULL,
    step_type varchar(50),
    description text,
    formula_config text,
    status int DEFAULT 1,
    create_time timestamptz DEFAULT now(),
    update_time timestamptz DEFAULT now()
);

-- 9. algorithm_step 表 (算法步骤)
CREATE TABLE IF NOT EXISTS public.algorithm_step (
    id bigserial PRIMARY KEY,
    config_id bigint NOT NULL,
    step_order int NOT NULL,
    step_name varchar(100) NOT NULL,
    step_type varchar(50),
    description text,
    formula text,
    parameters text,
    status int DEFAULT 1,
    create_time timestamptz DEFAULT now(),
    update_time timestamptz DEFAULT now()
);

-- 10. step_algorithm 表 (步骤算法)
CREATE TABLE IF NOT EXISTS public.step_algorithm (
    id bigserial PRIMARY KEY,
    step_id bigint NOT NULL,
    algorithm_id bigint NOT NULL,
    execution_order int DEFAULT 1,
    parameters text,
    create_time timestamptz DEFAULT now()
);

-- 11. step_execution_result 表 (步骤执行结果)
CREATE TABLE IF NOT EXISTS public.step_execution_result (
    id bigserial PRIMARY KEY,
    execution_record_id bigint NOT NULL,
    step_id bigint NOT NULL,
    step_name varchar(100),
    region_code varchar(20),
    result_data text,
    execution_status varchar(20),
    execute_time timestamptz,
    create_time timestamptz DEFAULT now()
);

-- 12. report 表 (报告)
CREATE TABLE IF NOT EXISTS public.report (
    id bigserial PRIMARY KEY,
    report_name varchar(100) NOT NULL,
    report_type varchar(50),
    template_path varchar(255),
    file_path varchar(255),
    file_name varchar(255),
    file_size bigint,
    status int DEFAULT 0,
    create_time timestamptz DEFAULT now(),
    update_time timestamptz DEFAULT now(),
    create_by varchar(50)
);

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
