-- Add year column to model_execution_record to store evaluation year
ALTER TABLE model_execution_record
ADD COLUMN IF NOT EXISTS `year` INT NULL COMMENT '评估年份';

-- Optional: backfill from related data if available (left as no-op)
-- UPDATE model_execution_record SET year = YEAR(start_time) WHERE year IS NULL;

