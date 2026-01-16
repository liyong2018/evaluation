-- ================================================
-- 从参考文件重新加载组织机构数据
-- 确保 organization 表包含：1个省、21个市州、183个市县
-- ================================================

USE evaluate_db;

-- 1. 清空现有数据
SET FOREIGN_KEY_CHECKS = 0;
TRUNCATE TABLE organization;
SET FOREIGN_KEY_CHECKS = 1;

-- 2. 插入省级数据（level=1）
-- 注意：参考文件使用 510000，数据库使用 51
INSERT INTO organization (code, name, level, year, data_source, province_name, is_baseline) VALUES
('51', '四川省', 1, 2020, 'BASELINE', '四川省', 1);

-- 3. 插入市级数据（level=2）
-- 参考文件中的城市代码是4位，对应数据库中的格式
INSERT INTO organization (code, name, level, year, data_source, province_name, city_name, parent_id, is_baseline) VALUES
('5101', '成都市', 2, 2020, 'BASELINE', '四川省', '成都市', (SELECT id FROM organization WHERE code='51'), 1),
('5103', '自贡市', 2, 2020, 'BASELINE', '四川省', '自贡市', (SELECT id FROM organization WHERE code='51'), 1),
('5104', '攀枝花市', 2, 2020, 'BASELINE', '四川省', '攀枝花市', (SELECT id FROM organization WHERE code='51'), 1),
('5105', '泸州市', 2, 2020, 'BASELINE', '四川省', '泸州市', (SELECT id FROM organization WHERE code='51'), 1),
('5106', '德阳市', 2, 2020, 'BASELINE', '四川省', '德阳市', (SELECT id FROM organization WHERE code='51'), 1),
('5107', '绵阳市', 2, 2020, 'BASELINE', '四川省', '绵阳市', (SELECT id FROM organization WHERE code='51'), 1),
('5108', '广元市', 2, 2020, 'BASELINE', '四川省', '广元市', (SELECT id FROM organization WHERE code='51'), 1),
('5109', '遂宁市', 2, 2020, 'BASELINE', '四川省', '遂宁市', (SELECT id FROM organization WHERE code='51'), 1),
('5110', '内江市', 2, 2020, 'BASELINE', '四川省', '内江市', (SELECT id FROM organization WHERE code='51'), 1),
('5111', '乐山市', 2, 2020, 'BASELINE', '四川省', '乐山市', (SELECT id FROM organization WHERE code='51'), 1),
('5113', '南充市', 2, 2020, 'BASELINE', '四川省', '南充市', (SELECT id FROM organization WHERE code='51'), 1),
('5114', '眉山市', 2, 2020, 'BASELINE', '四川省', '眉山市', (SELECT id FROM organization WHERE code='51'), 1),
('5115', '宜宾市', 2, 2020, 'BASELINE', '四川省', '宜宾市', (SELECT id FROM organization WHERE code='51'), 1),
('5116', '广安市', 2, 2020, 'BASELINE', '四川省', '广安市', (SELECT id FROM organization WHERE code='51'), 1),
('5117', '达州市', 2, 2020, 'BASELINE', '四川省', '达州市', (SELECT id FROM organization WHERE code='51'), 1),
('5118', '雅安市', 2, 2020, 'BASELINE', '四川省', '雅安市', (SELECT id FROM organization WHERE code='51'), 1),
('5119', '巴中市', 2, 2020, 'BASELINE', '四川省', '巴中市', (SELECT id FROM organization WHERE code='51'), 1),
('5120', '资阳市', 2, 2020, 'BASELINE', '四川省', '资阳市', (SELECT id FROM organization WHERE code='51'), 1),
('5132', '阿坝藏族羌族自治州', 2, 2020, 'BASELINE', '四川省', '阿坝藏族羌族自治州', (SELECT id FROM organization WHERE code='51'), 1),
('5133', '甘孜藏族自治州', 2, 2020, 'BASELINE', '四川省', '甘孜藏族自治州', (SELECT id FROM organization WHERE code='51'), 1),
('5134', '凉山彝族自治州', 2, 2020, 'BASELINE', '四川省', '凉山彝族自治州', (SELECT id FROM organization WHERE code='51'), 1);

-- 4. 插入区县级数据（level=3）
-- 成都市区县（20个）
INSERT INTO organization (code, name, level, year, data_source, province_name, city_name, county_name, parent_id, is_baseline) VALUES
('510104', '锦江区', 3, 2020, 'BASELINE', '四川省', '成都市', '锦江区', (SELECT id FROM organization WHERE code='5101'), 1),
('510105', '青羊区', 3, 2020, 'BASELINE', '四川省', '成都市', '青羊区', (SELECT id FROM organization WHERE code='5101'), 1),
('510106', '金牛区', 3, 2020, 'BASELINE', '四川省', '成都市', '金牛区', (SELECT id FROM organization WHERE code='5101'), 1),
('510107', '武侯区', 3, 2020, 'BASELINE', '四川省', '成都市', '武侯区', (SELECT id FROM organization WHERE code='5101'), 1),
('510108', '成华区', 3, 2020, 'BASELINE', '四川省', '成都市', '成华区', (SELECT id FROM organization WHERE code='5101'), 1),
('510112', '龙泉驿区', 3, 2020, 'BASELINE', '四川省', '成都市', '龙泉驿区', (SELECT id FROM organization WHERE code='5101'), 1),
('510113', '青白江区', 3, 2020, 'BASELINE', '四川省', '成都市', '青白江区', (SELECT id FROM organization WHERE code='5101'), 1),
('510114', '新都区', 3, 2020, 'BASELINE', '四川省', '成都市', '新都区', (SELECT id FROM organization WHERE code='5101'), 1),
('510115', '温江区', 3, 2020, 'BASELINE', '四川省', '成都市', '温江区', (SELECT id FROM organization WHERE code='5101'), 1),
('510116', '双流县', 3, 2020, 'BASELINE', '四川省', '成都市', '双流县', (SELECT id FROM organization WHERE code='5101'), 1),
('510117', '郫县', 3, 2020, 'BASELINE', '四川省', '成都市', '郫县', (SELECT id FROM organization WHERE code='5101'), 1),
('510118', '新津县', 3, 2020, 'BASELINE', '四川省', '成都市', '新津县', (SELECT id FROM organization WHERE code='5101'), 1),
('510121', '金堂县', 3, 2020, 'BASELINE', '四川省', '成都市', '金堂县', (SELECT id FROM organization WHERE code='5101'), 1),
('510129', '大邑县', 3, 2020, 'BASELINE', '四川省', '成都市', '大邑县', (SELECT id FROM organization WHERE code='5101'), 1),
('510131', '蒲江县', 3, 2020, 'BASELINE', '四川省', '成都市', '蒲江县', (SELECT id FROM organization WHERE code='5101'), 1),
('510181', '都江堰市', 3, 2020, 'BASELINE', '四川省', '成都市', '都江堰市', (SELECT id FROM organization WHERE code='5101'), 1),
('510182', '彭州市', 3, 2020, 'BASELINE', '四川省', '成都市', '彭州市', (SELECT id FROM organization WHERE code='5101'), 1),
('510183', '邛崃市', 3, 2020, 'BASELINE', '四川省', '成都市', '邛崃市', (SELECT id FROM organization WHERE code='5101'), 1),
('510184', '崇州市', 3, 2020, 'BASELINE', '四川省', '成都市', '崇州市', (SELECT id FROM organization WHERE code='5101'), 1),
('510185', '简阳市', 3, 2020, 'BASELINE', '四川省', '成都市', '简阳市', (SELECT id FROM organization WHERE code='5101'), 1);

-- 自贡市区县（6个）
INSERT INTO organization (code, name, level, year, data_source, province_name, city_name, county_name, parent_id, is_baseline) VALUES
('510302', '自流井区', 3, 2020, 'BASELINE', '四川省', '自贡市', '自流井区', (SELECT id FROM organization WHERE code='5103'), 1),
('510303', '贡井区', 3, 2020, 'BASELINE', '四川省', '自贡市', '贡井区', (SELECT id FROM organization WHERE code='5103'), 1),
('510304', '大安区', 3, 2020, 'BASELINE', '四川省', '自贡市', '大安区', (SELECT id FROM organization WHERE code='5103'), 1),
('510311', '沿滩区', 3, 2020, 'BASELINE', '四川省', '自贡市', '沿滩区', (SELECT id FROM organization WHERE code='5103'), 1),
('510321', '荣县', 3, 2020, 'BASELINE', '四川省', '自贡市', '荣县', (SELECT id FROM organization WHERE code='5103'), 1),
('510322', '富顺县', 3, 2020, 'BASELINE', '四川省', '自贡市', '富顺县', (SELECT id FROM organization WHERE code='5103'), 1);

-- 攀枝花市区县（5个）
INSERT INTO organization (code, name, level, year, data_source, province_name, city_name, county_name, parent_id, is_baseline) VALUES
('510402', '东区', 3, 2020, 'BASELINE', '四川省', '攀枝花市', '东区', (SELECT id FROM organization WHERE code='5104'), 1),
('510403', '西区', 3, 2020, 'BASELINE', '四川省', '攀枝花市', '西区', (SELECT id FROM organization WHERE code='5104'), 1),
('510411', '仁和区', 3, 2020, 'BASELINE', '四川省', '攀枝花市', '仁和区', (SELECT id FROM organization WHERE code='5104'), 1),
('510421', '米易县', 3, 2020, 'BASELINE', '四川省', '攀枝花市', '米易县', (SELECT id FROM organization WHERE code='5104'), 1),
('510422', '盐边县', 3, 2020, 'BASELINE', '四川省', '攀枝花市', '盐边县', (SELECT id FROM organization WHERE code='5104'), 1);

-- 泸州市区县（7个）
INSERT INTO organization (code, name, level, year, data_source, province_name, city_name, county_name, parent_id, is_baseline) VALUES
('510502', '江阳区', 3, 2020, 'BASELINE', '四川省', '泸州市', '江阳区', (SELECT id FROM organization WHERE code='5105'), 1),
('510503', '纳溪区', 3, 2020, 'BASELINE', '四川省', '泸州市', '纳溪区', (SELECT id FROM organization WHERE code='5105'), 1),
('510504', '龙马潭区', 3, 2020, 'BASELINE', '四川省', '泸州市', '龙马潭区', (SELECT id FROM organization WHERE code='5105'), 1),
('510521', '泸县', 3, 2020, 'BASELINE', '四川省', '泸州市', '泸县', (SELECT id FROM organization WHERE code='5105'), 1),
('510522', '合江县', 3, 2020, 'BASELINE', '四川省', '泸州市', '合江县', (SELECT id FROM organization WHERE code='5105'), 1),
('510524', '叙永县', 3, 2020, 'BASELINE', '四川省', '泸州市', '叙永县', (SELECT id FROM organization WHERE code='5105'), 1),
('510525', '古蔺县', 3, 2020, 'BASELINE', '四川省', '泸州市', '古蔺县', (SELECT id FROM organization WHERE code='5105'), 1);

-- 德阳市区县（6个）
INSERT INTO organization (code, name, level, year, data_source, province_name, city_name, county_name, parent_id, is_baseline) VALUES
('510603', '旌阳区', 3, 2020, 'BASELINE', '四川省', '德阳市', '旌阳区', (SELECT id FROM organization WHERE code='5106'), 1),
('510604', '罗江县', 3, 2020, 'BASELINE', '四川省', '德阳市', '罗江县', (SELECT id FROM organization WHERE code='5106'), 1),
('510623', '中江县', 3, 2020, 'BASELINE', '四川省', '德阳市', '中江县', (SELECT id FROM organization WHERE code='5106'), 1),
('510681', '广汉市', 3, 2020, 'BASELINE', '四川省', '德阳市', '广汉市', (SELECT id FROM organization WHERE code='5106'), 1),
('510682', '什邡市', 3, 2020, 'BASELINE', '四川省', '德阳市', '什邡市', (SELECT id FROM organization WHERE code='5106'), 1),
('510683', '绵竹市', 3, 2020, 'BASELINE', '四川省', '德阳市', '绵竹市', (SELECT id FROM organization WHERE code='5106'), 1);

-- 绵阳市区县（9个）
INSERT INTO organization (code, name, level, year, data_source, province_name, city_name, county_name, parent_id, is_baseline) VALUES
('510703', '涪城区', 3, 2020, 'BASELINE', '四川省', '绵阳市', '涪城区', (SELECT id FROM organization WHERE code='5107'), 1),
('510704', '游仙区', 3, 2020, 'BASELINE', '四川省', '绵阳市', '游仙区', (SELECT id FROM organization WHERE code='5107'), 1),
('510705', '安县', 3, 2020, 'BASELINE', '四川省', '绵阳市', '安县', (SELECT id FROM organization WHERE code='5107'), 1),
('510722', '三台县', 3, 2020, 'BASELINE', '四川省', '绵阳市', '三台县', (SELECT id FROM organization WHERE code='5107'), 1),
('510723', '盐亭县', 3, 2020, 'BASELINE', '四川省', '绵阳市', '盐亭县', (SELECT id FROM organization WHERE code='5107'), 1),
('510725', '梓潼县', 3, 2020, 'BASELINE', '四川省', '绵阳市', '梓潼县', (SELECT id FROM organization WHERE code='5107'), 1),
('510726', '北川羌族自治县', 3, 2020, 'BASELINE', '四川省', '绵阳市', '北川羌族自治县', (SELECT id FROM organization WHERE code='5107'), 1),
('510727', '平武县', 3, 2020, 'BASELINE', '四川省', '绵阳市', '平武县', (SELECT id FROM organization WHERE code='5107'), 1),
('510781', '江油市', 3, 2020, 'BASELINE', '四川省', '绵阳市', '江油市', (SELECT id FROM organization WHERE code='5107'), 1);

-- 广元市区县（7个）
INSERT INTO organization (code, name, level, year, data_source, province_name, city_name, county_name, parent_id, is_baseline) VALUES
('510802', '利州区', 3, 2020, 'BASELINE', '四川省', '广元市', '利州区', (SELECT id FROM organization WHERE code='5108'), 1),
('510811', '昭化区', 3, 2020, 'BASELINE', '四川省', '广元市', '昭化区', (SELECT id FROM organization WHERE code='5108'), 1),
('510812', '朝天区', 3, 2020, 'BASELINE', '四川省', '广元市', '朝天区', (SELECT id FROM organization WHERE code='5108'), 1),
('510821', '旺苍县', 3, 2020, 'BASELINE', '四川省', '广元市', '旺苍县', (SELECT id FROM organization WHERE code='5108'), 1),
('510822', '青川县', 3, 2020, 'BASELINE', '四川省', '广元市', '青川县', (SELECT id FROM organization WHERE code='5108'), 1),
('510823', '剑阁县', 3, 2020, 'BASELINE', '四川省', '广元市', '剑阁县', (SELECT id FROM organization WHERE code='5108'), 1),
('510824', '苍溪县', 3, 2020, 'BASELINE', '四川省', '广元市', '苍溪县', (SELECT id FROM organization WHERE code='5108'), 1);

-- 遂宁市区县（5个）
INSERT INTO organization (code, name, level, year, data_source, province_name, city_name, county_name, parent_id, is_baseline) VALUES
('510903', '船山区', 3, 2020, 'BASELINE', '四川省', '遂宁市', '船山区', (SELECT id FROM organization WHERE code='5109'), 1),
('510904', '安居区', 3, 2020, 'BASELINE', '四川省', '遂宁市', '安居区', (SELECT id FROM organization WHERE code='5109'), 1),
('510921', '蓬溪县', 3, 2020, 'BASELINE', '四川省', '遂宁市', '蓬溪县', (SELECT id FROM organization WHERE code='5109'), 1),
('510923', '大英县', 3, 2020, 'BASELINE', '四川省', '遂宁市', '大英县', (SELECT id FROM organization WHERE code='5109'), 1),
('510981', '射洪县', 3, 2020, 'BASELINE', '四川省', '遂宁市', '射洪县', (SELECT id FROM organization WHERE code='5109'), 1);

-- 内江市区县（5个）
INSERT INTO organization (code, name, level, year, data_source, province_name, city_name, county_name, parent_id, is_baseline) VALUES
('511002', '市中区', 3, 2020, 'BASELINE', '四川省', '内江市', '市中区', (SELECT id FROM organization WHERE code='5110'), 1),
('511011', '东兴区', 3, 2020, 'BASELINE', '四川省', '内江市', '东兴区', (SELECT id FROM organization WHERE code='5110'), 1),
('511024', '威远县', 3, 2020, 'BASELINE', '四川省', '内江市', '威远县', (SELECT id FROM organization WHERE code='5110'), 1),
('511025', '资中县', 3, 2020, 'BASELINE', '四川省', '内江市', '资中县', (SELECT id FROM organization WHERE code='5110'), 1),
('511083', '隆昌县', 3, 2020, 'BASELINE', '四川省', '内江市', '隆昌县', (SELECT id FROM organization WHERE code='5110'), 1);

-- 乐山市区县（11个）
INSERT INTO organization (code, name, level, year, data_source, province_name, city_name, county_name, parent_id, is_baseline) VALUES
('511102', '市中区', 3, 2020, 'BASELINE', '四川省', '乐山市', '市中区', (SELECT id FROM organization WHERE code='5111'), 1),
('511111', '沙湾区', 3, 2020, 'BASELINE', '四川省', '乐山市', '沙湾区', (SELECT id FROM organization WHERE code='5111'), 1),
('511112', '五通桥区', 3, 2020, 'BASELINE', '四川省', '乐山市', '五通桥区', (SELECT id FROM organization WHERE code='5111'), 1),
('511113', '金口河区', 3, 2020, 'BASELINE', '四川省', '乐山市', '金口河区', (SELECT id FROM organization WHERE code='5111'), 1),
('511123', '犍为县', 3, 2020, 'BASELINE', '四川省', '乐山市', '犍为县', (SELECT id FROM organization WHERE code='5111'), 1),
('511124', '井研县', 3, 2020, 'BASELINE', '四川省', '乐山市', '井研县', (SELECT id FROM organization WHERE code='5111'), 1),
('511126', '夹江县', 3, 2020, 'BASELINE', '四川省', '乐山市', '夹江县', (SELECT id FROM organization WHERE code='5111'), 1),
('511129', '沐川县', 3, 2020, 'BASELINE', '四川省', '乐山市', '沐川县', (SELECT id FROM organization WHERE code='5111'), 1),
('511132', '峨边彝族自治县', 3, 2020, 'BASELINE', '四川省', '乐山市', '峨边彝族自治县', (SELECT id FROM organization WHERE code='5111'), 1),
('511133', '马边彝族自治县', 3, 2020, 'BASELINE', '四川省', '乐山市', '马边彝族自治县', (SELECT id FROM organization WHERE code='5111'), 1),
('511181', '峨眉山市', 3, 2020, 'BASELINE', '四川省', '乐山市', '峨眉山市', (SELECT id FROM organization WHERE code='5111'), 1);

-- 南充市区县（9个）
INSERT INTO organization (code, name, level, year, data_source, province_name, city_name, county_name, parent_id, is_baseline) VALUES
('511302', '顺庆区', 3, 2020, 'BASELINE', '四川省', '南充市', '顺庆区', (SELECT id FROM organization WHERE code='5113'), 1),
('511303', '高坪区', 3, 2020, 'BASELINE', '四川省', '南充市', '高坪区', (SELECT id FROM organization WHERE code='5113'), 1),
('511304', '嘉陵区', 3, 2020, 'BASELINE', '四川省', '南充市', '嘉陵区', (SELECT id FROM organization WHERE code='5113'), 1),
('511321', '南部县', 3, 2020, 'BASELINE', '四川省', '南充市', '南部县', (SELECT id FROM organization WHERE code='5113'), 1),
('511322', '营山县', 3, 2020, 'BASELINE', '四川省', '南充市', '营山县', (SELECT id FROM organization WHERE code='5113'), 1),
('511323', '蓬安县', 3, 2020, 'BASELINE', '四川省', '南充市', '蓬安县', (SELECT id FROM organization WHERE code='5113'), 1),
('511324', '仪陇县', 3, 2020, 'BASELINE', '四川省', '南充市', '仪陇县', (SELECT id FROM organization WHERE code='5113'), 1),
('511325', '西充县', 3, 2020, 'BASELINE', '四川省', '南充市', '西充县', (SELECT id FROM organization WHERE code='5113'), 1),
('511381', '阆中市', 3, 2020, 'BASELINE', '四川省', '南充市', '阆中市', (SELECT id FROM organization WHERE code='5113'), 1);

-- 眉山市区县（6个）
INSERT INTO organization (code, name, level, year, data_source, province_name, city_name, county_name, parent_id, is_baseline) VALUES
('511402', '东坡区', 3, 2020, 'BASELINE', '四川省', '眉山市', '东坡区', (SELECT id FROM organization WHERE code='5114'), 1),
('511403', '彭山区', 3, 2020, 'BASELINE', '四川省', '眉山市', '彭山区', (SELECT id FROM organization WHERE code='5114'), 1),
('511421', '仁寿县', 3, 2020, 'BASELINE', '四川省', '眉山市', '仁寿县', (SELECT id FROM organization WHERE code='5114'), 1),
('511423', '洪雅县', 3, 2020, 'BASELINE', '四川省', '眉山市', '洪雅县', (SELECT id FROM organization WHERE code='5114'), 1),
('511424', '丹棱县', 3, 2020, 'BASELINE', '四川省', '眉山市', '丹棱县', (SELECT id FROM organization WHERE code='5114'), 1),
('511425', '青神县', 3, 2020, 'BASELINE', '四川省', '眉山市', '青神县', (SELECT id FROM organization WHERE code='5114'), 1);

-- 宜宾市区县（10个）
INSERT INTO organization (code, name, level, year, data_source, province_name, city_name, county_name, parent_id, is_baseline) VALUES
('511502', '翠屏区', 3, 2020, 'BASELINE', '四川省', '宜宾市', '翠屏区', (SELECT id FROM organization WHERE code='5115'), 1),
('511503', '南溪区', 3, 2020, 'BASELINE', '四川省', '宜宾市', '南溪区', (SELECT id FROM organization WHERE code='5115'), 1),
('511504', '宜宾县', 3, 2020, 'BASELINE', '四川省', '宜宾市', '宜宾县', (SELECT id FROM organization WHERE code='5115'), 1),
('511523', '江安县', 3, 2020, 'BASELINE', '四川省', '宜宾市', '江安县', (SELECT id FROM organization WHERE code='5115'), 1),
('511524', '长宁县', 3, 2020, 'BASELINE', '四川省', '宜宾市', '长宁县', (SELECT id FROM organization WHERE code='5115'), 1),
('511525', '高县', 3, 2020, 'BASELINE', '四川省', '宜宾市', '高县', (SELECT id FROM organization WHERE code='5115'), 1),
('511526', '珙县', 3, 2020, 'BASELINE', '四川省', '宜宾市', '珙县', (SELECT id FROM organization WHERE code='5115'), 1),
('511527', '筠连县', 3, 2020, 'BASELINE', '四川省', '宜宾市', '筠连县', (SELECT id FROM organization WHERE code='5115'), 1),
('511528', '兴文县', 3, 2020, 'BASELINE', '四川省', '宜宾市', '兴文县', (SELECT id FROM organization WHERE code='5115'), 1),
('511529', '屏山县', 3, 2020, 'BASELINE', '四川省', '宜宾市', '屏山县', (SELECT id FROM organization WHERE code='5115'), 1);

-- 广安市区县（6个）
INSERT INTO organization (code, name, level, year, data_source, province_name, city_name, county_name, parent_id, is_baseline) VALUES
('511602', '广安区', 3, 2020, 'BASELINE', '四川省', '广安市', '广安区', (SELECT id FROM organization WHERE code='5116'), 1),
('511603', '前锋区', 3, 2020, 'BASELINE', '四川省', '广安市', '前锋区', (SELECT id FROM organization WHERE code='5116'), 1),
('511621', '岳池县', 3, 2020, 'BASELINE', '四川省', '广安市', '岳池县', (SELECT id FROM organization WHERE code='5116'), 1),
('511622', '武胜县', 3, 2020, 'BASELINE', '四川省', '广安市', '武胜县', (SELECT id FROM organization WHERE code='5116'), 1),
('511623', '邻水县', 3, 2020, 'BASELINE', '四川省', '广安市', '邻水县', (SELECT id FROM organization WHERE code='5116'), 1),
('511681', '华蓥市', 3, 2020, 'BASELINE', '四川省', '广安市', '华蓥市', (SELECT id FROM organization WHERE code='5116'), 1);

-- 达州市区县（7个）
INSERT INTO organization (code, name, level, year, data_source, province_name, city_name, county_name, parent_id, is_baseline) VALUES
('511702', '通川区', 3, 2020, 'BASELINE', '四川省', '达州市', '通川区', (SELECT id FROM organization WHERE code='5117'), 1),
('511703', '达川区', 3, 2020, 'BASELINE', '四川省', '达州市', '达川区', (SELECT id FROM organization WHERE code='5117'), 1),
('511722', '宣汉县', 3, 2020, 'BASELINE', '四川省', '达州市', '宣汉县', (SELECT id FROM organization WHERE code='5117'), 1),
('511723', '开江县', 3, 2020, 'BASELINE', '四川省', '达州市', '开江县', (SELECT id FROM organization WHERE code='5117'), 1),
('511724', '大竹县', 3, 2020, 'BASELINE', '四川省', '达州市', '大竹县', (SELECT id FROM organization WHERE code='5117'), 1),
('511725', '渠县', 3, 2020, 'BASELINE', '四川省', '达州市', '渠县', (SELECT id FROM organization WHERE code='5117'), 1),
('511781', '万源市', 3, 2020, 'BASELINE', '四川省', '达州市', '万源市', (SELECT id FROM organization WHERE code='5117'), 1);

-- 雅安市区县（8个）
INSERT INTO organization (code, name, level, year, data_source, province_name, city_name, county_name, parent_id, is_baseline) VALUES
('511802', '雨城区', 3, 2020, 'BASELINE', '四川省', '雅安市', '雨城区', (SELECT id FROM organization WHERE code='5118'), 1),
('511803', '名山区', 3, 2020, 'BASELINE', '四川省', '雅安市', '名山区', (SELECT id FROM organization WHERE code='5118'), 1),
('511822', '荥经县', 3, 2020, 'BASELINE', '四川省', '雅安市', '荥经县', (SELECT id FROM organization WHERE code='5118'), 1),
('511823', '汉源县', 3, 2020, 'BASELINE', '四川省', '雅安市', '汉源县', (SELECT id FROM organization WHERE code='5118'), 1),
('511824', '石棉县', 3, 2020, 'BASELINE', '四川省', '雅安市', '石棉县', (SELECT id FROM organization WHERE code='5118'), 1),
('511825', '天全县', 3, 2020, 'BASELINE', '四川省', '雅安市', '天全县', (SELECT id FROM organization WHERE code='5118'), 1),
('511826', '芦山县', 3, 2020, 'BASELINE', '四川省', '雅安市', '芦山县', (SELECT id FROM organization WHERE code='5118'), 1),
('511827', '宝兴县', 3, 2020, 'BASELINE', '四川省', '雅安市', '宝兴县', (SELECT id FROM organization WHERE code='5118'), 1);

-- 巴中市区县（5个）
INSERT INTO organization (code, name, level, year, data_source, province_name, city_name, county_name, parent_id, is_baseline) VALUES
('511902', '巴州区', 3, 2020, 'BASELINE', '四川省', '巴中市', '巴州区', (SELECT id FROM organization WHERE code='5119'), 1),
('511903', '恩阳区', 3, 2020, 'BASELINE', '四川省', '巴中市', '恩阳区', (SELECT id FROM organization WHERE code='5119'), 1),
('511921', '通江县', 3, 2020, 'BASELINE', '四川省', '巴中市', '通江县', (SELECT id FROM organization WHERE code='5119'), 1),
('511922', '南江县', 3, 2020, 'BASELINE', '四川省', '巴中市', '南江县', (SELECT id FROM organization WHERE code='5119'), 1),
('511923', '平昌县', 3, 2020, 'BASELINE', '四川省', '巴中市', '平昌县', (SELECT id FROM organization WHERE code='5119'), 1);

-- 资阳市区县（4个）
INSERT INTO organization (code, name, level, year, data_source, province_name, city_name, county_name, parent_id, is_baseline) VALUES
('512002', '雁江区', 3, 2020, 'BASELINE', '四川省', '资阳市', '雁江区', (SELECT id FROM organization WHERE code='5120'), 1),
('512021', '安岳县', 3, 2020, 'BASELINE', '四川省', '资阳市', '安岳县', (SELECT id FROM organization WHERE code='5120'), 1),
('512022', '乐至县', 3, 2020, 'BASELINE', '四川省', '资阳市', '乐至县', (SELECT id FROM organization WHERE code='5120'), 1);

-- 阿坝藏族羌族自治州县（13个）
INSERT INTO organization (code, name, level, year, data_source, province_name, city_name, county_name, parent_id, is_baseline) VALUES
('513201', '马尔康县', 3, 2020, 'BASELINE', '四川省', '阿坝藏族羌族自治州', '马尔康县', (SELECT id FROM organization WHERE code='5132'), 1),
('513221', '汶川县', 3, 2020, 'BASELINE', '四川省', '阿坝藏族羌族自治州', '汶川县', (SELECT id FROM organization WHERE code='5132'), 1),
('513222', '理县', 3, 2020, 'BASELINE', '四川省', '阿坝藏族羌族自治州', '理县', (SELECT id FROM organization WHERE code='5132'), 1),
('513223', '茂县', 3, 2020, 'BASELINE', '四川省', '阿坝藏族羌族自治州', '茂县', (SELECT id FROM organization WHERE code='5132'), 1),
('513224', '松潘县', 3, 2020, 'BASELINE', '四川省', '阿坝藏族羌族自治州', '松潘县', (SELECT id FROM organization WHERE code='5132'), 1),
('513225', '九寨沟县', 3, 2020, 'BASELINE', '四川省', '阿坝藏族羌族自治州', '九寨沟县', (SELECT id FROM organization WHERE code='5132'), 1),
('513226', '金川县', 3, 2020, 'BASELINE', '四川省', '阿坝藏族羌族自治州', '金川县', (SELECT id FROM organization WHERE code='5132'), 1),
('513227', '小金县', 3, 2020, 'BASELINE', '四川省', '阿坝藏族羌族自治州', '小金县', (SELECT id FROM organization WHERE code='5132'), 1),
('513228', '黑水县', 3, 2020, 'BASELINE', '四川省', '阿坝藏族羌族自治州', '黑水县', (SELECT id FROM organization WHERE code='5132'), 1),
('513230', '壤塘县', 3, 2020, 'BASELINE', '四川省', '阿坝藏族羌族自治州', '壤塘县', (SELECT id FROM organization WHERE code='5132'), 1),
('513231', '阿坝县', 3, 2020, 'BASELINE', '四川省', '阿坝藏族羌族自治州', '阿坝县', (SELECT id FROM organization WHERE code='5132'), 1),
('513232', '若尔盖县', 3, 2020, 'BASELINE', '四川省', '阿坝藏族羌族自治州', '若尔盖县', (SELECT id FROM organization WHERE code='5132'), 1),
('513233', '红原县', 3, 2020, 'BASELINE', '四川省', '阿坝藏族羌族自治州', '红原县', (SELECT id FROM organization WHERE code='5132'), 1);

-- 甘孜藏族自治州县（18个）
INSERT INTO organization (code, name, level, year, data_source, province_name, city_name, county_name, parent_id, is_baseline) VALUES
('513301', '康定县', 3, 2020, 'BASELINE', '四川省', '甘孜藏族自治州', '康定县', (SELECT id FROM organization WHERE code='5133'), 1),
('513322', '泸定县', 3, 2020, 'BASELINE', '四川省', '甘孜藏族自治州', '泸定县', (SELECT id FROM organization WHERE code='5133'), 1),
('513323', '丹巴县', 3, 2020, 'BASELINE', '四川省', '甘孜藏族自治州', '丹巴县', (SELECT id FROM organization WHERE code='5133'), 1),
('513324', '九龙县', 3, 2020, 'BASELINE', '四川省', '甘孜藏族自治州', '九龙县', (SELECT id FROM organization WHERE code='5133'), 1),
('513325', '雅江县', 3, 2020, 'BASELINE', '四川省', '甘孜藏族自治州', '雅江县', (SELECT id FROM organization WHERE code='5133'), 1),
('513326', '道孚县', 3, 2020, 'BASELINE', '四川省', '甘孜藏族自治州', '道孚县', (SELECT id FROM organization WHERE code='5133'), 1),
('513327', '炉霍县', 3, 2020, 'BASELINE', '四川省', '甘孜藏族自治州', '炉霍县', (SELECT id FROM organization WHERE code='5133'), 1),
('513328', '甘孜县', 3, 2020, 'BASELINE', '四川省', '甘孜藏族自治州', '甘孜县', (SELECT id FROM organization WHERE code='5133'), 1),
('513329', '新龙县', 3, 2020, 'BASELINE', '四川省', '甘孜藏族自治州', '新龙县', (SELECT id FROM organization WHERE code='5133'), 1),
('513330', '德格县', 3, 2020, 'BASELINE', '四川省', '甘孜藏族自治州', '德格县', (SELECT id FROM organization WHERE code='5133'), 1),
('513331', '白玉县', 3, 2020, 'BASELINE', '四川省', '甘孜藏族自治州', '白玉县', (SELECT id FROM organization WHERE code='5133'), 1),
('513332', '石渠县', 3, 2020, 'BASELINE', '四川省', '甘孜藏族自治州', '石渠县', (SELECT id FROM organization WHERE code='5133'), 1),
('513333', '色达县', 3, 2020, 'BASELINE', '四川省', '甘孜藏族自治州', '色达县', (SELECT id FROM organization WHERE code='5133'), 1),
('513334', '理塘县', 3, 2020, 'BASELINE', '四川省', '甘孜藏族自治州', '理塘县', (SELECT id FROM organization WHERE code='5133'), 1),
('513335', '巴塘县', 3, 2020, 'BASELINE', '四川省', '甘孜藏族自治州', '巴塘县', (SELECT id FROM organization WHERE code='5133'), 1),
('513336', '乡城县', 3, 2020, 'BASELINE', '四川省', '甘孜藏族自治州', '乡城县', (SELECT id FROM organization WHERE code='5133'), 1),
('513337', '稻城县', 3, 2020, 'BASELINE', '四川省', '甘孜藏族自治州', '稻城县', (SELECT id FROM organization WHERE code='5133'), 1),
('513338', '得荣县', 3, 2020, 'BASELINE', '四川省', '甘孜藏族自治州', '得荣县', (SELECT id FROM organization WHERE code='5133'), 1);

-- 凉山彝族自治州县（17个）
INSERT INTO organization (code, name, level, year, data_source, province_name, city_name, county_name, parent_id, is_baseline) VALUES
('513401', '西昌市', 3, 2020, 'BASELINE', '四川省', '凉山彝族自治州', '西昌市', (SELECT id FROM organization WHERE code='5134'), 1),
('513422', '木里藏族自治县', 3, 2020, 'BASELINE', '四川省', '凉山彝族自治州', '木里藏族自治县', (SELECT id FROM organization WHERE code='5134'), 1),
('513423', '盐源县', 3, 2020, 'BASELINE', '四川省', '凉山彝族自治州', '盐源县', (SELECT id FROM organization WHERE code='5134'), 1),
('513424', '德昌县', 3, 2020, 'BASELINE', '四川省', '凉山彝族自治州', '德昌县', (SELECT id FROM organization WHERE code='5134'), 1),
('513425', '会理县', 3, 2020, 'BASELINE', '四川省', '凉山彝族自治州', '会理县', (SELECT id FROM organization WHERE code='5134'), 1),
('513426', '会东县', 3, 2020, 'BASELINE', '四川省', '凉山彝族自治州', '会东县', (SELECT id FROM organization WHERE code='5134'), 1),
('513427', '宁南县', 3, 2020, 'BASELINE', '四川省', '凉山彝族自治州', '宁南县', (SELECT id FROM organization WHERE code='5134'), 1),
('513428', '普格县', 3, 2020, 'BASELINE', '四川省', '凉山彝族自治州', '普格县', (SELECT id FROM organization WHERE code='5134'), 1),
('513429', '布拖县', 3, 2020, 'BASELINE', '四川省', '凉山彝族自治州', '布拖县', (SELECT id FROM organization WHERE code='5134'), 1),
('513431', '昭觉县', 3, 2020, 'BASELINE', '四川省', '凉山彝族自治州', '昭觉县', (SELECT id FROM organization WHERE code='5134'), 1),
('513432', '喜德县', 3, 2020, 'BASELINE', '四川省', '凉山彝族自治州', '喜德县', (SELECT id FROM organization WHERE code='5134'), 1),
('513433', '冕宁县', 3, 2020, 'BASELINE', '四川省', '凉山彝族自治州', '冕宁县', (SELECT id FROM organization WHERE code='5134'), 1),
('513434', '越西县', 3, 2020, 'BASELINE', '四川省', '凉山彝族自治州', '越西县', (SELECT id FROM organization WHERE code='5134'), 1),
('513435', '甘洛县', 3, 2020, 'BASELINE', '四川省', '凉山彝族自治州', '甘洛县', (SELECT id FROM organization WHERE code='5134'), 1),
('513436', '美姑县', 3, 2020, 'BASELINE', '四川省', '凉山彝族自治州', '美姑县', (SELECT id FROM organization WHERE code='5134'), 1),
('513437', '雷波县', 3, 2020, 'BASELINE', '四川省', '凉山彝族自治州', '雷波县', (SELECT id FROM organization WHERE code='5134'), 1),
('513430', '金阳县', 3, 2020, 'BASELINE', '四川省', '凉山彝族自治州', '金阳县', (SELECT id FROM organization WHERE code='5134'), 1);

-- 5. 验证数据
SELECT '=== 数据加载完成统计 ===' AS info;
SELECT level, COUNT(*) as count FROM organization GROUP BY level ORDER BY level;
SELECT '总记录数' AS info, COUNT(*) AS total FROM organization;
