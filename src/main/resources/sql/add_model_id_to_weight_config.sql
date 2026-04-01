-- 添加 model_id 字段到 weight_config 表
-- 执行时间: 2026-02-28
-- 目的：使用模型ID而不是名称来关联权重配置，更加可靠

-- 1. 添加 model_id 字段
ALTER TABLE `weight_config`
ADD COLUMN `model_id` BIGINT DEFAULT NULL COMMENT '关联的模型ID' AFTER `data_source`;

-- 2. 添加索引以提高查询性能
ALTER TABLE `weight_config`
ADD INDEX `idx_model_id` (`model_id`);

-- 3. 更新现有数据：根据 config_name 匹配对应的 model_id
-- 注意：需要根据实际的 evaluation_model 表数据更新 model_id 值
-- 示例更新语句（需要根据实际 model_id 调整）：

-- 乡镇级模型
UPDATE `weight_config` w
INNER JOIN `evaluation_model` m ON m.model_code = 'model_township'
SET w.model_id = m.id
WHERE w.config_name IN ('乡镇街道权重配置', '乡镇减灾能力评估模型', '乡镇（街道）减灾能力评估')
  AND w.data_source = 'township';

-- 社区-行政村级模型
UPDATE `weight_config` w
INNER JOIN `evaluation_model` m ON m.model_code = 'model_community_village'
SET w.model_id = m.id
WHERE w.config_name IN ('社区-社区单元权重配置', '社区-行政村级能力评估模型', '社区级（社区单元）减灾能力评估')
  AND w.data_source = 'community_village';

-- 社区-乡镇级模型
UPDATE `weight_config` w
INNER JOIN `evaluation_model` m ON m.model_code = 'model_community_township'
SET w.model_id = m.id
WHERE w.config_name IN ('社区-乡镇单元权重配置', '社区-乡镇级能力评估模型', '社区级（乡镇单元）减灾能力评估')
  AND w.data_source = 'community_township';

-- 综合模型
UPDATE `weight_config` w
INNER JOIN `evaluation_model` m ON m.model_code = 'model_comprehensive'
SET w.model_id = m.id
WHERE w.config_name IN ('综合模型权重配置', '综合减灾能力评估模型', '综合减灾能力评估')
  AND w.data_source = 'comprehensive';
