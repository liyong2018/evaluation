CREATE TABLE IF NOT EXISTS `sys_role_organization` (
  `role_id` bigint(20) NOT NULL COMMENT '角色ID',
  `organization_id` bigint(20) NOT NULL COMMENT '机构ID',
  PRIMARY KEY (`role_id`,`organization_id`),
  KEY `idx_role_id` (`role_id`),
  KEY `idx_organization_id` (`organization_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色-机构关联表';
