-- 社会组织减灾能力2020年数据表
CREATE TABLE IF NOT EXISTS `social_organization_disaster_reduction_capacity_2020` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `region_code` varchar(20) NOT NULL COMMENT '行政区代码',
  `province_name` varchar(50) DEFAULT NULL COMMENT '省名称',
  `city_name` varchar(50) DEFAULT NULL COMMENT '市名称',
  `county_name` varchar(50) DEFAULT NULL COMMENT '县名称',
  `emergency_equipment_material_value` decimal(15,2) DEFAULT 0.00 COMMENT '应急救援装备/物资总价值（元）',
  `passenger_vehicle_count` int(11) DEFAULT 0 COMMENT '自有客车数量（辆）',
  `freight_vehicle_count` int(11) DEFAULT 0 COMMENT '自有货运车辆数量（辆）',
  `special_operation_vehicle_count` int(11) DEFAULT 0 COMMENT '特种作业车辆（辆）',
  `last_year_science_education_audience` int(11) DEFAULT 0 COMMENT '上一年度科普宣教受众人次（人次）',
  `population` int(11) DEFAULT 0 COMMENT '区域总人口（人）',
  `year` int(11) NOT NULL DEFAULT 2020 COMMENT '年份',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_region_year` (`region_code`, `year`),
  KEY `idx_province` (`province_name`),
  KEY `idx_city` (`city_name`),
  KEY `idx_county` (`county_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='社会组织减灾能力2020年数据表';

-- 插入数据（来自德阳市）
INSERT INTO `social_organization_disaster_reduction_capacity_2020`
  (`region_code`, `province_name`, `city_name`, `county_name`,
   `emergency_equipment_material_value`, `passenger_vehicle_count`, `freight_vehicle_count`,
   `special_operation_vehicle_count`, `last_year_science_education_audience`, `population`, `year`)
VALUES
  ('510603', '四川省', '德阳市', '旌阳区', 3675798.00, 1, 1, 1, 69500, 828189, 2020),
  ('510604', '四川省', '德阳市', '罗江区', 0.00, 0, 0, 0, 3912, 209088, 2020),
  ('510623', '四川省', '德阳市', '中江县', 0.00, 0, 0, 0, 0, 946019, 2020),
  ('510681', '四川省', '德阳市', '广汉市', 10000000.00, 10, 0, 73, 1200, 626132, 2020),
  ('510682', '四川省', '德阳市', '什邡市', 0.00, 0, 0, 0, 20000, 406775, 2020),
  ('510683', '四川省', '德阳市', '绵竹市', 0.00, 0, 0, 0, 8252, 439958, 2020)
ON DUPLICATE KEY UPDATE
  `emergency_equipment_material_value` = VALUES(`emergency_equipment_material_value`),
  `passenger_vehicle_count` = VALUES(`passenger_vehicle_count`),
  `freight_vehicle_count` = VALUES(`freight_vehicle_count`),
  `special_operation_vehicle_count` = VALUES(`special_operation_vehicle_count`),
  `last_year_science_education_audience` = VALUES(`last_year_science_education_audience`),
  `population` = VALUES(`population`),
  `updated_at` = CURRENT_TIMESTAMP;
