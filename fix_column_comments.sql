-- 修复survey_data表的中文注释乱码问题
USE evaluate_db;

-- 设置字符集
SET NAMES utf8mb4;

-- 修复列注释
ALTER TABLE survey_data
  MODIFY COLUMN unique_id VARCHAR(100) NULL COMMENT '唯一码',
  MODIFY COLUMN verification_status VARCHAR(20) NULL COMMENT '核实状态',
  MODIFY COLUMN province VARCHAR(50) NULL COMMENT '省名称',
  MODIFY COLUMN city VARCHAR(50) NULL COMMENT '市名称',
  MODIFY COLUMN county VARCHAR(50) NULL COMMENT '县名称',
  MODIFY COLUMN township VARCHAR(100) NULL COMMENT '乡镇名称',
  MODIFY COLUMN township_address VARCHAR(500) NULL COMMENT '乡镇（街道）地址',
  MODIFY COLUMN year INT NULL COMMENT '数据所属年份',
  MODIFY COLUMN population BIGINT NULL COMMENT '常住人口数量',
  MODIFY COLUMN total_households INT NULL COMMENT '年末总户数(户)',
  MODIFY COLUMN main_disaster_types VARCHAR(200) NULL COMMENT '影响乡镇（街道）的主要灾害类型',
  MODIFY COLUMN disaster_types_other VARCHAR(500) NULL COMMENT '影响乡镇（街道）的主要灾害类型-其他项说明',
  MODIFY COLUMN management_staff INT NULL COMMENT '本级灾害管理工作人员总数',
  MODIFY COLUMN disaster_info_staff INT NULL COMMENT '本级灾害信息员人数',
  MODIFY COLUMN risk_assessment VARCHAR(10) NULL COMMENT '是否开展乡镇（街道）灾害风险评估',
  MODIFY COLUMN has_disaster_map VARCHAR(10) NULL COMMENT '是否有乡镇（街道）灾害类地图',
  MODIFY COLUMN warning_receive_method VARCHAR(200) NULL COMMENT '灾害预警信息接收方式',
  MODIFY COLUMN warning_receive_method_other VARCHAR(500) NULL COMMENT '灾害预警信息接收方式-其他项说明',
  MODIFY COLUMN warning_communication_method VARCHAR(200) NULL COMMENT '灾害预警信息传达方式',
  MODIFY COLUMN warning_communication_method_other VARCHAR(500) NULL COMMENT '灾害预警信息传达方式-其他项说明',
  MODIFY COLUMN disaster_report_method VARCHAR(200) NULL COMMENT '灾情信息上报方式',
  MODIFY COLUMN disaster_report_method_other VARCHAR(500) NULL COMMENT '灾情信息上报方式-其他项说明',
  MODIFY COLUMN emergency_plan_count INT NULL COMMENT '近3年编制或修订自然灾害应急预案数量(个)',
  MODIFY COLUMN emergency_response_count INT NULL COMMENT '近3年针对自然灾害启动应急响应次数(次)',
  MODIFY COLUMN training_drill_count INT NULL COMMENT '上一年度组织的应急管理培训和演练次数(次)',
  MODIFY COLUMN training_participants INT NULL COMMENT '上一年度组织的应急管理培训和演练参与人次',
  MODIFY COLUMN funding_support_method VARCHAR(50) NULL COMMENT '乡镇（街道）综合减灾工作经费保障方式',
  MODIFY COLUMN funding_support_method_other VARCHAR(500) NULL COMMENT '乡镇（街道）综合减灾工作经费保障方式-其他说明',
  MODIFY COLUMN funding_amount DECIMAL(15,2) NULL COMMENT '上一年度防灾减灾救灾资金投入总金额(万元)',
  MODIFY COLUMN material_storage_method VARCHAR(100) NULL COMMENT '救灾物资储备方式',
  MODIFY COLUMN material_storage_method_other VARCHAR(500) NULL COMMENT '救灾物资储备方式-其他项说明',
  MODIFY COLUMN storage_point_count INT NULL COMMENT '本级救灾物资、装备储备点数量(个)',
  MODIFY COLUMN storage_equipment_count INT NULL COMMENT '本级储备点救灾物资、装备数量(套/个/件)',
  MODIFY COLUMN emergency_power_count INT NULL COMMENT '其中：应急电源或应急发电设备数量(套或件)',
  MODIFY COLUMN emergency_communication_count INT NULL COMMENT '应急通信设备数量(套或件)',
  MODIFY COLUMN emergency_water_count INT NULL COMMENT '应急供水设备数量(套或件)',
  MODIFY COLUMN emergency_medical_count INT NULL COMMENT '应急医疗设备数量(套或件)',
  MODIFY COLUMN material_value DECIMAL(15,2) NULL COMMENT '现有储备物资、装备折合金额(万元)',
  MODIFY COLUMN shelter_count INT NULL COMMENT '本级灾害应急避难场所数量(个或处)',
  MODIFY COLUMN shelter_capacity INT NULL COMMENT '本级灾害应急避难场所容量',
  MODIFY COLUMN unit_leader VARCHAR(100) NULL COMMENT '单位负责人',
  MODIFY COLUMN statistics_leader VARCHAR(100) NULL COMMENT '统计负责人',
  MODIFY COLUMN form_filler VARCHAR(100) NULL COMMENT '填表人',
  MODIFY COLUMN contact_phone VARCHAR(50) NULL COMMENT '联系电话',
  MODIFY COLUMN report_date DATE NULL COMMENT '报出日期(年/月/日)',
  MODIFY COLUMN fill_instructions TEXT NULL COMMENT '填写说明';

SELECT 'Column comments fixed successfully!' AS result;