-- 测试中文字符数据存储
SET NAMES utf8mb4;
USE evaluate_db;

-- 插入测试数据
INSERT INTO survey_data (
    region_code,
    province,
    city,
    county,
    township,
    main_disaster_types,
    warning_receive_method,
    unit_leader,
    form_filler,
    year
) VALUES (
    'TEST001',
    '四川省',
    '眉山市',
    '青神县',
    '测试乡镇',
    '洪水;地震;滑坡',
    '电话;微信;网络系统',
    '测试负责人',
    '测试填表人',
    2024
);

-- 验证插入的数据
SELECT
    region_code,
    township,
    main_disaster_types,
    warning_receive_method,
    unit_leader,
    form_filler
FROM survey_data
WHERE region_code = 'TEST001';

-- 清理测试数据
DELETE FROM survey_data WHERE region_code = 'TEST001';

SELECT '中文数据测试完成' AS result;