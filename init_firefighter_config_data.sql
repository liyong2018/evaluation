-- 消防员配置数据初始化脚本
-- 用于在生产环境部署时初始化消防员配置数据

-- 检查表是否存在，如果不存在则创建
CREATE TABLE IF NOT EXISTS firefighter_config (
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
    created_by VARCHAR(50) DEFAULT 'system' COMMENT '创建人',
    updated_by VARCHAR(50) DEFAULT 'system' COMMENT '更新人'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='消防员配置表';

-- 添加唯一约束和索引
ALTER TABLE firefighter_config
  ADD UNIQUE KEY uk_region_code (region_code);

ALTER TABLE firefighter_config
  ADD INDEX idx_location (province_name, city_name, county_name, township_name) COMMENT '地理位置查询索引';

ALTER TABLE firefighter_config
  ADD INDEX idx_status (status) COMMENT '状态索引';

-- 清空现有数据（用于重新初始化）
DELETE FROM firefighter_config WHERE 1=1;

-- 插入眉山市青神县的消防员配置数据
INSERT INTO firefighter_config (region_code, province_name, city_name, county_name, township_name, firefighter_count, remark) VALUES
('511425001', '四川省', '眉山市', '青神县', '青竹街道', 26, '城区街道，消防员配置较多，包含专职消防队'),
('511425102', '四川省', '眉山市', '青神县', '汉阳镇', 5, '一般乡镇配置，兼职消防员'),
('511425108', '四川省', '眉山市', '青神县', '瑞峰镇', 0, '暂无专职消防员，应急响应人员兼任'),
('511425110', '四川省', '眉山市', '青神县', '西龙镇', 0, '暂无专职消防员，应急响应人员兼任'),
('511425112', '四川省', '眉山市', '青神县', '高台镇', 0, '暂无专职消防员，应急响应人员兼任'),
('511425217', '四川省', '眉山市', '青神县', '白果乡', 0, '乡级配置，暂无专职消防员，应急响应人员兼任'),
('511425218', '四川省', '眉山市', '青神县', '罗波乡', 0, '乡级配置，暂无专职消防员，应急响应人员兼任');

-- 插入眉山市其他区县的示例数据（可根据实际情况调整）
INSERT INTO firefighter_config (region_code, province_name, city_name, county_name, township_name, firefighter_count, remark) VALUES
-- 东坡区
('511402001', '四川省', '眉山市', '东坡区', '苏祠街道', 45, '市府所在地，消防员配置充足'),
('511402101', '四川省', '眉山市', '东坡区', '大石桥街道', 30, '城市区域，消防员配置充足'),
('511402102', '四川省', '眉山市', '东坡区', '通惠街道', 28, '城市区域，消防员配置充足'),
('511402103', '四川省', '眉山市', '东坡区', '尚义镇', 15, '一般乡镇配置'),
('511402104', '四川省', '眉山市', '东坡区', '多悦镇', 12, '一般乡镇配置'),
('511402105', '四川省', '眉山市', '东坡区', '万胜镇', 10, '一般乡镇配置'),
('511402106', '四川省', '眉山市', '东坡区', '秦家镇', 8, '一般乡镇配置'),
('511402107', '四川省', '眉山市', '东坡区', '富牛镇', 8, '一般乡镇配置'),
('511402108', '四川省', '眉山市', '东坡区', '永寿镇', 6, '一般乡镇配置'),
('511402109', '四川省', '眉山市', '东坡区', '复兴乡', 0, '乡级配置，应急响应人员兼任'),

-- 彭山区
('511423001', '四川省', '眉山市', '彭山区', '凤鸣街道', 35, '区府所在地，消防员配置充足'),
('511423101', '四川省', '眉山市', '彭山区', '观音街道', 32, '城市区域，消防员配置充足'),
('511423102', '四川省', '眉山市', '彭山区', '江口街道', 25, '城市区域，消防员配置充足'),
('511423103', '四川省', '眉山市', '彭山区', '黄丰镇', 18, '一般乡镇配置'),
('511423104', '四川省', '眉山市', '彭山区', '青龙镇', 15, '一般乡镇配置'),
('511423105', '四川省', '眉山市', '彭山区', '保胜乡', 0, '乡级配置，应急响应人员兼任'),
('511423106', '四川省', '眉山市', '彭山区', '义和乡', 0, '乡级配置，应急响应人员兼任');

-- 验证插入结果
SELECT '消防员配置数据初始化完成' AS status;

-- 显示插入的数据统计
SELECT
    '总记录数' as 指标,
    COUNT(*) as 数值
FROM firefighter_config;

-- 按县统计消防员数量
SELECT
    county_name as 县名称,
    COUNT(*) as 乡镇数量,
    SUM(firefighter_count) as 总消防员数量,
    ROUND(AVG(firefighter_count), 1) as 平均配置
FROM firefighter_config
WHERE status = 1
GROUP BY county_name
ORDER BY 总消防员数量 DESC;

-- 显示所有配置数据
SELECT * FROM firefighter_config ORDER BY province_name, city_name, county_name, township_name;