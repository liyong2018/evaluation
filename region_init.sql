-- 创建地区组织机构表
CREATE TABLE IF NOT EXISTS `region` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `code` varchar(50) NOT NULL COMMENT '地区代码',
  `name` varchar(100) NOT NULL COMMENT '地区名称',
  `parent_id` bigint DEFAULT NULL COMMENT '父级地区ID',
  `level` int NOT NULL COMMENT '地区级别（1-省，2-市，3-县，4-镇）',
  `sort` int DEFAULT 0 COMMENT '排序',
  `status` int DEFAULT 1 COMMENT '状态（1-启用，0-禁用）',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_code` (`code`),
  KEY `idx_parent_id` (`parent_id`),
  KEY `idx_level` (`level`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='地区组织机构表';

-- 插入示例数据
-- 省级
INSERT INTO `region` (`code`, `name`, `parent_id`, `level`, `sort`, `status`) VALUES
('510000', '四川省', NULL, 1, 1, 1);

-- 市级
INSERT INTO `region` (`code`, `name`, `parent_id`, `level`, `sort`, `status`) VALUES
('511400', '眉山市', 1, 2, 1, 1);

-- 县级
INSERT INTO `region` (`code`, `name`, `parent_id`, `level`, `sort`, `status`) VALUES
('511425', '青神县', 2, 3, 1, 1);

-- 镇级
INSERT INTO `region` (`code`, `name`, `parent_id`, `level`, `sort`, `status`) VALUES
('511425001', '汉阳镇', 3, 4, 1, 1),
('511425002', '西龙镇', 3, 4, 2, 1);

-- 添加更多示例数据以便测试
-- 其他省份
INSERT INTO `region` (`code`, `name`, `parent_id`, `level`, `sort`, `status`) VALUES
('110000', '北京市', NULL, 1, 2, 1),
('310000', '上海市', NULL, 1, 3, 1),
('440000', '广东省', NULL, 1, 4, 1);

-- 北京市区县
INSERT INTO `region` (`code`, `name`, `parent_id`, `level`, `sort`, `status`) VALUES
('110101', '东城区', 6, 2, 1, 1),
('110102', '西城区', 6, 2, 2, 1),
('110105', '朝阳区', 6, 2, 3, 1);

-- 上海市区县
INSERT INTO `region` (`code`, `name`, `parent_id`, `level`, `sort`, `status`) VALUES
('310101', '黄浦区', 7, 2, 1, 1),
('310104', '徐汇区', 7, 2, 2, 1),
('310105', '长宁区', 7, 2, 3, 1);

-- 广东省市级
INSERT INTO `region` (`code`, `name`, `parent_id`, `level`, `sort`, `status`) VALUES
('440100', '广州市', 8, 2, 1, 1),
('440300', '深圳市', 8, 2, 2, 1),
('440600', '佛山市', 8, 2, 3, 1);

-- 广州市区县
INSERT INTO `region` (`code`, `name`, `parent_id`, `level`, `sort`, `status`) VALUES
('440103', '荔湾区', 14, 3, 1, 1),
('440104', '越秀区', 14, 3, 2, 1),
('440105', '海珠区', 14, 3, 3, 1);

-- 深圳市区县
INSERT INTO `region` (`code`, `name`, `parent_id`, `level`, `sort`, `status`) VALUES
('440303', '罗湖区', 15, 3, 1, 1),
('440304', '福田区', 15, 3, 2, 1),
('440305', '南山区', 15, 3, 3, 1);