-- Add org_code column to store organization code (e.g., county code)
ALTER TABLE model_execution_record
ADD COLUMN IF NOT EXISTS `org_code` VARCHAR(64) NULL COMMENT '所属机构代码';
