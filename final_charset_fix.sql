-- 最终字符修复脚本 - 使用UTF8MB4字符集
-- 确保连接时使用正确字符集

SET NAMES utf8mb4;
SET CHARACTER SET utf8mb4;
SET character_set_connection = utf8mb4;
SET character_set_results = utf8mb4;
SET character_set_client = utf8mb4;

USE evaluate_db;

-- 重新设置主要列的注释（重点修复显示乱码的字段）
ALTER TABLE survey_data
  MODIFY COLUMN total_households INT NULL COMMENT '年末总户数(户)';

ALTER TABLE survey_data
  MODIFY COLUMN main_disaster_types VARCHAR(200) NULL COMMENT '影响乡镇（街道）的主要灾害类型';

ALTER TABLE survey_data
  MODIFY COLUMN disaster_types_other VARCHAR(500) NULL COMMENT '影响乡镇（街道）的主要灾害类型-其他项说明';

ALTER TABLE survey_data
  MODIFY COLUMN disaster_info_staff INT NULL COMMENT '本级灾害信息员人数';

ALTER TABLE survey_data
  MODIFY COLUMN has_disaster_map VARCHAR(10) NULL COMMENT '是否有乡镇（街道）灾害类地图(是/否)';

ALTER TABLE survey_data
  MODIFY COLUMN warning_receive_method VARCHAR(200) NULL COMMENT '灾害预警信息接收方式';

ALTER TABLE survey_data
  MODIFY COLUMN warning_receive_method_other VARCHAR(500) NULL COMMENT '灾害预警信息接收方式-其他项说明';

ALTER TABLE survey_data
  MODIFY COLUMN warning_communication_method VARCHAR(200) NULL COMMENT '灾害预警信息传达方式';

ALTER TABLE survey_data
  MODIFY COLUMN warning_communication_method_other VARCHAR(500) NULL COMMENT '灾害预警信息传达方式-其他项说明';

ALTER TABLE survey_data
  MODIFY COLUMN disaster_report_method VARCHAR(200) NULL COMMENT '灾情信息上报方式';

ALTER TABLE survey_data
  MODIFY COLUMN disaster_report_method_other VARCHAR(500) NULL COMMENT '灾情信息上报方式-其他项说明';

-- 验证修复结果
SELECT '字符修复完成' AS status;

-- 显示修复后的列注释
SHOW FULL COLUMNS FROM survey_data
WHERE Field IN ('total_households', 'main_disaster_types', 'disaster_info_staff', 'has_disaster_map');