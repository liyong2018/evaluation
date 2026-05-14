-- 将模型20（市级综合减灾能力评估）第3步（加权步骤）的硬编码权重替换为动态权重变量引用
-- 每个指标的合成权重 = 一级指标权重 * 二级指标权重
-- 权重变量（weight_<CODE>）由 ModelExecutionServiceImpl.loadBaseDataToContext 加载到执行上下文中

SET NAMES utf8mb4;

SET @step3_id = (SELECT id FROM model_step WHERE model_id = 20 AND step_code = 'weighted_matrix' LIMIT 1);

-- 政府减灾能力 (L1_GOVERNMENT) 下的6个二级指标
UPDATE step_algorithm SET ql_expression = 'gov_management_norm * weight_L1_GOVERNMENT * weight_L2_GOV_MANAGEMENT'      WHERE step_id = @step3_id AND algorithm_code = 'WEIGHT_01';
UPDATE step_algorithm SET ql_expression = 'gov_engineering_norm * weight_L1_GOVERNMENT * weight_L2_GOV_ENGINEERING'   WHERE step_id = @step3_id AND algorithm_code = 'WEIGHT_02';
UPDATE step_algorithm SET ql_expression = 'gov_monitoring_norm * weight_L1_GOVERNMENT * weight_L2_GOV_MONITORING'     WHERE step_id = @step3_id AND algorithm_code = 'WEIGHT_03';
UPDATE step_algorithm SET ql_expression = 'gov_material_norm * weight_L1_GOVERNMENT * weight_L2_GOV_MATERIAL'         WHERE step_id = @step3_id AND algorithm_code = 'WEIGHT_04';
UPDATE step_algorithm SET ql_expression = 'gov_rescue_team_norm * weight_L1_GOVERNMENT * weight_L2_GOV_RESCUE_TEAM'   WHERE step_id = @step3_id AND algorithm_code = 'WEIGHT_05';
UPDATE step_algorithm SET ql_expression = 'gov_relocation_norm * weight_L1_GOVERNMENT * weight_L2_GOV_RELOCATION'     WHERE step_id = @step3_id AND algorithm_code = 'WEIGHT_06';

-- 企业减灾能力 (L1_ENTERPRISE) 下的2个二级指标
UPDATE step_algorithm SET ql_expression = 'enterprise_engineering_norm * weight_L1_ENTERPRISE * weight_L2_ENT_ENGINEERING_RESCUE' WHERE step_id = @step3_id AND algorithm_code = 'WEIGHT_07';
UPDATE step_algorithm SET ql_expression = 'enterprise_insurance_norm * weight_L1_ENTERPRISE * weight_L2_ENT_INSURANCE'           WHERE step_id = @step3_id AND algorithm_code = 'WEIGHT_08';

-- 社会组织减灾能力 (L1_SOCIAL_ORGANIZATION) 下的4个二级指标
UPDATE step_algorithm SET ql_expression = 'social_material_norm * weight_L1_SOCIAL_ORGANIZATION * weight_L2_SOC_MATERIAL'   WHERE step_id = @step3_id AND algorithm_code = 'WEIGHT_09';
UPDATE step_algorithm SET ql_expression = 'social_transport_norm * weight_L1_SOCIAL_ORGANIZATION * weight_L2_SOC_TRANSPORT'  WHERE step_id = @step3_id AND algorithm_code = 'WEIGHT_10';
UPDATE step_algorithm SET ql_expression = 'social_rescue_norm * weight_L1_SOCIAL_ORGANIZATION * weight_L2_SOC_RESCUE'       WHERE step_id = @step3_id AND algorithm_code = 'WEIGHT_11';
UPDATE step_algorithm SET ql_expression = 'social_publicity_norm * weight_L1_SOCIAL_ORGANIZATION * weight_L2_SOC_PUBLICITY'  WHERE step_id = @step3_id AND algorithm_code = 'WEIGHT_12';

-- 乡镇（街道）减灾能力 (L1_TOWNSHIP) 下的3个二级指标
UPDATE step_algorithm SET ql_expression = 'township_management_norm * weight_L1_TOWNSHIP * weight_L2_TWN_DISASTER_MANAGEMENT'    WHERE step_id = @step3_id AND algorithm_code = 'WEIGHT_13';
UPDATE step_algorithm SET ql_expression = 'township_preparedness_norm * weight_L1_TOWNSHIP * weight_L2_TWN_DISASTER_PREPAREDNESS' WHERE step_id = @step3_id AND algorithm_code = 'WEIGHT_14';
UPDATE step_algorithm SET ql_expression = 'township_self_rescue_norm * weight_L1_TOWNSHIP * weight_L2_TWN_SELF_RESCUE_TRANSFER'   WHERE step_id = @step3_id AND algorithm_code = 'WEIGHT_15';

-- 社区（行政区）减灾能力 (L1_COMMUNITY) 下的3个二级指标
UPDATE step_algorithm SET ql_expression = 'community_management_norm * weight_L1_COMMUNITY * weight_L2_COM_DISASTER_MANAGEMENT'    WHERE step_id = @step3_id AND algorithm_code = 'WEIGHT_16';
UPDATE step_algorithm SET ql_expression = 'community_preparedness_norm * weight_L1_COMMUNITY * weight_L2_COM_DISASTER_PREPAREDNESS' WHERE step_id = @step3_id AND algorithm_code = 'WEIGHT_17';
UPDATE step_algorithm SET ql_expression = 'community_self_rescue_norm * weight_L1_COMMUNITY * weight_L2_COM_SELF_RESCUE_TRANSFER'   WHERE step_id = @step3_id AND algorithm_code = 'WEIGHT_18';

-- 家庭减灾能力 (L1_FAMILY) 下的4个二级指标
UPDATE step_algorithm SET ql_expression = 'family_vulnerability_norm * weight_L1_FAMILY * weight_L2_FAM_VULNERABILITY' WHERE step_id = @step3_id AND algorithm_code = 'WEIGHT_19';
UPDATE step_algorithm SET ql_expression = 'family_material_norm * weight_L1_FAMILY * weight_L2_FAM_MATERIAL'           WHERE step_id = @step3_id AND algorithm_code = 'WEIGHT_20';
UPDATE step_algorithm SET ql_expression = 'family_information_norm * weight_L1_FAMILY * weight_L2_FAM_INFORMATION'     WHERE step_id = @step3_id AND algorithm_code = 'WEIGHT_21';
UPDATE step_algorithm SET ql_expression = 'family_self_rescue_norm * weight_L1_FAMILY * weight_L2_FAM_SELF_RESCUE'     WHERE step_id = @step3_id AND algorithm_code = 'WEIGHT_22';
