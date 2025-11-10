-- 消防员配置表创建脚本
-- 用于存储各乡镇的消防员数量配置

-- 创建消防员配置表
CREATE TABLE firefighter_config (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    region_code VARCHAR(20) NOT NULL COMMENT '行政区划代码',
    province_name VARCHAR(100) NOT NULL COMMENT '省名称',
    city_name VARCHAR(100) NOT NULL COMMENT '市名称',
    county_name VARCHAR(100) NOT NULL COMMENT '县名称',
    township_name VARCHAR(100) NOT NULL COMMENT '乡镇名称',
    firefighter_count INT DEFAULT 0 COMMENT '消防员数量（人）',
    status TINYINT DEFAULT 1 COMMENT '状态（1-启用，0-禁用）',
    remark VARCHAR(500) COMMENT '备注',
    created_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    created_by VARCHAR(50) COMMENT '创建人',
    updated_by VARCHAR(50) COMMENT '更新人'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='消防员配置表';

-- 添加唯一约束
ALTER TABLE firefighter_config
  ADD UNIQUE KEY uk_region_code (region_code) COMMENT '行政区划代码唯一约束';

-- 添加查询优化索引
ALTER TABLE firefighter_config
  ADD INDEX idx_location (province_name, city_name, county_name, township_name) COMMENT '地理位置查询索引';

ALTER TABLE firefighter_config
  ADD INDEX idx_status (status) COMMENT '状态索引';

-- 插入示例数据
INSERT INTO firefighter_config (region_code, province_name, city_name, county_name, township_name, firefighter_count, remark) VALUES
('511425001', '四川省', '眉山市', '青神县', '青竹街道', 26, '城区街道，消防员配置较多'),
('511425102', '四川省', '眉山市', '青神县', '汉阳镇', 5, '一般乡镇配置'),
('511425108', '四川省', '眉山市', '青神县', '瑞峰镇', 0, '暂无专职消防员'),
('511425110', '四川省', '眉山市', '青神县', '西龙镇', 0, '暂无专职消防员'),
('511425112', '四川省', '眉山市', '青神县', '高台镇', 0, '暂无专职消防员'),
('511425217', '四川省', '眉山市', '青神县', '白果乡', 0, '乡级配置，暂无专职消防员'),
('511425218', '四川省', '眉山市', '青神县', '罗波乡', 0, '乡级配置，暂无专职消防员');

-- 验证插入结果
SELECT '消防员配置表创建完成，插入示例数据 ' AS status;

-- 显示表结构
DESCRIBE firefighter_config;

-- 显示插入的数据
SELECT * FROM firefighter_config;