-- 修改formula_config表的algorithm_step_id字段类型
-- 从BIGINT改为VARCHAR(100)以支持多个步骤ID（如"1,2,3"）

USE disaster_reduction_evaluation;

-- 1. 删除外键约束
ALTER TABLE formula_config DROP FOREIGN KEY formula_config_ibfk_1;

-- 2. 删除索引
DROP INDEX idx_formula_config_step ON formula_config;

-- 3. 修改字段类型
ALTER TABLE formula_config MODIFY COLUMN algorithm_step_id VARCHAR(100) NOT NULL COMMENT '算法步骤ID（支持多个，用逗号分隔）';

-- 4. 更新现有数据，将数字ID转换为字符串格式
UPDATE formula_config SET algorithm_step_id = CAST(algorithm_step_id AS CHAR);

-- 5. 重新创建索引（不再是外键约束）
CREATE INDEX idx_formula_config_step ON formula_config(algorithm_step_id);

-- 验证修改结果
SELECT 'Formula config table structure modified successfully!' as message;
DESC formula_config;