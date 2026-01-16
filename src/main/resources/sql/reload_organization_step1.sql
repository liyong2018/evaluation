-- ================================================
-- 重新加载组织机构数据 - 分步执行
-- ================================================

USE evaluate_db;

-- 1. 清空现有数据
SET FOREIGN_KEY_CHECKS = 0;
DELETE FROM organization;
SET FOREIGN_KEY_CHECKS = 1;

-- 2. 插入省级数据（level=1）
INSERT INTO organization (code, name, level, year, data_source, province_name, is_baseline) VALUES
('51', '四川省', 1, 2020, 'BASELINE', '四川省', 1);

-- 3. 验证省级数据
SELECT '=== 省级数据插入完成 ===' AS info;
SELECT * FROM organization WHERE level = 1;

-- 4. 插入市级数据（level=2）- parent_id设为NULL，稍后更新
INSERT INTO organization (code, name, level, year, data_source, province_name, city_name, parent_id, is_baseline) VALUES
('5101', '成都市', 2, 2020, 'BASELINE', '四川省', '成都市', NULL, 1),
('5103', '自贡市', 2, 2020, 'BASELINE', '四川省', '自贡市', NULL, 1),
('5104', '攀枝花市', 2, 2020, 'BASELINE', '四川省', '攀枝花市', NULL, 1),
('5105', '泸州市', 2, 2020, 'BASELINE', '四川省', '泸州市', NULL, 1),
('5106', '德阳市', 2, 2020, 'BASELINE', '四川省', '德阳市', NULL, 1),
('5107', '绵阳市', 2, 2020, 'BASELINE', '四川省', '绵阳市', NULL, 1),
('5108', '广元市', 2, 2020, 'BASELINE', '四川省', '广元市', NULL, 1),
('5109', '遂宁市', 2, 2020, 'BASELINE', '四川省', '遂宁市', NULL, 1),
('5110', '内江市', 2, 2020, 'BASELINE', '四川省', '内江市', NULL, 1),
('5111', '乐山市', 2, 2020, 'BASELINE', '四川省', '乐山市', NULL, 1),
('5113', '南充市', 2, 2020, 'BASELINE', '四川省', '南充市', NULL, 1),
('5114', '眉山市', 2, 2020, 'BASELINE', '四川省', '眉山市', NULL, 1),
('5115', '宜宾市', 2, 2020, 'BASELINE', '四川省', '宜宾市', NULL, 1),
('5116', '广安市', 2, 2020, 'BASELINE', '四川省', '广安市', NULL, 1),
('5117', '达州市', 2, 2020, 'BASELINE', '四川省', '达州市', NULL, 1),
('5118', '雅安市', 2, 2020, 'BASELINE', '四川省', '雅安市', NULL, 1),
('5119', '巴中市', 2, 2020, 'BASELINE', '四川省', '巴中市', NULL, 1),
('5120', '资阳市', 2, 2020, 'BASELINE', '四川省', '资阳市', NULL, 1),
('5132', '阿坝藏族羌族自治州', 2, 2020, 'BASELINE', '四川省', '阿坝藏族羌族自治州', NULL, 1),
('5133', '甘孜藏族自治州', 2, 2020, 'BASELINE', '四川省', '甘孜藏族自治州', NULL, 1),
('5134', '凉山彝族自治州', 2, 2020, 'BASELINE', '四川省', '凉山彝族自治州', NULL, 1);

-- 5. 更新市级数据的 parent_id
UPDATE organization SET parent_id = (SELECT id FROM (SELECT id FROM organization WHERE code='51' LIMIT 1) AS province) WHERE level = 2;

-- 6. 验证市级数据
SELECT '=== 市级数据插入完成 ===' AS info;
SELECT level, COUNT(*) as count FROM organization GROUP BY level ORDER BY level;
