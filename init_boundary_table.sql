-- 创建行政区划边界版本管理表
CREATE TABLE IF NOT EXISTS `sys_region_boundary` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `region_code` varchar(20) NOT NULL COMMENT '行政区划代码',
  `region_name` varchar(100) NOT NULL COMMENT '行政区划名称',
  `year` int NOT NULL COMMENT '年份',
  `file_path` varchar(255) NOT NULL COMMENT '边界文件路径(GeoJSON)',
  `boundary_type` varchar(20) DEFAULT 'city_split' COMMENT '边界类型: full(全量), city_split(市级切片)',
  `file_size` bigint DEFAULT 0 COMMENT '文件大小(字节)',
  `md5` varchar(32) DEFAULT NULL COMMENT '文件MD5',
  `create_by` varchar(50) DEFAULT NULL COMMENT '创建者',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(50) DEFAULT NULL COMMENT '更新者',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_region_year` (`region_code`,`year`),
  KEY `idx_year` (`year`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='行政区划边界版本管理表';

-- 插入当前已有数据的示例记录 (眉山市 2025)
INSERT INTO `sys_region_boundary` (`region_code`, `region_name`, `year`, `file_path`, `boundary_type`, `create_by`)
VALUES ('511400', '眉山市', 2025, '/boundaries/2025/city/眉山市.json', 'city_split', 'system')
ON DUPLICATE KEY UPDATE file_path = VALUES(file_path);

-- 插入当前已有数据的示例记录 (眉山市 2024)
INSERT INTO `sys_region_boundary` (`region_code`, `region_name`, `year`, `file_path`, `boundary_type`, `create_by`)
VALUES ('511400', '眉山市', 2024, '/boundaries/2024/city/眉山市.json', 'city_split', 'system')
ON DUPLICATE KEY UPDATE file_path = VALUES(file_path);
