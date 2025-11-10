-- 社区减灾能力数据表新增字段脚本（只添加字段，不修改现有字段）
-- 执行前请备份数据库！

-- 1. 添加缺失的字段
ALTER TABLE community_disaster_reduction_capacity
  ADD COLUMN unique_id VARCHAR(100) NULL COMMENT '唯一码';

ALTER TABLE community_disaster_reduction_capacity
  ADD COLUMN verification_status VARCHAR(50) NULL COMMENT '核实状态';

ALTER TABLE community_disaster_reduction_capacity
  ADD COLUMN community_address VARCHAR(500) NULL COMMENT '社区（行政村）地址';

ALTER TABLE community_disaster_reduction_capacity
  ADD COLUMN total_households INT NULL COMMENT '总户数（户）';

ALTER TABLE community_disaster_reduction_capacity
  ADD COLUMN age_0_14_count INT NULL COMMENT '0-14岁人数';

ALTER TABLE community_disaster_reduction_capacity
  ADD COLUMN age_65_plus_count INT NULL COMMENT '65岁（含）以上人数';

ALTER TABLE community_disaster_reduction_capacity
  ADD COLUMN disabled_person_count INT NULL COMMENT '残障人员人数';

ALTER TABLE community_disaster_reduction_capacity
  ADD COLUMN is_national_demo_community VARCHAR(10) NULL COMMENT '是否为全国综合减灾示范社区（是/否）';

ALTER TABLE community_disaster_reduction_capacity
  ADD COLUMN is_provincial_demo_community VARCHAR(10) NULL COMMENT '是否为省级综合减灾示范社区（是/否）';

ALTER TABLE community_disaster_reduction_capacity
  ADD COLUMN disaster_info_staff_count INT NULL COMMENT '灾害信息员人数（人）';

ALTER TABLE community_disaster_reduction_capacity
  ADD COLUMN emergency_shelter_count INT NULL COMMENT '本级灾害应急避难场所数量（个或处）';

ALTER TABLE community_disaster_reduction_capacity
  ADD COLUMN material_storage_method VARCHAR(200) NULL COMMENT '防灾减灾应急物资储备方式（多选）';

ALTER TABLE community_disaster_reduction_capacity
  ADD COLUMN material_storage_method_other VARCHAR(500) NULL COMMENT '防灾减灾应急物资储备方式-其他项说明';

ALTER TABLE community_disaster_reduction_capacity
  ADD COLUMN warning_receive_method VARCHAR(200) NULL COMMENT '灾害预警信息接收方式（多选）';

ALTER TABLE community_disaster_reduction_capacity
  ADD COLUMN warning_receive_method_other VARCHAR(500) NULL COMMENT '灾害预警信息接收方式-其他项说明';

ALTER TABLE community_disaster_reduction_capacity
  ADD COLUMN warning_communication_method VARCHAR(200) NULL COMMENT '灾害预警信息传达方式（多选）';

ALTER TABLE community_disaster_reduction_capacity
  ADD COLUMN warning_communication_method_other VARCHAR(500) NULL COMMENT '灾害预警信息传达方式-其他项说明';

ALTER TABLE community_disaster_reduction_capacity
  ADD COLUMN disaster_report_method VARCHAR(200) NULL COMMENT '灾情信息上报方式（多选）';

ALTER TABLE community_disaster_reduction_capacity
  ADD COLUMN disaster_report_method_other VARCHAR(500) NULL COMMENT '灾情信息上报方式-其他项说明';

ALTER TABLE community_disaster_reduction_capacity
  ADD COLUMN last_year_training_count INT NULL COMMENT '上一年度组织的防灾减灾培训活动次数（次）';

ALTER TABLE community_disaster_reduction_capacity
  ADD COLUMN last_year_drill_count INT NULL COMMENT '上一年度组织的防灾减灾演练活动次数（次）';

ALTER TABLE community_disaster_reduction_capacity
  ADD COLUMN unit_leader VARCHAR(100) NULL COMMENT '单位负责人';

ALTER TABLE community_disaster_reduction_capacity
  ADD COLUMN statistics_leader VARCHAR(100) NULL COMMENT '统计负责人';

ALTER TABLE community_disaster_reduction_capacity
  ADD COLUMN form_filler VARCHAR(100) NULL COMMENT '填表人';

ALTER TABLE community_disaster_reduction_capacity
  ADD COLUMN contact_phone VARCHAR(50) NULL COMMENT '联系电话';

ALTER TABLE community_disaster_reduction_capacity
  ADD COLUMN report_date DATE NULL COMMENT '报出日期（年/月/日）';

ALTER TABLE community_disaster_reduction_capacity
  ADD COLUMN fill_instructions TEXT NULL COMMENT '填写说明';

-- 2. 添加索引以优化查询性能
CREATE INDEX idx_community_unique_id ON community_disaster_reduction_capacity(unique_id);
CREATE INDEX idx_community_year ON community_disaster_reduction_capacity(year);

-- 3. 验证修改结果
SELECT '社区减灾能力表字段添加完成' AS status;