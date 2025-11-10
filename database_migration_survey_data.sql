-- 乡镇评估表 survey_data 结构调整
-- 添加缺失的字段以支持新的乡镇评估需求

USE evaluate_db;

-- 1. 添加唯一码字段
ALTER TABLE survey_data
ADD COLUMN unique_id VARCHAR(100) NULL COMMENT '唯一码' AFTER id;

-- 2. 添加核实状态字段
ALTER TABLE survey_data
ADD COLUMN verification_status VARCHAR(20) NULL COMMENT '核实状态' AFTER region_code;

-- 3. 添加乡镇地址字段
ALTER TABLE survey_data
ADD COLUMN township_address VARCHAR(500) NULL COMMENT '乡镇（街道）地址' AFTER township;

-- 4. 添加年末总户数字段
ALTER TABLE survey_data
ADD COLUMN total_households INT NULL COMMENT '年末总户数(户)' AFTER population;

-- 5. 添加主要灾害类型字段
ALTER TABLE survey_data
ADD COLUMN main_disaster_types VARCHAR(200) NULL COMMENT '影响乡镇（街道）的主要灾害类型' AFTER total_households;

-- 6. 添加主要灾害类型其他说明字段
ALTER TABLE survey_data
ADD COLUMN disaster_types_other VARCHAR(500) NULL COMMENT '影响乡镇（街道）的主要灾害类型-其他项说明' AFTER main_disaster_types;

-- 7. 添加本级灾害信息员人数字段
ALTER TABLE survey_data
ADD COLUMN disaster_info_staff INT NULL COMMENT '本级灾害信息员人数' AFTER management_staff;

-- 8. 添加是否有乡镇灾害类地图字段
ALTER TABLE survey_data
ADD COLUMN has_disaster_map VARCHAR(10) NULL COMMENT '是否有乡镇（街道）灾害类地图(是/否)' AFTER risk_assessment;

-- 9. 添加灾害预警信息接收方式字段
ALTER TABLE survey_data
ADD COLUMN warning_receive_method VARCHAR(200) NULL COMMENT '灾害预警信息接收方式' AFTER has_disaster_map;

-- 10. 添加灾害预警信息接收方式其他说明字段
ALTER TABLE survey_data
ADD COLUMN warning_receive_method_other VARCHAR(500) NULL COMMENT '灾害预警信息接收方式-其他项说明' AFTER warning_receive_method;

-- 11. 添加灾害预警信息传达方式字段
ALTER TABLE survey_data
ADD COLUMN warning_communication_method VARCHAR(200) NULL COMMENT '灾害预警信息传达方式' AFTER warning_receive_method_other;

-- 12. 添加灾害预警信息传达方式其他说明字段
ALTER TABLE survey_data
ADD COLUMN warning_communication_method_other VARCHAR(500) NULL COMMENT '灾害预警信息传达方式-其他项说明' AFTER warning_communication_method;

-- 13. 添加灾情信息上报方式字段
ALTER TABLE survey_data
ADD COLUMN disaster_report_method VARCHAR(200) NULL COMMENT '灾情信息上报方式' AFTER warning_communication_method_other;

-- 14. 添加灾情信息上报方式其他说明字段
ALTER TABLE survey_data
ADD COLUMN disaster_report_method_other VARCHAR(500) NULL COMMENT '灾情信息上报方式-其他项说明' AFTER disaster_report_method;

-- 15. 添加近3年编制或修订应急预案数量字段
ALTER TABLE survey_data
ADD COLUMN emergency_plan_count INT NULL COMMENT '近3年编制或修订自然灾害应急预案数量(个)' AFTER disaster_report_method_other;

-- 16. 添加近3年启动应急响应次数字段
ALTER TABLE survey_data
ADD COLUMN emergency_response_count INT NULL COMMENT '近3年针对自然灾害启动应急响应次数(次)' AFTER emergency_plan_count;

-- 17. 添加上一年度培训演练次数字段
ALTER TABLE survey_data
ADD COLUMN training_drill_count INT NULL COMMENT '上一年度组织的应急管理培训和演练次数(次)' AFTER emergency_response_count;

-- 18. 添加综合减灾工作经费保障方式字段
ALTER TABLE survey_data
ADD COLUMN funding_support_method VARCHAR(50) NULL COMMENT '乡镇（街道）综合减灾工作经费保障方式' AFTER training_participants;

-- 19. 添加综合减灾工作经费保障方式其他说明字段
ALTER TABLE survey_data
ADD COLUMN funding_support_method_other VARCHAR(500) NULL COMMENT '乡镇（街道）综合减灾工作经费保障方式-其他说明' AFTER funding_support_method;

-- 20. 添加救灾物资储备方式字段
ALTER TABLE survey_data
ADD COLUMN material_storage_method VARCHAR(100) NULL COMMENT '救灾物资储备方式' AFTER funding_amount;

-- 21. 添加救灾物资储备方式其他说明字段
ALTER TABLE survey_data
ADD COLUMN material_storage_method_other VARCHAR(500) NULL COMMENT '救灾物资储备方式-其他项说明' AFTER material_storage_method;

-- 22. 添加本级储备点数量字段
ALTER TABLE survey_data
ADD COLUMN storage_point_count INT NULL COMMENT '本级救灾物资、装备储备点数量(个)' AFTER material_storage_method_other;

-- 23. 添加储备点物资装备数量字段
ALTER TABLE survey_data
ADD COLUMN storage_equipment_count INT NULL COMMENT '本级储备点救灾物资、装备数量(套/个/件)' AFTER storage_point_count;

-- 24. 添加应急电源设备数量字段
ALTER TABLE survey_data
ADD COLUMN emergency_power_count INT NULL COMMENT '其中：应急电源或应急发电设备数量(套或件)' AFTER storage_equipment_count;

-- 25. 添加应急通信设备数量字段
ALTER TABLE survey_data
ADD COLUMN emergency_communication_count INT NULL COMMENT '应急通信设备数量(套或件)' AFTER emergency_power_count;

-- 26. 添加应急供水设备数量字段
ALTER TABLE survey_data
ADD COLUMN emergency_water_count INT NULL COMMENT '应急供水设备数量(套或件)' AFTER emergency_communication_count;

-- 27. 添加应急医疗设备数量字段
ALTER TABLE survey_data
ADD COLUMN emergency_medical_count INT NULL COMMENT '应急医疗设备数量(套或件)' AFTER emergency_water_count;

-- 28. 添加应急避难场所数量字段
ALTER TABLE survey_data
ADD COLUMN shelter_count INT NULL COMMENT '本级灾害应急避难场所数量(个或处)' AFTER material_value;

-- 29. 添加单位负责人字段
ALTER TABLE survey_data
ADD COLUMN unit_leader VARCHAR(100) NULL COMMENT '单位负责人' AFTER shelter_capacity;

-- 30. 添加统计负责人字段
ALTER TABLE survey_data
ADD COLUMN statistics_leader VARCHAR(100) NULL COMMENT '统计负责人' AFTER unit_leader;

-- 31. 添加填表人字段
ALTER TABLE survey_data
ADD COLUMN form_filler VARCHAR(100) NULL COMMENT '填表人' AFTER statistics_leader;

-- 32. 添加联系电话字段
ALTER TABLE survey_data
ADD COLUMN contact_phone VARCHAR(50) NULL COMMENT '联系电话' AFTER form_filler;

-- 33. 添加报出日期字段
ALTER TABLE survey_data
ADD COLUMN report_date DATE NULL COMMENT '报出日期(年/月/日)' AFTER contact_phone;

-- 34. 添加填写说明字段
ALTER TABLE survey_data
ADD COLUMN fill_instructions TEXT NULL COMMENT '填写说明' AFTER report_date;

-- 添加索引以提高查询性能
ALTER TABLE survey_data ADD INDEX idx_unique_id (unique_id);
ALTER TABLE survey_data ADD INDEX idx_verification_status (verification_status);
ALTER TABLE survey_data ADD INDEX idx_report_date (report_date);