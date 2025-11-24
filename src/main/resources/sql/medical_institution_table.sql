-- 医疗卫生机构表 (MySQL版本)
CREATE TABLE IF NOT EXISTS medical_institution (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    unique_code VARCHAR(100) NOT NULL UNIQUE COMMENT '唯一码',
    verification_status VARCHAR(20) NOT NULL DEFAULT '待核实' COMMENT '核实状态',
    unified_social_credit_code VARCHAR(50) COMMENT '统一社会信用代码/机构编码',
    code_type VARCHAR(50) COMMENT '代码类型',
    institution_name VARCHAR(500) NOT NULL COMMENT '医疗卫生机构名称',
    institution_address VARCHAR(1000) COMMENT '医疗卫生机构详细地址',
    institution_category_code VARCHAR(10) COMMENT '医疗卫生机构类别代码',
    institution_type_large VARCHAR(100) COMMENT '医疗机构类型（大类）',
    institution_type_medium VARCHAR(100) COMMENT '医疗机构类型（中类）',
    institution_type_specialized VARCHAR(100) COMMENT '医疗机构类型（专科医院分类）',
    hospital_level VARCHAR(100) COMMENT '医院等级',
    institution_nature VARCHAR(50) COMMENT '医疗机构性质',
    land_area DECIMAL(10,2) COMMENT '占地面积（平方米）',
    building_area DECIMAL(10,2) COMMENT '房屋建筑面积（平方米）',
    equipment_count_above_10k INT COMMENT '万元以上设备台数',
    total_staff INT COMMENT '在岗职工人数',
    health_technical_personnel INT COMMENT '卫生技术人员总数',
    registered_nurses INT COMMENT '注册护士人数',
    logistics_skill_personnel INT COMMENT '工勤技能人员数',
    annual_total_visits INT COMMENT '年度总诊疗人次数',
    annual_admission_count INT COMMENT '年度入院人数',
    annual_discharge_count INT COMMENT '年度出院人数',
    actual_hospital_beds INT COMMENT '实有住院床位数',
    negative_pressure_beds INT COMMENT '负压病房床位数',
    icu_beds INT COMMENT '重症加强护理病房（ICU）床位数',
    pre_hospital_emergency_personnel INT COMMENT '院前急救专业人员数',
    emergency_command_vehicle_count INT COMMENT '急救指挥车数量',
    transport_ambulance_count INT COMMENT '运转型急救车数量',
    monitor_ambulance_count INT COMMENT '监护型急救车数量',
    negative_pressure_ambulance_count INT COMMENT '负压急救车数量',
    blood_collection_vehicle_count INT COMMENT '采血车数',
    blood_delivery_vehicle_count INT COMMENT '送血车数',
    security_personnel_count INT COMMENT '安全保卫人员数量',
    emergency_power_supply VARCHAR(100) COMMENT '应急供电能力',
    emergency_power_supply_other VARCHAR(500) COMMENT '应急供电能力-其他项说明',
    water_supply_mode VARCHAR(100) COMMENT '供水方式',
    heating_mode VARCHAR(100) COMMENT '供暖方式',
    emergency_communication_mode VARCHAR(100) COMMENT '应急通信保障方式',
    emergency_communication_mode_other VARCHAR(500) COMMENT '应急通信保障方式-其他项说明',
    disaster_history_type VARCHAR(200) COMMENT '曾经遭受过的自然灾害类型',
    disaster_history_type_other VARCHAR(500) COMMENT '曾经遭受过的自然灾害类型-其他说明',
    emergency_plan_type VARCHAR(200) COMMENT '已有自然灾害应急预案类型',
    emergency_plan_type_other VARCHAR(500) COMMENT '已有自然灾害应急预案类型-其他说明',
    unit_leader VARCHAR(100) COMMENT '单位负责人',
    statistical_leader VARCHAR(100) COMMENT '统计负责人',
    form_filler VARCHAR(100) COMMENT '填表人',
    contact_phone VARCHAR(50) COMMENT '联系电话',
    report_date DATE COMMENT '报出日期',
    filling_instructions TEXT COMMENT '填写说明',
    year INT NOT NULL COMMENT '数据年份',
    org_code VARCHAR(50) COMMENT '组织机构代码',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_by VARCHAR(100) COMMENT '创建人',
    update_by VARCHAR(100) COMMENT '更新人'
) COMMENT='医疗卫生机构表';

-- 创建索引
CREATE INDEX idx_medical_institution_year ON medical_institution(year);
CREATE INDEX idx_medical_institution_org_code ON medical_institution(org_code);
CREATE INDEX idx_medical_institution_verification_status ON medical_institution(verification_status);
CREATE INDEX idx_medical_institution_institution_type_large ON medical_institution(institution_type_large);
CREATE INDEX idx_medical_institution_unified_social_credit_code ON medical_institution(unified_social_credit_code);
CREATE INDEX idx_medical_institution_create_time ON medical_institution(create_time);

-- 插入示例数据（基于用户提供的数据）
INSERT INTO medical_institution (
    unique_code, verification_status, unified_social_credit_code, code_type, institution_name,
    institution_address, institution_category_code, institution_type_large, institution_type_medium,
    institution_type_specialized, hospital_level, institution_nature, land_area, building_area,
    equipment_count_above_10k, total_staff, health_technical_personnel, registered_nurses,
    logistics_skill_personnel, annual_total_visits, annual_admission_count, annual_discharge_count,
    actual_hospital_beds, negative_pressure_beds, icu_beds, pre_hospital_emergency_personnel,
    emergency指挥车_count, transport_ambulance_count, monitor_ambulance_count,
    negative_pressure_ambulance_count, blood_collection_vehicle_count, blood_delivery_vehicle_count,
    security_personnel_count, emergency_power_supply, emergency_power_supply_other,
    water_supply_mode, heating_mode, emergency_communication_mode,
    emergency_communication_mode_other, disaster_history_type, disaster_history_type_other,
    emergency_plan_type, emergency_plan_type_other, unit_leader, statistical_leader,
    form_filler, contact_phone, report_date, year
) VALUES
(
    'afc97759-e9c4-11eb-9cad-ab1ae14cae2a', '核实更新', '1251172645171060X8', '统一社会信用代码',
    '青神县罗波乡卫生院', '四川省眉山市青神县罗波乡新桥社区罗宝街48号', 'C220', '基层医疗机构',
    '乡镇(街道)卫生院', '非专科医院', '甲等;一级', '公立', 1600.00, 1245.00,
    14, 19, 18, 8, 1, 23360, 695, 706, 30, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
    '1-19KW', '', '网管供水', '其他', '公共通信线路（固定或移动电话）', '',
    '无灾害记录', '', '洪涝灾害应急预案;地震灾害应急预案', '', '章亚群', '张林',
    '张坤燕', '13795549601', '2024-10-25', 2024
),
(
    'afc9775c-e9c4-11eb-9cad-ab1ae14cae2a', '核实更新', '12511726451710490C', '统一社会信用代码',
    '青神县青竹街道社区卫生服务中心（原青神县青竹街道南城卫生院）', '四川省眉山市青神县青竹街道兰沟村4组下牛市街159号', 'B100', '基层医疗机构',
    '社区卫生服务中心（站）', '非专科医院', '未定等;未定级', '公立', 748.31, 1416.00,
    0, 4, 3, 1, 1, 7090, 30, 30, 3, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
    '1-19KW', '', '网管供水', '无供暖设施', '公共通信线路（固定或移动电话）', '',
    '无灾害记录', '', '地震灾害应急预案', '', '李羚菱', '夏凌',
    '唐希', '18608229972', '2024-10-23', 2024
),
(
    'afc97757-e9c4-11eb-9cad-ab1ae14cae2a', '核实更新', '1251172645171036XD', '统一社会信用代码',
    '青神县青竹街道社区卫生服务中心', '四川省眉山市青神县青竹街道凤阳社区川主庙街73-1号', 'B100', '基层医疗机构',
    '社区卫生服务中心（站）', '非专科医院', '未定等;未定级', '公立', 1495.00, 1547.00,
    13, 49, 42, 17, 7, 36699, 102, 102, 15, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1, 1,
    '1-19KW', '', '网管供水', '其他', '公共通信线路（固定或移动电话）', '',
    '无灾害记录', '', '地震灾害应急预案', '', '李羚菱', '夏凌',
    '唐希', '18608229972', '2024-10-23', 2024
);