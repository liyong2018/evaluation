-- Active: 1755486980655@@192.168.15.203@30314@evaluate_db
-- 创建社区行政村减灾能力数据表
USE evaluate_db;

DROP TABLE IF EXISTS `community_disaster_reduction_capacity`;

CREATE TABLE `community_disaster_reduction_capacity` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `region_code` varchar(50) NOT NULL COMMENT '行政区代码',
  `province_name` varchar(100) DEFAULT NULL COMMENT '省名称',
  `city_name` varchar(100) DEFAULT NULL COMMENT '市名称',
  `county_name` varchar(100) DEFAULT NULL COMMENT '县名称',
  `township_name` varchar(100) DEFAULT NULL COMMENT '乡镇名称',
  `community_name` varchar(100) NOT NULL COMMENT '社区（行政村）名称',
  `has_emergency_plan` varchar(10) DEFAULT '否' COMMENT '是否有社区（行政村）应急预案（是/否）',
  `has_vulnerable_groups_list` varchar(10) DEFAULT '否' COMMENT '是否有本辖区弱势人群清单（是/否）',
  `has_disaster_points_list` varchar(10) DEFAULT '否' COMMENT '是否有本辖区地质灾害等隐患点清单（是/否）',
  `has_disaster_map` varchar(10) DEFAULT '否' COMMENT '是否有社区（行政村）灾害类地图（是/否）',
  `resident_population` int DEFAULT 0 COMMENT '常住人口数量（人）',
  `last_year_funding_amount` decimal(12,2) DEFAULT 0.00 COMMENT '上一年度防灾减灾救灾资金投入总金额（万元）',
  `materials_equipment_value` decimal(12,2) DEFAULT 0.00 COMMENT '现有储备物资、装备折合金额（万元）',
  `medical_service_count` int DEFAULT 0 COMMENT '社区医疗卫生服务站或村卫生室数量（个）',
  `militia_reserve_count` int DEFAULT 0 COMMENT '民兵预备役人数（人）',
  `registered_volunteer_count` int DEFAULT 0 COMMENT '登记注册志愿者人数（人）',
  `last_year_training_participants` int DEFAULT 0 COMMENT '上一年度防灾减灾培训活动培训人次（人次）',
  `last_year_drill_participants` int DEFAULT 0 COMMENT '参与上一年度组织的防灾减灾演练活动的居民(人次)',
  `emergency_shelter_capacity` int DEFAULT 0 COMMENT '本级灾害应急避难场所容量（人）',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_region_community` (`region_code`, `community_name`),
  KEY `idx_region_code` (`region_code`),
  KEY `idx_province_name` (`province_name`),
  KEY `idx_city_name` (`city_name`),
  KEY `idx_county_name` (`county_name`),
  KEY `idx_township_name` (`township_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='社区行政村减灾能力数据表';

-- 验证表是否创建成功
SHOW TABLES LIKE 'community_disaster_reduction_capacity';

-- 查看表结构
DESCRIBE `community_disaster_reduction_capacity`;