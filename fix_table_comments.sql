-- 修复数据库表中文注释乱码
-- 使用前请确认数据库字符集为 utf8mb4

USE evaluate_db;

-- 修复表注释
ALTER TABLE community_disaster_reduction_capacity COMMENT '社区行政村减灾能力数据表';
ALTER TABLE disaster_statistics COMMENT '地质灾害统计表';
ALTER TABLE firefighter_config COMMENT '消防员配置表';
ALTER TABLE grassroots_organization COMMENT '基层组织机构表（乡镇和社区）';
ALTER TABLE indicator_weight_score COMMENT '专家权重打分记录表';
ALTER TABLE medical_institution COMMENT '医疗卫生机构表';
ALTER TABLE organization COMMENT '组织机构表';
ALTER TABLE organization_boundary COMMENT '组织机构边界配置表';
ALTER TABLE sys_menu COMMENT '菜单权限表';
ALTER TABLE sys_region_boundary COMMENT '行政区划边界版本管理表';
ALTER TABLE sys_role COMMENT '角色表';
ALTER TABLE sys_role_menu COMMENT '角色菜单关联表';
ALTER TABLE sys_role_organization COMMENT '角色-机构关联表';
ALTER TABLE sys_user COMMENT '用户表';
ALTER TABLE sys_user_organization COMMENT '用户-组织数据权限关联表';
ALTER TABLE sys_user_role COMMENT '用户角色关联表';

-- 补充缺失的表注释（根据实体类推断）
ALTER TABLE algorithm_config COMMENT '算法配置表';
-- algorithm_step 是视图，跳过
ALTER TABLE evaluation_model COMMENT '评估模型表';
ALTER TABLE evaluation_result COMMENT '评估结果表';
ALTER TABLE field_mapping_config COMMENT '字段映射配置表';
ALTER TABLE indicator_weight COMMENT '指标权重表';
ALTER TABLE model_execution_record COMMENT '模型执行记录表';
ALTER TABLE model_step COMMENT '模型步骤表';
ALTER TABLE report COMMENT '报告表';
ALTER TABLE step_algorithm COMMENT '步骤算法表';
ALTER TABLE step_execution_result COMMENT '步骤执行结果表';
ALTER TABLE survey_data COMMENT '调查数据表';
ALTER TABLE weight_config COMMENT '权重配置表';

-- 查询验证
SELECT TABLE_NAME, TABLE_COMMENT
FROM information_schema.TABLES
WHERE TABLE_SCHEMA = 'evaluate_db'
ORDER BY TABLE_NAME;
